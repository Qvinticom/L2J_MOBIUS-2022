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

import java.util.Map.Entry;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.PremiumItem;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author Gnacik
 */
public class ExGetPremiumItemList implements IClientOutgoingPacket
{
	private final Player _player;
	
	public ExGetPremiumItemList(Player player)
	{
		_player = player;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_GET_PREMIUM_ITEM_LIST.writeId(packet);
		packet.writeD(_player.getPremiumItemList().size());
		for (Entry<Integer, PremiumItem> entry : _player.getPremiumItemList().entrySet())
		{
			final PremiumItem item = entry.getValue();
			packet.writeD(entry.getKey());
			packet.writeD(_player.getObjectId());
			packet.writeD(item.getItemId());
			packet.writeQ(item.getCount());
			packet.writeD(0); // ?
			packet.writeS(item.getSender());
		}
		return true;
	}
}
