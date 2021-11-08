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
	public static int THREADS_PER_SCHEDULED_THREAD_POOL;
	public static int INSTANT_THREAD_POOL_COUNT;
	public static int THREADS_PER_INSTANT_THREAD_POOL;
	// Npc
	public static boolean SHOW_NPC_LEVEL;
	public static boolean SHOW_NPC_AGGRESSION;
	
	public static void load()
	{
		// Load server config file (if exists)
		final PropertiesParser serverConfig = new PropertiesParser(SERVER_CONFIG_FILE);
		
		// Game
		SERVER_HOST_NAME = serverConfig.getString("GameserverHostname", "*");
		SERVER_PORT = serverConfig.getInt("GameserverPort", 7777);
		CLIENT_PROTOCOL_VERSION = serverConfig.getInt("ClientProtocolVersion", 417);
		MAXIMUM_ONLINE_PLAYERS = serverConfig.getInt("MaximumOnlineUsers", 2000);
		// Login
		LOGIN_HOST_NAME = serverConfig.getString("LoginserverHostname", "*");
		EXTERNAL_HOST_NAME = serverConfig.getString("ExternalHostname", "127.0.0.1");
		if (EXTERNAL_HOST_NAME == null)
		{
			EXTERNAL_HOST_NAME = "localhost";
		}
		INTERNAL_HOST_NAME = serverConfig.getString("InternalHostname", "127.0.0.1");
		if (INTERNAL_HOST_NAME == null)
		{
			INTERNAL_HOST_NAME = "localhost";
		}
		AUTO_CREATE_ACCOUNTS = serverConfig.getBoolean("AutoCreateAccounts", true);
		// Other
		LOG_UNKNOWN_PACKETS = serverConfig.getBoolean("LogUnknownPackets", false);
		
		// Load rates config file (if exists)
		final PropertiesParser ratesConfig = new PropertiesParser(RATES_CONFIG_FILE);
		RATE_XP = ratesConfig.getFloat("RateXp", 1);
		RATE_SP = ratesConfig.getFloat("RateSp", 1);
		RATE_DROP = ratesConfig.getFloat("RateDrop", 1);
		RATE_ADENA = ratesConfig.getFloat("RateAdena", 1);
		
		// Load karma config file (if exists)
		final PropertiesParser karmaConfig = new PropertiesParser(KARMA_CONFIG_FILE);
		KARMA_MIN_KARMA = karmaConfig.getInt("KarmaMin", 240);
		KARMA_MAX_KARMA = karmaConfig.getInt("KarmaMax", 10000);
		KARMA_LOST_MULTIPLIER = karmaConfig.getFloat("KarmaLostMultiplier", 1);
		KARMA_DROP_CHANCE = karmaConfig.getInt("KarmaDropChance", 5);
		KARMA_PROTECTED_ITEMS = Arrays.stream(karmaConfig.getIntArray("KarmaProtectedItems", ";")).boxed().collect(Collectors.toList());
		
		// Load threadpool config file (if exists)
		final PropertiesParser threadpoolConfig = new PropertiesParser(THREADPOOL_CONFIG_FILE);
		SCHEDULED_THREAD_POOL_COUNT = threadpoolConfig.getInt("ScheduledThreadPoolCount", -1);
		if (SCHEDULED_THREAD_POOL_COUNT == -1)
		{
			SCHEDULED_THREAD_POOL_COUNT = Runtime.getRuntime().availableProcessors();
		}
		THREADS_PER_SCHEDULED_THREAD_POOL = threadpoolConfig.getInt("ThreadsPerScheduledThreadPool", 4);
		INSTANT_THREAD_POOL_COUNT = threadpoolConfig.getInt("InstantThreadPoolCount", -1);
		if (INSTANT_THREAD_POOL_COUNT == -1)
		{
			INSTANT_THREAD_POOL_COUNT = Runtime.getRuntime().availableProcessors();
		}
		THREADS_PER_INSTANT_THREAD_POOL = threadpoolConfig.getInt("ThreadsPerInstantThreadPool", 2);
		
		// Load NPC config file (if exists)
		final PropertiesParser npcConfig = new PropertiesParser(NPC_CONFIG_FILE);
		SHOW_NPC_LEVEL = npcConfig.getBoolean("ShowNpcLevel", false);
		SHOW_NPC_AGGRESSION = npcConfig.getBoolean("ShowNpcAggression", false);
	}
}
