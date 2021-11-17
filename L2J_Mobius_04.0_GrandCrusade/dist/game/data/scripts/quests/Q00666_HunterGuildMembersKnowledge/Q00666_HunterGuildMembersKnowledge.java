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
package quests.Q00666_HunterGuildMembersKnowledge;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.enums.Faction;
import org.l2jmobius.gameserver.enums.QuestType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * Knowledgeable Hunter Guild Member (666) Updated for version Fafurion and below Edoo
 * @URL https://l2wiki.com/Knowledgeable_Hunter_Guild_Member
 * @author Dmitri
 */
public class Q00666_HunterGuildMembersKnowledge extends Quest
{
	// NPCs
	private static final int ARCTURUS = 34267;
	private static final int COLIN = 30703;
	// BOSS
	//@formatter:off
	private static final int[] BOSES =
	{ // BOSS LVL 86-97
		25932, 25875, 25879, 26077, 26078,
		26079, 26080, 26081, 26082, 26011,
		26012, 26013, 26014, 26015, 26016,
		25944, 3477, 25696, 25697, 25698,
		26000, 26001, 26002, 26003, 26004,
		26005, 3479, 25933, 26055, 26056,
		26057, 26058, 26059, 26060, 25989,
		25990, 25991, 25992, 25993, 25994,
		25943, 26066, 26067, 26068, 26069,
		26070, 26071, 3481, 25937, 25902,
		3473, 25886, 25887, 25888, 25978,
		25979, 25980, 25981, 25982, 25983,
		25967, 25968, 25969, 25970, 25971,
		25972
	};
	//@formatter:on
	// Misc
	private static final int MIN_LEVEL = 85;
	
	public Q00666_HunterGuildMembersKnowledge()
	{
		super(666);
		addStartNpc(ARCTURUS, COLIN);
		addTalkId(ARCTURUS, COLIN);
		addKillId(BOSES);
		addCondMinLevel(MIN_LEVEL, "34267-00.htm");
		addFactionLevel(Faction.HUNTERS_GUILD, 1, "34267-00.htm");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return htmltext;
		}
		switch (event)
		{
			case "30703-02.htm":
			case "30703-03.htm":
			case "34267-02.htm":
			case "34267-03.htm":
			{
				htmltext = event;
				break;
			}
			case "30703-04.htm":
			case "34267-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "30703-07.html":
			case "34267-07.html":
			{
				addFactionPoints(player, Faction.HUNTERS_GUILD, 150);
				qs.exitQuest(QuestType.DAILY, true);
				htmltext = event;
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		switch (qs.getState())
		{
			case State.CREATED:
			{
				switch (npc.getId())
				{
					case COLIN:
					{
						htmltext = "30703-01.htm";
						break;
					}
					case ARCTURUS:
					{
						htmltext = "34267-01.htm";
						break;
					}
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case COLIN:
					{
						htmltext = (qs.isCond(1)) ? "30703-05.html" : "30703-06.html";
						break;
					}
					case ARCTURUS:
					{
						htmltext = (qs.isCond(1)) ? "34267-05.html" : "34267-06.html";
						break;
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				if (!qs.isNowAvailable())
				{
					htmltext = getAlreadyCompletedMsg(player, QuestType.DAILY);
					break;
				}
				qs.setState(State.CREATED);
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		executeForEachPlayer(player, npc, isSummon, true, false);
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public void actionForEachPlayer(Player player, Npc npc, boolean isSummon)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(1) && player.isInsideRadius3D(npc, Config.ALT_PARTY_RANGE))
		{
			qs.setCond(2, true);
		}
	}
}
