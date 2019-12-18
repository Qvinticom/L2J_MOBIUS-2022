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

import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.AskJoinParty;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class RequestJoinParty extends ClientBasePacket
{
	private static final Logger _log = Logger.getLogger(RequestJoinParty.class.getName());
	
	public RequestJoinParty(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		final int id = readD();
		final int itemDistribution = readD();
		final PlayerInstance target = (PlayerInstance) World.getInstance().findObject(id);
		final PlayerInstance requestor = client.getActiveChar();
		if (requestor.isTransactionInProgress())
		{
			final SystemMessage msg = new SystemMessage(SystemMessage.WAITING_FOR_REPLY);
			requestor.sendPacket(msg);
			return;
		}
		if (target.isInParty())
		{
			final SystemMessage msg = new SystemMessage(SystemMessage.S1_IS_ALREADY_IN_PARTY);
			msg.addString(target.getName());
			requestor.sendPacket(msg);
			return;
		}
		if (!client.getActiveChar().isInParty())
		{
			createNewParty(client, itemDistribution, target, requestor);
		}
		else
		{
			addTargetToParty(client, itemDistribution, target, requestor);
		}
	}
	
	private void addTargetToParty(ClientThread client, int itemDistribution, PlayerInstance target, PlayerInstance requestor)
	{
		if (requestor.getParty().getMemberCount() >= 9)
		{
			requestor.sendPacket(new SystemMessage(SystemMessage.PARTY_FULL));
			return;
		}
		if (!requestor.getParty().isLeader(requestor))
		{
			requestor.sendPacket(new SystemMessage(SystemMessage.ONLY_LEADER_CAN_INVITE));
			return;
		}
		if (target.getKnownPlayers().contains(requestor))
		{
			if (!target.isTransactionInProgress())
			{
				target.setTransactionRequester(requestor);
				requestor.setTransactionRequester(target);
				final AskJoinParty ask = new AskJoinParty(requestor.getObjectId(), itemDistribution);
				target.sendPacket(ask);
				final SystemMessage msg = new SystemMessage(SystemMessage.YOU_INVITED_S1_TO_PARTY);
				msg.addString(target.getName());
				requestor.sendPacket(msg);
			}
			else
			{
				final SystemMessage msg = new SystemMessage(SystemMessage.S1_IS_BUSY_TRY_LATER);
				requestor.sendPacket(msg);
				_log.warning(requestor.getName() + " already received a party invitation");
			}
		}
		else
		{
			_log.warning(client.getActiveChar().getName() + " invited someone who doesn't know him.");
		}
	}
	
	private void createNewParty(ClientThread client, int itemDistribution, PlayerInstance target, PlayerInstance requestor)
	{
		if (target.getKnownPlayers().contains(requestor))
		{
			if (!target.isTransactionInProgress())
			{
				requestor.setParty(new Party(requestor, itemDistribution == 1));
				target.setTransactionRequester(requestor);
				requestor.setTransactionRequester(target);
				final AskJoinParty ask = new AskJoinParty(requestor.getObjectId(), itemDistribution);
				target.sendPacket(ask);
				final SystemMessage msg = new SystemMessage(SystemMessage.YOU_INVITED_S1_TO_PARTY);
				msg.addString(target.getName());
				requestor.sendPacket(msg);
			}
			else
			{
				final SystemMessage msg = new SystemMessage(SystemMessage.S1_IS_BUSY_TRY_LATER);
				msg.addString(target.getName());
				requestor.sendPacket(msg);
				_log.warning(requestor.getName() + " already received a party invitation");
			}
		}
	}
}
