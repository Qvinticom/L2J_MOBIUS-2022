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
import com.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 * @version $Revision: 1.8.2.3.2.7 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestUnEquipItem extends L2GameClientPacket
{
	private static final String _C__11_REQUESTUNEQUIPITEM = "[C] 11 RequestUnequipItem";
	private static Logger _log = Logger.getLogger(RequestUnEquipItem.class.getName());
	
	// cd
	private int _slot;
	
	@Override
	protected void readImpl()
	{
		_slot = readD();
	}
	
	@Override
	public void runImpl()
	{
		if (Config.DEBUG)
		{
			_log.fine("request unequip slot " + _slot);
		}
		
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		final L2ItemInstance item = activeChar.getInventory().getPaperdollItemByL2ItemId(_slot);
		if ((item != null) && item.isWear())
		{
			return;
		}
		
		// Prevent player to remove the weapon on special conditions
		if (activeChar.isAttackingNow() || activeChar.isCastingNow() || activeChar.isStunned() || activeChar.isSleeping() || activeChar.isParalyzed() || activeChar.isAlikeDead())
		{
			return;
		}
		
		final L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInBodySlotAndRecord(_slot);
		
		// show the update in the inventory
		final InventoryUpdate iu = new InventoryUpdate();
		
		for (final L2ItemInstance element : unequiped)
		{
			
			activeChar.checkSShotsMatch(null, element);
			iu.addModifiedItem(element);
		}
		activeChar.sendPacket(iu);
		
		activeChar.broadcastUserInfo();
		
		// this can be 0 if the user pressed the right mousebutton twice very fast
		if (unequiped.length > 0)
		{
			
			SystemMessage sm = null;
			
			if (unequiped[0].getEnchantLevel() > 0)
			{
				sm = new SystemMessage(SystemMessage.EQUIPMENT_S1_S2_REMOVED);
				sm.addNumber(unequiped[0].getEnchantLevel());
				sm.addItemName(unequiped[0].getItemId());
			}
			else
			{
				sm = new SystemMessage(SystemMessage.S1_DISARMED);
				sm.addItemName(unequiped[0].getItemId());
			}
			
			activeChar.sendPacket(sm);
			sm = null;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__11_REQUESTUNEQUIPITEM;
	}
}