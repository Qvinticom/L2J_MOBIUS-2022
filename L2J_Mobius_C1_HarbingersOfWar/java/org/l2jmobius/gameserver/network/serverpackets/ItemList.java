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

public class ItemList extends ServerBasePacket
{
	private final Collection<Item> _items;
	private final boolean _showWindow;
	
	public ItemList(Player cha, boolean showWindow)
	{
		_items = cha.getInventory().getItems();
		_showWindow = showWindow;
	}
	
	public ItemList(Collection<Item> items, boolean showWindow)
	{
		_items = items;
		_showWindow = showWindow;
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0x27);
		if (_showWindow)
		{
			writeH(1);
		}
		else
		{
			writeH(0);
		}
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
