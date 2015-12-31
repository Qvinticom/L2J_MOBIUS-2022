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

import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.PledgeCrest;

/**
 * @author Mobius
 */
public final class RequestPledgeCrest extends L2GameClientPacket
{
	private static final String _C__68_REQUESTPLEDGECREST = "[C] 68 RequestPledgeCrest";
	
	private int _crestId;
	private int _clanId;
	
	@Override
	protected void readImpl()
	{
		_crestId = readD();
		_clanId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if (_crestId == 0)
		{
			return;
		}
		
		final L2PcInstance activeChar = getClient().getActiveChar();
		if ((activeChar.getClan() != null) && (activeChar.getClan().getId() == _clanId))
		{
			return;
		}
		
		sendPacket(new PledgeCrest(_crestId));
	}
	
	@Override
	public String getType()
	{
		return _C__68_REQUESTPLEDGECREST;
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
