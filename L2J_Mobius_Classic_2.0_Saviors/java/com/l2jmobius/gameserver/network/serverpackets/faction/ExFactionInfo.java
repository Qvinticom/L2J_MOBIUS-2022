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
package com.l2jmobius.gameserver.network.serverpackets.faction;

import com.l2jmobius.commons.network.PacketWriter;
import com.l2jmobius.gameserver.network.OutgoingPackets;
import com.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author Mathael
 */
public class ExFactionInfo implements IClientOutgoingPacket
{
	
	private final int _playerId;
	private final boolean _openDialog;
	
	public ExFactionInfo(int playerId, boolean openDialog)
	{
		_playerId = playerId;
		_openDialog = openDialog;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_FACTION_INFO.writeId(packet);
		
		packet.writeD(_playerId);
		packet.writeC(_openDialog ? 1 : 0);
		packet.writeD(6);
		
		for (int i = 0; i < 6; i++)
		{
			packet.writeC(i);
			packet.writeH(0);
			packet.writeE(0);
		}
		
		return true;
	}
}
