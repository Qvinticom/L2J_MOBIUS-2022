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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.l2jmobius.commons.concurrent.ThreadPool;
import org.l2jmobius.gameserver.GameTimeController;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.knownlist.BoatKnownList;
import org.l2jmobius.gameserver.model.actor.templates.CreatureTemplate;
import org.l2jmobius.gameserver.model.holders.BoatPathHolder;
import org.l2jmobius.gameserver.model.holders.BoatPathHolder.BoatPoint;
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
	public float boatSpeed;
	
	private final String _name;
	protected BoatPathHolder _t1;
	protected BoatPathHolder _t2;
	protected int _cycle = 0;
	public VehicleDeparture _vd = null;
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
						it = player.getInventory().getItemByItemId(_t1.ticketId);
						if ((it != null) && (it.getCount() >= 1))
						{
							player.getInventory().destroyItem("Boat", it.getObjectId(), 1, player, this);
							final InventoryUpdate iu = new InventoryUpdate();
							iu.addModifiedItem(it);
							player.sendPacket(iu);
							_inboat.put(i, player);
							i++;
						}
						else if ((it == null) && (_t1.ticketId == 0))
						{
							_inboat.put(i, player);
							i++;
						}
						else
						{
							player.teleToLocation(_t1.ntx, _t1.nty, _t1.ntz, false);
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
						it = player.getInventory().getItemByItemId(_t2.ticketId);
						if ((it != null) && (it.getCount() >= 1))
						{
							player.getInventory().destroyItem("Boat", it.getObjectId(), 1, player, this);
							final InventoryUpdate iu = new InventoryUpdate();
							iu.addModifiedItem(it);
							player.sendPacket(iu);
							_inboat.put(i, player);
							i++;
						}
						else if ((it == null) && (_t2.ticketId == 0))
						{
							_inboat.put(i, player);
							i++;
						}
						else
						{
							player.teleToLocation(_t2.ntx, _t2.nty, _t2.ntz, false);
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
					sm = new CreatureSay(0, Say2.SHOUT, _t1.npc, _t1.sysmess10);
				}
				else
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t2.npc, _t2.sysmess10);
				}
				ps = new PlaySound(0, "itemsound.ship_arrival_departure", this);
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
					sm = new CreatureSay(0, Say2.SHOUT, _t1.npc, _t1.sysmess5);
				}
				else
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t2.npc, _t2.sysmess5);
				}
				ps = new PlaySound(0, "itemsound.ship_5min", this);
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
					sm = new CreatureSay(0, Say2.SHOUT, _t1.npc, _t1.sysmess1);
				}
				else
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t2.npc, _t2.sysmess1);
				}
				ps = new PlaySound(0, "itemsound.ship_1min", this);
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
					sm = new CreatureSay(0, Say2.SHOUT, _t1.npc, _t1.sysmess0);
				}
				else
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t2.npc, _t2.sysmess0);
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
					sm = new CreatureSay(0, Say2.SHOUT, _t1.npc, _t1.sysmessb);
				}
				else
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t2.npc, _t2.sysmessb);
				}
				ps = new PlaySound(0, "itemsound.ship_arrival_departure", this);
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
	
	public void setPathA(int idWaypoint1, int idWTicket1, int ntx1, int nty1, int ntz1, String idnpc1, String sysmess10, String sysmess5, String sysmess1, String sysmess0, String sysmessb, Map<Integer, BoatPoint> path)
	{
		_t1 = new BoatPathHolder(idWaypoint1, idWTicket1, ntx1, nty1, ntz1, idnpc1, sysmess10, sysmess5, sysmess1, sysmess0, sysmessb, _name, path);
	}
	
	public void setPathB(int idWaypoint1, int idWTicket1, int ntx1, int nty1, int ntz1, String idnpc1, String sysmess10, String sysmess5, String sysmess1, String sysmess0, String sysmessb, Map<Integer, BoatPoint> path)
	{
		_t2 = new BoatPathHolder(idWaypoint1, idWTicket1, ntx1, nty1, ntz1, idnpc1, sysmess10, sysmess5, sysmess1, sysmess0, sysmessb, _name, path);
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
