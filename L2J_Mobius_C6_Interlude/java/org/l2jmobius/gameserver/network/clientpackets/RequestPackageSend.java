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

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Folk;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.item.type.EtcItemType;
import org.l2jmobius.gameserver.model.itemcontainer.ItemContainer;
import org.l2jmobius.gameserver.model.itemcontainer.PlayerFreight;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.ItemList;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;

/**
 * @author -Wooden-
 */
public class RequestPackageSend implements IClientIncomingPacket
{
	private final List<ItemHolder> _items = new ArrayList<>();
	private int _objectID;
	private int _count;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_objectID = packet.readD();
		_count = packet.readD();
		if ((_count < 0) || (_count > 500))
		{
			_count = -1;
			return false;
		}
		
		for (int i = 0; i < _count; i++)
		{
			final int id = packet.readD(); // this is some id sent in PackageSendableList
			final int count = packet.readD();
			_items.add(new ItemHolder(id, count));
		}
		
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		if ((_count == -1) || (_items == null))
		{
			return;
		}
		
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (player.getObjectId() == _objectID)
		{
			return;
		}
		
		final Player target = Player.load(_objectID);
		if (player.getAccountChars().size() < 1)
		{
			return;
		}
		else if (!player.getAccountChars().containsKey(_objectID))
		{
			return;
		}
		
		if (World.getInstance().getPlayer(_objectID) != null)
		{
			return;
		}
		
		if (!client.getFloodProtectors().canPerformTransaction())
		{
			player.sendMessage("You depositing items too fast.");
			return;
		}
		
		final PlayerFreight freight = target.getFreight();
		player.setActiveWarehouse(freight);
		target.deleteMe();
		final ItemContainer warehouse = player.getActiveWarehouse();
		if (warehouse == null)
		{
			return;
		}
		
		final Folk manager = player.getLastFolkNPC();
		if (((manager == null) || !player.isInsideRadius2D(manager, Npc.INTERACTION_DISTANCE)) && !player.isGM())
		{
			return;
		}
		
		if ((warehouse instanceof PlayerFreight) && !player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Unsufficient privileges.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && (player.getKarma() > 0))
		{
			return;
		}
		
		// Freight price from config or normal price per item slot (30)
		final int fee = _count * Config.ALT_GAME_FREIGHT_PRICE;
		int currentAdena = player.getAdena();
		int slots = 0;
		for (ItemHolder i : _items)
		{
			final int objectId = i.id;
			final int count = i.count;
			
			// Check validity of requested item
			final Item item = player.checkItemManipulation(objectId, count, "deposit");
			
			// Check if item is null
			if (item == null)
			{
				LOGGER.warning("Error depositing a warehouse object for char " + player.getName() + " (validity check)");
				i.id = 0;
				i.count = 0;
				continue;
			}
			
			// Fix exploit for trade Augmented weapon with freight
			if (item.isAugmented())
			{
				LOGGER.warning("Error depositing a warehouse object for char " + player.getName() + " (item is augmented)");
				return;
			}
			
			if (!item.isTradeable() || (item.getItemType() == EtcItemType.QUEST))
			{
				return;
			}
			
			// Calculate needed adena and slots
			if (item.getItemId() == 57)
			{
				currentAdena -= count;
			}
			
			if (!item.isStackable())
			{
				slots += count;
			}
			else if (warehouse.getItemByItemId(item.getItemId()) == null)
			{
				slots++;
			}
		}
		
		// Item Max Limit Check
		if (!warehouse.validateCapacity(slots))
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}
		
		// Check if enough adena and charge the fee
		if ((currentAdena < fee) || !player.reduceAdena("Warehouse", fee, player.getLastFolkNPC(), false))
		{
			player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}
		
		// Proceed to the transfer
		final InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
		for (ItemHolder i : _items)
		{
			final int objectId = i.id;
			final int count = i.count;
			
			// check for an invalid item
			if ((objectId == 0) && (count == 0))
			{
				continue;
			}
			
			final Item oldItem = player.getInventory().getItemByObjectId(objectId);
			if (oldItem == null)
			{
				LOGGER.warning("Error depositing a warehouse object for char " + player.getName() + " (olditem == null)");
				continue;
			}
			
			final int itemId = oldItem.getItemId();
			if (((itemId >= 6611) && (itemId <= 6621)) || (itemId == 6842))
			{
				continue;
			}
			
			final Item newItem = player.getInventory().transferItem("Warehouse", objectId, count, warehouse, player, player.getLastFolkNPC());
			if (newItem == null)
			{
				LOGGER.warning("Error depositing a warehouse object for char " + player.getName() + " (newitem == null)");
				continue;
			}
			
			if (playerIU != null)
			{
				if ((oldItem.getCount() > 0) && (oldItem != newItem))
				{
					playerIU.addModifiedItem(oldItem);
				}
				else
				{
					playerIU.addRemovedItem(oldItem);
				}
			}
		}
		
		// Send updated item list to the player
		if (playerIU != null)
		{
			player.sendPacket(playerIU);
		}
		else
		{
			player.sendPacket(new ItemList(player, false));
		}
		
		// Update current load status on player
		final StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
		
		player.setActiveWarehouse(null);
	}
	
	private class ItemHolder
	{
		public int id;
		public int count;
		
		public ItemHolder(int i, int c)
		{
			id = i;
			count = c;
		}
	}
}
