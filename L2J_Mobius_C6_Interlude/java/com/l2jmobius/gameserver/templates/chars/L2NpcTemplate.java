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
package com.l2jmobius.gameserver.templates.chars;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.l2jmobius.gameserver.model.L2DropCategory;
import com.l2jmobius.gameserver.model.L2DropData;
import com.l2jmobius.gameserver.model.L2MinionData;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.base.ClassId;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.skills.Stats;
import com.l2jmobius.gameserver.templates.StatsSet;

/**
 * This cl contains all generic data of a L2Spawn object.<BR>
 * <BR>
 * <B><U> Data</U> :</B><BR>
 * <BR>
 * <li>npcId, type, name, sex</li>
 * <li>rewardExp, rewardSp</li>
 * <li>aggroRange, factionId, factionRange</li>
 * <li>rhand, lhand, armor</li>
 * <li>isUndead</li>
 * <li>_drops</li>
 * <li>_minions</li>
 * <li>_teachInfo</li>
 * <li>_skills</li>
 * <li>_questsStart</li><BR>
 * <BR>
 * @version $Revision: 1.1.2.4 $ $Date: 2005/04/02 15:57:51 $
 */
public final class L2NpcTemplate extends L2CharTemplate
{
	protected static final Logger LOGGER = Logger.getLogger(Quest.class.getName());
	
	public final int npcId;
	public final int idTemplate;
	public final String type;
	public final String name;
	public final boolean serverSideName;
	public final String title;
	public final boolean serverSideTitle;
	public final String sex;
	public final byte level;
	public final int rewardExp;
	public final int rewardSp;
	public final int aggroRange;
	public final int rhand;
	public final int lhand;
	public final int armor;
	public final String factionId;
	public final int factionRange;
	public final int absorbLevel;
	public final AbsorbCrystalType absorbType;
	public Race race;
	
	private final boolean _custom;
	
	public enum AbsorbCrystalType
	{
		LAST_HIT,
		FULL_PARTY,
		PARTY_ONE_RANDOM
	}
	
	public enum Race
	{
		UNDEAD,
		MAGICCREATURE,
		BEAST,
		ANIMAL,
		PLANT,
		HUMANOID,
		SPIRIT,
		ANGEL,
		DEMON,
		DRAGON,
		GIANT,
		BUG,
		FAIRIE,
		HUMAN,
		ELVE,
		DARKELVE,
		ORC,
		DWARVE,
		OTHER,
		NONLIVING,
		SIEGEWEAPON,
		DEFENDINGARMY,
		MERCENARIE,
		UNKNOWN
	}
	
	private final StatsSet _npcStatsSet;
	
	/** The table containing all Item that can be dropped by L2NpcInstance using this L2NpcTemplate */
	private final List<L2DropCategory> _categories = new ArrayList<>();
	
	/** The table containing all Minions that must be spawn with the L2NpcInstance using this L2NpcTemplate */
	private final List<L2MinionData> _minions = new ArrayList<>();
	
	private final List<ClassId> _teachInfo = new ArrayList<>();
	private final Map<Integer, L2Skill> _skills = new HashMap<>();
	private final Map<Stats, Double> _vulnerabilities = new HashMap<>();
	// contains a list of quests for each event type (questStart, questAttack, questKill, etc)
	private final Map<Quest.QuestEventType, Quest[]> _questEvents = new HashMap<>();
	
	/**
	 * Constructor of L2Character.<BR>
	 * <BR>
	 * @param set The StatsSet object to transfer data to the method
	 * @param custom
	 */
	public L2NpcTemplate(StatsSet set, boolean custom)
	{
		super(set);
		npcId = set.getInteger("npcId");
		idTemplate = set.getInteger("idTemplate");
		type = set.getString("type");
		name = set.getString("name");
		serverSideName = set.getBool("serverSideName");
		title = set.getString("title");
		serverSideTitle = set.getBool("serverSideTitle");
		sex = set.getString("sex");
		level = set.getByte("level");
		rewardExp = set.getInteger("rewardExp");
		rewardSp = set.getInteger("rewardSp");
		aggroRange = set.getInteger("aggroRange");
		rhand = set.getInteger("rhand");
		lhand = set.getInteger("lhand");
		armor = set.getInteger("armor");
		final String f = set.getString("factionId", null);
		if (f == null)
		{
			factionId = null;
		}
		else
		{
			factionId = f.intern();
		}
		factionRange = set.getInteger("factionRange", 0);
		absorbLevel = set.getInteger("absorb_level", 0);
		absorbType = AbsorbCrystalType.valueOf(set.getString("absorb_type"));
		_npcStatsSet = set;
		_custom = custom;
	}
	
