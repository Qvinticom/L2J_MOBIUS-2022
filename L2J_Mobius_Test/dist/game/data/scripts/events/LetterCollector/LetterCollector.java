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
package events.LetterCollector;

import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.event.LongTimeEvent;

/**
 * Event: Letter Collector
 * @URL http://www.lineage2.com/en/news/events/letter-collector-event-05112016.php
 * @author Gigi
 */
public final class LetterCollector extends LongTimeEvent
{
	// NPC
	private static final int ANGEL_CAT = 33873;
	// Items
	private static final int A = 3875;
	private static final int C = 3876;
	private static final int E = 3877;
	private static final int F = 3878;
	private static final int G = 3879;
	private static final int H = 3880;
	private static final int I = 3881;
	private static final int L = 3882;
	private static final int N = 3883;
	private static final int O = 3884;
	private static final int R = 3885;
	private static final int S = 3886;
	private static final int T = 3887;
	private static final int II = 3888;
	// Rewards Together
	private static final int DARK_AMARANTHINE_ENHANCEMENT_STONE = 3875;
	private static final int TAUTI_DUAL_AXE = 35001;
	private static final int TAUTI_ONE_HEADED_AXE = 34998;
	private static final int KELBIM_BOW = 46062;
	private static final int KELBIM_DUAL_DAGER = 46061;
	private static final int KELBIM_DAGER = 46060;
	private static final int KELBIM_CROSSBOW = 46063;
	private static final int KELBIM_ATELIA_FRAGMENT = 46079;
	private static final int BLESSED_SPECTER_THROWER = 18041;
	private static final int BLESSED_SPECTER_STORMER = 18040;
	private static final int BLESSED_SPECTER_RETRIBUTER = 18045;
	private static final int BLESSED_SPECTER_BUSTER = 18043;
	private static final int BLESSED_SPECTER_DUALSWORD = 18046;
	private static final int BLESSED_SPECTER_FIGHTER = 18039;
	private static final int BLESSED_SPECTER_CASTER = 18044;
	private static final int BLESSED_SPECTER_DUAL_DAGGER = 18047;
	private static final int BLESSED_SPECTER_SLASHER = 18037;
	private static final int BLESSED_SPECTER_CUTTER = 18036;
	private static final int BLESSED_SPECTER_SHAPER = 18035;
	private static final int BLESSED_SPECTER_AVANGER = 18038;
	private static final int BLESSED_SPECTER_DUAL_BLUNT = 18048;
	private static final int BLESSED_SPECTER_SHOOTER = 18042;
	private static final int DARK_ETERNAL_ENHACEMENT_STONE = 35567;
	private static final int BLESSED_SERAPH_BREASTPLATE = 18050;
	private static final int BLESSED_SERAPH_LEATHER_ARMOR = 18056;
	private static final int BLESSED_SERAPH_TUNIC = 18061;
	private static final int BLESSED_SERAPH_LEATHER_LEGGINGS = 18057;
	private static final int HIGH_GRADE_WIND_DEY_PACK = 39562;
	private static final int BLESSED_SERAPH_STOCKINGS = 18062;
	private static final int LV_5_LEGENDERY_DEY_PACK = 34954;
	private static final int BLESSED_SERAPH_SHOES = 18064;
	private static final int BLESSED_SERAPH_HELMET = 18049;
	private static final int BLESSED_SERAPH_GAITERS = 18051;
	private static final int BLESSED_SERAPH_LEATHER_HELMET = 18055;
	private static final int BLESSED_SERAPH_CIRCLET = 18060;
	private static final int BLESSED_SERAPH_GUANTLETS = 18052;
	private static final int BLESSED_SERAPH_LEATHER_GLOVES = 18058;
	private static final int LV_5_ANCIENT_DEY_PACK = 34955;
	private static final int BLESSED_SERAPH_LEATHER_BOOTS = 18059;
	private static final int BLESSED_SERAPH_BOOTS = 18053;
	private static final int BLESSED_SERAPH_GLOVES = 18063;
	private static final int LV_5_GIANT_DEY_PACK = 34953;
	private static final int BLESSED_SERAPH_SIGIL = 18065;
	private static final int BLESSED_SERAPH_SHIELD = 18054;
	private static final int DARK_ETERNAL_ENHACEMENT_STONE_FRAGMENT = 37802;
	private static final int TOP_GRADE_SPIRIT_STONE = 45932;
	private static final int MID_GRADE_WIND_DYE_PACK = 39561;
	private static final int LV_4_LEGENDERY_DEY_PACK = 34951;
	private static final int FORTUNE_POKET_STAGE_5 = 39633;
	private static final int LEONAS_SCROLL_10000000_SP = 38103;
	private static final int LOW_GRADE_WIND_DYE_PACK = 39560;
	private static final int LV_4_ANCIENT_DEY_PACK = 34952;
	private static final int LV_3_LEGENDERY_DEY_PACK = 34946;
	private static final int HIGH_GRADE_SPIRIT_STONE = 45931;
	private static final int LEONAS_SCROLL_5000000_SP = 38102;
	private static final int LV_4_GIANT_DEY_PACK = 34950;
	private static final int MID_GRADE_SPIRIT_STONE = 45930;
	private static final int SPIRIT_STONE_HAIR_ACCESSORY = 45937;
	private static final int LV_3_ANCIENT_DEY_PACK = 34947;
	private static final int LV_3_GIANT_DEY_PACK = 34945;
	private static final int LEONAS_SCROLL_1000000_SP = 38101;
	private static final int GAMESTONE_R_GRADE = 19440;
	private static final int BLUEBERRY_CAKE = 37009;
	private static final int SPIRIT_STONE = 45929;
	private static final int BLESSED_SPIRITSHOT_R_GEADE = 19442;
	private static final int SOULSHOT_R_GEADE = 17754;
	private static final int CRYSTAL_R_GEADE = 17371;
	
