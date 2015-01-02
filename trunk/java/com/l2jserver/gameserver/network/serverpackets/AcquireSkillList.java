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
package com.l2jserver.gameserver.network.serverpackets;

import java.util.List;

import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.datatables.SkillTreesData;
import com.l2jserver.gameserver.model.L2SkillLearn;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.holders.ItemHolder;

/**
 * @author Sdw
 */
public class AcquireSkillList extends L2GameServerPacket
{
	final List<L2SkillLearn> _learnable;
	
	public AcquireSkillList(L2PcInstance activeChar)
	{
		_learnable = SkillTreesData.getInstance().getAvailableSkills(activeChar, activeChar.getClassId(), false, false);
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x90);
		writeH(_learnable.size());
		for (L2SkillLearn skill : _learnable)
		{
			writeD(skill.getSkillId());
			writeH(skill.getSkillLevel());
			writeQ(skill.getLevelUpSp());
			writeC(skill.getGetLevel());
			writeC(skill.getGetLevel()); // Dual Class Level Required
			writeC(skill.getRequiredItems().size());
			for (ItemHolder item : skill.getRequiredItems())
			{
				writeD(item.getId());
				writeQ(item.getCount());
			}
			
			writeC(skill.getRemoveSkills().size());
			for (int skillId : skill.getRemoveSkills())
			{
				writeD(skillId);
				writeH(SkillData.getInstance().getMaxLevel(skillId));
			}
		}
	}
}
