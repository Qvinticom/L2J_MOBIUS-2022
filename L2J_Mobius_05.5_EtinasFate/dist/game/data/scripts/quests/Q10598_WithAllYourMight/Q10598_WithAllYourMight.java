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
package quests.Q10598_WithAllYourMight;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.CommonUtil;
import org.l2jmobius.gameserver.enums.Faction;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q10597_EscapeToTheShadowOfTheMotherTree.Q10597_EscapeToTheShadowOfTheMotherTree;

/**
 * With All Your Might (10598)
 * @URL https://l2wiki.com/With_All_Your_Might
 * @author Dmitri
 */
public class Q10598_WithAllYourMight extends Quest
{
	// NPCs
	private static final int ASTERIOS = 34411;
	private static final int NERUPA = 34412;
	// Monsters
	private static final int[] MONSTERS =
	{
		24117, // Crystal Reep
		24118, // Crystal Reep
		24119, // Crystal Needle
		24120, // Crystal Needle
		24121, // Treant Blossom
		24122, // Treant Blossom
		24123, // Flush Teasle
		24124, // Flush Teasle
		24125, // Creeper Rampike
		24126, // Creeper Rampike
		24139, // Reep Child
		24140, // Needle Child
		24141, // Blossom Child
		24142, // Teasle Child
		24127, // Lithra Aprias
		24128 // Nerupa Aprias
	};
	// Items
	private static final int LITHRAS_CONSIDERATION = 48366;
	// Rewards
	private static final int ELCYUM_CRYSTAL = 36514;
	private static final int RUNE_STONE = 39738;
	// Misc
	private static final int MIN_LEVEL = 103;
	
	public Q10598_WithAllYourMight()
	{
		super(10598);
		addStartNpc(ASTERIOS);
		addTalkId(ASTERIOS, NERUPA);
		addKillId(MONSTERS);
		registerQuestItems(LITHRAS_CONSIDERATION);
		addCondMinLevel(MIN_LEVEL, "34411-00.htm");
		addCondCompletedQuest(Q10597_EscapeToTheShadowOfTheMotherTree.class.getSimpleName(), "34411-00.htm");
		addFactionLevel(Faction.MOTHER_TREE_GUARDIANS, 10, "34411-00.htm");
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
			case "34411-02.htm":
			case "34411-03.htm":
			case "34412-02.html":
			case "34412-03.html":
			case "34411-07.html":
			{
				htmltext = event;
				break;
			}
			case "34411-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "34412-04.html":
			{
				qs.setCond(2, true);
				htmltext = event;
				break;
			}
			case "34411-08.html":
			{
				if (qs.isCond(3))
				{
					addExpAndSp(player, 543832495200L, 543832200);
					takeItems(player, LITHRAS_CONSIDERATION, -1);
					giveItems(player, ELCYUM_CRYSTAL, 3);
					giveItems(player, RUNE_STONE, 1);
					qs.exitQuest(false, true);
					htmltext = event;
					break;
				}
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
				if (npc.getId() == ASTERIOS)
				{
					htmltext = "34411-01.htm";
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case ASTERIOS:
					{
						if (qs.isCond(1))
						{
							htmltext = "34411-04.htm";
						}
						else if (qs.isCond(2))
						{
							htmltext = "34411-05.html";
						}
						else if (qs.isCond(3))
						{
							htmltext = "34411-06.html";
						}
						break;
					}
					case NERUPA:
					{
						if (qs.isCond(1))
						{
							htmltext = "34412-01.html";
						}
						else if (qs.isCond(2))
						{
							htmltext = "34412-05.html";
						}
						else if (qs.isCond(3))
						{
							htmltext = "34412-06.html";
						}
						break;
					}
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
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		executeForEachPlayer(player, npc, isSummon, true, false);
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public void actionForEachPlayer(Player player, Npc npc, boolean isSummon)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(2) && player.isInsideRadius3D(npc, Config.ALT_PARTY_RANGE) && CommonUtil.contains(MONSTERS, npc.getId()))
		{
			giveItems(player, LITHRAS_CONSIDERATION, 1);
			playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			if (getQuestItemsCount(player, LITHRAS_CONSIDERATION) >= 2000)
			{
				qs.setCond(3, true);
			}
		}
	}
}
