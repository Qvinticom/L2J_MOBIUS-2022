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
package quests.Q10538_GiantsEvolution;

import org.l2jmobius.gameserver.enums.Faction;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.model.skill.Skill;

/**
 * Giant's Evolution (10538)
 * @URL https://l2wiki.com/Giant%E2%80%99s_Evolution
 * @author Dmitri
 */
public class Q10538_GiantsEvolution extends Quest
{
	// NPCs
	private static final int RETBACH = 34218;
	// Monsters
	private static final int LESSER_GIANT_SOLDIER = 23748;
	private static final int ESSENCE_LASSER_GIANTS = 23754;
	private static final int ROOT_LASSER_GIANTS = 23749;
	// Items
	private static final int DEMON_TRACE = 46755; // Demon's Trace
	// Reward
	private static final int GINATS_ENERGY = 35563; // item: Giant's Energy
	// skill
	private static final int INJECT_SHINE_ENERGY = 18583;
	// Misc
	private static final int MIN_LEVEL = 100;
	
	public Q10538_GiantsEvolution()
	{
		super(10538);
		addStartNpc(RETBACH);
		addTalkId(RETBACH);
		addKillId(ESSENCE_LASSER_GIANTS, ROOT_LASSER_GIANTS);
		addSkillSeeId(ESSENCE_LASSER_GIANTS, ROOT_LASSER_GIANTS);
		registerQuestItems(DEMON_TRACE);
		addFactionLevel(Faction.GIANT_TRACKERS, 2, "34218-00.htm");
		addCondMinLevel(MIN_LEVEL, "34218-00.htm");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		switch (event)
		{
			case "34218-02.htm":
			case "34218-03.htm":
			{
				htmltext = event;
				break;
			}
			case "34218-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "34218-06.html":
			{
				if (qs.isCond(2))
				{
					if (player.getLevel() >= MIN_LEVEL)
					{
						addExpAndSp(player, 16610832000L, 39865770);
						giveItems(player, GINATS_ENERGY, 1);
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
		if (npc.getId() == RETBACH)
		{
			switch (qs.getState())
			{
				case State.CREATED:
				{
					htmltext = "34218-01.htm";
					break;
				}
				case State.STARTED:
				{
					if (qs.isCond(1))
					{
						htmltext = "34218-04.htm";
					}
					else
					{
						htmltext = "34218-05.html";
					}
					break;
				}
				case State.COMPLETED:
				{
					htmltext = getAlreadyCompletedMsg(player);
					break;
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onSkillSee(Npc npc, Player caster, Skill skill, WorldObject[] targets, boolean isSummon)
	{
		final QuestState qs = getQuestState(caster, false);
		if ((qs != null) && qs.isCond(1) && (skill.getId() == INJECT_SHINE_ENERGY))
		{
			switch (npc.getId())
			{
				case ROOT_LASSER_GIANTS:
				case ESSENCE_LASSER_GIANTS:
				{
					if ((getRandom(100) < 30) && npc.isAffectedBySkill(INJECT_SHINE_ENERGY))
					{
						npc.setScriptValue(1);
						final Npc mob = addSpawn(LESSER_GIANT_SOLDIER, npc, false, 60000L, false);
						addAttackPlayerDesire(mob, caster);
						npc.deleteMe();
					}
					else
					{
						npc.isScriptValue(0);
					}
					break;
				}
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isSummon);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && npc.isScriptValue(0) && giveItemRandomly(killer, DEMON_TRACE, 1, 100, 1, true))
		{
			qs.setCond(2, true);
		}
		return super.onKill(npc, killer, isSummon);
	}
}
