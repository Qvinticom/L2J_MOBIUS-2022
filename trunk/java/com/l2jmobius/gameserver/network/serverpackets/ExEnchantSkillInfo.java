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

import java.util.LinkedList;
import java.util.List;

import com.l2jmobius.commons.network.PacketWriter;
import com.l2jmobius.gameserver.data.xml.impl.EnchantSkillGroupsData;
import com.l2jmobius.gameserver.data.xml.impl.SkillData;
import com.l2jmobius.gameserver.model.L2EnchantSkillGroup.EnchantSkillHolder;
import com.l2jmobius.gameserver.model.L2EnchantSkillLearn;
import com.l2jmobius.gameserver.network.client.OutgoingPackets;

public final class ExEnchantSkillInfo implements IClientOutgoingPacket
{
	private final List<Integer> _routes = new LinkedList<>(); // skill lvls for each route
	
	private final int _id;
	private final int _lvl;
	private final int _maxlvl;
	private boolean _maxEnchanted = false;
	
	public ExEnchantSkillInfo(int id, int lvl)
	{
		_id = id;
		_lvl = lvl;
		_maxlvl = SkillData.getInstance().getMaxLevel(_id);
		
		final L2EnchantSkillLearn enchantLearn = EnchantSkillGroupsData.getInstance().getSkillEnchantmentBySkillId(_id);
		// do we have this skill?
		if (enchantLearn != null)
		{
			// skill already enchanted?
			if (_lvl > 1000)
			{
				_maxEnchanted = enchantLearn.isMaxEnchant(_lvl);
				
				// get detail for next level
				final EnchantSkillHolder esd = enchantLearn.getEnchantSkillHolder(_lvl);
				
				// if it exists add it
				if ((esd != null) && !_maxEnchanted)
				{
					_routes.add(_lvl + 1); // current enchant add firts
				}
				
				for (int route : enchantLearn.getAllRoutes())
				{
					if (((route * 1000) + (_lvl % 1000)) == _lvl)
					{
						continue;
					}
					_routes.add((route * 1000) + (_lvl % 1000));
				}
			}
			else
			// not already enchanted
			{
				for (int route : enchantLearn.getAllRoutes())
				{
					// add first level (+1) of all routes
					_routes.add((route * 1000) + 1);
				}
			}
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_ENCHANT_SKILL_INFO.writeId(packet);
		
		packet.writeD(_id);
		if (_lvl < 100)
		{
			packet.writeD(_lvl);
		}
		else
		{
			packet.writeH(_maxlvl);
			packet.writeH(_lvl);
		}
		packet.writeD(_maxEnchanted ? 0 : 1);
		packet.writeD(_lvl > 1000 ? 1 : 0); // enchanted?
		packet.writeD(_routes.size());
		
		for (int level : _routes)
		{
			packet.writeH(_maxlvl);
			packet.writeH(level);
		}
		
		return true;
	}
}
