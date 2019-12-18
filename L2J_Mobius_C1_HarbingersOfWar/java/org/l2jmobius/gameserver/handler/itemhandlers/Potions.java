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
import org.l2jmobius.gameserver.model.Potion;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUser;

public class Potions implements IItemHandler
{
	private static int[] _itemIds = new int[]
	{
		65,
		725,
		727,
		1060,
		1061,
		1073,
		1539,
		1540,
		726,
		728
	};
	
	@Override
	public int useItem(PlayerInstance activeChar, ItemInstance item)
	{
		final Potion potion = new Potion();
		final int itemId = item.getItemId();
		if ((itemId == 65) || (itemId == 725) || (itemId == 727) || (itemId == 1060) || (itemId == 1061) || (itemId == 1539) || (itemId == 1540) || (itemId == 1073))
		{
			final WorldObject oldTarget = activeChar.getTarget();
			activeChar.setTarget(activeChar);
			final MagicSkillUser msu = new MagicSkillUser(activeChar, 2038, 1, 0, 0);
			activeChar.sendPacket(msu);
			activeChar.broadcastPacket(msu);
			activeChar.setTarget(oldTarget);
			potion.setCurrentHpPotion1(activeChar, itemId);
		}
		else if ((itemId == 726) || (itemId == 728))
		{
			final WorldObject oldTarget = activeChar.getTarget();
			activeChar.setTarget(activeChar);
			final MagicSkillUser msu = new MagicSkillUser(activeChar, 2038, 1, 0, 0);
			activeChar.sendPacket(msu);
			activeChar.broadcastPacket(msu);
			activeChar.setTarget(oldTarget);
			potion.setCurrentMpPotion1(activeChar, itemId);
		}
		return 1;
	}
	
	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}
