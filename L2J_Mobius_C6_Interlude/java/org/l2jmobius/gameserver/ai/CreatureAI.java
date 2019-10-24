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

import static org.l2jmobius.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;
import static org.l2jmobius.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static org.l2jmobius.gameserver.ai.CtrlIntention.AI_INTENTION_CAST;
import static org.l2jmobius.gameserver.ai.CtrlIntention.AI_INTENTION_FOLLOW;
import static org.l2jmobius.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;
import static org.l2jmobius.gameserver.ai.CtrlIntention.AI_INTENTION_INTERACT;
import static org.l2jmobius.gameserver.ai.CtrlIntention.AI_INTENTION_MOVE_TO;
import static org.l2jmobius.gameserver.ai.CtrlIntention.AI_INTENTION_PICK_UP;
import static org.l2jmobius.gameserver.ai.CtrlIntention.AI_INTENTION_REST;

import org.l2jmobius.gameserver.ai.PlayerAI.IntentionCommand;
import org.l2jmobius.gameserver.model.Inventory;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.instance.BoatInstance;
import org.l2jmobius.gameserver.model.actor.instance.DoorInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance.ItemLocation;
import org.l2jmobius.gameserver.model.items.type.WeaponType;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.serverpackets.AutoAttackStop;
import org.l2jmobius.gameserver.taskmanager.AttackStanceTaskManager;

/**
 * This class manages AI of Creature.<BR>
 * <BR>
 * CreatureAI :<BR>
 * <BR>
 * <li>AttackableAI</li>
 * <li>DoorAI</li>
 * <li>PlayerAI</li>
 * <li>SummonAI</li><BR>
 * <BR>
 */
public class CreatureAI extends AbstractAI
{
	/** The skill we are curently casting by INTENTION_CAST */
	private Skill _skill;
	
	@Override
	protected void onEvtAttacked(Creature attacker)
	{
		clientStartAutoAttack();
	}
	
	/**
	 * Constructor of CreatureAI.<BR>
	 * <BR>
	 * @param accessor The AI accessor of the Creature
	 */
	public CreatureAI(Creature.AIAccessor accessor)
	{
		super(accessor);
	}
	
	/**
	 * Manage the Idle Intention : Stop Attack, Movement and Stand Up the actor.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Set the AI Intention to AI_INTENTION_IDLE</li>
	 * <li>Init cast and attack target</li>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Stand up the actor server side AND client side by sending Server->Client packet ChangeWaitType (broadcast)</li><BR>
	 * <BR>
	 */
	@Override
	protected void onIntentionIdle()
	{
		// Set the AI Intention to AI_INTENTION_IDLE
		changeIntention(AI_INTENTION_IDLE, null, null);
		
		// Init cast and attack target
		setCastTarget(null);
		setAttackTarget(null);
		
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
		
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();
	}
	
	/**
	 * Manage the Active Intention : Stop Attack, Movement and Launch Think Event.<BR>
	 * <BR>
	 * <B><U> Actions</U> : <I>if the Intention is not already Active</I></B><BR>
	 * <BR>
	 * <li>Set the AI Intention to AI_INTENTION_ACTIVE</li>
	 * <li>Init cast and attack target</li>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Launch the Think Event</li><BR>
	 * <BR>
	 * @param target
	 */
	protected void onIntentionActive(Creature target)
	{
		if ((target instanceof PlayerInstance) && (_actor instanceof PlayerInstance))
		{
			if ((((PlayerInstance) _actor).getKarma() > 0) && ((_actor.getLevel() - target.getLevel()) >= 10) && ((Playable) target).getProtectionBlessing() && !target.isInsideZone(ZoneId.PVP))
			{
				// If attacker have karma and have level >= 10 than his target and target have Newbie Protection Buff,
				clientActionFailed();
				return;
			}
		}
		
		// Check if the Intention is not already Active
		if (getIntention() != AI_INTENTION_ACTIVE)
		{
			// Set the AI Intention to AI_INTENTION_ACTIVE
			changeIntention(AI_INTENTION_ACTIVE, null, null);
			
			// Init cast and attack target
			setCastTarget(null);
			setAttackTarget(null);
			
			// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
			clientStopMoving(null);
			
			// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
			clientStopAutoAttack();
			
			// Launch the Think Event
			onEvtThink();
		}
	}
	
