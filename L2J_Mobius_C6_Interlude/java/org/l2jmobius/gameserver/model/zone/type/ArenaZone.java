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
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.model.zone.ZoneRespawn;
import org.l2jmobius.gameserver.network.SystemMessageId;

/**
 * A PVP Zone
 * @author durgus
 */
public class ArenaZone extends ZoneRespawn
{
	public ArenaZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(Creature creature)
	{
		if (creature.isPlayer() && !creature.isInsideZone(ZoneId.PVP))
		{
			creature.getActingPlayer().sendPacket(SystemMessageId.YOU_HAVE_ENTERED_A_COMBAT_ZONE);
		}
		creature.setInsideZone(ZoneId.PVP, true);
		creature.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
	}
	
	@Override
	protected void onExit(Creature creature)
	{
		creature.setInsideZone(ZoneId.PVP, false);
		creature.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
		if (creature.isPlayer() && !creature.isInsideZone(ZoneId.PVP))
		{
			creature.getActingPlayer().sendPacket(SystemMessageId.YOU_HAVE_LEFT_A_COMBAT_ZONE);
		}
	}
	
	@Override
	protected void onDieInside(Creature creature)
	{
	}
	
	@Override
	protected void onReviveInside(Creature creature)
	{
	}
	
	public void oustAllPlayers()
	{
		for (Creature creature : getCharactersInside())
		{
			if (creature == null)
			{
				continue;
			}
			
			if (creature instanceof Player)
			{
				final Player player = (Player) creature;
				if (player.isOnline())
				{
					player.teleToLocation(TeleportWhereType.TOWN);
				}
			}
		}
	}
}
