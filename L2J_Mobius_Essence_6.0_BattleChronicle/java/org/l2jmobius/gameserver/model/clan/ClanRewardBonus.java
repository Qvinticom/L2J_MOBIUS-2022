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
package org.l2jmobius.gameserver.model.clan;

import org.l2jmobius.gameserver.enums.ClanRewardType;
import org.l2jmobius.gameserver.model.holders.SkillHolder;

/**
 * @author UnAfraid
 */
public class ClanRewardBonus
{
	private final ClanRewardType _type;
	private final int _level;
	private final int _requiredAmount;
	private SkillHolder _skillReward;
	
	public ClanRewardBonus(ClanRewardType type, int level, int requiredAmount)
	{
		_type = type;
		_level = level;
		_requiredAmount = requiredAmount;
	}
	
	public ClanRewardType getType()
	{
		return _type;
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public int getRequiredAmount()
	{
		return _requiredAmount;
	}
	
	public SkillHolder getSkillReward()
	{
		return _skillReward;
	}
	
	public void setSkillReward(SkillHolder skillReward)
	{
		_skillReward = skillReward;
	}
}
