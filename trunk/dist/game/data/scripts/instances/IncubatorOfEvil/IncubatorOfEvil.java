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
package instances.IncubatorOfEvil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.ai.npc.FighterAI;
import com.l2jmobius.gameserver.instancemanager.InstanceManager;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2QuestGuardInstance;
import com.l2jmobius.gameserver.model.instancezone.InstanceWorld;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.network.NpcStringId;
import com.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import instances.AbstractInstance;
import quests.Q10341_DayOfDestinyHumansFate.Q10341_DayOfDestinyHumansFate;
import quests.Q10342_DayOfDestinyElvenFate.Q10342_DayOfDestinyElvenFate;
import quests.Q10343_DayOfDestinyDarkElfsFate.Q10343_DayOfDestinyDarkElfsFate;
import quests.Q10344_DayOfDestinyOrcsFate.Q10344_DayOfDestinyOrcsFate;
import quests.Q10345_DayOfDestinyDwarfsFate.Q10345_DayOfDestinyDwarfsFate;
import quests.Q10346_DayOfDestinyKamaelsFate.Q10346_DayOfDestinyKamaelsFate;

/**
 * Incubator of Evil Instance Zone.
 * @author Mobius
 */
public final class IncubatorOfEvil extends AbstractInstance
{
	// NPCs
	private static final int ADOLPH = 33170;
	private static final int ALICE = 33171;
	private static final int BARTON = 33172;
	private static final int HAYUK = 33173;
	private static final int ELIYAH = 33174;
	private static final int ARCHER = 33414;
	private static final int INFANTRY = 33415;
	// Monsters
	private static final int FINAL_BOSS = 27425;
	private static final List<Integer> MONSTERS = new ArrayList<>();
	static
	{
		MONSTERS.add(27430);
		MONSTERS.add(27431);
		MONSTERS.add(27432);
		MONSTERS.add(27433);
		MONSTERS.add(27434);
	}
	// Rewards
	private static final int SOE = 736;
	// Locations
	private static final Location START_LOC = new Location(56180, -172898, -7952);
	private static final Location FIGHT_LOC = new Location(56177, -175627, -7952);
	// Misc
	private static final int TEMPLATE_ID = 185;
	
	class IOEWorld extends InstanceWorld
	{
		L2QuestGuardInstance adolph = null;
		L2QuestGuardInstance alice = null;
		L2QuestGuardInstance barton = null;
		L2QuestGuardInstance hayuk = null;
		L2QuestGuardInstance eliyah = null;
		int selectionCount = 0;
		final List<L2Npc> savedSpawns = new CopyOnWriteArrayList<>();
	}
	
	public IncubatorOfEvil()
	{
		super(IncubatorOfEvil.class.getSimpleName());
		addStartNpc(ADOLPH);
		addTalkId(ADOLPH, BARTON, HAYUK, ELIYAH, ALICE);
		addFirstTalkId(ADOLPH, BARTON, HAYUK, ELIYAH, ALICE, ARCHER, INFANTRY);
		addKillId(MONSTERS);
		addKillId(FINAL_BOSS);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equals("enter_instance"))
		{
			final QuestState qs = getPlayerQuestState(player);
			if ((qs == null) || (qs.getCond() < 4) || qs.isCond(13))
			{
				return null;
			}
			qs.setCond(5, true);
			enterInstance(player, new IOEWorld(), "IncubatorOfEvil.xml", TEMPLATE_ID);
			return null;
		}
		
		final InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
		if ((tmpworld == null) || !(tmpworld instanceof IOEWorld))
		{
			return null;
		}
		final IOEWorld world = (IOEWorld) tmpworld;
		
