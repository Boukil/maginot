
import java.awt.*;
import java.awt.event.*;

/**
 If you register an object of this class as a listener to any
 object of the class JFrame, then if the user clicks the
 close-window button in the JFrame, the object of this class
 will end the program and close the JFrame.
*/


public class WindowDestroyer extends WindowAdapter
{
	TransferObject messagets;
	public WindowDestroyer (TransferObject messagets)
	{
		this.messagets = messagets;
	}
	
    public void windowClosing(WindowEvent e)
    {
    	messagets.putMessage("027:");
    	System.exit(0);		
    }
}
