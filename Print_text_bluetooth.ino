#include <AccelStepper.h>

String totalData;

// Creates an instance

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
}

void loop() {
  // put your main code here, to run repeatedly:
  if (Serial.available() > 0) {
    char data = Serial.read();
    totalData += data;
    switch (data) {
      case '\n':
        Serial.print("Data collected : ");
        Serial.print(totalData);
        totalData = "";
      default:
        break;
    }
  }
  delay(50);
}