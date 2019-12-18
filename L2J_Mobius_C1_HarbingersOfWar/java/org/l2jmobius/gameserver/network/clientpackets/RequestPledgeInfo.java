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

import java.util.logging.Logger;

import org.l2jmobius.gameserver.data.ClanTable;
import org.l2jmobius.gameserver.model.Clan;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.PledgeInfo;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListAll;

public class RequestPledgeInfo extends ClientBasePacket
{
	private static final Logger _log = Logger.getLogger(RequestPledgeInfo.class.getName());
	
	public RequestPledgeInfo(byte[] rawPacket, ClientThread client)
	{
		super(rawPacket);
		final int clanId = readD();
		final PlayerInstance activeChar = client.getActiveChar();
		final Clan clan = ClanTable.getInstance().getClan(clanId);
		if (clan == null)
		{
			_log.warning("Clan data for clanId " + clanId + " is missing");
			return;
		}
		final PledgeInfo pc = new PledgeInfo(clan);
		activeChar.sendPacket(pc);
		if (clan.getClanId() == activeChar.getClanId())
		{
			final PledgeShowMemberListAll pm = new PledgeShowMemberListAll(clan, activeChar);
			activeChar.sendPacket(pm);
		}
	}
}
