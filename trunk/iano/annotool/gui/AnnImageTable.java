package annotool.gui;

import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.MedianCut;
import ij.process.ShortProcessor;
import ij.process.StackConverter;
import ij3d.Content;
import ij3d.Image3DUniverse;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import annotool.Annotation;
import annotool.Annotator;
import annotool.io.DataInput;
import annotool.io.DirectoryReader;
import annotool.io.LabelReader;

public class AnnImageTable {

	String showIcon = "images/Zoom16.gif";
	final int THUMB_HEIGHT = 20;

	String[] children = null;
	int[][] targets;
	String directory = null;
	JScrollPane scrollPane = null;
	JTable table = null;
	annotool.io.DataInput problem = null;

	//Object[][] tabledata = null;
	int numOfAnno;
	java.util.ArrayList<String> annotations; //labels
	HashMap<String, String> classNames;

	AnnImageTable()
	{	}

	//the table with targets
	public JScrollPane buildImageTable(String directory, String targetFile, String ext) throws Exception
	{
		this.directory = directory;
		problem = new annotool.io.DataInput(directory,ext);//(String) extBox.getSelectedItem());
		children = problem.getChildren();

		if (children == null)
		{
			javax.swing.JOptionPane.showMessageDialog(null,"Error: File path may be incorrect.");
			return null;
		}	
		if (children.length == 0)
		{
			javax.swing.JOptionPane.showMessageDialog(null,"There is no image with the given extension.");
			return null;
		}
		//get the targets
		try
		{
			annotations =  new java.util.ArrayList<String>();
			LabelReader labelReader = new LabelReader(children.length, annotations);
			targets = labelReader.getTargets(targetFile,children);
			numOfAnno = labelReader.getNumOfAnnotations();
			//annotations = labelReader.getAnnotations();
			classNames = labelReader.getClassnames();
		}catch(java.io.FileNotFoundException e)
		{
		    e.printStackTrace();
		    javax.swing.JOptionPane.showMessageDialog(null,"Target file not found");
		    return null;
		}catch(Exception e)
		{
			e.printStackTrace();
			javax.swing.JOptionPane.showMessageDialog(null,"Format problem in target file. Please make sure labels are int and number of lines is correct.");
			return null;
		}
		//build up the JTable
		final String[] columnNames;
		columnNames = new String[numOfAnno + 2];
		columnNames[0] = "image thumbnail";
		columnNames[1]= "file name";
		for(int i= 0; i < numOfAnno; i++)
			columnNames[2+i] = annotations.get(i);
	
		final Object[][] tabledata = new Object[children.length][columnNames.length];
		for (int i = 0; i < children.length; i++)
		{
			tabledata[i][0] =  getButtonCell(i); 
			tabledata[i][1] = children[i];

			for (int j = 0; j < numOfAnno; j++)
				//targets are stored by one annotation per row	
				tabledata[i][2+j] = targets[j][i];
		}		

		// Create a model of the data. 
		javax.swing.table.TableModel dataModel = new javax.swing.table.AbstractTableModel() { 
			public int getColumnCount() { return columnNames.length; } 
			public int getRowCount() { return tabledata.length;} 
			public Object getValueAt(int row, int col) {return tabledata[row][col];} 
			public String getColumnName(int column) {return columnNames[column];} 
			public Class getColumnClass(int c) {return getValueAt(0, c).getClass();} 
			public boolean isCellEditable(int row, int col) {return false ;} 
			public void setValueAt(Object aValue, int row, int column) 
			{ tabledata[row][column] = aValue; 
			fireTableCellUpdated(row, column); //needed if data could change
			} 
		}; 

		TableCellRenderer defaultRenderer;
		table = new JTable(dataModel);
		table.setRowHeight(THUMB_HEIGHT + 4); 
		defaultRenderer = table.getDefaultRenderer(JButton.class);
		table.setDefaultRenderer(JButton.class,
				new JTableButtonRenderer(defaultRenderer));
		scrollPane = new JScrollPane(table);
		scrollPane.setOpaque(true); //content panes must be opaque

		table.addMouseListener(new JTableButtonMouseListener(table));
		return scrollPane;
	}


