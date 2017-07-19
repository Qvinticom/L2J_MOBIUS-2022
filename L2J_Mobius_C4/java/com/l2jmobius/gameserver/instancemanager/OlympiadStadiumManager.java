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
package com.l2jmobius.gameserver.instancemanager;

import java.util.logging.Logger;

import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.zone.type.L2OlympiadStadiumZone;

import javolution.util.FastList;

public class OlympiadStadiumManager
{
	protected static Logger _log = Logger.getLogger(OlympiadStadiumManager.class.getName());
	
	// =========================================================
	private static OlympiadStadiumManager _instance;
	
	public static final OlympiadStadiumManager getInstance()
	{
		if (_instance == null)
		{
			System.out.println("Initializing OlympiadStadiumManager");
			_instance = new OlympiadStadiumManager();
			
		}
		return _instance;
	}
	
	// =========================================================
	// Data Field
	private FastList<L2OlympiadStadiumZone> _olympiadStadiums;
	
	// =========================================================
	// Constructor
	public OlympiadStadiumManager()
	{
	}
	
	// Property - Public
	public void addStadium(L2OlympiadStadiumZone arena)
	{
		if (_olympiadStadiums == null)
		{
			_olympiadStadiums = new FastList<>();
		}
		
		_olympiadStadiums.add(arena);
	}
	
	public final L2OlympiadStadiumZone getStadium(L2Character character)
	{
		for (final L2OlympiadStadiumZone temp : _olympiadStadiums)
		{
			if (temp.isCharacterInZone(character))
			{
				return temp;
			}
		}
		return null;
	}
	
	public final L2OlympiadStadiumZone getOlympiadStadiumById(int olympiadStadiumId)
	{
		for (final L2OlympiadStadiumZone temp : _olympiadStadiums)
		{
			if (temp.getStadiumId() == olympiadStadiumId)
			{
				return temp;
			}
		}
		return null;
	}
}