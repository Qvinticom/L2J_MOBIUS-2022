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
package quests.Q10887_SaviorsPathDemonsAndAtelia;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.CommonUtil;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q10886_SaviorsPathSearchTheRefinery.Q10886_SaviorsPathSearchTheRefinery;

/**
 * Savior's Path - Demons and Atelia (10887)
 * @URL https://l2wiki.com/Savior%27s_Path_-_Demons_and_Atelia
 * @author CostyKiller
 */
public class Q10887_SaviorsPathDemonsAndAtelia extends Quest
{
	// NPC
	private static final int DEVIANNE = 34427;
	// Monsters
	private static final int[] MONSTERS =
	{
		// Atelia Refinery
		24150, // Devil Warrior
		24149, // Devil Nightmare
		24153, // Devil Varos
		24152, // Devil Sinist
		24151, // Devil Guardian
		24144, // Death Rogue
		24145, // Death Shooter
		24146, // Death Warrior
		24147, // Death Sorcerer
		24148, // Death Pondus
		24155, // Demonic Warrior
		24154, // Demonic Wizard
		24158, // Demonic Weiss
		24157, // Demonic Keras
		24156, // Demonic Archer
	};
	// Misc
	private static final int MIN_LEVEL = 103;
	private static final int DEMONS_KILLS_NEEDED = 500;
	
	public Q10887_SaviorsPathDemonsAndAtelia()
	{
		super(10887);
		addStartNpc(DEVIANNE);
		addTalkId(DEVIANNE);
		addKillId(MONSTERS);
		addCondMinLevel(MIN_LEVEL, "34427-00.html");
		addCondCompletedQuest(Q10886_SaviorsPathSearchTheRefinery.class.getSimpleName(), "34427-00.html");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		
		switch (event)
		{
			case "34427-02.htm":
			case "34427-03.htm":
			case "34427-06.htm":
			{
				htmltext = event;
				break;
			}
			case "34427-04.html":
			{
				if ((player.getLevel() >= MIN_LEVEL))
				{
					qs.startQuest();
					qs.set("DemonsKilled", 0);
					htmltext = event;
				}
				break;
			}
			case "34427-07.html":
			{
				if (qs.isCond(2))
				{
					if ((player.getLevel() >= MIN_LEVEL))
					{
						addExpAndSp(player, 108766499040L, 108766440);
						giveAdena(player, 12309205, true);
						qs.exitQuest(false, true);
						htmltext = event;
					}
					else
					{
						htmltext = getNoQuestLevelRewardMsg(player);
					}
				}
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
				htmltext = "34427-01.htm";
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					htmltext = "34427-05.html";
				}
				else if (qs.isCond(2) && (qs.getInt("DemonsKilled") >= DEMONS_KILLS_NEEDED))
				{
					htmltext = "34427-06.htm";
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
		if ((qs != null) && qs.isCond(1) && player.isInsideRadius3D(npc, Config.ALT_PARTY_RANGE))
		{
			if (qs.getInt("DemonsKilled") < DEMONS_KILLS_NEEDED)
			{
				final int killedDemons = qs.getInt("DemonsKilled") + 1;
				if (CommonUtil.contains(MONSTERS, npc.getId()))
				{
					qs.set("DemonsKilled", killedDemons);
					if ((killedDemons == DEMONS_KILLS_NEEDED))
					{
						qs.setCond(2, true);
					}
					else
					{
						playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
						sendNpcLogList(player);
					}
				}
			}
		}
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && (qs.isCond(1)))
		{
			final Set<NpcLogListHolder> holder = new HashSet<>();
			holder.add(new NpcLogListHolder(1019725, true, qs.getInt("DemonsKilled")));
			return holder;
		}
		return super.getNpcLogList(player);
	}
}