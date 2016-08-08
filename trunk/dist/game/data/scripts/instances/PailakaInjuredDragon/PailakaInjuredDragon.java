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
	// Zones
	private static final int[] ZONES =
	{
		200001,
		200002,
		200003,
		200004,
		200005,
		200006,
		200007,
		200008,
		200009
	};
	// Walls
	private final HashMap<Integer, MonsterWall> WALLS = new HashMap<>();
	private static final Location[] ZONES_TELEPORTS =
	{
		new Location(122452, -45808, -2981),
		new Location(116610, -46418, -2641),
		new Location(116237, -50961, -2636),
		new Location(117384, -52141, -2544),
		new Location(112169, -44004, -2707),
		new Location(109460, -45869, -2265),
		new Location(117111, -55927, -2380),
		new Location(109274, -41277, -2271),
		new Location(110023, -40263, -2001)
	};
	// Skill
	private static final SkillHolder LATANA_PRESENTATION_SKILL = new SkillHolder(5759, 1);
	// Misc
	private static final int TEMPLATE_ID = 45;
	
	public PailakaInjuredDragon()
	{
		addInstanceEnterId(TEMPLATE_ID);
		addStartNpc(KETRA_ORC_SHAMAN);
		addKillId(ANTELOPE1, ANTELOPE2, ANTELOPE3, GENERAL, GREAT_MAGUS, PROPHET, ELITE_GUARD, COMMANDER, OFFICER, RECRUIT, FOOTMAN, WARRIOR, PROPHET_GUARD, HEAD_GUARD, LATANA);
		addAttackId(SHAMAN, GRAND_PRIEST, CHIEF_PRIEST);
		addSpawnId(LATANA, SHAMAN, CHIEF_PRIEST, GRAND_PRIEST);
		addAggroRangeEnterId(LATANA);
		addEnterZoneId(ZONES);
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
			final int zoneId = ZONES[i];
			final List<L2Npc> npcs = instance.spawnGroup("wall_" + (i + 1));
			npcs.forEach(k -> k.setScriptValue(zoneId));
			WALLS.put(zoneId, new MonsterWall((i + 1), zoneId, npcs, ZONES_TELEPORTS[i]));
		}
		super.onInstanceCreated(instance, player);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		final MonsterWall wall = WALLS.get(npc.getScriptValue());
		if ((wall != null) && !wall.isUnlocked())
		{
			if (wall.getMobs().stream().filter(mob -> !mob.isDead()).count() == 0)
			{
				wall.unlock();
			}
			attacker.teleToLocation(wall.getZoneTeleportBack());
		}
		return super.onAttack(npc, attacker, damage, isSummon);
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
			
			if ((npcId != ANTELOPE1) && (npcId != ANTELOPE2) && (npcId != ANTELOPE3))
			{
				final MonsterWall wall = WALLS.get(npc.getScriptValue());
				if ((wall != null) && !wall.isUnlocked() && !wall.isPriestSpawned())
				{
					wall.spawnPriest(npc.getInstanceWorld());
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setIsImmobilized(true);
		if (npc.getId() != LATANA)
		{
			npc.setIsInvul(true);
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if (!character.isPlayer())
		{
			return super.onEnterZone(character, zone);
		}
		
		final MonsterWall wall = WALLS.get(zone.getId());
		if ((wall != null) && !wall.isUnlocked())
		{
			if ((wall.getMobs().stream().filter(mob -> !mob.isDead()).count() > 0) || wall.isPriestSpawned())
			{
				character.teleToLocation(wall.getZoneTeleportBack());
			}
		}
		
		return super.onEnterZone(character, zone);
	}
	
	private class MonsterWall
	{
		private final int _wallNumber;
		private final int _zoneId;
		private boolean _unlocked;
		private boolean _priestSpawned;
		private final List<L2Npc> _mobs;
		private List<L2Npc> _priests;
		private final Location _zoneTeleportBack;
		
		MonsterWall(int wallNumber, int zoneId, List<L2Npc> mobs, Location loc)
		{
			_unlocked = false;
			_priestSpawned = false;
			_wallNumber = wallNumber;
			_zoneId = zoneId;
			_mobs = mobs;
			_priests = null;
			_zoneTeleportBack = loc;
		}
		
		boolean isUnlocked()
		{
			return _unlocked;
		}
		
		boolean isPriestSpawned()
		{
			return _priestSpawned;
		}
		
		List<L2Npc> getMobs()
		{
			return _mobs;
		}
		
		Location getZoneTeleportBack()
		{
			return _zoneTeleportBack;
		}
		
		void spawnPriest(Instance instance)
		{
			_priests = instance.spawnGroup("wall_" + _wallNumber + "_add");
			_priests.forEach(k -> k.setScriptValue(_zoneId));
			_priestSpawned = true;
		}
		
		void unlock()
		{
			_unlocked = true;
			_priestSpawned = false;
			_priests.forEach(L2Npc::deleteMe);
			_mobs.forEach(L2Npc::deleteMe);
		}
	}
	
	public static void main(String[] args)
	{
		new PailakaInjuredDragon();
	}
}
