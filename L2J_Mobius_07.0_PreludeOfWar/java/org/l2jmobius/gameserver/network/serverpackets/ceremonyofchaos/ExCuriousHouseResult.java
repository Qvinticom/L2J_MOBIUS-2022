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
import org.l2jmobius.gameserver.enums.CeremonyOfChaosResult;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author Sdw
 */
public class ExCuriousHouseResult implements IClientOutgoingPacket
{
	private final CeremonyOfChaosResult _result;
	private final Collection<Player> _players;
	private final int _time;
	
	public ExCuriousHouseResult(CeremonyOfChaosResult result, Collection<Player> players, int time)
	{
		_result = result;
		_players = players;
		_time = time;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_CURIOUS_HOUSE_RESULT.writeId(packet);
		packet.writeD(0); // _event.getId()
		packet.writeH(_result.ordinal());
		packet.writeD(18); // max players
		packet.writeD(_players.size());
		int pos = 0;
		for (Player player : _players)
		{
			packet.writeD(player.getObjectId());
			packet.writeD(pos++); // position
			packet.writeD(player.getClassId().getId());
			packet.writeD(_time); // getLifeTime
			packet.writeD(player.getVariables().getInt(PlayerVariables.CEREMONY_OF_CHAOS_SCORE, 0));
		}
		return true;
	}
}
