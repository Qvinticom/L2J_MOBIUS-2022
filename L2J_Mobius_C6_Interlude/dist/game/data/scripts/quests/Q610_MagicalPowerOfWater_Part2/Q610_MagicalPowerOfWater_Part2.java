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
package quests.Q610_MagicalPowerOfWater_Part2;

import java.util.logging.Level;

import org.l2jmobius.gameserver.enums.RaidBossStatus;
import org.l2jmobius.gameserver.instancemanager.RaidBossSpawnManager;
import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.actor.instance.RaidBossInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

public class Q610_MagicalPowerOfWater_Part2 extends Quest
{
	// NPCs
	private static final int ASEFA = 31372;
	private static final int VARKAS_HOLY_ALTAR = 31560;
	// Monster
	private static final int SOUL_OF_WATER_ASHUTAR = 25316;
	// Items
	private static final int GREEN_TOTEM = 7238;
	private static final int ICE_HEART_OF_ASHUTAR = 7239;
	// Other
	private static final int CHECK_INTERVAL = 600000; // 10 minutes
	private static final int IDLE_INTERVAL = 2; // (X * CHECK_INTERVAL) = 20 minutes
	private NpcInstance _npc = null;
	private int _status = -1;
	
	public Q610_MagicalPowerOfWater_Part2()
	{
		super(610, "Magical Power of Water - Part 2");
		registerQuestItems(ICE_HEART_OF_ASHUTAR);
		addStartNpc(ASEFA);
		addTalkId(ASEFA, VARKAS_HOLY_ALTAR);
		addAttackId(SOUL_OF_WATER_ASHUTAR);
		addKillId(SOUL_OF_WATER_ASHUTAR);
		
		switch (RaidBossSpawnManager.getInstance().getRaidBossStatusId(SOUL_OF_WATER_ASHUTAR))
		{
			case UNDEFINED:
			{
				LOGGER.log(Level.WARNING, getName() + ": can not find spawned RaidBoss id=" + SOUL_OF_WATER_ASHUTAR);
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
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		// global quest timer has player==null -> cannot get QuestState
		if (event.equals("check"))
		{
			final RaidBossInstance raid = RaidBossSpawnManager.getInstance().getBosses().get(SOUL_OF_WATER_ASHUTAR);
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
			case "31372-04.htm":
			{
				if (st.hasQuestItems(GREEN_TOTEM))
				{
					st.startQuest();
				}
				else
				{
					htmltext = "31372-02.htm";
				}
				break;
			}
			case "31372-07.htm":
			{
				if (st.hasQuestItems(ICE_HEART_OF_ASHUTAR))
				{
					st.takeItems(ICE_HEART_OF_ASHUTAR, 1);
					st.rewardExpAndSp(10000, 0);
					st.playSound(QuestState.SOUND_FINISH);
					st.exitQuest(true);
				}
				else
				{
					htmltext = "31372-08.htm";
				}
				break;
			}
			case "31560-02.htm":
			{
				if (st.hasQuestItems(GREEN_TOTEM))
				{
					if (_status < 0)
					{
						if (spawnRaid())
						{
							st.setCond(2);
							st.playSound(QuestState.SOUND_MIDDLE);
							st.takeItems(GREEN_TOTEM, 1);
						}
					}
					else
					{
						htmltext = "31560-04.htm";
					}
				}
				else
				{
					htmltext = "31560-03.htm";
				}
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, PlayerInstance player)
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
				if (!st.hasQuestItems(GREEN_TOTEM))
				{
					htmltext = "31372-02.htm";
				}
				else if ((player.getLevel() < 75) && (player.getAllianceWithVarkaKetra() < 2))
				{
					htmltext = "31372-03.htm";
				}
				else
				{
					htmltext = "31372-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				final int cond = st.getCond();
				switch (npc.getNpcId())
				{
					case ASEFA:
					{
						htmltext = (cond < 3) ? "31372-05.htm" : "31372-06.htm";
						break;
					}
					case VARKAS_HOLY_ALTAR:
					{
						if (cond == 1)
						{
							htmltext = "31560-01.htm";
						}
						else if (cond == 2)
						{
							htmltext = "31560-05.htm";
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
	public String onAttack(NpcInstance npc, PlayerInstance attacker, int damage, boolean isPet)
	{
		_status = IDLE_INTERVAL;
		return null;
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance player, boolean isPet)
	{
		for (PlayerInstance partyMember : getPartyMembers(player, npc, 2))
		{
			final QuestState st = partyMember.getQuestState(getName());
			if (st == null)
			{
				continue;
			}
			
			st.setCond(3);
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(ICE_HEART_OF_ASHUTAR, 1);
		}
		
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
			_npc = addSpawn(VARKAS_HOLY_ALTAR, 105452, -36775, -1050, 34000, false, 0);
		}
	}
	
	private boolean spawnRaid()
	{
		final RaidBossInstance raid = RaidBossSpawnManager.getInstance().getBosses().get(SOUL_OF_WATER_ASHUTAR);
		if ((raid != null) && (raid.getRaidStatus() == RaidBossStatus.ALIVE))
		{
			// set temporarily spawn location (to provide correct behavior of RaidBossInstance.checkAndReturnToSpawn())
			// raid.getSpawn().setLoc(104771, -36993, -1149, Rnd.get(65536));
			
			addSpawn(SOUL_OF_WATER_ASHUTAR, 104771, -36993, -1149, 100, false, 0);
			raid.broadcastNpcSay("You didn’t come into this world. You came out of it, like a wave from the ocean. You are not a stranger here.");
			
			// set raid status
			_status = IDLE_INTERVAL;
			
			return true;
		}
		return false;
	}
	
	private void despawnRaid(NpcInstance raid)
	{
		// reset spawn location
		raid.getSpawn().setLoc(-105900, -252700, -15542, 0);
		
		// teleport raid back to secret place
		if (!raid.isDead())
		{
			raid.teleToLocation(-105900, -252700, -15542);
		}
		
		// reset raid status
		_status = -1;
	}
}