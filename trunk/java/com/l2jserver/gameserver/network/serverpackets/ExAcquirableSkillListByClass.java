/*
 * Copyright (C) 2004-2014 L2J Server
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

import com.l2jserver.gameserver.model.L2SkillLearn;
import com.l2jserver.gameserver.model.base.AcquireSkillType;

/**
 * @author UnAfraid
 */
public class ExAcquirableSkillListByClass extends L2GameServerPacket
{
	final List<L2SkillLearn> _learnable;
	final AcquireSkillType _type;
	
	public ExAcquirableSkillListByClass(List<L2SkillLearn> learnable, AcquireSkillType type)
	{
		_learnable = learnable;
		_type = type;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xFA);
		writeH(_type.getId());
		writeH(_learnable.size());
		for (L2SkillLearn skill : _learnable)
		{
			writeD(skill.getSkillId());
			writeH(skill.getSkillLevel());
			writeH(skill.getSkillLevel());
			writeC(skill.getGetLevel());
			writeQ(skill.getLevelUpSp());
			writeC(skill.getRequiredItems().size());
			if (_type == AcquireSkillType.SUBPLEDGE)
			{
				writeH(0x00);
			}
		}
	}
}
