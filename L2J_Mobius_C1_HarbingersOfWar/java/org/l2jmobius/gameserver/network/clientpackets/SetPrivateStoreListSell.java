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
import org.l2jmobius.gameserver.network.serverpackets.PrivateStoreMsgSell;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;

public class SetPrivateStoreListSell extends ClientBasePacket
{
	public SetPrivateStoreListSell(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		final int count = readD();
		final PlayerInstance player = client.getActiveChar();
		// TradeList tradelist = player.getTradeList();
		player.setSellList(new ArrayList<>());
		List<TradeItem> listsell = player.getSellList();
		for (int x = 0; x < count; ++x)
		{
			final TradeItem temp = new TradeItem();
			temp.setObjectId(readD());
			temp.setCount(readD());
			temp.setOwnersPrice(readD());
			temp.setItemId(player.getInventory().getItem(temp.getObjectId()).getItemId());
			listsell.add(temp);
		}
		if (count == 0)
		{
			listsell = null;
		}
		if (count != 0)
		{
			player.setPrivateStoreType(1);
			player.sendPacket(new ChangeWaitType(player, 0));
			player.broadcastPacket(new ChangeWaitType(player, 0));
			player.sendPacket(new UserInfo(player));
			player.broadcastPacket(new UserInfo(player));
			player.sendPacket(new PrivateStoreMsgSell(player));
			player.broadcastPacket(new PrivateStoreMsgSell(player));
		}
		else
		{
			player.setPrivateStoreType(0);
			player.sendPacket(new UserInfo(player));
			player.broadcastPacket(new UserInfo(player));
		}
	}
}
