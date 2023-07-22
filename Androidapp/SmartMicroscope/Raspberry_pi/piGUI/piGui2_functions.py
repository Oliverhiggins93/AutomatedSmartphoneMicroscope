import sys
from io import BytesIO
from PyQt5 import QtCore, QtGui,QtWidgets, uic
import numpy as np
import picamera
import time
import gui_functions.threading as thred
import gui_functions.photoviewer as photv
import gui_functions.helper_functions  as ghf
import gui_functions.sample_tracker as sample_tracker
import gui_functions.microscope_camera
import os
from RpiMotorLib import RpiMotorLib
import time

class guiFunctions():
    
    def __init__(self):
        self.root_directory = "/home/pi/microscope"
        self.save_directory = "/home/pi/microscope/data"
        
        sys.path.append('.')
        sys.path.append('..')
        
        self.sample_number = '00000000'
        
        try:
            
            self.image_directory = os.path.join(self.save_directory, self.sample_number, 'images')
            self.image_number = len(os.listdir(self.image_directory))
            self.image_save_path = os.path.join(self.save_directory, self.sample_number, 'images' , str(self.image_number).zfill(8) + '.jpg')
        except:
            self.image_save_path = os.path.join(self.save_directory, self.sample_number, 'images' , "00000000" + '.jpg')

        from motor_server import MotorServer
        self.m = MotorServer()
    
        self.xposition  = self.m.xlocation
        self.yposition = self.m.ylocation
        self.zposition = self.m.zlocation
        
        self.stepstomovexy = 200
        self.stepstomovez = 50
        
        self.create_sample_folder()
    
    def motor_move_button(self, motor, direction):
        print('motormove')
        if motor == 'x':
            locationtomove = self.xposition + (self.stepstomovexy * direction)
            self.m.movemotorx(locationtomove)
            self.xposition = locationtomove
        elif motor == 'y':
            locationtomove = self.yposition + (self.stepstomovexy * direction)
            self.m.movemotory(locationtomove)
            self.yposition = locationtomove
        elif motor == 'z':
            locationtomove = self.zposition + (self.stepstomovez * direction)
            self.m.movemotorz(locationtomove)
            self.zposition = locationtomove
        
    def create_sample_folder(self):
        try:
            os.mkdir(self.root_directory)
        except OSError as error:
            print(error)
        try:
            print(self.save_directory)
            os.mkdir(self.save_directory)
        except OSError as error:
            print(error)
        try:
            print(os.path.join(self.save_directory, str(self.sample_number).zfill(8)))
            os.mkdir(os.path.join(self.save_directory, str(self.sample_number).zfill(8)))
        except OSError as error:
            print(error)
        try:
            print(os.path.join(self.save_directory, str(self.sample_number).zfill(8), 'images'))
            os.mkdir(os.path.join(self.save_directory, str(self.sample_number).zfill(8), 'images'))
        except OSError as error:
            print(error)
        
              
    def set_sample_number(self,text):
        self.sample_number = text.zfill(8)
        
    def get_save_location(self,text):
        self.save_directory = "/home/pi/lensfree/data/" + self.sample_number
        return self.save_directory

    def setup_saving(self, save_directory):
        #get sample number
        self.sample_number = self.get_sample_number()
        print(self.sample_number)
        self.sampleTracker = sample_tracker.SampleTracker(save_directory,self.sample_number)
        #self.save_location_display.setText(str(self.sampleTracker.sample_savedir_microscope.resolve()))
        #for i in range(2,3):
        #    getattr(self,'sample_number_display_'+str(i)).setText(str(self.sample_number))
        #self.lensfree_image_number_display.setText(str(0))
        


    def choose_manual_save_folder(self):
        fname = QtWidgets.QFileDialog.getOpenFileName(self,'select a file location','/home/pi/lensfree/data')
        self.sample_savedir = fname
        self.save_location_display.setText(self.sample_savedir)

if __name__ == '__main__':
    guiFunc = guiFunctions()