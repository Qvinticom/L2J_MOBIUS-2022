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
package com.l2jmobius.gameserver.instancemanager;

import java.io.File;
import java.util.Map;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.scripting.L2ScriptEngineManager;
import com.l2jmobius.gameserver.scripting.ScriptManager;

import javolution.util.FastMap;

public class QuestManager extends ScriptManager<Quest>
{
	protected static Logger _log = Logger.getLogger(QuestManager.class.getName());
	
	// =========================================================
	private static QuestManager _Instance;
	
	public static final QuestManager getInstance()
	{
		if (_Instance == null)
		{
			System.out.println("Initializing QuestManager");
			_Instance = new QuestManager();
		}
		return _Instance;
	}
	
	// =========================================================
	
	// =========================================================
	// Data Field
	private final Map<String, Quest> _quests = new FastMap<>();
	
	// =========================================================
	// Constructor
	public QuestManager()
	{
	}
	
	// =========================================================
	// Method - Public
	public final boolean reload(String questFolder)
	{
		final Quest q = getQuest(questFolder);
		if (q == null)
		{
			return false;
		}
		
		return q.reload();
	}
	
	/**
	 * Reloads the quest given by questId.<BR>
	 * <B>NOTICE: Will only work if the quest name is equal to the quest folder name</B>
	 * @param questId The id of the quest to be reloaded
	 * @return true if reload was successful, false otherwise
	 */
	public final boolean reload(int questId)
	{
		final Quest q = getQuest(questId);
		if (q == null)
		{
			return false;
		}
		
		return q.reload();
	}
	
	public final void reloadAllQuests()
	{
		_log.info("Reloading Server Scripts");
		try
		{
			// unload all scripts
			for (final Quest quest : _quests.values())
			{
				if (quest != null)
				{
					quest.unload();
				}
			}
			
			// Firstly, preconfigure jython path
			L2ScriptEngineManager.getInstance().preConfigure();
			
			// now load all scripts
			final File scripts = new File(Config.DATAPACK_ROOT + "/data/scripts.cfg");
			L2ScriptEngineManager.getInstance().executeScriptList(scripts);
			
			QuestManager.getInstance().report();
		}
		catch (final Exception ioe)
		{
			_log.severe("Failed loading scripts.cfg, no script is going to be loaded");
		}
	}
	
	public final void report()
	{
		System.out.println("Loaded: " + _quests.size() + " quests");
	}
	
	public final void save()
	{
		for (final Quest q : _quests.values())
		{
			q.saveGlobalData();
		}
	}
	
	// =========================================================
	// Property - Public
	public final Quest getQuest(String name)
	{
		return _quests.get(name);
	}
	
	public final Quest getQuest(int questId)
	{
		for (final Quest q : _quests.values())
		{
			if (q.getQuestIntId() == questId)
			{
				return q;
			}
		}
		return null;
	}
	
	public final void addQuest(Quest newQuest)
	{
		if (newQuest == null)
		{
			throw new IllegalArgumentException("Quest argument cannot be null");
		}
		
		final Quest old = _quests.get(newQuest.getName());
		
		// FIXME: unloading the old quest at this point is a tad too late.
		// the new quest has already initialized itself and read the data, starting
		// an unpredictable number of tasks with that data. The old quest will now
		// save data which will never be read.
		// However, requesting the newQuest to re-read the data is not necessarily a
		// good option, since the newQuest may have already started timers, spawned NPCs
		// or taken any other action which it might re-take by re-reading the data.
		// the current solution properly closes the running tasks of the old quest but
		// ignores the data; perhaps the least of all evils...
		if (old != null)
		{
			old.unload();
			_log.info("Replaced: (" + old.getName() + ") with a new version (" + newQuest.getName() + ")");
		}
		_quests.put(newQuest.getName(), newQuest);
	}
	
	public final boolean removeQuest(Quest q)
	{
		return _quests.remove(q.getName()) != null;
	}
	
	/**
	 * @see com.l2jmobius.gameserver.scripting.ScriptManager#getAllManagedScripts()
	 */
	@Override
	public Iterable<Quest> getAllManagedScripts()
	{
		return _quests.values();
	}
	
	/**
	 * @see com.l2jmobius.gameserver.scripting.ScriptManager#unload(com.l2jmobius.gameserver.scripting.ManagedScript)
	 */
	@Override
	public boolean unload(Quest ms)
	{
		ms.saveGlobalData();
		return removeQuest(ms);
	}
	
	/**
	 * @see com.l2jmobius.gameserver.scripting.ScriptManager#getScriptManagerName()
	 */
	@Override
	public String getScriptManagerName()
	{
		return "QuestManager";
	}
}