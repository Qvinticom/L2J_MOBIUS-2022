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

import java.util.Collection;

import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;

public class PartyMatchList extends ServerBasePacket
{
	private static final String _S__AF_PARTYMATCHLIST = "[S] AF PartyMatchList";
	private final Collection<PlayerInstance> _matchingPlayers;
	
	public PartyMatchList(Collection<PlayerInstance> allPlayers)
	{
		_matchingPlayers = allPlayers;
	}
	
	@Override
	public byte[] getContent()
	{
		writeC(175);
		int size = _matchingPlayers.size();
		if (size > 40)
		{
			size = 40;
		}
		writeD(size);
		for (PlayerInstance player : _matchingPlayers)
		{
			writeD(player.getObjectId());
			writeS(player.getName());
			writeD(player.getLevel());
			writeD(player.getClassId());
			writeD(0);
			writeD(player.getClanId());
			writeD(0);
			writeD(player.getX());
			writeD(player.getY());
			writeD(player.getZ());
		}
		return getBytes();
	}
	
	@Override
	public String getType()
	{
		return _S__AF_PARTYMATCHLIST;
	}
}
