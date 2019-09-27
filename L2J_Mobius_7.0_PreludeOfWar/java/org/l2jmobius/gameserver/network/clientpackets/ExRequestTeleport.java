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
import org.l2jmobius.gameserver.data.xml.impl.TeleportListData;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.holders.TeleportListHolder;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;

/**
 * @author NviX
 */
public class ExRequestTeleport implements IClientIncomingPacket
{
	private int _locId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_locId = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final PlayerInstance player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		boolean success = false;
		
		for (TeleportListHolder teleport : TeleportListData.getInstance().getTeleports())
		{
			if (teleport.getLocId() == _locId)
			{
				if (player.getAdena() < teleport.getPrice())
				{
					player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
					return;
				}
				
				player.reduceAdena("teleport", teleport.getPrice(), player, true);
				player.teleToLocation(teleport.getX(), teleport.getY(), teleport.getZ());
				success = true;
				break;
			}
		}
		
		if (!success)
		{
			LOGGER.info("No registered teleport location for id: " + _locId);
			return;
		}
	}
}