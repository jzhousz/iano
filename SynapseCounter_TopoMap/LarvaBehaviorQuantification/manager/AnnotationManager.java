package manager;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;

import entities.FeaturesOfLarva;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.TextRoi;

public class AnnotationManager {

	// the information of this FeaturesOfLarva will be annotated on the image
	private FeaturesOfLarva featuresOfLarva = null;
	private ImagePlus imagePlus = null; // the image to be annotated text on

	// the point currently at which draw annotations
	private Point pointCurrent = null;
	private int yHeight = 20; // the height of a text row
	private Color color = null; // the color with which draw annotations
	private int fontSize = 18; // the font size

	private Font font = null;
	
	private int numRowError = 0;
	private Point ptError = null;
	
	public AnnotationManager(FeaturesOfLarva featuresOfLarva, ImagePlus imagePlus) {
		this.featuresOfLarva = featuresOfLarva;
		this.imagePlus = imagePlus;
		this.pointCurrent = new Point(50, 50);
		this.color = Color.green;
		this.font = new Font("SansSerif", Font.PLAIN, fontSize);
		this.ptError = new Point(340,300);
	}

	public AnnotationManager(FeaturesOfLarva featuresOfLarva, ImagePlus imagePlus, Point pointStart) {
		this(featuresOfLarva, imagePlus);
		this.pointCurrent = pointStart;
	}

	public void resetErrorMsgPos()
	{
		numRowError = 0;
	}
	
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
	
	//			Overlay overlay = new Overlay(roi);
	
				Overlay overlay = new Overlay();
				overlay.add(roi);
				
				imagePlus.getProcessor().setOverlay(overlay);
				imagePlus.getProcessor().setColor(color);
				roi.drawPixels(imagePlus.getProcessor());
				
//				imagePlus.getProcessor().setColor(color);
//				roi.drawPixels(imagePlus.getProcessor());
//				
////				TextRoi roi = new TextRoi(pointCurrent.x, pointCurrent.y, text, font);
//				Overlay overlay = new Overlay();
//				overlay.add(roi);
//
//				imagePlus.getProcessor().setOverlay(overlay);
//				imagePlus.getProcessor().setColor(color);
//				roi.drawPixels(imagePlus.getProcessor());
			}
		}
		
//		ptError.y = ptError.y + heightText * numRow;
		numRowError += numRow;
	}
	
