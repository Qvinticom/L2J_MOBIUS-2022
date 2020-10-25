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
public class LCoinShopProductHolder
{
	private final int _id;
	private final int _category;
	private final int[] _ingredientIds;
	private final long[] _ingredientQuantities;
	private final int _productionId;
	private final int _accountDailyLimit;
	
	public LCoinShopProductHolder(int id, int category, int[] ingredientIds, long[] ingredientQuantities, int productionId, int accountDailyLimit)
	{
		_id = id;
		_category = category;
		_ingredientIds = ingredientIds;
		_ingredientQuantities = ingredientQuantities;
		_productionId = productionId;
		_accountDailyLimit = accountDailyLimit;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public int getCategory()
	{
		return _category;
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
	
	public int getAccountDailyLimit()
	{
		return _accountDailyLimit;
	}
}
