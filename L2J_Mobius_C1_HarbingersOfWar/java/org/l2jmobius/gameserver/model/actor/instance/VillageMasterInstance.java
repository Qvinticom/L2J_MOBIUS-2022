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

import java.util.logging.Logger;

import org.l2jmobius.gameserver.data.ClanTable;
import org.l2jmobius.gameserver.model.Clan;
import org.l2jmobius.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;
import org.l2jmobius.gameserver.templates.Npc;

public class VillageMasterInstance extends NpcInstance
{
	private static Logger _log = Logger.getLogger(VillageMasterInstance.class.getName());
	
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
			String val = command.substring(12);
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
	
	@Override
	public void onAction(PlayerInstance player)
	{
		_log.fine("Village Master activated");
		super.onAction(player);
	}
	
	public void createClan(PlayerInstance player, String clanName)
	{
		_log.fine(player.getObjectId() + "(" + player.getName() + ") requested clan creation from " + getObjectId() + "(" + getName() + ")");
		if (player.getLevel() < 10)
		{
			SystemMessage sm = new SystemMessage(190);
			player.sendPacket(sm);
			return;
		}
		if (player.getClanId() != 0)
		{
			SystemMessage sm = new SystemMessage(190);
			player.sendPacket(sm);
			return;
		}
		if (clanName.length() > 16)
		{
			SystemMessage sm = new SystemMessage(262);
			player.sendPacket(sm);
			return;
		}
		Clan clan = ClanTable.getInstance().createClan(player, clanName);
		if (clan == null)
		{
			SystemMessage sm = new SystemMessage(261);
			player.sendPacket(sm);
			return;
		}
		player.setClan(clan);
		player.setClanId(clan.getClanId());
		player.setIsClanLeader(true);
		PledgeShowInfoUpdate pu = new PledgeShowInfoUpdate(clan, player);
		player.sendPacket(pu);
		UserInfo ui = new UserInfo(player);
		player.sendPacket(ui);
		SystemMessage sm = new SystemMessage(189);
		player.sendPacket(sm);
	}
}
