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
package org.l2jmobius.gameserver.model.items.enchant;

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.items.type.EtcItemType;
import org.l2jmobius.gameserver.model.items.type.ItemType;

/**
 * @author UnAfraid
 */
public class EnchantSupportItem extends AbstractEnchantItem
{
	private final boolean _isWeapon;
	private final boolean _isBlessed;
	private final boolean _isDown;
	private final boolean _isGiant;
	private final ItemType type;
	
	public EnchantSupportItem(StatSet set)
	{
		super(set);
		type = getItem().getItemType();
		_isWeapon = (type == EtcItemType.ENCHT_ATTR_INC_PROP_ENCHT_WP) || (type == EtcItemType.ENCHT_WP_DOWN) || (type == EtcItemType.BLESSED_ENCHT_ATTR_INC_PROP_ENCHT_WP) || (type == EtcItemType.GIANT_ENCHT_ATTR_INC_PROP_ENCHT_WP) || (type == EtcItemType.BLESSED_GIANT_ENCHT_ATTR_INC_PROP_ENCHT_WP);
		_isBlessed = (type == EtcItemType.BLESSED_ENCHT_ATTR_INC_PROP_ENCHT_AM) || (type == EtcItemType.BLESSED_ENCHT_ATTR_INC_PROP_ENCHT_WP) || (type == EtcItemType.BLESSED_GIANT_ENCHT_ATTR_INC_PROP_ENCHT_AM) || (type == EtcItemType.BLESSED_GIANT_ENCHT_ATTR_INC_PROP_ENCHT_WP);
		_isDown = (type == EtcItemType.ENCHT_AM_DOWN) || (type == EtcItemType.ENCHT_WP_DOWN);
		_isGiant = (type == EtcItemType.GIANT_ENCHT_ATTR_INC_PROP_ENCHT_AM) || (type == EtcItemType.GIANT_ENCHT_ATTR_INC_PROP_ENCHT_WP) || (type == EtcItemType.BLESSED_GIANT_ENCHT_ATTR_INC_PROP_ENCHT_AM) || (type == EtcItemType.BLESSED_GIANT_ENCHT_ATTR_INC_PROP_ENCHT_WP);
	}
	
	@Override
	public boolean isWeapon()
	{
		return _isWeapon;
	}
	
	public boolean isBlessed()
	{
		return _isBlessed;
	}
	
	public boolean isDown()
	{
		return _isDown;
	}
	
	public boolean isGiant()
	{
		return _isGiant;
	}
}
