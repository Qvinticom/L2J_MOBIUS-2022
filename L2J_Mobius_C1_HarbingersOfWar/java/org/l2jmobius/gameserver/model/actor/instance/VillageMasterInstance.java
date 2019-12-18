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
package org.l2jmobius.gameserver.model.actor.instance;

import org.l2jmobius.gameserver.data.ClanTable;
import org.l2jmobius.gameserver.model.Clan;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;
import org.l2jmobius.gameserver.templates.Npc;

public class VillageMasterInstance extends NpcInstance
{
	public VillageMasterInstance(Npc template)
	{
		super(template);
	}
	
	@Override
	public void onBypassFeedback(PlayerInstance player, String command)
	{
		super.onBypassFeedback(player, command);
		if (command.startsWith("create_clan"))
		{
			final String val = command.substring(12);
			createClan(player, val);
		}
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		pom = val == 0 ? "" + npcId : npcId + "-" + val;
		return "data/html/villagemaster/" + pom + ".htm";
	}
	
	public void createClan(PlayerInstance player, String clanName)
	{
		if (player.getLevel() < 10)
		{
			final SystemMessage sm = new SystemMessage(SystemMessage.FAILED_TO_CREATE_CLAN);
			player.sendPacket(sm);
			return;
		}
		if (player.getClanId() != 0)
		{
			final SystemMessage sm = new SystemMessage(SystemMessage.FAILED_TO_CREATE_CLAN);
			player.sendPacket(sm);
			return;
		}
		if (clanName.length() > 16)
		{
			final SystemMessage sm = new SystemMessage(SystemMessage.CLAN_NAME_TOO_LONG);
			player.sendPacket(sm);
			return;
		}
		final Clan clan = ClanTable.getInstance().createClan(player, clanName);
		if (clan == null)
		{
			final SystemMessage sm = new SystemMessage(SystemMessage.CLAN_NAME_INCORRECT);
			player.sendPacket(sm);
			return;
		}
		player.setClan(clan);
		player.setClanId(clan.getClanId());
		player.setIsClanLeader(true);
		final PledgeShowInfoUpdate pu = new PledgeShowInfoUpdate(clan, player);
		player.sendPacket(pu);
		final UserInfo ui = new UserInfo(player);
		player.sendPacket(ui);
		final SystemMessage sm = new SystemMessage(SystemMessage.CLAN_CREATED);
		player.sendPacket(sm);
	}
}
