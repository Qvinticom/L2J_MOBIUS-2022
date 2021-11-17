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

import org.l2jmobius.gameserver.handler.IAdminCommandHandler;
import org.l2jmobius.gameserver.model.AutoSpawnHandler;
import org.l2jmobius.gameserver.model.AutoSpawnHandler.AutoSpawnInstance;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.sevensigns.SevenSigns;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.util.BuilderUtil;

/**
 * Admin Command Handler for Mammon NPCs
 * @author Tempy, Zoey76
 */
public class AdminMammon implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_mammon_find",
		"admin_mammon_respawn",
	};
	
	private final boolean _isSealValidation = SevenSigns.getInstance().isSealValidationPeriod();
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		int teleportIndex = -1;
		final AutoSpawnInstance blackSpawnInst = AutoSpawnHandler.getInstance().getAutoSpawnInstance(SevenSigns.MAMMON_BLACKSMITH_ID, false);
		final AutoSpawnInstance merchSpawnInst = AutoSpawnHandler.getInstance().getAutoSpawnInstance(SevenSigns.MAMMON_MERCHANT_ID, false);
		if (command.startsWith("admin_mammon_find"))
		{
			try
			{
				if (command.length() > 17)
				{
					teleportIndex = Integer.parseInt(command.substring(18));
				}
			}
			catch (Exception e)
			{
				BuilderUtil.sendSysMessage(activeChar, "Usage: //mammon_find [teleportIndex] (where 1 = Blacksmith, 2 = Merchant)");
				return false;
			}
			
			if (!_isSealValidation)
			{
				activeChar.sendPacket(SystemMessageId.THE_SSQ_COMPETITION_PERIOD_IS_UNDERWAY);
				return false;
			}
			
			if (blackSpawnInst != null)
			{
				final Npc blackInst = blackSpawnInst.getNpcList().peek();
				if (blackInst != null)
				{
					BuilderUtil.sendSysMessage(activeChar, "Blacksmith of Mammon: " + blackInst.getX() + " " + blackInst.getY() + " " + blackInst.getZ());
					if (teleportIndex == 1)
					{
						activeChar.teleToLocation(blackInst.getLocation(), true);
					}
				}
			}
			else
			{
				BuilderUtil.sendSysMessage(activeChar, "Blacksmith of Mammon isn't registered for spawn.");
			}
			
			if (merchSpawnInst != null)
			{
				final Npc merchInst = merchSpawnInst.getNpcList().peek();
				if (merchInst != null)
				{
					BuilderUtil.sendSysMessage(activeChar, "Merchant of Mammon: " + merchInst.getX() + " " + merchInst.getY() + " " + merchInst.getZ());
					if (teleportIndex == 2)
					{
						activeChar.teleToLocation(merchInst.getLocation(), true);
					}
				}
			}
			else
			{
				BuilderUtil.sendSysMessage(activeChar, "Merchant of Mammon isn't registered for spawn.");
			}
		}
		else if (command.startsWith("admin_mammon_respawn"))
		{
			if (!_isSealValidation)
			{
				activeChar.sendPacket(SystemMessageId.THE_SSQ_COMPETITION_PERIOD_IS_UNDERWAY);
				return true;
			}
			
			if (merchSpawnInst != null)
			{
				final long merchRespawn = AutoSpawnHandler.getInstance().getTimeToNextSpawn(merchSpawnInst);
				BuilderUtil.sendSysMessage(activeChar, "The Merchant of Mammon will respawn in " + (merchRespawn / 60000) + " minute(s).");
			}
			else
			{
				BuilderUtil.sendSysMessage(activeChar, "Merchant of Mammon isn't registered for spawn.");
			}
			
			if (blackSpawnInst != null)
			{
				final long blackRespawn = AutoSpawnHandler.getInstance().getTimeToNextSpawn(blackSpawnInst);
				BuilderUtil.sendSysMessage(activeChar, "The Blacksmith of Mammon will respawn in " + (blackRespawn / 60000) + " minute(s).");
			}
			else
			{
				BuilderUtil.sendSysMessage(activeChar, "Blacksmith of Mammon isn't registered for spawn.");
			}
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
