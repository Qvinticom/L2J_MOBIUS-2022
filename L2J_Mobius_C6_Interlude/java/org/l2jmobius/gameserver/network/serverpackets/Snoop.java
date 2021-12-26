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
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * CDSDDSS -> (0xd5)(objId)(name)(0)(type)(speaker)(name)
 */
public class Snoop implements IClientOutgoingPacket
{
	private final Player _snooped;
	private final ChatType _type;
	private final String _speaker;
	private final String _msg;
	
	public Snoop(Player snooped, ChatType chatType, String speaker, String msg)
	{
		_snooped = snooped;
		_type = chatType;
		_speaker = speaker;
		_msg = msg;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.SNOOP.writeId(packet);
		packet.writeD(_snooped.getObjectId());
		packet.writeS(_snooped.getName());
		packet.writeD(0); // ??
		packet.writeD(_type.getClientId());
		packet.writeS(_speaker);
		packet.writeS(_msg);
		return true;
	}
}