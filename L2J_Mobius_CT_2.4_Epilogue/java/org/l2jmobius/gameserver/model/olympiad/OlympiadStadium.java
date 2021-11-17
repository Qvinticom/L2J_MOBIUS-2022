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
package org.l2jmobius.gameserver.model.olympiad;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.data.xml.DoorData;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Player;

/**
 * @author GodKratos
 */
class OlympiadStadium
{
	private static final Logger _log = Logger.getLogger(OlympiadStadium.class.getName());
	
	private boolean _freeToUse = true;
	private static DoorData _doorTable;
	private final int[] _coords = new int[3];
	private final int[] _doors = new int[2];
	private final List<Player> _spectators;
	
	public boolean isFreeToUse()
	{
		return _freeToUse;
	}
	
	public void setStadiaBusy()
	{
		_freeToUse = false;
	}
	
	public void setStadiaFree()
	{
		_freeToUse = true;
	}
	
	public int[] getCoordinates()
	{
		return _coords;
	}
	
	public int[] getDoorID()
	{
		return _doors;
	}
	
	public OlympiadStadium(int x, int y, int z, int d1, int d2)
	{
		_coords[0] = x;
		_coords[1] = y;
		_coords[2] = z;
		_doors[0] = d1;
		_doors[1] = d2;
		_spectators = new CopyOnWriteArrayList<>();
	}
	
	public void openDoors()
	{
		_doorTable = DoorData.getInstance();
		try
		{
			_doorTable.getDoor(getDoorID()[0]).openMe();
			_doorTable.getDoor(getDoorID()[1]).openMe();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "", e);
		}
	}
	
	public void closeDoors()
	{
		_doorTable = DoorData.getInstance();
		try
		{
			_doorTable.getDoor(getDoorID()[0]).closeMe();
			_doorTable.getDoor(getDoorID()[1]).closeMe();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "", e);
		}
	}
	
	protected void addSpectator(int id, Player spec, boolean storeCoords)
	{
		final Location loc = new Location(getCoordinates()[0] + 1200, getCoordinates()[1], getCoordinates()[2]);
		spec.enterOlympiadObserverMode(loc, id, storeCoords);
		_spectators.add(spec);
	}
	
	protected List<Player> getSpectators()
	{
		return _spectators;
	}
	
	protected void removeSpectator(Player spec)
	{
		if ((_spectators != null) && _spectators.contains(spec))
		{
			_spectators.remove(spec);
		}
	}
}
