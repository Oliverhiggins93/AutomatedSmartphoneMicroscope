 //This example code is in the Public Domain (or CC0 licensed, at your option.)
//By Evandro Copercini - 2018
//
//This example creates a bridge between Serial and Classical Bluetooth (SPP)
//and also demonstrate that SerialBT have the same functionalities of a normal Serial

#include "BluetoothSerial.h"
#include "AccelStepper.h"

#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif

BluetoothSerial SerialBT;


String readString;
String stepType;
String stepMagnitude;
int len;
int integerStep;
//Stepper motorx = Stepper(2048, 15, 2, 4, 16); //pins here refer to eps32 numbering on board surface. If not working try swapping pins connected to in2 and in3.
//Stepper motory = Stepper(2048, 13, 12, 14, 27);
//Stepper motorz = Stepper(2048, 26, 25, 33, 32);

AccelStepper motorx(AccelStepper::FULL4WIRE, 15, 4,2, 16); //hq currently has 2 and 4 wrong way around 
AccelStepper motory(AccelStepper::FULL4WIRE, 13, 12,14, 27); 
AccelStepper motorz(AccelStepper::FULL4WIRE, 26, 25, 33, 32);

int led = 18;
int brightness = 250;

void setup() {
  Serial.begin(115200);
  SerialBT.begin("SmartMicroscope2"); //Bluetooth device name
  Serial.println("The device started, now you can pair it with bluetooth!");

  motorx.setMaxSpeed(325);
  motory.setMaxSpeed(325);
  motorz.setMaxSpeed(325);
  motorx.setSpeed(300);
  motory.setSpeed(300);
  motorz.setSpeed(300);
  
  motorx.setAcceleration(300);
  motory.setAcceleration(300);
  motorz.setAcceleration(300);
  
  ledcAttachPin(led, 0);
  ledcSetup(0, 5000, 8);
  ledcWrite(0, 0);
  
}

String getStringPartByNr(String data, char separator, int index) {
    int stringData = 0;        //variable to count data part nr 
    String dataPart = "";      //variable to hole the return text

    for(int i = 0; i<data.length()-1; i++) {    //Walk through the text one letter at a time
        if(data[i]==separator) {
            //Count the number of times separator character appears in the text
            stringData++;
        } else if(stringData==index) {
            //get the text when separator is the rignt one
            dataPart.concat(data[i]);
        } else if(stringData>index) {
            //return text and stop if the next separator appears - to save CPU-time
            return dataPart;
            break;
        }
    }
    //return text if this is the last part
    return dataPart;
}

void loop() {
  
  motorx.run();
  motory.run();
  motorz.run();
  
  //if (Serial.available()) {
    //SerialBT.write(Serial.read());
  //}
  if (SerialBT.available()) {
    readString = SerialBT.readString();
    //Serial.println("Received:" + readString);
    //ledcWrite(0, brightness);
    
    if (readString.length() > 3){
      if (readString.substring(0,2) == "mr") {
        len = readString.length();
        integerStep = readString.substring(4,len).toInt();
        
        if (readString.substring(0,3) == "mrx"){
          motorx.moveTo(integerStep);
        }
        else if (readString.substring(0,3) == "mry"){
          motory.moveTo(integerStep);
        }
        else if (readString.substring(0,3) == "mrz"){
          motorz.moveTo(integerStep);
        }
      }


      else if (readString.substring(0,2) == "ma") {

        String xstring = getStringPartByNr(readString, ',', 0);
        int xlen = xstring.length();
        int xStep = xstring.substring(4,xlen).toInt();
        
        String ystring = getStringPartByNr(readString, ',', 1);
        int ylen = ystring.length();
        int yStep = ystring.substring(4,ylen).toInt();
        
        String zstring = getStringPartByNr(readString, ',', 2);
        int zlen = zstring.length();
        int zStep = zstring.substring(4,zlen).toInt();
        
        Serial.println(xStep);
        Serial.println(yStep);
        Serial.println(zStep);
        motorx.moveTo(xStep);
        motory.moveTo(yStep);
        motorz.moveTo(zStep);
      }
      
      
      else if (readString.substring(0,3) == "led") {
          len = readString.length();
          brightness = readString.substring(4,len).toInt();
          if (brightness <= 0 || brightness >= 255) {
              brightness = 255;
              }
          Serial.println(readString);
          }

        else if (readString.substring(0,2) == "po") {
        len = readString.length();
        int pos = readString.substring(4,len).toInt();
        if (readString.substring(0,3) == "pox"){
          motorx.setCurrentPosition(pos);
        }
        if (readString.substring(0,3) == "poy"){
          motory.setCurrentPosition(pos);
        }
        if (readString.substring(0,3) == "poz"){
          motorz.setCurrentPosition(pos);
        }
        Serial.println(readString);
        }
        
        else if (readString.substring(0,3) == "zer") {
          motorx.setCurrentPosition(0);
          motory.setCurrentPosition(0);
          motorz.setCurrentPosition(0);
          Serial.println(readString);
        }
        else if (readString.substring(0,3) == "ori") {
          motorx.moveTo(0);
          motory.moveTo(0);
          motorz.moveTo(0);
          Serial.println(readString);
        }
  }
  }
}
