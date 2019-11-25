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

import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.PetInstance;

public class PetItemList extends ServerBasePacket
{
	private final PetInstance _cha;
	private final Collection<ItemInstance> _items;
	
	public PetItemList(PetInstance cha)
	{
		_cha = cha;
		_items = _cha.getInventory().getItems();
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xCB);
		writeH(_items.size());
		for (ItemInstance item : _items)
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
