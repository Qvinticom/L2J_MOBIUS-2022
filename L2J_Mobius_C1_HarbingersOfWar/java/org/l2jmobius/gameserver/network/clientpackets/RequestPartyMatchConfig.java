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

import org.l2jmobius.gameserver.network.ClientThread;

public class RequestPartyMatchConfig extends ClientBasePacket
{
	private static final String _C__6F_REQUESTPARTYMATCHCONFIG = "[C] 6F RequestPartyMatchConfig";
	
	public RequestPartyMatchConfig(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		final int automaticRegistration = readD();
		final int showLevel = readD();
		final int showClass = readD();
		final String memo = readS();
		client.getActiveChar().setPartyMatchingAutomaticRegistration(automaticRegistration == 1);
		client.getActiveChar().setPartyMatchingShowLevel(showLevel == 1);
		client.getActiveChar().setPartyMatchingShowClass(showClass == 1);
		client.getActiveChar().setPartyMatchingMemo(memo);
	}
	
	@Override
	public String getType()
	{
		return _C__6F_REQUESTPARTYMATCHCONFIG;
	}
}
