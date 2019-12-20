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
package org.l2jmobius.gameserver.datatables.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.RecipeController;
import org.l2jmobius.gameserver.model.RecipeList;
import org.l2jmobius.gameserver.model.actor.instance.RecipeInstance;

/**
 * @author programmos
 */
public class RecipeTable extends RecipeController
{
	private static final Logger LOGGER = Logger.getLogger(RecipeTable.class.getName());
	
	private final Map<Integer, RecipeList> _lists;
	
	private RecipeTable()
	{
		_lists = new HashMap<>();
		String line = null;
		
		FileReader reader = null;
		BufferedReader buff = null;
		LineNumberReader lnr = null;
		
		try
		{
			final File recipesData = new File(Config.DATAPACK_ROOT, "data/csv/recipes.csv");
			
			reader = new FileReader(recipesData);
			buff = new BufferedReader(reader);
			lnr = new LineNumberReader(buff);
			
			while ((line = lnr.readLine()) != null)
			{
				if ((line.trim().length() == 0) || line.startsWith("#"))
				{
					continue;
				}
				
				parseList(line);
			}
			LOGGER.info("RecipeController: Loaded " + _lists.size() + " Recipes.");
		}
		catch (Exception e)
		{
			if (lnr != null)
			{
				LOGGER.warning("error while creating recipe controller in linenr: " + lnr.getLineNumber() + " " + e);
			}
			else
			{
				LOGGER.warning("No recipes were found in data folder");
			}
		}
		finally
		{
			if (lnr != null)
			{
				try
				{
					lnr.close();
				}
				catch (Exception e1)
				{
					LOGGER.warning("Problem with RecipeTable: " + e1.getMessage());
				}
			}
			
			if (buff != null)
			{
				try
				{
					buff.close();
				}
				catch (Exception e1)
				{
					LOGGER.warning("Problem with RecipeTable: " + e1.getMessage());
				}
			}
			
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (Exception e1)
				{
					LOGGER.warning("Problem with RecipeTable: " + e1.getMessage());
				}
			}
		}
	}
	
	private void parseList(String line)
	{
		try
		{
			StringTokenizer st = new StringTokenizer(line, ";");
			final List<RecipeInstance> recipePartList = new ArrayList<>();
			
			// we use common/dwarf for easy reading of the recipes.csv file
			String recipeTypeString = st.nextToken();
			
			// now parse the string into a boolean
			boolean isDwarvenRecipe;
			
			if (recipeTypeString.equalsIgnoreCase("dwarven"))
			{
				isDwarvenRecipe = true;
			}
			else if (recipeTypeString.equalsIgnoreCase("common"))
			{
				isDwarvenRecipe = false;
			}
			else
			{ // prints a helpfull message
				LOGGER.warning("Error parsing recipes.csv, unknown recipe type " + recipeTypeString);
				return;
			}
			
			String recipeName = st.nextToken();
			final int id = Integer.parseInt(st.nextToken());
			final int recipeId = Integer.parseInt(st.nextToken());
			final int level = Integer.parseInt(st.nextToken());
			
			// material
			StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");
			while (st2.hasMoreTokens())
			{
				StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
				final int rpItemId = Integer.parseInt(st3.nextToken());
				final int quantity = Integer.parseInt(st3.nextToken());
				RecipeInstance rp = new RecipeInstance(rpItemId, quantity);
				recipePartList.add(rp);
			}
			
			final int itemId = Integer.parseInt(st.nextToken());
			final int count = Integer.parseInt(st.nextToken());
			
			// npc fee
			/* String notdoneyet = */st.nextToken();
			
			final int mpCost = Integer.parseInt(st.nextToken());
			final int successRate = Integer.parseInt(st.nextToken());
			
			RecipeList recipeList = new RecipeList(id, level, recipeId, recipeName, successRate, mpCost, itemId, count, isDwarvenRecipe);
			
			for (RecipeInstance recipePart : recipePartList)
			{
				recipeList.addRecipe(recipePart);
			}
			_lists.put(_lists.size(), recipeList);
		}
		catch (Exception e)
		{
			LOGGER.warning("Exception in RecipeController.parseList() " + e);
		}
	}
	
	public int getRecipesCount()
	{
		return _lists.size();
	}
	
	public RecipeList getRecipeList(int listId)
	{
		return _lists.get(listId);
	}
	
	public RecipeList getRecipeByItemId(int itemId)
	{
		for (int i = 0; i < _lists.size(); i++)
		{
			final RecipeList find = _lists.get(i);
			if (find.getRecipeId() == itemId)
			{
				return find;
			}
		}
		return null;
	}
	
	public RecipeList getRecipeById(int recId)
	{
		for (int i = 0; i < _lists.size(); i++)
		{
			final RecipeList find = _lists.get(i);
			if (find.getId() == recId)
			{
				return find;
			}
		}
		return null;
	}
	
	public static RecipeTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final RecipeTable INSTANCE = new RecipeTable();
	}
}
