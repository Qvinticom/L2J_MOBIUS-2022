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
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.L2DatabaseFactory;
import com.l2jmobius.gameserver.Announcements;
import com.l2jmobius.gameserver.SevenSigns;
import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.datatables.ClanTable;
import com.l2jmobius.gameserver.datatables.DoorTable;
import com.l2jmobius.gameserver.instancemanager.CastleManager;
import com.l2jmobius.gameserver.instancemanager.CastleManorManager;
import com.l2jmobius.gameserver.instancemanager.CastleManorManager.CropProcure;
import com.l2jmobius.gameserver.instancemanager.CastleManorManager.SeedProduction;
import com.l2jmobius.gameserver.model.L2Clan;
import com.l2jmobius.gameserver.model.L2ClanMember;
import com.l2jmobius.gameserver.model.L2Manor;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.zone.type.L2CastleTeleportZone;
import com.l2jmobius.gameserver.model.zone.type.L2SiegeZone;
import com.l2jmobius.gameserver.network.serverpackets.PlaySound;
import com.l2jmobius.gameserver.network.serverpackets.PledgeShowInfoUpdate;

import javolution.util.FastList;
import javolution.util.FastMap;

public class Castle
{
	protected static Logger _log = Logger.getLogger(Castle.class.getName());
	
	// =========================================================
	// Data Field
	private FastList<CropProcure> _procure = new FastList<>();
	private FastList<SeedProduction> _production = new FastList<>();
	private FastList<CropProcure> _procureNext = new FastList<>();
	private FastList<SeedProduction> _productionNext = new FastList<>();
	private boolean _isNextPeriodApproved = false;
	
	private static final String CASTLE_MANOR_DELETE_PRODUCTION = "DELETE FROM castle_manor_production WHERE castle_id=?";
	private static final String CASTLE_MANOR_DELETE_PRODUCTION_PERIOD = "DELETE FROM castle_manor_production WHERE castle_id=? AND period=?";
	private static final String CASTLE_MANOR_DELETE_PROCURE = "DELETE FROM castle_manor_procure WHERE castle_id=?";
	private static final String CASTLE_MANOR_DELETE_PROCURE_PERIOD = "DELETE FROM castle_manor_procure WHERE castle_id=? AND period=?";
	private static final String CASTLE_UPDATE_CROP = "UPDATE castle_manor_procure SET can_buy=? WHERE crop_id=? AND castle_id=? AND period=?";
	private static final String CASTLE_UPDATE_SEED = "UPDATE castle_manor_production SET can_produce=? WHERE seed_id=? AND castle_id=? AND period=?";
	
	// =========================================================
	// Data Field
	private int _CastleId = 0;
	private final List<L2DoorInstance> _Doors = new FastList<>();
	private final List<String> _DoorDefault = new FastList<>();
	private String _Name = "";
	private int _OwnerId = 0;
	private Siege _Siege = null;
	private Calendar _SiegeDate;
	private int _SiegeDayOfWeek = 7; // Default to saturday
	private int _SiegeHourOfDay = 20; // Default to 8 pm server time
	private int _TaxPercent = 0;
	private double _TaxRate = 0;
	private int _Treasury = 0;
	
	private L2SiegeZone _zone;
	private L2CastleTeleportZone _teleZone;
	private int _nbArtifact = 1;
	
	private final Map<Integer, Integer> _engrave = new FastMap<>();
	private final Map<Integer, CastleFunction> _function;
	
	/** Castle Functions */
	public static final int FUNC_TELEPORT = 1;
	public static final int FUNC_RESTORE_HP = 2;
	public static final int FUNC_RESTORE_MP = 3;
	public static final int FUNC_RESTORE_EXP = 4;
	public static final int FUNC_SUPPORT = 5;
	
	public class CastleFunction
	{
		private final int _type;
		private int _lvl;
		protected int _fee;
		protected int _tempFee;
		private final long _rate;
		private long _endDate;
		protected boolean _inDebt;
		public boolean _cwh;
		Future<?> _functionTask;
		
