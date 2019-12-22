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

public class TradeStart extends ServerBasePacket
{
	private final PlayerInstance _me;
	private final List<ItemInstance> _tradelist = new ArrayList<>();
	
	public TradeStart(PlayerInstance me)
	{
		_me = me;
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0x2E);
		writeD(_me.getTransactionRequester().getObjectId());
		for (ItemInstance item : _me.getInventory().getItems())
		{
			if (item.isEquipped() || (item.getItem().getType2() == 3))
			{
				continue;
			}
			_tradelist.add(item);
		}
		final int count = _tradelist.size();
		writeH(count);
		for (int i = 0; i < count; ++i)
		{
			final ItemInstance temp = _tradelist.get(i);
			final int type = temp.getItem().getType1();
			writeH(type);
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getCount());
			writeH(temp.getItem().getType2());
			writeH(0);
			writeD(temp.getItem().getBodyPart());
			writeH(temp.getEnchantLevel());
			writeH(0);
			writeH(0);
		}
	}
}
