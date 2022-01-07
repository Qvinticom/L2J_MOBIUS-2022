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

import org.l2jmobius.gameserver.enums.TeleportWhereType;
import org.l2jmobius.gameserver.handler.ISkillHandler;
import org.l2jmobius.gameserver.instancemanager.GrandBossManager;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.skill.SkillType;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class Recall implements ISkillHandler
{
	private static final SkillType[] SKILL_TYPES =
	{
		SkillType.RECALL
	};
	
	@Override
	public void useSkill(Creature creature, Skill skill, List<Creature> targets)
	{
		try
		{
			if (creature instanceof Player)
			{
				final Player instance = (Player) creature;
				if (instance.isInOlympiadMode())
				{
					creature.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_USE_THAT_ITEM_IN_A_GRAND_OLYMPIAD_GAMES_MATCH));
					return;
				}
				
				// Checks summoner not in siege zone.
				if (creature.isInsideZone(ZoneId.SIEGE))
				{
					((Player) creature).sendMessage("You cannot summon in siege zone.");
					return;
				}
				
				if (creature.isInsideZone(ZoneId.PVP))
				{
					creature.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_DURING_COMBAT));
					return;
				}
				
				if ((GrandBossManager.getInstance().getZone(instance) != null) && !instance.isGM())
				{
					instance.sendPacket(SystemMessageId.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
					return;
				}
			}
			
			for (WorldObject target1 : targets)
			{
				if (!(target1 instanceof Creature))
				{
					continue;
				}
				
				final Creature target = (Creature) target1;
				if (target instanceof Player)
				{
					final Player targetChar = (Player) target;
					if (targetChar.isFestivalParticipant())
					{
						targetChar.sendPacket(SystemMessage.sendString("You can't use escape skill in a festival."));
						continue;
					}
					
					if (targetChar.isOnEvent())
					{
						targetChar.sendMessage("You can't use escape skill in an event.");
						continue;
					}
					
					if (targetChar.isInJail())
					{
						targetChar.sendPacket(SystemMessage.sendString("You can't escape from jail."));
						continue;
					}
					
					if (targetChar.isAlikeDead())
					{
						final SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED);
						sm.addString(targetChar.getName());
						creature.sendPacket(sm);
						continue;
					}
					
					if (targetChar.isInStoreMode())
					{
						final SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_CURRENTLY_TRADING_OR_OPERATING_A_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED);
						sm.addString(targetChar.getName());
						creature.sendPacket(sm);
						continue;
					}
					
					if ((GrandBossManager.getInstance().getZone(targetChar) != null) && !targetChar.isGM())
					{
						creature.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
						continue;
					}
					
					if (targetChar.isInOlympiadMode())
					{
						creature.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_CURRENTLY_PARTICIPATING_IN_THE_GRAND_OLYMPIAD));
						continue;
					}
					
					if (targetChar.isInsideZone(ZoneId.PVP))
					{
						creature.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
						continue;
					}
				}
				
				target.teleToLocation(TeleportWhereType.TOWN);
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
		catch (Throwable e)
		{
		}
	}
	
	@Override
	public SkillType[] getSkillTypes()
	{
		return SKILL_TYPES;
	}
}