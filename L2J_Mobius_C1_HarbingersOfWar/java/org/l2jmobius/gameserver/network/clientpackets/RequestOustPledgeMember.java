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
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListDeleteAll;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;

public class RequestOustPledgeMember extends ClientBasePacket
{
	private static final String _C__27_REQUESTOUSTPLEDGEMEMBER = "[C] 27 RequestOustPledgeMember";
	
	public RequestOustPledgeMember(byte[] rawPacket, ClientThread client)
	{
		super(rawPacket);
		String target = readS();
		// Connection con = client.getConnection();
		PlayerInstance activeChar = client.getActiveChar();
		if (!activeChar.isClanLeader())
		{
			return;
		}
		Clan clan = activeChar.getClan();
		ClanMember member = clan.getClanMember(target);
		if (member == null)
		{
			_log.warning("target is not member of the clan");
			return;
		}
		clan.removeClanMember(target);
		clan.store();
		SystemMessage msg = new SystemMessage(SystemMessage.CLAN_MEMBER_S1_EXPELLED);
		msg.addString(member.getName());
		clan.broadcastToOnlineMembers(msg);
		clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(target));
		if (member.isOnline())
		{
			PlayerInstance player = member.getPlayerInstance();
			player.setClan(null);
			player.setClanId(0);
			player.setTitle("");
			player.sendPacket(new SystemMessage(SystemMessage.CLAN_MEMBERSHIP_TERMINATED));
			player.sendPacket(new UserInfo(player));
			player.broadcastPacket(new CharInfo(player));
			player.sendPacket(new PledgeShowMemberListDeleteAll());
		}
	}
	
	@Override
	public String getType()
	{
		return _C__27_REQUESTOUSTPLEDGEMEMBER;
	}
}
