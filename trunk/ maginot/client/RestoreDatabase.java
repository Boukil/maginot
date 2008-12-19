import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;

public class RestoreDatabase 
{
	 TransferObject messagefs,messagets;
	String path;
	
	//put the table name here, and put the SQL command to create it in the same index in the other array.
	String tablestoget[] = {"sites","workorders","users","email"};
	String tableproperties[] =
	{"site_id INTEGER PRIMARY KEY NOT NULL,site_name VARCHAR(50) NOT NULL,phone VARCHAR(20),vlan INTEGER NOT NULL",
	 "WORK_ORDER_NUMBER INTEGER NOT NULL PRIMARY KEY, ECISD_NUMBER INTEGER, STATUS INTEGER NOT NULL, PROBLEM VARCHAR(10000) NOT NULL, RESOLUTION VARCHAR(10000), CAMPUS INTEGER NOT NULL, OPENED_BY INTEGER NOT NULL, CLOSED_BY INTEGER,MODEL VARCHAR(500),DATE_OPEN VARCHAR(20) NOT NULL, DATE_CLOSED VARCHAR(20),FIRST_NAME VARCHAR(200) NOT NULL, LAST_NAME VARCHAR(200) NOT NULL, ROOM_NUMBER VARCHAR(50), PHONE VARCHAR(100), priority INTEGER, SERVICE_TAG VARCHAR(30),ASSIGNED_TO VARCHAR(30) NOT NULL,FOLLOW_UP_SENT boolean,DATE_FOLLOW_UP_SENT VARCHAR(30),TIME_TO_COMPLETION VARCHAR(30)",
	 "USER_ID INTEGER NOT NULL PRIMARY KEY, USER_NAME VARCHAR(30) NOT NULL, PASSWORD VARCHAR(80) NOT NULL, CAMPUS VARCHAR(30) NOT NULL, USER_LEVEL INTEGER NOT NULL,LAST_LOGIN VARCHAR(20),ACTIVE INTEGER NOT NULL,bgcolor VARCHAR(20),txtcolor VARCHAR(20)",
	 "property VARCHAR(30) NOT NULL PRIMARY KEY, config VARCHAR(5000)"	
	};
	String insertionproperties[] = 
	{"site_id, site_name,phone,vlan",
	 "WORK_ORDER_NUMBER,ECISD_NUMBER,STATUS,PROBLEM,RESOLUTION,CAMPUS,OPENED_BY,CLOSED_BY,MODEL,DATE_OPEN,DATE_CLOSED,FIRST_NAME,LAST_NAME,ROOM_NUMBER,PHONE,priority,SERVICE_TAG,ASSIGNED_TO,FOLLOW_UP_SENT,DATE_FOLLOW_UP_SENT,TIME_TO_COMPLETION",
	 "USER_ID,USER_NAME,PASSWORD,CAMPUS,USER_LEVEL,LAST_LOGIN,ACTIVE,bgcolor,txtcolor",
	 "property,config"
	};
	
    public RestoreDatabase(TransferObject messagets,TransferObject messagefs) 
    {
    	path = JOptionPane.showInputDialog(null,"Enter the path for the database file\n(do not include a file name, just the location) \n If the dump is to be in a subfolder, that folder must exist.");
		if(path != null)
		{
			path = escapeCharacters(path);
    	
	    	JProgressBar progress = new JProgressBar(0,100);
	    	for(int f = 0; f < tablestoget.length; f++)
			{
	    		//step 1: drop the table we're restoring.
	    		messagets.putMessage("082:" + tablestoget[f]);
	    		//step 2: recreate the table.
	    		messagets.putMessage("083:" + tablestoget[f] + "," + tableproperties[f]);
	    		
	    		//step 3: read the file of the table we just dropped, and send the work order back to the server.
	    		try
	    		{
	    			int lines = countLines(tablestoget[f]);
	    			String filename = path + tablestoget[f] + ".maginot";
		    		BufferedReader inputStream = new BufferedReader(new FileReader(filename));
		    		
		    		for(int c = 0; c < lines; c++)
		    		{
		    			String line = inputStream.readLine();
		    			
		    			while(line.charAt(line.length()-1) != 41)
		    			{
		    				line = line + inputStream.readLine();
		    				c++;
		    			}
		    			line = line.substring(1,line.length()-1); //takes out the () in the line.
		    			
		    			messagets.putMessage("084:" + tablestoget[f] + " (" + insertionproperties[f] + ") VALUES (" + insertQuotes(line) + ")");
		    		}
		    		inputStream.close();
	    		}
		    	catch(FileNotFoundException e)
		    	{
		    		JOptionPane.showMessageDialog(null, "Error reading from saved text. \n Error Code 019", "Error", JOptionPane.ERROR_MESSAGE);
		    	}
		    	catch(IOException e)
		    	{
		    		JOptionPane.showMessageDialog(null, "Error reading from saved text. \n IO Exception \nError Code 020", "Error", JOptionPane.ERROR_MESSAGE);
		    	}
			}
			JOptionPane.showMessageDialog(null,"Restore Completed");
		}
		
    }
    
    public int countLines(String filename)
    {
    	filename=path + filename+ ".maginot";
    	
    	int numberoflines = 0;
   		try
    	{
    		BufferedReader inputStream = new BufferedReader(new FileReader(filename));
    		String tempstring = null;
    		while(inputStream.readLine() != null)
    		{
    			numberoflines++;
    		}
    		inputStream.close();
    	}
    	
    	catch(FileNotFoundException e)
    	{
    		JOptionPane.showMessageDialog(null, "The File was not found! \n Make sure your input file exists. \n The program will now exit.", "Error", JOptionPane.ERROR_MESSAGE);
			
			// makes the thread stop if the file cannot be found. 
			try
			{
				System.exit(0);	
			}
			catch(Exception g)
			{
			}
    	}
    	catch(IOException e)
    	{
    		JOptionPane.showMessageDialog(null, "Error reading from saved text.\n The Program will now exit.", "Error", JOptionPane.ERROR_MESSAGE);
    		System.exit(0);
    	}
    	return numberoflines;
    }
    
    public String insertQuotes(String query)
    {

		//figure out if the part of the string in commas is a int, if it isn't, wrap it in '
		//if it is, leave it alone.
		StringBuffer result = new StringBuffer();
		String parts[] = query.split(",");
		for(int c = 0; c < parts.length; c++)
		{
			try
			{
				int junk = Integer.parseInt(parts[c]);//if this succeeds, do nothing to it.
				result.append(parts[c]);
			}
			catch(Exception e)
			{
				//if it fails, that means its not an int, so put quotes around it. 
				if(!parts[c].equals("null"))
			 		result.append("\'" + parts[c] + "\'");
			 	else
			 		result.append("null");
			}
			if(c < (parts.length-1))
				result.append(",");
		}
		System.out.println(result.toString());
    	return result.toString();
    }
    
    
    public String escapeCharacters(String data)
	{
		int length = data.length();
		
		for(int c = 0; c < length; c ++)
		{
			if(data.charAt(c) == 92)
			{
				data = data.substring(0,c) +"\\" + data.substring(c+1,data.length());
				length = data.length();
			}
		}
		return data;
	}
}