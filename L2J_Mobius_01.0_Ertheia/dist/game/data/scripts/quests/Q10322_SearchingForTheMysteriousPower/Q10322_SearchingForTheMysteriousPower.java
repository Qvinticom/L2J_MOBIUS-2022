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
package quests.Q10322_SearchingForTheMysteriousPower;

import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.TutorialShowHtml;
import org.l2jmobius.gameserver.util.Util;

import quests.Q10321_QualificationsOfTheSeeker.Q10321_QualificationsOfTheSeeker;

/**
 * Searching For The Mysterious Power (10322)
 * @author ivantotov, Gladicek
 */
public class Q10322_SearchingForTheMysteriousPower extends Quest
{
	// NPCs
	private static final int SHANNON = 32974;
	private static final int ADVENTURERS_GUIDE = 32981;
	private static final int EVAIN = 33464;
	// Monster
	private static final int SCARECROW = 27457;
	// Reward
	private static final int WOODEN_ARROW = 17;
	private static final int ADENA = 57;
	private static final int HEALING_POTION = 1060;
	private static final int APPRENTICE_ADVENTURERS_STAFF = 7816;
	private static final int APPRENTICE_ADVENTURERS_BONE_CLUB = 7817;
	private static final int APPRENTICE_ADVENTURERS_KNIFE = 7818;
	private static final int APPRENTICE_ADVENTURERS_CESTUS = 7819;
	private static final int APPRENTICE_ADVENTURERS_BOW = 7820;
	private static final int APPRENTICE_ADVENTURERS_LONG_SWORD = 7821;
	// Misc
	private static final int MAX_LEVEL = 20;
	// Buffs
	private static final SkillHolder[] FIGHTER_BUFFS =
	{
		new SkillHolder(4322, 1), // Wind Walk
		new SkillHolder(4323, 1), // Shield
		new SkillHolder(5637, 1), // Magic Barrier
		new SkillHolder(4324, 1), // Bless the Body
		new SkillHolder(4325, 1), // Vampiric Rage
		new SkillHolder(4326, 1), // Regeneration
	};
	private static final SkillHolder[] MAGE_BUFFS =
	{
		new SkillHolder(4322, 1), // Wind Walk
		new SkillHolder(4323, 1), // Shield
		new SkillHolder(5637, 1), // Magic Barrier
		new SkillHolder(4328, 1), // Bless the Soul
		new SkillHolder(4329, 1), // Acumen
		new SkillHolder(4330, 1), // Concentration
		new SkillHolder(4331, 1), // Empower
	};
	
	public Q10322_SearchingForTheMysteriousPower()
	{
		super(10322);
		addStartNpc(SHANNON);
		addTalkId(SHANNON, ADVENTURERS_GUIDE, EVAIN);
		addKillId(SCARECROW);
		addCondMaxLevel(MAX_LEVEL, "32974-01a.html");
		addCondCompletedQuest(Q10321_QualificationsOfTheSeeker.class.getSimpleName(), "32974-01a.html");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "32974-03.html":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "32974-02.htm":
			{
				htmltext = event;
				break;
			}
			case "32981-02.html":
			{
				if (qs.isCond(4))
				{
					final SkillHolder[] buffs = player.isMageClass() ? MAGE_BUFFS : FIGHTER_BUFFS;
					if (buffs != null)
					{
						npc.setTarget(player);
						for (SkillHolder holder : buffs)
						{
							holder.getSkill().applyEffects(npc, player);
						}
					}
					player.sendPacket(new TutorialShowHtml(npc.getObjectId(), "..\\L2Text\\QT_002_Guide_01.htm", TutorialShowHtml.LARGE_WINDOW));
					qs.setCond(5, true);
					htmltext = event;
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && qs.isStarted() && Util.checkIfInRange(1500, npc, killer, true))
		{
			if (qs.isCond(2))
			{
				qs.setCond(3, true);
			}
			else if (qs.isCond(5))
			{
				qs.setCond(6, true);
			}
		}
		return super.onKill(npc, killer, isSummon);
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
				if (npc.getId() == SHANNON)
				{
					htmltext = "32974-01.htm";
					break;
				}
				else if (npc.getId() == EVAIN)
				{
					htmltext = "33464-07.html";
					break;
				}
				else if (npc.getId() == ADVENTURERS_GUIDE)
				{
					htmltext = "32981-04.html";
					break;
				}
			}
			case State.STARTED:
			{
				if (npc.getId() == SHANNON)
				{
					if (qs.isCond(1))
					{
						htmltext = "32974-04.html";
						break;
					}
				}
				else if (npc.getId() == ADVENTURERS_GUIDE)
				{
					if (qs.isCond(4))
					{
						htmltext = "32981-01.html";
						break;
					}
					else if (qs.isCond(5))
					{
						htmltext = "32981-03.html";
						break;
					}
				}
				else if (npc.getId() == EVAIN)
				{
					switch (qs.getCond())
					{
						case 1:
						{
							qs.setCond(2, true);
							htmltext = "33464-01.html";
							break;
						}
						case 2:
						{
							htmltext = "33464-02.html";
							break;
						}
						case 3:
						{
							qs.setCond(4, true);
							htmltext = "33464-03.html";
							break;
						}
						case 4:
						{
							htmltext = "33464-04.html";
							break;
						}
						case 5:
						{
							htmltext = "33464-05.html";
							break;
						}
						case 6:
						{
							showOnScreenMsg(player, NpcStringId.WEAPONS_HAVE_BEEN_ADDED_TO_YOUR_INVENTORY, ExShowScreenMessage.TOP_CENTER, 4500);
							giveItems(player, WOODEN_ARROW, 500);
							giveItems(player, ADENA, 70);
							giveItems(player, HEALING_POTION, 50);
							giveItems(player, APPRENTICE_ADVENTURERS_STAFF, 1);
							giveItems(player, APPRENTICE_ADVENTURERS_BONE_CLUB, 1);
							giveItems(player, APPRENTICE_ADVENTURERS_KNIFE, 1);
							giveItems(player, APPRENTICE_ADVENTURERS_CESTUS, 1);
							giveItems(player, APPRENTICE_ADVENTURERS_BOW, 1);
							giveItems(player, APPRENTICE_ADVENTURERS_LONG_SWORD, 1);
							addExpAndSp(player, 300, 5);
							npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.THERE_S_THE_NEXT_TRAINING_STEP);
							qs.exitQuest(false, true);
							htmltext = "33464-06.html";
							break;
						}
					}
				}
				break;
			}
			case State.COMPLETED:
			{
				if (npc.getId() == SHANNON)
				{
					htmltext = "32974-05.html";
					break;
				}
				else if (npc.getId() == EVAIN)
				{
					htmltext = "33464-08.html";
					break;
				}
				// Official is using same html for created/completed
				else if (npc.getId() == ADVENTURERS_GUIDE)
				{
					htmltext = "32981-04.html";
					break;
				}
			}
		}
		return htmltext;
	}
}