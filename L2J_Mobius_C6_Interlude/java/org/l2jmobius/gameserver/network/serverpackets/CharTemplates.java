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

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.templates.PlayerTemplate;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @version $Revision: 1.3.2.1.2.7 $ $Date: 2005/03/27 15:29:39 $
 */
public class CharTemplates implements IClientOutgoingPacket
{
	private final List<PlayerTemplate> _chars = new ArrayList<>();
	
	public void addChar(PlayerTemplate template)
	{
		_chars.add(template);
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.CHAR_TEMPLATES.writeId(packet);
		packet.writeD(_chars.size());
		for (PlayerTemplate temp : _chars)
		{
			packet.writeD(temp.getRace().ordinal());
			packet.writeD(temp.getClassId().getId());
			packet.writeD(0x46);
			packet.writeD(temp.getBaseSTR());
			packet.writeD(0x0a);
			packet.writeD(0x46);
			packet.writeD(temp.getBaseDEX());
			packet.writeD(0x0a);
			packet.writeD(0x46);
			packet.writeD(temp.getBaseCON());
			packet.writeD(0x0a);
			packet.writeD(0x46);
			packet.writeD(temp.getBaseINT());
			packet.writeD(0x0a);
			packet.writeD(0x46);
			packet.writeD(temp.getBaseWIT());
			packet.writeD(0x0a);
			packet.writeD(0x46);
			packet.writeD(temp.getBaseMEN());
			packet.writeD(0x0a);
		}
		return true;
	}
}
