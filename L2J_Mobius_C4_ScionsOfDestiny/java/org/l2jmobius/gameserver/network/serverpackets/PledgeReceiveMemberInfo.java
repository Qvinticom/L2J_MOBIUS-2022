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
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.ClanMember;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author -Wooden-
 */
public class PledgeReceiveMemberInfo implements IClientOutgoingPacket
{
	private final ClanMember _member;
	private final Player _player;
	
	public PledgeReceiveMemberInfo(ClanMember member, Player player)
	{
		_member = member;
		_player = player;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.PLEDGE_RECEIVE_MEMBER_INFO.writeId(packet);
		packet.writeD(_member.getClan().getClanId());
		packet.writeS(_member.getClan().getName());
		packet.writeS(_member.getClan().getLeaderName());
		packet.writeD(_member.getClan().getCrestId()); // crest id .. is used again
		packet.writeD(_member.getClan().getLevel());
		packet.writeD(_member.getClan().getCastleId());
		packet.writeD(_member.getClan().getHideoutId());
		packet.writeD(0);
		packet.writeD(_player.getLevel()); // ??
		packet.writeD(_member.getClan().getDissolvingExpiryTime() > Chronos.currentTimeMillis() ? 3 : 0);
		packet.writeD(0);
		packet.writeD(_member.getClan().getAllyId());
		packet.writeS(_member.getClan().getAllyName());
		packet.writeD(_member.getClan().getAllyCrestId());
		packet.writeD(_member.getClan().isAtWar()); // new c3
		packet.writeD(_member.getClan().getMembers().size() - 1);
		for (ClanMember m : _member.getClan().getMembers())
		{
			// TODO is this c4?
			if (m.getObjectId() == _player.getObjectId())
			{
				continue;
			}
			packet.writeS(m.getName());
			packet.writeD(m.getLevel());
			packet.writeD(m.getClassId());
			packet.writeD(0);
			packet.writeD(1);
			packet.writeD(m.isOnline() ? m.getObjectId() : 0); // 1=online 0=offline
		}
		return true;
	}
}
