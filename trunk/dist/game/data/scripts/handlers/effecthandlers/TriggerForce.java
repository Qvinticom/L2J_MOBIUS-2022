/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.effecthandlers;

import java.util.ArrayList;
import java.util.List;

import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.conditions.Condition;
import com.l2jserver.gameserver.model.effects.AbstractEffect;
import com.l2jserver.gameserver.model.holders.SkillHolder;
import com.l2jserver.gameserver.model.skills.BuffInfo;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.model.zone.ZoneId;

/**
 * Force Skill effect implementation.
 * @author Mobius, NviX
 */
public final class TriggerForce extends AbstractEffect
{
	private final SkillHolder _skill;
	private final List<L2PcInstance> _affectedMembers = new ArrayList<>();
	private final List<L2Character> _affectedObjects = new ArrayList<>();
	private final List<L2Character> _affectedObjToRemove = new ArrayList<>();
	private static final int SIGEL_FORCE = 1928;
	private static final int TYRR_FORCE = 1930;
	private static final int OTHELL_FORCE = 1932;
	private static final int YUL_FORCE = 1934;
	private static final int FEOH_FORCE = 1936;
	private static final int WYNN_FORCE = 1938;
	private static final int AEORE_FORCE = 1940;
	private static final int EVISCERATOR_FORCE = 30603;
	private static final int SAYHAS_SEER_FORCE = 30606;
	private static final int PARTY_SOLIDARITY = 1955;
	private static final int RAGE_AURA = 10029;
	private static final int CHALLENGE_AURA = 10031;
	private static final int IRON_AURA = 10033;
	private static final int RESISTANCE_AURA = 10035;
	private static final int RECOVERY_AURA = 10037;
	private static final int SPIRIT_AURA = 10039;
	private static final int ROLLING_THUNDER = 10287;
	
	/**
	 * @param attachCond
	 * @param applyCond
	 * @param set
	 * @param params
	 */
	public TriggerForce(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_skill = new SkillHolder(params.getInt("skillId", 0), 1);
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		final L2PcInstance effector = info.getEffector().getActingPlayer();
		final L2PcInstance effected = info.getEffected().getActingPlayer();
		
		if (effected.isDead() || (effector == null))
		{
			return;
		}
		
		if (_skill.getSkillId() == 0)
		{
			return;
		}
		if (effector.getParty() != null)
		{
			for (L2PcInstance member : effector.getParty().getMembers())
			{
				_affectedMembers.add(member);
				if ((member.calculateDistance(effector, true, false) < 900) && (_skill.getSkillId() != RAGE_AURA))
				{
					_skill.getSkill().applyEffects(effector, member);
				}
			}
		}
		else
		{
			if ((_skill.getSkillId() != RAGE_AURA) && (_skill.getSkillId() != ROLLING_THUNDER))
			{
				_skill.getSkill().applyEffects(effector, effector);
			}
			_affectedMembers.add(effector);
		}
	}
	
