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
package org.l2jmobius.gameserver.data.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.Document;

import org.l2jmobius.commons.util.IXmlReader;
import org.l2jmobius.gameserver.model.StatSet;

/**
 * Written by Berezkin Nikolay, on 13.04.2021
 */
public class SubjugationGacha implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(SubjugationGacha.class.getName());
	
	private static final Map<Integer, Map<Integer, Double>> _subjugations = new HashMap<>();
	
	public SubjugationGacha()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_subjugations.clear();
		parseDatapackFile("data/SubjugationGacha.xml");
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _subjugations.size() + " data.");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "purge", purgeNode ->
		{
			final StatSet set = new StatSet(parseAttributes(purgeNode));
			final int category = set.getInt("category");
			final Map<Integer, Double> items = new HashMap<>();
			forEach(purgeNode, "item", npcNode ->
			{
				final StatSet stats = new StatSet(parseAttributes(npcNode));
				final int itemId = stats.getInt("id");
				final double rate = stats.getDouble("rate");
				items.put(itemId, rate);
			});
			_subjugations.put(category, items);
		}));
	}
	
	public Map<Integer, Double> getSubjugation(int category)
	{
		return _subjugations.get(category);
	}
	
	public static SubjugationGacha getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final SubjugationGacha INSTANCE = new SubjugationGacha();
	}
}
