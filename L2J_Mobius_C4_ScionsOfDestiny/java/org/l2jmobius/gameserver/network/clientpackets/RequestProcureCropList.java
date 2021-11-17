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
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.data.xml.ManorSeedData;
import org.l2jmobius.gameserver.instancemanager.CastleManager;
import org.l2jmobius.gameserver.instancemanager.CastleManorManager;
import org.l2jmobius.gameserver.instancemanager.CastleManorManager.CropProcure;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.ManorManager;
import org.l2jmobius.gameserver.model.items.ItemTemplate;
import org.l2jmobius.gameserver.model.items.instance.Item;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.Util;

/**
 * Format: (ch) d [dddd] d: size [ d obj id d item id d manor id d count ]
 * @author l3x
 */
public class RequestProcureCropList implements IClientIncomingPacket
{
	private int _size;
	
	private int[] _items; // count*4
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_size = packet.readD();
		if (((_size * 16) > packet.getReadableBytes()) || (_size > 500) || (_size < 1))
		{
			_size = 0;
			return false;
		}
		_items = new int[_size * 4];
		for (int i = 0; i < _size; i++)
		{
			final int objId = packet.readD();
			_items[(i * 4) + 0] = objId;
			final int itemId = packet.readD();
			_items[(i * 4) + 1] = itemId;
			final int manorId = packet.readD();
			_items[(i * 4) + 2] = manorId;
			long count = packet.readD();
			if (count > Integer.MAX_VALUE)
			{
				count = Integer.MAX_VALUE;
			}
			_items[(i * 4) + 3] = (int) count;
		}
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		WorldObject target = player.getTarget();
		if (!(target instanceof ManorManager))
		{
			target = player.getLastFolkNPC();
		}
		
		if (!player.isGM() && ((target == null) || !(target instanceof ManorManager) || !player.isInsideRadius2D(target, Npc.INTERACTION_DISTANCE)))
		{
			return;
		}
		
		if (_size < 1)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final ManorManager manorManager = (ManorManager) target;
		final int currentManorId = manorManager.getCastle().getCastleId();
		
