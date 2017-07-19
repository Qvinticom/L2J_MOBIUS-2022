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
package com.l2jmobius.gameserver.network.clientpackets;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.Olympiad;
import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.communitybbs.CommunityBoard;
import com.l2jmobius.gameserver.handler.AdminCommandHandler;
import com.l2jmobius.gameserver.handler.IAdminCommandHandler;
import com.l2jmobius.gameserver.model.L2CharPosition;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2CastleChamberlainInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2ManorManagerInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.entity.L2Event;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * This class ...
 * @version $Revision: 1.12.4.5 $ $Date: 2005/04/11 10:06:11 $
 */
public class RequestBypassToServer extends L2GameClientPacket
{
	private static final String _C__21_REQUESTBYPASSTOSERVER = "[C] 21 RequestBypassToServer";
	private static Logger _log = Logger.getLogger(RequestBypassToServer.class.getName());
	
	// S
	private String _command;
	
	@Override
	protected void readImpl()
	{
		_command = readS();
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getServerBypass().tryPerformAction(_command))
		{
			return;
		}
		
		try
		{
			if (_command.startsWith("admin_"))
			{
				
				if (Config.ALT_PRIVILEGES_ADMIN && !AdminCommandHandler.getInstance().checkPrivileges(activeChar, _command))
				
				{
					
					_log.info("<GM>" + activeChar + " does not have sufficient privileges for command '" + _command + "'.");
					
					return;
					
				}
				
				final IAdminCommandHandler ach = AdminCommandHandler.getInstance().getAdminCommandHandler(_command);
				if (ach != null)
				{
					ach.useAdminCommand(_command, activeChar);
				}
				else
				{
					_log.warning("No handler registered for bypass '" + _command + "'");
				}
			}
			else if (_command.equals("come_here") && (activeChar.getAccessLevel() >= Config.GM_ACCESSLEVEL))
			{
				comeHere(activeChar);
			}
			else if (_command.startsWith("player_help "))
			{
				playerHelp(activeChar, _command.substring(12));
			}
			else if (_command.startsWith("npc_"))
			{
				if (!activeChar.validateBypass(_command))
				{
					return;
				}
				
				final int endOfId = _command.indexOf('_', 5);
				String id;
				if (endOfId > 0)
				{
					id = _command.substring(4, endOfId);
				}
				else
				{
					id = _command.substring(4);
				}
				
				try
				
				{
					final L2Object object = L2World.getInstance().findObject(Integer.parseInt(id));
					if (_command.substring(endOfId + 1).startsWith("event_participate"))
					{
						L2Event.inscribePlayer(activeChar);
					}
					else if ((object != null) && (object instanceof L2NpcInstance) && (endOfId > 0) && activeChar.isInsideRadius(object, L2NpcInstance.INTERACTION_DISTANCE, false, false))
					{
						((L2NpcInstance) object).onBypassFeedback(activeChar, _command.substring(endOfId + 1));
					}
					
					activeChar.sendPacket(new ActionFailed());
					
				}
				
				catch (final NumberFormatException nfe)
				{
				}
			}
			
			else if (_command.equals("menu_select?ask=-16&reply=1")) // Draw a Symbol
			{
				final L2Object object = activeChar.getTarget();
				if (object instanceof L2NpcInstance)
				{
					((L2NpcInstance) object).onBypassFeedback(activeChar, _command);
				}
				
			}
			else if (_command.equals("menu_select?ask=-16&reply=2"))
			{
				final L2Object object = activeChar.getTarget();
				if (object instanceof L2NpcInstance)
				{
					((L2NpcInstance) object).onBypassFeedback(activeChar, _command);
				}
				
			}
			else if (_command.startsWith("manor_menu_select?"))
			{
				final L2Object object = activeChar.getLastFolkNPC();
				if (((object instanceof L2ManorManagerInstance) || (object instanceof L2CastleChamberlainInstance)) && activeChar.isInsideRadius(object, L2NpcInstance.INTERACTION_DISTANCE, false, false))
				{
					((L2NpcInstance) object).onBypassFeedback(activeChar, _command);
				}
			}
			else if (_command.startsWith("bbs_"))
			{
				CommunityBoard.getInstance().handleCommands(getClient(), _command);
			}
			else if (_command.startsWith("_bbs"))
			{
				CommunityBoard.getInstance().handleCommands(getClient(), _command);
			}
			else if (_command.startsWith("Quest "))
			{
				if (!activeChar.validateBypass(_command))
				{
					return;
				}
				
				final L2PcInstance player = getClient().getActiveChar();
				final String p = _command.substring(6).trim();
				final int idx = p.indexOf(' ');
				if (idx < 0)
				{
					player.processQuestEvent(p, "");
				}
				else
				{
					player.processQuestEvent(p.substring(0, idx), p.substring(idx).trim());
				}
			}
			else if (_command.startsWith("OlympiadArenaChange"))
			{
				Olympiad.getInstance().bypassChangeArena(_command, activeChar);
			}
		}
		catch (final Exception e)
		{
			_log.log(Level.WARNING, "Bad RequestBypassToServer: ", e);
		}
	}
	
	/**
	 * @param activeChar
	 */
	private void comeHere(L2PcInstance activeChar)
	{
		final L2Object obj = activeChar.getTarget();
		if (obj instanceof L2NpcInstance)
		{
			final L2NpcInstance temp = (L2NpcInstance) obj;
			temp.setTarget(activeChar);
			temp.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(activeChar.getX(), activeChar.getY(), activeChar.getZ(), 0));
		}
	}
	
	private void playerHelp(L2PcInstance activeChar, String path)
	{
		
		if (path.indexOf("..") != -1)
		{
			return;
		}
		
		final String filename = "data/html/help/" + path;
		final NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(filename);
		activeChar.sendPacket(html);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__21_REQUESTBYPASSTOSERVER;
	}
}