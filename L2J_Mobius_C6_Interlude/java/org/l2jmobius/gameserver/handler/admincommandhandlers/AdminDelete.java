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
package org.l2jmobius.gameserver.handler.admincommandhandlers;

import org.l2jmobius.gameserver.datatables.sql.SpawnTable;
import org.l2jmobius.gameserver.handler.IAdminCommandHandler;
import org.l2jmobius.gameserver.instancemanager.GrandBossManager;
import org.l2jmobius.gameserver.instancemanager.RaidBossSpawnManager;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.spawn.Spawn;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * This class handles following admin commands: - delete = deletes target
 * @version $Revision: 1.2.2.1.2.4 $ $Date: 2005/04/11 10:05:56 $
 */
public class AdminDelete implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_delete"
	};
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
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
	
	// TODO: add possibility to delete any WorldObject (except PlayerInstance)
	private void handleDelete(PlayerInstance activeChar)
	{
		final WorldObject obj = activeChar.getTarget();
		
		if (obj instanceof NpcInstance)
		{
			final NpcInstance target = (NpcInstance) obj;
			target.deleteMe();
			
			final Spawn spawn = target.getSpawn();
			if (spawn != null)
			{
				spawn.stopRespawn();
				
				if (RaidBossSpawnManager.getInstance().isDefined(spawn.getNpcId()) && !spawn.isCustomBossInstance())
				{
					RaidBossSpawnManager.getInstance().deleteSpawn(spawn, true);
				}
				else
				{
					boolean update = true;
					if (GrandBossManager.getInstance().isDefined(spawn.getNpcId()) && spawn.isCustomBossInstance())
					{
						update = false;
					}
					
					SpawnTable.getInstance().deleteSpawn(spawn, update);
				}
			}
			
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("Deleted " + target.getName() + " from " + target.getObjectId() + ".");
			activeChar.sendPacket(sm);
		}
		else
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("Incorrect target.");
			activeChar.sendPacket(sm);
		}
	}
}
