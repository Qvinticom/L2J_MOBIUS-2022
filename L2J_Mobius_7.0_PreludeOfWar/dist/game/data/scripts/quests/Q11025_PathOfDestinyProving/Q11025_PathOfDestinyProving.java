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
package quests.Q11025_PathOfDestinyProving;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.xml.impl.CategoryData;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.base.ClassId;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLogin;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerProfessionChange;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.classchange.ExRequestClassChangeUi;

/**
 * Path of Destiny - Proving (11025)
 * @URL https://l2wiki.com/Path_of_Destiny_-_Proving
 * @author Dmitri
 */
public class Q11025_PathOfDestinyProving extends Quest
{
	// NPCs
	private static final int TARTI = 34505;
	private static final int RAYMOND = 30289;
	private static final int TERESIA = 33981;
	private static final int MYSTERIOUS_MAGE = 1033980; // TODO: Find proper ID.
	private static final int SKELETON_ARCHER = 27529;
	private static final int SKELETON_WARRIOR = 27528;
	// Items
	private static final int WIND_SPIRIT = 80673;
	// Class change rewards
	private static final int SS_R = 33780;
	private static final int BSS_R = 33794;
	private static final int BOX_R_HEAVY = 46924;
	private static final int BOX_R_LIGHT = 46925;
	private static final int BOX_R_ROBE = 46926;
	private static final int WEAPON_SWORD_R = 47008;
	private static final int WEAPON_SHIELD_R = 47026;
	private static final int WEAPON_GSWORD_R = 47009;
	private static final int WEAPON_BLUNT_R = 47010;
	private static final int WEAPON_FIST_R = 47011;
	private static final int WEAPON_SPEAR_R = 47012;
	private static final int WEAPON_BOW_R = 47013;
	private static final int WEAPON_DUALDAGGER_R = 47019;
	private static final int WEAPON_STAFF_R = 47017;
	private static final int WEAPON_DUALSWORD_R = 47018;
	private static final int WEAPON_CROSSBOW_R = 47014;
	private static final int ORICHALCUM_BOLT_R = 19443;
	// Location
	private static final Location TRAINING_GROUNDS_TELEPORT = new Location(-4983, 116607, -3344);
	// Misc
	private static final int MIN_LEVEL = 40;
	
