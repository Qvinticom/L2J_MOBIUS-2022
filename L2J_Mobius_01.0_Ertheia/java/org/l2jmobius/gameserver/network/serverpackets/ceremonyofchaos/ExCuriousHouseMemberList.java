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
package org.l2jmobius.gameserver.network.serverpackets.ceremonyofchaos;

import java.util.Collection;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author UnAfraid
 */
public class ExCuriousHouseMemberList implements IClientOutgoingPacket
{
	private final int _id;
	private final int _maxPlayers;
	private final Collection<Player> _players;
	
	public ExCuriousHouseMemberList(int id, int maxPlayers, Collection<Player> players)
	{
		_id = id;
		_maxPlayers = maxPlayers;
		_players = players;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_CURIOUS_HOUSE_MEMBER_LIST.writeId(packet);
		packet.writeD(_id);
		packet.writeD(_maxPlayers);
		packet.writeD(_players.size());
		int pos = 0;
		for (Player player : _players)
		{
			packet.writeD(player.getObjectId());
			packet.writeD(pos++);
			packet.writeD(player.getMaxHp());
			packet.writeD(player.getMaxCp());
			packet.writeD((int) player.getCurrentHp());
			packet.writeD((int) player.getCurrentCp());
		}
		return true;
	}
}
