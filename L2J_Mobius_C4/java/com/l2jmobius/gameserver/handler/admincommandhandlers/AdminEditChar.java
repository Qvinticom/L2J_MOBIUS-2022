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
package com.l2jmobius.gameserver.handler.admincommandhandlers;

import java.util.Collection;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.datatables.CharNameTable;
import com.l2jmobius.gameserver.datatables.ClanTable;
import com.l2jmobius.gameserver.handler.IAdminCommandHandler;
import com.l2jmobius.gameserver.model.GMAudit;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PetInstance;
import com.l2jmobius.gameserver.model.base.ClassId;
import com.l2jmobius.gameserver.network.L2GameClient;
import com.l2jmobius.gameserver.network.serverpackets.CharInfo;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jmobius.gameserver.network.serverpackets.PledgeShowMemberListAll;
import com.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.network.serverpackets.UserInfo;
import com.l2jmobius.gameserver.util.Util;

import javolution.text.TextBuilder;

/**
 * This class handles following admin commands: - edit_character - current_player - character_list - character_info - show_characters - find_character - find_ip - find_account - rec - nokarma - setkarma - settitle - changename - setsex - setclass - fullfood - save_modifications
 * @version $Revision: 1.3.2.1.2.10 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminEditChar implements IAdminCommandHandler
{
	private static Logger _log = Logger.getLogger(AdminEditChar.class.getName());
	
	private static String[] _adminCommands =
	{
		"admin_edit_character",
		"admin_current_player",
		"admin_nokarma", // this is to remove karma from selected char...
		"admin_setkarma", // sets karma of target char to any amount. //setkarma <karma>
		"admin_character_list", // same as character_info, kept for compatibility purposes
		"admin_character_info", // given a player name, displays an information window
		"admin_show_characters", // list of characters
		"admin_find_character", // find a player by his name or a part of it (case-insensitive)
		"admin_find_ip", // find all the player connections from a given IPv4Filter number
		"admin_find_account", // list all the characters from an account (useful for GMs w/o DB access)
		"admin_save_modifications", // consider it deprecated...
		"admin_rec", // gives recommendation points
		"admin_settitle", // changes char title
		"admin_changename", // changes char name
		"admin_setsex", // changes characters' sex
		"admin_setcolor", // change charnames' color display
		"admin_setclass", // changes chars' classId
		"admin_fullfood" // fulfills a pet's food bar
	};
	
	private static final int REQUIRED_LEVEL = Config.GM_CHAR_EDIT;
	private static final int REQUIRED_LEVEL2 = Config.GM_CHAR_EDIT_OTHER;
	private static final int REQUIRED_LEVEL_VIEW = Config.GM_CHAR_VIEW;
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!Config.ALT_PRIVILEGES_ADMIN)
		{
			if (!((checkLevel(activeChar.getAccessLevel()) || checkLevel2(activeChar.getAccessLevel())) && activeChar.isGM()))
			{
				return false;
			}
		}
		
		GMAudit.auditGMAction(activeChar.getName(), command, (activeChar.getTarget() != null) ? activeChar.getTarget().getName() : "no-target", "");
		
		if (command.equals("admin_current_player"))
		{
			showCharacterInfo(activeChar, null);
		}
		else if (command.startsWith("admin_character_list") || command.startsWith("admin_character_info"))
		{
			try
			{
				final String val = command.substring(21);
				final L2PcInstance target = L2World.getInstance().getPlayer(val);
				if (target != null)
				{
					showCharacterInfo(activeChar, target);
				}
				else
				{
					activeChar.sendMessage("Character does not exist.");
				}
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //character_info <player_name>");
			}
		}
		else if (command.startsWith("admin_show_characters"))
		{
			try
			{
				final String val = command.substring(22);
				final int page = Integer.parseInt(val);
				listCharacters(activeChar, page);
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				// Case of empty page number
				activeChar.sendMessage("Usage: //show_characters <page_number>");
			}
		}
		else if (command.startsWith("admin_find_character"))
		{
			try
			{
				final String val = command.substring(21);
				findCharacter(activeChar, val);
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				// Case of empty character name
				activeChar.sendMessage("Usage: //find_character <character_name>");
				listCharacters(activeChar, 0);
			}
		}
		else if (command.startsWith("admin_find_ip"))
		{
			try
			{
				final String val = command.substring(14);
				findCharactersPerIp(activeChar, val);
			}
			catch (final Exception e)
			{
				// Case of empty or malformed IP number
				activeChar.sendMessage("Usage: //find_ip <www.xxx.yyy.zzz>");
				listCharacters(activeChar, 0);
			}
		}
		else if (command.startsWith("admin_find_account"))
		{
			try
			{
				final String val = command.substring(19);
				findCharactersPerAccount(activeChar, val);
			}
			catch (final Exception e)
			{
				// Case of empty or malformed player name
				activeChar.sendMessage("Usage: //find_account <player_name>");
				listCharacters(activeChar, 0);
			}
		}
		else if (command.equals("admin_edit_character"))
		{
			editCharacter(activeChar);
		}
		else if (command.equals("admin_nokarma"))
		{
			setTargetKarma(activeChar, 0);
		}
		else if (command.startsWith("admin_setkarma"))
		{
			try
			{
				final String val = command.substring(15);
				final int karma = Integer.parseInt(val);
				if ((activeChar == activeChar.getTarget()) || (activeChar.getAccessLevel() >= REQUIRED_LEVEL2))
				{
					GMAudit.auditGMAction(activeChar.getName(), command, activeChar.getName(), "");
				}
				setTargetKarma(activeChar, karma);
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				if (Config.DEVELOPER)
				{
					System.out.println("Set karma error: " + e);
				}
				activeChar.sendMessage("Usage: //setkarma <new_karma_value>");
			}
		}
		else if (command.startsWith("admin_save_modifications"))
		{
			try
			{
				final String val = command.substring(24);
				if ((activeChar == activeChar.getTarget()) || (activeChar.getAccessLevel() >= REQUIRED_LEVEL2))
				{
					GMAudit.auditGMAction(activeChar.getName(), command, activeChar.getName(), "");
				}
				adminModifyCharacter(activeChar, val);
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				// Case of empty character name
				activeChar.sendMessage("Error while modifying character.");
				listCharacters(activeChar, 0);
			}
		}
		else if (command.startsWith("admin_rec"))
		{
			try
			{
				final String val = command.substring(10);
				final int recVal = Integer.parseInt(val);
				final L2Object target = activeChar.getTarget();
				L2PcInstance player = null;
				if ((activeChar != target) && (activeChar.getAccessLevel() < REQUIRED_LEVEL2))
				{
					return false;
				}
				if (target instanceof L2PcInstance)
				{
					player = (L2PcInstance) target;
				}
				else
				{
					return false;
				}
				
				player.setRecomHave(recVal);
				player.sendMessage("You have been recommended by a GM");
				player.broadcastUserInfo();
			}
			catch (final Exception e)
			{
				activeChar.sendMessage("Usage: //rec number");
			}
		}
		else if (command.startsWith("admin_setclass"))
		{
			try
			{
				if (command.equals("admin_setclass"))
				{
					AdminHelpPage.showHelpPage(activeChar, "charclasses.htm");
					return true;
				}
				
				final String val = command.substring(15);
				final int classidval = Integer.parseInt(val);
				
				final L2Object target = activeChar.getTarget();
				L2PcInstance player = null;
				if ((activeChar != target) && (activeChar.getAccessLevel() < REQUIRED_LEVEL2))
				{
					return false;
				}
				if (target instanceof L2PcInstance)
				{
					player = (L2PcInstance) target;
				}
				else
				{
					return false;
				}
				
				boolean valid = false;
				for (final ClassId classid : ClassId.values())
				{
					if (classidval == classid.getId())
					{
						valid = true;
					}
				}
				
				if (valid && (player.getClassId().getId() != classidval))
				{
					player.setClassId(classidval);
					if (!player.isSubClassActive())
					{
						player.setBaseClass(classidval);
					}
					final String newclass = player.getTemplate().className;
					player.store();
					player.sendMessage("A GM changed your class to " + newclass);
					player.broadcastUserInfo();
					activeChar.sendMessage(player.getName() + " is a " + newclass);
				}
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //setclass <valid_new_classid>");
			}
		}
		else if (command.startsWith("admin_settitle"))
		{
			try
			{
				final String val = command.substring(15);
				final L2Object target = activeChar.getTarget();
				L2PcInstance player = null;
				if ((activeChar != target) && (activeChar.getAccessLevel() < REQUIRED_LEVEL2))
				{
					return false;
				}
				if (target instanceof L2PcInstance)
				{
					player = (L2PcInstance) target;
				}
				else
				{
					return false;
				}
				
				player.setTitle(val);
				player.sendMessage("Your title has been changed by a GM.");
				player.broadcastTitleInfo();
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				// Case of empty character title
				activeChar.sendMessage("You need to specify the new title.");
			}
		}
		else if (command.startsWith("admin_changename"))
		{
			try
			{
				final String val = command.substring(17);
				final L2Object target = activeChar.getTarget();
				L2PcInstance player = null;
				if ((activeChar != target) && (activeChar.getAccessLevel() < REQUIRED_LEVEL2))
				{
					return false;
				}
				if (target instanceof L2PcInstance)
				{
					player = (L2PcInstance) target;
				}
				else
				{
					return false;
				}
				
				if (player.getName().equals(val))
				{
					activeChar.sendMessage("This name is already used by this player.");
					return false;
				}
				
				if (CharNameTable.getInstance().doesCharNameExist(val))
				{
					activeChar.sendMessage("Warning, name " + val + " already exists.");
					return false;
				}
				
				L2World.getInstance().removeFromAllPlayers(player);
				player.setName(val);
				player.store();
				L2World.getInstance().addToAllPlayers(player);
				
				player.sendMessage("Your name has been changed by a GM.");
				player.broadcastUserInfo();
				
				if (player.getClan() != null)
				{
					player.getClan().broadcastToOnlineMembers(new PledgeShowMemberListAll(player.getClan(), player));
				}
				
				player.leaveParty();
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				// Case of empty character name
				activeChar.sendMessage("Usage: //setname new_name_for_target");
			}
		}
		else if (command.startsWith("admin_setsex"))
		{
			final L2Object target = activeChar.getTarget();
			L2PcInstance player = null;
			if ((activeChar != target) && (activeChar.getAccessLevel() < REQUIRED_LEVEL2))
			{
				return false;
			}
			if (target instanceof L2PcInstance)
			{
				player = (L2PcInstance) target;
			}
			else
			{
				return false;
			}
			
			player.getAppearance().setSex(player.getAppearance().getSex() ? false : true);
			player.sendMessage("Your gender has been changed by a GM.");
			player.broadcastUserInfo();
			player.decayMe();
			player.spawnMe(player.getX(), player.getY(), player.getZ());
		}
		else if (command.startsWith("admin_setcolor"))
		{
			try
			{
				final String val = command.substring(15);
				final L2Object target = activeChar.getTarget();
				L2PcInstance player = null;
				if ((activeChar != target) && (activeChar.getAccessLevel() < REQUIRED_LEVEL2))
				{
					return false;
				}
				if (target instanceof L2PcInstance)
				{
					player = (L2PcInstance) target;
				}
				else
				{
					return false;
				}
				
				player.getAppearance().setNameColor(Integer.decode("0x" + val));
				player.sendMessage("Your name color has been changed by a GM.");
				player.broadcastUserInfo();
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				// Case of empty color
				activeChar.sendMessage("You need to specify the new color.");
			}
		}
		else if (command.startsWith("admin_fullfood"))
		{
			final L2Object target = activeChar.getTarget();
			
			if (target instanceof L2PetInstance)
			{
				final L2PetInstance targetPet = (L2PetInstance) target;
				targetPet.setCurrentFed(targetPet.getMaxFed());
			}
			else
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.INCORRECT_TARGET));
			}
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return _adminCommands;
	}
	
	private boolean checkLevel(int level)
	{
		return (level >= REQUIRED_LEVEL);
	}
	
	private boolean checkLevel2(int level)
	{
		return (level >= REQUIRED_LEVEL_VIEW);
	}
	
	private void listCharacters(L2PcInstance activeChar, int page)
	{
		final Collection<L2PcInstance> allPlayers = L2World.getInstance().getAllPlayers();
		final L2PcInstance[] players = allPlayers.toArray(new L2PcInstance[allPlayers.size()]);
		
		final int MaxCharactersPerPage = 20;
		int MaxPages = players.length / MaxCharactersPerPage;
		
		if (players.length > (MaxCharactersPerPage * MaxPages))
		{
			MaxPages++;
		}
		
		// Check if number of users changed
		if (page > MaxPages)
		{
			page = MaxPages;
		}
		
		final int CharactersStart = MaxCharactersPerPage * page;
		int CharactersEnd = players.length;
		if ((CharactersEnd - CharactersStart) > MaxCharactersPerPage)
		{
			CharactersEnd = CharactersStart + MaxCharactersPerPage;
		}
		
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/charlist.htm");
		final TextBuilder replyMSG = new TextBuilder();
		
		for (int x = 0; x < MaxPages; x++)
		{
			final int pagenr = x + 1;
			replyMSG.append("<center><a action=\"bypass -h admin_show_characters " + x + "\">Page " + pagenr + "</a></center>");
		}
		adminReply.replace("%pages%", replyMSG.toString());
		replyMSG.clear();
		for (int i = CharactersStart; i < CharactersEnd; i++)
		{
			// Add player info into new Table row
			replyMSG.append("<tr><td width=80><a action=\"bypass -h admin_character_info " + players[i].getName() + "\">" + players[i].getName() + "</a></td><td width=110>" + players[i].getTemplate().className + "</td><td width=40>" + players[i].getLevel() + "</td></tr>");
		}
		adminReply.replace("%players%", replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void showCharacterInfo(L2PcInstance activeChar, L2PcInstance player)
	{
		if (player == null)
		{
			final L2Object target = activeChar.getTarget();
			if (target instanceof L2PcInstance)
			{
				player = (L2PcInstance) target;
			}
			else
			{
				return;
			}
		}
		else
		{
			activeChar.setTarget(player);
		}
		gatherCharacterInfo(activeChar, player, "charinfo.htm");
	}
	
	/**
	 * @param activeChar
	 * @param player
	 * @param filename
	 */
	private void gatherCharacterInfo(L2PcInstance activeChar, L2PcInstance player, String filename)
	{
		String ip = "N/A";
		String account = "N/A";
		
		try
		{
			final String clientInfo = player.getClient().toString();
			account = clientInfo.substring(clientInfo.indexOf("Account: ") + 9, clientInfo.indexOf(" - IP: "));
			ip = clientInfo.substring(clientInfo.indexOf(" - IP: ") + 7, clientInfo.lastIndexOf("]"));
		}
		catch (final Exception e)
		{
		}
		
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/" + filename);
		adminReply.replace("%name%", player.getName());
		adminReply.replace("%level%", String.valueOf(player.getLevel()));
		adminReply.replace("%clan%", String.valueOf(ClanTable.getInstance().getClan(player.getClanId())));
		adminReply.replace("%xp%", String.valueOf(player.getExp()));
		adminReply.replace("%sp%", String.valueOf(player.getSp()));
		adminReply.replace("%class%", player.getTemplate().className);
		adminReply.replace("%ordinal%", String.valueOf(player.getClassId().ordinal()));
		adminReply.replace("%classid%", String.valueOf(player.getClassId()));
		adminReply.replace("%x%", String.valueOf(player.getX()));
		adminReply.replace("%y%", String.valueOf(player.getY()));
		adminReply.replace("%z%", String.valueOf(player.getZ()));
		adminReply.replace("%currenthp%", String.valueOf((int) player.getCurrentHp()));
		adminReply.replace("%maxhp%", String.valueOf(player.getMaxHp()));
		adminReply.replace("%karma%", String.valueOf(player.getKarma()));
		adminReply.replace("%currentmp%", String.valueOf((int) player.getCurrentMp()));
		adminReply.replace("%maxmp%", String.valueOf(player.getMaxMp()));
		adminReply.replace("%pvpflag%", String.valueOf(player.getPvpFlag()));
		adminReply.replace("%currentcp%", String.valueOf((int) player.getCurrentCp()));
		adminReply.replace("%maxcp%", String.valueOf(player.getMaxCp()));
		adminReply.replace("%pvpkills%", String.valueOf(player.getPvpKills()));
		adminReply.replace("%pkkills%", String.valueOf(player.getPkKills()));
		adminReply.replace("%currentload%", String.valueOf(player.getCurrentLoad()));
		adminReply.replace("%maxload%", String.valueOf(player.getMaxLoad()));
		adminReply.replace("%percent%", String.valueOf(Util.roundTo(((float) player.getCurrentLoad() / (float) player.getMaxLoad()) * 100, 2)));
		adminReply.replace("%patk%", String.valueOf(player.getPAtk(null)));
		adminReply.replace("%matk%", String.valueOf(player.getMAtk(null, null)));
		adminReply.replace("%pdef%", String.valueOf(player.getPDef(null)));
		adminReply.replace("%mdef%", String.valueOf(player.getMDef(null, null)));
		adminReply.replace("%accuracy%", String.valueOf(player.getAccuracy()));
		adminReply.replace("%evasion%", String.valueOf(player.getEvasionRate(null)));
		adminReply.replace("%critical%", String.valueOf(player.getCriticalHit(null, null)));
		adminReply.replace("%runspeed%", String.valueOf(player.getRunSpeed()));
		adminReply.replace("%patkspd%", String.valueOf(player.getPAtkSpd()));
		adminReply.replace("%matkspd%", String.valueOf(player.getMAtkSpd()));
		adminReply.replace("%access%", String.valueOf(player.getAccessLevel()));
		adminReply.replace("%account%", account);
		adminReply.replace("%ip%", ip);
		activeChar.sendPacket(adminReply);
	}
	
	private void setTargetKarma(L2PcInstance activeChar, int newKarma)
	{
		// function to change karma of selected char
		final L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			return;
		}
		
		if (newKarma >= 0)
		{
			// for display
			final int oldKarma = player.getKarma();
			
			// update karma
			player.setKarma(newKarma);
			
			// Common character information
			player.sendMessage("Admin has changed your karma from " + oldKarma + " to " + newKarma + ".");
			
			// Admin information
			activeChar.sendMessage("Successfully Changed karma for " + player.getName() + " from (" + oldKarma + ") to (" + newKarma + ").");
			
			if (Config.DEBUG)
			{
				_log.fine("[SET KARMA] [GM]" + activeChar.getName() + " Changed karma for " + player.getName() + " from (" + oldKarma + ") to (" + newKarma + ").");
			}
		}
		else
		{
			// tell admin of mistake
			activeChar.sendMessage("You must enter a value for karma equal to 0 or greater.");
			
			if (Config.DEBUG)
			{
				_log.fine("[SET KARMA] ERROR: [GM]" + activeChar.getName() + " entered an incorrect value for new karma: " + newKarma + " for " + player.getName() + ".");
			}
		}
	}
	
	private void adminModifyCharacter(L2PcInstance activeChar, String modifications)
	{
		final L2Object target = activeChar.getTarget();
		if (!(target instanceof L2PcInstance))
		{
			return;
		}
		
		final L2PcInstance player = (L2PcInstance) target;
		final StringTokenizer st = new StringTokenizer(modifications);
		
		if (st.countTokens() != 6)
		{
			editCharacter(player);
			activeChar.sendMessage("Changes have not been saved. Please set values for all stats.");
			return;
		}
		
		final String hp = st.nextToken();
		final String mp = st.nextToken();
		final String cp = st.nextToken();
		final String pvpflag = st.nextToken();
		final String pvpkills = st.nextToken();
		final String pkkills = st.nextToken();
		
		final int hpval = Integer.parseInt(hp);
		final int mpval = Integer.parseInt(mp);
		final int cpval = Integer.parseInt(cp);
		final int pvpflagval = Integer.parseInt(pvpflag);
		final int pvpkillsval = Integer.parseInt(pvpkills);
		final int pkkillsval = Integer.parseInt(pkkills);
		
		// Common character information
		player.sendMessage("Admin has changed your stats." + "  HP: " + hpval + "  MP: " + mpval + "  CP: " + cpval + "  PvP Flag: " + pvpflagval + " PvP/PK " + pvpkillsval + "/" + pkkillsval);
		
		player.setCurrentHp(hpval);
		player.setCurrentMp(mpval);
		player.setCurrentCp(cpval);
		player.setPvpFlag(pvpflagval);
		player.setPvpKills(pvpkillsval);
		player.setPkKills(pkkillsval);
		
		// Save the changed parameters to the database.
		player.store();
		
		final StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_HP, hpval);
		su.addAttribute(StatusUpdate.MAX_HP, player.getMaxHp());
		su.addAttribute(StatusUpdate.CUR_MP, mpval);
		su.addAttribute(StatusUpdate.MAX_MP, player.getMaxMp());
		su.addAttribute(StatusUpdate.CUR_CP, cpval);
		su.addAttribute(StatusUpdate.MAX_CP, player.getMaxCp());
		player.sendPacket(su);
		
		// Admin information
		player.sendMessage("Changed stats of " + player.getName() + "." + "  HP: " + hpval + "  MP: " + mpval + "  CP: " + cpval + "  PvP: " + pvpflagval + " / " + pvpkillsval);
		
		if (Config.DEBUG)
		{
			_log.fine("[GM]" + activeChar.getName() + " changed stats of " + player.getName() + ". " + " HP: " + hpval + " MP: " + mpval + " CP: " + cpval + " PvP: " + pvpflagval + " / " + pvpkillsval);
		}
		
		showCharacterInfo(activeChar, null); // Back to start
		
		player.broadcastPacket(new CharInfo(player));
		player.sendPacket(new UserInfo(player));
		
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		
		player.decayMe();
		player.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
	}
	
	private void editCharacter(L2PcInstance activeChar)
	{
		final L2Object target = activeChar.getTarget();
		if (!(target instanceof L2PcInstance))
		{
			return;
		}
		
		final L2PcInstance player = (L2PcInstance) target;
		gatherCharacterInfo(activeChar, player, "charedit.htm");
	}
	
	/**
	 * @param activeChar
	 * @param CharacterToFind
	 */
	private void findCharacter(L2PcInstance activeChar, String CharacterToFind)
	{
		int CharactersFound = 0;
		String name;
		final Collection<L2PcInstance> allPlayers = L2World.getInstance().getAllPlayers();
		final L2PcInstance[] players = allPlayers.toArray(new L2PcInstance[allPlayers.size()]);
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/charfind.htm");
		final TextBuilder replyMSG = new TextBuilder();
		for (final L2PcInstance player : players)
		{ // Add player info into new Table row
			name = player.getName();
			if (name.toLowerCase().contains(CharacterToFind.toLowerCase()))
			{
				CharactersFound = CharactersFound + 1;
				replyMSG.append("<tr><td width=80><a action=\"bypass -h admin_character_list " + name + "\">" + name + "</a></td><td width=110>" + player.getTemplate().className + "</td><td width=40>" + player.getLevel() + "</td></tr>");
			}
			if (CharactersFound > 20)
			{
				break;
			}
		}
		adminReply.replace("%results%", replyMSG.toString());
		replyMSG.clear();
		if (CharactersFound == 0)
		{
			replyMSG.append("s. Please try again.");
		}
		else if (CharactersFound > 20)
		{
			adminReply.replace("%number%", " more than 20");
			replyMSG.append("s.<br>Please refine your search to see all of the results.");
		}
		else if (CharactersFound == 1)
		{
			replyMSG.append(".");
		}
		else
		{
			replyMSG.append("s.");
		}
		adminReply.replace("%number%", String.valueOf(CharactersFound));
		adminReply.replace("%end%", replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	/**
	 * @param activeChar
	 * @param IpAdress
	 * @throws IllegalArgumentException
	 */
	private void findCharactersPerIp(L2PcInstance activeChar, String IpAdress) throws IllegalArgumentException
	{
		boolean findDisconnected = false;
		
		if (IpAdress.equals("disconnected"))
		{
			findDisconnected = true;
		}
		else
		{
			if (!IpAdress.matches("^(?:(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2(?:[0-4][0-9]|5[0-5]))\\.){3}(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2(?:[0-4][0-9]|5[0-5]))$"))
			{
				throw new IllegalArgumentException("Malformed IPv4Filter number");
			}
		}
		
		final Collection<L2PcInstance> allPlayers = L2World.getInstance().getAllPlayers();
		final L2PcInstance[] players = allPlayers.toArray(new L2PcInstance[allPlayers.size()]);
		
		int CharactersFound = 0;
		L2GameClient client;
		String name, ip = "0.0.0.0";
		final TextBuilder replyMSG = new TextBuilder();
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setFile("data/html/admin/ipfind.htm");
		for (final L2PcInstance player : players)
		{
			client = player.getClient();
			if (client.isDetached())
			{
				if (!findDisconnected)
				{
					continue;
				}
			}
			else
			{
				if (findDisconnected)
				{
					continue;
				}
				
				ip = client.getConnection().getInetAddress().getHostAddress();
				if (!ip.equals(IpAdress))
				{
					continue;
				}
			}
			
			name = player.getName();
			CharactersFound = CharactersFound + 1;
			replyMSG.append("<tr><td width=80><a action=\"bypass -h admin_character_list " + name + "\">" + name + "</a></td><td width=110>" + player.getTemplate().className + "</td><td width=40>" + player.getLevel() + "</td></tr>");
			
			if (CharactersFound > 20)
			{
				break;
			}
		}
		adminReply.replace("%results%", replyMSG.toString());
		replyMSG.clear();
		if (CharactersFound == 0)
		{
			replyMSG.append("s. Maybe they got d/c? :)");
		}
		else if (CharactersFound > 20)
		{
			adminReply.replace("%number%", " more than " + String.valueOf(CharactersFound));
			replyMSG.append("s.<br>In order to avoid you a client crash I won't <br1>display results beyond the 20th character.");
		}
		else if (CharactersFound == 1)
		{
			replyMSG.append(".");
		}
		else
		{
			replyMSG.append("s.");
		}
		adminReply.replace("%ip%", ip);
		adminReply.replace("%number%", String.valueOf(CharactersFound));
		adminReply.replace("%end%", replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	/**
	 * @param activeChar
	 * @param characterName
	 * @throws IllegalArgumentException
	 */
	private void findCharactersPerAccount(L2PcInstance activeChar, String characterName) throws IllegalArgumentException
	{
		if (characterName.matches(Config.CNAME_TEMPLATE))
		{
			String account = null;
			Map<Integer, String> chars;
			final L2PcInstance player = L2World.getInstance().getPlayer(characterName);
			if (player == null)
			{
				throw new IllegalArgumentException("Player doesn't exist");
			}
			chars = player.getAccountChars();
			account = player.getAccountName();
			final TextBuilder replyMSG = new TextBuilder();
			final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			adminReply.setFile("data/html/admin/accountinfo.htm");
			for (final String charname : chars.values())
			{
				replyMSG.append(charname + "<br1>");
			}
			adminReply.replace("%characters%", replyMSG.toString());
			adminReply.replace("%account%", account);
			adminReply.replace("%player%", characterName);
			activeChar.sendPacket(adminReply);
		}
		else
		{
			throw new IllegalArgumentException("Malformed character name");
		}
	}
}