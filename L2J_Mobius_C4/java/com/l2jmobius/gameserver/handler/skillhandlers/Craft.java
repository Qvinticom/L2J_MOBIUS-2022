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

import com.l2jmobius.gameserver.RecipeController;
import com.l2jmobius.gameserver.handler.ISkillHandler;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.L2Skill.SkillType;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 * @version $Revision: 1.1.2.2.2.4 $ $Date: 2005/04/06 16:13:48 $
 */
public class Craft implements ISkillHandler
{
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.handler.IItemHandler#useItem(com.l2jmobius.gameserver.model.L2PcInstance, com.l2jmobius.gameserver.model.L2ItemInstance)
	 */
	private static SkillType[] _skillIds =
	{
		SkillType.COMMON_CRAFT,
		SkillType.DWARVEN_CRAFT
	};
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.handler.IItemHandler#useItem(com.l2jmobius.gameserver.model.L2PcInstance, com.l2jmobius.gameserver.model.L2ItemInstance)
	 */
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets, boolean crit)
	{
		if ((activeChar == null) || !(activeChar instanceof L2PcInstance))
		{
			return;
		}
		
		final L2PcInstance player = (L2PcInstance) activeChar;
		
		if (player.getPrivateStoreType() != 0)
		{
			player.sendPacket(new SystemMessage(SystemMessage.CANNOT_CREATED_WHILE_ENGAGED_IN_TRADING));
			return;
		}
		RecipeController.getInstance().requestBookOpen(player, (skill.getSkillType() == SkillType.DWARVEN_CRAFT) ? true : false);
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return _skillIds;
	}
}
