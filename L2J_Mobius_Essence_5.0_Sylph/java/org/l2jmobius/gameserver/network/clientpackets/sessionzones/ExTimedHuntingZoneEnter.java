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

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.commons.util.Chronos;
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
		
		if (player.isInCombat())
		{
			player.sendMessage("You can only enter in time-limited hunting zones while not in combat.");
			return;
		}
		if (player.getReputation() < 0)
		{
			player.sendMessage("You can only enter in time-limited hunting zones when you have positive reputation.");
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
		if (player.isInInstance() || player.isInTimedHuntingZone(player.getX(), player.getY()))
		{
			player.sendMessage("Cannot use time-limited hunting zones while in an instance.");
			return;
		}
		
		if (((_zoneId == 1) && (player.getLevel() < 40)) //
			|| ((_zoneId == 4) && (player.getLevel() < 76)) //
			|| ((_zoneId == 11) && (player.getLevel() < 60)) //
			|| ((_zoneId == 12) && (player.getLevel() < 80)) //
			|| ((_zoneId == 101) && (player.getLevel() < 40)) //
		)
		{
			player.sendMessage("Your level does not correspond the zone equivalent.");
		}
		
		final long currentTime = Chronos.currentTimeMillis();
		long endTime = currentTime + player.getTimedHuntingZoneRemainingTime(_zoneId);
		final long lastEntryTime = player.getVariables().getLong(PlayerVariables.HUNTING_ZONE_ENTRY + _zoneId, 0);
		if ((lastEntryTime + Config.TIME_LIMITED_ZONE_RESET_DELAY) < currentTime)
		{
			if (endTime == currentTime)
			{
				endTime = Config.TIME_LIMITED_ZONE_INITIAL_TIME;
				player.getVariables().set(PlayerVariables.HUNTING_ZONE_ENTRY + _zoneId, currentTime);
			}
		}
		
		if (endTime > currentTime)
		{
			if (player.getAdena() > Config.TIME_LIMITED_ZONE_TELEPORT_FEE)
			{
				player.reduceAdena("TimedHuntingZone", Config.TIME_LIMITED_ZONE_TELEPORT_FEE, player, true);
			}
			else
			{
				player.sendMessage("Not enough adena.");
				return;
			}
			
			player.getVariables().set(PlayerVariables.HUNTING_ZONE_TIME + _zoneId, endTime - currentTime);
			
			switch (_zoneId)
			{
				case 1: // Primeval Isle
				{
					player.teleToLocation(9400, -21720, -3634);
					break;
				}
				case 4: // Forgotten Primeval Garden
				{
					player.teleToLocation(-112937, 213590, -13248);
					break;
				}
				case 11: // Aligator Island
				{
					player.teleToLocation(115528, 191580, -3371);
					break;
				}
				case 12: // Antharas Lair
				{
					player.teleToLocation(133247, 114445, -3724);
					break;
				}
				case 101: // Transcendent Instance Zone 1
				{
					player.teleToLocation(63443, 26304, -3755);
					break;
				}
				case 102: // Transcendent Instance Zone 2
				{
					player.teleToLocation(125277, 70262, -4408);
					break;
				}
				case 103: // Transcendent Instance Zone 3
				{
					player.teleToLocation(148724, -22366, -3436);
					break;
				}
				case 104: // Transcendent Instance Zone 4
				{
					player.teleToLocation(167965, 28800, -3606);
					break;
				}
				case 106: // Transcendent Instance Zone 6
				{
					player.teleToLocation(99797, 110524, -3702);
					break;
				}
				case 107: // Transcendent Instance Zone 7
				{
					player.teleToLocation(-50416, 145363, -2825);
					break;
				}
			}
		}
		else
		{
			player.sendMessage("You don't have enough time available to enter the hunting zone.");
		}
	}
}
