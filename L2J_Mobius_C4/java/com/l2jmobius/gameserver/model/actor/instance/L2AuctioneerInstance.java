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

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.datatables.MapRegionTable;
import com.l2jmobius.gameserver.instancemanager.AuctionManager;
import com.l2jmobius.gameserver.instancemanager.ClanHallManager;
import com.l2jmobius.gameserver.model.entity.Auction;
import com.l2jmobius.gameserver.model.entity.Auction.Bidder;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.network.serverpackets.ValidateLocation;
import com.l2jmobius.gameserver.templates.L2NpcTemplate;

import javolution.util.FastMap;

public final class L2AuctioneerInstance extends L2FolkInstance
{
	private static int Cond_All_False = 0;
	private static int Cond_Busy_Because_Of_Siege = 1;
	private static int Cond_Regular = 3;
	
	private final Map<Integer, Auction> _pendingAuctions = new FastMap<>();
	
	public L2AuctioneerInstance(int objectId, L2NpcTemplate template)
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
				showMessageWindow(player);
			}
			
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(new ActionFailed());
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		final int condition = validateCondition(player);
		if (condition <= Cond_All_False)
		{
			player.sendMessage("Inappropriate conditions.");
			return;
		}
		if (condition == Cond_Busy_Because_Of_Siege)
		{
			player.sendMessage("Busy because of siege.");
			return;
		}
		else if (condition == Cond_Regular)
		{
			final StringTokenizer st = new StringTokenizer(command, " ");
			final String actualCommand = st.nextToken(); // Get actual command
			
			String val = "";
			if (st.countTokens() >= 1)
			{
				val = st.nextToken();
			}
			
			if (actualCommand.equalsIgnoreCase("auction"))
			{
				if (val.isEmpty())
				{
					return;
				}
				
				try
				{
					final int days = Integer.parseInt(val);
					try
					{
						int bid = 0;
						if (st.countTokens() >= 1)
						{
							bid = Integer.parseInt(st.nextToken());
						}
						
						final Auction a = new Auction(player.getClan().getHasHideout(), player.getClan(), days * 86400000, bid, ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getName());
						if (_pendingAuctions.get(a.getId()) != null)
						{
							_pendingAuctions.remove(a.getId());
						}
						
						_pendingAuctions.put(a.getId(), a);
						
						final String filename = "data/html/auction/AgitSale3.htm";
						final NpcHtmlMessage html = new NpcHtmlMessage(1);
						html.setFile(filename);
						html.replace("%x%", val);
						html.replace("%AGIT_AUCTION_END_YY%", String.valueOf(a.getEndDate().get(Calendar.YEAR)));
						html.replace("%AGIT_AUCTION_END_MM%", String.valueOf(a.getEndDate().get(Calendar.MONTH) + 1));
						html.replace("%AGIT_AUCTION_END_DD%", String.valueOf(a.getEndDate().get(Calendar.DAY_OF_MONTH)));
						html.replace("%AGIT_AUCTION_END_HH%", String.valueOf(a.getEndDate().get(Calendar.HOUR_OF_DAY)));
						html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
						html.replace("%AGIT_AUCTION_MIN%", String.valueOf(a.getStartingBid()));
						html.replace("%AGIT_AUCTION_DESC%", ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getDesc());
						html.replace("%objectId%", String.valueOf((getObjectId())));
						player.sendPacket(html);
					}
					catch (final Exception e)
					{
						player.sendMessage("Invalid bid!");
					}
				}
				catch (final Exception e)
				{
					player.sendMessage("Invalid auction duration!");
				}
				
				return;
			}
			if (actualCommand.equalsIgnoreCase("confirmAuction"))
			{
				try
				{
					final Auction a = _pendingAuctions.get(player.getClan().getHasHideout());
					a.confirmAuction();
					_pendingAuctions.remove(player.getClan().getHasHideout());
				}
				catch (final Exception e)
				{
					player.sendMessage("Invalid auction!");
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("bidding"))
			{
				if (val.isEmpty())
				{
					return;
				}
				if (Config.DEBUG)
				{
					_log.warning("bidding show successful");
				}
				
				try
				{
					final int auctionId = Integer.parseInt(val);
					if (Config.DEBUG)
					{
						_log.warning("auction test started");
					}
					
					final String filename = "data/html/auction/AgitAuctionInfo.htm";
					final Auction a = AuctionManager.getInstance().getAuction(auctionId);
					
					final NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(filename);
					if (a != null)
					{
						html.replace("%AGIT_NAME%", a.getItemName());
						html.replace("%OWNER_PLEDGE_NAME%", a.getSellerClanName());
						html.replace("%OWNER_PLEDGE_MASTER%", a.getSellerName());
						html.replace("%AGIT_SIZE%", String.valueOf(ClanHallManager.getInstance().getClanHallById(a.getItemId()).getGrade() * 10));
						html.replace("%AGIT_LEASE%", String.valueOf(ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLease()));
						html.replace("%AGIT_LOCATION%", ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLocation());
						html.replace("%AGIT_AUCTION_END_YY%", String.valueOf(a.getEndDate().get(Calendar.YEAR)));
						html.replace("%AGIT_AUCTION_END_MM%", String.valueOf(a.getEndDate().get(Calendar.MONTH) + 1));
						html.replace("%AGIT_AUCTION_END_DD%", String.valueOf(a.getEndDate().get(Calendar.DAY_OF_MONTH)));
						html.replace("%AGIT_AUCTION_END_HH%", String.valueOf(a.getEndDate().get(Calendar.HOUR_OF_DAY)));
						html.replace("%AGIT_AUCTION_REMAIN%", String.valueOf((a.getEndDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis()) / 3600000) + " hours " + String.valueOf((((a.getEndDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis()) / 60000) % 60)) + " minutes");
						html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
						html.replace("%AGIT_AUCTION_COUNT%", String.valueOf(a.getBidders().size()));
						html.replace("%AGIT_AUCTION_DESC%", ClanHallManager.getInstance().getClanHallById(a.getItemId()).getDesc());
						html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_list");
						html.replace("%AGIT_LINK_BIDLIST%", "bypass -h npc_" + getObjectId() + "_bidlist " + a.getId());
						html.replace("%AGIT_LINK_RE%", "bypass -h npc_" + getObjectId() + "_bid1 " + a.getId());
					}
					else
					{
						_log.warning("Auctioneer Auction null for AuctionId : " + auctionId);
					}
					
					player.sendPacket(html);
				}
				catch (final Exception e)
				{
					player.sendMessage("Invalid auction!");
				}
				
				return;
			}
			else if (actualCommand.equalsIgnoreCase("bid"))
			{
				if (val.isEmpty())
				{
					return;
				}
				
				try
				{
					final int auctionId = Integer.parseInt(val);
					try
					{
						int bid = 0;
						if (st.countTokens() >= 1)
						{
							bid = Integer.parseInt(st.nextToken());
						}
						
						AuctionManager.getInstance().getAuction(auctionId).setBid(player, bid);
					}
					catch (final Exception e)
					{
						player.sendMessage("Invalid bid!");
					}
				}
				catch (final Exception e)
				{
					player.sendMessage("Invalid auction!");
				}
				
				return;
			}
			else if (actualCommand.equalsIgnoreCase("bid1"))
			{
				if ((player.getClan() == null) || (player.getClan().getLevel() < 2))
				{
					player.sendMessage("Your clan's level needs to be at least 2, before you can bid in an auction.");
					return;
				}
				
				if (val.isEmpty())
				{
					return;
				}
				
				if (((player.getClan().getAuctionBiddedAt() > 0) && (player.getClan().getAuctionBiddedAt() != Integer.parseInt(val))) || (player.getClan().getHasHideout() > 0))
				{
					player.sendPacket(new SystemMessage(676));
					return;
				}
				
				try
				{
					final String filename = "data/html/auction/AgitBid1.htm";
					
					int minimumBid = AuctionManager.getInstance().getAuction(Integer.parseInt(val)).getHighestBidderMaxBid();
					if (minimumBid == 0)
					{
						minimumBid = AuctionManager.getInstance().getAuction(Integer.parseInt(val)).getStartingBid();
					}
					
					final NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(filename);
					html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_bidding " + val);
					html.replace("%PLEDGE_ADENA%", String.valueOf(player.getClan().getWarehouse().getAdena()));
					html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(minimumBid + 1));
					html.replace("npc_%objectId%_bid", "npc_" + getObjectId() + "_bid " + val);
					player.sendPacket(html);
					return;
				}
				catch (final Exception e)
				{
					player.sendMessage("Invalid auction!");
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("list"))
			{
				if (Config.DEBUG)
				{
					_log.warning("cmd list: auction test started");
				}
				
				String items = "";
				final List<Auction> auctions = AuctionManager.getInstance().getAuctions();
				for (final Auction a : auctions)
				{
					items += "<tr>" + "<td>" + ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLocation() + "</td><td><a action=\"bypass -h npc_" + getObjectId() + "_bidding " + a.getId() + "\">" + a.getItemName() + "</a></td><td>" + a.getEndDate().get(Calendar.YEAR) + "/" + (a.getEndDate().get(Calendar.MONTH) + 1) + "/" + a.getEndDate().get(Calendar.DATE) + "</td><td>" + a.getStartingBid() + "</td>" + "</tr>";
				}
				
				final String filename = "data/html/auction/AgitAuctionList.htm";
				
				final NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(filename);
				html.replace("%itemsField%", items);
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("bidlist"))
			{
				int auctionId = 0;
				if (val.isEmpty())
				{
					if (player.getClan().getAuctionBiddedAt() <= 0)
					{
						return;
					}
					auctionId = player.getClan().getAuctionBiddedAt();
				}
				else
				{
					auctionId = Integer.parseInt(val);
				}
				if (Config.DEBUG)
				{
					_log.warning("cmd bidlist: auction test started");
				}
				
				String biders = "";
				final Map<Integer, Bidder> bidders = AuctionManager.getInstance().getAuction(auctionId).getBidders();
				for (final Bidder b : bidders.values())
				{
					biders += "<tr>" + "<td>" + b.getClanName() + "</td><td>" + b.getName() + "</td><td>" + b.getTimeBid().get(Calendar.YEAR) + "/" + (b.getTimeBid().get(Calendar.MONTH) + 1) + "/" + b.getTimeBid().get(Calendar.DATE) + "</td><td>" + b.getBid() + "</td>" + "</tr>";
				}
				final String filename = "data/html/auction/AgitBidderList.htm";
				
				final NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(filename);
				html.replace("%AGIT_LIST%", biders);
				html.replace("%x%", val);
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("selectedItems"))
			{
				if ((player.getClan() != null) && (player.getClan().getHasHideout() == 0) && (player.getClan().getAuctionBiddedAt() > 0))
				{
					final String filename = "data/html/auction/AgitBidInfo.htm";
					final NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(filename);
					final Auction a = AuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt());
					if (a != null)
					{
						html.replace("%AGIT_NAME%", a.getItemName());
						html.replace("%OWNER_PLEDGE_NAME%", a.getSellerClanName());
						html.replace("%OWNER_PLEDGE_MASTER%", a.getSellerName());
						html.replace("%AGIT_SIZE%", String.valueOf(ClanHallManager.getInstance().getClanHallById(a.getItemId()).getGrade() * 10));
						html.replace("%AGIT_LEASE%", String.valueOf(ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLease()));
						html.replace("%AGIT_LOCATION%", ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLocation());
						html.replace("%AGIT_AUCTION_END_YY%", String.valueOf(a.getEndDate().get(Calendar.YEAR)));
						html.replace("%AGIT_AUCTION_END_MM%", String.valueOf(a.getEndDate().get(Calendar.MONTH) + 1));
						html.replace("%AGIT_AUCTION_END_DD%", String.valueOf(a.getEndDate().get(Calendar.DAY_OF_MONTH)));
						html.replace("%AGIT_AUCTION_END_HH%", String.valueOf(a.getEndDate().get(Calendar.HOUR_OF_DAY)));
						html.replace("%AGIT_AUCTION_REMAIN%", String.valueOf((a.getEndDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis()) / 3600000) + " hours " + String.valueOf((((a.getEndDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis()) / 60000) % 60)) + " minutes");
						html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
						html.replace("%AGIT_AUCTION_MYBID%", String.valueOf(a.getBidders().get(player.getClanId()).getBid()));
						html.replace("%AGIT_AUCTION_DESC%", ClanHallManager.getInstance().getClanHallById(a.getItemId()).getDesc());
						html.replace("%objectId%", String.valueOf(getObjectId()));
					}
					else
					{
						_log.warning("Auctioneer Auction null for AuctionBiddedAt : " + player.getClan().getAuctionBiddedAt());
					}
					
					player.sendPacket(html);
					return;
				}
				else if ((player.getClan() != null) && (AuctionManager.getInstance().getAuction(player.getClan().getHasHideout()) != null))
				{
					final String filename = "data/html/auction/AgitSaleInfo.htm";
					final NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(filename);
					final Auction a = AuctionManager.getInstance().getAuction(player.getClan().getHasHideout());
					
					if (a != null)
					{
						html.replace("%AGIT_NAME%", a.getItemName());
						html.replace("%AGIT_OWNER_PLEDGE_NAME%", a.getSellerClanName());
						html.replace("%OWNER_PLEDGE_MASTER%", a.getSellerName());
						html.replace("%AGIT_SIZE%", String.valueOf(ClanHallManager.getInstance().getClanHallById(a.getItemId()).getGrade() * 10));
						html.replace("%AGIT_LEASE%", String.valueOf(ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLease()));
						html.replace("%AGIT_LOCATION%", ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLocation());
						html.replace("%AGIT_AUCTION_END_YY%", String.valueOf(a.getEndDate().get(Calendar.YEAR)));
						html.replace("%AGIT_AUCTION_END_MM%", String.valueOf(a.getEndDate().get(Calendar.MONTH) + 1));
						html.replace("%AGIT_AUCTION_END_DD%", String.valueOf(a.getEndDate().get(Calendar.DAY_OF_MONTH)));
						html.replace("%AGIT_AUCTION_END_HH%", String.valueOf(a.getEndDate().get(Calendar.HOUR_OF_DAY)));
						html.replace("%AGIT_AUCTION_REMAIN%", String.valueOf((a.getEndDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis()) / 3600000) + " hours " + String.valueOf((((a.getEndDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis()) / 60000) % 60)) + " minutes");
						html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
						html.replace("%AGIT_AUCTION_BIDCOUNT%", String.valueOf(a.getBidders().size()));
						html.replace("%AGIT_AUCTION_DESC%", ClanHallManager.getInstance().getClanHallById(a.getItemId()).getDesc());
						html.replace("%id%", String.valueOf(a.getId()));
						html.replace("%objectId%", String.valueOf(getObjectId()));
					}
					else
					{
						_log.warning("Auctioneer Auction null for getHasHideout : " + player.getClan().getHasHideout());
					}
					
					player.sendPacket(html);
					return;
				}
				else if ((player.getClan() != null) && (player.getClan().getHasHideout() != 0))
				{
					final int ItemId = player.getClan().getHasHideout();
					final String filename = "data/html/auction/AgitInfo.htm";
					final NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(filename);
					html.replace("%AGIT_NAME%", ClanHallManager.getInstance().getClanHallById(ItemId).getName());
					html.replace("%AGIT_OWNER_PLEDGE_NAME%", player.getClan().getName());
					html.replace("%OWNER_PLEDGE_MASTER%", player.getClan().getLeaderName());
					html.replace("%AGIT_SIZE%", String.valueOf(ClanHallManager.getInstance().getClanHallById(ItemId).getGrade() * 10));
					html.replace("%AGIT_LEASE%", String.valueOf(ClanHallManager.getInstance().getClanHallById(ItemId).getLease()));
					html.replace("%AGIT_LOCATION%", ClanHallManager.getInstance().getClanHallById(ItemId).getLocation());
					html.replace("%objectId%", String.valueOf(getObjectId()));
					player.sendPacket(html);
					return;
				}
			}
			else if (actualCommand.equalsIgnoreCase("cancelBid"))
			{
				final int bid = AuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt()).getBidders().get(player.getClanId()).getBid();
				final String filename = "data/html/auction/AgitBidCancel.htm";
				final NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(filename);
				html.replace("%AGIT_BID%", String.valueOf(bid));
				html.replace("%AGIT_BID_REMAIN%", String.valueOf((bid * 0.9)));
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("doCancelBid"))
			{
				if (AuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt()) != null)
				{
					AuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt()).cancelBid(player.getClanId());
					player.sendMessage("You have succesfully cancelled your bidding at the auction");
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("cancelAuction"))
			{
				if (!player.isClanLeader())
				{
					player.sendMessage("Only the clan leader has the privilege to do this.");
					return;
				}
				final String filename = "data/html/auction/AgitSaleCancel.htm";
				final NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(filename);
				html.replace("%AGIT_DEPOSIT%", String.valueOf(ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getLease()));
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("doCancelAuction"))
			{
				if (AuctionManager.getInstance().getAuction(player.getClan().getHasHideout()) != null)
				{
					AuctionManager.getInstance().getAuction(player.getClan().getHasHideout()).cancelAuction();
					player.sendMessage("Your auction has been canceled");
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("sale2"))
			{
				final String filename = "data/html/auction/AgitSale2.htm";
				final NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(filename);
				html.replace("%AGIT_LAST_PRICE%", String.valueOf(ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getLease()));
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("sale"))
			{
				if (!player.isClanLeader())
				{
					player.sendMessage("Only the clan leader has the privilege to do this.");
					return;
				}
				final String filename = "data/html/auction/AgitSale1.htm";
				final NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(filename);
				html.replace("%AGIT_DEPOSIT%", String.valueOf(ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getLease()));
				html.replace("%AGIT_PLEDGE_ADENA%", String.valueOf(player.getClan().getWarehouse().getAdena()));
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("rebid"))
			{
				if (!player.isClanLeader())
				{
					player.sendMessage("Only the clan leader has the privilege to do this.");
					return;
				}
				try
				{
					final String filename = "data/html/auction/AgitBid2.htm";
					final NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(filename);
					final Auction a = AuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt());
					if (a != null)
					{
						html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
						html.replace("%AGIT_AUCTION_END_YY%", String.valueOf(a.getEndDate().get(Calendar.YEAR)));
						html.replace("%AGIT_AUCTION_END_MM%", String.valueOf(a.getEndDate().get(Calendar.MONTH) + 1));
						html.replace("%AGIT_AUCTION_END_DD%", String.valueOf(a.getEndDate().get(Calendar.DAY_OF_MONTH)));
						html.replace("%AGIT_AUCTION_END_HH%", String.valueOf(a.getEndDate().get(Calendar.HOUR_OF_DAY)));
						html.replace("npc_%objectId%_bid1", "npc_" + getObjectId() + "_bid1 " + a.getId());
					}
					else
					{
						_log.warning("Auctioneer Auction null for AuctionBiddedAt : " + player.getClan().getAuctionBiddedAt());
					}
					
					player.sendPacket(html);
				}
				catch (final Exception e)
				{
					player.sendMessage("Invalid auction!");
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("location"))
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile("data/html/auction/location.htm");
				html.replace("%location%", MapRegionTable.getInstance().getClosestTownName(player));
				html.replace("%LOCATION%", getPictureName(player));
				player.sendPacket(html);
				return;
			}
			else if (actualCommand.equalsIgnoreCase("start"))
			{
				showMessageWindow(player);
				return;
			}
		}
		
		super.onBypassFeedback(player, command);
	}
	
	public void showMessageWindow(L2PcInstance player)
	{
		String filename = "data/html/auction/auction-no.htm";
		
		final int condition = validateCondition(player);
		if (condition == Cond_Busy_Because_Of_Siege)
		{
			filename = "data/html/auction/auction-busy.htm"; // Busy because of siege
		}
		else
		{
			filename = "data/html/auction/auction.htm";
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getNpcId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
	
	private int validateCondition(L2PcInstance player)
	{
		if ((getCastle() != null) && (getCastle().getCastleId() > 0))
		{
			if (getCastle().getSiege().getIsInProgress())
			{
				return Cond_Busy_Because_Of_Siege; // Busy because of siege
			}
			return Cond_Regular;
		}
		
		return Cond_All_False;
	}
	
	private String getPictureName(L2PcInstance plyr)
	{
		final int nearestTownId = MapRegionTable.getInstance().getMapRegion(plyr.getX(), plyr.getY());
		String nearestTown;
		
		switch (nearestTownId)
		{
			case 5:
				nearestTown = "GLUDIO";
				break;
			case 6:
				nearestTown = "GLUDIN";
				break;
			case 7:
				nearestTown = "DION";
				break;
			case 8:
				nearestTown = "GIRAN";
				break;
			
			case 15:
				nearestTown = "GODARD";
				break;
			
			default:
				nearestTown = "ADEN";
				break;
			
		}
		
		return nearestTown;
	}
}