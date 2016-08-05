package manager;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import entities.Larva;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.TextRoi;

/**
* The class manages the annotations on the image plus.
* @author  Yaoguang Zhong
* @version 1.1
* @since   08-02-2016
*/
public class AnnotationManager 
{
	// the information of this Larva will be annotated on the image
	private Larva larva = null;
	private ImagePlus imagePlus = null; // the image to be annotated text on

	// the point currently at which draw annotations
	private Point pointCurrent = null;
	private int yHeight = 20; // the height of a text row
	private Color color = null; // the color with which draw annotations
	private int fontSize = 18; // the font size

	private Font font = null;
	
	private int numRowError = 0;
	private Point ptError = null;
	
	/**
	* Manages the annotations on the image plus.
	* 
	* @param featuresOfLarva The list of frame containing the larva.
	* @param imagePlus The image plus annotated on.
	* @return None.
	*/
	public AnnotationManager(Larva featuresOfLarva, ImagePlus imagePlus) {
		this.larva = featuresOfLarva;
		this.imagePlus = imagePlus;
		this.pointCurrent = new Point(50, 50);
		this.color = Color.green;
		this.font = new Font("SansSerif", Font.PLAIN, fontSize);
		this.ptError = new Point(340,300);
	}

	/**
	* Manages the annotations on the image plus.
	* 
	* @param featuresOfLarva The list of frame containing the larva.
	* @param imagePlus The image plus annotated on.
	* @param pointStart The point start at.
	* @return None.
	*/
	public AnnotationManager(Larva featuresOfLarva, ImagePlus imagePlus, Point pointStart) {
		this(featuresOfLarva, imagePlus);
		this.pointCurrent = pointStart;
	}

	/**
	* Reset the error message pointer.
	* 
	* @return None.
	*/
	public void resetErrorMsgPos()
	{
		numRowError = 0;
	}
	
	/**
	* Annotate text on a image plus.
	* 
	* @param msg The message.
	* @param color The color.
	* @return None.
	*/
	public void annotateMessage(String msg, Color color) 
	{

		String[] splitArray = msg.split("\\s+");
		
		int numWordsInLine = 15;
		
		int heightText = 30;
		int yText = 0;
		String txt = "";
		Point pt = new Point(ptError.x, ptError.y + numRowError * heightText );
		
		int numRow = (int) Math.ceil( (double) splitArray.length / (double) numWordsInLine );

		for(int i = 0; i < numRow; i++)
		{
			yText = i * heightText;
			
			txt = "";
			for(int j = 0; j < numWordsInLine; j++)
			{
				int index = i * numWordsInLine + j;
			
				if(index < splitArray.length)
					
					txt += splitArray[index] + " ";
			
				TextRoi roi = new TextRoi(pt.x, pt.y + yText, txt, font);
	
				Overlay overlay = new Overlay();
				overlay.add(roi);
				
				imagePlus.getProcessor().setOverlay(overlay);
				imagePlus.getProcessor().setColor(color);
				roi.drawPixels(imagePlus.getProcessor());
			}
		}
		
		numRowError += numRow;
	}
	
