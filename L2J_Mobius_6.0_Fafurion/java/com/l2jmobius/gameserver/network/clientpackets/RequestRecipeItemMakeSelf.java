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
package com.l2jmobius.gameserver.network.clientpackets;

import com.l2jmobius.Config;
import com.l2jmobius.commons.network.PacketReader;
import com.l2jmobius.commons.util.Rnd;
import com.l2jmobius.gameserver.data.xml.impl.RecipeData;
import com.l2jmobius.gameserver.enums.PrivateStoreType;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.ItemHolder;
import com.l2jmobius.gameserver.model.holders.RecipeHolder;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;
import com.l2jmobius.gameserver.model.stats.Stats;
import com.l2jmobius.gameserver.network.L2GameClient;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.RecipeItemMakeInfo;
import com.l2jmobius.gameserver.util.Util;

/**
 * @author Nik
 */
public final class RequestRecipeItemMakeSelf implements IClientIncomingPacket
{
	private int _id;
	private ItemHolder[] _offeredItems;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_id = packet.readD();
		
		final int offeringsCount = packet.readD();
		if (offeringsCount > 0)
		{
			_offeredItems = new ItemHolder[offeringsCount];
			for (int i = 0; i < offeringsCount; i++)
			{
				final int objectId = packet.readD();
				final long count = packet.readQ();
				_offeredItems[i] = new ItemHolder(objectId, count);
			}
		}
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		final L2PcInstance activeChar = client.getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (!Config.IS_CRAFTING_ENABLED)
		{
			activeChar.sendMessage("Item creation is currently disabled.");
			return;
		}
		
		if (!client.getFloodProtectors().getManufacture().tryPerformAction("RecipeMakeSelf"))
		{
			return;
		}
		
		if (activeChar.isCastingNow())
		{
			activeChar.sendPacket(SystemMessageId.YOUR_RECIPE_BOOK_MAY_NOT_BE_ACCESSED_WHILE_USING_A_SKILL);
			return;
		}
		
		if (activeChar.getPrivateStoreType() == PrivateStoreType.MANUFACTURE)
		{
			activeChar.sendPacket(SystemMessageId.YOU_MAY_NOT_ALTER_YOUR_RECIPE_BOOK_WHILE_ENGAGED_IN_MANUFACTURING);
			return;
		}
		
		if (activeChar.isProcessingTransaction())
		{
			activeChar.sendPacket(SystemMessageId.ITEM_CREATION_IS_NOT_POSSIBLE_WHILE_ENGAGED_IN_A_TRADE);
			return;
		}
		
		// TODO: Check if its a retail-like check.
		if (activeChar.isAlikeDead())
		{
			return;
		}
		
		// On retail if player is requesting trade, it is instantly canceled.
		activeChar.cancelActiveTrade();
		
		final RecipeHolder recipe = RecipeData.getInstance().getRecipe(_id);
		if (recipe == null)
		{
			activeChar.sendPacket(SystemMessageId.THE_RECIPE_IS_INCORRECT);
			return;
		}
		
		if (!activeChar.hasRecipeList(recipe.getId()))
		{
			Util.handleIllegalPlayerAction(activeChar, "Warning!! Character " + activeChar.getName() + " of account " + activeChar.getAccountName() + " sent a false recipe id.", Config.DEFAULT_PUNISH);
			return;
		}
		
		// Check if stats or ingredients are met.
		if (!recipe.checkNecessaryStats(activeChar, activeChar, true) || !recipe.checkNecessaryIngredients(activeChar, true))
		{
			return;
		}
		
		// Check if all offerings are legit.
		if ((_offeredItems != null) && (recipe.getMaxOffering() > 0) && (recipe.getMaxOfferingBonus() > 0))
		{
			for (ItemHolder offer : _offeredItems)
			{
				final L2ItemInstance item = activeChar.getInventory().getItemByObjectId(offer.getId());
				if ((item == null) || (item.getCount() < offer.getCount()) || !item.isDestroyable())
				{
					return;
				}
			}
		}
		
		if (activeChar.isCrafting())
		{
			activeChar.sendPacket(SystemMessageId.CURRENTLY_CRAFTING_AN_ITEM_PLEASE_WAIT);
			return;
		}
		
		activeChar.setIsCrafting(true);
		
		// Take offerings to increase chance
		double offeringBonus = 0;
		if ((_offeredItems != null) && (recipe.getMaxOffering() > 0) && (recipe.getMaxOfferingBonus() > 0))
		{
			long offeredAdenaWorth = 0;
			for (ItemHolder offer : _offeredItems)
			{
				final L2ItemInstance item = activeChar.getInventory().getItemByObjectId(offer.getId());
				if (activeChar.destroyItem("CraftOffering", item, offer.getCount(), null, true))
				{
					offeredAdenaWorth += (item.getItem().getReferencePrice() * offer.getCount());
				}
			}
			
			offeringBonus = Math.min((offeredAdenaWorth / recipe.getMaxOffering()) * recipe.getMaxOfferingBonus(), recipe.getMaxOfferingBonus());
		}
		
		final boolean success = activeChar.tryLuck() || ((recipe.getSuccessRate() + offeringBonus) > Rnd.get(100));
		final boolean craftingCritical = success && (activeChar.getStat().getValue(Stats.CRAFTING_CRITICAL) > Rnd.get(100));
		
		if (success) // Successful craft.
		{
			if (craftingCritical)
			{
				activeChar.sendPacket(SystemMessageId.CRAFTING_CRITICAL);
			}
		}
		else // Failed craft.
		{
			activeChar.sendPacket(SystemMessageId.YOU_FAILED_AT_MIXING_THE_ITEM);
		}
		
		// Perform the crafting: take the items and give reward if success.
		recipe.doCraft(activeChar, activeChar, success, craftingCritical, true);
		
		// Send craft window. Must be sent after crafting so it properly counts the items.
		activeChar.sendPacket(new RecipeItemMakeInfo(recipe.getId(), activeChar, success, recipe.getMaxOffering()));
		
		activeChar.setIsCrafting(false);
	}
}
