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

import java.util.logging.Logger;

import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.SendTradeRequest;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class TradeRequest extends ClientBasePacket
{
	private static final Logger _log = Logger.getLogger(TradeRequest.class.getName());
	
	public TradeRequest(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		final int objectId = readD();
		final PlayerInstance player = client.getActiveChar();
		final World world = World.getInstance();
		final WorldObject target = world.findObject(objectId);
		if (!(target instanceof PlayerInstance) || (target.getObjectId() != objectId))
		{
			player.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
			return;
		}
		if (client.getActiveChar().getTransactionRequester() != null)
		{
			player.sendPacket(new SystemMessage(SystemMessage.ALREADY_TRADING));
			return;
		}
		final PlayerInstance pcTarget = (PlayerInstance) target;
		if (player.knownsObject(target) && !pcTarget.isTransactionInProgress())
		{
			pcTarget.setTransactionRequester(player);
			player.setTransactionRequester(pcTarget);
			pcTarget.sendPacket(new SendTradeRequest(player.getObjectId()));
			final SystemMessage sm = new SystemMessage(SystemMessage.REQUEST_S1_FOR_TRADE);
			sm.addString(pcTarget.getName());
			player.sendPacket(sm);
		}
		else
		{
			final SystemMessage sm = new SystemMessage(SystemMessage.S1_IS_BUSY_TRY_LATER);
			sm.addString(pcTarget.getName());
			player.sendPacket(sm);
			_log.info("transaction already in progress.");
		}
	}
}
