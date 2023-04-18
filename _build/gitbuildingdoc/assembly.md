[Raspberry Pi]:Parts/RaspberyPi.md ""
[Raspberry Pi Camera]:Parts/RaspberyPiCamera.md ""
[M3 bolt]:Parts/M3x8PanSteel.md ""
[28BYJ-48]:Parts/28BYJ-48.md ""
[ULN2003]:Parts/ULN2003.md ""
[Jumper wires]:Parts/Jumperwires.md ""
[3mm bolt coupler]:Parts/Shaftcouple3mm.md ""
[6mm bolt coupler]:Parts/Shaftcouple6mm.md ""
[M3x30mm bolt]:Parts/M3x30GHexSteel.md ""
[M3x8mm bolt]:Parts/M3x8PanSteel.md ""
[M6x130mm bolt]:Parts/M6x130PanSteel.md ""
[M3 nut]:Parts/M3nut.md ""
[M6 nut]:Parts/M6nut.md ""
[M2.5x6mm bolt]:Parts/M2.5x6.md ""
[M4x10mm bolt]:Parts/M4x10PanSteel.md ""
[Condenser lens]:Parts/Plasticcondenser.md ""
[White LED]:Parts/1WLED.md ""
[Resistor]:Parts/resistor.md ""
[RMS objective]:Parts/rmsobjective.md ""
[Tube lens]:Parts/tubelens.md ""
[Camera ribbon]:Parts/RaspberyPiCameraRibbon.md ""

# Assembling the microscope 

## Step 1: Attaching Raspberry pi and motor drivers to base {:id="attaching-raspberry-pi-and-motor-drivers-to-base" class="page-step"}

We will start by assembling the base of the microscope and attaching the electronic components. You will want to thread the bolts through the bottom of the base so that the threaded ends finish inside the microscopy body. 

Take the [Raspberry Pi] and thread an [M3 bolt] through the hole in each corners. Secure the [Raspberry Pi] with the bolts and 4x [M3 nut].

Take the 3x [ULN2003] motor drivers and attach these to the base in the same way with the [M3 bolt]s. Secure with [M3 nut].

Using 2x [M3 bolt] attach the [28BYJ-48] by screwing bolts through the arms into the plastic holders. Secure with [M3 nut].

<center>![Base instructions](images/building/base.jpg "")</center>

## Step 2: Motor and motor driver wiring {:id="motor-and-motor-driver-wiring" class="page-step"}

Attach each [28BYJ-48] header clip to the corresponding [ULN2003] motor driver.

We have had good success powering all of the motors using a simple parallel circuit from the 5V Raspberry Pi pin. Some resources online caution against this, due to concerns that the motors may drain more current than the 5V pin is rated to supply, but personally I have never found any difficulties with this, and it removes the requirement for an additional power source. 

Using [Jumper wires] attach each pin of the [ULN2003] to the [Raspberry Pi]. The pin order should be as follows:

* X axis:
    * **Motor pin 1**: Raspberry Pi pin 18
    * **Motor pin 2**: Raspberry Pi pin 23
    * **Motor pin 3**: Raspberry Pi pin 24
    * **Motor pin 4**: Raspberry Pi pin 25

* Y axis:
    * **Motor pin 1**: Raspberry Pi pin 8
    * **Motor pin 2**: Raspberry Pi pin 7
    * **Motor pin 3**: Raspberry Pi pin 12
    * **Motor pin 4**: Raspberry Pi pin 16

* Z axis:
    * **Motor pin 1**: Raspberry Pi pin 4
    * **Motor pin 2**: Raspberry Pi pin 17
    * **Motor pin 3**: Raspberry Pi pin 27
    * **Motor pin 4**: Raspberry Pi pin 22

Please note, if your motors do not move when you test them, you should try to swap motor pin 2 and 3 for each driver. 


## Step 3: Attaching screw couplings to motors {:id="attaching-screw-couplings-to-motors" class="page-step"}

There are three screw couples; 2x [6mm bolt coupler] and 1x [3mm bolt coupler] which are used to attach the [M3x30mm bolt] and M6 bolts to the [28BYJ-48] motors.

To assemble the screw couplings you should first screw a spare nut all the way along the length of the screw, so that it is flush (or close to flush) with the hex head. You can then insert the hex head into the hexagonal hole in the screw coupler. The nut will help the screw to stay in place in the coupler. 

