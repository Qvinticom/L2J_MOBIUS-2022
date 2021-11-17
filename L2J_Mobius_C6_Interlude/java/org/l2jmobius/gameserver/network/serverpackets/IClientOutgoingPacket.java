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

import java.util.logging.Logger;

import org.l2jmobius.commons.network.IOutgoingPacket;
import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.Player;

/**
 * @author KenM
 */
public interface IClientOutgoingPacket extends IOutgoingPacket
{
	Logger LOGGER = Logger.getLogger(IClientOutgoingPacket.class.getName());
	
	/**
	 * Sends this packet to the target player, useful for lambda operations like<br>
	 * {@code World.getInstance().getPlayers().forEach(packet::sendTo)}
	 * @param player
	 */
	default void sendTo(Player player)
	{
		player.sendPacket(this);
	}
	
	default void runImpl(Player player)
	{
	}
	
	default void writeOptionalD(PacketWriter packet, int value)
	{
		if (value >= Short.MAX_VALUE)
		{
			packet.writeH(Short.MAX_VALUE);
			packet.writeD(value);
		}
		else
		{
			packet.writeH(value);
		}
	}
}
