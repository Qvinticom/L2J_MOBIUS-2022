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
package org.l2jmobius.gameserver.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.skill.SkillType;
import org.l2jmobius.gameserver.model.skill.conditions.Condition;

/**
 * @author mkizub
 */
public class DocumentSkill extends DocumentBase
{
	private DocumentSkillDataHolder _currentSkill;
	private final List<Skill> _skillsInFile = new ArrayList<>();
	
	private class DocumentSkillDataHolder
	{
		public DocumentSkillDataHolder()
		{
		}
		
		public int id;
		public String name;
		public StatSet[] sets;
		public StatSet[] enchsets1;
		public StatSet[] enchsets2;
		public int currentLevel;
		public List<Skill> skills = new ArrayList<>();
		public List<Skill> currentSkills = new ArrayList<>();
	}
	
	public DocumentSkill(File file)
	{
		super(file);
	}
	
	private void setCurrentSkill(DocumentSkillDataHolder skill)
	{
		_currentSkill = skill;
	}
	
	@Override
	protected StatSet getStatSet()
	{
		return _currentSkill.sets[_currentSkill.currentLevel];
	}
	
	@Override
	protected String getTableValue(String name)
	{
		try
		{
			return _tables.get(name)[_currentSkill.currentLevel];
		}
		catch (RuntimeException e)
		{
			LOGGER.warning("Error in table: " + name + " of Skill Id " + _currentSkill.id + " " + e);
			return "";
		}
	}
	
	@Override
	protected String getTableValue(String name, int idx)
	{
		try
		{
			return _tables.get(name)[idx - 1];
		}
		catch (RuntimeException e)
		{
			LOGGER.warning("wrong level count in skill Id " + _currentSkill.id + " " + e);
			return "";
		}
	}
	