		public CastleFunction(int type, int lvl, int lease, int tempLease, long rate, long time, boolean cwh)
		{
			_type = type;
			_lvl = lvl;
			_fee = lease;
			_tempFee = tempLease;
			_rate = rate;
			_endDate = time;
			initializeTask(cwh);
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
		
		public long getEndTime()
		{
			return _endDate;
		}
		
		public void setLvl(int lvl)
		{
			_lvl = lvl;
		}
		
		public void setLease(int lease)
		{
			_fee = lease;
		}
		
		public void setEndTime(long time)
		{
			_endDate = time;
		}
		
		public Future<?> getFunctionTask()
		{
			return _functionTask;
		}
		
		private void initializeTask(boolean cwh)
		{
			if (getOwnerId() <= 0)
			{
				return;
			}
			
			final long currentTime = System.currentTimeMillis();
			if (_endDate > currentTime)
			{
				_functionTask = ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(cwh), _endDate - currentTime);
			}
			else
			{
				_functionTask = ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(cwh), 0);
			}
		}
		
		private class FunctionTask implements Runnable
		{
			public FunctionTask(boolean cwh)
			{
				_cwh = cwh;
			}
			
			@Override
			public void run()
			{
				try
				{
					if (getOwnerId() <= 0)
					{
						return;
					}
					
					if ((ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().getAdena() >= _fee) || !_cwh)
					{
						int fee = _fee;
						boolean newfc = true;
						if ((getEndTime() == 0) || (getEndTime() == -1))
						{
							if (getEndTime() == -1)
							{
								newfc = false;
								fee = _tempFee;
							}
						}
						else
						{
							newfc = false;
						}
						
						setEndTime(System.currentTimeMillis() + getRate());
						dbSave(newfc);
						
						if (_cwh)
						{
							ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().destroyItemByItemId("CS_function_fee", 57, fee, null, null);
							if (Config.DEBUG)
							{
								_log.warning("deducted " + fee + " adena from " + getName() + " owner's cwh for function id : " + getType());
							}
						}
						_functionTask = ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(true), getRate());
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
		
		public void dbSave(boolean newFunction)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				if (newFunction)
				{
					try (PreparedStatement statement = con.prepareStatement("INSERT INTO castle_functions (castle_id, type, lvl, lease, rate, endTime) VALUES (?,?,?,?,?,?)"))
					{
						statement.setInt(1, getCastleId());
						statement.setInt(2, getType());
						statement.setInt(3, getLvl());
						statement.setInt(4, getLease());
						statement.setLong(5, getRate());
						statement.setLong(6, getEndTime());
						statement.execute();
					}
				}
				else
				{
					try (PreparedStatement statement = con.prepareStatement("UPDATE castle_functions SET lvl=?, lease=?, endTime=? WHERE castle_id=? AND type=?"))
					{
						statement.setInt(1, getLvl());
						statement.setInt(2, getLease());
						statement.setLong(3, getEndTime());
						statement.setInt(4, getCastleId());
						statement.setInt(5, getType());
						statement.execute();
					}
				}
			}
			catch (final Exception e)
			{
				_log.severe("Exception: Castle.updateFunctions(int type, int lvl, int lease, long rate, long time, boolean addNew): " + e.getMessage());
			}
		}
	}
	
	// =========================================================
	// Constructor
	public Castle(int castleId)
	{
		_CastleId = castleId;
		
		if (_CastleId == 7)
		{
			_nbArtifact = 2;
		}
		
		load();
		loadDoor();
		
		_function = new FastMap<>();
		if (getOwnerId() != 0)
		{
			loadFunctions();
		}
	}
	
	// =========================================================
	// Method - Public
	
	/**
	 * Return function with id
	 * @param type
	 * @return
	 */
	public CastleFunction getFunction(int type)
	{
		if (_function.get(type) != null)
		{
			return _function.get(type);
		}
		return null;
	}
	
	public void engrave(L2Clan clan, int objId)
	
	{
		_engrave.put(objId, clan.getClanId());
		if (_engrave.size() == _nbArtifact)
		{
			
			for (final int id : _engrave.values())
			{
				if (id != clan.getClanId())
				{
					getSiege().announceToPlayer("Clan " + clan.getName() + " has finished engraving one of the rulers.", true);
					return;
				}
			}
			
			_engrave.clear();
			
			setOwner(clan);
			
		}
		else
		{
			getSiege().announceToPlayer("Clan " + clan.getName() + " has finished engraving one of the rulers.", true);
		}
	}
	
