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
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.clan.ClanMember;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class PledgeShowMemberListAll implements IClientOutgoingPacket
{
	private final Clan _clan;
	private final Player _player;
	
	public PledgeShowMemberListAll(Clan clan, Player player)
	{
		_clan = clan;
		_player = player;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.PLEDGE_SHOW_MEMBER_LIST_ALL.writeId(packet);
		packet.writeD(_clan.getClanId());
		packet.writeS(_clan.getName());
		packet.writeS(_clan.getLeaderName());
		packet.writeD(_clan.getCrestId()); // crest id .. is used again
		packet.writeD(_clan.getLevel());
		packet.writeD(_clan.getCastleId());
		packet.writeD(_clan.getHideoutId());
		packet.writeD(0);
		packet.writeD(_player.getLevel()); // ??
		packet.writeD(_clan.getDissolvingExpiryTime() > Chronos.currentTimeMillis() ? 3 : 0);
		packet.writeD(0);
		packet.writeD(_clan.getAllyId());
		packet.writeS(_clan.getAllyName());
		packet.writeD(_clan.getAllyCrestId());
		packet.writeD(_clan.isAtWar()); // new c3
		packet.writeD(_clan.getMembers().size() - 1);
		for (ClanMember m : _clan.getMembers())
		{
			// On C4 player is not shown.
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
