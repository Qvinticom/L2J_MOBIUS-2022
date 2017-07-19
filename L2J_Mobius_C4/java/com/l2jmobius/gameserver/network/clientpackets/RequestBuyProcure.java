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

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.datatables.ItemTable;
import com.l2jmobius.gameserver.instancemanager.CastleManorManager;
import com.l2jmobius.gameserver.instancemanager.CastleManorManager.CropProcure;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.L2Manor;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.actor.instance.L2ManorManagerInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.templates.L2Item;
import com.l2jmobius.gameserver.util.Util;

import javolution.util.FastList;

public class RequestBuyProcure extends L2GameClientPacket
{
	private static final String _C__C3_REQUESTBUYPROCURE = "[C] C3 RequestBuyProcure";
	@SuppressWarnings("unused")
	private int _listId;
	private int _count;
	private int[] _items;
	@SuppressWarnings("unused")
	private List<CropProcure> _procureList = new FastList<>();
	
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
		
		_items = new int[_count * 2];
		for (int i = 0; i < _count; i++)
		{
			readD();
			final int itemId = readD();
			_items[(i * 2) + 0] = itemId;
			final long cnt = readD();
			if ((cnt > Integer.MAX_VALUE) || (cnt < 1))
			{
				_count = 0;
				_items = null;
				return;
			}
			_items[(i * 2) + 1] = (int) cnt;
		}
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
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
		
		final L2Object target = player.getTarget();
		
		if (_count < 1)
		{
			sendPacket(new ActionFailed());
			return;
		}
		
		// Check for buylist validity and calculates summary values
		int slots = 0;
		int weight = 0;
		final L2ManorManagerInstance manor = ((target != null) && (target instanceof L2ManorManagerInstance)) ? (L2ManorManagerInstance) target : null;
		if (manor == null)
		{
			return;
		}
		for (int i = 0; i < _count; i++)
		{
			final int itemId = _items[(i * 2) + 0];
			final int count = _items[(i * 2) + 1];
			
			if (count > Integer.MAX_VALUE)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + Integer.MAX_VALUE + " items at the same time.", Config.DEFAULT_PUNISH);
				final SystemMessage sm = new SystemMessage(SystemMessage.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				sendPacket(sm);
				return;
			}
			
			final L2Item template = ItemTable.getInstance().getTemplate(L2Manor.getInstance().getRewardItem(itemId, manor.getCastle().getCrop(itemId, CastleManorManager.PERIOD_CURRENT).getReward()));
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
			sendPacket(new SystemMessage(SystemMessage.WEIGHT_LIMIT_EXCEEDED));
			return;
		}
		
		if (!player.getInventory().validateCapacity(slots))
		{
			sendPacket(new SystemMessage(SystemMessage.SLOTS_FULL));
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
			
			final int rewardItemId = L2Manor.getInstance().getRewardItem(itemId, manor.getCastle().getCrop(itemId, CastleManorManager.PERIOD_CURRENT).getReward());
			
			int rewardItemCount = 1;
			
			rewardItemCount = count / rewardItemCount;
			
			// Add item to Inventory and adjust update packet
			final L2ItemInstance item = player.getInventory().addItem("Manor", rewardItemId, rewardItemCount, player, manor);
			final L2ItemInstance iteme = player.getInventory().destroyItemByItemId("Manor", itemId, count, player, manor);
			
			if ((item == null) || (iteme == null))
			{
				continue;
			}
			
			playerIU.addRemovedItem(iteme);
			if (item.getCount() > rewardItemCount)
			{
				playerIU.addModifiedItem(item);
			}
			else
			{
				playerIU.addNewItem(item);
			}
			
			// Send Char Buy Messages
			SystemMessage sm = new SystemMessage(SystemMessage.EARNED_S2_S1_s);
			sm.addItemName(rewardItemId);
			sm.addNumber(rewardItemCount);
			player.sendPacket(sm);
			sm = null;
		}
		
		// Send update packets
		player.sendPacket(playerIU);
		
		final StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
	}
	
	@Override
	public String getType()
	{
		return _C__C3_REQUESTBUYPROCURE;
	}
}