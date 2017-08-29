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

import com.l2jmobius.commons.network.PacketReader;
import com.l2jmobius.commons.util.Rnd;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.request.CompoundRequest;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;
import com.l2jmobius.gameserver.network.L2GameClient;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import com.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jmobius.gameserver.network.serverpackets.compound.ExEnchantFail;
import com.l2jmobius.gameserver.network.serverpackets.compound.ExEnchantOneFail;
import com.l2jmobius.gameserver.network.serverpackets.compound.ExEnchantSucess;

/**
 * @author UnAfraid
 */
public class RequestNewEnchantTry implements IClientIncomingPacket
{
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		final L2PcInstance activeChar = client.getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		else if (activeChar.isInStoreMode())
		{
			client.sendPacket(SystemMessageId.YOU_CANNOT_DO_THAT_WHILE_IN_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
			client.sendPacket(ExEnchantOneFail.STATIC_PACKET);
			return;
		}
		else if (activeChar.isProcessingTransaction() || activeChar.isProcessingRequest())
		{
			client.sendPacket(SystemMessageId.YOU_CANNOT_USE_THIS_SYSTEM_DURING_TRADING_PRIVATE_STORE_AND_WORKSHOP_SETUP);
			client.sendPacket(ExEnchantOneFail.STATIC_PACKET);
			return;
		}
		
		final CompoundRequest request = activeChar.getRequest(CompoundRequest.class);
		if ((request == null) || request.isProcessing())
		{
			client.sendPacket(ExEnchantFail.STATIC_PACKET);
			return;
		}
		
		request.setProcessing(true);
		
		final L2ItemInstance itemOne = request.getItemOne();
		final L2ItemInstance itemTwo = request.getItemTwo();
		if ((itemOne == null) || (itemTwo == null))
		{
			client.sendPacket(ExEnchantFail.STATIC_PACKET);
			activeChar.removeRequest(request.getClass());
			return;
		}
		
		// Lets prevent using same item twice
		if (itemOne.getObjectId() == itemTwo.getObjectId())
		{
			client.sendPacket(new ExEnchantFail(itemOne.getItem().getId(), itemTwo.getItem().getId()));
			activeChar.removeRequest(request.getClass());
			return;
		}
		
		// Combining only same items!
		if (itemOne.getItem().getId() != itemTwo.getItem().getId())
		{
			client.sendPacket(new ExEnchantFail(itemOne.getItem().getId(), itemTwo.getItem().getId()));
			activeChar.removeRequest(request.getClass());
			return;
		}
		
		// Not implemented or not able to merge!
		if ((itemOne.getItem().getCompoundItem() == 0) || (itemOne.getItem().getCompoundChance() == 0))
		{
			client.sendPacket(new ExEnchantFail(itemOne.getItem().getId(), itemTwo.getItem().getId()));
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
				client.sendPacket(new ExEnchantSucess(item.getItem().getId()));
			}
		}
		else
		{
			iu.addRemovedItem(itemTwo);
			
			// Upon fail we destroy the second item.
			if (activeChar.destroyItem("Compound-Item-Two-Fail", itemTwo, null, true))
			{
				client.sendPacket(new ExEnchantFail(itemOne.getItem().getId(), itemTwo.getItem().getId()));
			}
		}
		
		activeChar.sendInventoryUpdate(iu);
		activeChar.removeRequest(request.getClass());
	}
}
