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
package com.l2jmobius.gameserver.network.clientpackets.appearance;

import com.l2jmobius.gameserver.data.xml.impl.AppearanceItemData;
import com.l2jmobius.gameserver.enums.ItemLocation;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.request.ShapeShiftingItemRequest;
import com.l2jmobius.gameserver.model.itemcontainer.PcInventory;
import com.l2jmobius.gameserver.model.items.L2Item;
import com.l2jmobius.gameserver.model.items.appearance.AppearanceStone;
import com.l2jmobius.gameserver.model.items.appearance.AppearanceType;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;
import com.l2jmobius.gameserver.model.items.type.ArmorType;
import com.l2jmobius.gameserver.model.items.type.WeaponType;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket;
import com.l2jmobius.gameserver.network.serverpackets.appearance.ExPutShapeShiftingExtractionItemResult;
import com.l2jmobius.gameserver.network.serverpackets.appearance.ExShapeShiftingResult;

/**
 * @author UnAfraid
 */
public class RequestExTryToPutShapeShiftingEnchantSupportItem extends L2GameClientPacket
{
	private int _targetItemObjId;
	private int _extracItemObjId;
	
	@Override
	protected void readImpl()
	{
		_targetItemObjId = readD();
		_extracItemObjId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		final ShapeShiftingItemRequest request = player.getRequest(ShapeShiftingItemRequest.class);
		
		if (player.isInStoreMode() || player.isInCraftMode() || player.isProcessingRequest() || player.isProcessingTransaction() || (request == null))
		{
			player.sendPacket(ExShapeShiftingResult.FAILED);
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THIS_SYSTEM_DURING_TRADING_PRIVATE_STORE_AND_WORKSHOP_SETUP);
			return;
		}
		
		final PcInventory inventory = player.getInventory();
		final L2ItemInstance targetItem = inventory.getItemByObjectId(_targetItemObjId);
		final L2ItemInstance extracItem = inventory.getItemByObjectId(_extracItemObjId);
		L2ItemInstance stone = request.getAppearanceStone();
		if ((targetItem == null) || (extracItem == null) || (stone == null))
		{
			player.sendPacket(ExShapeShiftingResult.FAILED);
			player.removeRequest(ShapeShiftingItemRequest.class);
			return;
		}
		
		if (!extracItem.isAppearanceable())
		{
			player.sendPacket(ExShapeShiftingResult.FAILED);
			player.removeRequest(ShapeShiftingItemRequest.class);
			return;
		}
		
		if ((extracItem.getItemLocation() != ItemLocation.INVENTORY) && (extracItem.getItemLocation() != ItemLocation.PAPERDOLL))
		{
			player.sendPacket(ExShapeShiftingResult.FAILED);
			player.removeRequest(ShapeShiftingItemRequest.class);
			return;
		}
		
		if ((stone = inventory.getItemByObjectId(stone.getObjectId())) == null)
		{
			player.sendPacket(ExShapeShiftingResult.FAILED);
			player.removeRequest(ShapeShiftingItemRequest.class);
			return;
		}
		final AppearanceStone appearanceStone = AppearanceItemData.getInstance().getStone(stone.getId());
		if (appearanceStone == null)
		{
			player.sendPacket(ExShapeShiftingResult.FAILED);
			player.removeRequest(ShapeShiftingItemRequest.class);
			return;
		}
		
		if ((appearanceStone.getType() == AppearanceType.RESTORE) || (appearanceStone.getType() == AppearanceType.FIXED))
		{
			player.sendPacket(ExShapeShiftingResult.FAILED);
			player.removeRequest(ShapeShiftingItemRequest.class);
			return;
		}
		
		if (extracItem.getVisualId() > 0)
		{
			player.sendPacket(ExShapeShiftingResult.FAILED);
			player.sendPacket(SystemMessageId.YOU_CANNOT_EXTRACT_FROM_A_MODIFIED_ITEM);
			player.removeRequest(ShapeShiftingItemRequest.class);
			return;
		}
		
