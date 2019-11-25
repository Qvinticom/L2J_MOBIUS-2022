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
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.SendTradeDone;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.TradeStart;

public class AnswerTradeRequest extends ClientBasePacket
{
	public AnswerTradeRequest(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		final int response = readD();
		final PlayerInstance player = client.getActiveChar();
		final PlayerInstance requestor = player.getTransactionRequester();
		if (requestor.getTransactionRequester() != null)
		{
			if (response == 1)
			{
				SystemMessage msg = new SystemMessage(SystemMessage.BEGIN_TRADE_WITH_S1);
				msg.addString(player.getName());
				requestor.sendPacket(msg);
				requestor.sendPacket(new TradeStart(requestor));
				if (requestor.getTradeList() == null)
				{
					requestor.setTradeList(new TradeList(0));
				}
				msg = new SystemMessage(SystemMessage.BEGIN_TRADE_WITH_S1);
				msg.addString(requestor.getName());
				player.sendPacket(msg);
				player.sendPacket(new TradeStart(player));
				if (player.getTradeList() == null)
				{
					player.setTradeList(new TradeList(0));
				}
			}
			else
			{
				final SystemMessage msg = new SystemMessage(SystemMessage.S1_DENIED_TRADE_REQUEST);
				msg.addString(player.getName());
				requestor.sendPacket(msg);
				requestor.setTransactionRequester(null);
				player.setTransactionRequester(null);
			}
		}
		else if (response != 0)
		{
			player.sendPacket(new SendTradeDone(0));
			final SystemMessage msg = new SystemMessage(SystemMessage.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			player.sendPacket(msg);
			player.setTransactionRequester(null);
		}
	}
}
