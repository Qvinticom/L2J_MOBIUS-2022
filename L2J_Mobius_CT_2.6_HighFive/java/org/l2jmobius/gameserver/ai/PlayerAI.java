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
import static org.l2jmobius.gameserver.ai.CtrlIntention.AI_INTENTION_MOVE_TO;
import static org.l2jmobius.gameserver.ai.CtrlIntention.AI_INTENTION_PICK_UP;
import static org.l2jmobius.gameserver.ai.CtrlIntention.AI_INTENTION_REST;

import org.l2jmobius.gameserver.model.Duel;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.actor.instance.StaticObjectInstance;
import org.l2jmobius.gameserver.model.interfaces.ILocational;
import org.l2jmobius.gameserver.model.skills.Skill;
import org.l2jmobius.gameserver.model.skills.targets.TargetType;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class PlayerAI extends PlayableAI
{
	private boolean _thinking; // to prevent recursive thinking
	
	private IntentionCommand _nextIntention = null;
	
	public PlayerAI(PlayerInstance player)
	{
		super(player);
	}
	
	private void saveNextIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		_nextIntention = new IntentionCommand(intention, arg0, arg1);
	}
	
	@Override
	public IntentionCommand getNextIntention()
	{
		return _nextIntention;
	}
	
	/**
	 * Saves the current Intention for this PlayerAI if necessary and calls changeIntention in AbstractAI.
	 * @param intention The new Intention to set to the AI
	 * @param arg0 The first parameter of the Intention
	 * @param arg1 The second parameter of the Intention
	 */
	@Override
	protected synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		// Forget next if it's not cast or it's cast and skill is toggle.
		if ((intention != AI_INTENTION_CAST) || ((arg0 != null) && !((Skill) arg0).isToggle()))
		{
			_nextIntention = null;
			super.changeIntention(intention, arg0, arg1);
			return;
		}
		
		// do nothing if next intention is same as current one.
		if ((intention == _intention) && (arg0 == _intentionArg0) && (arg1 == _intentionArg1))
		{
			super.changeIntention(intention, arg0, arg1);
			return;
		}
		
		// save current intention so it can be used after cast
		saveNextIntention(_intention, _intentionArg0, _intentionArg1);
		super.changeIntention(intention, arg0, arg1);
	}
	
	/**
	 * Launch actions corresponding to the Event ReadyToAct.<br>
	 * <br>
	 * <b><u>Actions</u>:</b>
	 * <ul>
	 * <li>Launch actions corresponding to the Event Think</li>
	 * </ul>
	 */
	@Override
	protected void onEvtReadyToAct()
	{
		// Launch actions corresponding to the Event Think
		if (_nextIntention != null)
		{
			setIntention(_nextIntention._crtlIntention, _nextIntention._arg0, _nextIntention._arg1);
			_nextIntention = null;
		}
		super.onEvtReadyToAct();
	}
	
	@Override
	protected void onEvtForgetObject(WorldObject object)
	{
		if (object.isPlayer())
		{
			getActor().getKnownRelations().remove(object.getObjectId());
		}
		
		super.onEvtForgetObject(object);
	}
	
	/**
	 * Launch actions corresponding to the Event Cancel.<br>
	 * <br>
	 * <b><u>Actions</u>:</b>
	 * <ul>
	 * <li>Stop an AI Follow Task</li>
	 * <li>Launch actions corresponding to the Event Think</li>
	 * </ul>
	 */
	@Override
	protected void onEvtCancel()
	{
		_nextIntention = null;
		super.onEvtCancel();
	}
	
	/**
	 * Finalize the casting of a skill. This method overrides CreatureAI method.<br>
	 * <b>What it does:</b><br>
	 * Check if actual intention is set to CAST and, if so, retrieves latest intention before the actual CAST and set it as the current intention for the player.
	 */
	@Override
	protected void onEvtFinishCasting()
	{
		if (getIntention() == AI_INTENTION_CAST)
		{
			// run interrupted or next intention
			if (_nextIntention != null)
			{
				if (_nextIntention._crtlIntention != AI_INTENTION_CAST)
				{
					setIntention(_nextIntention._crtlIntention, _nextIntention._arg0, _nextIntention._arg1);
				}
				else
				{
					setIntention(AI_INTENTION_IDLE);
				}
			}
			else
			{
				// set intention to idle if skill doesn't change intention.
				setIntention(AI_INTENTION_IDLE);
			}
		}
	}
	
	@Override
	protected void onIntentionRest()
	{
		if (getIntention() == AI_INTENTION_REST)
		{
			return;
		}
		
		changeIntention(AI_INTENTION_REST, null, null);
		setTarget(null);
		if (getAttackTarget() != null)
		{
			setAttackTarget(null);
		}
		clientStopMoving(null);
	}
	
	@Override
	protected void onIntentionActive()
	{
		setIntention(AI_INTENTION_IDLE);
	}
	
	/**
	 * Manage the Move To Intention : Stop current Attack and Launch a Move to Location Task.<br>
	 * <br>
	 * <b><u>Actions</u> : </b>
	 * <ul>
	 * <li>Stop the actor auto-attack server side AND client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Set the Intention of this AI to AI_INTENTION_MOVE_TO</li>
	 * <li>Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)</li>
	 * </ul>
	 */
	@Override
	protected void onIntentionMoveTo(ILocational loc)
	{
		if (getIntention() == AI_INTENTION_REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the PlayerInstance actor
			clientActionFailed();
			return;
		}
		if (_actor.getActingPlayer().getDuelState() == Duel.DUELSTATE_DEAD)
		{
			clientActionFailed();
			_actor.getActingPlayer().sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_MOVE_WHILE_FROZEN_PLEASE_WAIT));
			return;
		}
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow() || _actor.isAttackingNow())
		{
			clientActionFailed();
			saveNextIntention(AI_INTENTION_MOVE_TO, loc, null);
			return;
		}
		
		// Set the Intention of this AbstractAI to AI_INTENTION_MOVE_TO
		changeIntention(AI_INTENTION_MOVE_TO, loc, null);
		
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();
		
		// Abort the attack of the Creature and send Server->Client ActionFailed packet
		_actor.abortAttack();
		
		// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)
		moveTo(loc.getX(), loc.getY(), loc.getZ());
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
		
		clientStopMoving(null);
		_actor.doAttack(target);
	}
	
	private void thinkCast()
	{
		final Creature target = getCastTarget();
		if ((_skill.getTargetType() == TargetType.GROUND) && _actor.isPlayer())
		{
			if (maybeMoveToPosition(((PlayerInstance) _actor).getCurrentSkillWorldPosition(), _actor.getMagicalAttackRange(_skill)))
			{
				_actor.setCastingNow(false);
				return;
			}
		}
		else
		{
			if (checkTargetLost(target))
			{
				if (_skill.isBad() && (getAttackTarget() != null))
				{
					// Notify the target
					setCastTarget(null);
				}
				_actor.setCastingNow(false);
				return;
			}
			if ((target != null) && maybeMoveToPawn(target, _actor.getMagicalAttackRange(_skill)))
			{
				_actor.setCastingNow(false);
				return;
			}
		}
		
		if ((_skill.getHitTime() > 50) && !_skill.isSimultaneousCast())
		{
			clientStopMoving(null);
		}
		
		// Check if target has changed.
		final WorldObject currentTarget = _actor.getTarget();
		if ((currentTarget != target) && (currentTarget != null) && (target != null))
		{
			_actor.setTarget(target);
			_actor.doCast(_skill);
			_actor.setTarget(currentTarget);
			return;
		}
		
		_actor.doCast(_skill);
	}
	
	private void thinkPickUp()
	{
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			return;
		}
		
		final WorldObject target = getTarget();
		if (checkTargetLost(target) || maybeMoveToPawn(target, 36))
		{
			return;
		}
		
		setIntention(AI_INTENTION_IDLE);
		_actor.getActingPlayer().doPickupItem(target);
	}
	
	private void thinkInteract()
	{
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			return;
		}
		
		final WorldObject target = getTarget();
		if (checkTargetLost(target) || maybeMoveToPawn(target, 36))
		{
			return;
		}
		
		if (!(target instanceof StaticObjectInstance))
		{
			_actor.getActingPlayer().doInteract((Creature) target);
		}
		
		setIntention(AI_INTENTION_IDLE);
	}
	
	@Override
	public void onEvtThink()
	{
		if (_thinking && (getIntention() != AI_INTENTION_CAST))
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
}
