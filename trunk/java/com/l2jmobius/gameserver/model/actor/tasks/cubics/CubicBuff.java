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
package com.l2jmobius.gameserver.model.actor.tasks.cubics;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.L2Summon;
import com.l2jmobius.gameserver.model.actor.instance.L2CubicInstance;
import com.l2jmobius.gameserver.model.skills.Skill;
import com.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jmobius.gameserver.taskmanager.AttackStanceTaskManager;
import com.l2jmobius.util.Rnd;

/**
 * @author NviX
 */
public final class CubicBuff implements Runnable
{
	private static final Logger _log = Logger.getLogger(CubicBuff.class.getName());
	private final L2CubicInstance _cubic;
	private final AtomicInteger _currentCount = new AtomicInteger();
	private final int _chance;
	
	public CubicBuff(L2CubicInstance cubic, int chance)
	{
		_cubic = cubic;
		_chance = chance;
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
			if (!AttackStanceTaskManager.getInstance().hasAttackStanceTask(_cubic.getOwner()))
			{
				if (!_cubic.getOwner().hasSummon())
				{
					_cubic.stopAction();
					return;
				}
				
				for (L2Summon servitor : _cubic.getOwner().getServitors().values())
				{
					if (!AttackStanceTaskManager.getInstance().hasAttackStanceTask(servitor))
					{
						_cubic.stopAction();
						return;
					}
				}
			}
			
			if (Rnd.get(1, 100) < _chance)
			{
				Skill skill = null;
				for (Skill sk : _cubic.getSkills())
				{
					if ((sk.getId() == L2CubicInstance.SKILL_KNIGHT_CUBIC) || (sk.getId() == L2CubicInstance.SKILL_GUARDIAN_CUBIC))
					{
						skill = sk;
						break;
					}
				}
				
				final L2Character owner = _cubic.getOwner();
				
				if (skill != null)
				{
					skill.activateSkill(owner, owner);
					owner.broadcastPacket(new MagicSkillUse(owner, owner, skill.getId(), skill.getLevel(), 0, 0));
					// The cubic has done an action, increase the current count
					_currentCount.incrementAndGet();
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "", e);
		}
	}
}