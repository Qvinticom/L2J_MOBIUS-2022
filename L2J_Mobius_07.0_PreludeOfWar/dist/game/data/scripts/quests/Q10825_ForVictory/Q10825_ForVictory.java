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
package quests.Q10825_ForVictory;

import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.ceremonyofchaos.OnCeremonyOfChaosMatchResult;
import org.l2jmobius.gameserver.model.events.impl.olympiad.OnOlympiadMatchResult;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q10823_ExaltedOneWhoShattersTheLimit.Q10823_ExaltedOneWhoShattersTheLimit;

/**
 * For Victory (10825)
 * @URL https://l2wiki.com/For_Victory
 * @author Mobius
 */
public class Q10825_ForVictory extends Quest
{
	// NPC
	private static final int KURTIZ = 30870;
	// Flag of Protection NPCs
	private static final int FLAG_GLUDIO = 36741; // 1 Gludio Castle
	private static final int FLAG_DION = 36742; // 2 Dion Castle
	private static final int FLAG_GIRAN = 36743; // 3 Giran Castle
	private static final int FLAG_OREN = 36744; // 4 Oren Castle
	private static final int FLAG_ADEN = 36745; // 5 Aden Castle
	private static final int FLAG_INNADRIL = 36746; // 6 Innadril Castle
	private static final int FLAG_GODDARD = 36747; // 7 Goddard Castle
	private static final int FLAG_RUNE = 36748; // 8 Rune Castle
	private static final int FLAG_SCHUTTGART = 36749; // 9 Schuttgart Castle
	// Items
	private static final int PROOF_OF_SETTLEMENT = 80825;
	private static final int MARK_OF_VALOR = 46059;
	private static final int MERLOT_CERTIFICATE = 46056;
	private static final int MAMMON_CERTIFICATE = 45635;
	private static final int GUSTAV_CERTIFICATE = 45636;
	// Rewards
	private static final int KURTIZ_CERTIFICATE = 46057;
	private static final int SPELLBOOK_SUMMON_BATTLE_POTION = 45927;
	// Misc
	private static final int MIN_LEVEL = 100;
	private static final int MARK_OF_VALOR_NEEDED = 10;
	private static final int PROOF_OF_SETTLEMENT_NEEDED = 30;
	
