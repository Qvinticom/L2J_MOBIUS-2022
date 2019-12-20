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
package org.l2jmobius.gameserver.model.actor.templates;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.model.DropCategory;
import org.l2jmobius.gameserver.model.DropData;
import org.l2jmobius.gameserver.model.MinionData;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.StatsSet;
import org.l2jmobius.gameserver.model.base.ClassId;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.Quest.QuestEventType;
import org.l2jmobius.gameserver.model.skills.Stats;

/**
 * This cl contains all generic data of a Spawn object.<BR>
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
public class NpcTemplate extends CreatureTemplate
{
	protected static final Logger LOGGER = Logger.getLogger(NpcTemplate.class.getName());
	
	public int npcId;
	public int idTemplate;
	public String type;
	public String name;
	public boolean serverSideName;
	public String title;
	public boolean serverSideTitle;
	public String sex;
	public byte level;
	public int rewardExp;
	public int rewardSp;
	public int aggroRange;
	public int rhand;
	public int lhand;
	public int armor;
	public String factionId;
	public int factionRange;
	public int absorbLevel;
	public AbsorbCrystalType absorbType;
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
	
	/** The table containing all Item that can be dropped by NpcInstance using this NpcTemplate */
	private final List<DropCategory> _categories = new ArrayList<>();
	
	/** The table containing all Minions that must be spawn with the NpcInstance using this NpcTemplate */
	private final List<MinionData> _minions = new ArrayList<>();
	
	private final List<ClassId> _teachInfo = new ArrayList<>();
	private final Map<Integer, Skill> _skills = new HashMap<>();
	private final Map<Stats, Double> _vulnerabilities = new EnumMap<>(Stats.class);
	// contains a list of quests for each event type (questStart, questAttack, questKill, etc)
	private final Map<QuestEventType, Quest[]> _questEvents = new EnumMap<>(QuestEventType.class);
	
	/**
	 * Constructor of Creature.<BR>
	 * <BR>
	 * @param set The StatsSet object to transfer data to the method
	 * @param custom
	 */
	public NpcTemplate(StatsSet set, boolean custom)
	{
		super(set);
		npcId = set.getInt("npcId");
		idTemplate = set.getInt("idTemplate");
		type = set.getString("type");
		name = set.getString("name");
		serverSideName = set.getBoolean("serverSideName");
		title = set.getString("title");
		serverSideTitle = set.getBoolean("serverSideTitle");
		sex = set.getString("sex");
		level = set.getByte("level");
		rewardExp = set.getInt("rewardExp");
		rewardSp = set.getInt("rewardSp");
		aggroRange = set.getInt("aggroRange");
		rhand = set.getInt("rhand");
		lhand = set.getInt("lhand");
		armor = set.getInt("armor");
		final String f = set.getString("factionId", null);
		if (f == null)
		{
			factionId = null;
		}
		else
		{
			factionId = f.intern();
		}
		factionRange = set.getInt("factionRange", 0);
		absorbLevel = set.getInt("absorb_level", 0);
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
		// If the player is on a third class, fetch the class teacher information for its parent class.
		if (classId.getId() >= 88)
		{
			return _teachInfo.contains(classId.getParent());
		}
		
		return _teachInfo.contains(classId);
	}
	
	// add a drop to a given category. If the category does not exist, create it.
	public void addDropData(DropData drop, int categoryType)
	{
		if (!drop.isQuestDrop())
		{
			boolean catExists = false;
			for (DropCategory cat : _categories)
			{
				// if the category exists, add the drop to this category.
				if (cat.getCategoryType() == categoryType)
				{
					cat.addDropData(drop, type.equalsIgnoreCase("RaidBoss") || type.equalsIgnoreCase("GrandBoss"));
					catExists = true;
					break;
				}
			}
			// if the category doesn't exit, create it and add the drop
			if (!catExists)
			{
				final DropCategory cat = new DropCategory(categoryType);
				cat.addDropData(drop, type.equalsIgnoreCase("RaidBoss") || type.equalsIgnoreCase("GrandBoss"));
				_categories.add(cat);
			}
		}
	}
	
	public void addRaidData(MinionData minion)
	{
		_minions.add(minion);
	}
	
	public void addSkill(Skill skill)
	{
		_skills.put(skill.getId(), skill);
	}
	
	public void addVulnerability(Stats id, double vuln)
	{
		_vulnerabilities.put(id, vuln);
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
	 * Return the list of all possible UNCATEGORIZED drops of this NpcTemplate.<BR>
	 * <BR>
	 * @return
	 */
	public List<DropCategory> getDropData()
	{
		return _categories;
	}
	
	/**
	 * Return the list of all possible item drops of this NpcTemplate.<BR>
	 * (ie full drops and part drops, mats, miscellaneous & UNCATEGORIZED)<BR>
	 * <BR>
	 * @return
	 */
	public List<DropData> getAllDropData()
	{
		final List<DropData> lst = new ArrayList<>();
		for (DropCategory tmp : _categories)
		{
			lst.addAll(tmp.getAllDrops());
		}
		return lst;
	}
	
	/**
	 * Empty all possible drops of this NpcTemplate.
	 */
	public synchronized void clearAllDropData()
	{
		while (!_categories.isEmpty())
		{
			_categories.get(0).clearAllDrops();
			_categories.remove(0);
		}
		_categories.clear();
	}
	
	/**
	 * Return the list of all Minions that must be spawn with the NpcInstance using this NpcTemplate.
	 * @return
	 */
	public List<MinionData> getMinionData()
	{
		return _minions;
	}
	
	public Map<Integer, Skill> getSkills()
	{
		return _skills;
	}
	
	public void addQuestEvent(Quest.QuestEventType eventType, Quest q)
	{
		if (_questEvents.get(eventType) == null)
		{
			_questEvents.put(eventType, new Quest[]
			{
				q
			});
		}
		else
		{
			final Quest[] quests = _questEvents.get(eventType);
			final int len = quests.length;
			
			// If only one registration per npc is allowed for this event type then only register this NPC if not already registered for the specified event.
			// If a quest allows multiple registrations, then register regardless of count.
			// In all cases, check if this new registration is replacing an older copy of the SAME quest.
			if (!eventType.isMultipleRegistrationAllowed())
			{
				if (quests[0].getName().equals(q.getName()))
				{
					quests[0] = q;
				}
				else
				{
					LOGGER.warning("Quest event not allowed in multiple quests.  Skipped addition of Event Type \"" + eventType + "\" for NPC \"" + name + "\" and quest \"" + q.getName() + "\".");
				}
			}
			else
			{
				// Be ready to add a new quest to a new copy of the list, with larger size than previously.
				final Quest[] tmp = new Quest[len + 1];
				// Loop through the existing quests and copy them to the new list. While doing so, also check if this new quest happens to be just a replacement for a previously loaded quest.
				// If so, just save the updated reference and do NOT use the new list. Else, add the new quest to the end of the new list.
				for (int i = 0; i < len; i++)
				{
					if (quests[i].getName().equals(q.getName()))
					{
						quests[i] = q;
						return;
					}
					tmp[i] = quests[i];
				}
				tmp[len] = q;
				_questEvents.put(eventType, tmp);
			}
		}
	}
	
	public Quest[] getEventQuests(Quest.QuestEventType eventType)
	{
		if (_questEvents.get(eventType) == null)
		{
			return new Quest[0];
		}
		return _questEvents.get(eventType);
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
	
	public boolean isCustom()
	{
		return _custom;
	}
}
