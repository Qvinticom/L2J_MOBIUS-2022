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
package instances.FortressOfTheDeadInstance;

import com.l2jmobius.gameserver.instancemanager.InstanceManager;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.instancezone.InstanceWorld;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.network.NpcStringId;
import com.l2jmobius.gameserver.network.serverpackets.ExQuestNpcLogList;
import com.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import instances.AbstractInstance;
import quests.Q10752_WindsOfFateAPromise.Q10752_WindsOfFateAPromise;

/**
 * Fortress of The Dead Instance Zone.
 * @author Gigi
 */
public final class FortressOfTheDeadInstance extends AbstractInstance
{
	// NPCs
	private static final int KAIN_VAN_HALTER = 33979;
	private static final int MYSTERIOUS_WIZARD_2 = 33980;
	// Monsters
	private static final int VON_HELLMANN = 19566;
	private static final int VAMPIRIC_SOLDIER = 19567;
	// Locations
	private static final Location START_LOC = new Location(57972, -28955, 568);
	private static final Location EXIT_LOC = new Location(52084, -51317, -3096);
	// Misc
	private static final int TEMPLATE_ID = 254;
	private static final int KAINS_PROPHECY_MACHINE_FRAGMENT = 39538;
	private static final int VAMPIRIC_SOLDIER_KILL = 4;
	private static final int VON_HELLMANN_KILL = 1;
	
	class FTODWorld extends InstanceWorld
	{
	}
	
	public FortressOfTheDeadInstance()
	{
		super(FortressOfTheDeadInstance.class.getSimpleName());
		addTalkId(KAIN_VAN_HALTER, MYSTERIOUS_WIZARD_2);
		addFirstTalkId(KAIN_VAN_HALTER, MYSTERIOUS_WIZARD_2);
		addKillId(VAMPIRIC_SOLDIER, VON_HELLMANN);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = player.getQuestState(Q10752_WindsOfFateAPromise.class.getSimpleName());
		final InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
		if (qs == null)
		{
			return null;
		}
		String htmltext = null;
		switch (event)
		{
			case "33979-02.html":
			case "33979-03.html":
			case "33979-04.html":
			case "33979-05.html":
			case "33979-06.html":
			case "33979-07.html":
			case "33979-08.html":
			case "33979-09.html":
			case "33979-10.html":
			{
				htmltext = event;
				break;
			}
			case "33979-11.html":
			{
				if (tmpworld instanceof FTODWorld)
				{
					final FTODWorld world = (FTODWorld) tmpworld;
					showOnScreenMsg(player, NpcStringId.TALK_TO_THE_MYSTERIOUS_WIZARD, ExShowScreenMessage.TOP_CENTER, 10000);
					L2Npc wizard = addSpawn(MYSTERIOUS_WIZARD_2, npc.getX() + getRandom(-50, 50), npc.getY() + getRandom(-50, 50), npc.getZ(), npc.getHeading(), true, 60000, false, world.getInstanceId());
					wizard.setTitle(player.getName());
					wizard.setIsRunning(false);
					wizard.broadcastInfo();
					npc.deleteMe();
				}
				htmltext = event;
				break;
			}
			case "exit_instance":
			{
				npc.deleteMe();
				player.showQuestMovie(111);
				giveItems(player, KAINS_PROPHECY_MACHINE_FRAGMENT, 1);
				qs.setCond(9, true);
				startQuestTimer("TELEPORT", 27000, npc, player);
				break;
			}
			case "enter_instance":
			{
				enterInstance(player, new FTODWorld(), "FortressOfTheDead.xml", TEMPLATE_ID);
				qs.set(Integer.toString(VAMPIRIC_SOLDIER), 0);
				break;
			}
			case "SPAWN_HELLMANN":
			{
				if (tmpworld instanceof FTODWorld)
				{
					final FTODWorld world = (FTODWorld) tmpworld;
					L2Npc hellmann = addSpawn(VON_HELLMANN, 57963, -28676, 568, 49980, false, 300000, false, world.getInstanceId());
					addAttackDesire(hellmann, player);
				}
				break;
			}
			case "SPAWN_KAIN_VAN_HALTER":
			{
				if (tmpworld instanceof FTODWorld)
				{
					final FTODWorld world = (FTODWorld) tmpworld;
					addSpawn(KAIN_VAN_HALTER, 57963, -28676, 568, 49980, false, 300000, false, world.getInstanceId());
				}
				break;
			}
			case "TELEPORT":
			{
				tmpworld.removeAllowed(player.getObjectId());
				teleportPlayer(player, EXIT_LOC, 0);
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		switch (npc.getId())
		{
			case KAIN_VAN_HALTER:
			{
				htmltext = "33979-01.html";
				break;
			}
			case MYSTERIOUS_WIZARD_2:
			{
				htmltext = "33980-01.html";
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return npc.getId() + ".htm";
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		final QuestState qs = player.getQuestState(Q10752_WindsOfFateAPromise.class.getSimpleName());
		if ((qs != null) && (qs.isCond(8)))
		{
			switch (npc.getId())
			{
				case VAMPIRIC_SOLDIER:
				{
					int kills = qs.getInt(Integer.toString(VAMPIRIC_SOLDIER));
					if (kills < VAMPIRIC_SOLDIER_KILL)
					{
						kills++;
						qs.set(Integer.toString(VAMPIRIC_SOLDIER), kills);
					}
					break;
				}
				case VON_HELLMANN:
				{
					int kills = qs.getInt(Integer.toString(VON_HELLMANN));
					if (kills < VON_HELLMANN_KILL)
					{
						kills++;
						qs.set(Integer.toString(VON_HELLMANN), kills);
					}
					break;
				}
			}
			final ExQuestNpcLogList log = new ExQuestNpcLogList(getId());
			log.addNpc(VAMPIRIC_SOLDIER, qs.getInt(Integer.toString(VAMPIRIC_SOLDIER)));
			log.addNpc(VON_HELLMANN, qs.getInt(Integer.toString(VON_HELLMANN)));
			if (qs.getInt(Integer.toString(VAMPIRIC_SOLDIER)) >= VAMPIRIC_SOLDIER_KILL)
			{
				qs.unset(Integer.toString(VAMPIRIC_SOLDIER));
				startQuestTimer("SPAWN_HELLMANN", 8000, npc, player);
			}
			if (qs.getInt(Integer.toString(VON_HELLMANN)) >= VON_HELLMANN_KILL)
			{
				npc.deleteMe();
				qs.unset(Integer.toString(VON_HELLMANN));
				player.showQuestMovie(110);
				startQuestTimer("SPAWN_KAIN_VAN_HALTER", 5000, npc, player);
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public void onEnterInstance(L2PcInstance player, InstanceWorld world, boolean firstEntrance)
	{
		if (firstEntrance)
		{
			world.addAllowed(player.getObjectId());
		}
		teleportPlayer(player, START_LOC, world.getInstanceId());
	}
}