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

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.gameserver.handler.ISkillHandler;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Pet;
import org.l2jmobius.gameserver.model.skill.Formulas;
import org.l2jmobius.gameserver.model.skill.SkillTargetType;
import org.l2jmobius.gameserver.model.skill.SkillType;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.taskmanager.DecayTaskManager;

public class Resurrect implements ISkillHandler
{
	private static final SkillType[] SKILL_TYPES =
	{
		SkillType.RESURRECT
	};
	
	@Override
	public void useSkill(Creature creature, Skill skill, List<Creature> targets)
	{
		Player player = null;
		if (creature instanceof Player)
		{
			player = (Player) creature;
		}
		
		Creature target = null;
		Player targetPlayer;
		final List<Creature> targetToRes = new ArrayList<>();
		for (WorldObject target2 : targets)
		{
			if (target2 == null)
			{
				continue;
			}
			
			target = (Creature) target2;
			if (target instanceof Player)
			{
				targetPlayer = (Player) target;
				
				// Check for same party or for same clan, if target is for clan.
				if ((skill.getTargetType() == SkillTargetType.CORPSE_CLAN) && ((player == null) || (player.getClanId() != targetPlayer.getClanId())))
				{
					continue;
				}
			}
			
			if (target.isSpawned())
			{
				targetToRes.add(target);
			}
		}
		
		if (targetToRes.isEmpty())
		{
			creature.abortCast();
			creature.sendPacket(SystemMessage.sendString("No valid target to resurrect"));
		}
		
		for (Creature c : targetToRes)
		{
			if (creature instanceof Player)
			{
				if (c instanceof Player)
				{
					((Player) c).reviveRequest((Player) creature, skill, false);
				}
				else if (c instanceof Pet)
				{
					if (((Pet) c).getOwner() == creature)
					{
						c.doRevive(Formulas.getInstance().calculateSkillResurrectRestorePercent(skill.getPower(), creature));
					}
					else
					{
						((Pet) c).getOwner().reviveRequest((Player) creature, skill, true);
					}
				}
				else
				{
					c.doRevive(Formulas.getInstance().calculateSkillResurrectRestorePercent(skill.getPower(), creature));
				}
			}
			else
			{
				DecayTaskManager.getInstance().cancelDecayTask(c);
				c.doRevive(Formulas.getInstance().calculateSkillResurrectRestorePercent(skill.getPower(), creature));
			}
		}
		
		if (skill.isMagic() && skill.useSpiritShot())
		{
			if (creature.checkBss())
			{
				creature.removeBss();
			}
			if (creature.checkSps())
			{
				creature.removeSps();
			}
		}
		else if (skill.useSoulShot())
		{
			if (creature.checkSs())
			{
				creature.removeSs();
			}
		}
	}
	
	@Override
	public SkillType[] getSkillTypes()
	{
		return SKILL_TYPES;
	}
}
