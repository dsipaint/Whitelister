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
				//if user joined (presumably manually whitelisted) but isn't in the database
				if(!Data.isInTable(e.getPlayer().getName(), "submitted_name", utils.Data.tablename, con))
					con.createStatement().executeUpdate("insert into" + utils.Data.tablename + " (discord_id, submitted_name) values (null, \"" + e.getPlayer().getName() + "\")");
			}
			catch (SQLException e1)
			{
				e1.printStackTrace();
			}
	}
}
