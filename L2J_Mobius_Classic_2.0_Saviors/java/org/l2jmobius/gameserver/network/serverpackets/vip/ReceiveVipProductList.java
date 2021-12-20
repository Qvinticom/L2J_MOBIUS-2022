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
package org.l2jmobius.gameserver.network.serverpackets.vip;

import java.util.Collection;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.data.xml.PrimeShopData;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.primeshop.PrimeShopGroup;
import org.l2jmobius.gameserver.model.primeshop.PrimeShopItem;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

public class ReceiveVipProductList implements IClientOutgoingPacket
{
	private final Player _player;
	
	public ReceiveVipProductList(Player player)
	{
		_player = player;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		if (!Config.VIP_SYSTEM_ENABLED)
		{
			return false;
		}
		final Collection<PrimeShopGroup> products = PrimeShopData.getInstance().getPrimeItems().values();
		final PrimeShopGroup gift = PrimeShopData.getInstance().getVipGiftOfTier(_player.getVipTier());
		OutgoingPackets.RECIVE_VIP_PRODUCT_LIST.writeId(packet);
		packet.writeQ(_player.getAdena());
		packet.writeQ(_player.getGoldCoin()); // Gold Coin Amount
		packet.writeQ(_player.getSilverCoin()); // Silver Coin Amount
		packet.writeC(1); // Show Reward tab
		if (gift != null)
		{
			packet.writeD(products.size() + 1);
			writeProduct(gift, packet);
		}
		else
		{
			packet.writeD(products.size());
		}
		for (PrimeShopGroup product : products)
		{
			writeProduct(product, packet);
		}
		return true;
	}
	
	private void writeProduct(PrimeShopGroup product, PacketWriter buffer)
	{
		buffer.writeD(product.getBrId());
		buffer.writeC(product.getCat());
		buffer.writeC(product.getPaymentType());
		buffer.writeD(product.getPrice()); // L2 Coin | Gold Coin seems to use the same field based on payment type
		buffer.writeD(product.getSilverCoin());
		buffer.writeC(product.getPanelType()); // NEW - 6; HOT - 5 ... Unk
		buffer.writeC(product.getVipTier());
		buffer.writeC(10);
		buffer.writeC(product.getItems().size());
		for (PrimeShopItem item : product.getItems())
		{
			buffer.writeD(item.getId());
			buffer.writeD((int) item.getCount());
		}
	}
}
