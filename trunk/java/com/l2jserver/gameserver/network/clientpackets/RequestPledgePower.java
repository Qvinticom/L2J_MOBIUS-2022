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

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.serverpackets.ManagePledgePower;

public final class RequestPledgePower extends L2GameClientPacket
{
	private static final String _C__CC_REQUESTPLEDGEPOWER = "[C] CC RequestPledgePower";
	
	private int _rank;
	private int _action;
	private int _privs;
	
	@Override
	protected void readImpl()
	{
		_rank = readD();
		_action = readD();
		if (_action == 2)
		{
			_privs = readD();
		}
		else
		{
			_privs = 0;
		}
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if ((player == null) || !player.isClanLeader())
		{
			return;
		}
		
		if (_action == 2)
		{
			player.getClan().setRankPrivs(_rank, _privs);
		}
		else
		{
			player.getClan().updateRankPrivs(_rank, player.getClan().getRankPrivs(_rank).getBitmask());
		}
		player.sendPacket(new ManagePledgePower(player.getClan(), _action, _rank));
	}
	
	@Override
	public String getType()
	{
		return _C__CC_REQUESTPLEDGEPOWER;
	}
}