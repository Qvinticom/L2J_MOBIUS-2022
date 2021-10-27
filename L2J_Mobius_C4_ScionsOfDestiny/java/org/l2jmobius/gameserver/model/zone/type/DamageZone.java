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
package org.l2jmobius.gameserver.model.zone.type;

import java.util.concurrent.Future;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.zone.ZoneType;

/**
 * A damage zone
 * @author durgus
 */
public class DamageZone extends ZoneType
{
	private int _damagePerSec;
	
	private final int _startTask;
	private final int _reuseTask;
	protected volatile Future<?> _task;
	
	public DamageZone(int id)
	{
		super(id);
		
		// Setup default damage
		_damagePerSec = 100;
		
		// Setup default start / reuse time
		_startTask = 10;
		_reuseTask = 5000;
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("dmgSec"))
		{
			_damagePerSec = Integer.parseInt(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}
	
	@Override
	protected void onEnter(Creature creature)
	{
		Future<?> task = _task;
		if ((task == null) && (_damagePerSec != 0))
		{
			
			synchronized (this)
			{
				task = _task;
				if (task == null)
				{
					_task = task = ThreadPool.scheduleAtFixedRate(new ApplyDamage(), _startTask, _reuseTask);
				}
			}
		}
	}
	
	@Override
	protected void onExit(Creature creature)
	{
		if (getCharactersInside().isEmpty() && (_task != null))
		{
			_task.cancel(true);
			_task = null;
		}
	}
	
	protected int getDamagePerSecond()
	{
		return _damagePerSec;
	}
	
	private class ApplyDamage implements Runnable
	{
		protected ApplyDamage()
		{
		}
		
		@Override
		public void run()
		{
			if (getCharactersInside().isEmpty())
			{
				_task.cancel(false);
				_task = null;
				return;
			}
			
			for (Creature character : getCharactersInside())
			{
				if ((character != null) && character.isPlayer() && !character.isDead())
				{
					character.reduceCurrentHp(getDamagePerSecond(), null);
				}
			}
		}
	}
	
	@Override
	protected void onDieInside(Creature creature)
	{
	}
	
	@Override
	protected void onReviveInside(Creature creature)
	{
	}
}
