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

import org.l2jmobius.commons.concurrent.ThreadPool;
import org.l2jmobius.gameserver.handler.IAdminCommandHandler;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.entity.MonsterRace;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.DeleteObject;
import org.l2jmobius.gameserver.network.serverpackets.MonRaceInfo;
import org.l2jmobius.gameserver.network.serverpackets.PlaySound;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * This class handles following admin commands: - invul = turns invulnerability on/off
 * @version $Revision: 1.1.6.4 $ $Date: 2007/07/31 10:06:00 $
 */
public class AdminMonsterRace implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_mons"
	};
	
	protected static int state = -1;
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		if (command.equalsIgnoreCase("admin_mons"))
		{
			handleSendPacket(activeChar);
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void handleSendPacket(PlayerInstance activeChar)
	{
		/*
		 * -1 0 to initialize the race 0 15322 to start race 13765 -1 in middle of race -1 0 to end the race 8003 to 8027
		 */
		final int[][] codes =
		{
			{
				-1,
				0
			},
			{
				0,
				15322
			},
			{
				13765,
				-1
			},
			{
				-1,
				0
			}
		};
		final MonsterRace race = MonsterRace.getInstance();
		
		if (state == -1)
		{
			state++;
			race.newRace();
			race.newSpeeds();
			final MonRaceInfo spk = new MonRaceInfo(codes[state][0], codes[state][1], race.getMonsters(), race.getSpeeds());
			activeChar.sendPacket(spk);
			activeChar.broadcastPacket(spk);
		}
		else if (state == 0)
		{
			state++;
			
			final SystemMessage sm = new SystemMessage(SystemMessageId.MONSRACE_RACE_START);
			sm.addNumber(0);
			activeChar.sendPacket(sm);
			
			final PlaySound sRace = new PlaySound(1, "S_Race", 0, 0, 0, 0, 0);
			activeChar.sendPacket(sRace);
			activeChar.broadcastPacket(sRace);
			
			final PlaySound sRace2 = new PlaySound(0, "ItemSound2.race_start", 1, 121209259, 12125, 182487, -3559);
			activeChar.sendPacket(sRace2);
			activeChar.broadcastPacket(sRace2);
			
			final MonRaceInfo spk = new MonRaceInfo(codes[state][0], codes[state][1], race.getMonsters(), race.getSpeeds());
			activeChar.sendPacket(spk);
			activeChar.broadcastPacket(spk);
			
			ThreadPool.schedule(new RunRace(codes, activeChar), 5000);
		}
	}
	
	class RunRace implements Runnable
	{
		private final int[][] codes;
		private final PlayerInstance activeChar;
		
		public RunRace(int[][] pCodes, PlayerInstance pActiveChar)
		{
			codes = pCodes;
			activeChar = pActiveChar;
		}
		
		@Override
		public void run()
		{
			final MonRaceInfo spk = new MonRaceInfo(codes[2][0], codes[2][1], MonsterRace.getInstance().getMonsters(), MonsterRace.getInstance().getSpeeds());
			activeChar.sendPacket(spk);
			activeChar.broadcastPacket(spk);
			ThreadPool.schedule(new RunEnd(activeChar), 30000);
		}
	}
	
	class RunEnd implements Runnable
	{
		private final PlayerInstance activeChar;
		
		public RunEnd(PlayerInstance pActiveChar)
		{
			activeChar = pActiveChar;
		}
		
		@Override
		public void run()
		{
			DeleteObject obj = null;
			
			for (int i = 0; i < 8; i++)
			{
				obj = new DeleteObject(MonsterRace.getInstance().getMonsters()[i]);
				activeChar.sendPacket(obj);
				activeChar.broadcastPacket(obj);
			}
			state = -1;
		}
	}
}
