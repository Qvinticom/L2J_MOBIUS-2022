/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.network.serverpackets;

import com.l2jserver.gameserver.enums.ItemListType;
import com.l2jserver.gameserver.model.ItemInfo;
import com.l2jserver.gameserver.model.TradeItem;
import com.l2jserver.gameserver.model.buylist.Product;
import com.l2jserver.gameserver.model.itemcontainer.PcInventory;
import com.l2jserver.gameserver.model.items.L2WarehouseItem;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;

/**
 * @author UnAfraid
 */
public abstract class AbstractItemPacket extends AbstractMaskPacket<ItemListType>
{
	private static final byte[] MASKS =
	{
		0x00
	};
	
	@Override
	protected byte[] getMasks()
	{
		return MASKS;
	}
	
	@Override
	protected void onNewMaskAdded(ItemListType component)
	{
	}
	
	protected void writeItem(TradeItem item)
	{
		writeItem(new ItemInfo(item));
	}
	
	protected void writeItem(L2WarehouseItem item)
	{
		writeItem(new ItemInfo(item));
	}
	
	protected void writeItem(L2ItemInstance item)
	{
		writeItem(new ItemInfo(item));
	}
	
	protected void writeItem(Product item)
	{
		writeItem(new ItemInfo(item));
	}
	
	protected void writeTradeItem(TradeItem item)
	{
		writeH(item.getItem().getType1());
		writeD(item.getObjectId()); // ObjectId
		writeD(item.getItem().getDisplayId()); // ItemId
		writeQ(item.getCount()); // Quantity
		writeC(item.getItem().getType2()); // Item Type 2 : 00-weapon, 01-shield/armor, 02-ring/earring/necklace, 03-questitem, 04-adena, 05-item
		writeC(item.getCustomType1()); // Filler (always 0)
		writeQ(item.getItem().getBodyPart()); // Slot : 0006-lr.ear, 0008-neck, 0030-lr.finger, 0040-head, 0100-l.hand, 0200-gloves, 0400-chest, 0800-pants, 1000-feet, 4000-r.hand, 8000-r.hand
		writeC(item.getEnchant()); // Enchant level (pet level shown in control item)
		writeH(0x00); // Equipped : 00-No, 01-yes
		writeC(item.getCustomType2());
		writeD(0);
		writeItemElementalAndEnchant(new ItemInfo(item));
	}
	
	protected void writeItem(ItemInfo item)
	{
		final int mask = calculateMask(item);
		// cddcQcchQccddc
		writeC(mask);
		writeD(item.getObjectId()); // ObjectId
		writeD(item.getItem().getDisplayId()); // ItemId
		writeC(item.getEquipped() == 0 ? item.getLocation() : 0xFF); // T1
		writeQ(item.getCount()); // Quantity
		writeC(item.getItem().getType2()); // Item Type 2 : 00-weapon, 01-shield/armor, 02-ring/earring/necklace, 03-questitem, 04-adena, 05-item
		writeC(item.getCustomType1()); // Filler (always 0)
		writeH(item.getEquipped()); // Equipped : 00-No, 01-yes
		writeQ(item.getItem().getBodyPart()); // Slot : 0006-lr.ear, 0008-neck, 0030-lr.finger, 0040-head, 0100-l.hand, 0200-gloves, 0400-chest, 0800-pants, 1000-feet, 4000-r.hand, 8000-r.hand
		writeC(item.getEnchant()); // Enchant level (pet level shown in control item)
		writeC(0);
		writeD(item.getMana());
		writeD(item.getTime());
		writeC(0x01); // GOD Item enabled = 1 disabled (red) = 0
		if (containsMask(mask, ItemListType.AUGMENT_BONUS))
		{
			writeD(item.get1stAugmentationId());
			writeD(item.get2ndAugmentationId());
		}
		if (containsMask(mask, ItemListType.ELEMENTAL_ATTRIBUTE))
		{
			writeItemElemental(item);
		}
		if (containsMask(mask, ItemListType.ENCHANT_EFFECT))
		{
			writeItemEnchantEffect(item);
		}
		if (containsMask(mask, ItemListType.VISUAL_ID))
		{
			writeD(item.getVisualId()); // Item remodel visual ID
		}
	}
	
	protected static final int calculateMask(ItemInfo item)
	{
		int mask = 0;
		if (item.getAugmentationBonus() > 0)
		{
			mask |= ItemListType.AUGMENT_BONUS.getMask();
		}
		
		if (item.getAttackElementType() >= 0)
		{
			mask |= ItemListType.ELEMENTAL_ATTRIBUTE.getMask();
		}
		else
		{
			for (byte i = 0; i < 6; i++)
			{
				if (item.getElementDefAttr(i) >= 0)
				{
					mask |= ItemListType.ELEMENTAL_ATTRIBUTE.getMask();
					break;
				}
			}
		}
		
		if (item.getEnchantOptions() != null)
		{
			for (int id : item.getEnchantOptions())
			{
				if (id > 0)
				{
					mask |= ItemListType.ENCHANT_EFFECT.getMask();
					break;
				}
			}
		}
		
		if (item.getVisualId() > 0)
		{
			mask |= ItemListType.VISUAL_ID.getMask();
		}
		return mask;
	}
	
	protected void writeItemElementalAndEnchant(ItemInfo item)
	{
		writeItemElemental(item);
		writeItemEnchantEffect(item);
	}
	
	protected void writeItemElemental(ItemInfo item)
	{
		writeH(item.getAttackElementType());
		writeH(item.getAttackElementPower());
		for (byte i = 0; i < 6; i++)
		{
			writeH(item.getElementDefAttr(i));
		}
	}
	
	protected void writeItemEnchantEffect(ItemInfo item)
	{
		// Enchant Effects
		for (int op : item.getEnchantOptions())
		{
			writeD(op);
		}
	}
	
	protected void writeInventoryBlock(PcInventory inventory)
	{
		if (inventory.hasInventoryBlock())
		{
			writeH(inventory.getBlockItems().length);
			writeC(inventory.getBlockMode());
			for (int i : inventory.getBlockItems())
			{
				writeD(i);
			}
		}
		else
		{
			writeH(0x00);
		}
	}
	
	protected void writeCommissionItem(ItemInfo item)
	{
		writeD(0); // Always 0
		writeD(item.getItem().getId());
		writeQ(item.getCount());
		writeH(item.getItem().getType2());
		writeQ(item.getItem().getBodyPart());
		writeH(item.getEnchant());
		writeH(item.getCustomType2());
		writeItemElementalAndEnchant(item);
		writeD(item.getVisualId());
	}
}
