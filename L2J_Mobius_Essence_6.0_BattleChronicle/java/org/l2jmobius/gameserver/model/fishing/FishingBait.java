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
package org.l2jmobius.gameserver.model.fishing;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.commons.util.Rnd;

/**
 * @author bit
 */
public class FishingBait
{
	private final int _itemId;
	private final int _level;
	private final int _minPlayerLevel;
	private final int _maxPlayerLevel;
	private final double _chance;
	private final int _timeMin;
	private final int _timeMax;
	private final int _waitMin;
	private final int _waitMax;
	private final boolean _isPremiumOnly;
	private final List<FishingCatch> _rewards = new ArrayList<>();
	
	public FishingBait(int itemId, int level, int minPlayerLevel, int maxPlayerLevel, double chance, int timeMin, int timeMax, int waitMin, int waitMax, boolean isPremiumOnly)
	{
		_itemId = itemId;
		_level = level;
		_minPlayerLevel = minPlayerLevel;
		_maxPlayerLevel = maxPlayerLevel;
		_chance = chance;
		_timeMin = timeMin;
		_timeMax = timeMax;
		_waitMin = waitMin;
		_waitMax = waitMax;
		_isPremiumOnly = isPremiumOnly;
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public int getMinPlayerLevel()
	{
		return _minPlayerLevel;
	}
	
	public int getMaxPlayerLevel()
	{
		return _maxPlayerLevel;
	}
	
	public double getChance()
	{
		return _chance;
	}
	
	public int getTimeMin()
	{
		return _timeMin;
	}
	
	public int getTimeMax()
	{
		return _timeMax;
	}
	
	public int getWaitMin()
	{
		return _waitMin;
	}
	
	public int getWaitMax()
	{
		return _waitMax;
	}
	
	public boolean isPremiumOnly()
	{
		return _isPremiumOnly;
	}
	
	public List<FishingCatch> getRewards()
	{
		return _rewards;
	}
	
	public void addReward(FishingCatch catchData)
	{
		_rewards.add(catchData);
	}
	
	public FishingCatch getRandom()
	{
		float random = Rnd.get(100);
		for (FishingCatch fishingCatchData : _rewards)
		{
			if (fishingCatchData.getChance() > random)
			{
				return fishingCatchData;
			}
			random -= fishingCatchData.getChance();
		}
		return null;
	}
}
