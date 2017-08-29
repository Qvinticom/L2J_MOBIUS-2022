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
package com.l2jmobius.gameserver.network.serverpackets;

import java.util.LinkedList;
import java.util.List;

import com.l2jmobius.gameserver.model.skills.Skill;

public class ExEnchantSkillList extends L2GameServerPacket
{
	public enum EnchantSkillType
	{
		NORMAL,
		SAFE,
		UNTRAIN,
		CHANGE_ROUTE,
	}
	
	private final EnchantSkillType _type;
	private final List<Skill> _skills = new LinkedList<>();
	
	public ExEnchantSkillList(EnchantSkillType type)
	{
		_type = type;
	}
	
	public void addSkill(Skill skill)
	{
		_skills.add(skill);
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x29);
		
		writeD(_type.ordinal());
		writeD(_skills.size());
		for (Skill skill : _skills)
		{
			writeD(skill.getId());
			writeD(skill.getLevel());
		}
	}
}