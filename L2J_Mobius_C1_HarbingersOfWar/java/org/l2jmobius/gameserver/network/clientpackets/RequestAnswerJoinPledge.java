/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.gameserver.model.Clan;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.CharInfo;
import org.l2jmobius.gameserver.network.serverpackets.JoinPledge;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListAdd;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListAll;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;

public class RequestAnswerJoinPledge extends ClientBasePacket
{
	public RequestAnswerJoinPledge(byte[] rawPacket, ClientThread client)
	{
		super(rawPacket);
		final int answer = readD();
		// Connection con = client.getConnection();
		final PlayerInstance activeChar = client.getActiveChar();
		final PlayerInstance requestor = activeChar.getTransactionRequester();
		if (answer == 1)
		{
			activeChar.sendPacket(new JoinPledge(requestor.getClanId()));
			final Clan clan = requestor.getClan();
			clan.addClanMember(activeChar);
			clan.store();
			activeChar.setClanId(clan.getClanId());
			activeChar.setClan(clan);
			activeChar.sendPacket(new PledgeShowInfoUpdate(clan, activeChar));
			activeChar.sendPacket(new UserInfo(activeChar));
			activeChar.broadcastPacket(new CharInfo(activeChar));
			SystemMessage sm = new SystemMessage(SystemMessage.ENTERED_THE_CLAN);
			activeChar.sendPacket(sm);
			// ClanMember[] members = clan.getMembers();
			sm = new SystemMessage(SystemMessage.S1_HAS_JOINED_CLAN);
			sm.addString(activeChar.getName());
			clan.broadcastToOnlineMembers(new PledgeShowMemberListAdd(activeChar));
			clan.broadcastToOnlineMembers(sm);
			activeChar.sendPacket(new PledgeShowMemberListAll(clan, activeChar));
		}
		else
		{
			final SystemMessage sm = new SystemMessage(SystemMessage.S1_REFUSED_TO_JOIN_CLAN);
			sm.addString(activeChar.getName());
			requestor.sendPacket(sm);
		}
		requestor.setTransactionRequester(null);
		activeChar.setTransactionRequester(null);
	}
}