	public Q11025_PathOfDestinyProving()
	{
		super(11025);
		addStartNpc(TARTI);
		addFirstTalkId(TERESIA, MYSTERIOUS_MAGE);
		addTalkId(TARTI, RAYMOND, TERESIA, MYSTERIOUS_MAGE);
		addKillId(SKELETON_ARCHER, SKELETON_WARRIOR);
		registerQuestItems(WIND_SPIRIT);
		addCondMinLevel(MIN_LEVEL, "34505-11.html");
		setQuestNameNpcStringId(NpcStringId.LV_20_PATH_OF_DESTINY_PROVING);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		String htmltext = null;
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "30289-02.html":
			case "30289-05.html":
			case "34505-05.html":
			case "34505-06.html":
			case "34505-07.html":
			case "34505-08.html":
			case "34505-09.html":
			case "34505-12.html":
			case "1033980-02.html":
			{
				htmltext = event;
				break;
			}
			case "34505-02.html":
			{
				qs.startQuest();
				htmltext = event;
				if (player.getLevel() >= 40)
				{
					qs.setCond(2, true);
					htmltext = "34505-03.html";
				}
				break;
			}
			case "30289-03.html":
			{
				qs.setCond(3, true);
				giveItems(player, WIND_SPIRIT, 1);
				htmltext = event;
				break;
			}
			case "teleport":
			{
				if (qs.isCond(3))
				{
					player.teleToLocation(TRAINING_GROUNDS_TELEPORT);
				}
				break;
			}
			case "mega_menu":
			{
				if (qs.isCond(4))
				{
					addSpawn(MYSTERIOUS_MAGE, npc, true, 300000);
					showOnScreenMsg(player, NpcStringId.TALK_TO_THE_MYSTERIOUS_WIZARD_2, ExShowScreenMessage.TOP_CENTER, 10000);
					break;
				}
			}
			case "falver":
			{
				qs.setCond(5, true);
				showOnScreenMsg(player, NpcStringId.RETURN_TO_RAYMOND_OF_THE_TOWN_OF_GLUDIO, ExShowScreenMessage.TOP_CENTER, 10000);
				giveItems(player, WIND_SPIRIT, 1);
				htmltext = event;
				break;
			}
			case "30289-06.html":
			{
				qs.setCond(6, true);
				htmltext = event;
				break;
			}
			case "34505-10.html":
			{
				if (qs.isCond(6))
				{
					giveAdena(player, 5000, true);
					qs.exitQuest(false, true);
					if (CategoryData.getInstance().isInCategory(CategoryType.SECOND_CLASS_GROUP, player.getClassId().getId()) || //
						(CategoryData.getInstance().isInCategory(CategoryType.FIRST_CLASS_GROUP, player.getClassId().getId()) && (player.getRace() == Race.ERTHEIA)))
					{
						showOnScreenMsg(player, NpcStringId.CLASS_TRANSFER_IS_AVAILABLE_NCLICK_THE_CLASS_TRANSFER_ICON_IN_THE_NOTIFICATION_WINDOW_TO_TRANSFER_YOUR_CLASS, ExShowScreenMessage.TOP_CENTER, 10000);
						player.sendPacket(ExRequestClassChangeUi.STATIC_PACKET);
					}
					giveStoryBuffReward(npc, player);
					htmltext = event;
					break;
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		
		switch (qs.getState())
		{
			case State.CREATED:
			{
				if (npc.getId() == TARTI)
				{
					htmltext = "34505-01.html";
				}
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case TARTI:
					{
						if (qs.isCond(1) && (player.getLevel() > MIN_LEVEL))
						{
							qs.setCond(2, true);
							htmltext = "34505-03.html";
							break;
						}
						else if (qs.isCond(2))
						{
							htmltext = "34505-03.html"; // TODO: Proper second talk dialog.
							break;
						}
						else if (qs.isCond(6))
						{
							htmltext = "34505-04.html";
						}
						break;
					}
					case RAYMOND:
					{
						if (qs.isCond(2))
						{
							htmltext = "30289-01.html";
						}
						else if (qs.isCond(5))
						{
							htmltext = "30289-04.html";
						}
						else if (qs.isCond(6))
						{
							htmltext = "30289-06.html"; // TODO: Proper second talk dialog.
						}
						break;
					}
					case TERESIA:
					{
						if (qs.isCond(4))
						{
							htmltext = "33981-01.html";
						}
						break;
					}
					case MYSTERIOUS_MAGE:
					{
						if (qs.isCond(4))
						{
							htmltext = "33980-01.html";
						}
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
	public String onFirstTalk(Npc npc, PlayerInstance player)
	{
		return npc.getId() + "-01.html";
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, true);
		if ((qs != null) && qs.isCond(3))
		{
			addSpawn(TERESIA, npc, true, 300000);
			showOnScreenMsg(killer, NpcStringId.CHECK_ON_TELESHA, ExShowScreenMessage.TOP_CENTER, 10000);
			qs.setCond(4, true);
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@RegisterEvent(EventType.ON_PLAYER_PROFESSION_CHANGE)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onProfessionChange(OnPlayerProfessionChange event)
	{
		final PlayerInstance player = event.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (player.getRace() == Race.ERTHEIA)
		{
			if (!CategoryData.getInstance().isInCategory(CategoryType.SECOND_CLASS_GROUP, player.getClassId().getId()))
			{
				return;
			}
		}
		else if (!CategoryData.getInstance().isInCategory(CategoryType.THIRD_CLASS_GROUP, player.getClassId().getId()))
		{
			return;
		}
		
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCompleted())
		{
			giveItems(player, SS_R, 3000);
			giveItems(player, BSS_R, 2000);
			
			final int classId = player.getClassId().getId();
			switch (player.getRace())
			{
				case HUMAN:
				case ELF:
				case DARK_ELF:
				{
					if (CategoryData.getInstance().isInCategory(CategoryType.DIVISION_WIZARD, classId))
					{
						giveItems(player, BOX_R_ROBE, 1);
						giveItems(player, WEAPON_STAFF_R, 1);
					}
					else if (CategoryData.getInstance().isInCategory(CategoryType.SUBJOB_GROUP_BOW, classId))
					{
						giveItems(player, BOX_R_LIGHT, 1);
						giveItems(player, WEAPON_BOW_R, 1);
					}
					else if (CategoryData.getInstance().isInCategory(CategoryType.SUBJOB_GROUP_DAGGER, classId))
					{
						giveItems(player, BOX_R_LIGHT, 1);
						giveItems(player, WEAPON_DUALDAGGER_R, 1);
					}
					else if (CategoryData.getInstance().isInCategory(CategoryType.SUBJOB_GROUP_DANCE, classId) || (player.getClassId() == ClassId.GLADIATOR))
					{
						giveItems(player, BOX_R_HEAVY, 1);
						giveItems(player, WEAPON_DUALSWORD_R, 1);
					}
					else if (player.getClassId() == ClassId.WARLORD)
					{
						giveItems(player, BOX_R_HEAVY, 1);
						giveItems(player, WEAPON_SPEAR_R, 1);
					}
					else if (player.getClassId() == ClassId.DUELIST)
					{
						giveItems(player, BOX_R_HEAVY, 1);
						giveItems(player, WEAPON_DUALSWORD_R, 1);
					}
					else if (CategoryData.getInstance().isInCategory(CategoryType.TANKER_GROUP, classId))
					{
						giveItems(player, BOX_R_HEAVY, 1);
						giveItems(player, WEAPON_SWORD_R, 1);
						giveItems(player, WEAPON_SHIELD_R, 1);
					}
					else if (CategoryData.getInstance().isInCategory(CategoryType.RECOM_WARRIOR_GROUP, classId))
					{
						giveItems(player, BOX_R_HEAVY, 1);
						giveItems(player, WEAPON_SWORD_R, 1);
					}
					else
					{
						giveItems(player, BOX_R_HEAVY, 1);
						giveItems(player, WEAPON_SWORD_R, 1);
						giveItems(player, WEAPON_GSWORD_R, 1);
					}
					break;
				}
				case DWARF:
				{
					if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_OTHEL_GROUP, classId))
					{
						giveItems(player, BOX_R_LIGHT, 1);
						giveItems(player, WEAPON_DUALDAGGER_R, 1);
					}
					else if (CategoryData.getInstance().isInCategory(CategoryType.DWARF_BOUNTY_CLASS, classId))
					{
						giveItems(player, BOX_R_LIGHT, 1);
						giveItems(player, WEAPON_DUALDAGGER_R, 1);
					}
					else
					{
						giveItems(player, BOX_R_HEAVY, 1);
						giveItems(player, WEAPON_BLUNT_R, 1);
					}
					break;
				}
				case ORC:
				{
					if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_IS_GROUP, classId))
					{
						giveItems(player, BOX_R_LIGHT, 1);
						giveItems(player, WEAPON_DUALSWORD_R, 1);
					}
					else if (player.getClassId() == ClassId.TYRR_GRAND_KHAVATARI)
					{
						giveItems(player, BOX_R_LIGHT, 1);
						giveItems(player, WEAPON_FIST_R, 1);
					}
					else if (player.getClassId() == ClassId.TYRR_TITAN)
					{
						giveItems(player, BOX_R_HEAVY, 1);
						giveItems(player, WEAPON_GSWORD_R, 1);
					}
					else if (player.isMageClass())
					{
						giveItems(player, BOX_R_LIGHT, 1);
						giveItems(player, WEAPON_STAFF_R, 1);
					}
					else if (CategoryData.getInstance().isInCategory(CategoryType.LIGHT_ARMOR_CLASS, classId))
					{
						giveItems(player, BOX_R_LIGHT, 1);
						giveItems(player, WEAPON_FIST_R, 1);
					}
					else
					{
						giveItems(player, BOX_R_HEAVY, 1);
						giveItems(player, WEAPON_GSWORD_R, 1);
					}
					break;
				}
				case KAMAEL:
				{
					if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_FEOH_GROUP, classId))
					{
						giveItems(player, BOX_R_ROBE, 1);
						giveItems(player, WEAPON_STAFF_R, 1);
					}
					else if (CategoryData.getInstance().isInCategory(CategoryType.SIXTH_YR_GROUP, classId))
					{
						giveItems(player, BOX_R_LIGHT, 1);
						giveItems(player, ORICHALCUM_BOLT_R, 5000);
						giveItems(player, WEAPON_CROSSBOW_R, 1);
					}
					else if (CategoryData.getInstance().isInCategory(CategoryType.DIVISION_WIZARD, classId))
					{
						giveItems(player, BOX_R_ROBE, 1);
						giveItems(player, WEAPON_STAFF_R, 1);
					}
					else if (CategoryData.getInstance().isInCategory(CategoryType.DIVISION_ARCHER, classId))
					{
						giveItems(player, BOX_R_LIGHT, 1);
						giveItems(player, ORICHALCUM_BOLT_R, 5000);
						giveItems(player, WEAPON_CROSSBOW_R, 1);
					}
					else
					{
						giveItems(player, BOX_R_LIGHT, 1);
						giveItems(player, WEAPON_GSWORD_R, 1);
						break;
					}
				}
				case ERTHEIA:
				{
					if (player.isMageClass())
					{
						giveItems(player, BOX_R_ROBE, 1);
						giveItems(player, WEAPON_STAFF_R, 1);
					}
					else
					{
						giveItems(player, BOX_R_LIGHT, 1);
						giveItems(player, WEAPON_FIST_R, 1);
					}
					break;
				}
			}
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerLogin(OnPlayerLogin event)
	{
		if (Config.DISABLE_TUTORIAL)
		{
			return;
		}
		
		final PlayerInstance player = event.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (player.getRace() == Race.ERTHEIA)
		{
			if (!CategoryData.getInstance().isInCategory(CategoryType.FIRST_CLASS_GROUP, player.getClassId().getId()))
			{
				return;
			}
		}
		else if (!CategoryData.getInstance().isInCategory(CategoryType.SECOND_CLASS_GROUP, player.getClassId().getId()))
		{
			return;
		}
		
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isCompleted())
		{
			player.sendPacket(ExRequestClassChangeUi.STATIC_PACKET);
		}
	}
}
