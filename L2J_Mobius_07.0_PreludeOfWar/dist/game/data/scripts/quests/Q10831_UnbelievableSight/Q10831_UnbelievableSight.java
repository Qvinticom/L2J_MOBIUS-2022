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
package quests.Q10831_UnbelievableSight;

import org.l2jmobius.gameserver.enums.Faction;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q10830_TheLostGardenOfSpirits.Q10830_TheLostGardenOfSpirits;

/**
 * Unbelievable Sight (10831)
 * @URL https://l2wiki.com/Unbelievable_Sight
 * @author Gigi
 */
public class Q10831_UnbelievableSight extends Quest
{
	// NPC
	private static final int BELAS = 34056;
	// Monsters
	private static final int ENERGY_OF_WIND = 19647;
	private static final int KERBEROS_LAGER = 23550;
	private static final int KERBEROS_FORT = 23551;
	private static final int KERBEROS_NERO = 23552;
	private static final int FURY_SYLPH_BARRENA = 23553;
	private static final int FURY_SYLPH_TEMPTRESS = 23555;
	private static final int FURY_SYLPH_PURKA = 23556;
	private static final int FURY_KERBEROS_LEGER = 23557;
	private static final int FURY_KERBEROS_NERO = 23558;
	// Items
	private static final int TRANSFORMED_ENERGY = 48005;
	private static final int SOE = 46158; // Scroll of Escape: Blackbird Campsite
	// Misc
	private static final int MIN_LEVEL = 102;
	
	public Q10831_UnbelievableSight()
	{
		super(10831);
		addStartNpc(BELAS);
		addTalkId(BELAS);
		addKillId(ENERGY_OF_WIND);
		addKillId(KERBEROS_LAGER, KERBEROS_FORT, KERBEROS_NERO, FURY_SYLPH_BARRENA, FURY_SYLPH_TEMPTRESS, FURY_SYLPH_PURKA, FURY_KERBEROS_LEGER, FURY_KERBEROS_NERO);
		registerQuestItems(TRANSFORMED_ENERGY);
		addCondMinLevel(MIN_LEVEL, "34056-00.htm");
		addCondCompletedQuest(Q10830_TheLostGardenOfSpirits.class.getSimpleName(), "34056-00.htm");
		addFactionLevel(Faction.UNWORLDLY_VISITORS, 2, "34056-00.htm");
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
			case "34056-02.htm":
			case "34056-03.htm":
			{
				htmltext = event;
				break;
			}
			case "34056-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "34056-07.html":
			{
				giveItems(player, SOE, 1); // Scroll of Escape: Blackbird Campsite
				addExpAndSp(player, 44442855900L, 44442720);
				qs.exitQuest(false, true);
				htmltext = event;
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
				htmltext = "34056-01.htm";
				break;
			}
			case State.STARTED:
			{
				if (qs.isCond(1))
				{
					htmltext = "34056-05.html";
				}
				else
				{
					htmltext = "34056-06.html";
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
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && qs.isCond(1))
		{
			switch (npc.getId())
			{
				case KERBEROS_LAGER:
				case KERBEROS_FORT:
				case KERBEROS_NERO:
				case FURY_SYLPH_BARRENA:
				case FURY_SYLPH_TEMPTRESS:
				case FURY_SYLPH_PURKA:
				case FURY_KERBEROS_LEGER:
				case FURY_KERBEROS_NERO:
				{
					if (getRandom(100) < 50)
					{
						final Npc mob = addSpawn(ENERGY_OF_WIND, npc.getX(), npc.getY(), npc.getZ(), 0, true, 120000);
						addAttackPlayerDesire(mob, killer, 5);
					}
					break;
				}
				case ENERGY_OF_WIND:
				{
					if (giveItemRandomly(killer, npc, TRANSFORMED_ENERGY, 1, 10, 0.5, true))
					{
						qs.setCond(2, true);
					}
					break;
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}