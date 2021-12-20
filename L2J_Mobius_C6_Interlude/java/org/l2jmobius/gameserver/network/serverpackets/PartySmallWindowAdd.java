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
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @version $Revision: 1.4.2.1.2.5 $ $Date: 2005/03/27 15:29:57 $
 */
public class PartySmallWindowAdd implements IClientOutgoingPacket
{
	private final Player _member;
	private final int _leaderId;
	private final int _distribution;
	
	public PartySmallWindowAdd(Player member, Party party)
	{
		_member = member;
		_leaderId = party.getPartyLeaderOID();
		_distribution = party.getLootDistribution();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.PARTY_SMALL_WINDOW_ADD.writeId(packet);
		packet.writeD(_leaderId); // c3
		packet.writeD(_distribution); // c3
		packet.writeD(_member.getObjectId());
		packet.writeS(_member.getName());
		packet.writeD((int) _member.getCurrentCp()); // c4
		packet.writeD(_member.getMaxCp()); // c4
		packet.writeD((int) _member.getCurrentHp());
		packet.writeD(_member.getMaxHp());
		packet.writeD((int) _member.getCurrentMp());
		packet.writeD(_member.getMaxMp());
		packet.writeD(_member.getLevel());
		packet.writeD(_member.getClassId().getId());
		packet.writeD(0); // writeD(1); ??
		packet.writeD(0);
		return true;
	}
}
