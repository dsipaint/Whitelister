package utils;

public class TimeManager
{
	public static int getTicks(String arg)
	{
		int timeunit = 0;
		String[] args = getTimeArgs(arg);
		
		if(args != null)
		{
			int num = Integer.parseInt(args[0]);

			switch(args[1])
			{				
				case "s":
					timeunit = 20; //20 ticks in a second
					break;
					
				case "m":
					timeunit = 60*20; //60s per minute
					break;
				
				case "h":
					timeunit = 60*60*20; //60m per hour
					break;
			}
			
			return num*timeunit;
		}
		
		return 30*60*20; //default is 30m
	}
	
	public static String[] getTimeArgs(String s)
	{
		
		if(s.matches("\\d+(s|m|h)"))
		{
			String[] time_args = new String[2];
			int i = 0;
			for(; i < s.length(); i++)
			{
				if(Character.toString(s.charAt(i)).matches("[a-z]"))
					break;
			}
			
			time_args[0] = s.substring(0, i);
			time_args[1] = s.substring(i);
			
			return time_args;
		}
		else
			return null;
	}
}
