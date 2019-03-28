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
import com.l2jmobius.gameserver.instancemanager.CastleManager;
import com.l2jmobius.gameserver.instancemanager.FortManager;
import com.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import com.l2jmobius.gameserver.model.clan.Clan;
import com.l2jmobius.gameserver.network.GameClient;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.ExPledgeCount;
import com.l2jmobius.gameserver.network.serverpackets.JoinPledge;
import com.l2jmobius.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import com.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListAdd;
import com.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListAll;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestAnswerJoinPledge implements IClientIncomingPacket
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
		final PlayerInstance player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		final PlayerInstance requestor = player.getRequest().getPartner();
		if (requestor == null)
		{
			return;
		}
		
		if (_answer == 0)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_DIDN_T_RESPOND_TO_S1_S_INVITATION_JOINING_HAS_BEEN_CANCELLED);
			sm.addString(requestor.getName());
			player.sendPacket(sm);
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DID_NOT_RESPOND_INVITATION_TO_THE_CLAN_HAS_BEEN_CANCELLED);
			sm.addString(player.getName());
			requestor.sendPacket(sm);
		}
		else
		{
			if (!((requestor.getRequest().getRequestPacket() instanceof RequestJoinPledge) || (requestor.getRequest().getRequestPacket() instanceof RequestClanAskJoinByName)))
			{
				return; // hax
			}
			
			final int pledgeType;
			if (requestor.getRequest().getRequestPacket() instanceof RequestJoinPledge)
			{
				pledgeType = ((RequestJoinPledge) requestor.getRequest().getRequestPacket()).getPledgeType();
			}
			else
			{
				pledgeType = ((RequestClanAskJoinByName) requestor.getRequest().getRequestPacket()).getPledgeType();
			}
			
			final Clan clan = requestor.getClan();
			// we must double check this cause during response time conditions can be changed, i.e. another player could join clan
			if (clan.checkClanJoinCondition(requestor, player, pledgeType))
			{
				if (player.getClan() != null)
				{
					return;
				}
				
				player.sendPacket(new JoinPledge(requestor.getClanId()));
				
				player.setPledgeType(pledgeType);
				if (pledgeType == Clan.SUBUNIT_ACADEMY)
				{
					player.setPowerGrade(9); // academy
					player.setLvlJoinedAcademy(player.getLevel());
				}
				else
				{
					player.setPowerGrade(5); // new member starts at 5, not confirmed
				}
				
				clan.addClanMember(player);
				player.setClanPrivileges(player.getClan().getRankPrivs(player.getPowerGrade()));
				player.sendPacket(SystemMessageId.ENTERED_THE_CLAN);
				
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_JOINED_THE_CLAN);
				sm.addString(player.getName());
				clan.broadcastToOnlineMembers(sm);
				
				if (clan.getCastleId() > 0)
				{
					CastleManager.getInstance().getCastleByOwner(clan).giveResidentialSkills(player);
				}
				if (clan.getFortId() > 0)
				{
					FortManager.getInstance().getFortByOwner(clan).giveResidentialSkills(player);
				}
				player.sendSkillList();
				
				clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListAdd(player), player);
				clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
				clan.broadcastToOnlineMembers(new ExPledgeCount(clan));
				
				// this activates the clan tab on the new member
				PledgeShowMemberListAll.sendAllTo(player);
				player.setClanJoinExpiryTime(0);
				player.broadcastUserInfo();
			}
		}
		
		player.getRequest().onRequestResponse();
	}
}
