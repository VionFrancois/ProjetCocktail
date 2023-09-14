#include <AccelStepper.h>

// Pump 1
const int dirPin_pump1 = 8;
const int stepPin_pump1 = 7;

// Pump 2
const int dirPin_pump2 = 6;
const int stepPin_pump2 = 5;

// Pump 3
const int dirPin_pump3 = 4;
const int stepPin_pump3 = 3;


// Setting of the pumps
const int maxSpeed_wata = 1400;
const int acceleration_wata = 200;

const int maxSpeed_sirop = 1400;
const int acceleration_sirop = 200;

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

/**
 * Transform the quantity in step.
 * The ratio is calculated approximatly with our data.
 * The constant parameters are :
 * Speed = 2000
 * Acceleration = 550
 * 
 * @param quantity the amout of mililiters we want to get.
 * @return The number of steps needed to get the quantity wanted.
*/
long mLToStep(int quantity) {
  // TODO : change the function for the 2 motor speed. (check if the motor is slower it delivers lower quantity).
 
  double rate = 255.0;
  double step = rate * quantity;
  return step;
}


/**
 * Activates the backtracking for all the pumps in the array pumps.
 * When the backtracking starts, all the pumps are running backward to clean the pipes.
 * @param pumps AccelStepper array of all the running pumps.
*/
void backtrack(AccelStepper pumps[]){
  int i = 0;
  Serial.println("Start backtracking");
  for (i=0; i<ACTIVE_PUMPS; i++) {
    pumps[i].setCurrentPosition(0);
    pumps[i].moveTo(-5000);
  }
  
  bool running;
  do {
    running = false;
    for (i=0; i<ACTIVE_PUMPS; i++) {
      running = max(running, pumps[i].run());
    }
    if (Serial.available() > 0) {
      char data = Serial.read();
      switch (data) {
      case 'S':
        Serial.println("Stopping the pumps");
        return;
      default:
        break;
      }
    }
  }while(running);
  Serial.println("Finish backtracking");
}


/**
 * Run all the pumps in the array. All the pumps have to be set up before calling this function.
 * @param pumps AccelStepper array of all the running pumps.
*/
void runPumps(AccelStepper pumps[]) {
  bool stillRunning;
  do {
    stillRunning = false;
    for (int i=0; i<ACTIVE_PUMPS; i++){
      stillRunning = max(pumps[i].run(), stillRunning);
    }
    if (Serial.available() > 0) {
      char data = Serial.read();
      switch (data) {
      case 'S':
        Serial.println("Stopping the pumps");
        return;
      default:
        break;
      }
    }

  }while(stillRunning);
  Serial.println("Pumping finished");
  backtrack(pumps);
}


/**
 * Check if a string is numeric because it isn't implemented natively (cringe)
 * @param str The string to test
 * @return true if str is numeric, false otherwise.
*/
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
  long steps;
  String value;
  
  // request example : M11234\n

  // Maybe use regex, it'll be easier.
  if (request.length() < 3 || request.length() > 7) { 
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
        steps = mLToStep(qt);
        Serial.print("Pump 1 : ");
        Serial.print("Speed : ");
        Serial.print(qt);
        Serial.print("ml - ");
        Serial.print(steps);
        Serial.println("steps");

        pump1.moveTo(steps);
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
        steps = mLToStep(qt);
        Serial.print("Pump 2 : ");
        Serial.print(qt);
        Serial.print("ml - ");
        Serial.print(steps);
        Serial.println("steps");

        pump2.moveTo(steps);
        pump_list[ACTIVE_PUMPS] = pump2;
        ACTIVE_PUMPS++;

        Serial.print("Setup pump 2 with ");
        Serial.print(qt);
        Serial.println("ml");

        break;
      
      case '3':
         // pump3
        value = request.substring(2,request.length()-1);
        qt = value.toInt();
        steps = mLToStep(qt);
        Serial.print("Pump 3 : ");
        Serial.print(qt);
        Serial.print("ml - ");
        Serial.print(steps);
        Serial.println("steps");

        pump3.moveTo(steps);
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
  Serial.print("Starting...\n");

  // Pump 1
  pump1.setMaxSpeed(maxSpeed_wata);
  pump1.setAcceleration(acceleration_wata);
  pump1.setPinsInverted(true);

  // Pump 2
  pump2.setMaxSpeed(maxSpeed_sirop);
  pump2.setAcceleration(acceleration_sirop);
  pump2.setPinsInverted(true);

  // Pump 3
  pump3.setMaxSpeed(maxSpeed_sirop);
  pump3.setAcceleration(acceleration_sirop);
  pump3.setPinsInverted(false);

}


void loop() {
  String current_request;
  int feedback;
  AccelStepper pumps[MAX_PUMPS];

  Serial.println("Ready");
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
        
        case '#':
          Serial.println("Reseting pumps");
          resetPumps(pumps);
          break;
        case '=':
          Serial.println("Cleaning pipes");
          pumps[0] = pump1;
          pumps[1] = pump2;
          pumps[2] = pump3;
          ACTIVE_PUMPS = 3;
          backtrack(pumps);
          resetPumps(pumps);
          break;
        case 'S':
          Serial.println("Cannot stop pumps while they aren't running");
          break;
        default:
          current_request += data;
      }
    }
  }
}
