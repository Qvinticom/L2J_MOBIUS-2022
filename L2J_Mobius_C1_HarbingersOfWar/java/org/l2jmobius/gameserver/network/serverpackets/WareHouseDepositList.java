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

public class WareHouseDepositList extends ServerBasePacket
{
	private final PlayerInstance _cha;
	private final int _money;
	
	public WareHouseDepositList(PlayerInstance cha)
	{
		_cha = cha;
		_money = cha.getAdena();
	}
	
	@Override
	public void writeImpl()
	{
		int i;
		writeC(0x53);
		writeD(_money);
		final List<ItemInstance> itemlist = new ArrayList<>();
		for (ItemInstance item : _cha.getInventory().getItems())
		{
			if (item.isEquipped() || (item.getItem().getType2() == 3))
			{
				continue;
			}
			itemlist.add(item);
		}
		final int count = itemlist.size();
		writeH(count);
		for (i = 0; i < count; ++i)
		{
			final ItemInstance temp = itemlist.get(i);
			writeH(temp.getItem().getType1());
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getCount());
			writeH(temp.getItem().getType2());
			writeH(100);
			writeD(400);
			writeH(temp.getEnchantLevel());
			writeH(300);
			writeH(200);
			writeD(temp.getItemId());
		}
	}
}
