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
package com.l2jmobius.gameserver.handler.skillhandlers;

import com.l2jmobius.gameserver.ai.CtrlEvent;
import com.l2jmobius.gameserver.handler.ISkillHandler;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2Effect;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.L2Skill.SkillType;
import com.l2jmobius.gameserver.model.L2Summon;
import com.l2jmobius.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.skills.Formulas;
import com.l2jmobius.gameserver.skills.Stats;
import com.l2jmobius.gameserver.skills.funcs.Func;

/**
 * This Handles Disabler skills
 * @author _drunk_
 */
public class Disablers implements ISkillHandler
{
	protected SkillType[] _skillIds =
	{
		L2Skill.SkillType.STUN,
		L2Skill.SkillType.ROOT,
		L2Skill.SkillType.SLEEP,
		L2Skill.SkillType.CONFUSION,
		L2Skill.SkillType.AGGDAMAGE,
		L2Skill.SkillType.AGGREDUCE,
		L2Skill.SkillType.AGGREDUCE_CHAR,
		L2Skill.SkillType.AGGREMOVE,
		
		L2Skill.SkillType.MUTE,
		L2Skill.SkillType.FAKE_DEATH,
		L2Skill.SkillType.CONFUSE_MOB_ONLY,
		L2Skill.SkillType.MAGE_BANE,
		L2Skill.SkillType.WARRIOR_BANE,
		L2Skill.SkillType.CANCEL,
		L2Skill.SkillType.PARALYZE
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets, boolean crit)
	{
		final SkillType type = skill.getSkillType();
		
		boolean ss = false;
		boolean sps = false;
		boolean bss = false;
		
		final L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		if (weaponInst != null)
		{
			if (skill.useSpiritShot())
			{
				if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
				{
					bss = true;
				}
				else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
				{
					sps = true;
				}
			}
			else if (skill.useSoulShot())
			{
				if (weaponInst.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT)
				{
					ss = true;
				}
			}
		}
		// If there is no weapon equipped, check for an active summon.
		else if (activeChar instanceof L2Summon)
		{
			final L2Summon activeSummon = (L2Summon) activeChar;
			if (skill.useSpiritShot())
			{
				if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
				{
					bss = true;
				}
				else if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT)
				{
					sps = true;
				}
			}
			else if (activeSummon.getChargedSoulShot() == L2ItemInstance.CHARGED_SOULSHOT)
			{
				ss = true;
			}
		}
		else if (activeChar instanceof L2NpcInstance)
		{
			bss = ((L2NpcInstance) activeChar).isUsingShot(false);
			ss = ((L2NpcInstance) activeChar).isUsingShot(true);
		}
		
