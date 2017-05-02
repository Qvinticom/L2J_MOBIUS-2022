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
import java.util.StringTokenizer;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.data.sql.impl.TeleportLocationTable;
import com.l2jmobius.gameserver.data.xml.impl.MultisellData;
import com.l2jmobius.gameserver.data.xml.impl.TeleportersData;
import com.l2jmobius.gameserver.datatables.ItemTable;
import com.l2jmobius.gameserver.enums.InstanceType;
import com.l2jmobius.gameserver.instancemanager.CastleManager;
import com.l2jmobius.gameserver.instancemanager.SiegeManager;
import com.l2jmobius.gameserver.instancemanager.TownManager;
import com.l2jmobius.gameserver.model.L2TeleportLocation;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.templates.L2NpcTemplate;
import com.l2jmobius.gameserver.model.itemcontainer.Inventory;
import com.l2jmobius.gameserver.model.items.L2Item;
import com.l2jmobius.gameserver.model.teleporter.TeleportHolder;
import com.l2jmobius.gameserver.model.teleporter.TeleportLocation;
import com.l2jmobius.gameserver.model.teleporter.TeleportType;
import com.l2jmobius.gameserver.model.zone.ZoneId;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jmobius.gameserver.util.Util;

/**
 * @author NightMarez
 */
public final class L2TeleporterInstance extends L2Npc
{
	private static final int COND_ALL_FALSE = 0;
	private static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	private static final int COND_OWNER = 2;
	private static final int COND_REGULAR = 3;
	
	public L2TeleporterInstance(L2NpcTemplate template)
	{
		super(template);
		setInstanceType(InstanceType.L2TeleporterInstance);
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if (attacker.isMonster())
		{
			return true;
		}
		
		return super.isAutoAttackable(attacker);
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
					_log.warning(player + " attempted to use nobles teleport without being nobles!");
					break;
				}
				
				final TeleportHolder holder = TeleportersData.getInstance().getHolder(getId());
				if (holder == null)
				{
					_log.warning(player + " requested show teleports for npc with no teleport data " + toString());
					break;
				}
				
				final NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
				msg.setFile(player.getHtmlPrefix(), "data/html/teleporter/teleports.htm");
				final StringBuilder sb = new StringBuilder();
				final StringBuilder sb_f = new StringBuilder();
				final int questZoneId = player.getQuestZoneId();
				
				for (TeleportLocation loc : holder.getLocations(type))
				{
					final int id = loc.getId();
					final boolean isQuestTeleport = (questZoneId >= 0) && (loc.getQuestZoneId() == questZoneId);
					
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
					
					if (isQuestTeleport)
					{
						sb_f.append("<button align=left icon=\"quest\" action=\"bypass -h npc_" + getObjectId() + "_teleport " + type.ordinal() + " " + id + "\" msg=\"811;" + confirmDesc + "\">" + finalName + "</button>");
					}
					else
					{
						sb.append("<button align=left icon=\"teleport\" action=\"bypass -h npc_" + getObjectId() + "_teleport " + type.ordinal() + " " + id + "\" msg=\"811;" + confirmDesc + "\">" + finalName + "</button>");
					}
				}
				sb_f.append(sb.toString());
				msg.replace("%locations%", sb_f.toString());
				player.sendPacket(msg);
				break;
			}
			case "teleport":
			{
				final int typeId = parseNextInt(st, -1);
				if ((typeId < 0) || (typeId > TeleportType.values().length))
				{
					_log.warning(player + " attempted to use incorrect teleport type: " + typeId);
					return;
				}
				
				final TeleportType type = TeleportType.values()[typeId];
				if (((type == TeleportType.NOBLES_TOKEN) || (type == TeleportType.NOBLES_ADENA)) && !player.isNoble())
				{
					_log.warning(player + " attempted to use nobles teleport without being nobles!");
					break;
				}
				
				final int locId = parseNextInt(st, -1);
				final TeleportHolder holder = TeleportersData.getInstance().getHolder(getId());
				if (holder == null)
				{
					_log.warning(player + " requested show teleports for npc with no teleport data " + toString());
					break;
				}
				final TeleportLocation loc = holder.getLocation(type, locId);
				if (loc == null)
				{
					_log.warning(player + " attempted to use not existing teleport location id: " + locId);
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
				else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_GK && (player.getReputation() < 0))
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
			if (!player.isSubClassActive() && (player.getLevel() < (Config.MAX_FREE_TELEPORT_LEVEL + 1)))
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
		return (!Config.ALT_GAME_FREE_TELEPORT && ((player.getLevel() > Config.MAX_FREE_TELEPORT_LEVEL) || player.isSubClassActive()) && ((loc.getFeeId() != 0) && (loc.getFeeCount() > 0)));
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
			case MultisellData.PC_CAFE_POINTS:
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
			case MultisellData.RAIDBOSS_POINTS:
			{
				return "Raid Points";
			}
		}
		return "Unknown item: " + itemId;
	}
	
	private void processLegacyBypass(L2PcInstance player, String command)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		final int condition = validateCondition(player);
		
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String actualCommand = st.nextToken(); // Get actual command
		
		if (player.isAffectedBySkill(6201) || player.isAffectedBySkill(6202) || player.isAffectedBySkill(6203))
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			
			final String filename = "data/html/teleporter/epictransformed.htm";
			
			html.setFile(player.getHtmlPrefix(), filename);
			html.replace("%objectId%", String.valueOf(getObjectId()));
			html.replace("%npcname%", getName());
			player.sendPacket(html);
			return;
		}
		else if (actualCommand.equalsIgnoreCase("goto"))
		{
			final int npcId = getId();
			
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
			
			final int whereTo = Integer.parseInt(st.nextToken());
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
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		String filename = "data/html/teleporter/castleteleporter-no.htm";
		
		final int condition = validateCondition(player);
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
		final L2TeleportLocation list = TeleportLocationTable.getInstance().getTemplate(val);
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
			else if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_GK && (player.getReputation() < 0)) // karma
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
				final String filename = "data/html/teleporter/nobleteleporter-no.htm";
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
			
			final Calendar cal = Calendar.getInstance();
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
				
				player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ());
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
		if (CastleManager.getInstance().getCastle(this) == null)
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
