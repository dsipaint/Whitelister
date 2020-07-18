package com.fin.main;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

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
	private Main plugin;
	
	public NameListener(Connection con, Main plugin)
	{
		this.con = con;
		this.plugin = plugin;
	}
	
	public void onGuildMessageReceived(GuildMessageReceivedEvent e)
	{
		String msg = e.getMessage().getContentRaw();
		String[] arguments = msg.split(" ");
		
		//>add (in the right channel)
		if(e.getChannel().getId().equals(Main.channel_id) && arguments[0].equalsIgnoreCase(Main.PREFIX + "whitelist") && arguments.length == 2)
			new Whitelister(con, e.getMessage()).runTask(plugin);
	}
}
