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
package com.l2jmobius.gameserver.network.serverpackets;

import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

/**
 * Support for "Chat with Friends" dialog. Format: ch (hdSdh) h: Total Friend Count h: Unknown d: Player Object ID S: Friend Name d: Online/Offline h: Unknown
 * @author Tempy
 */
public class FriendList extends L2GameServerPacket
{
	// private static Logger _log = Logger.getLogger(FriendList.class.getName());
	private static final String _S__FA_FRIENDLIST = "[S] FA FriendList";
	
	private final L2PcInstance _cha;
	
	public FriendList(L2PcInstance cha)
	{
		_cha = cha;
	}
	
	@Override
	protected final void writeImpl()
	{
		final L2PcInstance _activeChar = getClient().getActiveChar();
		if (_activeChar == null)
		{
			return;
		}
		
		writeC(0xfa);
		
		if (_activeChar.getFriendList().size() > 0)
		{
			writeH(_activeChar.getFriendList().size());
			
			for (final L2PcInstance.Friend friend : _activeChar.getFriendList())
			
			{
				if (friend.getObjectId() == _cha.getObjectId())
				{
					if (!friend.getName().equals(_cha.getName()))
					{
						friend.setName(_cha.getName());
					}
					friend.setOnline(_cha.isOnline());
				}
				
				writeH(0); // ??
				writeD(friend.getObjectId());
				writeS(friend.getName());
				
				writeD(friend.isOnline()); // online status
				
				writeH(0); // ??
			}
		}
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.serverpackets.L2GameServerPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FA_FRIENDLIST;
	}
}