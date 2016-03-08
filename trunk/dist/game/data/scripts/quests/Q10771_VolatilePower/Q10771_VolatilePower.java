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
package quests.Q10771_VolatilePower;

import com.l2jmobius.gameserver.enums.ChatType;
import com.l2jmobius.gameserver.enums.QuestSound;
import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.ItemHolder;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.network.NpcStringId;
import com.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jmobius.gameserver.network.serverpackets.NpcSay;

/**
 * Volatile Power (10771)
 * @URL https://l2wiki.com/Volatile_Power
 * @author Gigi
 */
public class Q10771_VolatilePower extends Quest
{
	// NPCs
	private static final int JANSSEN = 30484;
	private static final int HIDDEN_CRUSHER = 33990;
	// Monster
	private static final int FRAGMENT_EATER = 27533;
	// Items
	private static final ItemHolder STEEL_DOOR_GUILD = new ItemHolder(37045, 20);
	private static final ItemHolder EAC = new ItemHolder(952, 2);
	private static final int SHINING_MYSTERIUS_FRAGMENT = 39713;
	private static final int NORMAL_FRAGMENT_DUST = 39714;
	// Reward
	private static final int EXP_REWARD = 2708350;
	private static final int SP_REWARD = 650;
	// Misc
	private static final int MIN_LEVEL = 44;
	
	public Q10771_VolatilePower()
	{
		super(10771, Q10771_VolatilePower.class.getSimpleName(), "Volatile Power");
		addStartNpc(JANSSEN);
		addTalkId(JANSSEN, HIDDEN_CRUSHER);
		registerQuestItems(SHINING_MYSTERIUS_FRAGMENT, NORMAL_FRAGMENT_DUST);
		addKillId(FRAGMENT_EATER);
		addCondMinLevel(MIN_LEVEL, "no_level.htm");
		addCondRace(Race.ERTHEIA, "noErtheya.html");
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
			case "30484-02.htm":
			case "30484-03.htm":
			case "30484-04.htm":
			{
				htmltext = event;
				break;
			}
			case "30484-05.htm":
			{
				qs.startQuest();
				htmltext = event;
				giveItems(player, SHINING_MYSTERIUS_FRAGMENT, 20);
				qs.set(Integer.toString(NORMAL_FRAGMENT_DUST), 0);
				qs.setCond(2);
				break;
			}
			case "30484-08.html":
			{
				takeItems(player, NORMAL_FRAGMENT_DUST, 20);
				addExpAndSp(player, EXP_REWARD, SP_REWARD);
				giveItems(player, STEEL_DOOR_GUILD);
				giveItems(player, EAC);
				qs.exitQuest(false, true);
				htmltext = event;
				break;
			}
			case "attack":
			{
				if ((getQuestItemsCount(player, SHINING_MYSTERIUS_FRAGMENT) > 0) && qs.isCond(2))
				{
					final L2Npc mob1 = addSpawn(FRAGMENT_EATER, qs.getPlayer().getX() + getRandom(-150, 150), qs.getPlayer().getY() + getRandom(-150, 150), qs.getPlayer().getZ(), getRandom(64000), false, 180000);
					npc.broadcastPacket(new NpcSay(mob1.getObjectId(), ChatType.NPC_GENERAL, FRAGMENT_EATER, NpcStringId.KILL_THEM_DON_T_LET_THEM_GET_AWAY_WITH_THE_FRAGMENT));
					addAttackDesire(mob1, qs.getPlayer());
					final L2Npc mob2 = addSpawn(FRAGMENT_EATER, qs.getPlayer().getX() + getRandom(-150, 150), qs.getPlayer().getY() + getRandom(-150, 150), qs.getPlayer().getZ(), getRandom(64000), false, 180000);
					npc.broadcastPacket(new NpcSay(mob2.getObjectId(), ChatType.NPC_GENERAL, FRAGMENT_EATER, NpcStringId.KILL_THEM_DON_T_LET_THEM_GET_AWAY_WITH_THE_FRAGMENT));
					addAttackDesire(mob2, qs.getPlayer());
					final L2Npc mob3 = addSpawn(FRAGMENT_EATER, qs.getPlayer().getX() + getRandom(-150, 150), qs.getPlayer().getY() + getRandom(-150, 150), qs.getPlayer().getZ(), getRandom(64000), false, 180000);
					npc.broadcastPacket(new NpcSay(mob3.getObjectId(), ChatType.NPC_GENERAL, FRAGMENT_EATER, NpcStringId.KILL_THEM_DON_T_LET_THEM_GET_AWAY_WITH_THE_FRAGMENT));
					addAttackDesire(mob3, qs.getPlayer());
					showOnScreenMsg(player, NpcStringId.THE_DEVICE_RAN_OUT_OF_MAGIC_TRY_LOOKING_FOR_ANOTHER, ExShowScreenMessage.TOP_CENTER, 4500);
					npc.deleteMe();
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
		String htmltext = getNoQuestMsg(player);
		
		switch (npc.getId())
		{
			case JANSSEN:
			{
				if (qs.isCreated())
				{
					htmltext = "30484-01.htm";
				}
				else if (qs.isStarted())
				{
					if (qs.isCond(2))
					{
						htmltext = "30484-06.html";
					}
				}
				else if (qs.isCond(3))
				{
					htmltext = "30484-07.html";
				}
				else if (qs.isCompleted())
				{
					htmltext = getAlreadyCompletedMsg(player);
				}
				break;
			}
			case HIDDEN_CRUSHER:
			{
				if (qs.isCond(2))
				{
					htmltext = "33990-01.html";
				}
				else if (qs.isCompleted())
				{
					htmltext = "33990-02.html";
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		final long count = getQuestItemsCount(killer, SHINING_MYSTERIUS_FRAGMENT);
		if ((qs != null) && qs.isCond(2) && (count > 0))
		{
			if (getRandom(10) < 5)
			{
				takeItems(killer, SHINING_MYSTERIUS_FRAGMENT, 1);
				giveItems(killer, NORMAL_FRAGMENT_DUST, 1);
				playSound(killer, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			if (getQuestItemsCount(killer, NORMAL_FRAGMENT_DUST) >= 20)
			{
				qs.setCond(3, true);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
}