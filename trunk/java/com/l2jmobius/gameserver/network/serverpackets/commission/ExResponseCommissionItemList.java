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
package com.l2jmobius.gameserver.network.serverpackets.commission;

import java.util.List;

import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;
import com.l2jmobius.gameserver.network.serverpackets.AbstractItemPacket;

/**
 * @author NosBit
 */
public class ExResponseCommissionItemList extends AbstractItemPacket
{
	private final List<L2ItemInstance> _items;
	
	public ExResponseCommissionItemList(List<L2ItemInstance> items)
	{
		_items = items;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xF3);
		writeD(_items.size());
		for (L2ItemInstance itemInstance : _items)
		{
			writeItem(itemInstance);
		}
	}
}
