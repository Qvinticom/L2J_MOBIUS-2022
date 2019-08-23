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
package org.l2jmobius.gameserver.templates.item;

import org.l2jmobius.gameserver.model.StatsSet;

public class Henna
{
	public int symbolId;
	public String symbolName;
	public int dye;
	public int price;
	public int amount;
	public int statINT;
	public int statSTR;
	public int statCON;
	public int statMEM;
	public int statDEX;
	public int statWIT;
	
	public Henna(StatsSet set)
	{
		symbolId = set.getInt("symbol_id");
		symbolName = ""; // set.getString("symbol_name");
		dye = set.getInt("dye");
		price = set.getInt("price");
		amount = set.getInt("amount");
		statINT = set.getInt("stat_INT");
		statSTR = set.getInt("stat_STR");
		statCON = set.getInt("stat_CON");
		statMEM = set.getInt("stat_MEM");
		statDEX = set.getInt("stat_DEX");
		statWIT = set.getInt("stat_WIT");
	}
	
	public int getSymbolId()
	{
		return symbolId;
	}
	
	public int getDyeId()
	{
		return dye;
	}
	
	public int getPrice()
	{
		return price;
	}
	
	public int getAmountDyeRequire()
	{
		return amount;
	}
	
	public int getStatINT()
	{
		return statINT;
	}
	
	public int getStatSTR()
	{
		return statSTR;
	}
	
	public int getStatCON()
	{
		return statCON;
	}
	
	public int getStatMEM()
	{
		return statMEM;
	}
	
	public int getStatDEX()
	{
		return statDEX;
	}
	
	public int getStatWIT()
	{
		return statWIT;
	}
}
