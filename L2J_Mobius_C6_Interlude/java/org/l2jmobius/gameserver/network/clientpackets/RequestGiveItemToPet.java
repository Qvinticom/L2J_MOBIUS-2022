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
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Pet;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.PacketLogger;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.util.IllegalPlayerAction;
import org.l2jmobius.gameserver.util.Util;

public class RequestGiveItemToPet implements IClientIncomingPacket
{
	private int _objectId;
	private int _amount;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_objectId = packet.readD();
		_amount = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if ((player == null) || !(player.getPet() instanceof Pet))
		{
			return;
		}
		
		if (!client.getFloodProtectors().canPerformTransaction())
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
		
		final Item item = player.getInventory().getItemByObjectId(_objectId);
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
			player.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_THIS_ITEM);
			return;
		}
		
		final Pet pet = (Pet) player.getPet();
		if (pet.isDead())
		{
			player.sendPacket(SystemMessageId.YOUR_PET_IS_MOTIONLESS_AND_ANY_ATTEMPT_YOU_MAKE_TO_GIVE_IT_SOMETHING_GOES_UNRECOGNIZED);
			return;
		}
		
		if (_amount < 0)
		{
			return;
		}
		
		if (player.transferItem("Transfer", _objectId, _amount, pet.getInventory(), pet) == null)
		{
			PacketLogger.warning("Invalid item transfer request: " + pet.getName() + "(pet) --> " + player.getName());
		}
	}
}
