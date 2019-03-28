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
package com.l2jmobius.gameserver.taskmanager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.l2jmobius.commons.concurrent.ThreadPool;
import com.l2jmobius.gameserver.model.actor.Creature;
import com.l2jmobius.gameserver.model.actor.Summon;
import com.l2jmobius.gameserver.model.actor.instance.CubicInstance;
import com.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import com.l2jmobius.gameserver.network.serverpackets.AutoAttackStop;

/**
 * @version $Revision: $ $Date: $
 * @author Luca Baldi
 */
public class AttackStanceTaskManager
{
	protected static final Logger LOGGER = Logger.getLogger(AttackStanceTaskManager.class.getName());
	
	protected Map<Creature, Long> _attackStanceTasks = new ConcurrentHashMap<>();
	
	private AttackStanceTaskManager()
	{
		ThreadPool.scheduleAtFixedRate(new FightModeScheduler(), 0, 1000);
	}
	
	public static AttackStanceTaskManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public void addAttackStanceTask(Creature actor)
	{
		if (actor instanceof Summon)
		{
			final Summon summon = (Summon) actor;
			actor = summon.getOwner();
		}
		if (actor instanceof PlayerInstance)
		{
			final PlayerInstance player = (PlayerInstance) actor;
			for (CubicInstance cubic : player.getCubics().values())
			{
				if (cubic.getId() != CubicInstance.LIFE_CUBIC)
				{
					cubic.doAction();
				}
			}
		}
		_attackStanceTasks.put(actor, System.currentTimeMillis());
	}
	
	public void removeAttackStanceTask(Creature actor)
	{
		if (actor instanceof Summon)
		{
			final Summon summon = (Summon) actor;
			actor = summon.getOwner();
		}
		_attackStanceTasks.remove(actor);
	}
	
	public boolean getAttackStanceTask(Creature actor)
	{
		if (actor instanceof Summon)
		{
			final Summon summon = (Summon) actor;
			actor = summon.getOwner();
		}
		return _attackStanceTasks.containsKey(actor);
	}
	
	private class FightModeScheduler implements Runnable
	{
		protected FightModeScheduler()
		{
		}
		
		@Override
		public void run()
		{
			final Long current = System.currentTimeMillis();
			try
			{
				if (_attackStanceTasks != null)
				{
					synchronized (this)
					{
						for (Creature actor : _attackStanceTasks.keySet())
						{
							if ((current - _attackStanceTasks.get(actor)) > 15000)
							{
								actor.broadcastPacket(new AutoAttackStop(actor.getObjectId()));
								if ((actor instanceof PlayerInstance) && (((PlayerInstance) actor).getPet() != null))
								{
									((PlayerInstance) actor).getPet().broadcastPacket(new AutoAttackStop(((PlayerInstance) actor).getPet().getObjectId()));
								}
								actor.getAI().setAutoAttacking(false);
								_attackStanceTasks.remove(actor);
							}
						}
					}
				}
			}
			catch (Exception e)
			{
				// TODO: Find out the reason for exception. Unless caught here, players remain in attack positions.
				LOGGER.warning("Error in FightModeScheduler: " + e.getMessage());
			}
		}
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final AttackStanceTaskManager _instance = new AttackStanceTaskManager();
	}
}
