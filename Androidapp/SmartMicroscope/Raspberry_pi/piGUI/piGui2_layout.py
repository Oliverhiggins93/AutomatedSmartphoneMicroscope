
import sys
from PyQt5.QtWidgets import QMainWindow, QApplication, QPushButton, QWidget, QAction, QTabWidget,QVBoxLayout, QGroupBox, QGridLayout, QLabel, QLineEdit, QComboBox, QListWidget
from PyQt5.QtGui import QIcon, QIntValidator,QImage, QPixmap
from PyQt5.QtCore import pyqtSlot
from PyQt5 import QtCore
from piGui2_functions import guiFunctions
from PIL import Image
from PIL.ImageQt import ImageQt
import numpy as np
import picamera
import os
import io
import time
import threading

class piGui_layout(QWidget):
    
    def __init__(self, parent, rect):
        self.guiFunc = guiFunctions()
        super(QWidget, self).__init__(parent)
        self.layout = QVBoxLayout(self)
        self.screenwidth = rect.width()
        self.screenheight= rect.height()
        
        # Initialize tab screen
        self.tabs = QTabWidget()
        self.tab1 = QWidget()
        self.tab2 = QWidget()
        self.tab3 = QWidget()
        self.tab4 = QWidget()
        self.tab5 = QWidget()
        self.tabs.resize(300,200)
        self.buttonMinHeight = int(rect.height() / 8)
        
        self.addCameraTab()
        self.addSampleTab()
        self.addCameraOptionsTab()
        self.addDetectionsTab()
