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
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.data.xml.ClanLevelData;
import org.l2jmobius.gameserver.instancemanager.CastleManager;
import org.l2jmobius.gameserver.instancemanager.ClanEntryManager;
import org.l2jmobius.gameserver.instancemanager.FortManager;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.clan.entry.PledgeRecruitInfo;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExPledgeCount;
import org.l2jmobius.gameserver.network.serverpackets.JoinPledge;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListAdd;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListAll;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Mobius
 */
public class RequestPledgeSignInForOpenJoiningMethod implements IClientIncomingPacket
{
	private int _clanId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_clanId = packet.readD();
		packet.readD();
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
		
		final PledgeRecruitInfo pledgeRecruitInfo = ClanEntryManager.getInstance().getClanById(_clanId);
		if (pledgeRecruitInfo != null)
		{
			final Clan clan = pledgeRecruitInfo.getClan();
			if ((clan != null) && (player.getClan() == null))
			{
				if (clan.getCharPenaltyExpiryTime() > Chronos.currentTimeMillis())
				{
					player.sendPacket(SystemMessageId.AFTER_A_CLAN_MEMBER_IS_DISMISSED_FROM_A_CLAN_THE_CLAN_MUST_WAIT_AT_LEAST_A_DAY_BEFORE_ACCEPTING_A_NEW_MEMBER);
					return;
				}
				if (player.getClanJoinExpiryTime() > Chronos.currentTimeMillis())
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.C1_CANNOT_JOIN_THE_CLAN_BECAUSE_ONE_DAY_HAS_NOT_YET_PASSED_SINCE_THEY_LEFT_ANOTHER_CLAN);
					sm.addString(player.getName());
					player.sendPacket(sm);
					return;
				}
				if (clan.getSubPledgeMembersCount(Clan.PLEDGE_CLASS_COMMON) >= ClanLevelData.getCommonMemberLimit(pledgeRecruitInfo.getClan().getLevel()))
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_FULL_AND_CANNOT_ACCEPT_ADDITIONAL_CLAN_MEMBERS_AT_THIS_TIME);
					sm.addString(clan.getName());
					player.sendPacket(sm);
					return;
				}
				
				player.sendPacket(new JoinPledge(clan.getId()));
				
				// player.setPowerGrade(9); // academy
				player.setPowerGrade(5); // New member starts at 5, not confirmed.
				clan.addClanMember(player);
				player.setClanPrivileges(player.getClan().getRankPrivs(player.getPowerGrade()));
				player.sendPacket(SystemMessageId.ENTERED_THE_CLAN);
				
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_JOINED_THE_CLAN);
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
				
				// This activates the clan tab on the new member.
				PledgeShowMemberListAll.sendAllTo(player);
				player.setClanJoinExpiryTime(0);
				player.broadcastUserInfo();
				
				ClanEntryManager.getInstance().removePlayerApplication(_clanId, player.getObjectId());
			}
		}
	}
}