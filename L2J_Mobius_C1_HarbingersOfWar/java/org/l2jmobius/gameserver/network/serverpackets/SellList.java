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
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;

public class SellList extends ServerBasePacket
{
	private static final String _S__1C_SELLLIST = "[S] 1C SellList";
	private final PlayerInstance _char;
	private final int _money;
	private final List<ItemInstance> _selllist = new ArrayList<>();
	
	public SellList(PlayerInstance player)
	{
		_char = player;
		_money = _char.getAdena();
	}
	
	@Override
	public byte[] getContent()
	{
		ItemInstance item;
		int i;
		writeC(28);
		writeD(_money);
		writeD(0);
		ItemInstance[] inventory = _char.getInventory().getItems();
		int count = _char.getInventory().getSize();
		for (i = 0; i < count; ++i)
		{
			item = inventory[i];
			if (item.isEquipped() || (item.getItemId() == 57) || (item.getItem().getType2() == 3))
			{
				continue;
			}
			_selllist.add(item);
			// _log.fine("item added to selllist: " + item.getItem().getName());
		}
		count = _selllist.size();
		writeH(count);
		for (i = 0; i < count; ++i)
		{
			item = _selllist.get(i);
			writeH(item.getItem().getType1());
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeD(item.getCount());
			writeH(item.getItem().getType2());
			writeH(0);
			if (item.getItem().getType1() < 4)
			{
				writeD(item.getItem().getBodyPart());
				writeH(item.getEnchantLevel());
				writeH(0);
				writeH(0);
			}
			writeD(item.getItem().getReferencePrice() / 2);
		}
		return getBytes();
	}
	
	@Override
	public String getType()
	{
		return _S__1C_SELLLIST;
	}
}
