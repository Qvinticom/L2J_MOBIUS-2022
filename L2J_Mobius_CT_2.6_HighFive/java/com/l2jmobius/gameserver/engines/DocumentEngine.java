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
package com.l2jmobius.gameserver.engines;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.commons.concurrent.ThreadPool;
import com.l2jmobius.commons.util.file.filter.XMLFilter;
import com.l2jmobius.gameserver.data.xml.impl.SkillData;
import com.l2jmobius.gameserver.engines.items.DocumentItem;
import com.l2jmobius.gameserver.engines.skills.DocumentSkill;
import com.l2jmobius.gameserver.model.items.Item;
import com.l2jmobius.gameserver.model.skills.Skill;

/**
 * @author mkizub
 */
public class DocumentEngine
{
	private static final Logger LOGGER = Logger.getLogger(DocumentEngine.class.getName());
	
	private final List<File> _itemFiles = new ArrayList<>();
	private final List<File> _skillFiles = new ArrayList<>();
	private static int count = 0;
	
	protected DocumentEngine()
	{
		processDirectory("data/stats/items", _itemFiles);
		if (Config.CUSTOM_ITEMS_LOAD)
		{
			processDirectory("data/stats/items/custom", _itemFiles);
		}
		processDirectory("data/stats/skills", _skillFiles);
		if (Config.CUSTOM_SKILLS_LOAD)
		{
			processDirectory("data/stats/skills/custom", _skillFiles);
		}
	}
	
	private void processDirectory(String dirName, List<File> list)
	{
		final File dir = new File(Config.DATAPACK_ROOT, dirName);
		if (!dir.exists())
		{
			LOGGER.warning("Dir " + dir.getAbsolutePath() + " does not exist.");
			return;
		}
		final File[] files = dir.listFiles(new XMLFilter());
		for (File file : files)
		{
			list.add(file);
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
		final List<ScheduledFuture<?>> jobs = new CopyOnWriteArrayList<>();
		for (File file : _skillFiles)
		{
			jobs.add(ThreadPool.schedule(() ->
			{
				final List<Skill> skills = loadSkills(file);
				if (skills == null)
				{
					return;
				}
				for (Skill skill : skills)
				{
					allSkills.put(SkillData.getSkillHashCode(skill), skill);
					count++;
				}
			}, 0));
		}
		while (!jobs.isEmpty())
		{
			for (ScheduledFuture<?> job : jobs)
			{
				if ((job == null) || job.isDone() || job.isCancelled())
				{
					jobs.remove(job);
				}
			}
		}
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + count + " Skill templates from XML files.");
	}
	
	/**
	 * Return created items
	 * @return List of {@link Item}
	 */
	public List<Item> loadItems()
	{
		final List<ScheduledFuture<?>> jobs = new CopyOnWriteArrayList<>();
		final List<Item> list = new CopyOnWriteArrayList<>();
		for (File file : _itemFiles)
		{
			jobs.add(ThreadPool.schedule(() ->
			{
				final DocumentItem document = new DocumentItem(file);
				document.parse();
				list.addAll(document.getItemList());
			}, 0));
		}
		while (!jobs.isEmpty())
		{
			for (ScheduledFuture<?> job : jobs)
			{
				if ((job == null) || job.isDone() || job.isCancelled())
				{
					jobs.remove(job);
				}
			}
		}
		return list;
	}
	
	private static class SingletonHolder
	{
		protected static final DocumentEngine _instance = new DocumentEngine();
	}
	
	public static DocumentEngine getInstance()
	{
		return SingletonHolder._instance;
	}
}
