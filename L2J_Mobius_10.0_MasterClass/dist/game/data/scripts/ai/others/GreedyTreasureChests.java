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
package ai.others;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.commons.util.CommonUtil;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.instancemanager.GlobalVariablesManager;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.holders.ItemHolder;

import ai.AbstractNpcAI;

/**
 * Greedy Treasure Chests AI.
 * @URL https://dev.l2central.info/main/articles/355.html
 * @author CostyKiller, NasSeKa
 */
public final class GreedyTreasureChests extends AbstractNpcAI
{
	// NPCs
	private static final int CHEST_LV110 = 8710;
	private static final int CHEST_LV120 = 8711;
	//@formatter:off
	private static final int[] TRIGGER_MOBS_LV110_CHEST_SV = // Silent Valley
	{
		24506, 24507, 24508, 24509, 24510,
	};
	private static final int[] TRIGGER_MOBS_LV110_CHEST_IT = // Ivory Tower Crater
	{
		24421, 24422, 24423, 24424, 24425, 24426,
	};
	private static final int[] TRIGGER_MOBS_LV110_CHEST_TC = // Tanor Canyon
	{
		20936,20937, 20938, 20939, 20940, 20941, 20942, 20943, 24587,
	};
	private static final int[] TRIGGER_MOBS_LV110_CHEST_AI = // Alligator Island
	{
		24373, 24376, 24377,	
	};
	private static final int[] TRIGGER_MOBS_LV110_CHEST_FS = // Field of Silence
	{
		24517, 24520, 24521, 24522, 24523,
	};
	private static final int[] TRIGGER_MOBS_LV110_CHEST_FM = // Forest of Mirrors
	{
		24461, 24462, 24463, 24464, 24465, 24466,
	};
	private static final int[] TRIGGER_MOBS_LV120_CHEST_VS = // Varka Silenos Barracks
	{
		24636, 24637, 24638, 24639, 24640, 				  
	};
	private static final int[] TRIGGER_MOBS_LV120_CHEST_KO = // Ketra OrcOutpost
	{
		24631, 24632, 24633, 24634, 24635,
	};
	private static final int[] TRIGGER_MOBS_LV120_CHEST_FW = // Field of Whispers
	{
		24304, 24305, 24306, 24307, 24308,
	};
	private static final int[] TRIGGER_MOBS_LV120_CHEST_IP = // Isle of Prayer
	{
		24445, 24446, 24447, 24448, 24449, 24450, 24451,
	};
	private static final int[] TRIGGER_MOBS_LV120_CHEST_BS = // Breka's Stronghold
	{
		24415, 24416, 24417, 24418, 24419, 24420,
	};
	private static final int[] TRIGGER_MOBS_LV120_CHEST_SM = // Sel Mahum Training Grounds
	{
		24492, 24493, 24494, 24495,
	};
	private static final int[] TRIGGER_MOBS_LV120_CHEST_PL = // Plains of Lizardman
	{
		24496, 24497, 24498, 24499,
	};
	private static final int[] TRIGGER_MOBS_LV120_CHEST_FOM = // Fields of Massacre
	{
		24486, 24487, 24488, 24489, 24490, 24491,
	};
	private static final int[] TRIGGER_MOBS_LV120_CHEST_SS = // Sea Of Spores
	{
		24621, 24622, 24623, 24624,
	};
	private static final int[] TRIGGER_MOBS_LV120_CHEST_DV = // Dragon Valley
	{
		24481, 24482,
	};
	private static final int[] TRIGGER_MOBS_LV120_CHEST_FT = // Fafurion Temple
	{
		24318, 24322, 24323, 24325, 24329,
	};
	private static final int[] TRIGGER_MOBS_LV120_CHEST_WS = // Wastelands
	{
		24500, 24501, 24502, 24503, 24504, 24505,
	};
	//@formatter:on
	// Items
	private static final List<ItemHolder> DROPLIST_LV110_CHEST = new ArrayList<>();
	static
	{
		DROPLIST_LV110_CHEST.add(new ItemHolder(27538, 1)); // +7 Bloody Helios Pack
		DROPLIST_LV110_CHEST.add(new ItemHolder(48204, 1)); // Radiant Warrior's Circlet
		DROPLIST_LV110_CHEST.add(new ItemHolder(48207, 1)); // Radiant Wizard's Circlet
		DROPLIST_LV110_CHEST.add(new ItemHolder(48210, 1)); // Radiant Knight's Circlet
		DROPLIST_LV110_CHEST.add(new ItemHolder(48493, 1)); // Dragon Rind Leather Shirt
		DROPLIST_LV110_CHEST.add(new ItemHolder(29758, 1)); // Hidden Secret Book
		DROPLIST_LV110_CHEST.add(new ItemHolder(48876, 1)); // Forgotten Spellbook Chapter 1
		DROPLIST_LV110_CHEST.add(new ItemHolder(48877, 1)); // Forgotten Spellbook Chapter 2
		DROPLIST_LV110_CHEST.add(new ItemHolder(48878, 1)); // Forgotten Spellbook Chapter 3
		DROPLIST_LV110_CHEST.add(new ItemHolder(48324, 1)); // Agathion Master's Box
		DROPLIST_LV110_CHEST.add(new ItemHolder(48910, 1)); // Shillien's Soul Crystal Box
		DROPLIST_LV110_CHEST.add(new ItemHolder(80996, 1)); // Storm Isle's Time Stone
		DROPLIST_LV110_CHEST.add(new ItemHolder(80997, 1)); // Primeval Isle's Time Stone
		DROPLIST_LV110_CHEST.add(new ItemHolder(80998, 1)); // Golden Altar's Time Stone
		DROPLIST_LV110_CHEST.add(new ItemHolder(81358, 1)); // Abandoned Coal Mines' Time Stone
		DROPLIST_LV110_CHEST.add(new ItemHolder(47751, 1)); // +8 Eternal Heavy Armor Capsule
		DROPLIST_LV110_CHEST.add(new ItemHolder(47752, 1)); // +8 Eternal Light Armor Capsule
		DROPLIST_LV110_CHEST.add(new ItemHolder(47753, 1)); // +8 Eternal Robe Capsule
	}
	private static final List<ItemHolder> DROPLIST_LV120_CHEST = new ArrayList<>();
	static
	{
		DROPLIST_LV120_CHEST.add(new ItemHolder(38859, 1)); // Ruby Lv. 5
		DROPLIST_LV120_CHEST.add(new ItemHolder(47686, 1)); // Blue Cat's Eye Lv. 5
		DROPLIST_LV120_CHEST.add(new ItemHolder(47681, 1)); // Red Cat's Eye Lv. 5
		DROPLIST_LV120_CHEST.add(new ItemHolder(38889, 1)); // Aquamarine Lv. 5
		DROPLIST_LV120_CHEST.add(new ItemHolder(46684, 1)); // Tanzanite Lv. 5
		DROPLIST_LV120_CHEST.add(new ItemHolder(38884, 1)); // Emerald Lv. 5
		DROPLIST_LV120_CHEST.add(new ItemHolder(38931, 1)); // Sapphire Lv. 5
		DROPLIST_LV120_CHEST.add(new ItemHolder(38874, 1)); // Obsidian Lv. 5
		DROPLIST_LV120_CHEST.add(new ItemHolder(38879, 1)); // Opal Lv. 5
		DROPLIST_LV120_CHEST.add(new ItemHolder(38899, 1)); // Pearl Lv. 5
		DROPLIST_LV120_CHEST.add(new ItemHolder(46674, 1)); // Vital Stone Lv. 5
		DROPLIST_LV120_CHEST.add(new ItemHolder(46679, 1)); // Garnet Lv. 5
		DROPLIST_LV120_CHEST.add(new ItemHolder(38854, 1)); // Topaz Lv. 5
		DROPLIST_LV120_CHEST.add(new ItemHolder(38894, 1)); // Diamond Lv. 5
		DROPLIST_LV120_CHEST.add(new ItemHolder(81453, 1)); // Improved Rune Stone
		DROPLIST_LV120_CHEST.add(new ItemHolder(80419, 1)); // Artifact Pack
		DROPLIST_LV120_CHEST.add(new ItemHolder(81463, 1)); // Tower of Insolence's Time Stone (Lower Floor)
		DROPLIST_LV120_CHEST.add(new ItemHolder(81449, 1)); // Angel's Earring Pack
		DROPLIST_LV120_CHEST.add(new ItemHolder(81450, 1)); // Angel's Ring Pack
		DROPLIST_LV120_CHEST.add(new ItemHolder(81448, 1)); // Angel's Necklace Pack
		DROPLIST_LV120_CHEST.add(new ItemHolder(48860, 1)); // Fallen Angel's Ring Pack
	}
	// Misc
	private static final int TREASURE_CHEST_CHANCE = 15; // 15% chance to spawn
	private static final long RESPAWN_DELAY = 43200000; // 12 hours = 43.200.000 milliseconds
	private static final long RND_OFFSET = 10800000; // 3 hours = 10.800.000 milliseconds
	
