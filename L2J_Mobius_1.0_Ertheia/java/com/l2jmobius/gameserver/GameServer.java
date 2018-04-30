/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jmobius.gameserver;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.Server;
import com.l2jmobius.commons.concurrent.ThreadPool;
import com.l2jmobius.commons.database.DatabaseFactory;
import com.l2jmobius.commons.util.DeadLockDetector;
import com.l2jmobius.gameserver.cache.HtmCache;
import com.l2jmobius.gameserver.data.sql.impl.AnnouncementsTable;
import com.l2jmobius.gameserver.data.sql.impl.CharNameTable;
import com.l2jmobius.gameserver.data.sql.impl.CharSummonTable;
import com.l2jmobius.gameserver.data.sql.impl.ClanTable;
import com.l2jmobius.gameserver.data.sql.impl.CrestTable;
import com.l2jmobius.gameserver.data.sql.impl.OfflineTradersTable;
import com.l2jmobius.gameserver.data.xml.impl.AbilityPointsData;
import com.l2jmobius.gameserver.data.xml.impl.ActionData;
import com.l2jmobius.gameserver.data.xml.impl.AdminData;
import com.l2jmobius.gameserver.data.xml.impl.AlchemyData;
import com.l2jmobius.gameserver.data.xml.impl.AppearanceItemData;
import com.l2jmobius.gameserver.data.xml.impl.ArmorSetsData;
import com.l2jmobius.gameserver.data.xml.impl.BeautyShopData;
import com.l2jmobius.gameserver.data.xml.impl.BuyListData;
import com.l2jmobius.gameserver.data.xml.impl.CategoryData;
import com.l2jmobius.gameserver.data.xml.impl.ClanHallData;
import com.l2jmobius.gameserver.data.xml.impl.ClanRewardData;
import com.l2jmobius.gameserver.data.xml.impl.ClassListData;
import com.l2jmobius.gameserver.data.xml.impl.CombinationItemsData;
import com.l2jmobius.gameserver.data.xml.impl.CubicData;
import com.l2jmobius.gameserver.data.xml.impl.DoorData;
import com.l2jmobius.gameserver.data.xml.impl.EnchantItemData;
import com.l2jmobius.gameserver.data.xml.impl.EnchantItemGroupsData;
import com.l2jmobius.gameserver.data.xml.impl.EnchantItemHPBonusData;
import com.l2jmobius.gameserver.data.xml.impl.EnchantItemOptionsData;
import com.l2jmobius.gameserver.data.xml.impl.EnchantSkillGroupsData;
import com.l2jmobius.gameserver.data.xml.impl.EventEngineData;
import com.l2jmobius.gameserver.data.xml.impl.ExperienceData;
import com.l2jmobius.gameserver.data.xml.impl.ExtendDropData;
import com.l2jmobius.gameserver.data.xml.impl.FakePlayerData;
import com.l2jmobius.gameserver.data.xml.impl.FishingData;
import com.l2jmobius.gameserver.data.xml.impl.HennaData;
import com.l2jmobius.gameserver.data.xml.impl.HitConditionBonusData;
import com.l2jmobius.gameserver.data.xml.impl.InitialEquipmentData;
import com.l2jmobius.gameserver.data.xml.impl.InitialShortcutData;
import com.l2jmobius.gameserver.data.xml.impl.ItemCrystallizationData;
import com.l2jmobius.gameserver.data.xml.impl.KarmaData;
import com.l2jmobius.gameserver.data.xml.impl.LuckyGameData;
import com.l2jmobius.gameserver.data.xml.impl.MultisellData;
import com.l2jmobius.gameserver.data.xml.impl.NpcData;
import com.l2jmobius.gameserver.data.xml.impl.OptionData;
import com.l2jmobius.gameserver.data.xml.impl.PetDataTable;
import com.l2jmobius.gameserver.data.xml.impl.PetSkillData;
import com.l2jmobius.gameserver.data.xml.impl.PlayerTemplateData;
import com.l2jmobius.gameserver.data.xml.impl.PlayerXpPercentLostData;
import com.l2jmobius.gameserver.data.xml.impl.PrimeShopData;
import com.l2jmobius.gameserver.data.xml.impl.RecipeData;
import com.l2jmobius.gameserver.data.xml.impl.ResidenceFunctionsData;
import com.l2jmobius.gameserver.data.xml.impl.SayuneData;
import com.l2jmobius.gameserver.data.xml.impl.SecondaryAuthData;
import com.l2jmobius.gameserver.data.xml.impl.ShuttleData;
import com.l2jmobius.gameserver.data.xml.impl.SiegeScheduleData;
import com.l2jmobius.gameserver.data.xml.impl.SkillData;
import com.l2jmobius.gameserver.data.xml.impl.SkillTreesData;
import com.l2jmobius.gameserver.data.xml.impl.SpawnsData;
import com.l2jmobius.gameserver.data.xml.impl.StaticObjectData;
import com.l2jmobius.gameserver.data.xml.impl.TeleportersData;
import com.l2jmobius.gameserver.data.xml.impl.TransformData;
import com.l2jmobius.gameserver.data.xml.impl.VariationData;
import com.l2jmobius.gameserver.datatables.BotReportTable;
import com.l2jmobius.gameserver.datatables.EventDroplist;
import com.l2jmobius.gameserver.datatables.ItemTable;
import com.l2jmobius.gameserver.geoengine.GeoEngine;
import com.l2jmobius.gameserver.handler.ConditionHandler;
import com.l2jmobius.gameserver.handler.EffectHandler;
import com.l2jmobius.gameserver.handler.SkillConditionHandler;
import com.l2jmobius.gameserver.idfactory.IdFactory;
import com.l2jmobius.gameserver.instancemanager.AirShipManager;
import com.l2jmobius.gameserver.instancemanager.AntiFeedManager;
import com.l2jmobius.gameserver.instancemanager.BoatManager;
import com.l2jmobius.gameserver.instancemanager.CastleManager;
import com.l2jmobius.gameserver.instancemanager.CastleManorManager;
import com.l2jmobius.gameserver.instancemanager.ClanEntryManager;
import com.l2jmobius.gameserver.instancemanager.ClanHallAuctionManager;
import com.l2jmobius.gameserver.instancemanager.CommissionManager;
import com.l2jmobius.gameserver.instancemanager.CursedWeaponsManager;
import com.l2jmobius.gameserver.instancemanager.DBSpawnManager;
import com.l2jmobius.gameserver.instancemanager.FactionManager;
import com.l2jmobius.gameserver.instancemanager.FakePlayerChatManager;
import com.l2jmobius.gameserver.instancemanager.FortManager;
import com.l2jmobius.gameserver.instancemanager.FortSiegeManager;
import com.l2jmobius.gameserver.instancemanager.GlobalVariablesManager;
import com.l2jmobius.gameserver.instancemanager.GraciaSeedsManager;
import com.l2jmobius.gameserver.instancemanager.GrandBossManager;
import com.l2jmobius.gameserver.instancemanager.InstanceManager;
import com.l2jmobius.gameserver.instancemanager.ItemAuctionManager;
import com.l2jmobius.gameserver.instancemanager.ItemsOnGroundManager;
import com.l2jmobius.gameserver.instancemanager.MailManager;
import com.l2jmobius.gameserver.instancemanager.MapRegionManager;
import com.l2jmobius.gameserver.instancemanager.MatchingRoomManager;
import com.l2jmobius.gameserver.instancemanager.MentorManager;
import com.l2jmobius.gameserver.instancemanager.PcCafePointsManager;
import com.l2jmobius.gameserver.instancemanager.PetitionManager;
import com.l2jmobius.gameserver.instancemanager.PremiumManager;
import com.l2jmobius.gameserver.instancemanager.PunishmentManager;
import com.l2jmobius.gameserver.instancemanager.QuestManager;
import com.l2jmobius.gameserver.instancemanager.SellBuffsManager;
import com.l2jmobius.gameserver.instancemanager.ServerRestartManager;
import com.l2jmobius.gameserver.instancemanager.SiegeGuardManager;
import com.l2jmobius.gameserver.instancemanager.SiegeManager;
import com.l2jmobius.gameserver.instancemanager.WalkingManager;
import com.l2jmobius.gameserver.instancemanager.ZoneManager;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.entity.Hero;
import com.l2jmobius.gameserver.model.events.EventDispatcher;
import com.l2jmobius.gameserver.model.olympiad.Olympiad;
import com.l2jmobius.gameserver.model.votereward.VoteSystem;
import com.l2jmobius.gameserver.network.ClientNetworkManager;
import com.l2jmobius.gameserver.network.loginserver.LoginServerNetworkManager;
import com.l2jmobius.gameserver.network.telnet.TelnetServer;
import com.l2jmobius.gameserver.scripting.ScriptEngineManager;
import com.l2jmobius.gameserver.taskmanager.TaskManager;
import com.l2jmobius.gameserver.util.Broadcast;

