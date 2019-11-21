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

import org.l2jmobius.gameserver.model.DropData;
import org.l2jmobius.gameserver.templates.Npc;

public class NpcTable
{
	private static Logger _log = Logger.getLogger(NpcTable.class.getName());
	private static NpcTable _instance;
	private final Map<Integer, Npc> _npcs = new HashMap<>();
	private boolean _initialized = true;
	
	public static NpcTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new NpcTable();
		}
		return _instance;
	}
	
	private NpcTable()
	{
		parseData();
		parseAdditionalData();
		parseDropData();
	}
	
	public boolean isInitialized()
	{
		return _initialized;
	}
	
	private void parseData()
	{
		try
		{
			File npcData = new File("data/npc.csv");
			LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(npcData)));
			String line = null;
			while ((line = lnr.readLine()) != null)
			{
				if (line.trim().isEmpty() || line.startsWith("#"))
				{
					continue;
				}
				Npc npc = parseList(line);
				_npcs.put(npc.getNpcId(), npc);
			}
			lnr.close();
			_log.config("Loaded " + _npcs.size() + " NPC templates.");
		}
		catch (FileNotFoundException e)
		{
			_initialized = false;
			_log.warning("npc.csv is missing in data folder.");
		}
		catch (Exception e)
		{
			_initialized = false;
			_log.warning("Error while creating npc table " + e);
		}
	}
	
	private Npc parseList(String line)
	{
		StringTokenizer st = new StringTokenizer(line, ";");
		Npc npc = new Npc();
		int id = Integer.parseInt(st.nextToken());
		if (id > 1000000)
		{
			id -= 1000000;
		}
		npc.setNpcId(id);
		npc.setName(st.nextToken());
		npc.setType(st.nextToken());
		npc.setRadius(Double.parseDouble(st.nextToken()));
		npc.setHeight(Double.parseDouble(st.nextToken()));
		return npc;
	}
	
	private void parseAdditionalData()
	{
		try
		{
			File npcData2 = new File("data/npc2.csv");
			LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(npcData2)));
			String line = null;
			while ((line = lnr.readLine()) != null)
			{
				if (line.trim().isEmpty() || line.startsWith("#"))
				{
					continue;
				}
				try
				{
					parseAdditionalDataLine(line);
				}
				catch (Exception e)
				{
					_log.warning("Parsing error in npc2.csv, line " + lnr.getLineNumber() + " / " + e.toString());
				}
			}
			lnr.close();
		}
		catch (FileNotFoundException e)
		{
			_log.warning("npc2.csv is missing in data folder.");
		}
		catch (Exception e)
		{
			_log.warning("Error while creating npc data table " + e);
		}
	}
	
	private void parseAdditionalDataLine(String line)
	{
		StringTokenizer st = new StringTokenizer(line, ";");
		int id = Integer.parseInt(st.nextToken());
		Npc npcDat = _npcs.get(id);
		if (npcDat == null)
		{
			_log.warning("Missing npc template id:" + id);
			return;
		}
		st.nextToken();
		npcDat.setLevel(Integer.parseInt(st.nextToken()));
		npcDat.setSex(st.nextToken());
		npcDat.setType(st.nextToken());
		npcDat.setAttackRange(Integer.parseInt(st.nextToken()));
		npcDat.setHp(Integer.parseInt(st.nextToken()));
		npcDat.setMp(Integer.parseInt(st.nextToken()));
		npcDat.setExp(Integer.parseInt(st.nextToken()));
		npcDat.setSp(Integer.parseInt(st.nextToken()));
		npcDat.setPatk(Integer.parseInt(st.nextToken()));
		npcDat.setPdef(Integer.parseInt(st.nextToken()));
		npcDat.setMatk(Integer.parseInt(st.nextToken()));
		npcDat.setMdef(Integer.parseInt(st.nextToken()));
		npcDat.setAtkspd(Integer.parseInt(st.nextToken()));
		npcDat.setAgro(Integer.parseInt(st.nextToken()) == 1);
		npcDat.setMatkspd(Integer.parseInt(st.nextToken()));
		npcDat.setRhand(Integer.parseInt(st.nextToken()));
		npcDat.setLhand(Integer.parseInt(st.nextToken()));
		npcDat.setArmor(Integer.parseInt(st.nextToken()));
		npcDat.setWalkSpeed(Integer.parseInt(st.nextToken()));
		npcDat.setRunSpeed(Integer.parseInt(st.nextToken()));
	}
	
	private void parseDropData()
	{
		try
		{
			File dropData = new File("data/droplist.csv");
			LineNumberReader lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(dropData)));
			String line = null;
			int n = 0;
			while ((line = lnr.readLine()) != null)
			{
				if (line.trim().isEmpty() || line.startsWith("#"))
				{
					continue;
				}
				try
				{
					parseDropLine(line);
					++n;
				}
				catch (Exception e)
				{
					_log.warning("Parsing error in droplist.csv, line " + lnr.getLineNumber() + " / " + e.toString());
				}
			}
			_log.config("Loaded " + n + " drop data templates.");
			lnr.close();
		}
		catch (FileNotFoundException e)
		{
			_log.warning("droplist.csv is missing in data folder.");
		}
		catch (Exception e)
		{
			_log.warning("Error while creating drop data table " + e);
		}
	}
	
	private void parseDropLine(String line)
	{
		StringTokenizer st = new StringTokenizer(line, ";");
		int mobId = Integer.parseInt(st.nextToken());
		Npc npc = _npcs.get(mobId);
		if (npc == null)
		{
			_log.warning("Could not add drop data for npcid:" + mobId);
			return;
		}
		DropData dropDat = new DropData();
		dropDat.setItemId(Integer.parseInt(st.nextToken()));
		dropDat.setMinDrop(Integer.parseInt(st.nextToken()));
		dropDat.setMaxDrop(Integer.parseInt(st.nextToken()));
		dropDat.setSweep(Integer.parseInt(st.nextToken()) == 1);
		dropDat.setChance(Integer.parseInt(st.nextToken()));
		npc.addDropData(dropDat);
	}
	
	public Npc getTemplate(int id)
	{
		return _npcs.get(id);
	}
}
