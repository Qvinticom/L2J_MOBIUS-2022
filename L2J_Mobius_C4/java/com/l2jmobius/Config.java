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
package com.l2jmobius;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.base.Experience;
import com.l2jmobius.gameserver.util.FloodProtectorConfig;
import com.l2jmobius.util.StringUtil;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import javolution.util.FastList;
import javolution.util.FastMap;

/**
 * This class contains global server configuration.<br>
 * It has static final fields initialized from configuration files.<br>
 * It's initialized at the very begin of startup, and later JIT will optimize away debug/unused code.
 * @author mkizub
 */
public final class Config
{
	protected static Logger _log = Logger.getLogger(Config.class.getName());
	
	public static final String EOL = System.lineSeparator();
	
	/** Debug/release mode */
	public static boolean DEBUG;
	/** Enable/disable assertions */
	public static boolean ASSERT;
	/** Enable/disable code 'in progress' */
	public static boolean DEVELOPER;
	
	/** Set if this server is a test server used for development */
	public static boolean TEST_SERVER;
	
	/** Game Server ports */
	public static int PORT_GAME;
	/** Login Server port */
	public static int PORT_LOGIN;
	/** Login Server bind ip */
	public static String LOGIN_BIND_ADDRESS;
	/** Number of tries of login before ban */
	public static int LOGIN_TRY_BEFORE_BAN;
	/** Number of seconds the IP ban will last, default 10 minutes */
	public static int LOGIN_BLOCK_AFTER_BAN;
	
	/** Hostname of the Game Server */
	public static String GAMESERVER_HOSTNAME;
	
	// Access to database
	/** Driver to access to database */
	public static String DATABASE_DRIVER;
	/** Path to access to database */
	public static String DATABASE_URL;
	/** Database login */
	public static String DATABASE_LOGIN;
	/** Database password */
	public static String DATABASE_PASSWORD;
	/** Maximum number of connections to the database */
	public static int DATABASE_MAX_CONNECTIONS;
	/** Time until a connection is closed */
	public static long CONNECTION_CLOSE_TIME;
	
	/** Maximum number of players allowed to play simultaneously on server */
	public static int MAXIMUM_ONLINE_USERS;
	
	// Setting for serverList
	/** Displays [] in front of server name ? */
	public static boolean SERVER_LIST_BRACKET;
	/** Displays a clock next to the server name ? */
	public static boolean SERVER_LIST_CLOCK;
	/** Display test server in the list of servers ? */
	public static boolean SERVER_LIST_TESTSERVER;
	/** Set the server as gm only at startup ? */
	public static boolean SERVER_GMONLY;
	
	// Thread pools size
	/** Thread pool size effect */
	public static int THREAD_P_EFFECTS;
	/** Thread pool size general */
	public static int THREAD_P_GENERAL;
	/** Packet max thread */
	public static int GENERAL_PACKET_THREAD_CORE_SIZE;
	public static int IO_PACKET_THREAD_CORE_SIZE;
	/** General max thread */
	public static int GENERAL_THREAD_CORE_SIZE;
	/** AI max thread */
	public static int AI_MAX_THREAD;
	
	/** Accept auto-loot ? */
	public static boolean AUTO_LOOT;
	/** Accept auto-loot for RBs ? */
	public static boolean AUTO_LOOT_RAIDS;
	
	/** Character name template */
	public static String CNAME_TEMPLATE;
	/** Pet name template */
	public static String PET_NAME_TEMPLATE;
	/** Maximum number of characters per account */
	public static int MAX_CHARACTERS_NUMBER_PER_ACCOUNT;
	
	/** Global chat state */
	public static String DEFAULT_GLOBAL_CHAT;
	/** Trade chat state */
	public static String DEFAULT_TRADE_CHAT;
	/** For test servers - everybody has admin rights */
	public static boolean EVERYBODY_HAS_ADMIN_RIGHTS;
	/** Alternative game crafting */
	public static boolean ALT_GAME_CREATION;
	/** Alternative game crafting speed mutiplier - default 0 (fastest but still not instant) */
	public static double ALT_GAME_CREATION_SPEED;
	/** Alternative game crafting XP rate multiplier - default 1 */
	public static double ALT_GAME_CREATION_XP_RATE;
	/** Alternative game crafting SP rate multiplier - default 1 */
	public static double ALT_GAME_CREATION_SP_RATE;
	/** Check if skills learned by a character are legal */
	public static boolean SKILL_CHECK_ENABLE;
	
	/** Enable modifying skill duration */
	public static boolean ENABLE_MODIFY_SKILL_DURATION;
	/** Skill duration list */
	public static Map<Integer, Integer> SKILL_DURATION_LIST;
	/** Block exp/sp command */
	public static boolean Boost_EXP_COMMAND;
	/** Enable Auto NPC target */
	public static boolean AUTO_TARGET_NPC;
	/** Enable Real Time */
	public static boolean ENABLE_REAL_TIME;
	
	/** Alternative game skill learning */
	public static boolean ALT_GAME_SKILL_LEARN;
	/** Alternative auto skill learning */
	public static boolean AUTO_LEARN_SKILLS;
	/** Alternative auto skill learning for 3rd class */
	public static boolean AUTO_LEARN_3RD_SKILLS;
	/** Cancel attack bow by hit */
	public static boolean ALT_GAME_CANCEL_BOW;
	/** Cancel cast by hit */
	public static boolean ALT_GAME_CANCEL_CAST;
	
	/** Alternative game - use tiredness, instead of CP */
	public static boolean ALT_GAME_TIREDNESS;
	
	/** Party Range */
	public static int ALT_PARTY_RANGE;
	public static int ALT_PARTY_RANGE2;
	
	/** Alternative Perfect shield defense rate */
	public static int ALT_PERFECT_SHLD_BLOCK;
	
	/** Alternative mob aggro in peaceful zone */
	public static boolean ALT_MOB_AGGRO_IN_PEACEZONE;
	
	/** Alternative freight modes - Freights can be withdrawed from any village */
	public static boolean ALT_GAME_FREIGHTS;
	/** Alternative freight modes - Sets the price value for each freightened item */
	public static int ALT_GAME_FREIGHT_PRICE;
	
	/** Alternative gaming - loss of XP on death */
	public static boolean ALT_GAME_DELEVEL;
	
	/** Alternative Weight Limit */
	public static double ALT_WEIGHT_LIMIT;
	
	/** Alternative gaming - magic dmg failures */
	public static boolean ALT_GAME_MAGICFAILURES;
	
	/** Alternative gaming - player must be in a castle-owning clan or ally to sign up for Dawn. */
	public static boolean ALT_GAME_REQUIRE_CASTLE_DAWN;
	
	/** Alternative gaming - allow clan-based castle ownage check rather than ally-based. */
	public static boolean ALT_GAME_REQUIRE_CLAN_CASTLE;
	
	/** Alternative gaming - allow free teleporting around the world. */
	public static boolean ALT_GAME_FREE_TELEPORT;
	
	/** Disallow recommend character twice or more a day ? */
	public static boolean ALT_RECOMMEND;
	
	/** Alternative gaming - add more or less than 3 sub-classes. */
	public static int ALT_MAX_SUBCLASS;
	
	/** Alternative gaming - allow sub-class addition without quest completion. */
	public static boolean ALT_GAME_SUBCLASS_WITHOUT_QUESTS;
	
	/** Alternative gaming - allow/disallow tutorial. */
	public static boolean ALT_ENABLE_TUTORIAL;
	
	/** View npc stats/drop by shift-cliking it for nongm-players */
	public static boolean ALT_GAME_VIEWNPC;
	
	/** Minimum number of player to participate in SevenSigns Festival */
	public static int ALT_FESTIVAL_MIN_PLAYER;
	
	/** Maximum of player contrib during Festival */
	public static int ALT_MAXIMUM_PLAYER_CONTRIB;
	
	/** Festival Manager start time. */
	public static long ALT_FESTIVAL_MANAGER_START;
	
	/** Festival Length */
	public static long ALT_FESTIVAL_LENGTH;
	
	/** Festival Cycle Length */
	public static long ALT_FESTIVAL_CYCLE_LENGTH;
	
	/** Festival First Spawn */
	public static long ALT_FESTIVAL_FIRST_SPAWN;
	
	/** Festival First Swarm */
	public static long ALT_FESTIVAL_FIRST_SWARM;
	
	/** Festival Second Spawn */
	public static long ALT_FESTIVAL_SECOND_SPAWN;
	
	/** Festival Second Swarm */
	public static long ALT_FESTIVAL_SECOND_SWARM;
	
	/** Festival Chest Spawn */
	public static long ALT_FESTIVAL_CHEST_SPAWN;
	
	/** Number of members needed to request a clan war */
	public static int ALT_CLAN_MEMBERS_FOR_WAR;
	
	/** Number of days before joining a new clan */
	public static int ALT_CLAN_JOIN_DAYS;
	/** Number of days before creating a new clan */
	public static int ALT_CLAN_CREATE_DAYS;
	/** Number of days it takes to dissolve a clan */
	public static int ALT_CLAN_DISSOLVE_DAYS;
	/** Number of days it takes to dissolve a clan again */
	public static int ALT_RECOVERY_PENALTY;
	
	/** Number of days before joining a new alliance when clan voluntarily leave an alliance */
	public static int ALT_ALLY_JOIN_DAYS_WHEN_LEAVED;
	/** Number of days before joining a new alliance when clan was dismissed from an alliance */
	public static int ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED;
	/** Number of days before accepting a new clan for alliance when clan was dismissed from an alliance */
	public static int ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED;
	/** Number of days before creating a new alliance when dissolved an alliance */
	public static int ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED;
	/** Maximum number of clans in ally */
	public static int ALT_MAX_NUM_OF_CLANS_IN_ALLY;
	/** Minimum number of parties to activate command channel */
	public static int ALT_CHANNEL_ACTIVATION_COUNT;
	
	/** Alternative gaming - all new characters always are newbies. */
	public static boolean ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE;
	
	/** Spell Book needed to learn skill */
	public static boolean SP_BOOK_NEEDED;
	/** Spell Book needet to enchant skill */
	public static boolean ES_SP_BOOK_NEEDED;
	/** Logging Chat Window */
	public static boolean LOG_CHAT;
	/** Logging Item Window */
	public static boolean LOG_ITEMS;
	
	/** Alternative privileges for admin */
	public static boolean ALT_PRIVILEGES_ADMIN;
	/** Alternative secure check privileges */
	public static boolean ALT_PRIVILEGES_SECURE_CHECK;
	/** Alternative default level for privileges */
	public static int ALT_PRIVILEGES_DEFAULT_LEVEL;
	
	/** Olympiad Competition Starting time */
	public static int ALT_OLY_START_TIME;
	
	/** Olympiad Minutes */
	public static int ALT_OLY_MIN;
	
	/** Olympiad Competition Period */
	public static long ALT_OLY_CPERIOD;
	
	/** Olympiad Battle Period */
	public static long ALT_OLY_BATTLE;
	
	/** Olympiad Battle Wait */
	public static long ALT_OLY_BWAIT;
	
	/** Olympiad Inital Wait */
	public static long ALT_OLY_IWAIT;
	
	/** Olympiad Weekly Period */
	public static long ALT_OLY_WPERIOD;
	
	/** Olympiad Validation Period */
	public static long ALT_OLY_VPERIOD;
	
	/** Olympiad Base Class */
	public static int ALT_OLY_CLASSED;
	
	/** Olympiad Non Base Class */
	public static int ALT_OLY_NONCLASSED;
	
	/** Olympiad Battle Reward */
	public static int ALT_OLY_BATTLE_REWARD_ITEM;
	
	/** Olympiad Class Based Reward Count */
	public static int ALT_OLY_CLASSED_RITEM_C;
	
	/** Olympiad Non Base Reward Count */
	public static int ALT_OLY_NONCLASSED_RITEM_C;
	
	/** Olympiad Competition Reward */
	public static int ALT_OLY_COMP_RITEM;
	
	/** Olympiad Item Reward */
	public static int ALT_OLY_GP_PER_POINT;
	
	/** Olympiad Min Points For Reward */
	public static int ALT_OLY_MIN_POINT_FOR_EXCH;
	
	/** Olympiad Hero Points */
	public static int ALT_OLY_HERO_POINTS;
	
	/** Manor Refresh Starting time */
	public static int ALT_MANOR_REFRESH_TIME;
	
	/** Manor Refresh Min */
	public static int ALT_MANOR_REFRESH_MIN;
	
	/** Manor Next Period Approve Starting time */
	public static int ALT_MANOR_APPROVE_TIME;
	
	/** Manor Next Period Approve Min */
	public static int ALT_MANOR_APPROVE_MIN;
	
	/** Manor Maintenance Time */
	public static int ALT_MANOR_MAINTENANCE_PERIOD;
	
	/** Manor Save All Actions */
	public static boolean ALT_MANOR_SAVE_ALL_ACTIONS;
	
	/** Manor Save Period Rate */
	public static int ALT_MANOR_SAVE_PERIOD_RATE;
	
	/** Initial Lottery prize */
	public static int ALT_LOTTERY_PRIZE;
	
	/** Lottery Ticket Price */
	public static int ALT_LOTTERY_TICKET_PRICE;
	
	/** What part of jackpot amount should receive characters who pick 5 wining numbers */
	public static float ALT_LOTTERY_5_NUMBER_RATE;
	
	/** What part of jackpot amount should receive characters who pick 4 wining numbers */
	public static float ALT_LOTTERY_4_NUMBER_RATE;
	
	/** What part of jackpot amount should receive characters who pick 3 wining numbers */
	public static float ALT_LOTTERY_3_NUMBER_RATE;
	
	/** How much adena receive characters who pick two or less of the winning number */
	public static int ALT_LOTTERY_2_AND_1_NUMBER_PRIZE;
	
	/** Four Sepulcher Settings */
	public static int FS_PENDING_TIME;
	public static int FS_ENTRY_TIME;
	public static int FS_PARTY_MEMBER_COUNT;
	
	/** Minimum size of a party that may enter dimensional rift */
	public static int RIFT_MIN_PARTY_SIZE;
	
	/** Time in ms the party has to wait until the mobs spawn when entering a room */
	public static int RIFT_SPAWN_DELAY;
	
	/** Amount of random rift jumps before party is ported back */
	public static int RIFT_MAX_JUMPS;
	
	/** Random time between two jumps in dimensional rift - in seconds */
	public static int RIFT_AUTO_JUMPS_TIME_MIN;
	public static int RIFT_AUTO_JUMPS_TIME_MAX;
	
	/** Dimensional Fragment cost for entering rift */
	public static int RIFT_ENTER_COST_RECRUIT;
	public static int RIFT_ENTER_COST_SOLDIER;
	public static int RIFT_ENTER_COST_OFFICER;
	public static int RIFT_ENTER_COST_CAPTAIN;
	public static int RIFT_ENTER_COST_COMMANDER;
	public static int RIFT_ENTER_COST_HERO;
	
	/** Time multiplier for boss room */
	public static float RIFT_BOSS_ROOM_TIME_MUTIPLY;
	
	/*
	 * ************************************************************************** GM CONFIG General GM AccessLevel *
	 */
	/** General GM access level */
	public static int GM_ACCESSLEVEL;
	/** General GM Minimal AccessLevel */
	public static int GM_MIN;
	/** General GM AccessLevel to change announcements */
	public static int GM_ANNOUNCE;
	/** General GM AccessLevel can /ban /unban */
	public static int GM_BAN;
	/** General GM AccessLevel can /ban /unban for chat */
	public static int GM_BAN_CHAT;
	/** General GM AccessLevel can /create_item and /gmshop */
	public static int GM_CREATE_ITEM;
	/** General GM AccessLevel can /delete */
	public static int GM_DELETE;
	/** General GM AccessLevel can /kick /disconnect */
	public static int GM_KICK;
	/** General GM AccessLevel for access to GMMenu */
	public static int GM_MENU;
	/** General GM AccessLevel to use god mode command */
	public static int GM_GODMODE;
	/** General GM AccessLevel with character edit rights */
	public static int GM_CHAR_EDIT;
	/** General GM AccessLevel with edit rights for other characters */
	public static int GM_CHAR_EDIT_OTHER;
	/** General GM AccessLevel with character view rights */
	public static int GM_CHAR_VIEW;
	/** General GM AccessLevel with NPC edit rights */
	public static int GM_NPC_EDIT;
	public static int GM_NPC_VIEW;
	/** General GM AccessLevel to teleport to any location */
	public static int GM_TELEPORT;
	/** General GM AccessLevel to teleport character to any location */
	public static int GM_TELEPORT_OTHER;
	/** General GM AccessLevel to restart server */
	public static int GM_RESTART;
	/** General GM AccessLevel for MonsterRace */
	public static int GM_MONSTERRACE;
	/** General GM AccessLevel to ride Wyvern */
	public static int GM_RIDER;
	/** General GM AccessLevel to unstuck without 5min delay */
	public static int GM_ESCAPE;
	/** General GM AccessLevel to resurect fixed after death */
	public static int GM_FIXED;
	/** General GM AccessLevel to create Path Nodes */
	public static int GM_CREATE_NODES;
	/** General GM AccessLevel with Enchant rights */
	public static int GM_ENCHANT;
	/** General GM AccessLevel to close/open Doors */
	public static int GM_DOOR;
	/** General GM AccessLevel with Resurrection rights */
	public static int GM_RES;
	/** General GM AccessLevel to attack in the peace zone */
	public static int GM_PEACEATTACK;
	/** General GM AccessLevel to heal */
	public static int GM_HEAL;
	/** General GM AccessLevel to unblock IPs detected as hack IPs */
	public static int GM_UNBLOCK;
	/** General GM AccessLevel to use Cache commands */
	public static int GM_CACHE;
	/** General GM AccessLevel to use test&st commands */
	public static int GM_TALK_BLOCK;
	public static int GM_TEST;
	/** Disable transaction on AccessLevel **/
	public static boolean GM_DISABLE_TRANSACTION;
	/** GM transactions disabled from this range */
	public static int GM_TRANSACTION_MIN;
	/** GM transactions disabled to this range */
	public static int GM_TRANSACTION_MAX;
	
	public static int GM_REPAIR = 75;
	
	/* Rate control */
	/** Rate for eXperience Point rewards */
	public static float RATE_XP;
	/** Rate for Skill Point rewards */
	public static float RATE_SP;
	/** Rate for party eXperience Point rewards */
	public static float RATE_PARTY_XP;
	/** Rate for party Skill Point rewards */
	public static float RATE_PARTY_SP;
	/** Rate for Quest rewards (XP and SP) */
	public static float RATE_QUESTS_REWARD;
	/** Rate for drop adena */
	public static float RATE_DROP_ADENA;
	/** Rate for cost of consumable */
	public static float RATE_CONSUMABLE_COST;
	/** Rate for dropped items */
	public static float RATE_DROP_ITEMS;
	/** Rate for dropped items for bosses */
	public static float RATE_BOSS_DROP_ITEMS;
	/** Rate for spoiled items */
	public static float RATE_DROP_SPOIL;
	/** Rate for manor items */
	public static int RATE_DROP_MANOR;
	/** Rate for quest items */
	public static float RATE_DROP_QUEST;
	/** Rate for karma and experience lose */
	public static float RATE_KARMA_EXP_LOST;
	/** Rate siege guards prices */
	public static float RATE_SIEGE_GUARDS_PRICE;
	/*
	 * Alternative Xp/Sp rewards, if not 0, then calculated as 2^((mob.level-player.level) / coef), A few examples for "AltGameExponentXp = 5." and "AltGameExponentSp = 3." diff = 0 (player and mob has the same level), XP bonus rate = 1, SP bonus rate = 1 diff = 3 (mob is 3 levels above), XP bonus
	 * rate = 1.52, SP bonus rate = 2 diff = 5 (mob is 5 levels above), XP bonus rate = 2, SP bonus rate = 3.17 diff = -8 (mob is 8 levels below), XP bonus rate = 0.4, SP bonus rate = 0.16
	 */
	/** Alternative eXperience Point rewards */
	public static float ALT_GAME_EXPONENT_XP;
	/** Alternative Spirit Point rewards */
	public static float ALT_GAME_EXPONENT_SP;
	
