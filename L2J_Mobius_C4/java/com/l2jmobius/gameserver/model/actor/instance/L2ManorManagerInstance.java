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
package com.l2jmobius.gameserver.model.actor.instance;

import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.l2jmobius.gameserver.TradeController;
import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.datatables.ItemTable;
import com.l2jmobius.gameserver.instancemanager.CastleManager;
import com.l2jmobius.gameserver.instancemanager.CastleManorManager;
import com.l2jmobius.gameserver.instancemanager.CastleManorManager.SeedProduction;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.L2TradeList;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.BuyList;
import com.l2jmobius.gameserver.network.serverpackets.BuyListSeed;
import com.l2jmobius.gameserver.network.serverpackets.ExShowCropInfo;
import com.l2jmobius.gameserver.network.serverpackets.ExShowManorDefaultInfo;
import com.l2jmobius.gameserver.network.serverpackets.ExShowProcureCropDetail;
import com.l2jmobius.gameserver.network.serverpackets.ExShowSeedInfo;
import com.l2jmobius.gameserver.network.serverpackets.ExShowSellCropList;
import com.l2jmobius.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.network.serverpackets.ValidateLocation;
import com.l2jmobius.gameserver.templates.L2NpcTemplate;

import javolution.util.FastList;

public class L2ManorManagerInstance extends L2MerchantInstance
{
	private static Logger _log = Logger.getLogger(L2ManorManagerInstance.class.getName());
	
	public L2ManorManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player))
		{
			return;
		}
		
		player.setLastFolkNPC(this);
		
		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			final MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
			
			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if (!canInteract(player))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else
			{
				// If player is a lord of this manor, alternative message from NPC
				if (CastleManorManager.getInstance().isDisabled())
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile("data/html/npcdefault.htm");
					html.replace("%objectId%", String.valueOf(getObjectId()));
					html.replace("%npcname%", getName());
					player.sendPacket(html);
				}
				else if (!player.isGM() // Player is not GM
					&& (getCastle() != null) && (getCastle().getCastleId() > 0 // Verification of castle
					) && (player.getClan() != null // Player have clan
					) && (getCastle().getOwnerId() == player.getClanId() // Player's clan owning the castle
					) && player.isClanLeader())
				{
					showMessageWindow(player, "manager-lord.htm");
				}
				else
				{
					showMessageWindow(player, "manager.htm");
				}
			}
		}
		
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(new ActionFailed());
	}
	
	private void showBuyWindow(L2PcInstance player, String val)
	{
		final double taxRate = 0;
		player.tempInventoryDisable();
		
		final L2TradeList list = TradeController.getInstance().getBuyList(Integer.parseInt(val));
		
		if (list != null)
		{
			list.getItems().get(0).setCount(1);
			final BuyList bl = new BuyList(list, player.getAdena(), taxRate);
			player.sendPacket(bl);
		}
		else
		{
			_log.info("possible client hacker: " + player.getName() + " attempting to buy from GM shop! < Ban him!");
			_log.info("buylist id:" + val);
		}
		
		player.sendPacket(new ActionFailed());
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if ((player.getLastFolkNPC() == null) || (player.getLastFolkNPC().getObjectId() != getObjectId()))
		{
			return;
		}
		
		if (command.startsWith("manor_menu_select"))
		{
			// input string format:
			// manor_menu_select?ask=X&state=Y&time=X
			if (CastleManorManager.getInstance().isUnderMaintenance())
			{
				player.sendPacket(new SystemMessage(SystemMessage.THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE));
				player.sendPacket(new ActionFailed());
				return;
			}
			
			final String params = command.substring(command.indexOf("?") + 1);
			final StringTokenizer st = new StringTokenizer(params, "&");
			final int ask = Integer.parseInt(st.nextToken().split("=")[1]);
			final int state = Integer.parseInt(st.nextToken().split("=")[1]);
			final int time = Integer.parseInt(st.nextToken().split("=")[1]);
			
			int castleId;
			if (state == -1)
			{
				castleId = getCastle().getCastleId();
			}
			else
			{
				castleId = state;
			}
			
			switch (ask)
			{
				// Main action
				case 1: // Seed purchase
					if (castleId != getCastle().getCastleId())
					{
						player.sendPacket(new SystemMessage(SystemMessage.HERE_YOU_CAN_BUY_ONLY_SEEDS_OF_S1_MANOR));
					}
					else
					{
						final L2TradeList tradeList = new L2TradeList(0);
						final FastList<SeedProduction> seeds = getCastle().getSeedProduction(CastleManorManager.PERIOD_CURRENT);
						
						for (final SeedProduction s : seeds)
						{
							final L2ItemInstance item = ItemTable.getInstance().createDummyItem(s.getId());
							item.setPriceToSell(s.getPrice());
							item.setCount(s.getCanProduce());
							if ((item.getCount() > 0) && (item.getPriceToSell() > 0))
							{
								tradeList.addItem(item);
							}
						}
						final BuyListSeed bl = new BuyListSeed(tradeList, castleId, player.getAdena());
						player.sendPacket(bl);
					}
					break;
				case 2: // Crop sales
					player.sendPacket(new ExShowSellCropList(player, castleId, getCastle().getCropProcure(CastleManorManager.PERIOD_CURRENT)));
					break;
				case 3: // Current seeds (Manor info)
					if ((time == 1) && !CastleManager.getInstance().getCastleById(castleId).isNextPeriodApproved())
					{
						player.sendPacket(new ExShowSeedInfo(castleId, null));
					}
					else
					{
						player.sendPacket(new ExShowSeedInfo(castleId, CastleManager.getInstance().getCastleById(castleId).getSeedProduction(time)));
					}
					break;
				case 4: // Current crops (Manor info)
					if ((time == 1) && !CastleManager.getInstance().getCastleById(castleId).isNextPeriodApproved())
					{
						player.sendPacket(new ExShowCropInfo(castleId, null));
					}
					else
					{
						player.sendPacket(new ExShowCropInfo(castleId, CastleManager.getInstance().getCastleById(castleId).getCropProcure(time)));
					}
					break;
				case 5: // Basic info (Manor info)
					player.sendPacket(new ExShowManorDefaultInfo());
					break;
				case 6: // Buy harvester
					showBuyWindow(player, "1" + getNpcId());
					break;
				case 9: // Edit sales (Crop sales)
					player.sendPacket(new ExShowProcureCropDetail(state));
					break;
			}
		}
		else if (command.startsWith("help"))
		{
			final StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken(); // discard first
			final String filename = "manor_client_help00" + st.nextToken() + ".htm";
			showMessageWindow(player, filename);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	public String getHtmlPath()
	{
		return "data/html/manormanager/";
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		return "data/html/manormanager/manager.htm"; // Used only in parent method to return from "Territory status" to initial screen.
	}
	
	private void showMessageWindow(L2PcInstance player, String filename)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(getHtmlPath() + filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getNpcId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
}