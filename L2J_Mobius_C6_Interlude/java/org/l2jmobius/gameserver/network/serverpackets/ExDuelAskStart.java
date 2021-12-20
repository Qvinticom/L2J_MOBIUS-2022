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
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * Format: ch Sd.
 * @author KenM
 */
public class ExDuelAskStart implements IClientOutgoingPacket
{
	/** The _requestor name. */
	private final String _requestorName;
	/** The _party duel. */
	private final int _partyDuel;
	
	/**
	 * Instantiates a new ex duel ask start.
	 * @param requestor the requestor
	 * @param partyDuel the party duel
	 */
	public ExDuelAskStart(String requestor, int partyDuel)
	{
		_requestorName = requestor;
		_partyDuel = partyDuel;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_DUEL_ASK_START.writeId(packet);
		packet.writeS(_requestorName);
		packet.writeD(_partyDuel);
		return true;
	}
}
