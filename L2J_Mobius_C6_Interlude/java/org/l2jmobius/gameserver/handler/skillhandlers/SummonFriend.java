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
import org.l2jmobius.gameserver.instancemanager.GrandBossManager;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.skill.SkillType;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ConfirmDlg;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.Util;

public class SummonFriend implements ISkillHandler
{
	private static final SkillType[] SKILL_TYPES =
	{
		SkillType.SUMMON_FRIEND
	};
	
	@Override
	public void useSkill(Creature creature, Skill skill, List<Creature> targets)
	{
		if (!(creature instanceof Player))
		{
			return;
		}
		
		final Player activePlayer = creature.getActingPlayer();
		if (!Player.checkSummonerStatus(activePlayer))
		{
			return;
		}
		
		if (activePlayer.isInOlympiadMode())
		{
			activePlayer.sendPacket(SystemMessageId.YOU_CANNOT_USE_THAT_ITEM_IN_A_GRAND_OLYMPIAD_GAMES_MATCH);
			return;
		}
		
		if (activePlayer.isOnEvent())
		{
			activePlayer.sendMessage("You cannot use this skill in an Event.");
			return;
		}
		
		// Checks summoner not in siege zone.
		if (activePlayer.isInsideZone(ZoneId.SIEGE))
		{
			activePlayer.sendMessage("You cannot summon in a siege zone.");
			return;
		}
		
		// Checks summoner not in arenas, siege zones, jail.
		if (activePlayer.isInsideZone(ZoneId.PVP))
		{
			activePlayer.sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_DURING_COMBAT);
			return;
		}
		
		if ((GrandBossManager.getInstance().getZone(activePlayer) != null) && !activePlayer.isGM())
		{
			activePlayer.sendPacket(SystemMessageId.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
			return;
		}
		
		try
		{
			for (WorldObject wo : targets)
			{
				if (!(wo instanceof Creature))
				{
					continue;
				}
				
				final Creature target = (Creature) wo;
				if (creature == target)
				{
					continue;
				}
				
				if (target instanceof Player)
				{
					final Player targetPlayer = target.getActingPlayer();
					if (!Player.checkSummonTargetStatus(targetPlayer, activePlayer))
					{
						continue;
					}
					
					if (targetPlayer.isAlikeDead())
					{
						final SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED);
						sm.addString(targetPlayer.getName());
						activePlayer.sendPacket(sm);
						continue;
					}
					
					if (targetPlayer.isInStoreMode())
					{
						final SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_CURRENTLY_TRADING_OR_OPERATING_A_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED);
						sm.addString(targetPlayer.getName());
						activePlayer.sendPacket(sm);
						continue;
					}
					
					// Target cannot be in combat (or dead, but that's checked by TARGET_PARTY).
					if (targetPlayer.isRooted() || targetPlayer.isInCombat())
					{
						final SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED);
						sm.addString(targetPlayer.getName());
						activePlayer.sendPacket(sm);
						continue;
					}
					
					if ((GrandBossManager.getInstance().getZone(targetPlayer) != null) && !targetPlayer.isGM())
					{
						activePlayer.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
						continue;
					}
					// Check for the the target's festival status.
					if (targetPlayer.isInOlympiadMode())
					{
						activePlayer.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_CURRENTLY_PARTICIPATING_IN_THE_GRAND_OLYMPIAD));
						continue;
					}
					
					// Check for the the target's festival status.
					if (targetPlayer.isFestivalParticipant())
					{
						activePlayer.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
						continue;
					}
					
					// Check for the target's jail status, arenas and siege zones.
					if (targetPlayer.isInsideZone(ZoneId.PVP))
					{
						activePlayer.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
						continue;
					}
					
					// Requires a Summoning Crystal.
					if ((targetPlayer.getInventory().getItemByItemId(8615) == null) && (skill.getId() != 1429)) // KidZor
					{
						activePlayer.sendMessage("Your target cannot be summoned while he hasn't got a Summoning Crystal.");
						targetPlayer.sendMessage("You cannot be summoned while you haven't got a Summoning Crystal.");
						continue;
					}
					
					if (!Util.checkIfInRange(0, activePlayer, targetPlayer, false))
					{
						// Check already summon.
						if (!targetPlayer.teleportRequest(activePlayer, skill))
						{
							final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ALREADY_BEEN_SUMMONED);
							sm.addString(targetPlayer.getName());
							activePlayer.sendPacket(sm);
							continue;
						}
						
						// Summon friend.
						if (skill.getId() == 1403)
						{
							// Send message.
							final ConfirmDlg confirm = new ConfirmDlg(SystemMessageId.S1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId());
							confirm.addString(activePlayer.getName());
							confirm.addZoneName(activePlayer.getX(), activePlayer.getY(), activePlayer.getZ());
							confirm.addTime(30000, targetPlayer);
							confirm.addRequesterId(activePlayer.getObjectId());
							targetPlayer.sendPacket(confirm);
						}
						else
						{
							Player.teleToTarget(targetPlayer, activePlayer, activePlayer.getLocation(), skill);
							targetPlayer.teleportRequest(activePlayer, skill);
						}
					}
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