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

import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.cache.CrestCache;
import com.l2jmobius.gameserver.network.serverpackets.PledgeCrest;

/**
 * This class ...
 * @version $Revision: 1.4.4.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestPledgeCrest extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(RequestPledgeCrest.class.getName());
	private static final String _C__68_REQUESTPLEDGECREST = "[C] 68 RequestPledgeCrest";
	
	private int _crestId;
	
	@Override
	protected void readImpl()
	{
		_crestId = 0;
		try
		{
			_crestId = readD();
		}
		catch (final Exception e)
		{
		}
	}
	
	@Override
	public void runImpl()
	{
		if (_crestId == 0)
		{
			return;
		}
		
		if (Config.DEBUG)
		{
			_log.fine("crestid " + _crestId + " requested");
		}
		
		final byte[] data = CrestCache.getInstance().getPledgeCrest(_crestId);
		if (data != null)
		{
			final PledgeCrest pc = new PledgeCrest(_crestId, data);
			sendPacket(pc);
		}
		else
		{
			if (Config.DEBUG)
			{
				_log.fine("crest is missing:" + _crestId);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__68_REQUESTPLEDGECREST;
	}
}