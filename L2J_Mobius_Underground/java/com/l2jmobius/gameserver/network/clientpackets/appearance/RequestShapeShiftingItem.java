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

import com.l2jmobius.commons.network.PacketReader;
import com.l2jmobius.gameserver.data.xml.impl.AppearanceItemData;
import com.l2jmobius.gameserver.enums.InventorySlot;
import com.l2jmobius.gameserver.enums.ItemLocation;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.request.ShapeShiftingItemRequest;
import com.l2jmobius.gameserver.model.itemcontainer.PcInventory;
import com.l2jmobius.gameserver.model.items.L2Item;
import com.l2jmobius.gameserver.model.items.appearance.AppearanceStone;
import com.l2jmobius.gameserver.model.items.appearance.AppearanceTargetType;
import com.l2jmobius.gameserver.model.items.appearance.AppearanceType;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;
import com.l2jmobius.gameserver.model.items.type.ArmorType;
import com.l2jmobius.gameserver.model.items.type.WeaponType;
import com.l2jmobius.gameserver.model.variables.ItemVariables;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.client.L2GameClient;
import com.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import com.l2jmobius.gameserver.network.serverpackets.ExAdenaInvenCount;
import com.l2jmobius.gameserver.network.serverpackets.ExUserInfoEquipSlot;
import com.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.network.serverpackets.appearance.ExPutShapeShiftingTargetItemResult;
import com.l2jmobius.gameserver.network.serverpackets.appearance.ExShapeShiftingResult;

/**
 * @author UnAfraid
 */
public class RequestShapeShiftingItem implements IClientIncomingPacket
{
	private int _targetItemObjId;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_targetItemObjId = packet.readD();
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		final L2PcInstance player = client.getActiveChar();
		if (player == null)
		{
			return;
		}
		
		final ShapeShiftingItemRequest request = player.getRequest(ShapeShiftingItemRequest.class);
		
		if (player.isInStoreMode() || player.isInCraftMode() || player.isProcessingRequest() || player.isProcessingTransaction() || (request == null))
		{
			client.sendPacket(ExShapeShiftingResult.FAILED);
			client.sendPacket(SystemMessageId.YOU_CANNOT_USE_THIS_SYSTEM_DURING_TRADING_PRIVATE_STORE_AND_WORKSHOP_SETUP);
			return;
		}
		
		final PcInventory inventory = player.getInventory();
		final L2ItemInstance targetItem = inventory.getItemByObjectId(_targetItemObjId);
		L2ItemInstance stone = request.getAppearanceStone();
		
		if ((targetItem == null) || (stone == null))
		{
			client.sendPacket(ExShapeShiftingResult.FAILED);
			player.removeRequest(ShapeShiftingItemRequest.class);
			return;
		}
		
		if (stone.getOwnerId() != player.getObjectId())
		{
			client.sendPacket(ExShapeShiftingResult.FAILED);
			player.removeRequest(ShapeShiftingItemRequest.class);
			return;
		}
		
		if (!targetItem.getItem().isAppearanceable())
		{
			client.sendPacket(ExShapeShiftingResult.FAILED);
			player.removeRequest(ShapeShiftingItemRequest.class);
			return;
		}
		
		if ((targetItem.getItemLocation() != ItemLocation.INVENTORY) && (targetItem.getItemLocation() != ItemLocation.PAPERDOLL))
		{
			client.sendPacket(ExShapeShiftingResult.FAILED);
			player.removeRequest(ShapeShiftingItemRequest.class);
			return;
		}
		
		stone = inventory.getItemByObjectId(stone.getObjectId());
		if (stone == null)
		{
			client.sendPacket(ExShapeShiftingResult.FAILED);
			player.removeRequest(ShapeShiftingItemRequest.class);
			return;
		}
		
		final AppearanceStone appearanceStone = AppearanceItemData.getInstance().getStone(stone.getId());
		if (appearanceStone == null)
		{
			client.sendPacket(ExShapeShiftingResult.FAILED);
			player.removeRequest(ShapeShiftingItemRequest.class);
			return;
		}
		
		if (((appearanceStone.getType() != AppearanceType.RESTORE) && (targetItem.getVisualId() > 0)) || ((appearanceStone.getType() == AppearanceType.RESTORE) && (targetItem.getVisualId() == 0)))
		{
			client.sendPacket(ExShapeShiftingResult.FAILED);
			player.removeRequest(ShapeShiftingItemRequest.class);
			return;
		}
		
		// TODO: Handle hair accessory!
		// if (!targetItem.isEtcItem() && (targetItem.getItem().getCrystalType() == CrystalType.NONE))
		{
			// client.sendPacket(ExShapeShiftingResult.FAILED);
			// player.removeRequest(ShapeShiftingItemRequest.class.getName());
			// return;
		}
		
