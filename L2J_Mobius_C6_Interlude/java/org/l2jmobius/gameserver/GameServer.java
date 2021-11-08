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
package org.l2jmobius.gameserver;

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.Calendar;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.enums.ServerMode;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.commons.util.DeadLockDetector;
import org.l2jmobius.commons.util.PropertiesParser;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.communitybbs.Manager.ForumsBBSManager;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.data.SchemeBufferTable;
import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.data.sql.AnnouncementsTable;
import org.l2jmobius.gameserver.data.sql.CharNameTable;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.data.sql.CrestTable;
import org.l2jmobius.gameserver.data.sql.HelperBuffTable;
import org.l2jmobius.gameserver.data.sql.NpcTable;
import org.l2jmobius.gameserver.data.sql.OfflineTraderTable;
import org.l2jmobius.gameserver.data.sql.PetDataTable;
import org.l2jmobius.gameserver.data.sql.SkillSpellbookTable;
import org.l2jmobius.gameserver.data.sql.SkillTreeTable;
import org.l2jmobius.gameserver.data.sql.SpawnTable;
import org.l2jmobius.gameserver.data.sql.TeleportLocationTable;
import org.l2jmobius.gameserver.data.xml.AdminData;
import org.l2jmobius.gameserver.data.xml.ArmorSetData;
import org.l2jmobius.gameserver.data.xml.AugmentationData;
import org.l2jmobius.gameserver.data.xml.BoatData;
import org.l2jmobius.gameserver.data.xml.DoorData;
import org.l2jmobius.gameserver.data.xml.ExperienceData;
import org.l2jmobius.gameserver.data.xml.ExtractableItemData;
import org.l2jmobius.gameserver.data.xml.FenceData;
import org.l2jmobius.gameserver.data.xml.FishData;
import org.l2jmobius.gameserver.data.xml.HennaData;
import org.l2jmobius.gameserver.data.xml.ManorSeedData;
import org.l2jmobius.gameserver.data.xml.MapRegionData;
import org.l2jmobius.gameserver.data.xml.MultisellData;
import org.l2jmobius.gameserver.data.xml.PlayerTemplateData;
import org.l2jmobius.gameserver.data.xml.RecipeData;
import org.l2jmobius.gameserver.data.xml.StaticObjectData;
import org.l2jmobius.gameserver.data.xml.SummonItemData;
import org.l2jmobius.gameserver.data.xml.WalkerRouteData;
import org.l2jmobius.gameserver.data.xml.ZoneData;
import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.handler.AdminCommandHandler;
import org.l2jmobius.gameserver.handler.AutoChatHandler;
import org.l2jmobius.gameserver.handler.ItemHandler;
import org.l2jmobius.gameserver.handler.SkillHandler;
import org.l2jmobius.gameserver.handler.UserCommandHandler;
import org.l2jmobius.gameserver.handler.VoicedCommandHandler;
import org.l2jmobius.gameserver.instancemanager.AuctionManager;
import org.l2jmobius.gameserver.instancemanager.CastleManager;
import org.l2jmobius.gameserver.instancemanager.CastleManorManager;
import org.l2jmobius.gameserver.instancemanager.ClanHallManager;
import org.l2jmobius.gameserver.instancemanager.ClassDamageManager;
import org.l2jmobius.gameserver.instancemanager.CoupleManager;
import org.l2jmobius.gameserver.instancemanager.CrownManager;
import org.l2jmobius.gameserver.instancemanager.CursedWeaponsManager;
import org.l2jmobius.gameserver.instancemanager.CustomMailManager;
import org.l2jmobius.gameserver.instancemanager.DayNightSpawnManager;
import org.l2jmobius.gameserver.instancemanager.DimensionalRiftManager;
import org.l2jmobius.gameserver.instancemanager.DuelManager;
import org.l2jmobius.gameserver.instancemanager.FishingChampionshipManager;
import org.l2jmobius.gameserver.instancemanager.FortManager;
import org.l2jmobius.gameserver.instancemanager.FortSiegeManager;
import org.l2jmobius.gameserver.instancemanager.FourSepulchersManager;
import org.l2jmobius.gameserver.instancemanager.GlobalVariablesManager;
import org.l2jmobius.gameserver.instancemanager.GrandBossManager;
import org.l2jmobius.gameserver.instancemanager.IdManager;
import org.l2jmobius.gameserver.instancemanager.ItemsOnGroundManager;
import org.l2jmobius.gameserver.instancemanager.MercTicketManager;
import org.l2jmobius.gameserver.instancemanager.PetitionManager;
import org.l2jmobius.gameserver.instancemanager.PrecautionaryRestartManager;
import org.l2jmobius.gameserver.instancemanager.QuestManager;
import org.l2jmobius.gameserver.instancemanager.RaidBossPointsManager;
import org.l2jmobius.gameserver.instancemanager.RaidBossSpawnManager;
import org.l2jmobius.gameserver.instancemanager.RecipeManager;
import org.l2jmobius.gameserver.instancemanager.ServerRestartManager;
import org.l2jmobius.gameserver.instancemanager.SiegeManager;
import org.l2jmobius.gameserver.instancemanager.TradeManager;
import org.l2jmobius.gameserver.instancemanager.events.PcPoint;
import org.l2jmobius.gameserver.instancemanager.games.Lottery;
import org.l2jmobius.gameserver.instancemanager.games.MonsterRace;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.olympiad.Hero;
import org.l2jmobius.gameserver.model.olympiad.Olympiad;
import org.l2jmobius.gameserver.model.partymatching.PartyMatchRoomList;
import org.l2jmobius.gameserver.model.partymatching.PartyMatchWaitingList;
import org.l2jmobius.gameserver.model.sevensigns.SevenSigns;
import org.l2jmobius.gameserver.model.sevensigns.SevenSignsFestival;
import org.l2jmobius.gameserver.model.siege.clanhalls.BanditStrongholdSiege;
import org.l2jmobius.gameserver.model.siege.clanhalls.DevastatedCastle;
import org.l2jmobius.gameserver.model.siege.clanhalls.FortressOfResistance;
import org.l2jmobius.gameserver.model.spawn.AutoSpawnHandler;
import org.l2jmobius.gameserver.network.ClientNetworkManager;
import org.l2jmobius.gameserver.script.EventDroplist;
import org.l2jmobius.gameserver.script.faenor.FaenorScriptEngine;
import org.l2jmobius.gameserver.scripting.ScriptEngineManager;
import org.l2jmobius.gameserver.taskmanager.GameTimeTaskManager;
import org.l2jmobius.gameserver.taskmanager.ItemsAutoDestroyTaskManager;
import org.l2jmobius.gameserver.taskmanager.TaskManager;
import org.l2jmobius.gameserver.ui.Gui;
import org.l2jmobius.telnet.TelnetStatusThread;

