/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.gameserver.handler.skillhandlers;

import org.l2jmobius.gameserver.handler.ISkillHandler;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class DamageSkill implements ISkillHandler
{
	public static final int POWER_STRIKE = 3;
	public static final int WIND_STRIKE = 1177;
	public static final int FLAME_STRIKE = 1181;
	public static final int MORTAL_BLOW = 16;
	public static final int POWER_SHOT = 56;
	public static final int IRON_PUNCH = 29;
	
	private static int[] _skillIds = new int[]
	{
		3,
		1177,
		1181,
		16,
		56,
		29
	};
	
	@Override
	public void useSkill(PlayerInstance activeChar, Skill skill, WorldObject target)
	{
		if (target instanceof Creature)
		{
			Creature targetChar = (Creature) target;
			int mdef = targetChar.getMagicalDefense();
			if (mdef == 0)
			{
				mdef = 350;
			}
			int dmg = (int) ((91 * skill.getPower() * Math.sqrt(activeChar.getMagicalAttack())) / mdef);
			SystemMessage sm = new SystemMessage(35);
			sm.addNumber(dmg);
			activeChar.sendPacket(sm);
			targetChar.reduceCurrentHp(dmg, activeChar);
			if (targetChar.getCurrentHp() > targetChar.getMaxHp())
			{
				targetChar.setCurrentHp(targetChar.getMaxHp());
			}
		}
	}
	
	@Override
	public int[] getSkillIds()
	{
		return _skillIds;
	}
}
