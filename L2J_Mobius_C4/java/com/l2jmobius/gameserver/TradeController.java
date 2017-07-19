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
package com.l2jmobius.gameserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.L2DatabaseFactory;
import com.l2jmobius.gameserver.datatables.ItemTable;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.L2TradeList;

import javolution.util.FastList;
import javolution.util.FastMap;

/**
 * This class ...
 * @version $Revision: 1.5.4.13 $ $Date: 2005/04/06 16:13:38 $
 */
public class TradeController
{
	private static Logger _log = Logger.getLogger(TradeController.class.getName());
	private static TradeController _instance;
	
	private int _nextListId;
	private final Map<Integer, L2TradeList> _lists;
	
	public static TradeController getInstance()
	{
		if (_instance == null)
		{
			_instance = new TradeController();
		}
		
		return _instance;
	}
	
	private TradeController()
	{
		_lists = new FastMap<>();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement1 = con.prepareStatement("SELECT shop_id, npc_id  FROM merchant_shopids");
			ResultSet rset1 = statement1.executeQuery())
		{
			int itemId, price, count, currentCount, time;
			long saveTime;
			while (rset1.next())
			{
				try (
					PreparedStatement statement = con.prepareStatement("SELECT item_id, price, shop_id, " + L2DatabaseFactory.getInstance().safetyString("order") + ", count, currentCount, time, savetimer FROM merchant_buylists WHERE shop_id=? ORDER BY " + L2DatabaseFactory.getInstance().safetyString("order") + " ASC"))
				{
					statement.setString(1, String.valueOf(rset1.getInt("shop_id")));
					try (ResultSet rset = statement.executeQuery())
					{
						final L2TradeList buy1 = new L2TradeList(rset1.getInt("shop_id"));
						
						while (rset.next())
						{
							itemId = rset.getInt("item_id");
							price = rset.getInt("price");
							count = rset.getInt("count");
							currentCount = rset.getInt("currentCount");
							time = rset.getInt("time");
							saveTime = rset.getLong("savetimer");
							
							final L2ItemInstance item = ItemTable.getInstance().createDummyItem(itemId);
							if (item == null)
							{
								_log.warning("Skipping itemId: " + itemId + " on buylistId: " + buy1.getListId() + ", missing data for that item.");
								continue;
							}
							
							if (price <= -1)
							{
								price = item.getItem().getReferencePrice();
							}
							
							if (Config.DEBUG)
							{
								// debug
								final double diff = ((double) (price)) / item.getItem().getReferencePrice();
								if ((diff < 0.8) || (diff > 1.2))
								{
									_log.severe("PRICING DEBUG: TradeListId: " + buy1.getListId() + " -  ItemId: " + itemId + " (" + item.getItem().getName() + ") diff: " + diff + " - Price: " + price + " - Reference: " + item.getItem().getReferencePrice());
								}
							}
							
							if (count > -1)
							{
								item.setCountDecrease(true);
							}
							
							item.setPriceToSell(price);
							item.setInitCount(count);
							
							if (currentCount > -1)
							{
								item.setCount(currentCount);
							}
							else
							{
								item.setCount(count);
							}
							
							item.setTime(time);
							if (item.getTime() > 0)
							{
								item.setRestoreTime(saveTime);
							}
							
							buy1.addItem(item);
							buy1.setNpcId(rset1.getString("npc_id"));
							
							_lists.put(new Integer(buy1.getListId()), buy1);
							_nextListId = Math.max(_nextListId, buy1.getListId() + 1);
						}
					}
				}
			}
			
			_log.config("TradeController: Loaded " + _lists.size() + " Buylists.");
		}
		catch (final Exception e)
		{
			_log.warning("TradeController: Buylists could not be initialized.");
			e.printStackTrace();
		}
		
