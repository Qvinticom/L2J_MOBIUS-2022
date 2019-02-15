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
package com.l2jmobius.gameserver.model.holders;

import com.l2jmobius.gameserver.model.skills.Skill;

/**
 * @author Mobius
 */
public class ClanSpecialtyHolder
{
	private final int _id;
	private final Skill _skill;
	private final int _clanLevel;
	private final int _clanReputation;
	private final int _previousSpecialty;
	private final int _previousSpecialtyAlt;
	
	public ClanSpecialtyHolder(int id, Skill skill, int clanLevel, int clanReputation, int previousSpecialty, int previousSpecialtyAlt)
	{
		_id = id;
		_skill = skill;
		_clanLevel = clanLevel;
		_clanReputation = clanReputation;
		_previousSpecialty = previousSpecialty;
		_previousSpecialtyAlt = previousSpecialtyAlt;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public Skill getSkill()
	{
		return _skill;
	}
	
	public int getClanLevel()
	{
		return _clanLevel;
	}
	
	public int getClanReputation()
	{
		return _clanReputation;
	}
	
	public int getPreviousSpecialty()
	{
		return _previousSpecialty;
	}
	
	public int getPreviousSpecialtyAlt()
	{
		return _previousSpecialtyAlt;
	}
}
