package com.fin.main;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;

import net.dv8tion.jda.api.entities.Message;
import utils.Data;

public class Whitelister extends BukkitRunnable
{
	
	private Connection con;
	private Message msg; 
	
	public Whitelister(Connection con, Message msg)
	{
		this.con = con;
		this.msg = msg;
	}
	
	@Override
	public void run()
	{
		String message = msg.getContentRaw();
		String[] arguments = message.split(" ");
		String userID = msg.getAuthor().getId();
		
		String name = arguments[1];
		
		try
		{
			//input sanitisation- only allowed a combination of letters, numbers or underscores
			if(!name.matches("[\\w|_]+"))
				return;
			
			//minecraft names fit this criteria
			if(name.length() < 3 || name.length() > 16)
				return;
			
			//only one instance of a username may exist in the table
			if(Data.isInTable(name, "submitted_name", utils.Data.tablename, con))
				return;
			
			Statement s = con.createStatement();
			ConsoleCommandSender console = Bukkit.getConsoleSender();
			
			//if discord user is in database already
			if(Data.isInTable(userID, "discord_id", utils.Data.tablename, con))
			{
				ResultSet rs = s.executeQuery("select submitted_name from " + utils.Data.tablename + " where discord_id = \"" + userID + "\"");
				rs.absolute(1);
				
				//remove old name from whitelist
				String old_username = rs.getString("submitted_name");
				Bukkit.dispatchCommand(console,  "whitelist remove " + old_username);
				
				//update name
				s.executeUpdate("update " + utils.Data.tablename + " set submitted_name = \"" + name + "\" where discord_id = \"" + userID + "\"");
			}
			else //if not
				s.executeUpdate("insert into " + utils.Data.tablename + " (discord_id, submitted_name) values (\"" + userID + "\", \"" + name + "\")");
			
			//add name to whitelist
			Bukkit.dispatchCommand(console,  "whitelist add " + name);
			
			msg.getAuthor().openPrivateChannel().queue( (channel) ->
			{
				channel.sendMessage("Your username " + name + " has been whitelisted!").queue();
			});
		}
		catch (SQLException e2)
		{
			e2.printStackTrace();
		}
		
		this.cancel();
	}
	
}
