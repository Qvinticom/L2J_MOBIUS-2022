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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.mmocore.network.SelectorConfig;
import org.mmocore.network.SelectorThread;

import com.l2jmobius.Config;
import com.l2jmobius.L2DatabaseFactory;
import com.l2jmobius.Server;
import com.l2jmobius.gameserver.cache.CrestCache;
import com.l2jmobius.gameserver.cache.HtmCache;
import com.l2jmobius.gameserver.communitybbs.Manager.ForumsBBSManager;
import com.l2jmobius.gameserver.datatables.ArmorSetsTable;
import com.l2jmobius.gameserver.datatables.CharNameTable;
import com.l2jmobius.gameserver.datatables.CharTemplateTable;
import com.l2jmobius.gameserver.datatables.ClanTable;
import com.l2jmobius.gameserver.datatables.DoorTable;
import com.l2jmobius.gameserver.datatables.ExtractableItemsData;
import com.l2jmobius.gameserver.datatables.FishTable;
import com.l2jmobius.gameserver.datatables.GmListTable;
import com.l2jmobius.gameserver.datatables.HelperBuffTable;
import com.l2jmobius.gameserver.datatables.HennaTable;
import com.l2jmobius.gameserver.datatables.HennaTreeTable;
import com.l2jmobius.gameserver.datatables.ItemTable;
import com.l2jmobius.gameserver.datatables.LevelUpData;
import com.l2jmobius.gameserver.datatables.MapRegionTable;
import com.l2jmobius.gameserver.datatables.NobleSkillTable;
import com.l2jmobius.gameserver.datatables.NpcBufferTable;
import com.l2jmobius.gameserver.datatables.NpcTable;
import com.l2jmobius.gameserver.datatables.NpcWalkerRoutesTable;
import com.l2jmobius.gameserver.datatables.OfflineTradersTable;
import com.l2jmobius.gameserver.datatables.SkillSpellbookTable;
import com.l2jmobius.gameserver.datatables.SkillTable;
import com.l2jmobius.gameserver.datatables.SkillTreeTable;
import com.l2jmobius.gameserver.datatables.SpawnTable;
import com.l2jmobius.gameserver.datatables.StaticObjects;
import com.l2jmobius.gameserver.datatables.SummonItemsData;
import com.l2jmobius.gameserver.datatables.TeleportLocationTable;
import com.l2jmobius.gameserver.datatables.ZoneTable;
import com.l2jmobius.gameserver.geodata.GeoData;
import com.l2jmobius.gameserver.geodata.pathfinding.PathFinding;
import com.l2jmobius.gameserver.handler.AdminCommandHandler;
import com.l2jmobius.gameserver.handler.ItemHandler;
import com.l2jmobius.gameserver.handler.SkillHandler;
import com.l2jmobius.gameserver.handler.UserCommandHandler;
import com.l2jmobius.gameserver.handler.VoicedCommandHandler;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminAdmin;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminAnnouncements;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminBBS;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminBan;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminBanChat;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminCache;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminChangeAccessLevel;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminCreateItem;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminDelete;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminDisconnect;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminDoorControl;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminEditChar;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminEditNpc;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminEffects;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminEnchant;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminEventEngine;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminExpSp;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminFightCalculator;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminGeodata;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminGm;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminGmChat;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminHeal;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminHelpPage;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminInvul;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminKick;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminKill;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminLevel;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminLogin;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminMammon;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminManor;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminMenu;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminMobGroup;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminMonsterRace;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminPForge;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminPathNode;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminPetition;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminPledge;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminPolymorph;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminQuest;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminRepairChar;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminRes;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminRideWyvern;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminShop;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminShutdown;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminSiege;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminSkill;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminSpawn;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminTarget;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminTeleport;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminTest;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminUnblockIp;
import com.l2jmobius.gameserver.handler.admincommandhandlers.AdminZone;
import com.l2jmobius.gameserver.handler.itemhandlers.BeastSoulShot;
import com.l2jmobius.gameserver.handler.itemhandlers.BeastSpice;
import com.l2jmobius.gameserver.handler.itemhandlers.BeastSpiritShot;
import com.l2jmobius.gameserver.handler.itemhandlers.BlessedSpiritShot;
import com.l2jmobius.gameserver.handler.itemhandlers.Book;
import com.l2jmobius.gameserver.handler.itemhandlers.CharChangePotions;
import com.l2jmobius.gameserver.handler.itemhandlers.ChestKey;
import com.l2jmobius.gameserver.handler.itemhandlers.CrystalCarol;
import com.l2jmobius.gameserver.handler.itemhandlers.EnchantScrolls;
import com.l2jmobius.gameserver.handler.itemhandlers.EnergyStone;
import com.l2jmobius.gameserver.handler.itemhandlers.ExtractableItems;
import com.l2jmobius.gameserver.handler.itemhandlers.Firework;
import com.l2jmobius.gameserver.handler.itemhandlers.FishShots;
import com.l2jmobius.gameserver.handler.itemhandlers.Harvester;
import com.l2jmobius.gameserver.handler.itemhandlers.MercTicket;
import com.l2jmobius.gameserver.handler.itemhandlers.PetFood;
import com.l2jmobius.gameserver.handler.itemhandlers.Potions;
import com.l2jmobius.gameserver.handler.itemhandlers.Recipes;
import com.l2jmobius.gameserver.handler.itemhandlers.Remedy;
import com.l2jmobius.gameserver.handler.itemhandlers.RollingDice;
import com.l2jmobius.gameserver.handler.itemhandlers.ScrollOfEscape;
import com.l2jmobius.gameserver.handler.itemhandlers.ScrollOfResurrection;
import com.l2jmobius.gameserver.handler.itemhandlers.Scrolls;
import com.l2jmobius.gameserver.handler.itemhandlers.Seed;
import com.l2jmobius.gameserver.handler.itemhandlers.SevenSignsRecord;
import com.l2jmobius.gameserver.handler.itemhandlers.SoulCrystals;
import com.l2jmobius.gameserver.handler.itemhandlers.SoulShots;
import com.l2jmobius.gameserver.handler.itemhandlers.SpecialXMas;
import com.l2jmobius.gameserver.handler.itemhandlers.SpiritShot;
import com.l2jmobius.gameserver.handler.itemhandlers.SummonItems;
import com.l2jmobius.gameserver.handler.itemhandlers.WorldMap;
import com.l2jmobius.gameserver.handler.skillhandlers.BalanceLife;
import com.l2jmobius.gameserver.handler.skillhandlers.BeastFeed;
import com.l2jmobius.gameserver.handler.skillhandlers.Blow;
import com.l2jmobius.gameserver.handler.skillhandlers.Charge;
import com.l2jmobius.gameserver.handler.skillhandlers.CombatPointHeal;
import com.l2jmobius.gameserver.handler.skillhandlers.Continuous;
import com.l2jmobius.gameserver.handler.skillhandlers.CpDamPercent;
import com.l2jmobius.gameserver.handler.skillhandlers.Craft;
import com.l2jmobius.gameserver.handler.skillhandlers.DeluxeKey;
import com.l2jmobius.gameserver.handler.skillhandlers.Disablers;
import com.l2jmobius.gameserver.handler.skillhandlers.DrainSoul;
import com.l2jmobius.gameserver.handler.skillhandlers.Fishing;
import com.l2jmobius.gameserver.handler.skillhandlers.FishingSkill;
import com.l2jmobius.gameserver.handler.skillhandlers.GetPlayer;
import com.l2jmobius.gameserver.handler.skillhandlers.Harvest;
import com.l2jmobius.gameserver.handler.skillhandlers.Heal;
import com.l2jmobius.gameserver.handler.skillhandlers.ManaDam;
import com.l2jmobius.gameserver.handler.skillhandlers.ManaHeal;
import com.l2jmobius.gameserver.handler.skillhandlers.Mdam;
import com.l2jmobius.gameserver.handler.skillhandlers.Pdam;
import com.l2jmobius.gameserver.handler.skillhandlers.Recall;
import com.l2jmobius.gameserver.handler.skillhandlers.Resurrect;
import com.l2jmobius.gameserver.handler.skillhandlers.Sow;
import com.l2jmobius.gameserver.handler.skillhandlers.Spoil;
import com.l2jmobius.gameserver.handler.skillhandlers.StrSiegeAssault;
import com.l2jmobius.gameserver.handler.skillhandlers.Sweep;
import com.l2jmobius.gameserver.handler.skillhandlers.Unlock;
import com.l2jmobius.gameserver.handler.usercommandhandlers.ChannelDelete;
import com.l2jmobius.gameserver.handler.usercommandhandlers.ChannelLeave;
import com.l2jmobius.gameserver.handler.usercommandhandlers.ChannelListUpdate;
import com.l2jmobius.gameserver.handler.usercommandhandlers.ClanPenalty;
import com.l2jmobius.gameserver.handler.usercommandhandlers.ClanWarsList;
import com.l2jmobius.gameserver.handler.usercommandhandlers.DisMount;
import com.l2jmobius.gameserver.handler.usercommandhandlers.Escape;
import com.l2jmobius.gameserver.handler.usercommandhandlers.Loc;
import com.l2jmobius.gameserver.handler.usercommandhandlers.Mount;
import com.l2jmobius.gameserver.handler.usercommandhandlers.OlympiadStat;
import com.l2jmobius.gameserver.handler.usercommandhandlers.PartyInfo;
import com.l2jmobius.gameserver.handler.usercommandhandlers.Time;
import com.l2jmobius.gameserver.handler.voicedcommandhandlers.TvTCommand;
import com.l2jmobius.gameserver.handler.voicedcommandhandlers.VoiceExperience;
import com.l2jmobius.gameserver.handler.voicedcommandhandlers.stats;
import com.l2jmobius.gameserver.idfactory.IdFactory;
import com.l2jmobius.gameserver.instancemanager.AuctionManager;
import com.l2jmobius.gameserver.instancemanager.BoatManager;
import com.l2jmobius.gameserver.instancemanager.CastleManager;
import com.l2jmobius.gameserver.instancemanager.CastleManorManager;
import com.l2jmobius.gameserver.instancemanager.ClanHallManager;
import com.l2jmobius.gameserver.instancemanager.DayNightSpawnManager;
import com.l2jmobius.gameserver.instancemanager.DimensionalRiftManager;
import com.l2jmobius.gameserver.instancemanager.FourSepulchersManager;
import com.l2jmobius.gameserver.instancemanager.GrandBossManager;
import com.l2jmobius.gameserver.instancemanager.ItemsOnGroundManager;
import com.l2jmobius.gameserver.instancemanager.MercTicketManager;
import com.l2jmobius.gameserver.instancemanager.PetitionManager;
import com.l2jmobius.gameserver.instancemanager.QuestManager;
import com.l2jmobius.gameserver.instancemanager.RaidBossSpawnManager;
import com.l2jmobius.gameserver.instancemanager.SiegeManager;
import com.l2jmobius.gameserver.model.AutoChatHandler;
import com.l2jmobius.gameserver.model.AutoSpawnHandler;
import com.l2jmobius.gameserver.model.EventEngine;
import com.l2jmobius.gameserver.model.L2Manor;
import com.l2jmobius.gameserver.model.L2Multisell;
import com.l2jmobius.gameserver.model.L2PetDataTable;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.PartyMatchRoomList;
import com.l2jmobius.gameserver.model.entity.AutoRewarder;
import com.l2jmobius.gameserver.model.entity.Hero;
import com.l2jmobius.gameserver.network.L2GameClient;
import com.l2jmobius.gameserver.network.L2GamePacketHandler;
import com.l2jmobius.gameserver.script.faenor.FaenorScriptEngine;
import com.l2jmobius.gameserver.scripting.L2ScriptEngineManager;
import com.l2jmobius.gameserver.taskmanager.AutoAnnounceTaskManager;
import com.l2jmobius.gameserver.taskmanager.KnownListUpdateTaskManager;
import com.l2jmobius.gameserver.taskmanager.TaskManager;
import com.l2jmobius.gameserver.util.DynamicExtension;
import com.l2jmobius.status.Status;
import com.l2jmobius.util.DeadLockDetector;
import com.l2jmobius.util.IPv4Filter;