	// Player Drop Rate control
	/** Limit for player drop */
	public static int PLAYER_DROP_LIMIT;
	/** Rate for drop */
	public static int PLAYER_RATE_DROP;
	/** Rate for player's item drop */
	public static int PLAYER_RATE_DROP_ITEM;
	/** Rate for player's equipment drop */
	public static int PLAYER_RATE_DROP_EQUIP;
	/** Rate for player's equipment and weapon drop */
	public static int PLAYER_RATE_DROP_EQUIP_WEAPON;
	
	// Pet Rates (Multipliers)
	/** Rate for experience rewards of the pet */
	public static float PET_XP_RATE;
	/** Rate for food consumption of the pet */
	public static int PET_FOOD_RATE;
	/** Rate for experience rewards of the Sin Eater */
	public static float SINEATER_XP_RATE;
	
	// Karma Drop Rate control
	/** Karma drop limit */
	public static int KARMA_DROP_LIMIT;
	/** Karma drop rate */
	public static int KARMA_RATE_DROP;
	/** Karma drop rate for item */
	public static int KARMA_RATE_DROP_ITEM;
	/** Karma drop rate for equipment */
	public static int KARMA_RATE_DROP_EQUIP;
	/** Karma drop rate for equipment and weapon */
	public static int KARMA_RATE_DROP_EQUIP_WEAPON;
	
	/** Time after which item will auto-destroy */
	public static int AUTODESTROY_ITEM_AFTER;
	/** List of items that will not be destroyed (seperated by ",") */
	public static String PROTECTED_ITEMS;
	/** List of items that will not be destroyed */
	public static List<Integer> LIST_PROTECTED_ITEMS;
	
	/** Update itens owned by this char when storing the char on DB */
	public static boolean UPDATE_ITEMS_ON_CHAR_STORE;
	/** Update itens only when strictly necessary */
	public static boolean LAZY_ITEMS_UPDATE;
	/** Auto destroy nonequipable items dropped by players */
	public static boolean DESTROY_DROPPED_PLAYER_ITEM;
	/** Auto destroy equipable items dropped by players */
	public static boolean DESTROY_EQUIPABLE_PLAYER_ITEM;
	/** Save items on ground for restoration on server restart */
	public static boolean SAVE_DROPPED_ITEM;
	/** Empty table ItemsOnGround after load all items */
	public static boolean EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD;
	/** Time interval to save into db items on ground */
	public static int SAVE_DROPPED_ITEM_INTERVAL;
	/** Clear all items stored in ItemsOnGround table */
	public static boolean CLEAR_DROPPED_ITEM_TABLE;
	
	/** Accept precise drop calculation ? */
	public static boolean PRECISE_DROP_CALCULATION;
	/** Accept multi-items drop ? */
	public static boolean MULTIPLE_ITEM_DROP;
	
	/** Falling Damage */
	public static boolean ENABLE_FALLING_DAMAGE;
	
	/** Period in days after which character is deleted */
	public static int DELETE_DAYS;
	
	/** Datapack root directory */
	public static File DATAPACK_ROOT;
	
	/** Maximum range mobs can randomly go from spawn point */
	public static int MAX_DRIFT_RANGE;
	
	/** Allow fishing ? */
	public static boolean ALLOWFISHING;
	
	/** Jail config **/
	public static boolean JAIL_IS_PVP;
	public static boolean JAIL_DISABLE_CHAT;
	
	/** Allow L2Walker */
	public static boolean ALLOW_L2WALKER;
	
	/** Allow Manor system */
	public static boolean ALLOW_MANOR;
	
	/** Allow NPC walkers */
	public static boolean ALLOW_NPC_WALKERS;
	
	/** Allow Pet walkers */
	public static boolean ALLOW_PET_WALKERS;
	
	/** Allow Discard item ? */
	public static boolean ALLOW_DISCARDITEM;
	/** Allow freight ? */
	public static boolean ALLOW_FREIGHT;
	/** Allow warehouse ? */
	public static boolean ALLOW_WAREHOUSE;
	/** Allow warehouse cache? */
	public static boolean WAREHOUSE_CACHE;
	/** How long store WH datas */
	public static int WAREHOUSE_CACHE_TIME;
	/** Allow wear ? (try on in shop) */
	public static boolean ALLOW_WEAR;
	/** Duration of the try on after which items are taken back */
	public static int WEAR_DELAY;
	/** Price of the try on of one item */
	public static int WEAR_PRICE;
	/** Allow lottery ? */
	public static boolean ALLOW_LOTTERY;
	/** Allow race ? */
	public static boolean ALLOW_RACE;
	/** Allow water ? */
	public static boolean ALLOW_WATER;
	/** Allow rent pet ? */
	public static boolean ALLOW_RENTPET;
	/** Allow boat ? */
	public static boolean ALLOW_BOAT;
	
	/** Time after which a packet is considered as lost */
	public static int PACKET_LIFETIME;
	
	/** Detects server deadlocks */
	public static boolean DEADLOCK_DETECTOR;
	/** Check interval in seconds */
	public static int DEADLOCK_CHECK_INTERVAL;
	/** Restarts server to remove deadlocks */
	public static boolean RESTART_ON_DEADLOCK;
	
	/** Allow Wyvern Upgrader ? */
	public static boolean ALLOW_WYVERN_UPGRADER;
	
	// protocol revision
	/** Minimal protocol revision */
	public static int MIN_PROTOCOL_REVISION;
	/** Maximal protocol revision */
	public static int MAX_PROTOCOL_REVISION;
	
	// random animation interval
	/** Minimal time between 2 animations of a NPC */
	public static int MIN_NPC_ANIMATION;
	/** Maximal time between 2 animations of a NPC */
	public static int MAX_NPC_ANIMATION;
	/** Minimal time between animations of a monster */
	public static int MIN_MONSTER_ANIMATION;
	/** Maximal time between animations of a monster */
	public static int MAX_MONSTER_ANIMATION;
	
	/** Knownlist update time interval */
	public static long KNOWNLIST_UPDATE_INTERVAL;
	
	/** Activate position recorder ? */
	public static boolean ACTIVATE_POSITION_RECORDER;
	/** Use 3D Map ? */
	public static boolean USE_3D_MAP;
	
	// Community Board
	/** Type of community */
	public static int COMMUNITY_TYPE;
	public static boolean BBS_SHOW_PLAYERLIST;
	public static String BBS_DEFAULT;
	/** Show level of the community board ? */
	public static boolean SHOW_LEVEL_COMMUNITYBOARD;
	/** Show status of the community board ? */
	public static boolean SHOW_STATUS_COMMUNITYBOARD;
	/** Size of the name page on the community board */
	public static int NAME_PAGE_SIZE_COMMUNITYBOARD;
	/** Name per row on community board */
	public static int NAME_PER_ROW_COMMUNITYBOARD;
	
	// Configuration files
	/**
	 * Properties file that allows selection of new Classes for storage of World Objects. <br>
	 * This may help servers with large amounts of players recieving error messages related to the <i>L2ObjectHashMap</i> and <i>L2ObejctHashSet</i> classes.
	 */
	/** Properties file for game server (connection and ingame) configurations */
	public static final String CONFIGURATION_FILE = "./config/server.ini";
	/** Properties file for game server options */
	public static final String OPTIONS_FILE = "./config/options.ini";
	/** Properties file for login server configurations */
	public static final String LOGIN_CONFIGURATION_FILE = "./config/loginserver.ini";
	/** Properties file for the ID factory */
	public static final String ID_CONFIG_FILE = "./config/idfactory.ini";
	/** Properties file for other configurations */
	public static final String OTHER_CONFIG_FILE = "./config/other.ini";
	/** Properties file for rates configurations */
	public static final String RATES_CONFIG_FILE = "./config/rates.ini";
	/** Properties file for alternative configurations */
	public static final String ALT_SETTINGS_FILE = "./config/altsettings.ini";
	/** Properties file for feature configurations */
	public static final String FEATURE_CONFIG_FILE = "./config/Feature.ini";
	/** Properties file for custom configurations */
	public static final String CUSTOM_CONFIG_FILE = "./config/custom.ini";
	/** Properties file for events configurations */
	public static final String EVENTS_CONFIG_FILE = "./config/events.ini";
	/** Properties file for PVP configurations */
	public static final String PVP_CONFIG_FILE = "./config/pvp.ini";
	/** Properties file for GM access configurations */
	public static final String GM_ACCESS_FILE = "./config/GMAccess.ini";
	/** Properties file for Flood Protector configurations */
	public static final String FLOOD_PROTECTOR_FILE = "./config/floodprotector.ini";
	/** Properties file for MMO configurations */
	public static final String MMO_CONFIG_FILE = "./config/mmo.ini";
	/** Properties file for telnet configurations */
	public static final String TELNET_FILE = "./config/telnet.ini";
	/** Properties file for siege configurations */
	public static final String SIEGE_CONFIGURATION_FILE = "./config/siege.ini";
	/** Properties file for olympiad configurations */
	public static final String OLYMPIAD_CONFIGURATION_FILE = "config/olympiad.ini";
	/** Properties file for extensions configurations */
	public static final String EXTENSIONS_CONFIGURATION_FILE = "config/extensions.ini";
	/** Properties file for GeoData configurations */
	public static final String GEODATA_CONFIGURATION_FILE = "config/GeoData.ini";
	/** Text file containing hexadecimal value of server ID */
	public static final String HEXID_FILE = "./config/hexid.txt";
	/**
	 * Properties file for alternative configure GM commands access level.<br>
	 * Note that this file only read if "AltPrivilegesAdmin = True"
	 */
	public static final String COMMAND_PRIVILEGES_FILE = "./config/command-privileges.ini";
	
	public static int MAX_ITEM_IN_PACKET;
	
	public static boolean CHECK_KNOWN;
	
	/** Game Server login port */
	public static int GAME_SERVER_LOGIN_PORT;
	/** Game Server login Host */
	public static String GAME_SERVER_LOGIN_HOST;
	/** Internal Hostname */
	public static String INTERNAL_HOSTNAME;
	/** External Hostname */
	public static String EXTERNAL_HOSTNAME;
	public static String ROUTER_HOSTNAME;
	
	public static int PATH_NODE_RADIUS;
	public static int NEW_NODE_ID;
	public static int SELECTED_NODE_ID;
	public static int LINKED_NODE_ID;
	public static String NEW_NODE_TYPE;
	
	/** Show L2Monster level and aggro ? */
	public static boolean SHOW_NPC_LVL;
	
	/**
	 * Force full item inventory packet to be sent for any item change ?<br>
	 * <u><i>Note:</i></u> This can increase network traffic
	 */
	public static boolean FORCE_INVENTORY_UPDATE;
	/** Disable the use of guards against agressive monsters ? */
	public static boolean GUARD_ATTACK_AGGRO_MOB;
	/** Allow use of NPC Buffer ? */
	public static boolean NPC_BUFFER_ENABLED;
	public static int AIO_BUFF_DURATION;
	/** Allow Offline Trade ? */
	public static boolean OFFLINE_TRADE_ENABLE;
	/** Allow Offline Craft ? */
	public static boolean OFFLINE_CRAFT_ENABLE;
	/** Restore Offliners ? */
	public static boolean RESTORE_OFFLINERS;
	/** Max Days for Offline Stores ? */
	public static int OFFLINE_MAX_DAYS;
	/** Disconnect shops that finished selling ? */
	public static boolean OFFLINE_DISCONNECT_FINISHED;
	/** Allow color for offline mode ? */
	public static boolean OFFLINE_SET_NAME_COLOR;
	/** Color for offline mode */
	public static int OFFLINE_NAME_COLOR;
	/** Allow teleporting to towns that are under siege ? */
	public static boolean ALLOW_SIEGE_TELEPORT;
	
	/** Allow players to keep subclass skills ? */
	public static boolean KEEP_SUBCLASS_SKILLS;
	
	/** Allow use Event Managers for change occupation ? */
	public static boolean ALLOW_CLASS_MASTERS;
	public static ClassMasterSettings CLASS_MASTER_SETTINGS;
	public static boolean ALLOW_ENTIRE_TREE;
	public static boolean ALTERNATE_CLASS_MASTER;
	
	/** Auto rewarding players */
	public static boolean ALLOW_AUTO_REWARDER;
	public static int AUTO_REWARD_DELAY;
	public static int AUTO_REWARD_ID;
	public static int AUTO_REWARD_COUNT;
	
	/** Custom starting spawn for new characters */
	public static boolean CUSTOM_STARTING_SPAWN;
	public static int CUSTOM_SPAWN_X;
	public static int CUSTOM_SPAWN_Y;
	public static int CUSTOM_SPAWN_Z;
	
	/** Allow players to view all available classes to the same village master */
	public static boolean CHANGE_SUBCLASS_EVERYWHERE;
	
	/** Auto Noblesse status at login */
	public static boolean AUTO_NOBLE_STATUS;
	
	/** Allow enchanting hero items */
	public static boolean ALLOW_HERO_ENCHANT;
	
	/** Allow Dual Box in game */
	public static boolean PREVENT_DUAL_BOXING;
	
	/** Use /block command as an AntiBuff shield */
	public static boolean BLOCK_UNWANTED_BUFFS;
	
	/** Enable custom data tables ? */
	public static boolean CUSTOM_SPAWNLIST_TABLE;
	public static boolean SAVE_GMSPAWN_ON_CUSTOM;
	public static boolean CUSTOM_NPC_TABLE;
	public static boolean CUSTOM_NPC_SKILLS_TABLE;
	public static boolean CUSTOM_ITEM_TABLES;
	public static boolean CUSTOM_ARMORSETS_TABLE;
	public static boolean CUSTOM_TELEPORT_TABLE;
	public static boolean CUSTOM_DROPLIST_TABLE;
	public static boolean CUSTOM_MERCHANT_TABLES;
	
	/** Champion Mod */
	public static boolean CHAMPION_ENABLE;
	public static boolean CHAMPION_PASSIVE;
	public static int CHAMPION_FREQUENCY;
	public static String CHAMPION_TITLE;
	public static int CHAMP_MIN_LVL;
	public static int CHAMP_MAX_LVL;
	public static int CHAMPION_HP;
	public static int CHAMPION_REWARDS;
	public static int CHAMPION_ADENAS_REWARDS;
	public static float CHAMPION_HP_REGEN;
	public static float CHAMPION_ATK;
	public static float CHAMPION_SPD_ATK;
	public static int CHAMPION_REWARD_LOWER_CHANCE;
	public static int CHAMPION_REWARD_HIGHER_CHANCE;
	public static int CHAMPION_REWARD_ID;
	public static int CHAMPION_REWARD_QTY;
	
	// --------------------------------------------------
	// FloodProtector Settings
	// --------------------------------------------------
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
	
	// --------------------------------------------------
	// MMO Settings
	// --------------------------------------------------
	public static int MMO_SELECTOR_SLEEP_TIME;
	public static int MMO_MAX_SEND_PER_PASS;
	public static int MMO_MAX_READ_PER_PASS;
	public static int MMO_HELPER_BUFFER_COUNT;
	
	/** Zone Setting */
	public static int ZONE_TOWN;
	
	/** Crafting Enabled? */
	public static boolean IS_CRAFTING_ENABLED;
	
	// Inventory slots limit
	/** Maximum inventory slots limits for non dwarf characters */
	public static int INVENTORY_MAXIMUM_NO_DWARF;
	/** Maximum inventory slots limits for dwarf characters */
	public static int INVENTORY_MAXIMUM_DWARF;
	/** Maximum inventory slots limits for GM */
	public static int INVENTORY_MAXIMUM_GM;
	/** Maximum inventory slots limits for pet */
	public static int INVENTORY_MAXIMUM_PET;
	
	// Warehouse slots limits
	/** Maximum inventory slots limits for non dwarf warehouse */
	public static int WAREHOUSE_SLOTS_NO_DWARF;
	/** Maximum inventory slots limits for dwarf warehouse */
	public static int WAREHOUSE_SLOTS_DWARF;
	/** Maximum inventory slots limits for clan warehouse */
	public static int WAREHOUSE_SLOTS_CLAN;
	/** Maximum inventory slots limits for freight */
	public static int FREIGHT_SLOTS;
	
	// Spoil Rates
	/** Allow spoil on lower level mobs than the character */
	public static boolean CAN_SPOIL_LOWER_LEVEL_MOBS;
	/** Allow delevel and spoil mob ? */
	public static boolean CAN_DELEVEL_AND_SPOIL_MOBS;
	/** Maximum level difference between player and mob level */
	public static float MAXIMUM_PLAYER_AND_MOB_LEVEL_DIFFERENCE;
	/** Base rate for spoil */
	public static float BASE_SPOIL_RATE;
	/** Minimum spoil rate */
	public static float MINIMUM_SPOIL_RATE;
	/** Maximum level difference between player and spoil level to allow before decreasing spoil chance */
	public static float SPOIL_LEVEL_DIFFERENCE_LIMIT;
	/** Spoil level multiplier */
	public static float SPOIL_LEVEL_DIFFERENCE_MULTIPLIER;
	/** Last level spoil learned */
	public static int LAST_LEVEL_SPOIL_IS_LEARNED;
	
	// Karma System Variables
	/** Minimum karma gain/loss */
	public static int KARMA_MIN_KARMA;
	/** Maximum karma gain/loss */
	public static int KARMA_MAX_KARMA;
	/** Number to divide the xp recieved by, to calculate karma lost on xp gain/lost */
	public static int KARMA_XP_DIVIDER;
	/** The Minimum Karma lost if 0 karma is to be removed */
	public static int KARMA_LOST_BASE;
	/** Can a GM drop item ? */
	public static boolean KARMA_DROP_GM;
	/** Should award a pvp point for killing a player with karma ? */
	public static boolean KARMA_AWARD_PK_KILL;
	/** Minimum PK required to drop */
	public static int KARMA_PK_LIMIT;
	
	/** List of pet items that cannot be dropped (seperated by ",") when PVP */
	public static String KARMA_NONDROPPABLE_PET_ITEMS;
	/** List of items that cannot be dropped (seperated by ",") when PVP */
	public static String KARMA_NONDROPPABLE_ITEMS;
	/** List of pet items that cannot be dropped when PVP */
	public static List<Integer> KARMA_LIST_NONDROPPABLE_PET_ITEMS;
	/** List of items that cannot be dropped when PVP */
	public static List<Integer> KARMA_LIST_NONDROPPABLE_ITEMS;
	
	/** List of items that cannot be dropped (seperated by ",") */
	public static String NONDROPPABLE_ITEMS;
	/** List of items that cannot be dropped */
	public static List<Integer> LIST_NONDROPPABLE_ITEMS;
	
	/** List of NPCs that rent pets (seperated by ",") */
	public static String PET_RENT_NPC;
	/** List of NPCs that rent pets */
	public static List<Integer> LIST_PET_RENT_NPC;
	
