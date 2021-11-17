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

import org.l2jmobius.gameserver.handler.ISkillHandler;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.Skill.SkillType;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.skill.Formulas;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * Class handling the Mana damage skill
 * @author slyce
 */
public class Manadam implements ISkillHandler
{
	private static final SkillType[] SKILL_TYPES =
	{
		SkillType.MANADAM
	};
	
	@Override
	public void useSkill(Creature creature, Skill skill, List<Creature> targets)
	{
		Creature target = null;
		if (creature.isAlikeDead())
		{
			return;
		}
		
		final boolean sps = creature.checkSps();
		final boolean bss = creature.checkBss();
		for (WorldObject target2 : targets)
		{
			target = (Creature) target2;
			if (target.reflectSkill(skill))
			{
				target = creature;
			}
			
			if (target == null)
			{
				continue;
			}
			
			final boolean acted = Formulas.getInstance().calcMagicAffected(creature, target, skill);
			if (target.isInvul() || !acted)
			{
				creature.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_MISSED));
			}
			else
			{
				final double damage = Formulas.getInstance().calcManaDam(creature, target, skill, sps, bss);
				final double mp = (damage > target.getCurrentMp() ? target.getCurrentMp() : damage);
				target.reduceCurrentMp(mp);
				
				if ((damage > 0) && target.isSleeping())
				{
					target.stopSleeping(null);
				}
				
				final StatusUpdate sump = new StatusUpdate(target.getObjectId());
				sump.addAttribute(StatusUpdate.CUR_MP, (int) target.getCurrentMp());
				target.sendPacket(sump);
				
				final SystemMessage sm = new SystemMessage(SystemMessageId.S2_S_MP_HAS_BEEN_DRAINED_BY_S1);
				if (creature instanceof Npc)
				{
					sm.addString(((Npc) creature).getName());
				}
				else if (creature instanceof Summon)
				{
					sm.addString(((Summon) creature).getName());
				}
				else
				{
					sm.addString(creature.getName());
				}
				sm.addNumber((int) mp);
				target.sendPacket(sm);
				
				if (creature instanceof Player)
				{
					final SystemMessage sm2 = new SystemMessage(SystemMessageId.YOUR_OPPONENT_S_MP_WAS_REDUCED_BY_S1);
					sm2.addNumber((int) mp);
					creature.sendPacket(sm2);
				}
			}
		}
		
		if (bss)
		{
			creature.removeBss();
		}
		else if (sps)
		{
			creature.removeSps();
		}
	}
	
	@Override
	public SkillType[] getSkillTypes()
	{
		return SKILL_TYPES;
	}
}
