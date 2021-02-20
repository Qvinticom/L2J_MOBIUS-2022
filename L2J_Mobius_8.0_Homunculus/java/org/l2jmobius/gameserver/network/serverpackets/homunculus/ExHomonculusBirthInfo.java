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
package org.l2jmobius.gameserver.network.serverpackets.homunculus;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author Mobius
 */
public class ExHomonculusBirthInfo implements IClientOutgoingPacket
{
	private final PlayerInstance _player;
	
	public ExHomonculusBirthInfo(PlayerInstance player)
	{
		_player = player;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_SHOW_BIRTH_INFO.writeId(packet);
		
		final int status = _player.getVariables().getInt(PlayerVariables.HOMUNCULUS_STATUS, 0);
		final int hp = _player.getVariables().getInt(PlayerVariables.HOMUNCULUS_HP, 0);
		final int sp = _player.getVariables().getInt(PlayerVariables.HOMUNCULUS_SP, 0);
		final int vp = _player.getVariables().getInt(PlayerVariables.HOMUNCULUS_VP, 0);
		final int time = _player.getVariables().getInt(PlayerVariables.HOMUNCULUS_TIME, 0);
		final long currentTime = Chronos.currentTimeMillis();
		
		packet.writeD(status); // 0 = time idle, 1 = time updating, 2 = summon enabled
		packet.writeD(hp); // hp 100
		packet.writeD(sp); // sp 10
		packet.writeD(vp); // vitality 5
		packet.writeQ((currentTime / 1000) + (86400 - Math.min(86400, (currentTime / 1000) - time)));
		
		return true;
	}
}
