import processing.core.PApplet;

public class Driver extends PApplet {
	Input	input;
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
		size(400,600);
	} // settings
	
	public void setup()
	{
		input	= new Input(new String[] {"Horse and Rider 1.wav", "Horse and Rider 2.wav", "Horse and Rider 3.wav"});
	} // setup
	
	public void draw()
	{
		println("Hello????");
		background(204, 150, 204);
		
		stroke(255);
		  for (int i = 0; i < freqEMM.hps.length; i++)
		  {
		    rect( i*10, height, 1, - freqEMM.hps[i] );
		  } // for

		  stroke(150, 50, 150);
		  rect( freqEMM.maxbin*10, height, 1, - freqEMM.hps[maxBin] );

		  stroke(50, 50, 255);
		  rect(secondMaxBin * 10, height, 1, -freqEMM.hps[secondMaxBin]);
		
//		ellipse(width / 2, height - input.getAdjustedFund(), 50, 50);
	} // draw
}