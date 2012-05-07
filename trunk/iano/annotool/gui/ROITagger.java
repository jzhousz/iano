package annotool.gui;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import annotool.analysis.Utility;
import annotool.io.DataInput;

/**
 * This class provides an interface to load image file and corresponding ROI file and
 * create subimages corresponding to the region of interests loaded as well as the target file
 * for the problem.
 * 
 * This can be used for generating training images from a single image and roi files.
 * 
 * This is used if the user prefers to save subimages and use target file mode.
 * Another alternative to creating sub images is to use ROI input mode.
 * 
 * @author Santosh
 *
 */
public class ROITagger extends JDialog implements ActionListener {
	private AnnOutputPanel pnlStatus = new AnnOutputPanel();
	private ImagePlus imp = null;
	private JButton btnLoadImg = null;
	private JButton btnLoadROI = null;
	private JButton btnSave = null;
	private JButton btnTemp = null;

	// testing purpose for 3D
	private JButton btn3DROITrain = null;
	private JButton btn3DROITest = null;

	// File chooser and context specific file filters to use with the file
	// chooser
	final JFileChooser fileChooser = new JFileChooser();

	public ROITagger(JFrame parent, String title, boolean isModal) {
		super(parent, title, isModal);

		Container container = getContentPane();
		container.setLayout(new BorderLayout());

		container.add(createRightColumn(), BorderLayout.EAST);
		container.add(pnlStatus, BorderLayout.SOUTH);

		pack();
		ij.gui.GUI.center(this);
		setVisible(true);
	}

	private JPanel createRightColumn() {
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new GridLayout(4, 1, 2, 2));

		btnLoadImg = new JButton("Load Image");
		btnLoadImg.addActionListener(this);

		btnLoadROI = new JButton("ROI Manager");
		btnLoadROI.addActionListener(this);

		btnSave = new JButton("Save");
		btnSave.addActionListener(this);

		btnTemp = new JButton("temp");
		btnTemp.addActionListener(this);

		btn3DROITrain = new JButton("3D ROI Train");
		btn3DROITrain.addActionListener(this);

		btn3DROITest = new JButton("3D ROI Test");
		btn3DROITest.addActionListener(this);

		rightPanel.add(btnLoadImg);
		rightPanel.add(btnLoadROI);
		rightPanel.add(btnSave);
		rightPanel.add(btnTemp);
		rightPanel.add(btn3DROITrain);
		rightPanel.add(btn3DROITest);

