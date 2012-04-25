package annotool.gui;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import annotool.Annotator;

public class LoadImageDialog extends JDialog {
	private JTabbedPane tabbedPane;
	
	private String targetTab = "Target File";
	private String dirTab = "Directory Tree";
	private String roiTab = "ROI";
	
	public static String[] extensions = {".*", ".jpg", ".tif", ".tiff", ".bmp", ".png", ".gif"};
	
	private static JFileChooser fileChooser;
	
	public LoadImageDialog(JFrame frame, LandingPanel pnlLanding, String modeFlag) {
		super(frame);
		
		if(fileChooser == null) {
			fileChooser = new JFileChooser();
			fileChooser.setCurrentDirectory(new java.io.File("."));
		}
		
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab(targetTab, new TargetFileDialogPanel(this, pnlLanding, modeFlag, fileChooser));
		if(!(Annotator.output.equals(Annotator.AN) || Annotator.output.equals(Annotator.ROI)))
				tabbedPane.addTab(dirTab, new DirectoryTreeDialogPanel(this, pnlLanding, modeFlag, fileChooser));
		tabbedPane.addTab(roiTab, new RoiModeDialogPanel(this, pnlLanding, modeFlag, fileChooser));
		
		this.add(tabbedPane);
		
		pack();
		setLocationRelativeTo(frame);
		setVisible(true);
	}
	
	public void dismiss() {
		setVisible(false);
	}
}