		if (appearanceStone.getWeaponType() != WeaponType.NONE)
		{
			if (!targetItem.isWeapon() || (targetItem.getItemType() != appearanceStone.getWeaponType()))
			{
				player.sendPacket(ExShapeShiftingResult.FAILED);
				player.sendPacket(SystemMessageId.THIS_ITEM_DOES_NOT_MEET_REQUIREMENTS);
				player.removeRequest(ShapeShiftingItemRequest.class);
				return;
			}
		}
		
		if (appearanceStone.getWeaponType() != WeaponType.NONE)
		{
			if (!targetItem.isWeapon() || (targetItem.getItemType() != appearanceStone.getWeaponType()))
			{
				player.sendPacket(ExShapeShiftingResult.FAILED);
				player.sendPacket(SystemMessageId.THIS_ITEM_DOES_NOT_MEET_REQUIREMENTS);
				player.removeRequest(ShapeShiftingItemRequest.class);
				return;
			}
			
			switch (appearanceStone.getHandType())
			{
				case ONE_HANDED:
				{
					if ((targetItem.getItem().getBodyPart() & L2Item.SLOT_R_HAND) != L2Item.SLOT_R_HAND)
					{
						player.sendPacket(ExShapeShiftingResult.FAILED);
						player.sendPacket(SystemMessageId.THIS_ITEM_DOES_NOT_MEET_REQUIREMENTS);
						player.removeRequest(ShapeShiftingItemRequest.class);
						return;
					}
					break;
				}
				case TWO_HANDED:
				{
					if ((targetItem.getItem().getBodyPart() & L2Item.SLOT_LR_HAND) != L2Item.SLOT_LR_HAND)
					{
						player.sendPacket(ExShapeShiftingResult.FAILED);
						player.sendPacket(SystemMessageId.THIS_ITEM_DOES_NOT_MEET_REQUIREMENTS);
						player.removeRequest(ShapeShiftingItemRequest.class);
						return;
					}
					break;
				}
			}
			
			switch (appearanceStone.getMagicType())
			{
				case MAGICAL:
				{
					if (!targetItem.getItem().isMagicWeapon())
					{
						player.sendPacket(ExShapeShiftingResult.FAILED);
						player.sendPacket(SystemMessageId.THIS_ITEM_DOES_NOT_MEET_REQUIREMENTS);
						player.removeRequest(ShapeShiftingItemRequest.class);
						return;
					}
					break;
				}
				case PHYISICAL:
				{
					if (targetItem.getItem().isMagicWeapon())
					{
						player.sendPacket(ExShapeShiftingResult.FAILED);
						player.sendPacket(SystemMessageId.THIS_ITEM_DOES_NOT_MEET_REQUIREMENTS);
						player.removeRequest(ShapeShiftingItemRequest.class);
						return;
					}
				}
			}
		}
		
		if (appearanceStone.getArmorType() != ArmorType.NONE)
		{
			switch (appearanceStone.getArmorType())
			{
				case SHIELD:
				{
					if (!targetItem.isArmor() || (targetItem.getItemType() != ArmorType.SHIELD))
					{
						player.sendPacket(ExShapeShiftingResult.FAILED);
						player.sendPacket(SystemMessageId.THIS_ITEM_DOES_NOT_MEET_REQUIREMENTS);
						player.removeRequest(ShapeShiftingItemRequest.class);
						return;
					}
					break;
				}
				case SIGIL:
				{
					if (!targetItem.isArmor() || (targetItem.getItemType() != ArmorType.SIGIL))
					{
						player.sendPacket(ExShapeShiftingResult.FAILED);
						player.sendPacket(SystemMessageId.THIS_ITEM_DOES_NOT_MEET_REQUIREMENTS);
						player.removeRequest(ShapeShiftingItemRequest.class);
						return;
					}
				}
			}
		}
		
		if (extracItem.getOwnerId() != player.getObjectId())
		{
			player.sendPacket(ExShapeShiftingResult.FAILED);
			player.removeRequest(ShapeShiftingItemRequest.class);
			return;
		}
		
		request.setAppearanceExtractItem(extracItem);
		player.sendPacket(ExPutShapeShiftingExtractionItemResult.SUCCESS);
	}
	
	@Override
	public String getType()
	{
		return getClass().getSimpleName();
	}
}
