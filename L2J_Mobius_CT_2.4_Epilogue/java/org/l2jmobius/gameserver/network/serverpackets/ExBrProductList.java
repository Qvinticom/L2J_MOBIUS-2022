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
package org.l2jmobius.gameserver.network.serverpackets;

import java.util.Collection;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.data.xml.PrimeShopData;
import org.l2jmobius.gameserver.model.holders.PrimeShopProductHolder;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author Mobius
 */
public class ExBrProductList implements IClientOutgoingPacket
{
	private final Collection<PrimeShopProductHolder> _itemList = PrimeShopData.getInstance().getAllItems();
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_BR_PRODUCT_LIST.writeId(packet);
		packet.writeD(_itemList.size());
		for (PrimeShopProductHolder product : _itemList)
		{
			final int category = product.getCategory();
			packet.writeD(product.getProductId()); // product id
			packet.writeH(category); // category id
			packet.writeD(product.getPrice()); // points
			switch (category)
			{
				case 6:
				{
					packet.writeD(1); // event
					break;
				}
				case 7:
				{
					packet.writeD(2); // best
					break;
				}
				case 8:
				{
					packet.writeD(3); // event & best
					break;
				}
				default:
				{
					packet.writeD(0); // normal
					break;
				}
			}
			packet.writeD(0); // start sale
			packet.writeD(0); // end sale
			packet.writeC(0); // day week
			packet.writeC(0); // start hour
			packet.writeC(0); // start min
			packet.writeC(0); // end hour
			packet.writeC(0); // end min
			packet.writeD(0); // current stock
			packet.writeD(0); // max stock
		}
		return true;
	}
}
