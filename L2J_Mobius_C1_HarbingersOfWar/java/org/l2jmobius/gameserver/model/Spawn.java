/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.gameserver.model;

import java.lang.reflect.Constructor;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.IdFactory;
import org.l2jmobius.gameserver.model.actor.instance.MonsterInstance;
import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import org.l2jmobius.gameserver.templates.Npc;
import org.l2jmobius.gameserver.threadpool.ThreadPool;
import org.l2jmobius.util.Rnd;

public class Spawn
{
	private static Logger _log = Logger.getLogger(Spawn.class.getName());
	private final Npc _template;
	private int _id;
	private String _location;
	private int _maximumCount;
	private int _currentCount;
	private int _scheduledCount;
	private int _npcid;
	private int _locx;
	private int _locy;
	private int _locz;
	private int _randomx;
	private int _randomy;
	private int _heading;
	private int _respawnDelay;
	private final Constructor<?> _constructor;
	
	public Spawn(Npc mobTemplate) throws ClassNotFoundException
	{
		_template = mobTemplate;
		final String implementationName = _template.getType();
		_constructor = Class.forName("org.l2jmobius.gameserver.model.actor.instance." + implementationName + "Instance").getConstructors()[0];
	}
	
	public int getAmount()
	{
		return _maximumCount;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public String getLocation()
	{
		return _location;
	}
	
	public int getLocx()
	{
		return _locx;
	}
	
	public int getLocy()
	{
		return _locy;
	}
	
	public int getLocz()
	{
		return _locz;
	}
	
	public int getNpcid()
	{
		return _npcid;
	}
	
	public int getHeading()
	{
		return _heading;
	}
	
	public int getRandomx()
	{
		return _randomx;
	}
	
	public int getRandomy()
	{
		return _randomy;
	}
	
	public void setAmount(int amount)
	{
		_maximumCount = amount;
	}
	
	public void setId(int id)
	{
		_id = id;
	}
	
	public void setLocation(String location)
	{
		_location = location;
	}
	
	public void setLocx(int locx)
	{
		_locx = locx;
	}
	
	public void setLocy(int locy)
	{
		_locy = locy;
	}
	
	public void setLocz(int locz)
	{
		_locz = locz;
	}
	
	public void setNpcid(int npcid)
	{
		_npcid = npcid;
	}
	
	public void setHeading(int heading)
	{
		_heading = heading;
	}
	
	public void setRandomx(int randomx)
	{
		_randomx = randomx;
	}
	
	public void setRandomy(int randomy)
	{
		_randomy = randomy;
	}
	
	public void decreaseCount(int npcId)
	{
		--_currentCount;
		if ((_scheduledCount + _currentCount) < _maximumCount)
		{
			++_scheduledCount;
			ThreadPool.schedule(new SpawnTask(npcId), _respawnDelay);
		}
	}
	
	public void init()
	{
		while (_currentCount < _maximumCount)
		{
			doSpawn();
		}
	}
	
	public void doSpawn()
	{
		NpcInstance mob = null;
		try
		{
			final Object[] parameters = new Object[]
			{
				_template
			};
			mob = (NpcInstance) _constructor.newInstance(parameters);
			mob.setObjectId(IdFactory.getInstance().getNextId());
			mob.setAutoAttackable(mob instanceof MonsterInstance);
			if (getRandomx() > 0)
			{
				final int random1 = Rnd.get(getRandomx());
				final int newlocx = (getLocx() + Rnd.get(getRandomx())) - random1;
				mob.setX(newlocx);
			}
			else
			{
				mob.setX(getLocx());
			}
			if (getRandomy() > 0)
			{
				final int random2 = Rnd.get(getRandomy());
				final int newlocy = (getLocy() + Rnd.get(getRandomy())) - random2;
				mob.setY(newlocy);
			}
			else
			{
				mob.setY(getLocy());
			}
			mob.setZ(getLocz());
			mob.setLevel(_template.getLevel());
			mob.setExpReward(_template.getExp());
			mob.setSpReward(_template.getSp());
			mob.setMaxHp(_template.getHp());
			mob.setCurrentHp(_template.getHp());
			mob.setWalkSpeed(_template.getWalkSpeed());
			mob.setRunSpeed(_template.getRunSpeed());
			mob.setPhysicalAttack(_template.getPatk());
			mob.setPhysicalDefense(_template.getPdef());
			mob.setMagicalAttack(_template.getMatk());
			mob.setMagicalDefense(_template.getMdef());
			mob.setMagicalSpeed(_template.getMatkspd());
			if (getHeading() == -1)
			{
				mob.setHeading(Rnd.get(61794));
			}
			else
			{
				mob.setHeading(getHeading());
			}
			mob.setMovementMultiplier(1.08);
			mob.setAttackSpeedMultiplier(0.983664);
			mob.setAttackRange(_template.getAttackRange());
			mob.setAggressive(_template.getAgro());
			mob.setRightHandItem(_template.getRhand());
			mob.setLeftHandItem(_template.getLhand());
			mob.setSpawn(this);
			World.getInstance().storeObject(mob);
			World.getInstance().addVisibleObject(mob);
			++_currentCount;
		}
		catch (Exception e)
		{
			_log.warning("NPC class not found");
		}
	}
	
	public void setRespawnDelay(int i)
	{
		_respawnDelay = i * 1000;
	}
	
	class SpawnTask implements Runnable
	{
		NpcInstance _instance;
		int _objId;
		
		public SpawnTask(int objid)
		{
			_objId = objid;
		}
		
		@Override
		public void run()
		{
			doSpawn();
			_scheduledCount--;
		}
	}
}
