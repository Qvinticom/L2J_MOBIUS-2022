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
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.ChangeWaitType;
import org.l2jmobius.gameserver.network.serverpackets.PrivateStoreMsgBuy;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;

public class SetPrivateStoreListBuy extends ClientBasePacket
{
	private static final String _C__91_SETPRIVATESTORELISTBUY = "[C] 91 SetPrivateStoreListSell";
	
	public SetPrivateStoreListBuy(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		int count = readD();
		if (count <= 10)
		{
			PlayerInstance player = client.getActiveChar();
			// TradeList tradelist = player.getTradeList();
			player.setBuyList(new ArrayList<>());
			List<TradeItem> listbuy = player.getBuyList();
			int cost = 0;
			for (int x = 0; x < count; ++x)
			{
				TradeItem temp = new TradeItem();
				temp.setItemId(readD());
				readH();
				temp.setCount(readD());
				temp.setOwnersPrice(readD());
				listbuy.add(temp);
				cost += temp.getOwnersPrice() * temp.getCount();
			}
			if (count == 0)
			{
				listbuy = null;
			}
			if (cost > player.getAdena())
			{
				count = 0;
				SystemMessage msg = new SystemMessage(SystemMessage.THE_PURCHASE_PRICE_IS_HIGHER_THAN_MONEY);
				player.sendPacket(msg);
			}
			if (count != 0)
			{
				player.setPrivateStoreType(3);
				player.sendPacket(new ChangeWaitType(player, 0));
				player.broadcastPacket(new ChangeWaitType(player, 0));
				player.sendPacket(new UserInfo(player));
				player.broadcastPacket(new UserInfo(player));
				player.sendPacket(new PrivateStoreMsgBuy(player));
				player.broadcastPacket(new PrivateStoreMsgBuy(player));
			}
			else
			{
				player.setPrivateStoreType(0);
				player.sendPacket(new UserInfo(player));
				player.broadcastPacket(new UserInfo(player));
			}
		}
		else
		{
			PlayerInstance player = client.getActiveChar();
			player.setPrivateStoreType(0);
			player.sendPacket(new UserInfo(player));
			player.broadcastPacket(new UserInfo(player));
		}
	}
	
	@Override
	public String getType()
	{
		return _C__91_SETPRIVATESTORELISTBUY;
	}
}