	//overloadded version: a table without targets (i.e. no results know yet)
	public JScrollPane buildImageTable(String directory, String ext) throws Exception
	{
		this.directory = directory;
		problem = new annotool.io.DataInput(directory, ext);//(String) extBox.getSelectedItem());
		children = problem.getChildren();

		if (children == null)
		{
			javax.swing.JOptionPane.showMessageDialog(null,"Error: File path may be incorrect.");
			return null;
		}	
		if (children.length == 0)
		{
			javax.swing.JOptionPane.showMessageDialog(null,"There is no image with the given extension.");
			return null;
		}
		//build up the JTable
		final String[] columnNames;
		columnNames = new String[2];
		columnNames[0] = "image thumbnail";
		columnNames[1]= "file name";

		final Object[][] tabledata = new Object[children.length][columnNames.length];
		for (int i = 0; i < children.length; i++)
		{
			tabledata[i][0] =  getButtonCell(i);
			tabledata[i][1] = children[i];

		}		

		// Create a model of the data. 
		javax.swing.table.TableModel dataModel = new javax.swing.table.AbstractTableModel() { 
			public int getColumnCount() { return columnNames.length; } 
			public int getRowCount() { return tabledata.length;} 
			public Object getValueAt(int row, int col) {return tabledata[row][col];} 
			public String getColumnName(int column) {return columnNames[column];} 
			public Class getColumnClass(int c) {return getValueAt(0, c).getClass();} 
			public boolean isCellEditable(int row, int col) {return false ;} 
			public void setValueAt(Object aValue, int row, int column) 
			{ tabledata[row][column] = aValue; 
			fireTableCellUpdated(row, column); //needed if data could change
			} 
		}; 

		TableCellRenderer defaultRenderer;
		table = new JTable(dataModel);
		table.setRowHeight(THUMB_HEIGHT + 4); 
		defaultRenderer = table.getDefaultRenderer(JButton.class);
		table.setDefaultRenderer(JButton.class,
				new JTableButtonRenderer(defaultRenderer));
		scrollPane = new JScrollPane(table);
		scrollPane.setOpaque(true); //content panes must be opaque
		table.addMouseListener(new JTableButtonMouseListener(table));

		return scrollPane;
	}
	
	/**
	 * For hierarchical directory structure.
	 * 
	 * @param directory
	 * @param ext
	 * @return
	 */
	public JScrollPane buildImageTableFromSubdirectories(String directory, String ext) throws Exception
	{
		this.directory = directory;
		problem = new annotool.io.DataInput(directory, ext, true);
		children = problem.getChildren();

		if (children == null)
		{
			javax.swing.JOptionPane.showMessageDialog(null,"Error: File path may be incorrect.");
			return null;
		}	
		if (children.length == 0)
		{
			javax.swing.JOptionPane.showMessageDialog(null,"There is no image with the given extension.");
			return null;
		}
		
		annotations = problem.getAnnotations();
		numOfAnno = annotations.size();
		classNames = problem.getClassNames();
		
		//build up the JTable
		final String[] columnNames;
		columnNames = new String[2];
		columnNames[0] = "image thumbnail";
		columnNames[1]= "file name";

		final Object[][] tabledata = new Object[children.length][columnNames.length];
		for (int i = 0; i < children.length; i++)
		{
			tabledata[i][0] =  getButtonCell(i);
			tabledata[i][1] = children[i];

		}		

		// Create a model of the data. 
		javax.swing.table.TableModel dataModel = new javax.swing.table.AbstractTableModel() { 
			public int getColumnCount() { return columnNames.length; } 
			public int getRowCount() { return tabledata.length;} 
			public Object getValueAt(int row, int col) {return tabledata[row][col];} 
			public String getColumnName(int column) {return columnNames[column];} 
			public Class getColumnClass(int c) {return getValueAt(0, c).getClass();} 
			public boolean isCellEditable(int row, int col) {return false ;} 
			public void setValueAt(Object aValue, int row, int column) 
			{ tabledata[row][column] = aValue; 
			fireTableCellUpdated(row, column); //needed if data could change
			} 
		}; 

		TableCellRenderer defaultRenderer;
		table = new JTable(dataModel);
		table.setRowHeight(THUMB_HEIGHT + 4); 
		defaultRenderer = table.getDefaultRenderer(JButton.class);
		table.setDefaultRenderer(JButton.class,
				new JTableButtonRenderer(defaultRenderer));
		scrollPane = new JScrollPane(table);
		scrollPane.setOpaque(true); //content panes must be opaque
		table.addMouseListener(new JTableButtonMouseListener(table));

		return scrollPane;
	}

	
	
