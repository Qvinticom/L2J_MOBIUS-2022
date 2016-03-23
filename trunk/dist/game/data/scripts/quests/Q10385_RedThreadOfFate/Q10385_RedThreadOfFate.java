/*
 * This file is part of the L2J Mobius project.
 * 
 * This file is part of the L2J Mobius Project.
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
package quests.Q10385_RedThreadOfFate;

import com.l2jmobius.gameserver.enums.ChatType;
import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.SkillHolder;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.quest.State;
import com.l2jmobius.gameserver.model.skills.Skill;
import com.l2jmobius.gameserver.network.NpcStringId;
import com.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jmobius.gameserver.network.serverpackets.NpcSay;
import com.l2jmobius.gameserver.util.Util;

import quests.Q10338_SeizeYourDestiny.Q10338_SeizeYourDestiny;

/**
 * Red Thread of Fate (10385)
 * @author Stayway
 */
public class Q10385_RedThreadOfFate extends Quest
{
	// NPCs
	private static final int RAINA = 33491;
	private static final int MORELYN = 30925;
	private static final int LANYA = 33783;
	private static final int WATER_SOURCE = 33784;
	private static final int LADY_OF_THE_LAKE = 31745;
	private static final int NERUPA = 30370;
	private static final int ENFEUX = 31519;
	private static final int INNOCENTIN = 31328;
	private static final int VULCAN = 31539;
	private static final int URN = 31149;
	private static final int WESLEY = 30166;
	private static final int HOUSE = 33788;
	private static final int PAAGRIO_TEMPLE = 33787;
	private static final int SHILEN = 33785;
	private static final int SOULS = 33789;
	private static final int MOTHER_TREE = 33786;
	
	// Items
	private static final int MYSTERIOUS_LETTER = 36072;
	private static final int WATER_GARDEN_OF_EVA = 36066;
	private static final int CLEAREST_WATER = 36067;
	private static final int BRIGHTEST_LIGHT = 36068;
	private static final int PUREST_SOUL = 36069;
	private static final int VULCAN_TRUE_GOLD = 36113;
	private static final int VULCAN_PURE_SILVER = 36114;
	private static final int VULCAN_BLOOD_FIRE = 36115;
	private static final int FIERCEST_FLAME = 36070;
	private static final int FONDEST_HEART = 36071;
	private static final int SCROLL_OF_ESCAPE_VOA = 39514;
	private static final int SCROLL_OF_ESCAPE_FOG = 39515;
	private static final int SCROLL_OF_ESCAPE_IT = 39516;
	private static final int SCROLL_OF_ESCAPE_DV = 40309;
	private static final int DIMENSIONAL_DIAMOND = 7562;
	// Monsters
	private static final int SHILEN_MESSENGER = 23151;
	// Locations
	private static final Location LANYA_LOC = new Location(79690, 251027, -10424, 0);
	private static final Location WATER_LOC = new Location(143218, 43916, -3024, 0);
	private static final Location WATERFALL_LOC = new Location(172458, 90314, -1984, 0);
	private static final Location VULCAN_LOC = new Location(180060, -111608, -5838, 0);
	// Social Action ID
	private static final int BOW = 7;
	// Instance Talking Island
	public static final int INSTANCE_ID = 241;
	// Skills
	private static final SkillHolder NPC_HOUSE = new SkillHolder(9583, 1);
	private static final SkillHolder NPC_PAAGRIO = new SkillHolder(9582, 1);
	private static final SkillHolder NPC_SHILEN = new SkillHolder(9580, 1);
	private static final SkillHolder NPC_SOULS = new SkillHolder(9581, 1);
	private static final SkillHolder NPC_TREE = new SkillHolder(9579, 1);
	