		return rightPanel;
	}

	@Override
	public void actionPerformed(ActionEvent ev) {
		if (ev.getSource() == btnLoadImg) {
			// fileChooser.resetChoosableFileFilters();
			// fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

			int returnVal = fileChooser.showOpenDialog(this);
			if (returnVal == JFileChooser.CANCEL_OPTION)
				return;

			File file = fileChooser.getSelectedFile();
			imp = new ImagePlus(file.getPath());
			imp.show();
		} else if (ev.getSource() == btnLoadROI) {
			RoiManager manager = RoiManager.getInstance();
			if (manager == null)
				manager = new RoiManager();
		} else if (ev.getSource() == btnSave) {
			RoiManager manager = RoiManager.getInstance();
			if (manager == null) {
				this.pnlStatus.setOutput("No ROI manager available.");
				return;
			}

			String className, annoLabel;

			// Display input dialog for class name and annotation label
			JTextField classField = new JTextField();
			JTextField annoField = new JTextField();
			JLabel lbClass = new JLabel("Class name:");
			JLabel lbAnno = new JLabel(
					"Annotation Label (only for new target file):");
			JPanel pnl = new JPanel(new GridLayout(2, 2, 2, 2));

			pnl.add(lbClass);
			pnl.add(classField);
			pnl.add(lbAnno);
			pnl.add(annoField);

			int result = JOptionPane.showConfirmDialog(null, pnl,
					"Class name and annotation label",
					JOptionPane.OK_CANCEL_OPTION);
			if (result == JOptionPane.CANCEL_OPTION)
				return;

			className = classField.getText();
			annoLabel = annoField.getText();

			if ((className == null) || className.trim().equals("")) {
				pnlStatus
						.setOutput("Class name is required before Rois can be saved.");
				return;
			}

			boolean is3D = (imp.getStackSize() > 1);

			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			int returnVal = fileChooser.showSaveDialog(this);
			if (returnVal == JFileChooser.CANCEL_OPTION)
				return;

			File file = fileChooser.getSelectedFile();

			Roi[] rois = manager.getRoisAsArray();

			// Cross platform new line character
			String newLine = System.getProperty("line.separator");

			File targetFile = new File(file, "target.txt");
			File saveFile = new File(file, "target_temp.txt");
			Scanner scanner = null;
			String classLine = null;
			boolean classExists = false;
			int key, classKey = 1;
			int maxKey = -1;

			if (targetFile.exists()) {
				// Read the existing file
				try {
					scanner = new Scanner(targetFile);
				} catch (FileNotFoundException e) {
					pnlStatus
							.setOutput("A practically impossible thing just happened. Please try again.");
					e.printStackTrace();
					return;
				}
				if (scanner.hasNextLine())
					annoLabel = scanner.nextLine(); // First line should be
													// annotation label

				// Second line should have classes
				if (scanner.hasNextLine()) {
					classLine = scanner.nextLine();
					Scanner lineScanner = new Scanner(classLine);
					while (lineScanner.hasNext()) {
						String pair[] = lineScanner.next().split(":");
						try {
							key = Integer.parseInt(pair[0]);
						} catch (NumberFormatException ex) {
							pnlStatus
									.setOutput("Existing target file is invalid.");
							return;
						}
						if (pair[1].equalsIgnoreCase(className)) {
							classExists = true;
							classKey = key;
						}
						if (key > maxKey)
							maxKey = key;
					}
					lineScanner.close();
				}

				// If class name already existed no need to update the class
				// line, otherwise append new class key:value to the class line
				if (!classExists) {
					classKey++;
					classLine = classLine + " " + classKey + ":" + className;
				}
			} else
				classLine = classKey + ":" + className;

			//Write to temporary target file
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(saveFile));
				writer.write(annoLabel + newLine);
				writer.write(classLine + newLine);

				if (scanner != null) {
					while (scanner.hasNextLine()) {
						String line = scanner.nextLine();
						writer.write(line + newLine);
					}
					scanner.close();
				}

				int lastAppend = 1;	//Number to add to end of file
				for (int i = 0; i < rois.length; i++) {
					imp.setRoi(rois[i]);
					ImagePlus roiImg = new ImagePlus("ROI", imp.getProcessor()
							.crop());
					
					String fileName = className + "_" + lastAppend + ".jpg";
					File imgFile = new File(file.getPath() + "/" + fileName);
					//Get unique file name
					while (imgFile.exists()) {
						lastAppend++;
						fileName = className + "_" + lastAppend + ".jpg";
						imgFile = new File(file.getPath() + "/" + fileName);
					}
					
					//Save roi as image
					ij.IJ.saveAs(roiImg, "jpeg", fileName);
					
					//Add entry to target file
					writer.write(classKey + " " + className + "_" + lastAppend
							+ ".jpg" + newLine);
				}
				
				writer.flush();
				writer.close();
			} catch (IOException ex) {
				pnlStatus
						.setOutput("Exception occured while writing target file: "
								+ file.getName());
				ex.printStackTrace();
			}
			
			//Remove original target file
			if(targetFile.exists())
				if(!targetFile.delete())
					pnlStatus.setOutput("Failed to delete existing target file.");
			
			if(saveFile.renameTo(targetFile)) {
				pnlStatus.setOutput("DONE!!!");
			}
			else
				pnlStatus.setOutput("Failed to change target file. Please go to the target directory and manually rename target_temp.txt to target.txt");
			
		} else if (ev.getSource() == btnTemp) {
			ImageProcessor ip = imp.getProcessor();
			Object datain = ip.getPixels();

			int totalwidth = ip.getWidth();
			int totalheight = ip.getHeight();

			float[] feature = new float[totalwidth * totalheight];

			int imageType = imp.getType();
			if (imageType == DataInput.GRAY8) {
				byte[] data = (byte[]) datain;
				for (int i = 0; i < totalwidth * totalheight; i++)
					feature[i] = (float) (data[i] & 0xff);
			} else if (imageType == DataInput.GRAY16) {
				int[] data = (int[]) datain;
				for (int i = 0; i < totalwidth * totalheight; i++)
					feature[i] = (float) (data[i] & 0xffff);
			} else if (imageType == DataInput.GRAY32) {
				float[] data = (float[]) datain;
				for (int i = 0; i < totalwidth * totalheight; i++)
					feature[i] = (float) data[i];
			} else if (imageType == DataInput.COLOR_RGB) {
				pnlStatus.setOutput("Only grayscale supported");
				return;
			} else {
				pnlStatus.setOutput("Unsuppored Image Type");
				return;
			}

			boolean[] isMaxima = Utility.getLocalMaxima(feature, totalwidth,
					totalheight, 1, 3, 3, 1);

			// float alpha = 0.6f; //transparent parameter (0: transparent; 1:
			// opaque)
			// int[] colors = new int[3];
			// float[] fcolors = new float[3];

			ImageConverter ic = new ImageConverter(imp);
			ic.convertToRGB();
			ip = imp.getChannelProcessor();

			for (int y = 0; y < totalheight; y++) {
				for (int x = 0; x < totalwidth; x++) {
					if (isMaxima[Utility.offset(x, y, totalwidth)]) {
						ip.moveTo(x, y);
						// ip.getPixel(x, y, colors);
						// for(int k = 0; k < colors.length; k++) fcolors[k] =
						// (float) colors[k]/256;

						// fcolors[0] = alpha + fcolors[0]*(1-alpha);
						// fcolors[1] = fcolors[1]*(1-alpha);
						// fcolors[2] = fcolors[2]*(1-alpha);

						// Color c = new Color(fcolors[0], fcolors[1],
						// fcolors[2]);
						Color c = new Color(1f, 0f, 0f);
						ip.setColor(c);
						ip.fillOval(x - 2, y - 2, 4, 4);
					}
				}
			}

			imp.updateAndDraw();

			pnlStatus.setOutput("Done");
		} else if (ev.getSource() == btn3DROITrain) {
			ImageProcessor ip = imp.getProcessor();
			annotool.analysis.ThreeDROIAnnotation.train3droi(imp);
		} else if (ev.getSource() == btn3DROITest) {
			ImageProcessor ip = imp.getProcessor();
			annotool.analysis.ThreeDROIAnnotation.test3droi(imp);
		}

	}
}
