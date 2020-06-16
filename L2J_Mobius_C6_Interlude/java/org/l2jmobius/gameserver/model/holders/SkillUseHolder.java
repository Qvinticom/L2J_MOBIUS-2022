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

import org.l2jmobius.gameserver.model.Skill;

/**
 * Skill casting information (used to queue when several skills are cast in a short time).
 * @author Mobius
 */
public class SkillUseHolder
{
	/** The _skill. */
	private final Skill _skill;
	
	/** The _ctrl pressed. */
	private final boolean _ctrlPressed;
	
	/** The _shift pressed. */
	private final boolean _shiftPressed;
	
	/**
	 * Instantiates a new skill dat.
	 * @param skill the skill
	 * @param ctrlPressed the ctrl pressed
	 * @param shiftPressed the shift pressed
	 */
	public SkillUseHolder(Skill skill, boolean ctrlPressed, boolean shiftPressed)
	{
		_skill = skill;
		_ctrlPressed = ctrlPressed;
		_shiftPressed = shiftPressed;
	}
	
	/**
	 * Checks if is ctrl pressed.
	 * @return true, if is ctrl pressed
	 */
	public boolean isCtrlPressed()
	{
		return _ctrlPressed;
	}
	
	/**
	 * Checks if is shift pressed.
	 * @return true, if is shift pressed
	 */
	public boolean isShiftPressed()
	{
		return _shiftPressed;
	}
	
	/**
	 * Gets the skill.
	 * @return the skill
	 */
	public Skill getSkill()
	{
		return _skill;
	}
	
	/**
	 * Gets the skill id.
	 * @return the skill id
	 */
	public int getSkillId()
	{
		return getSkill() != null ? getSkill().getId() : -1;
	}
}