		/*
		 * If enabled, initialize the custom buylist
		 */
		if (Config.CUSTOM_MERCHANT_TABLES)
		{
			final int initialSize = _lists.size();
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement1 = con.prepareStatement("SELECT shop_id, npc_id  FROM custom_merchant_shopids");
				ResultSet rset1 = statement1.executeQuery())
			{
				int itemId, price, count, currentCount, time;
				long saveTime;
				while (rset1.next())
				{
					try (
						PreparedStatement statement = con.prepareStatement("SELECT item_id, price, shop_id, " + L2DatabaseFactory.getInstance().safetyString("order") + ", count, currentCount, time, savetimer FROM custom_merchant_buylists WHERE shop_id=? ORDER BY " + L2DatabaseFactory.getInstance().safetyString("order") + " ASC"))
					{
						statement.setString(1, String.valueOf(rset1.getInt("shop_id")));
						try (ResultSet rset = statement.executeQuery())
						{
							final L2TradeList buy1 = new L2TradeList(rset1.getInt("shop_id"));
							
							while (rset.next())
							{
								itemId = rset.getInt("item_id");
								price = rset.getInt("price");
								count = rset.getInt("count");
								currentCount = rset.getInt("currentCount");
								time = rset.getInt("time");
								saveTime = rset.getLong("savetimer");
								
								final L2ItemInstance item = ItemTable.getInstance().createDummyItem(itemId);
								if (item == null)
								{
									_log.warning("Skipping itemId: " + itemId + " on buylistId: " + buy1.getListId() + ", missing data for that item.");
									continue;
								}
								
								if (price <= -1)
								{
									price = item.getItem().getReferencePrice();
								}
								
								if (Config.DEBUG)
								{
									// debug
									final double diff = ((double) (price)) / item.getItem().getReferencePrice();
									if ((diff < 0.8) || (diff > 1.2))
									{
										_log.severe("PRICING DEBUG: TradeListId: " + buy1.getListId() + " -  ItemId: " + itemId + " (" + item.getItem().getName() + ") diff: " + diff + " - Price: " + price + " - Reference: " + item.getItem().getReferencePrice());
									}
								}
								
								if (count > -1)
								{
									item.setCountDecrease(true);
								}
								
								item.setPriceToSell(price);
								item.setInitCount(count);
								
								if (currentCount > -1)
								{
									item.setCount(currentCount);
								}
								else
								{
									item.setCount(count);
								}
								
								item.setTime(time);
								if (item.getTime() > 0)
								{
									item.setRestoreTime(saveTime);
								}
								
								buy1.addItem(item);
								buy1.setNpcId(rset1.getString("npc_id"));
								
								_lists.put(new Integer(buy1.getListId()), buy1);
								_nextListId = Math.max(_nextListId, buy1.getListId() + 1);
							}
						}
					}
				}
				
				_log.config("TradeController: Loaded " + (_lists.size() - initialSize) + " Custom Buylists.");
			}
			catch (final Exception e)
			{
				_log.warning("TradeController: Custom Buylists could not be initialized.");
				e.printStackTrace();
			}
		}
	}
	
	// private int parseList(String line)
	// {
	// int itemCreated = 0;
	// StringTokenizer st = new StringTokenizer(line, ";");
	// int listId = Integer.parseInt(st.nextToken());
	// L2TradeList buy1 = new L2TradeList(listId);
	// while (st.hasMoreTokens())
	// {
	// int itemId = Integer.parseInt(st.nextToken());
	// int price = Integer.parseInt(st.nextToken());
	//
	// L2ItemInstance item = ItemTable.getInstance().createDummyItem(itemId);
	// item.setPriceToSell(price);
	// buy1.addItem(item);
	// itemCreated++;
	// }
	// _lists.put(new Integer(buy1.getListId()), buy1);
	// return itemCreated;
	// }
	
	public L2TradeList getBuyList(int listId)
	{
		return _lists.get(new Integer(listId));
	}
	
	public List<L2TradeList> getBuyListByNpcId(int npcId)
	{
		final List<L2TradeList> lists = new FastList<>();
		
		for (final L2TradeList list : _lists.values())
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
	
	public void dataCountStore()
	{
		int listId;
		
		if (_lists == null)
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			for (final L2TradeList list : _lists.values())
			{
				listId = list.getListId();
				// if (list == null)
				// {
				// continue;
				// }
				
				for (final L2ItemInstance Item : list.getItems())
				{
					if (Item.getCount() < Item.getInitCount())
					{
						try (PreparedStatement statement = con.prepareStatement("UPDATE merchant_buylists SET currentCount =? WHERE item_id =? && shop_id = ?"))
						{
							statement.setInt(1, Item.getCount());
							statement.setInt(2, Item.getItemId());
							statement.setInt(3, listId);
							statement.executeUpdate();
						}
					}
				}
			}
		}
		catch (final Exception e)
		{
			_log.log(Level.SEVERE, "TradeController: Could not store Count Item");
		}
	}
	
	/**
	 * @return
	 */
	public synchronized int getNextId()
	{
		return _nextListId++;
	}
}