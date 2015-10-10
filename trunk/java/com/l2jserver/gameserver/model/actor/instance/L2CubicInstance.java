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
package com.l2jserver.gameserver.model.actor.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jserver.Config;
import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.ai.CtrlEvent;
import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.instancemanager.DuelManager;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.L2Party;
import com.l2jserver.gameserver.model.actor.L2Attackable;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Summon;
import com.l2jserver.gameserver.model.actor.tasks.cubics.CubicAction;
import com.l2jserver.gameserver.model.actor.tasks.cubics.CubicBuff;
import com.l2jserver.gameserver.model.actor.tasks.cubics.CubicDisappear;
import com.l2jserver.gameserver.model.actor.tasks.cubics.CubicHeal;
import com.l2jserver.gameserver.model.effects.L2EffectType;
import com.l2jserver.gameserver.model.entity.TvTEvent;
import com.l2jserver.gameserver.model.entity.TvTEventTeam;
import com.l2jserver.gameserver.model.interfaces.IIdentifiable;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.model.stats.Formulas;
import com.l2jserver.gameserver.model.stats.Stats;
import com.l2jserver.gameserver.model.zone.ZoneId;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.util.Rnd;

public final class L2CubicInstance implements IIdentifiable
{
	private static final Logger _log = Logger.getLogger(L2CubicInstance.class.getName());
	
	// Type of Cubics
	public static final int STORM_CUBIC = 1;
	public static final int VAMPIRIC_CUBIC = 2;
	public static final int LIFE_CUBIC = 3;
	public static final int VIPER_CUBIC = 4;
	public static final int POLTERGEIST_CUBIC = 5;
	public static final int BINDING_CUBIC = 6;
	public static final int AQUA_CUBIC = 7;
	public static final int SPARK_CUBIC = 8;
	public static final int ATTRACT_CUBIC = 9;
	public static final int SMART_CUBIC_EVATEMPLAR = 10;
	public static final int SMART_CUBIC_SHILLIENTEMPLAR = 11;
	public static final int SMART_CUBIC_ARCANALORD = 12;
	public static final int SMART_CUBIC_ELEMENTALMASTER = 13;
	public static final int SMART_CUBIC_SPECTRALMASTER = 14;
	public static final int AVENGING_CUBIC = 15;
	public static final int KNIGHT_CUBIC = 16;
	public static final int FAIRY_CUBIC = 17;
	public static final int BUFF_CUBIC = 18;
	public static final int MIND_CUBIC = 19; // NOT USED
	public static final int PHANTOM_CUBIC = 20;
	public static final int HEX_CUBIC = 21;
	public static final int GUARDIAN_CUBIC = 22;
	
	// Max range of cubic skills
	// TODO: Check/fix the max range
	public static final int MAX_MAGIC_RANGE = 900;
	
	// Cubic skills
	public static final int SKILL_CUBIC_HEAL = 4051;
	public static final int SKILL_CUBIC_CURE = 5579;
	public static final int SKILL_CUBIC_HEALER = 11807;
	public static final int SKILL_BUFF_CUBIC_HEAL = 10083;
	public static final int SKILL_MIND_CUBIC_RECHARGE = 10084;
	public static final int SKILL_BUFF_CUBIC_GREAT_HEAL = 10082;
	public static final int SKILL_MIND_CUBIC_GREAT_RECHARGE = 10089;
	public static final int SKILL_AVENGING_CUBIC_CLEANCE = 11292;
	public static final int SKILL_KNIGHT_CUBIC = 10056;
	public static final int SKILL_GUARDIAN_CUBIC = 10093;
	
	private final L2PcInstance _owner;
	private L2Character _target;
	
	private final int _cubicId;
	private final int _cubicPower;
	private final int _cubicDelay;
	private final int _cubicSkillChance;
	private final int _cubicMaxCount;
	private boolean _active;
	private final boolean _givenByOther;
	
	private final List<Skill> _skills = new ArrayList<>();
	
	private Future<?> _disappearTask;
	private Future<?> _actionTask;
	
