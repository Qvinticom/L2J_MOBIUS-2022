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
package org.l2jmobius.gameserver.model.skill;

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
	VP_KEEP(29), // TODO: Unknown ClientID
	REAL_TARGET(30),
	DEATH_MARK(31),
	TURN_FLEE(32),
	INVINCIBILITY(33),
	AIR_BATTLE_SLOW(34),
	AIR_BATTLE_ROOT(35),
	CHANGE_WP(36),
	CHANGE_HAIR_G(37), // Gold Afro
	CHANGE_HAIR_P(38), // Pink Afro
	CHANGE_HAIR_B(39), // Black Afro
	UNKNOWN_40(40),
	STIGMA_OF_SILEN(41),
	SPEED_DOWN(42),
	FROZEN_PILLAR(43),
	CHANGE_VES_S(44),
	CHANGE_VES_C(45),
	CHANGE_VES_D(46),
	TIME_BOMB(47),
	MP_SHIELD(48),
	AIRBIND(49),
	CHANGEBODY(50),
	KNOCKDOWN(51),
	NAVIT_ADVENT(52),
	KNOCKBACK(53),
	CHANGE_7ANNIVERSARY(54),
	ON_SPOT_MOVEMENT(55),
	DEPORT(56),
	AURA_BUFF(57),
	AURA_BUFF_SELF(58),
	AURA_DEBUFF(59),
	AURA_DEBUFF_SELF(60),
	HURRICANE(61),
	HURRICANE_SELF(62),
	BLACK_MARK(63),
	BR_SOUL_AVATAR(64),
	CHANGE_GRADE_B(65),
	BR_BEAM_SWORD_ONEHAND(66),
	BR_BEAM_SWORD_DUAL(67),
	NO_CHAT(68),
	HERB_PA_UP(69),
	HERB_MA_UP(70),
	SEED_TALISMAN1(71),
	SEED_TALISMAN2(72),
	SEED_TALISMAN3(73),
	SEED_TALISMAN4(74),
	SEED_TALISMAN5(75),
	SEED_TALISMAN6(76),
	CURIOUS_HOUSE(77),
	NGRADE_CHANGE(78),
	DGRADE_CHANGE(79),
	CGRADE_CHANGE(80),
	BGRADE_CHANGE(81),
	AGRADE_CHANGE(82),
	SWEET_ICE_FLAKES(83),
	FANTASY_ICE_FLAKES(84),
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
	RUNWAY_ARMOR1(129),
	RUNWAY_ARMOR2(130),
	RUNWAY_ARMOR3(131),
	RUNWAY_ARMOR4(132),
	RUNWAY_ARMOR5(133),
	RUNWAY_ARMOR6(134),
	RUNWAY_WEAPON1(135),
	RUNWAY_WEAPON2(136),
	PALADIN_PROTECTION(141),
	SENTINEL_PROTECTION(142),
	REAL_TARGET_2(143),
	DIVINITY(144),
	SHILLIEN_PROTECTION(145),
	EVENT_STARS_0(146),
	EVENT_STARS_1(147),
	EVENT_STARS_2(148),
	EVENT_STARS_3(149),
	EVENT_STARS_4(150),
	EVENT_STARS_5(151),
	ABSORB_SHIELD(152),
	PHOENIX_AURA(153),
	REVENGE_AURA(154),
	EVAS_AURA(155),
	TEMPLAR_AURA(156),
	LONG_BLOW(157),
	WIDE_SWORD(158),
	BIG_FIST(159),
	SHADOW_STEP(160),
	TORNADO(161),
	SNOW_SLOW(162),
	SNOW_HOLD(163),
	TORNADO_SLOW(165),
	ASTATINE_WATER(166),
	BIG_BODY_COMBINATION_CAT_NPC(167),
	BIG_BODY_COMBINATION_UNICORN_NPC(168),
	BIG_BODY_COMBINATION_DEMON_NPC(169),
	BIG_BODY_COMBINATION_CAT_PC(170),
	BIG_BODY_COMBINATION_UNICORN_PC(171),
	BIG_BODY_COMBINATION_DEMON_PC(172),
	BIG_BODY_2(173),
	BIG_BODY_3(174),
	DRAGON_ULTIMATE(700),
	CHANGE_HALLOWEEN(1000);
	
	private final int _clientId;
	
	AbnormalVisualEffect(int clientId)
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
	public static AbnormalVisualEffect findByName(String name)
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