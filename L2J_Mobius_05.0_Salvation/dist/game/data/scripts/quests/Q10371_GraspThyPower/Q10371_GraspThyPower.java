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
package quests.Q10371_GraspThyPower;

import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.serverpackets.ExQuestNpcLogList;

import quests.Q10370_MenacingTimes.Q10370_MenacingTimes;

/**
 * Grasp Thy Power (10371)
 * @URL https://l2wiki.com/Grasp_Thy_Power
 * @author Gigi
 */
public class Q10371_GraspThyPower extends Quest
{
	// NPCs
	private static final int GERKENSHTEIN = 33648;
	// Monster's
	private static final int SUCCUBUS_SOLDIER = 23181;
	private static final int SUCCUBUS_WARRIOR = 23182;
	private static final int SUCCUBUS_ARCHER = 23183;
	private static final int SUCCUBUS_SHAMAN = 23184;
	private static final int BLOODY_SUCCUBUS = 23185;
	// Items
	private static final ItemHolder ADENA = new ItemHolder(57, 484990);
	// Reward
	private static final int EXP_REWARD = 22641900;
	private static final int SP_REWARD = 5434;
	// Misc
	private static final int MIN_LEVEL = 76;
	private static final int MAX_LEVEL = 81;
	
	public Q10371_GraspThyPower()
	{
		super(10371);
		addStartNpc(GERKENSHTEIN);
		addTalkId(GERKENSHTEIN);
		addKillId(SUCCUBUS_SOLDIER, SUCCUBUS_WARRIOR, SUCCUBUS_ARCHER, SUCCUBUS_SHAMAN, BLOODY_SUCCUBUS);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "no_level.html");
		addCondCompletedQuest(Q10370_MenacingTimes.class.getSimpleName(), "restriction.html");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		String htmltext = null;
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return getNoQuestMsg(player);
		}
		
		switch (event)
		{
			case "33648-02.htm":
			case "33648-03.htm":
			case "33648-07.html":
			{
				htmltext = event;
				break;
			}
			case "33648-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "33648-08.html":
			{
				addExpAndSp(player, EXP_REWARD, SP_REWARD);
				giveItems(player, ADENA);
				qs.exitQuest(false, true);
				htmltext = event;
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		if ((qs.isCreated()) && player.isAwakenedClass())
		{
			htmltext = "33648-01.htm";
		}
		else if (qs.isCond(1))
		{
			htmltext = "33648-05.html";
		}
		else if (qs.isCond(2))
		{
			htmltext = "33648-06.html";
		}
		else if (qs.isCompleted())
		{
			htmltext = "complete.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance killer, boolean isSummon)
	{
		final QuestState qs = getRandomPartyMemberState(killer, 1, 3, npc);
		if (qs != null)
		{
			switch (npc.getId())
			{
				case SUCCUBUS_SOLDIER:
				{
					int kills = qs.getInt(Integer.toString(SUCCUBUS_SOLDIER));
					if (kills < 12)
					{
						kills++;
						qs.set(Integer.toString(SUCCUBUS_SOLDIER), kills);
						playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
					break;
				}
				case SUCCUBUS_WARRIOR:
				{
					int kills = qs.getInt(Integer.toString(SUCCUBUS_WARRIOR));
					if (kills < 12)
					{
						kills++;
						qs.set(Integer.toString(SUCCUBUS_WARRIOR), kills);
						playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
					break;
				}
				case SUCCUBUS_ARCHER:
				{
					int kills = qs.getInt(Integer.toString(SUCCUBUS_ARCHER));
					if (kills < 8)
					{
						kills++;
						qs.set(Integer.toString(SUCCUBUS_ARCHER), kills);
						playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
					break;
				}
				case SUCCUBUS_SHAMAN:
				{
					int kills = qs.getInt(Integer.toString(SUCCUBUS_SHAMAN));
					if (kills < 8)
					{
						kills++;
						qs.set(Integer.toString(SUCCUBUS_SHAMAN), kills);
						playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
					break;
				}
				case BLOODY_SUCCUBUS:
				{
					int kills = qs.getInt(Integer.toString(BLOODY_SUCCUBUS));
					if (kills < 5)
					{
						kills++;
						qs.set(Integer.toString(BLOODY_SUCCUBUS), kills);
						playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
					break;
				}
			}
			
			final ExQuestNpcLogList log = new ExQuestNpcLogList(getId());
			log.addNpc(SUCCUBUS_SOLDIER, qs.getInt(Integer.toString(SUCCUBUS_SOLDIER)));
			log.addNpc(SUCCUBUS_WARRIOR, qs.getInt(Integer.toString(SUCCUBUS_WARRIOR)));
			log.addNpc(SUCCUBUS_ARCHER, qs.getInt(Integer.toString(SUCCUBUS_ARCHER)));
			log.addNpc(SUCCUBUS_SHAMAN, qs.getInt(Integer.toString(SUCCUBUS_SHAMAN)));
			log.addNpc(BLOODY_SUCCUBUS, qs.getInt(Integer.toString(BLOODY_SUCCUBUS)));
			qs.getPlayer().sendPacket(log);
			
			if ((qs.getInt(Integer.toString(SUCCUBUS_SOLDIER)) >= 12) && (qs.getInt(Integer.toString(SUCCUBUS_WARRIOR)) >= 12) && (qs.getInt(Integer.toString(SUCCUBUS_ARCHER)) >= 8) && (qs.getInt(Integer.toString(SUCCUBUS_SHAMAN)) >= 8) && (qs.getInt(Integer.toString(BLOODY_SUCCUBUS)) >= 5))
			{
				qs.setCond(2, true);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}