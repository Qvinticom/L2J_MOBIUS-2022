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

import java.util.Date;
import java.util.logging.Logger;

import javax.script.ScriptContext;

import org.w3c.dom.Node;

import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.script.DateRange;
import com.l2jmobius.gameserver.script.IntList;
import com.l2jmobius.gameserver.script.Parser;
import com.l2jmobius.gameserver.script.ParserFactory;
import com.l2jmobius.gameserver.script.ScriptEngine;

/**
 * @author Luis Arias
 */
public class FaenorEventParser extends FaenorParser
{
	static Logger _log = Logger.getLogger(FaenorEventParser.class.getName());
	private DateRange eventDates = null;
	
	@Override
	public void parseScript(final Node eventNode, ScriptContext context)
	{
		final String ID = attribute(eventNode, "ID");
		
		if (DEBUG)
		{
			_log.fine("Parsing Event \"" + ID + "\"");
		}
		
		eventDates = DateRange.parse(attribute(eventNode, "Active"), DATE_FORMAT);
		
		final Date currentDate = new Date();
		if (eventDates.getEndDate().before(currentDate))
		{
			_log.info("Event ID: (" + ID + ") has passed... Ignored.");
			return;
		}
		
		if (eventDates.getStartDate().after(currentDate))
		{
			_log.info("Event ID: (" + ID + ") is not active yet... Ignored.");
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					parseEventDropAndMessage(eventNode);
				}
			}, eventDates.getStartDate().getTime() - currentDate.getTime());
			return;
		}
		
		parseEventDropAndMessage(eventNode);
	}
	
	protected void parseEventDropAndMessage(Node eventNode)
	{
		for (Node node = eventNode.getFirstChild(); node != null; node = node.getNextSibling())
		{
			if (isNodeName(node, "DropList"))
			{
				parseEventDropList(node);
			}
			else if (isNodeName(node, "Message"))
			{
				parseEventMessage(node);
			}
		}
	}
	
	private void parseEventMessage(Node sysMsg)
	{
		if (DEBUG)
		{
			_log.fine("Parsing Event Message.");
		}
		
		try
		{
			final String type = attribute(sysMsg, "Type");
			final String[] message = attribute(sysMsg, "Msg").split("\n");
			
			if (type.equalsIgnoreCase("OnJoin"))
			{
				bridge.onPlayerLogin(message, eventDates);
			}
		}
		catch (final Exception e)
		{
			_log.warning("Error in event parser.");
			e.printStackTrace();
		}
	}
	
	private void parseEventDropList(Node dropList)
	{
		if (DEBUG)
		{
			_log.fine("Parsing Droplist.");
		}
		
		for (Node node = dropList.getFirstChild(); node != null; node = node.getNextSibling())
		{
			if (isNodeName(node, "AllDrop"))
			{
				parseEventDrop(node);
			}
		}
	}
	
	private void parseEventDrop(Node drop)
	{
		if (DEBUG)
		{
			_log.fine("Parsing Drop.");
		}
		
		try
		{
			final int[] items = IntList.parse(attribute(drop, "Items"));
			final int[] count = IntList.parse(attribute(drop, "Count"));
			final double chance = getPercent(attribute(drop, "Chance"));
			
			bridge.addEventDrop(items, count, chance, eventDates);
		}
		catch (final Exception e)
		{
			_log.warning("ERROR(parseEventDrop):" + e.getMessage());
		}
	}
	
	static class FaenorEventParserFactory extends ParserFactory
	{
		@Override
		public Parser create()
		{
			return (new FaenorEventParser());
		}
	}
	
	static
	{
		ScriptEngine.parserFactories.put(getParserName("Event"), new FaenorEventParserFactory());
	}
}