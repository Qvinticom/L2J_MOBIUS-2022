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
package com.l2jmobius.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.L2DatabaseFactory;
import com.l2jmobius.gameserver.model.L2DropCategory;
import com.l2jmobius.gameserver.model.L2DropData;
import com.l2jmobius.gameserver.model.L2MinionData;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.base.ClassId;
import com.l2jmobius.gameserver.skills.Stats;
import com.l2jmobius.gameserver.templates.L2NpcTemplate;
import com.l2jmobius.gameserver.templates.StatsSet;

import javolution.util.FastList;
import javolution.util.FastMap;

/**
 * This class ...
 * @version $Revision: 1.8.2.6.2.9 $ $Date: 2005/04/06 16:13:25 $
 */
public class NpcTable
{
	private static Logger _log = Logger.getLogger(NpcTable.class.getName());
	
	private static NpcTable _instance;
	
	private final Map<Integer, L2NpcTemplate> _npcs;
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
		_npcs = new FastMap<>();
		restoreNpcData();
	}
	
	private void restoreNpcData()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[]
			{
				"id",
				"idTemplate",
				"name",
				"serverSideName",
				"title",
				"serverSideTitle",
				"class",
				"collision_radius",
				"collision_height",
				"level",
				"sex",
				"type",
				"attackrange",
				"hp",
				"mp",
				"hpreg",
				"mpreg",
				"str",
				"con",
				"dex",
				"int",
				"wit",
				"men",
				"exp",
				"sp",
				"patk",
				"pdef",
				"matk",
				"mdef",
				"atkspd",
				"aggro",
				"matkspd",
				"rhand",
				"lhand",
				"armor",
				"walkspd",
				"runspd",
				"faction_id",
				"faction_range",
				"isUndead",
				"absorb_level",
				"ss",
				"bss",
				"ss_rate",
				"AI"
			}) + " FROM npc");
			ResultSet npcdata = statement.executeQuery())
		{
			fillNpcTable(npcdata, false);
		}
		catch (final Exception e)
		{
			_log.severe("NPCTable: Error creating NPC table: " + e);
		}
		
		if (Config.CUSTOM_NPC_TABLE)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[]
				{
					"id",
					"idTemplate",
					"name",
					"serverSideName",
					"title",
					"serverSideTitle",
					"class",
					"collision_radius",
					"collision_height",
					"level",
					"sex",
					"type",
					"attackrange",
					"hp",
					"mp",
					"hpreg",
					"mpreg",
					"str",
					"con",
					"dex",
					"int",
					"wit",
					"men",
					"exp",
					"sp",
					"patk",
					"pdef",
					"matk",
					"mdef",
					"atkspd",
					"aggro",
					"matkspd",
					"rhand",
					"lhand",
					"armor",
					"walkspd",
					"runspd",
					"faction_id",
					"faction_range",
					"isUndead",
					"absorb_level",
					"ss",
					"bss",
					"ss_rate",
					"AI"
				}) + " FROM custom_npc");
				ResultSet npcdata = statement.executeQuery())
			{
				fillNpcTable(npcdata, true);
			}
			catch (final Exception e)
			{
				_log.severe("NPCTable: Error creating Custom NPC table: " + e);
			}
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT npcid, skillid, level FROM npcskills");
			ResultSet npcskills = statement.executeQuery())
		{
			L2NpcTemplate npcDat = null;
			L2Skill npcSkill = null;
			
			while (npcskills.next())
			{
				final int mobId = npcskills.getInt("npcid");
				npcDat = _npcs.get(mobId);
				
				if (npcDat == null)
				{
					continue;
				}
				
				final int skillId = npcskills.getInt("skillid");
				final int level = npcskills.getInt("level");
				
				if (npcDat.race == 0)
				{
					if ((skillId >= 4290) && (skillId <= 4302))
					{
						npcDat.setRace(skillId);
						continue;
					}
				}
				
				npcSkill = SkillTable.getInstance().getInfo(skillId, level);
				if (npcSkill == null)
				{
					continue;
				}
				
				npcDat.addSkill(npcSkill);
			}
		}
		catch (final Exception e)
		{
			_log.severe("NPCTable: Error reading NPC skills table: " + e);
		}
		
		if (Config.CUSTOM_NPC_SKILLS_TABLE)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT npcid, skillid, level FROM custom_npcskills");
				ResultSet npcskills = statement.executeQuery())
			{
				L2NpcTemplate npcDat = null;
				L2Skill npcSkill = null;
				
				while (npcskills.next())
				{
					final int mobId = npcskills.getInt("npcid");
					npcDat = _npcs.get(mobId);
					
					if (npcDat == null)
					{
						continue;
					}
					
					final int skillId = npcskills.getInt("skillid");
					final int level = npcskills.getInt("level");
					
					if (npcDat.race == 0)
					{
						if ((skillId >= 4290) && (skillId <= 4302))
						{
							npcDat.setRace(skillId);
							continue;
						}
					}
					
					npcSkill = SkillTable.getInstance().getInfo(skillId, level);
					if (npcSkill == null)
					{
						continue;
					}
					
					npcDat.addSkill(npcSkill);
				}
			}
			catch (final Exception e)
			{
				_log.severe("NPCTable: Error reading Custom NPC skills table: " + e);
			}
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement2 = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[]
			{
				"mobId",
				"itemId",
				"min",
				"max",
				"category",
				"chance"
			}) + " FROM droplist ORDER BY mobId, chance DESC");
			ResultSet dropData = statement2.executeQuery())
		{
			L2DropData dropDat = null;
			L2NpcTemplate npcDat = null;
			
			while (dropData.next())
			{
				final int mobId = dropData.getInt("mobId");
				npcDat = _npcs.get(mobId);
				if (npcDat == null)
				{
					_log.severe("NPCTable: No npc correlating with id : " + mobId);
					continue;
				}
				dropDat = new L2DropData();
				
				dropDat.setItemId(dropData.getInt("itemId"));
				dropDat.setMinDrop(dropData.getInt("min"));
				dropDat.setMaxDrop(dropData.getInt("max"));
				dropDat.setChance(dropData.getInt("chance"));
				
				final int category = dropData.getInt("category");
				npcDat.addDropData(dropDat, category);
			}
		}
		catch (final Exception e)
		{
			_log.severe("NPCTable: Error reading NPC drop data: " + e);
		}
		
		if (Config.CUSTOM_DROPLIST_TABLE)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement2 = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[]
				{
					"mobId",
					"itemId",
					"min",
					"max",
					"category",
					"chance"
				}) + " FROM custom_droplist ORDER BY mobId, chance DESC");
				ResultSet dropData = statement2.executeQuery())
			{
				L2DropData dropDat = null;
				L2NpcTemplate npcDat = null;
				int cCount = 0;
				
				while (dropData.next())
				{
					final int mobId = dropData.getInt("mobId");
					npcDat = _npcs.get(mobId);
					if (npcDat == null)
					{
						_log.severe("NPCTable: No custom npc correlating with id : " + mobId);
						continue;
					}
					dropDat = new L2DropData();
					
					dropDat.setItemId(dropData.getInt("itemId"));
					dropDat.setMinDrop(dropData.getInt("min"));
					dropDat.setMaxDrop(dropData.getInt("max"));
					dropDat.setChance(dropData.getInt("chance"));
					
					final int category = dropData.getInt("category");
					npcDat.addDropData(dropDat, category);
					cCount++;
				}
				
				_log.info("CustomDropList : Added " + cCount + " drops to custom droplist");
			}
			catch (final Exception e)
			{
				_log.severe("NPCTable: Error reading Custom NPC drop data: " + e);
			}
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement3 = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[]
			{
				"npc_id",
				"class_id"
			}) + " FROM skill_learn");
			ResultSet learndata = statement3.executeQuery())
		{
			while (learndata.next())
			{
				final int npcId = learndata.getInt("npc_id");
				final int classId = learndata.getInt("class_id");
				
				final L2NpcTemplate npc = getTemplate(npcId);
				if (npc == null)
				{
					_log.warning("NPCTable: Error getting NPC template ID " + npcId + " while trying to load skill trainer data.");
					continue;
				}
				
				npc.addTeachInfo(ClassId.values()[classId]);
			}
		}
		catch (final Exception e)
		{
			_log.severe("NPCTable: Error reading NPC trainer data: " + e);
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement4 = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[]
			{
				"boss_id",
				"minion_id",
				"amount_min",
				"amount_max"
			}) + " FROM minions");
			ResultSet minionData = statement4.executeQuery())
		{
			L2MinionData minionDat = null;
			L2NpcTemplate npcDat = null;
			int cnt = 0;
			
			while (minionData.next())
			{
				final int raidId = minionData.getInt("boss_id");
				npcDat = _npcs.get(raidId);
				minionDat = new L2MinionData();
				minionDat.setMinionId(minionData.getInt("minion_id"));
				minionDat.setAmountMin(minionData.getInt("amount_min"));
				minionDat.setAmountMax(minionData.getInt("amount_max"));
				npcDat.addRaidData(minionDat);
				cnt++;
			}
			
			_log.config("NpcTable: Loaded " + cnt + " Minions.");
		}
		catch (final Exception e)
		{
			_log.severe("Error loading minion data: " + e);
		}
		
		_initialized = true;
	}
	
	private void fillNpcTable(ResultSet NpcData, boolean customData) throws Exception
	{
		int count = 0;
		while (NpcData.next())
		{
			final StatsSet npcDat = new StatsSet();
			final int id = NpcData.getInt("id");
			
			if (Config.ASSERT)
			{
				assert id < 1000000;
			}
			
			npcDat.set("npcId", id);
			npcDat.set("idTemplate", NpcData.getInt("idTemplate"));
			final int level = NpcData.getInt("level");
			npcDat.set("level", level);
			npcDat.set("jClass", NpcData.getString("class"));
			
			npcDat.set("baseShldDef", 0);
			npcDat.set("baseShldRate", 0);
			npcDat.set("baseCritRate", 38);
			
			npcDat.set("name", NpcData.getString("name"));
			npcDat.set("serverSideName", NpcData.getBoolean("serverSideName"));
			// npcDat.set("name", "");
			npcDat.set("title", NpcData.getString("title"));
			npcDat.set("serverSideTitle", NpcData.getBoolean("serverSideTitle"));
			npcDat.set("collision_radius", NpcData.getDouble("collision_radius"));
			npcDat.set("collision_height", NpcData.getDouble("collision_height"));
			npcDat.set("sex", NpcData.getString("sex"));
			npcDat.set("type", NpcData.getString("type"));
			npcDat.set("baseAtkRange", NpcData.getInt("attackrange"));
			npcDat.set("rewardExp", NpcData.getInt("exp"));
			npcDat.set("rewardSp", NpcData.getInt("sp"));
			npcDat.set("basePAtkSpd", NpcData.getInt("atkspd"));
			npcDat.set("baseMAtkSpd", NpcData.getInt("matkspd"));
			npcDat.set("aggroRange", NpcData.getInt("aggro"));
			npcDat.set("rhand", NpcData.getInt("rhand"));
			npcDat.set("lhand", NpcData.getInt("lhand"));
			npcDat.set("armor", NpcData.getInt("armor"));
			npcDat.set("baseWalkSpd", NpcData.getInt("walkspd"));
			npcDat.set("baseRunSpd", NpcData.getInt("runspd"));
			
			// constants, until we have stats in DB
			npcDat.set("baseSTR", NpcData.getInt("str"));
			npcDat.set("baseCON", NpcData.getInt("con"));
			npcDat.set("baseDEX", NpcData.getInt("dex"));
			npcDat.set("baseINT", NpcData.getInt("int"));
			npcDat.set("baseWIT", NpcData.getInt("wit"));
			npcDat.set("baseMEN", NpcData.getInt("men"));
			
			npcDat.set("baseHpMax", NpcData.getInt("hp"));
			npcDat.set("baseCpMax", 0);
			npcDat.set("baseMpMax", NpcData.getInt("mp"));
			npcDat.set("baseHpReg", NpcData.getFloat("hpreg") > 0 ? NpcData.getFloat("hpreg") : 1.5 + ((level - 1) / 10));
			npcDat.set("baseMpReg", NpcData.getFloat("mpreg") > 0 ? NpcData.getFloat("mpreg") : 0.9 + (0.3 * ((level - 1) / 10)));
			npcDat.set("basePAtk", NpcData.getInt("patk"));
			npcDat.set("basePDef", NpcData.getInt("pdef"));
			npcDat.set("baseMAtk", NpcData.getInt("matk"));
			npcDat.set("baseMDef", NpcData.getInt("mdef"));
			
			npcDat.set("factionId", NpcData.getString("faction_id"));
			npcDat.set("factionRange", NpcData.getInt("faction_range"));
			
			npcDat.set("isUndead", NpcData.getString("isUndead"));
			
			npcDat.set("absorb_level", NpcData.getString("absorb_level"));
			
			npcDat.set("ss", NpcData.getInt("ss"));
			npcDat.set("bss", NpcData.getInt("bss"));
			npcDat.set("ssRate", NpcData.getInt("ss_rate"));
			
			npcDat.set("AI", NpcData.getString("AI"));
			
			final L2NpcTemplate template = new L2NpcTemplate(npcDat);
			template.addVulnerability(Stats.BOW_WPN_VULN, 1);
			template.addVulnerability(Stats.BLUNT_WPN_VULN, 1);
			template.addVulnerability(Stats.DAGGER_WPN_VULN, 1);
			
			final L2NpcTemplate oldTemplate = getTemplate(id);
			if (oldTemplate != null)
			{
				// add quest events to the new template
				if (oldTemplate.questEvents != null)
				{
					template.questEvents = oldTemplate.questEvents;
				}
			}
			
			_npcs.put(id, template);
			count++;
		}
		
		if (count > 0)
		{
			if (!customData)
			{
				_log.config("NpcTable: (Re)Loaded " + count + " NPC template(s).");
			}
			else
			{
				_log.config("NpcTable: (Re)Loaded " + count + " custom NPC template(s).");
			}
		}
	}
	
	public void reloadNpc(int id)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			// save a copy of the old data
			final L2NpcTemplate old = getTemplate(id);
			final Map<Integer, L2Skill> skills = new FastMap<>();
			if (old.getSkills() != null)
			{
				skills.putAll(old.getSkills());
			}
			
			final FastList<L2DropCategory> categories = new FastList<>();
			if (old.getDropData() != null)
			{
				categories.addAll(old.getDropData());
			}
			
			ClassId[] classIds = null;
			if (old.getTeachInfo() != null)
			{
				classIds = old.getTeachInfo().clone();
			}
			
			final List<L2MinionData> minions = new FastList<>();
			if (old.getMinionData() != null)
			{
				minions.addAll(old.getMinionData());
			}
			
			// reload the NPC base data
			try (PreparedStatement st = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[]
			{
				"id",
				"idTemplate",
				"name",
				"serverSideName",
				"title",
				"serverSideTitle",
				"class",
				"collision_radius",
				"collision_height",
				"level",
				"sex",
				"type",
				"attackrange",
				"hp",
				"mp",
				"hpreg",
				"mpreg",
				"str",
				"con",
				"dex",
				"int",
				"wit",
				"men",
				"exp",
				"sp",
				"patk",
				"pdef",
				"matk",
				"mdef",
				"atkspd",
				"aggro",
				"matkspd",
				"rhand",
				"lhand",
				"armor",
				"walkspd",
				"runspd",
				"faction_id",
				"faction_range",
				"isUndead",
				"absorb_level",
				"ss",
				"bss",
				"ss_rate",
				"AI"
			}) + " FROM npc WHERE id=?"))
			{
				st.setInt(1, id);
				try (ResultSet rs = st.executeQuery())
				{
					fillNpcTable(rs, false);
				}
			}
			
			if (Config.CUSTOM_NPC_TABLE) // reload certain NPCs
			{
				try (PreparedStatement st = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[]
				{
					"id",
					"idTemplate",
					"name",
					"serverSideName",
					"title",
					"serverSideTitle",
					"class",
					"collision_radius",
					"collision_height",
					"level",
					"sex",
					"type",
					"attackrange",
					"hp",
					"mp",
					"hpreg",
					"mpreg",
					"str",
					"con",
					"dex",
					"int",
					"wit",
					"men",
					"exp",
					"sp",
					"patk",
					"pdef",
					"matk",
					"mdef",
					"atkspd",
					"aggro",
					"matkspd",
					"rhand",
					"lhand",
					"armor",
					"walkspd",
					"runspd",
					"faction_id",
					"faction_range",
					"isUndead",
					"absorb_level",
					"ss",
					"bss",
					"ss_rate",
					"AI"
				}) + " FROM custom_npc WHERE id=?"))
				{
					st.setInt(1, id);
					try (ResultSet rs = st.executeQuery())
					{
						fillNpcTable(rs, true);
					}
				}
			}
			
			// restore additional data from saved copy
			final L2NpcTemplate created = getTemplate(id);
			
			// set race
			created.setRace(old.race);
			
			for (final L2Skill skill : skills.values())
			{
				created.addSkill(skill);
			}
			
			if (classIds != null)
			{
				for (final ClassId classId : classIds)
				{
					created.addTeachInfo(classId);
				}
			}
			
			for (final L2MinionData minion : minions)
			{
				created.addRaidData(minion);
			}
		}
		catch (final Exception e)
		{
			_log.warning("NPCTable: Could not reload data for NPC " + id + ": " + e);
		}
	}
	
	// just wrapper
	public void reloadAllNpc()
	{
		restoreNpcData();
	}
	
	public void saveNpc(StatsSet npc)
	{
		String query = "";
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			final Map<String, Object> set = npc.getSet();
			
			String name = "";
			String values = "";
			
			for (final Object obj : set.keySet())
			{
				name = (String) obj;
				
				if (name.equalsIgnoreCase("npcId"))
				{
					continue;
				}
				
				if (!values.isEmpty())
				{
					values += ", ";
				}
				
				values += name + " = '" + set.get(name) + "'";
				
			}
			
			int updated = 0;
			if (Config.CUSTOM_NPC_TABLE)
			{
				query = "UPDATE custom_npc SET " + values + " WHERE id = ?";
				try (PreparedStatement statement = con.prepareStatement(query))
				{
					statement.setInt(1, npc.getInteger("npcId"));
					updated = statement.executeUpdate();
				}
			}
			
			if (updated == 0)
			{
				query = "UPDATE npc SET " + values + " WHERE id = ?";
				try (PreparedStatement statement = con.prepareStatement(query))
				{
					statement.setInt(1, npc.getInteger("npcId"));
					statement.executeUpdate();
				}
			}
		}
		catch (final Exception e)
		{
			_log.warning("NPCTable: Could not store new NPC data in database: " + e);
		}
	}
	
	public boolean isInitialized()
	{
		return _initialized;
	}
	
	public void replaceTemplate(L2NpcTemplate npc)
	{
		_npcs.put(npc.npcId, npc);
	}
	
	public L2NpcTemplate getTemplate(int id)
	{
		return _npcs.get(id);
	}
	
	public L2NpcTemplate getTemplateByName(String name)
	{
		for (final L2NpcTemplate npcTemplate : _npcs.values())
		{
			if (npcTemplate.name.equalsIgnoreCase(name))
			{
				return npcTemplate;
			}
		}
		
		return null;
	}
	
	public L2NpcTemplate[] getAllOfLevel(int lvl)
	{
		final List<L2NpcTemplate> list = new FastList<>();
		
		for (final L2NpcTemplate t : _npcs.values())
		{
			if (t.level == lvl)
			{
				list.add(t);
			}
		}
		
		return list.toArray(new L2NpcTemplate[list.size()]);
	}
	
	public L2NpcTemplate[] getAllMonstersOfLevel(int lvl)
	{
		final List<L2NpcTemplate> list = new FastList<>();
		
		for (final L2NpcTemplate t : _npcs.values())
		{
			if ((t.level == lvl) && "L2Monster".equals(t.type))
			{
				list.add(t);
			}
		}
		
		return list.toArray(new L2NpcTemplate[list.size()]);
	}
	
	public L2NpcTemplate[] getAllNpcStartingWith(String letter)
	{
		final List<L2NpcTemplate> list = new FastList<>();
		
		for (final L2NpcTemplate t : _npcs.values())
		{
			if (t.name.startsWith(letter) && "L2Npc".equals(t.type))
			{
				list.add(t);
			}
		}
		
		return list.toArray(new L2NpcTemplate[list.size()]);
	}
}