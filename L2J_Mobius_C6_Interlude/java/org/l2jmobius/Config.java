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
package org.l2jmobius;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.l2jmobius.commons.enums.IdFactoryType;
import org.l2jmobius.commons.enums.ServerMode;
import org.l2jmobius.commons.util.ClassMasterSettings;
import org.l2jmobius.commons.util.L2Properties;
import org.l2jmobius.commons.util.StringUtil;
import org.l2jmobius.gameserver.model.entity.olympiad.OlympiadPeriod;
import org.l2jmobius.gameserver.util.FloodProtectorConfig;
import org.l2jmobius.loginserver.LoginController;

public class Config
{
	private static final Logger LOGGER = Logger.getLogger(Config.class.getName());
	
	// --------------------------------------------------
	// Files
	// --------------------------------------------------
	
	// standard
	private static final String FILTER_FILE = "./config/chatfilter.txt";
	private static final String HEXID_FILE = "./config/hexid.txt";
	// main
	private static final String ACCESS_CONFIG_FILE = "./config/main/Access.ini";
	private static final String CHARACTER_CONFIG_FILE = "./config/main/Character.ini";
	private static final String CLANHALL_CONFIG_FILE = "./config/main/Clanhall.ini";
	public static final String CLASS_DAMAGE_CONFIG_FILE = "./config/main/ClassDamage.ini";
	private static final String CRAFTING_CONFIG_FILE = "./config/main/Crafting.ini";
	private static final String ELIT_CLANHALL_CONFIG_FILE = "./config/main/EliteClanhall.ini";
	private static final String ENCHANT_CONFIG_FILE = "./config/main/Enchant.ini";
	public static final String FORTSIEGE_CONFIG_FILE = "./config/main/Fort.ini";
	private static final String GENERAL_CONFIG_FILE = "./config/main/General.ini";
	private static final String GEODATA_CONFIG_FILE = "./config/main/GeoEngine.ini";
	private static final String OLYMP_CONFIG_FILE = "./config/main/Olympiad.ini";
	private static final String PHYSICS_CONFIG_FILE = "./config/main/Physics.ini";
	private static final String PVP_CONFIG_FILE = "./config/main/PvP.ini";
	private static final String RAIDBOSS_CONFIG_FILE = "./config/main/RaidBoss.ini";
	private static final String RATES_CONFIG_FILE = "./config/main/Rates.ini";
	private static final String SERVER_CONFIG_FILE = "./config/main/Server.ini";
	private static final String SEVENSIGNS_CONFIG_FILE = "./config/main/SevenSigns.ini";
	public static final String SIEGE_CONFIG_FILE = "./config/main/Siege.ini";
	// protected
	private static final String DAEMONS_CONFIG_FILE = "./config/protected/Daemons.ini";
	private static final String PROTECT_FLOOD_CONFIG_FILE = "./config/protected/Flood.ini";
	private static final String ID_CONFIG_FILE = "./config/protected/IdFactory.ini";
	private static final String PROTECT_OTHER_CONFIG_FILE = "./config/protected/Other.ini";
	public static final String TELNET_CONFIG_FILE = "./config/protected/Telnet.ini";
	// events
	private static final String EVENT_CTF_CONFIG_FILE = "./config/events/CtF.ini";
	private static final String EVENT_DM_CONFIG_FILE = "./config/events/DM.ini";
	private static final String EVENT_PC_BANG_POINT_CONFIG_FILE = "./config/events/PcBang.ini";
	private static final String EVENT_TVT_CONFIG_FILE = "./config/events/TvT.ini";
	private static final String EVENT_TW_CONFIG_FILE = "./config/events/TW.ini";
	// custom
	private static final String AWAY_CONFIG_FILE = "./config/custom/Away.ini";
	private static final String BANK_CONFIG_FILE = "./config/custom/Bank.ini";
	private static final String CHAMPION_CONFIG_FILE = "./config/custom/Champion.ini";
	private static final String MERCHANT_ZERO_SELL_PRICE_CONFIG_FILE = "./config/custom/MerchantZeroSellPrice.ini";
	private static final String OFFLINE_CONFIG_FILE = "./config/custom/Offline.ini";
	private static final String OTHER_CONFIG_FILE = "./config/custom/Other.ini";
	private static final String SCHEME_BUFFER_CONFIG_FILE = "./config/custom/SchemeBuffer.ini";
	private static final String EVENT_REBIRTH_CONFIG_FILE = "./config/custom/Rebirth.ini";
	private static final String EVENT_WEDDING_CONFIG_FILE = "./config/custom/Wedding.ini";
	// login
	private static final String LOGIN_CONFIG_FILE = "./config/main/LoginServer.ini";
	// others
	private static final String BANNED_IP_FILE = "./config/others/banned_ip.cfg";
	public static final String SERVER_NAME_FILE = "./config/others/servername.xml";
	// legacy
	private static final String LEGACY_BANNED_IP = "./config/banned_ip.cfg";
	
	// --------------------------------------------------
	// Constants
	// --------------------------------------------------
	public static final String EOL = System.lineSeparator();
	
	public static ServerMode SERVER_MODE = ServerMode.NONE;
	
	public static boolean EVERYBODY_HAS_ADMIN_RIGHTS;
	public static int MASTERACCESS_LEVEL;
	public static int USERACCESS_LEVEL;
	public static boolean MASTERACCESS_NAME_COLOR_ENABLED;
	public static boolean MASTERACCESS_TITLE_COLOR_ENABLED;
	public static int MASTERACCESS_NAME_COLOR;
	public static int MASTERACCESS_TITLE_COLOR;
	public static boolean SHOW_GM_LOGIN;
	public static boolean GM_STARTUP_INVISIBLE;
	public static boolean GM_SPECIAL_EFFECT;
	public static boolean GM_STARTUP_SILENCE;
	public static boolean GM_STARTUP_AUTO_LIST;
	public static String GM_ADMIN_MENU_STYLE;
	public static boolean GM_HERO_AURA;
	public static boolean GM_STARTUP_BUILDER_HIDE;
	public static boolean GM_STARTUP_INVULNERABLE;
	public static boolean GM_ANNOUNCER_NAME;
	public static boolean GM_CRITANNOUNCER_NAME;
	public static boolean GM_DEBUG_HTML_PATHS;
	public static boolean USE_SUPER_HASTE_AS_GM_SPEED;
	
	public static String DEFAULT_GLOBAL_CHAT;
	public static String DEFAULT_TRADE_CHAT;
	public static boolean TRADE_CHAT_WITH_PVP;
	public static int TRADE_PVP_AMOUNT;
	public static boolean GLOBAL_CHAT_WITH_PVP;
	public static int GLOBAL_PVP_AMOUNT;
	public static int BRUT_AVG_TIME;
	public static int BRUT_LOGON_ATTEMPTS;
	public static int BRUT_BAN_IP_TIME;
	public static int MAX_CHAT_LENGTH;
	public static boolean TRADE_CHAT_IS_NOOBLE;
	public static boolean PRECISE_DROP_CALCULATION;
	public static boolean MULTIPLE_ITEM_DROP;
	public static int DELETE_DAYS;
	public static int MAX_DRIFT_RANGE;
	public static boolean ALLOWFISHING;
	public static boolean ALLOW_MANOR;
	public static int AUTODESTROY_ITEM_AFTER;
	public static int HERB_AUTO_DESTROY_TIME;
	public static String PROTECTED_ITEMS;
	public static List<Integer> LIST_PROTECTED_ITEMS = new ArrayList<>();
	public static boolean DESTROY_DROPPED_PLAYER_ITEM;
	public static boolean DESTROY_EQUIPABLE_PLAYER_ITEM;
	public static boolean SAVE_DROPPED_ITEM;
	public static boolean EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD;
	public static int SAVE_DROPPED_ITEM_INTERVAL;
	public static boolean CLEAR_DROPPED_ITEM_TABLE;
	public static boolean ALLOW_DISCARDITEM;
	public static boolean ALLOW_FREIGHT;
	public static boolean ALLOW_WAREHOUSE;
	public static boolean WAREHOUSE_CACHE;
	public static int WAREHOUSE_CACHE_TIME;
	public static boolean ALLOW_WEAR;
	public static int WEAR_DELAY;
	public static int WEAR_PRICE;
	public static boolean ALLOW_LOTTERY;
	public static boolean ALLOW_RACE;
	public static boolean ALLOW_RENTPET;
	public static boolean ALLOW_BOAT;
	public static boolean ALLOW_CURSED_WEAPONS;
	public static boolean ALLOW_NPC_WALKERS;
	public static int MIN_NPC_ANIMATION;
	public static int MAX_NPC_ANIMATION;
	public static int MIN_MONSTER_ANIMATION;
	public static int MAX_MONSTER_ANIMATION;
	public static boolean USE_3D_MAP;
	public static boolean ENABLE_COMMUNITY_BOARD;
	public static String BBS_DEFAULT;
	public static int PATH_NODE_RADIUS;
	public static int NEW_NODE_ID;
	public static int SELECTED_NODE_ID;
	public static int LINKED_NODE_ID;
	public static String NEW_NODE_TYPE;
	public static boolean SHOW_NPC_LVL;
	public static boolean SHOW_NPC_AGGRESSION;
	public static int ZONE_TOWN;
	public static boolean COUNT_PACKETS = false;
	public static boolean DUMP_PACKET_COUNTS = false;
	public static int DUMP_INTERVAL_SECONDS = 60;
	public static int DEFAULT_PUNISH;
	public static int DEFAULT_PUNISH_PARAM;
	public static boolean AUTODELETE_INVALID_QUEST_DATA;
	public static boolean GRIDS_ALWAYS_ON;
	public static int GRID_NEIGHBOR_TURNON_TIME;
	public static int GRID_NEIGHBOR_TURNOFF_TIME;
	public static int MINIMUM_UPDATE_DISTANCE;
	public static int KNOWNLIST_FORGET_DELAY;
	public static int MINIMUN_UPDATE_TIME;
	public static boolean BYPASS_VALIDATION;
	public static boolean HIGH_RATE_SERVER_DROPS;
	public static boolean FORCE_COMPLETE_STATUS_UPDATE;
	
	public static int PORT_GAME;
	public static String GAMESERVER_HOSTNAME;
	public static String DATABASE_DRIVER;
	public static String DATABASE_URL;
	public static String DATABASE_LOGIN;
	public static String DATABASE_PASSWORD;
	public static int DATABASE_MAX_CONNECTIONS;
	public static boolean BACKUP_DATABASE;
	public static String MYSQL_BIN_PATH;
	public static String BACKUP_PATH;
	public static int BACKUP_DAYS;
	public static boolean RESERVE_HOST_ON_LOGIN = false;
	
	public static boolean IS_TELNET_ENABLED;
	
	public static IdFactoryType IDFACTORY_TYPE;
	public static boolean BAD_ID_CHECKING;
	
	public static boolean JAIL_IS_PVP;
	public static boolean JAIL_DISABLE_CHAT;
	public static int WYVERN_SPEED;
	public static int STRIDER_SPEED;
	public static boolean ALLOW_WYVERN_UPGRADER;
	public static String NONDROPPABLE_ITEMS;
	public static List<Integer> LIST_NONDROPPABLE_ITEMS = new ArrayList<>();
	public static String PET_RENT_NPC;
	public static List<Integer> LIST_PET_RENT_NPC = new ArrayList<>();
	public static boolean ENABLE_AIO_SYSTEM;
	public static Map<Integer, Integer> AIO_SKILLS;
	public static boolean ALLOW_AIO_NCOLOR;
	public static int AIO_NCOLOR;
	public static boolean ALLOW_AIO_TCOLOR;
	public static int AIO_TCOLOR;
	public static boolean ALLOW_AIO_USE_GK;
	public static boolean ALLOW_AIO_USE_CM;
	public static boolean ALLOW_AIO_IN_EVENTS;
	public static int CHAT_FILTER_PUNISHMENT_PARAM1;
	public static int CHAT_FILTER_PUNISHMENT_PARAM2;
	public static int CHAT_FILTER_PUNISHMENT_PARAM3;
	public static boolean USE_SAY_FILTER;
	public static String CHAT_FILTER_CHARS;
	public static String CHAT_FILTER_PUNISHMENT;
	public static List<String> FILTER_LIST = new ArrayList<>();
	public static int FS_TIME_ATTACK;
	public static int FS_TIME_COOLDOWN;
	public static int FS_TIME_ENTRY;
	public static int FS_TIME_WARMUP;
	public static int FS_PARTY_MEMBER_COUNT;
	public static boolean ALLOW_QUAKE_SYSTEM;
	public static boolean ENABLE_ANTI_PVP_FARM_MSG;
	public static float RATE_XP;
	public static float RATE_SP;
	public static float RATE_PARTY_XP;
	public static float RATE_PARTY_SP;
	public static float RATE_QUESTS_REWARD;
	public static float RATE_DROP_ADENA;
	public static float RATE_CONSUMABLE_COST;
	public static float RATE_DROP_ITEMS;
	public static float RATE_DROP_SEAL_STONES;
	public static float RATE_DROP_SPOIL;
	public static int RATE_DROP_MANOR;
	public static float RATE_DROP_QUEST;
	public static float RATE_KARMA_EXP_LOST;
	public static float RATE_SIEGE_GUARDS_PRICE;
	public static float RATE_DROP_COMMON_HERBS;
	public static float RATE_DROP_MP_HP_HERBS;
	public static float RATE_DROP_GREATER_HERBS;
	public static float RATE_DROP_SUPERIOR_HERBS;
	public static float RATE_DROP_SPECIAL_HERBS;
	public static int PLAYER_DROP_LIMIT;
	public static int PLAYER_RATE_DROP;
	public static int PLAYER_RATE_DROP_ITEM;
	public static int PLAYER_RATE_DROP_EQUIP;
	public static int PLAYER_RATE_DROP_EQUIP_WEAPON;
	public static float PET_XP_RATE;
	public static int PET_FOOD_RATE;
	public static float SINEATER_XP_RATE;
	public static int KARMA_DROP_LIMIT;
	public static int KARMA_RATE_DROP;
	public static int KARMA_RATE_DROP_ITEM;
	public static int KARMA_RATE_DROP_EQUIP;
	public static int KARMA_RATE_DROP_EQUIP_WEAPON;
	public static float ADENA_BOSS;
	public static float ADENA_RAID;
	public static float ADENA_MINION;
	public static float ITEMS_BOSS;
	public static float ITEMS_RAID;
	public static float ITEMS_MINION;
	public static float SPOIL_BOSS;
	public static float SPOIL_RAID;
	public static float SPOIL_MINION;
	
	public static boolean REMOVE_CASTLE_CIRCLETS;
	public static float ALT_GAME_SKILL_HIT_RATE;
	public static boolean ALT_GAME_VIEWNPC;
	public static int ALT_CLAN_MEMBERS_FOR_WAR;
	public static int ALT_CLAN_JOIN_DAYS;
	public static int ALT_CLAN_CREATE_DAYS;
	public static int ALT_CLAN_DISSOLVE_DAYS;
	public static int ALT_ALLY_JOIN_DAYS_WHEN_LEAVED;
	public static int ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED;
	public static int ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED;
	public static int ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED;
	public static boolean ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE;
	public static boolean ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH;
	public static int ALT_MAX_NUM_OF_CLANS_IN_ALLY;
	public static boolean ALT_PRIVILEGES_SECURE_CHECK;
	public static int ALT_PRIVILEGES_DEFAULT_LEVEL;
	public static int ALT_MANOR_REFRESH_TIME;
	public static int ALT_MANOR_REFRESH_MIN;
	public static int ALT_MANOR_APPROVE_TIME;
	public static int ALT_MANOR_APPROVE_MIN;
	public static int ALT_MANOR_MAINTENANCE_PERIOD;
	public static boolean ALT_MANOR_SAVE_ALL_ACTIONS;
	public static int ALT_MANOR_SAVE_PERIOD_RATE;
	public static int ALT_LOTTERY_PRIZE;
	public static int ALT_LOTTERY_TICKET_PRICE;
	public static float ALT_LOTTERY_5_NUMBER_RATE;
	public static float ALT_LOTTERY_4_NUMBER_RATE;
	public static float ALT_LOTTERY_3_NUMBER_RATE;
	public static int ALT_LOTTERY_2_AND_1_NUMBER_PRIZE;
	public static boolean ALT_FISH_CHAMPIONSHIP_ENABLED;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_ITEM;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_1;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_2;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_3;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_4;
	public static int ALT_FISH_CHAMPIONSHIP_REWARD_5;
	public static int RIFT_MIN_PARTY_SIZE;
	public static int RIFT_SPAWN_DELAY;
	public static int RIFT_MAX_JUMPS;
	public static int RIFT_AUTO_JUMPS_TIME_MIN;
	public static int RIFT_AUTO_JUMPS_TIME_MAX;
	public static int RIFT_ENTER_COST_RECRUIT;
	public static int RIFT_ENTER_COST_SOLDIER;
	public static int RIFT_ENTER_COST_OFFICER;
	public static int RIFT_ENTER_COST_CAPTAIN;
	public static int RIFT_ENTER_COST_COMMANDER;
	public static int RIFT_ENTER_COST_HERO;
	public static float RIFT_BOSS_ROOM_TIME_MUTIPLY;
	public static boolean FORCE_INVENTORY_UPDATE;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_SHOP;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_GK;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_TELEPORT;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_TRADE;
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE;
	public static boolean ALT_KARMA_TELEPORT_TO_FLORAN;
	public static boolean DONT_DESTROY_SS;
	public static int STANDARD_RESPAWN_DELAY;
	public static int RAID_RANKING_1ST;
	public static int RAID_RANKING_2ND;
	public static int RAID_RANKING_3RD;
	public static int RAID_RANKING_4TH;
	public static int RAID_RANKING_5TH;
	public static int RAID_RANKING_6TH;
	public static int RAID_RANKING_7TH;
	public static int RAID_RANKING_8TH;
	public static int RAID_RANKING_9TH;
	public static int RAID_RANKING_10TH;
	public static int RAID_RANKING_UP_TO_50TH;
	public static int RAID_RANKING_UP_TO_100TH;
	public static boolean EXPERTISE_PENALTY;
	public static boolean MASTERY_PENALTY;
	public static int LEVEL_TO_GET_PENALTY;
	public static boolean MASTERY_WEAPON_PENALTY;
	public static int LEVEL_TO_GET_WEAPON_PENALTY;
	public static int ACTIVE_AUGMENTS_START_REUSE_TIME;
	public static boolean NPC_ATTACKABLE;
	public static List<Integer> INVUL_NPC_LIST;
	public static boolean DISABLE_ATTACK_NPC_TYPE;
	public static String ALLOWED_NPC_TYPES;
	public static List<String> LIST_ALLOWED_NPC_TYPES = new ArrayList<>();
	public static boolean SELL_BY_ITEM;
	public static int SELL_ITEM;
	public static String DISABLE_BOW_CLASSES_STRING;
	public static List<Integer> DISABLE_BOW_CLASSES = new ArrayList<>();
	public static boolean ALT_MOBS_STATS_BONUS;
	public static boolean ALT_PETS_STATS_BONUS;
	public static double RAID_HP_REGEN_MULTIPLIER;
	public static double RAID_MP_REGEN_MULTIPLIER;
	public static double RAID_P_DEFENCE_MULTIPLIER;
	public static double RAID_M_DEFENCE_MULTIPLIER;
	public static double RAID_MINION_RESPAWN_TIMER;
	public static float RAID_MIN_RESPAWN_MULTIPLIER;
	public static float RAID_MAX_RESPAWN_MULTIPLIER;
	public static long CLICK_TASK;
	public static boolean ANNOUNCE_CASTLE_LORDS;
	public static boolean ANNOUNCE_MAMMON_SPAWN;
	public static boolean ALLOW_GUARDS;
	
	public static boolean ALT_GAME_REQUIRE_CASTLE_DAWN;
	public static boolean ALT_GAME_REQUIRE_CLAN_CASTLE;
	public static boolean ALT_REQUIRE_WIN_7S;
	public static int ALT_FESTIVAL_MIN_PLAYER;
	public static int ALT_MAXIMUM_PLAYER_CONTRIB;
	public static long ALT_FESTIVAL_MANAGER_START;
	public static long ALT_FESTIVAL_LENGTH;
	public static long ALT_FESTIVAL_CYCLE_LENGTH;
	public static long ALT_FESTIVAL_FIRST_SPAWN;
	public static long ALT_FESTIVAL_FIRST_SWARM;
	public static long ALT_FESTIVAL_SECOND_SPAWN;
	public static long ALT_FESTIVAL_SECOND_SWARM;
	public static long ALT_FESTIVAL_CHEST_SPAWN;
	
	public static long CH_TELE_FEE_RATIO;
	public static int CH_TELE1_FEE;
	public static int CH_TELE2_FEE;
	public static long CH_ITEM_FEE_RATIO;
	public static int CH_ITEM1_FEE;
	public static int CH_ITEM2_FEE;
	public static int CH_ITEM3_FEE;
	public static long CH_MPREG_FEE_RATIO;
	public static int CH_MPREG1_FEE;
	public static int CH_MPREG2_FEE;
	public static int CH_MPREG3_FEE;
	public static int CH_MPREG4_FEE;
	public static int CH_MPREG5_FEE;
	public static long CH_HPREG_FEE_RATIO;
	public static int CH_HPREG1_FEE;
	public static int CH_HPREG2_FEE;
	public static int CH_HPREG3_FEE;
	public static int CH_HPREG4_FEE;
	public static int CH_HPREG5_FEE;
	public static int CH_HPREG6_FEE;
	public static int CH_HPREG7_FEE;
	public static int CH_HPREG8_FEE;
	public static int CH_HPREG9_FEE;
	public static int CH_HPREG10_FEE;
	public static int CH_HPREG11_FEE;
	public static int CH_HPREG12_FEE;
	public static int CH_HPREG13_FEE;
	public static long CH_EXPREG_FEE_RATIO;
	public static int CH_EXPREG1_FEE;
	public static int CH_EXPREG2_FEE;
	public static int CH_EXPREG3_FEE;
	public static int CH_EXPREG4_FEE;
	public static int CH_EXPREG5_FEE;
	public static int CH_EXPREG6_FEE;
	public static int CH_EXPREG7_FEE;
	public static long CH_SUPPORT_FEE_RATIO;
	public static int CH_SUPPORT1_FEE;
	public static int CH_SUPPORT2_FEE;
	public static int CH_SUPPORT3_FEE;
	public static int CH_SUPPORT4_FEE;
	public static int CH_SUPPORT5_FEE;
	public static int CH_SUPPORT6_FEE;
	public static int CH_SUPPORT7_FEE;
	public static int CH_SUPPORT8_FEE;
	public static long CH_CURTAIN_FEE_RATIO;
	public static int CH_CURTAIN1_FEE;
	public static int CH_CURTAIN2_FEE;
	public static long CH_FRONT_FEE_RATIO;
	public static int CH_FRONT1_FEE;
	public static int CH_FRONT2_FEE;
	
	public static int DEVASTATED_DAY;
	public static int DEVASTATED_HOUR;
	public static int DEVASTATED_MINUTES;
	public static int PARTISAN_DAY;
	public static int PARTISAN_HOUR;
	public static int PARTISAN_MINUTES;
	
	public static boolean L2JMOD_CHAMPION_ENABLE;
	public static int L2JMOD_CHAMPION_FREQUENCY;
	public static int L2JMOD_CHAMP_MIN_LVL;
	public static int L2JMOD_CHAMP_MAX_LVL;
	public static int L2JMOD_CHAMPION_HP;
	public static int L2JMOD_CHAMPION_REWARDS;
	public static int L2JMOD_CHAMPION_ADENAS_REWARDS;
	public static float L2JMOD_CHAMPION_HP_REGEN;
	public static float L2JMOD_CHAMPION_ATK;
	public static float L2JMOD_CHAMPION_SPD_ATK;
	public static int L2JMOD_CHAMPION_REWARD;
	public static int L2JMOD_CHAMPION_REWARD_ID;
	public static int L2JMOD_CHAMPION_REWARD_QTY;
	public static String L2JMOD_CHAMP_TITLE;
	
	public static boolean MERCHANT_ZERO_SELL_PRICE;
	
	public static boolean L2JMOD_ALLOW_WEDDING;
	public static int L2JMOD_WEDDING_PRICE;
	public static boolean L2JMOD_WEDDING_PUNISH_INFIDELITY;
	public static boolean L2JMOD_WEDDING_TELEPORT;
	public static int L2JMOD_WEDDING_TELEPORT_PRICE;
	public static int L2JMOD_WEDDING_TELEPORT_DURATION;
	public static int L2JMOD_WEDDING_NAME_COLOR_NORMAL;
	public static int L2JMOD_WEDDING_NAME_COLOR_GEY;
	public static int L2JMOD_WEDDING_NAME_COLOR_LESBO;
	public static boolean L2JMOD_WEDDING_SAMESEX;
	public static boolean L2JMOD_WEDDING_FORMALWEAR;
	public static int L2JMOD_WEDDING_DIVORCE_COSTS;
	public static boolean WEDDING_GIVE_CUPID_BOW;
	public static boolean ANNOUNCE_WEDDING;
	
	public static String TVT_EVEN_TEAMS;
	public static boolean TVT_ALLOW_INTERFERENCE;
	public static boolean TVT_ALLOW_POTIONS;
	public static boolean TVT_ALLOW_SUMMON;
	public static boolean TVT_ON_START_REMOVE_ALL_EFFECTS;
	public static boolean TVT_ON_START_UNSUMMON_PET;
	public static boolean TVT_REVIVE_RECOVERY;
	public static boolean TVT_ANNOUNCE_TEAM_STATS;
	public static boolean TVT_ANNOUNCE_REWARD;
	public static boolean TVT_PRICE_NO_KILLS;
	public static boolean TVT_JOIN_CURSED;
	public static boolean TVT_COMMAND;
	public static long TVT_REVIVE_DELAY;
	public static boolean TVT_OPEN_FORT_DOORS;
	public static boolean TVT_CLOSE_FORT_DOORS;
	public static boolean TVT_OPEN_ADEN_COLOSSEUM_DOORS;
	public static boolean TVT_CLOSE_ADEN_COLOSSEUM_DOORS;
	public static int TVT_TOP_KILLER_REWARD;
	public static int TVT_TOP_KILLER_QTY;
	public static boolean TVT_AURA;
	public static boolean TVT_STATS_LOGGER;
	public static boolean TVT_REMOVE_BUFFS_ON_DIE;
	
	public static int TW_TOWN_ID;
	public static boolean TW_ALL_TOWNS;
	public static int TW_ITEM_ID;
	public static int TW_ITEM_AMOUNT;
	public static boolean TW_ALLOW_KARMA;
	public static boolean TW_DISABLE_GK;
	public static boolean TW_RESS_ON_DIE;
	
	public static boolean REBIRTH_ENABLE;
	public static String[] REBIRTH_ITEM_PRICE;
	public static String[] REBIRTH_MAGE_SKILL;
	public static String[] REBIRTH_FIGHTER_SKILL;
	public static int REBIRTH_MIN_LEVEL;
	public static int REBIRTH_MAX;
	public static int REBIRTH_RETURN_TO_LEVEL;
	
	public static boolean PCB_ENABLE;
	public static int PCB_MIN_LEVEL;
	public static int PCB_POINT_MIN;
	public static int PCB_POINT_MAX;
	public static int PCB_CHANCE_DUAL_POINT;
	public static int PCB_INTERVAL;
	
	public static boolean ALT_DEV_NO_QUESTS;
	public static boolean ALT_DEV_NO_SPAWNS;
	public static boolean ALT_DEV_NO_SCRIPT;
	public static boolean ALT_DEV_NO_RB;
	public static boolean SERVER_LIST_TESTSERVER;
	public static boolean SERVER_GMONLY;
	public static boolean GMAUDIT;
	public static boolean LOG_CHAT;
	public static boolean LOG_ITEMS;
	
	public static boolean LAZY_CACHE;
	
	public static boolean IS_CRAFTING_ENABLED;
	public static int DWARF_RECIPE_LIMIT;
	public static int COMMON_RECIPE_LIMIT;
	public static boolean ALT_GAME_CREATION;
	public static double ALT_GAME_CREATION_SPEED;
	public static double ALT_GAME_CREATION_XP_RATE;
	public static double ALT_GAME_CREATION_SP_RATE;
	public static boolean ALT_BLACKSMITH_USE_RECIPES;
	
	public static boolean ALLOW_AWAY_STATUS;
	public static int AWAY_TIMER;
	public static int BACK_TIMER;
	public static int AWAY_TITLE_COLOR;
	public static boolean AWAY_PLAYER_TAKE_AGGRO;
	public static boolean AWAY_PEACE_ZONE;
	
	public static boolean BANKING_SYSTEM_ENABLED;
	public static int BANKING_SYSTEM_GOLDBARS;
	public static int BANKING_SYSTEM_ADENA;
	
	public static int BUFFER_MAX_SCHEMES;
	public static int BUFFER_STATIC_BUFF_COST;
	
	public static boolean OFFLINE_TRADE_ENABLE;
	public static boolean OFFLINE_CRAFT_ENABLE;
	public static boolean OFFLINE_SET_NAME_COLOR;
	public static int OFFLINE_NAME_COLOR;
	public static boolean OFFLINE_MODE_IN_PEACE_ZONE;
	public static boolean OFFLINE_MODE_SET_INVULNERABLE;
	public static boolean OFFLINE_COMMAND1;
	public static boolean OFFLINE_COMMAND2;
	public static boolean OFFLINE_LOGOUT;
	public static boolean OFFLINE_SLEEP_EFFECT;
	public static boolean RESTORE_OFFLINERS;
	public static int OFFLINE_MAX_DAYS;
	public static boolean OFFLINE_DISCONNECT_FINISHED;
	
