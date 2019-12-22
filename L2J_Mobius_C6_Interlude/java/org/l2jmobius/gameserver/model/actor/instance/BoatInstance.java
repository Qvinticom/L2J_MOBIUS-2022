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
package org.l2jmobius.gameserver.model.actor.instance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.concurrent.ThreadPool;
import org.l2jmobius.gameserver.GameTimeController;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.knownlist.BoatKnownList;
import org.l2jmobius.gameserver.model.actor.templates.CreatureTemplate;
import org.l2jmobius.gameserver.model.items.Weapon;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.network.clientpackets.Say2;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.OnVehicleCheckLocation;
import org.l2jmobius.gameserver.network.serverpackets.PlaySound;
import org.l2jmobius.gameserver.network.serverpackets.VehicleDeparture;
import org.l2jmobius.gameserver.network.serverpackets.VehicleInfo;

/**
 * @author Maktakien
 */
public class BoatInstance extends Creature
{
	protected static final Logger LOGGER = Logger.getLogger(BoatInstance.class.getName());
	
	public float boatSpeed;
	
	private class BoatTrajet
	{
		private Map<Integer, BoatPoint> _path;
		
		public int idWaypoint1;
		public int idWTicket1;
		public int ntx1;
		public int nty1;
		public int ntz1;
		public int max;
		public String boatName;
		public String npc1;
		public String sysmess10_1;
		public String sysmess5_1;
		public String sysmess1_1;
		public String sysmessb_1;
		public String sysmess0_1;
		
		protected class BoatPoint
		{
			public int speed1;
			public int speed2;
			public int x;
			public int y;
			public int z;
			public int time;
		}
		
		/**
		 * @param pIdWaypoint1
		 * @param pIdWTicket1
		 * @param pNtx1
		 * @param pNty1
		 * @param pNtz1
		 * @param pNpc1
		 * @param pSysmess10
		 * @param pSysmess5
		 * @param pSysmess1
		 * @param pSysmess0
		 * @param pSysmessb
		 * @param pBoatname
		 */
		public BoatTrajet(int pIdWaypoint1, int pIdWTicket1, int pNtx1, int pNty1, int pNtz1, String pNpc1, String pSysmess10, String pSysmess5, String pSysmess1, String pSysmess0, String pSysmessb, String pBoatname)
		{
			idWaypoint1 = pIdWaypoint1;
			idWTicket1 = pIdWTicket1;
			ntx1 = pNtx1;
			nty1 = pNty1;
			ntz1 = pNtz1;
			npc1 = pNpc1;
			sysmess10_1 = pSysmess10;
			sysmess5_1 = pSysmess5;
			sysmess1_1 = pSysmess1;
			sysmessb_1 = pSysmessb;
			sysmess0_1 = pSysmess0;
			boatName = pBoatname;
			loadBoatPath();
		}
		
		public void parseLine(String line)
		{
			_path = new HashMap<>();
			final StringTokenizer st = new StringTokenizer(line, ";");
			st.nextToken();
			max = Integer.parseInt(st.nextToken());
			for (int i = 0; i < max; i++)
			{
				final BoatPoint bp = new BoatPoint();
				bp.speed1 = Integer.parseInt(st.nextToken());
				bp.speed2 = Integer.parseInt(st.nextToken());
				bp.x = Integer.parseInt(st.nextToken());
				bp.y = Integer.parseInt(st.nextToken());
				bp.z = Integer.parseInt(st.nextToken());
				bp.time = Integer.parseInt(st.nextToken());
				_path.put(i, bp);
			}
		}
		
