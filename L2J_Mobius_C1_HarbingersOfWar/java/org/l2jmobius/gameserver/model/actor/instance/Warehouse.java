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

import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.WareHouseDepositList;
import org.l2jmobius.gameserver.network.serverpackets.WareHouseWithdrawalList;
import org.l2jmobius.gameserver.templates.NpcTemplate;

public class Warehouse extends Npc
{
	private static Logger _log = Logger.getLogger(Warehouse.class.getName());
	
	public Warehouse(NpcTemplate template)
	{
		super(template);
	}
	
	@Override
	public String getHtmlPath(int npcId, int value)
	{
		String pom = "";
		pom = value == 0 ? "" + npcId : npcId + "-" + value;
		return "data/html/warehouse/" + pom + ".htm";
	}
	
	private void showRetrieveWindow(Player player)
	{
		final org.l2jmobius.gameserver.model.Warehouse list = player.getWarehouse();
		if (list != null)
		{
			player.sendPacket(new WareHouseWithdrawalList(player));
		}
		else
		{
			_log.warning("no items stored");
		}
		player.sendPacket(new ActionFailed());
	}
	
	private void showDepositWindow(Player player)
	{
		player.sendPacket(new WareHouseDepositList(player));
		player.sendPacket(new ActionFailed());
	}
	
	private void showDepositWindowClan(Player player)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setHtml("<html><body>Clans are not supported yet.</body></html>");
		player.sendPacket(html);
		player.sendPacket(new ActionFailed());
	}
	
	private void showWithdrawWindowClan(Player player)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setHtml("<html><body>Clans are not supported yet.</body></html>");
		player.sendPacket(html);
		player.sendPacket(new ActionFailed());
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
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
