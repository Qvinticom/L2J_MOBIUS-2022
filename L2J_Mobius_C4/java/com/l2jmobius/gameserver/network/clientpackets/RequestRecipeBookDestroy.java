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

import com.l2jmobius.gameserver.RecipeController;
import com.l2jmobius.gameserver.model.L2RecipeList;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.RecipeBookItemList;

public class RequestRecipeBookDestroy extends L2GameClientPacket
{
	private static final String _C__AC_REQUESTRECIPEBOOKDESTROY = "[C] AD RequestRecipeBookDestroy";
	// private static Logger _log = Logger.getLogger(RequestSellItem.class.getName());
	
	private int _RecipeID;
	
	@Override
	protected void readImpl()
	{
		_RecipeID = readD();
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar != null)
		{
			if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("RecipeDestroy"))
			{
				return;
			}
			
			final L2RecipeList rp = RecipeController.getInstance().getRecipeList(_RecipeID - 1);
			if (rp == null)
			{
				return;
			}
			
			activeChar.unregisterRecipeList(_RecipeID);
			
			final RecipeBookItemList response = new RecipeBookItemList(rp.isDwarvenRecipe(), activeChar.getMaxMp());
			if (rp.isDwarvenRecipe())
			{
				response.addRecipes(activeChar.getDwarvenRecipeBook());
			}
			else
			{
				response.addRecipes(activeChar.getCommonRecipeBook());
			}
			
			activeChar.sendPacket(response);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__AC_REQUESTRECIPEBOOKDESTROY;
	}
}