	public static boolean DM_ALLOW_INTERFERENCE;
	public static boolean DM_ALLOW_POTIONS;
	public static boolean DM_ALLOW_SUMMON;
	public static boolean DM_JOIN_CURSED;
	public static boolean DM_ON_START_REMOVE_ALL_EFFECTS;
	public static boolean DM_ON_START_UNSUMMON_PET;
	public static long DM_REVIVE_DELAY;
	public static boolean DM_COMMAND;
	public static boolean DM_ENABLE_KILL_REWARD;
	public static int DM_KILL_REWARD_ID;
	public static int DM_KILL_REWARD_AMOUNT;
	public static boolean DM_ANNOUNCE_REWARD;
	public static boolean DM_REVIVE_RECOVERY;
	public static int DM_SPAWN_OFFSET;
	public static boolean DM_STATS_LOGGER;
	public static boolean DM_ALLOW_HEALER_CLASSES;
	public static boolean DM_REMOVE_BUFFS_ON_DIE;
	
	public static String CTF_EVEN_TEAMS;
	public static boolean CTF_ALLOW_INTERFERENCE;
	public static boolean CTF_ALLOW_POTIONS;
	public static boolean CTF_ALLOW_SUMMON;
	public static boolean CTF_ON_START_REMOVE_ALL_EFFECTS;
	public static boolean CTF_ON_START_UNSUMMON_PET;
	public static boolean CTF_ANNOUNCE_TEAM_STATS;
	public static boolean CTF_ANNOUNCE_REWARD;
	public static boolean CTF_JOIN_CURSED;
	public static boolean CTF_REVIVE_RECOVERY;
	public static boolean CTF_COMMAND;
	public static boolean CTF_AURA;
	public static boolean CTF_STATS_LOGGER;
	public static int CTF_SPAWN_OFFSET;
	public static boolean CTF_REMOVE_BUFFS_ON_DIE;
	
	public static boolean ONLINE_PLAYERS_ON_LOGIN;
	public static boolean SUBSTUCK_SKILLS;
	public static boolean ALT_SERVER_NAME_ENABLED;
	public static boolean ANNOUNCE_TO_ALL_SPAWN_RB;
	public static boolean ANNOUNCE_TRY_BANNED_ACCOUNT;
	public static String ALT_Server_Name;
	public static boolean DONATOR_NAME_COLOR_ENABLED;
	public static int DONATOR_NAME_COLOR;
	public static int DONATOR_TITLE_COLOR;
	public static float DONATOR_XPSP_RATE;
	public static float DONATOR_ADENA_RATE;
	public static float DONATOR_DROP_RATE;
	public static float DONATOR_SPOIL_RATE;
	public static boolean CUSTOM_SPAWNLIST_TABLE;
	public static boolean SAVE_GMSPAWN_ON_CUSTOM;
	public static boolean DELETE_GMSPAWN_ON_CUSTOM;
	public static boolean CUSTOM_NPC_TABLE = true;
	public static boolean CUSTOM_TELEPORT_TABLE = true;
	public static boolean CUSTOM_DROPLIST_TABLE = true;
	public static boolean CUSTOM_MERCHANT_TABLES = true;
	public static boolean ALLOW_SIMPLE_STATS_VIEW;
	public static boolean ALLOW_DETAILED_STATS_VIEW;
	public static boolean ALLOW_ONLINE_VIEW;
	public static boolean WELCOME_HTM;
	public static String ALLOWED_SKILLS;
	public static List<Integer> ALLOWED_SKILLS_LIST = new ArrayList<>();
	public static boolean PROTECTOR_PLAYER_PK;
	public static boolean PROTECTOR_PLAYER_PVP;
	public static int PROTECTOR_RADIUS_ACTION;
	public static int PROTECTOR_SKILLID;
	public static int PROTECTOR_SKILLLEVEL;
	public static int PROTECTOR_SKILLTIME;
	public static String PROTECTOR_MESSAGE;
	public static boolean CASTLE_SHIELD;
	public static boolean CLANHALL_SHIELD;
	public static boolean APELLA_ARMORS;
	public static boolean OATH_ARMORS;
	public static boolean CASTLE_CROWN;
	public static boolean CASTLE_CIRCLETS;
	public static boolean KEEP_SUBCLASS_SKILLS;
	public static boolean CHAR_TITLE;
	public static String ADD_CHAR_TITLE;
	public static boolean NOBLE_CUSTOM_ITEMS;
	public static boolean HERO_CUSTOM_ITEMS;
	public static boolean ALLOW_CREATE_LVL;
	public static int CHAR_CREATE_LVL;
	public static boolean SPAWN_CHAR;
	public static int SPAWN_X;
	public static int SPAWN_Y;
	public static int SPAWN_Z;
	public static boolean ALLOW_HERO_SUBSKILL;
	public static int HERO_COUNT;
	public static int CRUMA_TOWER_LEVEL_RESTRICT;
	public static boolean ALLOW_RAID_BOSS_PETRIFIED;
	public static int ALT_PLAYER_PROTECTION_LEVEL;
	public static boolean ALLOW_LOW_LEVEL_TRADE;
	public static boolean USE_CHAT_FILTER;
	public static int MONSTER_RETURN_DELAY;
	public static boolean SCROLL_STACKABLE;
	public static boolean ALLOW_CHAR_KILL_PROTECT;
	public static int CLAN_LEADER_COLOR;
	public static int CLAN_LEADER_COLOR_CLAN_LEVEL;
	public static boolean CLAN_LEADER_COLOR_ENABLED;
	public static int CLAN_LEADER_COLORED;
	public static boolean SAVE_RAIDBOSS_STATUS_INTO_DB;
	public static boolean DISABLE_WEIGHT_PENALTY;
	public static int DIFFERENT_Z_CHANGE_OBJECT;
	public static int DIFFERENT_Z_NEW_MOVIE;
	public static int HERO_CUSTOM_ITEM_ID;
	public static int NOOBLE_CUSTOM_ITEM_ID;
	public static int HERO_CUSTOM_DAY;
	public static boolean ALLOW_FARM1_COMMAND;
	public static boolean ALLOW_FARM2_COMMAND;
	public static boolean ALLOW_PVP1_COMMAND;
	public static boolean ALLOW_PVP2_COMMAND;
	public static int FARM1_X;
	public static int FARM1_Y;
	public static int FARM1_Z;
	public static int PVP1_X;
	public static int PVP1_Y;
	public static int PVP1_Z;
	public static int FARM2_X;
	public static int FARM2_Y;
	public static int FARM2_Z;
	public static int PVP2_X;
	public static int PVP2_Y;
	public static int PVP2_Z;
	public static String FARM1_CUSTOM_MESSAGE;
	public static String FARM2_CUSTOM_MESSAGE;
	public static String PVP1_CUSTOM_MESSAGE;
	public static String PVP2_CUSTOM_MESSAGE;
	public static boolean GM_TRADE_RESTRICTED_ITEMS;
	public static boolean GM_RESTART_FIGHTING;
	public static boolean PM_MESSAGE_ON_START;
	public static boolean SERVER_TIME_ON_START;
	public static String PM_SERVER_NAME;
	public static String PM_TEXT1;
	public static String PM_TEXT2;
	public static boolean NEW_PLAYER_EFFECT;
	
	public static int KARMA_MIN_KARMA;
	public static int KARMA_MAX_KARMA;
	public static int KARMA_XP_DIVIDER;
	public static int KARMA_LOST_BASE;
	public static boolean KARMA_DROP_GM;
	public static boolean KARMA_AWARD_PK_KILL;
	public static int KARMA_PK_LIMIT;
	public static String KARMA_NONDROPPABLE_PET_ITEMS;
	public static String KARMA_NONDROPPABLE_ITEMS;
	public static List<Integer> KARMA_LIST_NONDROPPABLE_PET_ITEMS = new ArrayList<>();
	public static List<Integer> KARMA_LIST_NONDROPPABLE_ITEMS = new ArrayList<>();
	public static int PVP_NORMAL_TIME;
	public static int PVP_PVP_TIME;
	public static boolean PVP_COLOR_SYSTEM_ENABLED;
	public static int PVP_AMOUNT1;
	public static int PVP_AMOUNT2;
	public static int PVP_AMOUNT3;
	public static int PVP_AMOUNT4;
	public static int PVP_AMOUNT5;
	public static int NAME_COLOR_FOR_PVP_AMOUNT1;
	public static int NAME_COLOR_FOR_PVP_AMOUNT2;
	public static int NAME_COLOR_FOR_PVP_AMOUNT3;
	public static int NAME_COLOR_FOR_PVP_AMOUNT4;
	public static int NAME_COLOR_FOR_PVP_AMOUNT5;
	public static boolean PK_COLOR_SYSTEM_ENABLED;
	public static int PK_AMOUNT1;
	public static int PK_AMOUNT2;
	public static int PK_AMOUNT3;
	public static int PK_AMOUNT4;
	public static int PK_AMOUNT5;
	public static int TITLE_COLOR_FOR_PK_AMOUNT1;
	public static int TITLE_COLOR_FOR_PK_AMOUNT2;
	public static int TITLE_COLOR_FOR_PK_AMOUNT3;
	public static int TITLE_COLOR_FOR_PK_AMOUNT4;
	public static int TITLE_COLOR_FOR_PK_AMOUNT5;
	public static boolean PVP_REWARD_ENABLED;
	public static int PVP_REWARD_ID;
	public static int PVP_REWARD_AMOUNT;
	public static boolean PK_REWARD_ENABLED;
	public static int PK_REWARD_ID;
	public static int PK_REWARD_AMOUNT;
	public static int REWARD_PROTECT;
	public static boolean ENABLE_PK_INFO;
	public static boolean FLAGED_PLAYER_USE_BUFFER;
	public static boolean FLAGED_PLAYER_CAN_USE_GK;
	public static boolean PVPEXPSP_SYSTEM;
	public static int ADD_EXP;
	public static int ADD_SP;
	public static boolean ALLOW_POTS_IN_PVP;
	public static boolean ALLOW_SOE_IN_PVP;
	public static boolean ANNOUNCE_PVP_KILL;
	public static boolean ANNOUNCE_PK_KILL;
	public static boolean ANNOUNCE_ALL_KILL;
	public static int DUEL_SPAWN_X;
	public static int DUEL_SPAWN_Y;
	public static int DUEL_SPAWN_Z;
	public static boolean PVP_PK_TITLE;
	public static String PVP_TITLE_PREFIX;
	public static String PK_TITLE_PREFIX;
	public static boolean WAR_LEGEND_AURA;
	public static int KILLS_TO_GET_WAR_LEGEND_AURA;
	public static boolean ANTI_FARM_ENABLED;
	public static boolean ANTI_FARM_CLAN_ALLY_ENABLED;
	public static boolean ANTI_FARM_LVL_DIFF_ENABLED;
	public static int ANTI_FARM_MAX_LVL_DIFF;
	public static boolean ANTI_FARM_PDEF_DIFF_ENABLED;
	public static int ANTI_FARM_MAX_PDEF_DIFF;
	public static boolean ANTI_FARM_PATK_DIFF_ENABLED;
	public static int ANTI_FARM_MAX_PATK_DIFF;
	public static boolean ANTI_FARM_PARTY_ENABLED;
	public static boolean ANTI_FARM_IP_ENABLED;
	public static boolean ANTI_FARM_SUMMON;
	
	public static int ALT_OLY_NUMBER_HEROS_EACH_CLASS;
	public static boolean ALT_OLY_LOG_FIGHTS;
	public static boolean ALT_OLY_SHOW_MONTHLY_WINNERS;
	public static boolean ALT_OLY_ANNOUNCE_GAMES;
	public static List<Integer> LIST_OLY_RESTRICTED_SKILLS = new ArrayList<>();
	public static boolean ALT_OLY_AUGMENT_ALLOW;
	public static int ALT_OLY_TELEPORT_COUNTDOWN;
	public static int ALT_OLY_START_TIME;
	public static int ALT_OLY_MIN;
	public static long ALT_OLY_CPERIOD;
	public static long ALT_OLY_BATTLE;
	public static long ALT_OLY_WPERIOD;
	public static long ALT_OLY_VPERIOD;
	public static int ALT_OLY_CLASSED;
	public static int ALT_OLY_NONCLASSED;
	public static int ALT_OLY_BATTLE_REWARD_ITEM;
	public static int ALT_OLY_CLASSED_RITEM_C;
	public static int ALT_OLY_NONCLASSED_RITEM_C;
	public static int ALT_OLY_GP_PER_POINT;
	public static int ALT_OLY_MIN_POINT_FOR_EXCH;
	public static int ALT_OLY_HERO_POINTS;
	public static String ALT_OLY_RESTRICTED_ITEMS;
	public static List<Integer> LIST_OLY_RESTRICTED_ITEMS = new ArrayList<>();
	public static boolean ALLOW_EVENTS_DURING_OLY;
	public static boolean ALT_OLY_RECHARGE_SKILLS;
	public static int ALT_OLY_COMP_RITEM;
	public static boolean REMOVE_CUBIC_OLYMPIAD;
	public static boolean ALT_OLY_USE_CUSTOM_PERIOD_SETTINGS;
	public static OlympiadPeriod ALT_OLY_PERIOD;
	public static int ALT_OLY_PERIOD_MULTIPLIER;
	public static List<Integer> ALT_OLY_COMPETITION_DAYS;
	
	public static Map<Integer, Integer> NORMAL_WEAPON_ENCHANT_LEVEL = new HashMap<>();
	public static Map<Integer, Integer> BLESS_WEAPON_ENCHANT_LEVEL = new HashMap<>();
	public static Map<Integer, Integer> CRYSTAL_WEAPON_ENCHANT_LEVEL = new HashMap<>();
	public static Map<Integer, Integer> NORMAL_ARMOR_ENCHANT_LEVEL = new HashMap<>();
	public static Map<Integer, Integer> BLESS_ARMOR_ENCHANT_LEVEL = new HashMap<>();
	public static Map<Integer, Integer> CRYSTAL_ARMOR_ENCHANT_LEVEL = new HashMap<>();
	public static Map<Integer, Integer> NORMAL_JEWELRY_ENCHANT_LEVEL = new HashMap<>();
	public static Map<Integer, Integer> BLESS_JEWELRY_ENCHANT_LEVEL = new HashMap<>();
	public static Map<Integer, Integer> CRYSTAL_JEWELRY_ENCHANT_LEVEL = new HashMap<>();
	public static int ENCHANT_SAFE_MAX;
	public static int ENCHANT_SAFE_MAX_FULL;
	public static int ENCHANT_WEAPON_MAX;
	public static int ENCHANT_ARMOR_MAX;
	public static int ENCHANT_JEWELRY_MAX;
	public static int CRYSTAL_ENCHANT_MAX;
	public static int CRYSTAL_ENCHANT_MIN;
	public static boolean ENABLE_DWARF_ENCHANT_BONUS;
	public static int DWARF_ENCHANT_MIN_LEVEL;
	public static int DWARF_ENCHANT_BONUS;
	public static int AUGMENTATION_NG_SKILL_CHANCE;
	public static int AUGMENTATION_MID_SKILL_CHANCE;
	public static int AUGMENTATION_HIGH_SKILL_CHANCE;
	public static int AUGMENTATION_TOP_SKILL_CHANCE;
	public static int AUGMENTATION_BASESTAT_CHANCE;
	public static int AUGMENTATION_NG_GLOW_CHANCE;
	public static int AUGMENTATION_MID_GLOW_CHANCE;
	public static int AUGMENTATION_HIGH_GLOW_CHANCE;
	public static int AUGMENTATION_TOP_GLOW_CHANCE;
	public static boolean DELETE_AUGM_PASSIVE_ON_CHANGE;
	public static boolean DELETE_AUGM_ACTIVE_ON_CHANGE;
	public static boolean ENCHANT_HERO_WEAPON;
	public static int SOUL_CRYSTAL_BREAK_CHANCE;
	public static int SOUL_CRYSTAL_LEVEL_CHANCE;
	public static int SOUL_CRYSTAL_MAX_LEVEL;
	public static int CUSTOM_ENCHANT_VALUE;
	public static int ALT_OLY_ENCHANT_LIMIT;
	public static int BREAK_ENCHANT;
	public static int GM_OVER_ENCHANT;
	public static int MAX_ITEM_ENCHANT_KICK;
	
	public static FloodProtectorConfig FLOOD_PROTECTOR_USE_ITEM;
	public static FloodProtectorConfig FLOOD_PROTECTOR_ROLL_DICE;
	public static FloodProtectorConfig FLOOD_PROTECTOR_FIREWORK;
	public static FloodProtectorConfig FLOOD_PROTECTOR_ITEM_PET_SUMMON;
	public static FloodProtectorConfig FLOOD_PROTECTOR_HERO_VOICE;
	public static FloodProtectorConfig FLOOD_PROTECTOR_GLOBAL_CHAT;
	public static FloodProtectorConfig FLOOD_PROTECTOR_SUBCLASS;
	public static FloodProtectorConfig FLOOD_PROTECTOR_DROP_ITEM;
	public static FloodProtectorConfig FLOOD_PROTECTOR_SERVER_BYPASS;
	public static FloodProtectorConfig FLOOD_PROTECTOR_MULTISELL;
	public static FloodProtectorConfig FLOOD_PROTECTOR_TRANSACTION;
	public static FloodProtectorConfig FLOOD_PROTECTOR_MANUFACTURE;
	public static FloodProtectorConfig FLOOD_PROTECTOR_MANOR;
	public static FloodProtectorConfig FLOOD_PROTECTOR_CHARACTER_SELECT;
	public static FloodProtectorConfig FLOOD_PROTECTOR_UNKNOWN_PACKETS;
	public static FloodProtectorConfig FLOOD_PROTECTOR_PARTY_INVITATION;
	public static FloodProtectorConfig FLOOD_PROTECTOR_SAY_ACTION;
	public static FloodProtectorConfig FLOOD_PROTECTOR_MOVE_ACTION;
	public static FloodProtectorConfig FLOOD_PROTECTOR_GENERIC_ACTION;
	public static FloodProtectorConfig FLOOD_PROTECTOR_MACRO;
	public static FloodProtectorConfig FLOOD_PROTECTOR_POTION;
	
	public static boolean CHECK_SKILLS_ON_ENTER;
	public static boolean CHECK_NAME_ON_LOGIN;
	public static boolean L2WALKER_PROTECTION;
	public static boolean PROTECTED_ENCHANT;
	public static boolean ONLY_GM_ITEMS_FREE;
	public static boolean ONLY_GM_TELEPORT_FREE;
	public static boolean ALLOW_DUALBOX;
	public static int ALLOWED_BOXES;
	public static boolean ALLOW_DUALBOX_OLY;
	public static boolean ALLOW_DUALBOX_EVENT;
	
	public static int BLOW_ATTACK_FRONT;
	public static int BLOW_ATTACK_SIDE;
	public static int BLOW_ATTACK_BEHIND;
	public static int BACKSTAB_ATTACK_FRONT;
	public static int BACKSTAB_ATTACK_SIDE;
	public static int BACKSTAB_ATTACK_BEHIND;
	public static int MAX_PATK_SPEED;
	public static int MAX_MATK_SPEED;
	public static int MAX_PCRIT_RATE;
	public static int MAX_MCRIT_RATE;
	public static float MCRIT_RATE_MUL;
	public static int RUN_SPD_BOOST;
	public static int MAX_RUN_SPEED;
	public static float ALT_MAGES_PHYSICAL_DAMAGE_MULTI;
	public static float ALT_MAGES_MAGICAL_DAMAGE_MULTI;
	public static float ALT_FIGHTERS_PHYSICAL_DAMAGE_MULTI;
	public static float ALT_FIGHTERS_MAGICAL_DAMAGE_MULTI;
	public static float ALT_PETS_PHYSICAL_DAMAGE_MULTI;
	public static float ALT_PETS_MAGICAL_DAMAGE_MULTI;
	public static float ALT_NPC_PHYSICAL_DAMAGE_MULTI;
	public static float ALT_NPC_MAGICAL_DAMAGE_MULTI;
	public static float ALT_DAGGER_DMG_VS_HEAVY;
	public static float ALT_DAGGER_DMG_VS_ROBE;
	public static float ALT_DAGGER_DMG_VS_LIGHT;
	public static boolean ALLOW_RAID_LETHAL;
	
	public static boolean ALLOW_LETHAL_PROTECTION_MOBS;
	public static String LETHAL_PROTECTED_MOBS;
	public static List<Integer> LIST_LETHAL_PROTECTED_MOBS = new ArrayList<>();
	public static float MAGIC_CRITICAL_POWER;
	public static float STUN_CHANCE_MODIFIER;
	public static float BLEED_CHANCE_MODIFIER;
	public static float POISON_CHANCE_MODIFIER;
	public static float PARALYZE_CHANCE_MODIFIER;
	public static float ROOT_CHANCE_MODIFIER;
	public static float SLEEP_CHANCE_MODIFIER;
	public static float FEAR_CHANCE_MODIFIER;
	public static float CONFUSION_CHANCE_MODIFIER;
	public static float DEBUFF_CHANCE_MODIFIER;
	public static float BUFF_CHANCE_MODIFIER;
	public static boolean SEND_SKILLS_CHANCE_TO_PLAYERS;
	public static boolean REMOVE_WEAPON_SUBCLASS;
	public static boolean REMOVE_CHEST_SUBCLASS;
	public static boolean REMOVE_LEG_SUBCLASS;
	public static boolean ENABLE_CLASS_DAMAGES;
	public static boolean ENABLE_CLASS_DAMAGES_IN_OLY;
	public static boolean ENABLE_CLASS_DAMAGES_LOGGER;
	public static boolean LEAVE_BUFFS_ON_DIE;
	public static boolean ALT_RAIDS_STATS_BONUS;
	
	public static String GEODATA_PATH;
	public static int COORD_SYNCHRONIZE;
	public static int PART_OF_CHARACTER_HEIGHT;
	public static int MAX_OBSTACLE_HEIGHT;
	public static boolean PATHFINDING;
	public static String PATHFIND_BUFFERS;
	public static int BASE_WEIGHT;
	public static int DIAGONAL_WEIGHT;
	public static int HEURISTIC_WEIGHT;
	public static int OBSTACLE_MULTIPLIER;
	public static int MAX_ITERATIONS;
	public static boolean FALL_DAMAGE;
	public static boolean ALLOW_WATER;
	
	public static int RBLOCKRAGE;
	public static boolean PLAYERS_CAN_HEAL_RB;
	public static HashMap<Integer, Integer> RBS_SPECIFIC_LOCK_RAGE;
	public static boolean ALLOW_DIRECT_TP_TO_BOSS_ROOM;
	public static boolean ANTHARAS_OLD;
	public static int ANTHARAS_CLOSE;
	public static int ANTHARAS_DESPAWN_TIME;
	public static int ANTHARAS_RESP_FIRST;
	public static int ANTHARAS_RESP_SECOND;
	public static int ANTHARAS_WAIT_TIME;
	public static float ANTHARAS_POWER_MULTIPLIER;
	public static int BAIUM_SLEEP;
	public static int BAIUM_RESP_FIRST;
	public static int BAIUM_RESP_SECOND;
	public static float BAIUM_POWER_MULTIPLIER;
	public static int CORE_RESP_MINION;
	public static int CORE_RESP_FIRST;
	public static int CORE_RESP_SECOND;
	public static int CORE_LEVEL;
	public static int CORE_RING_CHANCE;
	public static float CORE_POWER_MULTIPLIER;
	public static int QA_RESP_NURSE;
	public static int QA_RESP_ROYAL;
	public static int QA_RESP_FIRST;
	public static int QA_RESP_SECOND;
	public static int QA_LEVEL;
	public static int QA_RING_CHANCE;
	public static float QA_POWER_MULTIPLIER;
	public static float LEVEL_DIFF_MULTIPLIER_MINION;
	public static int HPH_FIXINTERVALOFHALTER;
	public static int HPH_RANDOMINTERVALOFHALTER;
	public static int HPH_APPTIMEOFHALTER;
	public static int HPH_ACTIVITYTIMEOFHALTER;
	public static int HPH_FIGHTTIMEOFHALTER;
	public static int HPH_CALLROYALGUARDHELPERCOUNT;
	public static int HPH_CALLROYALGUARDHELPERINTERVAL;
	public static int HPH_INTERVALOFDOOROFALTER;
	public static int HPH_TIMEOFLOCKUPDOOROFALTAR;
	public static int ZAKEN_RESP_FIRST;
	public static int ZAKEN_RESP_SECOND;
	public static int ZAKEN_LEVEL;
	public static int ZAKEN_EARRING_CHANCE;
	public static float ZAKEN_POWER_MULTIPLIER;
	public static int ORFEN_RESP_FIRST;
	public static int ORFEN_RESP_SECOND;
	public static int ORFEN_LEVEL;
	public static int ORFEN_EARRING_CHANCE;
	public static float ORFEN_POWER_MULTIPLIER;
	public static int VALAKAS_RESP_FIRST;
	public static int VALAKAS_RESP_SECOND;
	public static int VALAKAS_WAIT_TIME;
	public static int VALAKAS_DESPAWN_TIME;
	public static float VALAKAS_POWER_MULTIPLIER;
	public static int FRINTEZZA_RESP_FIRST;
	public static int FRINTEZZA_RESP_SECOND;
	public static float FRINTEZZA_POWER_MULTIPLIER;
	public static boolean BYPASS_FRINTEZZA_PARTIES_CHECK;
	public static int FRINTEZZA_MIN_PARTIES;
	public static int FRINTEZZA_MAX_PARTIES;
	public static String RAID_INFO_IDS;
	public static List<Integer> RAID_INFO_IDS_LIST = new ArrayList<>();
	
	public static boolean AUTO_LOOT;
	public static boolean AUTO_LOOT_HERBS;
	public static boolean AUTO_LOOT_BOSS;
	public static boolean AUTO_LEARN_SKILLS;
	public static boolean AUTO_LEARN_DIVINE_INSPIRATION;
	public static boolean LIFE_CRYSTAL_NEEDED;
	public static boolean SP_BOOK_NEEDED;
	public static boolean ES_SP_BOOK_NEEDED;
	public static boolean DIVINE_SP_BOOK_NEEDED;
	public static boolean ALT_GAME_SKILL_LEARN;
	public static int ALLOWED_SUBCLASS;
	public static byte BASE_SUBCLASS_LEVEL;
	public static byte MAX_SUBCLASS_LEVEL;
	public static boolean ALT_GAME_SUBCLASS_WITHOUT_QUESTS;
	public static boolean ALT_RESTORE_EFFECTS_ON_SUBCLASS_CHANGE;
	public static int ALT_PARTY_RANGE;
	public static double ALT_WEIGHT_LIMIT;
	public static boolean ALT_GAME_DELEVEL;
	public static boolean ALT_GAME_MAGICFAILURES;
	public static boolean ALT_GAME_CANCEL_CAST;
	public static boolean ALT_GAME_CANCEL_BOW;
	public static boolean ALT_GAME_SHIELD_BLOCKS;
	public static int ALT_PERFECT_SHLD_BLOCK;
	public static boolean ALT_GAME_MOB_ATTACK_AI;
	public static boolean ALT_MOB_AGRO_IN_PEACEZONE;
	public static boolean ALT_GAME_FREIGHTS;
	public static int ALT_GAME_FREIGHT_PRICE;
	public static float ALT_GAME_EXPONENT_XP;
	public static float ALT_GAME_EXPONENT_SP;
	public static boolean ALT_GAME_TIREDNESS;
	public static boolean ALT_GAME_FREE_TELEPORT;
	public static boolean ALT_RECOMMEND;
	public static int ALT_RECOMMENDATIONS_NUMBER;
	public static int MAX_CHARACTERS_NUMBER_PER_ACCOUNT;
	public static int MAX_LEVEL_NEWBIE;
	public static int MAX_LEVEL_NEWBIE_STATUS;
	public static boolean DISABLE_TUTORIAL;
	public static int STARTING_ADENA;
	public static int STARTING_AA;
	public static boolean CUSTOM_STARTER_ITEMS_ENABLED;
	public static List<int[]> STARTING_CUSTOM_ITEMS_F = new ArrayList<>();
	public static List<int[]> STARTING_CUSTOM_ITEMS_M = new ArrayList<>();
	public static int INVENTORY_MAXIMUM_NO_DWARF;
	public static int INVENTORY_MAXIMUM_DWARF;
	public static int INVENTORY_MAXIMUM_GM;
	public static int MAX_ITEM_IN_PACKET;
	public static int WAREHOUSE_SLOTS_DWARF;
	public static int WAREHOUSE_SLOTS_NO_DWARF;
	public static int WAREHOUSE_SLOTS_CLAN;
	public static int FREIGHT_SLOTS;
	public static int MAX_PVTSTORE_SLOTS_DWARF;
	public static int MAX_PVTSTORE_SLOTS_OTHER;
	public static double HP_REGEN_MULTIPLIER;
	public static double MP_REGEN_MULTIPLIER;
	public static double CP_REGEN_MULTIPLIER;
	public static boolean ENABLE_KEYBOARD_MOVEMENT;
	public static int UNSTUCK_INTERVAL;
	public static int PLAYER_SPAWN_PROTECTION;
	public static int PLAYER_TELEPORT_PROTECTION;
	public static int PLAYER_FAKEDEATH_UP_PROTECTION;
	public static boolean DEEPBLUE_DROP_RULES;
	public static String PARTY_XP_CUTOFF_METHOD;
	public static double PARTY_XP_CUTOFF_PERCENT;
	public static int PARTY_XP_CUTOFF_LEVEL;
	public static double RESPAWN_RESTORE_CP;
	public static double RESPAWN_RESTORE_HP;
	public static double RESPAWN_RESTORE_MP;
	public static boolean RESPAWN_RANDOM_ENABLED;
	public static int RESPAWN_RANDOM_MAX_OFFSET;
	public static boolean PETITIONING_ALLOWED;
	public static int MAX_PETITIONS_PER_PLAYER;
	public static int MAX_PETITIONS_PENDING;
	public static int DEATH_PENALTY_CHANCE;
	public static boolean EFFECT_CANCELING;
	public static boolean STORE_SKILL_COOLTIME;
	public static byte BUFFS_MAX_AMOUNT;
	public static byte DEBUFFS_MAX_AMOUNT;
	public static boolean ENABLE_MODIFY_SKILL_DURATION;
	public static Map<Integer, Integer> SKILL_DURATION_LIST;
	public static boolean ALLOW_CLASS_MASTERS;
	public static boolean CLASS_MASTER_STRIDER_UPDATE;
	public static boolean ALLOW_CLASS_MASTERS_FIRST_CLASS;
	public static boolean ALLOW_CLASS_MASTERS_SECOND_CLASS;
	public static boolean ALLOW_CLASS_MASTERS_THIRD_CLASS;
	public static ClassMasterSettings CLASS_MASTER_SETTINGS;
	public static boolean ALLOW_REMOTE_CLASS_MASTERS;
	
