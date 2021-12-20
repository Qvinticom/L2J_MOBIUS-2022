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

public class HennaItemRemoveInfo implements IClientOutgoingPacket
{
	private final Player _player;
	private final Henna _henna;
	
	public HennaItemRemoveInfo(Henna henna, Player player)
	{
		_henna = henna;
		_player = player;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.HENNA_ITEM_REMOVE_INFO.writeId(packet);
		packet.writeD(_henna.getSymbolId()); // symbol Id
		packet.writeD(_henna.getDyeId()); // item id of dye
		packet.writeD(Henna.getRequiredDyeAmount() / 2); // amount of given dyes
		packet.writeD(_henna.getPrice() / 5); // amount of required adenas
		packet.writeD(1); // able to remove or not 0 is false and 1 is true
		packet.writeD(_player.getAdena());
		packet.writeD(_player.getINT()); // current INT
		packet.writeC(_player.getINT() - _henna.getINT()); // equip INT
		packet.writeD(_player.getSTR()); // current STR
		packet.writeC(_player.getSTR() - _henna.getSTR()); // equip STR
		packet.writeD(_player.getCON()); // current CON
		packet.writeC(_player.getCON() - _henna.getCON()); // equip CON
		packet.writeD(_player.getMEN()); // current MEM
		packet.writeC(_player.getMEN() - _henna.getMEN()); // equip MEM
		packet.writeD(_player.getDEX()); // current DEX
		packet.writeC(_player.getDEX() - _henna.getDEX()); // equip DEX
		packet.writeD(_player.getWIT()); // current WIT
		packet.writeC(_player.getWIT() - _henna.getWIT()); // equip WIT
		return true;
	}
}