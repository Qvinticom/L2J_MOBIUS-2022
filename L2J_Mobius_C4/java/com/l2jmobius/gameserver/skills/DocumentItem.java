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
package com.l2jmobius.gameserver.skills;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.l2jmobius.gameserver.Item;
import com.l2jmobius.gameserver.templates.L2Armor;
import com.l2jmobius.gameserver.templates.L2ArmorType;
import com.l2jmobius.gameserver.templates.L2EtcItem;
import com.l2jmobius.gameserver.templates.L2EtcItemType;
import com.l2jmobius.gameserver.templates.L2Item;
import com.l2jmobius.gameserver.templates.L2Weapon;
import com.l2jmobius.gameserver.templates.L2WeaponType;
import com.l2jmobius.gameserver.templates.StatsSet;

import javolution.util.FastList;
import javolution.util.FastMap;

/**
 * @author mkizub TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
final class DocumentItem extends DocumentBase
{
	private Item currentItem = null;
	private final List<L2Item> itemsInFile = new FastList<>();
	private Map<Integer, Item> itemData = new FastMap<>();
	
	/**
	 * @param pItemData
	 * @param file
	 */
	public DocumentItem(Map<Integer, Item> pItemData, File file)
	{
		super(file);
		itemData = pItemData;
	}
	
	/**
	 * @param item
	 */
	private void setCurrentItem(Item item)
	{
		currentItem = item;
	}
	
	@Override
	protected StatsSet getStatsSet()
	{
		return currentItem.set;
	}
	
	@Override
	protected Number getTableValue(String name)
	{
		return tables.get(name)[currentItem.currentLevel];
	}
	
	@Override
	protected Number getTableValue(String name, int idx)
	{
		return tables.get(name)[idx - 1];
	}
	
	@Override
	protected void parseDocument(Document doc)
	{
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("item".equalsIgnoreCase(d.getNodeName()))
					{
						setCurrentItem(new Item());
						parseItem(d);
						itemsInFile.add(currentItem.item);
						resetTable();
					}
				}
			}
			else if ("item".equalsIgnoreCase(n.getNodeName()))
			{
				setCurrentItem(new Item());
				parseItem(n);
				itemsInFile.add(currentItem.item);
			}
		}
	}
	
	protected void parseItem(Node n)
	{
		final int itemId = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
		final String itemName = n.getAttributes().getNamedItem("name").getNodeValue();
		
		currentItem.id = itemId;
		currentItem.name = itemName;
		currentItem.set = itemData.get(currentItem.id).set;
		currentItem.type = itemData.get(currentItem.id).type;
		
		final Node first = n.getFirstChild();
		for (n = first; n != null; n = n.getNextSibling())
		{
			if ("table".equalsIgnoreCase(n.getNodeName()))
			{
				parseTable(n);
			}
		}
		for (n = first; n != null; n = n.getNextSibling())
		{
			if ("set".equalsIgnoreCase(n.getNodeName()))
			{
				parseBeanSet(n, itemData.get(currentItem.id).set, 1);
			}
		}
		for (n = first; n != null; n = n.getNextSibling())
		{
			if ("for".equalsIgnoreCase(n.getNodeName()))
			{
				makeItem();
				parseTemplate(n, currentItem.item);
			}
		}
	}
	
	private void makeItem()
	{
		if (currentItem.item != null)
		{
			return;
		}
		if (currentItem.type instanceof L2ArmorType)
		{
			currentItem.item = new L2Armor((L2ArmorType) currentItem.type, currentItem.set);
		}
		else if (currentItem.type instanceof L2WeaponType)
		{
			currentItem.item = new L2Weapon((L2WeaponType) currentItem.type, currentItem.set);
		}
		else if (currentItem.type instanceof L2EtcItemType)
		{
			currentItem.item = new L2EtcItem((L2EtcItemType) currentItem.type, currentItem.set);
		}
		else
		{
			throw new Error("Unknown item type " + currentItem.type);
		}
	}
	
	/**
	 * @return
	 */
	public List<L2Item> getItemList()
	{
		return itemsInFile;
	}
}
