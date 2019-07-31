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
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.gameserver.datatables.sql.HennaTreeTable;
import org.l2jmobius.gameserver.model.actor.instance.HennaInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.serverpackets.HennaEquipList;

/**
 * RequestHennaList - 0xba
 * @author Tempy
 */
public class RequestHennaList extends GameClientPacket
{
	// This is just a trigger packet...
	@SuppressWarnings("unused")
	private int _unknown;
	
	@Override
	protected void readImpl()
	{
		_unknown = readD(); // ??
	}
	
	@Override
	protected void runImpl()
	{
		final PlayerInstance player = getClient().getPlayer();
		
		if (player == null)
		{
			return;
		}
		
		final HennaInstance[] henna = HennaTreeTable.getInstance().getAvailableHenna(player.getClassId());
		final HennaEquipList he = new HennaEquipList(player, henna);
		player.sendPacket(he);
	}
}
