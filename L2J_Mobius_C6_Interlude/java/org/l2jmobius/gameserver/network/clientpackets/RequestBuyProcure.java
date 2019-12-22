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
import org.l2jmobius.gameserver.datatables.xml.ItemTable;
import org.l2jmobius.gameserver.instancemanager.CastleManorManager;
import org.l2jmobius.gameserver.instancemanager.CastleManorManager.CropProcure;
import org.l2jmobius.gameserver.model.Manor;
import org.l2jmobius.gameserver.model.actor.instance.ManorManagerInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.items.Item;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.Util;

@SuppressWarnings("unused")
public class RequestBuyProcure extends GameClientPacket
{
	private int _listId;
	private int _count;
	private int[] _items;
	private List<CropProcure> _procureList = new ArrayList<>();
	
	@Override
	protected void readImpl()
	{
		_listId = readD();
		_count = readD();
		
		if (_count > 500) // protect server
		{
			_count = 0;
			return;
		}
		
		if (_count < 0) // protect server
		{
			_count = 0;
			return;
		}
		
		_items = new int[_count * 2];
		for (int i = 0; i < _count; i++)
		{
			final long servise = readD();
			final int itemId = readD();
			_items[(i * 2) + 0] = itemId;
			final long cnt = readD();
			
			if ((cnt > Integer.MAX_VALUE) || (cnt < 1))
			{
				_count = 0;
				return;
			}
			
			_items[(i * 2) + 1] = (int) cnt;
		}
	}
	
	@Override
	protected void runImpl()
	{
		final PlayerInstance player = getClient().getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getManor().tryPerformAction("BuyProcure"))
		{
			return;
		}
		
		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && (player.getKarma() > 0))
		{
			return;
		}
		
		if (_count < 1)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final long subTotal = 0;
		final int tax = 0;
		
		// Check for buylist validity and calculates summary values
		int slots = 0;
		int weight = 0;
		
		if (!(player.getTarget() instanceof ManorManagerInstance))
		{
			return;
		}
		
		final ManorManagerInstance manor = (ManorManagerInstance) player.getTarget();
		for (int i = 0; i < _count; i++)
		{
			final int itemId = _items[(i * 2) + 0];
			final int count = _items[(i * 2) + 1];
			final int price = 0;
			
			if (count > Integer.MAX_VALUE)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + Integer.MAX_VALUE + " items at the same time.", Config.DEFAULT_PUNISH);
				final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				sendPacket(sm);
				return;
			}
			
			final Item template = ItemTable.getInstance().getTemplate(Manor.getInstance().getRewardItem(itemId, manor.getCastle().getCrop(itemId, CastleManorManager.PERIOD_CURRENT).getReward()));
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
		
		if (!player.getInventory().validateWeight(weight))
		{
			player.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
			return;
		}
		
		if (!player.getInventory().validateCapacity(slots))
		{
			player.sendPacket(SystemMessageId.SLOTS_FULL);
			return;
		}
		
		// Proceed the purchase
		final InventoryUpdate playerIU = new InventoryUpdate();
		_procureList = manor.getCastle().getCropProcure(CastleManorManager.PERIOD_CURRENT);
		
		for (int i = 0; i < _count; i++)
		{
			final int itemId = _items[(i * 2) + 0];
			int count = _items[(i * 2) + 1];
			
			if (count < 0)
			{
				count = 0;
			}
			
			final int rewradItemId = Manor.getInstance().getRewardItem(itemId, manor.getCastle().getCrop(itemId, CastleManorManager.PERIOD_CURRENT).getReward());
			
			int rewradItemCount = 1; // Manor.getInstance().getRewardAmount(itemId, manor.getCastle().getCropReward(itemId));
			
			rewradItemCount = count / rewradItemCount;
			
			// Add item to Inventory and adjust update packet
			final ItemInstance item = player.getInventory().addItem("Manor", rewradItemId, rewradItemCount, player, manor);
			final ItemInstance iteme = player.getInventory().destroyItemByItemId("Manor", itemId, count, player, manor);
			
			if ((item == null) || (iteme == null))
			{
				continue;
			}
			
			playerIU.addRemovedItem(iteme);
			if (item.getCount() > rewradItemCount)
			{
				playerIU.addModifiedItem(item);
			}
			else
			{
				playerIU.addNewItem(item);
			}
			
			// Send Char Buy Messages
			final SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
			sm.addItemName(rewradItemId);
			sm.addNumber(rewradItemCount);
			player.sendPacket(sm);
			
			// manor.getCastle().setCropAmount(itemId, manor.getCastle().getCrop(itemId, CastleManorManager.PERIOD_CURRENT).getAmount() - count);
		}
		
		// Send update packets
		player.sendPacket(playerIU);
		
		final StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
	}
}
