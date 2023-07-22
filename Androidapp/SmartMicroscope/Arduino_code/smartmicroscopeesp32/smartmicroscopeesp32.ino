//This example code is in the Public Domain (or CC0 licensed, at your option.)
//By Evandro Copercini - 2018
//
//This example creates a bridge between Serial and Classical Bluetooth (SPP)
//and also demonstrate that SerialBT have the same functionalities of a normal Serial

#include "BluetoothSerial.h"
#include "Stepper.h"

#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif

BluetoothSerial SerialBT;


String readString;
String stepType;
String stepMagnitude;
Stepper motorx = Stepper(2048, 15, 2, 4, 16); //pins here refer to eps32 numbering on board surface. If not working try swapping pins connected to in2 and in3.
Stepper motory = Stepper(2048, 13, 12, 14, 27);
Stepper motorz = Stepper(2048, 26, 25, 33, 32);
int led = 18;
int brightness = 250;

void setup() {
  Serial.begin(115200);
  SerialBT.begin("SmartMicroscope"); //Bluetooth device name
  Serial.println("The device started, now you can pair it with bluetooth!");
  motorx.setSpeed(14);
  motory.setSpeed(14);
  motorz.setSpeed(14);
  ledcAttachPin(led, 0);
  ledcSetup(0, 5000, 8);
  ledcWrite(0, 0);
  
}

void loop() {
  
  
  if (Serial.available()) {
    SerialBT.write(Serial.read());
  }
  
  if (SerialBT.available()) {
    readString = SerialBT.readString();
    Serial.print("Received:");
    Serial.println(readString);
    ledcWrite(0, brightness);

    if (readString.length() > 3){
      if (readString.substring(0,3) == "mrx") {
        int len = readString.length();
        int integerStep = readString.substring(4,len).toInt();
         
        Serial.println(readString);
        Serial.println(integerStep);
        motorx.step(integerStep);
        Serial.println("Steps complete");
        }

      else if (readString.substring(0,3) == "mry") {
        int len = readString.length();
        int integerStep = readString.substring(4,len).toInt();
         
        Serial.println(readString);
        Serial.println(integerStep);
        motory.step(integerStep);
        Serial.println("Steps complete");
        }

      else if (readString.substring(0,3) == "mrz") {
        int len = readString.length();
        int integerStep = readString.substring(4,len).toInt();
         
        Serial.println(readString);
        Serial.println(integerStep);
        motorz.step(integerStep);
        Serial.println("Steps complete");
        }

      else if (readString.substring(0,3) == "led") {
        int len = readString.length();
        brightness = readString.substring(4,len).toInt();
          if (brightness <= 0 || brightness >= 255) {
            brightness = 255;
            }
        Serial.println(readString);
        }

    
  }
  }
        delay(1);
}
