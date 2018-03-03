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
package quests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.l2jmobius.commons.util.Rnd;
import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.instancemanager.QuestManager;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Party;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.L2Attackable;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;
import com.l2jmobius.gameserver.model.quest.jython.QuestJython;
import com.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;

public class SagasSuperClass extends QuestJython
{
	public String qn = "SagasSuperClass";
	public int qnu;
	public int[] NPC = {};
	public int[] Items = {};
	public int[] Mob = {};
	public int[] classid = {};
	public int[] prevclass = {};
	public int[] X = {};
	public int[] Y = {};
	public int[] Z = {};
	public String[] Text = {};
	Map<L2NpcInstance, Integer> _SpawnList = new HashMap<>();
	// @formatter:off
	private static int[][] QuestClass =
	{
		{ 0x7f }, { 0x80, 0x81 }, { 0x82 }, { 0x05 }, { 0x14 }, { 0x15 },
		{ 0x02 }, { 0x03 }, { 0x2e }, { 0x30 }, { 0x33 }, { 0x34 }, { 0x08 },
		{ 0x17 }, { 0x24 }, { 0x09 }, { 0x18 }, { 0x25 }, { 0x10 }, { 0x11 },
		{ 0x1e }, { 0x0c }, { 0x1b }, { 0x28 }, { 0x0e }, { 0x1c }, { 0x29 },
		{ 0x0d }, { 0x06 }, { 0x22 }, { 0x21 }, { 0x2b }, { 0x37 }, { 0x39 }
	};
	// @formatter:on
	
	public SagasSuperClass(int id, String name, String descr)
	{
		super(id, name, descr);
		qnu = id;
	}
	
	public void registerNPCs()
	{
		addStartNpc(NPC[0]);
		addAttackId(Mob[2]);
		addAttackId(Mob[1]);
		addSkillUseId(Mob[1]);
		addFirstTalkId(NPC[4]);
		for (int npc : NPC)
		{
			addTalkId(npc);
		}
		for (int mobid : Mob)
		{
			addKillId(mobid);
		}
		final int[] questItemIds = Items.clone();
		questItemIds[0] = 0;
		questItemIds[2] = 0; // remove Ice Crystal and Divine Stone of Wisdom
		registerQuestItems(questItemIds);
		for (int Archon_Minion = 21646; Archon_Minion < 21652; Archon_Minion++)
		{
			addKillId(Archon_Minion);
		}
		int[] Archon_Hellisha_Norm =
		{
			18212,
			18214,
			18215,
			18216,
			18218
		};
		for (int element : Archon_Hellisha_Norm)
		{
			addKillId(element);
		}
		for (int Guardian_Angel = 27214; Guardian_Angel < 27217; Guardian_Angel++)
		{
			addKillId(Guardian_Angel);
		}
	}
	
	public void Cast(L2NpcInstance npc, L2Character target, int skillId, int level)
	{
		target.broadcastPacket(new MagicSkillUse(target, target, skillId, level, 6000, 1));
		target.broadcastPacket(new MagicSkillUse(npc, npc, skillId, level, 6000, 1));
	}
	
	public void AddSpawn(QuestState st, L2NpcInstance mob)
	{
		_SpawnList.put(mob, st.getPlayer().getObjectId());
	}
	
	public L2NpcInstance FindSpawn(L2PcInstance player, L2NpcInstance npc)
	{
		if (_SpawnList.containsKey(npc) && (_SpawnList.get(npc) == player.getObjectId()))
		{
			return npc;
		}
		return null;
	}
	
	public void DeleteSpawn(QuestState st, L2NpcInstance npc)
	{
		if (_SpawnList.containsKey(npc))
		{
			_SpawnList.remove(npc);
			npc.deleteMe();
		}
	}
	