/**
 * This class ...
 * @version $Revision: 1.29.2.15.2.19 $ $Date: 2005/04/05 19:41:23 $
 */
public class GameServer
{
	private static final Logger _log = Logger.getLogger(GameServer.class.getName());
	
	private final SelectorThread<L2GameClient> _selectorThread;
	private final DeadLockDetector _deadDetectThread;
	private final ItemTable _itemTable;
	private final NpcTable _npcTable;
	private final HennaTable _hennaTable;
	private final IdFactory _idFactory;
	public static GameServer gameServer;
	
	private final ItemHandler _itemHandler;
	private final SkillHandler _skillHandler;
	private final AdminCommandHandler _adminCommandHandler;
	private final Shutdown _shutdownHandler;
	private final UserCommandHandler _userCommandHandler;
	private final VoicedCommandHandler _voicedCommandHandler;
	private final DoorTable _doorTable;
	private final SevenSigns _sevenSignsEngine;
	private final AutoChatHandler _autoChatHandler;
	private final AutoSpawnHandler _autoSpawnHandler;
	private final LoginServerThread _loginThread;
	private final HelperBuffTable _helperBuffTable;
	
	public static Status statusServer;
	@SuppressWarnings("unused")
	private final ThreadPoolManager _threadpools;
	
	public static final Calendar DateTimeServerStarted = Calendar.getInstance();
	
