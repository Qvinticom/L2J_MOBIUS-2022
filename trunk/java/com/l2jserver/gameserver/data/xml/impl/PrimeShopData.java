/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.data.xml.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jserver.Config;
import com.l2jserver.gameserver.data.xml.IXmlReader;
import com.l2jserver.gameserver.datatables.ItemTable;
import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.primeshop.PrimeShopGroup;
import com.l2jserver.gameserver.model.primeshop.PrimeShopItem;
import com.l2jserver.gameserver.network.serverpackets.primeshop.ExBRBuyProduct;
import com.l2jserver.gameserver.network.serverpackets.primeshop.ExBRBuyProduct.ExBrProductReplyType;
import com.l2jserver.gameserver.network.serverpackets.primeshop.ExBRGamePoint;
import com.l2jserver.gameserver.network.serverpackets.primeshop.ExBRProductInfo;
import com.l2jserver.gameserver.util.Util;

/**
 * @author Gnacik, UnAfraid
 */
public class PrimeShopData implements IXmlReader
{
	private final Map<Integer, PrimeShopGroup> _primeItems = new LinkedHashMap<>();
	
	protected PrimeShopData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_primeItems.clear();
		parseDatapackFile("data/PrimeShop.xml");
		
		if (_primeItems.size() > 0)
		{
			LOGGER.info(getClass().getSimpleName() + ": Loaded " + _primeItems.size() + " items");
		}
		else
		{
			LOGGER.info(getClass().getSimpleName() + ": System is disabled.");
		}
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				NamedNodeMap at = n.getAttributes();
				Node attribute = at.getNamedItem("enabled");
				if ((attribute != null) && Boolean.parseBoolean(attribute.getNodeValue()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("item".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();
							Node att;
							StatsSet set = new StatsSet();
							for (int i = 0; i < attrs.getLength(); i++)
							{
								att = attrs.item(i);
								set.set(att.getNodeName(), att.getNodeValue());
							}
							
							List<PrimeShopItem> items = new ArrayList<>();
							for (Node b = d.getFirstChild(); b != null; b = b.getNextSibling())
							{
								if ("item".equalsIgnoreCase(b.getNodeName()))
								{
									attrs = b.getAttributes();
									
									final int itemId = parseInteger(attrs, "itemId");
									final int count = parseInteger(attrs, "count");
									
									final L2Item item = ItemTable.getInstance().getTemplate(itemId);
									if (item == null)
									{
										LOGGER.severe(getClass().getSimpleName() + ": Item template null for itemId: " + itemId + " brId: " + set.getInt("id"));
										return;
									}
									
									items.add(new PrimeShopItem(itemId, count, item.getWeight(), item.isTradeable() ? 1 : 0));
								}
							}
							
							_primeItems.put(set.getInt("id"), new PrimeShopGroup(set, items));
						}
					}
				}
			}
		}
	}
	
	public void buyItem(L2PcInstance player, int brId, int count)
	{
		if (validatePlayer(brId, count, player))
		{
			final PrimeShopGroup item = _primeItems.get(brId);
			for (PrimeShopItem itm : item.getItems())
			{
				player.addItem("PrimeShop", itm.getId(), itm.getCount() * count, player, true);
			}
			final int points = player.getPrimePoints() - (item.getPrice() * count);
			
			player.setPrimePoints(points);
			
			player.sendPacket(new ExBRBuyProduct(ExBrProductReplyType.SUCCESS));
			player.sendPacket(new ExBRGamePoint(player));
		}
	}
	
	/**
	 * @param brId
	 * @param count
	 * @param player
	 * @return
	 */
	private boolean validatePlayer(int brId, int count, L2PcInstance player)
	{
		if ((count < 1) && (count > 99))
		{
			Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to buy invalid itemcount [" + count + "] from Prime", Config.DEFAULT_PUNISH);
			player.sendPacket(new ExBRBuyProduct(ExBrProductReplyType.INVALID_USER_STATE));
			return false;
		}
		
		final PrimeShopGroup item = _primeItems.get(brId);
		final long currentTime = System.currentTimeMillis() / 1000;
		
		if (item == null)
		{
			player.sendPacket(new ExBRBuyProduct(ExBrProductReplyType.INVALID_PRODUCT));
			Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to buy invalid brId from Prime", Config.DEFAULT_PUNISH);
			return false;
		}
		else if ((item.getMinLevel() > 0) && (item.getMinLevel() > player.getLevel()))
		{
			player.sendPacket(new ExBRBuyProduct(ExBrProductReplyType.INVALID_USER));
			return false;
		}
		else if ((item.getMaxLevel() > 0) && (item.getMaxLevel() < player.getLevel()))
		{
			player.sendPacket(new ExBRBuyProduct(ExBrProductReplyType.INVALID_USER));
			return false;
		}
		else if ((item.getMinBirthday() > 0) && (item.getMinBirthday() > player.getBirthdays()))
		{
			player.sendPacket(new ExBRBuyProduct(ExBrProductReplyType.INVALID_USER_STATE));
			return false;
		}
		else if ((item.getMaxBirthday() > 0) && (item.getMaxBirthday() < player.getBirthdays()))
		{
			player.sendPacket(new ExBRBuyProduct(ExBrProductReplyType.INVALID_USER_STATE));
			return false;
		}
		else if ((Calendar.getInstance().get(Calendar.DAY_OF_WEEK) & item.getDaysOfWeek()) == 0)
		{
			player.sendPacket(new ExBRBuyProduct(ExBrProductReplyType.NOT_DAY_OF_WEEK));
			return false;
		}
		else if ((item.getStartSale() > 1) && (item.getStartSale() > currentTime))
		{
			player.sendPacket(new ExBRBuyProduct(ExBrProductReplyType.BEFORE_SALE_DATE));
			return false;
		}
		else if ((item.getEndSale() > 1) && (item.getEndSale() < currentTime))
		{
			player.sendPacket(new ExBRBuyProduct(ExBrProductReplyType.AFTER_SALE_DATE));
			return false;
		}
		
		final int weight = item.getWeight() * count;
		final long slots = item.getCount() * count;
		
		if (player.getPrimePoints() < (item.getPrice() * count))
		{
			player.sendPacket(new ExBRBuyProduct(ExBrProductReplyType.LACK_OF_POINT));
			return false;
		}
		else if (player.getInventory().validateWeight(weight))
		{
			if (!player.getInventory().validateCapacity(slots))
			{
				player.sendPacket(new ExBRBuyProduct(ExBrProductReplyType.INVENTROY_OVERFLOW));
				return false;
			}
		}
		else
		{
			player.sendPacket(new ExBRBuyProduct(ExBrProductReplyType.INVENTROY_OVERFLOW));
			return false;
		}
		
		return true;
	}
	
	public void showProductInfo(L2PcInstance player, int brId)
	{
		final PrimeShopGroup item = _primeItems.get(brId);
		
		if ((player == null) || (item == null))
		{
			return;
		}
		
		player.sendPacket(new ExBRProductInfo(item));
	}
	
	public Map<Integer, PrimeShopGroup> getPrimeItems()
	{
		return _primeItems;
	}
	
	public static PrimeShopData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final PrimeShopData _instance = new PrimeShopData();
	}
}
