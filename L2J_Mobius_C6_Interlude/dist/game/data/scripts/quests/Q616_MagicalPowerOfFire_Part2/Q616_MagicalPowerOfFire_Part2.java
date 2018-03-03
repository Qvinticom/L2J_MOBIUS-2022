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
package quests.Q616_MagicalPowerOfFire_Part2;

import java.util.logging.Level;

import com.l2jmobius.commons.util.Rnd;
import com.l2jmobius.gameserver.instancemanager.RaidBossSpawnManager;
import com.l2jmobius.gameserver.instancemanager.RaidBossSpawnManager.StatusEnum;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;

public class Q616_MagicalPowerOfFire_Part2 extends Quest
{
	private static final String qn = "Q616_MagicalPowerOfFire_Part2";
	
	// Monster
	private static final int SOUL_OF_FIRE_NASTRON = 25306;
	
	// NPCs
	private static final int UDAN_MARDUI = 31379;
	private static final int KETRAS_HOLY_ALTAR = 31558;
	
	// Items
	private static final int RED_TOTEM = 7243;
	private static final int FIRE_HEART_OF_NASTRON = 7244;
	
	// Other
	private static final int CHECK_INTERVAL = 600000; // 10 minutes
	private static final int IDLE_INTERVAL = 2; // (X * CHECK_INTERVAL) = 20 minutes
	
	private L2NpcInstance _npc = null;
	private int _status = -1;
	
	public Q616_MagicalPowerOfFire_Part2()
	{
		super(616, qn, "Magical Power of Fire - Part 2");
		
		registerQuestItems(FIRE_HEART_OF_NASTRON);
		
		addStartNpc(UDAN_MARDUI);
		addTalkId(UDAN_MARDUI, KETRAS_HOLY_ALTAR);
		
		addAttackId(SOUL_OF_FIRE_NASTRON);
		addKillId(SOUL_OF_FIRE_NASTRON);
		
		switch (RaidBossSpawnManager.getInstance().getRaidBossStatusId(SOUL_OF_FIRE_NASTRON))
		{
			case UNDEFINED:
				LOGGER.log(Level.WARNING, qn + ": can not find spawned L2RaidBoss id=" + SOUL_OF_FIRE_NASTRON);
				break;
			
			case ALIVE:
				spawnNpc();
			case DEAD:
				startQuestTimer("check", CHECK_INTERVAL, null, null, true);
				break;
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		// global quest timer has player==null -> cannot get QuestState
		if (event.equals("check"))
		{
			L2RaidBossInstance raid = RaidBossSpawnManager.getInstance().getBosses().get(SOUL_OF_FIRE_NASTRON);
			if ((raid != null) && (raid.getRaidStatus() == StatusEnum.ALIVE))
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
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		// Udan Mardui
		if (event.equals("31379-04.htm"))
		{
			if (st.hasQuestItems(RED_TOTEM))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound(QuestState.SOUND_ACCEPT);
			}
			else
			{
				htmltext = "31379-02.htm";
			}
		}
		else if (event.equals("31379-08.htm"))
		{
			if (st.hasQuestItems(FIRE_HEART_OF_NASTRON))
			{
				st.takeItems(FIRE_HEART_OF_NASTRON, 1);
				st.rewardExpAndSp(10000, 0);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
			}
			else
			{
				htmltext = "31379-09.htm";
			}
		}
		// Ketra's Holy Altar
		else if (event.equals("31558-02.htm"))
		{
			if (st.hasQuestItems(RED_TOTEM))
			{
				if (_status < 0)
				{
					if (spawnRaid())
					{
						st.set("cond", "2");
						st.playSound(QuestState.SOUND_MIDDLE);
						st.takeItems(RED_TOTEM, 1);
					}
				}
				else
				{
					htmltext = "31558-04.htm";
				}
			}
			else
			{
				htmltext = "31558-03.htm";
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg();
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
				if (!st.hasQuestItems(RED_TOTEM))
				{
					htmltext = "31379-02.htm";
				}
				else if ((player.getLevel() < 75) && (player.getAllianceWithVarkaKetra() > -2))
				{
					htmltext = "31379-03.htm";
				}
				else
				{
					htmltext = "31379-01.htm";
				}
				break;
			
			case State.STARTED:
				final int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case UDAN_MARDUI:
						if (cond == 1)
						{
							htmltext = "31379-05.htm";
						}
						else if (cond == 2)
						{
							htmltext = "31379-06.htm";
						}
						else
						{
							htmltext = "31379-07.htm";
						}
						break;
					
					case KETRAS_HOLY_ALTAR:
						if (cond == 1)
						{
							htmltext = "31558-01.htm";
						}
						else if (cond == 2)
						{
							htmltext = "31558-05.htm";
						}
						break;
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		_status = IDLE_INTERVAL;
		return null;
	}
	
	@Override
	public String onKill(L2NpcInstance npc, L2PcInstance player, boolean isPet)
	{
		for (L2PcInstance partyMember : getPartyMembers(player, npc, "cond", "2"))
		{
			QuestState st = partyMember.getQuestState(qn);
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(FIRE_HEART_OF_NASTRON, 1);
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
			_npc = addSpawn(KETRAS_HOLY_ALTAR, 142368, -82512, -6487, 58000, false, 0);
		}
	}
	
	private boolean spawnRaid()
	{
		L2RaidBossInstance raid = RaidBossSpawnManager.getInstance().getBosses().get(SOUL_OF_FIRE_NASTRON);
		if ((raid != null) && (raid.getRaidStatus() == StatusEnum.ALIVE))
		{
			// set temporarily spawn location (to provide correct behavior of L2RaidBossInstance.checkAndReturnToSpawn())
			raid.getSpawn().setLoc(142624, -82285, -6491, Rnd.get(65536));
			
			// teleport raid from secret place
			raid.teleToLocation(142624, -82285, -6491);
			raid.setHeading(100);
			
			// set raid status
			_status = IDLE_INTERVAL;
			
			return true;
		}
		
		return false;
	}
	
	private void despawnRaid(L2NpcInstance raid)
	{
		// reset spawn location
		raid.getSpawn().setLoc(-105300, -252700, -15542, 0);
		
		// teleport raid back to secret place
		if (!raid.isDead())
		{
			raid.teleToLocation(-105300, -252700, -15542);
		}
		
		// reset raid status
		_status = -1;
	}
}