	public QuestState findRightState(L2NpcInstance npc)
	{
		L2PcInstance player = null;
		QuestState st = null;
		if (_SpawnList.containsKey(npc))
		{
			player = (L2PcInstance) L2World.getInstance().findObject(_SpawnList.get(npc));
			if (player != null)
			{
				st = player.getQuestState(qn);
			}
		}
		return st;
	}
	
	public void giveHallishaMark(QuestState st2)
	{
		if (st2.getInt("spawned") == 0)
		{
			if (st2.getQuestItemsCount(Items[3]) >= 700)
			{
				st2.takeItems(Items[3], 20);
				int xx = st2.getPlayer().getX();
				int yy = st2.getPlayer().getY();
				int zz = st2.getPlayer().getZ();
				L2NpcInstance Archon = st2.addSpawn(Mob[1], xx, yy, zz);
				AddSpawn(st2, Archon);
				st2.set("spawned", "1");
				st2.startQuestTimer("Archon Hellisha has despawned", 600000, Archon);
				Archon.broadcastNpcSay(Text[13].replace("PLAYERNAME", st2.getPlayer().getName()));
				((L2Attackable) Archon).addDamageHate(st2.getPlayer(), 0, 99999);
				Archon.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, st2.getPlayer(), null);
			}
			else
			{
				st2.giveItems(Items[3], 1);
			}
		}
	}
	
	public QuestState findQuest(L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if (st != null)
		{
			if (qnu != 68)
			{
				if (player.getClassId().getId() == QuestClass[qnu - 67][0])
				{
					return st;
				}
			}
			else
			{
				for (int q = 0; q < 2; q++)
				{
					if (player.getClassId().getId() == QuestClass[1][q])
					{
						return st;
					}
				}
			}
		}
		return null;
	}
	
	public int getClassId(L2PcInstance player)
	{
		if (player.getClassId().getId() == 0x81)
		{
			return classid[1];
		}
		return classid[0];
	}
	
	public int getPrevClass(L2PcInstance player)
	{
		if (player.getClassId().getId() == 0x81)
		{
			if (prevclass.length == 1)
			{
				return -1;
			}
			return prevclass[1];
		}
		return prevclass[0];
	}
	
	@Override
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = "";
		if (st != null)
		{
			if (event.equals("0-011.htm") || event.equals("0-012.htm") || event.equals("0-013.htm") || event.equals("0-014.htm") || event.equals("0-015.htm"))
			{
				htmltext = event;
			}
			else if (event.equals("accept"))
			{
				st.set("cond", "1");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
				st.giveItems(Items[10], 1);
				htmltext = "0-03.htm";
			}
			else if (event.equals("0-1"))
			{
				if (player.getLevel() < 76)
				{
					htmltext = "0-02.htm";
					if (st.getState() == State.CREATED)
					{
						st.exitQuest(true);
					}
				}
				else
				{
					htmltext = "0-05.htm";
				}
			}
			else if (event.equals("0-2"))
			{
				if (player.getLevel() >= 76)
				{
					st.exitQuest(false);
					st.set("cond", "0");
					htmltext = "0-07.htm";
					st.takeItems(Items[10], -1);
					st.rewardExpAndSp(2299404, 0);
					st.giveItems(57, 5000000);
					st.giveItems(6622, 1);
					int Class = getClassId(player);
					int prevClass = getPrevClass(player);
					player.setClassId(Class);
					if (!player.isSubClassActive() && (player.getBaseClass() == prevClass))
					{
						player.setBaseClass(Class);
					}
					player.broadcastUserInfo();
					Cast(npc, player, 4339, 1);
					
					Quest q = QuestManager.getInstance().getQuest("SkillTransfer");
					if (q != null)
					{
						q.startQuestTimer("givePormanders", 1, npc, player);
					}
				}
				else
				{
					st.takeItems(Items[10], -1);
					st.playSound("ItemSound.quest_middle");
					st.set("cond", "20");
					htmltext = "0-08.htm";
				}
			}
			else if (event.equals("1-3"))
			{
				st.set("cond", "3");
				htmltext = "1-05.htm";
			}
			else if (event.equals("1-4"))
			{
				st.set("cond", "4");
				st.takeItems(Items[0], 1);
				if (Items[11] != 0)
				{
					st.takeItems(Items[11], 1);
				}
				st.giveItems(Items[1], 1);
				htmltext = "1-06.htm";
			}
			else if (event.equals("2-1"))
			{
				st.set("cond", "2");
				htmltext = "2-05.htm";
			}
			else if (event.equals("2-2"))
			{
				st.set("cond", "5");
				st.takeItems(Items[1], 1);
				st.giveItems(Items[4], 1);
				htmltext = "2-06.htm";
			}
			else if (event.equals("3-5"))
			{
				htmltext = "3-07.htm";
			}
			else if (event.equals("3-6"))
			{
				st.set("cond", "11");
				htmltext = "3-02.htm";
			}
			else if (event.equals("3-7"))
			{
				st.set("cond", "12");
				htmltext = "3-03.htm";
			}
			else if (event.equals("3-8"))
			{
				st.set("cond", "13");
				st.takeItems(Items[2], 1);
				st.giveItems(Items[7], 1);
				htmltext = "3-08.htm";
			}
			else if (event.equals("4-1"))
			{
				htmltext = "4-010.htm";
			}
			else if (event.equals("4-2"))
			{
				st.giveItems(Items[9], 1);
				st.set("cond", "18");
				st.playSound("ItemSound.quest_middle");
				htmltext = "4-011.htm";
			}
			else if (event.equals("4-3"))
			{
				st.giveItems(Items[9], 1);
				st.set("cond", "18");
				npc.broadcastNpcSay(Text[13].replace("PLAYERNAME", player.getName()));
				st.set("Quest0", "0");
				cancelQuestTimer("Mob_2 has despawned", npc, player);
				st.playSound("ItemSound.quest_middle");
				DeleteSpawn(st, npc);
				return null;
			}
			else if (event.equals("5-1"))
			{
				st.set("cond", "6");
				st.takeItems(Items[4], 1);
				Cast(npc, player, 4546, 1);
				st.playSound("ItemSound.quest_middle");
				htmltext = "5-02.htm";
			}
			else if (event.equals("6-1"))
			{
				st.set("cond", "8");
				st.takeItems(Items[5], 1);
				Cast(npc, player, 4546, 1);
				st.playSound("ItemSound.quest_middle");
				htmltext = "6-03.htm";
			}
			else if (event.equals("7-1"))
			{
				if (st.getInt("spawned") == 1)
				{
					htmltext = "7-03.htm";
				}
				else if (st.getInt("spawned") == 0)
				{
					L2NpcInstance Mob_1 = st.addSpawn(Mob[0], X[0], Y[0], Z[0]);
					st.set("spawned", "1");
					st.startQuestTimer("Mob_1 Timer 1", 500, Mob_1);
					st.startQuestTimer("Mob_1 has despawned", 300000, Mob_1);
					AddSpawn(st, Mob_1);
					htmltext = "7-02.htm";
				}
				else
				{
					htmltext = "7-04.htm";
				}
			}
			else if (event.equals("7-2"))
			{
				st.set("cond", "10");
				st.takeItems(Items[6], 1);
				Cast(npc, player, 4546, 1);
				st.playSound("ItemSound.quest_middle");
				htmltext = "7-06.htm";
			}
			else if (event.equals("8-1"))
			{
				st.set("cond", "14");
				st.takeItems(Items[7], 1);
				Cast(npc, player, 4546, 1);
				st.playSound("ItemSound.quest_middle");
				htmltext = "8-02.htm";
			}
			else if (event.equals("9-1"))
			{
				st.set("cond", "17");
				st.takeItems(Items[8], 1);
				Cast(npc, player, 4546, 1);
				st.playSound("ItemSound.quest_middle");
				htmltext = "9-03.htm";
			}
			else if (event.equals("10-1"))
			{
				if (st.getInt("Quest0") == 0)
				{
					L2NpcInstance Mob_3 = st.addSpawn(Mob[2], X[1], Y[1], Z[1]);
					L2NpcInstance Mob_2 = st.addSpawn(NPC[4], X[2], Y[2], Z[2]);
					AddSpawn(st, Mob_3);
					AddSpawn(st, Mob_2);
					st.set("Mob_2", String.valueOf(Mob_2.getObjectId()));
					st.set("Quest0", "1");
					st.set("Quest1", "45");
					st.startRepeatingQuestTimer("Mob_3 Timer 1", 500, Mob_3);
					st.startQuestTimer("Mob_3 has despawned", 59000, Mob_3);
					st.startQuestTimer("Mob_2 Timer 1", 500, Mob_2);
					st.startQuestTimer("Mob_2 has despawned", 60000, Mob_2);
					htmltext = "10-02.htm";
				}
				else if (st.getInt("Quest1") == 45)
				{
					htmltext = "10-03.htm";
				}
				else
				{
					htmltext = "10-04.htm";
				}
			}
			else if (event.equals("10-2"))
			{
				st.set("cond", "19");
				st.takeItems(Items[9], 1);
				Cast(npc, player, 4546, 1);
				st.playSound("ItemSound.quest_middle");
				htmltext = "10-06.htm";
			}
			else if (event.equals("11-9"))
			{
				st.set("cond", "15");
				htmltext = "11-03.htm";
			}
			else if (event.equals("Mob_1 Timer 1"))
			{
				npc.broadcastNpcSay(Text[0].replace("PLAYERNAME", player.getName()));
				return null;
			}
			else if (event.equals("Mob_1 has despawned"))
			{
				npc.broadcastNpcSay(Text[1].replace("PLAYERNAME", player.getName()));
				st.set("spawned", "0");
				DeleteSpawn(st, npc);
				return null;
			}
			else if (event.equals("Archon Hellisha has despawned"))
			{
				npc.broadcastNpcSay(Text[6].replace("PLAYERNAME", player.getName()));
				st.set("spawned", "0");
				DeleteSpawn(st, npc);
				return null;
			}
			else if (event.equals("Mob_3 Timer 1"))
			{
				L2NpcInstance Mob_2 = FindSpawn(player, (L2NpcInstance) L2World.getInstance().findObject(st.getInt("Mob_2")));
				if (npc.getKnownList().knowsObject(Mob_2))
				{
					((L2Attackable) npc).addDamageHate(Mob_2, 0, 99999);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, Mob_2, null);
					Mob_2.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, npc, null);
					npc.broadcastNpcSay(Text[14].replace("PLAYERNAME", player.getName()));
					cancelQuestTimer("Mob_3 Timer 1", npc, player);
				}
				return null;
			}
			else if (event.equals("Mob_3 has despawned"))
			{
				npc.broadcastNpcSay(Text[15].replace("PLAYERNAME", player.getName()));
				st.set("Quest0", "2");
				DeleteSpawn(st, npc);
				return null;
			}
			else if (event.equals("Mob_2 Timer 1"))
			{
				npc.broadcastNpcSay(Text[7].replace("PLAYERNAME", player.getName()));
				st.startQuestTimer("Mob_2 Timer 2", 1500, npc);
				if (st.getInt("Quest1") == 45)
				{
					st.set("Quest1", "0");
				}
				return null;
			}
			else if (event.equals("Mob_2 Timer 2"))
			{
				npc.broadcastNpcSay(Text[8].replace("PLAYERNAME", player.getName()));
				st.startQuestTimer("Mob_2 Timer 3", 10000, npc);
				return null;
			}
			else if (event.equals("Mob_2 Timer 3"))
			{
				if (st.getInt("Quest0") == 0)
				{
					st.startQuestTimer("Mob_2 Timer 3", 13000, npc);
					if (st.getRandom(2) == 0)
					{
						npc.broadcastNpcSay(Text[9].replace("PLAYERNAME", player.getName()));
					}
					else
					{
						npc.broadcastNpcSay(Text[10].replace("PLAYERNAME", player.getName()));
					}
				}
				return null;
			}
			else if (event.equals("Mob_2 has despawned"))
			{
				st.set("Quest1", String.valueOf(st.getInt("Quest1") + 1));
				if ((st.getInt("Quest0") == 1) || (st.getInt("Quest0") == 2) || (st.getInt("Quest1") > 3))
				{
					st.set("Quest0", "0");
					if (st.getInt("Quest0") == 1)
					{
						npc.broadcastNpcSay(Text[11].replace("PLAYERNAME", player.getName()));
					}
					else
					{
						npc.broadcastNpcSay(Text[12].replace("PLAYERNAME", player.getName()));
					}
					DeleteSpawn(st, npc);
				}
				else
				{
					st.startQuestTimer("Mob_2 has despawned", 1000, npc);
				}
				return null;
			}
		}
		else
		{
			return null;
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2NpcInstance npc, L2PcInstance talker)
	{
		String htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>";
		QuestState st = talker.getQuestState(qn);
		if (st != null)
		{
			int npcId = npc.getNpcId();
			int cond = st.getInt("cond");
			if ((st.getState() == State.COMPLETED) && (npcId == NPC[0]))
			{
				htmltext = "<html><body>You have already completed this quest!</body></html>";
			}
			else if (st.getPlayer().getClassId().getId() == getPrevClass(st.getPlayer()))
			{
				if (cond == 0)
				{
					if (npcId == NPC[0])
					{
						htmltext = "0-01.htm";
					}
				}
				else if (cond == 1)
				{
					if (npcId == NPC[0])
					{
						htmltext = "0-04.htm";
					}
					else if (npcId == NPC[2])
					{
						htmltext = "2-01.htm";
					}
				}
				else if (cond == 2)
				{
					if (npcId == NPC[2])
					{
						htmltext = "2-02.htm";
					}
					else if (npcId == NPC[1])
					{
						htmltext = "1-01.htm";
					}
				}
				else if (cond == 3)
				{
					if ((npcId == NPC[1]) && (st.getQuestItemsCount(Items[0]) != 0))
					{
						htmltext = "1-02.htm";
						if ((Items[11] == 0) || (st.getQuestItemsCount(Items[11]) != 0))
						{
							htmltext = "1-03.htm";
						}
					}
				}
				else if (cond == 4)
				{
					if (npcId == NPC[1])
					{
						htmltext = "1-04.htm";
					}
					else if (npcId == NPC[2])
					{
						htmltext = "2-03.htm";
					}
				}
				else if (cond == 5)
				{
					if (npcId == NPC[2])
					{
						htmltext = "2-04.htm";
					}
					else if (npcId == NPC[5])
					{
						htmltext = "5-01.htm";
					}
				}
				else if (cond == 6)
				{
					if (npcId == NPC[5])
					{
						htmltext = "5-03.htm";
					}
					else if (npcId == NPC[6])
					{
						htmltext = "6-01.htm";
					}
				}
				else if (cond == 7)
				{
					if (npcId == NPC[6])
					{
						htmltext = "6-02.htm";
					}
				}
				else if (cond == 8)
				{
					if (npcId == NPC[6])
					{
						htmltext = "6-04.htm";
					}
					else if (npcId == NPC[7])
					{
						htmltext = "7-01.htm";
					}
				}
				else if (cond == 9)
				{
					if (npcId == NPC[7])
					{
						htmltext = "7-05.htm";
					}
				}
				else if (cond == 10)
				{
					if (npcId == NPC[7])
					{
						htmltext = "7-07.htm";
					}
					else if (npcId == NPC[3])
					{
						htmltext = "3-01.htm";
					}
				}
				else if ((cond == 11) || (cond == 12))
				{
					if (npcId == NPC[3])
					{
						if (st.getQuestItemsCount(Items[2]) > 0)
						{
							htmltext = "3-05.htm";
						}
						else
						{
							htmltext = "3-04.htm";
						}
					}
				}
				else if (cond == 13)
				{
					if (npcId == NPC[3])
					{
						htmltext = "3-06.htm";
					}
					else if (npcId == NPC[8])
					{
						htmltext = "8-01.htm";
					}
				}
				else if (cond == 14)
				{
					if (npcId == NPC[8])
					{
						htmltext = "8-03.htm";
					}
					else if (npcId == NPC[11])
					{
						htmltext = "11-01.htm";
					}
				}
				else if (cond == 15)
				{
					if (npcId == NPC[11])
					{
						htmltext = "11-02.htm";
					}
					else if (npcId == NPC[9])
					{
						htmltext = "9-01.htm";
					}
				}
				else if (cond == 16)
				{
					if (npcId == NPC[9])
					{
						htmltext = "9-02.htm";
					}
				}
				else if (cond == 17)
				{
					if (npcId == NPC[9])
					{
						htmltext = "9-04.htm";
					}
					else if (npcId == NPC[10])
					{
						htmltext = "10-01.htm";
					}
				}
				else if (cond == 18)
				{
					if (npcId == NPC[10])
					{
						htmltext = "10-05.htm";
					}
				}
				else if (cond == 19)
				{
					if (npcId == NPC[10])
					{
						htmltext = "10-07.htm";
					}
					else if (npcId == NPC[0])
					{
						htmltext = "0-06.htm";
					}
				}
				else if (cond == 20)
				{
					if (npcId == NPC[0])
					{
						if (st.getPlayer().getLevel() >= 76)
						{
							htmltext = "0-09.htm";
							if ((getClassId(st.getPlayer()) < 131) || (getClassId(st.getPlayer()) > 135)) // in Kamael quests, npc wants to chat for a bit before changing class
							{
								st.exitQuest(false);
								st.set("cond", "0");
								st.rewardExpAndSp(2299404, 0);
								st.giveItems(57, 5000000);
								st.giveItems(6622, 1);
								int Class = getClassId(st.getPlayer());
								int prevClass = getPrevClass(st.getPlayer());
								st.getPlayer().setClassId(Class);
								if (!st.getPlayer().isSubClassActive() && (st.getPlayer().getBaseClass() == prevClass))
								{
									st.getPlayer().setBaseClass(Class);
								}
								st.getPlayer().broadcastUserInfo();
								Cast(npc, st.getPlayer(), 4339, 1);
								
								Quest q = QuestManager.getInstance().getQuest("SkillTransfer");
								if (q != null)
								{
									q.startQuestTimer("givePormanders", 1, npc, st.getPlayer());
								}
							}
						}
						else
						{
							htmltext = "0-010.htm";
						}
					}
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2NpcInstance npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(qn);
		int npcId = npc.getNpcId();
		if (st != null)
		{
			int cond = st.getInt("cond");
			if (npcId == NPC[4])
			{
				if (cond == 17)
				{
					QuestState st2 = findRightState(npc);
					if (st2 != null)
					{
						player.setLastQuestNpcObject(npc.getObjectId());
						if (st == st2)
						{
							if (st.getInt("Tab") == 1)
							{
								if (st.getInt("Quest0") == 0)
								{
									htmltext = "4-04.htm";
								}
								else if (st.getInt("Quest0") == 1)
								{
									htmltext = "4-06.htm";
								}
							}
							else
							{
								if (st.getInt("Quest0") == 0)
								{
									htmltext = "4-01.htm";
								}
								else if (st.getInt("Quest0") == 1)
								{
									htmltext = "4-03.htm";
								}
							}
						}
						else
						{
							if (st.getInt("Tab") == 1)
							{
								if (st.getInt("Quest0") == 0)
								{
									htmltext = "4-05.htm";
								}
								else if (st.getInt("Quest0") == 1)
								{
									htmltext = "4-07.htm";
								}
							}
							else
							{
								if (st.getInt("Quest0") == 0)
								{
									htmltext = "4-02.htm";
								}
							}
						}
					}
				}
				else if (cond == 18)
				{
					htmltext = "4-08.htm";
				}
			}
		}
		if (htmltext == "")
		{
			npc.showChatWindow(player);
		}
		return htmltext;
	}
	
	@Override
	public String onAttack(L2NpcInstance npc, L2PcInstance player, int damage, boolean isPet)
	{
		QuestState st2 = findRightState(npc);
		if (st2 == null)
		{
			return super.onAttack(npc, player, damage, isPet);
		}
		int cond = st2.getInt("cond");
		QuestState st = player.getQuestState(qn);
		int npcId = npc.getNpcId();
		if ((npcId == Mob[2]) && (st == st2) && (cond == 17))
		{
			st.set("Quest0", String.valueOf(st.getInt("Quest0") + 1));
			if (st.getInt("Quest0") == 1)
			{
				npc.broadcastNpcSay(Text[16].replace("PLAYERNAME", player.getName()));
			}
			if (st.getInt("Quest0") > 15)
			{
				st.set("Quest0", "1");
				npc.broadcastNpcSay(Text[17].replace("PLAYERNAME", player.getName()));
				cancelQuestTimer("Mob_3 has despawned", npc, st2.getPlayer());
				st.set("Tab", "1");
				DeleteSpawn(st, npc);
			}
		}
		else if ((npcId == Mob[1]) && (cond == 15))
		{
			if ((st != st2) || ((st == st2) && player.isInParty()))
			{
				npc.broadcastNpcSay(Text[5].replace("PLAYERNAME", player.getName()));
				cancelQuestTimer("Archon Hellisha has despawned", npc, st2.getPlayer());
				st2.set("spawned", "0");
				DeleteSpawn(st2, npc);
			}
		}
		return super.onAttack(npc, player, damage, isPet);
	}
	
	@Override
	public String onSkillUse(L2NpcInstance npc, L2PcInstance caster, L2Skill skill)
	{
		if (_SpawnList.containsKey(npc) && (_SpawnList.get(npc) != caster.getObjectId()))
		{
			L2PcInstance quest_player = (L2PcInstance) L2World.getInstance().findObject(_SpawnList.get(npc));
			if (quest_player == null)
			{
				return null;
			}
			for (L2Object obj : skill.getTargetList(caster))
			{
				if ((obj == quest_player) || (obj == npc))
				{
					QuestState st2 = findRightState(npc);
					if (st2 == null)
					{
						return null;
					}
					npc.broadcastNpcSay(Text[5].replace("PLAYERNAME", caster.getName()));
					cancelQuestTimer("Archon Hellisha has despawned", npc, st2.getPlayer());
					st2.set("spawned", "0");
					DeleteSpawn(st2, npc);
				}
			}
		}
		return null;
	}
	
	@Override
	public String onKill(L2NpcInstance npc, L2PcInstance player, boolean isPet)
	{
		int npcId = npc.getNpcId();
		QuestState st = player.getQuestState(qn);
		for (int Archon_Minion = 21646; Archon_Minion < 21652; Archon_Minion++)
		{
			if (npcId == Archon_Minion)
			{
				L2Party party = player.getParty();
				if (party != null)
				{
					List<QuestState> PartyQuestMembers = new ArrayList<>();
					for (L2PcInstance player1 : party.getPartyMembers())
					{
						QuestState st1 = findQuest(player1);
						if (st1 != null)
						{
							if (st1.getInt("cond") == 15)
							{
								PartyQuestMembers.add(st1);
							}
						}
					}
					if (PartyQuestMembers.size() > 0)
					{
						QuestState st2 = PartyQuestMembers.get(Rnd.get(PartyQuestMembers.size()));
						giveHallishaMark(st2);
					}
				}
				else
				{
					QuestState st1 = findQuest(player);
					if (st1 != null)
					{
						if (st1.getInt("cond") == 15)
						{
							giveHallishaMark(st1);
						}
					}
				}
				return super.onKill(npc, player, isPet);
			}
		}
		
		int[] Archon_Hellisha_Norm =
		{
			18212,
			18214,
			18215,
			18216,
			18218
		};
		for (int element : Archon_Hellisha_Norm)
		{
			if (npcId == element)
			{
				QuestState st1 = findQuest(player);
				if (st1 != null)
				{
					if (st1.getInt("cond") == 15)
					{
						// This is just a guess....not really sure what it actually says, if anything
						npc.broadcastNpcSay(Text[4].replace("PLAYERNAME", st1.getPlayer().getName()));
						st1.giveItems(Items[8], 1);
						st1.takeItems(Items[3], -1);
						st1.set("cond", "16");
						st1.playSound("ItemSound.quest_middle");
					}
					
				}
				return super.onKill(npc, player, isPet);
			}
		}
		
		for (int Guardian_Angel = 27214; Guardian_Angel < 27217; Guardian_Angel++)
		{
			if (npcId == Guardian_Angel)
			{
				QuestState st1 = findQuest(player);
				if (st1 != null)
				{
					if (st1.getInt("cond") == 6)
					{
						if (st1.getInt("kills") < 9)
						{
							st1.set("kills", String.valueOf(st1.getInt("kills") + 1));
						}
						else
						{
							st1.playSound("ItemSound.quest_middle");
							st1.giveItems(Items[5], 1);
							st1.set("cond", "7");
						}
					}
					
				}
				return super.onKill(npc, player, isPet);
			}
		}
		if ((st != null) && (npcId != Mob[2]))
		{
			QuestState st2 = findRightState(npc);
			if (st2 == null)
			{
				return super.onKill(npc, player, isPet);
			}
			int cond = st.getInt("cond");
			if ((npcId == Mob[0]) && (cond == 8))
			{
				if (!player.isInParty())
				{
					if (st == st2)
					{
						npc.broadcastNpcSay(Text[12].replace("PLAYERNAME", player.getName()));
						st.giveItems(Items[6], 1);
						st.set("cond", "9");
						st.playSound("ItemSound.quest_middle");
					}
				}
				cancelQuestTimer("Mob_1 has despawned", npc, st2.getPlayer());
				st2.set("spawned", "0");
				DeleteSpawn(st2, npc);
			}
			else if ((npcId == Mob[1]) && (cond == 15))
			{
				if (!player.isInParty())
				{
					if (st == st2)
					{
						npc.broadcastNpcSay(Text[4].replace("PLAYERNAME", player.getName()));
						st.giveItems(Items[8], 1);
						st.takeItems(Items[3], -1);
						st.set("cond", "16");
						st.playSound("ItemSound.quest_middle");
					}
					else
					{
						npc.broadcastNpcSay(Text[5].replace("PLAYERNAME", player.getName()));
					}
				}
				cancelQuestTimer("Archon Hellisha has despawned", npc, st2.getPlayer());
				st2.set("spawned", "0");
				DeleteSpawn(st2, npc);
			}
		}
		else
		{
			if (npcId == Mob[0])
			{
				st = findRightState(npc);
				if (st != null)
				{
					cancelQuestTimer("Mob_1 has despawned", npc, st.getPlayer());
					st.set("spawned", "0");
					DeleteSpawn(st, npc);
				}
			}
			else if (npcId == Mob[1])
			{
				st = findRightState(npc);
				if (st != null)
				{
					cancelQuestTimer("Archon Hellisha has despawned", npc, st.getPlayer());
					st.set("spawned", "0");
					DeleteSpawn(st, npc);
				}
			}
		}
		return super.onKill(npc, player, isPet);
	}
}