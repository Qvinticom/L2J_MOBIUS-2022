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
package com.l2jmobius.gameserver.skills.l2skills;

import com.l2jmobius.gameserver.instancemanager.SiegeManager;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.actor.instance.L2ArtefactInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.entity.Siege;
import com.l2jmobius.gameserver.templates.StatsSet;

public class L2SkillEngrave extends L2Skill
{
	L2PcInstance player;
	Siege siege;
	
	public L2SkillEngrave(StatsSet set)
	
	{
		super(set);
	}
	
	@Override
	public boolean checkCondition(L2Character activeChar, boolean itemOrWeapon)
	{
		player = (L2PcInstance) activeChar;
		if (player == null)
		{
			return false;
		}
		
		siege = SiegeManager.getInstance().getSiege(player);
		
		if (!player.isSkillDisabled(getId()))
		{
			if (siege == null)
			{
				player.sendMessage("You may only use this skill during a siege.");
				return false;
			}
			
			if ((player.getClan() == null) || !player.isClanLeader())
			{
				player.sendMessage("Only clan leaders may use this skill.");
				return false;
			}
			
			if (siege.getAttackerClan(player.getClan()) == null)
			{
				player.sendMessage("You may only use this skill provided that you are an attacker.");
				return false;
			}
		}
		
		return super.checkCondition(activeChar, itemOrWeapon);
	}
	
	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	
	{
		
		try
		{
			if (targets[0] instanceof L2ArtefactInstance)
			{
				siege.getCastle().engrave(player.getClan(), targets[0].getObjectId());
			}
		}
		catch (final Exception e)
		{
		}
		
	}
}