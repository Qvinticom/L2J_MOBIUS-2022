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
import org.l2jmobius.gameserver.model.items.Henna;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class HennaRemoveList implements IClientOutgoingPacket
{
	private final Player _player;
	
	public HennaRemoveList(Player player)
	{
		_player = player;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.HENNA_REMOVE_LIST.writeId(packet);
		packet.writeD(_player.getAdena());
		packet.writeD(_player.getHennaEmptySlots());
		packet.writeD(Math.abs(_player.getHennaEmptySlots() - 3));
		
		for (int i = 1; i <= 3; i++)
		{
			final Henna henna = _player.getHenna(i);
			if (henna != null)
			{
				packet.writeD(henna.getSymbolId());
				packet.writeD(henna.getDyeId());
				packet.writeD(Henna.getRequiredDyeAmount() / 2);
				packet.writeD(henna.getPrice() / 5);
				packet.writeD(0x01);
			}
		}
		return true;
	}
}