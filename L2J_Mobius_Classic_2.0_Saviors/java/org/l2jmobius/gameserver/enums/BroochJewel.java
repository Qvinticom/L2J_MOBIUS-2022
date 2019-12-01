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
 * @implNote Update by RobikBobik
 */
public enum BroochJewel
{
	// Working effect
	RUBY_LV1(70451, 17817, 0.02, true),
	RUBY_LV2(70452, 17817, 0.03, true),
	RUBY_LV3(70453, 17817, 0.05, true),
	RUBY_LV4(70454, 17817, 0.08, true),
	RUBY_LV5(70455, 17817, 0.16, true),
	GREATER_RUBY_LV1(71368, 17817, 0.17, true), // + 1% p atk
	GREATER_RUBY_LV2(71369, 17817, 0.18, true), // + 2% p atk
	GREATER_RUBY_LV3(71370, 17817, 0.19, true), // + 3% p atk
	GREATER_RUBY_LV4(71371, 17817, 0.20, true), // + 5% p atk
	GREATER_RUBY_LV5(71372, 17817, 0.20, true), // + 7% p atk + crit. p. rate +10%
	
	// Working effect
	SHAPPHIRE_LV1(70456, 17821, 0.02, false),
	SHAPPHIRE_LV2(70457, 17821, 0.03, false),
	SHAPPHIRE_LV3(70458, 17821, 0.05, false),
	SHAPPHIRE_LV4(70459, 17821, 0.08, false),
	SHAPPHIRE_LV5(70460, 17821, 0.16, false),
	GREATER_SHAPPHIRE_LV1(71373, 17821, 00.17, false), // + 2 % m attack
	GREATER_SHAPPHIRE_LV2(71374, 17821, 00.18, false), // + 4 % m attack
	GREATER_SHAPPHIRE_LV3(71375, 17821, 00.19, false), // + 6 % m attack
	GREATER_SHAPPHIRE_LV4(71376, 17821, 00.20, false), // + 10 % m attack
	GREATER_SHAPPHIRE_LV5(71377, 17821, 00.20, false); // + 14 % m attack + crit. m. rate +10%
	
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
