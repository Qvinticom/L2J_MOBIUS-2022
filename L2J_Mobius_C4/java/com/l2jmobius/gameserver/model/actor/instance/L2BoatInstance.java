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
package com.l2jmobius.gameserver.model.actor.instance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.GameTimeController;
import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.ai.L2BoatAI;
import com.l2jmobius.gameserver.model.L2CharPosition;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.actor.knownlist.BoatKnownList;
import com.l2jmobius.gameserver.model.actor.stat.BoatStat;
import com.l2jmobius.gameserver.network.clientpackets.Say2;
import com.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import com.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jmobius.gameserver.network.serverpackets.PlaySound;
import com.l2jmobius.gameserver.network.serverpackets.VehicleDeparture;
import com.l2jmobius.gameserver.network.serverpackets.VehicleInfo;
import com.l2jmobius.gameserver.network.serverpackets.VehicleStarted;
import com.l2jmobius.gameserver.templates.L2CharTemplate;
import com.l2jmobius.gameserver.templates.L2Weapon;
import com.l2jmobius.gameserver.util.Util;

/**
 * @author Maktakien
 */
public class L2BoatInstance extends L2Character
{
	protected static final Logger _logBoat = Logger.getLogger(L2BoatInstance.class.getName());
	
	protected final ArrayList<L2PcInstance> _passengers = new ArrayList<>();
	protected ArrayList<L2BoatPoint> _currentPath;
	
	protected byte _runState = 0;
	
	protected L2BoatTrajet _t1;
	protected L2BoatTrajet _t2;
	
	// default
	protected int _cycle = 1;
	
	public L2BoatInstance(int objectId, L2CharTemplate template)
	{
		super(objectId, template);
		getKnownList();
		getStat();
		setAI(new L2BoatAI(new AIAccessor()));
	}
	
	@Override
	public final BoatKnownList getKnownList()
	{
		if ((super.getKnownList() == null) || !(super.getKnownList() instanceof BoatKnownList))
		{
			setKnownList(new BoatKnownList(this));
		}
		return (BoatKnownList) super.getKnownList();
	}
	
	@Override
	public final BoatStat getStat()
	{
		if ((super.getStat() == null) || !(super.getStat() instanceof BoatStat))
		{
			setStat(new BoatStat(this));
		}
		return (BoatStat) super.getStat();
	}
	
	public void begin()
	{
		// fistly, check passengers
		checkPassengers();
		
		_runState = 0;
		_currentPath = null;
		
		if (_cycle == 1)
		{
			_currentPath = _t1._path;
		}
		else
		{
			_currentPath = _t2._path;
		}
		
		if (_currentPath == null)
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			return;
		}
		
