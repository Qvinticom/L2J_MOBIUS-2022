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

import java.util.Map;
import java.util.logging.Logger;

import com.l2jmobius.gameserver.GameTimeController;
import com.l2jmobius.gameserver.model.L2Spawn;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2RaidBossInstance;

import javolution.util.FastMap;

/**
 * This class ...
 * @version $Revision: $ $Date: $
 * @author godson
 */

public class DayNightSpawnManager
{
	private static Logger _log = Logger.getLogger(DayNightSpawnManager.class.getName());
	
	private static DayNightSpawnManager _instance;
	private static Map<L2Spawn, L2NpcInstance> _dayCreatures;
	private static Map<L2Spawn, L2NpcInstance> _nightCreatures;
	private static Map<L2Spawn, L2RaidBossInstance> _bosses;
	
	// private static int _currentState; // 0 = Day, 1 = Night
	
	public static DayNightSpawnManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new DayNightSpawnManager();
		}
		return _instance;
	}
	
	private DayNightSpawnManager()
	{
		_dayCreatures = new FastMap<>();
		_nightCreatures = new FastMap<>();
		_bosses = new FastMap<>();
		
		_log.info("DayNightSpawnManager: Day/Night handler initialized");
	}
	
	public void addDayCreature(L2Spawn spawnDat)
	{
		if (_dayCreatures.containsKey(spawnDat))
		{
			_log.warning("DayNightSpawnManager: Spawn already added into day map");
			return;
		}
		_dayCreatures.put(spawnDat, null);
	}
	
	public void addNightCreature(L2Spawn spawnDat)
	{
		if (_nightCreatures.containsKey(spawnDat))
		{
			_log.warning("DayNightSpawnManager: Spawn already added into night map");
			return;
		}
		_nightCreatures.put(spawnDat, null);
	}
	
	public void spawnDayCreatures()
	{
		try
		{
			if (_nightCreatures.size() != 0)
			{
				int i = 0;
				for (final L2NpcInstance nightCreature : _nightCreatures.values())
				{
					if (nightCreature == null)
					{
						continue;
					}
					
					if (nightCreature.getSpawn() != null)
					{
						nightCreature.getSpawn().stopRespawn();
					}
					
					nightCreature.deleteMe();
					i++;
				}
				
				_log.info("DayNightSpawnManager: Deleted " + i + " night creatures");
			}
			
			int i = 0;
			
			L2NpcInstance creature = null;
			for (final L2Spawn spawnDat : _dayCreatures.keySet())
			{
				
				if (_dayCreatures.get(spawnDat) == null)
				{
					creature = spawnDat.doSpawn();
					if (creature == null)
					{
						continue;
					}
					creature.setCurrentHp(creature.getMaxHp());
					creature.setCurrentMp(creature.getMaxMp());
					
					_dayCreatures.remove(spawnDat);
					_dayCreatures.put(spawnDat, creature);
					
					creature = _dayCreatures.get(spawnDat);
					creature.getSpawn().startRespawn();
					
				}
				else
				{
					creature = _dayCreatures.get(spawnDat);
					if (creature == null)
					{
						continue;
					}
					creature.getSpawn().startRespawn();
					creature.setCurrentHp(creature.getMaxHp());
					creature.setCurrentMp(creature.getMaxMp());
					creature.spawnMe();
					
				}
				i++;
			}
			
			_log.info("DayNightSpawnManager: Spawning " + i + " day creatures");
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void spawnNightCreatures()
	{
		try
		{
			if (_dayCreatures.size() != 0)
			{
				int i = 0;
				for (final L2NpcInstance dayCreature : _dayCreatures.values())
				{
					if (dayCreature == null)
					{
						continue;
					}
					
					if (dayCreature.getSpawn() != null)
					{
						dayCreature.getSpawn().stopRespawn();
					}
					
					dayCreature.deleteMe();
					i++;
				}
				_log.info("DayNightSpawnManager: Deleted " + i + " day creatures");
			}
			
			int i = 0;
			
			L2NpcInstance creature = null;
			for (final L2Spawn spawnDat : _nightCreatures.keySet())
			{
				
				if (_nightCreatures.get(spawnDat) == null)
				{
					creature = spawnDat.doSpawn();
					if (creature == null)
					{
						continue;
					}
					
					_nightCreatures.remove(spawnDat);
					_nightCreatures.put(spawnDat, creature);
					creature.setCurrentHp(creature.getMaxHp());
					creature.setCurrentMp(creature.getMaxMp());
					
					creature = _nightCreatures.get(spawnDat);
					creature.getSpawn().startRespawn();
					
				}
				else
				{
					creature = _nightCreatures.get(spawnDat);
					if (creature == null)
					{
						continue;
					}
					creature.getSpawn().startRespawn();
					creature.setCurrentHp(creature.getMaxHp());
					creature.setCurrentMp(creature.getMaxMp());
					creature.spawnMe();
					
				}
				
				i++;
			}
			
			_log.info("DayNightSpawnManager: Spawning " + i + " night creatures");
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void changeMode(int mode)
	{
		if ((_nightCreatures.size() == 0) && (_dayCreatures.size() == 0))
		{
			return;
		}
		
		switch (mode)
		{
			case 0:
				spawnDayCreatures();
				specialNightBoss(0);
				break;
			case 1:
				spawnNightCreatures();
				specialNightBoss(1);
				break;
			default:
				_log.warning("DayNightSpawnManager: Wrong mode sent");
				break;
		}
	}
	
	public void notifyChangeMode()
	{
		try
		{
			if (GameTimeController.getInstance().isNowNight())
			{
				changeMode(1);
			}
			else
			{
				changeMode(0);
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void cleanUp()
	{
		_nightCreatures.clear();
		_dayCreatures.clear();
		_bosses.clear();
	}
	
	public void specialNightBoss(int mode)
	{
		try
		{
			for (final L2Spawn spawn : _bosses.keySet())
			{
				L2RaidBossInstance boss = _bosses.get(spawn);
				
				if (boss == null)
				{
					if (mode == 1)
					{
						boss = (L2RaidBossInstance) spawn.doSpawn();
						RaidBossSpawnManager.getInstance().notifySpawnNightBoss(boss);
						_bosses.remove(spawn);
						_bosses.put(spawn, boss);
					}
					continue;
				}
				
				if ((boss.getNpcId() == 10328) && boss.getRaidStatus().equals(RaidBossSpawnManager.StatusEnum.ALIVE))
				{
					handleHellmans(boss, mode);
				}
				return;
				
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void handleHellmans(L2RaidBossInstance boss, int mode)
	{
		switch (mode)
		{
			case 0:
				boss.deleteMe();
				_log.info("DayNightSpawnManager: Deleting Hellman raidboss");
				break;
			case 1:
				boss.spawnMe();
				_log.info("DayNightSpawnManager: Spawning Hellman raidboss");
				break;
		}
	}
	
	public L2RaidBossInstance handleBoss(L2Spawn spawnDat)
	{
		if (_bosses.containsKey(spawnDat))
		{
			return _bosses.get(spawnDat);
		}
		
		if (GameTimeController.getInstance().isNowNight())
		{
			final L2RaidBossInstance raidboss = (L2RaidBossInstance) spawnDat.doSpawn();
			_bosses.put(spawnDat, raidboss);
			return raidboss;
		}
		_bosses.put(spawnDat, null);
		
		return null;
	}
}