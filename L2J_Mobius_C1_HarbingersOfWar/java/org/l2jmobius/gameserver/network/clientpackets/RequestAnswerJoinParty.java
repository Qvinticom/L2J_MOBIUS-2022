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
import org.l2jmobius.gameserver.network.serverpackets.JoinParty;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class RequestAnswerJoinParty extends ClientBasePacket
{
	private static final String _C__2A_REQUESTANSWERPARTY = "[C] 2A RequestAnswerParty";
	
	public RequestAnswerJoinParty(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		final int response = readD();
		final PlayerInstance player = client.getActiveChar();
		final PlayerInstance requestor = player.getTransactionRequester();
		final JoinParty join = new JoinParty(response);
		requestor.sendPacket(join);
		if (response == 1)
		{
			player.joinParty(requestor.getParty());
		}
		else
		{
			final SystemMessage msg = new SystemMessage(SystemMessage.PLAYER_DECLINED);
			requestor.sendPacket(msg);
			if (requestor.getParty().getMemberCount() == 1)
			{
				requestor.setParty(null);
			}
		}
		player.setTransactionRequester(null);
		requestor.setTransactionRequester(null);
	}
	
	@Override
	public String getType()
	{
		return _C__2A_REQUESTANSWERPARTY;
	}
}
