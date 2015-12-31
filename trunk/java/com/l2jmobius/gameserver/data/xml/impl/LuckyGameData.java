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
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jmobius.gameserver.model.holders.ItemHolder;
import com.l2jmobius.util.Rnd;
import com.l2jmobius.util.data.xml.IXmlReader;

/**
 * @author Mathael
 */
public class LuckyGameData implements IXmlReader
{
	private static final List<ItemHolder> _fortuneReadingTicketRewards = new ArrayList<>();
	private static final List<ItemHolder> _luxuryFortuneReadingTicketRewards = new ArrayList<>();
	private static final List<ItemHolder> _rareLuxuryFortuneReadingTicketRewards = new ArrayList<>();
	
	protected LuckyGameData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_fortuneReadingTicketRewards.clear();
		_luxuryFortuneReadingTicketRewards.clear();
		_rareLuxuryFortuneReadingTicketRewards.clear();
		
		parseDatapackFile("LuckyGameData.xml");
		
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _fortuneReadingTicketRewards.size() + " Normal item rewards.");
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _luxuryFortuneReadingTicketRewards.size() + " Luxury item rewards.");
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _rareLuxuryFortuneReadingTicketRewards.size() + " Rare item rewards.");
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				final NamedNodeMap at = n.getAttributes();
				final Node attribute = at.getNamedItem("enabled");
				if ((attribute != null) && Boolean.parseBoolean(attribute.getNodeValue())) // <list enabled="true"
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("fortuneReadingTicketRewards".equalsIgnoreCase(d.getNodeName()))
						{
							for (Node b = d.getFirstChild(); b != null; b = b.getNextSibling())
							{
								if ("item".equalsIgnoreCase(b.getNodeName()))
								{
									final NamedNodeMap attrs = b.getAttributes();
									
									final int itemId = parseInteger(attrs, "id");
									final int count = parseInteger(attrs, "count");
									
									if ((itemId == 0) || (count == 0))
									{
										LOGGER.severe(getClass().getSimpleName() + ": itemId: [" + itemId + "] count: [" + count + "] cannot be zero.");
										return;
									}
									
									_fortuneReadingTicketRewards.add(new ItemHolder(itemId, count));
								}
							}
						}
						else if ("luxuryFortuneReadingTicketRewards".equalsIgnoreCase(d.getNodeName()))
						{
							for (Node b = d.getFirstChild(); b != null; b = b.getNextSibling())
							{
								if ("item".equalsIgnoreCase(b.getNodeName()))
								{
									final NamedNodeMap attrs = b.getAttributes();
									
									final int itemId = parseInteger(attrs, "id");
									final int count = parseInteger(attrs, "count");
									
									if ((itemId == 0) || (count == 0))
									{
										LOGGER.severe(getClass().getSimpleName() + ": itemId: [" + itemId + "] count: [" + count + "] cannot be zero.");
										return;
									}
									
									_luxuryFortuneReadingTicketRewards.add(new ItemHolder(itemId, count));
								}
							}
						}
						else if ("rareLuxuryFortuneReadingTicketRewards".equalsIgnoreCase(d.getNodeName()))
						{
							for (Node b = d.getFirstChild(); b != null; b = b.getNextSibling())
							{
								if ("item".equalsIgnoreCase(b.getNodeName()))
								{
									final NamedNodeMap attrs = b.getAttributes();
									
									final int itemId = parseInteger(attrs, "id");
									final int count = parseInteger(attrs, "count");
									
									if ((itemId == 0) || (count == 0))
									{
										LOGGER.severe(getClass().getSimpleName() + ": itemId: [" + itemId + "] count: [" + count + "] cannot be zero.");
										return;
									}
									
									_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(itemId, count));
								}
							}
						}
					}
				}
			}
		}
	}
	
	public static ItemHolder getRandomNormalReward()
	{
		return _fortuneReadingTicketRewards.get(Rnd.get(_fortuneReadingTicketRewards.size()));
	}
	
	public static ItemHolder getRandomLuxuryReward()
	{
		return _luxuryFortuneReadingTicketRewards.get(Rnd.get(_luxuryFortuneReadingTicketRewards.size()));
	}
	
	public static ItemHolder getRandomRareReward()
	{
		return _rareLuxuryFortuneReadingTicketRewards.get(Rnd.get(_rareLuxuryFortuneReadingTicketRewards.size()));
	}
	
	public static LuckyGameData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final LuckyGameData _instance = new LuckyGameData();
	}
}
