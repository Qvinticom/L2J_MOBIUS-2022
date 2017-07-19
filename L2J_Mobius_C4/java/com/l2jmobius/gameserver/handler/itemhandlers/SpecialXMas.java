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
package com.l2jmobius.gameserver.handler.itemhandlers;

import com.l2jmobius.gameserver.handler.IItemHandler;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jmobius.gameserver.network.serverpackets.ShowXMasSeal;

/**
 * @author devScarlet & mrTJO
 */
public class SpecialXMas implements IItemHandler
{
	private static int[] _itemIds =
	{
		5555
	};
	
	@Override
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
		{
			return;
		}
		final L2PcInstance activeChar = (L2PcInstance) playable;
		final int itemId = item.getItemId();
		
		if (itemId == 5555) // Token of Love
		{
			final ShowXMasSeal SXS = new ShowXMasSeal(5555);
			activeChar.broadcastPacket(SXS);
		}
	}
	
	/**
	 * @see com.l2jmobius.gameserver.handler.IItemHandler#getItemIds()
	 */
	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}
