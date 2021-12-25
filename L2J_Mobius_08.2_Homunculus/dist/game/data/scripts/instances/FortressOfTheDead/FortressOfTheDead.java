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
package instances.FortressOfTheDead;

import org.l2jmobius.gameserver.enums.Movie;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import instances.AbstractInstance;
import quests.Q11026_PathOfDestinyConviction.Q11026_PathOfDestinyConviction;

/**
 * Fortress of the Dead instance zone.
 * @author Gladicek
 */
public class FortressOfTheDead extends AbstractInstance
{
	// NPCs
	private static final int VAMPIRIC_SOLDIER = 19567;
	private static final int VON_HELLMAN = 19566;
	private static final int MYSTERIOUS_WIZARD = 33980;
	private static final int KAIN_VAN_HALTER = 33979;
	// Items
	private static final int KAIN_PROPHECY_MACHINE_FRAGMENT = 39538;
	// Location
	private static final Location VON_HELLMAN_LOC = new Location(57963, -28676, 568, 49980);
	private static final Location MYSTERIOUS_WIZARD_LOC = new Location(57982, -28645, 568);
	private static final Location KAIN_VAN_HALTER_LOC = new Location(57963, -28676, 568, 49980);
	// Misc
	private static final int TEMPLATE_ID = 254;
	
	public FortressOfTheDead()
	{
		super(TEMPLATE_ID);
		addStartNpc(KAIN_VAN_HALTER);
		addFirstTalkId(KAIN_VAN_HALTER, MYSTERIOUS_WIZARD);
		addTalkId(KAIN_VAN_HALTER, MYSTERIOUS_WIZARD);
		addKillId(VAMPIRIC_SOLDIER, VON_HELLMAN);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		if (event.equals("enterInstance"))
		{
			enterInstance(player, npc, TEMPLATE_ID);
		}
		else
		{
			final Instance world = npc.getInstanceWorld();
			if (isInInstance(world))
			{
				switch (event)
				{
					case "33979-01.html":
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
					case "exitInstance":
					{
						world.finishInstance(0);
						break;
					}
					case "vampire_dead":
					{
						addSpawn(VON_HELLMAN, VON_HELLMAN_LOC, false, 0, false, world.getId());
						break;
					}
					case "hellman_dead":
					{
						addSpawn(KAIN_VAN_HALTER, KAIN_VAN_HALTER_LOC, false, 0, false, world.getId());
						break;
					}
					case "spawnWizard":
					{
						showOnScreenMsg(player, NpcStringId.TALK_TO_THE_MYSTERIOUS_WIZARD, ExShowScreenMessage.TOP_CENTER, 5000);
						final Npc wizzard = addSpawn(MYSTERIOUS_WIZARD, MYSTERIOUS_WIZARD_LOC, true, 0, false, world.getId());
						wizzard.setSummoner(player);
						wizzard.setTitle(player.getAppearance().getVisibleName());
						wizzard.broadcastInfo();
						htmltext = "33979-11.html";
						break;
					}
					case "endCinematic":
					{
						final QuestState qs = player.getQuestState(Q11026_PathOfDestinyConviction.class.getSimpleName());
						if ((qs != null) && qs.isCond(18))
						{
							qs.setCond(19, true);
							giveItems(player, KAIN_PROPHECY_MACHINE_FRAGMENT, 1);
						}
						world.getNpc(KAIN_VAN_HALTER).deleteMe();
						world.getNpc(MYSTERIOUS_WIZARD).deleteMe();
						playMovie(player, Movie.ERT_QUEST_B);
						startQuestTimer("exitInstance", 25000, npc, player);
						break;
					}
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		final Instance world = npc.getInstanceWorld();
		if (isInInstance(world))
		{
			if (npc.getId() == VAMPIRIC_SOLDIER)
			{
				if (world.getAliveNpcCount(VAMPIRIC_SOLDIER) == 0)
				{
					startQuestTimer("vampire_dead", 180, npc, player);
				}
			}
			else if (npc.getId() == VON_HELLMAN)
			{
				npc.deleteMe();
				playMovie(player, Movie.ERT_QUEST_A);
				startQuestTimer("hellman_dead", 180, npc, player);
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		final QuestState qs = player.getQuestState(Q11026_PathOfDestinyConviction.class.getSimpleName());
		String htmltext = null;
		switch (npc.getId())
		{
			case KAIN_VAN_HALTER:
			{
				if ((qs != null) && qs.isCond(18))
				{
					htmltext = "33979.html";
				}
				break;
			}
			case MYSTERIOUS_WIZARD:
			{
				if ((qs != null) && qs.isCond(18))
				{
					htmltext = "33980.html";
				}
				break;
			}
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new FortressOfTheDead();
	}
}
