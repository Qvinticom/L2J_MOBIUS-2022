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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jmobius.gameserver.model.CrystalizationData;
import com.l2jmobius.gameserver.model.holders.ItemChanceHolder;
import com.l2jmobius.util.data.xml.IXmlReader;

/**
 * @author UnAfraid
 */
public final class ItemCrystalizationData implements IXmlReader
{
	private final Map<Integer, CrystalizationData> _items = new HashMap<>();
	
	protected ItemCrystalizationData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseDatapackFile("CrystalizableItems.xml");
		LOGGER.log(Level.INFO, getClass().getSimpleName() + ": Loaded: " + _items.size() + " Items");
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("crystalizable_item".equalsIgnoreCase(d.getNodeName()))
					{
						final int id = parseInteger(d.getAttributes(), "id");
						final CrystalizationData data = new CrystalizationData(id);
						for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling())
						{
							if ("item".equalsIgnoreCase(c.getNodeName()))
							{
								final NamedNodeMap attrs = c.getAttributes();
								data.addItem(new ItemChanceHolder(parseInteger(attrs, "id"), parseDouble(attrs, "chance"), parseLong(attrs, "count")));
							}
						}
						_items.put(id, data);
					}
				}
			}
		}
	}
	
	public CrystalizationData getCrystalization(int itemId)
	{
		return _items.get(itemId);
	}
	
	/**
	 * Gets the single instance of ItemCrystalizationData.
	 * @return single instance of ItemCrystalizationData
	 */
	public static final ItemCrystalizationData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ItemCrystalizationData _instance = new ItemCrystalizationData();
	}
}