	public void addTeachInfo(ClassId classId)
	{
		_teachInfo.add(classId);
	}
	
	public ClassId[] getTeachInfo()
	{
		return _teachInfo.toArray(new ClassId[_teachInfo.size()]);
	}
	
	public boolean canTeach(ClassId classId)
	{
		// If the player is on a third class, fetch the class teacher
		// information for its parent class.
		if (classId.getId() >= 88)
		{
			return _teachInfo.contains(classId.getParent());
		}
		
		return _teachInfo.contains(classId);
	}
	
	// add a drop to a given category. If the category does not exist, create it.
	public void addDropData(L2DropData drop, int categoryType)
	{
		if (drop.isQuestDrop())
		{
			// if (_questDrops == null)
			// _questDrops = new ArrayList<L2DropData>(0);
			// _questDrops.add(drop);
		}
		else
		{
			// if the category doesn't already exist, create it first
			// synchronized (_categories)
			// {
			boolean catExists = false;
			for (L2DropCategory cat : _categories)
			{
				// if the category exists, add the drop to this category.
				if (cat.getCategoryType() == categoryType)
				{
					cat.addDropData(drop, type.equalsIgnoreCase("L2RaidBoss") || type.equalsIgnoreCase("L2GrandBoss"));
					catExists = true;
					break;
				}
			}
			// if the category doesn't exit, create it and add the drop
			if (!catExists)
			{
				final L2DropCategory cat = new L2DropCategory(categoryType);
				cat.addDropData(drop, type.equalsIgnoreCase("L2RaidBoss") || type.equalsIgnoreCase("L2GrandBoss"));
				_categories.add(cat);
			}
			// }
		}
	}
	
	public void addRaidData(L2MinionData minion)
	{
		_minions.add(minion);
	}
	
	public void addSkill(L2Skill skill)
	{
		_skills.put(skill.getId(), skill);
	}
	
	public void addVulnerability(Stats id, double vuln)
	{
		_vulnerabilities.put(id, new Double(vuln));
	}
	
	public double getVulnerability(Stats id)
	{
		if (_vulnerabilities.get(id) == null)
		{
			return 1;
		}
		
		return _vulnerabilities.get(id);
	}
	
	public double removeVulnerability(Stats id)
	{
		return _vulnerabilities.remove(id);
	}
	
	/**
	 * Return the list of all possible UNCATEGORIZED drops of this L2NpcTemplate.<BR>
	 * <BR>
	 * @return
	 */
	public List<L2DropCategory> getDropData()
	{
		return _categories;
	}
	
	/**
	 * Return the list of all possible item drops of this L2NpcTemplate.<BR>
	 * (ie full drops and part drops, mats, miscellaneous & UNCATEGORIZED)<BR>
	 * <BR>
	 * @return
	 */
	public List<L2DropData> getAllDropData()
	{
		final List<L2DropData> lst = new ArrayList<>();
		for (L2DropCategory tmp : _categories)
		{
			lst.addAll(tmp.getAllDrops());
		}
		return lst;
	}
	
	/**
	 * Empty all possible drops of this L2NpcTemplate.<BR>
	 * <BR>
	 */
	public synchronized void clearAllDropData()
	{
		while (_categories.size() > 0)
		{
			_categories.get(0).clearAllDrops();
			_categories.remove(0);
		}
		_categories.clear();
	}
	
	/**
	 * Return the list of all Minions that must be spawn with the L2NpcInstance using this L2NpcTemplate.<BR>
	 * <BR>
	 * @return
	 */
	public List<L2MinionData> getMinionData()
	{
		return _minions;
	}
	
	public Map<Integer, L2Skill> getSkills()
	{
		return _skills;
	}
	
