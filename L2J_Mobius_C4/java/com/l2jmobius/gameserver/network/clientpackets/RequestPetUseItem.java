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

import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.handler.IItemHandler;
import com.l2jmobius.gameserver.handler.ItemHandler;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.L2PetDataTable;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PetInstance;
import com.l2jmobius.gameserver.network.serverpackets.PetItemList;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.templates.L2ArmorType;
import com.l2jmobius.gameserver.templates.L2Item;

public class RequestPetUseItem extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(RequestPetUseItem.class.getName());
	private static final String _C__8A_REQUESTPETUSEITEM = "[C] 8a RequestPetUseItem";
	
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		final L2PetInstance pet = (L2PetInstance) activeChar.getPet();
		
		if (pet == null)
		{
			return;
		}
		
		final L2ItemInstance item = pet.getInventory().getItemByObjectId(_objectId);
		
		if (item == null)
		{
			return;
		}
		
		if (item.isWear())
		{
			return;
		}
		
		final int itemId = item.getItemId();
		if (activeChar.isAlikeDead() || pet.isDead())
		
		{
			SystemMessage sm = new SystemMessage(SystemMessage.S1_CANNOT_BE_USED);
			sm.addItemName(item.getItemId());
			activeChar.sendPacket(sm);
			
			sm = null;
			return;
		}
		
		if (Config.DEBUG)
		{
			_log.finest(activeChar.getObjectId() + ": pet use item " + _objectId);
		}
		
		// Check if the item matches the pet
		if (item.isEquipable())
		{
			if (item.getItem().getBodyPart() == L2Item.SLOT_NECK)
			{
				if (item.getItem().getItemType() == L2ArmorType.PET)
				{
					useItem(pet, item, activeChar);
					return;
				}
			}
			
			if (L2PetDataTable.isWolf(pet.getNpcId()) && // wolf
				
				item.getItem().isForWolf())
			{
				useItem(pet, item, activeChar);
				return;
			}
			else if (L2PetDataTable.isHatchling(pet.getNpcId()) && // hatchlings
				item.getItem().isForHatchling())
			{
				useItem(pet, item, activeChar);
				return;
			}
			else if (L2PetDataTable.isStrider(pet.getNpcId()) && // striders
				item.getItem().isForStrider())
			{
				useItem(pet, item, activeChar);
				return;
			}
			
			else
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.ITEM_NOT_FOR_PETS));
				return;
			}
		}
		else if (L2PetDataTable.isPetFood(itemId))
		{
			if (L2PetDataTable.isWolf(pet.getNpcId()) && L2PetDataTable.isWolfFood(itemId))
			{
				useItem(pet, item, activeChar);
				return;
			}
			else if (L2PetDataTable.isSinEater(pet.getNpcId()) && L2PetDataTable.isSinEaterFood(itemId))
			{
				useItem(pet, item, activeChar);
				return;
			}
			else if (L2PetDataTable.isHatchling(pet.getNpcId()) && L2PetDataTable.isHatchlingFood(itemId))
			{
				useItem(pet, item, activeChar);
				return;
			}
			else if (L2PetDataTable.isStrider(pet.getNpcId()) && L2PetDataTable.isStriderFood(itemId))
			{
				useItem(pet, item, activeChar);
				return;
			}
			else if (L2PetDataTable.isWyvern(pet.getNpcId()) && L2PetDataTable.isWyvernFood(itemId))
			{
				useItem(pet, item, activeChar);
				return;
			}
			else if (L2PetDataTable.isBaby(pet.getNpcId()) && L2PetDataTable.isBabyFood(itemId))
			{
				useItem(pet, item, activeChar);
				return;
			}
			else
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.PET_CANNOT_USE_ITEM));
				return;
			}
		}
		
		final IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getItemId());
		
		if (handler != null)
		{
			useItem(pet, item, activeChar);
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.PET_CANNOT_USE_ITEM));
		}
		
		return;
	}
	
	private synchronized void useItem(L2PetInstance pet, L2ItemInstance item, L2PcInstance activeChar)
	{
		if (item.isEquipable())
		{
			if (item.isEquipped())
			{
				pet.getInventory().unEquipItemInSlot(item.getEquipSlot());
				switch (item.getItem().getBodyPart())
				{
					case L2Item.SLOT_R_HAND:
						pet.setWeapon(0);
						break;
					case L2Item.SLOT_CHEST:
						pet.setArmor(0);
						break;
					case L2Item.SLOT_NECK:
						pet.setJewel(0);
						break;
				}
			}
			else
			{
				pet.getInventory().equipItem(item);
				switch (item.getItem().getBodyPart())
				{
					case L2Item.SLOT_R_HAND:
						pet.setWeapon(item.getItemId());
						break;
					case L2Item.SLOT_CHEST:
						pet.setArmor(item.getItemId());
						break;
					case L2Item.SLOT_NECK:
						pet.setJewel(item.getItemId());
						break;
				}
			}
			
			activeChar.sendPacket(new PetItemList(pet));
			pet.updateAndBroadcastStatus(1);
		}
		else
		{
			final IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getItemId());
			
			if (handler == null)
			{
				_log.warning("no itemhandler registered for itemId:" + item.getItemId());
			}
			else
			
			{
				handler.useItem(pet, item);
				
				pet.updateAndBroadcastStatus(1);
				
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__8A_REQUESTPETUSEITEM;
	}
}