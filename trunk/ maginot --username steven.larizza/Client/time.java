import java.util.Calendar;
import java.util.StringTokenizer;
import java.text.SimpleDateFormat;
import java.io.*;
import javax.swing.JOptionPane;

public class time
{
	private String 	currentyear_string,
							currentmonth_string,
							currentday_string,
							currenttime,
							standardtime,
							TIME_ZONE;
			
	private  int 	currentyear_int,
						currentmonth_int,
						currentday_int,
						current_minute,
						current_second;
				
	
	TransferObject messagets,messagefs;
	
	
	public time(TransferObject messagets,TransferObject messagefs)
	{
		this.messagets = messagets;
		this.messagefs = messagefs;
		Calendar cal = Calendar.getInstance();
		//----------------------------------------------------------------------
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
		currentyear_string = sdf.format(cal.getTime());
		//----------------------------------------------------------------------
		sdf = new SimpleDateFormat("MMMMMMMMMMMMMM");
		currentmonth_string = sdf.format(cal.getTime());
		//----------------------------------------------------------------------
		sdf = new SimpleDateFormat("EEEEEEEEEE");
		currentday_string = sdf.format(cal.getTime());
		//----------------------------------------------------------------------
		sdf = new SimpleDateFormat("HH:mm:ss");
		currenttime = sdf.format(cal.getTime());
		//----------------------------------------------------------------------
		sdf = new SimpleDateFormat("dd");
		currentday_int = Integer.parseInt(sdf.format(cal.getTime()));
		//----------------------------------------------------------------------
		sdf = new SimpleDateFormat("zzz");
		TIME_ZONE = sdf.format(cal.getTime());
		//----------------------------------------------------------------------
		currentyear_int = Integer.parseInt(currentyear_string);
		//----------------------------------------------------------------------
		sdf = new SimpleDateFormat("M");
		currentmonth_int = Integer.parseInt(sdf.format(cal.getTime()));
		//----------------------------------------------------------------------
		sdf = new SimpleDateFormat("m");
		current_minute = Integer.parseInt(sdf.format(cal.getTime()));
		//----------------------------------------------------------------------
		sdf = new SimpleDateFormat("s");
		current_second = Integer.parseInt(sdf.format(cal.getTime()));	
		//----------------------------------------------------------------------
		sdf = new SimpleDateFormat("hh:mm:ss");
		standardtime = sdf.format(cal.getTime());
	}
	
	//date1 will is the date that is before date2
	//i.e. date1 is the start, date2 is the end.
	public String GetTimeDifference(String date1,String date2)//gets the difference between two dates.
	{
		
		//date2-date1
		int year1,month1,day1,hour1,minute1,second1;
		int year2,month2,day2,hour2,minute2,second2;
		int fyear,fmonth,fday,fhour,fminute,fsecond;
		
		//divide up the first date.
		
		StringTokenizer token1 = new StringTokenizer(date1,"-");
		year1 = Integer.parseInt(token1.nextToken());
		month1 = Integer.parseInt(token1.nextToken());
		day1 = Integer.parseInt(token1.nextToken());
		hour1 = Integer.parseInt(token1.nextToken());
		minute1 = Integer.parseInt(token1.nextToken());
		second1 = Integer.parseInt(token1.nextToken());
		
		StringTokenizer token2 = new StringTokenizer(date2,"-");
		year2 = Integer.parseInt(token2.nextToken());
		month2 = Integer.parseInt(token2.nextToken());
		day2 = Integer.parseInt(token2.nextToken());
		hour2 = Integer.parseInt(token2.nextToken());
		minute2 = Integer.parseInt(token2.nextToken());
		second2 = Integer.parseInt(token2.nextToken());
		
		if(second2<second1)//if borrow
		{
			minute2--;
			second2 = second2+60;
		}
		fsecond = second2-second1;
		
		if(minute2<minute1)
		{
			hour2--;
			minute2 = minute2+60;
		}
		fminute = minute2-minute1;
		
		if(hour2<hour1)
		{
			day2--;
			hour2 = hour2+24;
		}	
		fhour = hour2 - hour1;
		
		if(day2<day1)
		{
			day2 = day2 + GetDaysInMonth(month2,year2);
			month2--;
		}
		fday = hour2 - hour1;
		
		if(month2 < month1)
		{
			year2--;
			month2 = month2+12;
		}
		fmonth = month2 - month1;
		
		if(year2 < year1)
		{
			return "-1";
		}
		fyear = year2 - year1;
		
		return fyear + "-" + fmonth + "-" + fday + "-" + fhour + "-" + fminute + "-" + fsecond;
	}
	
	public int GetCurrentYear_asInt()
	{
		return currentyear_int;
	}
	public int GetCurrentMonth_asInt()
	{
		return currentmonth_int;
	}
	public int GetCurrentDay_asInt()
	{
		return currentday_int;
	}
	public String GetCurrentYear_asString()
	{
		return currentyear_string;
	}
	public String GetCurrentTime_asString()
	{
		return currenttime;
	}
	public String GetCurrentMonth_asString()
	{
		return currentmonth_string;
	}
	public String GetCurrentDay_asString()
	{
		return currentday_string;
	}
	public String GetDate()
	{
		return currentmonth_string + " " + currentday_int + " " + currentyear_string;
	}
	public int GetDaysInMonth(int month,int year)
	{
		if((month < 1) || (month >12))
		{
			return -1;
		}
		switch(month)
		{
			case 1:
				return 31;
			case 2:
				if((year%4) == 0)
				{
					if((year%100) ==0)
					{
						if((year%400) ==0)
						{
							return 29;
						}
						else
						{
							return 28;
						}
					}
					else
					{
						return 29;
					}
				}
				else
				{
					return 28;
				}
			case 3:
				return 31;
			case 4:
				return 30;
			case 5:
				return 31;
			case 6:
				return 30;
			case 7:
				return 31;
			case 8:
				return 31;
			case 9:
				return 30;
			case 10:
				return 31;
			case 11:
				return 30;
			case 12:
				return 31;	
		}
		return -1; //this is actually an unreachable statement :P
	}
	public String GetMilitaryTime_asString()
	{
		return currenttime;
	}
	public String GetStandardTime_asString()
	{
		return standardtime;
	}
}
