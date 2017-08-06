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
package instances.PailakaInjuredDragon;

import java.util.HashMap;
import java.util.List;

import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.SkillHolder;
import com.l2jmobius.gameserver.model.instancezone.Instance;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.spawns.SpawnGroup;
import com.l2jmobius.gameserver.model.zone.L2ZoneType;

import instances.AbstractInstance;
import quests.Q00144_PailakaInjuredDragon.Q00144_PailakaInjuredDragon;

/**
 * @author Mathael
 */
public class PailakaInjuredDragon extends AbstractInstance
{
	// NPCs
	private static final int KETRA_ORC_SUPPORTER_END = 32512;
	// Monsters
	private static final int RECRUIT = 18635;
	private static final int FOOTMAN = 18636;
	private static final int WARRIOR = 18642;
	private static final int OFFICER = 18646;
	private static final int GREAT_MAGUS = 18649;
	private static final int GENERAL = 18650;
	private static final int ELITE_GUARD = 18653;
	private static final int COMMANDER = 18654;
	private static final int HEAD_GUARD = 18655;
	private static final int PROPHET_GUARD = 18657;
	private static final int PROPHET = 18659;
	private static final int SHAMAN = 18640;
	private static final int CHIEF_PRIEST = 18648;
	private static final int GRAND_PRIEST = 18652;
	private static final int LATANA = 18660;
	private static final int ANTELOPE1 = 18637;
	private static final int ANTELOPE2 = 18643;
	private static final int ANTELOPE3 = 18651;
	// Usable Quest Items
	private static final int SHIELD_POTION = 13032;
	private static final int HEAL_POTION = 13033;
	// Walls
	private static final HashMap<Integer, Location> ZONES_TELEPORTS = new HashMap<>();
	static
	{
		ZONES_TELEPORTS.put(200001, new Location(122452, -45808, -2981));
		ZONES_TELEPORTS.put(200002, new Location(116610, -46418, -2641));
		ZONES_TELEPORTS.put(200003, new Location(116237, -50961, -2636));
		ZONES_TELEPORTS.put(200004, new Location(117384, -52141, -2544));
		ZONES_TELEPORTS.put(200005, new Location(112169, -44004, -2707));
		ZONES_TELEPORTS.put(200006, new Location(109460, -45869, -2265));
		ZONES_TELEPORTS.put(200007, new Location(117111, -55927, -2380));
		ZONES_TELEPORTS.put(200008, new Location(109274, -41277, -2271));
		ZONES_TELEPORTS.put(200009, new Location(110023, -40263, -2001));
	}
	// Skill
	private static final SkillHolder LATANA_PRESENTATION_SKILL = new SkillHolder(5759, 1);
	// Misc
	private static final int TEMPLATE_ID = 45;
	
	public PailakaInjuredDragon()
	{
		super(TEMPLATE_ID);
		addInstanceEnterId(TEMPLATE_ID);
		addKillId(ANTELOPE1, ANTELOPE2, ANTELOPE3, GENERAL, GREAT_MAGUS, PROPHET, ELITE_GUARD, COMMANDER, OFFICER, RECRUIT, FOOTMAN, WARRIOR, PROPHET_GUARD, HEAD_GUARD, SHAMAN, CHIEF_PRIEST, GRAND_PRIEST, LATANA);
		addSpawnId(LATANA);
		addAggroRangeEnterId(LATANA);
		addEnterZoneId(ZONES_TELEPORTS.keySet());
		addInstanceCreatedId(TEMPLATE_ID);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		switch (event)
		{
			case "enter":
			{
				enterInstance(player, npc, TEMPLATE_ID);
				break;
			}
			case "camera_start":
			{
				specialCamera(player, npc, 0, 0, 180, 800, 5000, 800, 0, 1, 0, 0);
				break; // TODO: Need retail values
			}
			case "camera_end":
			{
				specialCamera(player, npc, 0, 0, 180, 600, 5000, 600, 0, 1, 0, 0);
				break; // TODO: Need retail values
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	protected void onEnter(L2PcInstance player, Instance instance, boolean firstEnter)
	{
		super.onEnter(player, instance, firstEnter);
		if (firstEnter)
		{
			final QuestState qs = player.getQuestState(Q00144_PailakaInjuredDragon.class.getSimpleName());
			if ((qs != null) && qs.isCond(1))
			{
				qs.setCond(2, true);
				showHtmlFile(player, "32499-09.html");
			}
		}
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		if (npc.isScriptValue(0))
		{
			npc.setScriptValue(1);
			startQuestTimer("camera_start", 300, npc, player);
			npc.doCast(LATANA_PRESENTATION_SKILL.getSkill());
		}
		return super.onAggroRangeEnter(npc, player, isSummon);
	}
	
	@Override
	public void onInstanceCreated(Instance instance, L2PcInstance player)
	{
		for (int i = 0; i < 9; i++)
		{
			final int zoneId = 200000 + (i + 1);
			final List<L2Npc> npcs = instance.spawnGroup("wall_" + zoneId);
			npcs.forEach(k -> k.setScriptValue(zoneId));
			instance.getParameters().set("wall_" + zoneId, npcs.size());
		}
		
		super.onInstanceCreated(instance, player);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final int npcId = npc.getId();
		if (npcId == LATANA)
		{
			startQuestTimer("camera_end", 1000, npc, killer);
			addSpawn(KETRA_ORC_SUPPORTER_END, new Location(105517, -41692, -1781, 65323), false, 0, false, npc.getInstanceWorld().getId());
		}
		else
		{
			switch (getRandom(1, 3))
			{
				case 1:
				{
					npc.dropItem(killer, SHIELD_POTION, getRandom(1, 8));
					break;
				}
				case 2:
				{
					npc.dropItem(killer, HEAL_POTION, getRandom(1, 4));
					break;
				}
			}
			
			if ((npcId != ANTELOPE1) && (npcId != ANTELOPE2) && (npcId != ANTELOPE3) && (npcId != SHAMAN) && (npcId != CHIEF_PRIEST) && (npcId != GRAND_PRIEST))
			{
				final Instance world = npc.getInstanceWorld();
				final int zoneId = npc.getScriptValue();
				int killcount = world.getParameters().getInt("wall_" + zoneId);
				killcount--;
				world.setParameter("wall_" + zoneId, killcount);
				
				if (killcount <= 0)
				{
					world.getSpawnGroup("wall_" + zoneId).forEach(SpawnGroup::despawnAll);
					world.getSpawnGroup("wall_" + zoneId + "_add").forEach(SpawnGroup::despawnAll);
				}
				else if (!world.getParameters().getBoolean("wall_" + zoneId + "_add", false))
				{
					world.setParameter("wall_" + zoneId + "_add", true);
					world.spawnGroup("wall_" + zoneId + "_add").forEach(mage ->
					{
						mage.setTarget(killer);
						mage.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer);
					});
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setIsImmobilized(true);
		return super.onSpawn(npc);
	}
	
	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if (!character.isPlayer() || (character.getInstanceId() == 0))
		{
			return super.onEnterZone(character, zone);
		}
		
		final Instance world = character.getInstanceWorld();
		if (world.getParameters().getInt("wall_" + zone.getId()) > 0)
		{
			character.teleToLocation(ZONES_TELEPORTS.get(zone.getId()));
		}
		
		return super.onEnterZone(character, zone);
	}
	
	public static void main(String[] args)
	{
		new PailakaInjuredDragon();
	}
}
