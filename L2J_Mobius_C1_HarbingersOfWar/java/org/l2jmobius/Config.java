/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.l2jmobius.util.PropertiesParser;

/**
 * @author Mobius
 */
public class Config
{
	// --------------------------------------------------
	// Constants
	// --------------------------------------------------
	public static final String EOL = System.lineSeparator();
	
	// --------------------------------------------------
	// Config File Definitions
	// --------------------------------------------------
	public static final String INTERFACE_CONFIG_FILE = "./config/interface.ini";
	public static final String SERVER_CONFIG_FILE = "./config/server.ini";
	private static final String RATES_CONFIG_FILE = "./config/rates.ini";
	private static final String KARMA_CONFIG_FILE = "./config/karma.ini";
	private static final String THREADPOOL_CONFIG_FILE = "./config/threadpool.ini";
	private static final String NPC_CONFIG_FILE = "./config/npc.ini";
	
	// Interface
	public static boolean ENABLE_GUI;
	public static boolean DARK_THEME;
	// Game
	public static int SERVER_PORT;
	public static String SERVER_HOST_NAME;
	public static int CLIENT_PROTOCOL_VERSION;
	public static int MAXIMUM_ONLINE_PLAYERS;
	// Login
	public static String LOGIN_HOST_NAME;
	public static String EXTERNAL_HOST_NAME;
	public static String INTERNAL_HOST_NAME;
	public static boolean AUTO_CREATE_ACCOUNTS;
	// Other
	public static boolean LOG_UNKNOWN_PACKETS;
	// Rates
	public static float RATE_XP;
	public static float RATE_SP;
	public static float RATE_DROP;
	public static float RATE_ADENA;
	// Karma
	public static int KARMA_MIN_KARMA;
	public static int KARMA_MAX_KARMA;
	public static float KARMA_LOST_MULTIPLIER;
	public static int KARMA_DROP_CHANCE;
	public static List<Integer> KARMA_PROTECTED_ITEMS;
	// ThreadPool
	public static int SCHEDULED_THREAD_POOL_COUNT;
	public static int INSTANT_THREAD_POOL_COUNT;
	// Npc
	public static boolean SHOW_NPC_LEVEL;
	public static boolean SHOW_NPC_AGGRESSION;
	
	public static void load()
	{
		// Load server config file (if exists)
		final PropertiesParser serverSettings = new PropertiesParser(SERVER_CONFIG_FILE);
		
		// Game
		SERVER_HOST_NAME = serverSettings.getString("GameserverHostname", "*");
		SERVER_PORT = serverSettings.getInt("GameserverPort", 7777);
		CLIENT_PROTOCOL_VERSION = serverSettings.getInt("ClientProtocolVersion", 417);
		MAXIMUM_ONLINE_PLAYERS = serverSettings.getInt("MaximumOnlineUsers", 2000);
		// Login
		LOGIN_HOST_NAME = serverSettings.getString("LoginserverHostname", "*");
		EXTERNAL_HOST_NAME = serverSettings.getString("ExternalHostname", "127.0.0.1");
		if (EXTERNAL_HOST_NAME == null)
		{
			EXTERNAL_HOST_NAME = "localhost";
		}
		INTERNAL_HOST_NAME = serverSettings.getString("InternalHostname", "127.0.0.1");
		if (INTERNAL_HOST_NAME == null)
		{
			INTERNAL_HOST_NAME = "localhost";
		}
		AUTO_CREATE_ACCOUNTS = serverSettings.getBoolean("AutoCreateAccounts", true);
		// Other
		LOG_UNKNOWN_PACKETS = serverSettings.getBoolean("LogUnknownPackets", false);
		
		// Load rates config file (if exists)
		final PropertiesParser ratesSettings = new PropertiesParser(RATES_CONFIG_FILE);
		RATE_XP = ratesSettings.getFloat("RateXp", 1);
		RATE_SP = ratesSettings.getFloat("RateSp", 1);
		RATE_DROP = ratesSettings.getFloat("RateDrop", 1);
		RATE_ADENA = ratesSettings.getFloat("RateAdena", 1);
		
		// Load karma config file (if exists)
		final PropertiesParser karmaSettings = new PropertiesParser(KARMA_CONFIG_FILE);
		KARMA_MIN_KARMA = karmaSettings.getInt("KarmaMin", 240);
		KARMA_MAX_KARMA = karmaSettings.getInt("KarmaMax", 10000);
		KARMA_LOST_MULTIPLIER = karmaSettings.getFloat("KarmaLostMultiplier", 1);
		KARMA_DROP_CHANCE = karmaSettings.getInt("KarmaDropChance", 5);
		KARMA_PROTECTED_ITEMS = Arrays.stream(karmaSettings.getIntArray("KarmaProtectedItems", ";")).boxed().collect(Collectors.toList());
		
		// Load threadpool config file (if exists)
		final PropertiesParser threadpoolSettings = new PropertiesParser(THREADPOOL_CONFIG_FILE);
		SCHEDULED_THREAD_POOL_COUNT = threadpoolSettings.getInt("ScheduledThreadPoolCount", 40);
		INSTANT_THREAD_POOL_COUNT = threadpoolSettings.getInt("InstantThreadPoolCount", 20);
		
		// Load NPC config file (if exists)
		final PropertiesParser npcSettings = new PropertiesParser(NPC_CONFIG_FILE);
		SHOW_NPC_LEVEL = npcSettings.getBoolean("ShowNpcLevel", false);
		SHOW_NPC_AGGRESSION = npcSettings.getBoolean("ShowNpcAggression", false);
	}
}
