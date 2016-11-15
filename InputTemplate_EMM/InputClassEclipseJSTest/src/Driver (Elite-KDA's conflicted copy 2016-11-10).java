import processing.core.PApplet;
<<<<<<< HEAD
=======
import java.io.ByteArrayOutputStream;
>>>>>>> 4a4f8e45b789ff3416305762fab4c04ba1075fd4

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.Line.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import org.jaudiolibs.audioservers.javasound.JavasoundAudioServer;

import net.beadsproject.beads.core.AudioContext;

/**
<<<<<<< HEAD
 * 11/05/2016
 * @author EmilyMeuer
 * 
 * ** See what other implementations have done -- how do people normally open lines?
 * 	In JACK? UGens?
 * 
 * If I can get multiple lines, can I store them as UGens?
 * 
 * Try going directly to AudioInputStream's?
 * 
 * First, try using Port's.
 * 
 * Could be bad news:
 * "Commonly, only one input port can be open at a time, but an audio-input mixer 
 * that mixes audio from multiple ports is also possible. Another scenario consists 
 * of a mixer that has no ports but instead gets its audio input over a network."
 * from Capturing Audio tutorial, https://docs.oracle.com/javase/tutorial/sound/capturing.html
 *
 * So either go back to JACK, or try OSC?
 */

public class Driver extends PApplet {
	Input	input;
	// Supposedly this one is deprecated, but how do we add JSAudioServer to the build path?
	JavasoundAudioServer	jsas;
	
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
		AudioFormat	defaultAF	= new AudioFormat(44100, 16, 1, true, false);
		Mixer	inputMixer = AudioSystem.getMixer(AudioSystem.getMixerInfo()[3]);
		Mixer	outputMixer = AudioSystem.getMixer(AudioSystem.getMixerInfo()[3]);
		

		
//		sdl.getLineInfo();
		
		//		input	= new Input(1);
/*
		jsas = new JavasoundAudioServer(inputMixer,
	            outputMixer,
	            JSTimingMode mode,
	            AudioConfiguration context,
	            AudioClient client)
*/	            
		
		Info[] 	sourceInfo = inputMixer.getSourceLineInfo();
		Info[]	targetInfo = inputMixer.getTargetLineInfo();
		
		// [10]: Port FCA1616
//		Line.Info	lineInfo	= AudioSystem.getTargetLineInfo(AudioSystem.getMixerInfo()[10]);
		TargetDataLine	tdl;
		TargetDataLine	tdl1;
		TargetDataLine	tdl2;
		SourceDataLine	sdl;
		try {
			tdl	= AudioSystem.getTargetDataLine(defaultAF, AudioSystem.getMixerInfo()[3]);
			tdl1	= AudioSystem.getTargetDataLine(defaultAF, AudioSystem.getMixerInfo()[3]);
			tdl2	= AudioSystem.getTargetDataLine(defaultAF, AudioSystem.getMixerInfo()[3]);
//			sdl	= AudioSystem.getSourceDataLine(defaultAF, AudioSystem.getMixerInfo()[10]);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Driver.setup: line unavailable");		
=======
 * 11/10/2016
 * I've got sound from the Target Data Line!
 * But what we need now:
 *  - sound from multiple DAC lines?
 *  - convert these amplitudes into meaningful frequencies (yikes!)
 * 
 * 11/05/2016
 * 
 * @author EmilyMeuer
 * 
 *         ** See what other implementations have done -- how do people normally
 *         open lines? In JACK? UGens?
 * 
 *         If I can get multiple lines, can I store them as UGens?
 * 
 *         Try going directly to AudioInputStream's?
 * 
 *         First, try using Port's.
 * 
 *         Could be bad news: "Commonly, only one input port can be open at a
 *         time, but an audio-input mixer that mixes audio from multiple ports
 *         is also possible. Another scenario consists of a mixer that has no
 *         ports but instead gets its audio input over a network." from
 *         Capturing Audio tutorial,
 *         https://docs.oracle.com/javase/tutorial/sound/capturing.html
 *
 *         So either go back to JACK, or try OSC?
 *         
 *         Here's an example of drawing a sound-wave: http://www.ee.columbia.edu/~dpwe/resources/Processing/Oscilloscope.pde
 */

public class Driver extends PApplet {
	Input input;
	// Supposedly this one is deprecated, but how do we add JSAudioServer to the
	// build path?
	JavasoundAudioServer jsas;

	TargetDataLine tdl;
	TargetDataLine tdl1;
	TargetDataLine tdl2;
	SourceDataLine sdl;

	boolean stopped = false; // reads from the line until this is set to true
	// (and I never set it.... maybe later we'll add a button for it.)

	public static void main(String[] args) {
		PApplet.main("Driver");
	} // main

	public void settings() {
		size(1000, 600);
	} // settings

	public void setup() {
		AudioFormat defaultAF = new AudioFormat(44100, 16, 1, true, false);
		Mixer inputMixer = AudioSystem.getMixer(AudioSystem.getMixerInfo()[1]);
		Mixer outputMixer = AudioSystem.getMixer(AudioSystem.getMixerInfo()[3]);

		// sdl.getLineInfo();

		// input = new Input(1);
		/*
		 * jsas = new JavasoundAudioServer(inputMixer, outputMixer, JSTimingMode
		 * mode, AudioConfiguration context, AudioClient client)
		 */

		Info[] sourceInfo = inputMixer.getSourceLineInfo();
		Info[] targetInfo = inputMixer.getTargetLineInfo();

		// [10]: Port FCA1616
		// Line.Info lineInfo =
		// AudioSystem.getTargetLineInfo(AudioSystem.getMixerInfo()[10]);

		try {
			tdl = AudioSystem.getTargetDataLine(defaultAF, AudioSystem.getMixerInfo()[1]);
			tdl1 = AudioSystem.getTargetDataLine(defaultAF, AudioSystem.getMixerInfo()[1]);
			tdl2 = AudioSystem.getTargetDataLine(defaultAF, AudioSystem.getMixerInfo()[1]);
			// sdl = AudioSystem.getSourceDataLine(defaultAF,
			// AudioSystem.getMixerInfo()[10]);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Driver.setup: line unavailable");
>>>>>>> 4a4f8e45b789ff3416305762fab4c04ba1075fd4
		}
		try {
			tdl.open(tdl.getFormat());
			tdl1.open(tdl1.getFormat());
			tdl2.open(tdl2.getFormat());
		} catch (LineUnavailableException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
<<<<<<< HEAD
		
		System.out.println("tdl.getLineInfo(): " + tdl.getLineInfo());
		System.out.println("tdl1.getLineInfo(): " + tdl1.getLineInfo());
		System.out.println("tdl2.getLineInfo(): " + tdl2.getLineInfo());
		
		System.out.println("Source: ");
		for(int i = 0; i < sourceInfo.length; i++)
		{
//			AudioSystem.getLine(info[i]);
			System.out.println("[" + i + "]  " + sourceInfo[i]);
		} // for
		
		System.out.println("Target: ");
		for(int i = 0; i < targetInfo.length; i++)
		{
//			AudioSystem.getLine(info[i]);
			System.out.println("[" + i + "]  " + targetInfo[i]);
		} // for
		
		System.out.println("inputMixer.getLineInfo(): " + inputMixer.getLineInfo());
		Line[] targetLines = inputMixer.getTargetLines();
		for(int i = 0; i < targetLines.length; i++)
		{
=======

		System.out.println("tdl.getLineInfo(): " + tdl.getLineInfo());
		System.out.println("tdl1.getLineInfo(): " + tdl1.getLineInfo());
		System.out.println("tdl2.getLineInfo(): " + tdl2.getLineInfo());

		System.out.println("Source: ");
		for (int i = 0; i < sourceInfo.length; i++) {
			// AudioSystem.getLine(info[i]);
			System.out.println("[" + i + "]  " + sourceInfo[i]);
		} // for

		System.out.println("Target: ");
		for (int i = 0; i < targetInfo.length; i++) {
			// AudioSystem.getLine(info[i]);
			System.out.println("[" + i + "]  " + targetInfo[i]);
		} // for

		System.out.println("inputMixer.getLineInfo(): " + inputMixer.getLineInfo());
		Line[] targetLines = inputMixer.getTargetLines();
		for (int i = 0; i < targetLines.length; i++) {
>>>>>>> 4a4f8e45b789ff3416305762fab4c04ba1075fd4
			System.out.println(i + ": " + targetLines[i].getLineInfo());
			try {
				targetLines[i].open();
			} catch (LineUnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
<<<<<<< HEAD
		System.out.println("max Source lines: " + inputMixer.getMaxLines(sourceInfo[0]));
		System.out.println("max Target lines: " + inputMixer.getMaxLines(targetInfo[0]));
//		Line[] lines = inputMixer.getMaxLines(inputMixer.g)
//		System.out.println("lines.length = " + lines.length);
		System.out.println();
//		printMixers();
	} // setup
	
	public void draw()
	{
		background(204, 150, 204);
		
//		ellipse(width / 2, height - input.getAdjustedFund(), 50, 50);
	} // draw
	
	private void printMixers() {
		  try {
		    Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
		    int i = 0;
		    for (Mixer.Info info : mixerInfos) {
		      Mixer m = AudioSystem.getMixer(info);
		      String mixerName = m.getMixerInfo().getName();
		      System.out.println("mixer--" + mixerName);
		      if (i++ == 0) {
//		        mixer = m;
		    	  System.out.println("commented out a weird line here");
		      }
		      Line.Info[] lineInfos = m.getSourceLineInfo();
		      for (Line.Info lineInfo : lineInfos) {
		        System.out.println("source---" + lineInfo);
		        Line line = m.getLine(lineInfo);

		        System.out.println("\tsource-----" + line);
		      }
		      Line.Info[] lineInfos2 = m.getTargetLineInfo();
		      for (Line.Info lineInfo : lineInfos2) {
		        System.out.println("target---" + lineInfo);
		        Line line = m.getLine(lineInfo);
		        System.out.println("\ttarget-----" + line);
		      }

		    }
		  } catch (Exception e) {
		    e.printStackTrace();
		  }

		} // printMixers
=======
		// Pretty sure that getMaxLines() is worthless -- that's what the API
		// says.
		// System.out.println("max Source lines: " +
		// inputMixer.getMaxLines(sourceInfo[0]));
		System.out.println("max Target lines: " + inputMixer.getMaxLines(targetInfo[0]));
		// Line[] lines = inputMixer.getMaxLines(inputMixer.g)
		// System.out.println("lines.length = " + lines.length);
		System.out.println();
		// printMixers();
	} // setup

	public void draw() {
		background(204, 150, 204);
		stroke(255);

		// Assume that the TargetDataLine, line, has already
		// been obtained and opened.
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int numBytesRead;
		byte[] data = new byte[tdl.getBufferSize() / 5];

		// Begin audio capture.
		tdl.start();

		// Here, stopped is a global boolean set by another thread.
		// while (!stopped) {
		// while(millis() % 1000 != 0) {
		// Read the next chunk of data from the TargetDataLine.
		numBytesRead = tdl.read(data, 0, data.length);
		// Save this chunk of data.
		out.write(data, 0, numBytesRead);

		float x1;
		float x2;
		for (int i = 0; i < data.length - 1; i++) {
			// this if just gets rid of a little of the data, since there's so much.
			if (i % 2 != 0) {
				x1 = map(i, 0, data.length, 0, width);
				x2 = map(i + 1, 0, data.length, 0, width);
				// System.out.println("data[" + i + "] = " + data[i]);
				// ellipse(width / 2, height /2, 100, 100);
				line(x1, (height / 2) + data[i], x2, (height / 2) + data[i + 1]);
				// background(204, 150, 204);
				stroke(255);
			} // if
			// ellipse(i, width / 2, 20, 20);
		} // for

		// } // while

		// ellipse(width / 2, height - input.getAdjustedFund(), 50, 50);
	} // draw

	private void printMixers() {
		try {
			Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
			int i = 0;
			for (Mixer.Info info : mixerInfos) {
				Mixer m = AudioSystem.getMixer(info);
				String mixerName = m.getMixerInfo().getName();
				System.out.println("mixer--" + mixerName);
				if (i++ == 0) {
					// mixer = m;
					System.out.println("commented out a weird line here");
				}
				Line.Info[] lineInfos = m.getSourceLineInfo();
				for (Line.Info lineInfo : lineInfos) {
					System.out.println("source---" + lineInfo);
					Line line = m.getLine(lineInfo);

					System.out.println("\tsource-----" + line);
				}
				Line.Info[] lineInfos2 = m.getTargetLineInfo();
				for (Line.Info lineInfo : lineInfos2) {
					System.out.println("target---" + lineInfo);
					Line line = m.getLine(lineInfo);
					System.out.println("\ttarget-----" + line);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	} // printMixers
>>>>>>> 4a4f8e45b789ff3416305762fab4c04ba1075fd4
} // Driver