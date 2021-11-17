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
import org.l2jmobius.gameserver.data.xml.ClanLevelData;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.clan.ClanMember;
import org.l2jmobius.gameserver.model.clan.ClanPrivilege;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * Format: (ch) dSdS
 * @author -Wooden-
 */
public class RequestPledgeReorganizeMember implements IClientIncomingPacket
{
	private String _memberName;
	private int _newPledgeType;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		packet.readD(); // _isMemberSelected
		_memberName = packet.readS();
		_newPledgeType = packet.readD();
		packet.readS(); // _selectedMember
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
		
		final Clan clan = player.getClan();
		if (clan == null)
		{
			return;
		}
		
		if (!player.hasClanPrivilege(ClanPrivilege.CL_MANAGE_RANKS))
		{
			return;
		}
		
		final ClanMember member1 = clan.getClanMember(_memberName);
		if ((member1 == null) || (member1.getObjectId() == clan.getLeaderId()))
		{
			return;
		}
		
		final int oldPledgeType = member1.getPledgeType();
		if (oldPledgeType == _newPledgeType)
		{
			return;
		}
		
		if (clan.getSubPledgeMembersCount(_newPledgeType) >= (_newPledgeType == 0 ? ClanLevelData.getCommonMemberLimit(clan.getLevel()) : ClanLevelData.getEliteMemberLimit(clan.getLevel())))
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_FULL_AND_CANNOT_ACCEPT_ADDITIONAL_CLAN_MEMBERS_AT_THIS_TIME);
			sm.addString(_newPledgeType == 0 ? "Common Members" : "Elite Members");
			player.sendPacket(sm);
			return;
		}
		
		member1.setPledgeType(_newPledgeType);
		clan.broadcastClanStatus();
	}
}
