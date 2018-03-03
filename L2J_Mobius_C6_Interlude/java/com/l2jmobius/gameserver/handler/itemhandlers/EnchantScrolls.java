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
import com.l2jmobius.gameserver.model.actor.L2Playable;
import com.l2jmobius.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.ChooseInventoryItem;

public class EnchantScrolls implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		729,
		730,
		731,
		732,
		6569,
		6570, // a grade
		947,
		948,
		949,
		950,
		6571,
		6572, // b grade
		951,
		952,
		953,
		954,
		6573,
		6574, // c grade
		955,
		956,
		957,
		958,
		6575,
		6576, // d grade
		959,
		960,
		961,
		962,
		6577,
		6578
		// s grade
	};
	
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
		{
			return;
		}
		
		final L2PcInstance activeChar = (L2PcInstance) playable;
		if (activeChar.isCastingNow() || activeChar.isCastingPotionNow())
		{
			return;
		}
		
		activeChar.setActiveEnchantItem(item);
		activeChar.sendPacket(SystemMessageId.SELECT_ITEM_TO_ENCHANT);
		activeChar.sendPacket(new ChooseInventoryItem(item.getItemId()));
		return;
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
