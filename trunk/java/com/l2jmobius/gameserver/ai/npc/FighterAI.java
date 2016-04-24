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
package com.l2jmobius.gameserver.ai.npc;

import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2QuestGuardInstance;
import com.l2jmobius.gameserver.model.skills.Skill;
import com.l2jmobius.gameserver.util.Util;
import com.l2jmobius.util.Rnd;

/**
 * @author Mobius
 */
public final class FighterAI implements Runnable
{
	private L2PcInstance _player;
	private final L2QuestGuardInstance _guard;
	private int _followRange = 150;
	
	public FighterAI(L2PcInstance player, L2QuestGuardInstance guard)
	{
		_player = player;
		_guard = guard;
		_guard.setIsRunning(true);
		if (_guard.getSpawn() != null)
		{
			_guard.setSpawn(null);
		}
	}
	
	public void setPlayer(L2PcInstance player)
	{
		_player = player;
	}
	
	public void setFollowRange(int range)
	{
		_followRange = range;
	}
	
	@Override
	public void run()
	{
		// Schedule new task only when necessary.
		if ((_guard == null) || _guard.isDead() || (_player == null))
		{
			return;
		}
		ThreadPoolManager.getInstance().scheduleGeneral(new FighterAI(_player, _guard), _guard.isInCombat() ? 1000 : 3000);
		
		// Guard is occupied. Use skills logic.
		if (_guard.isInCombat())
		{
			if ((_guard.getTarget() != null) && _guard.getTarget().isMonster() && ((L2Character) _guard.getTarget()).isAlikeDead())
			{
				for (Skill skill : _guard.getSkills().values())
				{
					if (skill.isBad() && (skill.getCoolTime() <= 0) && !_guard.isCastingNow() && (_guard.calculateDistance(_guard.getTarget(), false, false) < skill.getCastRange()))
					{
						_guard.setHeading(Util.calculateHeadingFrom(_guard, _guard.getTarget()));
						skill.activateSkill(_guard, _guard.getTarget());
						break;
					}
				}
			}
			return; // Guard is occupied, no need to proceed.
		}
		
		// Assist combat logic.
		if (_player.isInCombat() && (_player.getTarget() != null) && _player.getTarget().isMonster() && !_player.getTarget().isInvul() //
			&& ((L2Character) _player.getTarget()).isInCombat() && !((L2Character) _player.getTarget()).isAlikeDead())
		{
			if (_guard.calculateDistance(_player.getTarget(), false, false) > 50)
			{
				_guard.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, _player.getTarget().getLocation());
			}
			else if (_guard.getTarget() != _player.getTarget())
			{
				_guard.addDamageHate((L2Character) _player.getTarget(), 0, 1000);
			}
		}
		
		// Try to kill nearby monsters logic.
		for (L2Character ch : _guard.getKnownList().getKnownCharacters())
		{
			if (ch.isMonster() && !ch.isInvul() && !ch.isAlikeDead())
			{
				_guard.addDamageHate(ch, 0, 1000);
				break;
			}
		}
		
		// Out of combat follow logic.
		if (!_guard.isInCombat())
		{
			_guard.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, (new Location((_player.getLocation().getX() + Rnd.get((_followRange * -1), _followRange)), (_player.getLocation().getY() + Rnd.get((_followRange * -1), _followRange)), _player.getLocation().getZ())));
		}
	}
}
