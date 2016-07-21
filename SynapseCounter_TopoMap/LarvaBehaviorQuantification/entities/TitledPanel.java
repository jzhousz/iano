package entities;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
   
public class TitledPanel extends JPanel {
     
    private String title;
    private Insets insets = new Insets( 10, 10, 10, 10 );
     
    public TitledPanel() {
        this( "" );
    }
     
    public TitledPanel( String title ) {
        this.title = title;
    }
     
    public Insets getInsets() {
        return insets;
    }
     
    public void paint( Graphics g ) {
        super.paint( g );
//        g.setColor( getForeground() );
        g.setColor( Color.lightGray );
        g.drawRect( 5, 5, getWidth() - 10, getHeight() - 10 );
        int width = g.getFontMetrics().stringWidth( title );
        g.setColor( getBackground() );
//        g.setColor( Color.lightGray );
        g.fillRect( 10, 0, width, 10 );
        g.setColor( getForeground() );
        g.drawString( title, 10, 10 );
    }
     
    public static void main( String[] args ) {
        JFrame f = new JFrame( "TitledPanel Tester" );
         
        TitledPanel p = new TitledPanel( "Title of Panel" );
        p.add( new Label( "Label 1" ) );
        p.add( new Label( "Label 2" ) );
        p.add( new Label( "Label 3" ) );
        f.add( p );
         
        f.addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent e ) {
                System.exit( 0 ); 
            }
        } );
        f.setBounds( 300, 300, 300, 300 );
        f.setVisible( true );
    }
}