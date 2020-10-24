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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.Spawn;
import org.l2jmobius.gameserver.model.entity.Castle;
import org.l2jmobius.gameserver.model.events.AbstractScript;

/**
 * Siege Flag Of Protection Manager.
 * @author CostyKiller
 */
public class SiegeFlagOfProtectionManager
{
	private static final Logger LOGGER = Logger.getLogger(SiegeFlagOfProtectionManager.class.getName());
	
	private static final Map<Integer, Set<Spawn>> FLAG_SPAWNS = new ConcurrentHashMap<>();
	
	// Flag of Protection NPCs
	private static final int FLAG_GLUDIO = 36741; // 1 Gludio Castle
	private static final int FLAG_DION = 36742; // 2 Dion Castle
	private static final int FLAG_GIRAN = 36743; // 3 Giran Castle
	private static final int FLAG_OREN = 36744; // 4 Oren Castle
	private static final int FLAG_ADEN = 36745; // 5 Aden Castle
	private static final int FLAG_INNADRIL = 36746; // 6 Innadril Castle
	private static final int FLAG_GODDARD = 36747; // 7 Goddard Castle
	private static final int FLAG_RUNE = 36748; // 8 Rune Castle
	private static final int FLAG_SCHUTTGART = 36749; // 9 Schuttgart Castle
	
	// Flag Spawn Positions
	private static final Location[] FLAG_LOC_GLUDIO =
	{
		new Location(-18488, 111080, -2575), // Front
		new Location(-17736, 107272, -2567), // Back
		new Location(-18568, 112328, -2509) // Tower
	};
	private static final Location[] FLAG_LOC_DION =
	{
		new Location(22440, 158632, -2776), // Front
		new Location(21752, 162488, -2764), // Back
		new Location(22536, 157432, -2703) // Tower
	};
	private static final Location[] FLAG_LOC_GIRAN =
	{
		new Location(114840, 144712, -2641), // Front
		new Location(118664, 145432, -2646), // Back
		new Location(113608, 144632, -2576) // Tower
	};
	private static final Location[] FLAG_LOC_OREN =
	{
		new Location(80904, 36824, -2368), // Front
		new Location(84728, 37512, -2364), // Back
		new Location(79672, 36728, -2303) // Tower
	};
	private static final Location[] FLAG_LOC_ADEN =
	{
		new Location(147464, 7240, -472), // Front
		new Location(149608, 8296, -472), // Right
		new Location(145304, 8296, -472), // Left
		new Location(148696, 1832, -472), // Right back
		new Location(146136, 1848, -472) // Left back
	};
	private static final Location[] FLAG_LOC_INNADRIL =
	{
		new Location(116392, 247464, -864), // Front
		new Location(115704, 251272, -862), // Back
		new Location(116488, 246200, -799) // Tower
	};
	private static final Location[] FLAG_LOC_GODDARD =
	{
		new Location(147320, -46072, -2087), // Front
		new Location(148056, -46600, -2386), // Right
		new Location(146888, -46568, -2386) // Left
	};
	private static final Location[] FLAG_LOC_RUNE =
	{
		new Location(15960, -48840, -1072), // Front
		new Location(16616, -50328, -643), // Right
		new Location(16616, -47976, -642), // Left
		new Location(13768, -52120, -961), // Right back
		new Location(13720, -46184, -954) // Left back
	};
	private static final Location[] FLAG_LOC_SCHUTTGART =
	{
		new Location(77704, -150104, -355), // Front
		new Location(78168, -150680, -654), // Right
		new Location(76968, -150632, -654) // Left
	};
	
	protected SiegeFlagOfProtectionManager()
	{
	}
	
