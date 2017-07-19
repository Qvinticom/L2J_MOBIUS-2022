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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.L2DatabaseFactory;
import com.l2jmobius.gameserver.datatables.SkillTable;
import com.l2jmobius.gameserver.instancemanager.OlympiadStadiumManager;
import com.l2jmobius.gameserver.model.Inventory;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.L2Party;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.L2Summon;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2CubicInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PetInstance;
import com.l2jmobius.gameserver.model.entity.Hero;
import com.l2jmobius.gameserver.model.entity.TvTEvent;
import com.l2jmobius.gameserver.network.serverpackets.ExOlympiadMatchEnd;
import com.l2jmobius.gameserver.network.serverpackets.ExOlympiadMode;
import com.l2jmobius.gameserver.network.serverpackets.ExOlympiadUserInfo;
import com.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.templates.StatsSet;
import com.l2jmobius.util.L2FastList;
import com.l2jmobius.util.Rnd;

import javolution.text.TextBuilder;
import javolution.util.FastMap;

/**
 * @author godson
 */
public class Olympiad
{
	protected static final Logger _log = Logger.getLogger(Olympiad.class.getName());
	
	private static Olympiad _instance;
	
	protected static Map<Integer, StatsSet> _nobles;
	protected static L2FastList<StatsSet> _heroesToBe;
	protected static L2FastList<L2PcInstance> _nonClassBasedRegisters;
	protected static Map<Integer, L2FastList<L2PcInstance>> _classBasedRegisters;
	
	public static final String OLYMPIAD_HTML_FILE = "data/html/olympiad/";
	private static final String OLYMPIAD_LOAD_NOBLES = "SELECT olympiad_nobles.char_id, olympiad_nobles.class_id, " + "characters.char_name, olympiad_nobles.olympiad_points, olympiad_nobles.competitions_done, " + "olympiad_nobles.competitions_won, olympiad_nobles.competitions_lost " + "FROM olympiad_nobles, characters WHERE characters.obj_Id = olympiad_nobles.char_id";
	private static final String OLYMPIAD_SAVE_NOBLES = "INSERT INTO olympiad_nobles " + "values (?,?,?,?,?,?)";
	private static final String OLYMPIAD_UPDATE_NOBLES = "UPDATE olympiad_nobles set " + "olympiad_points = ?, competitions_done = ?, competitions_won = ?, competitions_lost = ? where char_id = ?";
	private static final String OLYMPIAD_GET_HEROES = "SELECT olympiad_nobles.char_id, characters.char_name " + "FROM olympiad_nobles, characters WHERE characters.obj_Id = olympiad_nobles.char_id " + "AND olympiad_nobles.class_id = ? AND olympiad_nobles.competitions_done >= 5 AND olympiad_nobles.competitions_won > 0 " + "ORDER BY olympiad_nobles.olympiad_points DESC, olympiad_nobles.competitions_done DESC";
	private static final String GET_EACH_CLASS_LEADER = "SELECT characters.char_name from olympiad_nobles, characters " + "WHERE characters.obj_Id = olympiad_nobles.char_id AND olympiad_nobles.class_id = ? " + "AND olympiad_nobles.competitions_done >= 5 " + "ORDER BY olympiad_nobles.olympiad_points DESC, olympiad_nobles.competitions_done DESC";
	private static final String GET_RANKING_ALPHABETICALLY = "SELECT characters.char_name from olympiad_nobles, characters " + "WHERE characters.obj_Id = olympiad_nobles.char_id AND olympiad_nobles.class_id = ? " + "AND olympiad_nobles.competitions_done >= 5 ORDER BY characters.char_name";
	private static final String OLYMPIAD_DELETE_ALL = "DELETE from olympiad_nobles";
	private static final int[] HERO_IDS =
	{
		88,
		89,
		90,
		91,
		92,
		93,
		94,
		95,
		96,
		97,
		98,
		99,
		100,
		101,
		102,
		103,
		104,
		105,
		106,
		107,
		108,
		109,
		110,
		111,
		112,
		113,
		114,
		115,
		116,
		117,
		118
	};
	
	private static final int COMP_START = Config.ALT_OLY_START_TIME; // 8PM
	private static final int COMP_MIN = Config.ALT_OLY_MIN; // 00 mins
	private static final long COMP_PERIOD = Config.ALT_OLY_CPERIOD; // 4 hours
	protected static final long BATTLE_PERIOD = Config.ALT_OLY_BATTLE; // 3 mins
	protected static final long BATTLE_WAIT = Config.ALT_OLY_BWAIT; // 10mins
	protected static final long INITIAL_WAIT = Config.ALT_OLY_IWAIT; // 5mins
	protected static final long WEEKLY_PERIOD = Config.ALT_OLY_WPERIOD; // 1 week
	protected static final long VALIDATION_PERIOD = Config.ALT_OLY_VPERIOD; // 24 hours
	
	private static final int DEFAULT_POINTS = 18;
	protected static final int WEEKLY_POINTS = 3;
	
	public static final String CHAR_ID = "char_id";
	public static final String CLASS_ID = "class_id";
	public static final String CHAR_NAME = "char_name";
	public static final String POINTS = "olympiad_points";
	public static final String COMP_DONE = "competitions_done";
	public static final String COMP_WON = "competitions_won";
	public static final String COMP_LOST = "competitions_lost";
	
	protected long _olympiadEnd;
	protected long _validationEnd;
	protected int _period;
	protected long _nextWeeklyChange;
	protected int _currentCycle;
	private long _compEnd;
	
	protected static byte NONE = 0;
	protected static byte REGISTER = 1;
	protected static byte LASTFIGHT = 2;
	
	protected byte _compPeriodState = NONE;
	
	private Calendar _compStart;
	
	protected static boolean _compStarted = false;
	protected static boolean _battleStarted;
	
	protected ScheduledFuture<?> _scheduledCompStart;
	protected ScheduledFuture<?> _scheduledCompEnd;
	protected ScheduledFuture<?> _scheduledOlympiadEnd;
	protected ScheduledFuture<?> _scheduledWeeklyTask;
	protected ScheduledFuture<?> _scheduledValidationTask;
	
	protected static final Stadia[] STADIUMS =
	{
		new Stadia(-20814, -21189, -3030),
		new Stadia(-120324, -225077, -3331),
		new Stadia(-102495, -209023, -3331),
		new Stadia(-120156, -207378, -3331),
		new Stadia(-87628, -225021, -3331),
		new Stadia(-81705, -213209, -3331),
		new Stadia(-87593, -207339, -3331),
		new Stadia(-93709, -218304, -3331),
		new Stadia(-77157, -218608, -3331),
		new Stadia(-69682, -209027, -3331),
		new Stadia(-76887, -201256, -3331),
		new Stadia(-109985, -218701, -3331),
		new Stadia(-126367, -218228, -3331),
		new Stadia(-109629, -201292, -3331),
		new Stadia(-87523, -240169, -3331),
		new Stadia(-81748, -245950, -3331),
		new Stadia(-77123, -251473, -3331),
		new Stadia(-69778, -241801, -3331),
		new Stadia(-76754, -234014, -3331),
		new Stadia(-93742, -251032, -3331),
		new Stadia(-87466, -257752, -3331),
		new Stadia(-114413, -213241, -3331)
	};
	
	private static enum COMP_TYPE
	{
		CLASSED,
		NON_CLASSED
	}
	
	/**
	 * @author ascharot
	 */
	private class OlympiadGameTask implements Runnable
	{
		public L2OlympiadGame _game = null;
		
		private boolean _terminated = false;
		private boolean _started = false;
		
		public boolean isTerminated()
		{
			return _terminated || _game._aborted;
		}
		
		public boolean isStarted()
		{
			return _started;
		}
		
		public OlympiadGameTask(L2OlympiadGame game)
		{
			_game = game;
		}
		
		protected void cleanGame()
		{
			_started = false;
			_terminated = true;
			
			if (_game._gamestarted)
			{
				_game.PlayersStatusBack();
				_game.removals();
				_game.portPlayersBack();
				
			}
			
			_game._gamestarted = false;
			
			_game.clearPlayers();
			
			_game.clearSpectators();
			
			_manager.removeGame(_game);
			_game = null;
		}
		
		@Override
		public void run()
		{
			_started = true;
			if (_game != null)
			{
				if ((_game._playerOne != null) && (_game._playerTwo != null))
				{
					// Waiting to teleport to arena
					for (int i = 120; i > 10; i -= 5)
					{
						switch (i)
						{
							case 120:
							case 60:
							case 30:
							case 15:
								_game.sendMessageToPlayers(false, i);
								break;
						}
						
						try
						{
							Thread.sleep(5000);
						}
						catch (final InterruptedException e)
						{
						}
					}
					
					for (int i = 5; i > 0; i--)
					{
						_game.sendMessageToPlayers(false, i);
						try
						{
							Thread.sleep(1000);
						}
						catch (final InterruptedException e)
						{
						}
					}
					
					// Check if players are qualified to fight
					if (!_game._playerOne.checkOlympiadConditions())
					{
						_game._playerOne = null;
					}
					if (!_game._playerTwo.checkOlympiadConditions())
					{
						_game._playerTwo = null;
					}
					
					// Checking for opponents and teleporting to arena
					if (!_game.checkBattleStatus())
					{
						cleanGame();
						
						return;
					}
					
					_game.portPlayersToArena();
					_game.removals();
					
					try
					{
						Thread.sleep(5000);
					}
					catch (final InterruptedException e)
					{
					}
					
					synchronized (this)
					{
						if (!_battleStarted)
						{
							_battleStarted = true;
						}
					}
					
					for (int i = 60; i > 10; i -= 10)
					{
						_game.sendMessageToPlayers(true, i);
						try
						{
							Thread.sleep(10000);
						}
						catch (final InterruptedException e)
						{
						}
						
						if (i == 20)
						
						{
							
							_game.additions();
							_game.sendMessageToPlayers(true, 10);
							try
							{
								Thread.sleep(5000);
							}
							catch (final InterruptedException e)
							{
							}
						}
					}
					
					for (int i = 5; i > 0; i--)
					{
						_game.sendMessageToPlayers(true, i);
						try
						{
							Thread.sleep(1000);
						}
						catch (final InterruptedException e)
						{
						}
					}
					
					if (!_game.checkBattleStatus())
					{
						cleanGame();
						
						return;
					}
					
					_game.makeCompetitionStart();
					
					// Wait 6 mins (Battle)
					for (int i = 0; i < BATTLE_PERIOD; i += 10000)
					{
						try
						{
							Thread.sleep(10000);
							// If the game has Winner then stop waiting battle_period and validate winner
							if (_game.hasWinner())
							{
								break;
							}
							
							if (!_game.checkBattleStatus())
							{
								cleanGame();
								return;
							}
						}
						catch (final InterruptedException e)
						{
						}
					}
					
					_game.validateWinner();
					cleanGame();
				}
			}
		}
	}
	
