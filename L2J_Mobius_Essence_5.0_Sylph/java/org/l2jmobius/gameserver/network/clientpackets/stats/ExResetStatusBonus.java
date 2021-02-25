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
package org.l2jmobius.gameserver.network.clientpackets.stats;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.stats.Stat;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;

/**
 * @author Mobius
 */
public class ExResetStatusBonus implements IClientIncomingPacket
{
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final PlayerInstance player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		// 200 L-Coin requirement?
		if (player.reduceAdena("ExResetStatusBonus", 100000, player, true))
		{
			player.getStat().mergeAdd(Stat.STAT_STR, -player.getVariables().getInt(PlayerVariables.STAT_STR, 0));
			player.getStat().mergeAdd(Stat.STAT_DEX, -player.getVariables().getInt(PlayerVariables.STAT_DEX, 0));
			player.getStat().mergeAdd(Stat.STAT_CON, -player.getVariables().getInt(PlayerVariables.STAT_CON, 0));
			player.getStat().mergeAdd(Stat.STAT_INT, -player.getVariables().getInt(PlayerVariables.STAT_INT, 0));
			player.getStat().mergeAdd(Stat.STAT_WIT, -player.getVariables().getInt(PlayerVariables.STAT_WIT, 0));
			player.getStat().mergeAdd(Stat.STAT_MEN, -player.getVariables().getInt(PlayerVariables.STAT_MEN, 0));
			
			player.getVariables().remove(PlayerVariables.STAT_POINTS);
			player.getVariables().remove(PlayerVariables.STAT_STR);
			player.getVariables().remove(PlayerVariables.STAT_DEX);
			player.getVariables().remove(PlayerVariables.STAT_CON);
			player.getVariables().remove(PlayerVariables.STAT_INT);
			player.getVariables().remove(PlayerVariables.STAT_WIT);
			player.getVariables().remove(PlayerVariables.STAT_MEN);
			
			player.sendPacket(new UserInfo(player));
			player.getStat().recalculateStats(true);
		}
	}
}