	/**
	 * Loads all flags of protection for all castles.
	 * @param castle
	 */
	public void loadFlags(Castle castle)
	{
		Spawn spawn = null;
		switch (castle.getResidenceId())
		{
			case 1:
			{
				try
				{
					spawn = new Spawn(FLAG_GLUDIO);
					spawn.setXYZ(AbstractScript.getRandomEntry(FLAG_LOC_GLUDIO));
					getSpawnedFlags(castle.getResidenceId()).add(spawn);
				}
				catch (ClassNotFoundException | NoSuchMethodException | ClassCastException e)
				{
					e.printStackTrace();
				}
				break;
			}
			case 2:
			{
				try
				{
					spawn = new Spawn(FLAG_DION);
					spawn.setXYZ(AbstractScript.getRandomEntry(FLAG_LOC_DION));
					getSpawnedFlags(castle.getResidenceId()).add(spawn);
				}
				catch (ClassNotFoundException | NoSuchMethodException | ClassCastException e)
				{
					e.printStackTrace();
				}
				break;
			}
			case 3:
			{
				try
				{
					spawn = new Spawn(FLAG_GIRAN);
					spawn.setXYZ(AbstractScript.getRandomEntry(FLAG_LOC_GIRAN));
					getSpawnedFlags(castle.getResidenceId()).add(spawn);
				}
				catch (ClassNotFoundException | NoSuchMethodException | ClassCastException e)
				{
					e.printStackTrace();
				}
				break;
			}
			case 4:
			{
				try
				{
					spawn = new Spawn(FLAG_OREN);
					spawn.setXYZ(AbstractScript.getRandomEntry(FLAG_LOC_OREN));
					getSpawnedFlags(castle.getResidenceId()).add(spawn);
				}
				catch (ClassNotFoundException | NoSuchMethodException | ClassCastException e)
				{
					e.printStackTrace();
				}
				break;
			}
			case 5:
			{
				try
				{
					spawn = new Spawn(FLAG_ADEN);
					spawn.setXYZ(AbstractScript.getRandomEntry(FLAG_LOC_ADEN));
					getSpawnedFlags(castle.getResidenceId()).add(spawn);
				}
				catch (ClassNotFoundException | NoSuchMethodException | ClassCastException e)
				{
					e.printStackTrace();
				}
				break;
			}
			case 6:
			{
				try
				{
					spawn = new Spawn(FLAG_INNADRIL);
					spawn.setXYZ(AbstractScript.getRandomEntry(FLAG_LOC_INNADRIL));
					getSpawnedFlags(castle.getResidenceId()).add(spawn);
				}
				catch (ClassNotFoundException | NoSuchMethodException | ClassCastException e)
				{
					e.printStackTrace();
				}
				break;
			}
			case 7:
			{
				try
				{
					spawn = new Spawn(FLAG_GODDARD);
					spawn.setXYZ(AbstractScript.getRandomEntry(FLAG_LOC_GODDARD));
					getSpawnedFlags(castle.getResidenceId()).add(spawn);
				}
				catch (ClassNotFoundException | NoSuchMethodException | ClassCastException e)
				{
					e.printStackTrace();
				}
				break;
			}
			case 8:
			{
				try
				{
					spawn = new Spawn(FLAG_RUNE);
					spawn.setXYZ(AbstractScript.getRandomEntry(FLAG_LOC_RUNE));
					getSpawnedFlags(castle.getResidenceId()).add(spawn);
				}
				catch (ClassNotFoundException | NoSuchMethodException | ClassCastException e)
				{
					e.printStackTrace();
				}
				break;
			}
			case 9:
			{
				try
				{
					spawn = new Spawn(FLAG_SCHUTTGART);
					spawn.setXYZ(AbstractScript.getRandomEntry(FLAG_LOC_SCHUTTGART));
					getSpawnedFlags(castle.getResidenceId()).add(spawn);
				}
				catch (ClassNotFoundException | NoSuchMethodException | ClassCastException e)
				{
					e.printStackTrace();
				}
				break;
			}
		}
	}
	
	/**
	 * Spawn flag of protection for castle.
	 * @param castle the castle instance
	 */
	public void spawnFlag(Castle castle)
	{
		try
		{
			loadFlags(castle);
			
			for (Spawn spawn : getSpawnedFlags(castle.getResidenceId()))
			{
				if (spawn != null)
				{
					spawn.doSpawn();
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Error spawning flag of protection for castle " + castle.getName(), e);
		}
	}
	
	/**
	 * Unspawn flag of protection for castle.
	 * @param castle the castle instance
	 */
	public void unspawnFlag(Castle castle)
	{
		for (Spawn spawn : getSpawnedFlags(castle.getResidenceId()))
		{
			if ((spawn != null) && (spawn.getLastSpawn() != null))
			{
				spawn.stopRespawn();
				spawn.getLastSpawn().doDie(spawn.getLastSpawn());
			}
		}
		getSpawnedFlags(castle.getResidenceId()).clear();
	}
	
	public Set<Spawn> getSpawnedFlags(int castleId)
	{
		return FLAG_SPAWNS.computeIfAbsent(castleId, key -> ConcurrentHashMap.newKeySet());
	}
	
	/**
	 * Gets the single instance of {@code SiegeFlagOfProtectionManager}.
	 * @return single instance of {@code SiegeFlagOfProtectionManager}
	 */
	public static SiegeFlagOfProtectionManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final SiegeFlagOfProtectionManager INSTANCE = new SiegeFlagOfProtectionManager();
	}
}
