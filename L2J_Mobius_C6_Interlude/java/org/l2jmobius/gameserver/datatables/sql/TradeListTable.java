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
package org.l2jmobius.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.l2jmobius.commons.concurrent.ThreadPool;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.datatables.xml.ItemTable;
import org.l2jmobius.gameserver.model.StoreTradeList;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;

/**
 * This class manages buylists from database
 * @version $Revision: 1.5.4.13 $ $Date: 2005/04/06 16:13:38 $
 */
public class TradeListTable
{
	private static final Logger LOGGER = Logger.getLogger(TradeListTable.class.getName());
	
	private int _nextListId;
	private final Map<Integer, StoreTradeList> _lists = new HashMap<>();
	
	/** Task launching the function for restore count of Item (Clan Hall) */
	private class RestoreCount implements Runnable
	{
		private final int _timer;
		
		public RestoreCount(int time)
		{
			_timer = time;
		}
		
		@Override
		public void run()
		{
			restoreCount(_timer);
			dataTimerSave(_timer);
			ThreadPool.schedule(new RestoreCount(_timer), _timer * 60 * 60 * 1000);
		}
	}
	
	private TradeListTable()
	{
		load();
	}
	
	private void load(boolean custom)
	{
		_lists.clear();
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement1 = con.prepareStatement("SELECT shop_id,npc_id FROM " + (custom ? "custom_merchant_shopids" : "merchant_shopids"));
			final ResultSet rset1 = statement1.executeQuery();
			
			while (rset1.next())
			{
				final PreparedStatement statement = con.prepareStatement("SELECT item_id, price, shop_id, order, count, time, currentCount FROM " + (custom ? "custom_merchant_buylists" : "merchant_buylists") + " WHERE shop_id=? ORDER BY order ASC");
				statement.setString(1, String.valueOf(rset1.getInt("shop_id")));
				final ResultSet rset = statement.executeQuery();
				
				final StoreTradeList buylist = new StoreTradeList(rset1.getInt("shop_id"));
				
				buylist.setNpcId(rset1.getString("npc_id"));
				int _itemId = 0;
				int _itemCount = 0;
				int _price = 0;
				
				if (!buylist.isGm() && (NpcTable.getInstance().getTemplate(rset1.getInt("npc_id")) == null))
				{
					LOGGER.warning("TradeListTable: Merchant id " + rset1.getString("npc_id") + " with buylist " + buylist.getListId() + " does not exist.");
				}
				
				try
				{
					while (rset.next())
					{
						_itemId = rset.getInt("item_id");
						_price = rset.getInt("price");
						final int count = rset.getInt("count");
						final int currentCount = rset.getInt("currentCount");
						final int time = rset.getInt("time");
						
						final ItemInstance buyItem = ItemTable.getInstance().createDummyItem(_itemId);
						
						if (buyItem == null)
						{
							continue;
						}
						
						_itemCount++;
						
						if (count > -1)
						{
							buyItem.setCountDecrease(true);
						}
						buyItem.setPriceToSell(_price);
						buyItem.setTime(time);
						buyItem.setInitCount(count);
						
						if (currentCount > -1)
						{
							buyItem.setCount(currentCount);
						}
						else
						{
							buyItem.setCount(count);
						}
						
						buylist.addItem(buyItem);
						
						if (!buylist.isGm() && (buyItem.getReferencePrice() > _price))
						{
							LOGGER.warning("TradeListTable: Reference price of item " + _itemId + " in buylist " + buylist.getListId() + " higher then sell price.");
						}
					}
				}
				catch (Exception e)
				{
					LOGGER.warning("TradeListTable: Problem with buylist " + buylist.getListId() + ". " + e);
				}
				
				if (_itemCount > 0)
				{
					_lists.put(buylist.getListId(), buylist);
					_nextListId = Math.max(_nextListId, buylist.getListId() + 1);
				}
				else
				{
					LOGGER.warning("TradeListTable: Empty buylist " + buylist.getListId() + ".");
				}
				
				statement.close();
				rset.close();
			}
			rset1.close();
			statement1.close();
			
			LOGGER.info("TradeListTable: Loaded " + _lists.size() + " Buylists.");
			
			try
			{
				int time = 0;
				long savetimer = 0;
				final long currentMillis = System.currentTimeMillis();
				final PreparedStatement statement2 = con.prepareStatement("SELECT DISTINCT time, savetimer FROM " + (custom ? "merchant_buylists" : "merchant_buylists") + " WHERE time <> 0 ORDER BY time");
				final ResultSet rset2 = statement2.executeQuery();
				
				while (rset2.next())
				{
					time = rset2.getInt("time");
					savetimer = rset2.getLong("savetimer");
					if ((savetimer - currentMillis) > 0)
					{
						ThreadPool.schedule(new RestoreCount(time), savetimer - System.currentTimeMillis());
					}
					else
					{
						ThreadPool.schedule(new RestoreCount(time), 0);
					}
				}
				
				rset2.close();
				statement2.close();
			}
			catch (Exception e)
			{
				LOGGER.warning("TradeController: Could not restore Timer for Item count. " + e);
			}
		}
		catch (Exception e)
		{
			// problem with initializing buylists, go to next one
			LOGGER.warning("TradeListTable: Buylists could not be initialized. " + e);
		}
	}
	
	public void load()
	{
		load(false); // not custom
		load(true); // custom
	}
	
	public void reloadAll()
	{
		_lists.clear();
		
		load();
	}
	
	public StoreTradeList getBuyList(int listId)
	{
		if (_lists.containsKey(listId))
		{
			return _lists.get(listId);
		}
		
		return null;
	}
	
	public List<StoreTradeList> getBuyListByNpcId(int npcId)
	{
		final List<StoreTradeList> lists = new ArrayList<>();
		
		for (StoreTradeList list : _lists.values())
		{
			if (list.isGm())
			{
				continue;
			}
			/** if (npcId == list.getNpcId()) **/
			lists.add(list);
		}
		
		return lists;
	}
	
	protected void restoreCount(int time)
	{
		if (_lists == null)
		{
			return;
		}
		
		for (StoreTradeList list : _lists.values())
		{
			list.restoreCount(time);
		}
	}
	
	protected void dataTimerSave(int time)
	{
		final long timerSave = System.currentTimeMillis() + (time * 3600000); // 60*60*1000
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("UPDATE merchant_buylists SET savetimer =? WHERE time =?");
			statement.setLong(1, timerSave);
			statement.setInt(2, time);
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("TradeController: Could not update Timer save in Buylist. " + e);
		}
	}
	
	public void dataCountStore()
	{
		if (_lists == null)
		{
			return;
		}
		
		int listId;
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			
			for (StoreTradeList list : _lists.values())
			{
				if (list == null)
				{
					continue;
				}
				
				listId = list.getListId();
				
				for (ItemInstance Item : list.getItems())
				{
					if (Item.getCount() < Item.getInitCount()) // needed?
					{
						statement = con.prepareStatement("UPDATE merchant_buylists SET currentCount=? WHERE item_id=? AND shop_id=?");
						statement.setInt(1, Item.getCount());
						statement.setInt(2, Item.getItemId());
						statement.setInt(3, listId);
						statement.executeUpdate();
						statement.close();
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("TradeController: Could not store Count Item. " + e);
		}
	}
	
	public static TradeListTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final TradeListTable INSTANCE = new TradeListTable();
	}
}