	public Q10385_RedThreadOfFate()
	{
		super(10385, Q10385_RedThreadOfFate.class.getSimpleName(), "Red Thread of Fate");
		addStartNpc(RAINA);
		addTalkId(RAINA, MORELYN, LANYA, WATER_SOURCE, LADY_OF_THE_LAKE, NERUPA, ENFEUX, INNOCENTIN, VULCAN, URN, WESLEY, HOUSE, PAAGRIO_TEMPLE, SHILEN, SOULS, MOTHER_TREE);
		addFirstTalkId(LANYA, HOUSE, PAAGRIO_TEMPLE, SHILEN, SOULS, MOTHER_TREE);
		addSocialActionSeeId(LANYA);
		addSkillSeeId(HOUSE, PAAGRIO_TEMPLE, SHILEN, SOULS, MOTHER_TREE);
		registerQuestItems(MYSTERIOUS_LETTER, WATER_GARDEN_OF_EVA, CLEAREST_WATER, PUREST_SOUL, VULCAN_TRUE_GOLD, VULCAN_PURE_SILVER, VULCAN_BLOOD_FIRE, FIERCEST_FLAME, FONDEST_HEART, SCROLL_OF_ESCAPE_VOA, SCROLL_OF_ESCAPE_FOG, SCROLL_OF_ESCAPE_IT, SCROLL_OF_ESCAPE_DV);
		addKillId(SHILEN_MESSENGER);
		addSpawnId(SHILEN_MESSENGER);
		addCondNotRace(Race.ERTHEIA, "noRace.html");
		addCondCompletedQuest(Q10338_SeizeYourDestiny.class.getSimpleName(), "restriction.html");
		addCondMinLevel(85, "no_level.html");
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
			case "33491-02.htm":
			case "30370-02.html":
			case "31745-02.html":
			case "31539-02.html":
			case "30166-02.html":
			case "31539-03.html":
			case "30370-03.html":
			case "31745-04.html":
			case "31745-05.html":
			case "31745-06.html":
			case "31539-06.html":
			case "31539-07.html":
			case "33748-02.html":
			{
				htmltext = event;
				break;
			}
			case "33491-03.htm":
			{
				qs.startQuest();
				giveItems(player, MYSTERIOUS_LETTER, 1);
				htmltext = event;
				break;
			}
			case "33491-06.html":
			{
				giveItems(player, DIMENSIONAL_DIAMOND, 40);
				qs.exitQuest(false, true);
				htmltext = event;
				break;
			}
			case "30925-02.html":
			{
				if (qs.isCond(1))
				{
					qs.setCond(2);
				}
				htmltext = event;
				break;
			}
			case "33783-02.html":
			{
				if (qs.isCond(2))
				{
					qs.setCond(3);
				}
				htmltext = event;
				break;
			}
			case "TP1":
			{
				if (qs.isCond(4))
				{
					qs.setCond(5);
					player.teleToLocation(LANYA_LOC, 0);
				}
				break;
			}
			case "TP2":
			{
				if (qs.isCond(5))
				{
					qs.setCond(6);
					player.teleToLocation(WATER_LOC, 0);
					giveItems(player, WATER_GARDEN_OF_EVA, 1);
				}
				break;
			}
			case "31745-03.html":
			{
				if (qs.isCond(6))
				{
					takeItems(player, WATER_GARDEN_OF_EVA, 1);
				}
				htmltext = event;
				break;
			}
			case "TP3":
			{
				if (qs.isCond(6))
				{
					qs.setCond(7);
					player.teleToLocation(WATERFALL_LOC, 0);
					giveItems(player, CLEAREST_WATER, 1);
					htmltext = "31745-05.html";
				}
				break;
			}
			case "30370-04.html":
			{
				if (qs.isCond(7))
				{
					qs.setCond(8);
					giveItems(player, SCROLL_OF_ESCAPE_VOA, 1);
					giveItems(player, BRIGHTEST_LIGHT, 1);
					showOnScreenMsg(player, NpcStringId.TRY_USING_THE_TELEPORT_SCROLL_NERUPA_GAVE_YOUT_O_GO_TO_THE_VALLEY_OF_SAINTS, ExShowScreenMessage.TOP_CENTER, 4500);
				}
				break;
			}
			case "31519-02.html":
			{
				if (qs.isCond(8))
				{
					qs.setCond(9);
					giveItems(player, PUREST_SOUL, 1);
				}
				break;
			}
			case "31328-02.html":
			{
				if (qs.isCond(9))
				{
					qs.setCond(10);
					giveItems(player, SCROLL_OF_ESCAPE_FOG, 1);
					showOnScreenMsg(player, NpcStringId.TRY_USING_THE_TELEPORT_SCROLL_INNOCENTIN_GAVE_YOU_TO_GO_TO_THE_FORGE_OF_THE_GODS, ExShowScreenMessage.TOP_CENTER, 4500);
				}
				break;
			}
			case "31539-04.html":
			{
				if (qs.isCond(10))
				{
					qs.setCond(11);
					giveItems(player, SCROLL_OF_ESCAPE_IT, 1);
					giveItems(player, VULCAN_TRUE_GOLD, 1);
					giveItems(player, VULCAN_PURE_SILVER, 1);
					giveItems(player, VULCAN_BLOOD_FIRE, 1);
					showOnScreenMsg(player, NpcStringId.TRY_USING_THE_TELEPORT_SCROLL_VULCAN_GAVE_YOU_TO_GO_TO_IVORY_TOWER, ExShowScreenMessage.TOP_CENTER, 4500);
				}
				break;
			}
			case "31149-02.html":
			{
				if (qs.isCond(11) && hasQuestItems(player, VULCAN_TRUE_GOLD, VULCAN_PURE_SILVER, VULCAN_BLOOD_FIRE))
				{
					takeItems(player, VULCAN_TRUE_GOLD, -1);
					takeItems(player, VULCAN_PURE_SILVER, -1);
					takeItems(player, VULCAN_BLOOD_FIRE, -1);
					qs.setCond(12);
				}
				htmltext = "31149-02.html";
				break;
			}
			case "tp_vulcan":
			{
				if (qs.isCond(12))
				{
					qs.setCond(13);
					player.teleToLocation(VULCAN_LOC, 0);
					htmltext = "30166-03.html";
				}
				break;
			}
			case "33749-02.html":
			{
				if (qs.isCond(19))
				{
					qs.setCond(20);
				}
				break;
			}
			case "31539-08.html":
			{
				if (qs.isCond(13))
				{
					qs.setCond(14);
					giveItems(player, SCROLL_OF_ESCAPE_DV, 1);
					giveItems(player, FIERCEST_FLAME, 1);
					giveItems(player, FONDEST_HEART, 1);
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance player, Skill skill, L2Object[] targets, boolean isSummon)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		switch (qs.getCond())
		{
			case 14:
			{
				castSkill(npc, player, NPC_HOUSE.getSkill());
				qs.setCond(15);
				break;
			}
			case 15:
			{
				castSkill(npc, player, NPC_PAAGRIO.getSkill());
				qs.setCond(16);
				break;
			}
			case 16:
			{
				castSkill(npc, player, NPC_SHILEN.getSkill());
				addAttackDesire(addSpawn(SHILEN_MESSENGER, npc, true, 0, false), player);
				showOnScreenMsg(player, NpcStringId.YOU_MUST_DEFEAT_SHILEN_S_MESSENGER, ExShowScreenMessage.TOP_CENTER, 4500);
				startQuestTimer("DESPAWN", 10000, npc, player);
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), ChatType.NPC_GENERAL, npc.getId(), NpcStringId.BRIGHTEST_LIGHT_HOW_DARE_YOU_DESECRATE_THE_ALTAR_OF_SHILEN));
				qs.setCond(17);
				break;
			}
			case 17:
			{
				castSkill(npc, player, NPC_SOULS.getSkill());
				qs.setCond(18);
				break;
			}
			case 18:
			{
				castSkill(npc, player, NPC_TREE.getSkill());
				qs.setCond(19);
				break;
			}
		}
		return null;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs == null) || !qs.isStarted() || !Util.checkIfInRange(1500, npc, killer, true))
		{
			return super.onKill(npc, killer, isSummon);
		}
		switch (npc.getId())
		{
			case SHILEN_MESSENGER:
			{
				qs.setCond(17);
				break;
			}
		}
		return super.onKill(npc, killer, isSummon);
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
		
		caster.calculateDistance(caster.getTarget().getLocation(), true, false);
		final QuestState qs = getQuestState(caster, false);
		if (actionId == BOW)
		{
			switch (qs.getCond())
			{
				case 3:
				{
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), ChatType.NPC_GENERAL, npc.getTemplate().getDisplayId(), NpcStringId.SO_BORED_IS_THERE_NO_ONE_I_CAN_PLAY_WITH));
					qs.setCond(4, true);
					break;
				}
			}
		}
		return null;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = null;
		
		switch (npc.getId())
		{
			
			case LANYA:
			{
				if (qs.isCond(2))
				{
					htmltext = "33783-01.html";
				}
				else if (qs.isCond(3))
				{
					{
						htmltext = "33783-02.html";
					}
				}
				else if (qs.isCond(4))
				{
					{
						htmltext = "33783-03.html";
					}
				}
				break;
			}
			case HOUSE:
			{
				if (qs.isCond(14))
				{
					htmltext = "33788-01.html";
				}
				showOnScreenMsg(player, NpcStringId.USE_THE_FONDEST_HEART_IN_YOUR_INVENTORY, ExShowScreenMessage.TOP_CENTER, 4500);
				break;
			}
			case PAAGRIO_TEMPLE:
			{
				if (qs.isCond(15))
				{
					htmltext = "33787-01.html";
				}
				showOnScreenMsg(player, NpcStringId.USE_THE_FIERCEST_FLAME_IN_YOUR_INVENTORY, ExShowScreenMessage.TOP_CENTER, 4500);
				break;
			}
			case SHILEN:
			{
				if (qs.isCond(16))
				{
					htmltext = "33785-01.html";
				}
				showOnScreenMsg(player, NpcStringId.USE_THE_BRIGHTEST_LIGHT_IN_YOUR_INVENTORY, ExShowScreenMessage.TOP_CENTER, 4500);
				break;
			}
			case SOULS:
			{
				if (qs.isCond(17))
				{
					htmltext = "33789-01.html";
				}
				showOnScreenMsg(player, NpcStringId.USE_THE_PUREST_SOUL_IN_YOUR_INVENTORY, ExShowScreenMessage.TOP_CENTER, 4500);
				break;
			}
			case MOTHER_TREE:
			{
				if (qs.isCond(18))
				{
					htmltext = "33786-01.html";
				}
				showOnScreenMsg(player, NpcStringId.USE_THE_CLEAREST_WATER_IN_YOUR_INVENTORY, ExShowScreenMessage.TOP_CENTER, 4500);
				break;
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
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case RAINA:
					{
						if (qs.isCond(1))
						{
							htmltext = "33491-04.html";
						}
						else if (qs.isCond(22))
						{
							{
								htmltext = "33491-05.html";
							}
						}
						break;
					}
					case MORELYN:
					{
						if (qs.isCond(1))
						{
							htmltext = "30925-01.html";
						}
						else if (qs.isCond(2))
						{
							{
								htmltext = "30925-03.html";
							}
						}
						break;
					}
					case LANYA:
					{
						if (qs.isCond(2))
						{
							qs.setCond(3);
							htmltext = "33783-02.html";
						}
						break;
					}
					case WATER_SOURCE:
					{
						if (qs.isCond(5))
						{
							htmltext = "33784-01.html";
						}
						break;
					}
					case LADY_OF_THE_LAKE:
					{
						if (qs.isCond(6))
						{
							htmltext = "31745-01.html";
						}
						break;
					}
					case NERUPA:
					{
						if (qs.isCond(7))
						{
							htmltext = "30370-01.html";
						}
						break;
					}
					case ENFEUX:
					{
						if (qs.isCond(8))
						{
							htmltext = "31519-01.html";
						}
						break;
					}
					case INNOCENTIN:
					{
						if (qs.isCond(9))
						{
							htmltext = "31328-01.html";
						}
						else if (qs.isCond(10))
						{
							htmltext = "31328-03.html";
						}
						break;
					}
					case VULCAN:
					{
						if (qs.isCond(10))
						{
							htmltext = "31539-01.html";
						}
						else if (qs.isCond(13))
						{
							htmltext = "31539-05.html";
						}
						break;
					}
					case URN:
					{
						if (qs.isCond(11))
						{
							htmltext = "31149-01.html";
						}
						break;
					}
					case WESLEY:
					{
						if (qs.isCond(12))
						{
							htmltext = "30166-01.html";
						}
						break;
					}
				}
				break;
			}
			case State.CREATED:
			{
				if (npc.getId() == RAINA)
				{
					htmltext = "33491-01.htm";
					break;
				}
			}
			case State.COMPLETED:
			{
				htmltext = getAlreadyCompletedMsg(player);
				break;
			}
		}
		return htmltext;
	}
}
