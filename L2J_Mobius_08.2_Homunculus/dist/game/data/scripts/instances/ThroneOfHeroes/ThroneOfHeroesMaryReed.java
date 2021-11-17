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
 */
public class ThroneOfHeroesMaryReed extends AbstractInstance
{
	// NPCs
	private static final int ROIENTAL = 34571;
	// Monsters
	private static final int MARY_REED = 26267;
	private static final int MARY_REED_MINION_ZAKEN = 26255;
	// Throne's Treasure Chest Mary Reed
	private static final int TREASURE_CHEST = 26456;
	// Misc
	private static final int TEMPLATE_ID = 308;
	// NPC dialogs
	private static final NpcStringId[] ZAKEN_MESSAGES =
	{
		NpcStringId.MARY_REED_THEY_ARE_JUST_INSECTS_HAHA_SHOW_THEM_YOUR_TRUE_POWER,
		NpcStringId.MARY_REED_THESE_BRATS_ARE_PRETTY_STRONG_I_SHALL_REST_UP_A_BIT,
		NpcStringId.MARY_REED_THESE_BRATS_ARE_PRETTY_STRONG_WATCH_OUT,
		NpcStringId.MARY_REED_REVEALS_HER_TRUE_POWER
	};
	
	public ThroneOfHeroesMaryReed()
	{
		super(TEMPLATE_ID);
		addInstanceCreatedId(TEMPLATE_ID);
		addStartNpc(ROIENTAL);
		addTalkId(ROIENTAL);
		addAttackId(MARY_REED, MARY_REED_MINION_ZAKEN);
		addKillId(MARY_REED);
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
					showOnScreenMsg(world, NpcStringId.THE_CHALLENGE_FOR_THE_THRONE_OF_HEROES_MARY_REED_WILL_BEGIN_SHORTLY, ExShowScreenMessage.TOP_CENTER, 5000, true);
					startQuestTimer("ANNOUNCE_RAID_PREP", 15000, null, player);
				}
				break;
			}
			case "ANNOUNCE_RAID_PREP":
			{
				final Instance world = player.getInstanceWorld();
				if (isInInstance(world))
				{
					showOnScreenMsg(world, NpcStringId.THE_FIGHTING_AGAINST_MARY_REED_WILL_BEGIN_IN_10_SECONDS, ExShowScreenMessage.TOP_CENTER, 5000, true);
					startQuestTimer("ANNOUNCE_5", 10000, null, player);
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
					startQuestTimer("SPAWN_MARY_REED", 1000, null, player);
				}
				break;
			}
			case "SPAWN_MARY_REED":
			{
				final Instance world = player.getInstanceWorld();
				if (isInInstance(world))
				{
					showOnScreenMsg(world, NpcStringId.MARY_REED_GREETS_HEROES, ExShowScreenMessage.TOP_CENTER, 5000, true);
					world.spawnGroup("MARY_REED");
				}
				break;
			}
			case "ANNOUNCE_MARY_REED_SPAWNS_ZAKEN":
			{
				final Instance world = npc.getInstanceWorld();
				if (isInInstance(world))
				{
					showOnScreenMsg(world, NpcStringId.MARY_REED_SUMMONS_ZAKEN, ExShowScreenMessage.TOP_CENTER, 5000, true);
					startQuestTimer("MARY_REED_SPAWNS_ZAKEN", 10000, npc, null);
				}
				break;
			}
			case "MARY_REED_SPAWNS_ZAKEN":
			{
				final Instance world = npc.getInstanceWorld();
				if (isInInstance(world))
				{
					showOnScreenMsg(world, NpcStringId.ZAKEN_YOUR_TIME_HAS_COME, ExShowScreenMessage.TOP_CENTER, 5000, true);
					world.spawnGroup("MARY_REED_MINION_ZAKEN");
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
			if (npc.getId() == MARY_REED)
			{
				if ((hpPer <= 50) && world.isStatus(0))
				{
					startQuestTimer("ANNOUNCE_MARY_REED_SPAWNS_ZAKEN", 10000, npc, null);
					world.setStatus(1);
				}
				else if ((hpPer <= 30) && world.isStatus(1))
				{
					showOnScreenMsg(world, NpcStringId.DO_YOU_THINK_YOU_CAN_EVADE_MY_STRIKE_TAKE_THAT, ExShowScreenMessage.TOP_CENTER, 5000, true);
					world.setStatus(2);
				}
			}
			else if ((npc.getId() == MARY_REED_MINION_ZAKEN) && world.isStatus(2))
			{
				showOnScreenMsg(world, ZAKEN_MESSAGES[getRandom(4)], ExShowScreenMessage.TOP_CENTER, 5000, true);
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
			if (npc.getId() == MARY_REED)
			{
				// Despawn minions and stop timer
				cancelQuestTimer("ANNOUNCE_MARY_REED_SPAWNS_ZAKEN", npc, killer);
				cancelQuestTimer("MARY_REED_SPAWNS_ZAKEN", npc, killer);
				world.getAliveNpcs(MARY_REED_MINION_ZAKEN).forEach(beast -> beast.doDie(null));
				
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
		new ThroneOfHeroesMaryReed();
	}
}
