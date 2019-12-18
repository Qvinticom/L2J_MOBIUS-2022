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

import org.l2jmobius.gameserver.model.TradeList;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.SendTradeDone;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.TradeOtherAdd;
import org.l2jmobius.gameserver.network.serverpackets.TradeOwnAdd;

public class AddTradeItem extends ClientBasePacket
{
	public AddTradeItem(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		@SuppressWarnings("unused")
		final int tradeId = readD();
		final int objectId = readD();
		final int amount = readD();
		final PlayerInstance player = client.getActiveChar();
		final PlayerInstance requestor = player.getTransactionRequester();
		if (requestor.getTransactionRequester() != null)
		{
			final TradeList playerItemList = player.getTradeList();
			player.getTradeList().setConfirmedTrade(false);
			requestor.getTradeList().setConfirmedTrade(false);
			if (!playerItemList.getItems().isEmpty())
			{
				if (!playerItemList.contains(objectId))
				{
					final ItemInstance temp = new ItemInstance();
					temp.setObjectId(player.getInventory().getItem(objectId).getObjectId());
					temp.setCount(amount);
					playerItemList.addItem(temp);
					player.sendPacket(new TradeOwnAdd(player.getInventory().getItem(objectId), amount));
					requestor.sendPacket(new TradeOtherAdd(player.getInventory().getItem(objectId), amount));
				}
				else
				{
					ItemInstance tempTradeItem;
					final int InvItemCount = player.getInventory().getItem(objectId).getCount();
					if (InvItemCount != (tempTradeItem = playerItemList.getItem(objectId)).getCount())
					{
						if ((amount + tempTradeItem.getCount()) >= InvItemCount)
						{
							tempTradeItem.setCount(InvItemCount);
							player.sendPacket(new TradeOwnAdd(player.getInventory().getItem(objectId), amount));
							requestor.sendPacket(new TradeOtherAdd(player.getInventory().getItem(objectId), amount));
						}
						else
						{
							tempTradeItem.setCount(amount + tempTradeItem.getCount());
							player.sendPacket(new TradeOwnAdd(player.getInventory().getItem(objectId), amount));
							requestor.sendPacket(new TradeOtherAdd(player.getInventory().getItem(objectId), amount));
						}
					}
				}
			}
			else
			{
				final ItemInstance temp = new ItemInstance();
				temp.setObjectId(objectId);
				temp.setCount(amount);
				playerItemList.addItem(temp);
				player.sendPacket(new TradeOwnAdd(player.getInventory().getItem(objectId), amount));
				requestor.sendPacket(new TradeOtherAdd(player.getInventory().getItem(objectId), amount));
			}
		}
		else
		{
			player.sendPacket(new SendTradeDone(0));
			final SystemMessage msg = new SystemMessage(SystemMessage.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			player.sendPacket(msg);
			player.setTransactionRequester(null);
			requestor.getTradeList().getItems().clear();
			player.getTradeList().getItems().clear();
		}
	}
}