You can then push the shaft of the [28BYJ-48] into the rectangular hole of the screw coupler. 

Once these steps have been completed you should insert 2x [M3 bolt] into the holes in the side to secure the bolt and shaft from moving. Secure with [M3 nut].

<center>![Screw coupler](images/building/screwcouple.png "")</center>

## Step 4: Assembling Z stage {:id="assembling-z-stage" class="page-step"}

Insert an [M3 nut] into the flexible camera holder. 

<center>![Camera holder](images/building/flex_nut.png "")</center>

## Step 5: Attaching camera to Z stage {:id="attaching-camera-to-z-stage" class="page-step"}

Using 4x [M2.5x6mm bolt] Screw the camera onto the flexible holder.

<center>![Camera holder](images/building/flex_camera.png "")</center>

You will now need to attach the [Camera ribbon]. One end of the camera ribbon will attach to the Raspberry Pi Zero, and the other end will attach to the camera.

## Step 6: Attaching frame and Z stage to base {:id="attaching-frame-and-z-stage-to-base" class="page-step"}

Use 4x [M4x10mm bolt] to attach the frame to the base. The bolts should screw through the four central holes in the frame, through the camera holder, and into the base. 

<center>![Camera holder](images/building/frametobase.png "")</center>

## Step 7: Attaching objective tube to camera holder {:id="attaching-objective-tube-to-camera-holder" class="page-step"}

Use 4x [M3x8mm bolt] to attach the objective tube to the camera holder. The bolts screw through the four holes in the bottom of the objective tube arms and into the holes in the camera holder.

<center>![Camera holder](images/building/flexibleobjective.png "")</center>

## Step 8: Attaching XY stage bottom to objective holder {:id="attaching-xy-stage-bottom-to-objective-holder" class="page-step"}

Use 4x [M4x10mm bolt] to attach the X stage bottom to the objective holder.

Slide the X carriage top into the X stage bottom. This should slide directly in, but you may need to slightly bend the part to get the nut holder over the frame.

Insert an [M6 nut] into the X carriage. 

<center>![Camera holder](images/building/xcarriage.png "")</center>
 
## Step 9: Assembling Y stage {:id="assembling-y-stage" class="page-step"}

Use 4x [M3x8mm bolt] to attach the Y stage bottom to the X stage top.

Insert a 2x [M6x130mm bolt] into the X and Y stages through the holes in the side of the stage carriages. 

<center>![Camera holder](images/building/ycarriagem6.png "")</center>

## Step 10: Attaching motors Y stage {:id="attaching-motors-y-stage" class="page-step"}

Use 4x [M3x8mm bolt] to attach the [28BYJ-48] motors to the stage.

Slide the Y stage top into the stage. You may need to bend the stage slightly to get the part into the frame. 

Insert a [M6 nut] into the Y carriage.

<center>![Camera holder](images/building/ycarriagemotors.png "")</center>

## Step 11: Insert objective {:id="insert-objective" class="page-step"}

If you are using the [Tube lens], you should insert this into the Tube lens holder. You can then use 1x [M3x8mm bolt] inserted into the side of the objective holder to hold the tube lens in place. 

Insert the [RMS objective] into the objective holder. It should fit easily in place, but can be held in place using an [M3x8mm bolt].

<center>![Camera holder](images/building/objective.png "")</center>

## Step 12: Prepare illumination arm {:id="prepare-illumination-arm" class="page-step"}

Glue the [White LED] onto the illumination frame in the central recess.

Insert the [Condenser lens] into the condenser holder.

You should prepare the circuit by soldering a 100 ohm [Resistor] to 2x [Jumper wires]. The jumper wire attached to the positive terminal of the LED should be attached to the 5V pin of the Raspberry Pi. The jumper wire attached to the negative terminal of the LED should be attached to one of the ground pins of the Raspberry Pi. 

Using 2x [M3x8mm bolt], screw the condenser holder onto the illumination frame.

<center>![Camera holder](images/building/ledcondenser.png "")</center>

## Step 13: Attach illumination arm {:id="attach-illumination-arm" class="page-step"}

Use 4x [M4x10mm bolt] to attach the illumination frame to the microscope stage.

<center>![Camera holder](images/building/ledframe.png "")</center>

## Step 14: Congratulations! {:id="congratulations" class="page-step"}

Well done, you have successfully built a microscope! Continue to the next steps for information about using the software.

---

[Previous page](printing.md) | [Next page](raspberrypi.md)