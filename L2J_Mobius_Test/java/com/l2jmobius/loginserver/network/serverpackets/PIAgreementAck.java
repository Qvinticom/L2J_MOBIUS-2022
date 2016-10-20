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
package com.l2jmobius.loginserver.network.serverpackets;

/**
 * @author UnAfraid
 */
public class PIAgreementAck extends L2LoginServerPacket
{
	private final int _accountId;
	private final int _status;
	
	public PIAgreementAck(int accountId, int status)
	{
		_accountId = accountId;
		_status = status;
	}
	
	@Override
	protected void write()
	{
		writeC(0x12);
		writeD(_accountId);
		writeC(_status);
	}
}
