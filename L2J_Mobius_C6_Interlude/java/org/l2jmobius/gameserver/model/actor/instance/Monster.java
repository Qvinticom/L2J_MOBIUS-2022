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
package org.l2jmobius.gameserver.model.actor.instance;

import java.util.Collection;
import java.util.concurrent.ScheduledFuture;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.actor.Attackable;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.knownlist.MonsterKnownList;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.spawn.Spawn;
import org.l2jmobius.gameserver.util.MinionList;

/**
 * This class manages all Monsters.<br>
 * <li>Minion</li>
 * <li>RaidBoss</li>
 * <li>GrandBoss</li>
 * @version $Revision: 1.20.4.6 $ $Date: 2005/04/06 16:13:39 $
 */
public class Monster extends Attackable
{
	protected final MinionList _minionList;
	protected ScheduledFuture<?> _minionMaintainTask = null;
	
	/**
	 * Constructor of Monster (use Creature and Npc constructor).<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Call the Creature constructor to set the _template of the Monster (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR)</li>
	 * <li>Set the name of the Monster</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it</li><br>
	 * @param objectId Identifier of the object to initialized
	 * @param template the template
	 */
	public Monster(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		getKnownList(); // init knownlist
		_minionList = new MinionList(this);
	}
	
	@Override
	public MonsterKnownList getKnownList()
	{
		if (!(super.getKnownList() instanceof MonsterKnownList))
		{
			setKnownList(new MonsterKnownList(this));
		}
		return (MonsterKnownList) super.getKnownList();
	}
	
	public void returnHome()
	{
		ThreadPool.schedule(() ->
		{
			final Spawn mobSpawn = getSpawn();
			if (!isInCombat() && !isAlikeDead() && !isDead() && (mobSpawn != null) && !isInsideRadius2D(mobSpawn.getX(), mobSpawn.getY(), mobSpawn.getZ(), Config.MAX_DRIFT_RANGE))
			{
				teleToLocation(mobSpawn.getX(), mobSpawn.getY(), mobSpawn.getZ());
			}
		}, Config.MONSTER_RETURN_DELAY * 1000);
	}
	
	/**
	 * Return True if the attacker is not another Monster.
	 * @param attacker the attacker
	 * @return true, if is auto attackable
	 */
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return !(attacker instanceof Monster);
	}
	
	/**
	 * Return True if the Monster is Agressive (aggroRange > 0).
	 * @return true, if is aggressive
	 */
	@Override
	public boolean isAggressive()
	{
		return getTemplate().getAggroRange() > 0;
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		
		if (getTemplate().getMinionData() != null)
		{
			_minionList.clearRespawnList();
			manageMinions();
		}
	}
	
	/**
	 * Spawn all minions at a regular interval.
	 */
	protected void manageMinions()
	{
		_minionMaintainTask = ThreadPool.schedule(_minionList::spawnMinions, 1000);
	}
	
	public void callMinions()
	{
		if (_minionList.hasMinions())
		{
			for (Minion minion : _minionList.getSpawnedMinions())
			{
				// Get actual coords of the minion and check to see if it's too far away from this Monster
				if (!isInsideRadius2D(minion, 200))
				{
					// Get the coords of the master to use as a base to move the minion to
					final int masterX = getX();
					final int masterY = getY();
					final int masterZ = getZ();
					
					// Calculate a new random coord for the minion based on the master's coord
					int minionX = (masterX + Rnd.get(401)) - 200;
					int minionY = (masterY + Rnd.get(401)) - 200;
					final int minionZ = masterZ;
					while (((minionX != (masterX + 30)) && (minionX != (masterX - 30))) || ((minionY != (masterY + 30)) && (minionY != (masterY - 30))))
					{
						minionX = (masterX + Rnd.get(401)) - 200;
						minionY = (masterY + Rnd.get(401)) - 200;
					}
					
					// Move the minion to the new coords
					if (!minion.isInCombat() && !minion.isDead() && !minion.isMovementDisabled())
					{
						minion.moveToLocation(minionX, minionY, minionZ, 0);
					}
				}
			}
		}
	}
	
	/**
	 * Call minions to assist.
	 * @param attacker the attacker
	 */
	public void callMinionsToAssist(Creature attacker)
	{
		if (_minionList.hasMinions())
		{
			for (Minion minion : _minionList.getSpawnedMinions())
			{
				// Trigger the aggro condition of the minion
				if ((minion != null) && !minion.isDead())
				{
					if (this instanceof RaidBoss)
					{
						minion.addDamage(attacker, 100);
					}
					else
					{
						minion.addDamage(attacker, 1);
					}
				}
			}
		}
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		if (_minionMaintainTask != null)
		{
			_minionMaintainTask.cancel(true); // doesn't do it?
		}
		
		if (this instanceof RaidBoss)
		{
			deleteSpawnedMinions();
		}
		return true;
	}
	
	/**
	 * Gets the spawned minions.
	 * @return the spawned minions
	 */
	public Collection<Minion> getSpawnedMinions()
	{
		return _minionList.getSpawnedMinions();
	}
	
	/**
	 * Gets the total spawned minions instances.
	 * @return the total spawned minions instances
	 */
	public int getTotalSpawnedMinionsInstances()
	{
		return _minionList.countSpawnedMinions();
	}
	
	/**
	 * Gets the total spawned minions groups.
	 * @return the total spawned minions groups
	 */
	public int getTotalSpawnedMinionsGroups()
	{
		return _minionList.lazyCountSpawnedMinionsGroups();
	}
	
	/**
	 * Notify minion died.
	 * @param minion the minion
	 */
	public void notifyMinionDied(Minion minion)
	{
		_minionList.moveMinionToRespawnList(minion);
	}
	
	/**
	 * Notify minion spawned.
	 * @param minion the minion
	 */
	public void notifyMinionSpawned(Minion minion)
	{
		_minionList.addSpawnedMinion(minion);
	}
	
	/**
	 * Checks for minions.
	 * @return true, if successful
	 */
	public boolean hasMinions()
	{
		return _minionList.hasMinions();
	}
	
	@Override
	public void addDamageHate(Creature attacker, int damage, int aggro)
	{
		if (!(attacker instanceof Monster))
		{
			super.addDamageHate(attacker, damage, aggro);
		}
	}
	
	@Override
	public void deleteMe()
	{
		if (_minionList.hasMinions())
		{
			if (_minionMaintainTask != null)
			{
				_minionMaintainTask.cancel(true);
			}
			
			deleteSpawnedMinions();
		}
		super.deleteMe();
	}
	
	/**
	 * Delete spawned minions.
	 */
	public void deleteSpawnedMinions()
	{
		_minionList.clearRespawnList();
	}
	
	@Override
	public boolean isRandomWalkingEnabled()
	{
		return Config.MAX_DRIFT_RANGE > 0;
	}
	
	@Override
	public boolean isMonster()
	{
		return true;
	}
}
