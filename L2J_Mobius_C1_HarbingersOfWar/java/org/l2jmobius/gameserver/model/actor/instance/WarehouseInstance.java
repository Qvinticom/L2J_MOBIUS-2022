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

import org.l2jmobius.gameserver.model.Warehouse;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.WareHouseDepositList;
import org.l2jmobius.gameserver.network.serverpackets.WareHouseWithdrawalList;
import org.l2jmobius.gameserver.templates.Npc;

public class WarehouseInstance extends NpcInstance
{
	private static Logger _log = Logger.getLogger(WarehouseInstance.class.getName());
	
	public WarehouseInstance(Npc template)
	{
		super(template);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		pom = val == 0 ? "" + npcId : npcId + "-" + val;
		return "data/html/warehouse/" + pom + ".htm";
	}
	
	private void showRetrieveWindow(PlayerInstance player)
	{
		final Warehouse list = player.getWarehouse();
		if (list != null)
		{
			final WareHouseWithdrawalList wl = new WareHouseWithdrawalList(player);
			player.sendPacket(wl);
		}
		else
		{
			_log.warning("no items stored");
		}
		player.sendPacket(new ActionFailed());
	}
	
	private void showDepositWindow(PlayerInstance player)
	{
		final WareHouseDepositList dl = new WareHouseDepositList(player);
		player.sendPacket(dl);
		player.sendPacket(new ActionFailed());
	}
	
	private void showDepositWindowClan(PlayerInstance player)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setHtml("<html><body>Clans are not supported yet.</body></html>");
		player.sendPacket(html);
		player.sendPacket(new ActionFailed());
	}
	
	private void showWithdrawWindowClan(PlayerInstance player)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setHtml("<html><body>Clans are not supported yet.</body></html>");
		player.sendPacket(html);
		player.sendPacket(new ActionFailed());
	}
	
	@Override
	public void onBypassFeedback(PlayerInstance player, String command)
	{
		if (command.startsWith("WithdrawP"))
		{
			showRetrieveWindow(player);
		}
		else if (command.equals("DepositP"))
		{
			showDepositWindow(player);
		}
		else if (command.equals("WithdrawC"))
		{
			showWithdrawWindowClan(player);
		}
		else if (command.equals("DepositC"))
		{
			showDepositWindowClan(player);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
}
