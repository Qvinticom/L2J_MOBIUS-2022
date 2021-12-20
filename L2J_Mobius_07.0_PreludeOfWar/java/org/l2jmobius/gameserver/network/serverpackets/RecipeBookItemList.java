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
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.RecipeHolder;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class RecipeBookItemList implements IClientOutgoingPacket
{
	private final Collection<RecipeHolder> _recipes;
	private final boolean _isDwarvenCraft;
	private final int _maxMp;
	
	public RecipeBookItemList(Player player, boolean isDwarvenCraft)
	{
		_isDwarvenCraft = isDwarvenCraft;
		_maxMp = player.getMaxMp();
		_recipes = (isDwarvenCraft ? player.getDwarvenRecipeBook() : player.getCommonRecipeBook());
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.RECIPE_BOOK_ITEM_LIST.writeId(packet);
		packet.writeD(_isDwarvenCraft ? 0 : 1); // 0 = Dwarven - 1 = Common
		packet.writeD(_maxMp);
		if (_recipes == null)
		{
			packet.writeD(0);
		}
		else
		{
			packet.writeD(_recipes.size()); // number of items in recipe book
			int i = 1;
			for (RecipeHolder recipe : _recipes)
			{
				packet.writeD(recipe.getId());
				packet.writeD(i++);
			}
		}
		return true;
	}
}
