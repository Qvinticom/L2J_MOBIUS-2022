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
package org.l2jmobius.gameserver.handler.itemhandlers;

import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUser;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.templates.Item;
import org.l2jmobius.gameserver.templates.Weapon;

public class SoulShots implements IItemHandler
{
	private static int[] _itemIds = new int[]
	{
		1835,
		1463,
		1464,
		1465,
		1466,
		1467
	};
	
	@Override
	public int useItem(PlayerInstance activeChar, ItemInstance item)
	{
		if (activeChar.getActiveSoulshotGrade() != 0)
		{
			return 0;
		}
		final int SoulshotId = item.getItemId();
		final Weapon weapon = activeChar.getActiveWeapon();
		if (weapon == null)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_USE_SOULSHOTS));
			return 0;
		}
		final int grade = weapon.getCrystalType();
		final int soulShotConsumption = weapon.getSoulShotCount();
		final int count = item.getCount();
		if (soulShotConsumption == 0)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_USE_SOULSHOTS));
			return 0;
		}
		if (((grade == Item.CRYSTAL_NONE) && (SoulshotId != 1835)) || ((grade == Item.CRYSTAL_D) && (SoulshotId != 1463)) || ((grade == Item.CRYSTAL_C) && (SoulshotId != 1464)) || ((grade == Item.CRYSTAL_B) && (SoulshotId != 1465)) || ((grade == Item.CRYSTAL_A) && (SoulshotId != 1466)) || ((grade == Item.CRYSTAL_S) && (SoulshotId != 1467)))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.SOULSHOTS_GRADE_MISMATCH));
			return 0;
		}
		if (count < soulShotConsumption)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_SOULSHOTS));
			return 0;
		}
		activeChar.setActiveSoulshotGrade(grade);
		activeChar.sendPacket(new SystemMessage(SystemMessage.ENABLED_SOULSHOT));
		final WorldObject oldTarget = activeChar.getTarget();
		activeChar.setTarget(activeChar);
		final MagicSkillUser msu = new MagicSkillUser(activeChar, 2039, 1, 0, 0);
		activeChar.sendPacket(msu);
		activeChar.broadcastPacket(msu);
		activeChar.setTarget(oldTarget);
		return soulShotConsumption;
	}
	
	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}
