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
package com.l2jmobius.gameserver.model.quest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.L2DatabaseFactory;
import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.cache.HtmCache;
import com.l2jmobius.gameserver.datatables.NpcTable;
import com.l2jmobius.gameserver.instancemanager.QuestManager;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Party;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.L2Spawn;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.scripting.ManagedScript;
import com.l2jmobius.gameserver.scripting.ScriptManager;
import com.l2jmobius.gameserver.templates.L2NpcTemplate;
import com.l2jmobius.util.Rnd;

import javolution.util.FastList;
import javolution.util.FastMap;

/**
 * @author Luis Arias
 */
public class Quest extends ManagedScript
{
	protected static Logger _log = Logger.getLogger(Quest.class.getName());
	
	/** HashMap containing events from String value of the event */
	private static Map<String, Quest> allEventsS = new FastMap<>();
	/** HashMap containing lists of timers from the name of the timer */
	private static Map<String, FastList<QuestTimer>> allEventTimers = new FastMap<>();
	
	private final ReentrantReadWriteLock _rwLock = new ReentrantReadWriteLock();
	
	private final int _questId;
	private final String _name;
	private final String _descr;
	private State initialState;
	private final Map<String, State> states;
	
	// NOTE: questItemIds will be overridden by child classes. Ideally, it should be
	// protected instead of public. However, quest scripts written in Jython will
	// have trouble with protected, as Jython only knows private and public...
	// In fact, protected will typically be considered private thus breaking the scripts.
	// Leave this as public as a workaround.
	public int[] questItemIds = null;
	
	public static enum QuestEventType
	{
		ON_FIRST_TALK(false),
		QUEST_START(true),
		ON_TALK(true),
		ON_ATTACK(true),
		ON_KILL(true),
		ON_SPAWN(true),
		ON_SKILL_SEE(true),
		ON_AGGRO_RANGE_ENTER(true),
		ON_FACTION_CALL(true),
		ON_SPELL_FINISHED(true),
		ON_CREATURE_SEE(true);
		
		// control whether this event type is allowed for the same npc template in multiple quests
		// or if the npc must be registered in at most one quest for the specified event
		private boolean _allowMultipleRegistration;
		
		QuestEventType(boolean allowMultipleRegistration)
		{
			_allowMultipleRegistration = allowMultipleRegistration;
		}
		
		public boolean isMultipleRegistrationAllowed()
		{
			return _allowMultipleRegistration;
		}
	}
	
	/**
	 * Return collection view of the values contains in the allEventS
	 * @return Collection<Quest>
	 */
	public static Collection<Quest> findAllEvents()
	{
		return allEventsS.values();
	}
	
	/**
	 * (Constructor)Add values to class variables and put the quest in HashMaps.
	 * @param questId : int pointing out the ID of the quest
	 * @param name : String corresponding to the name of the quest
	 * @param descr : String for the description of the quest
	 */
	public Quest(int questId, String name, String descr)
	{
		
		_questId = questId;
		_name = name;
		_descr = descr;
		
		states = new FastMap<>();
		
		if (questId != 0)
		{
			QuestManager.getInstance().addQuest(Quest.this);
		}
		else
		{
			allEventsS.put(name, this);
		}
		
		init_LoadGlobalData();
	}
	
	/**
	 * The function init_LoadGlobalData is, by default, called by the constructor of all quests. Children of this class can implement this function in order to define what variables to load and what structures to save them in. By default, nothing is loaded.
	 */
	protected void init_LoadGlobalData()
	{
	}
	
	/**
	 * The function saveGlobalData is, by default, called at shutdown, for all quests, by the QuestManager. Children of this class can implement this function in order to convert their structures into <var, value> tuples and make calls to save them to the database, if needed. By default, nothing is
	 * saved.
	 */
	public void saveGlobalData()
	{
	}
	
	/**
	 * Return ID of the quest
	 * @return int
	 */
	public int getQuestIntId()
	{
		return _questId;
	}
	
	/**
	 * Set the initial state of the quest with parameter "state"
	 * @param state
	 */
	public void setInitialState(State state)
	{
		initialState = state;
	}
	
	/**
	 * Add a new QuestState to the database and return it.
	 * @param player
	 * @return QuestState : QuestState created
	 */
	public QuestState newQuestState(L2PcInstance player)
	{
		final QuestState qs = new QuestState(this, player, getInitialState());
		Quest.createQuestInDb(qs);
		return qs;
	}
	
	/**
	 * Return initial state of the quest
	 * @return State
	 */
	public State getInitialState()
	{
		return initialState;
	}
	
	/**
	 * Return name of the quest
	 * @return String
	 */
	public String getName()
	{
		return _name;
	}
	
	/**
	 * Return description of the quest
	 * @return String
	 */
	public String getDescr()
	{
		return _descr;
	}
	
	/**
	 * Add a state to the quest
	 * @param state
	 * @return state added
	 */
	public State addState(State state)
	{
		states.put(state.getName(), state);
		return state;
	}
	
