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
	private static int[] _skillIds = new int[]
	{
		3, // Power Strike
		16, // Mortal Blow
		29, // Iron Punch
		56, // Power Shot
		1177, // Wind Strike
		1181, // Flame Strike
	};
	
	@Override
	public void useSkill(PlayerInstance activeChar, Skill skill, WorldObject target)
	{
		// PvP flag.
		final boolean isEnemy = activeChar.isEnemy(target);
		if (isEnemy)
		{
			if (target.getActingPlayer() != null)
			{
				activeChar.updatePvPFlag(1);
			}
		}
		else // TODO: Target handlers.
		{
			return;
		}
		
		if (target instanceof Creature)
		{
			final Creature creature = (Creature) target;
			int mdef = creature.getMagicalDefense();
			if (mdef == 0)
			{
				mdef = 350;
			}
			final int dmg = (int) ((91 * skill.getPower() * Math.sqrt(activeChar.getMagicalAttack())) / mdef);
			final SystemMessage sm = new SystemMessage(SystemMessage.YOU_DID_S1_DMG);
			sm.addNumber(dmg);
			activeChar.sendPacket(sm);
			if (creature.getCurrentHp() > creature.getMaxHp())
			{
				creature.setCurrentHp(creature.getMaxHp());
			}
			creature.reduceCurrentHp(dmg, activeChar);
		}
	}
	
	@Override
	public int[] getSkillIds()
	{
		return _skillIds;
	}
}
