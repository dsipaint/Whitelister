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
	
	/*
	 * v1.1 CHANGELOG:
	 * 	I wrote the code so that the table to store data can be chosen.
	 * 	this is mostly because I'm anticipating using and reusing this plugin
	 * 	for my database only, so I need to be able to configure the tables and set
	 *	new ones for each server this runs on. Josh tried to give me a database, but
	 *	the account perms were fucked up. This is the best long-term plan I have.
	 */
	
	public void onEnable()
	{	
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
			s.executeUpdate("create table if not exists " + utils.Data.tablename + "("
					+ "discord_id varchar(25),"
					+ "submitted_name varchar(25) not null"
					+ ")");
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		//add name listener for the discord bot
		jda.addEventListener(new NameListener(con, this));
		
		//turn on the whitelist
		getServer().setWhitelist(true);
		
		//add death listener for the server
		Bukkit.getPluginManager().registerEvents(new DeathListener(this, con), this);
	}
	
	@Override
	public void onDisable()
	{		
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
						<table>table name</table>
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
			Node tablename = database.item(11);
			
			Node channelid = list.item(3);
			
			//setting database values for login
			String urlstr = "jdbc:mysql://" + location.getFirstChild().getNodeValue() + ":" 
					+ port.getFirstChild().getNodeValue() + "/" + databasename.getFirstChild().getNodeValue()
					+ "?useSSL=false";
			String usrstr = username.getFirstChild().getNodeValue();
			String pwdstr = password.getFirstChild().getNodeValue();
			con = DriverManager.getConnection(urlstr, usrstr, pwdstr);
			
			//channel id is set here despite the method name- convenient, I know
			channel_id = channelid.getFirstChild().getNodeValue();
			//custom table name
			utils.Data.tablename = tablename.getFirstChild().getNodeValue();
		}
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch(SQLException e)
		{
			System.out.println("DEBUG: [Error Code] " + e.getErrorCode());
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
			return db.parse(new InputSource("./plugins/GeneralWhitelister/whitelisterdata.xml"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
}
