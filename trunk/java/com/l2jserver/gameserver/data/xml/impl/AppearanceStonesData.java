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

import java.util.HashMap;
import java.util.logging.Level;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jserver.gameserver.data.xml.IXmlReader;
import com.l2jserver.gameserver.model.entity.AppearanceStone;
import com.l2jserver.gameserver.model.entity.AppearanceStone.AppearanceItemType;
import com.l2jserver.gameserver.model.entity.AppearanceStone.StoneType;
import com.l2jserver.gameserver.model.items.type.CrystalType;

/**
 * @author Erlandys
 */
public final class AppearanceStonesData implements IXmlReader
{
	private final HashMap<Integer, AppearanceStone> _stones = new HashMap<>();
	
	protected AppearanceStonesData()
	{
		load();
	}
	
	@Override
	public synchronized void load()
	{
		_stones.clear();
		parseDatapackFile("data/AppearanceStones.xml");
		LOGGER.log(Level.INFO, getClass().getSimpleName() + ": Loaded: " + _stones.size() + " appearance stones.");
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
					if ("stone".equalsIgnoreCase(d.getNodeName()))
					{
						final NamedNodeMap attrs = d.getAttributes();
						int itemId = parseInteger(attrs, "id");
						String type = parseString(attrs, "type");
						String itemType = parseString(attrs, "itemType");
						String grade = parseString(attrs, "maxGrade");
						long price = parseLong(attrs, "price", 0l);
						int targetItem = parseInteger(attrs, "targetItem", 0);
						long timeForAppearance = parseLong(attrs, "time", 0l);
						CrystalType cType = CrystalType.valueOf(grade.toUpperCase());
						type = type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase();
						itemType = itemType.substring(0, 1).toUpperCase() + itemType.substring(1).toLowerCase();
						StoneType sType = StoneType.valueOf(type);
						AppearanceItemType iType = AppearanceItemType.valueOf(itemType);
						_stones.put(itemId, new AppearanceStone(itemId, sType, iType, cType.getId(), price, targetItem, timeForAppearance));
					}
				}
			}
		}
	}
	
	public AppearanceStone getStone(int itemId)
	{
		if (_stones.containsKey(itemId))
		{
			return _stones.get(itemId);
		}
		return null;
	}
	
	public static final AppearanceStonesData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final AppearanceStonesData _instance = new AppearanceStonesData();
	}
}
