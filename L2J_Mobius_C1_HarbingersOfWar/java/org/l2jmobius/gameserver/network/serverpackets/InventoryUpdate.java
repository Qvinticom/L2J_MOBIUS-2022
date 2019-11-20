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

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;

public class InventoryUpdate extends ServerBasePacket
{
	private static final String _S__37_INVENTORYUPDATE = "[S] 37 InventoryUpdate";
	private final List<ItemInstance> _items;
	// private boolean _showWindow;
	
	public InventoryUpdate()
	{
		_items = new ArrayList<>();
	}
	
	public InventoryUpdate(List<ItemInstance> items)
	{
		_items = items;
	}
	
	public void addNewItem(ItemInstance item)
	{
		item.setLastChange(1);
		_items.add(item);
	}
	
	public void addModifiedItem(ItemInstance item)
	{
		item.setLastChange(2);
		_items.add(item);
	}
	
	public void addRemovedItem(ItemInstance item)
	{
		item.setLastChange(3);
		_items.add(item);
	}
	
	@Override
	public byte[] getContent()
	{
		writeC(55);
		int count = _items.size();
		writeH(count);
		for (int i = 0; i < count; ++i)
		{
			ItemInstance temp = _items.get(i);
			// _log.fine("oid:" + Integer.toHexString(temp.getObjectId()) + " item:" + temp.getItem().getName() + " last change:" + temp.getLastChange());
			writeH(temp.getLastChange());
			writeH(temp.getItem().getType1());
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getCount());
			writeH(temp.getItem().getType2());
			writeH(0);
			if (temp.isEquipped())
			{
				writeH(1);
			}
			else
			{
				writeH(0);
			}
			writeD(temp.getItem().getBodyPart());
			writeH(temp.getEnchantLevel());
			writeH(0);
		}
		return getBytes();
	}
	
	@Override
	public String getType()
	{
		return _S__37_INVENTORYUPDATE;
	}
}
