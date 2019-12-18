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
package org.l2jmobius.gameserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.data.CharTemplateTable;
import org.l2jmobius.gameserver.data.ClanTable;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.data.NpcTable;
import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.data.SpawnTable;
import org.l2jmobius.gameserver.data.TradeController;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.Spawn;
import org.l2jmobius.gameserver.model.TradeList;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.BuyList;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.ItemList;
import org.l2jmobius.gameserver.network.serverpackets.LeaveWorld;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUser;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import org.l2jmobius.gameserver.templates.Npc;

public class AdminCommands extends Thread
{
	private static Logger _log = Logger.getLogger(AdminCommands.class.getName());
	private static AdminCommands _instance;
	private static ClientThread clientShut;
	private static int secondsShut;
	private static Collection<Skill> adminSkills;
	private static String _characterToManipulate;
	
	public static AdminCommands getInstance()
	{
		if (_instance == null)
		{
			_instance = new AdminCommands();
		}
		return _instance;
	}
	
	public void handleCommands(ClientThread client, String command)
	{
		StringTokenizer st;
		NpcInstance target;
		String id;
		final PlayerInstance activeChar = client.getActiveChar();
		if (command.equals("admin_show"))
		{
			showMainPage(client);
		}
		else if (command.startsWith("admin_kill"))
		{
			final int objectId = Integer.parseInt(command.substring(11));
			final WorldObject temp = World.getInstance().findObject(objectId);
			if (temp instanceof NpcInstance)
			{
				target = (NpcInstance) temp;
				target.reduceCurrentHp((int) target.getCurrentHp() + 1, target);
			}
		}
		else if (command.startsWith("admin_delete"))
		{
			final int objectId = Integer.parseInt(command.substring(13));
			final WorldObject temp = World.getInstance().findObject(objectId);
			if (temp instanceof NpcInstance)
			{
				target = (NpcInstance) temp;
				target.deleteMe();
			}
		}
		else if (command.equals("admin_list_announcements"))
		{
			Announcements.getInstance().listAnnouncements(activeChar);
		}
		else if (command.equals("admin_reload_announcements"))
		{
			Announcements.getInstance().loadAnnouncements();
			Announcements.getInstance().listAnnouncements(activeChar);
		}
		else if (command.equals("admin_announce_announcements"))
		{
			for (PlayerInstance player : World.getInstance().getAllPlayers())
			{
				Announcements.getInstance().showAnnouncements(player);
			}
			Announcements.getInstance().listAnnouncements(activeChar);
		}
		else if (command.startsWith("admin_add_announcement"))
		{
			if (!command.equals("admin_add_announcement"))
			{
				try
				{
					final String val = command.substring(23);
					Announcements.getInstance().addAnnouncement(val);
					Announcements.getInstance().listAnnouncements(activeChar);
				}
				catch (StringIndexOutOfBoundsException e)
				{
				}
			}
		}
		else if (command.startsWith("admin_del_announcement"))
		{
			final int val = Integer.parseInt(command.substring(23));
			Announcements.getInstance().delAnnouncement(val);
			Announcements.getInstance().listAnnouncements(activeChar);
		}
		else if (command.equals("admin_show_moves"))
		{
			showHelpPage(client, "teleports.htm");
		}
		else if (command.equals("admin_show_spawns"))
		{
			showHelpPage(client, "spawns.htm");
		}
		if (command.startsWith("admin_buy"))
		{
			handleBuyRequest(client, command.substring(10));
		}
		else if (command.equals("admin_show_skills"))
		{
			showSkillsPage(client);
		}
		else if (command.equals("admin_remove_skills"))
		{
			removeSkillsPage(client);
		}
		else if (command.startsWith("admin_skill_list"))
		{
			showHelpPage(client, "skills.htm");
		}
		else if (command.startsWith("admin_skill_index"))
		{
			final String val = command.substring(18);
			showHelpPage(client, "skills/" + val + ".htm");
		}
		else if (command.startsWith("admin_spawn_index"))
		{
			final String val = command.substring(18);
			showHelpPage(client, "spawns/" + val + ".htm");
		}
		else if (command.equals("admin_character_disconnect"))
		{
			disconnectCharacter(client);
		}
		else if (command.equals("admin_show_teleport"))
		{
			showTeleportCharWindow(client);
		}
		else if (command.equals("admin_teleport_to_character"))
		{
			teleportToCharacter(client);
		}
		else if (command.equals("admin_add_exp_sp_to_character"))
		{
			addExpSp(client);
		}
		else if (command.equals("admin_edit_character"))
		{
			editCharacter(client);
		}
		else if (command.equals("admin_current_player"))
		{
			showCharacterList(client, _characterToManipulate);
		}
		else if (command.equals("admin_get_skills"))
		{
			adminGetSkills(client);
		}
		else if (command.equals("admin_reset_skills"))
		{
			adminResetSkills(client);
		}
		else if (command.startsWith("admin_move_to"))
		{
			try
			{
				final String val = command.substring(14);
				teleportTo(client, val);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Wrong or no Co-ordinates given.");
				showMainPage(client);
			}
		}
		else if (command.startsWith("admin_help"))
		{
			try
			{
				final String val = command.substring(11);
				showHelpPage(client, val);
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("admin_character_list"))
		{
			try
			{
				final String val = command.substring(21);
				showCharacterList(client, val);
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("admin_show_characters"))
		{
			try
			{
				final String val = command.substring(22);
				final int page = Integer.parseInt(val);
				listCharacters(client, page);
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("admin_find_character"))
		{
			try
			{
				final String val = command.substring(21);
				findCharacter(client, val);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("You didnt enter a character name to find.");
				listCharacters(client, 0);
			}
		}
		else if (command.startsWith("admin_add_exp_sp"))
		{
			try
			{
				final String val = command.substring(16);
				adminAddExpSp(client, val);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Error while adding Exp-Sp.");
				listCharacters(client, 0);
			}
		}
		else if (command.startsWith("admin_add_skill"))
		{
			try
			{
				final String val = command.substring(15);
				adminAddSkill(client, val);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Error while adding skill.");
				listCharacters(client, 0);
			}
		}
		else if (command.startsWith("admin_remove_skill"))
		{
			try
			{
				final String id2 = command.substring(19);
				final int idval = Integer.parseInt(id2);
				adminRemoveSkill(client, idval);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Error while removing skill.");
				listCharacters(client, 0);
			}
		}
		else if (command.startsWith("admin_save_modifications"))
		{
			try
			{
				final String val = command.substring(24);
				adminModifyCharacter(client, val);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Error while modifying character.");
				listCharacters(client, 0);
			}
		}
		else if (command.startsWith("admin_teleport_character"))
		{
			try
			{
				final String val = command.substring(25);
				teleportCharacter(client, val);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Wrong or no Co-ordinates given.");
				showTeleportCharWindow(client);
			}
		}
		else if (command.startsWith("admin_spawn_monster"))
		{
			try
			{
				final String val = command.substring(20);
				spawnMenu(client, val);
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("admin_spawn_confirm"))
		{
			try
			{
				final String val = command.substring(20);
				st = new StringTokenizer(val);
				if (st.countTokens() != 2)
				{
					id = st.nextToken();
					spawnMenu(client, id);
					return;
				}
				id = st.nextToken();
				final String targetName = st.nextToken();
				spawnMonster(client, id, targetName);
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("admin_server_shutdown"))
		{
			try
			{
				final int val = Integer.parseInt(command.substring(22));
				serverShutdown(client, val);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("You didnt enter the seconds untill server shutdowns.");
				serverShutdown(client, 0);
			}
		}
		else if (command.equals("admin_gm_shops"))
		{
			showHelpPage(client, "gmshops.htm");
		}
		else if (command.equals("admin_itemcreate"))
		{
			showHelpPage(client, "itemcreation.htm");
		}
		else if (command.startsWith("admin_create_item"))
		{
			try
			{
				final String val = command.substring(17);
				st = new StringTokenizer(val);
				if (st.countTokens() == 2)
				{
					id = st.nextToken();
					final int idval = Integer.parseInt(id);
					final String num = st.nextToken();
					final int numval = Integer.parseInt(num);
					createItem(client, idval, numval);
				}
				else if (st.countTokens() == 1)
				{
					id = st.nextToken();
					final int idval = Integer.parseInt(id);
					createItem(client, idval, 1);
				}
				else
				{
					showHelpPage(client, "itemcreation.htm");
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Error while creating item.");
			}
		}
	}
	
	private void handleBuyRequest(ClientThread client, String command)
	{
		final PlayerInstance player = client.getActiveChar();
		int val = -1;
		try
		{
			val = Integer.parseInt(command);
		}
		catch (Exception e)
		{
			_log.warning("admin buylist failed:" + command);
		}
		final TradeList list = TradeController.getInstance().getBuyList(val);
		if (list != null)
		{
			final BuyList bl = new BuyList(list, player.getAdena());
			player.sendPacket(bl);
		}
		else
		{
			_log.warning("no buylist with id:" + val);
		}
		player.sendPacket(new ActionFailed());
	}
	
	public void showMainPage(ClientThread client)
	{
		final PlayerInstance activeChar = client.getActiveChar();
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		final StringBuilder replyMSG = new StringBuilder("<html><title>Server Status</title>");
		replyMSG.append("<body>");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td>Players Online: " + World.getInstance().getAllPlayers().size() + "</td></tr>");
		replyMSG.append("<tr><td>Used Memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
		replyMSG.append(" bytes</td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("<br>");
		replyMSG.append("<br>");
		replyMSG.append("<center><button value=\"Help\" action=\"bypass -h admin_help admhelp.htm\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
		replyMSG.append("<center><button value=\"Shutdown Server\" action=\"bypass -h admin_server_shutdown 0\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
		replyMSG.append("<center><button value=\"List Characters\" action=\"bypass -h admin_show_characters 0\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
		replyMSG.append("<center><button value=\"Move to Location\" action=\"bypass -h admin_show_moves\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
		replyMSG.append("<center><button value=\"Spawn a Monster\" action=\"bypass -h admin_show_spawns\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
		replyMSG.append("<center><button value=\"Item Creation\" action=\"bypass -h admin_itemcreate\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
		replyMSG.append("<center><button value=\"GM Shop\" action=\"bypass -h admin_gm_shops\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
		replyMSG.append("<center><button value=\"Announcements\" action=\"bypass -h admin_list_announcements\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
		replyMSG.append("<br><br>");
		replyMSG.append("<right><button value=\"Close\" action=\"bypass -h admin_close_window\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></right>");
		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void teleportTo(ClientThread client, String coords)
	{
		final PlayerInstance activeChar = client.getActiveChar();
		final StringTokenizer st = new StringTokenizer(coords);
		final String x1 = st.nextToken();
		final int x = Integer.parseInt(x1);
		final String y1 = st.nextToken();
		final int y = Integer.parseInt(y1);
		final String z1 = st.nextToken();
		final int z = Integer.parseInt(z1);
		activeChar.teleToLocation(x, y, z);
		activeChar.sendMessage("You have been teleported to " + coords);
		showMainPage(client);
	}
	
	private void createItem(ClientThread client, int id, int num)
	{
		final PlayerInstance activeChar = client.getActiveChar();
		final ItemInstance createditem = ItemTable.getInstance().createItem(id);
		for (int i = 0; i < num; ++i)
		{
			activeChar.getInventory().addItem(createditem);
		}
		final ItemList il = new ItemList(activeChar, true);
		activeChar.sendPacket(il);
		activeChar.sendMessage("You have spawned " + num + " item(s) number " + id + " in your inventory.");
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		final StringBuilder replyMSG = new StringBuilder("<html><title>Item Creation Complete</title>");
		replyMSG.append("<body>");
		replyMSG.append("<center><button value=\"Back\" action=\"bypass -h admin_show\" width=40 height=15></center>");
		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void listCharacters(ClientThread client, int page)
	{
		final PlayerInstance activeChar = client.getActiveChar();
		final Collection<PlayerInstance> allWorldPlayers = World.getInstance().getAllPlayers();
		final PlayerInstance[] players = allWorldPlayers.toArray(new PlayerInstance[allWorldPlayers.size()]);
		final int maxCharactersPerPage = 20;
		int maxPages = players.length / maxCharactersPerPage;
		final int modulus = players.length % maxCharactersPerPage;
		if (modulus != 0)
		{
			++maxPages;
		}
		if (page > maxPages)
		{
			page = maxPages;
		}
		final int CharactersStart = maxCharactersPerPage * page;
		final int CharactersEnd = players.length - CharactersStart;
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		final StringBuilder replyMSG = new StringBuilder("<html><title>Characters List</title>");
		replyMSG.append("<body>");
		for (int x = 0; x < maxPages; ++x)
		{
			replyMSG.append("<a action=\"bypass -h admin_show_characters " + x + "\">Page" + x + 1 + "</a>\t");
		}
		replyMSG.append("<br>");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td>Name</td><td>Class</td><td>Level</td></tr>");
		for (int i = CharactersStart; i < CharactersEnd; ++i)
		{
			replyMSG.append("<tr><td><a action=\"bypass -h admin_character_list " + players[i].getName() + "\">" + players[i].getName() + "</a>" + "</td><td>" + CharTemplateTable.getInstance().getTemplate(players[i].getClassId()).getClassName() + "</td><td>" + players[i].getLevel() + "</td></tr>");
		}
		replyMSG.append("</table>");
		replyMSG.append("---------<p>");
		replyMSG.append("You can find a character by writing his name<p> and clicking Find bellow:");
		replyMSG.append("<edit var=\"character_name\" width=110>");
		replyMSG.append("<button value=\"Find\" action=\"bypass -h admin_find_character $character_name\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<center><button value=\"Back\" action=\"bypass -h admin_show\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void showCharacterList(ClientThread client, String charName)
	{
		final PlayerInstance activeChar = client.getActiveChar();
		final PlayerInstance player = World.getInstance().getPlayer(charName);
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		final StringBuilder replyMSG = new StringBuilder("<html><title>Character Information</title>");
		replyMSG.append("<body>");
		replyMSG.append("<br>");
		replyMSG.append("<center>" + player.getName() + "</center><p>");
		replyMSG.append("Clan: " + ClanTable.getInstance().getClan(player.getClanId()) + "<p>");
		replyMSG.append("Lv: " + player.getLevel() + " " + CharTemplateTable.getInstance().getTemplate(player.getClassId()).getClassName() + "<p>");
		replyMSG.append("Exp: " + player.getExp() + "<p>");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td>HP</td><td>" + player.getCurrentHp() + " / " + player.getMaxHp() + "</td><td>MP</td><td>" + player.getCurrentMp() + " / " + player.getMaxMp() + "</td></tr>");
		replyMSG.append("<tr><td>SP</td><td>" + player.getSp() + "</td><td>Load</td><td>" + player.getCurrentLoad() + " / " + player.getMaxLoad() + "</td></tr>");
		replyMSG.append("<tr><td>ClassId</td><td>" + player.getClassId() + "</td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("<br>");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td>Combat</td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td>P.ATK</td><td>" + player.getPhysicalAttack() + "</td><td>M.ATK</td><td>" + player.getMagicalAttack() + "</td></tr>");
		replyMSG.append("<tr><td>P.DEF</td><td>" + player.getPhysicalDefense() + "</td><td>M.DEF</td><td>" + player.getMagicalDefense() + "</td></tr>");
		replyMSG.append("<tr><td>Accuracy</td><td>" + player.getAccuracy() + "</td><td>Evasion</td><td>" + player.getEvasionRate() + "</td></tr>");
		replyMSG.append("<tr><td>Critical</td><td>" + player.getCriticalHit() + "</td><td>Speed</td><td>" + player.getRunSpeed() + "</td></tr>");
		replyMSG.append("<tr><td>ATK Spd</td><td>" + player.getPhysicalSpeed() + "</td><td>Casting Spd</td><td>" + player.getMagicalSpeed() + "</td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("<br>");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td>Basic</td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td>STR</td><td>" + player.getStr() + "</td><td>DEX</td><td>" + player.getDex() + "</td><td>CON</td><td>" + player.getCon() + "</td></tr>");
		replyMSG.append("<tr><td>INT</td><td>" + player.getInt() + "</td><td>WIT</td><td>" + player.getWit() + "</td><td>MEN</td><td>" + player.getMen() + "</td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("<br>");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td>Social</td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td>Karma</td><td>" + player.getKarma() + "</td><td>PvP</td><td>" + player.getPvpFlag() + " / " + player.getPvpKills() + "</td></tr>");
		replyMSG.append("</table><p>");
		replyMSG.append("Character Co-ordinates: " + player.getX() + " " + player.getY() + " " + player.getZ());
		replyMSG.append("<br>");
		replyMSG.append("<center><button value=\"Logout Character\" action=\"bypass -h admin_character_disconnect\" width=140 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
		replyMSG.append("<br>");
		replyMSG.append("<center><button value=\"Teleport Character\" action=\"bypass -h admin_show_teleport\" width=140 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
		replyMSG.append("<center><button value=\"Teleport to Character\" action=\"bypass -h admin_teleport_to_character\" width=140 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
		replyMSG.append("<center><button value=\"Add Xp-Sp to Character\" action=\"bypass -h admin_add_exp_sp_to_character\" width=140 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
		replyMSG.append("<center><button value=\"Edit Character\" action=\"bypass -h admin_edit_character\" width=140 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
		replyMSG.append("<center><button value=\"Manage skills\" action=\"bypass -h admin_show_skills\" width=140 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
		replyMSG.append("<center><button value=\"Switch to inventory\" action=\"bypass -h admin_set_context\" width=140 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
		replyMSG.append("<center><button value=\"Reset inventory\" action=\"bypass -h admin_reset_context\" width=140 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
		replyMSG.append("<br><center><button value=\"Back\" action=\"bypass -h admin_show\" width=40 height=15></center>");
		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
		_characterToManipulate = player.getName();
	}
	
	private void addExpSp(ClientThread client)
	{
		final PlayerInstance activeChar = client.getActiveChar();
		final PlayerInstance player = World.getInstance().getPlayer(_characterToManipulate);
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		final StringBuilder replyMSG = new StringBuilder("<html><title>Add Exp-Sp to " + player.getName() + "</title>");
		replyMSG.append("<body>");
		replyMSG.append("<br>");
		replyMSG.append("<center>Lv: " + player.getLevel() + " " + CharTemplateTable.getInstance().getTemplate(player.getClassId()).getClassName() + "<p>");
		replyMSG.append("Exp: " + player.getExp() + "<p>");
		replyMSG.append("Sp: " + player.getSp() + "<p>");
		replyMSG.append("</center><br>");
		replyMSG.append("<center>Caution ! Dont forget that modifying players stats can ruin the game...</center><br>");
		replyMSG.append("Exp: ");
		replyMSG.append("<edit var=\"exp_to_add\" width=110>");
		replyMSG.append("Sp: ");
		replyMSG.append("<edit var=\"sp_to_add\" width=110>");
		replyMSG.append("<button value=\"Add Exp-Sp\" action=\"bypass -h admin_add_exp_sp $exp_to_add $sp_to_add\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<br><center><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15></center>");
		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void adminAddExpSp(ClientThread client, String expSp)
	{
		final PlayerInstance activeChar = client.getActiveChar();
		final PlayerInstance player = World.getInstance().getPlayer(_characterToManipulate);
		final StringTokenizer st = new StringTokenizer(expSp);
		if (st.countTokens() != 2)
		{
			addExpSp(client);
		}
		else
		{
			final String exp = st.nextToken();
			final String sp = st.nextToken();
			final int expval = Integer.parseInt(exp);
			final int spval = Integer.parseInt(sp);
			player.sendMessage("Admin is adding you " + expval + " xp and " + spval + " sp.");
			player.addExpAndSp(expval, spval);
			activeChar.sendMessage("Added " + expval + " xp and " + spval + " sp to " + player.getName() + ".");
			showCharacterList(client, _characterToManipulate);
		}
	}
	
	private void adminModifyCharacter(ClientThread client, String modifications)
	{
		final PlayerInstance activeChar = client.getActiveChar();
		final PlayerInstance player = World.getInstance().getPlayer(_characterToManipulate);
		final StringTokenizer st = new StringTokenizer(modifications);
		if (st.countTokens() != 9)
		{
			editCharacter(client);
		}
		else
		{
			final String hp = st.nextToken();
			final String hpmax = st.nextToken();
			final String mp = st.nextToken();
			final String mpmax = st.nextToken();
			final String load = st.nextToken();
			final String karma = st.nextToken();
			final String pvpflag = st.nextToken();
			final String pvpkills = st.nextToken();
			final String classid = st.nextToken();
			final int hpval = Integer.parseInt(hp);
			final int hpmaxval = Integer.parseInt(hpmax);
			final int mpval = Integer.parseInt(mp);
			final int mpmaxval = Integer.parseInt(mpmax);
			final int loadval = Integer.parseInt(load);
			final int karmaval = Integer.parseInt(karma);
			final int pvpflagval = Integer.parseInt(pvpflag);
			final int pvpkillsval = Integer.parseInt(pvpkills);
			final int classidval = Integer.parseInt(classid);
			player.sendMessage("Admin has changed your stats. Hp: " + hpval + " HpMax: " + hpmaxval + " Mp: " + mpval + " MpMax: " + mpmaxval + " MaxLoad: " + loadval + " Karma: " + karmaval + " Pvp: " + pvpflagval + " / " + pvpkillsval + " ClassId: " + classidval);
			player.setCurrentHp(hpval);
			player.setCurrentMp(mpval);
			player.setMaxHp(hpmaxval);
			player.setMaxMp(mpmaxval);
			player.setMaxLoad(loadval);
			player.setKarma(karmaval);
			player.setPvpKills(pvpkillsval);
			player.setClassId(classidval);
			final StatusUpdate su = new StatusUpdate(player.getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, hpval);
			su.addAttribute(StatusUpdate.MAX_HP, hpmaxval);
			su.addAttribute(StatusUpdate.CUR_MP, mpval);
			su.addAttribute(StatusUpdate.MAX_MP, mpmaxval);
			su.addAttribute(StatusUpdate.MAX_LOAD, loadval);
			su.addAttribute(StatusUpdate.KARMA, karmaval);
			su.addAttribute(StatusUpdate.PVP_FLAG, pvpflagval);
			player.sendPacket(su);
			activeChar.sendMessage("Changed stats of " + player.getName() + ". " + " Hp: " + hpval + " HpMax: " + hpmaxval + " Mp: " + mpval + " MpMax: " + mpmaxval + " MaxLoad: " + loadval + " Karma: " + karmaval + " Pvp: " + pvpflagval + " / " + pvpkillsval + " ClassId: " + classidval);
			showCharacterList(client, _characterToManipulate);
		}
	}
	
	private void editCharacter(ClientThread client)
	{
		final PlayerInstance activeChar = client.getActiveChar();
		final PlayerInstance player = World.getInstance().getPlayer(_characterToManipulate);
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		final StringBuilder replyMSG = new StringBuilder("<html><title>Editing character " + player.getName() + "</title>");
		replyMSG.append("<body>");
		replyMSG.append("<br><center>Caution ! Dont forget that modifying players stats can ruin the game...</center><br>");
		replyMSG.append("Note: you must type all values to confirm modifications.</center><br>");
		replyMSG.append("<br>");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td>HP</td><td>" + player.getCurrentHp() + " / " + player.getMaxHp() + "</td><td>MP</td><td>" + player.getCurrentMp() + " / " + player.getMaxMp() + "</td></tr>");
		replyMSG.append("<tr><td>Load</td><td>" + player.getCurrentLoad() + " / " + player.getMaxLoad() + "</td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("<br>");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td>Social</td></tr>");
		replyMSG.append("</table>");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td>Karma</td><td>" + player.getKarma() + "</td><td>PvP</td><td>" + player.getPvpFlag() + " / " + player.getPvpKills() + "</td></tr>");
		replyMSG.append("<tr><td>ClassId</td><td>" + player.getClassId() + "</td></tr>");
		replyMSG.append("</table><p>");
		replyMSG.append("<br>");
		replyMSG.append("<center><table>");
		replyMSG.append("<tr><td>Hp:</td>");
		replyMSG.append("<td><edit var=\"hp\" width=110></td></tr>");
		replyMSG.append("<tr><td>HpMax:</td>");
		replyMSG.append("<td><edit var=\"hpmax\" width=110></td></tr>");
		replyMSG.append("<tr><td>Mp:</td>");
		replyMSG.append("<td><edit var=\"mp\" width=110></td></tr>");
		replyMSG.append("<tr><td>MpMax:</td>");
		replyMSG.append("<td><edit var=\"mpmax\" width=110></td></tr>");
		replyMSG.append("<tr><td>MaxLoad:</td>");
		replyMSG.append("<td><edit var=\"load\" width=110></td></tr>");
		replyMSG.append("<tr><td>Karma:</td>");
		replyMSG.append("<td><edit var=\"karma\" width=110></td></tr>");
		replyMSG.append("<tr><td>PvpKills:</td>");
		replyMSG.append("<td><edit var=\"pvpkills\" width=110></td></tr>");
		replyMSG.append("<tr><td>ClassId:</td>");
		replyMSG.append("<td><edit var=\"classid\" width=110></td></tr>");
		replyMSG.append("</table></center>");
		replyMSG.append("<center><button value=\"Save modifications\" action=\"bypass -h admin_save_modifications $hp $hpmax $mp $mpmax $load $karma $pvpflag $pvpkills $classid\" width=140 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
		replyMSG.append("<br><center><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15></center>");
		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void disconnectCharacter(ClientThread client)
	{
		final PlayerInstance activeChar = client.getActiveChar();
		final PlayerInstance player = World.getInstance().getPlayer(_characterToManipulate);
		if (player.getName().equals(activeChar.getName()))
		{
			activeChar.sendMessage("You cannot logout your character.");
		}
		else
		{
			activeChar.sendMessage("Character " + player.getName() + " disconnected from server.");
			final LeaveWorld ql = new LeaveWorld();
			player.sendPacket(ql);
			try
			{
				player.getNetConnection().close();
			}
			catch (IOException e)
			{
				// empty catch block
			}
		}
		showMainPage(client);
	}
	
	private void findCharacter(ClientThread client, String characterToFind)
	{
		final PlayerInstance activeChar = client.getActiveChar();
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		int charactersFound = 0;
		final StringBuilder replyMSG = new StringBuilder("<html><title>Character Search</title>");
		replyMSG.append("<body>");
		replyMSG.append("<br>");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td>Name</td><td>Class</td><td>Level</td></tr>");
		for (PlayerInstance player : World.getInstance().getAllPlayers())
		{
			if (!player.getName().startsWith(characterToFind))
			{
				continue;
			}
			++charactersFound;
			replyMSG.append("<tr><td><a action=\"bypass -h admin_character_list " + player.getName() + "\">" + player.getName() + "</a>" + "</td><td>" + CharTemplateTable.getInstance().getTemplate(player.getClassId()).getClassName() + "</td><td>" + player.getLevel() + "</td></tr>");
		}
		replyMSG.append("</table>");
		if (charactersFound == 0)
		{
			replyMSG.append("<br>Your search did not find any characters. Please try again:");
			replyMSG.append("<edit var=\"character_name\" width=110>");
			replyMSG.append("<button value=\"Find\" action=\"bypass -h admin_find_character $character_name\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
			replyMSG.append("<br>");
		}
		else
		{
			replyMSG.append("<br>Found " + charactersFound + " character");
			if (charactersFound == 1)
			{
				replyMSG.append(".");
			}
			else if (charactersFound > 1)
			{
				replyMSG.append("s.");
			}
		}
		replyMSG.append("<br>");
		replyMSG.append("<center><button value=\"Back\" action=\"bypass -h admin_show\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void showTeleportCharWindow(ClientThread client)
	{
		final PlayerInstance activeChar = client.getActiveChar();
		final PlayerInstance player = World.getInstance().getPlayer(_characterToManipulate);
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		final StringBuilder replyMSG = new StringBuilder("<html><title>Teleport Character</title>");
		replyMSG.append("<body>");
		replyMSG.append("The character you will teleport is " + player.getName() + ".");
		replyMSG.append("<br>");
		replyMSG.append("Co-ordinate x");
		replyMSG.append("<edit var=\"char_cord_x\" width=110>");
		replyMSG.append("Co-ordinate y");
		replyMSG.append("<edit var=\"char_cord_y\" width=110>");
		replyMSG.append("Co-ordinate z");
		replyMSG.append("<edit var=\"char_cord_z\" width=110>");
		replyMSG.append("<button value=\"Teleport\" action=\"bypass -h admin_teleport_character $char_cord_x $char_cord_y $char_cord_z\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<button value=\"Teleport near you\" action=\"bypass -h admin_teleport_character " + activeChar.getX() + " " + activeChar.getY() + " " + activeChar.getZ() + "\" width=115 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<center><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void teleportCharacter(ClientThread client, String coords)
	{
		final PlayerInstance activeChar = client.getActiveChar();
		final PlayerInstance player = World.getInstance().getPlayer(_characterToManipulate);
		if (player.getName().equals(activeChar.getName()))
		{
			player.sendMessage("You cannot teleport your character.");
		}
		else
		{
			final StringTokenizer st = new StringTokenizer(coords);
			final String x1 = st.nextToken();
			final int x = Integer.parseInt(x1);
			final String y1 = st.nextToken();
			final int y = Integer.parseInt(y1);
			final String z1 = st.nextToken();
			final int z = Integer.parseInt(z1);
			player.sendMessage("Admin is teleporting you.");
			player.teleToLocation(x, y, z);
			activeChar.sendMessage("Character " + player.getName() + " teleported to " + x + " " + y + " " + z);
		}
		showMainPage(client);
	}
	
	private void teleportToCharacter(ClientThread client)
	{
		final PlayerInstance activeChar = client.getActiveChar();
		final PlayerInstance player = World.getInstance().getPlayer(_characterToManipulate);
		if (player.getName().equals(activeChar.getName()))
		{
			activeChar.sendMessage("You cannot self teleport.");
		}
		else
		{
			player.teleToLocation(player.getX(), player.getY(), player.getZ());
			activeChar.sendMessage("You have teleported to character " + player.getName() + ".");
		}
		showMainPage(client);
	}
	
	private void spawnMenu(ClientThread client, String monsterId)
	{
		final PlayerInstance activeChar = client.getActiveChar();
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		final StringBuilder replyMSG = new StringBuilder("<html><title>Custom Spawn Menu</title>");
		replyMSG.append("<body><br>Enter target player's name below.<br>You may leave the field blank for self-spawn.<br><br>");
		replyMSG.append("<center><edit var=\"targetname\" width=160></center><br><br>");
		replyMSG.append("<center><button value=\"Spawn on self\" action=\"bypass -h admin_spawn_confirm " + monsterId + " " + activeChar.getName() + "\" width=160 height=15></center><br>");
		replyMSG.append("<center><button value=\"Spawn on character\" action=\"bypass -h admin_spawn_confirm " + monsterId + " $targetname\" width=160 height=15></center></body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void spawnMonster(ClientThread client, String monsterId, String charName)
	{
		final PlayerInstance activeChar = client.getActiveChar();
		final PlayerInstance targetPlayer = World.getInstance().getPlayer(charName);
		final int monsterTemplate = Integer.parseInt(monsterId);
		final Npc template1 = NpcTable.getInstance().getTemplate(monsterTemplate);
		if (template1 == null)
		{
			return;
		}
		try
		{
			final Spawn spawn = new Spawn(template1);
			spawn.setLocx(targetPlayer.getX());
			spawn.setLocy(targetPlayer.getY());
			spawn.setLocz(targetPlayer.getZ());
			spawn.setRandomx(0);
			spawn.setRandomy(0);
			spawn.setAmount(1);
			spawn.setHeading(targetPlayer.getHeading());
			spawn.setRespawnDelay(15);
			SpawnTable.getInstance().addNewSpawn(spawn);
			spawn.init();
			activeChar.sendMessage("Created " + template1.getName() + " on " + targetPlayer.getName() + ".");
		}
		catch (Exception e)
		{
			activeChar.sendMessage("Target player is offline.");
		}
		showHelpPage(client, "spawns.htm");
	}
	
	private int disconnectAllCharacters()
	{
		final LeaveWorld leaveWorld = new LeaveWorld();
		for (PlayerInstance player : World.getInstance().getAllPlayers())
		{
			player.sendPacket(leaveWorld);
			try
			{
				player.getNetConnection().close();
			}
			catch (IOException e)
			{
				// empty catch block
			}
		}
		return 1;
	}
	
	public void serverShutdown(ClientThread client, int seconds)
	{
		secondsShut = seconds;
		clientShut = client;
		final AdminCommands shutDownThread = new AdminCommands();
		shutDownThread.start();
	}
	
	@Override
	public void run()
	{
		if ((secondsShut == 0) && (clientShut != null))
		{
			final PlayerInstance activeChar = clientShut.getActiveChar();
			final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			final StringBuilder replyMSG = new StringBuilder("<html><title>Shutdown Server</title>");
			replyMSG.append("<body><br>");
			replyMSG.append("Enter in seconds the time till the server<p> shutdowns bellow:");
			replyMSG.append("<edit var=\"shutdown_time\" width=110>");
			replyMSG.append("<button value=\"Shutdown\" action=\"bypass -h admin_server_shutdown $shutdown_time\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
			replyMSG.append("<br>");
			replyMSG.append("<center><button value=\"Back\" action=\"bypass -h admin_show\" width=40 height=15></center>");
			replyMSG.append("</body></html>");
			adminReply.setHtml(replyMSG.toString());
			activeChar.sendPacket(adminReply);
		}
		else
		{
			try
			{
				broadcastToAll("The server will shutdown in " + ((secondsShut - (secondsShut % 60)) / 60) + " minutes and " + (secondsShut % 60) + " seconds.");
				for (int i = secondsShut; i > 0; --i)
				{
					if (i != secondsShut)
					{
						switch (i)
						{
							case 240:
							{
								broadcastToAll("The server will shutdown in 4 minutes.");
								break;
							}
							case 180:
							{
								broadcastToAll("The server will shutdown in 3 minutes.");
								break;
							}
							case 120:
							{
								broadcastToAll("The server will shutdown in 2 minutes.");
								break;
							}
							case 60:
							{
								broadcastToAll("The server will shutdown in 1 minutes.");
								break;
							}
							case 30:
							{
								broadcastToAll("The server will shutdown in 30 seconds.");
								break;
							}
							case 5:
							{
								broadcastToAll("The server will shutdown in 5 seconds, please logout NOW!");
							}
						}
					}
					final int delay = 1000;
					Thread.sleep(delay);
				}
				if (disconnectAllCharacters() == 1)
				{
					System.exit(0);
				}
				else
				{
					_log.warning("Error, aborting shutdown.");
				}
			}
			catch (InterruptedException e)
			{
				// empty catch block
			}
		}
	}
	
	private void showHelpPage(ClientThread client, String filename)
	{
		FileInputStream fis = null;
		try
		{
			final File file = new File("data/html/admin/" + filename);
			fis = new FileInputStream(file);
			final byte[] raw = new byte[fis.available()];
			fis.read(raw);
			final String content = new String(raw, StandardCharsets.UTF_8);
			final PlayerInstance activeChar = client.getActiveChar();
			final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			adminReply.setHtml(content);
			activeChar.sendPacket(adminReply);
			
			fis.close();
		}
		catch (Exception e)
		{
			try
			{
				if (fis != null)
				{
					fis.close();
				}
				return;
			}
			catch (Exception e1)
			{
				return;
			}
			catch (Throwable throwable)
			{
				try
				{
					if (fis != null)
					{
						fis.close();
					}
					throw throwable;
				}
				catch (Exception e1)
				{
					// empty catch block
				}
				throw throwable;
			}
		}
	}
	
	public void broadcastToAll(String message)
	{
		final CreatureSay cs = new CreatureSay(0, 9, "[Announcement]", message);
		for (PlayerInstance player : World.getInstance().getAllPlayers())
		{
			player.sendPacket(cs);
		}
	}
	
	private void removeSkillsPage(ClientThread client)
	{
		final PlayerInstance activeChar = client.getActiveChar();
		final PlayerInstance player = World.getInstance().getPlayer(_characterToManipulate);
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		final StringBuilder replyMSG = new StringBuilder("<html><title>Remove skills of " + player.getName() + "</title>");
		replyMSG.append("<body>");
		replyMSG.append("<br>");
		replyMSG.append("<center>Lv: " + player.getLevel() + " " + CharTemplateTable.getInstance().getTemplate(player.getClassId()).getClassName() + "<p>");
		replyMSG.append("</center><br>");
		replyMSG.append("<center>Caution ! Dont forget that modifying players skills can ruin the game...</center><br>");
		replyMSG.append("<center>Click on the skill you wish to remove:</center>");
		replyMSG.append("<center><table>");
		replyMSG.append("<tr><td><center>Name:</center></td><td></td><td>Lvl:</td><td></td><td>Id:</td></tr>");
		for (Skill skill : player.getAllSkills())
		{
			replyMSG.append("<tr><td><a action=\"bypass -h admin_remove_skill " + skill.getId() + "\">" + skill.getName() + "</a></td><td></td><td>" + skill.getLevel() + "</td><td></td><td>" + skill.getId() + "</td></tr>");
		}
		replyMSG.append("</table></center>");
		replyMSG.append("<br><center><table>");
		replyMSG.append("Remove custom skill:");
		replyMSG.append("<tr><td>Id: </td>");
		replyMSG.append("<td><edit var=\"id_to_remove\" width=110></td></tr>");
		replyMSG.append("</table></center>");
		replyMSG.append("<center><button value=\"Remove skill\" action=\"bypass -h admin_remove_skill $id_to_remove\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
		replyMSG.append("<br><center><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15></center>");
		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void showSkillsPage(ClientThread client)
	{
		final PlayerInstance activeChar = client.getActiveChar();
		final PlayerInstance player = World.getInstance().getPlayer(_characterToManipulate);
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		final StringBuilder replyMSG = new StringBuilder("<html><title>Modify skills of " + player.getName() + "</title>");
		replyMSG.append("<body>");
		replyMSG.append("<br>");
		replyMSG.append("<center>Lv: " + player.getLevel() + " " + CharTemplateTable.getInstance().getTemplate(player.getClassId()).getClassName() + "<p>");
		replyMSG.append("</center><br>");
		replyMSG.append("<center>Caution ! Dont forget that modifying players skills can ruin the game...</center><br>");
		replyMSG.append("<center><table>");
		replyMSG.append("<tr><td><button value=\"Add skills\" action=\"bypass -h admin_skill_list\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td><button value=\"Remove skills\" action=\"bypass -h admin_remove_skills\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("<tr><td><button value=\"Get skills\" action=\"bypass -h admin_get_skills\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td><button value=\"Reset skills\" action=\"bypass -h admin_reset_skills\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
		replyMSG.append("</table></center>");
		replyMSG.append("<br><center><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15></center>");
		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void adminGetSkills(ClientThread client)
	{
		final PlayerInstance activeChar = client.getActiveChar();
		final PlayerInstance player = World.getInstance().getPlayer(_characterToManipulate);
		if (player.getName().equals(activeChar.getName()))
		{
			player.sendMessage("There is no point in doing it on your character...");
		}
		else
		{
			final Collection<Skill> skills = player.getAllSkills();
			adminSkills = activeChar.getAllSkills();
			for (Skill skill : adminSkills)
			{
				activeChar.removeSkill(skill);
			}
			for (Skill skill : skills)
			{
				activeChar.addSkill(skill);
			}
			activeChar.sendMessage("You now have all the skills of  " + player.getName() + ".");
		}
		showSkillsPage(client);
	}
	
	private void adminResetSkills(ClientThread client)
	{
		final PlayerInstance activeChar = client.getActiveChar();
		final PlayerInstance player = World.getInstance().getPlayer(_characterToManipulate);
		if (adminSkills == null)
		{
			activeChar.sendMessage("You must first get the skills of someone to do this.");
		}
		else
		{
			final Collection<Skill> skills = player.getAllSkills();
			for (Skill skill : skills)
			{
				player.removeSkill(skill);
			}
			for (Skill skill : activeChar.getAllSkills())
			{
				player.addSkill(skill);
			}
			for (Skill skill : skills)
			{
				activeChar.removeSkill(skill);
			}
			for (Skill skill : adminSkills)
			{
				activeChar.addSkill(skill);
			}
			player.sendMessage("[GM]" + activeChar.getName() + " has updated your skills.");
			activeChar.sendMessage("You now have all your skills back.");
			adminSkills = null;
		}
		showSkillsPage(client);
	}
	
	private void adminAddSkill(ClientThread client, String val)
	{
		final PlayerInstance activeChar = client.getActiveChar();
		final PlayerInstance player = World.getInstance().getPlayer(_characterToManipulate);
		final StringTokenizer st = new StringTokenizer(val);
		if (st.countTokens() != 2)
		{
			showSkillsPage(client);
		}
		else
		{
			final String id = st.nextToken();
			final String level = st.nextToken();
			final int idval = Integer.parseInt(id);
			final int levelval = Integer.parseInt(level);
			final Skill skill = SkillTable.getInstance().getInfo(idval, levelval);
			if (skill != null)
			{
				player.sendMessage("Admin gave you the skill " + skill.getName() + ".");
				player.addSkill(skill);
				activeChar.sendMessage("You gave the skill " + skill.getName() + " to " + player.getName() + ".");
			}
			else
			{
				activeChar.sendMessage("Error: there is no such skill.");
			}
			showSkillsPage(client);
		}
	}
	
	private void adminRemoveSkill(ClientThread client, int idval)
	{
		final PlayerInstance activeChar = client.getActiveChar();
		final PlayerInstance player = World.getInstance().getPlayer(_characterToManipulate);
		final Skill skill = SkillTable.getInstance().getInfo(idval, player.getSkillLevel(idval));
		if (skill != null)
		{
			player.sendMessage("Admin removed the skill " + skill.getName() + ".");
			player.removeSkill(skill);
			activeChar.sendMessage("You removed the skill " + skill.getName() + " from " + player.getName() + ".");
		}
		else
		{
			activeChar.sendMessage("Error: there is no such skill.");
		}
		removeSkillsPage(client);
	}
	
	public void showSkill(ClientThread client, String val)
	{
		final PlayerInstance activeChar = client.getActiveChar();
		final int skillid = Integer.parseInt(val);
		final Skill skill = SkillTable.getInstance().getInfo(skillid, 1);
		if ((skill != null) && (skill.getTargetType() == 0))
		{
			activeChar.setTarget(activeChar);
			final MagicSkillUser msk = new MagicSkillUser(activeChar, skillid, 1, skill.getHitTime(), skill.getReuseDelay());
			activeChar.sendPacket(msk);
			activeChar.broadcastPacket(msk);
		}
	}
}