	//update the table after annotation is done
	public void updateTable(Annotation[][] predictions)
	{
		final String[] columnNames;
		columnNames = new String[numOfAnno+numOfAnno+ 2];
		columnNames[0] = "image thumbnail";
		columnNames[1]= "file name";
		for(int i= 0; i < numOfAnno; i++)
			columnNames[2+i] = annotations.get(i);
		for(int i= numOfAnno; i < numOfAnno+numOfAnno; i++)
			columnNames[2+i] = "annotation " +annotations.get(i-numOfAnno);

		final Object[][] tabledata = new Object[children.length][columnNames.length];
		for (int i = 0; i < children.length; i++)
		{
			tabledata[i][0] =  getButtonCell(i); 
			tabledata[i][1] = children[i];
			for (int j = 0; j < numOfAnno; j++)
				tabledata[i][2+j] = targets[j][i];
			for (int j = numOfAnno; j < numOfAnno+numOfAnno; j++)
				tabledata[i][2+j] = predictions[j-numOfAnno][i].anno;
		}		

		javax.swing.table.TableModel dataModel = new javax.swing.table.AbstractTableModel() { 
			public int getColumnCount() { return columnNames.length; } 
			public int getRowCount() { return tabledata.length;} 
			public Object getValueAt(int row, int col) {return tabledata[row][col];} 
			public String getColumnName(int column) {return columnNames[column];} 
			public Class getColumnClass(int c) {return getValueAt(0, c).getClass();} 
			public boolean isCellEditable(int row, int col) {return false ;} 
			public void setValueAt(Object aValue, int row, int column) 
			{ tabledata[row][column] = aValue; 
			fireTableCellUpdated(row, column); //needed if data could change
			} 
		}; 
		//update the model
		table.setModel(dataModel);
	}
	
	//Overloaded version: added July 19, 2011 : for update after applying model
	public void updateTable(Annotation[][] predictions, String[] modelLabels, boolean[] supportsProb,
			boolean isBinary)
	{
		int numModels = predictions.length;
		final String[] columnNames;
		if(isBinary)
			columnNames = new String[2 + numModels + 1]; //Extra summary column for binary case
		else
			columnNames = new String[2 + numModels];
		
		columnNames[0] = "image thumbnail";
		columnNames[1]= "file name";
		for(int i= 0; i < numModels; i++)
			columnNames[2 + i] = modelLabels[i];
		
		if(isBinary)
			columnNames[2 + numModels] = "Summary";
		
		final Object[][] tabledata = new Object[children.length][columnNames.length];
		for (int i = 0; i < children.length; i++)
		{
			tabledata[i][0] =  getButtonCell(i); 
			tabledata[i][1] = children[i];
			String summary = "";
			for (int j = 0; j < numModels; j++) {
				if(supportsProb[j]) {
					tabledata[i][2+j] = predictions[j][i].anno + String.format(" (%.2f%%)", 100 * predictions[j][i].prob);
				}
				else
					tabledata[i][2+j] = predictions[j][i].anno;
				
				if(isBinary && predictions[j][i].anno == 1)
					summary += modelLabels[j] + " ";
			}
			
			if(isBinary)
				tabledata[i][2 + numModels] = summary;
		}		

		javax.swing.table.TableModel dataModel = new javax.swing.table.AbstractTableModel() { 
			public int getColumnCount() { return columnNames.length; } 
			public int getRowCount() { return tabledata.length;} 
			public Object getValueAt(int row, int col) {return tabledata[row][col];} 
			public String getColumnName(int column) {return columnNames[column];} 
			public Class getColumnClass(int c) {return getValueAt(0, c).getClass();} 
			public boolean isCellEditable(int row, int col) {return false ;} 
			public void setValueAt(Object aValue, int row, int column) 
			{ tabledata[row][column] = aValue; 
			fireTableCellUpdated(row, column); //needed if data could change
			} 
		}; 
		//update the model
		table.setModel(dataModel);
	}

	//called after a button click in the table
	private void displayImageInPanel(int index)
	{
		if(problem.is3D(Annotator.dir+children[0])) {
			ImagePlus imp = ij.IJ.openImage(directory+children[index]);
			new StackConverter(imp).convertToRGB();
			//new StackConverter(imp).convertToGray8();
			
			// Create a universe
			Image3DUniverse univ = new Image3DUniverse();
			
			// Add the image as an isosurface
			Content c = univ.addVoltex(imp);
			
			//Show
			univ.show();
		}
		else {
			ImagePlus imgp = new ImagePlus(directory+children[index]);
			imgp.show();
		}
	}

	//inner class to help render a button in a table
	class JTableButtonRenderer implements TableCellRenderer {
		private TableCellRenderer defaultRenderer;

