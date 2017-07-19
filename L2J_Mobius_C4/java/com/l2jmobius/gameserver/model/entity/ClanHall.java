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
package com.l2jmobius.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.L2DatabaseFactory;
import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.datatables.ClanTable;
import com.l2jmobius.gameserver.datatables.DoorTable;
import com.l2jmobius.gameserver.instancemanager.AuctionManager;
import com.l2jmobius.gameserver.model.L2Clan;
import com.l2jmobius.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.zone.type.L2ClanHallZone;
import com.l2jmobius.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

import javolution.util.FastList;
import javolution.util.FastMap;

public class ClanHall
{
	protected static Logger _log = Logger.getLogger(ClanHall.class.getName());
	
	// =========================================================
	// Data Field
	private int _ClanHallId = 0;
	private List<L2DoorInstance> _Doors;
	private final List<String> _DoorDefault = new FastList<>();
	private String _Name = "";
	int _OwnerId = 0;
	private int _lease = 0;
	private String _desc = "";
	private String _location = "";
	long _paidUntil;
	
	private int _grade;
	protected final int _chRate = 604800000;
	private L2ClanHallZone _zone;
	
	private final Map<Integer, ClanHallFunction> _functions = new FastMap<>();
	protected boolean _paid;
	
	// clan hall functions
	public static final int FUNC_TELEPORT = 1;
	public static final int FUNC_ITEM_CREATE = 2;
	public static final int FUNC_RESTORE_HP = 3;
	public static final int FUNC_RESTORE_MP = 4;
	public static final int FUNC_RESTORE_EXP = 5;
	public static final int FUNC_SUPPORT = 6;
	public static final int FUNC_DECO_FRONTPLATEFORM = 7;
	public static final int FUNC_DECO_CURTAINS = 8;
	
	private class RentTask implements Runnable
	{
		public RentTask()
		{
		}
		