	/**
	 * Add a timer to the quest, if it doesn't exist already
	 * @param name of the timer (also passed back as "event" in onAdvEvent)
	 * @param time in ms for when to fire the timer
	 * @param npc associated with this timer (can be null)
	 * @param player associated with this timer (can be null)
	 */
	public void startQuestTimer(String name, long time, L2NpcInstance npc, L2PcInstance player)
	{
		startQuestTimer(name, time, npc, player, false);
	}
	
	/**
	 * Add a timer to the quest, if it doesn't exist already. If the timer is repeatable, it will auto-fire automatically, at a fixed rate, until explicitly cancelled.
	 * @param name of the timer (also passed back as "event" in onAdvEvent)
	 * @param time in ms for when to fire the timer
	 * @param npc associated with this timer (can be null)
	 * @param player associated with this timer (can be null)
	 * @param repeating indicates if the timer is repeatable or one-time.
	 */
	public void startQuestTimer(String name, long time, L2NpcInstance npc, L2PcInstance player, boolean repeating)
	{
		// Add quest timer if timer doesn't already exist
		FastList<QuestTimer> timers = getQuestTimers(name);
		// no timer exists with the same name, at all
		if (timers == null)
		{
			timers = new FastList<>();
			timers.add(new QuestTimer(this, name, time, npc, player, repeating));
			allEventTimers.put(name, timers);
		}
		else
		{
			// if there exists a timer with this name, allow the timer only if the [npc, player] set is unique
			// nulls act as wildcards
			if (getQuestTimer(name, npc, player) == null)
			{
				try
				{
					_rwLock.writeLock().lock();
					timers.add(new QuestTimer(this, name, time, npc, player, repeating));
				}
				finally
				{
					_rwLock.writeLock().unlock();
				}
			}
		}
	}
	
	public QuestTimer getQuestTimer(String name, L2NpcInstance npc, L2PcInstance player)
	{
		final FastList<QuestTimer> qt = getQuestTimers(name);
		if ((qt == null) || qt.isEmpty())
		{
			return null;
		}
		
		try
		{
			_rwLock.readLock().lock();
			for (final QuestTimer timer : qt)
			{
				if (timer.isMatch(this, name, npc, player))
				{
					return timer;
				}
			}
		}
		finally
		{
			_rwLock.readLock().unlock();
		}
		return null;
	}
	
	public FastList<QuestTimer> getQuestTimers(String name)
	{
		return allEventTimers.get(name);
	}
	
	public void cancelQuestTimers(String name)
	{
		final FastList<QuestTimer> timers = getQuestTimers(name);
		if (timers == null)
		{
			return;
		}
		
		try
		{
			_rwLock.writeLock().lock();
			for (final QuestTimer timer : timers)
			{
				if (timer != null)
				{
					timer.cancel();
				}
			}
		}
		finally
		{
			_rwLock.writeLock().unlock();
		}
	}
	
	public void cancelQuestTimer(String name, L2NpcInstance npc, L2PcInstance player)
	{
		final QuestTimer timer = getQuestTimer(name, npc, player);
		if (timer != null)
		{
			timer.cancel();
		}
	}
	
	public void removeQuestTimer(QuestTimer timer)
	{
		if (timer == null)
		{
			return;
		}
		final FastList<QuestTimer> timers = getQuestTimers(timer.getName());
		if (timers == null)
		{
			return;
		}
		
		try
		{
			_rwLock.writeLock().lock();
			timers.remove(timer);
		}
		finally
		{
			_rwLock.writeLock().unlock();
		}
	}
	
