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
package org.l2jmobius.gameserver.network.clientpackets.commission;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.instancemanager.ItemCommissionManager;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.commission.ExCloseCommission;

/**
 * @author NosBit, Ren
 */
public class RequestCommissionRegister implements IClientIncomingPacket
{
	private int _itemObjectId;
	private long _pricePerUnit;
	private long _itemCount;
	private int _durationType; // -1 = None, 0 = 1 Day, 1 = 3 Days, 2 = 5 Days, 3 = 7 Days, 4 = 15 Days, 5 = 30 Days;
	private int _feeDiscountType; // 0 = none, 1 = 30% discount, 2 = 100% discount;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_itemObjectId = packet.readD();
		packet.readS(); // Item Name they use it for search we will use server side available names.
		_pricePerUnit = packet.readQ();
		_itemCount = packet.readQ();
		_durationType = packet.readD();
		_feeDiscountType = packet.readH();
		// packet.readH(); // Unknown IDS;
		// packet.readD(); // Unknown
		// packet.readD(); // Unknown
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
		
		if ((_feeDiscountType < 0) || (_feeDiscountType > 2))
		{
			LOGGER.warning("Player " + player + " sent incorrect commission discount type: " + _feeDiscountType + ".");
			return;
		}
		
		if ((_feeDiscountType == 1) && (player.getInventory().getItemByItemId(22351) == null))
		{
			LOGGER.warning("Player " + player + ": Auction House Fee 30% Voucher no found in her inventory.");
			return;
		}
		else if ((_feeDiscountType == 2) && (player.getInventory().getItemByItemId(22352) == null))
		{
			LOGGER.warning("Player " + player + ": Auction House Fee 100% Voucher no found in her inventory.");
			return;
		}
		
		if ((_durationType < 0) || (_durationType > 5))
		{
			LOGGER.warning("Player " + player + " sent incorrect commission duration type: " + _durationType + ".");
			return;
		}
		
		if ((_durationType == 4) && (player.getInventory().getItemByItemId(22353) == null))
		{
			
			LOGGER.warning("Player " + player + ": Auction House (15-day) ExtensiΓ³n no found in her inventory.");
			return;
			
		}
		else if ((_durationType == 5) && (player.getInventory().getItemByItemId(22354) == null))
		{
			LOGGER.warning("Player " + player + ": Auction House (30-day) ExtensiΓ³n no found in her inventory.");
			return;
		}
		
		if (!ItemCommissionManager.isPlayerAllowedToInteract(player))
		{
			client.sendPacket(ExCloseCommission.STATIC_PACKET);
			return;
		}
		
		ItemCommissionManager.getInstance().registerItem(player, _itemObjectId, _itemCount, _pricePerUnit, _durationType, (byte) Math.min((_feeDiscountType * 30) * _feeDiscountType, 100));
	}
}
