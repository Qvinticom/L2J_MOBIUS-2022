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
package com.l2jmobius.gameserver.network.clientpackets.primeshop;

import com.l2jmobius.gameserver.data.xml.impl.PrimeShopData;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket;

/**
 * @author Gnacik, UnAfraid
 */
public final class RequestBRProductInfo extends L2GameClientPacket
{
	private int _brId;
	
	@Override
	protected void readImpl()
	{
		_brId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player != null)
		{
			PrimeShopData.getInstance().showProductInfo(player, _brId);
		}
	}
	
	@Override
	public String getType()
	{
		return getClass().getSimpleName();
	}
}
