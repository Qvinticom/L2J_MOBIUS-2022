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
package com.l2jmobius.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.logging.Logger;

import com.l2jmobius.L2DatabaseFactory;
import com.l2jmobius.gameserver.datatables.ItemTable;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

import javolution.util.FastList;

/**
 * This class ...
 * @version $Revision: 1.4.2.1.2.5 $ $Date: 2005/03/27 15:29:33 $
 */
public class L2TradeList
{
	private static Logger _log = Logger.getLogger(L2TradeList.class.getName());
	
	private final List<L2ItemInstance> _items;
	private final int _listId;
	private boolean _confirmed;
	private String _Buystorename, _Sellstorename;
	
	private String _npcId;
	
	public L2TradeList(int listId)
	{
		_items = new FastList<>();
		_listId = listId;
		_confirmed = false;
	}
	
	public void setNpcId(String id)
	{
		_npcId = id;
	}
	
	public String getNpcId()
	{
		return _npcId;
	}
	
	public void addItem(L2ItemInstance item)
	{
		_items.add(item);
	}
	
	public void replaceItem(int itemID, int price)
	{
		for (int i = 0; i < _items.size(); i++)
		{
			final L2ItemInstance item = _items.get(i);
			if (item.getItemId() == itemID)
			{
				item.setPriceToSell(price);
			}
		}
	}
	
	public boolean decreaseCount(int itemID, int count)
	{
		for (int i = 0; i < _items.size(); i++)
		{
			final L2ItemInstance item = _items.get(i);
			if (item.getItemId() == itemID)
			{
				if (item.getCount() >= count)
				{
					item.setCount(item.getCount() - count);
					return true;
				}
			}
		}
		return false;
	}
	
	public void removeItem(int itemID)
	{
		for (int i = 0; i < _items.size(); i++)
		{
			final L2ItemInstance item = _items.get(i);
			if (item.getItemId() == itemID)
			{
				_items.remove(i);
			}
		}
	}
	
	/**
	 * @return Returns the listId.
	 */
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
	
	/**
	 * @return Returns the items.
	 */
	public List<L2ItemInstance> getItems()
	{
		return _items;
	}
	
	public List<L2ItemInstance> getItems(int start, int end)
	{
		return _items.subList(start, end);
	}
	
	public int getPriceForItemId(int itemId)
	{
		for (int i = 0; i < _items.size(); i++)
		{
			final L2ItemInstance item = _items.get(i);
			if (item.getItemId() == itemId)
			{
				return item.getPriceToSell();
			}
		}
		return -1;
	}
	
	public boolean countDecrease(int itemId)
	{
		for (int i = 0; i < _items.size(); i++)
		{
			final L2ItemInstance item = _items.get(i);
			if (item.getItemId() == itemId)
			{
				return item.getCountDecrease();
			}
		}
		return false;
	}
	
	public boolean containsItemId(int itemId)
	{
		for (final L2ItemInstance item : _items)
		{
			if (item.getItemId() == itemId)
			{
				return true;
			}
		}
		return false;
	}
	
	public L2ItemInstance getItem(int ObjectId)
	{
		for (int i = 0; i < _items.size(); i++)
		{
			final L2ItemInstance item = _items.get(i);
			if (item.getObjectId() == ObjectId)
			{
				return item;
			}
		}
		return null;
	}
	
	public synchronized void setConfirmedTrade(boolean x)
	{
		_confirmed = x;
	}
	
	public synchronized boolean hasConfirmed()
	{
		return _confirmed;
	}
	
	public void removeItem(int objId, int count)
	{
		L2ItemInstance temp;
		for (int y = 0; y < _items.size(); y++)
		{
			temp = _items.get(y);
			if (temp.getObjectId() == objId)
			{
				if (count == temp.getCount())
				{
					_items.remove(temp);
				}
				
				break;
			}
		}
		
	}
	
	public boolean contains(int objId)
	{
		boolean bool = false;
		L2ItemInstance temp;
		for (int y = 0; y < _items.size(); y++)
		{
			temp = _items.get(y);
			if (temp.getObjectId() == objId)
			{
				bool = true;
				break;
			}
		}
		
		return bool;
	}
	
	public boolean validateTrade(L2PcInstance player)
	{
		final Inventory playersInv = player.getInventory();
		L2ItemInstance playerItem, temp;
		
		for (int y = 0; y < _items.size(); y++)
		{
			temp = _items.get(y);
			playerItem = playersInv.getItemByObjectId(temp.getObjectId());
			if ((playerItem == null) || (playerItem.getCount() < temp.getCount()))
			{
				return false;
			}
		}
		return true;
	}
	
