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

import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.model.actor.instance.PetInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.util.IllegalPlayerAction;
import org.l2jmobius.gameserver.util.Util;

public class RequestGiveItemToPet extends GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(RequestGetItemFromPet.class.getName());
	
	private int _objectId;
	private int _amount;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_amount = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final PlayerInstance player = getClient().getPlayer();
		if ((player == null) || !(player.getPet() instanceof PetInstance))
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("giveitemtopet"))
		{
			player.sendMessage("You give items to pet too fast.");
			return;
		}
		
		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TRADE && (player.getKarma() > 0))
		{
			return;
		}
		
		if (player.getPrivateStoreType() != 0)
		{
			player.sendMessage("Cannot exchange items while trading");
			return;
		}
		
		if (player.isCastingNow() || player.isCastingPotionNow())
		{
			return;
		}
		
		if (player.getActiveEnchantItem() != null)
		{
			Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " Tried To Use Enchant Exploit And Got Banned!", IllegalPlayerAction.PUNISH_KICKBAN);
			return;
		}
		
		final ItemInstance item = player.getInventory().getItemByObjectId(_objectId);
		
		if (item == null)
		{
			return;
		}
		
		if (item.isAugmented())
		{
			return;
		}
		
		if (!item.isDropable() || !item.isDestroyable() || !item.isTradeable())
		{
			player.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return;
		}
		
		final PetInstance pet = (PetInstance) player.getPet();
		
		if (pet.isDead())
		{
			player.sendPacket(SystemMessageId.CANNOT_GIVE_ITEMS_TO_DEAD_PET);
			return;
		}
		
		if (_amount < 0)
		{
			return;
		}
		
		if (player.transferItem("Transfer", _objectId, _amount, pet.getInventory(), pet) == null)
		{
			LOGGER.warning("Invalid item transfer request: " + pet.getName() + "(pet) --> " + player.getName());
		}
	}
}
