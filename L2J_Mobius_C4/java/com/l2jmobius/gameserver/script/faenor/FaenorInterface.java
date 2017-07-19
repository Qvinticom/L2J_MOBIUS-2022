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
package com.l2jmobius.gameserver.script.faenor;

import java.util.List;
import java.util.Map;

import javax.script.ScriptContext;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.Announcements;
import com.l2jmobius.gameserver.EventDroplist;
import com.l2jmobius.gameserver.model.L2DropCategory;
import com.l2jmobius.gameserver.model.L2DropData;
import com.l2jmobius.gameserver.model.L2PetData;
import com.l2jmobius.gameserver.script.DateRange;
import com.l2jmobius.gameserver.script.EngineInterface;
import com.l2jmobius.gameserver.script.Expression;
import com.l2jmobius.gameserver.templates.L2NpcTemplate;

import javolution.util.FastList;

/**
 * @author Luis Arias TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
public class FaenorInterface implements EngineInterface
{
	private static FaenorInterface _instance;
	
	public static FaenorInterface getInstance()
	{
		if (_instance == null)
		{
			_instance = new FaenorInterface();
		}
		return _instance;
	}
	
	public FaenorInterface()
	{
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.script.EngineInterface#getAllPlayers()
	 */
	public List<?> getAllPlayers()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Adds a new Quest Drop to an NPC
	 */
	@Override
	public void addQuestDrop(int npcID, int itemID, int min, int max, int chance, String questID, String[] states)
	{
		final L2NpcTemplate npc = _npcTable.getTemplate(npcID);
		if (npc == null)
		{
			throw new NullPointerException();
		}
		final L2DropData drop = new L2DropData();
		drop.setItemId(itemID);
		drop.setMinDrop(min);
		drop.setMaxDrop(max);
		drop.setChance(chance);
		drop.setQuestID(questID);
		drop.addStates(states);
		addDrop(npc, drop, false);
	}
	
	/**
	 * Adds a new Drop to an NPC
	 * @param npcID
	 * @param itemID
	 * @param min
	 * @param max
	 * @param sweep
	 * @param chance
	 * @throws NullPointerException
	 */
	public void addDrop(int npcID, int itemID, int min, int max, boolean sweep, int chance) throws NullPointerException
	{
		final L2NpcTemplate npc = _npcTable.getTemplate(npcID);
		if (npc == null)
		{
			if (Config.DEBUG)
			{
				System.out.print("Npc doesnt Exist");
			}
			throw new NullPointerException();
		}
		final L2DropData drop = new L2DropData();
		drop.setItemId(itemID);
		drop.setMinDrop(min);
		drop.setMaxDrop(max);
		drop.setChance(chance);
		
		addDrop(npc, drop, sweep);
	}
	
	/**
	 * Adds a new drop to an NPC. If the drop is sweep, it adds it to the NPC's Sweep category If the drop is non-sweep, it creates a new category for this drop.
	 * @param npc
	 * @param drop
	 * @param sweep
	 */
	public void addDrop(L2NpcTemplate npc, L2DropData drop, boolean sweep)
	{
		if (sweep)
		{
			addDrop(npc, drop, -1);
		}
		else
		{
			int maxCategory = -1;
			
			if (npc.getDropData() != null)
			{
				for (final L2DropCategory cat : npc.getDropData())
				{
					if (maxCategory < cat.getCategoryType())
					{
						maxCategory = cat.getCategoryType();
					}
				}
			}
			maxCategory++;
			npc.addDropData(drop, maxCategory);
		}
		
	}
	
	/**
	 * Adds a new drop to an NPC, in the specified category. If the category does not exist, it is created.
	 * @param npc
	 * @param drop
	 * @param category
	 */
	public void addDrop(L2NpcTemplate npc, L2DropData drop, int category)
	{
		npc.addDropData(drop, category);
	}
	
	/**
	 * @param npcID
	 * @return Returns the _questDrops.
	 */
	public List<L2DropData> getQuestDrops(int npcID)
	{
		final L2NpcTemplate npc = _npcTable.getTemplate(npcID);
		if (npc == null)
		{
			return null;
		}
		
		final List<L2DropData> questDrops = new FastList<>();
		if (npc.getDropData() != null)
		{
			for (final L2DropCategory cat : npc.getDropData())
			{
				for (final L2DropData drop : cat.getAllDrops())
				{
					if (drop.getQuestID() != null)
					{
						questDrops.add(drop);
					}
				}
			}
		}
		return questDrops;
	}
	
	@Override
	public void addEventDrop(int[] items, int[] count, double chance, DateRange range)
	{
		EventDroplist.getInstance().addGlobalDrop(items, count, (int) (chance * L2DropData.MAX_CHANCE), range);
	}
	
	@Override
	public void onPlayerLogin(String[] message, DateRange validDateRange)
	{
		Announcements.getInstance().addEventAnnouncement(validDateRange, message);
	}
	
	public void addPetData(ScriptContext context, int petID, int levelStart, int levelEnd, Map<String, String> stats)
	{
		final L2PetData[] petData = new L2PetData[(levelEnd - levelStart) + 1];
		int value = 0;
		for (int level = levelStart; level <= levelEnd; level++)
		{
			petData[level - 1] = new L2PetData();
			petData[level - 1].setPetID(petID);
			petData[level - 1].setPetLevel(level);
			
			context.setAttribute("level", new Double(level), ScriptContext.ENGINE_SCOPE);
			for (final String stat : stats.keySet())
			{
				value = ((Number) Expression.eval(context, "beanshell", stats.get(stat))).intValue();
				petData[level - 1].setStat(stat, value);
			}
			context.removeAttribute("level", ScriptContext.ENGINE_SCOPE);
		}
	}
}