public class GameServer
{
	private static final Logger LOGGER = Logger.getLogger(GameServer.class.getName());
	
	private final DeadLockDetector _deadDetectThread;
	private static GameServer INSTANCE;
	public static final Calendar dateTimeServerStarted = Calendar.getInstance();
	
	public long getUsedMemoryMB()
	{
		return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
	}
	
	public DeadLockDetector getDeadLockDetectorThread()
	{
		return _deadDetectThread;
	}
	
	public GameServer() throws Exception
	{
		final long serverLoadStart = System.currentTimeMillis();
		
		printSection("ThreadPool");
		ThreadPool.init();
		
		printSection("IdFactory");
		if (!IdFactory.getInstance().isInitialized())
		{
			LOGGER.severe(getClass().getSimpleName() + ": Could not read object IDs from database. Please check your configuration.");
			throw new Exception("Could not initialize the ID factory!");
		}
		
		// load script engines
		printSection("Scripting Engines");
		EventDispatcher.getInstance();
		ScriptEngineManager.getInstance();
		
		printSection("Telnet");
		TelnetServer.getInstance();
		
		printSection("World");
		// start game time control early
		GameTimeController.init();
		L2World.getInstance();
		MapRegionManager.getInstance();
		ZoneManager.getInstance();
		DoorData.getInstance();
		AnnouncementsTable.getInstance();
		GlobalVariablesManager.getInstance();
		
		printSection("Data");
		ActionData.getInstance();
		CategoryData.getInstance();
		SecondaryAuthData.getInstance();
		AbilityPointsData.getInstance();
		CombinationItemsData.getInstance();
		SayuneData.getInstance();
		ClanRewardData.getInstance();
		
		printSection("Skills");
		SkillConditionHandler.getInstance().executeScript();
		EffectHandler.getInstance().executeScript();
		EnchantSkillGroupsData.getInstance();
		SkillTreesData.getInstance();
		SkillData.getInstance();
		PetSkillData.getInstance();
		
		printSection("Items");
		ConditionHandler.getInstance().executeScript();
		ItemTable.getInstance();
		EnchantItemGroupsData.getInstance();
		EnchantItemData.getInstance();
		EnchantItemOptionsData.getInstance();
		ItemCrystallizationData.getInstance();
		OptionData.getInstance();
		VariationData.getInstance();
		EnchantItemHPBonusData.getInstance();
		BuyListData.getInstance();
		MultisellData.getInstance();
		RecipeData.getInstance();
		ArmorSetsData.getInstance();
		FishingData.getInstance();
		HennaData.getInstance();
		PrimeShopData.getInstance();
		PcCafePointsManager.getInstance();
		AppearanceItemData.getInstance();
		AlchemyData.getInstance();
		CommissionManager.getInstance();
		LuckyGameData.getInstance();
		
		printSection("Characters");
		ClassListData.getInstance();
		InitialEquipmentData.getInstance();
		InitialShortcutData.getInstance();
		ExperienceData.getInstance();
		PlayerXpPercentLostData.getInstance();
		KarmaData.getInstance();
		HitConditionBonusData.getInstance();
		PlayerTemplateData.getInstance();
		CharNameTable.getInstance();
		AdminData.getInstance();
		PetDataTable.getInstance();
		CubicData.getInstance();
		CharSummonTable.getInstance().init();
		BeautyShopData.getInstance();
		MentorManager.getInstance();
		
		if (Config.FACTION_SYSTEM_ENABLED)
		{
			FactionManager.getInstance();
		}
		
		if (Config.PREMIUM_SYSTEM_ENABLED)
		{
			LOGGER.info("PremiumManager: Premium system is enabled.");
			PremiumManager.getInstance();
		}
		
		printSection("Clans");
		ClanTable.getInstance();
		ResidenceFunctionsData.getInstance();
		ClanHallData.getInstance();
		ClanHallAuctionManager.getInstance();
		ClanEntryManager.getInstance();
		
		printSection("Geodata");
		long geodataMemory = getUsedMemoryMB();
		GeoEngine.getInstance();
		geodataMemory = getUsedMemoryMB() - geodataMemory;
		if (geodataMemory < 0)
		{
			geodataMemory = 0;
		}
		
		printSection("NPCs");
		NpcData.getInstance();
		FakePlayerData.getInstance();
		FakePlayerChatManager.getInstance();
		ExtendDropData.getInstance();
		SpawnsData.getInstance();
		WalkingManager.getInstance();
		StaticObjectData.getInstance();
		ItemAuctionManager.getInstance();
		CastleManager.getInstance().loadInstances();
		GrandBossManager.getInstance();
		EventDroplist.getInstance();
		CommissionManager.getInstance();
		
		printSection("Instance");
		InstanceManager.getInstance();
		
		printSection("Olympiad");
		Olympiad.getInstance();
		Hero.getInstance();
		
		// Call to load caches
		printSection("Cache");
		HtmCache.getInstance();
		CrestTable.getInstance();
		TeleportersData.getInstance();
		MatchingRoomManager.getInstance();
		PetitionManager.getInstance();
		CursedWeaponsManager.getInstance();
		TransformData.getInstance();
		BotReportTable.getInstance();
		if (Config.SELLBUFF_ENABLED)
		{
			SellBuffsManager.getInstance();
		}
		
		printSection("Scripts");
		QuestManager.getInstance();
		BoatManager.getInstance();
		AirShipManager.getInstance();
		ShuttleData.getInstance();
		GraciaSeedsManager.getInstance();
		
		try
		{
			LOGGER.info(getClass().getSimpleName() + ": Loading server scripts:");
			ScriptEngineManager.getInstance().executeMasterHandler();
			ScriptEngineManager.getInstance().executeScriptList();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Failed to execute script list!", e);
		}
		
		SpawnsData.getInstance().init();
		DBSpawnManager.getInstance();
		
		printSection("Event Engine");
		EventEngineData.getInstance();
		VoteSystem.initialize();
		
		printSection("Siege");
		SiegeManager.getInstance().getSieges();
		CastleManager.getInstance().activateInstances();
		FortManager.getInstance().loadInstances();
		FortManager.getInstance().activateInstances();
		FortSiegeManager.getInstance();
		SiegeScheduleData.getInstance();
		
		CastleManorManager.getInstance();
		SiegeGuardManager.getInstance();
		QuestManager.getInstance().report();
		
		if (Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance();
		}
		
		if ((Config.AUTODESTROY_ITEM_AFTER > 0) || (Config.HERB_AUTO_DESTROY_TIME > 0))
		{
			ItemsAutoDestroy.getInstance();
		}
		
		if (Config.ALLOW_RACE)
		{
			MonsterRace.getInstance();
		}
		TaskManager.getInstance();
		
		AntiFeedManager.getInstance().registerEvent(AntiFeedManager.GAME_ID);
		
		if (Config.ALLOW_MAIL)
		{
			MailManager.getInstance();
		}
		
		PunishmentManager.getInstance();
		
		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
		
		LOGGER.info("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());
		
		if ((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.RESTORE_OFFLINERS)
		{
			OfflineTradersTable.getInstance().restoreOfflineTraders();
		}
		
		if (Config.SERVER_RESTART_SCHEDULE_ENABLED)
		{
			ServerRestartManager.getInstance();
		}
		
		if (Config.DEADLOCK_DETECTOR)
		{
			_deadDetectThread = new DeadLockDetector(Duration.ofSeconds(Config.DEADLOCK_CHECK_INTERVAL), () ->
			{
				if (Config.RESTART_ON_DEADLOCK)
				{
					Broadcast.toAllOnlinePlayers("Server has stability issues - restarting now.");
					Shutdown.getInstance().startTelnetShutdown("DeadLockDetector - Auto Restart", 60, true);
				}
			});
			_deadDetectThread.setDaemon(true);
			_deadDetectThread.start();
		}
		else
		{
			_deadDetectThread = null;
		}
		System.gc();
		final long totalMem = Runtime.getRuntime().maxMemory() / 1048576;
		LOGGER.info(getClass().getSimpleName() + ": Started, using " + getUsedMemoryMB() + " of " + totalMem + " MB total memory.");
		LOGGER.info(getClass().getSimpleName() + ": Geodata use " + geodataMemory + " MB of memory.");
		LOGGER.info(getClass().getSimpleName() + ": Maximum number of connected players is " + Config.MAXIMUM_ONLINE_USERS + ".");
		LOGGER.info(getClass().getSimpleName() + ": Server loaded in " + ((System.currentTimeMillis() - serverLoadStart) / 1000) + " seconds.");
		
		ClientNetworkManager.getInstance().start();
		
		if (Boolean.getBoolean("newLoginServer"))
		{
			LoginServerNetworkManager.getInstance().connect();
		}
		else
		{
			LoginServerThread.getInstance().start();
		}
		
		Toolkit.getDefaultToolkit().beep();
	}
	
	public long getStartedTime()
	{
		return ManagementFactory.getRuntimeMXBean().getStartTime();
	}
	
	public String getUptime()
	{
		final long uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
		final long hours = uptime / 3600;
		final long mins = (uptime - (hours * 3600)) / 60;
		final long secs = ((uptime - (hours * 3600)) - (mins * 60));
		if (hours > 0)
		{
			return hours + "hrs " + mins + "mins " + secs + "secs";
		}
		return mins + "mins " + secs + "secs";
	}
	
	public static void main(String[] args) throws Exception
	{
		Server.serverMode = Server.MODE_GAMESERVER;
		
		/*** Main ***/
		// Create log folder
		final File logFolder = new File(Config.DATAPACK_ROOT, "log");
		logFolder.mkdir();
		
		// Create input stream for log file -- or store file data into memory
		try (InputStream is = new FileInputStream(new File("./log.cfg")))
		{
			LogManager.getLogManager().readConfiguration(is);
		}
		
		// Initialize config
		Config.load();
		printSection("Database");
		DatabaseFactory.getInstance();
		
		INSTANCE = new GameServer();
	}
	
	public static void printSection(String s)
	{
		s = "=[ " + s + " ]";
		while (s.length() < 61)
		{
			s = "-" + s;
		}
		LOGGER.info(s);
	}
	
	public static GameServer getInstance()
	{
		return INSTANCE;
	}
}
