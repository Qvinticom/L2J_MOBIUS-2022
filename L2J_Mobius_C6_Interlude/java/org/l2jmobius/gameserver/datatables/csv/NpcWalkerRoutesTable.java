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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.model.NpcWalkerNode;

/**
 * Main Table to Load Npc Walkers Routes and Chat SQL Table.<br>
 * @author Rayan RPG for L2Emu Project
 * @since 927
 */
public class NpcWalkerRoutesTable
{
	protected static final Logger LOGGER = Logger.getLogger(NpcWalkerRoutesTable.class.getName());
	
	private List<NpcWalkerNode> _routes;
	
	private NpcWalkerRoutesTable()
	{
	}
	
	public void load()
	{
		_routes = new ArrayList<>();
		
		FileReader reader = null;
		BufferedReader buff = null;
		LineNumberReader lnr = null;
		
		try
		{
			final File fileData = new File(Config.DATAPACK_ROOT + "/data/csv/walker_routes.csv");
			
			reader = new FileReader(fileData);
			buff = new BufferedReader(reader);
			lnr = new LineNumberReader(buff);
			
			NpcWalkerNode route;
			String line = null;
			
			// format:
			// route_id;npc_id;move_point;chatText;move_x;move_y;move_z;delay;running
			while ((line = lnr.readLine()) != null)
			{
				// ignore comments
				if ((line.trim().length() == 0) || line.startsWith("#"))
				{
					continue;
				}
				route = new NpcWalkerNode();
				final StringTokenizer st = new StringTokenizer(line, ";");
				
				final int route_id = Integer.parseInt(st.nextToken());
				final int npc_id = Integer.parseInt(st.nextToken());
				final String move_point = st.nextToken();
				final String chatText = st.nextToken();
				final int move_x = Integer.parseInt(st.nextToken());
				final int move_y = Integer.parseInt(st.nextToken());
				final int move_z = Integer.parseInt(st.nextToken());
				final int delay = Integer.parseInt(st.nextToken());
				final boolean running = Boolean.parseBoolean(st.nextToken());
				
				route.setRouteId(route_id);
				route.setNpcId(npc_id);
				route.setMovePoint(move_point);
				route.setChatText(chatText);
				route.setMoveX(move_x);
				route.setMoveY(move_y);
				route.setMoveZ(move_z);
				route.setDelay(delay);
				route.setRunning(running);
				
				_routes.add(route);
			}
			
			LOGGER.info("WalkerRoutesTable: Loaded " + _routes.size() + " Npc Walker Routes.");
		}
		catch (FileNotFoundException e)
		{
			LOGGER.warning("walker_routes.csv is missing in data folder");
		}
		catch (IOException e0)
		{
			LOGGER.warning("Error while creating table: " + e0.getMessage() + "\n" + e0);
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
					LOGGER.warning("Problem with NpcWalkerRoutesTable: " + e1.getMessage());
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
					LOGGER.warning("Problem with NpcWalkerRoutesTable: " + e1.getMessage());
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
					LOGGER.warning("Problem with NpcWalkerRoutesTable: " + e1.getMessage());
				}
			}
		}
	}
	
	public List<NpcWalkerNode> getRouteForNpc(int id)
	{
		final List<NpcWalkerNode> result = new ArrayList<>();
		for (NpcWalkerNode node : _routes)
		{
			if (node.getNpcId() == id)
			{
				result.add(node);
			}
		}
		return result;
	}
	
	public static NpcWalkerRoutesTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final NpcWalkerRoutesTable INSTANCE = new NpcWalkerRoutesTable();
	}
}
