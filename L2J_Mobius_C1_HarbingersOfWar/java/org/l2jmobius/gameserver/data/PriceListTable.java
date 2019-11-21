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
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.templates.L2Item;

public class PriceListTable
{
	private static Logger _log = Logger.getLogger(PriceListTable.class.getName());
	private static PriceListTable _instance;
	
	public PriceListTable()
	{
		loadPriceList();
	}
	
	public static PriceListTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new PriceListTable();
		}
		return _instance;
	}
	
	public void loadPriceList()
	{
		try
		{
			File file = new File("data/pricelist.csv");
			if (file.isFile() && file.exists())
			{
				int i = 0;
				String line = null;
				LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(file)));
				while ((line = lnr.readLine()) != null)
				{
					if (line.startsWith("#"))
					{
						continue;
					}
					StringTokenizer st = new StringTokenizer(line, ";");
					int itemId = Integer.parseInt(st.nextToken().toString());
					int price = Integer.parseInt(st.nextToken().toString());
					L2Item temp = ItemTable.getInstance().getTemplate(itemId);
					temp.setItemId(itemId);
					temp.setReferencePrice(price);
					++i;
				}
				_log.config("Loaded " + i + " prices.");
				lnr.close();
			}
			else
			{
				_log.config("data/pricelist.csv is missing!");
			}
		}
		catch (Exception e)
		{
			_log.warning("Error while loading price lists: " + e);
		}
	}
}
