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

import com.l2jmobius.gameserver.datatables.SkillData;

public final class SkillList extends L2GameServerPacket
{
	private final List<Skill> _skills = new ArrayList<>();
	private int _lastLearnedSkillId = 0;
	
	static class Skill
	{
		public int id;
		public int level;
		public boolean passive;
		public boolean disabled;
		public boolean enchanted;
		
		Skill(int pId, int pLevel, boolean pPassive, boolean pDisabled, boolean pEnchanted)
		{
			id = pId;
			level = pLevel;
			passive = pPassive;
			disabled = pDisabled;
			enchanted = pEnchanted;
		}
	}
	
	public void addSkill(int id, int level, boolean passive, boolean disabled, boolean enchanted)
	{
		_skills.add(new Skill(id, level, passive, disabled, enchanted));
	}
	
	public void setLastLearnedSkillId(int lastLearnedSkillId)
	{
		_lastLearnedSkillId = lastLearnedSkillId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x5F);
		writeD(_skills.size());
		
		for (Skill temp : _skills)
		{
			writeD(temp.passive ? 1 : 0);
			if (temp.level < 100)
			{
				writeD(temp.level);
			}
			else
			{
				final int maxlevel = SkillData.getInstance().getMaxLevel(temp.id);
				writeH(maxlevel);
				writeH(temp.level);
			}
			writeD(temp.id);
			writeD(-1); // GOD ReuseDelayShareGroupID
			writeC(temp.disabled ? 1 : 0); // iSkillDisabled
			writeC(temp.enchanted ? 1 : 0); // CanEnchant
		}
		writeD(_lastLearnedSkillId);
	}
}
