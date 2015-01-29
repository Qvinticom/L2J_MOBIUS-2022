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
package com.l2jserver.gameserver.network.clientpackets.primeshop;

import com.l2jserver.gameserver.data.xml.impl.PrimeShopData;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.clientpackets.L2GameClientPacket;

/**
 * @author Gnacik, UnAfraid
 */
public final class RequestBRBuyProduct extends L2GameClientPacket
{
	private int _brId;
	private int _count;
	
	@Override
	protected void readImpl()
	{
		_brId = readD();
		_count = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player != null)
		{
			PrimeShopData.getInstance().buyItem(player, _brId, _count);
		}
	}
	
	@Override
	public String getType()
	{
		return getClass().getSimpleName();
	}
}
