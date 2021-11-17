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
import org.l2jmobius.gameserver.data.xml.BoatData;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Boat;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.serverpackets.GetOnVehicle;

public class RequestGetOnVehicle implements IClientIncomingPacket
{
	private int _boatId;
	private int _x;
	private int _y;
	private int _z;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_boatId = packet.readD();
		_x = packet.readD();
		_y = packet.readD();
		_z = packet.readD();
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
		
		final Boat boat = BoatData.getInstance().getBoat(_boatId);
		if (boat == null)
		{
			return;
		}
		
		player.setBoatPosition(new Location(_x, _y, _z));
		player.setXYZ(boat.getPosition().getX(), boat.getPosition().getY(), boat.getPosition().getZ());
		player.broadcastPacket(new GetOnVehicle(player, boat, _x, _y, _z));
		player.revalidateZone(true);
	}
}
