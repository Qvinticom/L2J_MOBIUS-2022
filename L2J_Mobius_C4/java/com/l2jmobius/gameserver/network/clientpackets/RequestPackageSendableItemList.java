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

import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.PcFreight;
import com.l2jmobius.gameserver.network.serverpackets.PackageSendableList;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * Format: (c)d d: char object id (?)
 * @author -Wooden-
 */
public class RequestPackageSendableItemList extends L2GameClientPacket
{
	private static final String _C_9E_REQUESTPACKAGESENDABLEITEMLIST = "[C] 9E RequestPackageSendableItemList";
	private int _objectID;
	
	@Override
	protected void readImpl()
	{
		_objectID = readD();
	}
	
	/**
	 * @see com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket#runImpl()
	 */
	@Override
	public void runImpl()
	{
		if (getClient().getActiveChar() == null)
		{
			return;
		}
		
		final L2ItemInstance[] items = getClient().getActiveChar().getInventory().getAvailableItems(true);
		
		getClient().getActiveChar().setActiveWarehouse(new PcFreight(null));
		
		// build list...
		sendPacket(new PackageSendableList(items, _objectID));
		sendPacket(new SystemMessage(SystemMessage.PACKAGES_CAN_ONLY_BE_RETRIEVED_HERE));
	}
	
	@Override
	public String getType()
	{
		return _C_9E_REQUESTPACKAGESENDABLEITEMLIST;
	}
}