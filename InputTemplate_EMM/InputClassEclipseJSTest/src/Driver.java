import processing.core.PApplet;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.Line.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.jaudiolibs.audioservers.javasound.JavasoundAudioServer;

import net.beadsproject.beads.core.AudioContext;

/**
 * TODO:
 *  - add a button to control "stopped"
 *  - test w/built-in mic
 *  
 * 11/05/2016
 * @author EmilyMeuer
 * 
 * NB: lights didn't start on Behringer until I selected it (FCA1616) as default input device.
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
 * 
 * 11/06:
 * Reading from AudioInputStream from this example: http://www.jsresources.org/examples/AudioDataBuffer.java.html
 */

public class Driver extends PApplet {
	Input	input;
	// Supposedly this one is deprecated, but how do we add JSAudioServer to the build path?
	JavasoundAudioServer	jsas;
	
	TargetDataLine	tdl;
	TargetDataLine	tdl1;
	TargetDataLine	tdl2;
	SourceDataLine	sdl;
	float[]	features;
	float[]	features1;
	float[]	features2;
	AudioInputStream	ais;
	AudioInputStream	ais1;
	AudioInputStream	ais2;

	// Used in setup():
	AudioFileFormat 		fileFormat;
	AudioFileFormat.Type	targetFileType;
	AudioFormat 			audioFormat;
	
	// Used to store the audio data and draw waveforms:
	byte[] abAudioData;
	byte[] abAudioData1;
	byte[] abAudioData2;
	/** The size of the temporary read buffer, in frames.
	 */
	private static final int	BUFFER_LENGTH = 1024;
	
	boolean	stopped	= false;
	
	public static void main(String[] args)
	{
		PApplet.main("Driver");
	} // main
	
	public void settings()
	{
		size(1024,600);
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

		try {
			tdl	= AudioSystem.getTargetDataLine(defaultAF, AudioSystem.getMixerInfo()[1]);
			tdl1	= AudioSystem.getTargetDataLine(defaultAF, AudioSystem.getMixerInfo()[3]);
			tdl2	= AudioSystem.getTargetDataLine(defaultAF, AudioSystem.getMixerInfo()[3]);
//			sdl	= AudioSystem.getSourceDataLine(defaultAF, AudioSystem.getMixerInfo()[11]);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Driver.setup: line unavailable");		
		}
		try {
			tdl.open(tdl.getFormat());
			tdl1.open(tdl1.getFormat());
			tdl2.open(tdl2.getFormat());
		} catch (LineUnavailableException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
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
			System.out.println(i + ": " + targetLines[i].getLineInfo());
			try {
				targetLines[i].open();
			} catch (LineUnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("max Source lines: " + inputMixer.getMaxLines(sourceInfo[0]));
		System.out.println("max Target lines: " + inputMixer.getMaxLines(targetInfo[0]));
//		Line[] lines = inputMixer.getMaxLines(inputMixer.g)
//		System.out.println("lines.length = " + lines.length);
		System.out.println();
//		printMixers();
		
		features	= new float[tdl.getBufferSize()];
		features1	= new float[tdl1.getBufferSize()];
		features2	= new float[tdl2.getBufferSize()];
		
		ais		= new AudioInputStream(tdl);
		System.out.println("ais.getFrameLength = " + ais.getFrameLength());
		ais1	= new AudioInputStream(tdl1);
		ais2	= new AudioInputStream(tdl2);

		/*
		InputStream bufferedIn = new BufferedInputStream(ais);
		System.out.println("ais.getFrameLength = " + ais.getFrameLength());
		ais = new AudioInputStream(bufferedIn, ais.getFormat(), ais.getFrameLength());
*/
		
		//			fileFormat = AudioSystem.getAudioFileFormat(ais);
//			targetFileType = fileFormat.getType();
//			audioFormat = fileFormat.getFormat();			
		audioFormat = tdl.getFormat();

		stroke(255);
	} // setup
	
	public void draw()
	{
		background(204, 150, 204);
//		stroke(255);
		
//		ellipse(width / 2, height - input.getAdjustedFund(), 50, 50);
		

		try
		{
			
		/* Read the audio data into a memory buffer.
		 */
//		AudioInputStream inputAIS = AudioSystem.getAudioInputStream(sourceFile);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int nBufferSize = BUFFER_LENGTH * audioFormat.getFrameSize();
		byte[]	abBuffer = new byte[nBufferSize];
		
		System.out.println("baos.toString(): " + baos.toString());
//		System.out.println("baos = " + baos + "; audioFormat.getFrameSize() = " + audioFormat.getFrameSize() + "; abBuffer = " + abBuffer);
		while (true)
		{
//			if (DEBUG) { out("trying to read (bytes): " + abBuffer.length); }
			int	nBytesRead = ais.read(abBuffer);
//			if (DEBUG) { out("read (bytes): " + nBytesRead); }
			if (nBytesRead == -1)
			{
				break;
			}
			baos.write(abBuffer, 0, nBytesRead);
		} // while

		/* Here's the byte array everybody wants.
		 */
			abAudioData = baos.toByteArray();
		} catch(IOException ioe) {
			throw new IllegalArgumentException(ioe.getMessage());
		} // catch
		
		// Trying a different tactic:
		// Assume that the TargetDataLine, line, has already
		// been obtained and opened.
		ByteArrayOutputStream out  = new ByteArrayOutputStream();
		int numBytesRead;
		byte[] data = new byte[tdl.getBufferSize() / 5];

		// Begin audio capture.
		tdl.start();

		// Here, stopped is a global boolean set by another thread.
		while (!stopped) {
		   // Read the next chunk of data from the TargetDataLine.
		   numBytesRead =  tdl.read(data, 0, data.length);
		   // Save this chunk of data.
		   out.write(data, 0, numBytesRead);
		} // while
		
		System.out.println("millis() = " + millis());

		// Draw the sound waves:
		for(int x = 0; x < data.length - 1; x++)
		{
			line( x, 50 + data[x]*50, x+1, 50 + data[x+1]*50 );
		} // for

		/*
//		float[] features = testInput.ps.getFeatures();
//		tdl.read(features, 0, features.length);
//	    if(features != null){
	      for(int x = 0; x < abAudioData.length - 1; x++){
	//          int featureIndex = (x * features.length) / width;
	 //         int barHeight = Math.min((int)(features[featureIndex] * height), height - 1);
	         line( x, 50 + abAudioData[x]*50, x+1, 50 + abAudioData[x+1]*50 );
	         System.out.println("abAudioData[" + x + "] = " + abAudioData[x]);
	      } // for
//	    } // if
 *
 */
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
} // Driver