//	public void annotateMessage3(String msg, int numLine) {
//		annotateNumLine(msg, Color.red, numLine);
//	}

	public void annotateAll(PropertyManager prop) 
	{
		
		String strStimulus = "";
		if( PropertyManager.getBool( prop.getChrimson_stimulus() ) )
			strStimulus = "Chrimson";
		else
			strStimulus = "Blue Light";
		
		annotate("Stimulus: " + strStimulus + ", Frame: " + featuresOfLarva.getFrameId());
		
		annotate("Prev Valid: " + featuresOfLarva.getIsPreviousValid()+", Crnt Valid: " + featuresOfLarva.getIsValid());
		
		annotate("Curl of the Larva: " + featuresOfLarva.getCurl() );

		// if the featuresOfLarva has the previous frame
		if (featuresOfLarva.getFeaturesOfLarvaPrevious() != null) 
		{
			annotate("Distance Moved: " + featuresOfLarva.getDistanceCenterPoint());
			
			String strDistSidewaysEndPts = "";
			
			// if the larva is invalid or the previous larva is invalid
			if(featuresOfLarva.getIsValid() && featuresOfLarva.getIsPreviousValid())
				strDistSidewaysEndPts = Double.toString(featuresOfLarva.getDistanceSidewaysEndPts());
			else
				strDistSidewaysEndPts = Double.toString(featuresOfLarva.getDistanceSidewaysEndPts()) + " (Avg)";
			
			annotate("Sideways Distance (End Pt): "+ strDistSidewaysEndPts);
//			annotate("    Yao: " + featuresOfLarva.getDistanceSideways()+", Shw: " + featuresOfLarva.getDistanceSidewaysShawn());
//			annotate("    Quar: " + featuresOfLarva.getDistQuartile()+", End: " + strDistSidewaysEndPts);

			annotate("Sideways Distance Sign (End Pts):");

			annotate("    Signed: " + featuresOfLarva.getIsSidewaysEndPtsForward() + ", Directed: " + featuresOfLarva.getIsDistSidewaysPos() );
			
			annotate("Center Point: (" + featuresOfLarva.getFeaturesOfLarvaPrevious().getCenterPointOnFullFrame().x
					+ ", " + featuresOfLarva.getFeaturesOfLarvaPrevious().getCenterPointOnFullFrame().y + "), ("
					+ featuresOfLarva.getCenterPointOnFullFrame().x + ", "
					+ featuresOfLarva.getCenterPointOnFullFrame().y + ")");
			annotate("End Point 1: (" + featuresOfLarva.getFeaturesOfLarvaPrevious().getEndPointsOnFullFrame().get(0).x
					+ ", " + featuresOfLarva.getFeaturesOfLarvaPrevious().getEndPointsOnFullFrame().get(0).y + "), ("
					+ featuresOfLarva.getEndPointsOnFullFrame().get(0).x + ", "
					+ featuresOfLarva.getEndPointsOnFullFrame().get(0).y + ")");
			annotate("End Point 2: (" + featuresOfLarva.getFeaturesOfLarvaPrevious().getEndPointsOnFullFrame().get(1).x
					+ ", " + featuresOfLarva.getFeaturesOfLarvaPrevious().getEndPointsOnFullFrame().get(1).y + "), ("
					+ featuresOfLarva.getEndPointsOnFullFrame().get(1).x + ", "
					+ featuresOfLarva.getEndPointsOnFullFrame().get(1).y + ")");

			annotate("Center Point Linear Line (Parallel):");
			annotate("       " + featuresOfLarva.getLinearLineParallel());
			annotate("Center Point Linear Line (Perpend.):");
			annotate("       " + featuresOfLarva.getLinearLinePerpendicular());
			
			annotate("End Points Linear Line (Parallel):");
			annotate("       " + featuresOfLarva.getLinearLineEndPtsParallel());
			annotate("End Points Linear Line (Perpend.):");
			annotate("       " + featuresOfLarva.getLinearLineEndPtsPerp());
			
//			annotate("Quartile Linear Line (Parallel):");
//			annotate("       " + featuresOfLarva.getLineQuartileParallel());
//			annotate("Quartile Linear Line (Perpend.):");
//			annotate("       " + featuresOfLarva.getLineQuartilePerp());
			
		// if the featuresOfLarva doesn't have a previous frame
		}else
		{
			annotate("Total Distance : " + featuresOfLarva.getDistanceSidewaysTotal() );

			annotate("Center Point: (null, null), ("
					+ featuresOfLarva.getCenterPointOnFullFrame().x + ", "
					+ featuresOfLarva.getCenterPointOnFullFrame().y + ")");
			annotate("End Point 1: (null, null), ("
					+ featuresOfLarva.getEndPointsOnFullFrame().get(0).x + ", "
					+ featuresOfLarva.getEndPointsOnFullFrame().get(0).y + ")");
			annotate("End Point 2: (null, null), ("
					+ featuresOfLarva.getEndPointsOnFullFrame().get(1).x + ", "
					+ featuresOfLarva.getEndPointsOnFullFrame().get(1).y + ")");
		}
		
		annotate("Pixel Area : " + featuresOfLarva.getArea()+", Avg Area: "+featuresOfLarva.getAvgArea());
		annotate("Skel Len : " + featuresOfLarva.getLengthSkeleton()+", Avg Skel: "+featuresOfLarva.getAvgSkeletonLen());
		annotate("Linear & End pts Angle : " + featuresOfLarva.getAngleEndPointsLinear());
		annotate("Need Rec: " + featuresOfLarva.getNeedRecord()+", Rolling: " + featuresOfLarva.getIsRolling());
		
		if(featuresOfLarva.getIsRolling())
			annotate(new Point(1140, 43), "Rolling...", Color.yellow);
		
//		Point point1stQuartileFullFrame = featuresOfLarva.getPointOnFullFrame(featuresOfLarva.getPoint1stQuartile());
//		Point point3rdQuartileFullFrame = featuresOfLarva.getPointOnFullFrame(featuresOfLarva.getPoint3rdQuartile());
//		
//		annotate("Quar Pts: (" + point1stQuartileFullFrame.x
//				+ ", " + point1stQuartileFullFrame.y + "), ("
//				+ point3rdQuartileFullFrame.x + ", "
//				+ point3rdQuartileFullFrame.y + ")");

		String strOperator = "";
		
		annotate("Diameter of Larva: " + featuresOfLarva.getDiameter() );
		annotate("Perimeter:" + featuresOfLarva.getPerimeter() + ", Avg Peri:"+featuresOfLarva.getAvgPerimeter());
		
//		annotate("(Threshold) Num Frm:" + PropertyManager.getProperty("rolling_frame")
//				+ ", Peri:"+PropertyManager.getProperty("larva_perimeter"));
		annotate("(Threshold) Num Frm:" + prop.getRolling_frame()
		+ ", Peri:"+prop.getLarva_perimeter());
		annotate("(Size) Max:" + prop.getMax_size() + ", Min:" + prop.getMin_size());
		annotate("(Skel) Max:" + prop.getMax_skeleton() + ", Min:" + prop.getMin_skeleton() );
		annotate("SgnBgn: " + featuresOfLarva.getDistSwySgnBgn()+", sgnEnd: " +featuresOfLarva.getDistSwySgnEnd() );
		annotate( "dirBgn: " + featuresOfLarva.getDistSwyDirBgn() +", dirEnd: " +featuresOfLarva.getDistSwyDirEnd());
		
//		annotate("Auto Detect Rolling: " + PropertyManager.getProperty("auto_roll") );
		annotate("Auto Detect Rolling: " + prop.getAuto_roll() );
		annotate("Auto Detect Size: " + prop.getAuto_check_size() );
		annotate("Auto Detect Skeleton: " + prop.getAuto_check_skeleton() );
		
		annotate("Auto Fix Invalid Larva: " + prop.getFix_invalid_larva() );
		
//		annotate(new Point(1140, 43), "Rolling...", Color.yellow);
//		
//		annotate(new Point(1135, 70), "Roll Annota.", Color.green);
//		annotate(new Point(1135, 220), "Cropp Image", Color.green);
//		annotate(new Point(1135, 370), "Angle Image", Color.green);
//		annotate(new Point(1135, 520), "Binary Image", Color.green);
//		annotate(new Point(1135, 670), "Contour Image", Color.green);
	}

	public void annotate(Point pointTopLeft, String text, Color color) {
//		Font font = new Font("SansSerif", Font.PLAIN, fontSize);
		TextRoi roi = new TextRoi(pointTopLeft.x, pointTopLeft.y, text, font);
		Overlay overlay = new Overlay();
		overlay.add(roi);

		imagePlus.getProcessor().setOverlay(overlay);
		imagePlus.getProcessor().setColor(color);
		roi.drawPixels(imagePlus.getProcessor());
	}
	
	public void annotate(String text, Color color) {
//		Font font = new Font("SansSerif", Font.PLAIN, fontSize);
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
//		Font font = new Font("SansSerif", Font.PLAIN, fontSize);
		TextRoi roi = new TextRoi(pointCurrent.x, pointCurrent.y, text, font);
		Overlay overlay = new Overlay();
		overlay.add(roi);

		imagePlus.getProcessor().setOverlay(overlay);
		imagePlus.getProcessor().setColor(color);
		roi.drawPixels(imagePlus.getProcessor());
		pointCurrent.y += yHeight;
	}
	
	public void annotateNumLine3(String text, Color color, int numLine) 
	{
		String[] splitArray = text.split("\\s+");
		
//		Font font = new Font("SansSerif", Font.PLAIN, 18);
		
		int numWordsInLine = 15;
		
		int heightText = 30;
		int yText = 0;
		String txt = "";
		Point pt = new Point(340,300);
		
		int numRow = (int) Math.ceil( (double) splitArray.length / (double) numWordsInLine );
		
//		System.out.println("numRow: "+numRow);
		
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
	
	//			Overlay overlay = new Overlay(roi);
	
				imagePlus.getProcessor().setColor(color);
				roi.drawPixels(imagePlus.getProcessor());
			}
		}
	}
	
