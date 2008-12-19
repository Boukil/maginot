import javax.swing.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;



class ChangeMyPassword extends JFrame implements ActionListener
{
	private TextField oldpasswordfield;
    private TextField password1field;
    private TextField password2field;
    private Container contentPane;
    
    private JPanel panel;
    Color txtColor,bgColor;
    TransferObject messagefs,messagets;
    String username;
    
	public ChangeMyPassword(TransferObject messagets,TransferObject messagefs,String username)
	{		       
		this.username = username; 	
		this.messagets = messagets;
		this.messagefs = messagefs;
		getColors();
		
		setSize(320, 160);
		//setResizable(false);
		setTitle("Change Password");
		Container contentPane = getContentPane( );
		contentPane.setLayout(new BorderLayout( ));
		
		
		JFrame myWindow = new JFrame();
		myWindow.getContentPane().setLayout(new BorderLayout());
		addWindowListener(new WindowCloser(myWindow));
		contentPane.setBackground(bgColor);
		
		JLabel password1label = new JLabel("New password:");
		password1label.setForeground(txtColor);
		JLabel password2label = new JLabel("Confirm new password:");
		password2label.setForeground(txtColor);
		
		password1field = new TextField(20);
		password2field = new TextField(20);
		
		password1field.setEchoChar('*');
		password2field.setEchoChar('*');
		
		password1field.addActionListener(this);
		password2field.addActionListener(this);
		
		
		panel = new JPanel();
		panel.setBackground(bgColor);
		panel.setLayout(new BorderLayout());
		contentPane.add(panel);
		
		
		JPanel west_Panel = new JPanel();
		west_Panel.setBackground(bgColor);
		west_Panel.setLayout(new GridLayout(3,1));
		panel.add(west_Panel,BorderLayout.WEST);
		
		west_Panel.add(password1label);
		west_Panel.add(password2label);
		
		JPanel east_Panel = new JPanel();
		east_Panel.setBackground(bgColor);
		east_Panel.setLayout(new GridLayout(3,2));
		panel.add(east_Panel,BorderLayout.EAST);
		
		JPanel flow_panel2 = new JPanel();
		flow_panel2.setBackground(bgColor);
		JPanel flow_panel3 = new JPanel();
		flow_panel3.setBackground(bgColor);
		
		east_Panel.add(flow_panel2);
		east_Panel.add(flow_panel3); 
		
		flow_panel2.add(password1field);
		flow_panel3.add(password2field);
		
		JPanel buttonflow = new JPanel();
		buttonflow.setBackground(bgColor);
		buttonflow.setLayout(new FlowLayout());
		panel.add(buttonflow,BorderLayout.SOUTH);
		
		JButton loginbutton = new JButton("Login");
		loginbutton.setForeground(txtColor);
		loginbutton.addActionListener(this);
		buttonflow.add(loginbutton);
		this.setVisible(true);
        
	}
	public void actionPerformed(ActionEvent e)
	{
		String actionCommand = e.getActionCommand( );
	

		if((actionCommand.equals("Login")) || (password2field == e.getSource()) || (password1field == e.getSource()))
		{
			//make sure the new passwords match before we query the server. 
			//theres not reason to query the server if the passwords don't match anyway.
			
			String pw1 = password1field.getText();
			String pw2 = password2field.getText();
			
			if(pw1.equals(pw2))
			{
				//the passwords match so now we need to query the server for the password
				//to make sure the old passwords match.
				
				String password = password1field.getText();
				Encryption f = new Encryption();
				f.setPassword(password);
				
				String message = "024:" + removeCharacters(f.getPassword()) + "," + username;
				messagets.putMessage(message);
				JOptionPane.showMessageDialog(null,"Your password has been changed");
				this.dispose();
			}
			else
			{
				JOptionPane.showMessageDialog(null,"Your new passwords do not match. Please try again");
				password1field.setText("");
				password2field.setText("");
			}
		}
	}
	public String removeCharacters(String data)
	{
		int length = data.length();
		
		for(int c = 0; c < length; c ++)
		{
			if(data.charAt(c) == 44) //if the character is a ,
			{
				data = data.substring(0,c) + " " + data.substring(c+1,data.length());
				length = data.length();
			}
			if(data.charAt(c) == 39) //if the character is a '
			{
				data = data.substring(0,c) + " " + data.substring(c+1,data.length());
				length = data.length();
			}
			if(data.charAt(c) == 59) //if the character is a :
			{
				data = data.substring(0,c) + " " + data.substring(c+1,data.length());
				length = data.length();
			}
			if(data.charAt(c) == 58) //if the character is a ;
			{
				data = data.substring(0,c) + " " + data.substring(c+1,data.length());
				length = data.length();
			}
			if((data.charAt(c) == 40) || (data.charAt(c) == 41)) //if the character is a ( or a )
			{
				data = data.substring(0,c) + " " + data.substring(c+1,data.length());
				length = data.length();
			}
			if(data.charAt(c) == 92)
			{
				data = data.substring(0,c) + " " + data.substring(c+1,data.length());
				length = data.length();
			}
			
		}
		return data;
		
		
		
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
