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
package com.l2jmobius.gameserver.network.clientpackets.primeshop;

import com.l2jmobius.commons.network.PacketReader;
import com.l2jmobius.gameserver.data.xml.impl.PrimeShopData;
import com.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import com.l2jmobius.gameserver.network.GameClient;
import com.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import com.l2jmobius.gameserver.network.serverpackets.primeshop.ExBRProductList;

/**
 * @author Gnacik, UnAfraid
 */
public final class RequestBRProductList implements IClientIncomingPacket
{
	private int _type;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_type = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final PlayerInstance player = client.getPlayer();
		if (player != null)
		{
			
			switch (_type)
			{
				case 0: // Home page
				{
					player.sendPacket(new ExBRProductList(player, 0, PrimeShopData.getInstance().getPrimeItems().values()));
					break;
				}
				case 1: // History
				{
					break;
				}
				case 2: // Favorites
				{
					break;
				}
				default:
				{
					LOGGER.warning(player + " send unhandled product list type: " + _type);
					break;
				}
			}
		}
	}
}