	private GreedyTreasureChests()
	{
		super();
		addStartNpc(CHEST_LV110);
		addStartNpc(CHEST_LV120);
		addTalkId(CHEST_LV110);
		addTalkId(CHEST_LV120);
		addKillId(CHEST_LV110);
		addKillId(CHEST_LV120);
		addKillId(TRIGGER_MOBS_LV110_CHEST_SV);
		addKillId(TRIGGER_MOBS_LV110_CHEST_IT);
		addKillId(TRIGGER_MOBS_LV110_CHEST_TC);
		addKillId(TRIGGER_MOBS_LV110_CHEST_AI);
		addKillId(TRIGGER_MOBS_LV110_CHEST_FS);
		addKillId(TRIGGER_MOBS_LV110_CHEST_FM);
		addKillId(TRIGGER_MOBS_LV120_CHEST_VS);
		addKillId(TRIGGER_MOBS_LV120_CHEST_KO);
		addKillId(TRIGGER_MOBS_LV120_CHEST_FW);
		addKillId(TRIGGER_MOBS_LV120_CHEST_IP);
		addKillId(TRIGGER_MOBS_LV120_CHEST_BS);
		addKillId(TRIGGER_MOBS_LV120_CHEST_SM);
		addKillId(TRIGGER_MOBS_LV120_CHEST_PL);
		addKillId(TRIGGER_MOBS_LV120_CHEST_FOM);
		addKillId(TRIGGER_MOBS_LV120_CHEST_SS);
		addKillId(TRIGGER_MOBS_LV120_CHEST_DV);
		addKillId(TRIGGER_MOBS_LV120_CHEST_FT);
		addKillId(TRIGGER_MOBS_LV120_CHEST_WS);
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance killer, boolean isSummon)
	{
		if (npc.getId() == CHEST_LV110)
		{
			giveItems(killer, getRandomEntry(DROPLIST_LV110_CHEST));
		}
		else if (npc.getId() == CHEST_LV120)
		{
			giveItems(killer, getRandomEntry(DROPLIST_LV120_CHEST));
		}
		else if (getRandom(150) == TREASURE_CHEST_CHANCE)
		{
			final int npcId = npc.getId();
			final long currentTime = Chronos.currentTimeMillis();
			if (CommonUtil.contains(TRIGGER_MOBS_LV110_CHEST_SV, npcId))
			{
				if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_SV_1", 0))
				{
					addSpawn(CHEST_LV110, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_SV_1", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
				else if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_SV_2", 0))
				{
					addSpawn(CHEST_LV110, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_SV_2", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
			}
			else if (CommonUtil.contains(TRIGGER_MOBS_LV110_CHEST_IT, npcId))
			{
				if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_IT_1", 0))
				{
					addSpawn(CHEST_LV110, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_IT_1", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
				else if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_IT_2", 0))
				{
					addSpawn(CHEST_LV110, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_IT_2", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
			}
			else if (CommonUtil.contains(TRIGGER_MOBS_LV110_CHEST_TC, npcId))
			{
				if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_TC_1", 0))
				{
					addSpawn(CHEST_LV110, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_TC_1", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
				else if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_TC_2", 0))
				{
					addSpawn(CHEST_LV110, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_TC_2", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
			}
			else if (CommonUtil.contains(TRIGGER_MOBS_LV110_CHEST_AI, npcId))
			{
				if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_AI_1", 0))
				{
					addSpawn(CHEST_LV110, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_AI_1", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
				else if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_AI_2", 0))
				{
					addSpawn(CHEST_LV110, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_AI_2", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
			}
			else if (CommonUtil.contains(TRIGGER_MOBS_LV110_CHEST_FS, npcId))
			{
				if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_FS_1", 0))
				{
					addSpawn(CHEST_LV110, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_FS_1", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
				else if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_FS_2", 0))
				{
					addSpawn(CHEST_LV110, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_FS_2", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
			}
			else if (CommonUtil.contains(TRIGGER_MOBS_LV110_CHEST_FM, npcId))
			{
				if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_FM_1", 0))
				{
					addSpawn(CHEST_LV110, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_FM_1", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
				else if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_FM_2", 0))
				{
					addSpawn(CHEST_LV110, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_FM_2", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
				else if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_FM_3", 0))
				{
					addSpawn(CHEST_LV110, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_FM_3", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
				else if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_FM_4", 0))
				{
					addSpawn(CHEST_LV110, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_FM_4", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
			}
			else if (CommonUtil.contains(TRIGGER_MOBS_LV120_CHEST_VS, npcId))
			{
				if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_VS_1", 0))
				{
					addSpawn(CHEST_LV120, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_VS_1", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
				else if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_VS_2", 0))
				{
					addSpawn(CHEST_LV120, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_VS_2", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
			}
			else if (CommonUtil.contains(TRIGGER_MOBS_LV120_CHEST_KO, npcId))
			{
				if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_KO_1", 0))
				{
					addSpawn(CHEST_LV120, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_KO_1", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
				else if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_KO_2", 0))
				{
					addSpawn(CHEST_LV120, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_KO_2", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
			}
			else if (CommonUtil.contains(TRIGGER_MOBS_LV120_CHEST_FW, npcId))
			{
				if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_FW_1", 0))
				{
					addSpawn(CHEST_LV120, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_FW_1", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
				else if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_FW_2", 0))
				{
					addSpawn(CHEST_LV120, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_FW_2", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
			}
			else if (CommonUtil.contains(TRIGGER_MOBS_LV120_CHEST_IP, npcId))
			{
				if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_IP_1", 0))
				{
					addSpawn(CHEST_LV120, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_IP_1", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
				else if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_IP_2", 0))
				{
					addSpawn(CHEST_LV120, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_IP_2", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
			}
			else if (CommonUtil.contains(TRIGGER_MOBS_LV120_CHEST_BS, npcId))
			{
				if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_BS_1", 0))
				{
					addSpawn(CHEST_LV120, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_BS_1", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
				else if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_BS_2", 0))
				{
					addSpawn(CHEST_LV120, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_BS_2", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
			}
			else if (CommonUtil.contains(TRIGGER_MOBS_LV120_CHEST_SM, npcId))
			{
				if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_SM_1", 0))
				{
					addSpawn(CHEST_LV120, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_SM_1", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
				else if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_SM_2", 0))
				{
					addSpawn(CHEST_LV120, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_SM_2", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
			}
			else if (CommonUtil.contains(TRIGGER_MOBS_LV120_CHEST_PL, npcId))
			{
				if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_PL_1", 0))
				{
					addSpawn(CHEST_LV120, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_PL_1", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
				else if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_PL_2", 0))
				{
					addSpawn(CHEST_LV120, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_PL_2", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
			}
			else if (CommonUtil.contains(TRIGGER_MOBS_LV120_CHEST_FOM, npcId))
			{
				if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_FOM_1", 0))
				{
					addSpawn(CHEST_LV120, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_FOM_1", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
				else if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_FOM_2", 0))
				{
					addSpawn(CHEST_LV120, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_FOM_2", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
			}
			else if (CommonUtil.contains(TRIGGER_MOBS_LV120_CHEST_SS, npcId))
			{
				if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_SS_1", 0))
				{
					addSpawn(CHEST_LV120, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_SS_1", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
				else if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_SS_2", 0))
				{
					addSpawn(CHEST_LV120, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_SS_2", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
			}
			else if (CommonUtil.contains(TRIGGER_MOBS_LV120_CHEST_DV, npcId))
			{
				if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_DV_1", 0))
				{
					addSpawn(CHEST_LV120, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_DV_1", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
				else if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_DV_2", 0))
				{
					addSpawn(CHEST_LV120, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_DV_2", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
			}
			else if (CommonUtil.contains(TRIGGER_MOBS_LV120_CHEST_FT, npcId))
			{
				if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_FT_1", 0))
				{
					addSpawn(CHEST_LV120, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_FT_1", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
				else if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_FT_2", 0))
				{
					addSpawn(CHEST_LV120, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_FT_2", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
			}
			else if (CommonUtil.contains(TRIGGER_MOBS_LV120_CHEST_WS, npcId))
			{
				if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_WS_1", 0))
				{
					addSpawn(CHEST_LV120, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_WS_1", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
				else if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_WS_2", 0))
				{
					addSpawn(CHEST_LV120, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_WS_2", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
				else if (currentTime > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_WS_3", 0))
				{
					addSpawn(CHEST_LV120, npc, true, 0, true);
					GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_WS_3", Long.toString(currentTime + RESPAWN_DELAY + Rnd.get(RND_OFFSET)));
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new GreedyTreasureChests();
	}
}
