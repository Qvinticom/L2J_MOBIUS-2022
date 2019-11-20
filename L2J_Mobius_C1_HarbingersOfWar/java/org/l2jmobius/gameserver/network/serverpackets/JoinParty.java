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
package org.l2jmobius.gameserver.network.serverpackets;

public class JoinParty extends ServerBasePacket
{
	private static final String _S__4C_JOINPARTY = "[S] 4C JoinParty";
	private final int _response;
	
	public JoinParty(int response)
	{
		_response = response;
	}
	
	@Override
	public byte[] getContent()
	{
		_bao.write(76);
		writeD(_response);
		return _bao.toByteArray();
	}
	
	@Override
	public String getType()
	{
		return _S__4C_JOINPARTY;
	}
}
