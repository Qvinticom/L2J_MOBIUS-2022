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

import java.util.List;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.datatables.MapRegionTable;
import com.l2jmobius.gameserver.model.ItemContainer;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.PcFreight;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2WarehouseInstance;
import com.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jmobius.gameserver.network.serverpackets.ItemList;
import com.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.util.IllegalPlayerAction;
import com.l2jmobius.gameserver.util.Util;

import javolution.util.FastList;

/**
 * @author -Wooden-
 */
public class RequestPackageSend extends L2GameClientPacket
{
	private static final String _C_9F_REQUESTPACKAGESEND = "[C] 9F RequestPackageSend";
	private static Logger _log = Logger.getLogger(RequestPackageSend.class.getName());
	private final List<Item> _items = new FastList<>();
	private int _objectID;
	private int _count;
	
	@Override
	protected void readImpl()
	{
		_objectID = readD();
		_count = readD();
		
		if ((_count < 0) || (_count > 500))
		{
			_count = -1;
			return;
		}
		
		for (int i = 0; i < _count; i++)
		{
			final int id = readD(); // this is some id sent in PackageSendableList
			final int count = readD();
			_items.add(new Item(id, count));
		}
	}
	
	/**
	 * @see com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		if (_count == -1)
		{
			return;
		}
		
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		// why would sb do such a thing?
		if (player.getObjectId() == _objectID)
		{
			return;
		}
		
		if (!player.getAccountChars().containsKey(_objectID))
		{
			return;
		}
		
		final ItemContainer warehouse = player.getActiveWarehouse();
		if (warehouse == null)
		{
			return;
		}
		
		if (player.getActiveEnchantItem() != null)
		{
			Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to use enchant Exploit!", IllegalPlayerAction.PUNISH_KICKBAN);
			player.setActiveEnchantItem(null);
			return;
		}
		
		PcFreight freight = null;
		if (warehouse instanceof PcFreight)
		{
			freight = (PcFreight) warehouse;
		}
		
		if (freight == null)
		{
			return;
		}
		
		freight.doQuickRestore(_objectID);
		
		if (!(player.getLastFolkNPC() instanceof L2WarehouseInstance))
		{
			return;
		}
		
		final L2WarehouseInstance manager = ((L2WarehouseInstance) player.getLastFolkNPC());
		if (((manager == null) || !player.isInsideRadius(manager, L2NpcInstance.INTERACTION_DISTANCE, false, false)) && !player.isGM())
		{
			return;
		}
		
		if (Config.GM_DISABLE_TRANSACTION && (player.getAccessLevel() >= Config.GM_TRANSACTION_MIN) && (player.getAccessLevel() <= Config.GM_TRANSACTION_MAX))
		{
			player.sendMessage("Transactions are disabled for your Access Level.");
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
		
		for (final Item i : _items)
		{
			final int objectId = i.id;
			final int count = i.count;
			
			// Check validity of requested item
			final L2ItemInstance item = player.checkItemManipulation(objectId, count, "deposit");
			if (item == null)
			{
				_log.warning("Error depositing a warehouse object for char " + player.getName() + " (validity check)");
				i.id = 0;
				i.count = 0;
				continue;
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
			sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
			return;
		}
		
		// Check if enough adena and charge the fee
		if ((currentAdena < fee) || !player.reduceAdena("Warehouse", fee, player.getLastFolkNPC(), false))
		{
			sendPacket(new SystemMessage(SystemMessage.YOU_NOT_ENOUGH_ADENA));
			return;
		}
		
		if (!Config.ALT_GAME_FREIGHTS)
		{
			final int region = 1 + MapRegionTable.getInstance().getClosestTownNumber(player);
			
			freight.setActiveLocation(region);
		}
		
		// Proceed to the transfer
		final InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
		for (final Item i : _items)
		{
			final int objectId = i.id;
			final int count = i.count;
			
			// check for an invalid item
			if ((objectId == 0) && (count == 0))
			{
				continue;
			}
			
			final L2ItemInstance oldItem = player.getInventory().getItemByObjectId(objectId);
			if (oldItem == null)
			{
				_log.warning("Error depositing a warehouse object for char " + player.getName() + " (olditem == null)");
				continue;
			}
			
			if (!oldItem.isAvailable(player, true))
			{
				continue;
			}
			
			final L2ItemInstance newItem = player.getInventory().transferItem("Warehouse", objectId, count, warehouse, player, player.getLastFolkNPC());
			if (newItem == null)
			{
				_log.warning("Error depositing a warehouse object for char " + player.getName() + " (newitem == null)");
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
	}
	
	private class Item
	{
		public int id;
		public int count;
		
		public Item(int i, int c)
		{
			id = i;
			count = c;
		}
	}
	
	@Override
	public String getType()
	{
		return _C_9F_REQUESTPACKAGESEND;
	}
}