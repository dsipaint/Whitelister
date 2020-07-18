package com.fin.main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import javax.security.auth.login.LoginException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class Main extends JavaPlugin
{
	static JDA jda;
	static Connection con;
	static final String PREFIX = ">";
	static String channel_id;
	static boolean isTimerOn;
	Whitelister whitelister;
	static final String BOT_STARTUP_MESSAGE = "===============================================================================================================\r\n" + 
			"\r\n" + 
			"***To Join the event, Type >add {YOUR USERNAME} in this channel!\r\n" + 
			"If you make a mistake, Please do the command again! It will overwrite your existing name.\r\n***" + 
			"\r\n" + 
			"```IP= event.sootmc.com```\r\n" + 
			"\r\n" + 
			"1.14.4\r\n" + 
			"\r\n" + 
			"===============================================================================================================";
	
	public void onEnable()
	{
		isTimerOn = false;
		
		try
		{
			jda = new JDABuilder(AccountType.BOT).setToken("").build();
		}
		catch (LoginException e)
		{
			e.printStackTrace();
		}
		
		try
		{
			jda.awaitReady();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		
		//set up SQL
		con = getConnection();
		try
		{
			Statement s = con.createStatement();
			//setup queued users table
			s.executeUpdate("drop table if exists WhitelistQueuedUsers");
			s.executeUpdate("create table WhitelistQueuedUsers ("
					+ "discord_id varchar(25),"
					+ "submitted_name varchar(25) not null,"
					+ "whitelisted tinyint(1) default false"
					+ ")");
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		//add name listener for the discord bot
		jda.addEventListener(new NameListener(con));
		
		//turn on the whitelist
		getServer().setWhitelist(true);
		
		//add death listener for the server
		Bukkit.getPluginManager().registerEvents(new DeathListener(this, con), this);
		
		//add commands
		new CommandListener(this, con);
		
		jda.getTextChannelById(channel_id).sendMessage(BOT_STARTUP_MESSAGE).queue();
	}
	
	@Override
	public void onDisable()
	{
		//remove everyone from the whitelist
		Set<OfflinePlayer> players = getServer().getWhitelistedPlayers();
		
		for(OfflinePlayer p : players)
			p.setWhitelisted(false);
		
		//turn the whitelist off
		getServer().setWhitelist(false);
		
		//empty SQL database
		try
		{
			con.createStatement().executeUpdate("delete from WhitelistQueuedUsers");
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		jda.getTextChannelById(channel_id).sendMessage("**ign submissions are "
				+ "temporarily closed- the game is either resetting or is closed**").queue();
		//stop the discord bot
		jda.shutdown();
	}
	
	private Connection getConnection()
	{
		Connection con = null;
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			
			/*
			 Here is how manhuntdata.xml should look
			 <?xml version = '1.0'?>
				
				<game>
					<database>
						<username>SQL username</username>
						<password>SQL password</password>
						<location>database url</location>
						<port>3306</port>
						<databasename>database name</databasename>
					</database>
	
					<channelid>discord channel id</channelid>
				</game>
			 */
			
			//getting xml elements
			Document doc = getDocument();
			Element game = doc.getDocumentElement();
			NodeList list = game.getChildNodes();
			NodeList database = list.item(1).getChildNodes();
			Node username = database.item(1);
			Node password = database.item(3);
			Node port = database.item(5);
			Node location = database.item(7);
			Node databasename = database.item(9);
			
			Node channelid = list.item(3);
			
			//setting database values for login
			String urlstr = "jdbc:mysql://" + location.getFirstChild().getNodeValue() + ":" + port.getFirstChild().getNodeValue() + "/" + databasename.getFirstChild().getNodeValue();
			String usrstr = username.getFirstChild().getNodeValue();
			String pwdstr = password.getFirstChild().getNodeValue();
			con = DriverManager.getConnection(urlstr, usrstr, pwdstr);
			
			//channel id is set here despite the method name- convenient, I know
			channel_id = channelid.getFirstChild().getNodeValue();
		}
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		return con;
	}
	
	private Document getDocument()
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringComments(true);
			factory.setIgnoringElementContentWhitespace(true);
			DocumentBuilder db = factory.newDocumentBuilder();
			return db.parse(new InputSource("./plugins/whitelisterdata.xml"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
}
