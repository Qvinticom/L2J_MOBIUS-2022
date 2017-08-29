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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jmobius.commons.util.IGameXmlReader;
import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.teleporter.TeleportHolder;
import com.l2jmobius.gameserver.model.teleporter.TeleportLocation;
import com.l2jmobius.gameserver.model.teleporter.TeleportType;

/**
 * @author UnAfraid
 */
public class TeleportersData implements IGameXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(TeleportersData.class.getName());
	
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
		LOGGER.info(getClass().getSimpleName() + ": Loaded: " + _teleporters.size() + " npc teleporters.");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
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
							else if ("npcs".equals(tpNode.getNodeName()))
							{
								for (Node locNode = tpNode.getFirstChild(); locNode != null; locNode = locNode.getNextSibling())
								{
									if ("npc".equals(locNode.getNodeName()))
									{
										final int npcId = parseInteger(locNode.getAttributes(), "id");
										if (_teleporters.putIfAbsent(npcId, holder) != null)
										{
											LOGGER.warning(getClass().getSimpleName() + ": Duplicate location entires for npc: " + npcId);
										}
									}
								}
							}
						}
						
						if (_teleporters.putIfAbsent(id, holder) != null)
						{
							LOGGER.warning(getClass().getSimpleName() + ": Duplicate location entires for npc: " + id);
						}
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
