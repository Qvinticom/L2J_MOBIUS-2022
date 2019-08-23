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
package org.l2jmobius.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.datatables.SkillTable;
import org.l2jmobius.gameserver.model.DropCategory;
import org.l2jmobius.gameserver.model.DropData;
import org.l2jmobius.gameserver.model.MinionData;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.StatsSet;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.base.ClassId;
import org.l2jmobius.gameserver.skills.BaseStats;
import org.l2jmobius.gameserver.skills.Stats;

/**
 * @version $Revision: 1.8.2.6.2.9 $ $Date: 2005/04/06 16:13:25 $
 */
public class NpcTable
{
	private static final Logger LOGGER = Logger.getLogger(NpcTable.class.getName());
	
	private static NpcTable _instance;
	
	private final Map<Integer, NpcTemplate> npcs;
	private boolean _initialized = false;
	
	public static NpcTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new NpcTable();
		}
		
		return _instance;
	}
	
	private NpcTable()
	{
		npcs = new HashMap<>();
		
		restoreNpcData();
	}
	
	private void restoreNpcData()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM npc");
			final ResultSet npcdata = statement.executeQuery();
			fillNpcTable(npcdata, false);
			npcdata.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("NPCTable: Error creating NPC table. " + e);
		}
		
		if (Config.CUSTOM_NPC_TABLE)
		{
			try (Connection con = DatabaseFactory.getConnection())
			{
				PreparedStatement statement = con.prepareStatement("SELECT * FROM custom_npc");
				final ResultSet npcdata = statement.executeQuery();
				fillNpcTable(npcdata, true);
				npcdata.close();
				statement.close();
			}
			catch (Exception e)
			{
				LOGGER.warning("NPCTable: Error creating custom NPC table." + e);
			}
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT npcid, skillid, level FROM npcskills");
			final ResultSet npcskills = statement.executeQuery();
			NpcTemplate npcDat = null;
			Skill npcSkill = null;
			
			while (npcskills.next())
			{
				final int mobId = npcskills.getInt("npcid");
				npcDat = npcs.get(mobId);
				
				if (npcDat == null)
				{
					continue;
				}
				
				final int skillId = npcskills.getInt("skillid");
				final int level = npcskills.getInt("level");
				
				if ((npcDat.race == null) && (skillId == 4416))
				{
					npcDat.setRace(level);
					continue;
				}
				
				npcSkill = SkillTable.getInstance().getInfo(skillId, level);
				
				if (npcSkill == null)
				{
					continue;
				}
				
				npcDat.addSkill(npcSkill);
			}
			
			npcskills.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("NPCTable: Error reading NPC skills table." + e);
		}
		
		if (Config.CUSTOM_DROPLIST_TABLE)
		{
			try (Connection con = DatabaseFactory.getConnection())
			{
				PreparedStatement statement = con.prepareStatement("SELECT * FROM custom_droplist ORDER BY mobId, chance DESC");
				final ResultSet dropData = statement.executeQuery();
				
				int cCount = 0;
				
				while (dropData.next())
				{
					final int mobId = dropData.getInt("mobId");
					
					final NpcTemplate npcDat = npcs.get(mobId);
					
					if (npcDat == null)
					{
						LOGGER.warning("NPCTable: CUSTOM DROPLIST No npc correlating with id: " + mobId);
						continue;
					}
					
					final DropData dropDat = new DropData();
					dropDat.setItemId(dropData.getInt("itemId"));
					dropDat.setMinDrop(dropData.getInt("min"));
					dropDat.setMaxDrop(dropData.getInt("max"));
					dropDat.setChance(dropData.getInt("chance"));
					
					final int category = dropData.getInt("category");
					
					npcDat.addDropData(dropDat, category);
					cCount++;
				}
				dropData.close();
				statement.close();
				LOGGER.info("CustomDropList : Added " + cCount + " custom droplist");
			}
			catch (Exception e)
			{
				LOGGER.warning("NPCTable: Error reading NPC CUSTOM drop data." + e);
			}
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM droplist ORDER BY mobId, chance DESC");
			final ResultSet dropData = statement.executeQuery();
			DropData dropDat = null;
			NpcTemplate npcDat = null;
			
			while (dropData.next())
			{
				final int mobId = dropData.getInt("mobId");
				
				npcDat = npcs.get(mobId);
				
				if (npcDat == null)
				{
					LOGGER.info("NPCTable: No npc correlating with id: " + mobId);
					continue;
				}
				
				dropDat = new DropData();
				
				dropDat.setItemId(dropData.getInt("itemId"));
				dropDat.setMinDrop(dropData.getInt("min"));
				dropDat.setMaxDrop(dropData.getInt("max"));
				dropDat.setChance(dropData.getInt("chance"));
				
				final int category = dropData.getInt("category");
				
				npcDat.addDropData(dropDat, category);
			}
			
			dropData.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("NPCTable: Error reading NPC drop data." + e);
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM skill_learn");
			final ResultSet learndata = statement.executeQuery();
			
			while (learndata.next())
			{
				final int npcId = learndata.getInt("npc_id");
				final int classId = learndata.getInt("class_id");
				
				final NpcTemplate npc = getTemplate(npcId);
				if (npc == null)
				{
					LOGGER.warning("NPCTable: Error getting NPC template ID " + npcId + " while trying to load skill trainer data.");
					continue;
				}
				
				if (classId >= ClassId.values().length)
				{
					LOGGER.warning("NPCTable: Error defining learning data for NPC " + npcId + ": specified classId " + classId + " is higher then max one " + (ClassId.values().length - 1) + " specified into ClassID Enum --> check your Database to be complient with it");
					continue;
				}
				
				npc.addTeachInfo(ClassId.values()[classId]);
			}
			
			learndata.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("NPCTable: Error reading NPC trainer data." + e);
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM minions");
			final ResultSet minionData = statement.executeQuery();
			MinionData minionDat = null;
			NpcTemplate npcDat = null;
			int cnt = 0;
			
			while (minionData.next())
			{
				final int raidId = minionData.getInt("boss_id");
				
				npcDat = npcs.get(raidId);
				minionDat = new MinionData();
				minionDat.setMinionId(minionData.getInt("minion_id"));
				minionDat.setAmountMin(minionData.getInt("amount_min"));
				minionDat.setAmountMax(minionData.getInt("amount_max"));
				npcDat.addRaidData(minionDat);
				cnt++;
			}
			
			minionData.close();
			statement.close();
			LOGGER.info("NpcTable: Loaded " + cnt + " Minions.");
		}
		catch (Exception e)
		{
			LOGGER.info("Error loading minion data");
			e.printStackTrace();
		}
		
		_initialized = true;
	}
	
	private void fillNpcTable(ResultSet NpcData, boolean custom) throws Exception
	{
		while (NpcData.next())
		{
			final StatsSet npcDat = new StatsSet();
			
			final int id = NpcData.getInt("id");
			
			npcDat.set("npcId", id);
			npcDat.set("idTemplate", NpcData.getInt("idTemplate"));
			
			// Level: for special bosses could be different
			int level = 0;
			float diff = 0; // difference between setted value and retail one
			boolean minion = false;
			
			switch (id)
			{
				case 29002: // and minions
				case 29003:
				case 29004:
				case 29005:
				{
					minion = true;
				}
				case 29001: // queenAnt
				{
					if (Config.QA_LEVEL > 0)
					{
						diff = Config.QA_LEVEL - NpcData.getInt("level");
						level = Config.QA_LEVEL;
					}
					else
					{
						level = NpcData.getInt("level");
					}
					break;
				}
				case 29022: // zaken
				{
					if (Config.ZAKEN_LEVEL > 0)
					{
						diff = Config.ZAKEN_LEVEL - NpcData.getInt("level");
						level = Config.ZAKEN_LEVEL;
					}
					else
					{
						level = NpcData.getInt("level");
					}
					break;
				}
				case 29015: // and minions
				case 29016:
				case 29017:
				case 29018:
				{
					minion = true;
				}
				case 29014: // orfen
				{
					if (Config.ORFEN_LEVEL > 0)
					{
						diff = Config.ORFEN_LEVEL - NpcData.getInt("level");
						level = Config.ORFEN_LEVEL;
					}
					else
					{
						level = NpcData.getInt("level");
					}
					break;
				}
				case 29007: // and minions
				case 29008:
				case 290011:
				{
					minion = true;
				}
				case 29006: // core
				{
					if (Config.CORE_LEVEL > 0)
					{
						diff = Config.CORE_LEVEL - NpcData.getInt("level");
						level = Config.CORE_LEVEL;
					}
					else
					{
						level = NpcData.getInt("level");
					}
					break;
				}
				default:
				{
					level = NpcData.getInt("level");
				}
			}
			
			npcDat.set("level", level);
			npcDat.set("jClass", NpcData.getString("class"));
			
			npcDat.set("baseShldDef", 0);
			npcDat.set("baseShldRate", 0);
			npcDat.set("baseCritRate", 4);
			
			npcDat.set("name", NpcData.getString("name"));
			npcDat.set("serverSideName", NpcData.getBoolean("serverSideName"));
			npcDat.set("title", NpcData.getString("title"));
			npcDat.set("serverSideTitle", NpcData.getBoolean("serverSideTitle"));
			npcDat.set("collision_radius", NpcData.getDouble("collision_radius"));
			npcDat.set("collision_height", NpcData.getDouble("collision_height"));
			npcDat.set("sex", NpcData.getString("sex"));
			npcDat.set("type", NpcData.getString("type"));
			npcDat.set("baseAtkRange", NpcData.getInt("attackrange"));
			
			// BOSS POWER CHANGES
			double multi_value = 1;
			
			if (diff >= 15) // means that there is level customization
			{
				multi_value = multi_value * (diff / 10);
			}
			else if ((diff > 0) && (diff < 15))
			{
				multi_value = multi_value + (diff / 10);
			}
			
			if (minion)
			{
				multi_value = multi_value * Config.LEVEL_DIFF_MULTIPLIER_MINION;
			}
			else
			{
				switch (id)
				{
					case 29001: // queenAnt
					{
						if (Config.QA_POWER_MULTIPLIER > 0)
						{
							multi_value = multi_value * Config.QA_POWER_MULTIPLIER;
						}
						break;
					}
					case 29022: // zaken
					{
						if (Config.ZAKEN_POWER_MULTIPLIER > 0)
						{
							multi_value = multi_value * Config.ZAKEN_POWER_MULTIPLIER;
						}
						break;
					}
					case 29014: // orfen
					{
						if (Config.ORFEN_POWER_MULTIPLIER > 0)
						{
							multi_value = multi_value * Config.ORFEN_POWER_MULTIPLIER;
						}
						break;
					}
					case 29006: // core
					{
						if (Config.CORE_POWER_MULTIPLIER > 0)
						{
							multi_value = multi_value * Config.CORE_POWER_MULTIPLIER;
						}
						break;
					}
					case 29019: // antharas
					{
						if (Config.ANTHARAS_POWER_MULTIPLIER > 0)
						{
							multi_value = multi_value * Config.ANTHARAS_POWER_MULTIPLIER;
						}
						break;
					}
					case 29028: // valakas
					{
						if (Config.VALAKAS_POWER_MULTIPLIER > 0)
						{
							multi_value = multi_value * Config.VALAKAS_POWER_MULTIPLIER;
						}
						break;
					}
					case 29020: // baium
					{
						if (Config.BAIUM_POWER_MULTIPLIER > 0)
						{
							multi_value = multi_value * Config.BAIUM_POWER_MULTIPLIER;
						}
						break;
					}
					case 29045: // frintezza
					{
						if (Config.FRINTEZZA_POWER_MULTIPLIER > 0)
						{
							multi_value = multi_value * Config.FRINTEZZA_POWER_MULTIPLIER;
						}
						break;
					}
				}
			}
			
			npcDat.set("rewardExp", NpcData.getInt("exp") * multi_value);
			npcDat.set("rewardSp", NpcData.getInt("sp") * multi_value);
			npcDat.set("basePAtkSpd", NpcData.getInt("atkspd") * multi_value);
			npcDat.set("baseMAtkSpd", NpcData.getInt("matkspd") * multi_value);
			npcDat.set("baseHpMax", NpcData.getInt("hp") * multi_value);
			npcDat.set("baseMpMax", NpcData.getInt("mp") * multi_value);
			npcDat.set("baseHpReg", ((int) NpcData.getFloat("hpreg") * multi_value) > 0 ? NpcData.getFloat("hpreg") : 1.5 + ((level - 1) / 10.0));
			npcDat.set("baseMpReg", ((int) NpcData.getFloat("mpreg") * multi_value) > 0 ? NpcData.getFloat("mpreg") : 0.9 + ((0.3 * (level - 1)) / 10.0));
			npcDat.set("basePAtk", NpcData.getInt("patk") * multi_value);
			npcDat.set("basePDef", NpcData.getInt("pdef") * multi_value);
			npcDat.set("baseMAtk", NpcData.getInt("matk") * multi_value);
			npcDat.set("baseMDef", NpcData.getInt("mdef") * multi_value);
			
			npcDat.set("aggroRange", NpcData.getInt("aggro"));
			npcDat.set("rhand", NpcData.getInt("rhand"));
			npcDat.set("lhand", NpcData.getInt("lhand"));
			npcDat.set("armor", NpcData.getInt("armor"));
			npcDat.set("baseWalkSpd", NpcData.getInt("walkspd"));
			npcDat.set("baseRunSpd", NpcData.getInt("runspd"));
			
			// constants, until we have stats in DB
			npcDat.safeSet("baseSTR", NpcData.getInt("str"), 0, BaseStats.MAX_STAT_VALUE, "Loading npc template id: " + NpcData.getInt("idTemplate"));
			npcDat.safeSet("baseCON", NpcData.getInt("con"), 0, BaseStats.MAX_STAT_VALUE, "Loading npc template id: " + NpcData.getInt("idTemplate"));
			npcDat.safeSet("baseDEX", NpcData.getInt("dex"), 0, BaseStats.MAX_STAT_VALUE, "Loading npc template id: " + NpcData.getInt("idTemplate"));
			npcDat.safeSet("baseINT", NpcData.getInt("int"), 0, BaseStats.MAX_STAT_VALUE, "Loading npc template id: " + NpcData.getInt("idTemplate"));
			npcDat.safeSet("baseWIT", NpcData.getInt("wit"), 0, BaseStats.MAX_STAT_VALUE, "Loading npc template id: " + NpcData.getInt("idTemplate"));
			npcDat.safeSet("baseMEN", NpcData.getInt("men"), 0, BaseStats.MAX_STAT_VALUE, "Loading npc template id: " + NpcData.getInt("idTemplate"));
			
			npcDat.set("baseCpMax", 0);
			
			npcDat.set("factionId", NpcData.getString("faction_id"));
			npcDat.set("factionRange", NpcData.getInt("faction_range"));
			
			npcDat.set("isUndead", NpcData.getString("isUndead"));
			
			npcDat.set("absorb_level", NpcData.getString("absorb_level"));
			npcDat.set("absorb_type", NpcData.getString("absorb_type"));
			
			final NpcTemplate template = new NpcTemplate(npcDat, custom);
			template.addVulnerability(Stats.BOW_WPN_VULN, 1);
			template.addVulnerability(Stats.BLUNT_WPN_VULN, 1);
			template.addVulnerability(Stats.DAGGER_WPN_VULN, 1);
			
			npcs.put(id, template);
		}
		
		LOGGER.info("NpcTable: Loaded " + npcs.size() + " Npc Templates.");
	}
	
	public void reloadNpc(int id)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			// save a copy of the old data
			final NpcTemplate old = getTemplate(id);
			final Map<Integer, Skill> skills = new HashMap<>();
			
			skills.putAll(old.getSkills());
			
			final List<DropCategory> categories = new ArrayList<>();
			
			if (old.getDropData() != null)
			{
				categories.addAll(old.getDropData());
			}
			final ClassId[] classIds = old.getTeachInfo().clone();
			
			final List<MinionData> minions = new ArrayList<>();
			
			if (old.getMinionData() != null)
			{
				minions.addAll(old.getMinionData());
			}
			
			if (old.isCustom())
			{
				final PreparedStatement st = con.prepareStatement("SELECT * FROM custom_npc WHERE id=?");
				st.setInt(1, id);
				final ResultSet rs = st.executeQuery();
				fillNpcTable(rs, true);
				rs.close();
				st.close();
			}
			else
			{
				final PreparedStatement st = con.prepareStatement("SELECT * FROM npc WHERE id=?");
				st.setInt(1, id);
				final ResultSet rs = st.executeQuery();
				fillNpcTable(rs, false);
				rs.close();
				st.close();
			}
			
			// restore additional data from saved copy
			final NpcTemplate created = getTemplate(id);
			
			for (Skill skill : skills.values())
			{
				created.addSkill(skill);
			}
			
			for (ClassId classId : classIds)
			{
				created.addTeachInfo(classId);
			}
			
			for (MinionData minion : minions)
			{
				created.addRaidData(minion);
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("NPCTable: Could not reload data for NPC  " + id + " " + e);
		}
	}
	
	public void reloadAllNpc()
	{
		restoreNpcData();
	}
	
	public void saveNpc(StatsSet npc)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			final Map<String, Object> set = npc.getSet();
			
			String name = "";
			String values = "";
			
			final NpcTemplate old = getTemplate(npc.getInt("npcId"));
			
			for (Object obj : set.keySet())
			{
				name = (String) obj;
				
				if (!name.equalsIgnoreCase("npcId"))
				{
					if (values != "")
					{
						values += ", ";
					}
					
					values += name + " = '" + set.get(name) + "'";
				}
			}
			
			PreparedStatement statement = null;
			if (old.isCustom())
			{
				statement = con.prepareStatement("UPDATE custom_npc SET " + values + " WHERE id = ?");
			}
			else
			{
				statement = con.prepareStatement("UPDATE npc SET " + values + " WHERE id = ?");
			}
			statement.setInt(1, npc.getInt("npcId"));
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("NPCTable: Could not store new NPC data in database. " + e);
		}
	}
	
	public boolean isInitialized()
	{
		return _initialized;
	}
	
	public void replaceTemplate(NpcTemplate npc)
	{
		npcs.put(npc.npcId, npc);
	}
	
	public NpcTemplate getTemplate(int id)
	{
		return npcs.get(id);
	}
	
	public NpcTemplate getTemplateByName(String name)
	{
		for (NpcTemplate npcTemplate : npcs.values())
		{
			if (npcTemplate.name.equalsIgnoreCase(name))
			{
				return npcTemplate;
			}
		}
		
		return null;
	}
	
	public NpcTemplate[] getAllOfLevel(int lvl)
	{
		final List<NpcTemplate> list = new ArrayList<>();
		
		for (NpcTemplate t : npcs.values())
		{
			if (t.level == lvl)
			{
				list.add(t);
			}
		}
		
		return list.toArray(new NpcTemplate[list.size()]);
	}
	
	public NpcTemplate[] getAllMonstersOfLevel(int lvl)
	{
		final List<NpcTemplate> list = new ArrayList<>();
		
		for (NpcTemplate t : npcs.values())
		{
			if ((t.level == lvl) && "Monster".equals(t.type))
			{
				list.add(t);
			}
		}
		
		return list.toArray(new NpcTemplate[list.size()]);
	}
	
	public NpcTemplate[] getAllNpcStartingWith(String letter)
	{
		final List<NpcTemplate> list = new ArrayList<>();
		
		for (NpcTemplate t : npcs.values())
		{
			if (t.name.startsWith(letter) && "Npc".equals(t.type))
			{
				list.add(t);
			}
		}
		
		return list.toArray(new NpcTemplate[list.size()]);
	}
	
	/**
	 * @param classType
	 * @return
	 */
	public Set<Integer> getAllNpcOfClassType(String classType)
	{
		return null;
	}
	
	/**
	 * @param clazz
	 * @return
	 */
	public Set<Integer> getAllNpcOfL2jClass(Class<?> clazz)
	{
		return null;
	}
	
	/**
	 * @param aiType
	 * @return
	 */
	public Set<Integer> getAllNpcOfAiType(String aiType)
	{
		return null;
	}
	
	public Map<Integer, NpcTemplate> getAllTemplates()
	{
		return npcs;
	}
}
