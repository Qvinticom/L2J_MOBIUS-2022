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

import com.l2jmobius.gameserver.datatables.sql.SpawnTable;
import com.l2jmobius.gameserver.handler.IAdminCommandHandler;
import com.l2jmobius.gameserver.instancemanager.GrandBossManager;
import com.l2jmobius.gameserver.instancemanager.RaidBossSpawnManager;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.spawn.L2Spawn;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * This class handles following admin commands: - delete = deletes target
 * @version $Revision: 1.2.2.1.2.4 $ $Date: 2005/04/11 10:05:56 $
 */
public class AdminDelete implements IAdminCommandHandler
{
	// private static Logger LOGGER = Logger.getLogger(AdminDelete.class);
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_delete"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		/*
		 * if(!AdminCommandAccessRights.getInstance().hasAccess(command, activeChar.getAccessLevel())){ return false; } if(Config.GMAUDIT) { Logger _logAudit = Logger.getLogger("gmaudit"); LogRecord record = new LogRecord(Level.INFO, command); record.setParameters(new Object[] { "GM: " +
		 * activeChar.getName(), " to target [" + activeChar.getTarget() + "] " }); _logAudit.LOGGER(record); }
		 */
		
		if (command.equals("admin_delete"))
		{
			handleDelete(activeChar);
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	// TODO: add possibility to delete any L2Object (except L2PcInstance)
	private void handleDelete(L2PcInstance activeChar)
	{
		L2Object obj = activeChar.getTarget();
		
		if ((obj != null) && (obj instanceof L2NpcInstance))
		{
			final L2NpcInstance target = (L2NpcInstance) obj;
			target.deleteMe();
			
			L2Spawn spawn = target.getSpawn();
			if (spawn != null)
			{
				spawn.stopRespawn();
				
				if (RaidBossSpawnManager.getInstance().isDefined(spawn.getNpcId()) && !spawn.is_customBossInstance())
				{
					RaidBossSpawnManager.getInstance().deleteSpawn(spawn, true);
				}
				else
				{
					boolean update_db = true;
					if (GrandBossManager.getInstance().isDefined(spawn.getNpcId()) && spawn.is_customBossInstance())
					{
						update_db = false;
					}
					
					SpawnTable.getInstance().deleteSpawn(spawn, update_db);
				}
			}
			
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("Deleted " + target.getName() + " from " + target.getObjectId() + ".");
			activeChar.sendPacket(sm);
		}
		else
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("Incorrect target.");
			activeChar.sendPacket(sm);
		}
	}
}
