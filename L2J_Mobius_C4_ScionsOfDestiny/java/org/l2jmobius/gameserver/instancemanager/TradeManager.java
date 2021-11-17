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
package org.l2jmobius.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.model.StoreTradeList;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.taskmanager.BuyListTaskManager;

/**
 * @version $Revision: 1.5.4.13 $ $Date: 2005/04/06 16:13:38 $
 */
public class TradeManager
{
	private static final Logger LOGGER = Logger.getLogger(TradeManager.class.getName());
	
	private int _nextListId;
	private final Map<Integer, StoreTradeList> _lists;
	private final Map<Integer, StoreTradeList> _listsTaskItem;
	
	protected TradeManager()
	{
		boolean limitedItem = false;
		_lists = new HashMap<>();
		_listsTaskItem = new HashMap<>();
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement1 = con.prepareStatement("SELECT * FROM merchant_shopids");
			final ResultSet rset1 = statement1.executeQuery();
			
			while (rset1.next())
			{
				final PreparedStatement statement = con.prepareStatement("SELECT * FROM merchant_buylists WHERE shop_id=? ORDER BY `order` ASC");
				statement.setString(1, String.valueOf(rset1.getInt("shop_id")));
				final ResultSet rset = statement.executeQuery();
				if (rset.next())
				{
					limitedItem = false;
					final StoreTradeList buy1 = new StoreTradeList(rset1.getInt("shop_id"));
					int itemId = rset.getInt("item_id");
					int price = rset.getInt("price");
					int count = rset.getInt("count");
					int currentCount = rset.getInt("currentCount");
					int time = rset.getInt("time");
					
					final Item item = ItemTable.getInstance().createDummyItem(itemId);
					if (item == null)
					{
						rset.close();
						statement.close();
						continue;
					}
					
					if (count > -1)
					{
						item.setCountDecrease(true);
						limitedItem = true;
					}
					
					final int sellPrice = item.getReferencePrice() / 2;
					if (Config.CORRECT_PRICES && (price < sellPrice) && !rset1.getString("npc_id").equals("gm"))
					{
						LOGGER.warning("Buy price " + price + " is less than sell price " + sellPrice + " for ItemID:" + itemId + " of buylist " + buy1.getListId() + ".");
						price = sellPrice;
					}
					
					item.setPriceToSell(price);
					item.setTime(time);
					item.setInitCount(count);
					
					if (currentCount > -1)
					{
						item.setCount(currentCount);
					}
					else
					{
						item.setCount(count);
					}
					
					buy1.addItem(item);
					buy1.setNpcId(rset1.getString("npc_id"));
					
					try
					{
						while (rset.next())
						{
							itemId = rset.getInt("item_id");
							price = rset.getInt("price");
							count = rset.getInt("count");
							time = rset.getInt("time");
							currentCount = rset.getInt("currentCount");
							final Item item2 = ItemTable.getInstance().createDummyItem(itemId);
							if (item2 == null)
							{
								continue;
							}
							
							if (count > -1)
							{
								item2.setCountDecrease(true);
								limitedItem = true;
							}
							
							if (!rset1.getString("npc_id").equals("gm") && (price < (item2.getReferencePrice() / 2)))
							{
								LOGGER.warning("TradeList " + buy1.getListId() + " itemId  " + itemId + " has an ADENA sell price lower then reference price.. Automatically Updating it..");
								price = item2.getReferencePrice();
							}
							
							item2.setPriceToSell(price);
							
							item2.setTime(time);
							item2.setInitCount(count);
							if (currentCount > -1)
							{
								item2.setCount(currentCount);
							}
							else
							{
								item2.setCount(count);
							}
							buy1.addItem(item2);
						}
					}
					catch (Exception e)
					{
						LOGGER.warning("TradeController: Problem with buylist " + buy1.getListId() + " item " + itemId);
					}
					if (limitedItem)
					{
						_listsTaskItem.put(buy1.getListId(), buy1);
					}
					else
					{
						_lists.put(buy1.getListId(), buy1);
					}
					
					_nextListId = Math.max(_nextListId, buy1.getListId() + 1);
				}
				
				rset.close();
				statement.close();
			}
			rset1.close();
			statement1.close();
			
			LOGGER.info("TradeController: Loaded " + _lists.size() + " Buylists.");
			LOGGER.info("TradeController: Loaded " + _listsTaskItem.size() + " Limited Buylists.");
			
			/*
			 * Restore Task for reinitialize count of buy item
			 */
			try
			{
				int time = 0;
				long savetimer = 0;
				final long currentMillis = Chronos.currentTimeMillis();
				final PreparedStatement statement2 = con.prepareStatement("SELECT DISTINCT time, savetimer FROM merchant_buylists WHERE time <> 0 ORDER BY time");
				final ResultSet rset2 = statement2.executeQuery();
				
				while (rset2.next())
				{
					time = rset2.getInt("time");
					savetimer = rset2.getLong("savetimer");
					if ((savetimer - currentMillis) > 0)
					{
						BuyListTaskManager.getInstance().addTime(time, savetimer);
					}
					else
					{
						BuyListTaskManager.getInstance().addTime(time, 0);
					}
				}
				rset2.close();
				statement2.close();
			}
			catch (Exception e)
			{
				LOGGER.warning("TradeController: Could not restore Timer for Item count. " + e.getMessage());
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("TradeController: Buylists could not be initialized." + e.getMessage());
		}
		
		/*
		 * If enabled, initialize the custom buylist.
		 */
		if (Config.CUSTOM_MERCHANT_TABLES) // Custom merchant tables.
		{
			try (Connection con = DatabaseFactory.getConnection())
			{
				final int initialSize = _lists.size();
				final PreparedStatement statement1 = con.prepareStatement("SELECT * FROM custom_merchant_shopids");
				final ResultSet rset1 = statement1.executeQuery();
				
				while (rset1.next())
				{
					final PreparedStatement statement = con.prepareStatement("SELECT * FROM custom_merchant_buylists WHERE shop_id=? ORDER BY `order` ASC");
					statement.setString(1, String.valueOf(rset1.getInt("shop_id")));
					final ResultSet rset = statement.executeQuery();
					if (rset.next())
					{
						limitedItem = false;
						final StoreTradeList buy1 = new StoreTradeList(rset1.getInt("shop_id"));
						int itemId = rset.getInt("item_id");
						int price = rset.getInt("price");
						int count = rset.getInt("count");
						int currentCount = rset.getInt("currentCount");
						int time = rset.getInt("time");
						final Item item = ItemTable.getInstance().createDummyItem(itemId);
						if (item == null)
						{
							rset.close();
							statement.close();
							continue;
						}
						
						if (count > -1)
						{
							item.setCountDecrease(true);
							limitedItem = true;
						}
						
						if (!rset1.getString("npc_id").equals("gm") && (price < (item.getReferencePrice() / 2)))
						{
							LOGGER.warning("TradeList " + buy1.getListId() + " itemId  " + itemId + " has an ADENA sell price lower then reference price.. Automatically Updating it..");
							price = item.getReferencePrice();
						}
						
						item.setPriceToSell(price);
						item.setTime(time);
						item.setInitCount(count);
						
						if (currentCount > -1)
						{
							item.setCount(currentCount);
						}
						else
						{
							item.setCount(count);
						}
						
						buy1.addItem(item);
						buy1.setNpcId(rset1.getString("npc_id"));
						
						try
						{
							while (rset.next())
							{
								itemId = rset.getInt("item_id");
								price = rset.getInt("price");
								count = rset.getInt("count");
								time = rset.getInt("time");
								currentCount = rset.getInt("currentCount");
								final Item item2 = ItemTable.getInstance().createDummyItem(itemId);
								if (item2 == null)
								{
									continue;
								}
								if (count > -1)
								{
									item2.setCountDecrease(true);
									limitedItem = true;
								}
								
								if (!rset1.getString("npc_id").equals("gm") && (price < (item2.getReferencePrice() / 2)))
								{
									LOGGER.warning("TradeList " + buy1.getListId() + " itemId  " + itemId + " has an ADENA sell price lower then reference price.. Automatically Updating it..");
									price = item2.getReferencePrice();
								}
								
								item2.setPriceToSell(price);
								item2.setTime(time);
								item2.setInitCount(count);
								if (currentCount > -1)
								{
									item2.setCount(currentCount);
								}
								else
								{
									item2.setCount(count);
								}
								buy1.addItem(item2);
							}
						}
						catch (Exception e)
						{
							LOGGER.warning("TradeController: Problem with buylist " + buy1.getListId() + " item " + itemId);
						}
						if (limitedItem)
						{
							_listsTaskItem.put(buy1.getListId(), buy1);
						}
						else
						{
							_lists.put(buy1.getListId(), buy1);
						}
						_nextListId = Math.max(_nextListId, buy1.getListId() + 1);
					}
					
					rset.close();
					statement.close();
				}
				rset1.close();
				statement1.close();
				
				LOGGER.info("TradeController: Loaded " + (_lists.size() - initialSize) + " Custom Buylists.");
				
				/**
				 * Restore Task for reinitialize count of buy item
				 */
				try
				{
					int time = 0;
					long savetimer = 0;
					final long currentMillis = Chronos.currentTimeMillis();
					final PreparedStatement statement2 = con.prepareStatement("SELECT DISTINCT time, savetimer FROM custom_merchant_buylists WHERE time <> 0 ORDER BY time");
					final ResultSet rset2 = statement2.executeQuery();
					
					while (rset2.next())
					{
						time = rset2.getInt("time");
						savetimer = rset2.getLong("savetimer");
						if ((savetimer - currentMillis) > 0)
						{
							BuyListTaskManager.getInstance().addTime(time, savetimer);
						}
						else
						{
							BuyListTaskManager.getInstance().addTime(time, 0);
						}
					}
					rset2.close();
					statement2.close();
				}
				catch (Exception e)
				{
					LOGGER.warning("TradeController: Could not restore Timer for Item count. " + e.getMessage());
				}
			}
			catch (Exception e)
			{
				LOGGER.warning("TradeController: Buylists could not be initialized. " + e.getMessage());
			}
		}
	}
	
