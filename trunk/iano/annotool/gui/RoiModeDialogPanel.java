package annotool.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;

import annotool.Annotator;
import annotool.gui.model.ImageFilter;
import annotool.gui.model.ROIFilter;

public class RoiModeDialogPanel extends JPanel implements ActionListener {
	private JLabel lbRoiFiles = new JLabel("ROI File(s): ");
	private JLabel lbImageFile = new JLabel("Image File: ");
	private JTextField txtRoiFiles = new JTextField("", 30);
	private JTextField txtImageFile = new JTextField("", 30);
	private JButton btnBrowseRoi = new JButton("Browse ...");
	private JButton btnBrowseImage = new JButton("Browse ...");

	private JLabel lbTestRoiFiles = new JLabel("ROI File(s): ");
	private JLabel lbTestImageFile = new JLabel("Image File: ");
	private JTextField txtTestRoiFiles = new JTextField("", 30);
	private JTextField txtTestImageFile = new JTextField("", 30);
	private JButton btnBrowseTestRoi = new JButton("Browse ...");
	private JButton btnBrowseTestImage = new JButton("Browse ...");

	private JButton btnLoad = new JButton("Load");
	private JButton btnLoadCombined = new JButton("Load");
	private JButton btnCancel = new JButton("Cancel");

	private LandingPanel pnlLanding = null;
	private ImageReadyPanel pnlImage = null;

	private JFileChooser fileChooser = null; // new JFileChooser ();
	
	private LoadImageDialog dialog;
	
	private String[] roiPaths = null,
					 testRoiPaths = null;	

	public RoiModeDialogPanel(LoadImageDialog dialog, LandingPanel pnlLanding, String modeflag, JFileChooser fileChooser) {
		this.dialog = dialog;
		this.pnlLanding = pnlLanding;
		this.pnlImage = pnlLanding.getImageReadyPanel();
		this.fileChooser = fileChooser;
		
		this.txtRoiFiles.setEnabled(false);
		this.txtImageFile.setEnabled(false);
		this.txtTestRoiFiles.setEnabled(false);
		this.txtTestImageFile.setEnabled(false);

		if (modeflag == Annotator.TT) // tt, can set testingTarget to false too.
			this.add(buildTrainTestFileLoadingPanel());
		else if (modeflag == Annotator.CV) // cv
			this.add(buildFileLoadingPanel());
		else if (modeflag == Annotator.ROI) // roi
			this.add(buildFileLoadingPanel());
		else if (modeflag == Annotator.TO) // train only
			this.add(buildFileLoadingPanel());
		else if (modeflag == Annotator.AN) // Annotate
			this.add(buildFileLoadingPanel());
	}

	private JPanel buildFileLoadingPanel() {
		// the panel to load one set of images
		JPanel luPanel = new JPanel();
		luPanel.setLayout(new GridLayout(3, 1, 5, 5));

		JPanel lur1Panel = new JPanel();
		lur1Panel.setLayout(new BorderLayout(5, 5));
		lur1Panel.add(lbRoiFiles, BorderLayout.WEST);
		lur1Panel.add(txtRoiFiles, BorderLayout.CENTER);
		lur1Panel.add(btnBrowseRoi, BorderLayout.EAST);

		JPanel lur3Panel = new JPanel();
		lur3Panel.setLayout(new BorderLayout(5, 5));
		lur3Panel.add(lbImageFile, BorderLayout.WEST);
		lur3Panel.add(txtImageFile, BorderLayout.CENTER);
		lur3Panel.add(btnBrowseImage, BorderLayout.EAST);

		JPanel lur4Panel = new JPanel();
		lur4Panel.setLayout(new java.awt.FlowLayout());
		lur4Panel.add(btnLoad);
		lur4Panel.add(new JLabel("        "));
		lur4Panel.add(btnCancel);

		luPanel.add(lur1Panel);
		luPanel.add(lur3Panel);
		luPanel.add(lur4Panel);

		luPanel.setBorder(new CompoundBorder(new TitledBorder(null,
				"Input images", TitledBorder.LEFT, TitledBorder.TOP),
				new EmptyBorder(5, 5, 5, 5)));

		btnBrowseRoi.addActionListener(this); // launch dir chooser
		btnBrowseImage.addActionListener(this); // launch file chooser
		btnLoad.addActionListener(this);
		btnCancel.addActionListener(this);

		return luPanel;
	}