	public Q10825_ForVictory()
	{
		super(10825);
		addStartNpc(KURTIZ);
		addTalkId(KURTIZ, FLAG_GLUDIO, FLAG_DION, FLAG_GIRAN, FLAG_OREN, FLAG_ADEN, FLAG_INNADRIL, FLAG_GODDARD, FLAG_RUNE, FLAG_SCHUTTGART);
		addCondMinLevel(MIN_LEVEL, "30870-02.html");
		addCondStartedQuest(Q10823_ExaltedOneWhoShattersTheLimit.class.getSimpleName(), "30870-03.html");
		registerQuestItems(PROOF_OF_SETTLEMENT, MARK_OF_VALOR);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "30870-04.htm":
			case "30870-05.htm":
			{
				htmltext = event;
				break;
			}
			case "30870-06.html":
			{
				qs.startQuest();
				qs.set("TimeCheck", 0);
				htmltext = event;
				break;
			}
			case "30870-06a.html":
			{
				qs.setCond(3);
				htmltext = event;
				break;
			}
			case "30870-09.html":
			{
				if ((qs.isCond(2) && (getQuestItemsCount(player, MARK_OF_VALOR) >= MARK_OF_VALOR_NEEDED)) || (qs.isCond(4) && (getQuestItemsCount(player, PROOF_OF_SETTLEMENT) >= PROOF_OF_SETTLEMENT_NEEDED)))
				{
					if ((player.getLevel() >= MIN_LEVEL))
					{
						if (hasQuestItems(player, MERLOT_CERTIFICATE, MAMMON_CERTIFICATE, GUSTAV_CERTIFICATE))
						{
							htmltext = "30870-10.html";
						}
						if (qs.isCond(2))
						{
							takeItems(player, MARK_OF_VALOR, MARK_OF_VALOR_NEEDED);
						}
						else if (qs.isCond(4))
						{
							takeItems(player, PROOF_OF_SETTLEMENT, PROOF_OF_SETTLEMENT_NEEDED);
						}
						htmltext = event;
						giveItems(player, KURTIZ_CERTIFICATE, 1);
						giveItems(player, SPELLBOOK_SUMMON_BATTLE_POTION, 1);
						qs.exitQuest(false, true);
					}
					else
					{
						htmltext = getNoQuestLevelRewardMsg(player);
					}
				}
				break;
			}
			case "mark":
			{
				final long TIME_CHECK = Long.parseLong(qs.get("TimeCheck"));
				final long CURRENT_TIME = Chronos.currentTimeMillis();
				final long SIEGE_LENGTH = 7200000; // 2 hours in milliseconds
				switch (npc.getId())
				{
					case FLAG_GLUDIO:
					case FLAG_DION:
					case FLAG_GIRAN:
					case FLAG_OREN:
					case FLAG_INNADRIL:
					case FLAG_GODDARD:
					case FLAG_SCHUTTGART:
					{
						if (player != null)
						{
							if (qs.isCond(1) && (getQuestItemsCount(player, MARK_OF_VALOR) < MARK_OF_VALOR_NEEDED) && (CURRENT_TIME > (TIME_CHECK + SIEGE_LENGTH)))
							{
								if (player.getSiegeState() == 0)
								{
									htmltext = "CastleFlagOfProtection-03.html"; // Reward only siege participants
								}
								else
								{
									qs.set("TimeCheck", Long.toString(CURRENT_TIME));
									giveItems(player, MARK_OF_VALOR, 1);
									if (getQuestItemsCount(player, MARK_OF_VALOR) >= MARK_OF_VALOR_NEEDED)
									{
										qs.setCond(2, true);
									}
									playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
									htmltext = "CastleFlagOfProtection-02.html"; // Reward received
								}
							}
							else
							{
								htmltext = "CastleFlagOfProtection-04.html"; // Reward only from one castle per day
							}
						}
						break;
					}
					case FLAG_ADEN:
					case FLAG_RUNE:
					{
						if (player != null)
						{
							if (qs.isCond(1) && (getQuestItemsCount(player, MARK_OF_VALOR) < MARK_OF_VALOR_NEEDED) && (CURRENT_TIME > (TIME_CHECK + SIEGE_LENGTH)))
							{
								if (player.getSiegeState() == 0)
								{
									htmltext = "CastleFlagOfProtection-03.html"; // Reward only siege participants
								}
								else
								{
									qs.set("TimeCheck", String.valueOf(CURRENT_TIME));
									giveItems(player, MARK_OF_VALOR, 2);
									if (getQuestItemsCount(player, MARK_OF_VALOR) >= MARK_OF_VALOR_NEEDED)
									{
										qs.setCond(2, true);
									}
									playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
									htmltext = "CastleFlagOfProtection-02.html"; // Reward received
								}
							}
							else
							{
								htmltext = "CastleFlagOfProtection-04.html"; // Reward only from one castle per day
							}
						}
						break;
					}
					
				}
			}
				break;
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
				htmltext = "30870-01.htm";
				break;
			}
			case State.STARTED:
			{
				switch (qs.getCond())
				{
					case 1:
					{
						htmltext = "30870-07.html";
						break;
					}
					case 2:
					{
						if (getQuestItemsCount(player, MARK_OF_VALOR) >= MARK_OF_VALOR_NEEDED)
						{
							htmltext = "30870-08.html";
						}
						break;
					}
					case 3:
					{
						htmltext = "30870-07a.html";
						break;
					}
					case 4:
					{
						if (getQuestItemsCount(player, PROOF_OF_SETTLEMENT) >= PROOF_OF_SETTLEMENT_NEEDED)
						{
							htmltext = "30870-08a.html";
							break;
						}
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
	
	private void manageQuestProgress(Player player)
	{
		if (player != null)
		{
			final QuestState qs = getQuestState(player, false);
			if ((qs != null) && qs.isCond(3))
			{
				giveItems(player, PROOF_OF_SETTLEMENT, 1);
				playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
				if (getQuestItemsCount(player, PROOF_OF_SETTLEMENT) >= PROOF_OF_SETTLEMENT_NEEDED)
				{
					qs.setCond(4, true);
				}
			}
		}
	}
	
	@RegisterEvent(EventType.ON_CEREMONY_OF_CHAOS_MATCH_RESULT)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	private void onCeremonyOfChaosMatchResult(OnCeremonyOfChaosMatchResult event)
	{
		event.getMembers().forEach(player -> manageQuestProgress(player));
	}
	
	@RegisterEvent(EventType.ON_OLYMPIAD_MATCH_RESULT)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	private void onOlympiadMatchResult(OnOlympiadMatchResult event)
	{
		manageQuestProgress(event.getWinner().getPlayer());
		manageQuestProgress(event.getLoser().getPlayer());
	}
}