import io
import socket
import struct
import time
import picamera
import threading
import netifaces
from motor_server import MotorServer
import os
import numpy as np
from multiprocessing import Pool, cpu_count, Process
import concurrent.futures


class imageServer():
    def __init__(self):
        self.imagelen = 0
        self.imagelenfocus = 0
        self.imageready = False
        self.commsthreadrunning = False
        self.currentlymovingflag = False
        self.saveflag = False
        self.captureImage = False
        self.changeresolution = False
        self.previewresolution = (405, 304)
        self.largeresolution = (4056, 3040)
        self.exposuremode = 'default'
        self.whitebalance = 'default'
        self.m = MotorServer()
        self.client_socket = 0
        
        self.startStreaming()
    
    def startStreaming(self):
        
        with picamera.PiCamera() as camera:
            #camera.sensor_mode = 3
            camera.start_preview()
            video_port = True
            self.stream = io.BytesIO()
            serverthread = threading.Thread(target=self.serverthreadfunc)
            serverthread.start()
            camera.resolution = self.previewresolution
            #camera.exposure_mode = "backlight"
            #camera.awb_mode = 'off'
            #camera.awb_gains = (2.2,2.2)
            camera.awb_mode = 'tungsten'
            while(True):
                print(self.previewresolution[0])
                if int(self.previewresolution[0]) < 2000:
                    camera.framerate = 40
                else:
                    camera.framerate = 10
                    camera.sensor_mode = 3
                camera.resolution = self.previewresolution
                print(self.previewresolution)
                for imagecapture in camera.capture_continuous(self.stream, format='jpeg', use_video_port=video_port):
                    self.imagelenfocus = self.stream.tell()
                    if self.imageready == False:
                        if int(self.stream.tell()) > 100:
                            self.imagelen = self.stream.tell()
                            self.stream.seek(0)
                            self.streamim = self.stream.read()
                            self.imageready = True
                    self.stream.seek(0)
                    self.stream.truncate()
                    if self.captureImage == True and camera.resolution != self.largeresolution:
                        print('breaking', camera.resolution, self.largeresolution)
                        self.captureImage = False
                        break
                    elif self.changeresolution == True:
                        print('changing resolution')
                        self.changeresolution = False
                        break
                
                if self.exposuremode == 'off':
                    camera.shutter_speed = camera.exposure_speed
                    camera.exposure_mode = 'off'
                elif self.exposuremode == "auto":
                    camera.exposure_mode = 'auto'
                if self.whitebalance == 'off':
                    g = camera.awb_gains
                    camera.awb_mode = 'off'
                    camera.awb_gains = g
                elif self.whitebalance == 'auto':
                    camera.awb_mode = 'auto'

    def serverthreadfunc(self):
        print('connecting socket')
        while(True):
            try:
                ipaddress = netifaces.gateways()['default'][2][0]
                if ipaddress == '192.168.1.1':
                    ipaddress = '192.168.1.26'
                
                client_socket = socket.socket()
                client_socket.setblocking(False)
                client_socket.settimeout(1)
                client_socket.connect((ipaddress, 8000))
                
                connectedBoolean = True

                while(connectedBoolean):
                    #print('connectedboolean')
                    if self.imageready == True:

                        try:
                            connection = client_socket.makefile('wb')
                            #x y and z coordinates

                            if self.imagelen > 100:
                                connection.write(struct.pack('>L', int(self.imagelen)))#connection.write(struct.pack('>L', stream.tell()))
                                connection.flush()
                                connection.write(self.streamim)#connection.write(stream.read())
                                xtarget = int(self.m.xlocationtarget)
                                ytarget = int(self.m.ylocationtarget)
                                ztarget = int(self.m.zlocationtarget)
                                connection.write(struct.pack('>i', xtarget))
                                connection.write(struct.pack('>i', ytarget))
                                connection.write(struct.pack('>i', ztarget))
                                connection.flush()
                                connection.write(struct.pack('>?', self.currentlymovingflag))
                                connection.flush()
                                #connection.write(struct.pack('>?', captureImage))
                                #connection.flush()
                                #print('serverthread', threading.current_thread().ident)
                                self.imageready = False
                                #captureImage = False
                            if self.commsthreadrunning == False:
                                #commsthread = Process(target=commsthreadfunc, args=[client_socket])
                                try:
                                    commsthread = threading.Thread(target=self.commsthreadfunc, args=[client_socket])
                                    commsthread.start()
                                except Exception as e:
                                    print(e)


                        except Exception as e:
                            print(e)
                            client_socket.close()
                            connection.close()
                            self.connectedBoolean =False
                            break
                            
            except Exception as e:
                print(e)
                time.sleep(0.5)

    def commsthreadfunc(self, socket):
        self.commsthreadrunning = True
        try:
            rcvdData = socket.recv(1024).decode('utf-8')

            if not rcvdData:
                print("Motor connection closed")
                #return ''
            elif len(rcvdData)> 1:
                print(rcvdData[2:len(rcvdData)])
                self.processstringinput(rcvdData[2:len(rcvdData)])
        except Exception as e:
            print(e)
            rcvdData=0
        self.commsthreadrunning = False
               
    def processstringinput(self, settingstring):
        settingstring = settingstring.partition("\n")[0]
        if 'ma' in settingstring:
            self.currentlymovingflag = True
            movelocations = settingstring.split(',')
            movelocationsx = movelocations[0]
            movelocationsx = int(movelocationsx[4:len(movelocationsx)])
            movelocationsy = movelocations[1]
            movelocationsy = int(movelocationsy[4:len(movelocationsy)])        
            movelocationsz = movelocations[2]
            movelocationsz = int(movelocationsz[4:len(movelocationsz)])
            #print(movelocationsx, movelocationsy, movelocationsz)
            #print(m.xlocationtarget, movelocationsx, m.ylocationtarget, movelocationsy,  m.zlocationtarget, movelocationsz)
            
            if movelocationsx < 200000 and movelocationsx > -200000:
                #print('starting move')
                self.m.xlocationtarget = movelocationsx
                movemotorxproc = Process(target=self.m.movemotorx, args=[movelocationsx])
                movemotorxproc.start()
                self.m.xlocation = movelocationsx
                #print(movelocations, m.xlocationtarget, m.xlocation)
                #print('ending move')
            if movelocationsy < 200000 and movelocationsy > -200000:
                self.m.ylocationtarget = movelocationsy
                movemotoryproc = Process(target=self.m.movemotory, args=[movelocationsy])
                movemotoryproc.start()
                self.m.ylocation = movelocationsy
                #print('ymovedone')
            if movelocationsz < 200000 and movelocationsz > -200000:
                self.m.zlocationtarget = movelocationsz
                #print('1')
                movemotorzproc = Process(target=self.m.movemotorz, args=[movelocationsz])
                #print('2')
                movemotorzproc.start()
                self.m.zlocation = movelocationsz
                #print('zmovedone')
            self.m.savelocations()
            movemotorxproc.join()
            movemotoryproc.join()
            movemotorzproc.join()
            time.sleep(1.0)
            self.currentlymovingflag = False
            print('Currently moving stage:', self.currentlymovingflag)

        if 'capture' in settingstring:
            self.previewresolution = self.largeresolution
            self.captureImage = True
            print('capturing', self.captureImage)
        elif 'aex off' in settingstring:
            self.exposuremode = 'off'
        elif 'aex a' in settingstring:
            self.exposuremode = 'auto'
        elif 'awb off' in settingstring:
            self.whitebalance = 'off'
        elif 'awb a' in settingstring:
            self.whitebalance = 'auto'
        elif 'res 4056' in settingstring:
            self.largeresolution = (4056, 3040)
            self.previewresolution = (4056, 3040)
            self.captureImage = True
        elif 'res 2028' in settingstring:
            self.largeresolution = (2028, 1520)
            self.captureImage = True
        elif 'pre 2028' in settingstring:
            self.previewresolution = (2028, 1520)
            self.changeresolution = True
        elif 'pre 1014' in settingstring:
            self.previewresolution = (1014, 760)
            self.changeresolution = True
        elif 'pre 507' in settingstring:
            self.previewresolution = (405, 304)
            self.changeresolution = True
        elif 'setzero' in settingstring:
            self.m.setlocationzero()
        elif 'gotozero' in settingstring:
            self.m.gotozero()
        elif 'autofocus' in settingstring:
            self.currentlymovingflag = True
            if 'autofocus_coarse' in settingstring:
                focusdepths = [-150, -100,-50,-25,13,0,13,25, 50,100,150]
            else:
                focusdepths = [-40,-15,0,15,40]
            focusdepthsabsolute = []
            depthstreamsizes = []
            print('autofocusing')
            for depth in focusdepths:
                print('calculating depths')
                focusdepthsabsolute.append(self.m.zlocation + depth)
            print('focusdepths', focusdepthsabsolute)
            for depth in focusdepthsabsolute:
                while int(self.imagelen) < 1000:
                    time.sleep(1)
                
                self.m.movemotorz(depth)
                time.sleep(2)
                depthstreamsizes.append(self.imagelenfocus)
                print(self.imagelenfocus)
            maxfocusindex = depthstreamsizes.index(max(depthstreamsizes))
            self.m.movemotorz(focusdepthsabsolute[maxfocusindex])
            time.sleep(4)
            #captureImage=True
            self.currentlymovingflag = False
            print('Currently moving focus:', self.currentlymovingflag)
        


if __name__ == "__main__":
    os.chdir('/home/pi/Desktop/AndroidSmartMicroscopewithUSBStepperControl/Raspberry_pi')
    imageServer()