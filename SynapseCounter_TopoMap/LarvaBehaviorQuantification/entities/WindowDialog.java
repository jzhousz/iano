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

public class WindowDialog extends Dialog 
{
	private String message = null;
	
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
//        g.drawString("Warning:", 25,70 );
        g.drawString(message, 25, 90); 
        //g.drawString("TutorialsPoint.Com", 25,70 );
        //g.drawString("Version 1.0", 60, 90);      
     }
  }
