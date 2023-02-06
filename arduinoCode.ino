#include <AccelStepper.h>

// Define pin connections
const int dirPin = 4;
const int stepPin = 3;

// Define motor interface type
#define motorInterfaceType 1

// Creates an instance
AccelStepper myStepper(motorInterfaceType, stepPin, dirPin);

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  //pinMode(8,OUTPUT);

  myStepper.setMaxSpeed(1500); // Unit : steps per second
  myStepper.setAcceleration(550);
  myStepper.setSpeed(9000);
  myStepper.moveTo(40000); // 52000 -> 200ml
  //myStepper.moveTo(52000); // 52000 -> 200ml

  //set_stepper(myStepper);

}

void reset_stepper(AccelStepper step) {
  //step.setMaxSpeed(2000); // Unit : steps per second
  //step.setAcceleration(120);
  //step.setSpeed(9000);

  step.setCurrentPosition(0);
  step.moveTo(10000);
}

void loop() {
  // put your main code here, to run repeatedly:
  if(Serial.available()>0) {
    char data = Serial.read();
    switch(data) {
      case '1': 
        Serial.println("oui");
        bool running;
        do {         
           running = myStepper.run();
        } while(running);
        data = 0;
        myStepper.setCurrentPosition(0);
        myStepper.moveTo(40000); // 52000 -> 200ml

        //set_stepper(myStepper);
        //reset_stepper(myStepper);
        break;
      default : 
        break;
    }
    //Serial.println(data);
  }
  delay(50);
}