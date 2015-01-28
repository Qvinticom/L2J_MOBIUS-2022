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
package com.l2jserver.gameserver.network.clientpackets.itemappearance;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.clientpackets.L2GameClientPacket;

public final class RequestExCancelShape_Shifting_Item extends L2GameClientPacket
{
	private static final String _C__D0_C6_REQUESTEXCANCELSHAPE_SHIFTING_ITEM = "[C] D0:C6 RequestExCancelShape_Shifting_Item";
	
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		player.setAppearanceItem(null);
		player.setTargetAppearanceItem(null);
		player.setUsingAppearanceStone(null);
	}
	
	@Override
	public String getType()
	{
		return _C__D0_C6_REQUESTEXCANCELSHAPE_SHIFTING_ITEM;
	}
}
