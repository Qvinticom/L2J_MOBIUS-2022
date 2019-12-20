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

import org.l2jmobius.gameserver.handler.ISkillHandler;
import org.l2jmobius.gameserver.instancemanager.GrandBossManager;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.Skill.SkillType;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.entity.event.CTF;
import org.l2jmobius.gameserver.model.entity.event.DM;
import org.l2jmobius.gameserver.model.entity.event.TvT;
import org.l2jmobius.gameserver.model.entity.event.VIP;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ConfirmDlg;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.Util;

public class SummonFriend implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.SUMMON_FRIEND
	};
	
	@Override
	public void useSkill(Creature creature, Skill skill, WorldObject[] targets)
	{
		if (!(creature instanceof PlayerInstance))
		{
			return;
		}
		
		PlayerInstance activePlayer = (PlayerInstance) creature;
		
		if (!PlayerInstance.checkSummonerStatus(activePlayer))
		{
			return;
		}
		
		if (activePlayer.isInOlympiadMode())
		{
			activePlayer.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			return;
		}
		
		if (activePlayer._inEvent)
		{
			activePlayer.sendMessage("You cannot use this skill in Event.");
			return;
		}
		if (activePlayer._inEventCTF && CTF.isStarted())
		{
			activePlayer.sendMessage("You cannot use this skill in Event.");
			return;
		}
		if (activePlayer._inEventDM && DM.is_started())
		{
			activePlayer.sendMessage("You cannot use this skill in Event.");
			return;
		}
		if (activePlayer._inEventTvT && TvT.isStarted())
		{
			activePlayer.sendMessage("You cannot use this skill in Event.");
			return;
		}
		if (activePlayer._inEventVIP && VIP._started)
		{
			activePlayer.sendMessage("You cannot use this skill in Event.");
			return;
		}
		
		// Checks summoner not in siege zone
		if (creature.isInsideZone(ZoneId.SIEGE))
		{
			((PlayerInstance) creature).sendMessage("You cannot summon in siege zone.");
			return;
		}
		
		// Checks summoner not in arenas, siege zones, jail
		if (activePlayer.isInsideZone(ZoneId.PVP))
		{
			activePlayer.sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT);
			return;
		}
		
		if ((GrandBossManager.getInstance().getZone(activePlayer) != null) && !activePlayer.isGM())
		{
			activePlayer.sendPacket(SystemMessageId.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
			return;
		}
		
		try
		{
			for (WorldObject target1 : targets)
			{
				if (!(target1 instanceof Creature))
				{
					continue;
				}
				
				Creature target = (Creature) target1;
				if (creature == target)
				{
					continue;
				}
				
				if (target instanceof PlayerInstance)
				{
					PlayerInstance targetChar = (PlayerInstance) target;
					
					if (!PlayerInstance.checkSummonTargetStatus(targetChar, activePlayer))
					{
						continue;
					}
					
					if (targetChar.isAlikeDead())
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED);
						sm.addString(targetChar.getName());
						creature.sendPacket(sm);
						continue;
					}
					
					if (targetChar._inEvent)
					{
						targetChar.sendMessage("You cannot use this skill in a Event.");
						return;
					}
					if (targetChar._inEventCTF)
					{
						targetChar.sendMessage("You cannot use this skill in a Event.");
						return;
					}
					if (targetChar._inEventDM)
					{
						targetChar.sendMessage("You cannot use this skill in a Event.");
						return;
					}
					if (targetChar._inEventTvT)
					{
						targetChar.sendMessage("You cannot use this skill in a Event.");
						return;
					}
					if (targetChar._inEventVIP)
					{
						targetChar.sendMessage("You cannot use this skill in a Event.");
						return;
					}
					
					if (targetChar.isInStoreMode())
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_CURRENTLY_TRADING_OR_OPERATING_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED);
						sm.addString(targetChar.getName());
						creature.sendPacket(sm);
						continue;
					}
					
					// Target cannot be in combat (or dead, but that's checked by TARGET_PARTY)
					if (targetChar.isRooted() || targetChar.isInCombat())
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED);
						sm.addString(targetChar.getName());
						creature.sendPacket(sm);
						continue;
					}
					
					if ((GrandBossManager.getInstance().getZone(targetChar) != null) && !targetChar.isGM())
					{
						creature.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
						continue;
					}
					// Check for the the target's festival status
					if (targetChar.isInOlympiadMode())
					{
						creature.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_IN_OLYMPIAD));
						continue;
					}
					
					// Check for the the target's festival status
					if (targetChar.isFestivalParticipant())
					{
						creature.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
						continue;
					}
					
					// Check for the target's jail status, arenas and siege zones
					if (targetChar.isInsideZone(ZoneId.PVP))
					{
						creature.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
						continue;
					}
					
					// Requires a Summoning Crystal
					/* if (targetChar.getInventory().getItemByItemId(8615) == null) */
					if ((targetChar.getInventory().getItemByItemId(8615) == null) && (skill.getId() != 1429)) // KidZor
					{
						((PlayerInstance) creature).sendMessage("Your target cannot be summoned while he hasn't got a Summoning Crystal");
						targetChar.sendMessage("You cannot be summoned while you haven't got a Summoning Crystal");
						continue;
					}
					
					if (!Util.checkIfInRange(0, creature, target, false))
					{
						// Check already summon
						if (!targetChar.teleportRequest((PlayerInstance) creature, skill))
						{
							final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_SUMMONED);
							sm.addString(target.getName());
							creature.sendPacket(sm);
							continue;
						}
						
						// Summon friend
						if (skill.getId() == 1403)
						{
							// Send message
							final ConfirmDlg confirm = new ConfirmDlg(SystemMessageId.S1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId());
							confirm.addString(creature.getName());
							confirm.addZoneName(creature.getX(), creature.getY(), creature.getZ());
							confirm.addTime(30000);
							confirm.addRequesterId(creature.getObjectId());
							targetChar.sendPacket(confirm);
						}
						else
						{
							PlayerInstance.teleToTarget(targetChar, (PlayerInstance) creature, skill);
							targetChar.teleportRequest(null, null);
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
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}