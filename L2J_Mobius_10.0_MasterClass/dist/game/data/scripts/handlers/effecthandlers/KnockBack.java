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
package handlers.effecthandlers;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.gameserver.ai.CtrlEvent;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.enums.ClassId;
import org.l2jmobius.gameserver.enums.FlyType;
import org.l2jmobius.gameserver.enums.Race;
import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.effects.EffectFlag;
import org.l2jmobius.gameserver.model.effects.EffectType;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.stats.Formulas;
import org.l2jmobius.gameserver.network.serverpackets.ExAlterSkillRequest;
import org.l2jmobius.gameserver.network.serverpackets.FlyToLocation;
import org.l2jmobius.gameserver.network.serverpackets.ValidateLocation;
import org.l2jmobius.gameserver.util.Util;

/**
 * Check if this effect is not counted as being stunned.
 * @author UnAfraid, Mobius
 */
public class KnockBack extends AbstractEffect
{
	private final int _distance;
	private final int _speed;
	private final int _delay;
	private final int _animationSpeed;
	private final boolean _knockDown;
	private final FlyType _type;
	
	private static final Set<Creature> ACTIVE_KNOCKBACKS = ConcurrentHashMap.newKeySet();
	private static final Map<ClassId, Integer> KNOCKBACK_SKILLS = new EnumMap<>(ClassId.class);
	static
	{
		KNOCKBACK_SKILLS.put(ClassId.SIGEL_PHOENIX_KNIGHT, 10250); // Heavy Hit
		KNOCKBACK_SKILLS.put(ClassId.SIGEL_HELL_KNIGHT, 10250); // Heavy Hit
		KNOCKBACK_SKILLS.put(ClassId.SIGEL_EVA_TEMPLAR, 10250); // Heavy Hit
		KNOCKBACK_SKILLS.put(ClassId.SIGEL_SHILLIEN_TEMPLAR, 10250); // Heavy Hit
		KNOCKBACK_SKILLS.put(ClassId.SIGEL_DEATH_KNIGHT, 10250); // Heavy Hit
		KNOCKBACK_SKILLS.put(ClassId.TYRR_DUELIST, 10500); // Heavy Hit
		KNOCKBACK_SKILLS.put(ClassId.TYRR_DREADNOUGHT, 10500); // Heavy Hit
		KNOCKBACK_SKILLS.put(ClassId.TYRR_TITAN, 10500); // Heavy Hit
		KNOCKBACK_SKILLS.put(ClassId.TYRR_GRAND_KHAVATARI, 10500); // Heavy Hit
		KNOCKBACK_SKILLS.put(ClassId.TYRR_MAESTRO, 10500); // Heavy Hit
		KNOCKBACK_SKILLS.put(ClassId.TYRR_DOOMBRINGER, 10500); // Heavy Hit
		KNOCKBACK_SKILLS.put(ClassId.OTHELL_ADVENTURER, 10750); // Heavy Hit
		KNOCKBACK_SKILLS.put(ClassId.OTHELL_WIND_RIDER, 10750); // Heavy Hit
		KNOCKBACK_SKILLS.put(ClassId.OTHELL_GHOST_HUNTER, 10750); // Heavy Hit
		KNOCKBACK_SKILLS.put(ClassId.OTHELL_FORTUNE_SEEKER, 10750); // Heavy Hit
		KNOCKBACK_SKILLS.put(ClassId.YUL_SAGITTARIUS, 11000); // Heavy Hit
		KNOCKBACK_SKILLS.put(ClassId.YUL_MOONLIGHT_SENTINEL, 11000); // Heavy Hit
		KNOCKBACK_SKILLS.put(ClassId.YUL_GHOST_SENTINEL, 11000); // Heavy Hit
		KNOCKBACK_SKILLS.put(ClassId.YUL_TRICKSTER, 11000); // Heavy Hit
		KNOCKBACK_SKILLS.put(ClassId.FEOH_ARCHMAGE, 11250); // Heavy Hit
		KNOCKBACK_SKILLS.put(ClassId.FEOH_SOULTAKER, 11250); // Heavy Hit
		KNOCKBACK_SKILLS.put(ClassId.FEOH_MYSTIC_MUSE, 11250); // Heavy Hit
		KNOCKBACK_SKILLS.put(ClassId.FEOH_STORM_SCREAMER, 11250); // Heavy Hit
		KNOCKBACK_SKILLS.put(ClassId.FEOH_SOUL_HOUND, 11250); // Heavy Hit
		KNOCKBACK_SKILLS.put(ClassId.ISS_HIEROPHANT, 11750); // Heavy Hit
		KNOCKBACK_SKILLS.put(ClassId.ISS_SWORD_MUSE, 11750); // Heavy Hit
		KNOCKBACK_SKILLS.put(ClassId.ISS_SPECTRAL_DANCER, 11750); // Heavy Hit
		KNOCKBACK_SKILLS.put(ClassId.ISS_DOMINATOR, 11750); // Heavy Hit
		KNOCKBACK_SKILLS.put(ClassId.ISS_DOOMCRYER, 11750); // Heavy Hit
		KNOCKBACK_SKILLS.put(ClassId.WYNN_ARCANA_LORD, 11500); // Heavy Hit
		KNOCKBACK_SKILLS.put(ClassId.WYNN_ELEMENTAL_MASTER, 11500); // Heavy Hit
		KNOCKBACK_SKILLS.put(ClassId.WYNN_SPECTRAL_MASTER, 11500); // Heavy Hit
		KNOCKBACK_SKILLS.put(ClassId.AEORE_CARDINAL, 12000); // Heavy Hit
		KNOCKBACK_SKILLS.put(ClassId.AEORE_EVA_SAINT, 12000); // Heavy Hit
		KNOCKBACK_SKILLS.put(ClassId.AEORE_SHILLIEN_SAINT, 12000); // Heavy Hit
	}
	
