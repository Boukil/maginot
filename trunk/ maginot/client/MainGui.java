import javax.swing.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.DefaultTableModel;
import java.util.StringTokenizer;
import java.util.Arrays;
import javax.swing.table.TableColumn;

public class MainGui extends JFrame implements ActionListener
{
    private final double versionnum = 2.96;
    //communication with the server
    private Socket s = null;
    
	TransferObject messagefs,messagets;
	
	//GUI
	Container contentPane;
	
	//stuff for the first window
	private JPanel north_panel_south, north_panel,panel,panel1,panel2,panel3,panel5,south_panel5,
						flow_panel1,flow_panel2,flow_panel3;
	
    private TextField usernamefield;
    private String username;
    private TextField passwordfield,password1field,password2field;
    private JComboBox campusbox;
	boolean authenticated = true;
    private int userlevel;
    
    //work order information
    Object original[][]; //these are for open work orders on tab 1
    
    Object originalpendingopen[][];
    
    Object originalpendingclose[][];
    
    JScrollPane scrollpane1,scrollpane2,scrollpane3,historyscrollpane;
    
    //we need to know if this is the first or second time one of the show buttons is clicked
    // because if it is the second, we need to be able to take the previous table off of the panel.
    private int num_open_clicked = 0;
    private int num_pendingopen_clicked = 0;
    private int num_pendingclosed_clicked = 0;
    Color bgColor,txtColor;
    
    String oldpassword = "";
    
	int originalROW,originalCOL;
	DefaultTableModel tm,pendingtm;
	JTable table,historytable,pendingclosetable,pendingopentable;
	JLabel numberopenlabel;
	//--------------------------------------------------------------------------
	//stuff for the new work order
	private JTextField fnamefield,lnamefield,campusfield,phonefield,
		ecisdnum,model,roomnum, wonum, dateopenedfield,dateclosedfield,servicetagfield;
		
	private JComboBox campusboxnewwo,assignedtobox,prioritybox;
	JComboBox campusbox_tab1; //except this, which is on tab 1
	private JTextArea problemdes,problemres;
	//--------------------------------------------------------------------------

	JTabbedPane tabPanel;
	
	public MainGui()
	{
    	//-------------------------------------------------------
		establistConnection();
		authenticate(); //go authenticate to the server first.
		//if the user can authenticate, the rest of the GUI is drawn.
	}
	public void establistConnection()
	{
		System.out.println("Establishing Connection");
		//sets up the connection.
		try
		{
			int serverPort = 3000;
			
			//s = new Socket("NOC-MAG01.ectorcountyisd.org",serverPort);
			s = new Socket ("10.100.1.49",serverPort);
			//s = new Socket("10.100.2.34",serverPort);
			
			ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(s.getInputStream());
			
			messagets = new TransferObject();
			messagefs = new TransferObject();
			
			ToServerThread mythread2 = new ToServerThread(out,s,messagets);
			mythread2.start();
			
			FromServerThreadObject mythread3 = new FromServerThreadObject(in,s,messagefs);
			mythread3.start();
		}
		catch(UnknownHostException f)
		{
			System.out.println("Sock:" + f.getMessage());
			JOptionPane.showMessageDialog(null,"Could not connect to server. \nThe server may be down. \nProgram will exit.");
	    	System.exit(0);
		}
		catch(EOFException f)
		{
			System.out.println("EOF: " + f.getMessage());
			JOptionPane.showMessageDialog(null,"Could not connect to server.\nThe server may be down. \nProgram will exit.");
	    	System.exit(0);
		}
		catch(IOException f)
		{
			System.out.println("IO:" + f.getMessage());
			JOptionPane.showMessageDialog(null,"Could not connect to server. \nThe server may be down. \nProgram will exit.");
	    	System.exit(0);
		}
		finally
		{
			
		}
	}
	
	public void authenticate()
	{
		boolean domainname = false;
		
		String username = System.getProperty("user.name");
		for(int c = 0; c < username.length(); c++)
		{
			String ip = s.getInetAddress().toString();
			
			if((username.charAt(c) == '.') && (c > 1) && (c < username.length()) && (Integer.parseInt(ip.substring(1,ip.indexOf("."))) == 10))
			{
				domainname = true;
				messagets.putMessage("026:" + username);
				try
				{
					userlevel = Integer.parseInt(messagefs.getMessage().toString());	
				}
				catch(Exception e)
				{
					userlevel = 2;
				}
				boolean goodversion = authenticateVersion();
				if(goodversion)
				{
					gui();
				}
				else
				{
					JOptionPane.showMessageDialog(null,"This version of the Maginot Software is out of date. \n Please obtain a new copy." );
					System.exit(0);
				}
			}
		}
			
		if(domainname == false)
		{
			System.out.println("drawing GUI");
			setSize(250, 150);
		//	setResizable(false);
			setTitle("Work Order Login");
			contentPane = getContentPane( );
			contentPane.setLayout(new BorderLayout( ));
						
			JFrame myWindow = new JFrame();
			myWindow.getContentPane().setLayout(new BorderLayout());
			addWindowListener(new WindowCloser(myWindow));
			JLabel usernamelabel = new JLabel("Username:");
			JLabel passwordlabel = new JLabel("Password:");
			
			
			usernamefield = new TextField(20);
			passwordfield = new TextField(20);
			passwordfield.setEchoChar('*');
			
			usernamefield.addActionListener(this);
			passwordfield.addActionListener(this);
			
			panel = new JPanel();
			panel.setLayout(new BorderLayout());
			contentPane.add(panel);
			
			
			JPanel west_Panel = new JPanel();
			west_Panel.setLayout(new GridLayout(3,1));
			panel.add(west_Panel,BorderLayout.WEST);
			
			west_Panel.add(usernamelabel);
			west_Panel.add(passwordlabel);
			
			JPanel east_Panel = new JPanel();
			east_Panel.setLayout(new GridLayout(3,2));
			panel.add(east_Panel,BorderLayout.EAST);
			
			JPanel flow_panel1 = new JPanel();
			JPanel flow_panel2 = new JPanel();
			JPanel flow_panel3 = new JPanel();
			
			east_Panel.add(flow_panel1);
			east_Panel.add(flow_panel2);
			east_Panel.add(flow_panel3); 
			
			flow_panel1.add(usernamefield);
			flow_panel2.add(passwordfield);
			
			JPanel buttonflow = new JPanel();
			buttonflow.setLayout(new FlowLayout());
			panel.add(buttonflow,BorderLayout.SOUTH);
			
			JButton loginbutton = new JButton("Login");
			loginbutton.addActionListener(this);
			buttonflow.add(loginbutton);
			this.setVisible(true);
		}
	}
	
	public boolean authenticateVersion()
	{
		String message = "030:" + versionnum;
		messagets.putMessage(message);
		String data = messagefs.getMessage().toString();
		return data.equalsIgnoreCase("OK");
	}

