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
package org.l2jmobius.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.gameserver.model.TradeItem;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.ChangeWaitType;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;

public class SendPrivateStoreBuyBuyList extends ClientBasePacket
{
	public SendPrivateStoreBuyBuyList(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		final int buyerID = readD();
		final int count = readD();
		final World world = World.getInstance();
		final PlayerInstance seller = client.getActiveChar();
		final PlayerInstance buyer = (PlayerInstance) world.findObject(buyerID);
		final List<TradeItem> buyerlist = buyer.getBuyList();
		final List<TradeItem> sellerlist = new ArrayList<>();
		int cost = 0;
		for (int i = 0; i < count; ++i)
		{
			final TradeItem temp = new TradeItem();
			temp.setObjectId(readD());
			temp.setItemId(readD());
			readH();
			temp.setCount(readD());
			temp.setOwnersPrice(readD());
			cost += temp.getOwnersPrice() * temp.getCount();
			sellerlist.add(temp);
		}
		if ((buyer.getAdena() >= cost) && (count > 0) && (buyer.getPrivateStoreType() == 3))
		{
			buyer.getTradeList().BuySellItems(buyer, buyerlist, seller, sellerlist);
			buyer.getTradeList().updateBuyList(buyer, buyerlist);
			if (buyer.getBuyList().isEmpty())
			{
				buyer.setPrivateStoreType(0);
				buyer.sendPacket(new ChangeWaitType(buyer, 1));
				buyer.broadcastPacket(new ChangeWaitType(buyer, 1));
				buyer.sendPacket(new UserInfo(buyer));
				buyer.broadcastPacket(new UserInfo(buyer));
			}
		}
		else
		{
			buyer.getTradeList().updateBuyList(buyer, buyerlist);
		}
	}
}
