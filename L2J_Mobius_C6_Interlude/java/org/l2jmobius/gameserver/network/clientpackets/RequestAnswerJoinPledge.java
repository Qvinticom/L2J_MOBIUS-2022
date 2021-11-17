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
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.JoinPledge;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListAdd;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListAll;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class RequestAnswerJoinPledge implements IClientIncomingPacket
{
	private int _answer;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_answer = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		final Player requestor = player.getRequest().getPartner();
		if (requestor == null)
		{
			return;
		}
		
		if (_answer == 0)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DIDN_T_RESPOND_TO_S1_S_INVITATION_JOINING_HAS_BEEN_CANCELLED);
			sm.addString(requestor.getName());
			player.sendPacket(sm);
			sm = new SystemMessage(SystemMessageId.S1_DID_NOT_RESPOND_INVITATION_TO_THE_CLAN_HAS_BEEN_CANCELLED);
			sm.addString(player.getName());
			requestor.sendPacket(sm);
		}
		else
		{
			if (!(requestor.getRequest().getRequestPacket() instanceof RequestJoinPledge))
			{
				return; // hax
			}
			
			final RequestJoinPledge requestPacket = (RequestJoinPledge) requestor.getRequest().getRequestPacket();
			final Clan clan = requestor.getClan();
			// we must double check this cause during response time conditions can be changed, i.e. another player could join clan
			if ((clan != null) && clan.checkClanJoinCondition(requestor, player, requestPacket.getPledgeType()))
			{
				player.sendPacket(new JoinPledge(requestor.getClanId()));
				player.setPledgeType(requestPacket.getPledgeType());
				
				if (requestPacket.getPledgeType() == Clan.SUBUNIT_ACADEMY)
				{
					player.setPowerGrade(9); // adademy
					player.setLvlJoinedAcademy(player.getLevel());
				}
				else
				{
					player.setPowerGrade(5); // new member starts at 5, not confirmed
				}
				
				clan.addClanMember(player);
				player.setClanPrivileges(player.getClan().getRankPrivs(player.getPowerGrade()));
				
				player.sendPacket(SystemMessageId.ENTERED_THE_CLAN);
				
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_JOINED_THE_CLAN);
				sm.addString(player.getName());
				clan.broadcastToOnlineMembers(sm);
				
				clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListAdd(player), player);
				clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
				
				// this activates the clan tab on the new member
				player.sendPacket(new PledgeShowMemberListAll(clan, player));
				player.setClanJoinExpiryTime(0);
				player.broadcastUserInfo();
			}
		}
		
		player.getRequest().onRequestResponse();
	}
}