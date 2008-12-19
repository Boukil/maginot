import javax.swing.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.StringTokenizer;
import javax.swing.table.DefaultTableModel;
import java.util.Arrays;

class UserManager extends JFrame implements ActionListener
{
    private Container contentPane;
    
    private JPanel panel,panel5;
    
    TransferObject messagefs,messagets;
    
	Color bgColor,txtColor;
    JComboBox campusbox;
    TextField usernamefield; 
	TextField password1field; 
	TextField password2field;
	JScrollPane scrollpane1;
	JTable table;
	DefaultTableModel tm;
	
	Object workorders[][];
	
	JComboBox usertype;
	String usertypes[] = {"Help Desk","Technician","Standard User"};
	
	JComboBox activebox;
	String activetypes[] = {"No","Yes"};
	
	int table_usernums[];
    int num_clicked = 0;
    
	public UserManager(TransferObject messagets,TransferObject messagefs)
	{		        	
		this.messagets = messagets;
		this.messagefs = messagefs;
		getColors();
		setSize(600,500);
		//setResizable(false);
		setTitle("User Manager - Maginot Client");
		contentPane = getContentPane( );
		contentPane.setLayout(new BorderLayout( ));
		
		JFrame myWindow = new JFrame();
		myWindow.getContentPane().setLayout(new BorderLayout());
		addWindowListener(new WindowCloser(myWindow));
		
		
		
		//--------------------------------------------------------------------
		panel5 = new JPanel();
		panel5.setBackground(bgColor);
        panel5.setLayout(new BorderLayout());
        contentPane.add(panel5,BorderLayout.NORTH);
        
        JPanel north_panel5 = new JPanel();
        north_panel5.setBackground(bgColor);
        north_panel5.setLayout(new GridLayout(3,2));
        panel5.add(north_panel5,BorderLayout.NORTH);
        
        JPanel flows[] = new JPanel[6];
        
        for(int c = 0; c < 6; c++)
        {
        	flows[c] = new JPanel();
        	flows[c].setBackground(bgColor);
        	flows[c].setLayout(new FlowLayout());
        	north_panel5.add(flows[c]);
        }
        
        JLabel unamelabel = new JLabel("User Name:               ");
        unamelabel.setForeground(txtColor);
        usernamefield = new TextField(20);
        flows[0].add(unamelabel);
        flows[0].add(usernamefield);
        
        
		JLabel pwordlabel = new JLabel("Password:                ");
		pwordlabel.setForeground(txtColor);
		password1field = new TextField(20);
		flows[1].add(pwordlabel);
        flows[1].add(password1field);
		
		//get a list of sites from the server
	    String message2 = "054:";
	    messagets.putMessage(message2);
	    String campusoptions[] = messagefs.getMessage().toString().split(","); //we do this just so we can get the number of sites
	    Arrays.sort(campusoptions);
		
		JLabel campuslabel = new JLabel("Campus:");
		campuslabel.setForeground(txtColor);
		campusbox = new JComboBox(campusoptions);
		flows[2].add(campuslabel);
        flows[2].add(campusbox);
        
        JLabel confirmpwlabel = new JLabel("Confirm Password:");
        confirmpwlabel.setForeground(txtColor);
		password2field = new TextField(20);
		flows[3].add(confirmpwlabel);
        flows[3].add(password2field);
		
		JLabel typeslabel = new JLabel("User Type:");
		typeslabel.setForeground(txtColor);
		usertype = new JComboBox(usertypes);
		flows[4].add(typeslabel);
        flows[4].add(usertype);
        
        JLabel activelabel = new JLabel("Active:");
        activelabel.setForeground(txtColor);
        activebox = new JComboBox(activetypes);
        flows[5].add(activelabel);
        flows[5].add(activebox);
		
		//--------------------------------------------------------------------
		JPanel south_panel = new JPanel();
		south_panel.setBackground(bgColor);
		south_panel.setLayout(new FlowLayout());
		contentPane.add(south_panel,BorderLayout.SOUTH);
		
		
		JButton newbutton = new JButton("Save As New User");
		newbutton.setForeground(txtColor);
		south_panel.add(newbutton);
		newbutton.addActionListener(this);
		
		JButton updatebutton = new JButton("Update This User");
		updatebutton.setForeground(txtColor);
		south_panel.add(updatebutton);
		updatebutton.addActionListener(this);
		
		showUsers();
		this.setVisible(true);
        
	}
	public void actionPerformed(ActionEvent e)
	{
		String actionCommand = e.getActionCommand( );
		
		if(actionCommand.equals("Save As New User"))
		{
			if((password1field.getText().equals(password2field.getText())) && (password1field.getText() != null) && (!password1field.getText().equals("")))
			{
				String user_name = removeCharacters(usernamefield.getText());
				String password = removeCharacters(password1field.getText());
				String campus = campusbox.getSelectedItem().toString();
				int userlevel = usertype.getSelectedIndex();
				int active = activebox.getSelectedIndex();
				
				//the campus actually needs to be the campus number, not name.
				String message = "056:" + campus;
				messagets.putMessage(message);
				campus = messagefs.getMessage().toString();
				
				//Encrypt the password
				Encryption g = new Encryption();
				g.setPassword(password1field.getText());
				password = removeCharacters(g.getPassword());
				
				
				//check to make sure a user with this username does not already exist.
				message = "046:" + user_name;
				messagets.putMessage(message);
				String data = messagefs.getMessage().toString();
				//if data is actually equal to some value that is parsable, do NOT add the user.
				//if when the data variable is parsed, that means a user with that name doesn't exist. Issue the command to add the user.
				try
				{
					int data2 = Integer.parseInt(data);//we WANT this to throw an exception. Otherwise the user won't be created.
					JOptionPane.showMessageDialog(null,"That user already exists");	
				}
				catch(Exception f)
				{
					message = "042:" + user_name + "," + campus + "," + userlevel + "," + active + "," + password;
					messagets.putMessage(message);
				}
			}
			else
			{
				JOptionPane.showMessageDialog(null,"Your passwords do not match or are blank.\n Passwords cannot be blank. \n Please try again");
				password1field.setText("");
				password2field.setText("");
			}
		}
		if(actionCommand.equals("Update This User"))
		{
			if((password1field.getText().equals(password2field.getText())) && (password1field.getText() != null) && (!password1field.getText().equals("")))
			{
				String user_name = removeCharacters(usernamefield.getText());
				String password = removeCharacters(password1field.getText());
				String campus = campusbox.getSelectedItem().toString();
				int userlevel = usertype.getSelectedIndex();
				int active = activebox.getSelectedIndex();
				
				//the campus actually needs to be the campus number, not name.
				String message = "056:" + campus;
				messagets.putMessage(message);
				campus = messagefs.getMessage().toString();
				
				//Encrypt the password
				Encryption g = new Encryption();
				g.setPassword(password1field.getText());
				password = removeCharacters(g.getPassword());
				
				
				//translates this user_name to a uid so we know what user to update.
				message = "046:" + user_name;
				messagets.putMessage(message);
				String data = messagefs.getMessage().toString();  
				//this part is the opposite of the above. Here, we need the uid of the user trying to be updated.
				try
				{
					int data2 = Integer.parseInt(data);
					message = "043:" + user_name + "," + campus + "," + userlevel + "," + active + "," + password + "," + data2;
					messagets.putMessage(message);
				}
				catch(Exception f)
				{
					JOptionPane.showMessageDialog(null,"Cannot update because the user doesn't exist.");	
					
				}
			}
			else
			{
				JOptionPane.showMessageDialog(null,"Your passwords do not match or are blank.\n Passwords cannot be blank. \n Please try again");
				password1field.setText("");
				password2field.setText("");
			}
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
	public void showUsers()
	{
		num_clicked++;
		if(num_clicked > 1)
		{
			contentPane.remove(scrollpane1);
		}
		//----------------------------------------------------------------------
		//Column name stuff
		messagets.putMessage("041:"); //request the columnnames from the server for the users table
		String raw_columnNames = refineColumnNames(messagefs.getMessage().toString());//get the columnname message from the server
		
		//split up the column names and put it in this array of objects below.
		StringTokenizer tokenizer = new StringTokenizer(raw_columnNames,",");
		Object[] columnNames = new Object[tokenizer.countTokens()];
		
		int index = 0;
		while(tokenizer.hasMoreTokens())
		{
			columnNames[index] = tokenizer.nextToken();
			index++;
		}
		//----------------------------------------------------------------------
		//gets the information from the server
		String message = "040:";
		messagets.putMessage(message);
		String data = messagefs.getMessage().toString();
		
		int X = Integer.parseInt(data.substring(0,data.indexOf(",")));
		int Y = Integer.parseInt(data.substring(data.indexOf(",") +1,data.length()));
		workorders = new Object[X][Y];
		for(int c = 0; c < X; c++)
		{
			for(int d = 0; d < Y; d++)
			{
				workorders[c][d] = messagefs.getMessage();
			}
		}
		workorders = refineDataForTable(workorders);
		//----------------------------------------------------------------------
		
		tm = new DefaultTableModel() {public boolean isCellEditable(int rowIndex, int mColIndex) {return false;}}; 
		tm.setDataVector(workorders,columnNames);
		
		table = new JTable(tm);
		table.addMouseListener(new UserHistoryMouseAdapter());
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setAutoCreateRowSorter(true);
		
		scrollpane1 = new JScrollPane(table);
		contentPane.add(scrollpane1,BorderLayout.CENTER);
		contentPane.validate();
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
	
	public Object[][] refineDataForTable(Object object[][])
	{
		int X = object.length;
		int Y = object[0].length;
		Object refinedobject[][] = new Object[X][7];
		
		//----------------------------------------------------------------------
		//gets the information from the server
		String message = "050:"; //gets a list of sites
		messagets.putMessage(message);
		String data = messagefs.getMessage().toString();
		
		int X2 = Integer.parseInt(data.substring(0,data.indexOf(",")));
		int Y2 = Integer.parseInt(data.substring(data.indexOf(",") +1,data.length()));
		Object sites[][] = new Object[X2][Y2];
		for(int c = 0; c < X2; c++)
		{
			for(int d = 0; d < Y2; d++)
			{
				sites[c][d] = messagefs.getMessage();
			}
		}
		//----------------------------------------------------------------------
		//only keep the data that is going on the table
		for(int c = 0; c < X; c++)
		{
			int counter = 0; 
			for(int d = 0; d < Y; d++)
			{
				if(d==3)
				{
					//look for the site ID
					for(int f = 0; f < sites.length; f++)
					{
						if(object[c][d].equals(sites[f][0]))
						{
							//replace the ID with the number
							object[c][d] = sites[f][1];
							break;//abort the loop.
						}
					}
				}
				switch(d)
				{
					case 1:
					case 3:
					case 4:
					case 5:
					case 6:
					try
					{
						if(object[c][d] == (null))
						{
							object[c][d] = "";
						}
						else if(object[c][d].toString().equalsIgnoreCase("null"))
						{
							object[c][d] ="";
						}
						refinedobject[c][counter] = object[c][d];
						counter++;
					}
					catch(NullPointerException f)
					{
						counter++;
					}
						
					break;
				}
			}
		}
		return refinedobject;
	}
	public String refineColumnNames(String raw_columnNames)
	{
		//--------------------------------------------------------------
		//we are only going to keep some of the columnNames... the others will be removed.
		//--------------------------------------------------------------
		StringTokenizer pretokenizer = new StringTokenizer(raw_columnNames,",");
		
		String new_columnNames = "";
		
		int namescounter = 0;
		
		while(pretokenizer.hasMoreTokens())
		{
			String currenttoken = pretokenizer.nextToken();
			switch(namescounter)
			{
				case 1:
					currenttoken = "User Name";
					break;
				case 3:
					currenttoken = "Campus";
					break;
				case 4:
					currenttoken = "User Level";
					break;
				case 5:
					currenttoken = "Last Login";
					break;
				case 6:
					currenttoken = "Active";
					break;
				
			}
			switch(namescounter)
			{
				case 1:
				case 3:
				case 4:
				case 5:
				case 6:
					new_columnNames = new_columnNames + "," + currenttoken;
				break;
			}
			//------------------------------------------------------------------
			
			
			namescounter++;
		}
		return new_columnNames;
	}

	class UserHistoryMouseAdapter extends MouseAdapter
	{
		public void mouseClicked(MouseEvent e)
		{
			int selectedrow = table.getSelectedRow();
			usernamefield.setText(table.getValueAt(selectedrow,0).toString());
			campusbox.setSelectedItem(table.getValueAt(selectedrow,1).toString());
			usertype.setSelectedIndex(Integer.parseInt(table.getValueAt(selectedrow,2).toString()));
			activebox.setSelectedIndex(Integer.parseInt(table.getValueAt(selectedrow,4).toString()));
		}
	}
}
