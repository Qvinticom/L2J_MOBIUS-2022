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
package org.l2jmobius.gameserver.enums;

/**
 * @author Mobius
 */
public enum BroochJewel
{
	RUBY_LV1(38855, 17814, 0.01, true),
	RUBY_LV2(38856, 17814, 0.035, true),
	RUBY_LV3(38857, 17815, 0.075, true),
	RUBY_LV4(38858, 17816, 0.125, true),
	RUBY_LV5(38859, 17817, 0.2, true),
	GREATER_RUBY_LV1(47688, 18715, 0.2, true),
	GREATER_RUBY_LV2(48771, 18715, 0.2, true),
	GREATER_RUBY_LV3(48772, 18715, 0.2, true),
	GREATER_RUBY_LV4(48773, 18715, 0.2, true),
	GREATER_RUBY_LV5(48774, 18715, 0.2, true),
	SUPERIOR_RUBY_LV1(81505, 39658, 0.23, true),
	SUPERIOR_RUBY_LV2(81506, 39658, 0.23, true),
	SUPERIOR_RUBY_LV3(81507, 39658, 0.23, true),
	SUPERIOR_RUBY_LV4(81508, 39658, 0.23, true),
	SUPERIOR_RUBY_LV5(81509, 39658, 0.23, true),
	SUPERIOR_RUBY_LV6(81510, 39658, 0.23, true),
	SUPERIOR_RUBY_LV7(81511, 39658, 0.23, true),
	SUPERIOR_RUBY_LV8(81512, 39658, 0.23, true),
	SUPERIOR_RUBY_LV9(81513, 39658, 0.23, true),
	SUPERIOR_RUBY_LV10(81514, 39658, 0.23, true),
	SAPPHIRE_LV1(38927, 17818, 0.01, false),
	SAPPHIRE_LV2(38928, 17818, 0.035, false),
	SAPPHIRE_LV3(38929, 17819, 0.075, false),
	SAPPHIRE_LV4(38930, 17820, 0.125, false),
	SAPPHIRE_LV5(38931, 17821, 0.2, false),
	GREATER_SAPPHIRE_LV1(47689, 18718, 0.2, false),
	GREATER_SAPPHIRE_LV2(48775, 18718, 0.2, false),
	GREATER_SAPPHIRE_LV3(48776, 18718, 0.2, false),
	GREATER_SAPPHIRE_LV4(48777, 18718, 0.2, false),
	GREATER_SAPPHIRE_LV5(48778, 18718, 0.2, false),
	SUPERIOR_SAPPHIRE_LV1(81515, 39660, 0.23, false),
	SUPERIOR_SAPPHIRE_LV2(81516, 39660, 0.23, false),
	SUPERIOR_SAPPHIRE_LV3(81517, 39660, 0.23, false),
	SUPERIOR_SAPPHIRE_LV4(81518, 39660, 0.23, false),
	SUPERIOR_SAPPHIRE_LV5(81519, 39660, 0.23, false),
	SUPERIOR_SAPPHIRE_LV6(81520, 39660, 0.23, false),
	SUPERIOR_SAPPHIRE_LV7(81521, 39660, 0.23, false),
	SUPERIOR_SAPPHIRE_LV8(81522, 39660, 0.23, false),
	SUPERIOR_SAPPHIRE_LV9(81523, 39660, 0.23, false),
	SUPERIOR_SAPPHIRE_LV10(81524, 39660, 0.23, false);
	
	private int _itemId;
	private int _effectId;
	private double _bonus;
	private boolean _isRuby; // If not, it is sapphire.
	
	private BroochJewel(int itemId, int effectId, double bonus, boolean isRuby)
	{
		_itemId = itemId;
		_effectId = effectId;
		_bonus = bonus;
		_isRuby = isRuby;
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public int getEffectId()
	{
		return _effectId;
	}
	
	public double getBonus()
	{
		return _bonus;
	}
	
	public boolean isRuby()
	{
		return _isRuby;
	}
}