		for (int index = 0; index < targets.length; index++)
		{
			// Get a target
			if (!(targets[index] instanceof L2Character))
			{
				continue;
			}
			
			L2Character target = (L2Character) targets[index];
			
			if ((target == null) || target.isDead() || target.isInvul())
			{
				continue;
			}
			
			switch (type)
			{
				case FAKE_DEATH:
				{
					// stun/fakedeath is not mdef dependant, it depends on lvl difference, target CON and power of stun
					skill.getEffects(activeChar, target);
					break;
				}
				
				case STUN:
				{
					if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
					{
						if (Formulas.getInstance().calculateSkillReflect(skill, activeChar, target))
						{
							target = activeChar;
						}
						
						skill.getEffects(activeChar, target);
					}
					
					else
					{
						if (activeChar instanceof L2PcInstance)
						{
							final SystemMessage sm = new SystemMessage(SystemMessage.S1_WAS_UNAFFECTED_BY_S2);
							sm.addString(target.getName());
							sm.addSkillName(skill.getDisplayId());
							activeChar.sendPacket(sm);
						}
					}
					break;
				}
				case ROOT:
				case SLEEP:
				
				case PARALYZE:
				{
					if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
					{
						if (Formulas.getInstance().calculateSkillReflect(skill, activeChar, target))
						{
							target = activeChar;
						}
						
						skill.getEffects(activeChar, target);
					}
					
					else
					{
						if (activeChar instanceof L2PcInstance)
						{
							final SystemMessage sm = new SystemMessage(SystemMessage.S1_WAS_UNAFFECTED_BY_S2);
							sm.addString(target.getName());
							sm.addSkillName(skill.getDisplayId());
							activeChar.sendPacket(sm);
						}
					}
					
					break;
				}
				case CONFUSION:
				case MUTE:
				{
					if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
					{
						if (Formulas.getInstance().calculateSkillReflect(skill, activeChar, target))
						{
							target = activeChar;
						}
						
						// stop same type effect if available
						final L2Effect[] effects = target.getAllEffects();
						for (final L2Effect e : effects)
						{
							
							if (e.getSkill().getSkillType() == type)
							{
								e.exit();
							}
							
						}
						
						skill.getEffects(activeChar, target);
						
					}
					else
					{
						if (activeChar instanceof L2PcInstance)
						{
							final SystemMessage sm = new SystemMessage(SystemMessage.S1_WAS_UNAFFECTED_BY_S2);
							sm.addString(target.getName());
							sm.addSkillName(skill.getDisplayId());
							activeChar.sendPacket(sm);
						}
					}
					break;
				}
				case CONFUSE_MOB_ONLY:
				{
					// do nothing if not on mob
					if ((target instanceof L2MonsterInstance) && Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
					{
						if (Formulas.getInstance().calculateSkillReflect(skill, activeChar, target))
						{
							target = activeChar;
						}
						
						// stop same type effect if available
						final L2Effect[] effects = target.getAllEffects();
						for (final L2Effect e : effects)
						{
							
							if (e.getSkill().getSkillType() == type)
							{
								e.exit();
							}
							
						}
						
						skill.getEffects(activeChar, target);
						
					}
					else
					{
						if (activeChar instanceof L2PcInstance)
						{
							final SystemMessage sm = new SystemMessage(SystemMessage.S1_WAS_UNAFFECTED_BY_S2);
							sm.addString(target.getName());
							sm.addSkillName(skill.getDisplayId());
							activeChar.sendPacket(sm);
						}
					}
					
					break;
				}
				case AGGDAMAGE:
				{
					if (target instanceof L2MonsterInstance)
					{
						target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, (int) skill.getPower());
					}
					
					skill.getEffects(activeChar, target);
					
					final L2Effect effect = activeChar.getFirstEffect(skill.getId());
					if ((effect != null) && effect.isSelfEffect())
					{
						effect.exit();
					}
					
					skill.getEffectsSelf(activeChar);
					break;
				}
				case AGGREDUCE:
				{
					// these skills needs to be rechecked
					if (target instanceof L2MonsterInstance)
					{
						skill.getEffects(activeChar, target);
						
						final double aggdiff = ((L2MonsterInstance) target).getHating(activeChar) - target.calcStat(Stats.AGGRESSION, ((L2MonsterInstance) target).getHating(activeChar), target, skill);
						
						if (skill.getPower() > 0)
						{
							target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, null, -(int) skill.getPower());
						}
						else if (aggdiff > 0)
						{
							target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, null, -(int) aggdiff);
						}
					}
					break;
				}
				case AGGREDUCE_CHAR:
				{
					// these skills needs to be rechecked
					if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
					{
						if (target instanceof L2MonsterInstance)
						{
							target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, -((L2MonsterInstance) target).getHating(activeChar));
						}
						
						skill.getEffects(activeChar, target);
					}
					else
					{
						if (activeChar instanceof L2PcInstance)
						{
							final SystemMessage sm = new SystemMessage(SystemMessage.S1_WAS_UNAFFECTED_BY_S2);
							sm.addString(target.getName());
							sm.addSkillName(skill.getDisplayId());
							activeChar.sendPacket(sm);
						}
					}
					break;
				}
				case AGGREMOVE:
				{
					// these skills needs to be rechecked
					if (target instanceof L2MonsterInstance)
					{
						if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
						{
							if (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_UNDEAD)
							{
								if (target.isUndead())
								{
									target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, null, -((L2MonsterInstance) target).getHating(((L2MonsterInstance) target).getMostHated()));
								}
							}
							else
							{
								target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, null, -((L2MonsterInstance) target).getHating(((L2MonsterInstance) target).getMostHated()));
							}
						}
						else
						{
							if (activeChar instanceof L2PcInstance)
							{
								final SystemMessage sm = new SystemMessage(SystemMessage.S1_WAS_UNAFFECTED_BY_S2);
								sm.addString(target.getName());
								sm.addSkillName(skill.getDisplayId());
								activeChar.sendPacket(sm);
							}
						}
					}
					
					break;
				}
				case MAGE_BANE:
				{
					
					if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
					{
						final L2Effect[] effects = target.getAllEffects();
						for (final L2Effect e : effects)
						{
							for (final Func f : e.getStatFuncs())
							{
								if ((f._stat == Stats.MAGIC_ATTACK) || (f._stat == Stats.MAGIC_ATTACK_SPEED))
								{
									e.exit();
									break;
								}
							}
						}
					}
					else
					{
						
						if (activeChar instanceof L2PcInstance)
						{
							final SystemMessage sm = new SystemMessage(SystemMessage.S1_WAS_UNAFFECTED_BY_S2);
							sm.addString(target.getName());
							sm.addSkillName(skill.getId());
							activeChar.sendPacket(sm);
						}
						
					}
					break;
				}
				case WARRIOR_BANE:
				{
					if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
					{
						final L2Effect[] effects = target.getAllEffects();
						for (final L2Effect e : effects)
						{
							for (final Func f : e.getStatFuncs())
							{
								if ((f._stat == Stats.RUN_SPEED) || (f._stat == Stats.POWER_ATTACK_SPEED))
								{
									e.exit();
									break;
								}
							}
						}
					}
					else
					{
						
						if (activeChar instanceof L2PcInstance)
						{
							final SystemMessage sm = new SystemMessage(SystemMessage.S1_WAS_UNAFFECTED_BY_S2);
							sm.addString(target.getName());
							sm.addSkillName(skill.getId());
							activeChar.sendPacket(sm);
						}
						
					}
					break;
				}
				case CANCEL:
				{
					if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
					{
						target.negateEffects(null, 0, skill.getMaxNegatedEffects());
					}
					else
					{
						if (activeChar instanceof L2PcInstance)
						{
							final SystemMessage sm = new SystemMessage(SystemMessage.S1_WAS_UNAFFECTED_BY_S2);
							sm.addString(target.getName());
							sm.addSkillName(skill.getDisplayId());
							activeChar.sendPacket(sm);
						}
					}
					
					break;
					
				}
			}
		}
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return _skillIds;
	}
}
