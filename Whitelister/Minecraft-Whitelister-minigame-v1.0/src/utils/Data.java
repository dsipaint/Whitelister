package utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Data
{
	public static int getResultSize(ResultSet rs)
	{
		int count = 0;
		
		try
		{
			while(rs.next())
				count++;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return count;
	}
	
	public static boolean isInTable(String value, String column, String table, Connection c)
	{
		try
		{
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery("select " + column + " from " + table + " where " + column + " = \"" + value + "\"");
			if(getResultSize(rs) != 0)
				return true;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
}
