package annotool.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * This panel displays the controls specific to ROI annotation mode.
 * This is displayed as part of ImageReadyPanel when ROI annotation is selected.
 * It provides slider to select window size, radio buttons to select padding type for 
 * boundary pixels, checkbox to specify if only local maxima points are to be annotated, 
 * and an option to specify export annotation result.
 * 
 * @author Santosh
 *
 */
public class ROIParameterPanel extends JPanel implements ChangeListener, ItemListener, ActionListener {
	private JLabel lbInterval, lbPadMode;
	private JSlider slider = null;
	private JRadioButton rbNone, rbSymmetric;
	
	private JCheckBox cbExport, cbMaxima;
	private JLabel lbExportDir;
	private JButton btnSelectDir;
	private String exportDir = ""; //Directory which user can select to export annotation result
	
	//Constants representing padding models
	public static final int NONE = 0;
	public static final int SYMMETRIC = 1;
	
	final JFileChooser fileChooser = new JFileChooser();
	
	public ROIParameterPanel() {
		this(1, 100);
	}
	public ROIParameterPanel(int sliderMin, int sliderMax) {
		//Slider
		slider = new JSlider(JSlider.HORIZONTAL, sliderMin, sliderMax, (sliderMin + sliderMax) / 2);
		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(1);
	    slider.setPaintTicks(true);
	    slider.setPaintLabels(true);
	    slider.addChangeListener(this);
		
		//Radio buttons
	    rbNone = new JRadioButton("None", true);
	    rbSymmetric = new JRadioButton("Symmetric");
	    
	    ButtonGroup group = new ButtonGroup();
	    group.add(rbNone);
	    group.add(rbSymmetric);
	    
	    //Labels
		lbInterval = new JLabel("Interval Size: " + slider.getValue());
		lbPadMode = new JLabel("Padding Mode:");
		
		//Checkbox
		cbExport = new JCheckBox("Export Result");
		cbExport.addItemListener(this);
		
		cbMaxima = new JCheckBox("Local Maxima Only");
		
		//Other components for result export
		lbExportDir = new JLabel("Export Dir: ");
		lbExportDir.setEnabled(false);
		btnSelectDir = new JButton("Select Directory");
		btnSelectDir.addActionListener(this);
		btnSelectDir.setEnabled(false);
	    
	    
	    this.setBorder(BorderFactory.createTitledBorder(null, "ROI Parameters", 
				TitledBorder.LEFT, TitledBorder.TOP));
	    
	    //Add components to panel
	    this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	    this.add(lbInterval);
	    this.add(slider);
	    this.add(Box.createRigidArea(new Dimension(0, 15)));
	    this.add(lbPadMode);
	    this.add(rbNone);
	    this.add(rbSymmetric);
	    this.add(Box.createRigidArea(new Dimension(0, 15)));
	    this.add(cbMaxima);
	    this.add(Box.createRigidArea(new Dimension(0, 15)));
	    this.add(cbExport);
	    this.add(btnSelectDir);
	    this.add(lbExportDir);
	    
	    for(java.awt.Component component : this.getComponents()) {
	    	if(component instanceof JComponent)
	    		((JComponent)component).setAlignmentX(LEFT_ALIGNMENT);
	    }
	}
	
	@Override
	public void stateChanged(ChangeEvent ev) {
		JSlider src = (JSlider)ev.getSource();
		//if(!src.getValueIsAdjusting())
		lbInterval.setText("Interval Size: " + slider.getValue());
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		if(e.getSource() == cbExport)
		{
			if(cbExport.isSelected()) {
				btnSelectDir.setEnabled(true);
				lbExportDir.setEnabled(true);
			}
			else {
				btnSelectDir.setEnabled(false);
				lbExportDir.setEnabled(false);
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setMultiSelectionEnabled(false);
		
		int returnVal = fileChooser.showSaveDialog(this);		
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            this.exportDir = fileChooser.getSelectedFile().getPath();
            lbExportDir.setText(this.exportDir);
        }
	}	
	
	/**
	 * 
	 * @return 
	 * integer representing the ROI sliding interval
	 */
	public int getSelectedInterval() {
		return slider.getValue();
	}
	
	/**
	 * 
	 * @return 
	 * integer value representing the selected padding mode.
	 * 
	 * Padding modes are: 
	 * ROIParameterPanel.RESIZE, ROIParameterPanel.REPLICATE and ROIParameterPanel.SYMMETRIC
	 */
	public int getSelectedMode() {
		if(rbSymmetric.isSelected())
			return SYMMETRIC;
		else
			return NONE;
	}
	
	/**
	 * Enables or disables all the components in this panel
	 */	
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		for(java.awt.Component component : this.getComponents())
			component.setEnabled(enabled);
	}
	
	/**
	 * Whether or not to export prediction to file
	 * 
	 * @return
	 */
	public boolean isExport() {
		return cbExport.isSelected();
	}
	
	public String getExportDir() {
		return exportDir;
	}
	public void setExportDir(String exportDir) {
		this.exportDir = exportDir;
	}
	
	/**
	 * Apply annotation to local maxima only
	 */
	public boolean isLocalMaximaOnly() {
		return cbMaxima.isSelected();
	}
}
