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

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.datatables.ItemTable;
import com.l2jmobius.gameserver.handler.IAdminCommandHandler;
import com.l2jmobius.gameserver.model.GMAudit;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.ItemList;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jmobius.gameserver.templates.L2Item;

import javolution.text.TextBuilder;

/**
 * This class handles following admin commands: - itemcreate = show menu - create_item id [num] = creates num items with respective id, if num is not specified num = 1 - giveitem name id [num] = gives num items to specified player name with respective id, if num is not specified num = 1
 * @version $Revision: 1.2.2.2.2.3 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminCreateItem implements IAdminCommandHandler
{
	private static String[] _adminCommands =
	{
		"admin_itemcreate",
		"admin_create_item",
		"admin_giveitem"
	};
	
	private static final int REQUIRED_LEVEL = Config.GM_CREATE_ITEM;
	
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
		
		if (command.equals("admin_itemcreate"))
		{
			AdminHelpPage.showHelpPage(activeChar, "itemcreation.htm");
		}
		else if (command.startsWith("admin_create_item"))
		{
			try
			{
				final String val = command.substring(17);
				final StringTokenizer st = new StringTokenizer(val);
				if (st.countTokens() == 2)
				{
					final String id = st.nextToken();
					final int idval = Integer.parseInt(id);
					final String num = st.nextToken();
					final int numval = Integer.parseInt(num);
					createItem(activeChar, activeChar, idval, numval);
				}
				else if (st.countTokens() == 1)
				{
					final String id = st.nextToken();
					final int idval = Integer.parseInt(id);
					createItem(activeChar, activeChar, idval, 1);
				}
				else
				{
					AdminHelpPage.showHelpPage(activeChar, "itemcreation.htm");
				}
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Error while creating item.");
			}
			catch (final NumberFormatException nfe)
			{
				activeChar.sendMessage("Wrong number entered.");
			}
			
			GMAudit.auditGMAction(activeChar.getName(), command, "no-target", "");
		}
		else if (command.startsWith("admin_giveitem"))
		{
			try
			{
				final String val = command.substring(14);
				final StringTokenizer st = new StringTokenizer(val);
				if (st.countTokens() == 3)
				{
					final L2PcInstance target = L2World.getInstance().getPlayer(st.nextToken());
					if (target == null)
					{
						activeChar.sendMessage("Target is not online.");
						return false;
					}
					
					final String id = st.nextToken();
					final int idval = Integer.parseInt(id);
					final String num = st.nextToken();
					final int numval = Integer.parseInt(num);
					createItem(activeChar, target, idval, numval);
					
					GMAudit.auditGMAction(activeChar.getName(), command, target.getName(), "");
				}
				else if (st.countTokens() == 2)
				{
					final L2PcInstance target = L2World.getInstance().getPlayer(st.nextToken());
					if (target == null)
					{
						activeChar.sendMessage("Target is not online.");
						return false;
					}
					
					final String id = st.nextToken();
					final int idval = Integer.parseInt(id);
					createItem(activeChar, target, idval, 1);
					
					GMAudit.auditGMAction(activeChar.getName(), command, target.getName(), "");
				}
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Error while creating item.");
			}
			catch (final NumberFormatException nfe)
			{
				activeChar.sendMessage("Wrong number entered.");
			}
		}
		
		return true;
	}
	
	private void createItem(L2PcInstance activeChar, L2PcInstance target, int id, int num)
	{
		if (num > 20)
		{
			final L2Item template = ItemTable.getInstance().getTemplate(id);
			if (!template.isStackable())
			{
				activeChar.sendMessage("This item does not stack - Creation aborted.");
				return;
			}
		}
		
		target.getInventory().addItem("Admin", id, num, target, null);
		
		final ItemList il = new ItemList(target, true);
		target.sendPacket(il);
		
		activeChar.sendMessage("You have spawned " + num + " item(s) number " + id + " in " + target.getName() + "'s inventory.");
		
		if (activeChar != target)
		{
			target.sendMessage("An Admin has spawned " + num + " item(s) number " + id + " in your inventory.");
		}
		
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		final TextBuilder replyMSG = new TextBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=180><center>Item Creation Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_help itemcreation.htm\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<table width=270><tr><td>Item Creation Complete.<br></td></tr></table>");
		replyMSG.append("<table width=270><tr><td>You have spawned " + num + " item(s) number in " + target.getName() + "'s inventory.</td></tr></table>");
		replyMSG.append("</body></html>");
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	private boolean checkLevel(int level)
	{
		return (level >= REQUIRED_LEVEL);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return _adminCommands;
	}
}