		protected void loadBoatPath()
		{
			FileReader reader = null;
			BufferedReader buff = null;
			LineNumberReader lnr = null;
			
			try
			{
				final File boatpath = new File(Config.DATAPACK_ROOT, "data/csv/boatpath.csv");
				
				reader = new FileReader(boatpath);
				buff = new BufferedReader(reader);
				lnr = new LineNumberReader(buff);
				
				boolean token = false;
				String line = null;
				while ((line = lnr.readLine()) != null)
				{
					if ((line.trim().length() == 0) || !line.startsWith(idWaypoint1 + ";"))
					{
						continue;
					}
					parseLine(line);
					token = true;
					break;
				}
				if (!token)
				{
					LOGGER.warning("No path for boat " + boatName + " !!!");
				}
			}
			catch (FileNotFoundException e)
			{
				LOGGER.warning("boatpath.csv is missing in data folder " + e);
			}
			catch (Exception e)
			{
				LOGGER.warning("Error while creating boat table " + e);
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
						LOGGER.warning(e1.toString());
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
						LOGGER.warning(e1.toString());
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
						LOGGER.warning(e1.toString());
					}
				}
			}
		}
		
		public int state(int state, BoatInstance boat)
		{
			if (state < max)
			{
				final BoatPoint bp = _path.get(state);
				final double dx = boat.getX() - bp.x;
				final double dy = boat.getY() - bp.y;
				final double distance = Math.sqrt((dx * dx) + (dy * dy));
				final double cos = dx / distance;
				final double sin = dy / distance;
				
				boat.getPosition().setHeading((int) (Math.atan2(-sin, -cos) * 10430.378350470452724949566316381) + 32768);
				
				boat._vd = new VehicleDeparture(boat, bp.speed1, bp.speed2, bp.x, bp.y, bp.z);
				boatSpeed = bp.speed1;
				boat.moveToLocation(bp.x, bp.y, bp.z, (float) bp.speed1);
				final Collection<PlayerInstance> knownPlayers = boat.getKnownList().getKnownPlayers().values();
				if ((knownPlayers == null) || knownPlayers.isEmpty())
				{
					return bp.time;
				}
				for (PlayerInstance player : knownPlayers)
				{
					player.sendPacket(boat._vd);
				}
				if (bp.time == 0)
				{
					bp.time = 1;
				}
				
				return bp.time;
			}
			return 0;
		}
	}
	
	private final String _name;
	protected BoatTrajet _t1;
	protected BoatTrajet _t2;
	protected int _cycle = 0;
	protected VehicleDeparture _vd = null;
	private Map<Integer, PlayerInstance> _inboat;
	
	public BoatInstance(int objectId, CreatureTemplate template, String name)
	{
		super(objectId, template);
		super.setKnownList(new BoatKnownList(this));
		_name = name;
	}
	
	public void moveToLocation(int x, int y, int z, float speed)
	{
		final int curX = getX();
		final int curY = getY();
		
		// Calculate distance (dx,dy) between current position and destination
		final int dx = x - curX;
		final int dy = y - curY;
		final double distance = Math.sqrt((dx * dx) + (dy * dy));
		
		// Define movement angles needed
		// ^
		// | X (x,y)
		// | /
		// | /distance
		// | /
		// |/ angle
		// X ---------->
		// (curx,cury)
		
		final double cos = dx / distance;
		final double sin = dy / distance;
		// Create and Init a MoveData object
		final MoveData m = new MoveData();
		
		// Calculate and set the heading of the Creature
		getPosition().setHeading((int) (Math.atan2(-sin, -cos) * 10430.378350470452724949566316381) + 32768);
		
		m._xDestination = x;
		m._yDestination = y;
		m._zDestination = z; // this is what was requested from client
		m._heading = 0;
		m.onGeodataPathIndex = -1; // Initialize not on geodata path
		m._moveStartTime = GameTimeController.getGameTicks();
		
		// Set the Creature _move object to MoveData object
		_move = m;
		
		// Add the Creature to movingObjects of the GameTimeController
		// The GameTimeController manage objects movement
		GameTimeController.getInstance().registerMovingObject(this);
	}
	
	class BoatCaptain implements Runnable
	{
		private final int _state;
		private final BoatInstance _boat;
		
		public BoatCaptain(int state, BoatInstance instance)
		{
			_state = state;
			_boat = instance;
		}
		
		@Override
		public void run()
		{
			BoatCaptain bc;
			switch (_state)
			{
				case 1:
				{
					_boat.say(5);
					bc = new BoatCaptain(2, _boat);
					ThreadPool.schedule(bc, 240000);
					break;
				}
				case 2:
				{
					_boat.say(1);
					bc = new BoatCaptain(3, _boat);
					ThreadPool.schedule(bc, 40000);
					break;
				}
				case 3:
				{
					_boat.say(0);
					bc = new BoatCaptain(4, _boat);
					ThreadPool.schedule(bc, 20000);
					break;
				}
				case 4:
				{
					_boat.say(-1);
					_boat.begin();
					break;
				}
			}
		}
	}
	
	class Boatrun implements Runnable
	{
		private int _state;
		private final BoatInstance _boat;
		
		public Boatrun(int state, BoatInstance instance)
		{
			_state = state;
			_boat = instance;
		}
		
		@Override
		public void run()
		{
			_boat._vd = null;
			_boat.needOnVehicleCheckLocation = false;
			
			if (_boat._cycle == 1)
			{
				final int time = _boat._t1.state(_state, _boat);
				if (time > 0)
				{
					_state++;
					final Boatrun bc = new Boatrun(_state, _boat);
					ThreadPool.schedule(bc, time);
				}
				else if (time == 0)
				{
					_boat._cycle = 2;
					_boat.say(10);
					final BoatCaptain bc = new BoatCaptain(1, _boat);
					ThreadPool.schedule(bc, 300000);
				}
				else
				{
					_boat.needOnVehicleCheckLocation = true;
					_state++;
					_boat._runstate = _state;
				}
			}
			else if (_boat._cycle == 2)
			{
				final int time = _boat._t2.state(_state, _boat);
				if (time > 0)
				{
					_state++;
					final Boatrun bc = new Boatrun(_state, _boat);
					ThreadPool.schedule(bc, time);
				}
				else if (time == 0)
				{
					_boat._cycle = 1;
					_boat.say(10);
					final BoatCaptain bc = new BoatCaptain(1, _boat);
					ThreadPool.schedule(bc, 300000);
				}
				else
				{
					_boat.needOnVehicleCheckLocation = true;
					_state++;
					_boat._runstate = _state;
				}
			}
		}
	}
	
	public int _runstate = 0;
	
	public void evtArrived()
	{
		if (_runstate != 0)
		{
			final Boatrun bc = new Boatrun(_runstate, this);
			ThreadPool.schedule(bc, 10);
			_runstate = 0;
		}
	}
	
	public void sendVehicleDeparture(PlayerInstance player)
	{
		if (_vd != null)
		{
			player.sendPacket(_vd);
		}
	}
	
	public VehicleDeparture getVehicleDeparture()
	{
		return _vd;
	}
	
	public void beginCycle()
	{
		say(10);
		final BoatCaptain bc = new BoatCaptain(1, this);
		ThreadPool.schedule(bc, 300000);
	}
	
	private int lastx = -1;
	private int lasty = -1;
	protected boolean needOnVehicleCheckLocation = false;
	
	public void updatePeopleInTheBoat(int x, int y, int z)
	{
		if (_inboat != null)
		{
			boolean check = false;
			if ((lastx == -1) || (lasty == -1))
			{
				check = true;
				lastx = x;
				lasty = y;
			}
			else if ((((x - lastx) * (x - lastx)) + ((y - lasty) * (y - lasty))) > 2250000) // 1500 * 1500 = 2250000
			{
				check = true;
				lastx = x;
				lasty = y;
			}
			for (int i = 0; i < _inboat.size(); i++)
			{
				final PlayerInstance player = _inboat.get(i);
				if ((player != null) && player.isInBoat() && (player.getBoat() == this))
				{
					player.getPosition().setXYZ(x, y, z);
					player.revalidateZone(false);
				}
				
				if (check && needOnVehicleCheckLocation && (player != null))
				{
					player.sendPacket(new OnVehicleCheckLocation(this, x, y, z));
				}
			}
		}
	}
	
	public void begin()
	{
		if (_cycle == 1)
		{
			final Collection<PlayerInstance> knownPlayers = getKnownList().getKnownPlayers().values();
			if ((knownPlayers != null) && !knownPlayers.isEmpty())
			{
				_inboat = new HashMap<>();
				int i = 0;
				for (PlayerInstance player : knownPlayers)
				{
					if (player.isInBoat() && (player.getBoat() == this))
					{
						ItemInstance it;
						it = player.getInventory().getItemByItemId(_t1.idWTicket1);
						if ((it != null) && (it.getCount() >= 1))
						{
							player.getInventory().destroyItem("Boat", it.getObjectId(), 1, player, this);
							final InventoryUpdate iu = new InventoryUpdate();
							iu.addModifiedItem(it);
							player.sendPacket(iu);
							_inboat.put(i, player);
							i++;
						}
						else if ((it == null) && (_t1.idWTicket1 == 0))
						{
							_inboat.put(i, player);
							i++;
						}
						else
						{
							player.teleToLocation(_t1.ntx1, _t1.nty1, _t1.ntz1, false);
						}
					}
				}
			}
			final Boatrun bc = new Boatrun(0, this);
			ThreadPool.schedule(bc, 0);
		}
		else if (_cycle == 2)
		{
			final Collection<PlayerInstance> knownPlayers = getKnownList().getKnownPlayers().values();
			if ((knownPlayers != null) && !knownPlayers.isEmpty())
			{
				_inboat = new HashMap<>();
				int i = 0;
				for (PlayerInstance player : knownPlayers)
				{
					if (player.isInBoat() && (player.getBoat() == this))
					{
						ItemInstance it;
						it = player.getInventory().getItemByItemId(_t2.idWTicket1);
						if ((it != null) && (it.getCount() >= 1))
						{
							player.getInventory().destroyItem("Boat", it.getObjectId(), 1, player, this);
							final InventoryUpdate iu = new InventoryUpdate();
							iu.addModifiedItem(it);
							player.sendPacket(iu);
							_inboat.put(i, player);
							i++;
						}
						else if ((it == null) && (_t2.idWTicket1 == 0))
						{
							_inboat.put(i, player);
							i++;
						}
						else
						{
							player.teleToLocation(_t2.ntx1, _t2.nty1, _t2.ntz1, false);
						}
					}
				}
			}
			final Boatrun bc = new Boatrun(0, this);
			ThreadPool.schedule(bc, 0);
		}
	}
	
	public void say(int id)
	{
		final Collection<PlayerInstance> knownPlayers = getKnownList().getKnownPlayers().values();
		CreatureSay sm;
		PlaySound ps;
		switch (id)
		{
			case 10:
			{
				if (_cycle == 1)
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t1.npc1, _t1.sysmess10_1);
				}
				else
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t2.npc1, _t2.sysmess10_1);
				}
				ps = new PlaySound(0, "itemsound.ship_arrival_departure", 1, getObjectId(), getX(), getY(), getZ());
				if ((knownPlayers == null) || knownPlayers.isEmpty())
				{
					return;
				}
				for (PlayerInstance player : knownPlayers)
				{
					player.sendPacket(sm);
					player.sendPacket(ps);
					// Mobius: Fixes weird movement at the end of the path.
					player.sendPacket(new VehicleInfo(this));
				}
				break;
			}
			case 5:
			{
				if (_cycle == 1)
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t1.npc1, _t1.sysmess5_1);
				}
				else
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t2.npc1, _t2.sysmess5_1);
				}
				ps = new PlaySound(0, "itemsound.ship_5min", 1, getObjectId(), getX(), getY(), getZ());
				if ((knownPlayers == null) || knownPlayers.isEmpty())
				{
					return;
				}
				for (PlayerInstance player : knownPlayers)
				{
					player.sendPacket(sm);
					player.sendPacket(ps);
				}
				break;
			}
			case 1:
			{
				if (_cycle == 1)
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t1.npc1, _t1.sysmess1_1);
				}
				else
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t2.npc1, _t2.sysmess1_1);
				}
				ps = new PlaySound(0, "itemsound.ship_1min", 1, getObjectId(), getX(), getY(), getZ());
				if ((knownPlayers == null) || knownPlayers.isEmpty())
				{
					return;
				}
				for (PlayerInstance player : knownPlayers)
				{
					player.sendPacket(sm);
					player.sendPacket(ps);
				}
				break;
			}
			case 0:
			{
				if (_cycle == 1)
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t1.npc1, _t1.sysmess0_1);
				}
				else
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t2.npc1, _t2.sysmess0_1);
				}
				if ((knownPlayers == null) || knownPlayers.isEmpty())
				{
					return;
				}
				for (PlayerInstance player : knownPlayers)
				{
					player.sendPacket(sm);
				}
				break;
			}
			case -1:
			{
				if (_cycle == 1)
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t1.npc1, _t1.sysmessb_1);
				}
				else
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t2.npc1, _t2.sysmessb_1);
				}
				ps = new PlaySound(0, "itemsound.ship_arrival_departure", 1, getObjectId(), getX(), getY(), getZ());
				for (PlayerInstance player : knownPlayers)
				{
					player.sendPacket(sm);
					player.sendPacket(ps);
					// Mobius: Fixes weird movement at the end of the path.
					player.sendPacket(new VehicleInfo(this));
				}
				break;
			}
		}
	}
	
	public void spawn()
	{
		final Collection<PlayerInstance> knownPlayers = getKnownList().getKnownPlayers().values();
		_cycle = 1;
		beginCycle();
		if ((knownPlayers == null) || knownPlayers.isEmpty())
		{
			return;
		}
		final VehicleInfo vi = new VehicleInfo(this);
		for (PlayerInstance player : knownPlayers)
		{
			player.sendPacket(vi);
		}
	}
	
	/**
	 * @param idWaypoint1
	 * @param idWTicket1
	 * @param ntx1
	 * @param nty1
	 * @param ntz1
	 * @param idnpc1
	 * @param sysmess10
	 * @param sysmess5
	 * @param sysmess1
	 * @param sysmess0
	 * @param sysmessb
	 */
	public void setTrajet1(int idWaypoint1, int idWTicket1, int ntx1, int nty1, int ntz1, String idnpc1, String sysmess10, String sysmess5, String sysmess1, String sysmess0, String sysmessb)
	{
		_t1 = new BoatTrajet(idWaypoint1, idWTicket1, ntx1, nty1, ntz1, idnpc1, sysmess10, sysmess5, sysmess1, sysmess0, sysmessb, _name);
	}
	
	public void setTrajet2(int idWaypoint1, int idWTicket1, int ntx1, int nty1, int ntz1, String idnpc1, String sysmess10, String sysmess5, String sysmess1, String sysmess0, String sysmessb)
	{
		_t2 = new BoatTrajet(idWaypoint1, idWTicket1, ntx1, nty1, ntz1, idnpc1, sysmess10, sysmess5, sysmess1, sysmess0, sysmessb, _name);
	}
	
	@Override
	public void updateAbnormalEffect()
	{
		// No effects.
	}
	
	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		return null;
	}
	
	@Override
	public Weapon getActiveWeaponItem()
	{
		return null;
	}
	
	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}
	
	@Override
	public Weapon getSecondaryWeaponItem()
	{
		return null;
	}
	
	@Override
	public int getLevel()
	{
		return 0;
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return false;
	}
	
	@Override
	public boolean isBoat()
	{
		return true;
	}
}
