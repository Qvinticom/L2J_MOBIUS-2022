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
package quests.Q10791_TheManOfMystery;

import com.l2jmobius.gameserver.enums.QuestSound;
import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.base.ClassId;
import com.l2jmobius.gameserver.model.holders.ItemHolder;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.network.serverpackets.ExQuestNpcLogList;
import com.l2jmobius.gameserver.util.Util;

import quests.Q10790_AMercenaryHelper.Q10790_AMercenaryHelper;

/**
 * The Man Of Mystery (10791)
 * @URL https://l2wiki.com/The_Man_of_Mystery
 * @author Stayway reworked Gigi
 */
public class Q10791_TheManOfMystery extends Quest
{
	// NPCs
	private static final int DOKARA = 33847;
	private static final int KAIN_VAN_HALTER = 33993;
	// Monster
	private static final int SUSPICIOUS_COCOON = 27536;
	private static final int SUSPICIOUS_COCOON1 = 27537;
	private static final int SUSPICIOUS_COCOON2 = 27538;
	private static final int NEEDLE_STAKATO_CAPTAIN = 27542;
	private static final int NEEDLE_STAKATO = 27543;
	// Items
	private static final ItemHolder STEEL_DOOR_GUILD = new ItemHolder(37045, 63);
	private static final ItemHolder EAA = new ItemHolder(730, 2);
	// Reward
	private static final int EXP_REWARD = 16968420;
	private static final int SP_REWARD = 4072;
	// Misc
	private static final int MIN_LEVEL = 65;
	private static final int MAX_LEVEL = 70;
	
	public Q10791_TheManOfMystery()
	{
		super(10791, Q10791_TheManOfMystery.class.getSimpleName(), "The Man Of Mystery");
		addStartNpc(DOKARA);
		addTalkId(DOKARA);
		addKillId(SUSPICIOUS_COCOON, SUSPICIOUS_COCOON1, SUSPICIOUS_COCOON2, NEEDLE_STAKATO_CAPTAIN);
		addAttackId(NEEDLE_STAKATO_CAPTAIN);
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "no_level.html");
		addCondRace(Race.ERTHEIA, "noErtheia.html");
		addCondClassId(ClassId.MARAUDER, "no_quest.html");
		addCondCompletedQuest(Q10790_AMercenaryHelper.class.getSimpleName(), "restriction.html");
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return getNoQuestMsg(player);
		}
		
		switch (event)
		{
			case "33847-02.htm":
			case "33847-03.htm":
			{
				htmltext = event;
				break;
			}
			case "33847-04.htm":
			{
				qs.startQuest();
				qs.set(Integer.toString(SUSPICIOUS_COCOON), 0);
				qs.set(Integer.toString(NEEDLE_STAKATO_CAPTAIN), 0);
				htmltext = event;
				break;
			}
			case "33847-07.html":
			{
				if (qs.isCond(3))
				{
					addExpAndSp(player, EXP_REWARD, SP_REWARD);
					giveItems(player, STEEL_DOOR_GUILD);
					giveItems(player, EAA);
					qs.exitQuest(false, true);
					htmltext = event;
					break;
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		
		if (qs.isCreated())
		{
			htmltext = "33847-01.htm";
		}
		else if (qs.isStarted())
		{
			if ((qs.getCond() > 0) && (qs.getCond() < 3))
			{
				htmltext = "33847-05.html";
			}
			else if (qs.isCond(3))
			{
				htmltext = "33847-06.html";
			}
		}
		else if (qs.isCompleted())
		{
			htmltext = getAlreadyCompletedMsg(player);
		}
		
		return htmltext;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if (npc.isScriptValue(0))
		{
			for (int i = 0; i < 5; i++)
			{
				final L2Npc creature = addSpawn(NEEDLE_STAKATO, npc.getX() + getRandom(-20, 20), npc.getY() + getRandom(-20, 20), npc.getZ(), npc.getHeading(), true, 120000, false);
				addAttackDesire(creature, attacker);
			}
			for (int i1 = 0; i1 < 1; i1++)
			{
				final L2Npc helper = addSpawn(KAIN_VAN_HALTER, npc.getX() + getRandom(-100, 100), npc.getY() + getRandom(-100, 100), npc.getZ(), npc.getHeading(), true, 300000, false);
				addAttackDesire(helper, npc);
			}
			npc.setScriptValue(1);
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState qs = getRandomPartyMemberState(killer, -1, 3, npc);
		if ((qs != null) && (qs.getCond() > 0) && Util.checkIfInRange(1500, npc, qs.getPlayer(), false))
		{
			switch (npc.getId())
			{
				case SUSPICIOUS_COCOON:
				case SUSPICIOUS_COCOON1:
				case SUSPICIOUS_COCOON2:
				{
					int kills = qs.getInt(Integer.toString(SUSPICIOUS_COCOON));
					if (kills < 5)
					{
						kills++;
						qs.set(Integer.toString(SUSPICIOUS_COCOON), kills);
						playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
					if (kills >= 5)
					{
						qs.unset(Integer.toString(SUSPICIOUS_COCOON));
						final L2Npc mob1 = addSpawn(NEEDLE_STAKATO_CAPTAIN, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 600000, false);
						addAttackDesire(mob1, qs.getPlayer());
						qs.setCond(2);
					}
					break;
				}
				case NEEDLE_STAKATO_CAPTAIN:
				{
					int kills = qs.getInt(Integer.toString(NEEDLE_STAKATO_CAPTAIN));
					if ((kills < 1) && qs.isCond(2))
					{
						kills++;
						qs.set(Integer.toString(NEEDLE_STAKATO_CAPTAIN), kills);
						playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
					if (qs.getInt(Integer.toString(NEEDLE_STAKATO_CAPTAIN)) >= 1)
					{
						qs.setCond(3, true);
					}
					break;
				}
			}
			final ExQuestNpcLogList log = new ExQuestNpcLogList(getId());
			log.addNpc(SUSPICIOUS_COCOON, qs.getInt(Integer.toString(SUSPICIOUS_COCOON)));
			log.addNpc(NEEDLE_STAKATO_CAPTAIN, qs.getInt(Integer.toString(NEEDLE_STAKATO_CAPTAIN)));
			qs.getPlayer().sendPacket(log);
		}
		return super.onKill(npc, killer, isSummon);
	}
}