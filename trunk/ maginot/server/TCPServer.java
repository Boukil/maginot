import java.net.*;
import java.io.*;
import java.sql.*;
import java.awt.Color;
import java.util.StringTokenizer;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;


public class TCPServer
{
	public static void main (String args[])
	{
		//this section sets up the connections, and maintains them. 
		FromClientThread commfc[] = new FromClientThread[2];
		ToClientThreadObject commtco[] = new ToClientThreadObject[2];
		
		try
		{
			int serverPort = 3000;
			ServerSocket listenSocket = new ServerSocket(serverPort);
			int count = 0;
			
			while(true) //always listen for a connection.
			{
				System.out.println("Listening:");
				Socket clientSocket = listenSocket.accept();
				
				 // I don't think we need to keep all these in an array
				TransferObject messagefc = new TransferObject();
				TransferObject messagetco = new TransferObject();
				
				ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
				
				ObjectOutputStream outo = new ObjectOutputStream(clientSocket.getOutputStream());
				
				//extend the arrays if needed
				if(commtco.length == count)
				{
					FromClientThread temp_fct[] = new FromClientThread[commfc.length];
					ToClientThreadObject temp_tcto[] = new ToClientThreadObject[commtco.length];
					
					temp_fct = commfc;	
					temp_tcto = commtco;
					
					commfc = new FromClientThread[temp_fct.length *2];
					commtco = new ToClientThreadObject[temp_tcto.length*2];
					
					for(int c = 0; c < temp_fct.length; c++)
					{
						commfc[c] = temp_fct[c];
						commtco[c] = temp_tcto[c];
					}
				}
				commfc[count] = new FromClientThread(in, clientSocket,messagefc,count);
				commfc[count].start();
				
				commtco[count] = new ToClientThreadObject(outo,clientSocket,messagetco,count);
				commtco[count].start();
				
				Operations control = new Operations(clientSocket,messagetco,messagefc,count,commfc[count],commtco[count]);
				
				
				count++;
			}
		}
		catch(IOException e) {System.out.println("Listen:" + e.getMessage());}
	}
}

class FromClientThread extends Thread
{
	ObjectInputStream in;
	Socket s;
	TransferObject message;
	int client_num;
	
