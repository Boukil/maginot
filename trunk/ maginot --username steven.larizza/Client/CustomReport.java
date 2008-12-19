import javax.swing.*;
import java.awt.*;
import javax.swing.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.StringTokenizer;
import javax.swing.table.DefaultTableModel;

public class CustomReport extends JFrame implements ActionListener
{
	private Container contentPane;
	
	private JPanel panel,panel5;
	
	 TransferObject messagefs,messagets;
	
	TextField queryfield; 
		
	JScrollPane scrollpane1;
	JTable table;
	DefaultTableModel tm;
	
	Object result[][];
	
	int num_clicked = 0;
	
	public CustomReport(TransferObject messagets, TransferObject messagefs)
	{   
		this.messagets = messagets;
		this.messagefs = messagefs;
		
		setSize(600,500);
		setResizable(false);
		setTitle("Custom SQL Query - Maginot Client");
		contentPane = getContentPane( );
		contentPane.setLayout(new BorderLayout( ));
		
		JFrame myWindow = new JFrame();
		myWindow.getContentPane().setLayout(new BorderLayout());
		addWindowListener(new WindowCloser(myWindow));
		
		//--------------------------------------------------------------------
		panel5 = new JPanel();
        panel5.setLayout(new BorderLayout());
        contentPane.add(panel5,BorderLayout.NORTH);
        
        JPanel north_panel5 = new JPanel();
        north_panel5.setLayout(new GridLayout(1,2));
        panel5.add(north_panel5,BorderLayout.NORTH);
        
        JPanel flows[] = new JPanel[1];
        
        for(int c = 0; c < 1; c++)
        {
        	flows[c] = new JPanel();
        	flows[c].setLayout(new FlowLayout());
        	north_panel5.add(flows[c]);
        }
        
        JLabel querylabel = new JLabel("SQL Query:");
        queryfield = new TextField(20);
        queryfield.addActionListener(this);
        flows[0].add(querylabel);
        flows[0].add(queryfield);

		//--------------------------------------------------------------------
		JPanel south_panel = new JPanel();
		south_panel.setLayout(new FlowLayout());
		contentPane.add(south_panel,BorderLayout.SOUTH);
		
		
		JButton newbutton = new JButton("Run Query");
		south_panel.add(newbutton);
		newbutton.addActionListener(this);

		this.setVisible(true);
	}
	

	public void actionPerformed(ActionEvent e)
	{
		String actionCommand = e.getActionCommand();
		
		if((actionCommand.equals("Run Query")) || (queryfield == e.getSource()))
		{
			//----------------------------------------------------------------------
			//Column name stuff
			messagets.putMessage("001:"); //request the columnnames from the server
			String raw_columnNames = messagefs.getMessage().toString();//get the columnname message from the server
	
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
			//make sure an update, insert, or drop command is not about to run.
			String query = queryfield.getText();
			
			StringTokenizer token = new StringTokenizer(query," ");
			boolean execute_statement = true;
			for(int c = 0; c < token.countTokens(); c++)
			{
				String current_token = token.nextToken();
				if((current_token.equalsIgnoreCase("DROP")) || (current_token.equalsIgnoreCase("INSERT")) || (current_token.equalsIgnoreCase("UPDATE")))
				{
					execute_statement=false;
					queryfield.setText("");
					JOptionPane.showMessageDialog(null,"Do not attempt to make changes to the database here.\nYou may only use the SELECT query here.");
					break;
				}
			}
			if(num_clicked >= 1)
			{
				execute_statement = false;
				JOptionPane.showMessageDialog(null,"You have already executed a query. Please close the window and open another to\nexecute another query.");
			}
			
			if(execute_statement == true)
			{
				num_clicked++;
				messagets.putMessage("019:" + query);
				String data = messagefs.getMessage().toString();
				

				
				
	
				int X = Integer.parseInt(data.substring(0,data.indexOf(",")));
				int Y = Integer.parseInt(data.substring(data.indexOf(",") +1,data.length()));
				result = new Object[X][Y];
				
				for(int c = 0; c < X; c++)
				{
					for(int d = 0; d < Y; d++)
					{
						result[c][d] = messagefs.getMessage();
					}
				}
				
				//sets up the pending table
				tm = new DefaultTableModel() {public boolean isCellEditable(int rowIndex, int mColIndex) {return false;}}; 
				tm.setDataVector(result,columnNames);
				
				table = new JTable(tm);
				table.addMouseListener(new WOHistoryMouseAdapter());
				table.setAutoCreateRowSorter(true);
				table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
				
				JScrollPane scrollpane = new JScrollPane(table);
				
				contentPane.add(scrollpane,BorderLayout.CENTER);
				contentPane.validate();
				queryfield.setText("");
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

	class WOHistoryMouseAdapter extends MouseAdapter
	{
		public void mouseClicked(MouseEvent e)
		{
            if (e.getClickCount() >= 2) 
            {
            	try
            	{
            		int workordernumberselected = Integer.parseInt(table.getValueAt(table.getSelectedRow(),0).toString());
               		WODetails object = new WODetails(messagets,messagefs,0,workordernumberselected,4);
            	}
            	catch(Exception f)
            	{
            		JOptionPane.showMessageDialog(null,"Error: The program cannot read your selection. \n Please restart the application if the problem persists ");
            	}
            }
		}
	}
}