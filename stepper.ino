// Example sketch to control a stepper motor with A4988 stepper motor driver 
// and Arduino without a library. 
// More info: https://www.makerguides.com

// Define stepper motor connections and steps per revolution:
#define dirPin 6
#define stepPin 5
#define stepsPerRevolution 2000
#define delayy 650
#define dirPin1  8
#define stepPin1  7

void setup() {
  // Declare pins as output:
  pinMode(stepPin, OUTPUT);
  pinMode(dirPin, OUTPUT);

  pinMode(dirPin1, OUTPUT);
  pinMode(stepPin1, OUTPUT);
}

void loop() {
  // Set the spinning direction clockwise:
  digitalWrite(dirPin, HIGH);

  digitalWrite(dirPin1, HIGH);

  // Spin the stepper motor 1 revolution slowly:
  for (int i = 0; i < stepsPerRevolution; i++) {
    // These four lines result in 1 step:
    digitalWrite(stepPin, HIGH);
    digitalWrite(stepPin1, HIGH);
    delayMicroseconds(delayy);
    digitalWrite(stepPin, LOW);
    digitalWrite(stepPin1, LOW);
    delayMicroseconds(delayy);
  }

  digitalWrite(dirPin, LOW);
  digitalWrite(dirPin1, LOW);

  delay(1000);

  for (int i = 0; i < stepsPerRevolution; i++) {
    // These four lines result in 1 step:
    digitalWrite(stepPin, HIGH);
    digitalWrite(stepPin1, HIGH);
    delayMicroseconds(delayy);
    digitalWrite(stepPin, LOW);
    digitalWrite(stepPin1, LOW);
    delayMicroseconds(delayy);
  }

  delay(1000);



}