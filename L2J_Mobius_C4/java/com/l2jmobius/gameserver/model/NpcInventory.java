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
package com.l2jmobius.gameserver.model;

import java.util.List;

import com.l2jmobius.gameserver.model.L2ItemInstance.ItemLocation;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;

import javolution.util.FastList;

public class NpcInventory extends Inventory
{
	public static final int ADENA_ID = 57;
	public static final int ANCIENT_ADENA_ID = 5575;
	
	private final L2NpcInstance _owner;
	
	public boolean sshotInUse = false;
	public boolean bshotInUse = false;
	
	public NpcInventory(L2NpcInstance owner)
	{
		_owner = owner;
	}
	
	public void reset()
	{
		destroyAllItems("Reset", null, null);
		if (_owner.getTemplate().ss > 0)
		{
			addItem("Reset", 1835, _owner.getTemplate().ss, null, null);
		}
		
		if (_owner.getTemplate().bss > 0)
		{
			addItem("Reset", 3947, _owner.getTemplate().bss, null, null);
		}
	}
	
	@Override
	public L2NpcInstance getOwner()
	{
		return _owner;
	}
	
	@Override
	protected ItemLocation getBaseLocation()
	{
		return ItemLocation.NPC;
	}
	
	@Override
	protected ItemLocation getEquipLocation()
	{
		return ItemLocation.NPC;
	}
	
	/**
	 * Returns the list of all items in inventory that have a given item id.
	 * @param itemId
	 * @return L2ItemInstance[] : matching items from inventory
	 */
	public L2ItemInstance[] getAllItemsByItemId(int itemId)
	{
		final List<L2ItemInstance> list = new FastList<>();
		for (final L2ItemInstance item : _items)
		{
			if (item.getItemId() == itemId)
			{
				list.add(item);
			}
		}
		
		return list.toArray(new L2ItemInstance[list.size()]);
	}
}