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

import com.l2jmobius.commons.network.PacketReader;
import com.l2jmobius.gameserver.instancemanager.CastleManager;
import com.l2jmobius.gameserver.instancemanager.ClanEntryManager;
import com.l2jmobius.gameserver.instancemanager.FortManager;
import com.l2jmobius.gameserver.model.World;
import com.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import com.l2jmobius.gameserver.model.clan.Clan;
import com.l2jmobius.gameserver.network.GameClient;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.ExPledgeCount;
import com.l2jmobius.gameserver.network.serverpackets.JoinPledge;
import com.l2jmobius.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import com.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListAdd;
import com.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListAll;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Sdw
 */
public class RequestPledgeWaitingUserAccept implements IClientIncomingPacket
{
	private boolean _acceptRequest;
	private int _playerId;
	private int _clanId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_acceptRequest = packet.readD() == 1;
		_playerId = packet.readD();
		_clanId = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final PlayerInstance player = client.getPlayer();
		if ((player == null) || (player.getClan() == null))
		{
			return;
		}
		
		if (_acceptRequest)
		{
			final PlayerInstance target = World.getInstance().getPlayer(_playerId);
			final Clan clan = player.getClan();
			if ((target != null) && (target.getClan() == null) && (clan != null))
			{
				target.sendPacket(new JoinPledge(clan.getId()));
				
				// player.setPowerGrade(9); // academy
				target.setPowerGrade(5); // New member starts at 5, not confirmed.
				
				clan.addClanMember(target);
				target.setClanPrivileges(target.getClan().getRankPrivs(target.getPowerGrade()));
				target.sendPacket(SystemMessageId.ENTERED_THE_CLAN);
				
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_JOINED_THE_CLAN);
				sm.addString(target.getName());
				clan.broadcastToOnlineMembers(sm);
				
				if (clan.getCastleId() > 0)
				{
					CastleManager.getInstance().getCastleByOwner(clan).giveResidentialSkills(target);
				}
				if (clan.getFortId() > 0)
				{
					FortManager.getInstance().getFortByOwner(clan).giveResidentialSkills(target);
				}
				target.sendSkillList();
				
				clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListAdd(target), target);
				clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
				clan.broadcastToOnlineMembers(new ExPledgeCount(clan));
				
				// This activates the clan tab on the new member.
				PledgeShowMemberListAll.sendAllTo(target);
				target.setClanJoinExpiryTime(0);
				target.broadcastUserInfo();
				
				ClanEntryManager.getInstance().removePlayerApplication(_clanId, _playerId);
			}
		}
		else
		{
			ClanEntryManager.getInstance().removePlayerApplication(_clanId, _playerId);
		}
	}
}
