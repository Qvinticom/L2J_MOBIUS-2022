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
package quests.Q10363_RequestOfTheSeeker;

import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.ItemHolder;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;
import com.l2jmobius.gameserver.network.NpcStringId;
import com.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import quests.Q10362_CertificationOfTheSeeker.Q10362_CertificationOfTheSeeker;

/**
 * Request of the Seeker (10363)
 * @author spider, gyo
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
	// Monsters
	private static final int CRAWLER = 22991;
	private static final int STALKER = 22992;
	private static final int RESURRECTED_VENGEFUL_SPIRIT1 = 19157;
	private static final int RESURRECTED_VENGEFUL_SPIRIT2 = 19158;
	// Rewards
	private static final int ADENA_REWARD = 48000;
	private static final int EXP_REWARD = 72000;
	private static final int SP_REWARD = 16;
	private static final ItemHolder HEALING_POTIONS = new ItemHolder(1060, 100);
	private static final ItemHolder WOODEN_HELMET = new ItemHolder(43, 1);
	// Social Action IDs
	private static final int LAUGH = 10;
	private static final int DANCE = 12;
	private static final int SORROW = 13;
	// Others
	private static final int MIN_LEVEL = 12;
	private static final int MAX_LEVEL = 20;
	private static final int distanceLimit = 70;
	
	public Q10363_RequestOfTheSeeker()
	{
		super(10363, Q10363_RequestOfTheSeeker.class.getSimpleName(), "Request of the Seeker");
		addStartNpc(NAGEL);
		addTalkId(NAGEL, CELIN);
		addSocialActionSeeId(CORPSES);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "33450-08.htm");
		addCondCompletedQuest(Q10362_CertificationOfTheSeeker.class.getSimpleName(), "33450-08.htm");
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
					showOnScreenMsg(player, NpcStringId.USE_THE_YE_SAGIRA_TELEPORT_DEVICE_TO_GO_TO_EXPLORATION_AREA_3, ExShowScreenMessage.TOP_CENTER, 10000);
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
			case "life_over":
			{
				npc.deleteMe();
				return null;
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
					case 2:
					case 3:
					case 4:
					case 5:
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
				htmltext = npc.getId() == NAGEL ? "33450-07.html" : "33451-04.html";
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onSocialActionSee(L2Npc npc, L2PcInstance caster, int actionId)
	{
		if ((caster == null) || (npc == null))
		{
			return super.onSocialActionSee(npc, caster, actionId);
		}
		if ((caster.getTarget() == null) || !caster.getTarget().isNpc())
		{
			return super.onSocialActionSee(npc, caster, actionId);
		}
		
		final double distance = caster.calculateDistance(caster.getTarget().getLocation(), true, false);
		final QuestState qs = getQuestState(caster, false);
		if (actionId == SORROW)
		{
			if (distance > distanceLimit)
			{
				showOnScreenMsg(caster, NpcStringId.YOU_ARE_TOO_FAR_FROM_THE_CORPSE_TO_SHOW_YOUR_CONDOLENCES, ExShowScreenMessage.TOP_CENTER, 10000);
				npc.deleteMe();
			}
			else if (qs != null)
			{
				switch (qs.getCond())
				{
					case 1:
					{
						showOnScreenMsg(caster, NpcStringId.YOU_VE_SHOWN_YOUR_CONDOLENCES_TO_ONE_CORPSE, ExShowScreenMessage.TOP_CENTER, 10000);
						qs.setCond(2, true);
						npc.deleteMe();
						break;
					}
					case 2:
					{
						showOnScreenMsg(caster, NpcStringId.YOU_VE_SHOWN_YOUR_CONDOLENCES_TO_A_SECOND_CORPSE, ExShowScreenMessage.TOP_CENTER, 10000);
						qs.setCond(3, true);
						npc.deleteMe();
						break;
					}
					case 3:
					{
						showOnScreenMsg(caster, NpcStringId.YOU_VE_SHOWN_YOUR_CONDOLENCES_TO_A_THIRD_CORPSE, ExShowScreenMessage.TOP_CENTER, 10000);
						qs.setCond(4, true);
						npc.deleteMe();
						break;
					}
					case 4:
					{
						showOnScreenMsg(caster, NpcStringId.YOU_VE_SHOWN_YOUR_CONDOLENCES_TO_A_FOURTH_CORPSE, ExShowScreenMessage.TOP_CENTER, 10000);
						qs.setCond(5, true);
						npc.deleteMe();
						break;
					}
					case 5:
					{
						showOnScreenMsg(caster, NpcStringId.YOU_VE_SHOWN_YOUR_CONDOLENCES_TO_A_FIFTH_CORPSE, ExShowScreenMessage.TOP_CENTER, 10000);
						qs.setCond(6, true);
						npc.deleteMe();
						break;
					}
					default:
					{
						showOnScreenMsg(caster, NpcStringId.GRUDGE_OF_YE_SAGIRA_VICTIMS_HAVE_BEEN_RELIEVED_WITH_YOUR_TEARS, ExShowScreenMessage.TOP_CENTER, 10000);
						npc.deleteMe();
						break;
					}
				}
			}
			else
			{
				showOnScreenMsg(caster, NpcStringId.GRUDGE_OF_YE_SAGIRA_VICTIMS_HAVE_BEEN_RELIEVED_WITH_YOUR_TEARS, ExShowScreenMessage.TOP_CENTER, 10000);
				npc.deleteMe();
			}
		}
		else if ((actionId == LAUGH) || (actionId == DANCE))
		{
			if (distance > distanceLimit)
			{
				showOnScreenMsg(caster, NpcStringId.YOU_ARE_TOO_FAR_FROM_THE_CORPSE, ExShowScreenMessage.TOP_CENTER, 10000);
				npc.deleteMe();
			}
			else if ((qs == null) || (qs.getState() != State.STARTED))
			{
				addSpawn(getRandom(1) == 0 ? CRAWLER : STALKER, npc.getLocation());
				npc.deleteMe();
			}
			else
			{
				showOnScreenMsg(caster, NpcStringId.DON_T_TOY_WITH_THE_DEAD, ExShowScreenMessage.TOP_CENTER, 10000);
				L2Npc Spirit1 = addSpawn(RESURRECTED_VENGEFUL_SPIRIT1, new Location(caster.getX() - getRandom(100), caster.getY() - getRandom(100), caster.getZ(), 0));
				Spirit1.setCurrentHp(Spirit1.getMaxHp() / 2);
				addAttackDesire(Spirit1, caster);
				startQuestTimer("life_over", 20000, Spirit1, caster);
				L2Npc Spirit2 = addSpawn(RESURRECTED_VENGEFUL_SPIRIT2, new Location(caster.getX() - getRandom(100), caster.getY() - getRandom(100), caster.getZ(), 0));
				Spirit2.setCurrentHp(Spirit2.getMaxHp() / 2);
				addAttackDesire(Spirit2, caster);
				startQuestTimer("life_over", 20000, Spirit2, caster);
				npc.deleteMe();
			}
		}
		else
		{
			addSpawn(getRandom(1) == 0 ? CRAWLER : STALKER, ((L2Npc) caster.getTarget()).getLocation());
			npc.deleteMe();
		}
		return super.onSocialActionSee(npc, caster, actionId);
	}
}
