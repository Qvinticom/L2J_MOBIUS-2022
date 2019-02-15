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

import com.l2jmobius.commons.util.IGameXmlReader;
import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.holders.ClanSpecialtyHolder;
import com.l2jmobius.gameserver.model.skills.Skill;

/**
 * @author Mobius
 */
public class ClanSpecialtyData implements IGameXmlReader
{
	private static Logger LOGGER = Logger.getLogger(ClanSpecialtyData.class.getName());
	
	private final List<ClanSpecialtyHolder> _clanSpecialtyData = new ArrayList<>();
	
	protected ClanSpecialtyData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_clanSpecialtyData.clear();
		
		parseDatapackFile("data/ClanSpecialtyData.xml");
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _clanSpecialtyData.size() + " clan specialties.");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "clan", clanNode ->
		{
			final StatsSet set = new StatsSet(parseAttributes(clanNode));
			final int id = set.getInt("specialty");
			final int skillId = set.getInt("skilId");
			final int skillLevel = set.getInt("skillLevel");
			final int clanLevel = set.getInt("clanLevel");
			final int previousSpecialty = set.getInt("previousSpecialty");
			final int previousSpecialtyAlt = set.getInt("previousSpecialtyAlt");
			
			final Skill skill = SkillData.getInstance().getSkill(skillId, skillLevel);
			if (skill == null)
			{
				LOGGER.info(getClass().getSimpleName() + ": Could not create clan specialty, skill id " + skillId + " with level " + skillLevel + " does not exist.");
			}
			else
			{
				_clanSpecialtyData.add(new ClanSpecialtyHolder(id, skill, clanLevel, previousSpecialty, previousSpecialtyAlt));
			}
		}));
	}
	
	public List<ClanSpecialtyHolder> getSpecialties()
	{
		return _clanSpecialtyData;
	}
	
	public static ClanSpecialtyData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ClanSpecialtyData _instance = new ClanSpecialtyData();
	}
}
