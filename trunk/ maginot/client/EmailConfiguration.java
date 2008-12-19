import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class EmailConfiguration extends JFrame implements ActionListener
{
	JTabbedPane tabPanel;
	TransferObject messagets,messagefs;
	
	JTextField hostfield,fromfield,subjectfield,domainfield;
	JTextArea emailtext;
	Color bgColor,txtColor;
	public final char newline = 10;
	
	public EmailConfiguration(TransferObject messagets,TransferObject messagefs)
	{
		this.messagets = messagets;
		this.messagefs = messagefs;
		getColors();
		setSize(450,625);
		setResizable(false);
		setTitle("Email Manager");
		Container contentPane = getContentPane( );
        contentPane.setLayout(new BorderLayout( ));
        JFrame myWindow = new JFrame();
        myWindow.getContentPane().setLayout(new BorderLayout());
        addWindowListener(new WindowCloser(myWindow));
      
      	// the tabbed pane and all the tabs
        tabPanel = new JTabbedPane();

        //----------------------------------------------------------------------
        //puts the tabs on the tabbed pane. 
        //This is the document Panel
        JPanel panel1 = new JPanel();
        panel1.setBackground(bgColor);
        panel1.setLayout(new BorderLayout());
        tabPanel.addTab("Document",panel1);
		
		
		//the subject stuff.
		JPanel subjectpanel = new JPanel();
		subjectpanel.setBackground(bgColor);
		subjectpanel.setLayout(new FlowLayout());
		
		JLabel subjectlabel = new JLabel("Subject:");
		subjectlabel.setForeground(txtColor);
		subjectpanel.add(subjectlabel);
		
		subjectfield = new JTextField(20);
		subjectpanel.add(subjectfield);
		
		panel1.add(subjectpanel,BorderLayout.NORTH);
		
		
		//all the email body stuff
		JPanel emailtextpanel = new JPanel();
		emailtextpanel.setBackground(bgColor);
		emailtextpanel.setLayout(new FlowLayout());
		
		JLabel emailtextlabel = new JLabel("Body:");
		emailtextlabel.setForeground(txtColor);
		JPanel emailtextlabelpanel = new JPanel();
		emailtextlabelpanel.setBackground(bgColor);
		emailtextlabelpanel.setLayout(new FlowLayout());
		panel1.add(emailtextlabelpanel,BorderLayout.WEST);
		emailtextlabelpanel.add(emailtextlabel);
		
		emailtext = new JTextArea(30,30);
		emailtext.setLineWrap(true);
        emailtext.setWrapStyleWord(true);
        JScrollPane scrolledtext = new JScrollPane(emailtext);
        emailtextpanel.add(scrolledtext);
		panel1.add(emailtextpanel,BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBackground(bgColor);
		buttonPanel.setLayout(new FlowLayout());
		
		panel1.add(buttonPanel,BorderLayout.SOUTH);
		
		JButton savebutton = new JButton("Save");
		savebutton.setForeground(txtColor);
		savebutton.addActionListener(this);
		buttonPanel.add(savebutton);
		
		JButton cancelbutton = new JButton("Cancel");
		cancelbutton.setForeground(txtColor);
		cancelbutton.addActionListener(this);
		buttonPanel.add(cancelbutton);

		//--------------------------------------------------
    	JPanel panel2 = new JPanel();
    	panel2.setBackground(bgColor);
        panel2.setLayout(new FlowLayout());
        tabPanel.addTab("Configuration",panel2);
        
        JPanel flows[] = new JPanel[3];
        
        for(int c = 0; c < 3; c++)
        {
        	flows[c] = new JPanel();
        	flows[c].setBackground(bgColor);
        	flows[c].setLayout(new FlowLayout());
        	panel2.add(flows[c]);
        }
        
        hostfield = new JTextField(20);
        JLabel hostlabel = new JLabel("mail.host=");
        hostlabel.setForeground(txtColor);
        flows[0].add(hostlabel);
        flows[0].add(hostfield);
        
        fromfield = new JTextField(20); 
        JLabel fromlabel = new JLabel("mail.from=");
        fromlabel.setForeground(txtColor);
        flows[1].add(fromlabel);
        flows[1].add(fromfield);
        
        domainfield = new JTextField(20);
        JLabel domainfieldlabel = new JLabel("Domain=");
        domainfieldlabel.setForeground(txtColor);
        flows[2].add(domainfieldlabel);
        flows[2].add(domainfield);
        
        JPanel buttonPanel2 = new JPanel();
        buttonPanel2.setBackground(bgColor);
        buttonPanel2.setLayout(new FlowLayout());
        
        panel2.add(buttonPanel2,BorderLayout.SOUTH);
        
        JButton savebutton2 = new JButton("Save");
        savebutton2.setForeground(txtColor);
        savebutton2.addActionListener(this);
        buttonPanel2.add(savebutton2);
        
        JButton cancelbutton2 = new JButton("Cancel");
        cancelbutton2.setForeground(txtColor);
        cancelbutton2.addActionListener(this);
        buttonPanel2.add(cancelbutton2);
        
	    contentPane.add(tabPanel);
	    
	    readInValues();
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
	public void readInValues()
	{
		String message = "101:mail.host=";
		messagets.putMessage(message);
		String data = messagefs.getMessage().toString();
		if(!data.equalsIgnoreCase("null"))
			hostfield.setText(data);
		
		
		message = "101:mail.from=";
		messagets.putMessage(message);
		data = messagefs.getMessage().toString();
		if(!data.equalsIgnoreCase("null"))
			fromfield.setText(data);
		
		message = "101:domain=";
		messagets.putMessage(message);
		data = messagefs.getMessage().toString();
		if(!data.equalsIgnoreCase("null"))
			domainfield.setText(data);

		message = "101:subject=";
		messagets.putMessage(message);
		data = messagefs.getMessage().toString();
		if(!data.equalsIgnoreCase("null"))
			subjectfield.setText(data);
			
		message = "101:body=";
		messagets.putMessage(message);
		data = messagefs.getMessage().toString();
		if(!data.equalsIgnoreCase("null"))
			emailtext.setText(data);
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
			if(data.charAt(c) == 92)
			{
				data = data.substring(0,c) + " " + data.substring(c+1,data.length());
				length = data.length();
			}
		}
		return data;
	}
	
	public void saveEmail()
	{
		String body = "100:body=" + removeCharacters(emailtext.getText());
		messagets.putMessage(body);
		
		String subject = "100:subject=" + removeCharacters(subjectfield.getText());
		messagets.putMessage(subject);
	}

	public void saveConfigValues()
	{
		
		String mail_config = "100:mail.host=" + removeCharacters(hostfield.getText());
		messagets.putMessage(mail_config);
		
		mail_config = "100:mail.from=" + removeCharacters(fromfield.getText());
		messagets.putMessage(mail_config);
		
		mail_config = "100:domain=" + removeCharacters(domainfield.getText());
		messagets.putMessage(mail_config);
	}

	public void actionPerformed(ActionEvent e)
	{
		String actionCommand = e.getActionCommand( );
		
		if(actionCommand.equals("Save"))
		{
			if(tabPanel.getSelectedIndex() == 0)
			{
				saveEmail();
			}
			else if(tabPanel.getSelectedIndex() == 1)
			{
				saveConfigValues();
			}
		}
		
		if(actionCommand.equals("Cancel"))
		{
			dispose();
		}

	}
}