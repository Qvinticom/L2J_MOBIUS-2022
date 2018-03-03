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

import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PetInstance;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.ItemList;
import com.l2jmobius.gameserver.util.IllegalPlayerAction;
import com.l2jmobius.gameserver.util.Util;

public final class RequestGetItemFromPet extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(RequestGetItemFromPet.class.getName());
	
	private int _objectId;
	private int _amount;
	@SuppressWarnings("unused")
	private int _unknown;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_amount = readD();
		_unknown = readD();// = 0 for most trades
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		
		if ((player == null) || (player.getPet() == null) || !(player.getPet() instanceof L2PetInstance))
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("getfrompet"))
		{
			player.sendMessage("You get items from pet too fast.");
			return;
		}
		
		final L2PetInstance pet = (L2PetInstance) player.getPet();
		
		if (player.getActiveEnchantItem() != null)
		{
			Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " Tried To Use Enchant Exploit , And Got Banned!", IllegalPlayerAction.PUNISH_KICKBAN);
			return;
		}
		
		if (_amount < 0)
		{
			player.setAccessLevel(-1);
			Util.handleIllegalPlayerAction(player, "[RequestGetItemFromPet] count < 0! ban! oid: " + _objectId + " owner: " + player.getName(), Config.DEFAULT_PUNISH);
			return;
		}
		else if (_amount == 0)
		{
			return;
		}
		
		if (player.getDistanceSq(pet) > 40000) // 200*200
		{
			player.sendPacket(SystemMessageId.TARGET_TOO_FAR);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (pet.transferItem("Transfer", _objectId, _amount, player.getInventory(), player, pet) == null)
		{
			LOGGER.warning("Invalid item transfer request: " + pet.getName() + "(pet) --> " + player.getName());
		}
		player.sendPacket(new ItemList(player, true));
	}
}
