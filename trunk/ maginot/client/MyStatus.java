import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.StringTokenizer;
import java.util.Arrays;


public class MyStatus extends JFrame implements ActionListener
{
	public static final int WIDTH = 300;
    public static final int HEIGHT = 300;
    TransferObject messagets,messagefs;
     private JComboBox campusbox,yesno;
     private JTextField notes;
    Color bgColor,txtColor;
	public MyStatus(TransferObject messagets, TransferObject messagefs)
	{
		this.messagets = messagets;
		this.messagefs = messagefs;
		getColors();
		setSize(WIDTH, HEIGHT);
        setTitle("People Status");
        Container contentPane = getContentPane( );
        contentPane.setLayout(new BorderLayout());
        
        //make sure this user exists in the table.
        String message = "123:";
        messagets.putMessage(message);
        
        JPanel panel = new JPanel();
        panel.setBackground(bgColor);
        panel.setLayout(new GridLayout(4,1));
        contentPane.add(panel,BorderLayout.NORTH);
        contentPane.setBackground(bgColor);
        
        JPanel flows[] = new JPanel[4];
        
        for(int c = 0; c < 4; c++)
        {
        	flows[c] = new JPanel();
        	flows[c].setBackground(bgColor);
        	flows[c].setLayout(new FlowLayout());
        	panel.add(flows[c]);
        }

       	String options[] = {"In","Out"};
       	yesno = new JComboBox(options);
       	JLabel yesnolabel = new JLabel("You are currently:");
       	yesnolabel.setForeground(txtColor);
       	flows[0].add(yesnolabel);
       	flows[0].add(yesno);
       	

       	JLabel noteslabel = new JLabel("Notes:");
       	noteslabel.setForeground(txtColor);
       	notes = new JTextField(20);
       	flows[1].add(noteslabel);
       	flows[1].add(notes);
       	
       	
       	//get a list of sites from the server
        message = "054:";
        messagets.putMessage(message);
        String campusoptions[] = messagefs.getMessage().toString().split(",");
        
        Arrays.sort(campusoptions);
        campusbox = new JComboBox(campusoptions);
       	JLabel campuslabel = new JLabel("You are at:");
       	campuslabel.setForeground(txtColor);
       	flows[2].add(campuslabel);
       	flows[2].add(campusbox);

       	JButton saveButton = new JButton("Save Changes");
       	saveButton.setForeground(txtColor);
       	saveButton.addActionListener(this);
       	flows[3].add(saveButton);
       
		this.setVisible(true);
	}
	public void getColors()
	{
		//gets the color stuff
		messagets.putMessage("142:");
		String temp_color = messagefs.getMessage().toString();
		
		int r = Integer.parseInt(temp_color.substring(0,temp_color.indexOf(",")));
		int g = Integer.parseInt(temp_color.substring(temp_color.indexOf(",")+1,temp_color.lastIndexOf(",")));
		int b = Integer.parseInt(temp_color.substring(temp_color.lastIndexOf(",")+1,temp_color.length()));
		
		bgColor = new Color(r,g,b);
		
		messagets.putMessage("143:");
		temp_color = messagefs.getMessage().toString();
		
		r = Integer.parseInt(temp_color.substring(0,temp_color.indexOf(",")));
		g = Integer.parseInt(temp_color.substring(temp_color.indexOf(",")+1,temp_color.lastIndexOf(",")));
		b = Integer.parseInt(temp_color.substring(temp_color.lastIndexOf(",")+1,temp_color.length()));
		
		txtColor = new Color(r,g,b);
	}
	public void actionPerformed(ActionEvent e)
	{
		String actionCommand = e.getActionCommand( );
		
		if(actionCommand.equals("Save Changes"))
		{
			String message = "056:" + campusbox.getSelectedItem().toString();
			messagets.putMessage(message);
			String data = messagefs.getMessage().toString();
			if(notes.getText().equals("")){notes.setText("null");}
			
			message = "122:" + yesno.getSelectedIndex() + "," + removeCharacters(notes.getText()) + "," + data;
			messagets.putMessage(message);
			
			if(notes.getText().equals("null")){notes.setText("");}
			
			this.dispose();
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
			if(data.charAt(c) == 92)
			{
				data = data.substring(0,c) + " " + data.substring(c+1,data.length());
				length = data.length();
			}
		}
		return data;
	}
}