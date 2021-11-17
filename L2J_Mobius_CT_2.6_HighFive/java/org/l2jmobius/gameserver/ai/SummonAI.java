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
package org.l2jmobius.gameserver.ai;

import static org.l2jmobius.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static org.l2jmobius.gameserver.ai.CtrlIntention.AI_INTENTION_FOLLOW;
import static org.l2jmobius.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;

import java.util.concurrent.Future;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.skill.Skill;

public class SummonAI extends PlayableAI implements Runnable
{
	private static final int AVOID_RADIUS = 70;
	
	private volatile boolean _thinking; // to prevent recursive thinking
	private volatile boolean _startFollow = ((Summon) _actor).getFollowStatus();
	private Creature _lastAttack = null;
	
	private volatile boolean _startAvoid = false;
	private Future<?> _avoidTask = null;
	
	public SummonAI(Summon creature)
	{
		super(creature);
	}
	
	@Override
	protected void onIntentionAttack(Creature target)
	{
		if (Config.PATHFINDING && (GeoEngine.getInstance().findPath(_actor.getX(), _actor.getY(), _actor.getZ(), target.getX(), target.getY(), target.getZ(), _actor.getInstanceId()) == null))
		{
			return;
		}
		
		super.onIntentionAttack(target);
	}
	
	@Override
	protected void onIntentionIdle()
	{
		stopFollow();
		_startFollow = false;
		onIntentionActive();
	}
	
	@Override
	protected void onIntentionActive()
	{
		if (_startFollow)
		{
			setIntention(AI_INTENTION_FOLLOW, ((Summon) _actor).getOwner());
		}
		else
		{
			super.onIntentionActive();
		}
	}
	
	@Override
	synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		switch (intention)
		{
			case AI_INTENTION_ACTIVE:
			case AI_INTENTION_FOLLOW:
			{
				startAvoidTask();
				break;
			}
			default:
			{
				stopAvoidTask();
			}
		}
		
		super.changeIntention(intention, arg0, arg1);
	}
	
	private void thinkAttack()
	{
		if (checkTargetLostOrDead(getAttackTarget()))
		{
			setAttackTarget(null);
			return;
		}
		
		if (maybeMoveToPawn(getAttackTarget(), _actor.getPhysicalAttackRange()))
		{
			return;
		}
		
		clientStopMoving(null);
		_actor.doAttack(getAttackTarget());
	}
	
	private void thinkCast()
	{
		if (checkTargetLost(getCastTarget()))
		{
			setCastTarget(null);
			return;
		}
		
		final boolean val = _startFollow;
		if (maybeMoveToPawn(getCastTarget(), _actor.getMagicalAttackRange(_skill)))
		{
			return;
		}
		
		clientStopMoving(null);
		final Summon summon = (Summon) _actor;
		summon.setFollowStatus(false);
		setIntention(AI_INTENTION_IDLE);
		_startFollow = val;
		_actor.doCast(_skill);
	}
	
	private void thinkPickUp()
	{
		if (checkTargetLost(getTarget()) || maybeMoveToPawn(getTarget(), 36))
		{
			return;
		}
		
		setIntention(AI_INTENTION_IDLE);
		((Summon) _actor).doPickupItem(getTarget());
	}
	
	private void thinkInteract()
	{
		if (checkTargetLost(getTarget()) || maybeMoveToPawn(getTarget(), 36))
		{
			return;
		}
		
		setIntention(AI_INTENTION_IDLE);
	}
	
	@Override
	public void onEvtThink()
	{
		if (_thinking || _actor.isCastingNow() || _actor.isAllSkillsDisabled())
		{
			return;
		}
		
		_thinking = true;
		
		try
		{
			switch (getIntention())
			{
				case AI_INTENTION_ATTACK:
				{
					thinkAttack();
					break;
				}
				case AI_INTENTION_CAST:
				{
					thinkCast();
					break;
				}
				case AI_INTENTION_PICK_UP:
				{
					thinkPickUp();
					break;
				}
				case AI_INTENTION_INTERACT:
				{
					thinkInteract();
					break;
				}
			}
		}
		finally
		{
			_thinking = false;
		}
	}
	
	@Override
	protected void onEvtFinishCasting()
	{
		if (_lastAttack == null)
		{
			((Summon) _actor).setFollowStatus(_startFollow);
		}
		else
		{
			setIntention(AI_INTENTION_ATTACK, _lastAttack);
			_lastAttack = null;
		}
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker)
	{
		super.onEvtAttacked(attacker);
		
		avoidAttack(attacker);
	}
	
	@Override
	protected void onEvtEvaded(Creature attacker)
	{
		super.onEvtEvaded(attacker);
		
		avoidAttack(attacker);
	}
	
	private void avoidAttack(Creature attacker)
	{
		// trying to avoid if summon near owner
		if ((((Summon) _actor).getOwner() != null) && (((Summon) _actor).getOwner() != attacker) && ((Summon) _actor).getOwner().isInsideRadius3D(_actor, 2 * AVOID_RADIUS))
		{
			_startAvoid = true;
		}
	}
	
	@Override
	public void run()
	{
		if (!_startAvoid)
		{
			return;
		}
		_startAvoid = false;
		if (_clientMoving || _actor.isDead() || _actor.isMovementDisabled())
		{
			return;
		}
		final int ownerX = ((Summon) _actor).getOwner().getX();
		final int ownerY = ((Summon) _actor).getOwner().getY();
		final double angle = Math.toRadians(Rnd.get(-90, 90)) + Math.atan2(ownerY - _actor.getY(), ownerX - _actor.getX());
		final int targetX = ownerX + (int) (AVOID_RADIUS * Math.cos(angle));
		final int targetY = ownerY + (int) (AVOID_RADIUS * Math.sin(angle));
		if (GeoEngine.getInstance().canMoveToTarget(_actor.getX(), _actor.getY(), _actor.getZ(), targetX, targetY, _actor.getZ(), _actor.getInstanceId()))
		{
			moveTo(targetX, targetY, _actor.getZ());
		}
	}
	
	public void notifyFollowStatusChange()
	{
		_startFollow = !_startFollow;
		switch (getIntention())
		{
			case AI_INTENTION_ACTIVE:
			case AI_INTENTION_FOLLOW:
			case AI_INTENTION_IDLE:
			case AI_INTENTION_MOVE_TO:
			case AI_INTENTION_PICK_UP:
			{
				((Summon) _actor).setFollowStatus(_startFollow);
			}
		}
	}
	
	public void setStartFollowController(boolean value)
	{
		_startFollow = value;
	}
	
	@Override
	protected void onIntentionCast(Skill skill, WorldObject target)
	{
		if (getIntention() == AI_INTENTION_ATTACK)
		{
			_lastAttack = getAttackTarget();
		}
		else
		{
			_lastAttack = null;
		}
		super.onIntentionCast(skill, target);
	}
	
	private void startAvoidTask()
	{
		if (_avoidTask == null)
		{
			_avoidTask = ThreadPool.scheduleAtFixedRate(this, 100, 100);
		}
	}
	
	private void stopAvoidTask()
	{
		if (_avoidTask != null)
		{
			_avoidTask.cancel(false);
			_avoidTask = null;
		}
	}
	
	@Override
	public void stopAITask()
	{
		stopAvoidTask();
		super.stopAITask();
	}
}