	// This method add to the treasury
	/**
	 * Add amount to castle instance's treasury (warehouse).
	 * @param amount
	 */
	public void addToTreasury(int amount)
	{
		if (getOwnerId() <= 0)
		{
			return;
		}
		
		if (!_Name.equalsIgnoreCase("aden")) // If current castle instance is not Aden
		{
			final Castle aden = CastleManager.getInstance().getCastle("aden");
			if (aden != null)
			{
				final int adenTax = (int) (amount * aden.getTaxRate()); // Find out what Aden gets from the current castle instance's income
				if (aden.getOwnerId() > 0)
				{
					aden.addToTreasury(adenTax); // Only bother to really add the tax to the treasury if not npc owned
				}
				
				amount -= adenTax; // Subtract Aden's income from current castle instance's income
			}
		}
		
		addToTreasuryNoTax(amount);
	}
	
	/**
	 * Add amount to castle instance's treasury (warehouse), no tax paying.
	 * @param amount
	 * @return
	 */
	public boolean addToTreasuryNoTax(int amount)
	{
		if (getOwnerId() <= 0)
		{
			return false;
		}
		
		if (amount < 0)
		{
			amount *= -1;
			if (_Treasury < amount)
			{
				return false;
			}
			_Treasury -= amount;
		}
		else
		{
			if (((long) _Treasury + amount) > Integer.MAX_VALUE)
			{
				_Treasury = Integer.MAX_VALUE;
			}
			else
			{
				_Treasury += amount;
			}
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("Update castle set treasury = ? where id = ?"))
		{
			statement.setInt(1, getTreasury());
			statement.setInt(2, getCastleId());
			statement.execute();
		}
		catch (final Exception e)
		{
		}
		return true;
	}
	
	/**
	 * Move non clan members off castle area and to nearest town.<BR>
	 * <BR>
	 */
	public void banishForeigners()
	{
		_zone.banishForeigners(getOwnerId());
	}
	
	public void closeDoor(L2PcInstance activeChar, int doorId)
	{
		openCloseDoor(activeChar, doorId, false);
	}
	
	public void openDoor(L2PcInstance activeChar, int doorId)
	{
		openCloseDoor(activeChar, doorId, true);
	}
	
	public void openCloseDoor(L2PcInstance activeChar, int doorId, boolean open)
	{
		if (activeChar.getClanId() != getOwnerId())
		{
			return;
		}
		
		final L2DoorInstance door = getDoor(doorId);
		
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
	
	// This method is used to begin removing all castle upgrades
	public void removeUpgrade()
	{
		removeDoorUpgrade();
		for (final Map.Entry<Integer, CastleFunction> fc : _function.entrySet())
		{
			removeFunction(fc.getKey());
		}
		_function.clear();
	}
	
	// This method updates the castle tax rate
	public void setOwner(L2Clan clan)
	{
		// Remove old owner
		if ((getOwnerId() > 0) && ((clan == null) || (clan.getClanId() != getOwnerId())))
		{
			final L2Clan oldOwner = ClanTable.getInstance().getClan(getOwnerId()); // Try to find clan instance
			if (oldOwner != null)
			{
				CastleManager.getInstance().removeCirclet(oldOwner, getCastleId());
				oldOwner.setHasCastle(0); // Unset has castle flag for old owner
				
				Announcements.getInstance().announceToAll(oldOwner.getName() + " has lost " + getName() + " castle!");
			}
			
			for (final Map.Entry<Integer, CastleFunction> fc : _function.entrySet())
			{
				removeFunction(fc.getKey());
			}
			_function.clear();
		}
		
		updateOwnerInDB(clan); // Update in database
		
		if (getSiege().getIsInProgress())
		{
			getSiege().midVictory(); // Mid victory phase of siege
		}
	}
	
	// This method updates the castle tax rate
	public void setTaxPercent(L2PcInstance activeChar, int taxPercent)
	{
		int maxTax;
		switch (SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE))
		{
			case SevenSigns.CABAL_DAWN:
				maxTax = 25;
				break;
			case SevenSigns.CABAL_DUSK:
				maxTax = 5;
				break;
			default: // no owner
				maxTax = 15;
		}
		
		if ((taxPercent < 0) || (taxPercent > maxTax))
		{
			activeChar.sendMessage("Tax value must be between 0 and " + maxTax + ".");
			return;
		}
		
		setTaxPercent(taxPercent);
		activeChar.sendMessage(getName() + " castle tax was changed to " + taxPercent + "%.");
	}
	
