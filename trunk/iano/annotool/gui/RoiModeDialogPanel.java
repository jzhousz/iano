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
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;

import annotool.Annotator;
import annotool.gui.model.ImageFilter;

public class RoiModeDialogPanel extends JPanel implements ActionListener {
	JLabel fileL = new JLabel("ROI Dir: ");
	JLabel targetL = new JLabel("Image File: ");
	JTextField dirField = new JTextField("", 30);
	JTextField targetField = new JTextField("", 30);
	JButton filedir = new JButton("Browse ...");
	JButton targetFile = new JButton("Browse ...");

	JLabel testfileL = new JLabel("ROI Dir: ");
	JLabel testtargetL = new JLabel("Image File: ");
	JTextField testdirField = new JTextField("", 30);
	JTextField testtargetField = new JTextField("", 30);
	JButton testfiledir = new JButton("Browse ...");
	JButton testtargetFile = new JButton("Browse ...");

	JButton loadImageB = new JButton("Load");
	JButton combinedLoadImageB = new JButton("Load");
	JButton cancelB = new JButton("Cancel");

	LandingPanel pnlLanding = null;
	ImageReadyPanel pnlImage = null;

	JFileChooser fileChooser = null; // new JFileChooser ();
	
	LoadImageDialog dialog;

	public RoiModeDialogPanel(LoadImageDialog dialog, LandingPanel pnlLanding, String modeflag, JFileChooser fileChooser) {
		this.dialog = dialog;
		this.pnlLanding = pnlLanding;
		this.pnlImage = pnlLanding.getImageReadyPanel();
		this.fileChooser = fileChooser;

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
		lur1Panel.add(fileL, BorderLayout.WEST);
		lur1Panel.add(dirField, BorderLayout.CENTER);
		lur1Panel.add(filedir, BorderLayout.EAST);

		JPanel lur3Panel = new JPanel();
		lur3Panel.setLayout(new BorderLayout(5, 5));
		lur3Panel.add(targetL, BorderLayout.WEST);
		lur3Panel.add(targetField, BorderLayout.CENTER);
		lur3Panel.add(targetFile, BorderLayout.EAST);

		JPanel lur4Panel = new JPanel();
		lur4Panel.setLayout(new java.awt.FlowLayout());
		lur4Panel.add(loadImageB);
		lur4Panel.add(new JLabel("        "));
		lur4Panel.add(cancelB);

		luPanel.add(lur1Panel);
		luPanel.add(lur3Panel);
		luPanel.add(lur4Panel);

		luPanel.setBorder(new CompoundBorder(new TitledBorder(null,
				"Input images", TitledBorder.LEFT, TitledBorder.TOP),
				new EmptyBorder(5, 5, 5, 5)));

		filedir.addActionListener(this); // launch dir chooser
		targetFile.addActionListener(this); // launch file chooser
		loadImageB.addActionListener(this);
		cancelB.addActionListener(this);

		return luPanel;
	}

	private JPanel buildTrainTestFileLoadingPanel() {
		// the panel to load training set of images
		JPanel luPanel = new JPanel();
		luPanel.setLayout(new GridLayout(2, 1, 5, 5));
		JPanel lur1Panel = new JPanel();
		lur1Panel.setLayout(new BorderLayout(5, 5));
		lur1Panel.add(fileL, BorderLayout.WEST);
		lur1Panel.add(dirField, BorderLayout.CENTER);
		lur1Panel.add(filedir, BorderLayout.EAST);

		JPanel lur3Panel = new JPanel();
		lur3Panel.setLayout(new BorderLayout(5, 5));
		lur3Panel.add(targetL, BorderLayout.WEST);
		lur3Panel.add(targetField, BorderLayout.CENTER);
		lur3Panel.add(targetFile, BorderLayout.EAST);

		luPanel.add(lur1Panel);
		luPanel.add(lur3Panel);

		luPanel.setBorder(new CompoundBorder(new TitledBorder(null,
				"Training images", TitledBorder.LEFT, TitledBorder.TOP),
				new EmptyBorder(5, 5, 5, 5)));

		filedir.addActionListener(this); // launch dir chooser
		targetFile.addActionListener(this); // launch file chooser

		// build the panel for loading testing images
		JPanel ldPanel = new JPanel();
		ldPanel.setLayout(new GridLayout(3, 1, 5, 5));
		
		JPanel ldr1Panel = new JPanel();
		ldr1Panel.setLayout(new BorderLayout(5, 5));
		ldr1Panel.add(testfileL, BorderLayout.WEST);
		ldr1Panel.add(testdirField, BorderLayout.CENTER);
		ldr1Panel.add(testfiledir, BorderLayout.EAST);

		JPanel ldr3Panel = new JPanel();
		ldr3Panel.setLayout(new BorderLayout(5, 5));
		ldr3Panel.add(testtargetL, BorderLayout.WEST);
		ldr3Panel.add(testtargetField, BorderLayout.CENTER);
		ldr3Panel.add(testtargetFile, BorderLayout.EAST);

		ldPanel.add(ldr1Panel);
		ldPanel.add(ldr3Panel);

		ldPanel.setBorder(new CompoundBorder(new TitledBorder(null,
				"Testing images", TitledBorder.LEFT, TitledBorder.TOP),
				new EmptyBorder(5, 5, 5, 5)));

		JPanel loadbuttonPanel = new JPanel();
		loadbuttonPanel.setLayout(new java.awt.FlowLayout());
		loadbuttonPanel.add(combinedLoadImageB);
		loadbuttonPanel.add(new JLabel("        "));
		loadbuttonPanel.add(cancelB);

		combinedLoadImageB.addActionListener(this);
		cancelB.addActionListener(this);
		testfiledir.addActionListener(this); // launch dir chooser
		testtargetFile.addActionListener(this); // launch file chooser

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
		if (e.getSource() == filedir)
			openDir(dirField);
		else if (e.getSource() == targetFile)
			openFile(targetField);
		else if (e.getSource() == cancelB)
			dialog.dismiss();

		else if (e.getSource() == loadImageB) {
			
		} 
		else if (e.getSource() == testfiledir)
			openDir(testdirField);
		else if (e.getSource() == testtargetFile)
			openFile(testtargetField);
		else if (e.getSource() == combinedLoadImageB) {
		}
	}

	// methods open dir or file can be merged into one later
	private void openDir(JTextField dirfieldp) {
		fileChooser.setDialogTitle("Open ROI Directory");

		// Choose only directories
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		// Now open chooser
		int result = fileChooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File fFile = fileChooser.getSelectedFile();
			
			// display in the textfield.
			dirfieldp.setText(fFile.getAbsolutePath());
		}
	}

	private void openFile(JTextField targetField) {
		fileChooser.setDialogTitle("Open Image File");

		// Choose only files, not directories
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
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
