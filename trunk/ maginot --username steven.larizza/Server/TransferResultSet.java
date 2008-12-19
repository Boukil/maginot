//this transfers messages between the server and the client.

//this is sent to the in/out threads and the Operations thread. 
import java.sql.ResultSet;

public class TransferResultSet
{
	private ResultSet contents;
	private boolean empty = true;
	
	public synchronized void putMessage (ResultSet i)
	{ 
		while (empty == false) 
		{
			try 
			{ 
				wait(); 
			}
			catch (InterruptedException e) 
			{
			}
		}
		contents = i;
		empty = false;
		notify();
	}
	
	public synchronized ResultSet getMessage() 
	{
		while (empty == true)  
		{
			try 
			{ 	wait(); 
			}
			catch (InterruptedException e) 
			{
			}
		}
		empty = true;
		notify();
		ResultSet val = contents;
		return val;
  	}
}