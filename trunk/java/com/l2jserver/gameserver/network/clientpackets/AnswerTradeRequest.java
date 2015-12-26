/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.network.clientpackets;

import com.l2jserver.Config;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ActionFailed;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;
import com.l2jserver.gameserver.network.serverpackets.TradeDone;

/**
 * This class ...
 * @version $Revision: 1.5.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public final class AnswerTradeRequest extends L2GameClientPacket
{
	private static final String _C__55_ANSWERTRADEREQUEST = "[C] 55 AnswerTradeRequest";
	
	private int _response;
	
	@Override
	protected void readImpl()
	{
		_response = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2PcInstance partner = player.getActiveRequester();
		if (partner == null)
		{
			// Trade partner not found, cancel trade
			player.sendPacket(new TradeDone(0));
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THAT_PLAYER_IS_NOT_ONLINE));
			player.setActiveRequester(null);
			return;
		}
		else if (L2World.getInstance().getPlayer(partner.getObjectId()) == null)
		{
			// Trade partner not found, cancel trade
			player.sendPacket(new TradeDone(0));
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THAT_PLAYER_IS_NOT_ONLINE));
			player.setActiveRequester(null);
			return;
		}
		
		if (Config.FACTION_SYSTEM_ENABLED && ((player.isEvil() && partner.isGood()) || (player.isGood() && partner.isEvil())))
		{
			player.sendPacket(new TradeDone(0));
			player.sendMessage("You cannot trade with different team members.");
			player.setActiveRequester(null);
			return;
		}
		
		if ((_response == 1) && !partner.isRequestExpired())
		{
			player.startTrade(partner);
		}
		else
		{
			final SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_DENIED_YOUR_REQUEST_TO_TRADE);
			msg.addString(player.getName());
			partner.sendPacket(msg);
		}
		
		// Clears requesting status
		player.setActiveRequester(null);
		partner.onTransactionResponse();
	}
	
	@Override
	public String getType()
	{
		return _C__55_ANSWERTRADEREQUEST;
	}
}
