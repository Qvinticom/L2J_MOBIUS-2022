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

import org.l2jmobius.gameserver.model.TradeList;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.ChangeWaitType;
import org.l2jmobius.gameserver.network.serverpackets.PrivateSellListBuy;

public class RequestPrivateStoreBuyManage extends ClientBasePacket
{
	private static final String _C__90_REQUESTPRIVATESTOREBUYMANAGE = "[C] 90 RequestPrivateStoreBuyManage";
	
	public RequestPrivateStoreBuyManage(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		final PlayerInstance player = client.getActiveChar();
		final int privatetype = player.getPrivateStoreType();
		if (privatetype == 0)
		{
			if (player.getWaitType() != 1)
			{
				player.setWaitType(1);
				player.sendPacket(new ChangeWaitType(player, 1));
				player.broadcastPacket(new ChangeWaitType(player, 1));
			}
			if (player.getTradeList() == null)
			{
				player.setTradeList(new TradeList(0));
			}
			if (player.getBuyList() == null)
			{
				player.setBuyList(new ArrayList<>());
			}
			player.getTradeList().updateBuyList(player, player.getBuyList());
			player.setPrivateStoreType(4);
			player.sendPacket(new PrivateSellListBuy(client.getActiveChar()));
		}
		if (privatetype == 3)
		{
			player.setPrivateStoreType(4);
			player.sendPacket(new PrivateSellListBuy(client.getActiveChar()));
			player.sendPacket(new ChangeWaitType(player, 1));
			player.broadcastPacket(new ChangeWaitType(player, 1));
		}
	}
	
	@Override
	public String getType()
	{
		return _C__90_REQUESTPRIVATESTOREBUYMANAGE;
	}
}