		String htmltext = null;
		switch (event)
		{
			case "33170-5.htm":
			{
				htmltext = event;
				break;
			}
			case "33170-3.htm":
			{
				final QuestState qs = getPlayerQuestState(player);
				if ((qs != null) && qs.isCond(5))
				{
					qs.setCond(6, true);
					htmltext = event;
				}
				break;
			}
			case "select_alice":
			{
				htmltext = addVanguard(player, ALICE);
				break;
			}
			case "select_barton":
			{
				htmltext = addVanguard(player, BARTON);
				break;
			}
			case "select_hayuk":
			{
				htmltext = addVanguard(player, HAYUK);
				break;
			}
			case "select_eliyah":
			{
				htmltext = addVanguard(player, ELIYAH);
				break;
			}
			case "start_battle":
			{
				if (world.selectionCount == 2)
				{
					final QuestState qs = getPlayerQuestState(player);
					if ((qs != null) && qs.isCond(7))
					{
						world.setStatus(1);
						qs.setCond(8, true);
						teleportPlayer(player, FIGHT_LOC, world.getInstanceId(), false);
						
						if (world.adolph != null)
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new FighterAI(player, world.adolph), 1000);
						}
						if (world.alice != null)
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new FighterAI(player, world.alice), 1000);
						}
						if (world.barton != null)
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new FighterAI(player, world.barton), 1000);
						}
						if (world.hayuk != null)
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new FighterAI(player, world.hayuk), 1000);
						}
						if (world.eliyah != null)
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new FighterAI(player, world.eliyah), 1000);
						}
						
						final List<L2Npc> archers = spawnGroup("archers", world.getInstanceId());
						for (L2Npc spawn : archers)
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new FighterAI(player, ((L2QuestGuardInstance) spawn)), 1000);
						}
						final List<L2Npc> infantry = spawnGroup("infantry", world.getInstanceId());
						for (L2Npc spawn : infantry)
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new FighterAI(player, ((L2QuestGuardInstance) spawn)), 1000);
						}
						
						world.savedSpawns.addAll(spawnGroup("wave1", world.getInstanceId()));
						for (L2Npc spawn : world.savedSpawns)
						{
							addAttackDesire(spawn, player);
						}
						showScreenMessage(player, NpcStringId.CREATURES_RESURRECTED_DEFEND_YOURSELF);
						
						startQuestTimer("checkStatus", 5000, null, player, true);
					}
				}
				break;
			}
			case "checkStatus":
			{
				// Check if finished.
				if (world.getStatus() > 13)
				{
					final QuestState qs = getPlayerQuestState(player);
					if (qs != null)
					{
						rewardItems(player, SOE, 1);
						qs.setCond(13, true);
					}
					// 5 minute exit timer.
					InstanceManager.getInstance().getInstance(world.getInstanceId()).setDuration(Config.INSTANCE_FINISH_TIME);
					InstanceManager.getInstance().getInstance(world.getInstanceId()).setEmptyDestroyTime(0);
					// Stop quest timer.
					cancelQuestTimers("checkStatus");
					return null;
				}
				// Remove monsters killed by guards.
				for (L2Npc spawn : world.savedSpawns)
				{
					if (spawn.isDead())
					{
						world.savedSpawns.remove(spawn);
						
						if (spawn.getId() == FINAL_BOSS)
						{
							for (L2Npc monster : world.savedSpawns)
							{
								monster.deleteMe();
							}
							world.incStatus();
						}
					}
				}
				// Act according to world status.
				switch (world.getStatus())
				{
					case 1:
					{
						if (world.savedSpawns.isEmpty())
						{
							world.incStatus();
							world.savedSpawns.addAll(spawnGroup("wave2", world.getInstanceId()));
							showScreenMessage(player, NpcStringId.CREATURES_RESURRECTED_DEFEND_YOURSELF);
							for (L2Npc spawn : world.savedSpawns)
							{
								addAttackDesire(spawn, player);
							}
						}
						break;
					}
					case 2:
					{
						if (world.savedSpawns.isEmpty())
						{
							world.incStatus();
							world.savedSpawns.addAll(spawnGroup("wave3", world.getInstanceId()));
							showScreenMessage(player, NpcStringId.CREATURES_RESURRECTED_DEFEND_YOURSELF);
							for (L2Npc spawn : world.savedSpawns)
							{
								addAttackDesire(spawn, player);
							}
						}
						break;
					}
					case 3:
					{
						if (world.savedSpawns.isEmpty())
						{
							world.incStatus();
							world.savedSpawns.addAll(spawnGroup("wave4", world.getInstanceId()));
							showScreenMessage(player, NpcStringId.CREATURES_RESURRECTED_DEFEND_YOURSELF);
							for (L2Npc spawn : world.savedSpawns)
							{
								addAttackDesire(spawn, player);
							}
						}
						break;
					}
					case 4:
					{
						if (world.savedSpawns.isEmpty())
						{
							world.incStatus();
							world.savedSpawns.addAll(spawnGroup("wave5", world.getInstanceId()));
							showScreenMessage(player, NpcStringId.CREATURES_RESURRECTED_DEFEND_YOURSELF);
							for (L2Npc spawn : world.savedSpawns)
							{
								addAttackDesire(spawn, player);
							}
						}
						break;
					}
					case 5:
					{
						if (world.savedSpawns.isEmpty())
						{
							world.incStatus();
							world.savedSpawns.addAll(spawnGroup("wave6", world.getInstanceId()));
							showScreenMessage(player, NpcStringId.CREATURES_RESURRECTED_DEFEND_YOURSELF);
							for (L2Npc spawn : world.savedSpawns)
							{
								addAttackDesire(spawn, player);
							}
						}
						break;
					}
					case 6:
					{
						if (world.savedSpawns.isEmpty())
						{
							world.incStatus();
							world.savedSpawns.addAll(spawnGroup("wave7", world.getInstanceId()));
							showScreenMessage(player, NpcStringId.CREATURES_RESURRECTED_DEFEND_YOURSELF);
							for (L2Npc spawn : world.savedSpawns)
							{
								addAttackDesire(spawn, player);
							}
						}
						break;
					}
					case 7:
					{
						if (world.savedSpawns.isEmpty())
						{
							world.incStatus();
							world.savedSpawns.addAll(spawnGroup("wave8", world.getInstanceId()));
							showScreenMessage(player, NpcStringId.CREATURES_RESURRECTED_DEFEND_YOURSELF);
							for (L2Npc spawn : world.savedSpawns)
							{
								addAttackDesire(spawn, player);
							}
						}
						break;
					}
					case 8:
					{
						if (world.savedSpawns.isEmpty())
						{
							world.incStatus();
							world.savedSpawns.addAll(spawnGroup("wave9", world.getInstanceId()));
							showScreenMessage(player, NpcStringId.CREATURES_RESURRECTED_DEFEND_YOURSELF);
							for (L2Npc spawn : world.savedSpawns)
							{
								addAttackDesire(spawn, player);
							}
						}
						break;
					}
					case 9:
					{
						if (world.savedSpawns.isEmpty())
						{
							world.incStatus();
							world.savedSpawns.addAll(spawnGroup("wave10", world.getInstanceId()));
							showScreenMessage(player, NpcStringId.CREATURES_RESURRECTED_DEFEND_YOURSELF);
							for (L2Npc spawn : world.savedSpawns)
							{
								addAttackDesire(spawn, player);
							}
						}
						break;
					}
					case 10:
					{
						if (world.savedSpawns.isEmpty())
						{
							world.incStatus();
							world.savedSpawns.addAll(spawnGroup("wave11", world.getInstanceId()));
							showScreenMessage(player, NpcStringId.CREATURES_RESURRECTED_DEFEND_YOURSELF);
							for (L2Npc spawn : world.savedSpawns)
							{
								addAttackDesire(spawn, player);
							}
						}
						break;
					}
					case 11:
					{
						if (world.savedSpawns.isEmpty())
						{
							world.incStatus();
							world.savedSpawns.addAll(spawnGroup("wave12", world.getInstanceId()));
							showScreenMessage(player, NpcStringId.CREATURES_RESURRECTED_DEFEND_YOURSELF);
							for (L2Npc spawn : world.savedSpawns)
							{
								addAttackDesire(spawn, player);
							}
						}
						break;
					}
					case 12:
					{
						if (world.savedSpawns.isEmpty())
						{
							world.incStatus();
							world.savedSpawns.addAll(spawnGroup("wave13", world.getInstanceId()));
							showScreenMessage(player, NpcStringId.CREATURES_RESURRECTED_DEFEND_YOURSELF);
							for (L2Npc spawn : world.savedSpawns)
							{
								addAttackDesire(spawn, player);
							}
						}
						break;
					}
					case 13:
					{
						if (world.savedSpawns.isEmpty())
						{
							world.incStatus();
						}
						break;
					}
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getPlayerQuestState(player);
		if (qs == null)
		{
			return null;
		}
		String htmltext = getNoQuestMsg(player);
		switch (npc.getId())
		{
			case ADOLPH:
			{
				switch (qs.getCond())
				{
					case 5:
					{
						htmltext = "33170-2.htm";
						break;
					}
					case 6:
					{
						htmltext = "33170-3.htm";
						break;
					}
				}
				break;
			}
			case ALICE:
			case BARTON:
			case HAYUK:
			case ELIYAH:
			{
				switch (qs.getCond())
				{
					case 6:
					{
						htmltext = npc.getId() + "-2.htm";
						break;
					}
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getPlayerQuestState(player);
		if (qs == null)
		{
			return null;
		}
		
		switch (npc.getId())
		{
			case ADOLPH:
			{
				switch (qs.getCond())
				{
					case 4:
					case 5:
					case 6:
					{
						return "33170-1.htm";
					}
					case 7:
					{
						return "33170-4.htm";
					}
				}
				break;
			}
			case ALICE:
			case BARTON:
			case HAYUK:
			case ELIYAH:
			{
				switch (qs.getCond())
				{
					case 4:
					case 5:
					case 6:
					{
						return npc.getId() + "-1.htm";
					}
					// case 8: ?
				}
				break;
			}
			case ARCHER:
			case INFANTRY:
			{
				return npc.getId() + "-1.htm";
			}
		}
		
		return null;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(killer);
		if ((world == null) || !(world instanceof IOEWorld))
		{
			return null;
		}
		
		((IOEWorld) world).savedSpawns.remove(npc);
		
		if (npc.getId() == FINAL_BOSS)
		{
			for (L2Npc monster : ((IOEWorld) world).savedSpawns)
			{
				monster.deleteMe();
			}
			((IOEWorld) world).incStatus();
		}
		
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public void onEnterInstance(L2PcInstance player, InstanceWorld world, boolean firstEntrance)
	{
		if (firstEntrance)
		{
			world.setStatus(0);
			world.addAllowed(player.getObjectId());
		}
		teleportPlayer(player, START_LOC, world.getInstanceId());
	}
	
	private QuestState getPlayerQuestState(L2PcInstance player)
	{
		QuestState qs = null;
		switch (player.getRace())
		{
			case HUMAN:
			{
				qs = player.getQuestState(Q10341_DayOfDestinyHumansFate.class.getSimpleName());
				break;
			}
			case ELF:
			{
				qs = player.getQuestState(Q10342_DayOfDestinyElvenFate.class.getSimpleName());
				break;
			}
			case DARK_ELF:
			{
				qs = player.getQuestState(Q10343_DayOfDestinyDarkElfsFate.class.getSimpleName());
				break;
			}
			case ORC:
			{
				qs = player.getQuestState(Q10344_DayOfDestinyOrcsFate.class.getSimpleName());
				break;
			}
			case DWARF:
			{
				qs = player.getQuestState(Q10345_DayOfDestinyDwarfsFate.class.getSimpleName());
				break;
			}
			case KAMAEL:
			{
				qs = player.getQuestState(Q10346_DayOfDestinyKamaelsFate.class.getSimpleName());
				break;
			}
		}
		return qs;
	}
	
	private String addVanguard(L2PcInstance player, int addedNpcId)
	{
		final InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if ((world == null) || !(world instanceof IOEWorld) || (world.getStatus() > 1) //
			|| (player.getTarget() == null) || !player.getTarget().isNpc() || (((L2Npc) player.getTarget()).getId() != addedNpcId))
		{
			return null;
		}
		
		switch (addedNpcId)
		{
			case ALICE:
			{
				((L2Character) player.getTarget()).deleteMe();
				((IOEWorld) world).selectionCount++;
				((IOEWorld) world).alice = (L2QuestGuardInstance) addSpawn(ALICE, FIGHT_LOC, true, 0, false, world.getInstanceId());
				break;
			}
			case BARTON:
			{
				((L2Character) player.getTarget()).deleteMe();
				((IOEWorld) world).selectionCount++;
				((IOEWorld) world).barton = (L2QuestGuardInstance) addSpawn(BARTON, FIGHT_LOC, true, 0, false, world.getInstanceId());
				break;
			}
			case HAYUK:
			{
				((L2Character) player.getTarget()).deleteMe();
				((IOEWorld) world).selectionCount++;
				((IOEWorld) world).hayuk = (L2QuestGuardInstance) addSpawn(HAYUK, FIGHT_LOC, true, 0, false, world.getInstanceId());
				break;
			}
			case ELIYAH:
			{
				((L2Character) player.getTarget()).deleteMe();
				((IOEWorld) world).selectionCount++;
				((IOEWorld) world).eliyah = (L2QuestGuardInstance) addSpawn(ELIYAH, FIGHT_LOC, true, 0, false, world.getInstanceId());
				break;
			}
		}
		if (((IOEWorld) world).selectionCount == 2)
		{
			((IOEWorld) world).adolph = (L2QuestGuardInstance) addSpawn(ADOLPH, FIGHT_LOC, true, 0, false, world.getInstanceId());
			final QuestState qs = getPlayerQuestState(player);
			if ((qs != null) && qs.isCond(6))
			{
				qs.setCond(7, true);
			}
			// Remove remaining vanguards.
			for (L2Character ch : player.getKnownList().getKnownCharactersInRadius(500))
			{
				if (ch.isNpc() && (((L2Npc) ch).getId() != ADOLPH))
				{
					ch.deleteMe();
				}
			}
		}
		return null;
	}
	
	private void showScreenMessage(L2PcInstance player, NpcStringId stringId)
	{
		player.sendPacket(new ExShowScreenMessage(2, -1, 2, 0, 0, 0, 0, true, 10000, false, null, stringId, null));
	}
}