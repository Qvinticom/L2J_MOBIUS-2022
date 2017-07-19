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

import com.l2jmobius.gameserver.ai.CtrlEvent;
import com.l2jmobius.gameserver.handler.ISkillHandler;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.L2Skill.SkillType;
import com.l2jmobius.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.skills.Formulas;

/**
 * @author _drunk_ TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
public class Spoil implements ISkillHandler
{
	// private static Logger _log = Logger.getLogger(Spoil.class.getName());
	protected SkillType[] _skillIds =
	{
		SkillType.SPOIL
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets, boolean crit)
	{
		if (!(activeChar instanceof L2PcInstance))
		{
			return;
		}
		
		if (targets == null)
		{
			return;
		}
		
		for (int index = 0; index < targets.length; index++)
		{
			if (!(targets[index] instanceof L2MonsterInstance))
			{
				continue;
			}
			
			final L2MonsterInstance target = (L2MonsterInstance) targets[index];
			
			if (target.isSpoil())
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.ALREADY_SPOILED));
				continue;
			}
			
			// SPOIL SYSTEM by Lbaldi
			boolean spoil = false;
			if (target.isDead() == false)
			{
				spoil = Formulas.getInstance().calcMagicSuccess(activeChar, (L2Character) targets[index], skill);
				
				if (spoil)
				{
					target.setSpoil(true);
					target.setIsSpoiledBy(activeChar.getObjectId());
					activeChar.sendPacket(new SystemMessage(SystemMessage.SPOIL_SUCCESS));
				}
				else
				{
					final SystemMessage sm = new SystemMessage(SystemMessage.S1_WAS_UNAFFECTED_BY_S2);
					sm.addString(target.getName());
					sm.addSkillName(skill.getDisplayId());
					activeChar.sendPacket(sm);
				}
				target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar);
			}
		}
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return _skillIds;
	}
}
