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

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.enums.ShortcutType;
import org.l2jmobius.gameserver.model.Shortcut;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.serverpackets.ShortCutRegister;

public class RequestShortCutReg implements IClientIncomingPacket
{
	private ShortcutType _type;
	private int _id;
	private int _slot;
	private int _page;
	private int _level;
	private int _subLevel;
	private int _characterType; // 1 - player, 2 - pet
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		final int typeId = packet.readD();
		_type = ShortcutType.values()[(typeId < 1) || (typeId > 6) ? 0 : typeId];
		final int slot = packet.readD();
		_slot = slot % 12;
		_page = slot / 12;
		_id = packet.readD();
		_level = packet.readH();
		_subLevel = packet.readH(); // Sublevel
		_characterType = packet.readD();
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
		
		if ((_page > 10) || (_page < 0))
		{
			return;
		}
		
		final Shortcut sc = new Shortcut(_slot, _page, _type, _id, _level, _subLevel, _characterType);
		player.registerShortCut(sc);
		player.sendPacket(new ShortCutRegister(sc));
	}
}