	public void gui()
	{
		getColors();
		setSize(760,695);
		setResizable(true);
		setTitle("Maginot Client v2.9.6");
		contentPane = getContentPane( );
        contentPane.setLayout(new BorderLayout( ));
        JFrame myWindow = new JFrame();
        myWindow.getContentPane().setLayout(new BorderLayout());
        addWindowListener(new WindowDestroyer(messagets));
      
      	// the tabbed pane and all the tabs
        tabPanel = new JTabbedPane();

        //----------------------------------------------------------------------
        //puts the tabs on the tabbed pane. 
        panel1 = new JPanel();
        panel1.setBackground(bgColor);
        panel1.setLayout(new BorderLayout());
        tabPanel.addTab("Open Work Orders",panel1);
        
        if(userlevel == 0)
        {
        	panel2 = new JPanel();
        	panel2.setBackground(bgColor);
	        panel2.setLayout(new BorderLayout());
	        tabPanel.addTab("Pending Open",panel2);
	        
	        panel3 = new JPanel();
	        panel3.setBackground(bgColor);
	        panel3.setLayout(new BorderLayout());
	        tabPanel.addTab("Pending Closed",panel3);
        }
        
        panel5 = new JPanel();
        panel5.setBackground(bgColor);
        panel5.setLayout(new BorderLayout());
        tabPanel.addTab("New Work Order",panel5);
        
        if(userlevel ==2)
        {
        	tabPanel.setSelectedIndex(1);
        }
        //----------------------------------------------------------------------
        //panel1
        
        //get a list of sites from the server
        String message = "054:";
        messagets.putMessage(message);
        String campusoptions[] = messagefs.getMessage().toString().split(",");
        
        Arrays.sort(campusoptions);
        
        campusbox_tab1 = new JComboBox(campusoptions);
        
        JPanel campusboxpanel_tab1 = new JPanel();
        campusboxpanel_tab1.setBackground(bgColor);
        campusboxpanel_tab1.setLayout(new FlowLayout());
        campusboxpanel_tab1.add(campusbox_tab1);
        panel1.add(campusboxpanel_tab1,BorderLayout.NORTH);
        
        JButton showcampusbutton1 = new JButton("Show Campus");
        showcampusbutton1.setForeground(txtColor);
        
        showcampusbutton1.addActionListener(this);
       	campusboxpanel_tab1.add(showcampusbutton1);
       	
       	if(userlevel != 2)
       	{
       		JButton showallwobutton = new JButton("Show All Open W/Os");
       		showallwobutton.setForeground(txtColor);
       		showallwobutton.addActionListener(this);
       		campusboxpanel_tab1.add(showallwobutton);
       		
       		JButton myworkordersbutton = new JButton("My Workorders");
       		myworkordersbutton.setForeground(txtColor);
       		myworkordersbutton.addActionListener(this);
       		campusboxpanel_tab1.add(myworkordersbutton);
       	}
       	
       	JButton printcampusbutton = new JButton("Print");
       	printcampusbutton.setForeground(txtColor);
       	printcampusbutton.addActionListener(this);
       	campusboxpanel_tab1.add(printcampusbutton);

        contentPane.add(tabPanel);
        if(userlevel == 0)
        {
        	//----------------------------------------------------------------------
	        // panel2
	        JPanel north_panel2 = new JPanel();
	        north_panel2.setBackground(bgColor);
	        north_panel2.setLayout(new FlowLayout());
	        panel2.add(north_panel2,BorderLayout.NORTH);
	        
	        JButton showpendingopen = new JButton("Show Work Orders Pending Open");
	        showpendingopen.setForeground(txtColor);
	        showpendingopen.addActionListener(this);
	        north_panel2.add(showpendingopen);
	        
	        JButton printpendingopenbutton = new JButton("Print");
	        printpendingopenbutton.setForeground(txtColor);
       		printpendingopenbutton.addActionListener(this);
       		north_panel2.add(printpendingopenbutton);
	        
	        //----------------------------------------------------------------------
	        // panel3
	        JPanel north_panel3 = new JPanel();
	        north_panel3.setBackground(bgColor);
	        north_panel3.setLayout(new FlowLayout());
	        panel3.add(north_panel3,BorderLayout.NORTH);
	        
	        JButton showpendingclosed = new JButton("Show Work Orders Pending Closed");
	        showpendingclosed.setForeground(txtColor);
	        showpendingclosed.addActionListener(this);
	        north_panel3.add(showpendingclosed);
	        
	        JButton printpendingclosedbutton = new JButton("Print");
	        printpendingclosedbutton.setForeground(txtColor);
       		printpendingclosedbutton.addActionListener(this);
       		north_panel3.add(printpendingclosedbutton);
        }
        //----------------------------------------------------------------------
        //panel5  new work order
        
        JPanel north_panel5 = new JPanel();
        north_panel5.setBackground(bgColor);
        north_panel5.setLayout(new GridLayout(6,2));
        panel5.add(north_panel5,BorderLayout.NORTH);
        
        JPanel flows[] = new JPanel[12];
        
        for(int c = 0; c < 12; c++)
        {
        	flows[c] = new JPanel();
        	flows[c].setBackground(bgColor);
        	flows[c].setLayout(new FlowLayout());
        	north_panel5.add(flows[c]);
        }
        
        
        
        fnamefield = new JTextField(20);
        
        if(userlevel == 2)
        {
        	//could crash if there is no .
        	try{fnamefield.setText(System.getProperty("user.name").substring(0,System.getProperty("user.name").indexOf(".")));}
        	catch(Exception e){}
        }
        
        
        JLabel fnamelabel = new JLabel("First Name:");
        fnamelabel.setForeground(txtColor);
        flows[0].add(fnamelabel);
        flows[0].add(fnamefield);
        
        lnamefield = new JTextField(20);
        if(userlevel == 2)
        {
        	try{lnamefield.setText(System.getProperty("user.name").substring(System.getProperty("user.name").indexOf(".")+1,System.getProperty("user.name").length()));}
        	catch(Exception e){}
        }
        JLabel lnamelabel = new JLabel("Last Name:      ");
        lnamelabel.setForeground(txtColor);
        flows[1].add(lnamelabel);
        flows[1].add(lnamefield);
        
        if(userlevel != 2)
        {
        	JLabel wonumberlabel = new JLabel("W/O Number:");
        	wonumberlabel.setForeground(txtColor);
        	wonum = new JTextField(5);
        	wonum.setEditable(false);
        	flows[2].add(wonumberlabel);
        	flows[2].add(wonum);
        }
        if(userlevel < 2)
        {
        	String priorityoptions[] = {"1","2","3"};
        
	        prioritybox = new JComboBox(priorityoptions);
	        JLabel prioritylabel = new JLabel("Priority:    ");
	        prioritylabel.setForeground(txtColor);
	        flows[2].add(prioritylabel);
	        flows[2].add(prioritybox);
        }
        
        
        roomnum = new JTextField(20);
        JLabel roomnumlabel = new JLabel("Room Number:");
        roomnumlabel.setForeground(txtColor);
        flows[3].add(roomnumlabel);
        flows[3].add(roomnum);
        
        campusboxnewwo = new JComboBox(campusoptions);
        campusboxnewwo.addActionListener(this); // for the auto fill.
        JLabel campusboxlabel = new JLabel("Campus:");
        campusboxlabel.setForeground(txtColor);
        flows[4].add(campusboxlabel);
        flows[4].add(campusboxnewwo);
        
        phonefield = new JTextField(20);
        JLabel phonelabel = new JLabel("Phone Number:");
        phonelabel.setForeground(txtColor);
        flows[5].add(phonelabel);
        flows[5].add(phonefield);
        
        ecisdnum = new JTextField(20);
        JLabel ecisdlabel = new JLabel("ECISD number:");
        ecisdlabel.setForeground(txtColor);
        flows[6].add(ecisdlabel);
        flows[6].add(ecisdnum);
        
        if(userlevel !=2)
        {
        	model = new JTextField(20); 
	        JLabel modellabel = new JLabel("Model:                ");
	        modellabel.setForeground(txtColor);
	        flows[7].add(modellabel);
	        flows[7].add(model);
        	
        	dateopenedfield = new JTextField(20);
	        dateopenedfield.setEditable(false);
	        JLabel dateopenedlabel = new JLabel("Date Opened:");
	        dateopenedlabel.setForeground(txtColor);
	        flows[8].add(dateopenedlabel);
	        flows[8].add(dateopenedfield);
        	
        	dateclosedfield = new JTextField(20);
	        dateclosedfield.setEditable(false);
	        JLabel dateclosedlabel = new JLabel("Date Closed:");
	        dateclosedlabel.setForeground(txtColor);
	        flows[9].add(dateclosedlabel);
	        flows[9].add(dateclosedfield);
        }
        
        
        servicetagfield = new JTextField(20);
        JLabel servicetaglabel = new JLabel("Service Tag:");
        servicetaglabel.setForeground(txtColor);
        flows[10].add(servicetaglabel);
        flows[10].add(servicetagfield);
        
        //get a list of techs from the server
        message = "025:";
        messagets.putMessage(message);
        String assignedtooptions[] = messagefs.getMessage().toString().split(",");
        Arrays.sort(assignedtooptions);
        
        if(userlevel !=2)
        {
        	assignedtobox = new JComboBox(assignedtooptions);
        	JLabel assignedtolabel = new JLabel("Assigned To:");
        	assignedtolabel.setForeground(txtColor);
        	assignedtobox.setSelectedItem("Technicians");
        	flows[11].add(assignedtolabel);
        	flows[11].add(assignedtobox);
        }
        
        
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
        flowproblemdes.setLayout(new FlowLayout());
        west_center_panel5.add(flowproblemdes,BorderLayout.CENTER);
        
        JLabel blanklabel = new JLabel("                  ");
        blanklabel.setForeground(txtColor);
        west_center_panel5.add(blanklabel,BorderLayout.WEST);
        
        problemdes = new JTextArea(20,25);
        problemdes.setLineWrap(true);
        problemdes.setWrapStyleWord(true);
        
        JScrollPane scrolledtext = new JScrollPane(problemdes);
        
        flowproblemdes.add(scrolledtext);
        if(userlevel != 2)
        {
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
	        flowproblemres.setLayout(new FlowLayout());
	        east_center_panel5.add(flowproblemres,BorderLayout.CENTER);
	        
	        JLabel blanklabel2 = new JLabel("                     ");
	        blanklabel2.setForeground(txtColor);
	        east_center_panel5.add(blanklabel2,BorderLayout.EAST);
	        
	        problemres = new JTextArea(20,25);
	        problemres.setLineWrap(true);
	        problemres.setWrapStyleWord(true);
	        
	        JScrollPane scrolledtext2 = new JScrollPane(problemres);
	        
	        flowproblemres.add(scrolledtext2);
        }
        
        south_panel5 = new JPanel();
        south_panel5.setBackground(bgColor);
        south_panel5.setLayout(new BorderLayout());
        panel5.add(south_panel5,BorderLayout.SOUTH);
        
        
        JPanel button_south_panel5 = new JPanel();
        button_south_panel5.setBackground(bgColor);
        button_south_panel5.setLayout(new FlowLayout());
        south_panel5.add(button_south_panel5,BorderLayout.NORTH);
        
        if(userlevel !=2)
        {
        	JButton openWObuttontech = new JButton("Open for technicians");
        	openWObuttontech.setForeground(txtColor);
        	button_south_panel5.add(openWObuttontech);
       		openWObuttontech.addActionListener(this);
        }
        if(userlevel ==2)
        {
        	JButton openWObuttontech = new JButton("Request Work Order");
        	openWObuttontech.setForeground(txtColor);
        	button_south_panel5.add(openWObuttontech);
       		openWObuttontech.addActionListener(this);
        }
        
        if(userlevel == 0)
        {
        	JButton openWObuttonphone = new JButton("Open for phone support");
        	openWObuttonphone.setForeground(txtColor);
        	openWObuttonphone.addActionListener(this);
        	button_south_panel5.add(openWObuttonphone);
        }
        if(userlevel !=2)
        {
        	JButton closebutton = new JButton("Close");
        	closebutton.setForeground(txtColor);
    		closebutton.addActionListener(this);
    		button_south_panel5.add(closebutton);
        }
        
        //----------------------------------------------------------------------
        //file menu
        
        //Creates the Menus
		JMenu file = new JMenu("File");
		// creates the menu bar
		JMenuBar mBar = new JMenuBar(); 
		mBar.add(file);
		setJMenuBar(mBar);
		if(userlevel == 0)
		{
			JMenu managermenu = new JMenu("Managers");
			
			JMenuItem resetpas = new JMenuItem("User Manager");
			resetpas.addActionListener(this);
			managermenu.add(resetpas);
			
			JMenuItem sitemanager = new JMenuItem("Site Manager");
			sitemanager.addActionListener(this);
			managermenu.add(sitemanager);
				
			JMenuItem emailconfig = new JMenuItem("Email Manager");
			emailconfig.addActionListener(this);
			managermenu.add(emailconfig);
			
			mBar.add(managermenu);
			
			JMenu databasemenu = new JMenu("Database");
			
			JMenuItem databasecleanup = new JMenuItem("Cleanup Database...");
			databasecleanup.addActionListener(this);
			databasemenu.add(databasecleanup);
			databasemenu.addSeparator();
			
			JMenuItem databasebackup = new JMenuItem("Backup Database...");
			databasebackup.addActionListener(this);
			databasemenu.add(databasebackup);
			
			
			JMenuItem databaserestore = new JMenuItem("Restore Database...");
			databaserestore.addActionListener(this);
			databasemenu.add(databaserestore);
			mBar.add(databasemenu);
		}
		if(userlevel != 2)
		{
			JMenuItem openwonum = new JMenuItem("View Work Order #...");
			openwonum.addActionListener(this);
			file.add(openwonum);
			file.addSeparator();
			
			JMenuItem statusmenu = new JMenuItem("People Status");
			statusmenu.addActionListener(this);
			file.add(statusmenu);
			
			JMenuItem mystatsmenu = new JMenuItem("My Status");
			mystatsmenu.addActionListener(this);
			file.add(mystatsmenu);
			file.addSeparator();
		}
		
		JMenuItem changemypas = new JMenuItem("Change My Password");
		changemypas.addActionListener(this);
		file.add(changemypas);
		
		JMenuItem mysettings = new JMenuItem("Change My Settings");
		mysettings.addActionListener(this);
		file.add(mysettings);
		
		if(userlevel !=2)
		{
			file.addSeparator();
			JMenuItem reports = new JMenuItem("Reports");
			reports.addActionListener(this);
			file.add(reports);
			
			JMenuItem customreport = new JMenuItem("Custom Report");
			customreport.addActionListener(this);
			file.add(customreport);
			
		}
		
		
		//Creates the Menus
		JMenu help = new JMenu("Help");
		
		JMenuItem about = new JMenuItem("About: Work Order System");
		about.addActionListener(this);
		
		JMenuItem status = new JMenuItem("Status");
		status.addActionListener(this);
		
		help.add(status);
		help.add(about);
		mBar.add(help);
		showDefaultOpen();
		this.setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e)
	{
		String actionCommand = e.getActionCommand( );
		//======================================================================
		//a login request.
		if(((actionCommand.equals("Login")) || (usernamefield == e.getSource()) || (passwordfield == e.getSource())) && (!usernamefield.getText().equals("")) && (!passwordfield.getText().equals("")))
		{
			username = usernamefield.getText();
			Encryption f = new Encryption();
			f.setPassword(passwordfield.getText());
			
			String data = "022:" + removeCharacters(username) + "," + removeCharacters(f.getPassword());
			System.out.println(data);
			messagets.putMessage(data);
			
			String result = messagefs.getMessage().toString();
			
			int iresult = Integer.parseInt(result.substring(0,1));
			
			
			//if the username is null, the user will authenticate. 
			if(iresult == 0)
			{
				JOptionPane.showMessageDialog(null,"Authentication Failed:\n Invalid username, or password.");
			}
			else if(iresult == 1)
			{
				userlevel = Integer.parseInt(result.substring(1,2)); //sets the userlevel
				boolean goodversion = authenticateVersion();
				
				if(goodversion)
				{
					if(passwordfield.getText().equalsIgnoreCase("ecisd"))
					{
						JOptionPane.showMessageDialog(null, "You have a default password. \n You will now be prompted to change your password. \n You must change it before you can continue");
						contentPane.remove(panel);
						changePasswordDialog(passwordfield.getText());
					}
					else
					{
						//-------------------------------------------------------------
						//if this does not happen here, it will happen in the changePasswordDialog method after 
						//the user changes their password.
						JOptionPane.showMessageDialog(null,"Authentication Successful");
						contentPane.remove(panel);
						this.dispose();
						gui();
					}
				}
				else
				{
					JOptionPane.showMessageDialog(null,"This version of the Maginot Software is out of date. \n Please obtain a new copy." );
					System.exit(0);
				}
			}
			
			else if(iresult == 2)
			{
				JOptionPane.showMessageDialog(null,"Your user account has expired. \n Contact the Help Desk to solve this problem.");
				System.exit(0);
			}
		}
		if(e.getSource() == campusboxnewwo)
		{
			messagets.putMessage("070:" + campusboxnewwo.getSelectedItem());
			String data = messagefs.getMessage().toString();
			phonefield.setText(data);
		}
		if(actionCommand.equals("View Work Order #..."))
		{
			String temp_number = "";
			try
			{
				temp_number = JOptionPane.showInputDialog(null,"Enter the Work Order Number").trim();
				
				if((temp_number != null))
				{
					int number = Integer.parseInt(temp_number);
					WODetails object = new WODetails(messagets,messagefs,userlevel,number,4);
				}
			}
			catch(Exception f)
			{
				if(temp_number.length() > 0)
					JOptionPane.showMessageDialog(null,"You must enter only numbers here." + f);
			}
		}
		if(actionCommand.equals("Backup Database..."))
		{
			BackupDatabase backup = new BackupDatabase(messagets,messagefs);
		}
		if(actionCommand.equals("Cleanup Database..."))
		{
			messagets.putMessage("150:");
			JOptionPane.showMessageDialog(null,"Database cleanup Started");
		}
		if(actionCommand.equals("Show All Open W/Os"))
		{
			showDefaultOpen();
		}
		if(actionCommand.equals("Restore Database..."))
		{
			RestoreDatabase restore = new RestoreDatabase(messagets,messagefs);
		}
		if(actionCommand.equals("My Workorders"))
		{
			showMyWorkorders();
		}
		if(actionCommand.equals("Change My Settings"))
		{
			MySettings object = new MySettings(messagets,messagefs);
		}
		if(actionCommand.equals("Print"))
		{
			switch(tabPanel.getSelectedIndex())
			{
				case 0:
					PrintJTable print = new PrintJTable(table);
				break;
				case 1:
					print = new PrintJTable(pendingopentable);
				break;
				case 2:
					print = new PrintJTable(pendingclosetable);
				break;
			}
			
		}
		if(actionCommand.equals("Custom Report"))
		{
			CustomReport report = new CustomReport(messagets,messagefs);
			
		}
		if(actionCommand.equals("People Status"))
		{
			PeopleStatus status = new PeopleStatus(messagets,messagefs);
		}
		if(actionCommand.equals("My Status"))
		{
			MyStatus status = new MyStatus(messagets,messagefs);
		}
		if(actionCommand.equals("Run Database Cleanup"))
		{
			messagets.putMessage("080:");
			JOptionPane.showMessageDialog(null,"Database cleanup launched. \n This utility will run in the background on the server.");
		}
		if(actionCommand.equals("Email Manager"))
		{
			EmailConfiguration email = new EmailConfiguration(messagets,messagefs);
			email.setVisible(true);
		}
		if(actionCommand.equals("Change My Password"))
		{
			//this is for the user who wants to change their password. 
			ChangeMyPassword object = new ChangeMyPassword(messagets,messagefs,username);
		}
		if(actionCommand.equals("Site Manager"))
		{
			SiteManager object = new SiteManager(messagets,messagefs);
		}
		if(actionCommand.equals("Status"))
		{
			StatusDialog object = new StatusDialog(messagets,messagefs);
		}
		if((actionCommand.equals("Change Password")) || (password2field == e.getSource()) || (password1field == e.getSource()))
		{
			//this is for the user who has a default password to change it. 
			String pw1 = password1field.getText();
			String pw2 = password2field.getText();
			
			if(pw1.equals(pw2))
			{
				if(pw1.equalsIgnoreCase(oldpassword))
				{
					JOptionPane.showMessageDialog(null,"Your password is the same as you old password. \n You must change your password before you can continue");
				}
				else
				{
					//package up the new password and send it to the server.
					Encryption f = new Encryption();
					f.setPassword(pw1);
					
					String message = "024:" + removeCharacters(f.getPassword()) + "," + username;
					messagets.putMessage(message);
					
					String ok = messagefs.getMessage().toString();
					
					if(ok.equals("OK"))
					{
						JOptionPane.showMessageDialog(null,"Authentication Successful");
						contentPane.remove(panel);
						this.dispose();
						gui();
					}
					else
					{
						JOptionPane.showMessageDialog(null,"Your password was not reset. \n The server may be experiencing problems. \n Please try again");
					}
				}
			}
			else
			{
				JOptionPane.showMessageDialog(null,"Your passwords do not match");
			}
		}
		if(actionCommand.equals("Request Work Order"))
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
					if(servicetagfield.getText().equals(""))
					{
						servicetagfield.setText("null");
					}
					else if(ecisdnum.getText().trim().equals(""))
					{
						ecisdnum.setText("-1");
					}
					
					//translate the Selected Index of the campus box to an actual index in the database. 
					String message = "056:" + campusboxnewwo.getSelectedItem();
					messagets.putMessage(message);
					String campus_index = messagefs.getMessage().toString();
					
					//the userlevel 2s can't see the priority field therefore it is null. Assign a default value here.
					if(userlevel == 2)
					{
					//get the data from the fields and put it in a request, send it, and reset the fields. 
					message = "012:" +						
							removeCharacters(ecisdnum.getText().trim()) + "," +			removeCharacters(problemdes.getText()) 	+ "," +
							"null" + "," +	campus_index		+ "," +
							removeCharacters(fnamefield.getText())	+ "," +
							removeCharacters(lnamefield.getText()) + "," +			removeCharacters(roomnum.getText())  	+ "," +
							removeCharacters(phonefield.getText()) + "," +			 "null" 	+ "," +
							removeCharacters(servicetagfield.getText().trim()) + "," +	"10";		
					}
					//everyone else can see the priority field. 
					else
					{
						//get the data from the fields and put it in a request, send it, and reset the fields. 
					message = "012:" +						
							removeCharacters(ecisdnum.getText().trim()) + "," +			removeCharacters(problemdes.getText()) 	+ "," +
							"null" + "," +	campus_index		+ "," +
							removeCharacters(fnamefield.getText())	+ "," +
							removeCharacters(lnamefield.getText()) + "," +			removeCharacters(roomnum.getText())  	+ "," +
							removeCharacters(phonefield.getText()) + "," +			 prioritybox.getSelectedItem() 	+ "," +
							removeCharacters(servicetagfield.getText().trim()) + "," +		"Technicians";	
					
					}			
													
					
					//resets the text fields. 
					
					ecisdnum.setText("");
					servicetagfield.setText("");
					
					messagets.putMessage(message);
					JOptionPane.showMessageDialog(null,"Your work order has been sent to the Help Desk");
				}
				else
				{
					JOptionPane.showMessageDialog(null,"One of your required fields does not have any data \n or the data is invalid.\n Please completely fill in this form with valid data to continue.");
				}
		}
		//=======================================================================
		if(actionCommand.equals("Open for technicians"))
		{
			//------------------------------------------------
			//this section sets up the problem descripition or resolution string.
			messagets.putMessage("060:");
			String data = messagefs.getMessage().toString();
			
			String problemdesString = problemdes.getText();
			
			String problemresString = problemres.getText();
			
			if(problemdesString.length() < 1)
				problemdesString = "null";
			else if(problemdes.getText().length() > 1)
				problemdesString = problemdesString + "--" + data + "\n\n";
				
			if(problemresString.length() < 1)
				problemresString = "null";
			else if(problemres.getText().length() > 1)
				problemresString = problemresString  + "--" + data + "\n\n";
			
			//----------------------------------------------------------
			
			if(wonum.getText().equals("")) // if the work order doesn't alredy have a number
			{
				boolean goon = true;
				//make sure there is an entry in all the fields. 
				if(fnamefield.getText().equals(""))		{goon = false;}
				else if(lnamefield.getText().equals("")){goon = false;}
				else if(problemdes.getText().equals("")){goon = false;}
				
				//make sure the ecisd number can be parsed so we don't crash the server
				try
				{
					int number = Integer.parseInt(ecisdnum.getText().trim());
				}
				catch(Exception f)
				{
					if(!ecisdnum.getText().equals(""))
					{
						goon = false;
						JOptionPane.showMessageDialog(null,"Invalid ECISD number. You can only enter numbers here. \n If you do not know the number leave this field blank");
					}
				}
					
				if(goon)
				{
					//the following do not have to have values, but to avoid
					//making the server crash, insert a string "null";
					if(model.getText().equals(""))					{model.setText("null");}
					if(roomnum.getText().equals(""))				{roomnum.setText("null");}
					if(problemres.getText().equals(""))				{problemres.setText("null");}
					if(phonefield.getText().equals("")) 			{phonefield.setText("null");}
					if(servicetagfield.getText().equals(""))		{servicetagfield.setText("null");}
					if(ecisdnum.getText().trim().equals(""))		{ecisdnum.setText("null");}
					
					//translate the Selected Index of the campus box to an actual index in the database. 
					String message = "056:" + campusboxnewwo.getSelectedItem();
					messagets.putMessage(message);
					String campus_index = messagefs.getMessage().toString();
					//translate the assigned to box to an index in the database.
					
					message = "046:" + assignedtobox.getSelectedItem();
					messagets.putMessage(message);
					String assignedtoindex = messagefs.getMessage().toString();
				
				
					//get the data from the fields and put it in a request, send it, and reset the fields. 
					message = "006:" +						
							removeCharacters(ecisdnum.getText().trim()) + "," +			removeCharacters(problemdesString) 	+ "," +
							removeCharacters(problemresString) + "," +			campus_index		+ "," +
							removeCharacters(model.getText()) + "," +				removeCharacters(fnamefield.getText())	+ "," +
							removeCharacters(lnamefield.getText()) + "," +			removeCharacters(roomnum.getText())  	+ "," +
							removeCharacters(phonefield.getText()) + "," +			prioritybox.getSelectedItem() 	+ "," +
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
					servicetagfield.setText("");
					dateopenedfield.setText("");
					wonum.setText("");
					messagets.putMessage(message);
				}
				else
				{
					JOptionPane.showMessageDialog(null,"One of your required fields does not have any data \n or the data is invalid.\n Please completely fill in this form with valid data to continue.");
				}
			}
			//if the work order already has a number
			if(!wonum.getText().equals("")) // if the work order alredy has a number
			{
				boolean goon = true;
				//make sure there is an entry in all the fields. 
				if(fnamefield.getText().equals(""))		{goon = false;}
				else if(lnamefield.getText().equals("")){goon = false;}
				else if(problemdes.getText().equals("")){goon = false;}
				
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
					if(model.getText().equals(""))					{model.setText("null");}
					if(roomnum.getText().equals(""))				{roomnum.setText("null");}
					if(problemres.getText().equals(""))				{problemres.setText("null");}
					if(phonefield.getText().equals("")) 			{phonefield.setText("null");}
					if(servicetagfield.getText().equals(""))		{servicetagfield.setText("null");}
					if(ecisdnum.getText().trim().equals(""))		{ecisdnum.setText("-1");}
					
					
					//translate the Selected Index of the campus box to an actual index in the database. 
					String message = "056:" + campusboxnewwo.getSelectedItem();
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
					dateopenedfield.setText("");
					wonum.setText("");
					
					messagets.putMessage(message);
				}
				else
				{
					JOptionPane.showMessageDialog(null,"One of your required fields does not have any data \n or the data is invalid.\n Please completely fill in this form with valid data to continue.");
				}
			}
		}
		//=====================================================================
		if(actionCommand.equals("Close")) //the close button on the new WO panel
		{
			
			if(!wonum.getText().equals("")) // if the work order alredy has a number
			{
				boolean goon = true;
				//make sure there is an entry in all the fields. 
				if(fnamefield.getText().equals(""))		{goon = false;}
				else if(lnamefield.getText().equals("")){goon = false;}
				else if(problemdes.getText().equals("")){goon = false;}
				
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
					if(model.getText().equals(""))					{model.setText("null");}
					if(roomnum.getText().equals(""))				{roomnum.setText("null");}
					if(problemres.getText().equals(""))				{problemres.setText("null");}
					if(phonefield.getText().equals("")) 			{phonefield.setText("null");}
					if(servicetagfield.getText().equals(""))		{servicetagfield.setText("null");}
					if(ecisdnum.getText().trim().equals(""))		{ecisdnum.setText("-1");}
					
					//translate the Selected Index of the campus box to an actual index in the database. 
					String message = "056:" + campusboxnewwo.getSelectedItem();
					messagets.putMessage(message);
					String campus_index = messagefs.getMessage().toString();
					
					//translate the assigned to box to an index in the database.
					message = "046:" + assignedtobox.getSelectedItem();
					messagets.putMessage(message);
					String assignedtoindex = messagefs.getMessage().toString();	
					
					//get the data from the fields and put it in a request, send it, and reset the fields. 
					message = "008:" + removeCharacters(fnamefield.getText()) 							+ ","+ removeCharacters(lnamefield.getText()) 
										+ ","+ campus_index 											+ ","+ removeCharacters(roomnum.getText()) 
										+ ","+ prioritybox.getSelectedItem()							+ ","+ removeCharacters(phonefield.getText()) 
										+ ","+ removeCharacters(ecisdnum.getText().trim()) 				+ ","+ removeCharacters(model.getText()) 
										+ ","+removeCharacters(problemdes.getText()) 					+ ","+removeCharacters(problemres.getText()) 
										+ ","+wonum.getText()
										+ "," + removeCharacters(servicetagfield.getText().trim()) 		+ "," + assignedtoindex;
					
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
				}
				else
				{
					JOptionPane.showMessageDialog(null,"One of your required fields does not have any data \n or the data is invalid.\n Please completely fill in this form with valid data to continue.");
				}
			}
		}
		//======================================================================
		if(actionCommand.equals("Open for phone support"))
		{
		
			//querys the server for the machine history
			//and gets a work order number
			
			//make sure there is an entry in all the fields. 
			
				boolean goon = true;
				//make sure there is an entry in all the fields. 
				if(fnamefield.getText().equals(""))		{goon = false;}
				else if(lnamefield.getText().equals("")){goon = false;}
				else if(problemdes.getText().equals("")){goon = false;}
				
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
					
					if(model.getText().equals(""))					{model.setText("null");}
					if(roomnum.getText().equals(""))				{roomnum.setText("null");}
					if(problemres.getText().equals(""))				{problemres.setText("null");}
					if(phonefield.getText().equals("")) 			{phonefield.setText("null");}
					if(servicetagfield.getText().equals(""))		{servicetagfield.setText("null");}
					if(ecisdnum.getText().trim().equals(""))		{ecisdnum.setText("-1");}
					
				//translate the Selected Index of the campus box to an actual index in the database. 
				String message = "056:" + campusboxnewwo.getSelectedItem();
				messagets.putMessage(message);
				String campus_index = messagefs.getMessage().toString();
				
				//translate the assigned to box to an index in the database.
				message = "046:" + assignedtobox.getSelectedItem();
				messagets.putMessage(message);
				String assignedtoindex = messagefs.getMessage().toString();
			
				//get the data from the fields and put it in a request, send it, and reset the fields. 
				message = "007:" +						
							removeCharacters(ecisdnum.getText().trim()) + "," +				removeCharacters(problemdes.getText()) 		+ "," +
							removeCharacters(problemres.getText()) 		+ "," +				campus_index								+ "," +
							removeCharacters(model.getText())			+ "," +				removeCharacters(fnamefield.getText())		+ "," +
							removeCharacters(lnamefield.getText()) 		+ "," +				removeCharacters(roomnum.getText())  		+ "," +
							removeCharacters(phonefield.getText()) 		+ "," +				prioritybox.getSelectedItem()		 		+ "," +
							removeCharacters(servicetagfield.getText().trim()) + "," +		assignedtoindex;
				
				
				if(model.getText().equals("null"))					{model.setText("");}
				if(problemres.getText().equals("null"))				{problemres.setText("");}
				if(servicetagfield.getText().trim().equals("null"))	{servicetagfield.setText("");}
				if(ecisdnum.getText().trim().equals("-1"))			{ecisdnum.setText("");}
				if(phonefield.getText().trim().equals("null"))		{phonefield.setText("");}
				if(roomnum.getText().trim().equals("null"))			{roomnum.setText("");}
				
				messagets.putMessage(message);
				String data = messagefs.getMessage().toString(); //gets the work order number and the date opened
				StringTokenizer tokenizer = new StringTokenizer(data,",");
				wonum.setText(tokenizer.nextToken());
				dateopenedfield.setText(tokenizer.nextToken());
				//--------------------------------------------------------------
				//puts the history box at the bottom of the window.
				System.out.println(":" + ecisdnum.getText() +":");
				if((!ecisdnum.getText().equals("")) && (!ecisdnum.getText().equals("-1"))) //occasionally it will still be set to -1 when the program gets here.
				{
					//----------------------------------------------------------------------
					//Column name stuff
					messagets.putMessage("001:"); //request the columnnames from the server
					
					String new_columnNames = messagefs.getMessage().toString(); //get the columnname message from the server and refine them.
					
					//split up the column names and put it in this array of objects below.
					tokenizer = new StringTokenizer(new_columnNames,",");
					Object[] columnNames = new Object[tokenizer.countTokens()];
					
					columnNames = new_columnNames.split(","); //USE THIS INSTEAD OF TOKENIZER
					
					//----------------------------------------------------------------------
					JPanel south_south_panel5 = new JPanel();
					south_south_panel5.setLayout(new FlowLayout());
					
					south_panel5.add(south_south_panel5,BorderLayout.SOUTH);
					
					//-----------------------------------------------------------------------
					//request the workorder history
					message = "010:";
					messagets.putMessage(message);
					data = messagefs.getMessage().toString();
					
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
					//-------------------------------------------------------------------------
					//sets up the history table
					DefaultTableModel historytm = new DefaultTableModel() {public boolean isCellEditable(int rowIndex, int mColIndex) {return false;}}; 
					//this table model does not allow cell editing.
	
					historytm.setDataVector(workorders,columnNames);
					
					historytable = new JTable(historytm);
					historytable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
					historytable.addMouseListener(new WOHistoryMouseAdapter());
					setVisibleRowCount(historytable,10);
					historytable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					historytable.setAutoCreateRowSorter(true);
					
					historyscrollpane = new JScrollPane(historytable);
					historyscrollpane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
					south_south_panel5.add(historyscrollpane);
					south_south_panel5.validate();	
					pack();	
				}
				
			}
			else
			{
				JOptionPane.showMessageDialog(null,"One of your required fields does not have any data.\n Please completely fill in this form to continue.");
			}
		}
		//======================================================================
		if(actionCommand.equals("Show Work Orders Pending Open"))
		{
			showPendingOpen();
		}
		//======================================================================
		if(actionCommand.equals("Show Work Orders Pending Closed"))
		{
			showPendingClosed();
		}
		//======================================================================
		if(actionCommand.equals("About: Work Order System"))
		{	
			AboutDialog aboutbox = new AboutDialog();
			aboutbox.setVisible(true);
		}
		//======================================================================
		if(actionCommand.equals("Reports"))
		{
			Reports object = new Reports(messagets,messagefs,userlevel);
			object.setVisible(true);
		}
		//======================================================================
		if(actionCommand.equals("User Manager"))
		{
			UserManager object = new UserManager(messagets,messagefs);
			object.setVisible(true);
		}
		//======================================================================
		if(actionCommand.equals("Show Campus"))
		{
			panel1.remove(numberopenlabel);
			
			num_open_clicked++;
			if(num_open_clicked >= 1)
			{
				flow_panel1.remove(scrollpane1);
			}
			//gets the work orders for the current campus
			//translate the Selected Index of the campus box to an actual index in the database. 
			String message = "056:" + campusbox_tab1.getSelectedItem();
			System.out.println(message);
			messagets.putMessage(message);
			String campus_index = messagefs.getMessage().toString();
			
			message = "002:" + campus_index;
			messagets.putMessage(message);
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
			numberopenlabel = new JLabel("Number of open work orders: " + X + "");
			numberopenlabel.setForeground(txtColor);
			panel1.add(numberopenlabel,BorderLayout.SOUTH);
			//----------------------------------------------------------------------
			//Column name stuff
			//split up the column names and put it in this array of objects below.
			String raw_columnNames = messagefs.getMessage().toString();
			Object[] columnNames = raw_columnNames.split(",");

			//------------------------------------------------------------------
			//creates the table
			workorders = refineDataForTable(workorders);
			tm = new DefaultTableModel() {public boolean isCellEditable(int rowIndex, int mColIndex) {return false;}}; 
			tm.setDataVector(workorders,columnNames);
			
			table = new JTable(tm);
			table.addMouseListener(new WOOpenDetailsMouseAdapter());
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.setAutoCreateRowSorter(true);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			
			scrollpane1 = new JScrollPane(table);
			
			scrollpane1 = new JScrollPane(table);
		
			flow_panel1 = new JPanel();
			flow_panel1.setBackground(bgColor);
			flow_panel1.setLayout(new FlowLayout());
			flow_panel1.add(scrollpane1);
			panel1.add(flow_panel1,BorderLayout.CENTER);
			
			//-------------------------------------------------
			//changes the specified column to be x pixels wide.
		    int vColIndex = 2;
		    TableColumn col = table.getColumnModel().getColumn(vColIndex);
		    int width = 132;
		    col.setPreferredWidth(width);
			//-------------------------------------------------
			
			scrollpane1.setPreferredSize(new Dimension(600, 500));
			panel1.validate();
			
			//little extra thing here to show the current number of open workorders.
			JLabel numberopenlabel = new JLabel("Number of open work orders: " + X);
			panel1.add(numberopenlabel,BorderLayout.SOUTH);
		}
		//======================================================================
		
		
	} //these actionCommands are for the tabs and their buttons
	
	public void changePasswordDialog(String password)
	{
		oldpassword = password;
		setSize(300, 150);
		setTitle("Change Password");
		contentPane = getContentPane( );
        contentPane.setLayout(new BorderLayout( ));
        JFrame myWindow = new JFrame();
        myWindow.getContentPane().setLayout(new BorderLayout());
        addWindowListener(new WindowCloser(myWindow));
		
		JLabel password1label = new JLabel("Password:");
		JLabel password2label = new JLabel("Confirm Password:");
		
		
		password1field = new TextField(20);
		password2field = new TextField(20);
		
		password2field.setEchoChar('*');
		password1field.setEchoChar('*');
		
		password1field.addActionListener(this);
		password2field.addActionListener(this);
		
		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		contentPane.add(panel);
		
		
		JPanel west_Panel = new JPanel();
		west_Panel.setLayout(new GridLayout(3,1));
		panel.add(west_Panel,BorderLayout.WEST);
		
		west_Panel.add(password1label);
		west_Panel.add(password2label);
		
		JPanel east_Panel = new JPanel();
		east_Panel.setLayout(new GridLayout(3,2));
		panel.add(east_Panel,BorderLayout.EAST);
		
		JPanel flow_panel1 = new JPanel();
		JPanel flow_panel2 = new JPanel();
		
		east_Panel.add(flow_panel1);
		east_Panel.add(flow_panel2);
		
		flow_panel1.add(password1field );
		flow_panel2.add(password2field);
		
		JPanel buttonflow = new JPanel();
		buttonflow.setLayout(new FlowLayout());
		panel.add(buttonflow,BorderLayout.SOUTH);
		
		JButton changepasswordbutton = new JButton("Change Password");
		changepasswordbutton.addActionListener(this);
		buttonflow.add(changepasswordbutton);
		contentPane.validate();
		this.setVisible(true);
	}
	
	public void showMyWorkorders()
	{
		num_open_clicked++;
		if(num_open_clicked > 1)
		{
			panel1.remove(numberopenlabel);
			flow_panel1.remove(scrollpane1);
		}
			
		//send a request to the server to send the pending open work orders.
		String message = "028:";
		messagets.putMessage(message);
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
		numberopenlabel = new JLabel("Number of open work orders: " + X);
		panel1.add(numberopenlabel,BorderLayout.SOUTH);
		//don't put anything here because the finel message from 23 is get below 
		//(the number 23 is soo cursed. I've had more trouble with that call than any other).
		//----------------------------------------------------------------------
		//Column name stuff
		//split up the column names and put it in this array of objects below.
		String raw_columnNames = messagefs.getMessage().toString();
		Object[] columnNames = raw_columnNames.split(",");
		//------------------------------------------------------------------
		workorders = refineDataForTable(workorders);
		tm = new DefaultTableModel() {public boolean isCellEditable(int rowIndex, int mColIndex) {return false;}}; 
		tm.setDataVector(workorders,columnNames);
		
		table = new JTable(tm);
		table.addMouseListener(new WOOpenDetailsMouseAdapter());
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setAutoCreateRowSorter(true);
		
		//-------------------------------------------------
		//changes the specified column to be x pixels wide.
	    int vColIndex = 2;
	    TableColumn col = table.getColumnModel().getColumn(vColIndex);
	    int width = 132;
	    col.setPreferredWidth(width);
		//-------------------------------------------------
		
		scrollpane1 = new JScrollPane(table);
		
		flow_panel1 = new JPanel();
		flow_panel1.setBackground(bgColor);
		flow_panel1.setLayout(new FlowLayout());
		flow_panel1.add(scrollpane1);
		panel1.add(flow_panel1,BorderLayout.CENTER);

		
		
		scrollpane1.setPreferredSize(new Dimension(600, 500));
		panel1.validate();
		
		//little extra thing here to show the current number of open workorders.
		numberopenlabel = new JLabel("Number of open work orders: " + X);
		panel1.add(numberopenlabel,BorderLayout.SOUTH);
	}
	public void showDefaultOpen()
	{
		num_open_clicked++;
		if(num_open_clicked > 1)
		{
			panel1.remove(numberopenlabel);
			flow_panel1.remove(scrollpane1);
		}
			
		//send a request to the server to send the pending open work orders.
		String message = "023:";
		messagets.putMessage(message);
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
		numberopenlabel = new JLabel("Number of open work orders: " + X);
		panel1.add(numberopenlabel,BorderLayout.SOUTH);
		//don't put anything here because the finel message from 23 is get below 
		//(the number 23 is soo cursed. I've had more trouble with that call than any other).
		//----------------------------------------------------------------------
		//Column name stuff
		//split up the column names and put it in this array of objects below.
		String raw_columnNames = messagefs.getMessage().toString();
		Object[] columnNames = raw_columnNames.split(",");
		//------------------------------------------------------------------
		workorders = refineDataForTable(workorders);
		tm = new DefaultTableModel() {public boolean isCellEditable(int rowIndex, int mColIndex) {return false;}}; 
		tm.setDataVector(workorders,columnNames);
		
		table = new JTable(tm);
		table.addMouseListener(new WOOpenDetailsMouseAdapter());
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setAutoCreateRowSorter(true);
		
		//-------------------------------------------------
		//changes the specified column to be x pixels wide.
	    int vColIndex = 0;
	    TableColumn col = table.getColumnModel().getColumn(vColIndex);
	    int width = 50;
	    col.setPreferredWidth(width);
		//-------------------------------------------------
		//changes the specified column to be x pixels wide.
	    vColIndex = 1;
	    col = table.getColumnModel().getColumn(vColIndex);
	    width = 50;
	    col.setPreferredWidth(width);
	    //-------------------------------------------------
		//changes the specified column to be x pixels wide.
	   	vColIndex = 2;
	   	col = table.getColumnModel().getColumn(vColIndex);
	    width = 75;
	    col.setPreferredWidth(width);
		//-------------------------------------------------
		//changes the specified column to be x pixels wide.
	    vColIndex = 3;
	    col = table.getColumnModel().getColumn(vColIndex);
	    width = 150;
	    col.setPreferredWidth(width);
	    //-------------------------------------------------
		
		scrollpane1 = new JScrollPane(table);
		
		flow_panel1 = new JPanel();
		flow_panel1.setBackground(bgColor);
		flow_panel1.setLayout(new FlowLayout());
		flow_panel1.add(scrollpane1);
		panel1.add(flow_panel1,BorderLayout.CENTER);

		
		
		scrollpane1.setPreferredSize(new Dimension(600, 500));
		panel1.validate();
		
		//little extra thing here to show the current number of open workorders.
		numberopenlabel = new JLabel("Number of open work orders: " + X);
		numberopenlabel.setForeground(txtColor);
		panel1.add(numberopenlabel,BorderLayout.SOUTH);
	}
	public void showPendingOpen()
	{
		num_pendingopen_clicked++;
		if(num_pendingopen_clicked > 1)
		{
			flow_panel2.remove(scrollpane2);
		}
		//send a request to the server to send the pending open work orders.
		String message = "004:";
		messagets.putMessage(message);
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
		//----------------------------------------------------------------------
		//Column name stuff
		//split up the column names and put it in this array of objects below.
		String raw_columnNames = messagefs.getMessage().toString();
		Object[] columnNames = raw_columnNames.split(",");
		//-----------------------------------------------------------------
		//sets up the pending table
		workorders = refineDataForTable(workorders);
		pendingtm = new DefaultTableModel() {public boolean isCellEditable(int rowIndex, int mColIndex) {return false;}}; 
		pendingtm.setDataVector(workorders,columnNames);
		
		pendingopentable = new JTable(pendingtm);
		pendingopentable.addMouseListener(new WOPendingOpenMouseAdapter());
		
		pendingopentable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		pendingopentable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		pendingopentable.setAutoCreateRowSorter(true);
		//-------------------------------------------------
		//changes the specified column to be x pixels wide.
	    int vColIndex = 2;
	    TableColumn col = pendingopentable.getColumnModel().getColumn(vColIndex);
	    int width = 132;
	    col.setPreferredWidth(width);
		//-------------------------------------------------
		
		scrollpane2 = new JScrollPane(pendingopentable);
		
		//ask which way they want: with or without this panel
		flow_panel2 = new JPanel();
		flow_panel2.setBackground(bgColor);
		flow_panel2.setLayout(new FlowLayout());
		flow_panel2.add(scrollpane2);
		panel2.add(flow_panel2,BorderLayout.CENTER);
		
		scrollpane2.setPreferredSize(new Dimension(600, 500));
		
		panel2.validate();
	}
	
	public void showPendingClosed()
	{
		num_pendingclosed_clicked++;
		if(num_pendingclosed_clicked > 1)
		{
			flow_panel3.remove(scrollpane3);
		}
		//-------------------------------------------------------------------
		//send a request to the server to send the pending closed work orders
		String message = "005:";
		messagets.putMessage(message);
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
		
		//----------------------------------------------------------------------
		//Column name stuff
		//split up the column names and put it in this array of objects below.
		String raw_columnNames = messagefs.getMessage().toString();
		Object[] columnNames = raw_columnNames.split(",");
		//----------------------------------------------------------------------
		//sets up the pending table
		workorders = refineDataForTable(workorders);
		pendingtm = new DefaultTableModel() {public boolean isCellEditable(int rowIndex, int mColIndex) {return false;}}; 
		pendingtm.setDataVector(workorders,columnNames);
		
		pendingclosetable = new JTable(pendingtm);
		pendingclosetable.addMouseListener(new WOPendingCloseMouseAdapter());
		pendingclosetable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		pendingclosetable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		pendingclosetable.setAutoCreateRowSorter(true);
		//-------------------------------------------------
		//changes the specified column to be x pixels wide.
	    int vColIndex = 2;
	    TableColumn col = pendingclosetable.getColumnModel().getColumn(vColIndex);
	    int width = 132;
	    col.setPreferredWidth(width);
		//-------------------------------------------------
		
		scrollpane3 = new JScrollPane(pendingclosetable);
		
		//ask which way they want: with or without this panel
		flow_panel3 = new JPanel();
		flow_panel3.setBackground(bgColor);
		flow_panel3.setLayout(new FlowLayout());
		flow_panel3.add(scrollpane3);
		panel3.add(flow_panel3,BorderLayout.CENTER);
		
		scrollpane3.setPreferredSize(new Dimension(600, 500));
		panel3.validate();
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
	
	public void setVisibleRowCount(JTable table, int rows)
	{ 
	    int height = 0; 
	    for(int row=0; row<rows; row++) 
	        height += table.getRowHeight(row); 
	 
	    table.setPreferredScrollableViewportSize(new Dimension( 
	            table.getPreferredScrollableViewportSize().width, height )); 
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
		Object refinedobject[][] = new Object[X][Y];
		
		//----------------------------------------------------------------------
		//gets the information from the server
		String message = "050:"; //gets a list of sites
		messagets.putMessage(message);
		String data = messagefs.getMessage().toString();
		System.out.println("Data"+data);
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
		//
		for(int c = 0; c < X; c++)
		{
			//look for the site ID
			for(int f = 0; f < sites.length; f++)
			{
				if(object[c][4].equals(sites[f][0]))
				{
					//replace the ID with the number
					object[c][4] = sites[f][1];
					break;//abort the loop.
				}
			}
		}
		//---------------------------------------------------------------------
		//change the assigned_to value from the index to a user name.
		message = "047:"; //gets a list of sites
		messagets.putMessage(message);
		data = messagefs.getMessage().toString();
		X2 = Integer.parseInt(data.substring(0,data.indexOf(",")));
		Y2 = Integer.parseInt(data.substring(data.indexOf(",") +1,data.length()));
		Object users[][] = new Object[X2][Y2];
		for(int c = 0; c < X2; c++)
		{
			for(int d = 0; d < Y2; d++)
			{
				users[c][d] = messagefs.getMessage();
			}
		}
		//----------------------------------------------------------------------
		
		for(int c = 0; c < X; c++)
		{
			for(int f = 0; f < users.length; f++)
			{
				if(object[c][7].equals(users[f][0]))
				{
					object[c][7] = users[f][1];
				}
			}
		}
		
		
		return object;
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
			if(tabPanel.getSelectedIndex() == 0)
			{
				switch(namescounter)
				{
					case 0:
						currenttoken = " WO #";
						break;
					case 1:
						currenttoken = "ECISD #";
						break;
					case 2:
						currenttoken = "Problem";
						break;
					case 3:
						currenttoken = "Campus";
						break;
					case 4:
						currenttoken = "Date Open";
						break;
					case 5:
						currenttoken = "Room Number";
						break;
					case 6:
						currenttoken = "Assigned To";
						break;
				}
				switch(namescounter)
				{
					case 0:
						new_columnNames = new_columnNames + currenttoken;
						break;
					default:
						new_columnNames = new_columnNames + "," + currenttoken;
					break;
				}
			}
			//appears this way for tabPanel 2
			else if(tabPanel.getSelectedIndex() == 2)
			{
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
					case 18:
						currenttoken ="E-mail Sent?";
					
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
					case 18:
						new_columnNames = new_columnNames + "," + currenttoken;
					break;
				}
			}
			//this is the default view.
			else
			{
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
			}
			//------------------------------------------------------------------
			
			
			namescounter++;
		}
		return new_columnNames;
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
	//these are the mouse adapters for the tables. This way when the user double clicks
	//on a table, the right window is launched (depends on the tab).
	
	class WOHistoryMouseAdapter extends MouseAdapter
	{
		public void mouseClicked(MouseEvent e)
		{
            if (e.getClickCount() >= 2) 
            {
            	try
            	{
            		int workordernumberselected = Integer.parseInt(historytable.getValueAt(historytable.getSelectedRow(),0).toString());
                	WODetails object = new WODetails(messagets,messagefs,userlevel,workordernumberselected,4);
            	}
            	catch(Exception f)
            	{
            		//an exception may occur where an index above is -1. Not quite sure why.....
            		JOptionPane.showMessageDialog(null,"Error: The program cannot read your selection. \n Please restart the application if the problem persists ");
            	}
                
            }
		}
	}
	class WOPendingOpenMouseAdapter extends MouseAdapter
	{
		public void mouseClicked(MouseEvent e)
		{
            if (e.getClickCount() >= 2) 
            {
            	try
            	{
            		int workordernumberselected = Integer.parseInt(pendingopentable.getValueAt(pendingopentable.getSelectedRow(),0).toString());
            		//int workordernumberselected = pendingopentable_workordernums[pendingopentable.getSelectedRow()];
                	WODetails object = new WODetails(messagets,messagefs,userlevel,workordernumberselected,0);
            	}
            	catch(Exception f)
            	{
            		JOptionPane.showMessageDialog(null,"Error: The program cannot read your selection. \n Please restart the application if the problem persists ");
            	}
                
            }
		}
	}
	class WOPendingCloseMouseAdapter extends MouseAdapter
	{
		public void mouseClicked(MouseEvent e)
		{
            if (e.getClickCount() >= 2) 
            {
            	int workordernumberselected;
            	try
            	{
            	workordernumberselected = Integer.parseInt(pendingclosetable.getValueAt(pendingclosetable.getSelectedRow(),0).toString());
            		System.out.println("wonum" + workordernumberselected);
            	//	int workordernumberselected = pendingclosetable_workordernums[pendingclosetable.getSelectedRow()];
                	
            	}
            	catch(Exception f)
            	{
            		workordernumberselected = 0;
            		JOptionPane.showMessageDialog(null,"Error: The program cannot read your selection. \n Please restart the application if the problem persists ");
            	}
                WODetails object = new WODetails(messagets,messagefs,userlevel,workordernumberselected,1);
            }
		}
	}
	class WOOpenDetailsMouseAdapter extends MouseAdapter
	{
		public void mouseClicked(MouseEvent e)
		{
            if (e.getClickCount() >= 2) 
            {
            	int workordernumberselected = 0;
            	try
            	{
            		workordernumberselected = Integer.parseInt(table.getValueAt(table.getSelectedRow(),0).toString());
                	
            	}
                catch(Exception f)
                {
                	JOptionPane.showMessageDialog(null,"Error: The program cannot read your selection. \n Please restart the application if the problem persists:" + f);
                }
                WODetails object = new WODetails(messagets,messagefs,userlevel,workordernumberselected,3);
            }
		}
	}
	
	//handles client server communication
	class ToServerThread extends Thread
	{
		ObjectOutputStream out;
		Socket s;
		TransferObject message;
		
		public ToServerThread(ObjectOutputStream out,Socket s, TransferObject c)
		{
			message = c;
			this.out = out;
			this.s = s;
		}
		
		public void run()
		{
			try
			{
				while(true)
				{
					out = new ObjectOutputStream(s.getOutputStream());
					Object data = message.getMessage();
					out.writeObject(data);
				}	
			}
			
			catch(IOException e) 
			{
				System.out.println("IO:" + e.getMessage());
			}
	
		}
	}
	//handles client server communication
	class FromServerThreadObject extends Thread
	{
		ObjectInputStream in;
		Socket s;
		TransferObject message;
		int client_num;
		
		public FromServerThreadObject(ObjectInputStream in,Socket s, TransferObject c)
		{
			message = c;
			this.in = in;
			this.s = s;	
		}
		public void run()
		{
			Object data;
			try
			{
				while(true)
				{
					in = new ObjectInputStream(s.getInputStream());
					data = in.readObject();
					message.putMessage(data); //puts the message in the Transfer agent	
				}
			}
			catch(ClassNotFoundException e)
			{
				System.out.println("CNF:" + e.getMessage());
			}
			catch(IOException e) 
			{
				System.out.println("IO:" + e.getMessage());
			}
		}
	}	
}