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
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jmobius.gameserver.network.serverpackets.Dice;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.util.Broadcast;
import com.l2jmobius.util.Rnd;

/**
 * This class ...
 * @version $Revision: 1.1.4.2 $ $Date: 2005/03/27 15:30:07 $
 */
public class RollingDice implements IItemHandler
{
	private static int[] _itemIds =
	{
		4625,
		4626,
		4627,
		4628
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
		
		if (activeChar.isInOlympiadMode())
		
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return;
		}
		
		if ((itemId == 4625) || (itemId == 4626) || (itemId == 4627) || (itemId == 4628))
		{
			if (!activeChar.getFloodProtectors().getRollDice().tryPerformAction("roll dice"))
			{
				activeChar.sendPacket(new SystemMessage(835));
				return;
			}
			
			final int number = Rnd.get(1, 6);
			
			final Dice d = new Dice(activeChar.getObjectId(), item.getItemId(), number, activeChar.getX() - 30, activeChar.getY() - 30, activeChar.getZ());
			
			Broadcast.toSelfAndKnownPlayers(activeChar, d);
			
			final SystemMessage sm = new SystemMessage(SystemMessage.S1_ROLLED_S2);
			sm.addString(activeChar.getName());
			sm.addNumber(number);
			
			activeChar.sendPacket(sm);
			
			if (activeChar.isInsideZone(L2Character.ZONE_PEACE))
			{
				Broadcast.toKnownPlayers(activeChar, sm);
			}
			else if (activeChar.isInParty())
			{
				activeChar.getParty().broadcastToPartyMembers(activeChar, sm);
			}
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}