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
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemChanceHolder;
import org.l2jmobius.gameserver.model.itemcontainer.PlayerInventory;
import org.l2jmobius.gameserver.network.GameClient;

/**
 * @author Index, Mobius
 */
public class ExLetterCollectorTakeReward implements IClientIncomingPacket
{
	// Items
	private static final int A = 3875;
	private static final int E = 3877;
	private static final int G = 3879;
	private static final int I = 3881;
	private static final int L = 3882;
	private static final int N = 3883;
	private static final int O = 3884;
	private static final int T = 3886;
	private static final int H = 3880;
	private static final int II = 3888;
	private static final int W = 22894;
	private static final int D = 29545;
	private static final int P = 29825;
	
	// Rewards
	private static final ItemChanceHolder[] LINEAGE_II_REWARDS =
	{
		new ItemChanceHolder(80333, 5, 1), // Dragon Necklace
		new ItemChanceHolder(80334, 5, 1), // Dragon Earring
		new ItemChanceHolder(80335, 5, 1), // Dragon Ring
		new ItemChanceHolder(48296, 5, 1), // +10 Eternal Light Armor Capsule
		new ItemChanceHolder(48297, 5, 1), // +10 Eternal Robe Capsule
		new ItemChanceHolder(48295, 5, 1), // +10 Eternal Heavy Armor Capsule
		new ItemChanceHolder(39573, 5, 1), // Sealed Talisman - Insanity
		new ItemChanceHolder(37716, 5, 1), // Seven Signs' Energy
		new ItemChanceHolder(80416, 5, 1), // Fortune Box of 10 Billion Adena
		new ItemChanceHolder(36414, 5, 1), // Dragon Claw
		new ItemChanceHolder(47705, 15, 1), // Blue Cat's Eye Lv. 3 Jewelry Box
		new ItemChanceHolder(47627, 15, 1), // Sapphire Lv. 3 Jewelry Box
		new ItemChanceHolder(47626, 15, 1), // Ruby Lv. 3 Jewelry Box
		new ItemChanceHolder(47703, 15, 1), // Red Cat's Eye Lv. 3 Jewelry Box
		new ItemChanceHolder(47629, 15, 1), // Opal Lv. 3 Jewelry Box
		new ItemChanceHolder(28360, 15, 1), // Cat's Eye Lv. 3 Jewelry Box
		new ItemChanceHolder(28362, 15, 1), // Amethyst Lv. 3 Jewelry Box
		new ItemChanceHolder(47631, 15, 1), // Diamond Lv. 3 Jewelry Box
		new ItemChanceHolder(47634, 15, 1), // Pearl Lv. 3 Jewelry Box
		new ItemChanceHolder(47635, 15, 1), // Vital Stone Lv. 3 Jewelry Box
		new ItemChanceHolder(47630, 15, 1), // Obsidian Lv. 3 Jewelry Box
		new ItemChanceHolder(47633, 15, 1), // Aquamarine Lv. 3 Jewelry Box
		new ItemChanceHolder(80423, 30, 1), // Balance Artifact Box
		new ItemChanceHolder(80420, 30, 1), // Attack Artifact Box
		new ItemChanceHolder(80421, 30, 1), // Protection Artifact Box
		new ItemChanceHolder(80422, 30, 1), // Support Artifact Box
		new ItemChanceHolder(39374, 30, 1), // Scroll: 5.000.000 SP
		new ItemChanceHolder(46274, 30, 1), // Wind Vitality Tonic
		new ItemChanceHolder(35563, 30, 1), // Giant's Energy
		new ItemChanceHolder(38761, 30, 1), // Energy of Destruction 3-unit Pack
		new ItemChanceHolder(36513, 30, 1), // Elcyum Powder
		new ItemChanceHolder(27673, 30, 1), // Freya's Scroll of Storm
	};
	private static final ItemChanceHolder[] DEATH_REWARDS =
	{
		new ItemChanceHolder(47821, 5, 1), // Sayha's Talisman Lv. 10
		new ItemChanceHolder(47820, 5, 1), // Sayha's Talisman Lv. 9
		new ItemChanceHolder(37717, 5, 1), // Talisman - Seven Signs
		new ItemChanceHolder(39572, 5, 1), // Talisman - Insanity
		new ItemChanceHolder(48032, 5, 1), // Sayha's Talisman Lv. 8
		new ItemChanceHolder(80416, 5, 1), // Fortune Box of 10 Billion Adena
		new ItemChanceHolder(28448, 5, 1), // Sayha's Talisman Lv. 7
		new ItemChanceHolder(37715, 5, 1), // Talisman - Anakim
		new ItemChanceHolder(35649, 15, 1), // Sealed Talisman - Longing
		new ItemChanceHolder(29152, 15, 1), // High-grade Zodiac Agathion Pack
		new ItemChanceHolder(29151, 15, 1), // Mid-grade Zodiac Agathion Pack
		new ItemChanceHolder(28373, 15, 1), // Tanzenite Lv. 3 Jewelry Box
		new ItemChanceHolder(47632, 15, 1), // Emerald Lv. 3 Jewelry Box
		new ItemChanceHolder(47636, 15, 1), // Garnet Lv. 3 Jewelry Box
		new ItemChanceHolder(80423, 30, 1), // Balance Artifact Box
		new ItemChanceHolder(80420, 30, 1), // Attack Artifact Box
		new ItemChanceHolder(80421, 30, 1), // Protection Artifact Box
		new ItemChanceHolder(80422, 30, 1), // Support Artifact Box
		new ItemChanceHolder(39374, 30, 1), // Scroll: 5.000.000 SP
		new ItemChanceHolder(46274, 30, 1), // Wind Vitality Tonic
		new ItemChanceHolder(35563, 30, 1), // Giant's Energy
		new ItemChanceHolder(38761, 30, 1), // Energy of Destruction 3-unit Pack
		new ItemChanceHolder(36513, 30, 1), // Elcyum Powder
		new ItemChanceHolder(27673, 30, 1), // Freya's Scroll of Storm
	};
	private static final ItemChanceHolder[] KNIGHT_REWARDS =
	{
		new ItemChanceHolder(80416, 5, 1), // Fortune Box of 10 Billion Adena
		new ItemChanceHolder(28448, 5, 1), // Sayha's Talisman Lv. 7
		new ItemChanceHolder(26291, 5, 1), // Kaliel's Energy - Longing
		new ItemChanceHolder(36731, 5, 1), // Life Stone: Giant's Power
		new ItemChanceHolder(37714, 5, 1), // Talisman - Lilith
		new ItemChanceHolder(29150, 15, 1), // Low-grade Zodiac Agathion Pack
		new ItemChanceHolder(80423, 30, 1), // Balance Artifact Box
		new ItemChanceHolder(80420, 30, 1), // Attack Artifact Box
		new ItemChanceHolder(80421, 30, 1), // Protection Artifact Box
		new ItemChanceHolder(80422, 30, 1), // Support Artifact Box
		new ItemChanceHolder(36515, 15, 1), // Elcyum
		new ItemChanceHolder(48215, 30, 1), // Circlet Spirit Stone
		new ItemChanceHolder(28540, 30, 1), // Magnificent Brooch Spirit Stone
		new ItemChanceHolder(36514, 30, 1), // Elcyum Crystal
		new ItemChanceHolder(39374, 30, 1), // Scroll: 5.000.000 SP
		new ItemChanceHolder(46274, 30, 1), // Wind Vitality Tonic
		new ItemChanceHolder(35563, 30, 1), // Giant's Energy
		new ItemChanceHolder(38761, 30, 1), // Energy of Destruction 3-unit Pack
		new ItemChanceHolder(27673, 30, 1), // Freya's Scroll of Storm
	};
	
