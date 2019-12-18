/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.gameserver.model;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class TradeList
{
	private final List<ItemInstance> _items = new ArrayList<>();
	private final int _listId;
	private boolean _confirmed;
	private String _Buystorename;
	private String _Sellstorename;
	
	public TradeList(int listId)
	{
		_listId = listId;
		_confirmed = false;
	}
	
	public void addItem(ItemInstance item)
	{
		_items.add(item);
	}
	
	public int getListId()
	{
		return _listId;
	}
	
	public void setSellStoreName(String name)
	{
		_Sellstorename = name;
	}
	
	public String getSellStoreName()
	{
		return _Sellstorename;
	}
	
	public void setBuyStoreName(String name)
	{
		_Buystorename = name;
	}
	
	public String getBuyStoreName()
	{
		return _Buystorename;
	}
	
	public List<ItemInstance> getItems()
	{
		return _items;
	}
	
	public int getPriceForItemId(int itemId)
	{
		for (int i = 0; i < _items.size(); ++i)
		{
			final ItemInstance item = _items.get(i);
			if (item.getItemId() != itemId)
			{
				continue;
			}
			return item.getPrice();
		}
		return -1;
	}
	
	public ItemInstance getItem(int objectId)
	{
		for (int i = 0; i < _items.size(); ++i)
		{
			final ItemInstance item = _items.get(i);
			if (item.getObjectId() != objectId)
			{
				continue;
			}
			return item;
		}
		return null;
	}
	
	public void setConfirmedTrade(boolean x)
	{
		_confirmed = x;
	}
	
	public boolean hasConfirmed()
	{
		return _confirmed;
	}
	
	public void removeItem(int objId, int count)
	{
		for (int y = 0; y < _items.size(); ++y)
		{
			final ItemInstance temp = _items.get(y);
			if (temp.getObjectId() != objId)
			{
				continue;
			}
			if (count != temp.getCount())
			{
				break;
			}
			_items.remove(temp);
			break;
		}
	}
	
	public boolean contains(int objId)
	{
		boolean bool = false;
		for (int y = 0; y < _items.size(); ++y)
		{
			final ItemInstance temp = _items.get(y);
			if (temp.getObjectId() != objId)
			{
				continue;
			}
			bool = true;
			break;
		}
		return bool;
	}
	
	public void tradeItems(PlayerInstance player, PlayerInstance reciever)
	{
		final Inventory playersInv = player.getInventory();
		final Inventory recieverInv = reciever.getInventory();
		InventoryUpdate update = new InventoryUpdate();
		final ItemTable itemTable = ItemTable.getInstance();
		for (int y = 0; y < _items.size(); ++y)
		{
			final ItemInstance temp = _items.get(y);
			ItemInstance playerItem = playersInv.getItem(temp.getObjectId());
			final ItemInstance newitem = itemTable.createItem(playerItem.getItemId());
			newitem.setCount(temp.getCount());
			playerItem = playersInv.destroyItem(playerItem.getObjectId(), temp.getCount());
			final ItemInstance recieverItem = recieverInv.addItem(newitem);
			if (playerItem.getLastChange() == 2)
			{
				update.addModifiedItem(playerItem);
			}
			else
			{
				final World world = World.getInstance();
				world.removeObject(playerItem);
				update.addRemovedItem(playerItem);
			}
			player.sendPacket(update);
			update = new InventoryUpdate();
			if (recieverItem.getLastChange() == 2)
			{
				update.addModifiedItem(recieverItem);
			}
			else
			{
				update.addNewItem(recieverItem);
			}
			reciever.sendPacket(update);
		}
	}
	
	public void updateBuyList(PlayerInstance player, List<TradeItem> list)
	{
		final Inventory playersInv = player.getInventory();
		for (int count = 0; count != list.size(); ++count)
		{
			final TradeItem temp = list.get(count);
			final ItemInstance temp2 = playersInv.findItemByItemId(temp.getItemId());
			if (temp2 == null)
			{
				list.remove(count);
				--count;
				continue;
			}
			if (temp.getCount() != 0)
			{
				continue;
			}
			list.remove(count);
			--count;
		}
	}
	
	public void updateSellList(PlayerInstance player, List<TradeItem> list)
	{
		final Inventory playersInv = player.getInventory();
		for (int count = 0; count != list.size(); ++count)
		{
			final TradeItem temp = list.get(count);
			final ItemInstance temp2 = playersInv.getItem(temp.getObjectId());
			if (temp2 == null)
			{
				list.remove(count);
				--count;
				continue;
			}
			if (temp2.getCount() >= temp.getCount())
			{
				continue;
			}
			temp.setCount(temp2.getCount());
		}
	}
	
	public synchronized void BuySellItems(PlayerInstance buyer, List<TradeItem> buyerlist, PlayerInstance seller, List<TradeItem> sellerlist)
	{
		int x;
		int y;
		final Inventory sellerInv = seller.getInventory();
		final Inventory buyerInv = buyer.getInventory();
		TradeItem temp2 = null;
		WorldObject sellerItem = null;
		ItemInstance newitem = null;
		final InventoryUpdate buyerupdate = new InventoryUpdate();
		final InventoryUpdate sellerupdate = new InventoryUpdate();
		final ItemTable itemTable = ItemTable.getInstance();
		int cost = 0;
		final List<SystemMessage> sysmsgs = new ArrayList<>();
		for (y = 0; y < buyerlist.size(); ++y)
		{
			SystemMessage msg;
			final TradeItem buyerItem = buyerlist.get(y);
			for (x = 0; x < sellerlist.size(); ++x)
			{
				temp2 = sellerlist.get(x);
				if (temp2.getItemId() != buyerItem.getItemId())
				{
					continue;
				}
				sellerItem = sellerInv.findItemByItemId(buyerItem.getItemId());
				break;
			}
			if ((sellerItem == null) || (temp2 == null))
			{
				continue;
			}
			final int amount = buyerItem.getCount() > temp2.getCount() ? temp2.getCount() : buyerItem.getCount();
			sellerItem = sellerInv.destroyItem(sellerItem.getObjectId(), amount);
			cost = buyerItem.getCount() * buyerItem.getOwnersPrice();
			seller.addAdena(cost);
			newitem = itemTable.createItem(((ItemInstance) sellerItem).getItemId());
			newitem.setCount(amount);
			final ItemInstance temp = buyerInv.addItem(newitem);
			if (amount == 1)
			{
				msg = new SystemMessage(SystemMessage.S1_PURCHASED_S2);
				msg.addString(buyer.getName());
				msg.addItemName(((ItemInstance) sellerItem).getItemId());
				sysmsgs.add(msg);
				msg = new SystemMessage(SystemMessage.S1_PURCHASED_S2);
				msg.addString("You");
				msg.addItemName(((ItemInstance) sellerItem).getItemId());
				sysmsgs.add(msg);
			}
			else
			{
				msg = new SystemMessage(SystemMessage.S1_PURCHASED_S3_S2_S);
				msg.addString(buyer.getName());
				msg.addItemName(((ItemInstance) sellerItem).getItemId());
				msg.addNumber(amount);
				sysmsgs.add(msg);
				msg = new SystemMessage(SystemMessage.S1_PURCHASED_S3_S2_S);
				msg.addString("You");
				msg.addItemName(((ItemInstance) sellerItem).getItemId());
				msg.addNumber(amount);
				sysmsgs.add(msg);
			}
			buyer.reduceAdena(cost);
			if (temp2.getCount() == buyerItem.getCount())
			{
				sellerlist.remove(x);
				buyerItem.setCount(0);
			}
			else if (buyerItem.getCount() < temp2.getCount())
			{
				temp2.setCount(temp2.getCount() - buyerItem.getCount());
			}
			else
			{
				buyerItem.setCount(buyerItem.getCount() - temp2.getCount());
			}
			if (((ItemInstance) sellerItem).getLastChange() == 2)
			{
				sellerupdate.addModifiedItem((ItemInstance) sellerItem);
			}
			else
			{
				final World world = World.getInstance();
				world.removeObject(sellerItem);
				sellerupdate.addRemovedItem((ItemInstance) sellerItem);
			}
			if (temp.getLastChange() == 2)
			{
				buyerupdate.addModifiedItem(temp);
			}
			else
			{
				buyerupdate.addNewItem(temp);
			}
			sellerItem = null;
		}
		if (newitem != null)
		{
			ItemInstance adena = seller.getInventory().getAdenaInstance();
			adena.setLastChange(2);
			sellerupdate.addModifiedItem(adena);
			adena = buyer.getInventory().getAdenaInstance();
			adena.setLastChange(2);
			buyerupdate.addModifiedItem(adena);
			seller.sendPacket(sellerupdate);
			buyer.sendPacket(buyerupdate);
			y = 0;
			for (x = 0; x < sysmsgs.size(); ++x)
			{
				if (y == 0)
				{
					seller.sendPacket(sysmsgs.get(x));
					y = 1;
					continue;
				}
				buyer.sendPacket(sysmsgs.get(x));
				y = 0;
			}
		}
	}
}
