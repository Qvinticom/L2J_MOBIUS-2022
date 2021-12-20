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

public class HennaInfo implements IClientOutgoingPacket
{
	private final Player _player;
	private final Henna[] _hennas = new Henna[3];
	private int _count;
	
	public HennaInfo(Player player)
	{
		_player = player;
		_count = 0;
		for (int i = 0; i < 3; i++)
		{
			Henna henna = _player.getHenna(i + 1);
			if (henna != null)
			{
				_hennas[_count++] = henna;
			}
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.HENNA_INFO.writeId(packet);
		packet.writeC(_player.getHennaStatINT()); // equip INT
		packet.writeC(_player.getHennaStatSTR()); // equip STR
		packet.writeC(_player.getHennaStatCON()); // equip CON
		packet.writeC(_player.getHennaStatMEN()); // equip MEM
		packet.writeC(_player.getHennaStatDEX()); // equip DEX
		packet.writeC(_player.getHennaStatWIT()); // equip WIT
		// Henna slots
		int classId = _player.getClassId().level();
		if (classId == 1)
		{
			packet.writeD(2);
		}
		else if (classId > 1)
		{
			packet.writeD(3);
		}
		else
		{
			packet.writeD(0);
		}
		packet.writeD(_count); // size
		for (int i = 0; i < _count; i++)
		{
			packet.writeD(_hennas[i].getSymbolId());
			packet.writeD(_hennas[i].canBeUsedBy(_player) ? _hennas[i].getSymbolId() : 0);
		}
		return true;
	}
}