	public L2CubicInstance(L2PcInstance owner, int cubicId, int level, int cubicPower, int cubicDelay, int cubicSkillChance, int cubicMaxCount, int cubicDuration, boolean givenByOther)
	{
		_owner = owner;
		_cubicId = cubicId;
		_cubicPower = cubicPower;
		_cubicDelay = cubicDelay * 1000;
		_cubicSkillChance = cubicSkillChance;
		_cubicMaxCount = cubicMaxCount;
		_active = false;
		_givenByOther = givenByOther;
		
		switch (_cubicId)
		{
			case STORM_CUBIC:
				_skills.add(SkillData.getInstance().getSkill(4049, level));
				break;
			case VAMPIRIC_CUBIC:
				_skills.add(SkillData.getInstance().getSkill(4050, level));
				break;
			case LIFE_CUBIC:
				_skills.add(SkillData.getInstance().getSkill(4051, level));
				doAction();
				break;
			case VIPER_CUBIC:
				_skills.add(SkillData.getInstance().getSkill(4052, level));
				break;
			case POLTERGEIST_CUBIC:
				_skills.add(SkillData.getInstance().getSkill(4053, level));
				_skills.add(SkillData.getInstance().getSkill(4054, level));
				_skills.add(SkillData.getInstance().getSkill(4055, level));
				break;
			case BINDING_CUBIC:
				_skills.add(SkillData.getInstance().getSkill(4164, level));
				break;
			case AQUA_CUBIC:
				_skills.add(SkillData.getInstance().getSkill(4165, level));
				break;
			case SPARK_CUBIC:
				_skills.add(SkillData.getInstance().getSkill(4166, level));
				break;
			case ATTRACT_CUBIC:
				_skills.add(SkillData.getInstance().getSkill(5115, level));
				_skills.add(SkillData.getInstance().getSkill(5116, level));
				break;
			case SMART_CUBIC_ARCANALORD:
				_skills.add(SkillData.getInstance().getSkill(4051, 7));
				_skills.add(SkillData.getInstance().getSkill(4165, 9));
				break;
			case SMART_CUBIC_ELEMENTALMASTER:
				_skills.add(SkillData.getInstance().getSkill(4049, 8));
				_skills.add(SkillData.getInstance().getSkill(4166, 9));
				break;
			case SMART_CUBIC_SPECTRALMASTER:
				_skills.add(SkillData.getInstance().getSkill(4049, 8));
				_skills.add(SkillData.getInstance().getSkill(4052, 6));
				break;
			case SMART_CUBIC_EVATEMPLAR:
				_skills.add(SkillData.getInstance().getSkill(4053, 8));
				_skills.add(SkillData.getInstance().getSkill(4165, 9));
				break;
			case SMART_CUBIC_SHILLIENTEMPLAR:
				_skills.add(SkillData.getInstance().getSkill(4049, 8));
				_skills.add(SkillData.getInstance().getSkill(5115, 4));
				break;
			case AVENGING_CUBIC:
				_skills.add(SkillData.getInstance().getSkill(11292, level));
				_skills.add(SkillData.getInstance().getSkill(11293, level));
				_skills.add(SkillData.getInstance().getSkill(11294, level));
				break;
			case KNIGHT_CUBIC:
				_skills.add(SkillData.getInstance().getSkill(10056, level));
				doAction();
				break;
			case FAIRY_CUBIC:
				_skills.add(SkillData.getInstance().getSkill(11807, level));
				doAction();
				break;
			case BUFF_CUBIC:
				_skills.add(SkillData.getInstance().getSkill(10082, level));
				_skills.add(SkillData.getInstance().getSkill(10083, level));
				_skills.add(SkillData.getInstance().getSkill(10084, level));
				_skills.add(SkillData.getInstance().getSkill(10089, level));
				doAction();
				break;
			case MIND_CUBIC: // NOT USED
				_skills.add(SkillData.getInstance().getSkill(10084, level));
				_skills.add(SkillData.getInstance().getSkill(10089, level));
				doAction();
				break;
			case PHANTOM_CUBIC:
				_skills.add(SkillData.getInstance().getSkill(10085, level));
				break;
			case HEX_CUBIC:
				_skills.add(SkillData.getInstance().getSkill(10086, level));
				break;
			case GUARDIAN_CUBIC:
				_skills.add(SkillData.getInstance().getSkill(10093, level));
				doAction();
				break;
		}
		_disappearTask = ThreadPoolManager.getInstance().scheduleGeneral(new CubicDisappear(this), cubicDuration * 1000); // disappear
	}
	
