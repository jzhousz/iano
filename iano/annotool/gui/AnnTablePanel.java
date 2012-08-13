package annotool.gui;

import ij.ImagePlus;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import annotool.Annotation;
import annotool.io.DataInput;

/**
 * This panel contains either one or two AnnImageTable instances to display problem image/roi list.
 * It provides multiple methods to create one or two tables depending upon different modes.
 * 
 */
public class AnnTablePanel extends JPanel {

	JScrollPane tableOneScrollPane = null;
	JScrollPane tableTwoScrollPane = null;
	// for putting back prediction results after annotation
	AnnImageTable currentTestImageTable = null;
	AnnImageTable currentCVTable = null;
	AnnImageTable currentAnnotationTable = null;
	JFrame frame;

	HashMap<String, String> classNames;
	
	private DataInput trainingProblem = null;	//used by training only, cv and training/testing modes
	private DataInput testingProblem = null;	//used by training/testing mode
	
	private DataInput problem = null;	//used by new problems without target (from annotation/classification mode)
	
	private boolean is3D,
					isColor;

	public AnnTablePanel(JFrame frame) {
		this.frame = frame;
	}

	/**
	 * Used for cross validation mode or training only mode.
	 * 
	 * @param directory
	 * @param targetFile
	 * @param ext
	 * @return
	 * @throws Exception
	 */
	public boolean displayOneImageTable(String directory, String targetFile,
			String ext) throws Exception {
		resetTableBeforeBuild();

		AnnImageTable cvTable = new AnnImageTable();
		tableOneScrollPane = cvTable.buildImageTable(directory, targetFile, ext);
		classNames = cvTable.getClassNames();
		
		is3D = cvTable.is3D();
		isColor = cvTable.isColor();

		if (tableOneScrollPane != null) {
			this.setLayout(new java.awt.BorderLayout());
			this.add(tableOneScrollPane, java.awt.BorderLayout.CENTER);
			tableOneScrollPane.setBorder(new CompoundBorder(new TitledBorder(null,
					"data set", TitledBorder.LEFT, TitledBorder.TOP),
					new EmptyBorder(5, 5, 5, 5)));
			currentCVTable = cvTable;
			
			trainingProblem = cvTable.getProblem();
			
			adjustFrame();
			return true;
		} else
			return false;
	}
	
	/**
	 * For displaying one image table in roi input method.
	 * For cross validation and training only modes
	 * 
	 * @param imagePath
	 * @param roiPaths
	 * @return
	 * @throws Exception
	 */
	public boolean displayOneImageTable(String imagePath, String[] roiPaths, int depth, int newwidth, int newheight) throws Exception {		
		resetTableBeforeBuild();

		AnnImageTable singleTable = new AnnImageTable();
		tableOneScrollPane = singleTable.buildImageTableFromROI(imagePath, roiPaths, true, depth, newwidth, newheight);
		classNames = singleTable.getClassNames();
		
		is3D = singleTable.is3D();
		isColor = singleTable.isColor();

		if (tableOneScrollPane != null) {
			tableOneScrollPane.setBorder(new CompoundBorder(new TitledBorder(null,
					"data set", TitledBorder.LEFT, TitledBorder.TOP),
					new EmptyBorder(5, 5, 5, 5)));

			this.setLayout(new java.awt.GridLayout(1, 2, 5, 5));
			this.add(tableOneScrollPane);
			currentCVTable = singleTable;
			
			trainingProblem = singleTable.getProblem();
			
			//For annotate mode if used (TODO: use or not?)
			currentAnnotationTable = singleTable;
			problem = singleTable.getProblem();
			
			adjustFrame();
			return true;
		} else
			return false;
	}

