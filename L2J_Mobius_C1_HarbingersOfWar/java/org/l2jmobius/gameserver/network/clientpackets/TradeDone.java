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

import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.SendTradeDone;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class TradeDone extends ClientBasePacket
{
	private static final String _C__17_TRADEDONE = "[C] 17 TradeDone";
	
	public TradeDone(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		int response = readD();
		PlayerInstance player = client.getActiveChar();
		PlayerInstance requestor = player.getTransactionRequester();
		if (requestor.getTransactionRequester() != null)
		{
			if (response == 1)
			{
				player.getTradeList().setConfirmedTrade(true);
				if (requestor.getTradeList().hasConfirmed())
				{
					player.getTradeList().tradeItems(player, requestor);
					requestor.getTradeList().tradeItems(requestor, player);
					requestor.sendPacket(new SendTradeDone(1));
					player.sendPacket(new SendTradeDone(1));
					requestor.getTradeList().getItems().clear();
					player.getTradeList().getItems().clear();
					SystemMessage msg = new SystemMessage(SystemMessage.TRADE_SUCCESSFUL);
					requestor.sendPacket(msg);
					player.sendPacket(msg);
					requestor.setTransactionRequester(null);
					player.setTransactionRequester(null);
				}
				else
				{
					SystemMessage msg = new SystemMessage(SystemMessage.S1_CONFIRMED_TRADE);
					msg.addString(player.getName());
					requestor.sendPacket(msg);
				}
			}
			else
			{
				player.sendPacket(new SendTradeDone(0));
				requestor.sendPacket(new SendTradeDone(0));
				player.setTradeList(null);
				requestor.setTradeList(null);
				SystemMessage msg = new SystemMessage(SystemMessage.S1_CANCELED_TRADE);
				msg.addString(player.getName());
				requestor.sendPacket(msg);
				requestor.setTransactionRequester(null);
				player.setTransactionRequester(null);
			}
		}
		else
		{
			player.sendPacket(new SendTradeDone(0));
			SystemMessage msg = new SystemMessage(SystemMessage.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			player.sendPacket(msg);
			player.setTransactionRequester(null);
			requestor.setTradeList(null);
			player.setTradeList(null);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__17_TRADEDONE;
	}
}