	// Call validate before this
	public void tradeItems(L2PcInstance player, L2PcInstance reciever)
	{
		final Inventory playersInv = player.getInventory();
		final Inventory recieverInv = reciever.getInventory();
		L2ItemInstance playerItem, recieverItem, temp, newitem;
		InventoryUpdate update = new InventoryUpdate();
		final ItemTable itemTable = ItemTable.getInstance();
		
		for (int y = 0; y < _items.size(); y++)
		{
			temp = _items.get(y);
			playerItem = playersInv.getItemByObjectId(temp.getObjectId());
			// FIXME: why is this null??
			if (playerItem == null)
			{
				continue;
			}
			newitem = itemTable.createItem("L2TradeList", playerItem.getItemId(), playerItem.getCount(), player);
			newitem.setEnchantLevel(temp.getEnchantLevel());
			
			// DIRTY FIX: Fix for trading pet collar not updating pet with new collar object id
			changePetItemObjectId(playerItem.getObjectId(), newitem.getObjectId());
			
			// Remove item from sender and add item to reciever
			if (reciever.isGM() || player.isGM())
			{
				L2PcInstance gm;
				L2PcInstance target;
				if (reciever.isGM())
				{
					gm = reciever;
					target = player;
				}
				else
				{
					gm = player;
					target = reciever;
				}
				GMAudit.auditGMAction(gm.getName(), "trade", target.getName(), newitem.getItem().getName() + " - " + newitem.getItemId());
			}
			playerItem = playersInv.destroyItem("!L2TradeList!", playerItem.getObjectId(), temp.getCount(), null, null);
			recieverItem = recieverInv.addItem("!L2TradeList!", newitem, null, null);
			
			if (playerItem == null)
			{
				_log.warning("L2TradeList: PlayersInv.destroyItem returned NULL!");
				continue;
			}
			
			if (playerItem.getLastChange() == L2ItemInstance.MODIFIED)
			{
				update.addModifiedItem(playerItem);
			}
			else
			{
				final L2World world = L2World.getInstance();
				world.removeObject(playerItem);
				update.addRemovedItem(playerItem);
				
			}
			
			player.sendPacket(update);
			
			update = new InventoryUpdate();
			if (recieverItem.getLastChange() == L2ItemInstance.MODIFIED)
			{
				update.addModifiedItem(recieverItem);
			}
			else
			{
				update.addNewItem(recieverItem);
			}
			
			reciever.sendPacket(update);
		}
		
		// weight status update both player and reciever
		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
		
		su = new StatusUpdate(reciever.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, reciever.getCurrentLoad());
		reciever.sendPacket(su);
	}
	
	private void changePetItemObjectId(int oldObjectId, int newObjectId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE pets SET item_obj_id = ? WHERE item_obj_id = ?"))
		{
			statement.setInt(1, newObjectId);
			statement.setInt(2, oldObjectId);
			statement.executeUpdate();
		}
		catch (final Exception e)
		{
			_log.warning("could not change pet item object id: " + e);
		}
	}
	
	public void updateBuyList(L2PcInstance player, List<TradeItem> list)
	{
		
		TradeItem temp;
		int count;
		final Inventory playersInv = player.getInventory();
		L2ItemInstance temp2;
		count = 0;
		
		while (count != list.size())
		{
			temp = list.get(count);
			temp2 = playersInv.getItemByItemId(temp.getItemId());
			if (temp2 == null)
			{
				list.remove(count);
				count = count - 1;
			}
			else
			{
				if (temp.getCount() == 0)
				{
					list.remove(count);
					count = count - 1;
				}
			}
			count++;
		}
		
	}
	
	public void updateSellList(L2PcInstance player, List<TradeItem> list)
	{
		final Inventory playersInv = player.getInventory();
		TradeItem temp;
		L2ItemInstance temp2;
		int count = 0;
		while (count != list.size())
		{
			temp = list.get(count);
			temp2 = playersInv.getItemByObjectId(temp.getObjectId());
			if (temp2 == null)
			{
				list.remove(count);
				count = count - 1;
			}
			else
			{
				if (temp2.getCount() < temp.getCount())
				{
					temp.setCount(temp2.getCount());
				}
				
			}
			count++;
		}
		
	}
	