	public FromClientThread(ObjectInputStream in,Socket s, TransferObject c, int client_num)
	{
		message = c;
		this.client_num = client_num;
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

class ToClientThreadObject extends Thread
{
	ObjectOutputStream out;
	Socket s;
	TransferObject message;
	int client_num;
	
	public ToClientThreadObject(ObjectOutputStream out,Socket s, TransferObject c,int client_num)
	{
		message = c;
		this.client_num = client_num;
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
} //this thread sends objects

class Operations extends Thread
{
	private final double versionnum = 2.96;
	String data;
	TransferObject messagetco,messagefc;
	//InstructionTransfer instructionmessenger, returnmessage;
	int client_num;
	boolean authenticated = false;
	String campus = "ZZZ";
	String connecteduser = "ZZZ";
	int instruction_code;
	Socket clientSocket;
	
	//this way this thread can control its communication threads. And kill them when it needs to do so.
	FromClientThread commfc;
	ToClientThreadObject commtco;
	
	
	public Operations(Socket clientSocket, TransferObject messagetco, TransferObject messagefc, int client_num,FromClientThread commfc,ToClientThreadObject commtco)
	{
		this.commfc = commfc;
		this.commtco = commtco;
		
		this.clientSocket = clientSocket;
		this.client_num = client_num;
		this.messagetco = messagetco;
		this.messagefc = messagefc;
		this.start();
	}
	
	public void run()
	{
		//the server responds to the client, not the other way around. 
		//so lets send a message to the sever, and wait for a response. 
		while(true)
		{
			String data = messagefc.getMessage().toString(); //gets the message
			
			//figures out the instruction code.
			try
			{
				instruction_code = Integer.parseInt(data.substring(0,data.indexOf(":")));
			}
			catch(Exception e)
			{
				System.out.println("Error: unknown instruction code");
				instruction_code = 0;
			}
			
			if(instruction_code == 27) //instruction to kill the threads.
			{
				try
				{
					commfc.in.close();
					commtco.out.close();
					commtco.join(100);
					commfc.join(100);
					System.out.println("Join Successful");
				}
				catch(InterruptedException f)
				{
					System.out.println("Interrupted Exception joining threads");
				}
				catch(IOException f)
				{
					System.out.println("IOException closing DataStream");
				}
				break;
			}
			
			if((instruction_code == 22) && (authenticated == false))//checks if the user is authenticated or if it is an authentication request.
			{
				System.out.println("data" + data);
				authenticate(data);
			}
			
			//-----------------------------------------------------------------------------------------------------
			/* instruction code 26 is put below to avoid having to setup another connection to the sql server. 
			 * by putting it here it can use the same connection it has always used.
			 */
			else if((authenticated) || (instruction_code == 26)) //26 so it can authenticate domain users. 
			{
				//these instruction codes restore and backup the database. They occur too fast and lock up the server 
				// because the print lines can't keep up.
				if((instruction_code != 81) && (instruction_code != 82) && (instruction_code != 83) && (instruction_code != 84))
				{
					System.out.println("Request$" + data + "$ " + "From:" + connecteduser);
				}
					
				
				//remove the instructioncode from the string		
				data = data.substring(data.indexOf(":")+1,data.length());
				
				//set up the connection to the sql server.
				String sql;
				//connects to the database. 
				Connection connection = null;
			    try 
			    {
			    	if(instruction_code == 26)
			    	{
			    		ensureMethods();
			    	}
			        // Load the JDBC driver
			        Class.forName("org.gjt.mm.mysql.Driver");
			        
			        // Create a connection to the database
			        String serverName = "localhost";
			        String mydatabase = "Maginot";
			        
			        String url = "jdbc:mysql://" + serverName +  "/" + mydatabase; // a JDBC url
			        String username = "maginot"; //SQL username
			        String password = "dell001"; //SQL password for the above username.
			        connection = DriverManager.getConnection(url, username, password);
			        Statement stmt = connection.createStatement();
			        PreparedStatement pstmt;
			        
			        
			        //make sure the students table in this database exists. create it if it doesn't
					sql = "CREATE TABLE IF NOT EXISTS workorders (work_order_number INTEGER NOT NULL PRIMARY KEY, asset_number INTEGER, status INTEGER NOT NULL, problem VARCHAR(10000) NOT NULL, resolution VARCHAR(10000), campus INTEGER NOT NULL, opened_by INTEGER NOT NULL, closed_by INTEGER,model VARCHAR(500),date_open VARCHAR(20) NOT NULL, date_closed VARCHAR(20),first_name VARCHAR(200) NOT NULL, last_name VARCHAR(200) NOT NULL, room_number VARCHAR(50), phone VARCHAR(100), priority INTEGER, service_tag VARCHAR(30),assigned_to VARCHAR(30) NOT NULL,follow_up_sent boolean,date_follow_up_sent VARCHAR(30),time_to_completion VARCHAR(30));";			        
			        pstmt = connection.prepareStatement(sql);
			        pstmt.executeUpdate();
			        
			        //----------------------------------------------------------
			        //variables for the switch below.
			        ResultSet rs;
			        ResultSetMetaData md;
			        int nColumns;
			        StringBuffer columnNamesBuffer;
			        String columns_selected = ""; //not part of the SQL statement itself, but this is the variable sent to the client to tell it what columns were selected.
			        Object object[][];
			        
			        switch(instruction_code)
					{
						case 1: //send the metadata from the table workorders.
							sql = "SELECT * FROM workorders where status=1;";	
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();		
							md = rs.getMetaData();
	        
					        nColumns = md.getColumnCount();
					        columnNamesBuffer = new StringBuffer();
							for(int i=1;i<=nColumns;i++)
							{
								if(i==1)
									columnNamesBuffer.append(md.getColumnLabel(i));
								else
								{
									columnNamesBuffer.append(",");
									columnNamesBuffer.append(md.getColumnLabel(i));
								}	
							}
							messagetco.putMessage(columnNamesBuffer.toString());
						break;
						case 2: //send a copy of the open workorders for this campus
							sql = "SELECT work_order_number,priority,asset_number,problem,campus,date_open,room_number,assigned_to FROM workorders WHERE status=1 AND campus='"+data+"';";	
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							
							md = rs.getMetaData();
					        nColumns = md.getColumnCount();	
					        object = resultSetToObject(rs);
					        
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
					        
					        String xy = X + "," + Y; 
					        messagetco.putMessage(xy); //this is here so the client knows how many objects to expect.
					        for(int c = 0; c < X; c++)
					        {
					        	for(int d = 0; d < Y; d++)
					        	{
					        		messagetco.putMessage(object[c][d]);
					        	}
					        }
					       columns_selected = "WO #,Priority,Asset #,Problem,Campus,Date Open, Room #, Assigned To";
					       messagetco.putMessage(columns_selected);
						break;
						case 3: //translate UID into username
							sql = "SELECT user_name FROM users WHERE user_id=" + data +";";
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							
							String name = "";
							while(rs.next())
							{
								name =  rs.getString(1);
							}
							messagetco.putMessage(name);
							
						break;
						case 4: //send the pending open work orders
							sql = "SELECT work_order_number,priority,asset_number,problem,campus,date_open,room_number,assigned_to FROM workorders WHERE status=0;";	
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							
							md = rs.getMetaData();
					        nColumns = md.getColumnCount();	
					        object = resultSetToObject(rs);
					        
					        X = object.length;
					        try
					        {
					        	Y = object[0].length;
					        }
					        catch(Exception f)
					        {
					        	Y = 0;
					        }
					        
					        xy = X + "," + Y; 
					        messagetco.putMessage(xy); //this is here so the client knows how many objects to expect.
					        for(int c = 0; c < X; c++)
					        {
					        	for(int d = 0; d < Y; d++)
					        	{
					        		messagetco.putMessage(object[c][d]);
					        	}
					        }
							columns_selected = "WO #,Priority,Asset #,Problem,Campus,Date Open, Room #, Assigned To";
					       	messagetco.putMessage(columns_selected);						
					    break;
						case 5: //send the pending closed work orders
							sql = "SELECT work_order_number,priority,asset_number,problem,campus,date_open,room_number,assigned_to,follow_up_sent FROM workorders WHERE status=2;";
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							
							md = rs.getMetaData();
					        nColumns = md.getColumnCount();	
					        object = resultSetToObject(rs);
					        
					        X = object.length;
					        try
					        {
					        	Y = object[0].length;
					        }
					        catch(Exception f)
					        {
					        	Y = 0;
					        }
					        
					        xy = X + "," + Y; 
					        messagetco.putMessage(xy); //this is here so the client knows how many objects to expect.
					        for(int c = 0; c < X; c++)
					        {
					        	for(int d = 0; d < Y; d++)
					        	{
					        		messagetco.putMessage(object[c][d]);
					        	}
					        }
					        columns_selected = "WO #,Priority,Asset #,Problem,Campus,Date Open, Room #, Assigned To,E-mail Sent?";
					       	messagetco.putMessage(columns_selected);	
						break;
						case 6: //open w/o with a status of 1 (open)
							StringTokenizer tokenizer = new StringTokenizer(data,",");
							
							int wonum,assetnum,status,opened_by,closed_by;
							String problem,resolution,model,date_open,date_closed,firstname,lastname,roomnumber,phone,priority,servicetag,assignedto;
							
							
							wonum = nextWorkOrderNumber();
							
							String temp_asset = tokenizer.nextToken();
							
							if(!temp_asset.equals("null"))
								assetnum = Integer.parseInt(temp_asset);
							else
								assetnum = -1;
								
							status = 1;
							problem = tokenizer.nextToken();
							resolution = tokenizer.nextToken();
							campus = tokenizer.nextToken();
							//opened by solved below
							closed_by = -1;
							model = tokenizer.nextToken();
							
							time timeobject = new time();
							date_open =  timeobject.GetCurrentYear_asInt() + "-" + timeobject.GetCurrentMonth_asInt() + "-" + timeobject.GetCurrentDay_asInt()+ "-" + timeobject.GetMilitaryTime_asString();
							
							date_closed = "null";
							firstname = tokenizer.nextToken();
							lastname = tokenizer.nextToken();
							roomnumber = tokenizer.nextToken();
							phone = tokenizer.nextToken();
							priority = tokenizer.nextToken();
							servicetag = tokenizer.nextToken();
							assignedto = tokenizer.nextToken();
							
							//-----
							//gets the open by
							sql = "SELECT user_id FROM users WHERE user_name='"+connecteduser+ "';";
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							String s = "";
							while (rs.next()) 
							{
								// Get the data from the row using the column index
								s = rs.getString(1);
							}
							opened_by = Integer.parseInt(s); //the current connected user's id
							//-----
							
							if(assetnum == -1)
							{
								sql = "INSERT INTO workorders (work_order_number,asset_number,status,problem,resolution,campus,opened_by,closed_by,model,date_open,date_closed,first_name,last_name,room_number,phone,priority,service_tag,assigned_to) VALUES (" 
									+ wonum +","+ null +"," + status + ",'"+ problem + "'," + "'" + resolution + "','"+ campus + "',"+ opened_by + "," + closed_by +",'"+ model +"','"+ date_open +"','" + date_closed + "','" + firstname +"','"+ lastname +"','"+ roomnumber +"','"+ phone +"',"+ priority + ",'" + servicetag + "','" + assignedto + "');";
								pstmt = connection.prepareStatement(sql);
								pstmt.executeUpdate();
							}
							else
							{
								sql = "INSERT INTO workorders (work_order_number,asset_number,status,problem,resolution,campus,opened_by,closed_by,model,date_open,date_closed,first_name,last_name,room_number,phone,priority,service_tag,assigned_to) VALUES (" 
									+ wonum +","+ assetnum +"," + status + ",'"+ problem + "'," + "'" + resolution + "','"+ campus + "',"+ opened_by + "," + closed_by +",'"+ model +"','"+ date_open +"','" + date_closed + "','" + firstname +"','"+ lastname +"','"+ roomnumber +"','"+ phone +"',"+ priority + ",'" + servicetag + "','" + assignedto + "');";
								pstmt = connection.prepareStatement(sql);
								pstmt.executeUpdate();
							}
							
							
							
						break;
						case 7://open w/o with a status of 4
							tokenizer = new StringTokenizer(data,",");

							wonum = nextWorkOrderNumber();
							temp_asset = tokenizer.nextToken();
							
							if(!temp_asset.equals("null"))
								assetnum = Integer.parseInt(temp_asset);
							else
								assetnum = -1;
							status = 4;
							problem = tokenizer.nextToken();
							resolution = tokenizer.nextToken();
							campus = tokenizer.nextToken();
							//opened by solved below
							closed_by = -1;
							model = tokenizer.nextToken();
							
							timeobject = new time();
							date_open =  timeobject.GetCurrentYear_asInt() + "-" + timeobject.GetCurrentMonth_asInt() + "-" + timeobject.GetCurrentDay_asInt()+ "-" + timeobject.GetMilitaryTime_asString();
							
							date_closed = "null";
							firstname = tokenizer.nextToken();
							lastname = tokenizer.nextToken();
							roomnumber = tokenizer.nextToken();
							phone = tokenizer.nextToken();
							priority = tokenizer.nextToken();
							servicetag = tokenizer.nextToken();
							assignedto = tokenizer.nextToken();
							
							//------------
							//opened by
							sql = "SELECT user_id FROM users WHERE user_name='"+connecteduser+ "';";
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							s = "";
							while (rs.next()) 
							{
								// Get the data from the row using the column index
								s = rs.getString(1);
							}
							opened_by = Integer.parseInt(s); //the current connected user's id
							//-----------------------------
							if(assetnum == -1)
							{
								sql = "INSERT INTO workorders (work_order_number,asset_number,status,problem,resolution,campus,opened_by,closed_by,model,date_open,date_closed,first_name,last_name,room_number,phone,priority,service_tag,assigned_to) VALUES (" + wonum +","+ null +"," + status + ",'"+ problem + "','" + resolution + "',"+ campus + ","+ opened_by + "," + closed_by +",'"+ model +"','"+ date_open +"','" + date_closed + "','" + firstname +"','"+ lastname +"','"+ roomnumber +"','"+ phone +"',"+ priority + ",'" + servicetag + "','" + assignedto + "');";
								pstmt = connection.prepareStatement(sql);
								pstmt.executeUpdate();
							}
							else
							{
								sql = "INSERT INTO workorders (work_order_number,asset_number,status,problem,resolution,campus,opened_by,closed_by,model,date_open,date_closed,first_name,last_name,room_number,phone,priority,service_tag,assigned_to) VALUES (" + wonum +","+ assetnum +"," + status + ",'"+ problem + "','" + resolution + "',"+ campus + ","+ opened_by + "," + closed_by +",'"+ model +"','"+ date_open +"','" + date_closed + "','" + firstname +"','"+ lastname +"','"+ roomnumber +"','"+ phone +"',"+ priority + ",'" + servicetag + "','" + assignedto + "');";
								pstmt = connection.prepareStatement(sql);
								pstmt.executeUpdate();
							}
							
							messagetco.putMessage(Integer.toString(wonum) + "," + date_open);
						break;
						case 8: //close work order to pending status
							tokenizer = new StringTokenizer(data,",");
							
							status = 2;
							firstname = tokenizer.nextToken();
							lastname = tokenizer.nextToken();
							campus = tokenizer.nextToken();
							roomnumber = tokenizer.nextToken();
							priority = tokenizer.nextToken();
							phone = tokenizer.nextToken();
							
							temp_asset = tokenizer.nextToken();
							
							if(!temp_asset.equals("null"))
								assetnum = Integer.parseInt(temp_asset);
							else
								assetnum = -1;
								
							model = tokenizer.nextToken();
							problem = tokenizer.nextToken();
							resolution = tokenizer.nextToken();
							wonum = Integer.parseInt(tokenizer.nextToken());
							servicetag = tokenizer.nextToken();
							assignedto = tokenizer.nextToken();
							
							sql = "SELECT user_id FROM users WHERE user_name='"+connecteduser+ "';"; //gets the ID of the connected user
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							
							s = "";
							while (rs.next()) 
							{
								// Get the data from the row using the column index
								s = rs.getString(1);
							}
							
							closed_by = Integer.parseInt(s); //the current connected user's id
							
							timeobject = new time();
							date_closed =  timeobject.GetCurrentYear_asInt() + "-" + timeobject.GetCurrentMonth_asInt() + "-" + timeobject.GetCurrentDay_asInt()+ "-" + timeobject.GetMilitaryTime_asString();
							
							
							if(assetnum == -1)
							{
								sql = "UPDATE workorders SET asset_number = " + null + " WHERE work_order_number = " + wonum + ";";
								pstmt = connection.prepareStatement(sql);
								pstmt.executeUpdate();
							}
							else
							{
								sql = "UPDATE workorders SET asset_number = " + assetnum + " WHERE work_order_number = " + wonum + ";";
								pstmt = connection.prepareStatement(sql);
								pstmt.executeUpdate();
							}

							sql = "UPDATE workorders SET status = " + status + " WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET problem = '" + problem + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET resolution = '" + resolution + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET campus = '" + campus + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET closed_by = " + closed_by + " WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET model = '" + model + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET date_closed = '" + date_closed + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET first_name = '" + firstname + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET last_name = '" + lastname + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET room_number = '" + roomnumber + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET phone = '" + phone + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET priority = " + priority + " WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET service_tag = '" + servicetag + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET assigned_to = '" + assignedto + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
						break;
						case 9://change this (phone or pending open) W/O to a tech w/o
							tokenizer = new StringTokenizer(data,",");
							
							status = 1;
							firstname = tokenizer.nextToken();
							lastname = tokenizer.nextToken();
							campus = tokenizer.nextToken();
							roomnumber = tokenizer.nextToken();
							priority = tokenizer.nextToken();
							phone = tokenizer.nextToken();
							
							temp_asset = tokenizer.nextToken();
							
							if(!temp_asset.equals("null"))
								assetnum = Integer.parseInt(temp_asset);
							else
								assetnum = -1;
							model = tokenizer.nextToken();
							problem = tokenizer.nextToken();
							resolution = tokenizer.nextToken();
							wonum = Integer.parseInt(tokenizer.nextToken());
							servicetag = tokenizer.nextToken();
							assignedto = tokenizer.nextToken();
							date_open = tokenizer.nextToken();
							
							sql = "SELECT user_id FROM users WHERE user_name='"+connecteduser+ "';"; //gets the ID of the connected user
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							
							s = "";
							while (rs.next()) 
							{
								// Get the data from the row using the column index
								s = rs.getString(1);
							}
							
							if(assetnum == -1)
							{
								sql = "UPDATE workorders SET asset_number = " + null + " WHERE work_order_number = " + wonum + ";";
								pstmt = connection.prepareStatement(sql);
								pstmt.executeUpdate();
							}
							else
							{
								sql = "UPDATE workorders SET asset_number = " + assetnum + " WHERE work_order_number = " + wonum + ";";
								pstmt = connection.prepareStatement(sql);
								pstmt.executeUpdate();
							}

							sql = "UPDATE workorders SET status = " + status + " WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET problem = '" + problem + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET resolution = '" + resolution + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET campus = '" + campus + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET model = '" + model + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET date_open = '" + date_open + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET first_name = '" + firstname + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET last_name = '" + lastname + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET room_number = '" + roomnumber + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET phone = '" + phone + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET priority = " + priority + " WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET service_tag = '" + servicetag + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET assigned_to = '" + assignedto + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET date_open = '" + date_open + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
						break;
						case 10: //send the machine history
							sql = "SELECT * FROM workorders WHERE asset_number= "+ data + ";";
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							
							md = rs.getMetaData();
					        nColumns = md.getColumnCount();	
					        object = resultSetToObject(rs);
					        
					        X = object.length;
					        try
					        {
					        	Y = object[0].length;
					        }
					        catch(Exception f)
					        {
					        	Y = 0;
					        }
					        
					        xy = X + "," + Y; 
					        messagetco.putMessage(xy); //this is here so the client knows how many objects to expect.
					        for(int c = 0; c < X; c++)
					        {
					        	for(int d = 0; d < Y; d++)
					        	{
					        		messagetco.putMessage(object[c][d]);
					        	}
					        }
						break;
						case 11://send the information for a work order
							sql = "SELECT * FROM workorders WHERE work_order_number= " + data + ";";
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							md = rs.getMetaData();
							nColumns = md.getColumnCount();	
							StringBuffer workorderdata = new StringBuffer();
							int wocounter=  0;
							while(rs.next())
							{
								for(int i=1;i<=nColumns;i++)
								{
									workorderdata.append(rs.getString(i));
									workorderdata.append((i==nColumns)?":":",");
								}
								if(wocounter >50)
								{
									break;
								}
								wocounter++;
							}
							try
							{	//this will cause an exception if there is nothing in the string.
								//a slightly ghetto fix, but just remove the last character ";" off of the string before its sent. 
								workorderdata.substring(0,workorderdata.length()-1);
							}
							catch(Exception e)
							{
								System.out.println("String is null...aborting operation.");
							}
							messagetco.putMessage(workorderdata.toString());
						break;
						case 12: //open w/o request from teacher w/ status of 0
							tokenizer = new StringTokenizer(data,",");
							
							wonum = nextWorkOrderNumber();
							assetnum = Integer.parseInt(tokenizer.nextToken());
							status = 0;
							problem = tokenizer.nextToken();
							resolution = tokenizer.nextToken();
							campus = tokenizer.nextToken();
							//opened by solved below
							closed_by = -1;
							model = "null";
							
							timeobject = new time();
							date_open =  timeobject.GetCurrentYear_asInt() + "-" + timeobject.GetCurrentMonth_asInt() + "-" + timeobject.GetCurrentDay_asInt()+ "-" + timeobject.GetMilitaryTime_asString();
							
							date_closed = "null";
							firstname = tokenizer.nextToken();
							lastname = tokenizer.nextToken();
							roomnumber = tokenizer.nextToken();
							phone = tokenizer.nextToken();
							priority = tokenizer.nextToken();
							servicetag = tokenizer.nextToken();
							assignedto = tokenizer.nextToken();
							
							//------------
							//opened by
							sql = "SELECT user_id FROM users WHERE user_name='"+connecteduser+ "';";
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							s = "";
							while (rs.next()) 
							{
								// Get the data from the row using the column index
								s = rs.getString(1);
							}
							opened_by = Integer.parseInt(s); //the current connected user's id
							//-----------------------------
						
						
							sql = "INSERT INTO workorders (work_order_number,asset_number,status,problem,resolution,campus,opened_by,closed_by,model,date_open,date_closed,first_name,last_name,room_number,phone,priority,service_tag,assigned_to) VALUES (" 
									+ wonum +","+ assetnum +"," + status + ",'"+ problem + "'," + "'" + resolution + "','"+ campus + "',"+ opened_by + "," + closed_by +",'"+ model +"','"+ date_open +"','" + date_closed + "','" + firstname +"','"+ lastname +"','"+ roomnumber +"','"+ phone +"',"+ priority + ",'" + servicetag + "','" + assignedto + "');";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
						break;
						case 13://change status to final close
							tokenizer = new StringTokenizer(data,",");
							
							status = 3;
							firstname = tokenizer.nextToken();
							lastname = tokenizer.nextToken();
							campus = tokenizer.nextToken();
							roomnumber = tokenizer.nextToken();
							priority = tokenizer.nextToken();
							phone = tokenizer.nextToken();
							assetnum = Integer.parseInt(tokenizer.nextToken());
							model = tokenizer.nextToken();
							problem = tokenizer.nextToken();
							resolution = tokenizer.nextToken();
							wonum = Integer.parseInt(tokenizer.nextToken());
							servicetag = tokenizer.nextToken();
							assignedto = tokenizer.nextToken();
							
							sql = "SELECT user_id FROM users WHERE user_name='"+connecteduser+ "';"; //gets the ID of the connected user
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							
							s = "";
							while (rs.next()) 
							{
								// Get the data from the row using the column index
								s = rs.getString(1);
							}
							
							opened_by = Integer.parseInt(s); //the current connected user's id
							
							timeobject = new time();
							date_open =  timeobject.GetCurrentYear_asInt() + "-" + timeobject.GetCurrentMonth_asInt() + "-" + timeobject.GetCurrentDay_asInt()+ "-" + timeobject.GetMilitaryTime_asString();
							
							sql = "UPDATE workorders SET asset_number = " + assetnum + " WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET status = " + status + " WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET problem = '" + problem + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET resolution = '" + resolution + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET campus = '" + campus + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET opened_by = " + opened_by + " WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET model = '" + model + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET date_open = '" + date_open + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET first_name = '" + firstname + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET last_name = '" + lastname + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET room_number = '" + roomnumber + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET phone = '" + phone + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET priority = " + priority + " WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET service_tag = '" + servicetag + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET assigned_to = '" + assignedto + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
						break;
						case 14: //change the status back to phone support
							tokenizer = new StringTokenizer(data,",");
							
							status = 4;
							firstname = tokenizer.nextToken();
							lastname = tokenizer.nextToken();
							campus = tokenizer.nextToken();
							roomnumber = tokenizer.nextToken();
							priority = tokenizer.nextToken();
							phone = tokenizer.nextToken();
							assetnum = Integer.parseInt(tokenizer.nextToken());
							model = tokenizer.nextToken();
							problem = tokenizer.nextToken();
							resolution = tokenizer.nextToken();
							wonum = Integer.parseInt(tokenizer.nextToken());
							servicetag = tokenizer.nextToken();
							assignedto = tokenizer.nextToken();
							
							sql = "SELECT user_id FROM users WHERE user_name='"+connecteduser+ "';"; //gets the ID of the connected user
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							
							s = "";
							while (rs.next()) 
							{
								// Get the data from the row using the column index
								s = rs.getString(1);
							}
							
							opened_by = Integer.parseInt(s); //the current connected user's id
							
							timeobject = new time();
							date_open =  timeobject.GetCurrentYear_asInt() + "-" + timeobject.GetCurrentMonth_asInt() + "-" + timeobject.GetCurrentDay_asInt()+ "-" + timeobject.GetMilitaryTime_asString();
							
							sql = "UPDATE workorders SET asset_number = " + assetnum + " WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET status = " + status + " WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET problem = '" + problem + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET resolution = '" + resolution + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET campus = '" + campus + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET opened_by = " + opened_by + " WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET model = '" + model + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET date_open = '" + date_open + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET first_name = '" + firstname + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET last_name = '" + lastname + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET room_number = '" + roomnumber + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET phone = '" + phone + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET priority = " + priority + " WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET service_tag = '" + servicetag + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET assigned_to = '" + assignedto + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
						break;
						case 15: //send the campus this user is registered to
							sql = "SELECT campus FROM users WHERE user_name= '"+ data + "';";
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							md = rs.getMetaData();
							nColumns = md.getColumnCount();	
							workorderdata = new StringBuffer();
							while(rs.next())
							{
								workorderdata.append(rs.getString(1));
								workorderdata.append(":");
							}
							try
							{	//this will cause an exception if there is nothing in the string.
								//a slightly ghetto fix, but just remove the last character ";" off of the string before its sent. 
								workorderdata.substring(0,workorderdata.length()-1);
							}
							catch(Exception e)
							{
								System.out.println("String is null...aborting operation.");
							}
							messagetco.putMessage(workorderdata.toString());
						break;
						case 19://does the same thing as 20, but executes in an array of objects.
							sql = data;
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
					        	
					        md = rs.getMetaData();
					        nColumns = md.getColumnCount();	
					        object = resultSetToObject(rs);
					        
					        X = object.length;
					        try
					        {
					        	Y = object[0].length;
					        }
					        catch(Exception f)
					        {
					        	Y = 0;
					        }
					        
					        
					        xy = X + "," + Y; 
					        messagetco.putMessage(xy); //this is here so the client knows how many objects to expect.
					        for(int c = 0; c < X; c++)
					        {
					        	for(int d = 0; d < Y; d++)
					        	{
					        		messagetco.putMessage(object[c][d]);
					        	}
					        }
						break;
						//executes as a string
						case 20: //execute the raw sql command and send it back.
							//all the reports SQL requests are executed here.
							sql = data;
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							md = rs.getMetaData();
					        nColumns = md.getColumnCount();	
							workorderdata = new StringBuffer();
							while(rs.next())
							{
								for(int i=1;i<=nColumns;i++)
								{
									workorderdata.append(rs.getString(i));
									workorderdata.append((i==nColumns)?":":",");
								}
							}
							try
							{	//this will cause an exception if there is nothing in the string.
								//a slightly ghetto fix, but just remove the last character ";" off of the string before its sent. 
								workorderdata.substring(0,workorderdata.length()-1);
							}
							catch(Exception e)
							{
								System.out.println("String is null...aborting operation.");
							}
							messagetco.putMessage(workorderdata.toString());
						break;
						
						case 21: //send a list of technicians as UIDs
							sql = "SELECT user_id FROM users WHERE user_level < 2 AND active=1;";
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							String list = "";
							while(rs.next())
							{
								list = list + rs.getString(1) + ",";
							}
							
							try
							{	//this will cause an exception if there is nothing in the string.
								//a slightly ghetto fix, but just remove the last character ";" off of the string before its sent. 
								list = list.substring(0,list.length()-1);
							}
							catch(Exception e)
							{
								System.out.println("String is null...aborting operation.");
							}
							messagetco.putMessage(list);
						break;
						//case 22 is reserved
						case 23://send a copy of ALL the open work orders
							sql = "SELECT work_order_number,priority,asset_number,problem,campus,date_open,room_number,assigned_to,follow_up_sent FROM workorders WHERE status=1;";	
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							
							md = rs.getMetaData();
					        nColumns = md.getColumnCount();	
					        object = resultSetToObject(rs);
					        
					        X = object.length;
					        try
					        {
					        	Y = object[0].length;
					        }
					        catch(Exception f)
					        {
					        	Y = 0;
					        }
					        xy = X + "," + Y; 
					        messagetco.putMessage(xy); //this is here so the client knows how many objects to expect.
					        for(int c = 0; c < X; c++)
					        {
					        	for(int d = 0; d < Y; d++)
					        	{
					        		messagetco.putMessage(object[c][d]);
					        	}
					        }
					        columns_selected = "WO #,Priority,Asset #,Problem,Campus,Date Open, Room #, Assigned To";
					       	messagetco.putMessage(columns_selected);
						break;
						case 24: //update the users password.
							String newpassword = data.substring(0,data.indexOf(","));
							username = data.substring(data.indexOf(",") +1, data.length());
							
							sql = "UPDATE users SET password = '" + newpassword + "' WHERE user_name = '" + username + "';";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							messagetco.putMessage("OK");
						break;
						case 25://send a list of technicians as name (case 21 is the user numbers)
							sql = "SELECT user_name FROM users WHERE user_level=1 OR user_level=0 AND active=1;";
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							list = "";
							while(rs.next())
							{
								list = list + rs.getString(1) + ",";
							}
							
							try
							{	//this will cause an exception if there is nothing in the string.
								//a slightly ghetto fix, but just remove the last character ";" off of the string before its sent. 
								list = list.substring(0,list.length()-1);
							}
							catch(Exception e)
							{
								System.out.println("String is null...aborting operation.");
							}
							messagetco.putMessage(list);
						break;
						case 26://authentication for a domain user.
							//user *appears* to be a domain member.
							connecteduser = data.substring(data.indexOf(":")+1,data.length());
							
							//gets the userlevel from the database
							sql = "SELECT USER_LEVEL FROM users WHERE USER_NAME = '" + connecteduser + "';";
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							s = "";
							while (rs.next()) 
							{
								// Get the data from the row using the column index
								s = rs.getString(1);
							}
							String userlevel = s;
							//send the user the authentication message
							authenticated = true;
							messagetco.putMessage(userlevel); //authenticated
							//if the user is not in the maginot database, we need to put them in there.
							// this way they have a maginot user number.
							if(userlevel.equals(""))
							{
								//get the second octect and figure out which site they're from.
								String ip = clientSocket.getInetAddress().toString();
								ip = ip.substring(1,ip.length());
								
								StringTokenizer iptokenizer = new StringTokenizer(ip,".");
								
								String octects[] = new String[4];
								octects[0] = iptokenizer.nextToken();
								octects[1] = iptokenizer.nextToken();
								octects[2] = iptokenizer.nextToken();
								octects[3] = iptokenizer.nextToken();
								
								//get the campus this vlan is associated with.
								sql = "SELECT site_id from sites where vlan=" + octects[1] + ";";
								pstmt = connection.prepareStatement(sql);
								rs = pstmt.executeQuery();
								s = "";
								while (rs.next()) 
								{
									// Get the data from the row using the column index
									s = rs.getString(1);
								}
								
								//the user doesn't exist in Maginot...create it.
								/*in theory, we shouldn't multiple users with the same name. so the check for
								 *duplicate names was not made here. Since their login name is coming from AD, and AD
								 *cannot have duplicate names it shouldn't happen here either.
								 */
								int uid = nextUserNumber();
								//automatically make the user a standard user. If they require more than that, the help desk can assign the higher user level.
								sql = "INSERT INTO users (user_id,user_name,password,campus,user_level,active) VALUES (" + uid +",'"+ connecteduser +"','" + "domainuser" + "','" + s + "'," + 2 + ","+ 1 + ");";
								pstmt = connection.prepareStatement(sql);
								pstmt.executeUpdate();
							}
						break;
							//27 is reserved for domain authentication.
						case 28:
							sql = "SELECT user_id FROM users WHERE user_name='" + connecteduser +"';";
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							
							name = "";
							while(rs.next())
							{
								name =  rs.getString(1);
							}
							
							
							sql = "SELECT work_order_number,priority,asset_number,problem,campus,date_open,room_number,assigned_to,follow_up_sent FROM workorders WHERE assigned_to='"+ name+"' AND status=1;";	
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							
							md = rs.getMetaData();
					        nColumns = md.getColumnCount();	
					        object = resultSetToObject(rs);
					        
					        X = object.length;
					        try
					        {
					        	Y = object[0].length;
					        }
					        catch(Exception f)
					        {
					        	Y = 0;
					        }
					        xy = X + "," + Y; 
					        messagetco.putMessage(xy); //this is here so the client knows how many objects to expect.
					        for(int c = 0; c < X; c++)
					        {
					        	for(int d = 0; d < Y; d++)
					        	{
					        		messagetco.putMessage(object[c][d]);
					        	}
					        }
					        columns_selected = "WO #,Priority,Asset #,Problem,Campus,Date Open, Room #, Assigned To";
					       	messagetco.putMessage(columns_selected);
						break;
						case 30: //authenticates the version
							//we need this because if an older client attempts to connect to the server,
							//the client may corrupt the database.
							double clientnum = Double.parseDouble(data);
							if(clientnum == versionnum)
								messagetco.putMessage("OK");
							else
								messagetco.putMessage("NO");
						break;
						case 40: //send the users table
							sql = "SELECT * FROM users;";	
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							
							md = rs.getMetaData();
					        nColumns = md.getColumnCount();	
					        object = resultSetToObject(rs);
					        
					        X = object.length;
					        try
					        {
					        	Y = object[0].length;
					        }
					        catch(Exception f)
					        {
					        	Y = 0;
					        }
					        
					        xy = X + "," + Y; 
					        messagetco.putMessage(xy); //this is here so the client knows how many objects to expect.
					        for(int c = 0; c < X; c++)
					        {
					        	for(int d = 0; d < Y; d++)
					        	{
					        		messagetco.putMessage(object[c][d]);
					        	}
					        }
						break;
						case 41:
							//send the metadata from the table users
							sql = "SELECT * FROM users;";	
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();		
							md = rs.getMetaData();
	        
					        nColumns = md.getColumnCount();
					        String columnNames = "";
							for(int i=1;i<=nColumns;i++)
							{
								if(i==1)
									columnNames = columnNames + md.getColumnLabel(i);
								else
									columnNames = columnNames + "," + md.getColumnLabel(i);
							}
							messagetco.putMessage(columnNames);
						break;
						case 42: // Save this as a new user
							tokenizer = new StringTokenizer(data,",");
							String user_name = tokenizer.nextToken();
							String campus = tokenizer.nextToken();
								 userlevel = tokenizer.nextToken();
							String active = tokenizer.nextToken();
							       password = tokenizer.nextToken();
							int uid = nextUserNumber();
							
							sql = "INSERT INTO users (user_id,user_name,password,campus,user_level,active) VALUES (" 
									+ uid +",'"+ user_name +"','" + password + "','"+ campus + "'," + userlevel + ","+ active + ");";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							
							
							
						break;
						case 43: //Update this user
							tokenizer = new StringTokenizer(data,",");
							user_name = tokenizer.nextToken();
							campus = tokenizer.nextToken();
							userlevel = tokenizer.nextToken();
							active = tokenizer.nextToken();
							password = tokenizer.nextToken();
							uid = Integer.parseInt(tokenizer.nextToken());
							
							sql = "UPDATE users SET user_name = '" + user_name + "' WHERE user_id=" + uid + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							
							sql = "UPDATE users SET campus='" + campus + "' WHERE user_id=" +uid+ ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							
							sql = "UPDATE users SET user_level='" + userlevel + "' WHERE user_id=" +uid+ ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							
							sql = "UPDATE users SET active=" + active + " WHERE user_id=" +uid+ ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							
							sql = "UPDATE users SET password='" + password + "' WHERE user_id=" +uid+ ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							
						break;
						case 44: //Deactivate this user
							tokenizer = new StringTokenizer(data, ",");
							
							uid = Integer.parseInt(tokenizer.nextToken());
							sql = "UPDATE users SET active =0 WHERE user_id = " + uid + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate(); 
						break;
						case 45://update this work order with the following information
							tokenizer = new StringTokenizer(data,",");
							
							status = 1;
							wonum = Integer.parseInt(tokenizer.nextToken());
							
							temp_asset = tokenizer.nextToken();
							
							if(!temp_asset.equals("null"))
								assetnum = Integer.parseInt(temp_asset);
							else
								assetnum = -1;
							
							problem = tokenizer.nextToken();
							resolution = tokenizer.nextToken();
							campus = tokenizer.nextToken();
							model = tokenizer.nextToken();
							firstname = tokenizer.nextToken();
							lastname = tokenizer.nextToken();
							roomnumber = tokenizer.nextToken();
							phone = tokenizer.nextToken();
							priority = tokenizer.nextToken();
							servicetag = tokenizer.nextToken();
							assignedto = tokenizer.nextToken();
							
							if(assetnum == -1)
							{
								sql = "UPDATE workorders SET asset_number = " +null + " WHERE work_order_number = " + wonum + ";";
								pstmt = connection.prepareStatement(sql);
								pstmt.executeUpdate();
							}
							else
							{
								sql = "UPDATE workorders SET asset_number = " + assetnum + " WHERE work_order_number = " + wonum + ";";
								pstmt = connection.prepareStatement(sql);
								pstmt.executeUpdate();
							}
								
							sql = "UPDATE workorders SET status = " + status + " WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET problem = '" + problem + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET resolution = '" + resolution + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET campus = '" + campus + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();

							sql = "UPDATE workorders SET model = '" + model + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();

							sql = "UPDATE workorders SET first_name = '" + firstname + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET last_name = '" + lastname + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET room_number = '" + roomnumber + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET phone = '" + phone + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET priority = " + priority + " WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET service_tag = '" + servicetag + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE workorders SET assigned_to = '" + assignedto + "' WHERE work_order_number = " + wonum + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
						break;
						case 46://translate this username to a UID
							sql = "SELECT user_id FROM users WHERE user_name='" + data +"';";
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							
							name = "";
							while(rs.next())
							{
								name =  rs.getString(1);
							}
							messagetco.putMessage(name);
						break;
						case 47://send a list of users 0 and 1 with their names and IDs.
							sql = "select user_id,user_name from users where user_level < 2;";	
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							
							md = rs.getMetaData();
					        nColumns = md.getColumnCount();	
					        object = resultSetToObject(rs);
					        
					        X = object.length;
					        try
					        {
					        	Y = object[0].length;
					        }
					        catch(Exception f)
					        {
					        	Y = 0;
					        }
					        
					        xy = X + "," + Y; 
					        messagetco.putMessage(xy); //this is here so the client knows how many objects to expect.
					        for(int c = 0; c < X; c++)
					        {
					        	for(int d = 0; d < Y; d++)
					        	{
					        		messagetco.putMessage(object[c][d]);
					        	}
					        }
						
						break;
						//the 05x cases will be used for the table sites and the site functions.
						case 50: //return the table of sites
							sql = "SELECT * FROM sites;";	
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							
							md = rs.getMetaData();
					        nColumns = md.getColumnCount();	
					        object = resultSetToObject(rs);
					        
					        X = object.length;
					        try
					        {
					        	Y = object[0].length;
					        }
					        catch(Exception f)
					        {
					        	Y = 0;
					        }
					        
					        xy = X + "," + Y; 
					        messagetco.putMessage(xy); //this is here so the client knows how many objects to expect.
					        for(int c = 0; c < X; c++)
					        {
					        	for(int d = 0; d < Y; d++)
					        	{
					        		messagetco.putMessage(object[c][d]);
					        	}
					        }
						break;
						case 51:
							//send the metadata from the table sites
							sql = "SELECT * FROM sites;";	
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();		
							md = rs.getMetaData();
	        
					        nColumns = md.getColumnCount();
					        columnNames = "";
							for(int i=1;i<=nColumns;i++)
							{
								if(i==1)
									columnNames = columnNames + md.getColumnLabel(i);
								else
									columnNames = columnNames + "," + md.getColumnLabel(i);
							}
							messagetco.putMessage(columnNames);
						break;
						case 52: //save as a new site
							tokenizer = new StringTokenizer(data, ",");
							
							uid = nextSiteNumber();
							String sitename = tokenizer.nextToken();
							String phonenum = tokenizer.nextToken();
							int vlan = Integer.parseInt(tokenizer.nextToken());
							
							//make sure the username does not already exist in the database. 
							sql = "SELECT * FROM sites WHERE site_name='" + sitename+"';";
							
						    pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							
							int count = 0;
							while(rs.next())
							{
								count++;
							}
							if(count  == 0)
							{
							
								sql = "INSERT INTO sites (site_id, site_name,phone,vlan) VALUES (" + uid +",'"+sitename+"','"+phonenum+"'," + vlan+");";
								pstmt = connection.prepareStatement(sql);
								pstmt.executeUpdate();
								messagetco.putMessage("1");
							}
							else
							{
								messagetco.putMessage("2");
							}		
						break;
						case 53: //update this site
							tokenizer = new StringTokenizer(data, ",");
							
							int siteid = Integer.parseInt(tokenizer.nextToken());
							sitename = tokenizer.nextToken();
							phone = tokenizer.nextToken();
							vlan = Integer.parseInt(tokenizer.nextToken());
							
							//make sure the username does not already exist in the database. 
							sql = "SELECT * FROM sites WHERE site_name='" + sitename+"';";
						    pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							
							
							
							count = 0;
							while(rs.next())
							{
								count++;
								
							}
							if(count != 0)
							{
								//apparently we are attempting to update a user that has the 
								//same name as one already in the database.
								//Is the user we are trying to update this user? or a different one.
								//if it is a different one, issue an error message.
								//if it is this one, go ahead and update. 
								
								sql = "SELECT site_id FROM sites WHERE site_name='"+sitename+"';";
								pstmt = connection.prepareStatement(sql);
								rs = pstmt.executeQuery();
								
								while(rs.next())
								{
									int thisuid = Integer.parseInt(rs.getString(1));
									if(thisuid != siteid)
									{
										count = -1;
										break;
									}
									else
									{
										count = 0;
									}
								}
							}
							if(count  == 0)
							{
								sql = "UPDATE sites SET site_name = '" + sitename + "' WHERE site_id = " + siteid + ";";
								pstmt = connection.prepareStatement(sql);
								pstmt.executeUpdate();
							
								sql = "UPDATE sites SET phone= '" + phone + "' WHERE site_id = " + siteid + ";";
								pstmt = connection.prepareStatement(sql);
								pstmt.executeUpdate();
								
								sql = "UPDATE sites SET vlan= " + vlan + " WHERE site_id = " + siteid + ";";
								pstmt = connection.prepareStatement(sql);
								pstmt.executeUpdate();
								
								messagetco.putMessage("1");
							}
							else
							{
								messagetco.putMessage("2");
							}
							
						break;
						case 54://send a list of sites
							sql = "SELECT site_name FROM sites";
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							StringBuffer sitenames = new StringBuffer();
							while(rs.next())
							{
								sitenames.append(rs.getString(1));
								sitenames.append(",");
							}
							
							try
							{	//this will cause an exception if there is nothing in the string.
								//a slightly ghetto fix, but just remove the last character ";" off of the string before its sent. 
							
								sitenames.substring(0,sitenames.length()-1);
							}
							catch(Exception e)
							{
								System.out.println("String is null...aborting operation.");
							}
							messagetco.putMessage(sitenames.toString());
						break;
						case 55:
							//translate siteID into sitename
							sql = "SELECT site_name FROM sites WHERE site_id=" + data +";";
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							
							name = "";
							while(rs.next())
							{
								name =  rs.getString(1);
							}
							messagetco.putMessage(name);
						break;
						case 56://translate this siteName into a SiteID
							sql = "Select site_id FROM sites WHERE site_name='"+data+"';";
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							
							name = "";
							while(rs.next())
							{
								name =  rs.getString(1);
							}
							messagetco.putMessage(name);
						break;
						case 57: //send a list of all the siteIDs
							sql = "SELECT site_id FROM sites";
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							list = "";
							while(rs.next())
							{
								list = list + rs.getString(1) + ",";
							}
							
							try
							{	//this will cause an exception if there is nothing in the string.
								//a slightly ghetto fix, but just remove the last character ";" off of the string before its sent. 
								list = list.substring(0,list.length()-1);
							}
							catch(Exception e)
							{
								System.out.println("String is null...aborting operation.");
							}
							messagetco.putMessage(list);
						break;
						case 60://send the connected username.
							messagetco.putMessage(connecteduser);
						break;
						//the 07x cases will be used for auto fill information
						case 70://send the phone number for this site name.
							sql = "Select phone FROM sites WHERE site_name='"+data+"';";
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							
							name = "";
							while(rs.next())
							{
								name =  rs.getString(1);
							}
							messagetco.putMessage(name);
						break;
						//the 08x cases are used for managing the database from the client.
						case 81://send a copy of the requested table for backup.
							sql = "SELECT * FROM " + data + ";";	
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							
							md = rs.getMetaData();
					        nColumns = md.getColumnCount();	
					        object = resultSetToObject(rs);
					        
					        X = object.length;
					        try
					        {
					        	Y = object[0].length;
					        }
					        catch(Exception f)
					        {
					        	Y = 0;
					        }
					        
					        xy = X + "," + Y; 
					        messagetco.putMessage(xy); //this is here so the client knows how many objects to expect.
					        for(int c = 0; c < X; c++)
					        {
					        	for(int d = 0; d < Y; d++)
					        	{
					        		messagetco.putMessage(object[c][d]);
					        	}
					        }
						break;
						case 82://used for droping a table
							//the only thing we actually need to do is drop the table, and recreate it.
							//the client will then use another case number to upload the contents of the table.
							sql = "DROP TABLE IF EXISTS " + data + ";";	
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
						break;
						case 83://used for creating a table.
							sql = "CREATE TABLE " + data.substring(0,data.indexOf(",")) + " (" + data.substring(data.indexOf(",") +1,data.length()) + ");";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
						break;
						case 84://used for submitting a workorder that is being restored from backup.
							sql = "Insert into " + data + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
						break;
						//09x cases will be used for email validation
						case 91://send the date the email was sent.
								sql = "Select DATE_FOLLOW_UP_SENT from workorders where work_order_number='"+data+"';";
								pstmt = connection.prepareStatement(sql);
								rs = pstmt.executeQuery();
								name ="";
								while(rs.next())
								{
									name = rs.getString(1);
								}
								if(name == null)
								{
									name = "0";
								}
								messagetco.putMessage(name);
						break;
						case 92://set the email sent as true
							ensureConfigExists();
							sql = "UPDATE workorders set follow_up_sent=1 where work_order_number=" + data +";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							
							timeobject = new time();
							date_open =  timeobject.GetCurrentYear_asInt() + "-" + timeobject.GetCurrentMonth_asInt() + "-" + timeobject.GetCurrentDay_asInt()+ "-" + timeobject.GetMilitaryTime_asString();
							sql = "UPDATE workorders SET date_follow_up_sent='" + date_open +"' where work_order_number="+data +";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							
							//send the email
							sendEmail(Integer.parseInt(data));
								
						break;
						case 93: //set the email sent as false.
							
							sql = "UPDATE workorders SET follow_up_sent=0 WHERE work_order_number=" + data +";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							
							sql = "UPDATE workorders SET date_follow_up_sent=null WHERE work_order_number="+data +";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();	
						break;
						case 94://get the status of the email.
								sql = "SELECT follow_up_sent FROM workorders WHERE work_order_number='"+data+"';";
								pstmt = connection.prepareStatement(sql);
								rs = pstmt.executeQuery();
								name ="";
								while(rs.next())
								{
									name = rs.getString(1);
								}
								if(name == null)
								{
									name = "0";
								}
								messagetco.putMessage(name);
						break;
						//case 11x are used for the email document and Mail configs.
						case 100: // save the email body
							ensureConfigExists();
							sql = "UPDATE email SET config= '" + data.substring(data.indexOf("=")+1,data.length()) +  "' WHERE property='" +data.substring(0,data.indexOf("=")) +"';";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
						break;
						case 101: //read the config
							ensureConfigExists();
							data = data.substring(0,data.indexOf("="));
							sql = "SELECT config FROM email WHERE property='"+data+"';";
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							name ="";
							while(rs.next())
							{
								name = rs.getString(1);
							}
							if(name == null)
							{
								name = "0";
							}
							messagetco.putMessage(name);
						break;
						case 110://get the TTC for a Work order
							sql = "SELECT time_to_completion FROM workorders WHERE work_order_number='"+data+"';";
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							name = "";
							while(rs.next())
							{
								name =  rs.getString(1);
							}
							if(name ==null)
							{
								name = "null";
							}
							messagetco.putMessage(name);
						break;
						case 111://calculate the TTC for a work order and set it in the database so it doesn't have to be calculated again.
							sql = "SELECT date_open FROM workorders WHERE work_order_number='"+data+"';";
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							String start_time = "";
							while(rs.next())
							{
								start_time =  rs.getString(1);
							}
							sql = "SELECT date_closed FROM workorders WHERE work_order_number='"+data+"';";
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							String end_time = "";
							while(rs.next())
							{
								end_time =  rs.getString(1);
							}
							timeobject = new time();
							
							String ttc = timeobject.GetTimeDifference(start_time,end_time);
							
							sql = "UPDATE workorders SET TIME_TO_COMPLETION= '" +ttc +  "' where work_order_number='" +data +"';";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
						break;
						case 120:
							sql = "SELECT campus FROM status WHERE uid =" + data + ";";
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							
							campus ="";
							while(rs.next())
							{
								campus = rs.getString(1);
							}
							if(campus == null)
							{
								campus = "0";
							}
							messagetco.putMessage(campus);
						break;
						case 121: //send the metadata from the table status.
						sql = "SELECT * FROM status;";	
						pstmt = connection.prepareStatement(sql);
						rs = pstmt.executeQuery();		
						md = rs.getMetaData();
        
				        nColumns = md.getColumnCount();
				        columnNamesBuffer = new StringBuffer();
						for(int i=1;i<=nColumns;i++)
						{
							if(i==1)
								columnNamesBuffer.append(md.getColumnLabel(i));
							else
							{
								columnNamesBuffer.append(",");
								columnNamesBuffer.append(md.getColumnLabel(i));
							}	
						}
						messagetco.putMessage(columnNamesBuffer.toString());
						break;
						case 122:
							tokenizer = new StringTokenizer(data,",");
							
							int inout = Integer.parseInt(tokenizer.nextToken());
							String notes = tokenizer.nextToken();
							int campusnum = Integer.parseInt(tokenizer.nextToken());
							
							int connected = 0;
							sql = "SELECT user_id FROM users WHERE user_name='" + connecteduser +"';";
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							
							name = "";
							while(rs.next())
							{
								name =  rs.getString(1);
							}
							connected = Integer.parseInt(name);

							sql = "UPDATE status SET  campus=" + campusnum + " WHERE uid =" + connected + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE status SET  status=" + inout + " WHERE uid =" + connected + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
							sql = "UPDATE status SET  notes='" + notes + "' WHERE uid =" + connected + ";";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
						break;
						case 123:
							connected = 0;
							sql = "SELECT user_id FROM users WHERE user_name='" + connecteduser +"';";
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							
							name = "";
							while(rs.next())
							{
								name =  rs.getString(1);
							}
							connected = Integer.parseInt(name);
							sql = "INSERT INTO status (uid) VALUES("+connected + ");";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
						break;
						case 130://gets the priority for a workorder.
							sql = "SELECT priority FROM workorders WHERE work_order_number=" + data + ";";
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							
							rs.next();
							priority = rs.getString(1);
							
							if(priority == null)
							{
								priority = "null";
							}
							
							messagetco.putMessage(priority);
							
						break;
						case 140://sets the background color for the current user
							sql = "UPDATE users SET  bgcolor='" + data + "' WHERE user_name ='" + connecteduser + "';";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
						break;
						case 141: //set the text color for the current user
							sql = "UPDATE users SET  txtcolor='" + data + "' WHERE user_name ='" + connecteduser + "';";
							pstmt = connection.prepareStatement(sql);
							pstmt.executeUpdate();
						break;
						case 142: //gets the background color for the current user
							sql = "SELECT bgcolor FROM users WHERE user_name='" + connecteduser + "';";
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							
							rs.next();
							String color = rs.getString(1);
							if((color == null) || (color.equals("")))
							{
								color = "238,238,238";
							}
							else
							{
								color = color.replaceAll(" ", ",");
							}
							messagetco.putMessage(color);
						break;
						case 143: //gets the text color for the current user
							sql = "SELECT txtcolor FROM users WHERE user_name='" + connecteduser + "';";
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();
							
							rs.next();
							color = rs.getString(1);
							if((color == null) || (color.equals("")))
							{
								color = "0,0,0";
							}
							else
							{
								color = color.replaceAll(" ", ",");
							}
							messagetco.putMessage(color);
						break;
						//15x will be used for database cleanup
					/*	case 150:
							sql = "SELECT * FROM workorders where status=1;";	
							pstmt = connection.prepareStatement(sql);
							rs = pstmt.executeQuery();		
							md = rs.getMetaData();
							
							nColumns = md.getColumnCount();
							String names[] = new String[nColumns];
							
							for(int i=1;i<=nColumns;i++)
							{
								names[i-1] = (md.getColumnLabel(i));
							}
							for(int c = 0; c < nColumns; c++)
							{
								try
								{
									sql = "UPDATE workorders SET " + names[c]+"=null WHERE "+names[c] + "='null';";
									pstmt = connection.prepareStatement(sql);
									pstmt.executeUpdate();
								}
								catch(com.mysql.jdbc.MysqlDataTruncation e)
								{
									//the field type isn't a string
								}
								catch(com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException e)
								{
									//field type can't be null... in this and the above catch... whatever.
								}
							}
						break;*/
					}
					connection.close();
			    } 
			    catch (ClassNotFoundException e)
			    {
			    	System.out.println("Exception " + e);
			    	if(instruction_code == 3)
			    	{
			    		messagetco.putMessage("0");
			    	}
			        // Could not find the database driver
			    }
			    catch (SQLException e)
			    {
			    	if(instruction_code == 3)
			    	{
			    		messagetco.putMessage("0");
			    	}
			    	System.out.println("Exception " + e);
			        // Could not connect to the database
			    }
			}
			else
			{
				System.out.println("Error: Request from unauthenticated user");
			}
		}
		System.out.println("break");
	}
	
	public void sendEmail(int wonum)
	{					
		StringBuffer body = new StringBuffer();
		String domain = "";
		String from = "";
		String host = "";
		String closed_by = "";
		StringBuffer subject = new StringBuffer();
		
		String name = "";
		String sql;
		//connects to the database. 
		Connection connection = null;
		try 
	    {
			Class.forName("org.gjt.mm.mysql.Driver");
			
			// Create a connection to the database
			String serverName = "localhost";
			String mydatabase = "Maginot";
			
			String url = "jdbc:mysql://" + serverName +  "/" + mydatabase; // a JDBC url
			String username = "maginot"; //SQL username
			String password = "dell001"; //SQL password for the above username.
			connection = DriverManager.getConnection(url, username, password);
			Statement stmt = connection.createStatement();
			PreparedStatement pstmt;
			
			//gets the email body
			sql = "SELECT config FROM email WHERE property='body';";
			pstmt = connection.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
		
			while(rs.next())
			{
				body.append(rs.getString(1));
			}
			if(name == null)
			{
				name = "0";
			}
			
			sql = "SELECT config FROM email WHERE property='domain';";
			pstmt = connection.prepareStatement(sql);
			rs = pstmt.executeQuery();
		
			while(rs.next())
			{
				domain = "@" + rs.getString(1);
			}
			if(name == null)
			{
				name = "0";
			}
			sql = "SELECT closed_by FROM workorders WHERE work_order_number=" + wonum + ";";
			pstmt = connection.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			while(rs.next())
			{
				closed_by = rs.getString(1);
			}
			if(name == null)
			{
				closed_by = " ";
			}
			//translates the UID from the previous sql query to a username
			sql = "SELECT user_name FROM users WHERE user_id=" + closed_by +";";
			pstmt = connection.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			
			while(rs.next())
			{
				closed_by =  rs.getString(1);
			}
			
			//figures out who the email is going to
			sql = "SELECT first_name FROM workorders WHERE work_order_number=" + wonum + ";";
			pstmt = connection.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			while(rs.next())
			{
				name = rs.getString(1);
			}
			if(name == null)
			{
				name = "0";
			}
			sql = "SELECT last_name FROM workorders WHERE work_order_number=" + wonum + ";";
			pstmt = connection.prepareStatement(sql);
			rs = pstmt.executeQuery();
		
			while(rs.next())
			{
				name = name + "." + rs.getString(1) + domain;
			}
			if(name == null)
			{
				name = "0";
			}
			
			sql = "SELECT config FROM email WHERE property='mail.from';";
			pstmt = connection.prepareStatement(sql);
			rs = pstmt.executeQuery();
		
			while(rs.next())
			{
				from = rs.getString(1);
			}
			if(name == null)
			{
				name = "0";
			}
			
			sql = "SELECT config FROM email WHERE property='mail.host';";
			pstmt = connection.prepareStatement(sql);
			rs = pstmt.executeQuery();
		
			while(rs.next())
			{
				host = rs.getString(1);
			}
			if(name == null)
			{
				name = "0";
			}
			
			sql = "SELECT config FROM email WHERE property='subject';";
			pstmt = connection.prepareStatement(sql);
			rs = pstmt.executeQuery();
		
			while(rs.next())
			{
				subject.append(rs.getString(1));
			}
			if(name == null)
			{
				name = "0";
			}
		
	    }
	    catch (ClassNotFoundException e) 
	    {
	    	System.out.println("Exception " + e);
	        // Could not find the database driver
	    }
	    catch (SQLException e) 
	    {
	    	System.out.println("Exception " + e);
	        // Could not connect to the database
	    }
	    
	    //now, replace the variables with the actual values.
	    for(int c = 0; c < subject.length(); c++)
	    {
	    	if(subject.substring(c,c+1).equals("<"))
	    	{
	    		try
	    		{
	    			if(subject.substring(c-1,c+8).equalsIgnoreCase("<<wonum>>"))
	    			{
	    				String first = subject.substring(0,c-1);
	    				String second = subject.substring(c+8,subject.length());
	    				first = first + wonum + second;
	    				subject = new StringBuffer();
	    				subject.append(first);
	    			}
	    		}
	    		catch(Exception f)
	    		{
	    			
	    		}
	    	}
	    }
	    for(int c = 0; c < body.length(); c++)
	    {
	    	if(body.substring(c,c+1).equals("<"))
	    	{
	    		try
	    		{
	    			if(body.substring(c-1,c+12).equalsIgnoreCase("<<techsname>>"))
	    			{
	    				String first = body.substring(0,c-1);
	    				String second = body.substring(c+12,body.length());
	    				//takes out the . in the closed by variable.
	    				for(int e = 0; e < closed_by.length(); e++)
	    				{
	    					if(closed_by.charAt(e) == '.')
	    						closed_by = closed_by.substring(0,e) + " " + closed_by.substring(e+1,closed_by.length());
	    				}
	    				
	    				first = first +closed_by + second;
	    				body = new StringBuffer();
	    				body.append(first);
	    			}
	    		}
	    		catch(Exception f)
	    		{
	    			
	    		}
	    	}
	    }
	    
	    
	    
	    Emailer emailsender = new Emailer();	
		emailsender.sendEmail(from,name,subject.toString(), body.toString(),host);	
	}
	
	public int readLines(String textFileToRead)
    {
    	int numberoflines = 0;
   		try
    	{
    		BufferedReader inputStream = new BufferedReader(new FileReader(textFileToRead));
    		String tempstring = null;
    		while(inputStream.readLine() != null)
    		{
    			numberoflines++;
    		}
    		inputStream.close();
    	}
    	
    	catch(FileNotFoundException e)
    	{
    		System.out.println("The File was not found!");
    	}
    	catch(IOException e)
    	{
    		System.out.println("Error reading from saved text");
    	} 
    	return numberoflines;
    }
	
	public void authenticate(String data)
	{	
		ensureMethods();
		//set up the connection to the sql server.
		String sql;
		//connects to the database. 
		Connection connection = null;
		try 
	    {
			Class.forName("org.gjt.mm.mysql.Driver");
			
			// Create a connection to the database
			String serverName = "localhost";
			String mydatabase = "Maginot";
			
			String url = "jdbc:mysql://" + serverName +  "/" + mydatabase; // a JDBC url
			String username = "maginot"; //SQL username
			String password = "dell001"; //SQL password for the above username.
			connection = DriverManager.getConnection(url, username, password);
			Statement stmt = connection.createStatement();
			PreparedStatement pstmt;
			
		
			//----------------------------------------------------------
			//check the user in the database.
			
			//take apart the data line and figure out what username and password they are trying to connect with. 
			String connectioncode = data.substring(0,data.indexOf(":"));
			int position1, position2;
			position1 = data.indexOf(":");
			position2 = data.indexOf(",",position1);
			position1++;
			String uname = data.substring(position1,position2);
			position1 = position2+1; 
			position2 = data.indexOf( ",",position1);
			String pword = data.substring(position1,data.length());
			position1 = position2+1;
			
			sql = "SELECT password FROM users WHERE USER_NAME = '" + uname + "';";
			
			connecteduser = uname;
			
			
			pstmt = connection.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			String s = "";
			while (rs.next()) 
			{
				// Get the data from the row using the column index
				s = rs.getString(1);
			}
			System.out.println(s + "     " + pword);
			if(s.equalsIgnoreCase(pword))
			{
				//the password matches, but is the useraccount active?
				sql = "SELECT active FROM users WHERE USER_NAME = '" + uname + "';";

				pstmt = connection.prepareStatement(sql);
				rs = pstmt.executeQuery();
				s = "";
				while (rs.next()) 
				{
					// Get the data from the row using the column index
					s = rs.getString(1);
				}
				if(Integer.parseInt(s) == 1) // the user is active
				{
					//gets the userlevel from the database
					sql = "SELECT user_level FROM users WHERE user_name = '" + uname + "';";
					pstmt = connection.prepareStatement(sql);
					rs = pstmt.executeQuery();
					s = "";
					while (rs.next()) 
					{
						// Get the data from the row using the column index
						s = rs.getString(1);
					}
					String userlevel = s;
					
					
					//send the user the authentication message
					authenticated = true;
					messagetco.putMessage("1" + userlevel); //authenticated
					System.out.println("User Authenticated: " + uname + " Userlevel " + userlevel);
					
					//set the last log in time for this user
					time timeobject = new time();
					String logon_time =  timeobject.GetCurrentYear_asInt() + "-" + timeobject.GetCurrentMonth_asInt() + "-" + timeobject.GetCurrentDay_asInt()+ "-" + timeobject.GetMilitaryTime_asString();
								
					
					sql = "UPDATE users SET last_login ='"+ logon_time + "' WHERE user_name = '" + uname + "';";
					pstmt = connection.prepareStatement(sql);
					pstmt.executeUpdate(); 
				}
				else
				{
					messagetco.putMessage("2");
				}
			}
			else
			{
				messagetco.putMessage("0"); // not authenticated
			}
	    }
	    catch (ClassNotFoundException e) 
	    {
	    	System.out.println("Exception " + e);
	        // Could not find the database driver
	    }
	    catch (SQLException e) 
	    {
	    	System.out.println("Exception " + e);
	        // Could not connect to the database
	    }
	}
	
	public int nextWorkOrderNumber()
	{
		//----------------------------------------------------------------------
		//set up the connection to the sql server.
		String sql;
		//connects to the database. 
		Connection connection = null;
		PreparedStatement pstmt;
		try 
	    {
	        // Load the JDBC driver
	        
	        Class.forName("org.gjt.mm.mysql.Driver");
	        
	        // Create a connection to the database
	        String serverName = "localhost";
	        String mydatabase = "Maginot";
	        
	        String url = "jdbc:mysql://" + serverName +  "/" + mydatabase; // a JDBC url
	        String username = "maginot"; //SQL username
	        String password = "dell001"; //SQL password for the above username.
	        connection = DriverManager.getConnection(url, username, password);
	        Statement stmt = connection.createStatement();
	        //----------------------------------------------------------
	    } 
	    catch (ClassNotFoundException e) 
	    {
	    	System.out.println("Exception " + e);
	        // Could not find the database driver
	    }
	    catch (SQLException e) 
	    {
	    	System.out.println("Exception " + e);
	        // Could not connect to the database
	    }
	    //----------------------------------------------------------------------
	    try
	    {
	    	sql = "SELECT work_order_number FROM workorders";
	    	pstmt = connection.prepareStatement(sql);
	    	ResultSet rs = pstmt.executeQuery();
	    	
	    	String workorderdata = "";
	    	int i = 0;
			while(rs.next())
			{
				
				workorderdata = workorderdata + rs.getString(1) +",";
				i++;
			}
			
			try
			{	//this will cause an exception if there is nothing in the string.
				//a slightly ghetto fix, but just remove the last character ";" off of the string before its sent. 
				workorderdata = workorderdata.substring(0,workorderdata.length()-1);
			}
			catch(Exception e)
			{
				System.out.println("String is null...aborting operation.");
			}
		
			//all the work order numbers are in the string, comma delimited.
			
			StringTokenizer tokenizer = new StringTokenizer(workorderdata,",");
			int numberofworkorders = tokenizer.countTokens();
			
			
			int ordernumbers[] = new int[numberofworkorders];
			for(int c = 0; c < numberofworkorders; c++)
				ordernumbers[c] = Integer.parseInt(tokenizer.nextToken());
				
			int highestnumber = 0;
			for(int c = 0; c < numberofworkorders; c++)
			{
				if(ordernumbers[c] > highestnumber)
				{
					highestnumber = ordernumbers[c];
				}
			}
			return highestnumber+1;
	    }

	    catch (SQLException e) 
	    {
	    	System.out.println("Exception " + e);
	        // Could not connect to the database
    	}
		return -1;
	}
	public int nextUserNumber()
	{
		//----------------------------------------------------------------------
		//set up the connection to the sql server.
		String sql;
		//connects to the database. 
		Connection connection = null;
		PreparedStatement pstmt;
		try 
	    {
	        // Load the JDBC driver
	        
	        Class.forName("org.gjt.mm.mysql.Driver");
	        
	        // Create a connection to the database
	        String serverName = "localhost";
	        String mydatabase = "Maginot";
	        
	        String url = "jdbc:mysql://" + serverName +  "/" + mydatabase; // a JDBC url
	        String username = "maginot"; //SQL username
	        String password = "dell001"; //SQL password for the above username.
	        connection = DriverManager.getConnection(url, username, password);
	        Statement stmt = connection.createStatement();
	        //----------------------------------------------------------
	    } 
	    catch (ClassNotFoundException e) 
	    {
	    	System.out.println("Exception " + e);
	        // Could not find the database driver
	    }
	    catch (SQLException e) 
	    {
	    	System.out.println("Exception " + e);
	        // Could not connect to the database
	    }
	    //----------------------------------------------------------------------
	    try
	    {
	    	sql = "SELECT user_id FROM users";
	    	pstmt = connection.prepareStatement(sql);
	    	ResultSet rs = pstmt.executeQuery();
	    	
	    	String workorderdata = "";
	    	int i = 0;
			while(rs.next())
			{
				
				workorderdata = workorderdata + rs.getString(1) +",";
				i++;
			}
			
			try
			{	//this will cause an exception if there is nothing in the string.
				//a slightly ghetto fix, but just remove the last character ";" off of the string before its sent. 
				workorderdata = workorderdata.substring(0,workorderdata.length()-1);
			}
			catch(Exception e)
			{
				System.out.println("String is null...aborting operation.");
			}
		
			//all the user id numbers are in the string, comma delimited.
			
			StringTokenizer tokenizer = new StringTokenizer(workorderdata,",");
			int numberofusers = tokenizer.countTokens();
			
			
			int usernumbers[] = new int[numberofusers];
			for(int c = 0; c < numberofusers; c++)
				usernumbers[c] = Integer.parseInt(tokenizer.nextToken());
				
			int highestnumber = 0;
			for(int c = 0; c < numberofusers; c++)
			{
				if(usernumbers[c] > highestnumber)
				{
					highestnumber = usernumbers[c];
				}
			}
			return highestnumber+1;
	    }

	    catch (SQLException e) 
	    {
	    	System.out.println("Exception " + e);
	        // Could not connect to the database
    	}
		return -1;
	}
	public int nextSiteNumber()
	{
			//----------------------------------------------------------------------
		//set up the connection to the sql server.
		String sql;
		//connects to the database. 
		Connection connection = null;
		PreparedStatement pstmt;
		try 
	    {
	        // Load the JDBC driver
	        
	        Class.forName("org.gjt.mm.mysql.Driver");
	        
	        // Create a connection to the database
	        String serverName = "localhost";
	        String mydatabase = "Maginot";
	        
	        String url = "jdbc:mysql://" + serverName +  "/" + mydatabase; // a JDBC url
	        String username = "maginot"; //SQL username
	        String password = "dell001"; //SQL password for the above username.
	        connection = DriverManager.getConnection(url, username, password);
	        Statement stmt = connection.createStatement();
	        //----------------------------------------------------------
	    } 
	    catch (ClassNotFoundException e) 
	    {
	    	System.out.println("Exception " + e);
	        // Could not find the database driver
	    }
	    catch (SQLException e) 
	    {
	    	System.out.println("Exception " + e);
	        // Could not connect to the database
	    }
	    //----------------------------------------------------------------------
	    try
	    {
	    	sql = "SELECT site_id FROM sites";
	    	pstmt = connection.prepareStatement(sql);
	    	ResultSet rs = pstmt.executeQuery();
	    	
	    	String workorderdata = "";
	    	int i = 0;
			while(rs.next())
			{
				
				workorderdata = workorderdata + rs.getString(1) +",";
				i++;
			}
			
			try
			{	//this will cause an exception if there is nothing in the string.
				//a slightly ghetto fix, but just remove the last character ";" off of the string before its sent. 
				workorderdata = workorderdata.substring(0,workorderdata.length()-1);
			}
			catch(Exception e)
			{
				System.out.println("String is null...aborting operation.");
			}
		
			//all the user id numbers are in the string, comma delimited.
			
			StringTokenizer tokenizer = new StringTokenizer(workorderdata,",");
			int numberofusers = tokenizer.countTokens();
			
			
			int usernumbers[] = new int[numberofusers];
			for(int c = 0; c < numberofusers; c++)
				usernumbers[c] = Integer.parseInt(tokenizer.nextToken());
				
			int highestnumber = 0;
			for(int c = 0; c < numberofusers; c++)
			{
				if(usernumbers[c] > highestnumber)
				{
					highestnumber = usernumbers[c];
				}
			}
			return highestnumber+1;
	    }

	    catch (SQLException e) 
	    {
	    	System.out.println("Exception " + e);
	        // Could not connect to the database
    	}
		return -1;
	}
	
	public Object[][] resultSetToObject(ResultSet RS)
	{
		try
		{
			//counts the number of rows
			int Y = 0; 
			while(RS.next())
			{
				Y++;
			}
			//gets the number of columns (X value).
			ResultSetMetaData md = RS.getMetaData();
	        int nColumns = md.getColumnCount();
	        //initalizes the object array
			Object object[][] = new Object[Y][nColumns];
			//resets the position in the RS
			RS.first();
			//puts the RS in the object
			int counter = 0;
			while(counter < Y)
			{
				
				for(int i=1; i<= nColumns; i++)
				{
					object[counter][i-1] = RS.getString(i);
				}
				counter++;
				RS.next();
			}
			return object;
		}
		catch (SQLException e)
	    {
	    	if(instruction_code == 3)
	    	{
	    		messagetco.putMessage("0");
	    	}
	    	System.out.println("Exception " + e);
	        // Could not connect to the database
	    }
		Object nonobj[][] = new Object[0][0];
		
		return nonobj; 
	}
	
	public void ensureMethods()
	{
		ensureDatabaseExists(); //makes sure the database maginot exists
		ensureOneUserExists(); // makes sure a default user exists if none do.
		ensureOneSiteExists(); //makes sure a default campus exists if none do.
		ensureStatusExists();//makes sure the status table exists.
	}
	public void ensureDatabaseExists()
	{
		//connects to the database mysql, which always exists in a mysql server
		Connection connection = null;
	    try 
	    {
	        // Load the JDBC driver
	        
	        Class.forName("org.gjt.mm.mysql.Driver");
	        String sql;
	        // Create a connection to the database
	        String serverName = "localhost";
	        String mydatabase = "mysql";
	        
	        String url = "jdbc:mysql://" + serverName +  "/" + mydatabase; // a JDBC url
	        String username = "maginot"; //SQL username
	        String password = "dell001"; //SQL password for the above username.
	        connection = DriverManager.getConnection(url, username, password);
	        Statement stmt = connection.createStatement();
	        PreparedStatement pstmt;
	        //----------------------------------------------------------
			sql = "CREATE DATABASE IF NOT EXISTS Maginot;";
			pstmt = connection.prepareStatement(sql);
			pstmt.executeUpdate();
	    } 
	    catch (ClassNotFoundException e) 
	    {
	    	System.out.println("Exception " + e);
	        // Could not find the database driver
	    }
	    catch (SQLException e) 
	    {
	    	System.out.println("Exception " + e);
	        // Could not connect to the database
	    }
		
	}
	public void ensureOneUserExists() //this is just incase there are NO users in the database, create a default
	{
		Connection connection = null;
	    try 
	    {
	        // Load the JDBC driver
	        
	        Class.forName("org.gjt.mm.mysql.Driver");
	        String sql;
	        // Create a connection to the database
	        String serverName = "localhost";
	        String mydatabase = "Maginot";
	        
	        String url = "jdbc:mysql://" + serverName +  "/" + mydatabase; // a JDBC url
	        String username = "maginot"; //SQL username
	        String password = "dell001"; //SQL password for the above username.
	        connection = DriverManager.getConnection(url, username, password);
	        Statement stmt = connection.createStatement();
	        PreparedStatement pstmt;
	        
	        //creates the table if it does not exist.
			sql = "CREATE TABLE IF NOT EXISTS users(user_id INTEGER NOT NULL PRIMARY KEY, user_name VARCHAR(30) NOT NULL, password VARCHAR(80) NOT NULL, campus VARCHAR(30) NOT NULL, user_level INTEGER NOT NULL,last_login VARCHAR(20),active INTEGER NOT NULL,bgcolor VARCHAR(20),txtcolor VARCHAR(20));";
			pstmt = connection.prepareStatement(sql);
			pstmt.executeUpdate();
	        //----------------------------------------------------------
			sql = "SELECT * FROM users;";
		    pstmt = connection.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			
			int count = 0;
			while(rs.next())
			{
				count++;
			}
			if(count  == 0)
			{
				//there are no users in the database.
				sql = "INSERT INTO users (user_id, user_name,password,campus,user_level,active) VALUES (0,'administrator','!#/ zW¥§C‰JJ€Ã','Information Technology ',0,1 );";
				pstmt = connection.prepareStatement(sql);
				pstmt.executeUpdate();
			}
			else
			{
				//there are users in the database.
			}
	    } 
	    catch (ClassNotFoundException e) 
	    {
	    	System.out.println("Exception " + e);
	        // Could not find the database driver
	    }
	    catch (SQLException e) 
	    {
	    	System.out.println("Exception " + e);
	        // Could not connect to the database
	    }
		//----------------------------------------------------------------	
	}
	public void ensureOneSiteExists() //this is here incase there are NO campuses in the database, create a default
	{
		Connection connection = null;
	    try 
	    {
	        // Load the JDBC driver
	        
	        Class.forName("org.gjt.mm.mysql.Driver");
	        String sql;
	        // Create a connection to the database
	        String serverName = "localhost";
	        String mydatabase = "Maginot";
	        
	        String url = "jdbc:mysql://" + serverName +  "/" + mydatabase; // a JDBC url
	        String username = "maginot"; //SQL username
	        String password = "dell001"; //SQL password for the above username.
	        connection = DriverManager.getConnection(url, username, password);
	        Statement stmt = connection.createStatement();
	        PreparedStatement pstmt;
	        
	        //creates the table if it does not exist.
			sql = "CREATE TABLE IF NOT EXISTS sites(site_id INTEGER PRIMARY KEY NOT NULL,site_name VARCHAR(50) NOT NULL,phone VARCHAR(20),vlan INTEGER NOT NULL);";
			pstmt = connection.prepareStatement(sql);
			pstmt.executeUpdate();
	        //----------------------------------------------------------
			sql = "SELECT * FROM sites;";
		    pstmt = connection.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			
			int count = 0;
			while(rs.next())
			{
				count++;
			}
			if(count  == 0)
			{
				//there are no users in the database.
				sql = "INSERT INTO sites (site_id,site_name,phone,vlan) VALUES (0,'Information Technology','555-555-5555',100);";
				pstmt = connection.prepareStatement(sql);
				pstmt.executeUpdate();
			}
			else
			{
				//there are sites in the database.
			}
	    } 
	    catch (ClassNotFoundException e) 
	    {
	    	System.out.println("Exception " + e);
	        // Could not find the database driver
	    }
	    catch (SQLException e) 
	    {
	    	System.out.println("Exception " + e);
	        // Could not connect to the database
	    }
		//----------------------------------------------------------------
	}
	public void ensureConfigExists()
	{
		Connection connection = null;
	    try 
	    {
	        // Load the JDBC driver
	        Class.forName("org.gjt.mm.mysql.Driver");
	        String sql;
	        // Create a connection to the database
	        String serverName = "localhost";
	        String mydatabase = "Maginot";
	        
	        String url = "jdbc:mysql://" + serverName +  "/" + mydatabase; // a JDBC url
	        String username = "maginot"; //SQL username
	        String password = "dell001"; //SQL password for the above username.
	        connection = DriverManager.getConnection(url, username, password);
	        Statement stmt = connection.createStatement();
	        PreparedStatement pstmt;
	        
	        //creates the table if it does not exist.
			sql = "CREATE TABLE IF NOT EXISTS email(property VARCHAR(30) NOT NULL PRIMARY KEY, config VARCHAR(5000));";
			pstmt = connection.prepareStatement(sql);
			pstmt.executeUpdate();
			//---------------------------------------------------------
			//this makes sure all the appropriate properties exist
			//----------------------------------------------------------
			sql = "SELECT property FROM email where property='mail.host';";
		    pstmt = connection.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			
			int count = 0;
			while(rs.next())
			{
				count++;
			}
			if(count  == 0)
			{
				System.out.println(count + " " + "inserting");
				//there are no users in the database.
				sql = "INSERT INTO email (property) VALUES ('mail.host');";
				pstmt = connection.prepareStatement(sql);
				pstmt.executeUpdate();
			}
			else
			{
				//there are users in the database.
			}
			//-----------------------------------------------------------
			sql = "SELECT property FROM email where property='mail.from'";
		    pstmt = connection.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			count = 0;
			while(rs.next())
			{
				count++;
			}
			if(count  == 0)
			{
				//there are no users in the database.
				sql = "INSERT INTO email (property) VALUES ('mail.from');";
				pstmt = connection.prepareStatement(sql);
				pstmt.executeUpdate();
			}
			else
			{
				//there are users in the database.
			}
			//-----------------------------------------------------------
			sql = "SELECT property FROM email where property='body'";
		    pstmt = connection.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			count = 0;
			while(rs.next())
			{
				count++;
			}
			if(count  == 0)
			{
				//there are no users in the database.
				sql = "INSERT INTO email (property) VALUES ('body');";
				pstmt = connection.prepareStatement(sql);
				pstmt.executeUpdate();
			}
			else
			{
				//there are users in the database.
			}
			//-----------------------------------------------------------
			sql = "SELECT property FROM email where property='subject'";
		    pstmt = connection.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			count = 0;
			while(rs.next())
			{
				count++;
			}
			if(count  == 0)
			{
				//there are no users in the database.
				sql = "INSERT INTO email (property) VALUES ('subject');";
				pstmt = connection.prepareStatement(sql);
				pstmt.executeUpdate();
			}
			else
			{
				//there are users in the database.
			}
			//-----------------------------------------------------------
			sql = "SELECT property FROM email where property='domain'";
		    pstmt = connection.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			count = 0;
			while(rs.next())
			{
				count++;
			}
			if(count  == 0)
			{
				//there are no users in the database.
				sql = "INSERT INTO email (property) VALUES ('domain');";
				pstmt = connection.prepareStatement(sql);
				pstmt.executeUpdate();
			}
			else
			{
				//there are users in the database.
			}
	    } 
	    catch (ClassNotFoundException e) 
	    {
	    	System.out.println("Exception " + e);
	        // Could not find the database driver
	    }
	    catch (SQLException e) 
	    {
	    	System.out.println("Exception " + e);
	        // Could not connect to the database
	    }
		//----------------------------------------------------------------	
	} //makes sure the configuration table for the email exists.
	public void ensureStatusExists() //makes sure the table for checking status exists.
	{
		System.out.println("ensureStatusExists");
		Connection connection = null;
	    try 
	    {
	        // Load the JDBC driver
	        
	        Class.forName("org.gjt.mm.mysql.Driver");
	        String sql;
	        // Create a connection to the database
	        String serverName = "localhost";
	        String mydatabase = "Maginot";
	        
	        String url = "jdbc:mysql://" + serverName +  "/" + mydatabase; // a JDBC url
	        String username = "maginot"; //SQL username
	        String password = "dell001"; //SQL password for the above username.
	        connection = DriverManager.getConnection(url, username, password);
	        Statement stmt = connection.createStatement();
	        PreparedStatement pstmt;
	        
	        //creates the table if it does not exist.
			sql = "CREATE TABLE IF NOT EXISTS status(uid INTEGER NOT NULL PRIMARY KEY,campus INTEGER,notes VARCHAR(500),status INTEGER);";
			pstmt = connection.prepareStatement(sql);
			pstmt.executeUpdate();
	    } 
	    catch (ClassNotFoundException e) 
	    {
	    	System.out.println("Exception " + e);
	        // Could not find the database driver
	    }
	    catch (SQLException e) 
	    {
	    	System.out.println("Exception " + e);
	        // Could not connect to the database
	    }
		//----------------------------------------------------------------
	}
}