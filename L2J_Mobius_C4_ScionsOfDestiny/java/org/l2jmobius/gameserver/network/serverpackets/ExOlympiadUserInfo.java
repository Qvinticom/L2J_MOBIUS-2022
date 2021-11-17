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

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 * @author godson
 */
public class ExOlympiadUserInfo implements IClientOutgoingPacket
{
	private final int _side;
	private final Player _player;
	
	/**
	 * @param player
	 * @param side (1 = right, 2 = left)
	 */
	public ExOlympiadUserInfo(Player player, int side)
	{
		_player = player;
		_side = side;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		if (_player == null)
		{
			return false;
		}
		OutgoingPackets.EX_OLYMPIAD_USER_INFO.writeId(packet);
		packet.writeC(_side);
		packet.writeD(_player.getObjectId());
		packet.writeS(_player.getName());
		packet.writeD(_player.getClassId().getId());
		packet.writeD((int) _player.getCurrentHp());
		packet.writeD(_player.getMaxHp());
		packet.writeD((int) _player.getCurrentCp());
		packet.writeD(_player.getMaxCp());
		return true;
	}
}
