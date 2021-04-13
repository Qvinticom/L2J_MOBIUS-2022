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
package org.l2jmobius.loginserver.network.clientpackets;

import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.BaseRecievePacket;
import org.l2jmobius.loginserver.GameServerTable;
import org.l2jmobius.loginserver.LoginClient;
import org.l2jmobius.loginserver.LoginController;
import org.l2jmobius.loginserver.network.gameserverpackets.ServerStatus;
import org.l2jmobius.loginserver.network.serverpackets.LoginFail;
import org.l2jmobius.loginserver.network.serverpackets.PlayFail;
import org.l2jmobius.loginserver.network.serverpackets.PlayOk;

/**
 * Fromat is ddc d: first part of session id d: second part of session id c: server ID (session ID is sent in LoginOk packet and fixed to 0x55555555 0x44444444)
 */
public class RequestServerLogin extends BaseRecievePacket
{
	private final static Logger LOGGER = Logger.getLogger(RequestServerLogin.class.getName());
	
	private final int _key1;
	private final int _key2;
	private final int _server_id;
	
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
	public int getServerID()
	{
		return _server_id;
	}
	
	public RequestServerLogin(byte[] rawPacket, LoginClient client)
	{
		super(rawPacket, client);
		_key1 = readD();
		_key2 = readD();
		
		_server_id = readC();// = rawPacket[9] &0xff;
	}
	
	@Override
	public void run()
	{
		
		final LoginController lc = LoginController.getInstance();
		final int status = GameServerTable.getInstance().getGameServerStatus(getServerID());
		if ((status == ServerStatus.STATUS_DOWN) || ((status == ServerStatus.STATUS_GM_ONLY) && (getClient().getAccessLevel() <= 0)))
		{
			getClient().sendPacket(new PlayFail(PlayFail.REASON_SYSTEM_ERROR));
			return;
		}
		
		final int onlinePlayers = lc.getOnlinePlayerCount(getServerID());
		if (onlinePlayers >= lc.getMaxAllowedOnlinePlayers(getServerID()))
		{
			if (onlinePlayers == 0)
			{
				getClient().sendPacket(new PlayFail(PlayFail.REASON_SYSTEM_ERROR));
				return;
			}
			
			if (getClient().getAccessLevel() <= 0)
			{
				getClient().sendPacket(new PlayFail(PlayFail.REASON_TOO_MANY_PLAYERS));
				return;
			}
		}
		
		if (Config.SHOW_LICENCE)
		{
			if (!getClient().getSessionKey().checkLoginPair(_key1, _key2))
			{
				getClient().sendPacket(new LoginFail(LoginFail.REASON_ACCESS_FAILED));
				return;
			}
			
			if (Config.L2WALKER_PROTECTION && !getClient().hasAgreed())
			{
				LOGGER.warning("Account " + getClient().getAccount() + " tried to log in using a 3rd party program.");
				getClient().sendPacket(new LoginFail(LoginFail.REASON_ACCESS_FAILED));
				return;
			}
			getClient().setHasAgreed(false);
		}
		
		if (getClient().getLastServer() != getServerID())
		{
			lc.saveLastServer(getClient().getAccount(), getServerID());
		}
		
		getClient().sendPacket(new PlayOk(getClient().getSessionKey()));
		getClient().setAccount(null);
	}
}