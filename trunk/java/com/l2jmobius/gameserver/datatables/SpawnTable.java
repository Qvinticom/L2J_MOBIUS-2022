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
package com.l2jmobius.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.commons.database.DatabaseFactory;
import com.l2jmobius.gameserver.data.xml.impl.NpcData;
import com.l2jmobius.gameserver.model.L2Spawn;
import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.actor.templates.L2NpcTemplate;

/**
 * Spawn data retriever.
 * @author Zoey76
 */
public final class SpawnTable
{
	private static final Logger LOGGER = Logger.getLogger(SpawnTable.class.getName());
	// SQL
	private static final String SELECT_CUSTOM_SPAWNS = "SELECT count, npc_templateid, locx, locy, locz, heading, respawn_delay, respawn_random, loc_id, periodOfDay FROM custom_spawnlist";
	private static final String INSERT_CUSTOM_SPAWN = "INSERT INTO custom_spawnlist (count,npc_templateid,locx,locy,locz,heading,respawn_delay,respawn_random,loc_id) values(?,?,?,?,?,?,?,?,?)";
	private static final String DELETE_CUSTOM_SPAWN = "DELETE FROM custom_spawnlist WHERE locx=? AND locy=? AND locz=? AND npc_templateid=? AND heading=?";
	private static final Map<Integer, Set<L2Spawn>> _spawnTable = new ConcurrentHashMap<>();
	
	/**
	 * Wrapper to load all spawns.
	 */
	public void load()
	{
		if (!Config.ALT_DEV_NO_SPAWNS && Config.CUSTOM_SPAWNLIST_TABLE)
		{
			fillSpawnTable();
			LOGGER.info(getClass().getSimpleName() + ": Loaded " + _spawnTable.size() + " custom npc spawns.");
		}
	}
	
	private boolean checkTemplate(int npcId)
	{
		final L2NpcTemplate npcTemplate = NpcData.getInstance().getTemplate(npcId);
		if (npcTemplate == null)
		{
			LOGGER.warning(getClass().getSimpleName() + ": Data missing in NPC table for ID: " + npcId + ".");
			return false;
		}
		
		if (npcTemplate.isType("L2SiegeGuard") || npcTemplate.isType("L2RaidBoss"))
		{
			// Don't spawn
			return false;
		}
		
		return true;
	}
	
