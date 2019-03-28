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

import com.l2jmobius.gameserver.instancemanager.RaidBossPointsManager;
import com.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.ExGetBossRecord;

/**
 * Format: (ch) d
 * @author -Wooden-
 */
public class RequestGetBossRecord extends GameClientPacket
{
	@SuppressWarnings("unused")
	private int _bossId;
	
	@Override
	protected void readImpl()
	{
		_bossId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final PlayerInstance player = getClient().getPlayer();
		if (player == null)
		{
			return;
		}
		
		final int points = RaidBossPointsManager.getPointsByOwnerId(player.getObjectId());
		final int ranking = RaidBossPointsManager.calculateRanking(player.getObjectId());
		
		// trigger packet
		player.sendPacket(new ExGetBossRecord(ranking, points, RaidBossPointsManager.getList(player)));
		sendPacket(ActionFailed.STATIC_PACKET);
	}
}
