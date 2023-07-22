'''
A first pass at the PyQt GUI

This relies too much on globals
'''

import sys
from io import BytesIO
from PyQt5 import QtCore, QtGui,QtWidgets, uic
from PyQt5.QtCore import Qt
import numpy as np
import picamera
import time
#import gui_functions.threading as thred
#import gui_functions.photoviewer as photv
#import gui_functions.helper_functions  as ghf
import gui_functions.sample_tracker as sample_tracker
#import gui_functions.microscope_camera
import os
import PIL.Image
import PIL.ExifTags
import time

qtCreatorFile = "piGUI_ui.ui"

save_directory = "/home/pi/microscope/data/"

Ui_MainWindow, _ = uic.loadUiType(qtCreatorFile)

import sys
sys.path.append('.')
sys.path.append('..')
from motor_server import MotorServer
m = MotorServer()

class microscopeGUI(QtWidgets.QMainWindow, Ui_MainWindow):
    def __init__(self):
        '''
        This sets up the GUI mainwindow, connects widgets,
        and initialises calculations
        '''
        # PyQT initialisation code
        QtWidgets.QMainWindow.__init__(self)
        Ui_MainWindow.__init__(self)
        self.setupUi(self)
        self.threadpool = QtCore.QThreadPool()
        
        self.xposition  = m.xlocation
        self.yposition = m.ylocation
        self.zposition = m.zlocation
        
        self.setup_lensfree_camera_buttons()
        #self.setup_save_buttons()
        self.setup_scanning_buttons()
        self.quit_button.clicked.connect(lambda: QtCore.QCoreApplication.instance().quit())
        
    def setup_lensfree_camera_buttons(self):
        #self.lensfree_display = photv.PhotoViewer(self)
        self.lensfree_tab.layout = QtWidgets.QVBoxLayout()
        #self.lensfree_tab.layout.addWidget(self.lensfree_display)
        self.lensfree_tab.setLayout(self.lensfree_tab.layout)
        self.horizontalLayout_4.setAlignment(Qt.AlignBottom)
        self.horizontalLayout_4.setAlignment(Qt.AlignRight)
        #self.lensfree_display.setGeometry(0,0,410*1.2,546*1.5)#0,0,546/1,410/1)
        #self.lensfree_display.setMaximumSize(410*1.2,546*1.5)

        #self.lensfree_camera_live_button.clicked.connect(self.lensfree_camera_live_start)
        #self.lensfree_camera_live_stop_button.clicked.connect(self.lensfree_camera_live_stop)
        #self.lensfree_camera_live_stop_button.setDisabled(True)
        #self.lensfree_fullres_snap_button.clicked.connect(self.snap_lensfree_fullres_frame)
        #self.uscope_live_flag = False  # A flag to stop live capture
        #self.uscope_finished_flag = True #
        #self.scanning_live_flag = False
    
    def setup_scanning_buttons(self):
        self.scanning_forward_button_2.clicked.connect(lambda: m.movemotorx(m.xlocation + 300))
        self.scanning_backward_button_2.clicked.connect(lambda:  m.movemotorx(m.xlocation - 300))
        self.scanning_fullscan_button_2.clicked.connect(self.fullscan)
        self.uscope_live_flag = False  # A flag to stop live capture
        self.uscope_finished_flag = True #
        self.lensfree_live_flag = False
    
    def motor_move_button(self, direction, step, forward):
        GPIO_pins = (14,15,18) #these are for controlling the A4988 half and quarter step function. Currently unused 
        motor1 = RpiMotorLib.A4988Nema(direction, step, GPIO_pins, "A4988")
        motor1.motor_go(forward, "Full", 16, .001, False, .05)
        
    def fullscan(self):
        self.fullscan_countx = 0
        self.fullscan_county = 0
        self.fullscan_direction_counter = 1
        self.fullscan_direction_flag = True
        self.uscope_live_flag = False
        self.lensfree_live_flag = False

        # wait for camera to finish.
        while not self.lensfree_finished_flag:
            time.sleep(0.1)
        t0 = time.time()
        # Capture image
        with picamera.PiCamera(sensor_mode = 3,framerate = 5,resolution = (3280, 2464)) as camera:

            camera.awb_mode = 'auto'
            camera.exposure_mode = 'auto'
            
            while self.fullscan_county < 1 :
                #take image
                with picamera.array.PiBayerArray(camera) as stream:
                    camera.resolution = (3280, 2464)
                    camera.capture(stream,'jpeg',bayer = True)
                    self.lensfree_bayer = stream.array
                    other_im =  PIL.Image.open(stream)
                    exif = other_im._getexif()
                    metadata = exif_codes_to_string(exif)
                    other_im = np.array(other_im)
                    metadata['custom_set_awb_gains'] = camera.awb_gains
                    metadata['custom_set_shutter_speed'] = camera.exposure_speed
                    metadata['custom_set_iso'] = camera.iso
                    metadata['custom_set_analog_gain'] = camera.analog_gain
                    metadata['custom_set_digital_gain'] = camera.digital_gain
                    metadata['custom_set_awb_mode'] = camera.awb_mode
                    metadata['custom_set_exposure_mode'] = camera.exposure_mode
        
                
                other_im = np.array(other_im)
                self.sampleTracker.save_image('lensfree',self.lensfree_bayer,metadata,second_image = other_im)
                self.lensfree_image_number_display.setText(str(self.sampleTracker.lensfree_image_number))

                self.lensfree_image = other_im
                self.update_lensfree_image(reset_zoom = True)
                self.fullscan_countx = self.fullscan_countx + self.fullscan_direction_counter      
                self.motor_move_button(21,20,self.fullscan_direction_flag)
                self.motor_move_button(21,20,self.fullscan_direction_flag)
                self.motor_move_button(21,20,self.fullscan_direction_flag)
                self.update_lensfree_image(reset_zoom = True)
                print(self.fullscan_countx)
                if self.fullscan_countx == 10:
                    self.fullscan_county = self.fullscan_county + 1

            
    def setup_save_buttons(self):
        self.set_sample_number_button.clicked.connect(self.setup_saving)
        #self.manual_save_button.clicked.connect(self.choose_manual_save_folder)

    def change_picamera_settings(self,setting,amount):
        '''
        changes ISO and exposure settings
        Increments or decrements index into allowed values
        '''
        if setting == 'exposure':
            if self.exposure_setting + amount < 0 or self.exposure_setting + amount >= len(exposure_values):
                return 0
            else:
                self.exposure_setting += amount
                self.exposure_display.setText(str(exposure_values[self.exposure_setting]))
        elif setting == 'iso':
            if self.iso_setting + amount < 0 or self.iso_setting + amount >= len(iso_values):
                return 0
            else:
                self.iso_setting += amount
                self.iso_display.setText(str(iso_values[self.iso_setting]))

    def slider_nudge(self,slider_name,direction):
        '''
        Implements nudges for a generic slider
        '''
        if direction == 1:
            getattr(self,slider_name).setValue(getattr(self,slider_name).value()+1)
        else:
            getattr(self,slider_name).setValue(getattr(self,slider_name).value()-1)

    def grab_live_lensfree_frames(self,progress_callback):
        '''
        This method should only be run in a seperate thread
        otherwise your GUI will hang.
        '''

        with picamera.PiCamera(sensor_mode = 4,resolution = self.lensfree_im_sz,framerate = 25) as camera:
            with picamera.array.PiRGBArray(camera,size = self.lensfree_im_sz) as output:

                time.sleep(0.1) #allow cam warm up
                # set black and white for display
                # capture_continuous is an iterator over the camera output
                for frame in camera.capture_continuous(output,format = 'rgb',use_video_port = True):
                    camera.awb_mode = 'auto'
                    camera.exposure_mode = 'auto'

                    t0  = time.time()
                    lensfree_image = frame.array

                    # if the checkbox is checked propagate. This may be too slow
        

                    #send result to main thread
                    progress_callback.emit(lensfree_image)


                    if not self.lensfree_live_flag:
                        break

                    if time.time() - t0 < 0.1:
                        time.sleep(0.1 - (time.time()-t0))

                    output.truncate(0) # need to delete data before next acquisition


        self.lensfree_finished_flag = True

    def grab_live_scanning_frames(self,progress_callback):
        '''
        This method should only be run in a seperate thread
        otherwise your GUI will hang.
        '''

        with picamera.PiCamera(sensor_mode = 4,resolution = self.lensfree_im_sz,framerate = 10) as camera:
            with picamera.array.PiRGBArray(camera,size = self.lensfree_im_sz) as output:

                time.sleep(0.1) #allow cam warm up
                # set black and white for display
                camera.color_effects = (128,128)
                # capture_continuous is an iterator over the camera output
                for frame in camera.capture_continuous(output,format = 'bgr',use_video_port = True):
                    camera.awb_mode = 'auto'
                    camera.exposure_mode = 'auto'

                    t0  = time.time()
                    scanning_image = frame.array

            

                    #send result to main thread
                    progress_callback.emit(scanning_image)


                    if not self.scanning_live_flag:
                        break

                    if time.time() - t0 < 0.1:
                        time.sleep(0.1 - (time.time()-t0))

                    output.truncate(0) # need to delete data before next acquisition


        self.scanning_finished_flag = True
    def snap_scanning_fullres_frame(self):
        '''
        Assumes that file directories have been set.
        '''
        # stop both cameras
        self.uscope_live_flag = False
        self.scanning_live_flag = False

        # wait for camera to finish.
        while not self.scanning_finished_flag:
            time.sleep(0.1)
        t0 = time.time()
        # Capture image
        with picamera.PiCamera(sensor_mode = 3,framerate = 5) as camera:
            camera.awb_mode = 'auto'
            camera.exposure_mode = 'auto'

            #take image
            with picamera.array.PiBayerArray(camera) as stream:
                camera.capture(stream,'jpeg',bayer = True)
                self.scanning_bayer = stream.array
                self.sampleTracker.save_image('lensfree',self.scanning_bayer,[])
                self.lensfree_image_number_display.setText(str(self.sampleTracker.lensfree_image_number))
            self.update_scanning_image(reset_zoom = True)


    def get_sample_number(self):
        sample_number = ''
        for i in range(5):
            sample_number += str(getattr(self,'sample_num_'+str(i)).value())
        return sample_number


    def setup_saving(self):
        #get sample number
        self.sample_number = self.get_sample_number()
        print(self.sample_number)
        self.sampleTracker = sample_tracker.SampleTracker(save_directory,self.sample_number)
        self.save_location_display.setText(str(self.sampleTracker.sample_savedir_microscope.resolve()))
        for i in range(2,3):
            getattr(self,'sample_number_display_'+str(i)).setText(str(self.sample_number))
        self.lensfree_image_number_display.setText(str(0))
        


    def choose_manual_save_folder(self):
        fname = QtWidgets.QFileDialog.getOpenFileName(self,'select a file location','/home/pi/lensfree/data')
        self.sample_savedir = fname
        self.save_location_display.setText(self.sample_savedir)



if __name__ == "__main__":
    '''
    This executes the app if this script is run
    '''
    app = QtWidgets.QApplication(sys.argv)
    app.aboutToQuit.connect(app.deleteLater)
    window = microscopeGUI()
    window.showFullScreen()
    sys.exit(app.exec_())
