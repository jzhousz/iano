package annotool.gui;

import java.awt.Dimension;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
public class ROIParameterPanel extends JPanel implements ChangeListener {
	private JLabel lbInterval, lbPadMode;
	private JSlider slider = null;
	private JRadioButton rbNone, rbResize, rbSymmetric, rbReplicate;
	
	//Constants representing padding models
	public static final int NONE = 0;
	public static final int RESIZE = 1;
	public static final int SYMMETRIC = 2;
	public static final int REPLICATE = 3;
	
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
	    rbResize = new JRadioButton("Resize");
	    rbSymmetric = new JRadioButton("Symmetric");
	    rbReplicate = new JRadioButton("Replicate");
	    
	    ButtonGroup group = new ButtonGroup();
	    group.add(rbNone);
	    group.add(rbResize);
	    group.add(rbSymmetric);
	    group.add(rbReplicate);
	    
	    //Labels
		lbInterval = new JLabel("Interval Size: " + slider.getValue());
		lbPadMode = new JLabel("Padding Mode:");
	    
	    
	    this.setBorder(BorderFactory.createTitledBorder(null, "ROI Parameters", 
				TitledBorder.LEFT, TitledBorder.TOP));
	    
	    //Add components to panel
	    this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	    this.add(lbInterval);
	    this.add(slider);
	    this.add(Box.createRigidArea(new Dimension(0, 15)));
	    this.add(lbPadMode);
	    this.add(rbNone);
	    this.add(rbResize);
	    this.add(rbSymmetric);
	    this.add(rbReplicate);
	    
	    for(java.awt.Component component : this.getComponents()) {
	    	if(component instanceof JComponent)
	    		((JComponent)component).setAlignmentX(LEFT_ALIGNMENT);
	    }
	}
	
	public void stateChanged(ChangeEvent ev) {
		JSlider src = (JSlider)ev.getSource();
		//if(!src.getValueIsAdjusting())
		lbInterval.setText("Interval Size: " + slider.getValue());
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
		if(rbResize.isSelected())
			return RESIZE;
		else if(rbReplicate.isSelected())
			return REPLICATE;
		else if(rbSymmetric.isSelected())
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
}
