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
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 * @version $Revision: 1.1.2.2.2.9 $ $Date: 2005/04/04 19:08:01 $
 */
public class Charge implements ISkillHandler
{
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.handler.IItemHandler#useItem(com.l2jmobius.gameserver.model.L2PcInstance, com.l2jmobius.gameserver.model.L2ItemInstance)
	 */
	private static SkillType[] _skillIds =
	{
		L2Skill.SkillType.CHARGE
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets, boolean crit)
	{
		
		final L2PcInstance player = (L2PcInstance) activeChar;
		
		if (player.getCharges() >= skill.getMaxCharges())
		{
			player.sendPacket(new SystemMessage(1196));
			return;
		}
		
		player.addCharge(1);
		
		final SystemMessage sm = new SystemMessage(323);
		
		sm.addNumber(player.getCharges());
		
		activeChar.sendPacket(sm);
		
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return _skillIds;
	}
}