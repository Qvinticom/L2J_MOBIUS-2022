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
 * @author Mobius
 */
public class TimestampHolder
{
	private final Skill _skill;
	private final long _reuse;
	private final long _stamp;
	
	public TimestampHolder(Skill skill, long reuse)
	{
		_skill = skill;
		_reuse = reuse;
		_stamp = System.currentTimeMillis() + _reuse;
	}
	
	public TimestampHolder(Skill skill, long reuse, long stamp)
	{
		_skill = skill;
		_reuse = reuse;
		_stamp = stamp;
	}
	
	public long getStamp()
	{
		return _stamp;
	}
	
	public Skill getSkill()
	{
		return _skill;
	}
	
	public int getSkillId()
	{
		return _skill.getId();
	}
	
	public int getSkillLevel()
	{
		return _skill.getLevel();
	}
	
	public long getReuse()
	{
		return _reuse;
	}
	
	public long getRemaining()
	{
		return Math.max(_stamp - System.currentTimeMillis(), 0);
	}
	
	public boolean hasNotPassed()
	{
		return System.currentTimeMillis() < _stamp;
	}
}
