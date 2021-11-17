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
 * Knowledgeable Hunter Guild Member (666)
 * @URL https://l2wiki.com/Knowledgeable_Hunter_Guild_Member
 * @author Dmitri
 */
public class Q00666_HunterGuildMembersKnowledge extends Quest
{
	// NPCs
	private static final int ARCTURUS = 34267;
	private static final int COLIN = 30703;
	// BOSS
	private static final int[] BOSES =
	{
		3473, // Omega Golem
		3477, // Reinforced Super Kat the Cat
		3479, // Darkened Super Feline Queen
		3481, // Control-crazed Mew the Cat
		25876, // Maliss
		25877, // Isadora
		25886, // Houpon the Warden Overseer
		25887, // Crook the Mad
		25902, // Gigantic Golem (96 lvl)
		25922, // Nerva Chief Turakan
		25929, // Tegaffe
		25931, // Theor
		25933, // Garden Patrol Captain
		25937, // Spicula Negative
		25946, // Antharas' Herald Komabor
		25948, // Valakas' Herald Potigia
		25949, // Lindvior's Herald Numa
		25956, // Vengeful Eligos
		25957, // Vengeful Agarez
		25958, // Vengeful Lerazia
		25959, // Vengeful Oretross
		25960, // Vengeful Edaire
		25961, // Vengeful Agonia
		25982, // Varmonia
		25983, // Varkaron
		26001, // Amden Orc Turation
		26005, // Nerva Orc Nergatt
		26312, // Lithra
		26347, // Summoned Harpas
		26348, // Summoned Garp
		26349, // Summoned Moricks
		26431, // Avenger Alusion
		26432, // Avenger Graff
		26433, // Demon Venoma
		26434, // Fiend Sarboth
		26435, // Watcher Tristan
		26436, // Watcher Setheth
		26437, // Berserker Zetahl
		26438, // Berserker Tabris
		26439, // Ferocious Valac
		26440, // Arrogant Lebruum
		29163, // Tiat (attack type)
		29374, // Cyrax
	};
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
