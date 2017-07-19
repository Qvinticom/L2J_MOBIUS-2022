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
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.L2GameClient.GameClientState;
import com.l2jmobius.gameserver.network.serverpackets.CharSelected;

/**
 * This class ...
 * @version $Revision: 1.5.2.1.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public class CharacterSelected extends L2GameClientPacket
{
	private static final String _C__0D_CHARACTERSELECTED = "[C] 0D CharacterSelected";
	private static Logger _log = Logger.getLogger(CharacterSelected.class.getName());
	
	// cd
	private int _charSlot;
	
	@SuppressWarnings("unused")
	private int _unk1; // new in C4
	@SuppressWarnings("unused")
	private int _unk2; // new in C4
	@SuppressWarnings("unused")
	private int _unk3; // new in C4
	@SuppressWarnings("unused")
	private int _unk4; // new in C4
	
	@Override
	protected void readImpl()
	{
		_charSlot = readD();
		_unk1 = readH();
		_unk2 = readD();
		_unk3 = readD();
		_unk4 = readD();
	}
	
	@Override
	public void runImpl()
	{
		if (!getClient().getFloodProtectors().getCharacterSelect().tryPerformAction("CharacterSelect"))
		{
			return;
		}
		
		// we should always be able to acquire the lock
		// but if we cant lock then nothing should be done (ie repeated packet)
		if (getClient().getActiveCharLock().tryLock())
		{
			try
			{
				// should always be null
				// but if not then this is repeated packet and nothing should be done here
				if (getClient().getActiveChar() == null)
				{
					// The L2PcInstance must be created here, so that it can be attached to the L2GameClient
					if (Config.DEBUG)
					{
						_log.fine("selected slot:" + _charSlot);
					}
					
					// load up character from disk
					final L2PcInstance cha = getClient().loadCharFromDisk(_charSlot);
					if (cha == null)
					{
						return;
					}
					
					if (cha.getAccessLevel() < 0)
					{
						cha.logout();
						return;
					}
					
					cha.setClient(getClient());
					getClient().setActiveChar(cha);
					
					getClient().setState(GameClientState.IN_GAME);
					final CharSelected cs = new CharSelected(cha, getClient().getSessionId().playOkID1);
					sendPacket(cs);
				}
			}
			finally
			{
				getClient().getActiveCharLock().unlock();
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
		return _C__0D_CHARACTERSELECTED;
	}
}