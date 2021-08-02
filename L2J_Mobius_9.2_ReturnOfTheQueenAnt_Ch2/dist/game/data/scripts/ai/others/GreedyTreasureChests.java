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
 * @author CostyKiller
 */
public final class GreedyTreasureChests extends AbstractNpcAI
{
	// NPCs
	private static final int CHEST_LV110 = 8710;
	private static final int CHEST_LV120 = 8711;
	//@formatter:off
	private static final int[] TRIGGER_MOBS_LV110_CHEST_2_SPAWNS = // Silent Valley, Ivory Tower Crater, Alligator Island, Tanor Canyon, Field of Silence
	{
		24506, 24507, 24508, 24509, 24510, 24421, 24422, 24423, 24424, 24425,
		24426, 24373, 24376, 24377, 20936, 20937, 20938, 20939, 20940, 20941,
		20942, 20943, 24587, 24517, 24520, 24521, 24522, 24523,
	};
	private static final int[] TRIGGER_MOBS_LV110_CHEST_4_SPAWNS = // Forest of Mirrors
	{
		24461, 24462, 24463, 24464, 24465, 24466,
	};
	private static final int[] TRIGGER_MOBS_LV120_CHEST_2_SPAWNS = // Varka Silenos Barracks, Ketra Orc Outpost, Field of Whispers, Isle of Prayer, Breka's Stronghold, Sel Mahum Training Grounds, Plains of Lizardmen, Fields of Massacre, Sea of Spores, Dragon Valley, Fafurion Temple
	{
		24636, 24637, 24638, 24639, 24640, 24631, 24632, 24633, 24635, 24304,
		24305, 24306, 24307, 24308, 24445, 24446, 24447, 24448, 24451, 24415,
		24416, 24417, 24418, 24419, 24420, 24492, 24493, 24494, 24495, 24496,
		24497, 24498, 24499, 24486, 24487, 24488, 24489, 24490, 24491, 24621,
		24622, 24623, 24624, 24481, 24482, 24318, 24322, 24323, 24325, 24329,
	};
	private static final int[] TRIGGER_MOBS_LV120_CHEST_3_SPAWNS = // Wastelands
	{
		24500, 24501, 24502, 24503, 24504, 24505,
	};
	//@formatter:on
	// Items
	private static final List<ItemHolder> DROPLIST_LV110_CHEST = new ArrayList<>();
	static
	{
		DROPLIST_LV110_CHEST.add(new ItemHolder(17538, 1)); // +7 Bloody Helios Pack
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
	private static final long RND_OFFSET = Rnd.get(10800000); // 3 hours = 10.800.000 milliseconds
	
	private GreedyTreasureChests()
	{
		super();
		addStartNpc(CHEST_LV110, CHEST_LV120);
		addTalkId(CHEST_LV110, CHEST_LV120);
		addKillId(CHEST_LV110, CHEST_LV120);
		addKillId(TRIGGER_MOBS_LV110_CHEST_2_SPAWNS);
		addKillId(TRIGGER_MOBS_LV110_CHEST_4_SPAWNS);
		addKillId(TRIGGER_MOBS_LV120_CHEST_2_SPAWNS);
		addKillId(TRIGGER_MOBS_LV120_CHEST_3_SPAWNS);
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
		else if ((getRandom(100) < TREASURE_CHEST_CHANCE) && (Chronos.currentTimeMillis() > GlobalVariablesManager.getInstance().getLong("TREASURE_CHEST_RESPAWN_DELAY", 0)))
		{
			final int treasureChestId;
			if (CommonUtil.contains(TRIGGER_MOBS_LV110_CHEST_2_SPAWNS, npc.getId()) || CommonUtil.contains(TRIGGER_MOBS_LV110_CHEST_4_SPAWNS, npc.getId()))
			{
				treasureChestId = CHEST_LV110;
			}
			else
			{
				treasureChestId = CHEST_LV120;
			}
			// Number of spawns by areas.
			if (CommonUtil.contains(TRIGGER_MOBS_LV110_CHEST_2_SPAWNS, npc.getId()) || CommonUtil.contains(TRIGGER_MOBS_LV120_CHEST_2_SPAWNS, npc.getId()))
			{
				addSpawn(treasureChestId, npc, true, 0, true);
				addSpawn(treasureChestId, npc, true, 0, true);
			}
			else if (CommonUtil.contains(TRIGGER_MOBS_LV110_CHEST_4_SPAWNS, npc.getId()))
			{
				addSpawn(treasureChestId, npc, true, 0, true);
				addSpawn(treasureChestId, npc, true, 0, true);
				addSpawn(treasureChestId, npc, true, 0, true);
				addSpawn(treasureChestId, npc, true, 0, true);
			}
			else if (CommonUtil.contains(TRIGGER_MOBS_LV120_CHEST_3_SPAWNS, npc.getId()))
			{
				addSpawn(treasureChestId, npc, true, 0, true);
				addSpawn(treasureChestId, npc, true, 0, true);
				addSpawn(treasureChestId, npc, true, 0, true);
			}
			// Set global variable to check spawn time after 12 hours +/- 3 hours.
			GlobalVariablesManager.getInstance().set("TREASURE_CHEST_RESPAWN_DELAY", Chronos.currentTimeMillis() + RESPAWN_DELAY + RND_OFFSET);
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new GreedyTreasureChests();
	}
}