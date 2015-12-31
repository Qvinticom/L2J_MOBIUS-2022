/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.itemhandlers;

import com.l2jmobius.gameserver.handler.IItemHandler;
import com.l2jmobius.gameserver.model.actor.L2Playable;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.request.EnchantItemAttributeRequest;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.ExChangeAttributeItemList;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Erlandys
 */
public class ChangeAttribute implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_THIS_ITEM);
			return false;
		}
		
		final L2PcInstance activeChar = playable.getActingPlayer();
		if (activeChar.isCastingNow())
		{
			return false;
		}
		
		if (activeChar.hasItemRequest())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CHANGING_ATTRIBUTES_IS_IN_PROGRESS_PLEASE_TRY_AGAIN_AFTER_ENDING_THE_PREVIOUS_TASK));
			return false;
		}
		
		activeChar.addRequest(new EnchantItemAttributeRequest(activeChar, item.getObjectId()));
		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CHANGING_ATTRIBUTES_IS_IN_PROGRESS_PLEASE_TRY_AGAIN_AFTER_ENDING_THE_PREVIOUS_TASK));
		activeChar.sendPacket(new ExChangeAttributeItemList(activeChar, item.getObjectId()));
		return true;
	}
}
