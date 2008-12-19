//this transfers Objects between the server and the client.

//this is sent to the in/out threads and the Operations thread. 

public class TransferObject
{
	private Object contents;
	private boolean empty = true;
	
	public synchronized void putMessage (Object i)
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
	
	public synchronized Object getMessage() 
	{
		while (empty == true)  
		{
			try 
			{ 	
				wait(); 
			}
			catch (InterruptedException e) 
			{
				
			}
		}
		empty = true;
		notify();
		Object val = contents;
		return val;
  	}
}