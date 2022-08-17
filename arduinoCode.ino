// Include the AccelStepper Library
// vvv Documentation about accelStepper vvv
// http://www.airspayce.com/mikem/arduino/AccelStepper/classAccelStepper.html#abee8d466229b87accba33d6ec929c18f
#include <AccelStepper.h>

// Define pin connections
const int dirPin = 4;
const int stepPin = 3;

const int dirPin1 = 6;
const int stepPin1 = 5;

// Define motor interface type
#define motorInterfaceType 1

// Creates an instance
AccelStepper myStepper(motorInterfaceType, stepPin, dirPin);

AccelStepper myStepBro(motorInterfaceType, stepPin1, dirPin1);

void setup() {
  // set the maximum speed, acceleration factor,
  // initial speed and the target position
  myStepper.setMaxSpeed(2000); // Unit : steps per second
  myStepper.setAcceleration(120);
  myStepper.setSpeed(9000);
  myStepper.moveTo(52000); // 52000 -> 200ml

  myStepBro.setMaxSpeed(2000); // Unit : steps per second
  myStepBro.setAcceleration(120);
  myStepBro.setSpeed(9000);
  myStepBro.moveTo(52000); // 52000 -> 200ml
}

void loop() {
  // Change direction once the motor reaches target position
  /*
  if (myStepper.distanceToGo() == 0) 
    myStepper.moveTo(-myStepper.currentPosition());
 */   
  // Move the motor one step
  myStepper.run();
  myStepBro.run();
  
}