		if (!appearanceStone.getTargetTypes().contains(AppearanceTargetType.ACCESSORY)) // accessory tempfix
		{
			if (!appearanceStone.getCrystalTypes().isEmpty() && !appearanceStone.getCrystalTypes().contains(targetItem.getItem().getCrystalType()))
			{
				client.sendPacket(ExShapeShiftingResult.FAILED);
				player.removeRequest(ShapeShiftingItemRequest.class);
				return;
			}
		}
		
		if (appearanceStone.getTargetTypes().isEmpty())
		{
			client.sendPacket(ExShapeShiftingResult.FAILED);
			player.removeRequest(ShapeShiftingItemRequest.class);
			return;
		}
		
		if (!appearanceStone.getTargetTypes().contains(AppearanceTargetType.ALL))
		{
			if (targetItem.isWeapon() && !appearanceStone.getTargetTypes().contains(AppearanceTargetType.WEAPON))
			{
				client.sendPacket(ExShapeShiftingResult.FAILED);
				player.removeRequest(ShapeShiftingItemRequest.class);
				return;
				
			}
			else if (targetItem.isArmor() && !appearanceStone.getTargetTypes().contains(AppearanceTargetType.ARMOR) && !appearanceStone.getTargetTypes().contains(AppearanceTargetType.ACCESSORY))
			{
				client.sendPacket(ExShapeShiftingResult.FAILED);
				player.removeRequest(ShapeShiftingItemRequest.class);
				return;
			}
			else if (targetItem.isArmor() && !appearanceStone.getBodyParts().isEmpty() && !appearanceStone.getBodyParts().contains(targetItem.getItem().getBodyPart()))
			{
				client.sendPacket(ExPutShapeShiftingTargetItemResult.FAILED);
				player.removeRequest(ShapeShiftingItemRequest.class);
				return;
			}
		}
		
		if (appearanceStone.getWeaponType() != WeaponType.NONE)
		{
			if (!targetItem.isWeapon() || (targetItem.getItemType() != appearanceStone.getWeaponType()))
			{
				client.sendPacket(ExShapeShiftingResult.FAILED);
				player.removeRequest(ShapeShiftingItemRequest.class);
				return;
			}
			
			switch (appearanceStone.getHandType())
			{
				case ONE_HANDED:
				{
					if ((targetItem.getItem().getBodyPart() & L2Item.SLOT_R_HAND) != L2Item.SLOT_R_HAND)
					{
						client.sendPacket(ExShapeShiftingResult.FAILED);
						player.removeRequest(ShapeShiftingItemRequest.class);
						return;
					}
					break;
				}
				case TWO_HANDED:
				{
					if ((targetItem.getItem().getBodyPart() & L2Item.SLOT_LR_HAND) != L2Item.SLOT_LR_HAND)
					{
						client.sendPacket(ExShapeShiftingResult.FAILED);
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
						client.sendPacket(ExShapeShiftingResult.FAILED);
						player.removeRequest(ShapeShiftingItemRequest.class);
						return;
					}
					break;
				}
				case PHYISICAL:
				{
					if (targetItem.getItem().isMagicWeapon())
					{
						client.sendPacket(ExShapeShiftingResult.FAILED);
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
						client.sendPacket(ExShapeShiftingResult.FAILED);
						player.removeRequest(ShapeShiftingItemRequest.class);
						return;
					}
					break;
				}
				case SIGIL:
				{
					if (!targetItem.isArmor() || (targetItem.getItemType() != ArmorType.SIGIL))
					{
						client.sendPacket(ExShapeShiftingResult.FAILED);
						player.removeRequest(ShapeShiftingItemRequest.class);
						return;
					}
				}
			}
		}
		
		final L2ItemInstance extracItem = request.getAppearanceExtractItem();
		
