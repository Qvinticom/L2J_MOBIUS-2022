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

import com.l2jmobius.gameserver.instancemanager.FortManager;
import com.l2jmobius.gameserver.model.entity.Fort;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.ExShowFortressMapInfo;

/**
 * @author KenM
 */
public class RequestFortressMapInfo extends L2GameClientPacket
{
	private int _fortressId;
	
	@Override
	protected void readImpl()
	{
		_fortressId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Fort fort = FortManager.getInstance().getFortById(_fortressId);
		
		if (fort == null)
		{
			_log.warning("Fort is not found with id (" + _fortressId + ") in all forts with size of (" + FortManager.getInstance().getForts().size() + ") called by player (" + getActiveChar() + ")");
			
			if (getActiveChar() == null)
			{
				return;
			}
			
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		sendPacket(new ExShowFortressMapInfo(fort));
	}
}
