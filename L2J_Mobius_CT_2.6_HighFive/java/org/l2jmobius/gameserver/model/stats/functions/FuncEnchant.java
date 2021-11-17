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
package org.l2jmobius.gameserver.model.stats.functions;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.conditions.Condition;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.item.type.WeaponType;
import org.l2jmobius.gameserver.model.skills.Skill;
import org.l2jmobius.gameserver.model.stats.Stat;

public class FuncEnchant extends AbstractFunction
{
	public FuncEnchant(Stat stat, int order, Object owner, double value, Condition applayCond)
	{
		super(stat, order, owner, value, applayCond);
	}
	
	@Override
	public double calc(Creature effector, Creature effected, Skill skill, double initVal)
	{
		double value = initVal;
		if ((getApplayCond() != null) && !getApplayCond().test(effector, effected, skill))
		{
			return value;
		}
		
		final Item item = (Item) getFuncOwner();
		int enchant = item.getEnchantLevel();
		if (enchant <= 0)
		{
			return value;
		}
		
		int overenchant = 0;
		if (enchant > 3)
		{
			overenchant = enchant - 3;
			enchant = 3;
		}
		
		if (effector.isPlayer() && effector.getActingPlayer().isInOlympiadMode() && (Config.ALT_OLY_ENCHANT_LIMIT >= 0) && ((enchant + overenchant) > Config.ALT_OLY_ENCHANT_LIMIT))
		{
			if (Config.ALT_OLY_ENCHANT_LIMIT > 3)
			{
				overenchant = Config.ALT_OLY_ENCHANT_LIMIT - 3;
			}
			else
			{
				overenchant = 0;
				enchant = Config.ALT_OLY_ENCHANT_LIMIT;
			}
		}
		
		if ((getStat() == Stat.MAGIC_DEFENCE) || (getStat() == Stat.POWER_DEFENCE))
		{
			return value + enchant + (3 * overenchant);
		}
		
		if (getStat() == Stat.MAGIC_ATTACK)
		{
			switch (item.getItem().getCrystalTypePlus())
			{
				case S:
				{
					// M. Atk. increases by 4 for all weapons.
					// Starting at +4, M. Atk. bonus double.
					value += (4 * enchant) + (8 * overenchant);
					break;
				}
				case A:
				case B:
				case C:
				{
					// M. Atk. increases by 3 for all weapons.
					// Starting at +4, M. Atk. bonus double.
					value += (3 * enchant) + (6 * overenchant);
					break;
				}
				case D:
				case NONE:
				{
					// M. Atk. increases by 2 for all weapons. Starting at +4, M. Atk. bonus double.
					// Starting at +4, M. Atk. bonus double.
					value += (2 * enchant) + (4 * overenchant);
					break;
				}
			}
			return value;
		}
		
		if (item.isWeapon())
		{
			final WeaponType type = (WeaponType) item.getItemType();
			switch (item.getItem().getCrystalTypePlus())
			{
				case S:
				{
					if (item.getWeaponItem().getBodyPart() == ItemTemplate.SLOT_LR_HAND)
					{
						if (type == WeaponType.BOW)
						{
							// P. Atk. increases by 10 for bows.
							// Starting at +4, P. Atk. bonus double.
							value += (10 * enchant) + (20 * overenchant);
						}
						else if (type == WeaponType.CROSSBOW)
						{
							// P. Atk. increases by 7 for crossbows.
							// Starting at +4, P. Atk. bonus double.
							value += (7 * enchant) + (14 * overenchant);
						}
						else
						{
							// P. Atk. increases by 6 for two-handed swords, two-handed blunts, dualswords, and two-handed combat weapons.
							// Starting at +4, P. Atk. bonus double.
							value += (6 * enchant) + (12 * overenchant);
						}
					}
					else
					{
						// P. Atk. increases by 5 for one-handed swords, one-handed blunts, daggers, spears, and other weapons.
						// Starting at +4, P. Atk. bonus double.
						value += (5 * enchant) + (10 * overenchant);
					}
					break;
				}
				case A:
				{
					if (item.getWeaponItem().getBodyPart() == ItemTemplate.SLOT_LR_HAND)
					{
						if (type == WeaponType.BOW)
						{
							// P. Atk. increases by 8 for bows.
							// Starting at +4, P. Atk. bonus double.
							value += (8 * enchant) + (16 * overenchant);
						}
						else if (type == WeaponType.CROSSBOW)
						{
							// P. Atk. increases by 6 for crossbows.
							// Starting at +4, P. Atk. bonus double.
							value += (6 * enchant) + (12 * overenchant);
						}
						else
						{
							// P. Atk. increases by 5 for two-handed swords, two-handed blunts, dualswords, and two-handed combat weapons.
							// Starting at +4, P. Atk. bonus double.
							value += (5 * enchant) + (10 * overenchant);
						}
					}
					else
					{
						// P. Atk. increases by 4 for one-handed swords, one-handed blunts, daggers, spears, and other weapons.
						// Starting at +4, P. Atk. bonus double.
						value += (4 * enchant) + (8 * overenchant);
					}
					break;
				}
				case B:
				case C:
				{
					if (item.getWeaponItem().getBodyPart() == ItemTemplate.SLOT_LR_HAND)
					{
						if (type == WeaponType.BOW)
						{
							// P. Atk. increases by 6 for bows.
							// Starting at +4, P. Atk. bonus double.
							value += (6 * enchant) + (12 * overenchant);
						}
						else if (type == WeaponType.CROSSBOW)
						{
							// P. Atk. increases by 5 for crossbows.
							// Starting at +4, P. Atk. bonus double.
							value += (5 * enchant) + (10 * overenchant);
						}
						else
						{
							// P. Atk. increases by 4 for two-handed swords, two-handed blunts, dualswords, and two-handed combat weapons.
							// Starting at +4, P. Atk. bonus double.
							value += (4 * enchant) + (8 * overenchant);
						}
					}
					else
					{
						// P. Atk. increases by 3 for one-handed swords, one-handed blunts, daggers, spears, and other weapons.
						// Starting at +4, P. Atk. bonus double.
						value += (3 * enchant) + (6 * overenchant);
					}
					break;
				}
				case D:
				case NONE:
				{
					switch (type)
					{
						case BOW:
						{
							// Bows increase by 4.
							// Starting at +4, P. Atk. bonus double.
							value += (4 * enchant) + (8 * overenchant);
							break;
						}
						case CROSSBOW:
						{
							// Crossbows increase by 3.
							// Starting at +4, P. Atk. bonus double.
							value += (3 * enchant) + (6 * overenchant);
							break;
						}
						default:
						{
							// P. Atk. increases by 2 for all weapons with the exception of bows.
							// Starting at +4, P. Atk. bonus double.
							value += (2 * enchant) + (4 * overenchant);
							break;
						}
					}
					break;
				}
			}
		}
		return value;
	}
}