	private int _wordId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_wordId = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		final PlayerInventory inventory = player.getInventory();
		if (inventory == null)
		{
			return;
		}
		
		switch (_wordId)
		{
			case 0:
			{
				if ((inventory.getInventoryItemCount(L, -1) < 1) || //
					(inventory.getInventoryItemCount(I, -1) < 1) || //
					(inventory.getInventoryItemCount(N, -1) < 1) || //
					(inventory.getInventoryItemCount(E, -1) < 2) || //
					(inventory.getInventoryItemCount(A, -1) < 1) || //
					(inventory.getInventoryItemCount(G, -1) < 1) || //
					(inventory.getInventoryItemCount(II, -1) < 1))
				{
					return;
				}
				
				player.destroyItemByItemId("LetterCollector", L, 1, player, true);
				player.destroyItemByItemId("LetterCollector", I, 1, player, true);
				player.destroyItemByItemId("LetterCollector", N, 1, player, true);
				player.destroyItemByItemId("LetterCollector", E, 1, player, true);
				player.destroyItemByItemId("LetterCollector", A, 1, player, true);
				player.destroyItemByItemId("LetterCollector", G, 1, player, true);
				player.destroyItemByItemId("LetterCollector", E, 1, player, true);
				player.destroyItemByItemId("LetterCollector", II, 1, player, true);
				
				player.addItem("LetterCollector", getRandomReward(LINEAGE_II_REWARDS), player, true);
				break;
			}
			case 1:
			{
				if ((inventory.getInventoryItemCount(N, -1) < 2) || //
					(inventory.getInventoryItemCount(E, -1) < 3) || //
					(inventory.getInventoryItemCount(W, -1) < 1) || //
					(inventory.getInventoryItemCount(L, -1) < 1) || //
					(inventory.getInventoryItemCount(G, -1) < 1) || //
					(inventory.getInventoryItemCount(D, -1) < 1))
				{
					return;
				}
				
				player.destroyItemByItemId("LetterCollector", N, 1, player, true);
				player.destroyItemByItemId("LetterCollector", E, 1, player, true);
				player.destroyItemByItemId("LetterCollector", W, 1, player, true);
				player.destroyItemByItemId("LetterCollector", L, 1, player, true);
				player.destroyItemByItemId("LetterCollector", E, 1, player, true);
				player.destroyItemByItemId("LetterCollector", G, 1, player, true);
				player.destroyItemByItemId("LetterCollector", E, 1, player, true);
				player.destroyItemByItemId("LetterCollector", N, 1, player, true);
				player.destroyItemByItemId("LetterCollector", D, 1, player, true);
				
				player.addItem("LetterCollector", getRandomReward(DEATH_REWARDS), player, true);
				break;
			}
			case 2:
			{
				if ((inventory.getInventoryItemCount(T, -1) < 3) || //
					(inventory.getInventoryItemCount(O, -1) < 2) || //
					(inventory.getInventoryItemCount(H, -1) < 1) || //
					(inventory.getInventoryItemCount(E, -1) < 1) || //
					(inventory.getInventoryItemCount(P, -1) < 1))
				{
					return;
				}
				
				player.destroyItemByItemId("LetterCollector", T, 1, player, true);
				player.destroyItemByItemId("LetterCollector", O, 1, player, true);
				player.destroyItemByItemId("LetterCollector", T, 1, player, true);
				player.destroyItemByItemId("LetterCollector", H, 1, player, true);
				player.destroyItemByItemId("LetterCollector", E, 1, player, true);
				player.destroyItemByItemId("LetterCollector", T, 1, player, true);
				player.destroyItemByItemId("LetterCollector", O, 1, player, true);
				player.destroyItemByItemId("LetterCollector", P, 1, player, true);
				
				player.addItem("LetterCollector", getRandomReward(KNIGHT_REWARDS), player, true);
				break;
			}
		}
	}
	
	private ItemChanceHolder getRandomReward(ItemChanceHolder[] rewards)
	{
		ItemChanceHolder reward = null;
		while (reward == null)
		{
			final ItemChanceHolder random = rewards[Rnd.get(rewards.length)];
			if ((Rnd.get(100) < random.getChance()) && (ItemTable.getInstance().getTemplate(random.getId()) != null))
			{
				reward = random;
			}
		}
		return reward;
	}
}