	/**
	 * For modes without target known - annotation/classification
	 * 
	 * @param directory
	 * @param ext
	 * @return
	 * @throws Exception
	 */
	public boolean displayOneImageTable(String directory, String ext) throws Exception {
		resetTableBeforeBuild();

		AnnImageTable annotationTable = new AnnImageTable();
		tableOneScrollPane = annotationTable.buildImageTable(directory, ext);
		classNames = annotationTable.getClassNames();
		
		is3D = annotationTable.is3D();
		isColor = annotationTable.isColor();

		if (tableOneScrollPane != null) {
			this.setLayout(new java.awt.BorderLayout());
			this.add(tableOneScrollPane, java.awt.BorderLayout.CENTER);
			tableOneScrollPane.setBorder(new CompoundBorder(new TitledBorder(null,
					"data set", TitledBorder.LEFT, TitledBorder.TOP),
					new EmptyBorder(5, 5, 5, 5)));
			currentAnnotationTable = annotationTable;
			
			problem = annotationTable.getProblem();
			
			adjustFrame();
			return true;
		} else
			return false;
	}
	
	/**
	 * For ROI Annotation Mode -
	 * 
	 * @param files
	 * @param directory
	 * @param ext
	 * @param depth
	 * @return
	 * @throws Exception
	 */
	public boolean displayOneImageTable(String[] files, String directory, String ext) throws Exception {
		resetTableBeforeBuild();

		AnnImageTable annotationTable = new AnnImageTable();
		tableOneScrollPane = annotationTable.buildImageTable(files, directory, ext);
		classNames = annotationTable.getClassNames();
		
		is3D = annotationTable.is3D();
		isColor = annotationTable.isColor();

		if (tableOneScrollPane != null) {
			this.setLayout(new java.awt.BorderLayout());
			this.add(tableOneScrollPane, java.awt.BorderLayout.CENTER);
			tableOneScrollPane.setBorder(new CompoundBorder(new TitledBorder(null,
					"data set", TitledBorder.LEFT, TitledBorder.TOP),
					new EmptyBorder(5, 5, 5, 5)));
			currentAnnotationTable = annotationTable;
			
			problem = annotationTable.getProblem();
			
			adjustFrame();
			return true;
		} else
			return false;
	}

	/**
	 * Training/Testing mode 
	 * 
	 * @param directory
	 * @param targetFile
	 * @param ext
	 * @param testdir
	 * @param testtargetFile
	 * @param testext
	 * @return
	 * @throws Exception
	 */
	public boolean displayTwoImageTables(String directory, String targetFile,
			String ext, String testdir, String testtargetFile, String testext) throws Exception {		
		resetTableBeforeBuild();

		AnnImageTable trainingTable = new AnnImageTable();
		tableOneScrollPane = trainingTable.buildImageTable(directory, targetFile, ext);
		classNames = trainingTable.getClassNames();

		AnnImageTable testingTable = new AnnImageTable();
		tableTwoScrollPane = testingTable.buildImageTable(testdir, testtargetFile,
				testext);
		
		is3D = trainingTable.is3D();
		isColor = trainingTable.isColor();

		if (tableOneScrollPane != null && tableTwoScrollPane != null) {
			tableOneScrollPane.setBorder(new CompoundBorder(new TitledBorder(null,
					"training images", TitledBorder.LEFT, TitledBorder.TOP),
					new EmptyBorder(5, 5, 5, 5)));
			tableTwoScrollPane.setBorder(new CompoundBorder(new TitledBorder(null,
					"testing images", TitledBorder.LEFT, TitledBorder.TOP),
					new EmptyBorder(5, 5, 5, 5)));

			this.setLayout(new java.awt.GridLayout(1, 2, 5, 5));
			this.add(tableOneScrollPane);
			this.add(tableTwoScrollPane);
			currentTestImageTable = testingTable;
			
			trainingProblem = trainingTable.getProblem();
			testingProblem = testingTable.getProblem();
			
			adjustFrame();
			return true;
		} else
			return false;
	}

