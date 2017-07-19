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

import java.util.List;

import com.l2jmobius.gameserver.ThreadPoolManager;

import javolution.util.FastList;

public class QuestStateManager
{
	// =========================================================
	// Schedule Task
	public class ScheduleTimerTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				cleanUp();
				ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTimerTask(), 60000);
			}
			catch (final Throwable t)
			{
			}
		}
	}
	
	// =========================================================
	// Data Field
	private static QuestStateManager _Instance;
	private List<QuestState> _QuestStates = new FastList<>();
	
	// =========================================================
	// Constructor
	public QuestStateManager()
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTimerTask(), 60000);
	}
	
	// =========================================================
	// Method - Public
	
	/**
	 * Remove all QuestState for all player instance that does not exist
	 */
	public void cleanUp()
	{
		for (int i = getQuestStates().size() - 1; i >= 0; i--)
		{
			if (getQuestStates().get(i).getPlayer() == null)
			{
				removeQuestState(getQuestStates().get(i));
				getQuestStates().remove(i);
			}
		}
	}
	
	// =========================================================
	// Method - Private
	/**
	 * Remove QuestState instance
	 * @param qs
	 */
	private void removeQuestState(QuestState qs)
	{
		qs = null;
	}
	
	// =========================================================
	// Property - Public
	public static final QuestStateManager getInstance()
	{
		if (_Instance == null)
		{
			_Instance = new QuestStateManager();
		}
		return _Instance;
	}
	
	/**
	 * Return QuestState for specified player instance
	 * @param quest
	 * @return
	 */
	public QuestState getQuestState(Quest quest)
	{
		for (int i = 0; i < getQuestStates().size(); i++)
		{
			if ((getQuestStates().get(i).getQuest() != null) && (getQuestStates().get(i).getQuest().getQuestIntId() == quest.getQuestIntId()))
			{
				return getQuestStates().get(i);
			}
			
		}
		
		return null;
	}
	
	/**
	 * Return all QuestState
	 * @return
	 */
	public List<QuestState> getQuestStates()
	{
		if (_QuestStates == null)
		{
			_QuestStates = new FastList<>();
		}
		return _QuestStates;
	}
}