		public JTableButtonRenderer(TableCellRenderer renderer) {
			defaultRenderer = renderer;
		}

		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected,
				boolean hasFocus,
				int row, int column)
		{
			if(value instanceof Component)
				return (Component)value;
			return defaultRenderer.getTableCellRendererComponent(
					table, value, isSelected, hasFocus, row, column);
		}
	}
	
	//modified from thumbnail_maker at http://rsb.info.nih.gov/ij/plugins/download/Thumbnail_Maker.java Wayne Rasband
	/* Converts the specified image to an image 8-bit indexed color image of the specied width. */
	java.awt.Image makeThumbnail(ImagePlus imp, int thumbnailHeight) {
		if (imp==null)
			return null;
		ImageProcessor ip = imp.getProcessor();
		int width = ip.getWidth();
		int height = ip.getHeight();
		if (imp.getType()==ImagePlus.COLOR_256)
			ip = ip.convertToRGB();
		ip.smooth();
		ip.setInterpolate(true);
		ImageProcessor ip2 = ip.resize(thumbnailHeight*width/height, thumbnailHeight);
		ip.reset();
		if (ip2 instanceof ShortProcessor || ip2 instanceof FloatProcessor)
			ip2 = ip2.convertToByte(true);
		java.awt.Image img = reduceColors(ip2, 256);
		return img;
	}
	
	java.awt.Image reduceColors(ImageProcessor ip, int nColors) {
		if (ip instanceof ByteProcessor && nColors==256)
			return ip.createImage();
		ip = ip.convertToRGB();
		MedianCut mc = new MedianCut((int[])ip.getPixels(), ip.getWidth(), ip.getHeight());
		java.awt.Image img = mc.convert(nColors);
		return img;
	}

	//build up the table cells that shows the image by clicking the button
	//to speed up, maybe can precompute the thumbnails when the app is first loaded.
	JButton getButtonCell(int i)
	{
		//return new JButton(new ImageIcon(showIcon));
		
		if(problem.is3D(Annotator.dir+children[0]) || children.length > 100)
		{
			//if image is 3D or the set is big, don't show thumbnail
			return new JButton(new ImageIcon(showIcon));
		}
		else
		{	
		 JButton b = new JButton(new ImageIcon(makeThumbnail(new ImagePlus(directory+children[i]), THUMB_HEIGHT)));
		 b.setContentAreaFilled(false);
		 //b.setOpaque(true);
		 return b;
		}
	}

	//an inner class to help the buttons in the table to get the event
	//modified from http://www.devx.com/getHelpOn/10MinuteSolution/20425
	//by Daniel F. Savarese
	class JTableButtonMouseListener implements MouseListener {
		private JTable innertable;

		private void forwardEventToButton(MouseEvent e) {
			TableColumnModel columnModel = innertable.getColumnModel();
			int column = columnModel.getColumnIndexAtX(e.getX());
			int row    = e.getY() / innertable.getRowHeight();
			Object value;
			JButton button;
			MouseEvent buttonEvent;

			if(row >= innertable.getRowCount() || row < 0 ||
					column >= innertable.getColumnCount() || column < 0)
				return;

			value = innertable.getValueAt(row, column);

			if(!(value instanceof JButton))
				return;

			//get a mouse event
			button = (JButton)value;
			System.out.println("get a button event at row " + row);

			buttonEvent =
				(MouseEvent)SwingUtilities.convertMouseEvent(innertable, e, button);
			button.dispatchEvent(buttonEvent);
			innertable.repaint();

			displayImageInPanel(row);
			// This is necessary so that when a button is pressed and released
			// it gets rendered properly.  Otherwise, the button may still appear
			// pressed down when it has been released.
		}

		public JTableButtonMouseListener(JTable table) {
			innertable = table;
		}

		public void mouseClicked(MouseEvent e) {
			forwardEventToButton(e);
		}

		public void mouseEntered(MouseEvent e) {
			//forwardEventToButton(e);
		}

		public void mouseExited(MouseEvent e) {
			//forwardEventToButton(e);
		}

		public void mousePressed(MouseEvent e) {
			//forwardEventToButton(e);
		}

		public void mouseReleased(MouseEvent e) {
			//forwardEventToButton(e);
		}
	} //end of innerclass

	public String[] getChildren() {
		return children;
	}

	public HashMap<String, String> getClassNames() {
		return classNames;
	}
	
	public int[] getSelectedRows() {
		if (table != null)
			return table.getSelectedRows();
		
		return null;
	}
	
	//Added 4/16/2012: to use same problem for loading images and to use with algorithms
	public annotool.io.DataInput getProblem() {
		return problem;
	}
	
	public boolean isColor() throws Exception {
		if(problem == null | children == null)
			throw new Exception("Data not read yet.");
		
		return (problem.isColor(this.directory + children[0]));
	}

	public boolean is3D() throws Exception {
		if(problem == null | children == null)
			throw new Exception("Data not read yet.");
		
		return (problem.is3D(this.directory + children[0]));
	}

}
