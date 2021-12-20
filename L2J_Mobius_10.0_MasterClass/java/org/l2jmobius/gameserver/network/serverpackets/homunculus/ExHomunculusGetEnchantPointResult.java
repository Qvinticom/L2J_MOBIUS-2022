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
public class ExHomunculusGetEnchantPointResult implements IClientOutgoingPacket
{
	private final int _enchantType;
	
	public ExHomunculusGetEnchantPointResult(int enchantType)
	{
		_enchantType = enchantType;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_HOMUNCULUS_GET_ENCHANT_POINT_RESULT.writeId(packet);
		if (_enchantType != 2)
		{
			packet.writeD(1); // success
			packet.writeD(_enchantType);
			packet.writeD(SystemMessageId.YOU_VE_OBTAINED_UPGRADE_POINTS.getId());
		}
		else
		{
			packet.writeD(1);
			packet.writeD(_enchantType);
			packet.writeD(SystemMessageId.VP_ADDED.getId());
		}
		return true;
	}
}