	public void addQuestEvent(Quest.QuestEventType EventType, Quest q)
	{
		if (_questEvents.get(EventType) == null)
		{
			_questEvents.put(EventType, new Quest[]
			{
				q
			});
		}
		else
		{
			final Quest[] _quests = _questEvents.get(EventType);
			final int len = _quests.length;
			
			// if only one registration per npc is allowed for this event type
			// then only register this NPC if not already registered for the specified event.
			// if a quest allows multiple registrations, then register regardless of count
			// In all cases, check if this new registration is replacing an older copy of the SAME quest
			if (!EventType.isMultipleRegistrationAllowed())
			{
				if (_quests[0].getName().equals(q.getName()))
				{
					_quests[0] = q;
				}
				else
				{
					LOGGER.warning("Quest event not allowed in multiple quests.  Skipped addition of Event Type \"" + EventType + "\" for NPC \"" + name + "\" and quest \"" + q.getName() + "\".");
				}
			}
			else
			{
				// be ready to add a new quest to a new copy of the list, with larger size than previously.
				final Quest[] tmp = new Quest[len + 1];
				// loop through the existing quests and copy them to the new list. While doing so, also
				// check if this new quest happens to be just a replacement for a previously loaded quest.
				// If so, just save the updated reference and do NOT use the new list. Else, add the new
				// quest to the end of the new list
				for (int i = 0; i < len; i++)
				{
					if (_quests[i].getName().equals(q.getName()))
					{
						_quests[i] = q;
						return;
					}
					tmp[i] = _quests[i];
				}
				tmp[len] = q;
				_questEvents.put(EventType, tmp);
			}
		}
	}
	
	public Quest[] getEventQuests(Quest.QuestEventType EventType)
	{
		if (_questEvents.get(EventType) == null)
		{
			return new Quest[0];
		}
		
		return _questEvents.get(EventType);
	}
	
	public StatsSet getStatsSet()
	{
		return _npcStatsSet;
	}
	
	public void setRace(int raceId)
	{
		switch (raceId)
		{
			case 1:
			{
				race = Race.UNDEAD;
				break;
			}
			case 2:
			{
				race = Race.MAGICCREATURE;
				break;
			}
			case 3:
			{
				race = Race.BEAST;
				break;
			}
			case 4:
			{
				race = Race.ANIMAL;
				break;
			}
			case 5:
			{
				race = Race.PLANT;
				break;
			}
			case 6:
			{
				race = Race.HUMANOID;
				break;
			}
			case 7:
			{
				race = Race.SPIRIT;
				break;
			}
			case 8:
			{
				race = Race.ANGEL;
				break;
			}
			case 9:
			{
				race = Race.DEMON;
				break;
			}
			case 10:
			{
				race = Race.DRAGON;
				break;
			}
			case 11:
			{
				race = Race.GIANT;
				break;
			}
			case 12:
			{
				race = Race.BUG;
				break;
			}
			case 13:
			{
				race = Race.FAIRIE;
				break;
			}
			case 14:
			{
				race = Race.HUMAN;
				break;
			}
			case 15:
			{
				race = Race.ELVE;
				break;
			}
			case 16:
			{
				race = Race.DARKELVE;
				break;
			}
			case 17:
			{
				race = Race.ORC;
				break;
			}
			case 18:
			{
				race = Race.DWARVE;
				break;
			}
			case 19:
			{
				race = Race.OTHER;
				break;
			}
			case 20:
			{
				race = Race.NONLIVING;
				break;
			}
			case 21:
			{
				race = Race.SIEGEWEAPON;
				break;
			}
			case 22:
			{
				race = Race.DEFENDINGARMY;
				break;
			}
			case 23:
			{
				race = Race.MERCENARIE;
				break;
			}
			default:
			{
				race = Race.UNKNOWN;
				break;
			}
		}
	}
	
	public Race getRace()
	{
		if (race == null)
		{
			race = Race.UNKNOWN;
		}
		
		return race;
	}
	
	/**
	 * @return the level
	 */
	public byte getLevel()
	{
		return level;
	}
	
	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * @return the npcId
	 */
	public int getNpcId()
	{
		return npcId;
	}
	
	public final boolean isCustom()
	{
		return _custom;
	}
	
}
