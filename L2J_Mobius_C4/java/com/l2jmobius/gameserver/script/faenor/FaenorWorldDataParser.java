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
package com.l2jmobius.gameserver.script.faenor;

import java.util.Map;
import java.util.logging.Logger;

import javax.script.ScriptContext;

import org.w3c.dom.Node;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.script.IntList;
import com.l2jmobius.gameserver.script.Parser;
import com.l2jmobius.gameserver.script.ParserFactory;
import com.l2jmobius.gameserver.script.ScriptEngine;

import javolution.util.FastMap;

/**
 * @author Luis Arias
 */
public class FaenorWorldDataParser extends FaenorParser
{
	static Logger _log = Logger.getLogger(FaenorWorldDataParser.class.getName());
	// Script Types
	private final static String PET_DATA = "PetData";
	
	@Override
	public void parseScript(Node eventNode, ScriptContext context)
	{
		if (Config.DEBUG)
		{
			System.out.println("Parsing WorldData");
		}
		
		for (Node node = eventNode.getFirstChild(); node != null; node = node.getNextSibling())
		{
			if (isNodeName(node, PET_DATA))
			{
				parsePetData(node, context);
			}
		}
	}
	
	public class PetData
	{
		public int petID;
		public int levelStart;
		public int levelEnd;
		Map<String, String> statValues;
		
		public PetData()
		{
			statValues = new FastMap<>();
		}
	}
	
	private void parsePetData(Node petNode, ScriptContext context)
	{
		final PetData petData = new PetData();
		
		try
		{
			petData.petID = getInt(attribute(petNode, "ID"));
			final int[] levelRange = IntList.parse(attribute(petNode, "Levels"));
			petData.levelStart = levelRange[0];
			petData.levelEnd = levelRange[1];
			
			for (Node node = petNode.getFirstChild(); node != null; node = node.getNextSibling())
			{
				if (isNodeName(node, "Stat"))
				{
					parseStat(node, petData);
				}
			}
			bridge.addPetData(context, petData.petID, petData.levelStart, petData.levelEnd, petData.statValues);
		}
		catch (final Exception e)
		{
			petData.petID = -1;
			_log.warning("Error in pet Data parser.");
			e.printStackTrace();
		}
	}
	
	private void parseStat(Node stat, PetData petData)
	{
		// if (Config.DEBUG) System.out.println("Parsing Pet Statistic.");
		
		try
		{
			final String statName = attribute(stat, "Name");
			
			for (Node node = stat.getFirstChild(); node != null; node = node.getNextSibling())
			{
				if (isNodeName(node, "Formula"))
				{
					final String formula = parseForumla(node);
					petData.statValues.put(statName, formula);
				}
			}
		}
		catch (final Exception e)
		{
			petData.petID = -1;
			System.err.println("ERROR(parseStat):" + e.getMessage());
		}
	}
	
	private String parseForumla(Node formulaNode)
	{
		return formulaNode.getTextContent().trim();
	}
	
	static class FaenorWorldDataParserFactory extends ParserFactory
	{
		@Override
		public Parser create()
		{
			return (new FaenorWorldDataParser());
		}
	}
	
	static
	{
		ScriptEngine.parserFactories.put(getParserName("WorldData"), new FaenorWorldDataParserFactory());
	}
}
