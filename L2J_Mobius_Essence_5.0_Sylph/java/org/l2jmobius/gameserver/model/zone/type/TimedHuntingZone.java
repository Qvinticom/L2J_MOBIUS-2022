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
			
			final long primevalIsleExitTime = player.getTimedHuntingZoneRemainingTime(1);
			final long primevalGardenExitTime = player.getTimedHuntingZoneRemainingTime(4);
			final long alligatorIslandExitTime = player.getTimedHuntingZoneRemainingTime(11);
			final long antharasLairExitTime = player.getTimedHuntingZoneRemainingTime(12);
			final long transcendent1ExitTime = player.getTimedHuntingZoneRemainingTime(101);
			final long transcendent2ExitTime = player.getTimedHuntingZoneRemainingTime(102);
			final long transcendent3ExitTime = player.getTimedHuntingZoneRemainingTime(103);
			final long transcendent4ExitTime = player.getTimedHuntingZoneRemainingTime(104);
			final long transcendent6ExitTime = player.getTimedHuntingZoneRemainingTime(106);
			final long transcendent7ExitTime = player.getTimedHuntingZoneRemainingTime(107);
			
			if ((primevalIsleExitTime > 0) && player.isInTimedHuntingZone(1))
			{
				player.startTimedHuntingZone(1, primevalIsleExitTime);
			}
			else if ((primevalGardenExitTime > 0) && player.isInTimedHuntingZone(4))
			{
				player.startTimedHuntingZone(4, primevalGardenExitTime);
			}
			else if ((alligatorIslandExitTime > 0) && player.isInTimedHuntingZone(11))
			{
				player.startTimedHuntingZone(11, alligatorIslandExitTime);
			}
			else if ((antharasLairExitTime > 0) && player.isInTimedHuntingZone(12))
			{
				player.startTimedHuntingZone(12, antharasLairExitTime);
			}
			else if ((transcendent1ExitTime > 0) && player.isInTimedHuntingZone(101))
			{
				player.startTimedHuntingZone(101, transcendent1ExitTime);
			}
			else if ((transcendent2ExitTime > 0) && player.isInTimedHuntingZone(102))
			{
				player.startTimedHuntingZone(102, transcendent2ExitTime);
			}
			else if ((transcendent3ExitTime > 0) && player.isInTimedHuntingZone(103))
			{
				player.startTimedHuntingZone(103, transcendent3ExitTime);
			}
			else if ((transcendent4ExitTime > 0) && player.isInTimedHuntingZone(104))
			{
				player.startTimedHuntingZone(104, transcendent4ExitTime);
			}
			else if ((transcendent6ExitTime > 0) && player.isInTimedHuntingZone(106))
			{
				player.startTimedHuntingZone(106, transcendent6ExitTime);
			}
			else if ((transcendent7ExitTime > 0) && player.isInTimedHuntingZone(107))
			{
				player.startTimedHuntingZone(107, transcendent7ExitTime);
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
