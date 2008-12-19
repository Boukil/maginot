//this transfers messages between the server and the client.

//this is sent to the in/out threads and the Operations thread. 

public class TransferString
{
	private String contents;
	private boolean empty = true;
	
	public synchronized void putMessage (String i)
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
	
	public synchronized String getMessage() 
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
		String val = contents;
		return val;
  	}
}