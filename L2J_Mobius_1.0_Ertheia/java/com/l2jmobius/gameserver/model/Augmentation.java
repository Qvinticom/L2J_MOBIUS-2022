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

import java.util.logging.Logger;

import com.l2jmobius.gameserver.data.xml.impl.OptionData;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.options.Options;

/**
 * Used to store an augmentation and its bonuses.
 * @author durgus, UnAfraid
 */
public final class Augmentation
{
	private static final Logger LOGGER = Logger.getLogger(Augmentation.class.getName());
	private final Options[] _options;
	private final int _id;
	
	public Augmentation(int id)
	{
		_id = id;
		final int[] stats = new int[2];
		stats[0] = 0x0000FFFF & id;
		stats[1] = (id >> 16);
		_options = new Options[stats.length];
		
		for (int i = 0; i < stats.length; i++)
		{
			final Options op = OptionData.getInstance().getOptions(stats[i]);
			if (op != null)
			{
				_options[i] = op;
			}
			else
			{
				LOGGER.warning(getClass().getSimpleName() + ": Couldn't find option: " + stats[i]);
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
	
	public Options[] getOptions()
	{
		return _options;
	}
	
	public int getOptionId(int index)
	{
		if ((index >= 0) && (index < _options.length) && (_options[index] != null))
		{
			return _options[index].getId();
		}
		return 0;
	}
	
	public void applyBonus(L2PcInstance player)
	{
		for (Options op : _options)
		{
			if (op != null)
			{
				op.apply(player);
			}
		}
	}
	
	public void removeBonus(L2PcInstance player)
	{
		for (Options op : _options)
		{
			if (op != null)
			{
				op.remove(player);
			}
		}
	}
}
