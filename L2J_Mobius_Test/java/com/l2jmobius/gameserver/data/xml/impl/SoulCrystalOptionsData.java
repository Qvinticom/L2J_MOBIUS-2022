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

import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jmobius.gameserver.model.holders.SkillHolder;
import com.l2jmobius.gameserver.network.clientpackets.ensoul.SoulCrystalOption;
import com.l2jmobius.util.data.xml.IXmlReader;

/**
 * @author Mathael
 */
public class SoulCrystalOptionsData implements IXmlReader
{
	private static final HashMap<Integer, SoulCrystalOption> _soulCrystalOptions = new HashMap<>();
	
	protected SoulCrystalOptionsData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_soulCrystalOptions.clear();
		
		parseDatapackFile("SoulCrystalOptions.xml");
		
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _soulCrystalOptions.size() + " Soul Crystal Options.");
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		int skillId;
		int level;
		int effectId;
		int type;
		
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("SoulCrystalOptions".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("option".equalsIgnoreCase(d.getNodeName()))
					{
						final NamedNodeMap attrs = d.getAttributes();
						
						skillId = parseInteger(attrs, "skillId", 0);
						level = parseInteger(attrs, "level", 0);
						effectId = parseInteger(attrs, "effectId", 0); // unique
						type = parseInteger(attrs, "type", 1);
						
						if (effectId == 0)
						{
							LOGGER.severe(getClass().getSimpleName() + ": Bad Soul Crystal Option [" + effectId + "] !");
							return;
						}
						
						// Somes options need to be confirmed.
						if (skillId != 0)
						{
							_soulCrystalOptions.put(effectId, new SoulCrystalOption(effectId, type == 2, new SkillHolder(skillId, level)));
						}
					}
				}
			}
		}
	}
	
	public SoulCrystalOption getByEffectId(int effectId)
	{
		return _soulCrystalOptions.get(effectId);
	}
	
	public static SoulCrystalOptionsData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SoulCrystalOptionsData _instance = new SoulCrystalOptionsData();
	}
}
