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
import com.l2jmobius.gameserver.network.serverpackets.AskJoinFriend;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestFriendInvite extends L2GameClientPacket
{
	private static final String _C__5E_REQUESTFRIENDINVITE = "[C] 5E RequestFriendInvite";
	// private static Logger _log = Logger.getLogger(RequestFriendInvite.class.getName());
	
	private String _name;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
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
		
		final L2PcInstance friend = L2World.getInstance().getPlayer(_name);
		
		if ((friend == null) || friend.getAppearance().getInvisible())
		{
			// Target is not found in the game.
			sm = new SystemMessage(SystemMessage.THE_USER_YOU_REQUESTED_IS_NOT_IN_GAME);
			activeChar.sendPacket(sm);
			sm = null;
			return;
		}
		else if (friend == activeChar)
		{
			// You cannot add yourself to your own friend list.
			sm = new SystemMessage(SystemMessage.YOU_CANNOT_ADD_YOURSELF_TO_OWN_FRIEND_LIST);
			activeChar.sendPacket(sm);
			sm = null;
			return;
		}
		
		if (activeChar.getFriend(friend.getName()) != null)
		{
			// Player already is in your friendlist
			sm = new SystemMessage(SystemMessage.S1_ALREADY_IN_FRIENDS_LIST);
			sm.addString(_name);
			activeChar.sendPacket(sm);
			sm = null;
			return;
		}
		
		if (!friend.isProcessingRequest())
		{
			// request to become friend
			activeChar.onTransactionRequest(friend);
			sm = new SystemMessage(SystemMessage.S1_REQUESTED_TO_BECOME_FRIENDS);
			sm.addString(activeChar.getName());
			friend.sendPacket(sm);
			friend.sendPacket(new AskJoinFriend(activeChar.getName()));
		}
		else
		{
			sm = new SystemMessage(SystemMessage.S1_IS_BUSY_TRY_LATER);
			sm.addString(_name);
			activeChar.sendPacket(sm);
		}
		sm = null;
	}
	
	@Override
	public String getType()
	{
		return _C__5E_REQUESTFRIENDINVITE;
	}
}