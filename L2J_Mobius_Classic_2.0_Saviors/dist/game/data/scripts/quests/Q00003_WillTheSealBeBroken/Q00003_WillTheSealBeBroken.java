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
package quests.Q00003_WillTheSealBeBroken;

import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * Will the Seal be Broken? (3)
 * @author malyelfik
 */
public class Q00003_WillTheSealBeBroken extends Quest
{
	// NPC
	private static final int TALLOTH = 30141;
	// Monsters
	private static final int OMEN_BEAST = 20031;
	private static final int TAINTED_ZOMBIE = 20041;
	private static final int STINK_ZOMBIE = 20046;
	private static final int LESSER_SUCCUBUS = 20048;
	private static final int LESSER_SUCCUBUS_TUREN = 20052;
	private static final int LESSER_SUCCUBUS_TILFO = 20057;
	// Items
	private static final int OMEN_BEAST_EYE = 1081;
	private static final int TAINT_STONE = 1082;
	private static final int SUCCUBUS_BLOOD = 1083;
	// Misc
	private static final int MIN_LEVEL = 16;
	private static final int MAX_LEVEL = 26;
	
	public Q00003_WillTheSealBeBroken()
	{
		super(3);
		addStartNpc(TALLOTH);
		addTalkId(TALLOTH);
		addKillId(OMEN_BEAST, TAINTED_ZOMBIE, STINK_ZOMBIE, LESSER_SUCCUBUS, LESSER_SUCCUBUS_TILFO, LESSER_SUCCUBUS_TUREN);
		registerQuestItems(OMEN_BEAST_EYE, TAINT_STONE, SUCCUBUS_BLOOD);
		addCondMaxLevel(MAX_LEVEL, "30141-01.html");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = event;
		switch (event)
		{
			case "30141-03.htm":
			{
				qs.startQuest();
				break;
			}
			case "30141-05.html":
			{
				break;
			}
			default:
			{
				htmltext = null;
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		final Player member = getRandomPartyMember(player, 1);
		if (member == null)
		{
			return super.onKill(npc, player, isSummon);
		}
		final QuestState qs = getQuestState(member, false);
		switch (npc.getId())
		{
			case OMEN_BEAST:
			{
				giveItem(member, qs, OMEN_BEAST_EYE, getRegisteredItemIds());
				break;
			}
			case STINK_ZOMBIE:
			case TAINTED_ZOMBIE:
			{
				giveItem(member, qs, TAINT_STONE, getRegisteredItemIds());
				break;
			}
			case LESSER_SUCCUBUS:
			case LESSER_SUCCUBUS_TILFO:
			case LESSER_SUCCUBUS_TUREN:
			{
				giveItem(member, qs, SUCCUBUS_BLOOD, getRegisteredItemIds());
				break;
			}
		}
		return super.onKill(npc, player, isSummon);
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
				htmltext = (player.getRace() != Race.DARK_ELF) ? "30141-00.htm" : (player.getLevel() >= MIN_LEVEL) ? "30141-02.htm" : "30141-01.html";
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					htmltext = "30141-04.html";
				}
				else
				{
					giveAdena(player, 3800, true);
					qs.exitQuest(false, true);
					htmltext = "30141-06.html";
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
	
	private void giveItem(Player player, QuestState qs, int item, int... items)
	{
		if (!hasQuestItems(player, item))
		{
			giveItems(player, item, 1);
			playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			if (hasQuestItems(player, items))
			{
				qs.setCond(2, true);
			}
		}
	}
}