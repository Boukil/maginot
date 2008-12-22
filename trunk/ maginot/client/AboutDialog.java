import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class AboutDialog extends JFrame implements ActionListener
{
	public static final int WIDTH = 270;
    public static final int HEIGHT = 210;
    
    
	public AboutDialog()
	{
		setSize(WIDTH, HEIGHT);
		setResizable(false);
        setTitle("About: Maginot Software");
        Container contentPane = getContentPane( );
        contentPane.setLayout(new BorderLayout( ));
        
        JTextArea northtext1 = new JTextArea();
        contentPane.add(northtext1, BorderLayout.NORTH);
        northtext1.setEditable(false);
        
        JButton OK = new JButton("OK");
        OK.addActionListener(this);
        contentPane.add(OK, BorderLayout.SOUTH);
        contentPane.setBackground(Color.WHITE);
        
        northtext1.append("Maginot Client v3.0.0\n First Public Release\n");
        northtext1.append("Programmed by: Steven Larizza\n\n\n");
        
        
        northtext1.append("----------------------------------------------------------------\n");
        northtext1.append("Maginot Pronunciation Key: \n [mazh-uh-noh]; Fr. [ma-zhee-noh]");
		this.setVisible(true);
	}

	public void actionPerformed(ActionEvent e)
	{
		String actionCommand = e.getActionCommand( );
		
		if(actionCommand.equals("OK"))
		{
			dispose();
		}

	}
}