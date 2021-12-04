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
package org.l2jmobius.gameserver.network.serverpackets;

import java.util.Collection;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.clan.Clan.SubPledge;
import org.l2jmobius.gameserver.model.clan.ClanMember;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class PledgeShowMemberListAll implements IClientOutgoingPacket
{
	private final Clan _clan;
	private final Player _player;
	private final Collection<ClanMember> _members;
	private int _pledgeType;
	
	public PledgeShowMemberListAll(Clan clan, Player player)
	{
		_clan = clan;
		_player = player;
		_members = _clan.getMembers();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		_pledgeType = 0;
		writePledge(packet, 0);
		
		for (SubPledge element : _clan.getAllSubPledges())
		{
			_player.sendPacket(new PledgeReceiveSubPledgeCreated(element));
		}
		
		for (ClanMember m : _members)
		{
			if (m.getPledgeType() == 0)
			{
				continue;
			}
			_player.sendPacket(new PledgeShowMemberListAdd(m));
		}
		
		// unless this is sent sometimes, the client doesn't recognize the player as the leader
		_player.sendPacket(new UserInfo(_player));
		return true;
	}
	
	void writePledge(PacketWriter packet, int mainOrSubpledge)
	{
		OutgoingPackets.PLEDGE_SHOW_MEMBER_LIST_ALL.writeId(packet);
		
		packet.writeD(mainOrSubpledge); // c5 main clan 0 or any subpledge 1?
		packet.writeD(_clan.getClanId());
		packet.writeD(_pledgeType); // c5 - possibly pledge type?
		packet.writeS(_clan.getName());
		packet.writeS(_clan.getLeaderName());
		
		packet.writeD(_clan.getCrestId()); // crest id .. is used again
		packet.writeD(_clan.getLevel());
		packet.writeD(_clan.getCastleId());
		packet.writeD(_clan.getHideoutId());
		packet.writeD(ClanTable.getInstance().getTopRate(_clan.getClanId()));
		packet.writeD(_clan.getReputationScore()); // was activechar level
		packet.writeD(0); // 0
		packet.writeD(0); // 0
		
		packet.writeD(_clan.getAllyId());
		packet.writeS(_clan.getAllyName());
		packet.writeD(_clan.getAllyCrestId());
		packet.writeD(_clan.isAtWar());
		packet.writeD(_clan.getSubPledgeMembersCount(_pledgeType));
		
		int yellow;
		for (ClanMember m : _members)
		{
			if (m.getPledgeType() != _pledgeType)
			{
				continue;
			}
			
			if (m.getPledgeType() == -1)
			{
				yellow = m.getSponsor() != 0 ? 1 : 0;
			}
			else if (m.getPlayer() != null)
			{
				yellow = m.getPlayer().isClanLeader() ? 1 : 0;
			}
			else
			{
				yellow = 0;
			}
			
			packet.writeS(m.getName());
			packet.writeD(m.getLevel());
			packet.writeD(m.getClassId());
			packet.writeD(0);
			packet.writeD(1);
			packet.writeD(m.isOnline() || (_player.getObjectId() == m.getObjectId()) ? m.getObjectId() : 0);
			packet.writeD(yellow);
		}
	}
}
