import javax.swing.JTable;

public class PrintJTable
{
	public PrintJTable(JTable table)
	{
		try
		{
			table.print();
		}
		catch(Exception e)
		{
			
		}
	}
}