	public KnockBack(StatSet params)
	{
		_distance = params.getInt("distance", 50);
		_speed = params.getInt("speed", 0);
		_delay = params.getInt("delay", 0);
		_animationSpeed = params.getInt("animationSpeed", 0);
		_knockDown = params.getBoolean("knockDown", false);
		_type = params.getEnum("type", FlyType.class, _knockDown ? FlyType.PUSH_DOWN_HORIZONTAL : FlyType.PUSH_HORIZONTAL);
	}
	
	@Override
	public boolean calcSuccess(Creature effector, Creature effected, Skill skill)
	{
		return _knockDown || Formulas.calcProbability(100, effector, effected, skill);
	}
	
	@Override
	public boolean isInstant()
	{
		return !_knockDown;
	}
	
	@Override
	public long getEffectFlags()
	{
		return _knockDown ? EffectFlag.BLOCK_ACTIONS.getMask() : super.getEffectFlags();
	}
	
	@Override
	public EffectType getEffectType()
	{
		return _knockDown ? EffectType.BLOCK_ACTIONS : super.getEffectType();
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		if (!_knockDown)
		{
			knockBack(effector, effected);
		}
	}
	
	@Override
	public void continuousInstant(Creature effector, Creature effected, Skill skill, Item item)
	{
		effected.startParalyze();
		
		if (_knockDown)
		{
			knockBack(effector, effected);
		}
	}
	
	@Override
	public void onExit(Creature effector, Creature effected, Skill skill)
	{
		ACTIVE_KNOCKBACKS.remove(effected);
		effected.updateAbnormalVisualEffects();
		
		if (!effected.isPlayer())
		{
			effected.getAI().notifyEvent(CtrlEvent.EVT_THINK);
		}
	}
	
	private void knockBack(Creature effector, Creature effected)
	{
		if (!ACTIVE_KNOCKBACKS.contains(effected))
		{
			ACTIVE_KNOCKBACKS.add(effected);
			
			// Prevent knocking back raids and town NPCs.
			if (effected.isRaid() || (effected.isNpc() && !effected.isAttackable()))
			{
				return;
			}
			
			final double radians = Math.toRadians(Util.calculateAngleFrom(effector, effected));
			final int x = (int) (effected.getX() + (_distance * Math.cos(radians)));
			final int y = (int) (effected.getY() + (_distance * Math.sin(radians)));
			final int z = effected.getZ();
			final Location loc = GeoEngine.getInstance().getValidLocation(effected.getX(), effected.getY(), effected.getZ(), x, y, z, effected.getInstanceWorld());
			
			effected.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			effected.broadcastPacket(new FlyToLocation(effected, loc, _type, _speed, _delay, _animationSpeed));
			if (_knockDown)
			{
				effected.setHeading(Util.calculateHeadingFrom(effected, effector));
			}
			effected.setXYZ(loc);
			effected.broadcastPacket(new ValidateLocation(effected));
			effected.revalidateZone(true);
			
			World.getInstance().forEachVisibleObjectInRange(effected, Player.class, 1200, nearby ->
			{
				if ((nearby.getRace() != Race.ERTHEIA) && (nearby.getTarget() == effected) && nearby.isInCategory(CategoryType.SIXTH_CLASS_GROUP) && !nearby.isAlterSkillActive())
				{
					final int chainSkill = KNOCKBACK_SKILLS.get(nearby.getClassId()).intValue();
					if (nearby.getSkillRemainingReuseTime(chainSkill) == -1)
					{
						nearby.sendPacket(new ExAlterSkillRequest(nearby, chainSkill, chainSkill, 3));
					}
				}
			});
		}
	}
}