#        self.tabs.addTab(self.tab4,"Inference")
        self.addQuitTab()
        
        # Add tabs to widget
        self.layout.addWidget(self.tabs)
        self.setLayout(self.layout)
        self.guiFunc.create_sample_folder()
        
        self.depthsperposition = 3
        self.numberpositionsperscan = 196
        self.stepstomovescan = 200
    
    def addCameraOptionsTab(self):
        
        self.tabs.addTab(self.tab3,"Camera Options")
        self.tab3.layout = QVBoxLayout(self)
        gridlayouttab3 = QGridLayout()
        widget = QWidget()
        gridlayouttab3 = QGridLayout()
        for i in range(4):
            gridlayouttab3.setColumnStretch(i, 1)
        self.captureResolution = (2028, 1520)
        self.currentresolutiontext = QLabel("Resolution: " + str(self.captureResolution))
        gridlayouttab3.addWidget(self.currentresolutiontext,1,2)
        
        self.resolutionBox = QComboBox(widget)
        self.resolutionBox.addItems(['4056x3040', '2028x1520', '1332x990', '640x640'])
        self.resolutionBox.currentIndexChanged.connect(self.changeResolution)
        gridlayouttab3.addWidget(self.resolutionBox,2,2)
        self.tab3.setLayout(gridlayouttab3)
        
    def changeResolution(self, index):
        self.resolutions = [(4056,3040), (2028,1520), (1332,990), (640,640)]
        self.captureResolution = self.resolutions[index]
        self.currentresolutiontext.setText("Resolution: " + str(self.captureResolution))
    
    def addDetectionsTab(self):
        
        self.tabs.addTab(self.tab4,"Images")
        self.tab4.layout = QVBoxLayout(self)
        gridlayouttab4 = QGridLayout()
        widget = QWidget()
        for i in range(4):
            gridlayouttab4.setColumnStretch(i, 1)
            
        self.sampleNumberList = os.listdir(self.guiFunc.save_directory)
        
        
        self.sampleNumberDetectionsText = QLabel("Sample number:")
        
        self.sampleNumberDetectionsComboBox = QListWidget(self)
        self.sampleNumberDetectionsComboBox.addItems(self.sampleNumberList)
        self.sampleNumberDetectionsComboBox.currentItemChanged.connect(self.updateImageList)
        
        self.imageListDetectionsText = QLabel("Images:")

        self.imageListDetectionsComboBox = QListWidget(self)
        self.imageListDetectionsComboBox.addItems([])
        self.imageListDetectionsComboBox.currentItemChanged.connect(self.updateImageDisplay)
        
        self.imageDisplayDetections = QLabel(self)
        
        self.imagePlaceholder = np.ones((640,640, 3)).astype(np.uint8)
        self.qimg = QImage(self.imagePlaceholder.data, 640,640, 3*640, QImage.Format_RGB888)
        self.imageDisplayDetections.setPixmap(QPixmap(self.qimg))
        
        #gridlayouttab4.addWidget(self.resolutionBox,2,2)
        gridlayouttab4.addWidget(self.sampleNumberDetectionsText,0,0)
        gridlayouttab4.addWidget(self.sampleNumberDetectionsComboBox,0,1)
        gridlayouttab4.addWidget(self.imageListDetectionsText,1,0)
        gridlayouttab4.addWidget(self.imageListDetectionsComboBox,1,1)
        gridlayouttab4.addWidget(self.imageDisplayDetections,3,3)
        self.tab4.setLayout(gridlayouttab4)
    
    def updateImageDisplay(self):
        try:
            filename = os.path.join(self.guiFunc.save_directory, self.sampleNumberDetectionsComboBox.currentItem().text(), 'images', self.imageListDetectionsComboBox.currentItem().text())
            pixmap = QPixmap(filename)
            self.imageDisplayDetections.setPixmap(pixmap)
        except:
            print('cannot update image')
        
    def updateImageList(self):
        try:
            self.imageListDetectionsComboBox.clear()
            foldername = os.path.join(self.guiFunc.save_directory,  self.sampleNumberDetectionsComboBox.currentItem().text(), 'images')
            print(foldername)
            self.imageListDetectionsComboBox.addItems(os.listdir(foldername))
        except:
            print('no images to list')
        
    def addCameraTab(self):
        # Add tabs
        self.tabs.addTab(self.tab1,"Camera")
        self.tab1.layout = QVBoxLayout(self)
        
        gridlayouttab1 = QGridLayout()
        for i in range(4):
            gridlayouttab1.setColumnStretch(i, 1)
            #gridlayouttab1.setRowStretch(i, 1)
        
        widget = QWidget()
        livecamera = QPushButton(widget)
        livecamera.setText("Preview")
        livecamera.clicked.connect(self.startPreview)
        livecamera.setMinimumHeight(self.buttonMinHeight)
        gridlayouttab1.addWidget(livecamera,1,2)
        
        self.captureButton = QPushButton(widget)
        self.captureButton.setText("Capture image")
        self.captureButton.clicked.connect(self.captureImage)        
        self.captureButton.setMinimumHeight(self.buttonMinHeight)
        self.captureButton.setEnabled(False)        
        gridlayouttab1.addWidget(self.captureButton,1,3)
        
        movexplus = QPushButton(widget)
        movexplus.setText("X+")
        movexplus.clicked.connect(lambda: self.moveStage('x',1))
        movexplus.setMinimumHeight(self.buttonMinHeight)
        gridlayouttab1.addWidget(movexplus,2,2)
        
        movexminus = QPushButton(widget)
        movexminus.setText("X-")
        movexminus.clicked.connect(lambda: self.moveStage('x',-1))
        movexminus.setMinimumHeight(self.buttonMinHeight)    
        gridlayouttab1.addWidget(movexminus,2,3)
        
        moveyplus = QPushButton(widget)
        moveyplus.setText("Y+")
        moveyplus.clicked.connect(lambda: self.moveStage('y',1))
        moveyplus.setMinimumHeight(self.buttonMinHeight)
        gridlayouttab1.addWidget(moveyplus,3,2)
        
        moveyminus = QPushButton(widget)
        moveyminus.setText("Y-")
        moveyminus.clicked.connect(lambda: self.moveStage('y',-1))
        moveyminus.setMinimumHeight(self.buttonMinHeight)
        gridlayouttab1.addWidget(moveyminus,3,3)
        
        focusplus = QPushButton(widget)
        focusplus.setText("Focus+")
        focusplus.clicked.connect(lambda: self.moveStage('z',1))
        focusplus.setMinimumHeight(self.buttonMinHeight)
        gridlayouttab1.addWidget(focusplus,4,2)
        
        focusminus = QPushButton(widget)
        focusminus.setText("Focus-")
        focusminus.clicked.connect(lambda: self.moveStage('z',-1))
        focusminus.setMinimumHeight(self.buttonMinHeight)
        gridlayouttab1.addWidget(focusminus,4,3)
        
        self.scanButton = QPushButton(widget)
        self.scanButton.setText("Scan")
        self.scanButton.clicked.connect(self.startScan)
        self.scanButton.setMinimumHeight(self.buttonMinHeight)
        self.scanButton.setEnabled(False)  
        gridlayouttab1.addWidget(self.scanButton,5,2)
        
        self.currentlyscanningtext = QLabel("Not currently scanning")
        gridlayouttab1.addWidget(self.currentlyscanningtext,5,3)
        
        self.imagenumbertext = QLabel("Image: 000000")
        gridlayouttab1.addWidget(self.imagenumbertext,6,2)
        
        self.samplenumbertext = QLabel("Sample: 000000")
        gridlayouttab1.addWidget(self.samplenumbertext,6,3)
        
        increasestepsxybutton = QPushButton(widget)
        increasestepsxybutton.setText("+")
        increasestepsxybutton.clicked.connect(lambda: self.changeSteps( 'xy', 1.2))
        gridlayouttab1.addWidget(increasestepsxybutton,7,2)
        
        decreasestepsxybutton = QPushButton(widget)
        decreasestepsxybutton.setText("-")
        decreasestepsxybutton.clicked.connect(lambda: self.changeSteps( 'xy', 0.8))
        gridlayouttab1.addWidget(decreasestepsxybutton,7,3)
        
        increasestepsfocusbutton = QPushButton(widget)
        increasestepsfocusbutton.setText("+")
        increasestepsfocusbutton.clicked.connect(lambda: self.changeSteps( 'z', 1.2))
        gridlayouttab1.addWidget(increasestepsfocusbutton,8,2)
        
        decreasestepsfocusbutton = QPushButton(widget)
        decreasestepsfocusbutton.setText("-")
        decreasestepsfocusbutton.clicked.connect(lambda: self.changeSteps( 'z', 0.8))
        gridlayouttab1.addWidget(decreasestepsfocusbutton,8,3)
        
        self.stepspermovexytext = QLabel("Steps to move xy: " + str(self.guiFunc.stepstomovexy))
        gridlayouttab1.addWidget(self.stepspermovexytext,9,2)
        
        self.stepspermoveztext = QLabel("Steps to move focus: " + str(self.guiFunc.stepstomovez))
        gridlayouttab1.addWidget(self.stepspermoveztext,9,3)
        
        self.xlocationtext = QLabel("Location x: " + str(self.guiFunc.m.xlocation))
        gridlayouttab1.addWidget(self.xlocationtext,10,2)
        
        self.ylocationtext = QLabel("Location y: " + str(self.guiFunc.m.ylocation))
        gridlayouttab1.addWidget(self.ylocationtext,11,2)
        
        self.zlocationtext = QLabel("Location z: " + str(self.guiFunc.m.zlocation))
        gridlayouttab1.addWidget(self.zlocationtext,10,3)
        
        self.tab1.setLayout(gridlayouttab1)
    
    def moveStage(self, motor, direction):
       self.guiFunc.motor_move_button(motor, direction)
       self.xlocationtext.setText("Location x: " + str(self.guiFunc.m.xlocation))
       self.ylocationtext.setText("Location y: " + str(self.guiFunc.m.ylocation))
       self.zlocationtext.setText("Location z: " + str(self.guiFunc.m.zlocation))
       print('movestage')
       
    def changeSteps(self, axis, increment):
        if axis == 'xy':
            self.guiFunc.stepstomovexy = int(self.guiFunc.stepstomovexy * increment)
            self.stepspermovexytext.setText("Steps to move xy: " + str(self.guiFunc.stepstomovexy))
        elif axis == 'z':
            self.guiFunc.stepstomovez = int(self.guiFunc.stepstomovez * increment)
            self.stepspermoveztext.setText("Steps to move z: " + str(self.guiFunc.stepstomovez))
    
    def captureImage(self):
        self.guiFunc.create_sample_folder()
        self.guiFunc.image_save_path = os.path.join(self.guiFunc.save_directory, str(self.guiFunc.sample_number).zfill(8), 'images', str(self.guiFunc.image_number).zfill(8) + '.jpg')
        print('Capture: ', self.guiFunc.image_save_path)
        self.camera.resolution= self.captureResolution
        self.camera.capture(self.guiFunc.image_save_path,'jpeg', quality=75)
        self.imagenumbertext.setText(str(self.guiFunc.image_number).zfill(8))
        self.guiFunc.image_number = self.guiFunc.image_number + 1
        
    def startPreview(self):
        previewsize = 0.4
        self.camera = picamera.PiCamera(resolution=(4056, 3040), sensor_mode=3)
        #self.camera = picamera.PiCamera(resolution=(2028, 1520), sensor_mode=2)
        self.camera.start_preview(fullscreen=False,window=(0,int(self.screenheight/4),int(self.screenwidth * previewsize * 1.3),int(self.screenwidth * previewsize)))
        self.camera.awb_mode = 'tungsten'
        print('livePreview')
        self.captureButton.setEnabled(True)
        self.scanButton.setEnabled(True)
    
    def startAutofocus(self):
        #print('Autofocus')
        self.stream = io.BytesIO()
        focusdepthsabsolute = np.array([0,-10,10]) + self.guiFunc.m.zlocation
        depthstreamsizes = []
        #print('focusdepths', focusdepthsabsolute)
        for depth in focusdepthsabsolute:
            self.guiFunc.m.movemotorz(depth)
            self.camera.capture(self.stream, 'jpeg', use_video_port=True)
            depthstreamsizes.append(self.stream.tell())
            #print(self.stream.tell())
            self.stream.seek(0)
            self.stream.truncate(0)
        maxfocusindex = depthstreamsizes.index(max(depthstreamsizes))
        self.guiFunc.m.movemotorz(focusdepthsabsolute[maxfocusindex])
        print('Autofocus complete')
        
    def startScan(self):
        x = threading.Thread(target=self.startScanFunc)
        x.start()
        #x.join()
    
    def startScanFunc(self):
        print('scanning')
        starttime = time.time()
        self.camera.shutter_speed = self.camera.exposure_speed
        self.camera.exposure_mode = 'off'
        g = self.camera.awb_gains
        self.camera.awb_mode = 'off'
        self.camera.awb_gains = g
        
        self.imagesperside = int(np.sqrt(self.numberpositionsperscan))
        self.xstartposition = self.guiFunc.m.xlocation
        self.ystartposition = self.guiFunc.m.ylocation
        self.zstartposition = self.guiFunc.m.zlocation
        self.xpositionstoscan = self.guiFunc.m.xlocation + np.linspace(0, self.stepstomovescan * (self.imagesperside -1), self.imagesperside)
        self.ypositionstoscan = self.guiFunc.m.ylocation + np.linspace(0, self.stepstomovescan * (self.imagesperside -1), self.imagesperside)
        self.zpositionstoscan = np.array([-15,0,15])
        if self.depthsperposition != 3:
            self.zpositionstoscan = np.linspace(0,50,self.depthsperposition) - np.linspace(0,50,self.depthsperposition)[int(self.depthsperposition / 2)]
        
        print("x positions:",self.xpositionstoscan)
        print("y positions",self.ypositionstoscan)
        print("z positions", self.zpositionstoscan)
        
        for yi in range(len(self.ypositionstoscan)):
            for xi in range(len(self.xpositionstoscan)):
                
                self.currentlyscanningtext.setText("Scanning X: " + str(xi) + ", Y: " + str(yi) + ". Elapsed time: " + str((time.time() - starttime)/ 60) + " mins")
                
                self.guiFunc.m.movemotory(self.ypositionstoscan[yi])
                self.guiFunc.m.movemotorx(self.xpositionstoscan[xi])
                
                if xi % 3 ==0:
                    self.startAutofocus()
                self.currentzposition = self.guiFunc.m.zlocation
                for zi in range(len(self.zpositionstoscan)):
                    print("Focus position",self.currentzposition + self.zpositionstoscan[zi])
                    self.guiFunc.m.movemotorz(self.currentzposition + self.zpositionstoscan[zi])
                    time.sleep(1.5)
                    self.captureImage()
                self.guiFunc.m.movemotorz(self.currentzposition)
                
                self.xlocationtext.setText("Location x: " + str(self.guiFunc.m.xlocation))
                self.ylocationtext.setText("Location y: " + str(self.guiFunc.m.ylocation))
                self.zlocationtext.setText("Location z: " + str(self.guiFunc.m.zlocation))

            self.xpositionstoscan = np.flip(self.xpositionstoscan)
        
        self.guiFunc.m.movemotorx(self.xstartposition)
        self.guiFunc.m.movemotory(self.ystartposition)
        self.guiFunc.m.movemotorz(self.zstartposition)
        self.currentlyscanningtext.setText("Scanning complete. Elapsed time: " + str((time.time() - starttime)/ 60) + " mins")
        
    def addSampleTab(self):
        # Add tabs
        self.tabs.addTab(self.tab2,"Sample Options")
        self.tab2.layout = QVBoxLayout(self)
        
        gridlayouttab2 = QGridLayout()
        for i in range(4):
            gridlayouttab2.setColumnStretch(i, 1)
            gridlayouttab2.setRowStretch(i, 1)
              
        self.numbertext = QLabel("Sample number: 000000")
        gridlayouttab2.addWidget(self.numbertext,0,2)
        
        widget = QWidget()
        self.numberentry = QLineEdit()
        self.numberentry.setText("000000")
        self.numberentry.textChanged.connect(lambda: self.updateSampleNumber())
        gridlayouttab2.addWidget(self.numberentry,0,3)
        
        self.currentsavelocation = QLabel("Current save location: " + self.guiFunc.image_save_path)
        gridlayouttab2.addWidget(self.currentsavelocation,1,2)
        
        self.depthstext = QLabel("Depths per position:")
        gridlayouttab2.addWidget(self.depthstext,2,2)
        
        self.depthsentry = QLineEdit()
        self.depthsentry.setText("3")
        self.depthsentry.setValidator(QIntValidator(1, 10, self))
        self.depthsentry.textChanged.connect(lambda: self.updateSampleNumber())
        gridlayouttab2.addWidget(self.depthsentry,2,3)
        
        self.numberpositionsperscantext = QLabel("Positions per scan:")
        gridlayouttab2.addWidget(self.numberpositionsperscantext,3,2)
        
        self.numberpositionsperscanentry = QLineEdit()
        self.numberpositionsperscanentry.setText("196")
        self.numberpositionsperscanentry.setValidator(QIntValidator(16, 400, self))
        self.numberpositionsperscanentry.textChanged.connect(lambda: self.updateSampleNumber())
        gridlayouttab2.addWidget(self.numberpositionsperscanentry,3,3)
        
        self.stepstomovescantext = QLabel("Steps to move scan: 400")
        gridlayouttab2.addWidget(self.stepstomovescantext,4,2)
        
        self.stepstomovescanentry = QLineEdit()
        self.stepstomovescanentry.setText("400")
        self.stepstomovescanentry.setValidator(QIntValidator(50, 400, self))
        self.stepstomovescanentry.textChanged.connect(lambda: self.updateSampleNumber())
        gridlayouttab2.addWidget(self.stepstomovescanentry,4,3)
        
        gridlayouttab2.addWidget(self.scanButton,5,2)
        gridlayouttab2.addWidget(self.currentlyscanningtext,5,3)
        
        self.tab2.setLayout(gridlayouttab2)
    
    def updateSampleNumber(self):
        self.guiFunc.sample_number = str(self.numberentry.text()).zfill(8)
        self.guiFunc.image_directory = os.path.join(str(self.guiFunc.save_directory), str(self.guiFunc.sample_number).zfill(8), 'images')
        #self.guiFunc.create_sample_folder()
        try:
            self.guiFunc.image_number = len(os.listdir(self.guiFunc.image_directory))
            print(self.guiFunc.image_directory, self.guiFunc.image_number)
            self.guiFunc.image_save_path = os.path.join(str(self.guiFunc.save_directory), str(self.guiFunc.sample_number).zfill(8), 'images', str(self.guiFunc.image_number).zfill(8) + '.jpg')
            self.currentsavelocation.setText(self.guiFunc.image_save_path)
        except:
            self.guiFunc.image_number = 0
            self.guiFunc.image_save_path = os.path.join(str(self.guiFunc.save_directory), str(self.guiFunc.sample_number).zfill(8), 'images', str(self.guiFunc.image_number).zfill(8) + '.jpg')
            self.currentsavelocation.setText(self.guiFunc.image_save_path)
            
        self.samplenumbertext.setText(str(self.guiFunc.sample_number))
        self.numbertext.setText(str(self.guiFunc.sample_number))
        
        
        
        try:
            self.depthsperposition = int(self.depthsentry.text())
            self.depthstext.setText("Depths per position: " + str(self.depthsperposition))
        except:
            self.depthsperposition = 3
            self.depthstext.setText("Depths per position: " + str(self.depthsperposition))
        try:
            self.numberpositionsperscan = int(self.numberpositionsperscanentry.text())
            self.numberpositionsperscantext.setText("Positions per scan: " + str(self.numberpositionsperscan))
        except:
            self.numberpositionsperscan = 196
            self.numberpositionsperscantext.setText("Positions per scan: " + str(self.numberpositionsperscan))
        try:
            self.stepstomovescan = int(self.stepstomovescanentry.text())
            self.stepstomovescantext.setText("Steps to move during scan: " + str(self.stepstomovescan))
        except:
            self.stepstomovescan = 200
            self.stepstomovescantext.setText("Steps to move during scan: " + str(self.stepstomovescan))
    
        
    def addQuitTab(self):
        self.tabs.addTab(self.tab5,"Quit")
        self.tab5.layout = QVBoxLayout(self)
        gridlayouttab5 = QGridLayout()
        widget = QWidget()
        for i in range(4):
            gridlayouttab5.setColumnStretch(i, 1)
        quitButton = QPushButton(widget)
        quitButton.setText("Quit")
        quitButton.clicked.connect(lambda: QtCore.QCoreApplication.instance().quit())
        gridlayouttab5.addWidget(quitButton,2,2)
        self.tab5.setLayout(gridlayouttab5)
    
    
 