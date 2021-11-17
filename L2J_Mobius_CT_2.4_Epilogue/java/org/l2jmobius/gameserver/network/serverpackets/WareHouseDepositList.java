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
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class WareHouseDepositList implements IClientOutgoingPacket
{
	public static final int PRIVATE = 1;
	public static final int CLAN = 4;
	public static final int CASTLE = 3; // not sure
	public static final int FREIGHT = 1;
	private final long _playerAdena;
	private final List<Item> _items = new ArrayList<>();
	/**
	 * <ul>
	 * <li>0x01-Private Warehouse</li>
	 * <li>0x02-Clan Warehouse</li>
	 * <li>0x03-Castle Warehouse</li>
	 * <li>0x04-Warehouse</li>
	 * </ul>
	 */
	private final int _whType;
	
	public WareHouseDepositList(Player player, int type)
	{
		_whType = type;
		_playerAdena = player.getAdena();
		
		final boolean isPrivate = _whType == PRIVATE;
		for (Item temp : player.getInventory().getAvailableItems(true, isPrivate, false))
		{
			if ((temp != null) && temp.isDepositable(isPrivate))
			{
				_items.add(temp);
			}
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.WAREHOUSE_DEPOSIT_LIST.writeId(packet);
		packet.writeH(_whType);
		packet.writeQ(_playerAdena);
		packet.writeH(_items.size());
		
		for (Item item : _items)
		{
			packet.writeH(item.getItem().getType1());
			packet.writeD(item.getObjectId());
			packet.writeD(item.getId());
			packet.writeQ(item.getCount());
			packet.writeH(item.getItem().getType2());
			packet.writeH(item.getCustomType1());
			packet.writeD(item.getItem().getBodyPart());
			packet.writeH(item.getEnchantLevel());
			packet.writeH(0x00);
			packet.writeH(item.getCustomType2());
			packet.writeD(item.getObjectId());
			if (item.isAugmented())
			{
				packet.writeD(0x0000FFFF & item.getAugmentation().getAugmentationId());
				packet.writeD(item.getAugmentation().getAugmentationId() >> 16);
			}
			else
			{
				packet.writeQ(0x00);
			}
			
			packet.writeH(item.getAttackElementType());
			packet.writeH(item.getAttackElementPower());
			for (byte i = 0; i < 6; i++)
			{
				packet.writeH(item.getElementDefAttr(i));
			}
			
			packet.writeD(item.getMana());
			// T2
			packet.writeD(item.isTimeLimitedItem() ? (int) (item.getRemainingTime() / 1000) : -1);
			
			for (int op : item.getEnchantOptions())
			{
				packet.writeH(op);
			}
		}
		return true;
	}
}
