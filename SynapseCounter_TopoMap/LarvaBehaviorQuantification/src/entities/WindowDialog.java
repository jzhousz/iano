package entities;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Event;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
* The sub-class of Dialog.
* 
* @author  Yaoguang Zhong
* @version 1.1
* @since   08-02-2016
*/
public class WindowDialog extends Dialog 
{
	private String message = null;
	
	/**
	 * A constructor.
	 * @param parent The parent frame.
	 * @param message The message added in the dialog.
	 */
    public WindowDialog(Frame parent, String message)
    {
        super(parent, true);  
        this.message = message;
        
        setBackground(Color.gray);
        setLayout(new BorderLayout());
        Panel panel = new Panel();
        panel.add(new Button("Close"));
        add("South", panel);
        setSize(250,200);

        addWindowListener(new WindowAdapter() {
           public void windowClosing(WindowEvent windowEvent){
              dispose();
           }
        });
     }

     public boolean action(Event evt, Object arg)
     {
        if(arg.equals("Close")){
           dispose();
           return true;
        }
        return false;
     }

     public void paint(Graphics g)
     {
        g.setColor(Color.white);
        g.drawString(message, 25, 90); 
     }
  }
