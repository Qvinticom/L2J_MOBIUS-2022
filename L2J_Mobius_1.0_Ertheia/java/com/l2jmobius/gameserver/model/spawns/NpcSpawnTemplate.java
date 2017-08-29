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
package com.l2jmobius.gameserver.model.spawns;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jmobius.commons.util.Rnd;
import com.l2jmobius.gameserver.data.xml.impl.NpcData;
import com.l2jmobius.gameserver.datatables.SpawnTable;
import com.l2jmobius.gameserver.instancemanager.DBSpawnManager;
import com.l2jmobius.gameserver.instancemanager.ZoneManager;
import com.l2jmobius.gameserver.model.ChanceLocation;
import com.l2jmobius.gameserver.model.L2Spawn;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jmobius.gameserver.model.actor.templates.L2NpcTemplate;
import com.l2jmobius.gameserver.model.holders.MinionHolder;
import com.l2jmobius.gameserver.model.instancezone.Instance;
import com.l2jmobius.gameserver.model.interfaces.IParameterized;
import com.l2jmobius.gameserver.model.zone.type.L2SpawnTerritory;

/**
 * @author UnAfraid
 */
public class NpcSpawnTemplate implements Cloneable, IParameterized<StatsSet>
{
	private static final Logger LOGGER = Logger.getLogger(SpawnTemplate.class.getName());
	
	private final int _id;
	private final int _count;
	private final Duration _respawnTime;
	private final Duration _respawnTimeRandom;
	private List<ChanceLocation> _locations;
	private L2SpawnTerritory _zone;
	private StatsSet _parameters;
	private final boolean _spawnAnimation;
	private final boolean _saveInDB;
	private final String _dbName;
	private List<MinionHolder> _minions;
	private final SpawnTemplate _spawnTemplate;
	private final SpawnGroup _group;
	private final Set<L2Npc> _spawnedNpcs = ConcurrentHashMap.newKeySet();
	
	private NpcSpawnTemplate(NpcSpawnTemplate template)
	{
		_spawnTemplate = template._spawnTemplate;
		_group = template._group;
		_id = template._id;
		_count = template._count;
		_respawnTime = template._respawnTime;
		_respawnTimeRandom = template._respawnTimeRandom;
		_spawnAnimation = template._spawnAnimation;
		_saveInDB = template._saveInDB;
		_dbName = template._dbName;
		_locations = template._locations;
		_zone = template._zone;
		_parameters = template._parameters;
		_minions = template._minions;
	}
	
	public NpcSpawnTemplate(SpawnTemplate spawnTemplate, SpawnGroup group, StatsSet set)
	{
		_spawnTemplate = spawnTemplate;
		_group = group;
		_id = set.getInt("id");
		_count = set.getInt("count", 1);
		_respawnTime = set.getDuration("respawnTime", null);
		_respawnTimeRandom = set.getDuration("respawnRandom", null);
		_spawnAnimation = set.getBoolean("spawnAnimation", false);
		_saveInDB = set.getBoolean("dbSave", false);
		_dbName = set.getString("dbName", null);
		
		final int x = set.getInt("x", Integer.MAX_VALUE);
		final int y = set.getInt("y", Integer.MAX_VALUE);
		final int z = set.getInt("z", Integer.MAX_VALUE);
		final boolean xDefined = x != Integer.MAX_VALUE;
		final boolean yDefined = y != Integer.MAX_VALUE;
		final boolean zDefined = z != Integer.MAX_VALUE;
		if (xDefined && yDefined && zDefined)
		{
			_locations = new ArrayList<>();
			_locations.add(new ChanceLocation(x, y, z, set.getInt("heading", 0), 100));
		}
		else
		{
			if (xDefined || yDefined || zDefined)
			{
				throw new IllegalStateException(String.format("Spawn with partially declared and x: %s y: %s z: %s!", processParam(x), processParam(y), processParam(z)));
			}
			
			final String zoneName = set.getString("zone", null);
			if (zoneName != null)
			{
				final L2SpawnTerritory zone = ZoneManager.getInstance().getSpawnTerritory(zoneName);
				if (zone == null)
				{
					throw new NullPointerException("Spawn with non existing zone requested " + zoneName);
				}
				_zone = zone;
			}
		}
	}
	
	public void addSpawnLocation(ChanceLocation loc)
	{
		if (_locations == null)
		{
			_locations = new ArrayList<>();
		}
		_locations.add(loc);
	}
	
	public SpawnTemplate getSpawnTemplate()
	{
		return _spawnTemplate;
	}
	
	public SpawnGroup getGroup()
	{
		return _group;
	}
	
	private String processParam(int value)
	{
		return value != Integer.MAX_VALUE ? Integer.toString(value) : "undefined";
	}
	
	public int getId()
	{
		return _id;
	}
	
	public int getCount()
	{
		return _count;
	}
	
	public Duration getRespawnTime()
	{
		return _respawnTime;
	}
	
	public Duration getRespawnTimeRandom()
	{
		return _respawnTimeRandom;
	}
	
	public List<ChanceLocation> getLocation()
	{
		return _locations;
	}
	
	public L2SpawnTerritory getZone()
	{
		return _zone;
	}
	
	@Override
	public StatsSet getParameters()
	{
		return _parameters;
	}
	
	@Override
	public void setParameters(StatsSet parameters)
	{
		_parameters = parameters;
	}
	
	public boolean hasSpawnAnimation()
	{
		return _spawnAnimation;
	}
	
	public boolean hasDBSave()
	{
		return _saveInDB;
	}
	
