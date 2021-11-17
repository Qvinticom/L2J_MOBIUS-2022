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

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.data.xml.MapRegionData;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.model.zone.ZoneType;
import org.l2jmobius.gameserver.network.SystemMessageId;

/**
 * A jail zone
 * @author durgus update Harpun
 */
public class JailZone extends ZoneType
{
	public JailZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(Creature creature)
	{
		if (creature instanceof Player)
		{
			creature.setInsideZone(ZoneId.JAIL, true);
			creature.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
			if (Config.JAIL_IS_PVP)
			{
				creature.setInsideZone(ZoneId.PVP, true);
				((Player) creature).sendPacket(SystemMessageId.YOU_HAVE_ENTERED_A_COMBAT_ZONE);
			}
			else
			{
				creature.setInsideZone(ZoneId.PEACE, true);
			}
		}
	}
	
	@Override
	protected void onExit(Creature creature)
	{
		if (creature instanceof Player)
		{
			creature.setInsideZone(ZoneId.JAIL, false);
			creature.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
			if (Config.JAIL_IS_PVP)
			{
				creature.setInsideZone(ZoneId.PVP, false);
				((Player) creature).sendPacket(SystemMessageId.YOU_HAVE_LEFT_A_COMBAT_ZONE);
			}
			else
			{
				creature.setInsideZone(ZoneId.PEACE, false);
			}
			if (((Player) creature).isInJail())
			{
				// when a player wants to exit jail even if he is still jailed, teleport him back to jail
				ThreadPool.schedule(new BackToJail(creature), 2000);
				((Player) creature).sendMessage("You can't cheat your way out of here. You must wait until your jail time is over.");
			}
		}
	}
	
	@Override
	public void onDieInside(Creature creature)
	{
	}
	
	@Override
	public void onReviveInside(Creature creature)
	{
	}
	
	static class BackToJail implements Runnable
	{
		private final Player _player;
		
		BackToJail(Creature creature)
		{
			_player = (Player) creature;
		}
		
		@Override
		public void run()
		{
			_player.teleToLocation(MapRegionData.JAIL_LOCATION, false);
		}
	}
}