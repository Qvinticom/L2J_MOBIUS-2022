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
package org.l2jmobius.gameserver.network.serverpackets.pledgeV2;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author Bonux (bonuxq@gmail.com), NasSeKa
 * @date 29.09.2019
 **/
public class ExPledgeMissionRewardCount implements IClientOutgoingPacket
{
	private final int _doneMissionsCount;
	private final int _availableMissionsCount;
	
	public ExPledgeMissionRewardCount(Player player)
	{
		_doneMissionsCount = player.getVariables().getInt(PlayerVariables.DAILY_MISSION_COUNT, 0);
		_availableMissionsCount = player.getNobleLevel() == 1 ? 18 : player.getNobleLevel() >= 2 ? 20 : 16;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_PLEDGE_MISSION_REWARD_COUNT.writeId(packet);
		packet.writeD(Math.min(_availableMissionsCount, _doneMissionsCount)); // Received missions rewards.
		packet.writeD(_availableMissionsCount); // Available missions rewards. 18 - for noble, 20 - for honnorable noble.
		return true;
	}
}
