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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
		File file = new File("data/pricelist.csv");
		if (file.exists())
		{
			try
			{
				readFromDisk(file);
			}
			catch (IOException e)
			{
			}
		}
		else
		{
			_log.config("data/pricelist.csv is missing!");
		}
	}
	
	private void readFromDisk(File file) throws IOException
	{
		BufferedReader lnr = null;
		int i = 0;
		String line = null;
		lnr = new LineNumberReader(new FileReader(file));
		while ((line = ((LineNumberReader) lnr).readLine()) != null)
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
		try
		{
			lnr.close();
			return;
		}
		catch (FileNotFoundException e)
		{
			try
			{
				lnr.close();
				return;
			}
			catch (IOException e1)
			{
				try
				{
					e1.printStackTrace();
				}
				catch (Throwable throwable)
				{
					try
					{
						lnr.close();
						throw throwable;
					}
					catch (Exception e2)
					{
						// empty catch block
					}
					throw throwable;
				}
				try
				{
					lnr.close();
					return;
				}
				catch (Exception e2)
				{
					return;
				}
			}
		}
	}
}
