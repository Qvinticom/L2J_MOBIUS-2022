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

import org.l2jmobius.gameserver.data.TradeController;
import org.l2jmobius.gameserver.model.TradeList;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.BuyList;
import org.l2jmobius.gameserver.network.serverpackets.SellList;
import org.l2jmobius.gameserver.templates.Npc;

public class MerchantInstance extends NpcInstance
{
	private static Logger _log = Logger.getLogger(MerchantInstance.class.getName());
	
	public MerchantInstance(Npc template)
	{
		super(template);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		pom = val == 0 ? "" + npcId : npcId + "-" + val;
		return "data/html/merchant/" + pom + ".htm";
	}
	
	private void showBuyWindow(PlayerInstance player, int val)
	{
		final TradeList list = TradeController.getInstance().getBuyList(val);
		if (list != null)
		{
			player.sendPacket(new BuyList(list, player.getAdena()));
		}
		else
		{
			_log.warning("no buylist with id:" + val);
		}
		player.sendPacket(new ActionFailed());
	}
	
	private void showSellWindow(PlayerInstance player)
	{
		player.sendPacket(new SellList(player));
		player.sendPacket(new ActionFailed());
	}
	
	@Override
	public void onBypassFeedback(PlayerInstance player, String command)
	{
		super.onBypassFeedback(player, command);
		if (command.startsWith("Buy"))
		{
			showBuyWindow(player, Integer.parseInt(command.substring(4)));
		}
		else if (command.equals("Sell"))
		{
			showSellWindow(player);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
}
