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
package org.l2jmobius.gameserver.handler.custom;

import java.util.logging.Logger;

import org.l2jmobius.gameserver.handler.ICustomByPassHandler;
import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.handler.ItemHandler;
import org.l2jmobius.gameserver.handler.itemhandlers.ExtractableItems;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;

/**
 * @author Nick
 */
public class ExtractableByPassHandler implements ICustomByPassHandler
{
	protected static final Logger LOGGER = Logger.getLogger(ExtractableByPassHandler.class.getName());
	private static String[] _IDS =
	{
		"extractOne",
		"extractAll"
	};
	
	@Override
	public String[] getByPassCommands()
	{
		return _IDS;
	}
	
	@Override
	public void handleCommand(String command, PlayerInstance player, String parameters)
	{
		try
		{
			final int objId = Integer.parseInt(parameters);
			final ItemInstance item = player.getInventory().getItemByObjectId(objId);
			if (item == null)
			{
				return;
			}
			final IItemHandler ih = ItemHandler.getInstance().getItemHandler(item.getItemId());
			if ((ih == null) || !(ih instanceof ExtractableItems))
			{
				return;
			}
			if (command.compareTo(_IDS[0]) == 0)
			{
				((ExtractableItems) ih).doExtract(player, item, 1);
			}
			else if (command.compareTo(_IDS[1]) == 0)
			{
				((ExtractableItems) ih).doExtract(player, item, item.getCount());
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("ExtractableByPassHandler: Error while running " + e);
		}
	}
}
