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
package com.l2jmobius.gameserver.network.serverpackets;

import com.l2jmobius.commons.network.PacketWriter;
import com.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import com.l2jmobius.gameserver.model.items.instance.ItemInstance;
import com.l2jmobius.gameserver.network.OutgoingPackets;

public final class WareHouseWithdrawalList extends AbstractItemPacket
{
	public static final int PRIVATE = 1;
	public static final int CLAN = 4;
	public static final int CASTLE = 3; // not sure
	public static final int FREIGHT = 1;
	private long _playerAdena;
	private ItemInstance[] _items;
	/**
	 * <ul>
	 * <li>0x01-Private Warehouse</li>
	 * <li>0x02-Clan Warehouse</li>
	 * <li>0x03-Castle Warehouse</li>
	 * <li>0x04-Warehouse</li>
	 * </ul>
	 */
	private int _whType;
	
	public WareHouseWithdrawalList(PlayerInstance player, int type)
	{
		if (player.getActiveWarehouse() == null)
		{
			LOGGER.warning("error while sending withdraw request to: " + player.getName());
			return;
		}
		
		_playerAdena = player.getAdena();
		_items = player.getActiveWarehouse().getItems();
		_whType = type;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.WAREHOUSE_WITHDRAW_LIST.writeId(packet);
		packet.writeH(_whType);
		packet.writeQ(_playerAdena);
		packet.writeH(_items.length);
		
		for (ItemInstance item : _items)
		{
			writeItem(packet, item);
			packet.writeD(item.getObjectId());
		}
		return true;
	}
}