	public long getUsedMemoryMB()
	{
		return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576; // 1024 * 1024 = 1048576
	}
	
	public SelectorThread<L2GameClient> getSelectorThread()
	{
		return _selectorThread;
	}
	
	public GameServer() throws Exception
	{
		final long serverLoadStart = System.currentTimeMillis();
		gameServer = this;
		_log.finest("used mem:" + getUsedMemoryMB() + "MB");
		
		_idFactory = IdFactory.getInstance();
		if (!_idFactory.isInitialized())
		{
			_log.severe("Could not read object IDs from DB. Please Check Your Data.");
			throw new Exception("Could not initialize the ID factory");
		}
		
		_threadpools = ThreadPoolManager.getInstance();
		
		new File(Config.DATAPACK_ROOT, "data/clans").mkdirs();
		new File(Config.DATAPACK_ROOT, "data/crests").mkdirs();
		
		// load script engines
		L2ScriptEngineManager.getInstance();
		
		// start game time control early
		GameTimeController.getInstance();
		
		// keep the references of Singletons to prevent garbage collection
		CharNameTable.getInstance();
		
		_itemTable = ItemTable.getInstance();
		if (!_itemTable.isInitialized())
		{
			_log.severe("Could not find the extraced files. Please Check Your Data.");
			throw new Exception("Could not initialize the item table");
		}
		
		ExtractableItemsData.getInstance();
		SummonItemsData.getInstance();
		
		TradeController.getInstance();
		L2Multisell.getInstance();
		
		SkillTable.getInstance();
		
		// L2EMU_ADD by Rayan. L2J - BigBro
		if (Config.ALLOW_NPC_WALKERS)
		{
			NpcWalkerRoutesTable.getInstance();
		}
		
		RecipeController.getInstance();
		
		if (Config.NPC_BUFFER_ENABLED)
		{
			NpcBufferTable.getInstance();
		}
		
		SkillTreeTable.getInstance();
		ArmorSetsTable.getInstance();
		FishTable.getInstance();
		
		if (Config.SP_BOOK_NEEDED)
		{
			SkillSpellbookTable.getInstance();
		}
		
		CharTemplateTable.getInstance();
		NobleSkillTable.getInstance();
		
		// Call to load caches
		HtmCache.getInstance();
		CrestCache.getInstance();
		
		// forum has to be loaded before clan data, because of last forum id used should have also memo included
		if (Config.COMMUNITY_TYPE > 0)
		{
			ForumsBBSManager.getInstance().initRoot();
		}
		
		ClanTable.getInstance();
		
		GeoData.getInstance();
		if (Config.PATHFINDING > 0)
		{
			PathFinding.getInstance();
		}
		
		_npcTable = NpcTable.getInstance();
		
		if (!_npcTable.isInitialized())
		{
			_log.severe("Could not find the extraced files. Please Check Your Data.");
			throw new Exception("Could not initialize the npc table");
		}
		
		_hennaTable = HennaTable.getInstance();
		
		if (!_hennaTable.isInitialized())
		{
			throw new Exception("Could not initialize the Henna Table");
		}
		
		HennaTreeTable.getInstance();
		
		if (!_hennaTable.isInitialized())
		{
			throw new Exception("Could not initialize the Henna Tree Table");
		}
		
		_helperBuffTable = HelperBuffTable.getInstance();
		
		if (!_helperBuffTable.isInitialized())
		{
			throw new Exception("Could not initialize the Helper Buff Table");
		}
		
		// Load clan hall data before zone data
		ClanHallManager.getInstance();
		CastleManager.getInstance();
		SiegeManager.getInstance();
		
		TeleportLocationTable.getInstance();
		LevelUpData.getInstance();
		L2World.getInstance();
		ZoneTable.getInstance();
		SpawnTable.getInstance();
		RaidBossSpawnManager.getInstance();
		DayNightSpawnManager.getInstance().notifyChangeMode();
		DimensionalRiftManager.getInstance();
		GrandBossManager.getInstance();
		FourSepulchersManager.getInstance().init();
		Announcements.getInstance();
		MapRegionTable.getInstance();
		EventDroplist.getInstance();
		
		/** Load Manor data */
		L2Manor.getInstance();
		
		AuctionManager.getInstance();
		
		BoatManager.getInstance();
		CastleManorManager.getInstance();
		
		MercTicketManager.getInstance();
		PartyMatchRoomList.getInstance();
		PetitionManager.getInstance();
		QuestManager.getInstance();
		
		try
		{
			_log.info("Loading Server Scripts");
			final File scripts = new File(Config.DATAPACK_ROOT + "/data/scripts.cfg");
			L2ScriptEngineManager.getInstance().executeScriptList(scripts);
		}
		catch (final Exception ioe)
		{
			_log.severe("Failed loading scripts.cfg, no script is going to be loaded");
		}
		
		QuestManager.getInstance().report();
		
		if (Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance();
		}
		
		if (Config.AUTODESTROY_ITEM_AFTER > 0)
		{
			ItemsAutoDestroy.getInstance();
		}
		
		MonsterRace.getInstance();
		
		_doorTable = DoorTable.getInstance();
		_doorTable.parseData();
		StaticObjects.getInstance();
		
		_sevenSignsEngine = SevenSigns.getInstance();
		SevenSignsFestival.getInstance();
		_autoSpawnHandler = AutoSpawnHandler.getInstance();
		_autoChatHandler = AutoChatHandler.getInstance();
		
		// Spawn the Orators/Preachers if in the Seal Validation period.
		_sevenSignsEngine.spawnSevenSignsNPC();
		
		Olympiad.getInstance();
		Hero.getInstance();
		
		FaenorScriptEngine.getInstance();
		
		_log.config("AutoChatHandler: Loaded " + _autoChatHandler.size() + " handlers in total.");
		_log.config("AutoSpawnHandler: Loaded " + _autoSpawnHandler.size() + " handlers in total.");
		
		_itemHandler = ItemHandler.getInstance();
		_itemHandler.registerItemHandler(new ScrollOfEscape());
		_itemHandler.registerItemHandler(new ScrollOfResurrection());
		_itemHandler.registerItemHandler(new SoulShots());
		_itemHandler.registerItemHandler(new SpecialXMas());
		_itemHandler.registerItemHandler(new SpiritShot());
		_itemHandler.registerItemHandler(new BlessedSpiritShot());
		_itemHandler.registerItemHandler(new BeastSoulShot());
		_itemHandler.registerItemHandler(new BeastSpiritShot());
		_itemHandler.registerItemHandler(new ChestKey());
		_itemHandler.registerItemHandler(new WorldMap());
		_itemHandler.registerItemHandler(new PetFood());
		_itemHandler.registerItemHandler(new Potions());
		_itemHandler.registerItemHandler(new Recipes());
		
		_itemHandler.registerItemHandler(new RollingDice());
		_itemHandler.registerItemHandler(new EnchantScrolls());
		_itemHandler.registerItemHandler(new EnergyStone());
		_itemHandler.registerItemHandler(new Book());
		_itemHandler.registerItemHandler(new Remedy());
		_itemHandler.registerItemHandler(new Scrolls());
		_itemHandler.registerItemHandler(new CrystalCarol());
		_itemHandler.registerItemHandler(new SoulCrystals());
		_itemHandler.registerItemHandler(new SevenSignsRecord());
		_itemHandler.registerItemHandler(new CharChangePotions());
		_itemHandler.registerItemHandler(new Firework());
		
		_itemHandler.registerItemHandler(new Seed());
		_itemHandler.registerItemHandler(new Harvester());
		_itemHandler.registerItemHandler(new MercTicket());
		_itemHandler.registerItemHandler(new FishShots());
		_itemHandler.registerItemHandler(new ExtractableItems());
		_itemHandler.registerItemHandler(new SummonItems());
		_itemHandler.registerItemHandler(new BeastSpice());
		_log.config("ItemHandler: Loaded " + _itemHandler.size() + " handlers.");
		
		_skillHandler = SkillHandler.getInstance();
		_skillHandler.registerSkillHandler(new Blow());
		_skillHandler.registerSkillHandler(new Pdam());
		_skillHandler.registerSkillHandler(new Mdam());
		_skillHandler.registerSkillHandler(new Heal());
		_skillHandler.registerSkillHandler(new CombatPointHeal());
		_skillHandler.registerSkillHandler(new ManaDam());
		_skillHandler.registerSkillHandler(new ManaHeal());
		_skillHandler.registerSkillHandler(new BalanceLife());
		_skillHandler.registerSkillHandler(new Charge());
		_skillHandler.registerSkillHandler(new Continuous());
		_skillHandler.registerSkillHandler(new CpDamPercent());
		_skillHandler.registerSkillHandler(new Resurrect());
		_skillHandler.registerSkillHandler(new Spoil());
		_skillHandler.registerSkillHandler(new Sweep());
		_skillHandler.registerSkillHandler(new StrSiegeAssault());
		_skillHandler.registerSkillHandler(new Disablers());
		_skillHandler.registerSkillHandler(new Recall());
		_skillHandler.registerSkillHandler(new Unlock());
		_skillHandler.registerSkillHandler(new DrainSoul());
		_skillHandler.registerSkillHandler(new Craft());
		_skillHandler.registerSkillHandler(new Fishing());
		_skillHandler.registerSkillHandler(new FishingSkill());
		_skillHandler.registerSkillHandler(new Sow());
		_skillHandler.registerSkillHandler(new Harvest());
		_skillHandler.registerSkillHandler(new DeluxeKey());
		_skillHandler.registerSkillHandler(new BeastFeed());
		_skillHandler.registerSkillHandler(new GetPlayer());
		_log.config("SkillHandler: Loaded " + _skillHandler.size() + " handlers.");
		
		_adminCommandHandler = AdminCommandHandler.getInstance();
		_adminCommandHandler.registerAdminCommandHandler(new AdminAdmin());
		_adminCommandHandler.registerAdminCommandHandler(new AdminInvul());
		_adminCommandHandler.registerAdminCommandHandler(new AdminDelete());
		_adminCommandHandler.registerAdminCommandHandler(new AdminKill());
		_adminCommandHandler.registerAdminCommandHandler(new AdminTarget());
		_adminCommandHandler.registerAdminCommandHandler(new AdminShop());
		_adminCommandHandler.registerAdminCommandHandler(new AdminAnnouncements());
		_adminCommandHandler.registerAdminCommandHandler(new AdminCreateItem());
		_adminCommandHandler.registerAdminCommandHandler(new AdminHeal());
		_adminCommandHandler.registerAdminCommandHandler(new AdminHelpPage());
		_adminCommandHandler.registerAdminCommandHandler(new AdminShutdown());
		_adminCommandHandler.registerAdminCommandHandler(new AdminSpawn());
		_adminCommandHandler.registerAdminCommandHandler(new AdminSkill());
		_adminCommandHandler.registerAdminCommandHandler(new AdminExpSp());
		_adminCommandHandler.registerAdminCommandHandler(new AdminEventEngine());
		_adminCommandHandler.registerAdminCommandHandler(new AdminGmChat());
		_adminCommandHandler.registerAdminCommandHandler(new AdminEditChar());
		_adminCommandHandler.registerAdminCommandHandler(new AdminGm());
		_adminCommandHandler.registerAdminCommandHandler(new AdminTeleport());
		_adminCommandHandler.registerAdminCommandHandler(new AdminRepairChar());
		_adminCommandHandler.registerAdminCommandHandler(new AdminChangeAccessLevel());
		_adminCommandHandler.registerAdminCommandHandler(new AdminBan());
		_adminCommandHandler.registerAdminCommandHandler(new AdminPolymorph());
		_adminCommandHandler.registerAdminCommandHandler(new AdminBanChat());
		_adminCommandHandler.registerAdminCommandHandler(new AdminKick());
		_adminCommandHandler.registerAdminCommandHandler(new AdminDisconnect());
		_adminCommandHandler.registerAdminCommandHandler(new AdminMonsterRace());
		_adminCommandHandler.registerAdminCommandHandler(new AdminEditNpc());
		_adminCommandHandler.registerAdminCommandHandler(new AdminFightCalculator());
		_adminCommandHandler.registerAdminCommandHandler(new AdminMenu());
		_adminCommandHandler.registerAdminCommandHandler(new AdminSiege());
		_adminCommandHandler.registerAdminCommandHandler(new AdminPathNode());
		_adminCommandHandler.registerAdminCommandHandler(new AdminPetition());
		_adminCommandHandler.registerAdminCommandHandler(new AdminPForge());
		_adminCommandHandler.registerAdminCommandHandler(new AdminBBS());
		_adminCommandHandler.registerAdminCommandHandler(new AdminEffects());
		_adminCommandHandler.registerAdminCommandHandler(new AdminDoorControl());
		_adminCommandHandler.registerAdminCommandHandler(new AdminTest());
		_adminCommandHandler.registerAdminCommandHandler(new AdminEnchant());
		_adminCommandHandler.registerAdminCommandHandler(new AdminMobGroup());
		_adminCommandHandler.registerAdminCommandHandler(new AdminRes());
		_adminCommandHandler.registerAdminCommandHandler(new AdminMammon());
		_adminCommandHandler.registerAdminCommandHandler(new AdminUnblockIp());
		_adminCommandHandler.registerAdminCommandHandler(new AdminPledge());
		_adminCommandHandler.registerAdminCommandHandler(new AdminRideWyvern());
		_adminCommandHandler.registerAdminCommandHandler(new AdminLogin());
		_adminCommandHandler.registerAdminCommandHandler(new AdminCache());
		_adminCommandHandler.registerAdminCommandHandler(new AdminLevel());
		_adminCommandHandler.registerAdminCommandHandler(new AdminQuest());
		_adminCommandHandler.registerAdminCommandHandler(new AdminZone());
		_adminCommandHandler.registerAdminCommandHandler(new AdminGeodata());
		_adminCommandHandler.registerAdminCommandHandler(new AdminManor());
		
		// _adminCommandHandler.registerAdminCommandHandler(new AdminRadar());
		_log.config("AdminCommandHandler: Loaded " + _adminCommandHandler.size() + " handlers.");
		
		_userCommandHandler = UserCommandHandler.getInstance();
		_userCommandHandler.registerUserCommandHandler(new ClanPenalty());
		_userCommandHandler.registerUserCommandHandler(new ClanWarsList());
		_userCommandHandler.registerUserCommandHandler(new DisMount());
		_userCommandHandler.registerUserCommandHandler(new Escape());
		_userCommandHandler.registerUserCommandHandler(new Loc());
		_userCommandHandler.registerUserCommandHandler(new Mount());
		_userCommandHandler.registerUserCommandHandler(new OlympiadStat());
		
		_userCommandHandler.registerUserCommandHandler(new PartyInfo());
		_userCommandHandler.registerUserCommandHandler(new Time());
		_userCommandHandler.registerUserCommandHandler(new ChannelLeave());
		_userCommandHandler.registerUserCommandHandler(new ChannelDelete());
		_userCommandHandler.registerUserCommandHandler(new ChannelListUpdate());
		
		_log.config("UserCommandHandler: Loaded " + _userCommandHandler.size() + " handlers.");
		
		_voicedCommandHandler = VoicedCommandHandler.getInstance();
		_voicedCommandHandler.registerVoicedCommandHandler(new stats());
		
		if (Config.Boost_EXP_COMMAND)
		{
			_voicedCommandHandler.registerVoicedCommandHandler(new VoiceExperience());
		}
		
		_voicedCommandHandler.registerVoicedCommandHandler(new TvTCommand());
		
		_log.config("VoicedCommandHandler: Loaded " + _voicedCommandHandler.size() + " handlers.");
		
		TaskManager.getInstance();
		
		GmListTable.getInstance();
		
		// read pet stats from db
		L2PetDataTable.getInstance().loadPetsData();
		
		_shutdownHandler = Shutdown.getInstance();
		Runtime.getRuntime().addShutdownHook(_shutdownHandler);
		
		try
		{
			_doorTable.getDoor(24190001).openMe();
			_doorTable.getDoor(24190002).openMe();
			_doorTable.getDoor(24190003).openMe();
			_doorTable.getDoor(24190004).openMe();
			_doorTable.getDoor(23180001).openMe();
			_doorTable.getDoor(23180002).openMe();
			_doorTable.getDoor(23180003).openMe();
			_doorTable.getDoor(23180004).openMe();
			_doorTable.getDoor(23180005).openMe();
			_doorTable.getDoor(23180006).openMe();
			
			_doorTable.checkAutoOpen();
		}
		catch (final NullPointerException e)
		{
			_log.warning("There is an error in your Door.csv file. Please update that file.");
			if (Config.DEBUG)
			{
				e.printStackTrace();
			}
		}
		
		_log.config("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());
		
		// initialize the dynamic extension loader
		try
		{
			DynamicExtension.getInstance();
		}
		catch (final Exception ex)
		{
			_log.log(Level.WARNING, "DynamicExtension could not be loaded and initialized", ex);
		}
		
		if ((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.RESTORE_OFFLINERS)
		{
			OfflineTradersTable.restoreOfflineTraders();
		}
		
		if (Config.ALLOW_AUTO_REWARDER)
		{
			AutoRewarder.load();
		}
		
		// Start Event Engine
		EventEngine.load();
		
		KnownListUpdateTaskManager.getInstance();
		
		if (Config.DEADLOCK_DETECTOR)
		{
			_deadDetectThread = new DeadLockDetector();
			_deadDetectThread.setDaemon(true);
			_deadDetectThread.start();
		}
		else
		{
			_deadDetectThread = null;
		}
		
		System.gc();
		// maxMemory is the upper limit the JVM can use, totalMemory the size of the current allocation pool, freeMemory the unused memory in the allocation pool
		final long freeMem = ((Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory()) + Runtime.getRuntime().freeMemory()) / 1048576; // 1024 * 1024 = 1048576
		final long totalMem = Runtime.getRuntime().maxMemory() / 1048576;
		_log.info("GameServer Started, free memory " + freeMem + " Mb of " + totalMem + " Mb");
		
		_loginThread = LoginServerThread.getInstance();
		_loginThread.start();
		
		final SelectorConfig sc = new SelectorConfig();
		sc.MAX_READ_PER_PASS = Config.MMO_MAX_READ_PER_PASS;
		sc.MAX_SEND_PER_PASS = Config.MMO_MAX_SEND_PER_PASS;
		sc.SLEEP_TIME = Config.MMO_SELECTOR_SLEEP_TIME;
		sc.HELPER_BUFFER_COUNT = Config.MMO_HELPER_BUFFER_COUNT;
		
		final L2GamePacketHandler gph = new L2GamePacketHandler();
		_selectorThread = new SelectorThread<>(sc, gph, gph, gph, new IPv4Filter());
		
		InetAddress bindAddress = null;
		if (!Config.GAMESERVER_HOSTNAME.equals("*"))
		{
			try
			{
				bindAddress = InetAddress.getByName(Config.GAMESERVER_HOSTNAME);
			}
			catch (final UnknownHostException e1)
			{
				_log.log(Level.SEVERE, "WARNING: The GameServer bind address is invalid, using all avaliable IPs. Reason: " + e1.getMessage(), e1);
			}
		}
		
		try
		{
			_selectorThread.openServerSocket(bindAddress, Config.PORT_GAME);
		}
		catch (final IOException e)
		{
			_log.log(Level.SEVERE, "FATAL: Failed to open server socket. Reason: " + e.getMessage(), e);
			System.exit(1);
		}
		_selectorThread.start();
		
		_log.config("Maximum Number of Connected Players: " + Config.MAXIMUM_ONLINE_USERS);
		_log.log(Level.INFO, getClass().getSimpleName() + ": Server loaded in " + ((System.currentTimeMillis() - serverLoadStart) / 1000) + " seconds.");
		
		AutoAnnounceTaskManager.getInstance();
	}
	
	public static void main(String[] args) throws Exception
	{
		Server.SERVER_MODE = Server.MODE_GAMESERVER;
		
		// Local Constants
		final String LOG_FOLDER = "log"; // Name of folder for log file
		final String LOG_NAME = "./log.cfg"; // Name of log file
		
		/*** Main ***/
		// Create log folder
		final File logFolder = new File(Config.DATAPACK_ROOT, LOG_FOLDER);
		logFolder.mkdir();
		
		// Create input stream for log file -- or store file data into memory
		try (InputStream is = new FileInputStream(new File(LOG_NAME)))
		{
			LogManager.getLogManager().readConfiguration(is);
		}
		
		// Initialize config
		Config.load();
		
		L2DatabaseFactory.getInstance();
		gameServer = new GameServer();
		
		if (Config.IS_TELNET_ENABLED)
		{
			statusServer = new Status(Server.SERVER_MODE);
			statusServer.start();
		}
		else
		{
			System.out.println("Telnet server is currently disabled.");
		}
	}
}