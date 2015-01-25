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
package com.l2jserver.gameserver.network.clientpackets.friend;

import com.l2jserver.gameserver.data.sql.impl.CharNameTable;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.clientpackets.L2GameClientPacket;
import com.l2jserver.gameserver.network.serverpackets.friend.BlockListPacket;

public final class RequestUpdateBlockMemo extends L2GameClientPacket
{
	private static final String _C__D0_96_REQUESTUPDATEBLOCKMEMO = "[C] D0:96 RequestUpdateBlockMemo";
	
	private String cName;
	private String memo;
	
	@Override
	protected void readImpl()
	{
		cName = readS();
		memo = readS();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		player.getBlockList().setBlockMemo(CharNameTable.getInstance().getIdByName(cName), memo);
		player.sendPacket(new BlockListPacket(player));
	}
	
	@Override
	public String getType()
	{
		return _C__D0_96_REQUESTUPDATEBLOCKMEMO;
	}
	
}
