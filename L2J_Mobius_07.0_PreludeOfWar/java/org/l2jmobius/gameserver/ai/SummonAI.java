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

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skills.Skill;
import org.l2jmobius.gameserver.model.skills.SkillCaster;

public class SummonAI extends PlayableAI implements Runnable
{
	private static final int AVOID_RADIUS = 70;
	
	private volatile boolean _thinking; // to prevent recursive thinking
	private volatile boolean _startFollow = ((Summon) _actor).getFollowStatus();
	private Creature _lastAttack = null;
	
	private volatile boolean _startAvoid;
	private volatile boolean _isDefending;
	private Future<?> _avoidTask = null;
	
	// Fix: Infinite Atk. Spd. exploit
	private IntentionCommand _nextIntention = null;
	
	private void saveNextIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		_nextIntention = new IntentionCommand(intention, arg0, arg1);
	}
	
	@Override
	public IntentionCommand getNextIntention()
	{
		return _nextIntention;
	}
	
	public SummonAI(Summon summon)
	{
		super(summon);
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
		final Summon summon = (Summon) _actor;
		if (_startFollow)
		{
			setIntention(AI_INTENTION_FOLLOW, summon.getOwner());
		}
		else
		{
			super.onIntentionActive();
		}
	}
	
	@Override
	synchronized void changeIntention(CtrlIntention intention, Object... args)
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
		
		super.changeIntention(intention, args);
	}
	
	private void thinkAttack()
	{
		final WorldObject target = getTarget();
		final Creature attackTarget = (target != null) && target.isCreature() ? (Creature) target : null;
		if (checkTargetLostOrDead(attackTarget))
		{
			setTarget(null);
			if (_startFollow)
			{
				((Summon) _actor).setFollowStatus(true);
			}
			return;
		}
		
		if (maybeMoveToPawn(attackTarget, _actor.getPhysicalAttackRange()))
		{
			return;
		}
		
		clientStopMoving(null);
		
		// Fix: Infinite Atk. Spd. exploit
		if (_actor.isAttackingNow())
		{
			saveNextIntention(AI_INTENTION_ATTACK, attackTarget, null);
			return;
		}
		
		_actor.doAutoAttack(attackTarget);
	}
	
	private void thinkCast()
	{
		final Summon summon = (Summon) _actor;
		if (summon.isCastingNow(SkillCaster::isAnyNormalType))
		{
			return;
		}
		
		final WorldObject target = getCastTarget();
		if (checkTargetLost(target))
		{
			setTarget(null);
			setCastTarget(null);
			summon.setFollowStatus(true);
			return;
		}
		
		final boolean val = _startFollow;
		if (maybeMoveToPawn(target, _actor.getMagicalAttackRange(_skill)))
		{
			return;
		}
		
		summon.setFollowStatus(false);
		setIntention(AI_INTENTION_IDLE);
		_startFollow = val;
		_actor.doCast(_skill, _item, _skill.isBad(), _dontMove);
	}
	
	private void thinkPickUp()
	{
		final WorldObject target = getTarget();
		if (checkTargetLost(target))
		{
			return;
		}
		
		if (maybeMoveToPawn(target, 36))
		{
			return;
		}
		
		setIntention(AI_INTENTION_IDLE);
		getActor().doPickupItem(target);
	}
	
	private void thinkInteract()
	{
		final WorldObject target = getTarget();
		if (checkTargetLost(target))
		{
			return;
		}
		
		if (maybeMoveToPawn(target, 36))
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
		
		if (_isDefending)
		{
			allServitorsDefend(attacker);
		}
		else
		{
			avoidAttack(attacker);
		}
	}
	
	@Override
	protected void onEvtEvaded(Creature attacker)
	{
		super.onEvtEvaded(attacker);
		
		if (_isDefending)
		{
			allServitorsDefend(attacker);
		}
		else
		{
			avoidAttack(attacker);
		}
	}
	
	private void allServitorsDefend(Creature attacker)
	{
		final Creature owner = getActor().getOwner();
		if ((owner != null) && owner.getActingPlayer().hasServitors())
		{
			for (Summon summon : owner.getActingPlayer().getServitors().values())
			{
				if (((SummonAI) summon.getAI()).isDefending())
				{
					((SummonAI) summon.getAI()).defendAttack(attacker);
				}
			}
		}
		else
		{
			defendAttack(attacker);
		}
	}
	
	private void avoidAttack(Creature attacker)
	{
		// Don't move while casting. It breaks casting animation, but still casts the skill... looks so bugged.
		if (_actor.isCastingNow())
		{
			return;
		}
		
		final Creature owner = getActor().getOwner();
		// trying to avoid if summon near owner
		if ((owner != null) && (owner != attacker) && owner.isInsideRadius3D(_actor, 2 * AVOID_RADIUS))
		{
			_startAvoid = true;
		}
	}
	
	public void defendAttack(Creature attacker)
	{
		// Cannot defend while attacking or casting.
		if (_actor.isAttackingNow() || _actor.isCastingNow())
		{
			return;
		}
		
		final Summon summon = getActor();
		if ((summon.getOwner() != null) && (summon.getOwner() != attacker) && !summon.isMoving() && summon.canAttack(attacker, false))
		{
			summon.doAttack(attacker);
		}
	}
	
	@Override
	public void run()
	{
		if (_startAvoid)
		{
			_startAvoid = false;
			if (!_clientMoving && !_actor.isDead() && !_actor.isMovementDisabled() && (_actor.getMoveSpeed() > 0))
			{
				final int ownerX = ((Summon) _actor).getOwner().getX();
				final int ownerY = ((Summon) _actor).getOwner().getY();
				final double angle = Math.toRadians(Rnd.get(-90, 90)) + Math.atan2(ownerY - _actor.getY(), ownerX - _actor.getX());
				final int targetX = ownerX + (int) (AVOID_RADIUS * Math.cos(angle));
				final int targetY = ownerY + (int) (AVOID_RADIUS * Math.sin(angle));
				if (GeoEngine.getInstance().canMoveToTarget(_actor.getX(), _actor.getY(), _actor.getZ(), targetX, targetY, _actor.getZ(), _actor.getInstanceWorld()))
				{
					moveTo(targetX, targetY, _actor.getZ());
				}
			}
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
	protected void onIntentionCast(Skill skill, WorldObject target, Item item, boolean forceUse, boolean dontMove)
	{
		if (getIntention() == AI_INTENTION_ATTACK)
		{
			_lastAttack = (getTarget() != null) && getTarget().isCreature() ? (Creature) getTarget() : null;
		}
		else
		{
			_lastAttack = null;
		}
		super.onIntentionCast(skill, target, item, forceUse, dontMove);
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
	
	@Override
	public Summon getActor()
	{
		return (Summon) super.getActor();
	}
	
	/**
	 * @return if the summon is defending itself or master.
	 */
	public boolean isDefending()
	{
		return _isDefending;
	}
	
	/**
	 * @param isDefending set the summon to defend itself and master, or be passive and avoid while being attacked.
	 */
	public void setDefending(boolean isDefending)
	{
		_isDefending = isDefending;
	}
}
