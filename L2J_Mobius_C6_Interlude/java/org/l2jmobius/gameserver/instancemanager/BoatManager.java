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
package org.l2jmobius.gameserver.instancemanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.idfactory.IdFactory;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.instance.BoatInstance;
import org.l2jmobius.gameserver.model.actor.templates.CreatureTemplate;

public class BoatManager
{
	private static final Logger LOGGER = Logger.getLogger(BoatManager.class.getName());
	
	public static final BoatManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private Map<Integer, BoatInstance> _staticItems = new HashMap<>();
	
	public BoatManager()
	{
		LOGGER.info("Initializing BoatManager");
		load();
	}
	
	private final void load()
	{
		if (!Config.ALLOW_BOAT)
		{
			return;
		}
		
		FileReader reader = null;
		BufferedReader buff = null;
		LineNumberReader lnr = null;
		
		try
		{
			final File boatData = new File(Config.DATAPACK_ROOT, "data/csv/boat.csv");
			
			reader = new FileReader(boatData);
			buff = new BufferedReader(reader);
			lnr = new LineNumberReader(buff);
			
			String line = null;
			while ((line = lnr.readLine()) != null)
			{
				if ((line.trim().length() == 0) || line.startsWith("#"))
				{
					continue;
				}
				
				final BoatInstance boat = parseLine(line);
				boat.spawn();
				_staticItems.put(boat.getObjectId(), boat);
			}
		}
		catch (FileNotFoundException e)
		{
			LOGGER.warning("boat.csv is missing in data folder");
		}
		catch (Exception e)
		{
			LOGGER.warning("error while creating boat table " + e);
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
					LOGGER.warning("Problem with BoatManager " + e1.getMessage());
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
					LOGGER.warning("Problem with BoatManager " + e1.getMessage());
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
					LOGGER.warning("Problem with BoatManager " + e1.getMessage());
				}
			}
		}
	}
	
	private BoatInstance parseLine(String line)
	{
		BoatInstance boat;
		final StringTokenizer st = new StringTokenizer(line, ";");
		
		final String name = st.nextToken();
		final int id = Integer.parseInt(st.nextToken());
		final int xspawn = Integer.parseInt(st.nextToken());
		final int yspawn = Integer.parseInt(st.nextToken());
		final int zspawn = Integer.parseInt(st.nextToken());
		final int heading = Integer.parseInt(st.nextToken());
		
		final StatSet npcDat = new StatSet();
		npcDat.set("npcId", id);
		npcDat.set("level", 0);
		npcDat.set("jClass", "boat");
		
		npcDat.set("baseSTR", 0);
		npcDat.set("baseCON", 0);
		npcDat.set("baseDEX", 0);
		npcDat.set("baseINT", 0);
		npcDat.set("baseWIT", 0);
		npcDat.set("baseMEN", 0);
		
		npcDat.set("baseShldDef", 0);
		npcDat.set("baseShldRate", 0);
		npcDat.set("baseAccCombat", 38);
		npcDat.set("baseEvasRate", 38);
		npcDat.set("baseCritRate", 38);
		
		npcDat.set("collision_radius", 0);
		npcDat.set("collision_height", 0);
		npcDat.set("sex", "male");
		npcDat.set("type", "");
		npcDat.set("baseAtkRange", 0);
		npcDat.set("baseMpMax", 0);
		npcDat.set("baseCpMax", 0);
		npcDat.set("rewardExp", 0);
		npcDat.set("rewardSp", 0);
		npcDat.set("basePAtk", 0);
		npcDat.set("baseMAtk", 0);
		npcDat.set("basePAtkSpd", 0);
		npcDat.set("aggroRange", 0);
		npcDat.set("baseMAtkSpd", 0);
		npcDat.set("rhand", 0);
		npcDat.set("lhand", 0);
		npcDat.set("armor", 0);
		npcDat.set("baseWalkSpd", 0);
		npcDat.set("baseRunSpd", 0);
		npcDat.set("name", name);
		npcDat.set("baseHpMax", 50000);
		npcDat.set("baseHpReg", 3.e-3f);
		npcDat.set("baseMpReg", 3.e-3f);
		npcDat.set("basePDef", 100);
		npcDat.set("baseMDef", 100);
		final CreatureTemplate template = new CreatureTemplate(npcDat);
		boat = new BoatInstance(IdFactory.getInstance().getNextId(), template, name);
		boat.getPosition().setHeading(heading);
		boat.setXYZ(xspawn, yspawn, zspawn);
		
		int idWaypoint1 = Integer.parseInt(st.nextToken());
		int idWTicket1 = Integer.parseInt(st.nextToken());
		int ntx1 = Integer.parseInt(st.nextToken());
		int nty1 = Integer.parseInt(st.nextToken());
		int ntz1 = Integer.parseInt(st.nextToken());
		String npc1 = st.nextToken();
		String mess10 = st.nextToken();
		String mess5 = st.nextToken();
		String mess1 = st.nextToken();
		String mess0 = st.nextToken();
		String messb = st.nextToken();
		boat.setTrajet1(idWaypoint1, idWTicket1, ntx1, nty1, ntz1, npc1, mess10, mess5, mess1, mess0, messb);
		idWaypoint1 = Integer.parseInt(st.nextToken());
		idWTicket1 = Integer.parseInt(st.nextToken());
		ntx1 = Integer.parseInt(st.nextToken());
		nty1 = Integer.parseInt(st.nextToken());
		ntz1 = Integer.parseInt(st.nextToken());
		npc1 = st.nextToken();
		mess10 = st.nextToken();
		mess5 = st.nextToken();
		mess1 = st.nextToken();
		mess0 = st.nextToken();
		messb = st.nextToken();
		boat.setTrajet2(idWaypoint1, idWTicket1, ntx1, nty1, ntz1, npc1, mess10, mess5, mess1, mess0, messb);
		
		return boat;
	}
	
	public BoatInstance GetBoat(int boatId)
	{
		if (_staticItems == null)
		{
			_staticItems = new HashMap<>();
		}
		return _staticItems.get(boatId);
	}
	
	private static class SingletonHolder
	{
		protected static final BoatManager INSTANCE = new BoatManager();
	}
}
