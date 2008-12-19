/* this class differes from the WindowDestroyer class because
 *when this window listener is added to a JFrame, and the X is clicked to close the
 *window, only that window closes; the program does not end. If the WindowDestroyer 
 *listener is added to the JFrame, and the X is clicked, the whole program terminates.
 */


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class WindowCloser extends WindowAdapter
{
	JFrame window;
	
	public WindowCloser(JFrame window)
	{
		this.window = window;	
	}
    public void windowClosing(WindowEvent e)
    {
        window.dispose();
    }
}