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
package com.l2jmobius.gameserver.network.clientpackets.compound;

import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.request.CompoundRequest;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket;
import com.l2jmobius.gameserver.network.serverpackets.ExAdenaInvenCount;
import com.l2jmobius.gameserver.network.serverpackets.ExUserInfoInvenWeight;
import com.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jmobius.gameserver.network.serverpackets.compound.ExEnchantFail;
import com.l2jmobius.gameserver.network.serverpackets.compound.ExEnchantOneFail;
import com.l2jmobius.gameserver.network.serverpackets.compound.ExEnchantSucess;
import com.l2jmobius.util.Rnd;

/**
 * @author UnAfraid
 */
public class RequestNewEnchantTry extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		else if (activeChar.isInStoreMode())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_DO_THAT_WHILE_IN_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
			activeChar.sendPacket(ExEnchantOneFail.STATIC_PACKET);
			return;
		}
		else if (activeChar.isProcessingTransaction() || activeChar.isProcessingRequest())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_USE_THIS_SYSTEM_DURING_TRADING_PRIVATE_STORE_AND_WORKSHOP_SETUP);
			activeChar.sendPacket(ExEnchantOneFail.STATIC_PACKET);
			return;
		}
		
		final CompoundRequest request = activeChar.getRequest(CompoundRequest.class);
		if ((request == null) || request.isProcessing())
		{
			activeChar.sendPacket(ExEnchantFail.STATIC_PACKET);
			return;
		}
		
		request.setProcessing(true);
		
		final L2ItemInstance itemOne = request.getItemOne();
		final L2ItemInstance itemTwo = request.getItemTwo();
		if ((itemOne == null) || (itemTwo == null))
		{
			activeChar.sendPacket(ExEnchantFail.STATIC_PACKET);
			activeChar.removeRequest(request.getClass());
			return;
		}
		
		// Lets prevent using same item twice
		if (itemOne.getObjectId() == itemTwo.getObjectId())
		{
			activeChar.sendPacket(new ExEnchantFail(itemOne.getItem().getId(), itemTwo.getItem().getId()));
			activeChar.removeRequest(request.getClass());
			return;
		}
		
		// Combining only same items!
		if (itemOne.getItem().getId() != itemTwo.getItem().getId())
		{
			activeChar.sendPacket(new ExEnchantFail(itemOne.getItem().getId(), itemTwo.getItem().getId()));
			activeChar.removeRequest(request.getClass());
			return;
		}
		
		// Not implemented or not able to merge!
		if ((itemOne.getItem().getCompoundItem() == 0) || (itemOne.getItem().getCompoundChance() == 0))
		{
			activeChar.sendPacket(new ExEnchantFail(itemOne.getItem().getId(), itemTwo.getItem().getId()));
			activeChar.removeRequest(request.getClass());
			return;
		}
		
		final InventoryUpdate iu = new InventoryUpdate();
		final double random = Rnd.nextDouble() * 100;
		
		// Success
		if (random < itemOne.getItem().getCompoundChance())
		{
			iu.addRemovedItem(itemOne);
			iu.addRemovedItem(itemTwo);
			
			if (activeChar.destroyItem("Compound-Item-One", itemOne, null, true) && activeChar.destroyItem("Compound-Item-Two", itemTwo, null, true))
			{
				final L2ItemInstance item = activeChar.addItem("Compound-Result", itemOne.getItem().getCompoundItem(), 1, null, true);
				activeChar.sendPacket(new ExEnchantSucess(item.getItem().getId()));
			}
		}
		else
		{
			iu.addRemovedItem(itemTwo);
			
			// Upon fail we destroy the second item.
			if (activeChar.destroyItem("Compound-Item-Two-Fail", itemTwo, null, true))
			{
				activeChar.sendPacket(new ExEnchantFail(itemOne.getItem().getId(), itemTwo.getItem().getId()));
			}
		}
		
		activeChar.sendPacket(iu);
		activeChar.sendPacket(new ExAdenaInvenCount(activeChar));
		activeChar.sendPacket(new ExUserInfoInvenWeight(activeChar));
		activeChar.removeRequest(request.getClass());
	}
}
