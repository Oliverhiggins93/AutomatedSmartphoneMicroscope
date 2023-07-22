'''
A class to interface with the microscope camera


'''
import numpy as np
import pygame.camera
import pygame


class OcularGrabber():
    def __init__(self,resolution = (1600,1200)):
        pygame.camera.init()
        self.resolution = resolution

        self.clist = pygame.camera.list_cameras()
        if not self.clist:
            raise ValueError("Sorry, no cameras detected.")
        self.cam = pygame.camera.Camera('/dev/video0', self.resolution)
        self.cam.start()


    def get_frame(self):
        img =  pygame.surfarray.array3d(self.cam.get_image())
        return img

    def close(self):
        self.cam.stop()

    def get_size(self):
        return self.cam.get_size()
