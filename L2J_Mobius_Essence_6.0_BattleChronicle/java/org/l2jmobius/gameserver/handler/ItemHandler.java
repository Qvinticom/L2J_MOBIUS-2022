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
package org.l2jmobius.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import org.l2jmobius.gameserver.model.items.EtcItem;

/**
 * This class manages handlers of items
 * @author UnAfraid
 */
public class ItemHandler implements IHandler<IItemHandler, EtcItem>
{
	private final Map<String, IItemHandler> _datatable;
	
	/**
	 * Constructor of ItemHandler
	 */
	protected ItemHandler()
	{
		_datatable = new HashMap<>();
	}
	
	/**
	 * Adds handler of item type in <i>datatable</i>.<br>
	 * <b><i>Concept :</i></u><br>
	 * This handler is put in <i>datatable</i> Map &lt;String ; IItemHandler &gt; for each ID corresponding to an item type (existing in classes of package itemhandlers) sets as key of the Map.
	 * @param handler (IItemHandler)
	 */
	@Override
	public void registerHandler(IItemHandler handler)
	{
		_datatable.put(handler.getClass().getSimpleName(), handler);
	}
	
	@Override
	public synchronized void removeHandler(IItemHandler handler)
	{
		_datatable.remove(handler.getClass().getSimpleName());
	}
	
	/**
	 * Returns the handler of the item
	 * @param item
	 * @return IItemHandler
	 */
	@Override
	public IItemHandler getHandler(EtcItem item)
	{
		if ((item == null) || (item.getHandlerName() == null))
		{
			return null;
		}
		return _datatable.get(item.getHandlerName());
	}
	
	/**
	 * Returns the number of elements contained in datatable
	 * @return int : Size of the datatable
	 */
	@Override
	public int size()
	{
		return _datatable.size();
	}
	
	/**
	 * Create ItemHandler if doesn't exist and returns ItemHandler
	 * @return ItemHandler
	 */
	public static ItemHandler getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ItemHandler INSTANCE = new ItemHandler();
	}
}