	/**
	* Annotate all text on a image plus.
	* 
	* @param prop The properties.
	* @return None.
	*/
	public void annotateAll(PropertyManager prop) 
	{
		
		String strStimulus = "";
		if( PropertyManager.getBool( prop.getChrimson_stimulus() ) )
			strStimulus = "Chrimson";
		else
			strStimulus = "Blue Light";
		
		annotate("Stimulus: " + strStimulus + ", Frame: " + larva.getFrameId());
		
		annotate("Prev Valid: " + larva.getIsPreviousValid()+", Crnt Valid: " + larva.getIsValid());
		
		annotate("Curl: " + larva.getCurl() + ", BdAngle: " + larva.getAngleBody() );

		// if the featuresOfLarva has the previous frame
		if (larva.getLarvaPrevious() != null) 
		{
			annotate("Distance Moved: " + larva.getDistanceCenterPoint());
			
			String strDistSidewaysEndPts = "";
			
			// if the larva is invalid or the previous larva is invalid
			if(larva.getIsValid() && larva.getIsPreviousValid())
				strDistSidewaysEndPts = Double.toString(larva.getDistanceSidewaysEndPts());
			else
				strDistSidewaysEndPts = Double.toString(larva.getDistanceSidewaysEndPts()) + " (Avg)";
			
			annotate("Sideways Distance (End Pt): "+ strDistSidewaysEndPts);

			annotate("Sideways Distance Sign (End Pts):");

			annotate("    Signed: " + larva.getIsSidewaysEndPtsForward() + ", Directed: " + larva.getIsMoveRight() );
			annotate("    Signed: " + larva.getIsSidewaysEndPtsForward() + ",  isRight: " + larva.getIsDistSidewaysPos() );
			
			annotate("Center Point: (" + larva.getLarvaPrevious().getCenterPointOnFullFrame().x
					+ ", " + larva.getLarvaPrevious().getCenterPointOnFullFrame().y + "), ("
					+ larva.getCenterPointOnFullFrame().x + ", "
					+ larva.getCenterPointOnFullFrame().y + ")");
			annotate("End Point 1: (" + larva.getLarvaPrevious().getEndPointsOnFullFrame().get(0).x
					+ ", " + larva.getLarvaPrevious().getEndPointsOnFullFrame().get(0).y + "), ("
					+ larva.getEndPointsOnFullFrame().get(0).x + ", "
					+ larva.getEndPointsOnFullFrame().get(0).y + ")");
			annotate("End Point 2: (" + larva.getLarvaPrevious().getEndPointsOnFullFrame().get(1).x
					+ ", " + larva.getLarvaPrevious().getEndPointsOnFullFrame().get(1).y + "), ("
					+ larva.getEndPointsOnFullFrame().get(1).x + ", "
					+ larva.getEndPointsOnFullFrame().get(1).y + ")");

			annotate("Center Point Linear Line (Parallel):");
			annotate("       " + larva.getLinearLineParallel());
			annotate("Center Point Linear Line (Perpend.):");
			annotate("       " + larva.getLinearLinePerpendicular());
			
			annotate("End Points Linear Line (Parallel):");
			annotate("       " + larva.getLinearLineEndPtsParallel());
			annotate("End Points Linear Line (Perpend.):");
			annotate("       " + larva.getLinearLineEndPtsPerp());
			
		// if the featuresOfLarva doesn't have a previous frame
		}else
		{
			annotate("Total Distance : " + larva.getDistanceSidewaysTotal() );

			annotate("Center Point: (null, null), ("
					+ larva.getCenterPointOnFullFrame().x + ", "
					+ larva.getCenterPointOnFullFrame().y + ")");
			annotate("End Point 1: (null, null), ("
					+ larva.getEndPointsOnFullFrame().get(0).x + ", "
					+ larva.getEndPointsOnFullFrame().get(0).y + ")");
			annotate("End Point 2: (null, null), ("
					+ larva.getEndPointsOnFullFrame().get(1).x + ", "
					+ larva.getEndPointsOnFullFrame().get(1).y + ")");
		}
		
		annotate("Pixel Area : " + larva.getArea()+", Avg Area: "+larva.getAvgArea());
		annotate("Skel Len : " + larva.getLengthSkeleton()+", Avg Skel: "+larva.getAvgSkeletonLen());
		annotate("Linear & End pts Angle : " + larva.getAngleEndPointsLinear());
		annotate("Need Rec: " + larva.getNeedRecord()+", Rolling: " + larva.getIsRolling() );
				
		if(larva.getIsRolling())
			annotate(new Point(1140, 43), "Rolling...", Color.yellow);
		
		annotate("Diameter of Larva: " + larva.getDiameter() );
		annotate("Perimeter:" + larva.getPerimeter() + ", Avg Peri:"+larva.getAvgPerimeter());
		
		annotate("(Threshold) Num Frm:" + prop.getRolling_frame()
		+ ", Peri:"+prop.getLarva_perimeter());
		annotate("(Size) Max:" + prop.getMax_size() + ", Min:" + prop.getMin_size());
		annotate("(Skel) Max:" + prop.getMax_skeleton() + ", Min:" + prop.getMin_skeleton() );
		annotate("SgnBgn: " + larva.getDistSwySgnBgn()+", sgnEnd: " +larva.getDistSwySgnEnd() );
		annotate( "dirBgn: " + larva.getDistSwyDirBgn() +", dirEnd: " +larva.getDistSwyDirEnd());
		
		annotate("Auto Detect Rolling: " + prop.getAuto_roll() );
		annotate("Auto Detect Size: " + prop.getAuto_check_size() );
		annotate("Auto Detect Skeleton: " + prop.getAuto_check_skeleton() );
		
		annotate("Auto Fix Invalid Larva: " + prop.getFix_invalid_larva() );
		
		annotate("Tot SywDist: " + larva.getDistSidewaysRollTotal() + ", Rolls: " + larva.getRolls() 
				+ ",angCntEnd1End2: " + larva.getAngleCenterEnd1End2() + ",angCntEnd2End1: " + larva.getAngleCenterEnd2End1()
				+ ",angAvgCntEnd1: " + larva.getAngleAvgCenterEnd1() + ",angAvgCntEnd2: " + larva.getAngleAvgCenterEnd2() 
				+ ",angRef: " + larva.getAngleRefCenterN1());
		
//		annotate(new Point(1140, 43), "Rolling...", Color.yellow);
//		
//		annotate(new Point(1135, 70), "Roll Annota.", Color.green);
//		annotate(new Point(1135, 220), "Cropp Image", Color.green);
//		annotate(new Point(1135, 370), "Angle Image", Color.green);
//		annotate(new Point(1135, 520), "Binary Image", Color.green);
//		annotate(new Point(1135, 670), "Contour Image", Color.green);
	}

