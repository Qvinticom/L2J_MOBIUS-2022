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
package org.l2jmobius.gameserver.model.events.impl.item;

import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.impl.IBaseEvent;
import org.l2jmobius.gameserver.model.item.instance.Item;

/**
 * @author NightBR
 */
public class OnItemSoulCrystalAdd implements IBaseEvent
{
	private final Player _player;
	private final Item _item;
	
	public OnItemSoulCrystalAdd(Player player, Item item)
	{
		_player = player;
		_item = item;
	}
	
	public Player getPlayer()
	{
		return _player;
	}
	
	public Item getItem()
	{
		return _item;
	}
	
	@Override
	public EventType getType()
	{
		return EventType.ON_ITEM_SOUL_CRYSTAL_ADD;
	}
}
