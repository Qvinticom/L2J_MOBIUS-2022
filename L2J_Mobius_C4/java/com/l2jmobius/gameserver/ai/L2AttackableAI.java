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
package com.l2jmobius.gameserver.ai;

import static com.l2jmobius.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;
import static com.l2jmobius.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static com.l2jmobius.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;

import java.util.List;
import java.util.concurrent.Future;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.GameTimeController;
import com.l2jmobius.gameserver.Territory;
import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.geodata.GeoData;
import com.l2jmobius.gameserver.instancemanager.DimensionalRiftManager;
import com.l2jmobius.gameserver.model.L2Attackable;
import com.l2jmobius.gameserver.model.L2CharPosition;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.L2Summon;
import com.l2jmobius.gameserver.model.actor.instance.L2ChestInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2FestivalMonsterInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2FolkInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2FriendlyMobInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2GuardInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2MinionInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2RiftInvaderInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2SepulcherMonsterInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.taskmanager.DecayTaskManager;
import com.l2jmobius.gameserver.util.Util;
import com.l2jmobius.util.Rnd;

import javolution.util.FastList;

/**
 * This class manages AI of L2Attackable.<BR>
 * <BR>
 */
public class L2AttackableAI extends L2CharacterAI implements Runnable
{
	// protected static final Logger _log = Logger.getLogger(L2AttackableAI.class.getName());
	
	private static final int RANDOM_WALK_RATE = 30;
	// private static final int MAX_DRIFT_RANGE = 300;
	private static final int MAX_ATTACK_TIMEOUT = 1200; // int ticks, i.e. 2 minutes
	
	/** The L2Attackable AI task executed every 1s (call onEvtThink method) */
	private Future<?> aiTask;
	
	/** The delay after wich the attacked is stopped */
	private int _attackTimeout;
	
	/** The L2Attackable aggro counter */
	private int _globalAggro;
	
	/** The flag used to indicate that a thinking action is in progress */
	private boolean thinking; // to prevent recursive thinking
	
	/** For attack AI, analysis of mob and its targets */
	private final SelfAnalysis _selfAnalysis = new SelfAnalysis();
	private final TargetAnalysis _mostHatedAnalysis = new TargetAnalysis();
	private final TargetAnalysis _secondMostHatedAnalysis = new TargetAnalysis();
	
	/**
	 * Constructor of L2AttackableAI.<BR>
	 * <BR>
	 * @param accessor The AI accessor of the L2Character
	 */
	public L2AttackableAI(L2Character.AIAccessor accessor)
	{
		super(accessor);
		
		_selfAnalysis.Init();
		_attackTimeout = Integer.MAX_VALUE;
		_globalAggro = -10; // 10 seconds timeout of ATTACK after respawn
	}
	
	@Override
	public void run()
	{
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}
	
	/**
	 * Return True if the target is autoattackable (depends on the actor type).<BR>
	 * <BR>
	 * <B><U> Actor is a L2GuardInstance</U> :</B><BR>
	 * <BR>
	 * <li>The target isn't a Folk or a Door</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>The L2PcInstance target has karma (=PK)</li>
	 * <li>The L2MonsterInstance target is aggressive</li><BR>
	 * <BR>
	 * <B><U> Actor is a L2SiegeGuardInstance</U> :</B><BR>
	 * <BR>
	 * <li>The target isn't a Folk or a Door</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>A siege is in progress</li>
	 * <li>The L2PcInstance target isn't a Defender</li><BR>
	 * <BR>
	 * <B><U> Actor is a L2FriendlyMobInstance</U> :</B><BR>
	 * <BR>
	 * <li>The target isn't a Folk, a Door or another L2NpcInstance</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>The L2PcInstance target has karma (=PK)</li><BR>
	 * <BR>
	 * <B><U> Actor is a L2MonsterInstance</U> :</B><BR>
	 * <BR>
	 * <li>The target isn't a Folk, a Door or another L2NpcInstance</li>
	 * <li>The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)</li>
	 * <li>The target is in the actor Aggro range and is at the same height</li>
	 * <li>The actor is Aggressive</li><BR>
	 * <BR>
	 * @param target The targeted L2Object
	 * @return
	 */
	private boolean autoAttackCondition(L2Character target)
	{
		if ((target == null) || !(_actor instanceof L2Attackable))
		{
			return false;
		}
		
		final L2Attackable me = (L2Attackable) _actor;
		
		// Check if target is invulnerable
		if (target.isInvul())
		{
			return false;
		}
		
		// Check if the target isn't a Folk or a Door
		if ((target instanceof L2FolkInstance) || (target instanceof L2DoorInstance))
		{
			return false;
		}
		
		// Check if the target isn't dead, is in the Aggro range and is at the same height
		if (target.isAlikeDead() || !me.isInsideRadius(target, me.getAggroRange(), false, false) || (Math.abs(me.getZ() - target.getZ()) > 400))
		{
			return false;
		}
		
		if (_selfAnalysis.cannotMoveOnLand && !target.isInsideZone(L2Character.ZONE_WATER))
		{
			return false;
		}
		
		if (target instanceof L2PlayableInstance)
		{
			// Check if the AI isn't a Raid Boss/Town guard and the target isn't in silent move mode
			if (!(me.isRaid() || (me instanceof L2GuardInstance)) && ((L2PlayableInstance) target).isSilentMoving())
			{
				return false;
			}
		}
		
		// Check if the target is a L2PcInstance
		if (target instanceof L2PcInstance)
		{
			if (me.getFactionId() != null)
			{
				
				// Check if player is an ally
				if (me.getFactionId().equals("varka_silenos_clan") && ((L2PcInstance) target).isAlliedWithVarka())
				{
					return false;
				}
				
				if (me.getFactionId().equals("ketra_orc_clan") && ((L2PcInstance) target).isAlliedWithKetra())
				{
					return false;
				}
			}
			
			// check if the target is within the grace period for JUST getting up from fake death
			if (((L2PcInstance) target).isRecentFakeDeath())
			{
				return false;
			}
			
			if (target.isInParty() && target.getParty().isInDimensionalRift())
			{
				final byte riftType = target.getParty().getDimensionalRift().getType();
				final byte riftRoom = target.getParty().getDimensionalRift().getCurrentRoom();
				if ((me instanceof L2RiftInvaderInstance) && !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(me.getX(), me.getY(), me.getZ()))
				{
					return false;
				}
			}
		}
		
		// Check if the target is a L2Summon
		if (target instanceof L2Summon)
		{
			final L2PcInstance owner = ((L2Summon) target).getOwner();
			if (owner != null)
			{
				if (me.getFactionId() != null)
				{
					// Check if player is an ally
					if (me.getFactionId().equals("varka_silenos_clan") && owner.isAlliedWithVarka())
					{
						return false;
					}
					
					if (me.getFactionId().equals("ketra_orc_clan") && owner.isAlliedWithKetra())
					{
						return false;
					}
				}
			}
		}
		
		// Check if the actor is a L2GuardInstance
		if (_actor instanceof L2GuardInstance)
		{
			// Check if the L2PcInstance target has karma (=PK)
			if ((target instanceof L2PcInstance) && (((L2PcInstance) target).getKarma() > 0))
			{
				return GeoData.getInstance().canSeeTarget(me, target); // Los Check
			}
			
			// Check if the L2MonsterInstance target is aggressive
			if (target instanceof L2MonsterInstance)
			{
				return (((L2MonsterInstance) target).isAggressive() && GeoData.getInstance().canSeeTarget(me, target));
			}
			
			return false;
		}
		// Check if the actor is a L2FriendlyMobInstance
		else if (_actor instanceof L2FriendlyMobInstance)
		{
			// Check if the target isn't another L2NpcInstance
			if (target instanceof L2NpcInstance)
			{
				return false;
			}
			
			// Check if the L2PcInstance target has karma (=PK)
			if ((target instanceof L2PcInstance) && (((L2PcInstance) target).getKarma() > 0))
			{
				return GeoData.getInstance().canSeeTarget(me, target); // Los Check
			}
			
			return false;
		}
		// The actor is a L2MonsterInstance
		else
		{
			// Check if the target isn't another L2NpcInstance
			if (target instanceof L2NpcInstance)
			{
				return false;
			}
			
			// depending on config, do not allow mobs to attack _new_ players in peacezones,
			// unless they are already following those players from outside the peacezone.
			if (!Config.ALT_MOB_AGGRO_IN_PEACEZONE && target.isInsideZone(L2Character.ZONE_PEACE))
			{
				return false;
			}
			
			if (Config.CHAMPION_ENABLE && me.isChampion() && Config.CHAMPION_PASSIVE)
			{
				return false;
			}
			
			// Check if the actor is Aggressive
			return (me.isAggressive() && GeoData.getInstance().canSeeTarget(me, target));
		}
	}
	
