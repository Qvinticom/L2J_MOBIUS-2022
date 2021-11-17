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
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.handler.ISkillHandler;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.Skill.SkillType;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.instance.Chest;
import org.l2jmobius.gameserver.model.actor.instance.Door;
import org.l2jmobius.gameserver.model.skills.Formulas;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.SocialAction;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class Unlock implements ISkillHandler
{
	private static final SkillType[] SKILL_TYPES =
	{
		SkillType.UNLOCK
	};
	
	@Override
	public void useSkill(Creature creature, Skill skill, List<Creature> targets)
	{
		final List<Creature> targetList = skill.getTargetList(creature);
		if (targetList == null)
		{
			return;
		}
		
		for (WorldObject element : targetList)
		{
			final WorldObject target = element;
			final boolean success = Formulas.getInstance().calculateUnlockChance(skill);
			if (target instanceof Door)
			{
				final Door door = (Door) target;
				if (!door.isUnlockable())
				{
					creature.sendPacket(new SystemMessage(SystemMessageId.THIS_DOOR_CANNOT_BE_UNLOCKED));
					creature.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				
				if (success && (!door.isOpen()))
				{
					door.openMe();
					door.onOpen();
					creature.sendMessage("Unlock the door!");
				}
				else
				{
					creature.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_UNLOCK_THE_DOOR));
				}
			}
			else if (target instanceof Chest)
			{
				final Chest chest = (Chest) element;
				if ((chest.getCurrentHp() <= 0) || chest.isInteracted())
				{
					creature.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				
				int chestChance = 0;
				int chestGroup = 0;
				int chestTrapLimit = 0;
				if (chest.getLevel() > 60)
				{
					chestGroup = 4;
				}
				else if (chest.getLevel() > 40)
				{
					chestGroup = 3;
				}
				else if (chest.getLevel() > 30)
				{
					chestGroup = 2;
				}
				else
				{
					chestGroup = 1;
				}
				
				switch (chestGroup)
				{
					case 1:
					{
						if (skill.getLevel() > 10)
						{
							chestChance = 100;
						}
						else if (skill.getLevel() >= 3)
						{
							chestChance = 50;
						}
						else if (skill.getLevel() == 2)
						{
							chestChance = 45;
						}
						else if (skill.getLevel() == 1)
						{
							chestChance = 40;
						}
						chestTrapLimit = 10;
						break;
					}
					case 2:
					{
						if (skill.getLevel() > 12)
						{
							chestChance = 100;
						}
						else if (skill.getLevel() >= 7)
						{
							chestChance = 50;
						}
						else if (skill.getLevel() == 6)
						{
							chestChance = 45;
						}
						else if (skill.getLevel() == 5)
						{
							chestChance = 40;
						}
						else if (skill.getLevel() == 4)
						{
							chestChance = 35;
						}
						else if (skill.getLevel() == 3)
						{
							chestChance = 30;
						}
						chestTrapLimit = 30;
						break;
					}
					case 3:
					{
						if (skill.getLevel() >= 14)
						{
							chestChance = 50;
						}
						else if (skill.getLevel() == 13)
						{
							chestChance = 45;
						}
						else if (skill.getLevel() == 12)
						{
							chestChance = 40;
						}
						else if (skill.getLevel() == 11)
						{
							chestChance = 35;
						}
						else if (skill.getLevel() == 10)
						{
							chestChance = 30;
						}
						else if (skill.getLevel() == 9)
						{
							chestChance = 25;
						}
						else if (skill.getLevel() == 8)
						{
							chestChance = 20;
						}
						else if (skill.getLevel() == 7)
						{
							chestChance = 15;
						}
						else if (skill.getLevel() == 6)
						{
							chestChance = 10;
						}
						chestTrapLimit = 50;
						break;
					}
					case 4:
					{
						if (skill.getLevel() >= 14)
						{
							chestChance = 50;
						}
						else if (skill.getLevel() == 13)
						{
							chestChance = 45;
						}
						else if (skill.getLevel() == 12)
						{
							chestChance = 40;
						}
						else if (skill.getLevel() == 11)
						{
							chestChance = 35;
						}
						chestTrapLimit = 80;
						break;
					}
				}
				
				if (Rnd.get(100) <= chestChance)
				{
					creature.broadcastPacket(new SocialAction(creature.getObjectId(), 3));
					chest.setSpecialDrop();
					chest.setMustRewardExpSp(false);
					chest.setInteracted();
					chest.reduceCurrentHp(99999999, creature);
				}
				else
				{
					creature.broadcastPacket(new SocialAction(creature.getObjectId(), 13));
					if (Rnd.get(100) < chestTrapLimit)
					{
						chest.chestTrap(creature);
					}
					chest.setInteracted();
					chest.addDamageHate(creature, 0, 1);
					chest.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, creature);
				}
			}
		}
	}
	
	@Override
	public SkillType[] getSkillTypes()
	{
		return SKILL_TYPES;
	}
}