		int extracItemId = 0;
		if ((appearanceStone.getType() != AppearanceType.RESTORE) && (appearanceStone.getType() != AppearanceType.FIXED))
		{
			if (extracItem == null)
			{
				client.sendPacket(ExShapeShiftingResult.FAILED);
				player.removeRequest(ShapeShiftingItemRequest.class);
				return;
			}
			
			if (!targetItem.getItem().isAppearanceable())
			{
				client.sendPacket(ExShapeShiftingResult.FAILED);
				player.removeRequest(ShapeShiftingItemRequest.class);
				return;
			}
			
			if ((extracItem.getItemLocation() != ItemLocation.INVENTORY) && (extracItem.getItemLocation() != ItemLocation.PAPERDOLL))
			{
				client.sendPacket(ExShapeShiftingResult.FAILED);
				player.removeRequest(ShapeShiftingItemRequest.class);
				return;
			}
			
			if (!extracItem.isEtcItem() && (targetItem.getItem().getCrystalType().ordinal() <= extracItem.getItem().getCrystalType().ordinal()))
			{
				client.sendPacket(ExShapeShiftingResult.FAILED);
				player.removeRequest(ShapeShiftingItemRequest.class);
				return;
			}
			
			if (extracItem.getVisualId() > 0)
			{
				client.sendPacket(ExShapeShiftingResult.FAILED);
				player.removeRequest(ShapeShiftingItemRequest.class);
				return;
			}
			
			if (extracItem.getOwnerId() != player.getObjectId())
			{
				client.sendPacket(ExShapeShiftingResult.FAILED);
				player.removeRequest(ShapeShiftingItemRequest.class);
				return;
			}
			extracItemId = extracItem.getId();
		}
		
		if (targetItem.getOwnerId() != player.getObjectId())
		{
			client.sendPacket(ExShapeShiftingResult.FAILED);
			player.removeRequest(ShapeShiftingItemRequest.class);
			return;
		}
		
		final long cost = appearanceStone.getCost();
		if (cost > player.getAdena())
		{
			client.sendPacket(SystemMessageId.YOU_CANNOT_MODIFY_AS_YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			client.sendPacket(ExShapeShiftingResult.FAILED);
			player.removeRequest(ShapeShiftingItemRequest.class);
			return;
		}
		
		if (stone.getCount() < 1L)
		{
			client.sendPacket(ExShapeShiftingResult.FAILED);
			player.removeRequest(ShapeShiftingItemRequest.class);
			return;
		}
		if (appearanceStone.getType() == AppearanceType.NORMAL)
		{
			if (inventory.destroyItem(getClass().getSimpleName(), extracItem, 1, player, this) == null)
			{
				client.sendPacket(ExShapeShiftingResult.FAILED);
				player.removeRequest(ShapeShiftingItemRequest.class);
				return;
			}
		}
		
		inventory.destroyItem(getClass().getSimpleName(), stone, 1, player, this);
		player.reduceAdena(getClass().getSimpleName(), cost, extracItem, true);
		
		switch (appearanceStone.getType())
		{
			case RESTORE:
			{
				targetItem.setVisualId(0);
				targetItem.getVariables().set(ItemVariables.VISUAL_APPEARANCE_STONE_ID, 0);
				break;
			}
			case NORMAL:
			{
				targetItem.setVisualId(extracItem.getId());
				break;
			}
			case BLESSED:
			{
				targetItem.setVisualId(extracItem.getId());
				break;
			}
			case FIXED:
			{
				targetItem.setVisualId(appearanceStone.getVisualId());
				targetItem.getVariables().set(ItemVariables.VISUAL_APPEARANCE_STONE_ID, appearanceStone.getId());
				break;
			}
		}
		
		if ((appearanceStone.getType() != AppearanceType.RESTORE) && (appearanceStone.getLifeTime() > 0))
		{
			targetItem.getVariables().set(ItemVariables.VISUAL_APPEARANCE_LIFE_TIME, System.currentTimeMillis() + appearanceStone.getLifeTime());
			targetItem.scheduleVisualLifeTime();
		}
		
		targetItem.getVariables().storeMe();
		if (appearanceStone.getCost() > 0)
		{
			client.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_SPENT_S1_ON_A_SUCCESSFUL_APPEARANCE_MODIFICATION).addLong(cost));
		}
		else
		{
			client.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_S_APPEARANCE_MODIFICATION_HAS_FINISHED).addItemName(targetItem.getDisplayId()));
		}
		
		final InventoryUpdate iu = new InventoryUpdate();
		iu.addModifiedItem(targetItem);
		if (inventory.getItemByObjectId(stone.getObjectId()) == null)
		{
			iu.addRemovedItem(stone);
		}
		else
		{
			iu.addModifiedItem(stone);
		}
		player.sendInventoryUpdate(iu);
		
		player.removeRequest(ShapeShiftingItemRequest.class);
		client.sendPacket(new ExShapeShiftingResult(ExShapeShiftingResult.RESULT_SUCCESS, targetItem.getId(), extracItemId));
		if (targetItem.isEquipped())
		{
			player.broadcastUserInfo();
			final ExUserInfoEquipSlot slots = new ExUserInfoEquipSlot(player, false);
			for (InventorySlot slot : InventorySlot.values())
			{
				if (slot.getSlot() == targetItem.getLocationSlot())
				{
					slots.addComponentType(slot);
				}
			}
			client.sendPacket(slots);
		}
		client.sendPacket(new ExAdenaInvenCount(player));
	}
}
