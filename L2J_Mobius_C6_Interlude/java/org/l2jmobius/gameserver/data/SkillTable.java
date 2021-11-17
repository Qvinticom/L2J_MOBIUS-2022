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
package org.l2jmobius.gameserver.data;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.item.type.WeaponType;
import org.l2jmobius.gameserver.util.DocumentSkill;

public class SkillTable
{
	protected static final Logger LOGGER = Logger.getLogger(SkillTable.class.getName());
	
	private static final WeaponType[] WEAPON_MASKS =
	{
		WeaponType.ETC,
		WeaponType.BOW,
		WeaponType.POLE,
		WeaponType.DUALFIST,
		WeaponType.DUAL,
		WeaponType.BLUNT,
		WeaponType.SWORD,
		WeaponType.DAGGER,
		WeaponType.BIGSWORD,
		WeaponType.ROD,
		WeaponType.BIGBLUNT
	};
	
	private final List<File> _skillFiles = new ArrayList<>();
	private final Map<Integer, Skill> _skills = new HashMap<>();
	private final boolean _initialized = true;
	
	protected SkillTable()
	{
		hashFiles("data/stats/skills", _skillFiles);
		reload();
	}
	
	private void hashFiles(String dirname, List<File> hash)
	{
		final File dir = new File(Config.DATAPACK_ROOT, dirname);
		if (!dir.exists())
		{
			LOGGER.info("Dir " + dir.getAbsolutePath() + " not exists");
			return;
		}
		
		final File[] files = dir.listFiles();
		for (File f : files)
		{
			if (f.getName().endsWith(".xml") && !f.getName().startsWith("custom"))
			{
				hash.add(f);
			}
		}
		
		final File customfile = new File(Config.DATAPACK_ROOT, dirname + "/custom.xml");
		if (customfile.exists())
		{
			hash.add(customfile);
		}
	}
	
	public List<Skill> loadSkills(File file)
	{
		if (file == null)
		{
			LOGGER.warning("Skill file not found.");
			return null;
		}
		
		final DocumentSkill doc = new DocumentSkill(file);
		doc.parse();
		return doc.getSkills();
	}
	
	public void loadAllSkills(Map<Integer, Skill> allSkills)
	{
		int count = 0;
		for (File file : _skillFiles)
		{
			final List<Skill> s = loadSkills(file);
			if (s == null)
			{
				continue;
			}
			
			for (Skill skill : s)
			{
				allSkills.put(SkillTable.getSkillHashCode(skill), skill);
				count++;
			}
		}
		LOGGER.info("SkillsEngine: Loaded " + count + " skill templates.");
	}
	
	public void reload()
	{
		_skills.clear();
		loadAllSkills(_skills);
	}
	
	public boolean isInitialized()
	{
		return _initialized;
	}
	
	/**
	 * Provides the skill hash
	 * @param skill The Skill to be hashed
	 * @return SkillTable.getSkillHashCode(skill.getId(), skill.getLevel())
	 */
	public static int getSkillHashCode(Skill skill)
	{
		return getSkillHashCode(skill.getId(), skill.getLevel());
	}
	
	/**
	 * Centralized method for easier change of the hashing sys
	 * @param skillId The Skill Id
	 * @param skillLevel The Skill Level
	 * @return The Skill hash number
	 */
	public static int getSkillHashCode(int skillId, int skillLevel)
	{
		return (skillId * 256) + skillLevel;
	}
	
	public Skill getSkill(int skillId, int level)
	{
		return _skills.get(getSkillHashCode(skillId, level));
	}
	
	public int getMaxLevel(int magicId, int level)
	{
		Skill temp;
		int result = level;
		while (result < 100)
		{
			result++;
			temp = _skills.get(getSkillHashCode(magicId, result));
			if (temp == null)
			{
				return result - 1;
			}
		}
		return result;
	}
	
	public int calcWeaponsAllowed(int mask)
	{
		if (mask == 0)
		{
			return 0;
		}
		
		int weaponsAllowed = 0;
		for (int i = 0; i < WEAPON_MASKS.length; i++)
		{
			if ((mask & (1 << i)) != 0)
			{
				weaponsAllowed |= WEAPON_MASKS[i].mask();
			}
		}
		
		return weaponsAllowed;
	}
	
	public static SkillTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final SkillTable INSTANCE = new SkillTable();
	}
}