	public static long AUTOSAVE_INITIAL_TIME;
	public static long AUTOSAVE_DELAY_TIME;
	public static long CHECK_CONNECTION_INACTIVITY_TIME;
	public static long CHECK_CONNECTION_INITIAL_TIME;
	public static long CHECK_CONNECTION_DELAY_TIME;
	public static long CLEANDB_INITIAL_TIME;
	public static long CLEANDB_DELAY_TIME;
	public static long CHECK_TELEPORT_ZOMBIE_DELAY_TIME;
	public static long DEADLOCKCHECK_INTIAL_TIME;
	public static long DEADLOCKCHECK_DELAY_TIME;
	
	public static ArrayList<String> QUESTION_LIST = new ArrayList<>();
	
	public static int SERVER_ID;
	public static byte[] HEX_ID;
	
	public static int PORT_LOGIN;
	public static String LOGIN_BIND_ADDRESS;
	public static int LOGIN_TRY_BEFORE_BAN;
	public static int LOGIN_BLOCK_AFTER_BAN;
	public static int GAME_SERVER_LOGIN_PORT;
	public static String GAME_SERVER_LOGIN_HOST;
	public static String INTERNAL_HOSTNAME;
	public static String EXTERNAL_HOSTNAME;
	public static int REQUEST_ID;
	public static boolean ACCEPT_ALTERNATE_ID;
	public static File DATAPACK_ROOT;
	public static File SCRIPT_ROOT;
	public static int MAXIMUM_ONLINE_USERS;
	public static boolean SERVER_LIST_BRACKET;
	public static boolean SERVER_LIST_CLOCK;
	public static int MIN_PROTOCOL_REVISION;
	public static int MAX_PROTOCOL_REVISION;
	public static int SCHEDULED_THREAD_POOL_COUNT;
	public static int INSTANT_THREAD_POOL_COUNT;
	public static String CNAME_TEMPLATE;
	public static String PET_NAME_TEMPLATE;
	public static String CLAN_NAME_TEMPLATE;
	public static String ALLY_NAME_TEMPLATE;
	
	public static int IP_UPDATE_TIME;
	public static boolean SHOW_LICENCE;
	public static boolean FORCE_GGAUTH;
	public static boolean FLOOD_PROTECTION;
	public static int FAST_CONNECTION_LIMIT;
	public static int NORMAL_CONNECTION_TIME;
	public static int FAST_CONNECTION_TIME;
	public static int MAX_CONNECTION_PER_IP;
	public static boolean ACCEPT_NEW_GAMESERVER;
	public static boolean AUTO_CREATE_ACCOUNTS;
	public static String NETWORK_IP_LIST;
	public static long SESSION_TTL;
	public static int MAX_LOGINSESSIONS;
	
	public static void loadAccessConfig()
	{
		try
		{
			final Properties accessSettings = new Properties();
			final InputStream is = new FileInputStream(new File(ACCESS_CONFIG_FILE));
			accessSettings.load(is);
			is.close();
			
			EVERYBODY_HAS_ADMIN_RIGHTS = Boolean.parseBoolean(accessSettings.getProperty("EverybodyHasAdminRights", "false"));
			MASTERACCESS_LEVEL = Integer.parseInt(accessSettings.getProperty("MasterAccessLevel", "1"));
			MASTERACCESS_NAME_COLOR_ENABLED = Boolean.parseBoolean(accessSettings.getProperty("MasterNameColorEnabled", "false"));
			MASTERACCESS_TITLE_COLOR_ENABLED = Boolean.parseBoolean(accessSettings.getProperty("MasterTitleColorEnabled", "false"));
			MASTERACCESS_NAME_COLOR = Integer.decode("0x" + accessSettings.getProperty("MasterNameColor", "00FF00"));
			MASTERACCESS_TITLE_COLOR = Integer.decode("0x" + accessSettings.getProperty("MasterTitleColor", "00FF00"));
			USERACCESS_LEVEL = Integer.parseInt(accessSettings.getProperty("UserAccessLevel", "0"));
			GM_STARTUP_AUTO_LIST = Boolean.parseBoolean(accessSettings.getProperty("GMStartupAutoList", "true"));
			GM_ADMIN_MENU_STYLE = accessSettings.getProperty("GMAdminMenuStyle", "modern");
			GM_HERO_AURA = Boolean.parseBoolean(accessSettings.getProperty("GMHeroAura", "false"));
			GM_STARTUP_BUILDER_HIDE = Boolean.parseBoolean(accessSettings.getProperty("GMStartupBuilderHide", "true"));
			GM_STARTUP_INVULNERABLE = Boolean.parseBoolean(accessSettings.getProperty("GMStartupInvulnerable", "true"));
			GM_ANNOUNCER_NAME = Boolean.parseBoolean(accessSettings.getProperty("AnnounceGmName", "false"));
			GM_CRITANNOUNCER_NAME = Boolean.parseBoolean(accessSettings.getProperty("CritAnnounceName", "false"));
			SHOW_GM_LOGIN = Boolean.parseBoolean(accessSettings.getProperty("ShowGMLogin", "false"));
			GM_STARTUP_INVISIBLE = Boolean.parseBoolean(accessSettings.getProperty("GMStartupInvisible", "true"));
			GM_SPECIAL_EFFECT = Boolean.parseBoolean(accessSettings.getProperty("GmLoginSpecialEffect", "false"));
			GM_STARTUP_SILENCE = Boolean.parseBoolean(accessSettings.getProperty("GMStartupSilence", "true"));
			GM_DEBUG_HTML_PATHS = Boolean.parseBoolean(accessSettings.getProperty("GMDebugHtmlPaths", "true"));
			USE_SUPER_HASTE_AS_GM_SPEED = Boolean.parseBoolean(accessSettings.getProperty("UseSuperHasteAsGMSpeed", "false"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + ACCESS_CONFIG_FILE + " File.");
		}
	}
	
	public static void loadServerConfig()
	{
		try
		{
			final Properties serverSettings = new Properties();
			final InputStream is = new FileInputStream(new File(SERVER_CONFIG_FILE));
			serverSettings.load(is);
			is.close();
			GAMESERVER_HOSTNAME = serverSettings.getProperty("GameserverHostname");
			PORT_GAME = Integer.parseInt(serverSettings.getProperty("GameserverPort", "7777"));
			
			EXTERNAL_HOSTNAME = serverSettings.getProperty("ExternalHostname", "*");
			INTERNAL_HOSTNAME = serverSettings.getProperty("InternalHostname", "*");
			
			GAME_SERVER_LOGIN_PORT = Integer.parseInt(serverSettings.getProperty("LoginPort", "9014"));
			GAME_SERVER_LOGIN_HOST = serverSettings.getProperty("LoginHost", "127.0.0.1");
			
			DATABASE_DRIVER = serverSettings.getProperty("Driver", "org.mariadb.jdbc.Driver");
			DATABASE_URL = serverSettings.getProperty("URL", "jdbc:mariadb://localhost/");
			
			DATABASE_LOGIN = serverSettings.getProperty("Login", "root");
			DATABASE_PASSWORD = serverSettings.getProperty("Password", "");
			DATABASE_MAX_CONNECTIONS = Integer.parseInt(serverSettings.getProperty("MaximumDbConnections", "10"));
			
			BACKUP_DATABASE = Boolean.parseBoolean(serverSettings.getProperty("BackupDatabase", "false"));
			MYSQL_BIN_PATH = serverSettings.getProperty("MySqlBinLocation", "C:/xampp/mysql/bin/");
			BACKUP_PATH = serverSettings.getProperty("BackupPath", "../backup/");
			BACKUP_DAYS = Integer.parseInt(serverSettings.getProperty("BackupDays", "30"));
			
			REQUEST_ID = Integer.parseInt(serverSettings.getProperty("RequestServerID", "0"));
			ACCEPT_ALTERNATE_ID = Boolean.parseBoolean(serverSettings.getProperty("AcceptAlternateID", "true"));
			
			DATAPACK_ROOT = new File(serverSettings.getProperty("DatapackRoot", ".")).getCanonicalFile();
			SCRIPT_ROOT = new File(serverSettings.getProperty("ScriptRoot", "./data/scripts").replaceAll("\\\\", "/")).getCanonicalFile();
			
			MAXIMUM_ONLINE_USERS = Integer.parseInt(serverSettings.getProperty("MaximumOnlineUsers", "100"));
			
			SERVER_LIST_BRACKET = Boolean.parseBoolean(serverSettings.getProperty("ServerListBrackets", "false"));
			SERVER_LIST_CLOCK = Boolean.parseBoolean(serverSettings.getProperty("ServerListClock", "false"));
			
			MIN_PROTOCOL_REVISION = Integer.parseInt(serverSettings.getProperty("MinProtocolRevision", "660"));
			MAX_PROTOCOL_REVISION = Integer.parseInt(serverSettings.getProperty("MaxProtocolRevision", "665"));
			if (MIN_PROTOCOL_REVISION > MAX_PROTOCOL_REVISION)
			{
				throw new Error("MinProtocolRevision is bigger than MaxProtocolRevision in server configuration file.");
			}
			
			SCHEDULED_THREAD_POOL_COUNT = Integer.parseInt(serverSettings.getProperty("ScheduledThreadPoolCount", "40"));
			INSTANT_THREAD_POOL_COUNT = Integer.parseInt(serverSettings.getProperty("InstantThreadPoolCount", "20"));
			
			CNAME_TEMPLATE = serverSettings.getProperty("CnameTemplate", ".*");
			PET_NAME_TEMPLATE = serverSettings.getProperty("PetNameTemplate", ".*");
			CLAN_NAME_TEMPLATE = serverSettings.getProperty("ClanNameTemplate", ".*");
			ALLY_NAME_TEMPLATE = serverSettings.getProperty("AllyNameTemplate", ".*");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + SERVER_CONFIG_FILE + " File.");
		}
	}
	
