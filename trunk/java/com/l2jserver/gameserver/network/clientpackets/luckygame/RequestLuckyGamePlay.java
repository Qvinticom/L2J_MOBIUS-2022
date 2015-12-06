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
package com.l2jserver.gameserver.network.clientpackets.luckygame;

import com.l2jserver.gameserver.network.clientpackets.L2GameClientPacket;
import com.l2jserver.gameserver.network.serverpackets.luckygame.ExBettingLuckyGameResult;

/**
 * @author Mobius
 */
public class RequestLuckyGamePlay extends L2GameClientPacket
{
	private int _type;
	private int _count;
	
	@Override
	protected void readImpl()
	{
		_type = readD(); // luxury = 2, normal = 1
		_count = readD(); // count
	}
	
	@Override
	protected void runImpl()
	{
		getActiveChar().sendPacket(new ExBettingLuckyGameResult(_type, _count));
	}
	
	@Override
	public String getType()
	{
		return getClass().getSimpleName();
	}
}
