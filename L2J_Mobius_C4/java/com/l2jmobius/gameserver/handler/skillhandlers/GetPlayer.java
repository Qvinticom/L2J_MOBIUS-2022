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
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.L2Skill.SkillType;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.TeleportToLocation;
import com.l2jmobius.util.Rnd;

/*
 * Mobs can teleport players to them
 */
public class GetPlayer implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.GET_PLAYER
	};
	
	/**
	 * @see com.l2jmobius.gameserver.handler.ISkillHandler#useSkill(L2Character, L2Skill, L2Object[], boolean)
	 */
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets, boolean crit)
	{
		if (activeChar.isAlikeDead())
		{
			return;
		}
		
		for (final L2Object target : targets)
		{
			if (target instanceof L2PcInstance)
			{
				final L2PcInstance trg = (L2PcInstance) target;
				if (trg.isAlikeDead() || trg.isTeleporting() || trg.inOfflineMode())
				{
					continue;
				}
				
				// Stop movement
				trg.stopMove(null);
				trg.abortAttack();
				trg.abortCast();
				
				trg.setIsTeleporting(true);
				trg.setIsSummoned(true);
				trg.setTarget(null);
				
				final int x = activeChar.getX() + Rnd.get(-10, 10);
				final int y = activeChar.getY() + Rnd.get(-10, 10);
				final int z = activeChar.getZ();
				
				trg.broadcastPacket(new TeleportToLocation(trg, x, y, z));
				trg.setXYZ(x, y, z);
				
				if (trg.getWorldRegion() != null)
				{
					trg.getWorldRegion().revalidateZones(trg);
				}
			}
		}
	}
	
	/**
	 * @see com.l2jmobius.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}