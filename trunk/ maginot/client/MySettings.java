import javax.swing.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.text.*;


public class MySettings extends JFrame implements ActionListener
{
	
	public static final int WIDTH = 260;
    public static final int HEIGHT = 600;
    
    private JTabbedPane outputPanel;
    private JPanel north_panel_tab1;

    JPanel panel1;
    
    TransferObject messagets, messagefs;
	Color bgColor,txtColor;
	public MySettings(TransferObject messagets,TransferObject messagefs)
	{
		this.messagets = messagets;
		this.messagefs = messagefs;
		getColors();
		
		setSize(WIDTH, HEIGHT);
		setResizable(false);
        setTitle("Maginot - MySettings");
        Container contentPane = getContentPane( );
        contentPane.setLayout(new BorderLayout( ));
        JFrame myWindow = new JFrame();
        myWindow.getContentPane().setLayout(new BorderLayout());
        addWindowListener(new WindowCloser(myWindow));
      
      	// the tabbed pane and all the tabs
         outputPanel = new JTabbedPane();
      
        // the main panel.  Everything is added here. 
        JPanel main_panel = new JPanel();
        main_panel.setBackground(bgColor);
        main_panel.setLayout(new FlowLayout());
        contentPane.add(main_panel);
        
        //----------------------------------------------------------------------
        //puts the tabs on the tabbed pane. 
		
		panel1 = new JPanel();
		panel1.setBackground(bgColor);
    	outputPanel.addTab("Colors", panel1);
		
		//----------------------------------------------------------------------
		//this is tab 1
		JButton colorButton = new JButton("Choose Background Color");
		colorButton.setForeground(txtColor);
    	colorButton.addActionListener(this);
    	panel1.add(colorButton);
    	
    	JButton textButton = new JButton("     Choose Text Color          ");
    	textButton.setForeground(txtColor);
    	textButton.addActionListener(this);
    	panel1.add(textButton);
    	
    	JButton resetButton = new JButton("Reset My Colors");
    	resetButton.setForeground(txtColor);
    	resetButton.addActionListener(this);
    	panel1.add(resetButton);
    	//end tab 1
    	//----------------------------------------------------------------------
		
        contentPane.add(outputPanel, BorderLayout.CENTER);
        this.setVisible(true);
	}
	public void actionPerformed(ActionEvent a)
	{
		
		String actionCommand = a.getActionCommand( );
		
		if(actionCommand.equals("Choose Background Color"))
		{
			bgColor= JColorChooser.showDialog(this,"Choose Background Color",getBackground());
    		if (bgColor != null)
    		{
    			panel1.setBackground(bgColor);
    			
    			String color = bgColor.toString();
    			
    			String r = color.substring(color.indexOf("r=")+2,color.indexOf(","));
    			String g = color.substring(color.indexOf("g=")+2,color.indexOf(",",color.indexOf("g=")));
    			String b = color.substring(color.indexOf("b=")+2,color.indexOf("]",color.indexOf("b=")));
    			
    			color = r + " " + g + " " + b;
    			
      			messagets.putMessage("140:" + color);
      			JOptionPane.showMessageDialog(null, "You must restart the program for your settings to take full effect.");
    		}	
		}
		if(actionCommand.equals("     Choose Text Color          "))
		{
			txtColor= JColorChooser.showDialog(this,"Choose Text Color",getBackground());
      		if(txtColor != null)
      		{
      			String color = txtColor.toString();
    			
    			String r = color.substring(color.indexOf("r=")+2,color.indexOf(","));
    			String g = color.substring(color.indexOf("g=")+2,color.indexOf(",",color.indexOf("g=")));
    			String b = color.substring(color.indexOf("b=")+2,color.indexOf("]",color.indexOf("b=")));
    			
    			color = r + " " + g + " " + b;
    			
      			messagets.putMessage("141:" + color);
      			JOptionPane.showMessageDialog(null, "You must restart the program for your settings to take full effect.");
      		}		
      	}
      	if(actionCommand.equals("Reset My Colors"))
      	{
      		messagets.putMessage("141:");
      		messagets.putMessage("140:");
      		JOptionPane.showMessageDialog(null, "You must restart the program for your settings to take full effect.");
      	}
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
}