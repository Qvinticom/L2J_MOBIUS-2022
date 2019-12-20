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
package org.l2jmobius.gameserver.datatables.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.instancemanager.ArenaManager;
import org.l2jmobius.gameserver.instancemanager.CastleManager;
import org.l2jmobius.gameserver.instancemanager.ClanHallManager;
import org.l2jmobius.gameserver.instancemanager.FortManager;
import org.l2jmobius.gameserver.instancemanager.TownManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.entity.ClanHall;
import org.l2jmobius.gameserver.model.entity.siege.Castle;
import org.l2jmobius.gameserver.model.entity.siege.Fort;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.model.zone.type.ArenaZone;
import org.l2jmobius.gameserver.model.zone.type.ClanHallZone;
import org.l2jmobius.gameserver.model.zone.type.TownZone;

public class MapRegionTable
{
	private static final Logger LOGGER = Logger.getLogger(MapRegionTable.class.getName());
	
	private final int[][] _regions = new int[19][21];
	private final int[][] _pointsWithKarmas;
	
	public enum TeleportWhereType
	{
		Castle,
		ClanHall,
		SiegeFlag,
		Town,
		Fortress
	}
	
	private MapRegionTable()
	{
		FileReader reader = null;
		BufferedReader buff = null;
		LineNumberReader lnr = null;
		
		try
		{
			final File fileData = new File(Config.DATAPACK_ROOT + "/data/csv/mapregion.csv");
			
			reader = new FileReader(fileData);
			buff = new BufferedReader(reader);
			lnr = new LineNumberReader(buff);
			
			String line = null;
			
			int region;
			
			while ((line = lnr.readLine()) != null)
			{
				// ignore comments
				if ((line.trim().length() == 0) || line.startsWith("#"))
				{
					continue;
				}
				
				final StringTokenizer st = new StringTokenizer(line, ";");
				
				region = Integer.parseInt(st.nextToken());
				
				for (int j = 0; j < 10; j++)
				{
					_regions[j][region] = Integer.parseInt(st.nextToken());
				}
			}
		}
		catch (FileNotFoundException e)
		{
			LOGGER.warning("mapregion.csv is missing in data folder");
		}
		catch (NoSuchElementException e1)
		{
			LOGGER.warning("Error for structure CSV file: ");
		}
		catch (IOException e0)
		{
			LOGGER.warning("Error while creating table: " + e0);
		}
		finally
		{
			if (lnr != null)
			{
				try
				{
					lnr.close();
				}
				catch (Exception e1)
				{
					LOGGER.warning("Problem with MapRegionTable: " + e1.getMessage());
				}
			}
			
			if (buff != null)
			{
				try
				{
					buff.close();
				}
				catch (Exception e1)
				{
					LOGGER.warning("Problem with MapRegionTable: " + e1.getMessage());
				}
			}
			
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (Exception e1)
				{
					LOGGER.warning("Problem with MapRegionTable: " + e1.getMessage());
				}
			}
		}
		
