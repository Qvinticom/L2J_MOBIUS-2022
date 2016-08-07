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
import java.util.Map;

import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.SkillHolder;
import com.l2jmobius.gameserver.model.instancezone.Instance;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.zone.L2ZoneType;

import instances.AbstractInstance;
import quests.Q00144_PailakaInjuredDragon.Q00144_PailakaInjuredDragon;

/**
 * @author Mathael
 */
public class PailakaInjuredDragon extends AbstractInstance
{
	// NPCs
	private static final int KETRA_ORC_SHAMAN = 34799;
	private static final int KETRA_ORC_SUPPORTER = 32502;
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
	private static final int LATANA = 18660;
	// Zones
	private static final int BARRIER1 = 200001; // Zone 1
	private static final int BARRIER2 = 200002; // Zone 1
	private static final int BARRIER3 = 200003; // Zone 1
	private static final int BARRIER4 = 200004; // Zone 1
	private static final int BARRIER5 = 200005; // Zone 2
	private static final int BARRIER6 = 200006; // Zone 2
	private static final int BARRIER7 = 200007; // Zone 2
	private static final int BARRIER8 = 200008; // Zone 3
	private static final int BARRIER9 = 200009; // Zone 3
	private static final Map<Integer, Location> ZONE_TELEPORTS = new HashMap<>();
	static
	{
		ZONE_TELEPORTS.put(BARRIER1, new Location(122452, -45808, -2981));
		ZONE_TELEPORTS.put(BARRIER2, new Location(116610, -46418, -2641));
		ZONE_TELEPORTS.put(BARRIER3, new Location(116237, -50961, -2636));
		ZONE_TELEPORTS.put(BARRIER4, new Location(117384, -52141, -2544));
		ZONE_TELEPORTS.put(BARRIER5, new Location(112169, -44004, -2707));
		ZONE_TELEPORTS.put(BARRIER6, new Location(109460, -45869, -2265));
		ZONE_TELEPORTS.put(BARRIER7, new Location(117111, -55927, -2380));
		ZONE_TELEPORTS.put(BARRIER8, new Location(109274, -41277, -2271));
		ZONE_TELEPORTS.put(BARRIER9, new Location(110023, -40263, -2001));
	}
	// Skill
	private static final SkillHolder LATANA_PRESENTATION_SKILL = new SkillHolder(5759, 1);
	// Misc
	private static final int TEMPLATE_ID = 45;
	private int unlockZoneKillCount = 0;
	
	public PailakaInjuredDragon()
	{
		addInstanceEnterId(TEMPLATE_ID);
		addStartNpc(KETRA_ORC_SHAMAN);
		addKillId(GENERAL, GREAT_MAGUS, PROPHET, ELITE_GUARD, COMMANDER, OFFICER, RECRUIT, FOOTMAN, WARRIOR, PROPHET_GUARD, HEAD_GUARD, LATANA);
		addSpawnId(LATANA);
		addAggroRangeEnterId(LATANA);
		addEnterZoneId(ZONE_TELEPORTS.keySet());
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
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (npc.getId() == LATANA)
		{
			startQuestTimer("camera_end", 1000, npc, killer);
			addSpawn(KETRA_ORC_SUPPORTER, new Location(105517, -41692, -1781, 65323), false, 0, false, npc.getInstanceWorld().getId());
		}
		else
		{
			final Instance world = npc.getInstanceWorld();
			if (world.getStatus() < 3)
			{
				final int npcId = npc.getId();
				
				switch (world.getStatus())
				{
					case 0:
					{
						switch (npcId)
						{
							case RECRUIT:
							case FOOTMAN:
							case WARRIOR:
							{
								unlockZoneKillCount++;
							}
						}
						break;
					}
					case 1:
					{
						switch (npcId)
						{
							case OFFICER:
							case GREAT_MAGUS:
							case GENERAL:
							case ELITE_GUARD:
							case COMMANDER:
							{
								unlockZoneKillCount++;
							}
						}
						break;
					}
					case 2:
					{
						switch (npcId)
						{
							case HEAD_GUARD:
							case PROPHET_GUARD:
							{
								unlockZoneKillCount++;
							}
						}
						break;
					}
				}
				
				if (unlockZoneKillCount == 5)
				{
					world.setStatus(npc.getInstanceWorld().getStatus() + 1);
					unlockZoneKillCount = 0;
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
		if (!character.isPlayer())
		{
			return super.onEnterZone(character, zone);
		}
		
		final Instance world = character.getInstanceWorld();
		final int zoneId = zone.getId();
		switch (zoneId)
		{
			case BARRIER1:
			case BARRIER2:
			case BARRIER3:
			case BARRIER4:
			{
				if (world.getStatus() < 1)
				{
					character.teleToLocation(ZONE_TELEPORTS.get(zoneId));
				}
				break;
			}
			case BARRIER5:
			case BARRIER6:
			case BARRIER7:
			{
				if (world.getStatus() < 2)
				{
					character.teleToLocation(ZONE_TELEPORTS.get(zoneId));
				}
				break;
			}
			case BARRIER8:
			case BARRIER9:
			{
				if (world.getStatus() < 3)
				{
					character.teleToLocation(ZONE_TELEPORTS.get(zoneId));
				}
				break;
			}
		}
		return super.onEnterZone(character, zone);
	}
	
	public static void main(String[] args)
	{
		new PailakaInjuredDragon();
	}
}
