/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.communityboard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.l2jserver.Config;
import com.l2jserver.L2DatabaseFactory;
import com.l2jserver.gameserver.cache.HtmCache;
import com.l2jserver.gameserver.data.sql.impl.ClanTable;
import com.l2jserver.gameserver.data.xml.impl.BuyListData;
import com.l2jserver.gameserver.data.xml.impl.MultisellData;
import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.handler.CommunityBoardHandler;
import com.l2jserver.gameserver.handler.IParseBoardHandler;
import com.l2jserver.gameserver.model.actor.L2Summon;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.serverpackets.BuyList;
import com.l2jserver.gameserver.network.serverpackets.ExBuySellList;
import com.l2jserver.gameserver.network.serverpackets.ShowBoard;

/**
 * Home board.
 * @author Zoey76
 */
public final class HomeBoard implements IParseBoardHandler
{
	// SQL Queries
	private static final String COUNT_FAVORITES = "SELECT COUNT(*) AS favorites FROM `bbs_favorites` WHERE `playerId`=?";
	
	private static final String[] COMMANDS =
	{
		"_bbshome",
		"_bbstop",
		"_bbsmultisell",
		"_bbssell",
		"_bbsteleport",
		"_bbsbuff"
	};
	
	@Override
	public String[] getCommunityBoardCommands()
	{
		return COMMANDS;
	}
	
	@Override
	public boolean parseCommunityBoardCommand(String command, L2PcInstance activeChar)
	{
		if (command.equals("_bbshome") || command.equals("_bbstop"))
		{
			final String customPath = Config.CUSTOM_CB_ENABLED ? "Custom/" : "";
			CommunityBoardHandler.getInstance().addBypass(activeChar, "Home", command);
			
			String html = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "html/CommunityBoard/" + customPath + "home.html");
			html = html.replaceAll("%fav_count%", String.valueOf(getFavoriteCount(activeChar)));
			html = html.replaceAll("%region_count%", String.valueOf(getRegionCount(activeChar)));
			html = html.replaceAll("%clan_count%", String.valueOf(getClansCount()));
			CommunityBoardHandler.separateAndSend(html, activeChar);
		}
		else if (command.startsWith("_bbstop;"))
		{
			final String customPath = Config.CUSTOM_CB_ENABLED ? "Custom/" : "";
			final String path = command.replace("_bbstop;", "");
			if ((path.length() > 0) && path.endsWith(".html"))
			{
				final String html = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "html/CommunityBoard/" + customPath + path);
				CommunityBoardHandler.separateAndSend(html, activeChar);
			}
		}
		else if (Config.CUSTOM_CB_ENABLED && Config.COMMUNITYBOARD_ENABLE_MULTISELLS && command.startsWith("_bbsmultisell"))
		{
			final String fullBypass = command.replace("_bbsmultisell;", "");
			final String[] buypassOptions = fullBypass.split(",");
			final int multisellId = Integer.parseInt(buypassOptions[0]);
			final String page = buypassOptions[1];
			final String html = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "html/CommunityBoard/Custom/" + page + ".html");
			CommunityBoardHandler.separateAndSend(html, activeChar);
			MultisellData.getInstance().separateAndSend(multisellId, activeChar, null, false);
		}
		else if (Config.CUSTOM_CB_ENABLED && Config.COMMUNITYBOARD_ENABLE_MULTISELLS && command.startsWith("_bbssell"))
		{
			final String page = command.replace("_bbssell;", "");
			final String html = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "html/CommunityBoard/Custom/" + page + ".html");
			CommunityBoardHandler.separateAndSend(html, activeChar);
			activeChar.sendPacket(new BuyList(BuyListData.getInstance().getBuyList(423), activeChar.getAdena(), 0));
			activeChar.sendPacket(new ExBuySellList(activeChar, false));
		}
		else if (Config.CUSTOM_CB_ENABLED && Config.COMMUNITYBOARD_ENABLE_TELEPORTS && command.startsWith("_bbsteleport"))
		{
			final String fullBypass = command.replace("_bbsteleport;", "");
			final String[] buypassOptions = fullBypass.split(",");
			final int x = Integer.parseInt(buypassOptions[0]);
			final int y = Integer.parseInt(buypassOptions[1]);
			final int z = Integer.parseInt(buypassOptions[2]);
			if (activeChar.getInventory().getInventoryItemCount(Config.COMMUNITYBOARD_CURRENCY, -1) < Config.COMMUNITYBOARD_TELEPORT_PRICE)
			{
				activeChar.sendMessage("Not enough currency!");
				return false;
			}
			activeChar.sendPacket(new ShowBoard());
			activeChar.getInventory().destroyItemByItemId("CB_Teleport", Config.COMMUNITYBOARD_CURRENCY, Config.COMMUNITYBOARD_TELEPORT_PRICE, activeChar, activeChar);
			activeChar.teleToLocation(x, y, z, 0);
		}
		else if (Config.CUSTOM_CB_ENABLED && Config.COMMUNITYBOARD_ENABLE_BUFFS && command.startsWith("_bbsbuff"))
		{
			final String fullBypass = command.replace("_bbsbuff;", "");
			final String[] buypassOptions = fullBypass.split(",");
			final int buffId = Integer.parseInt(buypassOptions[0]);
			final int buffLevel = Integer.parseInt(buypassOptions[1]);
			final String page = buypassOptions[2];
			if (activeChar.getInventory().getInventoryItemCount(Config.COMMUNITYBOARD_CURRENCY, -1) < Config.COMMUNITYBOARD_BUFF_PRICE)
			{
				activeChar.sendMessage("Not enough currency!");
			}
			else
			{
				activeChar.getInventory().destroyItemByItemId("CB_Buff", Config.COMMUNITYBOARD_CURRENCY, Config.COMMUNITYBOARD_BUFF_PRICE, activeChar, activeChar);
				SkillData.getInstance().getSkill(buffId, buffLevel).applyEffects(activeChar, activeChar);
				if (activeChar.getServitors().size() > 0)
				{
					for (L2Summon summon : activeChar.getServitors().values())
					{
						SkillData.getInstance().getSkill(buffId, buffLevel).applyEffects(summon, summon);
					}
				}
				if (activeChar.hasPet())
				{
					SkillData.getInstance().getSkill(buffId, buffLevel).applyEffects(activeChar.getPet(), activeChar.getPet());
				}
			}
			final String html = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "html/CommunityBoard/Custom/" + page + ".html");
			CommunityBoardHandler.separateAndSend(html, activeChar);
		}
		return true;
	}
	
	/**
	 * Gets the Favorite links for the given player.
	 * @param player the player
	 * @return the favorite links count
	 */
	private static int getFavoriteCount(L2PcInstance player)
	{
		int count = 0;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(COUNT_FAVORITES))
		{
			ps.setInt(1, player.getObjectId());
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					count = rs.getInt("favorites");
				}
			}
		}
		catch (Exception e)
		{
			LOG.warning(FavoriteBoard.class.getSimpleName() + ": Coudn't load favorites count for player " + player.getName());
		}
		return count;
	}
	
	/**
	 * Gets the registered regions count for the given player.
	 * @param player the player
	 * @return the registered regions count
	 */
	private static int getRegionCount(L2PcInstance player)
	{
		return 0; // TODO: Implement.
	}
	
	/**
	 * Gets the clans count.
	 * @return the clans count
	 */
	private static int getClansCount()
	{
		return ClanTable.getInstance().getClans().length;
	}
}
