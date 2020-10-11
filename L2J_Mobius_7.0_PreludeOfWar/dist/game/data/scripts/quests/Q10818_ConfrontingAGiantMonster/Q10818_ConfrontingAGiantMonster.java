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
package quests.Q10818_ConfrontingAGiantMonster;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.CommonUtil;
import org.l2jmobius.gameserver.enums.QuestSound;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.quest.State;

import quests.Q10817_ExaltedOneWhoOvercomesTheLimit.Q10817_ExaltedOneWhoOvercomesTheLimit;

/**
 * Confronting a Giant Monster (10818)
 * @URL https://l2wiki.com/Confronting_a_Giant_Monster
 * @author Mobius, CostyKiller
 */
public class Q10818_ConfrontingAGiantMonster extends Quest
{
	// NPCs
	private static final int DAICHIR = 30537;
	private static final int JAEDIN = 33915;
	// Monsters
	private static final int ISTINA = 29196; // Extreme
	private static final int OCTAVIS = 29212; // Extreme
	private static final int TAUTI = 29237; // Extreme
	private static final int EKIMUS = 29251; // correct id?
	private static final int TRASKEN = 29197; // correct id?
	private static final int VERIDAN = 25796;
	private static final int KECHI = 25797;
	private static final int MICHAELA = 25799;
	private static final int[] MONSTERS =
	{
		// Giant's Cave Monsters
		23727, // Shaqrima Bathus
		23728, // Shaqrima Carcass -->
		23729, // Shaqrima Kshana -->
		23733, // Lesser Giant Warrior -->
		23734, // Lesser Giant Wizard -->
		23735, // Captive Familiar Spirit -->
		23736, // Captive Hell Demon -->
		23737, // Captive Succubus -->
		23738, // Captive Phantom -->
		23742, // Naia Bathus, Demons Foreman -->
		23743, // Naia Karkus, Demons Foreman -->
		23744, // Naia Kshana, Demons Foreman -->
		23746, // Recovering Lesser Giant Warrior -->
		23747, // Recovering Lesser Giant Wizard -->
		23749, // Root of the Lesser Giant -->
		23754, // Essence of the Lesser Giant -->
		
		// Fairy Settlement Monsters
		18972, // Treekin Defender Scout
		18973, // Treekin Defender Experienced Scout
		18974, // Centaur Archer Scout
		18975, // Centaur Archer Experienced Scout
		18976, // Beorning Berserker Scout
		18977, // Beorning Berserker Experienced Scout
		22863, // Fairy Warrior
		22864, // Fairy Warrior Wicked
		22865, // Fairy Warrior Mature
		22866, // Fairy Warrior Imperfect
		22867, // Fairy Warrior Violent
		22868, // Fairy Warrior Brutal
		22869, // Fairy Warrior Fully Enraged
		22870, // Fairy Warrior Slightly Enraged
		22871, // Fairy Rogue
		22875, // Fairy Rogue Wicked
		22873, // Fairy Rogue Mature
		22874, // Fairy Rogue Imperfect
		22875, // Fairy Rogue Violent
		22876, // Fairy Rogue Brutal
		22877, // Fairy Rogue Fully Enraged
		22878, // Fairy Rogue Slightly Enraged
		22879, // Fairy Knight
		22880, // Fairy Knight Wicked
		22881, // Fairy Knight Mature
		22882, // Fairy Knight Imperfect
		22883, // Fairy Knight Violent
		22884, // Fairy Knight Brutal
		22885, // Fairy Knight Fully Enraged
		22886, // Fairy Knight Slightly Enraged
		22887, // Satyr Wizard
		22888, // Satyr Wizard Wicked
		22889, // Satyr Wizard Mature
		22890, // Satyr Wizard Imperfect
		22891, // Satyr Wizard Violent
		22892, // Satyr Wizard Brutal
		22893, // Satyr Wizard Fully Enraged
		22894, // Satyr Wizard Slightly Enraged
		22895, // Satyr Summoner
		22896, // Satyr Summoner Wicked
		22897, // Satyr Summoner Mature
		22898, // Satyr Summoner Imperfect
		22899, // Satyr Summoner Violent
		22900, // Satyr Summoner Brutal
		22901, // Satyr Summoner Fully Enraged
		22902, // Satyr Summoner Slightly Enraged
		22903, // Satyr Witch
		22904, // Satyr Witch Wicked
		22905, // Satyr Witch Mature
		22906, // Satyr Witch Imperfect
		22907, // Satyr Witch Violent
		22908, // Satyr Witch Brutal
		22909, // Satyr Witch Fully Enraged
		22910, // Satyr Witch Slightly Enraged
		23041 // Pan Direm
	};
	
