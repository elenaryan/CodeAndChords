import net.beadsproject.beads.analysis.featureextractors.PowerSpectrum;
import processing.core.PApplet;

public class Driver extends PApplet {
	Input	input;
	Input.FrequencyEMM[] freqEMMarray;
	RainingNumbers	inputFreqRain;
	
	public static void main(String[] args)
	{
		PApplet.main("Driver");
	} // main

	public void settings()
	{
		size(1000,600);
	} // settings

	public void setup()
	{
		input	= new Input();
		//		input	= new Input(new String[] {"Horse and Rider 1.wav", "Horse and Rider 2.wav", "Horse and Rider 3.wav"});
		freqEMMarray = input.getFrequencyArray();
		inputFreqRain	= new RainingNumbers(this, width - 200, "input.getAdjustedFund()", 15);
		inputFreqRain.setBackgroundColor(250, 150, 204);
		inputFreqRain.setTextColor(0,  0,  0);
		inputFreqRain.setTitleSize(18);

		background(250, 150, 204);
	} // setup

	public void draw()
	{
//		background(250, 150, 204);
		stroke(250, 150, 204);
		fill(250, 150, 204);
		rect(0, 0, width - 200, height);
		
		drawPSSecondHighest(input);
		drawHPSSecondHighest(input);
		
		inputFreqRain.rain(input.getAdjustedFund());
		/*
 // This is now happening in drawHPSSecondHighest()
		stroke(255);
		  for (int i = 0; i < freqEMMarray[0].hps.length; i++)
		  {
		    rect( i*10, height, 1, - freqEMMarray[0].hps[i] );
		  } // for

		  stroke(150, 50, 150);
		  rect( freqEMMarray[0].hps[freqEMMarray[0].maxBin]*10, height, 1, - freqEMMarray[0].hps[freqEMMarray[0].maxBin] );

		  stroke(50, 50, 255);
		  rect(freqEMMarray[0].secondMaxBin * 10, height, 1, -freqEMMarray[0].hps[freqEMMarray[0].secondMaxBin]);
		 */	
		//		ellipse(width / 2, height - input.getAdjustedFund(), 50, 50);

	} // draw

	/**
	 * 11/19: currently, secondMaxBin is always after maxBin.
	 * This is problematic: I want secondMaxBin to be the one that maxBin is fluctuating with,
	 * but right now maxBin just goes back and forth between them.
	 * 
	 * Or: what if we always picked what is now getting "maxBin"?  Would that be helpful?
	 * @param input
	 */
	void drawHPSSecondHighest(Input input)
	{
//		println("drawHPSSecondHighest()");
		Input.FrequencyEMM freqEMM  = input.frequencyArray[0];
		int maxBin            = 0;
		int secondMaxBin      = 0;

		int	spacer;
		try
		{
			spacer	= width / freqEMM.hps.length;

			// draw rectangles for every frequency:
			stroke(255);
			for (int i = 0; i < freqEMM.hps.length; i++)
			{
				rect( i*spacer, height, 1, - freqEMM.hps[i] );
				/*
 // I don't want to find my max and secondMax here, b/c the point is to test them from Input.
	    if (freqEMM.hps[i] > freqEMM.hps[maxBin])
	    {
	      maxBin  = i;
	    } else if (freqEMM.hps[i] > freqEMM.hps[secondMaxBin] && freqEMM.hps[i] < freqEMM.hps[maxBin])
	    {
	      secondMaxBin  = i;
	    } // if
				 */
			} // for

			//	  maxBin		= freqEMM.maxBin;
			//	  secondMaxBin	= freqEMM.secondMaxBin;

			stroke(150, 50, 150);
			rect( freqEMM.maxBin*spacer, height, 1, - freqEMM.hps[freqEMM.maxBin] );


			stroke(50, 50, 255);
			rect(freqEMM.secondMaxBin * 10, height, 1, -freqEMM.hps[freqEMM.secondMaxBin]);
		} catch(NullPointerException npe)
		{
			println("InputClassEclipse_HPSCorrection.drawHPSSecondHighest: " + npe.getMessage());
		}
	} // drawHPSSecondHighest
	
	void drawPSSecondHighest(Input input)
	{
//		println("drawPSSecondHighest()");
		Input.FrequencyEMM freqEMM  = input.frequencyArray[0];
		PowerSpectrum	powerSpectrum = input.psArray[0];
		float[]	features	= powerSpectrum.getFeatures();
		int maxBin            = 0;
		int secondMaxBin      = 0;

		int	spacer;
		try
		{
			spacer	= width / features.length;

			// draw rectangles for every frequency:
			stroke(255);
			for (int i = 0; i < features.length; i++)
			{
				rect( i*spacer, height/2, 1, - features[i] );
				
 // Finding my max here b/c I don't ever do it in Input (might have a time delay accuracy error):
	    if (features[i] > features[maxBin])
	    {
	      maxBin  = i;
	    } else if (features[i] > features[secondMaxBin] && features[i] < features[maxBin])
	    {
	      secondMaxBin  = i;
	    } // if
				 
			} // for

			//	  maxBin		= freqEMM.maxBin;
			//	  secondMaxBin	= freqEMM.secondMaxBin;

			stroke(150, 50, 150);
			rect(maxBin*spacer, height/2, 1,  - features[maxBin] );


			stroke(50, 50, 255);
			rect(secondMaxBin * 10, height/2, 1, - features[secondMaxBin]);
		} catch(NullPointerException npe)
		{
			println("InputClassEclipse_HPSCorrection.drawHPSSecondHighest: " + npe.getMessage());
		}
	} // drawPSSecondHighest
} // Driver