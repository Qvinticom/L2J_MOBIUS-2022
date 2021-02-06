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
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
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
			
			final long currentTime = System.currentTimeMillis();
			final long primevalIsleExitTime = player.getVariables().getLong(PlayerVariables.HUNTING_ZONE_RESET_TIME + 1, 0);
			final long PrimevalGardenExitTime = player.getVariables().getLong(PlayerVariables.HUNTING_ZONE_RESET_TIME + 4, 0);
			final long AlligatorIslandExitTime = player.getVariables().getLong(PlayerVariables.HUNTING_ZONE_RESET_TIME + 11, 0);
			final long AntharasLairExitTime = player.getVariables().getLong(PlayerVariables.HUNTING_ZONE_RESET_TIME + 12, 0);
			final long Transcendent1ExitTime = player.getVariables().getLong(PlayerVariables.HUNTING_ZONE_RESET_TIME + 101, 0);
			final long Transcendent2ExitTime = player.getVariables().getLong(PlayerVariables.HUNTING_ZONE_RESET_TIME + 102, 0);
			final long Transcendent3ExitTime = player.getVariables().getLong(PlayerVariables.HUNTING_ZONE_RESET_TIME + 103, 0);
			final long Transcendent4ExitTime = player.getVariables().getLong(PlayerVariables.HUNTING_ZONE_RESET_TIME + 104, 0);
			final long Transcendent6ExitTime = player.getVariables().getLong(PlayerVariables.HUNTING_ZONE_RESET_TIME + 106, 0);
			final long Transcendent7ExitTime = player.getVariables().getLong(PlayerVariables.HUNTING_ZONE_RESET_TIME + 107, 0);
			
			if ((primevalIsleExitTime > currentTime) && player.isInTimedHuntingZone(1))
			{
				player.startTimedHuntingZone(1, primevalIsleExitTime - currentTime);
			}
			else if ((PrimevalGardenExitTime > currentTime) && player.isInTimedHuntingZone(4))
			{
				player.startTimedHuntingZone(4, PrimevalGardenExitTime - currentTime);
			}
			else if ((AlligatorIslandExitTime > currentTime) && player.isInTimedHuntingZone(11))
			{
				player.startTimedHuntingZone(11, AlligatorIslandExitTime - currentTime);
			}
			else if ((AntharasLairExitTime > currentTime) && player.isInTimedHuntingZone(12))
			{
				player.startTimedHuntingZone(12, AntharasLairExitTime - currentTime);
			}
			else if ((Transcendent1ExitTime > currentTime) && player.isInTimedHuntingZone(101))
			{
				player.startTimedHuntingZone(101, Transcendent1ExitTime - currentTime);
			}
			else if ((Transcendent2ExitTime > currentTime) && player.isInTimedHuntingZone(102))
			{
				player.startTimedHuntingZone(102, Transcendent2ExitTime - currentTime);
			}
			else if ((Transcendent3ExitTime > currentTime) && player.isInTimedHuntingZone(103))
			{
				player.startTimedHuntingZone(103, Transcendent3ExitTime - currentTime);
			}
			else if ((Transcendent4ExitTime > currentTime) && player.isInTimedHuntingZone(104))
			{
				player.startTimedHuntingZone(104, Transcendent4ExitTime - currentTime);
			}
			else if ((Transcendent6ExitTime > currentTime) && player.isInTimedHuntingZone(106))
			{
				player.startTimedHuntingZone(106, Transcendent6ExitTime - currentTime);
			}
			else if ((Transcendent7ExitTime > currentTime) && player.isInTimedHuntingZone(107))
			{
				player.startTimedHuntingZone(107, Transcendent7ExitTime - currentTime);
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
