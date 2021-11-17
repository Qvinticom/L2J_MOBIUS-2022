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
package quests.Q10537_KamaelDisarray;

import java.util.HashSet;
import java.util.Set;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.enums.Faction;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.FriendlyNpc;
import org.l2jmobius.gameserver.model.holders.NpcLogListHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.NpcStringId;

import quests.Q10538_GiantsEvolution.Q10538_GiantsEvolution;

/**
 * Kamael Disarray (10537)
 * @URL https://l2wiki.com/Kamael_Disarray
 * @author Gigi
 * @date 2018-02-04 - [12:06:33]
 */
public class Q10537_KamaelDisarray extends Quest
{
	// NPCs
	private static final int KRENAHT = 34237;
	private static final int RETBACH = 34218;
	private static final int STHOR = 34224;
	private static final int VETLE = 34225;
	// Monsters
	private static final int LESSER_GIANT_SOLDIER = 23748;
	private static final int ESSENCE_LASSER_GIANTS = 23754;
	private static final int ROOT_LASSER_GIANTS = 23749;
	// Items
	private static final int MINIONS_SHINE_STONE = 46748;
	private static final int MINIONS_REPOT = 46756;
	// Reward
	private static final int ELEXIR_OF_LIFE = 37097;
	private static final int ELEXIR_OF_MIND = 37098;
	private static final int ELEXIR_OF_CP = 37099;
	// skill
	private static final int INJECT_SHINE_ENERGY = 18583;
	// Misc
	private static final int KILLING_NPCSTRING_ID = NpcStringId.HELPING_THE_EVOLUTION_OF_THE_LESSER_GIANT.getId();
	private static final boolean PARTY_QUEST = false;
	private static final int MIN_LEVEL = 100;
	
