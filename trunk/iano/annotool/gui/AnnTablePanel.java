package annotool.gui;

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

public class AnnTablePanel extends JPanel {

	JScrollPane tableOne = null;
	JScrollPane tableTwo = null;
	// for putting back prediction results after annotation
	AnnImageTable currentTestImageTable = null;
	AnnImageTable currentCVTable = null;
	JFrame frame;

	HashMap<String, String> classNames;
	
	private DataInput trainingProblem = null;
	private DataInput testingProblem = null;
	
	private boolean is3D,
					isColor;

	public AnnTablePanel(JFrame frame) {
		this.frame = frame;
	}

	public boolean displayOneImageTable(String directory, String targetFile,
			String ext) throws Exception {
		trainingProblem = null;
		testingProblem = null;
		
		// remove the old table from the table panel first, if any.
		if (tableOne != null)
			this.remove(tableOne);

		if (tableTwo != null)
			this.remove(tableTwo);

		AnnImageTable cvTable = new AnnImageTable();
		tableOne = cvTable.buildImageTable(directory, targetFile, ext);
		classNames = cvTable.getClassNames();
		
		is3D = cvTable.is3D();
		isColor = cvTable.isColor();

		if (tableOne != null) {
			this.setLayout(new java.awt.BorderLayout());
			this.add(tableOne, java.awt.BorderLayout.CENTER);
			tableOne.setBorder(new CompoundBorder(new TitledBorder(null,
					"data set", TitledBorder.LEFT, TitledBorder.TOP),
					new EmptyBorder(5, 5, 5, 5)));
			currentCVTable = cvTable;
			
			trainingProblem = cvTable.getProblem();
			
			adjustFrame();
			return true;
		} else
			return false;
	}

	// No target case - eg. annotate
	public boolean displayOneImageTable(String directory, String ext) throws Exception {
		trainingProblem = null;
		testingProblem = null;
		
		// remove the old table from the table panel first, if any.
		if (tableOne != null)
			this.remove(tableOne);

		if (tableTwo != null)
			this.remove(tableTwo);

		AnnImageTable cvTable = new AnnImageTable();
		tableOne = cvTable.buildImageTable(directory, ext);
		classNames = cvTable.getClassNames();
		
		is3D = cvTable.is3D();
		isColor = cvTable.isColor();

		if (tableOne != null) {
			this.setLayout(new java.awt.BorderLayout());
			this.add(tableOne, java.awt.BorderLayout.CENTER);
			tableOne.setBorder(new CompoundBorder(new TitledBorder(null,
					"data set", TitledBorder.LEFT, TitledBorder.TOP),
					new EmptyBorder(5, 5, 5, 5)));
			currentCVTable = cvTable;
			
			testingProblem = cvTable.getProblem();
			
			adjustFrame();
			return true;
		} else
			return false;
	}

	// there is testing target file
	public boolean displayTwoImageTables(String directory, String targetFile,
			String ext, String testdir, String testtargetFile, String testext) throws Exception {		
		trainingProblem = null;
		testingProblem = null;
		
		// remove the old table from the table panel first, if any.
		if (tableOne != null)
			this.remove(tableOne);

		if (tableTwo != null)
			this.remove(tableTwo);

		AnnImageTable trainingTable = new AnnImageTable();
		tableOne = trainingTable.buildImageTable(directory, targetFile, ext);
		classNames = trainingTable.getClassNames();

		AnnImageTable testingTable = new AnnImageTable();
		tableTwo = testingTable.buildImageTable(testdir, testtargetFile,
				testext);
		
		is3D = trainingTable.is3D();
		isColor = trainingTable.isColor();

		if (tableOne != null && tableTwo != null) {
			tableOne.setBorder(new CompoundBorder(new TitledBorder(null,
					"training images", TitledBorder.LEFT, TitledBorder.TOP),
					new EmptyBorder(5, 5, 5, 5)));
			tableTwo.setBorder(new CompoundBorder(new TitledBorder(null,
					"testing images", TitledBorder.LEFT, TitledBorder.TOP),
					new EmptyBorder(5, 5, 5, 5)));

			this.setLayout(new java.awt.GridLayout(1, 2, 5, 5));
			this.add(tableOne);
			this.add(tableTwo);
			currentTestImageTable = testingTable;
			
			trainingProblem = trainingTable.getProblem();
			testingProblem = testingTable.getProblem();
			
			adjustFrame();
			return true;
		} else
			return false;
	}

