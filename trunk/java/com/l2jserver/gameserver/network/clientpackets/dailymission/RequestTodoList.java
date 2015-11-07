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
package com.l2jserver.gameserver.network.clientpackets.dailymission;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.clientpackets.L2GameClientPacket;
import com.l2jserver.gameserver.network.serverpackets.dailymission.ExOneDayReceiveRewardList;
import com.l2jserver.gameserver.network.serverpackets.dailymission.ExTodoListInzone;

/**
 * @author Mobius
 */
public class RequestTodoList extends L2GameClientPacket
{
	private int _tab;
	@SuppressWarnings("unused")
	private int _showAllLevels;
	
	@Override
	protected void readImpl()
	{
		_tab = readC(); // Daily Reward = 9, Event = 1, Instance Zone = 2
		_showAllLevels = readC(); // Disabled = 0, Enabled = 1
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		switch (_tab)
		{
			case 1:
			{
				player.sendPacket(new ExTodoListInzone());
				break;
			}
			case 2:
			{
				player.sendPacket(new ExTodoListInzone());
				break;
			}
			case 9:
			{
				player.sendPacket(new ExOneDayReceiveRewardList(player));
				break;
			}
		}
	}
	
	@Override
	public String getType()
	{
		return getClass().getSimpleName();
	}
}
