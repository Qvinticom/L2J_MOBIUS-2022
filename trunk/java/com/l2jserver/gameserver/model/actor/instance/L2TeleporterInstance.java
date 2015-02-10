/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.model.actor.instance;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.stream.Stream;

import com.l2jserver.Config;
import com.l2jserver.gameserver.cache.HtmCache;
import com.l2jserver.gameserver.data.sql.impl.TeleportLocationTable;
import com.l2jserver.gameserver.data.xml.impl.MultisellData;
import com.l2jserver.gameserver.data.xml.impl.TeleportersData;
import com.l2jserver.gameserver.datatables.ItemTable;
import com.l2jserver.gameserver.enums.InstanceType;
import com.l2jserver.gameserver.instancemanager.CastleManager;
import com.l2jserver.gameserver.instancemanager.SiegeManager;
import com.l2jserver.gameserver.instancemanager.TownManager;
import com.l2jserver.gameserver.model.L2TeleportLocation;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.templates.L2NpcTemplate;
import com.l2jserver.gameserver.model.itemcontainer.Inventory;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.model.teleporter.TeleportHolder;
import com.l2jserver.gameserver.model.teleporter.TeleportLocation;
import com.l2jserver.gameserver.model.teleporter.TeleportType;
import com.l2jserver.gameserver.model.zone.ZoneId;
import com.l2jserver.gameserver.network.NpcStringId;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ActionFailed;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jserver.gameserver.util.Util;

/**
 * @author NightMarez
 */
public final class L2TeleporterInstance extends L2Npc
{
	private static final int COND_ALL_FALSE = 0;
	private static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	private static final int COND_OWNER = 2;
	private static final int COND_REGULAR = 3;
	
