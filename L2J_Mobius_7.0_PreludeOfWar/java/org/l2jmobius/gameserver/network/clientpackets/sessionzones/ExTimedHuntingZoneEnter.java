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
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;

/**
 * @author Mobius
 */
public class ExTimedHuntingZoneEnter implements IClientIncomingPacket
{
	private int _zoneId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_zoneId = packet.readD();
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
			player.sendPacket(SystemMessageId.CANNOT_USE_TIME_LIMITED_HUNTING_ZONES_WHILE_WAITING_FOR_THE_OLYMPIAD);
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
		
		if (player.getVariables().getLong(PlayerVariables.HUNTING_ZONE_RESET_TIME + _zoneId, 0) > System.currentTimeMillis())
		{
			if (player.isInTimedHuntingZone())
			{
				player.sendPacket(SystemMessageId.YOU_WILL_EXCEED_THE_MAX_AMOUNT_OF_TIME_FOR_THE_HUNTING_ZONE_SO_YOU_CANNOT_ADD_ANY_MORE_TIME);
			}
			else
			{
				player.sendPacket(SystemMessageId.YOU_DON_T_HAVE_ENOUGH_TIME_AVAILABLE_TO_ENTER_THE_HUNTING_ZONE);
			}
			return;
		}
		
		if (((_zoneId == 1) && (player.getLevel() < 100)) //
			|| ((_zoneId == 6) && (player.getLevel() < 105)) //
		)
		{
			player.sendMessage("Your level does not corespont the zone equivalent.");
		}
		
		if (player.getAdena() > 150000)
		{
			player.reduceAdena("TimedHuntingZone", 150000, player, true);
			player.getVariables().set(PlayerVariables.HUNTING_ZONE_RESET_TIME + _zoneId, System.currentTimeMillis() + 18000000); // 300 minutes
			switch (_zoneId)
			{
				case 1: // Storm Isle
				{
					player.teleToLocation(194291, 176604, -1888);
					break;
				}
				case 6: // Primeval Isle
				{
					player.teleToLocation(9400, -21720, -3634);
					break;
				}
			}
			player.startTimedHuntingZone(_zoneId, 18000000); // 300 minutes
		}
		else
		{
			player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA);
		}
	}
}