	/**
	 * Manage the Rest Intention.<BR>
	 * <BR>
	 * <B><U> Actions</U> : </B><BR>
	 * <BR>
	 * <li>Set the AI Intention to AI_INTENTION_IDLE</li><BR>
	 * <BR>
	 */
	@Override
	protected void onIntentionRest()
	{
		// Set the AI Intention to AI_INTENTION_IDLE
		setIntention(AI_INTENTION_IDLE);
	}
	
	/**
	 * Manage the Attack Intention : Stop current Attack (if necessary), Start a new Attack and Launch Think Event.<BR>
	 * <BR>
	 * <B><U> Actions</U> : </B><BR>
	 * <BR>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Set the Intention of this AI to AI_INTENTION_ATTACK</li>
	 * <li>Set or change the AI attack target</li>
	 * <li>Start the actor Auto Attack client side by sending Server->Client packet AutoAttackStart (broadcast)</li>
	 * <li>Launch the Think Event</li><BR>
	 * <BR>
	 * <B><U> Overridden in</U> :</B><BR>
	 * <BR>
	 * <li>AttackableAI : Calculate attack timeout</li><BR>
	 * <BR>
	 */
	@Override
	protected void onIntentionAttack(Creature target)
	{
		if (target == null)
		{
			clientActionFailed();
			return;
		}
		
		if (getIntention() == AI_INTENTION_REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the PlayerInstance actor
			clientActionFailed();
			return;
		}
		
		if (_actor.isAllSkillsDisabled() || _actor.isAfraid())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the PlayerInstance actor
			clientActionFailed();
			return;
		}
		
