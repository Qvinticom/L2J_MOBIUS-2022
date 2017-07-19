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
import java.util.List;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.L2DatabaseFactory;
import com.l2jmobius.gameserver.instancemanager.ArenaManager;
import com.l2jmobius.gameserver.instancemanager.CastleManager;
import com.l2jmobius.gameserver.instancemanager.ClanHallManager;
import com.l2jmobius.gameserver.instancemanager.SiegeManager;
import com.l2jmobius.gameserver.instancemanager.TownManager;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.entity.Castle;
import com.l2jmobius.gameserver.model.entity.ClanHall;
import com.l2jmobius.gameserver.model.entity.Siege;
import com.l2jmobius.gameserver.model.zone.type.L2ArenaZone;
import com.l2jmobius.gameserver.model.zone.type.L2ClanHallZone;

/**
 * This class ...
 */
public class MapRegionTable
{
	private static Logger _log = Logger.getLogger(MapRegionTable.class.getName());
	
	private static MapRegionTable _instance;
	
	private final int[][] _regions = new int[19][21];
	
	public static enum TeleportWhereType
	{
		Castle,
		ClanHall,
		SiegeFlag,
		Town
	}
	
	public static MapRegionTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new MapRegionTable();
		}
		return _instance;
	}
	
	private MapRegionTable()
	{
		int count2 = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT region, sec0, sec1, sec2, sec3, sec4, sec5, sec6, sec7, sec8, sec9, sec10 FROM mapregion");
			ResultSet rset = statement.executeQuery())
		{
			int region;
			while (rset.next())
			{
				region = rset.getInt(1);
				
				for (int j = 0; j < 11; j++)
				{
					_regions[j][region] = rset.getInt(j + 2);
					count2++;
				}
			}
			
			if (Config.DEBUG)
			{
				_log.fine(count2 + " mapregion loaded");
			}
		}
		catch (final Exception e)
		{
			_log.warning("error while creating map region data: " + e);
		}
	}
	
	public final int getMapRegion(int posX, int posY)
	{
		return _regions[getMapRegionX(posX)][getMapRegionY(posY)];
	}
	
	public final int getMapRegionX(int posX)
	{
		return (posX >> 15) + 4;// + centerTileX;
	}
	
	public final int getMapRegionY(int posY)
	{
		return (posY >> 15) + 10;// + centerTileX;
	}
	
	public int getAreaCastle(L2Character activeChar)
	{
		final int area = getClosestTownNumber(activeChar);
		int castle;
		
		switch (area)
		{
			case 0:
				castle = 1;
				break;
			case 1:
				castle = 4;
				break;
			case 2:
				castle = 4;
				break;
			case 5:
				castle = 1;
				break;
			case 6:
				castle = 1;
				break;
			case 7:
				castle = 2;
				break;
			case 8:
				castle = 3;
				break;
			case 9:
				castle = 4;
				break;
			case 10:
				castle = 5;
				break;
			case 11:
				castle = 5;
				break;
			case 12:
				castle = 3;
				break;
			case 13:
				castle = 6;
				break;
			case 15:
				castle = 7;
				break;
			case 16:
				castle = 2;
				break;
			default:
				castle = 5;
				break;
		}
		
		return castle;
	}
	
	public int getClosestTownNumber(L2Character activeChar)
	{
		return getMapRegion(activeChar.getX(), activeChar.getY());
	}
	
	public String getClosestTownName(L2Character activeChar)
	{
		final int nearestTownId = getMapRegion(activeChar.getX(), activeChar.getY());
		String nearestTown;
		
		switch (nearestTownId)
		{
			case 0:
				nearestTown = "Talking Island Village";
				break;
			case 1:
				nearestTown = "Elven Village";
				break;
			case 2:
				nearestTown = "Dark Elven Village";
				break;
			case 3:
				nearestTown = "Orc Village";
				break;
			case 4:
				nearestTown = "Dwarven Village";
				break;
			case 5:
				nearestTown = "Gludio Castle Town";
				break;
			case 6:
				nearestTown = "Gludin Village";
				break;
			case 7:
				nearestTown = "Dion Castle Town";
				break;
			case 8:
				nearestTown = "Giran Castle Town";
				break;
			case 9:
				nearestTown = "Oren Castle Town";
				break;
			case 10:
				nearestTown = "Aden Castle Town";
				break;
			case 11:
				nearestTown = "Hunters Village";
				break;
			case 12:
				nearestTown = "Giran Harbor";
				break;
			case 13:
				nearestTown = "Innadril Castle Town";
				break;
			case 14:
				nearestTown = "Rune Castle Town";
				break;
			case 15:
				nearestTown = "Goddard Castle Town";
				break;
			case 16:
				nearestTown = "Floran Village";
				break;
			default:
				nearestTown = "Aden Castle Town";
				break;
		}
		
		return nearestTown;
	}
	
	public Location getTeleToLocation(L2Character activeChar, TeleportWhereType teleportWhere)
	{
		// int[] coord;
		
		if (activeChar instanceof L2PcInstance)
		{
			final L2PcInstance player = ((L2PcInstance) activeChar);
			
			// If in Monster Derby Track
			if (player.isInsideZone(L2Character.ZONE_MONSTERTRACK))
			{
				return new Location(12661, 181687, -3560);
			}
			
			Castle castle = null;
			ClanHall clanhall = null;
			
			if (player.getClan() != null)
			{
				// If teleport to clan hall
				if (teleportWhere == TeleportWhereType.ClanHall)
				{
					
					clanhall = ClanHallManager.getInstance().getClanHallByOwner(player.getClan());
					if (clanhall != null)
					{
						final L2ClanHallZone zone = clanhall.getZone();
						if (zone != null)
						{
							return zone.getSpawnLoc();
						}
						
					}
					
				}
				
				if (teleportWhere == TeleportWhereType.Castle)
				{
					castle = CastleManager.getInstance().getCastleByOwner(player.getClan());
					if (castle != null)
					{
						return castle.getZone().getSpawnLoc();
					}
					
					castle = CastleManager.getInstance().getCastle(player);
					if ((castle != null) && (castle.getCastleId() > 0))
					{
						// If teleport to castle
						if (castle.getSiege().getIsInProgress() && (castle.getSiege().getDefenderClan(player.getClan()) != null))
						{
							return castle.getZone().getSpawnLoc();
						}
					}
				}
				
				if (teleportWhere == TeleportWhereType.SiegeFlag)
				{
					final Siege siege = SiegeManager.getInstance().getSiege(player);
					if (siege != null)
					{
						// Check if player's clan is attacker
						final List<L2NpcInstance> flags = siege.getFlag(player.getClan());
						if ((flags != null) && !flags.isEmpty())
						{
							// Spawn to flag - Need more work to get player to the nearest flag
							final L2NpcInstance flag = flags.get(0);
							return new Location(flag.getX(), flag.getY(), flag.getZ());
						}
					}
				}
				
			}
			
			// Karma player land out of city
			if (player.getKarma() > 0)
			{
				try
				{
					return TownManager.getInstance().getClosestTown(activeChar).getChaoticSpawnLoc();
				}
				catch (final Exception e)
				{
					return new Location(17817, 170079, -3530);
				}
			}
			
			// Checking if in arena
			final L2ArenaZone arena = ArenaManager.getInstance().getArena(player);
			
			if (arena != null)
			{
				return arena.getSpawnLoc();
			}
		}
		
		// Get the nearest town
		try
		{
			
			return TownManager.getInstance().getClosestTown(activeChar).getSpawnLoc();
			
		}
		catch (final NullPointerException e)
		{
			// port to the Talking Island if no closest town found
			return new Location(-84176, 243382, -3126);
		}
	}
}