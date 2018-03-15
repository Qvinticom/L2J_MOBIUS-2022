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
package com.l2jmobius.gameserver.data.xml.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.model.ItemMallProduct;
import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.util.data.xml.IXmlReader;

/**
 * @author Mobius
 */
public class ItemMallData implements IXmlReader
{
	private static final Logger _log = Logger.getLogger(ItemMallData.class.getName());
	private final Map<Integer, ItemMallProduct> _mallList = new HashMap<>();
	
	protected ItemMallData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_mallList.clear();
		
		if (!Config.ENABLE_ITEM_MALL)
		{
			return;
		}
		
		parseDatapackFile("data/ItemMall.xml");
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		NamedNodeMap attrs;
		Node att;
		StatsSet set = null;
		for (Node a = doc.getFirstChild(); a != null; a = a.getNextSibling())
		{
			if ("list".equalsIgnoreCase(a.getNodeName()))
			{
				for (Node b = a.getFirstChild(); b != null; b = b.getNextSibling())
				{
					if ("product".equalsIgnoreCase(b.getNodeName()))
					{
						attrs = b.getAttributes();
						set = new StatsSet();
						for (int i = 0; i < attrs.getLength(); i++)
						{
							att = attrs.item(i);
							set.set(att.getNodeName(), att.getNodeValue());
						}
						final ItemMallProduct product = new ItemMallProduct(set.getInt("id"), set.getInt("category"), set.getInt("points"), set.getInt("item"), set.getInt("count"));
						_mallList.put(set.getInt("id"), product);
					}
				}
			}
		}
		
		_log.info(getClass().getSimpleName() + ": Loaded " + _mallList.size() + " products.");
	}
	
	public Collection<ItemMallProduct> getAllItems()
	{
		return _mallList.values();
	}
	
	public ItemMallProduct getProduct(int id)
	{
		return _mallList.get(id);
	}
	
	public static ItemMallData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ItemMallData _instance = new ItemMallData();
	}
}
