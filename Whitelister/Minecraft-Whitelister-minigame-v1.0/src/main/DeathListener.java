package com.fin.main;
import java.sql.Connection;
import java.sql.SQLException;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import utils.Data;

public class DeathListener implements Listener
{
	/*
	 * joinListener will fix people's database entries when they
	 * join the server, if they were whitelisted by an admin and skipped the queue
	 */
	
	private Main plugin;
	private Connection con;
	
	public DeathListener(Main plugin, Connection con)
	{
		this.plugin = plugin;
		this.con = con;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e)
	{
			try
			{
				//if user joined (presumably manually whitelisted) but was still in the queue (note this sets a joining user's whitelist to true even if it already was
				if(Data.isInTable(e.getPlayer().getName(), "submitted_name", "WhitelistQueuedUsers", con))
					con.createStatement().executeUpdate("update WhitelistQueuedUsers set whitelisted = true where submitted_name = \"" + e.getPlayer().getName() + "\"");
				else //add them to database if not
					con.createStatement().executeUpdate("insert into WhitelistQueuedUsers (discord_id, submitted_name, whitelisted) values (null, \"" + e.getPlayer().getName() + "\", true)");
			}
			catch (SQLException e1)
			{
				e1.printStackTrace();
			}
	}
}
