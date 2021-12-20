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
package org.l2jmobius.gameserver.network.serverpackets.pk;

import java.util.Set;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author Mobius
 */
public class ExPkPenaltyListOnlyLoc implements IClientOutgoingPacket
{
	public ExPkPenaltyListOnlyLoc()
	{
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_PK_PENALTY_LIST_ONLY_LOC.writeId(packet);
		final Set<Player> players = World.getInstance().getPkPlayers();
		packet.writeD(World.getInstance().getLastPkTime());
		packet.writeD(players.size());
		for (Player player : players)
		{
			packet.writeD(player.getObjectId());
			packet.writeD(player.getX());
			packet.writeD(player.getY());
			packet.writeD(player.getZ());
		}
		return true;
	}
}