	@Override
	protected void parseDocument(Document doc)
	{
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("skill".equalsIgnoreCase(d.getNodeName()))
					{
						setCurrentSkill(new DocumentSkillDataHolder());
						parseSkill(d);
						_skillsInFile.addAll(_currentSkill.skills);
						resetTable();
					}
				}
			}
			else if ("skill".equalsIgnoreCase(n.getNodeName()))
			{
				setCurrentSkill(new DocumentSkillDataHolder());
				parseSkill(n);
				_skillsInFile.addAll(_currentSkill.skills);
			}
		}
	}
	
	private void parseSkill(Node node)
	{
		Node n = node;
		final NamedNodeMap attrs = n.getAttributes();
		int enchantLevels1 = 0;
		int enchantLevels2 = 0;
		final int skillId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
		final String skillName = attrs.getNamedItem("name").getNodeValue();
		final String levels = attrs.getNamedItem("levels").getNodeValue();
		final int lastLvl = Integer.parseInt(levels);
		
		if (attrs.getNamedItem("enchantLevels1") != null)
		{
			enchantLevels1 = Integer.parseInt(attrs.getNamedItem("enchantLevels1").getNodeValue());
		}
		
		if (attrs.getNamedItem("enchantLevels2") != null)
		{
			enchantLevels2 = Integer.parseInt(attrs.getNamedItem("enchantLevels2").getNodeValue());
		}
		
		_currentSkill.id = skillId;
		_currentSkill.name = skillName;
		_currentSkill.sets = new StatSet[lastLvl];
		_currentSkill.enchsets1 = new StatSet[enchantLevels1];
		_currentSkill.enchsets2 = new StatSet[enchantLevels2];
		
		for (int i = 0; i < lastLvl; i++)
		{
			_currentSkill.sets[i] = new StatSet();
			_currentSkill.sets[i].set("skill_id", _currentSkill.id);
			_currentSkill.sets[i].set("level", i + 1);
			_currentSkill.sets[i].set("name", _currentSkill.name);
		}
		
		if (_currentSkill.sets.length != lastLvl)
		{
			throw new RuntimeException("Skill id=" + skillId + " number of levels missmatch, " + lastLvl + " levels expected");
		}
		
		final Node first = n.getFirstChild();
		for (n = first; n != null; n = n.getNextSibling())
		{
			if ("table".equalsIgnoreCase(n.getNodeName()))
			{
				parseTable(n);
			}
		}
		
		for (int i = 1; i <= lastLvl; i++)
		{
			for (n = first; n != null; n = n.getNextSibling())
			{
				if ("set".equalsIgnoreCase(n.getNodeName()))
				{
					parseBeanSet(n, _currentSkill.sets[i - 1], i);
				}
			}
		}
		
		for (int i = 0; i < enchantLevels1; i++)
		{
			_currentSkill.enchsets1[i] = new StatSet();
			_currentSkill.enchsets1[i].set("skill_id", _currentSkill.id);
			_currentSkill.enchsets1[i].set("level", i + 101);
			_currentSkill.enchsets1[i].set("name", _currentSkill.name);
			
			for (n = first; n != null; n = n.getNextSibling())
			{
				if ("set".equalsIgnoreCase(n.getNodeName()))
				{
					parseBeanSet(n, _currentSkill.enchsets1[i], _currentSkill.sets.length);
				}
			}
			
			for (n = first; n != null; n = n.getNextSibling())
			{
				if ("enchant1".equalsIgnoreCase(n.getNodeName()))
				{
					parseBeanSet(n, _currentSkill.enchsets1[i], i + 1);
				}
			}
		}
		
		if (_currentSkill.enchsets1.length != enchantLevels1)
		{
			throw new RuntimeException("Skill id=" + skillId + " number of levels missmatch, " + enchantLevels1 + " levels expected");
		}
		
		for (int i = 0; i < enchantLevels2; i++)
		{
			_currentSkill.enchsets2[i] = new StatSet();
			_currentSkill.enchsets2[i].set("skill_id", _currentSkill.id);
			_currentSkill.enchsets2[i].set("level", i + 141);
			_currentSkill.enchsets2[i].set("name", _currentSkill.name);
			
			for (n = first; n != null; n = n.getNextSibling())
			{
				if ("set".equalsIgnoreCase(n.getNodeName()))
				{
					parseBeanSet(n, _currentSkill.enchsets2[i], _currentSkill.sets.length);
				}
			}
			
			for (n = first; n != null; n = n.getNextSibling())
			{
				if ("enchant2".equalsIgnoreCase(n.getNodeName()))
				{
					parseBeanSet(n, _currentSkill.enchsets2[i], i + 1);
				}
			}
		}
		
		if (_currentSkill.enchsets2.length != enchantLevels2)
		{
			throw new RuntimeException("Skill id=" + skillId + " number of levels missmatch, " + enchantLevels2 + " levels expected");
		}
		
		makeSkills();
		for (int i = 0; i < lastLvl; i++)
		{
			_currentSkill.currentLevel = i;
			for (n = first; n != null; n = n.getNextSibling())
			{
				if ("cond".equalsIgnoreCase(n.getNodeName()))
				{
					final Condition condition = parseCondition(n.getFirstChild(), _currentSkill.currentSkills.get(i));
					final Node msg = n.getAttributes().getNamedItem("msg");
					if ((condition != null) && (msg != null))
					{
						condition.setMessage(msg.getNodeValue());
					}
					_currentSkill.currentSkills.get(i).attach(condition, false);
				}
				
				if ("for".equalsIgnoreCase(n.getNodeName()))
				{
					parseTemplate(n, _currentSkill.currentSkills.get(i));
				}
			}
		}
		for (int i = lastLvl; i < (lastLvl + enchantLevels1); i++)
		{
			_currentSkill.currentLevel = i - lastLvl;
			boolean found = false;
			for (n = first; n != null; n = n.getNextSibling())
			{
				if ("enchant1cond".equalsIgnoreCase(n.getNodeName()))
				{
					found = true;
					final Condition condition = parseCondition(n.getFirstChild(), _currentSkill.currentSkills.get(i));
					final Node msg = n.getAttributes().getNamedItem("msg");
					if ((condition != null) && (msg != null))
					{
						condition.setMessage(msg.getNodeValue());
					}
					_currentSkill.currentSkills.get(i).attach(condition, false);
				}
				
				if ("enchant1for".equalsIgnoreCase(n.getNodeName()))
				{
					found = true;
					parseTemplate(n, _currentSkill.currentSkills.get(i));
				}
			}
			
			// If none found, the enchanted skill will take effects from maxLvL of norm skill
			if (!found)
			{
				_currentSkill.currentLevel = lastLvl - 1;
				for (n = first; n != null; n = n.getNextSibling())
				{
					if ("cond".equalsIgnoreCase(n.getNodeName()))
					{
						final Condition condition = parseCondition(n.getFirstChild(), _currentSkill.currentSkills.get(i));
						final Node msg = n.getAttributes().getNamedItem("msg");
						if ((condition != null) && (msg != null))
						{
							condition.setMessage(msg.getNodeValue());
						}
						_currentSkill.currentSkills.get(i).attach(condition, false);
					}
					
					if ("for".equalsIgnoreCase(n.getNodeName()))
					{
						parseTemplate(n, _currentSkill.currentSkills.get(i));
					}
				}
			}
		}
		
		for (int i = lastLvl + enchantLevels1; i < (lastLvl + enchantLevels1 + enchantLevels2); i++)
		{
			boolean found = false;
			_currentSkill.currentLevel = i - lastLvl - enchantLevels1;
			for (n = first; n != null; n = n.getNextSibling())
			{
				if ("enchant2cond".equalsIgnoreCase(n.getNodeName()))
				{
					found = true;
					final Condition condition = parseCondition(n.getFirstChild(), _currentSkill.currentSkills.get(i));
					final Node msg = n.getAttributes().getNamedItem("msg");
					if ((condition != null) && (msg != null))
					{
						condition.setMessage(msg.getNodeValue());
					}
					_currentSkill.currentSkills.get(i).attach(condition, false);
				}
				
				if ("enchant2for".equalsIgnoreCase(n.getNodeName()))
				{
					found = true;
					parseTemplate(n, _currentSkill.currentSkills.get(i));
				}
			}
			
			// If none found, the enchanted skill will take effects from maxLvL of norm skill
			if (!found)
			{
				_currentSkill.currentLevel = lastLvl - 1;
				for (n = first; n != null; n = n.getNextSibling())
				{
					if ("cond".equalsIgnoreCase(n.getNodeName()))
					{
						final Condition condition = parseCondition(n.getFirstChild(), _currentSkill.currentSkills.get(i));
						final Node msg = n.getAttributes().getNamedItem("msg");
						if ((condition != null) && (msg != null))
						{
							condition.setMessage(msg.getNodeValue());
						}
						_currentSkill.currentSkills.get(i).attach(condition, false);
					}
					
					if ("for".equalsIgnoreCase(n.getNodeName()))
					{
						parseTemplate(n, _currentSkill.currentSkills.get(i));
					}
				}
			}
		}
		_currentSkill.skills.addAll(_currentSkill.currentSkills);
	}
	
	private void makeSkills()
	{
		int count = 0;
		_currentSkill.currentSkills = new ArrayList<>(_currentSkill.sets.length + _currentSkill.enchsets1.length + _currentSkill.enchsets2.length);
		
		for (int i = 0; i < _currentSkill.sets.length; i++)
		{
			try
			{
				_currentSkill.currentSkills.add(i, _currentSkill.sets[i].getEnum("skillType", SkillType.class).makeSkill(_currentSkill.sets[i]));
				count++;
			}
			catch (Exception e)
			{
				LOGGER.warning("Skill id=" + _currentSkill.sets[i].getEnum("skillType", SkillType.class).makeSkill(_currentSkill.sets[i]).getDisplayId() + "level" + _currentSkill.sets[i].getEnum("skillType", SkillType.class).makeSkill(_currentSkill.sets[i]).getLevel() + " " + e);
			}
		}
		
		int count2 = count;
		for (int i = 0; i < _currentSkill.enchsets1.length; i++)
		{
			try
			{
				_currentSkill.currentSkills.add(count2 + i, _currentSkill.enchsets1[i].getEnum("skillType", SkillType.class).makeSkill(_currentSkill.enchsets1[i]));
				count++;
			}
			catch (Exception e)
			{
				LOGGER.warning("Skill id=" + _currentSkill.enchsets1[i].getEnum("skillType", SkillType.class).makeSkill(_currentSkill.enchsets1[i]).getDisplayId() + " level=" + _currentSkill.enchsets1[i].getEnum("skillType", SkillType.class).makeSkill(_currentSkill.enchsets1[i]).getLevel() + " " + e);
			}
		}
		
		count2 = count;
		for (int i = 0; i < _currentSkill.enchsets2.length; i++)
		{
			try
			{
				_currentSkill.currentSkills.add(count2 + i, _currentSkill.enchsets2[i].getEnum("skillType", SkillType.class).makeSkill(_currentSkill.enchsets2[i]));
				count++;
			}
			catch (Exception e)
			{
				LOGGER.warning("Skill id=" + _currentSkill.enchsets2[i].getEnum("skillType", SkillType.class).makeSkill(_currentSkill.enchsets2[i]).getDisplayId() + " level=" + _currentSkill.enchsets2[i].getEnum("skillType", SkillType.class).makeSkill(_currentSkill.enchsets2[i]).getLevel() + " " + e);
			}
		}
	}
	
	public List<Skill> getSkills()
	{
		return _skillsInFile;
	}
}
