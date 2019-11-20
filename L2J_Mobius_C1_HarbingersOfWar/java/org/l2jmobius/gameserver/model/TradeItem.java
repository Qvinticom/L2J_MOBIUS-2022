/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.gameserver.model;

public class TradeItem
{
	private int _ObjectId;
	private int _ItemId;
	private int _Price;
	private int _storePrice;
	private int _count;
	
	public void setObjectId(int id)
	{
		_ObjectId = id;
	}
	
	public int getObjectId()
	{
		return _ObjectId;
	}
	
	public void setItemId(int id)
	{
		_ItemId = id;
	}
	
	public int getItemId()
	{
		return _ItemId;
	}
	
	public void setOwnersPrice(int price)
	{
		_Price = price;
	}
	
	public int getOwnersPrice()
	{
		return _Price;
	}
	
	public void setstorePrice(int price)
	{
		_storePrice = price;
	}
	
	public int getStorePrice()
	{
		return _storePrice;
	}
	
	public void setCount(int count)
	{
		_count = count;
	}
	
	public int getCount()
	{
		return _count;
	}
}
