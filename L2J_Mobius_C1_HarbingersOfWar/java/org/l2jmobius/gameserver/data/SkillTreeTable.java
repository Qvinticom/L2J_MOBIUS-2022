/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.gameserver.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.SkillLearn;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;

public class SkillTreeTable
{
	private static Logger _log = Logger.getLogger(SkillTreeTable.class.getName());
	private static SkillTreeTable _instance;
	private final Map<Integer, List<SkillLearn>> _skillTrees = new HashMap<>();
	
	public static SkillTreeTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new SkillTreeTable();
		}
		return _instance;
	}
	
	private SkillTreeTable()
	{
		File skillData = new File("data/skilltrees/D_Fighter.csv");
		readFile(skillData, 53, -1);
		skillData = new File("data/skilltrees/DE_Fighter.csv");
		readFile(skillData, 31, -1);
		skillData = new File("data/skilltrees/DE_Mage.csv");
		readFile(skillData, 38, -1);
		skillData = new File("data/skilltrees/E_Fighter.csv");
		readFile(skillData, 18, -1);
		skillData = new File("data/skilltrees/E_Mage.csv");
		readFile(skillData, 25, -1);
		skillData = new File("data/skilltrees/H_Fighter.csv");
		readFile(skillData, 0, -1);
		skillData = new File("data/skilltrees/H_Mage.csv");
		readFile(skillData, 10, -1);
		skillData = new File("data/skilltrees/O_Fighter.csv");
		readFile(skillData, 44, -1);
		skillData = new File("data/skilltrees/O_Mage.csv");
		readFile(skillData, 49, -1);
		skillData = new File("data/skilltrees/H_Knight.csv");
		readFile(skillData, 4, 0);
		skillData = new File("data/skilltrees/H_Warrior.csv");
		readFile(skillData, 1, 0);
		skillData = new File("data/skilltrees/H_Rogue.csv");
		readFile(skillData, 7, 0);
		skillData = new File("data/skilltrees/H_Cleric.csv");
		readFile(skillData, 15, 10);
		skillData = new File("data/skilltrees/H_Wizard.csv");
		readFile(skillData, 11, 10);
		skillData = new File("data/skilltrees/E_Knight.csv");
		readFile(skillData, 19, 18);
		skillData = new File("data/skilltrees/E_Scout.csv");
		readFile(skillData, 22, 18);
		skillData = new File("data/skilltrees/E_Wizard.csv");
		readFile(skillData, 26, 25);
		skillData = new File("data/skilltrees/E_Oracle.csv");
		readFile(skillData, 29, 25);
		skillData = new File("data/skilltrees/DE_PaulusKnight.csv");
		readFile(skillData, 32, 31);
		skillData = new File("data/skilltrees/DE_Assassin.csv");
		readFile(skillData, 35, 31);
		skillData = new File("data/skilltrees/DE_DarkWizard.csv");
		readFile(skillData, 39, 38);
		skillData = new File("data/skilltrees/DE_ShillienOracle.csv");
		readFile(skillData, 42, 38);
		skillData = new File("data/skilltrees/O_Monk.csv");
		readFile(skillData, 47, 44);
		skillData = new File("data/skilltrees/O_Raider.csv");
		readFile(skillData, 45, 44);
		skillData = new File("data/skilltrees/O_Shaman.csv");
		readFile(skillData, 50, 49);
		skillData = new File("data/skilltrees/D_Artisan.csv");
		readFile(skillData, 56, 53);
		skillData = new File("data/skilltrees/D_Scavenger.csv");
		readFile(skillData, 54, 53);
		skillData = new File("data/skilltrees/H_DarkAvenger.csv");
		readFile(skillData, 6, 4);
		skillData = new File("data/skilltrees/H_Paladin.csv");
		readFile(skillData, 5, 4);
		skillData = new File("data/skilltrees/H_TreasureHunter.csv");
		readFile(skillData, 8, 7);
		skillData = new File("data/skilltrees/H_Hawkeye.csv");
		readFile(skillData, 9, 7);
		skillData = new File("data/skilltrees/H_Gladiator.csv");
		readFile(skillData, 2, 1);
		skillData = new File("data/skilltrees/H_Warlord.csv");
		readFile(skillData, 3, 1);
		skillData = new File("data/skilltrees/H_Sorceror.csv");
		readFile(skillData, 12, 11);
		skillData = new File("data/skilltrees/H_Necromancer.csv");
		readFile(skillData, 13, 11);
		skillData = new File("data/skilltrees/H_Warlock.csv");
		readFile(skillData, 14, 11);
		skillData = new File("data/skilltrees/H_Bishop.csv");
		readFile(skillData, 16, 15);
		skillData = new File("data/skilltrees/H_Prophet.csv");
		readFile(skillData, 17, 15);
		skillData = new File("data/skilltrees/E_TempleKnight.csv");
		readFile(skillData, 20, 19);
		skillData = new File("data/skilltrees/E_SwordSinger.csv");
		readFile(skillData, 21, 19);
		skillData = new File("data/skilltrees/E_SilverRanger.csv");
		readFile(skillData, 24, 22);
		skillData = new File("data/skilltrees/E_PlainsWalker.csv");
		readFile(skillData, 23, 22);
		skillData = new File("data/skilltrees/E_ElementalSummoner.csv");
		readFile(skillData, 28, 26);
		skillData = new File("data/skilltrees/E_SpellSinger.csv");
		readFile(skillData, 27, 26);
		skillData = new File("data/skilltrees/E_Elder.csv");
		readFile(skillData, 30, 29);
		skillData = new File("data/skilltrees/DE_ShillienKnight.csv");
		readFile(skillData, 33, 32);
		skillData = new File("data/skilltrees/DE_BladeDancer.csv");
		readFile(skillData, 34, 32);
		skillData = new File("data/skilltrees/DE_AbyssWalker.csv");
		readFile(skillData, 36, 35);
		skillData = new File("data/skilltrees/DE_PhantomRanger.csv");
		readFile(skillData, 37, 35);
		skillData = new File("data/skilltrees/DE_ShillienElder.csv");
		readFile(skillData, 43, 42);
		skillData = new File("data/skilltrees/DE_PhantomSummoner.csv");
		readFile(skillData, 41, 39);
		skillData = new File("data/skilltrees/DE_Spellhowler.csv");
		readFile(skillData, 40, 39);
		skillData = new File("data/skilltrees/D_Warsmith.csv");
		readFile(skillData, 57, 56);
		skillData = new File("data/skilltrees/D_BountyHunter.csv");
		readFile(skillData, 55, 54);
		skillData = new File("data/skilltrees/O_Destroyer.csv");
		readFile(skillData, 46, 45);
		skillData = new File("data/skilltrees/O_Tyrant.csv");
		readFile(skillData, 48, 47);
		skillData = new File("data/skilltrees/O_Overlord.csv");
		readFile(skillData, 51, 50);
		skillData = new File("data/skilltrees/O_Warcryer.csv");
		readFile(skillData, 52, 50);
	}
	
	private void readFile(File skillData, int classId, int parentClassId)
	{
		String line = null;
		try
		{
			LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(skillData)));
			List<SkillLearn> list = new ArrayList<>();
			if (parentClassId != -1)
			{
				List<SkillLearn> parentList = _skillTrees.get(parentClassId);
				list.addAll(parentList);
			}
			while ((line = lnr.readLine()) != null)
			{
				if (line.trim().isEmpty() || line.startsWith("#"))
				{
					continue;
				}
				SkillLearn skill = parseList(line);
				list.add(skill);
			}
			_skillTrees.put(classId, list);
			lnr.close();
			_log.config("Skill tree for class " + classId + " has " + list.size() + " skills.");
		}
		catch (FileNotFoundException e)
		{
			_log.warning("SkillTree file for classId " + classId + " is missing in data folder.");
		}
		catch (Exception e)
		{
			_log.warning("Error while creating skill tree for classId " + classId + "  " + line + " " + e);
		}
	}
	
	private SkillLearn parseList(String line)
	{
		StringTokenizer st = new StringTokenizer(line, ";");
		SkillLearn skill = new SkillLearn();
		skill.setId(Integer.parseInt(st.nextToken()));
		skill.setName(st.nextToken());
		skill.setLevel(Integer.parseInt(st.nextToken()));
		skill.setSpCost(Integer.parseInt(st.nextToken()));
		skill.setMinLevel(Integer.parseInt(st.nextToken()));
		return skill;
	}
	
	public Collection<SkillLearn> getAvailableSkills(PlayerInstance cha)
	{
		List<SkillLearn> result = new ArrayList<>();
		List<SkillLearn> skills = _skillTrees.get(cha.getClassId());
		if (skills == null)
		{
			_log.warning("Skilltree for class " + cha.getClassId() + " is not defined !");
			return Collections.emptyList();
		}
		
		// TODO: Remove toArray.
		Skill[] oldSkills = cha.getAllSkills().toArray(new Skill[cha.getAllSkills().size()]);
		for (int i = 0; i < skills.size(); ++i)
		{
			SkillLearn temp = skills.get(i);
			if (temp.getMinLevel() > cha.getLevel())
			{
				continue;
			}
			boolean knownSkill = false;
			for (int j = 0; (j < oldSkills.length) && !knownSkill; ++j)
			{
				if (oldSkills[j].getId() != temp.getId())
				{
					continue;
				}
				knownSkill = true;
				if (oldSkills[j].getLevel() != (temp.getLevel() - 1))
				{
					continue;
				}
				result.add(temp);
			}
			if (knownSkill || (temp.getLevel() != 1))
			{
				continue;
			}
			result.add(temp);
		}
		return result;
	}
}
