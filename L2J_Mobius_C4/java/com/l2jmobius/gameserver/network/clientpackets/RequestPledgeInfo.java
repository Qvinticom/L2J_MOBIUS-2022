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
import com.l2jmobius.gameserver.datatables.ClanTable;
import com.l2jmobius.gameserver.model.L2Clan;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.PledgeInfo;
import com.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListAll;

/**
 * This class ...
 * @version $Revision: 1.5.4.3 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestPledgeInfo extends L2GameClientPacket
{
	private static final String _C__66_REQUESTPLEDGEINFO = "[C] 66 RequestPledgeInfo";
	private static Logger _log = Logger.getLogger(RequestPledgeInfo.class.getName());
	
	private int _clanId;
	
	@Override
	protected void readImpl()
	{
		_clanId = readD();
	}
	
	@Override
	public void runImpl()
	{
		if (Config.DEBUG)
		{
			_log.fine("infos for clan " + _clanId + " requested");
		}
		
		final L2PcInstance activeChar = getClient().getActiveChar();
		final L2Clan clan = ClanTable.getInstance().getClan(_clanId);
		if (clan == null)
		{
			if (Config.DEBUG)
			{
				_log.warning("Clan data for clanId " + _clanId + " is missing");
			}
			return; // we have no clan data ?!? should not happen
		}
		
		if (activeChar != null)
		{
			activeChar.sendPacket(new PledgeInfo(clan));
			
			if (clan.getClanId() == activeChar.getClanId())
			{
				final PledgeShowMemberListAll pm = new PledgeShowMemberListAll(clan, activeChar);
				activeChar.sendPacket(pm);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__66_REQUESTPLEDGEINFO;
	}
}