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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.l2jmobius.commons.database.DatabaseFactory;
import com.l2jmobius.gameserver.cache.CrestCache;
import com.l2jmobius.gameserver.datatables.sql.ClanTable;
import com.l2jmobius.gameserver.idfactory.IdFactory;
import com.l2jmobius.gameserver.model.L2Clan;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

public final class RequestSetAllyCrest extends L2GameClientPacket
{
	static Logger LOGGER = Logger.getLogger(RequestSetAllyCrest.class.getName());
	
	private int _length;
	private byte[] _data;
	
	@Override
	protected void readImpl()
	{
		_length = readD();
		if ((_length < 0) || (_length > 192))
		{
			return;
		}
		
		_data = new byte[_length];
		readB(_data);
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (_length < 0)
		{
			activeChar.sendMessage("File transfer error.");
			return;
		}
		
		if (_length > 192)
		{
			activeChar.sendMessage("The crest file size was too big (max 192 bytes).");
			return;
		}
		
		if (activeChar.getAllyId() != 0)
		{
			final L2Clan leaderclan = ClanTable.getInstance().getClan(activeChar.getAllyId());
			
			if ((activeChar.getClanId() != leaderclan.getClanId()) || !activeChar.isClanLeader())
			{
				return;
			}
			
			final CrestCache crestCache = CrestCache.getInstance();
			
			final int newId = IdFactory.getInstance().getNextId();
			
			if (!crestCache.saveAllyCrest(newId, _data))
			{
				LOGGER.warning("Error loading crest of ally:" + leaderclan.getAllyName());
				return;
			}
			
			if (leaderclan.getAllyCrestId() != 0)
			{
				crestCache.removeAllyCrest(leaderclan.getAllyCrestId());
			}
			
			try (Connection con = DatabaseFactory.getInstance().getConnection())
			{
				PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET ally_crest_id = ? WHERE ally_id = ?");
				statement.setInt(1, newId);
				statement.setInt(2, leaderclan.getAllyId());
				statement.executeUpdate();
				statement.close();
			}
			catch (SQLException e)
			{
				LOGGER.warning("could not update the ally crest id:" + e.getMessage());
			}
			
			for (L2Clan clan : ClanTable.getInstance().getClans())
			{
				if (clan.getAllyId() == activeChar.getAllyId())
				{
					clan.setAllyCrestId(newId);
					for (L2PcInstance member : clan.getOnlineMembers(""))
					{
						member.broadcastUserInfo();
					}
				}
			}
		}
	}
}
