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
package com.l2jmobius.gameserver.model.multisell;

import com.l2jmobius.gameserver.model.items.enchant.attribute.AttributeHolder;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;
import com.l2jmobius.gameserver.model.variables.ItemVariables;

/**
 * @author DS
 */
public class ItemInfo
{
	private final int _enchantLevel, _augmentId;
	private final byte _elementId;
	private final int _elementPower;
	private final AttributeHolder[] _attributes;
	private final int _visualId;
	private final int _visualStoneId;
	private final long _visualIdLifetime;
	
	public ItemInfo(L2ItemInstance item)
	{
		_enchantLevel = item.getEnchantLevel();
		_augmentId = item.getAugmentation() != null ? item.getAugmentation().getAugmentationId() : 0;
		_elementId = item.getAttackAttributeType().getClientId();
		_elementPower = item.getAttackAttributePower();
		_attributes = item.getAttributes() != null ? item.getAttributes().toArray(new AttributeHolder[6]) : new AttributeHolder[6];
		_visualId = item.getVisualId();
		_visualStoneId = item.getVariables().getInt(ItemVariables.VISUAL_APPEARANCE_STONE_ID, 0);
		_visualIdLifetime = item.getVisualLifeTime();
	}
	
	public final int getEnchantLevel()
	{
		return _enchantLevel;
	}
	
	public final int getAugmentId()
	{
		return _augmentId;
	}
	
	public final byte getElementId()
	{
		return _elementId;
	}
	
	public final int getElementPower()
	{
		return _elementPower;
	}
	
	public final AttributeHolder[] getElementals()
	{
		return _attributes;
	}
	
	public int getVisualId()
	{
		return _visualId;
	}
	
	public int getVisualStoneId()
	{
		return _visualStoneId;
	}
	
	public long getVisualIdLifeTime()
	{
		return _visualIdLifetime;
	}
}