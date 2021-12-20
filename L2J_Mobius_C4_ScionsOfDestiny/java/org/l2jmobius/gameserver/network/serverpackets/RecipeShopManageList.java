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
package org.l2jmobius.gameserver.network.serverpackets;

import java.util.Collection;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.ManufactureItem;
import org.l2jmobius.gameserver.model.ManufactureList;
import org.l2jmobius.gameserver.model.RecipeList;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * dd d(dd) d(ddd)
 */
public class RecipeShopManageList implements IClientOutgoingPacket
{
	private final Player _seller;
	private final boolean _isDwarven;
	private Collection<RecipeList> _recipes;
	
	public RecipeShopManageList(Player seller, boolean isDwarven)
	{
		_seller = seller;
		_isDwarven = isDwarven;
		if (_isDwarven && _seller.hasDwarvenCraft())
		{
			_recipes = _seller.getDwarvenRecipeBook();
		}
		else
		{
			_recipes = _seller.getCommonRecipeBook();
		}
		// clean previous recipes
		if (_seller.getCreateList() != null)
		{
			final ManufactureList list = _seller.getCreateList();
			for (ManufactureItem item : list.getList())
			{
				if (item.isDwarven() != _isDwarven)
				{
					list.getList().remove(item);
				}
			}
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.RECIPE_SHOP_MANAGE_LIST.writeId(packet);
		packet.writeD(_seller.getObjectId());
		packet.writeD(_seller.getAdena());
		packet.writeD(_isDwarven ? 0 : 1);
		if (_recipes == null)
		{
			packet.writeD(0);
		}
		else
		{
			packet.writeD(_recipes.size()); // number of items in recipe book
			int count = 0;
			for (RecipeList recipe : _recipes)
			{
				count++;
				packet.writeD(recipe.getId());
				packet.writeD(count);
			}
		}
		if (_seller.getCreateList() == null)
		{
			packet.writeD(0);
		}
		else
		{
			final ManufactureList list = _seller.getCreateList();
			packet.writeD(list.size());
			for (ManufactureItem item : list.getList())
			{
				packet.writeD(item.getRecipeId());
				packet.writeD(0);
				packet.writeD(item.getCost());
			}
		}
		return true;
	}
}
