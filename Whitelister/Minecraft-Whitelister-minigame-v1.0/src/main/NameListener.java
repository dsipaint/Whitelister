package com.fin.main;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import utils.Data;

public class NameListener extends ListenerAdapter
{
	/*
	 * Listens for names to be submitted for the plugin
	 * in a specific channel (one account per user)
	 */
	
	private Connection con;
	
	public NameListener(Connection con)
	{
		this.con = con;
	}
	
	public void onGuildMessageReceived(GuildMessageReceivedEvent e)
	{
		String msg = e.getMessage().getContentRaw();
		String[] arguments = msg.split(" ");
		String userID = e.getAuthor().getId();
		
		//>add (in the right channel)
		if(e.getChannel().getId().equals(Main.channel_id) && arguments[0].equalsIgnoreCase(Main.PREFIX + "add") && arguments.length == 2)
		{	
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
				if(Data.isInTable(name, "submitted_name", "WhitelistQueuedUsers", con))
					return;
				
				Statement s = con.createStatement();
				
				//if discord user is in database already
				if(Data.isInTable(userID, "discord_id", "WhitelistQueuedUsers", con))
				{
					ResultSet rs = s.executeQuery("select whitelisted from WhitelistQueuedUsers where discord_id = \"" + userID + "\"");
					rs.absolute(1);
					boolean isAlreadyWhitelisted = rs.getBoolean("whitelisted");
					
					//only let them submit a name if they are not already whitelisted- this stops submission of alts
					if(!isAlreadyWhitelisted)
						s.executeUpdate("update WhitelistQueuedUsers set submitted_name = \"" + name + "\" where discord_id = \"" + userID + "\"");
				}
				else //if not
					s.executeUpdate("insert into WhitelistQueuedUsers (discord_id, submitted_name) values (\"" + userID + "\", \"" + name + "\")");
				
				e.getAuthor().openPrivateChannel().queue( (channel) ->
				{
					channel.sendMessage("Your username has been submitted! Keep an eye on our messages to know when you've been whitelisted!").queue();
				});
			}
			catch (SQLException e2)
			{
				e2.printStackTrace();
			}
		}
	}
}
