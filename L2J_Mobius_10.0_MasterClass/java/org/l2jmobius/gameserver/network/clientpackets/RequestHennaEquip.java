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
import org.l2jmobius.gameserver.data.xml.HennaData;
import org.l2jmobius.gameserver.enums.PlayerCondOverride;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.Henna;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.PacketLogger;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.HennaEquipList;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.util.Util;

/**
 * @author Zoey76
 */
public class RequestHennaEquip implements IClientIncomingPacket
{
	private int _symbolId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_symbolId = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (!client.getFloodProtectors().canPerformTransaction())
		{
			return;
		}
		
		final Henna henna = HennaData.getInstance().getHenna(_symbolId);
		if (henna == null)
		{
			PacketLogger.warning("Invalid Henna Id: " + _symbolId + " from " + player);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (henna.isPremium())
		{
			if ((Config.PREMIUM_HENNA_SLOT_ENABLED_FOR_ALL || player.hasPremiumStatus()) && Config.PREMIUM_HENNA_SLOT_ENABLED && (player.getClassId().level() > 1))
			{
				if (player.getHenna(4) != null)
				{
					player.sendPacket(SystemMessageId.YOU_HAVE_NO_FREE_TATTOO_SLOTS);
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
			else
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		else if (player.getHennaEmptySlots() == 0)
		{
			player.sendPacket(SystemMessageId.YOU_HAVE_NO_FREE_TATTOO_SLOTS);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final long count = player.getInventory().getInventoryItemCount(henna.getDyeItemId(), -1);
		if (henna.isAllowedClass(player.getClassId()) && (count >= henna.getWearCount()) && (player.getAdena() >= henna.getWearFee()) && player.addHenna(henna))
		{
			player.destroyItemByItemId("Henna", henna.getDyeItemId(), henna.getWearCount(), player, true);
			player.getInventory().reduceAdena("Henna", henna.getWearFee(), player, player.getLastFolkNPC());
			final InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(player.getInventory().getAdenaInstance());
			player.sendInventoryUpdate(iu);
			player.sendPacket(new HennaEquipList(player));
			player.updateSymbolSealSkills();
			player.sendPacket(SystemMessageId.A_TATTOO_IS_ADDED);
		}
		else
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_MAKE_A_TATTOO);
			if (!player.canOverrideCond(PlayerCondOverride.ITEM_CONDITIONS) && !henna.isAllowedClass(player.getClassId()))
			{
				Util.handleIllegalPlayerAction(player, "Exploit attempt: Character " + player.getName() + " of account " + player.getAccountName() + " tryed to add a forbidden henna.", Config.DEFAULT_PUNISH);
			}
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
}
