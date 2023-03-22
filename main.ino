#include <AccelStepper.h>

// Pump 1
const int dirPin_pump1 = 4;
const int stepPin_pump1 = 3;

// Pump 2
const int dirPin_pump2 = 6;
const int stepPin_pump2 = 5;

// Pump 3
const int dirPin_pump3 = 8;
const int stepPin_pump3 = 7;


// Setting of the pumps
const int maxSpeed = 2000;
const int acceleration = 300;

// Define motor interface type
#define motorInterfaceType 1

// Creates the pumps variables
AccelStepper pump1(motorInterfaceType, stepPin_pump1, dirPin_pump1);
AccelStepper pump2(motorInterfaceType, stepPin_pump2, dirPin_pump2);
AccelStepper pump3(motorInterfaceType, stepPin_pump3, dirPin_pump3);

const int MAX_PUMPS = 3;

int ACTIVE_PUMPS = 0;

// State of the machine
const int RUNNING = 0;
const int WAITING = 1;
int CURRENT_STATE = WAITING; 

String totalData = "";


int mLToStep(int quantity) {
  // Transform the quantity (in mL) in step.
  // Formula : rate * quantity + JUMP
  // We round the step because we need a int and we can't accept float quantities.

  // Constant used in the measurements to get the value to map the quantity in step.
  // speed = 2 000
  // acceleration = 550
  // setspeed = 9000 ?
 
  float rate = 255.0;
  float step = rate * quantity;
  return step;
}


void backtrack(AccelStepper pumps[]){
  int i = 0;
  Serial.println("Start backtracking");
  for (i=0; i<ACTIVE_PUMPS; i++) {
    pumps[i].setCurrentPosition(0);
    pumps[i].moveTo(-5000);
  }
  
  bool running;
  do {
    bool tmp = false;
    for (i=0; i<ACTIVE_PUMPS; i++) {
      tmp = max(tmp, pumps[i].run());
    }
    running = tmp;
  }while(running);
  Serial.println("Finish backtracking");
}


void runPumps(AccelStepper pumps[]) {
  bool stillRunning;
  do {
    stillRunning = false;
    for (int i=0; i<ACTIVE_PUMPS; i++){
      stillRunning = max(pumps[i].run(), stillRunning);
    }
  }while(stillRunning);
  Serial.println("Pumping finished");
  backtrack(pumps);
}


bool isNumeric(String str){
  for (int i=0; i<str.length(); i++){
    if (!isDigit(str.charAt(i))){
      return(false);
    }
  }
  return(true);
}


int setupPump(String request, AccelStepper pump_list[]){  
  int qt;
  String value;
  
  // request example : M11234\n

  // Maybe use regex, it'll easier.
  if (request.length() < 3 || request.length() > 6) {
    Serial.println("Message too short or too long");
    return(1);
  }
  if (request.charAt(0) != 'M') {
      Serial.println("Message needs to start with M");
      return(1);
  }
  if (isDigit(request.charAt(1))) {
    switch(request.charAt(1)){
      case '1':
        // pump1
        value = request.substring(2);
        // if (!isNumeric(value)){
          // Serial.print("end 4 chars need to be integers, they are : ");
          // Serial.println(value);
          // return(1);
        // }
        qt = value.toInt();
        // RUN HERE
        pump1.moveTo(mLToStep(qt));
        pump_list[ACTIVE_PUMPS] = pump1;
        ACTIVE_PUMPS++;

        Serial.print("Setup pump 1 with ");
        Serial.print(qt);
        Serial.println("ml");

        break;
      case '2':
        // pump2
        value = request.substring(2);
        //if (!isNumeric(value)){
          // Serial.print("end 4 chars need to be integers, they are : ");
          // Serial.println(value);
          // return(1);
        // }
        qt = value.toInt();
        // RUN HERE
        pump2.moveTo(mLToStep(qt));
        pump_list[ACTIVE_PUMPS] = pump2;
        ACTIVE_PUMPS++;

        Serial.print("Setup pump 2 with ");
        Serial.print(qt);
        Serial.println("ml");

        break;
      
      case '3':
         // pump3
        value = request.substring(2,request.length()-1);
        if (!isNumeric(value)){
          Serial.print("end 4 chars need to be integers, they are : ");
          Serial.println(value);
          return(1);
        }
        qt = value.toInt();
        // RUN HERE
        pump3.moveTo(mLToStep(qt));
        pump_list[ACTIVE_PUMPS] = pump3;
        ACTIVE_PUMPS++;

        Serial.print("Setup pump 3 with ");
        Serial.print(qt);
        Serial.println("ml");

        break;
      
      default:
        Serial.println("2nd char need to be 1-2-3");
        return(1);
    }
  }
  return(0);
}

void resetPumps(AccelStepper pumps[]){
  for (int i=0; i < ACTIVE_PUMPS; i++){
    pumps[i].moveTo(0);
    pumps[i].setCurrentPosition(0);
  }
  ACTIVE_PUMPS = 0;
}


void errorInMessage(String message, AccelStepper pumps[]){
  resetPumps(pumps);
  Serial.print("Error : ");
  Serial.println(message);
}


void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  Serial.print("Start\n");

  // Pump 1
  pump1.setMaxSpeed(maxSpeed);
  pump1.setAcceleration(acceleration);

  // Pump 2
  pump2.setMaxSpeed(maxSpeed);
  pump2.setAcceleration(acceleration);

  // Pump 3
  pump3.setMaxSpeed(250);
  pump3.setAcceleration(100);

}

void loop() {
  String current_request;
  int feedback;
  Serial.println("Code setup");
  AccelStepper pumps[MAX_PUMPS];

  while(1) {
    if (Serial.available() > 0){
      char data = Serial.read();
      switch(data) {
        case '\n':
          if (current_request.charAt(0) == 'M'){
            Serial.println("Pump Activation");
            feedback = setupPump(current_request, pumps);
            if (feedback == 1){
              errorInMessage(current_request,pumps);
            } 
            current_request = "";
          } else {
            current_request = "";
          }  
          break;

        case '$':
          if (ACTIVE_PUMPS == 0) {
            Serial.println("You need to activate pumps before running them...");
          } else {
            // Run motors here 
            Serial.println("Running pumps");
            runPumps(pumps);
            resetPumps(pumps);
          }
          current_request = "";
          break;
        default:
          current_request += data;
      }
    }
  }
}