	public static class Stadia
	{
		private int[] _coords = new int[3];
		private boolean _freeToUse = true;
		
		public boolean isFreeToUse()
		{
			return _freeToUse;
		}
		
		public void setStadiaBusy()
		{
			_freeToUse = false;
		}
		
		public void setStadiaFree()
		{
			_freeToUse = true;
		}
		
		public int[] getCoordinates()
		{
			return _coords;
		}
		
		public Stadia(int[] coords)
		{
			_coords = coords;
		}
		
		public Stadia(int x, int y, int z)
		{
			_coords[0] = x;
			_coords[1] = y;
			_coords[2] = z;
			
		}
	}
	
	protected static OlympiadManager _manager;
	
	public static Olympiad getInstance()
	{
		if (_instance == null)
		{
			_instance = new Olympiad();
		}
		return _instance;
	}
	
	public Olympiad()
	{
		try
		{
			load();
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		
		if (_period == 0)
		{
			init();
		}
	}
	
	private void load() throws IOException
	{
		_nobles = new FastMap<>();
		
		final Properties OlympiadProperties = new Properties();
		try (InputStream is = new FileInputStream(new File("./" + Config.OLYMPIAD_CONFIGURATION_FILE)))
		{
			OlympiadProperties.load(is);
		}
		
		_currentCycle = Integer.parseInt(OlympiadProperties.getProperty("CurrentCycle", "1"));
		_period = Integer.parseInt(OlympiadProperties.getProperty("Period", "0"));
		_olympiadEnd = Long.parseLong(OlympiadProperties.getProperty("OlympiadEnd", "0"));
		_validationEnd = Long.parseLong(OlympiadProperties.getProperty("ValidationEnd", "0"));
		_nextWeeklyChange = Long.parseLong(OlympiadProperties.getProperty("NextWeeklyChange", "0"));
		
		switch (_period)
		{
			case 0:
				if ((_olympiadEnd == 0) || (_olympiadEnd < Calendar.getInstance().getTimeInMillis()))
				{
					setNewOlympiadEnd();
				}
				else
				{
					scheduleWeeklyChange();
				}
				break;
			case 1:
				if (_validationEnd > Calendar.getInstance().getTimeInMillis())
				{
					_scheduledValidationTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValidationEndTask(), getMillisToValidationEnd());
				}
				else
				{
					_currentCycle++;
					_period = 0;
					deleteNobles();
					setNewOlympiadEnd();
				}
				break;
			default:
				_log.warning("Olympiad System: Omg something went wrong in loading!! Period = " + _period);
				return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(OLYMPIAD_LOAD_NOBLES);
			ResultSet rset = statement.executeQuery())
		{
			while (rset.next())
			{
				final StatsSet statDat = new StatsSet();
				final int charId = rset.getInt(CHAR_ID);
				statDat.set(CLASS_ID, rset.getInt(CLASS_ID));
				statDat.set(CHAR_NAME, rset.getString(CHAR_NAME));
				statDat.set(POINTS, rset.getInt(POINTS));
				statDat.set(COMP_DONE, rset.getInt(COMP_DONE));
				statDat.set(COMP_WON, rset.getInt(COMP_WON));
				statDat.set(COMP_LOST, rset.getInt(COMP_LOST));
				statDat.set("to_save", false);
				
				_nobles.put(charId, statDat);
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		
		synchronized (this)
		{
			_log.info("Olympiad System: Loading Olympiad System....");
			if (_period == 0)
			{
				_log.info("Olympiad System: Currently in Olympiad Period");
			}
			else
			{
				_log.info("Olympiad System: Currently in Validation Period");
			}
			
			_log.info("Olympiad System: Period Ends....");
			
			long milliToEnd;
			if (_period == 0)
			{
				milliToEnd = getMillisToOlympiadEnd();
			}
			else
			{
				milliToEnd = getMillisToValidationEnd();
			}
			
			final double numSecs = (milliToEnd / 1000) % 60;
			double countDown = ((milliToEnd / 1000) - numSecs) / 60;
			final int numMins = (int) Math.floor(countDown % 60);
			countDown = (countDown - numMins) / 60;
			final int numHours = (int) Math.floor(countDown % 24);
			final int numDays = (int) Math.floor((countDown - numHours) / 24);
			
			_log.info("Olympiad System: In " + numDays + " days, " + numHours + " hours and " + numMins + " mins.");
			
			if (_period == 0)
			{
				_log.info("Olympiad System: Next Weekly Change is in....");
				
				milliToEnd = getMillisToWeekChange();
				
				final double numSecs2 = (milliToEnd / 1000) % 60;
				double countDown2 = ((milliToEnd / 1000) - numSecs2) / 60;
				final int numMins2 = (int) Math.floor(countDown2 % 60);
				countDown2 = (countDown2 - numMins2) / 60;
				final int numHours2 = (int) Math.floor(countDown2 % 24);
				final int numDays2 = (int) Math.floor((countDown2 - numHours2) / 24);
				
				_log.info("Olympiad System: " + numDays2 + " days, " + numHours2 + " hours and " + numMins2 + " mins.");
			}
		}
		
		_log.info("Olympiad System: Loaded " + _nobles.size() + " Nobles");
	}
	
	protected void init()
	{
		if (_period == 1)
		{
			return;
		}
		
		_nonClassBasedRegisters = new L2FastList<>();
		_classBasedRegisters = new FastMap<>();
		
		_compStart = Calendar.getInstance();
		_compStart.set(Calendar.HOUR_OF_DAY, COMP_START);
		_compStart.set(Calendar.MINUTE, COMP_MIN);
		_compEnd = _compStart.getTimeInMillis() + COMP_PERIOD;
		
		if (_scheduledOlympiadEnd != null)
		{
			_scheduledOlympiadEnd.cancel(true);
		}
		
		_scheduledOlympiadEnd = ThreadPoolManager.getInstance().scheduleGeneral(new OlympiadEndTask(), getMillisToOlympiadEnd());
		
		updateCompStatus();
		
	}
	
	protected class OlympiadEndTask implements Runnable
	{
		@Override
		public void run()
		{
			final SystemMessage sm = new SystemMessage(SystemMessage.OLYMPIAD_PERIOD_S1_HAS_ENDED);
			sm.addNumber(_currentCycle);
			
			Announcements.getInstance().announceToAll(sm);
			Announcements.getInstance().announceToAll("Olympiad Validation Period has began.");
			
			if (_scheduledWeeklyTask != null)
			{
				_scheduledWeeklyTask.cancel(true);
			}
			
			saveNobleData();
			
			_period = 1;
			
			sortHeroesToBe();
			giveHeroBonus();
			
			Hero.getInstance().computeNewHeroes(_heroesToBe);
			
			try
			{
				save();
			}
			catch (final Exception e)
			{
				_log.warning("Olympiad System: Failed to save Olympiad configuration: " + e);
			}
			
			final Calendar validationEnd = Calendar.getInstance();
			_validationEnd = validationEnd.getTimeInMillis() + VALIDATION_PERIOD;
			
			_scheduledValidationTask = ThreadPoolManager.getInstance().scheduleGeneral(new ValidationEndTask(), getMillisToValidationEnd());
		}
	}
	
	protected class ValidationEndTask implements Runnable
	{
		@Override
		public void run()
		{
			Announcements.getInstance().announceToAll("Olympiad Validation Period has ended.");
			_period = 0;
			_currentCycle++;
			deleteNobles();
			setNewOlympiadEnd();
			init();
		}
	}
	
	public boolean registerNoble(L2PcInstance noble, boolean classBased)
	{
		
		if (_compPeriodState != REGISTER)
		{
			
			noble.sendPacket(new SystemMessage(SystemMessage.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS));
			return false;
		}
		
		// Checked in L2PcInstance
		if (!noble.checkOlympiadConditions())
		{
			return false;
		}
		
		if (_classBasedRegisters.containsKey(noble.getClassId().getId()))
		{
			final L2FastList<L2PcInstance> classed = _classBasedRegisters.get(noble.getClassId().getId());
			for (final L2PcInstance participant : classed)
			{
				if (participant.getObjectId() == noble.getObjectId())
				{
					
					noble.sendPacket(new SystemMessage(SystemMessage.YOU_ARE_ALREADY_ON_THE_WAITING_LIST_TO_PARTICIPATE_IN_THE_GAME_FOR_YOUR_CLASS));
					return false;
				}
			}
		}
		
		if (isRegisteredInComp(noble))
		{
			noble.sendPacket(new SystemMessage(SystemMessage.YOU_ARE_ALREADY_ON_THE_WAITING_LIST_FOR_ALL_CLASSES_WAITING_TO_PARTICIPATE_IN_THE_GAME));
			
			return false;
		}
		
		if (!_nobles.containsKey(noble.getObjectId()))
		{
			final StatsSet statDat = new StatsSet();
			statDat.set(CLASS_ID, noble.getClassId().getId());
			statDat.set(CHAR_NAME, noble.getName());
			statDat.set(POINTS, DEFAULT_POINTS);
			statDat.set(COMP_DONE, 0);
			statDat.set(COMP_WON, 0);
			statDat.set(COMP_LOST, 0);
			statDat.set("to_save", true);
			
			_nobles.put(noble.getObjectId(), statDat);
		}
		
		if (classBased && (getNoblePoints(noble.getObjectId()) < 3))
		{
			noble.sendMessage("Cannot register when you have less than 3 points.");
			return false;
		}
		
		if (!classBased && (getNoblePoints(noble.getObjectId()) < 5))
		{
			noble.sendMessage("Cannot register when you have less than 5 points.");
			return false;
		}
		
		if (classBased)
		{
			if (_classBasedRegisters.containsKey(noble.getClassId().getId()))
			{
				final L2FastList<L2PcInstance> classed = _classBasedRegisters.get(noble.getClassId().getId());
				classed.add(noble);
				
				_classBasedRegisters.remove(noble.getClassId().getId());
				_classBasedRegisters.put(noble.getClassId().getId(), classed);
				
			}
			else
			{
				final L2FastList<L2PcInstance> classed = new L2FastList<>();
				classed.add(noble);
				
				_classBasedRegisters.put(noble.getClassId().getId(), classed);
			}
			
			noble.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_CLASSIFIED_GAMES));
		}
		else
		{
			_nonClassBasedRegisters.add(noble);
			
			noble.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_NO_CLASS_GAMES));
		}
		
		return true;
	}
	
	public boolean isRegistered(L2PcInstance noble)
	{
		boolean result = false;
		
		if ((_nonClassBasedRegisters != null) && _nonClassBasedRegisters.contains(noble))
		{
			result = true;
		}
		else if ((_classBasedRegisters != null) && _classBasedRegisters.containsKey(noble.getClassId().getId()))
		{
			final L2FastList<L2PcInstance> classed = _classBasedRegisters.get(noble.getClassId().getId());
			if ((classed != null) && classed.contains(noble))
			{
				result = true;
			}
		}
		
		return result;
	}
	
	public boolean unRegisterNoble(L2PcInstance noble)
	{
		
		if (_compPeriodState == NONE)
		{
			
			noble.sendPacket(new SystemMessage(SystemMessage.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS));
			return false;
		}
		
		if (!noble.isNoble())
		{
			
			noble.sendPacket(new SystemMessage(SystemMessage.ONLY_NOBLESS_CAN_PARTICIPATE_IN_THE_OLYMPIAD));
			return false;
		}
		
		if (!isRegistered(noble))
		{
			
			noble.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_NOT_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_A_GAME));
			return false;
		}
		
		if (_nonClassBasedRegisters.contains(noble))
		{
			_nonClassBasedRegisters.remove(noble);
		}
		else
		{
			final L2FastList<L2PcInstance> classed = _classBasedRegisters.get(noble.getClassId().getId());
			classed.remove(noble);
			
			_classBasedRegisters.remove(noble.getClassId().getId());
			_classBasedRegisters.put(noble.getClassId().getId(), classed);
		}
		
		for (final L2OlympiadGame game : _manager.getOlympiadGames().values())
		{
			if (game == null)
			{
				continue;
			}
			
			if ((game._playerOneID == noble.getObjectId()) || (game._playerTwoID == noble.getObjectId()))
			{
				noble.sendMessage("Cannot cancel registration while you are already selected for a game.");
				return false;
			}
		}
		
		noble.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_BEEN_DELETED_FROM_THE_WAITING_LIST_OF_A_GAME));
		
		return true;
	}
	
	public void removeDisconnectedCompetitor(L2PcInstance player)
	{
		
		if ((_manager != null) && (_manager.getOlympiadInstance(player.getOlympiadGameId()) != null))
		{
			_manager.getOlympiadInstance(player.getOlympiadGameId()).handleDisconnect(player);
		}
		
		final L2FastList<L2PcInstance> classed = _classBasedRegisters.get(player.getClassId().getId());
		
		if (_nonClassBasedRegisters.contains(player))
		{
			_nonClassBasedRegisters.remove(player);
		}
		else if ((classed != null) && classed.contains(player))
		{
			classed.remove(player);
			_classBasedRegisters.remove(player.getClassId().getId());
			_classBasedRegisters.put(player.getClassId().getId(), classed);
		}
	}
	
	private void updateCompStatus()
	{
		
		synchronized (this)
		{
			final long milliToStart = getMillisToCompBegin();
			
			final double numSecs = (milliToStart / 1000) % 60;
			double countDown = ((milliToStart / 1000) - numSecs) / 60;
			final int numMins = (int) Math.floor(countDown % 60);
			countDown = (countDown - numMins) / 60;
			final int numHours = (int) Math.floor(countDown % 24);
			final int numDays = (int) Math.floor((countDown - numHours) / 24);
			
			_log.info("Olympiad System: Competition Period Starts in " + numDays + " days, " + numHours + " hours and " + numMins + " mins.");
			_log.info("Olympiad System: Event starts/started : " + _compStart.getTime());
		}
		
		_scheduledCompStart = ThreadPoolManager.getInstance().scheduleGeneral(() ->
		{
			if (isOlympiadEnd())
			{
				return;
			}
			
			_compPeriodState = REGISTER;
			
			Announcements.getInstance().announceToAll(new SystemMessage(SystemMessage.THE_OLYMPIAD_GAME_HAS_STARTED));
			
			_log.info("Olympiad System: Olympiad Game Started");
			
			final Thread olyCycle = new Thread(new OlympiadManager());
			
			olyCycle.start();
			
			final long regEnd = getMillisToCompEnd() - 600000;
			if (regEnd > 0)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(() ->
				{
					if (isOlympiadEnd())
					{
						return;
					}
					
					_compPeriodState = LASTFIGHT;
					Announcements.getInstance().announceToAll("Olympiad Registration Period has ended.");
				}, regEnd);
			}
			
			_scheduledCompEnd = ThreadPoolManager.getInstance().scheduleGeneral(() ->
			{
				if (isOlympiadEnd())
				{
					return;
				}
				
				_compPeriodState = NONE;
				Announcements.getInstance().announceToAll(new SystemMessage(SystemMessage.THE_OLYMPIAD_GAME_HAS_ENDED));
				_log.info("Olympiad System: Olympiad Game Ended");
				
				try
				{
					while (_battleStarted)
					{
						try
						{
							// wait 1 minute for pending games to end
							Thread.sleep(60000);
						}
						catch (final InterruptedException e1)
						{
						}
					}
					save();
				}
				catch (final Exception e2)
				{
					_log.warning("Olympiad System: Failed to save Olympiad configuration: " + e2);
				}
				
				init();
			}, getMillisToCompEnd());
		}, getMillisToCompBegin());
	}
	
	private long getMillisToOlympiadEnd()
	{
		
		return (_olympiadEnd - Calendar.getInstance().getTimeInMillis());
		
	}
	
	public void manualSelectHeroes()
	{
		if (_scheduledOlympiadEnd != null)
		{
			_scheduledOlympiadEnd.cancel(true);
		}
		
		_scheduledOlympiadEnd = ThreadPoolManager.getInstance().scheduleGeneral(new OlympiadEndTask(), 0);
	}
	
	protected long getMillisToValidationEnd()
	{
		if (_validationEnd > Calendar.getInstance().getTimeInMillis())
		{
			return (_validationEnd - Calendar.getInstance().getTimeInMillis());
		}
		return 10L;
	}
	
	public boolean isOlympiadEnd()
	{
		return (_period != 0);
	}
	
	protected void setNewOlympiadEnd()
	{
		final SystemMessage sm = new SystemMessage(SystemMessage.OLYMPIAD_PERIOD_S1_HAS_STARTED);
		sm.addNumber(_currentCycle);
		
		Announcements.getInstance().announceToAll(sm);
		
		final Calendar currentTime = Calendar.getInstance();
		currentTime.add(Calendar.MONTH, 1);
		currentTime.set(Calendar.DAY_OF_MONTH, 1);
		currentTime.set(Calendar.AM_PM, Calendar.AM);
		currentTime.set(Calendar.HOUR, 12);
		currentTime.set(Calendar.MINUTE, 0);
		currentTime.set(Calendar.SECOND, 0);
		_olympiadEnd = currentTime.getTimeInMillis();
		
		final Calendar nextChange = Calendar.getInstance();
		_nextWeeklyChange = nextChange.getTimeInMillis() + WEEKLY_PERIOD;
		scheduleWeeklyChange();
	}
	
	public byte getCompPeriodState()
	{
		return _compPeriodState;
	}
	
	private long getMillisToCompBegin()
	{
		if ((_compStart.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) && (_compEnd > Calendar.getInstance().getTimeInMillis()))
		{
			return 10L;
		}
		
		if (_compStart.getTimeInMillis() > Calendar.getInstance().getTimeInMillis())
		{
			return (_compStart.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
		}
		
		return setNewCompBegin();
	}
	
	private long setNewCompBegin()
	{
		_compStart = Calendar.getInstance();
		_compStart.set(Calendar.HOUR_OF_DAY, COMP_START);
		_compStart.set(Calendar.MINUTE, COMP_MIN);
		_compStart.add(Calendar.HOUR_OF_DAY, 24);
		_compEnd = _compStart.getTimeInMillis() + COMP_PERIOD;
		
		_log.info("Olympiad System: New Schedule @ " + _compStart.getTime());
		
		return (_compStart.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
	}
	
	protected long getMillisToCompEnd()
	{
		
		return (_compEnd - Calendar.getInstance().getTimeInMillis());
		
	}
	
	private long getMillisToWeekChange()
	{
		if (_nextWeeklyChange > Calendar.getInstance().getTimeInMillis())
		{
			return (_nextWeeklyChange - Calendar.getInstance().getTimeInMillis());
		}
		return 10L;
	}
	
	private void scheduleWeeklyChange()
	{
		_scheduledWeeklyTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() ->
		{
			addWeeklyPoints();
			_log.info("Olympiad System: Added weekly points to nobles");
			
			final Calendar nextChange = Calendar.getInstance();
			_nextWeeklyChange = nextChange.getTimeInMillis() + WEEKLY_PERIOD;
		}, getMillisToWeekChange(), WEEKLY_PERIOD);
	}
	
	protected synchronized void addWeeklyPoints()
	{
		if (_period == 1)
		{
			return;
		}
		
		for (final Integer nobleId : _nobles.keySet())
		{
			final StatsSet nobleInfo = _nobles.get(nobleId);
			int currentPoints = nobleInfo.getInteger(POINTS);
			currentPoints += WEEKLY_POINTS;
			nobleInfo.set(POINTS, currentPoints);
			
			_nobles.remove(nobleId);
			_nobles.put(nobleId, nobleInfo);
		}
	}
	
	public String[] getMatchList()
	{
		return (_manager == null) ? null : _manager.getAllTitles();
	}
	
	public int getCurrentCycle()
	{
		return _currentCycle;
	}
	
	public void addSpectator(int id, L2PcInstance spectator, boolean storeCoords)
	{
		if ((_manager == null) || (_manager.getOlympiadInstance(id) == null))
		{
			spectator.sendPacket(new SystemMessage(SystemMessage.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS));
			return;
		}
		
		if (isRegisteredInComp(spectator))
		{
			spectator.sendPacket(new SystemMessage(SystemMessage.WHILE_YOU_ARE_ON_THE_WAITING_LIST_YOU_ARE_NOT_ALLOWED_TO_WATCH_THE_GAME));
			return;
		}
		
		if (TvTEvent.isRegistered(spectator))
		{
			return;
		}
		
		if (spectator.getEventTeam() > 0)
		{
			return;
		}
		
		final L2PcInstance[] players = _manager.getOlympiadInstance(id).getPlayers();
		if (players == null)
		{
			return;
		}
		
		spectator.enterOlympiadObserverMode(STADIUMS[id].getCoordinates()[0], STADIUMS[id].getCoordinates()[1], STADIUMS[id].getCoordinates()[2], id, storeCoords);
		
		_manager.getOlympiadInstance(id).addSpectator(spectator);
	}
	
	public void removeSpectator(int id, L2PcInstance spectator)
	{
		
		if ((_manager != null) && (_manager.getOlympiadInstance(id) != null))
		{
			_manager.getOlympiadInstance(id).removeSpectator(spectator);
		}
		
		spectator.leaveOlympiadObserverMode();
	}
	
	public L2FastList<L2PcInstance> getSpectators(int id)
	{
		if ((_manager == null) || (_manager.getOlympiadInstance(id) == null))
		{
			return null;
		}
		return _manager.getOlympiadInstance(id).getSpectators();
	}
	
	public Map<Integer, L2OlympiadGame> getOlympiadGames()
	{
		return _manager.getOlympiadGames();
	}
	
	public boolean playerInStadium(L2PcInstance player)
	{
		return OlympiadStadiumManager.getInstance().getStadium(player) != null;
	}
	
	public int[] getWaitingList()
	{
		final int[] array = new int[2];
		
		if (_compPeriodState == NONE)
		{
			return null;
		}
		
		int classCount = 0;
		
		if (_classBasedRegisters.size() != 0)
		{
			for (final L2FastList<L2PcInstance> classed : _classBasedRegisters.values())
			{
				classCount += classed.size();
			}
			
		}
		
		array[0] = classCount;
		array[1] = _nonClassBasedRegisters.size();
		
		return array;
	}
	
	protected synchronized void saveNobleData()
	{
		if (_nobles == null)
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			for (final Integer nobleId : _nobles.keySet())
			{
				final StatsSet nobleInfo = _nobles.get(nobleId);
				
				if (nobleInfo == null)
				{
					continue;
				}
				
				final int charId = nobleId;
				final int classId = nobleInfo.getInteger(CLASS_ID);
				
				final int points = nobleInfo.getInteger(POINTS);
				final int compDone = nobleInfo.getInteger(COMP_DONE);
				final int compWon = nobleInfo.getInteger(COMP_WON);
				final int compLost = nobleInfo.getInteger(COMP_LOST);
				final boolean toSave = nobleInfo.getBool("to_save");
				
				if (toSave)
				{
					try (PreparedStatement statement = con.prepareStatement(OLYMPIAD_SAVE_NOBLES))
					{
						statement.setInt(1, charId);
						statement.setInt(2, classId);
						
						statement.setInt(3, points);
						statement.setInt(4, compDone);
						statement.setInt(5, compWon);
						statement.setInt(6, compLost);
						statement.execute();
					}
					
					nobleInfo.set("to_save", false);
					
					_nobles.remove(nobleId);
					_nobles.put(nobleId, nobleInfo);
				}
				else
				{
					try (PreparedStatement statement = con.prepareStatement(OLYMPIAD_UPDATE_NOBLES))
					{
						statement.setInt(1, points);
						statement.setInt(2, compDone);
						statement.setInt(3, compWon);
						statement.setInt(4, compLost);
						statement.setInt(5, charId);
						statement.execute();
					}
				}
			}
		}
		catch (final SQLException e)
		{
			_log.warning("Olympiad System: Couldnt save nobles info in db");
		}
	}
	
	protected void sortHeroesToBe()
	{
		if (_period != 1)
		{
			return;
		}
		
		_heroesToBe = new L2FastList<>();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			StatsSet hero;
			
			for (final int element : HERO_IDS)
			{
				try (PreparedStatement statement = con.prepareStatement(OLYMPIAD_GET_HEROES))
				{
					statement.setInt(1, element);
					try (ResultSet rset = statement.executeQuery())
					{
						if (rset.next())
						{
							hero = new StatsSet();
							hero.set(CLASS_ID, element);
							hero.set(CHAR_ID, rset.getInt(CHAR_ID));
							hero.set(CHAR_NAME, rset.getString(CHAR_NAME));
							
							_heroesToBe.add(hero);
						}
					}
				}
			}
		}
		catch (final SQLException e)
		{
			_log.warning("Olympiad System: Could not load heroes from db");
		}
	}
	
	public L2FastList<String> getClassLeaderBoard(int classId)
	{
		final L2FastList<String> names = new L2FastList<>();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(_period == 1 ? GET_RANKING_ALPHABETICALLY : GET_EACH_CLASS_LEADER))
		{
			statement.setInt(1, classId);
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					names.add(rset.getString(CHAR_NAME));
				}
			}
		}
		catch (final SQLException e)
		{
			_log.warning("Olympiad System: Could not load heroes from db");
		}
		
		return names;
	}
	
	protected void giveHeroBonus()
	{
		if (_heroesToBe.size() == 0)
		{
			return;
		}
		
		for (final StatsSet hero : _heroesToBe)
		{
			final int charId = hero.getInteger(CHAR_ID);
			
			final StatsSet noble = _nobles.get(charId);
			int currentPoints = noble.getInteger(POINTS);
			currentPoints += Config.ALT_OLY_HERO_POINTS;
			noble.set(POINTS, currentPoints);
			
			_nobles.remove(charId);
			_nobles.put(charId, noble);
		}
	}
	
	public int getNoblessePasses(int objId)
	{
		if ((_period != 1) || (_nobles.size() == 0))
		{
			return 0;
		}
		
		final StatsSet noble = _nobles.get(objId);
		if (noble == null)
		{
			return 0;
		}
		
		int points = noble.getInteger(POINTS);
		if (points < Config.ALT_OLY_MIN_POINT_FOR_EXCH)
		{
			return 0;
		}
		
		noble.set(POINTS, 0);
		_nobles.remove(objId);
		_nobles.put(objId, noble);
		
		points *= Config.ALT_OLY_GP_PER_POINT;
		
		return points;
	}
	
	public boolean isRegisteredInComp(L2PcInstance player)
	{
		boolean result = isRegistered(player);
		
		if (_compPeriodState != NONE)
		{
			
			for (final L2OlympiadGame game : _manager.getOlympiadGames().values())
			{
				if (game == null)
				{
					continue;
				}
				
				if ((game._playerOneID == player.getObjectId()) || (game._playerTwoID == player.getObjectId()))
				{
					result = true;
					break;
				}
			}
		}
		
		return result;
	}
	
	public int getNoblePoints(int objId)
	{
		if (_nobles.size() == 0)
		{
			return 0;
		}
		
		final StatsSet noble = _nobles.get(objId);
		if (noble == null)
		{
			return 0;
		}
		final int points = noble.getInteger(POINTS);
		
		return points;
	}
	
	public int getCompetitionDone(int objId)
	{
		if (_nobles.size() == 0)
		{
			return 0;
		}
		
		final StatsSet noble = _nobles.get(objId);
		if (noble == null)
		{
			return 0;
		}
		final int points = noble.getInteger(COMP_DONE);
		
		return points;
	}
	
	public int getCompetitionWon(int objId)
	{
		if (_nobles.size() == 0)
		{
			return 0;
		}
		
		final StatsSet noble = _nobles.get(objId);
		if (noble == null)
		{
			return 0;
		}
		final int points = noble.getInteger(COMP_WON);
		
		return points;
	}
	
	public int getCompetitionLost(int objId)
	{
		if (_nobles.size() == 0)
		{
			return 0;
		}
		
		final StatsSet noble = _nobles.get(objId);
		if (noble == null)
		{
			return 0;
		}
		final int points = noble.getInteger(COMP_LOST);
		
		return points;
	}
	
	protected void deleteNobles()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(OLYMPIAD_DELETE_ALL))
		{
			statement.execute();
		}
		catch (final SQLException e)
		{
			_log.warning("Olympiad System: Couldnt delete nobles from db");
		}
		
		_nobles.clear();
	}
	
	public void save() throws IOException
	{
		saveNobleData();
		
		final Properties OlympiadProperties = new Properties();
		
		OlympiadProperties.setProperty("CurrentCycle", String.valueOf(_currentCycle));
		OlympiadProperties.setProperty("Period", String.valueOf(_period));
		OlympiadProperties.setProperty("OlympiadEnd", String.valueOf(_olympiadEnd));
		OlympiadProperties.setProperty("ValidationEnd", String.valueOf(_validationEnd));
		OlympiadProperties.setProperty("NextWeeklyChange", String.valueOf(_nextWeeklyChange));
		
		try (FileOutputStream fos = new FileOutputStream(new File(Config.DATAPACK_ROOT, Config.OLYMPIAD_CONFIGURATION_FILE)))
		{
			OlympiadProperties.store(fos, "Olympiad Properties");
		}
	}
	
	private class OlympiadManager implements Runnable
	{
		private final Map<Integer, L2OlympiadGame> _olympiadInstances;
		
		public OlympiadManager()
		{
			_olympiadInstances = new FastMap<>();
			_manager = this;
		}
		
		@Override
		public synchronized void run()
		{
			
			if (isOlympiadEnd())
			{
				return;
			}
			
			final Map<Integer, OlympiadGameTask> _gamesQueue = new FastMap<>();
			while (getCompPeriodState() != NONE)
			{
				if (_nobles.size() == 0)
				{
					try
					{
						wait(60000);
					}
					catch (final InterruptedException ex)
					{
						return;
					}
					continue;
				}
				
				int _gamesQueueSize = 0;
				
				int classBasedPgCount = 0;
				for (final L2FastList<L2PcInstance> classList : _classBasedRegisters.values())
				{
					classBasedPgCount += classList.size();
				}
				
				if ((classBasedPgCount >= Config.ALT_OLY_CLASSED) || (_nonClassBasedRegisters.size() >= Config.ALT_OLY_NONCLASSED))
				{
					
					// set up the games queue
					for (int i = 0; i < STADIUMS.length; i++)
					{
						if (!existNextOpponents(_nonClassBasedRegisters) && !existNextOpponents(getRandomClassList(_classBasedRegisters)))
						{
							break;
						}
						
						if (STADIUMS[i].isFreeToUse())
						{
							if (existNextOpponents(_nonClassBasedRegisters))
							{
								try
								{
									_olympiadInstances.put(i, new L2OlympiadGame(i, COMP_TYPE.NON_CLASSED, nextOpponents(_nonClassBasedRegisters), STADIUMS[i].getCoordinates()));
									_gamesQueue.put(i, new OlympiadGameTask(_olympiadInstances.get(i)));
									STADIUMS[i].setStadiaBusy();
								}
								catch (final Exception ex)
								{
									if (_olympiadInstances.get(i) != null)
									{
										for (final L2PcInstance player : _olympiadInstances.get(i).getPlayers())
										{
											player.sendMessage("Your olympiad registration was cancelled due to an error.");
											player.setIsInOlympiadMode(false);
											player.setIsOlympiadStart(false);
											player.setOlympiadSide(-1);
											player.setOlympiadGameId(-1);
										}
										_olympiadInstances.remove(i);
									}
									
									if (_gamesQueue.get(i) != null)
									{
										_gamesQueue.remove(i);
									}
									STADIUMS[i].setStadiaFree();
									
									// try to reuse this stadium next time
									i--;
								}
							}
							else if (existNextOpponents(getRandomClassList(_classBasedRegisters)))
							{
								try
								{
									_olympiadInstances.put(i, new L2OlympiadGame(i, COMP_TYPE.CLASSED, nextOpponents(getRandomClassList(_classBasedRegisters)), STADIUMS[i].getCoordinates()));
									_gamesQueue.put(i, new OlympiadGameTask(_olympiadInstances.get(i)));
									STADIUMS[i].setStadiaBusy();
								}
								catch (final Exception ex)
								{
									if (_olympiadInstances.get(i) != null)
									{
										for (final L2PcInstance player : _olympiadInstances.get(i).getPlayers())
										{
											player.sendMessage("Your olympiad registration was cancelled due to an error.");
											player.setIsInOlympiadMode(false);
											player.setIsOlympiadStart(false);
											player.setOlympiadSide(-1);
											player.setOlympiadGameId(-1);
										}
										_olympiadInstances.remove(i);
									}
									
									if (_gamesQueue.get(i) != null)
									{
										_gamesQueue.remove(i);
									}
									STADIUMS[i].setStadiaFree();
									
									// try to reuse this stadium next time
									i--;
								}
							}
						}
						else
						{
							if ((_gamesQueue.get(i) == null) || _gamesQueue.get(i).isTerminated() || (_gamesQueue.get(i)._game == null))
							{
								try
								{
									_olympiadInstances.remove(i);
									_gamesQueue.remove(i);
									STADIUMS[i].setStadiaFree();
									i--;
								}
								catch (final Exception e)
								{
									e.printStackTrace();
								}
							}
						}
					}
					
					// Start games
					_gamesQueueSize = _gamesQueue.size();
					for (int i = 0; i < _gamesQueueSize; i++)
					{
						if ((_gamesQueue.get(i) != null) && !_gamesQueue.get(i).isTerminated() && !_gamesQueue.get(i).isStarted())
						{
							// start new games
							final Thread T = new Thread(_gamesQueue.get(i));
							T.start();
						}
					}
				}
				
				// wait 30 sec due to server stress
				try
				{
					wait(30000);
				}
				catch (final InterruptedException e)
				{
					return;
				}
			}
			
			// when comp time finish wait for all games to be terminated before executing the cleanup code
			boolean allGamesTerminated = false;
			// wait for all games to be terminated
			while (!allGamesTerminated)
			{
				try
				{
					wait(30000);
				}
				catch (final InterruptedException e)
				{
				}
				
				if (_gamesQueue.size() == 0)
				{
					allGamesTerminated = true;
				}
				else
				{
					for (final OlympiadGameTask game : _gamesQueue.values())
					{
						allGamesTerminated = allGamesTerminated || game.isTerminated();
					}
				}
			}
			
			// when all games are terminated clear all
			_gamesQueue.clear();
			
			// Wait 20 seconds
			_olympiadInstances.clear();
			_classBasedRegisters.clear();
			_nonClassBasedRegisters.clear();
			
			_battleStarted = false;
		}
		
		protected L2OlympiadGame getOlympiadInstance(int index)
		{
			if ((_olympiadInstances != null) && (_olympiadInstances.size() > 0))
			{
				return _olympiadInstances.get(index);
			}
			return null;
		}
		
		protected Map<Integer, L2OlympiadGame> getOlympiadGames()
		{
			return (_olympiadInstances == null) ? null : _olympiadInstances;
		}
		
		protected int getSpectatedGame(L2PcInstance player)
		{
			
			for (int i = 0; i < _olympiadInstances.size(); i++)
			{
				if ((getSpectators(i) != null) && getSpectators(i).contains(player))
				{
					return i;
				}
			}
			return -1;
		}
		
		private L2FastList<L2PcInstance> getRandomClassList(Map<Integer, L2FastList<L2PcInstance>> list)
		{
			if (list.size() == 0)
			{
				return null;
			}
			
			final Map<Integer, L2FastList<L2PcInstance>> tmp = new FastMap<>();
			int tmpIndex = 0;
			for (final L2FastList<L2PcInstance> l : list.values())
			{
				tmp.put(tmpIndex, l);
				tmpIndex++;
			}
			
			L2FastList<L2PcInstance> rndList = new L2FastList<>();
			int classIndex = 0;
			
			if (tmp.size() == 1)
			{
				classIndex = 0;
			}
			else
			{
				classIndex = Rnd.nextInt(tmp.size());
			}
			rndList = tmp.get(classIndex);
			return rndList;
		}
		
		private L2FastList<L2PcInstance> nextOpponents(L2FastList<L2PcInstance> list)
		{
			final L2FastList<L2PcInstance> opponents = new L2FastList<>();
			if (list.size() == 0)
			{
				return opponents;
			}
			
			final int loopCount = (list.size() / 2);
			int first;
			int second;
			
			if (loopCount < 1)
			{
				return opponents;
			}
			
			first = Rnd.nextInt(list.size());
			opponents.add(list.get(first));
			list.remove(first);
			
			second = Rnd.nextInt(list.size());
			opponents.add(list.get(second));
			list.remove(second);
			
			return opponents;
			
		}
		
		private boolean existNextOpponents(L2FastList<L2PcInstance> list)
		{
			if (list == null)
			{
				return false;
			}
			
			if (list.size() == 0)
			{
				return false;
			}
			
			final int loopCount = list.size() >> 1;
			if (loopCount < 1)
			{
				return false;
			}
			
			return true;
			
		}
		
		protected String[] getAllTitles()
		{
			if (_olympiadInstances.size() == 0)
			{
				return null;
			}
			
			final String[] msg = new String[_olympiadInstances.size()];
			int count = 0;
			// int match = 1;
			int showbattle = 0;
			
			for (final L2OlympiadGame instance : _olympiadInstances.values())
			{
				if (instance._gamestarted == false)
				{
					return null;
				}
				
				showbattle = 1;
				
				msg[count] = "<" + showbattle + "><" + instance._stadiumID + "> In Progress: " + instance.getTitle();
				count++;
				// match++;
			}
			
			return msg;
		}
		
		protected void removeGame(L2OlympiadGame game)
		{
			if ((_olympiadInstances != null) && !_olympiadInstances.isEmpty())
			{
				for (int i = 0; i < _olympiadInstances.size(); i++)
				{
					if (_olympiadInstances.get(i) == game)
					{
						_olympiadInstances.remove(i);
					}
				}
			}
		}
	}
	
	private class L2OlympiadGame
	{
		protected COMP_TYPE _type;
		public boolean _aborted;
		public boolean _gamestarted;
		public boolean _playerOneDisconnected;
		public boolean _playerTwoDisconnected;
		public String _playerOneName;
		public String _playerTwoName;
		public int _playerOneID = 0;
		public int _playerTwoID = 0;
		
		public L2PcInstance _playerOne;
		public L2PcInstance _playerTwo;
		// public L2Spawn _spawnOne;
		// public L2Spawn _spawnTwo;
		private L2FastList<L2PcInstance> _players;
		private int[] _stadiumPort;
		private int x1, y1, z1, x2, y2, z2;
		public int _stadiumID;
		public L2FastList<L2PcInstance> _spectators;
		private SystemMessage _sm;
		private SystemMessage _sm2;
		private SystemMessage _sm3;
		
		protected L2OlympiadGame(int id, COMP_TYPE type, L2FastList<L2PcInstance> list, int[] stadiumPort)
		{
			_aborted = false;
			_gamestarted = false;
			_stadiumID = id;
			_playerOneDisconnected = false;
			_playerTwoDisconnected = false;
			_type = type;
			_stadiumPort = stadiumPort;
			_spectators = new L2FastList<>();
			
			if (list != null)
			{
				_players = list;
				_playerOne = list.get(0);
				_playerTwo = list.get(1);
				
				try
				{
					_playerOneName = _playerOne.getName();
					_playerTwoName = _playerTwo.getName();
					_playerOne.setOlympiadGameId(id);
					_playerTwo.setOlympiadGameId(id);
					_playerOneID = _playerOne.getObjectId();
					_playerTwoID = _playerTwo.getObjectId();
				}
				catch (final Exception e)
				{
					_aborted = true;
					clearPlayers();
				}
				
				if (Config.DEBUG)
				{
					_log.info("Olympiad System: Game - " + id + ": " + _playerOne.getName() + " Vs " + _playerTwo.getName());
				}
			}
			else
			{
				_aborted = true;
				clearPlayers();
				return;
			}
		}
		
		// public boolean isAborted()
		// {
		// return _aborted;
		// }
		
		protected void clearPlayers()
		{
			_playerOne = null;
			_playerTwo = null;
			_players = null;
			_playerOneName = "";
			_playerTwoName = "";
			_playerOneID = 0;
			_playerTwoID = 0;
		}
		
		protected void handleDisconnect(L2PcInstance player)
		{
			if (player == _playerOne)
			{
				_playerOneDisconnected = true;
			}
			else if (player == _playerTwo)
			{
				_playerTwoDisconnected = true;
			}
		}
		
		protected void removals()
		{
			if (_aborted)
			{
				return;
			}
			
			if ((_playerOne == null) || (_playerTwo == null))
			{
				return;
			}
			
			if (_playerOneDisconnected || _playerTwoDisconnected)
			{
				return;
			}
			
			for (final L2PcInstance player : _players)
			{
				try
				{
					
					// Abort casting if player casting
					if (player.isCastingNow())
					{
						player.abortCast();
					}
					
					player.getAppearance().setVisible();
					
					// Heal Player fully
					player.setCurrentCp(player.getMaxCp());
					player.setCurrentHp(player.getMaxHp());
					player.setCurrentMp(player.getMaxMp());
					
					// Remove Buffs
					player.stopAllEffects();
					player.addCharge(-player.getCharges());
					
					// Remove Summon's Buffs
					if (player.getPet() != null)
					{
						final L2Summon summon = player.getPet();
						summon.stopAllEffects();
						
						if (summon.isCastingNow())
						{
							summon.abortCast();
						}
						
						if (summon instanceof L2PetInstance)
						{
							summon.unSummon(player);
						}
					}
					
					if (player.getCubics() != null)
					{
						boolean removed = false;
						for (final L2CubicInstance cubic : player.getCubics().values())
						{
							if (cubic.givenByOther())
							{
								cubic.stopAction();
								cubic.cancelDisappear();
								player.delCubic(cubic.getId());
								removed = true;
							}
						}
						
						if (removed)
						{
							player.broadcastUserInfo();
						}
					}
					
					// Remove player from his party
					if (player.getParty() != null)
					{
						final L2Party party = player.getParty();
						party.removePartyMember(player, true);
					}
					
					// Remove Hero Weapons
					// check to prevent the using of weapon/shield on strider/wyvern
					L2ItemInstance wpn = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
					if (wpn == null)
					{
						wpn = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND);
					}
					
					final int itemId = wpn.getItemId();
					if ((itemId >= 6611) && (itemId <= 6621))
					
					{
						final L2ItemInstance[] unequiped = player.getInventory().unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart());
						final InventoryUpdate iu = new InventoryUpdate();
						for (final L2ItemInstance element : unequiped)
						{
							iu.addModifiedItem(element);
						}
						player.sendPacket(iu);
						player.abortAttack();
						player.broadcastUserInfo();
						
						// this can be 0 if the user pressed the right mouse button twice very fast
						if (unequiped.length > 0)
						{
							if (unequiped[0].isWear())
							{
								return;
							}
							
							SystemMessage sm = null;
							if (unequiped[0].getEnchantLevel() > 0)
							{
								sm = new SystemMessage(SystemMessage.EQUIPMENT_S1_S2_REMOVED);
								sm.addNumber(unequiped[0].getEnchantLevel());
								sm.addItemName(unequiped[0].getItemId());
							}
							else
							{
								sm = new SystemMessage(SystemMessage.S1_DISARMED);
								sm.addItemName(unequiped[0].getItemId());
							}
							player.sendPacket(sm);
						}
					}
					
					// Remove shot automation
					player.disableAutoShotsAll();
					
					// Discharge any active shots
					player.getActiveWeaponInstance().setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
					player.getActiveWeaponInstance().setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
				}
				catch (final Exception e)
				{
				}
			}
		}
		
		protected boolean portPlayersToArena()
		{
			final boolean _playerOneCrash = ((_playerOne == null) || _playerOneDisconnected);
			final boolean _playerTwoCrash = ((_playerTwo == null) || _playerTwoDisconnected);
			
			if (_playerOneCrash || _playerTwoCrash || _aborted)
			{
				_playerOne = null;
				_playerTwo = null;
				_aborted = true;
				return false;
			}
			
			try
			{
				x1 = _playerOne.getX();
				y1 = _playerOne.getY();
				z1 = _playerOne.getZ();
				
				x2 = _playerTwo.getX();
				y2 = _playerTwo.getY();
				z2 = _playerTwo.getZ();
				
				if (_playerOne.isSitting())
				{
					_playerOne.standUp();
				}
				
				if (_playerTwo.isSitting())
				{
					_playerTwo.standUp();
				}
				
				_playerOne.setTarget(null);
				_playerTwo.setTarget(null);
				
				_playerOne.teleToLocation(_stadiumPort[0], _stadiumPort[1], _stadiumPort[2], true);
				_playerTwo.teleToLocation(_stadiumPort[0], _stadiumPort[1], _stadiumPort[2], true);
				
				_playerOne.setIsInOlympiadMode(true);
				_playerOne.setIsOlympiadStart(false);
				_playerOne.setOlympiadSide(1);
				
				_playerTwo.setIsInOlympiadMode(true);
				_playerTwo.setIsOlympiadStart(false);
				_playerTwo.setOlympiadSide(2);
				
				_playerOne.sendPacket(new ExOlympiadMode(2));
				_playerTwo.sendPacket(new ExOlympiadMode(2));
				
				_gamestarted = true;
			}
			catch (final NullPointerException e)
			{
				return false;
			}
			return true;
		}
		
		protected void sendMessageToPlayers(boolean toBattleBegin, int nsecond)
		{
			
			if (!toBattleBegin)
			{
				_sm = new SystemMessage(SystemMessage.YOU_WILL_ENTER_THE_OLYMPIAD_STADIUM_IN_S1_SECOND_S);
			}
			else
			{
				_sm = new SystemMessage(SystemMessage.THE_GAME_WILL_START_IN_S1_SECOND_S);
			}
			
			_sm.addNumber(nsecond);
			
			for (final L2PcInstance player : _players)
			{
				if (player != null)
				{
					player.sendPacket(_sm);
				}
			}
		}
		
		protected void broadcastMessage(SystemMessage sm, boolean toAll)
		{
			for (final L2PcInstance player : _players)
			{
				if (player != null)
				{
					player.sendPacket(sm);
				}
			}
			
			if (toAll && (_spectators != null))
			{
				for (final L2PcInstance spec : _spectators)
				{
					if (spec == null)
					{
						continue;
					}
					
					spec.sendPacket(sm);
				}
			}
		}
		
		protected void portPlayersBack()
		{
			if (_playerOne != null)
			{
				
				_playerOne.sendPacket(new ExOlympiadMatchEnd());
				_playerOne.teleToLocation(x1, y1, z1, true);
			}
			
			if (_playerTwo != null)
			{
				
				_playerTwo.sendPacket(new ExOlympiadMatchEnd());
				_playerTwo.teleToLocation(x2, y2, z2, true);
			}
		}
		
		protected void PlayersStatusBack()
		{
			for (final L2PcInstance player : _players)
			{
				try
				{
					if (player.isDead())
					{
						player.setIsDead(false);
					}
					
					player.getStatus().startHpMpRegeneration();
					player.setCurrentCp(player.getMaxCp());
					player.setCurrentHp(player.getMaxHp());
					player.setCurrentMp(player.getMaxMp());
					player.setIsInOlympiadMode(false);
					player.setIsOlympiadStart(false);
					player.setOlympiadSide(-1);
					player.setOlympiadGameId(-1);
					player.sendPacket(new ExOlympiadMode(0));
				}
				catch (final Exception e)
				{
				}
			}
		}
		
		protected boolean checkBattleStatus()
		{
			
			final boolean _pOneCrash = ((_playerOne == null) || _playerOneDisconnected);
			final boolean _pTwoCrash = ((_playerTwo == null) || _playerTwoDisconnected);
			
			if (_pOneCrash || _pTwoCrash || _aborted)
			{
				final StatsSet playerOneStat = _nobles.get(_playerOneID);
				final StatsSet playerTwoStat = _nobles.get(_playerTwoID);
				
				final int playerOnePlayed = playerOneStat.getInteger(COMP_DONE);
				final int playerTwoPlayed = playerTwoStat.getInteger(COMP_DONE);
				final int playerOneWon = playerOneStat.getInteger(COMP_WON);
				final int playerTwoWon = playerTwoStat.getInteger(COMP_WON);
				final int playerOneLost = playerOneStat.getInteger(COMP_LOST);
				final int playerTwoLost = playerTwoStat.getInteger(COMP_LOST);
				final int playerOnePoints = playerOneStat.getInteger(POINTS);
				
				final int playerTwoPoints = playerTwoStat.getInteger(POINTS);
				
				if (_pOneCrash && !_pTwoCrash)
				{
					try
					{
						final int transferPoints = playerOnePoints / 5;
						
						playerOneStat.set(POINTS, playerOnePoints - transferPoints);
						playerOneStat.set(COMP_LOST, playerOneLost + 1);
						
						if (Config.DEBUG)
						{
							_log.info("Olympia Result: " + _playerOneName + " vs " + _playerTwoName + " ... " + _playerOneName + " lost " + transferPoints + " points for crash");
						}
						
						playerTwoStat.set(POINTS, playerTwoPoints + transferPoints);
						playerTwoStat.set(COMP_WON, playerTwoWon + 1);
						
						if (Config.DEBUG)
						{
							_log.info("Olympia Result: " + _playerOneName + " vs " + _playerTwoName + " ... " + _playerTwoName + " Win " + transferPoints + " points");
						}
						
						_sm = new SystemMessage(SystemMessage.S1_HAS_WON_THE_GAME);
						_sm2 = new SystemMessage(SystemMessage.S1_HAS_GAINED_S2_OLYMPIAD_POINTS);
						_sm3 = new SystemMessage(SystemMessage.S1_HAS_LOST_S2_OLYMPIAD_POINTS);
						_sm.addString(_playerTwoName);
						broadcastMessage(_sm, true);
						_sm2.addString(_playerTwoName);
						_sm2.addNumber(transferPoints);
						broadcastMessage(_sm2, false);
						_sm3.addString(_playerOneName);
						_sm3.addNumber(transferPoints);
						broadcastMessage(_sm3, false);
					}
					catch (final Exception e)
					{
						e.printStackTrace();
					}
				}
				else if (_pTwoCrash && !_pOneCrash)
				{
					try
					{
						
						final int transferPoints = playerTwoPoints / 5;
						playerTwoStat.set(POINTS, playerTwoPoints - transferPoints);
						playerTwoStat.set(COMP_LOST, playerTwoLost + 1);
						
						if (Config.DEBUG)
						{
							_log.info("Olympia Result: " + _playerTwoName + " vs " + _playerOneName + " ... " + _playerTwoName + " lost " + transferPoints + " points for crash");
						}
						
						playerOneStat.set(POINTS, playerOnePoints + transferPoints);
						playerOneStat.set(COMP_WON, playerOneWon + 1);
						
						if (Config.DEBUG)
						{
							_log.info("Olympia Result: " + _playerTwoName + " vs " + _playerOneName + " ... " + _playerOneName + " Win " + transferPoints + " points");
						}
						
						_sm = new SystemMessage(SystemMessage.S1_HAS_WON_THE_GAME);
						_sm2 = new SystemMessage(SystemMessage.S1_HAS_GAINED_S2_OLYMPIAD_POINTS);
						_sm3 = new SystemMessage(SystemMessage.S1_HAS_LOST_S2_OLYMPIAD_POINTS);
						_sm.addString(_playerOneName);
						broadcastMessage(_sm, true);
						_sm2.addString(_playerOneName);
						_sm2.addNumber(transferPoints);
						broadcastMessage(_sm2, false);
						_sm3.addString(_playerTwoName);
						_sm3.addNumber(transferPoints);
						broadcastMessage(_sm3, false);
					}
					catch (final Exception e)
					{
						e.printStackTrace();
					}
				}
				else if (_pOneCrash && _pTwoCrash)
				{
					try
					{
						final int pointDiff = Math.min(playerOnePoints, playerTwoPoints) / 5;
						playerOneStat.set(POINTS, playerOnePoints - pointDiff);
						playerTwoStat.set(POINTS, playerTwoPoints - pointDiff);
						
						if (Config.DEBUG)
						{
							_log.info("Olympia Result: " + _playerOneName + " vs " + _playerTwoName + " ... " + " both lost " + pointDiff + " points for crash");
						}
						
						_sm = new SystemMessage(SystemMessage.S1_HAS_LOST_S2_OLYMPIAD_POINTS);
						_sm.addString(_playerOneName);
						_sm.addNumber(pointDiff);
						broadcastMessage(_sm, false);
						_sm2 = new SystemMessage(SystemMessage.S1_HAS_LOST_S2_OLYMPIAD_POINTS);
						_sm2.addString(_playerTwoName);
						_sm2.addNumber(pointDiff);
						broadcastMessage(_sm2, false);
					}
					catch (final Exception e)
					{
						e.printStackTrace();
					}
				}
				
				playerOneStat.set(COMP_DONE, playerOnePlayed + 1);
				
				playerTwoStat.set(COMP_DONE, playerTwoPlayed + 1);
				
				return false;
			}
			
			return true;
		}
		
		protected boolean hasWinner()
		{
			
			if (_aborted || (_playerOne == null) || (_playerTwo == null))
			{
				return true;
			}
			
			double playerOneHp = 0;
			
			try
			{
				if ((_playerOne != null) && (_playerOne.getOlympiadGameId() != -1))
				{
					playerOneHp = _playerOne.getCurrentHp();
				}
			}
			catch (final Exception e)
			{
				playerOneHp = 0;
			}
			
			double playerTwoHp = 0;
			try
			{
				if ((_playerTwo != null) && (_playerTwo.getOlympiadGameId() != -1))
				{
					playerTwoHp = _playerTwo.getCurrentHp();
				}
			}
			catch (final Exception e)
			{
				playerTwoHp = 0;
			}
			
			if ((playerTwoHp == 0) || (playerOneHp == 0))
			{
				return true;
			}
			
			return false;
		}
		
		protected void validateWinner()
		{
			if (_aborted || (_playerOne == null) || (_playerTwo == null) || _playerOneDisconnected || _playerTwoDisconnected)
			{
				return;
			}
			
			StatsSet playerOneStat;
			StatsSet playerTwoStat;
			
			playerOneStat = _nobles.get(_playerOneID);
			playerTwoStat = _nobles.get(_playerTwoID);
			
			int _div;
			int _gpreward;
			
			final int playerOnePlayed = playerOneStat.getInteger(COMP_DONE);
			final int playerTwoPlayed = playerTwoStat.getInteger(COMP_DONE);
			final int playerOneWon = playerOneStat.getInteger(COMP_WON);
			final int playerTwoWon = playerTwoStat.getInteger(COMP_WON);
			final int playerOneLost = playerOneStat.getInteger(COMP_LOST);
			final int playerTwoLost = playerTwoStat.getInteger(COMP_LOST);
			
			final int playerOnePoints = playerOneStat.getInteger(POINTS);
			final int playerTwoPoints = playerTwoStat.getInteger(POINTS);
			
			double playerOneHp = 0;
			try
			{
				if ((_playerOne != null) && !_playerOneDisconnected)
				{
					if (!_playerOne.isDead())
					{
						playerOneHp = _playerOne.getCurrentHp() + _playerOne.getCurrentCp();
					}
				}
			}
			catch (final Exception e)
			{
				playerOneHp = 0;
			}
			
			double playerTwoHp = 0;
			try
			{
				if ((_playerTwo != null) && !_playerTwoDisconnected)
				{
					if (!_playerTwo.isDead())
					{
						playerTwoHp = _playerTwo.getCurrentHp() + _playerTwo.getCurrentCp();
					}
				}
			}
			catch (final Exception e)
			{
				playerTwoHp = 0;
			}
			
			_sm = new SystemMessage(SystemMessage.S1_HAS_WON_THE_GAME);
			_sm2 = new SystemMessage(SystemMessage.S1_HAS_GAINED_S2_OLYMPIAD_POINTS);
			_sm3 = new SystemMessage(SystemMessage.S1_HAS_LOST_S2_OLYMPIAD_POINTS);
			
			String result = "";
			
			// if players crashed, search if they've relogged
			_playerOne = L2World.getInstance().getPlayer(_playerOneName);
			_players.set(0, _playerOne);
			_playerTwo = L2World.getInstance().getPlayer(_playerTwoName);
			_players.set(1, _playerTwo);
			
			switch (_type)
			{
				case NON_CLASSED:
					_div = 5;
					_gpreward = Config.ALT_OLY_NONCLASSED_RITEM_C;
					break;
				default:
					_div = 3;
					_gpreward = Config.ALT_OLY_CLASSED_RITEM_C;
					break;
			}
			
			int pointDiff;
			
			if ((_playerOne == null) && (_playerTwo == null))
			{
				result = " tie";
				_sm = new SystemMessage(SystemMessage.THE_GAME_ENDED_IN_A_TIE);
				broadcastMessage(_sm, true);
			}
			else if ((_playerTwo == null) || (_playerTwo.isOnline() == 0) || ((playerTwoHp == 0) && (playerOneHp != 0)) || ((_playerOne.dmgDealt > _playerTwo.dmgDealt) && (playerTwoHp != 0) && (playerOneHp != 0)))
			{
				
				pointDiff = playerTwoPoints / _div;
				playerOneStat.set(POINTS, playerOnePoints + pointDiff);
				playerOneStat.set(COMP_WON, playerOneWon + 1);
				playerTwoStat.set(POINTS, playerTwoPoints - pointDiff);
				playerTwoStat.set(COMP_LOST, playerTwoLost + 1);
				
				_sm.addString(_playerOneName);
				broadcastMessage(_sm, true);
				_sm2.addString(_playerOneName);
				_sm2.addNumber(pointDiff);
				broadcastMessage(_sm2, false);
				_sm3.addString(_playerTwoName);
				_sm3.addNumber(pointDiff);
				broadcastMessage(_sm3, false);
				
				try
				{
					result = " (" + playerOneHp + "hp vs " + playerTwoHp + "hp - " + _playerOne.dmgDealt + "dmg vs " + _playerTwo.dmgDealt + "dmg) " + _playerOneName + " win " + pointDiff + " points";
					final L2ItemInstance item = _playerOne.getInventory().addItem("Olympiad", Config.ALT_OLY_BATTLE_REWARD_ITEM, _gpreward, _playerOne, null);
					final InventoryUpdate iu = new InventoryUpdate();
					iu.addModifiedItem(item);
					_playerOne.sendPacket(iu);
					
					final SystemMessage sm = new SystemMessage(SystemMessage.EARNED_S2_S1_s);
					sm.addItemName(item.getItemId());
					sm.addNumber(_gpreward);
					_playerOne.sendPacket(sm);
				}
				catch (final Exception e)
				{
				}
			}
			else if ((_playerOne == null) || (_playerOne.isOnline() == 0) || ((playerOneHp == 0) && (playerTwoHp != 0)) || ((_playerTwo.dmgDealt > _playerOne.dmgDealt) && (playerOneHp != 0) && (playerTwoHp != 0)))
			{
				
				pointDiff = playerOnePoints / _div;
				playerTwoStat.set(POINTS, playerTwoPoints + pointDiff);
				playerTwoStat.set(COMP_WON, playerTwoWon + 1);
				
				playerOneStat.set(POINTS, playerOnePoints - pointDiff);
				
				playerOneStat.set(COMP_LOST, playerOneLost + 1);
				
				_sm.addString(_playerTwoName);
				broadcastMessage(_sm, true);
				_sm2.addString(_playerTwoName);
				_sm2.addNumber(pointDiff);
				broadcastMessage(_sm2, false);
				_sm3.addString(_playerOneName);
				_sm3.addNumber(pointDiff);
				broadcastMessage(_sm3, false);
				
				try
				{
					result = " (" + playerOneHp + "hp vs " + playerTwoHp + "hp - " + _playerOne.dmgDealt + "dmg vs " + _playerTwo.dmgDealt + "dmg) " + _playerTwoName + " win " + pointDiff + " points";
					final L2ItemInstance item = _playerTwo.getInventory().addItem("Olympiad", Config.ALT_OLY_BATTLE_REWARD_ITEM, _gpreward, _playerTwo, null);
					final InventoryUpdate iu = new InventoryUpdate();
					iu.addModifiedItem(item);
					_playerTwo.sendPacket(iu);
					
					final SystemMessage sm = new SystemMessage(SystemMessage.EARNED_S2_S1_s);
					sm.addItemName(item.getItemId());
					sm.addNumber(_gpreward);
					_playerTwo.sendPacket(sm);
				}
				catch (final Exception e)
				{
				}
			}
			
			else
			{
				result = " tie";
				_sm = new SystemMessage(SystemMessage.THE_GAME_ENDED_IN_A_TIE);
				
				broadcastMessage(_sm, true);
				final int pointOneDiff = playerOnePoints / _div;
				final int pointTwoDiff = playerTwoPoints / _div;
				
				playerOneStat.set(POINTS, playerOnePoints - pointOneDiff);
				playerOneStat.set(COMP_LOST, playerOneLost + 1);
				
				playerTwoStat.set(POINTS, playerTwoPoints - pointTwoDiff);
				playerTwoStat.set(COMP_LOST, playerTwoLost + 1);
				
				_sm2 = new SystemMessage(SystemMessage.S1_HAS_LOST_S2_OLYMPIAD_POINTS);
				_sm2.addString(_playerOneName);
				_sm2.addNumber(pointOneDiff);
				broadcastMessage(_sm2, false);
				_sm3 = new SystemMessage(SystemMessage.S1_HAS_LOST_S2_OLYMPIAD_POINTS);
				_sm3.addString(_playerTwoName);
				_sm3.addNumber(pointTwoDiff);
				broadcastMessage(_sm3, false);
			}
			
			if (Config.DEBUG)
			{
				_log.info("Olympia Result: " + _playerOneName + " vs " + _playerTwoName + " ... " + result);
			}
			
			playerOneStat.set(COMP_DONE, playerOnePlayed + 1);
			playerTwoStat.set(COMP_DONE, playerTwoPlayed + 1);
			
			_nobles.remove(_playerOneID);
			_nobles.remove(_playerTwoID);
			
			_nobles.put(_playerOneID, playerOneStat);
			_nobles.put(_playerTwoID, playerTwoStat);
			
			for (int i = 40; i > 10; i -= 10)
			{
				_sm = new SystemMessage(SystemMessage.YOU_WILL_GO_BACK_TO_THE_VILLAGE_IN_S1_SECOND_S);
				_sm.addNumber(i);
				broadcastMessage(_sm, true);
				try
				{
					Thread.sleep(10000);
				}
				catch (final InterruptedException e)
				{
				}
				
				if (i == 20)
				{
					_sm = new SystemMessage(SystemMessage.YOU_WILL_GO_BACK_TO_THE_VILLAGE_IN_S1_SECOND_S);
					_sm.addNumber(10);
					broadcastMessage(_sm, true);
					try
					{
						Thread.sleep(5000);
					}
					catch (final InterruptedException e)
					{
					}
				}
			}
			
			for (int i = 5; i > 0; i--)
			{
				_sm = new SystemMessage(SystemMessage.YOU_WILL_GO_BACK_TO_THE_VILLAGE_IN_S1_SECOND_S);
				_sm.addNumber(i);
				broadcastMessage(_sm, true);
				
				try
				{
					Thread.sleep(1000);
				}
				catch (final InterruptedException e)
				{
				}
			}
		}
		
		protected void additions()
		{
			for (final L2PcInstance player : _players)
			{
				try
				{
					// Wind Walk Buff for Both
					L2Skill skill;
					SystemMessage sm;
					
					skill = SkillTable.getInstance().getInfo(1204, 2);
					skill.getEffects(player, player);
					
					sm = new SystemMessage(SystemMessage.YOU_FEEL_S1_EFFECT);
					sm.addSkillName(skill.getId());
					player.sendPacket(sm);
					
					if (player.isMageClass())
					{
						// Acumen Buff to Mages
						skill = SkillTable.getInstance().getInfo(1085, 1);
						skill.getEffects(player, player);
						
						sm = new SystemMessage(SystemMessage.YOU_FEEL_S1_EFFECT);
						sm.addSkillName(skill.getId());
						
					}
					else
					{
						// Haste Buff to Fighters
						skill = SkillTable.getInstance().getInfo(1086, 1);
						skill.getEffects(player, player);
						
						sm = new SystemMessage(SystemMessage.YOU_FEEL_S1_EFFECT);
						sm.addSkillName(skill.getId());
					}
					player.sendPacket(sm);
					
				}
				catch (final Exception e)
				{
				}
				finally
				{
					player.dmgDealt = 0;
				}
			}
		}
		
		protected boolean makeCompetitionStart()
		{
			if (_aborted)
			{
				return false;
			}
			
			_sm = new SystemMessage(SystemMessage.STARTS_THE_GAME);
			broadcastMessage(_sm, true);
			
			try
			{
				for (final L2PcInstance player : _players)
				{
					if (player == null)
					{
						continue;
					}
					
					player.setIsOlympiadStart(true);
					sendUserInfo(player);
					player.updateEffectIcons(true);
				}
			}
			catch (final Exception e)
			{
				_aborted = true;
				return false;
			}
			return true;
		}
		
		protected String getTitle()
		{
			String msg = "";
			msg += _playerOneName + "  /  " + _playerTwoName;
			return msg;
		}
		
		protected L2PcInstance[] getPlayers()
		{
			if ((_playerOne == null) || (_playerTwo == null))
			{
				return null;
			}
			
			final L2PcInstance[] players = new L2PcInstance[2];
			
			players[0] = _playerOne;
			players[1] = _playerTwo;
			
			return players;
		}
		
		protected L2FastList<L2PcInstance> getSpectators()
		{
			return _spectators;
		}
		
		protected void addSpectator(L2PcInstance spec)
		{
			if (!_spectators.contains(spec))
			{
				_spectators.add(spec);
			}
		}
		
		protected void removeSpectator(L2PcInstance spec)
		{
			if ((_spectators != null) && _spectators.contains(spec))
			{
				_spectators.remove(spec);
			}
		}
		
		protected void clearSpectators()
		{
			if (_spectators != null)
			{
				for (final L2PcInstance pc : _spectators)
				{
					if (pc == null)
					{
						continue;
					}
					
					getInstance().removeSpectator(pc.getOlympiadGameId(), pc);
				}
				_spectators.clear();
				
			}
		}
	}
	
	public static void sendUserInfo(L2PcInstance player)
	{
		if ((_manager == null) || (_manager.getOlympiadGames() == null))
		{
			return;
		}
		
		for (final L2OlympiadGame game : _manager.getOlympiadGames().values())
		{
			if (game == null)
			{
				continue;
			}
			
			if (player == game._playerOne)
			{
				game._playerTwo.sendPacket(new ExOlympiadUserInfo(player, 1));
			}
			if (player == game._playerTwo)
			{
				game._playerOne.sendPacket(new ExOlympiadUserInfo(player, 1));
			}
			
			if (game.getSpectators() != null)
			{
				for (final L2PcInstance spectator : game.getSpectators())
				{
					if (spectator == null)
					{
						continue;
					}
					
					spectator.sendPacket(new ExOlympiadUserInfo(player, player.getOlympiadSide()));
				}
			}
		}
	}
	
	public static void clearOfflineObservers(L2PcInstance player)
	{
		if ((_manager == null) || (_manager.getOlympiadGames() == null))
		{
			return;
		}
		
		for (final L2OlympiadGame game : _manager.getOlympiadGames().values())
		{
			if (game == null)
			{
				continue;
			}
			
			if ((game.getSpectators() != null) && game.getSpectators().contains(player))
			{
				game.getSpectators().remove(player);
			}
		}
	}
	
	public void sendMatchList(L2PcInstance player)
	{
		final NpcHtmlMessage reply = new NpcHtmlMessage(0);
		final TextBuilder replyMSG = new TextBuilder("<html><body>");
		
		final String[] matches = Olympiad.getInstance().getMatchList();
		
		int stad;
		int showbattle;
		replyMSG.append("Grand Olympiad Games Overview<br><br>" + "* Note: Keep in mind that once you click Return button, " + "you will leave Olympiad observer mode, " + "and be teleported back to town.<br>");
		
		if (matches != null)
		{
			for (int i = 0; i < matches.length; i++)
			{
				if ((_manager != null) && (_manager.getSpectatedGame(player) != i))
				{
					showbattle = Integer.parseInt(matches[i].substring(1, 2));
					stad = Integer.parseInt(matches[i].substring(4, 5));
					if (showbattle == 1)
					{
						replyMSG.append("<br><a action=\"bypass -h OlympiadArenaChange " + stad + "\">" + matches[i] + "</a>");
					}
					
				}
				else
				{
					replyMSG.append("<br>" + matches[i] + "");
				}
			}
		}
		else
		{
			replyMSG.append("<br>There are no matches at the moment.");
		}
		
		replyMSG.append("</body></html>");
		
		reply.setHtml(replyMSG.toString());
		player.sendPacket(reply);
	}
	
	public void bypassChangeArena(String command, L2PcInstance player)
	{
		if (!player.inObserverMode())
		{
			return;
		}
		
		final String[] commands = command.split(" ");
		final int id = Integer.parseInt(commands[1]);
		
		final int arena = _manager != null ? _manager.getSpectatedGame(player) : -1;
		if (arena < 0)
		{
			return;
		}
		
		getInstance().removeSpectator(arena, player);
		getInstance().addSpectator(id, player, false);
	}
}