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

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.clan.Clan.SubPledge;
import org.l2jmobius.gameserver.model.clan.ClanMember;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class PledgeShowMemberListAll implements IClientOutgoingPacket
{
	private final Clan _clan;
	private final Player _player;
	private final ClanMember[] _members;
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
		// FIXME: That's wrong on retail sends this whole packet few times (depending how much sub pledges it has)
		writePledge(packet, 0);
		for (SubPledge subPledge : _clan.getAllSubPledges())
		{
			_player.sendPacket(new PledgeReceiveSubPledgeCreated(subPledge, _clan));
		}
		for (ClanMember m : _members)
		{
			if (m.getPledgeType() == 0)
			{
				continue;
			}
			_player.sendPacket(new PledgeShowMemberListAdd(m));
		}
		// unless this is sent sometimes, the client doesn't recognise the player as the leader
		_player.sendPacket(new UserInfo(_player));
		_player.sendPacket(new ExBrExtraUserInfo(_player));
		return true;
	}
	
	private void writePledge(PacketWriter packet, int mainOrSubpledge)
	{
		OutgoingPackets.PLEDGE_SHOW_MEMBER_LIST_ALL.writeId(packet);
		packet.writeD(mainOrSubpledge);
		packet.writeD(_clan.getId());
		packet.writeD(_pledgeType);
		packet.writeS(_clan.getName());
		packet.writeS(_clan.getLeaderName());
		packet.writeD(_clan.getCrestId()); // crest id .. is used again
		packet.writeD(_clan.getLevel());
		packet.writeD(_clan.getCastleId());
		packet.writeD(_clan.getHideoutId());
		packet.writeD(_clan.getFortId());
		packet.writeD(_clan.getRank());
		packet.writeD(_clan.getReputationScore());
		packet.writeD(0); // 0
		packet.writeD(0); // 0
		packet.writeD(_clan.getAllyId());
		packet.writeS(_clan.getAllyName());
		packet.writeD(_clan.getAllyCrestId());
		packet.writeD(_clan.isAtWar() ? 1 : 0); // new c3
		packet.writeD(0); // Territory castle ID
		packet.writeD(_clan.getSubPledgeMembersCount(_pledgeType));
		for (ClanMember m : _members)
		{
			if (m.getPledgeType() != _pledgeType)
			{
				continue;
			}
			packet.writeS(m.getName());
			packet.writeD(m.getLevel());
			packet.writeD(m.getClassId());
			final Player player = m.getPlayer();
			if (player != null)
			{
				packet.writeD(player.getAppearance().isFemale() ? 1 : 0); // no visible effect
				packet.writeD(player.getRace().ordinal()); // packet.writeD(1);
			}
			else
			{
				packet.writeD(1); // no visible effect
				packet.writeD(1); // packet.writeD(1);
			}
			packet.writeD(m.isOnline() ? m.getObjectId() : 0); // objectId = online 0 = offline
			packet.writeD(m.getSponsor() != 0 ? 1 : 0);
		}
	}
}
