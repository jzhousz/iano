package annotool.gui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import annotool.Annotation;
import annotool.Annotator;
import annotool.ComboFeatures;
import annotool.classify.Classifier;
import annotool.classify.Validator;
import annotool.gui.model.Chain;
import annotool.gui.model.ChainFilter;
import annotool.gui.model.ChainTableModel;
import annotool.gui.model.ClassifierInfo;
import annotool.gui.model.Extractor;
import annotool.gui.model.ModelFilter;
import annotool.gui.model.ModelSaver;
import annotool.gui.model.Selector;
import annotool.gui.model.Utils;
import annotool.io.ChainIO;
import annotool.io.ChainModel;
import annotool.io.DataInput;

/**
 * Right side panel of Auto Comparison Mode.
 * It displays the chains of algorithms constructed and executes them in either TT or CV modes.
 * 
 * @author Santosh
 *
 */
public class ChainPanel extends JPanel implements ActionListener, ListSelectionListener, TableModelListener, Runnable{
	private JPanel pnlMain, pnlDetail,
	pnlTable, pnlControl,
	pnlButton,
	pnlSouth;
	private JTable tblChain = null;
	private JScrollPane scrollPane = null;
	
	//private static final int DIMUPPERBOUND = 600000;
	
	private JButton btnNew, btnRemove, 
	btnSaveChain, btnLoadChain, 
	btnRun, btnSaveModel,
	btnApplyModel, btnCopyChain, btnStop, btnTips,
	btnDefaultChains;

	private JCheckBox checkSelect;
	private ChainTableModel tableModel = new ChainTableModel();

	public boolean cFlag;
	//Details
	JTextArea taDetail = new JTextArea(6,30);
	JScrollPane detailPane = new JScrollPane(taDetail);
	AutoCompFrame gui = null;
	JFileChooser fileChooser = null;

	private Thread thread = null;
	private boolean isRunning = false;
	private String channel;

	JProgressBar bar = null;
	AnnOutputPanel pnlOutput = null;

	TableColumnAdjuster tca = null;

	//Indices for table columns
	public final static int COL_CHECK = 0;	
	public final static int COL_NAME = 1;	
	public final static int COL_CHAIN = 2;

	//To keep track of the best model for each genetic line
	ChainModel[] chainModels = null;

	boolean executed = false;	//Set to true if at least one chain is successfully executed

	//Keep features to be dumped into chain file
	int imgWidth;
	int imgHeight;
	int imgDepth = 1;  //for 3D ROI 8/13/12
	int imgStackSize; //for 3D image
	boolean is3d;

