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

import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.network.serverpackets.CharDeleteFail;
import com.l2jmobius.gameserver.network.serverpackets.CharDeleteOk;
import com.l2jmobius.gameserver.network.serverpackets.CharSelectInfo;

/**
 * This class ...
 * @version $Revision: 1.8.2.1.2.3 $ $Date: 2005/03/27 15:29:30 $
 */
public class CharacterDelete extends L2GameClientPacket
{
	private static final String _C__0C_CHARACTERDELETE = "[C] 0C CharacterDelete";
	private static Logger _log = Logger.getLogger(CharacterDelete.class.getName());
	
	// cd
	private int _charSlot;
	
	@Override
	protected void readImpl()
	{
		_charSlot = readD();
	}
	
	@Override
	public void runImpl()
	{
		if (Config.DEBUG)
		{
			_log.fine("deleting slot:" + _charSlot);
		}
		
		try
		{
			final byte answer = getClient().markToDeleteChar(_charSlot);
			switch (answer)
			{
				
				case -1: // Error
					break;
				case 0: // Success!
					sendPacket(new CharDeleteOk());
					break;
				case 1:
					sendPacket(new CharDeleteFail(CharDeleteFail.REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER));
					break;
				case 2:
					sendPacket(new CharDeleteFail(CharDeleteFail.REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED));
					break;
			}
			
		}
		catch (final Exception e)
		{
			_log.log(Level.SEVERE, "Error:", e);
			
		}
		
		final CharSelectInfo cl = new CharSelectInfo(getClient().getAccountName(), getClient().getSessionId().playOkID1);
		sendPacket(cl);
		getClient().setCharSelection(cl.getCharInfo());
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__0C_CHARACTERDELETE;
	}
}