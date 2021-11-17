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

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.serverpackets.StopRotation;

/**
 * @version $Revision: 1.1.4.3 $ $Date: 2005/03/27 15:29:30 $
 */
public class FinishRotating implements IClientIncomingPacket
{
	private int _degree;
	@SuppressWarnings("unused")
	private int _unknown;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_degree = packet.readD();
		_unknown = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		if (!Config.ENABLE_KEYBOARD_MOVEMENT)
		{
			return;
		}
		
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		StopRotation sr;
		if (player.isInAirShip() && player.getAirShip().isCaptain(player))
		{
			player.getAirShip().setHeading(_degree);
			sr = new StopRotation(player.getAirShip().getObjectId(), _degree, 0);
			player.getAirShip().broadcastPacket(sr);
		}
		else
		{
			sr = new StopRotation(player.getObjectId(), _degree, 0);
			player.broadcastPacket(sr);
		}
	}
}
