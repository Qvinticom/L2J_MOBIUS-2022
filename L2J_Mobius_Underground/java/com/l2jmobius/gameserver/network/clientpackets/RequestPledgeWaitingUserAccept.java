/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jmobius.gameserver.network.clientpackets;

import com.l2jmobius.commons.network.PacketReader;
import com.l2jmobius.gameserver.enums.UserInfoType;
import com.l2jmobius.gameserver.instancemanager.ClanEntryManager;
import com.l2jmobius.gameserver.model.L2Clan;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.L2GameClient;
import com.l2jmobius.gameserver.network.serverpackets.JoinPledge;
import com.l2jmobius.gameserver.network.serverpackets.UserInfo;

/**
 * @author Sdw
 */
public class RequestPledgeWaitingUserAccept implements IClientIncomingPacket
{
	private boolean _acceptRequest;
	private int _playerId;
	private int _clanId;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_acceptRequest = packet.readD() == 1;
		_playerId = packet.readD();
		_clanId = packet.readD();
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		final L2PcInstance activeChar = client.getActiveChar();
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
}
