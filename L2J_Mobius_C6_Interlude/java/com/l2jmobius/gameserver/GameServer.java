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

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.Server;
import com.l2jmobius.commons.concurrent.ThreadPool;
import com.l2jmobius.commons.crypt.nProtect;
import com.l2jmobius.commons.database.DatabaseFactory;
import com.l2jmobius.commons.mmocore.NetcoreConfig;
import com.l2jmobius.commons.mmocore.SelectorConfig;
import com.l2jmobius.commons.mmocore.SelectorThread;
import com.l2jmobius.commons.util.IPv4Filter;
import com.l2jmobius.commons.util.Memory;
import com.l2jmobius.commons.util.Util;
import com.l2jmobius.gameserver.cache.CrestCache;
import com.l2jmobius.gameserver.cache.HtmCache;
import com.l2jmobius.gameserver.communitybbs.Manager.ForumsBBSManager;
import com.l2jmobius.gameserver.datatables.BufferTable;
import com.l2jmobius.gameserver.datatables.GmListTable;
import com.l2jmobius.gameserver.datatables.HeroSkillTable;
import com.l2jmobius.gameserver.datatables.NobleSkillTable;
import com.l2jmobius.gameserver.datatables.OfflineTradeTable;
import com.l2jmobius.gameserver.datatables.SkillTable;
import com.l2jmobius.gameserver.datatables.csv.DoorTable;
import com.l2jmobius.gameserver.datatables.csv.ExtractableItemsData;
import com.l2jmobius.gameserver.datatables.csv.FishTable;
import com.l2jmobius.gameserver.datatables.csv.HennaTable;
import com.l2jmobius.gameserver.datatables.csv.MapRegionTable;
import com.l2jmobius.gameserver.datatables.csv.NpcWalkerRoutesTable;
import com.l2jmobius.gameserver.datatables.csv.RecipeTable;
import com.l2jmobius.gameserver.datatables.csv.StaticObjects;
import com.l2jmobius.gameserver.datatables.csv.SummonItemsData;
import com.l2jmobius.gameserver.datatables.sql.AccessLevels;
import com.l2jmobius.gameserver.datatables.sql.AdminCommandAccessRights;
import com.l2jmobius.gameserver.datatables.sql.ArmorSetsTable;
import com.l2jmobius.gameserver.datatables.sql.CharNameTable;
import com.l2jmobius.gameserver.datatables.sql.CharTemplateTable;
import com.l2jmobius.gameserver.datatables.sql.ClanTable;
import com.l2jmobius.gameserver.datatables.sql.CustomArmorSetsTable;
import com.l2jmobius.gameserver.datatables.sql.HelperBuffTable;
import com.l2jmobius.gameserver.datatables.sql.HennaTreeTable;
import com.l2jmobius.gameserver.datatables.sql.L2PetDataTable;
import com.l2jmobius.gameserver.datatables.sql.LevelUpData;
import com.l2jmobius.gameserver.datatables.sql.NpcTable;
import com.l2jmobius.gameserver.datatables.sql.SkillSpellbookTable;
import com.l2jmobius.gameserver.datatables.sql.SkillTreeTable;
import com.l2jmobius.gameserver.datatables.sql.SpawnTable;
import com.l2jmobius.gameserver.datatables.sql.TeleportLocationTable;
import com.l2jmobius.gameserver.datatables.xml.AugmentationData;
import com.l2jmobius.gameserver.datatables.xml.ExperienceData;
import com.l2jmobius.gameserver.datatables.xml.FenceData;
import com.l2jmobius.gameserver.datatables.xml.ItemTable;
import com.l2jmobius.gameserver.datatables.xml.ZoneData;
import com.l2jmobius.gameserver.geodata.GeoData;
import com.l2jmobius.gameserver.geodata.geoeditorcon.GeoEditorListener;
import com.l2jmobius.gameserver.geodata.pathfinding.PathFinding;
import com.l2jmobius.gameserver.handler.AdminCommandHandler;
import com.l2jmobius.gameserver.handler.AutoAnnouncementHandler;
import com.l2jmobius.gameserver.handler.AutoChatHandler;
import com.l2jmobius.gameserver.handler.ItemHandler;
import com.l2jmobius.gameserver.handler.SkillHandler;
import com.l2jmobius.gameserver.handler.UserCommandHandler;
import com.l2jmobius.gameserver.handler.VoicedCommandHandler;
import com.l2jmobius.gameserver.idfactory.IdFactory;
import com.l2jmobius.gameserver.instancemanager.AuctionManager;
import com.l2jmobius.gameserver.instancemanager.AutoSaveManager;
import com.l2jmobius.gameserver.instancemanager.AwayManager;
import com.l2jmobius.gameserver.instancemanager.BoatManager;
import com.l2jmobius.gameserver.instancemanager.CastleManager;
import com.l2jmobius.gameserver.instancemanager.CastleManorManager;
import com.l2jmobius.gameserver.instancemanager.ClanHallManager;
import com.l2jmobius.gameserver.instancemanager.ClassDamageManager;
import com.l2jmobius.gameserver.instancemanager.CoupleManager;
import com.l2jmobius.gameserver.instancemanager.CrownManager;
import com.l2jmobius.gameserver.instancemanager.CursedWeaponsManager;
import com.l2jmobius.gameserver.instancemanager.DayNightSpawnManager;
import com.l2jmobius.gameserver.instancemanager.DimensionalRiftManager;
import com.l2jmobius.gameserver.instancemanager.DuelManager;
import com.l2jmobius.gameserver.instancemanager.FishingChampionshipManager;
import com.l2jmobius.gameserver.instancemanager.FortManager;
import com.l2jmobius.gameserver.instancemanager.FortSiegeManager;
import com.l2jmobius.gameserver.instancemanager.FourSepulchersManager;
import com.l2jmobius.gameserver.instancemanager.GlobalVariablesManager;
import com.l2jmobius.gameserver.instancemanager.GrandBossManager;
import com.l2jmobius.gameserver.instancemanager.ItemsOnGroundManager;
import com.l2jmobius.gameserver.instancemanager.MercTicketManager;
import com.l2jmobius.gameserver.instancemanager.PetitionManager;
import com.l2jmobius.gameserver.instancemanager.QuestManager;
import com.l2jmobius.gameserver.instancemanager.RaidBossPointsManager;
import com.l2jmobius.gameserver.instancemanager.RaidBossSpawnManager;
import com.l2jmobius.gameserver.instancemanager.SiegeManager;
import com.l2jmobius.gameserver.model.L2Manor;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.PartyMatchRoomList;
import com.l2jmobius.gameserver.model.PartyMatchWaitingList;
import com.l2jmobius.gameserver.model.entity.Announcements;
import com.l2jmobius.gameserver.model.entity.Hero;
import com.l2jmobius.gameserver.model.entity.MonsterRace;
import com.l2jmobius.gameserver.model.entity.event.Lottery;
import com.l2jmobius.gameserver.model.entity.event.manager.EventManager;
import com.l2jmobius.gameserver.model.entity.olympiad.Olympiad;
import com.l2jmobius.gameserver.model.entity.sevensigns.SevenSigns;
import com.l2jmobius.gameserver.model.entity.sevensigns.SevenSignsFestival;
import com.l2jmobius.gameserver.model.entity.siege.clanhalls.BanditStrongholdSiege;
import com.l2jmobius.gameserver.model.entity.siege.clanhalls.DevastatedCastle;
import com.l2jmobius.gameserver.model.entity.siege.clanhalls.FortressOfResistance;
import com.l2jmobius.gameserver.model.multisell.L2Multisell;
import com.l2jmobius.gameserver.model.spawn.AutoSpawn;
import com.l2jmobius.gameserver.network.L2GameClient;
import com.l2jmobius.gameserver.network.L2GamePacketHandler;
import com.l2jmobius.gameserver.script.EventDroplist;
import com.l2jmobius.gameserver.script.faenor.FaenorScriptEngine;
import com.l2jmobius.gameserver.scripting.CompiledScriptCache;
import com.l2jmobius.gameserver.scripting.L2ScriptEngineManager;
import com.l2jmobius.gameserver.taskmanager.TaskManager;
import com.l2jmobius.gameserver.thread.LoginServerThread;
import com.l2jmobius.gameserver.thread.daemons.DeadlockDetector;
import com.l2jmobius.gameserver.thread.daemons.ItemsAutoDestroy;
import com.l2jmobius.gameserver.thread.daemons.PcPoint;
import com.l2jmobius.gameserver.ui.Gui;
import com.l2jmobius.gameserver.util.DynamicExtension;
import com.l2jmobius.status.Status;

