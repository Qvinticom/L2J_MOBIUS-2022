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
import org.l2jmobius.gameserver.templates.L2Weapon;

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
		int SoulshotId = item.getItemId();
		L2Weapon weapon = activeChar.getActiveWeapon();
		if (weapon == null)
		{
			activeChar.sendPacket(new SystemMessage(339));
			return 0;
		}
		int grade = weapon.getCrystalType();
		int soulShotConsumption = weapon.getSoulShotCount();
		int count = item.getCount();
		if (soulShotConsumption == 0)
		{
			activeChar.sendPacket(new SystemMessage(339));
			return 0;
		}
		if (((grade == 1) && (SoulshotId != 1835)) || ((grade == 2) && (SoulshotId != 1463)) || ((grade == 3) && (SoulshotId != 1464)) || ((grade == 4) && (SoulshotId != 1465)) || ((grade == 5) && (SoulshotId != 1466)) || ((grade == 6) && (SoulshotId != 1467)))
		{
			activeChar.sendPacket(new SystemMessage(337));
			return 0;
		}
		if (count < soulShotConsumption)
		{
			activeChar.sendPacket(new SystemMessage(338));
			return 0;
		}
		activeChar.setActiveSoulshotGrade(grade);
		activeChar.sendPacket(new SystemMessage(342));
		WorldObject OldTarget = activeChar.getTarget();
		activeChar.setTarget(activeChar);
		MagicSkillUser MSU = new MagicSkillUser(activeChar, 2039, 1, 0, 0);
		activeChar.sendPacket(MSU);
		activeChar.broadcastPacket(MSU);
		activeChar.setTarget(OldTarget);
		return soulShotConsumption;
	}
	
	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}
