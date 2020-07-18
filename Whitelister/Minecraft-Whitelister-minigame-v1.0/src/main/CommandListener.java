package com.fin.main;

import java.sql.Connection;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import utils.TimeManager;

public class CommandListener implements CommandExecutor
{
	private Main plugin;
	private Whitelister whitelister;
	private int whitelist_time;
	private Connection con;
	
	public CommandListener(Main plugin, Connection con)
	{
		whitelist_time = TimeManager.getTicks("30m"); //default set to 30m
		this.con = con;
		
		this.plugin = plugin;
		
		plugin.getCommand("settimer").setExecutor(this);
		plugin.getCommand("toggletimer").setExecutor(this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		//specifies a valid time
		if((!(sender instanceof Player) || sender.hasPermission("whitelister.settimer")) && label.equalsIgnoreCase("settimer"))
		{
			if(args.length >= 1)
			{
				whitelist_time = TimeManager.getTicks(args[0]);
				//time will be updated on next toggle anyway, if toggled off
				if(Main.isTimerOn)
				{
					whitelister.cancel(); //restart the whitelisting process with the new time
					whitelister = new Whitelister(plugin, con);//once the thread is cancelled a new one must be created
					whitelister.runTaskTimer(plugin, whitelist_time, whitelist_time);
				}
				
				if(args[0].matches("\\d+(s|m|h)"))
					sender.sendMessage(utils.Chat.format(plugin.getConfig().getString("whitelist_time_change_msg").replace("<time>", args[0])));
				else
					sender.sendMessage(utils.Chat.format(plugin.getConfig().getString("whitelist_time_change_err_msg")));
			}
			else
				sender.sendMessage(utils.Chat.format(plugin.getConfig().getString("whitelist_time_change_err_msg")));
							
			return true;
		}
		
		//toggles timer
		if((!(sender instanceof Player) || sender.hasPermission("whitelister.toggletimer")) && label.equalsIgnoreCase("toggletimer"))
		{
			Main.isTimerOn = !Main.isTimerOn;
			
			if(Main.isTimerOn)
			{
				whitelister = new Whitelister(plugin, con); //if this code is activated, the cancel method has already been called
				whitelister.runTaskTimer(plugin, whitelist_time, whitelist_time);
			}
			else
				whitelister.cancel();
				
			sender.sendMessage(utils.Chat.format(plugin.getConfig().getString("timer_toggle_msg").replace("<onoff>", Main.isTimerOn ? "ON":"OFF")));
			return true;
		}

		return false;
	}
}
