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
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.datatables.csv.HennaTable;
import org.l2jmobius.gameserver.datatables.sql.HennaTreeTable;
import org.l2jmobius.gameserver.model.actor.instance.HennaInstance;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.items.Henna;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.ItemList;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.Util;

public class RequestHennaEquip extends GameClientPacket
{
	private int _symbolId;
	
	/**
	 * packet type id 0xbb format: cd
	 */
	@Override
	protected void readImpl()
	{
		_symbolId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final PlayerInstance player = getClient().getPlayer();
		
		if (player == null)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("HennaEquip"))
		{
			return;
		}
		
		final Henna template = HennaTable.getInstance().getTemplate(_symbolId);
		
		if (template == null)
		{
			return;
		}
		
		final HennaInstance temp = new HennaInstance(template);
		int _count = 0;
		
		/*
		 * Prevents henna drawing exploit: 1) talk to SymbolMakerInstance 2) RequestHennaList 3) Don't close the window and go to a GrandMaster and change your subclass 4) Get SymbolMaker range again and press draw You could draw any kind of henna just having the required subclass...
		 */
		boolean cheater = true;
		
		for (HennaInstance h : HennaTreeTable.getInstance().getAvailableHenna(player.getClassId()))
		{
			if (h.getSymbolId() == temp.getSymbolId())
			{
				cheater = false;
				break;
			}
		}
		
		if ((player.getInventory() != null) && (player.getInventory().getItemByItemId(temp.getItemIdDye()) != null))
		{
			_count = player.getInventory().getItemByItemId(temp.getItemIdDye()).getCount();
		}
		
		if (!cheater && (_count >= temp.getAmountDyeRequire()) && (player.getAdena() >= temp.getPrice()) && player.addHenna(temp))
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
			sm.addNumber(temp.getItemIdDye());
			player.sendPacket(sm);
			player.sendPacket(SystemMessageId.SYMBOL_ADDED);
			
			// HennaInfo hi = new HennaInfo(temp,activeChar);
			// player.sendPacket(hi);
			
			player.getInventory().reduceAdena("Henna", temp.getPrice(), player, player.getLastFolkNPC());
			final ItemInstance dyeToUpdate = player.getInventory().destroyItemByItemId("Henna", temp.getItemIdDye(), temp.getAmountDyeRequire(), player, player.getLastFolkNPC());
			
			// update inventory
			final InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(player.getInventory().getAdenaInstance());
			iu.addModifiedItem(dyeToUpdate);
			player.sendPacket(iu);
			
			final ItemList il = new ItemList(getClient().getPlayer(), true);
			sendPacket(il);
		}
		else
		{
			player.sendPacket(SystemMessageId.CANT_DRAW_SYMBOL);
			
			if (!player.isGM() && cheater)
			{
				Util.handleIllegalPlayerAction(player, "Exploit attempt: Character " + player.getName() + " of account " + player.getAccountName() + " tryed to add a forbidden henna.", Config.DEFAULT_PUNISH);
			}
		}
	}
}
