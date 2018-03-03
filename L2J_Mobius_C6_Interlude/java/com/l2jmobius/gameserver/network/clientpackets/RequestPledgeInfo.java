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
package com.l2jmobius.gameserver.network.clientpackets;

import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.datatables.sql.ClanTable;
import com.l2jmobius.gameserver.model.L2Clan;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.PledgeInfo;

public final class RequestPledgeInfo extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(RequestPledgeInfo.class.getName());
	
	private int clanId;
	
	@Override
	protected void readImpl()
	{
		clanId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if (Config.DEBUG)
		{
			LOGGER.info("infos for clan " + clanId + " requested");
		}
		
		final L2PcInstance activeChar = getClient().getActiveChar();
		final L2Clan clan = ClanTable.getInstance().getClan(clanId);
		
		if (activeChar == null)
		{
			return;
		}
		
		if (clan == null)
		{
			if (Config.DEBUG && (clanId > 0))
			{
				LOGGER.info("Clan data for clanId " + clanId + " is missing for player " + activeChar.getName());
			}
			return; // we have no clan data ?!? should not happen
		}
		activeChar.sendPacket(new PledgeInfo(clan));
	}
}
