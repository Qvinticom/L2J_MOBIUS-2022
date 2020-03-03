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
package handlers.admincommandhandlers;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.handler.IAdminCommandHandler;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.entity.TvTEvent;
import org.l2jmobius.gameserver.model.entity.TvTEventTeleporter;
import org.l2jmobius.gameserver.model.entity.TvTManager;
import org.l2jmobius.gameserver.util.BuilderUtil;

/**
 * @author HorridoJoho
 */
public class AdminTvTEvent implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_tvt_add",
		"admin_tvt_remove",
		"admin_tvt_advance"
	};
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		if (command.equals("admin_tvt_add"))
		{
			final WorldObject target = activeChar.getTarget();
			if ((target == null) || !target.isPlayer())
			{
				BuilderUtil.sendSysMessage(activeChar, "You should select a player!");
				return true;
			}
			
			add(activeChar, (PlayerInstance) target);
		}
		else if (command.equals("admin_tvt_remove"))
		{
			final WorldObject target = activeChar.getTarget();
			if ((target == null) || !target.isPlayer())
			{
				BuilderUtil.sendSysMessage(activeChar, "You should select a player!");
				return true;
			}
			
			remove(activeChar, (PlayerInstance) target);
		}
		else if (command.equals("admin_tvt_advance"))
		{
			TvTManager.getInstance().skipDelay();
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void add(PlayerInstance activeChar, PlayerInstance playerInstance)
	{
		if (playerInstance.isOnEvent())
		{
			BuilderUtil.sendSysMessage(activeChar, "Player already participated in the event!");
			return;
		}
		
		if (!TvTEvent.addParticipant(playerInstance))
		{
			BuilderUtil.sendSysMessage(activeChar, "Player instance could not be added, it seems to be null!");
			return;
		}
		
		if (TvTEvent.isStarted())
		{
			new TvTEventTeleporter(playerInstance, TvTEvent.getParticipantTeamCoordinates(playerInstance.getObjectId()), true, false);
		}
	}
	
	private void remove(PlayerInstance activeChar, PlayerInstance playerInstance)
	{
		if (!TvTEvent.removeParticipant(playerInstance.getObjectId()))
		{
			BuilderUtil.sendSysMessage(activeChar, "Player is not part of the event!");
			return;
		}
		
		new TvTEventTeleporter(playerInstance, Config.TVT_EVENT_PARTICIPATION_NPC_COORDINATES, true, true);
	}
}
