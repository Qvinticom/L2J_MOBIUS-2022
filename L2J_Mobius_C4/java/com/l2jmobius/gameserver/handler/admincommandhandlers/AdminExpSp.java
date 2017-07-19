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

import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.handler.IAdminCommandHandler;
import com.l2jmobius.gameserver.model.GMAudit;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

import javolution.text.TextBuilder;

/**
 * This class handles following admin commands: - add_exp_sp_to_character = show menu - add_exp_sp exp sp = adds exp & sp to target
 * @version $Revision: 1.2.4.6 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminExpSp implements IAdminCommandHandler
{
	private static Logger _log = Logger.getLogger(AdminExpSp.class.getName());
	
	private static String[] _adminCommands =
	{
		"admin_add_exp_sp_to_character",
		"admin_add_exp_sp",
		"admin_remove_exp_sp"
	};
	
	private static final int REQUIRED_LEVEL = Config.GM_CHAR_EDIT;
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!Config.ALT_PRIVILEGES_ADMIN)
		{
			if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM()))
			{
				return false;
			}
		}
		
		if (command.equals("admin_add_exp_sp_to_character"))
		{
			addExpSp(activeChar);
			GMAudit.auditGMAction(activeChar.getName(), command, (activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target"), "");
		}
		else if (command.startsWith("admin_add_exp_sp"))
		{
			try
			{
				final String val = command.substring(16);
				adminAddExpSp(activeChar, val);
				GMAudit.auditGMAction(activeChar.getName(), command, val, "");
			}
			catch (final StringIndexOutOfBoundsException e)
			{ // Case of empty character name
				final SystemMessage sm = new SystemMessage(614);
				sm.addString("Error while adding Exp-Sp.");
				activeChar.sendPacket(sm);
				// listCharacters(client, 0);
			}
		}
		else if (command.startsWith("admin_remove_exp_sp"))
		{
			try
			{
				final String val = command.substring(19);
				adminRemoveExpSP(activeChar, val);
				GMAudit.auditGMAction(activeChar.getName(), command, val, "");
			}
			catch (final StringIndexOutOfBoundsException e)
			{ // Case of empty character name
				final SystemMessage sm = new SystemMessage(614);
				sm.addString("Error while removing Exp-Sp.");
				activeChar.sendPacket(sm);
				// listCharacters(client, 0);
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
	
	private void addExpSp(L2PcInstance activeChar)
	{
		final L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			final SystemMessage sm = new SystemMessage(614);
			sm.addString("Incorrect target.");
			activeChar.sendPacket(sm);
			return;
		}
		
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		
		final TextBuilder replyMSG = new TextBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<center>Editing <font color=\"LEVEL\">" + player.getName() + "</font></center>");
		replyMSG.append("<table width=270>");
		replyMSG.append("<tr><td>Level: " + player.getLevel() + "</td></tr>");
		replyMSG.append("<tr><td>Class: " + player.getTemplate().className + "</td></tr>");
		replyMSG.append("<tr><td>Exp: " + player.getExp() + "</td></tr>");
		replyMSG.append("<tr><td>SP: " + player.getSp() + "</td></tr></table>");
		replyMSG.append("<table width=270><tr><td>Note: Fill BOTH values before saving the modifications</td></tr>");
		replyMSG.append("<tr><td>and use 0 if no changes are needed.</td></tr></table><br>");
		replyMSG.append("<center><table><tr>");
		replyMSG.append("<td>Exp: <edit var=\"exp_to_add\" width=50></td>");
		replyMSG.append("<td>SP:  <edit var=\"sp_to_add\" width=50></td>");
		replyMSG.append("<td>&nbsp;<center><button value=\"Add\" action=\"bypass -h admin_add_exp_sp $exp_to_add $sp_to_add\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></td>");
		replyMSG.append("<td>&nbsp;<center><button value=\"Remove\" action=\"bypass -h admin_remove_exp_sp $exp_to_add $sp_to_add\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></td>");
		replyMSG.append("</tr></table></center>");
		replyMSG.append("</body></html>");
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void removeExp(L2PcInstance activeChar)
	{
		final L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			final SystemMessage sm = new SystemMessage(614);
			sm.addString("Incorrect target.");
			activeChar.sendPacket(sm);
			return;
		}
		
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		
		final TextBuilder replyMSG = new TextBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<center>Editing <font color=\"LEVEL\">" + player.getName() + "</font></center>");
		replyMSG.append("<table width=270>");
		replyMSG.append("<tr><td>Level: " + player.getLevel() + "</td></tr>");
		replyMSG.append("<tr><td>Class: " + player.getTemplate().className + "</td></tr>");
		replyMSG.append("<tr><td>Exp: " + player.getExp() + "</td></tr>");
		replyMSG.append("<tr><td>SP: " + player.getSp() + "</td></tr></table>");
		replyMSG.append("<table width=270><tr><td>Note: Fill BOTH values before saving the modifications</td></tr>");
		replyMSG.append("<tr><td>and use 0 if no changes are needed.</td></tr></table><br>");
		replyMSG.append("<center><table><tr>");
		replyMSG.append("<td>remove Exp: <edit var=\"exp_to_remove\" width=50></td>");
		replyMSG.append("<td>remove SP:  <edit var=\"sp_to_remove\" width=50></td>");
		replyMSG.append("<td>&nbsp;<button value=\"Save Changes\" action=\"bypass -h admin_remove_exp_sp $exp_to_remove $sp_to_remove\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table></center>");
		replyMSG.append("</body></html>");
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void adminAddExpSp(L2PcInstance activeChar, String ExpSp)
	{
		final L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			final SystemMessage sm = new SystemMessage(614);
			sm.addString("Incorrect target.");
			activeChar.sendPacket(sm);
			return;
		}
		
		final StringTokenizer st = new StringTokenizer(ExpSp);
		if (st.countTokens() != 2)
		{
			addExpSp(activeChar);
		}
		else
		{
			final String exp = st.nextToken();
			final String sp = st.nextToken();
			long expval = 0;
			int spval = 0;
			try
			{
				expval = Long.parseLong(exp);
				spval = Integer.parseInt(sp);
			}
			catch (final NumberFormatException e)
			{
				// Wrong number (maybe it's too big?)
				final SystemMessage smA = new SystemMessage(614);
				smA.addString("Wrong Number Format");
				activeChar.sendPacket(smA);
			}
			if ((expval != 0) || (spval != 0))
			{
				// Common character information
				final SystemMessage sm = new SystemMessage(614);
				sm.addString("Admin is adding you " + expval + " xp and " + spval + " sp.");
				player.sendPacket(sm);
				
				player.addExpAndSp(expval, spval);
				
				// Admin information
				final SystemMessage smA = new SystemMessage(614);
				smA.addString("Added " + expval + " xp and " + spval + " sp to " + player.getName() + ".");
				activeChar.sendPacket(smA);
				if (Config.DEBUG)
				{
					_log.fine("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") added " + expval + " xp and " + spval + " sp to " + player.getObjectId() + ".");
				}
				
			}
		}
	}
	
	private void adminRemoveExpSP(L2PcInstance activeChar, String ExpSp)
	{
		final L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			final SystemMessage sm = new SystemMessage(614);
			sm.addString("Incorrect target.");
			activeChar.sendPacket(sm);
			return;
		}
		
		final StringTokenizer st = new StringTokenizer(ExpSp);
		if (st.countTokens() != 2)
		{
			removeExp(activeChar);
		}
		else
		{
			final String exp = st.nextToken();
			final String sp = st.nextToken();
			long expval = 0;
			int spval = 0;
			try
			{
				expval = Long.parseLong(exp);
				spval = Integer.parseInt(sp);
			}
			catch (final NumberFormatException e)
			{
				// Wrong number (maybe it's too big?)
				final SystemMessage smA = new SystemMessage(614);
				smA.addString("Wrong Number Format");
				activeChar.sendPacket(smA);
			}
			if ((expval != 0) || (spval != 0))
			{
				// Common character information
				final SystemMessage sm = new SystemMessage(614);
				sm.addString("Admin is removing you " + expval + " xp and " + spval + " sp.");
				player.sendPacket(sm);
				
				player.removeExpAndSp(expval, spval);
				
				// Admin information
				final SystemMessage smA = new SystemMessage(614);
				smA.addString("Removed " + expval + " xp and " + spval + " sp from " + player.getName() + ".");
				activeChar.sendPacket(smA);
				if (Config.DEBUG)
				{
					_log.fine("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") added " + expval + " xp and " + spval + " sp to " + player.getObjectId() + ".");
				}
			}
		}
	}
}