public class GameServer
{
	private static Logger LOGGER = Logger.getLogger(GameServer.class.getName());
	
	private static SelectorThread<L2GameClient> _selectorThread;
	private static LoginServerThread _loginThread;
	private static L2GamePacketHandler _gamePacketHandler;
	private static Status _statusServer;
	
	public static final Calendar dateTimeServerStarted = Calendar.getInstance();
	
	public static void main(String[] args) throws Exception
	{
		Server.serverMode = Server.MODE_GAMESERVER;
		
		// GUI
		if (!GraphicsEnvironment.isHeadless())
		{
			new Gui();
		}
		
		// Create log folder
		final File logFolder = new File(Config.DATAPACK_ROOT, "log");
		logFolder.mkdir();
		
		// Create input stream for log file -- or store file data into memory
		try (InputStream is = new FileInputStream(new File("./log.cfg")))
		{
			LogManager.getLogManager().readConfiguration(is);
		}
		
		final long serverLoadStart = System.currentTimeMillis();
		
		// Load GameServer Configs
		Config.load();
		
		Util.printSection("Database");
		DatabaseFactory.getInstance();
		
		Util.printSection("ThreadPool");
		ThreadPool.init();
		if (Config.DEADLOCKCHECK_INTIAL_TIME > 0)
		{
			ThreadPool.scheduleAtFixedRate(DeadlockDetector.getInstance(), Config.DEADLOCKCHECK_INTIAL_TIME, Config.DEADLOCKCHECK_DELAY_TIME);
		}
		
		Util.printSection("IdFactory");
		if (!IdFactory.getInstance().isInitialized())
		{
			LOGGER.info("Could not read object IDs from DB. Please Check Your Data.");
			throw new Exception("Could not initialize the ID factory");
		}
		
		new File(Config.DATAPACK_ROOT, "data/clans").mkdirs();
		new File(Config.DATAPACK_ROOT, "data/crests").mkdirs();
		new File(Config.DATAPACK_ROOT, "data/pathnode").mkdirs();
		new File(Config.DATAPACK_ROOT, "data/geodata").mkdirs();
		
		HtmCache.getInstance();
		CrestCache.getInstance();
		L2ScriptEngineManager.getInstance();
		
		nProtect.getInstance();
		if (nProtect.isEnabled())
		{
			LOGGER.info("nProtect System Enabled");
		}
		
		Util.printSection("World");
		L2World.getInstance();
		MapRegionTable.getInstance();
		Announcements.getInstance();
		AutoAnnouncementHandler.getInstance();
		GlobalVariablesManager.getInstance();
		StaticObjects.getInstance();
		TeleportLocationTable.getInstance();
		PartyMatchWaitingList.getInstance();
		PartyMatchRoomList.getInstance();
		GameTimeController.getInstance();
		CharNameTable.getInstance();
		ExperienceData.getInstance();
		DuelManager.getInstance();
		
		if (Config.ENABLE_CLASS_DAMAGES)
		{
			ClassDamageManager.loadConfig();
		}
		
		if (Config.AUTOSAVE_DELAY_TIME > 0)
		{
			AutoSaveManager.getInstance().startAutoSaveManager();
		}
		
		Util.printSection("Skills");
		if (!SkillTable.getInstance().isInitialized())
		{
			LOGGER.info("Could not find the extraced files. Please Check Your Data.");
			throw new Exception("Could not initialize the skill table");
		}
		SkillTreeTable.getInstance();
		SkillSpellbookTable.getInstance();
		NobleSkillTable.getInstance();
		HeroSkillTable.getInstance();
		LOGGER.info("Skills: All skills loaded.");
		
		Util.printSection("Items");
		ItemTable.getInstance();
		ArmorSetsTable.getInstance();
		if (Config.CUSTOM_ARMORSETS_TABLE)
		{
			CustomArmorSetsTable.getInstance();
		}
		ExtractableItemsData.getInstance();
		SummonItemsData.getInstance();
		if (Config.ALLOWFISHING)
		{
			FishTable.getInstance();
		}
		
		Util.printSection("Npc");
		BufferTable.getInstance();
		NpcWalkerRoutesTable.getInstance().load();
		if (!NpcTable.getInstance().isInitialized())
		{
			LOGGER.info("Could not find the extracted files. Please Check Your Data.");
			throw new Exception("Could not initialize the npc table");
		}
		
		Util.printSection("Characters");
		if (Config.ENABLE_COMMUNITY_BOARD)
		{
			ForumsBBSManager.getInstance().initRoot();
		}
		
		ClanTable.getInstance();
		CharTemplateTable.getInstance();
		LevelUpData.getInstance();
		if (!HennaTable.getInstance().isInitialized())
		{
			throw new Exception("Could not initialize the Henna Table");
		}
		
		if (!HennaTreeTable.getInstance().isInitialized())
		{
			throw new Exception("Could not initialize the Henna Tree Table");
		}
		
		if (!HelperBuffTable.getInstance().isInitialized())
		{
			throw new Exception("Could not initialize the Helper Buff Table");
		}
		
		Util.printSection("Geodata");
		GeoData.getInstance();
		if (Config.PATHFINDING > 0)
		{
			PathFinding.getInstance();
		}
		
		Util.printSection("Economy");
		TradeController.getInstance();
		L2Multisell.getInstance();
		LOGGER.info("Multisell: loaded.");
		
		Util.printSection("Clan Halls");
		ClanHallManager.getInstance();
		FortressOfResistance.getInstance();
		DevastatedCastle.getInstance();
		BanditStrongholdSiege.getInstance();
		AuctionManager.getInstance();
		
		Util.printSection("Zone");
		ZoneData.getInstance();
		
		Util.printSection("Spawnlist");
		if (!Config.ALT_DEV_NO_SPAWNS)
		{
			SpawnTable.getInstance();
		}
		else
		{
			LOGGER.info("Spawn: disable load.");
		}
		if (!Config.ALT_DEV_NO_RB)
		{
			RaidBossSpawnManager.getInstance();
			GrandBossManager.getInstance();
			RaidBossPointsManager.init();
		}
		else
		{
			LOGGER.info("RaidBoss: disable load.");
		}
		DayNightSpawnManager.getInstance().notifyChangeMode();
		
		Util.printSection("Dimensional Rift");
		DimensionalRiftManager.getInstance();
		
		Util.printSection("Misc");
		RecipeTable.getInstance();
		RecipeController.getInstance();
		EventDroplist.getInstance();
		AugmentationData.getInstance();
		MonsterRace.getInstance();
		Lottery.getInstance();
		MercTicketManager.getInstance();
		PetitionManager.getInstance();
		CursedWeaponsManager.getInstance();
		TaskManager.getInstance();
		L2PetDataTable.getInstance().loadPetsData();
		if (Config.ACCEPT_GEOEDITOR_CONN)
		{
			GeoEditorListener.getInstance();
		}
		if (Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance();
		}
		if ((Config.AUTODESTROY_ITEM_AFTER > 0) || (Config.HERB_AUTO_DESTROY_TIME > 0))
		{
			ItemsAutoDestroy.getInstance();
		}
		
		Util.printSection("Manor");
		L2Manor.getInstance();
		CastleManorManager.getInstance();
		
		Util.printSection("Castles");
		CastleManager.getInstance();
		SiegeManager.getInstance();
		FortManager.getInstance();
		FortSiegeManager.getInstance();
		CrownManager.getInstance();
		
		Util.printSection("Boat");
		BoatManager.getInstance();
		
		Util.printSection("Doors");
		DoorTable.getInstance().parseData();
		FenceData.getInstance();
		
		Util.printSection("Four Sepulchers");
		FourSepulchersManager.getInstance();
		
		Util.printSection("Seven Signs");
		SevenSigns.getInstance();
		SevenSignsFestival.getInstance();
		AutoSpawn.getInstance();
		AutoChatHandler.getInstance();
		
		Util.printSection("Olympiad System");
		Olympiad.getInstance();
		Hero.getInstance();
		
		Util.printSection("Access Levels");
		AccessLevels.getInstance();
		AdminCommandAccessRights.getInstance();
		GmListTable.getInstance();
		
		Util.printSection("Handlers");
		ItemHandler.getInstance();
		SkillHandler.getInstance();
		AdminCommandHandler.getInstance();
		UserCommandHandler.getInstance();
		VoicedCommandHandler.getInstance();
		
		LOGGER.info("AutoChatHandler : Loaded " + AutoChatHandler.getInstance().size() + " handlers in total.");
		LOGGER.info("AutoSpawnHandler : Loaded " + AutoSpawn.getInstance().size() + " handlers in total.");
		
		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
		
		try
		{
			DoorTable doorTable = DoorTable.getInstance();
			
			// Opened by players like L2OFF
			// doorTable.getDoor(19160010).openMe();
			// doorTable.getDoor(19160011).openMe();
			
			doorTable.getDoor(19160012).openMe();
			doorTable.getDoor(19160013).openMe();
			doorTable.getDoor(19160014).openMe();
			doorTable.getDoor(19160015).openMe();
			doorTable.getDoor(19160016).openMe();
			doorTable.getDoor(19160017).openMe();
			doorTable.getDoor(24190001).openMe();
			doorTable.getDoor(24190002).openMe();
			doorTable.getDoor(24190003).openMe();
			doorTable.getDoor(24190004).openMe();
			doorTable.getDoor(23180001).openMe();
			doorTable.getDoor(23180002).openMe();
			doorTable.getDoor(23180003).openMe();
			doorTable.getDoor(23180004).openMe();
			doorTable.getDoor(23180005).openMe();
			doorTable.getDoor(23180006).openMe();
			doorTable.checkAutoOpen();
		}
		catch (NullPointerException e)
		{
			LOGGER.info("There is errors in your Door.csv file. Update door.csv");
		}
		
		Util.printSection("Scripts");
		if (!Config.ALT_DEV_NO_SCRIPT)
		{
			final File scripts = new File(Config.DATAPACK_ROOT, "data/scripts.cfg");
			L2ScriptEngineManager.getInstance().executeScriptsList(scripts);
			
			final CompiledScriptCache compiledScriptCache = L2ScriptEngineManager.getInstance().getCompiledScriptCache();
			if (compiledScriptCache == null)
			{
				LOGGER.info("Compiled Scripts Cache is disabled.");
			}
			else
			{
				compiledScriptCache.purge();
				if (compiledScriptCache.isModified())
				{
					compiledScriptCache.save();
					LOGGER.info("Compiled Scripts Cache was saved.");
				}
				else
				{
					LOGGER.info("Compiled Scripts Cache is up-to-date.");
				}
			}
			FaenorScriptEngine.getInstance();
		}
		else
		{
			LOGGER.info("Script: disable load.");
		}
		
		if (Config.ALT_FISH_CHAMPIONSHIP_ENABLED)
		{
			FishingChampionshipManager.getInstance();
		}
		
		/* QUESTS */
		Util.printSection("Quests");
		if (!Config.ALT_DEV_NO_QUESTS)
		{
			if (QuestManager.getInstance().getQuests().size() == 0)
			{
				QuestManager.getInstance().reloadAllQuests();
			}
			else
			{
				QuestManager.getInstance().report();
			}
		}
		else
		{
			QuestManager.getInstance().unloadAllQuests();
		}
		
		Util.printSection("Game Server");
		
		LOGGER.info("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());
		try
		{
			DynamicExtension.getInstance();
		}
		catch (Exception ex)
		{
			LOGGER.info("DynamicExtension could not be loaded and initialized" + ex);
		}
		
		Util.printSection("Custom Mods");
		
		if (Config.L2JMOD_ALLOW_WEDDING || Config.ALLOW_AWAY_STATUS || Config.PCB_ENABLE)
		{
			if (Config.L2JMOD_ALLOW_WEDDING)
			{
				CoupleManager.getInstance();
			}
			
			if (Config.ALLOW_AWAY_STATUS)
			{
				AwayManager.getInstance();
			}
			
			if (Config.PCB_ENABLE)
			{
				ThreadPool.scheduleAtFixedRate(PcPoint.getInstance(), Config.PCB_INTERVAL * 1000, Config.PCB_INTERVAL * 1000);
			}
		}
		else
		{
			LOGGER.info("All custom mods are Disabled.");
		}
		
		Util.printSection("EventManager");
		EventManager.getInstance().startEventRegistration();
		
		if (EventManager.TVT_EVENT_ENABLED || EventManager.CTF_EVENT_ENABLED || EventManager.DM_EVENT_ENABLED)
		{
			if (EventManager.TVT_EVENT_ENABLED)
			{
				LOGGER.info("TVT Event is Enabled.");
			}
			if (EventManager.CTF_EVENT_ENABLED)
			{
				LOGGER.info("CTF Event is Enabled.");
			}
			if (EventManager.DM_EVENT_ENABLED)
			{
				LOGGER.info("DM Event is Enabled.");
			}
		}
		else
		{
			LOGGER.info("All events are Disabled.");
		}
		
		if ((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.RESTORE_OFFLINERS)
		{
			OfflineTradeTable.restoreOfflineTraders();
		}
		
		Util.printSection("Protection");
		
		if (Config.CHECK_SKILLS_ON_ENTER)
		{
			LOGGER.info("Check skills on enter actived.");
		}
		
		if (Config.CHECK_NAME_ON_LOGIN)
		{
			LOGGER.info("Check bad name on enter actived.");
		}
		
		if (Config.PROTECTED_ENCHANT)
		{
			LOGGER.info("Check OverEnchant items on enter actived.");
		}
		
		if (Config.BYPASS_VALIDATION)
		{
			LOGGER.info("Bypass Validation actived.");
		}
		
		if (Config.L2WALKER_PROTEC)
		{
			LOGGER.info("L2Walker protection actived.");
		}
		
		Util.printSection("Info");
		LOGGER.info("Maximum Numbers of Connected Players: " + Config.MAXIMUM_ONLINE_USERS);
		LOGGER.info("GameServer Started, free memory " + Memory.getFreeMemory() + " Mb of " + Memory.getTotalMemory() + " Mb");
		LOGGER.info("Used memory: " + Memory.getUsedMemory() + " MB");
		
		Util.printSection("Status");
		System.gc();
		LOGGER.info("Server Loaded in " + ((System.currentTimeMillis() - serverLoadStart) / 1000) + " seconds");
		ServerStatus.getInstance();
		
		// Load telnet status
		Util.printSection("Telnet");
		if (Config.IS_TELNET_ENABLED)
		{
			_statusServer = new Status(Server.serverMode);
			_statusServer.start();
		}
		else
		{
			LOGGER.info("Telnet server is disabled.");
		}
		
		Util.printSection("Login");
		_loginThread = LoginServerThread.getInstance();
		_loginThread.start();
		
		final SelectorConfig sc = new SelectorConfig();
		sc.setMaxReadPerPass(NetcoreConfig.getInstance().MMO_MAX_READ_PER_PASS);
		sc.setMaxSendPerPass(NetcoreConfig.getInstance().MMO_MAX_SEND_PER_PASS);
		sc.setSleepTime(NetcoreConfig.getInstance().MMO_SELECTOR_SLEEP_TIME);
		sc.setHelperBufferCount(NetcoreConfig.getInstance().MMO_HELPER_BUFFER_COUNT);
		
		_gamePacketHandler = new L2GamePacketHandler();
		
		_selectorThread = new SelectorThread<>(sc, _gamePacketHandler, _gamePacketHandler, _gamePacketHandler, new IPv4Filter());
		
		InetAddress bindAddress = null;
		if (!Config.GAMESERVER_HOSTNAME.equals("*"))
		{
			try
			{
				bindAddress = InetAddress.getByName(Config.GAMESERVER_HOSTNAME);
			}
			catch (UnknownHostException e1)
			{
				LOGGER.warning("The GameServer bind address is invalid, using all avaliable IPs. Reason: " + e1);
			}
		}
		
		try
		{
			_selectorThread.openServerSocket(bindAddress, Config.PORT_GAME);
		}
		catch (IOException e)
		{
			LOGGER.severe("Failed to open server socket. Reason: " + e);
			System.exit(1);
		}
		_selectorThread.start();
	}
	
	public static SelectorThread<L2GameClient> getSelectorThread()
	{
		return _selectorThread;
	}
}