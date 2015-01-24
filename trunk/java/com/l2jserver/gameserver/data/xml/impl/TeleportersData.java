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
import java.util.Map;
import java.util.logging.Level;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jserver.gameserver.data.xml.IXmlReader;
import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.teleporter.TeleportHolder;
import com.l2jserver.gameserver.model.teleporter.TeleportLocation;
import com.l2jserver.gameserver.model.teleporter.TeleportType;

/**
 * @author UnAfraid
 */
public class TeleportersData implements IXmlReader
{
	private final Map<Integer, TeleportHolder> _teleporters = new HashMap<>();
	
	protected TeleportersData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_teleporters.clear();
		parseDatapackDirectory("data/teleporters", true);
		LOGGER.log(Level.INFO, "Loaded: " + _teleporters.size() + " npc teleporters.");
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		for (Node listNode = doc.getFirstChild(); listNode != null; listNode = listNode.getNextSibling())
		{
			if ("list".equals(listNode.getNodeName()))
			{
				for (Node npcNode = listNode.getFirstChild(); npcNode != null; npcNode = npcNode.getNextSibling())
				{
					if ("npc".equals(npcNode.getNodeName()))
					{
						final int id = parseInteger(npcNode.getAttributes(), "id");
						final TeleportHolder holder = new TeleportHolder(id);
						for (Node tpNode = npcNode.getFirstChild(); tpNode != null; tpNode = tpNode.getNextSibling())
						{
							if ("teleport".equals(tpNode.getNodeName()))
							{
								final TeleportType type = parseEnum(tpNode.getAttributes(), TeleportType.class, "type", TeleportType.NORMAL);
								for (Node locNode = tpNode.getFirstChild(); locNode != null; locNode = locNode.getNextSibling())
								{
									if ("location".equals(locNode.getNodeName()))
									{
										final NamedNodeMap attrs = locNode.getAttributes();
										final int nextId = holder.getLocations(type).size() + 1;
										final StatsSet set = new StatsSet();
										for (int i = 0; i < attrs.getLength(); i++)
										{
											final Node locationNode = attrs.item(i);
											set.set(locationNode.getNodeName(), locationNode.getNodeValue());
										}
										holder.addLocation(type, new TeleportLocation(nextId, set));
									}
								}
							}
						}
						_teleporters.put(id, holder);
					}
				}
			}
		}
	}
	
	public TeleportHolder getHolder(int npcId)
	{
		return _teleporters.get(npcId);
	}
	
	/**
	 * Gets the single instance of TeleportersData.
	 * @return single instance of TeleportersData
	 */
	public static TeleportersData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final TeleportersData _instance = new TeleportersData();
	}
}
