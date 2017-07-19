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
package com.l2jmobius.loginserver.network.clientpackets;

import com.l2jmobius.loginserver.GameServerTable;
import com.l2jmobius.loginserver.network.L2LoginClient;
import com.l2jmobius.loginserver.network.serverpackets.LoginFail;

/**
 * Format: ddc d: fist part of session id d: second part of session id c: ? (session ID is sent in LoginOk packet and fixed to 0x55555555 0x44444444)
 */
public class RequestServerList extends ClientBasePacket
{
	private final int _key1;
	private final int _key2;
	private final int _data3;
	
	/**
	 * @return
	 */
	public int getKey1()
	{
		return _key1;
	}
	
	/**
	 * @return
	 */
	public int getKey2()
	{
		return _key2;
	}
	
	/**
	 * @return
	 */
	public int getData3()
	{
		return _data3;
	}
	
	public RequestServerList(byte[] rawPacket, L2LoginClient client)
	{
		super(rawPacket, client);
		_key1 = readD(); // loginOk 1
		_key2 = readD(); // loginOk 2
		_data3 = readC(); // ?
	}
	
	@Override
	public void run()
	{
		if ((getClient().getSessionKey() != null) && getClient().getSessionKey().checkLoginPair(_key1, _key2))
		{
			getClient().setHasAgreed(true);
			GameServerTable.getInstance().createServerList(getClient());
			
		}
		else
		{
			getClient().sendPacket(new LoginFail(LoginFail.REASON_ACCESS_FAILED));
		}
	}
}