	private LetterCollector()
	{
		super(LetterCollector.class.getSimpleName(), "events");
		addStartNpc(ANGEL_CAT);
		addFirstTalkId(ANGEL_CAT);
		addTalkId(ANGEL_CAT);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		switch (event)
		{
			case "33873-1.htm":
			case "33873-2.htm":
			{
				htmltext = event;
				break;
			}
			case "lineage":
			{
				if ((getQuestItemsCount(player, L) >= 1) && (getQuestItemsCount(player, I) >= 1) && (getQuestItemsCount(player, N) >= 1) && (getQuestItemsCount(player, E) >= 2) && (getQuestItemsCount(player, A) >= 1) && (getQuestItemsCount(player, G) >= 1) && (getQuestItemsCount(player, II) >= 1))
				{
					takeItems(player, L, 1);
					takeItems(player, I, 1);
					takeItems(player, N, 1);
					takeItems(player, E, 2);
					takeItems(player, A, 1);
					takeItems(player, G, 1);
					takeItems(player, II, 1);
					htmltext = "33873-1.htm";
				}
				else
				{
					htmltext = "noItem.htm";
				}
				break;
			}
			case "together":
			{
				if ((getQuestItemsCount(player, T) >= 2) && (getQuestItemsCount(player, O) >= 1) && (getQuestItemsCount(player, G) >= 1) && (getQuestItemsCount(player, E) >= 2) && (getQuestItemsCount(player, H) >= 1) && (getQuestItemsCount(player, R) >= 1))
				{
					takeItems(player, T, 2);
					takeItems(player, O, 1);
					takeItems(player, G, 1);
					takeItems(player, E, 2);
					takeItems(player, H, 1);
					takeItems(player, R, 1);
					giveItemRandomly(player, null, DARK_AMARANTHINE_ENHANCEMENT_STONE, 1, 1, 0.07, false);
					giveItemRandomly(player, null, TAUTI_DUAL_AXE, 1, 1, 0.01, false);
					giveItemRandomly(player, null, TAUTI_ONE_HEADED_AXE, 1, 1, 0.01, false);
					giveItemRandomly(player, null, KELBIM_BOW, 1, 1, 0.01, false);
					giveItemRandomly(player, null, KELBIM_DUAL_DAGER, 1, 1, 0.01, false);
					giveItemRandomly(player, null, KELBIM_DAGER, 1, 1, 0.01, false);
					giveItemRandomly(player, null, KELBIM_CROSSBOW, 1, 1, 0.01, false);
					giveItemRandomly(player, null, KELBIM_ATELIA_FRAGMENT, 1, 1, 0.05, false);
					giveItemRandomly(player, null, BLESSED_SPECTER_THROWER, 1, 1, 0.02, false);
					giveItemRandomly(player, null, BLESSED_SPECTER_STORMER, 1, 1, 0.02, false);
					giveItemRandomly(player, null, BLESSED_SPECTER_RETRIBUTER, 1, 1, 0.02, false);
					giveItemRandomly(player, null, BLESSED_SPECTER_BUSTER, 1, 1, 0.02, false);
					giveItemRandomly(player, null, BLESSED_SPECTER_DUALSWORD, 1, 1, 0.015, false);
					giveItemRandomly(player, null, BLESSED_SPECTER_FIGHTER, 1, 1, 0.02, false);
					giveItemRandomly(player, null, BLESSED_SPECTER_CASTER, 1, 1, 0.02, false);
					giveItemRandomly(player, null, BLESSED_SPECTER_DUAL_DAGGER, 1, 1, 0.015, false);
					giveItemRandomly(player, null, BLESSED_SPECTER_SLASHER, 1, 1, 0.02, false);
					giveItemRandomly(player, null, BLESSED_SPECTER_CUTTER, 1, 1, 0.02, false);
					giveItemRandomly(player, null, BLESSED_SPECTER_SHAPER, 1, 1, 0.02, false);
					giveItemRandomly(player, null, BLESSED_SPECTER_AVANGER, 1, 1, 0.02, false);
					giveItemRandomly(player, null, BLESSED_SPECTER_DUAL_BLUNT, 1, 1, 0.015, false);
					giveItemRandomly(player, null, BLESSED_SPECTER_SHOOTER, 1, 1, 0.02, false);
					giveItemRandomly(player, null, DARK_ETERNAL_ENHACEMENT_STONE, 1, 1, 0.25, false);
					giveItemRandomly(player, null, BLESSED_SERAPH_BREASTPLATE, 1, 1, 0.025, false);
					giveItemRandomly(player, null, BLESSED_SERAPH_LEATHER_ARMOR, 1, 1, 0.025, false);
					giveItemRandomly(player, null, BLESSED_SERAPH_TUNIC, 1, 1, 0.025, false);
					giveItemRandomly(player, null, BLESSED_SERAPH_LEATHER_LEGGINGS, 1, 1, 0.025, false);
					giveItemRandomly(player, null, HIGH_GRADE_WIND_DEY_PACK, 1, 1, 0.3, false);
					giveItemRandomly(player, null, BLESSED_SERAPH_STOCKINGS, 1, 1, 0.025, false);
					giveItemRandomly(player, null, LV_5_LEGENDERY_DEY_PACK, 1, 1, 0.1, false);
					giveItemRandomly(player, null, BLESSED_SERAPH_SHOES, 1, 1, 0.025, false);
					giveItemRandomly(player, null, BLESSED_SERAPH_HELMET, 1, 1, 0.025, false);
					giveItemRandomly(player, null, BLESSED_SERAPH_GAITERS, 1, 1, 0.025, false);
					giveItemRandomly(player, null, BLESSED_SERAPH_LEATHER_HELMET, 1, 1, 0.025, false);
					giveItemRandomly(player, null, BLESSED_SERAPH_CIRCLET, 1, 1, 0.025, false);
					giveItemRandomly(player, null, BLESSED_SERAPH_GUANTLETS, 1, 1, 0.025, false);
					giveItemRandomly(player, null, BLESSED_SERAPH_LEATHER_GLOVES, 1, 1, 0.025, false);
					giveItemRandomly(player, null, LV_5_ANCIENT_DEY_PACK, 1, 1, 0.15, false);
					giveItemRandomly(player, null, BLESSED_SERAPH_LEATHER_BOOTS, 1, 1, 0.025, false);
					giveItemRandomly(player, null, BLESSED_SERAPH_BOOTS, 1, 1, 0.025, false);
					giveItemRandomly(player, null, BLESSED_SERAPH_GLOVES, 1, 1, 0.025, false);
					giveItemRandomly(player, null, LV_5_GIANT_DEY_PACK, 1, 1, 0.15, false);
					giveItemRandomly(player, null, BLESSED_SERAPH_SIGIL, 1, 1, 0.025, false);
					giveItemRandomly(player, null, BLESSED_SERAPH_SHIELD, 1, 1, 0.025, false);
					giveItemRandomly(player, null, DARK_ETERNAL_ENHACEMENT_STONE_FRAGMENT, 1, 1, 0.55, false);
					giveItemRandomly(player, null, TOP_GRADE_SPIRIT_STONE, 1, 1, 0.06, false);
					giveItemRandomly(player, null, MID_GRADE_WIND_DYE_PACK, 1, 1, 0.3, false);
					giveItemRandomly(player, null, LV_4_LEGENDERY_DEY_PACK, 1, 1, 0.2, false);
					giveItemRandomly(player, null, FORTUNE_POKET_STAGE_5, 1, 1, 0.32, false);
					giveItemRandomly(player, null, LEONAS_SCROLL_10000000_SP, 1, 1, 0.25, false);
					giveItemRandomly(player, null, LOW_GRADE_WIND_DYE_PACK, 1, 1, 0.38, false);
					giveItemRandomly(player, null, LV_4_ANCIENT_DEY_PACK, 1, 1, 0.24, false);
					giveItemRandomly(player, null, LV_3_LEGENDERY_DEY_PACK, 1, 1, 0.25, false);
					giveItemRandomly(player, null, HIGH_GRADE_SPIRIT_STONE, 1, 1, 0.08, false);
					giveItemRandomly(player, null, LV_4_GIANT_DEY_PACK, 1, 1, 0.2, false);
					giveItemRandomly(player, null, MID_GRADE_SPIRIT_STONE, 1, 1, 0.1, false);
					giveItemRandomly(player, null, SPIRIT_STONE_HAIR_ACCESSORY, 1, 1, 0.1, false);
					giveItemRandomly(player, null, LV_3_ANCIENT_DEY_PACK, 1, 1, 0.26, false);
					giveItemRandomly(player, null, LEONAS_SCROLL_5000000_SP, 1, 1, 0.3, false);
					giveItemRandomly(player, null, LV_3_GIANT_DEY_PACK, 1, 1, 0.25, false);
					giveItemRandomly(player, null, LEONAS_SCROLL_1000000_SP, 1, 1, 0.35, false);
					giveItemRandomly(player, null, GAMESTONE_R_GRADE, 1, 1, 0.8, false);
					giveItemRandomly(player, null, BLUEBERRY_CAKE, 3, 3, 0.3, false);
					giveItemRandomly(player, null, SPIRIT_STONE, 1, 1, 0.15, false);
					giveItemRandomly(player, null, BLESSED_SPIRITSHOT_R_GEADE, 500, 500, 0.7, false);
					giveItemRandomly(player, null, SOULSHOT_R_GEADE, 2000, 2000, 0.8, false);
					giveItemRandomly(player, null, CRYSTAL_R_GEADE, 20, 20, 0.14, false);
					htmltext = "33873-1.htm";
				}
				else
				{
					htmltext = "noItem.htm";
				}
				break;
			}
			case "ncsoft":
			{
				if ((getQuestItemsCount(player, N) >= 1) && (getQuestItemsCount(player, C) >= 1) && (getQuestItemsCount(player, S) >= 1) && (getQuestItemsCount(player, O) >= 1) && (getQuestItemsCount(player, F) >= 1) && (getQuestItemsCount(player, T) >= 1))
				{
					takeItems(player, N, 1);
					takeItems(player, C, 1);
					takeItems(player, S, 1);
					takeItems(player, O, 1);
					takeItems(player, F, 1);
					takeItems(player, T, 1);
					htmltext = "33873-1.htm";
				}
				else
				{
					htmltext = "noItem.htm";
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return npc.getId() + "-1.htm";
	}
	
	public static void main(String[] args)
	{
		new LetterCollector();
	}
}