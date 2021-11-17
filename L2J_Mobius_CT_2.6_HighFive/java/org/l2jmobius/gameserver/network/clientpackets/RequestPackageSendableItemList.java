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

import java.util.Collection;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.items.instance.Item;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.serverpackets.PackageSendableList;

/**
 * @author -Wooden-
 * @author UnAfraid Thanks mrTJO
 */
public class RequestPackageSendableItemList implements IClientIncomingPacket
{
	private int _objectID;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_objectID = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Collection<Item> items = client.getPlayer().getInventory().getAvailableItems(true, true, true);
		client.sendPacket(new PackageSendableList(items, _objectID, client.getPlayer().getAdena()));
	}
}
