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
package instances.KartiasLabyrinth.AI;

import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.ai.CtrlIntention;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.instance.L2QuestGuardInstance;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.util.Util;
import com.l2jserver.util.Rnd;

/**
 * @author Mobius
 */
public final class Healer implements Runnable
{
	private L2PcInstance _player;
	private final L2QuestGuardInstance _guard;
	private int _followRange = 200;
	
	public Healer(L2PcInstance player, L2QuestGuardInstance guard)
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
		ThreadPoolManager.getInstance().scheduleGeneral(new Healer(_player, _guard), _guard.isInCombat() ? 1000 : 3000);
		
		// Guard is occupied. Use skills logic.
		if (_guard.isInCombat())
		{
			L2PcInstance targetPlayer = null;
			for (L2Character ch : _guard.getKnownList().getKnownCharacters())
			{
				if (ch.isPlayer() && !ch.isAlikeDead())
				{
					targetPlayer = (L2PcInstance) ch;
					break;
				}
			}
			for (Skill skill : _guard.getSkills().values())
			{
				if ((targetPlayer != null) && !targetPlayer.isAlikeDead() //
					&& !skill.isBad() && !_guard.isCastingNow() && (_guard.calculateDistance(targetPlayer, false, false) < skill.getCastRange()))
				{
					_guard.setHeading(Util.calculateHeadingFrom(_guard, targetPlayer));
					_guard.setTarget(targetPlayer);
					skill.activateSkill(_guard, targetPlayer);
					break;
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
			final int moveToLocX = _player.getLocation().getX() + Rnd.get((_followRange * -1), _followRange);
			final int moveToLocY = _player.getLocation().getY() + Rnd.get((_followRange * -1), _followRange);
			final int moveToLocZ = _player.getLocation().getZ();
			final Location moveToLocation = new Location(moveToLocX, moveToLocY, moveToLocZ);
			_guard.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, moveToLocation);
		}
	}
}
