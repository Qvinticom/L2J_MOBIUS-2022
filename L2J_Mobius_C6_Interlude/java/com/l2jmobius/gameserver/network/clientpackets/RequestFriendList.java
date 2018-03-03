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
package com.l2jmobius.gameserver.network.clientpackets;

import com.l2jmobius.gameserver.datatables.sql.CharNameTable;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public final class RequestFriendList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		// ======<Friend List>======
		activeChar.sendPacket(SystemMessageId.FRIEND_LIST_HEAD);
		
		for (int id : activeChar.getFriendList())
		{
			final String friendName = CharNameTable.getInstance().getPlayerName(id);
			if (friendName == null)
			{
				continue;
			}
			
			final L2PcInstance friend = L2World.getInstance().getPlayer(id);
			
			activeChar.sendPacket(SystemMessage.getSystemMessage(((friend == null) || (friend.isOnline() == 0)) ? SystemMessageId.S1_OFFLINE : SystemMessageId.S1_ONLINE).addString(friendName));
		}
		
		// =========================
		activeChar.sendPacket(SystemMessageId.FRIEND_LIST_FOOT);
	}
}
