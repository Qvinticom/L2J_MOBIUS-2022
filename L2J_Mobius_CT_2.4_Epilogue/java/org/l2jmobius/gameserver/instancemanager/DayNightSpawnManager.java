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
package org.l2jmobius.gameserver.instancemanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.GameTimeController;
import org.l2jmobius.gameserver.enums.RaidBossStatus;
import org.l2jmobius.gameserver.model.Spawn;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.RaidBossInstance;

/**
 * @author godson
 */
public class DayNightSpawnManager
{
	private static Logger LOGGER = Logger.getLogger(DayNightSpawnManager.class.getName());
	
	private final List<Spawn> _dayCreatures = new ArrayList<>();
	private final List<Spawn> _nightCreatures = new ArrayList<>();
	private final Map<Spawn, RaidBossInstance> _bosses = new ConcurrentHashMap<>();
	
	// private static int _currentState; // 0 = Day, 1 = Night
	
	public static DayNightSpawnManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	protected DayNightSpawnManager()
	{
		// Prevent external initialization.
	}
	
	public void addDayCreature(Spawn spawnDat)
	{
		_dayCreatures.add(spawnDat);
	}
	
	public void addNightCreature(Spawn spawnDat)
	{
		_nightCreatures.add(spawnDat);
	}
	
	/**
	 * Spawn Day Creatures, and Unspawn Night Creatures
	 */
	public void spawnDayCreatures()
	{
		spawnCreatures(_nightCreatures, _dayCreatures, "night", "day");
	}
	
	/**
	 * Spawn Night Creatures, and Unspawn Day Creatures
	 */
	public void spawnNightCreatures()
	{
		spawnCreatures(_dayCreatures, _nightCreatures, "day", "night");
	}
	
	/**
	 * Manage Spawn/Respawn
	 * @param unSpawnCreatures List with spawns must be unspawned
	 * @param spawnCreatures List with spawns must be spawned
	 * @param unspawnLogInfo String for log info for unspawned NpcInstance
	 * @param spawnLogInfo String for log info for spawned NpcInstance
	 */
	private void spawnCreatures(List<Spawn> unSpawnCreatures, List<Spawn> spawnCreatures, String unspawnLogInfo, String spawnLogInfo)
	{
		try
		{
			if (!unSpawnCreatures.isEmpty())
			{
				int i = 0;
				for (Spawn spawn : unSpawnCreatures)
				{
					if (spawn == null)
					{
						continue;
					}
					
					spawn.stopRespawn();
					final Npc last = spawn.getLastSpawn();
					if (last != null)
					{
						last.deleteMe();
						i++;
					}
				}
				LOGGER.info("DayNightSpawnManager: Removed " + i + " " + unspawnLogInfo + " creatures");
			}
			
			int i = 0;
			for (Spawn spawnDat : spawnCreatures)
			{
				if (spawnDat == null)
				{
					continue;
				}
				spawnDat.startRespawn();
				spawnDat.doSpawn();
				i++;
			}
			
			LOGGER.info("DayNightSpawnManager: Spawned " + i + " " + spawnLogInfo + " creatures");
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Error while spawning creatures: " + e.getMessage(), e);
		}
	}
	
	private void changeMode(int mode)
	{
		if (_nightCreatures.isEmpty() && _dayCreatures.isEmpty() && _bosses.isEmpty())
		{
			return;
		}
		
		switch (mode)
		{
			case 0:
			{
				spawnDayCreatures();
				specialNightBoss(0);
				break;
			}
			case 1:
			{
				spawnNightCreatures();
				specialNightBoss(1);
				break;
			}
			default:
			{
				LOGGER.warning("DayNightSpawnManager: Wrong mode sent");
				break;
			}
		}
	}
	
	public DayNightSpawnManager trim()
	{
		((ArrayList<?>) _nightCreatures).trimToSize();
		((ArrayList<?>) _dayCreatures).trimToSize();
		return this;
	}
	
	public void notifyChangeMode()
	{
		try
		{
			changeMode(GameTimeController.getInstance().isNight() ? 1 : 0);
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Error while notifyChangeMode(): " + e.getMessage(), e);
		}
	}
	
	public void cleanUp()
	{
		_nightCreatures.clear();
		_dayCreatures.clear();
		_bosses.clear();
	}
	
	private void specialNightBoss(int mode)
	{
		try
		{
			RaidBossInstance boss;
			for (Entry<Spawn, RaidBossInstance> entry : _bosses.entrySet())
			{
				boss = entry.getValue();
				if ((boss == null) && (mode == 1))
				{
					final Spawn spawn = entry.getKey();
					boss = (RaidBossInstance) spawn.doSpawn();
					RaidBossSpawnManager.getInstance().notifySpawnNightBoss(boss);
					_bosses.put(spawn, boss);
					continue;
				}
				
				if ((boss == null) && (mode == 0))
				{
					continue;
				}
				
				if ((boss != null) && (boss.getId() == 25328) && (boss.getRaidStatus() == RaidBossStatus.ALIVE))
				{
					handleHellmans(boss, mode);
				}
				return;
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Error while specialNoghtBoss(): " + e.getMessage(), e);
		}
	}
	
	private void handleHellmans(RaidBossInstance boss, int mode)
	{
		switch (mode)
		{
			case 0:
			{
				boss.deleteMe();
				LOGGER.info(getClass().getSimpleName() + ": Deleting Hellman raidboss");
				break;
			}
			case 1:
			{
				if (!boss.isSpawned())
				{
					boss.spawnMe();
				}
				LOGGER.info(getClass().getSimpleName() + ": Spawning Hellman raidboss");
				break;
			}
		}
	}
	
	public RaidBossInstance handleBoss(Spawn spawnDat)
	{
		if (_bosses.containsKey(spawnDat))
		{
			return _bosses.get(spawnDat);
		}
		
		if (GameTimeController.getInstance().isNight())
		{
			final RaidBossInstance raidboss = (RaidBossInstance) spawnDat.doSpawn();
			_bosses.put(spawnDat, raidboss);
			
			return raidboss;
		}
		return null;
	}
	
	private static class SingletonHolder
	{
		protected static final DayNightSpawnManager INSTANCE = new DayNightSpawnManager();
	}
}
