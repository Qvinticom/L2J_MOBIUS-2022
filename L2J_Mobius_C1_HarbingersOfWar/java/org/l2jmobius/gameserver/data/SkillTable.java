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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.model.Skill;

public class SkillTable
{
	private static Logger _log = Logger.getLogger(SkillTable.class.getName());
	private static SkillTable _instance;
	private final Map<Integer, Skill> _skills = new HashMap<>();
	private boolean _initialized = true;
	
	public static SkillTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new SkillTable();
		}
		return _instance;
	}
	
	private SkillTable()
	{
		try
		{
			File skillData = new File("data/skills.csv");
			LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(skillData)));
			String line = null;
			while ((line = lnr.readLine()) != null)
			{
				if (line.trim().isEmpty() || line.startsWith("#"))
				{
					continue;
				}
				final Skill skill = parseList(line);
				_skills.put((skill.getId() * 100) + skill.getLevel(), skill);
			}
			skillData = new File("data/skills2.csv");
			lnr.close();
			lnr = new LineNumberReader(new BufferedReader(new FileReader(skillData)));
			while ((line = lnr.readLine()) != null)
			{
				if (line.trim().isEmpty() || line.startsWith("#"))
				{
					continue;
				}
				parseList2(line);
			}
			lnr.close();
			_log.config("Loaded " + _skills.size() + " skills.");
		}
		catch (FileNotFoundException e)
		{
			_initialized = false;
			_log.warning("Skills.csv or skills2.csv is missing in data folder: " + e.toString());
		}
		catch (Exception e)
		{
			_initialized = false;
			_log.warning("Error while creating skill table: " + e.toString());
		}
	}
	
	public boolean isInitialized()
	{
		return _initialized;
	}
	
	private void parseList2(String line)
	{
		final StringTokenizer st = new StringTokenizer(line, ";");
		final int id = Integer.parseInt(st.nextToken());
		st.nextToken();
		final int level = Integer.parseInt(st.nextToken());
		final int key = (id * 100) + level;
		final Skill skill = _skills.get(key);
		if (skill == null)
		{
			return;
		}
		final String target = st.nextToken();
		if (target.equalsIgnoreCase("self"))
		{
			skill.setTargetType(Skill.TARGET_SELF);
		}
		else if (target.equalsIgnoreCase("one"))
		{
			skill.setTargetType(Skill.TARGET_ONE);
		}
		else if (target.equalsIgnoreCase("party"))
		{
			skill.setTargetType(Skill.TARGET_PARTY);
		}
		else if (target.equalsIgnoreCase("clan"))
		{
			skill.setTargetType(Skill.TARGET_CLAN);
		}
		else if (target.equalsIgnoreCase("pet"))
		{
			skill.setTargetType(Skill.TARGET_PET);
		}
		skill.setPower(Integer.parseInt(st.nextToken()));
	}
	
	private Skill parseList(String line)
	{
		final StringTokenizer st = new StringTokenizer(line, ";");
		final Skill skill = new Skill();
		skill.setId(Integer.parseInt(st.nextToken()));
		skill.setName(st.nextToken());
		skill.setLevel(Integer.parseInt(st.nextToken()));
		final String opType = st.nextToken();
		if (opType.equalsIgnoreCase("once"))
		{
			skill.setOperateType(Skill.OP_ONCE);
		}
		else if (opType.equalsIgnoreCase("always"))
		{
			skill.setOperateType(Skill.OP_ALWAYS);
		}
		else if (opType.equalsIgnoreCase("duration"))
		{
			skill.setOperateType(Skill.OP_DURATION);
		}
		else if (opType.equalsIgnoreCase("toggle"))
		{
			skill.setOperateType(Skill.OP_TOGGLE);
		}
		skill.setMagic(Boolean.parseBoolean(st.nextToken())); // ?
		skill.setMpConsume(Integer.parseInt(st.nextToken()));
		skill.setHpConsume(Integer.parseInt(st.nextToken()));
		skill.setItemConsumeId(Integer.parseInt(st.nextToken()));
		skill.setItemConsume(Integer.parseInt(st.nextToken()));
		skill.setCastRange(Integer.parseInt(st.nextToken()));
		skill.setSkillTime(Integer.parseInt(st.nextToken()));
		skill.setReuseDelay(Integer.parseInt(st.nextToken()));
		skill.setBuffDuration(Integer.parseInt(st.nextToken()));
		skill.setHitTime(Integer.parseInt(st.nextToken()));
		return skill;
	}
	
	public Skill getInfo(int magicId, int level)
	{
		return _skills.get((magicId * 100) + level);
	}
}
