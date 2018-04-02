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

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import com.l2jmobius.Config;
import com.l2jmobius.commons.concurrent.ThreadPool;
import com.l2jmobius.gameserver.datatables.SkillTable;
import com.l2jmobius.gameserver.handler.IAdminCommandHandler;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jmobius.gameserver.network.serverpackets.UserInfo;

/**
 * This class ...
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */
public class AdminTest implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_test",
		"admin_stats",
		"admin_mcrit",
		"admin_addbufftest",
		"admin_skill_test",
		"admin_st",
		"admin_mp",
		"admin_known",
		"admin_oly_obs_mode",
		"admin_obs_mode"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equals("admin_stats"))
		{
			for (String line : ThreadPool.getStats())
			{
				activeChar.sendMessage(line);
			}
		}
		if (command.equals("admin_mcrit"))
		{
			final L2Character target = (L2Character) activeChar.getTarget();
			
			activeChar.sendMessage("Activechar Mcrit " + activeChar.getMCriticalHit(null, null));
			activeChar.sendMessage("Activechar baseMCritRate " + activeChar.getTemplate().baseMCritRate);
			
			if (target != null)
			{
				activeChar.sendMessage("Target Mcrit " + target.getMCriticalHit(null, null));
				activeChar.sendMessage("Target baseMCritRate " + target.getTemplate().baseMCritRate);
			}
		}
		if (command.equals("admin_addbufftest"))
		{
			final L2Character target = (L2Character) activeChar.getTarget();
			activeChar.sendMessage("cast");
			
			final L2Skill skill = SkillTable.getInstance().getInfo(1085, 3);
			
			if (target != null)
			{
				activeChar.sendMessage("target locked");
				
				for (int i = 0; i < 100;)
				{
					if (activeChar.isCastingNow())
					{
						continue;
					}
					
					activeChar.sendMessage("Casting " + i);
					activeChar.useMagic(skill, false, false);
					i++;
				}
			}
		}
		else if (command.startsWith("admin_skill_test") || command.startsWith("admin_st"))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command);
				st.nextToken();
				
				final int id = Integer.parseInt(st.nextToken());
				
				adminTestSkill(activeChar, id);
			}
			catch (NumberFormatException | NoSuchElementException e)
			{
				activeChar.sendMessage("Command format is //skill_test <ID>");
			}
		}
		else if (command.equals("admin_mp on"))
		{
			// .startPacketMonitor();
			activeChar.sendMessage("command not working");
		}
		else if (command.equals("admin_mp off"))
		{
			// .stopPacketMonitor();
			activeChar.sendMessage("command not working");
		}
		else if (command.equals("admin_mp dump"))
		{
			// .dumpPacketHistory();
			activeChar.sendMessage("command not working");
		}
		else if (command.equals("admin_known on"))
		{
			Config.CHECK_KNOWN = true;
		}
		else if (command.equals("admin_known off"))
		{
			Config.CHECK_KNOWN = false;
		}
		else if (command.equals("admin_test"))
		{
			activeChar.sendMessage("Now the server will send a packet that client cannot read correctly");
			activeChar.sendMessage("generating a critical error..");
			
			int i = 5;
			while (i > 0)
			{
				activeChar.sendMessage("Client will crash in " + i + " seconds");
				
				try
				{
					Thread.sleep(1000);
					i--;
				}
				catch (InterruptedException e)
				{
				}
			}
			
			final UserInfo ui = new UserInfo(activeChar);
			ui._critical_test = true;
			
			activeChar.sendPacket(ui);
		}
		else if (command.startsWith("admin_oly_obs_mode"))
		{
			if (!activeChar.inObserverMode())
			{
				activeChar.enterOlympiadObserverMode(activeChar.getX(), activeChar.getY(), activeChar.getZ(), -1);
			}
			else
			{
				activeChar.leaveOlympiadObserverMode();
			}
		}
		else if (command.startsWith("admin_obs_mode"))
		{
			if (!activeChar.inObserverMode())
			{
				activeChar.enterObserverMode(activeChar.getX(), activeChar.getY(), activeChar.getZ());
			}
			else
			{
				activeChar.leaveObserverMode();
			}
		}
		return true;
	}
	
	/**
	 * @param activeChar
	 * @param id
	 */
	private void adminTestSkill(L2PcInstance activeChar, int id)
	{
		L2Character player;
		L2Object target = activeChar.getTarget();
		
		if ((target == null) || !(target instanceof L2Character))
		{
			player = activeChar;
		}
		else
		{
			player = (L2Character) target;
		}
		
		player.broadcastPacket(new MagicSkillUse(activeChar, player, id, 1, 1, 1));
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.handler.IAdminCommandHandler#getAdminCommandList()
	 */
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}