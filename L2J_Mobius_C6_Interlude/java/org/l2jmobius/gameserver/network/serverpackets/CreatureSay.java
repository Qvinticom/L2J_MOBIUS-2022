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
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class CreatureSay implements IClientOutgoingPacket
{
	private final int _objectId;
	private final ChatType _chatType;
	private final String _charName;
	private final String _text;
	
	/**
	 * @param objectId
	 * @param chatType
	 * @param charName
	 * @param text
	 */
	public CreatureSay(int objectId, ChatType chatType, String charName, String text)
	{
		_objectId = objectId;
		_chatType = chatType;
		_charName = charName;
		_text = text;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.CREATURE_SAY.writeId(packet);
		packet.writeD(_objectId);
		packet.writeD(_chatType.getClientId());
		packet.writeS(_charName);
		packet.writeS(_text);
		return true;
	}
	
	@Override
	public void runImpl(Player player)
	{
		if (player != null)
		{
			player.broadcastSnoop(_chatType, _charName, _text, this);
		}
	}
}