	public void startAITask()
	{
		// If not idle - create an AI task (schedule onEvtThink repeatedly)
		if (aiTask == null)
		{
			aiTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 1000, 1000);
		}
	}
	
	public void stopAITask()
	{
		if (aiTask != null)
		{
			aiTask.cancel(false);
			aiTask = null;
		}
	}
	
	@Override
	protected void onEvtDead()
	{
		stopAITask();
		super.onEvtDead();
	}
	
	/**
	 * Set the Intention of this L2CharacterAI and create an AI Task executed every 1s (call onEvtThink method) for this L2Attackable.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : If actor _knowPlayer isn't EMPTY, AI_INTENTION_IDLE will be change in AI_INTENTION_ACTIVE</B></FONT><BR>
	 * <BR>
	 * @param intention The new Intention to set to the AI
	 * @param arg0 The first parameter of the Intention
	 * @param arg1 The second parameter of the Intention
	 */
	@Override
	synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		if ((intention == AI_INTENTION_IDLE) || (intention == AI_INTENTION_ACTIVE))
		{
			// Check if actor is not dead
			if (!_actor.isAlikeDead())
			{
				final L2Attackable npc = (L2Attackable) _actor;
				
				// If its _knownPlayer isn't empty set the Intention to AI_INTENTION_ACTIVE
				if (npc.getKnownList().getKnownPlayers().size() > 0)
				{
					intention = AI_INTENTION_ACTIVE;
				}
				else
				{
					if (npc.getSpawn() != null)
					{
						if (!npc.isInsideRadius(npc.getSpawn().getLocx(), npc.getSpawn().getLocy(), npc.getSpawn().getLocz(), Config.MAX_DRIFT_RANGE + Config.MAX_DRIFT_RANGE, true, false))
						{
							intention = AI_INTENTION_ACTIVE;
						}
					}
				}
			}
			
			if (intention == AI_INTENTION_IDLE)
			{
				// Set the Intention of this L2AttackableAI to AI_INTENTION_IDLE
				super.changeIntention(AI_INTENTION_IDLE, null, null);
				
				// Stop AI task and detach AI from NPC
				if (aiTask != null)
				{
					aiTask.cancel(true);
					aiTask = null;
				}
				
				// Cancel the AI
				_accessor.detachAI();
				
				return;
			}
		}
		
		// Set the Intention of this L2AttackableAI to intention
		super.changeIntention(intention, arg0, arg1);
		
		// If not idle - create an AI task (schedule onEvtThink repeatedly)
		startAITask();
	}
	
	/**
	 * Manage the Attack Intention : Stop current Attack (if necessary), Calculate attack timeout, Start a new Attack and Launch Think Event.<BR>
	 * <BR>
	 * @param target The L2Character to attack
	 */
	@Override
	protected void onIntentionAttack(L2Character target)
	{
		// Calculate the attack timeout
		_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
		
		// self and buffs
		if ((_selfAnalysis.lastBuffTick + 100) < GameTimeController.getGameTicks())
		{
			for (final L2Skill sk : _selfAnalysis.buffSkills)
			{
				if (_actor.getFirstEffect(sk.getId()) == null)
				{
					if (_actor.getCurrentMp() < sk.getMpConsume())
					{
						continue;
					}
					
					if (_actor.isSkillDisabled(sk.getId()))
					{
						continue;
					}
					
					// no clan buffs here?
					if (sk.getTargetType() == L2Skill.SkillTargetType.TARGET_CLAN)
					{
						continue;
					}
					
					final L2Object OldTarget = _actor.getTarget();
					_actor.setTarget(_actor);
					clientStopMoving(null);
					_accessor.doCast(sk);
					
					// forcing long reuse delay so if cast get interrupted or there would be several buffs, doesn't cast again
					_selfAnalysis.lastBuffTick = GameTimeController.getGameTicks();
					_actor.setTarget(OldTarget);
				}
			}
		}
		
		// Manage the Attack Intention : Stop current Attack (if necessary), Start a new Attack and Launch Think Event
		super.onIntentionAttack(target);
	}
	
	/**
	 * Manage AI standard thinks of a L2Attackable (called by onEvtThink).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Update every 1s the _globalAggro counter to come close to 0</li>
	 * <li>If the actor is Aggressive and can attack, add all autoAttackable L2Character in its Aggro Range to its _aggroList, chose a target and order to attack it</li>
	 * <li>If the actor is a L2GuardInstance that can't attack, order to it to return to its home location</li>
	 * <li>If the actor is a L2MonsterInstance that can't attack, order to it to random walk (1/100)</li><BR>
	 * <BR>
	 */
	private void thinkActive()
	{
		final L2Attackable npc = (L2Attackable) _actor;
		
		// Update every 1s the _globalAggro counter to come close to 0
		if (_globalAggro != 0)
		{
			if (_globalAggro < 0)
			{
				_globalAggro++;
			}
			else
			{
				_globalAggro--;
			}
		}
		
		// Add all autoAttackable L2Character in L2Attackable Aggro Range to its _aggroList with 0 damage and 1 hate
		// A L2Attackable isn't aggressive during 10s after its spawn because _globalAggro is set to -10
		if (_globalAggro >= 0)
		{
			// Get all visible objects inside its Aggro Range
			// L2Object[] objects = L2World.getInstance().getVisibleObjects(_actor, ((L2NpcInstance)_actor).getAggroRange());
			
			// Go through visible objects
			for (final L2Object obj : npc.getKnownList().getKnownObjects().values())
			{
				if (!(obj instanceof L2Character))
				{
					continue;
				}
				
				final L2Character target = (L2Character) obj;
				
				/*
				 * Check to see if this is a festival mob spawn. If it is, then check to see if the aggro trigger is a festival participant...if so, move to attack it.
				 */
				if ((_actor instanceof L2FestivalMonsterInstance) && (obj instanceof L2PcInstance))
				{
					final L2PcInstance targetPlayer = (L2PcInstance) obj;
					
					if (!(targetPlayer.isFestivalParticipant()))
					{
						continue;
					}
				}
				
				// For each L2Character check if the target is autoattackable
				if (autoAttackCondition(target)) // check aggression
				{
					// Get the hate level of the L2Attackable against this L2Character target contained in _aggroList
					final int hating = npc.getHating(target);
					
					// Add the attacker to the L2Attackable _aggroList with 0 damage and 1 hate
					if (hating == 0)
					{
						npc.addDamageHate(target, 0, 1);
					}
				}
			}
			
			// Choose a target from its aggroList
			L2Character hated;
			if (_actor.isConfused())
			{
				hated = getAttackTarget(); // effect handles selection
			}
			else
			{
				hated = npc.getMostHated();
			}
			
			// Order to the L2Attackable to attack the target
			if ((hated != null) && !npc.isCoreAIDisabled())
			{
				// Get the hate level of the L2Attackable against this L2Character target contained in _aggroList
				final int aggro = npc.getHating(hated);
				
				if ((aggro + _globalAggro) > 0)
				{
					// Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
					if (!_actor.isRunning())
					{
						_actor.setRunning();
					}
					
					// Set the AI Intention to AI_INTENTION_ATTACK
					setIntention(CtrlIntention.AI_INTENTION_ATTACK, hated);
				}
				
				return;
			}
		}
		
		// Check if the actor is a L2GuardInstance
		if (_actor instanceof L2GuardInstance)
		{
			// Order to the L2GuardInstance to return to its home location because there's no target to attack
			((L2GuardInstance) _actor).returnHome();
		}
		
		// If this is a festival monster, then it remains in the same location.
		if (_actor instanceof L2FestivalMonsterInstance)
		{
			return;
		}
		
		// Minions following leader
		if ((_actor instanceof L2MinionInstance) && (((L2MinionInstance) _actor).getLeader() != null))
		{
			int offset;
			if (_actor.isRaid())
			{
				offset = 500; // for Raids - need correction
			}
			else
			{
				offset = 200; // for normal minions - need correction :)
			}
			
			if (((L2MinionInstance) _actor).getLeader().isRunning())
			{
				_actor.setRunning();
			}
			else
			{
				_actor.setWalking();
			}
			
			if (_actor.getPlanDistanceSq(((L2MinionInstance) _actor).getLeader()) > (offset * offset))
			{
				int x1, y1, z1;
				x1 = (((L2MinionInstance) _actor).getLeader().getX() + Rnd.nextInt((offset - 30) * 2)) - (offset - 30);
				y1 = (((L2MinionInstance) _actor).getLeader().getY() + Rnd.nextInt((offset - 30) * 2)) - (offset - 30);
				z1 = ((L2MinionInstance) _actor).getLeader().getZ();
				// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)
				moveTo(x1, y1, z1);
				return;
			}
			else if (Rnd.nextInt(RANDOM_WALK_RATE) == 0)
			{
				// self and clan buffs
				for (final L2Skill sk : _selfAnalysis.buffSkills)
				{
					if (_actor.getFirstEffect(sk.getId()) == null)
					{
						// if clan buffs, don't buff every time
						if ((sk.getTargetType() != L2Skill.SkillTargetType.TARGET_SELF) && (Rnd.nextInt(2) != 0))
						{
							continue;
						}
						
						if (_actor.getCurrentMp() < sk.getMpConsume())
						{
							continue;
						}
						
						if (_actor.isSkillDisabled(sk.getId()))
						{
							continue;
						}
						
						final L2Object OldTarget = _actor.getTarget();
						_actor.setTarget(_actor);
						clientStopMoving(null);
						_accessor.doCast(sk);
						_actor.setTarget(OldTarget);
						return;
					}
				}
			}
		}
		// Order to the L2MonsterInstance to random walk (1/100)
		else if ((npc.getSpawn() != null) && (Rnd.nextInt(RANDOM_WALK_RATE) == 0) && !((_actor instanceof L2RaidBossInstance) || (_actor instanceof L2MinionInstance) || (_actor instanceof L2GrandBossInstance) || (_actor instanceof L2ChestInstance) || (_actor instanceof L2GuardInstance) || (_actor instanceof L2SepulcherMonsterInstance) || npc.isQuestMonster()))
		{
			int x1, y1, z1;
			
			final int range = Config.MAX_DRIFT_RANGE;
			
			// self and clan buffs
			for (final L2Skill sk : _selfAnalysis.buffSkills)
			{
				if (_actor.getFirstEffect(sk.getId()) == null)
				{
					// if clan buffs, don't buff every time
					if ((sk.getTargetType() != L2Skill.SkillTargetType.TARGET_SELF) && (Rnd.nextInt(2) != 0))
					{
						continue;
					}
					
					if (_actor.getCurrentMp() < sk.getMpConsume())
					{
						continue;
					}
					
					if (_actor.isSkillDisabled(sk.getId()))
					{
						continue;
					}
					
					final L2Object OldTarget = _actor.getTarget();
					_actor.setTarget(_actor);
					clientStopMoving(null);
					_accessor.doCast(sk);
					_actor.setTarget(OldTarget);
					return;
				}
			}
			
			// If NPC with random coord in territory
			if ((npc.getSpawn().getLocx() == 0) && (npc.getSpawn().getLocy() == 0))
			{
				// Calculate a destination point in the spawn area
				final int p[] = Territory.getInstance().getRandomPoint(npc.getSpawn().getLocation());
				x1 = p[0];
				y1 = p[1];
				z1 = p[2];
				
				// Calculate the distance between the current position of the L2Character and the target (x,y)
				final double distance2 = _actor.getPlanDistanceSq(x1, y1);
				
				if (distance2 > (range * range))
				{
					npc.setIsReturningToSpawnPoint(true);
					final float delay = (float) Math.sqrt(distance2) / range;
					x1 = _actor.getX() + (int) ((x1 - _actor.getX()) / delay);
					y1 = _actor.getY() + (int) ((y1 - _actor.getY()) / delay);
				}
				
				// If NPC with random fixed coord, don't move (unless needs to return to spawnpoint)
				if ((Territory.getInstance().getProcMax(npc.getSpawn().getLocation()) > 0) && !npc.isReturningToSpawnPoint())
				{
					return;
				}
			}
			else
			{
				// If NPC with fixed coord
				x1 = npc.getSpawn().getLocx();
				y1 = npc.getSpawn().getLocy();
				z1 = npc.getSpawn().getLocz();
				
				if (!_actor.isInsideRadius(x1, y1, z1, Config.MAX_DRIFT_RANGE + Config.MAX_DRIFT_RANGE, true, false))
				{
					npc.setIsReturningToSpawnPoint(true);
				}
				else
				{
					// If NPC with fixed coord
					x1 += Rnd.nextInt(range * 2) - range;
					y1 += Rnd.nextInt(range * 2) - range;
					z1 = npc.getZ();
				}
			}
			
			// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)
			moveTo(x1, y1, z1);
		}
	}
	
	/**
	 * Manage AI attack thinks of a L2Attackable (called by onEvtThink).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Update the attack timeout if actor is running</li>
	 * <li>If target is dead or timeout is expired, stop this attack and set the Intention to AI_INTENTION_ACTIVE</li>
	 * <li>Call all L2Object of its Faction inside the Faction Range</li>
	 * <li>Chose a target and order to attack it with magic skill or physical attack</li><BR>
	 * <BR>
	 * TODO: Manage casting rules to healer mobs (like Ant Nurses)
	 */
	private void thinkAttack()
	{
		final L2Character originalAttackTarget = getAttackTarget();
		// Check if target is dead or if timeout is expired to stop this attack
		if ((originalAttackTarget == null) || originalAttackTarget.isAlikeDead() || (_attackTimeout < GameTimeController.getGameTicks()))
		{
			// Stop hating this target after the attack timeout or if target is dead
			if (originalAttackTarget != null)
			{
				final L2Attackable npc = (L2Attackable) _actor;
				npc.stopHating(originalAttackTarget);
			}
			
			// Cancel target and timeout
			_attackTimeout = Integer.MAX_VALUE;
			
			// Set the AI Intention to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);
			
			_actor.setWalking();
			return;
		}
		
		// Handle all L2Object of its Faction inside the Faction Range
		if ((((L2NpcInstance) _actor).getFactionId() != null) && !(originalAttackTarget instanceof L2Attackable))
		{
			final String faction_id = ((L2NpcInstance) _actor).getFactionId();
			
			// Go through all L2Object that belong to its faction
			for (final L2Object obj : _actor.getKnownList().getKnownObjects().values())
			{
				if (obj instanceof L2NpcInstance)
				{
					final L2NpcInstance npc = (L2NpcInstance) obj;
					// if (npc == null)
					// {
					// continue;
					// }
					
					if (!faction_id.equals(npc.getFactionId()))
					{
						continue;
					}
					
					// Check if the L2Object is inside the Faction Range of the actor
					if (_actor.isInsideRadius(npc, (npc.getFactionRange() + npc.getAggroRange()), false, true) && (npc.getAI() != null))
					{
						if ((Math.abs(originalAttackTarget.getZ() - npc.getZ()) < 600) && _actor.getAttackByList().contains(originalAttackTarget) && ((npc.getAI()._intention == CtrlIntention.AI_INTENTION_IDLE) || (npc.getAI()._intention == CtrlIntention.AI_INTENTION_ACTIVE)) && GeoData.getInstance().canSeeTarget(_actor, npc))
						{
							final L2PcInstance player = originalAttackTarget.getActingPlayer();
							if (player != null)
							{
								if (npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_FACTION_CALL) != null)
								{
									for (final Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_FACTION_CALL))
									{
										quest.notifyFactionCall(npc, (L2NpcInstance) _actor, player, (originalAttackTarget instanceof L2Summon));
									}
								}
							}
							
							if ((originalAttackTarget instanceof L2PcInstance) && originalAttackTarget.isInParty() && originalAttackTarget.getParty().isInDimensionalRift())
							{
								final byte riftType = originalAttackTarget.getParty().getDimensionalRift().getType();
								final byte riftRoom = originalAttackTarget.getParty().getDimensionalRift().getCurrentRoom();
								if ((_actor instanceof L2RiftInvaderInstance) && !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(npc.getX(), npc.getY(), npc.getZ()))
								{
									continue;
								}
							}
							
							// Notify the L2Object AI with EVT_AGGRESSION
							npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, originalAttackTarget, 1);
						}
						
						// heal or resurrect friends
						if (_selfAnalysis.hasHealOrResurrect && !_actor.isAttackingDisabled() && (npc.getCurrentHp() < (npc.getMaxHp() * 0.6)) && (_actor.getCurrentHp() > (_actor.getMaxHp() / 2)) && (_actor.getCurrentMp() > (_actor.getMaxMp() / 2)))
						{
							if (npc.isDead() && (_actor instanceof L2MinionInstance))
							{
								if (((L2MinionInstance) _actor).getLeader() == npc)
								{
									for (final L2Skill sk : _selfAnalysis.resurrectSkills)
									{
										if (_actor.getCurrentMp() < sk.getMpConsume())
										{
											continue;
										}
										
										if (_actor.isSkillDisabled(sk.getId()))
										{
											continue;
										}
										
										if (!Util.checkIfInRange(sk.getCastRange(), _actor, npc, true))
										{
											continue;
										}
										
										if (10 >= Rnd.get(100))
										{
											continue;
										}
										
										if (!GeoData.getInstance().canSeeTarget(_actor, npc))
										{
											break;
										}
										
										final L2Object OldTarget = _actor.getTarget();
										_actor.setTarget(npc);
										// would this ever be fast enough for the decay not to run?
										// giving some extra seconds
										DecayTaskManager.getInstance().cancelDecayTask(npc);
										DecayTaskManager.getInstance().addDecayTask(npc);
										clientStopMoving(null);
										_accessor.doCast(sk);
										_actor.setTarget(OldTarget);
										return;
									}
								}
							}
							else if (npc.isInCombat())
							{
								for (final L2Skill sk : _selfAnalysis.healSkills)
								{
									if (_actor.getCurrentMp() < sk.getMpConsume())
									{
										continue;
									}
									
									if (_actor.isSkillDisabled(sk.getId()))
									{
										continue;
									}
									
									if (!Util.checkIfInRange(sk.getCastRange(), _actor, npc, true))
									{
										continue;
									}
									
									int chance = 4;
									if (_actor instanceof L2MinionInstance)
									{
										// minions support boss
										if (((L2MinionInstance) _actor).getLeader() == npc)
										{
											chance = 6;
										}
										else
										{
											chance = 3;
										}
									}
									
									if (npc instanceof L2GrandBossInstance)
									{
										chance = 6;
									}
									
									if (chance >= Rnd.get(100))
									{
										continue;
									}
									
									if (!GeoData.getInstance().canSeeTarget(_actor, npc))
									{
										break;
									}
									
									final L2Object OldTarget = _actor.getTarget();
									_actor.setTarget(npc);
									clientStopMoving(null);
									_accessor.doCast(sk);
									_actor.setTarget(OldTarget);
									return;
								}
							}
						}
					}
				}
			}
		}
		
		if (_actor.isAttackingDisabled())
		{
			return;
		}
		
		// Get 2 most hated chars
		List<L2Character> hated = ((L2Attackable) _actor).get2MostHated();
		if (_actor.isConfused())
		{
			if (hated != null)
			{
				hated.set(0, originalAttackTarget); // effect handles selection
			}
			else
			{
				hated = new FastList<>();
				hated.add(originalAttackTarget);
				hated.add(null);
			}
		}
		
		if ((hated == null) || (hated.get(0) == null))
		{
			setIntention(AI_INTENTION_ACTIVE);
			return;
		}
		
		if (hated.get(0) != originalAttackTarget)
		{
			setAttackTarget(hated.get(0));
		}
		
		_mostHatedAnalysis.Update(hated.get(0));
		_secondMostHatedAnalysis.Update(hated.get(1));
		
		// Get all information needed to choose between physical or magical attack
		_actor.setTarget(_mostHatedAnalysis.character);
		final double dist2 = _actor.getPlanDistanceSq(_mostHatedAnalysis.character.getX(), _mostHatedAnalysis.character.getY());
		final int combinedCollision = (int) (_actor.getTemplate().collisionRadius + _mostHatedAnalysis.character.getTemplate().collisionRadius);
		int range = _actor.getPhysicalAttackRange() + combinedCollision;
		
		// Reconsider target if _actor hasn't got hits in for last 14 sec
		if (!_actor.isMuted() && ((_attackTimeout - 160) < GameTimeController.getGameTicks()) && (_secondMostHatedAnalysis.character != null))
		{
			if (Util.checkIfInRange(900, _actor, hated.get(1), true))
			{
				// take off 2* the amount the aggro is larger than second most
				final int aggro = 2 * (((L2Attackable) _actor).getHating(hated.get(0)) - ((L2Attackable) _actor).getHating(hated.get(1)));
				onEvtAggression(hated.get(0), -aggro);
				// Calculate a new attack timeout
				_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
			}
		}
		
		// Reconsider target during next round if actor is rooted and cannot reach mostHated but can
		// reach secondMostHated
		if (_actor.isRooted() && (_secondMostHatedAnalysis.character != null))
		{
			if (_selfAnalysis.isMage && (dist2 > (_selfAnalysis.maxCastRange * _selfAnalysis.maxCastRange)) && (_actor.getPlanDistanceSq(_secondMostHatedAnalysis.character.getX(), _secondMostHatedAnalysis.character.getY()) < (_selfAnalysis.maxCastRange * _selfAnalysis.maxCastRange)))
			{
				final int aggro = 1 + (((L2Attackable) _actor).getHating(hated.get(0)) - ((L2Attackable) _actor).getHating(hated.get(1)));
				onEvtAggression(hated.get(0), -aggro);
			}
			else if ((dist2 > (range * range)) && (_actor.getPlanDistanceSq(_secondMostHatedAnalysis.character.getX(), _secondMostHatedAnalysis.character.getY()) < (range * range)))
			{
				final int aggro = 1 + (((L2Attackable) _actor).getHating(hated.get(0)) - ((L2Attackable) _actor).getHating(hated.get(1)));
				onEvtAggression(hated.get(0), -aggro);
			}
		}
		
		// Considering, if bigger range will be attempted
		if ((dist2 < (10000 + (combinedCollision * combinedCollision))) && !_selfAnalysis.isFighter && !_selfAnalysis.isBalanced && (_selfAnalysis.hasLongRangeSkills || _selfAnalysis.isArcher) && (_mostHatedAnalysis.isBalanced || _mostHatedAnalysis.isFighter) && (_mostHatedAnalysis.character.isRooted() || _mostHatedAnalysis.isSlower) && ((Config.PATHFINDING == 2 ? 20 : 12) >= Rnd.get(100))) // chance
		{
			int posX = _actor.getX();
			int posY = _actor.getY();
			final int posZ = _actor.getZ();
			final double distance = Math.sqrt(dist2); // This way, we only do the sqrt if we need it
			
			int signx = -1;
			int signy = -1;
			if (_actor.getX() > _mostHatedAnalysis.character.getX())
			{
				signx = 1;
			}
			if (_actor.getY() > _mostHatedAnalysis.character.getY())
			{
				signy = 1;
			}
			posX += Math.round((float) ((signx * ((range / 2) + (Rnd.get(range)))) - distance));
			posY += Math.round((float) ((signy * ((range / 2) + (Rnd.get(range)))) - distance));
			setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(posX, posY, posZ, 0));
			return;
		}
		
		// Cannot see target, needs to go closer, currently just goes to range 300 if mage
		if ((dist2 > ((310 * 310) + (combinedCollision * combinedCollision))) && _selfAnalysis.hasLongRangeSkills && !GeoData.getInstance().canSeeTarget(_actor, _mostHatedAnalysis.character))
		{
			if (!(_selfAnalysis.isMage && _actor.isMuted()))
			{
				moveToPawn(_mostHatedAnalysis.character, 300);
				return;
			}
		}
		
		if (_mostHatedAnalysis.character.isMoving())
		{
			range += 50;
		}
		
		// Check if the actor is far from target
		if (dist2 > (range * range))
		{
			if (!_actor.isMuted() && (_selfAnalysis.hasLongRangeSkills || !_selfAnalysis.healSkills.isEmpty()))
			{
				// check for long ranged skills and heal/buff skills
				if (!_mostHatedAnalysis.isCanceled)
				{
					for (final L2Skill sk : _selfAnalysis.cancelSkills)
					{
						final int castRange = sk.getCastRange() + combinedCollision;
						if (_actor.isSkillDisabled(sk.getId()) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (dist2 > (castRange * castRange)))
						{
							continue;
						}
						
						if (Rnd.nextInt(100) <= 8)
						{
							clientStopMoving(null);
							_accessor.doCast(sk);
							_mostHatedAnalysis.isCanceled = true;
							_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
							return;
						}
					}
				}
				
				if ((_selfAnalysis.lastDebuffTick + 60) < GameTimeController.getGameTicks())
				{
					for (final L2Skill sk : _selfAnalysis.debuffSkills)
					{
						final int castRange = sk.getCastRange() + combinedCollision;
						if (_actor.isSkillDisabled(sk.getId()) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (dist2 > (castRange * castRange)))
						{
							continue;
						}
						
						int chance = 8;
						if (_selfAnalysis.isFighter && _mostHatedAnalysis.isMage)
						{
							chance = 3;
						}
						if (_selfAnalysis.isFighter && _mostHatedAnalysis.isArcher)
						{
							chance = 12;
						}
						if (_selfAnalysis.isMage && !_mostHatedAnalysis.isMage)
						{
							chance = 10;
						}
						if (_mostHatedAnalysis.isMagicResistant)
						{
							chance /= 2;
						}
						
						if (Rnd.nextInt(100) <= chance)
						{
							clientStopMoving(null);
							_accessor.doCast(sk);
							_selfAnalysis.lastDebuffTick = GameTimeController.getGameTicks();
							_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
							return;
						}
					}
				}
				
				if (!_mostHatedAnalysis.character.isMuted())
				{
					int chance = 8;
					if (!(_mostHatedAnalysis.isMage || _mostHatedAnalysis.isBalanced))
					{
						chance = 3;
					}
					
					for (final L2Skill sk : _selfAnalysis.muteSkills)
					{
						final int castRange = sk.getCastRange() + combinedCollision;
						if (_actor.isSkillDisabled(sk.getId()) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (dist2 > (castRange * castRange)))
						{
							continue;
						}
						
						if (Rnd.nextInt(100) <= chance)
						{
							clientStopMoving(null);
							_accessor.doCast(sk);
							_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
							return;
						}
					}
				}
				
				if ((_secondMostHatedAnalysis.character != null) && !_secondMostHatedAnalysis.character.isMuted() && (_secondMostHatedAnalysis.isMage || _secondMostHatedAnalysis.isBalanced))
				{
					final double secondHatedDist2 = _actor.getPlanDistanceSq(_secondMostHatedAnalysis.character.getX(), _secondMostHatedAnalysis.character.getY());
					for (final L2Skill sk : _selfAnalysis.muteSkills)
					{
						final int castRange = sk.getCastRange() + combinedCollision;
						if (_actor.isSkillDisabled(sk.getId()) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (secondHatedDist2 > (castRange * castRange)))
						{
							continue;
						}
						
						if (Rnd.nextInt(100) <= 2)
						{
							_actor.setTarget(_secondMostHatedAnalysis.character);
							clientStopMoving(null);
							_accessor.doCast(sk);
							_actor.setTarget(_mostHatedAnalysis.character);
							return;
						}
					}
				}
				
				if (!_mostHatedAnalysis.character.isSleeping())
				{
					for (final L2Skill sk : _selfAnalysis.sleepSkills)
					{
						final int castRange = sk.getCastRange() + combinedCollision;
						if (_actor.isSkillDisabled(sk.getId()) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (dist2 > (castRange * castRange)))
						{
							continue;
						}
						
						if (Rnd.nextInt(100) <= 1)
						{
							clientStopMoving(null);
							_accessor.doCast(sk);
							_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
							return;
						}
					}
				}
				
				if ((_secondMostHatedAnalysis.character != null) && !_secondMostHatedAnalysis.character.isSleeping())
				{
					final double secondHatedDist2 = _actor.getPlanDistanceSq(_secondMostHatedAnalysis.character.getX(), _secondMostHatedAnalysis.character.getY());
					for (final L2Skill sk : _selfAnalysis.sleepSkills)
					{
						final int castRange = sk.getCastRange() + combinedCollision;
						if (_actor.isSkillDisabled(sk.getId()) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (secondHatedDist2 > (castRange * castRange)))
						{
							continue;
						}
						
						if (Rnd.nextInt(100) <= 3)
						{
							_actor.setTarget(_secondMostHatedAnalysis.character);
							clientStopMoving(null);
							_accessor.doCast(sk);
							_actor.setTarget(_mostHatedAnalysis.character);
							return;
						}
					}
				}
				
				if (!_mostHatedAnalysis.character.isRooted())
				{
					for (final L2Skill sk : _selfAnalysis.rootSkills)
					{
						final int castRange = sk.getCastRange() + combinedCollision;
						if (_actor.isSkillDisabled(sk.getId()) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (dist2 > (castRange * castRange)))
						{
							continue;
						}
						
						if (Rnd.nextInt(100) <= (_mostHatedAnalysis.isSlower ? 3 : 8))
						{
							clientStopMoving(null);
							_accessor.doCast(sk);
							_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
							return;
						}
					}
				}
				
				if (!_mostHatedAnalysis.character.isAttackingDisabled())
				{
					for (final L2Skill sk : _selfAnalysis.generalDisablers)
					{
						final int castRange = sk.getCastRange() + combinedCollision;
						if (_actor.isSkillDisabled(sk.getId()) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (dist2 > (castRange * castRange)))
						{
							continue;
						}
						
						if (Rnd.nextInt(100) <= ((_selfAnalysis.isFighter && _actor.isRooted()) ? 15 : 7))
						{
							clientStopMoving(null);
							_accessor.doCast(sk);
							_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
							return;
						}
					}
				}
				
				if (_actor.getCurrentHp() < (_actor.getMaxHp() * 0.4))
				{
					for (final L2Skill sk : _selfAnalysis.healSkills)
					{
						if (_actor.isSkillDisabled(sk.getId()) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)))
						{
							continue;
						}
						
						int chance = 7;
						if (_mostHatedAnalysis.character.isAttackingDisabled())
						{
							chance += 10;
						}
						
						if ((_secondMostHatedAnalysis.character == null) || _secondMostHatedAnalysis.character.isAttackingDisabled())
						{
							chance += 10;
						}
						
						if (Rnd.nextInt(100) <= chance)
						{
							_actor.setTarget(_actor);
							clientStopMoving(null);
							_accessor.doCast(sk);
							_actor.setTarget(_mostHatedAnalysis.character);
							return;
						}
					}
				}
				
				// chance decision for launching long range skills
				int castingChance = 5;
				if (_selfAnalysis.isMage)
				{
					castingChance = 50; // mages
				}
				
				if (_selfAnalysis.isBalanced)
				{
					if (!_mostHatedAnalysis.isFighter)
					{
						castingChance = 15;
					}
					else
					{
						castingChance = 25; // stay away from fighters
					}
				}
				
				if (_selfAnalysis.isFighter)
				{
					if (_mostHatedAnalysis.isMage)
					{
						castingChance = 3;
					}
					else
					{
						castingChance = 7;
					}
					
					if (_actor.isRooted())
					{
						castingChance = 20; // doesn't matter if no success first round
					}
				}
				
				for (final L2Skill sk : _selfAnalysis.generalSkills)
				{
					final int castRange = sk.getCastRange() + combinedCollision;
					if (_actor.isSkillDisabled(sk.getId()) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (dist2 > (castRange * castRange)))
					{
						continue;
					}
					
					if (Rnd.nextInt(100) <= castingChance)
					{
						clientStopMoving(null);
						_accessor.doCast(sk);
						_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
						return;
					}
				}
			}
			
			// Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
			if (_selfAnalysis.isMage)
			{
				if (_actor.isMuted())
				{
					return;
				}
				range = _selfAnalysis.maxCastRange;
			}
			
			if (_mostHatedAnalysis.character.isMoving())
			{
				range -= 100;
			}
			if (range < 5)
			{
				range = 5;
			}
			moveToPawn(_mostHatedAnalysis.character, range);
			return;
		}
		// **************************************************
		// Else, if this is close enough for physical attacks
		// In case many mobs are trying to hit from same place, move a bit,
		// circling around the target
		if (Rnd.nextInt(100) <= 33) // check it once per 3 seconds
		{
			for (final L2Object nearby : _actor.getKnownList().getKnownCharactersInRadius(20))
			{
				if ((nearby instanceof L2Attackable) && (nearby != _mostHatedAnalysis.character))
				{
					int diffx = Rnd.get(combinedCollision, combinedCollision + 40);
					if (Rnd.get(10) < 5)
					{
						diffx = -diffx;
					}
					
					int diffy = Rnd.get(combinedCollision, combinedCollision + 40);
					if (Rnd.get(10) < 5)
					{
						diffy = -diffy;
					}
					
					moveTo(_mostHatedAnalysis.character.getX() + diffx, _mostHatedAnalysis.character.getY() + diffy, _mostHatedAnalysis.character.getZ());
					return;
				}
			}
		}
		
		// Calculate a new attack timeout.
		_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
		
		// check for close combat skills && heal/buff skills
		if (!_mostHatedAnalysis.isCanceled)
		{
			for (final L2Skill sk : _selfAnalysis.cancelSkills)
			{
				if ((_actor.isMuted() && sk.isMagic()) || (_actor.isPhysicalMuted() && !sk.isMagic()))
				{
					continue;
				}
				
				final int castRange = sk.getCastRange() + combinedCollision;
				if (_actor.isSkillDisabled(sk.getId()) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (dist2 > (castRange * castRange)))
				{
					continue;
				}
				
				if (Rnd.nextInt(100) <= 8)
				{
					clientStopMoving(null);
					_accessor.doCast(sk);
					_mostHatedAnalysis.isCanceled = true;
					return;
				}
			}
		}
		
		if ((_selfAnalysis.lastDebuffTick + 60) < GameTimeController.getGameTicks())
		{
			for (final L2Skill sk : _selfAnalysis.debuffSkills)
			{
				if ((_actor.isMuted() && sk.isMagic()) || (_actor.isPhysicalMuted() && !sk.isMagic()))
				{
					continue;
				}
				
				final int castRange = sk.getCastRange() + combinedCollision;
				if (_actor.isSkillDisabled(sk.getId()) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (dist2 > (castRange * castRange)))
				{
					continue;
				}
				
				int chance = 5;
				if (_selfAnalysis.isFighter && _mostHatedAnalysis.isMage)
				{
					chance = 3;
				}
				if (_selfAnalysis.isFighter && _mostHatedAnalysis.isArcher)
				{
					chance = 3;
				}
				if (_selfAnalysis.isMage && !_mostHatedAnalysis.isMage)
				{
					chance = 4;
				}
				if (_mostHatedAnalysis.isMagicResistant)
				{
					chance /= 2;
				}
				if (sk.getCastRange() < 200)
				{
					chance += 3;
				}
				
				if (Rnd.nextInt(100) <= chance)
				{
					clientStopMoving(null);
					_accessor.doCast(sk);
					_selfAnalysis.lastDebuffTick = GameTimeController.getGameTicks();
					return;
				}
			}
		}
		
		if (!_mostHatedAnalysis.character.isMuted() && (_mostHatedAnalysis.isMage || _mostHatedAnalysis.isBalanced))
		{
			for (final L2Skill sk : _selfAnalysis.muteSkills)
			{
				if ((_actor.isMuted() && sk.isMagic()) || (_actor.isPhysicalMuted() && !sk.isMagic()))
				{
					continue;
				}
				
				final int castRange = sk.getCastRange() + combinedCollision;
				if (_actor.isSkillDisabled(sk.getId()) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (dist2 > (castRange * castRange)))
				{
					continue;
				}
				
				if (Rnd.nextInt(100) <= 7)
				{
					clientStopMoving(null);
					_accessor.doCast(sk);
					return;
				}
			}
		}
		
		if ((_secondMostHatedAnalysis.character != null) && !_secondMostHatedAnalysis.character.isMuted() && (_secondMostHatedAnalysis.isMage || _secondMostHatedAnalysis.isBalanced))
		{
			final double secondHatedDist2 = _actor.getPlanDistanceSq(_secondMostHatedAnalysis.character.getX(), _secondMostHatedAnalysis.character.getY());
			for (final L2Skill sk : _selfAnalysis.muteSkills)
			{
				if ((_actor.isMuted() && sk.isMagic()) || (_actor.isPhysicalMuted() && !sk.isMagic()))
				{
					continue;
				}
				
				final int castRange = sk.getCastRange() + combinedCollision;
				if (_actor.isSkillDisabled(sk.getId()) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (secondHatedDist2 > (castRange * castRange)))
				{
					continue;
				}
				
				if (Rnd.nextInt(100) <= 3)
				{
					_actor.setTarget(_secondMostHatedAnalysis.character);
					clientStopMoving(null);
					_accessor.doCast(sk);
					_actor.setTarget(_mostHatedAnalysis.character);
					return;
				}
			}
		}
		
		if ((_secondMostHatedAnalysis.character != null) && !_secondMostHatedAnalysis.character.isSleeping())
		{
			final double secondHatedDist2 = _actor.getPlanDistanceSq(_secondMostHatedAnalysis.character.getX(), _secondMostHatedAnalysis.character.getY());
			for (final L2Skill sk : _selfAnalysis.sleepSkills)
			{
				if ((_actor.isMuted() && sk.isMagic()) || (_actor.isPhysicalMuted() && !sk.isMagic()))
				{
					continue;
				}
				
				final int castRange = sk.getCastRange() + combinedCollision;
				if (_actor.isSkillDisabled(sk.getId()) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (secondHatedDist2 > (castRange * castRange)))
				{
					continue;
				}
				
				if (Rnd.nextInt(100) <= 4)
				{
					_actor.setTarget(_secondMostHatedAnalysis.character);
					clientStopMoving(null);
					_accessor.doCast(sk);
					_actor.setTarget(_mostHatedAnalysis.character);
					return;
				}
			}
		}
		
		if (!_mostHatedAnalysis.character.isRooted() && _mostHatedAnalysis.isFighter && !_selfAnalysis.isFighter)
		{
			for (final L2Skill sk : _selfAnalysis.rootSkills)
			{
				if ((_actor.isMuted() && sk.isMagic()) || (_actor.isPhysicalMuted() && !sk.isMagic()))
				{
					continue;
				}
				
				final int castRange = sk.getCastRange() + combinedCollision;
				if (_actor.isSkillDisabled(sk.getId()) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (dist2 > (castRange * castRange)))
				{
					continue;
				}
				
				if (Rnd.nextInt(100) <= 4)
				{
					clientStopMoving(null);
					_accessor.doCast(sk);
					return;
				}
			}
		}
		
		if (!_mostHatedAnalysis.character.isAttackingDisabled())
		{
			for (final L2Skill sk : _selfAnalysis.generalDisablers)
			{
				if ((_actor.isMuted() && sk.isMagic()) || (_actor.isPhysicalMuted() && !sk.isMagic()))
				{
					continue;
				}
				
				final int castRange = sk.getCastRange() + combinedCollision;
				if (_actor.isSkillDisabled(sk.getId()) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (dist2 > (castRange * castRange)))
				{
					continue;
				}
				
				if (Rnd.nextInt(100) <= ((sk.getCastRange() < 200) ? 10 : 7))
				{
					clientStopMoving(null);
					_accessor.doCast(sk);
					return;
				}
			}
		}
		
		if (_actor.getCurrentHp() < (_actor.getMaxHp() * 0.4))
		{
			for (final L2Skill sk : _selfAnalysis.healSkills)
			{
				if ((_actor.isMuted() && sk.isMagic()) || (_actor.isPhysicalMuted() && !sk.isMagic()))
				{
					continue;
				}
				
				if (_actor.isSkillDisabled(sk.getId()) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)))
				{
					continue;
				}
				
				int chance = 7;
				if (_mostHatedAnalysis.character.isAttackingDisabled())
				{
					chance += 10;
				}
				if ((_secondMostHatedAnalysis.character == null) || _secondMostHatedAnalysis.character.isAttackingDisabled())
				{
					chance += 10;
				}
				
				if (Rnd.nextInt(100) <= chance)
				{
					_actor.setTarget(_actor);
					clientStopMoving(null);
					_accessor.doCast(sk);
					_actor.setTarget(_mostHatedAnalysis.character);
					return;
				}
			}
		}
		
		for (final L2Skill sk : _selfAnalysis.generalSkills)
		{
			if ((_actor.isMuted() && sk.isMagic()) || (_actor.isPhysicalMuted() && !sk.isMagic()))
			{
				continue;
			}
			
			final int castRange = sk.getCastRange() + combinedCollision;
			if (_actor.isSkillDisabled(sk.getId()) || (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) || (dist2 > (castRange * castRange)))
			{
				continue;
			}
			
			// chance decision for launching general skills in melee fight
			// close range skills should be higher, long range lower
			int castingChance = 5;
			if (_selfAnalysis.isMage)
			{
				if (sk.getCastRange() < 200)
				{
					castingChance = 35;
				}
				else
				{
					castingChance = 25; // mages
				}
			}
			
			if (_selfAnalysis.isBalanced)
			{
				if (sk.getCastRange() < 200)
				{
					castingChance = 12;
				}
				else
				{
					if (_mostHatedAnalysis.isMage)
					{
						castingChance = 2;
					}
					else
					{
						castingChance = 5;
					}
				}
			}
			
			if (_selfAnalysis.isFighter)
			{
				if (sk.getCastRange() < 200)
				{
					castingChance = 12;
				}
				else
				{
					if (_mostHatedAnalysis.isMage)
					{
						castingChance = 1;
					}
					else
					{
						castingChance = 3;
					}
				}
			}
			
			if (Rnd.nextInt(100) <= castingChance)
			{
				clientStopMoving(null);
				_accessor.doCast(sk);
				return;
			}
		}
		
		// Finally, physical attacks
		clientStopMoving(null);
		_accessor.doAttack(getAttackTarget());
	}
	
	/**
	 * Manage AI thinking actions of a L2Attackable.<BR>
	 * <BR>
	 */
	@Override
	protected void onEvtThink()
	{
		// Check if the actor can't use skills and if a thinking action isn't already in progress
		if (thinking || _actor.isAllSkillsDisabled())
		{
			return;
		}
		
		// Start thinking action
		thinking = true;
		
		try
		{
			// Manage AI thinks of a L2Attackable
			if (getIntention() == AI_INTENTION_ACTIVE)
			{
				thinkActive();
			}
			else if (getIntention() == AI_INTENTION_ATTACK)
			{
				thinkAttack();
			}
		}
		finally
		{
			// Stop thinking action
			thinking = false;
		}
	}
	
	/**
	 * Launch actions corresponding to the Event Attacked.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Init the attack : Calculate the attack timeout, Set the _globalAggro to 0, Add the attacker to the actor _aggroList</li>
	 * <li>Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance</li>
	 * <li>Set the Intention to AI_INTENTION_ATTACK</li><BR>
	 * <BR>
	 * @param attacker The L2Character that attacks the actor
	 */
	@Override
	protected void onEvtAttacked(L2Character attacker)
	{
		final L2Attackable me = (L2Attackable) _actor;
		
		// Calculate the attack timeout
		_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getGameTicks();
		
		// Set the _globalAggro to 0 to permit attack even just after spawn
		if (_globalAggro < 0)
		{
			_globalAggro = 0;
		}
		
		// Add the attacker to the _aggroList of the actor
		me.addDamageHate(attacker, 0, 1);
		
		// Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
		if (!_actor.isRunning())
		{
			_actor.setRunning();
		}
		
		// Set the Intention to AI_INTENTION_ATTACK
		if (getIntention() != AI_INTENTION_ATTACK)
		{
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
		}
		else if (me.getMostHated() != getAttackTarget())
		{
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
		}
		
		// If this attackable is a L2MonsterInstance and it has spawned minions, call its minions to battle
		if (me instanceof L2MonsterInstance)
		{
			L2MonsterInstance master = (L2MonsterInstance) me;
			if (me instanceof L2MinionInstance)
			{
				master = ((L2MinionInstance) me).getLeader();
				if ((master != null) && !master.isInCombat() && !master.isDead())
				{
					master.addDamageHate(attacker, 0, 1);
					master.callMinionsToAssist(attacker);
				}
			}
			else if (master.hasMinions())
			{
				master.callMinionsToAssist(attacker);
			}
		}
		
		super.onEvtAttacked(attacker);
	}
	
	/**
	 * Launch actions corresponding to the Event Aggression.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Add the target to the actor _aggroList or update hate if already present</li>
	 * <li>Set the actor Intention to AI_INTENTION_ATTACK (if actor is L2GuardInstance check if it isn't too far from its home location)</li><BR>
	 * <BR>
	 * @param target The L2Character that attacks
	 * @param aggro The value of hate to add to the actor against the target
	 */
	@Override
	protected void onEvtAggression(L2Character target, int aggro)
	{
		final L2Attackable me = (L2Attackable) _actor;
		
		if (target != null)
		{
			// Add the target to the actor _aggroList or update hate if already present
			me.addDamageHate(target, 0, aggro);
			
			// Get the hate of the actor against the target
			// only if hate is definitely reduced
			if (aggro < 0)
			{
				if (me.getHating(target) <= 0)
				{
					if (me.getMostHated() == null)
					{
						_globalAggro = -25;
						me.clearAggroList();
						setIntention(AI_INTENTION_ACTIVE);
						_actor.setWalking();
					}
				}
				return;
			}
			
			// Set the actor AI Intention to AI_INTENTION_ATTACK
			if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
			{
				// Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance
				if (!_actor.isRunning())
				{
					_actor.setRunning();
				}
				
				setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
		}
		else
		{
			// currently only for setting lower general aggro
			if (aggro >= 0)
			{
				return;
			}
			
			final L2Character mostHated = me.getMostHated();
			if (mostHated == null)
			{
				_globalAggro = -25;
				return;
			}
			for (final L2Character aggroed : me.getAggroList().keySet())
			{
				me.addDamageHate(aggroed, 0, aggro);
			}
			
			aggro = me.getHating(mostHated);
			if (aggro <= 0)
			{
				_globalAggro = -25;
				me.clearAggroList();
				setIntention(AI_INTENTION_ACTIVE);
				_actor.setWalking();
			}
		}
	}
}