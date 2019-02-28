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

import com.l2jmobius.commons.network.IIncomingPacket;
import com.l2jmobius.commons.network.PacketReader;
import com.l2jmobius.loginserver.network.L2LoginClient;
import com.l2jmobius.loginserver.network.serverpackets.PIAgreementAck;

/**
 * @author UnAfraid
 */
public class RequestPIAgreement implements IIncomingPacket<L2LoginClient>
{
	private int _accountId;
	private int _status;
	
	@Override
	public boolean read(L2LoginClient client, PacketReader packet)
	{
		_accountId = packet.readD();
		_status = packet.readC();
		return true;
	}
	
	@Override
	public void run(L2LoginClient client)
	{
		client.sendPacket(new PIAgreementAck(_accountId, _status));
	}
}
