import processing.core.PApplet;

public class Driver extends PApplet {
	Input	input;
	Input.FrequencyEMM[] freqEMMarray;
	/**
	 * 11/12/2016
	 * Problem:
	 * 	- process() doesn't ever seem to be called.
	 * 
	 * HERE's WHY: this isn't in the build path of a current project!!! Why ever not????!!!
	 */
	
	public static void main(String[] args)
	{
		PApplet.main("Driver");
	} // main
	
	public void settings()
	{
		size(600,600);
	} // settings
	
	public void setup()
	{
		input	= new Input(new String[] {"Horse and Rider 1.wav", "Horse and Rider 2.wav", "Horse and Rider 3.wav"});
		freqEMMarray = input.getFrequencyArray();
	} // setup
	
	public void draw()
	{
		println("Hello????");
		background(204, 150, 204);
		
		
		stroke(255);
		  for (int i = 0; i < freqEMMarray[0].hps.length; i++)
		  {
		    rect( i*10, height, 1, - freqEMMarray[0].hps[i] );
		  } // for

		  stroke(150, 50, 150);
		  rect( freqEMMarray[0].hps[freqEMMarray[0].maxBin]*10, height, 1, - freqEMMarray[0].hps[freqEMMarray[0].maxBin] );

		  stroke(50, 50, 255);
		  rect(freqEMMarray[0].secondMaxBin * 10, height, 1, -freqEMMarray[0].hps[freqEMMarray[0].secondMaxBin]);
		
//		ellipse(width / 2, height - input.getAdjustedFund(), 50, 50);
	} // draw
}
