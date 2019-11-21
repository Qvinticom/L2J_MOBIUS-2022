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
package org.l2jmobius.loginserver.network.clientpackets;

public class RequestAuthLogin
{
	private String _user;
	private final String _password;
	
	public String getPassword()
	{
		return _password;
	}
	
	public String getUser()
	{
		return _user;
	}
	
	public RequestAuthLogin(byte[] rawPacket)
	{
		_user = new String(rawPacket, 1, 14).trim();
		_user = _user.toLowerCase();
		_password = new String(rawPacket, 15, 14).trim();
	}
}
