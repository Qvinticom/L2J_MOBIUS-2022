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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jmobius.gameserver.enums.CastleSide;
import com.l2jmobius.gameserver.model.holders.CastleSpawnHolder;
import com.l2jmobius.util.data.xml.IXmlReader;

/**
 * @author St3eT
 */
public final class CastleData implements IXmlReader
{
	private final Map<Integer, List<CastleSpawnHolder>> _castles = new HashMap<>();
	
	protected CastleData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_castles.clear();
		parseDatapackDirectory("/castles", true);
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		for (Node listNode = doc.getFirstChild(); listNode != null; listNode = listNode.getNextSibling())
		{
			if ("list".equals(listNode.getNodeName()))
			{
				for (Node castleNode = listNode.getFirstChild(); castleNode != null; castleNode = castleNode.getNextSibling())
				{
					if ("castle".equals(castleNode.getNodeName()))
					{
						final int castleId = parseInteger(castleNode.getAttributes(), "id");
						final List<CastleSpawnHolder> spawns = new ArrayList<>();
						
						for (Node tpNode = castleNode.getFirstChild(); tpNode != null; tpNode = tpNode.getNextSibling())
						{
							if ("spawn".equals(tpNode.getNodeName()))
							{
								final CastleSide side = parseEnum(tpNode.getAttributes(), CastleSide.class, "castleSide", CastleSide.NEUTRAL);
								for (Node npcNode = tpNode.getFirstChild(); npcNode != null; npcNode = npcNode.getNextSibling())
								{
									if ("npc".equals(npcNode.getNodeName()))
									{
										final NamedNodeMap np = npcNode.getAttributes();
										spawns.add(new CastleSpawnHolder(parseInteger(np, "id"), side, parseInteger(np, "x"), parseInteger(np, "y"), parseInteger(np, "z"), parseInteger(np, "heading")));
									}
								}
							}
						}
						_castles.put(castleId, spawns);
					}
				}
			}
		}
	}
	
	public final List<CastleSpawnHolder> getSpawnsForSide(int castleId, CastleSide side)
	{
		return _castles.getOrDefault(castleId, Collections.emptyList()).stream().filter(s -> s.getSide() == side).collect(Collectors.toList());
	}
	
	/**
	 * Gets the single instance of TeleportersData.
	 * @return single instance of TeleportersData
	 */
	public static CastleData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final CastleData _instance = new CastleData();
	}
}
