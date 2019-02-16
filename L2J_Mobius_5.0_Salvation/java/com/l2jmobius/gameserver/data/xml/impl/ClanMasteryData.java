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
import com.l2jmobius.gameserver.model.holders.ClanMasteryHolder;
import com.l2jmobius.gameserver.model.skills.Skill;

/**
 * @author Mobius
 */
public class ClanMasteryData implements IGameXmlReader
{
	private static Logger LOGGER = Logger.getLogger(ClanMasteryData.class.getName());
	
	private final List<ClanMasteryHolder> _clanMasteryData = new ArrayList<>();
	
	protected ClanMasteryData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_clanMasteryData.clear();
		
		parseDatapackFile("data/ClanMasteryData.xml");
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _clanMasteryData.size() + " clan masteries.");
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "clan", clanNode ->
		{
			final StatsSet set = new StatsSet(parseAttributes(clanNode));
			final int id = set.getInt("mastery");
			final int skillId = set.getInt("skilId");
			final int skillLevel = set.getInt("skillLevel");
			final int clanLevel = set.getInt("clanLevel");
			final int clanReputation = set.getInt("clanReputation");
			final int previousMastery = set.getInt("previousMastery", 0);
			final int previousMasteryAlt = set.getInt("previousMasteryAlt", 0);
			
			final Skill skill = SkillData.getInstance().getSkill(skillId, skillLevel);
			if (skill == null)
			{
				LOGGER.info(getClass().getSimpleName() + ": Could not create clan mastery, skill id " + skillId + " with level " + skillLevel + " does not exist.");
			}
			else
			{
				_clanMasteryData.add(new ClanMasteryHolder(id, skill, clanLevel, clanReputation, previousMastery, previousMasteryAlt));
			}
		}));
	}
	
	public List<ClanMasteryHolder> getMasteries()
	{
		return _clanMasteryData;
	}
	
	public static ClanMasteryData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ClanMasteryData _instance = new ClanMasteryData();
	}
}
