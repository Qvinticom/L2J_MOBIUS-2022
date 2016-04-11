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
package quests.Q10791_TheManOfMystery;

import quests.Q10790_AMercenaryHelper.Q10790_AMercenaryHelper;

import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.base.ClassId;
import com.l2jmobius.gameserver.model.holders.ItemHolder;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;
import com.l2jmobius.gameserver.network.serverpackets.ExQuestNpcLogList;

/**
 * The Man of Mystery (10791)
 * @author Stayway
 */
public class Q10791_TheManOfMystery extends Quest
{
	// NPC
	private static final int DOKARA = 33847;
	private static final int VAN_HALTER = 33993;
	// Monsters
	private static final int SUSPICIOUS_COCOON = 27536;
	private static final int SUSPICIOUS_COCOON1 = 27537;
	private static final int SUSPICIOUS_COCOON2 = 27538;
	private static final int NEEDLE_STAKATO_CAPTAIN = 27542;
	private static final int NEEDLE_STAKATO = 27543;
	// Item
	private static final ItemHolder GUILD_COIN = new ItemHolder(37045, 63);
	private static final ItemHolder ENCHANT_ARMOR_A = new ItemHolder(26351, 5);
	// Rewards
	private static final int EXP_REWARD = 16968420;
	private static final int SP_REWARD = 4072;
	// Other
	private static final int MIN_LEVEL = 65;
	private static final int MAX_LEVEL = 70;
	private static final String KILL_VAR = "KillCount";
	
	public Q10791_TheManOfMystery()
	{
		super(10791, Q10791_TheManOfMystery.class.getSimpleName(), "The Man of Mystery");
		addStartNpc(DOKARA);
		addTalkId(DOKARA, VAN_HALTER);
		addKillId(SUSPICIOUS_COCOON, SUSPICIOUS_COCOON1, SUSPICIOUS_COCOON2, NEEDLE_STAKATO_CAPTAIN, NEEDLE_STAKATO);
		addFirstTalkId(VAN_HALTER);
		addCondRace(Race.ERTHEIA, "noErtheia.html");
		addCondClassId(ClassId.MARAUDER, "no_class.html");
		addCondCompletedQuest(Q10790_AMercenaryHelper.class.getSimpleName(), "no_quest.html");
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "33847-02.htm":
			case "33847-03.htm":
			{
				htmltext = event;
				break;
			}
			case "33847-04.htm": // start the quest
			{
				qs.startQuest();
				qs.set(Integer.toString(SUSPICIOUS_COCOON), 0);
				htmltext = event;
				break;
			}
			case "33847-07.html":
			{
				if (qs.isCond(3))
				{
					giveItems(player, GUILD_COIN);
					giveItems(player, ENCHANT_ARMOR_A);
					addExpAndSp(player, EXP_REWARD, SP_REWARD);
					qs.exitQuest(false, true);
					htmltext = event;
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = null;
		final int npcId = npc.getId();
		switch (qs.getState())
		{
			case State.CREATED:
			{
				if ((player.getLevel() < MIN_LEVEL) || (player.getLevel() > MAX_LEVEL))
				{
					htmltext = "no_level.html";
				}
				else
				{
					htmltext = "33847-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					htmltext = "33847-05.html";
				}
				else if ((qs.isCond(3) && (npcId == DOKARA)))
				{
					htmltext = "33847-06.html";
				}
				else if ((qs.isCond(3) && (npcId == VAN_HALTER)))
				{
					htmltext = "33993-02.html";
				}
				break;
			}
			case State.COMPLETED:
			{
				htmltext = getAlreadyCompletedMsg(player);
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = null;
		
		switch (npc.getId())
		{
			case VAN_HALTER:
			{
				if (qs.isCond(3))
				{
					htmltext = "33993-01.html";
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		
		if ((qs != null) && qs.isCond(1))
		{
			switch (npc.getId())
			{
				case SUSPICIOUS_COCOON:
				case SUSPICIOUS_COCOON1:
				case SUSPICIOUS_COCOON2:
				{
					int kills = qs.getInt(Integer.toString(SUSPICIOUS_COCOON));
					kills++;
					qs.set(Integer.toString(SUSPICIOUS_COCOON), kills);
					final ExQuestNpcLogList log = new ExQuestNpcLogList(getId());
					log.addNpc(SUSPICIOUS_COCOON, kills);
					killer.sendPacket(log);
					if (kills >= 5)
					{
						qs.setCond(2);
						addAttackDesire(addSpawn(NEEDLE_STAKATO_CAPTAIN, npc.getLocation()), killer);
						addAttackDesire(addSpawn(NEEDLE_STAKATO, npc.getLocation()), killer);
						addAttackDesire(addSpawn(NEEDLE_STAKATO, npc.getLocation()), killer);
						addAttackDesire(addSpawn(NEEDLE_STAKATO, npc.getLocation()), killer);
						addAttackDesire(addSpawn(NEEDLE_STAKATO, npc.getLocation()), killer);
					}
				}
				
				case NEEDLE_STAKATO_CAPTAIN:
				{
					final int killCount = qs.getInt(KILL_VAR) + 1;
					if ((killCount == 1) && (qs.isCond(2)))
					{
						qs.unset(Integer.toString(SUSPICIOUS_COCOON));
						addSpawn(VAN_HALTER, npc.getX() + 20, npc.getY() + 20, npc.getZ(), npc.getHeading(), false, 80000);
						qs.setCond(3);
					}
					break;
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}