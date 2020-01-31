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

import org.l2jmobius.gameserver.datatables.xml.MapRegionData;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.entity.siege.Castle;
import org.l2jmobius.gameserver.model.zone.type.TownZone;

public class TownManager
{
	private List<TownZone> _towns;
	
	private TownManager()
	{
	}
	
	public void addTown(TownZone arena)
	{
		if (_towns == null)
		{
			_towns = new ArrayList<>();
		}
		
		_towns.add(arena);
	}
	
	public TownZone getClosestTown(WorldObject activeObject)
	{
		switch (MapRegionData.getInstance().getMapRegion(activeObject.getPosition().getX(), activeObject.getPosition().getY()))
		{
			case 0:
			{
				return getTown(2); // TI
			}
			case 1:
			{
				return getTown(3); // Elven
			}
			case 2:
			{
				return getTown(1); // DE
			}
			case 3:
			{
				return getTown(4); // Orc
			}
			case 4:
			{
				return getTown(6); // Dwarven
			}
			case 5:
			{
				return getTown(7); // Gludio
			}
			case 6:
			{
				return getTown(5); // Gludin
			}
			case 7:
			{
				return getTown(8); // Dion
			}
			case 8:
			{
				return getTown(9); // Giran
			}
			case 9:
			{
				return getTown(10); // Oren
			}
			case 10:
			{
				return getTown(12); // Aden
			}
			case 11:
			{
				return getTown(11); // HV
			}
			case 12:
			{
				return getTown(9); // Giran Harbour
			}
			case 13:
			{
				return getTown(15); // Heine
			}
			case 14:
			{
				return getTown(14); // Rune
			}
			case 15:
			{
				return getTown(13); // Goddard
			}
			case 16:
			{
				return getTown(17); // Schuttgart
			}
			case 17:
			{
				return getTown(16); // Floran
			}
			case 18:
			{
				return getTown(19); // Primeval Isle
			}
		}
		
		return getTown(16); // Default to floran
	}
	
	public static final int getClosestLocation(WorldObject activeObject)
	{
		switch (MapRegionData.getInstance().getMapRegion(activeObject.getPosition().getX(), activeObject.getPosition().getY()))
		{
			case 0:
			{
				return 1; // TI
			}
			case 1:
			{
				return 4; // Elven
			}
			case 2:
			{
				return 3; // DE
			}
			case 3:
			{
				return 9; // Orc
			}
			case 4:
			{
				return 9; // Dwarven
			}
			case 5:
			{
				return 2; // Gludio
			}
			case 6:
			{
				return 2; // Gludin
			}
			case 7:
			{
				return 5; // Dion
			}
			case 8:
			{
				return 6; // Giran
			}
			case 9:
			{
				return 10; // Oren
			}
			case 10:
			{
				return 13; // Aden
			}
			case 11:
			{
				return 11; // HV
			}
			case 12:
			{
				return 6; // Giran Harbour
			}
			case 13:
			{
				return 12; // Heine
			}
			case 14:
			{
				return 14; // Rune
			}
			case 15:
			{
				return 15; // Goddard
			}
			case 16:
			{
				return 9; // Schuttgart
			}
		}
		return 0;
	}
	
	public boolean townHasCastleInSiege(int townId)
	{
		final int[] castleidarray =
		{
			0,
			0,
			0,
			0,
			0,
			0,
			0,
			1,
			2,
			3,
			4,
			0,
			5,
			7,
			8,
			6,
			0,
			9,
			0
		};
		final int castleIndex = castleidarray[townId];
		
		if (castleIndex > 0)
		{
			final Castle castle = CastleManager.getInstance().getCastles().get(CastleManager.getInstance().getCastleIndex(castleIndex));
			if (castle != null)
			{
				return castle.getSiege().isInProgress();
			}
		}
		return false;
	}
	
	public boolean townHasCastleInSiege(int x, int y)
	{
		final int curtown = MapRegionData.getInstance().getMapRegion(x, y);
		final int[] castleidarray =
		{
			0,
			0,
			0,
			0,
			0,
			1,
			0,
			2,
			3,
			4,
			5,
			0,
			0,
			6,
			8,
			7,
			9,
			0,
			0
		};
		// find an instance of the castle for this town.
		final int castleIndex = castleidarray[curtown];
		if (castleIndex > 0)
		{
			final Castle castle = CastleManager.getInstance().getCastles().get(CastleManager.getInstance().getCastleIndex(castleIndex));
			if (castle != null)
			{
				return castle.getSiege().isInProgress();
			}
		}
		return false;
	}
	
	public TownZone getTown(int townId)
	{
		for (TownZone temp : _towns)
		{
			if (temp.getTownId() == townId)
			{
				return temp;
			}
		}
		
		return null;
	}
	
	/**
	 * Returns the town at that position (if any)
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public TownZone getTown(int x, int y, int z)
	{
		for (TownZone temp : _towns)
		{
			if (temp.isInsideZone(x, y, z))
			{
				return temp;
			}
		}
		
		return null;
	}
	
	public static TownManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final TownManager INSTANCE = new TownManager();
	}
}
