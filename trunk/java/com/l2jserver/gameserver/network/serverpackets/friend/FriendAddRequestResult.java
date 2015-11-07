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
package com.l2jserver.gameserver.network.serverpackets.friend;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket;

/**
 * @author UnAfraid
 */
public class FriendAddRequestResult extends L2GameServerPacket
{
	private final int _result;
	private final int _charId;
	private final String _charName;
	private final int _isOnline;
	private final int _charObjectId;
	private final int _charLevel;
	private final int _charClassId;
	
	public FriendAddRequestResult(L2PcInstance activeChar, int result)
	{
		_result = result;
		_charId = activeChar.getObjectId();
		_charName = activeChar.getName();
		_isOnline = activeChar.isOnlineInt();
		_charObjectId = activeChar.getObjectId();
		_charLevel = activeChar.getLevel();
		_charClassId = activeChar.getActiveClassId();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x55);
		writeD(_result);
		writeD(_charId);
		writeS(_charName);
		writeD(_isOnline);
		writeD(_charObjectId);
		writeD(_charLevel);
		writeD(_charClassId);
		writeH(0x00); // Always 0 on retail
	}
}
