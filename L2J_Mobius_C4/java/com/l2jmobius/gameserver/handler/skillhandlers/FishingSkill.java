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

import com.l2jmobius.gameserver.handler.ISkillHandler;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2Fishing;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.L2Skill.SkillType;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.templates.L2Weapon;

public class FishingSkill implements ISkillHandler
{
	// private static Logger _log = Logger.getLogger(SiegeFlag.class.getName());
	protected SkillType[] _skillIds =
	{
		SkillType.PUMPING,
		SkillType.REELING
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets, boolean crit)
	{
		if ((activeChar == null) || !(activeChar instanceof L2PcInstance))
		{
			return;
		}
		
		final L2PcInstance player = (L2PcInstance) activeChar;
		
		final L2Fishing fish = player.getFishCombat();
		if (fish == null)
		{
			if (skill.getSkillType() == SkillType.PUMPING)
			{
				// Pumping skill is available only while fishing
				player.sendPacket(new SystemMessage(SystemMessage.CAN_USE_PUMPING_ONLY_WHILE_FISHING));
			}
			else if (skill.getSkillType() == SkillType.REELING)
			{
				// Reeling skill is available only while fishing
				player.sendPacket(new SystemMessage(SystemMessage.CAN_USE_REELING_ONLY_WHILE_FISHING));
			}
			
			player.sendPacket(new ActionFailed());
			return;
		}
		
		final L2Weapon weaponItem = player.getActiveWeaponItem();
		final L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		
		if ((weaponInst == null) || (weaponItem == null))
		{
			return;
		}
		
		int SS = 1;
		int pen = 0;
		if (weaponInst.getChargedFishshot())
		{
			SS = 2;
		}
		
		final double gradebonus = 1 + (weaponItem.getCrystalType() * 0.1);
		int dmg = (int) (skill.getPower() * gradebonus * SS);
		
		if (player.getSkillLevel(1315) <= (skill.getLevel() - 2)) // 1315 - Fish Expertise
		{
			// Penalty
			
			player.sendPacket(new SystemMessage(SystemMessage.REELING_PUMPING_3_LEVELS_HIGHER_THAN_FISHING_PENALTY));
			
			pen = 50;
			
			final int penatlydmg = dmg - pen;
			if (player.isGM())
			{
				player.sendMessage("Dmg w/o penalty = " + dmg);
			}
			dmg = penatlydmg;
		}
		
		if (SS > 1)
		{
			weaponInst.setChargedFishshot(false);
		}
		
		if (skill.getSkillType() == SkillType.REELING)
		{
			fish.useRealing(dmg, pen);
		}
		else
		{
			fish.usePumping(dmg, pen);
		}
		
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return _skillIds;
	}
}