		// Check if the Intention is already AI_INTENTION_ATTACK
		if (getIntention() == AI_INTENTION_ATTACK)
		{
			// Check if the AI already targets the Creature
			if (getAttackTarget() != target)
			{
				// Set the AI attack target (change target)
				setAttackTarget(target);
				
				stopFollow();
				
				// Launch the Think Event
				notifyEvent(CtrlEvent.EVT_THINK, null);
			}
			else
			{
				clientActionFailed(); // else client freezes until cancel target
			}
		}
		else
		{
			// Set the Intention of this AbstractAI to AI_INTENTION_ATTACK
			changeIntention(AI_INTENTION_ATTACK, target, null);
			
			// Set the AI attack target
			setAttackTarget(target);
			
			stopFollow();
			
			// Launch the Think Event
			notifyEvent(CtrlEvent.EVT_THINK, null);
		}
	}
	
	/**
	 * Manage the Cast Intention : Stop current Attack, Init the AI in order to cast and Launch Think Event.<BR>
	 * <BR>
	 * <B><U> Actions</U> : </B><BR>
	 * <BR>
	 * <li>Set the AI cast target</li>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Cancel action client side by sending Server->Client packet ActionFailed to the PlayerInstance actor</li>
	 * <li>Set the AI skill used by INTENTION_CAST</li>
	 * <li>Set the Intention of this AI to AI_INTENTION_CAST</li>
	 * <li>Launch the Think Event</li><BR>
	 * <BR>
	 */
	@Override
	protected void onIntentionCast(Skill skill, WorldObject target)
	{
		if ((getIntention() == AI_INTENTION_REST) && skill.isMagic())
		{
			clientActionFailed();
			return;
		}
		
		if (_actor.isAllSkillsDisabled() && !skill.isPotion())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the PlayerInstance actor
			clientActionFailed();
			return;
		}
		
		// can't cast if muted
		if (_actor.isMuted() && skill.isMagic())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the PlayerInstance actor
			clientActionFailed();
			return;
		}
		if ((target instanceof PlayerInstance) && (_actor instanceof PlayerInstance))
		{
			if ((((PlayerInstance) _actor).getKarma() > 0) && ((_actor.getLevel() - ((PlayerInstance) target).getLevel()) >= 10) && ((Playable) target).getProtectionBlessing() && !((Creature) target).isInsideZone(ZoneId.PVP))
			{
				// If attacker have karma and have level >= 10 than his target and target have Newbie Protection Buff,
				clientActionFailed();
				return;
			}
		}
		
		// Set the AI cast target
		setCastTarget((Creature) target);
		
		// Stop actions client-side to cast the skill
		if (skill.getHitTime() > 50)
		{
			// Abort the attack of the Creature and send Server->Client ActionFailed packet
			_actor.abortAttack();
			
			// Cancel action client side by sending Server->Client packet ActionFailed to the PlayerInstance actor
			// no need for second ActionFailed packet, abortAttack() already sent it
			// clientActionFailed();
		}
		
		// Set the AI skill used by INTENTION_CAST
		set_skill(skill);
		
		// Change the Intention of this AbstractAI to AI_INTENTION_CAST
		changeIntention(AI_INTENTION_CAST, skill, target);
		
		// Launch the Think Event
		notifyEvent(CtrlEvent.EVT_THINK, null);
	}
	
	/**
	 * Manage the Move To Intention : Stop current Attack and Launch a Move to Location Task.<BR>
	 * <BR>
	 * <B><U> Actions</U> : </B><BR>
	 * <BR>
	 * <li>Stop the actor auto-attack server side AND client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Set the Intention of this AI to AI_INTENTION_MOVE_TO</li>
	 * <li>Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)</li><BR>
	 * <BR>
	 */
	@Override
	protected void onIntentionMoveTo(Location pos)
	{
		if (getIntention() == AI_INTENTION_REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the PlayerInstance actor
			clientActionFailed();
			return;
		}
		
		if ((_actor instanceof PlayerInstance) && (_actor.isAttackingNow() || _actor.isCastingNow()) && !_actor.isMoving())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the PlayerInstance actor
			clientActionFailed();
			return;
		}
		
		// Set the Intention of this AbstractAI to AI_INTENTION_MOVE_TO
		changeIntention(AI_INTENTION_MOVE_TO, pos, null);
		
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();
		
		// Abort the attack of the Creature and send Server->Client ActionFailed packet
		if (_actor instanceof PlayerInstance)
		{
			final ItemInstance rhand = ((PlayerInstance) _actor).getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
			if (((rhand != null) && (rhand.getItemType() == WeaponType.BOW)))
			{
				if (!_actor.isAttackingNow())
				{
					_actor.abortAttack();
				}
			}
			else
			{
				_actor.abortAttack();
			}
		}
		else // case Npc
		{
			_actor.abortAttack();
		}
		
		// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)
		moveTo(pos.getX(), pos.getY(), pos.getZ());
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.l2jmobius.gameserver.ai.AbstractAI#onIntentionMoveToInABoat(org.l2jmobius.gameserver.model.Location, org.l2jmobius.gameserver.model.Location)
	 */
	@Override
	protected void onIntentionMoveToInABoat(Location destination, Location origin)
	{
		if (getIntention() == AI_INTENTION_REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the PlayerInstance actor
			clientActionFailed();
			return;
		}
		
		if (_actor.isAllSkillsDisabled())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the PlayerInstance actor
			clientActionFailed();
			return;
		}
		
		// Set the Intention of this AbstractAI to AI_INTENTION_MOVE_TO
		//
		// changeIntention(AI_INTENTION_MOVE_TO, new Location(((PlayerInstance)_actor).getBoat().getX() - destination.x, ((PlayerInstance)_actor).getBoat().getY() - destination.y, ((PlayerInstance)_actor).getBoat().getZ() - destination.z, 0) , null);
		
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();
		
		// Abort the attack of the Creature and send Server->Client ActionFailed packet
		_actor.abortAttack();
		
		// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)
		moveToInABoat(destination, origin);
	}
	
	/**
	 * Manage the Follow Intention : Stop current Attack and Launch a Follow Task.<BR>
	 * <BR>
	 * <B><U> Actions</U> : </B><BR>
	 * <BR>
	 * <li>Stop the actor auto-attack server side AND client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Set the Intention of this AI to AI_INTENTION_FOLLOW</li>
	 * <li>Create and Launch an AI Follow Task to execute every 1s</li><BR>
	 * <BR>
	 */
	@Override
	protected void onIntentionFollow(Creature target)
	{
		if (getIntention() == AI_INTENTION_REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the PlayerInstance actor
			clientActionFailed();
			return;
		}
		
		if (_actor.isAllSkillsDisabled())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the PlayerInstance actor
			clientActionFailed();
			return;
		}
		
		if (_actor.isImobilised() || _actor.isRooted())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the PlayerInstance actor
			clientActionFailed();
			return;
		}
		
		// Dead actors can`t follow
		if (_actor.isDead())
		{
			clientActionFailed();
			return;
		}
		
		// do not follow yourself
		if (_actor == target)
		{
			clientActionFailed();
			return;
		}
		
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();
		
		// Set the Intention of this AbstractAI to AI_INTENTION_FOLLOW
		changeIntention(AI_INTENTION_FOLLOW, target, null);
		
		// Create and Launch an AI Follow Task to execute every 1s
		startFollow(target);
	}
	
	/**
	 * Manage the PickUp Intention : Set the pick up target and Launch a Move To Pawn Task (offset=20).<BR>
	 * <BR>
	 * <B><U> Actions</U> : </B><BR>
	 * <BR>
	 * <li>Set the AI pick up target</li>
	 * <li>Set the Intention of this AI to AI_INTENTION_PICK_UP</li>
	 * <li>Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)</li><BR>
	 * <BR>
	 */
	@Override
	protected void onIntentionPickUp(WorldObject object)
	{
		if (getIntention() == AI_INTENTION_REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the PlayerInstance actor
			clientActionFailed();
			return;
		}
		
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the PlayerInstance actor
			clientActionFailed();
			return;
		}
		
		if ((object instanceof ItemInstance) && (((ItemInstance) object).getItemLocation() != ItemLocation.VOID))
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the PlayerInstance actor
			clientActionFailed();
			return;
		}
		
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();
		
		// Set the Intention of this AbstractAI to AI_INTENTION_PICK_UP
		changeIntention(AI_INTENTION_PICK_UP, object, null);
		
		// Set the AI pick up target
		setTarget(object);
		
		if ((object.getX() == 0) && (object.getY() == 0)) // TODO: Find the drop&spawn bug
		{
			final Creature creature = getActor();
			if (creature instanceof PlayerInstance)
			{
				clientActionFailed();
				return;
			}
			object.setXYZ(getActor().getX(), getActor().getY(), getActor().getZ() + 5);
		}
		
		// Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
		moveToPawn(object, 20);
	}
	
	/**
	 * Manage the Interact Intention : Set the interact target and Launch a Move To Pawn Task (offset=60).<BR>
	 * <BR>
	 * <B><U> Actions</U> : </B><BR>
	 * <BR>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Set the AI interact target</li>
	 * <li>Set the Intention of this AI to AI_INTENTION_INTERACT</li>
	 * <li>Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)</li><BR>
	 * <BR>
	 */
	@Override
	protected void onIntentionInteract(WorldObject object)
	{
		if (getIntention() == AI_INTENTION_REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the PlayerInstance actor
			clientActionFailed();
			return;
		}
		
		if (_actor.isAllSkillsDisabled())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the PlayerInstance actor
			clientActionFailed();
			return;
		}
		
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();
		
		if (getIntention() != AI_INTENTION_INTERACT)
		{
			// Set the Intention of this AbstractAI to AI_INTENTION_INTERACT
			changeIntention(AI_INTENTION_INTERACT, object, null);
			
			// Set the AI interact target
			setTarget(object);
			
			// Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
			moveToPawn(object, 60);
		}
	}
	
	/**
	 * Do nothing.<BR>
	 * <BR>
	 */
	@Override
	protected void onEvtThink()
	{
		// do nothing
	}
	
	/**
	 * Do nothing.<BR>
	 * <BR>
	 */
	@Override
	protected void onEvtAggression(Creature target, int aggro)
	{
		// do nothing
	}
	
	/**
	 * Launch actions corresponding to the Event Stunned then onAttacked Event.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Break an attack and send Server->Client ActionFailed packet and a System Message to the Creature</li>
	 * <li>Break a cast and send Server->Client ActionFailed packet and a System Message to the Creature</li>
	 * <li>Launch actions corresponding to the Event onAttacked (only for AttackableAI after the stunning periode)</li><BR>
	 * <BR>
	 */
	@Override
	protected void onEvtStunned(Creature attacker)
	{
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		
		if (AttackStanceTaskManager.getInstance().getAttackStanceTask(_actor))
		{
			AttackStanceTaskManager.getInstance().removeAttackStanceTask(_actor);
		}
		
		// Stop Server AutoAttack also
		setAutoAttacking(false);
		
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
		
		// Launch actions corresponding to the Event onAttacked (only for AttackableAI after the stunning periode)
		onEvtAttacked(attacker);
	}
	
	/**
	 * Launch actions corresponding to the Event Sleeping.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Break an attack and send Server->Client ActionFailed packet and a System Message to the Creature</li>
	 * <li>Break a cast and send Server->Client ActionFailed packet and a System Message to the Creature</li><BR>
	 * <BR>
	 */
	@Override
	protected void onEvtSleeping(Creature attacker)
	{
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		
		if (AttackStanceTaskManager.getInstance().getAttackStanceTask(_actor))
		{
			AttackStanceTaskManager.getInstance().removeAttackStanceTask(_actor);
		}
		
		// stop Server AutoAttack also
		setAutoAttacking(false);
		
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
	}
	
	/**
	 * Launch actions corresponding to the Event Rooted.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Launch actions corresponding to the Event onAttacked</li><BR>
	 * <BR>
	 */
	@Override
	protected void onEvtRooted(Creature attacker)
	{
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		// _actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		// if (AttackStanceTaskManager.getInstance().getAttackStanceTask(_actor))
		// AttackStanceTaskManager.getInstance().removeAttackStanceTask(_actor);
		
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
		
		// Launch actions corresponding to the Event onAttacked
		onEvtAttacked(attacker);
	}
	
	/**
	 * Launch actions corresponding to the Event Confused.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Launch actions corresponding to the Event onAttacked</li><BR>
	 * <BR>
	 */
	@Override
	protected void onEvtConfused(Creature attacker)
	{
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
		
		// Launch actions corresponding to the Event onAttacked
		onEvtAttacked(attacker);
	}
	
	/**
	 * Launch actions corresponding to the Event Muted.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Break a cast and send Server->Client ActionFailed packet and a System Message to the Creature</li><BR>
	 * <BR>
	 */
	@Override
	protected void onEvtMuted(Creature attacker)
	{
		// Break a cast and send Server->Client ActionFailed packet and a System Message to the Creature
		onEvtAttacked(attacker);
	}
	
	/**
	 * Launch actions corresponding to the Event ReadyToAct.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Launch actions corresponding to the Event Think</li><BR>
	 * <BR>
	 */
	@Override
	protected void onEvtReadyToAct()
	{
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}
	
	/**
	 * Do nothing.<BR>
	 * <BR>
	 */
	@Override
	protected void onEvtUserCmd(Object arg0, Object arg1)
	{
		// do nothing
	}
	
	/**
	 * Launch actions corresponding to the Event Arrived.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>If the Intention was AI_INTENTION_MOVE_TO, set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>Launch actions corresponding to the Event Think</li><BR>
	 * <BR>
	 */
	@Override
	protected void onEvtArrived()
	{
		// Launch an explore task if necessary
		if (_accessor.getActor() instanceof PlayerInstance)
		{
			((PlayerInstance) _accessor.getActor()).revalidateZone(true);
		}
		else
		{
			_accessor.getActor().revalidateZone();
		}
		
		if (_accessor.getActor().moveToNextRoutePoint())
		{
			return;
		}
		
		clientStoppedMoving();
		
		// If the Intention was AI_INTENTION_MOVE_TO, set the Intention to AI_INTENTION_ACTIVE
		if (getIntention() == AI_INTENTION_MOVE_TO)
		{
			setIntention(AI_INTENTION_ACTIVE);
		}
		
		// Launch actions corresponding to the Event Think
		onEvtThink();
		
		if (_actor instanceof BoatInstance)
		{
			((BoatInstance) _actor).evtArrived();
		}
	}
	
	/**
	 * Launch actions corresponding to the Event ArrivedRevalidate.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Launch actions corresponding to the Event Think</li><BR>
	 * <BR>
	 */
	@Override
	protected void onEvtArrivedRevalidate()
	{
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}
	
	/**
	 * Launch actions corresponding to the Event ArrivedBlocked.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>If the Intention was AI_INTENTION_MOVE_TO, set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>Launch actions corresponding to the Event Think</li><BR>
	 * <BR>
	 */
	@Override
	protected void onEvtArrivedBlocked(Location blocked_at_pos)
	{
		// If the Intention was AI_INTENTION_MOVE_TO, set the Intention to AI_INTENTION_ACTIVE
		if ((getIntention() == AI_INTENTION_MOVE_TO) || (getIntention() == AI_INTENTION_CAST))
		{
			setIntention(AI_INTENTION_ACTIVE);
		}
		
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(blocked_at_pos);
		
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}
	
	/**
	 * Launch actions corresponding to the Event ForgetObject.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>If the object was targeted and the Intention was AI_INTENTION_INTERACT or AI_INTENTION_PICK_UP, set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>If the object was targeted to attack, stop the auto-attack, cancel target and set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>If the object was targeted to cast, cancel target and set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>If the object was targeted to follow, stop the movement, cancel AI Follow Task and set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>If the targeted object was the actor , cancel AI target, stop AI Follow Task, stop the movement and set the Intention to AI_INTENTION_IDLE</li><BR>
	 * <BR>
	 */
	@Override
	protected void onEvtForgetObject(WorldObject object)
	{
		// If the object was targeted and the Intention was AI_INTENTION_INTERACT or AI_INTENTION_PICK_UP, set the Intention to AI_INTENTION_ACTIVE
		if (getTarget() == object)
		{
			setTarget(null);
			
			if (getIntention() == AI_INTENTION_INTERACT)
			{
				setIntention(AI_INTENTION_ACTIVE);
			}
			else if (getIntention() == AI_INTENTION_PICK_UP)
			{
				setIntention(AI_INTENTION_ACTIVE);
			}
		}
		
		// Check if the object was targeted to attack
		if (getAttackTarget() == object)
		{
			// Cancel attack target
			setAttackTarget(null);
			
			// Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);
		}
		
		// Check if the object was targeted to cast
		if (getCastTarget() == object)
		{
			// Cancel cast target
			setCastTarget(null);
			
			// Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);
		}
		
		// Check if the object was targeted to follow
		if (getFollowTarget() == object)
		{
			// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
			clientStopMoving(null);
			
			// Stop an AI Follow Task
			stopFollow();
			
			// Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);
		}
		
		// Check if the targeted object was the actor
		if (_actor == object)
		{
			// Cancel AI target
			setTarget(null);
			setAttackTarget(null);
			setCastTarget(null);
			
			// Stop an AI Follow Task
			stopFollow();
			
			// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
			clientStopMoving(null);
			
			// Set the Intention of this AbstractAI to AI_INTENTION_IDLE
			changeIntention(AI_INTENTION_IDLE, null, null);
		}
	}
	
	/**
	 * Launch actions corresponding to the Event Cancel.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Stop an AI Follow Task</li>
	 * <li>Launch actions corresponding to the Event Think</li><BR>
	 * <BR>
	 */
	@Override
	protected void onEvtCancel()
	{
		// Stop an AI Follow Task
		stopFollow();
		
		if (!AttackStanceTaskManager.getInstance().getAttackStanceTask(_actor))
		{
			_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		}
		
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}
	
	/**
	 * Launch actions corresponding to the Event Dead.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Stop an AI Follow Task</li>
	 * <li>Kill the actor client side by sending Server->Client packet AutoAttackStop, StopMove/StopRotation, Die (broadcast)</li><BR>
	 * <BR>
	 */
	@Override
	protected void onEvtDead()
	{
		// Stop an AI Follow Task
		stopFollow();
		
		// Kill the actor client side by sending Server->Client packet AutoAttackStop, StopMove/StopRotation, Die (broadcast)
		clientNotifyDead();
		
		if (!(_actor instanceof PlayerInstance))
		{
			_actor.setWalking();
		}
	}
	
	/**
	 * Launch actions corresponding to the Event Fake Death.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Stop an AI Follow Task</li>
	 */
	@Override
	protected void onEvtFakeDeath()
	{
		// Stop an AI Follow Task
		stopFollow();
		
		// Stop the actor movement and send Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
		
		// Init AI
		setIntention(AI_INTENTION_IDLE);
		setTarget(null);
		setCastTarget(null);
		setAttackTarget(null);
	}
	
	/**
	 * Do nothing.<BR>
	 * <BR>
	 */
	@Override
	protected void onEvtFinishCasting()
	{
		// do nothing
	}
	
	/**
	 * Manage the Move to Pawn action in function of the distance and of the Interact area.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Get the distance between the current position of the Creature and the target (x,y)</li>
	 * <li>If the distance > offset+20, move the actor (by running) to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)</li>
	 * <li>If the distance <= offset+20, Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>PLayerAI, SummonAI</li><BR>
	 * <BR>
	 * @param target The targeted WorldObject
	 * @param offset The Interact area radius
	 * @return True if a movement must be done
	 */
	protected boolean maybeMoveToPawn(WorldObject target, int offset)
	{
		// Get the distance between the current position of the Creature and the target (x,y)
		if (target == null)
		{
			LOGGER.warning("maybeMoveToPawn: target == NULL!");
			return false;
		}
		
		// skill radius -1
		if (offset < 0)
		{
			return false;
		}
		
		int offsetWithCollision = offset + _actor.getTemplate().collisionRadius;
		if (target instanceof Creature)
		{
			offsetWithCollision += ((Creature) target).getTemplate().collisionRadius;
		}
		
		if (!_actor.isInsideRadius(target, offsetWithCollision, false, false))
		{
			final Creature follow = getFollowTarget();
			
			// Caller should be Playable and thinkAttack/thinkCast/thinkInteract/thinkPickUp
			if (follow != null)
			{
				// prevent attack-follow into peace zones
				if ((getAttackTarget() != null) && (_actor instanceof Playable) && (target instanceof Playable))
				{
					if (getAttackTarget() == follow)
					{
						// allow GMs to keep following
						final boolean isGM = _actor instanceof PlayerInstance ? ((PlayerInstance) _actor).isGM() : false;
						if (Creature.isInsidePeaceZone(_actor, target) && !isGM)
						{
							stopFollow();
							setIntention(AI_INTENTION_IDLE);
							return true;
						}
					}
				}
				// if the target is too far (maybe also teleported)
				if (!_actor.isInsideRadius(target, 2000, false, false))
				{
					stopFollow();
					setIntention(AI_INTENTION_IDLE);
					return true;
				}
				// allow larger hit range when the target is moving (check is run only once per second)
				if (!_actor.isInsideRadius(target, offsetWithCollision + 100, false, false))
				{
					return true;
				}
				
				stopFollow();
				return false;
			}
			
			if (_actor.isMovementDisabled())
			{
				return true;
			}
			
			// If not running, set the Creature movement type to run and send Server->Client packet ChangeMoveType to all others PlayerInstance
			if (!_actor.isRunning() && !(this instanceof PlayerAI))
			{
				_actor.setRunning();
			}
			
			stopFollow();
			
			if ((target instanceof Creature) && !(target instanceof DoorInstance))
			{
				if (((Creature) target).isMoving())
				{
					offset -= 100;
				}
				if (offset < 5)
				{
					offset = 5;
				}
				
				startFollow((Creature) target, offset);
			}
			else
			{
				// Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
				moveToPawn(target, offset);
			}
			return true;
		}
		
		if (getFollowTarget() != null)
		{
			stopFollow();
		}
		
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		// clientStopMoving(null);
		return false;
	}
	
	/**
	 * Modify current Intention and actions if the target is lost or dead.<BR>
	 * <BR>
	 * <B><U> Actions</U> : <I>If the target is lost or dead</I></B><BR>
	 * <BR>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li><BR>
	 * Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation<BR>
	 * (broadcast)</li>
	 * <li>Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE</li><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>PLayerAI, SummonAI</li><BR>
	 * <BR>
	 * @param target The targeted WorldObject
	 * @return True if the target is lost or dead (false if fakedeath)
	 */
	protected boolean checkTargetLostOrDead(Creature target)
	{
		if ((target == null) || target.isAlikeDead())
		{
			// check if player is fakedeath
			if ((target != null) && target.isFakeDeath())
			{
				// target.stopFakeDeath(null);
				return false;
			}
			
			// Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);
			
			return true;
		}
		return false;
	}
	
	/**
	 * Modify current Intention and actions if the target is lost.<BR>
	 * <BR>
	 * <B><U> Actions</U> : <I>If the target is lost</I></B><BR>
	 * <BR>
	 * <li>Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)</li>
	 * <li>Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)</li>
	 * <li>Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE</li><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>PLayerAI, SummonAI</li><BR>
	 * <BR>
	 * @param target The targeted WorldObject
	 * @return True if the target is lost
	 */
	protected boolean checkTargetLost(WorldObject target)
	{
		// check if player is fakedeath
		if (target instanceof PlayerInstance)
		{
			PlayerInstance target2 = (PlayerInstance) target; // convert object to chara
			
			if (target2.isFakeDeath())
			{
				target2.stopFakeDeath(null);
				return false;
			}
		}
		if (target == null)
		{
			// Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);
			
			return true;
		}
		return false;
	}
	
	/**
	 * @see org.l2jmobius.gameserver.ai.AbstractAI#onIntentionActive()
	 */
	@Override
	protected void onIntentionActive()
	{
		// Check if the Intention is not already Active
		if (getIntention() != AI_INTENTION_ACTIVE)
		{
			// Set the AI Intention to AI_INTENTION_ACTIVE
			changeIntention(AI_INTENTION_ACTIVE, null, null);
			
			// Init cast and attack target
			setCastTarget(null);
			setAttackTarget(null);
			
			// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
			clientStopMoving(null);
			
			// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
			clientStopAutoAttack();
			
			// Launch the Think Event
			onEvtThink();
		}
	}
	
	/**
	 * @return the _skill
	 */
	public synchronized Skill get_skill()
	{
		return _skill;
	}
	
	/**
	 * @param _skill the _skill to set
	 */
	public synchronized void set_skill(Skill _skill)
	{
		this._skill = _skill;
	}
	
	public IntentionCommand getNextIntention()
	{
		return null;
	}
	
}
