/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package quests.Q10363_RequestOfTheSeeker;

import quests.Q10362_CertificationOfTheSeeker.Q10362_CertificationOfTheSeeker;

import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.holders.ItemHolder;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.model.quest.State;
import com.l2jserver.gameserver.network.NpcStringId;
import com.l2jserver.gameserver.network.serverpackets.ExShowScreenMessage;

/**
 * Request of the Seeker (10363)
 * @author spider
 */
public class Q10363_RequestOfTheSeeker extends Quest
{
	// NPCs
	private static final int NAGEL = 33450;
	private static final int CELIN = 33451;
	private static final int[] CORPSES =
	{
		32961,
		32962,
		32963,
		32964
	};
	// Rewards
	private static final int ADENA_REWARD = 48000;
	private static final int EXP_REWARD = 72000;
	private static final int SP_REWARD = 16;
	private static final ItemHolder HEALING_POTIONS = new ItemHolder(1060, 100);
	private static final ItemHolder WOODEN_HELMET = new ItemHolder(43, 1);
	// Others
	private static final int SORROW_ID = 13;
	private static final int MIN_LEVEL = 12;
	private static final int MAX_LEVEL = 20;
	
	public Q10363_RequestOfTheSeeker()
	{
		super(10363, Q10363_RequestOfTheSeeker.class.getSimpleName(), "Request of the Seeker");
		addStartNpc(NAGEL);
		addTalkId(NAGEL, CELIN);
		addSocialActionSeeId(CORPSES);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "no_level.htm");
		addCondCompletedQuest(Q10362_CertificationOfTheSeeker.class.getSimpleName(), "no_prequest.html");
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "33450-02.htm":
			{
				htmltext = event;
				break;
			}
			case "33450-03.htm": // start quest
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "33450-06.html":
			{
				if (qs.getCond() == 6)
				{
					qs.setCond(7);
					htmltext = event;
				}
				break;
			}
			case "33451-02.html":
			{
				htmltext = event;
				break;
			}
			case "33451-03.html":
			{
				if (qs.isCond(7))
				{
					giveAdena(player, ADENA_REWARD, true);
					addExpAndSp(player, EXP_REWARD, SP_REWARD);
					giveItems(player, WOODEN_HELMET);
					giveItems(player, HEALING_POTIONS);
					qs.exitQuest(false, true);
					htmltext = event;
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = null;
		switch (qs.getState())
		{
			case State.CREATED:
			{
				htmltext = npc.getId() == NAGEL ? "33450-01.htm" : getNoQuestMsg(player);
				break;
			}
			case State.STARTED:
			{
				switch (qs.getCond())
				{
					case 1:
					{
						htmltext = npc.getId() == NAGEL ? "33450-04.html" : getNoQuestMsg(player);
						break;
					}
					case 6:
					{
						htmltext = npc.getId() == NAGEL ? "33450-05.html" : getNoQuestMsg(player);
						break;
					}
					case 7:
					{
						htmltext = npc.getId() == NAGEL ? "33450-06.html" : "33451-01.html";
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
	public String onSocialActionSee(L2Npc npc, L2PcInstance caster, int actionId)
	{
		
		final QuestState qs = getQuestState(caster, false);
		if ((qs != null) && (qs.getCond() >= 1) && (qs.getCond() < 6) && (actionId == SORROW_ID) && (caster.getTarget().getObjectId() == npc.getObjectId()))
		{
			switch (qs.getCond())
			{
				case 1:
				{
					showOnScreenMsg(caster, NpcStringId.YOU_VE_SHOWN_YOUR_CONDOLENCES_TO_ONE_CORPSE, ExShowScreenMessage.TOP_CENTER, 5000);
					qs.setCond(2);
					npc.deleteMe();
					break;
				}
				case 2:
				{
					showOnScreenMsg(caster, NpcStringId.YOU_VE_SHOWN_YOUR_CONDOLENCES_TO_A_SECOND_CORPSE, ExShowScreenMessage.TOP_CENTER, 5000);
					qs.setCond(3);
					npc.deleteMe();
					break;
				}
				case 3:
				{
					showOnScreenMsg(caster, NpcStringId.YOU_VE_SHOWN_YOUR_CONDOLENCES_TO_A_THIRD_CORPSE, ExShowScreenMessage.TOP_CENTER, 5000);
					qs.setCond(4);
					npc.deleteMe();
					break;
				}
				case 4:
				{
					showOnScreenMsg(caster, NpcStringId.YOU_VE_SHOWN_YOUR_CONDOLENCES_TO_A_FOURTH_CORPSE, ExShowScreenMessage.TOP_CENTER, 5000);
					qs.setCond(5);
					npc.deleteMe();
					break;
				}
				case 5:
				{
					showOnScreenMsg(caster, NpcStringId.YOU_VE_SHOWN_YOUR_CONDOLENCES_TO_A_FIFTH_CORPSE, ExShowScreenMessage.TOP_CENTER, 5000);
					qs.setCond(6);
					npc.deleteMe();
					break;
				}
			}
		}
		return super.onSocialActionSee(npc, caster, actionId);
	}
}
