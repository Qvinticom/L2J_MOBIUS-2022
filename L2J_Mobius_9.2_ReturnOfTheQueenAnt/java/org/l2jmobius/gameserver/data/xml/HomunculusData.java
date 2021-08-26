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

import org.w3c.dom.Document;

import org.l2jmobius.commons.util.IXmlReader;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.homunculus.HomunculusTemplate;

/**
 * @author Mobius
 */
public class HomunculusData implements IXmlReader
{
	private final Map<Integer, HomunculusTemplate> _templates = new HashMap<>();
	
	protected HomunculusData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_templates.clear();
		parseDatapackFile("data/HomunculusData.xml");
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _templates.size() + " templates.");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "homunculus", homunculusNode ->
		{
			final StatSet set = new StatSet(parseAttributes(homunculusNode));
			final int id = set.getInt("id");
			_templates.put(id, new HomunculusTemplate(id, set.getInt("type"), set.getInt("basicSkillId"), set.getInt("basicSkillLevel"), set.getInt("skillId1"), set.getInt("skillId2"), set.getInt("skillId3"), set.getInt("skillId4"), set.getInt("skillId5"), set.getInt("hpLevel1"), set.getInt("atkLevel1"), set.getInt("defLevel1"), set.getInt("expToLevel2"), set.getInt("hpLevel2"), set.getInt("atkLevel2"), set.getInt("defLevel2"), set.getInt("expToLevel3"), set.getInt("hpLevel3"), set.getInt("atkLevel3"), set.getInt("defLevel3"), set.getInt("expToLevel4"), set.getInt("hpLevel4"), set.getInt("atkLevel4"), set.getInt("defLevel4"), set.getInt("expToLevel5"), set.getInt("hpLevel5"), set.getInt("atkLevel5"), set.getInt("defLevel5"), set.getInt("expToLevel6"), set.getInt("critRate")));
		}));
	}
	
	public HomunculusTemplate getTemplate(int id)
	{
		return _templates.get(id);
	}
	
	public int size()
	{
		return _templates.size();
	}
	
	public static HomunculusData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final HomunculusData INSTANCE = new HomunculusData();
	}
}
