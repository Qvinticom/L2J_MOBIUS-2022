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
package com.l2jmobius.loginserver.network.clientpackets;

import com.l2jmobius.Config;
import com.l2jmobius.commons.network.IIncomingPacket;
import com.l2jmobius.commons.network.PacketReader;
import com.l2jmobius.loginserver.network.L2LoginClient;
import com.l2jmobius.loginserver.network.serverpackets.PIAgreementCheck;

/**
 * @author UnAfraid
 */
public class RequestPIAgreementCheck implements IIncomingPacket<L2LoginClient>
{
	private int _accountId;
	
	@Override
	public boolean read(L2LoginClient client, PacketReader packet)
	{
		_accountId = packet.readD();
		byte[] padding0 = new byte[3];
		byte[] checksum = new byte[4];
		byte[] padding1 = new byte[12];
		packet.readB(padding0, 0, padding0.length);
		packet.readB(checksum, 0, checksum.length);
		packet.readB(padding1, 0, padding1.length);
		return true;
	}
	
	@Override
	public void run(L2LoginClient client)
	{
		client.sendPacket(new PIAgreementCheck(_accountId, Config.SHOW_PI_AGREEMENT ? 0x01 : 0x00));
	}
}
