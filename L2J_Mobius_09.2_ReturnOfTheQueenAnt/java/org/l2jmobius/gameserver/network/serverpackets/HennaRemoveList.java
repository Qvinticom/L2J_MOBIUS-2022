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
import org.l2jmobius.gameserver.model.item.Henna;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author Zoey76
 */
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
		OutgoingPackets.HENNA_UNEQUIP_LIST.writeId(packet);
		packet.writeQ(_player.getAdena());
		final boolean premiumSlotEnabled = _player.getHenna(4) != null;
		packet.writeD(premiumSlotEnabled ? 4 : 3); // seems to be max size
		packet.writeD((premiumSlotEnabled ? 4 : 3) - _player.getHennaEmptySlots()); // slots used
		for (Henna henna : _player.getHennaList())
		{
			if (henna != null)
			{
				packet.writeD(henna.getDyeId());
				packet.writeD(henna.getDyeItemId());
				packet.writeQ(henna.getCancelCount());
				packet.writeQ(henna.getCancelFee());
				packet.writeD(0);
				packet.writeD(0);
			}
		}
		return true;
	}
}
