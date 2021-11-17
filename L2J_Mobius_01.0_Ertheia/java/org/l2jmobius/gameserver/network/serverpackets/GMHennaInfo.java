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

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.Henna;
import org.l2jmobius.gameserver.model.stats.BaseStat;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * This server packet sends the player's henna information using the Game Master's UI.
 * @author KenM, Zoey76
 */
public class GMHennaInfo implements IClientOutgoingPacket
{
	private final Player _player;
	private final List<Henna> _hennas = new ArrayList<>();
	
	public GMHennaInfo(Player player)
	{
		_player = player;
		for (int i = 1; i < 4; i++)
		{
			if (player.getHenna(i) != null)
			{
				_hennas.add(player.getHenna(i));
			}
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.GMHENNA_INFO.writeId(packet);
		
		packet.writeC(_player.getHennaValue(BaseStat.INT)); // equip INT
		packet.writeC(_player.getHennaValue(BaseStat.STR)); // equip STR
		packet.writeC(_player.getHennaValue(BaseStat.CON)); // equip CON
		packet.writeC(_player.getHennaValue(BaseStat.MEN)); // equip MEN
		packet.writeC(_player.getHennaValue(BaseStat.DEX)); // equip DEX
		packet.writeC(_player.getHennaValue(BaseStat.WIT)); // equip WIT
		packet.writeC(_player.getHennaValue(BaseStat.LUC)); // equip LUC
		packet.writeC(_player.getHennaValue(BaseStat.CHA)); // equip CHA
		packet.writeD(3); // Slots
		packet.writeD(_hennas.size()); // Size
		for (Henna henna : _hennas)
		{
			packet.writeD(henna.getDyeId());
			packet.writeD(0x01);
		}
		if (_player.getHenna(4) != null)
		{
			packet.writeD(_player.getHenna(4).getDyeId());
			packet.writeD(0x00); // Premium Slot Dye Time Left
			packet.writeD(_player.getHenna(4).isAllowedClass(_player.getClassId()) ? 0x01 : 0x00);
		}
		else
		{
			packet.writeD(0x00); // Premium Slot Dye ID
			packet.writeD(0x00); // Premium Slot Dye Time Left
			packet.writeD(0x00); // Premium Slot Dye ID isValid
		}
		return true;
	}
}
