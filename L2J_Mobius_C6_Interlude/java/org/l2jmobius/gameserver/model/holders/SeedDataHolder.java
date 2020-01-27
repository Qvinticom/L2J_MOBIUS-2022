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
package org.l2jmobius.gameserver.model.holders;

import org.l2jmobius.Config;

/**
 * @author Mobius
 */
public class SeedDataHolder
{
	private final int _id;
	private final int _level; // seed level
	private final int _crop; // crop type
	private final int _mature; // mature crop type
	private final int _type1;
	private final int _type2;
	private final int _manorId; // id of manor (castle id) where seed can be farmed
	private final boolean _isAlternative;
	private final int _limitSeeds;
	private final int _limitCrops;
	
	public SeedDataHolder(int id, int level, int crop, int mature, int type1, int type2, int manorId, boolean isAlternative, int limitSeeds, int limitCrops)
	{
		_id = id;
		_level = level;
		_crop = crop;
		_mature = mature;
		_type1 = type1;
		_type2 = type2;
		_manorId = manorId;
		_isAlternative = isAlternative;
		_limitSeeds = limitSeeds;
		_limitCrops = limitCrops;
	}
	
	public int getManorId()
	{
		return _manorId;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public int getCrop()
	{
		return _crop;
	}
	
	public int getMature()
	{
		return _mature;
	}
	
	public int getReward(int type)
	{
		return type == 1 ? _type1 : _type2;
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public boolean isAlternative()
	{
		return _isAlternative;
	}
	
	public int getSeedLimit()
	{
		return _limitSeeds * Config.RATE_DROP_MANOR;
	}
	
	public int getCropLimit()
	{
		return _limitCrops * Config.RATE_DROP_MANOR;
	}
}
