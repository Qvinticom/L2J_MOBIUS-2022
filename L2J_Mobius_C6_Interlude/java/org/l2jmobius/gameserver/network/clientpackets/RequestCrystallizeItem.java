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
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.itemcontainer.PlayerInventory;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.PacketLogger;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.ItemList;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.IllegalPlayerAction;
import org.l2jmobius.gameserver.util.Util;

public class RequestCrystallizeItem implements IClientIncomingPacket
{
	private int _objectId;
	private int _count;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_objectId = packet.readD();
		_count = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			PacketLogger.warning("RequestCrystalizeItem: activeChar was null.");
			return;
		}
		
		if (!client.getFloodProtectors().canPerformTransaction())
		{
			player.sendMessage("You crystallizing too fast.");
			return;
		}
		
		if (_count <= 0)
		{
			Util.handleIllegalPlayerAction(player, "[RequestCrystallizeItem] count <= 0! ban! oid: " + _objectId + " owner: " + player.getName(), IllegalPlayerAction.PUNISH_KICK);
			return;
		}
		
		if ((player.getPrivateStoreType() != 0) || player.isInCrystallize())
		{
			player.sendPacket(SystemMessageId.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}
		
		final int skillLevel = player.getSkillLevel(Skill.SKILL_CRYSTALLIZE);
		if (skillLevel <= 0)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_MAY_NOT_CRYSTALLIZE_THIS_ITEM_YOUR_CRYSTALLIZATION_SKILL_LEVEL_IS_TOO_LOW));
			final ActionFailed af = ActionFailed.STATIC_PACKET;
			player.sendPacket(af);
			return;
		}
		
		final PlayerInventory inventory = player.getInventory();
		if (inventory != null)
		{
			final Item item = inventory.getItemByObjectId(_objectId);
			if ((item == null) || item.isWear())
			{
				final ActionFailed af = ActionFailed.STATIC_PACKET;
				player.sendPacket(af);
				return;
			}
			
			final int itemId = item.getItemId();
			if (((itemId >= 6611) && (itemId <= 6621)) || (itemId == 6842))
			{
				return;
			}
			
			if (_count > item.getCount())
			{
				_count = player.getInventory().getItemByObjectId(_objectId).getCount();
			}
		}
		
		final Item itemToRemove = player.getInventory().getItemByObjectId(_objectId);
		if ((itemToRemove == null) || itemToRemove.isWear())
		{
			return;
		}
		
		if (!itemToRemove.getItem().isCrystallizable() || (itemToRemove.getItem().getCrystalCount() <= 0) || (itemToRemove.getItem().getCrystalType() == ItemTemplate.CRYSTAL_NONE))
		{
			PacketLogger.warning(player.getObjectId() + " tried to crystallize " + itemToRemove.getItem().getItemId());
			return;
		}
		
		// Check if the char can crystallize C items and return if false;
		if ((itemToRemove.getItem().getCrystalType() == ItemTemplate.CRYSTAL_C) && (skillLevel <= 1))
		{
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_MAY_NOT_CRYSTALLIZE_THIS_ITEM_YOUR_CRYSTALLIZATION_SKILL_LEVEL_IS_TOO_LOW));
			final ActionFailed af = ActionFailed.STATIC_PACKET;
			player.sendPacket(af);
			return;
		}
		
		// Check if the user can crystallize B items and return if false;
		if ((itemToRemove.getItem().getCrystalType() == ItemTemplate.CRYSTAL_B) && (skillLevel <= 2))
		{
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_MAY_NOT_CRYSTALLIZE_THIS_ITEM_YOUR_CRYSTALLIZATION_SKILL_LEVEL_IS_TOO_LOW));
			final ActionFailed af = ActionFailed.STATIC_PACKET;
			player.sendPacket(af);
			return;
		}
		
		// Check if the user can crystallize A items and return if false;
		if ((itemToRemove.getItem().getCrystalType() == ItemTemplate.CRYSTAL_A) && (skillLevel <= 3))
		{
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_MAY_NOT_CRYSTALLIZE_THIS_ITEM_YOUR_CRYSTALLIZATION_SKILL_LEVEL_IS_TOO_LOW));
			final ActionFailed af = ActionFailed.STATIC_PACKET;
			player.sendPacket(af);
			return;
		}
		
		// Check if the user can crystallize S items and return if false;
		if ((itemToRemove.getItem().getCrystalType() == ItemTemplate.CRYSTAL_S) && (skillLevel <= 4))
		{
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_MAY_NOT_CRYSTALLIZE_THIS_ITEM_YOUR_CRYSTALLIZATION_SKILL_LEVEL_IS_TOO_LOW));
			final ActionFailed af = ActionFailed.STATIC_PACKET;
			player.sendPacket(af);
			return;
		}
		
		player.setInCrystallize(true);
		
		// unequip if needed
		if (itemToRemove.isEquipped())
		{
			if (itemToRemove.isAugmented())
			{
				itemToRemove.getAugmentation().removeBonus(player);
			}
			
			final InventoryUpdate iu = new InventoryUpdate();
			for (Item element : player.getInventory().unEquipItemInSlotAndRecord(itemToRemove.getEquipSlot()))
			{
				iu.addModifiedItem(element);
			}
			player.sendPacket(iu);
			// player.updatePDef();
			// player.updatePAtk();
			// player.updateMDef();
			// player.updateMAtk();
			// player.updateAccuracy();
			// player.updateCriticalChance();
		}
		
		// remove from inventory
		final Item removedItem = player.getInventory().destroyItem("Crystalize", _objectId, _count, player, null);
		
		// add crystals
		final int crystalId = itemToRemove.getItem().getCrystalItemId();
		final int crystalAmount = itemToRemove.getCrystalCount();
		final Item createditem = player.getInventory().addItem("Crystalize", crystalId, crystalAmount, player, itemToRemove);
		final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S2_S1_S);
		sm.addItemName(crystalId);
		sm.addNumber(crystalAmount);
		player.sendPacket(sm);
		
		// send inventory update
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			final InventoryUpdate iu = new InventoryUpdate();
			if (removedItem.getCount() == 0)
			{
				iu.addRemovedItem(removedItem);
			}
			else
			{
				iu.addModifiedItem(removedItem);
			}
			
			if (createditem.getCount() != crystalAmount)
			{
				iu.addModifiedItem(createditem);
			}
			else
			{
				iu.addNewItem(createditem);
			}
			
			player.sendPacket(iu);
		}
		else
		{
			player.sendPacket(new ItemList(player, false));
		}
		
		// status & user info
		final StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
		
		player.broadcastUserInfo();
		
		final World world = World.getInstance();
		world.removeObject(removedItem);
		
		player.setInCrystallize(false);
	}
}
