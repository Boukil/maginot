import javax.swing.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.util.StringTokenizer;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.util.Arrays;


public class WODetails extends JFrame implements ActionListener
{
	Container contentPane;	
	JPanel panel5,south_panel5;
	JTextField fnamefield,lnamefield,wonum,roomnum,phonefield,dateemailsent,
				ecisdnum,model, openedbyfield,closedbyfield, dateopenedfield,dateclosedfield,servicetagfield;
				
	JTextArea problemdes, problemres;
	JTextArea problemadddes,problemaddres; //adds a resolution or descripition.
	String problemdesString, problemresString;

	JComboBox campusbox,assignedtobox,prioritybox;
	
	Color bgColor,txtColor;
	
	TransferObject messagefs,messagets;
	int userlevel;
	int workordernumber;
	int statusnum;
	JCheckBoxMenuItem cb;
	
	//the status num is the code that manipulates the GUI
	//0  - view open pending
	//1 - view closed pending
	//3 - view Details (when listed by campus) the tech should have the close button here
	//4 - WO history from the tab 5
	
	
	public WODetails(TransferObject messagets,TransferObject messagefs,int userlevel,int workordernumber,int statusnum)
	{
		this.statusnum = statusnum;
		this.workordernumber = workordernumber;
		this.messagets = messagets;
		this.messagefs = messagefs;
		this.userlevel = userlevel;
		
		getColors();
		
		setSize(750,760);
		setResizable(false);
		setTitle("Maginot Client - Work Order Pending Open");
		contentPane = getContentPane();
		contentPane.setBackground(bgColor);
        contentPane.setLayout(new BorderLayout( ));
        JFrame myWindow = new JFrame();
        myWindow.getContentPane().setLayout(new BorderLayout());
        addWindowListener(new WindowCloser(myWindow));
        
        
		//----------------------------------------------------------------------
        //panel5  new work order
        
        panel5 = new JPanel();
        panel5.setBackground(bgColor);
        panel5.setLayout(new BorderLayout());
        contentPane.add(panel5);
        
        JPanel north_panel5 = new JPanel();
        north_panel5.setBackground(bgColor);
        north_panel5.setLayout(new GridLayout(9,2));
        panel5.add(north_panel5,BorderLayout.NORTH);
        
        JPanel flows[] = new JPanel[18];
        
        for(int c = 0; c < 18; c++)
        {
        	flows[c] = new JPanel();
        	flows[c].setBackground(bgColor);
        	flows[c].setLayout(new FlowLayout());
        	north_panel5.add(flows[c]);
        }
        
        
        
        fnamefield = new JTextField(20);
        JLabel fnamelabel = new JLabel("First Name");
        fnamelabel.setForeground(txtColor);
        flows[0].add(fnamelabel);
        flows[0].add(fnamefield);
        
        lnamefield = new JTextField(20);
        JLabel lnamelabel = new JLabel("Last Name:      ");
        lnamelabel.setForeground(txtColor);
        flows[1].add(lnamelabel);
        flows[1].add(lnamefield);
        
        
        JLabel wonumberlabel = new JLabel("W/O Number:");
        wonumberlabel.setForeground(txtColor);
        wonum = new JTextField(5);
        wonum.setEditable(false);
        flows[2].add(wonumberlabel);
        flows[2].add(wonum);
        
        //get a list of sites from the server
        String message = "054:";
        messagets.putMessage(message);
        String campusoptions[] = messagefs.getMessage().toString().split(",");
        Arrays.sort(campusoptions);
        
        System.out.println(userlevel);
        
    	//get the priority level from the server
    	messagets.putMessage("130:" + workordernumber);
    	message = messagefs.getMessage().toString();
    	
    	String priorityoptions[] = {"1","2","3"};
        prioritybox = new JComboBox(priorityoptions);
        
        if(!message.equalsIgnoreCase("null"))
        	prioritybox.setSelectedIndex(Integer.parseInt(message)-1);	
        else
        	prioritybox.setSelectedIndex(-1);
        	
        JLabel priorityboxlabel = new JLabel("Priority:    ");
        priorityboxlabel.setForeground(txtColor);
        flows[2].add(priorityboxlabel);
        flows[2].add(prioritybox);
        
        if(userlevel > 0)
        {
        	prioritybox.setEnabled(false);
        }
        
        
        roomnum = new JTextField(20);
        JLabel roomnumlabel = new JLabel("Room Number:");
        roomnumlabel.setForeground(txtColor);
        flows[3].add(roomnumlabel);
        flows[3].add(roomnum);
        
        campusbox = new JComboBox(campusoptions);
        JLabel campusboxlabel = new JLabel("Campus:");
        campusboxlabel.setForeground(txtColor);
        flows[4].add(campusboxlabel);
        flows[4].add(campusbox);
        
        phonefield = new JTextField(20);
        JLabel phonelabel = new JLabel("Phone Number:");
        phonelabel.setForeground(txtColor);
        flows[5].add(phonelabel);
        flows[5].add(phonefield);
        
        ecisdnum = new JTextField(20);
        JLabel ecisdlabel = new JLabel("ECISD number");
        ecisdlabel.setForeground(txtColor);
        flows[6].add(ecisdlabel);
        flows[6].add(ecisdnum);
        
        model = new JTextField(20); 
        JLabel modellabel = new JLabel("Model:                ");
        modellabel.setForeground(txtColor);
        flows[7].add(modellabel);
        flows[7].add(model);
        
        openedbyfield = new JTextField(20);
        openedbyfield.setEditable(false);
        JLabel openedbylabel = new JLabel("Opened by:");
        openedbylabel.setForeground(txtColor);
        flows[8].add(openedbylabel);
        flows[8].add(openedbyfield);
        
        closedbyfield = new JTextField(20);
        closedbyfield.setEditable(false);
        JLabel closedbylabel = new JLabel("Closed by:");
        closedbylabel.setForeground(txtColor);
        flows[9].add(closedbylabel);
        flows[9].add(closedbyfield);
        
        dateopenedfield = new JTextField(20);
        dateopenedfield.setEditable(false);
        JLabel dateopenedlabel = new JLabel("Date Opened:");
        dateopenedlabel.setForeground(txtColor);
        flows[10].add(dateopenedlabel);
        flows[10].add(dateopenedfield);
        
        dateclosedfield = new JTextField(20);
        dateclosedfield.setEditable(false);
        JLabel dateclosedlabel = new JLabel("Date Closed:");
        dateclosedlabel.setForeground(txtColor);
        flows[11].add(dateclosedlabel);
        flows[11].add(dateclosedfield);
        
        servicetagfield = new JTextField(20);
        JLabel servicetaglabel = new JLabel("Service Tag:");
        servicetaglabel.setForeground(txtColor);
        flows[12].add(servicetaglabel);
        flows[12].add(servicetagfield);
        
        JTextField ttcfield = new JTextField(20);
        ttcfield.setEditable(false);
        JLabel ttclabel = new JLabel("Time to Completion");
        ttclabel.setForeground(txtColor);
        flows[13].add(ttclabel);
        flows[13].add(ttcfield);
        //gets the TTC only if the status is pending closed or closed.
        if((statusnum == 1) || (statusnum == 4))
        {
        	message = "110:" + workordernumber;
        	messagets.putMessage(message);
        	message = messagefs.getMessage().toString();
        	if(!message.equalsIgnoreCase("null"))
       			ttcfield.setText(message);
        }
        //get a list of techs from the server
        message = "025:";
        messagets.putMessage(message);
        String assignedtooptions[] = messagefs.getMessage().toString().split(",");
        Arrays.sort(assignedtooptions);
        
        assignedtobox = new JComboBox(assignedtooptions);

        JLabel assignedtolabel = new JLabel("Assigned To:");
        assignedtolabel.setForeground(txtColor);
        
        flows[14].add(assignedtolabel);
        flows[14].add(assignedtobox);
        
        JPanel center_panel5 = new JPanel();
        center_panel5.setBackground(bgColor);
        center_panel5.setLayout(new BorderLayout());
        panel5.add(center_panel5,BorderLayout.CENTER);
        
        
        //---------------------------------------------------------
        //the panels to set up the west JTextArea properly
        
        JPanel west_center_panel5 = new JPanel();
        west_center_panel5.setBackground(bgColor);
        west_center_panel5.setLayout(new BorderLayout());
        panel5.add(west_center_panel5,BorderLayout.WEST);
        
        JPanel flowdescriptionlabel = new JPanel();
        flowdescriptionlabel.setBackground(bgColor);
        flowdescriptionlabel.setLayout(new FlowLayout());
        west_center_panel5.add(flowdescriptionlabel,BorderLayout.NORTH);
        
        JLabel problemdeslabel = new JLabel("Problem description:");
        problemdeslabel.setForeground(txtColor);
        flowdescriptionlabel.add(problemdeslabel);
        
        JPanel flowproblemdes = new JPanel();
        flowproblemdes.setBackground(bgColor);
        flowproblemdes.setLayout(new GridLayout(2,1));
        west_center_panel5.add(flowproblemdes,BorderLayout.CENTER);
        
        JLabel blanklabel = new JLabel("                  ");
        blanklabel.setForeground(txtColor);
        west_center_panel5.add(blanklabel,BorderLayout.WEST);
        
        problemdes = new JTextArea(10,25);
        problemdes.setEditable(false);
        problemdes.setLineWrap(true);
        problemdes.setWrapStyleWord(true);
        JScrollPane scrolledtext = new JScrollPane(problemdes);
        flowproblemdes.add(scrolledtext);
        
        problemadddes = new JTextArea(10,25);
        problemadddes.setLineWrap(true);
        problemadddes.setWrapStyleWord(true);
        JScrollPane scrolledtext4 = new JScrollPane(problemadddes);
        flowproblemdes.add(scrolledtext4);
        
        
        //--------------------------------------------------------------------
        //same as above, but the the JTextArea east
        JPanel east_center_panel5 = new JPanel();
        east_center_panel5.setBackground(bgColor);
        east_center_panel5.setLayout(new BorderLayout());
        panel5.add(east_center_panel5,BorderLayout.EAST);
        
        JPanel flowresolutionlabel = new JPanel();
        flowresolutionlabel.setBackground(bgColor);
        flowresolutionlabel.setLayout(new FlowLayout());
        east_center_panel5.add(flowresolutionlabel,BorderLayout.NORTH);
        
        JLabel resolutionlabel = new JLabel("Problem resolution:                  ");
        resolutionlabel.setForeground(txtColor);
        flowresolutionlabel.add(resolutionlabel);
        
        JPanel flowproblemres = new JPanel();
        flowproblemres.setBackground(bgColor);
        flowproblemres.setLayout(new GridLayout(2,1));
        east_center_panel5.add(flowproblemres,BorderLayout.CENTER);
        
        JLabel blanklabel2 = new JLabel("                     ");
        blanklabel2.setForeground(txtColor);
        east_center_panel5.add(blanklabel2,BorderLayout.EAST);
        
        problemres = new JTextArea(10,25);
        problemres.setEditable(false);
        problemres.setLineWrap(true);
        problemres.setWrapStyleWord(true);
        JScrollPane scrolledtext2 = new JScrollPane(problemres);
        flowproblemres.add(scrolledtext2);
        
        problemaddres = new JTextArea(10,25);
        problemaddres.setLineWrap(true);
        problemaddres.setWrapStyleWord(true);
        JScrollPane scrolledtext3 = new JScrollPane(problemaddres);
        flowproblemres.add(scrolledtext3);
        
        
        
        south_panel5 = new JPanel();
        south_panel5.setBackground(bgColor);
        south_panel5.setLayout(new BorderLayout());
        panel5.add(south_panel5,BorderLayout.SOUTH);
        
        JPanel button_south_panel5 = new JPanel();
        button_south_panel5.setBackground(bgColor);
        button_south_panel5.setLayout(new FlowLayout());
        south_panel5.add(button_south_panel5,BorderLayout.NORTH);
        
        
        
        //the only thing that varies between the pending open and the pending close
        //is what buttons are on the GUI, and what they do.
        
        if(statusnum == 0) //pending open
        {
        	JButton markopenbutton = new JButton("Open for Technicians");
        	markopenbutton.setForeground(txtColor);
        	button_south_panel5.add(markopenbutton);
        	markopenbutton.addActionListener(this);
        	
        	JButton closebutton = new JButton("Close");
        	closebutton.setForeground(txtColor);
	    	closebutton.addActionListener(this);
	    	button_south_panel5.add(closebutton);
        }
        else if(statusnum == 1) // pending close
        {
        	JButton openWObuttontech = new JButton("Open for Technicians");
        	openWObuttontech.setForeground(txtColor);
	        button_south_panel5.add(openWObuttontech);
	        openWObuttontech.addActionListener(this);
	        
	        JButton savebutton = new JButton("Save");
	    	savebutton.setForeground(txtColor);
	    	savebutton.addActionListener(this);
	    	button_south_panel5.add(savebutton);
	        
	    	JButton closebutton = new JButton("Close");
	    	closebutton.setForeground(txtColor);
	    	closebutton.addActionListener(this);
	    	button_south_panel5.add(closebutton);
	    	//adds the checkbox and the date field that the email was sent.
	    	
	    	cb = new JCheckBoxMenuItem("Email Sent");
	    	cb.setBackground(bgColor);
	    	cb.setForeground(txtColor);
	    	cb.addActionListener(this);
	    	flows[16].add(cb);
	    	
	    	messagets.putMessage("094:" +workordernumber);
	    	String data = messagefs.getMessage().toString();
	    	
	    	if(data.equalsIgnoreCase("1"))
	    	{
	    		cb.setState(true);
	    	}
	    	JLabel emailsendfieldlabel = new JLabel("Date Email Sent");
	    	emailsendfieldlabel.setForeground(txtColor);
	    	dateemailsent = new JTextField(20);
	    	dateemailsent.setEditable(false);
	    	flows[17].add(emailsendfieldlabel);
	    	flows[17].add(dateemailsent);
	    	
	    	if(cb.getState() == true)
	    	{
	    		messagets.putMessage("091:" + workordernumber);
	    		data = messagefs.getMessage().toString();
	    		dateemailsent.setText(data);
	    	}
        }
        else if(statusnum == 3) // work order is open on the Technician's screen
        {
        	JButton closebutton = new JButton("Close");
        	closebutton.setForeground(txtColor);
	    	closebutton.addActionListener(this);
	    	button_south_panel5.add(closebutton);
	    	
	    	JButton savebutton = new JButton("Save");
	    	savebutton.setForeground(txtColor);
	    	savebutton.addActionListener(this);
	    	button_south_panel5.add(savebutton);
        }
		else if(statusnum == 4)
		{
			//set them all editable to false;
			fnamefield.setEditable(false);
			lnamefield.setEditable(false);
			wonum.setEditable(false);
			roomnum.setEditable(false);
			phonefield.setEditable(false);
			ecisdnum.setEditable(false);
			model.setEditable(false);
			openedbyfield.setEditable(false);
			closedbyfield.setEditable(false);
			dateopenedfield.setEditable(false);
			dateclosedfield.setEditable(false);
			servicetagfield.setEditable(false);      
			problemdes.setEditable(false);
			problemres.setEditable(false);
		}       
        // populates the fields with info from the server.
        
        message = "011:" + workordernumber;
        messagets.putMessage(message);
        String data = messagefs.getMessage().toString();
        System.out.println(data);
        StringTokenizer tokenizer = new StringTokenizer(data,",");
        
        
        wonum.setText(tokenizer.nextToken());
       	 	if(wonum.getText().equals("null"))
       	 		wonum.setText("");
       	 	else
       	 		setTitle("Maginot Client - WO# " + wonum.getText());
       	 		
        ecisdnum.setText(tokenizer.nextToken());
	        if(ecisdnum.getText().equals("-1"))
	        	ecisdnum.setText("");
	        else if(ecisdnum.getText().equals("null"))
	        	ecisdnum.setText("");
        String junk = tokenizer.nextToken(); //status number
        
        problemdes.setText(tokenizer.nextToken());
	        if(problemdes.getText().equals("null"))
	        	problemdes.setText("");
        problemres.setText(tokenizer.nextToken());
	        if(problemres.getText().equals("null"))
	        	problemres.setText("");
	    
	    //campus
	    String campus = tokenizer.nextToken();
	    messagets.putMessage("055:" + campus);
	    
	    campusbox.setSelectedItem(messagefs.getMessage());
	    
        openedbyfield.setText(tokenizer.nextToken());
	       
        closedbyfield.setText(tokenizer.nextToken());
	        
        model.setText(tokenizer.nextToken());
	        if(model.getText().equals("null"))
	        	model.setText("");
        dateopenedfield.setText(tokenizer.nextToken());
	        if(dateopenedfield.getText().equals("null"))
	        	dateopenedfield.setText("");
        dateclosedfield.setText(tokenizer.nextToken());
	        if(dateclosedfield.getText().equals("null"))
	        	dateclosedfield.setText("");
        fnamefield.setText(tokenizer.nextToken());
	        if(fnamefield.getText().equals("null"))
	        	fnamefield.setText("");
        lnamefield.setText(tokenizer.nextToken());
	        if(lnamefield.getText().equals("null"))
	        	lnamefield.setText("");
        roomnum.setText(tokenizer.nextToken());
	        if(roomnum.getText().equals("null"))
	        	roomnum.setText("");
        phonefield.setText(tokenizer.nextToken());
	        if(phonefield.getText().equals("null"))
	        	phonefield.setText("");
	        	
	    String priority = tokenizer.nextToken();
	    System.out.println(":" + priority +":");
	    if(!priority.equals("null"))
			prioritybox.setSelectedIndex(Integer.parseInt(priority)-1);	
				       
	    servicetagfield.setText(tokenizer.nextToken());
	    
        if(servicetagfield.getText().equals("null"))
        	servicetagfield.setText("");
	    
	    
	    messagets.putMessage("003:" + tokenizer.nextToken());
	    
	    
	    assignedtobox.setSelectedItem(messagefs.getMessage().toString());
	        	
	     //this translates the opened by and closed by from the USER_IDs
	     //to the usernames 
	    if(closedbyfield.getText().equals("-1"))
	    {
	    	closedbyfield.setText("");
	    }
	    else
	    {
	    	message = "003:" + closedbyfield.getText();
	    	messagets.putMessage(message);
	    	String temp_user = messagefs.getMessage().toString();
	    	closedbyfield.setText(temp_user);
	    } 
	    	
	    if(openedbyfield.getText().equals("null"))
	    {
	    	openedbyfield.setText("");
	    }
	    else
	    {
	    	message = "003:" + openedbyfield.getText();
	    	messagets.putMessage(message);
	    	String temp_user = messagefs.getMessage().toString();
	    	openedbyfield.setText(temp_user);
	    } 	
	    
	    JMenu file = new JMenu("File");
	    
	    
		
		JMenuItem print = new JMenuItem("Print");
		print.addActionListener(this);
		file.add(print);
		
		// creates the menu bar
		JMenuBar mBar = new JMenuBar(); 
		mBar.add(file);
		setJMenuBar(mBar);

	    	
	    	
		this.setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e)
	{
		String actionCommand = e.getActionCommand( );
		//------------------------------------------------
		//this section sets up the problem descripition or resolution string.
		messagets.putMessage("060:");
		String data = messagefs.getMessage().toString();
		
		problemdesString = problemdes.getText() + problemadddes.getText();
		
		problemresString = problemres.getText() + problemaddres.getText();
		
		if(problemdesString.length() < 1)
			problemdesString = "null";
		else if(problemadddes.getText().length() > 1)
			problemdesString = problemdesString + "--" + data + "\n\n";
			
		if(problemresString.length() < 1)
			problemresString = "null";
		else if(problemaddres.getText().length() > 1)
			problemresString = problemresString  + "--" + data + "\n\n";
		
		//----------------------------------------------------------
		
		if(cb == e.getSource()) //cb is the check box
		{
			if(cb.getState() == true)
			{
				messagets.putMessage("092:" + workordernumber);
				messagets.putMessage("091:" + workordernumber);
				dateemailsent.setText(messagefs.getMessage().toString());
			}
			else
			{
				messagets.putMessage("093:" + workordernumber);
				dateemailsent.setText("");
			}
		}
		if(actionCommand.equals("Print"))
		{
			print object = new print();
		}
		
		if(actionCommand.equals("Open for Technicians"))
		{
			switch(statusnum)
			{
				case 0:
					openForTechniciansPendingOpen();
				break;
				case 1:
					openForTechniciansPendingClosed();
					
				break;
			}
		}
		if(actionCommand.equals("Close"))
		{
			switch(statusnum)
			{
				case 0:
				case 1:
					closeToPermanently();
				break;
				case 3:
					closeToPending();
				break;
			}
		}
		
		if(actionCommand.equals("Save"))
		{
			save();
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
	
	public void save() //save the updates on the GUI
	{
		boolean goon = true;
			
		try
		{
			int number = Integer.parseInt(ecisdnum.getText().trim());
		}
		catch(Exception f)
		{
			if(!ecisdnum.getText().equals(""))
			{
				goon = false;
				JOptionPane.showMessageDialog(null,"Invalid ECISD number. You can only enter numbers here. \n If you do not know the number, leave this field blank");
			}
		}
		if(goon)
		{
			//the following do not have to have values, but to avoid
			//making the server crash, insert a string "null";
			if(model.getText().equals(""))
			{
				model.setText("null");
			}
			if(ecisdnum.getText().equals(""))
			{
				ecisdnum.setText("null");
			}
			if(problemres.getText().equals(""))
			{
				problemres.setText("null");
			}
			if(phonefield.getText().trim().equals(""))
			{
				phonefield.setText("null");
			}
			if(servicetagfield.getText().trim().equals(""))
			{
				servicetagfield.setText("null");
			}
			if(roomnum.getText().trim().equals(""))
			{
				roomnum.setText("null");
			}
			
			//translate the Selected Index of the campus box to an actual index in the database. 
				String message = "056:" + campusbox.getSelectedItem();
				messagets.putMessage(message);
				String campus_index = messagefs.getMessage().toString();
				
			//translate the assigned to box to an index in the database.
			message = "046:" + assignedtobox.getSelectedItem();
			messagets.putMessage(message);
			String assignedtoindex = messagefs.getMessage().toString();
					
			message = "045:" + wonum.getText() + "," + 
				removeCharacters(ecisdnum.getText().trim()) + "," +				removeCharacters(problemdesString) 	+ "," +
				removeCharacters(problemresString) + "," +						campus_index		+ "," +
				removeCharacters(model.getText()) + "," +						removeCharacters(fnamefield.getText())	+ "," +
				removeCharacters(lnamefield.getText()) + "," +					removeCharacters(roomnum.getText())  	+ "," +
				removeCharacters(phonefield.getText()) + "," +					prioritybox.getSelectedItem() 	+ "," +
				removeCharacters(servicetagfield.getText().trim()) + "," +		assignedtoindex;
			messagets.putMessage(message);
			if(model.getText().equals("null"))
			{
				model.setText("");
			}
			if(problemres.getText().equals("null"))
			{
				problemres.setText("");
			}
			if(servicetagfield.getText().trim().equals("null"))
			{
				servicetagfield.setText("");
			}
			else if(ecisdnum.getText().trim().equals("null"))
			{
				ecisdnum.setText("");
			}
			if(roomnum.getText().equals("null"))
			{
				roomnum.setText("");
			}
			this.dispose();
		}
	}
	
	public void openForTechniciansPendingOpen()
	{
			boolean goon = true;
			//make sure there is an entry in all the fields. 
			if(fnamefield.getText().equals(""))		{goon = false;}
			else if(lnamefield.getText().equals("")) 	{goon = false;}
			else if(roomnum.getText().equals(""))		{goon = false;}
			else if(phonefield.getText().equals("")) 	{goon = false;}
			else if(problemdes.getText().equals("")){goon = false;}
			
			if((servicetagfield.getText().trim().equals("")) && (ecisdnum.getText().trim().equals("")))
			{
				goon = false;
			}
			
			
			if((!ecisdnum.getText().trim().equals("")) && (goon))
			{
				//make sure the ecisd number can be parsed so we don't crash the server
				try
				{
					int number = Integer.parseInt(ecisdnum.getText().trim());
				}
				catch(Exception f)
				{
					goon = false;
				}	
			}
			else
			{
				ecisdnum.setText("-1");
			}
			if(goon)
			{
				//the following do not have to have values, but to avoid
				//making the server crash, insert a string "null";
				if(model.getText().equals(""))
				{
					model.setText("null");
				}
				if(problemres.getText().equals(""))
				{
					problemres.setText("null");
				}
				if(servicetagfield.getText().trim().equals(""))
				{
					servicetagfield.setText("null");
				}
				else if(ecisdnum.getText().trim().equals(""))
				{
					ecisdnum.setText("-1");
				}
			
				//translate the Selected Index of the campus box to an actual index in the database. 
				String message = "056:" + campusbox.getSelectedItem();
				messagets.putMessage(message);
				String campus_index = messagefs.getMessage().toString();
				
				//translate the assigned to box to an index in the database.
				message = "046:" + assignedtobox.getSelectedItem();
				messagets.putMessage(message);
				String assignedtoindex = messagefs.getMessage().toString();
			
					//get the data from the fields and put it in a request, send it, and reset the fields. 
					message = "009:" + removeCharacters(fnamefield.getText()) + ","+ removeCharacters(lnamefield.getText()) 
													+ "," + campus_index + ","+ removeCharacters(roomnum.getText()) 
													+ "," + prioritybox.getSelectedItem() + ","+ removeCharacters(phonefield.getText()) 
													+ "," + removeCharacters(ecisdnum.getText().trim()) + ","+ removeCharacters(model.getText()) 
													+ "," + removeCharacters(problemdesString) + ","+removeCharacters(problemresString) + ","+wonum.getText()
													+ "," + removeCharacters(servicetagfield.getText().trim()) + "," + assignedtoindex
													+ "," + removeCharacters(dateopenedfield.getText());
				
				//resets the text fields. 
				fnamefield.setText("");
				lnamefield.setText("");
				roomnum.setText("");
				phonefield.setText("");
				ecisdnum.setText("");
				model.setText("");
				problemdes.setText("");
				problemres.setText("");
				servicetagfield.setText("");
				
				messagets.putMessage(message);
				this.dispose();
			}
			else
			{
				ecisdnum.setText("");
				JOptionPane.showMessageDialog(null,"One of your required fields does not have any data \n or the data is invalid.\n Please completely fill in this form with valid data to continue.");
			}
	} //open the work order for the techs that currently has a pending open status
	public void openForTechniciansPendingClosed() //this is for a work order that has been close, but will be reopened.
	{
		if(wonum.getText().equals("")) // if the work order doesn't alredy have a number
		{
			boolean goon = true;
			//make sure there is an entry in all the fields. 
			if(fnamefield.getText().equals(""))		{goon = false;}
			else if(lnamefield.getText().equals("")) 	{goon = false;}
			else if(roomnum.getText().equals(""))		{goon = false;}
			else if(phonefield.getText().equals("")) 	{goon = false;}
			else if(problemdes.getText().equals("")){goon = false;}
			
			//make sure the ecisd number can be parsed so we don't crash the server
			try
			{
				int number = Integer.parseInt(ecisdnum.getText().trim());
			}
			catch(Exception f)
			{
				ecisdnum.setText("");
				if(servicetagfield.getText().trim().equals(""))
				{
					goon = false;
				}
			}
			
			if((ecisdnum.getText().trim().equals("")) && (servicetagfield.getText().trim().equals("")))
			{
				goon = false; //new need either a servicetag or an ecisd number
			}
			
			
			
			if(goon)
			{
				//the following do not have to have values, but to avoid
				//making the server crash, insert a string "null";
				if(model.getText().equals(""))
				{
					model.setText("null");
				}
				if(problemres.getText().equals(""))
				{
					problemres.setText("null");
				}
				if(servicetagfield.getText().trim().equals(""))
				{
					servicetagfield.setText("null");
				}
				else if(ecisdnum.getText().trim().equals(""))
				{
					ecisdnum.setText("-1");
				
				//translate the Selected Index of the campus box to an actual index in the database. 
				String message = "056:" + campusbox.getSelectedItem();
				messagets.putMessage(message);
				String campus_index = messagefs.getMessage().toString();
				
				//translate the assigned to box to an index in the database.
				message = "046:" + assignedtobox.getSelectedItem();
				messagets.putMessage(message);
				String assignedtoindex = messagefs.getMessage().toString();
			
				//get the data from the fields and put it in a request, send it, and reset the fields. 
				message = "006:" +						
							removeCharacters(ecisdnum.getText().trim()) + "," +			removeCharacters(problemdesString) 	+ "," +
							removeCharacters(problemresString) + "," +					campus_index		+ "," +
							removeCharacters(model.getText()) + "," +					removeCharacters(fnamefield.getText())	+ "," +
							removeCharacters(lnamefield.getText()) + "," +				removeCharacters(roomnum.getText())  	+ "," +
							removeCharacters(phonefield.getText()) + "," +				prioritybox.getSelectedItem() 	+ "," +
							removeCharacters(servicetagfield.getText().trim()) + "," +		assignedtoindex;
				
				//resets the text fields. 
				fnamefield.setText("");
				lnamefield.setText("");
				roomnum.setText("");
				phonefield.setText("");
				ecisdnum.setText("");
				model.setText("");
				problemdes.setText("");
				problemres.setText("");
				
				messagets.putMessage(message);
				this.dispose();
			}
			else
			{
				JOptionPane.showMessageDialog(null,"One of your required fields does not have any data \n or the data is invalid.\n Please completely fill in this form with valid data to continue.");
			}
			}
		}
		//if the work order already has a number
		if(!wonum.getText().equals("")) // if the work order alredy has a number
		{
			boolean goon = true;
			//make sure there is an entry in all the fields. 
			if(fnamefield.getText().equals(""))		{goon = false;}
			else if(lnamefield.getText().equals("")) 	{goon = false;}
			else if(roomnum.getText().equals(""))		{goon = false;}
			else if(phonefield.getText().equals("")) 	{goon = false;}
			else if(ecisdnum.getText().equals("")) 	{goon = false;}
			else if(problemdes.getText().equals("")){goon = false;}
			
			//make sure the ecisd number can be parsed so we don't crash the server
			try
			{
				int number = Integer.parseInt(ecisdnum.getText().trim());
			}
			catch(Exception f)
			{
				goon = false;
			}
			if((ecisdnum.getText().trim().equals("")) && (servicetagfield.getText().trim().equals("")))
			{
				goon = false; //new need either a servicetag or an ecisd number
			}
			
			if(goon)
			{
				//the following do not have to have values, but to avoid
				//making the server crash, insert a string "null";
				if(model.getText().equals(""))
				{
					model.setText("null");
				}
				if(problemres.getText().equals(""))
				{
					problemres.setText("null");
				}
				if(servicetagfield.getText().trim().equals(""))
				{
					servicetagfield.setText("null");
				}
				else if(ecisdnum.getText().trim().equals(""))
				{
					ecisdnum.setText("-1");
				}
				
				//translate the Selected Index of the campus box to an actual index in the database. 
				String message = "056:" + campusbox.getSelectedItem();
				messagets.putMessage(message);
				String campus_index = messagefs.getMessage().toString();
				
				//translate the assigned to box to an index in the database.
				message = "046:" + assignedtobox.getSelectedItem();
				messagets.putMessage(message);
				String assignedtoindex = messagefs.getMessage().toString();
			
					//get the data from the fields and put it in a request, send it, and reset the fields. 
					message = "009:" + removeCharacters(fnamefield.getText()) + ","+ removeCharacters(lnamefield.getText()) 
													+ ","+ campus_index + ","+ removeCharacters(roomnum.getText()) 
													+ ","+ prioritybox.getSelectedItem() + ","+ removeCharacters(phonefield.getText()) 
													+ ","+ removeCharacters(ecisdnum.getText().trim()) + ","+ removeCharacters(model.getText()) 
													+ ","+removeCharacters(problemdesString) + ","+removeCharacters(problemresString) + ","+wonum.getText()
													+ "," + removeCharacters(servicetagfield.getText().trim()) + "," + assignedtoindex
													+ "," + removeCharacters(dateopenedfield.getText());
				
				//resets the text fields. 
				fnamefield.setText("");
				lnamefield.setText("");
				roomnum.setText("");
				phonefield.setText("");
				ecisdnum.setText("");
				model.setText("");
				problemdes.setText("");
				problemres.setText("");
				servicetagfield.setText("");
				
				messagets.putMessage(message);
				this.dispose();
			}
			else
			{
				JOptionPane.showMessageDialog(null,"One of your required fields does not have any data \n or the data is invalid.\n Please completely fill in this form with valid data to continue.");
			}
		}
		
	}
	
	public void closeToPermanently()
	{
			boolean goon = true;
			//make sure there is an entry in all the fields. 
			if(fnamefield.getText().equals(""))		{goon = false;}
			else if(lnamefield.getText().equals("")) 	{goon = false;}
			else if(problemdes.getText().equals("")){goon = false;}
			else if(problemres.getText().equals("")) {goon = false;}
			
			//make sure the ecisd number can be parsed so we don't crash the server
			try
			{
				int number = Integer.parseInt(ecisdnum.getText().trim());
			}
			catch(Exception f)
			{
				ecisdnum.setText("");
			}
			
			if(goon)
			{
				//the following do not have to have values, but to avoid
				//making the server crash, insert a string "null";
				if(model.getText().equals(""))		{model.setText("null");}
				if(problemres.getText().equals(""))	{problemres.setText("null");}
				if(roomnum.getText().equals(""))	{roomnum.setText("null");}
				if(phonefield.getText().equals("")) {phonefield.setText("null");}
				
				if(servicetagfield.getText().trim().equals("")){servicetagfield.setText("null");}
				if(ecisdnum.getText().trim().equals("")){ecisdnum.setText("-1");}
					
				//translate the Selected Index of the campus box to an actual index in the database. 
				String message = "056:" + campusbox.getSelectedItem();
				messagets.putMessage(message);
				String campus_index = messagefs.getMessage().toString();
				
				//translate the assigned to box to an index in the database.
				message = "046:" + assignedtobox.getSelectedItem();
				messagets.putMessage(message);
				String assignedtoindex = messagefs.getMessage().toString();
			
				//get the data from the fields and put it in a request, send it, and reset the fields. 
				message = "013:" + removeCharacters(fnamefield.getText()) + ","+ removeCharacters(lnamefield.getText()) 
												+ ","+ campus_index + ","+ removeCharacters(roomnum.getText()) 
												+ ","+ prioritybox.getSelectedItem() + ","+ removeCharacters(phonefield.getText()) 
												+ ","+ removeCharacters(ecisdnum.getText().trim()) + ","+ removeCharacters(model.getText()) 
												+ ","+ removeCharacters(problemdesString) + ","+removeCharacters(problemresString) + ","+wonum.getText()
												+ ","+ removeCharacters(servicetagfield.getText().trim()) + "," + assignedtoindex;
				
				//resets the text fields. 
				fnamefield.setText("");
				lnamefield.setText("");
				roomnum.setText("");
				phonefield.setText("");
				ecisdnum.setText("");
				model.setText("");
				problemdes.setText("");
				problemres.setText("");
				servicetagfield.setText("");
				
				messagets.putMessage(message);
				this.dispose();
			}
			else
			{
				JOptionPane.showMessageDialog(null,"One of your required fields does not have any data \n or the data is invalid.\n Please completely fill in this form with valid data to continue.");
			}
	}
	public void closeToPending()
	{
		boolean goon = true;
		//make sure there is an entry in all the fields. 
		if(fnamefield.getText().equals(""))		{goon = false;}
		else if(lnamefield.getText().equals("")) 	{goon = false;}
		else if(problemdes.getText().equals("") && (problemadddes.getText().equals(""))){goon = false;}
		else if(problemres.getText().equals("") && (problemaddres.getText().equals(""))) {goon = false;}
		
		//make sure the ecisd number can be parsed so we don't crash the server
		try
		{
			int number = Integer.parseInt(ecisdnum.getText().trim());
		}
		catch(Exception f)
		{
			ecisdnum.setText("");
		}
	
		
		if(goon)
		{
			//the following do not have to have values, but to avoid
			//making the server crash, insert a string "null";
			if(model.getText().equals(""))		{model.setText("null");}
			if(problemres.getText().equals(""))	{problemres.setText("null");}
			if(roomnum.getText().equals(""))	{roomnum.setText("null");}
			if(phonefield.getText().equals("")) {phonefield.setText("null");}
			
			if(servicetagfield.getText().trim().equals("")){servicetagfield.setText("null");}
			if(ecisdnum.getText().trim().equals("")){ecisdnum.setText("-1");}
			
			//translate the Selected Index of the campus box to an actual index in the database. 
			String message = "056:" + campusbox.getSelectedItem();
			messagets.putMessage(message);
			String campus_index = messagefs.getMessage().toString();
			
			//translate the assigned to box to an index in the database.
			message = "046:" + assignedtobox.getSelectedItem();
			messagets.putMessage(message);
			String assignedtoindex = messagefs.getMessage().toString();
		
			//get the data from the fields and put it in a request, send it, and reset the fields. 
			message = "008:" + removeCharacters(fnamefield.getText()) + ","+ removeCharacters(lnamefield.getText()) 
											+ ","+ campus_index + ","+ removeCharacters(roomnum.getText()) 
											+ ","+ prioritybox.getSelectedItem() + ","+ removeCharacters(phonefield.getText()) 
											+ ","+ removeCharacters(ecisdnum.getText().trim()) + ","+ removeCharacters(model.getText()) 
											+ ","+ removeCharacters(problemdesString) + ","+removeCharacters(problemresString) + ","+wonum.getText()
											+ "," + removeCharacters(servicetagfield.getText()) + "," + assignedtoindex;
			int wonumint = Integer.parseInt(wonum.getText());
			
			//resets the text fields. 
			fnamefield.setText("");
			lnamefield.setText("");
			roomnum.setText("");
			phonefield.setText("");
			ecisdnum.setText("");
			model.setText("");
			problemdes.setText("");
			problemres.setText("");
			servicetagfield.setText("");
			
			messagets.putMessage(message);
			
			//this part calculates the TTC
			//and sets it up in the database.
			
			message = "111:"+wonumint;
			messagets.putMessage(message);
			this.dispose();
		}
		else
		{
			JOptionPane.showMessageDialog(null,"One of your required fields does not have any data \n or the data is invalid.\n Please completely fill in this form with valid data to continue.");
		}
		
	}
	
	class print extends JFrame
	{
		public print()
		{
			setTitle("Document");
			setResizable(false);
			setSize( 600, 700 );
			setBackground( Color.gray );
			getContentPane().setLayout( new BorderLayout() );
	
			JPanel topPanel = new JPanel();
			topPanel.setLayout( new BorderLayout() );
			getContentPane().add( topPanel, BorderLayout.CENTER );
			//------------------------------------------------
			//starts the printing part
			StringBuffer text = new StringBuffer();
		
			text.append("Name:" + fnamefield.getText() + " " + lnamefield.getText() + "\n");
			
			text.append("WO#" + wonum.getText() + "\n");
			if(prioritybox.getSelectedItem() != null)
				text.append("Priority:" + prioritybox.getSelectedItem() + "\n");
			text.append("Campus:" + campusbox.getSelectedItem() + "\n");
			text.append("Room:" + roomnum.getText() + "\n");
			text.append("Phone:" + phonefield.getText() + "\n");
			text.append("ECISD #:"+ ecisdnum.getText() + "\n");
			text.append("Model:" + model.getText() + "\n");
			text.append("Opened by:" + openedbyfield.getText() + "\n");
			text.append("Closed by:" + closedbyfield.getText() + "\n");
			text.append("Date Opened:" + dateopenedfield.getText() + "\n");
			text.append("Date Closed:" + dateclosedfield.getText() + "\n");
			text.append("Service Tag:" + servicetagfield.getText() + "\n");
			text.append("Assigned to:" + assignedtobox.getSelectedItem() + "\n\n");
			text.append("--------------------------------------------------------------------------------------------------------------------------\n");
			text.append("Problem description:" + problemdes.getText() +"\n\n\n");
			text.append("Problem resolution:" + problemres.getText());
			
			//--------------------------------------------------
			//sends off the print job
			JEditorPane finaltext = new JEditorPane();
			topPanel.add(finaltext);
			finaltext.setText(text.toString());
			
			this.setVisible(true);
			PrintComponent printer = new PrintComponent(finaltext);
			this.dispose();
		}
	}
	
	
	public String removeCharacters(String data)
	{	
		int length = -1;
		
		if(data != null)
			length = data.length();
		else
			data = "null";
		
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
			if(data.charAt(c) == 59) //if the character is a ;
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
			
		}
		return data;
	}
}