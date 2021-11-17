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
package quests.Q625_TheFinestIngredients_Part2;

import java.util.logging.Level;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.enums.RaidBossStatus;
import org.l2jmobius.gameserver.instancemanager.RaidBossSpawnManager;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.RaidBoss;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

public class Q625_TheFinestIngredients_Part2 extends Quest
{
	// NPCs
	private static final int JEREMY = 31521;
	private static final int YETI_TABLE = 31542;
	// Monster
	private static final int ICICLE_EMPEROR_BUMBALUMP = 25296;
	// Items
	private static final int SOY_SAUCE_JAR = 7205;
	private static final int FOOD_FOR_BUMBALUMP = 7209;
	private static final int SPECIAL_YETI_MEAT = 7210;
	private static final int[] REWARD_DYE =
	{
		4589,
		4590,
		4591,
		4592,
		4593,
		4594
	};
	// Other
	private static final int CHECK_INTERVAL = 600000; // 10 minutes
	private static final int IDLE_INTERVAL = 3; // (X * CHECK_INTERVAL) = 30 minutes
	private Npc _npc = null;
	private int _status = -1;
	
	public Q625_TheFinestIngredients_Part2()
	{
		super(625, "The Finest Ingredients - Part 2");
		registerQuestItems(FOOD_FOR_BUMBALUMP, SPECIAL_YETI_MEAT);
		addStartNpc(JEREMY);
		addTalkId(JEREMY, YETI_TABLE);
		addAttackId(ICICLE_EMPEROR_BUMBALUMP);
		addKillId(ICICLE_EMPEROR_BUMBALUMP);
		
		switch (RaidBossSpawnManager.getInstance().getRaidBossStatusId(ICICLE_EMPEROR_BUMBALUMP))
		{
			case UNDEFINED:
			{
				LOGGER.log(Level.WARNING, getName() + ": can not find spawned RaidBoss id=" + ICICLE_EMPEROR_BUMBALUMP);
				break;
			}
			case ALIVE:
			{
				spawnNpc();
				// fallthrough
			}
			case DEAD:
			{
				startQuestTimer("check", CHECK_INTERVAL, null, null, true);
				break;
			}
		}
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		// global quest timer has player==null -> cannot get QuestState
		if (event.equals("check"))
		{
			final RaidBoss raid = RaidBossSpawnManager.getInstance().getBosses().get(ICICLE_EMPEROR_BUMBALUMP);
			if ((raid != null) && (raid.getRaidStatus() == RaidBossStatus.ALIVE))
			{
				if ((_status >= 0) && (_status-- == 0))
				{
					despawnRaid(raid);
				}
				
				spawnNpc();
			}
			
			return null;
		}
		
		String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "31521-03.htm":
			{
				if (st.hasQuestItems(SOY_SAUCE_JAR))
				{
					st.startQuest();
					st.takeItems(SOY_SAUCE_JAR, 1);
					st.giveItems(FOOD_FOR_BUMBALUMP, 1);
				}
				else
				{
					htmltext = "31521-04.htm";
				}
				break;
			}
			case "31521-08.htm":
			{
				if (st.hasQuestItems(SPECIAL_YETI_MEAT))
				{
					st.takeItems(SPECIAL_YETI_MEAT, 1);
					st.rewardItems(REWARD_DYE[Rnd.get(REWARD_DYE.length)], 5);
					st.playSound(QuestState.SOUND_FINISH);
					st.exitQuest(true);
				}
				else
				{
					htmltext = "31521-09.htm";
				}
				break;
			}
			case "31542-02.htm":
			{
				if (st.hasQuestItems(FOOD_FOR_BUMBALUMP))
				{
					if (_status < 0)
					{
						if (spawnRaid())
						{
							st.setCond(2);
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(FOOD_FOR_BUMBALUMP, 1);
						}
					}
					else
					{
						htmltext = "31542-04.htm";
					}
				}
				else
				{
					htmltext = "31542-03.htm";
				}
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = getNoQuestMsg();
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
			{
				htmltext = (player.getLevel() < 73) ? "31521-02.htm" : "31521-01.htm";
				break;
			}
			case State.STARTED:
			{
				final int cond = st.getCond();
				switch (npc.getNpcId())
				{
					case JEREMY:
					{
						if (cond == 1)
						{
							htmltext = "31521-05.htm";
						}
						else if (cond == 2)
						{
							htmltext = "31521-06.htm";
						}
						else
						{
							htmltext = "31521-07.htm";
						}
						break;
					}
					case YETI_TABLE:
					{
						if (cond == 1)
						{
							htmltext = "31542-01.htm";
						}
						else if (cond == 2)
						{
							htmltext = "31542-05.htm";
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
	public String onAttack(Npc npc, Player attacker, int damage, boolean isPet)
	{
		_status = IDLE_INTERVAL;
		return null;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isPet)
	{
		for (Player partyMember : getPartyMembers(player, npc, 2))
		{
			final QuestState st = partyMember.getQuestState(getName());
			if (st == null)
			{
				continue;
			}
			
			st.setCond(3);
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(SPECIAL_YETI_MEAT, 1);
		}
		
		npc.broadcastNpcSay("Oooh!");
		
		// despawn raid (reset info)
		despawnRaid(npc);
		
		// despawn npc
		if (_npc != null)
		{
			_npc.deleteMe();
			_npc = null;
		}
		
		return null;
	}
	
	private void spawnNpc()
	{
		// spawn npc, if not spawned
		if (_npc == null)
		{
			_npc = addSpawn(YETI_TABLE, 157136, -121456, -2363, 40000, false, 0);
		}
	}
	
	private boolean spawnRaid()
	{
		final RaidBoss raid = RaidBossSpawnManager.getInstance().getBosses().get(ICICLE_EMPEROR_BUMBALUMP);
		if ((raid != null) && (raid.getRaidStatus() == RaidBossStatus.ALIVE))
		{
			// set temporarily spawn location (to provide correct behavior of RaidBoss.checkAndReturnToSpawn())
			// raid.getSpawn().setLoc(157117, -121939, -2397, Rnd.get(65536));
			
			addSpawn(ICICLE_EMPEROR_BUMBALUMP, 157117, -121939, -2397, 100, false, 0);
			raid.broadcastNpcSay("I smell something delicious...");
			
			// set raid status
			_status = IDLE_INTERVAL;
			
			return true;
		}
		return false;
	}
	
	private void despawnRaid(Npc raid)
	{
		// reset spawn location
		raid.getSpawn().setLoc(-104700, -252700, -15542, 0);
		
		// teleport raid back to secret place
		if (!raid.isDead())
		{
			raid.teleToLocation(-104700, -252700, -15542);
		}
		
		// reset raid status
		_status = -1;
	}
}