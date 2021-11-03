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
package org.l2jmobius.gameserver.model.holders;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.gameserver.enums.TaxType;
import org.l2jmobius.gameserver.model.ItemInfo;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.model.itemcontainer.ItemContainer;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;

/**
 * A modified version of {@link MultisellListHolder} that may include altered data of the original and other dynamic data resulted from players' interraction.
 * @author Nik
 */
public class PreparedMultisellListHolder extends MultisellListHolder
{
	private int _npcObjectId;
	private final boolean _inventoryOnly;
	private double _taxRate;
	private List<ItemInfo> _itemInfos;
	
	public PreparedMultisellListHolder(MultisellListHolder list, boolean inventoryOnly, ItemContainer inventory, Npc npc, double ingredientMultiplier, double productMultiplier)
	{
		super(list.getId(), list.isChanceMultisell(), list.isApplyTaxes(), list.isMaintainEnchantment(), list.getIngredientMultiplier(), list.getProductMultiplier(), list._entries, list._npcsAllowed);
		
		_inventoryOnly = inventoryOnly;
		if (npc != null)
		{
			_npcObjectId = npc.getObjectId();
			_taxRate = npc.getCastleTaxRate(TaxType.BUY);
		}
		
		// Display items from inventory that are available for exchange.
		if (inventoryOnly)
		{
			_entries = new ArrayList<>();
			_itemInfos = new ArrayList<>();
			
			// Only do the match up on equippable items that are not currently equipped. For each appropriate item, produce a set of entries for the multisell list.
			for (ItemInstance item : inventory.getItems())
			{
				if (!item.isEquipped() && (item.isArmor() || item.isWeapon()))
				{
					// Check ingredients of each entry to see if it's an entry we'd like to include.
					for (MultisellEntryHolder entry : list.getEntries())
					{
						for (ItemChanceHolder holder : entry.getIngredients())
						{
							if (holder.getId() == item.getId())
							{
								_entries.add(entry);
								_itemInfos.add(new ItemInfo(item));
							}
						}
					}
				}
			}
		}
	}
	
	public ItemInfo getItemEnchantment(int index)
	{
		return _itemInfos != null ? _itemInfos.get(index) : null;
	}
	
	public double getTaxRate()
	{
		return isApplyTaxes() ? _taxRate : 0;
	}
	
	public boolean isInventoryOnly()
	{
		return _inventoryOnly;
	}
	
	public boolean checkNpcObjectId(int npcObjectId)
	{
		return (_npcObjectId == 0) || (_npcObjectId == npcObjectId);
	}
	
	/**
	 * @param ingredient
	 * @return the new count of the given ingredient after applying ingredient multiplier and adena tax rate.
	 */
	public long getIngredientCount(ItemHolder ingredient)
	{
		return (ingredient.getId() == Inventory.ADENA_ID) ? Math.round(ingredient.getCount() * getIngredientMultiplier() * (1 + getTaxRate())) : Math.round(ingredient.getCount() * getIngredientMultiplier());
	}
	
	/**
	 * @param product
	 * @return the new count of the given product after applying product multiplier.
	 */
	public long getProductCount(ItemChanceHolder product)
	{
		return Math.round(product.getCount() * getProductMultiplier());
	}
}