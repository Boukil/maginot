import java.awt.*;
import javax.swing.*;
import java.awt.print.*;

class PrintComponent implements Printable
{
	private Component componentToBePrinted;
	
	public PrintComponent(Component componentToBePrinted) 
	{
		this.componentToBePrinted = componentToBePrinted;
		print();
	}
	public void print() 
	{
		PrinterJob printJob = PrinterJob.getPrinterJob();
		printJob.setPrintable(this);
		
		if (printJob.printDialog())
		{
			try 
			{
				printJob.print();
			} 
			catch(PrinterException pe) 
			{
				System.out.println("Error printing: " + pe);
			}
		}	
	}
	
	public int print(Graphics g, PageFormat pageFormat, int pageIndex) 
	{
		if (pageIndex > 0) 
		{
			return(NO_SUCH_PAGE);
		} 
		else 
		{
			Graphics2D g2d = (Graphics2D)g;
			g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
			disableDoubleBuffering(componentToBePrinted);
			componentToBePrinted.paint(g2d);
			enableDoubleBuffering(componentToBePrinted);
			return(PAGE_EXISTS);
		}
	}
	
	/** The speed and quality of printing suffers dramatically if
	*  any of the containers have double buffering turned on.
	*  So this turns if off globally.
	*  @see enableDoubleBuffering
	*/
	public static void disableDoubleBuffering(Component c) 
	{
		RepaintManager currentManager = RepaintManager.currentManager(c);
		currentManager.setDoubleBufferingEnabled(false);
	}
	
	/** Re-enables double buffering globally. */
	
	public static void enableDoubleBuffering(Component c) 
	{
		RepaintManager currentManager = RepaintManager.currentManager(c);
		currentManager.setDoubleBufferingEnabled(true);
	}
}