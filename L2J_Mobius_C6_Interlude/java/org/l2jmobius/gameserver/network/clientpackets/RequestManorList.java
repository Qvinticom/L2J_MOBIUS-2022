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

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.serverpackets.ExSendManorList;

/**
 * Format: ch c (id) 0xD0 h (subid) 0x08
 * @author l3x
 */
public class RequestManorList implements IClientIncomingPacket
{
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
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
		final List<String> manorsName = new ArrayList<>();
		manorsName.add("gludio");
		manorsName.add("dion");
		manorsName.add("giran");
		manorsName.add("oren");
		manorsName.add("aden");
		manorsName.add("innadril");
		manorsName.add("goddard");
		manorsName.add("rune");
		manorsName.add("schuttgart");
		player.sendPacket(new ExSendManorList(manorsName));
	}
}
