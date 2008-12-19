import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.StringTokenizer;
import javax.swing.table.DefaultTableModel;
import java.util.Arrays;

public class Reports extends JFrame implements ActionListener
{
	 TransferObject messagefs,messagets;
	int userlevel;
	Container contentPane;
	
	JPanel panel,north_panel,south_panel,center_flow;
	
	String reportsoptions[] = { "Work orders closed by technician",
								"Work orders closed by campus",
								"Work orders open by campus",
								"Work Orders Open by ECISD number",
								"Work orders assigned to technician",
								"Summary of closed work orders by campus",
								"Summary of closed work orders by technician",
								"Summary of open work orders by campus",
								"Summary of open work orders assigned to technician"};
									/*,
								"Average TTC per campus",
								"Average TTC per technician",
								"Average TTC for all closed work orders"
								};*/
								

								
								
	JComboBox reports,techbox,campusbox;
	JButton displaybutton;
	JTextField ecisdnumfield;
	
	//printing variable
	boolean tabledisplayed = false;
	
	//stuff for the table
	int workordernums[];
	Object original[][];
	DefaultTableModel tm;
	JTable table;
	JScrollPane scrollpane;
	public Reports(TransferObject messagets, TransferObject messagefs,int userlevel)
	{   
		this.messagefs = messagefs;
		this.messagets = messagets;
		this.userlevel = userlevel;
		drawInterface(); //this is here so the interface can be redrawn over and over
		
			
		//----------------------------------------------------------------------
        //file menu
        
        //Creates the Menus
		JMenu file = new JMenu("File");

		JMenuItem print = new JMenuItem("Print");
		print.addActionListener(this);
		file.add(print);
		
		// creates the menu bar
		JMenuBar mBar = new JMenuBar(); 
		mBar.add(file);
		setJMenuBar(mBar);

		//-----------------------------------------------------------------
	}
	
	public void drawInterface()
	{
		try{panel.remove(south_panel);}	
		catch(Exception e){}
		
		try{panel.remove(north_panel);}
		catch(Exception e){}
		
		try{contentPane.remove(panel);}
		catch(Exception e){}
		
		try{contentPane.remove(scrollpane);}
		catch(Exception e){}
		
		try{contentPane.remove(center_flow);}
		catch(Exception e){}
		
		setSize(690, 690);
		//setResizable(false);
		setTitle("Work Order Login");
		contentPane = getContentPane( );
		contentPane.setLayout(new BorderLayout( ));
		JFrame myWindow = new JFrame();
		myWindow.getContentPane().setLayout(new BorderLayout());
		addWindowListener(new WindowCloser(myWindow));
		
		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		contentPane.add(panel,BorderLayout.NORTH);
		
		north_panel = new JPanel();
		north_panel.setLayout(new FlowLayout());
		panel.add(north_panel,BorderLayout.NORTH);
		
		south_panel = new JPanel();
		south_panel.setLayout(new FlowLayout());
		panel.add(south_panel,BorderLayout.SOUTH);
		
		int selectedindex = 0;
		try{selectedindex = reports.getSelectedIndex();}
		catch(Exception e){}
		
		reports = new JComboBox(reportsoptions);
		
		try{reports.setSelectedIndex(selectedindex);}
		catch(Exception e){}
		reports.addActionListener(this);

		
		north_panel.add(reports);
		contentPane.validate();
	}

