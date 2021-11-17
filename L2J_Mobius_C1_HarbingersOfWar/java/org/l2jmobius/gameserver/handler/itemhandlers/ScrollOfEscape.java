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
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUser;
import org.l2jmobius.gameserver.network.serverpackets.SetupGauge;
import org.l2jmobius.gameserver.threads.ThreadPool;

public class ScrollOfEscape implements IItemHandler
{
	private static final int[] ITEM_IDS = new int[]
	{
		736
	};
	
	@Override
	public int useItem(Player activeChar, Item item)
	{
		if (activeChar.isAllSkillsDisabled())
		{
			return 0;
		}
		
		activeChar.disableAllSkills();
		activeChar.setTarget(activeChar);
		final Skill skill = SkillTable.getInstance().getSkill(1050, 1);
		final MagicSkillUser msk = new MagicSkillUser(activeChar, 1050, 1, 20000, 0);
		activeChar.sendPacket(msk);
		activeChar.broadcastPacket(msk);
		activeChar.sendPacket(new SetupGauge(SetupGauge.BLUE, skill.getSkillTime()));
		
		ThreadPool.schedule(() ->
		{
			if (activeChar.isAllSkillsDisabled())
			{
				final int[] townCords = MapRegionTable.getInstance().getClosestTownCords(activeChar);
				activeChar.teleToLocation(townCords[0], townCords[1], townCords[2]);
				activeChar.enableAllSkills();
			}
		}, skill.getSkillTime() > 200 ? skill.getSkillTime() - 200 : 0);
		
		return 1;
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