	// these are methods to call from java
	public final boolean notifyAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		String res = null;
		try
		{
			res = onAttack(npc, attacker, damage, isPet);
		}
		catch (final Exception e)
		{
			return showError(attacker, e);
		}
		return showResult(attacker, res);
	}
	
	public final boolean notifyDeath(L2Character killer, L2Character victim, QuestState qs)
	{
		String res = null;
		try
		{
			res = onDeath(killer, victim, qs);
		}
		catch (final Exception e)
		{
			return showError(qs.getPlayer(), e);
		}
		return showResult(qs.getPlayer(), res);
	}
	
	public final boolean notifySpellFinished(L2NpcInstance npc, L2PcInstance player, L2Skill skill)
	{
		String res = null;
		try
		{
			res = onSpellFinished(npc, player, skill);
		}
		catch (final Exception e)
		{
			return showError(player, e);
		}
		return showResult(player, res);
	}
	
	public final boolean notifyEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onAdvEvent(event, npc, player);
		}
		catch (final Exception e)
		{
			return showError(player, e);
		}
		return showResult(player, res);
	}
	
	public final boolean notifyKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet)
	{
		String res = null;
		try
		{
			res = onKill(npc, killer, isPet);
		}
		catch (final Exception e)
		{
			return showError(killer, e);
		}
		
		return showResult(killer, res);
	}
	
	public final boolean notifyTalk(L2NpcInstance npc, QuestState qs)
	{
		String res = null;
		try
		{
			res = onTalk(npc, qs);
		}
		catch (final Exception e)
		{
			return showError(qs.getPlayer(), e);
		}
		
		qs.getPlayer().setLastQuestNpcObject(npc.getObjectId());
		return showResult(qs.getPlayer(), res);
	}
	
	// override the default NPC dialogs when a quest defines this for the given NPC
	public final boolean notifyFirstTalk(L2NpcInstance npc, L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onFirstTalk(npc, player);
		}
		catch (final Exception e)
		{
			return showError(player, e);
		}
		
		player.setLastQuestNpcObject(npc.getObjectId());
		
		// if the quest returns text to display, display it. Otherwise, use the default npc text.
		if ((res != null) && (res.length() > 0))
		{
			return showResult(player, res);
		}
		
		// note: if the default html for this npc needs to be shown, onFirstTalk should
		// call npc.showChatWindow(player) and then return null.
		return true;
	}
	
	public final boolean notifyAggroRangeEnter(L2NpcInstance npc, L2PcInstance player, boolean isPet)
	{
		ThreadPoolManager.getInstance().executeAi(() ->
		{
			String res = null;
			try
			{
				res = onAggroRangeEnter(npc, player, isPet);
			}
			catch (final Exception e)
			{
				showError(player, e);
			}
			showResult(player, res);
		});
		return true;
	}
	
	public final boolean notifySkillSee(L2NpcInstance npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		ThreadPoolManager.getInstance().executeAi(() ->
		{
			String res = null;
			try
			{
				res = onSkillSee(npc, caster, skill, targets, isPet);
			}
			catch (final Exception e)
			{
				showError(caster, e);
			}
			showResult(caster, res);
		});
		return true;
	}
	
	public final boolean notifyFactionCall(L2NpcInstance npc, L2NpcInstance caller, L2PcInstance attacker, boolean isPet)
	{
		String res = null;
		try
		{
			res = onFactionCall(npc, caller, attacker, isPet);
		}
		catch (final Exception e)
		{
			return showError(attacker, e);
		}
		return showResult(attacker, res);
	}
	
	public final boolean notifySpawn(L2NpcInstance npc)
	{
		try
		{
			onSpawn(npc);
		}
		catch (final Exception e)
		{
			_log.log(Level.WARNING, "Exception on onSpawn() in notifySpawn(): " + e.getMessage(), e);
			return true;
		}
		return false;
	}
	
	public final boolean notifyCreatureSee(L2NpcInstance npc, L2PcInstance player, boolean isPet)
	{
		ThreadPoolManager.getInstance().executeAi(() ->
		{
			String res = null;
			try
			{
				res = onCreatureSee(npc, player, isPet);
			}
			catch (final Exception e)
			{
				showError(player, e);
			}
			showResult(player, res);
		});
		return true;
	}
	
	// these are methods that java calls to invoke scripts
	public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		return null;
	}
	
	public String onDeath(L2Character killer, L2Character victim, QuestState qs)
	{
		if (killer instanceof L2NpcInstance)
		{
			return onAdvEvent("", (L2NpcInstance) killer, qs.getPlayer());
		}
		return onAdvEvent("", null, qs.getPlayer());
	}
	
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		if (player != null)
		{
			// if not overriden by a subclass, then default to the returned value of the simpler (and older) onEvent override
			// if the player has a state, use it as parameter in the next call, else return null
			final QuestState qs = player.getQuestState(getName());
			if (qs != null)
			{
				return onEvent(event, qs);
			}
		}
		return null;
	}
	
	public String onEvent(String event, QuestState qs)
	{
		return null;
	}
	
	public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet)
	{
		return null;
	}
	
	public String onTalk(L2NpcInstance npc, QuestState qs)
	{
		return null;
	}
	
	public String onFirstTalk(L2NpcInstance npc, L2PcInstance player)
	{
		return null;
	}
	
	public String onAggroRangeEnter(L2NpcInstance npc, L2PcInstance player, boolean isPet)
	{
		return null;
	}
	
	public String onSpellFinished(L2NpcInstance npc, L2PcInstance player, L2Skill skill)
	{
		return null;
	}
	
	public String onSkillSee(L2NpcInstance npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		return null;
	}
	
	public String onFactionCall(L2NpcInstance npc, L2NpcInstance caller, L2PcInstance attacker, boolean isPet)
	{
		return null;
	}
	
	public String onSpawn(L2NpcInstance npc)
	{
		return null;
	}
	
	public String onCreatureSee(L2NpcInstance npc, L2PcInstance player, boolean isPet)
	{
		return null;
	}
	
	/**
	 * Add this quest to the list of quests that the passed mob will respond to for the specified Event type.<BR>
	 * <BR>
	 * @param npcId : id of the NPC to register
	 * @param eventType : type of event being registered
	 * @return L2NpcTemplate : Npc Template corresponding to the npcId, or null if the id is invalid
	 */
	public L2NpcTemplate addEventId(int npcId, QuestEventType eventType)
	{
		try
		{
			final L2NpcTemplate t = NpcTable.getInstance().getTemplate(npcId);
			if (t != null)
			{
				t.addQuestEvent(eventType, this);
			}
			
			return t;
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public L2NpcTemplate addStartNpc(int npcId)
	{
		return addEventId(npcId, Quest.QuestEventType.QUEST_START);
	}
	
	/**
	 * Show message error to player who has an access level greater than 0
	 * @param player : player
	 * @param t : Throwable
	 * @return boolean
	 */
	private boolean showError(L2PcInstance player, Throwable t)
	{
		_log.log(Level.WARNING, getScriptFile().getAbsolutePath(), t);
		if (player.getAccessLevel() > 0)
		{
			String res = "";
			try (StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw))
			{
				t.printStackTrace(pw);
				res = "<html><body><title>Script error</title>" + sw.toString() + "</body></html>";
			}
			catch (final Exception e)
			{
				return false;
			}
			return showResult(player, res);
		}
		return false;
	}
	
	/**
	 * Show a message to player.<BR>
	 * <BR>
	 * <U><I>Concept : </I></U><BR>
	 * 3 cases are managed according to the value of the parameter "res" :<BR>
	 * <LI><U>"res" ends with string ".html" :</U> an HTML is opened in order to be shown in a dialog box</LI>
	 * <LI><U>"res" starts with "<html>" :</U> the message hold in "res" is shown in a dialog box</LI>
	 * <LI><U>otherwise :</U> the message hold in "res" is shown in chat box</LI>
	 * @param player : player
	 * @param res : String pointing out the message to show at the player
	 * @return boolean
	 */
	private boolean showResult(L2PcInstance player, String res)
	{
		if ((res == null) || res.isEmpty())
		{
			return true;
		}
		
		if (res.endsWith(".htm"))
		{
			showHtmlFile(player, res);
		}
		else if (res.startsWith("<html>"))
		{
			final NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
			npcReply.setHtml(res);
			npcReply.replace("%playername%", player.getName());
			player.sendPacket(npcReply);
			player.sendPacket(new ActionFailed());
		}
		else
		{
			final SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
			sm.addString(res);
			player.sendPacket(sm);
		}
		return false;
	}
	
	/**
	 * Add quests to the L2PCInstance of the player.<BR>
	 * <BR>
	 * <U><I>Action : </U></I><BR>
	 * Add state of quests, and variables for quests in the HashMap _quest of L2PcInstance
	 * @param player : Player who is entering the world
	 */
	public final static void playerEnter(L2PcInstance player)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			// Get list of quests owned by the player from database
			try (PreparedStatement statement = con.prepareStatement("SELECT name,value FROM character_quests WHERE char_id=? AND var=?");
				PreparedStatement invalidQuestData = con.prepareStatement("DELETE FROM character_quests WHERE char_id=? and name=?"))
			{
				statement.setInt(1, player.getObjectId());
				statement.setString(2, "<state>");
				try (ResultSet rs = statement.executeQuery())
				{
					while (rs.next())
					{
						// Get ID of the quest and ID of its state
						final String questId = rs.getString("name");
						final String stateId = rs.getString("value");
						
						// Search quest associated with the ID
						final Quest q = QuestManager.getInstance().getQuest(questId);
						if (q == null)
						{
							_log.finer("Unknown quest " + questId + " for player " + player.getName());
							if (Config.AUTODELETE_INVALID_QUEST_DATA)
							{
								invalidQuestData.setInt(1, player.getObjectId());
								invalidQuestData.setString(2, questId);
								invalidQuestData.executeUpdate();
							}
							continue;
						}
						
						// Create an object State containing the state of the quest
						final State state = q.states.get(stateId);
						if (state == null)
						{
							_log.finer("Unknown state in quest " + questId + " for player " + player.getName());
							if (Config.AUTODELETE_INVALID_QUEST_DATA)
							{
								invalidQuestData.setInt(1, player.getObjectId());
								invalidQuestData.setString(2, questId);
								invalidQuestData.executeUpdate();
							}
							continue;
						}
						
						// Create a new QuestState for the player that will be added to the player's list of quests
						new QuestState(q, player, state);
					}
				}
			}
			
			// Get list of quests owned by the player from the DB in order to add variables used in the quest.
			try (PreparedStatement statement = con.prepareStatement("SELECT name,var,value FROM character_quests WHERE char_id=? AND var<>?");
				PreparedStatement invalidQuestDataVar = con.prepareStatement("delete FROM character_quests WHERE char_id=? and name=? and var=?"))
			{
				statement.setInt(1, player.getObjectId());
				statement.setString(2, "<state>");
				try (ResultSet rs = statement.executeQuery())
				{
					while (rs.next())
					{
						final String questId = rs.getString("name");
						final String var = rs.getString("var");
						final String value = rs.getString("value");
						// Get the QuestState saved in the loop before
						final QuestState qs = player.getQuestState(questId);
						if (qs == null)
						{
							_log.finer("Lost variable " + var + " in quest " + questId + " for player " + player.getName());
							if (Config.AUTODELETE_INVALID_QUEST_DATA)
							{
								invalidQuestDataVar.setInt(1, player.getObjectId());
								invalidQuestDataVar.setString(2, questId);
								invalidQuestDataVar.setString(3, var);
								invalidQuestDataVar.executeUpdate();
							}
							continue;
						}
						
						// Add parameter to the quest
						qs.setInternal(var, value);
					}
				}
			}
		}
		catch (final Exception e)
		{
			_log.log(Level.WARNING, "could not insert char quest:", e);
		}
		
		// events
		for (final String name : allEventsS.keySet())
		{
			player.processQuestEvent(name, "enter");
		}
	}
	
	/**
	 * Insert (or Update) in the database variables that need to stay persistant for this quest after a reboot. This function is for storage of values that do not related to a specific player but are global for all characters. For example, if we need to disable a quest-gatekeeper until a certain
	 * time (as is done with some grand-boss gatekeepers), we can save that time in the DB.
	 * @param var : String designating the name of the variable for the quest
	 * @param value : String designating the value of the variable for the quest
	 */
	public final void saveGlobalQuestVar(String var, String value)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("REPLACE INTO quest_global_data (quest_name,var,value) VALUES (?,?,?)"))
		{
			statement.setString(1, getName());
			statement.setString(2, var);
			statement.setString(3, value);
			statement.executeUpdate();
		}
		catch (final Exception e)
		{
			_log.log(Level.WARNING, "could not insert global quest variable:", e);
		}
	}
	
	/**
	 * Read from the database a previously saved variable for this quest. Due to performance considerations, this function should best be used only when the quest is first loaded. Subclasses of this class can define structures into which these loaded values can be saved. However, on-demand usage of
	 * this function throughout the script is not prohibited, only not recommended. Values read from this function were entered by calls to "saveGlobalQuestVar"
	 * @param var : String designating the name of the variable for the quest
	 * @return String : String representing the loaded value for the passed var, or an empty string if the var was invalid
	 */
	public final String loadGlobalQuestVar(String var)
	{
		String result = "";
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT value FROM quest_global_data WHERE quest_name = ? AND var = ?"))
		{
			statement.setString(1, getName());
			statement.setString(2, var);
			try (ResultSet rs = statement.executeQuery())
			{
				if (rs.first())
				{
					result = rs.getString(1);
				}
			}
		}
		catch (final Exception e)
		{
			_log.log(Level.WARNING, "could not load global quest variable:", e);
		}
		return result;
	}
	
	/**
	 * Permanently delete from the database a global quest variable that was previously saved for this quest.
	 * @param var : String designating the name of the variable for the quest
	 */
	public final void deleteGlobalQuestVar(String var)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM quest_global_data WHERE quest_name = ? AND var = ?"))
		{
			statement.setString(1, getName());
			statement.setString(2, var);
			statement.executeUpdate();
		}
		catch (final Exception e)
		{
			_log.log(Level.WARNING, "could not delete global quest variable:", e);
		}
	}
	
	/**
	 * Permanently delete from the database all global quest variables that was previously saved for this quest.
	 */
	public final void deleteAllGlobalQuestVars()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM quest_global_data WHERE quest_name = ?"))
		{
			statement.setString(1, getName());
			statement.executeUpdate();
		}
		catch (final Exception e)
		{
			_log.log(Level.WARNING, "could not delete global quest variables:", e);
		}
	}
	
	/**
	 * Insert in the database the quest for the player.
	 * @param qs : QuestState pointing out the state of the quest
	 * @param var : String designating the name of the variable for the quest
	 * @param value : String designating the value of the variable for the quest
	 */
	public static void createQuestVarInDb(QuestState qs, String var, String value)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("REPLACE INTO character_quests (char_id,name,var,value) VALUES (?,?,?,?)"))
		{
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setString(2, qs.getQuestName());
			statement.setString(3, var);
			statement.setString(4, value);
			statement.executeUpdate();
		}
		catch (final Exception e)
		{
			_log.log(Level.WARNING, "could not insert char quest:", e);
		}
	}
	
	/**
	 * Update the value of the variable "var" for the quest.<BR>
	 * <BR>
	 * <U><I>Actions :</I></U><BR>
	 * The selection of the right record is made with :
	 * <LI>char_id = qs.getPlayer().getObjectID()</LI>
	 * <LI>name = qs.getQuestName()</LI>
	 * <LI>var = var</LI> <BR>
	 * <BR>
	 * The modification made is :
	 * <LI>value = parameter value</LI>
	 * @param qs : Quest State
	 * @param var : String designating the name of the variable for quest
	 * @param value : String designating the value of the variable for quest
	 */
	public static void updateQuestVarInDb(QuestState qs, String var, String value)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE character_quests SET value=? WHERE char_id=? AND name=? AND var = ?"))
		{
			statement.setString(1, value);
			statement.setInt(2, qs.getPlayer().getObjectId());
			statement.setString(3, qs.getQuestName());
			statement.setString(4, var);
			statement.executeUpdate();
		}
		catch (final Exception e)
		{
			_log.log(Level.WARNING, "could not update char quest:", e);
		}
	}
	
	/**
	 * Delete a variable of player's quest from the database.
	 * @param qs : object QuestState pointing out the player's quest
	 * @param var : String designating the variable characterizing the quest
	 */
	public static void deleteQuestVarInDb(QuestState qs, String var)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM character_quests WHERE char_id=? AND name=? AND var=?"))
		{
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setString(2, qs.getQuestName());
			statement.setString(3, var);
			statement.executeUpdate();
		}
		catch (final Exception e)
		{
			_log.log(Level.WARNING, "could not delete char quest:", e);
		}
	}
	
	/**
	 * Delete the player's quest from database.
	 * @param qs : QuestState pointing out the player's quest
	 */
	public static void deleteQuestInDb(QuestState qs)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM character_quests WHERE char_id=? AND name=?"))
		{
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setString(2, qs.getQuestName());
			statement.executeUpdate();
		}
		catch (final Exception e)
		{
			_log.log(Level.WARNING, "could not delete char quest:", e);
		}
	}
	
	/**
	 * Create a record in database for quest.<BR>
	 * <BR>
	 * <U><I>Actions :</I></U><BR>
	 * Use fucntion createQuestVarInDb() with following parameters :<BR>
	 * <LI>QuestState : parameter sq that puts in fields of database :
	 * <UL type="square">
	 * <LI>char_id : ID of the player</LI>
	 * <LI>name : name of the quest</LI>
	 * </UL>
	 * </LI>
	 * <LI>var : string "&lt;state&gt;" as the name of the variable for the quest</LI>
	 * <LI>val : string corresponding at the ID of the state (in fact, initial state)</LI>
	 * @param qs : QuestState
	 */
	public static void createQuestInDb(QuestState qs)
	{
		createQuestVarInDb(qs, "<state>", qs.getStateId());
	}
	
	/**
	 * Update informations regarding quest in database.<BR>
	 * <U><I>Actions :</I></U><BR>
	 * <LI>Get ID state of the quest recorded in object qs</LI>
	 * <LI>Test if quest is completed. If true, add a star (*) before the ID state</LI>
	 * <LI>Save in database the ID state (with or without the star) for the variable called "&lt;state&gt;" of the quest</LI>
	 * @param qs : QuestState
	 */
	public static void updateQuestInDb(QuestState qs)
	{
		updateQuestVarInDb(qs, "<state>", qs.getStateId());
	}
	
	/**
	 * Add this quest to the list of quests that the passed mob will respond to for Attack Events.<BR>
	 * <BR>
	 * @param attackId
	 * @return int : attackId
	 */
	public L2NpcTemplate addAttackId(int attackId)
	{
		return addEventId(attackId, Quest.QuestEventType.ON_ATTACK);
	}
	
	/**
	 * Add this quest to the list of quests that the passed mob will respond to for Kill Events.<BR>
	 * <BR>
	 * @param killId
	 * @return int : killId
	 */
	public L2NpcTemplate addKillId(int killId)
	{
		return addEventId(killId, Quest.QuestEventType.ON_KILL);
	}
	
	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Talk Events.<BR>
	 * <BR>
	 * @param talkId : ID of the NPC
	 * @return int : ID of the NPC
	 */
	public L2NpcTemplate addTalkId(int talkId)
	{
		return addEventId(talkId, Quest.QuestEventType.ON_TALK);
	}
	
	/**
	 * Add the quest to the NPC's first-talk (default action dialog)
	 * @param npcId
	 * @return L2NpcTemplate : Start NPC
	 */
	public L2NpcTemplate addFirstTalkId(int npcId)
	{
		return addEventId(npcId, Quest.QuestEventType.ON_FIRST_TALK);
	}
	
	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Character See Events.<BR>
	 * <BR>
	 * @param npcId : ID of the NPC
	 * @return int : ID of the NPC
	 */
	public L2NpcTemplate addAggroRangeEnterId(int npcId)
	{
		return addEventId(npcId, Quest.QuestEventType.ON_AGGRO_RANGE_ENTER);
	}
	
	public L2NpcTemplate addSpellFinishedId(int npcId)
	{
		return addEventId(npcId, Quest.QuestEventType.ON_SPELL_FINISHED);
	}
	
	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Skill-See Events.<BR>
	 * <BR>
	 * @param npcId : ID of the NPC
	 * @return int : ID of the NPC
	 */
	public L2NpcTemplate addSkillSeeId(int npcId)
	{
		return addEventId(npcId, Quest.QuestEventType.ON_SKILL_SEE);
	}
	
	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Faction Call Events.<BR>
	 * <BR>
	 * @param npcId : ID of the NPC
	 * @return int : ID of the NPC
	 */
	public L2NpcTemplate addFactionCallId(int npcId)
	{
		return addEventId(npcId, Quest.QuestEventType.ON_FACTION_CALL);
	}
	
	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Spawn Events.<BR>
	 * <BR>
	 * @param npcId : ID of the NPC
	 * @return int : ID of the NPC
	 */
	public L2NpcTemplate addSpawnId(int npcId)
	{
		return addEventId(npcId, Quest.QuestEventType.ON_SPAWN);
	}
	
	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Creature-See Events.<BR>
	 * <BR>
	 * @param npcId : ID of the NPC
	 * @return int : ID of the NPC
	 */
	public L2NpcTemplate addCreatureSeeId(int npcId)
	{
		return addEventId(npcId, Quest.QuestEventType.ON_CREATURE_SEE);
	}
	
	// returns a random party member's L2PcInstance for the passed player's party
	// returns the passed player if he has no party.
	public L2PcInstance getRandomPartyMember(L2PcInstance player)
	{
		// NPE prevention. If the player is null, there is nothing to return
		if (player == null)
		{
			return null;
		}
		if ((player.getParty() == null) || (player.getParty().getPartyMembers().size() == 0))
		{
			return player;
		}
		final L2Party party = player.getParty();
		return party.getPartyMembers().get(Rnd.get(party.getPartyMembers().size()));
	}
	
	/**
	 * Auxilary function for party quests. Note: This function is only here because of how commonly it may be used by quest developers. For any variations on this function, the quest script can always handle things on its own
	 * @param player : the instance of a player whose party is to be searched
	 * @param value : the value of the "cond" variable that must be matched
	 * @return L2PcInstance: L2PcInstance for a random party member that matches the specified condition, or null if no match.
	 */
	public L2PcInstance getRandomPartyMember(L2PcInstance player, String value)
	{
		return getRandomPartyMember(player, "cond", value);
	}
	
	/**
	 * Auxilary function for party quests. Note: This function is only here because of how commonly it may be used by quest developers. For any variations on this function, the quest script can always handle things on its own
	 * @param player : the instance of a player whose party is to be searched
	 * @param var : a tuple specifying a quest condition that must be satisfied for a party member to be considered.
	 * @param value
	 * @return L2PcInstance: L2PcInstance for a random party member that matches the specified condition, or null if no match. If the var is null, any random party member is returned (i.e. no condition is applied).
	 */
	public L2PcInstance getRandomPartyMember(L2PcInstance player, String var, String value)
	{
		// if no valid player instance is passed, there is nothing to check...
		if (player == null)
		{
			return null;
		}
		
		// for null var condition, return any random party member.
		if (var == null)
		{
			return getRandomPartyMember(player);
		}
		
		// normal cases...if the player is not in a partym check the player's state
		QuestState temp = null;
		final L2Party party = player.getParty();
		// if this player is not in a party, just check if this player instance matches the conditions itself
		if ((party == null) || (party.getPartyMembers().size() == 0))
		{
			temp = player.getQuestState(getName());
			if ((temp != null) && (temp.get(var) != null) && temp.get(var).equalsIgnoreCase(value))
			{
				return player; // match
			}
			return null;
		}
		
		// if the player is in a party, gather a list of all matching party members (possibly
		// including this player)
		final FastList<L2PcInstance> candidates = new FastList<>();
		
		L2Object target = player.getTarget();
		if (target == null)
		{
			target = player;
		}
		
		for (final L2PcInstance partyMember : party.getPartyMembers())
		{
			temp = partyMember.getQuestState(getName());
			if ((temp != null) && (temp.get(var) != null) && temp.get(var).equalsIgnoreCase(value) && partyMember.isInsideRadius(target, 1500, true, false))
			{
				candidates.add(partyMember);
			}
		}
		
		// if there was no match, return null...
		if (candidates.size() == 0)
		{
			return null;
		}
		
		// if a match was found from the party, return one of them at random.
		return candidates.get(Rnd.get(candidates.size()));
	}
	
	/**
	 * Auxilary function for party quests. Note: This function is only here because of how commonly it may be used by quest developers. For any variations on this function, the quest script can always handle things on its own
	 * @param player : the instance of a player whose party is to be searched
	 * @param state : the state in which the party member's queststate must be in order to be considered.
	 * @return L2PcInstance: L2PcInstance for a random party member that matches the specified condition, or null if no match. If the var is null, any random party member is returned (i.e. no condition is applied).
	 */
	public L2PcInstance getRandomPartyMemberState(L2PcInstance player, State state)
	{
		// if no valid player instance is passed, there is nothing to check...
		if (player == null)
		{
			return null;
		}
		
		// for null var condition, return any random party member.
		if (state == null)
		{
			return getRandomPartyMember(player);
		}
		
		// normal cases...if the player is not in a partym check the player's state
		QuestState temp = null;
		final L2Party party = player.getParty();
		// if this player is not in a party, just check if this player instance matches the conditions itself
		if ((party == null) || (party.getPartyMembers().size() == 0))
		{
			temp = player.getQuestState(getName());
			if ((temp != null) && (temp.getState() == state))
			{
				return player; // match
			}
			return null;
		}
		
		// if the player is in a party, gather a list of all matching party members (possibly
		// including this player)
		final FastList<L2PcInstance> candidates = new FastList<>();
		
		L2Object target = player.getTarget();
		if (target == null)
		{
			target = player;
		}
		
		for (final L2PcInstance partyMember : party.getPartyMembers())
		{
			temp = partyMember.getQuestState(getName());
			if ((temp != null) && (temp.getState() == state) && partyMember.isInsideRadius(target, 1500, true, false))
			{
				candidates.add(partyMember);
			}
		}
		
		// if there was no match, return null...
		if (candidates.size() == 0)
		{
			return null;
		}
		
		// if a match was found from the party, return one of them at random.
		return candidates.get(Rnd.get(candidates.size()));
	}
	
	/**
	 * Show HTML file to client
	 * @param player
	 * @param fileName
	 * @return String : message sent to client
	 */
	public String showHtmlFile(L2PcInstance player, String fileName)
	{
		final String questId = getName();
		
		// Create handler to file linked to the quest
		final String directory = getDescr().toLowerCase();
		String content = HtmCache.getInstance().getHtm("data/scripts/" + directory + "/" + questId + "/" + fileName);
		
		if (content == null)
		{
			content = HtmCache.getInstance().getHtmForce("data/scripts/quests/" + questId + "/" + fileName);
		}
		
		if ((player != null) && (player.getTarget() != null))
		{
			content = content.replaceAll("%objectId%", String.valueOf(player.getTarget().getObjectId()));
		}
		
		// Send message to client if message not empty
		if ((content != null) && (player != null))
		{
			final NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
			npcReply.setHtml(content);
			npcReply.replace("%playername%", player.getName());
			player.sendPacket(npcReply);
			player.sendPacket(new ActionFailed());
		}
		
		return content;
	}
	
	// =========================================================
	// QUEST SPAWNS
	// =========================================================
	
	public class DeSpawnScheduleTimerTask implements Runnable
	{
		L2NpcInstance _npc = null;
		
		public DeSpawnScheduleTimerTask(L2NpcInstance npc)
		{
			_npc = npc;
		}
		
		@Override
		public void run()
		{
			_npc.onDecay();
		}
	}
	
	/**
	 * Add a temporary (quest) spawn Return instance of newly spawned npc
	 * @param npcId
	 * @param cha
	 * @return
	 */
	public L2NpcInstance addSpawn(int npcId, L2Character cha)
	{
		return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), false, 0);
	}
	
	public L2NpcInstance addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, int despawnDelay)
	{
		L2NpcInstance result = null;
		try
		{
			final L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
			if (template != null)
			{
				// Sometimes, even if the quest script specifies some xyz (for example npc.getX() etc) by the time the code
				// reaches here, xyz have become 0! Also, a questdev might have purposely set xy to 0,0...however,
				// the spawn code is coded such that if x=y=0, it looks into location for the spawn loc! This will NOT work
				// with quest spawns! For both of the above cases, we need a fail-safe spawn. For this, we use the
				// default spawn location, which is at the player's loc.
				if ((x == 0) && (y == 0))
				{
					_log.log(Level.SEVERE, "Failed to adjust bad locks for quest spawn!  Spawn aborted!");
					return null;
				}
				
				if (randomOffset)
				{
					int offset;
					offset = Rnd.get(2); // Get the direction of the offset
					if (offset == 0)
					{
						offset = -1; // make offset negative
					}
					offset *= Rnd.get(50, 100);
					x += offset;
					
					offset = Rnd.get(2); // Get the direction of the offset
					if (offset == 0)
					{
						offset = -1; // make offset negative
					}
					offset *= Rnd.get(50, 100);
					y += offset;
				}
				
				final L2Spawn spawn = new L2Spawn(template);
				spawn.setHeading(heading);
				spawn.setLocx(x);
				spawn.setLocy(y);
				spawn.setLocz(z);
				spawn.stopRespawn();
				result = spawn.spawnOne();
				
				if (despawnDelay > 0)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new DeSpawnScheduleTimerTask(result), despawnDelay);
				}
				
				return result;
			}
		}
		catch (final Exception e1)
		{
			_log.warning("Could not spawn Npc " + npcId);
		}
		
		return null;
	}
	
	public int[] getRegisteredItemIds()
	{
		return questItemIds;
	}
	
	/**
	 * This is used to register all monsters contained in mobs for a particular script
	 * @param mobs
	 */
	public void registerMobs(int[] mobs)
	{
		for (final int id : mobs)
		{
			addAttackId(id);
			addKillId(id);
			addSpawnId(id);
			addSpellFinishedId(id);
			addSkillSeeId(id);
			addFactionCallId(id);
			addAggroRangeEnterId(id);
		}
	}
	
	public Map<String, State> getStates()
	{
		return states;
	}
	
	/**
	 * @see com.l2jmobius.gameserver.scripting.ManagedScript#getScriptName()
	 */
	@Override
	public String getScriptName()
	{
		return getName();
	}
	
	/**
	 * @see com.l2jmobius.gameserver.scripting.ManagedScript#setActive(boolean)
	 */
	@Override
	public void setActive(boolean status)
	{
		// TODO implement me
	}
	
	/**
	 * @see com.l2jmobius.gameserver.scripting.ManagedScript#reload()
	 */
	@Override
	public boolean reload()
	{
		unload();
		return super.reload();
	}
	
	/**
	 * @see com.l2jmobius.gameserver.scripting.ManagedScript#unload()
	 */
	@Override
	public boolean unload()
	{
		saveGlobalData();
		
		// cancel all pending timers before reloading.
		// if timers ought to be restarted, the quest can take care of it
		// with its code (example: save global data indicating what timer must
		// be restarted).
		for (final FastList<QuestTimer> timers : allEventTimers.values())
		{
			for (final QuestTimer timer : timers)
			{
				timer.cancel();
			}
		}
		allEventTimers.clear();
		
		return QuestManager.getInstance().removeQuest(this);
	}
	
	/**
	 * @see com.l2jmobius.gameserver.scripting.ManagedScript#getScriptManager()
	 */
	@Override
	public ScriptManager<?> getScriptManager()
	{
		return QuestManager.getInstance();
	}
}