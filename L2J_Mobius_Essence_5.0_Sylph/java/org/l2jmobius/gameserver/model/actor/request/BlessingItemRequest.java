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
package org.l2jmobius.gameserver.model.actor.request;

import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;

/**
 * @author Horus
 */
public class BlessingItemRequest extends AbstractRequest
{
	private volatile int _blessingItemObjectId;
	private volatile int _blessingScrollObjectId;
	
	public BlessingItemRequest(PlayerInstance player, int enchantingScrollObjectId)
	{
		super(player);
		_blessingScrollObjectId = enchantingScrollObjectId;
	}
	
	public ItemInstance getBlessingItem()
	{
		return getActiveChar().getInventory().getItemByObjectId(_blessingItemObjectId);
	}
	
	public void setBlessingItem(int objectId)
	{
		_blessingItemObjectId = objectId;
	}
	
	public ItemInstance getBlessScroll()
	{
		return getActiveChar().getInventory().getItemByObjectId(_blessingScrollObjectId);
	}
	
	public void setBlessScroll(int objectId)
	{
		_blessingScrollObjectId = objectId;
	}
	
	@Override
	public boolean isItemRequest()
	{
		return true;
	}
	
	@Override
	public boolean canWorkWith(AbstractRequest request)
	{
		return !request.isItemRequest();
	}
	
	@Override
	public boolean isUsing(int objectId)
	{
		return (objectId > 0) && ((objectId == _blessingItemObjectId) || (objectId == _blessingScrollObjectId));
	}
}
