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
package quests.Q00695_DefendTheHallOfSuffering;

import java.util.Calendar;

import org.l2jmobius.gameserver.instancemanager.InstanceManager;
import org.l2jmobius.gameserver.instancemanager.SoIManager;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.model.instancezone.InstanceWorld;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class Q00695_DefendTheHallOfSuffering extends Quest
{
	private static final int TEPIOS = 32603;
	private static final int TEPIOS2 = 32530;
	private static final int SOE = 736;
	private static final int TEMPLATE_ID = 116;
	
	public Q00695_DefendTheHallOfSuffering()
	{
		super(695);
		addStartNpc(TEPIOS);
		addTalkId(TEPIOS);
		addTalkId(TEPIOS2);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final String htmltext = event;
		final QuestState qs = player.getQuestState(getName());
		if (qs == null)
		{
			return htmltext;
		}
		
		if (event.equals("32603-02.html"))
		{
			qs.startQuest();
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
				if ((player.getLevel() >= 75) && (player.getLevel() <= 82))
				{
					if (SoIManager.getCurrentStage() == 4)
					{
						htmltext = "32603-01.htm";
					}
					else
					{
						htmltext = "32603-04.htm";
					}
				}
				else
				{
					htmltext = "32603-00.html";
					qs.exitQuest(true);
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case TEPIOS:
					{
						htmltext = "32603-01a.html";
						break;
					}
					case TEPIOS2:
					{
						final InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
						if ((world != null) && (world.getTemplateId() == TEMPLATE_ID))
						{
							final int tag = world.getParameters().getInt("tag", -1);
							if (tag == -1)
							{
								htmltext = "32530-11.html";
							}
							else if ((player.getParty() != null) && (player.getParty().getLeaderObjectId() == player.getObjectId()))
							{
								for (Player member : player.getParty().getMembers())
								{
									final QuestState qs1 = member.getQuestState(getName());
									if (qs1 != null)
									{
										if (tag == 13777)
										{
											giveItems(member, 13777, 1);
											giveItems(member, SOE, 1);
											qs1.unset("cond");
											qs1.exitQuest(true);
											playSound(member, "ItemSound.quest_finish");
											htmltext = "32530-00.html";
											finishInstance(player);
										}
										else if (tag == 13778)
										{
											giveItems(member, 13778, 1);
											giveItems(member, SOE, 1);
											qs1.unset("cond");
											qs1.exitQuest(true);
											playSound(member, "ItemSound.quest_finish");
											htmltext = "32530-01.html";
											finishInstance(player);
										}
										else if (tag == 13779)
										{
											giveItems(member, 13779, 1);
											giveItems(member, SOE, 1);
											qs1.unset("cond");
											qs1.exitQuest(true);
											playSound(member, "ItemSound.quest_finish");
											htmltext = "32530-02.html";
											finishInstance(player);
										}
										else if (tag == 13780)
										{
											giveItems(member, 13780, 1);
											giveItems(member, SOE, 1);
											qs1.unset("cond");
											qs1.exitQuest(true);
											playSound(member, "ItemSound.quest_finish");
											htmltext = "32530-03.html";
											finishInstance(player);
										}
										else if (tag == 13781)
										{
											giveItems(member, 13781, 1);
											giveItems(member, SOE, 1);
											qs1.unset("cond");
											qs1.exitQuest(true);
											playSound(member, "ItemSound.quest_finish");
											htmltext = "32530-04.html";
											finishInstance(player);
										}
										else if (tag == 13782)
										{
											giveItems(member, 13782, 1);
											giveItems(member, SOE, 1);
											qs1.unset("cond");
											qs1.exitQuest(true);
											playSound(member, "ItemSound.quest_finish");
											htmltext = "32530-05.html";
											finishInstance(player);
										}
										else if (tag == 13783)
										{
											giveItems(member, 13783, 1);
											giveItems(member, SOE, 1);
											qs1.unset("cond");
											qs1.exitQuest(true);
											playSound(member, "ItemSound.quest_finish");
											htmltext = "32530-06.html";
											finishInstance(player);
										}
										else if (tag == 13784)
										{
											giveItems(member, 13784, 1);
											giveItems(member, SOE, 1);
											qs1.unset("cond");
											qs1.exitQuest(true);
											playSound(member, "ItemSound.quest_finish");
											htmltext = "32530-07.html";
											finishInstance(player);
										}
										else if (tag == 13785)
										{
											giveItems(member, 13785, 1);
											giveItems(member, SOE, 1);
											qs1.unset("cond");
											qs1.exitQuest(true);
											playSound(member, "ItemSound.quest_finish");
											htmltext = "32530-08.html";
											finishInstance(player);
										}
										else if (tag == 13786)
										{
											giveItems(member, 13786, 1);
											giveItems(member, SOE, 1);
											qs1.unset("cond");
											qs1.exitQuest(true);
											playSound(member, "ItemSound.quest_finish");
											htmltext = "32530-09.html";
											finishInstance(player);
										}
										else
										{
											htmltext = "32530-11.html";
										}
									}
								}
							}
							else
							{
								return "32530-10.html";
							}
						}
						else
						{
							htmltext = "32530-11.html";
						}
						break;
					}
				}
				break;
			}
		}
		return htmltext;
	}
	
	private void finishInstance(Player player)
	{
		final InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		final Calendar reenter = Calendar.getInstance();
		reenter.set(Calendar.MINUTE, 30);
		if (reenter.get(Calendar.HOUR_OF_DAY) >= 6)
		{
			reenter.add(Calendar.DATE, 1);
		}
		reenter.set(Calendar.HOUR_OF_DAY, 6);
		
		final SystemMessage sm = new SystemMessage(SystemMessageId.INSTANT_ZONE_S1_S_ENTRY_HAS_BEEN_RESTRICTED_YOU_CAN_CHECK_THE_NEXT_POSSIBLE_ENTRY_TIME_BY_USING_THE_COMMAND_INSTANCEZONE);
		sm.addInstanceName(TEMPLATE_ID);
		
		for (Player plr : world.getAllowed())
		{
			if (plr != null)
			{
				InstanceManager.getInstance().setInstanceTime(plr.getObjectId(), TEMPLATE_ID, reenter.getTimeInMillis());
				if (plr.isOnline())
				{
					plr.sendPacket(sm);
				}
			}
		}
		final Instance inst = InstanceManager.getInstance().getInstance(world.getInstanceId());
		inst.setDuration(5 * 60000);
		inst.setEmptyDestroyTime(0);
	}
}