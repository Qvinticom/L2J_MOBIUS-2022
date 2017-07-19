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
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.ExAutoSoulShot;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 * @version $Revision: 1.0.0.0 $ $Date: 2005/07/11 15:29:30 $
 */
public class RequestAutoSoulShot extends L2GameClientPacket
{
	private static final String _C__CF_REQUESTAUTOSOULSHOT = "[C] CF RequestAutoSoulShot";
	private static Logger _log = Logger.getLogger(RequestAutoSoulShot.class.getName());
	
	// format cd
	private int _itemId;
	private int _type; // 1 = on : 0 = off;
	
	@Override
	protected void readImpl()
	{
		_itemId = readD();
		_type = readD();
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if ((activeChar.getPrivateStoreType() == 0) && (activeChar.getActiveRequester() == null) && !activeChar.isDead())
		{
			if (Config.DEBUG)
			{
				_log.fine("AutoSoulShot:" + _itemId);
			}
			
			final L2ItemInstance item = activeChar.getInventory().getItemByItemId(_itemId);
			
			if (item != null)
			{
				
				if (_type == 1)
				{
					// Fishing shots are not automatic on retail
					if ((_itemId < 6535) || (_itemId > 6540))
					{
						// Attempt to charge first shot on activation
						if ((_itemId == 6645) || (_itemId == 6646) || (_itemId == 6647))
						{
							if (activeChar.getPet() != null)
							{
								activeChar.addAutoSoulShot(_itemId);
								final ExAutoSoulShot atk = new ExAutoSoulShot(_itemId, _type);
								activeChar.sendPacket(atk);
								
								// start the auto soulshot use
								SystemMessage sm = new SystemMessage(SystemMessage.USE_OF_S1_WILL_BE_AUTO);
								sm.addString(item.getItemName());
								activeChar.sendPacket(sm);
								sm = null;
								
								activeChar.rechargeAutoSoulShot(true, true, true);
							}
							else
							{
								activeChar.sendPacket(new SystemMessage(SystemMessage.NO_PET_TO_AUTOMATE_USE));
							}
						}
						else
						{
							SystemMessage sm;
							if ((_itemId >= 3947) && (_itemId <= 3952) && activeChar.isInOlympiadMode())
							{
								sm = new SystemMessage(SystemMessage.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
								sm.addString(item.getItemName());
								activeChar.sendPacket(sm);
							}
							else
							{
								activeChar.addAutoSoulShot(_itemId);
								final ExAutoSoulShot atk = new ExAutoSoulShot(_itemId, _type);
								activeChar.sendPacket(atk);
								
								// start the auto soulshot use
								sm = new SystemMessage(SystemMessage.USE_OF_S1_WILL_BE_AUTO);
								sm.addString(item.getItemName());
								activeChar.sendPacket(sm);
								
								activeChar.rechargeAutoSoulShot(true, true, false);
							}
							sm = null;
						}
					}
					
				}
				else if (_type == 0)
				{
					activeChar.removeAutoSoulShot(_itemId);
					final ExAutoSoulShot atk = new ExAutoSoulShot(_itemId, _type);
					activeChar.sendPacket(atk);
					
					// cancel the auto soulshot use
					SystemMessage sm = new SystemMessage(SystemMessage.AUTO_USE_OF_S1_CANCELLED);
					sm.addString(item.getItemName());
					activeChar.sendPacket(sm);
					sm = null;
				}
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
		return _C__CF_REQUESTAUTOSOULSHOT;
	}
}