	public void actionPerformed(ActionEvent e)
	{
		String actionCommand = e.getActionCommand();
		
		if(e.getSource() == reports)
		{
			tabledisplayed = false;
			drawInterface();
			//depending on what is selected in this box, display the second box.
			switch(reports.getSelectedIndex())
			{
				case 0:
				case 4:
					JLabel techboxlabel = new JLabel("             Select Technician:");
					south_panel.add(techboxlabel);
		
					messagets.putMessage("025:");
					String message[] = messagefs.getMessage().toString().split(",");
					Arrays.sort(message);
					techbox = new JComboBox(message);
					
					south_panel.add(techbox);
		
					displaybutton = new JButton("Display");
					displaybutton.addActionListener(this);
					south_panel.add(displaybutton);
				break;
				case 1:
				case 2:
					//get a list of sites from the server
			        String message2 = "054:";
			        messagets.putMessage(message2);
			        String campusoptions[] = messagefs.getMessage().toString().split(",");
					Arrays.sort(campusoptions);
					
					JLabel campusboxlabel = new JLabel("Select Campus:");
					south_panel.add(campusboxlabel);
					campusbox = new JComboBox(campusoptions);
					south_panel.add(campusbox);
					
					displaybutton = new JButton("Display");
					displaybutton.addActionListener(this);
					
					south_panel.add(displaybutton);
				break;
				case 3:
					JLabel ecisdboxlabel = new JLabel("Enter ECISD #:");
					south_panel.add(ecisdboxlabel);
					
					ecisdnumfield = new JTextField(20);
					
					south_panel.add(ecisdnumfield);
					
					displaybutton = new JButton("Display");
					displaybutton.addActionListener(this);
					
					south_panel.add(displaybutton);
				break;
				case 5:
				case 6:
				case 7:
				case 8:
					actionCommand = "Display"; //its a summary, so go ahead and go below.
				break;
				case 9:
				case 10:
					TTC();
				break;
			}
			contentPane.validate();
		}
		if(actionCommand.equals("Print"))
		{
			//we only want to do this, if there is a table displayed on the gui.
			if(tabledisplayed)
			{
				switch(reports.getSelectedIndex())
				{
					 case 0:
					 case 1:
					 case 2:
					 case 4:
					 	PrintJTable printer = new PrintJTable(table);
					 break;
					 default:
					 	JOptionPane.showMessageDialog(null,"This screen is not currently eligible for printing");
					 break;
					 
				}
			}
			else
				JOptionPane.showMessageDialog(null,"This screen is not currently eligible for printing");
		}
		
		
		if(actionCommand.equals("Display"))
		{
			//generates the SQL query
			String sql = "020:" + "SELECT * FROM workorders ";
			switch(reports.getSelectedIndex())
			{
				case 0://Work orders closed by Technician
					String sql2 = "020: SELECT USER_ID FROM users WHERE user_name ='" + techbox.getSelectedIndex() + "';";
					messagets.putMessage(sql2);
					String message = messagefs.getMessage().toString();
					
					sql = "019: SELECT * FROM workorders WHERE status=3 AND closed_by='" + message +"';";
					messagets.putMessage(sql);
					String data = messagefs.getMessage().toString();
		
					int X = Integer.parseInt(data.substring(0,data.indexOf(",")));
					int Y = Integer.parseInt(data.substring(data.indexOf(",") +1,data.length()));
					Object workorders[][] = new Object[X][Y];
					for(int c = 0; c < X; c++)
					{
						for(int d = 0; d < Y; d++)
						{
							workorders[c][d] = messagefs.getMessage();
						}
					}
					workorders = refineDataForTable(workorders);
					
					displayTable(workorders);
					tabledisplayed = true;
				break;
				case 1://Work orders closed by Campus
					//translate the Selected Index of the campus box to an actual index in the database. 
					message = "056:" + campusbox.getSelectedItem();
					messagets.putMessage(message);
					String campus_index = messagefs.getMessage().toString();
					
					sql = "019: SELECT * FROM workorders WHERE status=3 AND campus='" + campus_index + "';";
					messagets.putMessage(sql);
					data = messagefs.getMessage().toString();
		
					X = Integer.parseInt(data.substring(0,data.indexOf(",")));
					Y = Integer.parseInt(data.substring(data.indexOf(",") +1,data.length()));
					workorders = new Object[X][Y];
					for(int c = 0; c < X; c++)
					{
						for(int d = 0; d < Y; d++)
						{
							workorders[c][d] = messagefs.getMessage();
						}
					}
					workorders = refineDataForTable(workorders);
					
					displayTable(workorders);
					tabledisplayed = true;
				break;
				case 2://Work orders open by Campus
					//translate the Selected Index of the campus box to an actual index in the database. 
					message = "056:" + campusbox.getSelectedItem();
					messagets.putMessage(message);
					campus_index = messagefs.getMessage().toString();
					
					sql = "019: SELECT * FROM workorders WHERE status=1 AND campus='" + campus_index + "';";
					messagets.putMessage(sql);
					data = messagefs.getMessage().toString();
		
					X = Integer.parseInt(data.substring(0,data.indexOf(",")));
					Y = Integer.parseInt(data.substring(data.indexOf(",") +1,data.length()));
					workorders = new Object[X][Y];
					for(int c = 0; c < X; c++)
					{
						for(int d = 0; d < Y; d++)
						{
							workorders[c][d] = messagefs.getMessage();
						}
					}
					workorders = refineDataForTable(workorders);
					
					displayTable(workorders);
					tabledisplayed = true;
				break;
				case 3://Work Orders Open by ECISD Number
					if(!ecisdnumfield.getText().equals(""))
					{
						try
						{
							int a = Integer.parseInt(ecisdnumfield.getText());
						}
						catch(Exception f)
						{
							ecisdnumfield.setText("0");
						}
						sql = "019: SELECT * FROM workorders WHERE status=1 AND ecisd_number=" + removeCharacters(ecisdnumfield.getText()) + ";";
						messagets.putMessage(sql);
						data = messagefs.getMessage().toString();
			
						X = Integer.parseInt(data.substring(0,data.indexOf(",")));
						Y = Integer.parseInt(data.substring(data.indexOf(",") +1,data.length()));
						workorders = new Object[X][Y];
						for(int c = 0; c < X; c++)
						{
							for(int d = 0; d < Y; d++)
							{
								workorders[c][d] = messagefs.getMessage();
							}
						}
						workorders = refineDataForTable(workorders);
						
						displayTable(workorders);
						tabledisplayed = true;
					}
				break;
				case 4://work orders assigned to technicians
					sql = "019: SELECT * FROM workorders WHERE status=1 AND assigned_to='" + techbox.getSelectedItem() +"';";
					messagets.putMessage(sql);
					data = messagefs.getMessage().toString();
		
					X = Integer.parseInt(data.substring(0,data.indexOf(",")));
					Y = Integer.parseInt(data.substring(data.indexOf(",") +1,data.length()));
					workorders = new Object[X][Y];
					for(int c = 0; c < X; c++)
					{
						for(int d = 0; d < Y; d++)
						{
							workorders[c][d] = messagefs.getMessage();
						}
					}
					workorders = refineDataForTable(workorders);
					
					displayTable(workorders);
					tabledisplayed = true;
				break;
				case 5://Summary of closed work orders by campus
					sql = "019: SELECT * FROM workorders WHERE status=3;";
					messagets.putMessage(sql);
					data = messagefs.getMessage().toString();
		
					X = Integer.parseInt(data.substring(0,data.indexOf(",")));
					Y = Integer.parseInt(data.substring(data.indexOf(",") +1,data.length()));
					workorders = new Object[X][Y];
					for(int c = 0; c < X; c++)
					{
						for(int d = 0; d < Y; d++)
						{
							workorders[c][d] = messagefs.getMessage();
						}
					}
					workorders = refineDataForTable(workorders);
					
					displaySummary(workorders);
				break;
				case 6://Summary of closed work orders by technician
					sql = "019: SELECT * FROM workorders WHERE status=3;";
					messagets.putMessage(sql);
					data = messagefs.getMessage().toString();
		
					X = Integer.parseInt(data.substring(0,data.indexOf(",")));
					Y = Integer.parseInt(data.substring(data.indexOf(",") +1,data.length()));
					workorders = new Object[X][Y];
					for(int c = 0; c < X; c++)
					{
						for(int d = 0; d < Y; d++)
						{
							workorders[c][d] = messagefs.getMessage();
						}
					}
				//	workorders = refineDataForTable(workorders);
					
					displaySummary(workorders);
				break;
				case 7: //summary of open work orders by campus
					sql = "019: SELECT * FROM workorders WHERE status=1;";
					messagets.putMessage(sql);
					data = messagefs.getMessage().toString();
					System.out.println(data);
					X = Integer.parseInt(data.substring(0,data.indexOf(",")));
					Y = Integer.parseInt(data.substring(data.indexOf(",") +1,data.length()));
					workorders = new Object[X][Y];
					for(int c = 0; c < X; c++)
					{
						for(int d = 0; d < Y; d++)
						{
							workorders[c][d] = messagefs.getMessage();
						}
					}
					workorders = refineDataForTable(workorders);
					
					displaySummary(workorders);
				break;
				case 8://summary of open work orders assigned to technician
					sql = "019:SELECT * FROM workorders WHERE status=1";
					messagets.putMessage(sql);
					data = messagefs.getMessage().toString();
					System.out.println(data);
					X = Integer.parseInt(data.substring(0,data.indexOf(",")));
					Y = Integer.parseInt(data.substring(data.indexOf(",") +1,data.length()));
					workorders = new Object[X][Y];
					for(int c = 0; c < X; c++)
					{
						for(int d = 0; d < Y; d++)
						{
							workorders[c][d] = messagefs.getMessage();
						}
					}
					//workorders = refineDataForTable(workorders);
					
					displaySummary(workorders);
					
				break;
			}
			contentPane.validate();
		}
	}
	
