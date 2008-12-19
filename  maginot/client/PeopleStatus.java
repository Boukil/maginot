import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.TableColumn;
import javax.swing.table.DefaultTableModel;
import java.util.StringTokenizer;


public class PeopleStatus extends JFrame implements ActionListener
{
	public static final int WIDTH = 520;
    public static final int HEIGHT = 520;
    TransferObject messagets,messagefs;
    Color bgColor,txtColor;
     
	public PeopleStatus(TransferObject messagets, TransferObject messagefs)
	{
		this.messagets = messagets;
		this.messagefs = messagefs;
		
		getColors();
		
		setSize(WIDTH, HEIGHT);
        setTitle("People Status");
        Container contentPane = getContentPane( );
        contentPane.setLayout(new BorderLayout( ));

       
       	//requests the status table from the server.
       	messagets.putMessage("081:status");
		String data = messagefs.getMessage().toString();
		
		int X = Integer.parseInt(data.substring(0,data.indexOf(",")));
		int Y = Integer.parseInt(data.substring(data.indexOf(",") +1,data.length()));
		
		Object tabledata[][] = new Object[X][Y];
		for(int c = 0; c < X; c++)
		{
			for(int d = 0; d < Y; d++)
			{
				tabledata[c][d] = messagefs.getMessage();
			}
		}
		
		//convert the UIDs in the table to UserNames
		for(int c = 0; c< X; c++)
		{
			int d = Integer.parseInt(tabledata[c][0].toString());
			messagets.putMessage("003:"+d);
			tabledata[c][0] = messagefs.getMessage();
			//converts the Campus portion
			if(tabledata[c][1] != null)
			{
				d = Integer.parseInt(tabledata[c][1].toString());
				messagets.putMessage("055:" + d);
				tabledata[c][1] = messagefs.getMessage();
			}
			
			if(tabledata[c][2] != null)
			{
				if(tabledata[c][2].toString().equals("null"))
				{
					tabledata[c][2] = " ";
				}
			}
			
			if(tabledata[c][3] != null)
			{
				if(Integer.parseInt(tabledata[c][3].toString()) == 0)
				{
					tabledata[c][3] = "In";
				}
				else if(Integer.parseInt(tabledata[c][3].toString()) == 1)
				{
					tabledata[c][3] = "Out";
				}
			}
		}
		
		//----------------------------------------------------------------------
		//Column name stuff
		messagets.putMessage("121:"); //request the columnnames from the server
		String raw_columnNames = refineColumnNames(messagefs.getMessage().toString());//get the columnname message from the server
		//String raw_columnNames = messagefs.getMessage().toString();
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
       	DefaultTableModel tm = new DefaultTableModel() {public boolean isCellEditable(int rowIndex, int mColIndex) {return false;}}; 
		tm.setDataVector(tabledata,columnNames);
		
		JTable table = new JTable(tm);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setAutoCreateRowSorter(true);
		
		//-------------------------------------------------
		//changes the specified column to be x pixels wide.
	    int vColIndex = 2;
	    TableColumn col = table.getColumnModel().getColumn(vColIndex);
	    int width = 130;
	    col.setPreferredWidth(width);
		//-------------------------------------------------
		
		//-------------------------------------------------
		//changes the specified column to be x pixels wide.
	    vColIndex = 1;
	   	col = table.getColumnModel().getColumn(vColIndex);
	    width = 130;
	    col.setPreferredWidth(width);
		//-------------------------------------------------
		
		//-------------------------------------------------
		//changes the specified column to be x pixels wide.
	    vColIndex = 0;
	   	col = table.getColumnModel().getColumn(vColIndex);
	    width = 115;
	    col.setPreferredWidth(width);
		//-------------------------------------------------

       	
       	contentPane.add(table,BorderLayout.CENTER);
       	
       	JScrollPane scrollpane1 = new JScrollPane(table);
       	scrollpane1.setPreferredSize(new Dimension(453, 440));
		
		JPanel flow_panel1 = new JPanel();
		flow_panel1.setBackground(bgColor);
		flow_panel1.setLayout(new FlowLayout());
		flow_panel1.add(scrollpane1);
		contentPane.add(flow_panel1,BorderLayout.CENTER);
       
       	JPanel flow_panel2 = new JPanel();
       	flow_panel2.setBackground(bgColor);
       	flow_panel2.setLayout(new FlowLayout());
       	contentPane.add(flow_panel2,BorderLayout.SOUTH);
       	
       	JButton saveButton = new JButton("Save Changes");
       	saveButton.setForeground(txtColor);
       	saveButton.addActionListener(this);
       //	flow_panel2.add(saveButton);
       	
       	JButton updateButton = new JButton("Update");
       	updateButton.setForeground(txtColor);
       	updateButton.addActionListener(this);
       	//flow_panel2.add(updateButton);
       
		this.setVisible(true);
	}

	public void actionPerformed(ActionEvent e)
	{
		String actionCommand = e.getActionCommand( );
		
		if(actionCommand.equals("Save"))
		{
			System.out.println("Save Changes");
		}
		if(actionCommand.equals("Update"))
		{
			
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
						currenttoken = "Name";
						break;
					case 1:
						currenttoken = "Campus";
						break;
					case 2:
						currenttoken = "Notes";
						break;
					case 3:
						currenttoken = "Status";
						break;

				}
					
				
				switch(namescounter)
				{
					case 0:
						new_columnNames = new_columnNames + currenttoken;
						break;
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
}