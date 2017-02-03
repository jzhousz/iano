package entities;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * This class extends from OutputStream to redirect output to a JTextArrea
 * 
 * @author www.codejava.net
 *
 */
public class CustomOutputStream extends OutputStream
{
	private JTextPane textArea;
//	private JTextPane textAreaException;

	/**
	 * A constructor.
	 * 
	 * @param textArea
	 */
	public CustomOutputStream(JTextPane textArea)
	{
		this.textArea = textArea;
//		this.textAreaException = textAreaException;
	}

	@Override
	public void write(int b) throws IOException
	{
		// redirects data to the text area
//		textArea.append(String.valueOf((char) b));
		StyledDocument doc = textArea.getStyledDocument();

		Style style = textArea.addStyle("I'm a Style", null);
		StyleConstants.setForeground(style, Color.red);

		try
		{
			doc.insertString(doc.getLength(), String.valueOf((char) b), style);
		} catch (BadLocationException e)
		{
		}
		
		// scrolls the text area to the end of data
		textArea.setCaretPosition(textArea.getDocument().getLength());
	}
	
}