	/**
	* Annotate text on a image plus.
	* 
	* @param pointTopLeft The point on the top left.
	* @param text The message.
	* @param color The color.
	* @return None.
	*/
	public void annotate(Point pointTopLeft, String text, Color color) 
	{
		TextRoi roi = new TextRoi(pointTopLeft.x, pointTopLeft.y, text, font);
		Overlay overlay = new Overlay();
		overlay.add(roi);

		imagePlus.getProcessor().setOverlay(overlay);
		imagePlus.getProcessor().setColor(color);
		roi.drawPixels(imagePlus.getProcessor());
	}
	
	/**
	* Annotate text on a image plus.
	* 
	* @param text The message.
	* @param color The color.
	* @return None.
	*/
	public void annotate(String text, Color color) 
	{
		TextRoi roi = new TextRoi(pointCurrent.x, pointCurrent.y, text, font);
		Overlay overlay = new Overlay();
		overlay.add(roi);

		imagePlus.getProcessor().setOverlay(overlay);
		imagePlus.getProcessor().setColor(color);
		roi.drawPixels(imagePlus.getProcessor());
		pointCurrent.y += yHeight;
	}

	/**
	 * Annotate text on an image.
	 * 
	 * @param imagePlus
	 *            The image processor to be annotated.
	 * @param text
	 *            The text size of the annotation.
	 * @return void
	 */
	public void annotate(String text) {
		TextRoi roi = new TextRoi(pointCurrent.x, pointCurrent.y, text, font);
		Overlay overlay = new Overlay();
		overlay.add(roi);

		imagePlus.getProcessor().setOverlay(overlay);
		imagePlus.getProcessor().setColor(color);
		roi.drawPixels(imagePlus.getProcessor());
		pointCurrent.y += yHeight;
	}
	
	public void annotateNumLine37(String text, Color color, int numLine) 
	{
		String[] splitArray = text.split("\\s+");
		
		int numWordsInLine = 15;
		
		int heightText = 30;
		int yText = 0;
		String txt = "";
		Point pt = new Point(340,300);
		
		int numRow = (int) Math.ceil( (double) splitArray.length / (double) numWordsInLine );
		
		for(int i = 0; i < numRow; i++)
		{
			yText = i * heightText;
			
			txt = "";
			for(int j = 0; j < numWordsInLine; j++)
			{
				int index = i * numWordsInLine + j;
			
				if(index < splitArray.length)
					
					txt += splitArray[index] + " ";
			
				TextRoi roi = new TextRoi(pt.x, pt.y + yText, txt, font);
	
				imagePlus.getProcessor().setColor(color);
				roi.drawPixels(imagePlus.getProcessor());
			}
		}
	}
	
	public static void annotateNumLine27(ImagePlus imagePlus, String text, Point pt, Color color) 
	{
		String[] splitArray = text.split("\\s+");
		
		Font font = new Font("SansSerif", Font.PLAIN, 18);
		
		int numWordsInLine = 15;
		
		int heightText = 30;
		int yText = 0;
		String txt = "";
		
		int numRow = (int) Math.ceil( (double) splitArray.length / (double) numWordsInLine );
		
		System.out.println("numRow: "+numRow);
		
		for(int i = 0; i < numRow; i++)
		{
			yText = i * heightText;
			
			txt = "";
			for(int j = 0; j < numWordsInLine; j++)
			{
				int index = i * numWordsInLine + j;
			
				if(index < splitArray.length)
					
					txt += splitArray[index] + " ";
			
			TextRoi roi = new TextRoi(pt.x, pt.y + yText, txt, font);
			imagePlus.getProcessor().setColor(color);
			roi.drawPixels(imagePlus.getProcessor());
			}
		}
	}

	public Larva getLarva7() {
		return larva;
	}

	public void setLarva7(Larva featuresOfLarva) {
		this.larva = featuresOfLarva;
	}

	public Point getPointCurrent() {
		return pointCurrent;
	}

	public void setPointCurrent(Point pointCurrent) {
		this.pointCurrent = pointCurrent;
	}

	public int getyHeight() {
		return yHeight;
	}

	public void setyHeight(int yHeight) {
		this.yHeight = yHeight;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public int getFontSize() {
		return fontSize;
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	public int getNumRowError() {
		return numRowError;
	}

	public void setNumRowError(int numRowError) {
		this.numRowError = numRowError;
	}

}
