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
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.model.TeleportLocation;

public class TeleportLocationTable
{
	private static Logger _log = Logger.getLogger(TeleportLocationTable.class.getName());
	private static TeleportLocationTable _instance;
	private final Map<Integer, TeleportLocation> _teleports = new HashMap<>();
	
	public static TeleportLocationTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new TeleportLocationTable();
		}
		return _instance;
	}
	
	private TeleportLocationTable()
	{
		try
		{
			final File teleData = new File("data/teleport.csv");
			if (teleData.isFile() && teleData.exists())
			{
				final LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(teleData)));
				String line = null;
				while ((line = lnr.readLine()) != null)
				{
					if (line.trim().isEmpty() || line.startsWith("#"))
					{
						continue;
					}
					final StringTokenizer st = new StringTokenizer(line, ";");
					final TeleportLocation teleport = new TeleportLocation();
					teleport.setTeleId(Integer.parseInt(st.nextToken()));
					teleport.setLocX(Integer.parseInt(st.nextToken()));
					teleport.setLocY(Integer.parseInt(st.nextToken()));
					teleport.setLocZ(Integer.parseInt(st.nextToken()));
					teleport.setPrice(Integer.parseInt(st.nextToken()));
					_teleports.put(teleport.getTeleId(), teleport);
				}
				lnr.close();
				_log.config("Loaded " + _teleports.size() + " Teleport templates.");
			}
			else
			{
				_log.warning("teleport.csv is missing in data folder.");
			}
		}
		catch (Exception e)
		{
			_log.warning("Error while creating teleport table " + e);
		}
	}
	
	public TeleportLocation getTemplate(int id)
	{
		return _teleports.get(id);
	}
}
