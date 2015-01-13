/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.network.clientpackets;

import com.l2jserver.gameserver.datatables.CrestTable;
import com.l2jserver.gameserver.model.L2Crest;
import com.l2jserver.gameserver.network.serverpackets.ExPledgeEmblem;

/**
 * @author -Wooden-, Sdw
 */
public final class RequestExPledgeCrestLarge extends L2GameClientPacket
{
	private static final String _C__D0_10_REQUESTEXPLEDGECRESTLARGE = "[C] D0:10 RequestExPledgeCrestLarge";
	
	private int _crestId;
	private int _clanId;
	
	@Override
	protected void readImpl()
	{
		_crestId = readD();
		_clanId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2Crest crest = CrestTable.getInstance().getCrest(_crestId);
		final byte[] data = crest != null ? crest.getData() : null;
		if (data != null)
		{
			for (int i = 0; i <= 4; i++)
			{
				if (i < 4)
				{
					final byte[] fullChunk = new byte[14336];
					System.arraycopy(data, (14336 * i), fullChunk, 0, 14336);
					sendPacket(new ExPledgeEmblem(_crestId, fullChunk, _clanId, i));
				}
				else
				{
					final byte[] lastChunk = new byte[8320];
					System.arraycopy(data, (14336 * i), lastChunk, 0, 8320);
					sendPacket(new ExPledgeEmblem(_crestId, lastChunk, _clanId, i));
				}
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_10_REQUESTEXPLEDGECRESTLARGE;
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}