	/** Duration (in ms) while a player stay in PVP mode after hitting an innocent */
	public static int PVP_NORMAL_TIME;
	/** Duration (in ms) while a player stay in PVP mode after hitting a purple player */
	public static int PVP_PVP_TIME;
	
	// Karma Punishment
	/** Allow player with karma to be killed in peace zone ? */
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE;
	/** Allow player with karma to shop ? */
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_SHOP;
	/** Allow player with karma to use gatekeepers ? */
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_GK;
	/** Allow player with karma to use SOE or Return skill ? */
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_TELEPORT;
	/** Allow player with karma to trade ? */
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_TRADE;
	/** Allow player with karma to use warehouse ? */
	public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE;
	
	/** Enumeration for type of ID Factory */
	public static enum IdFactoryType
	{
		Compaction,
		BitSet,
		Stack
	}
	
	/** ID Factory type */
	public static IdFactoryType IDFACTORY_TYPE;
	/** Check for bad ID ? */
	public static boolean BAD_ID_CHECKING;
	
	/** Enumeration for type of maps object */
	public static enum ObjectMapType
	{
		L2ObjectHashMap,
		WorldObjectMap
	}
	
	/** Enumeration for type of set object */
	public static enum ObjectSetType
	{
		L2ObjectHashSet,
		WorldObjectSet
	}
	
	/** Type of map object */
	public static ObjectMapType MAP_TYPE;
	/** Type of set object */
	public static ObjectSetType SET_TYPE;
	
	/**
	 * Allow lesser effects to be canceled if stronger effects are used when effects of the same stack group are used.<br>
	 * New effects that are added will be canceled if they are of lesser priority to the old one.
	 */
	public static boolean EFFECT_CANCELING;
	
	/** Auto-delete invalid quest data ? */
	public static boolean AUTODELETE_INVALID_QUEST_DATA;
	
	/** Chance that an item will succesfully be enchanted */
	public static int ENCHANT_CHANCE_WEAPON;
	public static int ENCHANT_CHANCE_ARMOR;
	public static int ENCHANT_CHANCE_JEWELRY;
	public static int BLESSED_ENCHANT_CHANCE_WEAPON;
	public static int BLESSED_ENCHANT_CHANCE_ARMOR;
	public static int BLESSED_ENCHANT_CHANCE_JEWELRY;
	/** Maximum level of enchantment */
	public static int ENCHANT_MAX_WEAPON;
	public static int ENCHANT_MAX_ARMOR;
	public static int ENCHANT_MAX_JEWELRY;
	/** maximum level of safe enchantment for normal items */
	public static int ENCHANT_SAFE_MAX;
	/** maximum level of safe enchantment for full body armor */
	public static int ENCHANT_SAFE_MAX_FULL;
	
	// Character multipliers
	/** Multiplier for character HP regeneration */
	public static double HP_REGEN_MULTIPLIER;
	/** Mutilplier for character MP regeneration */
	public static double MP_REGEN_MULTIPLIER;
	/** Multiplier for character CP regeneration */
	public static double CP_REGEN_MULTIPLIER;
	
	// Raid Boss multipliers
	/** Multiplier for Raid boss HP regeneration */
	public static double RAID_HP_REGEN_MULTIPLIER;
	/** Mulitplier for Raid boss MP regeneration */
	public static double RAID_MP_REGEN_MULTIPLIER;
	/** Multiplier for Raid boss power defense multiplier */
	public static double RAID_PDEFENCE_MULTIPLIER;
	/** Multiplier for Raid boss magic defense multiplier */
	public static double RAID_MDEFENCE_MULTIPLIER;
	/** Raid Boss Minin Spawn Timer */
	public static double RAID_MINION_RESPAWN_TIMER;
	/** Mulitplier for Raid boss minimum time respawn */
	public static float RAID_MIN_RESPAWN_MULTIPLIER;
	/** Mulitplier for Raid boss maximum time respawn */
	public static float RAID_MAX_RESPAWN_MULTIPLIER;
	/** Amount of adenas when starting a new character */
	public static int STARTING_ADENA;
	/** Starting level of a new character */
	public static byte STARTING_LEVEL;
	/** Starting level of a new subclass */
	public static byte STARTING_SUB_LEVEL;
	
	/** Maximum character running speed */
	public static int MAX_RUN_SPEED;
	/** Maximum character Physical Critical Rate */
	public static int MAX_PCRIT_RATE;
	/** Maximum character Magic Critical Rate */
	public static int MAX_MCRIT_RATE;
	/** Maximum character Physical Attack Speed */
	public static int MAX_PATK_SPEED;
	/** Maximum character Magic Attack Speed */
	public static int MAX_MATK_SPEED;
	
	/** Deep Blue Mobs' Drop Rules Enabled */
	public static boolean DEEPBLUE_DROP_RULES;
	public static int UNSTUCK_INTERVAL;
	
	/** Is telnet enabled ? */
	public static boolean IS_TELNET_ENABLED;
	/** Telnet status port */
	public static int TELNET_PORT;
	
	/** Player Protection control */
	public static int PLAYER_SPAWN_PROTECTION;
	public static int PLAYER_FAKEDEATH_UP_PROTECTION;
	
	/** Define Party XP cutoff point method - Possible values: level and percentage */
	public static String PARTY_XP_CUTOFF_METHOD;
	/** Define the cutoff point value for the "level" method */
	public static int PARTY_XP_CUTOFF_LEVEL;
	/** Define the cutoff point value for the "percentage" method */
	public static double PARTY_XP_CUTOFF_PERCENT;
	
	/** Percent HP is restore on respawn */
	public static double RESPAWN_RESTORE_HP;
	
	/** Allow randomizing of the respawn point in towns. */
	public static boolean RESPAWN_RANDOM_ENABLED;
	/** The maximum offset from the base respawn point to allow. */
	public static int RESPAWN_RANDOM_MAX_OFFSET;
	
	/** Maximum number of available slots for pvt stores (sell/buy) - Dwarves */
	public static int MAX_PVTSTORE_SLOTS_DWARF;
	/** Maximum number of available slots for pvt stores (sell/buy) - Others */
	public static int MAX_PVTSTORE_SLOTS_OTHER;
	
	/** Store skills cooltime on char exit/relogin */
	public static boolean STORE_SKILL_COOLTIME;
	/** Show licence or not just after login (if false, will directly go to the Server List */
	public static boolean SHOW_LICENCE;
	
	/** Default punishment for illegal actions */
	public static int DEFAULT_PUNISH;
	/** Parameter for default punishment */
	public static int DEFAULT_PUNISH_PARAM;
	
	/** Accept new game server ? */
	public static boolean ACCEPT_NEW_GAMESERVER;
	/** Hexadecimal ID of the game server */
	public static byte[] HEX_ID;
	/** Accept alternate ID for server ? */
	public static boolean ACCEPT_ALTERNATE_ID;
	/** ID for request to the server */
	public static int REQUEST_ID;
	public static boolean RESERVE_HOST_ON_LOGIN = false;
	
	public static int MINIMUM_UPDATE_DISTANCE;
	public static int KNOWNLIST_FORGET_DELAY;
	public static int MINIMUN_UPDATE_TIME;
	
	public static boolean ANNOUNCE_MAMMON_SPAWN;
	public static boolean LAZY_CACHE;
	
	/** Enable colored name for GM ? */
	public static boolean GM_NAME_COLOR_ENABLED;
	/** Color of GM name */
	public static int GM_NAME_COLOR;
	/** Color of admin name */
	public static int ADMIN_NAME_COLOR;
	/** Place an aura around the GM ? */
	public static boolean GM_HERO_AURA;
	/** Set the GM invulnerable at startup ? */
	public static boolean GM_STARTUP_INVULNERABLE;
	/** Set the GM invisible at startup ? */
	public static boolean GM_STARTUP_INVISIBLE;
	/** Set silence to GM at startup ? */
	public static boolean GM_STARTUP_SILENCE;
	/** Add GM in the GM list at startup ? */
	public static boolean GM_STARTUP_AUTO_LIST;
	
	/** Allow petition ? */
	public static boolean PETITIONING_ALLOWED;
	/** Maximum number of petitions per player */
	public static int MAX_PETITIONS_PER_PLAYER;
	/** Maximum number of petitions pending */
	public static int MAX_PETITIONS_PENDING;
	
	/** Bypass exploit protection ? */
	public static boolean BYPASS_VALIDATION;
	
	/** Only GM buy items for free **/
	public static boolean ONLY_GM_ITEMS_FREE;
	
	/** GM Audit ? */
	public static boolean GMAUDIT;
	
	/** Allow auto-create account ? */
	public static boolean AUTO_CREATE_ACCOUNTS;
	
	public static boolean FLOOD_PROTECTION;
	public static int FAST_CONNECTION_LIMIT;
	public static int NORMAL_CONNECTION_TIME;
	public static int FAST_CONNECTION_TIME;
	public static int MAX_CONNECTION_PER_IP;
	
	/** Enforce gameguard query on character login ? */
	public static boolean GAMEGUARD_ENFORCE;
	/** Don't allow player to perform trade,talk with npc and move until gameguard reply received ? */
	public static boolean GAMEGUARD_PROHIBITACTION;
	
	/** Recipebook limits */
	public static int DWARF_RECIPE_LIMIT;
	public static int COMMON_RECIPE_LIMIT;
	
	/** Grid Options */
	public static boolean GRIDS_ALWAYS_ON;
	public static int GRID_NEIGHBOR_TURNON_TIME;
	public static int GRID_NEIGHBOR_TURNOFF_TIME;
	
	/** Clan Hall function related configs */
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
	
	/** Castle function related configs */
	public static long CS_TELE_FEE_RATIO;
	public static int CS_TELE1_FEE;
	public static int CS_TELE2_FEE;
	public static long CS_MPREG_FEE_RATIO;
	public static int CS_MPREG1_FEE;
	public static int CS_MPREG2_FEE;
	public static int CS_MPREG3_FEE;
	public static int CS_MPREG4_FEE;
	public static long CS_HPREG_FEE_RATIO;
	public static int CS_HPREG1_FEE;
	public static int CS_HPREG2_FEE;
	public static int CS_HPREG3_FEE;
	public static int CS_HPREG4_FEE;
	public static int CS_HPREG5_FEE;
	public static long CS_EXPREG_FEE_RATIO;
	public static int CS_EXPREG1_FEE;
	public static int CS_EXPREG2_FEE;
	public static int CS_EXPREG3_FEE;
	public static int CS_EXPREG4_FEE;
	public static long CS_SUPPORT_FEE_RATIO;
	public static int CS_SUPPORT1_FEE;
	public static int CS_SUPPORT2_FEE;
	public static int CS_SUPPORT3_FEE;
	public static int CS_SUPPORT4_FEE;
	
	/** GeoData Settings */
	public static int PATHFINDING;
	public static File PATHNODE_DIR;
	public static String PATHFIND_BUFFERS;
	public static float LOW_WEIGHT;
	public static float MEDIUM_WEIGHT;
	public static float HIGH_WEIGHT;
	public static boolean ADVANCED_DIAGONAL_STRATEGY;
	public static float DIAGONAL_WEIGHT;
	public static int MAX_POSTFILTER_PASSES;
	public static boolean DEBUG_PATH;
	public static boolean FORCE_GEODATA;
	public static int COORD_SYNCHRONIZE;
	public static Path GEODATA_PATH;
	public static boolean TRY_LOAD_UNSPECIFIED_REGIONS;
	public static Map<String, Boolean> GEODATA_REGIONS;
	
	/** Max number of buffs */
	public static byte BUFFS_MAX_AMOUNT;
	
	/** Alt Settings for devs */
	public static boolean ALT_DEV_NO_QUESTS;
	public static boolean ALT_DEV_NO_SPAWNS;
	
