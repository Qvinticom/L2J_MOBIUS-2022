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
package org.l2jmobius.gameserver.model.conditions;

import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.taskmanager.GameTimeTaskManager;

/**
 * The Class ConditionGameTime.
 * @author mkizub
 */
public class ConditionGameTime extends Condition
{
	/**
	 * The Enum CheckGameTime.
	 */
	public enum CheckGameTime
	{
		NIGHT
	}
	
	private final CheckGameTime _check;
	private final boolean _required;
	
	/**
	 * Instantiates a new condition game time.
	 * @param check the check
	 * @param required the required
	 */
	public ConditionGameTime(CheckGameTime check, boolean required)
	{
		_check = check;
		_required = required;
	}
	
	/**
	 * Test impl.
	 * @return true, if successful
	 */
	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		switch (_check)
		{
			case NIGHT:
			{
				return GameTimeTaskManager.getInstance().isNight() == _required;
			}
		}
		return !_required;
	}
}
