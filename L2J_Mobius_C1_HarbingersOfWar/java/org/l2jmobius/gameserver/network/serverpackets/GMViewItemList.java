/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.gameserver.network.serverpackets;

import java.util.Collection;

import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;

public class GMViewItemList extends ServerBasePacket
{
	private final Collection<Item> _items;
	private final String _playerName;
	
	public GMViewItemList(Player cha)
	{
		_items = cha.getInventory().getItems();
		_playerName = cha.getName();
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xAD);
		writeS(_playerName);
		writeH(1);
		writeH(_items.size());
		for (Item item : _items)
		{
			writeH(item.getItem().getType1());
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeD(item.getCount());
			writeH(item.getItem().getType2());
			writeH(255);
			if (item.isEquipped())
			{
				writeH(1);
			}
			else
			{
				writeH(0);
			}
			writeD(item.getItem().getBodyPart());
			writeH(item.getEnchantLevel());
			writeH(0);
		}
	}
}
