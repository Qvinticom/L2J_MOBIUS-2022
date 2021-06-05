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
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author Mobius
 */
public class ExShowHomunculusBirthInfo implements IClientOutgoingPacket
{
	private final int _hpPoints;
	private final int _spPoints;
	private final int _vpPoints;
	private final int _homunculusCreateTime;
	
	public ExShowHomunculusBirthInfo(PlayerInstance player)
	{
		_hpPoints = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_HP_POINTS, 0);
		_spPoints = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_SP_POINTS, 0);
		_vpPoints = player.getVariables().getInt(PlayerVariables.HOMUNCULUS_VP_POINTS, 0);
		_homunculusCreateTime = (int) (player.getVariables().getLong(PlayerVariables.HOMUNCULUS_CREATION_TIME, 0) / 1000);
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_SHOW_HOMUNCULUS_BIRTH_INFO.writeId(packet);
		
		int creationStage = 0;
		if (_homunculusCreateTime > 0)
		{
			if (((System.currentTimeMillis() / 1000) >= _homunculusCreateTime) && (_hpPoints == 100) && (_spPoints == 10) && (_vpPoints == 5))
			{
				creationStage = 2;
			}
			else
			{
				creationStage = 1;
			}
		}
		packet.writeD(creationStage); // in creation process (0: can create, 1: in process, 2: can awake
		packet.writeD(_hpPoints); // hp points
		packet.writeD(_spPoints); // sp points
		packet.writeD(_vpPoints); // vp points
		packet.writeD(_homunculusCreateTime); // finish time
		packet.writeD(0); // JP = 0. ?
		
		return true;
	}
}
