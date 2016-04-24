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

import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.SpawnItem;
import com.l2jmobius.gameserver.network.serverpackets.UserInfo;

public class RequestRecordInfo extends L2GameClientPacket
{
	private static final String _C__6E_REQUEST_RECORD_INFO = "[C] 6E RequestRecordInfo";
	
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		activeChar.sendPacket(new UserInfo(activeChar));
		
		for (L2Object object : activeChar.getKnownList().getKnownObjects().values())
		{
			if (object.getPoly().isMorphed() && object.getPoly().getPolyType().equals("item"))
			{
				activeChar.sendPacket(new SpawnItem(object));
			}
			else
			{
				if (!object.isVisibleFor(activeChar))
				{
					object.sendInfo(activeChar);
					if ((object instanceof L2Character) && (((L2Character) object).getAI() != null))
					{
						((L2Character) object).getAI().describeStateToPlayer(activeChar);
					}
				}
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__6E_REQUEST_RECORD_INFO;
	}
}
