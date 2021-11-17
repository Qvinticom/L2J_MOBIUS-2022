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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.data.sql.NpcTable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.siege.Fort;
import org.l2jmobius.gameserver.model.spawn.Spawn;

/**
 * @author programmos, scoria dev
 */
public class FortSiegeGuardManager
{
	private static final Logger LOGGER = Logger.getLogger(FortSiegeGuardManager.class.getName());
	
	private final Fort _fort;
	private final List<Spawn> _siegeGuardSpawn = new ArrayList<>();
	
	public FortSiegeGuardManager(Fort fort)
	{
		_fort = fort;
	}
	
	/**
	 * Add guard.
	 * @param player
	 * @param npcId
	 */
	public void addSiegeGuard(Player player, int npcId)
	{
		if (player == null)
		{
			return;
		}
		
		addSiegeGuard(player.getX(), player.getY(), player.getZ(), player.getHeading(), npcId);
	}
	
	/**
	 * Add guard.
	 * @param x
	 * @param y
	 * @param z
	 * @param heading
	 * @param npcId
	 */
	public void addSiegeGuard(int x, int y, int z, int heading, int npcId)
	{
		saveSiegeGuard(x, y, z, heading, npcId, 0);
	}
	
	/**
	 * Hire merc.
	 * @param player
	 * @param npcId
	 */
	public void hireMerc(Player player, int npcId)
	{
		if (player == null)
		{
			return;
		}
		
		hireMerc(player.getX(), player.getY(), player.getZ(), player.getHeading(), npcId);
	}
	
	/**
	 * Hire merc.
	 * @param x
	 * @param y
	 * @param z
	 * @param heading
	 * @param npcId
	 */
	public void hireMerc(int x, int y, int z, int heading, int npcId)
	{
		saveSiegeGuard(x, y, z, heading, npcId, 1);
	}
	
	/**
	 * Remove a single mercenary, identified by the npcId and location. Presumably, this is used when a fort lord picks up a previously dropped ticket
	 * @param npcId
	 * @param x
	 * @param y
	 * @param z
	 */
	public void removeMerc(int npcId, int x, int y, int z)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("Delete From fort_siege_guards Where npcId = ? And x = ? AND y = ? AND z = ? AND isHired = 1");
			statement.setInt(1, npcId);
			statement.setInt(2, x);
			statement.setInt(3, y);
			statement.setInt(4, z);
			statement.execute();
			statement.close();
		}
		catch (Exception e1)
		{
			LOGGER.warning("Error deleting hired siege guard at " + x + ',' + y + ',' + z + ":" + e1);
		}
	}
	
	/**
	 * Remove mercs.
	 */
	public void removeMercs()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("Delete From fort_siege_guards Where fortId = ? And isHired = 1");
			statement.setInt(1, _fort.getFortId());
			statement.execute();
			statement.close();
		}
		catch (Exception e1)
		{
			LOGGER.warning("Error deleting hired siege guard for fort " + _fort.getName() + ":" + e1);
		}
	}
	
	/**
	 * Spawn guards.
	 */
	public void spawnSiegeGuard()
	{
		try
		{
			loadSiegeGuard();
			for (Spawn spawn : _siegeGuardSpawn)
			{
				if (spawn != null)
				{
					spawn.init();
				}
			}
		}
		catch (Throwable t)
		{
			LOGGER.warning("Error spawning siege guards for fort " + _fort.getName() + ":" + t);
		}
	}
	
	/**
	 * Unspawn guards.
	 */
	public void unspawnSiegeGuard()
	{
		for (Spawn spawn : _siegeGuardSpawn)
		{
			if (spawn == null)
			{
				continue;
			}
			
			spawn.stopRespawn();
			spawn.getLastSpawn().doDie(spawn.getLastSpawn());
		}
		
		_siegeGuardSpawn.clear();
	}
	
	/**
	 * Load guards.
	 */
	private void loadSiegeGuard()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT * FROM fort_siege_guards Where fortId = ? ");
			statement.setInt(1, _fort.getFortId());
			final ResultSet rs = statement.executeQuery();
			Spawn spawn1;
			NpcTemplate template1;
			
			while (rs.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rs.getInt("npcId"));
				if (template1 != null)
				{
					spawn1 = new Spawn(template1);
					spawn1.setId(rs.getInt("id"));
					spawn1.setAmount(1);
					spawn1.setX(rs.getInt("x"));
					spawn1.setY(rs.getInt("y"));
					spawn1.setZ(rs.getInt("z"));
					spawn1.setHeading(rs.getInt("heading"));
					spawn1.setRespawnDelay(rs.getInt("respawnDelay"));
					spawn1.setLocation(0);
					_siegeGuardSpawn.add(spawn1);
				}
				else
				{
					LOGGER.warning("Missing npc data in npc table for id: " + rs.getInt("npcId"));
				}
			}
			rs.close();
			statement.close();
		}
		catch (Exception e1)
		{
			LOGGER.warning("Error loading siege guard for fort " + _fort.getName() + ":" + e1);
		}
	}
	
	/**
	 * Save guards.
	 * @param x
	 * @param y
	 * @param z
	 * @param heading
	 * @param npcId
	 * @param isHire
	 */
	private void saveSiegeGuard(int x, int y, int z, int heading, int npcId, int isHire)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("Insert Into fort_siege_guards (fortId, npcId, x, y, z, heading, respawnDelay, isHired) Values (?, ?, ?, ?, ?, ?, ?, ?)");
			statement.setInt(1, _fort.getFortId());
			statement.setInt(2, npcId);
			statement.setInt(3, x);
			statement.setInt(4, y);
			statement.setInt(5, z);
			statement.setInt(6, heading);
			if (isHire == 1)
			{
				statement.setInt(7, 0);
			}
			else
			{
				statement.setInt(7, 600);
			}
			statement.setInt(8, isHire);
			statement.execute();
			statement.close();
		}
		catch (Exception e1)
		{
			LOGGER.warning("Error adding siege guard for fort " + _fort.getName() + ":" + e1);
		}
	}
	
	public Fort getFort()
	{
		return _fort;
	}
	
	public List<Spawn> getSiegeGuardSpawn()
	{
		return _siegeGuardSpawn;
	}
}
