import ij.plugin.PlugIn;

/**
 *  This is the stub for IANO under IMAGEJ when used as an IJ plugin.
 *  Its compiled class should be put under ImageJ/plugins/IANO/.
 *  Note that ImageJ/ImageJ.cfg needs to be modified to add cp, library path.
 *
 */
public class IANO_ implements PlugIn {

    /** Creates a new instance of I_ano */

    public IANO_() {
        if(ui == null) ui = new annotool.gui.AnnotatorGUI(null);
    }


    public void run(String arg) {

        if(ui.isVisible() == false) ui.setVisible(true);
    }

    private static annotool.gui.AnnotatorGUI ui = null ;
}
