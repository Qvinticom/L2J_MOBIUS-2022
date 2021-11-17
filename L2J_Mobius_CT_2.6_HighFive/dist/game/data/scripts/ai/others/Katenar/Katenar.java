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
package ai.others.Katenar;

import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.NpcStringId;

import ai.AbstractNpcAI;
import quests.Q00065_CertifiedSoulBreaker.Q00065_CertifiedSoulBreaker;

/**
 * Katenar AI for quests Certified Soul Breaker (65)
 * @author ivantotov
 */
public class Katenar extends AbstractNpcAI
{
	// NPC
	private static final int KATENAR = 32242;
	// Item
	private static final int SEALED_DOCUMENT = 9803;
	
	private Katenar()
	{
		addStartNpc(KATENAR);
		addTalkId(KATENAR);
		addFirstTalkId(KATENAR);
		addSpawnId(KATENAR);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final Npc npc0 = npc.getVariables().getObject("npc0", Npc.class);
		final String htmltext = null;
		switch (event)
		{
			case "CREATED_50":
			{
				if ((npc0 != null) && !npc.getVariables().getBoolean("SPAWNED", false))
				{
					npc0.getVariables().set("SPAWNED", false);
				}
				npc.deleteMe();
				break;
			}
			case "GOOD_LUCK":
			{
				final QuestState qs = player.getQuestState(Q00065_CertifiedSoulBreaker.class.getSimpleName());
				if (qs.isMemoState(14))
				{
					if ((npc0 != null) && !npc.getVariables().getBoolean("SPAWNED", false))
					{
						npc0.getVariables().set("SPAWNED", false);
						npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.GOOD_LUCK);
					}
					npc.deleteMe();
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player talker)
	{
		final QuestState qs = talker.getQuestState(Q00065_CertifiedSoulBreaker.class.getSimpleName());
		String htmltext = getNoQuestMsg(talker);
		final int memoState = qs.getMemoState();
		if (memoState == 12)
		{
			htmltext = "32242-01.html";
		}
		else if (memoState == 13)
		{
			final Player player = npc.getVariables().getObject("player0", Player.class);
			if (player == talker)
			{
				qs.setMemoState(14);
				qs.setCond(13, true);
				htmltext = "32242-02.html";
			}
			else
			{
				qs.setMemoState(14);
				qs.setCond(13, true);
				htmltext = "32242-03.html";
			}
			if (!hasQuestItems(player, SEALED_DOCUMENT))
			{
				giveItems(player, SEALED_DOCUMENT, 1);
			}
		}
		else if (memoState == 14)
		{
			htmltext = "32242-04.html";
		}
		return htmltext;
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		startQuestTimer("CREATED_50", 50000, npc, null);
		final Player player = npc.getVariables().getObject("player0", Player.class);
		if (player != null)
		{
			npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.I_AM_LATE);
		}
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new Katenar();
	}
}