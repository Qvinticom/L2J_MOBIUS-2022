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
import static org.l2jmobius.gameserver.ai.CtrlIntention.AI_INTENTION_CAST;
import static org.l2jmobius.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;
import static org.l2jmobius.gameserver.ai.CtrlIntention.AI_INTENTION_INTERACT;
import static org.l2jmobius.gameserver.ai.CtrlIntention.AI_INTENTION_PICK_UP;
import static org.l2jmobius.gameserver.ai.CtrlIntention.AI_INTENTION_REST;

import java.util.EmptyStackException;
import java.util.Stack;

import org.l2jmobius.commons.concurrent.ThreadPool;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Creature.AIAccessor;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.actor.instance.StaticObjectInstance;
import org.l2jmobius.gameserver.model.actor.knownlist.WorldObjectKnownList.KnownListAsynchronousUpdateTask;

public class PlayerAI extends CreatureAI
{
	private boolean _thinking; // to prevent recursive thinking
	
	class IntentionCommand
	{
		protected CtrlIntention _crtlIntention;
		protected Object _arg0;
		protected Object _arg1;
		
		protected IntentionCommand(CtrlIntention pIntention, Object pArg0, Object pArg1)
		{
			_crtlIntention = pIntention;
			_arg0 = pArg0;
			_arg1 = pArg1;
		}
	}
	
	private final Stack<IntentionCommand> _interuptedIntentions = new Stack<>();
	
	private synchronized Stack<IntentionCommand> getInterruptedIntentions()
	{
		return _interuptedIntentions;
	}
	
	public PlayerAI(AIAccessor accessor)
	{
		super(accessor);
	}
	
	/**
	 * Saves the current Intention for this PlayerAI if necessary and calls changeIntention in AbstractAI.<BR>
	 * <BR>
	 * @param intention The new Intention to set to the AI
	 * @param arg0 The first parameter of the Intention
	 * @param arg1 The second parameter of the Intention
	 */
	@Override
	public void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		/*
		 * if (Config.DEBUG) LOGGER.warning("PlayerAI: changeIntention -> " + intention + " " + arg0 + " " + arg1);
		 */
		
		// nothing to do if it does not CAST intention
		if (intention != AI_INTENTION_CAST)
		{
			super.changeIntention(intention, arg0, arg1);
			return;
		}
		
		final CtrlIntention _intention = getIntention();
		final Object _intentionArg0 = get_intentionArg0();
		final Object _intentionArg1 = get_intentionArg1();
		
		// do nothing if next intention is same as current one.
		if ((intention == _intention) && (arg0 == _intentionArg0) && (arg1 == _intentionArg1))
		{
			super.changeIntention(intention, arg0, arg1);
			return;
		}
		
		/*
		 * if (Config.DEBUG) LOGGER.warning("PlayerAI: changeIntention -> Saving current intention: " + _intention + " " + _intention_arg0 + " " + _intention_arg1);
		 */
		