		_pointsWithKarmas = new int[19][3];
		// Talking Island
		_pointsWithKarmas[0][0] = -79077;
		_pointsWithKarmas[0][1] = 240355;
		_pointsWithKarmas[0][2] = -3440;
		// Elven
		_pointsWithKarmas[1][0] = 43503;
		_pointsWithKarmas[1][1] = 40398;
		_pointsWithKarmas[1][2] = -3450;
		// DarkElven
		_pointsWithKarmas[2][0] = 1675;
		_pointsWithKarmas[2][1] = 19581;
		_pointsWithKarmas[2][2] = -3110;
		// Orc
		_pointsWithKarmas[3][0] = -44413;
		_pointsWithKarmas[3][1] = -121762;
		_pointsWithKarmas[3][2] = -235;
		// Dwalf
		_pointsWithKarmas[4][0] = 12009;
		_pointsWithKarmas[4][1] = -187319;
		_pointsWithKarmas[4][2] = -3309;
		// Gludio
		_pointsWithKarmas[5][0] = -18872;
		_pointsWithKarmas[5][1] = 126216;
		_pointsWithKarmas[5][2] = -3280;
		// Gludin
		_pointsWithKarmas[6][0] = -85915;
		_pointsWithKarmas[6][1] = 150402;
		_pointsWithKarmas[6][2] = -3060;
		// Dion
		_pointsWithKarmas[7][0] = 23652;
		_pointsWithKarmas[7][1] = 144823;
		_pointsWithKarmas[7][2] = -3330;
		// Giran
		_pointsWithKarmas[8][0] = 79125;
		_pointsWithKarmas[8][1] = 154197;
		_pointsWithKarmas[8][2] = -3490;
		// Oren
		_pointsWithKarmas[9][0] = 73840;
		_pointsWithKarmas[9][1] = 58193;
		_pointsWithKarmas[9][2] = -2730;
		// Aden
		_pointsWithKarmas[10][0] = 44413;
		_pointsWithKarmas[10][1] = 22610;
		_pointsWithKarmas[10][2] = 235;
		// Hunters
		_pointsWithKarmas[11][0] = 114137;
		_pointsWithKarmas[11][1] = 72993;
		_pointsWithKarmas[11][2] = -2445;
		// Giran
		_pointsWithKarmas[12][0] = 79125;
		_pointsWithKarmas[12][1] = 154197;
		_pointsWithKarmas[12][2] = -3490;
		// heine
		_pointsWithKarmas[13][0] = 119536;
		_pointsWithKarmas[13][1] = 218558;
		_pointsWithKarmas[13][2] = -3495;
		// Rune Castle Town
		_pointsWithKarmas[14][0] = 42931;
		_pointsWithKarmas[14][1] = -44733;
		_pointsWithKarmas[14][2] = -1326;
		// Goddard
		_pointsWithKarmas[15][0] = 147419;
		_pointsWithKarmas[15][1] = -64980;
		_pointsWithKarmas[15][2] = -3457;
		// Schuttgart
		_pointsWithKarmas[16][0] = 85184;
		_pointsWithKarmas[16][1] = -138560;
		_pointsWithKarmas[16][2] = -2256;
		// TODO Primeval Isle
		_pointsWithKarmas[18][0] = 10468;
		_pointsWithKarmas[18][1] = -24569;
		_pointsWithKarmas[18][2] = -3645;
	}
	
	public int getMapRegion(int posX, int posY)
	{
		return _regions[getMapRegionX(posX)][getMapRegionY(posY)];
	}
	
	public int getMapRegionX(int posX)
	{
		return (posX >> 15) + 4; // + centerTileX;
	}
	
	public int getMapRegionY(int posY)
	{
		return (posY >> 15) + 10; // + centerTileX;
	}
	
	public int getAreaCastle(Creature creature)
	{
		final int area = getClosestTownNumber(creature);
		int castle;
		
		switch (area)
		{
			case 0:
			{
				castle = 1;
				break; // Talking Island Village
			}
			case 1:
			{
				castle = 4;
				break; // Elven Village
			}
			case 2:
			{
				castle = 4;
				break; // Dark Elven Village
			}
			case 3:
			{
				castle = 9;
				break; // Orc Village
			}
			case 4:
			{
				castle = 9;
				break; // Dwarven Village
			}
			case 5:
			{
				castle = 1;
				break; // Town of Gludio
			}
			case 6:
			{
				castle = 1;
				break; // Gludin Village
			}
			case 7:
			{
				castle = 2;
				break; // Town of Dion
			}
			case 8:
			{
				castle = 3;
				break; // Town of Giran
			}
			case 9:
			{
				castle = 4;
				break; // Town of Oren
			}
			case 10:
			{
				castle = 5;
				break; // Town of Aden
			}
			case 11:
			{
				castle = 5;
				break; // Hunters Village
			}
			case 12:
			{
				castle = 3;
				break; // Giran Harbor
			}
			case 13:
			{
				castle = 6;
				break; // Heine
			}
			case 14:
			{
				castle = 8;
				break; // Rune Township
			}
			case 15:
			{
				castle = 7;
				break; // Town of Goddard
			}
			case 16:
			{
				castle = 9;
				break; // Town of Shuttgart
			}
			case 17:
			{
				castle = 4;
				break; // Ivory Tower
			}
			case 18:
			{
				castle = 8;
				break; // Primeval Isle Wharf
			}
			default:
			{
				castle = 5;
				break; // Town of Aden
			}
		}
		return castle;
	}
	
	public int getClosestTownNumber(Creature creature)
	{
		return getMapRegion(creature.getX(), creature.getY());
	}
	
	public String getClosestTownName(Creature creature)
	{
		final int nearestTownId = getMapRegion(creature.getX(), creature.getY());
		String nearestTown;
		
		switch (nearestTownId)
		{
			case 0:
			{
				nearestTown = "Talking Island Village";
				break;
			}
			case 1:
			{
				nearestTown = "Elven Village";
				break;
			}
			case 2:
			{
				nearestTown = "Dark Elven Village";
				break;
			}
			case 3:
			{
				nearestTown = "Orc Village";
				break;
			}
			case 4:
			{
				nearestTown = "Dwarven Village";
				break;
			}
			case 5:
			{
				nearestTown = "Town of Gludio";
				break;
			}
			case 6:
			{
				nearestTown = "Gludin Village";
				break;
			}
			case 7:
			{
				nearestTown = "Town of Dion";
				break;
			}
			case 8:
			{
				nearestTown = "Town of Giran";
				break;
			}
			case 9:
			{
				nearestTown = "Town of Oren";
				break;
			}
			case 10:
			{
				nearestTown = "Town of Aden";
				break;
			}
			case 11:
			{
				nearestTown = "Hunters Village";
				break;
			}
			case 12:
			{
				nearestTown = "Giran Harbor";
				break;
			}
			case 13:
			{
				nearestTown = "Heine";
				break;
			}
			case 14:
			{
				nearestTown = "Rune Township";
				break;
			}
			case 15:
			{
				nearestTown = "Town of Goddard";
				break;
			}
			case 16:
			{
				nearestTown = "Town of Shuttgart";
				break;
			}
			case 18:
			{
				nearestTown = "Primeval Isle";
				break;
			}
			default:
			{
				nearestTown = "Town of Aden";
				break;
			}
		}
		
		return nearestTown;
	}
	
	public Location getTeleToLocation(Creature creature, TeleportWhereType teleportWhere)
	{
		int[] coord;
		
		if (creature instanceof PlayerInstance)
		{
			final PlayerInstance player = (PlayerInstance) creature;
			
			// If in Monster Derby Track
			if (player.isInsideZone(ZoneId.MONSTER_TRACK))
			{
				return new Location(12661, 181687, -3560);
			}
			
			Castle castle = null;
			Fort fort = null;
			ClanHall clanhall = null;
			
			if (player.getClan() != null)
			{
				// If teleport to clan hall
				if (teleportWhere == TeleportWhereType.ClanHall)
				{
					clanhall = ClanHallManager.getInstance().getClanHallByOwner(player.getClan());
					if (clanhall != null)
					{
						final ClanHallZone zone = clanhall.getZone();
						if (zone != null)
						{
							return zone.getSpawn();
						}
					}
				}
				
				// If teleport to castle
				if (teleportWhere == TeleportWhereType.Castle)
				{
					castle = CastleManager.getInstance().getCastleByOwner(player.getClan());
				}
				
				// If teleport to fort
				if (teleportWhere == TeleportWhereType.Fortress)
				{
					fort = FortManager.getInstance().getFortByOwner(player.getClan());
				}
				
				// Check if player is on castle&fortress ground
				if (castle == null)
				{
					castle = CastleManager.getInstance().getCastle(player);
				}
				
				if (fort == null)
				{
					fort = FortManager.getInstance().getFort(player);
				}
				
				if ((castle != null) && (castle.getCastleId() > 0))
				{
					// If Teleporting to castle or
					// If is on caslte with siege and player's clan is defender
					if ((teleportWhere == TeleportWhereType.Castle) || ((teleportWhere == TeleportWhereType.Castle) && castle.getSiege().getIsInProgress() && (castle.getSiege().getDefenderClan(player.getClan()) != null)))
					{
						coord = castle.getZone().getSpawn();
						return new Location(coord[0], coord[1], coord[2]);
					}
					
					if ((teleportWhere == TeleportWhereType.SiegeFlag) && castle.getSiege().getIsInProgress())
					{
						// Check if player's clan is attacker
						List<NpcInstance> flags = castle.getSiege().getFlag(player.getClan());
						if ((flags != null) && !flags.isEmpty())
						{
							// Spawn to flag - Need more work to get player to the nearest flag
							final NpcInstance flag = flags.get(0);
							return new Location(flag.getX(), flag.getY(), flag.getZ());
						}
					}
				}
				
				else if ((fort != null) && (fort.getFortId() > 0))
				{
					// teleporting to castle or fortress
					// is on caslte with siege and player's clan is defender
					if ((teleportWhere == TeleportWhereType.Fortress) || ((teleportWhere == TeleportWhereType.Fortress) && fort.getSiege().getIsInProgress() && (fort.getSiege().getDefenderClan(player.getClan()) != null)))
					{
						coord = fort.getZone().getSpawn();
						return new Location(coord[0], coord[1], coord[2]);
					}
					
					if ((teleportWhere == TeleportWhereType.SiegeFlag) && fort.getSiege().getIsInProgress())
					{
						// check if player's clan is attacker
						List<NpcInstance> flags = fort.getSiege().getFlag(player.getClan());
						
						if ((flags != null) && !flags.isEmpty())
						{
							// spawn to flag
							final NpcInstance flag = flags.get(0);
							return new Location(flag.getX(), flag.getY(), flag.getZ());
						}
					}
				}
			}
			
			// teleport RED PK 5+ to Floran Village
			if ((player.getPkKills() > 5) && (player.getKarma() > 1))
			{
				return new Location(17817, 170079, -3530);
			}
			
			// Karma player land out of city
			if (player.getKarma() > 1)
			{
				final int closest = getMapRegion(creature.getX(), creature.getY());
				
				if ((closest >= 0) && (closest < _pointsWithKarmas.length))
				{
					return new Location(_pointsWithKarmas[closest][0], _pointsWithKarmas[closest][1], _pointsWithKarmas[closest][2]);
				}
				return new Location(17817, 170079, -3530);
			}
			
			// Checking if in arena
			final ArenaZone arena = ArenaManager.getInstance().getArena(player);
			if (arena != null)
			{
				coord = arena.getSpawnLoc();
				return new Location(coord[0], coord[1], coord[2]);
			}
		}
		
		// Get the nearest town
		TownZone localZone = null;
		if ((creature != null) && ((localZone = TownManager.getInstance().getClosestTown(creature)) != null))
		{
			coord = localZone.getSpawnLoc();
			return new Location(coord[0], coord[1], coord[2]);
		}
		
		localZone = TownManager.getInstance().getTown(9); // giran
		coord = localZone.getSpawnLoc();
		return new Location(coord[0], coord[1], coord[2]);
	}
	
	public static MapRegionTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final MapRegionTable INSTANCE = new MapRegionTable();
	}
}
