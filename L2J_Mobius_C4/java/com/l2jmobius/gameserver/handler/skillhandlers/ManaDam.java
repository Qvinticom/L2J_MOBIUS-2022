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
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.L2Skill.SkillType;
import com.l2jmobius.gameserver.model.L2Summon;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.skills.Formulas;

/**
 * Class handling the Mana damage skill
 * @author slyce
 */
public class ManaDam implements ISkillHandler
{
	private static SkillType[] _skillIds =
	{
		SkillType.MANADAM
	};
	
	/**
	 * @see com.l2jmobius.gameserver.handler.ISkillHandler#useSkill(com.l2jmobius.gameserver.model.L2Character, com.l2jmobius.gameserver.model.L2Skill, com.l2jmobius.gameserver.model.L2Object[], boolean)
	 */
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets, boolean crit)
	{
		if (activeChar.isAlikeDead())
		{
			return;
		}
		
		boolean ss = false;
		boolean bss = false;
		
		final L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		if (weaponInst != null)
		{
			if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
			{
				bss = true;
			}
			else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
			{
				ss = true;
			}
		}
		// If there is no weapon equipped, check for an active summon.
		else if (activeChar instanceof L2Summon)
		{
			final L2Summon activeSummon = (L2Summon) activeChar;
			
			if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
			{
				bss = true;
			}
			else if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT)
			{
				ss = true;
			}
		}
		else if (activeChar instanceof L2NpcInstance)
		{
			ss = ((L2NpcInstance) activeChar).isUsingShot(true);
			bss = ((L2NpcInstance) activeChar).isUsingShot(false);
		}
		
		for (final L2Object target2 : targets)
		{
			final L2Character target = (L2Character) target2;
			if (target.isInvul())
			{
				return;
			}
			
			final double damage = Formulas.getInstance().calcManaDam(activeChar, target, skill, ss, bss);
			if (damage > 0)
			{
				final double mp = (damage > target.getCurrentMp() ? target.getCurrentMp() : damage);
				target.reduceCurrentMp(mp);
				
				if (target instanceof L2PcInstance)
				{
					final StatusUpdate sump = new StatusUpdate(target.getObjectId());
					sump.addAttribute(StatusUpdate.CUR_MP, (int) target.getCurrentMp());
					// [L2J_JP EDIT START - TSL]
					target.sendPacket(sump);
					
					final SystemMessage sm = new SystemMessage(SystemMessage.S2_MP_HAS_BEEN_DRAINED_BY_S1);
					sm.addString(activeChar.getName());
					sm.addNumber((int) mp);
					target.sendPacket(sm);
				}
			}
			
			// [L2J_JP EDIT END - TSL]
		}
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return _skillIds;
	}
}