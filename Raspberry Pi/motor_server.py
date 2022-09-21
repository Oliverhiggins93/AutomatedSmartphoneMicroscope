import socket
import RPi.GPIO as GPIO
from RpiMotorLib import RpiMotorLib
import numpy as np
import netifaces
import threading
import time
import os

class MotorServer():
    def __init__(self):
        os.chdir('/home/pi/Desktop/AndroidSmartMicroscopewithUSBStepperControl/Raspberry_pi')
        self.GpioPinsX = [18, 23, 24, 25]
        self.GpioPinsY = [8, 7, 12, 16]
        self.GpioPinsZ = [4, 17, 27, 22]
        self.xmotor = RpiMotorLib.BYJMotor("xmotor", "28BYJ")
        self.ymotor = RpiMotorLib.BYJMotor("ymotor", "28BYJ")
        self.zmotor = RpiMotorLib.BYJMotor("zmotor", "28BYJ")
        self.xlocationsavedir = os.path.join(os.getcwd(), 'motorlocations/xlocation.npy')
        self.ylocationsavedir = os.path.join(os.getcwd(), 'motorlocations/ylocation.npy')
        self.zlocationsavedir = os.path.join(os.getcwd(), 'motorlocations/zlocation.npy')
        self.xtargetsavedir = os.path.join(os.getcwd(), 'motorlocations/xlocationtarget.npy')
        self.ytargetsavedir = os.path.join(os.getcwd(), 'motorlocations/ylocationtarget.npy')
        self.ztargetsavedir = os.path.join(os.getcwd(), 'motorlocations/zlocationtarget.npy')
        
        try:
            self.xlocation = np.load(self.xlocationsavedir)
            self.ylocation = np.load(self.ylocationsavedir)
            self.zlocation = np.load(self.zlocationsavedir)
            self.xlocationtarget = np.load(self.xtargetsavedir)
            self.ylocationtarget = np.load(self.ytargetsavedir)
            self.zlocationtarget = np.load(self.ztargetsavedir)
        except:
            print('exception')
            self.xlocation = 0
            self.ylocation = 0
            self.zlocation = 0
            self.xlocationtarget = 0#np.load('xlocationtarget.npy')
            self.ylocationtarget =  0#np.load('ylocationtarget.npy')
            self.zlocationtarget =  0#np.load('zlocationtarget.npy')
            np.save(self.xlocationsavedir, self.xlocation)
            np.save(self.ylocationsavedir, self.ylocation)
            np.save(self.zlocationsavedir, self.zlocation)
            np.save(self.xtargetsavedir, self.xlocation)
            np.save(self.ytargetsavedir, self.ylocation)
            np.save(self.ztargetsavedir, self.zlocation)
        
    def positiveornegative(self, num):
        if num >= 0:
            return False
        else:
            return True
    
    def movemotorx(self, location):
        try:
            self.xlocationtarget = int(location)
            np.save(self.xlocationsavedir, self.xlocation)
            np.save(self.xtargetsavedir, self.xlocationtarget)
            self.targetlocationx = int(location)
            stepstomovex = (self.targetlocationx - self.xlocation)
            self.xmotor.motor_run(self.GpioPinsX,.001, abs(stepstomovex), self.positiveornegative(stepstomovex), False, "half", .1)
            self.xlocation = self.xlocation + stepstomovex
            np.save(self.xtargetsavedir, self.targetlocationx)
            np.save(self.xlocationsavedir, self.xlocation)
        except:
            print('x motor error')

    def movemotory(self, location):
        try:
            self.ylocationtarget = int(location)
            np.save(self.ylocationsavedir, self.ylocation)
            np.save(self.ytargetsavedir, self.ylocationtarget)
            self.targetlocationy = int(location)
            stepstomovey = int((self.targetlocationy - self.ylocation))
            self.ymotor.motor_run(self.GpioPinsY,.001, abs(stepstomovey), self.positiveornegative(stepstomovey), False, "half", .05)
            self.ylocation = self.ylocation + stepstomovey
            np.save(self.ytargetsavedir, self.targetlocationy)
            np.save(self.ylocationsavedir, self.ylocation)
        except:
            print('y motor error')
    
    def movemotorz(self, location):
        try:
            self.zlocationtarget = int(location)
            self.targetlocationz = int(location)
            np.save(self.ztargetsavedir, self.targetlocationz)
            stepstomovez = int((self.targetlocationz - self.zlocation))
            self.zmotor.motor_run(self.GpioPinsZ,.001, abs(stepstomovez), self.positiveornegative(stepstomovez), False, "half", .05)
            self.zlocation = self.zlocation + stepstomovez         
            print('current location z: ', self.zlocation, 'target: ', self.targetlocationz, 'stepstomove: ', stepstomovez)
        except:
            print('z motor error')

     
    def savelocations(self):
        np.save(self.xlocationsavedir, self.xlocation)
        np.save(self.ylocationsavedir, self.ylocation)
        np.save(self.zlocationsavedir, self.zlocation)
    def gotozero(self):
        self.xlocationtarget = 0
        self.ylocationtarget = 0
        self.zlocationtarget = 0
        np.save(self.xtargetsavedir, 0)
        np.save(self.ytargetsavedir, 0)
        np.save(self.ztargetsavedir, 0)
        
    def setlocationzero(self):
        self.xlocation = 0
        self.ylocation = 0
        self.zlocation = 0
        self.savelocations()
        self.gotozero()
