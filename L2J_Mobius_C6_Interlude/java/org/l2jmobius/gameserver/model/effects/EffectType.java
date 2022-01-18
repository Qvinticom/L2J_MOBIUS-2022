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
package org.l2jmobius.gameserver.model.effects;

/**
 * Effect types.
 * @author nBd
 */
public enum EffectType
{
	BUFF,
	DEBUFF,
	CHARGE,
	DMG_OVER_TIME,
	HEAL_OVER_TIME,
	COMBAT_POINT_HEAL_OVER_TIME,
	MANA_DMG_OVER_TIME,
	MANA_HEAL_OVER_TIME,
	MP_CONSUME_PER_LEVEL,
	RELAXING,
	STUN,
	ROOT,
	SLEEP,
	HATE,
	FAKE_DEATH,
	CONFUSION,
	CONFUSE_MOB_ONLY,
	MUTE,
	IMMOBILEUNTILATTACKED,
	FEAR,
	SALVATION,
	SILENT_MOVE,
	SIGNET_EFFECT,
	SIGNET_GROUND,
	SEED,
	PARALYZE,
	STUN_SELF,
	PSYCHICAL_MUTE,
	REMOVE_TARGET,
	TARGET_ME,
	SILENCE_MAGIC_PHYSICAL,
	BETRAY,
	NOBLESSE_BLESSING,
	PHOENIX_BLESSING,
	PETRIFICATION,
	BLUFF,
	BATTLE_FORCE,
	SPELL_FORCE,
	CHARM_OF_LUCK,
	INVINCIBLE,
	PROTECTION_BLESSING,
	INTERRUPT,
	MEDITATION,
	BLOW,
	FUSION,
	CANCEL,
	BLOCK_BUFF,
	BLOCK_DEBUFF,
	PREVENT_BUFF,
	CLAN_GATE,
	NEGATE,
	HERO_CANCEL
}