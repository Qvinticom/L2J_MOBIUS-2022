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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.scripting.L2ScriptEngineManager;
import com.l2jmobius.gameserver.scripting.ScriptManager;

public class QuestManager extends ScriptManager<Quest>
{
	protected static final Logger LOGGER = Logger.getLogger(QuestManager.class.getName());
	private Map<String, Quest> _quests = new HashMap<>();
	private static QuestManager _instance;
	private int _questCount;
	
	public static QuestManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new QuestManager();
		}
		return _instance;
	}
	
	public QuestManager()
	{
		LOGGER.info("Initializing QuestManager");
		_questCount = 0;
	}
	
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
	 * Reloads a the quest given by questId.<BR>
	 * <B>NOTICE: Will only work if the quest name is equal the quest folder name</B>
	 * @param questId The id of the quest to be reloaded
	 * @return true if reload was succesful, false otherwise
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
		LOGGER.info("Reloading Server Scripts");
		// unload all scripts
		for (Quest quest : _quests.values())
		{
			if (quest != null)
			{
				quest.unload();
			}
		}
		// now load all scripts
		final File scripts = new File(Config.DATAPACK_ROOT + "/data/scripts.cfg");
		L2ScriptEngineManager.getInstance().executeScriptsList(scripts);
		getInstance().report();
	}
	
	public final void report()
	{
		LOGGER.info("Loaded: " + _questCount + " quest scripts.");
	}
	
	public final void save()
	{
		for (Quest q : getQuests().values())
		{
			q.saveGlobalData();
		}
	}
	
	// =========================================================
	// Property - Public
	public final Quest getQuest(String name)
	{
		return getQuests().get(name);
	}
	
	public final Quest getQuest(int questId)
	{
		for (Quest q : getQuests().values())
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
		if (getQuests().containsKey(newQuest.getName()))
		{
			LOGGER.info("Replaced: " + newQuest.getName() + " with a new version.");
		}
		else if (newQuest.getQuestIntId() > 0)
		{
			_questCount++;
		}
		
		// Note: HastMap will replace the old value if the key already exists
		// so there is no need to explicitly try to remove the old reference.
		getQuests().put(newQuest.getName(), newQuest);
	}
	
	public final Map<String, Quest> getQuests()
	{
		if (_quests == null)
		{
			_quests = new HashMap<>();
		}
		
		return _quests;
	}
	
	/**
	 * This will reload quests
	 */
	public static void reload()
	{
		_instance = new QuestManager();
	}
	
	@Override
	public Iterable<Quest> getAllManagedScripts()
	{
		return _quests.values();
	}
	
	@Override
	public boolean unload(Quest ms)
	{
		ms.saveGlobalData();
		return removeQuest(ms);
	}
	
	@Override
	public String getScriptManagerName()
	{
		return "QuestManager";
	}
	
	public final boolean removeQuest(Quest q)
	{
		return _quests.remove(q.getName()) != null;
	}
	
	public final void unloadAllQuests()
	{
		LOGGER.info("Unloading Server Quests");
		// unload all scripts
		for (Quest quest : _quests.values())
		{
			if (quest != null)
			{
				quest.unload();
			}
		}
		getInstance().report();
	}
}
