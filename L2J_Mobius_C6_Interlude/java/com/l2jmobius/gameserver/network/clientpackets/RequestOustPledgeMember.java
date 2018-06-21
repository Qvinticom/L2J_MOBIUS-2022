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
import com.l2jmobius.gameserver.model.L2Clan;
import com.l2jmobius.gameserver.model.L2ClanMember;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public final class RequestOustPledgeMember extends L2GameClientPacket
{
	static Logger LOGGER = Logger.getLogger(RequestOustPledgeMember.class.getName());
	
	private String _target;
	
	@Override
	protected void readImpl()
	{
		_target = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (activeChar.getClan() == null)
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
			return;
		}
		
		if ((activeChar.getClanPrivileges() & L2Clan.CP_CL_DISMISS) != L2Clan.CP_CL_DISMISS)
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		if (activeChar.getName().equalsIgnoreCase(_target))
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_DISMISS_YOURSELF);
			return;
		}
		
		final L2Clan clan = activeChar.getClan();
		
		final L2ClanMember member = clan.getClanMember(_target);
		
		if (member == null)
		{
			LOGGER.info("Target (" + _target + ") is not member of the clan");
			return;
		}
		
		if (member.isOnline() && member.getPlayerInstance().isInCombat())
		{
			activeChar.sendPacket(SystemMessageId.CLAN_MEMBER_CANNOT_BE_DISMISSED_DURING_COMBAT);
			return;
		}
		
		// this also updates the database
		clan.removeClanMember(_target, System.currentTimeMillis() + (Config.ALT_CLAN_JOIN_DAYS * 86400000)); // Like L2OFF also player takes the penality
		clan.setCharPenaltyExpiryTime(System.currentTimeMillis() + (Config.ALT_CLAN_JOIN_DAYS * 86400000)); // 24*60*60*1000 = 86400000
		clan.updateClanInDB();
		
		SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_EXPELLED);
		sm.addString(member.getName());
		clan.broadcastToOnlineMembers(sm);
		activeChar.sendPacket(SystemMessageId.YOU_HAVE_SUCCEEDED_IN_EXPELLING_CLAN_MEMBER);
		activeChar.sendPacket(SystemMessageId.YOU_MUST_WAIT_BEFORE_ACCEPTING_A_NEW_MEMBER);
		
		// Remove the Player From the Member list
		clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(_target));
		if (member.isOnline())
		{
			final L2PcInstance player = member.getPlayerInstance();
			player.sendPacket(SystemMessageId.CLAN_MEMBERSHIP_TERMINATED);
			player.setActiveWarehouse(null);
		}
	}
}