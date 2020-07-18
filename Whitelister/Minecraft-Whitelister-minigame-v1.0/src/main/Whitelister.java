package com.fin.main;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import utils.Data;

public class Whitelister extends BukkitRunnable
{
	private Main plugin;
	private Random r;
	private Connection con;
	
	public Whitelister(Main plugin, Connection con)
	{
		this.plugin = plugin;
		r = new Random();
		this.con = con;
	}
	
	@Override public void run()
	{		
		//add player from database to the whitelist
		try
		{
			Statement s = con.createStatement();
			int data_size = Data.getResultSize(s.executeQuery("select * from WhitelistQueuedUsers where whitelisted = false"));
			if(data_size > 0)
			{
				int random = r.nextInt(data_size) + 1;
				ResultSet rs = con.createStatement().executeQuery("select discord_id, submitted_name "
						+ "from WhitelistQueuedUsers where whitelisted = false");
				rs.absolute(random);
				String username = rs.getString("submitted_name");
				String discID = rs.getString("discord_id");
				//whitelist them
				ConsoleCommandSender console = Bukkit.getConsoleSender();
				Bukkit.dispatchCommand(console, "whitelist add " + username);
				
				s.executeUpdate("update WhitelistQueuedUsers set whitelisted = true where submitted_name = \"" + username + "\"");
				Main.jda.getTextChannelById(Main.channel_id).getGuild().getMemberById(discID).getUser()
				.openPrivateChannel().queue( (channel) ->
				{
					channel.sendMessage("Your account " + username + " has been whitelisted!").queue();
				});
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}