		// Calculate summary values
		int slots = 0;
		int weight = 0;
		for (int i = 0; i < _size; i++)
		{
			final int itemId = _items[(i * 4) + 1];
			final int manorId = _items[(i * 4) + 2];
			final int count = _items[(i * 4) + 3];
			if ((itemId == 0) || (manorId == 0) || (count == 0))
			{
				continue;
			}
			
			if (count < 1)
			{
				continue;
			}
			
			if (count > Integer.MAX_VALUE)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + Integer.MAX_VALUE + " items at the same time.", Config.DEFAULT_PUNISH);
				player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED));
				return;
			}
			
			try
			{
				final CropProcure crop = CastleManager.getInstance().getCastleById(manorId).getCrop(itemId, CastleManorManager.PERIOD_CURRENT);
				final int rewardItemId = ManorSeedData.getInstance().getRewardItem(itemId, crop.getReward());
				final ItemTemplate template = ItemTable.getInstance().getTemplate(rewardItemId);
				weight += count * template.getWeight();
				if (!template.isStackable())
				{
					slots += count;
				}
				else if (player.getInventory().getItemByItemId(itemId) == null)
				{
					slots++;
				}
			}
			catch (NullPointerException e)
			{
			}
		}
		
		if (!player.getInventory().validateWeight(weight))
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
			return;
		}
		
		if (!player.getInventory().validateCapacity(slots))
		{
			player.sendPacket(SystemMessageId.YOUR_INVENTORY_IS_FULL);
			return;
		}
		
		// Proceed the purchase
		final InventoryUpdate playerIU = new InventoryUpdate();
		for (int i = 0; i < _size; i++)
		{
			final int objId = _items[(i * 4) + 0];
			final int cropId = _items[(i * 4) + 1];
			final int manorId = _items[(i * 4) + 2];
			final int count = _items[(i * 4) + 3];
			if ((objId == 0) || (cropId == 0) || (manorId == 0) || (count == 0))
			{
				continue;
			}
			
			if (count < 1)
			{
				continue;
			}
			
			CropProcure crop = null;
			
			try
			{
				crop = CastleManager.getInstance().getCastleById(manorId).getCrop(cropId, CastleManorManager.PERIOD_CURRENT);
			}
			catch (NullPointerException e)
			{
				continue;
			}
			if ((crop == null) || (crop.getId() == 0) || (crop.getPrice() == 0))
			{
				continue;
			}
			
			int fee = 0; // fee for selling to other manors
			
			final int rewardItem = ManorSeedData.getInstance().getRewardItem(cropId, crop.getReward());
			if (count > crop.getAmount())
			{
				continue;
			}
			
			final int sellPrice = count * ManorSeedData.getInstance().getCropBasicPrice(cropId);
			final int rewardPrice = ItemTable.getInstance().getTemplate(rewardItem).getReferencePrice();
			if (rewardPrice == 0)
			{
				continue;
			}
			
			final int rewardItemCount = sellPrice / rewardPrice;
			if (rewardItemCount < 1)
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.FAILED_IN_TRADING_S2_OF_CROP_S1);
				sm.addItemName(cropId);
				sm.addNumber(count);
				player.sendPacket(sm);
				continue;
			}
			
			if (manorId != currentManorId)
			{
				fee = (sellPrice * 5) / 100; // 5% fee for selling to other manor
			}
			
			if (player.getInventory().getAdena() < fee)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.FAILED_IN_TRADING_S2_OF_CROP_S1);
				sm.addItemName(cropId);
				sm.addNumber(count);
				player.sendPacket(sm);
				sm = new SystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				player.sendPacket(sm);
				continue;
			}
			
			// Add item to Inventory and adjust update packet
			Item itemDel = null;
			Item itemAdd = null;
			if (player.getInventory().getItemByObjectId(objId) != null)
			{
				// check if player have correct items count
				final Item item = player.getInventory().getItemByObjectId(objId);
				if (item.getCount() < count)
				{
					continue;
				}
				
				itemDel = player.getInventory().destroyItem("Manor", objId, count, player, manorManager);
				if (itemDel == null)
				{
					continue;
				}
				
				if (fee > 0)
				{
					player.getInventory().reduceAdena("Manor", fee, player, manorManager);
				}
				
				crop.setAmount(crop.getAmount() - count);
				if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
				{
					CastleManager.getInstance().getCastleById(manorId).updateCrop(crop.getId(), crop.getAmount(), CastleManorManager.PERIOD_CURRENT);
				}
				
				itemAdd = player.getInventory().addItem("Manor", rewardItem, rewardItemCount, player, manorManager);
			}
			else
			{
				continue;
			}
			
			if (itemAdd == null)
			{
				continue;
			}
			
			playerIU.addRemovedItem(itemDel);
			if (itemAdd.getCount() > rewardItemCount)
			{
				playerIU.addModifiedItem(itemAdd);
			}
			else
			{
				playerIU.addNewItem(itemAdd);
			}
			
			// Send System Messages
			SystemMessage sm = new SystemMessage(SystemMessageId.TRADED_S2_OF_CROP_S1);
			sm.addItemName(cropId);
			sm.addNumber(count);
			player.sendPacket(sm);
			
			if (fee > 0)
			{
				sm = new SystemMessage(SystemMessageId.S1_ADENA_HAS_BEEN_WITHDRAWN_TO_PAY_FOR_PURCHASING_FEES);
				sm.addNumber(fee);
				player.sendPacket(sm);
			}
			
			sm = new SystemMessage(SystemMessageId.S2_S1_HAS_DISAPPEARED);
			sm.addItemName(cropId);
			sm.addNumber(count);
			player.sendPacket(sm);
			
			if (fee > 0)
			{
				sm = new SystemMessage(SystemMessageId.S1_ADENA_DISAPPEARED);
				sm.addNumber(fee);
				player.sendPacket(sm);
			}
			
			sm = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S2_S1_S);
			sm.addItemName(rewardItem);
			sm.addNumber(rewardItemCount);
			player.sendPacket(sm);
		}
		
		// Send update packets
		player.sendPacket(playerIU);
		
		final StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
	}
}
