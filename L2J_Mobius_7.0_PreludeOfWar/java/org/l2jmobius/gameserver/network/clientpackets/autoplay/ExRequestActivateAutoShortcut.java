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
package org.l2jmobius.gameserver.network.clientpackets.autoplay;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.Shortcut;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.autoplay.ExActivateAutoShortcut;

/**
 * @author JoeAlisson
 */
public class ExRequestActivateAutoShortcut implements IClientIncomingPacket
{
	private boolean _activate;
	private int _room;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_room = packet.readH();
		_activate = packet.readC() == 1;
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final PlayerInstance player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (!_activate)
		{
			client.sendPacket(new ExActivateAutoShortcut(_room, _activate));
			return;
		}
		
		if (_room == -1)
		{
			// TODO: auto supply
			client.sendPacket(new ExActivateAutoShortcut(_room, _activate));
		}
		else
		{
			final int slot = _room % 12;
			final int page = _room / 12;
			final Shortcut shortcut = player.getShortCut(slot, page);
			if (shortcut != null)
			{
				if ((page == 23) && (slot == 1))
				{
					// auto potion
					final ItemInstance item = player.getInventory().getItemByObjectId(shortcut.getId());
					if ((item == null) || !item.isPotion())
					{
						return;
					}
				}
				
				// TODO: auto skill
				client.sendPacket(new ExActivateAutoShortcut(_room, _activate));
			}
		}
	}
}
