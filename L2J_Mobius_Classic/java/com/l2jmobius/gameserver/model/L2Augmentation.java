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
package com.l2jmobius.gameserver.model;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.l2jmobius.gameserver.data.xml.impl.OptionData;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.options.Options;

/**
 * Used to store an augmentation and its bonuses.
 * @author durgus, UnAfraid
 */
public final class L2Augmentation
{
	private static final Logger LOGGER = Logger.getLogger(L2Augmentation.class.getName());
	private final List<Options> _options = new ArrayList<>();
	private boolean _active;
	private final int _id;
	
	public L2Augmentation(int id)
	{
		_id = id;
		_active = false;
		final int[] stats = new int[2];
		stats[0] = 0x0000FFFF & id;
		stats[1] = (id >> 16);
		
		for (int stat : stats)
		{
			final Options op = OptionData.getInstance().getOptions(stat);
			if (op != null)
			{
				_options.add(op);
			}
			else
			{
				LOGGER.warning(getClass().getSimpleName() + ": Couldn't find option: " + stat);
			}
		}
	}
	
	/**
	 * Get the augmentation "id" used in serverpackets.
	 * @return augmentationId
	 */
	public int getId()
	{
		return _id;
	}
	
	public List<Options> getOptions()
	{
		return _options;
	}
	
	public void applyBonus(L2PcInstance player)
	{
		// make sure the bonuses are not applied twice..
		if (_active)
		{
			return;
		}
		
		for (Options op : _options)
		{
			op.apply(player);
		}
		
		player.getStat().recalculateStats(true);
		_active = true;
	}
	
	public void removeBonus(L2PcInstance player)
	{
		// make sure the bonuses are not removed twice
		if (!_active)
		{
			return;
		}
		
		for (Options op : _options)
		{
			op.remove(player);
		}
		
		player.getStat().recalculateStats(true);
		_active = false;
	}
}
