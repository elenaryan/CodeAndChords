import processing.core.PApplet;

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
 * 11/05/2016
 * @author EmilyMeuer
 * 
 * If I can get multiple lines, can I store them as UGens?
 * 
 * Try going directly to AudioInputStream's?
 * 
 * First, try using Port's.
 *
 */

public class Driver extends PApplet {
	Input	input;
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
		AudioFormat	defaultAF	= new AudioFormat(44100, 16, 4, true, false);
		Mixer	inputMixer = AudioSystem.getMixer(AudioSystem.getMixerInfo()[3]);
		Mixer	outputMixer = AudioSystem.getMixer(AudioSystem.getMixerInfo()[3]);
		
		// [10]: Port FCA1616
//		Line.Info	lineInfo	= AudioSystem.getTargetLineInfo(AudioSystem.getMixerInfo()[10]);
		TargetDataLine	tdl;
		SourceDataLine	sdl;
		try {
//			tdl	= AudioSystem.getTargetDataLine(defaultAF, AudioSystem.getMixerInfo()[10]);
			sdl	= AudioSystem.getSourceDataLine(defaultAF, AudioSystem.getMixerInfo()[10]);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Driver.setup: line unavailable");		
		}
		
		sdl.getLineInfo();
		
		//		input	= new Input(1);
/*
		jsas = new JavasoundAudioServer(inputMixer,
	            outputMixer,
	            JSTimingMode mode,
	            AudioConfiguration context,
	            AudioClient client)
*/	            
		Info[] info = inputMixer.getSourceLineInfo();
		for(int i = 0; i < info.length; i++)
		{
//			AudioSystem.getLine(info[i]);
			System.out.println("[" + i + "]  " + info[i]);
		} // for
//		Line[] lines = inputMixer.getMaxLines(inputMixer.g)
//		System.out.println("lines.length = " + lines.length);
	} // setup
	
	public void draw()
	{
		background(204, 150, 204);
		
//		ellipse(width / 2, height - input.getAdjustedFund(), 50, 50);
	} // draw
}