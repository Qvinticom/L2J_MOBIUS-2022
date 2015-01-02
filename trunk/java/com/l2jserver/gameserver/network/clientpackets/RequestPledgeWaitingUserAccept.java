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

import com.l2jserver.gameserver.enums.UserInfoType;
import com.l2jserver.gameserver.instancemanager.ClanEntryManager;
import com.l2jserver.gameserver.model.L2Clan;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.serverpackets.JoinPledge;
import com.l2jserver.gameserver.network.serverpackets.UserInfo;

/**
 * @author Sdw
 */
public class RequestPledgeWaitingUserAccept extends L2GameClientPacket
{
	private static final String _C__D0_DB_REQUESTPLEDGEDWAITINGUSERACCEPT = "[C] D0;DB RequestPledgeWaitingUserAccept";
	
	private boolean _acceptRequest;
	private int _playerId;
	private int _clanId;
	
	@Override
	protected void readImpl()
	{
		_acceptRequest = readD() == 1;
		_playerId = readD();
		_clanId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if ((activeChar == null) || (activeChar.getClan() == null))
		{
			return;
		}
		
		if (_acceptRequest)
		{
			final L2PcInstance player = L2World.getInstance().getPlayer(_playerId);
			if (player != null)
			{
				final L2Clan clan = activeChar.getClan();
				clan.addClanMember(player);
				player.sendPacket(new JoinPledge(_clanId));
				final UserInfo ui = new UserInfo(player);
				ui.addComponentType(UserInfoType.CLAN);
				player.sendPacket(ui);
				player.broadcastInfo();
				
				ClanEntryManager.getInstance().removePlayerApplication(clan.getId(), _playerId);
			}
		}
		else
		{
			ClanEntryManager.getInstance().removePlayerApplication(activeChar.getClanId(), _playerId);
		}
		
	}
	
	@Override
	public String getType()
	{
		return _C__D0_DB_REQUESTPLEDGEDWAITINGUSERACCEPT;
	}
}
