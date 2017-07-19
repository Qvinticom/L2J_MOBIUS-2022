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

import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.skills.Formulas;
import com.l2jmobius.gameserver.templates.StatsSet;

public class L2SkillChargeDmg extends L2Skill
{
	final int num_charges;
	
	public L2SkillChargeDmg(StatsSet set)
	{
		super(set);
		num_charges = set.getInteger("num_charges", getLevel());
	}
	
	@Override
	public boolean checkCondition(L2Character activeChar, boolean itemOrWeapon)
	{
		final L2PcInstance player = (L2PcInstance) activeChar;
		
		if (player.getCharges() < num_charges)
		{
			final SystemMessage sm = new SystemMessage(113);
			sm.addSkillName(getId());
			activeChar.sendPacket(sm);
			return false;
		}
		return super.checkCondition(activeChar, itemOrWeapon);
	}
	
	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		final L2PcInstance player = (L2PcInstance) caster;
		
		if (caster.isAlikeDead())
		{
			return;
		}
		
		// Formula tested by L2Guru
		double modifier = 0;
		modifier = 0.8 + (0.201 * player.getCharges());
		
		player.addCharge(-num_charges);
		
		final L2ItemInstance weapon = caster.getActiveWeaponInstance();
		final boolean soul = ((weapon != null) && (weapon.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT));
		
		for (final L2Object target2 : targets)
		{
			
			final L2Character target = (L2Character) target2;
			if (target.isAlikeDead())
			{
				continue;
			}
			
			final boolean shld = Formulas.getInstance().calcShldUse(caster, target);
			
			double damage = Formulas.getInstance().calcPhysDam(caster, target, this, shld, false, false, soul);
			
			if (damage > 0)
			{
				
				damage = damage * modifier;
				target.reduceCurrentHp(damage, caster);
				
				caster.sendDamageMessage(target, (int) damage, false, false, false);
			}
		}
	}
}