	public String getDBName()
	{
		return _dbName;
	}
	
	public List<MinionHolder> getMinions()
	{
		return _minions != null ? _minions : Collections.emptyList();
	}
	
	public void addMinion(MinionHolder minion)
	{
		if (_minions == null)
		{
			_minions = new ArrayList<>();
		}
		_minions.add(minion);
	}
	
	public Set<L2Npc> getSpawnedNpcs()
	{
		return _spawnedNpcs;
	}
	
	public final Location getSpawnLocation()
	{
		if (_locations != null)
		{
			final double locRandom = (100 * Rnd.nextDouble());
			float cumulativeChance = 0;
			for (ChanceLocation loc : _locations)
			{
				cumulativeChance += loc.getChance();
				if (locRandom <= cumulativeChance)
				{
					return loc;
				}
			}
			LOGGER.warning("Couldn't match location by chance turning first..");
			return null;
		}
		else if (_zone != null)
		{
			final Location loc = _zone.getRandomPoint();
			loc.setHeading(Rnd.get(65535));
			return loc;
		}
		else if (!_group.getTerritories().isEmpty())
		{
			final L2SpawnTerritory territory = _group.getTerritories().get(Rnd.get(_group.getTerritories().size()));
			for (int i = 0; i < 100; i++)
			{
				final Location loc = territory.getRandomPoint();
				if (_group.getBannedTerritories().isEmpty() || _group.getBannedTerritories().stream().allMatch(bannedTerritory -> !bannedTerritory.isInsideZone(loc.getX(), loc.getY(), loc.getZ())))
				{
					return loc;
				}
			}
		}
		else if (!_spawnTemplate.getTerritories().isEmpty())
		{
			final L2SpawnTerritory territory = _spawnTemplate.getTerritories().get(Rnd.get(_spawnTemplate.getTerritories().size()));
			for (int i = 0; i < 100; i++)
			{
				final Location loc = territory.getRandomPoint();
				if (_spawnTemplate.getBannedTerritories().isEmpty() || _spawnTemplate.getBannedTerritories().stream().allMatch(bannedTerritory -> !bannedTerritory.isInsideZone(loc.getX(), loc.getY(), loc.getZ())))
				{
					return loc;
				}
			}
		}
		return null;
	}
	
	public void spawn()
	{
		spawn(null);
	}
	
	public void spawn(Instance instance)
	{
		try
		{
			final L2NpcTemplate npcTemplate = NpcData.getInstance().getTemplate(_id);
			if (npcTemplate != null)
			{
				if (npcTemplate.isType("L2Defender"))
				{
					LOGGER.warning("Attempting to spawn npc id: " + _id + " type: " + npcTemplate.getType() + " file: " + _spawnTemplate.getFile().getName() + " spawn: " + _spawnTemplate.getName() + " group: " + _group.getName());
					return;
				}
				
				final L2Spawn spawn = new L2Spawn(npcTemplate);
				final Location loc = getSpawnLocation();
				if (loc == null)
				{
					LOGGER.warning("Couldn't initialize new spawn, no location found!");
					return;
				}
				
				spawn.setInstanceId(instance != null ? instance.getId() : 0);
				spawn.setXYZ(loc);
				spawn.setHeading(loc.getHeading());
				spawn.setAmount(_count);
				spawn.setLocation(loc);
				int respawn = 0, respawnRandom = 0;
				if (_respawnTime != null)
				{
					respawn = (int) _respawnTime.getSeconds();
				}
				if (_respawnTimeRandom != null)
				{
					respawnRandom = (int) _respawnTimeRandom.getSeconds();
				}
				
				if (respawn > 0)
				{
					spawn.setRespawnDelay(respawn, respawnRandom);
					spawn.startRespawn();
				}
				else
				{
					spawn.stopRespawn();
				}
				
				spawn.setSpawnTemplate(this);
				
				if (_saveInDB)
				{
					if (!DBSpawnManager.getInstance().isDefined(_id))
					{
						DBSpawnManager.getInstance().addNewSpawn(spawn, true);
					}
				}
				else
				{
					for (int i = 0; i < spawn.getAmount(); i++)
					{
						final L2Npc npc = spawn.doSpawn(_spawnAnimation);
						if (npc.isMonster() && (_minions != null))
						{
							((L2MonsterInstance) npc).getMinionList().spawnMinions(_minions);
						}
						_spawnedNpcs.add(npc);
					}
					
					SpawnTable.getInstance().addNewSpawn(spawn, false);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Couldn't spawn npc " + _id, e);
		}
	}
	
	public void despawn()
	{
		_spawnedNpcs.forEach(npc ->
		{
			npc.getSpawn().stopRespawn();
			SpawnTable.getInstance().deleteSpawn(npc.getSpawn(), false);
			npc.deleteMe();
		});
		_spawnedNpcs.clear();
	}
	
	public void notifySpawnNpc(L2Npc npc)
	{
		_spawnTemplate.notifyEvent(event -> event.onSpawnNpc(_spawnTemplate, _group, npc));
	}
	
	public void notifyDespawnNpc(L2Npc npc)
	{
		_spawnTemplate.notifyEvent(event -> event.onSpawnDespawnNpc(_spawnTemplate, _group, npc));
	}
	
	public void notifyNpcDeath(L2Npc npc, L2Character killer)
	{
		_spawnTemplate.notifyEvent(event -> event.onSpawnNpcDeath(_spawnTemplate, _group, npc, killer));
	}
	
	@Override
	public NpcSpawnTemplate clone()
	{
		return new NpcSpawnTemplate(this);
	}
}