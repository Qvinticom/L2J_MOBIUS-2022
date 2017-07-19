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

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.GameTimeController;
import com.l2jmobius.gameserver.Shutdown;
import com.l2jmobius.gameserver.handler.IAdminCommandHandler;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

import javolution.text.TextBuilder;

/**
 * This class handles following admin commands: - server_shutdown [sec] = shows menu or shuts down server in sec seconds
 * @version $Revision: 1.5.2.1.2.4 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminShutdown implements IAdminCommandHandler
{
	// private static Logger _log = Logger.getLogger(AdminShutdown.class.getName());
	
	private static String[] _adminCommands =
	{
		"admin_server_shutdown",
		"admin_server_restart",
		"admin_server_abort"
	};
	private static final int REQUIRED_LEVEL = Config.GM_RESTART;
	
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
		
		if (command.startsWith("admin_server_shutdown"))
		{
			try
			{
				final int val = Integer.parseInt(command.substring(22));
				serverShutdown(activeChar, val, false);
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				sendHtmlForm(activeChar);
			}
		}
		else if (command.startsWith("admin_server_restart"))
		{
			try
			{
				final int val = Integer.parseInt(command.substring(21));
				serverShutdown(activeChar, val, true);
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				sendHtmlForm(activeChar);
			}
		}
		else if (command.startsWith("admin_server_abort"))
		{
			serverAbort(activeChar);
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
	
	private void sendHtmlForm(L2PcInstance activeChar)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		
		final int t = GameTimeController.getInstance().getGameTime();
		final int h = t / 60;
		final int m = t % 60;
		final SimpleDateFormat format = new SimpleDateFormat("h:mm a");
		final Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, h);
		cal.set(Calendar.MINUTE, m);
		
		final TextBuilder replyMSG = new TextBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=180><center>Server Management Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<table>");
		replyMSG.append("<tr><td>Players Online: " + L2World.getInstance().getAllPlayersCount() + "</td></tr>");
		replyMSG.append("<tr><td>Used Memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + " bytes</td></tr>");
		replyMSG.append("<tr><td>Server Rates: " + Config.RATE_XP + "x, " + Config.RATE_SP + "x, " + Config.RATE_DROP_ADENA + "x, " + Config.RATE_DROP_ITEMS + "x, " + Config.RATE_BOSS_DROP_ITEMS + "x</td></tr>");
		replyMSG.append("<tr><td>Game Time: " + format.format(cal.getTime()) + "</td></tr>");
		replyMSG.append("</table><br>");
		replyMSG.append("<table width=270>");
		replyMSG.append("<tr><td>Enter in seconds the time till the server shutdowns bellow:</td></tr>");
		replyMSG.append("<br>");
		replyMSG.append("<tr><td><center>Seconds till: <edit var=\"shutdown_time\" width=60></center></td></tr>");
		replyMSG.append("</table><br>");
		replyMSG.append("<center><table><tr><td>");
		replyMSG.append("<button value=\"Shutdown\" action=\"bypass -h admin_server_shutdown $shutdown_time\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
		replyMSG.append("<button value=\"Restart\" action=\"bypass -h admin_server_restart $shutdown_time\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
		replyMSG.append("<button value=\"Abort\" action=\"bypass -h admin_server_abort\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("</td></tr></table></center>");
		replyMSG.append("</body></html>");
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private void serverShutdown(L2PcInstance activeChar, int seconds, boolean restart)
	{
		Shutdown.getInstance().startShutdown(activeChar, seconds, restart);
	}
	
	private void serverAbort(L2PcInstance activeChar)
	{
		Shutdown.getInstance().abort(activeChar);
	}
}