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
package handlers.effecthandlers;

import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.effects.AbstractEffect;
import com.l2jmobius.gameserver.model.skills.BuffInfo;

/**
 * Reward Item on Exit effect implementation.
 * @author Mobius
 */
public final class RewardItemOnExit extends AbstractEffect
{
	private final int _itemId;
	private final long _itemCount;
	
	public RewardItemOnExit(StatsSet params)
	{
		_itemId = params.getInt("itemId", 40313); // Default item is Santa's Mark.
		_itemCount = params.getLong("itemCount", 1);
	}
	
	@Override
	public void onExit(BuffInfo info)
	{
		if (!info.isRemoved() && (info.getEffected() != null) && info.getEffected().isPlayer() && !info.getEffected().isDead())
		{
			ThreadPoolManager.execute(() -> // Make sure item is saved.
			{
				info.getEffected().getActingPlayer().addItem("RewardItemOnExitEffect", _itemId, _itemCount, info.getEffected(), true);
			});
		}
	}
}
