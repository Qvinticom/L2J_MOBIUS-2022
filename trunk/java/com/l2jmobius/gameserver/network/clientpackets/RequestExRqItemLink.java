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

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;
import com.l2jmobius.gameserver.network.L2GameClient;
import com.l2jmobius.gameserver.network.serverpackets.ExRpItemLink;

/**
 * @author KenM
 */
public class RequestExRqItemLink extends L2GameClientPacket
{
	private static final String _C__D0_1E_REQUESTEXRQITEMLINK = "[C] D0:1E RequestExRqItemLink";
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2GameClient client = getClient();
		if (client == null)
		{
			return;
		}
		
		final L2Object object = L2World.getInstance().findObject(_objectId);
		if (object instanceof L2ItemInstance)
		{
			if (((L2ItemInstance) object).isPublished())
			{
				client.sendPacket(new ExRpItemLink((L2ItemInstance) object));
			}
			else
			{
				if (Config.DEBUG)
				{
					_log.info(getClient() + " requested item link for item which wasnt published! ID:" + _objectId);
				}
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_1E_REQUESTEXRQITEMLINK;
	}
}
