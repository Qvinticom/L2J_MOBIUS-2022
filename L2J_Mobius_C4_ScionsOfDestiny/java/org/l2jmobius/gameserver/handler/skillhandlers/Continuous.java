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
package org.l2jmobius.gameserver.handler.skillhandlers;

import java.util.List;

import org.l2jmobius.commons.util.Rnd;

//

import org.l2jmobius.gameserver.ai.CtrlEvent;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.handler.ISkillHandler;
import org.l2jmobius.gameserver.model.Effect;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.Skill.SkillType;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Attackable;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.actor.instance.Door;
import org.l2jmobius.gameserver.model.skills.Formulas;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class Continuous implements ISkillHandler
{
	private static final SkillType[] SKILL_TYPES =
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
		// Skill.SkillType.MANAHEAL,
		// Skill.SkillType.MANA_BY_LEVEL,
		SkillType.FEAR,
		SkillType.CONT,
		SkillType.WEAKNESS,
		SkillType.REFLECT,
		SkillType.UNDEAD_DEFENSE,
		SkillType.AGGDEBUFF,
		SkillType.FORCE_BUFF
	};
	
	@Override
	public void useSkill(Creature creature, Skill skillValue, List<Creature> targets)
	{
		if (creature == null)
		{
			return;
		}
		
		Player player = null;
		if (creature instanceof Player)
		{
			player = (Player) creature;
		}
		
		Skill usedSkill = skillValue;
		if (usedSkill.getEffectId() != 0)
		{
			final int skillLevel = usedSkill.getEffectLvl();
			final int skillEffectId = usedSkill.getEffectId();
			Skill skill;
			if (skillLevel == 0)
			{
				skill = SkillTable.getInstance().getSkill(skillEffectId, 1);
			}
			else
			{
				skill = SkillTable.getInstance().getSkill(skillEffectId, skillLevel);
			}
			
			if (skill != null)
			{
				usedSkill = skill;
			}
		}
		
		final Skill skill = usedSkill;
		final boolean bss = creature.checkBss();
		final boolean sps = creature.checkSps();
		final boolean ss = creature.checkSs();
		for (WorldObject target2 : targets)
		{
			Creature target = (Creature) target2;
			if (target == null)
			{
				continue;
			}
			
			if ((target instanceof Player) && (creature instanceof Playable) && skill.isOffensive())
			{
				final Player targetChar = (creature instanceof Player) ? (Player) creature : ((Summon) creature).getOwner();
				final Player attacked = (Player) target;
				if ((attacked.getClanId() != 0) && (targetChar.getClanId() != 0) && (attacked.getClanId() == targetChar.getClanId()) && (attacked.getPvpFlag() == 0))
				{
					continue;
				}
				if ((attacked.getAllyId() != 0) && (targetChar.getAllyId() != 0) && (attacked.getAllyId() == targetChar.getAllyId()) && (attacked.getPvpFlag() == 0))
				{
					continue;
				}
			}
			
			if ((skill.getSkillType() != SkillType.BUFF) && (skill.getSkillType() != SkillType.HOT) && (skill.getSkillType() != SkillType.CPHOT) && (skill.getSkillType() != SkillType.MPHOT) && (skill.getSkillType() != SkillType.UNDEAD_DEFENSE) && (skill.getSkillType() != SkillType.AGGDEBUFF) && (skill.getSkillType() != SkillType.CONT) && target.reflectSkill(skill))
			{
				target = creature;
			}
			
			// Walls and doors should not be buffed.
			if ((target instanceof Door) && ((skill.getSkillType() == SkillType.BUFF) || (skill.getSkillType() == SkillType.HOT)))
			{
				continue;
			}
			
			// Anti-Buff Protection prevents you from getting buffs by other players.
			if ((creature instanceof Playable) && (target != creature) && target.isBuffProtected() && !skill.isHeroSkill() && ((skill.getSkillType() == SkillType.BUFF) || (skill.getSkillType() == SkillType.HEAL_PERCENT) || (skill.getSkillType() == SkillType.FORCE_BUFF) || (skill.getSkillType() == SkillType.MANAHEAL_PERCENT) || (skill.getSkillType() == SkillType.COMBATPOINTHEAL) || (skill.getSkillType() == SkillType.REFLECT)))
			{
				continue;
			}
			
			// Possibility of a lethal strike.
			if (!target.isRaid() && (!(target instanceof Npc) || (((Npc) target).getNpcId() != 35062)))
			{
				final int chance = Rnd.get(1000);
				Formulas.getInstance();
				if ((skill.getLethalChance2() > 0) && (chance < Formulas.calcLethal(creature, target, skill.getLethalChance2())))
				{
					if (target instanceof Npc)
					{
						target.reduceCurrentHp(target.getCurrentHp() - 1, creature);
						creature.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE));
					}
				}
				else
				{
					Formulas.getInstance();
					if ((skill.getLethalChance1() > 0) && (chance < Formulas.calcLethal(creature, target, skill.getLethalChance1())) && (target instanceof Npc))
					{
						target.reduceCurrentHp(target.getCurrentHp() / 2, creature);
						creature.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE));
					}
				}
			}
			
			if (skill.isOffensive())
			{
				final boolean acted = Formulas.getInstance().calcSkillSuccess(creature, target, skill, ss, sps, bss);
				if (!acted)
				{
					creature.sendPacket(new SystemMessage(SystemMessageId.YOUR_ATTACK_HAS_FAILED));
					continue;
				}
			}
			else if ((skill.getSkillType() == SkillType.BUFF) && !Formulas.getInstance().calcBuffSuccess(target, skill))
			{
				if (player != null)
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_RESISTED_YOUR_S2);
					sm.addString(target.getName());
					sm.addSkillName(skill.getDisplayId());
					creature.sendPacket(sm);
				}
				continue;
			}
			
			if (skill.isToggle())
			{
				boolean stopped = false;
				
				for (Effect e : target.getAllEffects())
				{
					if ((e != null) && (e.getSkill().getId() == skill.getId()))
					{
						e.exit(false);
						stopped = true;
					}
				}
				
				if (stopped)
				{
					break;
				}
			}
			
			// If target is not in game anymore...
			if ((target instanceof Player) && !((Player) target).isOnline())
			{
				continue;
			}
			
			skill.applyEffects(creature, target, ss, sps, bss);
			
			if (skill.getSkillType() == SkillType.AGGDEBUFF)
			{
				if (target instanceof Attackable)
				{
					target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, creature, (int) skill.getPower());
				}
				else if (target instanceof Playable)
				{
					if (target.getTarget() == creature)
					{
						target.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, creature);
					}
					else
					{
						target.setTarget(creature);
					}
				}
			}
			
			if (target.isDead() && (skill.getTargetType() == Skill.SkillTargetType.TARGET_AREA_CORPSE_MOB) && (target instanceof Npc))
			{
				((Npc) target).endDecayTask();
			}
		}
		
		if (!skill.isToggle())
		{
			if (skill.isMagic() && skill.useSpiritShot())
			{
				if (bss)
				{
					creature.removeBss();
				}
				else if (sps)
				{
					creature.removeSps();
				}
			}
			else if (skill.useSoulShot())
			{
				creature.removeSs();
			}
		}
		
		skill.applySelfEffects(creature);
	}
	
	@Override
	public SkillType[] getSkillTypes()
	{
		return SKILL_TYPES;
	}
}
