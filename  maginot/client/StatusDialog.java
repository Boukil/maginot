import javax.swing.*;
import java.awt.*;
import java.awt.event.*;



public class StatusDialog  extends JFrame implements ActionListener
{
	public static final int WIDTH = 270;
    public static final int HEIGHT = 165;
    
     TransferObject messagefs,messagets;
     Color bgColor,txtColor;
    
	public StatusDialog(TransferObject messagets, TransferObject messagefs)
	{
		this.messagefs = messagefs;
		this.messagets = messagets;
		getColors();
		
		setSize(WIDTH, HEIGHT);
		setResizable(false);
        setTitle("Status");
        Container contentPane = getContentPane( );
        contentPane.setBackground(bgColor);
        contentPane.setLayout(new BorderLayout( ));
        
        JLabel label1 = new JLabel("Connected as:");
        label1.setForeground(txtColor);
        
        JPanel flowpanel = new JPanel();
        flowpanel.setBackground(bgColor);
        contentPane.add(flowpanel,BorderLayout.CENTER);
        
        flowpanel.add(label1);
        
        messagets.putMessage("060:");
       	String data = messagefs.getMessage().toString();
       	
       	JLabel label2 = new JLabel(data);
       	label2.setForeground(txtColor);
       	flowpanel.add(label2);
        
		this.setVisible(true);
	}
	
	public void getColors()
	{
		//gets the color stuff
		messagets.putMessage("142:");
		String temp_color = messagefs.getMessage().toString();
		
		int r = Integer.parseInt(temp_color.substring(0,temp_color.indexOf(",")));
		int g = Integer.parseInt(temp_color.substring(temp_color.indexOf(",")+1,temp_color.lastIndexOf(",")));
		int b = Integer.parseInt(temp_color.substring(temp_color.lastIndexOf(",")+1,temp_color.length()));
		
		bgColor = new Color(r,g,b);
		
		messagets.putMessage("143:");
		temp_color = messagefs.getMessage().toString();
		
		r = Integer.parseInt(temp_color.substring(0,temp_color.indexOf(",")));
		g = Integer.parseInt(temp_color.substring(temp_color.indexOf(",")+1,temp_color.lastIndexOf(",")));
		b = Integer.parseInt(temp_color.substring(temp_color.lastIndexOf(",")+1,temp_color.length()));
		
		txtColor = new Color(r,g,b);
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