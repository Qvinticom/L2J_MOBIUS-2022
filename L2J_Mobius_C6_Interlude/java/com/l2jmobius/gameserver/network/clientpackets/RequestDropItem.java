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
import com.l2jmobius.gameserver.datatables.GmListTable;
import com.l2jmobius.gameserver.instancemanager.CursedWeaponsManager;
import com.l2jmobius.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance.SkillDat;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jmobius.gameserver.network.serverpackets.ItemList;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.templates.item.L2EtcItemType;
import com.l2jmobius.gameserver.templates.item.L2Item;
import com.l2jmobius.gameserver.util.IllegalPlayerAction;
import com.l2jmobius.gameserver.util.Util;

public final class RequestDropItem extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(RequestDropItem.class.getName());
	
	private int _objectId;
	private int _count;
	private int _x;
	private int _y;
	private int _z;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_count = readD();
		_x = readD();
		_y = readD();
		_z = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if ((activeChar == null) || activeChar.isDead())
		{
			return;
		}
		
		if (activeChar.isGM() && (activeChar.getAccessLevel().getLevel() > 2))
		{ // just head gm and admin can drop items on the ground
			sendPacket(SystemMessage.sendString("You have not right to discard anything from inventory"));
			return;
		}
		
		// Fix against safe enchant exploit
		if (activeChar.getActiveEnchantItem() != null)
		{
			sendPacket(SystemMessage.sendString("You can't discard items during enchant."));
			return;
		}
		
		// Flood protect drop to avoid packet lag
		if (!getClient().getFloodProtectors().getDropItem().tryPerformAction("drop item"))
		{
			return;
		}
		
		final L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		
		if ((item == null) || (_count == 0) || !activeChar.validateItemManipulation(_objectId, "drop"))
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
			return;
		}
		
		if ((!Config.ALLOW_DISCARDITEM && !activeChar.isGM()) || (!item.isDropable()))
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
			return;
		}
		
		if (item.isAugmented())
		{
			activeChar.sendPacket(SystemMessageId.AUGMENTED_ITEM_CANNOT_BE_DISCARDED);
			return;
		}
		
		if ((item.getItemType() == L2EtcItemType.QUEST) && !(activeChar.isGM()))
		{
			return;
		}
		
		// Drop item disabled by config
		if (activeChar.isGM() && Config.GM_TRADE_RESTRICTED_ITEMS)
		{
			activeChar.sendMessage("Drop item disabled for GM by config!");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final int itemId = item.getItemId();
		
		// Cursed Weapons cannot be dropped
		if (CursedWeaponsManager.getInstance().isCursed(itemId))
		{
			return;
		}
		
		if (_count > item.getCount())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
			return;
		}
		
		if ((Config.PLAYER_SPAWN_PROTECTION > 0) && activeChar.isInvul() && !activeChar.isGM())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
			return;
		}
		
		if (_count <= 0)
		{
			activeChar.setAccessLevel(-1); // ban
			Util.handleIllegalPlayerAction(activeChar, "[RequestDropItem] count <= 0! ban! oid: " + _objectId + " owner: " + activeChar.getName(), IllegalPlayerAction.PUNISH_KICK);
			return;
		}
		
		if (!item.isStackable() && (_count > 1))
		{
			Util.handleIllegalPlayerAction(activeChar, "[RequestDropItem] count > 1 but item is not stackable! ban! oid: " + _objectId + " owner: " + activeChar.getName(), IllegalPlayerAction.PUNISH_KICK);
			return;
		}
		
		if (!activeChar.getAccessLevel().allowTransaction())
		{
			activeChar.sendMessage("Unsufficient privileges.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (activeChar.isProcessingTransaction() || (activeChar.getPrivateStoreType() != 0))
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
			return;
		}
		
		if (activeChar.isFishing())
		{
			// You can't mount, dismount, break and drop items while fishing
			activeChar.sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_2);
			return;
		}
		
		// Cannot discard item that the skill is consuming.
		if (activeChar.isCastingNow())
		{
			final SkillDat skillDat = activeChar.getCurrentSkill();
			if ((skillDat != null) && (skillDat.getSkill().getItemConsumeId() == item.getItemId()) //
				&& ((activeChar.getInventory().getInventoryItemCount(item.getItemId(), -1) - skillDat.getSkill().getItemConsume()) < _count))
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
				return;
			}
		}
		
		if ((L2Item.TYPE2_QUEST == item.getItem().getType2()) && !activeChar.isGM())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_EXCHANGE_ITEM);
			return;
		}
		
		if (!activeChar.isInsideRadius(_x, _y, 150, false) || (Math.abs(_z - activeChar.getZ()) > 50))
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_DISTANCE_TOO_FAR);
			return;
		}
		
		if (item.isEquipped())
		{
			// Remove augementation boni on unequip
			if (item.isAugmented())
			{
				item.getAugmentation().removeBonus(activeChar);
			}
			
			final L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInBodySlotAndRecord(item.getItem().getBodyPart());
			final InventoryUpdate iu = new InventoryUpdate();
			
			for (L2ItemInstance element : unequiped)
			{
				activeChar.checkSSMatch(null, element);
				
				iu.addModifiedItem(element);
			}
			activeChar.sendPacket(iu);
			activeChar.broadcastUserInfo();
			
			final ItemList il = new ItemList(activeChar, true);
			activeChar.sendPacket(il);
		}
		
		final L2ItemInstance dropedItem = activeChar.dropItem("Drop", _objectId, _count, _x, _y, _z, null, false, false);
		if ((dropedItem != null) && (dropedItem.getItemId() == 57) && (dropedItem.getCount() >= 1000000) && (Config.RATE_DROP_ADENA <= 200))
		{
			final String msg = "Character (" + activeChar.getName() + ") has dropped (" + dropedItem.getCount() + ")adena at (" + _x + "," + _y + "," + _z + ")";
			LOGGER.warning(msg);
			GmListTable.broadcastMessageToGMs(msg);
		}
	}
}
