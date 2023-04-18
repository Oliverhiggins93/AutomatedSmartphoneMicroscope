[SD card]:Parts/SDCard.md ""

# Raspberry Pi Software

A software image for the Raspberry Pi will be available soon which can be burned directly.  

Currently the easiest way to set up your Raspberry Pi to stream to your smartphone is as follows:

* Flash your [SD card] with the latest version of Raspbian. 
* Open a terminal window
* Clone the repository by running command:
    * cd ~
    * git clone https://github.com/Oliverhiggins93/AutomatedSmartphoneMicroscope.git
* Add the Raspberry Pi server script to rc.local to schedule running at startup. Typing the following in to the terminal:
    * sudo nano /etc/rc.local
    * Add the following line before "exit 0":
        * sudo python3 /~/AutomatedSmartphoneMicroscope/Raspberry_pi/stills_server.py &

* Add your phone hotspot as a WiFi connection to join:
    * Open a terminal window and enter the following:
        * sudo nano /etc/wpa_supplicant/wpa_supplicant.conf
    * Copy-paste the following into the wpa_supplicant.conf file: 

<div markdown="1" class="info-block">
country=gb </br>
update_config=1 </br>
ctrl_interface=/var/run/wpa_supplicant </br>

network={ </br>
scan_ssid=1 </br>
ssid="microscope" <br>
psk="smart" <br> 
}
</div>


<div markdown="1" class="caution-block">
**Security warning**

Here we are suggesting to use a default SSID and password. This will mean that you will need to change the SSID of your phone so that the Raspberry Pi can pair to your phone. This is convenient because each smartphone user can change their hotspot ssid and password relatively quickly without having to interface with a screen. If you are worried about changing your smartphone's hotspot SSID and password to something that may be less secure, I would suggest you can change the ssid or psk field to the default for your smartphone. 
</div>

* Reboot. Upon reboot your Raspberry Pi will start looking to join the mobile hotspot of your smartphone, and as long as the camera has been correctly connected, the script will begin streaming images.

---

[Previous page](assembly.md) | [Next page](androidinstallation.md)