	public void setTaxPercent(int taxPercent)
	{
		_TaxPercent = taxPercent;
		_TaxRate = _TaxPercent / 100.0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("Update castle set taxPercent = ? where id = ?"))
		{
			statement.setInt(1, taxPercent);
			statement.setInt(2, getCastleId());
			statement.execute();
		}
		catch (final Exception e)
		{
		}
	}
	
	/**
	 * Respawn all doors on castle grounds<BR>
	 * <BR>
	 */
	public void spawnDoor()
	{
		spawnDoor(false);
	}
	
	/**
	 * Respawn all doors on castle grounds<BR>
	 * <BR>
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
		loadDoorUpgrade(); // Check for any upgrade the doors may have
	}
	
	// This method upgrade door
	public void upgradeDoor(int doorId, int hp, int pDef, int mDef)
	{
		final L2DoorInstance door = getDoor(doorId);
		if (door == null)
		{
			return;
		}
		
		if (door.getDoorId() == doorId)
		{
			door.setCurrentHp(door.getMaxHp() + hp);
			
			saveDoorUpgrade(doorId, hp, pDef, mDef);
			return;
		}
	}
	
	// =========================================================
	// Method - Private
	// This method loads castle
	private void load()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			try (PreparedStatement statement = con.prepareStatement("Select * from castle where id = ?"))
			{
				statement.setInt(1, getCastleId());
				try (ResultSet rs = statement.executeQuery())
				{
					while (rs.next())
					{
						_Name = rs.getString("name");
						
						_SiegeDate = Calendar.getInstance();
						_SiegeDate.setTimeInMillis(rs.getLong("siegeDate"));
						
						_SiegeDayOfWeek = rs.getInt("siegeDayOfWeek");
						if ((_SiegeDayOfWeek < 1) || (_SiegeDayOfWeek > 7))
						{
							_SiegeDayOfWeek = 7;
						}
						
						_SiegeHourOfDay = rs.getInt("siegeHourOfDay");
						if ((_SiegeHourOfDay < 0) || (_SiegeHourOfDay > 23))
						{
							_SiegeHourOfDay = 20;
						}
						
						_TaxPercent = rs.getInt("taxPercent");
						_Treasury = rs.getInt("treasury");
						
					}
				}
			}
			
			_TaxRate = _TaxPercent / 100.0;
			
			try (PreparedStatement statement = con.prepareStatement("Select clan_id from clan_data where hasCastle = ?"))
			{
				statement.setInt(1, getCastleId());
				try (ResultSet rs = statement.executeQuery())
				{
					while (rs.next())
					{
						_OwnerId = rs.getInt("clan_id");
					}
				}
			}
		}
		catch (final Exception e)
		{
			System.out.println("Exception: loadCastleData(): " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/** Load All Functions */
	private void loadFunctions()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("Select * from castle_functions where castle_id = ?"))
		{
			statement.setInt(1, getCastleId());
			try (ResultSet rs = statement.executeQuery())
			{
				while (rs.next())
				{
					_function.put(rs.getInt("type"), new CastleFunction(rs.getInt("type"), rs.getInt("lvl"), rs.getInt("lease"), 0, rs.getLong("rate"), rs.getLong("endTime"), true));
				}
			}
		}
		catch (final Exception e)
		{
			_log.severe("Exception: Castle.loadFunctions(): " + e.getMessage());
		}
	}
	
	/**
	 * Remove function In List and in DB
	 * @param functionType
	 */
	public void removeFunction(int functionType)
	{
		final CastleFunction function = _function.remove(functionType);
		if ((function != null) && (function.getFunctionTask() != null))
		{
			function.getFunctionTask().cancel(false);
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM castle_functions WHERE castle_id=? AND type=?"))
		{
			statement.setInt(1, getCastleId());
			statement.setInt(2, functionType);
			statement.execute();
		}
		catch (final Exception e)
		{
			_log.severe("Exception: Castle.removeFunctions(int functionType): " + e.getMessage());
		}
	}
	
	public boolean updateFunctions(L2PcInstance player, int type, int lvl, int lease, long rate, boolean addNew)
	{
		if (Config.DEBUG)
		{
			_log.warning("Called Castle.updateFunctions(int type, int lvl, int lease, long rate, boolean addNew) Owner : " + getOwnerId());
		}
		
		if (lease > 0)
		{
			if (!player.destroyItemByItemId("Consume", 57, lease, null, true))
			{
				return false;
			}
		}
		
		if (addNew)
		{
			_function.put(type, new CastleFunction(type, lvl, lease, 0, rate, 0, false));
		}
		else
		{
			if ((lvl == 0) && (lease == 0))
			{
				removeFunction(type);
			}
			else
			{
				final int diffLease = lease - _function.get(type).getLease();
				
				if (Config.DEBUG)
				{
					_log.warning("Called Castle.updateFunctions diffLease : " + diffLease);
				}
				
				if (diffLease > 0)
				{
					_function.remove(type);
					_function.put(type, new CastleFunction(type, lvl, lease, 0, rate, -1, false));
				}
				else
				{
					_function.get(type).setLease(lease);
					_function.get(type).setLvl(lvl);
					_function.get(type).dbSave(false);
				}
			}
		}
		return true;
	}
	
	// This method loads castle door data from database
	private void loadDoor()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("Select * from castle_door where castleId = ?"))
		{
			statement.setInt(1, getCastleId());
			try (ResultSet rs = statement.executeQuery())
			{
				while (rs.next())
				{
					// Create list of the door default for use when respawning dead doors
					_DoorDefault.add(rs.getString("name") + ";" + rs.getInt("id") + ";" + rs.getInt("x") + ";" + rs.getInt("y") + ";" + rs.getInt("z") + ";" + rs.getInt("range_xmin") + ";" + rs.getInt("range_ymin") + ";" + rs.getInt("range_zmin") + ";" + rs.getInt("range_xmax") + ";" + rs.getInt("range_ymax") + ";" + rs.getInt("range_zmax")
						
						+ ";" + rs.getInt("hp") + ";" + rs.getInt("pDef") + ";" + rs.getInt("mDef"));
					
					final L2DoorInstance door = DoorTable.parseList(_DoorDefault.get(_DoorDefault.size() - 1));
					door.spawnMe(door.getX(), door.getY(), door.getZ());
					_Doors.add(door);
					DoorTable.getInstance().putDoor(door);
				}
			}
		}
		catch (final Exception e)
		{
			System.out.println("Exception: loadCastleDoor(): " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	// This method loads castle door upgrade data from database
	private void loadDoorUpgrade()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("Select * from castle_doorupgrade where doorId in (Select Id from castle_door where castleId = ?)"))
		{
			statement.setInt(1, getCastleId());
			try (ResultSet rs = statement.executeQuery())
			{
				while (rs.next())
				{
					upgradeDoor(rs.getInt("id"), rs.getInt("hp"), rs.getInt("pDef"), rs.getInt("mDef"));
				}
			}
		}
		catch (final Exception e)
		{
			System.out.println("Exception: loadCastleDoorUpgrade(): " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void removeDoorUpgrade()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("delete from castle_doorupgrade where doorId in (select id from castle_door where castleId=?)"))
		{
			statement.setInt(1, getCastleId());
			statement.execute();
		}
		catch (final Exception e)
		{
			System.out.println("Exception: removeDoorUpgrade(): " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void saveDoorUpgrade(int doorId, int hp, int pDef, int mDef)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO castle_doorupgrade (doorId, hp, pDef, mDef) values (?,?,?,?)"))
		{
			statement.setInt(1, doorId);
			statement.setInt(2, hp);
			statement.setInt(3, pDef);
			statement.setInt(4, mDef);
			statement.execute();
		}
		catch (final Exception e)
		{
			System.out.println("Exception: saveDoorUpgrade(int doorId, int hp, int pDef, int mDef): " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void updateOwnerInDB(L2Clan clan)
	{
		if (clan != null)
		{
			_OwnerId = clan.getClanId(); // Update owner id property
		}
		else
		{
			_OwnerId = 0; // Remove owner
			resetManor();
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			// NEED TO REMOVE HAS CASTLE FLAG FROM CLAN_DATA
			// SHOULD BE CHECKED FROM CASTLE TABLE
			try (PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET hasCastle=0 WHERE hasCastle=?"))
			{
				statement.setInt(1, getCastleId());
				statement.execute();
			}
			
			try (PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET hasCastle=? WHERE clan_id=?"))
			{
				statement.setInt(1, getCastleId());
				statement.setInt(2, getOwnerId());
				statement.execute();
			}
			
			// Announce to clan members
			if (clan != null)
			{
				clan.setHasCastle(getCastleId()); // Set has castle flag for new owner
				Announcements.getInstance().announceToAll(clan.getName() + " has taken " + getName() + " castle!");
				
				for (final L2ClanMember member : clan.getMembers())
				
				{
					
					if (member.isOnline() && (member.getPlayerInstance() != null))
					
					{
						
						member.getPlayerInstance().sendPacket(new PledgeShowInfoUpdate(clan));
						
					}
					
				}
				
				clan.broadcastToOnlineMembers(new PlaySound(1, "Siege_Victory", 0, 0, 0, 0, 0));
			}
		}
		catch (final Exception e)
		{
			System.out.println("Exception: updateOwnerInDB(L2Clan clan): " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	// =========================================================
	// Property
	public final int getCastleId()
	{
		return _CastleId;
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
	
	public final Siege getSiege()
	{
		if (_Siege == null)
		{
			_Siege = new Siege(new Castle[]
			{
				this
			});
		}
		return _Siege;
	}
	
	public final Calendar getSiegeDate()
	{
		return _SiegeDate;
	}
	
	public final int getSiegeDayOfWeek()
	{
		return _SiegeDayOfWeek;
	}
	
	public final int getSiegeHourOfDay()
	{
		return _SiegeHourOfDay;
	}
	
	public final int getTaxPercent()
	{
		return _TaxPercent;
	}
	
	public final double getTaxRate()
	{
		return _TaxRate;
	}
	
	public final int getTreasury()
	{
		return _Treasury;
	}
	
	public boolean checkIfInZone(int x, int y, int z)
	{
		return _zone.isInsideZone(x, y, z);
	}
	
	public void setZone(L2SiegeZone zone)
	{
		_zone = zone;
	}
	
	public L2SiegeZone getZone()
	{
		return _zone;
	}
	
	public void setTeleZone(L2CastleTeleportZone zone)
	{
		_teleZone = zone;
	}
	
	public L2CastleTeleportZone getTeleZone()
	{
		return _teleZone;
	}
	
	public void oustAllPlayers()
	{
		getTeleZone().oustAllPlayers();
	}
	
	public double getDistance(L2Object obj)
	{
		return _zone.getDistanceToZone(obj);
	}
	
	public FastList<SeedProduction> getSeedProduction(int period)
	{
		return (period == CastleManorManager.PERIOD_CURRENT ? _production : _productionNext);
	}
	
	public FastList<CropProcure> getCropProcure(int period)
	{
		return (period == CastleManorManager.PERIOD_CURRENT ? _procure : _procureNext);
	}
	
	public void setSeedProduction(FastList<SeedProduction> seed, int period)
	{
		if (period == CastleManorManager.PERIOD_CURRENT)
		{
			_production = seed;
		}
		else
		{
			_productionNext = seed;
		}
	}
	
	public void setCropProcure(FastList<CropProcure> crop, int period)
	{
		if (period == CastleManorManager.PERIOD_CURRENT)
		{
			_procure = crop;
		}
		else
		{
			_procureNext = crop;
		}
	}
	
	public synchronized SeedProduction getSeed(int seedId, int period)
	{
		for (final SeedProduction seed : getSeedProduction(period))
		{
			if (seed.getId() == seedId)
			{
				return seed;
			}
		}
		
		return null;
	}
	
	public synchronized CropProcure getCrop(int cropId, int period)
	{
		for (final CropProcure crop : getCropProcure(period))
		{
			if (crop.getId() == cropId)
			{
				return crop;
			}
		}
		return null;
	}
	
	public int getManorCost(int period)
	{
		FastList<CropProcure> procure;
		FastList<SeedProduction> production;
		
		if (period == CastleManorManager.PERIOD_CURRENT)
		{
			procure = _procure;
			production = _production;
		}
		else
		{
			procure = _procureNext;
			production = _productionNext;
		}
		
		int total = 0;
		if (production != null)
		{
			for (final SeedProduction seed : production)
			{
				total += L2Manor.getInstance().getSeedBuyPrice(seed.getId()) * seed.getStartProduce();
			}
		}
		
		if (procure != null)
		{
			for (final CropProcure crop : procure)
			{
				total += crop.getPrice() * crop.getStartAmount();
			}
		}
		return total;
	}
	
	// Save manor production data
	public void saveSeedData()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			try (PreparedStatement statement = con.prepareStatement(CASTLE_MANOR_DELETE_PRODUCTION))
			{
				statement.setInt(1, getCastleId());
				statement.execute();
			}
			
			if (_production != null)
			{
				int count = 0;
				String query = "INSERT INTO castle_manor_production VALUES ";
				final String values[] = new String[_production.size()];
				for (final SeedProduction s : _production)
				{
					values[count] = "(" + getCastleId() + "," + s.getId() + "," + s.getCanProduce() + "," + s.getStartProduce() + "," + s.getPrice() + "," + CastleManorManager.PERIOD_CURRENT + ")";
					count++;
				}
				
				if (values.length > 0)
				{
					query += values[0];
					for (int i = 1; i < values.length; i++)
					{
						query += "," + values[i];
					}
					
					try (PreparedStatement statement = con.prepareStatement(query))
					{
						statement.execute();
					}
				}
			}
			
			if (_productionNext != null)
			{
				int count = 0;
				String query = "INSERT INTO castle_manor_production VALUES ";
				final String values[] = new String[_productionNext.size()];
				for (final SeedProduction s : _productionNext)
				{
					values[count] = "(" + getCastleId() + "," + s.getId() + "," + s.getCanProduce() + "," + s.getStartProduce() + "," + s.getPrice() + "," + CastleManorManager.PERIOD_NEXT + ")";
					count++;
				}
				
				if (values.length > 0)
				{
					query += values[0];
					for (int i = 1; i < values.length; i++)
					{
						query += "," + values[i];
					}
					
					try (PreparedStatement statement = con.prepareStatement(query))
					{
						statement.execute();
					}
				}
			}
		}
		catch (final Exception e)
		{
			_log.info("Error adding seed production data for castle " + getName() + ": " + e.getMessage());
		}
	}
	
	// Save manor production data for specified period
	public void saveSeedData(int period)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			try (PreparedStatement statement = con.prepareStatement(CASTLE_MANOR_DELETE_PRODUCTION_PERIOD))
			{
				statement.setInt(1, getCastleId());
				statement.setInt(2, period);
				statement.execute();
			}
			
			FastList<SeedProduction> prod = null;
			prod = getSeedProduction(period);
			
			if (prod != null)
			{
				int count = 0;
				String query = "INSERT INTO castle_manor_production VALUES ";
				final String values[] = new String[prod.size()];
				for (final SeedProduction s : prod)
				{
					values[count] = "(" + getCastleId() + "," + s.getId() + "," + s.getCanProduce() + "," + s.getStartProduce() + "," + s.getPrice() + "," + period + ")";
					count++;
				}
				
				if (values.length > 0)
				{
					query += values[0];
					for (int i = 1; i < values.length; i++)
					{
						query += "," + values[i];
					}
					
					try (PreparedStatement statement = con.prepareStatement(query))
					{
						statement.execute();
					}
				}
			}
		}
		catch (final Exception e)
		{
			_log.info("Error adding seed production data for castle " + getName() + ": " + e.getMessage());
		}
	}
	
	// Save crop procure data
	public void saveCropData()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			try (PreparedStatement statement = con.prepareStatement(CASTLE_MANOR_DELETE_PROCURE))
			{
				statement.setInt(1, getCastleId());
				statement.execute();
			}
			
			if ((_procure != null) && (_procure.size() > 0))
			{
				int count = 0;
				String query = "INSERT INTO castle_manor_procure VALUES ";
				final String values[] = new String[_procure.size()];
				for (final CropProcure cp : _procure)
				{
					values[count] = "(" + getCastleId() + "," + cp.getId() + "," + cp.getAmount() + "," + cp.getStartAmount() + "," + cp.getPrice() + "," + cp.getReward() + "," + CastleManorManager.PERIOD_CURRENT + ")";
					count++;
				}
				
				if (values.length > 0)
				{
					query += values[0];
					for (int i = 1; i < values.length; i++)
					{
						query += "," + values[i];
					}
					
					try (PreparedStatement statement = con.prepareStatement(query))
					{
						statement.execute();
					}
				}
			}
			
			if ((_procureNext != null) && (_procureNext.size() > 0))
			{
				int count = 0;
				String query = "INSERT INTO castle_manor_procure VALUES ";
				final String values[] = new String[_procureNext.size()];
				for (final CropProcure cp : _procureNext)
				{
					values[count] = "(" + getCastleId() + "," + cp.getId() + "," + cp.getAmount() + "," + cp.getStartAmount() + "," + cp.getPrice() + "," + cp.getReward() + "," + CastleManorManager.PERIOD_NEXT + ")";
					count++;
				}
				
				if (values.length > 0)
				{
					query += values[0];
					for (int i = 1; i < values.length; i++)
					{
						query += "," + values[i];
					}
					
					try (PreparedStatement statement = con.prepareStatement(query))
					{
						statement.execute();
					}
				}
			}
		}
		catch (final Exception e)
		{
			_log.info("Error adding crop data for castle " + getName() + ": " + e.getMessage());
		}
	}
	
	// Save crop procure data for specified period
	public void saveCropData(int period)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			try (PreparedStatement statement = con.prepareStatement(CASTLE_MANOR_DELETE_PROCURE_PERIOD))
			{
				statement.setInt(1, getCastleId());
				statement.setInt(2, period);
				statement.execute();
			}
			
			FastList<CropProcure> proc = null;
			proc = getCropProcure(period);
			
			if ((proc != null) && (proc.size() > 0))
			{
				int count = 0;
				String query = "INSERT INTO castle_manor_procure VALUES ";
				final String values[] = new String[proc.size()];
				
				for (final CropProcure cp : proc)
				{
					values[count] = "(" + getCastleId() + "," + cp.getId() + "," + cp.getAmount() + "," + cp.getStartAmount() + "," + cp.getPrice() + "," + cp.getReward() + "," + period + ")";
					count++;
				}
				
				if (values.length > 0)
				{
					query += values[0];
					for (int i = 1; i < values.length; i++)
					{
						query += "," + values[i];
					}
					
					try (PreparedStatement statement = con.prepareStatement(query))
					{
						statement.execute();
					}
				}
			}
		}
		catch (final Exception e)
		{
			_log.info("Error adding crop data for castle " + getName() + ": " + e.getMessage());
		}
	}
	
	public void updateCrop(int cropId, int amount, int period)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(CASTLE_UPDATE_CROP))
		{
			statement.setInt(1, amount);
			statement.setInt(2, cropId);
			statement.setInt(3, getCastleId());
			statement.setInt(4, period);
			statement.execute();
		}
		catch (final Exception e)
		{
			_log.info("Error adding crop data for castle " + getName() + ": " + e.getMessage());
		}
	}
	
	public void updateSeed(int seedId, int amount, int period)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(CASTLE_UPDATE_SEED))
		{
			statement.setInt(1, amount);
			statement.setInt(2, seedId);
			statement.setInt(3, getCastleId());
			statement.setInt(4, period);
			statement.execute();
		}
		catch (final Exception e)
		{
			_log.info("Error adding seed production data for castle " + getName() + ": " + e.getMessage());
		}
	}
	
	public boolean isNextPeriodApproved()
	{
		return _isNextPeriodApproved;
	}
	
	public void setNextPeriodApproved(boolean val)
	{
		_isNextPeriodApproved = val;
	}
	
	public void resetManor()
	{
		setCropProcure(new FastList<CropProcure>(), CastleManorManager.PERIOD_CURRENT);
		setCropProcure(new FastList<CropProcure>(), CastleManorManager.PERIOD_NEXT);
		setSeedProduction(new FastList<SeedProduction>(), CastleManorManager.PERIOD_CURRENT);
		setSeedProduction(new FastList<SeedProduction>(), CastleManorManager.PERIOD_NEXT);
		if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
		{
			saveCropData();
			saveSeedData();
		}
	}
}