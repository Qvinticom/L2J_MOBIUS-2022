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
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.network.GameClient;

/**
 * @author NviX
 */
public class RequestAutoUse implements IClientIncomingPacket
{
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		int unk1 = packet.readC(); // C - true. This is summary amount of next data received.
		LOGGER.info("received packet RequestAutoUse with unk1:" + unk1);
		int unk2 = packet.readC();
		LOGGER.info("and unk2: " + unk2);
		int unk3 = packet.readC(); // Can target mobs, that attacked by other players?
		LOGGER.info("and unk3: " + unk3);
		int unk4 = packet.readC(); // Auto pickup?
		LOGGER.info("and unk4: " + unk4);
		int unk5 = packet.readC();
		LOGGER.info("and unk5: " + unk5);
		int unk6 = packet.readC();
		LOGGER.info("and unk6: " + unk6);
		int unk7 = packet.readC(); // short range :1; long: 0
		LOGGER.info("and unk7: " + unk7);
		int unk8 = packet.readC(); // received 51 when logged in game...
		LOGGER.info("and unk8: " + unk8);
		int unk9 = packet.readC();
		LOGGER.info("and unk9: " + unk9);
		int unk10 = packet.readC();
		LOGGER.info("and unk10: " + unk10);
		int unk11 = packet.readC();
		LOGGER.info("and unk11: " + unk11);
		int unk12 = packet.readC(); // enable/ disable?
		LOGGER.info("and unk12: " + unk12);
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
	}
}