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
package com.l2jmobius.gameserver.model.cubic;

import java.util.Comparator;
import java.util.concurrent.ScheduledFuture;

import com.l2jmobius.Config;
import com.l2jmobius.commons.util.Rnd;
import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Party;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.templates.L2CubicTemplate;
import com.l2jmobius.gameserver.model.skills.Skill;
import com.l2jmobius.gameserver.network.serverpackets.ExUserInfoCubic;
import com.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;

/**
 * @author UnAfraid
 */
public class CubicInstance
{
	private final L2PcInstance _owner;
	private final L2PcInstance _caster;
	private final L2CubicTemplate _template;
	private ScheduledFuture<?> _skillUseTask;
	private ScheduledFuture<?> _expireTask;
	
	public CubicInstance(L2PcInstance owner, L2PcInstance caster, L2CubicTemplate template)
	{
		_owner = owner;
		_caster = caster;
		_template = template;
		activate();
	}
	
	private void activate()
	{
		_skillUseTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this::tryToUseSkill, 0, _template.getDelay() * 1000);
		_expireTask = ThreadPoolManager.getInstance().scheduleAi(this::deactivate, _template.getDuration() * 1000);
	}
	
	public void deactivate()
	{
		if ((_skillUseTask != null) && !_skillUseTask.isDone())
		{
			_skillUseTask.cancel(true);
		}
		_skillUseTask = null;
		
		if ((_expireTask != null) && !_expireTask.isDone())
		{
			_expireTask.cancel(true);
		}
		_expireTask = null;
		_owner.getCubics().remove(_template.getId());
		_owner.sendPacket(new ExUserInfoCubic(_owner));
		_owner.broadcastCharInfo();
	}
	
	private void tryToUseSkill()
	{
		final double random = Rnd.nextDouble() * 100;
		double commulativeChance = 0;
		for (CubicSkill cubicSkill : _template.getSkills())
		{
			if ((commulativeChance += cubicSkill.getTriggerRate()) > random)
			{
				final Skill skill = cubicSkill.getSkill();
				if ((skill != null) && (Rnd.get(100) < cubicSkill.getSuccessRate()))
				{
					final L2Character target = findTarget(cubicSkill);
					if (target != null)
					{
						_caster.broadcastPacket(new MagicSkillUse(_owner, target, skill.getDisplayId(), skill.getDisplayLevel(), skill.getHitTime(), skill.getReuseDelay()));
						skill.activateSkill(_owner, target);
						break;
					}
				}
			}
		}
	}
	
	private L2Character findTarget(CubicSkill cubicSkill)
	{
		switch (_template.getTargetType())
		{
			case BY_SKILL:
			{
				if (!_template.validateConditions(this, _owner, _owner))
				{
					return null;
				}
				
				final Skill skill = cubicSkill.getSkill();
				if (skill != null)
				{
					switch (cubicSkill.getTargetType())
					{
						case HEAL:
						{
							final L2Party party = _owner.getParty();
							if (party != null)
							{
								return party.getMembers().stream().filter(member -> cubicSkill.validateConditions(this, _owner, member) && member.isInsideRadius(_owner, Config.ALT_PARTY_RANGE, true, true)).sorted(Comparator.comparingInt(L2Character::getCurrentHpPercent).reversed()).findFirst().orElse(null);
							}
							if (cubicSkill.validateConditions(this, _owner, _owner))
							{
								return _owner;
							}
							break;
						}
						case MASTER:
						{
							if (cubicSkill.validateConditions(this, _owner, _owner))
							{
								return _owner;
							}
							break;
						}
						case TARGET:
						{
							final L2Object possibleTarget = skill.getTarget(_owner, false, false, false);
							if ((possibleTarget != null) && possibleTarget.isCharacter())
							{
								if (cubicSkill.validateConditions(this, _owner, (L2Character) possibleTarget))
								{
									return (L2Character) possibleTarget;
								}
							}
							break;
						}
					}
				}
				break;
			}
			case TARGET:
			{
				switch (cubicSkill.getTargetType())
				{
					case HEAL:
					{
						final L2Party party = _owner.getParty();
						if (party != null)
						{
							return party.getMembers().stream().filter(member -> cubicSkill.validateConditions(this, _owner, member) && member.isInsideRadius(_owner, Config.ALT_PARTY_RANGE, true, true)).sorted(Comparator.comparingInt(L2Character::getCurrentHpPercent).reversed()).findFirst().orElse(null);
						}
						if (cubicSkill.validateConditions(this, _owner, _owner))
						{
							return _owner;
						}
						break;
					}
					case MASTER:
					{
						if (cubicSkill.validateConditions(this, _owner, _owner))
						{
							return _owner;
						}
						break;
					}
					case TARGET:
					{
						final L2Object targetObject = _owner.getTarget();
						if ((targetObject != null) && targetObject.isCharacter())
						{
							final L2Character target = (L2Character) targetObject;
							if (cubicSkill.validateConditions(this, _owner, target))
							{
								return target;
							}
						}
						break;
					}
				}
				break;
			}
			case HEAL:
			{
				final L2Party party = _owner.getParty();
				if (party != null)
				{
					return party.getMembers().stream().filter(member -> member.isInsideRadius(_owner, Config.ALT_PARTY_RANGE, true, true)).sorted(Comparator.comparingInt(L2Character::getCurrentHpPercent).reversed()).findFirst().orElse(null);
				}
				if (cubicSkill.validateConditions(this, _owner, _owner))
				{
					return _owner;
				}
				break;
			}
		}
		return null;
	}
	
	/**
	 * @return the {@link L2Character} that owns this cubic
	 */
	public L2Character getOwner()
	{
		return _owner;
	}
	
	/**
	 * @return the {@link L2Character} that casted this cubic
	 */
	public L2Character getCaster()
	{
		return _caster;
	}
	
	/**
	 * @return {@code true} if cubic is casted from someone else but the owner, {@code false}
	 */
	public boolean isGivenByOther()
	{
		return _caster != _owner;
	}
	
	/**
	 * @return the {@link L2CubicTemplate} of this cubic
	 */
	public L2CubicTemplate getTemplate()
	{
		return _template;
	}
}
