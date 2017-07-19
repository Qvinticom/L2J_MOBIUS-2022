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
package com.l2jmobius.gameserver.network.serverpackets;

import com.l2jmobius.gameserver.model.L2Clan;

/**
 * This class ...
 * @version $Revision: 1.2.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class PledgeShowInfoUpdate extends L2GameServerPacket
{
	private static final String _S__A1_PLEDGESHOWINFOUPDATE = "[S] 88 PledgeShowInfoUpdate";
	private final L2Clan _clan;
	
	public PledgeShowInfoUpdate(L2Clan clan)
	{
		_clan = clan;
	}
	
	@Override
	protected final void writeImpl()
	{
		
		writeC(0x88);
		// sending empty data so client will ask all the info in response ;)
		writeD(_clan.getClanId());
		writeD(_clan.getCrestId());
		writeD(_clan.getLevel());
		writeD(_clan.getHasCastle());
		writeD(_clan.getHasHideout());
		writeD(0);
		writeD(_clan.getLeader().getLevel());
		writeD(_clan.getDissolvingExpiryTime() > System.currentTimeMillis() ? 3 : 0);
		writeD(0);
		writeD(_clan.getAllyId());
		
		writeS(_clan.getAllyName());
		
		writeD(_clan.getAllyCrestId());
		
		writeD(_clan.isAtWar());
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.serverpackets.L2GameServerPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__A1_PLEDGESHOWINFOUPDATE;
	}
}