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
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUser;
import org.l2jmobius.gameserver.network.serverpackets.SetupGauge;
import org.l2jmobius.gameserver.network.serverpackets.StopMove;

public class ScrollOfEscape implements IItemHandler
{
	private static int[] _itemIds = new int[]
	{
		736
	};
	
	@Override
	public int useItem(PlayerInstance activeChar, ItemInstance item)
	{
		final int[] townCords = MapRegionTable.getInstance().getClosestTownCords(activeChar);
		activeChar.setTarget(activeChar);
		final Skill skill = SkillTable.getInstance().getInfo(1050, 1);
		final MagicSkillUser msk = new MagicSkillUser(activeChar, 1050, 1, 20000, 0);
		activeChar.sendPacket(msk);
		activeChar.broadcastPacket(msk);
		activeChar.sendPacket(new SetupGauge(SetupGauge.BLUE, skill.getSkillTime()));
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
		final StopMove sm = new StopMove(activeChar);
		activeChar.sendPacket(sm);
		activeChar.broadcastPacket(sm);
		final ActionFailed af = new ActionFailed();
		activeChar.sendPacket(af);
		activeChar.teleToLocation(townCords[0], townCords[1], townCords[2]);
		
		return 1;
	}
	
	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}