	// overloadded version when there is no testing targets
	//is this still used?
	public boolean displayTwoImageTables(String directory, String targetFile,
			String ext, String testdir, String testext) throws Exception {
		resetTableBeforeBuild();

		AnnImageTable trainingTable = new AnnImageTable();
		tableOneScrollPane = trainingTable.buildImageTable(directory, targetFile,
				ext);
		AnnImageTable testingTable = new AnnImageTable();
		tableTwoScrollPane = testingTable.buildImageTable(testdir, testext);

		tableOneScrollPane.setBorder(new CompoundBorder(new TitledBorder(null,
				"training AOI patterns", TitledBorder.LEFT, TitledBorder.TOP),
				new EmptyBorder(5, 5, 5, 5)));
		tableTwoScrollPane.setBorder(new CompoundBorder(new TitledBorder(null,
				"images to be annotated", TitledBorder.LEFT, TitledBorder.TOP),
				new EmptyBorder(5, 5, 5, 5)));
		
		is3D = trainingTable.is3D();
		isColor = trainingTable.isColor();

		if (tableOneScrollPane != null && tableTwoScrollPane != null) {
			this.setLayout(new java.awt.GridLayout(1, 2, 5, 5));
			this.add(tableOneScrollPane);
			this.add(tableTwoScrollPane);
			currentTestImageTable = testingTable;
			
			testingProblem = testingTable.getProblem();
			
			adjustFrame();
			return true;
		} else
			return false;
	}

	public void updateTestingTable(Annotation[][] predictions) {
		if (currentTestImageTable != null)
			currentTestImageTable.updateTable(predictions);
		adjustFrame();
	}

	public void updateCVTable(Annotation[][] predictions) {
		if (currentCVTable != null)
			currentCVTable.updateTable(predictions);
		adjustFrame();
	}

	public void updateAnnotationTable(Annotation[][] predictions,
			String[] modelLabels, boolean[] supportsProb, boolean isBinary) {
		if (currentAnnotationTable != null)
			currentAnnotationTable.updateTable(predictions, modelLabels, supportsProb,
					isBinary);
		adjustFrame();
	}

