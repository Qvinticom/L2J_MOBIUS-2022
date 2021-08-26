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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.l2jmobius.commons.util.IXmlReader;
import org.l2jmobius.gameserver.model.holders.SymbolSealHolder;
import org.l2jmobius.gameserver.model.skills.Skill;

/**
 * @author NviX
 */
public class SymbolSealData implements IXmlReader
{
	private final Map<Integer, List<SymbolSealHolder>> _data = new HashMap<>();
	
	protected SymbolSealData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseDatapackFile("data/SymbolSealData.xml");
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
					if ("class".equalsIgnoreCase(d.getNodeName()))
					{
						final int classId = parseInteger(d.getAttributes(), "id");
						if (!_data.containsKey(classId))
						{
							_data.put(classId, new ArrayList<>());
						}
						for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
						{
							if ("symbol".equalsIgnoreCase(cd.getNodeName()))
							{
								final int symbolId = parseInteger(cd.getAttributes(), "id");
								final int skillId = parseInteger(cd.getAttributes(), "skillId");
								_data.get(classId).add(new SymbolSealHolder(symbolId, SkillData.getInstance().getSkill(skillId, 1)));
							}
						}
					}
				}
			}
		}
	}
	
	public Skill getSkill(int classId, int symbolId)
	{
		return _data.get(classId).get(symbolId).getSkill();
	}
	
	public static SymbolSealData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final SymbolSealData INSTANCE = new SymbolSealData();
	}
}
