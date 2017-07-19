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
package com.l2jmobius.gameserver.network.clientpackets;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.FriendRecvMsg;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * Receive Private (Friend) Message - 0xCC Format: c SS S: Message S: Receiving Player
 * @author Tempy
 */
public class RequestSendFriendMsg extends L2GameClientPacket
{
	private static final String _C__CC_REQUESTSENDMSG = "[C] CC RequestSendMsg";
	private static Logger _logChat = Logger.getLogger(RequestSendFriendMsg.class.getName());
	
	private String _message;
	private String _receiver;
	
	@Override
	protected void readImpl()
	{
		_message = readS();
		_receiver = readS();
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if ((_message == null) || _message.isEmpty() || (_message.length() > 300))
		{
			return;
		}
		
		if (activeChar.getFriend(_receiver) == null)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_NOT_FOUND_IN_THE_GAME));
			return;
		}
		
		final L2PcInstance targetPlayer = L2World.getInstance().getPlayer(_receiver);
		if (targetPlayer == null)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_NOT_FOUND_IN_THE_GAME));
			return;
		}
		
		if (Config.LOG_CHAT)
		{
			final LogRecord record = new LogRecord(Level.INFO, _message);
			record.setLoggerName("chat");
			record.setParameters(new Object[]
			{
				"PRIV_MSG",
				"[" + activeChar.getName() + " to " + _receiver + "]"
			});
			_logChat.log(record);
		}
		
		final FriendRecvMsg frm = new FriendRecvMsg(activeChar.getName(), _receiver, _message);
		targetPlayer.sendPacket(frm);
	}
	
	@Override
	public String getType()
	{
		return _C__CC_REQUESTSENDMSG;
	}
}