	private JPanel buildTrainTestFileLoadingPanel() {
		// the panel to load training set of images
		JPanel luPanel = new JPanel();
		luPanel.setLayout(new GridLayout(2, 1, 5, 5));
		JPanel lur1Panel = new JPanel();
		lur1Panel.setLayout(new BorderLayout(5, 5));
		lur1Panel.add(lbRoiFiles, BorderLayout.WEST);
		lur1Panel.add(txtRoiFiles, BorderLayout.CENTER);
		lur1Panel.add(btnBrowseRoi, BorderLayout.EAST);

		JPanel lur3Panel = new JPanel();
		lur3Panel.setLayout(new BorderLayout(5, 5));
		lur3Panel.add(lbImageFile, BorderLayout.WEST);
		lur3Panel.add(txtImageFile, BorderLayout.CENTER);
		lur3Panel.add(btnBrowseImage, BorderLayout.EAST);

		luPanel.add(lur1Panel);
		luPanel.add(lur3Panel);

		luPanel.setBorder(new CompoundBorder(new TitledBorder(null,
				"Training images", TitledBorder.LEFT, TitledBorder.TOP),
				new EmptyBorder(5, 5, 5, 5)));

		btnBrowseRoi.addActionListener(this); // launch dir chooser
		btnBrowseImage.addActionListener(this); // launch file chooser

		// build the panel for loading testing images
		JPanel ldPanel = new JPanel();
		ldPanel.setLayout(new GridLayout(3, 1, 5, 5));
		
		JPanel ldr1Panel = new JPanel();
		ldr1Panel.setLayout(new BorderLayout(5, 5));
		ldr1Panel.add(lbTestRoiFiles, BorderLayout.WEST);
		ldr1Panel.add(txtTestRoiFiles, BorderLayout.CENTER);
		ldr1Panel.add(btnBrowseTestRoi, BorderLayout.EAST);

		JPanel ldr3Panel = new JPanel();
		ldr3Panel.setLayout(new BorderLayout(5, 5));
		ldr3Panel.add(lbTestImageFile, BorderLayout.WEST);
		ldr3Panel.add(txtTestImageFile, BorderLayout.CENTER);
		ldr3Panel.add(btnBrowseTestImage, BorderLayout.EAST);

		ldPanel.add(ldr1Panel);
		ldPanel.add(ldr3Panel);

		ldPanel.setBorder(new CompoundBorder(new TitledBorder(null,
				"Testing images", TitledBorder.LEFT, TitledBorder.TOP),
				new EmptyBorder(5, 5, 5, 5)));

		JPanel loadbuttonPanel = new JPanel();
		loadbuttonPanel.setLayout(new java.awt.FlowLayout());
		loadbuttonPanel.add(btnLoadCombined);
		loadbuttonPanel.add(new JLabel("        "));
		loadbuttonPanel.add(btnCancel);

		btnLoadCombined.addActionListener(this);
		btnCancel.addActionListener(this);
		btnBrowseTestRoi.addActionListener(this); // launch dir chooser
		btnBrowseTestImage.addActionListener(this); // launch file chooser

		// JPanel combinedPanel = AnnotatorGUI.createVerticalPanel(true);
		JPanel combinedPanel = new JPanel();
		combinedPanel.setLayout(new BoxLayout(combinedPanel, BoxLayout.Y_AXIS));
		combinedPanel.setAlignmentY(TOP_ALIGNMENT);
		combinedPanel.setAlignmentX(LEFT_ALIGNMENT);
		Border loweredBorder = new CompoundBorder(new SoftBevelBorder(
				SoftBevelBorder.LOWERED), new EmptyBorder(5, 5, 5, 5));
		combinedPanel.setBorder(loweredBorder);

		combinedPanel.add(luPanel);
		combinedPanel.add(ldPanel);
		combinedPanel.add(loadbuttonPanel);

		return combinedPanel;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnBrowseRoi)
			roiPaths = openRois(txtRoiFiles);
		else if (e.getSource() == btnBrowseImage)
			openImage(txtImageFile);
		else if (e.getSource() == btnCancel)
			dialog.dismiss();

		else if (e.getSource() == btnLoad) {
			
		} 
		else if (e.getSource() == btnBrowseTestRoi)
			testRoiPaths = openRois(txtTestRoiFiles);
		else if (e.getSource() == btnBrowseTestImage)
			openImage(txtTestImageFile);
		else if (e.getSource() == btnLoadCombined) {
			if(roiPaths == null || testRoiPaths == null) {
				JOptionPane.showMessageDialog(this,
					    "Please select roi files to use.",
					    "Insufficient input",
					    JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			String imagePath = txtImageFile.getText().trim();
			String testImagePath = txtTestImageFile.getText().trim();
			
			boolean displayOK = true,
					isColor = false,
					is3D = false;
			String errorMsg = "";
			
			AnnTablePanel pnlTable = pnlImage.getTablePanel();
			try {
				displayOK = pnlTable.displayTwoImageTables(imagePath, roiPaths, testImagePath, testRoiPaths);
				isColor = pnlTable.isColor();
				is3D = pnlTable.is3D();
			} catch (Exception ex) {
				displayOK = false;
				errorMsg = ex.getMessage();
				ex.printStackTrace();				
			}
			
			if (displayOK) {
				pnlImage.setIs3D(is3D);
				pnlImage.channelEnabled(isColor);

				// write some information about the opened image in the
				// outputpanel ...
				pnlImage.getOutputPanel().setOutput("Training Image: " + imagePath);	
				pnlImage.getOutputPanel().setOutput("Testing Image: " + testImagePath);

				pnlImage.setMode();
				pnlImage.showClassLegends();

				// Display the panel with images
				pnlLanding.displayImageReadyPanel();

				// close the dialog
				dialog.dismiss();
			}
			else
				JOptionPane.showMessageDialog(this,
					    "Error loading data. " + errorMsg,
					    "Exception:",
					    JOptionPane.ERROR_MESSAGE);
		}
	}

	private String[] openRois(JTextField dirfieldp) {
		fileChooser.setDialogTitle("Open ROI File(s)");

		// Choose only directories
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setFileFilter(new ROIFilter());

		// Now open chooser
		int result = fileChooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File[] files = fileChooser.getSelectedFiles();
			StringBuffer displayText = new StringBuffer();
			
			String[] roiPaths = new String[files.length];
			
			for(int i = 0; i < files.length; i++) {
				displayText.append("'" + files[i].getName() + "' ");
				roiPaths[i] = files[i].getAbsolutePath();
			}
			
			// display in the textfield.
			dirfieldp.setText(displayText.toString());
			
			return roiPaths;
		}
		
		return null;
	}

	private void openImage(JTextField targetField) {
		fileChooser.setDialogTitle("Open Image File");

		// Choose only files, not directories
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileFilter(new ImageFilter());
		
		// Now open chooser
		int result = fileChooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {

			File fFile = fileChooser.getSelectedFile();
			targetField.setText(fFile.getAbsolutePath());
		}
		
		fileChooser.resetChoosableFileFilters();
	}
}
