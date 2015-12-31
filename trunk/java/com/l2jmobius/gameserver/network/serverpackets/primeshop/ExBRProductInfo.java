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
package com.l2jmobius.gameserver.network.serverpackets.primeshop;

import com.l2jmobius.gameserver.model.primeshop.PrimeShopGroup;
import com.l2jmobius.gameserver.model.primeshop.PrimeShopItem;
import com.l2jmobius.gameserver.network.serverpackets.L2GameServerPacket;

/**
 * @author Gnacik
 */
public class ExBRProductInfo extends L2GameServerPacket
{
	private final PrimeShopGroup _item;
	
	public ExBRProductInfo(PrimeShopGroup item)
	{
		_item = item;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0xD8);
		writeD(_item.getBrId());
		writeD(_item.getPrice());
		writeD(_item.getItems().size());
		for (PrimeShopItem item : _item.getItems())
		{
			writeD(item.getId());
			writeD((int) item.getCount());
			writeD(item.getWeight());
			writeD(item.isTradable());
		}
	}
}
