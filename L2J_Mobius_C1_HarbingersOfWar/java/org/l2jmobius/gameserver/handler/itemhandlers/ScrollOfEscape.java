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

import org.l2jmobius.gameserver.data.MapRegionTable;
import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUser;
import org.l2jmobius.gameserver.network.serverpackets.SetupGauge;
import org.l2jmobius.gameserver.network.serverpackets.StopMove;
import org.l2jmobius.gameserver.network.serverpackets.TeleportToLocation;

public class ScrollOfEscape implements IItemHandler
{
	private static int[] _itemIds = new int[]
	{
		736
	};
	
	@Override
	public int useItem(PlayerInstance activeChar, ItemInstance item)
	{
		String townCordsString = MapRegionTable.getInstance().getClosestTownCords(activeChar);
		String[] temp = null;
		temp = townCordsString.split("!");
		int townX = Integer.parseInt(temp[0]);
		int townY = Integer.parseInt(temp[1]);
		int townZ = Integer.parseInt(temp[2]);
		activeChar.setTarget(activeChar);
		Skill skill = SkillTable.getInstance().getInfo(1050, 1);
		MagicSkillUser msk = new MagicSkillUser(activeChar, 1050, 1, 20000, 0);
		activeChar.sendPacket(msk);
		activeChar.broadcastPacket(msk);
		SetupGauge sg = new SetupGauge(0, skill.getSkillTime());
		activeChar.sendPacket(sg);
		if (skill.getSkillTime() > 200)
		{
			try
			{
				Thread.sleep(skill.getSkillTime() - 200);
			}
			catch (InterruptedException e)
			{
				// empty catch block
			}
		}
		StopMove sm = new StopMove(activeChar);
		activeChar.sendPacket(sm);
		activeChar.broadcastPacket(sm);
		ActionFailed af = new ActionFailed();
		activeChar.sendPacket(af);
		World.getInstance().removeVisibleObject(activeChar);
		activeChar.removeAllKnownObjects();
		TeleportToLocation teleport = new TeleportToLocation(activeChar, townX, townY, townZ);
		activeChar.sendPacket(teleport);
		activeChar.broadcastPacket(teleport);
		activeChar.setX(townX);
		activeChar.setY(townY);
		activeChar.setZ(townZ);
		try
		{
			Thread.sleep(2000L);
		}
		catch (InterruptedException e)
		{
			// empty catch block
		}
		return 1;
	}
	
	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}
