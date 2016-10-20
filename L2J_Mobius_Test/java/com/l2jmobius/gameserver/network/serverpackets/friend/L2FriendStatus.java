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
package com.l2jmobius.gameserver.network.serverpackets.friend;

import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.L2GameServerPacket;

/**
 * Support for "Chat with Friends" dialog. <br />
 * Inform player about friend online status change
 * @author JIV
 */
public class L2FriendStatus extends L2GameServerPacket
{
	public static final int MODE_OFFLINE = 0;
	public static final int MODE_ONLINE = 1;
	public static final int MODE_LEVEL = 2;
	public static final int MODE_CLASS = 3;
	
	private final int _type;
	private final int _objectId;
	private final int _classId;
	private final int _level;
	private final String _name;
	
	public L2FriendStatus(L2PcInstance player, int type)
	{
		_objectId = player.getObjectId();
		_classId = player.getActiveClassId();
		_level = player.getLevel();
		_name = player.getName();
		_type = type;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x59);
		writeD(_type);
		writeS(_name);
		switch (_type)
		{
			case MODE_OFFLINE:
			{
				writeD(_objectId);
				break;
			}
			case MODE_LEVEL:
			{
				writeD(_level);
				break;
			}
			case MODE_CLASS:
			{
				writeD(_classId);
				break;
			}
		}
	}
}
