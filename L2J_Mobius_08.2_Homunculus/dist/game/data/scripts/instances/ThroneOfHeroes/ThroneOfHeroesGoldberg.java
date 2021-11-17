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
package instances.ThroneOfHeroes;

import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import instances.AbstractInstance;

/**
 * @author CostyKiller
 * @URL: https://www.youtube.com/watch?v=fJWAWU5XpZk
 */
public class ThroneOfHeroesGoldberg extends AbstractInstance
{
	// NPCs
	private static final int ROIENTAL = 34571;
	// Monsters
	private static final int GOLDBERG = 26250;
	private static final int[] GOLDBERG_MINIONS =
	{
		26251, // Handy Cannon Gunner
		26252, // Anchor Warrior
		26253, // Operator
		26254 // Axe Warrior
	};
	// Throne's Treasure Chest Goldberg
	private static final int TREASURE_CHEST = 26455;
	// Misc
	private static final int TEMPLATE_ID = 307;
	
	public ThroneOfHeroesGoldberg()
	{
		super(TEMPLATE_ID);
		addInstanceCreatedId(TEMPLATE_ID);
		addStartNpc(ROIENTAL);
		addTalkId(ROIENTAL);
		addAttackId(GOLDBERG);
		addKillId(GOLDBERG);
	}
	