	public synchronized void buySellItems(L2PcInstance buyer, List<TradeItem> buyerslist, L2PcInstance seller, List<TradeItem> sellerslist)
	{
		final Inventory sellerInv = seller.getInventory();
		final Inventory buyerInv = buyer.getInventory();
		
		// TradeItem buyerItem = null;
		TradeItem temp2 = null;
		
		L2ItemInstance sellerItem = null;
		L2ItemInstance temp = null;
		L2ItemInstance newitem = null;
		L2ItemInstance adena = null;
		int enchantLevel = 0;
		
		final InventoryUpdate buyerupdate = new InventoryUpdate();
		final InventoryUpdate sellerupdate = new InventoryUpdate();
		
		final ItemTable itemTable = ItemTable.getInstance();
		
		int amount = 0;
		int x = 0;
		int y = 0;
		
		final List<SystemMessage> sysmsgs = new FastList<>();
		SystemMessage msg = null;
		
		for (final TradeItem buyerItem : buyerslist)
		{
			for (x = 0; x < sellerslist.size(); x++)// find in sellerslist
			{
				temp2 = sellerslist.get(x);
				if (temp2.getItemId() == buyerItem.getItemId())
				{
					sellerItem = sellerInv.getItemByItemId(buyerItem.getItemId());
					break;
				}
			}
			
			if (sellerItem != null)
			{
				if ((temp2 != null) && (buyerItem.getCount() > temp2.getCount()))
				{
					amount = temp2.getCount();
				}
				if (buyerItem.getCount() > sellerItem.getCount())
				{
					amount = sellerItem.getCount();
				}
				else
				{
					amount = buyerItem.getCount();
				}
				if (buyerItem.getCount() > (Integer.MAX_VALUE / buyerItem.getOwnersPrice()))
				{
					_log.warning("Integer Overflow on Cost. Possible Exploit attempt between " + buyer.getName() + " and " + seller.getName() + ".");
					return;
				}
				// int cost = amount * buyerItem.getOwnersPrice();
				enchantLevel = sellerItem.getEnchantLevel();
				sellerItem = sellerInv.destroyItem("", sellerItem.getObjectId(), amount, null, null);
				// buyer.reduceAdena(cost);
				// seller.addAdena(cost);
				newitem = itemTable.createItem("L2TradeList", sellerItem.getItemId(), amount, buyer, seller);
				newitem.setEnchantLevel(enchantLevel);
				temp = buyerInv.addItem("", newitem, null, null);
				if (amount == 1)// system msg stuff
				{
					msg = new SystemMessage(SystemMessage.S1_PURCHASED_S2);
					msg.addString(buyer.getName());
					msg.addItemName(sellerItem.getItemId());
					sysmsgs.add(msg);
					msg = new SystemMessage(SystemMessage.S1_PURCHASED_S2);
					msg.addString("You");
					msg.addItemName(sellerItem.getItemId());
					sysmsgs.add(msg);
				}
				else
				{
					msg = new SystemMessage(SystemMessage.S1_PURCHASED_S3_S2_s);
					msg.addString(buyer.getName());
					msg.addItemName(sellerItem.getItemId());
					msg.addNumber(amount);
					sysmsgs.add(msg);
					msg = new SystemMessage(SystemMessage.S1_PURCHASED_S3_S2_s);
					msg.addString("You");
					msg.addItemName(sellerItem.getItemId());
					msg.addNumber(amount);
					sysmsgs.add(msg);
				}
				if ((temp2 != null) && (temp2.getCount() == buyerItem.getCount()))
				{
					sellerslist.remove(temp2);
					buyerItem.setCount(0);
				}
				else if (temp2 != null)
				{
					if (buyerItem.getCount() < temp2.getCount())
					{
						temp2.setCount(temp2.getCount() - buyerItem.getCount());
					}
					else
					{
						buyerItem.setCount(buyerItem.getCount() - temp2.getCount());
					}
				}
				
				if (sellerItem.getLastChange() == L2ItemInstance.MODIFIED)
				{
					sellerupdate.addModifiedItem(sellerItem);
					
				}
				else
				{
					final L2World world = L2World.getInstance();
					world.removeObject(sellerItem);
					sellerupdate.addRemovedItem(sellerItem);
					
				}
				
				if (temp.getLastChange() == L2ItemInstance.MODIFIED)
				{
					buyerupdate.addModifiedItem(temp);
				}
				else
				{
					buyerupdate.addNewItem(temp);
				}
				
				// }
				
				sellerItem = null;
			}
		}
		if (newitem != null)
		{
			// updateSellList(seller,sellerslist);
			adena = seller.getInventory().getAdenaInstance();
			adena.setLastChange(L2ItemInstance.MODIFIED);
			sellerupdate.addModifiedItem(adena);
			adena = buyer.getInventory().getAdenaInstance();
			adena.setLastChange(L2ItemInstance.MODIFIED);
			buyerupdate.addModifiedItem(adena);
			
			seller.sendPacket(sellerupdate);
			buyer.sendPacket(buyerupdate);
			y = 0;
			
			for (x = 0; x < sysmsgs.size(); x++)
			{
				
				if (y == 0)
				{
					seller.sendPacket(sysmsgs.get(x));
					y = 1;
				}
				else
				{
					buyer.sendPacket(sysmsgs.get(x));
					y = 0;
				}
			}
		}
	}
}