public class GameServer
{
	private static final Logger LOGGER = Logger.getLogger(GameServer.class.getName());
	
	private static TelnetStatusThread _statusServer;
	private static GameServer INSTANCE;
	public static final Calendar dateTimeServerStarted = Calendar.getInstance();
	
	public long getUsedMemoryMB()
	{
		return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
	}
	
	public GameServer() throws Exception
	{
		final long serverLoadStart = Chronos.currentTimeMillis();
		
		// GUI
		final PropertiesParser interfaceConfig = new PropertiesParser(Config.INTERFACE_CONFIG_FILE);
		Config.ENABLE_GUI = interfaceConfig.getBoolean("EnableGUI", true);
		if (Config.ENABLE_GUI && !GraphicsEnvironment.isHeadless())
		{
			Config.DARK_THEME = interfaceConfig.getBoolean("DarkTheme", true);
			System.out.println("GameServer: Running in GUI mode.");
			new Gui();
		}
		
		// Create log folder
		final File logFolder = new File(".", "log");
		logFolder.mkdir();
		
		// Create input stream for log file -- or store file data into memory
		try (InputStream is = new FileInputStream(new File("./log.cfg")))
		{
			LogManager.getLogManager().readConfiguration(is);
		}
		
		// Initialize config
		Config.load(ServerMode.GAME);
		
		printSection("Database");
		DatabaseFactory.init();
		
		printSection("ThreadPool");
		ThreadPool.init();
		
		printSection("IdManager");
		IdManager.getInstance();
		if (!IdManager.hasInitialized())
		{
			LOGGER.severe("IdFactory: Could not read object IDs from database. Please check your configuration.");
			throw new Exception("Could not initialize the ID factory!");
		}
		
		new File(Config.DATAPACK_ROOT, "data/geodata").mkdirs();
		
		HtmCache.getInstance();
		CrestTable.getInstance();
		ScriptEngineManager.getInstance();
		
		printSection("World");
		World.getInstance();
		MapRegionData.getInstance();
		AnnouncementsTable.getInstance();
		GlobalVariablesManager.getInstance();
		StaticObjectData.getInstance();
		TeleportLocationTable.getInstance();
		PartyMatchWaitingList.getInstance();
		PartyMatchRoomList.getInstance();
		GameTimeTaskManager.getInstance();
		CharNameTable.getInstance();
		ExperienceData.getInstance();
		DuelManager.getInstance();
		
		printSection("Players");
		PlayerTemplateData.getInstance();
		if (Config.ENABLE_CLASS_DAMAGE_SETTINGS)
		{
			ClassDamageManager.loadConfig();
		}
		ClanTable.getInstance();
		if (Config.ENABLE_COMMUNITY_BOARD)
		{
			ForumsBBSManager.getInstance().initRoot();
		}
		
		printSection("Skills");
		if (!SkillTable.getInstance().isInitialized())
		{
			LOGGER.info("Could not find the extraced files. Please Check Your Data.");
			throw new Exception("Could not initialize the skill table");
		}
		SkillTreeTable.getInstance();
		SkillSpellbookTable.getInstance();
		if (!HelperBuffTable.getInstance().isInitialized())
		{
			throw new Exception("Could not initialize the Helper Buff Table.");
		}
		LOGGER.info("Skills: All skills loaded.");
		
		printSection("Items");
		ItemTable.getInstance();
		ArmorSetData.getInstance();
		ExtractableItemData.getInstance();
		SummonItemData.getInstance();
		HennaData.getInstance();
		if (Config.ALLOWFISHING)
		{
			FishData.getInstance();
		}
		
		printSection("Npc");
		SchemeBufferTable.getInstance();
		WalkerRouteData.getInstance();
		if (!NpcTable.getInstance().isInitialized())
		{
			LOGGER.info("Could not find the extracted files. Please Check Your Data.");
			throw new Exception("Could not initialize the npc table");
		}
		
		printSection("Geodata");
		GeoEngine.getInstance();
		
		printSection("Economy");
		TradeManager.getInstance();
		MultisellData.getInstance();
		
		printSection("Clan Halls");
		ClanHallManager.getInstance();
		FortressOfResistance.getInstance();
		DevastatedCastle.getInstance();
		BanditStrongholdSiege.getInstance();
		AuctionManager.getInstance();
		
		printSection("Zone");
		ZoneData.getInstance();
		
		printSection("Spawnlist");
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
		
		printSection("Dimensional Rift");
		DimensionalRiftManager.getInstance();
		
		printSection("Misc");
		RecipeData.getInstance();
		RecipeManager.getInstance();
		EventDroplist.getInstance();
		AugmentationData.getInstance();
		MonsterRace.getInstance();
		Lottery.getInstance();
		MercTicketManager.getInstance();
		PetitionManager.getInstance();
		CursedWeaponsManager.getInstance();
		TaskManager.getInstance();
		PetDataTable.getInstance();
		if (Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance();
		}
		if ((Config.AUTODESTROY_ITEM_AFTER > 0) || (Config.HERB_AUTO_DESTROY_TIME > 0))
		{
			ItemsAutoDestroyTaskManager.getInstance();
		}
		
		printSection("Manor");
		ManorSeedData.getInstance();
		CastleManorManager.getInstance();
		
		printSection("Castles");
		CastleManager.getInstance();
		SiegeManager.getInstance();
		FortManager.getInstance();
		FortSiegeManager.getInstance();
		CrownManager.getInstance();
		
		printSection("Boat");
		BoatData.getInstance();
		
		printSection("Doors");
		DoorData.getInstance().load();
		FenceData.getInstance();
		
		printSection("Four Sepulchers");
		FourSepulchersManager.getInstance();
		
		printSection("Seven Signs");
		SevenSigns.getInstance();
		SevenSignsFestival.getInstance();
		AutoSpawnHandler.getInstance();
		AutoChatHandler.getInstance();
		
		printSection("Olympiad System");
		Olympiad.getInstance();
		Hero.getInstance();
		
		printSection("Access Levels");
		AdminData.getInstance();
		
		printSection("Handlers");
		ItemHandler.getInstance();
		SkillHandler.getInstance();
		AdminCommandHandler.getInstance();
		UserCommandHandler.getInstance();
		VoicedCommandHandler.getInstance();
		
		LOGGER.info("AutoChatHandler: Loaded " + AutoChatHandler.getInstance().size() + " handlers in total.");
		LOGGER.info("AutoSpawnHandler: Loaded " + AutoSpawnHandler.getInstance().size() + " handlers in total.");
		
		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
		
		// Schedule auto opening/closing doors.
		DoorData.getInstance().checkAutoOpen();
		
		if (Config.CUSTOM_MAIL_MANAGER_ENABLED)
		{
			CustomMailManager.getInstance();
		}
		
		printSection("Scripts");
		if (!Config.ALT_DEV_NO_SCRIPT)
		{
			LOGGER.info("ScriptEngineManager: Loading server scripts:");
			ScriptEngineManager.getInstance().executeScriptList();
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
		printSection("Quests");
		if (!Config.ALT_DEV_NO_QUESTS)
		{
			if (QuestManager.getInstance().getQuests().isEmpty())
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
		
		printSection("Game Server");
		
		LOGGER.info("IdFactory: Free ObjectID's remaining: " + IdManager.size());
		
		if (Config.ALLOW_WEDDING)
		{
			CoupleManager.getInstance();
		}
		
		if (Config.PCB_ENABLE)
		{
			ThreadPool.scheduleAtFixedRate(PcPoint.getInstance(), Config.PCB_INTERVAL * 1000, Config.PCB_INTERVAL * 1000);
		}
		
		if ((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.RESTORE_OFFLINERS)
		{
			OfflineTraderTable.restoreOfflineTraders();
		}
		
		printSection("Protection");
		
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
		
		if (Config.L2WALKER_PROTECTION)
		{
			LOGGER.info("L2Walker protection actived.");
		}
		
		if (Config.SERVER_RESTART_SCHEDULE_ENABLED)
		{
			ServerRestartManager.getInstance();
		}
		
		if (Config.PRECAUTIONARY_RESTART_ENABLED)
		{
			PrecautionaryRestartManager.getInstance();
		}
		if (Config.DEADLOCK_DETECTOR)
		{
			final DeadLockDetector deadDetectThread = new DeadLockDetector(Duration.ofSeconds(Config.DEADLOCK_CHECK_INTERVAL), () ->
			{
				if (Config.RESTART_ON_DEADLOCK)
				{
					AnnouncementsTable.getInstance().announceToAll("Server has stability issues - restarting now.");
					Shutdown.getInstance().startShutdown(null, 60, true);
				}
			});
			deadDetectThread.setDaemon(true);
			deadDetectThread.start();
		}
		
		printSection("Status");
		
		if (Config.IS_TELNET_ENABLED)
		{
			_statusServer = new TelnetStatusThread();
			_statusServer.start();
		}
		
		System.gc();
		final long totalMem = Runtime.getRuntime().maxMemory() / 1048576;
		LOGGER.info(getClass().getSimpleName() + ": Started, using " + getUsedMemoryMB() + " of " + totalMem + " MB total memory.");
		LOGGER.info(getClass().getSimpleName() + ": Maximum number of connected players is " + Config.MAXIMUM_ONLINE_USERS + ".");
		LOGGER.info(getClass().getSimpleName() + ": Server loaded in " + ((Chronos.currentTimeMillis() - serverLoadStart) / 1000) + " seconds.");
		
		ClientNetworkManager.getInstance().start();
		
		LoginServerThread.getInstance().start();
		
		Toolkit.getDefaultToolkit().beep();
	}
	
	public static void main(String[] args) throws Exception
	{
		INSTANCE = new GameServer();
	}
	
	private void printSection(String section)
	{
		String s = "=[ " + section + " ]";
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