	/**
	 * Retrieves spawn data from database.
	 * @return the spawn count
	 */
	private int fillSpawnTable()
	{
		int npcSpawnCount = 0;
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery(SELECT_CUSTOM_SPAWNS))
		{
			while (rs.next())
			{
				final StatsSet spawnInfo = new StatsSet();
				final int npcId = rs.getInt("npc_templateid");
				
				// Check basic requirements first
				if (!checkTemplate(npcId))
				{
					// Don't spawn
					continue;
				}
				
				spawnInfo.set("npcTemplateid", npcId);
				spawnInfo.set("count", rs.getInt("count"));
				spawnInfo.set("x", rs.getInt("locx"));
				spawnInfo.set("y", rs.getInt("locy"));
				spawnInfo.set("z", rs.getInt("locz"));
				spawnInfo.set("heading", rs.getInt("heading"));
				spawnInfo.set("respawnDelay", rs.getInt("respawn_delay"));
				spawnInfo.set("respawnRandom", rs.getInt("respawn_random"));
				spawnInfo.set("locId", rs.getInt("loc_id"));
				spawnInfo.set("periodOfDay", rs.getInt("periodOfDay"));
				npcSpawnCount += addSpawn(spawnInfo);
			}
		}
		catch (Exception e)
		{
			LOGGER.warning(getClass().getSimpleName() + ": Spawn could not be initialized: " + e);
		}
		return npcSpawnCount;
	}
	
	/**
	 * Creates NPC spawn
	 * @param spawnInfo StatsSet of spawn parameters
	 * @param AIData Map of specific AI parameters for this spawn
	 * @return count NPC instances, spawned by this spawn
	 */
	private int addSpawn(StatsSet spawnInfo, Map<String, Integer> AIData)
	{
		L2Spawn spawnDat;
		int ret = 0;
		try
		{
			spawnDat = new L2Spawn(spawnInfo.getInt("npcTemplateid"));
			spawnDat.setAmount(spawnInfo.getInt("count", 1));
			spawnDat.setX(spawnInfo.getInt("x", 0));
			spawnDat.setY(spawnInfo.getInt("y", 0));
			spawnDat.setZ(spawnInfo.getInt("z", 0));
			spawnDat.setHeading(spawnInfo.getInt("heading", -1));
			spawnDat.setRespawnDelay(spawnInfo.getInt("respawnDelay", 0), spawnInfo.getInt("respawnRandom", 0));
			spawnDat.setLocationId(spawnInfo.getInt("locId", 0));
			final String spawnName = spawnInfo.getString("spawnName", "");
			if (!spawnName.isEmpty())
			{
				spawnDat.setName(spawnName);
			}
			addSpawn(spawnDat);
			
			ret += spawnDat.init();
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			LOGGER.warning(getClass().getSimpleName() + ": Spawn could not be initialized: " + e);
		}
		
		return ret;
	}
	
	/**
	 * Wrapper for {@link #addSpawn(StatsSet, Map)}.
	 * @param spawnInfo StatsSet of spawn parameters
	 * @return count NPC instances, spawned by this spawn
	 */
	private int addSpawn(StatsSet spawnInfo)
	{
		return addSpawn(spawnInfo, null);
	}
	
	/**
	 * Gets the spawn data.
	 * @return the spawn data
	 */
	public Map<Integer, Set<L2Spawn>> getSpawnTable()
	{
		return _spawnTable;
	}
	
	/**
	 * Gets the spawns for the NPC Id.
	 * @param npcId the NPC Id
	 * @return the spawn set for the given npcId
	 */
	public Set<L2Spawn> getSpawns(int npcId)
	{
		return _spawnTable.getOrDefault(npcId, Collections.emptySet());
	}
	
	/**
	 * Gets the spawn count for the given NPC ID.
	 * @param npcId the NPC Id
	 * @return the spawn count
	 */
	public int getSpawnCount(int npcId)
	{
		return getSpawns(npcId).size();
	}
	
	/**
	 * Gets a spawn for the given NPC ID.
	 * @param npcId the NPC Id
	 * @return a spawn for the given NPC ID or {@code null}
	 */
	public L2Spawn getAnySpawn(int npcId)
	{
		return getSpawns(npcId).stream().findFirst().orElse(null);
	}
	
	/**
	 * Adds a new spawn to the spawn table.
	 * @param spawn the spawn to add
	 * @param storeInDb if {@code true} it'll be saved in the database
	 */
	public void addNewSpawn(L2Spawn spawn, boolean storeInDb)
	{
		addSpawn(spawn);
		
		if (storeInDb)
		{
			try (Connection con = DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(INSERT_CUSTOM_SPAWN))
			{
				ps.setInt(1, spawn.getAmount());
				ps.setInt(2, spawn.getId());
				ps.setInt(3, spawn.getX());
				ps.setInt(4, spawn.getY());
				ps.setInt(5, spawn.getZ());
				ps.setInt(6, spawn.getHeading());
				ps.setInt(7, spawn.getRespawnDelay() / 1000);
				ps.setInt(8, spawn.getRespawnMaxDelay() - spawn.getRespawnMinDelay());
				ps.setInt(9, spawn.getLocationId());
				ps.execute();
			}
			catch (Exception e)
			{
				LOGGER.warning(getClass().getSimpleName() + ": Could not store spawn in the DB: " + e);
			}
		}
	}
	
	/**
	 * Delete an spawn from the spawn table.
	 * @param spawn the spawn to delete
	 * @param updateDb if {@code true} database will be updated
	 */
	public void deleteSpawn(L2Spawn spawn, boolean updateDb)
	{
		if (!removeSpawn(spawn))
		{
			return;
		}
		
		if (updateDb)
		{
			try (Connection con = DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(DELETE_CUSTOM_SPAWN))
			{
				ps.setInt(1, spawn.getX());
				ps.setInt(2, spawn.getY());
				ps.setInt(3, spawn.getZ());
				ps.setInt(4, spawn.getId());
				ps.setInt(5, spawn.getHeading());
				ps.execute();
			}
			catch (Exception e)
			{
				LOGGER.warning(getClass().getSimpleName() + ": Spawn " + spawn + " could not be removed from DB: " + e);
			}
		}
	}
	
	/**
	 * Add a spawn to the spawn set if present, otherwise add a spawn set and add the spawn to the newly created spawn set.
	 * @param spawn the NPC spawn to add
	 */
	private void addSpawn(L2Spawn spawn)
	{
		_spawnTable.computeIfAbsent(spawn.getId(), k -> ConcurrentHashMap.newKeySet(1)).add(spawn);
	}
	
	/**
	 * Remove a spawn from the spawn set, if the spawn set is empty, remove it as well.
	 * @param spawn the NPC spawn to remove
	 * @return {@code true} if the spawn was successfully removed, {@code false} otherwise
	 */
	private boolean removeSpawn(L2Spawn spawn)
	{
		final Set<L2Spawn> set = _spawnTable.get(spawn.getId());
		if (set != null)
		{
			final boolean removed = set.remove(spawn);
			if (set.isEmpty())
			{
				_spawnTable.remove(spawn.getId());
			}
			set.forEach(this::notifyRemoved);
			return removed;
		}
		notifyRemoved(spawn);
		return false;
	}
	
	private void notifyRemoved(L2Spawn spawn)
	{
		if ((spawn != null) && (spawn.getLastSpawn() != null) && (spawn.getNpcSpawnTemplate() != null))
		{
			spawn.getNpcSpawnTemplate().notifyDespawnNpc(spawn.getLastSpawn());
		}
	}
	
	/**
	 * Execute a procedure over all spawns.<br>
	 * <font size="4" color="red">Do not use it!</font>
	 * @param function the function to execute
	 * @return {@code true} if all procedures were executed, {@code false} otherwise
	 */
	public boolean forEachSpawn(Function<L2Spawn, Boolean> function)
	{
		for (Set<L2Spawn> set : _spawnTable.values())
		{
			for (L2Spawn spawn : set)
			{
				if (!function.apply(spawn))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	public static SpawnTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SpawnTable _instance = new SpawnTable();
	}
}
