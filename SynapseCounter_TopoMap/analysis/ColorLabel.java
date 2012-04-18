package synapse.analysis;

import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/** This is modal dialog box that displays HTML formated text. */
public class ColorLabel extends JDialog {
    public ColorLabel(int[] colors) {
        super(ij.IJ.getInstance(), "Legends", false);
        Container container = getContentPane();
        
        int numColors = colors.length;
        
        JPanel pnlContainer = new JPanel();
        pnlContainer.setLayout(new GridLayout(numColors, 2, 2, 2));
        pnlContainer.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        container.add(pnlContainer);
        
        for(int i = 0; i < numColors; i++) {
			Color c = new Color(colors[i]);
        
			JPanel colorBox = new JPanel();
			colorBox.setBackground(c);
			pnlContainer.add(colorBox);
			pnlContainer.add(new JLabel("BIN: " + (i + 1)));
        }
        
        setForeground(Color.black);
        pack();
        ij.gui.GUI.center(this);
        setVisible(true);
    }
}