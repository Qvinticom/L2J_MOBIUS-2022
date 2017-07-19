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

import java.util.logging.Logger;

import com.l2jmobius.gameserver.handler.IUserCommandHandler;
import com.l2jmobius.gameserver.handler.UserCommandHandler;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 * @version $Revision: 1.1.2.1.2.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestUserCommand extends L2GameClientPacket
{
	private static final String _C__AA_REQUESTUSERCOMMAND = "[C] aa RequestUserCommand";
	static Logger _log = Logger.getLogger(RequestUserCommand.class.getName());
	
	private int _command;
	
	@Override
	protected void readImpl()
	{
		_command = readD();
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		final IUserCommandHandler handler = UserCommandHandler.getInstance().getUserCommandHandler(_command);
		if (handler != null)
		{
			handler.useUserCommand(_command, getClient().getActiveChar());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__AA_REQUESTUSERCOMMAND;
	}
}