/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.gameserver.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.model.TradeList;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;

public class TradeController
{
	private static Logger _log = Logger.getLogger(TradeController.class.getName());
	private static TradeController _instance;
	private final Map<Integer, TradeList> _lists = new HashMap<>();
	
	public static TradeController getInstance()
	{
		if (_instance == null)
		{
			_instance = new TradeController();
		}
		return _instance;
	}
	
	private TradeController()
	{
		String line = null;
		try
		{
			final File buylistData = new File("data/buylists.csv");
			final LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(buylistData)));
			while ((line = lnr.readLine()) != null)
			{
				if (line.trim().isEmpty() || line.startsWith("#"))
				{
					continue;
				}
				parseList(line);
			}
			lnr.close();
			_log.config("Loaded " + _lists.size() + " buylists.");
		}
		catch (FileNotFoundException e)
		{
			_log.warning("No buylists were found in data folder.");
		}
		catch (Exception e)
		{
			_log.warning("Error while creating buylists: " + e);
		}
	}
	
	private void parseList(String line)
	{
		final StringTokenizer st = new StringTokenizer(line, ";");
		final int listId = Integer.parseInt(st.nextToken());
		final TradeList buy1 = new TradeList(listId);
		while (st.hasMoreTokens())
		{
			final int itemId = Integer.parseInt(st.nextToken());
			final int price = Integer.parseInt(st.nextToken());
			final ItemInstance item = ItemTable.getInstance().createDummyItem(itemId);
			item.setPrice(price);
			buy1.addItem(item);
		}
		_lists.put(buy1.getListId(), buy1);
	}
	
	public TradeList getBuyList(int listId)
	{
		return _lists.get(listId);
	}
}
