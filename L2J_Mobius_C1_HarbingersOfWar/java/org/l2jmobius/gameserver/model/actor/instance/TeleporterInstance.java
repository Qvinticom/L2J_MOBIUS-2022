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

import org.l2jmobius.gameserver.data.TeleportLocationTable;
import org.l2jmobius.gameserver.model.TeleportLocation;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.templates.Npc;

public class TeleporterInstance extends NpcInstance
{
	private static Logger _log = Logger.getLogger(TeleporterInstance.class.getName());
	
	public TeleporterInstance(Npc template)
	{
		super(template);
	}
	
	@Override
	public void onBypassFeedback(PlayerInstance player, String command)
	{
		super.onBypassFeedback(player, command);
		if (command.startsWith("goto"))
		{
			final int val = Integer.parseInt(command.substring(5));
			doTeleport(player, val);
		}
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		pom = val == 0 ? "" + npcId : npcId + "-" + val;
		return "data/html/teleporter/" + pom + ".htm";
	}
	
	private void doTeleport(PlayerInstance player, int val)
	{
		final TeleportLocation list = TeleportLocationTable.getInstance().getTemplate(val);
		if (list != null)
		{
			if (player.getAdena() >= list.getPrice())
			{
				player.reduceAdena(list.getPrice());
				player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ());
			}
			else
			{
				final SystemMessage sm = new SystemMessage(SystemMessage.YOU_NOT_ENOUGH_ADENA);
				player.sendPacket(sm);
			}
		}
		else
		{
			_log.warning("No teleport destination with id:" + val);
		}
		player.sendPacket(new ActionFailed());
	}
}