	@Override
	public boolean onActionTime(BuffInfo info)
	{
		final L2PcInstance effector = info.getEffector().getActingPlayer();
		// if die
		if (effector.isDead())
		{
			return false;
		}
		// apply offensive aura to enemies
		if ((_skill.getSkillId() == RAGE_AURA) || (_skill.getSkillId() == ROLLING_THUNDER))
		{
			final boolean srcInArena = (effector.isInsideZone(ZoneId.PVP) && (!effector.isInsideZone(ZoneId.SIEGE)));
			for (L2Character obj : effector.getKnownList().getKnownCharactersInRadius(200))
			{
				if (((obj.isAttackable() || obj.isPlayable()) && !obj.isDoor()) && Skill.checkForAreaOffensiveSkills(effector, obj, _skill.getSkill(), srcInArena) && !_affectedObjects.contains(obj))
				{
					_affectedObjects.add(obj);
					_skill.getSkill().applyEffects(effector, obj);
				}
			}
		}
		// remove offensive aura from enemies who not in affect radius
		if (!_affectedObjects.isEmpty())
		{
			for (L2Character obj : _affectedObjects)
			{
				if (!effector.getKnownList().getKnownCharactersInRadius(200).contains(obj))
				{
					if (obj.getEffectList().isAffectedBySkill(RAGE_AURA))
					{
						obj.getEffectList().remove(true, obj.getEffectList().getBuffInfoBySkillId(RAGE_AURA));
					}
					if (obj.getEffectList().isAffectedBySkill(ROLLING_THUNDER))
					{
						obj.getEffectList().remove(true, obj.getEffectList().getBuffInfoBySkillId(ROLLING_THUNDER));
					}
					_affectedObjToRemove.add(obj);
				}
			}
			if (!_affectedObjToRemove.isEmpty())
			{
				final int limit = _affectedObjToRemove.size();
				for (int i = 0; i < limit; i++)
				{
					if (_affectedObjects.contains(_affectedObjToRemove.get(i)))
					{
						_affectedObjects.remove(i);
						i--;
					}
				}
			}
			_affectedObjToRemove.clear();
		}
		// apply effect to new party members or remove if member > 900 distance from effector.
		if (effector.getParty() != null)
		{
			for (L2PcInstance member : effector.getParty().getMembers())
			{
				if (!_affectedMembers.contains(member))
				{
					_affectedMembers.add(member);
				}
				if (!member.getEffectList().isAffectedBySkill(_skill.getSkillId()) && (member.calculateDistance(effector, true, false) < 900) && (_skill.getSkillId() != RAGE_AURA) && (_skill.getSkillId() != ROLLING_THUNDER))
				{
					if ((member != effector))
					{
						_skill.getSkill().applyEffects(effector, member);
					}
					else if ((_skill.getSkillId() != CHALLENGE_AURA) && (_skill.getSkillId() != IRON_AURA) && (_skill.getSkillId() != RESISTANCE_AURA) && (_skill.getSkillId() != RECOVERY_AURA) && (_skill.getSkillId() != SPIRIT_AURA))
					{
						_skill.getSkill().applyEffects(effector, effector);
					}
				}
				else if (member.getEffectList().isAffectedBySkill(_skill.getSkillId()) && (member.calculateDistance(effector, true, false) > 900))
				{
					member.getEffectList().remove(true, member.getEffectList().getBuffInfoBySkillId(_skill.getSkillId()));
				}
			}
		}
		// if any member leave from party
		if (!_affectedMembers.isEmpty())
		{
			for (L2PcInstance player : _affectedMembers)
			{
				if ((player != effector) && !player.isInPartyWith(effector) && (player.getEffectList().getBuffInfoBySkillId(_skill.getSkillId()) != null))
				{
					player.getEffectList().remove(true, player.getEffectList().getBuffInfoBySkillId(_skill.getSkillId()));
					if (player.getEffectList().getBuffInfoBySkillId(PARTY_SOLIDARITY) != null)
					{
						player.getEffectList().remove(true, player.getEffectList().getBuffInfoBySkillId(PARTY_SOLIDARITY));
					}
				}
			}
		}
		_affectedMembers.clear();
		// Party Solidarity apply/remove
		if (effector.getParty() != null)
		{
			for (L2PcInstance member : effector.getParty().getMembers())
			{
				_affectedMembers.add(member);
				int activeForces = 0;
				if (member.getEffectList().getBuffInfoBySkillId(SIGEL_FORCE) != null)
				{
					activeForces++;
				}
				if (member.getEffectList().getBuffInfoBySkillId(TYRR_FORCE) != null)
				{
					activeForces++;
				}
				if (member.getEffectList().getBuffInfoBySkillId(OTHELL_FORCE) != null)
				{
					activeForces++;
				}
				if (member.getEffectList().getBuffInfoBySkillId(YUL_FORCE) != null)
				{
					activeForces++;
				}
				if (member.getEffectList().getBuffInfoBySkillId(FEOH_FORCE) != null)
				{
					activeForces++;
				}
				if (member.getEffectList().getBuffInfoBySkillId(WYNN_FORCE) != null)
				{
					activeForces++;
				}
				if (member.getEffectList().getBuffInfoBySkillId(AEORE_FORCE) != null)
				{
					activeForces++;
				}
				if (member.getEffectList().getBuffInfoBySkillId(EVISCERATOR_FORCE) != null)
				{
					activeForces++;
				}
				if (member.getEffectList().getBuffInfoBySkillId(SAYHAS_SEER_FORCE) != null)
				{
					activeForces++;
				}
				
				if ((activeForces < 4) || ((member.getEffectList().getBuffInfoBySkillId(AEORE_FORCE) == null) || (member.getEffectList().getBuffInfoBySkillId(SIGEL_FORCE) == null)))
				{
					if (member.getEffectList().getBuffInfoBySkillId(PARTY_SOLIDARITY) != null)
					{
						member.getEffectList().remove(true, member.getEffectList().getBuffInfoBySkillId(PARTY_SOLIDARITY));
					}
				}
				if ((activeForces >= 4) && (member.getEffectList().getBuffInfoBySkillId(AEORE_FORCE) != null) && (member.getEffectList().getBuffInfoBySkillId(SIGEL_FORCE) != null))
				{
					BuffInfo skill = member.getEffectList().getBuffInfoBySkillId(PARTY_SOLIDARITY);
					if (!member.getEffectList().isAffectedBySkill(PARTY_SOLIDARITY) || (skill.getSkill().getLevel() != Math.min((activeForces - 3), 3)))
					{
						member.makeTriggerCast(SkillData.getInstance().getSkill(PARTY_SOLIDARITY, Math.min((activeForces - 3), 3)), member);
						
					}
				}
			}
		}
		else
		{
			_affectedMembers.add(effector);
		}
		return true;
	}
	
	@Override
	public void onExit(BuffInfo info)
	{
		final L2PcInstance effector = info.getEffector().getActingPlayer();
		final int skillId = info.getSkill().getId();
		
		if ((effector.getEffectList().getBuffInfoBySkillId(skillId) == null) && (effector.getEffectList().getBuffInfoBySkillId(skillId + 1) != null))
		{
			effector.getEffectList().remove(true, effector.getEffectList().getBuffInfoBySkillId(skillId + 1));
		}
		
		if (effector.getParty() != null)
		{
			for (L2PcInstance member : _affectedMembers)
			{
				if (member.getEffectList().getBuffInfoBySkillId(skillId + 1) != null)
				{
					member.getEffectList().remove(true, member.getEffectList().getBuffInfoBySkillId(skillId + 1));
				}
			}
		}
		// remove Rage Aura from all affected enemies
		if (!_affectedObjects.isEmpty())
		{
			for (L2Character obj : _affectedObjects)
			{
				if (obj.getEffectList().isAffectedBySkill(RAGE_AURA))
				{
					obj.getEffectList().remove(true, obj.getEffectList().getBuffInfoBySkillId(RAGE_AURA));
				}
				if (obj.getEffectList().isAffectedBySkill(ROLLING_THUNDER))
				{
					obj.getEffectList().remove(true, obj.getEffectList().getBuffInfoBySkillId(ROLLING_THUNDER));
				}
			}
			_affectedObjects.clear();
			_affectedObjToRemove.clear();
		}
	}
}