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
import org.l2jmobius.gameserver.model.ClanMember;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.CharInfo;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListDeleteAll;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;

public class RequestWithdrawalPledge extends ClientBasePacket
{
	private static final String _C__26_REQUESTWITHDRAWALPLEDGE = "[C] 26 RequestWithdrawalPledge";
	
	public RequestWithdrawalPledge(byte[] rawPacket, ClientThread client)
	{
		super(rawPacket);
		// Connection con = client.getConnection();
		PlayerInstance activeChar = client.getActiveChar();
		if (activeChar.getClanId() == 0)
		{
			return;
		}
		Clan clan = activeChar.getClan();
		ClanMember member = clan.getClanMember(activeChar.getName());
		clan.removeClanMember(activeChar.getName());
		clan.store();
		activeChar.sendPacket(new SystemMessage(SystemMessage.CLAN_MEMBERSHIP_TERMINATED));
		PlayerInstance player = member.getPlayerInstance();
		player.setClan(null);
		player.setClanId(0);
		player.setTitle("");
		player.sendPacket(new UserInfo(player));
		player.broadcastPacket(new CharInfo(player));
		player.sendPacket(new PledgeShowMemberListDeleteAll());
	}
	
	@Override
	public String getType()
	{
		return _C__26_REQUESTWITHDRAWALPLEDGE;
	}
}
