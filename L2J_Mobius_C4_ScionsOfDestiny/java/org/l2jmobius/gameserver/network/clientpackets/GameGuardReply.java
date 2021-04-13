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
import org.l2jmobius.gameserver.network.GameClient;

/**
 * @author zabbix Lets drink to code! Unknown Packet: ca 0000: 45 00 01 00 1e 37 a2 f5 00 00 00 00 00 00 00 00 E....7..........
 */
public class GameGuardReply implements IClientIncomingPacket
{
	private final int[] _reply = new int[4];
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_reply[0] = packet.readD();
		_reply[1] = packet.readD();
		_reply[2] = packet.readD();
		_reply[3] = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		client.setGameGuardOk(true);
	}
}