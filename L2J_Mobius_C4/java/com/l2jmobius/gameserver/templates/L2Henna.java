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
package com.l2jmobius.gameserver.templates;

/**
 * This class ...
 * @version $Revision$ $Date$
 */
public class L2Henna
{
	public final int symbol_id;
	public final String symbol_name;
	public final int dye;
	public final int price;
	public final int amount;
	public final int stat_INT;
	public final int stat_STR;
	public final int stat_CON;
	public final int stat_MEN;
	public final int stat_DEX;
	public final int stat_WIT;
	
	public L2Henna(StatsSet set)
	{
		
		symbol_id = set.getInteger("symbol_id");
		symbol_name = ""; // set.getString("symbol_name");
		dye = set.getInteger("dye");
		price = set.getInteger("price");
		amount = set.getInteger("amount");
		stat_INT = set.getInteger("stat_INT");
		stat_STR = set.getInteger("stat_STR");
		stat_CON = set.getInteger("stat_CON");
		stat_MEN = set.getInteger("stat_MEN");
		stat_DEX = set.getInteger("stat_DEX");
		stat_WIT = set.getInteger("stat_WIT");
		
	}
	
	public int getSymbolId()
	{
		return symbol_id;
	}
	
	/**
	 * @return
	 */
	public int getDyeId()
	{
		return dye;
	}
	
	/**
	 * @return
	 */
	public int getPrice()
	{
		return price;
	}
	
	/**
	 * @return
	 */
	public int getAmountDyeRequire()
	{
		return amount;
	}
	
	/**
	 * @return
	 */
	public int getStatINT()
	{
		return stat_INT;
	}
	
	/**
	 * @return
	 */
	public int getStatSTR()
	{
		return stat_STR;
	}
	
	/**
	 * @return
	 */
	public int getStatCON()
	{
		return stat_CON;
	}
	
	/**
	 * @return
	 */
	public int getStatMEN()
	{
		return stat_MEN;
	}
	
	/**
	 * @return
	 */
	public int getStatDEX()
	{
		return stat_DEX;
	}
	
	/**
	 * @return
	 */
	public int getStatWIT()
	{
		return stat_WIT;
	}
}
