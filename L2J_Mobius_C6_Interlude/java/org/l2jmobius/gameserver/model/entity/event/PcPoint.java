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
package org.l2jmobius.gameserver.model.entity.event;

import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * @author ProGramMoS
 */
public class PcPoint implements Runnable
{
	Logger LOGGER = Logger.getLogger(PcPoint.class.getName());
	
	private PcPoint()
	{
		LOGGER.info("PcBang point event started.");
	}
	
	@Override
	public void run()
	{
		int score = 0;
		for (PlayerInstance player : World.getInstance().getAllPlayers())
		{
			if ((player.isOnline() == 1) && (player.getLevel() > Config.PCB_MIN_LEVEL) && !player.isInOfflineMode())
			{
				score = Rnd.get(Config.PCB_POINT_MIN, Config.PCB_POINT_MAX);
				
				if (Rnd.get(100) <= Config.PCB_CHANCE_DUAL_POINT)
				{
					score *= 2;
					
					player.addPcBangScore(score);
					
					SystemMessage sm = new SystemMessage(SystemMessageId.DOUBLE_POINTS_YOU_GOT_$51_GLASSES_PC);
					sm.addNumber(score);
					player.sendPacket(sm);
					
					player.updatePcBangWnd(score, true, true);
				}
				else
				{
					player.addPcBangScore(score);
					
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_RECEVIED_$51_GLASSES_PC);
					sm.addNumber(score);
					player.sendPacket(sm);
					
					player.updatePcBangWnd(score, true, false);
				}
			}
		}
	}
	
	public static PcPoint getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final PcPoint INSTANCE = new PcPoint();
	}
}
