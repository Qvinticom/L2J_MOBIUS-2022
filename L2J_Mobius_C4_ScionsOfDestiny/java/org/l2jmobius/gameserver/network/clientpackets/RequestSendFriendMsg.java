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

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.FriendRecvMsg;

/**
 * Recieve Private (Friend) Message - 0xCC Format: c SS S: Message S: Receiving Player
 */
public class RequestSendFriendMsg implements IClientIncomingPacket
{
	private static java.util.logging.Logger _logChat = java.util.logging.Logger.getLogger("chat");
	
	private String _message;
	private String _reciever;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_message = packet.readS();
		_reciever = packet.readS();
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
		
		final Player targetPlayer = World.getInstance().getPlayer(_reciever);
		if ((targetPlayer == null) || !targetPlayer.getFriendList().contains(player.getObjectId()))
		{
			player.sendPacket(SystemMessageId.THAT_PLAYER_IS_NOT_ONLINE);
			return;
		}
		
		if (Config.LOG_CHAT)
		{
			final LogRecord record = new LogRecord(Level.INFO, _message);
			record.setLoggerName("chat");
			record.setParameters(new Object[]
			{
				"PRIV_MSG",
				"[" + player.getName() + " to " + _reciever + "]"
			});
			
			_logChat.log(record);
		}
		
		targetPlayer.sendPacket(new FriendRecvMsg(player.getName(), _reciever, _message));
	}
}
