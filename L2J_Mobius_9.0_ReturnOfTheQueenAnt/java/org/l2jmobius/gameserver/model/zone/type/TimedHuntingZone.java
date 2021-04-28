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
package org.l2jmobius.gameserver.model.zone.type;

import org.l2jmobius.gameserver.enums.TeleportWhereType;
import org.l2jmobius.gameserver.instancemanager.MapRegionManager;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.model.zone.ZoneType;
import org.l2jmobius.gameserver.network.serverpackets.sessionzones.TimedHuntingZoneExit;

/**
 * @author Mobius
 */
public class TimedHuntingZone extends ZoneType
{
	public TimedHuntingZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(Creature creature)
	{
		final PlayerInstance player = creature.getActingPlayer();
		if (player != null)
		{
			player.setInsideZone(ZoneId.TIMED_HUNTING, true);
			
			final long stormIsleExitTime = player.getTimedHuntingZoneRemainingTime(1);
			final long primevalIsleExitTime = player.getTimedHuntingZoneRemainingTime(6);
			final long goldenAltarExitTime = player.getTimedHuntingZoneRemainingTime(7);
			final long coalMinesExitTime = player.getTimedHuntingZoneRemainingTime(11);
			final long toiExitTime = player.getTimedHuntingZoneRemainingTime(8);
			final long imperialTombExitTime = player.getTimedHuntingZoneRemainingTime(12);
			if ((stormIsleExitTime > 0) && player.isInTimedHuntingZone(1))
			{
				player.startTimedHuntingZone(1, stormIsleExitTime);
			}
			else if ((primevalIsleExitTime > 0) && player.isInTimedHuntingZone(6))
			{
				player.startTimedHuntingZone(6, primevalIsleExitTime);
			}
			else if ((goldenAltarExitTime > 0) && player.isInTimedHuntingZone(7))
			{
				player.startTimedHuntingZone(7, goldenAltarExitTime);
			}
			else if ((coalMinesExitTime > 0) && player.isInTimedHuntingZone(11))
			{
				player.startTimedHuntingZone(11, coalMinesExitTime);
			}
			else if ((toiExitTime > 0) && player.isInTimedHuntingZone(8))
			{
				player.startTimedHuntingZone(8, toiExitTime);
			}
			else if ((imperialTombExitTime > 0) && player.isInTimedHuntingZone(12))
			{
				player.startTimedHuntingZone(12, imperialTombExitTime);
			}
			else if (!player.isGM())
			{
				player.teleToLocation(MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.TOWN));
			}
		}
	}
	
	@Override
	protected void onExit(Creature creature)
	{
		final PlayerInstance player = creature.getActingPlayer();
		if (player != null)
		{
			player.setInsideZone(ZoneId.TIMED_HUNTING, false);
			player.sendPacket(TimedHuntingZoneExit.STATIC_PACKET);
		}
	}
}
