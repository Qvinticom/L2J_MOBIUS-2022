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
package quests.Q510_AClansReputation;

import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class Q510_AClansReputation extends Quest
{
	private static final String qn = "Q510_AClansReputation";
	
	// NPC
	private static final int Valdis = 31331;
	
	// Quest Item
	private static final int Claw = 8767;
	
	// Reward
	private static final int CLAN_POINTS_REWARD = 50; // Quantity of points
	
	public Q510_AClansReputation()
	{
		super(510, qn, "A Clan's Reputation");
		
		registerQuestItems(Claw);
		
		addStartNpc(Valdis);
		addTalkId(Valdis);
		
		addKillId(22215, 22216, 22217);
	}
	
	@Override
	public String onAdvEvent(String event, NpcInstance npc, PlayerInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equalsIgnoreCase("31331-3.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31331-6.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(NpcInstance npc, PlayerInstance player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg();
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.CREATED:
				if (!player.isClanLeader())
				{
					st.exitQuest(true);
					htmltext = "31331-0.htm";
				}
				else if (player.getClan().getLevel() < 5)
				{
					st.exitQuest(true);
					htmltext = "31331-0.htm";
				}
				else
				{
					htmltext = "31331-1.htm";
				}
				break;
			
			case State.STARTED:
				if (st.getInt("cond") == 1)
				{
					int count = st.getQuestItemsCount(Claw);
					if (count > 0)
					{
						int reward = (CLAN_POINTS_REWARD * count);
						st.takeItems(Claw, -1);
						Clan clan = player.getClan();
						clan.setReputationScore(clan.getReputationScore() + reward, true);
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_QUEST_COMPLETED_AND_S1_POINTS_GAINED).addNumber(reward));
						clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
						
						htmltext = "31331-7.htm";
					}
					else
					{
						htmltext = "31331-4.htm";
					}
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(NpcInstance npc, PlayerInstance player, boolean isPet)
	{
		// Retrieve the qs of the clan leader.
		QuestState st = getClanLeaderQuestState(player, npc);
		if ((st == null) || !st.isStarted())
		{
			return null;
		}
		
		st.giveItems(Claw, 1);
		st.playSound(QuestState.SOUND_MIDDLE);
		
		return null;
	}
}