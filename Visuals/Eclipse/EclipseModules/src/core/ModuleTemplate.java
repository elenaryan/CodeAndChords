package core;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Map;

import controlP5.Button;
import controlP5.ColorWheel;
import controlP5.ControlEvent;
import controlP5.ControlFont;
import controlP5.ControlP5;
import controlP5.ControlP5Constants;
import controlP5.Controller;
import controlP5.ScrollableList;
import controlP5.Slider;
import controlP5.Textfield;
import controlP5.Textlabel;
import controlP5.Toggle;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * Future:
 *  - look into putting things into Groups -- either all in one, or each section its own group?
 *  - ControlListener that takes everything and doesn't require Modules to have controlEvent()?
 *    (see A.S. answer: https://forum.processing.org/two/discussion/2692/controlp5-problems-creating-a-toggle-controller-with-custom-images-on-a-second-tab)
 * - Custom controllers: http://www.sojamo.de/libraries/controlP5/reference/controlP5/ControllerView.html
 * 
 * Emily Meuer
 * 01/11/2017
 * 
 * Putting all the pop-out sidebar/moduleTemplate stuff from Module_01_02 in this class.
 * 
 * @author Emily Meuer
 *
 */
public class ModuleTemplate {

	// Static var's: colorStyles
	private	static	float	CS_RAINBOW	= 1;
	private	static	float	CS_DICHROM	= 2;
	private	static	float	CS_TRICHROM	= 3;
	private	static	float	CS_CUSTOM	= 4;
	private	float	curColorStyle;

	// For rounding numbers in sliders to two digits:
	private	DecimalFormat	decimalFormat	= new DecimalFormat("#.##");

	// Choose input file here:
	// Raw:
	//String  inputFile  = "src/module_01_PitchHueBackground/module_01_02_PitchHueBackground_ModuleTemplate_EMM/Emily_CMajor-2016_09_2-16bit-44.1K Raw.wav";
	// Tuned:
	String  inputFile  = "src/module_01_PitchHueBackground/module_01_02_PitchHueBackground_ModuleTemplate_EMM/Emily_CMajor-2016_09_2-16bit-44.1K Tuned.wav";
	// Kanye:
	//String  inputFile  = "src/module_01_PitchHueBackground/module_01_02_PitchHueBackground_ModuleTemplate_EMM/Emily_CMajor-2016_09_2-16bit-44.1K Kanye.wav";

	// Global vars - TODO: all private!
	private	PApplet		parent;
	private ControlP5 	nonSidebarCP5;
	private ControlP5 	sidebarCP5;
	private	ControlP5	colorWheelCP5;	// Need a separate CP5 so that I can turn off autoDraw on the others, 
	// draw a transparent rectangle, and then draw only the ColorWheels on top of that.
	private	Input		input;

	private Toggle		play;
	private	Button		hamburger;
	private	Button		menuX;

	private	Textlabel	hideLabel;
	private	Toggle		hidePlayButton;
	private	Toggle		hideMenuButton;
	private	Toggle		hideScale;

	// These are prob. extraneous, since I can get them from the ControlP5 by knowing their label...
	private	Textlabel	thresholdLabel;
	private	Slider		threshold;
	private	Textfield	thresholdTF;
	private	Textlabel	attackLabel;
	private	Slider		attack;
	private	Textfield	attackTF;
	private	Textlabel	releaseLabel;
	private	Slider		release;
	private	Textfield	releaseTF;
	private	Textlabel	transitionLabel;
	private	Slider		transition;
	private	Textfield	transitionTF;

	private	int			leftAlign;
	private	int			leftEdgeX;
	private	int[]		leftEdgeXArray;

	private	String		sidebarTitle;

	private	int			scaleLength;
	private	int			majMinChrom;
	private	String		curKey;
	private int 		keyAddVal;		// amount that must be subtracted in legend() 
	// to line pitches up with the correct scale degree of the current key.

	private	final String[]	notesCtoBFlats	= new String[] { 
			"C", "Db", "D", "Eb", "E", "F", "Gb",  "G", "Ab", "A", "Bb", "B"
	};

	private final String[]	notesCtoBSharps	= new String[] { 
			"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
	};

	private	final	int[][] scaleDegrees = new int[][] {
		// major:
		new int[]  { 0, 2, 4, 5, 7, 9, 11
		},
		// minor:
		new int[]  { 0, 2, 3, 5, 7, 8, 10
		}
	}; // scaleDegrees

	private	float[][]	colors;
	private int[] 		rootColor;

	int[]				textYVals;
	int[]				noteYVals;
	int[]				modulateYVals;

	private	boolean		showScale;

	private	float		thresholdLevel;
	private	float		attackTime;
	private	float		releaseTime;
	private	float		transitionTime;

	private	float[]		attackReleaseTransition	= new float[3];

	public ModuleTemplate(PApplet parent, Input input, String sidebarTitle)
	{
		this.parent	= parent;
		this.input	= input;

		// ControlP5 for playButton and hamburger:
		this.nonSidebarCP5	= new ControlP5(this.parent);

		// ControlP5 for most of the sidebar elements:
		this.sidebarCP5		= new ControlP5(this.parent);

		// This technically works, but it horribly blurry:
		//		this.sidebarCP5.setFont(this.parent.createFont("Consolas", 10) );

		this.sidebarCP5.setVisible(false);

		// ControlP5 for ColorWheels (having a separate one allows us to setAutoDraw(false) on the other CP5,
		// draw a transparent rectangle over those controllers, and then draw the ColorWheel on top of that):
//		this.colorWheelCP5	= new ControlP5(this.parent);
//		this.sidebarCP5.setVisible(false);

		this.sidebarTitle	= sidebarTitle;

		//		this.leftEdgeXArray	= new int[] { 0, this.parent.width / 3 };
		this.setLeftEdgeX(0);

		this.setColors(new float[12][3]);

		this.curColorStyle	= this.CS_RAINBOW;
		this.rootColor	= new int[] { 255, 0, 0, };

		this.setCurKey("A", 2);
		this.rainbow();

		this.textYVals		= new int[9];
		this.noteYVals		= new int[6];
		this.modulateYVals	= new int[3];

		//this.initModuleTemplate();
	} // ModuleTemplate

	// Methods:
	//TODO: make initModuleTemplate() private again, once it can be called from constructor.

	/**
	 * Called from constructor to calculate Y vals and call the methods for instantiating the necessary buttons;
	 * will eventually call different button methods depending on the module number.
	 */
	public void initModuleTemplate()
	{
		// Add play button, hamburger and menu x:
		addOutsideButtons();

		// Add Menu and Title labels (after menuX, because it gets its x values from that):
		ControlFont	largerStandard	= new ControlFont(ControlP5.BitFontStandard58, 13);

		this.sidebarCP5.addTextlabel("title")
		.setPosition((this.parent.width / 3) / 4, 5)	// This x val is usually this.leftAlign, but doesn't get set until sidebar is open.
		// TODO: is this ^ wise?  Maybe always set leftAlign to be (this.parent.width / 3) / 4?
		.setFont(largerStandard)
		//			.setFont(this.parent.createFont("Consolas", 12, true))	// This is so blurry....
		.setValue(this.sidebarTitle);

		this.sidebarCP5.addTextlabel("menu")
		.setPosition(this.menuX.getPosition()[0] + this.menuX.getWidth() + 3, 10)
		.setHeight(15)
		.setValue("Menu");

		// calculate y's
		// set y vals for first set of scrollbar labels:
		textYVals[0]	=	26;
		// Given our height = 250 and "hide" (textYVals[0]) starts at [40] - now 26 (1/17),
		// We want a difference of 27.  This gets that:
		int	yValDif = (int)((this.parent.height - textYVals[0]) / 18);//(textYVals.length + noteYVals.length + modulateYVals.length));
		// ... but no smaller than 25:
		if(yValDif < 25) {
			yValDif	= 25;
		}
		System.out.println("yValDif = " + yValDif);
		yValDif = 26;

		for(int i = 1; i < textYVals.length - 1; i++)
		{
			textYVals[i]	= textYVals[i - 1] + yValDif;
		} // for
		// Add extra space before "Pitch Color Codes":
		textYVals[textYVals.length - 1]	= textYVals[textYVals.length - 2] + (int)(yValDif * 1.5);

		// set y vals for the note names:
		noteYVals[0]	= textYVals[textYVals.length - 1] + yValDif;
		for(int i = 1; i < noteYVals.length; i++)
		{
			noteYVals[i]	= noteYVals[i - 1] + yValDif;
		}

		// set y vals for the color modulate scrollbars:
		// (add double space between this and previous group of note text fields)
		modulateYVals[0]	= noteYVals[noteYVals.length - 1] + (int)(yValDif * 1.5);
		for(int i = 1; i < modulateYVals.length; i++)
		{
			modulateYVals[i]	= modulateYVals[i - 1] + yValDif;
		}

		// leftAlign will be set in displaySidebar in relation to leftEdgeX, 
		// but the button functions need to use it earlier:
		this.leftAlign	= (this.parent.width / 3) / 4;

		// call add methods:
		addHideButtons(textYVals[0]);

		// TODO: pass it one Y and either a height and spacer or a distance between y's.
		addSliders(textYVals[1], textYVals[2], textYVals[3], textYVals[4]);

		addKeySelector(textYVals[5]);

		addRootColorSelector(textYVals[6]);

		addColorStyleButtons(textYVals[7]);

		this.addCustomPitchColor(textYVals[8], noteYVals);

		addModulateSliders(modulateYVals);

		this.sidebarCP5.getController("keyDropdown").bringToFront();
	} // initModuleTemplate

	/*
	 *  - alignLeft (x var to pass to the add functions)
	 *  - yValues (will pass the appropriate one to each of the functions)
	 *  TODO: how calculate these y values?  (for now, imagine they are correct...)
	 *  
	 */

	private void addOutsideButtons()
	{
		int	playX		= this.parent.width - 45;
		int	playY		= 15;
		int	playWidth	= 30;
		int	playHeight	= 30;

		// add play button:
		PImage[]	images	= { this.parent.loadImage("playButton.png"), this.parent.loadImage("stopButton.png") };

		images[0].resize(playWidth - 5, playHeight);
		images[1].resize(playWidth, playHeight);
		this.play	= this.nonSidebarCP5.addToggle("play")
				.setPosition(playX, playY)
				.setImages(images)
				.updateSize();

		int	hamburgerX		= 10;
		int	hamburgerY		= 13;
		int	hamburgerWidth	= 30;
		int	hamburgerHeight	= 30;

		PImage	hamburger	= this.parent.loadImage("hamburger.png");
		hamburger.resize(hamburgerWidth, hamburgerHeight);
		this.hamburger	= this.nonSidebarCP5.addButton("hamburger")
				.setPosition(hamburgerX, hamburgerY)
				.setImage(hamburger)
				.updateSize();

		int	menuXX			= 5;
		int	menuXY			= 5;
		int	menuXWidth		= 15;
		int	menuXHeight		= 15;

		PImage	menuX	= this.parent.loadImage("menuX.png");
		menuX.resize(menuXWidth, 0);
		this.menuX	= this.sidebarCP5.addButton("menuX")
				.setPosition(menuXX, menuXY)
				.setImage(menuX)
				.updateSize();
	} // addOutsideButtons

	private void addHideButtons(int	hideY)
	{
		//TODO: can all labels connected to this controller be Center aligned automatically?
		int	hideWidth     = 70;
		int hideSpace	= 4;

		int	labelX		= 10;
		int	playButtonX	= this.leftAlign;
		int	menuButtonX	= this.leftAlign + hideWidth + hideSpace;
		int	scaleX		= this.leftAlign + (+ hideWidth + hideSpace) * 2;

		this.sidebarCP5.addTextlabel("hide")
		.setPosition(labelX, hideY + 4)
		.setValue("Hide");

		this.hidePlayButton	= this.sidebarCP5.addToggle("playButton")
				.setPosition(playButtonX, hideY)
				.setWidth(hideWidth)
				.setId(4);
		this.hidePlayButton.getCaptionLabel().set("Play Button").align(ControlP5.CENTER, ControlP5.CENTER);


		this.hideMenuButton	= this.sidebarCP5.addToggle("menuButton")
				.setPosition(menuButtonX, hideY)
				.setWidth(hideWidth)
				.setId(5);
		this.hideMenuButton.getCaptionLabel().set("Menu Button").align(ControlP5.CENTER, ControlP5.CENTER);


		this.hideScale	= this.sidebarCP5.addToggle("scale")
				.setPosition(scaleX, hideY)
				.setWidth(hideWidth)
				.toggle()
				.setId(6);
		this.hideScale.getCaptionLabel().set("Scale").align(ControlP5.CENTER, ControlP5.CENTER);

	} // addHideButtons

	/**
	 * Method called during initialization to instatiate the Threshold, Attack, Release,
	 * and Transition sliders.
	 * 
	 * Sliders have an odd and Textfields an even ID number, all less than 10 (no duplicates allowed).
	 * This will be used to connect them in controlEvent.
	 * Names are based on ids; format: "slider" OR "textfield + [id]
	 *  - thresholdSlider	= "slider0", thresholdTF	= "textfield1"
	 *  - attackSlider	= "slider2", attackTF	= "textfield3"
	 *  - releaseSlider	= "slider4", releaseTF	= "textfield5"
	 *  - transitionSlider	= "slider6", transitionTF	= "textfield7"
	 * 
	 * @param thresholdY	y value of the Threshold slider group
	 * @param attackY	y value of the Attack slider group
	 * @param releaseY		y value of the Release slider group
	 * @param transitionY	y value of the Transition slider group
	 */
	private void addSliders(int thresholdY, int attackY, int releaseY, int transitionY)
	{
		int	labelX			= 10;
		int	labelWidth		= 70;

		int	sliderWidth		= 145;
		int	sliderHeight	= 20;

		int	spacer			= 5;
		int	tfWidth			= 70;

		this.thresholdLabel	= this.sidebarCP5.addLabel("thresholdLabel")
				.setPosition(labelX, thresholdY + 4)
				.setWidth(labelWidth)
				.setValue("Threshold");
		System.out.println("sliderWidth = " + sliderWidth + "; sliderHeight = " + sliderHeight);

		// Threshold slider:
		this.threshold	= this.sidebarCP5.addSlider("slider0")
				.setPosition(this.leftAlign, thresholdY)
				.setSize(sliderWidth, sliderHeight)
				.setSliderMode(Slider.FLEXIBLE)
				.setRange(2, 100)
				.setValue(10)
				.setLabelVisible(false)
				.setId(0);
		this.setThresholdLevel(10);

		// Threshold textfield:
		this.thresholdTF	= this.sidebarCP5.addTextfield("textfield1")
				.setPosition(this.leftAlign + sliderWidth + spacer, thresholdY)
				.setSize(tfWidth, sliderHeight)
				.setLabelVisible(false)
				.setText(this.threshold.getValue() + "")
				.setLabelVisible(false)
				.setAutoClear(false)
				.setId(1);

		// Test: not adding them as variables, seeing how that goes. :)

		// Attack group:
		//	- Textlabel:
		this.sidebarCP5.addLabel("attackLabel")
		.setPosition(labelX, attackY + 4)
		.setWidth(labelWidth)
		.setValue("Attack");

		//	- Slider:
		this.sidebarCP5.addSlider("slider2")
		.setPosition(this.leftAlign, attackY)
		.setSize(sliderWidth, sliderHeight)
		.setSliderMode(Slider.FLEXIBLE)
		.setRange(2, 20)
		.setValue(10)
		.setLabelVisible(false)
		.setId(2);

		// Setting attack for reference by Module:
		this.attackReleaseTransition[0]	= 10;

		//	- Textfield:
		this.sidebarCP5.addTextfield("textfield3")
		.setPosition(this.leftAlign + sliderWidth + spacer, attackY)
		.setSize(tfWidth, sliderHeight)
		.setText(this.sidebarCP5.getController("slider2").getValue() + "")
		.setLabelVisible(false)
		.setAutoClear(false)
		.setId(3);

		// Release:
		// - Textlabel:
		this.sidebarCP5.addLabel("releaseLabel")
		.setPosition(labelX, releaseY + 4)
		.setWidth(labelWidth)
		.setValue("Release");

		//	- Slider:
		this.sidebarCP5.addSlider("slider4")
		.setPosition(this.leftAlign, releaseY)
		.setSize(sliderWidth, sliderHeight)
		.setSliderMode(Slider.FLEXIBLE)
		.setRange(2, 20)
		.setValue(10)
		.setLabelVisible(false)
		.setId(4);

		// Setting release for reference by Module:
		this.attackReleaseTransition[1]	= 10;

		//	- Textlabel:
		this.sidebarCP5.addTextfield("textfield5")
		.setPosition(this.leftAlign + sliderWidth + spacer, releaseY)
		.setSize(tfWidth, sliderHeight)
		.setText(this.sidebarCP5.getController("slider4").getValue() + "")
		.setLabelVisible(false)
		.setAutoClear(false)
		.setId(5);

		// Transition:
		// - Textlabel:
		this.sidebarCP5.addLabel("transitionLabel")
		.setPosition(labelX, transitionY + 4)
		.setWidth(labelWidth)
		.setValue("Transition");

		//	- Slider:
		this.sidebarCP5.addSlider("slider6")
		.setPosition(this.leftAlign, transitionY)
		.setSize(sliderWidth, sliderHeight)
		.setSliderMode(Slider.FLEXIBLE)
		.setRange(2, 20)
		.setValue(10)
		.setLabelVisible(false)
		.setId(6);

		// Setting transition for reference by Module:
		this.attackReleaseTransition[2]	= 10;

		//	- Textlabel:
		this.sidebarCP5.addTextfield("textfield7")
		.setPosition(this.leftAlign + sliderWidth + spacer, transitionY)
		.setSize(tfWidth, sliderHeight)
		.setText(this.sidebarCP5.getController("slider6").getValue() + "")
		.setLabelVisible(false)
		.setAutoClear(false)
		.setId(7);

	} // addSliders

	/**
	 * Method called during instantiation to initialize the key selector drop-down menu (ScrollableList)
	 * and major/minor/chromatic selection buttons.
	 * 
	 * @param keyY	y value of the menu and buttons.
	 */
	private void addKeySelector(int	keyY)
	{

		int	labelX			= 10;
		int	labelWidth		= 70;

		int	listWidth		= 65;
		int	spacer			= 5;

		int	toggleWidth		= 45;
		int	majorX			= this.leftAlign + listWidth + spacer;
		int	minorX			= this.leftAlign + listWidth + spacer + (toggleWidth + spacer);
		int	chromX			= this.leftAlign + listWidth + spacer + ((toggleWidth + spacer) * 2);

		String[] keyOptions	= new String[] {
				"A", "Bb", "B", "C", "Db", "D", "Eb", "E", "F", "F#", "Gb", "G", "Ab"
		};

		// "Key" Textlabel
		this.sidebarCP5.addTextlabel("key")
		.setPosition(labelX, keyY + 4)
		.setValue("Key");

		// "Letter" drop-down menu (better name?)
		this.sidebarCP5.addScrollableList("keyDropdown")
		.setPosition(this.leftAlign, keyY)
		.setWidth(listWidth)
		.setItems(keyOptions)
		.setOpen(false)
		.setLabel("Select a key:")
		.getCaptionLabel().toUpperCase(false);

		// Maj/Min/Chrom Toggles
		// (These each have an internalValue - 0 = Major, 1 = Minor, and 2 = Chromatic - 
		//  and will set this.majMinChrom to their value when clicked.)
		this.sidebarCP5.addToggle("major")
		.setPosition(majorX, keyY)
		.setWidth(toggleWidth)
		.setCaptionLabel("Major")
		.setInternalValue(0);
		this.sidebarCP5.getController("major").getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER);

		this.sidebarCP5.addToggle("minor")
		.setPosition(minorX, keyY)
		.setWidth(toggleWidth)
		.setCaptionLabel("Minor")
		.setInternalValue(1);
		this.sidebarCP5.getController("minor").getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER);


		this.sidebarCP5.addToggle("chrom")
		.setPosition(chromX, keyY)
		.setWidth(toggleWidth)
		.setCaptionLabel("Chromatic")
		.setInternalValue(2);
		this.sidebarCP5.getController("chrom").getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER);

		((Toggle)(this.sidebarCP5.getController("chrom"))).setState(true);
	} // addKeySelector

	/**
	 * Method called during instantiation to initialize the root color selector.
	 * 
	 * @param rootColorY	y value of the root color selector.
	 */
	private void addRootColorSelector(int rootColorY)
	{
		int	labelX			= 10;
		int	buttonWidth		= 50;
		int	textfieldX		= this.leftAlign + buttonWidth + 5;
		int	textfieldWidth	= 90;

		this.sidebarCP5.addTextlabel("rootColor")
		.setPosition(labelX, rootColorY + 4)
		.setValue("Root Color");

		// Buttons, ColorWheels and corresponding Textfields will have id's of 21 or over;
		// Button id % 3 == 0; ColorWheel id % 3 == 1, Textfield id % 3 == 2.

		// Needs to be added to sidebarCP5 so it is still visible to turn off the ColorWheel:
		// (name follows conventions for customPitchColor buttons)
		this.sidebarCP5.addButton("rootColorButton")
		.setPosition(this.leftAlign, rootColorY)
		.setWidth(buttonWidth)
		.setLabel("Root")
		.setId(21);

		this.sidebarCP5.addColorWheel("rootColorWheel")
		.setPosition(this.leftAlign, rootColorY + 20)
		.setRGB(this.parent.color(102, 0, 102))
		.setLabelVisible(false)
		.setVisible(false)
		.setId(22);

		this.sidebarCP5.addTextfield("rootColorTF")
		.setPosition(textfieldX, rootColorY)
		.setWidth(textfieldWidth)
		.setAutoClear(false)
		.setLabelVisible(false)
		.setText("Code#")
		.setId(23);
	} // addRootColorSelector

	/**
	 * Method called during instantiation to initialize the color style Toggles
	 * (Rainbow, Dichromatic, Trichromatic, and Custom).
	 * 
	 * @param colorStyleY	y value of the colorStyle Toggles
	 */
	private void addColorStyleButtons(int colorStyleY)
	{
		int	colorStyleWidth	= 50;
		int	colorStyleSpace	= 6;

		int	labelX			= 10;

		int rainbowX     	= this.leftAlign;
		int dichromaticX	= this.leftAlign + colorStyleWidth + colorStyleSpace;
		int trichromaticX	= this.leftAlign + (colorStyleWidth + colorStyleSpace) * 2;
		int customX			= this.leftAlign + (colorStyleWidth + colorStyleSpace) * 3;

		this.sidebarCP5.addTextlabel("colorStyle")
		.setPosition(labelX, colorStyleY + 4)
		.setValue("Color Style");

		this.sidebarCP5.addToggle("rainbow")
		.setPosition(rainbowX, colorStyleY)
		.setWidth(colorStyleWidth)
		.setCaptionLabel("Rainbow")
		.setState(true)
		.setInternalValue(this.CS_RAINBOW);
		this.sidebarCP5.getController("rainbow").getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER);

		this.sidebarCP5.addToggle("dichrom")
		.setPosition(dichromaticX, colorStyleY)
		.setWidth(colorStyleWidth)
		.setCaptionLabel("Dichrom.")
		.setInternalValue(this.CS_DICHROM);
		this.sidebarCP5.getController("dichrom").getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER);

		this.sidebarCP5.addToggle("trichrom")
		.setPosition(trichromaticX, colorStyleY)
		.setWidth(colorStyleWidth)
		.setCaptionLabel("Trichrom.")
		.setInternalValue(this.CS_TRICHROM);
		this.sidebarCP5.getController("trichrom").getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER);

		this.sidebarCP5.addToggle("custom")
		.setPosition(customX, colorStyleY)
		.setWidth(colorStyleWidth)
		.setCaptionLabel("Custom")
		.setInternalValue(this.CS_CUSTOM);
		this.sidebarCP5.getController("custom").getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER);
	} // addColorStyleButtons

	/**
	 * Method called during instantiation to initialize note buttons and their corresponding ColorWheels.
	 * 
	 * @param noteYVals	int[] of y values for each note button
	 */
	private void addCustomPitchColor(int labelYVal, int[] noteYVals)
	{
		int spacer1			= 5;	// between buttons and textfields
		int	spacer2			= 15;	// between the two rows of pitches
		int	labelX			= 10;

		int	buttonWidth		= 30;
		int	textfieldWidth	= 90;

		int	noteX1			= this.leftAlign - 40;
		int	textfieldX1		= noteX1 + buttonWidth + spacer1;

		int	noteX2			= textfieldX1 + textfieldWidth + spacer2;
		int	textfieldX2		= noteX2 + buttonWidth + spacer1;
		
		int	colorWheelX		= textfieldX1;


		String[]	noteNames1	= new String[] {
				"A", "A#/Bb", "B", "C", "C#/Db", "D"
		}; // noteNames
		String[]	noteNames2	= new String[] {
				"D#/Eb", "E", "F", "F#/Gb", "G", "G#/Ab"
		}; // noteNames2

		this.sidebarCP5.addTextlabel("customPitchColor")
		.setPosition(labelX, labelYVal + 4)
		.setValue("Custom Pitch Color");

		// Note Buttons, ColorWheels and corresponding Textfields will have id's of 24 or over;
		// Button id % 3 == 0; ColorWheel id % 3 == 1, Textfield id % 3 == 2.
		int	namePos	= 0;
		int	id		= 24;

		// First row of pitches:
		for(int i = 0; i < noteNames1.length; i++)
		{
			// Needs to be added to sidebarCP5 so it is still visible to turn off the ColorWheel:
			this.sidebarCP5.addButton("button" + id)
			.setPosition(noteX1, noteYVals[i])
			.setWidth(buttonWidth)
			.setLabel(noteNames1[namePos])
			.setId(id)
			.getCaptionLabel().toUpperCase(false);

			id = id + 1;

			this.sidebarCP5.addColorWheel("colorWheel" + id)
			.setPosition(colorWheelX, noteYVals[i] - 200)		// 200 = height of ColorWheel
			.setRGB(this.parent.color(102, 0, 102))
			.setLabelVisible(false)
			.setVisible(false)
			.setId(id);

			id = id + 1;

			this.sidebarCP5.addTextfield("textfield" + id)
			.setPosition(textfieldX1, noteYVals[i])
			.setWidth(textfieldWidth)
			.setAutoClear(false)
			.setLabelVisible(false)
			.setText("Code#")
			.setId(id);

			id = id + 1;
			namePos	= namePos + 1;
		} // first row of pitches

		namePos	= 0;
		// Second row of pitches:
		for(int i = 0; i < noteNames1.length; i++)
		{
			this.sidebarCP5.addButton("button" + id)
			.setPosition(noteX2, noteYVals[i])
			.setWidth(buttonWidth)
			.setLabel(noteNames2[namePos])
			.setId(id)
			.getCaptionLabel().toUpperCase(false);

			id = id + 1;

			this.sidebarCP5.addColorWheel("colorWheel" + id)
			.setPosition(noteX2, noteYVals[i] - 200)
			.setRGB(this.parent.color(102, 0, 102))
			.setLabelVisible(false)
			.setVisible(false)
			.setId(id);

			id = id + 1;

			this.sidebarCP5.addTextfield("textfield" + id)
			.setPosition(textfieldX2, noteYVals[i])
			.setWidth(textfieldWidth)
			.setAutoClear(false)
			.setLabelVisible(false)
			.setText("Code#")
			.setId(id);

			id = id + 1;
			namePos	= namePos + 1;
		} // for - second row of pitches
		
		Color	transparentBlack	= new Color(0, 0, 0, 200);
		int		transBlackInt		= transparentBlack.getRGB();
		
		this.sidebarCP5.addBackground("background")
			.setPosition(0, 0)
			.setSize(this.parent.width / 3, this.parent.height)
			.setBackgroundColor(transBlackInt)
			.setVisible(false);
	} // addNoteColorSelectors

	/**
	 * Method called during instantiation, to initialize the color modulate sliders.
	 * 
	 * @param modulateYVals	int[] of the y values of the red, green, and blue sliders, respectively.
	 */
	private void addModulateSliders(int[] modulateYVals)
	{
		int	labelX			= 10;
		int	labelWidth		= 70;

		int	sliderWidth		= 145;
		int	sliderHeight	= 20;

		int	spacer			= 5;	// distance between slider and corresponding textfield
		int	tfWidth			= 70;	// width of Textfields

		String[]	names	= new String[] { "redModLabel", "greenModLabel", "blueModLabel" };
		String[]	values	= new String[] { "Red Modulate", "Green mod.", "Blue modulate" };

		int	id	= 8;		// this id picks up where the transition textfield - "textfield7" - left off.

		for(int i = 0; i < modulateYVals.length; i++)
		{
			// - Textlabel:
			this.sidebarCP5.addLabel(names[i])
			.setPosition(labelX, modulateYVals[i] + 4)
			.setWidth(labelWidth)
			.setValue(values[i]);

			//	- Slider:
			this.sidebarCP5.addSlider("slider" + id)
			.setPosition(this.leftAlign, modulateYVals[i])
			.setSize(sliderWidth, sliderHeight)
			.setSliderMode(Slider.FLEXIBLE)
			.setValue(10)
			.setLabelVisible(false)
			.setId(id);

			id	= id + 1;

			//	- Textlabel:
			this.sidebarCP5.addTextfield("textfield" + id)
			.setPosition(this.leftAlign + sliderWidth + spacer, modulateYVals[i])
			.setSize(tfWidth, sliderHeight)
			.setText(this.sidebarCP5.getController("slider" + (id-1)).getValue() + "")
			.setLabelVisible(false)
			.setAutoClear(false)
			.setId(id);

			id	= id + 1;
		} // for
	} // addModulateSliders


	public void update()
	{
		this.thresholdTF.setValue(this.threshold.getValue());
		this.thresholdTF.setText(this.threshold.getValue() + "");
		System.out.println("this.threshold.getValue() = " + this.threshold.getValue() + 
				"this.threshold.getValuePosition = " + this.threshold.getValuePosition());
	} // update

	private void updateColors(float colorStyle)
	{
		if(colorStyle < 1 || colorStyle > 4) {
			throw new IllegalArgumentException("Module_01_02.updateColors: char paramter " + colorStyle + " is not recognized; must be 1 - 4.");
		}

		this.curColorStyle	= colorStyle;

		// Rainbow:
		if(this.curColorStyle == ModuleTemplate.CS_RAINBOW)
		{
			this.rainbow();
		}

		// Dichromatic:
		if(this.curColorStyle == ModuleTemplate.CS_DICHROM)
		{
			this.dichromatic_OneRGB(this.rootColor);
		}

		// Trichromatic:
		if(this.curColorStyle == ModuleTemplate.CS_TRICHROM)
		{
			this.trichromatic_OneRGB(this.rootColor);
		}

		// Custom:
		if(this.curColorStyle == ModuleTemplate.CS_CUSTOM)
		{			
			// First, set the key to chromatic:
			this.setCurKey("A", 2);
			
			// Then populate the textfields with the current colors in the colors array:
			// (textfield id's start at 23 and go up by 3)
			int	id	= 26;
			int	notePos	= 0;
			Textfield	curTextfield;
			
			for(int colorPos = 0; colorPos < this.colors.length; colorPos++)
			{
				curTextfield 	= (Textfield)this.sidebarCP5.getController("textfield" + id);
				System.out.println("curextfield = " + curTextfield);
				curTextfield.setText("rgb(" + this.colors[colorPos][0] + ", " + this.colors[colorPos][1] + ", " + this.colors[colorPos][2] + ")");
				id	= id + 3;
			} // for - colorPos
			
			// (The functionality in controlEvent will check for custom, and if it is custom, they will set their position of colors to their internal color.)
			// (Will they need to check to make sure that the key is actually chromatic?)
		} // custom colorStyle
	} // updateColors

	public void legend(int goalHuePos)
	{

		this.parent.textSize(24);
		/*
		String[] notes = new String[] {
				"C", 
				"C#", 
				"D", 
				"D#", 
				"E", 
				"F", 
				"F#", 
				"G", 
				"G#", 
				"A", 
				"A#", 
				"B", 
				"C"
		}; // notes
		 */
		String[]	notes	= this.getScale(this.curKey, this.majMinChrom);
		// 12/19: updating to be on the side.
		// 01/05: changing it back!
		float  sideWidth   = (this.parent.width - leftEdgeX) / notes.length;
		float  sideHeight  = this.parent.width / notes.length;
		//  float  side = height / colors.length;

		//	stroke(255);
		this.parent.noStroke();

		for (int i = 0; i < notes.length; i++)
		{
			this.parent.fill(this.getColors()[i][0], this.getColors()[i][1], this.getColors()[i][2]);
			/*
			if(i == 0)
			{
				for(int j = 0; j < this.colors[i].length; j++)
				{
					println("legend: colors[0][" + j + "] = " + colors[0][j]);
				}
			}
			 */
			if (i == goalHuePos) {
				this.parent.rect(leftEdgeX + (sideWidth * i), (float)(this.parent.height - (sideHeight * 1.5)), sideWidth, (float) (sideHeight * 1.5));
				//      rect(0, (side * i), side * 1.5, side);
			} else {
				this.parent.rect(leftEdgeX + (sideWidth * i), this.parent.height - sideHeight, sideWidth, sideHeight);
				//      rect(0, (side * i), side, side);
			}
			this.parent.fill(0);
			this.parent.text(notes[i], (float) (leftEdgeX + (sideWidth * i) + (sideWidth * 0.35)), this.parent.height - 20);
		} // for
	} // legend

	void displaySidebar()
	{
		this.sidebarCP5.setVisible(true);
		this.sidebarCP5.setVisible(true);
		this.setLeftEdgeX(this.parent.width / 3);
		this.leftAlign	= (this.getLeftEdgeX() / 4);

		this.parent.stroke(255);
		this.parent.fill(0);
		this.parent.rect(0, 0, getLeftEdgeX(), this.parent.height);
		/*
		int textX  		= 5;
		int	noteNameX1	= 40;
		int	noteNameX2 	= noteNameX1 + 135;

		String[]	textArray	= new String[] {
				"",
				"",
				"",
				"",
				"",
				"",
				"",
				"",
				""				
		}; // textArray


		String[]	noteNames1	= new String[] {
				"", "", "", "", "", ""
		}; // noteNames
		String[]	noteNames2	= new String[] {
				"D#/Eb", "E", "F", "F#/Gb", "G", "G#/Ab"
		}; // noteNames2

		String[]	modulateText	= new String[] {
				"", "", ""
		}; // modulateText
		 */


		/*
		this.parent.fill(255);
		this.parent.textSize(12);
		this.parent.text(this.sidebarTitle, this.leftAlign, 17);
		this.parent.textSize(10);
		this.parent.text("Menu", this.menuX.getPosition()[0] + this.menuX.getWidth() + 3, 16);
		 */	
		/*

		for(int i = 0; i < textArray.length; i++)
		{
			this.parent.text(textArray[i], textX, textYVals[i]);
		}
		for(int i = 0; i < noteNames1.length; i++)
		{
			this.parent.text(noteNames1[i], noteNameX1, noteYVals[i]);
		}
		for(int i = 0; i < noteNames2.length; i++)
		{
			this.parent.text(noteNames2[i], noteNameX2, noteYVals[i]);
		}
		 */
		/*
		for(int i = 0; i < modulateText.length; i++)
		{
			this.parent.text(modulateText[i], textX, modulateYVals[i]);
		}
		for(int i = 0; i < scrollbarArray.length; i++)
		{
			scrollbarArray[i].update();
			scrollbarArray[i].display();
		} // for - update and display first set of scrollbars

		for(int i = 0; i < this.modulateScrollbarArray.length; i++)
		{
			modulateScrollbarArray[i].update();
			modulateScrollbarArray[i].display();
		} // for - update and display modulate color scrollbars
		 */
	} // displaySidebar

	public String[] getScale(String key, int majMinChrom)
	{
		String[][] allNotes	= new String[][] {
			new String[] { "A" , "A" }, 
			new String[] { "A#", "Bb" }, 
			new String[] { "B" , "Cb" },
			new String[] { "C" , "C" },
			new String[] { "C#", "Db" }, 
			new String[] { "D" , "D" }, 
			new String[] { "D#", "Eb" }, 
			new String[] { "E" , "E" }, 
			new String[] { "F" , "F"}, 
			new String[] { "F#", "Gb" }, 
			new String[] { "G" , "G" }, 
			new String[] { "G#", "Ab" }
		};
		int[]	majorScale	= new int[] {
				2, 2, 1, 2, 2, 2, 1
		};
		int[]	minorScale	= new int[] {
				2, 1, 2, 2, 1, 2, 2
		};
		int[]	chromaticScale	= new int[] {
				1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1
		};
		int[][]	allScales	= new int[][] {
			majorScale,
			minorScale,
			chromaticScale
		};

		// find starting position in allNotes:
		boolean	flag	= false;
		int[]		startHere	= new int[2];
		for(int i = 0; i < allNotes.length; i++)
		{
			for(int j = 0; j < allNotes[i].length; j++)
			{
				if(allNotes[i][j] == key && flag == false)
				{
					startHere[0]	= i;
					startHere[1]	= j;
					flag	= true;
				} // if
			} // for - j
		} // for - i
		if(flag == false) 
		{
			throw new IllegalArgumentException("Module_01_02_PHB_ModuleTemplate.getScale: key " + key + " is not a valid key.");
		} // if - throw exception if key not found

		// Determine whether the key should use sharps or flats when choosing from enharmonic notes:
		// Be sure to include the space when doing indexOf - to avoid false positives with keys that are sharped
		String	sharps;
		String	flats;
		if(majMinChrom == 0 || majMinChrom == 2)
		{
			sharps	= "C G D A E B ";
			flats	= "F Bb Eb Ab Db Gb ";
		} else {
			sharps	= "A E B F# C# G# D#";
			flats	= "D G C F Bb Eb ";
		}
		String[]	sharpsAndFlats	= new String[] { sharps, flats };

		int	sharpOrFlat	= -1;
		for(int i = 0; i < sharpsAndFlats.length; i++)
		{
			if(sharpsAndFlats[i].indexOf(key + " ") > -1)
			{
				sharpOrFlat	= i;
			}
		}
		if(sharpOrFlat == -1) {
			throw new IllegalArgumentException("Module_01_02.getScale: key " + key + " is not supported at this time. Sorry! Try an enharmonic equivalent.");
		}

		String[]	result	= new String[allScales[majMinChrom].length];
		int	scalePos	= startHere[0];
		// i is position in result;
		// scalePos is position in allNotes
		for(int i = 0; i < result.length; i++)
		{
			result[i]	= allNotes[scalePos][sharpOrFlat];
			scalePos	= (scalePos + allScales[majMinChrom][i]) % allNotes.length;
		}

		this.scaleLength	= result.length;
		this.majMinChrom	= majMinChrom;

		return result;
	} // getScale

	public void setCurKey(String key, int majMinChrom)
	{
		// Check both sharps and flats, and take whichever one doesn't return -1:
		int	modPosition	= Math.max(this.arrayContains(this.notesCtoBFlats, key), this.arrayContains(this.notesCtoBSharps, key));

		if(modPosition == -1)	{
			throw new IllegalArgumentException("Module_01_02.setCurKey: " + key + " is not a valid key.");
		}

		this.majMinChrom	= majMinChrom;
		this.curKey			= key;
		this.setKeyAddVal(modPosition);
	} // setCurKey

	/**
	 * Used in draw for determining whether a particular scale degree is in the 
	 * major or minor scale;
	 * returns the position of the element if it exists in the array,
	 * or -1 if the element is not in the array.
	 * 
	 * @param array		String[] to be searched for the given element
	 * @param element	String whose position in the given array is to be returned.
	 * @return		position of the given element in the given array, or -1 
	 * 				if the element does not exist in the array.
	 */
	private int arrayContains(String[] array, String element) {
		if(array == null) {
			throw new IllegalArgumentException("Module_01_02.arrayContains(String[], String): array parameter is null.");
		}
		if(element == null) {
			throw new IllegalArgumentException("Module_01_02.arrayContains(String[], String): String parameter is null.");
		}

		for (int i = 0; i < array.length; i++)
		{
			//    println("array[i] = " + array[i]);
			if (array[i] == element) {
				return i;
			} // if
		} // for

		return -1;
	}

	/**
	 * Converts the given color to HSB and sends it to dichromatic_OneHSB.
	 * (dichromatic_OneHSB will send it to _TwoHSB, which will set this.colors, changing the scale.)
	 * 
	 * @param rgbVals	float[] of RGB values defining the color for the root of the scale.
	 */
	public void dichromatic_OneRGB(int[] rgbVals)
	{
		if(rgbVals == null) {
			throw new IllegalArgumentException("Module_01_02.dichromatic_OneRGB: int[] parameter is null.");
		}

		float[]	hsbVals	= new float[3];
		Color.RGBtoHSB(rgbVals[0], rgbVals[1], rgbVals[2], hsbVals);

		this.dichromatic_OneHSB(hsbVals);
	} // dichromatic_OneRGB

	/**
	 * Uses the given HSB color to find the color across it on the HSB wheel,
	 * converts both colors to RGB, and passes them as parameters to dichromatic_TwoRGB.
	 * 
	 * @param hue	float[] of HSB values defining the color at the root of the current scale.
	 */
	private void dichromatic_OneHSB(float[] hsbVals)
	{
		if(hsbVals == null) {
			throw new IllegalArgumentException("Module_01_02.dichromatic_OneHSB: float[] parameter hsbVals is null.");
		} // error checking

		// find the complement:
		float[]	hsbComplement	= new float[] { (float) ((hsbVals[0] + 0.5) % 1), 1, 1 };

		// convert them both to RGB;
		float[]	rgbVals1	= new float[3];
		float[]	rgbVals2	= new float[3];

		int	rgb1	= Color.HSBtoRGB(hsbVals[0], hsbVals[1], hsbVals[2]);
		Color	rgbColor1	=  new Color(rgb1);

		// Using individual get[Color]() functions b/c getComponents() uses a 0-1 range.
		rgbVals1[0]	= rgbColor1.getRed();
		rgbVals1[1]	= rgbColor1.getGreen();
		rgbVals1[2]	= rgbColor1.getBlue();	

		int	rgb2	= Color.HSBtoRGB(hsbComplement[0], hsbComplement[1], hsbComplement[2]);
		Color	rgbColor2	=  new Color(rgb2);

		// Using individual get[Color]() functions b/c getComponents() uses a 0-1 range.
		rgbVals2[0]	= rgbColor2.getRed();
		rgbVals2[1]	= rgbColor2.getGreen();
		rgbVals2[2]	= rgbColor2.getBlue();	

		this.dichromatic_TwoRGB(rgbVals1, rgbVals2);
	} // dichromatic_OneHSB(int)

	/**
	 * This method fills colors with the spectrum of colors between the given rgb colors.
	 * 
	 * @param rgbVals1	float[] of rgb values defining rootColor.
	 * @param rgbVals2	float[] of rgb values defining the color of the last note of the scale.
	 */
	public void dichromatic_TwoRGB(float[] rgbVals1, float[] rgbVals2)
	{
		if(rgbVals1 == null || rgbVals2 == null) {
			throw new IllegalArgumentException("Module_01_02.dichromatic_TwoRGB: at least one of the float[] parameters is null.");
		} // error checking

		float	redDelta	= (rgbVals1[0] - rgbVals2[0]) / this.scaleLength;
		float	greenDelta	= (rgbVals1[1] - rgbVals2[1]) / this.scaleLength;
		float	blueDelta	= (rgbVals1[2] - rgbVals2[2]) / this.scaleLength;

		for(int i = 0; i < rgbVals1.length; i++)
		{
			this.getColors()[0][i]	= rgbVals1[i];
		}
		for(int i = 1; i < this.scaleLength && i < this.getColors().length; i++)
		{
			for(int j = 0; j < this.getColors()[i].length; j++)
			{
				this.getColors()[i][0]	= this.getColors()[i - 1][0] - redDelta;
				this.getColors()[i][1]	= this.getColors()[i - 1][1] - greenDelta;
				this.getColors()[i][2]	= this.getColors()[i - 1][2] - blueDelta;
			} // for - j
		} // for - i
	} // dichromatic_TwoRGB

	/**
	 * Converts the given color to HSB and sends it to dichromatic_OneHSB.
	 * (dichromatic_OneHSB will send it to _TwoHSB, which will set this.colors, changing the scale.)
	 * 
	 * @param rgbVals	float[] of RGB values defining the color for the root of the scale.
	 */
	public void trichromatic_OneRGB(int[] rgbVals)
	{
		if(rgbVals == null) {
			throw new IllegalArgumentException("Module_01_02.trichromatic_OneRGB: int[] parameter is null.");
		}

		float[]	hsbVals	= new float[3];
		Color.RGBtoHSB(rgbVals[0], rgbVals[1], rgbVals[2], hsbVals);

		this.trichromatic_OneHSB(hsbVals);
	} // trichromatic_OneRGB

	/**
	 * ** This method should not be called w/out setting rootColor before hand.
	 * 
	 * Uses the given HSB color to find the color across it on the HSB wheel,
	 * converts both colors to RGB, and passes them as parameters to dichromatic_TwoRGB.
	 *
	 * @param hsbVals	float[] of HSB values defining the color at the root of the current scale.
	 */
	private void trichromatic_OneHSB(float[] hsbVals)
	{
		if(hsbVals == null) {
			throw new IllegalArgumentException("Module_01_02.dichromatic_OneHSB: float[] parameter hsbVals is null.");
		} // error checking

		// find the triad:
		float[]	hsbTriad1	= new float[] { (float) ((hsbVals[0] + 0.33) % 1), 1, 1 };
		float[]	hsbTriad2	= new float[] { (float) ((hsbVals[0] + 0.67) % 1), 1, 1 };

		// convert them both to RGB;
		float[]	rgbVals1	= new float[3];
		float[]	rgbVals2	= new float[3];
		float[]	rgbVals3	= new float[3];

		int	rgb1	= Color.HSBtoRGB(hsbVals[0], hsbVals[1], hsbVals[2]);
		Color	rgbColor1	=  new Color(rgb1);

		// Using individual get[Color]() functions b/c getComponents() uses a 0-1 range.
		rgbVals1[0]	= rgbColor1.getRed();
		rgbVals1[1]	= rgbColor1.getGreen();
		rgbVals1[2]	= rgbColor1.getBlue();	

		int	rgb2	= Color.HSBtoRGB(hsbTriad1[0], hsbTriad1[1], hsbTriad1[2]);
		Color	rgbColor2	=  new Color(rgb2);

		// Using individual get[Color]() functions b/c getComponents() uses a 0-1 range.
		rgbVals2[0]	= rgbColor2.getRed();
		rgbVals2[1]	= rgbColor2.getGreen();
		rgbVals2[2]	= rgbColor2.getBlue();	

		int	rgb3	= Color.HSBtoRGB(hsbTriad2[0], hsbTriad2[1], hsbTriad2[2]);
		Color	rgbColor3	=  new Color(rgb3);

		// Using individual get[Color]() functions b/c getComponents() uses a 0-1 range.
		rgbVals3[0]	= rgbColor3.getRed();
		rgbVals3[1]	= rgbColor3.getGreen();
		rgbVals3[2]	= rgbColor3.getBlue();	

		this.trichromatic_ThreeRGB(rgbVals1, rgbVals2, rgbVals3);
	} // trichromatic_OneHSB

	public void trichromatic_ThreeRGB(float[] rgbVals1, float[] rgbVals2, float[] rgbVals3)
	{
		if(rgbVals1 == null || rgbVals2 == null || rgbVals3 == null) {
			throw new IllegalArgumentException("Module_01_02.trichromatic_ThreeRGB: at least one of the float[] parameters is null.");
		} // error checking

		int	color1pos	= 0;
		int	color2pos;
		int	color3pos;

		if(this.majMinChrom == 2)
		{
			// if chromatic scale, put the colors equally throughout:
			color2pos	= this.scaleLength / 3;
			color3pos	= (this.scaleLength / 3) * 2;
		} else {
			color2pos	= 3;	// subdominant
			color3pos	= 4;	// dominant
		}

		// TODO: this might need to be divided by 4 to make it to the actual color (or dichr. should be colors.length - 1?):
		float	redDelta1	= (rgbVals1[0] - rgbVals2[0]) / (color2pos - color1pos);
		float	greenDelta1	= (rgbVals1[1] - rgbVals2[1]) / (color2pos - color1pos);
		float	blueDelta1	= (rgbVals1[2] - rgbVals2[2]) / (color2pos - color1pos);

		float	redDelta2	= (rgbVals2[0] - rgbVals3[0]) / (color3pos - color2pos);
		float	greenDelta2	= (rgbVals2[1] - rgbVals3[1]) / (color3pos - color2pos);
		float	blueDelta2	= (rgbVals2[2] - rgbVals3[2]) / (color3pos - color2pos);

		float	redDelta3	= (rgbVals3[0] - rgbVals1[0]) / (this.scaleLength - color3pos);
		float	greenDelta3	= (rgbVals3[1] - rgbVals1[1]) / (this.scaleLength - color3pos);
		float	blueDelta3	= (rgbVals3[2] - rgbVals1[2]) / (this.scaleLength - color3pos);

		// fill first position with first color:
		for(int i = 0; i < rgbVals1.length; i++)
		{
			this.getColors()[0][i]	= rgbVals1[i];
		}

		// fill from first color to second color:
		for(int i = 1; i < color2pos + 1; i++)
		{
			for(int j = 0; j < this.getColors()[i].length; j++)
			{
				this.getColors()[i][0]	= this.getColors()[i - 1][0] - redDelta1;
				this.getColors()[i][1]	= this.getColors()[i - 1][1] - greenDelta1;
				this.getColors()[i][2]	= this.getColors()[i - 1][2] - blueDelta1;
			} // for - j
		} // for - first color to second color


		// fill from second color to third color:
		for(int i = color2pos + 1; i < color3pos + 1; i++)
		{
			for(int j = 0; j < this.getColors()[i].length; j++)
			{
				this.getColors()[i][0]	= this.getColors()[i - 1][0] - redDelta2;
				this.getColors()[i][1]	= this.getColors()[i - 1][1] - greenDelta2;
				this.getColors()[i][2]	= this.getColors()[i - 1][2] - blueDelta2;
			} // for - j
		} // for - first color to second color

		// fill from third color back to first color:
		for(int i = color3pos + 1; i < this.scaleLength; i++)
		{
			for(int j = 0; j < this.getColors()[i].length; j++)
			{
				this.getColors()[i][0]	= this.getColors()[i - 1][0] - redDelta3;
				this.getColors()[i][1]	= this.getColors()[i - 1][1] - greenDelta3;
				this.getColors()[i][2]	= this.getColors()[i - 1][2] - blueDelta3;
			} // for - j
		} // for - third color to first color
	} //trichromatic_ThreeRGB

	/**
	 * Populates colors with rainbow colors (ROYGBIV - with a few more for chromatic scales).
	 */
	public void rainbow()
	{
		float[][][] rainbowColors = rainbowColors	= new float[][][] { 
			new float[][] {
				{ 255, 0, 0 }, 
				{ 255, (float) 127.5, 0 }, 
				{ 255, 255, 0 }, 
				{ (float) 127.5, 255, 0 },
				{ 0, 255, 255 },  
				{ 0, 0, 255 },
				{ (float) 127.5, 0, 255 }
			}, // major
			new float[][] {
				{ 255, 0, 0 }, 
				{ 255, (float) 127.5, 0 }, 
				{ 255, 255, 0 }, 
				{ (float) 127.5, 255, 0 },
				{ 0, 255, 255 },  
				{ 0, 0, 255 },
				{ (float) 127.5, 0, 255 }
			}, // minor
			new float[][] {
				{ 255, 0, 0 }, 
				{ 255, (float) 127.5, 0 }, 
				{ 255, 255, 0 }, 
				{ (float) 127.5, 255, 0 }, 
				{ 0, 255, 0 }, 
				{ 0, 255, (float) 127.5 }, 
				{ 0, 255, 255 }, 
				{ 0, (float) 127.5, 255 }, 
				{ 0, 0, 255 }, 
				{ (float) 127.5, 0, 255 }, 
				{ 255, 0, 255 }, 
				{ 255, 0, (float) 127.5 }
			} // chromatic
		}; // rainbowColors

		for(int i = 0; i < this.getColors().length && i < rainbowColors[this.majMinChrom].length; i++)
		{
			for(int j = 0; j < this.getColors()[i].length && j < rainbowColors[this.majMinChrom][i].length; j++)
			{
				this.getColors()[i][j]	= rainbowColors[this.majMinChrom][i][j];
			} // for - j (going through rgb values)
		} // for - i (going through colors)

	} // rainbow

	/**
	 * This method handles the functionality of all the buttons, sliders, and textFields;
	 * Notate bene: any classes that include a moduleTemplate *must* include a controlEvent(ControlEvent) method that calls this method.
	 * 
	 * @param theControlEvent	ControlEvent used to determine which controller needs to act.
	 */
	public void controlEvent(ControlEvent controlEvent)
	{
		System.out.println("ModuleTemplate: theControlEvent.getController() = " + controlEvent.getController());

		int	id	= controlEvent.getController().getId();
		// Play button:
		if(controlEvent.getController().getName().equals("play"))
		{
			for (int i = 0; i < input.getuGenArray().length; i++)
			{
				input.getuGenArray()[i].pause(true);
			} // for

			if(this.play.getBooleanValue())
			{
				this.input.uGenArrayFromSample(this.inputFile);
			} else {
				this.input.uGenArrayFromNumInputs(1);
			}
		} // if - play

		// Hamburger button:
		if(controlEvent.getController().getName().equals("hamburger"))
		{
			this.displaySidebar();
			this.hamburger.setVisible(false);
		} // if - hamburger

		// MenuX button:
		if(controlEvent.getController().getName().equals("menuX"))
		{
			this.setLeftEdgeX(0);
			this.sidebarCP5.setVisible(false);
			this.sidebarCP5.setVisible(false);
			this.hamburger.setVisible(!this.sidebarCP5.getController("menuButton").isActive());
		} // if - menuX

		// Hide play button button:
		if(controlEvent.getController().getId() == this.hidePlayButton.getId())
		{
			this.play.setVisible(!this.play.isVisible());
		} // if - hidePlayButton

		// Hide menu button button:
		if(controlEvent.getController().getId() == this.hideMenuButton.getId())
		{
			this.hamburger.setVisible(!this.hamburger.isVisible());
		} // if - hidePlayButton

		// Hide scale:
		if(controlEvent.getName().equals("scale"))
		{
			this.setShowScale(((Toggle) (controlEvent.getController())).getState());
		}

		//TODO: set this cutoff in a more relevant place - perhaps when sliders are created?
		// (If I have a numSliders, it would be (numSliders * 2).
		int	sliderCutoff	= 14;

		// Sliders (sliders have odd id num and corresponding textfields have the next odd number)
		if(id % 2 == 0 && id < sliderCutoff)
		{
			Slider	curSlider	= (Slider)this.sidebarCP5.getController("slider" + id);
			Textfield	curTextfield	= (Textfield)this.sidebarCP5.getController("textfield" + (id + 1));
			String	sliderValString	= this.decimalFormat.format(curSlider.getValue());

			curTextfield.setText(sliderValString);

			float	sliderValFloat	= Float.parseFloat(sliderValString);

			// Threshold:
			if(id == 0)
			{
				this.setThresholdLevel(sliderValFloat);
			}

			// Attack, Release, and Transition:
			if(id == 2 || id == 4 || id == 6)
			{
				int	pos	= (id / 2) - 1;
				this.attackReleaseTransition[pos]	= sliderValFloat;
			}
		}

		// Textfields
		if(id % 2 == 1 && id < sliderCutoff && id > 0)
		{
			Textfield	curTextfield	= (Textfield)this.sidebarCP5.getController("textfield" + id);
			Slider		curSlider		= (Slider)this.sidebarCP5.getController("slider" + (id - 1));

			try	{
				curSlider.setValue(Float.parseFloat(curTextfield.getStringValue()));
			} catch(NumberFormatException nfe) {
				System.out.println("ModuleTemplate.controlEvent: string value " + curTextfield.getStringValue() + 
						"for controller " + curTextfield + " cannot be parsed to a float.  Please enter a number.");
			} // catch
		} // textField

		// Key dropdown ScrollableList:
		if(controlEvent.getName().equals("keyDropdown"))
		{
			controlEvent.getController().bringToFront();
			
			// keyPos is the position of the particular key in the Scrollable List:
			int	keyPos	= (int)controlEvent.getValue();

			// getItem returns a Map of the color, state, value, name, etc. of that particular item
			//  in the ScrollableList:
			Map<String, Object> keyMap = this.sidebarCP5.get(ScrollableList.class, "keyDropdown").getItem(keyPos);
			// All we want is the name:
			String	key	= (String) keyMap.get("name");

			this.setCurKey(key, this.majMinChrom);
			this.displaySidebar();
		} // keyDropdown

		// Major/Minor/Chromatic buttons
		if(controlEvent.getName().equals("major") ||
				controlEvent.getName().equals("minor") ||
				controlEvent.getName().equals("chrom"))
		{
			Toggle	curToggle	= (Toggle) controlEvent.getController();
			System.out.println("Maj/Min/Chrom: internalValue() = " + curToggle.internalValue());
			this.setCurKey(this.curKey, (int) curToggle.internalValue());

			// Turn off the other two:
			Toggle[] toggleArray	= new Toggle[] {
					(Toggle)this.sidebarCP5.getController("major"),
					(Toggle)this.sidebarCP5.getController("minor"),
					(Toggle)this.sidebarCP5.getController("chrom"),
			};
			boolean[]	broadcastState	= new boolean[toggleArray.length];
			for(int i = 0; i < toggleArray.length; i++)
			{
				// save the current broadcast state of the controller:
				broadcastState[i]	= toggleArray[i].isBroadcast();

				// turn off broadcasting to avoid endless looping in this method:
				toggleArray[i].setBroadcast(false);

				// only switch off the ones that weren't just clicked:
				if(!controlEvent.getController().getName().equals(toggleArray[i].getName()))
				{
					toggleArray[i].setState(false);
				}

				// set broadcasting back to original setting:
				toggleArray[i].setBroadcast(broadcastState[i]);
			} // for - switch off all Toggles:

			this.updateColors(this.curColorStyle);

		} // majMinChrom buttons
		

		// Root color selector button:
		if(controlEvent.getName().equals("rootColorButton"))
		{
			Button	rootButton	= (Button)controlEvent.getController();
			// draw slightly transparent rectangle:
			if(rootButton.getBooleanValue())
			{
				// Want to turn off automatic drawing so that our transparent rectangle can go on top of the controllers.
//				this.sidebarCP5.setAutoDraw(false);

				// Draw all the controllers:
//				this.sidebarCP5.draw();

				// Then cover with a rectangle (black, w/alpha of 50):
				this.sidebarCP5.getGroup("background").setVisible(true);
				this.sidebarCP5.getGroup("background").bringToFront();

				this.sidebarCP5.getController("rootColorButton").bringToFront();
				this.sidebarCP5.getController("rootColorWheel").bringToFront();
				this.sidebarCP5.getController("rootColorTF").bringToFront();
				/*
				this.parent.fill(0, 150);
				this.parent.rect(0, 0, getLeftEdgeX(), this.parent.height);
				*/
			} else {
				this.sidebarCP5.getGroup("background").setVisible(false);
				this.displaySidebar();
			}

			this.sidebarCP5.getController("rootColorWheel").setVisible(rootButton.getBooleanValue());
			this.updateColors(this.curColorStyle);
		} // Root color selector button (i.e., show color wheel)

		// Root Color Wheel
		if(controlEvent.getName().equals("rootColorWheel"))
		{
			ColorWheel	rootCW	= (ColorWheel)controlEvent.getController();
			int	rgbColor	= rootCW.getRGB();
			Color	color	= new Color(rgbColor);

			Textfield	rootColorTF	= (Textfield)this.sidebarCP5.getController("rootColorTF");
			rootColorTF.setText("rgb(" + color.getRed() + ", " + color.getGreen() + ", " + color.getBlue() + ")");

			this.rootColor[0]	= color.getRed();
			this.rootColor[1]	= color.getGreen();
			this.rootColor[2]	= color.getBlue();

			this.colors[0][0]	= color.getRed();
			this.colors[0][1]	= color.getGreen();
			this.colors[0][2]	= color.getBlue();

			this.updateColors(this.curColorStyle);
		} // root color wheel


		// Color Selection: 
		// Buttons, ColorWheels and corresponding Textfields will have id's of 21 or over;
		// Button id % 3 == 0; ColorWheel id % 3 == 1, Textfield id % 3 == 2.

		// Custom pitch color selector buttons:
		if(controlEvent.getId() > 23 && (controlEvent.getId() % 3 == 0))
		{
			Button	curButton	= (Button)controlEvent.getController();

			// draw slightly transparent rectangle:
			if(curButton.getBooleanValue())
			{
				// Want to turn off automatic drawing so that our transparent rectangle can go on top of the controllers.
//				this.sidebarCP5.setAutoDraw(false);
//				this.sidebarCP5.setAutoDraw(false);

				// Draw all the controllers:
//				this.sidebarCP5.draw();
//				this.sidebarCP5.draw();

				// Then cover with a rectangle (black, w/alpha of 50):
//				this.parent.rect(0, 0, getLeftEdgeX(), this.parent.height);
				System.out.println("this.sidebarCP5 = " + this.sidebarCP5);
				this.sidebarCP5.getGroup("background").setVisible(true);
				this.sidebarCP5.getGroup("background").bringToFront();
//				this.sidebarCP5.getController("background").setVisible(true);
	/*			
				this.sidebarCP5.getController("button" + controlEvent.getId()).draw(this.parent.g);
				this.sidebarCP5.getController("button" + controlEvent.getId()).setVisible(true);
				this.sidebarCP5.getController("colorWheel" + (controlEvent.getId() + 1)).draw(this.parent.g);
				this.sidebarCP5.getController("textfield" + (controlEvent.getId() + 2)).draw(this.parent.g);
				*/
				
//				this.sidebarCP5.getController("colorWheel" + (controlEvent.getId() + 1)).setVisible(curButton.getBooleanValue());
//				this.sidebarCP5.getController("colorWheel" + (controlEvent.getId() + 1)).bringToFront();
			} else {
				this.sidebarCP5.setAutoDraw(true);
				this.sidebarCP5.getGroup("background").setVisible(false);
				this.displaySidebar();
			}
			/*
			while(((Button)(this.sidebarCP5.getController("button" + controlEvent.getId()))).getBooleanValue())
			{
				this.sidebarCP5.getController("button" + controlEvent.getId()).draw(this.parent.g);
				this.sidebarCP5.getController("colorWheel" + (controlEvent.getId() + 1)).draw(this.parent.g);
				this.sidebarCP5.getController("textfield" + (controlEvent.getId() + 2)).draw(this.parent.g);
				System.out.println("Still in the while...");
			}*/
			
			this.sidebarCP5.getController("button" + (controlEvent.getId())).bringToFront();
			this.sidebarCP5.getController("colorWheel" + (controlEvent.getId() + 1)).bringToFront();
			this.sidebarCP5.getController("textfield" + (controlEvent.getId() + 2)).bringToFront();

			this.sidebarCP5.getController("colorWheel" + (controlEvent.getId() + 1)).setVisible(curButton.getBooleanValue());
			this.updateColors(this.curColorStyle);
		} // custom pitch color selectors (i.e., show color wheel)

		// Custom pitch ColorWheels
		if(controlEvent.getId() > 23 && (controlEvent.getId() % 3 == 1))
		{
			ColorWheel	rootCW	= (ColorWheel)controlEvent.getController();
			int	rgbColor	= rootCW.getRGB();
			Color	color	= new Color(rgbColor);

			Textfield	rootColorTF	= (Textfield)this.sidebarCP5.getController("textfield" + (controlEvent.getId() + 1));
			rootColorTF.setText("rgb(" + color.getRed() + ", " + color.getGreen() + ", " + color.getBlue() + ")");

			id	= controlEvent.getId();
			int	notePos	= ((id + 2) / 3) - 9;
//			notePos	= (notePos + keyAddVal - 3 + this.colors.length) % this.colors.length;

			if(this.sidebarCP5.getController("custom").getValue() == 1)
			{
				this.colors[notePos][0]	= color.getRed();
				this.colors[notePos][1]	= color.getGreen();
				this.colors[notePos][2]	= color.getBlue();
			}

			this.updateColors(this.curColorStyle);
			
			
			System.out.println(controlEvent.getController() + ": notePos = " + notePos);
		} // custom pitch color wheels

		// ColorWheel Textfields (id 20 or over; id % 3 == 2):
		if(controlEvent.getId() > 20 && (controlEvent.getId() % 3 == 2))
		{
			id	= controlEvent.getId();
			int	notePos	= ((id + 2) / 3) - 9;
			
			if(notePos > -1 && notePos < this.colors.length)
			{
				
			}
			
			System.out.println(controlEvent.getController() + ": notePos = " + notePos);
			
			System.out.println("ModuleTemplate.controlEvent: Entering your own color is not yet supported. Sorry! Please try again later.");
		} // ColorWheel Textfields


		// Color Style:
		if(controlEvent.getName().equals("rainbow") ||
				controlEvent.getName().equals("dichrom") ||
				controlEvent.getName().equals("trichrom") ||
				controlEvent.getName().equals("custom"))
		{
			Toggle	curToggle	= (Toggle) controlEvent.getController();
			// Set root color/call correct function for the new colorStyle:
			this.updateColors(curToggle.internalValue());

			// Turn off the other Toggles:
			Toggle[] toggleArray	= new Toggle[] {
					(Toggle)this.sidebarCP5.getController("rainbow"),
					(Toggle)this.sidebarCP5.getController("dichrom"),
					(Toggle)this.sidebarCP5.getController("trichrom"),
					(Toggle)this.sidebarCP5.getController("custom")
			};
			boolean[]	broadcastState	= new boolean[toggleArray.length];
			for(int i = 0; i < toggleArray.length; i++)
			{
				// save the current broadcast state of the controller:
				broadcastState[i]	= toggleArray[i].isBroadcast();

				// turn off broadcasting to avoid endless looping in this method:
				toggleArray[i].setBroadcast(false);

				// switch off the ones that weren't just clicked, but keep the current one on:
				if(!controlEvent.getController().getName().equals(toggleArray[i].getName()))
				{
					toggleArray[i].setState(false);
				} else {
					toggleArray[i].setState(true);
				}

				// set broadcasting back to original setting:
				toggleArray[i].setBroadcast(broadcastState[i]);
			} // for - switch off all Toggles:

		} // colorStyle buttons

	} // controlEvent

	public int getLeftEdgeX() {
		return leftEdgeX;
	}

	public void setLeftEdgeX(int leftEdgeX) {
		this.leftEdgeX = leftEdgeX;
	}

	public float[][] getColors() {
		return colors;
	}

	public void setColors(float[][] colors) {
		this.colors = colors;
	}

	public int getKeyAddVal() {
		return keyAddVal;
	}

	public void setKeyAddVal(int keyAddVal) {
		this.keyAddVal = keyAddVal;
	}

	public boolean isShowScale() {
		return showScale;
	}

	public void setShowScale(boolean showScale) {
		this.showScale = showScale;
	}

	public float getThresholdLevel() {
		return thresholdLevel;
	}

	public void setThresholdLevel(float thresholdLevel) {
		this.thresholdLevel = thresholdLevel;
	}

	public float getAttackTime() {
		return attackTime;
	}

	public void setAttackTime(float attackTime) {
		this.attackTime = attackTime;
	}

	public float getReleaseTime() {
		return releaseTime;
	}

	public void setReleaseTime(float releaseTime) {
		this.releaseTime = releaseTime;
	}

	public float getTransitionTime() {
		return transitionTime;
	}

	public void setTransitionTime(float transitionTime) {
		this.transitionTime = transitionTime;
	}

	public float getAttackReleaseTransition(int arORt)
	{
		if(arORt < 0 || arORt > this.attackReleaseTransition.length)
		{
			throw new IllegalArgumentException("ModuleTemplate.getAttackReleaseTransition: parameter " + arORt + " is not a valid position in the array this.attackReleaseTransition.");
		} // error checking

		return	this.attackReleaseTransition[arORt];
	} // getAttackReleaseTransition


	/*
	 * 01/11/2017 brainstorming:
	 * I'll have options for 
	 * 
	 * Do I want generic sliders?  I won't have a great way of accessing their results,
	 * but I'll also have more freedom in making ones.
	 * Otherwise, I can say the only ones you can use are the ones that I made.
	 * (Which makes sense, because then I don't need to set new range values each time,
	 * and maybe it can even  interact with Input -- do something when it crosses the threshold, etc.)
	 * 
	 * I don't want displaySidebar() to be a whole bunch of if's, though;
	 * Maybe: I can have an ArrayList of functions that are implemented for this particular instance,
	 * and I go through that array list and call the corresponding functions.
	 * Then I'll have another huge if() function that takes a number parameter and does the thing it's supposed to do.
	 *  ^ Not too bad, b/c it only counts toward text.  Other things are implementable once.
	 *  
	 *  Slight problem = how controlP5 deals w/events. I need a separate function for each button I might have,
	 *  but what if I make two of those buttons? (That doesn't make sense... Why would you have two hidePlay buttons?)
	 *  
	 *  Main problem: I really don't understand ControlP5 yet.
	 *  Solution:	I'll implement Module_01_02 w/ControlP5, not generically at all.
	 *  			Then, when I can see how it works, maybe I can make it generic.
	 *  			(Kind of what I was going to do, anyway; we'll take this one step at a time.)
	 */

}