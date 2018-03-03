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
package com.l2jmobius.gameserver.network.clientpackets;

import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.ExConfirmVariationGemstone;
import com.l2jmobius.gameserver.templates.item.L2Item;

/**
 * Format:(ch) dddd
 * @author -Wooden-
 */
public final class RequestConfirmGemStone extends L2GameClientPacket
{
	private int _targetItemObjId;
	private int _refinerItemObjId;
	private int _gemstoneItemObjId;
	private int _gemstoneCount;
	
	@Override
	protected void readImpl()
	{
		_targetItemObjId = readD();
		_refinerItemObjId = readD();
		_gemstoneItemObjId = readD();
		_gemstoneCount = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		final L2ItemInstance targetItem = (L2ItemInstance) L2World.getInstance().findObject(_targetItemObjId);
		final L2ItemInstance refinerItem = (L2ItemInstance) L2World.getInstance().findObject(_refinerItemObjId);
		final L2ItemInstance gemstoneItem = (L2ItemInstance) L2World.getInstance().findObject(_gemstoneItemObjId);
		
		if ((targetItem == null) || (refinerItem == null) || (gemstoneItem == null))
		{
			return;
		}
		
		// Make sure the item is a gemstone
		final int gemstoneItemId = gemstoneItem.getItem().getItemId();
		
		if ((gemstoneItemId != 2130) && (gemstoneItemId != 2131))
		{
			activeChar.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}
		
		// Check if the gemstoneCount is sufficant
		final int itemGrade = targetItem.getItem().getItemGrade();
		
		switch (itemGrade)
		{
			case L2Item.CRYSTAL_C:
			{
				if ((_gemstoneCount != 20) || (gemstoneItemId != 2130))
				{
					activeChar.sendPacket(SystemMessageId.GEMSTONE_QUANTITY_IS_INCORRECT);
					return;
				}
				break;
			}
			case L2Item.CRYSTAL_B:
			{
				if ((_gemstoneCount != 30) || (gemstoneItemId != 2130))
				{
					activeChar.sendPacket(SystemMessageId.GEMSTONE_QUANTITY_IS_INCORRECT);
					return;
				}
				break;
			}
			case L2Item.CRYSTAL_A:
			{
				if ((_gemstoneCount != 20) || (gemstoneItemId != 2131))
				{
					activeChar.sendPacket(SystemMessageId.GEMSTONE_QUANTITY_IS_INCORRECT);
					return;
				}
				break;
			}
			case L2Item.CRYSTAL_S:
			{
				if ((_gemstoneCount != 25) || (gemstoneItemId != 2131))
				{
					activeChar.sendPacket(SystemMessageId.GEMSTONE_QUANTITY_IS_INCORRECT);
					return;
				}
				break;
			}
		}
		
		activeChar.sendPacket(new ExConfirmVariationGemstone(_gemstoneItemObjId, _gemstoneCount));
		activeChar.sendPacket(SystemMessageId.PRESS_THE_AUGMENT_BUTTON_TO_BEGIN);
	}
}
