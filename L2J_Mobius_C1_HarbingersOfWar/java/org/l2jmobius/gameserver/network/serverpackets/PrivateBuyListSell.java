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

import org.l2jmobius.gameserver.model.TradeItem;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;

public class PrivateBuyListSell extends ServerBasePacket
{
	private final PlayerInstance _buyer;
	private final PlayerInstance _seller;
	
	public PrivateBuyListSell(PlayerInstance buyer, PlayerInstance seller)
	{
		_buyer = buyer;
		_seller = seller;
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xB4);
		writeD(_seller.getObjectId());
		writeD(_buyer.getAdena());
		final int count = _seller.getSellList().size();
		writeD(count);
		for (int i = 0; i < count; ++i)
		{
			final TradeItem temp2 = _seller.getSellList().get(i);
			writeD(0);
			writeD(temp2.getObjectId());
			writeD(temp2.getItemId());
			writeD(temp2.getCount());
			writeD(0);
			writeH(0);
			writeH(0);
			writeH(0);
			writeD(temp2.getOwnersPrice());
			writeD(temp2.getStorePrice());
		}
	}
}
