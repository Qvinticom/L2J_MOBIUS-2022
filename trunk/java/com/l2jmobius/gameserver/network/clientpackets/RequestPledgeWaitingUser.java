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

import com.l2jmobius.gameserver.instancemanager.ClanEntryManager;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.clan.entry.PledgeApplicantInfo;
import com.l2jmobius.gameserver.network.serverpackets.ExPledgeWaitingList;
import com.l2jmobius.gameserver.network.serverpackets.ExPledgeWaitingUser;

/**
 * @author Sdw
 */
public class RequestPledgeWaitingUser extends L2GameClientPacket
{
	private static final String _C__D0_DA_REQUESTPLEDGEDWAITINGUSER = "[C] D0;DA RequestPledgeWaitingUser";
	
	private int _clanId;
	private int _playerId;
	
	@Override
	protected void readImpl()
	{
		_clanId = readD();
		_playerId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if ((activeChar == null) || (activeChar.getClanId() != _clanId))
		{
			return;
		}
		
		final PledgeApplicantInfo infos = ClanEntryManager.getInstance().getPlayerApplication(_clanId, _playerId);
		
		if (infos != null)
		{
			activeChar.sendPacket(new ExPledgeWaitingUser(infos));
		}
		else
		{
			activeChar.sendPacket(new ExPledgeWaitingList(_clanId));
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_DA_REQUESTPLEDGEDWAITINGUSER;
	}
}