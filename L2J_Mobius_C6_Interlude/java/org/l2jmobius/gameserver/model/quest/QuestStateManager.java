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
package org.l2jmobius.gameserver.model.quest;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.commons.concurrent.ThreadPool;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;

public class QuestStateManager
{
	public class ScheduleTimerTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				cleanUp();
				ThreadPool.schedule(new ScheduleTimerTask(), 60000);
			}
			catch (Throwable t)
			{
			}
		}
	}
	
	private List<QuestState> _questStates = new ArrayList<>();
	
	public QuestStateManager()
	{
		ThreadPool.schedule(new ScheduleTimerTask(), 60000);
	}
	
	/**
	 * Add QuestState for the specified player instance
	 * @param quest
	 * @param player
	 * @param state
	 */
	public void addQuestState(Quest quest, PlayerInstance player, byte state)
	{
		QuestState qs = getQuestState(player);
		if (qs == null)
		{
			qs = new QuestState(quest, player, state);
		}
	}
	
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
	
	/**
	 * Remove QuestState instance
	 * @param qs
	 */
	private void removeQuestState(QuestState qs)
	{
		qs = null;
	}
	
	/**
	 * Return QuestState for specified player instance
	 * @param player
	 * @return
	 */
	public QuestState getQuestState(PlayerInstance player)
	{
		for (int i = 0; i < getQuestStates().size(); i++)
		{
			if ((getQuestStates().get(i).getPlayer() != null) && (getQuestStates().get(i).getPlayer().getObjectId() == player.getObjectId()))
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
		if (_questStates == null)
		{
			_questStates = new ArrayList<>();
		}
		return _questStates;
	}
	
	public static QuestStateManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final QuestStateManager INSTANCE = new QuestStateManager();
	}
}
