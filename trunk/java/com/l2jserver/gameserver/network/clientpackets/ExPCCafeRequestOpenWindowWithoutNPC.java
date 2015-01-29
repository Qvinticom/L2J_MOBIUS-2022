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
package com.l2jserver.gameserver.network.clientpackets;

import com.l2jserver.Config;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;

public class ExPCCafeRequestOpenWindowWithoutNPC extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance _activeChar = getClient().getActiveChar();
		if ((_activeChar != null) && Config.PC_BANG_ENABLED)
		{
			getHtmlPage(_activeChar);
		}
	}
	
	public void getHtmlPage(L2PcInstance player)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage();
		html.setFile(player.getHtmlPrefix(), "data/html/pccafe.htm");
		player.sendPacket(html);
	}
	
	@Override
	public String getType()
	{
		return getClass().getName();
	}
}
