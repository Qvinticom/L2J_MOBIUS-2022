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
package instances.TalkingIsland;

import com.l2jmobius.gameserver.instancemanager.InstanceManager;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.SkillHolder;
import com.l2jmobius.gameserver.model.instancezone.InstanceWorld;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;
import com.l2jmobius.gameserver.model.skills.Skill;
import com.l2jmobius.gameserver.network.NpcStringId;
import com.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import instances.AbstractInstance;
import quests.Q10385_RedThreadOfFate.Q10385_RedThreadOfFate;

/**
 * @author Stayway
 */
public class TalkingIsland extends AbstractInstance
{
	// NPCs
	private static final int DARK_KNIGHT = 33751;
	private static final int DARIN = 33748;
	private static final int ROXXY = 33749;
	private static final int BIOTIN = 30031;
	private static final int MOTHER_TREE = 33786;
	// Skill
	private static final SkillHolder NPC_TREE = new SkillHolder(9579, 1);
	// Locations
	private static final Location START_LOC = new Location(210705, 13259, -3754);
	private static final Location EXIT_LOC = new Location(-113647, 246016, -3696);
	// Instance
	private static final int TEMPLATE_ID = 241;
	
	class TIWorld extends InstanceWorld
	{
		L2Npc dark = null;
	}
	
	public TalkingIsland()
	{
		super(TalkingIsland.class.getSimpleName());
		addTalkId(DARIN, ROXXY, BIOTIN, DARK_KNIGHT);
		addFirstTalkId(DARIN, ROXXY, BIOTIN, DARK_KNIGHT);
		addSkillSeeId(MOTHER_TREE);
		addSpawnId(DARK_KNIGHT);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final TIWorld world = (TIWorld) InstanceManager.getInstance().getWorld(npc.getInstanceId());
		final QuestState qs = player.getQuestState(Q10385_RedThreadOfFate.class.getSimpleName());
		
		String htmltext = null;
		switch (event)
		{
			case "33751-03.html":
			{
				htmltext = event;
				break;
			}
			case "30031-03.html":
			{
				if (qs.isCond(20) && (player.getInstanceId() == world.getInstanceId()))
				{
					qs.setCond(21);
					spawnGroup("dark", world.getInstanceId());
					showOnScreenMsg(player, NpcStringId.GO_OUTSIDE_THE_TEMPLE, ExShowScreenMessage.TOP_CENTER, 4500);
					final double distance = npc.calculateDistance(player, false, false);
					if ((distance <= 200))
					{
						showOnScreenMsg(player, NpcStringId.A_MYSTERIOUS_DARK_KNIGHT_IS_HERE, ExShowScreenMessage.TOP_CENTER, 4500);
					}
					return "30031-03.html";
				}
				break;
			}
			case "33751-02.html":
			{
				if (qs.isCond(21))
				{
					return "33751-02.html";
				}
				break;
			}
			case "33748-03.html":
			{
				if (qs.isCond(19))
				{
					showOnScreenMsg(player, NpcStringId.SPEAK_WITH_ROXXY, ExShowScreenMessage.TOP_CENTER, 4500);
				}
				break;
			}
			case "exit":
			{
				qs.setCond(22);
				{
					teleportPlayer(player, EXIT_LOC, 0);
					player.showQuestMovie(75);
					break;
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = player.getQuestState(Q10385_RedThreadOfFate.class.getSimpleName());
		String htmltext = null;
		
		switch (qs.getState())
		{
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case DARIN:
					{
						if (qs.isCond(19))
						{
							htmltext = "33748-02.html";
						}
						break;
					}
					case ROXXY:
					{
						if (qs.isCond(19))
						{
							qs.setCond(20);
							htmltext = "33749-02.html";
						}
						break;
					}
					case BIOTIN:
					{
						if (qs.isCond(20))
						{
							htmltext = "30031-02.html";
						}
						break;
					}
					case DARK_KNIGHT:
					{
						if (qs.isCond(21))
						{
							htmltext = "33751-02.html";
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
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = player.getQuestState(Q10385_RedThreadOfFate.class.getSimpleName());
		String htmltext = null;
		if ((qs != null))
		{
			switch (npc.getId())
			{
				case DARIN:
				{
					if (qs.isCond(19))
					{
						htmltext = "33748-01.html";
					}
					break;
				}
				case ROXXY:
				{
					if (qs.isCond(19))
					{
						htmltext = "33749-01.html";
					}
					break;
				}
				case BIOTIN:
				{
					if (qs.isCond(20))
					{
						htmltext = "30031-01.html";
					}
					break;
				}
				case DARK_KNIGHT:
				{
					if (qs.isCond(21))
					{
						htmltext = "33751-01.html";
					}
					break;
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance player, Skill skill, L2Object[] targets, boolean isSummon)
	{
		final QuestState qs = player.getQuestState(Q10385_RedThreadOfFate.class.getSimpleName());
		if ((qs != null) && qs.isCond(19))
		{
			final Skill npcDefault = NPC_TREE.getSkill();
			castSkill(npc, player, npcDefault);
		}
		{
			enterInstance(player, new TIWorld(), "TalkingIsland.xml", TEMPLATE_ID);
		}
		
		return null;
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