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
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * sample 06 8f19904b 2522d04b 00000000 80 950c0000 4af50000 08f2ffff 0000 - 0 damage (missed 0x80) 06 85071048 bc0e504b 32000000 10 fc41ffff fd240200 a6f5ffff 0100 bc0e504b 33000000 10 3.... format dddc dddh (ddc)
 * @version $Revision: 1.1.6.2 $ $Date: 2005/03/27 15:29:39 $
 */
public class MonRaceInfo implements IClientOutgoingPacket
{
	private final int _unknown1;
	private final int _unknown2;
	private final Npc[] _monsters;
	private final int[][] _speeds;
	
	public MonRaceInfo(int unknown1, int unknown2, Npc[] monsters, int[][] speeds)
	{
		/*
		 * -1 0 to initial the race 0 15322 to start race 13765 -1 in middle of race -1 0 to end the race
		 */
		_unknown1 = unknown1;
		_unknown2 = unknown2;
		_monsters = monsters;
		_speeds = speeds;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.MON_RACE_INFO.writeId(packet);
		
		packet.writeD(_unknown1);
		packet.writeD(_unknown2);
		packet.writeD(8);
		
		for (int i = 0; i < 8; i++)
		{
			packet.writeD(_monsters[i].getObjectId()); // npcObjectID
			packet.writeD(_monsters[i].getTemplate().getDisplayId() + 1000000); // npcID
			packet.writeD(14107); // origin X
			packet.writeD(181875 + (58 * (7 - i))); // origin Y
			packet.writeD(-3566); // origin Z
			packet.writeD(12080); // end X
			packet.writeD(181875 + (58 * (7 - i))); // end Y
			packet.writeD(-3566); // end Z
			packet.writeF(_monsters[i].getTemplate().getCollisionHeight()); // coll. height
			packet.writeF(_monsters[i].getTemplate().getCollisionRadius()); // coll. radius
			packet.writeD(120); // ?? unknown
			for (int j = 0; j < 20; j++)
			{
				if (_unknown1 == 0)
				{
					packet.writeC(_speeds[i][j]);
				}
				else
				{
					packet.writeC(0);
				}
			}
			packet.writeD(0);
		}
		
		return true;
	}
}
