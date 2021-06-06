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
package org.l2jmobius.gameserver.network.serverpackets.homunculus;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author nexvill
 */
public class ExHomunculusEnchantEXPResult implements IClientOutgoingPacket
{
	private final boolean _success;
	private final boolean _newLevel;
	
	public ExHomunculusEnchantEXPResult(boolean success, boolean newLevel)
	{
		_success = success;
		_newLevel = newLevel;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_HOMUNCULUS_ENCHANT_EXP_RESULT.writeId(packet);
		if (!_success)
		{
			packet.writeD(0);
			packet.writeD(SystemMessageId.NOT_ENOUGH_UPGRADE_POINTS.getId());
		}
		else if (!_newLevel)
		{
			packet.writeD(1); // success
			packet.writeD(0); // SystemMessageId
		}
		else
		{
			packet.writeD(1);
			packet.writeD(SystemMessageId.THE_HOMUNCULUS_LEVEL_IS_INCREASED.getId());
		}
		return true;
	}
}