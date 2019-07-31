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
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.items.Henna;
import org.l2jmobius.gameserver.model.stats.BaseStats;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * This server packet sends the player's henna information using the Game Master's UI.
 * @author KenM, Zoey76
 */
public class GMHennaInfo implements IClientOutgoingPacket
{
	private final PlayerInstance _player;
	private final List<Henna> _hennas = new ArrayList<>();
	
	public GMHennaInfo(PlayerInstance player)
	{
		_player = player;
		for (Henna henna : _player.getHennaList())
		{
			if (henna != null)
			{
				_hennas.add(henna);
			}
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.GMHENNA_INFO.writeId(packet);
		
		packet.writeH(_player.getHennaValue(BaseStats.INT)); // equip INT
		packet.writeH(_player.getHennaValue(BaseStats.STR)); // equip STR
		packet.writeH(_player.getHennaValue(BaseStats.CON)); // equip CON
		packet.writeH(_player.getHennaValue(BaseStats.MEN)); // equip MEN
		packet.writeH(_player.getHennaValue(BaseStats.DEX)); // equip DEX
		packet.writeH(_player.getHennaValue(BaseStats.WIT)); // equip WIT
		packet.writeH(0x00); // equip LUC
		packet.writeH(0x00); // equip CHA
		packet.writeD(3); // Slots
		packet.writeD(_hennas.size()); // Size
		for (Henna henna : _hennas)
		{
			packet.writeD(henna.getDyeId());
			packet.writeD(0x01);
		}
		packet.writeD(0x00);
		packet.writeD(0x00);
		packet.writeD(0x00);
		return true;
	}
}
