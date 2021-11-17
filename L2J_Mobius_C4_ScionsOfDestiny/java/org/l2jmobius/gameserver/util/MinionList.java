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
package org.l2jmobius.gameserver.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.data.sql.NpcTable;
import org.l2jmobius.gameserver.instancemanager.IdManager;
import org.l2jmobius.gameserver.model.MinionData;
import org.l2jmobius.gameserver.model.actor.instance.Minion;
import org.l2jmobius.gameserver.model.actor.instance.Monster;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;

/**
 * @author luisantonioa, Mobius
 */
public class MinionList
{
	private final Set<Minion> _spawnedMinions = ConcurrentHashMap.newKeySet();
	private final Map<Long, Integer> _respawnTasks = new ConcurrentHashMap<>();
	private final Monster _master;
	
	public MinionList(Monster master)
	{
		_master = master;
	}
	
	public int countSpawnedMinions()
	{
		return _spawnedMinions.size();
	}
	
	public int countSpawnedMinionsById(int minionId)
	{
		int count = 0;
		for (Minion minion : _spawnedMinions)
		{
			if (minion.getNpcId() == minionId)
			{
				count++;
			}
		}
		return count;
	}
	
	public boolean hasMinions()
	{
		return !_spawnedMinions.isEmpty();
	}
	
	public Collection<Minion> getSpawnedMinions()
	{
		return _spawnedMinions;
	}
	
	public void addSpawnedMinion(Minion minion)
	{
		_spawnedMinions.add(minion);
	}
	
	public int lazyCountSpawnedMinionsGroups()
	{
		final Set<Integer> seenGroups = new HashSet<>();
		for (Minion minion : _spawnedMinions)
		{
			seenGroups.add(minion.getNpcId());
		}
		return seenGroups.size();
	}
	
	public void removeSpawnedMinion(Minion minion)
	{
		_spawnedMinions.remove(minion);
	}
	
	public void moveMinionToRespawnList(Minion minion)
	{
		final Long current = Chronos.currentTimeMillis();
		_spawnedMinions.remove(minion);
		if (_respawnTasks.get(current) == null)
		{
			_respawnTasks.put(current, minion.getNpcId());
		}
		else
		{
			for (int i = 1; i < 30; i++)
			{
				if (_respawnTasks.get(current + i) == null)
				{
					_respawnTasks.put(current + i, minion.getNpcId());
					break;
				}
			}
		}
	}
	
	public void clearRespawnList()
	{
		for (Minion minion : _spawnedMinions)
		{
			if (minion == null)
			{
				continue;
			}
			
			minion.abortAttack();
			minion.abortCast();
			minion.deleteMe();
		}
		
		_spawnedMinions.clear();
		_respawnTasks.clear();
	}
	
	/**
	 * Manage respawning of minions for this RaidBoss.
	 */
	public void maintainMinions()
	{
		if ((_master == null) || _master.isAlikeDead())
		{
			return;
		}
		
		final Long current = Chronos.currentTimeMillis();
		if (_respawnTasks != null)
		{
			for (Entry<Long, Integer> entry : _respawnTasks.entrySet())
			{
				final long deathTime = entry.getKey();
				final double delay = Config.RAID_MINION_RESPAWN_TIMER;
				if ((current - deathTime) > delay)
				{
					spawnSingleMinion(entry.getValue());
					_respawnTasks.remove(deathTime);
				}
			}
		}
	}
	
	/**
	 * Manage the spawn of all Minions of this RaidBoss.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Get the Minion data of all Minions that must be spawn</li>
	 * <li>For each Minion type, spawn the amount of Minion needed</li>
	 */
	public void spawnMinions()
	{
		if ((_master == null) || _master.isAlikeDead())
		{
			return;
		}
		
		int minionCount;
		int minionId;
		int minionsToSpawn;
		for (MinionData minion : _master.getTemplate().getMinionData())
		{
			minionCount = minion.getAmount();
			minionId = minion.getMinionId();
			minionsToSpawn = minionCount - countSpawnedMinionsById(minionId);
			for (int i = 0; i < minionsToSpawn; i++)
			{
				spawnSingleMinion(minionId);
			}
		}
	}
	
	/**
	 * Init a Minion and add it in the world as a visible object.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Get the template of the Minion to spawn</li>
	 * <li>Create and Init the Minion and generate its Identifier</li>
	 * <li>Set the Minion HP, MP and Heading</li>
	 * <li>Set the Minion leader to this RaidBoss</li>
	 * <li>Init the position of the Minion and add it in the world as a visible object</li><br>
	 * @param minionid The NpcTemplate Identifier of the Minion to spawn
	 */
	public void spawnSingleMinion(int minionid)
	{
		// Get the template of the Minion to spawn
		final NpcTemplate minionTemplate = NpcTable.getInstance().getTemplate(minionid);
		
		// Create and Init the Minion and generate its Identifier
		final Minion monster = new Minion(IdManager.getInstance().getNextId(), minionTemplate);
		
		// Set the Minion HP, MP and Heading
		monster.setCurrentHpMp(monster.getMaxHp(), monster.getMaxMp());
		monster.setHeading(_master.getHeading());
		
		// Set the Minion leader to this RaidBoss
		monster.setLeader(_master);
		
		// Init the position of the Minion and add it in the world as a visible object
		int spawnConstant;
		final int randSpawnLim = 170;
		int randPlusMin = 1;
		spawnConstant = Rnd.get(randSpawnLim);
		randPlusMin = Rnd.get(2);
		if (randPlusMin == 1)
		{
			spawnConstant *= -1;
		}
		
		final int newX = _master.getX() + spawnConstant;
		spawnConstant = Rnd.get(randSpawnLim);
		randPlusMin = Rnd.get(2);
		if (randPlusMin == 1)
		{
			spawnConstant *= -1;
		}
		
		final int newY = _master.getY() + spawnConstant;
		monster.spawnMe(newX, newY, _master.getZ());
		
		// Assist master
		if (!_master.getAggroList().isEmpty())
		{
			monster.getAggroList().putAll(_master.getAggroList());
			monster.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, monster.getAggroList().keySet().stream().findFirst().get());
		}
	}
}
