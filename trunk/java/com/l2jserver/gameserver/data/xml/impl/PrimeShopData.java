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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jserver.gameserver.datatables.ItemTable;
import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.primeshop.PrimeShopGroup;
import com.l2jserver.gameserver.model.primeshop.PrimeShopItem;
import com.l2jserver.gameserver.network.serverpackets.primeshop.ExBRProductInfo;
import com.l2jserver.util.data.xml.IXmlReader;

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
		parseDatapackFile("PrimeShop.xml");
		
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
	
	public void showProductInfo(L2PcInstance player, int brId)
	{
		final PrimeShopGroup item = _primeItems.get(brId);
		
		if ((player == null) || (item == null))
		{
			return;
		}
		
		player.sendPacket(new ExBRProductInfo(item));
	}
	
	public PrimeShopGroup getItem(int brId)
	{
		return _primeItems.get(brId);
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
