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
package quests.Q504_CompetitionForTheBanditStronghold;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.model.siege.clanhalls.BanditStrongholdSiege;

/**
 * @author Mobius
 * @note Based on python script
 */
public class Q504_CompetitionForTheBanditStronghold extends Quest
{
	// NPCs
	private static final int MESSENGER = 35437;
	private static final int TARLK_BUGBEAR = 20570;
	private static final int TARLK_BUGBEAR_WARRIOR = 20571;
	private static final int TARLK_BUGBEAR_HIGH_WARRIOR = 20572;
	private static final int TARLK_BASILISK = 20573;
	private static final int ELDER_TARLK_BASILISK = 20574;
	// Items
	private static final int TARLK_AMULET = 4332;
	private static final int ALLIANCE_TROPHEY = 5009;
	
	public Q504_CompetitionForTheBanditStronghold()
	{
		super(504, "Competition for the Bandit Stronghold");
		addStartNpc(MESSENGER);
		addTalkId(MESSENGER);
		addKillId(TARLK_BUGBEAR, TARLK_BUGBEAR_WARRIOR, TARLK_BUGBEAR_HIGH_WARRIOR, TARLK_BASILISK, ELDER_TARLK_BASILISK);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		final QuestState qs = player.getQuestState(getName());
		if (qs == null)
		{
			return htmltext;
		}
		
		if (event.equals("a2.htm"))
		{
			qs.startQuest();
		}
		if (event.equals("a4.htm"))
		{
			if (qs.getQuestItemsCount(TARLK_AMULET) == 30)
			{
				qs.takeItems(TARLK_AMULET, -30);
				qs.giveItems(ALLIANCE_TROPHEY, 1);
				qs.playSound(QuestState.SOUND_FINISH);
				qs.exitQuest(true);
			}
			else
			{
				htmltext = "a5.htm";
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = getNoQuestMsg();
		final QuestState qs = player.getQuestState(getName());
		if (qs == null)
		{
			return htmltext;
		}
		
		final Clan clan = player.getClan();
		if (clan == null)
		{
			return "a6.htm";
		}
		if (clan.getLevel() < 4)
		{
			return "a6.htm";
		}
		if (!clan.getLeaderName().equals(player.getName()))
		{
			return "a6.htm";
		}
		if (BanditStrongholdSiege.getInstance().isRegistrationPeriod())
		{
			if (npc.getNpcId() == MESSENGER)
			{
				final int cond = qs.getCond();
				if (cond == 0)
				{
					htmltext = "a1.htm";
				}
				else if (cond > 1)
				{
					htmltext = "a3.htm";
				}
			}
		}
		else
		{
			htmltext = null;
			npc.showChatWindow(player, 3);
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isPet)
	{
		final QuestState qs = player.getQuestState(getName());
		if ((qs == null) || (qs.getState() != State.STARTED))
		{
			return null;
		}
		
		if ((qs.getCond() < 2) && (qs.getQuestItemsCount(TARLK_AMULET) < 30))
		{
			qs.giveItems(TARLK_AMULET, 1);
			qs.playSound(QuestState.SOUND_ITEMGET);
			if (qs.getQuestItemsCount(TARLK_AMULET) == 30)
			{
				qs.setCond(2);
			}
		}
		
		return null;
	}
}