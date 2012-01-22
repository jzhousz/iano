package annotool.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** This is modal dialog box that displays HTML formated text. */
public class LegendDialog extends JDialog implements ActionListener {
    public LegendDialog(String title, float[][] colorMasks, HashMap<String, String> classNames) {
        super(ij.IJ.getInstance(), title, false);
        Container container = getContentPane();
        
        int numColors = colorMasks.length;
        int classes = classNames.size();
        
        JPanel pnlContainer = new JPanel();
        pnlContainer.setLayout(new GridLayout(classes, 2, 2, 2));
        pnlContainer.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        container.add(pnlContainer);
        
        for(String key : classNames.keySet()) {
			int intKey = Integer.valueOf(key);
			
			int colorLabel = intKey % numColors;
			Color c = new Color(colorMasks[colorLabel][0], colorMasks[colorLabel][1], colorMasks[colorLabel][2]);
        
			JPanel colorBox = new JPanel();
			colorBox.setBackground(c);
			pnlContainer.add(colorBox);
			pnlContainer.add(new JLabel(classNames.get(key)));
        }
        
        setForeground(Color.black);
        pack();
        ij.gui.GUI.center(this);
        setVisible(true);
    }
    
    public void actionPerformed(ActionEvent e) {
        //setVisible(false);
        dispose();
    }
}