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

import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 * @version $Revision: 1.3.4.3 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestFriendList extends L2GameClientPacket
{
	// private static Logger _log = Logger.getLogger(RequestFriendList.class.getName());
	private static final String _C__60_REQUESTFRIENDLIST = "[C] 60 RequestFriendList";
	
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		SystemMessage sm;
		
		// ======<Friend List>======
		activeChar.sendPacket(new SystemMessage(SystemMessage.FRIEND_LIST_HEAD));
		
		for (final L2PcInstance.Friend friend : activeChar.getFriendList())
		{
			final L2PcInstance onlineFriend = L2World.getInstance().getPlayer(friend.getName());
			if (onlineFriend == null)
			{
				// (Currently: Offline)
				sm = new SystemMessage(SystemMessage.S1_OFFLINE);
				sm.addString(friend.getName());
			}
			else
			{
				// (Currently: Online)
				sm = new SystemMessage(SystemMessage.S1_ONLINE);
				sm.addString(friend.getName());
			}
			
			activeChar.sendPacket(sm);
		}
		
		// =========================
		activeChar.sendPacket(new SystemMessage(SystemMessage.FRIEND_LIST_FOOT));
	}
	
	@Override
	public String getType()
	{
		return _C__60_REQUESTFRIENDLIST;
	}
}