import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;


public class BackupDatabase
{
	 TransferObject messagefs,messagets;
	String path;
	int X,Y;
	
	//if a table is ever added to the program, all the needs to be done is for the name to be added to this array. 
	//the table will then be dumped with the rest.
	//RestoreDatabase will also have to be updated.
	String tablestoget[] = {"sites","workorders","users","email"};
	
	public BackupDatabase(TransferObject messagets,TransferObject messagefs)
	{
		this.messagefs = messagefs;
		this.messagets = messagets;
		path = JOptionPane.showInputDialog(null,"Enter the path for the database file\n(do not include a file name, just the location) \n If the dump is to be in a subfolder, that folder must exist.");
		
		if(path != null)
		{
			path = escapeCharacters(path);
			//------------------------------------------------------------------------------------------------------------------------------
			
			for(int f = 0; f < tablestoget.length; f++)
			{
				messagets.putMessage("081:" + tablestoget[f]);//requests a copy of the table sites.
				String data = messagefs.getMessage().toString();
				
				X = Integer.parseInt(data.substring(0,data.indexOf(",")));
				Y = Integer.parseInt(data.substring(data.indexOf(",") +1,data.length()));
				
				Object table[][] = new Object[X][Y];
				for(int c = 0; c < X; c++)
				{
					for(int d = 0; d < Y; d++)
					{
						//we don't want to save the user's password.(there may be issues with some password hashes). Reset all the passwods to ecisd.
						if((f==2) && (d==2))
						{
							String junk = messagefs.getMessage().toString();
							table[c][d] = "!#/ zW¥§C‰JJ€Ã";
						}
						else
						{
							table[c][d] = messagefs.getMessage();
						}
					}
				}
				writeTable(table,tablestoget[f]);
			}
			
			//----------------------------------------------------------------------------------------------------------------------------
			JOptionPane.showMessageDialog(null,"Database backup completed.");
		}
		
		
	}
	
	public void writeTable(Object table[][],String name)
	{
		try
		{
			PrintWriter outputStream = new PrintWriter(new FileOutputStream(path + "\\" + name + ".maginot"));
			for(int c = 0; c < X; c++)
			{
				outputStream.print("(");
				for(int d = 0; d < Y; d++)
				{
					try
					{
						outputStream.print(removeLineBreaks(table[c][d].toString()));
					}
					catch(NullPointerException f)
					{
						outputStream.print("null");
					}
					if(d < (Y-1))
					{
						outputStream.print(",");
					}
				}
				outputStream.print(")");
				if(c < (X-1))
					outputStream.println("");
				
			}
			outputStream.close();
		}
		catch(FileNotFoundException e)
		{
			System.out.println("Exception:" + e);
			// we don't need anything here because we will never get a file not found
			// because the file is created in the try block. 
		}
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
	public String removeLineBreaks(String data)
	{
		for(int c = 0; c<data.length(); c++)
		{
			if(data.charAt(c) == 10)
			{
				String first = data.substring(0,c);
				String last = data.substring(c+1,data.length());
				data = first + " " + last;
				c=0;//start over to make sure. Once all the line breaks are out of the line, it will stop starting over.
			}
		}
		return data;
	}
	
}