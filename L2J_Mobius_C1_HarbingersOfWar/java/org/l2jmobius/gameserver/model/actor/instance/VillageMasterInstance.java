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
	public String getHtmlPath(int npcId, int value)
	{
		String pom = "";
		pom = value == 0 ? "" + npcId : npcId + "-" + value;
		return "data/html/villagemaster/" + pom + ".htm";
	}
	
	public void createClan(PlayerInstance player, String clanName)
	{
		if (player.getLevel() < 10)
		{
			player.sendPacket(new SystemMessage(SystemMessage.FAILED_TO_CREATE_CLAN));
			return;
		}
		if (player.getClanId() != 0)
		{
			player.sendPacket(new SystemMessage(SystemMessage.FAILED_TO_CREATE_CLAN));
			return;
		}
		if (clanName.length() > 16)
		{
			player.sendPacket(new SystemMessage(SystemMessage.CLAN_NAME_TOO_LONG));
			return;
		}
		final Clan clan = ClanTable.getInstance().createClan(player, clanName);
		if (clan == null)
		{
			player.sendPacket(new SystemMessage(SystemMessage.CLAN_NAME_INCORRECT));
			return;
		}
		player.setClan(clan);
		player.setClanId(clan.getClanId());
		player.setClanLeader(true);
		player.sendPacket(new PledgeShowInfoUpdate(clan, player));
		player.sendPacket(new UserInfo(player));
		player.sendPacket(new SystemMessage(SystemMessage.CLAN_CREATED));
	}
}
