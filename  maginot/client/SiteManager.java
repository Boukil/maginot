import javax.swing.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.StringTokenizer;
import javax.swing.table.DefaultTableModel;

class SiteManager extends JFrame implements ActionListener
{
    private Container contentPane;
    
    private JPanel panel,panel5;
    
     TransferObject messagefs,messagets;

	TextField phonefield, vlanfield, sitenamefield;
	Color bgColor,txtColor;
	JScrollPane scrollpane1;
	JTable table;
	DefaultTableModel tm;
	
	Object original[][];
	
	
	int table_usernums[];
    int num_clicked = 0;
    
	public SiteManager(TransferObject messagets,TransferObject messagefs)
	{		        	
		this.messagets = messagets;
		this.messagefs = messagefs;
		getColors();
		setSize(600,500);
		//setResizable(false);
		setTitle("New User - Maginot Client");
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
        north_panel5.setLayout(new GridLayout(2,2));
        panel5.add(north_panel5,BorderLayout.NORTH);
        
        JPanel flows[] = new JPanel[3];
        
        for(int c = 0; c < 3; c++)
        {
        	flows[c] = new JPanel();
        	flows[c].setBackground(bgColor);
        	flows[c].setLayout(new FlowLayout());
        	north_panel5.add(flows[c]);
        }
        
        JLabel unamelabel = new JLabel("Site Name:   ");
        unamelabel.setForeground(txtColor);
        sitenamefield = new TextField(20);
        flows[0].add(unamelabel);
        flows[0].add(sitenamefield);
        
        JLabel phonelabel = new JLabel("Phone Number:");
        phonelabel.setForeground(txtColor);
		phonefield = new TextField(20);
		flows[1].add(phonelabel);
        flows[1].add(phonefield);
        
        JLabel vlanlabel = new JLabel("            vLAN:");
        vlanlabel.setForeground(txtColor);
        vlanfield = new TextField(20);
       	flows[2].add(vlanlabel);
       	flows[2].add(vlanfield);
		
		
		//--------------------------------------------------------------------
		JPanel south_panel = new JPanel();
		south_panel.setBackground(bgColor);
		south_panel.setLayout(new FlowLayout());
		contentPane.add(south_panel,BorderLayout.SOUTH);
		
		
		JButton newbutton = new JButton("Save As New Site");
		newbutton.setForeground(txtColor);
		south_panel.add(newbutton);
		newbutton.addActionListener(this);
		
		JButton updatebutton = new JButton("Update This Site");
		updatebutton.setForeground(txtColor);
		south_panel.add(updatebutton);
		updatebutton.addActionListener(this);
		
		showSites();
		this.setVisible(true);
        
	}
	public void actionPerformed(ActionEvent e)
	{
		String actionCommand = e.getActionCommand( );
		
		if(actionCommand.equals("Save As New Site"))
		{
			if(phonefield.getText().equals(""))
			{
				phonefield.setText("null");
			}
			
			int indexselected = table.getSelectedRow();
			String message = "052:" + sitenamefield.getText() + ","+ phonefield.getText() + "," + vlanfield.getText();
			messagets.putMessage(message);
			String result = messagefs.getMessage().toString();
			if(phonefield.getText().equals("null"))
			{
				phonefield.setText("");
			}
			if(result.equals("1"))
			{
				JOptionPane.showMessageDialog(null,"Site Imported");
			}
			else
			{
				JOptionPane.showMessageDialog(null,"Site could not be imported. \n Make sure the server is running.");
			}
			
			showSites();
		}
		
		if(table.getSelectedRow() != -1)
		{
			if(actionCommand.equals("Update This Site"))
			{
				//send the update in this format:
				//UID,username,password,campus,userlevel
				if(phonefield.getText().equals(""))
				{
					phonefield.setText("null");
				}
				int indexselected = table.getSelectedRow();
				System.out.println(original[indexselected][0]);
				String message = "053:" + original[indexselected][0] + ","+ sitenamefield.getText() + ","+ phonefield.getText() + "," + vlanfield.getText();
				messagets.putMessage(message);
				
				String data = messagefs.getMessage().toString();
				if(phonefield.getText().equals("null"))
				{
					phonefield.setText("");
				}
				if(data.equals("1"))
				{
				}
				else
				{
					JOptionPane.showMessageDialog(null,"There is already a site with that name. \n Please try again.");
				}
			
				showSites();
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
	public void showSites()
	{
		num_clicked++;
		if(num_clicked > 1)
		{
			contentPane.remove(scrollpane1);
		}
		//----------------------------------------------------------------------
		//Column name stuff
		messagets.putMessage("051:"); //request the columnnames from the server for the users table
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
		String message = "050:";
		messagets.putMessage(message);
		String data = messagefs.getMessage().toString();
		
		int X = Integer.parseInt(data.substring(0,data.indexOf(",")));
		int Y = Integer.parseInt(data.substring(data.indexOf(",") +1,data.length()));
		Object workorders[][] = new Object[X][Y];
				original = new Object[X][Y];
		for(int c = 0; c < X; c++)
		{
			for(int d = 0; d < Y; d++)
			{
				workorders[c][d] = messagefs.getMessage();
				original[c][d] = workorders[c][d];
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
		//only keep the data that is going on the table
		for(int c = 0; c < X; c++)
		{
			int counter = 0; 
			for(int d = 0; d < Y; d++)
			{
				switch(d)
				{
					case 1:
					case 2:
					case 3:
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
					currenttoken = "Site Name";
					break;
				case 2:
					currenttoken = "Phone";
					break;
				case 3:
					currenttoken = "vLAN";
					break;
			}
			switch(namescounter)
			{
				case 1:
				case 2:
				case 3:
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
           //set the fields to this user
           int indexselected = table.getSelectedRow();
           sitenamefield.setText(table.getValueAt(indexselected,0) + "");
           phonefield.setText(table.getValueAt(indexselected,1) + "");
           vlanfield.setText(table.getValueAt(indexselected,2) + "");
		}
	}
}
