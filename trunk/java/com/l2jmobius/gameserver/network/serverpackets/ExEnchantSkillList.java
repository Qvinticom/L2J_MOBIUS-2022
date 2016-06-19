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

import java.util.ArrayList;
import java.util.List;

import com.l2jmobius.commons.network.PacketWriter;
import com.l2jmobius.gameserver.network.client.OutgoingPackets;

public class ExEnchantSkillList implements IClientOutgoingPacket
{
	public enum EnchantSkillType
	{
		NORMAL,
		SAFE,
		UNTRAIN,
		CHANGE_ROUTE,
	}
	
	private final EnchantSkillType _type;
	private final List<Skill> _skills;
	
	static class Skill
	{
		public int id;
		public int nextLevel;
		
		Skill(int pId, int pNextLevel)
		{
			id = pId;
			nextLevel = pNextLevel;
		}
	}
	
	public void addSkill(int id, int level)
	{
		_skills.add(new Skill(id, level));
	}
	
	public ExEnchantSkillList(EnchantSkillType type)
	{
		_type = type;
		_skills = new ArrayList<>();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_ENCHANT_SKILL_LIST.writeId(packet);
		
		packet.writeD(_type.ordinal());
		packet.writeD(_skills.size());
		for (Skill sk : _skills)
		{
			packet.writeD(sk.id);
			packet.writeD(sk.nextLevel);
		}
		return true;
	}
}