	/**
	 * Creates a teleporter.
	 * @param template the teleporter NPC template
	 */
	public L2TeleporterInstance(L2NpcTemplate template)
	{
		super(template);
		setInstanceType(InstanceType.L2TeleporterInstance);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String cmd = st.nextToken();
		switch (cmd)
		{
			case "showNoblesSelect":
			{
				final NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
				msg.setFile(player.getHtmlPrefix(), "data/html/teleporter/" + (player.isNoble() ? "nobles_select" : "not_nobles") + ".htm");
				msg.replace("%objectId%", getObjectId());
				player.sendPacket(msg);
				break;
			}
			case "showTeleports":
			{
				final TeleportType type = parseTeleportType(st);
				if (((type == TeleportType.NOBLES_TOKEN) || (type == TeleportType.NOBLES_ADENA)) && !player.isNoble())
				{
					_log.log(Level.WARNING, player + " attempted to use nobles teleport without being nobles!");
					break;
				}
				
				final TeleportHolder holder = TeleportersData.getInstance().getHolder(getId());
				if (holder == null)
				{
					_log.log(Level.WARNING, player + " requested show teleports for npc with no teleport data " + toString());
					break;
				}
				
				final NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
				msg.setFile(player.getHtmlPrefix(), "data/html/teleporter/teleports.htm");
				final StringBuilder sb = new StringBuilder();
				final Collection<TeleportLocation> locs = holder.getLocations(type);
				final List<NpcStringId> questLocations = new ArrayList<>();
				for (QuestState qs : player.getAllQuestStates())
				{
					final NpcStringId npcString = qs.getQuestLocation();
					if ((npcString != null) && !questLocations.contains(npcString))
					{
						questLocations.add(npcString);
					}
				}
				
				final Stream<TeleportLocation> stream = !questLocations.isEmpty() ? locs.stream().sorted((o1, o2) -> questLocations.contains(o1.getNpcStringId()) ? 1 : questLocations.contains(o2.getNpcStringId()) ? -1 : 0) : locs.stream();
				stream.forEach(loc ->
				{
					final int id = loc.getId();
					
					String finalName = loc.getName();
					String confirmDesc = loc.getName();
					if (loc.getNpcStringId() != null)
					{
						finalName = "<fstring>" + loc.getNpcStringId().getId() + "</fstring>";
						confirmDesc = "F;" + loc.getNpcStringId().getId();
					}
					if (shouldPayFee(player, type, loc))
					{
						finalName += " - " + calculateFee(player, type, loc) + " " + getItemName(loc.getFeeId(), true);
					}
					sb.append("<button align=left icon=" + (!questLocations.contains(loc.getNpcStringId()) ? "teleport" : "quest") + " action=\"bypass -h npc_" + getObjectId() + "_teleport " + type.ordinal() + " " + id + "\" msg=\"811;" + confirmDesc + "\">" + finalName + "</button>");
				});
				msg.replace("%locations%", sb.toString());
				player.sendPacket(msg);
				break;
			}
			case "teleport":
			{
				final int typeId = parseNextInt(st, -1);
				if ((typeId < 0) || (typeId > TeleportType.values().length))
				{
					_log.log(Level.WARNING, player + " attempted to use incorrect teleport type: " + typeId);
					return;
				}
				
				final TeleportType type = TeleportType.values()[typeId];
				if (((type == TeleportType.NOBLES_TOKEN) || (type == TeleportType.NOBLES_ADENA)) && !player.isNoble())
				{
					_log.log(Level.WARNING, player + " attempted to use nobles teleport without being nobles!");
					break;
				}
				
				final int locId = parseNextInt(st, -1);
				final TeleportHolder holder = TeleportersData.getInstance().getHolder(getId());
				if (holder == null)
				{
					_log.log(Level.WARNING, player + " requested show teleports for npc with no teleport data " + toString());
					break;
				}
				final TeleportLocation loc = holder.getLocation(type, locId);
				if (loc == null)
				{
					_log.log(Level.WARNING, player + " attempted to use not existing teleport location id: " + locId);
					return;
				}
				
				// you cannot teleport to village that is in siege
				if (SiegeManager.getInstance().getSiege(loc.getX(), loc.getY(), loc.getZ()) != null)
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_TO_A_VILLAGE_THAT_IS_IN_A_SIEGE);
				}
				else if (TownManager.townHasCastleInSiege(loc.getX(), loc.getY()) && isInsideZone(ZoneId.TOWN))
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_TO_A_VILLAGE_THAT_IS_IN_A_SIEGE);
				}
				else if (getCastle().getSiege().isInProgress())
				{
					final NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
					msg.setFile(player.getHtmlPrefix(), "data/html/teleporter/castleteleporter-busy.htm");
					player.sendPacket(msg);
				}
				else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_GK && (player.getKarma() != 0)) // TODO: Update me when Karma is replaced with Reputation system!
				{
					player.sendMessage("Go away, you're not welcome here.");
				}
				else if (player.isCombatFlagEquipped())
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD);
				}
				else if (shouldPayFee(player, type, loc) && !player.destroyItemByItemId("Teleport", loc.getFeeId(), calculateFee(player, type, loc), this, true))
				{
					if (loc.getFeeId() == Inventory.ADENA_ID)
					{
						player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
					}
					else
					{
						player.sendMessage("You do not have enough " + getItemName(loc.getFeeId(), false));
					}
				}
				else if (!player.isAlikeDead())
				{
					player.teleToLocation(loc);
				}
				break;
			}
			default:
			{
				processLegacyBypass(player, command);
				break;
			}
		}
	}
	
	/**
	 * For characters below level 77 teleport service is free.<br>
	 * From 8.00 pm to 00.00 from Monday till Tuesday for all characters there's a 50% discount on teleportation services
	 * @param player
	 * @param type
	 * @param loc
	 * @return
	 */
	private long calculateFee(L2PcInstance player, TeleportType type, TeleportLocation loc)
	{
		if (type == TeleportType.NORMAL)
		{
			if (!player.isSubClassActive() && (player.getLevel() < 77))
			{
				return 0;
			}
			
			final Calendar cal = Calendar.getInstance();
			final int hour = cal.get(Calendar.HOUR_OF_DAY);
			final int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
			if ((hour >= 20) && ((dayOfWeek >= Calendar.MONDAY) && (dayOfWeek <= Calendar.TUESDAY)))
			{
				return loc.getFeeCount() / 2;
			}
		}
		return loc.getFeeCount();
	}
	
	protected boolean shouldPayFee(L2PcInstance player, TeleportType type, TeleportLocation loc)
	{
		return (type != TeleportType.NORMAL) || (!Config.ALT_GAME_FREE_TELEPORT && ((player.getLevel() > 76) || player.isSubClassActive()) && ((loc.getFeeId() != 0) && (loc.getFeeCount() > 0)));
	}
	
	protected int parseNextInt(StringTokenizer st, int defaultVal)
	{
		if (st.hasMoreTokens())
		{
			final String token = st.nextToken();
			if (Util.isDigit(token))
			{
				return Integer.valueOf(token);
			}
		}
		return defaultVal;
	}
	
	protected TeleportType parseTeleportType(StringTokenizer st)
	{
		TeleportType type = TeleportType.NORMAL;
		if (st.hasMoreTokens())
		{
			final String typeToken = st.nextToken();
			for (TeleportType teleportType : TeleportType.values())
			{
				if (teleportType.name().equalsIgnoreCase(typeToken))
				{
					type = teleportType;
					break;
				}
			}
		}
		return type;
	}
	
	protected String getItemName(int itemId, boolean fstring)
	{
		if (fstring)
		{
			if (itemId == Inventory.ADENA_ID)
			{
				return "<fstring>1000308</fstring>";
			}
			else if (itemId == Inventory.ANCIENT_ADENA_ID)
			{
				return "<fstring>1000309</fstring>";
			}
		}
		final L2Item item = ItemTable.getInstance().getTemplate(itemId);
		if (item != null)
		{
			return item.getName();
		}
		switch (itemId)
		{
			case MultisellData.PC_BANG_POINTS:
			{
				return "Player Commendation Points";
			}
			case MultisellData.CLAN_REPUTATION:
			{
				return "Clan Reputation Points";
			}
			case MultisellData.FAME:
			{
				return "Fame";
			}
		}
		return "Unknown item: " + itemId;
	}
	
	private void processLegacyBypass(L2PcInstance player, String command)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		int condition = validateCondition(player);
		
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command
		
		if (player.isAffectedBySkill(6201) || player.isAffectedBySkill(6202) || player.isAffectedBySkill(6203))
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			
			String filename = "data/html/teleporter/epictransformed.htm";
			
			html.setFile(player.getHtmlPrefix(), filename);
			html.replace("%objectId%", String.valueOf(getObjectId()));
			html.replace("%npcname%", getName());
			player.sendPacket(html);
			return;
		}
		else if (actualCommand.equalsIgnoreCase("goto"))
		{
			int npcId = getId();
			
			switch (npcId)
			{
				case 32534: // Seed of Infinity
				case 32539:
					if (player.isFlyingMounted())
					{
						player.sendPacket(SystemMessageId.YOU_CANNOT_ENTER_A_SEED_WHILE_IN_A_FLYING_TRANSFORMATION_STATE);
						return;
					}
					break;
			}
			
			if (st.countTokens() <= 0)
			{
				return;
			}
			
			int whereTo = Integer.parseInt(st.nextToken());
			if (condition == COND_REGULAR)
			{
				doTeleport(player, whereTo);
				return;
			}
			else if (condition == COND_OWNER)
			{
				// TODO: Replace 0 with highest level when privilege level is implemented
				int minPrivilegeLevel = 0;
				if (st.countTokens() >= 1)
				{
					minPrivilegeLevel = Integer.parseInt(st.nextToken());
				}
				
				// TODO: Replace 10 with privilege level of player
				if (10 >= minPrivilegeLevel)
				{
					doTeleport(player, whereTo);
				}
				else
				{
					player.sendMessage("You don't have the sufficient access level to teleport there.");
				}
				return;
			}
		}
		else if (command.startsWith("Chat"))
		{
			Calendar cal = Calendar.getInstance();
			int val = 0;
			try
			{
				val = Integer.parseInt(command.substring(5));
			}
			catch (IndexOutOfBoundsException ioobe)
			{
			}
			catch (NumberFormatException nfe)
			{
			}
			
			if ((val == 1) && (player.getLevel() < 41))
			{
				showNewbieHtml(player);
				return;
			}
			else if ((val == 1) && (cal.get(Calendar.HOUR_OF_DAY) >= 20) && (cal.get(Calendar.HOUR_OF_DAY) <= 23) && ((cal.get(Calendar.DAY_OF_WEEK) == 1) || (cal.get(Calendar.DAY_OF_WEEK) == 7)))
			{
				showHalfPriceHtml(player);
				return;
			}
			showChatWindow(player, val);
		}
		super.onBypassFeedback(player, command);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}
		
		return "data/html/teleporter/" + pom + ".htm";
	}
	
	private void showNewbieHtml(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		
		String filename = "data/html/teleporter/free/" + getTemplate().getId() + ".htm";
		if (!HtmCache.getInstance().isLoadable(filename))
		{
			filename = "data/html/teleporter/" + getTemplate().getId() + "-1.htm";
		}
		
		html.setFile(player.getHtmlPrefix(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
	
	private void showHalfPriceHtml(L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		
		String filename = "data/html/teleporter/half/" + getId() + ".htm";
		if (!HtmCache.getInstance().isLoadable(filename))
		{
			filename = "data/html/teleporter/" + getId() + "-1.htm";
		}
		
		html.setFile(player.getHtmlPrefix(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		String filename = "data/html/teleporter/castleteleporter-no.htm";
		
		int condition = validateCondition(player);
		if (condition == COND_REGULAR)
		{
			super.showChatWindow(player);
			return;
		}
		else if (condition > COND_ALL_FALSE)
		{
			if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
			{
				filename = "data/html/teleporter/castleteleporter-busy.htm"; // Busy because of siege
			}
			else if (condition == COND_OWNER) // Clan owns castle
			{
				filename = getHtmlPath(getId(), 0); // Owner message window
			}
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player.getHtmlPrefix(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
	
	private void doTeleport(L2PcInstance player, int val)
	{
		L2TeleportLocation list = TeleportLocationTable.getInstance().getTemplate(val);
		if (list != null)
		{
			// you cannot teleport to village that is in siege
			if (SiegeManager.getInstance().getSiege(list.getLocX(), list.getLocY(), list.getLocZ()) != null)
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_TO_A_VILLAGE_THAT_IS_IN_A_SIEGE);
				return;
			}
			else if (TownManager.townHasCastleInSiege(list.getLocX(), list.getLocY()) && isInsideZone(ZoneId.TOWN))
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_TO_A_VILLAGE_THAT_IS_IN_A_SIEGE);
				return;
			}
			else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_GK && (player.getKarma() > 0)) // karma
			{
				player.sendMessage("Go away, you're not welcome here.");
				return;
			}
			else if (player.isCombatFlagEquipped())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD);
				return;
			}
			else if (list.getIsForNoble() && !player.isNoble())
			{
				String filename = "data/html/teleporter/nobleteleporter-no.htm";
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(player.getHtmlPrefix(), filename);
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%npcname%", getName());
				player.sendPacket(html);
				return;
			}
			else if (player.isAlikeDead())
			{
				return;
			}
			
			Calendar cal = Calendar.getInstance();
			int price = list.getPrice();
			
			if (player.getLevel() < 41)
			{
				price = 0;
			}
			else if (!list.getIsForNoble())
			{
				if ((cal.get(Calendar.HOUR_OF_DAY) >= 20) && (cal.get(Calendar.HOUR_OF_DAY) <= 23) && ((cal.get(Calendar.DAY_OF_WEEK) == 1) || (cal.get(Calendar.DAY_OF_WEEK) == 7)))
				{
					price /= 2;
				}
			}
			
			if (Config.ALT_GAME_FREE_TELEPORT || player.destroyItemByItemId("Teleport " + (list.getIsForNoble() ? " nobless" : ""), list.getItemId(), price, this, true))
			{
				if (Config.DEBUG)
				{
					_log.info("Teleporting player " + player.getName() + " to new location: " + list.getLocX() + ":" + list.getLocY() + ":" + list.getLocZ());
				}
				
				player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ(), false);
			}
		}
		else
		{
			_log.warning("No teleport destination with id:" + val);
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private int validateCondition(L2PcInstance player)
	{
		// Teleporter isn't on castle ground
		if (CastleManager.getInstance().getCastleIndex(this) < 0)
		{
			return COND_REGULAR; // Regular access
		}
		// Teleporter is on castle ground and siege is in progress
		else if (getCastle().getSiege().isInProgress())
		{
			return COND_BUSY_BECAUSE_OF_SIEGE; // Busy because of siege
		}
		// Teleporter is on castle ground and player is in a clan
		else if (player.getClan() != null)
		{
			if (getCastle().getOwnerId() == player.getClanId())
			{
				return COND_OWNER; // Owner
			}
		}
		
		return COND_ALL_FALSE;
	}
}
