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
package com.l2jmobius.gameserver.network.serverpackets;

import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.RecipeController;
import com.l2jmobius.gameserver.model.L2RecipeList;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

/**
 * format dddd
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class RecipeItemMakeInfo extends L2GameServerPacket
{
	private static final String _S__D7_RECIPEITEMMAKEINFO = "[S] D7 RecipeItemMakeInfo";
	private static Logger _log = Logger.getLogger(RecipeItemMakeInfo.class.getName());
	
	private final int _id;
	private final L2PcInstance _player;
	private final boolean _success;
	
	public RecipeItemMakeInfo(int id, L2PcInstance player, boolean success)
	{
		_id = id;
		_player = player;
		_success = success;
	}
	
	public RecipeItemMakeInfo(int id, L2PcInstance player)
	{
		_id = id;
		_player = player;
		_success = true;
	}
	
	@Override
	protected final void writeImpl()
	{
		final L2RecipeList recipe = RecipeController.getInstance().getRecipeById(_id);
		
		if (recipe != null)
		{
			writeC(0xD7);
			
			writeD(_id);
			writeD(recipe.isDwarvenRecipe() ? 0 : 1); // 0 = Dwarven - 1 = Common
			writeD((int) _player.getCurrentMp());
			writeD(_player.getMaxMp());
			writeD(_success ? 1 : 0); // item creation success/failed
		}
		else if (Config.DEBUG)
		{
			_log.info("No recipe found with ID = " + _id);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.serverpackets.L2GameServerPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__D7_RECIPEITEMMAKEINFO;
	}
}