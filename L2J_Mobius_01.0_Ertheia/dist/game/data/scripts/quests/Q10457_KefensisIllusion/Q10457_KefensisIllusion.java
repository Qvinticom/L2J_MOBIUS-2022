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
package quests.Q10457_KefensisIllusion;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q10455_ElikiasLetter.Q10455_ElikiasLetter;

/**
 * Kefensis' Illusion (10457)
 * @URL https://l2wiki.com/Kefensis%27_Illusion
 * @author Dmitri
 */
public class Q10457_KefensisIllusion extends Quest
{
	// NPC
	private static final int DEVIANNE = 31590;
	// Monsters
	private static final int VIPER = 23389;
	private static final int SMAUG = 23384;
	private static final int LUNATIKAN = 23385;
	private static final int JABBERWOK = 23386;
	private static final int KANZAROTH = 23387;
	private static final int KANDILOTH = 23388;
	private static final int GARION = 23395;
	private static final int GARION_NETI = 23396;
	private static final int DESERT_WENDIGO = 23397;
	private static final int KORAZA = 23398;
	private static final int BEND_BEETLE = 23399;
	// Skill
	private static final SkillHolder DESERT_THIRST = new SkillHolder(16697, 1);
	// Misc
	private static final int MIN_LEVEL = 99;
	
	public Q10457_KefensisIllusion()
	{
		super(10457);
		addStartNpc(DEVIANNE);
		addTalkId(DEVIANNE);
		addKillId(VIPER, SMAUG, LUNATIKAN, JABBERWOK, KANZAROTH, KANDILOTH, GARION, GARION_NETI, DESERT_WENDIGO, KORAZA, BEND_BEETLE);
		addCondMinLevel(MIN_LEVEL, "31590-00.htm");
		addCondCompletedQuest(Q10455_ElikiasLetter.class.getSimpleName(), "31590-00.htm");
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
			case "31590-02.htm":
			case "31590-03.htm":
			{
				htmltext = event;
				break;
			}
			case "31590-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "31590-07.html":
			{
				// Rewards
				giveAdena(player, 2373300, true);
				addExpAndSp(player, 3876316782L, 9303137);
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
				htmltext = "31590-01.htm";
				break;
			}
			case State.STARTED:
			{
				switch (qs.getCond())
				{
					case 1:
					case 2:
					case 3:
					{
						htmltext = "31590-05.html";
						break;
					}
					case 4:
					{
						htmltext = "31590-06.html";
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
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && (qs.getCond() >= 1) && killer.isInsideRadius3D(npc, Config.ALT_PARTY_RANGE))
		{
			switch (qs.getCond())
			{
				case 1:
				{
					switch (npc.getId())
					{
						case SMAUG:
						case LUNATIKAN:
						case JABBERWOK:
						case KANZAROTH:
						case KANDILOTH:
						case GARION:
						case GARION_NETI:
						case DESERT_WENDIGO:
						case KORAZA:
						case BEND_BEETLE:
						{
							if (getRandom(100) < 25)
							{
								npc.doCast(DESERT_THIRST.getSkill());
								qs.setCond(2, true);
							}
							break;
						}
					}
					break;
				}
				case 2:
				{
					switch (npc.getId())
					{
						case SMAUG:
						case LUNATIKAN:
						case JABBERWOK:
						case KANZAROTH:
						case KANDILOTH:
						case GARION:
						case GARION_NETI:
						case DESERT_WENDIGO:
						case KORAZA:
						case BEND_BEETLE:
						{
							if (getRandom(100) < 25)
							{
								final Npc mob = addSpawn(VIPER, npc.getX(), npc.getY(), npc.getZ(), 0, true, 120000);
								addAttackPlayerDesire(mob, killer, 5);
								qs.setCond(3, true);
							}
							break;
						}
					}
					break;
				}
				case 3:
				{
					switch (npc.getId())
					{
						case SMAUG:
						case LUNATIKAN:
						case JABBERWOK:
						case KANZAROTH:
						case KANDILOTH:
						case GARION:
						case GARION_NETI:
						case DESERT_WENDIGO:
						case KORAZA:
						case BEND_BEETLE:
						{
							if (getRandom(100) < 25)
							{
								final Npc mob = addSpawn(VIPER, npc.getX(), npc.getY(), npc.getZ(), 0, true, 120000);
								addAttackPlayerDesire(mob, killer, 5);
							}
							break;
						}
						case VIPER:
						{
							if (getRandom(100) < 25)
							{
								qs.setCond(4, true);
							}
							break;
						}
					}
					break;
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}