	/**
	 * This class initializes all global variables for configuration.<br>
	 * If key doesn't appear in properties file, a default value is setting on by this class.
	 */
	public static void load()
	{
		if (Server.SERVER_MODE == Server.MODE_GAMESERVER)
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
			
			_log.info("Loading Gameserver Configuration Files.");
			
			final Properties serverSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(CONFIGURATION_FILE)))
			{
				serverSettings.load(is);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + CONFIGURATION_FILE + " File.");
			}
			
			GAMESERVER_HOSTNAME = serverSettings.getProperty("GameserverHostname");
			PORT_GAME = Integer.parseInt(serverSettings.getProperty("GameserverPort", "7777"));
			
			INTERNAL_HOSTNAME = serverSettings.getProperty("InternalHostname", "*");
			EXTERNAL_HOSTNAME = serverSettings.getProperty("ExternalHostname", "*");
			ROUTER_HOSTNAME = serverSettings.getProperty("RouterHostname", "");
			
			GAME_SERVER_LOGIN_PORT = Integer.parseInt(serverSettings.getProperty("LoginPort", "9014"));
			GAME_SERVER_LOGIN_HOST = serverSettings.getProperty("LoginHost", "127.0.0.1");
			
			REQUEST_ID = Integer.parseInt(serverSettings.getProperty("RequestServerID", "0"));
			ACCEPT_ALTERNATE_ID = Boolean.parseBoolean(serverSettings.getProperty("AcceptAlternateID", "True"));
			
			DATABASE_DRIVER = serverSettings.getProperty("Driver", "com.mysql.jdbc.Driver");
			DATABASE_URL = serverSettings.getProperty("URL", "jdbc:mysql://localhost/l2jdb");
			DATABASE_LOGIN = serverSettings.getProperty("Login", "root");
			DATABASE_PASSWORD = serverSettings.getProperty("Password", "");
			DATABASE_MAX_CONNECTIONS = Integer.parseInt(serverSettings.getProperty("MaximumDbConnections", "10"));
			CONNECTION_CLOSE_TIME = Long.parseLong(serverSettings.getProperty("ConnectionCloseTime", "60000"));
			
			try
			{
				DATAPACK_ROOT = new File(serverSettings.getProperty("DatapackRoot", ".").replaceAll("\\\\", "/")).getCanonicalFile();
			}
			catch (final Exception e)
			{
				_log.warning("Error setting datapack root!");
				DATAPACK_ROOT = new File(".");
			}
			
			CNAME_TEMPLATE = serverSettings.getProperty("CnameTemplate", ".*");
			PET_NAME_TEMPLATE = serverSettings.getProperty("PetNameTemplate", ".*");
			
			MAX_CHARACTERS_NUMBER_PER_ACCOUNT = Integer.parseInt(serverSettings.getProperty("CharMaxNumber", "7"));
			MAXIMUM_ONLINE_USERS = Integer.parseInt(serverSettings.getProperty("MaximumOnlineUsers", "100"));
			
			MIN_PROTOCOL_REVISION = Integer.parseInt(serverSettings.getProperty("MinProtocolRevision", "660"));
			MAX_PROTOCOL_REVISION = Integer.parseInt(serverSettings.getProperty("MaxProtocolRevision", "665"));
			
			if (MIN_PROTOCOL_REVISION > MAX_PROTOCOL_REVISION)
			{
				throw new Error("MinProtocolRevision is bigger than MaxProtocolRevision in server configuration file.");
			}
			
			_log.info("Loading GeoData Configuration Files.");
			
			final Properties geoData = new Properties();
			try (InputStream is = new FileInputStream(new File(GEODATA_CONFIGURATION_FILE)))
			{
				geoData.load(is);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + GEODATA_CONFIGURATION_FILE + " File.");
			}
			
			PATHFINDING = Integer.parseInt(geoData.getProperty("PathFinding", "0"));
			PATHFIND_BUFFERS = geoData.getProperty("PathFindBuffers", "100x6;128x6;192x6;256x4;320x4;384x4;500x2");
			LOW_WEIGHT = Float.parseFloat(geoData.getProperty("LowWeight", "0.5f"));
			MEDIUM_WEIGHT = Float.parseFloat(geoData.getProperty("MediumWeight", "2"));
			HIGH_WEIGHT = Float.parseFloat(geoData.getProperty("HighWeight", "3"));
			ADVANCED_DIAGONAL_STRATEGY = Boolean.parseBoolean(geoData.getProperty("AdvancedDiagonalStrategy", "true"));
			DIAGONAL_WEIGHT = Float.parseFloat(geoData.getProperty("DiagonalWeight", "0.707f"));
			MAX_POSTFILTER_PASSES = Integer.parseInt(geoData.getProperty("MaxPostfilterPasses", "3"));
			DEBUG_PATH = Boolean.parseBoolean(geoData.getProperty("DebugPath", "false"));
			FORCE_GEODATA = Boolean.parseBoolean(geoData.getProperty("ForceGeoData", "true"));
			COORD_SYNCHRONIZE = Integer.parseInt(geoData.getProperty("CoordSynchronize", "-1"));
			GEODATA_PATH = Paths.get(geoData.getProperty("GeoDataPath", "./data/geodata"));
			TRY_LOAD_UNSPECIFIED_REGIONS = Boolean.parseBoolean(geoData.getProperty("TryLoadUnspecifiedRegions", "true"));
			GEODATA_REGIONS = new HashMap<>();
			for (int regionX = L2World.TILE_X_MIN; regionX <= L2World.TILE_X_MAX; regionX++)
			{
				for (int regionY = L2World.TILE_Y_MIN; regionY <= L2World.TILE_Y_MAX; regionY++)
				{
					final String key = regionX + "_" + regionY;
					if (geoData.containsKey(regionX + "_" + regionY))
					{
						GEODATA_REGIONS.put(key, Boolean.parseBoolean(geoData.getProperty(key, "false")));
					}
				}
			}
			
			final Properties optionsSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(OPTIONS_FILE)))
			{
				optionsSettings.load(is);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + OPTIONS_FILE + " File.");
			}
			
			EVERYBODY_HAS_ADMIN_RIGHTS = Boolean.parseBoolean(optionsSettings.getProperty("EverybodyHasAdminRights", "false"));
			
			DEBUG = Boolean.parseBoolean(optionsSettings.getProperty("Debug", "false"));
			ASSERT = Boolean.parseBoolean(optionsSettings.getProperty("Assert", "false"));
			DEVELOPER = Boolean.parseBoolean(optionsSettings.getProperty("Developer", "false"));
			TEST_SERVER = Boolean.parseBoolean(optionsSettings.getProperty("TestServer", "false"));
			SERVER_LIST_TESTSERVER = Boolean.parseBoolean(optionsSettings.getProperty("TestServer", "false"));
			
			SERVER_LIST_BRACKET = Boolean.valueOf(optionsSettings.getProperty("ServerListBrackets", "false"));
			SERVER_LIST_CLOCK = Boolean.valueOf(optionsSettings.getProperty("ServerListClock", "false"));
			SERVER_GMONLY = Boolean.valueOf(optionsSettings.getProperty("ServerGMOnly", "false"));
			
			SKILL_CHECK_ENABLE = Boolean.valueOf(optionsSettings.getProperty("SkillCheckEnable", "False"));
			
			AUTODESTROY_ITEM_AFTER = Integer.parseInt(optionsSettings.getProperty("AutoDestroyDroppedItemAfter", "0"));
			PROTECTED_ITEMS = optionsSettings.getProperty("ListOfProtectedItems");
			LIST_PROTECTED_ITEMS = new FastList<>();
			for (final String id : PROTECTED_ITEMS.split(","))
			{
				LIST_PROTECTED_ITEMS.add(Integer.parseInt(id));
			}
			UPDATE_ITEMS_ON_CHAR_STORE = Boolean.parseBoolean(optionsSettings.getProperty("UpdateItemsOnCharStore", "false"));
			LAZY_ITEMS_UPDATE = Boolean.parseBoolean(optionsSettings.getProperty("LazyItemsUpdate", "false"));
			DESTROY_DROPPED_PLAYER_ITEM = Boolean.valueOf(optionsSettings.getProperty("DestroyPlayerDroppedItem", "false"));
			DESTROY_EQUIPABLE_PLAYER_ITEM = Boolean.valueOf(optionsSettings.getProperty("DestroyEquipableItem", "false"));
			SAVE_DROPPED_ITEM = Boolean.valueOf(optionsSettings.getProperty("SaveDroppedItem", "false"));
			EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD = Boolean.valueOf(optionsSettings.getProperty("EmptyDroppedItemTableAfterLoad", "false"));
			SAVE_DROPPED_ITEM_INTERVAL = Integer.parseInt(optionsSettings.getProperty("SaveDroppedItemInterval", "0")) * 60000;
			CLEAR_DROPPED_ITEM_TABLE = Boolean.valueOf(optionsSettings.getProperty("ClearDroppedItemTable", "false"));
			
			PRECISE_DROP_CALCULATION = Boolean.valueOf(optionsSettings.getProperty("PreciseDropCalculation", "True"));
			MULTIPLE_ITEM_DROP = Boolean.valueOf(optionsSettings.getProperty("MultipleItemDrop", "True"));
			
			final String str = optionsSettings.getProperty("EnableFallingDamage", "auto");
			ENABLE_FALLING_DAMAGE = "auto".equalsIgnoreCase(str) ? PATHFINDING > 0 : Boolean.parseBoolean(str);
			
			ALLOW_WAREHOUSE = Boolean.valueOf(optionsSettings.getProperty("AllowWarehouse", "True"));
			WAREHOUSE_CACHE = Boolean.valueOf(optionsSettings.getProperty("WarehouseCache", "False"));
			WAREHOUSE_CACHE_TIME = Integer.parseInt(optionsSettings.getProperty("WarehouseCacheTime", "15"));
			ALLOW_FREIGHT = Boolean.valueOf(optionsSettings.getProperty("AllowFreight", "True"));
			ALLOW_WEAR = Boolean.valueOf(optionsSettings.getProperty("AllowWear", "False"));
			WEAR_DELAY = Integer.parseInt(optionsSettings.getProperty("WearDelay", "5"));
			WEAR_PRICE = Integer.parseInt(optionsSettings.getProperty("WearPrice", "10"));
			ALLOW_LOTTERY = Boolean.valueOf(optionsSettings.getProperty("AllowLottery", "False"));
			ALLOW_RACE = Boolean.valueOf(optionsSettings.getProperty("AllowRace", "False"));
			ALLOW_WATER = Boolean.valueOf(optionsSettings.getProperty("AllowWater", "False"));
			ALLOW_RENTPET = Boolean.valueOf(optionsSettings.getProperty("AllowRentPet", "False"));
			ALLOW_DISCARDITEM = Boolean.valueOf(optionsSettings.getProperty("AllowDiscardItem", "True"));
			ALLOWFISHING = Boolean.valueOf(optionsSettings.getProperty("AllowFishing", "True"));
			ALLOW_BOAT = Boolean.valueOf(optionsSettings.getProperty("AllowBoat", "False"));
			
			ALLOW_L2WALKER = Boolean.valueOf(optionsSettings.getProperty("AllowL2Walker", "False"));
			ALLOW_MANOR = Boolean.valueOf(optionsSettings.getProperty("AllowManor", "True"));
			ALLOW_NPC_WALKERS = Boolean.valueOf(optionsSettings.getProperty("AllowNpcWalkers", "True"));
			ALLOW_PET_WALKERS = Boolean.valueOf(optionsSettings.getProperty("AllowPetWalkers", "True"));
			
			ACTIVATE_POSITION_RECORDER = Boolean.valueOf(optionsSettings.getProperty("ActivatePositionRecorder", "False"));
			
			DEFAULT_GLOBAL_CHAT = optionsSettings.getProperty("GlobalChat", "ON");
			DEFAULT_TRADE_CHAT = optionsSettings.getProperty("TradeChat", "ON");
			
			LOG_CHAT = Boolean.valueOf(optionsSettings.getProperty("LogChat", "False"));
			LOG_ITEMS = Boolean.valueOf(optionsSettings.getProperty("LogItems", "false"));
			
			GMAUDIT = Boolean.valueOf(optionsSettings.getProperty("GMAudit", "False"));
			
			COMMUNITY_TYPE = Integer.parseInt(optionsSettings.getProperty("CommunityType", "1"));
			BBS_SHOW_PLAYERLIST = Boolean.valueOf(optionsSettings.getProperty("BBSShowPlayerList", "False"));
			BBS_DEFAULT = optionsSettings.getProperty("BBSDefault", "_bbshome");
			SHOW_LEVEL_COMMUNITYBOARD = Boolean.valueOf(optionsSettings.getProperty("ShowLevelOnCommunityBoard", "False"));
			SHOW_STATUS_COMMUNITYBOARD = Boolean.valueOf(optionsSettings.getProperty("ShowStatusOnCommunityBoard", "True"));
			NAME_PAGE_SIZE_COMMUNITYBOARD = Integer.parseInt(optionsSettings.getProperty("NamePageSizeOnCommunityBoard", "50"));
			NAME_PER_ROW_COMMUNITYBOARD = Integer.parseInt(optionsSettings.getProperty("NamePerRowOnCommunityBoard", "5"));
			
			ZONE_TOWN = Integer.parseInt(optionsSettings.getProperty("ZoneTown", "0"));
			
			MAX_DRIFT_RANGE = Integer.parseInt(optionsSettings.getProperty("MaxDriftRange", "300"));
			
			MIN_NPC_ANIMATION = Integer.parseInt(optionsSettings.getProperty("MinNPCAnimation", "10"));
			MAX_NPC_ANIMATION = Integer.parseInt(optionsSettings.getProperty("MaxNPCAnimation", "20"));
			
			MIN_MONSTER_ANIMATION = Integer.parseInt(optionsSettings.getProperty("MinMonsterAnimation", "5"));
			MAX_MONSTER_ANIMATION = Integer.parseInt(optionsSettings.getProperty("MaxMonsterAnimation", "20"));
			
			KNOWNLIST_UPDATE_INTERVAL = Long.parseLong(optionsSettings.getProperty("KnownListUpdateInterval", "1250"));
			
			SHOW_NPC_LVL = Boolean.valueOf(optionsSettings.getProperty("ShowNpcLevel", "False"));
			
			FORCE_INVENTORY_UPDATE = Boolean.valueOf(optionsSettings.getProperty("ForceInventoryUpdate", "False"));
			
			AUTODELETE_INVALID_QUEST_DATA = Boolean.valueOf(optionsSettings.getProperty("AutoDeleteInvalidQuestData", "False"));
			
			THREAD_P_EFFECTS = Integer.parseInt(optionsSettings.getProperty("ThreadPoolSizeEffects", "6"));
			THREAD_P_GENERAL = Integer.parseInt(optionsSettings.getProperty("ThreadPoolSizeGeneral", "15"));
			GENERAL_PACKET_THREAD_CORE_SIZE = Integer.parseInt(optionsSettings.getProperty("GeneralPacketThreadCoreSize", "4"));
			IO_PACKET_THREAD_CORE_SIZE = Integer.parseInt(optionsSettings.getProperty("UrgentPacketThreadCoreSize", "2"));
			AI_MAX_THREAD = Integer.parseInt(optionsSettings.getProperty("AiMaxThread", "10"));
			GENERAL_THREAD_CORE_SIZE = Integer.parseInt(optionsSettings.getProperty("GeneralThreadCoreSize", "4"));
			
			DELETE_DAYS = Integer.parseInt(optionsSettings.getProperty("DeleteCharAfterDays", "7"));
			
			DEFAULT_PUNISH = Integer.parseInt(optionsSettings.getProperty("DefaultPunish", "2"));
			DEFAULT_PUNISH_PARAM = Integer.parseInt(optionsSettings.getProperty("DefaultPunishParam", "0"));
			
			LAZY_CACHE = Boolean.valueOf(optionsSettings.getProperty("LazyCache", "False"));
			
			PACKET_LIFETIME = Integer.parseInt(optionsSettings.getProperty("PacketLifeTime", "0"));
			
			DEADLOCK_DETECTOR = Boolean.valueOf(optionsSettings.getProperty("DeadLockDetector", "False"));
			DEADLOCK_CHECK_INTERVAL = Integer.parseInt(optionsSettings.getProperty("DeadLockCheckInterval", "20"));
			RESTART_ON_DEADLOCK = Boolean.valueOf(optionsSettings.getProperty("RestartOnDeadlock", "False"));
			
			BYPASS_VALIDATION = Boolean.valueOf(optionsSettings.getProperty("BypassValidation", "True"));
			
			ONLY_GM_ITEMS_FREE = Boolean.valueOf(optionsSettings.getProperty("OnlyGMItemsFree", "True"));
			
			GAMEGUARD_ENFORCE = Boolean.valueOf(optionsSettings.getProperty("GameGuardEnforce", "False"));
			GAMEGUARD_PROHIBITACTION = Boolean.valueOf(optionsSettings.getProperty("GameGuardProhibitAction", "False"));
			
			GRIDS_ALWAYS_ON = Boolean.parseBoolean(optionsSettings.getProperty("GridsAlwaysOn", "False"));
			GRID_NEIGHBOR_TURNON_TIME = Integer.parseInt(optionsSettings.getProperty("GridNeighborTurnOnTime", "1"));
			GRID_NEIGHBOR_TURNOFF_TIME = Integer.parseInt(optionsSettings.getProperty("GridNeighborTurnOffTime", "90"));
			
			// ---------------------------------------------------
			// Configuration values not found in config files
			// ---------------------------------------------------
			
			USE_3D_MAP = Boolean.valueOf(optionsSettings.getProperty("Use3DMap", "False"));
			
			PATH_NODE_RADIUS = Integer.parseInt(optionsSettings.getProperty("PathNodeRadius", "50"));
			NEW_NODE_ID = Integer.parseInt(optionsSettings.getProperty("NewNodeId", "7952"));
			SELECTED_NODE_ID = Integer.parseInt(optionsSettings.getProperty("NewNodeId", "7952"));
			LINKED_NODE_ID = Integer.parseInt(optionsSettings.getProperty("NewNodeId", "7952"));
			NEW_NODE_TYPE = optionsSettings.getProperty("NewNodeType", "npc");
			
			MINIMUM_UPDATE_DISTANCE = Integer.parseInt(optionsSettings.getProperty("MaximumUpdateDistance", "50"));
			MINIMUN_UPDATE_TIME = Integer.parseInt(optionsSettings.getProperty("MinimumUpdateTime", "500"));
			CHECK_KNOWN = Boolean.valueOf(optionsSettings.getProperty("CheckKnownList", "false"));
			KNOWNLIST_FORGET_DELAY = Integer.parseInt(optionsSettings.getProperty("KnownListForgetDelay", "10000"));
			
			// telnet
			final Properties telnetSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(TELNET_FILE)))
			{
				telnetSettings.load(is);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + TELNET_FILE + " File.");
			}
			
			IS_TELNET_ENABLED = Boolean.valueOf(telnetSettings.getProperty("EnableTelnet", "False"));
			TELNET_PORT = Integer.parseInt(telnetSettings.getProperty("GameStatusPort", "54321"));
			
			// MMO
			final Properties mmoSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(MMO_CONFIG_FILE)))
			{
				mmoSettings.load(is);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + MMO_CONFIG_FILE + " File.");
			}
			
			MMO_SELECTOR_SLEEP_TIME = Integer.parseInt(mmoSettings.getProperty("SleepTime", "20"));
			MMO_MAX_SEND_PER_PASS = Integer.parseInt(mmoSettings.getProperty("MaxSendPerPass", "12"));
			MMO_MAX_READ_PER_PASS = Integer.parseInt(mmoSettings.getProperty("MaxReadPerPass", "12"));
			MMO_HELPER_BUFFER_COUNT = Integer.parseInt(mmoSettings.getProperty("HelperBufferCount", "20"));
			
			// id factory
			final Properties idSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(ID_CONFIG_FILE)))
			{
				idSettings.load(is);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + ID_CONFIG_FILE + " File.");
			}
			
			MAP_TYPE = ObjectMapType.valueOf(idSettings.getProperty("L2Map", "WorldObjectMap"));
			SET_TYPE = ObjectSetType.valueOf(idSettings.getProperty("L2Set", "WorldObjectSet"));
			IDFACTORY_TYPE = IdFactoryType.valueOf(idSettings.getProperty("IDFactory", "Compaction"));
			BAD_ID_CHECKING = Boolean.valueOf(idSettings.getProperty("BadIdChecking", "True"));
			
			// Load FloodProtector Properties file
			final Properties security = new Properties();
			try (InputStream is = new FileInputStream(new File(FLOOD_PROTECTOR_FILE)))
			{
				security.load(is);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + FLOOD_PROTECTOR_FILE + " File.");
			}
			
			loadFloodProtectorConfigs(security);
			
			// other
			final Properties otherSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(OTHER_CONFIG_FILE)))
			{
				otherSettings.load(is);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + OTHER_CONFIG_FILE + " File.");
			}
			
			DEEPBLUE_DROP_RULES = Boolean.parseBoolean(otherSettings.getProperty("UseDeepBlueDropRules", "True"));
			GUARD_ATTACK_AGGRO_MOB = Boolean.valueOf(otherSettings.getProperty("GuardAttackAggroMob", "False"));
			EFFECT_CANCELING = Boolean.valueOf(otherSettings.getProperty("CancelLesserEffect", "True"));
			ALLOW_WYVERN_UPGRADER = Boolean.valueOf(otherSettings.getProperty("AllowWyvernUpgrader", "False"));
			
			/* Inventory slots limits */
			INVENTORY_MAXIMUM_NO_DWARF = Integer.parseInt(otherSettings.getProperty("MaximumSlotsForNoDwarf", "80"));
			INVENTORY_MAXIMUM_DWARF = Integer.parseInt(otherSettings.getProperty("MaximumSlotsForDwarf", "100"));
			INVENTORY_MAXIMUM_GM = Integer.parseInt(otherSettings.getProperty("MaximumSlotsForGMPlayer", "250"));
			
			INVENTORY_MAXIMUM_PET = Integer.parseInt(otherSettings.getProperty("MaximumSlotsForPet", "12"));
			MAX_ITEM_IN_PACKET = Math.max(INVENTORY_MAXIMUM_NO_DWARF, Math.max(INVENTORY_MAXIMUM_DWARF, INVENTORY_MAXIMUM_GM));
			
			/* Warehouse slots limits */
			WAREHOUSE_SLOTS_NO_DWARF = Integer.parseInt(otherSettings.getProperty("MaximumWarehouseSlotsForNoDwarf", "100"));
			WAREHOUSE_SLOTS_DWARF = Integer.parseInt(otherSettings.getProperty("MaximumWarehouseSlotsForDwarf", "120"));
			WAREHOUSE_SLOTS_CLAN = Integer.parseInt(otherSettings.getProperty("MaximumWarehouseSlotsForClan", "200"));
			FREIGHT_SLOTS = Integer.parseInt(otherSettings.getProperty("MaximumFreightSlots", "20"));
			
			/* chance to enchant an item over +3 */
			ENCHANT_CHANCE_WEAPON = Integer.parseInt(otherSettings.getProperty("EnchantChanceWeapon", "68"));
			ENCHANT_CHANCE_ARMOR = Integer.parseInt(otherSettings.getProperty("EnchantChanceArmor", "52"));
			ENCHANT_CHANCE_JEWELRY = Integer.parseInt(otherSettings.getProperty("EnchantChanceJewelry", "54"));
			BLESSED_ENCHANT_CHANCE_WEAPON = Integer.parseInt(otherSettings.getProperty("BlessedEnchantChanceWeapon", "68"));
			BLESSED_ENCHANT_CHANCE_ARMOR = Integer.parseInt(otherSettings.getProperty("BlessedEnchantChanceArmor", "52"));
			BLESSED_ENCHANT_CHANCE_JEWELRY = Integer.parseInt(otherSettings.getProperty("BlessedEnchantChanceJewelry", "54"));
			/* limit on enchant */
			ENCHANT_MAX_WEAPON = Integer.parseInt(otherSettings.getProperty("EnchantMaxWeapon", "25"));
			ENCHANT_MAX_ARMOR = Integer.parseInt(otherSettings.getProperty("EnchantMaxArmor", "25"));
			ENCHANT_MAX_JEWELRY = Integer.parseInt(otherSettings.getProperty("EnchantMaxJewelry", "25"));
			/* limit of safe enchant normal */
			ENCHANT_SAFE_MAX = Integer.parseInt(otherSettings.getProperty("EnchantSafeMax", "3"));
			/* limit of safe enchant full */
			ENCHANT_SAFE_MAX_FULL = Integer.parseInt(otherSettings.getProperty("EnchantSafeMaxFull", "4"));
			
			/* if different from 100 (ie 100%) heal rate is modified acordingly */
			HP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("HpRegenMultiplier", "100")) / 100;
			MP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("MpRegenMultiplier", "100")) / 100;
			CP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("CpRegenMultiplier", "100")) / 100;
			
			RAID_HP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("RaidHpRegenMultiplier", "100")) / 100;
			RAID_MP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("RaidMpRegenMultiplier", "100")) / 100;
			RAID_PDEFENCE_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("RaidPDefenceMultiplier", "100")) / 100;
			RAID_MDEFENCE_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("RaidMDefenceMultiplier", "100")) / 100;
			RAID_MINION_RESPAWN_TIMER = Integer.parseInt(otherSettings.getProperty("RaidMinionRespawnTime", "300000"));
			RAID_MIN_RESPAWN_MULTIPLIER = Float.parseFloat(otherSettings.getProperty("RaidMinRespawnMultiplier", "1.0"));
			RAID_MAX_RESPAWN_MULTIPLIER = Float.parseFloat(otherSettings.getProperty("RaidMaxRespawnMultiplier", "1.0"));
			
			STARTING_ADENA = Integer.parseInt(otherSettings.getProperty("StartingAdena", "0"));
			STARTING_LEVEL = Byte.parseByte(otherSettings.getProperty("StartingLevel", "1"));
			if (STARTING_LEVEL > (Experience.MAX_LEVEL - 1))
			{
				STARTING_LEVEL = Experience.MAX_LEVEL - 1;
			}
			STARTING_SUB_LEVEL = Byte.parseByte(otherSettings.getProperty("StartingSubclassLevel", "40"));
			
			if (STARTING_SUB_LEVEL > (Experience.MAX_LEVEL - 1))
			{
				STARTING_SUB_LEVEL = Experience.MAX_LEVEL - 1;
			}
			
			MAX_RUN_SPEED = Integer.parseInt(otherSettings.getProperty("MaxRunSpeed", "250"));
			MAX_PCRIT_RATE = Integer.parseInt(otherSettings.getProperty("MaxPCritRate", "500"));
			MAX_MCRIT_RATE = Integer.parseInt(otherSettings.getProperty("MaxMCritRate", "300"));
			MAX_PATK_SPEED = Integer.parseInt(otherSettings.getProperty("MaxPAtkSpeed", "1500"));
			MAX_MATK_SPEED = Integer.parseInt(otherSettings.getProperty("MaxMAtkSpeed", "1999"));
			
			UNSTUCK_INTERVAL = Integer.parseInt(otherSettings.getProperty("UnstuckInterval", "300"));
			
			/* Player protection after teleport or login */
			PLAYER_SPAWN_PROTECTION = Integer.parseInt(otherSettings.getProperty("PlayerSpawnProtection", "0"));
			
			/* Player protection after recovering from fake death (works against mobs only) */
			PLAYER_FAKEDEATH_UP_PROTECTION = Integer.parseInt(otherSettings.getProperty("PlayerFakeDeathUpProtection", "0"));
			
			/* Defines some Party XP related values */
			PARTY_XP_CUTOFF_METHOD = otherSettings.getProperty("PartyXpCutoffMethod", "percentage");
			PARTY_XP_CUTOFF_PERCENT = Double.parseDouble(otherSettings.getProperty("PartyXpCutoffPercent", "3."));
			PARTY_XP_CUTOFF_LEVEL = Integer.parseInt(otherSettings.getProperty("PartyXpCutoffLevel", "30"));
			
			/* Amount of HP that is restored */
			RESPAWN_RESTORE_HP = Double.parseDouble(otherSettings.getProperty("RespawnRestoreHP", "70")) / 100;
			
			RESPAWN_RANDOM_ENABLED = Boolean.parseBoolean(otherSettings.getProperty("RespawnRandomOffset", "True"));
			RESPAWN_RANDOM_MAX_OFFSET = Integer.parseInt(otherSettings.getProperty("RespawnRandomMaxOffset", "20"));
			
			/* Maximum number of available slots for pvt stores */
			MAX_PVTSTORE_SLOTS_DWARF = Integer.parseInt(otherSettings.getProperty("MaxPvtStoreSlotsDwarf", "5"));
			MAX_PVTSTORE_SLOTS_OTHER = Integer.parseInt(otherSettings.getProperty("MaxPvtStoreSlotsOther", "4"));
			
			STORE_SKILL_COOLTIME = Boolean.parseBoolean(otherSettings.getProperty("StoreSkillCooltime", "true"));
			
			PET_RENT_NPC = otherSettings.getProperty("ListPetRentNpc", "7827");
			LIST_PET_RENT_NPC = new FastList<>();
			for (final String id : PET_RENT_NPC.split(","))
			{
				LIST_PET_RENT_NPC.add(Integer.parseInt(id));
			}
			NONDROPPABLE_ITEMS = otherSettings.getProperty("ListOfNonDroppableItems", "1147,425,1146,461,10,2368,7,6,2370,2369,5598");
			
			LIST_NONDROPPABLE_ITEMS = new FastList<>();
			for (final String id : NONDROPPABLE_ITEMS.split(","))
			{
				LIST_NONDROPPABLE_ITEMS.add(Integer.parseInt(id));
			}
			
			ANNOUNCE_MAMMON_SPAWN = Boolean.parseBoolean(otherSettings.getProperty("AnnounceMammonSpawn", "True"));
			
			ALT_PRIVILEGES_ADMIN = Boolean.parseBoolean(otherSettings.getProperty("AltPrivilegesAdmin", "False"));
			ALT_PRIVILEGES_SECURE_CHECK = Boolean.parseBoolean(otherSettings.getProperty("AltPrivilegesSecureCheck", "True"));
			ALT_PRIVILEGES_DEFAULT_LEVEL = Integer.parseInt(otherSettings.getProperty("AltPrivilegesDefaultLevel", "100"));
			
			GM_NAME_COLOR_ENABLED = Boolean.parseBoolean(otherSettings.getProperty("GMNameColorEnabled", "False"));
			GM_NAME_COLOR = Integer.decode("0x" + otherSettings.getProperty("GMNameColor", "FFFF00"));
			ADMIN_NAME_COLOR = Integer.decode("0x" + otherSettings.getProperty("AdminNameColor", "00FF00"));
			GM_HERO_AURA = Boolean.parseBoolean(otherSettings.getProperty("GMHeroAura", "True"));
			GM_STARTUP_INVULNERABLE = Boolean.parseBoolean(otherSettings.getProperty("GMStartupInvulnerable", "True"));
			GM_STARTUP_INVISIBLE = Boolean.parseBoolean(otherSettings.getProperty("GMStartupInvisible", "True"));
			GM_STARTUP_SILENCE = Boolean.parseBoolean(otherSettings.getProperty("GMStartupSilence", "True"));
			GM_STARTUP_AUTO_LIST = Boolean.parseBoolean(otherSettings.getProperty("GMStartupAutoList", "True"));
			
			PETITIONING_ALLOWED = Boolean.parseBoolean(otherSettings.getProperty("PetitioningAllowed", "True"));
			MAX_PETITIONS_PER_PLAYER = Integer.parseInt(otherSettings.getProperty("MaxPetitionsPerPlayer", "5"));
			MAX_PETITIONS_PENDING = Integer.parseInt(otherSettings.getProperty("MaxPetitionsPending", "25"));
			
			JAIL_IS_PVP = Boolean.valueOf(otherSettings.getProperty("JailIsPvp", "True"));
			JAIL_DISABLE_CHAT = Boolean.valueOf(otherSettings.getProperty("JailDisableChat", "True"));
			
			// rates
			final Properties ratesSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(RATES_CONFIG_FILE)))
			{
				ratesSettings.load(is);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + RATES_CONFIG_FILE + " File.");
			}
			
			RATE_XP = Float.parseFloat(ratesSettings.getProperty("RateXp", "1."));
			RATE_SP = Float.parseFloat(ratesSettings.getProperty("RateSp", "1."));
			RATE_PARTY_XP = Float.parseFloat(ratesSettings.getProperty("RatePartyXp", "1."));
			RATE_PARTY_SP = Float.parseFloat(ratesSettings.getProperty("RatePartySp", "1."));
			RATE_QUESTS_REWARD = Float.parseFloat(ratesSettings.getProperty("RateQuestsReward", "1."));
			RATE_DROP_ADENA = Float.parseFloat(ratesSettings.getProperty("RateDropAdena", "1."));
			RATE_CONSUMABLE_COST = Float.parseFloat(ratesSettings.getProperty("RateConsumableCost", "1."));
			RATE_DROP_ITEMS = Float.parseFloat(ratesSettings.getProperty("RateDropItems", "1."));
			RATE_BOSS_DROP_ITEMS = Float.parseFloat(ratesSettings.getProperty("RateBossDropItems", "1."));
			RATE_DROP_SPOIL = Float.parseFloat(ratesSettings.getProperty("RateDropSpoil", "1."));
			RATE_DROP_MANOR = Integer.parseInt(ratesSettings.getProperty("RateDropManor", "1"));
			RATE_DROP_QUEST = Float.parseFloat(ratesSettings.getProperty("RateDropQuest", "1."));
			RATE_KARMA_EXP_LOST = Float.parseFloat(ratesSettings.getProperty("RateKarmaExpLost", "1."));
			RATE_SIEGE_GUARDS_PRICE = Float.parseFloat(ratesSettings.getProperty("RateSiegeGuardsPrice", "1."));
			
			PLAYER_DROP_LIMIT = Integer.parseInt(ratesSettings.getProperty("PlayerDropLimit", "3"));
			PLAYER_RATE_DROP = Integer.parseInt(ratesSettings.getProperty("PlayerRateDrop", "5"));
			PLAYER_RATE_DROP_ITEM = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropItem", "70"));
			PLAYER_RATE_DROP_EQUIP = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropEquip", "25"));
			PLAYER_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropEquipWeapon", "5"));
			
			PET_XP_RATE = Float.parseFloat(ratesSettings.getProperty("PetXpRate", "1."));
			PET_FOOD_RATE = Integer.parseInt(ratesSettings.getProperty("PetFoodRate", "1"));
			SINEATER_XP_RATE = Float.parseFloat(ratesSettings.getProperty("SinEaterXpRate", "1."));
			
			KARMA_DROP_LIMIT = Integer.parseInt(ratesSettings.getProperty("KarmaDropLimit", "10"));
			KARMA_RATE_DROP = Integer.parseInt(ratesSettings.getProperty("KarmaRateDrop", "70"));
			KARMA_RATE_DROP_ITEM = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropItem", "50"));
			KARMA_RATE_DROP_EQUIP = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropEquip", "40"));
			KARMA_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropEquipWeapon", "10"));
			
			CAN_SPOIL_LOWER_LEVEL_MOBS = Boolean.parseBoolean(ratesSettings.getProperty("CanSpoilLowerLevelMobs", "false"));
			CAN_DELEVEL_AND_SPOIL_MOBS = Boolean.parseBoolean(ratesSettings.getProperty("CanDelevelToSpoil", "true"));
			MAXIMUM_PLAYER_AND_MOB_LEVEL_DIFFERENCE = Float.parseFloat(ratesSettings.getProperty("MaximumPlayerAndMobLevelDifference", "9."));
			BASE_SPOIL_RATE = Float.parseFloat(ratesSettings.getProperty("BasePercentChanceOfSpoilSuccess", "40."));
			MINIMUM_SPOIL_RATE = Float.parseFloat(ratesSettings.getProperty("MinimumPercentChanceOfSpoilSuccess", "3."));
			SPOIL_LEVEL_DIFFERENCE_LIMIT = Float.parseFloat(ratesSettings.getProperty("SpoilLevelDifferenceLimit", "5."));
			SPOIL_LEVEL_DIFFERENCE_MULTIPLIER = Float.parseFloat(ratesSettings.getProperty("SpoilLevelMultiplier", "7."));
			LAST_LEVEL_SPOIL_IS_LEARNED = Integer.parseInt(ratesSettings.getProperty("LastLevelSpoilIsLearned", "72"));
			
			// alternative settings
			final Properties altSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(ALT_SETTINGS_FILE)))
			{
				altSettings.load(is);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + ALT_SETTINGS_FILE + " File.");
			}
			
			ALT_GAME_TIREDNESS = Boolean.parseBoolean(altSettings.getProperty("AltGameTiredness", "false"));
			ALT_GAME_CREATION = Boolean.parseBoolean(altSettings.getProperty("AltGameCreation", "false"));
			ALT_GAME_CREATION_SPEED = Double.parseDouble(altSettings.getProperty("AltGameCreationSpeed", "1"));
			ALT_GAME_CREATION_XP_RATE = Double.parseDouble(altSettings.getProperty("AltGameCreationRateXp", "1"));
			ALT_GAME_CREATION_SP_RATE = Double.parseDouble(altSettings.getProperty("AltGameCreationRateSp", "1"));
			ALT_GAME_SKILL_LEARN = Boolean.parseBoolean(altSettings.getProperty("AltGameSkillLearn", "false"));
			AUTO_LEARN_SKILLS = Boolean.parseBoolean(altSettings.getProperty("AutoLearnSkills", "false"));
			AUTO_LEARN_3RD_SKILLS = Boolean.parseBoolean(altSettings.getProperty("AutoLearn3rdClassSkills", "false"));
			ALT_GAME_CANCEL_BOW = altSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("bow") || altSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("all");
			ALT_GAME_CANCEL_CAST = altSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("cast") || altSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("all");
			ALT_PERFECT_SHLD_BLOCK = Integer.parseInt(altSettings.getProperty("AltPerfectShieldBlockRate", "5"));
			ALT_GAME_DELEVEL = Boolean.parseBoolean(altSettings.getProperty("Delevel", "true"));
			ALT_WEIGHT_LIMIT = Double.parseDouble(altSettings.getProperty("AltWeightLimit", "1"));
			ALT_GAME_MAGICFAILURES = Boolean.parseBoolean(altSettings.getProperty("MagicFailures", "false"));
			ALT_MOB_AGGRO_IN_PEACEZONE = Boolean.parseBoolean(altSettings.getProperty("AltMobAggroInPeaceZone", "true"));
			ALT_GAME_EXPONENT_XP = Float.parseFloat(altSettings.getProperty("AltGameExponentXp", "0."));
			ALT_GAME_EXPONENT_SP = Float.parseFloat(altSettings.getProperty("AltGameExponentSp", "0."));
			ALT_GAME_FREIGHTS = Boolean.parseBoolean(altSettings.getProperty("AltGameFreights", "false"));
			ALT_GAME_FREIGHT_PRICE = Integer.parseInt(altSettings.getProperty("AltGameFreightPrice", "1000"));
			ALT_PARTY_RANGE = Integer.parseInt(altSettings.getProperty("AltPartyRange", "1600"));
			ALT_PARTY_RANGE2 = Integer.parseInt(altSettings.getProperty("AltPartyRange2", "1400"));
			IS_CRAFTING_ENABLED = Boolean.parseBoolean(altSettings.getProperty("CraftingEnabled", "true"));
			SP_BOOK_NEEDED = Boolean.parseBoolean(altSettings.getProperty("SpBookNeeded", "true"));
			ES_SP_BOOK_NEEDED = Boolean.parseBoolean(altSettings.getProperty("EnchantSkillSpBookNeeded", "true"));
			AUTO_LOOT = Boolean.parseBoolean(altSettings.getProperty("AutoLoot", "false"));
			AUTO_LOOT_RAIDS = Boolean.parseBoolean(altSettings.getProperty("AutoLootRaids", "false"));
			ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanBeKilledInPeaceZone", "false"));
			ALT_GAME_KARMA_PLAYER_CAN_SHOP = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanShop", "true"));
			ALT_GAME_KARMA_PLAYER_CAN_USE_GK = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanUseGK", "false"));
			ALT_GAME_KARMA_PLAYER_CAN_TELEPORT = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanTeleport", "true"));
			ALT_GAME_KARMA_PLAYER_CAN_TRADE = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanTrade", "true"));
			ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanUseWareHouse", "true"));
			ALT_GAME_FREE_TELEPORT = Boolean.parseBoolean(altSettings.getProperty("AltFreeTeleporting", "False"));
			ALT_RECOMMEND = Boolean.parseBoolean(altSettings.getProperty("AltRecommend", "False"));
			ALT_MAX_SUBCLASS = Integer.parseInt(altSettings.getProperty("AltMaxSubClasses", "3"));
			ALT_GAME_SUBCLASS_WITHOUT_QUESTS = Boolean.parseBoolean(altSettings.getProperty("AltSubClassWithoutQuests", "False"));
			ALT_ENABLE_TUTORIAL = Boolean.parseBoolean(altSettings.getProperty("AltEnableTutorial", "True"));
			ALT_GAME_VIEWNPC = Boolean.parseBoolean(altSettings.getProperty("AltGameViewNpc", "False"));
			ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE = Boolean.parseBoolean(altSettings.getProperty("AltNewCharAlwaysIsNewbie", "False"));
			DWARF_RECIPE_LIMIT = Integer.parseInt(altSettings.getProperty("DwarfRecipeLimit", "50"));
			COMMON_RECIPE_LIMIT = Integer.parseInt(altSettings.getProperty("CommonRecipeLimit", "50"));
			
			ALT_CLAN_MEMBERS_FOR_WAR = Integer.parseInt(altSettings.getProperty("AltClanMembersForWar", "15"));
			ALT_CLAN_JOIN_DAYS = Integer.parseInt(altSettings.getProperty("DaysBeforeJoinAClan", "5"));
			ALT_CLAN_CREATE_DAYS = Integer.parseInt(altSettings.getProperty("DaysBeforeCreateAClan", "10"));
			
			ALT_CLAN_DISSOLVE_DAYS = Integer.parseInt(altSettings.getProperty("DaysToPassToDissolveAClan", "7"));
			ALT_RECOVERY_PENALTY = Integer.parseInt(altSettings.getProperty("DaysToPassToDissolveAgain", "7"));
			
			ALT_ALLY_JOIN_DAYS_WHEN_LEAVED = Integer.parseInt(altSettings.getProperty("DaysBeforeJoinAllyWhenLeaved", "1"));
			ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED = Integer.parseInt(altSettings.getProperty("DaysBeforeJoinAllyWhenDismissed", "1"));
			ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED = Integer.parseInt(altSettings.getProperty("DaysBeforeAcceptNewClanWhenDismissed", "1"));
			ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED = Integer.parseInt(altSettings.getProperty("DaysBeforeCreateNewAllyWhenDissolved", "10"));
			ALT_MAX_NUM_OF_CLANS_IN_ALLY = Integer.parseInt(altSettings.getProperty("AltMaxNumOfClansInAlly", "12"));
			
			ALT_CHANNEL_ACTIVATION_COUNT = Integer.parseInt(altSettings.getProperty("AltChannelActivationCount", "5"));
			// Just in case admins set it to less than 2 parties
			if (ALT_CHANNEL_ACTIVATION_COUNT < 2)
			{
				ALT_CHANNEL_ACTIVATION_COUNT = 2;
			}
			
			ALT_OLY_START_TIME = Integer.parseInt(altSettings.getProperty("AltOlyStartTime", "20"));
			ALT_OLY_MIN = Integer.parseInt(altSettings.getProperty("AltOlyMin", "00"));
			ALT_OLY_CPERIOD = Long.parseLong(altSettings.getProperty("AltOlyCPeriod", "14400000"));
			ALT_OLY_BATTLE = Long.parseLong(altSettings.getProperty("AltOlyBattle", "180000"));
			ALT_OLY_BWAIT = Long.parseLong(altSettings.getProperty("AltOlyBWait", "600000"));
			ALT_OLY_IWAIT = Long.parseLong(altSettings.getProperty("AltOlyIWait", "300000"));
			ALT_OLY_WPERIOD = Long.parseLong(altSettings.getProperty("AltOlyWPeriod", "604800000"));
			ALT_OLY_VPERIOD = Long.parseLong(altSettings.getProperty("AltOlyVPeriod", "86400000"));
			
			ALT_OLY_CLASSED = Integer.parseInt(altSettings.getProperty("AltOlyClassedParticipants", "5"));
			
			ALT_OLY_NONCLASSED = Integer.parseInt(altSettings.getProperty("AltOlyNonClassedParticipants", "9"));
			ALT_OLY_BATTLE_REWARD_ITEM = Integer.parseInt(altSettings.getProperty("AltOlyBattleRewItem", "6651"));
			ALT_OLY_CLASSED_RITEM_C = Integer.parseInt(altSettings.getProperty("AltOlyClassedRewItemCount", "50"));
			ALT_OLY_NONCLASSED_RITEM_C = Integer.parseInt(altSettings.getProperty("AltOlyNonClassedRewItemCount", "30"));
			ALT_OLY_COMP_RITEM = Integer.parseInt(altSettings.getProperty("AltOlyCompRewItem", "6651"));
			ALT_OLY_GP_PER_POINT = Integer.parseInt(altSettings.getProperty("AltOlyGPPerPoint", "1000"));
			ALT_OLY_MIN_POINT_FOR_EXCH = Integer.parseInt(altSettings.getProperty("AltOlyMinPointForExchange", "50"));
			ALT_OLY_HERO_POINTS = Integer.parseInt(altSettings.getProperty("AltOlyHeroPoints", "300"));
			
			ALT_MANOR_REFRESH_TIME = Integer.parseInt(altSettings.getProperty("AltManorRefreshTime", "20"));
			ALT_MANOR_REFRESH_MIN = Integer.parseInt(altSettings.getProperty("AltManorRefreshMin", "00"));
			ALT_MANOR_APPROVE_TIME = Integer.parseInt(altSettings.getProperty("AltManorApproveTime", "6"));
			ALT_MANOR_APPROVE_MIN = Integer.parseInt(altSettings.getProperty("AltManorApproveMin", "00"));
			ALT_MANOR_MAINTENANCE_PERIOD = Integer.parseInt(altSettings.getProperty("AltManorMaintenancePeriod", "360000"));
			ALT_MANOR_SAVE_ALL_ACTIONS = Boolean.parseBoolean(altSettings.getProperty("AltManorSaveAllActions", "True"));
			ALT_MANOR_SAVE_PERIOD_RATE = Integer.parseInt(altSettings.getProperty("AltManorSavePeriodRate", "2"));
			
			ALT_LOTTERY_PRIZE = Integer.parseInt(altSettings.getProperty("AltLotteryPrize", "50000"));
			ALT_LOTTERY_TICKET_PRICE = Integer.parseInt(altSettings.getProperty("AltLotteryTicketPrice", "2000"));
			ALT_LOTTERY_5_NUMBER_RATE = Float.parseFloat(altSettings.getProperty("AltLottery5NumberRate", "0.6"));
			ALT_LOTTERY_4_NUMBER_RATE = Float.parseFloat(altSettings.getProperty("AltLottery4NumberRate", "0.2"));
			ALT_LOTTERY_3_NUMBER_RATE = Float.parseFloat(altSettings.getProperty("AltLottery3NumberRate", "0.2"));
			ALT_LOTTERY_2_AND_1_NUMBER_PRIZE = Integer.parseInt(altSettings.getProperty("AltLottery2and1NumberPrize", "200"));
			BUFFS_MAX_AMOUNT = Byte.parseByte(altSettings.getProperty("maxbuffamount", "20"));
			ALT_DEV_NO_QUESTS = Boolean.parseBoolean(altSettings.getProperty("AltDevNoQuests", "False"));
			ALT_DEV_NO_SPAWNS = Boolean.parseBoolean(altSettings.getProperty("AltDevNoSpawns", "False"));
			
			// Four Sepulcher Config
			FS_PENDING_TIME = Integer.parseInt(altSettings.getProperty("PendingTime", "50"));
			FS_ENTRY_TIME = Integer.parseInt(altSettings.getProperty("EntryTime", "5"));
			FS_PARTY_MEMBER_COUNT = Integer.parseInt(altSettings.getProperty("NumberOfNecessaryPartyMembers", "4"));
			
			if (FS_PENDING_TIME <= 0)
			{
				FS_PENDING_TIME = 50;
			}
			if (FS_ENTRY_TIME <= 0)
			{
				FS_ENTRY_TIME = 5;
			}
			if (FS_PARTY_MEMBER_COUNT <= 0)
			{
				FS_PARTY_MEMBER_COUNT = 4;
			}
			
			// Dimensional Rift Config
			RIFT_MIN_PARTY_SIZE = Integer.parseInt(altSettings.getProperty("RiftMinPartySize", "2"));
			RIFT_MAX_JUMPS = Integer.parseInt(altSettings.getProperty("MaxRiftJumps", "4"));
			RIFT_SPAWN_DELAY = Integer.parseInt(altSettings.getProperty("RiftSpawnDelay", "10000"));
			RIFT_AUTO_JUMPS_TIME_MIN = Integer.parseInt(altSettings.getProperty("AutoJumpsDelayMin", "480"));
			RIFT_AUTO_JUMPS_TIME_MAX = Integer.parseInt(altSettings.getProperty("AutoJumpsDelayMax", "600"));
			RIFT_ENTER_COST_RECRUIT = Integer.parseInt(altSettings.getProperty("RecruitCost", "18"));
			RIFT_ENTER_COST_SOLDIER = Integer.parseInt(altSettings.getProperty("SoldierCost", "21"));
			RIFT_ENTER_COST_OFFICER = Integer.parseInt(altSettings.getProperty("OfficerCost", "24"));
			RIFT_ENTER_COST_CAPTAIN = Integer.parseInt(altSettings.getProperty("CaptainCost", "27"));
			RIFT_ENTER_COST_COMMANDER = Integer.parseInt(altSettings.getProperty("CommanderCost", "30"));
			RIFT_ENTER_COST_HERO = Integer.parseInt(altSettings.getProperty("HeroCost", "33"));
			RIFT_BOSS_ROOM_TIME_MUTIPLY = Float.parseFloat(altSettings.getProperty("BossRoomTimeMultiply", "1.5"));
			
			// custom settings
			final Properties customSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(CUSTOM_CONFIG_FILE)))
			{
				customSettings.load(is);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + CUSTOM_CONFIG_FILE + " File.");
			}
			
			BLOCK_UNWANTED_BUFFS = Boolean.valueOf(customSettings.getProperty("BlockUnwantedBuffs", "false"));
			
			CUSTOM_SPAWNLIST_TABLE = Boolean.valueOf(customSettings.getProperty("CustomSpawnlistTable", "false"));
			SAVE_GMSPAWN_ON_CUSTOM = Boolean.valueOf(customSettings.getProperty("SaveGmSpawnOnCustom", "false"));
			CUSTOM_NPC_TABLE = Boolean.valueOf(customSettings.getProperty("CustomNpcTable", "false"));
			CUSTOM_NPC_SKILLS_TABLE = Boolean.valueOf(customSettings.getProperty("CustomNpcSkillsTable", "false"));
			CUSTOM_ITEM_TABLES = Boolean.valueOf(customSettings.getProperty("CustomItemTables", "false"));
			CUSTOM_ARMORSETS_TABLE = Boolean.valueOf(customSettings.getProperty("CustomArmorSetsTable", "false"));
			CUSTOM_TELEPORT_TABLE = Boolean.valueOf(customSettings.getProperty("CustomTeleportTable", "false"));
			CUSTOM_DROPLIST_TABLE = Boolean.valueOf(customSettings.getProperty("CustomDroplistTable", "false"));
			CUSTOM_MERCHANT_TABLES = Boolean.valueOf(customSettings.getProperty("CustomMerchantTables", "false"));
			
			CHAMPION_ENABLE = Boolean.valueOf(customSettings.getProperty("ChampionEnable", "false"));
			CHAMPION_PASSIVE = Boolean.valueOf(customSettings.getProperty("ChampionPassive", "false"));
			CHAMPION_FREQUENCY = Integer.parseInt(customSettings.getProperty("ChampionFrequency", "0"));
			CHAMPION_TITLE = customSettings.getProperty("ChampionTitle", "Champion");
			CHAMP_MIN_LVL = Integer.parseInt(customSettings.getProperty("ChampionMinLevel", "20"));
			CHAMP_MAX_LVL = Integer.parseInt(customSettings.getProperty("ChampionMaxLevel", "70"));
			CHAMPION_HP = Integer.parseInt(customSettings.getProperty("ChampionHp", "8"));
			CHAMPION_HP_REGEN = Float.parseFloat(customSettings.getProperty("ChampionHpRegen", "1."));
			CHAMPION_REWARDS = Integer.parseInt(customSettings.getProperty("ChampionRewards", "8"));
			CHAMPION_ADENAS_REWARDS = Integer.parseInt(customSettings.getProperty("ChampionAdenasRewards", "1"));
			CHAMPION_ATK = Float.parseFloat(customSettings.getProperty("ChampionAtk", "1."));
			CHAMPION_SPD_ATK = Float.parseFloat(customSettings.getProperty("ChampionSpdAtk", "1."));
			CHAMPION_REWARD_LOWER_CHANCE = Integer.parseInt(customSettings.getProperty("ChampionRewardLowerLvlItemChance", "0"));
			CHAMPION_REWARD_HIGHER_CHANCE = Integer.parseInt(customSettings.getProperty("ChampionRewardHigherLvlItemChance", "0"));
			CHAMPION_REWARD_ID = Integer.parseInt(customSettings.getProperty("ChampionRewardItemID", "6393"));
			CHAMPION_REWARD_QTY = Integer.parseInt(customSettings.getProperty("ChampionRewardItemQty", "1"));
			
			ALLOW_AUTO_REWARDER = Boolean.valueOf(customSettings.getProperty("AllowAutoRewarder", "False"));
			AUTO_REWARD_DELAY = Integer.parseInt(customSettings.getProperty("AutoRewardDelay", "1200"));
			AUTO_REWARD_ID = Integer.parseInt(customSettings.getProperty("AutoRewardID", "57"));
			AUTO_REWARD_COUNT = Integer.parseInt(customSettings.getProperty("AutoRewardCount", "1000"));
			
			CUSTOM_STARTING_SPAWN = Boolean.parseBoolean(customSettings.getProperty("CustomStartingSpawn", "False"));
			CUSTOM_SPAWN_X = Integer.parseInt(customSettings.getProperty("CustomSpawnX", ""));
			CUSTOM_SPAWN_Y = Integer.parseInt(customSettings.getProperty("CustomSpawnY", ""));
			CUSTOM_SPAWN_Z = Integer.parseInt(customSettings.getProperty("CustomSpawnZ", ""));
			
			ENABLE_MODIFY_SKILL_DURATION = Boolean.parseBoolean(customSettings.getProperty("EnableModifySkillDuration", "False"));
			
			// Create Map only if enabled
			if (ENABLE_MODIFY_SKILL_DURATION)
			{
				SKILL_DURATION_LIST = new FastMap<>();
				String[] propertySplit;
				propertySplit = customSettings.getProperty("SkillDurationList", "").split(";");
				for (final String skill : propertySplit)
				{
					final String[] skillSplit = skill.split(",");
					if (skillSplit.length != 2)
					{
						System.out.println("[SkillDurationList]: invalid config property -> SkillDurationList \"" + skill + "\"");
					}
					else
					{
						try
						{
							SKILL_DURATION_LIST.put(Integer.valueOf(skillSplit[0]), Integer.valueOf(skillSplit[1]));
						}
						catch (final NumberFormatException nfe)
						{
							if (!skill.isEmpty())
							{
								System.out.println("[SkillDurationList]: invalid config property -> SkillList \"" + skillSplit[0] + "\"" + skillSplit[1]);
							}
						}
					}
				}
			}
			
			Boost_EXP_COMMAND = Boolean.parseBoolean(customSettings.getProperty("SpExpCommand", "False"));
			AUTO_TARGET_NPC = Boolean.parseBoolean(customSettings.getProperty("EnableAutoTargetNPC", "False"));
			CHANGE_SUBCLASS_EVERYWHERE = Boolean.parseBoolean(customSettings.getProperty("ChooseAllSubClassesEveryWhere", "False"));
			AUTO_NOBLE_STATUS = Boolean.parseBoolean(customSettings.getProperty("AutoNoblesseAtLogin", "False"));
			ALLOW_HERO_ENCHANT = Boolean.parseBoolean(customSettings.getProperty("AllowEnchantHeroItems", "False"));
			PREVENT_DUAL_BOXING = Boolean.parseBoolean(customSettings.getProperty("PreventDualBoxing", "False"));
			ENABLE_REAL_TIME = Boolean.parseBoolean(customSettings.getProperty("EnableRealTime", "False"));
			NPC_BUFFER_ENABLED = Boolean.valueOf(customSettings.getProperty("NPCBufferEnabled", "False"));
			AIO_BUFF_DURATION = Integer.parseInt(customSettings.getProperty("AIOBuffDuration", "0"));
			OFFLINE_TRADE_ENABLE = Boolean.valueOf(customSettings.getProperty("OfflineTradeEnable", "false"));
			OFFLINE_CRAFT_ENABLE = Boolean.valueOf(customSettings.getProperty("OfflineCraftEnable", "false"));
			RESTORE_OFFLINERS = Boolean.parseBoolean(customSettings.getProperty("RestoreOffliners", "True"));
			OFFLINE_MAX_DAYS = Integer.parseInt(customSettings.getProperty("OfflineMaxDays", "10"));
			OFFLINE_DISCONNECT_FINISHED = Boolean.parseBoolean(customSettings.getProperty("OfflineDisconnectFinished", "true"));
			OFFLINE_SET_NAME_COLOR = Boolean.valueOf(customSettings.getProperty("OfflineSetNameColor", "false"));
			OFFLINE_NAME_COLOR = Integer.decode("0x" + customSettings.getProperty("OfflineNameColor", "808080"));
			ALLOW_SIEGE_TELEPORT = Boolean.valueOf(customSettings.getProperty("AllowSiegeTeleport", "False"));
			KEEP_SUBCLASS_SKILLS = Boolean.valueOf(customSettings.getProperty("KeepSubClassSkills", "False"));
			ALLOW_CLASS_MASTERS = Boolean.valueOf(customSettings.getProperty("AllowClassMasters", "False"));
			ALLOW_ENTIRE_TREE = Boolean.valueOf(customSettings.getProperty("AllowEntireTree", "False"));
			ALTERNATE_CLASS_MASTER = Boolean.valueOf(customSettings.getProperty("AlternateClassMaster", "False"));
			if (ALLOW_CLASS_MASTERS || ALTERNATE_CLASS_MASTER)
			{
				CLASS_MASTER_SETTINGS = new ClassMasterSettings(String.valueOf(customSettings.getProperty("ConfigClassMaster")));
			}
			
			// Feature settings
			final Properties Feature = new Properties();
			try (InputStream is = new FileInputStream(new File(FEATURE_CONFIG_FILE)))
			{
				Feature.load(is);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + FEATURE_CONFIG_FILE + " File.");
			}
			
			CS_TELE_FEE_RATIO = Long.parseLong(Feature.getProperty("CastleTeleportFunctionFeeRatio", "604800000"));
			CS_TELE1_FEE = Integer.parseInt(Feature.getProperty("CastleTeleportFunctionFeeLvl1", "7000"));
			CS_TELE2_FEE = Integer.parseInt(Feature.getProperty("CastleTeleportFunctionFeeLvl2", "14000"));
			CS_SUPPORT_FEE_RATIO = Long.parseLong(Feature.getProperty("CastleSupportFunctionFeeRatio", "86400000"));
			CS_SUPPORT1_FEE = Integer.parseInt(Feature.getProperty("CastleSupportFeeLvl1", "7000"));
			CS_SUPPORT2_FEE = Integer.parseInt(Feature.getProperty("CastleSupportFeeLvl2", "21000"));
			CS_SUPPORT3_FEE = Integer.parseInt(Feature.getProperty("CastleSupportFeeLvl3", "37000"));
			CS_SUPPORT4_FEE = Integer.parseInt(Feature.getProperty("CastleSupportFeeLvl4", "52000"));
			CS_MPREG_FEE_RATIO = Long.parseLong(Feature.getProperty("CastleMpRegenerationFunctionFeeRatio", "86400000"));
			CS_MPREG1_FEE = Integer.parseInt(Feature.getProperty("CastleMpRegenerationFeeLvl1", "2000"));
			CS_MPREG2_FEE = Integer.parseInt(Feature.getProperty("CastleMpRegenerationFeeLvl2", "6500"));
			CS_MPREG3_FEE = Integer.parseInt(Feature.getProperty("CastleMpRegenerationFeeLvl3", "13750"));
			CS_MPREG4_FEE = Integer.parseInt(Feature.getProperty("CastleMpRegenerationFeeLvl4", "20000"));
			CS_HPREG_FEE_RATIO = Long.parseLong(Feature.getProperty("CastleHpRegenerationFunctionFeeRatio", "86400000"));
			CS_HPREG1_FEE = Integer.parseInt(Feature.getProperty("CastleHpRegenerationFeeLvl1", "1000"));
			CS_HPREG2_FEE = Integer.parseInt(Feature.getProperty("CastleHpRegenerationFeeLvl2", "1500"));
			CS_HPREG3_FEE = Integer.parseInt(Feature.getProperty("CastleHpRegenerationFeeLvl3", "2250"));
			CS_HPREG4_FEE = Integer.parseInt(Feature.getProperty("CastleHpRegenerationFeeLvl4", "3270"));
			CS_HPREG5_FEE = Integer.parseInt(Feature.getProperty("CastleHpRegenerationFeeLvl5", "5166"));
			CS_EXPREG_FEE_RATIO = Long.parseLong(Feature.getProperty("CastleExpRegenerationFunctionFeeRatio", "86400000"));
			CS_EXPREG1_FEE = Integer.parseInt(Feature.getProperty("CastleExpRegenerationFeeLvl1", "9000"));
			CS_EXPREG2_FEE = Integer.parseInt(Feature.getProperty("CastleExpRegenerationFeeLvl2", "15000"));
			CS_EXPREG3_FEE = Integer.parseInt(Feature.getProperty("CastleExpRegenerationFeeLvl3", "21000"));
			CS_EXPREG4_FEE = Integer.parseInt(Feature.getProperty("CastleExpRegenerationFeeLvl4", "30000"));
			
			CH_TELE_FEE_RATIO = Long.valueOf(Feature.getProperty("ClanHallTeleportFunctionFeeRation", "86400000"));
			CH_TELE1_FEE = Integer.valueOf(Feature.getProperty("ClanHallTeleportFunctionFeeLvl1", "86400000"));
			CH_TELE2_FEE = Integer.valueOf(Feature.getProperty("ClanHallTeleportFunctionFeeLvl2", "86400000"));
			CH_SUPPORT_FEE_RATIO = Long.valueOf(Feature.getProperty("ClanHallSupportFunctionFeeRation", "86400000"));
			CH_SUPPORT1_FEE = Integer.valueOf(Feature.getProperty("ClanHallSupportFeeLvl1", "86400000"));
			CH_SUPPORT2_FEE = Integer.valueOf(Feature.getProperty("ClanHallSupportFeeLvl2", "86400000"));
			CH_SUPPORT3_FEE = Integer.valueOf(Feature.getProperty("ClanHallSupportFeeLvl3", "86400000"));
			CH_SUPPORT4_FEE = Integer.valueOf(Feature.getProperty("ClanHallSupportFeeLvl4", "86400000"));
			CH_SUPPORT5_FEE = Integer.valueOf(Feature.getProperty("ClanHallSupportFeeLvl5", "86400000"));
			CH_SUPPORT6_FEE = Integer.valueOf(Feature.getProperty("ClanHallSupportFeeLvl6", "86400000"));
			CH_SUPPORT7_FEE = Integer.valueOf(Feature.getProperty("ClanHallSupportFeeLvl7", "86400000"));
			CH_SUPPORT8_FEE = Integer.valueOf(Feature.getProperty("ClanHallSupportFeeLvl8", "86400000"));
			CH_MPREG_FEE_RATIO = Long.valueOf(Feature.getProperty("ClanHallMpRegenerationFunctionFeeRation", "86400000"));
			CH_MPREG1_FEE = Integer.valueOf(Feature.getProperty("ClanHallMpRegenerationFeeLvl1", "86400000"));
			CH_MPREG2_FEE = Integer.valueOf(Feature.getProperty("ClanHallMpRegenerationFeeLvl2", "86400000"));
			CH_MPREG3_FEE = Integer.valueOf(Feature.getProperty("ClanHallMpRegenerationFeeLvl3", "86400000"));
			CH_MPREG4_FEE = Integer.valueOf(Feature.getProperty("ClanHallMpRegenerationFeeLvl4", "86400000"));
			CH_MPREG5_FEE = Integer.valueOf(Feature.getProperty("ClanHallMpRegenerationFeeLvl5", "86400000"));
			CH_HPREG_FEE_RATIO = Long.valueOf(Feature.getProperty("ClanHallHpRegenerationFunctionFeeRation", "86400000"));
			CH_HPREG1_FEE = Integer.valueOf(Feature.getProperty("ClanHallHpRegenerationFeeLvl1", "86400000"));
			CH_HPREG2_FEE = Integer.valueOf(Feature.getProperty("ClanHallHpRegenerationFeeLvl2", "86400000"));
			CH_HPREG3_FEE = Integer.valueOf(Feature.getProperty("ClanHallHpRegenerationFeeLvl3", "86400000"));
			CH_HPREG4_FEE = Integer.valueOf(Feature.getProperty("ClanHallHpRegenerationFeeLvl4", "86400000"));
			CH_HPREG5_FEE = Integer.valueOf(Feature.getProperty("ClanHallHpRegenerationFeeLvl5", "86400000"));
			CH_HPREG6_FEE = Integer.valueOf(Feature.getProperty("ClanHallHpRegenerationFeeLvl6", "86400000"));
			CH_HPREG7_FEE = Integer.valueOf(Feature.getProperty("ClanHallHpRegenerationFeeLvl7", "86400000"));
			CH_HPREG8_FEE = Integer.valueOf(Feature.getProperty("ClanHallHpRegenerationFeeLvl8", "86400000"));
			CH_HPREG9_FEE = Integer.valueOf(Feature.getProperty("ClanHallHpRegenerationFeeLvl9", "86400000"));
			CH_HPREG10_FEE = Integer.valueOf(Feature.getProperty("ClanHallHpRegenerationFeeLvl10", "86400000"));
			CH_HPREG11_FEE = Integer.valueOf(Feature.getProperty("ClanHallHpRegenerationFeeLvl11", "86400000"));
			CH_HPREG12_FEE = Integer.valueOf(Feature.getProperty("ClanHallHpRegenerationFeeLvl12", "86400000"));
			CH_HPREG13_FEE = Integer.valueOf(Feature.getProperty("ClanHallHpRegenerationFeeLvl13", "86400000"));
			CH_EXPREG_FEE_RATIO = Long.valueOf(Feature.getProperty("ClanHallExpRegenerationFunctionFeeRation", "86400000"));
			CH_EXPREG1_FEE = Integer.valueOf(Feature.getProperty("ClanHallExpRegenerationFeeLvl1", "86400000"));
			CH_EXPREG2_FEE = Integer.valueOf(Feature.getProperty("ClanHallExpRegenerationFeeLvl2", "86400000"));
			CH_EXPREG3_FEE = Integer.valueOf(Feature.getProperty("ClanHallExpRegenerationFeeLvl3", "86400000"));
			CH_EXPREG4_FEE = Integer.valueOf(Feature.getProperty("ClanHallExpRegenerationFeeLvl4", "86400000"));
			CH_EXPREG5_FEE = Integer.valueOf(Feature.getProperty("ClanHallExpRegenerationFeeLvl5", "86400000"));
			CH_EXPREG6_FEE = Integer.valueOf(Feature.getProperty("ClanHallExpRegenerationFeeLvl6", "86400000"));
			CH_EXPREG7_FEE = Integer.valueOf(Feature.getProperty("ClanHallExpRegenerationFeeLvl7", "86400000"));
			CH_ITEM_FEE_RATIO = Long.valueOf(Feature.getProperty("ClanHallItemCreationFunctionFeeRation", "86400000"));
			CH_ITEM1_FEE = Integer.valueOf(Feature.getProperty("ClanHallItemCreationFunctionFeeLvl1", "86400000"));
			CH_ITEM2_FEE = Integer.valueOf(Feature.getProperty("ClanHallItemCreationFunctionFeeLvl2", "86400000"));
			CH_ITEM3_FEE = Integer.valueOf(Feature.getProperty("ClanHallItemCreationFunctionFeeLvl3", "86400000"));
			CH_CURTAIN_FEE_RATIO = Long.valueOf(Feature.getProperty("ClanHallCurtainFunctionFeeRation", "86400000"));
			CH_CURTAIN1_FEE = Integer.valueOf(Feature.getProperty("ClanHallCurtainFunctionFeeLvl1", "86400000"));
			CH_CURTAIN2_FEE = Integer.valueOf(Feature.getProperty("ClanHallCurtainFunctionFeeLvl2", "86400000"));
			CH_FRONT_FEE_RATIO = Long.valueOf(Feature.getProperty("ClanHallFrontPlatformFunctionFeeRation", "86400000"));
			CH_FRONT1_FEE = Integer.valueOf(Feature.getProperty("ClanHallFrontPlatformFunctionFeeLvl1", "86400000"));
			CH_FRONT2_FEE = Integer.valueOf(Feature.getProperty("ClanHallFrontPlatformFunctionFeeLvl2", "86400000"));
			
			ALT_GAME_REQUIRE_CASTLE_DAWN = Boolean.parseBoolean(Feature.getProperty("AltRequireCastleForDawn", "False"));
			ALT_GAME_REQUIRE_CLAN_CASTLE = Boolean.parseBoolean(Feature.getProperty("AltRequireClanCastle", "False"));
			ALT_FESTIVAL_MIN_PLAYER = Integer.parseInt(Feature.getProperty("AltFestivalMinPlayer", "5"));
			ALT_MAXIMUM_PLAYER_CONTRIB = Integer.parseInt(Feature.getProperty("AltMaxPlayerContrib", "1000000"));
			ALT_FESTIVAL_MANAGER_START = Long.parseLong(Feature.getProperty("AltFestivalManagerStart", "120000"));
			ALT_FESTIVAL_LENGTH = Long.parseLong(Feature.getProperty("AltFestivalLength", "1080000"));
			ALT_FESTIVAL_CYCLE_LENGTH = Long.parseLong(Feature.getProperty("AltFestivalCycleLength", "2280000"));
			ALT_FESTIVAL_FIRST_SPAWN = Long.parseLong(Feature.getProperty("AltFestivalFirstSpawn", "120000"));
			ALT_FESTIVAL_FIRST_SWARM = Long.parseLong(Feature.getProperty("AltFestivalFirstSwarm", "300000"));
			ALT_FESTIVAL_SECOND_SPAWN = Long.parseLong(Feature.getProperty("AltFestivalSecondSpawn", "540000"));
			ALT_FESTIVAL_SECOND_SWARM = Long.parseLong(Feature.getProperty("AltFestivalSecondSwarm", "720000"));
			ALT_FESTIVAL_CHEST_SPAWN = Long.parseLong(Feature.getProperty("AltFestivalChestSpawn", "900000"));
			
			// pvp config
			final Properties pvpSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(PVP_CONFIG_FILE)))
			{
				pvpSettings.load(is);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + PVP_CONFIG_FILE + " File.");
			}
			
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
			
			KARMA_LIST_NONDROPPABLE_PET_ITEMS = new FastList<>();
			for (final String id : KARMA_NONDROPPABLE_PET_ITEMS.split(","))
			{
				KARMA_LIST_NONDROPPABLE_PET_ITEMS.add(Integer.parseInt(id));
			}
			
			KARMA_LIST_NONDROPPABLE_ITEMS = new FastList<>();
			for (final String id : KARMA_NONDROPPABLE_ITEMS.split(","))
			{
				KARMA_LIST_NONDROPPABLE_ITEMS.add(Integer.parseInt(id));
			}
			
			PVP_NORMAL_TIME = Integer.parseInt(pvpSettings.getProperty("PvPVsNormalTime", "15000"));
			PVP_PVP_TIME = Integer.parseInt(pvpSettings.getProperty("PvPVsPvPTime", "30000"));
			
			// access levels
			final Properties gmSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(GM_ACCESS_FILE)))
			{
				gmSettings.load(is);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + GM_ACCESS_FILE + " File.");
			}
			
			GM_ACCESSLEVEL = Integer.parseInt(gmSettings.getProperty("GMAccessLevel", "100"));
			GM_MIN = Integer.parseInt(gmSettings.getProperty("GMMinLevel", "100"));
			GM_ANNOUNCE = Integer.parseInt(gmSettings.getProperty("GMCanAnnounce", "100"));
			GM_BAN = Integer.parseInt(gmSettings.getProperty("GMCanBan", "100"));
			GM_BAN_CHAT = Integer.parseInt(gmSettings.getProperty("GMCanBanChat", "100"));
			GM_CREATE_ITEM = Integer.parseInt(gmSettings.getProperty("GMCanShop", "100"));
			GM_DELETE = Integer.parseInt(gmSettings.getProperty("GMCanDelete", "100"));
			GM_KICK = Integer.parseInt(gmSettings.getProperty("GMCanKick", "100"));
			GM_MENU = Integer.parseInt(gmSettings.getProperty("GMMenu", "100"));
			GM_GODMODE = Integer.parseInt(gmSettings.getProperty("GMGodMode", "100"));
			GM_CHAR_EDIT = Integer.parseInt(gmSettings.getProperty("GMCanEditChar", "100"));
			GM_CHAR_EDIT_OTHER = Integer.parseInt(gmSettings.getProperty("GMCanEditCharOther", "100"));
			GM_CHAR_VIEW = Integer.parseInt(gmSettings.getProperty("GMCanViewChar", "100"));
			GM_NPC_EDIT = Integer.parseInt(gmSettings.getProperty("GMCanEditNPC", "100"));
			GM_NPC_VIEW = Integer.parseInt(gmSettings.getProperty("GMCanViewNPC", "100"));
			GM_TELEPORT = Integer.parseInt(gmSettings.getProperty("GMCanTeleport", "100"));
			GM_TELEPORT_OTHER = Integer.parseInt(gmSettings.getProperty("GMCanTeleportOther", "100"));
			GM_RESTART = Integer.parseInt(gmSettings.getProperty("GMCanRestart", "100"));
			GM_MONSTERRACE = Integer.parseInt(gmSettings.getProperty("GMMonsterRace", "100"));
			GM_RIDER = Integer.parseInt(gmSettings.getProperty("GMRider", "100"));
			GM_ESCAPE = Integer.parseInt(gmSettings.getProperty("GMFastUnstuck", "100"));
			GM_FIXED = Integer.parseInt(gmSettings.getProperty("GMResurectFixed", "100"));
			GM_CREATE_NODES = Integer.parseInt(gmSettings.getProperty("GMCreateNodes", "100"));
			GM_ENCHANT = Integer.parseInt(gmSettings.getProperty("GMEnchant", "100"));
			GM_DOOR = Integer.parseInt(gmSettings.getProperty("GMDoor", "100"));
			GM_RES = Integer.parseInt(gmSettings.getProperty("GMRes", "100"));
			GM_PEACEATTACK = Integer.parseInt(gmSettings.getProperty("GMPeaceAttack", "100"));
			GM_HEAL = Integer.parseInt(gmSettings.getProperty("GMHeal", "100"));
			GM_UNBLOCK = Integer.parseInt(gmSettings.getProperty("GMUnblock", "100"));
			GM_CACHE = Integer.parseInt(gmSettings.getProperty("GMCache", "100"));
			GM_TALK_BLOCK = Integer.parseInt(gmSettings.getProperty("GMTalkBlock", "100"));
			GM_TEST = Integer.parseInt(gmSettings.getProperty("GMTest", "100"));
			
			final String gmTrans = gmSettings.getProperty("GMDisableTransaction", "False");
			if (!gmTrans.equalsIgnoreCase("false"))
			{
				final String[] params = gmTrans.split(",");
				GM_DISABLE_TRANSACTION = true;
				GM_TRANSACTION_MIN = Integer.parseInt(params[0]);
				GM_TRANSACTION_MAX = Integer.parseInt(params[1]);
			}
			else
			{
				GM_DISABLE_TRANSACTION = false;
			}
			
			final Properties Settings = new Properties();
			try (InputStream is = new FileInputStream(HEXID_FILE))
			{
				Settings.load(is);
			}
			catch (final Exception e)
			{
				_log.warning("Could not load HexID file (" + HEXID_FILE + "). Hopefully login will give us one.");
			}
			HEX_ID = new BigInteger(Settings.getProperty("HexID"), 16).toByteArray();
		}
		else if (Server.SERVER_MODE == Server.MODE_LOGINSERVER)
		{
			_log.info("Loading LoginServer Configuration Files.");
			
			final Properties serverSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(LOGIN_CONFIGURATION_FILE)))
			{
				serverSettings.load(is);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + CONFIGURATION_FILE + " File.");
			}
			
			GAME_SERVER_LOGIN_HOST = serverSettings.getProperty("LoginHostname", "*");
			GAME_SERVER_LOGIN_PORT = Integer.parseInt(serverSettings.getProperty("LoginPort", "9014"));
			
			LOGIN_BIND_ADDRESS = serverSettings.getProperty("LoginserverHostname", "*");
			PORT_LOGIN = Integer.parseInt(serverSettings.getProperty("LoginserverPort", "2106"));
			
			DEBUG = Boolean.parseBoolean(serverSettings.getProperty("Debug", "false"));
			DEVELOPER = Boolean.parseBoolean(serverSettings.getProperty("Developer", "false"));
			ASSERT = Boolean.parseBoolean(serverSettings.getProperty("Assert", "false"));
			
			ACCEPT_NEW_GAMESERVER = Boolean.parseBoolean(serverSettings.getProperty("AcceptNewGameServer", "True"));
			REQUEST_ID = Integer.parseInt(serverSettings.getProperty("RequestServerID", "0"));
			ACCEPT_ALTERNATE_ID = Boolean.parseBoolean(serverSettings.getProperty("AcceptAlternateID", "True"));
			
			LOGIN_TRY_BEFORE_BAN = Integer.parseInt(serverSettings.getProperty("LoginTryBeforeBan", "10"));
			
			LOGIN_BLOCK_AFTER_BAN = Integer.parseInt(serverSettings.getProperty("LoginBlockAfterBan", "600"));
			GM_MIN = Integer.parseInt(serverSettings.getProperty("GMMinLevel", "100"));
			
			try
			{
				DATAPACK_ROOT = new File(serverSettings.getProperty("DatapackRoot", ".").replaceAll("\\\\", "/")).getCanonicalFile();
			}
			catch (final Exception e)
			{
				_log.warning("Error setting datapack root!");
				DATAPACK_ROOT = new File(".");
			}
			
			INTERNAL_HOSTNAME = serverSettings.getProperty("InternalHostname", "localhost");
			EXTERNAL_HOSTNAME = serverSettings.getProperty("ExternalHostname", "localhost");
			
			DATABASE_DRIVER = serverSettings.getProperty("Driver", "com.mysql.jdbc.Driver");
			DATABASE_URL = serverSettings.getProperty("URL", "jdbc:mysql://localhost/l2jdb");
			DATABASE_LOGIN = serverSettings.getProperty("Login", "root");
			DATABASE_PASSWORD = serverSettings.getProperty("Password", "");
			DATABASE_MAX_CONNECTIONS = Integer.parseInt(serverSettings.getProperty("MaximumDbConnections", "10"));
			CONNECTION_CLOSE_TIME = Long.parseLong(serverSettings.getProperty("ConnectionCloseTime", "60000"));
			
			SHOW_LICENCE = Boolean.parseBoolean(serverSettings.getProperty("ShowLicence", "true"));
			AUTO_CREATE_ACCOUNTS = Boolean.parseBoolean(serverSettings.getProperty("AutoCreateAccounts", "True"));
			
			FLOOD_PROTECTION = Boolean.parseBoolean(serverSettings.getProperty("EnableFloodProtection", "True"));
			FAST_CONNECTION_LIMIT = Integer.parseInt(serverSettings.getProperty("FastConnectionLimit", "15"));
			NORMAL_CONNECTION_TIME = Integer.parseInt(serverSettings.getProperty("NormalConnectionTime", "700"));
			FAST_CONNECTION_TIME = Integer.parseInt(serverSettings.getProperty("FastConnectionTime", "350"));
			MAX_CONNECTION_PER_IP = Integer.parseInt(serverSettings.getProperty("MaxConnectionPerIP", "50"));
			
			// telnet
			final Properties telnetSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(TELNET_FILE)))
			{
				telnetSettings.load(is);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
				throw new Error("Failed to Load " + TELNET_FILE + " File.");
			}
			
			IS_TELNET_ENABLED = Boolean.valueOf(telnetSettings.getProperty("EnableTelnet", "False"));
			TELNET_PORT = Integer.parseInt(telnetSettings.getProperty("LoginStatusPort", "12345"));
		}
		else
		{
			_log.severe("Could not Load Config: server mode was not set");
		}
	}
	
	/**
	 * Set a new value to a game parameter from the admin console.
	 * @param pName (String) : name of the parameter to change
	 * @param pValue (String) : new value of the parameter
	 * @return boolean : true if modification has been made
	 */
	public static boolean setParameterValue(String pName, String pValue)
	{
		// Server settings
		if (pName.equalsIgnoreCase("RateXp"))
		{
			RATE_XP = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateSp"))
		{
			RATE_SP = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RatePartyXp"))
		{
			RATE_PARTY_XP = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RatePartySp"))
		{
			RATE_PARTY_SP = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateQuestsReward"))
		{
			RATE_QUESTS_REWARD = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateDropAdena"))
		{
			RATE_DROP_ADENA = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateConsumableCost"))
		{
			RATE_CONSUMABLE_COST = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateDropItems"))
		{
			RATE_DROP_ITEMS = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateBossDropItems"))
		{
			RATE_BOSS_DROP_ITEMS = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateDropSpoil"))
		{
			RATE_DROP_SPOIL = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateDropManor"))
		{
			RATE_DROP_MANOR = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("RateDropQuest"))
		{
			RATE_DROP_QUEST = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateKarmaExpLost"))
		{
			RATE_KARMA_EXP_LOST = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("RateSiegeGuardsPrice"))
		{
			RATE_SIEGE_GUARDS_PRICE = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("PlayerDropLimit"))
		{
			PLAYER_DROP_LIMIT = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PlayerRateDrop"))
		{
			PLAYER_RATE_DROP = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PlayerRateDropItem"))
		{
			PLAYER_RATE_DROP_ITEM = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PlayerRateDropEquip"))
		{
			PLAYER_RATE_DROP_EQUIP = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PlayerRateDropEquipWeapon"))
		{
			PLAYER_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("KarmaDropLimit"))
		{
			KARMA_DROP_LIMIT = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("KarmaRateDrop"))
		{
			KARMA_RATE_DROP = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("KarmaRateDropItem"))
		{
			KARMA_RATE_DROP_ITEM = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("KarmaRateDropEquip"))
		{
			KARMA_RATE_DROP_EQUIP = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("KarmaRateDropEquipWeapon"))
		{
			KARMA_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AutoDestroyDroppedItemAfter"))
		{
			AUTODESTROY_ITEM_AFTER = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("DestroyPlayerDroppedItem"))
		{
			DESTROY_DROPPED_PLAYER_ITEM = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("DestroyEquipableItem"))
		{
			DESTROY_EQUIPABLE_PLAYER_ITEM = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("SaveDroppedItem"))
		{
			SAVE_DROPPED_ITEM = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("EmptyDroppedItemTableAfterLoad"))
		{
			EMPTY_DROPPED_ITEM_TABLE_AFTER_LOAD = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("SaveDroppedItemInterval"))
		{
			SAVE_DROPPED_ITEM_INTERVAL = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ClearDroppedItemTable"))
		{
			CLEAR_DROPPED_ITEM_TABLE = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("PreciseDropCalculation"))
		{
			PRECISE_DROP_CALCULATION = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("MultipleItemDrop"))
		{
			MULTIPLE_ITEM_DROP = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("LowWeight"))
		{
			LOW_WEIGHT = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("MediumWeight"))
		{
			MEDIUM_WEIGHT = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("HighWeight"))
		{
			HIGH_WEIGHT = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("AdvancedDiagonalStrategy"))
		{
			ADVANCED_DIAGONAL_STRATEGY = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("DiagonalWeight"))
		{
			DIAGONAL_WEIGHT = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("MaxPostfilterPasses"))
		{
			MAX_POSTFILTER_PASSES = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("CoordSynchronize"))
		{
			COORD_SYNCHRONIZE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("EnableFallingDamage"))
		{
			ENABLE_FALLING_DAMAGE = Boolean.parseBoolean(pValue);
		}
		else if (pName.equalsIgnoreCase("DeleteCharAfterDays"))
		{
			DELETE_DAYS = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowDiscardItem"))
		{
			ALLOW_DISCARDITEM = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowFreight"))
		{
			ALLOW_FREIGHT = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowWarehouse"))
		{
			ALLOW_WAREHOUSE = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowWear"))
		{
			ALLOW_WEAR = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("WearDelay"))
		{
			WEAR_DELAY = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("WearPrice"))
		{
			WEAR_PRICE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowWater"))
		{
			ALLOW_WATER = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowRentPet"))
		{
			ALLOW_RENTPET = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowBoat"))
		{
			ALLOW_BOAT = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowL2Walker"))
		{
			ALLOW_L2WALKER = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowManor"))
		{
			ALLOW_MANOR = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowNpcWalkers"))
		{
			ALLOW_NPC_WALKERS = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowPetWalkers"))
		{
			ALLOW_PET_WALKERS = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("BypassValidation"))
		{
			BYPASS_VALIDATION = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("CommunityType"))
		{
			COMMUNITY_TYPE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("BBSShowPlayerList"))
		{
			BBS_SHOW_PLAYERLIST = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("BBSDefault"))
		{
			BBS_DEFAULT = pValue;
		}
		else if (pName.equalsIgnoreCase("ShowLevelOnCommunityBoard"))
		{
			SHOW_LEVEL_COMMUNITYBOARD = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("ShowStatusOnCommunityBoard"))
		{
			SHOW_STATUS_COMMUNITYBOARD = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("NamePageSizeOnCommunityBoard"))
		{
			NAME_PAGE_SIZE_COMMUNITYBOARD = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("NamePerRowOnCommunityBoard"))
		{
			NAME_PER_ROW_COMMUNITYBOARD = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ShowNpcLevel"))
		{
			SHOW_NPC_LVL = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("ForceInventoryUpdate"))
		{
			FORCE_INVENTORY_UPDATE = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AutoDeleteInvalidQuestData"))
		{
			AUTODELETE_INVALID_QUEST_DATA = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumOnlineUsers"))
		{
			MAXIMUM_ONLINE_USERS = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ZoneTown"))
		{
			ZONE_TOWN = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumUpdateDistance"))
		{
			MINIMUM_UPDATE_DISTANCE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MinimumUpdateTime"))
		{
			MINIMUN_UPDATE_TIME = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("CheckKnownList"))
		{
			CHECK_KNOWN = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("KnownListForgetDelay"))
		{
			KNOWNLIST_FORGET_DELAY = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("UseDeepBlueDropRules"))
		{
			DEEPBLUE_DROP_RULES = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("GuardAttackAggroMob"))
		{
			GUARD_ATTACK_AGGRO_MOB = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("CancelLesserEffect"))
		{
			EFFECT_CANCELING = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumSlotsForNoDwarf"))
		{
			INVENTORY_MAXIMUM_NO_DWARF = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumSlotsForDwarf"))
		{
			INVENTORY_MAXIMUM_DWARF = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumSlotsForGMPlayer"))
		{
			INVENTORY_MAXIMUM_GM = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumWarehouseSlotsForNoDwarf"))
		{
			WAREHOUSE_SLOTS_NO_DWARF = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumWarehouseSlotsForDwarf"))
		{
			WAREHOUSE_SLOTS_DWARF = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumWarehouseSlotsForClan"))
		{
			WAREHOUSE_SLOTS_CLAN = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumFreightSlots"))
		{
			FREIGHT_SLOTS = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("EnchantChanceWeapon"))
		{
			ENCHANT_CHANCE_WEAPON = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("EnchantChanceArmor"))
		{
			ENCHANT_CHANCE_ARMOR = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("EnchantChanceJewelry"))
		{
			ENCHANT_CHANCE_JEWELRY = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("BlessedEnchantChanceWeapon"))
		{
			BLESSED_ENCHANT_CHANCE_WEAPON = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("BlessedEnchantChanceArmor"))
		{
			BLESSED_ENCHANT_CHANCE_ARMOR = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("BlessedEnchantChanceJewelry"))
		{
			BLESSED_ENCHANT_CHANCE_JEWELRY = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("EnchantMaxWeapon"))
		{
			ENCHANT_MAX_WEAPON = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("EnchantMaxArmor"))
		{
			ENCHANT_MAX_ARMOR = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("EnchantMaxJewelry"))
		{
			ENCHANT_MAX_JEWELRY = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("EnchantSafeMax"))
		{
			ENCHANT_SAFE_MAX = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("EnchantSafeMaxFull"))
		{
			ENCHANT_SAFE_MAX_FULL = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("HpRegenMultiplier"))
		{
			HP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("MpRegenMultiplier"))
		{
			MP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("CpRegenMultiplier"))
		{
			CP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("RaidHpRegenMultiplier"))
		{
			RAID_HP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("RaidMpRegenMultiplier"))
		{
			RAID_MP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("RaidPDefenceMultiplier"))
		{
			RAID_PDEFENCE_MULTIPLIER = Double.parseDouble(pValue) / 100;
		}
		else if (pName.equalsIgnoreCase("RaidMDefenceMultiplier"))
		{
			RAID_MDEFENCE_MULTIPLIER = Double.parseDouble(pValue) / 100;
		}
		else if (pName.equalsIgnoreCase("RaidMinionRespawnTime"))
		{
			RAID_MINION_RESPAWN_TIMER = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("StartingAdena"))
		{
			STARTING_ADENA = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("StartingLevel"))
		{
			STARTING_LEVEL = Byte.parseByte(pValue);
		}
		else if (pName.equalsIgnoreCase("StartingSubclassLevel"))
		{
			STARTING_SUB_LEVEL = Byte.parseByte(pValue);
		}
		else if (pName.equalsIgnoreCase("UnstuckInterval"))
		{
			UNSTUCK_INTERVAL = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PlayerSpawnProtection"))
		{
			PLAYER_SPAWN_PROTECTION = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PlayerFakeDeathUpProtection"))
		{
			PLAYER_FAKEDEATH_UP_PROTECTION = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PartyXpCutoffMethod"))
		{
			PARTY_XP_CUTOFF_METHOD = pValue;
		}
		else if (pName.equalsIgnoreCase("PartyXpCutoffPercent"))
		{
			PARTY_XP_CUTOFF_PERCENT = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("PartyXpCutoffLevel"))
		{
			PARTY_XP_CUTOFF_LEVEL = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("RespawnRestoreHP"))
		{
			RESPAWN_RESTORE_HP = Double.parseDouble(pValue) / 100;
		}
		else if (pName.equalsIgnoreCase("MaxPvtStoreSlotsDwarf"))
		{
			MAX_PVTSTORE_SLOTS_DWARF = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaxPvtStoreSlotsOther"))
		{
			MAX_PVTSTORE_SLOTS_OTHER = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("StoreSkillCooltime"))
		{
			STORE_SKILL_COOLTIME = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AnnounceMammonSpawn"))
		{
			ANNOUNCE_MAMMON_SPAWN = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("CanSpoilLowerLevelMobs"))
		{
			CAN_SPOIL_LOWER_LEVEL_MOBS = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("CanDelevelToSpoil"))
		{
			CAN_DELEVEL_AND_SPOIL_MOBS = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("MaximumPlayerAndMobLevelDifference"))
		{
			MAXIMUM_PLAYER_AND_MOB_LEVEL_DIFFERENCE = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("BasePercentChanceOfSpoilSuccess"))
		{
			BASE_SPOIL_RATE = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("MinimumPercentChanceOfSpoilSuccess"))
		{
			MINIMUM_SPOIL_RATE = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("SpoilLevelDifferenceLimit"))
		{
			SPOIL_LEVEL_DIFFERENCE_LIMIT = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("SpoilLevelMultiplier"))
		{
			SPOIL_LEVEL_DIFFERENCE_MULTIPLIER = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("LastLevelSpoilIsLearned"))
		{
			LAST_LEVEL_SPOIL_IS_LEARNED = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameTiredness"))
		{
			ALT_GAME_TIREDNESS = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameCreation"))
		{
			ALT_GAME_CREATION = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameCreationSpeed"))
		{
			ALT_GAME_CREATION_SPEED = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameCreationXpRate"))
		{
			ALT_GAME_CREATION_XP_RATE = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameCreationSpRate"))
		{
			ALT_GAME_CREATION_SP_RATE = Double.parseDouble(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameSkillLearn"))
		{
			ALT_GAME_SKILL_LEARN = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AutoLearnSkills"))
		{
			AUTO_LEARN_SKILLS = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AutoLearn3rdClassSkills"))
		{
			AUTO_LEARN_3RD_SKILLS = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameCancelByHit"))
		{
			ALT_GAME_CANCEL_BOW = pValue.equalsIgnoreCase("bow") || pValue.equalsIgnoreCase("all");
			ALT_GAME_CANCEL_CAST = pValue.equalsIgnoreCase("cast") || pValue.equalsIgnoreCase("all");
		}
		
		else if (pName.equalsIgnoreCase("AltPerfectShieldBlockRate"))
		{
			ALT_PERFECT_SHLD_BLOCK = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("Delevel"))
		{
			ALT_GAME_DELEVEL = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("MagicFailures"))
		{
			ALT_GAME_MAGICFAILURES = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltMobAggroInPeaceZone"))
		{
			ALT_MOB_AGGRO_IN_PEACEZONE = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameExponentXp"))
		{
			ALT_GAME_EXPONENT_XP = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameExponentSp"))
		{
			ALT_GAME_EXPONENT_SP = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowSiegeTeleport"))
		{
			ALLOW_SIEGE_TELEPORT = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowClassMasters"))
		{
			ALLOW_CLASS_MASTERS = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AllowEntireTree"))
		{
			ALLOW_ENTIRE_TREE = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AlternateClassMaster"))
		{
			ALTERNATE_CLASS_MASTER = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameFreights"))
		{
			ALT_GAME_FREIGHTS = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltGameFreightPrice"))
		{
			ALT_GAME_FREIGHT_PRICE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AltPartyRange"))
		{
			ALT_PARTY_RANGE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AltPartyRange2"))
		{
			ALT_PARTY_RANGE2 = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("CraftingEnabled"))
		{
			IS_CRAFTING_ENABLED = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("EnchantSkillSpBookNeeded"))
		{
			ES_SP_BOOK_NEEDED = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AutoLoot"))
		{
			AUTO_LOOT = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AutoLootRaids"))
		{
			AUTO_LOOT_RAIDS = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanBeKilledInPeaceZone"))
		{
			ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanShop"))
		{
			ALT_GAME_KARMA_PLAYER_CAN_SHOP = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanUseGK"))
		{
			ALT_GAME_KARMA_PLAYER_CAN_USE_GK = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanTeleport"))
		{
			ALT_GAME_KARMA_PLAYER_CAN_TELEPORT = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanTrade"))
		{
			ALT_GAME_KARMA_PLAYER_CAN_TRADE = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltKarmaPlayerCanUseWareHouse"))
		{
			ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltRequireCastleForDawn"))
		{
			ALT_GAME_REQUIRE_CASTLE_DAWN = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltRequireClanCastle"))
		{
			ALT_GAME_REQUIRE_CLAN_CASTLE = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltFreeTeleporting"))
		{
			ALT_GAME_FREE_TELEPORT = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltMaxSubClasses"))
		{
			ALT_MAX_SUBCLASS = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("AltSubClassWithoutQuests"))
		{
			ALT_GAME_SUBCLASS_WITHOUT_QUESTS = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltEnableTutorial"))
		{
			ALT_ENABLE_TUTORIAL = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AltNewCharAlwaysIsNewbie"))
		{
			ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("DwarfRecipeLimit"))
		{
			DWARF_RECIPE_LIMIT = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("CommonRecipeLimit"))
		{
			COMMON_RECIPE_LIMIT = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionEnable"))
		{
			CHAMPION_ENABLE = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionPassive"))
		{
			CHAMPION_PASSIVE = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionFrequency"))
		{
			CHAMPION_FREQUENCY = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionTitle"))
		{
			CHAMPION_TITLE = pValue;
		}
		else if (pName.equalsIgnoreCase("ChampionMinLevel"))
		{
			CHAMP_MIN_LVL = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionMaxLevel"))
		{
			CHAMP_MAX_LVL = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionHp"))
		{
			CHAMPION_HP = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionHpRegen"))
		{
			CHAMPION_HP_REGEN = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionRewards"))
		{
			CHAMPION_REWARDS = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionAdenasRewards"))
		{
			CHAMPION_ADENAS_REWARDS = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionAtk"))
		{
			CHAMPION_ATK = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionSpdAtk"))
		{
			CHAMPION_SPD_ATK = Float.parseFloat(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionRewardLowerLvlItemChance"))
		{
			CHAMPION_REWARD_LOWER_CHANCE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionRewardHigherLvlItemChance"))
		{
			CHAMPION_REWARD_HIGHER_CHANCE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionRewardItemID"))
		{
			CHAMPION_REWARD_ID = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("ChampionRewardItemQty"))
		{
			CHAMPION_REWARD_QTY = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MinKarma"))
		{
			KARMA_MIN_KARMA = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("MaxKarma"))
		{
			KARMA_MAX_KARMA = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("XPDivider"))
		{
			KARMA_XP_DIVIDER = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("BaseKarmaLost"))
		{
			KARMA_LOST_BASE = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("CanGMDropEquipment"))
		{
			KARMA_DROP_GM = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("AwardPKKillPVPPoint"))
		{
			KARMA_AWARD_PK_KILL = Boolean.valueOf(pValue);
		}
		else if (pName.equalsIgnoreCase("MinimumPKRequiredToDrop"))
		{
			KARMA_PK_LIMIT = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PvPVsNormalTime"))
		{
			PVP_NORMAL_TIME = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("PvPVsPvPTime"))
		{
			PVP_PVP_TIME = Integer.parseInt(pValue);
		}
		else if (pName.equalsIgnoreCase("GlobalChat"))
		{
			DEFAULT_GLOBAL_CHAT = pValue;
		}
		else if (pName.equalsIgnoreCase("TradeChat"))
		{
			DEFAULT_TRADE_CHAT = pValue;
		}
		else
		{
			return false;
		}
		return true;
	}
	
	/**
	 * Save hexadecimal ID of the server in the properties file.
	 * @param string (String) : hexadecimal ID of the server to store
	 */
	public static void saveHexid(String string)
	{
		saveHexid(string, HEXID_FILE);
	}
	
	/**
	 * Save hexadecimal ID of the server in the properties file.
	 * @param string (String) : hexadecimal ID of the server to store
	 * @param fileName (String) : name of the properties file
	 */
	public static void saveHexid(String string, String fileName)
	{
		try
		{
			final Properties hexSetting = new Properties();
			final File file = new File(fileName);
			// Create a new empty file only if it doesn't exist
			file.createNewFile();
			
			try (OutputStream out = new FileOutputStream(file))
			{
				hexSetting.setProperty("HexID", string);
				hexSetting.store(out, "the hexID to auth into login");
			}
		}
		catch (final Exception e)
		{
			_log.warning("Failed to save hex id to " + fileName + " File.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads flood protector configurations.
	 * @param properties
	 */
	private static void loadFloodProtectorConfigs(Properties properties)
	{
		loadFloodProtectorConfig(properties, FLOOD_PROTECTOR_USE_ITEM, "UseItem", "4");
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
	}
	
	/**
	 * Loads single flood protector configuration.
	 * @param properties Properties file reader
	 * @param config flood protector configuration instance
	 * @param configString flood protector configuration string that determines for which flood protector configuration should be read
	 * @param defaultInterval default flood protector interval
	 */
	private static void loadFloodProtectorConfig(final Properties properties, final FloodProtectorConfig config, final String configString, final String defaultInterval)
	{
		config.FLOOD_PROTECTION_INTERVAL = Integer.parseInt(properties.getProperty(StringUtil.concat("FloodProtector", configString, "Interval"), defaultInterval));
		config.LOG_FLOODING = Boolean.parseBoolean(properties.getProperty(StringUtil.concat("FloodProtector", configString, "LogFlooding"), "False"));
		config.PUNISHMENT_LIMIT = Integer.parseInt(properties.getProperty(StringUtil.concat("FloodProtector", configString, "PunishmentLimit"), "0"));
		config.PUNISHMENT_TYPE = properties.getProperty(StringUtil.concat("FloodProtector", configString, "PunishmentType"), "none");
		config.PUNISHMENT_TIME = Integer.parseInt(properties.getProperty(StringUtil.concat("FloodProtector", configString, "PunishmentTime"), "0"));
	}
	
	public static class ClassMasterSettings
	{
		private final TIntObjectHashMap<TIntIntHashMap> _claimItems;
		private final TIntObjectHashMap<TIntIntHashMap> _rewardItems;
		private final TIntObjectHashMap<Boolean> _allowedClassChange;
		
		public ClassMasterSettings(String _configLine)
		{
			_claimItems = new TIntObjectHashMap<>(3);
			_rewardItems = new TIntObjectHashMap<>(3);
			_allowedClassChange = new TIntObjectHashMap<>(3);
			if (_configLine != null)
			{
				parseConfigLine(_configLine.trim());
			}
		}
		
		private void parseConfigLine(String _configLine)
		{
			final StringTokenizer st = new StringTokenizer(_configLine, ";");
			
			while (st.hasMoreTokens())
			{
				// get allowed class change
				final int job = Integer.parseInt(st.nextToken());
				
				_allowedClassChange.put(job, true);
				
				TIntIntHashMap _items = new TIntIntHashMap();
				// parse items needed for class change
				if (st.hasMoreTokens())
				{
					final StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");
					
					while (st2.hasMoreTokens())
					{
						final StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
						final int _itemId = Integer.parseInt(st3.nextToken());
						final int _quantity = Integer.parseInt(st3.nextToken());
						_items.put(_itemId, _quantity);
					}
				}
				
				_claimItems.put(job, _items);
				
				_items = new TIntIntHashMap();
				// parse gifts after class change
				if (st.hasMoreTokens())
				{
					final StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");
					
					while (st2.hasMoreTokens())
					{
						final StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
						final int _itemId = Integer.parseInt(st3.nextToken());
						final int _quantity = Integer.parseInt(st3.nextToken());
						_items.put(_itemId, _quantity);
					}
				}
				
				_rewardItems.put(job, _items);
			}
		}
		
		public boolean isAllowed(int job)
		{
			if (_allowedClassChange == null)
			{
				return false;
			}
			
			if (_allowedClassChange.containsKey(job))
			{
				return _allowedClassChange.get(job);
			}
			
			return false;
		}
		
		public TIntIntHashMap getRewardItems(int job)
		{
			if (_rewardItems.containsKey(job))
			{
				return _rewardItems.get(job);
			}
			
			return null;
		}
		
		public TIntIntHashMap getRequireItems(int job)
		{
			if (_claimItems.containsKey(job))
			{
				return _claimItems.get(job);
			}
			
			return null;
		}
	}
}