	private void adjustFrame() {
		frame.pack();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) (dim.getWidth() - frame.getWidth()) / 2;
		int y = (int) (dim.getHeight() - frame.getHeight()) / 2;
		frame.setLocation(x, y);
	}

	// Added: santosh 7/25/2011
	public AnnImageTable getAnnotationTable() {
		return currentAnnotationTable;
		//return currentCVTable;
	}

	public void removeTables() {
		if (tableOneScrollPane != null)
			this.remove(tableOneScrollPane);

		if (tableTwoScrollPane != null)
			this.remove(tableTwoScrollPane);
	}

	public HashMap<String, String> getClassNames() {
		return classNames;
	}

	/**
	 * Added: 3/29/2012 This method is used for loading from a hierarchical
	 * directory structure and does not have a target file, instead the targets
	 * are determined by folder structure. Each individual
	 * folder inside the top level directory is a different class. So, files within a folder
	 * belong to that class.
	 */
	public boolean displayTwoImageTables(String directory, String ext,
			String testdir, String testext) throws Exception {
		resetTableBeforeBuild();

		AnnImageTable trainingTable = new AnnImageTable();
		tableOneScrollPane = trainingTable.buildImageTableFromSubdirectories(directory, ext, true);
		classNames = trainingTable.getClassNames();

		AnnImageTable testingTable = new AnnImageTable();
		tableTwoScrollPane = testingTable.buildImageTableFromSubdirectories(testdir, testext, true);
		
		is3D = trainingTable.is3D();
		isColor = trainingTable.isColor();

		if (tableOneScrollPane != null && tableTwoScrollPane != null) {
			tableOneScrollPane.setBorder(new CompoundBorder(new TitledBorder(null,
					"training images", TitledBorder.LEFT, TitledBorder.TOP),
					new EmptyBorder(5, 5, 5, 5)));
			tableTwoScrollPane.setBorder(new CompoundBorder(new TitledBorder(null,
					"testing images", TitledBorder.LEFT, TitledBorder.TOP),
					new EmptyBorder(5, 5, 5, 5)));

			this.setLayout(new java.awt.GridLayout(1, 2, 5, 5));
			this.add(tableOneScrollPane);
			this.add(tableTwoScrollPane);
			currentTestImageTable = testingTable;
			
			trainingProblem = trainingTable.getProblem();
			testingProblem = testingTable.getProblem();
			
			adjustFrame();
			return true;
		} else
			return false;
	}
	
	/**
	 * Displays two training and testing image tables when rois sets are used as input.
	 * 
	 * @param imagePath
	 * @param roiPaths
	 * @param testImagePath
	 * @param testRoiPaths
	 * @return
	 * @throws Exception
	 */
	public boolean displayTwoImageTables(String imagePath, String[] roiPaths, 
			String testImagePath, String[] testRoiPaths, int depth, int newwidth, int newheight) throws Exception {		
		resetTableBeforeBuild();

		AnnImageTable trainingTable = new AnnImageTable();
		tableOneScrollPane = trainingTable.buildImageTableFromROI(imagePath, roiPaths, true, depth, newwidth, newheight);
		classNames = trainingTable.getClassNames();

		AnnImageTable testingTable = new AnnImageTable();
		tableTwoScrollPane = testingTable.buildImageTableFromROI(testImagePath, testRoiPaths, true, depth, newwidth, newheight);
		
		is3D = trainingTable.is3D();
		isColor = trainingTable.isColor();

		if (tableOneScrollPane != null && tableTwoScrollPane != null) {
			tableOneScrollPane.setBorder(new CompoundBorder(new TitledBorder(null,
					"training set", TitledBorder.LEFT, TitledBorder.TOP),
					new EmptyBorder(5, 5, 5, 5)));
			tableTwoScrollPane.setBorder(new CompoundBorder(new TitledBorder(null,
					"testing set", TitledBorder.LEFT, TitledBorder.TOP),
					new EmptyBorder(5, 5, 5, 5)));

			this.setLayout(new java.awt.GridLayout(1, 2, 5, 5));
			this.add(tableOneScrollPane);
			this.add(tableTwoScrollPane);
			currentTestImageTable = testingTable;
			
			trainingProblem = trainingTable.getProblem();
			testingProblem = testingTable.getProblem();
			
			adjustFrame();
			return true;
		} else
			return false;
	}

	/**
	 * This method is used for displaying one image table for directory structure input mode.
	 * So, this can be training only, cv, annotation or classificaton modes.
	 * note: For annotation/classification it displays targets for now (TODO: decide what to do)
	 * 
	 * @param directory
	 * @param ext
	 * @param isDirectoryStructure
	 * @return
	 * @throws Exception
	 */
	public boolean displayOneImageTable(String directory, String ext, boolean isDirectoryStructure) throws Exception {
		if (!isDirectoryStructure)
			return displayOneImageTable(directory, ext);

		resetTableBeforeBuild();

		AnnImageTable singleTable = new AnnImageTable();
		tableOneScrollPane = singleTable.buildImageTableFromSubdirectories(directory, ext, true);
		classNames = singleTable.getClassNames();

		if (tableOneScrollPane != null) {
			this.setLayout(new java.awt.BorderLayout());
			this.add(tableOneScrollPane, java.awt.BorderLayout.CENTER);
			tableOneScrollPane.setBorder(new CompoundBorder(new TitledBorder(null,
					"data set", TitledBorder.LEFT, TitledBorder.TOP),
					new EmptyBorder(5, 5, 5, 5)));
			
			
			currentCVTable = singleTable;	//For cv mode
			trainingProblem = singleTable.getProblem(); //For cv and training only mode
			
			//For annotate mode
			currentAnnotationTable = singleTable;
			problem = singleTable.getProblem();
			
			adjustFrame();
			return true;
		} else
			return false;
	}
	
	/**
	 * Gets the problem used for training only mode, cross validation mode
	 * and training part of training/testing mode.
	 * @return
	 */
	public DataInput getTrainingProblem() {
		return trainingProblem;
	}

	/**
	 * Gets the problem used for testing part of training/testing mode
	 * @return
	 */
	public DataInput getTestingProblem() {
		return testingProblem;
	}
	
	/**
	 * Gets the problem used for annotation/classification mode
	 * @return
	 */
	public DataInput getProblem() {
		return problem;
	}

	public boolean is3D() {
		return is3D;
	}

	public boolean isColor() {
		return isColor;
	}
	
	/**
	 * Common things to do before building tables with different parameters.
	 * Set all datainput references to null
	 * remove tableOneScrollPane and tableTwoScrollPane
	 */
	private void resetTableBeforeBuild() {
		trainingProblem = null;
		testingProblem = null;
		problem = null;
		
		// remove the old table from the table panel first, if any.
		if (tableOneScrollPane != null)
			this.remove(tableOneScrollPane);

		if (tableTwoScrollPane != null)
			this.remove(tableTwoScrollPane);
	}
	
}
