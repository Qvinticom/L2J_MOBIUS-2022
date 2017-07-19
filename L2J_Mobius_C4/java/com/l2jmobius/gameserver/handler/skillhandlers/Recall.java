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

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.datatables.MapRegionTable;
import com.l2jmobius.gameserver.handler.ISkillHandler;
import com.l2jmobius.gameserver.instancemanager.DimensionalRiftManager;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.L2Skill.SkillType;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class Recall implements ISkillHandler
{
	// private static Logger _log = Logger.getLogger(Recall.class.getName());
	protected SkillType[] _skillIds =
	{
		SkillType.RECALL
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets, boolean crit)
	{
		try
		{
			for (int index = 0; index < targets.length; index++)
			{
				if (!(targets[index] instanceof L2PcInstance))
				{
					continue;
				}
				
				final L2PcInstance target = (L2PcInstance) targets[index];
				
				// If Alternate rule Karma punishment is set to true, forbid skill Recall to player with Karma
				if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT && (target.getKarma() > 0))
				{
					continue;
				}
				
				if (target.isInOlympiadMode())
				{
					target.sendPacket(new SystemMessage(SystemMessage.THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
					continue;
				}
				
				if (target.getPrivateStoreType() != 0)
				{
					continue;
				}
				
				if ((target.isInParty() && target.getParty().isInDimensionalRift()) || DimensionalRiftManager.getInstance().checkIfInRiftZone(target.getX(), target.getY(), target.getZ(), true))
				{
					target.sendMessage("Once a party is ported in another dimension, its members cannot get out of it.");
					return;
				}
				
				// Check to see if the current player target is in a festival.
				
				if (target.isFestivalParticipant())
				
				{
					
					target.sendMessage("You may not use an escape skill in a festival.");
					
					continue;
					
				}
				
				// Check to see if player is in jail
				
				if (target.isInJail())
				
				{
					
					target.sendMessage("You cannot escape from jail.");
					
					continue;
					
				}
				
				if (target.getEventTeam() > 0)
				{
					activeChar.sendMessage("You may not use an escape skill in TvT Event.");
					continue;
				}
				
				target.teleToLocation(MapRegionTable.TeleportWhereType.Town);
				
			}
			
		}
		
		catch (final Throwable e)
		
		{
			if (Config.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return _skillIds;
	}
}