	// overloadded version when there is no testing targets
	public boolean displayTwoImageTables(String directory, String targetFile,
			String ext, String testdir, String testext) throws Exception {
		trainingProblem = null;
		testingProblem = null;
		
		// remove the old table from the table panel first, if any.
		if (tableOne != null)
			this.remove(tableOne);
		if (tableTwo != null)
			this.remove(tableTwo);

		AnnImageTable trainingTable = new AnnImageTable();
		tableOne = trainingTable.buildImageTable(directory, targetFile,
				ext);
		AnnImageTable testingTable = new AnnImageTable();
		tableTwo = testingTable.buildImageTable(testdir, testext);

		tableOne.setBorder(new CompoundBorder(new TitledBorder(null,
				"training AOI patterns", TitledBorder.LEFT, TitledBorder.TOP),
				new EmptyBorder(5, 5, 5, 5)));
		tableTwo.setBorder(new CompoundBorder(new TitledBorder(null,
				"images to be annotated", TitledBorder.LEFT, TitledBorder.TOP),
				new EmptyBorder(5, 5, 5, 5)));
		
		is3D = trainingTable.is3D();
		isColor = trainingTable.isColor();

		if (tableOne != null && tableTwo != null) {
			this.setLayout(new java.awt.GridLayout(1, 2, 5, 5));
			this.add(tableOne);
			this.add(tableTwo);
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
		if (currentCVTable != null)
			currentCVTable.updateTable(predictions, modelLabels, supportsProb,
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
		return currentCVTable;
	}

	public void removeTables() {
		if (tableOne != null)
			this.remove(tableOne);

		if (tableTwo != null)
			this.remove(tableTwo);
	}

	public HashMap<String, String> getClassNames() {
		return classNames;
	}

	/**
	 * Added: 3/29/2012 This method is used for loading from a hierarchical
	 * directory structure and does not have a target file. Each individual
	 * folder inside the top level directory is a different class.
	 */
	public boolean displayTwoImageTables(String directory, String ext,
			String testdir, String testext) throws Exception {
		trainingProblem = null;
		testingProblem = null;
		
		// remove the old table from the table panel first, if any.
		if (tableOne != null)
			this.remove(tableOne);

		if (tableTwo != null)
			this.remove(tableTwo);

		AnnImageTable trainingTable = new AnnImageTable();
		tableOne = trainingTable.buildImageTableFromSubdirectories(directory, ext);
		classNames = trainingTable.getClassNames();

		AnnImageTable testingTable = new AnnImageTable();
		tableTwo = testingTable.buildImageTableFromSubdirectories(testdir, testext);
		
		is3D = trainingTable.is3D();
		isColor = trainingTable.isColor();

		if (tableOne != null && tableTwo != null) {
			tableOne.setBorder(new CompoundBorder(new TitledBorder(null,
					"training images", TitledBorder.LEFT, TitledBorder.TOP),
					new EmptyBorder(5, 5, 5, 5)));
			tableTwo.setBorder(new CompoundBorder(new TitledBorder(null,
					"testing images", TitledBorder.LEFT, TitledBorder.TOP),
					new EmptyBorder(5, 5, 5, 5)));

			this.setLayout(new java.awt.GridLayout(1, 2, 5, 5));
			this.add(tableOne);
			this.add(tableTwo);
			currentTestImageTable = testingTable;
			
			trainingProblem = trainingTable.getProblem();
			testingProblem = testingTable.getProblem();
			
			adjustFrame();
			return true;
		} else
			return false;
	}

	public boolean displayOneImageTable(String directory, String ext, boolean isDirectoryStructure) throws Exception {
		if (!isDirectoryStructure)
			return displayOneImageTable(directory, ext);

		trainingProblem = null;
		testingProblem = null;
		
		// remove the old table from the table panel first, if any.
		if (tableOne != null)
			this.remove(tableOne);

		if (tableTwo != null)
			this.remove(tableTwo);

		AnnImageTable cvTable = new AnnImageTable();
		tableOne = cvTable.buildImageTableFromSubdirectories(directory, ext);
		classNames = cvTable.getClassNames();

		if (tableOne != null) {
			this.setLayout(new java.awt.BorderLayout());
			this.add(tableOne, java.awt.BorderLayout.CENTER);
			tableOne.setBorder(new CompoundBorder(new TitledBorder(null,
					"data set", TitledBorder.LEFT, TitledBorder.TOP),
					new EmptyBorder(5, 5, 5, 5)));
			currentCVTable = cvTable;
			
			trainingProblem = cvTable.getProblem();
			
			adjustFrame();
			return true;
		} else
			return false;
	}

	public DataInput getTrainingProblem() {
		return trainingProblem;
	}

	public DataInput getTestingProblem() {
		return testingProblem;
	}

	public boolean is3D() {
		return is3D;
	}

	public boolean isColor() {
		return isColor;
	}
	
	
}
