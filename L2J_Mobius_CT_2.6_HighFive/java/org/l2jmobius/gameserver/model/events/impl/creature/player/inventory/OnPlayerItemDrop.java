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
package org.l2jmobius.gameserver.model.events.impl.creature.player.inventory;

import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.impl.IBaseEvent;
import org.l2jmobius.gameserver.model.items.instance.Item;

/**
 * @author UnAfraid
 */
public class OnPlayerItemDrop implements IBaseEvent
{
	private final Player _player;
	private final Item _item;
	private final Location _loc;
	
	public OnPlayerItemDrop(Player player, Item item, Location loc)
	{
		_player = player;
		_item = item;
		_loc = loc;
	}
	
	public Player getPlayer()
	{
		return _player;
	}
	
	public Item getItem()
	{
		return _item;
	}
	
	public Location getLocation()
	{
		return _loc;
	}
	
	@Override
	public EventType getType()
	{
		return EventType.ON_PLAYER_ITEM_DROP;
	}
}
