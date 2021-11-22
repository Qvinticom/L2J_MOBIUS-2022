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
package org.l2jmobius.gameserver.model.actor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.LoginServerThread;
import org.l2jmobius.gameserver.ai.CreatureAI;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.ai.PlayerAI;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.cache.WarehouseCacheManager;
import org.l2jmobius.gameserver.communitybbs.BB.Forum;
import org.l2jmobius.gameserver.communitybbs.Manager.ForumsBBSManager;
import org.l2jmobius.gameserver.data.HeroSkillTable;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.data.NobleSkillTable;
import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.data.sql.AnnouncementsTable;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.data.sql.NpcTable;
import org.l2jmobius.gameserver.data.sql.SkillTreeTable;
import org.l2jmobius.gameserver.data.xml.AdminData;
import org.l2jmobius.gameserver.data.xml.ExperienceData;
import org.l2jmobius.gameserver.data.xml.FishData;
import org.l2jmobius.gameserver.data.xml.HennaData;
import org.l2jmobius.gameserver.data.xml.MapRegionData;
import org.l2jmobius.gameserver.data.xml.PlayerTemplateData;
import org.l2jmobius.gameserver.data.xml.RecipeData;
import org.l2jmobius.gameserver.data.xml.ZoneData;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.enums.TeleportWhereType;
import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.handler.ItemHandler;
import org.l2jmobius.gameserver.handler.admincommandhandlers.AdminEditChar;
import org.l2jmobius.gameserver.handler.skillhandlers.SiegeFlag;
import org.l2jmobius.gameserver.handler.skillhandlers.StrSiegeAssault;
import org.l2jmobius.gameserver.handler.skillhandlers.TakeCastle;
import org.l2jmobius.gameserver.instancemanager.CastleManager;
import org.l2jmobius.gameserver.instancemanager.CoupleManager;
import org.l2jmobius.gameserver.instancemanager.CursedWeaponsManager;
import org.l2jmobius.gameserver.instancemanager.DimensionalRiftManager;
import org.l2jmobius.gameserver.instancemanager.DuelManager;
import org.l2jmobius.gameserver.instancemanager.FortSiegeManager;
import org.l2jmobius.gameserver.instancemanager.ItemsOnGroundManager;
import org.l2jmobius.gameserver.instancemanager.QuestManager;
import org.l2jmobius.gameserver.instancemanager.RebirthManager;
import org.l2jmobius.gameserver.instancemanager.RecipeManager;
import org.l2jmobius.gameserver.instancemanager.SiegeManager;
import org.l2jmobius.gameserver.model.AccessLevel;
import org.l2jmobius.gameserver.model.BlockList;
import org.l2jmobius.gameserver.model.Duel;
import org.l2jmobius.gameserver.model.Effect;
import org.l2jmobius.gameserver.model.Fish;
import org.l2jmobius.gameserver.model.Fishing;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.Macro;
import org.l2jmobius.gameserver.model.MacroList;
import org.l2jmobius.gameserver.model.ManufactureList;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.Radar;
import org.l2jmobius.gameserver.model.RecipeList;
import org.l2jmobius.gameserver.model.Request;
import org.l2jmobius.gameserver.model.ShortCut;
import org.l2jmobius.gameserver.model.ShortCuts;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.Skill.SkillTargetType;
import org.l2jmobius.gameserver.model.Skill.SkillType;
import org.l2jmobius.gameserver.model.SkillLearn;
import org.l2jmobius.gameserver.model.SubClass;
import org.l2jmobius.gameserver.model.Timestamp;
import org.l2jmobius.gameserver.model.TradeList;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.WorldRegion;
import org.l2jmobius.gameserver.model.actor.appearance.PlayerAppearance;
import org.l2jmobius.gameserver.model.actor.instance.Boat;
import org.l2jmobius.gameserver.model.actor.instance.Cubic;
import org.l2jmobius.gameserver.model.actor.instance.Door;
import org.l2jmobius.gameserver.model.actor.instance.FestivalMonster;
import org.l2jmobius.gameserver.model.actor.instance.Folk;
import org.l2jmobius.gameserver.model.actor.instance.FortSiegeGuard;
import org.l2jmobius.gameserver.model.actor.instance.GrandBoss;
import org.l2jmobius.gameserver.model.actor.instance.Guard;
import org.l2jmobius.gameserver.model.actor.instance.Monster;
import org.l2jmobius.gameserver.model.actor.instance.RaidBoss;
import org.l2jmobius.gameserver.model.actor.instance.Servitor;
import org.l2jmobius.gameserver.model.actor.instance.SiegeGuard;
import org.l2jmobius.gameserver.model.actor.instance.TamedBeast;
import org.l2jmobius.gameserver.model.actor.knownlist.PlayerKnownList;
import org.l2jmobius.gameserver.model.actor.stat.PlayerStat;
import org.l2jmobius.gameserver.model.actor.status.PlayerStatus;
import org.l2jmobius.gameserver.model.actor.templates.PlayerTemplate;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.clan.ClanMember;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.holders.PlayerStatsHolder;
import org.l2jmobius.gameserver.model.holders.SkillUseHolder;
import org.l2jmobius.gameserver.model.holders.SummonRequestHolder;
import org.l2jmobius.gameserver.model.item.Armor;
import org.l2jmobius.gameserver.model.item.Henna;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.Weapon;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.item.type.ArmorType;
import org.l2jmobius.gameserver.model.item.type.EtcItemType;
import org.l2jmobius.gameserver.model.item.type.WeaponType;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.model.itemcontainer.ItemContainer;
import org.l2jmobius.gameserver.model.itemcontainer.PetInventory;
import org.l2jmobius.gameserver.model.itemcontainer.PlayerFreight;
import org.l2jmobius.gameserver.model.itemcontainer.PlayerInventory;
import org.l2jmobius.gameserver.model.itemcontainer.PlayerWarehouse;
import org.l2jmobius.gameserver.model.olympiad.Hero;
import org.l2jmobius.gameserver.model.olympiad.Olympiad;
import org.l2jmobius.gameserver.model.partymatching.PartyMatchRoom;
import org.l2jmobius.gameserver.model.partymatching.PartyMatchRoomList;
import org.l2jmobius.gameserver.model.partymatching.PartyMatchWaitingList;
import org.l2jmobius.gameserver.model.quest.EventType;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.model.sevensigns.SevenSigns;
import org.l2jmobius.gameserver.model.sevensigns.SevenSignsFestival;
import org.l2jmobius.gameserver.model.siege.Castle;
import org.l2jmobius.gameserver.model.siege.FortSiege;
import org.l2jmobius.gameserver.model.siege.Siege;
import org.l2jmobius.gameserver.model.siege.clanhalls.DevastatedCastle;
import org.l2jmobius.gameserver.model.skill.BaseStat;
import org.l2jmobius.gameserver.model.skill.Formulas;
import org.l2jmobius.gameserver.model.skill.Stat;
import org.l2jmobius.gameserver.model.skill.effects.EffectCharge;
import org.l2jmobius.gameserver.model.skill.handlers.SkillSummon;
import org.l2jmobius.gameserver.model.variables.AccountVariables;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.model.zone.type.WaterZone;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.ChangeWaitType;
import org.l2jmobius.gameserver.network.serverpackets.CharInfo;
import org.l2jmobius.gameserver.network.serverpackets.ConfirmDlg;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.EtcStatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.ExAutoSoulShot;
import org.l2jmobius.gameserver.network.serverpackets.ExDuelUpdateUserInfo;
import org.l2jmobius.gameserver.network.serverpackets.ExFishingEnd;
import org.l2jmobius.gameserver.network.serverpackets.ExFishingStart;
import org.l2jmobius.gameserver.network.serverpackets.ExOlympiadMode;
import org.l2jmobius.gameserver.network.serverpackets.ExOlympiadUserInfo;
import org.l2jmobius.gameserver.network.serverpackets.ExPCCafePointInfo;
import org.l2jmobius.gameserver.network.serverpackets.ExSetCompassZoneCode;
import org.l2jmobius.gameserver.network.serverpackets.FriendList;
import org.l2jmobius.gameserver.network.serverpackets.HennaInfo;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.ItemList;
import org.l2jmobius.gameserver.network.serverpackets.LeaveWorld;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillCanceld;
import org.l2jmobius.gameserver.network.serverpackets.MyTargetSelected;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.NpcInfo;
import org.l2jmobius.gameserver.network.serverpackets.ObservationMode;
import org.l2jmobius.gameserver.network.serverpackets.ObservationReturn;
import org.l2jmobius.gameserver.network.serverpackets.PartySmallWindowUpdate;
import org.l2jmobius.gameserver.network.serverpackets.PetInventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import org.l2jmobius.gameserver.network.serverpackets.PrivateStoreListBuy;
import org.l2jmobius.gameserver.network.serverpackets.PrivateStoreListSell;
import org.l2jmobius.gameserver.network.serverpackets.QuestList;
import org.l2jmobius.gameserver.network.serverpackets.RecipeShopSellList;
import org.l2jmobius.gameserver.network.serverpackets.RelationChanged;
import org.l2jmobius.gameserver.network.serverpackets.Ride;
import org.l2jmobius.gameserver.network.serverpackets.SendTradeDone;
import org.l2jmobius.gameserver.network.serverpackets.SetupGauge;
import org.l2jmobius.gameserver.network.serverpackets.ShortBuffStatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.ShortCutInit;
import org.l2jmobius.gameserver.network.serverpackets.ShortCutRegister;
import org.l2jmobius.gameserver.network.serverpackets.SkillCoolTime;
import org.l2jmobius.gameserver.network.serverpackets.SkillList;
import org.l2jmobius.gameserver.network.serverpackets.Snoop;
import org.l2jmobius.gameserver.network.serverpackets.SocialAction;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.StopMove;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.TargetSelected;
import org.l2jmobius.gameserver.network.serverpackets.TitleUpdate;
import org.l2jmobius.gameserver.network.serverpackets.TradePressOtherOk;
import org.l2jmobius.gameserver.network.serverpackets.TradePressOwnOk;
import org.l2jmobius.gameserver.network.serverpackets.TradeStart;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;
import org.l2jmobius.gameserver.network.serverpackets.ValidateLocation;
import org.l2jmobius.gameserver.taskmanager.GameTimeTaskManager;
import org.l2jmobius.gameserver.taskmanager.ItemsAutoDestroyTaskManager;
import org.l2jmobius.gameserver.taskmanager.PlayerAutoSaveTaskManager;
import org.l2jmobius.gameserver.taskmanager.PvpFlagTaskManager;
import org.l2jmobius.gameserver.util.Broadcast;
import org.l2jmobius.gameserver.util.IllegalPlayerAction;
import org.l2jmobius.gameserver.util.Util;

/**
 * This class represents all player characters in the world.<br>
 * There is always a client-thread connected to this (except if a player-store is activated upon logout).
 */
public class Player extends Playable
{
	/** SQL queries */
	private static final String UPDATE_CHARACTER = "UPDATE characters SET level=?,maxHp=?,curHp=?,maxCp=?,curCp=?,maxMp=?,curMp=?,str=?,con=?,dex=?,_int=?,men=?,wit=?,face=?,hairStyle=?,hairColor=?,heading=?,x=?,y=?,z=?,exp=?,expBeforeDeath=?,sp=?,karma=?,pvpkills=?,pkkills=?,rec_have=?,rec_left=?,clanid=?,maxload=?,race=?,classid=?,deletetime=?,title=?,accesslevel=?,online=?,isin7sdungeon=?,clan_privs=?,wantspeace=?,base_class=?,onlinetime=?,punish_level=?,punish_timer=?,newbie=?,nobless=?,power_grade=?,subpledge=?,last_recom_date=?,lvl_joined_academy=?,apprentice=?,sponsor=?,varka_ketra_ally=?,clan_join_expiry_time=?,clan_create_expiry_time=?,char_name=?,death_penalty_level=?,pc_point=?,name_color=?,title_color=?,aio=?,aio_end=? WHERE charId=?";
	private static final String RESTORE_CHARACTER = "SELECT account_name, charId, char_name, level, maxHp, curHp, maxCp, curCp, maxMp, curMp, acc, crit, evasion, mAtk, mDef, mSpd, pAtk, pDef, pSpd, runSpd, walkSpd, str, con, dex, _int, men, wit, face, hairStyle, hairColor, sex, heading, x, y, z, movement_multiplier, attack_speed_multiplier, colRad, colHeight, exp, expBeforeDeath, sp, karma, pvpkills, pkkills, clanid, maxload, race, classid, deletetime, cancraft, title, rec_have, rec_left, accesslevel, online, char_slot, lastAccess, clan_privs, wantspeace, base_class, onlinetime, isin7sdungeon,punish_level,punish_timer,newbie, nobless, power_grade, subpledge, last_recom_date, lvl_joined_academy, apprentice, sponsor, varka_ketra_ally,clan_join_expiry_time,clan_create_expiry_time,death_penalty_level,pc_point,name_color,title_color,first_log,aio,aio_end FROM characters WHERE charId=?";
	private static final String RESTORE_SKILLS_FOR_CHAR_ALT_SUBCLASS = "SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=? ORDER BY (skill_level+0)";
	private static final String RESTORE_CHAR_SUBCLASSES = "SELECT class_id,exp,sp,level,class_index FROM character_subclasses WHERE char_obj_id=? ORDER BY class_index ASC";
	private static final String ADD_CHAR_SUBCLASS = "INSERT INTO character_subclasses (char_obj_id,class_id,exp,sp,level,class_index) VALUES (?,?,?,?,?,?)";
	private static final String UPDATE_CHAR_SUBCLASS = "UPDATE character_subclasses SET exp=?,sp=?,level=?,class_id=? WHERE char_obj_id=? AND class_index =?";
	private static final String DELETE_CHAR_SUBCLASS = "DELETE FROM character_subclasses WHERE char_obj_id=? AND class_index=?";
	private static final String RESTORE_CHAR_HENNAS = "SELECT slot,symbol_id FROM character_hennas WHERE char_obj_id=? AND class_index=?";
	private static final String ADD_CHAR_HENNA = "INSERT INTO character_hennas (char_obj_id,symbol_id,slot,class_index) VALUES (?,?,?,?)";
	private static final String DELETE_CHAR_HENNA = "DELETE FROM character_hennas WHERE char_obj_id=? AND slot=? AND class_index=?";
	private static final String DELETE_CHAR_HENNAS = "DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=?";
	private static final String DELETE_CHAR_SHORTCUTS = "DELETE FROM character_shortcuts WHERE char_obj_id=? AND class_index=?";
	private static final String RESTORE_CHAR_RECOMS = "SELECT char_id,target_id FROM character_recommends WHERE char_id=?";
	private static final String ADD_CHAR_RECOM = "INSERT INTO character_recommends (char_id,target_id) VALUES (?,?)";
	private static final String DELETE_CHAR_RECOMS = "DELETE FROM character_recommends WHERE char_id=?";
	private static final String INSERT_CHARACTER_RECIPEBOOK = "INSERT INTO character_recipebook (char_id, id, type) VALUES(?,?,?)";
	private static final String DELETE_CHARARACTER_RECIPEBOOK = "DELETE FROM character_recipebook WHERE char_id=? AND id=?";
	private static final String RESTORE_SKILLS_FOR_CHAR = "SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=? AND class_index=?";
	private static final String ADD_NEW_SKILL = "INSERT INTO character_skills (char_obj_id,skill_id,skill_level,skill_name,class_index) VALUES (?,?,?,?,?)";
	private static final String UPDATE_CHARACTER_SKILL_LEVEL = "UPDATE character_skills SET skill_level=? WHERE skill_id=? AND char_obj_id=? AND class_index=?";
	private static final String DELETE_SKILL_FROM_CHAR = "DELETE FROM character_skills WHERE skill_id=? AND char_obj_id=? AND class_index=?";
	private static final String DELETE_CHAR_SKILLS = "DELETE FROM character_skills WHERE char_obj_id=? AND class_index=?";
	private static final String ADD_SKILL_SAVE = "REPLACE INTO character_skills_save (char_obj_id,skill_id,skill_level,effect_count,effect_cur_time,reuse_delay,systime,restore_type,class_index,buff_index) VALUES (?,?,?,?,?,?,?,?,?,?)";
	private static final String RESTORE_SKILL_SAVE = "SELECT skill_id,skill_level,effect_count,effect_cur_time, reuse_delay, systime FROM character_skills_save WHERE char_obj_id=? AND class_index=? AND restore_type=? ORDER BY buff_index ASC";
	private static final String DELETE_SKILL_SAVE = "DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=?";
	
	public static final int REQUEST_TIMEOUT = 15;
	public static final int STORE_PRIVATE_NONE = 0;
	public static final int STORE_PRIVATE_SELL = 1;
	public static final int STORE_PRIVATE_BUY = 3;
	public static final int STORE_PRIVATE_MANUFACTURE = 5;
	public static final int STORE_PRIVATE_PACKAGE_SELL = 8;
	
	private PlayerStatsHolder savedStatus = null;
	private final long _instanceLoginTime;
	protected long _toggleUse = 0;
	public int _activeBoxes = -1;
	public List<String> _activeBoxCharacters = new ArrayList<>();
	private GameClient _client;
	private String _accountName;
	private long _deleteTimer;
	private boolean _isOnline = false;
	private long _onlineTime;
	private long _onlineBeginTime;
	private long _lastAccess;
	private long _uptime;
	protected int _baseClass;
	protected int _activeClass;
	protected int _classIndex = 0;
	private boolean _firstLogin;
	private int _pcBangPoints = 0;
	private final Map<Integer, SubClass> _subClasses = new ConcurrentHashMap<>();
	private PlayerAppearance _appearance;
	private long _expBeforeDeath;
	private int _karma;
	private int _pvpKills;
	private int _pkKills;
	private int _lastKill = 0;
	private int _count = 0;
	private byte _pvpFlag;
	private long _pvpFlagLasts;
	private byte _siegeState = 0;
	private int _curWeightPenalty = 0;
	private int _lastCompassZone; // the last compass zone update send to the client
	private boolean _isIn7sDungeon = false;
	private int _heroConsecutiveKillCount = 0;
	private boolean _isPvpHero = false;
	public int _originalTitleColorAway;
	public String _originalTitleAway;
	private boolean _isAio = false;
	private long _aioEndTime = 0;
	private boolean _expGain = true;
	private boolean _inOlympiadMode = false;
	private boolean _olympiadStart = false;
	private int[] _olympiadPosition;
	private int _olympiadGameId = -1;
	private int _olympiadSide = -1;
	private boolean _isInDuel = false;
	private int _duelState = Duel.DUELSTATE_NODUEL;
	private int _duelId = 0;
	private SystemMessageId _noDuelReason = SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL;
	private Boat _boat;
	private Location _inBoatPosition;
	private int _mountType;
	private int _mountObjectID = 0;
	public int _telemode = 0;
	private int _isSilentMoving = 0;
	private boolean _inCrystallize;
	private boolean _isCrafting;
	private final Map<Integer, RecipeList> _dwarvenRecipeBook = new HashMap<>();
	private final Map<Integer, RecipeList> _commonRecipeBook = new HashMap<>();
	private boolean _waitTypeSitting;
	private boolean _relax;
	private int _obsX;
	private int _obsY;
	private int _obsZ;
	private boolean _observerMode = false;
	private Location _lastClientPosition = new Location(0, 0, 0);
	private Location _lastServerPosition = new Location(0, 0, 0);
	private int _recomHave; // how much I was recommended by others
	private int _recomLeft; // how many recomendations I can give to others
	private long _lastRecomUpdate;
	private final List<Integer> _recomChars = new ArrayList<>();
	private final PlayerInventory _inventory = new PlayerInventory(this);
	private PlayerWarehouse _warehouse;
	private final PlayerFreight _freight = new PlayerFreight(this);
	private int _privatestore;
	private TradeList _activeTradeList;
	private ItemContainer _activeWarehouse;
	private ManufactureList _createList;
	private TradeList _sellList;
	private TradeList _buyList;
	private boolean _newbie;
	private boolean _noble = false;
	private boolean _hero = false;
	private boolean _donator = false;
	private Folk _lastFolkNpc = null;
	private int _questNpcObject = 0;
	private int _partyFind = 0;
	private final SummonRequestHolder _summonRequest = new SummonRequestHolder();
	private final Map<String, QuestState> _quests = new HashMap<>();
	private final ShortCuts _shortCuts = new ShortCuts(this);
	private final MacroList _macroses = new MacroList(this);
	private final List<Player> _snoopListener = new ArrayList<>();
	private final List<Player> _snoopedPlayer = new ArrayList<>();
	private ClassId _skillLearningClassId;
	
	private final Henna[] _henna = new Henna[3];
	private int _hennaSTR;
	private int _hennaINT;
	private int _hennaDEX;
	private int _hennaMEN;
	private int _hennaWIT;
	private int _hennaCON;
	
	private Summon _summon = null;
	private TamedBeast _tamedBeast = null;
	private Radar _radar;
	private int _clanId = 0;
	private Clan _clan;
	private int _apprentice = 0;
	private int _sponsor = 0;
	private long _clanJoinExpiryTime;
	private long _clanCreateExpiryTime;
	private int _powerGrade = 0;
	private int _clanPrivileges = 0;
	private int _pledgeClass = 0;
	private int _pledgeType = 0;
	private int _lvlJoinedAcademy = 0;
	private int _wantsPeace = 0;
	private int _deathPenaltyBuffLevel = 0;
	private AccessLevel _accessLevel;
	private boolean _messageRefusal = false; // message refusal mode
	private boolean _dietMode = false; // ignore weight penalty
	private boolean _exchangeRefusal = false; // Exchange refusal
	private Party _party;
	private Player _activeRequester;
	private long _requestExpireTime = 0;
	private final Request _request = new Request(this);
	private Item _arrowItem;
	private long _protectEndTime = 0;
	private long _teleportProtectEndTime = 0;
	private long _recentFakeDeathEndTime = 0;
	private Weapon _fistsWeaponItem;
	private final Map<Integer, String> _chars = new HashMap<>();
	private int _expertiseIndex; // index in EXPERTISE_LEVELS
	private int _expertisePenalty = 0;
	private int _masteryPenalty = 0;
	private Item _activeEnchantItem = null;
	protected boolean _inventoryDisable = false;
	protected Map<Integer, Cubic> _cubics = new ConcurrentHashMap<>();
	protected Map<Integer, Integer> _activeSoulShots = new ConcurrentHashMap<>();
	public Quest dialog = null;
	private final int[] _loto = new int[5];
	private final int[] _race = new int[2];
	private final BlockList _blockList = new BlockList(this);
	private boolean _isRegisteredOnEvent = false;
	private boolean _isOnSoloEvent = false;
	private boolean _isOnEvent = false;
	private int _team = 0;
	private int _alliedVarkaKetra = 0;
	private int _hasCoupon = 0;
	private Fishing _fishCombat;
	private boolean _fishing = false;
	private int _fishX = 0;
	private int _fishY = 0;
	private int _fishZ = 0;
	private Item _lure = null;
	private ScheduledFuture<?> _taskRentPet;
	private ScheduledFuture<?> _taskWater;
	private final List<String> _validBypass = new ArrayList<>();
	private final List<String> _validBypass2 = new ArrayList<>();
	private final List<String> _validLink = new ArrayList<>();
	private Forum _forumMail;
	private Forum _forumMemo;
	private SkillUseHolder _currentSkill;
	private SkillUseHolder _currentPetSkill;
	private SkillUseHolder _queuedSkill;
	private boolean _isWearingFormalWear = false;
	private Location _currentSkillWorldPosition;
	private int _cursedWeaponEquipedId = 0;
	private int _reviveRequested = 0;
	private double _revivePower = 0;
	private boolean _revivePet = false;
	private double _cpUpdateIncCheck = .0;
	private double _cpUpdateDecCheck = .0;
	private double _cpUpdateInterval = .0;
	private double _mpUpdateIncCheck = .0;
	private double _mpUpdateDecCheck = .0;
	private double _mpUpdateInterval = .0;
	private long _timerToAttack;
	private boolean _isInOfflineMode = false;
	private boolean _isTradeOff = false;
	private long _offlineShopStart = 0;
	public int _originalNameColorOffline = 0xFFFFFF;
	private int _herbstask = 0;
	private boolean _married = false;
	private int _marriedType = 0;
	private int _partnerId = 0;
	private int _coupleId = 0;
	private boolean _engagerequest = false;
	private int _engageid = 0;
	private boolean _marryrequest = false;
	private boolean _marryaccepted = false;
	private boolean _isLocked = false;
	private boolean _isStored = false;
	private int _masteryWeapPenalty = 0;
	private boolean _learningSkill = false;
	private ScheduledFuture<?> _taskWarnUserTakeBreak;
	private boolean _wasInvisible = false;
	private ScheduledFuture<?> _shortBuffTask = null;
	private final List<Integer> _friendList = new ArrayList<>();
	private final List<Integer> _selectedFriendList = new ArrayList<>(); // Related to CB.
	private final List<Integer> _selectedBlocksList = new ArrayList<>(); // Related to CB.
	private int _mailPosition;
	private Fish _fish;
	private final Map<Integer, Timestamp> _reuseTimestamps = new ConcurrentHashMap<>();
	boolean _gmStatus = true; // true by default since this is used by GMs
	public WorldObject _saymode = null;
	private static final int FALLING_VALIDATION_DELAY = 1000;
	private long _fallingTimestamp = 0;
	private volatile int _fallingDamage = 0;
	private Future<?> _fallingDamageTask = null;
	private final Location _lastPartyPosition = new Location(0, 0, 0);
	private PunishLevel _punishLevel = PunishLevel.NONE;
	private long _punishTimer = 0;
	private ScheduledFuture<?> _punishTask;
	private final GatesRequest _gatesRequest = new GatesRequest();
	private final HashMap<Integer, Long> _confirmDlgRequests = new HashMap<>();
	private int _currentMultiSellId = -1;
	private int _partyroom = 0;
	
	/** The table containing all minimum level needed for each Expertise (None, D, C, B, A, S). */
	private static final int[] EXPERTISE_LEVELS =
	{
		SkillTreeTable.getInstance().getExpertiseLevel(0), // NONE
		SkillTreeTable.getInstance().getExpertiseLevel(1), // D
		SkillTreeTable.getInstance().getExpertiseLevel(2), // C
		SkillTreeTable.getInstance().getExpertiseLevel(3), // B
		SkillTreeTable.getInstance().getExpertiseLevel(4), // A
		SkillTreeTable.getInstance().getExpertiseLevel(5), // S
	};
	
	/** The Constant COMMON_CRAFT_LEVELS. */
	private static final int[] COMMON_CRAFT_LEVELS =
	{
		5,
		20,
		28,
		36,
		43,
		49,
		55,
		62
	};
	
	private final Map<String, Object> _scripts = new ConcurrentHashMap<>();
	
	/**
	 * Create a new Player and add it in the characters table of the database.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Create a new Player with an account name</li>
	 * <li>Set the name, the Hair Style, the Hair Color and the Face type of the Player</li>
	 * <li>Add the player in the characters table of the database</li><br>
	 * @param objectId Identifier of the object to initialized
	 * @param template The PlayerTemplate to apply to the Player
	 * @param accountName The name of the Player
	 * @param name The name of the Player
	 * @param hairStyle The hair style Identifier of the Player
	 * @param hairColor The hair color Identifier of the Player
	 * @param face The face type Identifier of the Player
	 * @param sex the sex
	 * @return The Player added to the database or null
	 */
	public static Player create(int objectId, PlayerTemplate template, String accountName, String name, byte hairStyle, byte hairColor, byte face, boolean sex)
	{
		// Create a new Player with an account name
		final PlayerAppearance app = new PlayerAppearance(face, hairColor, hairStyle, sex);
		final Player player = new Player(objectId, template, accountName, app);
		
		// Set the name of the Player
		player.setName(name);
		
		// Set the base class ID to that of the actual class ID.
		player.setBaseClass(player.getClassId());
		
		if (Config.ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE)
		{
			player.setNewbie(true);
		}
		
		// Add the player in the characters table of the database
		final boolean ok = player.createDb();
		if (!ok)
		{
			return null;
		}
		
		return player;
	}
	
	/**
	 * Creates the dummy player.
	 * @param objectId the object id
	 * @param name the name
	 * @return the player instance
	 */
	public static Player createDummyPlayer(int objectId, String name)
	{
		// Create a new Player with an account name
		final Player player = new Player(objectId);
		player.setName(name);
		
		return player;
	}
	
	/**
	 * The Class AIAccessor.
	 */
	public class AIAccessor extends Creature.AIAccessor
	{
		/**
		 * Gets the player.
		 * @return the player
		 */
		public Player getPlayer()
		{
			return Player.this;
		}
		
		/**
		 * Do pickup item.
		 * @param object the object
		 */
		public void doPickupItem(WorldObject object)
		{
			Player.this.doPickupItem(object);
		}
		
		/**
		 * Do interact.
		 * @param target the target
		 */
		public void doInteract(Creature target)
		{
			Player.this.doInteract(target);
		}
		
		@Override
		public void doAttack(Creature target)
		{
			if (isInsidePeaceZone(Player.this, target))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			// Pk protection config
			if (Config.ALLOW_CHAR_KILL_PROTECT && !isGM() && target.isPlayer() && (target.getActingPlayer().getPvpFlag() == 0) && (target.getActingPlayer().getKarma() == 0))
			{
				final int thisLevel = getLevel();
				final int targetLevel = target.getActingPlayer().getLevel();
				final Siege siege = SiegeManager.getInstance().getSiege(Player.this);
				if ((((siege == null) || !siege.isInProgress()) && ((thisLevel >= 20) && (targetLevel < 20))) || ((thisLevel >= 40) && (targetLevel < 40)) || ((thisLevel >= 52) && (targetLevel < 52)) || ((thisLevel >= 61) && (targetLevel < 61)) || ((thisLevel >= 76) && (targetLevel < 76)))
				{
					sendMessage("You can only engage in PvP when your target is within your level grade.");
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
			
			super.doAttack(target);
			
			// cancel the recent fake-death protection instantly if the player attacks or casts spells
			getPlayer().setRecentFakeDeath(false);
			
			for (Cubic cubic : _cubics.values())
			{
				if (cubic.getId() != Cubic.LIFE_CUBIC)
				{
					cubic.doAction();
				}
			}
		}
		
		@Override
		public void doCast(Skill skill)
		{
			// cancel the recent fake-death protection instantly if the player attacks or casts spells
			getPlayer().setRecentFakeDeath(false);
			if (skill == null)
			{
				return;
			}
			
			// Like L2OFF you can use cupid bow skills on peace zone
			// Like L2OFF players can use TARGET_AURA skills on peace zone, all targets will be ignored.
			// Check limited to active target.
			if (skill.isOffensive() && (isInsidePeaceZone(Player.this, getTarget()) && (skill.getTargetType() != SkillTargetType.TARGET_AURA)) && ((skill.getId() != 3260 /* Forgiveness */) && (skill.getId() != 3261 /* Heart Shot */) && (skill.getId() != 3262/* Double Heart Shot */)))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			super.doCast(skill);
			
			if (!skill.isOffensive())
			{
				return;
			}
			
			switch (skill.getTargetType())
			{
				case TARGET_GROUND:
				{
					return;
				}
				default:
				{
					final WorldObject mainTarget = skill.getFirstOfTargetList(Player.this);
					if (!(mainTarget instanceof Creature))
					{
						return;
					}
					
					for (Cubic cubic : _cubics.values())
					{
						if ((cubic != null) && (cubic.getId() != Cubic.LIFE_CUBIC))
						{
							cubic.doAction();
						}
					}
					break;
				}
			}
		}
	}
	
	/**
	 * Gets the actual status.
	 * @return the actual status
	 */
	public PlayerStatsHolder getActualStatus()
	{
		savedStatus = new PlayerStatsHolder(this);
		return savedStatus;
	}
	
	/**
	 * Gets the last saved status.
	 * @return the last saved status
	 */
	public PlayerStatsHolder getLastSavedStatus()
	{
		return savedStatus;
	}
	
	public boolean isSpawnProtected()
	{
		return (_protectEndTime != 0) && (_protectEndTime > GameTimeTaskManager.getGameTicks());
	}
	
	public boolean isTeleportProtected()
	{
		return (_teleportProtectEndTime != 0) && (_teleportProtectEndTime > GameTimeTaskManager.getGameTicks());
	}
	
	/**
	 * Task for Herbs.
	 */
	public class HerbTask implements Runnable
	{
		/** The _process. */
		private final String _process;
		
		/** The _item id. */
		private final int _itemId;
		
		/** The _count. */
		private final int _count;
		
		/** The _reference. */
		private final WorldObject _reference;
		
		/** The _send message. */
		private final boolean _sendMessage;
		
		/**
		 * Instantiates a new herb task.
		 * @param process the process
		 * @param itemId the item id
		 * @param count the count
		 * @param reference the reference
		 * @param sendMessage the send message
		 */
		HerbTask(String process, int itemId, int count, WorldObject reference, boolean sendMessage)
		{
			_process = process;
			_itemId = itemId;
			_count = count;
			_reference = reference;
			_sendMessage = sendMessage;
		}
		
		@Override
		public void run()
		{
			try
			{
				addItem(_process, _itemId, _count, _reference, _sendMessage);
			}
			catch (Throwable t)
			{
				LOGGER.warning(t.getMessage());
			}
		}
	}
	
	/**
	 * Gets the account name.
	 * @return the account name
	 */
	public String getAccountName()
	{
		if (_client != null)
		{
			return _client.getAccountName();
		}
		return _accountName;
	}
	
	/**
	 * Gets the account chars.
	 * @return the account chars
	 */
	public Map<Integer, String> getAccountChars()
	{
		return _chars;
	}
	
	/**
	 * Gets the relation.
	 * @param target the target
	 * @return the relation
	 */
	public int getRelation(Player target)
	{
		int result = 0;
		
		// karma and pvp may not be required
		if (getPvpFlag() != 0)
		{
			result |= RelationChanged.RELATION_PVP_FLAG;
		}
		if (getKarma() > 0)
		{
			result |= RelationChanged.RELATION_HAS_KARMA;
		}
		
		if (isClanLeader())
		{
			result |= RelationChanged.RELATION_LEADER;
		}
		
		if (getSiegeState() != 0)
		{
			result |= RelationChanged.RELATION_INSIEGE;
			if (getSiegeState() != target.getSiegeState())
			{
				result |= RelationChanged.RELATION_ENEMY;
			}
			else
			{
				result |= RelationChanged.RELATION_ALLY;
			}
			if (getSiegeState() == 1)
			{
				result |= RelationChanged.RELATION_ATTACKER;
			}
		}
		
		if ((getClan() != null) && (target.getClan() != null) && (target.getPledgeType() != Clan.SUBUNIT_ACADEMY) && (getPledgeType() != Clan.SUBUNIT_ACADEMY) && target.getClan().isAtWarWith(getClan().getClanId()))
		{
			result |= RelationChanged.RELATION_1SIDED_WAR;
			if (getClan().isAtWarWith(target.getClan().getClanId()))
			{
				result |= RelationChanged.RELATION_MUTUAL_WAR;
			}
		}
		return result;
	}
	
	/**
	 * Retrieve a Player from the characters table of the database and add it in _allObjects of the World (call restore method).<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Retrieve the Player from the characters table of the database</li>
	 * <li>Add the Player object in _allObjects</li>
	 * <li>Set the x,y,z position of the Player and make it invisible</li>
	 * <li>Update the overloaded status of the Player</li><br>
	 * @param objectId Identifier of the object to initialized
	 * @return The Player loaded from the database
	 */
	public static Player load(int objectId)
	{
		return restore(objectId);
	}
	
	/**
	 * Inits the pc status update values.
	 */
	private void initPcStatusUpdateValues()
	{
		_cpUpdateInterval = getMaxCp() / 352.0;
		_cpUpdateIncCheck = getMaxCp();
		_cpUpdateDecCheck = getMaxCp() - _cpUpdateInterval;
		_mpUpdateInterval = getMaxMp() / 352.0;
		_mpUpdateIncCheck = getMaxMp();
		_mpUpdateDecCheck = getMaxMp() - _mpUpdateInterval;
	}
	
	/**
	 * Constructor of Player (use Creature constructor).<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Call the Creature constructor to create an empty _skills slot and copy basic Calculator set to this Player</li>
	 * <li>Set the name of the Player</li><br>
	 * <font color=#FF0000><b><u>Caution</u>: This method SET the level of the Player to 1</b></font>
	 * @param objectId Identifier of the object to initialized
	 * @param template The PlayerTemplate to apply to the Player
	 * @param accountName The name of the account including this Player
	 * @param app the app
	 */
	private Player(int objectId, PlayerTemplate template, String accountName, PlayerAppearance app)
	{
		super(objectId, template);
		getKnownList(); // init knownlist
		getStat(); // init stats
		getStatus(); // init status
		initCharStatusUpdateValues();
		initPcStatusUpdateValues();
		
		_accountName = accountName;
		_appearance = app;
		
		// Create an AI
		_ai = new PlayerAI(new AIAccessor());
		
		// Create a Radar object
		_radar = new Radar(this);
		
		// Retrieve from the database all skills of this Player and add them to _skills
		// Retrieve from the database all items of this Player and add them to _inventory
		getInventory().restore();
		if (!Config.WAREHOUSE_CACHE)
		{
			getWarehouse();
		}
		getFreight().restore();
		
		_instanceLoginTime = Chronos.currentTimeMillis();
	}
	
	/**
	 * Instantiates a new pc instance.
	 * @param objectId the object id
	 */
	private Player(int objectId)
	{
		super(objectId, null);
		getKnownList(); // init knownlist
		getStat(); // init stats
		getStatus(); // init status
		super.initCharStatusUpdateValues();
		initPcStatusUpdateValues();
		
		_instanceLoginTime = Chronos.currentTimeMillis();
	}
	
	@Override
	public PlayerKnownList getKnownList()
	{
		if (!(super.getKnownList() instanceof PlayerKnownList))
		{
			setKnownList(new PlayerKnownList(this));
		}
		return (PlayerKnownList) super.getKnownList();
	}
	
	@Override
	public PlayerStat getStat()
	{
		if (!(super.getStat() instanceof PlayerStat))
		{
			setStat(new PlayerStat(this));
		}
		return (PlayerStat) super.getStat();
	}
	
	@Override
	public PlayerStatus getStatus()
	{
		if (!(super.getStatus() instanceof PlayerStatus))
		{
			setStatus(new PlayerStatus(this));
		}
		return (PlayerStatus) super.getStatus();
	}
	
	/**
	 * Gets the appearance.
	 * @return the appearance
	 */
	public PlayerAppearance getAppearance()
	{
		return _appearance;
	}
	
	/**
	 * Return the base PlayerTemplate link to the Player.
	 * @return the base template
	 */
	public PlayerTemplate getBaseTemplate()
	{
		return PlayerTemplateData.getInstance().getTemplate(_baseClass);
	}
	
	/**
	 * Return the PlayerTemplate link to the Player.
	 * @return the template
	 */
	@Override
	public PlayerTemplate getTemplate()
	{
		return (PlayerTemplate) super.getTemplate();
	}
	
	/**
	 * Sets the template.
	 * @param newclass the new template
	 */
	public void setTemplate(ClassId newclass)
	{
		super.setTemplate(PlayerTemplateData.getInstance().getTemplate(newclass));
	}
	
	public void setTimerToAttack(long time)
	{
		_timerToAttack = time;
	}
	
	public long getTimerToAttack()
	{
		return _timerToAttack;
	}
	
	/**
	 * Return the AI of the Player (create it if necessary).
	 * @return the aI
	 */
	@Override
	public CreatureAI getAI()
	{
		if (_ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
				{
					_ai = new PlayerAI(new AIAccessor());
				}
			}
		}
		return _ai;
	}
	
	/** Return the Level of the Player. */
	@Override
	public int getLevel()
	{
		int level = getStat().getLevel();
		if (level == -1)
		{
			final Player localChar = restore(getObjectId());
			if (localChar != null)
			{
				level = localChar.getLevel();
			}
		}
		
		if (level < 0)
		{
			level = 1;
		}
		
		return level;
	}
	
	/**
	 * Return the _newbie state of the Player.
	 * @return true, if is newbie
	 */
	public boolean isNewbie()
	{
		return _newbie;
	}
	
	/**
	 * Set the _newbie state of the Player.
	 * @param isNewbie The Identifier of the _newbie state<br>
	 */
	public void setNewbie(boolean isNewbie)
	{
		_newbie = isNewbie;
	}
	
	/**
	 * Sets the base class.
	 * @param baseClass the new base class
	 */
	public void setBaseClass(int baseClass)
	{
		_baseClass = baseClass;
	}
	
	/**
	 * Sets the base class.
	 * @param classId the new base class
	 */
	public void setBaseClass(ClassId classId)
	{
		_baseClass = classId.getId();
	}
	
	/**
	 * Checks if is in store mode.
	 * @return true, if is in store mode
	 */
	public boolean isInStoreMode()
	{
		return getPrivateStoreType() > 0;
	}
	
	/**
	 * Checks if is in craft mode.
	 * @return true, if is in craft mode
	 */
	public boolean isCrafting()
	{
		return _isCrafting;
	}
	
	/**
	 * Checks if is in craft mode.
	 * @param isCrafting
	 */
	public void setCrafting(boolean isCrafting)
	{
		_isCrafting = isCrafting;
	}
	
	/** The _kicked. */
	private boolean _kicked = false;
	
	/**
	 * Manage Logout Task.
	 * @param kicked the kicked
	 */
	public void logout(boolean kicked)
	{
		_kicked = kicked;
		closeNetConnection();
	}
	
	/**
	 * Checks if is kicked.
	 * @return true, if is kicked
	 */
	public boolean isKicked()
	{
		return _kicked;
	}
	
	/**
	 * Sets the kicked.
	 * @param value the new kicked
	 */
	public void setKicked(boolean value)
	{
		_kicked = value;
	}
	
	/**
	 * Manage Logout Task.
	 */
	public void logout()
	{
		logout(false);
	}
	
	/**
	 * Return a table containing all Common RecipeList of the Player.
	 * @return the common recipe book
	 */
	public Collection<RecipeList> getCommonRecipeBook()
	{
		return _commonRecipeBook.values();
	}
	
	/**
	 * Return a table containing all Dwarf RecipeList of the Player.
	 * @return the dwarven recipe book
	 */
	public Collection<RecipeList> getDwarvenRecipeBook()
	{
		return _dwarvenRecipeBook.values();
	}
	
	/**
	 * Add a new RecipList to the table _commonrecipebook containing all RecipeList of the Player.
	 * @param recipe The RecipeList to add to the _recipebook
	 */
	public void registerCommonRecipeList(RecipeList recipe)
	{
		_commonRecipeBook.put(recipe.getId(), recipe);
	}
	
	/**
	 * Add a new RecipList to the table _recipebook containing all RecipeList of the Player.
	 * @param recipe The RecipeList to add to the _recipebook
	 */
	public void registerDwarvenRecipeList(RecipeList recipe)
	{
		_dwarvenRecipeBook.put(recipe.getId(), recipe);
	}
	
	/**
	 * Checks for recipe list.
	 * @param recipeId the recipe id
	 * @return <b>TRUE</b> if player has the recipe on Common or Dwarven Recipe book else returns <b>FALSE</b>
	 */
	public boolean hasRecipeList(int recipeId)
	{
		return _dwarvenRecipeBook.containsKey(recipeId) || _commonRecipeBook.containsKey(recipeId);
	}
	
	/**
	 * Tries to remove a RecipList from the table _DwarvenRecipeBook or from table _CommonRecipeBook, those table contain all RecipeList of the Player.
	 * @param recipeId the recipe id
	 */
	public void unregisterRecipeList(int recipeId)
	{
		if (_dwarvenRecipeBook.containsKey(recipeId))
		{
			_dwarvenRecipeBook.remove(recipeId);
		}
		else if (_commonRecipeBook.containsKey(recipeId))
		{
			_commonRecipeBook.remove(recipeId);
		}
		else
		{
			LOGGER.warning("Attempted to remove unknown RecipeList: " + recipeId);
		}
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement deleteRecipe = con.prepareStatement(DELETE_CHARARACTER_RECIPEBOOK))
		{
			deleteRecipe.setInt(1, getObjectId());
			deleteRecipe.setInt(2, recipeId); // 0 = Normal recipe, 1 Dwarven recipe
			deleteRecipe.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.warning("Player.unregisterRecipeList : Could not delete recipe book data " + e);
		}
		
		for (ShortCut sc : getAllShortCuts())
		{
			if ((sc != null) && (sc.getId() == recipeId) && (sc.getType() == ShortCut.TYPE_RECIPE))
			{
				deleteShortCut(sc.getSlot(), sc.getPage());
			}
		}
	}
	
	/**
	 * Returns the Id for the last talked quest NPC.
	 * @return the last quest npc object
	 */
	public int getLastQuestNpcObject()
	{
		return _questNpcObject;
	}
	
	/**
	 * Sets the last quest npc object.
	 * @param npcId the new last quest npc object
	 */
	public void setLastQuestNpcObject(int npcId)
	{
		_questNpcObject = npcId;
	}
	
	/**
	 * Return the QuestState object corresponding to the quest name.
	 * @param quest The name of the quest
	 * @return the quest state
	 */
	public QuestState getQuestState(String quest)
	{
		return _quests.get(quest);
	}
	
	/**
	 * Add a QuestState to the table _quest containing all quests began by the Player.
	 * @param qs The QuestState to add to _quest
	 */
	public void setQuestState(QuestState qs)
	{
		_quests.put(qs.getQuestName(), qs);
	}
	
	/**
	 * Remove a QuestState from the table _quest containing all quests began by the Player.
	 * @param quest The name of the quest
	 */
	public void delQuestState(String quest)
	{
		_quests.remove(quest);
	}
	
	/**
	 * Return a list containing all Quest in progress from the table _quests.
	 * @return the all active quests
	 */
	public List<Quest> getAllActiveQuests()
	{
		final List<Quest> quests = new ArrayList<>();
		for (QuestState qs : _quests.values())
		{
			if (qs != null)
			{
				if (qs.getQuest().getQuestId() >= 1999)
				{
					continue;
				}
				
				if (qs.isCompleted())
				{
					continue;
				}
				
				if (!qs.isStarted())
				{
					continue;
				}
				
				quests.add(qs.getQuest());
			}
		}
		
		return quests;
	}
	
	/**
	 * Return a table containing all QuestState to modify after a Attackable killing.
	 * @param npc the npc
	 * @return the quests for attacks
	 */
	public List<QuestState> getQuestsForAttacks(Npc npc)
	{
		// Create a QuestState lisy that will contain all QuestState to modify
		List<QuestState> states = new ArrayList<>();
		
		// Go through the QuestState of the Player quests
		for (Quest quest : npc.getTemplate().getEventQuests(EventType.ON_ATTACK))
		{
			// Check if the Identifier of the Attackable attck is needed for the current quest
			if (getQuestState(quest.getName()) != null)
			{
				// Copy the current Player QuestState in the QuestState table
				states.add(getQuestState(quest.getName()));
			}
		}
		
		// Return a list containing all QuestState to modify
		return states;
	}
	
	/**
	 * Return a table containing all QuestState to modify after a Attackable killing.
	 * @param npc the npc
	 * @return the quests for kills
	 */
	public List<QuestState> getQuestsForKills(Npc npc)
	{
		// Create a QuestState lisy that will contain all QuestState to modify
		List<QuestState> states = new ArrayList<>();
		
		// Go through the QuestState of the Player quests
		for (Quest quest : npc.getTemplate().getEventQuests(EventType.ON_KILL))
		{
			// Check if the Identifier of the Attackable killed is needed for the current quest
			if (getQuestState(quest.getName()) != null)
			{
				// Copy the current Player QuestState in the QuestState table
				states.add(getQuestState(quest.getName()));
			}
		}
		
		// Return a list containing all QuestState to modify
		return states;
	}
	
	/**
	 * Return a table containing all QuestState from the table _quests in which the Player must talk to the NPC.
	 * @param npcId The Identifier of the NPC
	 * @return the quests for talk
	 */
	public List<QuestState> getQuestsForTalk(int npcId)
	{
		// Create a QuestState list that will contain all QuestState to modify
		List<QuestState> states = new ArrayList<>();
		
		// Go through the QuestState of the Player quests
		for (Quest quest : NpcTable.getInstance().getTemplate(npcId).getEventQuests(EventType.QUEST_TALK))
		{
			// Copy the current Player QuestState in the QuestState table
			if ((quest != null) && (getQuestState(quest.getName()) != null))
			{
				states.add(getQuestState(quest.getName()));
			}
		}
		
		// Return a list containing all QuestState to modify
		return states;
	}
	
	public Collection<QuestState> getAllQuestStates()
	{
		return _quests.values();
	}
	
	/**
	 * Process quest event.
	 * @param quest the quest
	 * @param eventValue the event
	 * @return the quest state
	 */
	public QuestState processQuestEvent(String quest, String eventValue)
	{
		QuestState retval = null;
		String event = eventValue;
		if (event == null)
		{
			event = "";
		}
		if (event.length() == 0)
		{
			return retval;
		}
		
		QuestState qs = getQuestState(quest);
		if (qs == null)
		{
			Quest q = null;
			if (!Config.ALT_DEV_NO_QUESTS)
			{
				q = QuestManager.getInstance().getQuest(quest);
			}
			
			if (q == null)
			{
				return retval;
			}
			qs = q.newQuestState(this);
		}
		
		if ((qs != null) && (_questNpcObject > 0))
		{
			final WorldObject object = World.getInstance().findObject(_questNpcObject);
			if ((object instanceof Npc) && isInsideRadius2D(object, Npc.INTERACTION_DISTANCE))
			{
				final Npc npc = (Npc) object;
				final List<QuestState> states = getQuestsForTalk(npc.getNpcId());
				if (!states.isEmpty())
				{
					for (QuestState state : states)
					{
						if ((state.getQuest().getQuestId() == qs.getQuest().getQuestId()) && !qs.isCompleted())
						{
							if (qs.getQuest().notifyEvent(event, npc, this))
							{
								showQuestWindow(quest, State.getStateName(qs.getState()));
							}
							retval = qs;
						}
					}
					sendPacket(new QuestList(this));
				}
			}
		}
		
		return retval;
	}
	
	private void showQuestWindow(String questId, String stateId)
	{
		final String path = "data/scripts/quests/" + questId + "/" + stateId + ".htm";
		final String content = HtmCache.getInstance().getHtm(path);
		if (content != null)
		{
			final NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
			npcReply.setHtml(content);
			sendPacket(npcReply);
		}
		
		sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	/**
	 * Return a table containing all ShortCut of the Player.
	 * @return the all short cuts
	 */
	public Collection<ShortCut> getAllShortCuts()
	{
		return _shortCuts.getAllShortCuts();
	}
	
	/**
	 * Return the ShortCut of the Player corresponding to the position (page-slot).
	 * @param slot The slot in wich the shortCuts is equiped
	 * @param page The page of shortCuts containing the slot
	 * @return the short cut
	 */
	public ShortCut getShortCut(int slot, int page)
	{
		return _shortCuts.getShortCut(slot, page);
	}
	
	/**
	 * Add a L2shortCut to the Player _shortCuts.
	 * @param shortcut the shortcut
	 */
	public void registerShortCut(ShortCut shortcut)
	{
		_shortCuts.registerShortCut(shortcut);
	}
	
	/**
	 * Delete the ShortCut corresponding to the position (page-slot) from the Player _shortCuts.
	 * @param slot the slot
	 * @param page the page
	 */
	public void deleteShortCut(int slot, int page)
	{
		_shortCuts.deleteShortCut(slot, page);
	}
	
	/**
	 * Add a Macro to the Player _macroses.
	 * @param macro the macro
	 */
	public void registerMacro(Macro macro)
	{
		_macroses.registerMacro(macro);
	}
	
	/**
	 * Delete the Macro corresponding to the Identifier from the Player _macroses.
	 * @param id the id
	 */
	public void deleteMacro(int id)
	{
		_macroses.deleteMacro(id);
	}
	
	/**
	 * Return all Macro of the Player.
	 * @return the macroses
	 */
	public MacroList getMacroses()
	{
		return _macroses;
	}
	
	/**
	 * Set the siege state of the Player.<br>
	 * 1 = attacker, 2 = defender, 0 = not involved
	 * @param siegeState the new siege state
	 */
	public void setSiegeState(byte siegeState)
	{
		_siegeState = siegeState;
	}
	
	/**
	 * Get the siege state of the Player.<br>
	 * 1 = attacker, 2 = defender, 0 = not involved
	 * @return the siege state
	 */
	public byte getSiegeState()
	{
		return _siegeState;
	}
	
	/**
	 * Set the PvP Flag of the Player.
	 * @param pvpFlag the new pvp flag
	 */
	public void setPvpFlag(int pvpFlag)
	{
		_pvpFlag = (byte) pvpFlag;
	}
	
	/**
	 * Gets the pvp flag.
	 * @return the pvp flag
	 */
	public byte getPvpFlag()
	{
		return _pvpFlag;
	}
	
	/**
	 * Sets the pvp flag lasts.
	 * @param time the new pvp flag lasts
	 */
	public void setPvpFlagLasts(long time)
	{
		_pvpFlagLasts = time;
	}
	
	/**
	 * Gets the pvp flag lasts.
	 * @return the pvp flag lasts
	 */
	public long getPvpFlagLasts()
	{
		return _pvpFlagLasts;
	}
	
	/**
	 * Start pvp flag.
	 */
	public void startPvPFlag()
	{
		updatePvPFlag(1);
		PvpFlagTaskManager.getInstance().add(this);
	}
	
	/**
	 * Stop pvp reg task.
	 */
	public void stopPvpRegTask()
	{
		PvpFlagTaskManager.getInstance().remove(this);
	}
	
	/**
	 * Stop pvp flag.
	 */
	public void stopPvPFlag()
	{
		stopPvpRegTask();
		updatePvPFlag(0);
	}
	
	public void updatePvPFlag(int value)
	{
		if (getPvpFlag() == value)
		{
			return;
		}
		setPvpFlag(value);
		
		sendPacket(new UserInfo(this));
		
		// If this player has a pet update the pets pvp flag as well
		if (getPet() != null)
		{
			sendPacket(new RelationChanged(getPet(), getRelation(this), false));
		}
		
		for (Player target : getKnownList().getKnownPlayers().values())
		{
			if (target == null)
			{
				continue;
			}
			
			target.sendPacket(new RelationChanged(this, getRelation(this), isAutoAttackable(target)));
			if (getPet() != null)
			{
				target.sendPacket(new RelationChanged(getPet(), getRelation(this), isAutoAttackable(target)));
			}
		}
	}
	
	@Override
	public void revalidateZone(boolean force)
	{
		// Cannot validate if not in a world region (happens during teleport)
		final WorldRegion region = getWorldRegion();
		if (region == null)
		{
			return;
		}
		
		// This function is called too often from movement code.
		if (!force && (calculateDistanceSq3D(_lastZoneValidateLocation.getX(), _lastZoneValidateLocation.getY(), _lastZoneValidateLocation.getZ()) < 10000))
		{
			return;
		}
		_lastZoneValidateLocation.setXYZ(getX(), getY(), getZ());
		
		region.revalidateZones(this);
		
		if (Config.ALLOW_WATER)
		{
			checkWaterState();
		}
		
		if (isInsideZone(ZoneId.SIEGE))
		{
			if (_lastCompassZone == ExSetCompassZoneCode.SIEGEWARZONE2)
			{
				return;
			}
			_lastCompassZone = ExSetCompassZoneCode.SIEGEWARZONE2;
			sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.SIEGEWARZONE2));
		}
		else if (isInsideZone(ZoneId.PVP))
		{
			if (_lastCompassZone == ExSetCompassZoneCode.PVPZONE)
			{
				return;
			}
			_lastCompassZone = ExSetCompassZoneCode.PVPZONE;
			sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.PVPZONE));
		}
		else if (isIn7sDungeon())
		{
			if (_lastCompassZone == ExSetCompassZoneCode.SEVENSIGNSZONE)
			{
				return;
			}
			_lastCompassZone = ExSetCompassZoneCode.SEVENSIGNSZONE;
			sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.SEVENSIGNSZONE));
		}
		else if (isInsideZone(ZoneId.PEACE))
		{
			if (_lastCompassZone == ExSetCompassZoneCode.PEACEZONE)
			{
				return;
			}
			_lastCompassZone = ExSetCompassZoneCode.PEACEZONE;
			sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.PEACEZONE));
		}
		else
		{
			if (_lastCompassZone == ExSetCompassZoneCode.GENERALZONE)
			{
				return;
			}
			if (_lastCompassZone == ExSetCompassZoneCode.SIEGEWARZONE2)
			{
				updatePvPStatus();
			}
			_lastCompassZone = ExSetCompassZoneCode.GENERALZONE;
			sendPacket(new ExSetCompassZoneCode(ExSetCompassZoneCode.GENERALZONE));
		}
	}
	
	/**
	 * Return True if the Player can Craft Dwarven Recipes.
	 * @return true, if successful
	 */
	public boolean hasDwarvenCraft()
	{
		return getSkillLevel(Skill.SKILL_CREATE_DWARVEN) >= 1;
	}
	
	/**
	 * Gets the dwarven craft.
	 * @return the dwarven craft
	 */
	public int getDwarvenCraft()
	{
		return getSkillLevel(Skill.SKILL_CREATE_DWARVEN);
	}
	
	/**
	 * Return True if the Player can Craft Dwarven Recipes.
	 * @return true, if successful
	 */
	public boolean hasCommonCraft()
	{
		return getSkillLevel(Skill.SKILL_CREATE_COMMON) >= 1;
	}
	
	/**
	 * Gets the common craft.
	 * @return the common craft
	 */
	public int getCommonCraft()
	{
		return getSkillLevel(Skill.SKILL_CREATE_COMMON);
	}
	
	/**
	 * Return the PK counter of the Player.
	 * @return the pk kills
	 */
	public int getPkKills()
	{
		return _pkKills;
	}
	
	/**
	 * Set the PK counter of the Player.
	 * @param pkKills the new pk kills
	 */
	public void setPkKills(int pkKills)
	{
		_pkKills = pkKills;
	}
	
	/**
	 * Return the _deleteTimer of the Player.
	 * @return the delete timer
	 */
	public long getDeleteTimer()
	{
		return _deleteTimer;
	}
	
	/**
	 * Set the _deleteTimer of the Player.
	 * @param deleteTimer the new delete timer
	 */
	public void setDeleteTimer(long deleteTimer)
	{
		_deleteTimer = deleteTimer;
	}
	
	/**
	 * Return the current weight of the Player.
	 * @return the current load
	 */
	public int getCurrentLoad()
	{
		return _inventory.getTotalWeight();
	}
	
	/**
	 * Return date of las update of recomPoints.
	 * @return the last recom update
	 */
	public long getLastRecomUpdate()
	{
		return _lastRecomUpdate;
	}
	
	/**
	 * Sets the last recom update.
	 * @param date the new last recom update
	 */
	public void setLastRecomUpdate(long date)
	{
		_lastRecomUpdate = date;
	}
	
	/**
	 * Return the number of recommandation obtained by the Player.
	 * @return the recom have
	 */
	public int getRecomHave()
	{
		return _recomHave;
	}
	
	/**
	 * Increment the number of recommandation obtained by the Player (Max : 255).
	 */
	protected void incRecomHave()
	{
		if (_recomHave < 255)
		{
			_recomHave++;
		}
	}
	
	/**
	 * Set the number of recommandation obtained by the Player (Max : 255).
	 * @param value the new recom have
	 */
	public void setRecomHave(int value)
	{
		if (value > 255)
		{
			_recomHave = 255;
		}
		else if (value < 0)
		{
			_recomHave = 0;
		}
		else
		{
			_recomHave = value;
		}
	}
	
	/**
	 * Return the number of recommandation that the Player can give.
	 * @return the recom left
	 */
	public int getRecomLeft()
	{
		return _recomLeft;
	}
	
	/**
	 * Increment the number of recommandation that the Player can give.
	 */
	protected void decRecomLeft()
	{
		if (_recomLeft > 0)
		{
			_recomLeft--;
		}
	}
	
	/**
	 * Give recom.
	 * @param target the target
	 */
	public void giveRecom(Player target)
	{
		if (Config.ALT_RECOMMEND)
		{
			try (Connection con = DatabaseFactory.getConnection())
			{
				final PreparedStatement statement = con.prepareStatement(ADD_CHAR_RECOM);
				statement.setInt(1, getObjectId());
				statement.setInt(2, target.getObjectId());
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				LOGGER.warning("could not update char recommendations:" + e);
			}
		}
		target.incRecomHave();
		decRecomLeft();
		_recomChars.add(target.getObjectId());
	}
	
	/**
	 * Can recom.
	 * @param target the target
	 * @return true, if successful
	 */
	public boolean canRecom(Player target)
	{
		return !_recomChars.contains(target.getObjectId());
	}
	
	/**
	 * Set the exp of the Player before a death.
	 * @param exp the new exp before death
	 */
	public void setExpBeforeDeath(long exp)
	{
		_expBeforeDeath = exp;
	}
	
	/**
	 * Gets the exp before death.
	 * @return the exp before death
	 */
	public long getExpBeforeDeath()
	{
		return _expBeforeDeath;
	}
	
	/**
	 * Return the Karma of the Player.
	 * @return the karma
	 */
	public int getKarma()
	{
		return _karma;
	}
	
	/**
	 * Set the Karma of the Player and send a Server->Client packet StatusUpdate (broadcast).
	 * @param value the new karma
	 */
	public void setKarma(int value)
	{
		int karma = value;
		if (karma < 0)
		{
			karma = 0;
		}
		
		if ((_karma == 0) && (karma > 0))
		{
			for (WorldObject object : getKnownList().getKnownObjects().values())
			{
				if (!(object instanceof Guard))
				{
					continue;
				}
				
				if (((Guard) object).getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
				{
					((Guard) object).getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
				}
			}
		}
		else if ((_karma > 0) && (karma == 0))
		{
			// Send a Server->Client StatusUpdate packet with Karma and PvP Flag to the Player and all Player to inform (broadcast)
			setKarmaFlag(0);
		}
		
		_karma = karma;
		broadcastKarma();
	}
	
	/**
	 * Return the max weight that the Player can load.
	 * @return the max load
	 */
	public int getMaxLoad()
	{
		// Weight Limit = (CON Modifier*69000)*Skills
		// Source http://l2p.bravehost.com/weightlimit.html (May 2007)
		
		final int con = getCON();
		if (con < 1)
		{
			return 31000;
		}
		
		if (con > 59)
		{
			return 176000;
		}
		
		final double baseLoad = Math.floor(BaseStat.CON.calcBonus(this) * 69000 * Config.ALT_WEIGHT_LIMIT);
		return (int) calcStat(Stat.MAX_LOAD, baseLoad, this, null);
	}
	
	/**
	 * Gets the expertise penalty.
	 * @return the expertise penalty
	 */
	public int getExpertisePenalty()
	{
		return _expertisePenalty;
	}
	
	/**
	 * Gets the mastery penalty.
	 * @return the mastery penalty
	 */
	public int getMasteryPenalty()
	{
		return _masteryPenalty;
	}
	
	/**
	 * Gets the mastery weap penalty.
	 * @return the mastery weap penalty
	 */
	public int getMasteryWeapPenalty()
	{
		return _masteryWeapPenalty;
	}
	
	/**
	 * Gets the weight penalty.
	 * @return the weight penalty
	 */
	public int getWeightPenalty()
	{
		if (_dietMode)
		{
			return 0;
		}
		return _curWeightPenalty;
	}
	
	/**
	 * Update the overloaded status of the Player.
	 */
	public void refreshOverloaded()
	{
		if (Config.DISABLE_WEIGHT_PENALTY)
		{
			setOverloaded(false);
		}
		else if (_dietMode)
		{
			setOverloaded(false);
			_curWeightPenalty = 0;
			super.removeSkill(getKnownSkill(4270));
			sendPacket(new EtcStatusUpdate(this));
			Broadcast.toKnownPlayers(this, new CharInfo(this, isGM() && getAppearance().isInvisible()));
		}
		else
		{
			final int maxLoad = getMaxLoad();
			if (maxLoad > 0)
			{
				final long weightproc = (long) (((getCurrentLoad() - calcStat(Stat.WEIGHT_PENALTY, 1, this, null)) * 1000) / maxLoad);
				int newWeightPenalty;
				if (weightproc < 500)
				{
					newWeightPenalty = 0;
				}
				else if (weightproc < 666)
				{
					newWeightPenalty = 1;
				}
				else if (weightproc < 800)
				{
					newWeightPenalty = 2;
				}
				else if (weightproc < 1000)
				{
					newWeightPenalty = 3;
				}
				else
				{
					newWeightPenalty = 4;
				}
				
				if (_curWeightPenalty != newWeightPenalty)
				{
					_curWeightPenalty = newWeightPenalty;
					if (newWeightPenalty > 0)
					{
						super.addSkill(SkillTable.getInstance().getSkill(4270, newWeightPenalty));
						sendSkillList(); // Fix visual bug
					}
					else
					{
						super.removeSkill(getKnownSkill(4270));
						sendSkillList(); // Fix visual bug
					}
					
					sendPacket(new EtcStatusUpdate(this));
					Broadcast.toKnownPlayers(this, new CharInfo(this, isGM() && getAppearance().isInvisible()));
				}
			}
		}
		
		sendPacket(new UserInfo(this));
	}
	
	/**
	 * Refresh mastery penality.
	 */
	public void refreshMasteryPenality()
	{
		if (!Config.MASTERY_PENALTY || (getLevel() <= Config.LEVEL_TO_GET_PENALTY))
		{
			return;
		}
		
		boolean heavyMastery = false;
		boolean lightMastery = false;
		boolean robeMastery = false;
		for (Skill skill : getAllSkills())
		{
			if (skill.getName().contains("Heavy Armor Mastery"))
			{
				heavyMastery = true;
			}
			
			if (skill.getName().contains("Light Armor Mastery"))
			{
				lightMastery = true;
			}
			
			if (skill.getName().contains("Robe Mastery"))
			{
				robeMastery = true;
			}
		}
		
		int newMasteryPenalty = 0;
		if (!heavyMastery && !lightMastery && !robeMastery)
		{
			// not completed 1st class transfer or not acquired yet the mastery skills
			newMasteryPenalty = 0;
		}
		else
		{
			for (Item item : getInventory().getItems())
			{
				if ((item != null) && item.isEquipped() && (item.getItem() instanceof Armor))
				{
					// No penality for formal wear
					if (item.getItemId() == 6408)
					{
						continue;
					}
					
					final Armor armorItem = (Armor) item.getItem();
					switch (armorItem.getItemType())
					{
						case HEAVY:
						{
							if (!heavyMastery)
							{
								newMasteryPenalty++;
							}
							break;
						}
						case LIGHT:
						{
							if (!lightMastery)
							{
								newMasteryPenalty++;
							}
							break;
						}
						case MAGIC:
						{
							if (!robeMastery)
							{
								newMasteryPenalty++;
							}
							break;
						}
					}
				}
			}
		}
		
		if (_masteryPenalty != newMasteryPenalty)
		{
			final int penalties = _masteryWeapPenalty + _expertisePenalty + newMasteryPenalty;
			if (penalties > 0)
			{
				super.addSkill(SkillTable.getInstance().getSkill(4267, 1)); // level used to be newPenalty
			}
			else
			{
				super.removeSkill(getKnownSkill(4267));
			}
			
			sendPacket(new EtcStatusUpdate(this));
			_masteryPenalty = newMasteryPenalty;
		}
	}
	
	/**
	 * Can interact.
	 * @param player the player
	 * @return true, if successful
	 */
	protected boolean canInteract(Player player)
	{
		return isInsideRadius2D(player, 50);
	}
	
	/**
	 * Refresh mastery weapon penality.
	 */
	public void refreshMasteryWeapPenality()
	{
		if (!Config.MASTERY_WEAPON_PENALTY || (getLevel() <= Config.LEVEL_TO_GET_WEAPON_PENALTY))
		{
			return;
		}
		
		boolean bluntMastery = false;
		boolean bowMastery = false;
		boolean daggerMastery = false;
		boolean fistMastery = false;
		boolean dualMastery = false;
		boolean poleMastery = false;
		boolean swordMastery = false;
		boolean twoHandMastery = false;
		for (Skill skill : getAllSkills())
		{
			if (skill.getName().contains("Sword Blunt Mastery"))
			{
				swordMastery = true;
				bluntMastery = true;
				continue;
			}
			
			if (skill.getName().contains("Blunt Mastery"))
			{
				bluntMastery = true;
				continue;
			}
			
			if (skill.getName().contains("Bow Mastery"))
			{
				bowMastery = true;
				continue;
			}
			
			if (skill.getName().contains("Dagger Mastery"))
			{
				daggerMastery = true;
				continue;
			}
			
			if (skill.getName().contains("Fist Mastery"))
			{
				fistMastery = true;
				continue;
			}
			
			if (skill.getName().contains("Dual Weapon Mastery"))
			{
				dualMastery = true;
				continue;
			}
			
			if (skill.getName().contains("Polearm Mastery"))
			{
				poleMastery = true;
				continue;
			}
			
			if (skill.getName().contains("Two-handed Weapon Mastery"))
			{
				twoHandMastery = true;
				continue;
			}
		}
		
		int newMasteryPenalty = 0;
		if (!bowMastery && !bluntMastery && !daggerMastery && !fistMastery && !dualMastery && !poleMastery && !swordMastery && !twoHandMastery)
		{
			// not completed 1st class transfer or not acquired yet the mastery skills
			newMasteryPenalty = 0;
		}
		else
		{
			for (Item item : getInventory().getItems())
			{
				if ((item != null) && item.isEquipped() && (item.getItem() instanceof Weapon) && !isCursedWeaponEquiped())
				{
					// No penality for cupid's bow
					if (item.isCupidBow())
					{
						continue;
					}
					
					final Weapon weaponItem = (Weapon) item.getItem();
					
					switch (weaponItem.getItemType())
					{
						case BIGBLUNT:
						case BIGSWORD:
						{
							if (!twoHandMastery)
							{
								newMasteryPenalty++;
							}
							break;
						}
						case BLUNT:
						{
							if (!bluntMastery)
							{
								newMasteryPenalty++;
							}
							break;
						}
						case BOW:
						{
							if (!bowMastery)
							{
								newMasteryPenalty++;
							}
							break;
						}
						case DAGGER:
						{
							if (!daggerMastery)
							{
								newMasteryPenalty++;
							}
							break;
						}
						case DUAL:
						{
							if (!dualMastery)
							{
								newMasteryPenalty++;
							}
							break;
						}
						case DUALFIST:
						case FIST:
						{
							if (!fistMastery)
							{
								newMasteryPenalty++;
							}
							break;
						}
						case POLE:
						{
							if (!poleMastery)
							{
								newMasteryPenalty++;
							}
							break;
						}
						case SWORD:
						{
							if (!swordMastery)
							{
								newMasteryPenalty++;
							}
							break;
						}
					}
				}
			}
		}
		
		if (_masteryWeapPenalty != newMasteryPenalty)
		{
			final int penalties = _masteryPenalty + _expertisePenalty + newMasteryPenalty;
			if (penalties > 0)
			{
				super.addSkill(SkillTable.getInstance().getSkill(4267, 1)); // level used to be newPenalty
			}
			else
			{
				super.removeSkill(getKnownSkill(4267));
			}
			
			sendPacket(new EtcStatusUpdate(this));
			_masteryWeapPenalty = newMasteryPenalty;
		}
	}
	
	/**
	 * Refresh expertise penalty.
	 */
	public void refreshExpertisePenalty()
	{
		if (!Config.EXPERTISE_PENALTY)
		{
			return;
		}
		
		// This code works on principle that first 1-5 levels of penalty is for weapon and 6-10levels are for armor
		int intensityW = 0; // Default value
		int intensityA = 5; // Default value.
		int intensity = 0; // Level of grade penalty.
		for (Item item : getInventory().getItems())
		{
			if ((item != null) && item.isEquipped()) // Checks if items equipped
			{
				final int crystaltype = item.getItem().getCrystalType(); // Gets grade of item
				// Checks if item crystal levels is above character levels and also if last penalty for weapon was lower.
				if ((crystaltype > getExpertiseIndex()) && item.isWeapon() && (crystaltype > intensityW))
				{
					intensityW = crystaltype - getExpertiseIndex();
				}
				// Checks if equipped armor, accesories are above character level and adds each armor penalty.
				if ((crystaltype > getExpertiseIndex()) && !item.isWeapon())
				{
					intensityA += crystaltype - getExpertiseIndex();
				}
			}
		}
		
		if (intensityA == 5)// Means that there isn't armor penalty.
		{
			intensity = intensityW;
		}
		
		else
		{
			intensity = intensityW + intensityA;
		}
		
		// Checks if penalty is above maximum and sets it to maximum.
		if (intensity > 10)
		{
			intensity = 10;
		}
		
		if (getExpertisePenalty() != intensity)
		{
			int penalties = _masteryPenalty + _masteryWeapPenalty + intensity;
			if (penalties > 10) // Checks if penalties are out of bounds for skill level on XML
			{
				penalties = 10;
			}
			
			_expertisePenalty = intensity;
			if (penalties > 0)
			{
				super.addSkill(SkillTable.getInstance().getSkill(4267, intensity));
				sendSkillList();
			}
			else
			{
				super.removeSkill(getKnownSkill(4267));
				sendSkillList();
				_expertisePenalty = 0;
			}
		}
	}
	
	public void checkIfWeaponIsAllowed()
	{
		// Override for Gamemasters
		if (isGM())
		{
			return;
		}
		// Iterate through all effects currently on the character.
		for (Effect currenteffect : getAllEffects())
		{
			final Skill effectSkill = currenteffect.getSkill();
			// Ignore all buff skills that are party related (ie. songs, dances) while still remaining weapon dependant on cast though.
			// Check to rest to assure current effect meets weapon requirements.
			if (!effectSkill.isOffensive() && ((effectSkill.getTargetType() != SkillTargetType.TARGET_PARTY) || (effectSkill.getSkillType() != SkillType.BUFF)) && !effectSkill.getWeaponDependancy(this))
			{
				sendMessage(effectSkill.getName() + " cannot be used with this weapon.");
				currenteffect.exit();
			}
		}
	}
	
	/**
	 * Check ss match.
	 * @param equipped the equipped
	 * @param unequipped the unequipped
	 */
	public void checkSSMatch(Item equipped, Item unequipped)
	{
		if (unequipped == null)
		{
			return;
		}
		
		if ((unequipped.getItem().getType2() == ItemTemplate.TYPE2_WEAPON) && ((equipped == null) || (equipped.getItem().getCrystalType() != unequipped.getItem().getCrystalType())))
		{
			for (Item ss : getInventory().getItems())
			{
				final int itemId = ss.getItemId();
				if ((((itemId >= 2509) && (itemId <= 2514)) || ((itemId >= 3947) && (itemId <= 3952)) || ((itemId <= 1804) && (itemId >= 1808)) || (itemId == 5789) || (itemId == 5790) || (itemId == 1835)) && (ss.getItem().getCrystalType() == unequipped.getItem().getCrystalType()))
				{
					sendPacket(new ExAutoSoulShot(itemId, 0));
					final SystemMessage sm = new SystemMessage(SystemMessageId.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_DEACTIVATED);
					sm.addString(ss.getItemName());
					sendPacket(sm);
				}
			}
		}
	}
	
	/**
	 * Return the the PvP Kills of the Player (Number of player killed during a PvP).
	 * @return the pvp kills
	 */
	public int getPvpKills()
	{
		return _pvpKills;
	}
	
	/**
	 * Set the the PvP Kills of the Player (Number of player killed during a PvP).
	 * @param pvpKills the new pvp kills
	 */
	public void setPvpKills(int pvpKills)
	{
		_pvpKills = pvpKills;
	}
	
	/**
	 * Return the ClassId object of the Player contained in PlayerTemplate.
	 * @return the class id
	 */
	public ClassId getClassId()
	{
		return getTemplate().getClassId();
	}
	
	/**
	 * Set the template of the Player.
	 * @param id The Identifier of the PlayerTemplate to set to the Player
	 */
	public void setClassId(int id)
	{
		if ((getLvlJoinedAcademy() != 0) && (_clan != null) && (ClassId.getClassId(id).level() == 2))
		{
			if (getLvlJoinedAcademy() <= 16)
			{
				_clan.setReputationScore(_clan.getReputationScore() + 400, true);
			}
			else if (getLvlJoinedAcademy() >= 39)
			{
				_clan.setReputationScore(_clan.getReputationScore() + 170, true);
			}
			else
			{
				_clan.setReputationScore((_clan.getReputationScore() + 400) - ((getLvlJoinedAcademy() - 16) * 10), true);
			}
			
			_clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(_clan));
			setLvlJoinedAcademy(0);
			// oust pledge member from the academy, cuz he has finished his 2nd class transfer
			final SystemMessage msg = new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_HAS_BEEN_EXPELLED);
			msg.addString(getName());
			_clan.broadcastToOnlineMembers(msg);
			_clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(getName()));
			_clan.removeClanMember(getName(), 0);
			sendPacket(SystemMessageId.CONGRATULATIONS_YOU_WILL_NOW_GRADUATE_FROM_THE_CLAN_ACADEMY_AND_LEAVE_YOUR_CURRENT_CLAN_AS_A_GRADUATE_OF_THE_ACADEMY_YOU_CAN_IMMEDIATELY_JOIN_A_CLAN_AS_A_REGULAR_MEMBER_WITHOUT_BEING_SUBJECT_TO_ANY_PENALTIES);
			
			// receive graduation gift
			getInventory().addItem("Gift", 8181, 1, this, null); // give academy circlet
			getInventory().updateDatabase(); // update database
		}
		if (isSubClassActive())
		{
			getSubClasses().get(_classIndex).setClassId(id);
		}
		doCast(SkillTable.getInstance().getSkill(5103, 1));
		setClassTemplate(id);
	}
	
	/**
	 * Return the Experience of the Player.
	 * @return the exp
	 */
	public long getExp()
	{
		return getStat().getExp();
	}
	
	/**
	 * Sets the active enchant item.
	 * @param scroll the new active enchant item
	 */
	public void setActiveEnchantItem(Item scroll)
	{
		_activeEnchantItem = scroll;
	}
	
	/**
	 * Gets the active enchant item.
	 * @return the active enchant item
	 */
	public Item getActiveEnchantItem()
	{
		return _activeEnchantItem;
	}
	
	/**
	 * Set the fists weapon of the Player (used when no weapon is equiped).
	 * @param weaponItem The fists Weapon to set to the Player
	 */
	public void setFistsWeaponItem(Weapon weaponItem)
	{
		_fistsWeaponItem = weaponItem;
	}
	
	/**
	 * Return the fists weapon of the Player (used when no weapon is equiped).
	 * @return the fists weapon item
	 */
	public Weapon getFistsWeaponItem()
	{
		return _fistsWeaponItem;
	}
	
	/**
	 * Return the fists weapon of the Player Class (used when no weapon is equiped).
	 * @param classId the class id
	 * @return the weapon
	 */
	public Weapon findFistsWeaponItem(int classId)
	{
		Weapon weaponItem = null;
		if ((classId >= 0x00) && (classId <= 0x09))
		{
			// human fighter fists
			final ItemTemplate temp = ItemTable.getInstance().getTemplate(246);
			weaponItem = (Weapon) temp;
		}
		else if ((classId >= 0x0a) && (classId <= 0x11))
		{
			// human mage fists
			final ItemTemplate temp = ItemTable.getInstance().getTemplate(251);
			weaponItem = (Weapon) temp;
		}
		else if ((classId >= 0x12) && (classId <= 0x18))
		{
			// elven fighter fists
			final ItemTemplate temp = ItemTable.getInstance().getTemplate(244);
			weaponItem = (Weapon) temp;
		}
		else if ((classId >= 0x19) && (classId <= 0x1e))
		{
			// elven mage fists
			final ItemTemplate temp = ItemTable.getInstance().getTemplate(249);
			weaponItem = (Weapon) temp;
		}
		else if ((classId >= 0x1f) && (classId <= 0x25))
		{
			// dark elven fighter fists
			final ItemTemplate temp = ItemTable.getInstance().getTemplate(245);
			weaponItem = (Weapon) temp;
		}
		else if ((classId >= 0x26) && (classId <= 0x2b))
		{
			// dark elven mage fists
			final ItemTemplate temp = ItemTable.getInstance().getTemplate(250);
			weaponItem = (Weapon) temp;
		}
		else if ((classId >= 0x2c) && (classId <= 0x30))
		{
			// orc fighter fists
			final ItemTemplate temp = ItemTable.getInstance().getTemplate(248);
			weaponItem = (Weapon) temp;
		}
		else if ((classId >= 0x31) && (classId <= 0x34))
		{
			// orc mage fists
			final ItemTemplate temp = ItemTable.getInstance().getTemplate(252);
			weaponItem = (Weapon) temp;
		}
		else if ((classId >= 0x35) && (classId <= 0x39))
		{
			// dwarven fists
			final ItemTemplate temp = ItemTable.getInstance().getTemplate(247);
			weaponItem = (Weapon) temp;
		}
		return weaponItem;
	}
	
	/**
	 * Give Expertise skill of this level and remove beginner Lucky skill.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Get the Level of the Player</li>
	 * <li>If Player Level is 5, remove beginner Lucky skill</li>
	 * <li>Add the Expertise skill corresponding to its Expertise level</li>
	 * <li>Update the overloaded status of the Player</li><br>
	 * <font color=#FF0000><b><u>Caution</u>: This method DOESN'T give other free skills (SP needed = 0)</b></font>
	 */
	public synchronized void rewardSkills()
	{
		rewardSkills(false);
	}
	
	public synchronized void rewardSkills(boolean restore)
	{
		// Get the Level of the Player
		final int lvl = getLevel();
		
		// Remove beginner Lucky skill
		if (lvl == 10)
		{
			removeSkill(SkillTable.getInstance().getSkill(194, 1));
		}
		
		// Calculate the current higher Expertise of the Player
		for (int i = 0; i < EXPERTISE_LEVELS.length; i++)
		{
			if (lvl >= EXPERTISE_LEVELS[i])
			{
				setExpertiseIndex(i);
			}
		}
		
		// Add the Expertise skill corresponding to its Expertise level
		if (getExpertiseIndex() > 0)
		{
			final Skill skill = SkillTable.getInstance().getSkill(239, getExpertiseIndex());
			addSkill(skill, !restore);
		}
		
		// Active skill dwarven craft
		if ((getSkillLevel(1321) < 1) && (getRace() == Race.DWARF))
		{
			final Skill skill = SkillTable.getInstance().getSkill(1321, 1);
			addSkill(skill, !restore);
		}
		
		// Active skill common craft
		if (getSkillLevel(1322) < 1)
		{
			final Skill skill = SkillTable.getInstance().getSkill(1322, 1);
			addSkill(skill, !restore);
		}
		
		for (int i = 0; i < COMMON_CRAFT_LEVELS.length; i++)
		{
			if ((lvl >= COMMON_CRAFT_LEVELS[i]) && (getSkillLevel(1320) < (i + 1)))
			{
				final Skill skill = SkillTable.getInstance().getSkill(1320, (i + 1));
				addSkill(skill, !restore);
			}
		}
		
		// Auto-Learn skills if activated
		if (Config.AUTO_LEARN_SKILLS)
		{
			giveAvailableSkills();
		}
		sendSkillList();
		
		if ((_clan != null) && (_clan.getLevel() > 3) && isClanLeader())
		{
			SiegeManager.getInstance().addSiegeSkills(this);
		}
		
		// This function gets called on login, so not such a bad place to check weight
		refreshOverloaded(); // Update the overloaded status of the Player
		refreshExpertisePenalty(); // Update the expertise status of the Player
		refreshMasteryPenality();
		
		refreshMasteryWeapPenality();
	}
	
	/**
	 * Regive all skills which aren't saved to database, like Noble, Hero, Clan Skills.
	 */
	private synchronized void regiveTemporarySkills()
	{
		// Do not call this on enterworld or char load
		
		// Add noble skills if noble
		if (isNoble())
		{
			setNoble(true);
		}
		
		// Add Hero skills if hero
		if (isHero())
		{
			setHero(true);
		}
		
		// Add clan skills
		if ((getClan() != null) && (getClan().getReputationScore() >= 0))
		{
			for (Skill sk : getClan().getAllSkills())
			{
				if (sk.getMinPledgeClass() <= getPledgeClass())
				{
					addSkill(sk, false);
				}
			}
		}
		
		// Reload passive skills from armors / jewels / weapons
		getInventory().reloadEquippedItems();
	}
	
	/**
	 * Give all available skills to the player.
	 */
	public void giveAvailableSkills()
	{
		int skillCounter = 0;
		
		final Collection<Skill> skills = SkillTreeTable.getInstance().getAllAvailableSkills(this, getClassId());
		for (Skill skill : skills)
		{
			if (getSkillLevel(skill.getId()) == 0)
			{
				skillCounter++;
			}
			
			// Penality skill are not auto learn
			if ((skill.getId() == 4267 /* Grade Penalty */) || (skill.getId() == 4270 /* Weight Penalty */))
			{
				continue;
			}
			
			// fix when learning toggle skills
			if (skill.isToggle())
			{
				final Effect toggleEffect = getFirstEffect(skill.getId());
				if (toggleEffect != null)
				{
					// stop old toggle skill effect, and give new toggle skill effect back
					toggleEffect.exit(false);
					skill.applyEffects(this, this, false, false, false);
				}
			}
			
			addSkill(skill, true);
			
			if (Config.AUTO_LEARN_SKILLS)
			{
				for (ShortCut shortcut : getAllShortCuts())
				{
					if ((shortcut != null) && (shortcut.getId() == skill.getId()) && (shortcut.getType() == ShortCut.TYPE_SKILL))
					{
						final ShortCut newsc = new ShortCut(shortcut.getSlot(), shortcut.getPage(), shortcut.getType(), shortcut.getId(), shortcut.getLevel());
						sendPacket(new ShortCutRegister(newsc));
						registerShortCut(newsc);
					}
				}
				sendPacket(new ShortCutInit(this));
			}
		}
		
		sendMessage("You have learned " + skillCounter + " new skills.");
	}
	
	/**
	 * Set the Experience value of the Player.
	 * @param exp the new exp
	 */
	public void setExp(long exp)
	{
		getStat().setExp(exp);
	}
	
	/**
	 * Return the Race object of the Player.
	 * @return the race
	 */
	public Race getRace()
	{
		if (!isSubClassActive())
		{
			return getTemplate().getRace();
		}
		return PlayerTemplateData.getInstance().getTemplate(_baseClass).getRace();
	}
	
	/**
	 * Gets the radar.
	 * @return the radar
	 */
	public Radar getRadar()
	{
		return _radar;
	}
	
	/**
	 * Return the SP amount of the Player.
	 * @return the sp
	 */
	public int getSp()
	{
		return getStat().getSp();
	}
	
	/**
	 * Set the SP amount of the Player.
	 * @param sp the new sp
	 */
	public void setSp(int sp)
	{
		super.getStat().setSp(sp);
	}
	
	/**
	 * Return true if this Player is a clan leader in ownership of the passed castle.
	 * @param castleId the castle id
	 * @return true, if is castle lord
	 */
	public boolean isCastleLord(int castleId)
	{
		final Clan clan = getClan();
		
		// player has clan and is the clan leader, check the castle info
		if ((clan != null) && (clan.getLeader().getPlayer() == this))
		{
			// if the clan has a castle and it is actually the queried castle, return true
			final Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
			if ((castle != null) && (castle == CastleManager.getInstance().getCastleById(castleId)))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Return the Clan Identifier of the Player.
	 * @return the clan id
	 */
	public int getClanId()
	{
		return _clanId;
	}
	
	/**
	 * Return the Clan Crest Identifier of the Player or 0.
	 * @return the clan crest id
	 */
	public int getClanCrestId()
	{
		if (_clan != null)
		{
			return _clan.getCrestId();
		}
		return 0;
	}
	
	/**
	 * Gets the clan crest large id.
	 * @return The Clan CrestLarge Identifier or 0
	 */
	public int getClanCrestLargeId()
	{
		if (_clan != null)
		{
			return _clan.getCrestLargeId();
		}
		return 0;
	}
	
	/**
	 * Gets the clan join expiry time.
	 * @return the clan join expiry time
	 */
	public long getClanJoinExpiryTime()
	{
		return _clanJoinExpiryTime;
	}
	
	/**
	 * Sets the clan join expiry time.
	 * @param time the new clan join expiry time
	 */
	public void setClanJoinExpiryTime(long time)
	{
		_clanJoinExpiryTime = time;
	}
	
	/**
	 * Gets the clan create expiry time.
	 * @return the clan create expiry time
	 */
	public long getClanCreateExpiryTime()
	{
		return _clanCreateExpiryTime;
	}
	
	/**
	 * Sets the clan create expiry time.
	 * @param time the new clan create expiry time
	 */
	public void setClanCreateExpiryTime(long time)
	{
		_clanCreateExpiryTime = time;
	}
	
	/**
	 * Sets the online time.
	 * @param time the new online time
	 */
	public void setOnlineTime(long time)
	{
		_onlineTime = time;
		_onlineBeginTime = Chronos.currentTimeMillis();
	}
	
	public long getOnlineTime()
	{
		return _onlineTime;
	}
	
	/**
	 * Return the PcInventory Inventory of the Player contained in _inventory.
	 * @return the inventory
	 */
	public PlayerInventory getInventory()
	{
		return _inventory;
	}
	
	/**
	 * Delete a ShortCut of the Player _shortCuts.
	 * @param objectId the object id
	 */
	public void removeItemFromShortCut(int objectId)
	{
		_shortCuts.deleteShortCutByObjectId(objectId);
	}
	
	/**
	 * Return True if the Player is sitting.
	 * @return true, if is sitting
	 */
	public boolean isSitting()
	{
		return _waitTypeSitting;
	}
	
	/**
	 * Set _waitTypeSitting to given value.
	 * @param value the new checks if is sitting
	 */
	public void setSitting(boolean value)
	{
		_waitTypeSitting = value;
	}
	
	/**
	 * Sit down the Player, set the AI Intention to AI_INTENTION_REST and send a Server->Client ChangeWaitType packet (broadcast)
	 */
	public void sitDown()
	{
		if (isFakeDeath())
		{
			stopFakeDeath(null);
		}
		
		if (isCastingNow() && !_relax)
		{
			return;
		}
		
		if (!_waitTypeSitting && !isAttackDisabled() && !isOutOfControl() && !isImmobilized())
		{
			breakAttack();
			setSitting(true);
			broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_SITTING));
			// Schedule a sit down task to wait for the animation to finish
			ThreadPool.schedule(new SitDownTask(this), 2500);
			setParalyzed(true);
		}
	}
	
	class SitDownTask implements Runnable
	{
		Player _player;
		
		SitDownTask(Player player)
		{
			_player = player;
		}
		
		@Override
		public void run()
		{
			setSitting(true);
			_player.setParalyzed(false);
			_player.getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
		}
	}
	
	class StandUpTask implements Runnable
	{
		Player _player;
		
		StandUpTask(Player player)
		{
			_player = player;
		}
		
		@Override
		public void run()
		{
			_player.setSitting(false);
			_player.setImmobilized(false);
			_player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
	}
	
	/**
	 * Stand up the Player, set the AI Intention to AI_INTENTION_IDLE and send a Server->Client ChangeWaitType packet (broadcast).
	 */
	public void standUp()
	{
		if (isFakeDeath())
		{
			broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_STANDING));
			// Schedule a stand up task to wait for the animation to finish
			setImmobilized(true);
			ThreadPool.schedule(new StandUpTask(this), 2000);
			stopFakeDeath(null);
		}
		
		if (_waitTypeSitting && !isInStoreMode() && !isAlikeDead())
		{
			if (_relax)
			{
				setRelax(false);
				stopEffects(Effect.EffectType.RELAXING);
			}
			
			broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_STANDING));
			// Schedule a stand up task to wait for the animation to finish
			setImmobilized(true);
			ThreadPool.schedule(new StandUpTask(this), 2500);
		}
	}
	
	/**
	 * Set the value of the _relax value. Must be True if using skill Relax and False if not.
	 * @param value the new relax
	 */
	public void setRelax(boolean value)
	{
		_relax = value;
	}
	
	/**
	 * Return the PcWarehouse object of the Player.
	 * @return the warehouse
	 */
	public PlayerWarehouse getWarehouse()
	{
		if (_warehouse == null)
		{
			_warehouse = new PlayerWarehouse(this);
			_warehouse.restore();
		}
		if (Config.WAREHOUSE_CACHE)
		{
			WarehouseCacheManager.getInstance().addCacheTask(this);
		}
		return _warehouse;
	}
	
	/**
	 * Free memory used by Warehouse.
	 */
	public void clearWarehouse()
	{
		if (_warehouse != null)
		{
			_warehouse.deleteMe();
		}
		_warehouse = null;
	}
	
	/**
	 * Return the PcFreight object of the Player.
	 * @return the freight
	 */
	public PlayerFreight getFreight()
	{
		return _freight;
	}
	
	/**
	 * Return the Adena amount of the Player.
	 * @return the adena
	 */
	public int getAdena()
	{
		return _inventory.getAdena();
	}
	
	/**
	 * Return the Item amount of the Player.
	 * @param itemId the item id
	 * @param enchantLevel the enchant level
	 * @return the item count
	 */
	public int getItemCount(int itemId, int enchantLevel)
	{
		return _inventory.getInventoryItemCount(itemId, enchantLevel);
	}
	
	/**
	 * Return the Ancient Adena amount of the Player.
	 * @return the ancient adena
	 */
	public int getAncientAdena()
	{
		return _inventory.getAncientAdena();
	}
	
	/**
	 * Add adena to Inventory of the Player and send a Server->Client InventoryUpdate packet to the Player.
	 * @param process : String Identifier of process triggering this action
	 * @param amount : int Quantity of adena to be added
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 */
	public void addAdena(String process, int amount, WorldObject reference, boolean sendMessage)
	{
		int count = amount;
		if (count > 0)
		{
			if (_inventory.getAdena() == Integer.MAX_VALUE)
			{
				return;
			}
			else if (_inventory.getAdena() >= (Integer.MAX_VALUE - count))
			{
				count = Integer.MAX_VALUE - _inventory.getAdena();
				_inventory.addAdena(process, count, this, reference);
			}
			else if (_inventory.getAdena() < (Integer.MAX_VALUE - count))
			{
				_inventory.addAdena(process, count, this, reference);
			}
			if (sendMessage)
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S1_ADENA);
				sm.addNumber(count);
				sendPacket(sm);
			}
			
			// Send update packet
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				final InventoryUpdate iu = new InventoryUpdate();
				iu.addItem(_inventory.getAdenaInstance());
				sendPacket(iu);
			}
			else
			{
				sendPacket(new ItemList(this, false));
			}
		}
	}
	
	/**
	 * Reduce adena in Inventory of the Player and send a Server->Client InventoryUpdate packet to the Player.
	 * @param process : String Identifier of process triggering this action
	 * @param count : int Quantity of adena to be reduced
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successful
	 */
	public boolean reduceAdena(String process, int count, WorldObject reference, boolean sendMessage)
	{
		if (count > getAdena())
		{
			if (sendMessage)
			{
				sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			}
			return false;
		}
		
		if (count > 0)
		{
			final Item adenaItem = _inventory.getAdenaInstance();
			_inventory.reduceAdena(process, count, this, reference);
			
			// Send update packet
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				final InventoryUpdate iu = new InventoryUpdate();
				iu.addItem(adenaItem);
				sendPacket(iu);
			}
			else
			{
				sendPacket(new ItemList(this, false));
			}
			
			if (sendMessage)
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_ADENA_DISAPPEARED);
				sm.addNumber(count);
				sendPacket(sm);
			}
		}
		
		return true;
	}
	
	/**
	 * Add ancient adena to Inventory of the Player and send a Server->Client InventoryUpdate packet to the Player.
	 * @param process : String Identifier of process triggering this action
	 * @param count : int Quantity of ancient adena to be added
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 */
	public void addAncientAdena(String process, int count, WorldObject reference, boolean sendMessage)
	{
		if (sendMessage)
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S2_S1_S);
			sm.addItemName(PlayerInventory.ANCIENT_ADENA_ID);
			sm.addNumber(count);
			sendPacket(sm);
		}
		
		if (count > 0)
		{
			_inventory.addAncientAdena(process, count, this, reference);
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				final InventoryUpdate iu = new InventoryUpdate();
				iu.addItem(_inventory.getAncientAdenaInstance());
				sendPacket(iu);
			}
			else
			{
				sendPacket(new ItemList(this, false));
			}
		}
	}
	
	/**
	 * Reduce ancient adena in Inventory of the Player and send a Server->Client InventoryUpdate packet to the Player.
	 * @param process : String Identifier of process triggering this action
	 * @param count : int Quantity of ancient adena to be reduced
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successful
	 */
	public boolean reduceAncientAdena(String process, int count, WorldObject reference, boolean sendMessage)
	{
		if (count > getAncientAdena())
		{
			if (sendMessage)
			{
				sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			}
			return false;
		}
		
		if (count > 0)
		{
			final Item ancientAdenaItem = _inventory.getAncientAdenaInstance();
			_inventory.reduceAncientAdena(process, count, this, reference);
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				final InventoryUpdate iu = new InventoryUpdate();
				iu.addItem(ancientAdenaItem);
				sendPacket(iu);
			}
			else
			{
				sendPacket(new ItemList(this, false));
			}
			
			if (sendMessage)
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.S2_S1_HAS_DISAPPEARED);
				sm.addNumber(count);
				sm.addItemName(PlayerInventory.ANCIENT_ADENA_ID);
				sendPacket(sm);
			}
		}
		
		return true;
	}
	
	/**
	 * Adds item to inventory and send a Server->Client InventoryUpdate packet to the Player.
	 * @param process : String Identifier of process triggering this action
	 * @param item : Item to be added
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 */
	public void addItem(String process, Item item, WorldObject reference, boolean sendMessage)
	{
		if (item.getCount() > 0)
		{
			// Sends message to client if requested
			if (sendMessage)
			{
				if (item.getCount() > 1)
				{
					if (item.isStackable() && !Config.MULTIPLE_ITEM_DROP)
					{
						final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1);
						sm.addItemName(item.getItemId());
						sendPacket(sm);
					}
					else
					{
						final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S2_S1);
						sm.addItemName(item.getItemId());
						sm.addNumber(item.getCount());
						sendPacket(sm);
					}
				}
				else if (item.getEnchantLevel() > 0)
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_A_S1_S2);
					sm.addNumber(item.getEnchantLevel());
					sm.addItemName(item.getItemId());
					sendPacket(sm);
				}
				else
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1);
					sm.addItemName(item.getItemId());
					sendPacket(sm);
				}
			}
			
			// Add the item to inventory
			final Item newitem = _inventory.addItem(process, item, this, reference);
			
			// Send inventory update packet
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				final InventoryUpdate playerIU = new InventoryUpdate();
				playerIU.addItem(newitem);
				sendPacket(playerIU);
			}
			else
			{
				sendPacket(new ItemList(this, false));
			}
			
			// Update current load as well
			final StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
			sendPacket(su);
			
			// If over capacity, Drop the item
			if (!isGM() && !_inventory.validateCapacity(0))
			{
				dropItem("InvDrop", newitem, null, true, true);
			}
			else if (CursedWeaponsManager.getInstance().isCursed(newitem.getItemId()))
			{
				CursedWeaponsManager.getInstance().activate(this, newitem);
			}
		}
		
		// If you pickup arrows.
		if (item.getItem().getItemType() == EtcItemType.ARROW)
		{
			// If a bow is equipped, try to equip them if no arrows is currently equipped.
			final Weapon currentWeapon = getActiveWeaponItem();
			if ((currentWeapon != null) && (currentWeapon.getItemType() == WeaponType.BOW) && (getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) == null))
			{
				checkAndEquipArrows();
			}
		}
	}
	
	/**
	 * Adds item to Inventory and send a Server->Client InventoryUpdate packet to the Player.
	 * @param process : String Identifier of process triggering this action
	 * @param itemId : int Item Identifier of the item to be added
	 * @param count : int Quantity of items to be added
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 */
	public void addItem(String process, int itemId, int count, WorldObject reference, boolean sendMessage)
	{
		if (count > 0)
		{
			// Sends message to client if requested
			if (sendMessage && ((!isCastingNow() && (ItemTable.getInstance().createDummyItem(itemId).getItemType() == EtcItemType.HERB)) || (ItemTable.getInstance().createDummyItem(itemId).getItemType() != EtcItemType.HERB)))
			{
				if (count > 1)
				{
					if (process.equalsIgnoreCase("sweep") || process.equalsIgnoreCase("Quest"))
					{
						final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S2_S1_S);
						sm.addItemName(itemId);
						sm.addNumber(count);
						sendPacket(sm);
					}
					else
					{
						final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S2_S1);
						sm.addItemName(itemId);
						sm.addNumber(count);
						sendPacket(sm);
					}
				}
				else if (process.equalsIgnoreCase("sweep") || process.equalsIgnoreCase("Quest"))
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S1);
					sm.addItemName(itemId);
					sendPacket(sm);
				}
				else
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1);
					sm.addItemName(itemId);
					sendPacket(sm);
				}
			}
			// Auto use herbs - autoloot
			if (ItemTable.getInstance().createDummyItem(itemId).getItemType() == EtcItemType.HERB) // If item is herb dont add it to iv :]
			{
				if (!isCastingNow() && !isCastingPotionNow())
				{
					final Item herb = new Item(getObjectId(), itemId);
					final IItemHandler handler = ItemHandler.getInstance().getItemHandler(herb.getItemId());
					if (handler == null)
					{
						LOGGER.warning("No item handler registered for Herb - item ID " + herb.getItemId() + ".");
					}
					else
					{
						handler.useItem(this, herb);
						if (_herbstask >= 100)
						{
							_herbstask -= 100;
						}
					}
				}
				else
				{
					_herbstask += 100;
					ThreadPool.schedule(new HerbTask(process, itemId, count, reference, sendMessage), _herbstask);
				}
			}
			else
			{
				// Add the item to inventory
				final Item item = _inventory.addItem(process, itemId, count, this, reference);
				
				// Send inventory update packet
				if (!Config.FORCE_INVENTORY_UPDATE)
				{
					final InventoryUpdate playerIU = new InventoryUpdate();
					playerIU.addItem(item);
					sendPacket(playerIU);
				}
				else
				{
					sendPacket(new ItemList(this, false));
				}
				
				// Update current load as well
				final StatusUpdate su = new StatusUpdate(getObjectId());
				su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
				sendPacket(su);
				
				// If over capacity, drop the item
				if (!isGM() && !_inventory.validateCapacity(item))
				{
					dropItem("InvDrop", item, null, true, true);
				}
				else if (CursedWeaponsManager.getInstance().isCursed(item.getItemId()))
				{
					CursedWeaponsManager.getInstance().activate(this, item);
				}
			}
		}
	}
	
	/**
	 * Destroy item from inventory and send a Server->Client InventoryUpdate packet to the Player.
	 * @param process : String Identifier of process triggering this action
	 * @param item : Item to be destroyed
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successful
	 */
	public boolean destroyItem(String process, Item item, WorldObject reference, boolean sendMessage)
	{
		final Item destoyedItem = _inventory.destroyItem(process, item, this, reference);
		if (destoyedItem == null)
		{
			if (sendMessage)
			{
				sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
			}
			return false;
		}
		
		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			final InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(destoyedItem);
			sendPacket(playerIU);
		}
		else
		{
			sendPacket(new ItemList(this, false));
		}
		
		// Update current load as well
		final StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		
		// Sends message to client if requested
		if (sendMessage)
		{
			if (_count > 1)
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.S2_S1_HAS_DISAPPEARED);
				sm.addItemName(destoyedItem.getItemId());
				sm.addNumber(_count);
				sendPacket(sm);
			}
			else
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_DISAPPEARED);
				sm.addItemName(destoyedItem.getItemId());
				sendPacket(sm);
			}
		}
		
		return true;
	}
	
	/**
	 * Destroys item from inventory and send a Server->Client InventoryUpdate packet to the Player.
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : int Item Instance identifier of the item to be destroyed
	 * @param count : int Quantity of items to be destroyed
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successful
	 */
	@Override
	public boolean destroyItem(String process, int objectId, int count, WorldObject reference, boolean sendMessage)
	{
		final Item item = _inventory.getItemByObjectId(objectId);
		if ((item == null) || (item.getCount() < count) || (_inventory.destroyItem(process, objectId, count, this, reference) == null))
		{
			if (sendMessage)
			{
				sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
			}
			return false;
		}
		
		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			final InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(item);
			sendPacket(playerIU);
		}
		else
		{
			sendPacket(new ItemList(this, false));
		}
		
		// Update current load as well
		final StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		
		// Sends message to client if requested
		if (sendMessage)
		{
			if (count > 1)
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.S2_S1_HAS_DISAPPEARED);
				sm.addItemName(item.getItemId());
				sm.addNumber(count);
				sendPacket(sm);
			}
			else
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_DISAPPEARED);
				sm.addItemName(item.getItemId());
				sendPacket(sm);
			}
		}
		
		return true;
	}
	
	/**
	 * Destroys shots from inventory without logging and only occasional saving to database. Sends a Server->Client InventoryUpdate packet to the Player.
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : int Item Instance identifier of the item to be destroyed
	 * @param count : int Quantity of items to be destroyed
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successful
	 */
	public boolean destroyItemWithoutTrace(String process, int objectId, int count, WorldObject reference, boolean sendMessage)
	{
		final Item item = _inventory.getItemByObjectId(objectId);
		if ((item == null) || (item.getCount() < count))
		{
			if (sendMessage)
			{
				sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
			}
			return false;
		}
		
		// Adjust item quantity
		if (item.getCount() > count)
		{
			synchronized (item)
			{
				item.changeCountWithoutTrace(process, -count, this, reference);
				item.setLastChange(Item.MODIFIED);
				_inventory.refreshWeight();
			}
		}
		else
		{
			// Destroy entire item and save to database
			_inventory.destroyItem(process, item, this, reference);
		}
		
		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			final InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(item);
			sendPacket(playerIU);
		}
		else
		{
			sendPacket(new ItemList(this, false));
		}
		
		// Update current load as well
		final StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		
		// Sends message to client if requested
		if (sendMessage)
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_DISAPPEARED);
			sm.addNumber(count);
			sm.addItemName(item.getItemId());
			sendPacket(sm);
		}
		
		return true;
	}
	
	/**
	 * Destroy item from inventory by using its <b>itemId</b> and send a Server->Client InventoryUpdate packet to the Player.
	 * @param process : String Identifier of process triggering this action
	 * @param itemId : int Item identifier of the item to be destroyed
	 * @param count : int Quantity of items to be destroyed
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successful
	 */
	@Override
	public boolean destroyItemByItemId(String process, int itemId, int count, WorldObject reference, boolean sendMessage)
	{
		final Item item = _inventory.getItemByItemId(itemId);
		if ((item == null) || (item.getCount() < count) || (_inventory.destroyItemByItemId(process, itemId, count, this, reference) == null))
		{
			if (sendMessage)
			{
				sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
			}
			return false;
		}
		
		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			final InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(item);
			sendPacket(playerIU);
		}
		else
		{
			sendPacket(new ItemList(this, false));
		}
		
		// Update current load as well
		final StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		
		// Sends message to client if requested
		if (sendMessage)
		{
			if (count > 1)
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.S2_S1_HAS_DISAPPEARED);
				sm.addItemName(item.getItemId());
				sm.addNumber(count);
				sendPacket(sm);
			}
			else
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_DISAPPEARED);
				sm.addItemName(item.getItemId());
				sendPacket(sm);
			}
		}
		
		return true;
	}
	
	/**
	 * Destroy all weared items from inventory and send a Server->Client InventoryUpdate packet to the Player.
	 * @param process : String Identifier of process triggering this action
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 */
	public void destroyWearedItems(String process, WorldObject reference, boolean sendMessage)
	{
		// Go through all Items of the inventory
		for (Item item : getInventory().getItems())
		{
			// Check if the item is a Try On item in order to remove it
			if (item.isWear())
			{
				if (item.isEquipped())
				{
					getInventory().unEquipItemInSlotAndRecord(item.getEquipSlot());
				}
				
				if (_inventory.destroyItem(process, item, this, reference) == null)
				{
					LOGGER.warning("Player " + getName() + " can't destroy weared item: " + item.getName() + "[ " + item.getObjectId() + " ]");
					continue;
				}
				
				// Send an Unequipped Message in system window of the player for each Item
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_BEEN_DISARMED);
				sm.addItemName(item.getItemId());
				sendPacket(sm);
			}
		}
		
		// Send the StatusUpdate Server->Client Packet to the player with new CUR_LOAD (0x0e) information
		final StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		
		// Send the ItemList Server->Client Packet to the player in order to refresh its Inventory
		sendPacket(new ItemList(getInventory().getItems(), true));
		
		// Send a Server->Client packet UserInfo to this Player and CharInfo to all Player in its _KnownPlayers
		broadcastUserInfo();
		
		// Sends message to client if requested
		sendMessage("Trying-on mode has ended.");
	}
	
	/**
	 * Transfers item to another ItemContainer and send a Server->Client InventoryUpdate packet to the Player.
	 * @param process : String Identifier of process triggering this action
	 * @param objectId the object id
	 * @param count : int Quantity of items to be transfered
	 * @param target the target
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @return Item corresponding to the new item or the updated item in inventory
	 */
	public Item transferItem(String process, int objectId, int count, Inventory target, WorldObject reference)
	{
		final Item oldItem = checkItemManipulation(objectId, count, "transfer");
		if (oldItem == null)
		{
			return null;
		}
		
		final Item newItem = getInventory().transferItem(process, objectId, count, target, this, reference);
		if (newItem == null)
		{
			return null;
		}
		
		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			final InventoryUpdate playerIU = new InventoryUpdate();
			if ((oldItem.getCount() > 0) && (oldItem != newItem))
			{
				playerIU.addModifiedItem(oldItem);
			}
			else
			{
				playerIU.addRemovedItem(oldItem);
			}
			sendPacket(playerIU);
		}
		else
		{
			sendPacket(new ItemList(this, false));
		}
		
		// Update current load as well
		StatusUpdate playerSU = new StatusUpdate(getObjectId());
		playerSU.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(playerSU);
		
		// Send target update packet
		if (target instanceof PlayerInventory)
		{
			final Player targetPlayer = ((PlayerInventory) target).getOwner();
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				final InventoryUpdate playerIU = new InventoryUpdate();
				if (newItem.getCount() > count)
				{
					playerIU.addModifiedItem(newItem);
				}
				else
				{
					playerIU.addNewItem(newItem);
				}
				targetPlayer.sendPacket(playerIU);
			}
			else
			{
				targetPlayer.sendPacket(new ItemList(targetPlayer, false));
			}
			
			// Update current load as well
			playerSU = new StatusUpdate(targetPlayer.getObjectId());
			playerSU.addAttribute(StatusUpdate.CUR_LOAD, targetPlayer.getCurrentLoad());
			targetPlayer.sendPacket(playerSU);
		}
		else if (target instanceof PetInventory)
		{
			final PetInventoryUpdate petIU = new PetInventoryUpdate();
			if (newItem.getCount() > count)
			{
				petIU.addModifiedItem(newItem);
			}
			else
			{
				petIU.addNewItem(newItem);
			}
			((PetInventory) target).getOwner().getOwner().sendPacket(petIU);
		}
		
		return newItem;
	}
	
	/**
	 * Drop item from inventory and send a Server->Client InventoryUpdate packet to the Player.
	 * @param process : String Identifier of process triggering this action
	 * @param item : Item to be dropped
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @param protectItem the protect item
	 */
	public void dropItem(String process, Item item, WorldObject reference, boolean sendMessage, boolean protectItem)
	{
		if (_freight.getItemByObjectId(item.getObjectId()) != null)
		{
			// Send a Server->Client ActionFailed to the Player in order to avoid that the client wait another packet
			sendPacket(ActionFailed.STATIC_PACKET);
			
			Util.handleIllegalPlayerAction(this, "Warning!! Character " + getName() + " of account " + getAccountName() + " tried to drop Freight Items", IllegalPlayerAction.PUNISH_KICK);
			return;
		}
		
		final Item droppedItem = _inventory.dropItem(process, item, this, reference);
		if (droppedItem == null)
		{
			if (sendMessage)
			{
				sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
			}
			return;
		}
		
		droppedItem.dropMe(this, (getX() + Rnd.get(50)) - 25, (getY() + Rnd.get(50)) - 25, getZ() + 20);
		
		if ((Config.AUTODESTROY_ITEM_AFTER > 0) && Config.DESTROY_DROPPED_PLAYER_ITEM && !Config.LIST_PROTECTED_ITEMS.contains(droppedItem.getItemId()) && ((droppedItem.isEquipable() && Config.DESTROY_EQUIPABLE_PLAYER_ITEM) || !droppedItem.isEquipable()))
		{
			ItemsAutoDestroyTaskManager.getInstance().addItem(droppedItem);
		}
		if (Config.DESTROY_DROPPED_PLAYER_ITEM)
		{
			droppedItem.setProtected(droppedItem.isEquipable() && (!droppedItem.isEquipable() || !Config.DESTROY_EQUIPABLE_PLAYER_ITEM));
		}
		else
		{
			droppedItem.setProtected(true);
		}
		
		if (protectItem)
		{
			droppedItem.getDropProtection().protect(this);
		}
		
		// Send inventory update packet.
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			final InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(droppedItem);
			sendPacket(playerIU);
		}
		else
		{
			sendPacket(new ItemList(this, false));
		}
		
		// Update current load as well.
		final StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		
		// Sends message to client if requested.
		if (sendMessage)
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_DROPPED_S1);
			sm.addItemName(droppedItem.getItemId());
			sendPacket(sm);
		}
	}
	
	/**
	 * Drop item from inventory by using its <b>objectID</b> and send a Server->Client InventoryUpdate packet to the Player.
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : int Item Instance identifier of the item to be dropped
	 * @param count : int Quantity of items to be dropped
	 * @param x : int coordinate for drop X
	 * @param y : int coordinate for drop Y
	 * @param z : int coordinate for drop Z
	 * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @param protectItem the protect item
	 * @return Item corresponding to the new item or the updated item in inventory
	 */
	public Item dropItem(String process, int objectId, int count, int x, int y, int z, WorldObject reference, boolean sendMessage, boolean protectItem)
	{
		if (_freight.getItemByObjectId(objectId) != null)
		{
			// Send a Server->Client ActionFailed to the Player in order to avoid that the client wait another packet
			sendPacket(ActionFailed.STATIC_PACKET);
			
			Util.handleIllegalPlayerAction(this, "Warning!! Character " + getName() + " of account " + getAccountName() + " tried to drop Freight Items", IllegalPlayerAction.PUNISH_KICK);
			return null;
		}
		
		final Item inventoryItem = _inventory.getItemByObjectId(objectId);
		final Item droppedItem = _inventory.dropItem(process, objectId, count, this, reference);
		if (droppedItem == null)
		{
			if (sendMessage)
			{
				sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
			}
			return null;
		}
		
		droppedItem.dropMe(this, x, y, z);
		
		if ((Config.AUTODESTROY_ITEM_AFTER > 0) && Config.DESTROY_DROPPED_PLAYER_ITEM && !Config.LIST_PROTECTED_ITEMS.contains(droppedItem.getItemId()) && ((droppedItem.isEquipable() && Config.DESTROY_EQUIPABLE_PLAYER_ITEM) || !droppedItem.isEquipable()))
		{
			ItemsAutoDestroyTaskManager.getInstance().addItem(droppedItem);
		}
		if (Config.DESTROY_DROPPED_PLAYER_ITEM)
		{
			droppedItem.setProtected(droppedItem.isEquipable() && (!droppedItem.isEquipable() || !Config.DESTROY_EQUIPABLE_PLAYER_ITEM));
		}
		else
		{
			droppedItem.setProtected(true);
		}
		
		if (protectItem)
		{
			droppedItem.getDropProtection().protect(this);
		}
		
		// Send inventory update packet.
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			final InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(inventoryItem);
			sendPacket(playerIU);
		}
		else
		{
			sendPacket(new ItemList(this, false));
		}
		
		// Update current load as well.
		final StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		
		// Sends message to client if requested.
		if (sendMessage)
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_DROPPED_S1);
			sm.addItemName(droppedItem.getItemId());
			sendPacket(sm);
		}
		
		return droppedItem;
	}
	
	/**
	 * Check item manipulation.
	 * @param objectId the object id
	 * @param count the count
	 * @param action the action
	 * @return the item instance
	 */
	public Item checkItemManipulation(int objectId, int count, String action)
	{
		if (World.getInstance().findObject(objectId) == null)
		{
			LOGGER.warning(getObjectId() + ": player tried to " + action + " item not available in World");
			return null;
		}
		
		final Item item = getInventory().getItemByObjectId(objectId);
		if ((item == null) || (item.getOwnerId() != getObjectId()))
		{
			LOGGER.warning(getObjectId() + ": player tried to " + action + " item he is not owner of");
			return null;
		}
		
		if ((count < 0) || ((count > 1) && !item.isStackable()))
		{
			LOGGER.warning(getObjectId() + ": player tried to " + action + " item with invalid count: " + count);
			return null;
		}
		
		if (count > item.getCount())
		{
			LOGGER.warning(getObjectId() + ": player tried to " + action + " more items than he owns");
			return null;
		}
		
		// Pet is summoned and not the item that summoned the pet AND not the buggle from strider you're mounting
		if (((getPet() != null) && (getPet().getControlItemId() == objectId)) || (getMountObjectID() == objectId))
		{
			return null;
		}
		
		if ((getActiveEnchantItem() != null) && (getActiveEnchantItem().getObjectId() == objectId))
		{
			return null;
		}
		
		if (item.isWear())
		{
			// cannot drop/trade wear-items
			return null;
		}
		
		return item;
	}
	
	/**
	 * Set _protectEndTime according settings.
	 * @param protect the new protection
	 */
	public void setProtection(boolean protect)
	{
		if (isInOlympiadMode())
		{
			return;
		}
		
		_protectEndTime = protect ? GameTimeTaskManager.getGameTicks() + (Config.PLAYER_SPAWN_PROTECTION * GameTimeTaskManager.TICKS_PER_SECOND) : 0;
		if (protect)
		{
			ThreadPool.schedule(new TeleportProtectionFinalizer(this), (Config.PLAYER_SPAWN_PROTECTION - 1) * 1000);
		}
	}
	
	/**
	 * Set _teleportProtectEndTime according settings.
	 * @param protect the new protection
	 */
	public void setTeleportProtection(boolean protect)
	{
		_teleportProtectEndTime = protect ? GameTimeTaskManager.getGameTicks() + (Config.PLAYER_TELEPORT_PROTECTION * GameTimeTaskManager.TICKS_PER_SECOND) : 0;
		if (protect)
		{
			ThreadPool.schedule(new TeleportProtectionFinalizer(this), (Config.PLAYER_TELEPORT_PROTECTION - 1) * 1000);
		}
	}
	
	static class TeleportProtectionFinalizer implements Runnable
	{
		private final Player _player;
		
		TeleportProtectionFinalizer(Player player)
		{
			_player = player;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (_player.isSpawnProtected())
				{
					_player.sendMessage("The effect of Spawn Protection has been removed.");
				}
				else if (_player.isTeleportProtected())
				{
					_player.sendMessage("The effect of Teleport Spawn Protection has been removed.");
				}
				
				if (Config.PLAYER_SPAWN_PROTECTION > 0)
				{
					_player.setProtection(false);
				}
				
				if (Config.PLAYER_TELEPORT_PROTECTION > 0)
				{
					_player.setTeleportProtection(false);
				}
			}
			catch (Throwable e)
			{
			}
		}
	}
	
	/**
	 * Set protection from agro mobs when getting up from fake death, according settings.
	 * @param protect the new recent fake death
	 */
	public void setRecentFakeDeath(boolean protect)
	{
		_recentFakeDeathEndTime = protect ? GameTimeTaskManager.getGameTicks() + (Config.PLAYER_FAKEDEATH_UP_PROTECTION * GameTimeTaskManager.TICKS_PER_SECOND) : 0;
	}
	
	/**
	 * Checks if is recent fake death.
	 * @return true, if is recent fake death
	 */
	public boolean isRecentFakeDeath()
	{
		return _recentFakeDeathEndTime > GameTimeTaskManager.getGameTicks();
	}
	
	/**
	 * Get the client owner of this char.
	 * @return the client
	 */
	public GameClient getClient()
	{
		return _client;
	}
	
	/**
	 * Sets the client.
	 * @param client the new client
	 */
	public void setClient(GameClient client)
	{
		_client = client;
	}
	
	/**
	 * Close the active connection with the client.
	 */
	public void closeNetConnection()
	{
		if (_client != null)
		{
			_client.close(new LeaveWorld());
			setClient(null);
		}
	}
	
	/**
	 * Manage actions when a player click on this Player.<br>
	 * <br>
	 * <b><u>Actions on first click on the Player (Select it)</u>:</b><br>
	 * <li>Set the target of the player</li>
	 * <li>Send a Server->Client packet MyTargetSelected to the player (display the select window)</li><br>
	 * <br>
	 * <b><u>Actions on second click on the Player (Follow it/Attack it/Intercat with it)</u>:</b><br>
	 * <li>Send a Server->Client packet MyTargetSelected to the player (display the select window)</li>
	 * <li>If this Player has a Private Store, notify the player AI with AI_INTENTION_INTERACT</li>
	 * <li>If this Player is autoAttackable, notify the player AI with AI_INTENTION_ATTACK</li>
	 * <li>If this Player is NOT autoAttackable, notify the player AI with AI_INTENTION_FOLLOW</li><br>
	 * <br>
	 * <b><u>Example of use</u>:</b><br>
	 * <li>Client packet : Action, AttackRequest</li><br>
	 * @param player The player that start an action on this Player
	 */
	@Override
	public void onAction(Player player)
	{
		// Check if the Player is confused
		if (player.isOutOfControl())
		{
			// Send a Server->Client packet ActionFailed to the player
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if the player already target this Player
		if (player.getTarget() != this)
		{
			// Set the target of the player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the player
			// The color to display in the select window is White
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));
			if (player != this)
			{
				player.sendPacket(new ValidateLocation(this));
			}
		}
		else
		{
			if (player != this)
			{
				player.sendPacket(new ValidateLocation(this));
			}
			// Check if this Player has a Private Store
			if (getPrivateStoreType() != 0)
			{
				// Notify the Player AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				
				// Calculate the distance between the Player
				if (canInteract(player))
				{
					// Notify the Player AI with AI_INTENTION_INTERACT
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
				}
			}
			else if (isAutoAttackable(player))
			{
				// Player with lvl < 21 can't attack a cursed weapon holder
				// And a cursed weapon holder can't attack players with lvl < 21
				if ((isCursedWeaponEquiped() && (player.getLevel() < 21)) || (player.isCursedWeaponEquiped() && (getLevel() < 21)))
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else if (Config.PATHFINDING)
				{
					if (GeoEngine.getInstance().canSeeTarget(player, this))
					{
						player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
						player.onActionRequest();
					}
				}
				else
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
					player.onActionRequest();
				}
			}
			else if (Config.PATHFINDING)
			{
				if (GeoEngine.getInstance().canSeeTarget(player, this))
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
				}
			}
			else
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
			}
		}
	}
	
	@Override
	public void onActionShift(Player player)
	{
		final Weapon currentWeapon = player.getActiveWeaponItem();
		if (player.isGM())
		{
			if (this != player.getTarget())
			{
				player.setTarget(this);
				player.sendPacket(new MyTargetSelected(getObjectId(), 0));
				if (player != this)
				{
					player.sendPacket(new ValidateLocation(this));
				}
			}
			else
			{
				AdminEditChar.gatherCharacterInfo(player, this, "charinfo.htm");
			}
		}
		else // Like L2OFF set the target of the Player player
		{
			// Check if the Player is confused
			if (player.isOutOfControl())
			{
				// Send a Server->Client packet ActionFailed to the player
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			// Check if the player already target this Player
			if (player.getTarget() != this)
			{
				// Set the target of the player
				player.setTarget(this);
				
				// Send a Server->Client packet MyTargetSelected to the player
				// The color to display in the select window is White
				player.sendPacket(new MyTargetSelected(getObjectId(), 0));
				if (player != this)
				{
					player.sendPacket(new ValidateLocation(this));
				}
			}
			else
			{
				if (player != this)
				{
					player.sendPacket(new ValidateLocation(this));
				}
				// Check if this Player has a Private Store
				if (getPrivateStoreType() != 0)
				{
					// Notify the Player AI with AI_INTENTION_INTERACT
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
					
					// Calculate the distance between the Player
					if (canInteract(player))
					{
						// Notify the Player AI with AI_INTENTION_INTERACT
						player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
					}
				}
				else if (isAutoAttackable(player))
				{
					// Player with lvl < 21 can't attack a cursed weapon holder
					// And a cursed weapon holder can't attack players with lvl < 21
					if ((isCursedWeaponEquiped() && (player.getLevel() < 21)) || (player.isCursedWeaponEquiped() && (getLevel() < 21)))
					{
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
					else if (Config.PATHFINDING)
					{
						if (GeoEngine.getInstance().canSeeTarget(player, this))
						{
							// Calculate the distance between the Player
							// Only archer can hit from long
							if ((currentWeapon != null) && (currentWeapon.getItemType() == WeaponType.BOW))
							{
								player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
								player.onActionRequest();
							}
							else if (canInteract(player))
							{
								player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
								player.onActionRequest();
							}
							else
							{
								player.sendPacket(ActionFailed.STATIC_PACKET);
							}
						}
					}
					else // Calculate the distance between the Player. Only archer can hit from long.
					if ((currentWeapon != null) && (currentWeapon.getItemType() == WeaponType.BOW))
					{
						player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
						player.onActionRequest();
					}
					else if (canInteract(player))
					{
						player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
						player.onActionRequest();
					}
					else
					{
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
				}
				else if (Config.PATHFINDING)
				{
					if (GeoEngine.getInstance().canSeeTarget(player, this))
					{
						// Calculate the distance between the Player. Only archer can hit from long.
						if ((currentWeapon != null) && (currentWeapon.getItemType() == WeaponType.BOW))
						{
							player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
						}
						else if (canInteract(player))
						{
							player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
						}
						else
						{
							player.sendPacket(ActionFailed.STATIC_PACKET);
						}
					}
				}
				else if ((currentWeapon != null) && (currentWeapon.getItemType() == WeaponType.BOW)) // Calculate the distance between the Player. Only archer can hit from long.
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
				}
				else if (canInteract(player))
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
				}
				else
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
			}
		}
	}
	
	/**
	 * Are player offensive skills locked.
	 * @return true, if successful
	 */
	public boolean arePlayerOffensiveSkillsLocked()
	{
		return isInOlympiadMode() && !isOlympiadStart();
	}
	
	/**
	 * Returns true if cp update should be done, false if not.
	 * @return boolean
	 */
	private boolean needCpUpdate()
	{
		final double currentCp = getCurrentCp();
		if ((currentCp <= 1.0) || (getMaxCp() < MAX_HP_BAR_PX))
		{
			return true;
		}
		
		if ((currentCp <= _cpUpdateDecCheck) || (currentCp >= _cpUpdateIncCheck))
		{
			if (currentCp == getMaxCp())
			{
				_cpUpdateIncCheck = currentCp + 1;
				_cpUpdateDecCheck = currentCp - _cpUpdateInterval;
			}
			else
			{
				final double doubleMulti = currentCp / _cpUpdateInterval;
				int intMulti = (int) doubleMulti;
				_cpUpdateDecCheck = _cpUpdateInterval * (doubleMulti < intMulti ? intMulti - 1 : intMulti);
				_cpUpdateIncCheck = _cpUpdateDecCheck + _cpUpdateInterval;
			}
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Returns true if mp update should be done, false if not.
	 * @return boolean
	 */
	private boolean needMpUpdate()
	{
		final double currentMp = getCurrentMp();
		if ((currentMp <= 1.0) || (getMaxMp() < MAX_HP_BAR_PX))
		{
			return true;
		}
		
		if ((currentMp <= _mpUpdateDecCheck) || (currentMp >= _mpUpdateIncCheck))
		{
			if (currentMp == getMaxMp())
			{
				_mpUpdateIncCheck = currentMp + 1;
				_mpUpdateDecCheck = currentMp - _mpUpdateInterval;
			}
			else
			{
				final double doubleMulti = currentMp / _mpUpdateInterval;
				int intMulti = (int) doubleMulti;
				_mpUpdateDecCheck = _mpUpdateInterval * (doubleMulti < intMulti ? intMulti - 1 : intMulti);
				_mpUpdateIncCheck = _mpUpdateDecCheck + _mpUpdateInterval;
			}
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Send packet StatusUpdate with current HP,MP and CP to the Player and only current HP, MP and Level to all other Player of the Party.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Send the Server->Client packet StatusUpdate with current HP, MP and CP to this Player</li>
	 * <li>Send the Server->Client packet PartySmallWindowUpdate with current HP, MP and Level to all other Player of the Party</li><br>
	 * <font color=#FF0000><b><u>Caution</u>: This method DOESN'T SEND current HP and MP to all Player of the _statusListener</b></font>
	 */
	@Override
	public void broadcastStatusUpdate()
	{
		// We must not send these informations to other players
		// Send the Server->Client packet StatusUpdate with current HP and MP to all Player that must be informed of HP/MP updates of this Player
		// super.broadcastStatusUpdate();
		
		// Send the Server->Client packet StatusUpdate with current HP, MP and CP to this Player
		if (Config.FORCE_COMPLETE_STATUS_UPDATE)
		{
			final StatusUpdate su = new StatusUpdate(this);
			sendPacket(su);
		}
		else
		{
			final StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
			su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
			su.addAttribute(StatusUpdate.CUR_CP, (int) getCurrentCp());
			su.addAttribute(StatusUpdate.MAX_CP, getMaxCp());
			sendPacket(su);
		}
		
		// Check if a party is in progress and party window update is usefull
		if (isInParty() && (needCpUpdate() || needHpUpdate() || needMpUpdate()))
		{
			// Send the Server->Client packet PartySmallWindowUpdate with current HP, MP and Level to all other Player of the Party
			getParty().broadcastToPartyMembers(this, new PartySmallWindowUpdate(this));
		}
		
		if (isInOlympiadMode())
		{
			for (Player player : getKnownList().getKnownPlayers().values())
			{
				if ((player.getOlympiadGameId() == getOlympiadGameId()) && player.isOlympiadStart())
				{
					player.sendPacket(new ExOlympiadUserInfo(this, 1));
				}
			}
			if ((Olympiad.getInstance().getSpectators(_olympiadGameId) != null) && isOlympiadStart())
			{
				for (Player spectator : Olympiad.getInstance().getSpectators(_olympiadGameId))
				{
					if (spectator == null)
					{
						continue;
					}
					spectator.sendPacket(new ExOlympiadUserInfo(this, getOlympiadSide()));
				}
			}
		}
		
		if (isInDuel())
		{
			DuelManager.getInstance().broadcastToOppositTeam(this, new ExDuelUpdateUserInfo(this));
		}
	}
	
	/**
	 * Update pvp color.
	 * @param pvpKillAmount the pvp kill amount
	 */
	public void updatePvPColor(int pvpKillAmount)
	{
		if (Config.PVP_COLOR_SYSTEM_ENABLED)
		{
			// Check if the character has GM access and if so, let them be.
			if (isGM())
			{
				return;
			}
			
			// Check if the character is donator and if so, let them be.
			if (isDonator())
			{
				return;
			}
			
			if ((pvpKillAmount >= Config.PVP_AMOUNT1) && (pvpKillAmount < Config.PVP_AMOUNT2))
			{
				getAppearance().setNameColor(Config.NAME_COLOR_FOR_PVP_AMOUNT1);
			}
			else if ((pvpKillAmount >= Config.PVP_AMOUNT2) && (pvpKillAmount < Config.PVP_AMOUNT3))
			{
				getAppearance().setNameColor(Config.NAME_COLOR_FOR_PVP_AMOUNT2);
			}
			else if ((pvpKillAmount >= Config.PVP_AMOUNT3) && (pvpKillAmount < Config.PVP_AMOUNT4))
			{
				getAppearance().setNameColor(Config.NAME_COLOR_FOR_PVP_AMOUNT3);
			}
			else if ((pvpKillAmount >= Config.PVP_AMOUNT4) && (pvpKillAmount < Config.PVP_AMOUNT5))
			{
				getAppearance().setNameColor(Config.NAME_COLOR_FOR_PVP_AMOUNT4);
			}
			else if (pvpKillAmount >= Config.PVP_AMOUNT5)
			{
				getAppearance().setNameColor(Config.NAME_COLOR_FOR_PVP_AMOUNT5);
			}
		}
	}
	
	/**
	 * Update pk color.
	 * @param pkKillAmount the pk kill amount
	 */
	public void updatePkColor(int pkKillAmount)
	{
		if (Config.PK_COLOR_SYSTEM_ENABLED)
		{
			// Check if the character has GM access and if so, let them be, like above.
			if (isGM())
			{
				return;
			}
			
			if ((pkKillAmount >= Config.PK_AMOUNT1) && (pkKillAmount < Config.PVP_AMOUNT2))
			{
				getAppearance().setTitleColor(Config.TITLE_COLOR_FOR_PK_AMOUNT1);
			}
			else if ((pkKillAmount >= Config.PK_AMOUNT2) && (pkKillAmount < Config.PVP_AMOUNT3))
			{
				getAppearance().setTitleColor(Config.TITLE_COLOR_FOR_PK_AMOUNT2);
			}
			else if ((pkKillAmount >= Config.PK_AMOUNT3) && (pkKillAmount < Config.PVP_AMOUNT4))
			{
				getAppearance().setTitleColor(Config.TITLE_COLOR_FOR_PK_AMOUNT3);
			}
			else if ((pkKillAmount >= Config.PK_AMOUNT4) && (pkKillAmount < Config.PVP_AMOUNT5))
			{
				getAppearance().setTitleColor(Config.TITLE_COLOR_FOR_PK_AMOUNT4);
			}
			else if (pkKillAmount >= Config.PK_AMOUNT5)
			{
				getAppearance().setTitleColor(Config.TITLE_COLOR_FOR_PK_AMOUNT5);
			}
		}
	}
	
	/**
	 * Send a Server->Client packet UserInfo to this Player and CharInfo to all Player in its _KnownPlayers.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * Others Player in the detection area of the Player are identified in <b>_knownPlayers</b>. In order to inform other players of this Player state modifications, server just need to go through _knownPlayers to send Server->Client Packet<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Send a Server->Client packet UserInfo to this Player (Public and Private Data)</li>
	 * <li>Send a Server->Client packet CharInfo to all Player in _KnownPlayers of the Player (Public data only)</li><br>
	 * <font color=#FF0000><b><u>Caution</u>: DON'T SEND UserInfo packet to other players instead of CharInfo packet. Indeed, UserInfo packet contains PRIVATE DATA as MaxHP, STR, DEX...</b></font>
	 */
	public void broadcastUserInfo()
	{
		// Send a Server->Client packet UserInfo to this Player
		sendPacket(new UserInfo(this));
		
		// Broadcast char info to known players
		broadcastCharInfo();
	}
	
	public void broadcastCharInfo()
	{
		final CharInfo charInfo = new CharInfo(this, false);
		for (Player player : getKnownList().getKnownPlayers().values())
		{
			if (player == null)
			{
				continue;
			}
			
			try
			{
				if (player.isGM() && getAppearance().isInvisible())
				{
					player.sendPacket(new CharInfo(this, true));
				}
				else
				{
					player.sendPacket(charInfo);
				}
			}
			catch (NullPointerException e)
			{
			}
		}
	}
	
	/**
	 * Broadcast title info.
	 */
	public void broadcastTitleInfo()
	{
		// Send a Server->Client packet UserInfo to this Player
		sendPacket(new UserInfo(this));
		Broadcast.toKnownPlayers(this, new TitleUpdate(this));
	}
	
	@Override
	public void broadcastPacket(IClientOutgoingPacket mov)
	{
		final boolean isCharInfo = mov instanceof CharInfo;
		if (!isCharInfo)
		{
			sendPacket(mov);
		}
		
		for (Player player : getKnownList().getKnownPlayers().values())
		{
			if (!player.isGM() && (getAppearance().isInvisible() || inObserverMode()))
			{
				continue;
			}
			
			player.sendPacket(mov);
			
			if (isCharInfo)
			{
				final int relation = getRelation(player);
				if ((getKnownList().getKnownRelations().get(player.getObjectId()) != null) && (getKnownList().getKnownRelations().get(player.getObjectId()) != relation))
				{
					player.sendPacket(new RelationChanged(this, relation, player.isAutoAttackable(this)));
				}
			}
		}
	}
	
	@Override
	public void broadcastPacket(IClientOutgoingPacket mov, int radius)
	{
		final boolean isCharInfo = mov instanceof CharInfo;
		if (!isCharInfo)
		{
			sendPacket(mov);
		}
		
		for (Player player : getKnownList().getKnownPlayers().values())
		{
			if (!isInsideRadius3D(player, radius))
			{
				continue;
			}
			
			if (!player.isGM() && (getAppearance().isInvisible() || inObserverMode()))
			{
				continue;
			}
			
			player.sendPacket(mov);
			
			if (isCharInfo)
			{
				final int relation = getRelation(player);
				if ((getKnownList().getKnownRelations().get(player.getObjectId()) != null) && (getKnownList().getKnownRelations().get(player.getObjectId()) != relation))
				{
					player.sendPacket(new RelationChanged(this, relation, player.isAutoAttackable(this)));
				}
			}
		}
	}
	
	/**
	 * @return the Alliance Identifier of the Player.
	 */
	public int getAllyId()
	{
		return _clan == null ? 0 : _clan.getAllyId();
	}
	
	public int getAllyCrestId()
	{
		return getAllyId() == 0 ? 0 : _clan.getAllyCrestId();
	}
	
	/**
	 * Manage Interact Task with another Player.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>If the private store is a STORE_PRIVATE_SELL, send a Server->Client PrivateBuyListSell packet to the Player</li>
	 * <li>If the private store is a STORE_PRIVATE_BUY, send a Server->Client PrivateBuyListBuy packet to the Player</li>
	 * <li>If the private store is a STORE_PRIVATE_MANUFACTURE, send a Server->Client RecipeShopSellList packet to the Player</li><br>
	 * @param target The Creature targeted
	 */
	public void doInteract(Creature target)
	{
		if (target instanceof Player)
		{
			final Player temp = (Player) target;
			sendPacket(ActionFailed.STATIC_PACKET);
			
			if ((temp.getPrivateStoreType() == STORE_PRIVATE_SELL) || (temp.getPrivateStoreType() == STORE_PRIVATE_PACKAGE_SELL))
			{
				sendPacket(new PrivateStoreListSell(this, temp));
			}
			else if (temp.getPrivateStoreType() == STORE_PRIVATE_BUY)
			{
				sendPacket(new PrivateStoreListBuy(this, temp));
			}
			else if (temp.getPrivateStoreType() == STORE_PRIVATE_MANUFACTURE)
			{
				sendPacket(new RecipeShopSellList(this, temp));
			}
		}
		else if (target != null) // _interactTarget=null should never happen but one never knows ^^;
		{
			target.onAction(this);
		}
	}
	
	/**
	 * Manage AutoLoot Task.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Send a System Message to the Player : YOU_PICKED_UP_S1_ADENA or YOU_PICKED_UP_S1_S2</li>
	 * <li>Add the Item to the Player inventory</li>
	 * <li>Send a Server->Client packet InventoryUpdate to this Player with NewItem (use a new slot) or ModifiedItem (increase amount)</li>
	 * <li>Send a Server->Client packet StatusUpdate to this Player with current weight</li><br>
	 * <font color=#FF0000><b><u>Caution</u>: If a Party is in progress, distribute Items between party members</b></font>
	 * @param target The Item dropped
	 * @param item the item
	 */
	public void doAutoLoot(Attackable target, ItemHolder item)
	{
		if (isInParty())
		{
			getParty().distributeItem(this, item, false, target);
		}
		else if (item.getId() == 57)
		{
			addAdena("AutoLoot", item.getCount(), target, true);
		}
		else
		{
			addItem("AutoLoot", item.getId(), item.getCount(), target, true);
		}
	}
	
	/**
	 * Manage Pickup Task.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Send a Server->Client packet StopMove to this Player</li>
	 * <li>Remove the Item from the world and send server->client GetItem packets</li>
	 * <li>Send a System Message to the Player : YOU_PICKED_UP_S1_ADENA or YOU_PICKED_UP_S1_S2</li>
	 * <li>Add the Item to the Player inventory</li>
	 * <li>Send a Server->Client packet InventoryUpdate to this Player with NewItem (use a new slot) or ModifiedItem (increase amount)</li>
	 * <li>Send a Server->Client packet StatusUpdate to this Player with current weight</li><br>
	 * <font color=#FF0000><b><u>Caution</u>: If a Party is in progress, distribute Items between party members</b></font>
	 * @param object The Item to pick up
	 */
	protected void doPickupItem(WorldObject object)
	{
		if (isAlikeDead() || isFakeDeath())
		{
			return;
		}
		
		// Set the AI Intention to AI_INTENTION_IDLE
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		
		// Check if the WorldObject to pick up is a Item
		if (!(object instanceof Item))
		{
			// dont try to pickup anything that is not an item :)
			LOGGER.warning(this + "trying to pickup wrong target." + getTarget());
			return;
		}
		
		final Item target = (Item) object;
		
		// Send a Server->Client packet ActionFailed to this Player
		sendPacket(ActionFailed.STATIC_PACKET);
		
		// Send a Server->Client packet StopMove to this Player
		sendPacket(new StopMove(this));
		
		synchronized (target)
		{
			// Check if the target to pick up is visible
			if (!target.isSpawned())
			{
				// Send a Server->Client packet ActionFailed to this Player
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			// Like L2OFF you can't pickup items with private store opened
			if (getPrivateStoreType() != 0)
			{
				// Send a Server->Client packet ActionFailed to this Player
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (!target.getDropProtection().tryPickUp(this) && (target.getItemId() != 8190) && (target.getItemId() != 8689))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				final SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_PICK_UP_S1);
				smsg.addItemName(target.getItemId());
				sendPacket(smsg);
				return;
			}
			if (((isInParty() && (getParty().getLootDistribution() == Party.ITEM_LOOTER)) || !isInParty()) && !_inventory.validateCapacity(target))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.YOUR_INVENTORY_IS_FULL);
				return;
			}
			if (isInvul() && !isGM())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				final SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_PICK_UP_S1);
				smsg.addItemName(target.getItemId());
				sendPacket(smsg);
				return;
			}
			if ((target.getOwnerId() != 0) && (target.getOwnerId() != getObjectId()) && !isInLooterParty(target.getOwnerId()))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				
				if (target.getItemId() == 57)
				{
					final SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_PICK_UP_S1_ADENA);
					smsg.addNumber(target.getCount());
					sendPacket(smsg);
				}
				else if (target.getCount() > 1)
				{
					final SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_PICK_UP_S2_S1_S);
					smsg.addItemName(target.getItemId());
					smsg.addNumber(target.getCount());
					sendPacket(smsg);
				}
				else
				{
					final SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_PICK_UP_S1);
					smsg.addItemName(target.getItemId());
					sendPacket(smsg);
				}
				return;
			}
			
			if ((target.getItemId() == 57) && (_inventory.getAdena() == Integer.MAX_VALUE))
			{
				sendMessage("You have reached the maximum amount of adena, please spend or deposit the adena so you may continue obtaining adena.");
				return;
			}
			
			if ((target.getItemLootShedule() != null) && ((target.getOwnerId() == getObjectId()) || isInLooterParty(target.getOwnerId())))
			{
				target.resetOwnerTimer();
			}
			
			// Fixed it's not possible pick up the object if you exceed the maximum weight.
			if ((_inventory.getTotalWeight() + (target.getItem().getWeight() * target.getCount())) > getMaxLoad())
			{
				sendMessage("You have reached the maximun weight.");
				return;
			}
			
			// Remove the Item from the world and send server->client GetItem packets
			target.pickupMe(this);
			if (Config.SAVE_DROPPED_ITEM)
			{
				ItemsOnGroundManager.getInstance().removeObject(target);
			}
		}
		
		// Auto use herbs - pick up
		if (target.getItemType() == EtcItemType.HERB)
		{
			final IItemHandler handler = ItemHandler.getInstance().getItemHandler(target.getItemId());
			if (handler == null)
			{
				LOGGER.info("No item handler registered for item ID " + target.getItemId() + ".");
			}
			else
			{
				handler.useItem(this, target);
			}
			ItemTable.getInstance().destroyItem("Consume", target, this, null);
		}
		// Cursed Weapons are not distributed
		else if (CursedWeaponsManager.getInstance().isCursed(target.getItemId()))
		{
			addItem("Pickup", target, null, true);
		}
		else if (FortSiegeManager.getInstance().isCombat(target.getItemId()))
		{
			addItem("Pickup", target, null, true);
		}
		else
		{
			// if item is instance of ArmorType or WeaponType broadcast an "Attention" system message
			if ((target.getItemType() instanceof ArmorType) || (target.getItemType() instanceof WeaponType) || (target.getItem() instanceof Armor) || (target.getItem() instanceof Weapon))
			{
				if (target.getEnchantLevel() > 0)
				{
					final SystemMessage msg = new SystemMessage(SystemMessageId.ATTENTION_S1_PICKED_UP_S2_S3);
					msg.addString(getName());
					msg.addNumber(target.getEnchantLevel());
					msg.addItemName(target.getItemId());
					broadcastPacket(msg, 1400);
				}
				else
				{
					final SystemMessage msg = new SystemMessage(SystemMessageId.ATTENTION_S1_PICKED_UP_S2);
					msg.addString(getName());
					msg.addItemName(target.getItemId());
					broadcastPacket(msg, 1400);
				}
			}
			
			// Check if a Party is in progress
			if (isInParty())
			{
				getParty().distributeItem(this, target);
			}
			else if ((target.getItemId() == 57) && (getInventory().getAdenaInstance() != null))
			{
				addAdena("Pickup", target.getCount(), null, true);
				ItemTable.getInstance().destroyItem("Pickup", target, this, null);
			}
			// Target is regular item
			else
			{
				addItem("Pickup", target, null, true);
				
				// Like L2OFF Auto-Equip arrows if player has a bow and player picks up arrows.
				if ((target.getItem() != null) && (target.getItem().getItemType() == EtcItemType.ARROW))
				{
					checkAndEquipArrows();
				}
			}
		}
	}
	
	/**
	 * Set a target.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Remove the Player from the _statusListener of the old target if it was a Creature</li>
	 * <li>Add the Player to the _statusListener of the new target if it's a Creature</li>
	 * <li>Target the new WorldObject (add the target to the Player _target, _knownObject and Player to _KnownObject of the WorldObject)</li><br>
	 * @param worldObject The WorldObject to target
	 */
	@Override
	public void setTarget(WorldObject worldObject)
	{
		// Check if the new target is visible
		WorldObject newTarget = worldObject;
		if ((newTarget != null) && !newTarget.isSpawned())
		{
			newTarget = null;
		}
		
		// Prevents /target exploiting
		if ((newTarget != null) && (!(newTarget instanceof Player) || !isInParty() || !((Player) newTarget).isInParty() || (getParty().getPartyLeaderOID() != ((Player) newTarget).getParty().getPartyLeaderOID())) && (Math.abs(newTarget.getZ() - getZ()) > Config.DIFFERENT_Z_NEW_MOVIE))
		{
			newTarget = null;
		}
		
		if (!isGM())
		{
			// Can't target and attack festival monsters if not participant
			if ((newTarget instanceof FestivalMonster) && !isFestivalParticipant())
			{
				newTarget = null;
			}
			else if (isInParty() && getParty().isInDimensionalRift())
			{
				final byte riftType = getParty().getDimensionalRift().getType();
				final byte riftRoom = getParty().getDimensionalRift().getCurrentRoom();
				if ((newTarget != null) && !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(newTarget.getX(), newTarget.getY(), newTarget.getZ()))
				{
					newTarget = null;
				}
			}
		}
		
		// Get the current target
		final WorldObject oldTarget = getTarget();
		if (oldTarget != null)
		{
			if (oldTarget.equals(newTarget))
			{
				return; // no target change
			}
			
			// Remove the Player from the _statusListener of the old target if it was a Creature
			if (oldTarget instanceof Creature)
			{
				((Creature) oldTarget).removeStatusListener(this);
			}
		}
		
		// Add the Player to the _statusListener of the new target if it's a Creature
		if (newTarget instanceof Creature)
		{
			((Creature) newTarget).addStatusListener(this);
			final TargetSelected my = new TargetSelected(getObjectId(), newTarget.getObjectId(), getX(), getY(), getZ());
			
			// Send packet just to me and to party, not to any other that does not use the information
			if (!isInParty())
			{
				sendPacket(my);
			}
			else
			{
				_party.broadcastToPartyMembers(my);
			}
		}
		
		// Target the new WorldObject (add the target to the Player _target, _knownObject and Player to _KnownObject of the WorldObject)
		super.setTarget(newTarget);
	}
	
	/**
	 * Return the active weapon instance (always equipped in the right hand).
	 * @return the active weapon instance
	 */
	@Override
	public Item getActiveWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
	}
	
	/**
	 * Return the active weapon item (always equipped in the right hand).
	 * @return the active weapon item
	 */
	@Override
	public Weapon getActiveWeaponItem()
	{
		final Item weapon = getActiveWeaponInstance();
		if (weapon == null)
		{
			return getFistsWeaponItem();
		}
		return (Weapon) weapon.getItem();
	}
	
	/**
	 * Gets the chest armor instance.
	 * @return the chest armor instance
	 */
	public Item getChestArmorInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
	}
	
	/**
	 * Gets the legs armor instance.
	 * @return the legs armor instance
	 */
	public Item getLegsArmorInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS);
	}
	
	/**
	 * Gets the active chest armor item.
	 * @return the active chest armor item
	 */
	public Armor getActiveChestArmorItem()
	{
		final Item armor = getChestArmorInstance();
		if (armor == null)
		{
			return null;
		}
		return (Armor) armor.getItem();
	}
	
	/**
	 * Gets the active legs armor item.
	 * @return the active legs armor item
	 */
	public Armor getActiveLegsArmorItem()
	{
		final Item legs = getLegsArmorInstance();
		if (legs == null)
		{
			return null;
		}
		return (Armor) legs.getItem();
	}
	
	/**
	 * Checks if is wearing heavy armor.
	 * @return true, if is wearing heavy armor
	 */
	public boolean isWearingHeavyArmor()
	{
		final Item legs = getLegsArmorInstance();
		final Item armor = getChestArmorInstance();
		if ((armor != null) && (legs != null) && ((ArmorType) legs.getItemType() == ArmorType.HEAVY) && ((ArmorType) armor.getItemType() == ArmorType.HEAVY))
		{
			return true;
		}
		if ((armor != null) && ((getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST).getItem().getBodyPart() == ItemTemplate.SLOT_FULL_ARMOR) && ((ArmorType) armor.getItemType() == ArmorType.HEAVY)))
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if is wearing light armor.
	 * @return true, if is wearing light armor
	 */
	public boolean isWearingLightArmor()
	{
		final Item legs = getLegsArmorInstance();
		final Item armor = getChestArmorInstance();
		if ((armor != null) && (legs != null) && ((ArmorType) legs.getItemType() == ArmorType.LIGHT) && ((ArmorType) armor.getItemType() == ArmorType.LIGHT))
		{
			return true;
		}
		if ((armor != null) && ((getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST).getItem().getBodyPart() == ItemTemplate.SLOT_FULL_ARMOR) && ((ArmorType) armor.getItemType() == ArmorType.LIGHT)))
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if is wearing magic armor.
	 * @return true, if is wearing magic armor
	 */
	public boolean isWearingMagicArmor()
	{
		final Item legs = getLegsArmorInstance();
		final Item armor = getChestArmorInstance();
		if ((armor != null) && (legs != null) && ((ArmorType) legs.getItemType() == ArmorType.MAGIC) && ((ArmorType) armor.getItemType() == ArmorType.MAGIC))
		{
			return true;
		}
		if ((armor != null) && ((getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST).getItem().getBodyPart() == ItemTemplate.SLOT_FULL_ARMOR) && ((ArmorType) armor.getItemType() == ArmorType.MAGIC)))
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if is wearing formal wear.
	 * @return true, if is wearing formal wear
	 */
	public boolean isWearingFormalWear()
	{
		return _isWearingFormalWear;
	}
	
	/**
	 * Sets the checks if is wearing formal wear.
	 * @param value the new checks if is wearing formal wear
	 */
	public void setWearingFormalWear(boolean value)
	{
		_isWearingFormalWear = value;
	}
	
	/**
	 * Checks if is married.
	 * @return true, if is married
	 */
	public boolean isMarried()
	{
		return _married;
	}
	
	/**
	 * Sets the married.
	 * @param value the new married
	 */
	public void setMarried(boolean value)
	{
		_married = value;
	}
	
	/**
	 * Married type.
	 * @return the int
	 */
	public int marriedType()
	{
		return _marriedType;
	}
	
	/**
	 * Sets the married type.
	 * @param type the new married type
	 */
	public void setmarriedType(int type)
	{
		_marriedType = type;
	}
	
	/**
	 * Checks if is engage request.
	 * @return true, if is engage request
	 */
	public boolean isEngageRequest()
	{
		return _engagerequest;
	}
	
	/**
	 * Sets the engage request.
	 * @param state the state
	 * @param playerid the playerid
	 */
	public void setEngageRequest(boolean state, int playerid)
	{
		_engagerequest = state;
		_engageid = playerid;
	}
	
	/**
	 * Sets the mary request.
	 * @param value the new mary request
	 */
	public void setMaryRequest(boolean value)
	{
		_marryrequest = value;
	}
	
	/**
	 * Checks if is mary request.
	 * @return true, if is mary request
	 */
	public boolean isMaryRequest()
	{
		return _marryrequest;
	}
	
	/**
	 * Sets the marry accepted.
	 * @param value the new marry accepted
	 */
	public void setMarryAccepted(boolean value)
	{
		_marryaccepted = value;
	}
	
	/**
	 * Checks if is marry accepted.
	 * @return true, if is marry accepted
	 */
	public boolean isMarryAccepted()
	{
		return _marryaccepted;
	}
	
	/**
	 * Gets the engage id.
	 * @return the engage id
	 */
	public int getEngageId()
	{
		return _engageid;
	}
	
	/**
	 * Gets the partner id.
	 * @return the partner id
	 */
	public int getPartnerId()
	{
		return _partnerId;
	}
	
	/**
	 * Sets the partner id.
	 * @param partnerid the new partner id
	 */
	public void setPartnerId(int partnerid)
	{
		_partnerId = partnerid;
	}
	
	/**
	 * Gets the couple id.
	 * @return the couple id
	 */
	public int getCoupleId()
	{
		return _coupleId;
	}
	
	/**
	 * Sets the couple id.
	 * @param coupleId the new couple id
	 */
	public void setCoupleId(int coupleId)
	{
		_coupleId = coupleId;
	}
	
	/**
	 * Engage answer.
	 * @param answer the answer
	 */
	public void engageAnswer(int answer)
	{
		if (!_engagerequest || (_engageid == 0))
		{
			return;
		}
		
		final Player ptarget = (Player) World.getInstance().findObject(_engageid);
		setEngageRequest(false, 0);
		if (ptarget != null)
		{
			if (answer == 1)
			{
				CoupleManager.getInstance().createCouple(ptarget, this);
				ptarget.sendMessage("Request to Engage has been >ACCEPTED<");
			}
			else
			{
				ptarget.sendMessage("Request to Engage has been >DENIED<!");
			}
		}
	}
	
	/**
	 * Return the secondary weapon instance (always equipped in the left hand).
	 * @return the secondary weapon instance
	 */
	@Override
	public Item getSecondaryWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
	}
	
	/**
	 * Return the secondary weapon item (always equipped in the left hand) or the fists weapon.
	 * @return the secondary weapon item
	 */
	@Override
	public Weapon getSecondaryWeaponItem()
	{
		final Item weapon = getSecondaryWeaponInstance();
		if (weapon == null)
		{
			return getFistsWeaponItem();
		}
		
		final ItemTemplate item = weapon.getItem();
		if (item instanceof Weapon)
		{
			return (Weapon) item;
		}
		
		return null;
	}
	
	/**
	 * Kill the Creature, Apply Death Penalty, Manage gain/loss Karma and Item Drop.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Reduce the Experience of the Player in function of the calculated Death Penalty</li>
	 * <li>If necessary, unsummon the Pet of the killed Player</li>
	 * <li>Manage Karma gain for attacker and Karam loss for the killed Player</li>
	 * <li>If the killed Player has Karma, manage Drop Item</li>
	 * <li>Kill the Player</li><br>
	 * @param killer the killer
	 * @return true, if successful
	 */
	@Override
	public boolean doDie(Creature killer)
	{
		// Kill the Player
		if (!super.doDie(killer))
		{
			return false;
		}
		
		Castle castle = null;
		if (getClan() != null)
		{
			castle = CastleManager.getInstance().getCastleByOwner(getClan());
			if (castle != null)
			{
				castle.destroyClanGate();
			}
		}
		
		if (killer != null)
		{
			final Player pk = killer.getActingPlayer();
			
			// Clear resurrect xp calculation
			setExpBeforeDeath(0);
			
			if (isCursedWeaponEquiped())
			{
				CursedWeaponsManager.getInstance().drop(_cursedWeaponEquipedId, killer);
			}
			else if ((pk == null) || !pk.isCursedWeaponEquiped())
			{
				onDieDropItem(killer); // Check if any item should be dropped
				if ((!isInsideZone(ZoneId.PVP) || isInsideZone(ZoneId.SIEGE)))
				{
					if ((pk != null) && (pk.getClan() != null) && (getClan() != null) && !isAcademyMember() && !pk.isAcademyMember() && _clan.isAtWarWith(pk.getClanId()) && pk.getClan().isAtWarWith(_clan.getClanId()))
					{
						if (getClan().getReputationScore() > 0)
						{
							pk.getClan().setReputationScore(((Player) killer).getClan().getReputationScore() + 2, true);
							pk.getClan().broadcastToOnlineMembers(new PledgeShowInfoUpdate(pk.getClan())); // Update status to all members
						}
						if (pk.getClan().getReputationScore() > 0)
						{
							_clan.setReputationScore(_clan.getReputationScore() - 2, true);
							_clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(_clan)); // Update status to all members
						}
					}
					if (Config.ALT_GAME_DELEVEL)
					{
						// Reduce the Experience of the Player in function of the calculated Death Penalty
						// NOTE: deathPenalty +- Exp will update karma
						if ((getSkillLevel(Skill.SKILL_LUCKY) <= 0) || (getStat().getLevel() > 9))
						{
							deathPenalty(((pk != null) && (getClan() != null) && (pk.getClan() != null) && pk.getClan().isAtWarWith(getClanId())));
						}
					}
					else
					{
						onDieUpdateKarma(); // Update karma if delevel is not allowed
					}
				}
			}
		}
		
		// Unsummon Cubics
		unsummonAllCubics();
		
		if (_forceBuff != null)
		{
			abortCast();
		}
		
		for (Creature creature : getKnownList().getKnownCharacters())
		{
			if ((creature.getTarget() == this) && creature.isCastingNow())
			{
				creature.abortCast();
			}
		}
		
		if (isInParty() && getParty().isInDimensionalRift())
		{
			getParty().getDimensionalRift().getDeadMemberList().add(this);
		}
		
		// calculate death penalty buff
		calculateDeathPenaltyBuffLevel(killer);
		
		stopRentPet();
		stopWaterTask();
		
		// leave war legend aura if enabled
		_heroConsecutiveKillCount = 0;
		if (Config.WAR_LEGEND_AURA && !_hero && _isPvpHero)
		{
			setHeroAura(false);
			sendMessage("War Legend state removed.");
		}
		
		// Refresh focus force like L2OFF
		sendPacket(new EtcStatusUpdate(this));
		return true;
	}
	
	/**
	 * On die drop item.
	 * @param killer the killer
	 */
	private void onDieDropItem(Creature killer)
	{
		if (isOnEvent())
		{
			return;
		}
		
		if ((getKarma() <= 0) && (killer instanceof Player) && (((Player) killer).getClan() != null) && (getClan() != null) && ((Player) killer).getClan().isAtWarWith(getClanId()))
		{
			return;
		}
		
		if (!isInsideZone(ZoneId.PVP) && (!isGM() || Config.KARMA_DROP_GM))
		{
			boolean isKarmaDrop = false;
			final boolean isKillerNpc = killer instanceof Npc;
			final int pkLimit = Config.KARMA_PK_LIMIT;
			int dropEquip = 0;
			int dropEquipWeapon = 0;
			int dropItem = 0;
			int dropLimit = 0;
			int dropPercent = 0;
			if ((getKarma() > 0) && (getPkKills() >= pkLimit))
			{
				isKarmaDrop = true;
				dropPercent = Config.KARMA_RATE_DROP;
				dropEquip = Config.KARMA_RATE_DROP_EQUIP;
				dropEquipWeapon = Config.KARMA_RATE_DROP_EQUIP_WEAPON;
				dropItem = Config.KARMA_RATE_DROP_ITEM;
				dropLimit = Config.KARMA_DROP_LIMIT;
			}
			else if (isKillerNpc && (getLevel() > 4) && !isFestivalParticipant())
			{
				dropPercent = Config.PLAYER_RATE_DROP;
				dropEquip = Config.PLAYER_RATE_DROP_EQUIP;
				dropEquipWeapon = Config.PLAYER_RATE_DROP_EQUIP_WEAPON;
				dropItem = Config.PLAYER_RATE_DROP_ITEM;
				dropLimit = Config.PLAYER_DROP_LIMIT;
			}
			
			int dropCount = 0;
			while ((dropPercent > 0) && (Rnd.get(100) < dropPercent) && (dropCount < dropLimit))
			{
				int itemDropPercent = 0;
				for (Item itemDrop : getInventory().getItems())
				{
					// Don't drop
					if (itemDrop.isAugmented() || // Dont drop augmented items
						itemDrop.isShadowItem() || // Dont drop Shadow Items
						(itemDrop.getItemId() == 57) || // Adena
						(itemDrop.getItem().getType2() == ItemTemplate.TYPE2_QUEST) || // Quest Items
						(itemDrop.getItem().isDropable() == false) || // Non-dropable item
						Config.KARMA_LIST_NONDROPPABLE_ITEMS.contains(itemDrop.getItemId()) || // Item listed in the non droppable item list
						Config.KARMA_LIST_NONDROPPABLE_PET_ITEMS.contains(itemDrop.getItemId()) || // Item listed in the non droppable pet item list
						((getPet() != null) && (getPet().getControlItemId() == itemDrop.getItemId() // Control Item of active pet
						)))
					{
						continue;
					}
					
					if (itemDrop.isEquipped())
					{
						// Set proper chance according to Item type of equipped Item
						itemDropPercent = itemDrop.getItem().getType2() == ItemTemplate.TYPE2_WEAPON ? dropEquipWeapon : dropEquip;
						getInventory().unEquipItemInSlotAndRecord(itemDrop.getEquipSlot());
					}
					else
					{
						itemDropPercent = dropItem; // Item in inventory
					}
					
					// NOTE: Each time an item is dropped, the chance of another item being dropped gets lesser (dropCount * 2)
					if (Rnd.get(100) < itemDropPercent)
					{
						dropItem("DieDrop", itemDrop, killer, true, !isKarmaDrop);
						dropCount++;
						break;
					}
				}
			}
		}
	}
	
	/**
	 * On die update karma.
	 */
	private void onDieUpdateKarma()
	{
		// Karma lose for server that does not allow delevel
		if (getKarma() > 0)
		{
			// this formula seems to work relatively well:
			// baseKarma * thisLVL * (thisLVL/100)
			// Calculate the new Karma of the attacker : newKarma = baseKarma*pkCountMulti*levelDiffMulti
			double karmaLost = Config.KARMA_LOST_BASE;
			karmaLost *= getLevel(); // multiply by char lvl
			karmaLost *= getLevel() / 100.0; // divide by 0.charLVL
			karmaLost = Math.round(karmaLost);
			if (karmaLost < 0)
			{
				karmaLost = 1;
			}
			
			// Decrease Karma of the Player and Send it a Server->Client StatusUpdate packet with Karma and PvP Flag if necessary
			setKarma(getKarma() - (int) karmaLost);
		}
	}
	
	/**
	 * On kill update pvp karma.
	 * @param target the target
	 */
	public void onKillUpdatePvPKarma(Creature target)
	{
		if (target == null)
		{
			return;
		}
		
		if (!(target instanceof Playable))
		{
			return;
		}
		
		if (isOnEvent())
		{
			return;
		}
		
		if (isCursedWeaponEquipped())
		{
			CursedWeaponsManager.getInstance().increaseKills(_cursedWeaponEquipedId);
			// Custom message for time left
			// CursedWeapon cw = CursedWeaponsManager.getInstance().getCursedWeapon(_cursedWeaponEquipedId);
			// SystemMessage msg = SystemMessageId.THERE_IS_S1_HOUR_AND_S2_MINUTE_LEFT_OF_THE_FIXED_USAGE_TIME);
			// int timeLeftInHours = (int)(((cw.getTimeLeft()/60000)/60));
			// msg.addItemName(_cursedWeaponEquipedId);
			// msg.addNumber(timeLeftInHours);
			// sendPacket(msg);
			return;
		}
		
		Player targetPlayer = null;
		if (target instanceof Player)
		{
			targetPlayer = (Player) target;
		}
		else if (target instanceof Summon)
		{
			targetPlayer = ((Summon) target).getOwner();
		}
		
		if (targetPlayer == null)
		{
			return; // Target player is null
		}
		
		if (targetPlayer == this)
		{
			return; // Target player is self
		}
		
		if (isCursedWeaponEquiped())
		{
			CursedWeaponsManager.getInstance().increaseKills(_cursedWeaponEquipedId);
			return;
		}
		
		// If in duel and you kill (only can kill summon), do nothing
		if (isInDuel() && targetPlayer.isInDuel())
		{
			return;
		}
		
		// If in Arena, do nothing
		if (isInsideZone(ZoneId.PVP) || targetPlayer.isInsideZone(ZoneId.PVP))
		{
			return;
		}
		
		// check anti-farm
		if (!checkAntiFarm(targetPlayer))
		{
			return;
		}
		
		if (Config.ANTI_FARM_SUMMON && (target instanceof Servitor))
		{
			return;
		}
		
		// Check if it's pvp
		if ((checkIfPvP(target) && (targetPlayer.getPvpFlag() != 0)) || (isInsideZone(ZoneId.PVP) && targetPlayer.isInsideZone(ZoneId.PVP)))
		{
			increasePvpKills();
		}
		else
		{
			// check about wars
			if ((targetPlayer.getClan() != null) && (getClan() != null) && getClan().isAtWarWith(targetPlayer.getClanId()) && targetPlayer.getClan().isAtWarWith(getClanId()))
			{
				// 'Both way war' -> 'PvP Kill'
				increasePvpKills();
				if ((target instanceof Player) && Config.ANNOUNCE_PVP_KILL)
				{
					AnnouncementsTable.getInstance().announceToAll("Player " + getName() + " hunted Player " + target.getName());
				}
				else if ((target instanceof Player) && Config.ANNOUNCE_ALL_KILL)
				{
					AnnouncementsTable.getInstance().announceToAll("Player " + getName() + " killed Player " + target.getName());
				}
				addItemReward(targetPlayer);
				return;
			}
			
			// 'No war' or 'One way war' -> 'Normal PK'
			if (!isOnEvent())
			{
				if (targetPlayer.getKarma() > 0) // Target player has karma
				{
					if (Config.KARMA_AWARD_PK_KILL)
					{
						increasePvpKills();
					}
					
					if ((target instanceof Player) && Config.ANNOUNCE_PVP_KILL)
					{
						AnnouncementsTable.getInstance().announceToAll("Player " + getName() + " hunted Player " + target.getName());
					}
				}
				else if (targetPlayer.getPvpFlag() == 0) // Target player doesn't have karma
				{
					increasePkKillsAndKarma(targetPlayer.getLevel());
					if ((target instanceof Player) && Config.ANNOUNCE_PK_KILL)
					{
						AnnouncementsTable.getInstance().announceToAll("Player " + getName() + " has assassinated Player " + target.getName());
					}
				}
			}
		}
		if ((target instanceof Player) && Config.ANNOUNCE_ALL_KILL)
		{
			AnnouncementsTable.getInstance().announceToAll("Player " + getName() + " killed Player " + target.getName());
		}
		
		if (isOnEvent())
		{
			return;
		}
		
		if (targetPlayer.getObjectId() == _lastKill)
		{
			_count += 1;
		}
		else
		{
			_count = 1;
			_lastKill = targetPlayer.getObjectId();
		}
		
		if ((Config.REWARD_PROTECT == 0) || (_count <= Config.REWARD_PROTECT))
		{
			addItemReward(targetPlayer);
		}
	}
	
	/**
	 * Check anti farm.
	 * @param targetPlayer the target player
	 * @return true, if successful
	 */
	private boolean checkAntiFarm(Player targetPlayer)
	{
		if (Config.ANTI_FARM_ENABLED)
		{
			// Anti FARM Clan - Ally
			if ((Config.ANTI_FARM_CLAN_ALLY_ENABLED && ((getClanId() > 0) && (targetPlayer.getClanId() > 0) && (getClanId() == targetPlayer.getClanId()))) || ((getAllyId() > 0) && (targetPlayer.getAllyId() > 0) && (getAllyId() == targetPlayer.getAllyId())))
			{
				sendMessage("Farm is punishable with Ban! GM informed.");
				LOGGER.info("PVP POINT FARM ATTEMPT, " + getName() + " and " + targetPlayer.getName() + ". CLAN or ALLY.");
				return false;
			}
			
			// Anti FARM level player < 40
			if (Config.ANTI_FARM_LEVEL_DIFF_ENABLED && (targetPlayer.getLevel() < Config.ANTI_FARM_MAX_LEVEL_DIFF))
			{
				sendMessage("Farm is punishable with Ban! Don't kill new players! GM informed.");
				LOGGER.info("PVP POINT FARM ATTEMPT, " + getName() + " and " + targetPlayer.getName() + ". LVL DIFF.");
				return false;
			}
			
			// Anti FARM pdef < 300
			if (Config.ANTI_FARM_PDEF_DIFF_ENABLED && (targetPlayer.getPDef(targetPlayer) < Config.ANTI_FARM_MAX_PDEF_DIFF))
			{
				sendMessage("Farm is punishable with Ban! GM informed.");
				LOGGER.info("PVP POINT FARM ATTEMPT, " + getName() + " and " + targetPlayer.getName() + ". MAX PDEF DIFF.");
				return false;
			}
			
			// Anti FARM p atk < 300
			if (Config.ANTI_FARM_PATK_DIFF_ENABLED && (targetPlayer.getPAtk(targetPlayer) < Config.ANTI_FARM_MAX_PATK_DIFF))
			{
				sendMessage("Farm is punishable with Ban! GM informed.");
				LOGGER.info("PVP POINT FARM ATTEMPT, " + getName() + " and " + targetPlayer.getName() + ". MAX PATK DIFF.");
				return false;
			}
			
			// Anti FARM Party
			if (Config.ANTI_FARM_PARTY_ENABLED && (getParty() != null) && (targetPlayer.getParty() != null) && getParty().equals(targetPlayer.getParty()))
			{
				sendMessage("Farm is punishable with Ban! GM informed.");
				LOGGER.info("PVP POINT FARM ATTEMPT, " + getName() + " and " + targetPlayer.getName() + ". SAME PARTY.");
				return false;
			}
			
			// Anti FARM same IP
			if (Config.ANTI_FARM_IP_ENABLED && (_client != null) && (targetPlayer.getClient() != null))
			{
				final String ip1 = _client.getConnectionAddress().getHostAddress();
				final String ip2 = targetPlayer.getClient().getConnectionAddress().getHostAddress();
				if (ip1.equals(ip2))
				{
					sendMessage("Farm is punishable with Ban! GM informed.");
					LOGGER.info("PVP POINT FARM ATTEMPT: " + getName() + " and " + targetPlayer.getName() + ". SAME IP.");
					return false;
				}
			}
			return true;
		}
		return true;
	}
	
	/**
	 * Adds the item reword.
	 * @param targetPlayer the target player
	 */
	private void addItemReward(Player targetPlayer)
	{
		// IP check
		if ((targetPlayer.getClient() != null) && (targetPlayer.getClient().getConnectionAddress() != null))
		{
			if (targetPlayer.getClient().getConnectionAddress() != _client.getConnectionAddress())
			{
				if ((targetPlayer.getKarma() > 0) || (targetPlayer.getPvpFlag() > 0)) // killing target pk or in pvp
				{
					if (Config.PVP_REWARD_ENABLED)
					{
						final int item = Config.PVP_REWARD_ID;
						final ItemTemplate reward = ItemTable.getInstance().getTemplate(item);
						final int amount = Config.PVP_REWARD_AMOUNT;
						getInventory().addItem("Winning PvP", Config.PVP_REWARD_ID, Config.PVP_REWARD_AMOUNT, this, null);
						sendMessage("You have earned " + amount + " item(s) of " + reward.getName() + ".");
					}
					
					if (!Config.FORCE_INVENTORY_UPDATE)
					{
						final InventoryUpdate iu = new InventoryUpdate();
						iu.addItem(_inventory.getItemByItemId(Config.PVP_REWARD_ID));
						sendPacket(iu);
					}
				}
				else // target is not pk and not in pvp ---> PK KILL
				{
					if (Config.PK_REWARD_ENABLED)
					{
						final int item = Config.PK_REWARD_ID;
						final ItemTemplate reward = ItemTable.getInstance().getTemplate(item);
						final int amount = Config.PK_REWARD_AMOUNT;
						getInventory().addItem("Winning PK", Config.PK_REWARD_ID, Config.PK_REWARD_AMOUNT, this, null);
						sendMessage("You have earned " + amount + " item(s) of " + reward.getName() + ".");
					}
					
					if (!Config.FORCE_INVENTORY_UPDATE)
					{
						final InventoryUpdate iu = new InventoryUpdate();
						iu.addItem(_inventory.getItemByItemId(Config.PK_REWARD_ID));
						sendPacket(iu);
					}
				}
			}
			else
			{
				sendMessage("Farm is punishable with Ban! Don't kill your Box!");
				LOGGER.warning("PVP POINT FARM ATTEMPT: " + getName() + " and " + targetPlayer.getName() + ". SAME IP.");
			}
		}
	}
	
	/**
	 * Increase the pvp kills count and send the info to the player.
	 */
	public void increasePvpKills()
	{
		if (isOnEvent())
		{
			return;
		}
		
		// Add karma to attacker and increase its PK counter
		setPvpKills(getPvpKills() + 1);
		
		// Increase the kill count for a special hero aura
		_heroConsecutiveKillCount++;
		
		// If heroConsecutiveKillCount == 30 give hero aura
		if ((_heroConsecutiveKillCount == Config.KILLS_TO_GET_WAR_LEGEND_AURA) && Config.WAR_LEGEND_AURA)
		{
			setHeroAura(true);
			AnnouncementsTable.getInstance().criticalAnnounceToAll(getName() + " becames War Legend with " + Config.KILLS_TO_GET_WAR_LEGEND_AURA + " PvP!!");
		}
		
		if (Config.PVPEXPSP_SYSTEM)
		{
			addExpAndSp(Config.ADD_EXP, Config.ADD_SP);
			{
				sendMessage("Earned Exp & SP for a pvp kill");
			}
		}
		
		if (Config.PVP_PK_TITLE)
		{
			updateTitle();
		}
		
		// Update the character's name color if they reached any of the 5 PvP levels.
		updatePvPColor(getPvpKills());
		broadcastUserInfo();
		
		// Send a Server->Client UserInfo packet to attacker with its Karma and PK Counter
		sendPacket(new UserInfo(this));
	}
	
	/**
	 * Increase pk count, karma and send the info to the player.
	 * @param targLVL : level of the killed player
	 */
	public void increasePkKillsAndKarma(int targLVL)
	{
		if (isOnEvent())
		{
			return;
		}
		
		final int baseKarma = Config.KARMA_MIN_KARMA;
		int newKarma = baseKarma;
		final int karmaLimit = Config.KARMA_MAX_KARMA;
		final int pkLVL = getLevel();
		final int pkPKCount = getPkKills();
		int levelDiffMulti = 0;
		int pkCountMulti = 0;
		
		// Check if the attacker has a PK counter greater than 0
		if (pkPKCount > 0)
		{
			pkCountMulti = pkPKCount / 2;
		}
		else
		{
			pkCountMulti = 1;
		}
		
		if (pkCountMulti < 1)
		{
			pkCountMulti = 1;
		}
		
		// Calculate the level difference Multiplier between attacker and killed Player
		if (pkLVL > targLVL)
		{
			levelDiffMulti = pkLVL / targLVL;
		}
		else
		{
			levelDiffMulti = 1;
		}
		
		if (levelDiffMulti < 1)
		{
			levelDiffMulti = 1;
		}
		
		// Calculate the new Karma of the attacker : newKarma = baseKarma*pkCountMulti*levelDiffMulti
		newKarma *= pkCountMulti;
		newKarma *= levelDiffMulti;
		
		// Make sure newKarma is less than karmaLimit and higher than baseKarma
		if (newKarma < baseKarma)
		{
			newKarma = baseKarma;
		}
		
		if (newKarma > karmaLimit)
		{
			newKarma = karmaLimit;
		}
		
		// Fix to prevent overflow (=> karma has a max value of 2 147 483 647)
		if (getKarma() > (Integer.MAX_VALUE - newKarma))
		{
			newKarma = Integer.MAX_VALUE - getKarma();
		}
		
		// Add karma to attacker and increase its PK counter
		setPkKills(getPkKills() + 1);
		
		if (Config.PVP_PK_TITLE)
		{
			updateTitle();
		}
		
		// Update the character's title color if they reached any of the 5 PK levels.
		updatePkColor(getPkKills());
		broadcastUserInfo();
		
		// Send a Server->Client UserInfo packet to attacker with its Karma and PK Counter
		sendPacket(new UserInfo(this));
	}
	
	/**
	 * Calculate karma lost.
	 * @param exp the exp
	 * @return the int
	 */
	public int calculateKarmaLost(long exp)
	{
		// KARMA LOSS
		// When a PKer gets killed by another player or a Monster, it loses a certain amount of Karma based on their level.
		// this (with defaults) results in a level 1 losing about ~2 karma per death, and a lvl 70 loses about 11760 karma per death...
		// You lose karma as long as you were not in a pvp zone and you did not kill urself.
		// NOTE: exp for death (if delevel is allowed) is based on the players level
		long expGained = Math.abs(exp);
		expGained /= Config.KARMA_XP_DIVIDER;
		int karmaLost = 0;
		if (expGained > Integer.MAX_VALUE)
		{
			karmaLost = Integer.MAX_VALUE;
		}
		else
		{
			karmaLost = (int) expGained;
		}
		
		if (karmaLost < Config.KARMA_LOST_BASE)
		{
			karmaLost = Config.KARMA_LOST_BASE;
		}
		if (karmaLost > getKarma())
		{
			karmaLost = getKarma();
		}
		
		return karmaLost;
	}
	
	/**
	 * Update pvp status.
	 */
	public void updatePvPStatus()
	{
		if (isOnEvent())
		{
			return;
		}
		
		if (isInsideZone(ZoneId.PVP))
		{
			return;
		}
		
		setPvpFlagLasts(Chronos.currentTimeMillis() + Config.PVP_NORMAL_TIME);
		if (getPvpFlag() == 0)
		{
			startPvPFlag();
		}
	}
	
	/**
	 * Update pvp status.
	 * @param target the target
	 */
	public void updatePvPStatus(Creature target)
	{
		Player targetPlayer = null;
		if (target instanceof Player)
		{
			targetPlayer = (Player) target;
		}
		else if (target instanceof Summon)
		{
			targetPlayer = ((Summon) target).getOwner();
		}
		
		if (targetPlayer == null)
		{
			return;
		}
		
		if (isOnEvent())
		{
			return;
		}
		
		if ((!isInsideZone(ZoneId.PVP) || !targetPlayer.isInsideZone(ZoneId.PVP)) && (targetPlayer.getKarma() == 0))
		{
			if (checkIfPvP(targetPlayer))
			{
				setPvpFlagLasts(Chronos.currentTimeMillis() + Config.PVP_PVP_TIME);
			}
			else
			{
				setPvpFlagLasts(Chronos.currentTimeMillis() + Config.PVP_NORMAL_TIME);
			}
			if (getPvpFlag() == 0)
			{
				startPvPFlag();
			}
		}
	}
	
	/**
	 * Restore the specified % of experience this Player has lost and sends a Server->Client StatusUpdate packet.
	 * @param restorePercent the restore percent
	 */
	public void restoreExp(double restorePercent)
	{
		if (getExpBeforeDeath() > 0)
		{
			// Restore the specified % of lost experience.
			getStat().addExp((int) Math.round(((getExpBeforeDeath() - getExp()) * restorePercent) / 100));
			setExpBeforeDeath(0);
		}
	}
	
	/**
	 * Reduce the Experience (and level if necessary) of the Player in function of the calculated Death Penalty.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Calculate the Experience loss</li>
	 * <li>Set the value of _expBeforeDeath</li>
	 * <li>Set the new Experience value of the Player and Decrease its level if necessary</li>
	 * <li>Send a Server->Client StatusUpdate packet with its new Experience</li><br>
	 * @param atwar the atwar
	 */
	public void deathPenalty(boolean atwar)
	{
		// Get the level of the Player
		final int lvl = getLevel();
		
		// The death steal you some Exp
		double percentLost = 4.0; // standart 4% (lvl>20)
		if (getLevel() < 20)
		{
			percentLost = 10.0;
		}
		else if ((getLevel() >= 20) && (getLevel() < 40))
		{
			percentLost = 7.0;
		}
		else if ((getLevel() >= 40) && (getLevel() < 75))
		{
			percentLost = 4.0;
		}
		else if ((getLevel() >= 75) && (getLevel() < 81))
		{
			percentLost = 2.0;
		}
		
		if (getKarma() > 0)
		{
			percentLost *= Config.RATE_KARMA_EXP_LOST;
		}
		
		if (isFestivalParticipant() || atwar || isInsideZone(ZoneId.SIEGE))
		{
			percentLost /= 4.0;
		}
		
		// Calculate the Experience loss
		long lostExp = 0;
		if (!isOnEvent())
		{
			final byte maxLevel = ExperienceData.getInstance().getMaxLevel();
			if (lvl < maxLevel)
			{
				lostExp = Math.round(((getStat().getExpForLevel(lvl + 1) - getStat().getExpForLevel(lvl)) * percentLost) / 100);
			}
			else
			{
				lostExp = Math.round(((getStat().getExpForLevel(maxLevel) - getStat().getExpForLevel(maxLevel - 1)) * percentLost) / 100);
			}
		}
		// Get the Experience before applying penalty
		setExpBeforeDeath(getExp());
		
		if (getCharmOfCourage())
		{
			if ((getSiegeState() > 0) && isInsideZone(ZoneId.SIEGE))
			{
				lostExp = 0;
			}
			setCharmOfCourage(false);
		}
		
		// Set the new Experience value of the Player
		getStat().addExp(-lostExp);
	}
	
	/**
	 * Manage the increase level task of a Player (Max MP, Max MP, Recommandation, Expertise and beginner skills...).<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Send a Server->Client System Message to the Player : YOU_INCREASED_YOUR_LEVEL</li>
	 * <li>Send a Server->Client packet StatusUpdate to the Player with new LEVEL, MAX_HP and MAX_MP</li>
	 * <li>Set the current HP and MP of the Player, Launch/Stop a HP/MP/CP Regeneration Task and send StatusUpdate packet to all other Player to inform (exclusive broadcast)</li>
	 * <li>Recalculate the party level</li>
	 * <li>Recalculate the number of Recommandation that the Player can give</li>
	 * <li>Give Expertise skill of this level and remove beginner Lucky skill</li>
	 */
	public void increaseLevel()
	{
		// Set the current HP and MP of the Creature, Launch/Stop a HP/MP/CP Regeneration Task and send StatusUpdate packet to all other Player to inform (exclusive broadcast)
		setCurrentHpMp(getMaxHp(), getMaxMp());
		setCurrentCp(getMaxCp());
	}
	
	/**
	 * Stop the HP/MP/CP Regeneration task.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Set the RegenActive flag to False</li>
	 * <li>Stop the HP/MP/CP Regeneration task</li>
	 */
	public void stopAllTimers()
	{
		stopHpMpRegeneration();
		stopWarnUserTakeBreak();
		stopWaterTask();
		stopRentPet();
		stopPvpRegTask();
		stopPunishTask(true);
	}
	
	/**
	 * Return the Summon of the Player or null.
	 * @return the pet
	 */
	@Override
	public Summon getPet()
	{
		return _summon;
	}
	
	/**
	 * Set the Summon of the Player.
	 * @param summon the new pet
	 */
	public void setPet(Summon summon)
	{
		_summon = summon;
	}
	
	/**
	 * Return the Summon of the Player or null.
	 * @return the trained beast
	 */
	public TamedBeast getTrainedBeast()
	{
		return _tamedBeast;
	}
	
	/**
	 * Set the Summon of the Player.
	 * @param tamedBeast the new trained beast
	 */
	public void setTrainedBeast(TamedBeast tamedBeast)
	{
		_tamedBeast = tamedBeast;
	}
	
	/**
	 * Return the Player requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).
	 * @return the request
	 */
	public Request getRequest()
	{
		return _request;
	}
	
	/**
	 * Set the Player requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).
	 * @param requester the new active requester
	 */
	public synchronized void setActiveRequester(Player requester)
	{
		_activeRequester = requester;
	}
	
	/**
	 * Return the Player requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).
	 * @return the active requester
	 */
	public synchronized Player getActiveRequester()
	{
		final Player requester = _activeRequester;
		if ((requester != null) && requester.isRequestExpired() && (_activeTradeList == null))
		{
			_activeRequester = null;
		}
		return _activeRequester;
	}
	
	/**
	 * Return True if a transaction is in progress.
	 * @return true, if is processing request
	 */
	public boolean isProcessingRequest()
	{
		return (_activeRequester != null) || (_requestExpireTime > GameTimeTaskManager.getGameTicks());
	}
	
	/**
	 * Return True if a transaction is in progress.
	 * @return true, if is processing transaction
	 */
	public boolean isProcessingTransaction()
	{
		return (_activeRequester != null) || (_activeTradeList != null) || (_requestExpireTime > GameTimeTaskManager.getGameTicks());
	}
	
	/**
	 * Select the Warehouse to be used in next activity.
	 * @param partner the partner
	 */
	public void onTransactionRequest(Player partner)
	{
		_requestExpireTime = GameTimeTaskManager.getGameTicks() + (REQUEST_TIMEOUT * GameTimeTaskManager.TICKS_PER_SECOND);
		if (partner != null)
		{
			partner.setActiveRequester(this);
		}
	}
	
	/**
	 * Select the Warehouse to be used in next activity.
	 */
	public void onTransactionResponse()
	{
		_requestExpireTime = 0;
	}
	
	/**
	 * Select the Warehouse to be used in next activity.
	 * @param warehouse the new active warehouse
	 */
	public void setActiveWarehouse(ItemContainer warehouse)
	{
		_activeWarehouse = warehouse;
	}
	
	/**
	 * Return active Warehouse.
	 * @return the active warehouse
	 */
	public ItemContainer getActiveWarehouse()
	{
		return _activeWarehouse;
	}
	
	/**
	 * Select the TradeList to be used in next activity.
	 * @param tradeList the new active trade list
	 */
	public void setActiveTradeList(TradeList tradeList)
	{
		_activeTradeList = tradeList;
	}
	
	/**
	 * Return active TradeList.
	 * @return the active trade list
	 */
	public TradeList getActiveTradeList()
	{
		return _activeTradeList;
	}
	
	/**
	 * On trade start.
	 * @param partner the partner
	 */
	public void onTradeStart(Player partner)
	{
		_activeTradeList = new TradeList(this);
		_activeTradeList.setPartner(partner);
		
		final SystemMessage msg = new SystemMessage(SystemMessageId.YOU_BEGIN_TRADING_WITH_S1);
		msg.addString(partner.getName());
		sendPacket(msg);
		sendPacket(new TradeStart(this));
	}
	
	/**
	 * On trade confirm.
	 * @param partner the partner
	 */
	public void onTradeConfirm(Player partner)
	{
		final SystemMessage msg = new SystemMessage(SystemMessageId.S1_HAS_CONFIRMED_THE_TRADE);
		msg.addString(partner.getName());
		sendPacket(msg);
		partner.sendPacket(TradePressOwnOk.STATIC_PACKET);
		sendPacket(TradePressOtherOk.STATIC_PACKET);
	}
	
	/**
	 * On trade cancel.
	 * @param partner the partner
	 */
	public void onTradeCancel(Player partner)
	{
		if (_activeTradeList == null)
		{
			return;
		}
		
		_activeTradeList.lock();
		_activeTradeList = null;
		sendPacket(new SendTradeDone(0));
		final SystemMessage msg = new SystemMessage(SystemMessageId.S1_HAS_CANCELED_THE_TRADE);
		msg.addString(partner.getName());
		sendPacket(msg);
	}
	
	/**
	 * On trade finish.
	 * @param successfull the successfull
	 */
	public void onTradeFinish(boolean successfull)
	{
		_activeTradeList = null;
		sendPacket(new SendTradeDone(1));
		if (successfull)
		{
			sendPacket(SystemMessageId.YOUR_TRADE_IS_SUCCESSFUL);
		}
	}
	
	/**
	 * Start trade.
	 * @param partner the partner
	 */
	public void startTrade(Player partner)
	{
		onTradeStart(partner);
		partner.onTradeStart(this);
	}
	
	/**
	 * Cancel active trade.
	 */
	public void cancelActiveTrade()
	{
		if (_activeTradeList == null)
		{
			return;
		}
		
		final Player partner = _activeTradeList.getPartner();
		if (partner != null)
		{
			partner.onTradeCancel(this);
		}
		onTradeCancel(this);
	}
	
	/**
	 * Return the _createList object of the Player.
	 * @return the creates the list
	 */
	public ManufactureList getCreateList()
	{
		return _createList;
	}
	
	/**
	 * Set the _createList object of the Player.
	 * @param x the new creates the list
	 */
	public void setCreateList(ManufactureList x)
	{
		_createList = x;
	}
	
	/**
	 * Return the _sellList object of the Player.
	 * @return the sell list
	 */
	public TradeList getSellList()
	{
		if (_sellList == null)
		{
			_sellList = new TradeList(this);
		}
		return _sellList;
	}
	
	/**
	 * Return the _buyList object of the Player.
	 * @return the buy list
	 */
	public TradeList getBuyList()
	{
		if (_buyList == null)
		{
			_buyList = new TradeList(this);
		}
		return _buyList;
	}
	
	/**
	 * Set the Private Store type of the Player.<br>
	 * <br>
	 * <b><u>Values</u>:</b>
	 * <li>0 : STORE_PRIVATE_NONE</li>
	 * <li>1 : STORE_PRIVATE_SELL</li>
	 * <li>2 : sellmanage</li>
	 * <li>3 : STORE_PRIVATE_BUY</li>
	 * <li>4 : buymanage</li>
	 * <li>5 : STORE_PRIVATE_MANUFACTURE</li><br>
	 * @param type the new private store type
	 */
	public void setPrivateStoreType(int type)
	{
		_privatestore = type;
		if ((_privatestore == STORE_PRIVATE_NONE) && ((_client == null) || isInOfflineMode()))
		{
			store();
			if (Config.OFFLINE_DISCONNECT_FINISHED)
			{
				World.OFFLINE_TRADE_COUNT--;
				deleteMe();
				
				if (_client != null)
				{
					_client.setPlayer(null); // prevent deleteMe from being called a second time on disconnection
				}
			}
		}
	}
	
	/**
	 * Return the Private Store type of the Player.<br>
	 * <br>
	 * <b><u>Values</u>:</b>
	 * <li>0 : STORE_PRIVATE_NONE</li>
	 * <li>1 : STORE_PRIVATE_SELL</li>
	 * <li>2 : sellmanage</li>
	 * <li>3 : STORE_PRIVATE_BUY</li>
	 * <li>4 : buymanage</li>
	 * <li>5 : STORE_PRIVATE_MANUFACTURE</li>
	 * @return the private store type
	 */
	public int getPrivateStoreType()
	{
		return _privatestore;
	}
	
	/**
	 * Set the _skillLearningClassId object of the Player.
	 * @param classId the new skill learning class id
	 */
	public void setSkillLearningClassId(ClassId classId)
	{
		_skillLearningClassId = classId;
	}
	
	/**
	 * Return the _skillLearningClassId object of the Player.
	 * @return the skill learning class id
	 */
	public ClassId getSkillLearningClassId()
	{
		return _skillLearningClassId;
	}
	
	/**
	 * Set the _clan object, _clanId, _clanLeader Flag and title of the Player.
	 * @param clan the new clan
	 */
	public void setClan(Clan clan)
	{
		_clan = clan;
		setTitle("");
		
		if (clan == null)
		{
			_clanId = 0;
			_clanPrivileges = 0;
			_pledgeType = 0;
			_powerGrade = 0;
			_lvlJoinedAcademy = 0;
			_apprentice = 0;
			_sponsor = 0;
			return;
		}
		
		if (!clan.isMember(getName()))
		{
			// char has been kicked from clan
			setClan(null);
			return;
		}
		
		_clanId = clan.getClanId();
		
		// Add clan leader skills if clanleader
		addClanLeaderSkills(isClanLeader() && (clan.getLevel() >= 4));
	}
	
	/**
	 * Return the _clan object of the Player.
	 * @return the clan
	 */
	public Clan getClan()
	{
		return _clan;
	}
	
	/**
	 * Return True if the Player is the leader of its clan.
	 * @return true, if is clan leader
	 */
	public boolean isClanLeader()
	{
		if (getClan() == null)
		{
			return false;
		}
		return getObjectId() == getClan().getLeaderId();
	}
	
	/**
	 * Reduce the number of arrows owned by the Player and send it Server->Client Packet InventoryUpdate or ItemList (to unequip if the last arrow was consummed).
	 */
	@Override
	protected void reduceArrowCount()
	{
		final Item arrows = getInventory().destroyItem("Consume", getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND), 1, this, null);
		if ((arrows == null) || (arrows.getCount() == 0))
		{
			getInventory().unEquipItemInSlot(Inventory.PAPERDOLL_LHAND);
			_arrowItem = null;
			sendPacket(new ItemList(this, false));
		}
		else if (!Config.FORCE_INVENTORY_UPDATE)
		{
			final InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(arrows);
			sendPacket(iu);
		}
		else
		{
			sendPacket(new ItemList(this, false));
		}
	}
	
	/**
	 * Equip arrows needed in left hand and send a Server->Client packet ItemList to the Player then return True.
	 * @return true, if successful
	 */
	@Override
	protected boolean checkAndEquipArrows()
	{
		// Check if nothing is equipped in left hand
		if ((getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) == null) //
			// Mobius: Fix for pickup/equip arrows on dual weapons.
			&& (getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND).getItemType() == WeaponType.BOW))
		{
			// Get the Item of the arrows needed for this bow
			_arrowItem = getInventory().findArrowForBow(getActiveWeaponItem());
			if (_arrowItem != null)
			{
				// Equip arrows needed in left hand
				getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, _arrowItem);
				
				// Send a Server->Client packet ItemList to this Player to update left hand equipement
				sendPacket(new ItemList(this, false));
			}
		}
		else
		{
			// Get the Item of arrows equipped in left hand
			_arrowItem = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		}
		return _arrowItem != null;
	}
	
	/**
	 * Disarm the player's weapon and shield.
	 * @return true, if successful
	 */
	public boolean disarmWeapons()
	{
		// Don't allow disarming a cursed weapon
		if (isCursedWeaponEquiped() && !getAccessLevel().isGm())
		{
			return false;
		}
		
		// Unequip the weapon
		Item wpn = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		if (wpn == null)
		{
			wpn = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND);
		}
		
		if (wpn != null)
		{
			if (wpn.isWear())
			{
				return false;
			}
			
			// Remove augementation boni on unequip
			if (wpn.isAugmented())
			{
				wpn.getAugmentation().removeBonus(this);
			}
			
			final List<Item> unequipped = getInventory().unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart());
			final InventoryUpdate iu = new InventoryUpdate();
			for (Item element : unequipped)
			{
				iu.addModifiedItem(element);
			}
			sendPacket(iu);
			
			abortAttack();
			broadcastUserInfo();
			
			// this can be 0 if the user pressed the right mousebutton twice very fast
			if (!unequipped.isEmpty())
			{
				SystemMessage sm = null;
				final Item unequippedItem = unequipped.get(0);
				if (unequippedItem.getEnchantLevel() > 0)
				{
					sm = new SystemMessage(SystemMessageId.THE_EQUIPMENT_S1_S2_HAS_BEEN_REMOVED);
					sm.addNumber(unequippedItem.getEnchantLevel());
					sm.addItemName(unequippedItem.getItemId());
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.S1_HAS_BEEN_DISARMED);
					sm.addItemName(unequippedItem.getItemId());
				}
				sendPacket(sm);
			}
		}
		
		// Unequip the shield
		final Item sld = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if (sld != null)
		{
			if (sld.isWear())
			{
				return false;
			}
			
			final List<Item> unequipped = getInventory().unEquipItemInBodySlotAndRecord(sld.getItem().getBodyPart());
			final InventoryUpdate iu = new InventoryUpdate();
			for (Item element : unequipped)
			{
				iu.addModifiedItem(element);
			}
			sendPacket(iu);
			
			abortAttack();
			broadcastUserInfo();
			
			// this can be 0 if the user pressed the right mousebutton twice very fast
			if (!unequipped.isEmpty())
			{
				SystemMessage sm = null;
				final Item item = unequipped.get(0);
				if (item.getEnchantLevel() > 0)
				{
					sm = new SystemMessage(SystemMessageId.THE_EQUIPMENT_S1_S2_HAS_BEEN_REMOVED);
					sm.addNumber(item.getEnchantLevel());
					sm.addItemName(item.getItemId());
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.S1_HAS_BEEN_DISARMED);
					sm.addItemName(item.getItemId());
				}
				sendPacket(sm);
			}
		}
		return true;
	}
	
	/**
	 * Return True if the Player use a dual weapon.
	 * @return true, if is using dual weapon
	 */
	@Override
	public boolean isUsingDualWeapon()
	{
		final Weapon weaponItem = getActiveWeaponItem();
		if (weaponItem == null)
		{
			return false;
		}
		
		if (weaponItem.getItemType() == WeaponType.DUAL)
		{
			return true;
		}
		else if (weaponItem.getItemType() == WeaponType.DUALFIST)
		{
			return true;
		}
		else if (weaponItem.getItemId() == 248)
		{
			return true;
		}
		else if (weaponItem.getItemId() == 252)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Sets the uptime.
	 * @param time the new uptime
	 */
	public void setUptime(long time)
	{
		_uptime = time;
	}
	
	/**
	 * Gets the uptime.
	 * @return the uptime
	 */
	public long getUptime()
	{
		return Chronos.currentTimeMillis() - _uptime;
	}
	
	/**
	 * Return True if the Player is invulnerable.
	 * @return true, if is invul
	 */
	@Override
	public boolean isInvul()
	{
		return _isInvul || _isTeleporting || (_protectEndTime > GameTimeTaskManager.getGameTicks()) || (_teleportProtectEndTime > GameTimeTaskManager.getGameTicks());
	}
	
	/**
	 * Return True if the Player has a Party in progress.
	 * @return true, if is in party
	 */
	@Override
	public boolean isInParty()
	{
		return _party != null;
	}
	
	/**
	 * Set the _party object of the Player (without joining it).
	 * @param party the new party
	 */
	public void setParty(Party party)
	{
		_party = party;
	}
	
	/**
	 * Set the _party object of the Player AND join it.
	 * @param party the party
	 */
	public void joinParty(Party party)
	{
		if (party == null)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (party.getMemberCount() == 9)
		{
			sendPacket(SystemMessageId.THE_PARTY_IS_FULL);
			return;
		}
		
		if (party.getPartyMembers().contains(this))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (party.getMemberCount() < 9)
		{
			// First set the party otherwise this wouldn't be considered as in a party into the Creature.updateEffectIcons() call.
			_party = party;
			party.addPartyMember(this);
		}
	}
	
	/**
	 * Return true if the Player is a GM.
	 * @return true, if is GM
	 */
	public boolean isGM()
	{
		return getAccessLevel().isGm();
	}
	
	/**
	 * Manage the Leave Party task of the Player.
	 */
	public void leaveParty()
	{
		if (isInParty())
		{
			_party.removePartyMember(this);
			_party = null;
		}
	}
	
	/**
	 * Return the _party object of the Player.
	 * @return the party
	 */
	@Override
	public Party getParty()
	{
		return _party;
	}
	
	public void setFirstLog(int firstLog)
	{
		_firstLogin = false;
		if (firstLog == 1)
		{
			_firstLogin = true;
		}
	}
	
	/**
	 * Sets the first LOGGER.
	 * @param firstLog the new first LOGGER
	 */
	public void setFirstLog(boolean firstLog)
	{
		_firstLogin = firstLog;
	}
	
	/**
	 * Gets the first LOGGER.
	 * @return the first LOGGER
	 */
	public boolean getFirstLog()
	{
		return _firstLogin;
	}
	
	/**
	 * Manage a cancel cast task for the Player.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Set the Intention of the AI to AI_INTENTION_IDLE</li>
	 * <li>Enable all skills (set _allSkillsDisabled to False)</li>
	 * <li>Send a Server->Client Packet MagicSkillCanceld to the Player and all Player in the _KnownPlayers of the Creature (broadcast)</li>
	 */
	public void cancelCastMagic()
	{
		// Set the Intention of the AI to AI_INTENTION_IDLE
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		
		// Enable all skills (set _allSkillsDisabled to False)
		enableAllSkills();
		
		// Send a Server->Client Packet MagicSkillCanceld to the Player and all Player in the _KnownPlayers of the Creature (broadcast)
		
		// Broadcast the packet to self and known players.
		Broadcast.toSelfAndKnownPlayersInRadius(this, new MagicSkillCanceld(getObjectId()), 810000/* 900 */);
	}
	
	/**
	 * Set the _accessLevel of the Player.
	 * @param level the new access level
	 */
	public void setAccessLevel(int level)
	{
		if (level > 0)
		{
			LOGGER.warning("Access level " + level + " set for character " + getName() + "! Just a warning ;)");
		}
		
		final AccessLevel accessLevel = AdminData.getInstance().getAccessLevel(level);
		if (accessLevel == null)
		{
			if (level < 0)
			{
				AdminData.getInstance().addBanAccessLevel(level);
				_accessLevel = AdminData.getInstance().getAccessLevel(level);
			}
			else
			{
				LOGGER.warning("Tried to set unregistered access level " + level + " to character " + getName() + ". Setting access level without privileges!");
				_accessLevel = AdminData.getInstance().getAccessLevel(0);
			}
		}
		else
		{
			_accessLevel = accessLevel;
		}
		
		if (_accessLevel.isGm())
		{
			if (_accessLevel.useNameColor())
			{
				getAppearance().setNameColor(_accessLevel.getNameColor());
			}
			if (_accessLevel.useTitleColor())
			{
				getAppearance().setTitleColor(_accessLevel.getTitleColor());
			}
			broadcastUserInfo();
		}
	}
	
	/**
	 * Sets the account accesslevel.
	 * @param level the new account accesslevel
	 */
	public void setAccountAccesslevel(int level)
	{
		LoginServerThread.getInstance().sendAccessLevel(getAccountName(), level);
	}
	
	/**
	 * Return the _accessLevel of the Player.
	 * @return the access level
	 */
	public AccessLevel getAccessLevel()
	{
		if (Config.EVERYBODY_HAS_ADMIN_RIGHTS)
		{
			return AdminData.getInstance().getAccessLevel(100);
		}
		else if (_accessLevel == null)
		{
			setAccessLevel(0);
		}
		return _accessLevel;
	}
	
	@Override
	public double getLevelMod()
	{
		return ((100.0 - 11) + getLevel()) / 100.0;
	}
	
	/**
	 * Update Stats of the Player client side by sending Server->Client packet UserInfo/StatusUpdate to this Player and CharInfo/StatusUpdate to all Player in its _KnownPlayers (broadcast).
	 * @param broadcastType the broadcast type
	 */
	public void updateAndBroadcastStatus(int broadcastType)
	{
		refreshOverloaded();
		refreshExpertisePenalty();
		// Send a Server->Client packet UserInfo to this Player and CharInfo to all Player in its _KnownPlayers (broadcast)
		if (broadcastType == 1)
		{
			sendPacket(new UserInfo(this));
		}
		
		if (broadcastType == 2)
		{
			broadcastUserInfo();
		}
	}
	
	/**
	 * Send a Server->Client StatusUpdate packet with Karma and PvP Flag to the Player and all Player to inform (broadcast).
	 * @param flag the new karma flag
	 */
	public void setKarmaFlag(int flag)
	{
		sendPacket(new UserInfo(this));
		for (Player player : getKnownList().getKnownPlayers().values())
		{
			player.sendPacket(new RelationChanged(this, getRelation(player), isAutoAttackable(player)));
			if (getPet() != null)
			{
				getPet().broadcastPacket(new NpcInfo(getPet(), null));
			}
		}
	}
	
	/**
	 * Send a Server->Client StatusUpdate packet with Karma to the Player and all Player to inform (broadcast).
	 */
	public void broadcastKarma()
	{
		sendPacket(new UserInfo(this));
		for (Player player : getKnownList().getKnownPlayers().values())
		{
			if (player == null)
			{
				continue;
			}
			
			player.sendPacket(new RelationChanged(this, getRelation(player), isAutoAttackable(player)));
			if (getPet() != null)
			{
				getPet().broadcastPacket(new NpcInfo(getPet(), null));
			}
		}
	}
	
	/**
	 * Set the online Flag to True or False and update the characters table of the database with online status and lastAccess (called when login and logout).
	 * @param isOnline the new online status
	 */
	public void setOnlineStatus(boolean isOnline)
	{
		if (_isOnline != isOnline)
		{
			_isOnline = isOnline;
		}
		
		// Update the characters table of the database with online status and lastAccess (called when login and logout)
		updateOnlineStatus();
	}
	
	/**
	 * Sets the checks if is in7s dungeon.
	 * @param isIn7sDungeon the new checks if is in7s dungeon
	 */
	public void setIn7sDungeon(boolean isIn7sDungeon)
	{
		if (_isIn7sDungeon != isIn7sDungeon)
		{
			_isIn7sDungeon = isIn7sDungeon;
		}
		
		updateIsIn7sDungeonStatus();
	}
	
	/**
	 * Update the characters table of the database with online status and lastAccess of this Player (called when login and logout).
	 */
	public void updateOnlineStatus()
	{
		if (isInOfflineMode())
		{
			return;
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("UPDATE characters SET online=?, lastAccess=? WHERE charId=?");
			statement.setInt(1, isOnline() ? 1 : 0);
			statement.setLong(2, Chronos.currentTimeMillis());
			statement.setInt(3, getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("could not set char online status:" + e);
		}
	}
	
	/**
	 * Update is in7s dungeon status.
	 */
	public void updateIsIn7sDungeonStatus()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("UPDATE characters SET isIn7sDungeon=?, lastAccess=? WHERE charId=?");
			statement.setInt(1, isIn7sDungeon() ? 1 : 0);
			statement.setLong(2, Chronos.currentTimeMillis());
			statement.setInt(3, getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("could not set char isIn7sDungeon status:" + e);
		}
	}
	
	/**
	 * Update first LOGGER.
	 */
	public void updateFirstLog()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("UPDATE characters SET first_log=? WHERE charId=?");
			int fl;
			if (getFirstLog())
			{
				fl = 1;
			}
			else
			{
				fl = 0;
			}
			statement.setInt(1, fl);
			statement.setInt(2, getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("could not set char first login:" + e);
		}
	}
	
	/**
	 * Create a new player in the characters table of the database.
	 * @return true, if successful
	 */
	private boolean createDb()
	{
		boolean output = false;
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			statement = con.prepareStatement("INSERT INTO characters (account_name,charId,char_name,level,maxHp,curHp,maxCp,curCp,maxMp,curMp,acc,crit,evasion,mAtk,mDef,mSpd,pAtk,pDef,pSpd,runSpd,walkSpd,str,con,dex,_int,men,wit,face,hairStyle,hairColor,sex,movement_multiplier,attack_speed_multiplier,colRad,colHeight,exp,sp,karma,pvpkills,pkkills,clanid,maxload,race,classid,deletetime,cancraft,title,accesslevel,online,isin7sdungeon,clan_privs,wantspeace,base_class,newbie,nobless,power_grade,last_recom_date,name_color,title_color,aio,aio_end) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setString(1, _accountName);
			statement.setInt(2, getObjectId());
			statement.setString(3, getName());
			statement.setInt(4, getLevel());
			statement.setInt(5, getMaxHp());
			statement.setDouble(6, getCurrentHp());
			statement.setInt(7, getMaxCp());
			statement.setDouble(8, getCurrentCp());
			statement.setInt(9, getMaxMp());
			statement.setDouble(10, getCurrentMp());
			statement.setInt(11, getAccuracy());
			statement.setInt(12, getCriticalHit(null, null));
			statement.setInt(13, getEvasionRate(null));
			statement.setInt(14, getMAtk(null, null));
			statement.setInt(15, getMDef(null, null));
			statement.setInt(16, getMAtkSpd());
			statement.setInt(17, getPAtk(null));
			statement.setInt(18, getPDef(null));
			statement.setInt(19, getPAtkSpd());
			statement.setInt(20, getRunSpeed());
			statement.setInt(21, getWalkSpeed());
			statement.setInt(22, getSTR());
			statement.setInt(23, getCON());
			statement.setInt(24, getDEX());
			statement.setInt(25, getINT());
			statement.setInt(26, getMEN());
			statement.setInt(27, getWIT());
			statement.setInt(28, getAppearance().getFace());
			statement.setInt(29, getAppearance().getHairStyle());
			statement.setInt(30, getAppearance().getHairColor());
			statement.setInt(31, getAppearance().isFemale() ? 1 : 0);
			statement.setDouble(32, 1 /* getMovementMultiplier() */);
			statement.setDouble(33, 1 /* getAttackSpeedMultiplier() */);
			statement.setDouble(34, getCollisionRadius());
			statement.setDouble(35, getCollisionHeight());
			statement.setLong(36, getExp());
			statement.setInt(37, getSp());
			statement.setInt(38, getKarma());
			statement.setInt(39, getPvpKills());
			statement.setInt(40, getPkKills());
			statement.setInt(41, getClanId());
			statement.setInt(42, getMaxLoad());
			statement.setInt(43, getRace().ordinal());
			statement.setInt(44, getClassId().getId());
			statement.setLong(45, getDeleteTimer());
			statement.setInt(46, hasDwarvenCraft() ? 1 : 0);
			statement.setString(47, getTitle());
			statement.setInt(48, getAccessLevel().getLevel());
			statement.setInt(49, isOnline() ? 1 : 0);
			statement.setInt(50, isIn7sDungeon() ? 1 : 0);
			statement.setInt(51, getClanPrivileges());
			statement.setInt(52, getWantsPeace());
			statement.setInt(53, getBaseClass());
			statement.setInt(54, isNewbie() ? 1 : 0);
			statement.setInt(55, isNoble() ? 1 : 0);
			statement.setLong(56, 0);
			statement.setLong(57, Chronos.currentTimeMillis());
			statement.setString(58, StringToHex(Integer.toHexString(getAppearance().getNameColor()).toUpperCase()));
			statement.setString(59, StringToHex(Integer.toHexString(getAppearance().getTitleColor()).toUpperCase()));
			statement.setInt(60, isAio() ? 1 : 0);
			statement.setLong(61, 0);
			statement.executeUpdate();
			statement.close();
			
			output = true;
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not insert char data " + e);
		}
		return output;
	}
	
	/**
	 * Retrieve a Player from the characters table of the database and add it in _allObjects of the World.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Retrieve the Player from the characters table of the database</li>
	 * <li>Add the Player object in _allObjects</li>
	 * <li>Set the x,y,z position of the Player and make it invisible</li>
	 * <li>Update the overloaded status of the Player</li><br>
	 * @param objectId Identifier of the object to initialized
	 * @return The Player loaded from the database
	 */
	private static Player restore(int objectId)
	{
		Player player = null;
		double curHp = 0;
		double curCp = 0;
		double curMp = 0;
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement(RESTORE_CHARACTER);
			statement.setInt(1, objectId);
			final ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				final int activeClassId = rset.getInt("classid");
				final boolean female = rset.getInt("sex") != 0;
				final PlayerTemplate template = PlayerTemplateData.getInstance().getTemplate(activeClassId);
				final PlayerAppearance app = new PlayerAppearance(rset.getByte("face"), rset.getByte("hairColor"), rset.getByte("hairStyle"), female);
				player = new Player(objectId, template, rset.getString("account_name"), app);
				player.setName(rset.getString("char_name"));
				player._lastAccess = rset.getLong("lastAccess");
				player.getStat().setExp(rset.getLong("exp"));
				player.setExpBeforeDeath(rset.getLong("expBeforeDeath"));
				player.getStat().setLevel(rset.getByte("level"));
				player.getStat().setSp(rset.getInt("sp"));
				
				player.setWantsPeace(rset.getInt("wantspeace"));
				
				player.setHeading(rset.getInt("heading"));
				
				player.setKarma(rset.getInt("karma"));
				player.setPvpKills(rset.getInt("pvpkills"));
				player.setPkKills(rset.getInt("pkkills"));
				player.setOnlineTime(rset.getLong("onlinetime"));
				player.setNewbie(rset.getInt("newbie") == 1);
				player.setNoble(rset.getInt("nobless") == 1);
				player.setClanJoinExpiryTime(rset.getLong("clan_join_expiry_time"));
				player.setFirstLog(rset.getInt("first_log"));
				player._pcBangPoints = rset.getInt("pc_point");
				if (player.getClanJoinExpiryTime() < Chronos.currentTimeMillis())
				{
					player.setClanJoinExpiryTime(0);
				}
				player.setClanCreateExpiryTime(rset.getLong("clan_create_expiry_time"));
				if (player.getClanCreateExpiryTime() < Chronos.currentTimeMillis())
				{
					player.setClanCreateExpiryTime(0);
				}
				
				final int clanId = rset.getInt("clanid");
				player.setPowerGrade((int) rset.getLong("power_grade"));
				player.setPledgeType(rset.getInt("subpledge"));
				player.setLastRecomUpdate(rset.getLong("last_recom_date"));
				
				if (clanId > 0)
				{
					player.setClan(ClanTable.getInstance().getClan(clanId));
				}
				
				if (player.getClan() != null)
				{
					if (player.getClan().getLeaderId() != player.getObjectId())
					{
						if (player.getPowerGrade() == 0)
						{
							player.setPowerGrade(5);
						}
						player.setClanPrivileges(player.getClan().getRankPrivs(player.getPowerGrade()));
					}
					else
					{
						player.setClanPrivileges(Clan.CP_ALL);
						player.setPowerGrade(1);
					}
				}
				else
				{
					player.setClanPrivileges(Clan.CP_NOTHING);
				}
				
				player.setDeleteTimer(rset.getLong("deletetime"));
				
				player.setTitle(rset.getString("title"));
				player.setFistsWeaponItem(player.findFistsWeaponItem(activeClassId));
				player.setUptime(Chronos.currentTimeMillis());
				
				curHp = rset.getDouble("curHp");
				curCp = rset.getDouble("curCp");
				curMp = rset.getDouble("curMp");
				
				// Check recs
				player.checkRecom(rset.getInt("rec_have"), rset.getInt("rec_left"));
				player._classIndex = 0;
				try
				{
					player.setBaseClass(rset.getInt("base_class"));
				}
				catch (Exception e)
				{
					player.setBaseClass(activeClassId);
				}
				
				// Restore Subclass Data (cannot be done earlier in function)
				if (restoreSubClassData(player) && (activeClassId != player.getBaseClass()))
				{
					for (SubClass subClass : player.getSubClasses().values())
					{
						if (subClass.getClassId() == activeClassId)
						{
							player._classIndex = subClass.getClassIndex();
						}
					}
				}
				if ((player.getClassIndex() == 0) && (activeClassId != player.getBaseClass()))
				{
					// Subclass in use but doesn't exist in DB - a possible restart-while-modifysubclass cheat has been attempted.
					// Switching to use base class
					player.setClassId(player.getBaseClass());
					LOGGER.warning("Player " + player.getName() + " reverted to base class. Possibly has tried a relogin exploit while subclassing.");
				}
				else
				{
					player._activeClass = activeClassId;
				}
				
				player.setApprentice(rset.getInt("apprentice"));
				player.setSponsor(rset.getInt("sponsor"));
				player.setLvlJoinedAcademy(rset.getInt("lvl_joined_academy"));
				player.setIn7sDungeon(rset.getInt("isin7sdungeon") == 1);
				player.setPunishLevel(rset.getInt("punish_level"));
				if (player.getPunishLevel() != PunishLevel.NONE)
				{
					player.setPunishTimer(rset.getLong("punish_timer"));
				}
				else
				{
					player.setPunishTimer(0);
				}
				
				try
				{
					player.getAppearance().setNameColor(Integer.decode(new StringBuilder().append("0x").append(rset.getString("name_color")).toString()).intValue());
					player.getAppearance().setTitleColor(Integer.decode(new StringBuilder().append("0x").append(rset.getString("title_color")).toString()).intValue());
				}
				catch (Exception e)
				{
					// leave them as default
				}
				
				player.setAccessLevel(rset.getInt("accesslevel"));
				
				CursedWeaponsManager.getInstance().checkPlayer(player);
				
				player.setAllianceWithVarkaKetra(rset.getInt("varka_ketra_ally"));
				
				player.setDeathPenaltyBuffLevel(rset.getInt("death_penalty_level"));
				player.setAio(rset.getInt("aio") == 1);
				player.setAioEndTime(rset.getLong("aio_end"));
				
				// Set the x,y,z position of the Player and make it invisible
				final int x = rset.getInt("x");
				final int y = rset.getInt("y");
				final int z = rset.getInt("z");
				player.setXYZInvisible(x, y, z);
				player.setLastServerPosition(x, y, z);
				
				// Retrieve the name and ID of the other characters assigned to this account.
				final PreparedStatement stmt = con.prepareStatement("SELECT charId, char_name FROM characters WHERE account_name=? AND charId<>?");
				stmt.setString(1, player._accountName);
				stmt.setInt(2, objectId);
				final ResultSet chars = stmt.executeQuery();
				
				while (chars.next())
				{
					final Integer charId = chars.getInt("charId");
					final String charName = chars.getString("char_name");
					player._chars.put(charId, charName);
				}
				
				chars.close();
				stmt.close();
				break;
			}
			
			rset.close();
			statement.close();
			
			if (player == null)
			{
				return null;
			}
			
			// Retrieve from the database all secondary data of this Player and reward expertise/lucky skills if necessary.
			// Note that Clan, Noblesse and Hero skills are given separately and not here.
			player.restoreCharData();
			// reward skill restore mode in order to avoid duplicate storage of already stored skills
			player.rewardSkills(true);
			
			// Restore pet if exists in the world
			player.setPet(World.getInstance().getPet(player.getObjectId()));
			if (player.getPet() != null)
			{
				player.getPet().setOwner(player);
			}
			
			// Update the overloaded status of the Player
			player.refreshOverloaded();
			
			player.restoreFriendList();
			
			PlayerAutoSaveTaskManager.getInstance().add(player);
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not restore char data " + e);
		}
		
		if (player != null)
		{
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				LOGGER.warning(e.toString());
			}
			
			// once restored all the skill status, update current CP, MP and HP
			player.setCurrentHpDirect(curHp);
			player.setCurrentCpDirect(curCp);
			player.setCurrentMpDirect(curMp);
		}
		return player;
	}
	
	/**
	 * Gets the mail.
	 * @return the mail
	 */
	public Forum getMail()
	{
		if (_forumMail == null)
		{
			setMail(ForumsBBSManager.getInstance().getForumByName("MailRoot").getChildByName(getName()));
			
			if (_forumMail == null)
			{
				ForumsBBSManager.getInstance().createNewForum(getName(), ForumsBBSManager.getInstance().getForumByName("MailRoot"), Forum.MAIL, Forum.OWNERONLY, getObjectId());
				setMail(ForumsBBSManager.getInstance().getForumByName("MailRoot").getChildByName(getName()));
			}
		}
		return _forumMail;
	}
	
	/**
	 * Sets the mail.
	 * @param forum the new mail
	 */
	public void setMail(Forum forum)
	{
		_forumMail = forum;
	}
	
	/**
	 * Gets the memo.
	 * @return the memo
	 */
	public Forum getMemo()
	{
		if (_forumMemo == null)
		{
			setMemo(ForumsBBSManager.getInstance().getForumByName("MemoRoot").getChildByName(_accountName));
			
			if (_forumMemo == null)
			{
				ForumsBBSManager.getInstance().createNewForum(_accountName, ForumsBBSManager.getInstance().getForumByName("MemoRoot"), Forum.MEMO, Forum.OWNERONLY, getObjectId());
				setMemo(ForumsBBSManager.getInstance().getForumByName("MemoRoot").getChildByName(_accountName));
			}
		}
		return _forumMemo;
	}
	
	/**
	 * Sets the memo.
	 * @param forum the new memo
	 */
	public void setMemo(Forum forum)
	{
		_forumMemo = forum;
	}
	
	/**
	 * Restores sub-class data for the Player, used to check the current class index for the character.
	 * @param player the player
	 * @return true, if successful
	 */
	private static boolean restoreSubClassData(Player player)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement(RESTORE_CHAR_SUBCLASSES);
			statement.setInt(1, player.getObjectId());
			
			final ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				final SubClass subClass = new SubClass();
				subClass.setClassId(rset.getInt("class_id"));
				subClass.setLevel(rset.getByte("level"));
				subClass.setExp(rset.getLong("exp"));
				subClass.setSp(rset.getInt("sp"));
				subClass.setClassIndex(rset.getInt("class_index"));
				
				// Enforce the correct indexing of _subClasses against their class indexes.
				player.getSubClasses().put(subClass.getClassIndex(), subClass);
			}
			
			statement.close();
			rset.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not restore classes for " + player.getName() + ": " + e);
		}
		
		return true;
	}
	
	/**
	 * Restores secondary data for the Player, based on the current class index.
	 */
	private void restoreCharData()
	{
		// Retrieve from the database all skills of this Player and add them to _skills.
		restoreSkills();
		
		// Retrieve from the database all macroses of this Player and add them to _macroses.
		_macroses.restore();
		
		// Retrieve from the database all shortCuts of this Player and add them to _shortCuts.
		_shortCuts.restore();
		
		// Retrieve from the database all henna of this Player and add them to _henna.
		restoreHenna();
		
		// Retrieve from the database all recom data of this Player and add to _recomChars.
		if (Config.ALT_RECOMMEND)
		{
			restoreRecom();
		}
		
		// Retrieve from the database the recipe book of this Player.
		if (!isSubClassActive())
		{
			restoreRecipeBook();
		}
	}
	
	public void saveRecipeIntoDB(RecipeList recipe)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement insertRecipe = con.prepareStatement(INSERT_CHARACTER_RECIPEBOOK))
		{
			insertRecipe.setInt(1, getObjectId());
			insertRecipe.setInt(2, recipe.getId());
			insertRecipe.setBoolean(3, recipe.isDwarvenRecipe()); // 0 = Normal recipe, 1 Dwarven recipe
			insertRecipe.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.warning("Player.saveRecipeIntoDB : Could not store recipe book data " + e);
		}
	}
	
	/**
	 * Restore recipe book data for this Player.
	 */
	private void restoreRecipeBook()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT id, type FROM character_recipebook WHERE char_id=?");
			statement.setInt(1, getObjectId());
			final ResultSet rset = statement.executeQuery();
			RecipeList recipe;
			while (rset.next())
			{
				recipe = RecipeData.getInstance().getRecipe(rset.getInt("id"));
				if (rset.getInt("type") == 1)
				{
					registerDwarvenRecipeList(recipe);
				}
				else
				{
					registerCommonRecipeList(recipe);
				}
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not restore recipe book data:" + e);
		}
	}
	
	/**
	 * @param <T>
	 * @param script
	 * @return
	 */
	public <T> T addScript(T script)
	{
		_scripts.put(script.getClass().getName(), script);
		return script;
	}
	
	/**
	 * @param <T>
	 * @param script
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T removeScript(Class<T> script)
	{
		return (T) _scripts.remove(script.getName());
	}
	
	/**
	 * @param <T>
	 * @param script
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getScript(Class<T> script)
	{
		return (T) _scripts.get(script.getName());
	}
	
	/**
	 * @return {@code true} if {@link PlayerVariables} instance is attached to current player's scripts, {@code false} otherwise.
	 */
	public boolean hasVariables()
	{
		return getScript(PlayerVariables.class) != null;
	}
	
	/**
	 * @return {@link PlayerVariables} instance containing parameters regarding player.
	 */
	public PlayerVariables getVariables()
	{
		final PlayerVariables vars = getScript(PlayerVariables.class);
		return vars != null ? vars : addScript(new PlayerVariables(getObjectId()));
	}
	
	/**
	 * @return {@code true} if {@link AccountVariables} instance is attached to current player's scripts, {@code false} otherwise.
	 */
	public boolean hasAccountVariables()
	{
		return getScript(AccountVariables.class) != null;
	}
	
	/**
	 * @return {@link AccountVariables} instance containing parameters regarding player.
	 */
	public AccountVariables getAccountVariables()
	{
		final AccountVariables vars = getScript(AccountVariables.class);
		return vars != null ? vars : addScript(new AccountVariables(getAccountName()));
	}
	
	/**
	 * Store.
	 * @param force the force
	 */
	public synchronized void store(boolean force)
	{
		// update client coords, if these look like true
		if (!force && isInsideRadius2D(getClientX(), getClientY(), getClientZ(), 1000))
		{
			setXYZ(getClientX(), getClientY(), getClientZ());
		}
		
		storeCharBase();
		storeCharSub();
		
		// Dont store effect if the char was on Offline trade
		if (!isStored())
		{
			storeEffect();
		}
		
		final PlayerVariables vars = getScript(PlayerVariables.class);
		if (vars != null)
		{
			vars.storeMe();
		}
		
		final AccountVariables aVars = getScript(AccountVariables.class);
		if (aVars != null)
		{
			aVars.storeMe();
		}
		
		getInventory().updateDatabase();
		getWarehouse().updateDatabase();
		getFreight().updateDatabase();
		
		// If char is in Offline trade, setStored must be true
		setStored(isInOfflineMode());
	}
	
	/**
	 * Update Player stats in the characters table of the database.
	 */
	public synchronized void store()
	{
		store(false);
	}
	
	/**
	 * Store char base.
	 */
	private synchronized void storeCharBase()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			// Get the exp, level, and sp of base class to store in base table
			final int currentClassIndex = getClassIndex();
			_classIndex = 0;
			final long exp = getStat().getExp();
			final int level = getStat().getLevel();
			final int sp = getStat().getSp();
			_classIndex = currentClassIndex;
			PreparedStatement statement;
			
			// Update base class
			statement = con.prepareStatement(UPDATE_CHARACTER);
			statement.setInt(1, level);
			statement.setInt(2, getMaxHp());
			statement.setDouble(3, getCurrentHp());
			statement.setInt(4, getMaxCp());
			statement.setDouble(5, getCurrentCp());
			statement.setInt(6, getMaxMp());
			statement.setDouble(7, getCurrentMp());
			statement.setInt(8, getSTR());
			statement.setInt(9, getCON());
			statement.setInt(10, getDEX());
			statement.setInt(11, getINT());
			statement.setInt(12, getMEN());
			statement.setInt(13, getWIT());
			statement.setInt(14, getAppearance().getFace());
			statement.setInt(15, getAppearance().getHairStyle());
			statement.setInt(16, getAppearance().getHairColor());
			statement.setInt(17, getHeading());
			statement.setInt(18, _observerMode ? _obsX : getX());
			statement.setInt(19, _observerMode ? _obsY : getY());
			statement.setInt(20, _observerMode ? _obsZ : getZ());
			statement.setLong(21, exp);
			statement.setLong(22, getExpBeforeDeath());
			statement.setInt(23, sp);
			statement.setInt(24, getKarma());
			statement.setInt(25, getPvpKills());
			statement.setInt(26, getPkKills());
			statement.setInt(27, getRecomHave());
			statement.setInt(28, getRecomLeft());
			statement.setInt(29, getClanId());
			statement.setInt(30, getMaxLoad());
			statement.setInt(31, getRace().ordinal());
			statement.setInt(32, getClassId().getId());
			statement.setLong(33, getDeleteTimer());
			statement.setString(34, getTitle());
			statement.setInt(35, getAccessLevel().getLevel());
			if (_isInOfflineMode || isOnline()) // in offline mode or online
			{
				statement.setInt(36, 1);
			}
			else
			{
				statement.setInt(36, isOnline() ? 1 : 0);
			}
			
			statement.setInt(37, isIn7sDungeon() ? 1 : 0);
			statement.setInt(38, getClanPrivileges());
			statement.setInt(39, getWantsPeace());
			statement.setInt(40, getBaseClass());
			long totalOnlineTime = _onlineTime;
			if (_onlineBeginTime > 0)
			{
				totalOnlineTime += (Chronos.currentTimeMillis() - _onlineBeginTime) / 1000;
			}
			statement.setLong(41, _offlineShopStart > 0 ? _onlineTime : totalOnlineTime);
			statement.setInt(42, getPunishLevel().value());
			statement.setLong(43, getPunishTimer());
			statement.setInt(44, isNewbie() ? 1 : 0);
			statement.setInt(45, isNoble() ? 1 : 0);
			statement.setLong(46, getPowerGrade());
			statement.setInt(47, getPledgeType());
			statement.setLong(48, getLastRecomUpdate());
			statement.setInt(49, getLvlJoinedAcademy());
			statement.setLong(50, getApprentice());
			statement.setLong(51, getSponsor());
			statement.setInt(52, getAllianceWithVarkaKetra());
			statement.setLong(53, getClanJoinExpiryTime());
			statement.setLong(54, getClanCreateExpiryTime());
			statement.setString(55, getName());
			statement.setLong(56, getDeathPenaltyBuffLevel());
			statement.setInt(57, getPcBangScore());
			statement.setString(58, StringToHex(Integer.toHexString(_originalNameColorOffline).toUpperCase()));
			statement.setString(59, StringToHex(Integer.toHexString(getAppearance().getTitleColor()).toUpperCase()));
			statement.setInt(60, isAio() ? 1 : 0);
			statement.setLong(61, getAioEndTime());
			statement.setInt(62, getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not store char base data: " + e);
		}
	}
	
	/**
	 * Store char sub.
	 */
	private synchronized void storeCharSub()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			if (getTotalSubClasses() > 0)
			{
				for (SubClass subClass : getSubClasses().values())
				{
					statement = con.prepareStatement(UPDATE_CHAR_SUBCLASS);
					statement.setLong(1, subClass.getExp());
					statement.setInt(2, subClass.getSp());
					statement.setInt(3, subClass.getLevel());
					statement.setInt(4, subClass.getClassId());
					statement.setInt(5, getObjectId());
					statement.setInt(6, subClass.getClassIndex());
					statement.execute();
					statement.close();
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not store sub class data for " + getName() + ": " + e);
		}
	}
	
	private synchronized void storeEffect()
	{
		if (!Config.STORE_SKILL_COOLTIME)
		{
			return;
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			// Delete all current stored effects for char to avoid dupe
			statement = con.prepareStatement(DELETE_SKILL_SAVE);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			statement.execute();
			statement.close();
			
			// Store all effect data along with calulated remaining reuse delays for matching skills. 'restore_type'= 0.
			final Collection<Effect> effects = getAllEffects();
			statement = con.prepareStatement(ADD_SKILL_SAVE);
			int buffIndex = 0;
			final List<Integer> storedSkills = new ArrayList<>();
			final long currentTime = Chronos.currentTimeMillis();
			for (Effect effect : effects)
			{
				final int skillId = effect.getSkill().getId();
				if (storedSkills.contains(skillId))
				{
					continue;
				}
				storedSkills.add(skillId);
				
				if (effect.getInUse() && !effect.getSkill().isToggle() && !effect.getStackType().equals("BattleForce") && !effect.getStackType().equals("SpellForce") && (effect.getSkill().getSkillType() != SkillType.FORCE_BUFF))
				{
					statement.setInt(1, getObjectId());
					statement.setInt(2, skillId);
					statement.setInt(3, effect.getSkill().getLevel());
					statement.setInt(4, effect.getCount());
					statement.setInt(5, effect.getTime());
					if (_reuseTimestamps.containsKey(effect.getSkill().getId()))
					{
						final Timestamp t = _reuseTimestamps.get(effect.getSkill().getId());
						statement.setLong(6, currentTime < t.getStamp() ? t.getReuse() : 0);
						statement.setLong(7, currentTime < t.getStamp() ? t.getStamp() : 0);
					}
					else
					{
						statement.setLong(6, 0);
						statement.setLong(7, 0);
					}
					statement.setInt(8, 0);
					statement.setInt(9, getClassIndex());
					statement.setInt(10, ++buffIndex);
					statement.execute();
				}
			}
			// Store the reuse delays of remaining skills which lost effect but still under reuse delay. 'restore_type' 1.
			for (Timestamp t : _reuseTimestamps.values())
			{
				if (currentTime < t.getStamp())
				{
					final int skillId = t.getSkillId();
					final int skillLevel = t.getSkillLevel();
					if (storedSkills.contains(skillId))
					{
						continue;
					}
					storedSkills.add(skillId);
					
					statement.setInt(1, getObjectId());
					statement.setInt(2, skillId);
					statement.setInt(3, skillLevel);
					statement.setInt(4, -1);
					statement.setInt(5, -1);
					statement.setLong(6, t.getReuse());
					statement.setLong(7, t.getStamp());
					statement.setInt(8, 1);
					statement.setInt(9, getClassIndex());
					statement.setInt(10, ++buffIndex);
					statement.execute();
				}
			}
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not store char effect data: " + e);
		}
	}
	
	/**
	 * Return True if the Player is online.
	 * @return if player is online
	 */
	public boolean isOnline()
	{
		return _isOnline;
	}
	
	/**
	 * Checks if is in7s dungeon.
	 * @return true, if is in7s dungeon
	 */
	public boolean isIn7sDungeon()
	{
		return _isIn7sDungeon;
	}
	
	/**
	 * Adds the skill.
	 * @param newSkill the new skill
	 * @param store the store
	 * @return the skill
	 */
	public synchronized Skill addSkill(Skill newSkill, boolean store)
	{
		_learningSkill = true;
		// Add a skill to the Player _skills and its Func objects to the calculator set of the Player
		final Skill oldSkill = super.addSkill(newSkill);
		
		// Add or update a Player skill in the character_skills table of the database
		if (store)
		{
			storeSkill(newSkill, oldSkill, -1);
		}
		
		_learningSkill = false;
		return oldSkill;
	}
	
	/**
	 * Checks if is learning skill.
	 * @return true, if is learning skill
	 */
	public boolean isLearningSkill()
	{
		return _learningSkill;
	}
	
	/**
	 * Removes the skill.
	 * @param skill the skill
	 * @param store the store
	 * @return the skill
	 */
	public Skill removeSkill(Skill skill, boolean store)
	{
		if (store)
		{
			return removeSkill(skill);
		}
		return super.removeSkill(skill);
	}
	
	/**
	 * Remove a skill from the Creature and its Func objects from calculator set of the Creature and save update in the character_skills table of the database.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * All skills own by a Creature are identified in <b>_skills</b><br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Remove the skill from the Creature _skills</li>
	 * <li>Remove all its Func objects from the Creature calculator set</li><br>
	 * <br>
	 * <b><u>Overriden in</u>:</b><br>
	 * <br>
	 * <li>Player : Save update in the character_skills table of the database</li><br>
	 * @param skill The Skill to remove from the Creature
	 * @return The Skill removed
	 */
	@Override
	public synchronized Skill removeSkill(Skill skill)
	{
		// Remove a skill from the Creature and its Func objects from calculator set of the Creature
		final Skill oldSkill = super.removeSkill(skill);
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			if (oldSkill != null)
			{
				statement = con.prepareStatement(DELETE_SKILL_FROM_CHAR);
				statement.setInt(1, oldSkill.getId());
				statement.setInt(2, getObjectId());
				statement.setInt(3, getClassIndex());
				statement.execute();
				statement.close();
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("Error could not delete skill: " + e);
		}
		
		for (ShortCut sc : getAllShortCuts())
		{
			if ((sc != null) && (skill != null) && (sc.getId() == skill.getId()) && (sc.getType() == ShortCut.TYPE_SKILL))
			{
				deleteShortCut(sc.getSlot(), sc.getPage());
			}
		}
		
		return oldSkill;
	}
	
	/**
	 * Add or update a Player skill in the character_skills table of the database.<br>
	 * If newClassIndex > -1, the skill will be stored with that class index, not the current one.
	 * @param newSkill the new skill
	 * @param oldSkill the old skill
	 * @param newClassIndex the new class index
	 */
	private void storeSkill(Skill newSkill, Skill oldSkill, int newClassIndex)
	{
		int classIndex = _classIndex;
		if (newClassIndex > -1)
		{
			classIndex = newClassIndex;
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement = null;
			if ((oldSkill != null) && (newSkill != null))
			{
				statement = con.prepareStatement(UPDATE_CHARACTER_SKILL_LEVEL);
				statement.setInt(1, newSkill.getLevel());
				statement.setInt(2, oldSkill.getId());
				statement.setInt(3, getObjectId());
				statement.setInt(4, classIndex);
				statement.execute();
				statement.close();
			}
			else if (newSkill != null)
			{
				statement = con.prepareStatement(ADD_NEW_SKILL);
				statement.setInt(1, getObjectId());
				statement.setInt(2, newSkill.getId());
				statement.setInt(3, newSkill.getLevel());
				statement.setString(4, newSkill.getName());
				statement.setInt(5, classIndex);
				statement.execute();
				statement.close();
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("Error could not store char skills: " + e);
		}
	}
	
	/**
	 * Check player skills and remove unlegit ones (excludes hero, noblesse and cursed weapon skills).
	 */
	public void checkAllowedSkills()
	{
		// Exclude GM characters.
		if (isGM())
		{
			return;
		}
		
		final Collection<SkillLearn> skillTree = SkillTreeTable.getInstance().getAllowedSkills(getClassId());
		boolean foundskill;
		
		// Loop through all player skills.
		for (Skill skill : getAllSkills())
		{
			final int skillId = skill.getId();
			foundskill = false;
			
			// Loop through all skills in player skilltree.
			for (SkillLearn temp : skillTree)
			{
				if (temp.getId() == skillId)
				{
					foundskill = true;
				}
			}
			
			if (!foundskill)
			{
				// Exclude noble skills.
				if (isNoble() && (((skillId >= 325) && (skillId <= 397)) || ((skillId >= 1323) && (skillId <= 1327))))
				{
					foundskill = true;
				}
				// Exclude hero skills.
				else if (isHero() && (((skillId >= 395) && (skillId <= 396)) || ((skillId >= 1374) && (skillId <= 1376))))
				{
					foundskill = true;
				}
				// Exclude cursed weapon skills.
				else if (isCursedWeaponEquiped() && (skillId == CursedWeaponsManager.getInstance().getCursedWeapon(_cursedWeaponEquipedId).getSkillId()))
				{
					foundskill = true;
				}
				// Exclude clan skills. Exclude seal of ruler / build siege HQ.
				else if ((getClan() != null) && (((skillId >= 370) && (skillId <= 391)) || ((getClan().getLeaderId() == getObjectId()) && ((skillId == 246) || (skillId == 247)))))
				{
					foundskill = true;
				}
				// Exclude fishing skills and common skills + dwarfen craft.
				else if (((skillId >= 1312) && (skillId <= 1322)) || ((skillId >= 1368) && (skillId <= 1373)))
				{
					foundskill = true;
				}
				// Exclude AS / enchant bonus / penalty etc skills.
				else if ((skillId >= 3000) && (skillId < 7000))
				{
					foundskill = true;
				}
				// Exclude Skills from AllowedSkills in custom/Other.ini
				else if (Config.ALLOWED_SKILLS_LIST.contains(skillId))
				{
					foundskill = true;
				}
				// Exclude Donator character.
				else if (isDonator())
				{
					foundskill = true;
				}
				// Exclude Aio character.
				else if (isAio())
				{
					foundskill = true;
				}
			}
			
			// Remove skill.
			if (!foundskill)
			{
				removeSkill(skill);
			}
		}
		
		// Update skill list.
		sendSkillList();
	}
	
	/**
	 * Retrieve from the database all skills of this Player and add them to _skills.
	 */
	public synchronized void restoreSkills()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			if (!Config.KEEP_SUBCLASS_SKILLS)
			{
				final PreparedStatement statement = con.prepareStatement(RESTORE_SKILLS_FOR_CHAR);
				statement.setInt(1, getObjectId());
				statement.setInt(2, getClassIndex());
				final ResultSet rset = statement.executeQuery();
				
				// Go though the recordset of this SQL query
				while (rset.next())
				{
					final int id = rset.getInt("skill_id");
					final int level = rset.getInt("skill_level");
					if (id > 9000)
					{
						continue; // fake skills for base stats
					}
					
					// Create a Skill object for each record
					final Skill skill = SkillTable.getInstance().getSkill(id, level);
					
					// Add the Skill object to the Creature _skills and its Func objects to the calculator set of the Creature
					super.addSkill(skill);
				}
				
				rset.close();
				statement.close();
			}
			else
			{
				final PreparedStatement statement = con.prepareStatement(RESTORE_SKILLS_FOR_CHAR_ALT_SUBCLASS);
				statement.setInt(1, getObjectId());
				final ResultSet rset = statement.executeQuery();
				
				// Go though the recordset of this SQL query
				while (rset.next())
				{
					final int id = rset.getInt("skill_id");
					final int level = rset.getInt("skill_level");
					if (id > 9000)
					{
						continue; // fake skills for base stats
					}
					
					// Create a Skill object for each record
					final Skill skill = SkillTable.getInstance().getSkill(id, level);
					
					// Add the Skill object to the Creature _skills and its Func objects to the calculator set of the Creature
					super.addSkill(skill);
				}
				
				rset.close();
				statement.close();
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not restore character skills: " + e);
		}
	}
	
	public void restoreEffects()
	{
		restoreEffects(true);
	}
	
	/**
	 * Retrieve from the database all skill effects of this Player and add them to the player.
	 * @param activateEffects
	 */
	public void restoreEffects(boolean activateEffects)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			ResultSet rset;
			final long currentTime = Chronos.currentTimeMillis();
			
			// Restore Type 0 These skill were still in effect on the character upon logout. Some of which were self casted and might still have had a long reuse delay which also is restored.
			statement = con.prepareStatement(RESTORE_SKILL_SAVE);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			statement.setInt(3, 0);
			rset = statement.executeQuery();
			
			while (rset.next())
			{
				final int skillId = rset.getInt("skill_id");
				final int skillLevel = rset.getInt("skill_level");
				final int effectCount = rset.getInt("effect_count");
				final int effectCurTime = rset.getInt("effect_cur_time");
				final long reuseDelay = rset.getLong("reuse_delay");
				final long systime = rset.getLong("systime");
				
				// Just incase the admin minipulated this table incorrectly :x
				if ((skillId == -1) || (effectCount == -1) || (effectCurTime == -1) || (reuseDelay < 0))
				{
					continue;
				}
				
				if (activateEffects)
				{
					final Skill skill = SkillTable.getInstance().getSkill(skillId, skillLevel);
					skill.applyEffects(this, this, false, false, false);
					for (Effect effect : getAllEffects())
					{
						if (effect.getSkill().getId() == skillId)
						{
							effect.setCount(effectCount);
							effect.setFirstTime(effectCurTime);
						}
					}
				}
				
				final long remainingTime = systime - currentTime;
				if (remainingTime > 10)
				{
					final Skill skill = SkillTable.getInstance().getSkill(skillId, skillLevel);
					if (skill == null)
					{
						continue;
					}
					
					disableSkill(skill, remainingTime);
					addTimestamp(new Timestamp(skill, reuseDelay, systime));
				}
			}
			rset.close();
			statement.close();
			
			// Restore Type 1 The remaning skills lost effect upon logout but were still under a high reuse delay.
			statement = con.prepareStatement(RESTORE_SKILL_SAVE);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			statement.setInt(3, 1);
			rset = statement.executeQuery();
			
			while (rset.next())
			{
				final int skillId = rset.getInt("skill_id");
				final int skillLevel = rset.getInt("skill_level");
				final long reuseDelay = rset.getLong("reuse_delay");
				final long systime = rset.getLong("systime");
				final long remainingTime = systime - currentTime;
				if (remainingTime > 0)
				{
					final Skill skill = SkillTable.getInstance().getSkill(skillId, skillLevel);
					if (skill == null)
					{
						continue;
					}
					
					disableSkill(skill, remainingTime);
					addTimestamp(new Timestamp(skill, reuseDelay, systime));
				}
			}
			rset.close();
			statement.close();
			
			statement = con.prepareStatement(DELETE_SKILL_SAVE);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not restore active effect data: " + e);
		}
		
		updateEffectIcons();
	}
	
	/**
	 * Retrieve from the database all Recommendation data of this Player, add to _recomChars and calculate stats of the Player.
	 */
	private void restoreRecom()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement(RESTORE_CHAR_RECOMS);
			statement.setInt(1, getObjectId());
			final ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				_recomChars.add(rset.getInt("target_id"));
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("could not restore recommendations: " + e);
		}
	}
	
	/**
	 * Retrieve from the database all Henna of this Player, add them to _henna and calculate stats of the Player.
	 */
	private void restoreHenna()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement = con.prepareStatement(RESTORE_CHAR_HENNAS);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			ResultSet rset = statement.executeQuery();
			for (int i = 0; i < 3; i++)
			{
				_henna[i] = null;
			}
			
			while (rset.next())
			{
				int slot = rset.getInt("slot");
				if ((slot < 1) || (slot > 3))
				{
					continue;
				}
				
				int symbolId = rset.getInt("symbol_id");
				if (symbolId != 0)
				{
					Henna tpl = HennaData.getInstance().getHenna(symbolId);
					if (tpl != null)
					{
						_henna[slot - 1] = tpl;
					}
				}
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not restore henna: " + e);
		}
		
		// Calculate Henna modifiers of this Player
		recalcHennaStats();
	}
	
	/**
	 * @return the number of Henna empty slot of the Player.
	 */
	public int getHennaEmptySlots()
	{
		int totalSlots = 0;
		if (getClassId().level() == 1)
		{
			totalSlots = 2;
		}
		else
		{
			totalSlots = 3;
		}
		
		for (int i = 0; i < 3; i++)
		{
			if (_henna[i] != null)
			{
				totalSlots--;
			}
		}
		
		if (totalSlots <= 0)
		{
			return 0;
		}
		
		return totalSlots;
	}
	
	/**
	 * Remove a Henna of the Player, save update in the character_hennas table of the database and send HennaInfo/UserInfo packet to this Player.
	 * @param slot The slot number to make checks on.
	 * @return true if successful.
	 */
	public boolean removeHenna(int slot)
	{
		if ((slot < 1) || (slot > 3))
		{
			return false;
		}
		
		if (_henna[slot - 1] == null)
		{
			return false;
		}
		
		Henna henna = _henna[slot - 1];
		_henna[slot - 1] = null;
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement = con.prepareStatement(DELETE_CHAR_HENNA);
			statement.setInt(1, getObjectId());
			statement.setInt(2, slot);
			statement.setInt(3, getClassIndex());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not remove char henna: " + e);
		}
		
		// Calculate Henna modifiers of this Player
		recalcHennaStats();
		
		// Send HennaInfo packet to this Player
		sendPacket(new HennaInfo(this));
		
		// Send UserInfo packet to this Player
		sendPacket(new UserInfo(this));
		reduceAdena("Henna", henna.getPrice() / 5, this, false);
		
		// Add the recovered dyes to the player's inventory and notify them.
		addItem("Henna", henna.getDyeId(), Henna.getRequiredDyeAmount() / 2, this, true);
		sendPacket(SystemMessageId.THE_SYMBOL_HAS_BEEN_DELETED);
		return true;
	}
	
	/**
	 * Add a Henna to the Player, save update in the character_hennas table of the database and send Server->Client HennaInfo/UserInfo packet to this Player.
	 * @param henna The Henna template to add.
	 */
	public void addHenna(Henna henna)
	{
		for (int i = 0; i < 3; i++)
		{
			if (_henna[i] == null)
			{
				_henna[i] = henna;
				
				// Calculate Henna modifiers of this Player
				recalcHennaStats();
				
				try (Connection con = DatabaseFactory.getConnection())
				{
					PreparedStatement statement = con.prepareStatement(ADD_CHAR_HENNA);
					statement.setInt(1, getObjectId());
					statement.setInt(2, henna.getSymbolId());
					statement.setInt(3, i + 1);
					statement.setInt(4, getClassIndex());
					statement.execute();
					statement.close();
				}
				catch (Exception e)
				{
					LOGGER.warning("Could not save char henna: " + e);
				}
				
				sendPacket(new HennaInfo(this));
				sendPacket(new UserInfo(this));
				sendPacket(SystemMessageId.THE_SYMBOL_HAS_BEEN_ADDED);
				return;
			}
		}
	}
	
	/**
	 * Calculate Henna modifiers of this Player.
	 */
	private void recalcHennaStats()
	{
		_hennaINT = 0;
		_hennaSTR = 0;
		_hennaCON = 0;
		_hennaMEN = 0;
		_hennaWIT = 0;
		_hennaDEX = 0;
		for (int i = 0; i < 3; i++)
		{
			if (_henna[i] == null)
			{
				continue;
			}
			
			_hennaINT += _henna[i].getINT();
			_hennaSTR += _henna[i].getSTR();
			_hennaMEN += _henna[i].getMEN();
			_hennaCON += _henna[i].getCON();
			_hennaWIT += _henna[i].getWIT();
			_hennaDEX += _henna[i].getDEX();
		}
		
		if (_hennaINT > 5)
		{
			_hennaINT = 5;
		}
		
		if (_hennaSTR > 5)
		{
			_hennaSTR = 5;
		}
		
		if (_hennaMEN > 5)
		{
			_hennaMEN = 5;
		}
		
		if (_hennaCON > 5)
		{
			_hennaCON = 5;
		}
		
		if (_hennaWIT > 5)
		{
			_hennaWIT = 5;
		}
		
		if (_hennaDEX > 5)
		{
			_hennaDEX = 5;
		}
	}
	
	/**
	 * @param slot A slot to check.
	 * @return the Henna of this Player corresponding to the selected slot.
	 */
	public Henna getHenna(int slot)
	{
		if ((slot < 1) || (slot > 3))
		{
			return null;
		}
		return _henna[slot - 1];
	}
	
	public int getHennaStatINT()
	{
		return _hennaINT;
	}
	
	public int getHennaStatSTR()
	{
		return _hennaSTR;
	}
	
	public int getHennaStatCON()
	{
		return _hennaCON;
	}
	
	public int getHennaStatMEN()
	{
		return _hennaMEN;
	}
	
	public int getHennaStatWIT()
	{
		return _hennaWIT;
	}
	
	public int getHennaStatDEX()
	{
		return _hennaDEX;
	}
	
	public void autoSave()
	{
		store();
		
		if (Config.UPDATE_ITEMS_ON_CHAR_STORE)
		{
			getInventory().updateDatabase();
			getWarehouse().updateDatabase();
			getFreight().updateDatabase();
		}
	}
	
	/**
	 * Return True if the Player is autoAttackable.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Check if the attacker isn't the Player Pet</li>
	 * <li>Check if the attacker is Monster</li>
	 * <li>If the attacker is a Player, check if it is not in the same party</li>
	 * <li>Check if the Player has Karma</li>
	 * <li>If the attacker is a Player, check if it is not in the same siege clan (Attacker, Defender)</li>
	 * @param attacker the attacker
	 * @return true, if is auto attackable
	 */
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		// Check if the attacker isn't the Player Pet
		if ((attacker == this) || (attacker == getPet()))
		{
			return false;
		}
		
		// Check if the attacker is a Monster
		if (attacker instanceof Monster)
		{
			return true;
		}
		
		// Check if the attacker is not in the same party, excluding duels like L2OFF
		if ((getParty() != null) && getParty().getPartyMembers().contains(attacker) && ((getDuelState() != Duel.DUELSTATE_DUELLING) || (getDuelId() != ((Player) attacker).getDuelId())))
		{
			return false;
		}
		
		// Check if the attacker is in olympia and olympia start
		if ((attacker instanceof Player) && ((Player) attacker).isInOlympiadMode())
		{
			if (isInOlympiadMode() && isOlympiadStart() && (((Player) attacker).getOlympiadGameId() == getOlympiadGameId()))
			{
				return !isFakeDeath();
			}
			return false;
		}
		
		// Check if the attacker is not in the same clan, excluding duels like L2OFF
		if ((getClan() != null) && (attacker != null) && getClan().isMember(attacker.getName()) && ((getDuelState() != Duel.DUELSTATE_DUELLING) || (getDuelId() != ((Player) attacker).getDuelId())))
		{
			return false;
		}
		
		// Ally check
		if (attacker instanceof Playable)
		{
			Player player = null;
			if (attacker instanceof Player)
			{
				player = (Player) attacker;
			}
			else if (attacker instanceof Summon)
			{
				player = ((Summon) attacker).getOwner();
			}
			
			// Check if the attacker is not in the same ally, excluding duels like L2OFF
			if ((player != null) && (getAllyId() != 0) && (player.getAllyId() != 0) && (getAllyId() == player.getAllyId()) && ((getDuelState() != Duel.DUELSTATE_DUELLING) || (getDuelId() != player.getDuelId())))
			{
				return false;
			}
		}
		
		// Check if the attacker is in an event
		if (isOnEvent() && (attacker instanceof Playable))
		{
			final Player player = attacker.getActingPlayer();
			if ((player != null) && player.isOnEvent())
			{
				return isOnSoloEvent() || (getTeam() != player.getTeam());
			}
		}
		
		if (Creature.isInsidePeaceZone(attacker, this))
		{
			return false;
		}
		
		// Check if the Player has Karma
		if ((getKarma() > 0) || (getPvpFlag() > 0))
		{
			return true;
		}
		
		// Check if the attacker is a Player
		if (attacker instanceof Player)
		{
			// is AutoAttackable if both players are in the same duel and the duel is still going on
			if ((getDuelState() == Duel.DUELSTATE_DUELLING) && (getDuelId() == ((Player) attacker).getDuelId()))
			{
				return true;
			}
			// Check if the Player is in an arena or a siege area
			if (isInsideZone(ZoneId.PVP) && ((Player) attacker).isInsideZone(ZoneId.PVP))
			{
				return true;
			}
			
			if (getClan() != null)
			{
				final Siege siege = SiegeManager.getInstance().getSiege(getX(), getY(), getZ());
				final FortSiege fortsiege = FortSiegeManager.getInstance().getSiege(getX(), getY(), getZ());
				if (siege != null)
				{
					// Check if a siege is in progress and if attacker and the Player aren't in the Defender clan
					if (siege.checkIsDefender(((Player) attacker).getClan()) && siege.checkIsDefender(getClan()))
					{
						return false;
					}
					
					// Check if a siege is in progress and if attacker and the Player aren't in the Attacker clan
					if (siege.checkIsAttacker(((Player) attacker).getClan()) && siege.checkIsAttacker(getClan()))
					{
						return false;
					}
				}
				if (fortsiege != null)
				{
					// Check if a siege is in progress and if attacker and the Player aren't in the Defender clan
					if (fortsiege.checkIsDefender(((Player) attacker).getClan()) && fortsiege.checkIsDefender(getClan()))
					{
						return false;
					}
					
					// Check if a siege is in progress and if attacker and the Player aren't in the Attacker clan
					if (fortsiege.checkIsAttacker(((Player) attacker).getClan()) && fortsiege.checkIsAttacker(getClan()))
					{
						return false;
					}
				}
				
				// Check if clan is at war
				if ((getClan() != null) && (((Player) attacker).getClan() != null) && getClan().isAtWarWith(((Player) attacker).getClanId()) && (getWantsPeace() == 0) && (((Player) attacker).getWantsPeace() == 0) && !isAcademyMember())
				{
					return true;
				}
			}
		}
		else if (attacker instanceof SiegeGuard)
		{
			if (getClan() != null)
			{
				final Siege siege = SiegeManager.getInstance().getSiege(this);
				return ((siege != null) && siege.checkIsAttacker(getClan())) || DevastatedCastle.getInstance().isInProgress();
			}
		}
		else if (attacker instanceof FortSiegeGuard)
		{
			if (getClan() != null)
			{
				final FortSiege fortsiege = FortSiegeManager.getInstance().getSiege(this);
				return (fortsiege != null) && fortsiege.checkIsAttacker(getClan());
			}
		}
		
		return false;
	}
	
	/**
	 * Check if the active Skill can be casted.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Check if the skill isn't toggle and is offensive</li>
	 * <li>Check if the target is in the skill cast range</li>
	 * <li>Check if the skill is Spoil type and if the target isn't already spoiled</li>
	 * <li>Check if the caster owns enought consummed Item, enough HP and MP to cast the skill</li>
	 * <li>Check if the caster isn't sitting</li>
	 * <li>Check if all skills are enabled and this skill is enabled</li>
	 * <li>Check if the caster own the weapon needed</li>
	 * <li>Check if the skill is active</li>
	 * <li>Check if all casting conditions are completed</li>
	 * <li>Notify the AI with AI_INTENTION_CAST and target</li><br>
	 * @param skill The Skill to use
	 * @param forceUse used to force ATTACK on players
	 * @param dontMove used to prevent movement, if not in range
	 */
	public void useMagic(Skill skill, boolean forceUse, boolean dontMove)
	{
		if (isDead())
		{
			abortCast();
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (skill == null)
		{
			abortCast();
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final int skillId = skill.getId();
		int currSkillId = -1;
		final SkillUseHolder current = getCurrentSkill();
		if (current != null)
		{
			currSkillId = current.getSkillId();
		}
		
		if (inObserverMode())
		{
			sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
			abortCast();
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if the caster is sitting
		if (isSitting() && !skill.isPotion())
		{
			// Send a System Message to the caster
			sendPacket(SystemMessageId.YOU_CANNOT_MOVE_WHILE_SITTING);
			
			// Send a Server->Client packet ActionFailed to the Player
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if the skill type is TOGGLE
		if (skill.isToggle())
		{
			// Like L2OFF you can't use fake death if you are mounted
			if ((skill.getId() == 60 /* Fake Death */) && isMounted())
			{
				return;
			}
			
			// Get effects of the skill
			final Effect effect = getFirstEffect(skill);
			
			// Like L2OFF toogle skills have little delay
			if ((_toggleUse != 0) && ((_toggleUse + 400) > Chronos.currentTimeMillis()))
			{
				_toggleUse = 0;
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			_toggleUse = Chronos.currentTimeMillis();
			if (effect != null)
			{
				// fake death exception
				if (skill.getId() != 60 /* Fake Death */)
				{
					effect.exit(false);
				}
				
				// Send a Server->Client packet ActionFailed to the Player
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		// Check if the skill is active
		if (skill.isPassive())
		{
			// just ignore the passive skill request. why does the client send it anyway ??
			// Send a Server->Client packet ActionFailed to the Player
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if it is ok to summon Siege Golem (13), Wild Hog Cannon (299) or Swoop Cannon (448).
		if (((skillId == 13) || (skillId == 299) || (skillId == 448)) && !SiegeManager.getInstance().checkIfOkToSummon(this, false) && !FortSiegeManager.getInstance().checkIfOkToSummon(this, false))
		{
			return;
		}
		
		// If a skill is currently being used, queue this one if this is not the same
		// Note that this check is currently imperfect: getCurrentSkill() isn't always null when a skill has
		// failed to cast, or the casting is not yet in progress when this is rechecked
		if ((currSkillId != -1) && (isCastingNow() || isCastingPotionNow()))
		{
			final SkillUseHolder currentSkill = getCurrentSkill();
			// Check if new skill different from current skill in progress
			if ((currentSkill != null) && (skill.getId() == currentSkill.getSkillId()))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			// Create a new SkillUseHolder object and queue it in the player _queuedSkill
			setQueuedSkill(skill, forceUse, dontMove);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if skill is in reuse time
		if (isSkillDisabled(skill))
		{
			// Cp potion message like L2OFF
			if ((skill.getId() == 2166 /* CP Gauge Potion */))
			{
				if (skill.getLevel() == 2)
				{
					sendMessage("Greater CP Potion is not available at this time: being prepared for reuse.");
				}
				else if (skill.getLevel() == 1)
				{
					sendMessage("CP Potion is not available at this time: being prepared for reuse.");
				}
			}
			else
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_NOT_AVAILABLE_AT_THIS_TIME_BEING_PREPARED_FOR_REUSE);
				sm.addSkillName(skill.getId(), skill.getLevel());
				sendPacket(sm);
			}
			
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// ************************************* Check Casting in Progress *******************************************
		
		// Create a new SkillUseHolder object and set the player _currentSkill
		// This is used mainly to save & queue the button presses, since Creature has
		// _lastSkillCast which could otherwise replace it
		setCurrentSkill(skill, forceUse, dontMove);
		if (_queuedSkill != null)
		{
			setQueuedSkill(null, false, false);
		}
		
		// Triggered skills cannot be used directly
		if (!_triggeredSkills.isEmpty() && (_triggeredSkills.get(skill.getId()) != null))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// ************************************* Check Target *******************************************
		// Create and set a WorldObject containing the target of the skill
		WorldObject target = null;
		final SkillTargetType skillTargetType = skill.getTargetType();
		final SkillType skillType = skill.getSkillType();
		
		switch (skillTargetType)
		{
			// Target the player if skill type is AURA, PARTY, CLAN or SELF
			case TARGET_AURA:
			{
				if (isInOlympiadMode() && !isOlympiadStart())
				{
					setTarget(this);
				}
				// fallthough?
			}
			case TARGET_PARTY:
			case TARGET_ALLY:
			case TARGET_CLAN:
			case TARGET_GROUND:
			case TARGET_SELF:
			{
				target = this;
				break;
			}
			case TARGET_PET:
			{
				target = getPet();
				break;
			}
			default:
			{
				target = getTarget();
				break;
			}
		}
		
		// Check the validity of the target
		if (target == null)
		{
			sendPacket(SystemMessageId.YOUR_TARGET_CANNOT_BE_FOUND);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// skills can be used on Walls and Doors only durring siege
		// Ignore skill UNLOCK
		if (skill.isOffensive() && (target instanceof Door))
		{
			final boolean isCastle = ((((Door) target).getCastle() != null) && (((Door) target).getCastle().getCastleId() > 0) && ((Door) target).getCastle().getSiege().isInProgress());
			final boolean isFort = ((((Door) target).getFort() != null) && (((Door) target).getFort().getFortId() > 0) && ((Door) target).getFort().getSiege().isInProgress());
			if ((!isCastle && !isFort))
			{
				return;
			}
		}
		
		// Like L2OFF you can't heal random purple people without using CTRL
		final SkillUseHolder skillUseHolder = getCurrentSkill();
		if ((skillUseHolder != null) && (skill.getSkillType() == SkillType.HEAL) && !skillUseHolder.isCtrlPressed() && (target instanceof Player) && (target.getActingPlayer().getPvpFlag() == 1) && (this != target))
		{
			if (((getClanId() == 0) || (target.getActingPlayer().getClanId() == 0)) || (getClanId() != target.getActingPlayer().getClanId()))
			{
				if (((getAllyId() == 0) || (target.getActingPlayer().getAllyId() == 0)) || (getAllyId() != target.getActingPlayer().getAllyId()))
				{
					if (((getParty() == null) || (target.getActingPlayer().getParty() == null)) || (!getParty().equals(target.getActingPlayer().getParty())))
					{
						sendPacket(SystemMessageId.INVALID_TARGET);
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
				}
			}
		}
		
		// If target is not attackable, send a Server->Client packet ActionFailed
		if (!target.canBeAttacked() && !getAccessLevel().allowPeaceAttack() && !target.isDoor() && (skillType != SkillType.TAKECASTLE))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Are the target and the player in the same duel?
		if (isInDuel() && (!(target instanceof Player) || (target.getActingPlayer().getDuelId() != getDuelId())) && (!(target instanceof Servitor) || (((Summon) target).getOwner().getDuelId() != getDuelId())))
		{
			sendMessage("You cannot do this while duelling.");
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Pk protection config
		if (Config.ALLOW_CHAR_KILL_PROTECT && skill.isOffensive() && !isGM() && target.isPlayer() && (target.getActingPlayer().getPvpFlag() == 0) && (target.getActingPlayer().getKarma() == 0))
		{
			final int thisLevel = getLevel();
			final int targetLevel = target.getActingPlayer().getLevel();
			final Siege siege = SiegeManager.getInstance().getSiege(this);
			if ((((siege == null) || !siege.isInProgress()) && ((thisLevel >= 20) && (targetLevel < 20))) || ((thisLevel >= 40) && (targetLevel < 40)) || ((thisLevel >= 52) && (targetLevel < 52)) || ((thisLevel >= 61) && (targetLevel < 61)) || ((thisLevel >= 76) && (targetLevel < 76)))
			{
				sendMessage("You can only engage in PvP when your target is within your level grade.");
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		// ************************************* Check skill availability *******************************************
		
		// Check if all skills are disabled
		if (isAllSkillsDisabled() && !getAccessLevel().allowPeaceAttack())
		{
			// Send a Server->Client packet ActionFailed to the Player
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// prevent casting signets to peace zone
		if (((skill.getSkillType() == SkillType.SIGNET) || (skill.getSkillType() == SkillType.SIGNET_CASTTIME)) && isInsidePeaceZone(this))
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS);
			sm.addSkillName(skillId);
			sendPacket(sm);
			return;
		}
		
		// ************************************* Check Consumables *******************************************
		
		// Check if the caster has enough MP
		if (getCurrentMp() < (getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill)))
		{
			// Send a System Message to the caster
			sendPacket(SystemMessageId.NOT_ENOUGH_MP);
			
			// Send a Server->Client packet ActionFailed to the Player
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if the caster has enough HP
		if (getCurrentHp() <= skill.getHpConsume())
		{
			// Send a System Message to the caster
			sendPacket(SystemMessageId.NOT_ENOUGH_HP);
			
			// Send a Server->Client packet ActionFailed to the Player
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if the spell consummes an Item
		if (skill.getItemConsume() > 0)
		{
			// Get the Item consummed by the spell
			final Item requiredItems = getInventory().getItemByItemId(skill.getItemConsumeId());
			
			// Check if the caster owns enought consummed Item to cast
			if ((requiredItems == null) || (requiredItems.getCount() < skill.getItemConsume()))
			{
				// Checked: when a summon skill failed, server show required consume item count
				if (skillType == SkillType.SUMMON)
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.SUMMONING_A_SERVITOR_COSTS_S2_S1);
					sm.addItemName(skill.getItemConsumeId());
					sm.addNumber(skill.getItemConsume());
					sendPacket(sm);
				}
				else
				{
					// Send a System Message to the caster
					sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
				}
				return;
			}
		}
		
		// Like L2OFF if you are mounted on wyvern you can't use own skills
		if (isFlying() && (skillId != 327) && (skillId != 4289) && !skill.isPotion())
		{
			sendMessage("You cannot use skills while riding a wyvern.");
			return;
		}
		
		// Like L2OFF if you have a summon you can't summon another one (ignore cubics)
		if ((skillType == SkillType.SUMMON) && (skill instanceof SkillSummon) && !((SkillSummon) skill).isCubic() && ((getPet() != null) || isMounted()))
		{
			sendPacket(SystemMessageId.YOU_ALREADY_HAVE_A_PET);
			return;
		}
		
		if ((skill.getNumCharges() > 0) && (skill.getSkillType() != SkillType.CHARGE) && (skill.getSkillType() != SkillType.CHARGEDAM) && (skill.getSkillType() != SkillType.CHARGE_EFFECT) && (skill.getSkillType() != SkillType.PDAM))
		{
			final EffectCharge effect = (EffectCharge) getFirstEffect(Effect.EffectType.CHARGE);
			if ((effect == null) || (effect.numCharges < skill.getNumCharges()))
			{
				sendPacket(new SystemMessage(SystemMessageId.S1_IS_NOT_AVAILABLE_AT_THIS_TIME_BEING_PREPARED_FOR_REUSE).addSkillName(skillId));
				return;
			}
			
			effect.numCharges -= skill.getNumCharges();
			sendPacket(new EtcStatusUpdate(this));
			if (effect.numCharges == 0)
			{
				effect.exit(false);
			}
		}
		
		// ************************************* Check Casting Conditions *******************************************
		
		// Check if the caster own the weapon needed
		if (!skill.getWeaponDependancy(this))
		{
			// Send a Server->Client packet ActionFailed to the Player
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		// Check if all casting conditions are completed
		if (!skill.checkCondition(this, target, false))
		{
			// Send a Server->Client packet ActionFailed to the Player
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// ************************************* Check Player State *******************************************
		
		// Abnormal effects(ex : Stun, Sleep...) are checked in Creature useMagic()
		
		// Check if the player use "Fake Death" skill
		if (isAlikeDead() && !skill.isPotion() && (skill.getSkillType() != SkillType.FAKE_DEATH))
		{
			// Send a Server->Client packet ActionFailed to the Player
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (isFishing() && (skillType != SkillType.PUMPING) && (skillType != SkillType.REELING) && (skillType != SkillType.FISHING))
		{
			// Only fishing skills are available
			sendPacket(SystemMessageId.ONLY_FISHING_SKILLS_MAY_BE_USED_AT_THIS_TIME);
			return;
		}
		
		// ************************************* Check Skill Type *******************************************
		
		// Check if this is offensive magic skill
		if (skill.isOffensive())
		{
			if (isInsidePeaceZone(this, target) // Like L2OFF you can use cupid bow skills on peace zone
				&& ((skill.getId() != 3260 /* Forgiveness */) && (skill.getId() != 3261 /* Heart Shot */) && (skill.getId() != 3262 /* Double Heart Shot */) //
					&& (skillTargetType != SkillTargetType.TARGET_AURA))) // Like L2OFF people can use TARGET_AURE skills on peace zone
			{
				// If Creature or target is in a peace zone, send a system message TARGET_IN_PEACEZONE a Server->Client packet ActionFailed
				sendPacket(SystemMessageId.YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			if (isInOlympiadMode() && !isOlympiadStart() && (skillTargetType != SkillTargetType.TARGET_AURA))
			{
				// if Player is in Olympia and the match isn't already start, send a Server->Client packet ActionFailed
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (!(target instanceof Monster) && (skillType == SkillType.CONFUSE_MOB_ONLY))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			// Check if a Forced ATTACK is in progress on non-attackable target
			if (!target.isAutoAttackable(this) //
				&& (!forceUse && ((skill.getId() != 3260 /* Forgiveness */) && (skill.getId() != 3261 /* Heart Shot */) && (skill.getId() != 3262 /* Double Heart Shot */))) //
				&& (skillTargetType != SkillTargetType.TARGET_AURA) //
				&& (skillTargetType != SkillTargetType.TARGET_CLAN) //
				&& (skillTargetType != SkillTargetType.TARGET_ALLY) //
				&& (skillTargetType != SkillTargetType.TARGET_PARTY) //
				&& (skillTargetType != SkillTargetType.TARGET_SELF) //
				&& (skillTargetType != SkillTargetType.TARGET_GROUND))
			{
				// Send a Server->Client packet ActionFailed to the Player
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			// Check if the target is in the skill cast range
			if (dontMove)
			{
				// Calculate the distance between the Player and the target
				if (skillTargetType == SkillTargetType.TARGET_GROUND)
				{
					if (!isInsideRadius2D(getCurrentSkillWorldPosition().getX(), getCurrentSkillWorldPosition().getY(), getCurrentSkillWorldPosition().getZ(), skill.getCastRange() + getTemplate().getCollisionRadius()))
					{
						// Send a System Message to the caster
						sendPacket(SystemMessageId.YOUR_TARGET_IS_OUT_OF_RANGE);
						
						// Send a Server->Client packet ActionFailed to the Player
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
				}
				else if ((skill.getCastRange() > 0) && !isInsideRadius2D(target, skill.getCastRange() + getTemplate().getCollisionRadius())) // Calculate the distance between the Player and the target
				{
					// Send a System Message to the caster
					sendPacket(SystemMessageId.YOUR_TARGET_IS_OUT_OF_RANGE);
					
					// Send a Server->Client packet ActionFailed to the Player
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
			// Check range for SIGNET skills
			else if ((skillType == SkillType.SIGNET) && !isInsideRadius2D(getCurrentSkillWorldPosition().getX(), getCurrentSkillWorldPosition().getY(), getCurrentSkillWorldPosition().getZ(), skill.getCastRange() + getTemplate().getCollisionRadius()))
			{
				// Send a System Message to the caster
				sendPacket(SystemMessageId.YOUR_TARGET_IS_OUT_OF_RANGE);
				
				// Send a Server->Client packet ActionFailed to the Player
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		// Check if the skill is defensive and if the target is a monster and if force attack is set.. if not then we don't want to cast.
		if (!skill.isOffensive() && (target instanceof Monster) && !forceUse && (skillTargetType != SkillTargetType.TARGET_PET) && (skillTargetType != SkillTargetType.TARGET_AURA) && (skillTargetType != SkillTargetType.TARGET_CLAN) && (skillTargetType != SkillTargetType.TARGET_SELF) && (skillTargetType != SkillTargetType.TARGET_PARTY) && (skillTargetType != SkillTargetType.TARGET_ALLY) && (skillTargetType != SkillTargetType.TARGET_CORPSE_MOB) && (skillTargetType != SkillTargetType.TARGET_AREA_CORPSE_MOB) && (skillTargetType != SkillTargetType.TARGET_GROUND) && (skillType != SkillType.BEAST_FEED) && (skillType != SkillType.DELUXE_KEY_UNLOCK) && (skillType != SkillType.UNLOCK))
		{
			// send the action failed so that the skill doens't go off.
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if the skill is Spoil type and if the target isn't already spoiled
		if ((skillType == SkillType.SPOIL) && !(target instanceof Monster))
		{
			// Send a System Message to the Player
			sendPacket(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET);
			
			// Send a Server->Client packet ActionFailed to the Player
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if the skill is Sweep type and if conditions not apply
		if ((skillType == SkillType.SWEEP) && (target instanceof Attackable))
		{
			final int spoilerId = ((Attackable) target).getSpoiledBy();
			if (((Attackable) target).isDead())
			{
				if (!((Attackable) target).isSpoil())
				{
					// Send a System Message to the Player
					sendPacket(SystemMessageId.SWEEPER_FAILED_TARGET_NOT_SPOILED);
					
					// Send a Server->Client packet ActionFailed to the Player
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				
				if ((getObjectId() != spoilerId) && !isInLooterParty(spoilerId))
				{
					// Send a System Message to the Player
					sendPacket(SystemMessageId.THERE_ARE_NO_PRIORITY_RIGHTS_ON_A_SWEEPER);
					
					// Send a Server->Client packet ActionFailed to the Player
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
		}
		
		// Check if the skill is Drain Soul (Soul Crystals) and if the target is a MOB
		if ((skillType == SkillType.DRAIN_SOUL) && !(target instanceof Monster))
		{
			// Send a System Message to the Player
			sendPacket(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET);
			
			// Send a Server->Client packet ActionFailed to the Player
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if ((skillTargetType == SkillTargetType.TARGET_GROUND) && (getCurrentSkillWorldPosition() == null))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if this is a Pvp skill and target isn't a non-flagged/non-karma player
		switch (skillTargetType)
		{
			case TARGET_PARTY:
			case TARGET_ALLY: // For such skills, checkPvpSkill() is called from Skill.getTargetList()
			case TARGET_CLAN: // For such skills, checkPvpSkill() is called from Skill.getTargetList()
			case TARGET_AURA:
			case TARGET_SELF:
			case TARGET_GROUND:
			{
				break;
			}
			default:
			{
				// if pvp skill is not allowed for given target
				if (!checkPvpSkill(target, skill) && !getAccessLevel().allowPeaceAttack() //
					&& ((skill.getId() != 3260 /* Forgiveness */) && (skill.getId() != 3261 /* Heart Shot */) && (skill.getId() != 3262 /* Double Heart Shot */)))
				{
					// Send a System Message to the Player
					sendPacket(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET);
					
					// Send a Server->Client packet ActionFailed to the Player
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
		}
		
		if ((skillTargetType == SkillTargetType.TARGET_HOLY) && !TakeCastle.checkIfOkToCastSealOfRule(this, false))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			abortCast();
			return;
		}
		
		if ((skillType == SkillType.SIEGEFLAG) && !SiegeFlag.checkIfOkToPlaceFlag(this, false))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			abortCast();
			return;
		}
		else if ((skillType == SkillType.STRSIEGEASSAULT) && !StrSiegeAssault.checkIfOkToUseStriderSiegeAssault(this, false))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			abortCast();
			return;
		}
		
		// TEMPFIX: Check client Z coordinate instead of server z to avoid exploit killing Zaken from others floor
		if ((target instanceof GrandBoss) && (((GrandBoss) target).getNpcId() == 29022) && (Math.abs(getClientZ() - target.getZ()) > 200))
		{
			sendPacket(SystemMessageId.CANNOT_SEE_TARGET);
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// GeoData Los Check here
		if ((skill.getCastRange() > 0) && !GeoEngine.getInstance().canSeeTarget(this, target))
		{
			sendPacket(SystemMessageId.CANNOT_SEE_TARGET);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// If all conditions are checked, create a new SkillUseHolder object and set the player _currentSkill
		setCurrentSkill(skill, forceUse, dontMove);
		
		// Check if the active Skill can be casted (ex : not sleeping...), Check if the target is correct and Notify the AI with AI_INTENTION_CAST and target
		super.useMagic(skill);
	}
	
	/**
	 * Checks if is in looter party.
	 * @param looterId the looter id
	 * @return true, if is in looter party
	 */
	public boolean isInLooterParty(int looterId)
	{
		final Player looter = World.getInstance().getPlayer(looterId);
		
		// if Player is in a CommandChannel
		if (isInParty() && getParty().isInCommandChannel() && (looter != null))
		{
			return getParty().getCommandChannel().getMembers().contains(looter);
		}
		
		if (isInParty() && (looter != null))
		{
			return getParty().getPartyMembers().contains(looter);
		}
		
		return false;
	}
	
	/**
	 * Check if the requested casting is a Pc->Pc skill cast and if it's a valid pvp condition.
	 * @param target WorldObject instance containing the target
	 * @param skill Skill instance with the skill being casted
	 * @return False if the skill is a pvpSkill and target is not a valid pvp target
	 */
	public boolean checkPvpSkill(WorldObject target, Skill skill)
	{
		return checkPvpSkill(target, skill, false);
	}
	
	/**
	 * Check if the requested casting is a Pc->Pc skill cast and if it's a valid pvp condition.
	 * @param worldObject WorldObject instance containing the target
	 * @param skill Skill instance with the skill being casted
	 * @param srcIsSummon is Summon - caster?
	 * @return False if the skill is a pvpSkill and target is not a valid pvp target
	 */
	public boolean checkPvpSkill(WorldObject worldObject, Skill skill, boolean srcIsSummon)
	{
		WorldObject target = worldObject;
		
		// Check if player and target are in events and on the same team.
		if ((target instanceof Player) && isOnEvent() && skill.isOffensive())
		{
			return target.getActingPlayer().isOnEvent() && (isOnSoloEvent() || (getTeam() != target.getActingPlayer().getTeam()));
		}
		
		// check for PC->PC Pvp status
		if (target instanceof Summon)
		{
			target = ((Summon) target).getOwner();
		}
		
		if ((target != null) && // target not null and
			(target != this) && // target is not self and
			(target instanceof Player) && // target is Player and
			(!isInDuel() || (target.getActingPlayer().getDuelId() != getDuelId())) && // self is not in a duel and attacking opponent
			!isInsideZone(ZoneId.PVP) && // Pc is not in PvP zone
			!target.getActingPlayer().isInsideZone(ZoneId.PVP)) // target is not in PvP zone
		{
			final SkillUseHolder skillUseHolder = getCurrentSkill();
			if (skill.isPvpSkill()) // pvp skill
			{
				if ((getClan() != null) && (target.getActingPlayer().getClan() != null) && getClan().isAtWarWith(target.getActingPlayer().getClan().getClanId()) && target.getActingPlayer().getClan().isAtWarWith(getClan().getClanId()))
				{
					return true; // in clan war player can attack whites even with sleep etc.
				}
				if ((target.getActingPlayer().getPvpFlag() == 0) && // target's pvp flag is not set and
					(target.getActingPlayer().getKarma() == 0)) // target has no karma
				{
					return false;
				}
			}
			else if ((skillUseHolder != null) && !skillUseHolder.isCtrlPressed() && skill.isOffensive() && !srcIsSummon)
			{
				if ((getClan() != null) && (target.getActingPlayer().getClan() != null) && getClan().isAtWarWith(target.getActingPlayer().getClan().getClanId()) && target.getActingPlayer().getClan().isAtWarWith(getClan().getClanId()))
				{
					return true; // in clan war player can attack whites even without ctrl
				}
				if ((target.getActingPlayer().getPvpFlag() == 0) && // target's pvp flag is not set and
					(target.getActingPlayer().getKarma() == 0)) // target has no karma
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Reduce Item quantity of the Player Inventory and send it a Server->Client packet InventoryUpdate.
	 * @param itemConsumeId the item consume id
	 * @param itemCount the item count
	 */
	@Override
	public void consumeItem(int itemConsumeId, int itemCount)
	{
		if ((itemConsumeId != 0) && (itemCount != 0))
		{
			destroyItemByItemId("Consume", itemConsumeId, itemCount, null, true);
		}
	}
	
	/**
	 * Return True if the Player is a Mage.
	 * @return true, if is mage class
	 */
	public boolean isMageClass()
	{
		return getClassId().isMage();
	}
	
	/**
	 * Checks if is mounted.
	 * @return true, if is mounted
	 */
	public boolean isMounted()
	{
		return _mountType > 0;
	}
	
	/**
	 * Set the type of Pet mounted (0 : none, 1 : Stridder, 2 : Wyvern) and send a Server->Client packet InventoryUpdate to the Player.
	 * @return true, if successful
	 */
	public boolean checkLandingState()
	{
		// Check if char is in a no landing zone
		if (isInsideZone(ZoneId.NO_LANDING))
		{
			return true;
		}
		else
		// if this is a castle that is currently being sieged, and the rider is NOT a castle owner he cannot land.
		// castle owner is the leader of the clan that owns the castle where the pc is
		if (isInsideZone(ZoneId.SIEGE) && ((getClan() == null) || (CastleManager.getInstance().getCastle(this) != CastleManager.getInstance().getCastleByOwner(getClan())) || (this != getClan().getLeader().getPlayer())))
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Sets the mount type.
	 * @param mountType the mount type
	 * @return true, if successful
	 */
	public boolean setMountType(int mountType)
	{
		if (checkLandingState() && (mountType == 2))
		{
			return false;
		}
		
		switch (mountType)
		{
			case 0:
			{
				setFlying(false);
				setRiding(false);
				break; // Dismounted
			}
			case 1:
			{
				setRiding(true);
				if (isNoble())
				{
					final Skill striderAssaultSkill = SkillTable.getInstance().getSkill(325, 1);
					addSkill(striderAssaultSkill, false); // not saved to DB
				}
				break;
			}
			case 2:
			{
				setFlying(true);
				break; // Flying Wyvern
			}
		}
		
		_mountType = mountType;
		
		// Send a Server->Client packet InventoryUpdate to the Player in order to update speed
		sendPacket(new UserInfo(this));
		return true;
	}
	
	/**
	 * Return the type of Pet mounted (0 : none, 1 : Stridder, 2 : Wyvern).
	 * @return the mount type
	 */
	public int getMountType()
	{
		return _mountType;
	}
	
	/**
	 * Send a Server->Client packet UserInfo to this Player and CharInfo to all Player in its _KnownPlayers.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * Others Player in the detection area of the Player are identified in <b>_knownPlayers</b>. In order to inform other players of this Player state modifications, server just need to go through _knownPlayers to send Server->Client Packet<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Send a Server->Client packet UserInfo to this Player (Public and Private Data)</li>
	 * <li>Send a Server->Client packet CharInfo to all Player in _KnownPlayers of the Player (Public data only)</li><br>
	 * <font color=#FF0000><b><u>Caution</u>: DON'T SEND UserInfo packet to other players instead of CharInfo packet. Indeed, UserInfo packet contains PRIVATE DATA as MaxHP, STR, DEX...</b></font>
	 */
	@Override
	public void updateAbnormalEffect()
	{
		broadcastUserInfo();
	}
	
	/**
	 * Disable the Inventory and create a new task to enable it after 1.5s.
	 */
	public void tempInvetoryDisable()
	{
		_inventoryDisable = true;
		ThreadPool.schedule(new InventoryEnable(), 1500);
	}
	
	/**
	 * Return True if the Inventory is disabled.
	 * @return true, if is invetory disabled
	 */
	public boolean isInvetoryDisabled()
	{
		return _inventoryDisable;
	}
	
	class InventoryEnable implements Runnable
	{
		@Override
		public void run()
		{
			_inventoryDisable = false;
		}
	}
	
	/**
	 * Gets the cubics.
	 * @return the cubics
	 */
	public Map<Integer, Cubic> getCubics()
	{
		// clean cubics instances
		for (Entry<Integer, Cubic> entry : _cubics.entrySet())
		{
			final Integer id = entry.getKey();
			if ((id == null) || (entry.getValue() == null))
			{
				try
				{
					_cubics.remove(id);
				}
				catch (NullPointerException e)
				{
					// Tried to remove a null key.
				}
			}
		}
		return _cubics;
	}
	
	/**
	 * Add a Cubic to the Player _cubics.
	 * @param id the id
	 * @param level the level
	 * @param matk the matk
	 * @param activationtime the activationtime
	 * @param activationchance the activationchance
	 * @param totalLifetime the total lifetime
	 * @param givenByOther the given by other
	 */
	public void addCubic(int id, int level, double matk, int activationtime, int activationchance, int totalLifetime, boolean givenByOther)
	{
		final Cubic cubic = new Cubic(this, id, level, (int) matk, activationtime, activationchance, totalLifetime, givenByOther);
		_cubics.put(id, cubic);
	}
	
	/**
	 * Remove a Cubic from the Player _cubics.
	 * @param id the id
	 */
	public void delCubic(int id)
	{
		_cubics.remove(id);
	}
	
	/**
	 * Return the Cubic corresponding to the Identifier of the Player _cubics.
	 * @param id the id
	 * @return the cubic
	 */
	public Cubic getCubic(int id)
	{
		return _cubics.get(id);
	}
	
	public void unsummonAllCubics()
	{
		// Unsummon Cubics
		if (!_cubics.isEmpty())
		{
			for (Cubic cubic : _cubics.values())
			{
				cubic.stopAction();
				cubic.cancelDisappear();
			}
			
			_cubics.clear();
		}
	}
	
	@Override
	public String toString()
	{
		return "player " + getName();
	}
	
	/**
	 * Return the modifier corresponding to the Enchant Effect of the Active Weapon (Min : 127).
	 * @return the enchant effect
	 */
	public int getEnchantEffect()
	{
		final Item wpn = getActiveWeaponInstance();
		if (wpn == null)
		{
			return 0;
		}
		return Math.min(127, wpn.getEnchantLevel());
	}
	
	/**
	 * Set the _lastFolkNpc of the Player corresponding to the last Folk wich one the player talked.
	 * @param folkNpc the new last folk npc
	 */
	public void setLastFolkNPC(Folk folkNpc)
	{
		_lastFolkNpc = folkNpc;
	}
	
	/**
	 * Return the _lastFolkNpc of the Player corresponding to the last Folk wich one the player talked.
	 * @return the last folk npc
	 */
	public Folk getLastFolkNPC()
	{
		return _lastFolkNpc;
	}
	
	/**
	 * Set the Silent Moving mode Flag.
	 * @param flag the new silent moving
	 */
	public void setSilentMoving(boolean flag)
	{
		if (flag)
		{
			_isSilentMoving++;
		}
		else
		{
			_isSilentMoving--;
		}
	}
	
	/**
	 * Return True if the Silent Moving mode is active.
	 * @return true, if is silent moving
	 */
	public boolean isSilentMoving()
	{
		return _isSilentMoving > 0;
	}
	
	/**
	 * Return True if Player is a participant in the Festival of Darkness.
	 * @return true, if is festival participant
	 */
	public boolean isFestivalParticipant()
	{
		return SevenSignsFestival.getInstance().isPlayerParticipant(this);
	}
	
	/**
	 * Adds the auto soul shot.
	 * @param itemId the item id
	 */
	public void addAutoSoulShot(int itemId)
	{
		_activeSoulShots.put(itemId, itemId);
	}
	
	/**
	 * Removes the auto soul shot.
	 * @param itemId the item id
	 */
	public void removeAutoSoulShot(int itemId)
	{
		_activeSoulShots.remove(itemId);
	}
	
	/**
	 * Gets the auto soul shot.
	 * @return the auto soul shot
	 */
	public Map<Integer, Integer> getAutoSoulShot()
	{
		return _activeSoulShots;
	}
	
	/**
	 * Recharge auto soul shot.
	 * @param physical the physical
	 * @param magic the magic
	 * @param summon the summon
	 */
	public void rechargeAutoSoulShot(boolean physical, boolean magic, boolean summon)
	{
		Item item;
		IItemHandler handler;
		if ((_activeSoulShots == null) || (_activeSoulShots.isEmpty()))
		{
			return;
		}
		
		for (int itemId : _activeSoulShots.values())
		{
			item = getInventory().getItemByItemId(itemId);
			if (item != null)
			{
				if (magic)
				{
					if (!summon)
					{
						if ((itemId == 2509) || (itemId == 2510) || (itemId == 2511) || (itemId == 2512) || (itemId == 2513) || (itemId == 2514) || (itemId == 3947) || (itemId == 3948) || (itemId == 3949) || (itemId == 3950) || (itemId == 3951) || (itemId == 3952) || (itemId == 5790))
						{
							handler = ItemHandler.getInstance().getItemHandler(itemId);
							if (handler != null)
							{
								handler.useItem(this, item);
							}
						}
					}
					else if ((itemId == 6646) || (itemId == 6647))
					{
						handler = ItemHandler.getInstance().getItemHandler(itemId);
						if (handler != null)
						{
							handler.useItem(this, item);
						}
					}
				}
				
				if (physical)
				{
					if (!summon)
					{
						if ((itemId == 1463) || (itemId == 1464) || (itemId == 1465) || (itemId == 1466) || (itemId == 1467) || (itemId == 1835) || (itemId == 5789))
						{
							handler = ItemHandler.getInstance().getItemHandler(itemId);
							if (handler != null)
							{
								handler.useItem(this, item);
							}
						}
					}
					else if (itemId == 6645)
					{
						handler = ItemHandler.getInstance().getItemHandler(itemId);
						if (handler != null)
						{
							handler.useItem(this, item);
						}
					}
				}
			}
			else
			{
				removeAutoSoulShot(itemId);
			}
		}
	}
	
	class WarnUserTakeBreak implements Runnable
	{
		@Override
		public void run()
		{
			if (isOnline())
			{
				sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_BEEN_PLAYING_FOR_AN_EXTENDED_PERIOD_OF_TIME_PLEASE_CONSIDER_TAKING_A_BREAK));
			}
			else
			{
				stopWarnUserTakeBreak();
			}
		}
	}
	
	class RentPetTask implements Runnable
	{
		@Override
		public void run()
		{
			stopRentPet();
		}
	}
	
	public ScheduledFuture<?> _taskforfish;
	
	class WaterTask implements Runnable
	{
		@Override
		public void run()
		{
			double reduceHp = getMaxHp() / 100.0;
			if (reduceHp < 1)
			{
				reduceHp = 1;
			}
			
			reduceCurrentHp(reduceHp, Player.this, false);
			// reduced hp, becouse not rest
			final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_TAKEN_S1_DAMAGE_BECAUSE_YOU_WERE_UNABLE_TO_BREATHE);
			sm.addNumber((int) reduceHp);
			sendPacket(sm);
		}
	}
	
	class LookingForFishTask implements Runnable
	{
		boolean _isNoob;
		boolean _isUpperGrade;
		int _fishType;
		int _fishGutsCheck;
		int _gutsCheckTime;
		long _endTaskTime;
		
		/**
		 * Instantiates a new looking for fish task.
		 * @param fishWaitTime the fish wait time
		 * @param fishGutsCheck the fish guts check
		 * @param fishType the fish type
		 * @param isNoob the is noob
		 * @param isUpperGrade the is upper grade
		 */
		protected LookingForFishTask(int fishWaitTime, int fishGutsCheck, int fishType, boolean isNoob, boolean isUpperGrade)
		{
			_fishGutsCheck = fishGutsCheck;
			_endTaskTime = Chronos.currentTimeMillis() + fishWaitTime + 10000;
			_fishType = fishType;
			_isNoob = isNoob;
			_isUpperGrade = isUpperGrade;
		}
		
		@Override
		public void run()
		{
			if (Chronos.currentTimeMillis() >= _endTaskTime)
			{
				endFishing(false);
				return;
			}
			if (_fishType == -1)
			{
				return;
			}
			final int check = Rnd.get(1000);
			if (_fishGutsCheck > check)
			{
				stopLookingForFishTask();
				startFishCombat(_isNoob, _isUpperGrade);
			}
		}
	}
	
	/**
	 * Gets the clan privileges.
	 * @return the clan privileges
	 */
	public int getClanPrivileges()
	{
		return _clanPrivileges;
	}
	
	/**
	 * Sets the clan privileges.
	 * @param n the new clan privileges
	 */
	public void setClanPrivileges(int n)
	{
		_clanPrivileges = n;
	}
	
	/**
	 * Sets the pledge class.
	 * @param classId the new pledge class
	 */
	public void setPledgeClass(int classId)
	{
		_pledgeClass = classId;
	}
	
	/**
	 * Gets the pledge class.
	 * @return the pledge class
	 */
	public int getPledgeClass()
	{
		return _pledgeClass;
	}
	
	/**
	 * Sets the pledge type.
	 * @param typeId the new pledge type
	 */
	public void setPledgeType(int typeId)
	{
		_pledgeType = typeId;
	}
	
	/**
	 * Gets the pledge type.
	 * @return the pledge type
	 */
	public int getPledgeType()
	{
		return _pledgeType;
	}
	
	/**
	 * Gets the apprentice.
	 * @return the apprentice
	 */
	public int getApprentice()
	{
		return _apprentice;
	}
	
	/**
	 * Sets the apprentice.
	 * @param apprenticeId the new apprentice id
	 */
	public void setApprentice(int apprenticeId)
	{
		_apprentice = apprenticeId;
	}
	
	/**
	 * Gets the sponsor.
	 * @return the sponsor
	 */
	public int getSponsor()
	{
		return _sponsor;
	}
	
	/**
	 * Sets the sponsor.
	 * @param sponsorId the new sponsor id
	 */
	public void setSponsor(int sponsorId)
	{
		_sponsor = sponsorId;
	}
	
	/**
	 * Send message.
	 * @param message the message
	 */
	@Override
	public void sendMessage(String message)
	{
		sendPacket(SystemMessage.sendString(message));
	}
	
	/**
	 * Enter observer mode.
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void enterObserverMode(int x, int y, int z)
	{
		_obsX = getX();
		_obsY = getY();
		_obsZ = getZ();
		
		// Unsummon pet while entering on Observer mode
		if (getPet() != null)
		{
			getPet().unSummon(this);
		}
		
		// Unsummon cubics while entering on Observer mode
		unsummonAllCubics();
		
		_observerMode = true;
		setTarget(null);
		stopMove(null);
		setParalyzed(true);
		setInvul(true);
		
		_wasInvisible = getAppearance().isInvisible();
		getAppearance().setInvisible();
		
		sendPacket(new ObservationMode(x, y, z));
		getKnownList().removeAllKnownObjects(); // reinit knownlist
		setXYZ(x, y, z);
		teleToLocation(x, y, z);
		broadcastUserInfo();
	}
	
	/**
	 * Enter olympiad observer mode.
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @param id the id
	 */
	public void enterOlympiadObserverMode(int x, int y, int z, int id)
	{
		// Unsummon pet while entering on Observer mode
		if (getPet() != null)
		{
			getPet().unSummon(this);
		}
		
		// Unsummon cubics while entering on Observer mode
		unsummonAllCubics();
		
		if (getParty() != null)
		{
			getParty().removePartyMember(this);
		}
		
		_olympiadGameId = id;
		if (isSitting())
		{
			standUp();
		}
		
		if (!_observerMode)
		{
			_obsX = getX();
			_obsY = getY();
			_obsZ = getZ();
		}
		
		_observerMode = true;
		setTarget(null);
		setInvul(true);
		_wasInvisible = getAppearance().isInvisible();
		getAppearance().setInvisible();
		
		teleToLocation(x, y, z);
		sendPacket(new ExOlympiadMode(3, this));
		broadcastUserInfo();
	}
	
	/**
	 * Leave observer mode.
	 */
	public void leaveObserverMode()
	{
		if (!_observerMode)
		{
			LOGGER.warning("Player " + getName() + " request leave observer mode when he not use it!");
			Util.handleIllegalPlayerAction(this, "Warning!! Character " + getName() + " tried to cheat in observer mode.", Config.DEFAULT_PUNISH);
		}
		setTarget(null);
		setXYZ(_obsX, _obsY, _obsZ);
		setParalyzed(false);
		
		if (_wasInvisible)
		{
			getAppearance().setInvisible();
		}
		else
		{
			getAppearance().setVisible();
		}
		
		setInvul(false);
		
		if (getAI() != null)
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		
		teleToLocation(_obsX, _obsY, _obsZ, false);
		_observerMode = false;
		sendPacket(new ObservationReturn(this));
		if (!_wasInvisible)
		{
			broadcastUserInfo();
		}
	}
	
	/**
	 * Leave olympiad observer mode.
	 */
	public void leaveOlympiadObserverMode()
	{
		setTarget(null);
		sendPacket(new ExOlympiadMode(0, this));
		teleToLocation(_obsX, _obsY, _obsZ, true);
		getAppearance().setVisible();
		setInvul(false);
		if (getAI() != null)
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		Olympiad.getInstance();
		Olympiad.removeSpectator(_olympiadGameId, this);
		_olympiadGameId = -1;
		_observerMode = false;
		if (!_wasInvisible)
		{
			broadcastUserInfo();
		}
	}
	
	/**
	 * Update name title color.
	 */
	public void updateNameTitleColor()
	{
		if (isMarried())
		{
			if (marriedType() == 1)
			{
				getAppearance().setNameColor(Config.WEDDING_NAME_COLOR_LESBO);
			}
			else if (marriedType() == 2)
			{
				getAppearance().setNameColor(Config.WEDDING_NAME_COLOR_GEY);
			}
			else
			{
				getAppearance().setNameColor(Config.WEDDING_NAME_COLOR_NORMAL);
			}
		}
		
		/** Updates title and name color of a donator **/
		if (Config.DONATOR_NAME_COLOR_ENABLED && isDonator())
		{
			getAppearance().setNameColor(Config.DONATOR_NAME_COLOR);
			getAppearance().setTitleColor(Config.DONATOR_TITLE_COLOR);
		}
	}
	
	/**
	 * Sets the olympiad side.
	 * @param value the new olympiad side
	 */
	public void setOlympiadSide(int value)
	{
		_olympiadSide = value;
	}
	
	/**
	 * Gets the olympiad side.
	 * @return the olympiad side
	 */
	public int getOlympiadSide()
	{
		return _olympiadSide;
	}
	
	/**
	 * Sets the olympiad game id.
	 * @param id the new olympiad game id
	 */
	public void setOlympiadGameId(int id)
	{
		_olympiadGameId = id;
	}
	
	/**
	 * Gets the olympiad game id.
	 * @return the olympiad game id
	 */
	public int getOlympiadGameId()
	{
		return _olympiadGameId;
	}
	
	/**
	 * Gets the obs x.
	 * @return the obs x
	 */
	public int getObsX()
	{
		return _obsX;
	}
	
	/**
	 * Gets the obs y.
	 * @return the obs y
	 */
	public int getObsY()
	{
		return _obsY;
	}
	
	/**
	 * Gets the obs z.
	 * @return the obs z
	 */
	public int getObsZ()
	{
		return _obsZ;
	}
	
	/**
	 * In observer mode.
	 * @return true, if successful
	 */
	public boolean inObserverMode()
	{
		return _observerMode;
	}
	
	/**
	 * set observer mode.
	 * @param mode
	 */
	public void setObserverMode(boolean mode)
	{
		_observerMode = mode;
	}
	
	/**
	 * Gets the tele mode.
	 * @return the tele mode
	 */
	public int getTeleMode()
	{
		return _telemode;
	}
	
	/**
	 * Sets the tele mode.
	 * @param mode the new tele mode
	 */
	public void setTeleMode(int mode)
	{
		_telemode = mode;
	}
	
	/**
	 * Sets the loto.
	 * @param i the i
	 * @param value the value
	 */
	public void setLoto(int i, int value)
	{
		_loto[i] = value;
	}
	
	/**
	 * Gets the loto.
	 * @param i the i
	 * @return the loto
	 */
	public int getLoto(int i)
	{
		return _loto[i];
	}
	
	/**
	 * Sets the race.
	 * @param i the i
	 * @param value the value
	 */
	public void setRace(int i, int value)
	{
		_race[i] = value;
	}
	
	/**
	 * Gets the race.
	 * @param i the i
	 * @return the race
	 */
	public int getRace(int i)
	{
		return _race[i];
	}
	
	/**
	 * Send a Server->Client packet StatusUpdate to the Player.
	 */
	@Override
	public void sendPacket(IClientOutgoingPacket packet)
	{
		if (_client != null)
		{
			_client.sendPacket(packet);
		}
	}
	
	/**
	 * Send SystemMessage packet.
	 * @param id
	 */
	public void sendPacket(SystemMessageId id)
	{
		sendPacket(SystemMessage.getSystemMessage(id));
	}
	
	/**
	 * Gets the message refusal.
	 * @return the message refusal
	 */
	public boolean isInRefusalMode()
	{
		return _messageRefusal;
	}
	
	/**
	 * Sets the message refusal.
	 * @param mode the new message refusal
	 */
	public void setInRefusalMode(boolean mode)
	{
		_messageRefusal = mode;
		sendPacket(new EtcStatusUpdate(this));
	}
	
	/**
	 * Sets the diet mode.
	 * @param mode the new diet mode
	 */
	public void setDietMode(boolean mode)
	{
		_dietMode = mode;
	}
	
	/**
	 * Gets the diet mode.
	 * @return the diet mode
	 */
	public boolean getDietMode()
	{
		return _dietMode;
	}
	
	/**
	 * Sets the exchange refusal.
	 * @param mode the new exchange refusal
	 */
	public void setExchangeRefusal(boolean mode)
	{
		_exchangeRefusal = mode;
	}
	
	/**
	 * Gets the exchange refusal.
	 * @return the exchange refusal
	 */
	public boolean getExchangeRefusal()
	{
		return _exchangeRefusal;
	}
	
	/**
	 * Gets the block list.
	 * @return the block list
	 */
	public BlockList getBlockList()
	{
		return _blockList;
	}
	
	/**
	 * Sets the hero aura.
	 * @param heroAura the new hero aura
	 */
	public void setHeroAura(boolean heroAura)
	{
		_isPvpHero = heroAura;
	}
	
	/**
	 * Gets the checks if is pvp hero.
	 * @return the checks if is pvp hero
	 */
	public boolean isPVPHero()
	{
		return _isPvpHero;
	}
	
	/**
	 * Gets the count.
	 * @return the count
	 */
	public int getCount()
	{
		int count = 0;
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT count FROM heroes WHERE char_name=?");
			statement.setString(1, getName());
			final ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				count = rset.getInt("count");
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning(e.toString());
		}
		
		if (count != 0)
		{
			return count;
		}
		return 0;
	}
	
	/**
	 * Reload pvp hero aura.
	 */
	public void reloadPVPHeroAura()
	{
		sendPacket(new UserInfo(this));
	}
	
	/**
	 * Sets the donator.
	 * @param value the new donator
	 */
	public void setDonator(boolean value)
	{
		_donator = value;
	}
	
	/**
	 * Checks if is donator.
	 * @return true, if is donator
	 */
	public boolean isDonator()
	{
		return _donator;
	}
	
	/**
	 * Sets the checks if is in olympiad mode.
	 * @param value the new checks if is in olympiad mode
	 */
	public void setInOlympiadMode(boolean value)
	{
		_inOlympiadMode = value;
	}
	
	/**
	 * Sets the checks if is olympiad start.
	 * @param value the new checks if is olympiad start
	 */
	public void setOlympiadStart(boolean value)
	{
		_olympiadStart = value;
	}
	
	/**
	 * Checks if is olympiad start.
	 * @return true, if is olympiad start
	 */
	public boolean isOlympiadStart()
	{
		return _olympiadStart;
	}
	
	/**
	 * Sets the olympiad position.
	 * @param pos the new olympiad position
	 */
	public void setOlympiadPosition(int[] pos)
	{
		_olympiadPosition = pos;
	}
	
	/**
	 * Gets the olympiad position.
	 * @return the olympiad position
	 */
	public int[] getOlympiadPosition()
	{
		return _olympiadPosition;
	}
	
	/**
	 * Checks if is hero.
	 * @return true, if is hero
	 */
	public boolean isHero()
	{
		return _hero;
	}
	
	/**
	 * Checks if is in olympiad mode.
	 * @return true, if is in olympiad mode
	 */
	public boolean isInOlympiadMode()
	{
		return _inOlympiadMode;
	}
	
	/**
	 * Checks if is in duel.
	 * @return true, if is in duel
	 */
	public boolean isInDuel()
	{
		return _isInDuel;
	}
	
	/**
	 * Gets the duel id.
	 * @return the duel id
	 */
	public int getDuelId()
	{
		return _duelId;
	}
	
	/**
	 * Sets the duel state.
	 * @param mode the new duel state
	 */
	public void setDuelState(int mode)
	{
		_duelState = mode;
	}
	
	/**
	 * Gets the duel state.
	 * @return the duel state
	 */
	public int getDuelState()
	{
		return _duelState;
	}
	
	/**
	 * Sets the coupon.
	 * @param coupon the new coupon
	 */
	public void setCoupon(int coupon)
	{
		if ((coupon >= 0) && (coupon <= 3))
		{
			_hasCoupon = coupon;
		}
	}
	
	/**
	 * Adds the coupon.
	 * @param coupon the coupon
	 */
	public void addCoupon(int coupon)
	{
		if ((coupon == 1) || ((coupon == 2) && !getCoupon(coupon - 1)))
		{
			_hasCoupon += coupon;
		}
	}
	
	/**
	 * Gets the coupon.
	 * @param coupon the coupon
	 * @return the coupon
	 */
	public boolean getCoupon(int coupon)
	{
		return (((_hasCoupon == 1) || (_hasCoupon == 3)) && (coupon == 0)) || (((_hasCoupon == 2) || (_hasCoupon == 3)) && (coupon == 1));
	}
	
	/**
	 * Sets up the duel state using a non 0 duelId.
	 * @param duelId 0=not in a duel
	 */
	public void setInDuel(int duelId)
	{
		if (duelId > 0)
		{
			_isInDuel = true;
			_duelState = Duel.DUELSTATE_DUELLING;
			_duelId = duelId;
		}
		else
		{
			if (_duelState == Duel.DUELSTATE_DEAD)
			{
				enableAllSkills();
				getStatus().startHpMpRegeneration();
			}
			_isInDuel = false;
			_duelState = Duel.DUELSTATE_NODUEL;
			_duelId = 0;
		}
	}
	
	/**
	 * This returns a SystemMessage stating why the player is not available for duelling.
	 * @return S1_CANNOT_DUEL... message
	 */
	public SystemMessage getNoDuelReason()
	{
		final SystemMessage sm = new SystemMessage(_noDuelReason);
		sm.addString(getName());
		_noDuelReason = SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL;
		return sm;
	}
	
	/**
	 * Checks if this player might join / start a duel. To get the reason use getNoDuelReason() after calling this function.
	 * @return true if the player might join/start a duel.
	 */
	public boolean canDuel()
	{
		if (isInCombat() || isInJail())
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_ENGAGED_IN_BATTLE;
			return false;
		}
		if (isDead() || isAlikeDead() || (getCurrentHp() < (getMaxHp() / 2)) || (getCurrentMp() < (getMaxMp() / 2)))
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_S_HP_OR_MP_IS_BELOW_50_PERCENT;
			return false;
		}
		if (isInDuel())
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_ALREADY_ENGAGED_IN_A_DUEL;
			return false;
		}
		if (isInOlympiadMode())
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_PARTICIPATING_IN_THE_OLYMPIAD;
			return false;
		}
		if (isCursedWeaponEquiped())
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_IN_A_CHAOTIC_STATE;
			return false;
		}
		if (getPrivateStoreType() != STORE_PRIVATE_NONE)
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_ENGAGED_IN_A_PRIVATE_STORE_OR_MANUFACTURE;
			return false;
		}
		if (isMounted() || isInBoat())
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_RIDING_A_BOAT_WYVERN_OR_STRIDER;
			return false;
		}
		if (isFishing())
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_FISHING;
			return false;
		}
		if (isInsideZone(ZoneId.PVP) || isInsideZone(ZoneId.PEACE) || isInsideZone(ZoneId.SIEGE))
		{
			_noDuelReason = SystemMessageId.S1_CANNOT_MAKE_A_CHALLENGE_TO_A_DUEL_BECAUSE_S1_IS_CURRENTLY_IN_A_DUEL_PROHIBITED_AREA_PEACEFUL_ZONE_SEVEN_SIGNS_ZONE_NEAR_WATER_RESTART_PROHIBITED_AREA;
			return false;
		}
		return true;
	}
	
	/**
	 * Checks if is noble.
	 * @return true, if is noble
	 */
	public boolean isNoble()
	{
		return _noble;
	}
	
	/**
	 * Sets the noble.
	 * @param value the new noble
	 */
	public void setNoble(boolean value)
	{
		if (value)
		{
			for (Skill s : NobleSkillTable.getNobleSkills())
			{
				addSkill(s, false); // Dont Save Noble skills to Sql
			}
		}
		else
		{
			for (Skill s : NobleSkillTable.getNobleSkills())
			{
				super.removeSkill(s); // Just Remove skills without deleting from Sql
			}
		}
		_noble = value;
		sendSkillList();
	}
	
	/**
	 * Adds the clan leader skills.
	 * @param value the value
	 */
	public void addClanLeaderSkills(boolean value)
	{
		if (value)
		{
			SiegeManager.getInstance().addSiegeSkills(this);
		}
		else
		{
			SiegeManager.getInstance().removeSiegeSkills(this);
		}
		sendSkillList();
	}
	
	/**
	 * Sets the lvl joined academy.
	 * @param lvl the new lvl joined academy
	 */
	public void setLvlJoinedAcademy(int lvl)
	{
		_lvlJoinedAcademy = lvl;
	}
	
	/**
	 * Gets the lvl joined academy.
	 * @return the lvl joined academy
	 */
	public int getLvlJoinedAcademy()
	{
		return _lvlJoinedAcademy;
	}
	
	/**
	 * Checks if is academy member.
	 * @return true, if is academy member
	 */
	public boolean isAcademyMember()
	{
		return _lvlJoinedAcademy > 0;
	}
	
	public boolean isRegisteredOnEvent()
	{
		return _isRegisteredOnEvent || _isOnEvent;
	}
	
	public void setRegisteredOnEvent(boolean value)
	{
		_isRegisteredOnEvent = value;
	}
	
	@Override
	public boolean isOnEvent()
	{
		return _isOnEvent;
	}
	
	public void setOnEvent(boolean value)
	{
		_isOnEvent = value;
	}
	
	public boolean isOnSoloEvent()
	{
		return _isOnSoloEvent;
	}
	
	public void setOnSoloEvent(boolean value)
	{
		_isOnSoloEvent = value;
	}
	
	/**
	 * Sets the team.
	 * @param team the new team
	 */
	public void setTeam(int team)
	{
		_team = team;
	}
	
	/**
	 * Gets the team.
	 * @return the team
	 */
	public int getTeam()
	{
		return _team;
	}
	
	/**
	 * Sets the wants peace.
	 * @param wantsPeace the new wants peace
	 */
	public void setWantsPeace(int wantsPeace)
	{
		_wantsPeace = wantsPeace;
	}
	
	/**
	 * Gets the wants peace.
	 * @return the wants peace
	 */
	public int getWantsPeace()
	{
		return _wantsPeace;
	}
	
	/**
	 * Checks if is fishing.
	 * @return true, if is fishing
	 */
	public boolean isFishing()
	{
		return _fishing;
	}
	
	/**
	 * Sets the fishing.
	 * @param fishing the new fishing
	 */
	public void setFishing(boolean fishing)
	{
		_fishing = fishing;
	}
	
	/**
	 * Sets the alliance with varka ketra.
	 * @param sideAndLvlOfAlliance the new alliance with varka ketra
	 */
	public void setAllianceWithVarkaKetra(int sideAndLvlOfAlliance)
	{
		// [-5,-1] varka, 0 neutral, [1,5] ketra
		_alliedVarkaKetra = sideAndLvlOfAlliance;
	}
	
	/**
	 * Gets the alliance with varka ketra.
	 * @return the alliance with varka ketra
	 */
	public int getAllianceWithVarkaKetra()
	{
		return _alliedVarkaKetra;
	}
	
	/**
	 * Checks if is allied with varka.
	 * @return true, if is allied with varka
	 */
	public boolean isAlliedWithVarka()
	{
		return _alliedVarkaKetra < 0;
	}
	
	/**
	 * Checks if is allied with ketra.
	 * @return true, if is allied with ketra
	 */
	public boolean isAlliedWithKetra()
	{
		return _alliedVarkaKetra > 0;
	}
	
	/**
	 * Send skill list.
	 */
	public void sendSkillList()
	{
		sendSkillList(this);
	}
	
	/**
	 * Send skill list.
	 * @param player the player
	 */
	public void sendSkillList(Player player)
	{
		final SkillList sl = new SkillList();
		if (player != null)
		{
			for (Skill s : player.getAllSkills())
			{
				if (s == null)
				{
					continue;
				}
				
				// Fake skills to change base stats.
				if (s.getId() > 9000)
				{
					continue;
				}
				
				if (s.bestowed())
				{
					continue;
				}
				
				if (s.isChance())
				{
					sl.addSkill(s.getId(), s.getLevel(), s.isChance());
				}
				else
				{
					sl.addSkill(s.getId(), s.getLevel(), s.isPassive());
				}
			}
		}
		sendPacket(sl);
	}
	
	/**
	 * 1. Add the specified class ID as a subclass (up to the maximum number of <b>three</b>) for this character.<br>
	 * 2. This method no longer changes the active _classIndex of the player. This is only done by the calling of setActiveClass() method as that should be the only way to do so.
	 * @param classId the class id
	 * @param classIndex the class index
	 * @return boolean subclassAdded
	 */
	public synchronized boolean addSubClass(int classId, int classIndex)
	{
		// Reload skills from armors / jewels / weapons
		getInventory().reloadEquippedItems();
		
		// Remove Item RHAND
		if (Config.REMOVE_WEAPON_SUBCLASS)
		{
			final Item rhand = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
			if (rhand != null)
			{
				final InventoryUpdate iu = new InventoryUpdate();
				for (Item element : getInventory().unEquipItemInBodySlotAndRecord(rhand.getItem().getBodyPart()))
				{
					iu.addModifiedItem(element);
				}
				sendPacket(iu);
			}
		}
		
		// Remove Item CHEST
		if (Config.REMOVE_CHEST_SUBCLASS)
		{
			final Item chest = getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
			if (chest != null)
			{
				final InventoryUpdate iu = new InventoryUpdate();
				for (Item element : getInventory().unEquipItemInBodySlotAndRecord(chest.getItem().getBodyPart()))
				{
					iu.addModifiedItem(element);
				}
				sendPacket(iu);
			}
		}
		
		// Remove Item LEG
		if (Config.REMOVE_LEG_SUBCLASS)
		{
			final Item legs = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS);
			if (legs != null)
			{
				final InventoryUpdate iu = new InventoryUpdate();
				for (Item element : getInventory().unEquipItemInBodySlotAndRecord(legs.getItem().getBodyPart()))
				{
					iu.addModifiedItem(element);
				}
				sendPacket(iu);
			}
		}
		
		if ((getTotalSubClasses() == Config.ALLOWED_SUBCLASS) || (classIndex == 0))
		{
			return false;
		}
		
		if (getSubClasses().containsKey(classIndex))
		{
			return false;
		}
		
		// Note: Never change _classIndex in any method other than setActiveClass().
		
		final SubClass newClass = new SubClass();
		newClass.setClassId(classId);
		newClass.setClassIndex(classIndex);
		
		boolean output = false;
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement(ADD_CHAR_SUBCLASS);
			statement.setInt(1, getObjectId());
			statement.setInt(2, newClass.getClassId());
			statement.setLong(3, newClass.getExp());
			statement.setInt(4, newClass.getSp());
			statement.setInt(5, newClass.getLevel());
			statement.setInt(6, newClass.getClassIndex()); // <-- Added
			statement.execute();
			statement.close();
			
			output = true;
		}
		catch (Exception e)
		{
			LOGGER.warning("WARNING: Could not add character sub class for " + getName() + ": " + e);
		}
		
		if (output)
		{
			// Commit after database INSERT incase exception is thrown.
			getSubClasses().put(newClass.getClassIndex(), newClass);
			
			final ClassId subTemplate = ClassId.getClassId(classId);
			final Collection<SkillLearn> skillTree = SkillTreeTable.getInstance().getAllowedSkills(subTemplate);
			if (skillTree == null)
			{
				return true;
			}
			
			final Map<Integer, Skill> prevSkillList = new HashMap<>();
			for (SkillLearn skillInfo : skillTree)
			{
				if (skillInfo.getMinLevel() <= 40)
				{
					final Skill prevSkill = prevSkillList.get(skillInfo.getId());
					final Skill newSkill = SkillTable.getInstance().getSkill(skillInfo.getId(), skillInfo.getLevel());
					if ((newSkill == null) || ((prevSkill != null) && (prevSkill.getLevel() > newSkill.getLevel())))
					{
						continue;
					}
					
					prevSkillList.put(newSkill.getId(), newSkill);
					storeSkill(newSkill, prevSkill, classIndex);
				}
			}
		}
		
		return output;
	}
	
	/**
	 * 1. Completely erase all existance of the subClass linked to the classIndex.<br>
	 * 2. Send over the newClassId to addSubClass()to create a new instance on this classIndex.<br>
	 * 3. Upon Exception, revert the player to their BaseClass to avoid further problems.
	 * @param classIndex the class index
	 * @param newClassId the new class id
	 * @return boolean subclassAdded
	 */
	public boolean modifySubClass(int classIndex, int newClassId)
	{
		boolean output = false;
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			
			// Remove all henna info stored for this sub-class.
			statement = con.prepareStatement(DELETE_CHAR_HENNAS);
			statement.setInt(1, getObjectId());
			statement.setInt(2, classIndex);
			statement.execute();
			statement.close();
			
			// Remove all shortcuts info stored for this sub-class.
			statement = con.prepareStatement(DELETE_CHAR_SHORTCUTS);
			statement.setInt(1, getObjectId());
			statement.setInt(2, classIndex);
			statement.execute();
			statement.close();
			
			// Remove all effects info stored for this sub-class.
			statement = con.prepareStatement(DELETE_SKILL_SAVE);
			statement.setInt(1, getObjectId());
			statement.setInt(2, classIndex);
			statement.execute();
			statement.close();
			
			// Remove all skill info stored for this sub-class.
			statement = con.prepareStatement(DELETE_CHAR_SKILLS);
			statement.setInt(1, getObjectId());
			statement.setInt(2, classIndex);
			statement.execute();
			statement.close();
			
			// Remove all basic info stored about this sub-class.
			statement = con.prepareStatement(DELETE_CHAR_SUBCLASS);
			statement.setInt(1, getObjectId());
			statement.setInt(2, classIndex);
			statement.execute();
			statement.close();
			
			output = true;
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not modify sub class for " + getName() + " to class index " + classIndex + ": " + e);
		}
		
		getSubClasses().remove(classIndex);
		
		if (output)
		{
			return addSubClass(newClassId, classIndex);
		}
		return false;
	}
	
	/**
	 * Checks if is sub class active.
	 * @return true, if is sub class active
	 */
	public boolean isSubClassActive()
	{
		return _classIndex > 0;
	}
	
	/**
	 * Gets the sub classes.
	 * @return the sub classes
	 */
	public Map<Integer, SubClass> getSubClasses()
	{
		return _subClasses;
	}
	
	/**
	 * Gets the total sub classes.
	 * @return the total sub classes
	 */
	public int getTotalSubClasses()
	{
		return getSubClasses().size();
	}
	
	/**
	 * Gets the base class.
	 * @return the base class
	 */
	public int getBaseClass()
	{
		return _baseClass;
	}
	
	/**
	 * Gets the active class.
	 * @return the active class
	 */
	public synchronized int getActiveClass()
	{
		return _activeClass;
	}
	
	/**
	 * Gets the class index.
	 * @return the class index
	 */
	public int getClassIndex()
	{
		return _classIndex;
	}
	
	/**
	 * Sets the class template.
	 * @param classId the new class template
	 */
	private synchronized void setClassTemplate(int classId)
	{
		_activeClass = classId;
		
		final PlayerTemplate t = PlayerTemplateData.getInstance().getTemplate(classId);
		if (t == null)
		{
			LOGGER.warning("Missing template for classId: " + classId);
			throw new Error();
		}
		
		// Set the template of the Player
		setTemplate(t);
	}
	
	/**
	 * Changes the character's class based on the given class index.<br>
	 * An index of zero specifies the character's original (base) class, while indexes 1-3 specifies the character's sub-classes respectively.
	 * @param classIndex the class index
	 */
	public synchronized void setActiveClass(int classIndex)
	{
		if (isInCombat() || (getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK))
		{
			sendMessage("Impossible switch class if in combat");
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Delete a force buff upon class change.
		if (_forceBuff != null)
		{
			abortCast();
		}
		
		// 1. Call store() before modifying _classIndex to avoid skill effects rollover. 2. Register the correct _classId against applied 'classIndex'.
		store();
		
		if (classIndex == 0)
		{
			setClassTemplate(getBaseClass());
		}
		else
		{
			try
			{
				setClassTemplate(getSubClasses().get(classIndex).getClassId());
			}
			catch (Exception e)
			{
				LOGGER.info("Could not switch " + getName() + "'s sub class to class index " + classIndex + ": " + e);
				return;
			}
		}
		_classIndex = classIndex;
		if (isInParty())
		{
			getParty().recalculatePartyLevel();
		}
		
		if (getPet() instanceof Servitor)
		{
			getPet().unSummon(this);
		}
		
		unsummonAllCubics();
		
		synchronized (getAllSkills())
		{
			for (Skill oldSkill : getAllSkills())
			{
				super.removeSkill(oldSkill);
			}
		}
		
		// Rebind CursedWeapon passive.
		if (isCursedWeaponEquiped())
		{
			CursedWeaponsManager.getInstance().givePassive(_cursedWeaponEquipedId);
		}
		
		stopAllEffects();
		
		if (isSubClassActive())
		{
			_dwarvenRecipeBook.clear();
			_commonRecipeBook.clear();
		}
		else
		{
			restoreRecipeBook();
		}
		
		// Restore any Death Penalty Buff
		restoreDeathPenaltyBuffLevel();
		
		restoreSkills();
		regiveTemporarySkills();
		rewardSkills();
		restoreEffects(Config.ALT_RESTORE_EFFECTS_ON_SUBCLASS_CHANGE);
		
		// Reload skills from armors / jewels / weapons
		getInventory().reloadEquippedItems();
		
		// Remove Item RHAND
		if (Config.REMOVE_WEAPON_SUBCLASS)
		{
			final Item rhand = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
			if (rhand != null)
			{
				final InventoryUpdate iu = new InventoryUpdate();
				for (Item element : getInventory().unEquipItemInBodySlotAndRecord(rhand.getItem().getBodyPart()))
				{
					iu.addModifiedItem(element);
				}
				sendPacket(iu);
			}
		}
		
		// Remove Item CHEST
		if (Config.REMOVE_CHEST_SUBCLASS)
		{
			final Item chest = getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
			if (chest != null)
			{
				final InventoryUpdate iu = new InventoryUpdate();
				for (Item element : getInventory().unEquipItemInBodySlotAndRecord(chest.getItem().getBodyPart()))
				{
					iu.addModifiedItem(element);
				}
				sendPacket(iu);
			}
		}
		
		// Remove Item LEG
		if (Config.REMOVE_LEG_SUBCLASS)
		{
			final Item legs = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS);
			if (legs != null)
			{
				final InventoryUpdate iu = new InventoryUpdate();
				for (Item element : getInventory().unEquipItemInBodySlotAndRecord(legs.getItem().getBodyPart()))
				{
					iu.addModifiedItem(element);
				}
				sendPacket(iu);
			}
		}
		
		// Check player skills
		if (Config.CHECK_SKILLS_ON_ENTER && !Config.ALT_GAME_SKILL_LEARN)
		{
			checkAllowedSkills();
		}
		
		sendPacket(new EtcStatusUpdate(this));
		
		// if player has quest 422: Repent Your Sins, remove it
		final QuestState st = getQuestState("422_RepentYourSins");
		if (st != null)
		{
			st.exitQuest(true);
		}
		
		for (int i = 0; i < 3; i++)
		{
			_henna[i] = null;
		}
		
		restoreHenna();
		sendPacket(new HennaInfo(this));
		if (getCurrentHp() > getMaxHp())
		{
			setCurrentHp(getMaxHp());
		}
		
		if (getCurrentMp() > getMaxMp())
		{
			setCurrentMp(getMaxMp());
		}
		
		if (getCurrentCp() > getMaxCp())
		{
			setCurrentCp(getMaxCp());
		}
		
		// Refresh player infos and update new status
		broadcastUserInfo();
		refreshOverloaded();
		refreshExpertisePenalty();
		refreshMasteryPenality();
		refreshMasteryWeapPenality();
		sendPacket(new UserInfo(this));
		sendPacket(new ItemList(this, false));
		getInventory().refreshWeight();
		
		// Clear resurrect xp calculation
		setExpBeforeDeath(0);
		_macroses.restore();
		_macroses.sendUpdate();
		_shortCuts.restore();
		sendPacket(new ShortCutInit(this));
		
		// Rebirth Caller - if player has any skills, they will be granted them.
		if (Config.REBIRTH_ENABLE)
		{
			RebirthManager.getInstance().grantRebirthSkills(this);
		}
		
		broadcastPacket(new SocialAction(getObjectId(), 15));
		sendPacket(new SkillCoolTime(this));
		if (getClan() != null)
		{
			getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));
		}
	}
	
	/**
	 * Broadcast class icon.
	 */
	public void broadcastClassIcon()
	{
		// Update class icon in party and clan
		if (isInParty())
		{
			getParty().broadcastToPartyMembers(new PartySmallWindowUpdate(this));
		}
		
		if (getClan() != null)
		{
			getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));
		}
	}
	
	/**
	 * Stop warn user take break.
	 */
	public void stopWarnUserTakeBreak()
	{
		if (_taskWarnUserTakeBreak != null)
		{
			_taskWarnUserTakeBreak.cancel(true);
			_taskWarnUserTakeBreak = null;
		}
	}
	
	/**
	 * Start warn user take break.
	 */
	public void startWarnUserTakeBreak()
	{
		if (_taskWarnUserTakeBreak == null)
		{
			_taskWarnUserTakeBreak = ThreadPool.scheduleAtFixedRate(new WarnUserTakeBreak(), 7200000, 7200000);
		}
	}
	
	/**
	 * Stop rent pet.
	 */
	public void stopRentPet()
	{
		if (_taskRentPet != null)
		{
			// if the rent of a wyvern expires while over a flying zone, tp to down before unmounting
			if (checkLandingState() && (getMountType() == 2))
			{
				teleToLocation(TeleportWhereType.TOWN);
			}
			
			if (setMountType(0)) // this should always be true now, since we teleported already
			{
				_taskRentPet.cancel(true);
				_taskRentPet = null;
				final Ride dismount = new Ride(getObjectId(), Ride.ACTION_DISMOUNT, 0);
				sendPacket(dismount);
				broadcastPacket(dismount);
			}
		}
	}
	
	/**
	 * Start rent pet.
	 * @param seconds the seconds
	 */
	public void startRentPet(int seconds)
	{
		if (_taskRentPet == null)
		{
			_taskRentPet = ThreadPool.scheduleAtFixedRate(new RentPetTask(), seconds * 1000, seconds * 1000);
		}
	}
	
	/**
	 * Checks if is rented pet.
	 * @return true, if is rented pet
	 */
	public boolean isRentedPet()
	{
		return _taskRentPet != null;
	}
	
	/**
	 * Stop water task.
	 */
	public void stopWaterTask()
	{
		if (_taskWater != null)
		{
			_taskWater.cancel(false);
			_taskWater = null;
			sendPacket(new SetupGauge(2, 0));
			// for catacombs...
			broadcastUserInfo();
		}
	}
	
	/**
	 * Start water task.
	 */
	public void startWaterTask()
	{
		broadcastUserInfo();
		if (!isDead() && (_taskWater == null))
		{
			final int timeinwater = 86000;
			sendPacket(new SetupGauge(2, timeinwater));
			_taskWater = ThreadPool.scheduleAtFixedRate(new WaterTask(), timeinwater, 1000);
		}
	}
	
	/**
	 * Checks if is in water.
	 * @return true, if is in water
	 */
	public boolean isInWater()
	{
		return _taskWater != null;
	}
	
	/**
	 * Check water state.
	 */
	public void checkWaterState()
	{
		if (isInsideZone(ZoneId.WATER))
		{
			startWaterTask();
		}
		else
		{
			stopWaterTask();
		}
	}
	
	/**
	 * On player enter.
	 */
	public void onPlayerEnter()
	{
		startWarnUserTakeBreak();
		
		if (SevenSigns.getInstance().isSealValidationPeriod() || SevenSigns.getInstance().isCompResultsPeriod())
		{
			if (!isGM() && isIn7sDungeon() && (SevenSigns.getInstance().getPlayerCabal(this) != SevenSigns.getInstance().getCabalHighestScore()))
			{
				teleToLocation(TeleportWhereType.TOWN);
				setIn7sDungeon(false);
				sendMessage("You have been teleported to the nearest town due to the beginning of the Seal Validation period.");
			}
		}
		else if (!isGM() && isIn7sDungeon() && (SevenSigns.getInstance().getPlayerCabal(this) == SevenSigns.CABAL_NULL))
		{
			teleToLocation(TeleportWhereType.TOWN);
			setIn7sDungeon(false);
			sendMessage("You have been teleported to the nearest town because you have not signed for any cabal.");
		}
		
		// jail task
		updatePunishState();
		
		if (isGM() && !Config.GM_STARTUP_BUILDER_HIDE)
		{
			// Bleah, see L2J custom below.
			if (_isInvul)
			{
				sendMessage("Entering world in Invulnerable mode.");
			}
			
			if (getAppearance().isInvisible())
			{
				sendMessage("Entering world in Invisible mode.");
			}
			
			if (isInRefusalMode())
			{
				sendMessage("Entering world in Message Refusal mode.");
			}
		}
		
		revalidateZone(true);
		
		notifyFriends(false);
		
		// Fix against exploit on anti-target on login
		decayMe();
		spawnMe();
		broadcastUserInfo();
	}
	
	/**
	 * Gets the last access.
	 * @return the last access
	 */
	public long getLastAccess()
	{
		return _lastAccess;
	}
	
	/**
	 * Check recom.
	 * @param recsHave the recs have
	 * @param recsLeft the recs left
	 */
	private void checkRecom(int recsHave, int recsLeft)
	{
		final Calendar check = Calendar.getInstance();
		check.setTimeInMillis(_lastRecomUpdate);
		check.add(Calendar.DAY_OF_MONTH, 1);
		
		final Calendar min = Calendar.getInstance();
		_recomHave = recsHave;
		_recomLeft = recsLeft;
		if ((getStat().getLevel() < 10) || check.after(min))
		{
			return;
		}
		
		restartRecom();
	}
	
	/**
	 * Restart recom.
	 */
	public void restartRecom()
	{
		if (Config.ALT_RECOMMEND)
		{
			try (Connection con = DatabaseFactory.getConnection())
			{
				final PreparedStatement statement = con.prepareStatement(DELETE_CHAR_RECOMS);
				statement.setInt(1, getObjectId());
				statement.execute();
				statement.close();
				
				_recomChars.clear();
			}
			catch (Exception e)
			{
				LOGGER.warning("could not clear char recommendations: " + e);
			}
		}
		
		if (getStat().getLevel() < 20)
		{
			_recomLeft = 3;
			_recomHave--;
		}
		else if (getStat().getLevel() < 40)
		{
			_recomLeft = 6;
			_recomHave -= 2;
		}
		else
		{
			_recomLeft = 9;
			_recomHave -= 3;
		}
		
		if (_recomHave < 0)
		{
			_recomHave = 0;
		}
		
		// If we have to update last update time, but it's now before 13, we should set it to yesterday
		final Calendar update = Calendar.getInstance();
		if (update.get(Calendar.HOUR_OF_DAY) < 13)
		{
			update.add(Calendar.DAY_OF_MONTH, -1);
		}
		
		update.set(Calendar.HOUR_OF_DAY, 13);
		_lastRecomUpdate = update.getTimeInMillis();
	}
	
	@Override
	public void doRevive()
	{
		super.doRevive();
		
		updateEffectIcons();
		sendPacket(new EtcStatusUpdate(this));
		_reviveRequested = 0;
		_revivePower = 0;
		
		// Teleport summon to player.
		if (isInsideZone(ZoneId.PEACE) && (_summon != null) && !_summon.isInsideZone(ZoneId.SIEGE))
		{
			_summon.teleToLocation(getLocation(), true);
		}
		
		if (isInParty() && getParty().isInDimensionalRift() && !DimensionalRiftManager.getInstance().checkIfInPeaceZone(getX(), getY(), getZ()))
		{
			getParty().getDimensionalRift().memberRessurected(this);
		}
		
		if (isOnEvent())
		{
			getStatus().setCurrentHp(getMaxHp());
			getStatus().setCurrentMp(getMaxMp());
			getStatus().setCurrentCp(getMaxCp());
		}
	}
	
	@Override
	public void doRevive(double revivePower)
	{
		// Restore the player's lost experience, depending on the % return of the skill used (based on its power).
		restoreExp(revivePower);
		doRevive();
	}
	
	/**
	 * Revive request.
	 * @param reviver the reviver
	 * @param skill the skill
	 * @param pet the pet
	 */
	public void reviveRequest(Player reviver, Skill skill, boolean pet)
	{
		if (_reviveRequested == 1)
		{
			if (_revivePet == pet)
			{
				reviver.sendPacket(SystemMessageId.RESURRECTION_HAS_ALREADY_BEEN_PROPOSED); // Resurrection is already been proposed.
			}
			else if (pet)
			{
				reviver.sendPacket(SystemMessageId.A_PET_CANNOT_BE_RESURRECTED_WHILE_IT_S_OWNER_IS_IN_THE_PROCESS_OF_RESURRECTING); // A pet cannot be resurrected while it's owner is in the process of resurrecting.
			}
			else
			{
				reviver.sendPacket(SystemMessageId.WHILE_A_PET_IS_ATTEMPTING_TO_RESURRECT_IT_CANNOT_HELP_IN_RESURRECTING_ITS_MASTER); // While a pet is attempting to resurrect, it cannot help in resurrecting its master.
			}
			return;
		}
		if ((pet && (getPet() != null) && getPet().isDead()) || (!pet && isDead()))
		{
			_reviveRequested = 1;
			if (isPhoenixBlessed())
			{
				_revivePower = 100;
			}
			else if (skill != null)
			{
				_revivePower = Formulas.getInstance().calculateSkillResurrectRestorePercent(skill.getPower(), reviver);
			}
			else
			{
				_revivePower = 0;
			}
			_revivePet = pet;
			final ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.S1_IS_MAKING_AN_ATTEMPT_AT_RESURRECTION_DO_YOU_WANT_TO_CONTINUE_WITH_THIS_RESURRECTION.getId());
			dlg.addString(reviver.getName());
			sendPacket(dlg);
		}
	}
	
	/**
	 * Revive answer.
	 * @param answer the answer
	 */
	public void reviveAnswer(int answer)
	{
		if ((_reviveRequested != 1) || (!isDead() && !_revivePet) || (_revivePet && (getPet() != null) && !getPet().isDead()))
		{
			return;
		}
		// If character refuse a PhoenixBlessed autoress, cancel all buffs he had
		if ((answer == 0) && isPhoenixBlessed())
		{
			stopPhoenixBlessing(null);
			stopAllEffects();
		}
		if (answer == 1)
		{
			if (!_revivePet)
			{
				if (_revivePower != 0)
				{
					doRevive(_revivePower);
				}
				else
				{
					doRevive();
				}
			}
			else if (getPet() != null)
			{
				if (_revivePower != 0)
				{
					getPet().doRevive(_revivePower);
				}
				else
				{
					getPet().doRevive();
				}
			}
		}
		_reviveRequested = 0;
		_revivePower = 0;
	}
	
	/**
	 * Checks if is revive requested.
	 * @return true, if is revive requested
	 */
	public boolean isReviveRequested()
	{
		return _reviveRequested == 1;
	}
	
	/**
	 * Checks if is reviving pet.
	 * @return true, if is reviving pet
	 */
	public boolean isRevivingPet()
	{
		return _revivePet;
	}
	
	/**
	 * Removes the reviving.
	 */
	public void removeReviving()
	{
		_reviveRequested = 0;
		_revivePower = 0;
	}
	
	/**
	 * On action request.
	 */
	public void onActionRequest()
	{
		if (isSpawnProtected())
		{
			setProtection(false);
			if (!isInsideZone(ZoneId.PEACE))
			{
				sendMessage("You are no longer protected from aggressive monsters.");
			}
		}
		if (isTeleportProtected())
		{
			setTeleportProtection(false);
			if (!isInsideZone(ZoneId.PEACE))
			{
				sendMessage("Teleport spawn protection ended.");
			}
		}
	}
	
	/**
	 * Sets the expertise index.
	 * @param expertiseIndex The expertiseIndex to set.
	 */
	public void setExpertiseIndex(int expertiseIndex)
	{
		_expertiseIndex = expertiseIndex;
	}
	
	/**
	 * Gets the expertise index.
	 * @return Returns the expertiseIndex.
	 */
	public int getExpertiseIndex()
	{
		return _expertiseIndex;
	}
	
	@Override
	public void onTeleported()
	{
		super.onTeleported();
		
		// Force a revalidation
		revalidateZone(true);
		
		if ((Config.PLAYER_TELEPORT_PROTECTION > 0) && !isInOlympiadMode())
		{
			setTeleportProtection(true);
			sendMessage("The effects of Teleport Spawn Protection flow through you.");
		}
		
		if (Config.ALLOW_WATER)
		{
			checkWaterState();
		}
		
		// Modify the position of the tamed beast if necessary (normal pets are handled by super...though Player is the only class that actually has pets!!!)
		if (getTrainedBeast() != null)
		{
			getTrainedBeast().getAI().stopFollow();
			getTrainedBeast().teleToLocation(getPosition().getX() + Rnd.get(-100, 100), getPosition().getY() + Rnd.get(-100, 100), getPosition().getZ());
			getTrainedBeast().getAI().startFollow(this);
		}
		
		// To be sure update also the pvp flag / war tag status
		if (!inObserverMode())
		{
			broadcastUserInfo();
		}
	}
	
	@Override
	public boolean updatePosition(int gameTicks)
	{
		// Get movement data
		final MoveData m = _move;
		if (_move == null)
		{
			return true;
		}
		
		if (!isSpawned())
		{
			_move = null;
			return true;
		}
		
		// Check if the position has alreday be calculated
		if (m._moveTimestamp == 0)
		{
			m._moveTimestamp = m._moveStartTime;
		}
		
		// Check if the position has alreday be calculated
		if (m._moveTimestamp == gameTicks)
		{
			return false;
		}
		
		final double dx = m._xDestination - getX();
		final double dy = m._yDestination - getY();
		final double dz = m._zDestination - getZ();
		final int distPassed = ((int) getStat().getMoveSpeed() * (gameTicks - m._moveTimestamp)) / GameTimeTaskManager.TICKS_PER_SECOND;
		final double distFraction = distPassed / Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
		if (distFraction > 1)
		{
			// Set the position of the Creature to the destination
			super.setXYZ(m._xDestination, m._yDestination, m._zDestination);
		}
		else
		{
			// Set the position of the Creature to estimated after parcial move
			super.setXYZ(getX() + (int) ((dx * distFraction) + 0.5), getY() + (int) ((dy * distFraction) + 0.5), getZ() + (int) (dz * distFraction));
		}
		
		// Set the timer of last position update to now
		m._moveTimestamp = gameTicks;
		revalidateZone(false);
		
		return distFraction > 1;
	}
	
	/**
	 * Sets the last client position.
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void setLastClientPosition(int x, int y, int z)
	{
		_lastClientPosition.setXYZ(x, y, z);
	}
	
	/**
	 * Sets the last client position.
	 * @param loc the new last client position
	 */
	public void setLastClientPosition(Location loc)
	{
		_lastClientPosition = loc;
	}
	
	/**
	 * Check last client position.
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @return true, if successful
	 */
	public boolean checkLastClientPosition(int x, int y, int z)
	{
		return _lastClientPosition.equals(x, y, z);
	}
	
	/**
	 * Gets the last client distance.
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @return the last client distance
	 */
	public int getLastClientDistance(int x, int y, int z)
	{
		final double dx = x - _lastClientPosition.getX();
		final double dy = y - _lastClientPosition.getY();
		final double dz = z - _lastClientPosition.getZ();
		return (int) Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
	}
	
	public Location getLastServerPosition()
	{
		return _lastServerPosition;
	}
	
	/**
	 * Sets the last server position.
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void setLastServerPosition(int x, int y, int z)
	{
		_lastServerPosition.setXYZ(x, y, z);
	}
	
	/**
	 * Sets the last server position.
	 * @param loc the new last server position
	 */
	public void setLastServerPosition(Location loc)
	{
		_lastServerPosition = loc;
	}
	
	/**
	 * Check last server position.
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @return true, if successful
	 */
	public boolean checkLastServerPosition(int x, int y, int z)
	{
		return _lastServerPosition.equals(x, y, z);
	}
	
	/**
	 * Gets the last server distance.
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @return the last server distance
	 */
	public int getLastServerDistance(int x, int y, int z)
	{
		final double dx = x - _lastServerPosition.getX();
		final double dy = y - _lastServerPosition.getY();
		final double dz = z - _lastServerPosition.getZ();
		return (int) Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
	}
	
	@Override
	public void addExpAndSp(long addToExp, int addToSp)
	{
		getStat().addExpAndSp(addToExp, addToSp);
	}
	
	/**
	 * Removes the exp and sp.
	 * @param removeExp the remove exp
	 * @param removeSp the remove sp
	 */
	public void removeExpAndSp(long removeExp, int removeSp)
	{
		getStat().removeExpAndSp(removeExp, removeSp);
	}
	
	@Override
	public void reduceCurrentHp(double i, Creature attacker)
	{
		getStatus().reduceHp(i, attacker);
		
		// notify the tamed beast of attacks
		if (getTrainedBeast() != null)
		{
			getTrainedBeast().onOwnerGotAttacked(attacker);
		}
	}
	
	/**
	 * Request Teleport action.
	 * @param requester the requester
	 * @param skill the skill
	 * @return true, if successful
	 */
	public boolean teleportRequest(Player requester, Skill skill)
	{
		if ((_summonRequest.getSummoner() != null) && (requester != null))
		{
			return false;
		}
		_summonRequest.setTarget(requester, skill);
		return true;
	}
	
	/**
	 * Action teleport answer.
	 * @param answer the answer
	 * @param requesterId the requester id
	 */
	public void teleportAnswer(int answer, int requesterId)
	{
		if (_summonRequest.getSummoner() == null)
		{
			return;
		}
		if ((answer == 1) && (_summonRequest.getSummoner().getObjectId() == requesterId))
		{
			teleToTarget(this, _summonRequest.getSummoner(), _summonRequest.getLocation(), _summonRequest.getSkill());
		}
		_summonRequest.setTarget(null, null);
	}
	
	/**
	 * Tele to target.
	 * @param player the target player
	 * @param summoner the summoner player
	 * @param location the summon location
	 * @param summonSkill the summon skill
	 */
	public static void teleToTarget(Player player, Player summoner, Location location, Skill summonSkill)
	{
		if ((player == null) || (summoner == null) || (summonSkill == null))
		{
			return;
		}
		
		if (!checkSummonerStatus(summoner))
		{
			return;
		}
		
		if (!checkSummonTargetStatus(player, summoner))
		{
			return;
		}
		
		final int itemConsumeId = summonSkill.getTargetConsumeId();
		final int itemConsumeCount = summonSkill.getTargetConsume();
		if ((itemConsumeId != 0) && (itemConsumeCount != 0))
		{
			if (player.getInventory().getInventoryItemCount(itemConsumeId, 0) < itemConsumeCount)
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_IS_REQUIRED_FOR_SUMMONING);
				sm.addItemName(summonSkill.getTargetConsumeId());
				player.sendPacket(sm);
				return;
			}
			player.getInventory().destroyItemByItemId("Consume", itemConsumeId, itemConsumeCount, summoner, player);
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_DISAPPEARED);
			sm.addItemName(summonSkill.getTargetConsumeId());
			player.sendPacket(sm);
		}
		
		player.teleToLocation(location, true);
	}
	
	/**
	 * Check summoner status.
	 * @param summonerChar the summoner char
	 * @return true, if successful
	 */
	public static boolean checkSummonerStatus(Player summonerChar)
	{
		if (summonerChar == null)
		{
			return false;
		}
		
		if (summonerChar.isInOlympiadMode())
		{
			summonerChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_USE_THAT_ITEM_IN_A_GRAND_OLYMPIAD_GAMES_MATCH));
			return false;
		}
		
		if (summonerChar.inObserverMode())
		{
			return false;
		}
		
		if (summonerChar.isInsideZone(ZoneId.NO_SUMMON_FRIEND) || summonerChar.isFlying() || summonerChar.isMounted())
		{
			summonerChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
			return false;
		}
		return true;
	}
	
	/**
	 * Check summon target status.
	 * @param target the target
	 * @param summonerChar the summoner char
	 * @return true, if successful
	 */
	public static boolean checkSummonTargetStatus(WorldObject target, Player summonerChar)
	{
		if (!(target instanceof Player))
		{
			return false;
		}
		
		final Player targetChar = (Player) target;
		if (targetChar.isAlikeDead())
		{
			return false;
		}
		
		if (targetChar.isInStoreMode())
		{
			return false;
		}
		
		if (targetChar.isRooted() || targetChar.isInCombat())
		{
			return false;
		}
		
		if (targetChar.isInOlympiadMode())
		{
			summonerChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_CURRENTLY_PARTICIPATING_IN_THE_GRAND_OLYMPIAD));
			return false;
		}
		
		if (targetChar.isFestivalParticipant() || targetChar.isFlying())
		{
			summonerChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
			return false;
		}
		
		if (targetChar.inObserverMode())
		{
			return false;
		}
		
		if (targetChar.isInCombat())
		{
			summonerChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
			return false;
		}
		
		if (targetChar.isInsideZone(ZoneId.NO_SUMMON_FRIEND))
		{
			return false;
		}
		
		return true;
	}
	
	@Override
	public void reduceCurrentHp(double value, Creature attacker, boolean awake)
	{
		getStatus().reduceHp(value, attacker, awake);
		
		// notify the tamed beast of attacks
		if (getTrainedBeast() != null)
		{
			getTrainedBeast().onOwnerGotAttacked(attacker);
		}
	}
	
	public void broadcastSnoop(ChatType _chatType, String name, String text, CreatureSay cs)
	{
		if (!_snoopListener.isEmpty())
		{
			final Snoop sn = new Snoop(this, _chatType, name, text);
			for (Player pci : _snoopListener)
			{
				if (pci != null)
				{
					pci.sendPacket(cs);
					pci.sendPacket(sn);
				}
			}
		}
	}
	
	public void addSnooper(Player pci)
	{
		if (!_snoopListener.contains(pci))
		{
			_snoopListener.add(pci);
		}
	}
	
	public void removeSnooper(Player pci)
	{
		_snoopListener.remove(pci);
	}
	
	public void addSnooped(Player pci)
	{
		if (!_snoopedPlayer.contains(pci))
		{
			_snoopedPlayer.add(pci);
		}
	}
	
	public void removeSnooped(Player pci)
	{
		_snoopedPlayer.remove(pci);
	}
	
	/**
	 * Adds the bypass.
	 * @param bypass the bypass
	 */
	public synchronized void addBypass(String bypass)
	{
		if (bypass == null)
		{
			return;
		}
		_validBypass.add(bypass);
	}
	
	/**
	 * Adds the bypass2.
	 * @param bypass the bypass
	 */
	public synchronized void addBypass2(String bypass)
	{
		if (bypass == null)
		{
			return;
		}
		_validBypass2.add(bypass);
	}
	
	/**
	 * Validate bypass.
	 * @param cmd the cmd
	 * @return true, if successful
	 */
	public synchronized boolean validateBypass(String cmd)
	{
		if (!Config.BYPASS_VALIDATION)
		{
			return true;
		}
		
		for (String bp : _validBypass)
		{
			if (bp == null)
			{
				continue;
			}
			
			if (bp.equals(cmd))
			{
				return true;
			}
		}
		
		for (String bp : _validBypass2)
		{
			if (bp == null)
			{
				continue;
			}
			
			if (cmd.startsWith(bp))
			{
				return true;
			}
		}
		if (cmd.startsWith("npc_") && cmd.endsWith("_SevenSigns 7"))
		{
			return true;
		}
		
		final Player player = _client.getPlayer();
		// We decided to put a kick because when a player is doing quest with a BOT he sends invalid bypass.
		Util.handleIllegalPlayerAction(player, "[Player] player [" + player.getName() + "] sent invalid bypass '" + cmd + "'", Config.DEFAULT_PUNISH);
		return false;
	}
	
	/**
	 * Validate item manipulation by item id.
	 * @param itemId the item id
	 * @param action the action
	 * @return true, if successful
	 */
	public boolean validateItemManipulationByItemId(int itemId, String action)
	{
		final Item item = getInventory().getItemByItemId(itemId);
		if ((item == null) || (item.getOwnerId() != getObjectId()))
		{
			LOGGER.warning(getObjectId() + ": player tried to " + action + " item he is not owner of");
			return false;
		}
		if ((getActiveEnchantItem() != null) && (getActiveEnchantItem().getItemId() == itemId))
		{
			LOGGER.warning(getObjectId() + ":player tried to " + action + " an enchant scroll he was using");
			return false;
		}
		
		if (CursedWeaponsManager.getInstance().isCursed(itemId))
		{
			// can not trade a cursed weapon
			return false;
		}
		
		if (item.isWear())
		{
			// cannot drop/trade wear-items
			return false;
		}
		
		return true;
	}
	
	/**
	 * Validate item manipulation.
	 * @param objectId the object id
	 * @param action the action
	 * @return true, if successful
	 */
	public boolean validateItemManipulation(int objectId, String action)
	{
		final Item item = getInventory().getItemByObjectId(objectId);
		if ((item == null) || (item.getOwnerId() != getObjectId()))
		{
			LOGGER.warning(getObjectId() + ": player tried to " + action + " item he is not owner of");
			return false;
		}
		
		// Pet is summoned and not the item that summoned the pet AND not the buggle from strider you're mounting
		if (((getPet() != null) && (getPet().getControlItemId() == objectId)) || (getMountObjectID() == objectId))
		{
			return false;
		}
		
		if ((getActiveEnchantItem() != null) && (getActiveEnchantItem().getObjectId() == objectId))
		{
			return false;
		}
		
		if (CursedWeaponsManager.getInstance().isCursed(item.getItemId()))
		{
			// can not trade a cursed weapon
			return false;
		}
		
		if (item.isWear())
		{
			// cannot drop/trade wear-items
			return false;
		}
		
		return true;
	}
	
	/**
	 * Clear bypass.
	 */
	public synchronized void clearBypass()
	{
		_validBypass.clear();
		_validBypass2.clear();
	}
	
	/**
	 * Validate link.
	 * @param cmd the cmd
	 * @return true, if successful
	 */
	public synchronized boolean validateLink(String cmd)
	{
		if (!Config.BYPASS_VALIDATION)
		{
			return true;
		}
		
		for (String bp : _validLink)
		{
			if (bp == null)
			{
				continue;
			}
			
			if (bp.equals(cmd))
			{
				return true;
			}
		}
		LOGGER.warning("[Player] player [" + getName() + "] sent invalid link '" + cmd + "', ban this player!");
		return false;
	}
	
	/**
	 * Clear links.
	 */
	public synchronized void clearLinks()
	{
		_validLink.clear();
	}
	
	/**
	 * Adds the link.
	 * @param link the link
	 */
	public synchronized void addLink(String link)
	{
		if (link == null)
		{
			return;
		}
		_validLink.add(link);
	}
	
	/**
	 * Checks if is in boat.
	 * @return Returns the inBoat.
	 */
	public boolean isInBoat()
	{
		return _boat != null;
	}
	
	/**
	 * Gets the boat.
	 * @return the boat
	 */
	public Boat getBoat()
	{
		return _boat;
	}
	
	/**
	 * Sets the boat.
	 * @param boat the new boat
	 */
	public void setBoat(Boat boat)
	{
		if ((boat == null) && (_boat != null))
		{
			_boat.removePassenger(this);
		}
		_boat = boat;
	}
	
	/**
	 * Gets the in boat position.
	 * @return the in boat position
	 */
	public Location getBoatPosition()
	{
		return _inBoatPosition;
	}
	
	/**
	 * Sets the in boat position.
	 * @param location the new in boat location
	 */
	public void setBoatPosition(Location location)
	{
		_inBoatPosition = location;
	}
	
	/**
	 * Sets the in crystallize.
	 * @param inCrystallize the new in crystallize
	 */
	public void setInCrystallize(boolean inCrystallize)
	{
		_inCrystallize = inCrystallize;
	}
	
	/**
	 * Checks if is in crystallize.
	 * @return true, if is in crystallize
	 */
	public boolean isInCrystallize()
	{
		return _inCrystallize;
	}
	
	/**
	 * Manage the delete task of a Player (Leave Party, Unsummon pet, Save its inventory in the database, Remove it from the world...).<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>If the Player is in observer mode, set its position to its position before entering in observer mode</li>
	 * <li>Set the online Flag to True or False and update the characters table of the database with online status and lastAccess</li>
	 * <li>Stop the HP/MP/CP Regeneration task</li>
	 * <li>Cancel Crafting, Attak or Cast</li>
	 * <li>Remove the Player from the world</li>
	 * <li>Stop Party and Unsummon Pet</li>
	 * <li>Update database with items in its inventory and remove them from the world</li>
	 * <li>Remove all WorldObject from _knownObjects and _knownPlayer of the Creature then cancel Attak or Cast and notify AI</li>
	 * <li>Close the connection with the client</li>
	 */
	public synchronized void deleteMe()
	{
		// Check if the Player is in observer mode to set its position to its position before entering in observer mode
		if (inObserverMode())
		{
			setXYZ(_obsX, _obsY, _obsZ);
		}
		
		if (isTeleporting())
		{
			try
			{
				wait(2000);
			}
			catch (InterruptedException e)
			{
				LOGGER.warning(e.toString());
			}
			onTeleported();
		}
		
		Castle castle = null;
		if (getClan() != null)
		{
			castle = CastleManager.getInstance().getCastleByOwner(getClan());
			if (castle != null)
			{
				castle.destroyClanGate();
			}
		}
		
		// Set the online Flag to True or False and update the characters table of the database with online status and lastAccess (called when login and logout)
		try
		{
			setOnlineStatus(false);
		}
		catch (Throwable t)
		{
			LOGGER.warning("deleteMe()" + t);
		}
		
		// Stop the HP/MP/CP Regeneration task (scheduled tasks)
		try
		{
			stopAllTimers();
		}
		catch (Throwable t)
		{
			LOGGER.warning("deleteMe()" + t);
		}
		
		// Stop crafting, if in progress
		try
		{
			RecipeManager.getInstance().requestMakeItemAbort(this);
		}
		catch (Throwable t)
		{
			LOGGER.warning("deleteMe()" + t);
		}
		
		// Cancel Attack or Cast
		try
		{
			abortAttack();
			abortCast();
			setTarget(null);
		}
		catch (Throwable t)
		{
			LOGGER.warning("deleteMe()" + t);
		}
		
		PartyMatchWaitingList.getInstance().removePlayer(this);
		if (_partyroom != 0)
		{
			final PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(_partyroom);
			if (room != null)
			{
				room.deleteMember(this);
			}
		}
		
		// Remove from world regions zones
		if (getWorldRegion() != null)
		{
			getWorldRegion().removeFromZones(this);
		}
		
		try
		{
			if (_forceBuff != null)
			{
				abortCast();
			}
			
			for (Creature creature : getKnownList().getKnownCharacters())
			{
				if ((creature.getForceBuff() != null) && (creature.getForceBuff().getTarget() == this))
				{
					creature.abortCast();
				}
			}
		}
		catch (Throwable t)
		{
			LOGGER.warning("deleteMe()" + t);
		}
		
		// Remove the Player from the world
		if (isSpawned())
		{
			try
			{
				decayMe();
			}
			catch (Throwable t)
			{
				LOGGER.warning("deleteMe()" + t);
			}
		}
		
		// If a Party is in progress, leave it
		if (isInParty())
		{
			try
			{
				leaveParty();
			}
			catch (Throwable t)
			{
				LOGGER.warning("deleteMe()" + t);
			}
		}
		
		// If the Player has Pet, unsummon it
		if (getPet() != null)
		{
			try
			{
				getPet().unSummon(this);
			}
			catch (Throwable t)
			{
				LOGGER.warning("deleteMe()" + t);
			}
		}
		
		if ((getClanId() != 0) && (getClan() != null))
		{
			// set the status for pledge member list to OFFLINE
			try
			{
				final ClanMember clanMember = getClan().getClanMember(getName());
				if (clanMember != null)
				{
					clanMember.setPlayer(null);
				}
			}
			catch (Throwable t)
			{
				LOGGER.warning("deleteMe()" + t);
			}
		}
		
		if (getActiveRequester() != null)
		{
			// deals with sudden exit in the middle of transaction
			setActiveRequester(null);
		}
		
		if (getOlympiadGameId() != -1)
		{
			Olympiad.getInstance().removeDisconnectedCompetitor(this);
		}
		
		// If the Player is a GM, remove it from the GM List
		if (isGM())
		{
			try
			{
				AdminData.getInstance().deleteGm(this);
			}
			catch (Throwable t)
			{
				LOGGER.warning("deleteMe()" + t);
			}
		}
		
		// Update database with items in its inventory and remove them from the world
		try
		{
			getInventory().deleteMe();
		}
		catch (Throwable t)
		{
			LOGGER.warning("deleteMe()" + t);
		}
		
		// Update database with items in its warehouse and remove them from the world
		try
		{
			clearWarehouse();
		}
		catch (Throwable t)
		{
			LOGGER.warning("deleteMe()" + t);
		}
		
		if (Config.WAREHOUSE_CACHE)
		{
			WarehouseCacheManager.getInstance().remCacheTask(this);
		}
		
		// Update database with items in its freight and remove them from the world
		try
		{
			getFreight().deleteMe();
		}
		catch (Throwable t)
		{
			LOGGER.warning("deleteMe()" + t);
		}
		
		// Remove all WorldObject from _knownObjects and _knownPlayer of the Creature then cancel Attak or Cast and notify AI
		try
		{
			getKnownList().removeAllKnownObjects();
		}
		catch (Throwable t)
		{
			LOGGER.warning("deleteMe()" + t);
		}
		
		// Close the connection with the client
		closeNetConnection();
		
		if (getClanId() > 0)
		{
			getClan().broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(this), this);
		}
		
		for (Player player : _snoopedPlayer)
		{
			player.removeSnooper(this);
		}
		
		for (Player player : _snoopListener)
		{
			player.removeSnooped(this);
		}
		
		if (_chanceSkills != null)
		{
			_chanceSkills.setOwner(null);
			_chanceSkills = null;
		}
		
		notifyFriends(true);
		
		// Remove WorldObject object from _allObjects of World
		World.getInstance().removeObject(this);
		World.getInstance().removeFromAllPlayers(this); // force remove in case of crash during teleport
		PlayerAutoSaveTaskManager.getInstance().remove(this);
	}
	
	private class ShortBuffTask implements Runnable
	{
		private Player _player = null;
		
		public ShortBuffTask(Player player)
		{
			_player = player;
		}
		
		@Override
		public void run()
		{
			if (_player == null)
			{
				return;
			}
			
			_player.sendPacket(new ShortBuffStatusUpdate(0, 0, 0));
		}
	}
	
	/**
	 * @param magicId
	 * @param level
	 * @param time
	 */
	public void shortBuffStatusUpdate(int magicId, int level, int time)
	{
		if (_shortBuffTask != null)
		{
			_shortBuffTask.cancel(false);
			_shortBuffTask = null;
		}
		_shortBuffTask = ThreadPool.schedule(new ShortBuffTask(this), 15000);
		sendPacket(new ShortBuffStatusUpdate(magicId, level, time));
	}
	
	public List<Integer> getFriendList()
	{
		return _friendList;
	}
	
	public void selectFriend(Integer friendId)
	{
		if (!_selectedFriendList.contains(friendId))
		{
			_selectedFriendList.add(friendId);
		}
	}
	
	public void deselectFriend(Integer friendId)
	{
		if (_selectedFriendList.contains(friendId))
		{
			_selectedFriendList.remove(friendId);
		}
	}
	
	public List<Integer> getSelectedFriendList()
	{
		return _selectedFriendList;
	}
	
	private void restoreFriendList()
	{
		_friendList.clear();
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT friend_id FROM character_friends WHERE char_id = ? AND relation = 0");
			statement.setInt(1, getObjectId());
			final ResultSet rset = statement.executeQuery();
			int friendId;
			while (rset.next())
			{
				friendId = rset.getInt("friend_id");
				if (friendId == getObjectId())
				{
					continue;
				}
				
				_friendList.add(friendId);
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Error found in " + getName() + "'s friendlist: " + e);
		}
	}
	
	private void notifyFriends(boolean login)
	{
		for (int id : _friendList)
		{
			final Player friend = World.getInstance().getPlayer(id);
			if (friend != null)
			{
				friend.sendPacket(new FriendList(friend));
				if (login)
				{
					friend.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_FRIEND_HAS_LOGGED_IN).addString(getName()));
				}
			}
		}
	}
	
	public void selectBlock(Integer friendId)
	{
		if (!_selectedBlocksList.contains(friendId))
		{
			_selectedBlocksList.add(friendId);
		}
	}
	
	public void deselectBlock(Integer friendId)
	{
		if (_selectedBlocksList.contains(friendId))
		{
			_selectedBlocksList.remove(friendId);
		}
	}
	
	public List<Integer> getSelectedBlocksList()
	{
		return _selectedBlocksList;
	}
	
	public int getMailPosition()
	{
		return _mailPosition;
	}
	
	public void setMailPosition(int mailPosition)
	{
		_mailPosition = mailPosition;
	}
	
	public void startFishing(int x, int y, int z)
	{
		stopMove(null);
		setImmobilized(true);
		_fishing = true;
		_fishX = x;
		_fishY = y;
		_fishZ = z;
		broadcastUserInfo();
		// Starts fishing
		final int lvl = getRandomFishLvl();
		final int group = getRandomGroup();
		final int type = getRandomFishType(group);
		_fish = FishData.getInstance().getFish(lvl, type, group);
		if (_fish == null)
		{
			sendMessage("Error - Fishes are not definied");
			endFishing(false);
			return;
		}
		sendPacket(SystemMessageId.YOU_CAST_YOUR_LINE_AND_START_TO_FISH);
		broadcastPacket(new ExFishingStart(this, _fish.getType(), x, y, z, _lure.isNightLure()));
		startLookingForFishTask();
	}
	
	public void stopLookingForFishTask()
	{
		if (_taskforfish != null)
		{
			_taskforfish.cancel(false);
			_taskforfish = null;
		}
	}
	
	public void startLookingForFishTask()
	{
		if (!isDead() && (_taskforfish == null))
		{
			int checkDelay = 0;
			boolean isNoob = false;
			boolean isUpperGrade = false;
			if (_lure != null)
			{
				final int lureid = _lure.getItemId();
				isNoob = _fish.getGroup() == 0;
				isUpperGrade = _fish.getGroup() == 2;
				if ((lureid == 6519) || (lureid == 6522) || (lureid == 6525) || (lureid == 8505) || (lureid == 8508) || (lureid == 8511))
				{
					checkDelay = Math.round((float) (_fish.getGutsCheckTime() * 1.33));
				}
				else if ((lureid == 6520) || (lureid == 6523) || (lureid == 6526) || ((lureid >= 8505) && (lureid <= 8513)) || ((lureid >= 7610) && (lureid <= 7613)) || ((lureid >= 7807) && (lureid <= 7809)) || ((lureid >= 8484) && (lureid <= 8486)))
				{
					checkDelay = Math.round((float) (_fish.getGutsCheckTime() * 1.00));
				}
				else if ((lureid == 6521) || (lureid == 6524) || (lureid == 6527) || (lureid == 8507) || (lureid == 8510) || (lureid == 8513))
				{
					checkDelay = Math.round((float) (_fish.getGutsCheckTime() * 0.66));
				}
			}
			_taskforfish = ThreadPool.scheduleAtFixedRate(new LookingForFishTask(_fish.getWaitTime(), _fish.getGuts(), _fish.getType(), isNoob, isUpperGrade), 10000, checkDelay);
		}
	}
	
	private int getRandomGroup()
	{
		switch (_lure.getItemId())
		{
			case 7807: // green for beginners
			case 7808: // purple for beginners
			case 7809: // yellow for beginners
			case 8486: // prize-winning for beginners
			{
				return 0;
			}
			case 8485: // prize-winning luminous
			case 8506: // green luminous
			case 8509: // purple luminous
			case 8512: // yellow luminous
			{
				return 2;
			}
			default:
			{
				return 1;
			}
		}
	}
	
	private int getRandomFishType(int group)
	{
		final int check = Rnd.get(100);
		int type = 1;
		switch (group)
		{
			case 0: // fish for novices
			{
				switch (_lure.getItemId())
				{
					case 7807: // green lure, preferred by fast-moving (nimble) fish (type 5)
					{
						if (check <= 54)
						{
							type = 5;
						}
						else if (check <= 77)
						{
							type = 4;
						}
						else
						{
							type = 6;
						}
						break;
					}
					case 7808: // purple lure, preferred by fat fish (type 4)
					{
						if (check <= 54)
						{
							type = 4;
						}
						else if (check <= 77)
						{
							type = 6;
						}
						else
						{
							type = 5;
						}
						break;
					}
					case 7809: // yellow lure, preferred by ugly fish (type 6)
					{
						if (check <= 54)
						{
							type = 6;
						}
						else if (check <= 77)
						{
							type = 5;
						}
						else
						{
							type = 4;
						}
						break;
					}
					case 8486: // prize-winning fishing lure for beginners
					{
						if (check <= 33)
						{
							type = 4;
						}
						else if (check <= 66)
						{
							type = 5;
						}
						else
						{
							type = 6;
						}
						break;
					}
				}
				break;
			}
			case 1: // normal fish
			{
				switch (_lure.getItemId())
				{
					case 7610:
					case 7611:
					case 7612:
					case 7613:
					{
						type = 3;
						break;
					}
					case 6519: // all theese lures (green) are prefered by fast-moving (nimble) fish (type 1)
					case 8505:
					case 6520:
					case 6521:
					case 8507:
					{
						if (check <= 54)
						{
							type = 1;
						}
						else if (check <= 74)
						{
							type = 0;
						}
						else if (check <= 94)
						{
							type = 2;
						}
						else
						{
							type = 3;
						}
						break;
					}
					case 6522: // all theese lures (purple) are prefered by fat fish (type 0)
					case 8508:
					case 6523:
					case 6524:
					case 8510:
					{
						if (check <= 54)
						{
							type = 0;
						}
						else if (check <= 74)
						{
							type = 1;
						}
						else if (check <= 94)
						{
							type = 2;
						}
						else
						{
							type = 3;
						}
						break;
					}
					case 6525: // all theese lures (yellow) are prefered by ugly fish (type 2)
					case 8511:
					case 6526:
					case 6527:
					case 8513:
					{
						if (check <= 55)
						{
							type = 2;
						}
						else if (check <= 74)
						{
							type = 1;
						}
						else if (check <= 94)
						{
							type = 0;
						}
						else
						{
							type = 3;
						}
						break;
					}
					case 8484: // prize-winning fishing lure
					{
						if (check <= 33)
						{
							type = 0;
						}
						else if (check <= 66)
						{
							type = 1;
						}
						else
						{
							type = 2;
						}
						break;
					}
				}
				break;
			}
			case 2: // upper grade fish, luminous lure
			{
				switch (_lure.getItemId())
				{
					case 8506: // green lure, preferred by fast-moving (nimble) fish (type 8)
					{
						if (check <= 54)
						{
							type = 8;
						}
						else if (check <= 77)
						{
							type = 7;
						}
						else
						{
							type = 9;
						}
						break;
					}
					case 8509: // purple lure, preferred by fat fish (type 7)
					{
						if (check <= 54)
						{
							type = 7;
						}
						else if (check <= 77)
						{
							type = 9;
						}
						else
						{
							type = 8;
						}
						break;
					}
					case 8512: // yellow lure, preferred by ugly fish (type 9)
					{
						if (check <= 54)
						{
							type = 9;
						}
						else if (check <= 77)
						{
							type = 8;
						}
						else
						{
							type = 7;
						}
						break;
					}
					case 8485: // prize-winning fishing lure
					{
						if (check <= 33)
						{
							type = 7;
						}
						else if (check <= 66)
						{
							type = 8;
						}
						else
						{
							type = 9;
						}
						break;
					}
				}
				break;
			}
		}
		return type;
	}
	
	private int getRandomFishLvl()
	{
		final Collection<Effect> effects = getAllEffects();
		int skillLevel = getSkillLevel(1315);
		for (Effect e : effects)
		{
			if (e.getSkill().getId() == 2274 /* Fisherman's Potion */)
			{
				skillLevel = (int) e.getSkill().getPower(this);
			}
		}
		if (skillLevel <= 0)
		{
			return 1;
		}
		int randomlvl;
		final int check = Rnd.get(100);
		if (check <= 50)
		{
			randomlvl = skillLevel;
		}
		else if (check <= 85)
		{
			randomlvl = skillLevel - 1;
			if (randomlvl <= 0)
			{
				randomlvl = 1;
			}
		}
		else
		{
			randomlvl = skillLevel + 1;
			if (randomlvl > 27)
			{
				randomlvl = 27;
			}
		}
		return randomlvl;
	}
	
	public void startFishCombat(boolean isNoob, boolean isUpperGrade)
	{
		_fishCombat = new Fishing(this, _fish, isNoob, isUpperGrade, _lure.getItemId());
	}
	
	/**
	 * End fishing.
	 * @param win the win
	 */
	public void endFishing(boolean win)
	{
		broadcastPacket(new ExFishingEnd(win, this));
		_fishing = false;
		_fishX = 0;
		_fishY = 0;
		_fishZ = 0;
		broadcastUserInfo();
		
		if (_fishCombat == null)
		{
			sendPacket(SystemMessageId.BAITS_HAVE_BEEN_LOST_BECAUSE_THE_FISH_GOT_AWAY);
		}
		
		_fishCombat = null;
		_lure = null;
		// Ends fishing
		sendPacket(SystemMessageId.YOU_REEL_YOUR_LINE_IN_AND_STOP_FISHING);
		setImmobilized(false);
		stopLookingForFishTask();
	}
	
	public Fishing getFishCombat()
	{
		return _fishCombat;
	}
	
	public int getFishX()
	{
		return _fishX;
	}
	
	public int getFishY()
	{
		return _fishY;
	}
	
	public int getFishZ()
	{
		return _fishZ;
	}
	
	public void setLure(Item lure)
	{
		_lure = lure;
	}
	
	public Item getLure()
	{
		return _lure;
	}
	
	public void setPartyFind(int find)
	{
		_partyFind = find;
	}
	
	public int getPartyFind()
	{
		return _partyFind;
	}
	
	public int getInventoryLimit()
	{
		int ivlim;
		if (isGM())
		{
			ivlim = Config.INVENTORY_MAXIMUM_GM;
		}
		else if (getRace() == Race.DWARF)
		{
			ivlim = Config.INVENTORY_MAXIMUM_DWARF;
		}
		else
		{
			ivlim = Config.INVENTORY_MAXIMUM_NO_DWARF;
		}
		ivlim += (int) getStat().calcStat(Stat.INV_LIM, 0, null, null);
		return ivlim;
	}
	
	public int getWareHouseLimit()
	{
		int whlim;
		if (getRace() == Race.DWARF)
		{
			whlim = Config.WAREHOUSE_SLOTS_DWARF;
		}
		else
		{
			whlim = Config.WAREHOUSE_SLOTS_NO_DWARF;
		}
		whlim += (int) getStat().calcStat(Stat.WH_LIM, 0, null, null);
		return whlim;
	}
	
	public int getPrivateSellStoreLimit()
	{
		int pslim;
		if (getRace() == Race.DWARF)
		{
			pslim = Config.MAX_PVTSTORE_SLOTS_DWARF;
		}
		
		else
		{
			pslim = Config.MAX_PVTSTORE_SLOTS_OTHER;
		}
		pslim += (int) getStat().calcStat(Stat.P_SELL_LIM, 0, null, null);
		return pslim;
	}
	
	public int getPrivateBuyStoreLimit()
	{
		int pblim;
		if (getRace() == Race.DWARF)
		{
			pblim = Config.MAX_PVTSTORE_SLOTS_DWARF;
		}
		else
		{
			pblim = Config.MAX_PVTSTORE_SLOTS_OTHER;
		}
		pblim += (int) getStat().calcStat(Stat.P_BUY_LIM, 0, null, null);
		return pblim;
	}
	
	public int getFreightLimit()
	{
		return Config.FREIGHT_SLOTS + (int) getStat().calcStat(Stat.FREIGHT_LIM, 0, null, null);
	}
	
	public int getDwarfRecipeLimit()
	{
		int recdlim = Config.DWARF_RECIPE_LIMIT;
		recdlim += (int) getStat().calcStat(Stat.REC_D_LIM, 0, null, null);
		return recdlim;
	}
	
	public int getCommonRecipeLimit()
	{
		int recclim = Config.COMMON_RECIPE_LIMIT;
		recclim += (int) getStat().calcStat(Stat.REC_C_LIM, 0, null, null);
		return recclim;
	}
	
	public void setMountObjectID(int oid)
	{
		_mountObjectID = oid;
	}
	
	public int getMountObjectID()
	{
		return _mountObjectID;
	}
	
	/**
	 * Get the current skill in use or return null.
	 * @return the current skill
	 */
	public SkillUseHolder getCurrentSkill()
	{
		return _currentSkill;
	}
	
	/**
	 * Create a new SkillUseHolder object and set the player _currentSkill.
	 * @param currentSkill the current skill
	 * @param ctrlPressed the ctrl pressed
	 * @param shiftPressed the shift pressed
	 */
	public void setCurrentSkill(Skill currentSkill, boolean ctrlPressed, boolean shiftPressed)
	{
		if (currentSkill == null)
		{
			_currentSkill = null;
			return;
		}
		_currentSkill = new SkillUseHolder(currentSkill, ctrlPressed, shiftPressed);
	}
	
	/**
	 * Gets the queued skill.
	 * @return the queued skill
	 */
	public SkillUseHolder getQueuedSkill()
	{
		return _queuedSkill;
	}
	
	/**
	 * Create a new SkillUseHolder object and queue it in the player _queuedSkill.
	 * @param queuedSkill the queued skill
	 * @param ctrlPressed the ctrl pressed
	 * @param shiftPressed the shift pressed
	 */
	public void setQueuedSkill(Skill queuedSkill, boolean ctrlPressed, boolean shiftPressed)
	{
		if (queuedSkill == null)
		{
			_queuedSkill = null;
			return;
		}
		_queuedSkill = new SkillUseHolder(queuedSkill, ctrlPressed, shiftPressed);
	}
	
	/**
	 * Gets the power grade.
	 * @return the power grade
	 */
	public int getPowerGrade()
	{
		return _powerGrade;
	}
	
	/**
	 * Sets the power grade.
	 * @param power the new power grade
	 */
	public void setPowerGrade(int power)
	{
		_powerGrade = power;
	}
	
	/**
	 * Checks if is cursed weapon equiped.
	 * @return true, if is cursed weapon equiped
	 */
	public boolean isCursedWeaponEquiped()
	{
		return _cursedWeaponEquipedId != 0;
	}
	
	/**
	 * Sets the cursed weapon equipped id.
	 * @param value the new cursed weapon equipped id
	 */
	public void setCursedWeaponEquipedId(int value)
	{
		_cursedWeaponEquipedId = value;
	}
	
	/**
	 * Gets the cursed weapon equipped id.
	 * @return the cursed weapon equipped id
	 */
	public int getCursedWeaponEquipedId()
	{
		return _cursedWeaponEquipedId;
	}
	
	/** The _charm of courage. */
	private boolean _charmOfCourage = false;
	
	/**
	 * Gets the charm of courage.
	 * @return the charm of courage
	 */
	public boolean getCharmOfCourage()
	{
		return _charmOfCourage;
	}
	
	/**
	 * Sets the charm of courage.
	 * @param value the new charm of courage
	 */
	public void setCharmOfCourage(boolean value)
	{
		_charmOfCourage = value;
		sendPacket(new EtcStatusUpdate(this));
	}
	
	/**
	 * Gets the death penalty buff level.
	 * @return the death penalty buff level
	 */
	public int getDeathPenaltyBuffLevel()
	{
		return _deathPenaltyBuffLevel;
	}
	
	/**
	 * Sets the death penalty buff level.
	 * @param level the new death penalty buff level
	 */
	public void setDeathPenaltyBuffLevel(int level)
	{
		_deathPenaltyBuffLevel = level;
	}
	
	/**
	 * Calculate death penalty buff level.
	 * @param killer the killer
	 */
	public void calculateDeathPenaltyBuffLevel(Creature killer)
	{
		if ((Config.DEATH_PENALTY_CHANCE > 0) && (Rnd.get(100) < Config.DEATH_PENALTY_CHANCE) && !killer.isPlayer() && !isGM() && !getCharmOfLuck() && !(killer instanceof GrandBoss) && !(killer instanceof RaidBoss) && !isInsideZone(ZoneId.PVP) && !isInsideZone(ZoneId.SIEGE))
		{
			increaseDeathPenaltyBuffLevel();
		}
	}
	
	/**
	 * Increase death penalty buff level.
	 */
	public void increaseDeathPenaltyBuffLevel()
	{
		if (getDeathPenaltyBuffLevel() >= 15)
		{
			return;
		}
		
		if (getDeathPenaltyBuffLevel() != 0)
		{
			final Skill skill = SkillTable.getInstance().getSkill(5076, getDeathPenaltyBuffLevel());
			if (skill != null)
			{
				removeSkill(skill, true);
			}
		}
		
		_deathPenaltyBuffLevel++;
		
		addSkill(SkillTable.getInstance().getSkill(5076, getDeathPenaltyBuffLevel()), false);
		sendPacket(new EtcStatusUpdate(this));
		final SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_DEATH_PENALTY_IS_NOW_LEVEL_S1);
		sm.addNumber(getDeathPenaltyBuffLevel());
		sendPacket(sm);
		sendSkillList();
	}
	
	/**
	 * Reduce death penalty buff level.
	 */
	public void reduceDeathPenaltyBuffLevel()
	{
		if (getDeathPenaltyBuffLevel() <= 0)
		{
			return;
		}
		
		final Skill skill = SkillTable.getInstance().getSkill(5076, getDeathPenaltyBuffLevel());
		if (skill != null)
		{
			removeSkill(skill, true);
			sendSkillList();
		}
		
		_deathPenaltyBuffLevel--;
		
		if (getDeathPenaltyBuffLevel() > 0)
		{
			addSkill(SkillTable.getInstance().getSkill(5076, getDeathPenaltyBuffLevel()), false);
			sendPacket(new EtcStatusUpdate(this));
			final SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_DEATH_PENALTY_IS_NOW_LEVEL_S1);
			sm.addNumber(getDeathPenaltyBuffLevel());
			sendPacket(sm);
			sendSkillList();
		}
		else
		{
			sendPacket(new EtcStatusUpdate(this));
			sendPacket(SystemMessageId.YOUR_DEATH_PENALTY_HAS_BEEN_LIFTED);
		}
	}
	
	public void restoreCustomStatus()
	{
		final long currentTime = Chronos.currentTimeMillis();
		if (getVariables().getLong("CustomDonatorEnd", 0) > currentTime)
		{
			setDonator(true);
		}
		
		final long heroEnd = getVariables().getLong("CustomHeroEnd", 0);
		if (getVariables().getBoolean("CustomHero", false) && ((heroEnd == 0) || (heroEnd > currentTime)))
		{
			setHero(true);
		}
		else if ((Hero.getInstance().getHeroes() != null) && !Hero.getInstance().getHeroes().containsKey(getObjectId()))
		{
			// Delete wings of destiny.
			destroyItem("CustomHeroEnd", 6842, 1, null, false);
		}
		
		if (getVariables().getBoolean("CustomNoble", false))
		{
			setNoble(true);
		}
	}
	
	/**
	 * Restore death penalty buff level.
	 */
	public void restoreDeathPenaltyBuffLevel()
	{
		final Skill skill = SkillTable.getInstance().getSkill(5076, getDeathPenaltyBuffLevel());
		if (skill != null)
		{
			removeSkill(skill, true);
		}
		
		if (getDeathPenaltyBuffLevel() > 0)
		{
			addSkill(SkillTable.getInstance().getSkill(5076, getDeathPenaltyBuffLevel()), false);
			final SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_DEATH_PENALTY_IS_NOW_LEVEL_S1);
			sm.addNumber(getDeathPenaltyBuffLevel());
			sendPacket(sm);
		}
		sendPacket(new EtcStatusUpdate(this));
	}
	
	/**
	 * Index according to skill id the current timestamp of use.
	 * @param s the s
	 * @param r the r
	 */
	public void addTimestamp(Skill s, int r)
	{
		_reuseTimestamps.put(s.getId(), new Timestamp(s, r));
	}
	
	/**
	 * Index according to skill this TimeStamp instance for restoration purposes only.
	 * @param t the t
	 */
	private void addTimestamp(Timestamp t)
	{
		_reuseTimestamps.put(t.getSkillId(), t);
	}
	
	/**
	 * Index according to skill id the current timestamp of use.
	 * @param skill the skill
	 */
	public void removeTimestamp(Skill skill)
	{
		_reuseTimestamps.remove(skill.getId());
	}
	
	public Collection<Timestamp> getReuseTimeStamps()
	{
		return _reuseTimestamps.values();
	}
	
	public void resetSkillTime(boolean sendSkillList)
	{
		for (Skill skill : getAllSkills())
		{
			if ((skill != null) && skill.isActive() && (skill.getId() != 1324 /* Summon CP Potion */))
			{
				enableSkill(skill);
			}
		}
		if (sendSkillList)
		{
			sendSkillList();
		}
		sendPacket(new SkillCoolTime(this));
	}
	
	@Override
	public void sendDamageMessage(Creature target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{
		// Check if hit is missed
		if (miss)
		{
			sendPacket(SystemMessageId.YOU_HAVE_MISSED);
			return;
		}
		
		// Check if hit is critical
		if (pcrit)
		{
			sendPacket(SystemMessageId.CRITICAL_HIT);
			
		}
		
		if (mcrit)
		{
			sendPacket(SystemMessageId.MAGIC_CRITICAL_HIT);
			
		}
		
		if (isInOlympiadMode() && (target instanceof Player) && target.getActingPlayer().isInOlympiadMode() && (target.getActingPlayer().getOlympiadGameId() == getOlympiadGameId()))
		{
			Olympiad.getInstance().notifyCompetitorDamage(this, damage, getOlympiadGameId());
		}
		
		if (this != target)
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HIT_FOR_S1_DAMAGE);
			sm.addNumber(damage);
			sendPacket(sm);
		}
	}
	
	/**
	 * Update title.
	 */
	public void updateTitle()
	{
		setTitle(Config.PVP_TITLE_PREFIX + getPvpKills() + Config.PK_TITLE_PREFIX + getPkKills() + " ");
	}
	
	/**
	 * Return true if last request is expired.
	 * @return true, if is request expired
	 */
	public boolean isRequestExpired()
	{
		return (_requestExpireTime <= GameTimeTaskManager.getGameTicks());
	}
	
	/**
	 * Sets the GM status active.
	 * @param value the new GM status active
	 */
	public void setGmStatusActive(boolean value)
	{
		_gmStatus = value;
	}
	
	/**
	 * Checks for GM status active.
	 * @return true, if successful
	 */
	public boolean hasGmStatusActive()
	{
		return _gmStatus;
	}
	
	/**
	 * Gets the say mode.
	 * @return the say mode
	 */
	public WorldObject getSayMode()
	{
		return _saymode;
	}
	
	/**
	 * Sets the say mode.
	 * @param say the new say mode
	 */
	public void setSayMode(WorldObject say)
	{
		_saymode = say;
	}
	
	/**
	 * Gets the current skill world position.
	 * @return the current skill world position
	 */
	public Location getCurrentSkillWorldPosition()
	{
		return _currentSkillWorldPosition;
	}
	
	/**
	 * Sets the current skill world position.
	 * @param location the new current skill world position
	 */
	public void setCurrentSkillWorldPosition(Location location)
	{
		_currentSkillWorldPosition = location;
	}
	
	/**
	 * Checks if is cursed weapon equipped.
	 * @return true, if is cursed weapon equipped
	 */
	public boolean isCursedWeaponEquipped()
	{
		return _cursedWeaponEquipedId != 0;
	}
	
	/**
	 * Dismount.
	 * @return true, if successful
	 */
	public boolean dismount()
	{
		if (ZoneData.getInstance().getZone(getX(), getY(), getZ() - 300, WaterZone.class) == null)
		{
			if (!isInWater() && (getZ() > 10000))
			{
				sendPacket(SystemMessageId.YOU_ARE_NOT_ALLOWED_TO_DISMOUNT_AT_THIS_LOCATION);
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			if ((GeoEngine.getInstance().getHeight(getX(), getY(), getZ()) + 300) < getZ())
			{
				sendPacket(SystemMessageId.YOU_CANNOT_DISMOUNT_FROM_THIS_ELEVATION);
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}
		else
		{
			ThreadPool.schedule(() ->
			{
				if (isInWater())
				{
					broadcastUserInfo();
				}
			}, 1500);
		}
		
		final boolean wasFlying = isFlying();
		setMountType(0);
		if (wasFlying)
		{
			removeSkill(SkillTable.getInstance().getSkill(4289, 1));
		}
		broadcastPacket(new Ride(getObjectId(), Ride.ACTION_DISMOUNT, 0));
		setMountObjectID(0);
		broadcastUserInfo();
		return true;
	}
	
	/**
	 * Gets the pc bang score.
	 * @return the pc bang score
	 */
	public int getPcBangScore()
	{
		return _pcBangPoints;
	}
	
	/**
	 * Reduce pc bang score.
	 * @param to the to
	 */
	public void reducePcBangScore(int to)
	{
		_pcBangPoints -= to;
		updatePcBangWnd(to, false, false);
	}
	
	/**
	 * Adds the pc bang score.
	 * @param to the to
	 */
	public void addPcBangScore(int to)
	{
		_pcBangPoints += to;
	}
	
	/**
	 * Update pc bang wnd.
	 * @param score the score
	 * @param add the add
	 * @param duble the duble
	 */
	public void updatePcBangWnd(int score, boolean add, boolean duble)
	{
		sendPacket(new ExPCCafePointInfo(this, score, add, 24, duble));
	}
	
	/**
	 * Show pc bang window.
	 */
	public void showPcBangWindow()
	{
		sendPacket(new ExPCCafePointInfo(this, 0, false, 24, false));
	}
	
	/**
	 * String to hex.
	 * @param value the color
	 * @return the string
	 */
	private String StringToHex(String value)
	{
		String color = value;
		switch (color.length())
		{
			case 1:
			{
				color = new StringBuilder().append("00000").append(color).toString();
				break;
			}
			case 2:
			{
				color = new StringBuilder().append("0000").append(color).toString();
				break;
			}
			case 3:
			{
				color = new StringBuilder().append("000").append(color).toString();
				break;
			}
			case 4:
			{
				color = new StringBuilder().append("00").append(color).toString();
				break;
			}
			case 5:
			{
				color = new StringBuilder().append('0').append(color).toString();
				break;
			}
		}
		return color;
	}
	
	/**
	 * Checks if is offline.
	 * @return true, if is offline
	 */
	public boolean isInOfflineMode()
	{
		return _isInOfflineMode;
	}
	
	/**
	 * Sets the offline.
	 * @param set the new offline
	 */
	public void setOfflineMode(boolean set)
	{
		_isInOfflineMode = set;
	}
	
	/**
	 * Checks if is trade disabled.
	 * @return true, if is trade disabled
	 */
	public boolean isTradeDisabled()
	{
		return _isTradeOff || isCastingNow();
	}
	
	/**
	 * Sets the trade disabled.
	 * @param set the new trade disabled
	 */
	public void setTradeDisabled(boolean set)
	{
		_isTradeOff = set;
	}
	
	/**
	 * Check if local player can make multibox and also refresh local boxes instances number.
	 * @return true, if successful
	 */
	public boolean canMultiBox()
	{
		boolean canMultiBox = true;
		int boxCount = 1;
		final List<String> activeBoxes = new ArrayList<>();
		if ((_client != null) && (_client.getConnectionAddress() != null) && !_client.isDetached() && (_client.getConnectionAddress() != null))
		{
			final String playerIP = _client.getConnectionAddress().getHostAddress();
			for (Player player : World.getInstance().getAllPlayers())
			{
				if ((player != null) && (player != this) && player.isOnline() && (player.getClient() != null) && (player.getClient().getConnectionAddress() != null) && !player.getClient().isDetached() && (player.getClient().getConnectionAddress() != null) && playerIP.equals(player.getClient().getConnectionAddress().getHostAddress()))
				{
					boxCount++;
					activeBoxes.add(player.getName());
					if (!Config.ALLOW_DUALBOX || (boxCount > Config.ALLOWED_BOXES))
					{
						canMultiBox = false;
						break;
					}
				}
			}
		}
		
		if (canMultiBox)
		{
			_activeBoxes = boxCount;
			if (!activeBoxes.contains(getName()))
			{
				activeBoxes.add(getName());
				_activeBoxCharacters = activeBoxes;
			}
			refreshOtherBoxes();
		}
		
		return canMultiBox;
	}
	
	/**
	 * Increase active boxes number for local player and other boxer for same IP.
	 */
	public void refreshOtherBoxes()
	{
		if ((_client != null) && (_client.getConnectionAddress() != null) && !_client.isDetached() && (_client.getConnectionAddress() != null))
		{
			final String playerIP = _client.getConnectionAddress().getHostAddress();
			for (Player player : World.getInstance().getAllPlayers())
			{
				if ((player != null) && (player != this) && player.isOnline() && (player.getClient() != null) && (player.getClient().getConnectionAddress() != null) && !player.getClient().isDetached() && !player.getName().equals(getName()) && playerIP.equals(player.getClient().getConnectionAddress().getHostAddress()))
				{
					player._activeBoxes = _activeBoxes;
					player._activeBoxCharacters = _activeBoxCharacters;
				}
			}
		}
	}
	
	/**
	 * Decrease active boxes number for local player and other boxer for same IP.
	 */
	public void decreaseBoxes()
	{
		_activeBoxes = _activeBoxes - 1;
		_activeBoxCharacters.remove(getName());
		refreshOtherBoxes();
	}
	
	/**
	 * Aio System Start.
	 * @return true, if is aio
	 */
	public boolean isAio()
	{
		return _isAio;
	}
	
	/**
	 * Sets the aio.
	 * @param value the new aio
	 */
	public void setAio(boolean value)
	{
		_isAio = value;
	}
	
	/**
	 * Reward aio skills.
	 */
	public void rewardAioSkills()
	{
		Skill skill;
		for (Integer skillId : Config.AIO_SKILLS.keySet())
		{
			final int skillLevel = Config.AIO_SKILLS.get(skillId);
			skill = SkillTable.getInstance().getSkill(skillId, skillLevel);
			if (skill != null)
			{
				addSkill(skill, true);
			}
		}
		sendMessage("GM give to you Aio's skills");
	}
	
	/**
	 * Lost aio skills.
	 */
	public void lostAioSkills()
	{
		Skill skill;
		for (Integer skillId : Config.AIO_SKILLS.keySet())
		{
			final int skillLevel = Config.AIO_SKILLS.get(skillId);
			skill = SkillTable.getInstance().getSkill(skillId, skillLevel);
			removeSkill(skill);
		}
	}
	
	/**
	 * Sets the aio end time.
	 * @param value the new aio end time
	 */
	public void setAioEndTime(long value)
	{
		_aioEndTime = value;
	}
	
	/**
	 * Sets the end time.
	 * @param process the process
	 * @param time the value
	 */
	public void setEndTime(String process, int time)
	{
		int value = time;
		if (value > 0)
		{
			long endDay;
			final Calendar calendar = Calendar.getInstance();
			if (value >= 30)
			{
				while (value >= 30)
				{
					if (calendar.get(Calendar.MONTH) == 11)
					{
						calendar.roll(Calendar.YEAR, true);
					}
					calendar.roll(Calendar.MONTH, true);
					value -= 30;
				}
			}
			if ((value < 30) && (value > 0))
			{
				while (value > 0)
				{
					if ((calendar.get(Calendar.DATE) == 28) && (calendar.get(Calendar.MONTH) == 1))
					{
						calendar.roll(Calendar.MONTH, true);
					}
					if (calendar.get(Calendar.DATE) == 30)
					{
						if (calendar.get(Calendar.MONTH) == 11)
						{
							calendar.roll(Calendar.YEAR, true);
						}
						calendar.roll(Calendar.MONTH, true);
					}
					calendar.roll(Calendar.DATE, true);
					value--;
				}
			}
			
			endDay = calendar.getTimeInMillis();
			if (process.equals("aio"))
			{
				_aioEndTime = endDay;
			}
			else
			{
				LOGGER.info("process " + process + "no Known while try set end date");
				return;
			}
			final Date dt = new Date(endDay);
			LOGGER.info("" + process + " end time for player " + getName() + " is " + dt);
		}
		else if (process.equals("aio"))
		{
			_aioEndTime = 0;
		}
		else
		{
			LOGGER.info("process " + process + "no Known while try set end date");
		}
	}
	
	/**
	 * Gets the aio end time.
	 * @return the aio end time
	 */
	public long getAioEndTime()
	{
		return _aioEndTime;
	}
	
	public void setExpGain(boolean value)
	{
		_expGain = value;
	}
	
	public boolean isExpGainEnabled()
	{
		return _expGain;
	}
	
	/**
	 * Gets the offline start time.
	 * @return the offline start time
	 */
	public long getOfflineStartTime()
	{
		return _offlineShopStart;
	}
	
	/**
	 * Sets the offline start time.
	 * @param time the new offline start time
	 */
	public void setOfflineStartTime(long time)
	{
		_offlineShopStart = time;
	}
	
	public float getCollisionRadius()
	{
		if (isMounted() && (_summon != null))
		{
			return _summon.getTemplate().getCollisionRadius();
		}
		return _appearance.isFemale() ? getBaseTemplate().getFCollisionRadiusFemale() : getBaseTemplate().getFCollisionRadius();
	}
	
	public float getCollisionHeight()
	{
		if (isMounted() && (_summon != null))
		{
			return _summon.getTemplate().getCollisionHeight();
		}
		return _appearance.isFemale() ? getBaseTemplate().getFCollisionHeightFemale() : getBaseTemplate().getFCollisionHeight();
	}
	
	/**
	 * Return true if character falling now On the start of fall return false for correct coord sync !.
	 * @param z the z
	 * @return true, if is falling
	 */
	public boolean isFalling(int z)
	{
		if (isDead() || isFlying() || isInsideZone(ZoneId.WATER))
		{
			return false;
		}
		
		if ((_fallingTimestamp != 0) && (Chronos.currentTimeMillis() < _fallingTimestamp))
		{
			return true;
		}
		
		final int deltaZ = getZ() - z;
		if (deltaZ <= getBaseTemplate().getFallHeight())
		{
			_fallingTimestamp = 0;
			return false;
		}
		
		// If there is no geodata loaded for the place we are, client Z correction might cause falling damage.
		if (!GeoEngine.getInstance().hasGeo(getX(), getY()))
		{
			_fallingTimestamp = 0;
			return false;
		}
		
		if (_fallingDamage == 0)
		{
			_fallingDamage = (int) Formulas.calcFallDam(this, deltaZ);
		}
		if (_fallingDamageTask != null)
		{
			_fallingDamageTask.cancel(true);
		}
		_fallingDamageTask = ThreadPool.schedule(() ->
		{
			if ((_fallingDamage > 0) && !isInvul())
			{
				reduceCurrentHp(Math.min(_fallingDamage, getCurrentHp() - 1), null, false);
				sendPacket(new SystemMessage(SystemMessageId.YOU_RECEIVED_S1_DAMAGE_FROM_TAKING_A_HIGH_FALL).addNumber(_fallingDamage));
			}
			_fallingDamage = 0;
			_fallingDamageTask = null;
		}, 1500);
		
		// Prevent falling under ground.
		sendPacket(new ValidateLocation(this));
		_fallingTimestamp = Chronos.currentTimeMillis() + FALLING_VALIDATION_DELAY;
		return false;
	}
	
	/**
	 * Sets the last party position.
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void setLastPartyPosition(int x, int y, int z)
	{
		_lastPartyPosition.setXYZ(x, y, z);
	}
	
	/**
	 * Gets the last party position distance.
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @return the last party position distance
	 */
	public int getLastPartyPositionDistance(int x, int y, int z)
	{
		final double dx = (x - _lastPartyPosition.getX());
		final double dy = (y - _lastPartyPosition.getY());
		final double dz = (z - _lastPartyPosition.getZ());
		return (int) Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
	}
	
	/**
	 * Checks if is locked.
	 * @return true, if is locked
	 */
	public boolean isLocked()
	{
		return _isLocked;
	}
	
	/**
	 * Sets the locked.
	 * @param a the new locked
	 */
	public void setLocked(boolean a)
	{
		_isLocked = a;
	}
	
	/**
	 * Checks if is stored.
	 * @return true, if is stored
	 */
	public boolean isStored()
	{
		return _isStored;
	}
	
	/**
	 * Sets the stored.
	 * @param a the new stored
	 */
	public void setStored(boolean a)
	{
		_isStored = a;
	}
	
	public enum PunishLevel
	{
		NONE(0, ""),
		CHAT(1, "chat banned"),
		JAIL(2, "jailed"),
		CHAR(3, "banned"),
		ACC(4, "banned");
		
		private final int punValue;
		private final String punString;
		
		/**
		 * Instantiates a new punish level.
		 * @param value the value
		 * @param string the string
		 */
		PunishLevel(int value, String string)
		{
			punValue = value;
			punString = string;
		}
		
		public int value()
		{
			return punValue;
		}
		
		public String string()
		{
			return punString;
		}
	}
	
	private static class GatesRequest
	{
		private Door _target = null;
		
		public GatesRequest()
		{
		}
		
		public void setTarget(Door door)
		{
			_target = door;
		}
		
		public Door getDoor()
		{
			return _target;
		}
	}
	
	public void gatesRequest(Door door)
	{
		_gatesRequest.setTarget(door);
	}
	
	public void gatesAnswer(int answer, int type)
	{
		if (_gatesRequest.getDoor() == null)
		{
			return;
		}
		
		if ((answer == 1) && (getTarget() == _gatesRequest.getDoor()) && (type == 1))
		{
			_gatesRequest.getDoor().openMe();
		}
		else if ((answer == 1) && (getTarget() == _gatesRequest.getDoor()) && (type == 0))
		{
			_gatesRequest.getDoor().closeMe();
		}
		
		_gatesRequest.setTarget(null);
	}
	
	/**
	 * returns punishment level of player.
	 * @return the punish level
	 */
	public PunishLevel getPunishLevel()
	{
		return _punishLevel;
	}
	
	/**
	 * Checks if is in jail.
	 * @return True if player is jailed
	 */
	public boolean isInJail()
	{
		return _punishLevel == PunishLevel.JAIL;
	}
	
	/**
	 * Checks if is chat banned.
	 * @return True if player is chat banned
	 */
	public boolean isChatBanned()
	{
		return _punishLevel == PunishLevel.CHAT;
	}
	
	/**
	 * Sets the punish level.
	 * @param state the new punish level
	 */
	public void setPunishLevel(int state)
	{
		switch (state)
		{
			case 0:
			{
				_punishLevel = PunishLevel.NONE;
				break;
			}
			case 1:
			{
				_punishLevel = PunishLevel.CHAT;
				break;
			}
			case 2:
			{
				_punishLevel = PunishLevel.JAIL;
				break;
			}
			case 3:
			{
				_punishLevel = PunishLevel.CHAR;
				break;
			}
			case 4:
			{
				_punishLevel = PunishLevel.ACC;
				break;
			}
		}
	}
	
	/**
	 * Sets the punish level.
	 * @param state the state
	 * @param delayInMinutes the delay in minutes
	 */
	public void setPunishLevel(PunishLevel state, int delayInMinutes)
	{
		final long delayInMilliseconds = delayInMinutes * 60000;
		setPunishLevel(state, delayInMilliseconds);
	}
	
	/**
	 * Sets punish level for player based on delay.
	 * @param state the state
	 * @param delayInMilliseconds 0 - Indefinite
	 */
	public void setPunishLevel(PunishLevel state, long delayInMilliseconds)
	{
		switch (state)
		{
			case NONE: // Remove Punishments
			{
				switch (_punishLevel)
				{
					case CHAT:
					{
						_punishLevel = state;
						stopPunishTask(true);
						sendPacket(new EtcStatusUpdate(this));
						sendMessage("Your Chat ban has been lifted");
						break;
					}
					case JAIL:
					{
						_punishLevel = state;
						// Open a Html message to inform the player
						final NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
						final String jailInfos = HtmCache.getInstance().getHtm("data/html/jail_out.htm");
						if (jailInfos != null)
						{
							htmlMsg.setHtml(jailInfos);
						}
						else
						{
							htmlMsg.setHtml("<html><body>You are free for now, respect server rules!</body></html>");
						}
						sendPacket(htmlMsg);
						stopPunishTask(true);
						teleToLocation(17836, 170178, -3507, true); // Floran
						break;
					}
				}
				break;
			}
			case CHAT: // Chat Ban
			{
				// not allow player to escape jail using chat ban
				if (_punishLevel == PunishLevel.JAIL)
				{
					break;
				}
				_punishLevel = state;
				_punishTimer = 0;
				sendPacket(new EtcStatusUpdate(this));
				// Remove the task if any
				stopPunishTask(false);
				
				if (delayInMilliseconds > 0)
				{
					_punishTimer = delayInMilliseconds;
					
					// start the countdown
					final int minutes = (int) (delayInMilliseconds / 60000);
					_punishTask = ThreadPool.schedule(new PunishTask(this), _punishTimer);
					sendMessage("You are chat banned for " + minutes + " minutes.");
				}
				else
				{
					sendMessage("You have been chat banned");
				}
				break;
				
			}
			case JAIL: // Jail Player
			{
				_punishLevel = state;
				_punishTimer = 0;
				// Remove the task if any
				stopPunishTask(false);
				
				if (delayInMilliseconds > 0)
				{
					_punishTimer = delayInMilliseconds; // Delay in milliseconds
					
					// start the countdown
					_punishTask = ThreadPool.schedule(new PunishTask(this), _punishTimer);
					sendMessage("You are in jail for " + (delayInMilliseconds / 60000) + " minutes.");
				}
				
				if (Olympiad.getInstance().isRegisteredInComp(this))
				{
					Olympiad.getInstance().removeDisconnectedCompetitor(this);
				}
				
				// Open a Html message to inform the player
				final NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
				final String jailInfos = HtmCache.getInstance().getHtm("data/html/jail_in.htm");
				if (jailInfos != null)
				{
					htmlMsg.setHtml(jailInfos);
				}
				else
				{
					htmlMsg.setHtml("<html><body>You have been put in jail by an admin.</body></html>");
				}
				sendPacket(htmlMsg);
				setInstanceId(0);
				setIn7sDungeon(false);
				
				teleToLocation(MapRegionData.JAIL_LOCATION, false);
				break;
			}
			case CHAR: // Ban Character
			{
				setAccessLevel(-100);
				logout();
				break;
			}
			case ACC: // Ban Account
			{
				setAccountAccesslevel(-100);
				logout();
				break;
			}
			default:
			{
				_punishLevel = state;
				break;
			}
		}
		
		// store in database
		storeCharBase();
	}
	
	/**
	 * Gets the punish timer.
	 * @return the punish timer
	 */
	public long getPunishTimer()
	{
		return _punishTimer;
	}
	
	/**
	 * Sets the punish timer.
	 * @param time the new punish timer
	 */
	public void setPunishTimer(long time)
	{
		_punishTimer = time;
	}
	
	/**
	 * Update punish state.
	 */
	private void updatePunishState()
	{
		if (getPunishLevel() != PunishLevel.NONE)
		{
			// If punish timer exists, restart punishtask.
			if (_punishTimer > 0)
			{
				_punishTask = ThreadPool.schedule(new PunishTask(this), _punishTimer);
				sendMessage("You are still " + getPunishLevel().string() + " for " + (_punishTimer / 60000) + " minutes.");
			}
			// If player escaped, put him back in jail
			if ((getPunishLevel() == PunishLevel.JAIL) && !isInsideZone(ZoneId.JAIL))
			{
				teleToLocation(MapRegionData.JAIL_LOCATION, false);
			}
		}
	}
	
	/**
	 * Stop punish task.
	 * @param save the save
	 */
	public void stopPunishTask(boolean save)
	{
		if (_punishTask != null)
		{
			if (save)
			{
				long delay = _punishTask.getDelay(TimeUnit.MILLISECONDS);
				if (delay < 0)
				{
					delay = 0;
				}
				setPunishTimer(delay);
			}
			_punishTask.cancel(false);
			_punishTask = null;
		}
	}
	
	private class PunishTask implements Runnable
	{
		Player _player;
		
		/**
		 * Instantiates a new punish task.
		 * @param player the player
		 */
		protected PunishTask(Player player)
		{
			_player = player;
		}
		
		@Override
		public void run()
		{
			_player.setPunishLevel(PunishLevel.NONE, 0);
		}
	}
	
	public void addConfirmDlgRequestTime(int requestId, int time)
	{
		_confirmDlgRequests.put(requestId, Chronos.currentTimeMillis() + time + 2000);
	}
	
	public Long getConfirmDlgRequestTime(int requestId)
	{
		return _confirmDlgRequests.get(requestId);
	}
	
	public void removeConfirmDlgRequestTime(int requestId)
	{
		_confirmDlgRequests.remove(requestId);
	}
	
	/**
	 * Test if player inventory is under 80% capaity.
	 * @return true, if is inventory under80
	 */
	public boolean isInventoryUnder80()
	{
		return getInventory().getSize() <= (getInventoryLimit() * 0.8);
	}
	
	/**
	 * Gets the multi sell id.
	 * @return the multi sell id
	 */
	public int getMultiSellId()
	{
		return _currentMultiSellId;
	}
	
	/**
	 * Sets the multi sell id.
	 * @param listid the new multi sell id
	 */
	public void setMultiSellId(int listid)
	{
		_currentMultiSellId = listid;
	}
	
	/**
	 * Checks if is party waiting.
	 * @return true, if is party waiting
	 */
	public boolean isPartyWaiting()
	{
		return PartyMatchWaitingList.getInstance().getPlayers().contains(this);
	}
	
	/**
	 * Sets the party room.
	 * @param id the new party room
	 */
	public void setPartyRoom(int id)
	{
		_partyroom = id;
	}
	
	/**
	 * Gets the party room.
	 * @return the party room
	 */
	public int getPartyRoom()
	{
		return _partyroom;
	}
	
	/**
	 * Checks if is in party match room.
	 * @return true, if is in party match room
	 */
	public boolean isInPartyMatchRoom()
	{
		return _partyroom > 0;
	}
	
	/**
	 * Checks if is item equipped by item id.
	 * @param itemId the item_id
	 * @return true, if is item equipped by item id
	 */
	public boolean isItemEquippedByItemId(int itemId)
	{
		if (_inventory == null)
		{
			return false;
		}
		
		if ((_inventory.getAllItemsByItemId(itemId) == null) || _inventory.getAllItemsByItemId(itemId).isEmpty())
		{
			return false;
		}
		
		return _inventory.checkIfEquipped(itemId);
	}
	
	/**
	 * Gets the _instance login time.
	 * @return the _instanceLoginTime
	 */
	public long getInstanceLoginTime()
	{
		return _instanceLoginTime;
	}
	
	/**
	 * Sets the sex db.
	 * @param player the player
	 * @param mode the mode
	 */
	public static void setSexDB(Player player, int mode)
	{
		if (player == null)
		{
			return;
		}
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("UPDATE characters SET sex=? WHERE charId=?");
			statement.setInt(1, player.getAppearance().isFemale() ? 1 : 0);
			statement.setInt(2, player.getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("SetSex:  Could not store data:" + e);
		}
	}
	
	@Override
	public Player getActingPlayer()
	{
		return this;
	}
	
	public void checkItemRestriction()
	{
		for (int i = 0; i < Inventory.PAPERDOLL_TOTALSLOTS; i++)
		{
			final Item equippedItem = getInventory().getPaperdollItem(i);
			if ((equippedItem != null) && !equippedItem.checkOlympCondition())
			{
				if (equippedItem.isAugmented())
				{
					equippedItem.getAugmentation().removeBonus(this);
				}
				final List<Item> items = getInventory().unEquipItemInSlotAndRecord(i);
				if (equippedItem.isWear())
				{
					continue;
				}
				SystemMessage sm = null;
				if (equippedItem.getEnchantLevel() > 0)
				{
					sm = new SystemMessage(SystemMessageId.THE_EQUIPMENT_S1_S2_HAS_BEEN_REMOVED);
					sm.addNumber(equippedItem.getEnchantLevel());
					sm.addItemName(equippedItem.getItemId());
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.S1_HAS_BEEN_DISARMED);
					sm.addItemName(equippedItem.getItemId());
				}
				sendPacket(sm);
				final InventoryUpdate iu = new InventoryUpdate();
				iu.addItems(items);
				sendPacket(iu);
				broadcastUserInfo();
			}
		}
	}
	
	public void enterOlympiadObserverMode(int x, int y, int z, int id, boolean storeCoords)
	{
		if (getPet() != null)
		{
			getPet().unSummon(this);
		}
		
		unsummonAllCubics();
		
		_olympiadGameId = id;
		if (isSitting())
		{
			standUp();
		}
		if (storeCoords)
		{
			_obsX = getX();
			_obsY = getY();
			_obsZ = getZ();
		}
		setTarget(null);
		setInvul(true);
		getAppearance().setInvisible();
		teleToLocation(x, y, z, true);
		sendPacket(new ExOlympiadMode(3, this));
		_observerMode = true;
		broadcastUserInfo();
	}
	
	public void leaveOlympiadObserverMode(boolean olymp)
	{
		setTarget(null);
		sendPacket(new ExOlympiadMode(0, this));
		teleToLocation(_obsX, _obsY, _obsZ, true);
		if (!AdminData.getInstance().hasAccess("admin_invis", getAccessLevel()))
		{
			getAppearance().setVisible();
		}
		if (!AdminData.getInstance().hasAccess("admin_invul", getAccessLevel()))
		{
			setInvul(false);
		}
		if (getAI() != null)
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		if (!olymp)
		{
			Olympiad.removeSpectator(_olympiadGameId, this);
		}
		_olympiadGameId = -1;
		_observerMode = false;
		broadcastUserInfo();
	}
	
	public void setHero(boolean hero)
	{
		_hero = hero;
		if (_hero && (_baseClass == _activeClass))
		{
			giveHeroSkills();
		}
		else if ((getCount() >= Config.HERO_COUNT) && _hero && Config.ALLOW_HERO_SUBSKILL)
		{
			giveHeroSkills();
		}
		else
		{
			removeHeroSkills();
		}
	}
	
	public void giveHeroSkills()
	{
		for (Skill s : HeroSkillTable.getHeroSkills())
		{
			addSkill(s, false); // Dont Save Hero skills to database
		}
		sendSkillList();
	}
	
	public void removeHeroSkills()
	{
		for (Skill s : HeroSkillTable.getHeroSkills())
		{
			super.removeSkill(s); // Just Remove skills from nonHero characters
		}
		sendSkillList();
	}
	
	/**
	 * Get the current pet skill in use or return null.<br>
	 * @return
	 */
	public SkillUseHolder getCurrentPetSkill()
	{
		return _currentPetSkill;
	}
	
	/**
	 * Create a new SkillUseHolder object and set the player _currentPetSkill.
	 * @param currentSkill
	 * @param ctrlPressed
	 * @param shiftPressed
	 */
	public void setCurrentPetSkill(Skill currentSkill, boolean ctrlPressed, boolean shiftPressed)
	{
		if (currentSkill == null)
		{
			_currentPetSkill = null;
			return;
		}
		_currentPetSkill = new SkillUseHolder(currentSkill, ctrlPressed, shiftPressed);
	}
	
	@Override
	public boolean isPlayer()
	{
		return true;
	}
}