	public ChainPanel(AutoCompFrame gui, String channel, AnnOutputPanel pnlOutput, boolean chFlag, boolean is3D) {
		this.channel = channel;		
		this.gui = gui;
		gui.setButtonsEnabled(false);
		this.pnlOutput = pnlOutput;
		is3d = is3D;
		cFlag = chFlag;

		fileChooser = new JFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(false); //Remove "all files" options from file chooser

		this.setLayout(new BorderLayout());
		this.setBorder(new EmptyBorder(10, 10, 10, 10));

		pnlTable = new JPanel(new BorderLayout());

		pnlDetail = new JPanel(new BorderLayout());
		pnlDetail.setBorder(new CompoundBorder(new TitledBorder(null, "Selected Chain Detail", 
				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(10, 10, 10, 10)));
		pnlDetail.add(detailPane, BorderLayout.CENTER);
		pnlDetail.setPreferredSize(new java.awt.Dimension(300, 200));
		taDetail.setMargin(new Insets(10,10,10,10));
		taDetail.setEditable(false);

		tblChain = new JTable(tableModel){
			//preferred size or the viewport size, whichever is greater
			//Needed since we are using auto resize off
			public boolean getScrollableTracksViewportWidth()
			{            	
				return getPreferredSize().width < getParent().getWidth();
			}
		};
		tblChain.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblChain.getSelectionModel().addListSelectionListener(this);
		tblChain.getModel().addTableModelListener(this);

		tblChain.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 		//To show horizontal scrollbars
		tblChain.getColumnModel().getColumn(COL_CHECK).setMaxWidth(30);
		tblChain.getColumnModel().getColumn(COL_CHECK).setMinWidth(30);
		tblChain.getColumnModel().getColumn(COL_NAME).setMinWidth(80);

		//Left justify the header text
		((DefaultTableCellRenderer)tblChain.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

		//For adjusting table columns to fit content width
		tca = new TableColumnAdjuster(tblChain);
		tca.setOnlyAdjustLarger(false);
		tca.adjustColumns();

		scrollPane = new JScrollPane(tblChain);
		pnlTable.add(scrollPane, BorderLayout.CENTER);

		btnNew = new JButton("New");
		btnNew.addActionListener(this);
		btnRemove = new JButton("Remove");
		btnRemove.setEnabled(false);
		btnRemove.addActionListener(this);		
		btnSaveChain = new JButton("Save Chains");
		btnSaveChain.setEnabled(false);
		btnSaveChain.addActionListener(this);
		btnLoadChain = new JButton("Load Chains");
		btnLoadChain.addActionListener(this);
		btnRun = new JButton("Run");
		btnRun.setEnabled(false);
		btnRun.addActionListener(this);
		btnSaveModel = new JButton("Save Model");
		btnSaveModel.setToolTipText("Save best model from comparision.");
		btnSaveModel.setEnabled(false);
		btnSaveModel.addActionListener(this);
		btnApplyModel = new JButton("Annotate");
		btnApplyModel.setEnabled(false);
		btnApplyModel.addActionListener(this);
		btnTips = new JButton("Tips");
		btnTips.setEnabled(true);
		btnTips.addActionListener(this);
		
		btnCopyChain = new JButton("Copy Selected");
		btnCopyChain.addActionListener(this);
		btnCopyChain.setEnabled(false);
		btnStop = new JButton("Stop");
		btnStop.addActionListener(this);
		btnStop.setEnabled(false);
		btnDefaultChains = new JButton("Default Chains");
		btnDefaultChains.setToolTipText("Load the default chains into the table.");
		btnDefaultChains.addActionListener(this);
		btnDefaultChains.setEnabled(true);

		pnlButton = new JPanel(new GridLayout(6, 2)); // Rows by columns (6, 2)
		pnlButton.add(btnNew);
		pnlButton.add(btnRemove);
		pnlButton.add(btnSaveChain);
		pnlButton.add(btnLoadChain);
		pnlButton.add(btnRun);
		pnlButton.add(btnSaveModel);
		pnlButton.add(btnApplyModel);
		pnlButton.add(btnTips);

		pnlButton.add(btnCopyChain);
		pnlButton.add(btnStop);
		pnlButton.add(btnDefaultChains);

		pnlControl = new JPanel();
		pnlControl.setLayout(new FlowLayout());
		pnlControl.add(pnlButton);	

		pnlMain = new JPanel(new BorderLayout());
		pnlMain.setBorder(new CompoundBorder(new TitledBorder(null, "Algorithm Chains", 
				TitledBorder.LEFT, TitledBorder.TOP), new EmptyBorder(10, 10, 10, 10)));
		pnlMain.add(pnlTable, BorderLayout.CENTER);
		pnlMain.add(pnlControl, BorderLayout.EAST);

		checkSelect = new JCheckBox("Select All");
		checkSelect.addActionListener(this);
		pnlMain.add(checkSelect, BorderLayout.NORTH);

		bar = new JProgressBar(0, 100);
		bar.setValue(0);
		bar.setStringPainted(true);

		pnlSouth  = new JPanel(new BorderLayout());
		pnlSouth.add(bar, BorderLayout.SOUTH);
		pnlSouth.add(pnlDetail, BorderLayout.CENTER);

		this.add(pnlMain, BorderLayout.CENTER);
		this.add(pnlSouth, BorderLayout.SOUTH);
		
		
	}

	public void actionPerformed(ActionEvent ev) {
		if(ev.getSource().equals(btnNew)) {
			//Check if last chain in the table is complete
			int size = tblChain.getRowCount();
			if(size > 0) {
				Chain lastChain = (Chain)tblChain.getValueAt(size - 1, COL_CHAIN);
				if(!lastChain.isComplete()) {
					JOptionPane.showMessageDialog(this,
							"The last chain is not yet complete. Classifier is required.", 
							"Incomplete Chain",
							JOptionPane.INFORMATION_MESSAGE);
					return;
				}
			}

			String name = createChainName();
			Chain chain = new Chain(name);
			Object[] rowData = {new Boolean(false), name, chain};//chain.setName((String)tableModel.getValueAt(insertIndex, COL_NAME));
			tableModel.insertNewRow(rowData);

			//Select the newly inserted row
			tblChain.changeSelection(tableModel.getRowCount() - 1, COL_CHAIN, false, false);

			//Put the name column in edit mode
			tblChain.editCellAt(tableModel.getRowCount() - 1, COL_NAME);

			setButtonState();
		}
		else if(ev.getSource().equals(btnRemove)) {
			// Check if there is a checked chain.
			// If there is, then remove it.
			if (hasSelectedChain()) {
				int i = tblChain.getRowCount() - 1;
				for (; i >= 0; i--)
				{
					if (((Boolean) tblChain.getValueAt(i, COL_CHECK)).booleanValue())
					{
						System.out.println("Removing row: " + i);
						tableModel.removeRow(i);
					}
				}
				tca.adjustColumns();

				taDetail.setText("");
				setButtonState();
			}
			// If there is no chain checked, pop up a message box.
			else {
				String message = "There is no chain selected for removal, select one and try again.";
				JOptionPane.showMessageDialog(null, message, "Select Chain", JOptionPane.INFORMATION_MESSAGE);
			}

		}
		else if(ev.getSource().equals(btnSaveChain)) {
			//Save chains to file
			ArrayList<Chain> chainList = new ArrayList<Chain>();

			for(int i = 0; i < tableModel.getRowCount(); i++) {
				chainList.add((Chain)tableModel.getValueAt(i, COL_CHAIN));
			}
			if(chainList.isEmpty()) {
				JOptionPane.showMessageDialog(this,
						"There shoulb be at least one chain to save.", 
						"Empty List",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			fileChooser.resetChoosableFileFilters();
			fileChooser.addChoosableFileFilter(new ChainFilter());
			int returnVal = fileChooser.showSaveDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {	        	
				ChainIO chainSaver = new ChainIO();
				try {
					File file = fileChooser.getSelectedFile();
					//Add extension if not present
					String filePath = file.getPath();
					if(!filePath.toLowerCase().endsWith("." + Utils.CHAIN_EXT)) {
						file = new File(filePath + "." + Utils.CHAIN_EXT);
					}
					chainSaver.save(file, chainList);
					pnlOutput.setOutput("Chain saved to " + file.getAbsolutePath());
				}
				catch (IOException ex) {
					System.out.println("Exception thrown while writing chain list to file.");
					ex.printStackTrace();
					pnlOutput.setOutput("ERROR: Save failed. (I/O Exception)");
				}
			}
		}
		else if(ev.getSource().equals(btnLoadChain)) {
			int size = tblChain.getRowCount();
			if (size > 0)
			{
				Chain lastChain = (Chain)tblChain.getValueAt(size - 1, COL_CHAIN);
				if(!lastChain.isComplete()) {
					JOptionPane.showMessageDialog(this,
							"The last chain is not yet complete. Classifier is required.", 
							"Incomplete Chain",
							JOptionPane.INFORMATION_MESSAGE);
					return;
				}
			}
			fileChooser.resetChoosableFileFilters();
			fileChooser.addChoosableFileFilter(new ChainFilter());
			int returnVal = fileChooser.showOpenDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
			
				ChainIO chainLoader = new ChainIO();
				try {
					ArrayList<Chain> chainList = chainLoader.load(file);
					//tableModel.removeAll();
					taDetail.setText("");
					for(Chain chain : chainList) {
						Object[] rowData = {new Boolean(false), chain.getName(), chain};		//TODO: load and use chain names also
						tableModel.insertNewRow(rowData);	        			
					}
					pnlOutput.setOutput("Chain successfully loaded.");
				}
				catch (Exception ex) {
					System.out.println("Exception thrown while loading chain list from file.");
					ex.printStackTrace();
					pnlOutput.setOutput("ERROR: Load failed.");
				}

				tca.adjustColumns();

				//Enable/disable buttons based on whether has rows or not
				setButtonState();
			}
		}
		else if(ev.getSource().equals(btnRun)) {
			//Check if the last chain is complete
			int size = tblChain.getRowCount();
			if(size > 0) {
				Chain lastChain = (Chain)tblChain.getValueAt(size - 1, COL_CHAIN);
				if(!lastChain.isComplete()) {
					JOptionPane.showMessageDialog(this,
							"The last chain is not yet complete. Classifier is required.", 
							"Incomplete Chain",
							JOptionPane.INFORMATION_MESSAGE);
					return;
				}

				// Check to make sure that there are no same names
				for (int i = 0; i < size; i++)
				{
					for ( int j = 0; j < size; j++)
					{
						if (i != j)
						{	
							String name1 = (String)tblChain.getValueAt(i, COL_NAME);
							String name2 = (String)tblChain.getValueAt(j, COL_NAME);
							if ( name1.equals(name2) )
							{
								JOptionPane.showMessageDialog(this,
										"Two or more chains have the same name, rename them to continue.", 
										"Conflicting Chains",
										JOptionPane.INFORMATION_MESSAGE);
								return;
							}
						}
					}
				}
				
				/*
				// Check the chains for dimension issues.
				int dimension = 0;
				for (int i = 0; i < size; i++)
				{
					//Set the dimension of the problem.
					try {
						dimension = gui.trainingProblem.getWidth() * gui.trainingProblem.getHeight() * gui.trainingProblem.getStackSize();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					
					//Get the currently selected extractor and selector
					boolean extractor = ((Chain)tblChain.getValueAt(i, COL_CHAIN)).hasExtractors();			
					boolean selector = ((Chain)tblChain.getValueAt(i, COL_CHAIN)).hasSelectors();	
					
					// If the dimension is out of bounds and there is no extractor or selector, have a pop up dialogue
					// for the user.
					if (!(AlgorithmValidation.isWithinBounds( dimension, DIMUPPERBOUND, extractor, selector)) )
					{
						// Prints out the dimension to the console
						System.out.println("Dimension: " + dimension);
						
						String message = "The dimension is too high and might cause a heap space error. \nIt is recomended that you add an extractor or a selector to avoid this. Continue?";
						
						// The actual pop up dialog. 
						int diag_result = JOptionPane.showConfirmDialog(null, message);
						
						// If yes, then continue normally.
						if (diag_result == JOptionPane.YES_OPTION)
						{
			            	break;
						}
						// If no, don't do anything.
						else if (diag_result == JOptionPane.NO_OPTION)
						{
							return;
						}
						// Also don't do anything. 
						else
						{
							return;
						}
					}
				}*/
				
				if (thread == null)  {
					thread = new Thread(this);
					isRunning = true;
					thread.start();
				}
			}
		}
		else if(ev.getSource().equals(btnSaveModel)) {
			if(thread == null) {
				//Show confirmation dialog
				int choice = JOptionPane.showConfirmDialog(this,
						"Depending on the algorithm, saving a model can take a while to finish.\n" + 
						"If two chains have the same result, the first one will be saved\n" + 
						"Do you want to continue?",
						"Information",
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.INFORMATION_MESSAGE);

				if(choice == JOptionPane.CANCEL_OPTION)
					return;

				fileChooser.resetChoosableFileFilters();
				fileChooser.addChoosableFileFilter(new ModelFilter());
				int returnVal = fileChooser.showSaveDialog(this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					pnlOutput.setOutput("Saving Model...");
					File file = fileChooser.getSelectedFile();

					//Reset progress bar
					bar.setValue(0);

					JButton[] buttons = {btnRun, btnSaveModel}; 
					ModelSaver saver = new ModelSaver(bar, pnlOutput, buttons, chainModels, file);
					Thread t1 = new Thread(saver);
					t1.start();
				}
			}
			else
				pnlOutput.setOutput("Cannot save model during processing.");
		}
		else if(ev.getSource().equals(btnApplyModel)) {
			if(thread == null) {
				int choice = JOptionPane.showConfirmDialog(this,
						"This will close all open windows.\n" +
						"The best model from the comparison will be used for the annotation process.\n" + 
						"All other unsaved progress will be lost.\n" +
						"Do you wish to continue?",
						"Information",
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.INFORMATION_MESSAGE);

				if(choice == JOptionPane.CANCEL_OPTION)
					return;

				//Set the flag that indicates this frame as annotation firing frame and then fire close window
				gui.applyModelFired = true;
				gui.setChainModels(chainModels);
				gui.pullThePlug();				
			}
		}
		else if(ev.getSource().equals(checkSelect)){
			Object item = new Object();
			if (checkSelect.isSelected()) //If the button is selected, set to true
				item = true;
			else // Else set to false.
				item = false;
			
			for (int i = 0; i < tblChain.getRowCount(); i++) //Sets all rows to the value in item.
				{
				tblChain.setValueAt(item, i, COL_CHECK);
				}
		}
		else if(ev.getSource().equals(btnCopyChain)){
			if (tblChain.getSelectedRow() == -1){
				System.out.println("No row selected.");
				return;
				}
			else
				{
				String name = createChainName();
				Chain chain = new Chain(name);
				chain.copyChain((Chain)tblChain.getValueAt(tblChain.getSelectedRow(), COL_CHAIN));
				Object[] rowData = {new Boolean(false), name, chain};
			
				tableModel.insertNewRow(rowData);
				//Select the newly inserted row
				tblChain.changeSelection(tableModel.getRowCount() - 1, COL_CHAIN, false, false);
				}
		}
		else if(ev.getSource().equals(btnStop)){
			if (isRunning)
			{
				Object[] options = {"Yes","No"};
			
				int selection = JOptionPane.showOptionDialog(null, "Do you want to stop the running chains? Note: Some algorithms may take a while to complete.", 
										"Stop", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);
			
				if (selection == 0) {
						stopChains();
				}
				else if (selection == 1) {
					System.out.println("Nothing happens");
					}
			}
			else {
				JOptionPane.showMessageDialog(null, "The chains are not running!");
			}
		}
		else if(ev.getSource().equals(btnTips)) {
			// Create the new editor pane for the tips
            final JEditorPane editorPane = new JEditorPane();
            
            // The popup
			JPopupMenu popup = new JPopupMenu();
            popup.setLayout(new BorderLayout());
            popup.add(new JScrollPane(editorPane));
            
            // Get the preferred size.
            //Dimension d = popup.getPreferredSize();
            
            // Set the URI of the file
            final String biocat_help = "resources/biocat_help.html";
            java.net.URL page = this.getClass().getResource("/"+biocat_help);
            String set_editor_pane = null;
            try {
				if (page != null)
				{
					set_editor_pane = page.toURI().toURL().toString();
				}
				else
					set_editor_pane = biocat_help;
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
			}
            
            popup.setPopupSize(500, 300);
            Dimension s = btnTips.getSize();
            editorPane.setEditable(false);
            editorPane.setEnabled(true);
            editorPane.setContentType("text/html");
            
            // Try to set the editor pane to the file.
            try {
				editorPane.setPage(set_editor_pane);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
            
            // Show the popup
            popup.show(btnTips, s.width / 2, s.height / 2);
            
            // Add a hyperlink listener to the links in the editor pane.
            editorPane.addHyperlinkListener(new HyperlinkListener() {
            	@Override
            	public void hyperlinkUpdate(HyperlinkEvent hle) {
            		if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
                        System.out.println(hle.getURL());
                        Desktop desktop = Desktop.getDesktop();
                        try {
                            desktop.browse(hle.getURL().toURI());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
            		}
            	}
            });
		}
		else if(ev.getSource().equals(btnDefaultChains)) {
			// Make sure that the last chain is completed
			int size = tblChain.getRowCount();
			if (size > 0)
			{
				Chain lastChain = (Chain)tblChain.getValueAt(size - 1, COL_CHAIN);
				if(!lastChain.isComplete()) {
					JOptionPane.showMessageDialog(this,
							"The last chain is not yet complete. Classifier is required.", 
							"Incomplete Chain",
							JOptionPane.INFORMATION_MESSAGE);
					return;
				}
			}
			
			// If it is 3D then imgStackSize will be > 1
			// Else set to 2D
			

			String default_chain = "";
			if (is3d)
				default_chain = "resources/DEFAULT_CHAINS_3D.ichn";
			else
				default_chain = "resources/DEFAULT_CHAINS_2D.ichn";

			// Open the chains and put them in the table.
			ChainIO chainLoader = new ChainIO();
			try {
				
				ArrayList<Chain> chainList = chainLoader.load(this.getClass().getResourceAsStream("/" + default_chain));
				taDetail.setText("");
				for(Chain chain : chainList) {
					Object[] rowData = {new Boolean(false), chain.getName(), chain};
					tableModel.insertNewRow(rowData);	        			
				}
				pnlOutput.setOutput("Chain successfully loaded.");
				}
			catch (Exception ex) {
				System.out.println("Exception thrown while loading chain list from file.");
				ex.printStackTrace();
				pnlOutput.setOutput("ERROR: Load failed.");
			}
			
		
			tca.adjustColumns();
			
			//Enable/disable buttons based on whether has rows or not
			setButtonState();
			
			
		}
		
		
	}
	
	public void stopChains() {
		if (thread != null)
		{
			isRunning = false;
			// We are using this deprecated method because there is no data shared among the chains.
			// We never plan on restarting this thread, so it really doesn't have much effect.
			try 
			{ 
				thread.stop();
			}
			catch (ThreadDeath td) 
			{
				System.out.println("Killing the thread...");
			}
		}
	}
	
	private void setButtonState() {
		if(tableModel.getRowCount() > 0) {
			btnRemove.setEnabled(true);
			btnCopyChain.setEnabled(true);
			btnSaveChain.setEnabled(true);
			gui.setButtonsEnabled(true);
		}
		else {
			btnRemove.setEnabled(false);
			btnCopyChain.setEnabled(false);
			btnSaveChain.setEnabled(false);
			gui.setButtonsEnabled(false);
		}		

		btnRun.setEnabled(hasSelectedChain());
	}
	/*
	 * Checks if there is at least one selected chain.
	 */
	private boolean hasSelectedChain() {		
		for(int row = 0; row < tableModel.getRowCount(); row++) {
			if((Boolean)tableModel.getValueAt(row, COL_CHECK)) 
				return true;
		}
		return false;
	}
	public void valueChanged(ListSelectionEvent ev) {
		if (ev.getValueIsAdjusting()) {
			return;
		}
		showItemDetail();
	}
	public void tableChanged(TableModelEvent ev) {
		int row = ev.getFirstRow();
		int column = ev.getColumn();
		if(row < 0 || column < 0)
			return;
		
		TableModel model = (TableModel)ev.getSource();
		String columnName = model.getColumnName(column);
		if(columnName.equals("Name")) {
			String name = model.getValueAt(row, column).toString();
			Chain chain = (Chain)model.getValueAt(row, COL_CHAIN);
			chain.setName(name);
		}
		else if(columnName.equals("")) {	//If check box state changed, then enable/disable run button
			btnRun.setEnabled(hasSelectedChain());
		}
		//If something gets unchecked by the user, make sure select all is no longer checked
		for (int i = 0; i < tblChain.getRowCount(); i++)
			{
			if (!((Boolean)tblChain.getValueAt(i, COL_CHECK).equals(true)))
					{
					checkSelect.setSelected(false);
					break;
					}
			checkSelect.setSelected(true);
			}
	}
	/**
	 * Adds the supplied extractor to the currently selected chain.
	 * @param ex
	 */
	public void addExtractor(Extractor ex) {
		int currentRow = tblChain.getSelectedRow();
		if(currentRow < 0)
			return;
		Chain chain = (Chain)tblChain.getValueAt(currentRow, COL_CHAIN);
		if(ex.getName().equalsIgnoreCase("None"))						//Clear extractors if 'None' selected
			chain.clearExtractors();
		else
			chain.addExtractor(ex);


		tca.adjustColumns();

		tblChain.repaint();
		showItemDetail();
	}
	/**
	 * Adds the supplied selector to the currently selected chain.
	 * @param sel
	 */
	public void addSelector(Selector sel) {
		int currentRow = tblChain.getSelectedRow();
		if(currentRow < 0)
			return;
		Chain chain = (Chain)tblChain.getValueAt(currentRow, COL_CHAIN);		
		if(sel.getName().equalsIgnoreCase("None"))						//Clear selectors if 'None' selected
			chain.clearSelectors();
		else
			chain.addSelector(sel);

		tca.adjustColumns();

		tblChain.repaint();
		showItemDetail();
	}
	
	/**
	 * Added 1/16/2014
	 * Adds the supplied Classifier to the currently selected chain.
	 * @param Class
	 */
	public void addClassifierInfo(ClassifierInfo Class) {
		int currentRow = tblChain.getSelectedRow();
		if(currentRow < 0)
			return;
		Chain chain = (Chain)tblChain.getValueAt(currentRow, COL_CHAIN);		
		if(Class.getName().equalsIgnoreCase("None"))						//Clear Classfifer if 'None' selected
			chain.clearClassifier();
		
		else
			chain.addClassifierInfo(Class);
		
		

		tca.adjustColumns();

		tblChain.repaint();
		showItemDetail();
	}
	

	/* Removed 1/16/2014
	
	**
	 * Adds the supplied classifier to the currently selected chain.
	 * 
	 * @param name	Name of the classifier
	 * @param params	HashMap of parameters for the classifier
	 * @param className	Class name to use for the classifier
	 * @param externalPath	External path (if loaded from plugin)
	 *
	public void addClassifier(String name, HashMap<String, String> params, String className, String externalPath) {
		int currentRow = tblChain.getSelectedRow();
		if(currentRow < 0)
			return;
		Chain chain = (Chain)tblChain.getValueAt(currentRow, COL_CHAIN);
		chain.setClassifier(name);
		chain.setClassParams(params);
		chain.setClassifierClassName(className);
		chain.setClassifierExternalPath(externalPath);

		tca.adjustColumns();

		tblChain.repaint();
		showItemDetail();
	}
	
	*/
	
	/*
	 * Adds the supplied Ensemble to the currently selected chain.
	 * 
	 * @param name	Name of the Ensemble
	 * @param params	HashMap of parameters for the Ensemble
	 * @param className	Class name to use for the Ensemble
	 * @param externalPath	External path (if loaded from plugin)
	 */
	
	/*
	public void addEnsemble(String name, HashMap<String, String> params, String ensName, String externalPath) {
		int currentRow = tblChain.getSelectedRow();
		if(currentRow < 0)
			return;
		Chain chain = (Chain)tblChain.getValueAt(currentRow, COL_CHAIN);
		
		chain.setEnsemble(name);
		chain.setEnsParams(params);
		chain.setEnsembleClassName(ensName);
		chain.setEnsembleExternalPath(externalPath);
		
		tca.adjustColumns();

		tblChain.repaint();
		showItemDetail();
	}
	*/
	
	public Boolean isEns()
	{

		Chain chain = (Chain)tblChain.getValueAt(tblChain.getSelectedRow(), COL_CHAIN);	
		
		return chain.isEns();
	}
	
	public ArrayList<ClassifierInfo> getClassifierInfo()
	{

		Chain chain = (Chain)tblChain.getValueAt(tblChain.getSelectedRow(), COL_CHAIN);	
		
		return chain.getClassifierInfo();
	}
	
	/**
	 * Displays the details for the algorithms in the selected chain from the table
	 */
	private void showItemDetail() {
		int currentRow = tblChain.getSelectedRow();
		if(currentRow < 0)
			return;

		final Chain chain = (Chain)tblChain.getValueAt(currentRow, COL_CHAIN);
		taDetail.setText("");
		if(chain.getExtractors().size() > 0) {
			taDetail.setText(taDetail.getText() + "-------------------------------------------------------------------------------\n");
			taDetail.setText(taDetail.getText() + "FEATURE EXTRACTOR(S):\n");
			taDetail.setText(taDetail.getText() + "-------------------------------------------------------------------------------\n");
			for(Extractor ex : chain.getExtractors()) {
				taDetail.setText(taDetail.getText() + ex.getName() + "\n");
				for (String parameter : ex.getParams().keySet()) {
					taDetail.setText(taDetail.getText() + parameter + "=" + ex.getParams().get(parameter) + "\n");
				}
				taDetail.setText(taDetail.getText() + "\n");
			}
		}
		if(chain.getSelectors().size() > 0) {
			taDetail.setText(taDetail.getText() + "-------------------------------------------------------------------------------\n");
			taDetail.setText(taDetail.getText() + "FEATURE SELECTOR (S):\n");
			taDetail.setText(taDetail.getText() + "-------------------------------------------------------------------------------\n");
			for(Selector sel : chain.getSelectors()) {
				taDetail.setText(taDetail.getText() + sel.getName() + "\n");
				for (String parameter : sel.getParams().keySet()) {
					taDetail.setText(taDetail.getText() + parameter + "=" + sel.getParams().get(parameter) + "\n");
				}
				taDetail.setText(taDetail.getText() + "\n");
			}
		}
		
		//Added 1/16/2014
		if(chain.getClassifierInfo() != null) {
			taDetail.setText(taDetail.getText() + "-------------------------------------------------------------------------------\n");
			taDetail.setText(taDetail.getText() + "CLASSIFIER (S):\n");
			taDetail.setText(taDetail.getText() + "-------------------------------------------------------------------------------\n");
			for(ClassifierInfo cla : chain.getClassifierInfo()) {
				taDetail.setText(taDetail.getText() +  cla.getName() + "\n");
				for (String parameter :  cla.getParams().keySet()) {
					taDetail.setText(taDetail.getText() + parameter + "=" +  cla.getParams().get(parameter) + "\n");
				}
				taDetail.setText(taDetail.getText() + "\n");
			}
	/*		
		if(chain.isEns()) {
			taDetail.setText(taDetail.getText() + "-------------------------------------------------------------------------------\n");
			taDetail.setText(taDetail.getText() + "Ensemble:\n");
			taDetail.setText(taDetail.getText() + "-------------------------------------------------------------------------------\n");
			taDetail.setText(taDetail.getText() + chain.getEnsemble() + "\n");
			for (String parameter : chain.getEnsParams().keySet()) {
				taDetail.setText(taDetail.getText() + parameter + "=" +chain.getEnsParams().get(parameter) + "\n");
				}
			}
		*/	
		}
		
		/* Removed 1/16/2014
		if(chain.getClassifier() != null) {
			taDetail.setText(taDetail.getText() + "-------------------------------------------------------------------------------\n");
			taDetail.setText(taDetail.getText() + "CLASSIFIER:\n");
			taDetail.setText(taDetail.getText() + "-------------------------------------------------------------------------------\n");
			taDetail.setText(taDetail.getText() + chain.getClassifier() + "\n");
			for (String parameter : chain.getClassParams().keySet()) {
				taDetail.setText(taDetail.getText() + parameter + "=" +chain.getClassParams().get(parameter) + "\n");
			}
		}
		*/
		//taDetail.setCaretPosition(taDetail.getText().length());
	}

	@Override
	public void run() {
		//Disable buttons
		btnRun.setEnabled(false);
		btnNew.setEnabled(false);
		btnRemove.setEnabled(false);
		btnLoadChain.setEnabled(false);
		btnSaveModel.setEnabled(false);
		btnApplyModel.setEnabled(false);
		gui.setButtonsEnabled(false);
		tblChain.setEnabled(false);
		btnStop.setEnabled(true);
		btnCopyChain.setEnabled(false);
		
		executed = false;

		//extracted from individual process method e.g. ttrun()
		DataInput trainingProblem = gui.trainingProblem;
		try {
			imgWidth = trainingProblem.getWidth();
			imgHeight = trainingProblem.getHeight();
			imgStackSize = trainingProblem.getStackSize();
			if(imgStackSize > 1)
			{
				if (trainingProblem.getMode() == DataInput.ROIMODE)	
					{
					System.out.println("Inside if...");
					imgDepth = trainingProblem.getDepth();
					System.out.println("imgDepth: " + imgDepth);
					}
				else
					{
					System.out.println("Inside else...");
					imgDepth = imgStackSize;
					System.out.println("imgDepth: " + imgDepth);
					}
			}
		} catch (Exception e) {
			pnlOutput.setOutput("Failed to read width/height/depth/stackSize from the problem.");
			e.printStackTrace();
			return;
		}

		try {
			//Initiate appropriate process
			if(Annotator.output.equals(Annotator.TT)) {				//TT Mode
				ttRun();
			}
			else if(Annotator.output.equals(Annotator.CV)) {			//Cross validation mode
				cvRun();			
			}
		}
		catch (Throwable t) {
			pnlOutput.setOutput("ERROR: " + t.getMessage());
			t.printStackTrace();
		}
		
		isRunning = false;
		thread = null;

		//Enable buttons
		btnRun.setEnabled(true);
		btnNew.setEnabled(true);
		btnRemove.setEnabled(true);
		btnLoadChain.setEnabled(true);
		btnSaveModel.setEnabled(executed);
		btnApplyModel.setEnabled(executed);
		gui.setButtonsEnabled(true);
		tblChain.setEnabled(true);
		btnStop.setEnabled(false);
		btnCopyChain.setEnabled(true);
	}

	//Training/Testing
	private void ttRun() {
		Annotator anno = new Annotator();

		//read images and wrapped into DataInput instances.
		DataInput trainingProblem = gui.trainingProblem;
		DataInput testingProblem = gui.testingProblem;

		//int[] resArr = new int[2]; //place holder for misc results
		ArrayList<String> annoLabels = trainingProblem.getAnnotations();
		HashMap<String, String> classNames = trainingProblem.getClassNames();
		//int[][] trainingTargets = trainingProblem.getTargets();
		//get statistics from training set
		int numOfAnno = annoLabels.size();
		anno.setAnnotationLabels(annoLabels); //why??	        

		//testing set targets
		//int[][] testingTargets = testingProblem.getTargets();

		//Initialize float array to hold rates for each annotation for each selected chain and list of selected chains to pass to result panel
		ArrayList<Chain> selectedChains = getSelectedChains(); 
		float[][] rates = new float[selectedChains.size()][numOfAnno];

		int chainCount = 0;

		//Chain Models to keep track of the best model for each target
		chainModels = new ChainModel[numOfAnno];

		//Initialize common features for chain models
		for(int i = 0; i < numOfAnno; i++) {
			chainModels[i] = new ChainModel();
			//Save information to dump in chain file
			chainModels[i].setImageSet(new File(Annotator.dir).getAbsolutePath());
			chainModels[i].setTestingSet(new File(Annotator.testdir).getAbsolutePath());
			chainModels[i].setMode("Training/Testing");
			chainModels[i].setChannel(channel);
			chainModels[i].setLabel(anno.getAnnotationLabels().get(i));
			chainModels[i].setClassNames(classNames);

			if(is3d)
				chainModels[i].setImageSize(imgWidth + "x" + imgHeight + "x" + imgDepth);
			else
				chainModels[i].setImageSize(imgWidth + "x" + imgHeight);
		}

		//go through the chains one by one
		for(int row = 0; row < tableModel.getRowCount(); row++) {
			System.gc(); //do clean up before starting a chain
			//Only use the checked chains
			if(!(Boolean)tableModel.getValueAt(row, COL_CHECK))
				continue; 			    	
			selectRow(row);
			//feature extraction.
			if (!setProgress(30))
				return;
			Chain chain = (Chain)tableModel.getValueAt(row, COL_CHAIN);//Second column is chain object
			pnlOutput.setOutput("Processing " + chain.getName() + ":");	    	
			//Chain list loaded from file may have incomplete chain in the middle if the file has been tampered with
			if(!chain.isComplete()) 
				pnlOutput.setOutput("Incomplete chain encountered. Chain = " + chain.getName());

			rates[chainCount] = ttRunBody(chain, anno, trainingProblem, testingProblem);
			chainCount++;
		}//End of loop for chains

		//Display result
		if(executed)	//Display result if at least one chain is executed
			gui.addTab("Auto Comparison Results", rates, anno.getAnnotationLabels(), selectedChains, imgWidth, imgHeight, imgDepth, is3d, channel, cFlag);
	}

	//process one chain
	public float[] ttRunBody(Chain chain, Annotator anno, DataInput trainingProblem, DataInput testingProblem) {	
		int[][] trainingTargets = trainingProblem.getTargets();
		int[][] testingTargets = testingProblem.getTargets();

		String extractor = "None";
		HashMap<String, String> params = new HashMap<String, String>();
		String externalPath = null;
		int numOfAnno = trainingProblem.getAnnotations().size();
		float rates[] = new float[numOfAnno];
		int numExtractors = chain.getExtractors().size();
		float[][][] exTrainFeatures = new float[numExtractors][][];
		float[][][] exTestFeatures = new float[numExtractors][][];

		int trainSize = 0, testSize = 0;	//To keep track of total size
		boolean extracted = true;
		if (gui != null) pnlOutput.setOutput("Extracting features...");

		for(int exIndex=0; exIndex < numExtractors; exIndex++) {
			
			//add more checking on progress and whether to stop.3/13
			if(gui != null) //extraction: 30-50
				if (!setProgress(30+exIndex*20/numExtractors)) {
					return null;
				}
			extractor = chain.getExtractors().get(exIndex).getClassName();
			params = chain.getExtractors().get(exIndex).getParams();
			externalPath = chain.getExtractors().get(exIndex).getExternalPath();

			try {
				exTrainFeatures[exIndex] = anno.extractGivenAMethod(extractor, externalPath, params, trainingProblem);
				exTestFeatures[exIndex] = anno.extractGivenAMethod(extractor, externalPath, params, testingProblem);
			}
			catch (Exception ex) {
				if(gui != null)
					pnlOutput.setOutput("ERROR: Feature extractor failed! Extractor = " + chain.getExtractors().get(exIndex).getName() + " Chain = " + chain.getName());
				ex.printStackTrace();
				//Set rate for all annotation targets to 0 (this chain only)
				for(int i = 0; i < numOfAnno; i++)
					rates[i] = 0;

				extracted = false;
				break;			//break out of the extractors loop (exIndex)
			}

			trainSize += exTrainFeatures[exIndex][0].length;
			testSize += exTestFeatures[exIndex][0].length;
		}
		if(!extracted) {
			//chainCount++;
			return null; 			//Continue with next row (chain)
		}

		float[][] trainingFeatures = null;
		float[][] testingFeatures = null;

		if(numExtractors < 1) {	//If no extractor, call the function by passing "None"
			try {
				trainingFeatures = anno.extractGivenAMethod(extractor, null, params, trainingProblem);
				testingFeatures = anno.extractGivenAMethod(extractor, null, params, testingProblem);
			}
			catch (Exception ex) {
				if(gui != null)
					pnlOutput.setOutput("ERROR: Feature extractor failed! Extractor = " + extractor + " Chain = " + chain.getName());
				ex.printStackTrace();
				//Set rate for all annotation targets to 0 (this chain only)
				for(int i = 0; i < numOfAnno; i++)
					rates[i] = 0;

				//chainCount++;
				return null;	//Continue to another row (chain)
			}
		}
		else {	//Else, create feature array with enough space to hold data from all extractors 
			int trainingLength,
			testingLength;
			try {
				trainingLength = trainingProblem.getLength();
				testingLength = testingProblem.getLength();
			} catch (Exception ex) {
				if (gui != null)
					pnlOutput.setOutput("ERROR: Failed to get problem length.");
				ex.printStackTrace();
				return null;
			}
			trainingFeatures = new float[trainingLength][trainSize];
			testingFeatures = new float[testingLength][testSize];

			int destPosTrain = 0, destPosTest = 0;
			for(int exIndex=0; exIndex < numExtractors; exIndex++) {
				for(int item=0; item < trainingFeatures.length; item++)
					System.arraycopy(exTrainFeatures[exIndex][item], 0, trainingFeatures[item], destPosTrain, exTrainFeatures[exIndex][item].length);

				for(int item=0; item < testingFeatures.length; item++)
					System.arraycopy(exTestFeatures[exIndex][item], 0, testingFeatures[item], destPosTest, exTestFeatures[exIndex][item].length);

				destPosTrain += exTrainFeatures[exIndex][0].length;
				destPosTest += exTestFeatures[exIndex][0].length;
			}
		}
		exTrainFeatures = null;
		exTestFeatures = null;
		//End of extraction

		//apply feature selector and classifier
		if(gui != null)
			if (!setProgress(50)) {
				return null;
			}
		//trainingTestingOutput(trainingFeatures, testingFeatures, trainingTargets, testingTargets, numOfAnno);

		int testingLength = testingFeatures.length;

		//initialize structure to store annotation results
		Annotation[][] annotations = new Annotation[numOfAnno][testingLength];
		for (int i = 0; i < numOfAnno; i++) {
			for (int j = 0; j < testingLength; j++) {
				annotations[i][j] = new Annotation();
			}
		}

		boolean selected = true; //Set to false if selector throws exception
		//loop for each annotation target (one image may have multiple labels)
		for (int i = 0; i < numOfAnno; i++) {
			//Selected features for each annotation labels
			float[][] selectedTrainingFeatures = trainingFeatures;
			float[][] selectedTestingFeatures = testingFeatures;

			ArrayList<Selector> selectors = new ArrayList<Selector>();

			ComboFeatures combo = null;	            
			if(chain.hasSelectors()) 
			{	   
				if(gui != null)
					pnlOutput.setOutput("Selecting features...");

				//Apply each feature selector in the chain
				for(Selector selector : chain.getSelectors()) {
					//Supervised feature selectors need corresponding target data
					try {
						combo = anno.selectGivenAMethod(selector.getClassName(), selector.getExternalPath(), selector.getParams(), selectedTrainingFeatures, selectedTestingFeatures, trainingTargets[i], testingTargets[i]);
					}
					catch (Exception ex) {
						if(gui != null)
							pnlOutput.setOutput("ERROR: Feature selection failed! Selector = " + selector.getName() + " Chain = " + chain.getName());
						selected = false;
						break;	//Break out of selectors loop
					}

					//selected features overrides the passed in original features
					selectedTrainingFeatures = combo.getTrainingFeatures();
					selectedTestingFeatures = combo.getTestingFeatures();

					//This is needed for saving model, each annotation label needs a new selector to be created (i.e. cannot reuse selectors in chain)
					Selector currentSelector = new Selector(selector.getName());
					currentSelector.setSelectedIndices(combo.getSelectedIndices());
					selectors.add(currentSelector);
				}

				if(!selected) {
					//Set rate for all annotation targets to 0 (this chain only)
					for(int j = 0; j < numOfAnno; j++)
						rates[j] = 0;
					break;			//Break out of annotation targets loop
				}
			}
			//pass the training and testing data to Validator
			//get rate and prediction results for testing data
			float rate = 0;
			if(gui != null) 
				pnlOutput.setOutput("Classifying/Annotating...");

			
			//ArrayList<Classifier> classifierObj = new ArrayList<Classifier>(); 
			if(chain.hasClassifier())
			{
				try {
				
				//for(ClassifierInfo Class : chain.getClassifierInfo()) {
				//	classifierObj.add(anno.getClassifierGivenName(Class.getClassName(), Class.getExternalPath(), Class.getParams()));
				//}
					//Supervised feature selectors need corresponding target data
					
						
						
						rate = anno.classifyGivenAMethod( chain.getSavableClassifier(), selectedTrainingFeatures, selectedTestingFeatures, trainingTargets[i], testingTargets[i], annotations[i]);
						System.out.println("The rate get from classifyGivenAMethod:"+rate);
						executed = true;
						
					} 
					catch (Exception e) 
					{
						if(gui != null)
							pnlOutput.setOutput("ERROR: Classifier failure! ");
						e.printStackTrace();
					}
			}
			/* removed 1/16/2014
			Classifier classifierObj = null; 
			
			try {
				classifierObj = anno.getClassifierGivenName(chain.getClassifierClassName(), chain.getClassifierExternalPath(), chain.getClassParams());
				rate = anno.classifyGivenAMethod(classifierObj, chain.getClassParams(), selectedTrainingFeatures, selectedTestingFeatures, trainingTargets[i], testingTargets[i], annotations[i]);
				System.out.println("The rate get from classifyGivenAMethod:"+rate);
				executed = true;
			} catch (Exception e) {
				if(gui != null)
					pnlOutput.setOutput("ERROR: Classifier failure! Classifier = " + chain.getClassifier() + " Chain = " + chain.getName());
				e.printStackTrace();
			}
			*/

			rates[i] = rate;
			if(gui != null) {
				//If rate for this target(ith target) is better with this chain,
				//then, save this as new best model
				if(rate >= chainModels[i].getResult()) {
					chainModels[i].setExtractors(chain.getExtractors());//Can use extractors from chain because every annotation label shares the same extractors
					chainModels[i].setSelectors(selectors);				//Cannot use selectors from chain because each annotation label needs separate selected indices

					chainModels[i].setSavableClassifier(chain.getSavableClassifier()); //Added 1/28/2014
					chainModels[i].setClassifierInfo(chain.getClassifierInfo()); //Added 1/28/2014
					/*
					chainModels[i].setEnsembleName(chain.getEnsemble());
					chainModels[i].setEnsembleClass(chain.getEnsembleClassName());
					chainModels[i].setEnsemblePath(chain.getEnsembleExternalPath());
					chainModels[i].setEnsParams(chain.getEnsParams());
					*/
					/* Removed 1/16/2014
					chainModels[i].setClassifierName(chain.getClassifier());
					chainModels[i].setClassifierClass(chain.getClassifierClassName());
					chainModels[i].setClassifierPath(chain.getClassifierExternalPath());
					chainModels[i].setClassifier(classifierObj);
					chainModels[i].setClassParams(chain.getClassParams());
					*/
					chainModels[i].setResult(rate);
				}	            	

				System.out.println(rate);

				pnlOutput.setOutput("Recognition rate for " + anno.getAnnotationLabels().get(i) + ": " + rate);
				if (!setProgress(50 + (i + 1) * 50 / numOfAnno)) {
					return null;
				}
			}
		}//end of loop for annotation targets

		return rates;

	}



	private void cvRun() {
		Annotator anno = new Annotator();

		//------ read image data from the directory ------------//
		DataInput problem = gui.trainingProblem;

		//-----  read targets matrix (for multiple annotations, one per column) --------//
		if (!setProgress(20)) {
			return;
		}

		ArrayList<String> annoLabels = problem.getAnnotations();
		HashMap<String, String> classNames = problem.getClassNames();
		int[][] targets = problem.getTargets();
		int numOfAnno = annoLabels.size();
		anno.setAnnotationLabels(annoLabels);	//why??

				//Initialize float array to hold rates for each annotation for each selected chain and list of selected chains to pass to result panel
		ArrayList<Chain> selectedChains = getSelectedChains();
		float[][] rates = new float[selectedChains.size()][numOfAnno];

		int chainCount = 0;

		//Chain Models to keep track of the best model for each target
		chainModels = new ChainModel[numOfAnno];

		//Initialize common features for chain models
		for(int i = 0; i < numOfAnno; i++) {
			chainModels[i] = new ChainModel();
			//Save information to dump in chain file
			chainModels[i].setImageSet(new File(Annotator.dir).getAbsolutePath());
			chainModels[i].setMode("Cross Validation. Fold: " + Annotator.fold);
			chainModels[i].setChannel(channel);
			chainModels[i].setLabel(anno.getAnnotationLabels().get(i));
			chainModels[i].setClassNames(classNames);
			if(is3d)
				chainModels[i].setImageSize(imgWidth + "x" + imgHeight + "x" + imgDepth);
			else
				chainModels[i].setImageSize(imgWidth + "x" + imgHeight);
		}

		for(int row = 0; row < tableModel.getRowCount(); row++) {

			//Each chain should do a clean up of previous used memory. 11/15/2012
			System.gc();

			//Only use the checked chains
			if(!(Boolean)tableModel.getValueAt(row, COL_CHECK))
				continue;

			//Select the currently processed row in the table
			selectRow(row);

			Chain chain = (Chain)tableModel.getValueAt(row, COL_CHAIN);//Second column is chain object

			pnlOutput.setOutput("Processing " + chain.getName() + ":");

			//Chain list loaded from file may have incomplete chain in the middle if the file has been tampered with
			if(!chain.isComplete()) {
				pnlOutput.setOutput("Incomplete chain encountered. Chain = " + chain.getName());
				//continue;//TODO: may be not proceed?
			}

			//----- feature extraction -------//
			if (!setProgress(30)) {
				return;
			}
			pnlOutput.setOutput("Extracing features ... ");

			//Start of extraction
			float[][] features =  null;
			try {
				features = this.extractWithMultipleExtractors(problem, chain.getExtractors(), anno, gui);
			} catch (Exception e) {
				e.printStackTrace();
				pnlOutput.setOutput("ERROR: Feature extractor failed! Chain = " + chain.getName());

				//Set rate for all annotation targets to 0 (this chain only)
				for(int i = 0; i < numOfAnno; i++)
					rates[chainCount][i] = 0;

				chainCount++;
				continue; 			//Continue with next row (chain)
			}
			//End of extraction

			//raw data is not used after this point, set to null.: commented because used for subsequent loop runs
			//problem.setDataNull();
			if (!setProgress(50)) 
				return;

			//Apply Feature Selection and Classification in CV mode.
			int length = features.length;

			// parameters that are same for all target labels
			boolean shuffle = Boolean.parseBoolean(Annotator.shuffleFlag);
			// fold number K
			int K = 0;
			try {
				if (Annotator.fold.equals("LOO")) {
					K = length;
				}
				else {
					K = Integer.parseInt(Annotator.fold);
				}
			}
			catch (NumberFormatException e) {
				System.out.println("Number of fold is not a valid int. Set to " + length + ".");
				K = length;
			}
			if (K <= 0 || K > length) {
				System.out.println("Number of fold is not a valid int. Set to " + length + ".");
				K = length;
			}

			//allocate space for the results.
			Annotation[][] results = new Annotation[numOfAnno][length];
			for (int i = 0; i < numOfAnno; i++) {
				for (int j = 0; j < length; j++) {
					results[i][j] = new Annotation();
				}
			}

			boolean selected = true;
			//loop for each annotation target
			for (int i = 0; i < numOfAnno; i++) {
				float recograte[] = null;
				int start = 50 + i * 50 / numOfAnno;
				int region = 50 / numOfAnno;

				//Selected features for each annotation labels
				float[][] selectedFeatures = features;            

				ArrayList<Selector> selectors = new ArrayList<Selector>();

				ComboFeatures combo = null;
				if(chain.hasSelectors()) 
				{	            	
					pnlOutput.setOutput("Selecting features...");

					//Apply each feature selector in the chain
					for(Selector selector : chain.getSelectors()) {

						if (!setProgress(start)) 
							return;
						//Supervised feature selectors need corresponding target data
						try {
							combo = anno.selectGivenAMethod(selector.getClassName(), selector.getExternalPath(), selector.getParams(), selectedFeatures, targets[i]);
						}
						catch (Exception ex) {
							pnlOutput.setOutput("ERROR: Feature selection failed! Selector = " + selector.getName() + ", Chain = " + chain.getName());
							selected = false;
							break; //Break out of selectors loop
						}
						//selected features overrides the passed in original features
						selectedFeatures = combo.getTrainingFeatures();

						//This is needed for saving model, each annotation label needs a new selector to be created (i.e. cannot reuse selectors in chain)
						Selector currentSelector = new Selector(selector.getName());
						currentSelector.setSelectedIndices(combo.getSelectedIndices());
						selectors.add(currentSelector);
					}

					if(!selected) {
						//Set rate for all annotation targets to 0 (this chain only)
						for(int j = 0; j < numOfAnno; j++)
							rates[chainCount][j] = 0;
						break;			//Break out of annotation targets loop
					}
				}

				pnlOutput.setOutput("Classifying/Annotating ... ");

				if(chain.hasClassifier())
				{
				
							try {
								recograte = (new Validator(bar, start, region)).KFoldGivenAClassifier(K, selectedFeatures, targets[i], chain.getSavableClassifier(), shuffle, results[i]);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							executed = true;
			
						
					
					/*
					int j = 0;
					
					//Apply each classifier in the chain
					for(ClassifierInfo Class : chain.getClassifier()) {
						//Supervised feature selectors need corresponding target data
						try {
							if( !chain.isEns() )
							{
								classifierObj.add(anno.getClassifierGivenName(Class.getClassName(), Class.getExternalPath(), Class.getParams()));
								recograte = (new Validator(bar, start, region)).KFoldGivenAClassifier(K, selectedFeatures, targets[i], classifierObj.get(j), Class.getParams(), shuffle, results[i]);
								executed = true;
								j++;
							}
							else
							{
								classifierObj.add(anno.getClassifierGivenName(Class.getClassName(), Class.getExternalPath(), Class.getParams()));
								recograte = (new Validator(bar, start, region)).KFoldGivenAClassifier(K, selectedFeatures, targets[i], classifierObj.get(j), Class.getParams(), shuffle, results[i]);
								//Ens.addResult(recograte);
								executed = true;
								j++;
							}
						} catch (Exception e) {
							if(gui != null)
								pnlOutput.setOutput("ERROR: Classifier failure! ");
							e.printStackTrace();
						}
					}
					*/
				}
				//if( chain.isEns() )
				//{
				//	recograte = Ens.classify();
				//}	
				
				
				
				
				
				/* Removed 1/16/2014
				Classifier classifierObj = null;
				try {
					classifierObj = anno.getClassifierGivenName(chain.getClassifierClassName(), chain.getClassifierExternalPath(), chain.getClassParams());
					recograte = (new Validator(bar, start, region)).KFoldGivenAClassifier(K, selectedFeatures, targets[i], classifierObj, chain.getClassParams(), shuffle, results[i]);
					executed = true;
				} catch (Exception e) {
					e.printStackTrace();
					pnlOutput.setOutput("ERROR: Classifier failure! Chain = " + chain.getName());
				}
				*/

				rates[chainCount][i] = recograte[K];

				//If rate for this target(ith target) is better with this chain,
				//then, save this as new best model
				if(recograte[K] >= chainModels[i].getResult()) {
					chainModels[i].setExtractors(chain.getExtractors());
					chainModels[i].setSelectors(selectors);
					
					chainModels[i].setSavableClassifier( chain.getSavableClassifier() ); //Added 1/16/2014
					chainModels[i].setClassifierInfo(chain.getClassifierInfo());
					/*
					chainModels[i].setEnsembleName(chain.getEnsemble());
					chainModels[i].setEnsembleClass(chain.getEnsembleClassName());
					chainModels[i].setEnsemblePath(chain.getEnsembleExternalPath());
					chainModels[i].setEnsParams(chain.getEnsParams());
					*/
					/* Removed 1/16/2014
					chainModels[i].setClassifierName(chain.getClassifier());
					chainModels[i].setClassifierClass(chain.getClassifierClassName());
					chainModels[i].setClassifierPath(chain.getClassifierExternalPath());
					chainModels[i].setClassifier(classifierObj);
					chainModels[i].setClassParams(chain.getClassParams());
					*/
					chainModels[i].setResult(recograte[K]);
				}

				//output results to GUI and file
				System.out.println("Rate for annotation target " + i + ": " + recograte[K]);
				pnlOutput.setOutput("Recognition rate for " + anno.getAnnotationLabels().get(i) + ": " + recograte[K]);
			} //end of loop for annotation targets

			chainCount++;
		}//End of loop for chains
		
		//Display result
		if(executed)	//Display result if at least one chain is executed
			gui.addTab("Auto Comparison Results", rates, anno.getAnnotationLabels(), selectedChains, imgWidth, imgHeight, imgDepth, is3d, channel, cFlag);
	}
	/*
	 * The method has 2 purposes:
	 * 1. update the value of the progress bar in GUI.
	 * 2. check if there is a need to stop the working thread.
	 * It is called periodically by the working thread.
	 */
	private boolean setProgress(final int currentProgress) {
		if (thread == null) {
			System.out.println("thread is null");
			return false;
		}
		//if	(thread.isInterrupted())
		if (!isRunning && (currentProgress > 0)) {
			System.out.println("Interrupted at progress " + currentProgress);
			if (bar != null) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						bar.setValue(0);
					}
				});
			}
			pnlOutput.setOutput("Process cancelled by user.");
			return false;
		}

		if (bar != null) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					bar.setValue(currentProgress);
				}
			});
		}
		return true;
	}

	/*
	 * Selets the chain(row) currently being processed
	 */
	private void selectRow(int row) {
		tblChain.changeSelection(row, 1, false, false);
	}
	/*
	 * Get unique name for a chain
	 */
	private String createChainName() {
		int i=1;

		while(true) {
			//Break if unique name found
			if(!nameExists("Chain " + String.valueOf(i)))
				break;
			i++;
		}
		return "Chain " + String.valueOf(i);
	}
	/*
	 * Check if the supplied chain name already exists in the table
	 */
	private boolean nameExists(String name) {
		for(int row = 0; row < tblChain.getRowCount(); row++) {
			String rowName = (String)tableModel.getValueAt(row, COL_NAME);
			if(name.equals(rowName))
				return true;
		}
		return false;
	}

	/**
	 * Get the list of chains that are selected in the chain table.
	 * @return
	 */
	private ArrayList<Chain> getSelectedChains() {
		ArrayList<Chain> selectedChains = new ArrayList<Chain>(); 
		for(int row = 0; row < tableModel.getRowCount(); row++)
			if((Boolean)tableModel.getValueAt(row, COL_CHECK))
				selectedChains.add((Chain)tableModel.getValueAt(row, COL_CHAIN));

		return selectedChains;
	}

	//move from Anntator to work with progress bar
	private float[][] extractWithMultipleExtractors(DataInput problem, ArrayList<Extractor> extractors, Annotator anno,  AutoCompFrame gui) throws Exception {
	   	String extractor = "None";
	    HashMap<String, String> params = new HashMap<String, String>();
	    String externalPath = null;
	        
	    int numExtractors = extractors.size();
	    float[][][] exFeatures = new float[numExtractors][][];
	        
	    int dataSize = 0;	//To keep track of total size
	    for(int exIndex=0; exIndex < numExtractors; exIndex++) {
	    	
			//add more checking on progress and whether to stop.3/13
			if(gui != null) //extraction: 30-50
				if (!setProgress(30+exIndex*20/numExtractors)) {
					return null;
				}
	        extractor = extractors.get(exIndex).getClassName();
	        params = extractors.get(exIndex).getParams();
	        externalPath = extractors.get(exIndex).getExternalPath();
	        	
	        exFeatures[exIndex] = anno.extractGivenAMethod(extractor, externalPath, params, problem);
	        	
	        	dataSize += exFeatures[exIndex][0].length;
	        }
	        
	   float[][] features = null;
	        
	   if(numExtractors < 1) {	//If no extractor, call the function by passing "None"
	        	features = anno.extractGivenAMethod(extractor, null, params, problem);
	   }
	   else {	//Else, create feature array with enough space to hold data from all extractors 
	      	features = new float[problem.getLength()][dataSize];
	        	
	      	int destPos = 0;
	       	for(int exIndex=0; exIndex < numExtractors; exIndex++) {
	       		for(int item=0; item < features.length; item++) {
	       			System.arraycopy(exFeatures[exIndex][item], 0, features[item], destPos, exFeatures[exIndex][item].length);
	       		}
	       		destPos += exFeatures[exIndex][0].length;
	       	}
	   }
       return features;
    }

	
}
