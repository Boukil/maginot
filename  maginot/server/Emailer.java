import java.util.*;
import java.io.*;
import javax.mail.*;
import javax.mail.internet.*;


public final class Emailer 
{
	/**
	* Send a single email.
	*/
	public void sendEmail(String aFromEmailAddr, String aToEmailAddr,String aSubject, String aBody,String aSmtpHost)
	{
		//Here, no Authenticator argument is used (it is null).
		//Authenticators are used to prompt the user for user
		//name and password.
		
		Properties a = new Properties();
		a.setProperty("mail.host",aSmtpHost);
		Session session = Session.getDefaultInstance( a , null );
		MimeMessage message = new MimeMessage( session );
		try 
		{
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(aToEmailAddr));
			message.setSubject(aSubject);
			message.setFrom(new InternetAddress(aFromEmailAddr));
			message.setText(aBody);
			Transport.send(message);
		}
		catch (MessagingException ex)
		{
			System.err.println("Cannot send email. " + ex);
		}
	}
} 