	public synchronized void doAction()
	{
		if (_active)
		{
			return;
		}
		_active = true;
		
		switch (_cubicId)
		{
			case AQUA_CUBIC:
			case BINDING_CUBIC:
			case SPARK_CUBIC:
			case STORM_CUBIC:
			case POLTERGEIST_CUBIC:
			case VAMPIRIC_CUBIC:
			case VIPER_CUBIC:
			case ATTRACT_CUBIC:
			case SMART_CUBIC_ARCANALORD:
			case SMART_CUBIC_ELEMENTALMASTER:
			case SMART_CUBIC_SPECTRALMASTER:
			case SMART_CUBIC_EVATEMPLAR:
			case SMART_CUBIC_SHILLIENTEMPLAR:
			case AVENGING_CUBIC:
			case PHANTOM_CUBIC:
			case HEX_CUBIC:
				_actionTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new CubicAction(this, _cubicSkillChance), 0, _cubicDelay);
				break;
			case LIFE_CUBIC:
			case FAIRY_CUBIC:
			case BUFF_CUBIC:
			case MIND_CUBIC:
				_actionTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new CubicHeal(this), 0, _cubicDelay);
				break;
			case KNIGHT_CUBIC:
			case GUARDIAN_CUBIC:
				_actionTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new CubicBuff(this, _cubicSkillChance), 0, _cubicDelay);
				break;
		}
	}
	
	@Override
	public int getId()
	{
		return _cubicId;
	}
	
	public L2PcInstance getOwner()
	{
		return _owner;
	}
	
	public int getCubicPower()
	{
		return _cubicPower;
	}
	
	public L2Character getTarget()
	{
		return _target;
	}
	
	public void setTarget(L2Character target)
	{
		_target = target;
	}
	
	public List<Skill> getSkills()
	{
		return _skills;
	}
	
	public int getCubicMaxCount()
	{
		return _cubicMaxCount;
	}
	
	public void stopAction()
	{
		_target = null;
		if (_actionTask != null)
		{
			_actionTask.cancel(true);
			_actionTask = null;
		}
		_active = false;
	}
	
	public void cancelDisappear()
	{
		if (_disappearTask != null)
		{
			_disappearTask.cancel(true);
			_disappearTask = null;
		}
	}
	
	/** this sets the enemy target for a cubic */
	public void getCubicTarget()
	{
		try
		{
			_target = null;
			L2Object ownerTarget = _owner.getTarget();
			if (ownerTarget == null)
			{
				return;
			}
			// TvT event targeting
			if (TvTEvent.isStarted() && TvTEvent.isPlayerParticipant(_owner.getObjectId()))
			{
				TvTEventTeam enemyTeam = TvTEvent.getParticipantEnemyTeam(_owner.getObjectId());
				
				if (ownerTarget.getActingPlayer() != null)
				{
					L2PcInstance target = ownerTarget.getActingPlayer();
					if (enemyTeam.containsPlayer(target.getObjectId()) && !(target.isDead()))
					{
						_target = (L2Character) ownerTarget;
					}
				}
				return;
			}
			// Duel targeting
			if (_owner.isInDuel())
			{
				L2PcInstance PlayerA = DuelManager.getInstance().getDuel(_owner.getDuelId()).getPlayerA();
				L2PcInstance PlayerB = DuelManager.getInstance().getDuel(_owner.getDuelId()).getPlayerB();
				
				if (DuelManager.getInstance().getDuel(_owner.getDuelId()).isPartyDuel())
				{
					L2Party partyA = PlayerA.getParty();
					L2Party partyB = PlayerB.getParty();
					L2Party partyEnemy = null;
					
					if (partyA != null)
					{
						if (partyA.getMembers().contains(_owner))
						{
							if (partyB != null)
							{
								partyEnemy = partyB;
							}
							else
							{
								_target = PlayerB;
							}
						}
						else
						{
							partyEnemy = partyA;
						}
					}
					else
					{
						if (PlayerA == _owner)
						{
							if (partyB != null)
							{
								partyEnemy = partyB;
							}
							else
							{
								_target = PlayerB;
							}
						}
						else
						{
							_target = PlayerA;
						}
					}
					if ((_target == PlayerA) || (_target == PlayerB))
					{
						if (_target == ownerTarget)
						{
							return;
						}
					}
					if (partyEnemy != null)
					{
						if (partyEnemy.getMembers().contains(ownerTarget))
						{
							_target = (L2Character) ownerTarget;
						}
						return;
					}
				}
				if ((PlayerA != _owner) && (ownerTarget == PlayerA))
				{
					_target = PlayerA;
					return;
				}
				if ((PlayerB != _owner) && (ownerTarget == PlayerB))
				{
					_target = PlayerB;
					return;
				}
				_target = null;
				return;
			}
			// Olympiad targeting
			if (_owner.isInOlympiadMode())
			{
				if (_owner.isOlympiadStart())
				{
					if (ownerTarget.isPlayable())
					{
						final L2PcInstance targetPlayer = ownerTarget.getActingPlayer();
						if ((targetPlayer != null) && (targetPlayer.getOlympiadGameId() == _owner.getOlympiadGameId()) && (targetPlayer.getOlympiadSide() != _owner.getOlympiadSide()))
						{
							_target = (L2Character) ownerTarget;
						}
					}
				}
				return;
			}
			// test owners target if it is valid then use it
			final L2Summon pet = _owner.getPet();
			if (ownerTarget.isCharacter() && (ownerTarget != pet) && !_owner.hasServitor(ownerTarget.getObjectId()) && (ownerTarget != _owner))
			{
				// target mob which has aggro on you or your summon
				if (ownerTarget.isAttackable())
				{
					final L2Attackable attackable = (L2Attackable) ownerTarget;
					if (attackable.isInAggroList(_owner) && !attackable.isDead())
					{
						_target = (L2Character) ownerTarget;
						return;
					}
					if (_owner.hasSummon())
					{
						if (attackable.isInAggroList(pet) && !attackable.isDead())
						{
							_target = (L2Character) ownerTarget;
							return;
						}
						for (L2Summon servitor : _owner.getServitors().values())
						{
							if (attackable.isInAggroList(servitor) && !attackable.isDead())
							{
								_target = (L2Character) ownerTarget;
								return;
							}
						}
					}
				}
				
				// get target in pvp or in siege
				L2PcInstance enemy = null;
				
				if (((_owner.getPvpFlag() > 0) && !_owner.isInsideZone(ZoneId.PEACE)) || _owner.isInsideZone(ZoneId.PVP))
				{
					if (!((L2Character) ownerTarget).isDead())
					{
						enemy = ownerTarget.getActingPlayer();
					}
					
					if (enemy != null)
					{
						boolean targetIt = true;
						
						if (_owner.getParty() != null)
						{
							if (_owner.getParty().getMembers().contains(enemy))
							{
								targetIt = false;
							}
							else if (_owner.getParty().getCommandChannel() != null)
							{
								if (_owner.getParty().getCommandChannel().getMembers().contains(enemy))
								{
									targetIt = false;
								}
							}
						}
						if ((_owner.getClan() != null) && !_owner.isInsideZone(ZoneId.PVP))
						{
							if (_owner.getClan().isMember(enemy.getObjectId()))
							{
								targetIt = false;
							}
							if ((_owner.getAllyId() > 0) && (enemy.getAllyId() > 0))
							{
								if (_owner.getAllyId() == enemy.getAllyId())
								{
									targetIt = false;
								}
							}
						}
						if ((enemy.getPvpFlag() == 0) && !enemy.isInsideZone(ZoneId.PVP))
						{
							targetIt = false;
						}
						if (enemy.isInsideZone(ZoneId.PEACE))
						{
							targetIt = false;
						}
						if ((_owner.getSiegeState() > 0) && (_owner.getSiegeState() == enemy.getSiegeState()))
						{
							targetIt = false;
						}
						if (!enemy.isVisible())
						{
							targetIt = false;
						}
						
						if (targetIt)
						{
							_target = enemy;
							return;
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "", e);
		}
	}
	
	public void useCubicContinuous(Skill skill, L2Object[] targets)
	{
		for (L2Character target : (L2Character[]) targets)
		{
			if ((target == null) || target.isDead())
			{
				continue;
			}
			
			if (skill.isBad())
			{
				byte shld = Formulas.calcShldUse(_owner, target, skill);
				boolean acted = Formulas.calcCubicSkillSuccess(this, target, skill, shld);
				if (!acted)
				{
					_owner.sendPacket(SystemMessageId.YOUR_ATTACK_HAS_FAILED);
					continue;
				}
				
			}
			
			// Apply effects
			skill.applyEffects(_owner, target, false, false, true, 0);
			
			// If this is a bad skill notify the duel manager, so it can be removed after the duel (player & target must be in the same duel).
			if (target.isPlayer() && target.getActingPlayer().isInDuel() && skill.isBad() && (_owner.getDuelId() == target.getActingPlayer().getDuelId()))
			{
				DuelManager.getInstance().onBuff(target.getActingPlayer(), skill);
			}
		}
	}
	
	/**
	 * @param skill
	 * @param targets
	 */
	public void useCubicMdam(Skill skill, L2Object[] targets)
	{
		for (L2Character target : (L2Character[]) targets)
		{
			if (target == null)
			{
				continue;
			}
			
			if (target.isAlikeDead())
			{
				if (target.isPlayer())
				{
					target.stopFakeDeath(true);
				}
				else
				{
					continue;
				}
			}
			
			boolean mcrit = Formulas.calcMCrit(_owner.getMCriticalHit(target, skill));
			byte shld = Formulas.calcShldUse(_owner, target, skill);
			int damage = (int) Formulas.calcMagicDam(this, target, skill, mcrit, shld);
			
			if (Config.DEBUG)
			{
				_log.info("L2SkillMdam: useCubicSkill() -> damage = " + damage);
			}
			
			if (damage > 0)
			{
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}
				
				// Shield Deflect Magic: If target is reflecting the skill then no damage is done.
				if (target.getStat().calcStat(Stats.VENGEANCE_SKILL_MAGIC_DAMAGE, 0, target, skill) > Rnd.get(100))
				{
					damage = 0;
				}
				else
				{
					_owner.sendDamageMessage(target, damage, mcrit, false, false);
					target.reduceCurrentHp(damage, _owner, skill);
				}
			}
		}
	}
	
	public void useCubicDrain(Skill skill, L2Object[] targets)
	{
		if (Config.DEBUG)
		{
			_log.info("L2SkillDrain: useCubicSkill()");
		}
		
		for (L2Character target : (L2Character[]) targets)
		{
			if (target.isAlikeDead())
			{
				continue;
			}
			
			boolean mcrit = Formulas.calcMCrit(_owner.getMCriticalHit(target, skill));
			byte shld = Formulas.calcShldUse(_owner, target, skill);
			
			int damage = (int) Formulas.calcMagicDam(this, target, skill, mcrit, shld);
			if (Config.DEBUG)
			{
				_log.info("L2SkillDrain: useCubicSkill() -> damage = " + damage);
			}
			
			// TODO: Unhardcode fixed value
			double hpAdd = (0.4 * damage);
			L2PcInstance owner = _owner;
			double hp = ((owner.getCurrentHp() + hpAdd) > owner.getMaxHp() ? owner.getMaxHp() : (owner.getCurrentHp() + hpAdd));
			
			owner.setCurrentHp(hp);
			
			// Check to see if we should damage the target
			if ((damage > 0) && !target.isDead())
			{
				target.reduceCurrentHp(damage, _owner, skill);
				
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}
				owner.sendDamageMessage(target, damage, mcrit, false, false);
			}
		}
	}
	
	public void useCubicDisabler(Skill skill, L2Object[] targets)
	{
		if (Config.DEBUG)
		{
			_log.info("Disablers: useCubicSkill()");
		}
		
		for (L2Character target : (L2Character[]) targets)
		{
			if ((target == null) || target.isDead())
			{
				continue;
			}
			
			byte shld = Formulas.calcShldUse(_owner, target, skill);
			
			if (skill.hasEffectType(L2EffectType.STUN, L2EffectType.PARALYZE, L2EffectType.ROOT))
			{
				if (Formulas.calcCubicSkillSuccess(this, target, skill, shld))
				{
					// Apply effects
					skill.applyEffects(_owner, target, false, false, true, 0);
					
					// If this is a bad skill notify the duel manager, so it can be removed after the duel (player & target must be in the same duel).
					if (target.isPlayer() && target.getActingPlayer().isInDuel() && skill.isBad() && (_owner.getDuelId() == target.getActingPlayer().getDuelId()))
					{
						DuelManager.getInstance().onBuff(target.getActingPlayer(), skill);
					}
					
					if (Config.DEBUG)
					{
						_log.info("Disablers: useCubicSkill() -> success");
					}
				}
				else
				{
					if (Config.DEBUG)
					{
						_log.info("Disablers: useCubicSkill() -> failed");
					}
				}
			}
			
			if (skill.hasEffectType(L2EffectType.AGGRESSION))
			{
				if (Formulas.calcCubicSkillSuccess(this, target, skill, shld))
				{
					if (target.isAttackable())
					{
						target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, _owner, (int) ((150 * skill.getPower()) / (target.getLevel() + 7)));
					}
					
					// Apply effects
					skill.applyEffects(_owner, target, false, false, true, 0);
					
					if (Config.DEBUG)
					{
						_log.info("Disablers: useCubicSkill() -> success");
					}
				}
				else
				{
					if (Config.DEBUG)
					{
						_log.info("Disablers: useCubicSkill() -> failed");
					}
				}
			}
		}
	}
	
	/**
	 * @param owner
	 * @param target
	 * @return true if the target is inside of the owner's max Cubic range
	 */
	public static boolean isInCubicRange(L2Character owner, L2Character target)
	{
		if ((owner == null) || (target == null))
		{
			return false;
		}
		
		int x, y, z;
		// temporary range check until real behavior of cubics is known/coded
		int range = MAX_MAGIC_RANGE;
		
		x = (owner.getX() - target.getX());
		y = (owner.getY() - target.getY());
		z = (owner.getZ() - target.getZ());
		
		return (((x * x) + (y * y) + (z * z)) <= (range * range));
	}
	
	/** this sets the friendly target for a cubic */
	public void cubicTargetForHeal()
	{
		L2Character target = null;
		double percentleft = 100.0;
		L2Party party = _owner.getParty();
		
		// if owner is in a duel but not in a party duel, then it is the same as he does not have a party
		if (_owner.isInDuel())
		{
			if (!DuelManager.getInstance().getDuel(_owner.getDuelId()).isPartyDuel())
			{
				party = null;
			}
		}
		
		if ((party != null) && !_owner.isInOlympiadMode())
		{
			// Get all visible objects in a spheric area near the L2Character
			// Get a list of Party Members
			for (L2Character partyMember : party.getMembers())
			{
				if (!partyMember.isDead())
				{
					// if party member not dead, check if he is in cast range of heal cubic
					if (isInCubicRange(_owner, partyMember))
					{
						// member is in cubic casting range, check if he need heal and if he have
						// the lowest HP
						if (partyMember.getCurrentHp() < partyMember.getMaxHp())
						{
							if (percentleft > (partyMember.getCurrentHp() / partyMember.getMaxHp()))
							{
								percentleft = (partyMember.getCurrentHp() / partyMember.getMaxHp());
								target = partyMember;
							}
						}
					}
				}
				final L2Summon pet = partyMember.getPet();
				if (pet != null)
				{
					if (pet.isDead() || !isInCubicRange(_owner, pet))
					{
						continue;
					}
					
					// member's pet is in cubic casting range, check if he need heal and if he have
					// the lowest HP
					if (pet.getCurrentHp() < pet.getMaxHp())
					{
						if (percentleft > (pet.getCurrentHp() / pet.getMaxHp()))
						{
							percentleft = (pet.getCurrentHp() / pet.getMaxHp());
							target = pet;
						}
					}
				}
				for (L2Summon s : partyMember.getServitors().values())
				{
					if (s.isDead() || !isInCubicRange(_owner, s))
					{
						continue;
					}
					
					// member's pet is in cubic casting range, check if he need heal and if he have
					// the lowest HP
					if (s.getCurrentHp() < s.getMaxHp())
					{
						if (percentleft > (s.getCurrentHp() / s.getMaxHp()))
						{
							percentleft = (s.getCurrentHp() / s.getMaxHp());
							target = s;
						}
					}
				}
			}
		}
		else
		{
			if (_owner.getCurrentHp() < _owner.getMaxHp())
			{
				percentleft = (_owner.getCurrentHp() / _owner.getMaxHp());
				target = _owner;
			}
			for (L2Summon summon : _owner.getServitors().values())
			{
				if (!summon.isDead() && (summon.getCurrentHp() < summon.getMaxHp()) && (percentleft > (summon.getCurrentHp() / summon.getMaxHp())) && isInCubicRange(_owner, summon))
				{
					target = summon;
				}
			}
			final L2Summon pet = _owner.getPet();
			if (pet != null)
			{
				if (!pet.isDead() && (pet.getCurrentHp() < pet.getMaxHp()) && (percentleft > (pet.getCurrentHp() / pet.getMaxHp())) && isInCubicRange(_owner, pet))
				{
					target = _owner.getPet();
				}
			}
		}
		
		_target = target;
	}
	
	public boolean givenByOther()
	{
		return _givenByOther;
	}
}
