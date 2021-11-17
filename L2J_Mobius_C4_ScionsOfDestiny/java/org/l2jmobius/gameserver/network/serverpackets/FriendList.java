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
package org.l2jmobius.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.data.sql.CharNameTable;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * Support for "Chat with Friends" dialog.
 * @author Tempy
 */
public class FriendList implements IClientOutgoingPacket
{
	private final List<FriendInfo> _info;
	
	private static class FriendInfo
	{
		int _objId;
		String _name;
		boolean _online;
		
		public FriendInfo(int objId, String name, boolean online)
		{
			_objId = objId;
			_name = name;
			_online = online;
		}
	}
	
	public FriendList(Player player)
	{
		_info = new ArrayList<>(player.getFriendList().size());
		for (int objId : player.getFriendList())
		{
			final String name = CharNameTable.getInstance().getPlayerName(objId);
			final Player player1 = World.getInstance().getPlayer(objId);
			_info.add(new FriendInfo(objId, name, ((player1 != null) && player1.isOnline())));
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.FRIEND_LIST.writeId(packet);
		packet.writeD(_info.size());
		for (FriendInfo friend : _info)
		{
			packet.writeD(friend._objId);
			packet.writeS(friend._name);
			packet.writeD(friend._online ? 0x01 : 0x00);
			packet.writeD(friend._online ? friend._objId : 0x00);
		}
		return true;
	}
}