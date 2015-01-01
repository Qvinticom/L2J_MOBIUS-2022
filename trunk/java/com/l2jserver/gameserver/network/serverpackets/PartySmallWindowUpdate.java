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

import com.l2jserver.gameserver.enums.PartySmallWindowUpdateType;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

public final class PartySmallWindowUpdate extends L2GameServerPacket
{
	private final L2PcInstance _member;
	private int _flags = 0;
	
	public PartySmallWindowUpdate(L2PcInstance member, boolean addAllFlags)
	{
		_member = member;
		if (addAllFlags)
		{
			for (PartySmallWindowUpdateType type : PartySmallWindowUpdateType.values())
			{
				addUpdateType(type);
			}
		}
	}
	
	public void addUpdateType(PartySmallWindowUpdateType type)
	{
		_flags |= type.getMask();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x52);
		writeD(_member.getObjectId());
		writeH(_flags);
		if (containsMask(_flags, PartySmallWindowUpdateType.CURRENT_CP))
		{
			writeD((int) _member.getCurrentCp()); // c4
		}
		if (containsMask(_flags, PartySmallWindowUpdateType.MAX_CP))
		{
			writeD(_member.getMaxCp()); // c4
		}
		if (containsMask(_flags, PartySmallWindowUpdateType.CURRENT_HP))
		{
			writeD((int) _member.getCurrentHp());
		}
		if (containsMask(_flags, PartySmallWindowUpdateType.MAX_HP))
		{
			writeD(_member.getMaxHp());
		}
		if (containsMask(_flags, PartySmallWindowUpdateType.CURRENT_MP))
		{
			writeD((int) _member.getCurrentMp());
		}
		if (containsMask(_flags, PartySmallWindowUpdateType.MAX_MP))
		{
			writeD(_member.getMaxMp());
		}
		if (containsMask(_flags, PartySmallWindowUpdateType.LEVEL))
		{
			writeC(_member.getLevel());
		}
		if (containsMask(_flags, PartySmallWindowUpdateType.CLASS_ID))
		{
			writeH(_member.getClassId().getId());
		}
		if (containsMask(_flags, PartySmallWindowUpdateType.VITALITY_POINTS))
		{
			writeD(_member.getVitalityPoints());
		}
	}
}
