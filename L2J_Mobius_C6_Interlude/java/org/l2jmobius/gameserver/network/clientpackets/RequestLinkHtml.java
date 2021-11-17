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
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author zabbix Lets drink to code!
 */
public class RequestLinkHtml implements IClientIncomingPacket
{
	private String _link;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_link = packet.readS();
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
		
		if (_link.contains("..") || !_link.contains(".htm"))
		{
			LOGGER.warning("[RequestLinkHtml] hack? link contains prohibited characters: '" + _link + "', skipped");
			return;
		}
		
		if (!player.validateLink(_link))
		{
			return;
		}
		
		final NpcHtmlMessage msg = new NpcHtmlMessage(0);
		msg.setFile(_link);
		
		player.sendPacket(msg);
	}
}
