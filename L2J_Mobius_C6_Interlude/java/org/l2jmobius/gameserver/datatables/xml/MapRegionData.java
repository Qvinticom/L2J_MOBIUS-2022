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
package org.l2jmobius.gameserver.datatables.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.l2jmobius.commons.util.IXmlReader;
import org.l2jmobius.gameserver.enums.TeleportWhereType;
import org.l2jmobius.gameserver.instancemanager.ArenaManager;
import org.l2jmobius.gameserver.instancemanager.CastleManager;
import org.l2jmobius.gameserver.instancemanager.ClanHallManager;
import org.l2jmobius.gameserver.instancemanager.FortManager;
import org.l2jmobius.gameserver.instancemanager.TownManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.StatSet;
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

/**
 * @author Mobius
 */
public class MapRegionData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(MapRegionData.class.getName());
	
	public static final Location FLORAN_VILLAGE_LOCATION = new Location(17817, 170079, -3530);
	public static final Location JAIL_LOCATION = new Location(-114356, -249645, -2984);
	private static final Location EXIT_MONSTER_RACE_LOCATION = new Location(12661, 181687, -3560);
	private static final List<Location> KARMA_LOCATIONS = new ArrayList<>();
	static
	{
		KARMA_LOCATIONS.add(new Location(-79077, 240355, -3440)); // Talking Island Village
		KARMA_LOCATIONS.add(new Location(43503, 40398, -3450)); // Elven Village
		KARMA_LOCATIONS.add(new Location(1675, 19581, -3110)); // Dark Elven Village
		KARMA_LOCATIONS.add(new Location(-44413, -121762, -235)); // Orc Village
		KARMA_LOCATIONS.add(new Location(12009, -187319, -3309)); // Dwarven Village
		KARMA_LOCATIONS.add(new Location(-18872, 126216, -3280)); // Town of Gludio
		KARMA_LOCATIONS.add(new Location(-85915, 150402, -3060)); // Gludin Village
		KARMA_LOCATIONS.add(new Location(23652, 144823, -3330)); // Town of Dion
		KARMA_LOCATIONS.add(new Location(79125, 154197, -3490)); // Town of Giran
		KARMA_LOCATIONS.add(new Location(73840, 58193, -2730)); // Town of Oren
		KARMA_LOCATIONS.add(new Location(44413, 22610, 235)); // Town of Aden
		KARMA_LOCATIONS.add(new Location(114137, 72993, -2445)); // Hunters Village
		KARMA_LOCATIONS.add(new Location(79125, 154197, -3490)); // Giran Harbor
		KARMA_LOCATIONS.add(new Location(119536, 218558, -3495)); // Heine
		KARMA_LOCATIONS.add(new Location(42931, -44733, -1326)); // Rune Township
		KARMA_LOCATIONS.add(new Location(147419, -64980, -3457)); // Town of Goddard
		KARMA_LOCATIONS.add(new Location(85184, -138560, -2256)); // Town of Shuttgart
		KARMA_LOCATIONS.add(new Location(17817, 170079, -3530)); // Floran Village
		KARMA_LOCATIONS.add(new Location(9927, -24138, -3723)); // Primeval Isle Wharf
	}
	private static final int[][] REGIONS = new int[19][21];
	
	protected MapRegionData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseDatapackFile("data/MapRegions.xml");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		try
		{
			int id = 0;
			final StatSet set = new StatSet();
			
			final Node n = doc.getFirstChild();
			for (Node node = n.getFirstChild(); node != null; node = node.getNextSibling())
			{
				if ("map".equalsIgnoreCase(node.getNodeName()))
				{
					final NamedNodeMap attrs = node.getAttributes();
					for (int i = 0; i < attrs.getLength(); i++)
					{
						final Node attr = attrs.item(i);
						set.set(attr.getNodeName(), attr.getNodeValue());
					}
					
					id = set.getInt("id");
					REGIONS[0][id] = set.getInt("region1");
					REGIONS[1][id] = set.getInt("region2");
					REGIONS[2][id] = set.getInt("region3");
					REGIONS[3][id] = set.getInt("region4");
					REGIONS[4][id] = set.getInt("region5");
					REGIONS[5][id] = set.getInt("region6");
					REGIONS[6][id] = set.getInt("region7");
					REGIONS[7][id] = set.getInt("region8");
					REGIONS[8][id] = set.getInt("region9");
					REGIONS[9][id] = set.getInt("region10");
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warning(getClass().getSimpleName() + ": Error while reading map region data: " + e);
		}
	}
	
	public int getMapRegion(int posX, int posY)
	{
		return REGIONS[getMapRegionX(posX)][getMapRegionY(posY)];
	}
	
	public int getMapRegionX(int posX)
	{
		// +4 to shift coords to center
		return (posX >> 15) + 4;
	}
	
	public int getMapRegionY(int posY)
	{
		// +10 to shift coords to center
		return (posY >> 15) + 10;
	}
	
	public int getAreaCastle(Creature creature)
	{
		switch (getClosestTownNumber(creature))
		{
			case 0:// Talking Island Village
			{
				return 1;
			}
			case 1: // Elven Village
			{
				return 4;
			}
			case 2: // Dark Elven Village
			{
				return 4;
			}
			case 3: // Orc Village
			{
				return 9;
			}
			case 4: // Dwarven Village
			{
				return 9;
			}
			case 5: // Town of Gludio
			{
				return 1;
			}
			case 6: // Gludin Village
			{
				return 1;
			}
			case 7: // Town of Dion
			{
				return 2;
			}
			case 8: // Town of Giran
			{
				return 3;
			}
			case 9: // Town of Oren
			{
				return 4;
			}
			case 10: // Town of Aden
			{
				return 5;
			}
			case 11: // Hunters Village
			{
				return 5;
			}
			case 12: // Giran Harbor
			{
				return 3;
			}
			case 13: // Heine
			{
				return 6;
			}
			case 14: // Rune Township
			{
				return 8;
			}
			case 15: // Town of Goddard
			{
				return 7;
			}
			case 16: // Town of Shuttgart
			{
				return 9;
			}
			case 17: // Ivory Tower
			{
				return 4;
			}
			case 18: // Primeval Isle Wharf
			{
				return 8;
			}
			default: // Town of Aden
			{
				return 5;
			}
		}
	}
	
	public int getClosestTownNumber(Creature creature)
	{
		return getMapRegion(creature.getX(), creature.getY());
	}
	
	public String getClosestTownName(Creature creature)
	{
		switch (getMapRegion(creature.getX(), creature.getY()))
		{
			case 0:
			{
				return "Talking Island Village";
			}
			case 1:
			{
				return "Elven Village";
			}
			case 2:
			{
				return "Dark Elven Village";
			}
			case 3:
			{
				return "Orc Village";
			}
			case 4:
			{
				return "Dwarven Village";
			}
			case 5:
			{
				return "Town of Gludio";
			}
			case 6:
			{
				return "Gludin Village";
			}
			case 7:
			{
				return "Town of Dion";
			}
			case 8:
			{
				return "Town of Giran";
			}
			case 9:
			{
				return "Town of Oren";
			}
			case 10:
			{
				return "Town of Aden";
			}
			case 11:
			{
				return "Hunters Village";
			}
			case 12:
			{
				return "Giran Harbor";
			}
			case 13:
			{
				return "Heine";
			}
			case 14:
			{
				return "Rune Township";
			}
			case 15:
			{
				return "Town of Goddard";
			}
			case 16:
			{
				return "Town of Shuttgart";
			}
			case 18:
			{
				return "Primeval Isle";
			}
			default:
			{
				return "Town of Aden";
			}
		}
	}
	
	public Location getTeleToLocation(Creature creature, TeleportWhereType teleportWhere)
	{
		if (creature instanceof PlayerInstance)
		{
			final PlayerInstance player = creature.getActingPlayer();
			
			// If in Monster Derby Track
			if (player.isInsideZone(ZoneId.MONSTER_TRACK))
			{
				return EXIT_MONSTER_RACE_LOCATION;
			}
			
			Castle castle = null;
			Fort fort = null;
			ClanHall clanhall = null;
			
			if (player.getClan() != null)
			{
				// If teleport to clan hall
				if (teleportWhere == TeleportWhereType.CLANHALL)
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
				if (teleportWhere == TeleportWhereType.CASTLE)
				{
					castle = CastleManager.getInstance().getCastleByOwner(player.getClan());
				}
				
				// If teleport to fort
				if (teleportWhere == TeleportWhereType.FORTRESS)
				{
					fort = FortManager.getInstance().getFortByOwner(player.getClan());
				}
				
				// Check if player is on castle or fortress ground
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
					// If Teleporting to castle or if is on caslte with siege and player's clan is defender
					if ((teleportWhere == TeleportWhereType.CASTLE) || ((teleportWhere == TeleportWhereType.CASTLE) && castle.getSiege().isInProgress() && (castle.getSiege().getDefenderClan(player.getClan()) != null)))
					{
						return castle.getZone().getSpawn();
					}
					
					if ((teleportWhere == TeleportWhereType.SIEGEFLAG) && castle.getSiege().isInProgress())
					{
						// Check if player's clan is attacker
						final List<NpcInstance> flags = castle.getSiege().getFlag(player.getClan());
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
					// Teleporting to castle or fortress is on castle with siege and player's clan is defender
					if ((teleportWhere == TeleportWhereType.FORTRESS) || ((teleportWhere == TeleportWhereType.FORTRESS) && fort.getSiege().isInProgress() && (fort.getSiege().getDefenderClan(player.getClan()) != null)))
					{
						return fort.getZone().getSpawn();
					}
					
					if ((teleportWhere == TeleportWhereType.SIEGEFLAG) && fort.getSiege().isInProgress())
					{
						// Check if player's clan is attacker
						final List<NpcInstance> flags = fort.getSiege().getFlag(player.getClan());
						if ((flags != null) && !flags.isEmpty())
						{
							// Spawn to flag
							final NpcInstance flag = flags.get(0);
							return new Location(flag.getX(), flag.getY(), flag.getZ());
						}
					}
				}
			}
			
			// Teleport red pk 5+ to Floran Village
			if ((player.getPkKills() > 5) && (player.getKarma() > 1))
			{
				return FLORAN_VILLAGE_LOCATION;
			}
			
			// Karma player land out of city
			if (player.getKarma() > 1)
			{
				final int closest = getMapRegion(creature.getX(), creature.getY());
				if ((closest >= 0) && (closest < KARMA_LOCATIONS.size()))
				{
					return KARMA_LOCATIONS.get(closest);
				}
				return FLORAN_VILLAGE_LOCATION;
			}
			
			// Checking if in arena
			final ArenaZone arena = ArenaManager.getInstance().getArena(player);
			if (arena != null)
			{
				return arena.getSpawnLoc();
			}
		}
		
		// Get the nearest town
		TownZone localZone = null;
		if ((creature != null) && ((localZone = TownManager.getInstance().getClosestTown(creature)) != null))
		{
			return localZone.getSpawnLoc();
		}
		
		localZone = TownManager.getInstance().getTown(9); // Giran
		return localZone.getSpawnLoc();
	}
	
	public static MapRegionData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final MapRegionData INSTANCE = new MapRegionData();
	}
}
