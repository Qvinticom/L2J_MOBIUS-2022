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
import org.l2jmobius.gameserver.model.items.instance.Item;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.commission.ExCloseCommission;
import org.l2jmobius.gameserver.network.serverpackets.commission.ExResponseCommissionInfo;

/**
 * @author NosBit
 */
public class RequestCommissionInfo implements IClientIncomingPacket
{
	private int _itemObjectId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_itemObjectId = packet.readD();
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
		
		if (!ItemCommissionManager.isPlayerAllowedToInteract(player))
		{
			client.sendPacket(ExCloseCommission.STATIC_PACKET);
			return;
		}
		
		final Item itemInstance = player.getInventory().getItemByObjectId(_itemObjectId);
		if (itemInstance != null)
		{
			client.sendPacket(player.getLastCommissionInfos().getOrDefault(itemInstance.getId(), ExResponseCommissionInfo.EMPTY));
		}
		else
		{
			client.sendPacket(ExResponseCommissionInfo.EMPTY);
		}
	}
}
