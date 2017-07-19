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
package com.l2jmobius.gameserver.skills.funcs;

import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.skills.Env;
import com.l2jmobius.gameserver.skills.Stats;
import com.l2jmobius.gameserver.templates.L2Item;
import com.l2jmobius.gameserver.templates.L2WeaponType;

public class FuncEnchant extends Func
{
	public FuncEnchant(Stats stat, int order, Object owner, Lambda lambda)
	{
		super(stat, order, owner);
	}
	
	@Override
	public void calc(Env env)
	{
		if ((_cond != null) && !_cond.test(env))
		{
			return;
		}
		
		final L2ItemInstance item = (L2ItemInstance) _funcOwner;
		
		int enchant = item.getEnchantLevel();
		if (enchant <= 0)
		{
			return;
		}
		
		int overenchant = 0;
		if (enchant > 3)
		{
			overenchant = enchant - 3;
			enchant = 3;
		}
		
		if ((_stat == Stats.MAGIC_DEFENCE) || (_stat == Stats.POWER_DEFENCE))
		{
			env.value += enchant + (3 * overenchant);
			return;
		}
		
		if (_stat == Stats.MAGIC_ATTACK)
		{
			switch (item.getItem().getCrystalType())
			{
				case L2Item.CRYSTAL_S:
					env.value += (4 * enchant) + (8 * overenchant);
					break;
				case L2Item.CRYSTAL_A:
					env.value += (3 * enchant) + (6 * overenchant);
					break;
				case L2Item.CRYSTAL_B:
					env.value += (3 * enchant) + (6 * overenchant);
					break;
				case L2Item.CRYSTAL_C:
					env.value += (3 * enchant) + (6 * overenchant);
					break;
				case L2Item.CRYSTAL_D:
					env.value += (2 * enchant) + (4 * overenchant);
					break;
			}
			return;
		}
		
		switch (item.getItem().getCrystalType())
		{
			case L2Item.CRYSTAL_S:
				if (item.getItemType() == L2WeaponType.BOW)
				{
					env.value += (10 * enchant) + (20 * overenchant);
				}
				else
				{
					env.value += (5 * enchant) + (10 * overenchant);
				}
				break;
			case L2Item.CRYSTAL_A:
				if (item.getItemType() == L2WeaponType.BOW)
				{
					env.value += (8 * enchant) + (16 * overenchant);
				}
				else
				{
					env.value += (4 * enchant) + (8 * overenchant);
				}
				break;
			case L2Item.CRYSTAL_B:
				if (item.getItemType() == L2WeaponType.BOW)
				{
					env.value += (6 * enchant) + (12 * overenchant);
				}
				else
				{
					env.value += (3 * enchant) + (6 * overenchant);
				}
				break;
			case L2Item.CRYSTAL_C:
				if (item.getItemType() == L2WeaponType.BOW)
				{
					env.value += (6 * enchant) + (12 * overenchant);
				}
				else
				{
					env.value += (3 * enchant) + (6 * overenchant);
				}
				break;
			case L2Item.CRYSTAL_D:
			case L2Item.CRYSTAL_NONE:
				if (item.getItemType() == L2WeaponType.BOW)
				{
					env.value += (4 * enchant) + (8 * overenchant);
				}
				else
				{
					env.value += (2 * enchant) + (4 * overenchant);
				}
				break;
		}
	}
}