	// Items
	private static final int DARK_SOUL_STONE = 46055;
	private static final int OLYMPIAD_MANAGER_CERTIFICATE = 45629;
	private static final int ISHUMA_CERTIFICATE = 45630;
	private static final int SIR_KRISTOF_RODEMAI_CERTIFICATE = 45631;
	private static final int PROOF_OF_RESISTANCE = 80823;
	private static final int VERIDAN_SOUL_STONE = 46052;
	private static final int KECHI_SOUL_STONE = 46053;
	private static final int MICHAELA_SOUL_STONE = 46054;
	// Rewards
	private static final long EXP_AMOUNT = 193815839115L;
	private static final int DAICHIR_CERTIFICATE = 45628;
	// Misc
	private static final int MIN_LEVEL = 99;
	private static final int PROOF_OF_RESISTANCE_NEEDED = 10000;
	
	public Q10818_ConfrontingAGiantMonster()
	{
		super(10818);
		addStartNpc(DAICHIR);
		addTalkId(DAICHIR, JAEDIN);
		addKillId(ISTINA, OCTAVIS, TAUTI, EKIMUS, TRASKEN);
		addKillId(MONSTERS);
		addCondMinLevel(MIN_LEVEL, "30537-02.html");
		addCondStartedQuest(Q10817_ExaltedOneWhoOvercomesTheLimit.class.getSimpleName(), "30537-03.html");
		registerQuestItems(DARK_SOUL_STONE, VERIDAN_SOUL_STONE, KECHI_SOUL_STONE, MICHAELA_SOUL_STONE);
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
			case "30537-04.htm":
			case "30537-05.htm":
			{
				htmltext = event;
				break;
			}
			case "30537-06.htm":
			{
				qs.startQuest();
				qs.set(Integer.toString(ISTINA), "false");
				qs.set(Integer.toString(OCTAVIS), "false");
				qs.set(Integer.toString(TAUTI), "false");
				qs.set(Integer.toString(EKIMUS), "false");
				htmltext = event;
				break;
			}
			case "30537-06b.html":
			{
				qs.setCond(3);
				qs.unset(Integer.toString(ISTINA));
				qs.unset(Integer.toString(OCTAVIS));
				qs.unset(Integer.toString(TAUTI));
				qs.unset(Integer.toString(EKIMUS));
				htmltext = event;
				break;
			}
			case "30537-09.html":
			{
				if ((player.getLevel() >= MIN_LEVEL))
				{
					if ((qs.isCond(4) && (getQuestItemsCount(player, PROOF_OF_RESISTANCE) >= PROOF_OF_RESISTANCE_NEEDED)) || (qs.isCond(2) && hasQuestItems(player, DARK_SOUL_STONE) && (qs.get(Integer.toString(ISTINA)).equals("true") && qs.get(Integer.toString(OCTAVIS)).equals("true") && qs.get(Integer.toString(TAUTI)).equals("true") && qs.get(Integer.toString(EKIMUS)).equals("true"))))
					{
						if (hasQuestItems(player, OLYMPIAD_MANAGER_CERTIFICATE, ISHUMA_CERTIFICATE, SIR_KRISTOF_RODEMAI_CERTIFICATE))
						{
							htmltext = "30537-10.html";
						}
						else
						{
							htmltext = event;
						}
						if (qs.isCond(2))
						{
							takeItems(player, DARK_SOUL_STONE, 1);
							qs.unset(Integer.toString(ISTINA));
							qs.unset(Integer.toString(OCTAVIS));
							qs.unset(Integer.toString(TAUTI));
							qs.unset(Integer.toString(EKIMUS));
						}
						giveItems(player, DAICHIR_CERTIFICATE, 1);
						addExpAndSp(player, EXP_AMOUNT, 0);
						qs.exitQuest(false, true);
					}
				}
				else
				{
					htmltext = getNoQuestLevelRewardMsg(player);
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
				htmltext = "30537-01.htm";
				break;
			}
			case State.STARTED:
			{
				switch (npc.getId())
				{
					case DAICHIR:
					{
						if (qs.isCond(2))
						{
							if (hasQuestItems(player, DARK_SOUL_STONE) && (qs.get(Integer.toString(ISTINA)).equals("true") && qs.get(Integer.toString(OCTAVIS)).equals("true") && qs.get(Integer.toString(TAUTI)).equals("true") && qs.get(Integer.toString(EKIMUS)).equals("true")))
							{
								htmltext = "30537-08.html";
							}
							else
							{
								htmltext = "30537-07.html";
							}
						}
						else if (qs.isCond(4))
						{
							if (getQuestItemsCount(player, PROOF_OF_RESISTANCE) >= PROOF_OF_RESISTANCE_NEEDED)
							{
								htmltext = "30537-08.html";
							}
							else
							{
								htmltext = "30537-07a.html";
							}
						}
						break;
					}
					// XXX: Set Ekimus quest check until instance is done
					case JAEDIN:
					{
						if (qs.get(Integer.toString(EKIMUS)).equals("false"))
						{
							htmltext = "33915-01.html";
							qs.set(Integer.toString(EKIMUS), "true");
							playSound(player, QuestSound.ITEMSOUND_QUEST_MIDDLE);
						}
						else
						{
							htmltext = "33915-02.html";
						}
					}
						break;
				}
				return htmltext;
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
	public String onKill(Npc npc, PlayerInstance player, boolean isSummon)
	{
		executeForEachPlayer(player, npc, isSummon, true, false);
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public void actionForEachPlayer(PlayerInstance player, Npc npc, boolean isSummon)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && qs.isStarted() && player.isInsideRadius3D(npc, Config.ALT_PARTY_RANGE))
		{
			if (npc.getId() == TRASKEN)
			{
				giveItems(player, DARK_SOUL_STONE, 1);
				playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			else if (npc.getId() == VERIDAN)
			{
				giveItems(player, VERIDAN_SOUL_STONE, 1);
				playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			else if (npc.getId() == KECHI)
			{
				giveItems(player, KECHI_SOUL_STONE, 1);
				playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			else if (npc.getId() == MICHAELA)
			{
				giveItems(player, MICHAELA_SOUL_STONE, 1);
				playSound(player, QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			else if (CommonUtil.contains(MONSTERS, npc.getId()))
			{
				giveItems(player, PROOF_OF_RESISTANCE, 1);
				if (getQuestItemsCount(player, PROOF_OF_RESISTANCE) >= PROOF_OF_RESISTANCE_NEEDED)
				{
					qs.setCond(4, true);
				}
			}
			else if ((npc.getId() == ISTINA) || (npc.getId() == OCTAVIS) || (npc.getId() == TAUTI) || (npc.getId() == EKIMUS))
			{
				qs.set(Integer.toString(npc.getId()), "true");
				notifyKill(npc, player, isSummon);
				playSound(player, QuestSound.ITEMSOUND_QUEST_MIDDLE);
				if ((qs.get(Integer.toString(ISTINA)).equals("true") && qs.get(Integer.toString(OCTAVIS)).equals("true") && qs.get(Integer.toString(TAUTI)).equals("true") && qs.get(Integer.toString(EKIMUS)).equals("true")))
				{
					qs.setCond(2);
				}
			}
		}
	}
}