	public static void loadTelnetConfig()
	{
		FileInputStream is = null;
		try
		{
			final L2Properties telnetSettings = new L2Properties();
			is = new FileInputStream(new File(TELNET_CONFIG_FILE));
			telnetSettings.load(is);
			
			IS_TELNET_ENABLED = Boolean.parseBoolean(telnetSettings.getProperty("EnableTelnet", "false"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + TELNET_CONFIG_FILE + " File.");
		}
		finally
		{
			
			if (is != null)
			{
				try
				{
					is.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public static void loadIdFactoryConfig()
	{
		try
		{
			final Properties idSettings = new Properties();
			final InputStream is = new FileInputStream(new File(ID_CONFIG_FILE));
			idSettings.load(is);
			is.close();
			
			IDFACTORY_TYPE = IdFactoryType.valueOf(idSettings.getProperty("IDFactory", "BITSET"));
			BAD_ID_CHECKING = Boolean.parseBoolean(idSettings.getProperty("BadIdChecking", "true"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + ID_CONFIG_FILE + " File.");
		}
	}
	
	public static void loadRatesConfig()
	{
		try
		{
			final Properties ratesSettings = new Properties();
			final InputStream is = new FileInputStream(new File(RATES_CONFIG_FILE));
			ratesSettings.load(is);
			is.close();
			
			RATE_XP = Float.parseFloat(ratesSettings.getProperty("RateXp", "1.00"));
			RATE_SP = Float.parseFloat(ratesSettings.getProperty("RateSp", "1.00"));
			RATE_PARTY_XP = Float.parseFloat(ratesSettings.getProperty("RatePartyXp", "1.00"));
			RATE_PARTY_SP = Float.parseFloat(ratesSettings.getProperty("RatePartySp", "1.00"));
			RATE_QUESTS_REWARD = Float.parseFloat(ratesSettings.getProperty("RateQuestsReward", "1.00"));
			RATE_DROP_ADENA = Float.parseFloat(ratesSettings.getProperty("RateDropAdena", "1.00"));
			RATE_CONSUMABLE_COST = Float.parseFloat(ratesSettings.getProperty("RateConsumableCost", "1.00"));
			RATE_DROP_ITEMS = Float.parseFloat(ratesSettings.getProperty("RateDropItems", "1.00"));
			RATE_DROP_SEAL_STONES = Float.parseFloat(ratesSettings.getProperty("RateDropSealStones", "1.00"));
			RATE_DROP_SPOIL = Float.parseFloat(ratesSettings.getProperty("RateDropSpoil", "1.00"));
			RATE_DROP_MANOR = Integer.parseInt(ratesSettings.getProperty("RateDropManor", "1.00"));
			RATE_DROP_QUEST = Float.parseFloat(ratesSettings.getProperty("RateDropQuest", "1.00"));
			RATE_KARMA_EXP_LOST = Float.parseFloat(ratesSettings.getProperty("RateKarmaExpLost", "1.00"));
			RATE_SIEGE_GUARDS_PRICE = Float.parseFloat(ratesSettings.getProperty("RateSiegeGuardsPrice", "1.00"));
			RATE_DROP_COMMON_HERBS = Float.parseFloat(ratesSettings.getProperty("RateCommonHerbs", "15.00"));
			RATE_DROP_MP_HP_HERBS = Float.parseFloat(ratesSettings.getProperty("RateHpMpHerbs", "10.00"));
			RATE_DROP_GREATER_HERBS = Float.parseFloat(ratesSettings.getProperty("RateGreaterHerbs", "4.00"));
			RATE_DROP_SUPERIOR_HERBS = Float.parseFloat(ratesSettings.getProperty("RateSuperiorHerbs", "0.80")) * 10;
			RATE_DROP_SPECIAL_HERBS = Float.parseFloat(ratesSettings.getProperty("RateSpecialHerbs", "0.20")) * 10;
			
			PLAYER_DROP_LIMIT = Integer.parseInt(ratesSettings.getProperty("PlayerDropLimit", "3"));
			PLAYER_RATE_DROP = Integer.parseInt(ratesSettings.getProperty("PlayerRateDrop", "5"));
			PLAYER_RATE_DROP_ITEM = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropItem", "70"));
			PLAYER_RATE_DROP_EQUIP = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropEquip", "25"));
			PLAYER_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropEquipWeapon", "5"));
			
			PET_XP_RATE = Float.parseFloat(ratesSettings.getProperty("PetXpRate", "1.00"));
			PET_FOOD_RATE = Integer.parseInt(ratesSettings.getProperty("PetFoodRate", "1"));
			SINEATER_XP_RATE = Float.parseFloat(ratesSettings.getProperty("SinEaterXpRate", "1.00"));
			
			KARMA_DROP_LIMIT = Integer.parseInt(ratesSettings.getProperty("KarmaDropLimit", "10"));
			KARMA_RATE_DROP = Integer.parseInt(ratesSettings.getProperty("KarmaRateDrop", "70"));
			KARMA_RATE_DROP_ITEM = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropItem", "50"));
			KARMA_RATE_DROP_EQUIP = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropEquip", "40"));
			KARMA_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropEquipWeapon", "10"));
			
			/** RB rate **/
			ADENA_BOSS = Float.parseFloat(ratesSettings.getProperty("AdenaBoss", "1.00"));
			ADENA_RAID = Float.parseFloat(ratesSettings.getProperty("AdenaRaid", "1.00"));
			ADENA_MINION = Float.parseFloat(ratesSettings.getProperty("AdenaMinion", "1.00"));
			ITEMS_BOSS = Float.parseFloat(ratesSettings.getProperty("ItemsBoss", "1.00"));
			ITEMS_RAID = Float.parseFloat(ratesSettings.getProperty("ItemsRaid", "1.00"));
			ITEMS_MINION = Float.parseFloat(ratesSettings.getProperty("ItemsMinion", "1.00"));
			SPOIL_BOSS = Float.parseFloat(ratesSettings.getProperty("SpoilBoss", "1.00"));
			SPOIL_RAID = Float.parseFloat(ratesSettings.getProperty("SpoilRaid", "1.00"));
			SPOIL_MINION = Float.parseFloat(ratesSettings.getProperty("SpoilMinion", "1.00"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + RATES_CONFIG_FILE + " File.");
		}
	}
	
	public static void loadGeneralConfig()
	{
		try
		{
			final Properties generalSettings = new Properties();
			final InputStream is = new FileInputStream(new File(GENERAL_CONFIG_FILE));
			generalSettings.load(is);
			is.close();
			
			SERVER_LIST_TESTSERVER = Boolean.parseBoolean(generalSettings.getProperty("TestServer", "false"));
			SERVER_GMONLY = Boolean.parseBoolean(generalSettings.getProperty("ServerGMOnly", "false"));
			ALT_DEV_NO_QUESTS = Boolean.parseBoolean(generalSettings.getProperty("AltDevNoQuests", "false"));
			ALT_DEV_NO_SPAWNS = Boolean.parseBoolean(generalSettings.getProperty("AltDevNoSpawns", "false"));
			ALT_DEV_NO_SCRIPT = Boolean.parseBoolean(generalSettings.getProperty("AltDevNoScript", "false"));
			ALT_DEV_NO_RB = Boolean.parseBoolean(generalSettings.getProperty("AltDevNoRB", "false"));
			
			GMAUDIT = Boolean.parseBoolean(generalSettings.getProperty("GMAudit", "false"));
			LOG_CHAT = Boolean.parseBoolean(generalSettings.getProperty("LogChat", "false"));
			LOG_ITEMS = Boolean.parseBoolean(generalSettings.getProperty("LogItems", "false"));
			
			LAZY_CACHE = Boolean.parseBoolean(generalSettings.getProperty("LazyCache", "false"));
			
			REMOVE_CASTLE_CIRCLETS = Boolean.parseBoolean(generalSettings.getProperty("RemoveCastleCirclets", "true"));
			ALT_GAME_VIEWNPC = Boolean.parseBoolean(generalSettings.getProperty("AltGameViewNpc", "false"));
			ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE = Boolean.parseBoolean(generalSettings.getProperty("AltNewCharAlwaysIsNewbie", "false"));
			ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH = Boolean.parseBoolean(generalSettings.getProperty("AltMembersCanWithdrawFromClanWH", "false"));
			ALT_MAX_NUM_OF_CLANS_IN_ALLY = Integer.parseInt(generalSettings.getProperty("AltMaxNumOfClansInAlly", "3"));
			
			ALT_CLAN_MEMBERS_FOR_WAR = Integer.parseInt(generalSettings.getProperty("AltClanMembersForWar", "15"));
			ALT_CLAN_JOIN_DAYS = Integer.parseInt(generalSettings.getProperty("DaysBeforeJoinAClan", "5"));
			ALT_CLAN_CREATE_DAYS = Integer.parseInt(generalSettings.getProperty("DaysBeforeCreateAClan", "10"));
			ALT_CLAN_DISSOLVE_DAYS = Integer.parseInt(generalSettings.getProperty("DaysToPassToDissolveAClan", "7"));
			ALT_ALLY_JOIN_DAYS_WHEN_LEAVED = Integer.parseInt(generalSettings.getProperty("DaysBeforeJoinAllyWhenLeaved", "1"));
			ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED = Integer.parseInt(generalSettings.getProperty("DaysBeforeJoinAllyWhenDismissed", "1"));
			ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED = Integer.parseInt(generalSettings.getProperty("DaysBeforeAcceptNewClanWhenDismissed", "1"));
			ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED = Integer.parseInt(generalSettings.getProperty("DaysBeforeCreateNewAllyWhenDissolved", "10"));
			
			ALT_MANOR_REFRESH_TIME = Integer.parseInt(generalSettings.getProperty("AltManorRefreshTime", "20"));
			ALT_MANOR_REFRESH_MIN = Integer.parseInt(generalSettings.getProperty("AltManorRefreshMin", "00"));
			ALT_MANOR_APPROVE_TIME = Integer.parseInt(generalSettings.getProperty("AltManorApproveTime", "6"));
			ALT_MANOR_APPROVE_MIN = Integer.parseInt(generalSettings.getProperty("AltManorApproveMin", "00"));
			ALT_MANOR_MAINTENANCE_PERIOD = Integer.parseInt(generalSettings.getProperty("AltManorMaintenancePeriod", "360000"));
			ALT_MANOR_SAVE_ALL_ACTIONS = Boolean.parseBoolean(generalSettings.getProperty("AltManorSaveAllActions", "false"));
			ALT_MANOR_SAVE_PERIOD_RATE = Integer.parseInt(generalSettings.getProperty("AltManorSavePeriodRate", "2"));
			
			ALT_LOTTERY_PRIZE = Integer.parseInt(generalSettings.getProperty("AltLotteryPrize", "50000"));
			ALT_LOTTERY_TICKET_PRICE = Integer.parseInt(generalSettings.getProperty("AltLotteryTicketPrice", "2000"));
			ALT_LOTTERY_5_NUMBER_RATE = Float.parseFloat(generalSettings.getProperty("AltLottery5NumberRate", "0.6"));
			ALT_LOTTERY_4_NUMBER_RATE = Float.parseFloat(generalSettings.getProperty("AltLottery4NumberRate", "0.2"));
			ALT_LOTTERY_3_NUMBER_RATE = Float.parseFloat(generalSettings.getProperty("AltLottery3NumberRate", "0.2"));
			ALT_LOTTERY_2_AND_1_NUMBER_PRIZE = Integer.parseInt(generalSettings.getProperty("AltLottery2and1NumberPrize", "200"));
			
			ALT_FISH_CHAMPIONSHIP_ENABLED = Boolean.parseBoolean(generalSettings.getProperty("AltFishChampionshipEnabled", "true"));
			ALT_FISH_CHAMPIONSHIP_REWARD_ITEM = Integer.parseInt(generalSettings.getProperty("AltFishChampionshipRewardItemId", "57"));
			ALT_FISH_CHAMPIONSHIP_REWARD_1 = Integer.parseInt(generalSettings.getProperty("AltFishChampionshipReward1", "800000"));
			ALT_FISH_CHAMPIONSHIP_REWARD_2 = Integer.parseInt(generalSettings.getProperty("AltFishChampionshipReward2", "500000"));
			ALT_FISH_CHAMPIONSHIP_REWARD_3 = Integer.parseInt(generalSettings.getProperty("AltFishChampionshipReward3", "300000"));
			ALT_FISH_CHAMPIONSHIP_REWARD_4 = Integer.parseInt(generalSettings.getProperty("AltFishChampionshipReward4", "200000"));
			ALT_FISH_CHAMPIONSHIP_REWARD_5 = Integer.parseInt(generalSettings.getProperty("AltFishChampionshipReward5", "100000"));
			
			RIFT_MIN_PARTY_SIZE = Integer.parseInt(generalSettings.getProperty("RiftMinPartySize", "5"));
			RIFT_MAX_JUMPS = Integer.parseInt(generalSettings.getProperty("MaxRiftJumps", "4"));
			RIFT_SPAWN_DELAY = Integer.parseInt(generalSettings.getProperty("RiftSpawnDelay", "10000"));
			RIFT_AUTO_JUMPS_TIME_MIN = Integer.parseInt(generalSettings.getProperty("AutoJumpsDelayMin", "480"));
			RIFT_AUTO_JUMPS_TIME_MAX = Integer.parseInt(generalSettings.getProperty("AutoJumpsDelayMax", "600"));
			RIFT_ENTER_COST_RECRUIT = Integer.parseInt(generalSettings.getProperty("RecruitCost", "18"));
			RIFT_ENTER_COST_SOLDIER = Integer.parseInt(generalSettings.getProperty("SoldierCost", "21"));
			RIFT_ENTER_COST_OFFICER = Integer.parseInt(generalSettings.getProperty("OfficerCost", "24"));
			RIFT_ENTER_COST_CAPTAIN = Integer.parseInt(generalSettings.getProperty("CaptainCost", "27"));
			RIFT_ENTER_COST_COMMANDER = Integer.parseInt(generalSettings.getProperty("CommanderCost", "30"));
			RIFT_ENTER_COST_HERO = Integer.parseInt(generalSettings.getProperty("HeroCost", "33"));
			RIFT_BOSS_ROOM_TIME_MUTIPLY = Float.parseFloat(generalSettings.getProperty("BossRoomTimeMultiply", "1.5"));
			
			DONT_DESTROY_SS = Boolean.parseBoolean(generalSettings.getProperty("DontDestroySS", "false"));
			
			STANDARD_RESPAWN_DELAY = Integer.parseInt(generalSettings.getProperty("StandardRespawnDelay", "180"));
			
			RAID_RANKING_1ST = Integer.parseInt(generalSettings.getProperty("1stRaidRankingPoints", "1250"));
			RAID_RANKING_2ND = Integer.parseInt(generalSettings.getProperty("2ndRaidRankingPoints", "900"));
			RAID_RANKING_3RD = Integer.parseInt(generalSettings.getProperty("3rdRaidRankingPoints", "700"));
			RAID_RANKING_4TH = Integer.parseInt(generalSettings.getProperty("4thRaidRankingPoints", "600"));
			RAID_RANKING_5TH = Integer.parseInt(generalSettings.getProperty("5thRaidRankingPoints", "450"));
			RAID_RANKING_6TH = Integer.parseInt(generalSettings.getProperty("6thRaidRankingPoints", "350"));
			RAID_RANKING_7TH = Integer.parseInt(generalSettings.getProperty("7thRaidRankingPoints", "300"));
			RAID_RANKING_8TH = Integer.parseInt(generalSettings.getProperty("8thRaidRankingPoints", "200"));
			RAID_RANKING_9TH = Integer.parseInt(generalSettings.getProperty("9thRaidRankingPoints", "150"));
			RAID_RANKING_10TH = Integer.parseInt(generalSettings.getProperty("10thRaidRankingPoints", "100"));
			RAID_RANKING_UP_TO_50TH = Integer.parseInt(generalSettings.getProperty("UpTo50thRaidRankingPoints", "25"));
			RAID_RANKING_UP_TO_100TH = Integer.parseInt(generalSettings.getProperty("UpTo100thRaidRankingPoints", "12"));
			
			EXPERTISE_PENALTY = Boolean.parseBoolean(generalSettings.getProperty("ExpertisePenalty", "true"));
			MASTERY_PENALTY = Boolean.parseBoolean(generalSettings.getProperty("MasteryPenalty", "false"));
			LEVEL_TO_GET_PENALTY = Integer.parseInt(generalSettings.getProperty("LevelToGetPenalty", "20"));
			
			MASTERY_WEAPON_PENALTY = Boolean.parseBoolean(generalSettings.getProperty("MasteryWeaponPenality", "false"));
			LEVEL_TO_GET_WEAPON_PENALTY = Integer.parseInt(generalSettings.getProperty("LevelToGetWeaponPenalty", "20"));
			
			ACTIVE_AUGMENTS_START_REUSE_TIME = Integer.parseInt(generalSettings.getProperty("AugmStartReuseTime", "0"));
			
			INVUL_NPC_LIST = new ArrayList<>();
			final String t = generalSettings.getProperty("InvulNpcList", "30001-32132,35092-35103,35142-35146,35176-35187,35218-35232,35261-35278,35308-35319,35352-35367,35382-35407,35417-35427,35433-35469,35497-35513,35544-35587,35600-35617,35623-35628,35638-35640,35644,35645,50007,70010,99999");
			String as[];
			final int k = (as = t.split(",")).length;
			for (int j = 0; j < k; j++)
			{
				final String t2 = as[j];
				if (t2.contains("-"))
				{
					final int a1 = Integer.parseInt(t2.split("-")[0]);
					final int a2 = Integer.parseInt(t2.split("-")[1]);
					for (int i = a1; i <= a2; i++)
					{
						INVUL_NPC_LIST.add(Integer.valueOf(i));
					}
				}
				else
				{
					INVUL_NPC_LIST.add(Integer.valueOf(Integer.parseInt(t2)));
				}
			}
			DISABLE_ATTACK_NPC_TYPE = Boolean.parseBoolean(generalSettings.getProperty("DisableAttackToNpcs", "false"));
			ALLOWED_NPC_TYPES = generalSettings.getProperty("AllowedNPCTypes");
			LIST_ALLOWED_NPC_TYPES = new ArrayList<>();
			for (String npc_type : ALLOWED_NPC_TYPES.split(","))
			{
				LIST_ALLOWED_NPC_TYPES.add(npc_type);
			}
			NPC_ATTACKABLE = Boolean.parseBoolean(generalSettings.getProperty("NpcAttackable", "false"));
			
			SELL_BY_ITEM = Boolean.parseBoolean(generalSettings.getProperty("SellByItem", "false"));
			SELL_ITEM = Integer.parseInt(generalSettings.getProperty("SellItem", "57"));
			
			ALT_MOBS_STATS_BONUS = Boolean.parseBoolean(generalSettings.getProperty("AltMobsStatsBonus", "true"));
			ALT_PETS_STATS_BONUS = Boolean.parseBoolean(generalSettings.getProperty("AltPetsStatsBonus", "true"));
			
			RAID_HP_REGEN_MULTIPLIER = Double.parseDouble(generalSettings.getProperty("RaidHpRegenMultiplier", "100")) / 100;
			RAID_MP_REGEN_MULTIPLIER = Double.parseDouble(generalSettings.getProperty("RaidMpRegenMultiplier", "100")) / 100;
			RAID_P_DEFENCE_MULTIPLIER = Double.parseDouble(generalSettings.getProperty("RaidPhysicalDefenceMultiplier", "100")) / 100;
			RAID_M_DEFENCE_MULTIPLIER = Double.parseDouble(generalSettings.getProperty("RaidMagicalDefenceMultiplier", "100")) / 100;
			RAID_MINION_RESPAWN_TIMER = Integer.parseInt(generalSettings.getProperty("RaidMinionRespawnTime", "300000"));
			RAID_MIN_RESPAWN_MULTIPLIER = Float.parseFloat(generalSettings.getProperty("RaidMinRespawnMultiplier", "1.0"));
			RAID_MAX_RESPAWN_MULTIPLIER = Float.parseFloat(generalSettings.getProperty("RaidMaxRespawnMultiplier", "1.0"));
			
			CLICK_TASK = Integer.parseInt(generalSettings.getProperty("ClickTaskDelay", "50"));
			
			WYVERN_SPEED = Integer.parseInt(generalSettings.getProperty("WyvernSpeed", "100"));
			STRIDER_SPEED = Integer.parseInt(generalSettings.getProperty("StriderSpeed", "80"));
			ALLOW_WYVERN_UPGRADER = Boolean.parseBoolean(generalSettings.getProperty("AllowWyvernUpgrader", "false"));
			
			ENABLE_AIO_SYSTEM = Boolean.parseBoolean(generalSettings.getProperty("EnableAioSystem", "true"));
			ALLOW_AIO_NCOLOR = Boolean.parseBoolean(generalSettings.getProperty("AllowAioNameColor", "true"));
			AIO_NCOLOR = Integer.decode("0x" + generalSettings.getProperty("AioNameColor", "88AA88"));
			ALLOW_AIO_TCOLOR = Boolean.parseBoolean(generalSettings.getProperty("AllowAioTitleColor", "true"));
			AIO_TCOLOR = Integer.decode("0x" + generalSettings.getProperty("AioTitleColor", "88AA88"));
			ALLOW_AIO_USE_GK = Boolean.parseBoolean(generalSettings.getProperty("AllowAioUseGk", "false"));
			ALLOW_AIO_USE_CM = Boolean.parseBoolean(generalSettings.getProperty("AllowAioUseClassMaster", "false"));
			ALLOW_AIO_IN_EVENTS = Boolean.parseBoolean(generalSettings.getProperty("AllowAioInEvents", "false"));
			if (ENABLE_AIO_SYSTEM)
			{
				final String[] AioSkillsSplit = generalSettings.getProperty("AioSkills", "").split(";");
				AIO_SKILLS = new HashMap<>(AioSkillsSplit.length);
				for (String skill : AioSkillsSplit)
				{
					final String[] skillSplit = skill.split(",");
					if (skillSplit.length != 2)
					{
						LOGGER.info("[Aio System]: invalid config property in " + GENERAL_CONFIG_FILE + " -> AioSkills \"" + skill + "\"");
					}
					else
					{
						try
						{
							AIO_SKILLS.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
						}
						catch (NumberFormatException nfe)
						{
							if (!skill.equals(""))
							{
								LOGGER.info("[Aio System]: invalid config property in " + GENERAL_CONFIG_FILE + " -> AioSkills \"" + skillSplit[0] + "\"" + skillSplit[1]);
							}
						}
					}
				}
			}
			
			PET_RENT_NPC = generalSettings.getProperty("ListPetRentNpc", "30827");
			LIST_PET_RENT_NPC = new ArrayList<>();
			for (String id : PET_RENT_NPC.split(","))
			{
				LIST_PET_RENT_NPC.add(Integer.parseInt(id));
			}
			NONDROPPABLE_ITEMS = generalSettings.getProperty("ListOfNonDroppableItems", "1147,425,1146,461,10,2368,7,6,2370,2369,5598");
			
			LIST_NONDROPPABLE_ITEMS = new ArrayList<>();
			for (String id : NONDROPPABLE_ITEMS.split(","))
			{
				LIST_NONDROPPABLE_ITEMS.add(Integer.parseInt(id));
			}
			
			JAIL_IS_PVP = Boolean.parseBoolean(generalSettings.getProperty("JailIsPvp", "true"));
			JAIL_DISABLE_CHAT = Boolean.parseBoolean(generalSettings.getProperty("JailDisableChat", "true"));
			
			USE_SAY_FILTER = Boolean.parseBoolean(generalSettings.getProperty("UseChatFilter", "false"));
			CHAT_FILTER_CHARS = generalSettings.getProperty("ChatFilterChars", "[I love L2jMobius]");
			CHAT_FILTER_PUNISHMENT = generalSettings.getProperty("ChatFilterPunishment", "off");
			CHAT_FILTER_PUNISHMENT_PARAM1 = Integer.parseInt(generalSettings.getProperty("ChatFilterPunishmentParam1", "1"));
			CHAT_FILTER_PUNISHMENT_PARAM2 = Integer.parseInt(generalSettings.getProperty("ChatFilterPunishmentParam2", "1000"));
			
			FS_TIME_ATTACK = Integer.parseInt(generalSettings.getProperty("TimeOfAttack", "50"));
			FS_TIME_COOLDOWN = Integer.parseInt(generalSettings.getProperty("TimeOfCoolDown", "5"));
			FS_TIME_ENTRY = Integer.parseInt(generalSettings.getProperty("TimeOfEntry", "3"));
			FS_TIME_WARMUP = Integer.parseInt(generalSettings.getProperty("TimeOfWarmUp", "2"));
			FS_PARTY_MEMBER_COUNT = Integer.parseInt(generalSettings.getProperty("NumberOfNecessaryPartyMembers", "4"));
			
			if (FS_TIME_ATTACK <= 0)
			{
				FS_TIME_ATTACK = 50;
			}
			if (FS_TIME_COOLDOWN <= 0)
			{
				FS_TIME_COOLDOWN = 5;
			}
			if (FS_TIME_ENTRY <= 0)
			{
				FS_TIME_ENTRY = 3;
			}
			if (FS_TIME_WARMUP <= 0)
			{
				FS_TIME_WARMUP = 2;
			}
			if (FS_PARTY_MEMBER_COUNT <= 0)
			{
				FS_PARTY_MEMBER_COUNT = 4;
			}
			
			ALLOW_QUAKE_SYSTEM = Boolean.parseBoolean(generalSettings.getProperty("AllowQuakeSystem", "false"));
			ENABLE_ANTI_PVP_FARM_MSG = Boolean.parseBoolean(generalSettings.getProperty("EnableAntiPvpFarmMsg", "false"));
			
			ANNOUNCE_CASTLE_LORDS = Boolean.parseBoolean(generalSettings.getProperty("AnnounceCastleLords", "false"));
			ANNOUNCE_MAMMON_SPAWN = Boolean.parseBoolean(generalSettings.getProperty("AnnounceMammonSpawn", "true"));
			ALLOW_GUARDS = Boolean.parseBoolean(generalSettings.getProperty("AllowGuards", "false"));
			
			AUTODESTROY_ITEM_AFTER = Integer.parseInt(generalSettings.getProperty("AutoDestroyDroppedItemAfter", "0"));
			HERB_AUTO_DESTROY_TIME = Integer.parseInt(generalSettings.getProperty("AutoDestroyHerbTime", "15")) * 1000;
			PROTECTED_ITEMS = generalSettings.getProperty("ListOfProtectedItems");
			LIST_PROTECTED_ITEMS = new ArrayList<>();
			for (String id : PROTECTED_ITEMS.split(","))
			{
				LIST_PROTECTED_ITEMS.add(Integer.parseInt(id));
			}
			DESTROY_DROPPED_PLAYER_ITEM = Boolean.parseBoolean(generalSettings.getProperty("DestroyPlayerDroppedItem", "false"));
			DESTROY_EQUIPABLE_PLAYER_ITEM = Boolean.parseBoolean(generalSettings.getProperty("DestroyEquipableItem", "false"));
			SAVE_DROPPED_ITEM = Boolean.parseBoolean(generalSettings.getProperty("SaveDroppedItem", "false"));
			EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD = Boolean.parseBoolean(generalSettings.getProperty("EmptyDroppedItemTableAfterLoad", "false"));
			SAVE_DROPPED_ITEM_INTERVAL = Integer.parseInt(generalSettings.getProperty("SaveDroppedItemInterval", "0")) * 60000;
			CLEAR_DROPPED_ITEM_TABLE = Boolean.parseBoolean(generalSettings.getProperty("ClearDroppedItemTable", "false"));
			
			PRECISE_DROP_CALCULATION = Boolean.parseBoolean(generalSettings.getProperty("PreciseDropCalculation", "true"));
			MULTIPLE_ITEM_DROP = Boolean.parseBoolean(generalSettings.getProperty("MultipleItemDrop", "true"));
			
			ALLOW_WAREHOUSE = Boolean.parseBoolean(generalSettings.getProperty("AllowWarehouse", "true"));
			WAREHOUSE_CACHE = Boolean.parseBoolean(generalSettings.getProperty("WarehouseCache", "false"));
			WAREHOUSE_CACHE_TIME = Integer.parseInt(generalSettings.getProperty("WarehouseCacheTime", "15"));
			ALLOW_FREIGHT = Boolean.parseBoolean(generalSettings.getProperty("AllowFreight", "true"));
			ALLOW_WEAR = Boolean.parseBoolean(generalSettings.getProperty("AllowWear", "false"));
			WEAR_DELAY = Integer.parseInt(generalSettings.getProperty("WearDelay", "5"));
			WEAR_PRICE = Integer.parseInt(generalSettings.getProperty("WearPrice", "10"));
			ALLOW_LOTTERY = Boolean.parseBoolean(generalSettings.getProperty("AllowLottery", "false"));
			ALLOW_RACE = Boolean.parseBoolean(generalSettings.getProperty("AllowRace", "false"));
			ALLOW_RENTPET = Boolean.parseBoolean(generalSettings.getProperty("AllowRentPet", "false"));
			ALLOW_DISCARDITEM = Boolean.parseBoolean(generalSettings.getProperty("AllowDiscardItem", "true"));
			ALLOWFISHING = Boolean.parseBoolean(generalSettings.getProperty("AllowFishing", "false"));
			ALLOW_MANOR = Boolean.parseBoolean(generalSettings.getProperty("AllowManor", "false"));
			ALLOW_BOAT = Boolean.parseBoolean(generalSettings.getProperty("AllowBoat", "false"));
			ALLOW_NPC_WALKERS = Boolean.parseBoolean(generalSettings.getProperty("AllowNpcWalkers", "true"));
			ALLOW_CURSED_WEAPONS = Boolean.parseBoolean(generalSettings.getProperty("AllowCursedWeapons", "false"));
			
			DEFAULT_GLOBAL_CHAT = generalSettings.getProperty("GlobalChat", "ON");
			DEFAULT_TRADE_CHAT = generalSettings.getProperty("TradeChat", "ON");
			MAX_CHAT_LENGTH = Integer.parseInt(generalSettings.getProperty("MaxChatLength", "100"));
			
			TRADE_CHAT_IS_NOOBLE = Boolean.parseBoolean(generalSettings.getProperty("TradeChatIsNooble", "false"));
			TRADE_CHAT_WITH_PVP = Boolean.parseBoolean(generalSettings.getProperty("TradeChatWithPvP", "false"));
			TRADE_PVP_AMOUNT = Integer.parseInt(generalSettings.getProperty("TradePvPAmount", "800"));
			GLOBAL_CHAT_WITH_PVP = Boolean.parseBoolean(generalSettings.getProperty("GlobalChatWithPvP", "false"));
			GLOBAL_PVP_AMOUNT = Integer.parseInt(generalSettings.getProperty("GlobalPvPAmount", "1500"));
			
			ENABLE_COMMUNITY_BOARD = Boolean.parseBoolean(generalSettings.getProperty("EnableCommunityBoard", "true"));
			BBS_DEFAULT = generalSettings.getProperty("BBSDefault", "_bbshome");
			
			ZONE_TOWN = Integer.parseInt(generalSettings.getProperty("ZoneTown", "0"));
			
			MAX_DRIFT_RANGE = Integer.parseInt(generalSettings.getProperty("MaxDriftRange", "300"));
			
			MIN_NPC_ANIMATION = Integer.parseInt(generalSettings.getProperty("MinNpcAnimation", "5"));
			MAX_NPC_ANIMATION = Integer.parseInt(generalSettings.getProperty("MaxNpcAnimation", "60"));
			MIN_MONSTER_ANIMATION = Integer.parseInt(generalSettings.getProperty("MinMonsterAnimation", "5"));
			MAX_MONSTER_ANIMATION = Integer.parseInt(generalSettings.getProperty("MaxMonsterAnimation", "60"));
			
			SHOW_NPC_LVL = Boolean.parseBoolean(generalSettings.getProperty("ShowNpcLevel", "false"));
			SHOW_NPC_AGGRESSION = Boolean.parseBoolean(generalSettings.getProperty("ShowNpcAggression", "false"));
			
			FORCE_INVENTORY_UPDATE = Boolean.parseBoolean(generalSettings.getProperty("ForceInventoryUpdate", "false"));
			
			FORCE_COMPLETE_STATUS_UPDATE = Boolean.parseBoolean(generalSettings.getProperty("ForceCompletePlayerStatusUpdate", "true"));
			
			AUTODELETE_INVALID_QUEST_DATA = Boolean.parseBoolean(generalSettings.getProperty("AutoDeleteInvalidQuestData", "false"));
			
			DELETE_DAYS = Integer.parseInt(generalSettings.getProperty("DeleteCharAfterDays", "7"));
			
			DEFAULT_PUNISH = Integer.parseInt(generalSettings.getProperty("DefaultPunish", "2"));
			DEFAULT_PUNISH_PARAM = Integer.parseInt(generalSettings.getProperty("DefaultPunishParam", "0"));
			
			GRIDS_ALWAYS_ON = Boolean.parseBoolean(generalSettings.getProperty("GridsAlwaysOn", "false"));
			GRID_NEIGHBOR_TURNON_TIME = Integer.parseInt(generalSettings.getProperty("GridNeighborTurnOnTime", "30"));
			GRID_NEIGHBOR_TURNOFF_TIME = Integer.parseInt(generalSettings.getProperty("GridNeighborTurnOffTime", "300"));
			
			USE_3D_MAP = Boolean.parseBoolean(generalSettings.getProperty("Use3DMap", "false"));
			
			PATH_NODE_RADIUS = Integer.parseInt(generalSettings.getProperty("PathNodeRadius", "50"));
			NEW_NODE_ID = Integer.parseInt(generalSettings.getProperty("NewNodeId", "7952"));
			SELECTED_NODE_ID = Integer.parseInt(generalSettings.getProperty("NewNodeId", "7952"));
			LINKED_NODE_ID = Integer.parseInt(generalSettings.getProperty("NewNodeId", "7952"));
			NEW_NODE_TYPE = generalSettings.getProperty("NewNodeType", "npc");
			
			COUNT_PACKETS = Boolean.parseBoolean(generalSettings.getProperty("CountPacket", "false"));
			DUMP_PACKET_COUNTS = Boolean.parseBoolean(generalSettings.getProperty("DumpPacketCounts", "false"));
			DUMP_INTERVAL_SECONDS = Integer.parseInt(generalSettings.getProperty("PacketDumpInterval", "60"));
			
			MINIMUM_UPDATE_DISTANCE = Integer.parseInt(generalSettings.getProperty("MaximumUpdateDistance", "50"));
			MINIMUN_UPDATE_TIME = Integer.parseInt(generalSettings.getProperty("MinimumUpdateTime", "500"));
			KNOWNLIST_FORGET_DELAY = Integer.parseInt(generalSettings.getProperty("KnownListForgetDelay", "10000"));
			
			HIGH_RATE_SERVER_DROPS = Boolean.parseBoolean(generalSettings.getProperty("HighRateServerDrops", "false"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + GENERAL_CONFIG_FILE + " File.");
		}
	}
	
	public static void load7sConfig()
	{
		try
		{
			final Properties SevenSettings = new Properties();
			final InputStream is = new FileInputStream(new File(SEVENSIGNS_CONFIG_FILE));
			SevenSettings.load(is);
			is.close();
			
			ALT_GAME_REQUIRE_CASTLE_DAWN = Boolean.parseBoolean(SevenSettings.getProperty("AltRequireCastleForDawn", "false"));
			ALT_GAME_REQUIRE_CLAN_CASTLE = Boolean.parseBoolean(SevenSettings.getProperty("AltRequireClanCastle", "false"));
			ALT_REQUIRE_WIN_7S = Boolean.parseBoolean(SevenSettings.getProperty("AltRequireWin7s", "true"));
			ALT_FESTIVAL_MIN_PLAYER = Integer.parseInt(SevenSettings.getProperty("AltFestivalMinPlayer", "5"));
			ALT_MAXIMUM_PLAYER_CONTRIB = Integer.parseInt(SevenSettings.getProperty("AltMaxPlayerContrib", "1000000"));
			ALT_FESTIVAL_MANAGER_START = Long.parseLong(SevenSettings.getProperty("AltFestivalManagerStart", "120000"));
			ALT_FESTIVAL_LENGTH = Long.parseLong(SevenSettings.getProperty("AltFestivalLength", "1080000"));
			ALT_FESTIVAL_CYCLE_LENGTH = Long.parseLong(SevenSettings.getProperty("AltFestivalCycleLength", "2280000"));
			ALT_FESTIVAL_FIRST_SPAWN = Long.parseLong(SevenSettings.getProperty("AltFestivalFirstSpawn", "120000"));
			ALT_FESTIVAL_FIRST_SWARM = Long.parseLong(SevenSettings.getProperty("AltFestivalFirstSwarm", "300000"));
			ALT_FESTIVAL_SECOND_SPAWN = Long.parseLong(SevenSettings.getProperty("AltFestivalSecondSpawn", "540000"));
			ALT_FESTIVAL_SECOND_SWARM = Long.parseLong(SevenSettings.getProperty("AltFestivalSecondSwarm", "720000"));
			ALT_FESTIVAL_CHEST_SPAWN = Long.parseLong(SevenSettings.getProperty("AltFestivalChestSpawn", "900000"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + SEVENSIGNS_CONFIG_FILE + " File.");
		}
	}
	
	public static void loadCHConfig()
	{
		try
		{
			final Properties clanhallSettings = new Properties();
			final InputStream is = new FileInputStream(new File(CLANHALL_CONFIG_FILE));
			clanhallSettings.load(is);
			is.close();
			CH_TELE_FEE_RATIO = Long.parseLong(clanhallSettings.getProperty("ClanHallTeleportFunctionFeeRation", "86400000"));
			CH_TELE1_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallTeleportFunctionFeeLvl1", "86400000"));
			CH_TELE2_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallTeleportFunctionFeeLvl2", "86400000"));
			CH_SUPPORT_FEE_RATIO = Long.parseLong(clanhallSettings.getProperty("ClanHallSupportFunctionFeeRation", "86400000"));
			CH_SUPPORT1_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallSupportFeeLvl1", "86400000"));
			CH_SUPPORT2_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallSupportFeeLvl2", "86400000"));
			CH_SUPPORT3_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallSupportFeeLvl3", "86400000"));
			CH_SUPPORT4_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallSupportFeeLvl4", "86400000"));
			CH_SUPPORT5_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallSupportFeeLvl5", "86400000"));
			CH_SUPPORT6_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallSupportFeeLvl6", "86400000"));
			CH_SUPPORT7_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallSupportFeeLvl7", "86400000"));
			CH_SUPPORT8_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallSupportFeeLvl8", "86400000"));
			CH_MPREG_FEE_RATIO = Long.parseLong(clanhallSettings.getProperty("ClanHallMpRegenerationFunctionFeeRation", "86400000"));
			CH_MPREG1_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl1", "86400000"));
			CH_MPREG2_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl2", "86400000"));
			CH_MPREG3_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl3", "86400000"));
			CH_MPREG4_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl4", "86400000"));
			CH_MPREG5_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallMpRegenerationFeeLvl5", "86400000"));
			CH_HPREG_FEE_RATIO = Long.parseLong(clanhallSettings.getProperty("ClanHallHpRegenerationFunctionFeeRation", "86400000"));
			CH_HPREG1_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl1", "86400000"));
			CH_HPREG2_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl2", "86400000"));
			CH_HPREG3_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl3", "86400000"));
			CH_HPREG4_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl4", "86400000"));
			CH_HPREG5_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl5", "86400000"));
			CH_HPREG6_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl6", "86400000"));
			CH_HPREG7_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl7", "86400000"));
			CH_HPREG8_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl8", "86400000"));
			CH_HPREG9_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl9", "86400000"));
			CH_HPREG10_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl10", "86400000"));
			CH_HPREG11_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl11", "86400000"));
			CH_HPREG12_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl12", "86400000"));
			CH_HPREG13_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallHpRegenerationFeeLvl13", "86400000"));
			CH_EXPREG_FEE_RATIO = Long.parseLong(clanhallSettings.getProperty("ClanHallExpRegenerationFunctionFeeRation", "86400000"));
			CH_EXPREG1_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl1", "86400000"));
			CH_EXPREG2_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl2", "86400000"));
			CH_EXPREG3_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl3", "86400000"));
			CH_EXPREG4_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl4", "86400000"));
			CH_EXPREG5_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl5", "86400000"));
			CH_EXPREG6_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl6", "86400000"));
			CH_EXPREG7_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallExpRegenerationFeeLvl7", "86400000"));
			CH_ITEM_FEE_RATIO = Long.parseLong(clanhallSettings.getProperty("ClanHallItemCreationFunctionFeeRation", "86400000"));
			CH_ITEM1_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallItemCreationFunctionFeeLvl1", "86400000"));
			CH_ITEM2_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallItemCreationFunctionFeeLvl2", "86400000"));
			CH_ITEM3_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallItemCreationFunctionFeeLvl3", "86400000"));
			CH_CURTAIN_FEE_RATIO = Long.parseLong(clanhallSettings.getProperty("ClanHallCurtainFunctionFeeRation", "86400000"));
			CH_CURTAIN1_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallCurtainFunctionFeeLvl1", "86400000"));
			CH_CURTAIN2_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallCurtainFunctionFeeLvl2", "86400000"));
			CH_FRONT_FEE_RATIO = Long.parseLong(clanhallSettings.getProperty("ClanHallFrontPlatformFunctionFeeRation", "86400000"));
			CH_FRONT1_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallFrontPlatformFunctionFeeLvl1", "86400000"));
			CH_FRONT2_FEE = Integer.parseInt(clanhallSettings.getProperty("ClanHallFrontPlatformFunctionFeeLvl2", "86400000"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + CLANHALL_CONFIG_FILE + " File.");
		}
	}
	
	public static void loadElitCHConfig()
	{
		try
		{
			final Properties elitchSettings = new Properties();
			final InputStream is = new FileInputStream(new File(ELIT_CLANHALL_CONFIG_FILE));
			elitchSettings.load(is);
			is.close();
			
			DEVASTATED_DAY = Integer.parseInt(elitchSettings.getProperty("DevastatedDay", "1"));
			DEVASTATED_HOUR = Integer.parseInt(elitchSettings.getProperty("DevastatedHour", "18"));
			DEVASTATED_MINUTES = Integer.parseInt(elitchSettings.getProperty("DevastatedMinutes", "0"));
			PARTISAN_DAY = Integer.parseInt(elitchSettings.getProperty("PartisanDay", "5"));
			PARTISAN_HOUR = Integer.parseInt(elitchSettings.getProperty("PartisanHour", "21"));
			PARTISAN_MINUTES = Integer.parseInt(elitchSettings.getProperty("PartisanMinutes", "0"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + ELIT_CLANHALL_CONFIG_FILE + " File.");
		}
	}
	
	public static void loadChampionConfig()
	{
		try
		{
			final Properties ChampionSettings = new Properties();
			final InputStream is = new FileInputStream(new File(CHAMPION_CONFIG_FILE));
			ChampionSettings.load(is);
			is.close();
			
			L2JMOD_CHAMPION_ENABLE = Boolean.parseBoolean(ChampionSettings.getProperty("ChampionEnable", "false"));
			L2JMOD_CHAMPION_FREQUENCY = Integer.parseInt(ChampionSettings.getProperty("ChampionFrequency", "0"));
			L2JMOD_CHAMP_MIN_LVL = Integer.parseInt(ChampionSettings.getProperty("ChampionMinLevel", "20"));
			L2JMOD_CHAMP_MAX_LVL = Integer.parseInt(ChampionSettings.getProperty("ChampionMaxLevel", "60"));
			L2JMOD_CHAMPION_HP = Integer.parseInt(ChampionSettings.getProperty("ChampionHp", "7"));
			L2JMOD_CHAMPION_HP_REGEN = Float.parseFloat(ChampionSettings.getProperty("ChampionHpRegen", "1.0"));
			L2JMOD_CHAMPION_REWARDS = Integer.parseInt(ChampionSettings.getProperty("ChampionRewards", "8"));
			L2JMOD_CHAMPION_ADENAS_REWARDS = Integer.parseInt(ChampionSettings.getProperty("ChampionAdenasRewards", "1"));
			L2JMOD_CHAMPION_ATK = Float.parseFloat(ChampionSettings.getProperty("ChampionAtk", "1.0"));
			L2JMOD_CHAMPION_SPD_ATK = Float.parseFloat(ChampionSettings.getProperty("ChampionSpdAtk", "1.0"));
			L2JMOD_CHAMPION_REWARD = Integer.parseInt(ChampionSettings.getProperty("ChampionRewardItem", "0"));
			L2JMOD_CHAMPION_REWARD_ID = Integer.parseInt(ChampionSettings.getProperty("ChampionRewardItemID", "6393"));
			L2JMOD_CHAMPION_REWARD_QTY = Integer.parseInt(ChampionSettings.getProperty("ChampionRewardItemQty", "1"));
			L2JMOD_CHAMP_TITLE = ChampionSettings.getProperty("ChampionTitle", "Champion");
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + CHAMPION_CONFIG_FILE + " File.");
		}
	}
	
	public static void loadMerchantZeroPriceConfig()
	{
		try
		{
			final Properties MerchantZeroSellPrice = new Properties();
			final InputStream is = new FileInputStream(new File(MERCHANT_ZERO_SELL_PRICE_CONFIG_FILE));
			MerchantZeroSellPrice.load(is);
			is.close();
			
			MERCHANT_ZERO_SELL_PRICE = Boolean.parseBoolean(MerchantZeroSellPrice.getProperty("MerchantZeroSellPrice", "false"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + CHAMPION_CONFIG_FILE + " File.");
		}
	}
	
	public static void loadWeddingConfig()
	{
		try
		{
			final Properties WeddingSettings = new Properties();
			final InputStream is = new FileInputStream(new File(EVENT_WEDDING_CONFIG_FILE));
			WeddingSettings.load(is);
			is.close();
			
			L2JMOD_ALLOW_WEDDING = Boolean.parseBoolean(WeddingSettings.getProperty("AllowWedding", "false"));
			L2JMOD_WEDDING_PRICE = Integer.parseInt(WeddingSettings.getProperty("WeddingPrice", "250000000"));
			L2JMOD_WEDDING_PUNISH_INFIDELITY = Boolean.parseBoolean(WeddingSettings.getProperty("WeddingPunishInfidelity", "true"));
			L2JMOD_WEDDING_TELEPORT = Boolean.parseBoolean(WeddingSettings.getProperty("WeddingTeleport", "true"));
			L2JMOD_WEDDING_TELEPORT_PRICE = Integer.parseInt(WeddingSettings.getProperty("WeddingTeleportPrice", "50000"));
			L2JMOD_WEDDING_TELEPORT_DURATION = Integer.parseInt(WeddingSettings.getProperty("WeddingTeleportDuration", "60"));
			L2JMOD_WEDDING_NAME_COLOR_NORMAL = Integer.decode("0x" + WeddingSettings.getProperty("WeddingNameCollorN", "FFFFFF"));
			L2JMOD_WEDDING_NAME_COLOR_GEY = Integer.decode("0x" + WeddingSettings.getProperty("WeddingNameCollorB", "FFFFFF"));
			L2JMOD_WEDDING_NAME_COLOR_LESBO = Integer.decode("0x" + WeddingSettings.getProperty("WeddingNameCollorL", "FFFFFF"));
			L2JMOD_WEDDING_SAMESEX = Boolean.parseBoolean(WeddingSettings.getProperty("WeddingAllowSameSex", "false"));
			L2JMOD_WEDDING_FORMALWEAR = Boolean.parseBoolean(WeddingSettings.getProperty("WeddingFormalWear", "true"));
			L2JMOD_WEDDING_DIVORCE_COSTS = Integer.parseInt(WeddingSettings.getProperty("WeddingDivorceCosts", "20"));
			WEDDING_GIVE_CUPID_BOW = Boolean.parseBoolean(WeddingSettings.getProperty("WeddingGiveBow", "false"));
			ANNOUNCE_WEDDING = Boolean.parseBoolean(WeddingSettings.getProperty("AnnounceWedding", "true"));
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + EVENT_WEDDING_CONFIG_FILE + " File.");
		}
	}
	
	public static void loadTVTConfig()
	{
		try
		{
			final Properties TVTSettings = new Properties();
			final InputStream is = new FileInputStream(new File(EVENT_TVT_CONFIG_FILE));
			TVTSettings.load(is);
			is.close();
			
			TVT_EVEN_TEAMS = TVTSettings.getProperty("TvTEvenTeams", "BALANCE");
			TVT_ALLOW_INTERFERENCE = Boolean.parseBoolean(TVTSettings.getProperty("TvTAllowInterference", "false"));
			TVT_ALLOW_POTIONS = Boolean.parseBoolean(TVTSettings.getProperty("TvTAllowPotions", "false"));
			TVT_ALLOW_SUMMON = Boolean.parseBoolean(TVTSettings.getProperty("TvTAllowSummon", "false"));
			TVT_ON_START_REMOVE_ALL_EFFECTS = Boolean.parseBoolean(TVTSettings.getProperty("TvTOnStartRemoveAllEffects", "true"));
			TVT_ON_START_UNSUMMON_PET = Boolean.parseBoolean(TVTSettings.getProperty("TvTOnStartUnsummonPet", "true"));
			TVT_REVIVE_RECOVERY = Boolean.parseBoolean(TVTSettings.getProperty("TvTReviveRecovery", "false"));
			TVT_ANNOUNCE_TEAM_STATS = Boolean.parseBoolean(TVTSettings.getProperty("TvTAnnounceTeamStats", "false"));
			TVT_ANNOUNCE_REWARD = Boolean.parseBoolean(TVTSettings.getProperty("TvTAnnounceReward", "false"));
			TVT_PRICE_NO_KILLS = Boolean.parseBoolean(TVTSettings.getProperty("TvTPriceNoKills", "false"));
			TVT_JOIN_CURSED = Boolean.parseBoolean(TVTSettings.getProperty("TvTJoinWithCursedWeapon", "true"));
			TVT_COMMAND = Boolean.parseBoolean(TVTSettings.getProperty("TvTCommand", "true"));
			TVT_REVIVE_DELAY = Long.parseLong(TVTSettings.getProperty("TvTReviveDelay", "20000"));
			if (TVT_REVIVE_DELAY < 1000)
			{
				TVT_REVIVE_DELAY = 1000; // can't be set less then 1 second
			}
			TVT_OPEN_FORT_DOORS = Boolean.parseBoolean(TVTSettings.getProperty("TvTOpenFortDoors", "false"));
			TVT_CLOSE_FORT_DOORS = Boolean.parseBoolean(TVTSettings.getProperty("TvTCloseFortDoors", "false"));
			TVT_OPEN_ADEN_COLOSSEUM_DOORS = Boolean.parseBoolean(TVTSettings.getProperty("TvTOpenAdenColosseumDoors", "false"));
			TVT_CLOSE_ADEN_COLOSSEUM_DOORS = Boolean.parseBoolean(TVTSettings.getProperty("TvTCloseAdenColosseumDoors", "false"));
			TVT_TOP_KILLER_REWARD = Integer.parseInt(TVTSettings.getProperty("TvTTopKillerRewardId", "5575"));
			TVT_TOP_KILLER_QTY = Integer.parseInt(TVTSettings.getProperty("TvTTopKillerRewardQty", "2000000"));
			TVT_AURA = Boolean.parseBoolean(TVTSettings.getProperty("TvTAura", "false"));
			TVT_STATS_LOGGER = Boolean.parseBoolean(TVTSettings.getProperty("TvTStatsLogger", "true"));
			
			TVT_REMOVE_BUFFS_ON_DIE = Boolean.parseBoolean(TVTSettings.getProperty("TvTRemoveBuffsOnPlayerDie", "false"));
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + EVENT_TVT_CONFIG_FILE + " File.");
		}
	}
	
	public static void loadTWConfig()
	{
		try
		{
			final Properties TWSettings = new Properties();
			final InputStream is = new FileInputStream(new File(EVENT_TW_CONFIG_FILE));
			TWSettings.load(is);
			is.close();
			
			TW_TOWN_ID = Integer.parseInt(TWSettings.getProperty("TWTownId", "9"));
			TW_ALL_TOWNS = Boolean.parseBoolean(TWSettings.getProperty("TWAllTowns", "false"));
			TW_ITEM_ID = Integer.parseInt(TWSettings.getProperty("TownWarItemId", "57"));
			TW_ITEM_AMOUNT = Integer.parseInt(TWSettings.getProperty("TownWarItemAmount", "5000"));
			TW_ALLOW_KARMA = Boolean.parseBoolean(TWSettings.getProperty("AllowKarma", "false"));
			TW_DISABLE_GK = Boolean.parseBoolean(TWSettings.getProperty("DisableGK", "true"));
			TW_RESS_ON_DIE = Boolean.parseBoolean(TWSettings.getProperty("SendRessOnDeath", "false"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + EVENT_TW_CONFIG_FILE + " File.");
		}
	}
	
	public static void loadREBIRTHConfig()
	{
		try
		{
			final Properties REBIRTHSettings = new Properties();
			final InputStream is = new FileInputStream(new File(EVENT_REBIRTH_CONFIG_FILE));
			REBIRTHSettings.load(is);
			is.close();
			
			REBIRTH_ENABLE = Boolean.parseBoolean(REBIRTHSettings.getProperty("REBIRTH_ENABLE", "false"));
			REBIRTH_MIN_LEVEL = Integer.parseInt(REBIRTHSettings.getProperty("REBIRTH_MIN_LEVEL", "80"));
			REBIRTH_MAX = Integer.parseInt(REBIRTHSettings.getProperty("REBIRTH_MAX", "3"));
			REBIRTH_RETURN_TO_LEVEL = Integer.parseInt(REBIRTHSettings.getProperty("REBIRTH_RETURN_TO_LEVEL", "1"));
			
			REBIRTH_ITEM_PRICE = REBIRTHSettings.getProperty("REBIRTH_ITEM_PRICE", "").split(";");
			REBIRTH_MAGE_SKILL = REBIRTHSettings.getProperty("REBIRTH_MAGE_SKILL", "").split(";");
			REBIRTH_FIGHTER_SKILL = REBIRTHSettings.getProperty("REBIRTH_FIGHTER_SKILL", "").split(";");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + EVENT_REBIRTH_CONFIG_FILE + " File.");
		}
	}
	
	public static void loadPCBPointConfig()
	{
		try
		{
			final Properties pcbpSettings = new Properties();
			final InputStream is = new FileInputStream(new File(EVENT_PC_BANG_POINT_CONFIG_FILE));
			pcbpSettings.load(is);
			is.close();
			
			PCB_ENABLE = Boolean.parseBoolean(pcbpSettings.getProperty("PcBangPointEnable", "true"));
			PCB_MIN_LEVEL = Integer.parseInt(pcbpSettings.getProperty("PcBangPointMinLevel", "20"));
			PCB_POINT_MIN = Integer.parseInt(pcbpSettings.getProperty("PcBangPointMinCount", "20"));
			PCB_POINT_MAX = Integer.parseInt(pcbpSettings.getProperty("PcBangPointMaxCount", "1000000"));
			
			if (PCB_POINT_MAX < 1)
			{
				PCB_POINT_MAX = Integer.MAX_VALUE;
			}
			
			PCB_CHANCE_DUAL_POINT = Integer.parseInt(pcbpSettings.getProperty("PcBangPointDualChance", "20"));
			PCB_INTERVAL = Integer.parseInt(pcbpSettings.getProperty("PcBangPointTimeStamp", "900"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + EVENT_PC_BANG_POINT_CONFIG_FILE + " File.");
		}
	}
	
	public static void loadCraftConfig()
	{
		try
		{
			final Properties craftSettings = new Properties();
			final InputStream is = new FileInputStream(new File(CRAFTING_CONFIG_FILE));
			craftSettings.load(is);
			is.close();
			
			DWARF_RECIPE_LIMIT = Integer.parseInt(craftSettings.getProperty("DwarfRecipeLimit", "50"));
			COMMON_RECIPE_LIMIT = Integer.parseInt(craftSettings.getProperty("CommonRecipeLimit", "50"));
			IS_CRAFTING_ENABLED = Boolean.parseBoolean(craftSettings.getProperty("CraftingEnabled", "true"));
			ALT_GAME_CREATION = Boolean.parseBoolean(craftSettings.getProperty("AltGameCreation", "false"));
			ALT_GAME_CREATION_SPEED = Double.parseDouble(craftSettings.getProperty("AltGameCreationSpeed", "1"));
			ALT_GAME_CREATION_XP_RATE = Double.parseDouble(craftSettings.getProperty("AltGameCreationRateXp", "1"));
			ALT_GAME_CREATION_SP_RATE = Double.parseDouble(craftSettings.getProperty("AltGameCreationRateSp", "1"));
			ALT_BLACKSMITH_USE_RECIPES = Boolean.parseBoolean(craftSettings.getProperty("AltBlacksmithUseRecipes", "true"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + CRAFTING_CONFIG_FILE + " File.");
		}
	}
	
	public static void loadAWAYConfig()
	{
		try
		{
			final Properties AWAYSettings = new Properties();
			final InputStream is = new FileInputStream(new File(AWAY_CONFIG_FILE));
			AWAYSettings.load(is);
			is.close();
			
			/** Away System **/
			ALLOW_AWAY_STATUS = Boolean.parseBoolean(AWAYSettings.getProperty("AllowAwayStatus", "false"));
			AWAY_PLAYER_TAKE_AGGRO = Boolean.parseBoolean(AWAYSettings.getProperty("AwayPlayerTakeAggro", "false"));
			AWAY_TITLE_COLOR = Integer.decode("0x" + AWAYSettings.getProperty("AwayTitleColor", "0000FF"));
			AWAY_TIMER = Integer.parseInt(AWAYSettings.getProperty("AwayTimer", "30"));
			BACK_TIMER = Integer.parseInt(AWAYSettings.getProperty("BackTimer", "30"));
			AWAY_PEACE_ZONE = Boolean.parseBoolean(AWAYSettings.getProperty("AwayOnlyInPeaceZone", "false"));
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + AWAY_CONFIG_FILE + " File.");
		}
	}
	
	public static void loadBankingConfig()
	{
		try
		{
			final Properties BANKSettings = new Properties();
			final InputStream is = new FileInputStream(new File(BANK_CONFIG_FILE));
			BANKSettings.load(is);
			is.close();
			
			BANKING_SYSTEM_ENABLED = Boolean.parseBoolean(BANKSettings.getProperty("BankingEnabled", "false"));
			BANKING_SYSTEM_GOLDBARS = Integer.parseInt(BANKSettings.getProperty("BankingGoldbarCount", "1"));
			BANKING_SYSTEM_ADENA = Integer.parseInt(BANKSettings.getProperty("BankingAdenaCount", "500000000"));
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + BANK_CONFIG_FILE + " File.");
		}
	}
	
	public static void loadBufferConfig()
	{
		try
		{
			final Properties ShemeBufferSettings = new Properties();
			final InputStream is = new FileInputStream(new File(SCHEME_BUFFER_CONFIG_FILE));
			ShemeBufferSettings.load(is);
			is.close();
			
			BUFFER_MAX_SCHEMES = Integer.parseInt(ShemeBufferSettings.getProperty("BufferMaxSchemesPerChar", "4"));
			BUFFER_STATIC_BUFF_COST = Integer.parseInt(ShemeBufferSettings.getProperty("BufferStaticCostPerBuff", "-1"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + SCHEME_BUFFER_CONFIG_FILE + " File.");
		}
	}
	
	public static void loadOfflineConfig()
	{
		try
		{
			final Properties OfflineSettings = new Properties();
			final InputStream is = new FileInputStream(new File(OFFLINE_CONFIG_FILE));
			OfflineSettings.load(is);
			is.close();
			
			OFFLINE_TRADE_ENABLE = Boolean.parseBoolean(OfflineSettings.getProperty("OfflineTradeEnable", "false"));
			OFFLINE_CRAFT_ENABLE = Boolean.parseBoolean(OfflineSettings.getProperty("OfflineCraftEnable", "false"));
			OFFLINE_SET_NAME_COLOR = Boolean.parseBoolean(OfflineSettings.getProperty("OfflineNameColorEnable", "false"));
			OFFLINE_NAME_COLOR = Integer.decode("0x" + OfflineSettings.getProperty("OfflineNameColor", "ff00ff"));
			
			OFFLINE_MODE_IN_PEACE_ZONE = Boolean.parseBoolean(OfflineSettings.getProperty("OfflineModeInPeaceZone", "false"));
			OFFLINE_MODE_SET_INVULNERABLE = Boolean.parseBoolean(OfflineSettings.getProperty("OfflineModeSetInvulnerable", "false"));
			
			OFFLINE_COMMAND1 = Boolean.parseBoolean(OfflineSettings.getProperty("OfflineCommand1", "true"));
			OFFLINE_COMMAND2 = Boolean.parseBoolean(OfflineSettings.getProperty("OfflineCommand2", "false"));
			OFFLINE_LOGOUT = Boolean.parseBoolean(OfflineSettings.getProperty("OfflineLogout", "false"));
			OFFLINE_SLEEP_EFFECT = Boolean.parseBoolean(OfflineSettings.getProperty("OfflineSleepEffect", "true"));
			
			RESTORE_OFFLINERS = Boolean.parseBoolean(OfflineSettings.getProperty("RestoreOffliners", "false"));
			OFFLINE_MAX_DAYS = Integer.parseInt(OfflineSettings.getProperty("OfflineMaxDays", "10"));
			OFFLINE_DISCONNECT_FINISHED = Boolean.parseBoolean(OfflineSettings.getProperty("OfflineDisconnectFinished", "true"));
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + OFFLINE_CONFIG_FILE + " File.");
		}
	}
	
	public static void loadDMConfig()
	{
		try
		{
			final Properties DMSettings = new Properties();
			final InputStream is = new FileInputStream(new File(EVENT_DM_CONFIG_FILE));
			DMSettings.load(is);
			is.close();
			
			DM_ALLOW_INTERFERENCE = Boolean.parseBoolean(DMSettings.getProperty("DMAllowInterference", "false"));
			DM_ALLOW_POTIONS = Boolean.parseBoolean(DMSettings.getProperty("DMAllowPotions", "false"));
			DM_ALLOW_SUMMON = Boolean.parseBoolean(DMSettings.getProperty("DMAllowSummon", "false"));
			DM_JOIN_CURSED = Boolean.parseBoolean(DMSettings.getProperty("DMJoinWithCursedWeapon", "false"));
			DM_ON_START_REMOVE_ALL_EFFECTS = Boolean.parseBoolean(DMSettings.getProperty("DMOnStartRemoveAllEffects", "true"));
			DM_ON_START_UNSUMMON_PET = Boolean.parseBoolean(DMSettings.getProperty("DMOnStartUnsummonPet", "true"));
			DM_REVIVE_DELAY = Long.parseLong(DMSettings.getProperty("DMReviveDelay", "20000"));
			if (DM_REVIVE_DELAY < 1000)
			{
				DM_REVIVE_DELAY = 1000; // can't be set less then 1 second
			}
			
			DM_REVIVE_RECOVERY = Boolean.parseBoolean(DMSettings.getProperty("DMReviveRecovery", "false"));
			
			DM_COMMAND = Boolean.parseBoolean(DMSettings.getProperty("DMCommand", "false"));
			DM_ENABLE_KILL_REWARD = Boolean.parseBoolean(DMSettings.getProperty("DMEnableKillReward", "false"));
			DM_KILL_REWARD_ID = Integer.parseInt(DMSettings.getProperty("DMKillRewardID", "6392"));
			DM_KILL_REWARD_AMOUNT = Integer.parseInt(DMSettings.getProperty("DMKillRewardAmount", "1"));
			
			DM_ANNOUNCE_REWARD = Boolean.parseBoolean(DMSettings.getProperty("DMAnnounceReward", "false"));
			DM_SPAWN_OFFSET = Integer.parseInt(DMSettings.getProperty("DMSpawnOffset", "100"));
			
			DM_STATS_LOGGER = Boolean.parseBoolean(DMSettings.getProperty("DMStatsLogger", "true"));
			
			DM_ALLOW_HEALER_CLASSES = Boolean.parseBoolean(DMSettings.getProperty("DMAllowedHealerClasses", "true"));
			
			DM_REMOVE_BUFFS_ON_DIE = Boolean.parseBoolean(DMSettings.getProperty("DMRemoveBuffsOnPlayerDie", "false"));
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + EVENT_DM_CONFIG_FILE + " File.");
		}
	}
	
	public static void loadCTFConfig()
	{
		try
		{
			final Properties CTFSettings = new Properties();
			final InputStream is = new FileInputStream(new File(EVENT_CTF_CONFIG_FILE));
			CTFSettings.load(is);
			is.close();
			
			CTF_EVEN_TEAMS = CTFSettings.getProperty("CTFEvenTeams", "BALANCE");
			CTF_ALLOW_INTERFERENCE = Boolean.parseBoolean(CTFSettings.getProperty("CTFAllowInterference", "false"));
			CTF_ALLOW_POTIONS = Boolean.parseBoolean(CTFSettings.getProperty("CTFAllowPotions", "false"));
			CTF_ALLOW_SUMMON = Boolean.parseBoolean(CTFSettings.getProperty("CTFAllowSummon", "false"));
			CTF_ON_START_REMOVE_ALL_EFFECTS = Boolean.parseBoolean(CTFSettings.getProperty("CTFOnStartRemoveAllEffects", "true"));
			CTF_ON_START_UNSUMMON_PET = Boolean.parseBoolean(CTFSettings.getProperty("CTFOnStartUnsummonPet", "true"));
			CTF_ANNOUNCE_TEAM_STATS = Boolean.parseBoolean(CTFSettings.getProperty("CTFAnnounceTeamStats", "false"));
			CTF_ANNOUNCE_REWARD = Boolean.parseBoolean(CTFSettings.getProperty("CTFAnnounceReward", "false"));
			CTF_JOIN_CURSED = Boolean.parseBoolean(CTFSettings.getProperty("CTFJoinWithCursedWeapon", "true"));
			CTF_REVIVE_RECOVERY = Boolean.parseBoolean(CTFSettings.getProperty("CTFReviveRecovery", "false"));
			CTF_COMMAND = Boolean.parseBoolean(CTFSettings.getProperty("CTFCommand", "true"));
			CTF_AURA = Boolean.parseBoolean(CTFSettings.getProperty("CTFAura", "true"));
			
			CTF_STATS_LOGGER = Boolean.parseBoolean(CTFSettings.getProperty("CTFStatsLogger", "true"));
			
			CTF_SPAWN_OFFSET = Integer.parseInt(CTFSettings.getProperty("CTFSpawnOffset", "100"));
			
			CTF_REMOVE_BUFFS_ON_DIE = Boolean.parseBoolean(CTFSettings.getProperty("CTFRemoveBuffsOnPlayerDie", "false"));
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + EVENT_CTF_CONFIG_FILE + " File.");
		}
	}
	
	public static void loadCustomServerConfig()
	{
		try
		{
			final Properties CustomServerSettings = new Properties();
			final InputStream is = new FileInputStream(new File(OTHER_CONFIG_FILE));
			CustomServerSettings.load(is);
			is.close();
			
			/** Custom Tables **/
			CUSTOM_SPAWNLIST_TABLE = Boolean.parseBoolean(CustomServerSettings.getProperty("CustomSpawnlistTable", "true"));
			SAVE_GMSPAWN_ON_CUSTOM = Boolean.parseBoolean(CustomServerSettings.getProperty("SaveGmSpawnOnCustom", "true"));
			DELETE_GMSPAWN_ON_CUSTOM = Boolean.parseBoolean(CustomServerSettings.getProperty("DeleteGmSpawnOnCustom", "true"));
			
			ONLINE_PLAYERS_ON_LOGIN = Boolean.parseBoolean(CustomServerSettings.getProperty("OnlineOnLogin", "false"));
			
			/** Protector **/
			PROTECTOR_PLAYER_PK = Boolean.parseBoolean(CustomServerSettings.getProperty("ProtectorPlayerPK", "false"));
			PROTECTOR_PLAYER_PVP = Boolean.parseBoolean(CustomServerSettings.getProperty("ProtectorPlayerPVP", "false"));
			PROTECTOR_RADIUS_ACTION = Integer.parseInt(CustomServerSettings.getProperty("ProtectorRadiusAction", "500"));
			PROTECTOR_SKILLID = Integer.parseInt(CustomServerSettings.getProperty("ProtectorSkillId", "1069"));
			PROTECTOR_SKILLLEVEL = Integer.parseInt(CustomServerSettings.getProperty("ProtectorSkillLevel", "42"));
			PROTECTOR_SKILLTIME = Integer.parseInt(CustomServerSettings.getProperty("ProtectorSkillTime", "800"));
			PROTECTOR_MESSAGE = CustomServerSettings.getProperty("ProtectorMessage", "Protector, not spawnkilling here, go read the rules !!!");
			
			/** Donator color name **/
			DONATOR_NAME_COLOR_ENABLED = Boolean.parseBoolean(CustomServerSettings.getProperty("DonatorNameColorEnabled", "false"));
			DONATOR_NAME_COLOR = Integer.decode("0x" + CustomServerSettings.getProperty("DonatorColorName", "00FFFF"));
			DONATOR_TITLE_COLOR = Integer.decode("0x" + CustomServerSettings.getProperty("DonatorTitleColor", "00FF00"));
			DONATOR_XPSP_RATE = Float.parseFloat(CustomServerSettings.getProperty("DonatorXpSpRate", "1.5"));
			DONATOR_ADENA_RATE = Float.parseFloat(CustomServerSettings.getProperty("DonatorAdenaRate", "1.5"));
			DONATOR_DROP_RATE = Float.parseFloat(CustomServerSettings.getProperty("DonatorDropRate", "1.5"));
			DONATOR_SPOIL_RATE = Float.parseFloat(CustomServerSettings.getProperty("DonatorSpoilRate", "1.5"));
			
			/** Welcome Htm **/
			WELCOME_HTM = Boolean.parseBoolean(CustomServerSettings.getProperty("WelcomeHtm", "false"));
			
			/** Server Name **/
			ALT_SERVER_NAME_ENABLED = Boolean.parseBoolean(CustomServerSettings.getProperty("ServerNameEnabled", "false"));
			ANNOUNCE_TO_ALL_SPAWN_RB = Boolean.parseBoolean(CustomServerSettings.getProperty("AnnounceToAllSpawnRb", "false"));
			ANNOUNCE_TRY_BANNED_ACCOUNT = Boolean.parseBoolean(CustomServerSettings.getProperty("AnnounceTryBannedAccount", "false"));
			ALT_Server_Name = CustomServerSettings.getProperty("ServerName");
			DIFFERENT_Z_CHANGE_OBJECT = Integer.parseInt(CustomServerSettings.getProperty("DifferentZchangeObject", "650"));
			DIFFERENT_Z_NEW_MOVIE = Integer.parseInt(CustomServerSettings.getProperty("DifferentZnewmovie", "1000"));
			
			ALLOW_SIMPLE_STATS_VIEW = Boolean.parseBoolean(CustomServerSettings.getProperty("AllowSimpleStatsView", "true"));
			ALLOW_DETAILED_STATS_VIEW = Boolean.parseBoolean(CustomServerSettings.getProperty("AllowDetailedStatsView", "false"));
			ALLOW_ONLINE_VIEW = Boolean.parseBoolean(CustomServerSettings.getProperty("AllowOnlineView", "false"));
			
			KEEP_SUBCLASS_SKILLS = Boolean.parseBoolean(CustomServerSettings.getProperty("KeepSubClassSkills", "false"));
			
			ALLOWED_SKILLS = CustomServerSettings.getProperty("AllowedSkills", "541,542,543,544,545,546,547,548,549,550,551,552,553,554,555,556,557,558,617,618,619");
			ALLOWED_SKILLS_LIST = new ArrayList<>();
			for (String id : ALLOWED_SKILLS.trim().split(","))
			{
				ALLOWED_SKILLS_LIST.add(Integer.parseInt(id.trim()));
			}
			CASTLE_SHIELD = Boolean.parseBoolean(CustomServerSettings.getProperty("CastleShieldRestriction", "true"));
			CLANHALL_SHIELD = Boolean.parseBoolean(CustomServerSettings.getProperty("ClanHallShieldRestriction", "true"));
			APELLA_ARMORS = Boolean.parseBoolean(CustomServerSettings.getProperty("ApellaArmorsRestriction", "true"));
			OATH_ARMORS = Boolean.parseBoolean(CustomServerSettings.getProperty("OathArmorsRestriction", "true"));
			CASTLE_CROWN = Boolean.parseBoolean(CustomServerSettings.getProperty("CastleLordsCrownRestriction", "true"));
			CASTLE_CIRCLETS = Boolean.parseBoolean(CustomServerSettings.getProperty("CastleCircletsRestriction", "true"));
			CHAR_TITLE = Boolean.parseBoolean(CustomServerSettings.getProperty("CharTitle", "false"));
			ADD_CHAR_TITLE = CustomServerSettings.getProperty("CharAddTitle", "Welcome");
			
			NOBLE_CUSTOM_ITEMS = Boolean.parseBoolean(CustomServerSettings.getProperty("EnableNobleCustomItem", "true"));
			NOOBLE_CUSTOM_ITEM_ID = Integer.parseInt(CustomServerSettings.getProperty("NoobleCustomItemId", "6673"));
			HERO_CUSTOM_ITEMS = Boolean.parseBoolean(CustomServerSettings.getProperty("EnableHeroCustomItem", "true"));
			HERO_CUSTOM_ITEM_ID = Integer.parseInt(CustomServerSettings.getProperty("HeroCustomItemId", "3481"));
			HERO_CUSTOM_DAY = Integer.parseInt(CustomServerSettings.getProperty("HeroCustomDay", "0"));
			
			ALLOW_CREATE_LVL = Boolean.parseBoolean(CustomServerSettings.getProperty("CustomStartingLvl", "false"));
			CHAR_CREATE_LVL = Integer.parseInt(CustomServerSettings.getProperty("CharLvl", "80"));
			SPAWN_CHAR = Boolean.parseBoolean(CustomServerSettings.getProperty("CustomSpawn", "false"));
			SPAWN_X = Integer.parseInt(CustomServerSettings.getProperty("SpawnX", ""));
			SPAWN_Y = Integer.parseInt(CustomServerSettings.getProperty("SpawnY", ""));
			SPAWN_Z = Integer.parseInt(CustomServerSettings.getProperty("SpawnZ", ""));
			ALLOW_LOW_LEVEL_TRADE = Boolean.parseBoolean(CustomServerSettings.getProperty("AllowLowLevelTrade", "true"));
			ALLOW_HERO_SUBSKILL = Boolean.parseBoolean(CustomServerSettings.getProperty("CustomHeroSubSkill", "false"));
			HERO_COUNT = Integer.parseInt(CustomServerSettings.getProperty("HeroCount", "1"));
			CRUMA_TOWER_LEVEL_RESTRICT = Integer.parseInt(CustomServerSettings.getProperty("CrumaTowerLevelRestrict", "56"));
			ALLOW_RAID_BOSS_PETRIFIED = Boolean.parseBoolean(CustomServerSettings.getProperty("AllowRaidBossPetrified", "true"));
			ALT_PLAYER_PROTECTION_LEVEL = Integer.parseInt(CustomServerSettings.getProperty("AltPlayerProtectionLevel", "0"));
			MONSTER_RETURN_DELAY = Integer.parseInt(CustomServerSettings.getProperty("MonsterReturnDelay", "1200"));
			SCROLL_STACKABLE = Boolean.parseBoolean(CustomServerSettings.getProperty("ScrollStackable", "false"));
			ALLOW_CHAR_KILL_PROTECT = Boolean.parseBoolean(CustomServerSettings.getProperty("AllowLowLvlProtect", "false"));
			CLAN_LEADER_COLOR_ENABLED = Boolean.parseBoolean(CustomServerSettings.getProperty("ClanLeaderNameColorEnabled", "true"));
			CLAN_LEADER_COLORED = Integer.parseInt(CustomServerSettings.getProperty("ClanLeaderColored", "1"));
			CLAN_LEADER_COLOR = Integer.decode("0x" + CustomServerSettings.getProperty("ClanLeaderColor", "00FFFF"));
			CLAN_LEADER_COLOR_CLAN_LEVEL = Integer.parseInt(CustomServerSettings.getProperty("ClanLeaderColorAtClanLevel", "1"));
			SAVE_RAIDBOSS_STATUS_INTO_DB = Boolean.parseBoolean(CustomServerSettings.getProperty("SaveRBStatusIntoDB", "false"));
			DISABLE_WEIGHT_PENALTY = Boolean.parseBoolean(CustomServerSettings.getProperty("DisableWeightPenalty", "false"));
			ALLOW_FARM1_COMMAND = Boolean.parseBoolean(CustomServerSettings.getProperty("AllowFarm1Command", "false"));
			ALLOW_FARM2_COMMAND = Boolean.parseBoolean(CustomServerSettings.getProperty("AllowFarm2Command", "false"));
			ALLOW_PVP1_COMMAND = Boolean.parseBoolean(CustomServerSettings.getProperty("AllowPvP1Command", "false"));
			ALLOW_PVP2_COMMAND = Boolean.parseBoolean(CustomServerSettings.getProperty("AllowPvP2Command", "false"));
			FARM1_X = Integer.parseInt(CustomServerSettings.getProperty("farm1_X", "81304"));
			FARM1_Y = Integer.parseInt(CustomServerSettings.getProperty("farm1_Y", "14589"));
			FARM1_Z = Integer.parseInt(CustomServerSettings.getProperty("farm1_Z", "-3469"));
			PVP1_X = Integer.parseInt(CustomServerSettings.getProperty("pvp1_X", "81304"));
			PVP1_Y = Integer.parseInt(CustomServerSettings.getProperty("pvp1_Y", "14589"));
			PVP1_Z = Integer.parseInt(CustomServerSettings.getProperty("pvp1_Z", "-3469"));
			FARM2_X = Integer.parseInt(CustomServerSettings.getProperty("farm2_X", "81304"));
			FARM2_Y = Integer.parseInt(CustomServerSettings.getProperty("farm2_Y", "14589"));
			FARM2_Z = Integer.parseInt(CustomServerSettings.getProperty("farm2_Z", "-3469"));
			PVP2_X = Integer.parseInt(CustomServerSettings.getProperty("pvp2_X", "81304"));
			PVP2_Y = Integer.parseInt(CustomServerSettings.getProperty("pvp2_Y", "14589"));
			PVP2_Z = Integer.parseInt(CustomServerSettings.getProperty("pvp2_Z", "-3469"));
			FARM1_CUSTOM_MESSAGE = CustomServerSettings.getProperty("Farm1CustomMeesage", "You have been teleported to Farm Zone 1!");
			FARM2_CUSTOM_MESSAGE = CustomServerSettings.getProperty("Farm2CustomMeesage", "You have been teleported to Farm Zone 2!");
			PVP1_CUSTOM_MESSAGE = CustomServerSettings.getProperty("PvP1CustomMeesage", "You have been teleported to PvP Zone 1!");
			PVP2_CUSTOM_MESSAGE = CustomServerSettings.getProperty("PvP2CustomMeesage", "You have been teleported to PvP Zone 2!");
			
			GM_TRADE_RESTRICTED_ITEMS = Boolean.parseBoolean(CustomServerSettings.getProperty("GMTradeRestrictedItems", "false"));
			GM_RESTART_FIGHTING = Boolean.parseBoolean(CustomServerSettings.getProperty("GMRestartFighting", "false"));
			PM_MESSAGE_ON_START = Boolean.parseBoolean(CustomServerSettings.getProperty("PMWelcomeShow", "false"));
			SERVER_TIME_ON_START = Boolean.parseBoolean(CustomServerSettings.getProperty("ShowServerTimeOnStart", "false"));
			PM_SERVER_NAME = CustomServerSettings.getProperty("PMServerName", "Server");
			PM_TEXT1 = CustomServerSettings.getProperty("PMText1", "Have Fun and Nice Stay on");
			PM_TEXT2 = CustomServerSettings.getProperty("PMText2", "Vote for us every 24h");
			NEW_PLAYER_EFFECT = Boolean.parseBoolean(CustomServerSettings.getProperty("NewPlayerEffect", "true"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + OTHER_CONFIG_FILE + " File.");
		}
	}
	
	public static void loadPvpConfig()
	{
		try
		{
			final Properties pvpSettings = new Properties();
			final InputStream is = new FileInputStream(new File(PVP_CONFIG_FILE));
			pvpSettings.load(is);
			is.close();
			
			/* KARMA SYSTEM */
			KARMA_MIN_KARMA = Integer.parseInt(pvpSettings.getProperty("MinKarma", "240"));
			KARMA_MAX_KARMA = Integer.parseInt(pvpSettings.getProperty("MaxKarma", "10000"));
			KARMA_XP_DIVIDER = Integer.parseInt(pvpSettings.getProperty("XPDivider", "260"));
			KARMA_LOST_BASE = Integer.parseInt(pvpSettings.getProperty("BaseKarmaLost", "0"));
			
			KARMA_DROP_GM = Boolean.parseBoolean(pvpSettings.getProperty("CanGMDropEquipment", "false"));
			KARMA_AWARD_PK_KILL = Boolean.parseBoolean(pvpSettings.getProperty("AwardPKKillPVPPoint", "true"));
			
			KARMA_PK_LIMIT = Integer.parseInt(pvpSettings.getProperty("MinimumPKRequiredToDrop", "5"));
			
			KARMA_NONDROPPABLE_PET_ITEMS = pvpSettings.getProperty("ListOfPetItems", "2375,3500,3501,3502,4422,4423,4424,4425,6648,6649,6650");
			KARMA_NONDROPPABLE_ITEMS = pvpSettings.getProperty("ListOfNonDroppableItems", "57,1147,425,1146,461,10,2368,7,6,2370,2369,6842,6611,6612,6613,6614,6615,6616,6617,6618,6619,6620,6621");
			
			KARMA_LIST_NONDROPPABLE_PET_ITEMS = new ArrayList<>();
			for (String id : KARMA_NONDROPPABLE_PET_ITEMS.split(","))
			{
				KARMA_LIST_NONDROPPABLE_PET_ITEMS.add(Integer.parseInt(id));
			}
			
			KARMA_LIST_NONDROPPABLE_ITEMS = new ArrayList<>();
			for (String id : KARMA_NONDROPPABLE_ITEMS.split(","))
			{
				KARMA_LIST_NONDROPPABLE_ITEMS.add(Integer.parseInt(id));
			}
			
			PVP_NORMAL_TIME = Integer.parseInt(pvpSettings.getProperty("PvPVsNormalTime", "15000"));
			PVP_PVP_TIME = Integer.parseInt(pvpSettings.getProperty("PvPVsPvPTime", "30000"));
			ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE = Boolean.parseBoolean(pvpSettings.getProperty("AltKarmaPlayerCanBeKilledInPeaceZone", "false"));
			ALT_GAME_KARMA_PLAYER_CAN_SHOP = Boolean.parseBoolean(pvpSettings.getProperty("AltKarmaPlayerCanShop", "true"));
			ALT_GAME_KARMA_PLAYER_CAN_USE_GK = Boolean.parseBoolean(pvpSettings.getProperty("AltKarmaPlayerCanUseGK", "false"));
			ALT_GAME_KARMA_PLAYER_CAN_TELEPORT = Boolean.parseBoolean(pvpSettings.getProperty("AltKarmaPlayerCanTeleport", "true"));
			ALT_GAME_KARMA_PLAYER_CAN_TRADE = Boolean.parseBoolean(pvpSettings.getProperty("AltKarmaPlayerCanTrade", "true"));
			ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE = Boolean.parseBoolean(pvpSettings.getProperty("AltKarmaPlayerCanUseWareHouse", "true"));
			ALT_KARMA_TELEPORT_TO_FLORAN = Boolean.parseBoolean(pvpSettings.getProperty("AltKarmaTeleportToFloran", "true"));
			/** Custom Reword **/
			PVP_REWARD_ENABLED = Boolean.parseBoolean(pvpSettings.getProperty("PvpRewardEnabled", "false"));
			PVP_REWARD_ID = Integer.parseInt(pvpSettings.getProperty("PvpRewardItemId", "6392"));
			PVP_REWARD_AMOUNT = Integer.parseInt(pvpSettings.getProperty("PvpRewardAmmount", "1"));
			
			PK_REWARD_ENABLED = Boolean.parseBoolean(pvpSettings.getProperty("PKRewardEnabled", "false"));
			PK_REWARD_ID = Integer.parseInt(pvpSettings.getProperty("PKRewardItemId", "6392"));
			PK_REWARD_AMOUNT = Integer.parseInt(pvpSettings.getProperty("PKRewardAmmount", "1"));
			
			REWARD_PROTECT = Integer.parseInt(pvpSettings.getProperty("RewardProtect", "1"));
			
			// PVP Name Color System configs - Start
			PVP_COLOR_SYSTEM_ENABLED = Boolean.parseBoolean(pvpSettings.getProperty("EnablePvPColorSystem", "false"));
			PVP_AMOUNT1 = Integer.parseInt(pvpSettings.getProperty("PvpAmount1", "500"));
			PVP_AMOUNT2 = Integer.parseInt(pvpSettings.getProperty("PvpAmount2", "1000"));
			PVP_AMOUNT3 = Integer.parseInt(pvpSettings.getProperty("PvpAmount3", "1500"));
			PVP_AMOUNT4 = Integer.parseInt(pvpSettings.getProperty("PvpAmount4", "2500"));
			PVP_AMOUNT5 = Integer.parseInt(pvpSettings.getProperty("PvpAmount5", "5000"));
			NAME_COLOR_FOR_PVP_AMOUNT1 = Integer.decode("0x" + pvpSettings.getProperty("ColorForAmount1", "00FF00"));
			NAME_COLOR_FOR_PVP_AMOUNT2 = Integer.decode("0x" + pvpSettings.getProperty("ColorForAmount2", "00FF00"));
			NAME_COLOR_FOR_PVP_AMOUNT3 = Integer.decode("0x" + pvpSettings.getProperty("ColorForAmount3", "00FF00"));
			NAME_COLOR_FOR_PVP_AMOUNT4 = Integer.decode("0x" + pvpSettings.getProperty("ColorForAmount4", "00FF00"));
			NAME_COLOR_FOR_PVP_AMOUNT5 = Integer.decode("0x" + pvpSettings.getProperty("ColorForAmount5", "00FF00"));
			
			// PK Title Color System configs - Start
			PK_COLOR_SYSTEM_ENABLED = Boolean.parseBoolean(pvpSettings.getProperty("EnablePkColorSystem", "false"));
			PK_AMOUNT1 = Integer.parseInt(pvpSettings.getProperty("PkAmount1", "500"));
			PK_AMOUNT2 = Integer.parseInt(pvpSettings.getProperty("PkAmount2", "1000"));
			PK_AMOUNT3 = Integer.parseInt(pvpSettings.getProperty("PkAmount3", "1500"));
			PK_AMOUNT4 = Integer.parseInt(pvpSettings.getProperty("PkAmount4", "2500"));
			PK_AMOUNT5 = Integer.parseInt(pvpSettings.getProperty("PkAmount5", "5000"));
			TITLE_COLOR_FOR_PK_AMOUNT1 = Integer.decode("0x" + pvpSettings.getProperty("TitleForAmount1", "00FF00"));
			TITLE_COLOR_FOR_PK_AMOUNT2 = Integer.decode("0x" + pvpSettings.getProperty("TitleForAmount2", "00FF00"));
			TITLE_COLOR_FOR_PK_AMOUNT3 = Integer.decode("0x" + pvpSettings.getProperty("TitleForAmount3", "00FF00"));
			TITLE_COLOR_FOR_PK_AMOUNT4 = Integer.decode("0x" + pvpSettings.getProperty("TitleForAmount4", "00FF00"));
			TITLE_COLOR_FOR_PK_AMOUNT5 = Integer.decode("0x" + pvpSettings.getProperty("TitleForAmount5", "00FF00"));
			
			FLAGED_PLAYER_USE_BUFFER = Boolean.parseBoolean(pvpSettings.getProperty("AltKarmaFlagPlayerCanUseBuffer", "false"));
			
			FLAGED_PLAYER_CAN_USE_GK = Boolean.parseBoolean(pvpSettings.getProperty("FlaggedPlayerCanUseGK", "false"));
			PVPEXPSP_SYSTEM = Boolean.parseBoolean(pvpSettings.getProperty("AllowAddExpSpAtPvP", "false"));
			ADD_EXP = Integer.parseInt(pvpSettings.getProperty("AddExpAtPvp", "0"));
			ADD_SP = Integer.parseInt(pvpSettings.getProperty("AddSpAtPvp", "0"));
			ALLOW_SOE_IN_PVP = Boolean.parseBoolean(pvpSettings.getProperty("AllowSoEInPvP", "true"));
			ALLOW_POTS_IN_PVP = Boolean.parseBoolean(pvpSettings.getProperty("AllowPotsInPvP", "true"));
			/** Enable Pk Info mod. Displays number of times player has killed other */
			ENABLE_PK_INFO = Boolean.parseBoolean(pvpSettings.getProperty("EnablePkInfo", "false"));
			// Get the AnnounceAllKill, AnnouncePvpKill and AnnouncePkKill values
			ANNOUNCE_ALL_KILL = Boolean.parseBoolean(pvpSettings.getProperty("AnnounceAllKill", "false"));
			ANNOUNCE_PVP_KILL = Boolean.parseBoolean(pvpSettings.getProperty("AnnouncePvPKill", "false"));
			ANNOUNCE_PK_KILL = Boolean.parseBoolean(pvpSettings.getProperty("AnnouncePkKill", "false"));
			
			DUEL_SPAWN_X = Integer.parseInt(pvpSettings.getProperty("DuelSpawnX", "-102495"));
			DUEL_SPAWN_Y = Integer.parseInt(pvpSettings.getProperty("DuelSpawnY", "-209023"));
			DUEL_SPAWN_Z = Integer.parseInt(pvpSettings.getProperty("DuelSpawnZ", "-3326"));
			PVP_PK_TITLE = Boolean.parseBoolean(pvpSettings.getProperty("PvpPkTitle", "false"));
			PVP_TITLE_PREFIX = pvpSettings.getProperty("PvPTitlePrefix", " ");
			PK_TITLE_PREFIX = pvpSettings.getProperty("PkTitlePrefix", " | ");
			
			WAR_LEGEND_AURA = Boolean.parseBoolean(pvpSettings.getProperty("WarLegendAura", "false"));
			KILLS_TO_GET_WAR_LEGEND_AURA = Integer.parseInt(pvpSettings.getProperty("KillsToGetWarLegendAura", "30"));
			
			ANTI_FARM_ENABLED = Boolean.parseBoolean(pvpSettings.getProperty("AntiFarmEnabled", "false"));
			ANTI_FARM_CLAN_ALLY_ENABLED = Boolean.parseBoolean(pvpSettings.getProperty("AntiFarmClanAlly", "false"));
			ANTI_FARM_LVL_DIFF_ENABLED = Boolean.parseBoolean(pvpSettings.getProperty("AntiFarmLvlDiff", "false"));
			ANTI_FARM_MAX_LVL_DIFF = Integer.parseInt(pvpSettings.getProperty("AntiFarmMaxLvlDiff", "40"));
			ANTI_FARM_PDEF_DIFF_ENABLED = Boolean.parseBoolean(pvpSettings.getProperty("AntiFarmPdefDiff", "false"));
			ANTI_FARM_MAX_PDEF_DIFF = Integer.parseInt(pvpSettings.getProperty("AntiFarmMaxPdefDiff", "300"));
			ANTI_FARM_PATK_DIFF_ENABLED = Boolean.parseBoolean(pvpSettings.getProperty("AntiFarmPatkDiff", "false"));
			ANTI_FARM_MAX_PATK_DIFF = Integer.parseInt(pvpSettings.getProperty("AntiFarmMaxPatkDiff", "300"));
			ANTI_FARM_PARTY_ENABLED = Boolean.parseBoolean(pvpSettings.getProperty("AntiFarmParty", "false"));
			ANTI_FARM_IP_ENABLED = Boolean.parseBoolean(pvpSettings.getProperty("AntiFarmIP", "false"));
			ANTI_FARM_SUMMON = Boolean.parseBoolean(pvpSettings.getProperty("AntiFarmSummon", "false"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + PVP_CONFIG_FILE + " File.");
		}
	}
	
	public static void loadOlympConfig()
	{
		try
		{
			final Properties OLYMPSetting = new Properties();
			final InputStream is = new FileInputStream(new File(OLYMP_CONFIG_FILE));
			OLYMPSetting.load(is);
			is.close();
			ALT_OLY_START_TIME = Integer.parseInt(OLYMPSetting.getProperty("AltOlyStartTime", "18"));
			ALT_OLY_MIN = Integer.parseInt(OLYMPSetting.getProperty("AltOlyMin", "00"));
			ALT_OLY_CPERIOD = Long.parseLong(OLYMPSetting.getProperty("AltOlyCPeriod", "21600000"));
			ALT_OLY_BATTLE = Long.parseLong(OLYMPSetting.getProperty("AltOlyBattle", "360000"));
			ALT_OLY_WPERIOD = Long.parseLong(OLYMPSetting.getProperty("AltOlyWPeriod", "604800000"));
			ALT_OLY_VPERIOD = Long.parseLong(OLYMPSetting.getProperty("AltOlyVPeriod", "86400000"));
			ALT_OLY_CLASSED = Integer.parseInt(OLYMPSetting.getProperty("AltOlyClassedParticipants", "5"));
			ALT_OLY_NONCLASSED = Integer.parseInt(OLYMPSetting.getProperty("AltOlyNonClassedParticipants", "9"));
			ALT_OLY_BATTLE_REWARD_ITEM = Integer.parseInt(OLYMPSetting.getProperty("AltOlyBattleRewItem", "6651"));
			ALT_OLY_CLASSED_RITEM_C = Integer.parseInt(OLYMPSetting.getProperty("AltOlyClassedRewItemCount", "50"));
			ALT_OLY_NONCLASSED_RITEM_C = Integer.parseInt(OLYMPSetting.getProperty("AltOlyNonClassedRewItemCount", "30"));
			ALT_OLY_COMP_RITEM = Integer.parseInt(OLYMPSetting.getProperty("AltOlyCompRewItem", "6651"));
			ALT_OLY_GP_PER_POINT = Integer.parseInt(OLYMPSetting.getProperty("AltOlyGPPerPoint", "1000"));
			ALT_OLY_MIN_POINT_FOR_EXCH = Integer.parseInt(OLYMPSetting.getProperty("AltOlyMinPointForExchange", "50"));
			ALT_OLY_HERO_POINTS = Integer.parseInt(OLYMPSetting.getProperty("AltOlyHeroPoints", "100"));
			ALT_OLY_RESTRICTED_ITEMS = OLYMPSetting.getProperty("AltOlyRestrictedItems", "0");
			LIST_OLY_RESTRICTED_ITEMS = new ArrayList<>();
			for (String id : ALT_OLY_RESTRICTED_ITEMS.split(","))
			{
				LIST_OLY_RESTRICTED_ITEMS.add(Integer.parseInt(id));
			}
			ALLOW_EVENTS_DURING_OLY = Boolean.parseBoolean(OLYMPSetting.getProperty("AllowEventsDuringOly", "false"));
			
			ALT_OLY_RECHARGE_SKILLS = Boolean.parseBoolean(OLYMPSetting.getProperty("AltOlyRechargeSkills", "false"));
			
			/* Remove cubic at the enter of olympiad */
			REMOVE_CUBIC_OLYMPIAD = Boolean.parseBoolean(OLYMPSetting.getProperty("RemoveCubicOlympiad", "false"));
			
			ALT_OLY_NUMBER_HEROS_EACH_CLASS = Integer.parseInt(OLYMPSetting.getProperty("AltOlyNumberHerosEachClass", "1"));
			ALT_OLY_LOG_FIGHTS = Boolean.parseBoolean(OLYMPSetting.getProperty("AlyOlyLogFights", "false"));
			ALT_OLY_SHOW_MONTHLY_WINNERS = Boolean.parseBoolean(OLYMPSetting.getProperty("AltOlyShowMonthlyWinners", "true"));
			ALT_OLY_ANNOUNCE_GAMES = Boolean.parseBoolean(OLYMPSetting.getProperty("AltOlyAnnounceGames", "true"));
			LIST_OLY_RESTRICTED_SKILLS = new ArrayList<>();
			for (String id : OLYMPSetting.getProperty("AltOlyRestrictedSkills", "0").split(","))
			{
				LIST_OLY_RESTRICTED_SKILLS.add(Integer.parseInt(id));
			}
			ALT_OLY_AUGMENT_ALLOW = Boolean.parseBoolean(OLYMPSetting.getProperty("AltOlyAugmentAllow", "true"));
			ALT_OLY_TELEPORT_COUNTDOWN = Integer.parseInt(OLYMPSetting.getProperty("AltOlyTeleportCountDown", "120"));
			
			ALT_OLY_USE_CUSTOM_PERIOD_SETTINGS = Boolean.parseBoolean(OLYMPSetting.getProperty("AltOlyUseCustomPeriodSettings", "false"));
			ALT_OLY_PERIOD = OlympiadPeriod.valueOf(OLYMPSetting.getProperty("AltOlyPeriod", "MONTH"));
			ALT_OLY_PERIOD_MULTIPLIER = Integer.parseInt(OLYMPSetting.getProperty("AltOlyPeriodMultiplier", "1"));
			ALT_OLY_COMPETITION_DAYS = new ArrayList<>();
			for (String s : OLYMPSetting.getProperty("AltOlyCompetitionDays", "1,2,3,4,5,6,7").split(","))
			{
				ALT_OLY_COMPETITION_DAYS.add(Integer.parseInt(s));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + OLYMP_CONFIG_FILE + " File.");
		}
	}
	
	public static void loadEnchantConfig()
	{
		try
		{
			final Properties ENCHANTSetting = new Properties();
			final InputStream is = new FileInputStream(new File(ENCHANT_CONFIG_FILE));
			ENCHANTSetting.load(is);
			is.close();
			
			String[] propertySplit = ENCHANTSetting.getProperty("NormalWeaponEnchantLevel", "").split(";");
			for (String readData : propertySplit)
			{
				final String[] writeData = readData.split(",");
				if (writeData.length != 2)
				{
					LOGGER.info("invalid config property");
				}
				else
				{
					try
					{
						NORMAL_WEAPON_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
					}
					catch (NumberFormatException nfe)
					{
						if (!readData.equals(""))
						{
							LOGGER.info("invalid config property");
						}
					}
				}
			}
			
			propertySplit = ENCHANTSetting.getProperty("BlessWeaponEnchantLevel", "").split(";");
			for (String readData : propertySplit)
			{
				final String[] writeData = readData.split(",");
				if (writeData.length != 2)
				{
					LOGGER.info("invalid config property");
				}
				else
				{
					try
					{
						BLESS_WEAPON_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
					}
					catch (NumberFormatException nfe)
					{
						if (!readData.equals(""))
						{
							LOGGER.info("invalid config property");
						}
					}
				}
			}
			
			propertySplit = ENCHANTSetting.getProperty("CrystalWeaponEnchantLevel", "").split(";");
			for (String readData : propertySplit)
			{
				final String[] writeData = readData.split(",");
				if (writeData.length != 2)
				{
					LOGGER.info("invalid config property");
				}
				else
				{
					try
					{
						CRYSTAL_WEAPON_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
					}
					catch (NumberFormatException nfe)
					{
						if (!readData.equals(""))
						{
							LOGGER.info("invalid config property");
						}
					}
				}
			}
			
			propertySplit = ENCHANTSetting.getProperty("NormalArmorEnchantLevel", "").split(";");
			for (String readData : propertySplit)
			{
				final String[] writeData = readData.split(",");
				if (writeData.length != 2)
				{
					LOGGER.info("invalid config property");
				}
				else
				{
					try
					{
						NORMAL_ARMOR_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
					}
					catch (NumberFormatException nfe)
					{
						if (!readData.equals(""))
						{
							LOGGER.info("invalid config property");
						}
					}
				}
			}
			
			propertySplit = ENCHANTSetting.getProperty("BlessArmorEnchantLevel", "").split(";");
			for (String readData : propertySplit)
			{
				final String[] writeData = readData.split(",");
				if (writeData.length != 2)
				{
					LOGGER.info("invalid config property");
				}
				else
				{
					try
					{
						BLESS_ARMOR_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
					}
					catch (NumberFormatException nfe)
					{
						if (!readData.equals(""))
						{
							LOGGER.info("invalid config property");
						}
					}
				}
			}
			
			propertySplit = ENCHANTSetting.getProperty("CrystalArmorEnchantLevel", "").split(";");
			for (String readData : propertySplit)
			{
				final String[] writeData = readData.split(",");
				if (writeData.length != 2)
				{
					LOGGER.info("invalid config property");
				}
				else
				{
					try
					{
						CRYSTAL_ARMOR_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
					}
					catch (NumberFormatException nfe)
					{
						if (!readData.equals(""))
						{
							LOGGER.info("invalid config property");
						}
					}
				}
			}
			
			propertySplit = ENCHANTSetting.getProperty("NormalJewelryEnchantLevel", "").split(";");
			for (String readData : propertySplit)
			{
				final String[] writeData = readData.split(",");
				if (writeData.length != 2)
				{
					LOGGER.info("invalid config property");
				}
				else
				{
					try
					{
						NORMAL_JEWELRY_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
					}
					catch (NumberFormatException nfe)
					{
						if (!readData.equals(""))
						{
							LOGGER.info("invalid config property");
						}
					}
				}
			}
			
			propertySplit = ENCHANTSetting.getProperty("BlessJewelryEnchantLevel", "").split(";");
			for (String readData : propertySplit)
			{
				final String[] writeData = readData.split(",");
				if (writeData.length != 2)
				{
					LOGGER.info("invalid config property");
				}
				else
				{
					try
					{
						BLESS_JEWELRY_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
					}
					catch (NumberFormatException nfe)
					{
						if (!readData.equals(""))
						{
							LOGGER.info("invalid config property");
						}
					}
				}
			}
			
			propertySplit = ENCHANTSetting.getProperty("CrystalJewelryEnchantLevel", "").split(";");
			for (String readData : propertySplit)
			{
				final String[] writeData = readData.split(",");
				if (writeData.length != 2)
				{
					LOGGER.info("invalid config property");
				}
				else
				{
					try
					{
						CRYSTAL_JEWELRY_ENCHANT_LEVEL.put(Integer.parseInt(writeData[0]), Integer.parseInt(writeData[1]));
					}
					catch (NumberFormatException nfe)
					{
						if (!readData.equals(""))
						{
							LOGGER.info("invalid config property");
						}
					}
				}
			}
			
			/** limit of safe enchant normal **/
			ENCHANT_SAFE_MAX = Integer.parseInt(ENCHANTSetting.getProperty("EnchantSafeMax", "3"));
			
			/** limit of safe enchant full **/
			ENCHANT_SAFE_MAX_FULL = Integer.parseInt(ENCHANTSetting.getProperty("EnchantSafeMaxFull", "4"));
			
			/** limit of max enchant **/
			ENCHANT_WEAPON_MAX = Integer.parseInt(ENCHANTSetting.getProperty("EnchantWeaponMax", "25"));
			ENCHANT_ARMOR_MAX = Integer.parseInt(ENCHANTSetting.getProperty("EnchantArmorMax", "25"));
			ENCHANT_JEWELRY_MAX = Integer.parseInt(ENCHANTSetting.getProperty("EnchantJewelryMax", "25"));
			
			/** CRYSTAL SCROLL enchant limits **/
			CRYSTAL_ENCHANT_MIN = Integer.parseInt(ENCHANTSetting.getProperty("CrystalEnchantMin", "20"));
			CRYSTAL_ENCHANT_MAX = Integer.parseInt(ENCHANTSetting.getProperty("CrystalEnchantMax", "0"));
			
			/** bonus for dwarf **/
			ENABLE_DWARF_ENCHANT_BONUS = Boolean.parseBoolean(ENCHANTSetting.getProperty("EnableDwarfEnchantBonus", "false"));
			DWARF_ENCHANT_MIN_LEVEL = Integer.parseInt(ENCHANTSetting.getProperty("DwarfEnchantMinLevel", "80"));
			DWARF_ENCHANT_BONUS = Integer.parseInt(ENCHANTSetting.getProperty("DwarfEnchantBonus", "15"));
			
			/** augmentation chance **/
			AUGMENTATION_NG_SKILL_CHANCE = Integer.parseInt(ENCHANTSetting.getProperty("AugmentationNGSkillChance", "15"));
			AUGMENTATION_MID_SKILL_CHANCE = Integer.parseInt(ENCHANTSetting.getProperty("AugmentationMidSkillChance", "30"));
			AUGMENTATION_HIGH_SKILL_CHANCE = Integer.parseInt(ENCHANTSetting.getProperty("AugmentationHighSkillChance", "45"));
			AUGMENTATION_TOP_SKILL_CHANCE = Integer.parseInt(ENCHANTSetting.getProperty("AugmentationTopSkillChance", "60"));
			AUGMENTATION_BASESTAT_CHANCE = Integer.parseInt(ENCHANTSetting.getProperty("AugmentationBaseStatChance", "1"));
			
			/** augmentation glow **/
			AUGMENTATION_NG_GLOW_CHANCE = Integer.parseInt(ENCHANTSetting.getProperty("AugmentationNGGlowChance", "0"));
			AUGMENTATION_MID_GLOW_CHANCE = Integer.parseInt(ENCHANTSetting.getProperty("AugmentationMidGlowChance", "40"));
			AUGMENTATION_HIGH_GLOW_CHANCE = Integer.parseInt(ENCHANTSetting.getProperty("AugmentationHighGlowChance", "70"));
			AUGMENTATION_TOP_GLOW_CHANCE = Integer.parseInt(ENCHANTSetting.getProperty("AugmentationTopGlowChance", "100"));
			
			/** augmentation configs **/
			DELETE_AUGM_PASSIVE_ON_CHANGE = Boolean.parseBoolean(ENCHANTSetting.getProperty("DeleteAgmentPassiveEffectOnChangeWep", "true"));
			DELETE_AUGM_ACTIVE_ON_CHANGE = Boolean.parseBoolean(ENCHANTSetting.getProperty("DeleteAgmentActiveEffectOnChangeWep", "true"));
			
			/** enchant hero weapon **/
			ENCHANT_HERO_WEAPON = Boolean.parseBoolean(ENCHANTSetting.getProperty("EnableEnchantHeroWeapons", "false"));
			
			/** soul crystal **/
			SOUL_CRYSTAL_BREAK_CHANCE = Integer.parseInt(ENCHANTSetting.getProperty("SoulCrystalBreakChance", "10"));
			SOUL_CRYSTAL_LEVEL_CHANCE = Integer.parseInt(ENCHANTSetting.getProperty("SoulCrystalLevelChance", "32"));
			SOUL_CRYSTAL_MAX_LEVEL = Integer.parseInt(ENCHANTSetting.getProperty("SoulCrystalMaxLevel", "13"));
			
			/** count enchant **/
			CUSTOM_ENCHANT_VALUE = Integer.parseInt(ENCHANTSetting.getProperty("CustomEnchantValue", "1"));
			ALT_OLY_ENCHANT_LIMIT = Integer.parseInt(ENCHANTSetting.getProperty("AltOlyMaxEnchant", "-1"));
			BREAK_ENCHANT = Integer.parseInt(ENCHANTSetting.getProperty("BreakEnchant", "0"));
			
			MAX_ITEM_ENCHANT_KICK = Integer.parseInt(ENCHANTSetting.getProperty("EnchantKick", "0"));
			GM_OVER_ENCHANT = Integer.parseInt(ENCHANTSetting.getProperty("GMOverEnchant", "0"));
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + ENCHANT_CONFIG_FILE + " File.");
		}
	}
	
	public static void loadFloodConfig()
	{
		try
		{
			FLOOD_PROTECTOR_USE_ITEM = new FloodProtectorConfig("UseItemFloodProtector");
			FLOOD_PROTECTOR_ROLL_DICE = new FloodProtectorConfig("RollDiceFloodProtector");
			FLOOD_PROTECTOR_FIREWORK = new FloodProtectorConfig("FireworkFloodProtector");
			FLOOD_PROTECTOR_ITEM_PET_SUMMON = new FloodProtectorConfig("ItemPetSummonFloodProtector");
			FLOOD_PROTECTOR_HERO_VOICE = new FloodProtectorConfig("HeroVoiceFloodProtector");
			FLOOD_PROTECTOR_GLOBAL_CHAT = new FloodProtectorConfig("GlobalChatFloodProtector");
			FLOOD_PROTECTOR_SUBCLASS = new FloodProtectorConfig("SubclassFloodProtector");
			FLOOD_PROTECTOR_DROP_ITEM = new FloodProtectorConfig("DropItemFloodProtector");
			FLOOD_PROTECTOR_SERVER_BYPASS = new FloodProtectorConfig("ServerBypassFloodProtector");
			FLOOD_PROTECTOR_MULTISELL = new FloodProtectorConfig("MultiSellFloodProtector");
			FLOOD_PROTECTOR_TRANSACTION = new FloodProtectorConfig("TransactionFloodProtector");
			FLOOD_PROTECTOR_MANUFACTURE = new FloodProtectorConfig("ManufactureFloodProtector");
			FLOOD_PROTECTOR_MANOR = new FloodProtectorConfig("ManorFloodProtector");
			FLOOD_PROTECTOR_CHARACTER_SELECT = new FloodProtectorConfig("CharacterSelectFloodProtector");
			
			FLOOD_PROTECTOR_UNKNOWN_PACKETS = new FloodProtectorConfig("UnknownPacketsFloodProtector");
			FLOOD_PROTECTOR_PARTY_INVITATION = new FloodProtectorConfig("PartyInvitationFloodProtector");
			FLOOD_PROTECTOR_SAY_ACTION = new FloodProtectorConfig("SayActionFloodProtector");
			FLOOD_PROTECTOR_MOVE_ACTION = new FloodProtectorConfig("MoveActionFloodProtector");
			FLOOD_PROTECTOR_GENERIC_ACTION = new FloodProtectorConfig("GenericActionFloodProtector", true);
			FLOOD_PROTECTOR_MACRO = new FloodProtectorConfig("MacroFloodProtector", true);
			FLOOD_PROTECTOR_POTION = new FloodProtectorConfig("PotionFloodProtector", true);
			
			try
			{
				final L2Properties security = new L2Properties();
				final FileInputStream is = new FileInputStream(new File(PROTECT_FLOOD_CONFIG_FILE));
				security.load(is);
				
				loadFloodProtectorConfigs(security);
				is.close();
				
			}
			catch (Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + PROTECT_FLOOD_CONFIG_FILE);
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + PROTECT_FLOOD_CONFIG_FILE + " File.");
		}
	}
	
	/**
	 * Loads single flood protector configuration.
	 * @param properties L2Properties file reader
	 * @param config flood protector configuration instance
	 * @param configString flood protector configuration string that determines for which flood protector configuration should be read
	 * @param defaultInterval default flood protector interval
	 */
	private static void loadFloodProtectorConfig(L2Properties properties, FloodProtectorConfig config, String configString, String defaultInterval)
	{
		config.FLOOD_PROTECTION_INTERVAL = Float.parseFloat(properties.getProperty(StringUtil.concat("FloodProtector", configString, "Interval"), defaultInterval));
		config.LOG_FLOODING = Boolean.parseBoolean(properties.getProperty(StringUtil.concat("FloodProtector", configString, "LogFlooding"), "false"));
		config.PUNISHMENT_LIMIT = Integer.parseInt(properties.getProperty(StringUtil.concat("FloodProtector", configString, "PunishmentLimit"), "0"));
		config.PUNISHMENT_TYPE = properties.getProperty(StringUtil.concat("FloodProtector", configString, "PunishmentType"), "none");
		config.PUNISHMENT_TIME = Integer.parseInt(properties.getProperty(StringUtil.concat("FloodProtector", configString, "PunishmentTime"), "0"));
	}
	
	public static void loadPOtherConfig()
	{
		try
		{
			final Properties POtherSetting = new Properties();
			final InputStream is = new FileInputStream(new File(PROTECT_OTHER_CONFIG_FILE));
			POtherSetting.load(is);
			is.close();
			
			CHECK_NAME_ON_LOGIN = Boolean.parseBoolean(POtherSetting.getProperty("CheckNameOnEnter", "true"));
			CHECK_SKILLS_ON_ENTER = Boolean.parseBoolean(POtherSetting.getProperty("CheckSkillsOnEnter", "true"));
			
			/** l2walker protection **/
			L2WALKER_PROTECTION = Boolean.parseBoolean(POtherSetting.getProperty("L2WalkerProtection", "false"));
			
			/** enchant protected **/
			PROTECTED_ENCHANT = Boolean.parseBoolean(POtherSetting.getProperty("ProtectorEnchant", "false"));
			
			ONLY_GM_TELEPORT_FREE = Boolean.parseBoolean(POtherSetting.getProperty("OnlyGMTeleportFree", "false"));
			ONLY_GM_ITEMS_FREE = Boolean.parseBoolean(POtherSetting.getProperty("OnlyGMItemsFree", "false"));
			
			BYPASS_VALIDATION = Boolean.parseBoolean(POtherSetting.getProperty("BypassValidation", "true"));
			
			ALLOW_DUALBOX_OLY = Boolean.parseBoolean(POtherSetting.getProperty("AllowDualBoxInOly", "true"));
			ALLOW_DUALBOX_EVENT = Boolean.parseBoolean(POtherSetting.getProperty("AllowDualBoxInEvent", "true"));
			ALLOWED_BOXES = Integer.parseInt(POtherSetting.getProperty("AllowedBoxes", "99"));
			ALLOW_DUALBOX = Boolean.parseBoolean(POtherSetting.getProperty("AllowDualBox", "true"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + PROTECT_OTHER_CONFIG_FILE + " File.");
		}
	}
	
	public static void loadPHYSICSConfig()
	{
		try
		{
			final Properties PHYSICSSetting = new Properties();
			final InputStream is = new FileInputStream(new File(PHYSICS_CONFIG_FILE));
			PHYSICSSetting.load(is);
			is.close();
			
			ENABLE_CLASS_DAMAGES = Boolean.parseBoolean(PHYSICSSetting.getProperty("EnableClassDamagesSettings", "true"));
			ENABLE_CLASS_DAMAGES_IN_OLY = Boolean.parseBoolean(PHYSICSSetting.getProperty("EnableClassDamagesSettingsInOly", "true"));
			ENABLE_CLASS_DAMAGES_LOGGER = Boolean.parseBoolean(PHYSICSSetting.getProperty("EnableClassDamagesLogger", "true"));
			
			BLOW_ATTACK_FRONT = Integer.parseInt(PHYSICSSetting.getProperty("BlowAttackFront", "50"));
			BLOW_ATTACK_SIDE = Integer.parseInt(PHYSICSSetting.getProperty("BlowAttackSide", "60"));
			BLOW_ATTACK_BEHIND = Integer.parseInt(PHYSICSSetting.getProperty("BlowAttackBehind", "70"));
			
			BACKSTAB_ATTACK_FRONT = Integer.parseInt(PHYSICSSetting.getProperty("BackstabAttackFront", "0"));
			BACKSTAB_ATTACK_SIDE = Integer.parseInt(PHYSICSSetting.getProperty("BackstabAttackSide", "0"));
			BACKSTAB_ATTACK_BEHIND = Integer.parseInt(PHYSICSSetting.getProperty("BackstabAttackBehind", "70"));
			
			// Max patk speed and matk speed
			MAX_PATK_SPEED = Integer.parseInt(PHYSICSSetting.getProperty("MaxPAtkSpeed", "1500"));
			MAX_MATK_SPEED = Integer.parseInt(PHYSICSSetting.getProperty("MaxMAtkSpeed", "1999"));
			
			if (MAX_PATK_SPEED < 1)
			{
				MAX_PATK_SPEED = Integer.MAX_VALUE;
			}
			
			if (MAX_MATK_SPEED < 1)
			{
				MAX_MATK_SPEED = Integer.MAX_VALUE;
			}
			
			MAX_PCRIT_RATE = Integer.parseInt(PHYSICSSetting.getProperty("MaxPCritRate", "500"));
			MAX_MCRIT_RATE = Integer.parseInt(PHYSICSSetting.getProperty("MaxMCritRate", "300"));
			MCRIT_RATE_MUL = Float.parseFloat(PHYSICSSetting.getProperty("McritMulDif", "1"));
			
			MAGIC_CRITICAL_POWER = Float.parseFloat(PHYSICSSetting.getProperty("MagicCriticalPower", "3.0"));
			
			STUN_CHANCE_MODIFIER = Float.parseFloat(PHYSICSSetting.getProperty("StunChanceModifier", "1.0"));
			BLEED_CHANCE_MODIFIER = Float.parseFloat(PHYSICSSetting.getProperty("BleedChanceModifier", "1.0"));
			POISON_CHANCE_MODIFIER = Float.parseFloat(PHYSICSSetting.getProperty("PoisonChanceModifier", "1.0"));
			PARALYZE_CHANCE_MODIFIER = Float.parseFloat(PHYSICSSetting.getProperty("ParalyzeChanceModifier", "1.0"));
			ROOT_CHANCE_MODIFIER = Float.parseFloat(PHYSICSSetting.getProperty("RootChanceModifier", "1.0"));
			SLEEP_CHANCE_MODIFIER = Float.parseFloat(PHYSICSSetting.getProperty("SleepChanceModifier", "1.0"));
			FEAR_CHANCE_MODIFIER = Float.parseFloat(PHYSICSSetting.getProperty("FearChanceModifier", "1.0"));
			CONFUSION_CHANCE_MODIFIER = Float.parseFloat(PHYSICSSetting.getProperty("ConfusionChanceModifier", "1.0"));
			DEBUFF_CHANCE_MODIFIER = Float.parseFloat(PHYSICSSetting.getProperty("DebuffChanceModifier", "1.0"));
			BUFF_CHANCE_MODIFIER = Float.parseFloat(PHYSICSSetting.getProperty("BuffChanceModifier", "1.0"));
			
			ALT_MAGES_PHYSICAL_DAMAGE_MULTI = Float.parseFloat(PHYSICSSetting.getProperty("AltPDamageMages", "1.00"));
			ALT_MAGES_MAGICAL_DAMAGE_MULTI = Float.parseFloat(PHYSICSSetting.getProperty("AltMDamageMages", "1.00"));
			ALT_FIGHTERS_PHYSICAL_DAMAGE_MULTI = Float.parseFloat(PHYSICSSetting.getProperty("AltPDamageFighters", "1.00"));
			ALT_FIGHTERS_MAGICAL_DAMAGE_MULTI = Float.parseFloat(PHYSICSSetting.getProperty("AltMDamageFighters", "1.00"));
			ALT_PETS_PHYSICAL_DAMAGE_MULTI = Float.parseFloat(PHYSICSSetting.getProperty("AltPDamagePets", "1.00"));
			ALT_PETS_MAGICAL_DAMAGE_MULTI = Float.parseFloat(PHYSICSSetting.getProperty("AltMDamagePets", "1.00"));
			ALT_NPC_PHYSICAL_DAMAGE_MULTI = Float.parseFloat(PHYSICSSetting.getProperty("AltPDamageNpc", "1.00"));
			ALT_NPC_MAGICAL_DAMAGE_MULTI = Float.parseFloat(PHYSICSSetting.getProperty("AltMDamageNpc", "1.00"));
			ALT_DAGGER_DMG_VS_HEAVY = Float.parseFloat(PHYSICSSetting.getProperty("DaggerVSHeavy", "2.50"));
			ALT_DAGGER_DMG_VS_ROBE = Float.parseFloat(PHYSICSSetting.getProperty("DaggerVSRobe", "1.80"));
			ALT_DAGGER_DMG_VS_LIGHT = Float.parseFloat(PHYSICSSetting.getProperty("DaggerVSLight", "2.00"));
			RUN_SPD_BOOST = Integer.parseInt(PHYSICSSetting.getProperty("RunSpeedBoost", "0"));
			MAX_RUN_SPEED = Integer.parseInt(PHYSICSSetting.getProperty("MaxRunSpeed", "250"));
			
			ALLOW_RAID_LETHAL = Boolean.parseBoolean(PHYSICSSetting.getProperty("AllowLethalOnRaids", "false"));
			
			ALLOW_LETHAL_PROTECTION_MOBS = Boolean.parseBoolean(PHYSICSSetting.getProperty("AllowLethalProtectionMobs", "false"));
			
			LETHAL_PROTECTED_MOBS = PHYSICSSetting.getProperty("LethalProtectedMobs", "");
			
			LIST_LETHAL_PROTECTED_MOBS = new ArrayList<>();
			for (String id : LETHAL_PROTECTED_MOBS.split(","))
			{
				LIST_LETHAL_PROTECTED_MOBS.add(Integer.parseInt(id));
			}
			
			SEND_SKILLS_CHANCE_TO_PLAYERS = Boolean.parseBoolean(PHYSICSSetting.getProperty("SendSkillsChanceToPlayers", "false"));
			
			/* Remove equip during subclass change */
			REMOVE_WEAPON_SUBCLASS = Boolean.parseBoolean(PHYSICSSetting.getProperty("RemoveWeaponSubclass", "false"));
			REMOVE_CHEST_SUBCLASS = Boolean.parseBoolean(PHYSICSSetting.getProperty("RemoveChestSubclass", "false"));
			REMOVE_LEG_SUBCLASS = Boolean.parseBoolean(PHYSICSSetting.getProperty("RemoveLegSubclass", "false"));
			
			DISABLE_BOW_CLASSES_STRING = PHYSICSSetting.getProperty("DisableBowForClasses", "");
			DISABLE_BOW_CLASSES = new ArrayList<>();
			for (String class_id : DISABLE_BOW_CLASSES_STRING.split(","))
			{
				if (!class_id.equals(""))
				{
					DISABLE_BOW_CLASSES.add(Integer.parseInt(class_id));
				}
			}
			
			LEAVE_BUFFS_ON_DIE = Boolean.parseBoolean(PHYSICSSetting.getProperty("LeaveBuffsOnDie", "true"));
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + PHYSICS_CONFIG_FILE + " File.");
		}
	}
	
	public static void loadgeodataConfig()
	{
		try
		{
			final Properties geodataSetting = new Properties();
			final InputStream is = new FileInputStream(new File(GEODATA_CONFIG_FILE));
			geodataSetting.load(is);
			is.close();
			
			GEODATA_PATH = geodataSetting.getProperty("GeoDataPath", "./data/geodata/");
			COORD_SYNCHRONIZE = Integer.parseInt(geodataSetting.getProperty("CoordSynchronize", "-1"));
			
			PART_OF_CHARACTER_HEIGHT = Integer.parseInt(geodataSetting.getProperty("PartOfCharacterHeight", "75"));
			MAX_OBSTACLE_HEIGHT = Integer.parseInt(geodataSetting.getProperty("MaxObstacleHeight", "32"));
			
			PATHFINDING = Boolean.parseBoolean(geodataSetting.getProperty("PathFinding", "true"));
			PATHFIND_BUFFERS = geodataSetting.getProperty("PathFindBuffers", "100x6;128x6;192x6;256x4;320x4;384x4;500x2");
			BASE_WEIGHT = Integer.parseInt(geodataSetting.getProperty("BaseWeight", "10"));
			DIAGONAL_WEIGHT = Integer.parseInt(geodataSetting.getProperty("DiagonalWeight", "14"));
			OBSTACLE_MULTIPLIER = Integer.parseInt(geodataSetting.getProperty("ObstacleMultiplier", "10"));
			HEURISTIC_WEIGHT = Integer.parseInt(geodataSetting.getProperty("HeuristicWeight", "20"));
			MAX_ITERATIONS = Integer.parseInt(geodataSetting.getProperty("MaxIterations", "3500"));
			
			FALL_DAMAGE = Boolean.parseBoolean(geodataSetting.getProperty("FallDamage", "false"));
			ALLOW_WATER = Boolean.parseBoolean(geodataSetting.getProperty("AllowWater", "false"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + GEODATA_CONFIG_FILE + " File.");
		}
	}
	
	public static void loadBossConfig()
	{
		try
		{
			final Properties bossSettings = new Properties();
			final InputStream is = new FileInputStream(new File(RAIDBOSS_CONFIG_FILE));
			bossSettings.load(is);
			is.close();
			
			ALT_RAIDS_STATS_BONUS = Boolean.parseBoolean(bossSettings.getProperty("AltRaidsStatsBonus", "true"));
			
			RBLOCKRAGE = Integer.parseInt(bossSettings.getProperty("RBlockRage", "5000"));
			
			if ((RBLOCKRAGE > 0) && (RBLOCKRAGE < 100))
			{
				LOGGER.info("ATTENTION: RBlockRage, if enabled (>0), must be >=100");
				LOGGER.info("	-- RBlockRage setted to 100 by default");
				RBLOCKRAGE = 100;
			}
			
			RBS_SPECIFIC_LOCK_RAGE = new HashMap<>();
			
			final String RBS_SPECIFIC_LOCK_RAGE_String = bossSettings.getProperty("RaidBossesSpecificLockRage", "");
			
			if (!RBS_SPECIFIC_LOCK_RAGE_String.equals(""))
			{
				
				final String[] locked_bosses = RBS_SPECIFIC_LOCK_RAGE_String.split(";");
				
				for (String actual_boss_rage : locked_bosses)
				{
					final String[] boss_rage = actual_boss_rage.split(",");
					
					int specific_rage = Integer.parseInt(boss_rage[1]);
					
					if ((specific_rage > 0) && (specific_rage < 100))
					{
						LOGGER.info("ATTENTION: RaidBossesSpecificLockRage Value for boss " + boss_rage[0] + ", if enabled (>0), must be >=100");
						LOGGER.info("	-- RaidBossesSpecificLockRage Value for boss " + boss_rage[0] + " setted to 100 by default");
						specific_rage = 100;
					}
					
					RBS_SPECIFIC_LOCK_RAGE.put(Integer.parseInt(boss_rage[0]), specific_rage);
				}
				
			}
			
			PLAYERS_CAN_HEAL_RB = Boolean.parseBoolean(bossSettings.getProperty("PlayersCanHealRb", "true"));
			
			// ============================================================
			ALLOW_DIRECT_TP_TO_BOSS_ROOM = Boolean.parseBoolean(bossSettings.getProperty("AllowDirectTeleportToBossRoom", "false"));
			// Antharas
			ANTHARAS_OLD = Boolean.parseBoolean(bossSettings.getProperty("AntharasOldScript", "true"));
			ANTHARAS_CLOSE = Integer.parseInt(bossSettings.getProperty("AntharasClose", "1200"));
			ANTHARAS_DESPAWN_TIME = Integer.parseInt(bossSettings.getProperty("AntharasDespawnTime", "240"));
			ANTHARAS_RESP_FIRST = Integer.parseInt(bossSettings.getProperty("AntharasRespFirst", "192"));
			ANTHARAS_RESP_SECOND = Integer.parseInt(bossSettings.getProperty("AntharasRespSecond", "145"));
			ANTHARAS_WAIT_TIME = Integer.parseInt(bossSettings.getProperty("AntharasWaitTime", "30"));
			ANTHARAS_POWER_MULTIPLIER = Float.parseFloat(bossSettings.getProperty("AntharasPowerMultiplier", "1.0"));
			// ============================================================
			// Baium
			BAIUM_SLEEP = Integer.parseInt(bossSettings.getProperty("BaiumSleep", "1800"));
			BAIUM_RESP_FIRST = Integer.parseInt(bossSettings.getProperty("BaiumRespFirst", "121"));
			BAIUM_RESP_SECOND = Integer.parseInt(bossSettings.getProperty("BaiumRespSecond", "8"));
			BAIUM_POWER_MULTIPLIER = Float.parseFloat(bossSettings.getProperty("BaiumPowerMultiplier", "1.0"));
			// ============================================================
			// Core
			CORE_RESP_MINION = Integer.parseInt(bossSettings.getProperty("CoreRespMinion", "60"));
			CORE_RESP_FIRST = Integer.parseInt(bossSettings.getProperty("CoreRespFirst", "37"));
			CORE_RESP_SECOND = Integer.parseInt(bossSettings.getProperty("CoreRespSecond", "42"));
			CORE_LEVEL = Integer.parseInt(bossSettings.getProperty("CoreLevel", "0"));
			CORE_RING_CHANCE = Integer.parseInt(bossSettings.getProperty("CoreRingChance", "0"));
			CORE_POWER_MULTIPLIER = Float.parseFloat(bossSettings.getProperty("CorePowerMultiplier", "1.0"));
			// ============================================================
			// Queen Ant
			QA_RESP_NURSE = Integer.parseInt(bossSettings.getProperty("QueenAntRespNurse", "60"));
			QA_RESP_ROYAL = Integer.parseInt(bossSettings.getProperty("QueenAntRespRoyal", "120"));
			QA_RESP_FIRST = Integer.parseInt(bossSettings.getProperty("QueenAntRespFirst", "19"));
			QA_RESP_SECOND = Integer.parseInt(bossSettings.getProperty("QueenAntRespSecond", "35"));
			QA_LEVEL = Integer.parseInt(bossSettings.getProperty("QALevel", "0"));
			QA_RING_CHANCE = Integer.parseInt(bossSettings.getProperty("QARingChance", "0"));
			QA_POWER_MULTIPLIER = Float.parseFloat(bossSettings.getProperty("QueenAntPowerMultiplier", "1.0"));
			// ============================================================
			// ZAKEN
			ZAKEN_RESP_FIRST = Integer.parseInt(bossSettings.getProperty("ZakenRespFirst", "60"));
			ZAKEN_RESP_SECOND = Integer.parseInt(bossSettings.getProperty("ZakenRespSecond", "8"));
			ZAKEN_LEVEL = Integer.parseInt(bossSettings.getProperty("ZakenLevel", "0"));
			ZAKEN_EARRING_CHANCE = Integer.parseInt(bossSettings.getProperty("ZakenEarringChance", "0"));
			ZAKEN_POWER_MULTIPLIER = Float.parseFloat(bossSettings.getProperty("ZakenPowerMultiplier", "1.0"));
			// ============================================================
			// ORFEN
			ORFEN_RESP_FIRST = Integer.parseInt(bossSettings.getProperty("OrfenRespFirst", "20"));
			ORFEN_RESP_SECOND = Integer.parseInt(bossSettings.getProperty("OrfenRespSecond", "8"));
			ORFEN_LEVEL = Integer.parseInt(bossSettings.getProperty("OrfenLevel", "0"));
			ORFEN_EARRING_CHANCE = Integer.parseInt(bossSettings.getProperty("OrfenEarringChance", "0"));
			ORFEN_POWER_MULTIPLIER = Float.parseFloat(bossSettings.getProperty("OrfenPowerMultiplier", "1.0"));
			// ============================================================
			// VALAKAS
			VALAKAS_RESP_FIRST = Integer.parseInt(bossSettings.getProperty("ValakasRespFirst", "192"));
			VALAKAS_RESP_SECOND = Integer.parseInt(bossSettings.getProperty("ValakasRespSecond", "44"));
			VALAKAS_WAIT_TIME = Integer.parseInt(bossSettings.getProperty("ValakasWaitTime", "30"));
			VALAKAS_POWER_MULTIPLIER = Float.parseFloat(bossSettings.getProperty("ValakasPowerMultiplier", "1.0"));
			VALAKAS_DESPAWN_TIME = Integer.parseInt(bossSettings.getProperty("ValakasDespawnTime", "15"));
			// ============================================================
			// FRINTEZZA
			FRINTEZZA_RESP_FIRST = Integer.parseInt(bossSettings.getProperty("FrintezzaRespFirst", "48"));
			FRINTEZZA_RESP_SECOND = Integer.parseInt(bossSettings.getProperty("FrintezzaRespSecond", "8"));
			FRINTEZZA_POWER_MULTIPLIER = Float.parseFloat(bossSettings.getProperty("FrintezzaPowerMultiplier", "1.0"));
			
			BYPASS_FRINTEZZA_PARTIES_CHECK = Boolean.parseBoolean(bossSettings.getProperty("BypassPartiesCheck", "false"));
			FRINTEZZA_MIN_PARTIES = Integer.parseInt(bossSettings.getProperty("FrintezzaMinParties", "4"));
			FRINTEZZA_MAX_PARTIES = Integer.parseInt(bossSettings.getProperty("FrintezzaMaxParties", "5"));
			// ============================================================
			
			LEVEL_DIFF_MULTIPLIER_MINION = Float.parseFloat(bossSettings.getProperty("LevelDiffMultiplierMinion", "0.5"));
			
			RAID_INFO_IDS = bossSettings.getProperty("RaidInfoIDs", "");
			RAID_INFO_IDS_LIST = new ArrayList<>();
			for (String id : RAID_INFO_IDS.split(","))
			{
				RAID_INFO_IDS_LIST.add(Integer.parseInt(id));
			}
			
			// High Priestess van Halter
			HPH_FIXINTERVALOFHALTER = Integer.parseInt(bossSettings.getProperty("FixIntervalOfHalter", "172800"));
			if ((HPH_FIXINTERVALOFHALTER < 300) || (HPH_FIXINTERVALOFHALTER > 864000))
			{
				HPH_FIXINTERVALOFHALTER = 172800;
			}
			HPH_FIXINTERVALOFHALTER *= 6000;
			
			HPH_RANDOMINTERVALOFHALTER = Integer.parseInt(bossSettings.getProperty("RandomIntervalOfHalter", "86400"));
			if ((HPH_RANDOMINTERVALOFHALTER < 300) || (HPH_RANDOMINTERVALOFHALTER > 864000))
			{
				HPH_RANDOMINTERVALOFHALTER = 86400;
			}
			HPH_RANDOMINTERVALOFHALTER *= 6000;
			
			HPH_APPTIMEOFHALTER = Integer.parseInt(bossSettings.getProperty("AppTimeOfHalter", "20"));
			if ((HPH_APPTIMEOFHALTER < 5) || (HPH_APPTIMEOFHALTER > 60))
			{
				HPH_APPTIMEOFHALTER = 20;
			}
			HPH_APPTIMEOFHALTER *= 6000;
			
			HPH_ACTIVITYTIMEOFHALTER = Integer.parseInt(bossSettings.getProperty("ActivityTimeOfHalter", "21600"));
			if ((HPH_ACTIVITYTIMEOFHALTER < 7200) || (HPH_ACTIVITYTIMEOFHALTER > 86400))
			{
				HPH_ACTIVITYTIMEOFHALTER = 21600;
			}
			HPH_ACTIVITYTIMEOFHALTER *= 1000;
			
			HPH_FIGHTTIMEOFHALTER = Integer.parseInt(bossSettings.getProperty("FightTimeOfHalter", "7200"));
			if ((HPH_FIGHTTIMEOFHALTER < 7200) || (HPH_FIGHTTIMEOFHALTER > 21600))
			{
				HPH_FIGHTTIMEOFHALTER = 7200;
			}
			HPH_FIGHTTIMEOFHALTER *= 6000;
			
			HPH_CALLROYALGUARDHELPERCOUNT = Integer.parseInt(bossSettings.getProperty("CallRoyalGuardHelperCount", "6"));
			if ((HPH_CALLROYALGUARDHELPERCOUNT < 1) || (HPH_CALLROYALGUARDHELPERCOUNT > 6))
			{
				HPH_CALLROYALGUARDHELPERCOUNT = 6;
			}
			
			HPH_CALLROYALGUARDHELPERINTERVAL = Integer.parseInt(bossSettings.getProperty("CallRoyalGuardHelperInterval", "10"));
			if ((HPH_CALLROYALGUARDHELPERINTERVAL < 1) || (HPH_CALLROYALGUARDHELPERINTERVAL > 60))
			{
				HPH_CALLROYALGUARDHELPERINTERVAL = 10;
			}
			HPH_CALLROYALGUARDHELPERINTERVAL *= 6000;
			
			HPH_INTERVALOFDOOROFALTER = Integer.parseInt(bossSettings.getProperty("IntervalOfDoorOfAlter", "5400"));
			if ((HPH_INTERVALOFDOOROFALTER < 60) || (HPH_INTERVALOFDOOROFALTER > 5400))
			{
				HPH_INTERVALOFDOOROFALTER = 5400;
			}
			HPH_INTERVALOFDOOROFALTER *= 6000;
			
			HPH_TIMEOFLOCKUPDOOROFALTAR = Integer.parseInt(bossSettings.getProperty("TimeOfLockUpDoorOfAltar", "180"));
			if ((HPH_TIMEOFLOCKUPDOOROFALTAR < 60) || (HPH_TIMEOFLOCKUPDOOROFALTAR > 600))
			{
				HPH_TIMEOFLOCKUPDOOROFALTAR = 180;
			}
			HPH_TIMEOFLOCKUPDOOROFALTAR *= 6000;
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + RAIDBOSS_CONFIG_FILE + " File.");
		}
	}
	
	public static void loadCharacterConfig()
	{
		try
		{
			final L2Properties characterSettings = new L2Properties(CHARACTER_CONFIG_FILE);
			
			AUTO_LOOT = characterSettings.getProperty("AutoLoot").equalsIgnoreCase("true");
			AUTO_LOOT_HERBS = characterSettings.getProperty("AutoLootHerbs").equalsIgnoreCase("true");
			AUTO_LOOT_BOSS = characterSettings.getProperty("AutoLootBoss").equalsIgnoreCase("true");
			AUTO_LEARN_SKILLS = Boolean.parseBoolean(characterSettings.getProperty("AutoLearnSkills", "false"));
			AUTO_LEARN_DIVINE_INSPIRATION = Boolean.parseBoolean(characterSettings.getProperty("AutoLearnDivineInspiration", "false"));
			LIFE_CRYSTAL_NEEDED = Boolean.parseBoolean(characterSettings.getProperty("LifeCrystalNeeded", "true"));
			SP_BOOK_NEEDED = Boolean.parseBoolean(characterSettings.getProperty("SpBookNeeded", "true"));
			ES_SP_BOOK_NEEDED = Boolean.parseBoolean(characterSettings.getProperty("EnchantSkillSpBookNeeded", "true"));
			DIVINE_SP_BOOK_NEEDED = Boolean.parseBoolean(characterSettings.getProperty("DivineInspirationSpBookNeeded", "true"));
			ALT_GAME_SKILL_LEARN = Boolean.parseBoolean(characterSettings.getProperty("AltGameSkillLearn", "false"));
			ALLOWED_SUBCLASS = Integer.parseInt(characterSettings.getProperty("AllowedSubclass", "3"));
			BASE_SUBCLASS_LEVEL = Byte.parseByte(characterSettings.getProperty("BaseSubclassLevel", "40"));
			MAX_SUBCLASS_LEVEL = Byte.parseByte(characterSettings.getProperty("MaxSubclassLevel", "81"));
			ALT_GAME_SUBCLASS_WITHOUT_QUESTS = Boolean.parseBoolean(characterSettings.getProperty("AltSubClassWithoutQuests", "false"));
			ALT_RESTORE_EFFECTS_ON_SUBCLASS_CHANGE = Boolean.parseBoolean(characterSettings.getProperty("AltRestoreEffectOnSub", "false"));
			ALT_PARTY_RANGE = Integer.parseInt(characterSettings.getProperty("AltPartyRange", "1500"));
			ALT_WEIGHT_LIMIT = Double.parseDouble(characterSettings.getProperty("AltWeightLimit", "1"));
			ALT_GAME_DELEVEL = Boolean.parseBoolean(characterSettings.getProperty("Delevel", "true"));
			ALT_GAME_MAGICFAILURES = Boolean.parseBoolean(characterSettings.getProperty("MagicFailures", "false"));
			ALT_GAME_CANCEL_CAST = characterSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("cast") || characterSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("all");
			ALT_GAME_CANCEL_BOW = characterSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("bow") || characterSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("all");
			ALT_GAME_SHIELD_BLOCKS = Boolean.parseBoolean(characterSettings.getProperty("AltShieldBlocks", "false"));
			ALT_PERFECT_SHLD_BLOCK = Integer.parseInt(characterSettings.getProperty("AltPerfectShieldBlockRate", "10"));
			ALT_GAME_MOB_ATTACK_AI = Boolean.parseBoolean(characterSettings.getProperty("AltGameMobAttackAI", "false"));
			ALT_MOB_AGRO_IN_PEACEZONE = Boolean.parseBoolean(characterSettings.getProperty("AltMobAgroInPeaceZone", "true"));
			ALT_GAME_FREIGHTS = Boolean.parseBoolean(characterSettings.getProperty("AltGameFreights", "false"));
			ALT_GAME_FREIGHT_PRICE = Integer.parseInt(characterSettings.getProperty("AltGameFreightPrice", "1000"));
			ALT_GAME_EXPONENT_XP = Float.parseFloat(characterSettings.getProperty("AltGameExponentXp", "0."));
			ALT_GAME_EXPONENT_SP = Float.parseFloat(characterSettings.getProperty("AltGameExponentSp", "0."));
			ALT_GAME_TIREDNESS = Boolean.parseBoolean(characterSettings.getProperty("AltGameTiredness", "false"));
			ALT_GAME_FREE_TELEPORT = Boolean.parseBoolean(characterSettings.getProperty("AltFreeTeleporting", "false"));
			ALT_RECOMMEND = Boolean.parseBoolean(characterSettings.getProperty("AltRecommend", "false"));
			ALT_RECOMMENDATIONS_NUMBER = Integer.parseInt(characterSettings.getProperty("AltMaxRecommendationNumber", "255"));
			MAX_CHARACTERS_NUMBER_PER_ACCOUNT = Integer.parseInt(characterSettings.getProperty("CharMaxNumber", "0"));
			MAX_LEVEL_NEWBIE = Integer.parseInt(characterSettings.getProperty("MaxLevelNewbie", "20"));
			MAX_LEVEL_NEWBIE_STATUS = Integer.parseInt(characterSettings.getProperty("MaxLevelNewbieStatus", "40"));
			DISABLE_TUTORIAL = Boolean.parseBoolean(characterSettings.getProperty("DisableTutorial", "false"));
			STARTING_ADENA = Integer.parseInt(characterSettings.getProperty("StartingAdena", "100"));
			STARTING_AA = Integer.parseInt(characterSettings.getProperty("StartingAncientAdena", "0"));
			CUSTOM_STARTER_ITEMS_ENABLED = Boolean.parseBoolean(characterSettings.getProperty("CustomStarterItemsEnabled", "false"));
			if (CUSTOM_STARTER_ITEMS_ENABLED)
			{
				STARTING_CUSTOM_ITEMS_M.clear();
				String[] propertySplit = characterSettings.getProperty("StartingCustomItemsMage", "57,0").split(";");
				for (String reward : propertySplit)
				{
					final String[] rewardSplit = reward.split(",");
					if (rewardSplit.length != 2)
					{
						LOGGER.warning("StartingCustomItemsMage[Config.load()]: invalid config property -> StartingCustomItemsMage \"" + reward + "\"");
					}
					else
					{
						try
						{
							STARTING_CUSTOM_ITEMS_M.add(new int[]
							{
								Integer.parseInt(rewardSplit[0]),
								Integer.parseInt(rewardSplit[1])
							});
						}
						catch (NumberFormatException nfe)
						{
							if (!reward.isEmpty())
							{
								LOGGER.warning("StartingCustomItemsMage[Config.load()]: invalid config property -> StartingCustomItemsMage \"" + reward + "\"");
							}
						}
					}
				}
				
				STARTING_CUSTOM_ITEMS_F.clear();
				propertySplit = characterSettings.getProperty("StartingCustomItemsFighter", "57,0").split(";");
				for (String reward : propertySplit)
				{
					final String[] rewardSplit = reward.split(",");
					if (rewardSplit.length != 2)
					{
						LOGGER.warning("StartingCustomItemsFighter[Config.load()]: invalid config property -> StartingCustomItemsFighter \"" + reward + "\"");
					}
					else
					{
						try
						{
							STARTING_CUSTOM_ITEMS_F.add(new int[]
							{
								Integer.parseInt(rewardSplit[0]),
								Integer.parseInt(rewardSplit[1])
							});
						}
						catch (NumberFormatException nfe)
						{
							if (!reward.isEmpty())
							{
								LOGGER.warning("StartingCustomItemsFighter[Config.load()]: invalid config property -> StartingCustomItemsFighter \"" + reward + "\"");
							}
						}
					}
				}
			}
			INVENTORY_MAXIMUM_NO_DWARF = Integer.parseInt(characterSettings.getProperty("MaximumSlotsForNoDwarf", "80"));
			INVENTORY_MAXIMUM_DWARF = Integer.parseInt(characterSettings.getProperty("MaximumSlotsForDwarf", "100"));
			INVENTORY_MAXIMUM_GM = Integer.parseInt(characterSettings.getProperty("MaximumSlotsForGMPlayer", "250"));
			MAX_ITEM_IN_PACKET = Math.max(INVENTORY_MAXIMUM_NO_DWARF, Math.max(INVENTORY_MAXIMUM_DWARF, INVENTORY_MAXIMUM_GM));
			WAREHOUSE_SLOTS_DWARF = Integer.parseInt(characterSettings.getProperty("MaximumWarehouseSlotsForDwarf", "120"));
			WAREHOUSE_SLOTS_NO_DWARF = Integer.parseInt(characterSettings.getProperty("MaximumWarehouseSlotsForNoDwarf", "100"));
			WAREHOUSE_SLOTS_CLAN = Integer.parseInt(characterSettings.getProperty("MaximumWarehouseSlotsForClan", "150"));
			FREIGHT_SLOTS = Integer.parseInt(characterSettings.getProperty("MaximumFreightSlots", "20"));
			MAX_PVTSTORE_SLOTS_DWARF = Integer.parseInt(characterSettings.getProperty("MaxPvtStoreSlotsDwarf", "5"));
			MAX_PVTSTORE_SLOTS_OTHER = Integer.parseInt(characterSettings.getProperty("MaxPvtStoreSlotsOther", "4"));
			HP_REGEN_MULTIPLIER = Double.parseDouble(characterSettings.getProperty("HpRegenMultiplier", "100")) / 100;
			MP_REGEN_MULTIPLIER = Double.parseDouble(characterSettings.getProperty("MpRegenMultiplier", "100")) / 100;
			CP_REGEN_MULTIPLIER = Double.parseDouble(characterSettings.getProperty("CpRegenMultiplier", "100")) / 100;
			ENABLE_KEYBOARD_MOVEMENT = Boolean.parseBoolean(characterSettings.getProperty("KeyboardMovement", "true"));
			UNSTUCK_INTERVAL = Integer.parseInt(characterSettings.getProperty("UnstuckInterval", "300"));
			PLAYER_SPAWN_PROTECTION = Integer.parseInt(characterSettings.getProperty("PlayerSpawnProtection", "0"));
			PLAYER_TELEPORT_PROTECTION = Integer.parseInt(characterSettings.getProperty("PlayerTeleportProtection", "0"));
			PLAYER_FAKEDEATH_UP_PROTECTION = Integer.parseInt(characterSettings.getProperty("PlayerFakeDeathUpProtection", "0"));
			DEEPBLUE_DROP_RULES = Boolean.parseBoolean(characterSettings.getProperty("UseDeepBlueDropRules", "true"));
			PARTY_XP_CUTOFF_METHOD = characterSettings.getProperty("PartyXpCutoffMethod", "percentage");
			PARTY_XP_CUTOFF_PERCENT = Double.parseDouble(characterSettings.getProperty("PartyXpCutoffPercent", "3."));
			PARTY_XP_CUTOFF_LEVEL = Integer.parseInt(characterSettings.getProperty("PartyXpCutoffLevel", "30"));
			RESPAWN_RESTORE_CP = Double.parseDouble(characterSettings.getProperty("RespawnRestoreCP", "0")) / 100;
			RESPAWN_RESTORE_HP = Double.parseDouble(characterSettings.getProperty("RespawnRestoreHP", "70")) / 100;
			RESPAWN_RESTORE_MP = Double.parseDouble(characterSettings.getProperty("RespawnRestoreMP", "70")) / 100;
			RESPAWN_RANDOM_ENABLED = Boolean.parseBoolean(characterSettings.getProperty("RespawnRandomInTown", "false"));
			RESPAWN_RANDOM_MAX_OFFSET = Integer.parseInt(characterSettings.getProperty("RespawnRandomMaxOffset", "50"));
			PETITIONING_ALLOWED = Boolean.parseBoolean(characterSettings.getProperty("PetitioningAllowed", "true"));
			MAX_PETITIONS_PER_PLAYER = Integer.parseInt(characterSettings.getProperty("MaxPetitionsPerPlayer", "5"));
			MAX_PETITIONS_PENDING = Integer.parseInt(characterSettings.getProperty("MaxPetitionsPending", "25"));
			DEATH_PENALTY_CHANCE = Integer.parseInt(characterSettings.getProperty("DeathPenaltyChance", "20"));
			EFFECT_CANCELING = Boolean.parseBoolean(characterSettings.getProperty("CancelLesserEffect", "true"));
			STORE_SKILL_COOLTIME = Boolean.parseBoolean(characterSettings.getProperty("StoreSkillCooltime", "true"));
			BUFFS_MAX_AMOUNT = Byte.parseByte(characterSettings.getProperty("MaxBuffAmount", "24"));
			DEBUFFS_MAX_AMOUNT = Byte.parseByte(characterSettings.getProperty("MaxDebuffAmount", "6"));
			ENABLE_MODIFY_SKILL_DURATION = Boolean.parseBoolean(characterSettings.getProperty("EnableModifySkillDuration", "false"));
			if (ENABLE_MODIFY_SKILL_DURATION)
			{
				SKILL_DURATION_LIST = new HashMap<>();
				
				String[] propertySplit;
				propertySplit = characterSettings.getProperty("SkillDurationList", "").split(";");
				
				for (String skill : propertySplit)
				{
					final String[] skillSplit = skill.split(",");
					if (skillSplit.length != 2)
					{
						LOGGER.info("[SkillDurationList]: invalid config property -> SkillDurationList \"" + skill + "\"");
					}
					else
					{
						try
						{
							SKILL_DURATION_LIST.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
						}
						catch (NumberFormatException nfe)
						{
							if (!skill.equals(""))
							{
								LOGGER.info("[SkillDurationList]: invalid config property -> SkillList \"" + skillSplit[0] + "\"" + skillSplit[1]);
							}
						}
					}
				}
			}
			ALLOW_CLASS_MASTERS = Boolean.parseBoolean(characterSettings.getProperty("AllowClassMasters", "false"));
			CLASS_MASTER_STRIDER_UPDATE = Boolean.parseBoolean(characterSettings.getProperty("AllowClassMastersStriderUpdate", "false"));
			ALLOW_CLASS_MASTERS_FIRST_CLASS = Boolean.parseBoolean(characterSettings.getProperty("AllowClassMastersFirstClass", "true"));
			ALLOW_CLASS_MASTERS_SECOND_CLASS = Boolean.parseBoolean(characterSettings.getProperty("AllowClassMastersSecondClass", "true"));
			ALLOW_CLASS_MASTERS_THIRD_CLASS = Boolean.parseBoolean(characterSettings.getProperty("AllowClassMastersThirdClass", "true"));
			CLASS_MASTER_SETTINGS = new ClassMasterSettings(characterSettings.getProperty("ConfigClassMaster"));
			ALLOW_REMOTE_CLASS_MASTERS = Boolean.parseBoolean(characterSettings.getProperty("AllowRemoteClassMasters", "false"));
		}
		catch (Exception e)
		{
			LOGGER.warning("Failed to load " + CHARACTER_CONFIG_FILE + " file.");
		}
	}
	
	public static void loadDaemonsConf()
	{
		try
		{
			final L2Properties daemonsSettings = new L2Properties(DAEMONS_CONFIG_FILE);
			
			AUTOSAVE_INITIAL_TIME = Long.parseLong(daemonsSettings.getProperty("AutoSaveInitial", "300000"));
			AUTOSAVE_DELAY_TIME = Long.parseLong(daemonsSettings.getProperty("AutoSaveDelay", "900000"));
			CHECK_CONNECTION_INITIAL_TIME = Long.parseLong(daemonsSettings.getProperty("CheckConnectionInitial", "300000"));
			CHECK_CONNECTION_DELAY_TIME = Long.parseLong(daemonsSettings.getProperty("CheckConnectionDelay", "900000"));
			CHECK_CONNECTION_INACTIVITY_TIME = Long.parseLong(daemonsSettings.getProperty("CheckConnectionInactivityTime", "90000"));
			CLEANDB_INITIAL_TIME = Long.parseLong(daemonsSettings.getProperty("CleanDBInitial", "300000"));
			CLEANDB_DELAY_TIME = Long.parseLong(daemonsSettings.getProperty("CleanDBDelay", "900000"));
			CHECK_TELEPORT_ZOMBIE_DELAY_TIME = Long.parseLong(daemonsSettings.getProperty("CheckTeleportZombiesDelay", "90000"));
			DEADLOCKCHECK_INTIAL_TIME = Long.parseLong(daemonsSettings.getProperty("DeadLockCheck", "0"));
			DEADLOCKCHECK_DELAY_TIME = Long.parseLong(daemonsSettings.getProperty("DeadLockDelay", "0"));
		}
		catch (Exception e)
		{
			LOGGER.warning("Failed to load " + DAEMONS_CONFIG_FILE + " file.");
		}
	}
	
	/**
	 * Loads all Filter Words
	 */
	public static void loadFilter()
	{
		LineNumberReader lnr = null;
		try
		{
			final File filter_file = new File(FILTER_FILE);
			if (!filter_file.exists())
			{
				return;
			}
			
			lnr = new LineNumberReader(new BufferedReader(new FileReader(filter_file)));
			String line = null;
			while ((line = lnr.readLine()) != null)
			{
				if ((line.trim().length() == 0) || line.startsWith("#"))
				{
					continue;
				}
				FILTER_LIST.add(line.trim());
			}
			LOGGER.info("Loaded " + FILTER_LIST.size() + " Filter Words.");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + FILTER_FILE + " File.");
		}
		finally
		{
			if (lnr != null)
			{
				try
				{
					lnr.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void loadHexed()
	{
		try
		{
			final Properties Settings = new Properties();
			final InputStream is = new FileInputStream(new File(HEXID_FILE));
			Settings.load(is);
			is.close();
			SERVER_ID = Integer.parseInt(Settings.getProperty("ServerID"));
			HEX_ID = new BigInteger(Settings.getProperty("HexID"), 16).toByteArray();
		}
		catch (Exception e)
		{
			LOGGER.warning("Could not load HexID file (" + HEXID_FILE + "). Hopefully login will give us one.");
		}
	}
	
	public static void loadLoginStartConfig()
	{
		try
		{
			final Properties serverSettings = new Properties();
			final InputStream is = new FileInputStream(new File(LOGIN_CONFIG_FILE));
			serverSettings.load(is);
			is.close();
			
			GAME_SERVER_LOGIN_HOST = serverSettings.getProperty("LoginHostname", "*");
			GAME_SERVER_LOGIN_PORT = Integer.parseInt(serverSettings.getProperty("LoginPort", "9013"));
			
			LOGIN_BIND_ADDRESS = serverSettings.getProperty("LoginserverHostname", "*");
			PORT_LOGIN = Integer.parseInt(serverSettings.getProperty("LoginserverPort", "2106"));
			
			ACCEPT_NEW_GAMESERVER = Boolean.parseBoolean(serverSettings.getProperty("AcceptNewGameServer", "true"));
			
			LOGIN_TRY_BEFORE_BAN = Integer.parseInt(serverSettings.getProperty("LoginTryBeforeBan", "10"));
			LOGIN_BLOCK_AFTER_BAN = Integer.parseInt(serverSettings.getProperty("LoginBlockAfterBan", "600"));
			
			INTERNAL_HOSTNAME = serverSettings.getProperty("InternalHostname", "localhost");
			EXTERNAL_HOSTNAME = serverSettings.getProperty("ExternalHostname", "localhost");
			
			DATABASE_DRIVER = serverSettings.getProperty("Driver", "org.mariadb.jdbc.Driver");
			DATABASE_URL = serverSettings.getProperty("URL", "jdbc:mariadb://localhost/l2jdb");
			DATABASE_LOGIN = serverSettings.getProperty("Login", "root");
			DATABASE_PASSWORD = serverSettings.getProperty("Password", "");
			DATABASE_MAX_CONNECTIONS = Integer.parseInt(serverSettings.getProperty("MaximumDbConnections", "10"));
			
			BACKUP_DATABASE = Boolean.parseBoolean(serverSettings.getProperty("BackupDatabase", "false"));
			MYSQL_BIN_PATH = serverSettings.getProperty("MySqlBinLocation", "C:/xampp/mysql/bin/");
			BACKUP_PATH = serverSettings.getProperty("BackupPath", "../backup/");
			BACKUP_DAYS = Integer.parseInt(serverSettings.getProperty("BackupDays", "30"));
			
			// Anti Brute force attack on login
			BRUT_AVG_TIME = Integer.parseInt(serverSettings.getProperty("BrutAvgTime", "30")); // in Seconds
			BRUT_LOGON_ATTEMPTS = Integer.parseInt(serverSettings.getProperty("BrutLogonAttempts", "15"));
			BRUT_BAN_IP_TIME = Integer.parseInt(serverSettings.getProperty("BrutBanIpTime", "900")); // in Seconds
			
			SHOW_LICENCE = Boolean.parseBoolean(serverSettings.getProperty("ShowLicence", "false"));
			IP_UPDATE_TIME = Integer.parseInt(serverSettings.getProperty("IpUpdateTime", "15"));
			FORCE_GGAUTH = Boolean.parseBoolean(serverSettings.getProperty("ForceGGAuth", "false"));
			
			AUTO_CREATE_ACCOUNTS = Boolean.parseBoolean(serverSettings.getProperty("AutoCreateAccounts", "true"));
			
			FLOOD_PROTECTION = Boolean.parseBoolean(serverSettings.getProperty("EnableFloodProtection", "true"));
			FAST_CONNECTION_LIMIT = Integer.parseInt(serverSettings.getProperty("FastConnectionLimit", "15"));
			NORMAL_CONNECTION_TIME = Integer.parseInt(serverSettings.getProperty("NormalConnectionTime", "700"));
			FAST_CONNECTION_TIME = Integer.parseInt(serverSettings.getProperty("FastConnectionTime", "350"));
			MAX_CONNECTION_PER_IP = Integer.parseInt(serverSettings.getProperty("MaxConnectionPerIP", "50"));
			
			NETWORK_IP_LIST = serverSettings.getProperty("NetworkList", "");
			SESSION_TTL = Long.parseLong(serverSettings.getProperty("SessionTTL", "25000"));
			MAX_LOGINSESSIONS = Integer.parseInt(serverSettings.getProperty("MaxSessions", "200"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("Failed to Load " + LOGIN_CONFIG_FILE + " File.");
		}
	}
	
	public static void loadBanFile()
	{
		File conf_file = new File(BANNED_IP_FILE);
		if (!conf_file.exists())
		{
			// old file position
			conf_file = new File(LEGACY_BANNED_IP);
		}
		
		if (conf_file.exists() && conf_file.isFile())
		{
			FileInputStream fis = null;
			try
			{
				fis = new FileInputStream(conf_file);
				
				LineNumberReader reader = null;
				String line;
				String[] parts;
				try
				{
					reader = new LineNumberReader(new InputStreamReader(fis));
					
					while ((line = reader.readLine()) != null)
					{
						line = line.trim();
						// check if this line isnt a comment line
						if ((line.length() > 0) && (line.charAt(0) != '#'))
						{
							// split comments if any
							parts = line.split("#", 2);
							
							// discard comments in the line, if any
							line = parts[0];
							
							parts = line.split(" ");
							
							final String address = parts[0];
							
							long duration = 0;
							
							if (parts.length > 1)
							{
								try
								{
									duration = Long.parseLong(parts[1]);
								}
								catch (NumberFormatException e)
								{
									LOGGER.warning("Skipped: Incorrect ban duration (" + parts[1] + ") on (" + conf_file.getName() + "). Line: " + reader.getLineNumber());
									continue;
								}
							}
							
							try
							{
								LoginController.getInstance().addBanForAddress(address, duration);
							}
							catch (UnknownHostException e)
							{
								LOGGER.warning("Skipped: Invalid address (" + parts[0] + ") on (" + conf_file.getName() + "). Line: " + reader.getLineNumber());
							}
						}
					}
				}
				catch (IOException e)
				{
					LOGGER.warning("Error while reading the bans file (" + conf_file.getName() + "). Details: " + e);
				}
				
				LOGGER.info("Loaded " + LoginController.getInstance().getBannedIps().size() + " IP Bans.");
			}
			catch (FileNotFoundException e)
			{
				LOGGER.warning("Failed to load banned IPs file (" + conf_file.getName() + ") for reading. Reason: " + e);
			}
			finally
			{
				if (fis != null)
				{
					try
					{
						fis.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		else
		{
			LOGGER.info("IP Bans file (" + conf_file.getName() + ") is missing or is a directory, skipped.");
		}
	}
	
	public static void saveHexid(int serverId, String string)
	{
		saveHexid(serverId, string, HEXID_FILE);
	}
	
	public static void saveHexid(int serverId, String hexId, String fileName)
	{
		OutputStream out = null;
		try
		{
			final Properties hexSetting = new Properties();
			final File file = new File(fileName);
			if (file.createNewFile())
			{
				out = new FileOutputStream(file);
				hexSetting.setProperty("ServerID", String.valueOf(serverId));
				hexSetting.setProperty("HexID", hexId);
				hexSetting.store(out, "the hexID to auth into login");
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("Failed to save hex id to " + fileName + " File.");
			e.printStackTrace();
		}
		finally
		{
			
			if (out != null)
			{
				try
				{
					out.close();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			
		}
	}
	
	public static void unallocateFilterBuffer()
	{
		LOGGER.info("Cleaning Chat Filter..");
		FILTER_LIST.clear();
	}
	
	/**
	 * Loads flood protector configurations.
	 * @param properties
	 */
	private static void loadFloodProtectorConfigs(L2Properties properties)
	{
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_USE_ITEM, "UseItem", "1");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_ROLL_DICE, "RollDice", "42");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_FIREWORK, "Firework", "42");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_ITEM_PET_SUMMON, "ItemPetSummon", "16");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_HERO_VOICE, "HeroVoice", "100");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_GLOBAL_CHAT, "GlobalChat", "5");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_SUBCLASS, "Subclass", "20");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_DROP_ITEM, "DropItem", "10");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_SERVER_BYPASS, "ServerBypass", "5");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_MULTISELL, "MultiSell", "1");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_TRANSACTION, "Transaction", "10");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_MANUFACTURE, "Manufacture", "3");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_MANOR, "Manor", "30");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_CHARACTER_SELECT, "CharacterSelect", "30");
		
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_UNKNOWN_PACKETS, "UnknownPackets", "5");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_PARTY_INVITATION, "PartyInvitation", "30");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_SAY_ACTION, "SayAction", "100");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_MOVE_ACTION, "MoveAction", "30");
		
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_GENERIC_ACTION, "GenericAction", "5");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_MACRO, "Macro", "10");
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_POTION, "Potion", "4");
	}
	
	public static void load(ServerMode serverMode)
	{
		SERVER_MODE = serverMode;
		if (SERVER_MODE == ServerMode.GAME)
		{
			loadHexed();
			
			// Load network
			loadServerConfig();
			
			// Load system
			loadIdFactoryConfig();
			
			// Head
			loadRatesConfig();
			loadCharacterConfig();
			loadGeneralConfig();
			load7sConfig();
			loadCHConfig();
			loadElitCHConfig();
			loadOlympConfig();
			loadEnchantConfig();
			loadBossConfig();
			
			// Head functions
			loadCustomServerConfig();
			loadPHYSICSConfig();
			loadAccessConfig();
			loadPvpConfig();
			loadCraftConfig();
			
			// Event config
			loadCTFConfig();
			loadDMConfig();
			loadTVTConfig();
			loadTWConfig();
			
			// Protect
			loadFloodConfig();
			loadPOtherConfig();
			
			// Geo&path
			loadgeodataConfig();
			
			// Custom
			loadChampionConfig();
			loadMerchantZeroPriceConfig();
			loadWeddingConfig();
			loadREBIRTHConfig();
			loadAWAYConfig();
			loadBankingConfig();
			loadBufferConfig();
			loadPCBPointConfig();
			loadOfflineConfig();
			
			// Other
			loadDaemonsConf();
			
			if (USE_SAY_FILTER)
			{
				loadFilter();
			}
			
			loadTelnetConfig();
		}
		else if (SERVER_MODE == ServerMode.LOGIN)
		{
			loadLoginStartConfig();
			
			loadTelnetConfig();
		}
		else
		{
			LOGGER.severe("Could not Load Config: server mode was not set.");
		}
	}
}
