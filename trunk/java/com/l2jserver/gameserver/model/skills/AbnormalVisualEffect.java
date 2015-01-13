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
package com.l2jserver.gameserver.model.skills;

/**
 * Abnormal Visual Effect enumerated.
 * @author NosBit
 */
public enum AbnormalVisualEffect
{
	DOT_BLEEDING(1),
	DOT_POISON(2),
	DOT_FIRE(3),
	DOT_WATER(4),
	DOT_WIND(5),
	DOT_SOIL(6),
	STUN(7),
	SLEEP(8),
	SILENCE(9),
	ROOT(10),
	PARALYZE(11),
	FLESH_STONE(12),
	DOT_MP(13),
	BIG_HEAD(14),
	DOT_FIRE_AREA(15),
	CHANGE_TEXTURE(16),
	BIG_BODY(17),
	FLOATING_ROOT(18),
	DANCE_ROOT(19),
	GHOST_STUN(20),
	STEALTH(21),
	SEIZURE1(22),
	SEIZURE2(23),
	MAGIC_SQUARE(24),
	FREEZING(25),
	SHAKE(26),
	ULTIMATE_DEFENCE(28),
	VP_UP(29),
	REAL_TARGET(30),
	DEATH_MARK(31),
	TURN_FLEE(32),
	INVINCIBILITY(33),
	AIR_BATTLE_SLOW(34),
	AIR_BATTLE_ROOT(35),
	CHANGE_WEAPON(36),
	CHANGE_GOLD_AFRO(37),
	CHANGE_PINK_AFRO(38),
	CHANGE_BLACK_AFRO(39),
	STIGMA_OF_SILEN(41),
	SPEED_DOWN(42),
	FROZEN_PILLAR(43),
	CHANGE_VESPER_S(44),
	CHANGE_VESPER_C(45),
	CHANGE_VESPER_D(46),
	TIME_BOMB(47),
	MP_SHIELD(48),
	AIR_BIND(49),
	CHANGE_BODY(50),
	KNOCK_DOWN(51),
	NAVIT_ADVENT(52),
	KNOCK_BACK(53),
	CHANGE_7TH_ANNIVERSARY(54),
	ON_SPOT_MOVEMENT(55),
	DEPORT(56),
	AURA_BUFF(57),
	AURA_BUFF_SELF(58),
	AURA_DEBUFF(59),
	AURA_DEBUFF_SELF(60),
	HURRICANE(61),
	HURRICANE_SELF(62),
	BLACK_MARK(63),
	SOUL_AVATAR(64),
	CHANGE_8TH_ANNIVERSARY(65),
	NO_CHAT(68),
	HERB_OF_POWER(69),
	HERB_OF_MAGIC(70),
	TALISMAN_DECO_DARK_PURPLE(71),
	TALISMAN_DECO_GOLD(73),
	TALISMAN_DECO_ORANGE(74),
	TALISMAN_DECO_BLUE(75),
	TALISMAN_DECO_LIGHT_PURPLE(76),
	CHANGE_CURIOUS_HOUSE(77),
	CHANGE_MEMORY_N(78),
	CHANGE_MEMORY_D(79),
	CHANGE_MEMORY_C(80),
	CHANGE_MEMORY_B(81),
	CHANGE_MEMORY_A(82),
	CHANGE_SWIMSUIT_A(83),
	CHANGE_SWIMSUIT_B(84),
	CHANGE_XMAS(85),
	CARD_PC_DECO(86),
	CHANGE_DINOS(87),
	CHANGE_VALENTINE(88),
	CHOCOLATE(89),
	CANDY(90),
	COOKIE(91),
	STARS_0(92),
	STARS_1(93),
	STARS_2(94),
	STARS_3(95),
	STARS_4(96),
	STARS_5(97),
	DUELING(98),
	FREEZING2(99),
	CHANGE_YOGI(100),
	YOGI(101),
	MUSICAL_NOTE_YELLOW(102),
	MUSICAL_NOTE_BLUE(103),
	MUSICAL_NOTE_GREEN(104),
	TENTH_ANNIVERSARY(105),
	XMAS_SOCKS(106),
	XMAS_TREE(107),
	XMAS_SNOWMAN(108),
	OTHELL_ROGUE_BLUFF(109),
	HE_PROTECT(110),
	SU_SUMCROSS(111),
	WIND_STUN(112),
	STORM_SIGN2(113),
	STORM_SIGN1(114),
	WIND_BLEND(115),
	DECEPTIVE_BLINK(116),
	WIND_HIDE(117),
	PSY_POWER(118),
	SQUALL(119),
	WIND_ILLUSION(120),
	SAYHA_FURY(121),
	HIDE4(123),
	PMENTAL_TRAIL(124),
	HOLD_LIGHTING(125),
	GRAVITY_SPACE_3(126),
	SPACEREF(127),
	HE_ASPECT(128),
	CHANGE_AR1(129),
	CHANGE_AR2(130),
	CHANGE_AR3(131),
	CHANGE_AR4(132),
	CHANGE_AR5(133),
	CHANGE_AR6(134),
	CHANGE_WP1(135),
	CHANGE_WP2(136),
	CHANGE_HALLOWEEN(1000);
	
	private final int _clientId;
	
	private AbnormalVisualEffect(int clientId)
	{
		_clientId = clientId;
	}
	
	/**
	 * Gets the client id.
	 * @return the client id
	 */
	public int getClientId()
	{
		return _clientId;
	}
	
	/**
	 * Finds abnormal visual effect by name.
	 * @param name the name
	 * @return The abnormal visual effect if its found, {@code null} otherwise
	 */
	public static final AbnormalVisualEffect findByName(String name)
	{
		for (AbnormalVisualEffect abnormalVisualEffect : values())
		{
			if (abnormalVisualEffect.name().equalsIgnoreCase(name))
			{
				return abnormalVisualEffect;
			}
		}
		return null;
	}
}