	public StoreTradeList getBuyList(int listId)
	{
		if (_lists.get(listId) != null)
		{
			return _lists.get(listId);
		}
		return _listsTaskItem.get(listId);
	}
	
	public List<StoreTradeList> getBuyListByNpcId(int npcId)
	{
		final List<StoreTradeList> lists = new ArrayList<>();
		for (StoreTradeList list : _lists.values())
		{
			if (list.getNpcId().startsWith("gm"))
			{
				continue;
			}
			
			if (npcId == Integer.parseInt(list.getNpcId()))
			{
				lists.add(list);
			}
		}
		for (StoreTradeList list : _listsTaskItem.values())
		{
			if (list.getNpcId().startsWith("gm"))
			{
				continue;
			}
			
			if (npcId == Integer.parseInt(list.getNpcId()))
			{
				lists.add(list);
			}
		}
		return lists;
	}
	
	public void restoreCount(int time)
	{
		if (_listsTaskItem == null)
		{
			return;
		}
		
		for (StoreTradeList list : _listsTaskItem.values())
		{
			list.restoreCount(time);
		}
	}
	
	public void dataTimerSave(int time)
	{
		final long timerSave = Chronos.currentTimeMillis() + (time * 60 * 60 * 1000);
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("UPDATE merchant_buylists SET savetimer=? WHERE time=?");
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
		int listId;
		if (_listsTaskItem == null)
		{
			return;
		}
		
		PreparedStatement statement;
		try (Connection con = DatabaseFactory.getConnection())
		{
			for (StoreTradeList list : _listsTaskItem.values())
			{
				if (list == null)
				{
					continue;
				}
				
				listId = list.getListId();
				for (Item item : list.getItems())
				{
					if (item.getCount() < item.getInitCount()) // needed?
					{
						statement = con.prepareStatement("UPDATE merchant_buylists SET currentCount=? WHERE item_id=? AND shop_id=?");
						statement.setInt(1, item.getCount());
						statement.setInt(2, item.getItemId());
						statement.setInt(3, listId);
						statement.executeUpdate();
						statement.close();
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("TradeController: Could not store Count Item");
		}
	}
	
	public synchronized int getNextId()
	{
		return _nextListId++;
	}
	
	public static TradeManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final TradeManager INSTANCE = new TradeManager();
	}
}
