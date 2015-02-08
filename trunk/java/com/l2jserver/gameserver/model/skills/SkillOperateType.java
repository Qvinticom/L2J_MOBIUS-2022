/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.model.skills;

/**
 * This enum class holds the skill operative types:
 * <ul>
 * <li>ACTIVE_INSTANT</li>
 * <li>ACTIVE_CONTINUOUS</li>
 * <li>ACTIVE_WITH_TRIGGER</li>
 * <li>SPECIAL_HERB</li>
 * <li>CHANNELING_INSTANT</li>
 * <li>CHANNELING_CONTINUOUS</li>
 * <li>DIRECTIONAL_INSTANT</li>
 * <li>DIRECTIONAL_CONTINUOUS</li>
 * <li>PASSIVE</li>
 * <li>TOGGLE</li>
 * </ul>
 * @author Zoey76
 */
public enum SkillOperateType
{
	/**
	 * Active Skill with "Instant Effect" (for example damage skills heal/pdam/mdam/cpdam skills).
	 */
	ACTIVE_INSTANT,
	
	/**
	 * Active Skill with "Continuous effect + Instant effect" (for example buff/debuff or damage/heal over time skills).
	 */
	ACTIVE_CONTINUOUS,
	
	/**
	 * Active Skill with "Instant effect + Continuous effect"
	 */
	ACTIVE_WITH_TRIGGER,
	
	/**
	 * Active Skill with "Instant effect + ?" used for special event herb (itemId 20903, skillId 22158).
	 */
	SPECIAL_HERB,
	
	/**
	 * Continuous Active Skill with "instant effect" (instant effect casted by ticks).
	 */
	CHANNELING_INSTANT,
	
	/**
	 * Continuous Active Skill with "continuous effect" (continuous effect casted by ticks).
	 */
	CHANNELING_CONTINUOUS,
	
	/**
	 * Directional Active Skill with "Charge/Rush instant effect".
	 */
	DIRECTIONAL_INSTANT,
	
	/**
	 * Directional Active Skill with "Charge/Rush Continuous effect".
	 */
	DIRECTIONAL_CONTINUOUS,
	
	/**
	 * Passive Skill.
	 */
	PASSIVE,
	
	/**
	 * Toggle Skill.
	 */
	TOGGLE;
	
	/**
	 * Verifies if the operative type correspond to an active skill.
	 * @return {@code true} if the operative skill type is active, {@code false} otherwise
	 */
	public boolean isActive()
	{
		switch (this)
		{
			case ACTIVE_INSTANT:
			case ACTIVE_CONTINUOUS:
			case ACTIVE_WITH_TRIGGER:
			case CHANNELING_INSTANT:
			case CHANNELING_CONTINUOUS:
			case DIRECTIONAL_INSTANT:
			case DIRECTIONAL_CONTINUOUS:
			case SPECIAL_HERB:
				return true;
			default:
				return false;
		}
	}
	
	/**
	 * Verifies if the operative type correspond to a continuous skill.
	 * @return {@code true} if the operative skill type is continuous, {@code false} otherwise
	 */
	public boolean isContinuous()
	{
		switch (this)
		{
			case ACTIVE_CONTINUOUS:
			case DIRECTIONAL_CONTINUOUS:
			case SPECIAL_HERB:
				return true;
			default:
				return false;
		}
	}
	
	/**
	 * Verifies if the operative type correspond to a continuous skill.
	 * @return {@code true} if the operative skill type is continuous, {@code false} otherwise
	 */
	public boolean isSelfContinuous()
	{
		return (this == ACTIVE_WITH_TRIGGER);
	}
	
	/**
	 * Verifies if the operative type correspond to a passive skill.
	 * @return {@code true} if the operative skill type is passive, {@code false} otherwise
	 */
	public boolean isPassive()
	{
		return (this == PASSIVE);
	}
	
	/**
	 * Verifies if the operative type correspond to a toggle skill.
	 * @return {@code true} if the operative skill type is toggle, {@code false} otherwise
	 */
	public boolean isToggle()
	{
		return (this == TOGGLE);
	}
	
	/**
	 * Verifies if the operative type correspond to a channeling skill.
	 * @return {@code true} if the operative skill type is channeling, {@code false} otherwise
	 */
	public boolean isChanneling()
	{
		switch (this)
		{
			case CHANNELING_INSTANT:
			case CHANNELING_CONTINUOUS:
				return true;
			default:
				return false;
		}
	}
}
