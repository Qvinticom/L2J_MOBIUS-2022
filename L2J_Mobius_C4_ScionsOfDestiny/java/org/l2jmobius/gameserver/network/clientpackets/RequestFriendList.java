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
import org.l2jmobius.gameserver.data.sql.CharNameTable;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class RequestFriendList implements IClientIncomingPacket
{
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
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
		
		// ======<Friend List>======
		player.sendPacket(SystemMessageId.FRIENDS_LIST);
		
		for (int id : player.getFriendList())
		{
			final String friendName = CharNameTable.getInstance().getPlayerName(id);
			if (friendName == null)
			{
				continue;
			}
			
			final Player friend = World.getInstance().getPlayer(id);
			player.sendPacket(SystemMessage.getSystemMessage(((friend == null) || !friend.isOnline()) ? SystemMessageId.S1_CURRENTLY_OFFLINE : SystemMessageId.S1_CURRENTLY_ONLINE).addString(friendName));
		}
		
		// =========================
		player.sendPacket(SystemMessageId.EMPTY_3);
	}
}
