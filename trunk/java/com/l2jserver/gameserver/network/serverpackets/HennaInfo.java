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

import java.util.ArrayList;
import java.util.List;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.items.L2Henna;

/**
 * This server packet sends the player's henna information.
 * @author Zoey76
 */
public final class HennaInfo extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private final List<L2Henna> _hennas = new ArrayList<>();
	
	public HennaInfo(L2PcInstance player)
	{
		_activeChar = player;
		for (L2Henna henna : _activeChar.getHennaList())
		{
			if (henna != null)
			{
				_hennas.add(henna);
			}
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xE5);
		writeD(_activeChar.getHennaStatINT()); // equip INT
		writeD(_activeChar.getHennaStatSTR()); // equip STR
		writeD(_activeChar.getHennaStatCON()); // equip CON
		writeD(_activeChar.getHennaStatMEN()); // equip MEN
		writeD(_activeChar.getHennaStatDEX()); // equip DEX
		writeD(_activeChar.getHennaStatWIT()); // equip WIT
		writeD(_activeChar.getHennaStatLUC()); // equip LUC
		writeD(_activeChar.getHennaStatCHA()); // equip CHA
		writeD(3 - _activeChar.getHennaEmptySlots()); // Slots
		writeD(_hennas.size()); // Size
		for (L2Henna henna : _hennas)
		{
			writeD(henna.getDyeId());
			writeD(henna.isAllowedClass(_activeChar.getClassId()) ? 0x01 : 0x00);
		}
		writeD(0x00); // Unknown
		writeD(0x00); // Unknown
		writeD(0x00); // Unknown
	}
}
