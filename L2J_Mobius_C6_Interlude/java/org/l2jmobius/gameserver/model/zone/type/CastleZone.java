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

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.gameserver.instancemanager.CastleManager;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.SiegeSummon;
import org.l2jmobius.gameserver.model.siege.Castle;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.model.zone.ZoneRespawn;
import org.l2jmobius.gameserver.network.SystemMessageId;

/**
 * A castle zone
 * @author durgus
 */
public class CastleZone extends ZoneRespawn
{
	private Castle _castle;
	
	public CastleZone(int id)
	{
		super(id);
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		switch (name)
		{
			case "castleId":
			{
				final int castleId = Integer.parseInt(value);
				// Register self to the correct castle
				_castle = CastleManager.getInstance().getCastleById(castleId);
				if (_castle != null)
				{
					_castle.setZone(this);
				}
				break;
			}
			default:
			{
				super.setParameter(name, value);
				break;
			}
		}
	}
	
	@Override
	protected void onEnter(Creature creature)
	{
		creature.setInsideZone(ZoneId.CASTLE, true);
		if (_castle.getSiege().isInProgress())
		{
			creature.setInsideZone(ZoneId.PVP, true);
			creature.setInsideZone(ZoneId.SIEGE, true);
			if (creature instanceof Player)
			{
				((Player) creature).sendPacket(SystemMessageId.YOU_HAVE_ENTERED_A_COMBAT_ZONE);
			}
		}
	}
	
	@Override
	protected void onExit(Creature creature)
	{
		creature.setInsideZone(ZoneId.CASTLE, false);
		if (_castle.getSiege().isInProgress())
		{
			creature.setInsideZone(ZoneId.PVP, false);
			creature.setInsideZone(ZoneId.SIEGE, false);
			if (creature instanceof Player)
			{
				((Player) creature).sendPacket(SystemMessageId.YOU_HAVE_LEFT_A_COMBAT_ZONE);
				
				// Set pvp flag
				if (((Player) creature).getPvpFlag() == 0)
				{
					((Player) creature).startPvPFlag();
				}
			}
		}
		if (creature instanceof SiegeSummon)
		{
			((SiegeSummon) creature).unSummon(((SiegeSummon) creature).getOwner());
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
	
	public void updateZoneStatusForCharactersInside()
	{
		if (_castle.getSiege().isInProgress())
		{
			for (Creature creature : getCharactersInside())
			{
				try
				{
					onEnter(creature);
				}
				catch (NullPointerException e)
				{
				}
			}
		}
		else
		{
			for (Creature creature : getCharactersInside())
			{
				try
				{
					creature.setInsideZone(ZoneId.PVP, false);
					creature.setInsideZone(ZoneId.SIEGE, false);
					if (creature instanceof Player)
					{
						((Player) creature).sendPacket(SystemMessageId.YOU_HAVE_LEFT_A_COMBAT_ZONE);
					}
					
					if (creature instanceof SiegeSummon)
					{
						((SiegeSummon) creature).unSummon(((SiegeSummon) creature).getOwner());
					}
				}
				catch (NullPointerException e)
				{
				}
			}
		}
	}
	
	/**
	 * Removes all foreigners from the castle
	 * @param owningClanId
	 */
	public void banishForeigners(int owningClanId)
	{
		for (Creature temp : getCharactersInside())
		{
			if (!(temp instanceof Player))
			{
				continue;
			}
			
			if (((Player) temp).getClanId() == owningClanId)
			{
				continue;
			}
			
			((Player) temp).teleToLocation(getChaoticSpawnLoc(), true);
		}
	}
	
	/**
	 * Sends a message to all players in this zone
	 * @param message
	 */
	public void announceToPlayers(String message)
	{
		for (Creature temp : getCharactersInside())
		{
			if (temp instanceof Player)
			{
				((Player) temp).sendMessage(message);
			}
		}
	}
	
	/**
	 * Returns all players within this zone
	 * @return
	 */
	public List<Player> getAllPlayers()
	{
		final List<Player> players = new ArrayList<>();
		for (Creature temp : getCharactersInside())
		{
			if (temp instanceof Player)
			{
				players.add((Player) temp);
			}
		}
		return players;
	}
	
	public boolean isSiegeActive()
	{
		if (_castle != null)
		{
			return _castle.isSiegeInProgress();
		}
		return false;
	}
}
