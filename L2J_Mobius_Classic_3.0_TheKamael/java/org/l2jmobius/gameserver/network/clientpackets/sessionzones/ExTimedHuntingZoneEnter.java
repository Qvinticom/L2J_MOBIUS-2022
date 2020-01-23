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
package org.l2jmobius.gameserver.network.clientpackets.sessionzones;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.olympiad.OlympiadManager;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;

/**
 * @author Mobius
 */
public class ExTimedHuntingZoneEnter implements IClientIncomingPacket
{
	private int _fieldId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_fieldId = packet.readD();
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
		
		if (player.isMounted())
		{
			player.sendMessage("Cannot use time-limited hunting zones while mounted.");
			return;
		}
		if (player.isInDuel())
		{
			player.sendMessage("Cannot use time-limited hunting zones during a duel.");
			return;
		}
		if (player.isInOlympiadMode() || OlympiadManager.getInstance().isRegistered(player))
		{
			player.sendMessage("Cannot use time-limited hunting zones while waiting for the Olympiad.");
			return;
		}
		if (player.isOnEvent() || (player.getBlockCheckerArena() > -1))
		{
			player.sendMessage("Cannot use time-limited hunting zones while registered on an event.");
			return;
		}
		if (player.isInInstance())
		{
			player.sendMessage("Cannot use time-limited hunting zones while in an instance.");
			return;
		}
		
		if (player.getVariables().getLong(PlayerVariables.HUNTING_ZONE_RESET_TIME, 0) > System.currentTimeMillis())
		{
			if (player.isInTimedHuntingZone())
			{
				player.sendMessage("You will exceed the max amount of time for the hunting zone, so you cannot add any more time.");
			}
			else
			{
				player.sendMessage("You don't have enough time available to enter the hunting zone.");
			}
			return;
		}
		
		if ((_fieldId == 2) && (player.getLevel() < 78))
		{
			player.sendMessage("Your level is too low.");
		}
		
		if (player.getAdena() > 10000)
		{
			player.reduceAdena("TimedHuntingZone", 10000, player, true);
			player.getVariables().set(PlayerVariables.HUNTING_ZONE_RESET_TIME, System.currentTimeMillis() + 18000000); // 300 minutes
			switch (_fieldId)
			{
				case 2: // Ancient Pirates' Tomb
				{
					player.teleToLocation(13181, -78651, -5977);
					break;
				}
			}
			player.startTimedHuntingZone(18000000); // 300 minutes
		}
		else
		{
			player.sendMessage("Not enough adena.");
		}
	}
}
