package annotool.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

public class DirectoryTreeDialogPanel extends JPanel implements ActionListener {
	JLabel fileL = new JLabel("Parent Dir: ");
	JLabel imgextL = new JLabel("Image Extension / Filter: ");
	JTextField dirField = new JTextField(Annotator.DEFAULT_DIR, 30);
	JButton filedir = new JButton("Browse ...");
	JComboBox extBox = new JComboBox(LoadImageDialog.extensions);

	JLabel testfileL = new JLabel("Parent Dir: ");
	JLabel testimgextL = new JLabel("Image Extension / Filter: ");
	JTextField testdirField = new JTextField(Annotator.DEFAULT_TESTDIR, 30);
	JButton testfiledir = new JButton("Browse ...");
	JComboBox testextBox = new JComboBox(LoadImageDialog.extensions);

	JButton loadImageB = new JButton("Load");
	JButton combinedLoadImageB = new JButton("Load");
	JButton cancelB = new JButton("Cancel");

	LandingPanel pnlLanding = null;
	ImageReadyPanel pnlImage = null;

	JFileChooser fileChooser = null; // new JFileChooser ();
	
	LoadImageDialog dialog;

	public DirectoryTreeDialogPanel(LoadImageDialog dialog, LandingPanel pnlLanding, String modeflag, JFileChooser fileChooser) {
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

		JPanel lur2Panel = new JPanel();
		lur2Panel.setLayout(new java.awt.FlowLayout(FlowLayout.RIGHT));
		lur2Panel.add(imgextL);
		lur2Panel.add(extBox);

		JPanel lur4Panel = new JPanel();
		lur4Panel.setLayout(new java.awt.FlowLayout());
		lur4Panel.add(loadImageB);
		lur4Panel.add(new JLabel("        "));
		lur4Panel.add(cancelB);

		luPanel.add(lur1Panel);
		luPanel.add(lur2Panel);
		luPanel.add(lur4Panel);

		luPanel.setBorder(new CompoundBorder(new TitledBorder(null,
				"Input images", TitledBorder.LEFT, TitledBorder.TOP),
				new EmptyBorder(5, 5, 5, 5)));
		// luPanel.setBackground(java.awt.Color.white);

		filedir.addActionListener(this); // launch dir chooser
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

		JPanel lur2Panel = new JPanel();
		lur2Panel.setLayout(new java.awt.FlowLayout(FlowLayout.RIGHT));
		lur2Panel.add(imgextL);
		lur2Panel.add(extBox);

		luPanel.add(lur1Panel);
		luPanel.add(lur2Panel);

		luPanel.setBorder(new CompoundBorder(new TitledBorder(null,
				"Training images", TitledBorder.LEFT, TitledBorder.TOP),
				new EmptyBorder(5, 5, 5, 5)));

		filedir.addActionListener(this); // launch dir chooser

		// build the panel for loading testing images
		JPanel ldPanel = new JPanel();
		ldPanel.setLayout(new GridLayout(2, 1, 5, 5));
		
		JPanel ldr1Panel = new JPanel();
		ldr1Panel.setLayout(new BorderLayout(5, 5));
		ldr1Panel.add(testfileL, BorderLayout.WEST);
		ldr1Panel.add(testdirField, BorderLayout.CENTER);
		ldr1Panel.add(testfiledir, BorderLayout.EAST);

		JPanel ldr2Panel = new JPanel();
		ldr2Panel.setLayout(new java.awt.FlowLayout(FlowLayout.RIGHT));
		ldr2Panel.add(testimgextL);
		ldr2Panel.add(testextBox);

		ldPanel.add(ldr1Panel);
		ldPanel.add(ldr2Panel);

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
		else if (e.getSource() == cancelB)
			dialog.dismiss();

		else if (e.getSource() == loadImageB) {
			// mode 1: cv
			// set the problem parameters
			Annotator.dir = dirField.getText().trim() + "//";
			Annotator.ext = (String) extBox.getSelectedItem();
			
			// display images, plus enable the go button if successful
			boolean displayOK,
					isColor = false,
					is3D = false;
			try {
				displayOK = pnlImage.getTablePanel().displayOneImageTable(
							Annotator.dir, Annotator.ext, true);
				isColor = isColor();
				is3D = is3D();
			} catch (Exception ex) {
				displayOK = false;
				ex.printStackTrace();
			}

			// controlPanel.thingsEnabled(displayOK);
			if (displayOK) // if display is true
			{
				pnlImage.setIs3D(is3D);
				pnlImage.channelEnabled(isColor);

				// write some information about the opened image in the
				// outputpanel ...
				pnlImage.getOutputPanel()
						.setOutput(
								"Images loaded from "
										+ dirField.getText().trim() + ".");
				pnlImage.setMode();
				pnlImage.showClassLegends();

				// Display the panel with images
				pnlLanding.displayImageReadyPanel();

				// close the dialog
				dialog.dismiss();
			}
			else {
				JOptionPane.showMessageDialog(this,
					    "Failed to load images.",
					    "Loading Error",
					    JOptionPane.ERROR_MESSAGE);
			}
				
		} else if (e.getSource() == testfiledir)
			openDir(testdirField);
		else if (e.getSource() == combinedLoadImageB) {
			// mode 2 training/testing
			Annotator.dir = dirField.getText().trim() + "//";
			Annotator.ext = (String) extBox.getSelectedItem();
			Annotator.testdir = testdirField.getText().trim() + "//";
			Annotator.testext = (String) testextBox.getSelectedItem();

			boolean displayOK,
				isColor = false,
				is3D = false;
			try {
				displayOK = pnlImage.getTablePanel().displayTwoImageTables(
							Annotator.dir, Annotator.ext,
							Annotator.testdir, Annotator.testext);
				isColor = isColor();
				is3D = is3D();
				
			} catch (Exception ex) {
				displayOK = false;
				ex.printStackTrace();
			}

			// controlPanel.thingsEnabled(displayOK);
			if (displayOK) // if display is true
			{
				pnlImage.setIs3D(is3D);
				pnlImage.channelEnabled(isColor);

				// write some information about the opened image in the
				// outputpanel ...
				pnlImage.getOutputPanel().setOutput(
						"Training Images loaded from "
								+ dirField.getText().trim() + ".");
				pnlImage.getOutputPanel().setOutput(
						"Testing Images loaded from "
								+ testdirField.getText().trim() + ".");

				pnlImage.setMode();
				pnlImage.showClassLegends();

				// Display the panel with images
				pnlLanding.displayImageReadyPanel();

				// close the dialog
				dialog.dismiss();
			}
			else {
				JOptionPane.showMessageDialog(this,
					    "Failed to load images.",
					    "Loading Error",
					    JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	// methods open dir or file can be merged into one later
	private void openDir(JTextField dirfieldp) {
		fileChooser.setDialogTitle("Open Image Directory");

		// Choose only directories
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		// Set filter for Java source files.
		// fc.setFileFilter (fJavaFilter); //replace the image ext combo box?

		// Now open chooser
		int result = fileChooser.showOpenDialog(this);
		if (result == JFileChooser.CANCEL_OPTION) {
			return;
		} else if (result == JFileChooser.APPROVE_OPTION) {
			java.io.File fFile = fileChooser.getSelectedFile();
			// display in the textfield.
			dirfieldp.setText(fFile.getAbsolutePath());
		}
	}

	private boolean isColor() throws Exception {
		annotool.io.DataInput problem = new annotool.io.DataInput(
				Annotator.dir, Annotator.ext, true);
		String[] children = problem.getChildren();
		return (problem.isColor(Annotator.dir + children[0]));
	}

	private boolean is3D() throws Exception {
		annotool.io.DataInput problem = new annotool.io.DataInput(
				Annotator.dir, Annotator.ext, true);
		String[] children = problem.getChildren();
		return (problem.is3D(Annotator.dir + children[0]));
	}
}
