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
package org.l2jmobius.gameserver.model.itemcontainer;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.enums.ItemLocation;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author UnAfraid
 */
public class PlayerFreight extends ItemContainer
{
	private final Player _owner;
	private final int _ownerId;
	
	public PlayerFreight(int objectId)
	{
		_owner = null;
		_ownerId = objectId;
		restore();
	}
	
	public PlayerFreight(Player owner)
	{
		_owner = owner;
		_ownerId = owner.getObjectId();
	}
	
	@Override
	public int getOwnerId()
	{
		return _ownerId;
	}
	
	@Override
	public Player getOwner()
	{
		return _owner;
	}
	
	@Override
	public ItemLocation getBaseLocation()
	{
		return ItemLocation.FREIGHT;
	}
	
	@Override
	public String getName()
	{
		return "Freight";
	}
	
	@Override
	public boolean validateCapacity(long slots)
	{
		return (getSize() + slots) <= (_owner == null ? Config.ALT_FREIGHT_SLOTS : Config.ALT_FREIGHT_SLOTS + (int) _owner.getStat().calcStat(Stat.FREIGHT_LIM, 0, null, null));
	}
	
	@Override
	public void refreshWeight()
	{
	}
}