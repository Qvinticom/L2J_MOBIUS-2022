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
package quests.Q10961_EffectiveTraining;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

/**
 * Effective Training (10961)
 * @author RobikBobik
 * @Notee: Based on NA server September 2019
 */
public class Q10961_EffectiveTraining extends Quest
{
	// NPCs
	private static final int NEWBIE_GUIDE = 34110;
	private static final int REAHEN = 34111;
	// Monsters
	private static final int GREY_KELTIR = 21981;
	private static final int ELDER_GREY_KELTIR = 21982;
	private static final int BLACK_WOLF = 21983;
	private static final int ELDER_BLACK_WOLF = 21984;
	// Items
	private static final ItemHolder SOE_TO_REAHEN = new ItemHolder(91917, 1);
	private static final ItemHolder SOE_NOVICE = new ItemHolder(10650, 10);
	private static final ItemHolder RING_NOVICE = new ItemHolder(49041, 2);
	private static final ItemHolder EARRING_NOVICE = new ItemHolder(49040, 2);
	private static final ItemHolder NECKLACE_NOVICE = new ItemHolder(49039, 1);
	// Misc
	private static final int MAX_LEVEL = 20;
	private static final String KILL_COUNT_VAR = "KillCount";
	
	public Q10961_EffectiveTraining()
	{
		super(10961);
		addStartNpc(NEWBIE_GUIDE);
		addTalkId(NEWBIE_GUIDE, REAHEN);
		addKillId(GREY_KELTIR, ELDER_GREY_KELTIR, BLACK_WOLF, ELDER_BLACK_WOLF);
		addCondMaxLevel(MAX_LEVEL, "no_lvl.html");
		setQuestNameNpcStringId(NpcStringId.LV_2_20_EFFECTIVE_TRAINING);
	}
	
	@Override
	public boolean checkPartyMember(PlayerInstance member, Npc npc)
	{
		final QuestState qs = getQuestState(member, false);
		return ((qs != null) && qs.isStarted());
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "TELEPORT_TO_HUNTING_GROUND":
			{
				giveStoryBuffReward(npc, player);
				player.teleToLocation(-120020, 55668, -1560);
				break;
			}
			case "34110-02.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "34111-02.htm":
			{
				if (qs.isStarted())
				{
					addExpAndSp(player, 260000, 6000);
					giveItems(player, SOE_NOVICE);
					giveItems(player, RING_NOVICE);
					giveItems(player, EARRING_NOVICE);
					giveItems(player, NECKLACE_NOVICE);
					giveStoryBuffReward(npc, player);
					qs.exitQuest(false, true);
					htmltext = event;
					break;
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && qs.isCond(1))
		{
			final int killCount = qs.getInt(KILL_COUNT_VAR) + 1;
			if (killCount < 20)
			{
				qs.set(KILL_COUNT_VAR, killCount);
				playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				sendNpcLogList(killer);
				
			}
			else
			{
				qs.setCond(2, true);
				qs.unset(KILL_COUNT_VAR);
				giveItems(killer, SOE_TO_REAHEN);
				showOnScreenMsg(killer, NpcStringId.YOU_VE_KILLED_ALL_THE_MONSTERS_NUSE_THE_SCROLL_OF_ESCAPE_IN_YOUR_INVENTORY_TO_RETURN_TO_TRADER_REAHEN, ExShowScreenMessage.TOP_CENTER, 10000);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(1))
		{
			final Set<NpcLogListHolder> holder = new HashSet<>();
			holder.add(new NpcLogListHolder(NpcStringId.KILL_MONSTERS_ON_THE_HILL_OF_HOPE.getId(), true, qs.getInt(KILL_COUNT_VAR)));
			return holder;
		}
		return super.getNpcLogList(player);
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		if (qs.isCreated())
		{
			htmltext = "34110-01.htm";
		}
		else if (qs.isStarted())
		{
			switch (npc.getId())
			{
				case NEWBIE_GUIDE:
				{
					if (qs.isCond(1))
					{
						htmltext = "34110-02.htm";
					}
					break;
				}
				case REAHEN:
				{
					if (qs.isCond(2))
					{
						htmltext = "34111.htm";
					}
					break;
				}
			}
		}
		else if (qs.isCompleted())
		{
			if (npc.getId() == NEWBIE_GUIDE)
			{
				htmltext = getAlreadyCompletedMsg(player);
			}
		}
		return htmltext;
	}
}