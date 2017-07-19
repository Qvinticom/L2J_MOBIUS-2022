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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jmobius.L2DatabaseFactory;
import com.l2jmobius.gameserver.cache.CrestCache;
import com.l2jmobius.gameserver.idfactory.IdFactory;
import com.l2jmobius.gameserver.model.L2Clan;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * Format : chdb c (id) 0xD0 h (subid) 0x11 d data size b raw data (picture i think ;) )
 * @author -Wooden-
 */
public class RequestExSetPledgeCrestLarge extends L2GameClientPacket
{
	private static final String _C__D0_11_REQUESTEXSETPLEDGECRESTLARGE = "[C] D0:11 RequestExSetPledgeCrestLarge";
	private static Logger _log = Logger.getLogger(RequestExSetPledgeCrestLarge.class.getName());
	
	private int _size;
	private byte[] _data;
	
	@Override
	protected void readImpl()
	{
		_size = readD();
		if (_size > 2176)
		{
			return;
		}
		
		_data = new byte[_size];
		readB(_data);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket#runImpl()
	 */
	@Override
	public void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		final L2Clan clan = activeChar.getClan();
		if (clan == null)
		{
			return;
		}
		
		if ((activeChar.getClanPrivileges() & L2Clan.CP_CL_REGISTER_CREST) != L2Clan.CP_CL_REGISTER_CREST)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_ARE_NOT_AUTHORIZED));
			return;
		}
		
		if (_data == null)
		{
			CrestCache.getInstance().removePledgeCrestLarge(clan.getCrestId());
			
			clan.setHasCrestLarge(false);
			activeChar.sendMessage("The insignia has been removed.");
			
			for (final L2PcInstance member : clan.getOnlineMembers(0))
			{
				member.broadcastUserInfo();
			}
			
			return;
		}
		
		if (_size > 2176)
		{
			activeChar.sendMessage("The insignia file size is greater than 2176 bytes.");
			return;
		}
		
		if ((clan.getHasCastle() == 0) && (clan.getHasHideout() == 0))
		{
			activeChar.sendMessage("Only a clan that owns a clan hall or a castle can get an emblem displayed on clan related items."); // there is a system message for that but didnt found the id
			return;
		}
		
		final CrestCache crestCache = CrestCache.getInstance();
		final int newId = IdFactory.getInstance().getNextId();
		
		if (!crestCache.savePledgeCrestLarge(newId, _data))
		{
			_log.log(Level.INFO, "Error loading large crest of clan:" + clan.getName());
			return;
		}
		
		if (clan.hasCrestLarge())
		{
			crestCache.removePledgeCrestLarge(clan.getCrestLargeId());
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET crest_large_id = ? WHERE clan_id = ?"))
		{
			statement.setInt(1, newId);
			statement.setInt(2, clan.getClanId());
			statement.executeUpdate();
		}
		catch (final SQLException e)
		{
			_log.warning("could not update the large crest id:" + e.getMessage());
		}
		
		clan.setCrestLargeId(newId);
		clan.setHasCrestLarge(true);
		
		activeChar.sendPacket(new SystemMessage(SystemMessage.CLAN_EMBLEM_WAS_SUCCESSFULLY_REGISTERED));
		
		for (final L2PcInstance member : clan.getOnlineMembers(0))
		{
			member.broadcastUserInfo();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__D0_11_REQUESTEXSETPLEDGECRESTLARGE;
	}
}