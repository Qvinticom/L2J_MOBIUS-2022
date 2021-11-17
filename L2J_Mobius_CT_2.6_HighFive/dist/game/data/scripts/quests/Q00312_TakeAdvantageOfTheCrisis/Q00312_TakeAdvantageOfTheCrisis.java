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
package quests.Q00312_TakeAdvantageOfTheCrisis;

import java.util.HashMap;
import java.util.Map;

import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

/**
 * Take Advantage of the Crisis! (312)
 * @author malyelfik
 */
public class Q00312_TakeAdvantageOfTheCrisis extends Quest
{
	// NPC
	private static final int FILAUR = 30535;
	// Monsters
	private static final Map<Integer, Integer> MOBS = new HashMap<>();
	static
	{
		MOBS.put(22678, 291); // Grave Robber Summoner (Lunatic)
		MOBS.put(22679, 596); // Grave Robber Magician (Lunatic)
		MOBS.put(22680, 610); // Grave Robber Worker (Lunatic)
		MOBS.put(22681, 626); // Grave Robber Warrior (Lunatic)
		MOBS.put(22682, 692); // Grave Robber Warrior of Light (Lunatic)
		MOBS.put(22683, 650); // Servitor of Darkness
		MOBS.put(22684, 310); // Servitor of Darkness
		MOBS.put(22685, 626); // Servitor of Darkness
		MOBS.put(22686, 626); // Servitor of Darkness
		MOBS.put(22687, 308); // Phantoms of the Mine
		MOBS.put(22688, 416); // Evil Spirits of the Mine
		MOBS.put(22689, 212); // Mine Bug
		MOBS.put(22690, 748); // Earthworm's Descendant
	}
	// Item
	private static final int MINERAL_FRAGMENT = 14875;
	// Misc
	private static final int MIN_LEVEL = 80;
	
	public Q00312_TakeAdvantageOfTheCrisis()
	{
		super(312);
		addStartNpc(FILAUR);
		addTalkId(FILAUR);
		addKillId(MOBS.keySet());
		registerQuestItems(MINERAL_FRAGMENT);
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
			case "30535-02.html":
			case "30535-03.html":
			case "30535-04.html":
			case "30535-05.htm":
			case "30535-09.html":
			case "30535-10.html":
			{
				break;
			}
			case "30535-06.htm":
			{
				qs.startQuest();
				break;
			}
			case "30535-11.html":
			{
				qs.exitQuest(true, true);
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
		if ((member != null) && (getRandom(1000) < MOBS.get(npc.getId())))
		{
			giveItems(member, MINERAL_FRAGMENT, 1);
			playSound(member, QuestSound.ITEMSOUND_QUEST_ITEMGET);
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
				htmltext = (player.getLevel() >= MIN_LEVEL) ? "30535-01.htm" : "30535-00.htm";
				break;
			}
			case State.STARTED:
			{
				htmltext = (hasQuestItems(player, MINERAL_FRAGMENT)) ? "30535-08.html" : "30535-07.html";
				break;
			}
		}
		return htmltext;
	}
}