	public Q10537_KamaelDisarray()
	{
		super(10537);
		addStartNpc(KRENAHT);
		addTalkId(KRENAHT, RETBACH);
		addKillId(LESSER_GIANT_SOLDIER);
		addSkillSeeId(ESSENCE_LASSER_GIANTS, ROOT_LASSER_GIANTS);
		registerQuestItems(MINIONS_REPOT);
		// addCreatureSeeId(RETBACH, STHOR, VETLE);
		addFactionLevel(Faction.GIANT_TRACKERS, 2, "34237-00.htm");
		addCondMinLevel(MIN_LEVEL, "34237-00.htm");
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
			case "34237-02.htm":
			case "34237-03.htm":
			case "34218-02.html":
			case "34218-03.html":
			case "34237-08.html":
			{
				htmltext = event;
				break;
			}
			case "34237-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "34218-04.html":
			{
				giveItems(player, MINIONS_SHINE_STONE, 1);
				qs.setCond(2, true);
				htmltext = event;
				break;
			}
			case "34218-06.html":
			{
				takeItems(player, MINIONS_SHINE_STONE, -1);
				giveItems(player, MINIONS_REPOT, 1);
				qs.setCond(4, true);
				htmltext = event;
				break;
			}
			case "34237-07.html":
			{
				takeItems(player, MINIONS_REPOT, -1);
				qs.setCond(5, true);
				htmltext = event;
				break;
			}
			case "spawn":
			{
				addSpawn(STHOR, 183825, 47249, -4360, 13558, false, 15000);
				Npc vatle = addSpawn(VETLE, 183872, 47271, -4360, 28150, false, 15000);
				startQuestTimer("attack", 8000, vatle, player);
				qs.setCond(6, true);
				break;
			}
			case "attack":
			{
				World.getInstance().forEachVisibleObjectInRange(npc, FriendlyNpc.class, 500, cha ->
				{
					if (cha.getId() == RETBACH)
					{
						npc.setTarget(cha);
						npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, cha);
						ThreadPool.schedule(() -> cha.reduceCurrentHp(1000000, npc, null), 6000);
					}
				});
				break;
			}
			case "34237-10.html":
			{
				if (qs.isCond(6))
				{
					if (player.getLevel() >= MIN_LEVEL)
					{
						addExpAndSp(player, 7382592000L, 17718120);
						giveItems(player, ELEXIR_OF_LIFE, 8);
						giveItems(player, ELEXIR_OF_MIND, 8);
						giveItems(player, ELEXIR_OF_CP, 4);
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
		String htmltext = getNoQuestMsg(player);
		final QuestState qs = getQuestState(player, true);
		if (npc.getId() == KRENAHT)
		{
			switch (qs.getState())
			{
				case State.CREATED:
				{
					htmltext = "34237-01.htm";
					break;
				}
				case State.STARTED:
				{
					if ((qs.getCond() > 0) && (qs.getCond() < 4))
					{
						htmltext = "34237-05.html";
					}
					else if (qs.isCond(4))
					{
						htmltext = "34237-06.html";
					}
					else if (qs.isCond(6))
					{
						htmltext = "34237-09.html";
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
		if (npc.getId() == RETBACH)
		{
			final QuestState qs10538 = player.getQuestState(Q10538_GiantsEvolution.class.getSimpleName());
			switch (qs.getState())
			{
				case State.STARTED:
				{
					if (qs.isCond(1))
					{
						htmltext = "34218-01.html";
					}
					else if (qs.isCond(2))
					{
						htmltext = "34218-04.html";
					}
					else if (qs.isCond(3) && (qs10538 != null) && qs10538.isCompleted())
					{
						htmltext = "34218-05.html";
					}
					else if (qs.isCond(3))
					{
						htmltext = "34218-07.html";
					}
					else if (qs.isCond(4))
					{
						htmltext = "34218-06.html";
					}
					else if (qs.isCond(5))
					{
						htmltext = "34218-08.html";
					}
					else if (qs.isCond(6))
					{
						htmltext = "34218-09.html";
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
	
	// public String onCreatureSee(Npc npc, Creature creature)
	// {
	// final Npc npc = (Npc) event.getCreature();
	// final Creature creature = event.getSeen();
	// switch (npc.getId())
	// {
	// case STHOR:
	// {
	// if ((creature != null) && (creature.getId() == RETBACH))
	// {
	// sendMessage(npc, 553810, 1500); // Henchman of the Giants! Surrender your head for our independence!
	// }
	// break;
	// }
	// case RETBACH:
	// {
	// if ((creature != null) && (creature.getId() == STHOR))
	// {
	// sendMessage(npc, 553811, 4000); // You ungrateful fiend! I shall report this to Hermuncus!
	// }
	// break;
	// }
	// case VETLE:
	// {
	// if ((creature != null) && (creature.getId() == RETBACH))
	// {
	// sendMessage(npc, 553810, 8000); // Henchman of the Giants! Surrender your head for our independence!
	// }
	// break;
	// }
	// }
	// }
	
	@Override
	public String onSkillSee(Npc npc, Player caster, Skill skill, WorldObject[] targets, boolean isSummon)
	{
		final QuestState qs = getQuestState(caster, false);
		if ((qs != null) && qs.isCond(2) && (skill.getId() == INJECT_SHINE_ENERGY))
		{
			switch (npc.getId())
			{
				case ROOT_LASSER_GIANTS:
				case ESSENCE_LASSER_GIANTS:
				{
					if ((getRandom(100) < 30) && npc.isAffectedBySkill(INJECT_SHINE_ENERGY))
					{
						final Npc mob = addSpawn(LESSER_GIANT_SOLDIER, npc, false, 60000L, false);
						addAttackPlayerDesire(mob, caster);
						npc.deleteMe();
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
		final QuestState qs = PARTY_QUEST ? getRandomPartyMemberState(killer, -1, 3, npc) : getQuestState(killer, false);
		if ((qs != null) && qs.isCond(2))
		{
			final int killedGhosts = qs.getInt("AncientGhosts") + 1;
			qs.set("AncientGhosts", killedGhosts);
			playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			if (killedGhosts >= 30)
			{
				qs.setCond(3, true);
			}
			sendNpcLogList(killer);
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public Set<NpcLogListHolder> getNpcLogList(Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCond(2))
		{
			final Set<NpcLogListHolder> holder = new HashSet<>();
			holder.add(new NpcLogListHolder(KILLING_NPCSTRING_ID, true, qs.getInt("AncientGhosts")));
			return holder;
		}
		return super.getNpcLogList(player);
	}
	
	// private void sendMessage(Npc npc, int msgId, int delay)
	// {
	// ThreadPool.schedule(() ->
	// {
	// if (npc != null)
	// {
	// npc.broadcastPacket(new NpcSay(npc.getObjectId(), ChatType.NPC_GENERAL, npc.getId(), msgId));
	// }
	// }, delay);
	// }
}