//	public void annotateNumLine(String text, Color color, int numLine) {
//		Font font = new Font("SansSerif", Font.PLAIN, fontSize);
//		TextRoi roi = new TextRoi(pointCurrent.x, pointCurrent.y, 300, 300, text, font);
//		Overlay overlay = new Overlay();
//		overlay.add(roi);
//
//		imagePlus.getProcessor().setOverlay(overlay);
//		imagePlus.getProcessor().setColor(color);
//		roi.drawPixels(imagePlus.getProcessor());
//		pointCurrent.y += numLine * yHeight;
//	}
	
	public static void annotateNumLine2(ImagePlus imagePlus, String text, Point pt, Color color) 
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
			
	//		TextRoi roi2 = new TextRoi(pt.x, pt.y+h, "something2 something2 something2 something2 something2", font);
			
//			Overlay overlay = new Overlay(roi);
	//		overlay.add(roi);
			
	//		imagePlus.getProcessor().setOverlay(overlay);
			imagePlus.getProcessor().setColor(color);
			roi.drawPixels(imagePlus.getProcessor());
			}
		}
		
//		txt = "something2 something2 something2 something2 something2";
//		
//		roi = new TextRoi(pt.x, pt.y + h, txt, font);
//		
//		Overlay overlay2 = new Overlay(roi);
//		overlay2.add(roi);
//		
////		imagePlus.getProcessor().setOverlay(overlay2);
//		imagePlus.getProcessor().setColor(color);
//		roi.drawPixels(imagePlus.getProcessor());
		
//		pointCurrent.y += numLine * yHeight;
	}

	public FeaturesOfLarva getFeaturesOfLarva() {
		return featuresOfLarva;
	}

	public void setFeaturesOfLarva(FeaturesOfLarva featuresOfLarva) {
		this.featuresOfLarva = featuresOfLarva;
	}

	// public Point getPointStart() {
	// return pointStart;
	// }
	//
	// public void setPointStart(Point pointStart) {
	// this.pointStart = pointStart;
	// }

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
