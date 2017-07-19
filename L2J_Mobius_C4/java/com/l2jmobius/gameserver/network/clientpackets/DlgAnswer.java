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

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Dezmond_snz Format: cddd
 */
public class DlgAnswer extends L2GameClientPacket
{
	private static final String _C__C5_DLGANSWER = "[C] C5 DlgAnswer";
	private static Logger _log = Logger.getLogger(DlgAnswer.class.getName());
	
	private int _messageId;
	private int _answer;
	
	@Override
	protected void readImpl()
	{
		_messageId = readD();
		_answer = readD();
	}
	
	@Override
	public void runImpl()
	{
		if (Config.DEBUG)
		{
			_log.fine(getType() + ": Answer acepted. Message ID " + _messageId + ", asnwer " + _answer);
		}
		
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (_messageId == SystemMessage.RESSURECTION_REQUEST)
		{
			activeChar.ReviveAnswer(_answer);
		}
		else if (_messageId == 1140)
		{
			activeChar.gatesAnswer(_answer, 1);
		}
		else if (_messageId == 1141)
		{
			activeChar.gatesAnswer(_answer, 0);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__C5_DLGANSWER;
	}
}