	public void TTC()
	{
		if(reports.getSelectedIndex() == 8)
		{
			drawInterface();
			//one array index for every school.
			//we will keep these indexes corresponding to the campuses array.
			//i.e. Admin is index 0 in both arrays, AIM is index 1 etc......
			
			//go ahead and put the message into the object array so that it can be searched in a loop.
			
			JTextArea summaryarea = new JTextArea(30,40);
			summaryarea.setText("");
			summaryarea.setEditable(false);
			
			JScrollPane scrollpane = new JScrollPane(summaryarea);
			
			center_flow = new JPanel();
			center_flow.setLayout(new FlowLayout());
			center_flow.add(scrollpane);
			
			contentPane.add(center_flow,BorderLayout.CENTER);
			
			//----------------------------------------------------------------------------
			//needs to get a list of sites from the server.
			String message = "054:";
			messagets.putMessage(message);
			String data = messagefs.getMessage().toString();
			Object sites[] = data.split(",");
			//-----------------------------------------------------------------------------
			//gets the TTC for all the w/os for this site
			for(int c = 0; c < sites.length; c++) //one iteration for every site.
			{
				//---------------------------------------------------------------------------------------------
				//gets the TTC and puts them in workorders for site X
				message = "019:SELECT time_to_completion FROM workorders WHERE campus='" + sites[c] + "';";
				messagets.putMessage(message);
				data = messagefs.getMessage().toString();
				int X = Integer.parseInt(data.substring(0,data.indexOf(",")));
				int Y = Integer.parseInt(data.substring(data.indexOf(",") +1,data.length()));
				Object workorders[][] = new Object[X][Y];
				
				for(int f = 0; f < X; f++)
				{
					for(int d = 0; d < Y; d++)
					{
						workorders[f][d] = messagefs.getMessage();
						System.out.println(workorders[f][0]);
					}
				}
				//---------------------------------------------------------------------------------------------
				int number_non_nulls = 0;
				for(int f = 0; f < X; f++)
				{
					if(workorders[f][0] !=null)
						number_non_nulls++;
				}
				//System.out.println("Not null" + number_non_nulls);
				//define and initalize the array
				Completion completion[] = new Completion[number_non_nulls];
				for(int f = 0; f < number_non_nulls; f++)
				{
					completion[f] = new Completion();
				}
				//--------------------------------------------------------------------------------------------
				//sets all the values from the array.
				for(int f = 0; f < number_non_nulls; f++)
				{
					System.out.println("F" + f);
					if(workorders[f][0] !=null)
					{
						StringTokenizer token = new StringTokenizer(workorders[f][0].toString(),"-");
						completion[f].setYear(Integer.parseInt(token.nextToken()));
						completion[f].setMonth(Integer.parseInt(token.nextToken()));
						completion[f].setDay(Integer.parseInt(token.nextToken()));
						completion[f].setHour(Integer.parseInt(token.nextToken()));
						completion[f].setMinute(Integer.parseInt(token.nextToken()));
						completion[f].setSecond(Integer.parseInt(token.nextToken()));
					}
				}
				int year =completion[0].getYear();
				int month = completion[0].getMonth();
				int day = completion[0].getDay();
				int hour = completion[0].getHour();
				int minute = completion[0].getMinute();
				int second = completion[0].getSecond(); //holds the averages for this school.
				
				//total all the values from the array and store them above.
				for(int f = 1; f< number_non_nulls; f++)
				{
					year +=completion[f].getYear();
					month+=completion[f].getMonth();
					day+=completion[f].getDay();
					hour+=completion[f].getHour();
					minute+=completion[f].getMinute();
					second+=completion[f].getSecond();
					System.out.println(second);
				}
				
				year = year / number_non_nulls;
				month = month / number_non_nulls;
				day = day / number_non_nulls;
				hour = hour / number_non_nulls;
				minute = minute /  number_non_nulls;
				second = second / number_non_nulls;
				System.out.println(second);
				String average = year + "-" + month + "-" + day + "-" + hour + "-" + minute + "-" + second;
				summaryarea.append(sites[c] + "\t" + average + "\n");
			}
		}
		else if(reports.getSelectedIndex() == 9)
		{
		}
		else if(reports.getSelectedIndex() == 10)
		{
		}
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
	public void displayTable(Object workorders[][])
	{
		drawInterface();
		//----------------------------------------------------------------------
		//Column name stuff
		messagets.putMessage("001:"); //request the columnnames from the server
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
		//send a request to the server to send the work orders
		
		
		//sets up the pending table
		tm = new DefaultTableModel() {public boolean isCellEditable(int rowIndex, int mColIndex) {return false;}}; 
		tm.setDataVector(workorders,columnNames);
		
		table = new JTable(tm);
		table.addMouseListener(new WOHistoryMouseAdapter());
		//table.setAutoCreateRowSorter(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		scrollpane = new JScrollPane(table);
		
		//ask which way they want: with or without this panel
	//	flow_panel = new JPanel();
	//	flow_panel.setLayout(new FlowLayout());
	//	flow_panel.add(scrollpane);
		
		contentPane.add(scrollpane,BorderLayout.CENTER);
		contentPane.validate();
	}
	
	public void displaySummary(Object workorders[][])
	{
		System.out.println("Summary");
		drawInterface();
		//one array index for every school.
		//we will keep these indexes corresponding to the campuses array.
		//i.e. Admin is index 0 in both arrays, AIM is index 1 etc......
		
		//go ahead and put the message into the object array so that it can be searched in a loop.
		
		JTextArea summaryarea = new JTextArea(30,40);
		summaryarea.setText("");
		summaryarea.setEditable(false);
		
		JScrollPane scrollpane = new JScrollPane(summaryarea);
		
		center_flow = new JPanel();
		center_flow.setLayout(new FlowLayout());
		center_flow.add(scrollpane);
		
		contentPane.add(center_flow,BorderLayout.CENTER);
		
		
		//======================================================================
        
		switch(reports.getSelectedIndex())
		{
			case 7:
			case 5: //display the  campus summary
			
				messagets.putMessage("057:");
				String siteIDs[] = messagefs.getMessage().toString().split(",");
				
				int workordercount[][] = new int[siteIDs.length][2];
				
				for(int c = 0; c < workordercount.length; c++)
				{
					workordercount[c][0] = Integer.parseInt(siteIDs[c]);
					workordercount[c][1] = 0;
				}
				int counter = 0;
				System.out.println("length" + workorders.length);
				for(int c = 0; c < workorders.length; c++)//add the work orders
				{
					for(int d = 0; d < workordercount.length; d++)
					{
						if(workordercount[d][0] == Integer.parseInt(workorders[c][3] + ""))
						{
							workordercount[d][1]++;
							break;
						}
					}
				}
				for(int c = 0; c < siteIDs.length; c++)
				{	
					messagets.putMessage("055:"+workordercount[c][0]);
					String campus = messagefs.getMessage().toString();
					summaryarea.append(campus);
					
					if(campus.length() <= 14)
						summaryarea.append("\t\t");
					else
						summaryarea.append("\t");
						
					summaryarea.append(workordercount[c][1] + "\n");
				}
				
			break;
			case 6: //display the tech summary
				//get a list of techs
				messagets.putMessage("021:");
				String techs[] = messagefs.getMessage().toString().split(",");
				
				workordercount = new int[techs.length][2];
				
				for(int c = 0; c < workordercount.length; c++)
				{
					workordercount[c][0] = Integer.parseInt(techs[c]);
					workordercount[c][1] = 0;
				}
				counter = 0;
				for(int c = 0; c < workorders.length; c++)//add the work orders
				{
					for(int d = 0; d < workordercount.length; d++)
					{
						if((workorders[c][7] == "null") || (workorders[c][7] == null))
						{
							workorders[c][7] = -1;
						}
						if(workordercount[d][0] == Integer.parseInt(workorders[c][7] + ""))
						{
							workordercount[d][1]++;
							break;
						}
					}
				}
				for(int c = 0; c < techs.length; c++)
				{	
					messagets.putMessage("003:"+workordercount[c][0]);
					String techname = messagefs.getMessage().toString();
					summaryarea.append(techname);
					if(techname.length() <= 15)
						summaryarea.append("\t\t");
					else
						summaryarea.append("\t");
						
					summaryarea.append(workordercount[c][1] + "\n");
				}
			break;
			case 8:
				//get a list of techs
				messagets.putMessage("021:");
				techs = messagefs.getMessage().toString().split(",");
				
				workordercount = new int[techs.length][2];
				
				for(int c = 0; c < workordercount.length; c++)
				{
					workordercount[c][0] = Integer.parseInt(techs[c]);
					workordercount[c][1] = 0;
				}
				
				counter = 0;
				for(int c = 0; c < workorders.length; c++)//add the work orders
				{
					for(int d = 0; d < workordercount.length; d++)
					{
						if((workorders[c][17] == "null") || (workorders[c][17] == null))
						{
							workorders[c][17] = -1;
						}
						if(workordercount[d][0] == Integer.parseInt(workorders[c][17] + ""))
						{
							workordercount[d][1]++;
							break;
						}
					}
				}
				for(int c = 0; c < techs.length; c++)
				{	
					messagets.putMessage("003:"+workordercount[c][0]);
					String techname = messagefs.getMessage().toString();
					summaryarea.append(techname);
					if(techname.length() <= 15)
						summaryarea.append("\t\t");
					else
						summaryarea.append("\t");
						
					summaryarea.append(workordercount[c][1] + "\n");
				}
			break;
		}
		contentPane.validate();
	}
	
	public Object[][] refineDataForTable(Object object[][])
	{
		int X = object.length;
		int Y;
		try
        {
        	Y = object[0].length;
        }
        catch(Exception f)
        {
        	Y = 0;
        }
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
		//the data is made to look like this for tab panel 0
		//only keep the data that is going on the table
		for(int c = 0; c < X; c++)
		{
			int counter=0;
			//only do this IF it is not a summary.
			if((reports.getSelectedIndex() !=5) && (reports.getSelectedIndex() != 6) && (reports.getSelectedIndex() !=7))
			{
				for(int d = 0; d < Y; d++)
				{
					//this makes the campus name appear instead of the siteID
					if(d==5)
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
						case 0:
						case 1:
						case 3:
						case 5:
						case 9:
						case 13:
						case 17:
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
						break;
					}
				}
			}
			else
			{
				for(int d = 0; d < Y; d++)
				{
					switch(d)
					{
						case 0:
						case 1:
						case 3:
						case 5:
						case 9:
						case 13:
						case 17:
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
						break;
					}
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
			
			//appears this way for tab panel 0
			switch(namescounter)
			{
				case 0:
					currenttoken = " WO #";
					break;
				case 1:
					currenttoken = "ECISD #";
					break;
				case 3:
					currenttoken = "Problem";
					break;
				case 5:
					currenttoken = "Campus";
					break;
				case 9:
					currenttoken = "Date Open";
					break;
				case 13:
					currenttoken = "Room Number";
					break;
				case 17:
					currenttoken = "Assigned To";
					break;
				
			}
			switch(namescounter)
			{
				case 0:
					new_columnNames = new_columnNames + currenttoken;
					break;
				case 1:
				case 3:
				case 5:
				case 9:
				case 13:
				case 17:
					new_columnNames = new_columnNames + "," + currenttoken;
				break;
			}
		
			//------------------------------------------------------------------
			namescounter++;
		}
		return new_columnNames;
	}
	
	class WOHistoryMouseAdapter extends MouseAdapter
	{
		public void mouseClicked(MouseEvent e)
		{
            if (e.getClickCount() >= 2) 
            {
            	try
            	{
            		int workordernumberselected = Integer.parseInt(table.getValueAt(table.getSelectedRow(),0).toString());
               		WODetails object = new WODetails(messagets,messagefs,userlevel,workordernumberselected,4);
            	}
            	catch(Exception f)
            	{
            		JOptionPane.showMessageDialog(null,"Error: The program cannot read your selection. \n Please restart the application if the problem persists " );
            	}
            }
		}
	}
	
	class Completion
	{
		private int day;
		private int hour;
		private int minute;
		private int month;
		private int year;
		private int second;
		
		public Completion()
		{
			year = 0;
			month = 0;
			day = 0;
			hour = 0;
			minute = 0;
			second = 0;
		}
		public void setMonth(int a)
		{
			month = a;
		}
		public void setYear(int a)
		{
			year = a;
		}
		public void setDay(int a)
		{
			day = a;
		}
		public void setHour(int a)
		{
			hour = a;
		}
		public void setMinute(int a)
		{
			minute = a;
		}
		public void setSecond(int a)
		{
			second = a;
		}
		public int getYear()
		{
			return year;
		}
		public int getMonth()
		{
			return month;
		}
		public int getDay()
		{
			return day;
		}
		public int getHour()
		{
			return hour;
		}
		public int getMinute()
		{
			return minute;
		}
		public int getSecond()
		{
			return second;
		}
	}
	
}