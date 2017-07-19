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
import com.l2jmobius.gameserver.datatables.SkillTable;
import com.l2jmobius.gameserver.handler.ISkillHandler;
import com.l2jmobius.gameserver.model.L2Attackable;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2Effect;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.L2Skill.SkillType;
import com.l2jmobius.gameserver.model.L2Summon;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.skills.Formulas;

/**
 * This class ...
 * @version $Revision: 1.1.2.2.2.9 $ $Date: 2005/04/03 15:55:04 $
 */
public class Continuous implements ISkillHandler
{
	private static SkillType[] _skillIds =
	{
		L2Skill.SkillType.BUFF,
		L2Skill.SkillType.DEBUFF,
		L2Skill.SkillType.DOT,
		L2Skill.SkillType.MDOT,
		L2Skill.SkillType.POISON,
		L2Skill.SkillType.BLEED,
		L2Skill.SkillType.HOT,
		L2Skill.SkillType.CPHOT,
		L2Skill.SkillType.MPHOT,
		L2Skill.SkillType.FEAR,
		L2Skill.SkillType.CONT,
		L2Skill.SkillType.WEAKNESS,
		L2Skill.SkillType.REFLECT,
		L2Skill.SkillType.UNDEAD_DEFENSE,
		L2Skill.SkillType.AGGDEBUFF,
		L2Skill.SkillType.NEGATE
	};
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.handler.IItemHandler#useItem(com.l2jmobius.gameserver.model.L2PcInstance, com.l2jmobius.gameserver.model.L2ItemInstance)
	 */
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets, boolean crit)
	{
		boolean ss = false;
		boolean sps = false;
		boolean bss = false;
		
		final L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		if (weaponInst != null)
		{
			if (skill.useSpiritShot())
			{
				if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
				{
					bss = true;
				}
				else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
				{
					sps = true;
				}
			}
			else if (skill.useSoulShot())
			{
				if (weaponInst.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT)
				{
					ss = true;
				}
			}
		}
		// If there is no weapon equipped, check for an active summon.
		else if (activeChar instanceof L2Summon)
		{
			final L2Summon activeSummon = (L2Summon) activeChar;
			if (skill.useSpiritShot())
			{
				if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
				{
					bss = true;
				}
				else if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT)
				{
					sps = true;
				}
			}
			else if (activeSummon.getChargedSoulShot() == L2ItemInstance.CHARGED_SOULSHOT)
			{
				ss = true;
			}
		}
		else if (activeChar instanceof L2NpcInstance)
		{
			bss = ((L2NpcInstance) activeChar).isUsingShot(false);
			ss = ((L2NpcInstance) activeChar).isUsingShot(true);
		}
		
		for (final L2Object target2 : targets)
		{
			L2Character target = (L2Character) target2;
			
			if (skill.isOffensive())
			{
				final boolean acted = Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss);
				if (!acted)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.ATTACK_FAILED));
					continue;
				}
				
				if (skill.hasEffects())
				{
					if (Formulas.getInstance().calculateSkillReflect(skill, activeChar, target))
					{
						target = activeChar;
					}
				}
			}
			
			if (skill.isToggle() || skill.isHotSpringsDisease())
			{
				final L2Effect[] effects = target.getAllEffects();
				if (effects != null)
				{
					for (final L2Effect e : effects)
					{
						if (e == null)
						{
							continue;
						}
						
						if (e.getSkill().getId() == skill.getId())
						{
							if (skill.isToggle())
							{
								e.exit();
								return;
							}
							
							if (e.getSkill().getLevel() < 10)
							{
								skill = SkillTable.getInstance().getInfo(skill.getId(), e.getSkill().getLevel() + 1);
								e.exit();
							}
							break;
						}
					}
				}
			}
			
			// Negate effect
			skill.doNegate(target);
			
			// Do the most important check before sending the message
			if (skill.getEffects(activeChar, target).length > 0)
			{
				if (!skill.isOffensive())
				{
					final SystemMessage smsg = new SystemMessage(SystemMessage.YOU_FEEL_S1_EFFECT);
					smsg.addString(skill.getName());
					target.sendPacket(smsg);
				}
			}
			
			if (skill.getSkillType() == L2Skill.SkillType.AGGDEBUFF)
			{
				if (target instanceof L2Attackable)
				{
					target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, (int) skill.getPower());
				}
			}
		}
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return _skillIds;
	}
}