		final L2BoatPoint point = _currentPath.get(0);
		if (point != null)
		{
			getStat().setMoveSpeed(point.getMoveSpeed());
			getStat().setRotationSpeed(point.getRotationSpeed());
			
			// departure
			getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(point.getX(), point.getY(), point.getZ(), 0));
		}
	}
	
	private void checkPassengers()
	{
		_passengers.clear();
		final Collection<L2PcInstance> knownPlayers = getKnownList().getKnownPlayersInRadius(1000);
		if ((knownPlayers != null) && !knownPlayers.isEmpty())
		{
			for (final L2PcInstance player : getKnownList().getKnownPlayersInRadius(1000))
			{
				if (player == null)
				{
					continue;
				}
				
				if (player.isInBoat() && (player.getBoat() == this))
				{
					addPassenger(player);
				}
			}
		}
	}
	
	@Override
	public boolean moveToNextRoutePoint()
	{
		_move = null;
		_runState++;
		
		if (_runState < _currentPath.size())
		{
			final L2BoatPoint point = _currentPath.get(_runState);
			if (!isMovementDisabled())
			{
				getStat().setMoveSpeed(point.getMoveSpeed());
				getStat().setRotationSpeed(point.getRotationSpeed());
				
				final MoveData m = new MoveData();
				m.disregardingGeodata = false;
				m.onGeodataPathIndex = -1;
				m._xDestination = point.getX();
				m._yDestination = point.getY();
				m._zDestination = point.getZ();
				m._heading = 0;
				
				final double dx = point.getX() - getX();
				final double dy = point.getY() - getY();
				final double distance = Math.sqrt((dx * dx) + (dy * dy));
				if (distance > 1)
				{
					setHeading(Util.calculateHeadingFrom(getX(), getY(), point.getX(), point.getY()));
				}
				
				GameTimeController.getInstance();
				m._moveStartTime = GameTimeController.getGameTicks();
				_move = m;
				
				GameTimeController.getInstance().registerMovingObject(this);
				
				broadcastPacket(new VehicleDeparture(this));
				return true;
			}
		}
		
		if (_cycle == 1)
		{
			_cycle = 2;
		}
		else
		{
			_cycle = 1;
		}
		
		say(10);
		ThreadPoolManager.getInstance().scheduleGeneral(new BoatCaptain(1, this), 300000);
		return false;
	}
	
	@Override
	public boolean updatePosition(int gameTicks)
	{
		final boolean result = super.updatePosition(gameTicks);
		
		if (!_passengers.isEmpty())
		{
			for (final L2PcInstance player : _passengers)
			{
				if ((player != null) && (player.getBoat() == this))
				{
					player.setXYZ(getX(), getY(), getZ());
					
					player.revalidateZone(false);
				}
			}
		}
		return result;
	}
	
	@Override
	public void stopMove(L2CharPosition pos)
	{
		_move = null;
		if (pos != null)
		{
			setXYZ(pos.x, pos.y, pos.z);
			setHeading(pos.heading);
			revalidateZone(true);
		}
		
		broadcastPacket(new VehicleStarted(getObjectId(), 0));
		broadcastPacket(new VehicleInfo(this));
	}
	
	private void addPassenger(L2PcInstance player)
	{
		final int itemId;
		if (_cycle == 1)
		{
			itemId = _t1._IdWTicket1;
		}
		else
		{
			itemId = _t2._IdWTicket1;
		}
		
		if (itemId != 0)
		{
			final L2ItemInstance it = player.getInventory().getItemByItemId(itemId);
			if ((it != null) && (it.getCount() >= 1))
			{
				player.getInventory().destroyItem("Boat", it, 1, player, this);
				final InventoryUpdate iu = new InventoryUpdate();
				iu.addModifiedItem(it);
				player.sendPacket(iu);
			}
			else
			{
				oustPlayer(player);
				return;
			}
		}
		_passengers.add(player);
	}
	
	public void oustPlayer(L2PcInstance player)
	{
		final int x, y, z;
		if (_cycle == 1)
		{
			x = _t1._ntx1;
			y = _t1._nty1;
			z = _t1._ntz1;
		}
		else
		{
			x = _t2._ntx1;
			y = _t2._nty1;
			z = _t2._ntz1;
		}
		
		removePassenger(player);
		
		if (player.isOnline() > 0)
		{
			player.teleToLocation(x, y, z);
		}
		else
		{
			player.setXYZInvisible(x, y, z); // disconnects handling
		}
	}
	
	public void removePassenger(L2PcInstance player)
	{
		if (!_passengers.isEmpty() && _passengers.contains(player))
		{
			_passengers.remove(player);
		}
	}
	
	/**
	 * @param i
	 */
	public void say(int i)
	{
		final Collection<L2PcInstance> knownPlayers = getKnownList().getKnownPlayers().values();
		if ((knownPlayers == null) || knownPlayers.isEmpty())
		{
			return;
		}
		
		CreatureSay sm;
		PlaySound ps;
		switch (i)
		{
			case 10:
				if (_cycle == 1)
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t1._npc1, _t1._sysmess10_1);
				}
				else
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t2._npc1, _t2._sysmess10_1);
				}
				
				ps = new PlaySound(0, "itemsound.ship_arrival_departure", 1, getObjectId(), getX(), getY(), getZ());
				
				for (final L2PcInstance player : knownPlayers)
				{
					player.sendPacket(sm);
					player.sendPacket(ps);
				}
				break;
			case 5:
				if (_cycle == 1)
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t1._npc1, _t1._sysmess5_1);
				}
				else
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t2._npc1, _t2._sysmess5_1);
				}
				
				ps = new PlaySound(0, "itemsound.ship_5min", 1, getObjectId(), getX(), getY(), getZ());
				
				for (final L2PcInstance player : knownPlayers)
				{
					player.sendPacket(sm);
					player.sendPacket(ps);
				}
				break;
			case 1:
				if (_cycle == 1)
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t1._npc1, _t1._sysmess1_1);
				}
				else
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t2._npc1, _t2._sysmess1_1);
				}
				
				ps = new PlaySound(0, "itemsound.ship_1min", 1, getObjectId(), getX(), getY(), getZ());
				
				for (final L2PcInstance player : knownPlayers)
				{
					player.sendPacket(sm);
					player.sendPacket(ps);
				}
				break;
			case 0:
				if (_cycle == 1)
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t1._npc1, _t1._sysmess0_1);
				}
				else
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t2._npc1, _t2._sysmess0_1);
				}
				
				for (final L2PcInstance player : knownPlayers)
				{
					player.sendPacket(sm);
				}
				break;
			case -1:
				if (_cycle == 1)
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t1._npc1, _t1._sysmessb_1);
				}
				else
				{
					sm = new CreatureSay(0, Say2.SHOUT, _t2._npc1, _t2._sysmessb_1);
				}
				
				ps = new PlaySound(0, "itemsound.ship_arrival_departure", 1, getObjectId(), getX(), getY(), getZ());
				
				for (final L2PcInstance player : knownPlayers)
				{
					player.sendPacket(sm);
					player.sendPacket(ps);
				}
				break;
		}
	}
	
	public void beginCycle()
	{
		say(10);
		ThreadPoolManager.getInstance().scheduleGeneral(new BoatCaptain(1, this), 300000);
	}
	
	private class BoatCaptain implements Runnable
	{
		private final int _state;
		private final L2BoatInstance _boat;
		
		/**
		 * @param i
		 * @param instance
		 */
		public BoatCaptain(int i, L2BoatInstance instance)
		{
			_state = i;
			_boat = instance;
		}
		
		@Override
		public void run()
		{
			// final BoatCaptain bc;
			switch (_state)
			{
				case 1:
					_boat.say(5);
					ThreadPoolManager.getInstance().scheduleGeneral(new BoatCaptain(2, _boat), 240000);
					break;
				case 2:
					_boat.say(1);
					ThreadPoolManager.getInstance().scheduleGeneral(new BoatCaptain(3, _boat), 40000);
					break;
				case 3:
					_boat.say(0);
					ThreadPoolManager.getInstance().scheduleGeneral(new BoatCaptain(4, _boat), 20000);
					break;
				case 4:
					_boat.say(-1);
					_boat.begin();
					break;
			}
		}
	}
	
	/**
	 * @param idWaypoint1
	 * @param idWTicket1
	 * @param ntx1
	 * @param nty1
	 * @param ntz1
	 * @param idnpc1
	 * @param sysmess10_1
	 * @param sysmess5_1
	 * @param sysmess1_1
	 * @param sysmess0_1
	 * @param sysmessb_1
	 */
	public void SetTrajet1(int idWaypoint1, int idWTicket1, int ntx1, int nty1, int ntz1, String idnpc1, String sysmess10_1, String sysmess5_1, String sysmess1_1, String sysmess0_1, String sysmessb_1)
	{
		_t1 = new L2BoatTrajet(idWaypoint1, idWTicket1, ntx1, nty1, ntz1, idnpc1, sysmess10_1, sysmess5_1, sysmess1_1, sysmess0_1, sysmessb_1);
	}
	
	public void SetTrajet2(int idWaypoint1, int idWTicket1, int ntx1, int nty1, int ntz1, String idnpc1, String sysmess10_1, String sysmess5_1, String sysmess1_1, String sysmess0_1, String sysmessb_1)
	{
		_t2 = new L2BoatTrajet(idWaypoint1, idWTicket1, ntx1, nty1, ntz1, idnpc1, sysmess10_1, sysmess5_1, sysmess1_1, sysmess0_1, sysmessb_1);
	}
	
	private class L2BoatPoint
	{
		int speed1;
		int speed2;
		int x;
		int y;
		int z;
		
		public L2BoatPoint()
		{
		}
		
		public int getMoveSpeed()
		{
			return speed1;
		}
		
		public int getRotationSpeed()
		{
			return speed2;
		}
		
		public int getX()
		{
			return x;
		}
		
		public int getY()
		{
			return y;
		}
		
		public int getZ()
		{
			return z;
		}
	}
	
	private class L2BoatTrajet
	{
		ArrayList<L2BoatPoint> _path;
		
		public int _IdWaypoint1;
		public int _IdWTicket1;
		public int _ntx1;
		public int _nty1;
		public int _ntz1;
		public String _npc1;
		public String _sysmess10_1;
		public String _sysmess5_1;
		public String _sysmess1_1;
		public String _sysmessb_1;
		public String _sysmess0_1;
		
		/**
		 * @param idWaypoint1
		 * @param idWTicket1
		 * @param ntx1
		 * @param nty1
		 * @param ntz1
		 * @param npc1
		 * @param sysmess10_1
		 * @param sysmess5_1
		 * @param sysmess1_1
		 * @param sysmess0_1
		 * @param sysmessb_1
		 */
		public L2BoatTrajet(int idWaypoint1, int idWTicket1, int ntx1, int nty1, int ntz1, String npc1, String sysmess10_1, String sysmess5_1, String sysmess1_1, String sysmess0_1, String sysmessb_1)
		{
			_IdWaypoint1 = idWaypoint1;
			_IdWTicket1 = idWTicket1;
			_ntx1 = ntx1;
			_nty1 = nty1;
			_ntz1 = ntz1;
			_npc1 = npc1;
			_sysmess10_1 = sysmess10_1;
			_sysmess5_1 = sysmess5_1;
			_sysmess1_1 = sysmess1_1;
			_sysmessb_1 = sysmessb_1;
			_sysmess0_1 = sysmess0_1;
			loadBoatPath();
		}
		
		/**
		 * @param line
		 */
		public void parseLine(String line)
		{
			_path = new ArrayList<>();
			final StringTokenizer st = new StringTokenizer(line, ";");
			Integer.parseInt(st.nextToken());
			
			final int max = Integer.parseInt(st.nextToken());
			for (int i = 0; i < max; i++)
			{
				final L2BoatPoint bp = new L2BoatPoint();
				bp.speed1 = Integer.parseInt(st.nextToken());
				bp.speed2 = Integer.parseInt(st.nextToken());
				bp.x = Integer.parseInt(st.nextToken());
				bp.y = Integer.parseInt(st.nextToken());
				bp.z = Integer.parseInt(st.nextToken());
				_path.add(bp);
			}
		}
		
		/**
		 * 
		 */
		private void loadBoatPath()
		{
			final File boatData = new File(Config.DATAPACK_ROOT, "data/boatpath.csv");
			try (FileReader fr = new FileReader(boatData);
				BufferedReader br = new BufferedReader(fr);
				LineNumberReader lnr = new LineNumberReader(br))
			{
				String line = null;
				while ((line = lnr.readLine()) != null)
				{
					if ((line.trim().length() == 0) || !line.startsWith(_IdWaypoint1 + ";"))
					{
						continue;
					}
					parseLine(line);
					return;
				}
				_logBoat.warning("No path for boat " + getName() + " !!!");
			}
			catch (final FileNotFoundException e)
			{
				_logBoat.warning("boatpath.csv is missing in data folder");
			}
			catch (final Exception e)
			{
				_logBoat.warning("error while creating boat table " + e);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.model.L2Character#updateAbnormalEffect()
	 */
	@Override
	public void updateAbnormalEffect()
	{
		// TODO Auto-generated method stub
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.model.L2Character#getActiveWeaponInstance()
	 */
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.model.L2Character#getActiveWeaponItem()
	 */
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.model.L2Character#getSecondaryWeaponInstance()
	 */
	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.model.L2Character#getSecondaryWeaponItem()
	 */
	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.model.L2Character#getLevel()
	 */
	@Override
	public int getLevel()
	{
		// TODO Auto-generated method stub
		return 0;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.model.L2Object#isAutoAttackable(com.l2jmobius.gameserver.model.L2Character)
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	public void sendBoatInfo(L2PcInstance activeChar)
	{
		activeChar.sendPacket(new VehicleInfo(this));
		if (isMoving())
		{
			activeChar.sendPacket(new VehicleDeparture(this));
		}
	}
	
	public class AIAccessor extends L2Character.AIAccessor
	{
		@Override
		public void detachAI()
		{
		}
	}
}