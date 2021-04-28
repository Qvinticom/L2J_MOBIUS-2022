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
package org.l2jmobius.gameserver.model.holders;

/**
 * @author Mobius
 */
public class LimitShopProductHolder
{
	private final int _id;
	private final int _category;
	private final int _minLevel;
	private final int _maxLevel;
	private final int[] _ingredientIds;
	private final long[] _ingredientQuantities;
	private final int _productionId;
	private final long _count;
	private final float _chance;
	private final int _productionId2;
	private final long _count2;
	private final float _chance2;
	private final int _productionId3;
	private final long _count3;
	private final int _accountDailyLimit;
	private final int _accountBuyLimit;
	
	public LimitShopProductHolder(int id, int category, int minLevel, int maxLevel, int[] ingredientIds, long[] ingredientQuantities, int productionId, long count, float chance, int productionId2, long count2, float chance2, int productionId3, long count3, int accountDailyLimit, int accountBuyLimit)
	{
		_id = id;
		_category = category;
		_minLevel = minLevel;
		_maxLevel = maxLevel;
		_ingredientIds = ingredientIds;
		_ingredientQuantities = ingredientQuantities;
		_productionId = productionId;
		_count = count;
		_chance = chance;
		_productionId2 = productionId2;
		_count2 = count2;
		_chance2 = chance2;
		_productionId3 = productionId3;
		_count3 = count3;
		_accountDailyLimit = accountDailyLimit;
		_accountBuyLimit = accountBuyLimit;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public int getCategory()
	{
		return _category;
	}
	
	public int getMinLevel()
	{
		return _minLevel;
	}
	
	public int getMaxLevel()
	{
		return _maxLevel;
	}
	
	public int[] getIngredientIds()
	{
		return _ingredientIds;
	}
	
	public long[] getIngredientQuantities()
	{
		return _ingredientQuantities;
	}
	
	public int getProductionId()
	{
		return _productionId;
	}
	
	public long getCount()
	{
		return _count;
	}
	
	public float getChance()
	{
		return _chance;
	}
	
	public int getProductionId2()
	{
		return _productionId2;
	}
	
	public long getCount2()
	{
		return _count2;
	}
	
	public float getChance2()
	{
		return _chance2;
	}
	
	public int getProductionId3()
	{
		return _productionId3;
	}
	
	public long getCount3()
	{
		return _count3;
	}
	
	public int getAccountDailyLimit()
	{
		return _accountDailyLimit;
	}
	
	public int getAccountBuyLimit()
	{
		return _accountBuyLimit;
	}
}
