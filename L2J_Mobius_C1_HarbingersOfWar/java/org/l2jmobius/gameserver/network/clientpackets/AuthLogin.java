/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.gameserver.network.clientpackets;

import java.io.IOException;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.AuthLoginFail;
import org.l2jmobius.gameserver.network.serverpackets.CharSelectInfo;
import org.l2jmobius.loginserver.LoginController;

public class AuthLogin extends ClientBasePacket
{
	private static final Logger _log = Logger.getLogger(AuthLogin.class.getName());
	
	public AuthLogin(byte[] decrypt, ClientThread client) throws IOException
	{
		super(decrypt);
		final String loginName = readS().toLowerCase();
		@SuppressWarnings("unused")
		final long key1 = readD();
		final long key2 = readD();
		
		final int access = LoginController.getInstance().getGmAccessLevel(loginName);
		if (!LoginController.getInstance().loginPossible(access))
		{
			_log.warning("Server is full. client is blocked: " + loginName);
			client.getConnection().sendPacket(new AuthLoginFail(AuthLoginFail.SYSTEM_ERROR_LOGIN_LATER));
			return;
		}
		client.setLoginName(loginName);
		client.setLoginFolder(loginName);
		final int sessionKey = LoginController.getInstance().getKeyForAccount(loginName);
		if (sessionKey != key2)
		{
			_log.warning("session key is not correct. closing connection");
			client.getConnection().sendPacket(new AuthLoginFail(AuthLoginFail.SYSTEM_ERROR_LOGIN_LATER));
		}
		else
		{
			LoginController.getInstance().addGameServerLogin(loginName, client.getConnection());
			final CharSelectInfo cl = new CharSelectInfo(loginName, client.getSessionId());
			client.getConnection().sendPacket(cl);
		}
		client.setAccessLevel(access);
	}
}
