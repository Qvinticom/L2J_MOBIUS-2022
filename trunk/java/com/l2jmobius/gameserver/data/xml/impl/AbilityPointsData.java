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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jmobius.commons.util.IGameXmlReader;
import com.l2jmobius.gameserver.model.holders.RangeAbilityPointsHolder;

/**
 * @author UnAfraid
 */
public final class AbilityPointsData implements IGameXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(AbilityPointsData.class.getName());
	private final List<RangeAbilityPointsHolder> _points = new ArrayList<>();
	
	protected AbilityPointsData()
	{
		load();
	}
	
	@Override
	public synchronized void load()
	{
		_points.clear();
		parseDatapackFile("config/AbilityPoints.xml");
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _points.size() + " range fees.");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("points".equalsIgnoreCase(d.getNodeName()))
					{
						final NamedNodeMap attrs = d.getAttributes();
						final int from = parseInteger(attrs, "from");
						final int to = parseInteger(attrs, "to");
						final int costs = parseInteger(attrs, "costs");
						_points.add(new RangeAbilityPointsHolder(from, to, costs));
					}
				}
			}
		}
	}
	
	public RangeAbilityPointsHolder getHolder(int points)
	{
		for (RangeAbilityPointsHolder holder : _points)
		{
			if ((holder.getMin() <= points) && (holder.getMax() >= points))
			{
				return holder;
			}
		}
		return null;
	}
	
	public long getPrice(int points)
	{
		points++; // for next point
		final RangeAbilityPointsHolder holder = getHolder(points);
		if (holder == null)
		{
			final RangeAbilityPointsHolder prevHolder = getHolder(points - 1);
			if (prevHolder != null)
			{
				return prevHolder.getSP();
			}
			
			// No data found
			return points >= 13 ? 1_000_000_000 : points >= 9 ? 750_000_000 : points >= 5 ? 500_000_000 : 250_000_000;
		}
		return holder.getSP();
	}
	
	public static AbilityPointsData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final AbilityPointsData _instance = new AbilityPointsData();
	}
}
