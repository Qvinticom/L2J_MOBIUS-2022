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

import com.l2jmobius.commons.util.Rnd;

//

import com.l2jmobius.gameserver.ai.CtrlEvent;
import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.datatables.SkillTable;
import com.l2jmobius.gameserver.handler.ISkillHandler;
import com.l2jmobius.gameserver.instancemanager.DuelManager;
import com.l2jmobius.gameserver.model.L2Effect;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.L2Skill.SkillType;
import com.l2jmobius.gameserver.model.actor.L2Attackable;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.L2Playable;
import com.l2jmobius.gameserver.model.actor.L2Summon;
import com.l2jmobius.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.skills.Formulas;

public class Continuous implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.BUFF,
		SkillType.DEBUFF,
		SkillType.DOT,
		SkillType.MDOT,
		SkillType.POISON,
		SkillType.BLEED,
		SkillType.HOT,
		SkillType.CPHOT,
		SkillType.MPHOT,
		// L2Skill.SkillType.MANAHEAL,
		// L2Skill.SkillType.MANA_BY_LEVEL,
		SkillType.FEAR,
		SkillType.CONT,
		SkillType.WEAKNESS,
		SkillType.REFLECT,
		SkillType.UNDEAD_DEFENSE,
		SkillType.AGGDEBUFF,
		SkillType.FORCE_BUFF
	};
	private L2Skill _skill;
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill2, L2Object[] targets)
	{
		if (activeChar == null)
		{
			return;
		}
		
		L2PcInstance player = null;
		if (activeChar instanceof L2PcInstance)
		{
			player = (L2PcInstance) activeChar;
		}
		
		if (skill2.getEffectId() != 0)
		{
			final int skillLevel = skill2.getEffectLvl();
			final int skillEffectId = skill2.getEffectId();
			if (skillLevel == 0)
			{
				_skill = SkillTable.getInstance().getInfo(skillEffectId, 1);
			}
			else
			{
				_skill = SkillTable.getInstance().getInfo(skillEffectId, skillLevel);
			}
			
			if (_skill != null)
			{
				skill2 = _skill;
			}
		}
		
		final L2Skill skill = skill2;
		if (skill == null)
		{
			return;
		}
		
		final boolean bss = activeChar.checkBss();
		final boolean sps = activeChar.checkSps();
		final boolean ss = activeChar.checkSs();
		
		for (L2Object target2 : targets)
		{
			L2Character target = (L2Character) target2;
			
			if (target == null)
			{
				continue;
			}
			
			if ((target instanceof L2PcInstance) && (activeChar instanceof L2Playable) && skill.isOffensive())
			{
				final L2PcInstance _char = (activeChar instanceof L2PcInstance) ? (L2PcInstance) activeChar : ((L2Summon) activeChar).getOwner();
				final L2PcInstance _attacked = (L2PcInstance) target;
				if ((_attacked.getClanId() != 0) && (_char.getClanId() != 0) && (_attacked.getClanId() == _char.getClanId()) && (_attacked.getPvpFlag() == 0))
				{
					continue;
				}
				if ((_attacked.getAllyId() != 0) && (_char.getAllyId() != 0) && (_attacked.getAllyId() == _char.getAllyId()) && (_attacked.getPvpFlag() == 0))
				{
					continue;
				}
			}
			
			if ((skill.getSkillType() != SkillType.BUFF) && (skill.getSkillType() != SkillType.HOT) && (skill.getSkillType() != SkillType.CPHOT) && (skill.getSkillType() != SkillType.MPHOT) && (skill.getSkillType() != SkillType.UNDEAD_DEFENSE) && (skill.getSkillType() != SkillType.AGGDEBUFF) && (skill.getSkillType() != SkillType.CONT))
			{
				if (target.reflectSkill(skill))
				{
					target = activeChar;
				}
			}
			
			// Walls and Door should not be buffed
			if ((target instanceof L2DoorInstance) && ((skill.getSkillType() == SkillType.BUFF) || (skill.getSkillType() == SkillType.HOT)))
			{
				continue;
			}
			
			// Anti-Buff Protection prevents you from getting buffs by other players
			if ((activeChar instanceof L2Playable) && (target != activeChar) && target.isBuffProtected() && !skill.isHeroSkill() && ((skill.getSkillType() == SkillType.BUFF) || (skill.getSkillType() == SkillType.HEAL_PERCENT) || (skill.getSkillType() == SkillType.FORCE_BUFF) || (skill.getSkillType() == SkillType.MANAHEAL_PERCENT) || (skill.getSkillType() == SkillType.COMBATPOINTHEAL) || (skill.getSkillType() == SkillType.REFLECT)))
			{
				continue;
			}
			
			// Player holding a cursed weapon can't be buffed and can't buff
			if (skill.getSkillType() == SkillType.BUFF)
			{
				if (target != activeChar)
				{
					if ((target instanceof L2PcInstance) && ((L2PcInstance) target).isCursedWeaponEquiped())
					{
						continue;
					}
					else if ((player != null) && player.isCursedWeaponEquiped())
					{
						continue;
					}
				}
			}
			
			// Possibility of a lethal strike
			if (!target.isRaid() && (!(target instanceof L2NpcInstance) || (((L2NpcInstance) target).getNpcId() != 35062)))
			{
				final int chance = Rnd.get(1000);
				Formulas.getInstance();
				if ((skill.getLethalChance2() > 0) && (chance < Formulas.calcLethal(activeChar, target, skill.getLethalChance2())))
				{
					if (target instanceof L2NpcInstance)
					{
						target.reduceCurrentHp(target.getCurrentHp() - 1, activeChar);
						activeChar.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE));
					}
				}
				else
				{
					Formulas.getInstance();
					if ((skill.getLethalChance1() > 0) && (chance < Formulas.calcLethal(activeChar, target, skill.getLethalChance1())))
					{
						if (target instanceof L2NpcInstance)
						{
							target.reduceCurrentHp(target.getCurrentHp() / 2, activeChar);
							activeChar.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE));
						}
					}
				}
			}
			
			if (skill.isOffensive())
			{
				final boolean acted = Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss);
				
				if (!acted)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.ATTACK_FAILED));
					continue;
				}
			}
			else if (skill.getSkillType() == SkillType.BUFF)
			{
				if (!Formulas.getInstance().calcBuffSuccess(target, skill))
				{
					if (player != null)
					{
						final SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
						sm.addString(target.getName());
						sm.addSkillName(skill.getDisplayId());
						activeChar.sendPacket(sm);
					}
					continue;
				}
			}
			
			if (skill.isToggle())
			{
				boolean stopped = false;
				
				final L2Effect[] effects = target.getAllEffects();
				if (effects != null)
				{
					for (L2Effect e : effects)
					{
						if (e != null)
						{
							if (e.getSkill().getId() == skill.getId())
							{
								e.exit(false);
								stopped = true;
							}
						}
					}
				}
				
				if (stopped)
				{
					break;
				}
			}
			
			// If target is not in game anymore...
			if ((target instanceof L2PcInstance) && (((L2PcInstance) target).isOnline() == 0))
			{
				continue;
			}
			
			// if this is a debuff let the duel manager know about it so the debuff can be removed after the duel (player & target must be in the same duel)
			if ((target instanceof L2PcInstance) && (player != null) && ((L2PcInstance) target).isInDuel() && ((skill.getSkillType() == SkillType.DEBUFF) || (skill.getSkillType() == SkillType.BUFF)) && (player.getDuelId() == ((L2PcInstance) target).getDuelId()))
			{
				DuelManager dm = DuelManager.getInstance();
				if (dm != null)
				{
					final L2Effect[] effects = skill.getEffects(activeChar, target, ss, sps, bss);
					if (effects != null)
					{
						for (L2Effect buff : effects)
						{
							if (buff != null)
							{
								dm.onBuff(((L2PcInstance) target), buff);
							}
						}
					}
				}
			}
			else
			{
				skill.getEffects(activeChar, target, ss, sps, bss);
			}
			
			if (skill.getSkillType() == SkillType.AGGDEBUFF)
			{
				if (target instanceof L2Attackable)
				{
					target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, (int) skill.getPower());
				}
				else if (target instanceof L2Playable)
				{
					if (target.getTarget() == activeChar)
					{
						target.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, activeChar);
					}
					else
					{
						target.setTarget(activeChar);
					}
				}
			}
			
			if (target.isDead() && (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_AREA_CORPSE_MOB) && (target instanceof L2NpcInstance))
			{
				((L2NpcInstance) target).endDecayTask();
			}
		}
		
		if (!skill.isToggle())
		{
			if (skill.isMagic() && skill.useSpiritShot())
			{
				if (bss)
				{
					activeChar.removeBss();
				}
				else if (sps)
				{
					activeChar.removeSps();
				}
			}
			else if (skill.useSoulShot())
			{
				activeChar.removeSs();
			}
		}
		
		skill.getEffectsSelf(activeChar);
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
