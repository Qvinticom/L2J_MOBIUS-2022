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
package org.l2jmobius.gameserver.handler.itemhandlers;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.datatables.xml.RecipeData;
import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.model.RecipeList;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class Recipes implements IItemHandler
{
	private static final int[] ITEM_IDS = RecipeData.getInstance().getAllItemIds();
	
	@Override
	public void useItem(Playable playable, ItemInstance item)
	{
		if (!(playable instanceof PlayerInstance))
		{
			return;
		}
		
		final PlayerInstance player = (PlayerInstance) playable;
		if (!Config.IS_CRAFTING_ENABLED)
		{
			player.sendMessage("Crafting is disabled, you cannot register this recipe.");
			return;
		}
		
		final RecipeList recipe = RecipeData.getInstance().getRecipeByItemId(item.getItemId());
		if (player.hasRecipeList(recipe.getId()))
		{
			player.sendPacket(new SystemMessage(SystemMessageId.RECIPE_ALREADY_REGISTERED));
		}
		else if (recipe.isDwarvenRecipe())
		{
			if (player.hasDwarvenCraft())
			{
				if (recipe.getLevel() > player.getDwarvenCraft())
				{
					// Cannot add recipe, because create item level too low.
					player.sendPacket(new SystemMessage(SystemMessageId.CREATE_LVL_TOO_LOW_TO_REGISTER));
				}
				else if (player.getDwarvenRecipeBook().length >= player.getDwarfRecipeLimit())
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.UP_TO_S1_RECIPES_CAN_REGISTER);
					sm.addNumber(player.getDwarfRecipeLimit());
					player.sendPacket(sm);
				}
				else
				{
					player.registerDwarvenRecipeList(recipe);
					player.saveRecipeIntoDB(recipe);
					player.destroyItem("Consume", item.getObjectId(), 1, null, false);
					final SystemMessage sm = new SystemMessage(SystemMessageId.S1_ADDED);
					sm.addString(item.getItemName());
					player.sendPacket(sm);
				}
			}
			else
			{
				player.sendPacket(new SystemMessage(SystemMessageId.CANT_REGISTER_NO_ABILITY_TO_CRAFT));
			}
		}
		else if (player.hasCommonCraft())
		{
			if (recipe.getLevel() > player.getCommonCraft())
			{
				// Cannot add recipe, because create item level too low.
				player.sendPacket(new SystemMessage(SystemMessageId.CREATE_LVL_TOO_LOW_TO_REGISTER));
			}
			else if (player.getCommonRecipeBook().length >= player.getCommonRecipeLimit())
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.UP_TO_S1_RECIPES_CAN_REGISTER);
				sm.addNumber(player.getCommonRecipeLimit());
				player.sendPacket(sm);
			}
			else
			{
				player.registerCommonRecipeList(recipe);
				player.saveRecipeIntoDB(recipe);
				player.destroyItem("Consume", item.getObjectId(), 1, null, false);
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_ADDED);
				sm.addString(item.getItemName());
				player.sendPacket(sm);
			}
		}
		else
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CANT_REGISTER_NO_ABILITY_TO_CRAFT));
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
