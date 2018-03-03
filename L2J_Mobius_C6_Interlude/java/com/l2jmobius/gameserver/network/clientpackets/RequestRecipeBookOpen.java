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
import com.l2jmobius.gameserver.RecipeController;

public final class RequestRecipeBookOpen extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(RequestRecipeBookOpen.class.getName());
	
	private boolean _isDwarvenCraft;
	
	@Override
	protected void readImpl()
	{
		_isDwarvenCraft = readD() == 0;
		if (Config.DEBUG)
		{
			LOGGER.info("RequestRecipeBookOpen : " + (_isDwarvenCraft ? "dwarvenCraft" : "commonCraft"));
		}
	}
	
	@Override
	protected void runImpl()
	{
		if (getClient().getActiveChar() == null)
		{
			return;
		}
		
		if (getClient().getActiveChar().getPrivateStoreType() != 0)
		{
			getClient().getActiveChar().sendMessage("Cannot use recipe book while trading");
			return;
		}
		
		RecipeController.getInstance().requestBookOpen(getClient().getActiveChar(), _isDwarvenCraft);
	}
}
