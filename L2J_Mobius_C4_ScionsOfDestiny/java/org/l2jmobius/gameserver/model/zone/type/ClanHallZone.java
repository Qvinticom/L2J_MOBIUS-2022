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

import org.l2jmobius.gameserver.data.sql.ClanHallTable;
import org.l2jmobius.gameserver.enums.TeleportWhereType;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.residences.ClanHall;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.model.zone.ZoneRespawn;
import org.l2jmobius.gameserver.network.serverpackets.ClanHallDecoration;

/**
 * A clan hall zone
 * @author durgus
 */
public class ClanHallZone extends ZoneRespawn
{
	private int _clanHallId;
	
	public ClanHallZone(int id)
	{
		super(id);
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		switch (name)
		{
			case "clanHallId":
			{
				_clanHallId = Integer.parseInt(value);
				// Register self to the correct clan hall
				final ClanHall clanHall = ClanHallTable.getInstance().getClanHallById(_clanHallId);
				if (clanHall != null)
				{
					clanHall.setZone(this);
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
		if (creature instanceof Player)
		{
			// Set as in clan hall
			creature.setInsideZone(ZoneId.CLAN_HALL, true);
			
			final ClanHall clanHall = ClanHallTable.getInstance().getClanHallById(_clanHallId);
			if (clanHall == null)
			{
				return;
			}
			
			// Send decoration packet
			((Player) creature).sendPacket(new ClanHallDecoration(clanHall));
			
			// Send a message
			if ((clanHall.getOwnerId() != 0) && (clanHall.getOwnerId() == ((Player) creature).getClanId()))
			{
				((Player) creature).sendMessage("You have entered your clan hall.");
			}
		}
	}
	
	@Override
	protected void onExit(Creature creature)
	{
		if (creature instanceof Player)
		{
			// Unset clanhall zone
			creature.setInsideZone(ZoneId.CLAN_HALL, false);
			
			// Send a message
			if ((((Player) creature).getClanId() != 0) && (ClanHallTable.getInstance().getClanHallById(_clanHallId).getOwnerId() == ((Player) creature).getClanId()))
			{
				((Player) creature).sendMessage("You have left your clan hall.");
			}
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
	
	/**
	 * Removes all foreigners from the clan hall
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
			
			((Player) temp).teleToLocation(TeleportWhereType.TOWN);
		}
	}
}
