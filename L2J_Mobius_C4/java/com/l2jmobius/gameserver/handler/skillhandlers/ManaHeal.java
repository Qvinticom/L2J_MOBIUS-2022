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
import com.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.skills.Stats;

/**
 * This class ...
 * @version $Revision: 1.1.2.2.2.1 $ $Date: 2005/03/02 15:38:36 $
 */
public class ManaHeal implements ISkillHandler
{
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.handler.IItemHandler#useItem(com.l2jmobius.gameserver.model.L2PcInstance, com.l2jmobius.gameserver.model.L2ItemInstance)
	 */
	private static SkillType[] _skillIds =
	{
		SkillType.MANAHEAL,
		SkillType.MANARECHARGE
	};
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.handler.IItemHandler#useItem(com.l2jmobius.gameserver.model.L2PcInstance, com.l2jmobius.gameserver.model.L2ItemInstance)
	 */
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets, boolean crit)
	{
		for (final L2Object target2 : targets)
		{
			final L2Character target = (L2Character) target2;
			
			final double mp = (skill.getSkillType() == SkillType.MANARECHARGE) ? target.calcStat(Stats.RECHARGE_MP_RATE, skill.getPower(), null, null) : skill.getPower();
			target.setLastHealAmount((int) mp);
			target.setCurrentMp(mp + target.getCurrentMp());
			final StatusUpdate sump = new StatusUpdate(target.getObjectId());
			sump.addAttribute(StatusUpdate.CUR_MP, (int) target.getCurrentMp());
			target.sendPacket(sump);
			
			if ((activeChar instanceof L2PcInstance) && (activeChar != target))
			{
				final SystemMessage sm = new SystemMessage(SystemMessage.S2_MP_RESTORED_BY_S1);
				sm.addString(activeChar.getName());
				sm.addNumber((int) mp);
				target.sendPacket(sm);
			}
			else
			{
				final SystemMessage sm = new SystemMessage(SystemMessage.S1_MP_RESTORED);
				sm.addNumber((int) mp);
				target.sendPacket(sm);
			}
		}
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return _skillIds;
	}
}