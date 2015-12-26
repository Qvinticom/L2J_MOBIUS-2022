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
package com.l2jserver.gameserver.model.actor.tasks.cubics;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.instance.L2CubicInstance;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jserver.util.Rnd;

/**
 * Cubic heal task.
 * @author Zoey76
 */
public class CubicHeal implements Runnable
{
	private static final Logger _log = Logger.getLogger(CubicHeal.class.getName());
	private final L2CubicInstance _cubic;
	private final AtomicInteger _currentCount = new AtomicInteger();
	
	public CubicHeal(L2CubicInstance cubic)
	{
		_cubic = cubic;
	}
	
	@Override
	public void run()
	{
		if (_cubic == null)
		{
			return;
		}
		
		if (_cubic.getOwner().isDead() || !_cubic.getOwner().isOnline())
		{
			_cubic.stopAction();
			_cubic.getOwner().getCubics().remove(_cubic.getId());
			_cubic.getOwner().broadcastUserInfo();
			_cubic.cancelDisappear();
			return;
		}
		
		// The cubic has already reached its limit and it will stay idle until its duration ends.
		if ((_cubic.getCubicMaxCount() > -1) && (_currentCount.get() >= _cubic.getCubicMaxCount()))
		{
			_cubic.stopAction();
			return;
		}
		
		try
		{
			Skill skill = null;
			// Base chance 10% to use great skill
			final double chance = Rnd.get();
			for (Skill sk : _cubic.getSkills())
			{
				switch (sk.getId())
				{
					case L2CubicInstance.SKILL_CUBIC_HEAL:
					case L2CubicInstance.SKILL_CUBIC_HEALER:
						skill = sk;
						break;
					case L2CubicInstance.SKILL_BUFF_CUBIC_HEAL:
						if (chance > 0.6)
						{
							skill = sk;
						}
						break;
					case L2CubicInstance.SKILL_MIND_CUBIC_RECHARGE:
						if ((chance > 0.2) && (chance <= 0.6))
						{
							skill = sk;
						}
						break;
					case L2CubicInstance.SKILL_BUFF_CUBIC_GREAT_HEAL:
						if ((chance > 0.1) && (chance <= 0.2))
						{
							skill = sk;
						}
						break;
					case L2CubicInstance.SKILL_MIND_CUBIC_GREAT_RECHARGE:
						if (chance <= 0.1)
						{
							skill = sk;
						}
						break;
				}
				if (skill != null)
				{
					break;
				}
			}
			
			if (skill != null)
			{
				switch (skill.getId())
				{
					case L2CubicInstance.SKILL_CUBIC_HEAL:
						_cubic.cubicTargetForHeal();
						final L2Character target = _cubic.getTarget();
						if ((target != null) && !target.isDead())
						{
							if ((target.getMaxHp() - target.getCurrentHp()) > skill.getPower())
							{
								skill.activateSkill(_cubic.getOwner(), target);
								_cubic.getOwner().broadcastPacket(new MagicSkillUse(_cubic.getOwner(), target, skill.getId(), skill.getLevel(), 0, 0));
								// The cubic has done an action, increase the current count
								_currentCount.incrementAndGet();
							}
						}
						break;
					case L2CubicInstance.SKILL_MIND_CUBIC_RECHARGE:
					case L2CubicInstance.SKILL_MIND_CUBIC_GREAT_RECHARGE:
						final L2Character owner = _cubic.getOwner();
						if ((owner != null) && !owner.isDead())
						{
							if ((owner.getMaxMp() - owner.getCurrentMp()) > skill.getPower())
							{
								skill.activateSkill(owner, owner);
								owner.broadcastPacket(new MagicSkillUse(owner, owner, skill.getId(), skill.getLevel(), 0, 0));
								// The cubic has done an action, increase the current count
								_currentCount.incrementAndGet();
							}
						}
						break;
					case L2CubicInstance.SKILL_CUBIC_HEALER:
					case L2CubicInstance.SKILL_BUFF_CUBIC_HEAL:
					case L2CubicInstance.SKILL_BUFF_CUBIC_GREAT_HEAL:
						final L2Character _owner = _cubic.getOwner();
						if ((_owner != null) && !_owner.isDead())
						{
							if ((_owner.getMaxHp() - _owner.getCurrentHp()) > skill.getPower())
							{
								skill.activateSkill(_owner, _owner);
								_owner.broadcastPacket(new MagicSkillUse(_owner, _owner, skill.getId(), skill.getLevel(), 0, 0));
								// The cubic has done an action, increase the current count
								_currentCount.incrementAndGet();
							}
						}
						break;
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "", e);
		}
	}
}