		@Override
		public void run()
		{
			try
			{
				if (_OwnerId == 0)
				{
					return;
				}
				
				if (ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().getAdena() >= getLease())
				{
					if (_paidUntil != 0)
					{
						while (_paidUntil <= System.currentTimeMillis())
						{
							_paidUntil += _chRate;
						}
					}
					else
					{
						_paidUntil = System.currentTimeMillis() + _chRate;
					}
					
					ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().destroyItemByItemId("CH_rental_fee", 57, getLease(), null, null);
					
					if (Config.DEBUG)
					{
						_log.warning("deducted " + getLease() + " adena from " + getName() + " owner's cwh for ClanHall _paidUntil" + _paidUntil);
					}
					
					ThreadPoolManager.getInstance().scheduleGeneral(new RentTask(), _paidUntil - System.currentTimeMillis());
					_paid = true;
					updateDb();
				}
				else
				{
					_paid = false;
					if (System.currentTimeMillis() > (_paidUntil + _chRate))
					{
						setOwner(null);
						ClanTable.getInstance().getClan(getOwnerId()).broadcastToOnlineMembers(new SystemMessage(SystemMessage.THE_CLAN_HALL_FEE_IS_ONE_WEEK_OVERDUE_THEREFORE_THE_CLAN_HALL_OWNERSHIP_HAS_BEEN_REVOKED));
					}
					else
					{
						updateDb();
						ClanTable.getInstance().getClan(getOwnerId()).broadcastToOnlineMembers(new SystemMessage(SystemMessage.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW));
						
						if ((System.currentTimeMillis() + (1000 * 60 * 60 * 24)) <= (_paidUntil + _chRate))
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new RentTask(), (1000 * 60 * 60 * 24)); // 1 day
						}
						else
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new RentTask(), (_paidUntil + _chRate) - System.currentTimeMillis());
						}
					}
				}
			}
			catch (final Throwable t)
			{
			}
		}
	}
	
	private void startRentTask(boolean forced)
	{
		final long currentTime = System.currentTimeMillis();
		if (_paidUntil > currentTime)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new RentTask(), _paidUntil - currentTime);
		}
		else if (!_paid && !forced)
		{
			if ((System.currentTimeMillis() + (1000 * 60 * 60 * 24)) <= (_paidUntil + _chRate))
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new RentTask(), (1000 * 60 * 60 * 24)); // 1 day
			}
			else
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new RentTask(), (_paidUntil + _chRate) - System.currentTimeMillis());
			}
		}
		else
		{
			new RentTask().run();
		}
	}
	
	// =========================================================
	// Constructor
	public ClanHall(int clanHallId, String name, int ownerId, int lease, String desc, String location, long paidUntil, int Grade, boolean paid)
	{
		_ClanHallId = clanHallId;
		_Name = name;
		_OwnerId = ownerId;
		_lease = lease;
		_desc = desc;
		_location = location;
		_paidUntil = paidUntil;
		_grade = Grade;
		_paid = paid;
		
		if (getOwnerId() == 0)
		{
			return;
		}
		
		if (ClanTable.getInstance().getClan(_OwnerId) != null)
		{
			ClanTable.getInstance().getClan(_OwnerId).setHasHideout(_ClanHallId);
		}
		
		loadFunctions();
		startRentTask(false);
	}
	
	// =========================================================
	// Method - Public
	public void openCloseDoor(int doorId, boolean open)
	{
		openCloseDoor(getDoor(doorId), open);
	}
	
	public void openCloseDoor(L2DoorInstance door, boolean open)
	{
		if (door != null)
		{
			if (open)
			{
				door.openMe();
			}
			else
			{
				door.closeMe();
			}
		}
	}
	
	public void openCloseDoor(L2PcInstance activeChar, int doorId, boolean open)
	{
		if ((activeChar != null) && (activeChar.getClanId() == getOwnerId()))
		{
			openCloseDoor(doorId, open);
		}
	}
	
	public void openCloseDoors(boolean open)
	{
		for (final L2DoorInstance door : getDoors())
		{
			if (door != null)
			{
				if (open)
				{
					door.openMe();
				}
				else
				{
					door.closeMe();
				}
			}
		}
	}
	
	public void openCloseDoors(L2PcInstance activeChar, boolean open)
	{
		if ((activeChar != null) && (activeChar.getClanId() == getOwnerId()))
		{
			openCloseDoors(open);
		}
	}
	
	public void setOwner(L2Clan clan)
	{
		// Remove old owner
		if ((getOwnerId() > 0) && ((clan == null) || (clan.getClanId() != getOwnerId())))
		{
			// Try to find clan instance
			final L2Clan oldOwner = ClanTable.getInstance().getClan(getOwnerId());
			if (oldOwner != null)
			{
				oldOwner.setHasHideout(0); // Unset hasHideout flag for old owner
			}
		}
		
		updateOwnership(clan); // Update in database
	}
	
	private void updateOwnership(L2Clan clan)
	{
		if (clan != null)
		{
			// Update owner id property
			_OwnerId = clan.getClanId();
			
			// Announce to clan members
			clan.setHasHideout(getId()); // Set has hideout flag for new owner
			clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
			
			_paidUntil = System.currentTimeMillis();
			
			// start rent task
			startRentTask(true);
		}
		else
		{
			// Removals
			_paidUntil = 0;
			_OwnerId = 0;
			_paid = false;
			
			// Reset functions
			for (final Map.Entry<Integer, ClanHallFunction> fc : _functions.entrySet())
			{
				removeFunction(fc.getKey());
			}
			_functions.clear();
			
			if (AuctionManager.initNPC(getId()))
			{
				AuctionManager.getInstance().getAuctions().add(new Auction(getId()));
			}
		}
		
		updateDb();
	}
	
	/** Respawn all doors */
	public void spawnDoor()
	{
		spawnDoor(false);
	}
	
	/**
	 * Respawn all doors
	 * @param isDoorWeak
	 */
	public void spawnDoor(boolean isDoorWeak)
	{
		for (int i = 0; i < getDoors().size(); i++)
		{
			L2DoorInstance door = getDoors().get(i);
			if (door.getCurrentHp() <= 0)
			{
				door.decayMe(); // Kill current if not killed already
				door = DoorTable.parseList(_DoorDefault.get(i));
				DoorTable.getInstance().putDoor(door); // Read the new door to the DoorTable By Erb
				if (isDoorWeak)
				{
					door.setCurrentHp(door.getMaxHp() / 2);
				}
				door.spawnMe(door.getX(), door.getY(), door.getZ());
				getDoors().set(i, door);
			}
			else if (door.getOpen() == 0)
			{
				door.closeMe();
			}
		}
	}
	
	// =========================================================
	// Method - Private
	private void loadFunctions()
	
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("Select * from clanhall_functions where hall_id = ?"))
		{
			statement.setInt(1, getId());
			try (ResultSet rs = statement.executeQuery())
			{
				while (rs.next())
				{
					_functions.put(rs.getInt("type"), new ClanHallFunction(rs.getInt("type"), rs.getInt("lvl"), rs.getInt("lease"), rs.getLong("rate"), rs.getLong("endTime")));
				}
			}
		}
		catch (final Exception e)
		{
			_log.log(Level.SEVERE, "Exception: ClanHall.loadFunctions(): " + e.getMessage(), e);
		}
	}
	
	void updateDb()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE clanhall SET ownerId=?, paidUntil=?, paid=? WHERE id=?"))
		{
			statement.setInt(1, _OwnerId);
			statement.setLong(2, _paidUntil);
			statement.setInt(3, (_paid) ? 1 : 0);
			statement.setInt(4, _ClanHallId);
			statement.execute();
		}
		catch (final Exception e)
		{
			System.out.println("Exception: updateDb: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	// =========================================================
	// Property
	public final int getId()
	{
		return _ClanHallId;
	}
	
	public final L2DoorInstance getDoor(int doorId)
	{
		if (doorId <= 0)
		{
			return null;
		}
		
		for (int i = 0; i < getDoors().size(); i++)
		{
			final L2DoorInstance door = getDoors().get(i);
			if (door.getDoorId() == doorId)
			{
				return door;
			}
		}
		return null;
	}
	
	public final List<L2DoorInstance> getDoors()
	{
		if (_Doors == null)
		{
			_Doors = new FastList<>();
		}
		return _Doors;
	}
	
	public final String getName()
	{
		return _Name;
	}
	
	public final int getOwnerId()
	{
		return _OwnerId;
	}
	
	public final int getLease()
	{
		return _lease;
	}
	
	public final String getDesc()
	{
		return _desc;
	}
	
	public final String getLocation()
	{
		return _location;
	}
	
	public final long getPaidUntil()
	{
		return _paidUntil;
	}
	
	public final int getGrade()
	{
		return _grade;
	}
	
	public final boolean getPaid()
	{
		return _paid;
	}
	
	public void setZone(L2ClanHallZone zone)
	{
		_zone = zone;
	}
	
	public L2ClanHallZone getZone()
	{
		return _zone;
	}
	
	public void banishForeigners()
	{
		_zone.banishForeigners(getOwnerId());
	}
	
	public ClanHallFunction getFunction(int type)
	{
		if (_functions.get(type) != null)
		{
			return _functions.get(type);
		}
		return null;
	}
	
	public void removeFunction(int functionType)
	{
		final ClanHallFunction function = _functions.remove(functionType);
		if ((function != null) && (function.getFunctionTask() != null))
		{
			function.getFunctionTask().cancel(false);
		}
		
		// ===================================== Removes from DB===============
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM clanhall_functions WHERE hall_id=? AND type=?"))
		{
			statement.setInt(1, getId());
			statement.setInt(2, functionType);
			statement.execute();
		}
		catch (final Exception e)
		{
			_log.log(Level.SEVERE, "Exception: ClanHall.removeFunction(int functionType): " + e.getMessage(), e);
		}
		// ============================================================================
	}
	
	public boolean updateFunctions(int type, int lvl, int lease, long rate, long time, boolean addNew)
	{
		if (Config.DEBUG)
		{
			_log.warning("Called ClanHall.updateFunctions(int type, int lvl, int lease, long rate, long time, boolean addNew)");
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			if (addNew)
			{
				if ((ClanTable.getInstance().getClan(getOwnerId()) != null) && (ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().getAdena() >= lease))
				{
					ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().destroyItemByItemId("CH_function_fee", 57, lease, null, null);
				}
				else
				{
					return false;
				}
				
				try (PreparedStatement statement = con.prepareStatement("INSERT INTO clanhall_functions (hall_id, type, lvl, lease, rate, endTime) VALUES (?,?,?,?,?,?)"))
				{
					statement.setInt(1, getId());
					statement.setInt(2, type);
					statement.setInt(3, lvl);
					statement.setInt(4, lease);
					statement.setLong(5, rate);
					statement.setLong(6, time);
					statement.execute();
				}
				_functions.put(type, new ClanHallFunction(type, lvl, lease, rate, time));
				
				if (Config.DEBUG)
				{
					_log.warning("INSERT INTO clanhall_functions (hall_id, type, lvl, lease, rate, endTime) VALUES (?,?,?,?,?,?)");
				}
			}
			else if (getFunction(type) != null)
			{
				if ((lvl == 0) && (lease == 0))
				{
					removeFunction(type);
					return true;
				}
				
				final ClanHallFunction function = getFunction(type);
				
				if (ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().getAdena() >= (lease - function.getLease()))
				{
					if ((lease - function.getLease()) > 0)
					{
						ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().destroyItemByItemId("CH_function_fee", 57, lease - function.getLease(), null, null);
					}
				}
				else
				{
					return false;
				}
				
				try (PreparedStatement statement = con.prepareStatement("UPDATE clanhall_functions SET lvl=?, lease=? WHERE hall_id=? AND type=?"))
				{
					statement.setInt(1, lvl);
					statement.setInt(2, lease);
					statement.setInt(3, getId());
					statement.setInt(4, type);
					statement.execute();
				}
				function.setLvl(lvl);
				function.setLease(lease);
				
				if (Config.DEBUG)
				{
					_log.warning("UPDATE clanhall_functions WHERE hall_id=? AND id=? SET lvl, lease");
				}
			}
		}
		catch (final Exception e)
		{
			_log.log(Level.SEVERE, "Exception: ClanHall.updateFunctions(int type, int lvl, int lease, long rate, long time, boolean addNew): " + e.getMessage(), e);
		}
		return true;
	}
	
	public class ClanHallFunction
	{
		private final int _type;
		private int _lvl;
		protected int _fee;
		private final long _rate;
		long _endTime;
		Future<?> _functionTask;
		
		public ClanHallFunction(int type, int lvl, int lease, long rate, long time)
		{
			_type = type;
			_lvl = lvl;
			_fee = lease;
			_rate = rate;
			_endTime = time;
			_functionTask = ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(), 1000);
		}
		
		public int getType()
		{
			return _type;
		}
		
		public int getLvl()
		{
			return _lvl;
		}
		
		public int getLease()
		{
			return _fee;
		}
		
		public long getRate()
		{
			return _rate;
		}
		
		public void setLvl(int lvl)
		{
			_lvl = lvl;
		}
		
		public void setLease(int lease)
		{
			_fee = lease;
		}
		
		public long getEndTime()
		{
			return _endTime;
		}
		
		public Future<?> getFunctionTask()
		{
			return _functionTask;
		}
		
		public void updateFunctionRent()
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("UPDATE clanhall_functions SET endTime=? WHERE type=? AND hall_id=?"))
			{
				statement.setLong(1, _endTime);
				statement.setInt(2, getType());
				statement.setInt(3, getId());
				statement.execute();
			}
			catch (final Exception e)
			{
				_log.log(Level.SEVERE, "Exception: ClanHall.ClanHallFunction.updateFunctionRent(int functionType): " + e.getMessage(), e);
			}
		}
		
		// ==========================================================
		// FunctionTask
		
		private class FunctionTask implements Runnable
		{
			public FunctionTask()
			{
			}
			
			@Override
			public void run()
			{
				try
				{
					if ((ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().getAdena() >= _fee) && (ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().getAdena() >= (_fee * 2))) // if player didn't pay before add extra fee
					{
						if ((_endTime - System.currentTimeMillis()) <= 0)
						{
							ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().destroyItemByItemId("CH_function_fee", 57, _fee, null, null);
							
							if (Config.DEBUG)
							{
								_log.warning("deducted " + _fee + " adena from " + getName() + " owner's cwh for functions");
							}
							
							_endTime = System.currentTimeMillis() + getRate();
							
							updateFunctionRent();
						}
						
						_functionTask = ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(), _endTime - System.currentTimeMillis());
					}
					else
					{
						removeFunction(getType());
					}
				}
				catch (final Throwable t)
				{
				}
			}
		}
	}
}