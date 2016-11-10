import interfascia.*;
/* 11-10-16
  Kristen Andrews
  
  The Signal In The Noise Mod13
  There will be a randlomy changing/morphing background. When an 
  input is recieved, tere will be a disruption in the field 
 */
GUIController controller;
Input testInput;

float threshold = 5.0;
int thresMin  = 0;
int thresMax  = 100;
int thres;
int delay;


void setup(){
  controller   = new GUIController(this);
  testInput = new Input();
  size(640, 360);
  background(0);
  strokeWeight(5);
  frameRate(9);
  //delay  = millis();
  
}
void draw(){
  
  for (int i = 0; i < width; i++) {
    float r = random(255) ;
    stroke(r);
    line(i, 0, i, height);
  }
 
  for (int i = height; i > 0; i--) {
   float r = random(255);
    stroke(r);
    line(i, 0, width, i);
  }
 for (int i = height; i > 0; i--){
    float r = random(255);
    stroke(r);
    line(height, 0, i, height);
 }
 for (int i = height; i > 0; i--){
     float r = random(255);
    stroke(r);
    line(height, width, 0, i);
 }

  println("Input:" + testInput.getAmplitude());

  if(testInput.getAmplitude()>threshold){
    stroke(115, 110, 127);
    fill(115, 110, 127);
    rect(50, 50, 100, 100);
  }
}