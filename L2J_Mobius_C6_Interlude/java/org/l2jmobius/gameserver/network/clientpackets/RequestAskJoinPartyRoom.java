/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExAskJoinPartyRoom;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * Format: (ch) S
 * @author -Wooden-
 */
public class RequestAskJoinPartyRoom implements IClientIncomingPacket
{
	private static String _name;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_name = packet.readS();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		// Send PartyRoom invite request (with activeChar) name to the target
		final Player target = World.getInstance().getPlayer(_name);
		if (target != null)
		{
			if (!target.isProcessingRequest())
			{
				player.onTransactionRequest(target);
				target.sendPacket(new ExAskJoinPartyRoom(player.getName()));
			}
			else
			{
				player.sendPacket(new SystemMessage(SystemMessageId.S1_IS_BUSY_PLEASE_TRY_AGAIN_LATER).addString(target.getName()));
			}
		}
		else
		{
			player.sendPacket(SystemMessageId.THAT_PLAYER_IS_NOT_ONLINE);
		}
	}
}
