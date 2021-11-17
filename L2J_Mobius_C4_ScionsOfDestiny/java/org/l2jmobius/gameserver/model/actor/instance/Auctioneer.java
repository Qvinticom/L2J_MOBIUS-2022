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
package org.l2jmobius.gameserver.model.actor.instance;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.data.sql.ClanHallTable;
import org.l2jmobius.gameserver.data.xml.MapRegionData;
import org.l2jmobius.gameserver.instancemanager.AuctionManager;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.residences.ClanHallAuction;
import org.l2jmobius.gameserver.model.residences.ClanHallAuction.Bidder;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.MyTargetSelected;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.ValidateLocation;

public class Auctioneer extends Folk
{
	private static final int COND_ALL_FALSE = 0;
	private static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	private static final int COND_REGULAR = 3;
	
	private final Map<Integer, ClanHallAuction> _pendingAuctions = new HashMap<>();
	
	public Auctioneer(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onAction(Player player)
	{
		if (!canTarget(player))
		{
			return;
		}
		
		player.setLastFolkNPC(this);
		
		// Check if the Player already target the Npc
		if (this != player.getTarget())
		{
			// Set the target of the Player player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the Player player
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));
			
			// Send a Server->Client packet ValidateLocation to correct the Npc position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else if (!canInteract(player)) // Calculate the distance between the Player and the Npc
		{
			// Notify the Player AI with AI_INTENTION_INTERACT
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
		}
		else
		{
			showMessageWindow(player);
		}
		// Send a Server->Client ActionFailed to the Player in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		final int condition = validateCondition(player);
		if (condition == COND_ALL_FALSE)
		{
			// TODO: html
			player.sendMessage("Wrong conditions.");
			return;
		}
		
		if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
		{
			// TODO: html
			player.sendMessage("Busy because of siege.");
			return;
		}
		else if (condition == COND_REGULAR)
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
				if (val.equals(""))
				{
					return;
				}
				
				try
				{
					final int days = Integer.parseInt(val);
					try
					{
						final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
						int bid = 0;
						if (st.countTokens() >= 1)
						{
							bid = Integer.parseInt(st.nextToken());
						}
						
						final ClanHallAuction a = new ClanHallAuction(player.getClan().getHideoutId(), player.getClan(), days * 86400000, bid, ClanHallTable.getInstance().getClanHallByOwner(player.getClan()).getName());
						if (_pendingAuctions.get(a.getId()) != null)
						{
							_pendingAuctions.remove(a.getId());
						}
						
						_pendingAuctions.put(a.getId(), a);
						
						final String filename = "data/html/auction/AgitSale3.htm";
						final NpcHtmlMessage html = new NpcHtmlMessage(1);
						html.setFile(filename);
						html.replace("%x%", val);
						html.replace("%AGIT_AUCTION_END%", format.format(a.getEndDate()));
						html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
						html.replace("%AGIT_AUCTION_MIN%", String.valueOf(a.getStartingBid()));
						html.replace("%AGIT_AUCTION_DESC%", ClanHallTable.getInstance().getClanHallByOwner(player.getClan()).getDesc());
						html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_sale2");
						html.replace("%objectId%", String.valueOf(getObjectId()));
						player.sendPacket(html);
					}
					catch (Exception e)
					{
						player.sendMessage("Invalid bid!");
					}
				}
				catch (Exception e)
				{
					player.sendMessage("Invalid auction duration!");
				}
				return;
			}
			if (actualCommand.equalsIgnoreCase("confirmAuction"))
			{
				try
				{
					final ClanHallAuction a = _pendingAuctions.get(player.getClan().getHideoutId());
					a.confirmAuction();
					_pendingAuctions.remove(player.getClan().getHideoutId());
				}
				catch (Exception e)
				{
					player.sendMessage("Invalid auction");
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("bidding"))
			{
				if (val.equals(""))
				{
					return;
				}
				
				try
				{
					final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
					final int auctionId = Integer.parseInt(val);
					final String filename = "data/html/auction/AgitAuctionInfo.htm";
					final ClanHallAuction a = AuctionManager.getInstance().getAuction(auctionId);
					final NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(filename);
					if (a != null)
					{
						html.replace("%AGIT_NAME%", a.getItemName());
						html.replace("%OWNER_PLEDGE_NAME%", a.getSellerClanName());
						html.replace("%OWNER_PLEDGE_MASTER%", a.getSellerName());
						html.replace("%AGIT_SIZE%", "30 ");
						html.replace("%AGIT_LEASE%", String.valueOf(ClanHallTable.getInstance().getClanHallById(a.getItemId()).getLease()));
						html.replace("%AGIT_LOCATION%", ClanHallTable.getInstance().getClanHallById(a.getItemId()).getLocation());
						html.replace("%AGIT_AUCTION_END%", format.format(a.getEndDate()));
						html.replace("%AGIT_AUCTION_REMAIN%", ((a.getEndDate() - Chronos.currentTimeMillis()) / 3600000) + " hours " + (((a.getEndDate() - Chronos.currentTimeMillis()) / 60000) % 60) + " minutes");
						html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
						html.replace("%AGIT_AUCTION_COUNT%", String.valueOf(a.getBidders().size()));
						html.replace("%AGIT_AUCTION_DESC%", ClanHallTable.getInstance().getClanHallById(a.getItemId()).getDesc());
						html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_list");
						html.replace("%AGIT_LINK_BIDLIST%", "bypass -h npc_" + getObjectId() + "_bidlist " + a.getId());
						html.replace("%AGIT_LINK_RE%", "bypass -h npc_" + getObjectId() + "_bid1 " + a.getId());
					}
					else
					{
						LOGGER.warning("Auctioneer Auction null for AuctionId : " + auctionId);
					}
					player.sendPacket(html);
				}
				catch (Exception e)
				{
					player.sendMessage("Invalid auction!");
				}
				
				return;
			}
			else if (actualCommand.equalsIgnoreCase("bid"))
			{
				if (val.equals(""))
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
					catch (NumberFormatException e)
					{
						player.sendMessage("Invalid bid!");
					}
					catch (Exception e)
					{
					}
				}
				catch (Exception e)
				{
					player.sendMessage("Invalid auction!");
				}
				
				return;
			}
			else if (actualCommand.equalsIgnoreCase("bid1"))
			{
				if ((player.getClan() == null) || (player.getClan().getLevel() < 2))
				{
					player.sendMessage("Your clan's level needs to be at least 2, before you can bid in an auction");
					return;
				}
				
				if (val.equals(""))
				{
					return;
				}
				
				if (((player.getClan().getAuctionBiddedAt() > 0) && (player.getClan().getAuctionBiddedAt() != Integer.parseInt(val))) || (player.getClan().getHideoutId() > 0))
				{
					player.sendMessage("You can't bid at more than one auction");
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
					html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(minimumBid));
					html.replace("npc_%objectId%_bid", "npc_" + getObjectId() + "_bid " + val);
					player.sendPacket(html);
				}
				catch (Exception e)
				{
					player.sendMessage("Invalid auction!");
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("list"))
			{
				final List<ClanHallAuction> auctions = AuctionManager.getInstance().getAuctions();
				final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
				/** Limit for make new page, prevent client crash **/
				int limit = 15;
				int start;
				int i = 1;
				final double npage = Math.ceil((float) auctions.size() / limit);
				if (val.equals(""))
				{
					start = 1;
				}
				else
				{
					start = (limit * (Integer.parseInt(val) - 1)) + 1;
					limit *= Integer.parseInt(val);
				}
				
				String items = "";
				items += "<table width=280 border=0><tr>";
				for (int j = 1; j <= npage; j++)
				{
					items += "<td><center><a action=\"bypass -h npc_" + getObjectId() + "_list " + j + "\"> Page " + j + " </a></center></td>";
				}
				items += "</tr></table><table width=280 border=0>";
				for (ClanHallAuction a : auctions)
				{
					if (i > limit)
					{
						break;
					}
					else if (i < start)
					{
						i++;
						continue;
					}
					else
					{
						i++;
					}
					
					items += "<tr><td>" + ClanHallTable.getInstance().getClanHallById(a.getItemId()).getLocation() + "</td><td><a action=\"bypass -h npc_" + getObjectId() + "_bidding " + a.getId() + "\">" + a.getItemName() + "</a></td><td>" + format.format(a.getEndDate()) + "</td><td>" + a.getStartingBid() + "</td></tr>";
				}
				items += "</table>";
				final String filename = "data/html/auction/AgitAuctionList.htm";
				final NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(filename);
				html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
				html.replace("%itemsField%", items);
				player.sendPacket(html);
				
				return;
			}
			else if (actualCommand.equalsIgnoreCase("bidlist"))
			{
				int auctionId = 0;
				if (val.equals(""))
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
				
				String biders = "";
				final Map<Integer, Bidder> bidders = AuctionManager.getInstance().getAuction(auctionId).getBidders();
				for (Bidder b : bidders.values())
				{
					biders += "<tr><td>" + b.getClanName() + "</td><td>" + b.getName() + "</td><td>" + b.getTimeBid().get(Calendar.YEAR) + "/" + (b.getTimeBid().get(Calendar.MONTH) + 1) + "/" + b.getTimeBid().get(Calendar.DATE) + "</td><td>" + b.getBid() + "</td></tr>";
				}
				final String filename = "data/html/auction/AgitBidderList.htm";
				final NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(filename);
				html.replace("%AGIT_LIST%", biders);
				html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_selectedItems");
				html.replace("%x%", val);
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				
				return;
			}
			else if (actualCommand.equalsIgnoreCase("selectedItems"))
			{
				if ((player.getClan() != null) && (player.getClan().getHideoutId() == 0) && (player.getClan().getAuctionBiddedAt() > 0))
				{
					final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
					final String filename = "data/html/auction/AgitBidInfo.htm";
					final NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(filename);
					final ClanHallAuction a = AuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt());
					if (a != null)
					{
						html.replace("%AGIT_NAME%", a.getItemName());
						html.replace("%OWNER_PLEDGE_NAME%", a.getSellerClanName());
						html.replace("%OWNER_PLEDGE_MASTER%", a.getSellerName());
						html.replace("%AGIT_SIZE%", "30 ");
						html.replace("%AGIT_LEASE%", String.valueOf(ClanHallTable.getInstance().getClanHallById(a.getItemId()).getLease()));
						html.replace("%AGIT_LOCATION%", ClanHallTable.getInstance().getClanHallById(a.getItemId()).getLocation());
						html.replace("%AGIT_AUCTION_END%", format.format(a.getEndDate()));
						html.replace("%AGIT_AUCTION_REMAIN%", ((a.getEndDate() - Chronos.currentTimeMillis()) / 3600000) + " hours " + (((a.getEndDate() - Chronos.currentTimeMillis()) / 60000) % 60) + " minutes");
						html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
						html.replace("%AGIT_AUCTION_MYBID%", String.valueOf(a.getBidders().get(player.getClanId()).getBid()));
						html.replace("%AGIT_AUCTION_DESC%", ClanHallTable.getInstance().getClanHallById(a.getItemId()).getDesc());
						html.replace("%objectId%", String.valueOf(getObjectId()));
						html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
					}
					else
					{
						LOGGER.warning("Auctioneer Auction null for AuctionBiddedAt : " + player.getClan().getAuctionBiddedAt());
					}
					player.sendPacket(html);
					
					return;
				}
				else if ((player.getClan() != null) && (AuctionManager.getInstance().getAuction(player.getClan().getHideoutId()) != null))
				{
					final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
					final String filename = "data/html/auction/AgitSaleInfo.htm";
					final NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(filename);
					final ClanHallAuction a = AuctionManager.getInstance().getAuction(player.getClan().getHideoutId());
					if (a != null)
					{
						html.replace("%AGIT_NAME%", a.getItemName());
						html.replace("%AGIT_OWNER_PLEDGE_NAME%", a.getSellerClanName());
						html.replace("%OWNER_PLEDGE_MASTER%", a.getSellerName());
						html.replace("%AGIT_SIZE%", "30 ");
						html.replace("%AGIT_LEASE%", String.valueOf(ClanHallTable.getInstance().getClanHallById(a.getItemId()).getLease()));
						html.replace("%AGIT_LOCATION%", ClanHallTable.getInstance().getClanHallById(a.getItemId()).getLocation());
						html.replace("%AGIT_AUCTION_END%", format.format(a.getEndDate()));
						html.replace("%AGIT_AUCTION_REMAIN%", ((a.getEndDate() - Chronos.currentTimeMillis()) / 3600000) + " hours " + (((a.getEndDate() - Chronos.currentTimeMillis()) / 60000) % 60) + " minutes");
						html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
						html.replace("%AGIT_AUCTION_BIDCOUNT%", String.valueOf(a.getBidders().size()));
						html.replace("%AGIT_AUCTION_DESC%", ClanHallTable.getInstance().getClanHallById(a.getItemId()).getDesc());
						html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
						html.replace("%id%", String.valueOf(a.getId()));
						html.replace("%objectId%", String.valueOf(getObjectId()));
					}
					else
					{
						LOGGER.warning("Auctioneer Auction null for getHideoutId : " + player.getClan().getHideoutId());
					}
					player.sendPacket(html);
					
					return;
				}
				else if ((player.getClan() != null) && (player.getClan().getHideoutId() != 0))
				{
					final int ItemId = player.getClan().getHideoutId();
					final String filename = "data/html/auction/AgitInfo.htm";
					final NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(filename);
					
					if (ClanHallTable.getInstance().getClanHallById(ItemId) != null)
					{
						html.replace("%AGIT_NAME%", ClanHallTable.getInstance().getClanHallById(ItemId).getName());
						html.replace("%AGIT_OWNER_PLEDGE_NAME%", player.getClan().getName());
						html.replace("%OWNER_PLEDGE_MASTER%", player.getClan().getLeaderName());
						html.replace("%AGIT_SIZE%", "30 ");
						html.replace("%AGIT_LEASE%", String.valueOf(ClanHallTable.getInstance().getClanHallById(ItemId).getLease()));
						html.replace("%AGIT_LOCATION%", ClanHallTable.getInstance().getClanHallById(ItemId).getLocation());
						html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
						html.replace("%objectId%", String.valueOf(getObjectId()));
					}
					else
					{
						LOGGER.warning("Clan Hall ID NULL : " + ItemId + " Can be caused by concurent write in ClanHallManager");
					}
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
				html.replace("%AGIT_BID_REMAIN%", String.valueOf((int) (bid * 0.9)));
				html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_selectedItems");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				
				return;
			}
			else if (actualCommand.equalsIgnoreCase("doCancelBid"))
			{
				if (AuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt()) != null)
				{
					AuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt()).cancelBid(player.getClanId());
					player.sendMessage("You have succesfully canceled your bidding at the auction");
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("cancelAuction"))
			{
				if (((player.getClanPrivileges() & Clan.CP_CH_AUCTION) != Clan.CP_CH_AUCTION))
				{
					player.sendMessage("You don't have the right privilleges to do this");
					return;
				}
				final String filename = "data/html/auction/AgitSaleCancel.htm";
				final NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(filename);
				html.replace("%AGIT_DEPOSIT%", String.valueOf(ClanHallTable.getInstance().getClanHallByOwner(player.getClan()).getLease()));
				html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_selectedItems");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				
				return;
			}
			else if (actualCommand.equalsIgnoreCase("doCancelAuction"))
			{
				if (AuctionManager.getInstance().getAuction(player.getClan().getHideoutId()) != null)
				{
					AuctionManager.getInstance().getAuction(player.getClan().getHideoutId()).cancelAuction();
					player.sendMessage("Your auction has been canceled");
				}
				return;
			}
			else if (actualCommand.equalsIgnoreCase("sale2"))
			{
				final String filename = "data/html/auction/AgitSale2.htm";
				final NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(filename);
				html.replace("%AGIT_LAST_PRICE%", String.valueOf(ClanHallTable.getInstance().getClanHallByOwner(player.getClan()).getLease()));
				html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_sale");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				
				return;
			}
			else if (actualCommand.equalsIgnoreCase("sale"))
			{
				if (((player.getClanPrivileges() & Clan.CP_CH_AUCTION) != Clan.CP_CH_AUCTION))
				{
					player.sendMessage("You don't have the right privilleges to do this");
					return;
				}
				final String filename = "data/html/auction/AgitSale1.htm";
				final NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(filename);
				html.replace("%AGIT_DEPOSIT%", String.valueOf(ClanHallTable.getInstance().getClanHallByOwner(player.getClan()).getLease()));
				html.replace("%AGIT_PLEDGE_ADENA%", String.valueOf(player.getClan().getWarehouse().getAdena()));
				html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_selectedItems");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				
				return;
			}
			else if (actualCommand.equalsIgnoreCase("rebid"))
			{
				final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
				if (((player.getClanPrivileges() & Clan.CP_CH_AUCTION) != Clan.CP_CH_AUCTION))
				{
					player.sendMessage("You don't have the right privileges to do this");
					return;
				}
				try
				{
					final String filename = "data/html/auction/AgitBid2.htm";
					final NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(filename);
					final ClanHallAuction a = AuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt());
					if (a != null)
					{
						html.replace("%AGIT_AUCTION_BID%", String.valueOf(a.getBidders().get(player.getClanId()).getBid()));
						html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
						html.replace("%AGIT_AUCTION_END%", format.format(a.getEndDate()));
						html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_selectedItems");
						html.replace("npc_%objectId%_bid1", "npc_" + getObjectId() + "_bid1 " + a.getId());
					}
					else
					{
						LOGGER.warning("Auctioneer Auction null for AuctionBiddedAt : " + player.getClan().getAuctionBiddedAt());
					}
					player.sendPacket(html);
				}
				catch (Exception e)
				{
					player.sendMessage("Invalid auction!");
				}
				
				return;
			}
			else if (actualCommand.equalsIgnoreCase("location"))
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile("data/html/auction/location.htm");
				html.replace("%location%", MapRegionData.getInstance().getClosestTownName(player));
				html.replace("%LOCATION%", getPictureName(player));
				html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
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
	
	public void showMessageWindow(Player player)
	{
		String filename; // = "data/html/auction/auction-no.htm";
		
		final int condition = validateCondition(player);
		if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
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
	
	private int validateCondition(Player player)
	{
		if ((getCastle() != null) && (getCastle().getCastleId() > 0))
		{
			if (getCastle().getSiege().isInProgress())
			{
				return COND_BUSY_BECAUSE_OF_SIEGE; // Busy because of siege
			}
			return COND_REGULAR;
		}
		return COND_ALL_FALSE;
	}
	
	private String getPictureName(Player plyr)
	{
		final int nearestTownId = MapRegionData.getInstance().getMapRegion(plyr.getX(), plyr.getY());
		String nearestTown;
		
		switch (nearestTownId)
		{
			case 5:
			{
				nearestTown = "GLUDIO";
				break;
			}
			case 6:
			{
				nearestTown = "GLUDIN";
				break;
			}
			case 7:
			{
				nearestTown = "DION";
				break;
			}
			case 8:
			{
				nearestTown = "GIRAN";
				break;
			}
			case 14:
			{
				nearestTown = "RUNE";
				break;
			}
			case 15:
			{
				nearestTown = "GODARD";
				break;
			}
			case 16:
			{
				nearestTown = "SCHUTTGART";
				break;
			}
			default:
			{
				nearestTown = "ADEN";
				break;
			}
		}
		return nearestTown;
	}
}
