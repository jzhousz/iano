package annotool;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class AnnTablePanel extends JPanel  {

	JScrollPane tableOne = null;
	JScrollPane tableTwo = null;
	//for putting back prediction results after annotation
	AnnImageTable currentTestImageTable = null; 
	AnnImageTable currentCVTable = null;
	JFrame frame;

	public AnnTablePanel(JFrame frame) 
	{ this.frame = frame;	}

	public boolean displayOneImageTable(String directory, String targetFile, String ext)
	{
		//remove the old table from the table panel first, if any.
		if (tableOne != null)
			this.remove(tableOne);

		if (tableTwo != null)
			this.remove(tableTwo);

		AnnImageTable cvTable = new AnnImageTable();
		tableOne = cvTable.buildImageTable(directory, targetFile, ext);

		if (tableOne != null)
		{
			this.setLayout(new java.awt.BorderLayout());
			this.add(tableOne, java.awt.BorderLayout.CENTER);
			tableOne.setBorder(new CompoundBorder(new TitledBorder(null, "cross validation data set", 
					TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(5,5,5,5))); 
			currentCVTable = cvTable;
			frame.pack();
			return true;
		}
		else return false;
	}

	//there is testing target file
	public boolean displayTwoImageTables(String directory, String targetFile, String ext, String testdir, String testtargetFile, String testext)
	{
		//remove the old table from the table panel first, if any.
		if (tableOne != null)
			this.remove(tableOne);

		if (tableTwo != null)
			this.remove(tableTwo);

		tableOne = new AnnImageTable().buildImageTable(directory, targetFile, ext);
		AnnImageTable testingTable = new AnnImageTable();
		tableTwo = testingTable.buildImageTable(testdir, testtargetFile, testext);

		tableOne.setBorder(new CompoundBorder(new TitledBorder(null, "training images", 
				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(5,5,5,5))); 
		tableTwo.setBorder(new CompoundBorder(new TitledBorder(null, "testing images", 
				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(5,5,5,5))); 

		if (tableOne != null &&tableTwo != null)
		{
			this.setLayout(new java.awt.GridLayout(1,2,5,5));
			this.add(tableOne);
			this.add(tableTwo);
			currentTestImageTable = testingTable;
			adjustFrame();
			return true;
		}
		else return false;
	}


	//overloadded version when there is no testing targets
	public boolean displayTwoImageTables(String directory, String targetFile, String ext, String testdir, String testext)
	{
		//remove the old table from the table panel first, if any.
		if (tableOne != null)
			this.remove(tableOne);
		if (tableTwo != null)
			this.remove(tableTwo);

		tableOne = new AnnImageTable().buildImageTable(directory, targetFile, ext);
		AnnImageTable testingTable = new AnnImageTable();
		tableTwo = testingTable.buildImageTable(testdir, testext);

		tableOne.setBorder(new CompoundBorder(new TitledBorder(null, "training AOI patterns", 
				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(5,5,5,5))); 
		tableTwo.setBorder(new CompoundBorder(new TitledBorder(null, "images to be annotated", 
				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(5,5,5,5))); 

		if (tableOne != null &&tableTwo != null)
		{
			this.setLayout(new java.awt.GridLayout(1,2,5,5));
			this.add(tableOne);
			this.add(tableTwo);
			currentTestImageTable = testingTable;
			adjustFrame();
			return true;
		}
		else return false;
	}

	
	public void updateTestingTable(Annotation[][] predictions)
	{
		if (currentTestImageTable != null)
			currentTestImageTable.updateTable(predictions);
		adjustFrame();
	}

	public void updateCVTable(Annotation[][] predictions)
	{
		if (currentCVTable != null)
			currentCVTable.updateTable(predictions);
		adjustFrame();
	}

	private void adjustFrame()
	{
		frame.pack();
		Dimension dim =
			Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int)(dim.getWidth() - frame.getWidth())/2;
		int y = (int)(dim.getHeight() - frame.getHeight())/2;
		frame.setLocation(x,y);
	}



}
