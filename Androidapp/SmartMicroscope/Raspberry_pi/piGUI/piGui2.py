# -*- coding: utf-8 -*-
"""
Created on Tue Dec  6 12:13:26 2022

@author: CDT
"""

import sys
from PyQt5.QtWidgets import QMainWindow, QApplication, QPushButton, QWidget, QAction, QTabWidget,QVBoxLayout, QGroupBox, QGridLayout, QLabel
from PyQt5.QtGui import QIcon, QIntValidator
from PyQt5.QtCore import pyqtSlot
from PyQt5 import QtCore
from piGui2_layout import piGui_layout



class App(QMainWindow):

    def __init__(self, rect):
        super().__init__()
        self.title = 'Microscope GUI'
        self.setWindowTitle(self.title)
        #self.setGeometry(self.left, self.top, self.width, self.height)
        #rect = QApplication(sys.argv).primaryScreen().availableGeometry()
        self.table_widget = piGui_layout(self, rect)
        self.setCentralWidget(self.table_widget)
        self.show()
        self.showMaximized()
        
        
if __name__ == '__main__': 
    app = QApplication(sys.argv)
    rect = app.primaryScreen().availableGeometry()
    app.aboutToQuit.connect(app.deleteLater)
    ex = App(rect)
    sys.exit(app.exec_())