		// push current intention to stack
		getInterruptedIntentions().push(new IntentionCommand(_intention, _intentionArg0, _intentionArg1));
		super.changeIntention(intention, arg0, arg1);
	}
	
	/**
	 * Finalize the casting of a skill. This method overrides CreatureAI method.<BR>
	 * <BR>
	 * <B>What it does:</B> Check if actual intention is set to CAST and, if so, retrieves latest intention before the actual CAST and set it as the current intention for the player
	 */
	@Override
	protected void onEvtFinishCasting()
	{
		// forget interupted actions after offensive skill
		final Skill skill = get_skill();
		
		if ((skill != null) && skill.isOffensive())
		{
			getInterruptedIntentions().clear();
		}
		
		if (getIntention() == AI_INTENTION_CAST)
		{
			// run interupted intention if it remain.
			if (!getInterruptedIntentions().isEmpty())
			{
				IntentionCommand cmd = null;
				try
				{
					cmd = getInterruptedIntentions().pop();
				}
				catch (EmptyStackException ese)
				{
				}
				
				/*
				 * if (Config.DEBUG) LOGGER.warning("PlayerAI: onEvtFinishCasting -> " + cmd._intention + " " + cmd._arg0 + " " + cmd._arg1);
				 */
				
				if ((cmd != null) && (cmd._crtlIntention != AI_INTENTION_CAST)) // previous state shouldn't be casting
				{
					setIntention(cmd._crtlIntention, cmd._arg0, cmd._arg1);
				}
				else
				{
					setIntention(AI_INTENTION_IDLE);
				}
			}
			else
			{
				/*
				 * if (Config.DEBUG) LOGGER.warning("PlayerAI: no previous intention set... Setting it to IDLE");
				 */
				// set intention to idle if skill doesn't change intention.
				setIntention(AI_INTENTION_IDLE);
			}
		}
	}
	
	@Override
	protected void onIntentionRest()
	{
		if (getIntention() != AI_INTENTION_REST)
		{
			changeIntention(AI_INTENTION_REST, null, null);
			setTarget(null);
			
			if (getAttackTarget() != null)
			{
				setAttackTarget(null);
			}
			
			clientStopMoving(null);
		}
	}
	
	@Override
	protected void onIntentionActive()
	{
		setIntention(AI_INTENTION_IDLE);
	}
	
	@Override
	protected void clientNotifyDead()
	{
		_clientMovingToPawnOffset = 0;
		_clientMoving = false;
		
		super.clientNotifyDead();
	}
	
	private void thinkAttack()
	{
		final Creature target = getAttackTarget();
		if (target == null)
		{
			return;
		}
		
		if (checkTargetLostOrDead(target))
		{
			// Notify the target
			setAttackTarget(null);
			return;
		}
		
		if (maybeMoveToPawn(target, _actor.getPhysicalAttackRange()))
		{
			return;
		}
		
		_accessor.doAttack(target);
		return;
	}
	
	private void thinkCast()
	{
		final Creature target = getCastTarget();
		final Skill skill = get_skill();
		// if (Config.DEBUG) LOGGER.warning("PlayerAI: thinkCast -> Start");
		
		if (checkTargetLost(target))
		{
			if (skill.isOffensive() && (getAttackTarget() != null))
			{
				// Notify the target
				setCastTarget(null);
			}
			return;
		}
		
		if (target != null)
		{
			if (maybeMoveToPawn(target, _actor.getMagicalAttackRange(skill)))
			{
				return;
			}
		}
		
		if (skill.getHitTime() > 50)
		{
			clientStopMoving(null);
		}
		
		final WorldObject oldTarget = _actor.getTarget();
		
		if (oldTarget != null)
		{
			// Replace the current target by the cast target
			if ((target != null) && (oldTarget != target))
			{
				_actor.setTarget(getCastTarget());
			}
			
			// Launch the Cast of the skill
			_accessor.doCast(get_skill());
			
			// Restore the initial target
			if ((target != null) && (oldTarget != target))
			{
				_actor.setTarget(oldTarget);
			}
		}
		else
		{
			_accessor.doCast(skill);
		}
		
		return;
	}
	
	private void thinkPickUp()
	{
		if (_actor.isAllSkillsDisabled())
		{
			return;
		}
		
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
		((PlayerInstance.AIAccessor) _accessor).doPickupItem(target);
		
		return;
	}
	
	private void thinkInteract()
	{
		if (_actor.isAllSkillsDisabled())
		{
			return;
		}
		
		final WorldObject target = getTarget();
		if (checkTargetLost(target))
		{
			return;
		}
		
		if (maybeMoveToPawn(target, 36))
		{
			return;
		}
		
		if (!(target instanceof StaticObjectInstance))
		{
			((PlayerInstance.AIAccessor) _accessor).doInteract((Creature) target);
		}
		
		setIntention(AI_INTENTION_IDLE);
		return;
	}
	
	@Override
	protected void onEvtThink()
	{
		if (_thinking || _actor.isAllSkillsDisabled())
		{
			return;
		}
		
		_thinking = true;
		try
		{
			if (getIntention() == AI_INTENTION_ATTACK)
			{
				thinkAttack();
			}
			else if (getIntention() == AI_INTENTION_CAST)
			{
				thinkCast();
			}
			else if (getIntention() == AI_INTENTION_PICK_UP)
			{
				thinkPickUp();
			}
			else if (getIntention() == AI_INTENTION_INTERACT)
			{
				thinkInteract();
			}
		}
		finally
		{
			_thinking = false;
		}
	}
	
	@Override
	protected void onEvtArrivedRevalidate()
	{
		ThreadPool.execute(new KnownListAsynchronousUpdateTask(_actor));
		super.onEvtArrivedRevalidate();
	}
	
}
