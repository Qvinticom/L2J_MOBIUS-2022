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
package quests.Q11026_PathOfDestinyConviction;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.xml.impl.CategoryData;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.enums.Movie;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerLogin;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.classchange.ExRequestClassChangeUi;

import quests.Q11025_PathOfDestinyProving.Q11025_PathOfDestinyProving;

/**
 * Path of Destiny - Conviction (11026)
 * @URL https://l2wiki.com/Path_of_Destiny_-_Conviction
 * @author Dmitri, Mobius
 */
public class Q11026_PathOfDestinyConviction extends Quest
{
	// NPCs
	private static final int TARTI = 34505;
	private static final int RAYMOND = 30289;
	private static final int KAIN_VAN_HALTER = 31639;
	private static final int MYSTERIOUS_MAGE = 31522;
	private static final int VAMPIRE_SOLDIER = 21582;
	private static final int VON_HELLMANN = 19566;
	// Items
	private static final int WIND_SPIRIT = 80673;
	// Location
	private static final Location TELEPORT_1 = new Location(57983, -28955, 568);
	private static final Location TELEPORT_2 = new Location(-14180, 123840, -3120);
	// Misc
	private static final int MIN_LEVEL = 76;
	
	public Q11026_PathOfDestinyConviction()
	{
		super(11026);
		addStartNpc(TARTI);
		addTalkId(TARTI, RAYMOND, KAIN_VAN_HALTER, MYSTERIOUS_MAGE);
		addFirstTalkId(KAIN_VAN_HALTER, MYSTERIOUS_MAGE);
		addKillId(VAMPIRE_SOLDIER, VON_HELLMANN);
		registerQuestItems(WIND_SPIRIT);
		addCondCompletedQuest(Q11025_PathOfDestinyProving.class.getSimpleName(), "33963-06.html");
		setQuestNameNpcStringId(NpcStringId.LV_40_PATH_OF_DESTINY_CONVICTION);
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
			case "34505-08.html":
			case "34505-09.html":
			case "34505-11.html":
			case "30289-03.html":
			case "31639-02.html":
			case "31639-03.html":
			case "31639-04.html":
			case "31639-05.html":
			case "31639-06.html":
			case "31639-07.html":
			case "31639-08.html":
			case "31639-09.html":
			case "31639-11.html":
			{
				htmltext = event;
				break;
			}
			case "34505-02.html":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "34505-03.html":
			{
				htmltext = event;
				if (player.getLevel() >= MIN_LEVEL)
				{
					htmltext = "34505-04.htm";
				}
				break;
			}
			case "34505-05.html":
			{
				if (qs.isCond(1))
				{
					qs.setCond(2, true);
				}
				htmltext = event;
				break;
			}
			case "30289-02.html":
			{
				if (qs.isCond(2))
				{
					qs.setCond(3, true);
				}
				htmltext = event;
				break;
			}
			case "teleport":
			{
				if (qs.isCond(3))
				{
					final Npc mob = addSpawn(VAMPIRE_SOLDIER, 57983, -28955, 568, 0, true, 180000);
					addAttackPlayerDesire(mob, player);
					player.teleToLocation(TELEPORT_1);
				}
				break;
			}
			case "31639-10.html":
			{
				if (qs.isCond(3))
				{
					addSpawn(MYSTERIOUS_MAGE, npc.getX() + 40, npc.getY() + 40, npc.getZ(), npc.getHeading(), false, 120000);
					showOnScreenMsg(player, NpcStringId.TALK_TO_THE_MYSTERIOUS_WIZARD, ExShowScreenMessage.TOP_CENTER, 10000);
					break;
				}
			}
			case "falver":
			{
				if (qs.isCond(3))
				{
					qs.setCond(4, true);
					playMovie(player, Movie.SI_CHOICE_OF_KAIN_A);
					giveItems(player, WIND_SPIRIT, 1);
					player.teleToLocation(TELEPORT_2);
					htmltext = event;
				}
				break;
			}
			case "34505-10.html":
			{
				if (qs.isCond(4))
				{
					addExpAndSp(player, 14281098, 12852);
					qs.exitQuest(false, true);
					if (CategoryData.getInstance().isInCategory(CategoryType.THIRD_CLASS_GROUP, player.getClassId().getId()) || //
						(CategoryData.getInstance().isInCategory(CategoryType.SECOND_CLASS_GROUP, player.getClassId().getId()) && (player.getRace() == Race.ERTHEIA)))
					{
						player.sendPacket(ExRequestClassChangeUi.STATIC_PACKET);
					}
					giveStoryBuffReward(npc, player);
					htmltext = event;
				}
				break;
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
						if (qs.isCond(1))
						{
							if (player.getLevel() >= MIN_LEVEL)
							{
								qs.setCond(2, true);
								htmltext = "34505-05.html";
							}
							else
							{
								htmltext = "34505-06.html";
							}
							break;
						}
						else if (qs.isCond(2))
						{
							htmltext = "34505-05.html"; // TODO: Proper second talk dialog.
							break;
						}
						else if (qs.isCond(4))
						{
							htmltext = "34505-07.html";
						}
						break;
					}
					case RAYMOND:
					{
						if (qs.isCond(2))
						{
							htmltext = "30289-01.html";
						}
						break;
					}
					case KAIN_VAN_HALTER:
					{
						if (qs.isCond(3))
						{
							htmltext = "31639-01.html";
						}
						break;
					}
					case MYSTERIOUS_MAGE:
					{
						if (qs.isCond(3))
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
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && qs.isCond(3))
		{
			switch (npc.getId())
			{
				case VAMPIRE_SOLDIER:
				{
					final Npc mob = addSpawn(VON_HELLMANN, npc, false, 120000);
					addAttackPlayerDesire(mob, killer);
					break;
				}
				case VON_HELLMANN:
				{
					playMovie(killer, Movie.SI_CHOICE_OF_KAIN_B);
					addSpawn(KAIN_VAN_HALTER, 57983, -28955, 568, 0, false, 120000);
					break;
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
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
			player.sendPacket(ExRequestClassChangeUi.STATIC_PACKET);
		}
	}
}
