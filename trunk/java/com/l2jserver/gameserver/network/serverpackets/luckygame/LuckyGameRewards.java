/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.network.serverpackets.luckygame;

import java.util.ArrayList;
import java.util.List;

import com.l2jserver.gameserver.model.holders.ItemHolder;
import com.l2jserver.util.Rnd;

/**
 * @author Mobius
 */
public final class LuckyGameRewards // TODO: Move to XML?
{
	private final static List<ItemHolder> _fortuneReadingTicketRewards = new ArrayList<>();
	static
	{
		_fortuneReadingTicketRewards.add(new ItemHolder(6656, 1)); // Antharas' Earring
		_fortuneReadingTicketRewards.add(new ItemHolder(21712, 1)); // Blessed Zaken's Earring
		_fortuneReadingTicketRewards.add(new ItemHolder(37511, 1)); // Enhanced Istina's Earring - Warrior
		_fortuneReadingTicketRewards.add(new ItemHolder(37517, 1)); // Enhanced Istina's Earring - Wizard
		_fortuneReadingTicketRewards.add(new ItemHolder(37512, 1)); // Enhanced Istina's Necklace - Warrior
		_fortuneReadingTicketRewards.add(new ItemHolder(37518, 1)); // Enhanced Istina's Necklace - Wizard
		_fortuneReadingTicketRewards.add(new ItemHolder(37510, 1)); // Enhanced Istina's Ring - Warrior
		_fortuneReadingTicketRewards.add(new ItemHolder(37516, 1)); // Enhanced Istina's Ring - Wizard
		_fortuneReadingTicketRewards.add(new ItemHolder(37508, 1)); // Enhanced Octavis' Earring - Warrior
		_fortuneReadingTicketRewards.add(new ItemHolder(37514, 1)); // Enhanced Octavis' Earring - Wizard
		_fortuneReadingTicketRewards.add(new ItemHolder(37509, 1)); // Enhanced Octavis' Necklace - Warrior
		_fortuneReadingTicketRewards.add(new ItemHolder(37515, 1)); // Enhanced Octavis' Necklace - Wizard
		_fortuneReadingTicketRewards.add(new ItemHolder(37507, 1)); // Enhanced Octavis' Ring - Warrior
		_fortuneReadingTicketRewards.add(new ItemHolder(37513, 1)); // Enhanced Octavis' Ring - Wizard
		_fortuneReadingTicketRewards.add(new ItemHolder(6661, 1)); // Orfen's Earring
		_fortuneReadingTicketRewards.add(new ItemHolder(6660, 1)); // Queen Ant's Ring
		_fortuneReadingTicketRewards.add(new ItemHolder(6657, 1)); // Valakas' Necklace
		_fortuneReadingTicketRewards.add(new ItemHolder(17527, 1)); // Scroll: Enchant Armor: (R-grade)
		_fortuneReadingTicketRewards.add(new ItemHolder(17526, 1)); // Scroll: Enchant Weapon: (R-grade)
		_fortuneReadingTicketRewards.add(new ItemHolder(19448, 1)); // Blessed Scroll: Enchant Armor (R-grade)
		_fortuneReadingTicketRewards.add(new ItemHolder(19447, 1)); // Blessed Scroll: Enchant Weapon (R-grade)
		_fortuneReadingTicketRewards.add(new ItemHolder(33817, 1)); // Giant's Scroll: Enchant Armor (R-grade)
		_fortuneReadingTicketRewards.add(new ItemHolder(33811, 1)); // Giant's Scroll: Enchant Weapon (R-grade)
		_fortuneReadingTicketRewards.add(new ItemHolder(36160, 1)); // Scroll of Blessing: (R2-grade)
		_fortuneReadingTicketRewards.add(new ItemHolder(36161, 1)); // Scroll of Blessing: (R95-grade)
		_fortuneReadingTicketRewards.add(new ItemHolder(36162, 1)); // Scroll of Blessing: (R99-grade)
		_fortuneReadingTicketRewards.add(new ItemHolder(27600, 1)); // Scroll of Blessing: Armor (R95-grade)
		_fortuneReadingTicketRewards.add(new ItemHolder(27601, 1)); // Scroll of Blessing: Armor (R99-grade)
		_fortuneReadingTicketRewards.add(new ItemHolder(27597, 1)); // Scroll of Blessing: Weapon (R95-grade)
		_fortuneReadingTicketRewards.add(new ItemHolder(27598, 1)); // Scroll of Blessing: Weapon (R99-grade)
		_fortuneReadingTicketRewards.add(new ItemHolder(36513, 5)); // Elcyum Powder
		_fortuneReadingTicketRewards.add(new ItemHolder(39631, 1)); // Fortune Pocket - Stage 3
		_fortuneReadingTicketRewards.add(new ItemHolder(39633, 1)); // Fortune Pocket - Stage 5
		_fortuneReadingTicketRewards.add(new ItemHolder(19440, 5)); // Gemstone (R-grade)
		_fortuneReadingTicketRewards.add(new ItemHolder(35563, 2)); // Giant's Energy
		_fortuneReadingTicketRewards.add(new ItemHolder(46395, 1)); // Shadow Ingot
		_fortuneReadingTicketRewards.add(new ItemHolder(39592, 1)); // Tempest Stone
		_fortuneReadingTicketRewards.add(new ItemHolder(46151, 1)); // Superior Giant's Codex - Mastery Chapter 1
		_fortuneReadingTicketRewards.add(new ItemHolder(46150, 1)); // Superior Giant's Codex Chapter 1
		_fortuneReadingTicketRewards.add(new ItemHolder(46478, 1)); // Leona's Soul Crystal - Stage 5
		_fortuneReadingTicketRewards.add(new ItemHolder(46508, 1)); // Lionel's Soul Crystal - Stage 5
		_fortuneReadingTicketRewards.add(new ItemHolder(46493, 1)); // Pantheon's Soul Crystal - Stage 5
		_fortuneReadingTicketRewards.add(new ItemHolder(46449, 1)); // Kain's Soul Crystal - Stage 6
		_fortuneReadingTicketRewards.add(new ItemHolder(46509, 1)); // Lionel's Soul Crystal - Stage 6
		_fortuneReadingTicketRewards.add(new ItemHolder(46464, 1)); // Mermoden's Soul Crystal - Stage 6
		_fortuneReadingTicketRewards.add(new ItemHolder(46494, 1)); // Pantheon's Soul Crystal - Stage 6
		_fortuneReadingTicketRewards.add(new ItemHolder(39637, 1)); // Venir's Talisman - Stage 4
		_fortuneReadingTicketRewards.add(new ItemHolder(39639, 1)); // Venir's Talisman - Stage 6
		_fortuneReadingTicketRewards.add(new ItemHolder(39641, 1)); // Venir's Talisman - Stage 8
		_fortuneReadingTicketRewards.add(new ItemHolder(35649, 1)); // Sealed Talisman - Longing
		_fortuneReadingTicketRewards.add(new ItemHolder(37715, 1)); // Talisman - Anakim
		_fortuneReadingTicketRewards.add(new ItemHolder(37714, 1)); // Talisman - Lilith
	}
	private static final List<ItemHolder> _luxuryFortuneReadingTicketRewards = new ArrayList<>();
	static
	{
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(22426, 1)); // Ancient Scroll: Enchant Weapon: (R-grade)
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(19448, 1)); // Blessed Scroll: Enchant Armor: (R-grade)
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(19447, 1)); // Blessed Scroll: Enchant Weapon: (R-grade)
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(36164, 2)); // Giant's Scroll: Enchant Armor: (R-grade)
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(33811, 1)); // Giant's Scroll: Enchant Weapon (R-grade)
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(27600, 2)); // Scroll of Blessing: Armor: (R95 grade)
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(27601, 2)); // Scroll of Blessing: Armor: (R99 grade)
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(36160, 1)); // Scroll of Blessing: (R2-grade)
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(36161, 1)); // Scroll of Blessing: (R95-grade)
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(36162, 1)); // Scroll of Blessing: (R99-grade)
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(27597, 1)); // Scroll of Blessing: Weapon: (R95-grade)
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(27598, 1)); // Scroll of Blessing: Weapon: (R99-grade)
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(17527, 2)); // Scroll: Enchant Armor: (R-grade)
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(17526, 1)); // Scroll: Enchant Weapon: (R-grade)
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(35566, 1)); // Dark Amaranthine Enhancement Stone
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(35567, 1)); // Dark Eternal Enhancement Stone
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(6656, 1)); // Antharas' Earring
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(21712, 1)); // Blessed Zaken's Earring
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(37511, 1)); // Enhanced Istina's Earring - Warrior
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(37517, 1)); // Enhanced Istina's Earring - Wizard
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(37512, 1)); // Enhanced Istina's Necklace - Warrior
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(37518, 1)); // Enhanced Istina's Necklace - Wizard
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(37510, 1)); // Enhanced Istina's Ring - Warrior
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(37516, 1)); // Enhanced Istina's Ring - Wizard
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(37508, 1)); // Enhanced Octavis' Earring - Warrior
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(37514, 1)); // Enhanced Octavis' Earring - Wizard
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(37509, 1)); // Enhanced Octavis' Necklace - Warrior
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(37515, 1)); // Enhanced Octavis' Necklace - Wizard
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(37507, 1)); // Enhanced Octavis' Ring - Warrior
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(37513, 1)); // Enhanced Octavis' Ring - Wizard
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(6661, 1)); // Orfen's Earring
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(6660, 1)); // Queen Ant's Ring
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(6657, 1)); // Valakas' Necklace
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(27462, 1)); // Angel Circlet Box
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(27460, 1)); // Cat Ears Box
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(27463, 1)); // Demon Circlet Box
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(27466, 1)); // Eyepatch Box
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(27467, 1)); // Party Hat Box
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(27464, 1)); // Pirate's Eyepatch Box
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(27458, 1)); // Rabbit Ears Box
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(27459, 1)); // Raccoon Ears Box
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(27468, 1)); // Refined Angel Ring Box
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(27469, 1)); // Refined Devil Horn Box
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(27461, 1)); // Romantic Chapeau Box
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(27465, 1)); // Outlaw's Eyepatch Box
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(36515, 1)); // Elcyum
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(39631, 3)); // Fortune Pocket - Stage 3
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(39633, 1)); // Fortune Pocket - Stage 5
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(19440, 15)); // Gem Stone - R grade
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(35563, 10)); // Giant's Energy
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(39738, 1)); // Rune Stone
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(39592, 1)); // Tempest Stone
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(46150, 1)); // Superior Giant's Codex - Chapter 1
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(46151, 1)); // Superior Giant's Codex - Mastery Chapter 1
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(46478, 1)); // Leona's Soul Crystal - Stage 5
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(46508, 1)); // Lionel's Soul Crystal - Stage 5
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(46493, 1)); // Pantheon's Soul Crystal - Stage 5
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(46449, 1)); // Kain's Soul Crystal - Stage 6
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(46479, 1)); // Leona's Soul Crystal - Stage 6
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(46509, 1)); // Lionel's Soul Crystal - Stage 6
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(46464, 1)); // Mermoden's Soul Crystal - Stage 6
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(46494, 1)); // Pantheon's Soul Crystal - Stage 6
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(46553, 1)); // Mystic Soul Crystal
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(37722, 1)); // Eva's Special Pack
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(37720, 1)); // Maphr's Special Pack
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(37719, 1)); // Pa'agrio's Special Pack
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(37721, 1)); // Sayha's Special Pack
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(26444, 1)); // Shadow Buster Pack
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(26438, 1)); // Shadow Cutter Pack
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(26446, 1)); // Shadow Dualsword Pack
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(26440, 1)); // Shadow Fighter Pack
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(46395, 10)); // Shadow Ingot
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(26445, 1)); // Shadow Retributer Pack
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(26437, 1)); // Shadow Shaper Pack
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(26443, 1)); // Shadow Shooter Pack
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(26439, 1)); // Shadow Slasher Pack
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(26441, 1)); // Shadow Stormer Pack
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(26442, 1)); // Shadow Thrower Pack
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(37715, 1)); // Talisman - Anakim
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(37714, 1)); // Talisman - Lilith
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(37717, 1)); // Talisman - Seven Signs
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(39637, 1)); // Venir's Talisman - Stage 4
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(39639, 1)); // Venir's Talisman - Stage 6
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(39641, 1)); // Venir's Talisman - Stage 8
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(39643, 1)); // Venir's Talisman - Stage 10
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(39647, 1)); // Venir's Talisman - Stage 14
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(39573, 1)); // Sealed Talisman - Insanity
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(35649, 1)); // Sealed Talisman - Longing
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(37716, 1)); // Seven Signs' Energy
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(34999, 1)); // Tauti's Axe
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(35001, 1)); // Tauti's Dual Axe
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(34998, 1)); // Tauti's One-handed Axe
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(46063, 1)); // Kelbim Crossbow
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(46061, 1)); // Kelbim Dual Dagger
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(46062, 1)); // Kelbim's Bow
		_luxuryFortuneReadingTicketRewards.add(new ItemHolder(46060, 1)); // Kelbim's Dagger
	}
	private static final List<ItemHolder> _rareLuxuryFortuneReadingTicketRewards = new ArrayList<>();
	static
	{
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(22426, 1)); // Ancient Scroll: Enchant Weapon - R grade
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(35566, 1)); // Dark Amaranthine Enhancement Stone
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(35567, 1)); // Dark Eternal Enhancement Stone
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(27601, 2)); // Scroll of Blessing: Armor (R99-grade)
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(36160, 1)); // Scroll of Blessing: (R2-grade)
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(36161, 1)); // Scroll of Blessing: (R95-grade)
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(36162, 1)); // Scroll of Blessing: (R99-grade)
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(27598, 1)); // Scroll of Blessing: Weapon (R99-grade)
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(6656, 1)); // Antharas' Earring
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(21712, 1)); // Blessed Zaken's Earring
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(37511, 1)); // Enhanced Istina's Earring - Warrior
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(37517, 1)); // Enhanced Istina's Earring - Wizard
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(37512, 1)); // Enhanced Istina's Necklace - Warrior
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(37518, 1)); // Enhanced Istina's Necklace - Wizard
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(37510, 1)); // Enhanced Istina's Ring - Warrior
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(37516, 1)); // Enhanced Istina's Ring - Wizard
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(37508, 1)); // Enhanced Octavis' Earring - Warrior
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(37514, 1)); // Enhanced Octavis' Earring - Wizard
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(37509, 1)); // Enhanced Octavis' Necklace - Warrior
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(37515, 1)); // Enhanced Octavis' Necklace - Wizard
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(37507, 1)); // Enhanced Octavis' Ring - Warrior
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(37513, 1)); // Enhanced Octavis' Ring - Wizard
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(6661, 1)); // Orfen's Earring
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(6660, 1)); // Queen Ant's Ring
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(27462, 1)); // Angel Circlet Box
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(27460, 1)); // Cat Ears Box
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(27463, 1)); // Devil Circlet Box
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(27464, 1)); // Eyepatch Box
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(27465, 1)); // Outlaw's Eyepatch Box
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(27467, 1)); // Party Hat Box
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(27464, 1)); // Pirate's Eyepatch Box
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(27458, 1)); // Rabbit Ears Box
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(27459, 1)); // Raccoon Ears Box
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(27468, 1)); // Refined Angel Ring Box
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(27469, 1)); // Refined Devil Horn Box
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(27461, 1)); // Romantic Chapeau Box
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(37722, 1)); // Eva's Special Pack
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(37720, 1)); // Maphr's Special Pack
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(37719, 1)); // Pa'agrio's Special Pack
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(37721, 1)); // Sayha's Special Pack
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(46449, 1)); // Kain's Soul Crystal - Stage 6
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(46478, 1)); // Leona's Soul Crystal - Stage 5
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(46479, 1)); // Leona's Soul Crystal - Stage 6
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(46508, 1)); // Lionel's Soul Crystal - Stage 5
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(46509, 1)); // Lionel's Soul Crystal - Stage 6
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(46464, 1)); // Mermoden's Soul Crystal - Stage 6
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(46493, 1)); // Pantheon's Soul Crystal - Stage 5
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(46494, 1)); // Pantheon's Soul Crystal - Stage 6
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(46553, 1)); // Mystic Soul Crystal
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(46063, 1)); // Kelbim Crossbow
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(46061, 1)); // Kelbim Dual Dagger
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(46062, 1)); // Kelbim's Bow
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(46060, 1)); // Kelbim's Dagger
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(34999, 1)); // Tauti's Axe
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(35001, 1)); // Tauti's Dual Axe
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(34998, 1)); // Tauti's One-handed Axe
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(6657, 1)); // Valakas' Necklace
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(46395, 10)); // Shadow Ingot
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(46151, 1)); // Superior Giant's Codex - Mastery Chapter 1
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(39573, 1)); // Sealed Talisman - Insanity
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(35649, 1)); // Sealed Talisman - Longing
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(37716, 1)); // Seven Signs' Energy
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(37715, 1)); // Talisman - Anakim
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(37714, 1)); // Talisman - Lilith
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(37717, 1)); // Talisman - Seven Signs
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(39641, 1)); // Venir's Talisman - Stage 8
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(39643, 1)); // Venir's Talisman - Stage 10
		_rareLuxuryFortuneReadingTicketRewards.add(new ItemHolder(39647, 1)); // Venir's Talisman - Stage 14
	}
	
	public static ItemHolder getRandomNormal()
	{
		return _fortuneReadingTicketRewards.get(Rnd.get(_fortuneReadingTicketRewards.size()));
	}
	
	public static ItemHolder getRandomLuxury()
	{
		return _luxuryFortuneReadingTicketRewards.get(Rnd.get(_luxuryFortuneReadingTicketRewards.size()));
	}
	
	public static ItemHolder getRandomRare()
	{
		return _rareLuxuryFortuneReadingTicketRewards.get(Rnd.get(_rareLuxuryFortuneReadingTicketRewards.size()));
	}
}
