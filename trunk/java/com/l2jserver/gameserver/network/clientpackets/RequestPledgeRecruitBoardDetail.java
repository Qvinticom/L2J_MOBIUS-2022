/*
 * Copyright (C) 2004-2014 L2J Server
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

import com.l2jserver.gameserver.instancemanager.ClanEntryManager;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.clan.entry.PledgeRecruitInfo;
import com.l2jserver.gameserver.network.serverpackets.ExPledgeRecruitBoardDetail;

/**
 * @author Sdw
 */
public class RequestPledgeRecruitBoardDetail extends L2GameClientPacket
{
	private static final String _C__D0_D6_REQUESTPLEDGERECRUITBOARDDETAIL = "[C] D0;D6 RequestPledgeRecruitBoardDetail";
	
	private int _clanId;
	
	@Override
	protected void readImpl()
	{
		_clanId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		final PledgeRecruitInfo pledgeRecruitInfo = ClanEntryManager.getInstance().getClanById(_clanId);
		
		if (pledgeRecruitInfo == null)
		{
			return;
		}
		
		activeChar.sendPacket(new ExPledgeRecruitBoardDetail(pledgeRecruitInfo));
	}
	
	@Override
	public String getType()
	{
		return _C__D0_D6_REQUESTPLEDGERECRUITBOARDDETAIL;
	}
}