	@Override
	public void onInstanceCreated(Instance activeInstance, Player player)
	{
		activeInstance.setStatus(0);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "enterInstance":
			{
				enterInstance(player, npc, TEMPLATE_ID);
				startQuestTimer("ANNOUNCE_RAID_START", 10000, null, player);
				break;
			}
			case "reenterInstance":
			{
				final Instance activeInstance = getPlayer(player);
				if (isInInstance(activeInstance))
				{
					enterInstance(player, npc, activeInstance.getTemplateId());
				}
				break;
			}
			case "ANNOUNCE_RAID_START":
			{
				final Instance world = player.getInstanceWorld();
				if (isInInstance(world))
				{
					showOnScreenMsg(world, NpcStringId.THE_CHALLENGE_FOR_THE_THRONE_OF_HEROES_GOLDBERG_WILL_BEGIN_SHORTLY_PLEASE_GET_READY, ExShowScreenMessage.TOP_CENTER, 5000, true);
					startQuestTimer("ANNOUNCE_RAID_PREP", 15000, null, player);
				}
				break;
			}
			case "ANNOUNCE_RAID_PREP":
			{
				final Instance world = player.getInstanceWorld();
				if (isInInstance(world))
				{
					showOnScreenMsg(world, NpcStringId.THE_FIGHTING_AGAINST_GOLDBERG_WILL_BEGIN_IN_10_SECONDS, ExShowScreenMessage.TOP_CENTER, 5000, true);
					startQuestTimer("ANNOUNCE_5", 5000, null, player);
				}
				break;
			}
			case "ANNOUNCE_5":
			{
				final Instance world = player.getInstanceWorld();
				if (isInInstance(world))
				{
					showOnScreenMsg(world, NpcStringId.FIVE_SECONDS, ExShowScreenMessage.TOP_CENTER, 1000, true);
					startQuestTimer("ANNOUNCE_4", 1000, null, player);
				}
				break;
			}
			case "ANNOUNCE_4":
			{
				final Instance world = player.getInstanceWorld();
				if (isInInstance(world))
				{
					showOnScreenMsg(world, NpcStringId.FOUR_SECONDS, ExShowScreenMessage.TOP_CENTER, 1000, true);
					startQuestTimer("ANNOUNCE_3", 1000, null, player);
				}
				break;
			}
			case "ANNOUNCE_3":
			{
				final Instance world = player.getInstanceWorld();
				if (isInInstance(world))
				{
					showOnScreenMsg(world, NpcStringId.THREE_SECONDS_2, ExShowScreenMessage.TOP_CENTER, 1000, true);
					startQuestTimer("ANNOUNCE_2", 1000, null, player);
				}
				break;
			}
			case "ANNOUNCE_2":
			{
				final Instance world = player.getInstanceWorld();
				if (isInInstance(world))
				{
					showOnScreenMsg(world, NpcStringId.TWO_SECONDS_2, ExShowScreenMessage.TOP_CENTER, 1000, true);
					startQuestTimer("ANNOUNCE_1", 1000, null, player);
				}
				break;
			}
			case "ANNOUNCE_1":
			{
				final Instance world = player.getInstanceWorld();
				if (isInInstance(world))
				{
					showOnScreenMsg(world, NpcStringId.ONE_SECOND_2, ExShowScreenMessage.TOP_CENTER, 1000, true);
					startQuestTimer("SPAWN_GOLDBERG", 1000, null, player);
				}
				break;
			}
			case "SPAWN_GOLDBERG":
			{
				final Instance world = player.getInstanceWorld();
				if (isInInstance(world))
				{
					showOnScreenMsg(world, NpcStringId.GOLDBERG_APPEARS, ExShowScreenMessage.TOP_CENTER, 5000, true);
					world.spawnGroup("GOLDBERG");
				}
				break;
			}
			case "SPAWN_GOLDBERG_MINIONS":
			{
				final Instance world = npc.getInstanceWorld();
				if (isInInstance(world))
				{
					showOnScreenMsg(world, NpcStringId.GOLDBERG_SUMMONS_HIS_MINIONS, ExShowScreenMessage.TOP_CENTER, 5000, true);
					world.spawnGroup("GOLDBERG_MINIONS");
				}
				break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon)
	{
		final Instance world = npc.getInstanceWorld();
		if (isInInstance(world))
		{
			final int hpPer = npc.getCurrentHpPercent();
			if (npc.getId() == GOLDBERG)
			{
				if ((hpPer <= 50) && world.isStatus(0))
				{
					startQuestTimer("SPAWN_GOLDBERG_MINIONS", 10000, npc, null);
					world.setStatus(1);
				}
				else if ((hpPer <= 30) && world.isStatus(1))
				{
					showOnScreenMsg(world, NpcStringId.GOLDBERG_BRINGS_OUT_ALL_OF_THE_POWER_WITHIN_HIM, ExShowScreenMessage.TOP_CENTER, 5000, true);
					world.setStatus(2);
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final Instance world = npc.getInstanceWorld();
		if (isInInstance(world))
		{
			if (npc.getId() == GOLDBERG)
			{
				// Despawn minions and stop timer
				cancelQuestTimer("SPAWN_GOLDBERG_MINIONS", npc, killer);
				world.getAliveNpcs(GOLDBERG_MINIONS).forEach(beast -> beast.doDie(null));
				
				// Spawn treasure chests
				boolean eightCCMembersOrMore = ((killer.getCommandChannel() != null) && (killer.getCommandChannel().getMemberCount() >= 8));
				if (killer.isGM() || eightCCMembersOrMore)
				{
					addSpawn(TREASURE_CHEST, killer.getX() + getRandom(-150, 150), killer.getY() + getRandom(-150, 150), killer.getZ() + 10, 0, false, 0, true, world.getId());
					addSpawn(TREASURE_CHEST, killer.getX() + getRandom(-150, 150), killer.getY() + getRandom(-150, 150), killer.getZ() + 10, 0, false, 0, true, world.getId());
				}
				else
				{
					addSpawn(TREASURE_CHEST, killer.getX() + getRandom(-150, 150), killer.getY() + getRandom(-150, 150), killer.getZ() + 10, 0, false, 0, true, world.getId());
				}
				// Finish instance
				world.finishInstance(2);
				if (!killer.isGM())
				{
					// Set clan variable
					killer.getClan().getVariables().set("TOH_DONE", Chronos.currentTimeMillis());
					killer.getClan().getVariables().storeMe();
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new ThroneOfHeroesGoldberg();
	}
}
