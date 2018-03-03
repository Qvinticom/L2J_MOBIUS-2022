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
package com.l2jmobius.gameserver.model.entity.olympiad;

import java.util.ArrayList;
import java.util.List;

import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author GodKratos
 */
class OlympiadStadium
{
	private boolean _freeToUse = true;
	private final int[] _coords = new int[3];
	private final List<L2PcInstance> _spectators;
	
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
		clearSpectators();
	}
	
	public int[] getCoordinates()
	{
		return _coords;
	}
	
	public OlympiadStadium(int x, int y, int z)
	{
		_coords[0] = x;
		_coords[1] = y;
		_coords[2] = z;
		_spectators = new ArrayList<>();
	}
	
	protected void addSpectator(int id, L2PcInstance spec, boolean storeCoords)
	{
		spec.enterOlympiadObserverMode(getCoordinates()[0], getCoordinates()[1], getCoordinates()[2], id, storeCoords);
		_spectators.add(spec);
	}
	
	protected List<L2PcInstance> getSpectators()
	{
		return _spectators;
	}
	
	protected void removeSpectator(L2PcInstance spec)
	{
		if ((_spectators != null) && _spectators.contains(spec))
		{
			_spectators.remove(spec);
		}
	}
	
	private void clearSpectators()
	{
		_spectators.clear();
	}
}
