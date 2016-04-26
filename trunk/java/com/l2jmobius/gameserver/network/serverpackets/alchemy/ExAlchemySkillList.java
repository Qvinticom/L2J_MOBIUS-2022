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
package com.l2jmobius.gameserver.network.serverpackets.alchemy;

import java.util.ArrayList;
import java.util.List;

import com.l2jmobius.gameserver.data.xml.impl.SkillTreesData;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.skills.Skill;
import com.l2jmobius.gameserver.network.serverpackets.L2GameServerPacket;

/**
 * @author UnAfraid
 */
public class ExAlchemySkillList extends L2GameServerPacket
{
	private static final int ALCHEMY_CUBE_SKILL = 17943;
	private final List<Skill> _skills = new ArrayList<>();
	
	public ExAlchemySkillList(L2PcInstance player)
	{
		for (Skill skill : player.getAllSkills())
		{
			// Make sure its alchemy skill.
			if (SkillTreesData.getInstance().getAlchemySkill(skill.getId(), skill.getLevel()) != null)
			{
				_skills.add(skill);
			}
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x174);
		writeD(_skills.size());
		for (Skill skill : _skills)
		{
			writeD(skill.getId());
			writeD(skill.getLevel());
			writeQ(0x00); // Always 0 on Naia, SP i guess?
			writeC(skill.getId() == ALCHEMY_CUBE_SKILL ? 0x00 : 0x01);
		}
	}
}
