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

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Sdw
 */
public class ExShowBeautyMenu extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private final int _type;
	
	public final static int MODIFY_APPEARANCE = 0;
	public final static int RESTORE_APPEARANCE = 1;
	
	public ExShowBeautyMenu(L2PcInstance activeChar, int type)
	{
		_activeChar = activeChar;
		_type = type;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x134);
		
		writeD(_type);
		writeD(_activeChar.getVisualHair());
		writeD(_activeChar.getVisualHairColor());
		writeD(_activeChar.getVisualFace());
	}
}