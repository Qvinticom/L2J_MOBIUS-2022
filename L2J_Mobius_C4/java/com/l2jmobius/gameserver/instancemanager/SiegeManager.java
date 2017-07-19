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
package com.l2jmobius.gameserver.instancemanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.L2DatabaseFactory;
import com.l2jmobius.gameserver.datatables.SkillTable;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2Clan;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.entity.Castle;
import com.l2jmobius.gameserver.model.entity.Siege;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

import javolution.util.FastList;
import javolution.util.FastMap;

public class SiegeManager
{
	private static Logger _log = Logger.getLogger(SiegeManager.class.getName());
	
	// =========================================================
	private static SiegeManager _Instance;
	
	public static final SiegeManager getInstance()
	{
		if (_Instance == null)
		{
			System.out.println("Initializing SiegeManager");
			_Instance = new SiegeManager();
			_Instance.load();
		}
		return _Instance;
	}
	
	// =========================================================
	// Data Field
	private int _Attacker_Max_Clans = 500; // Max number of clans
	private int _Attacker_RespawnDelay = 0; // Time in ms. Changeable in siege.config
	private int _Defender_Max_Clans = 500; // Max number of clans
	
	// Siege settings
	private FastMap<Integer, FastList<SiegeSpawn>> _artefactSpawnList;
	private FastMap<Integer, FastList<SiegeSpawn>> _controlTowerSpawnList;
	
	private int _Flag_MaxCount = 1; // Changeable in siege.config
	private int _Siege_Clan_MinLevel = 4; // Changeable in siege.config
	private int _Siege_Length = 120; // Time in minute. Changeable in siege.config
	
	// =========================================================
	// Constructor
	public SiegeManager()
	{
	}
	
	// =========================================================
	// Method - Public
	public final void addSiegeSkills(L2PcInstance character)
	{
		character.addSkill(SkillTable.getInstance().getInfo(246, 1), false);
		character.addSkill(SkillTable.getInstance().getInfo(247, 1), false);
	}
	
	/**
	 * Return true if character summon<BR>
	 * <BR>
	 * @param activeChar The L2Character of the character can summon
	 * @param isCheckOnly
	 * @return
	 */
	public final boolean checkIfOkToSummon(L2Character activeChar, boolean isCheckOnly)
	{
		if ((activeChar == null) || !(activeChar instanceof L2PcInstance))
		{
			return false;
		}
		
		final SystemMessage sm = new SystemMessage(614);
		final L2PcInstance player = (L2PcInstance) activeChar;
		
		if (getSiege(activeChar) == null)
		{
			sm.addString("You may only summon this in a siege battlefield.");
		}
		else if ((player.getClanId() != 0) && (getSiege(activeChar).getAttackerClan(player.getClanId()) == null))
		{
			sm.addString("Only attackers have the right to use this skill.");
		}
		else
		{
			return true;
		}
		
		if (!isCheckOnly)
		{
			player.sendPacket(sm);
		}
		
		return false;
	}
	
	/**
	 * Return true if the clan is registered or owner of a castle<BR>
	 * <BR>
	 * @param clan The L2Clan of the player
	 * @param castleId
	 * @return
	 */
	public final boolean checkIsRegistered(L2Clan clan, int castleId)
	{
		if (clan == null)
		{
			return false;
		}
		
		if (clan.getHasCastle() > 0)
		{
			return true;
		}
		
		boolean register = false;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT clan_id FROM siege_clans where clan_id=? and castle_id=?"))
		{
			statement.setInt(1, clan.getClanId());
			statement.setInt(2, castleId);
			try (ResultSet rs = statement.executeQuery())
			{
				while (rs.next())
				{
					register = true;
					break;
				}
			}
		}
		catch (final Exception e)
		{
			System.out.println("Exception: checkIsRegistered(): " + e.getMessage());
			e.printStackTrace();
		}
		return register;
	}
	
	public final void removeSiegeSkills(L2PcInstance character)
	{
		character.removeSkill(SkillTable.getInstance().getInfo(246, 1));
		character.removeSkill(SkillTable.getInstance().getInfo(247, 1));
	}
	
	// =========================================================
	// Method - Private
	private final void load()
	{
		final Properties siegeSettings = new Properties();
		try (InputStream is = new FileInputStream(new File(Config.SIEGE_CONFIGURATION_FILE)))
		{
			siegeSettings.load(is);
		}
		catch (final Exception e)
		{
			System.err.println("Error while loading siege data.");
			e.printStackTrace();
		}
		
		// Siege setting
		_Attacker_Max_Clans = Integer.decode(siegeSettings.getProperty("AttackerMaxClans", "500"));
		_Attacker_RespawnDelay = Integer.decode(siegeSettings.getProperty("AttackerRespawn", "0"));
		
		_Defender_Max_Clans = Integer.decode(siegeSettings.getProperty("DefenderMaxClans", "500"));
		
		_Flag_MaxCount = Integer.decode(siegeSettings.getProperty("MaxFlags", "1"));
		_Siege_Clan_MinLevel = Integer.decode(siegeSettings.getProperty("SiegeClanMinLevel", "4"));
		_Siege_Length = Integer.decode(siegeSettings.getProperty("SiegeLength", "120"));
		
		// Siege spawns settings
		_controlTowerSpawnList = new FastMap<>();
		_artefactSpawnList = new FastMap<>();
		
		for (final Castle castle : CastleManager.getInstance().getCastles())
		{
			final FastList<SiegeSpawn> _controlTowersSpawns = new FastList<>();
			
			for (int i = 1; i < 0xFF; i++)
			{
				final String _spawnParams = siegeSettings.getProperty(castle.getName() + "ControlTower" + Integer.toString(i), "");
				
				if (_spawnParams.length() == 0)
				{
					break;
				}
				
				final StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");
				
				try
				{
					final int x = Integer.parseInt(st.nextToken());
					final int y = Integer.parseInt(st.nextToken());
					final int z = Integer.parseInt(st.nextToken());
					final int npc_id = Integer.parseInt(st.nextToken());
					final int hp = Integer.parseInt(st.nextToken());
					
					_controlTowersSpawns.add(new SiegeSpawn(castle.getCastleId(), x, y, z, 0, npc_id, hp));
				}
				catch (final Exception e)
				{
					_log.warning("Error while loading control tower(s) for " + castle.getName() + " castle.");
				}
			}
			
			final FastList<SiegeSpawn> _artefactSpawns = new FastList<>();
			
			for (int i = 1; i < 0xFF; i++)
			{
				final String _spawnParams = siegeSettings.getProperty(castle.getName() + "Artefact" + Integer.toString(i), "");
				
				if (_spawnParams.length() == 0)
				{
					break;
				}
				
				final StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");
				
				try
				{
					final int x = Integer.parseInt(st.nextToken());
					final int y = Integer.parseInt(st.nextToken());
					final int z = Integer.parseInt(st.nextToken());
					final int heading = Integer.parseInt(st.nextToken());
					final int npc_id = Integer.parseInt(st.nextToken());
					
					_artefactSpawns.add(new SiegeSpawn(castle.getCastleId(), x, y, z, heading, npc_id));
				}
				catch (final Exception e)
				{
					_log.warning("Error while loading artefact(s) for " + castle.getName() + " castle.");
				}
			}
			
			_controlTowerSpawnList.put(castle.getCastleId(), _controlTowersSpawns);
			_artefactSpawnList.put(castle.getCastleId(), _artefactSpawns);
		}
	}
	
	// =========================================================
	// Property - Public
	
	public final FastList<SiegeSpawn> getArtefactSpawnList(int _castleId)
	{
		if (_artefactSpawnList.containsKey(_castleId))
		{
			return _artefactSpawnList.get(_castleId);
		}
		return null;
	}
	
	public final FastList<SiegeSpawn> getControlTowerSpawnList(int _castleId)
	{
		if (_controlTowerSpawnList.containsKey(_castleId))
		{
			return _controlTowerSpawnList.get(_castleId);
		}
		return null;
	}
	
	public final int getAttackerMaxClans()
	{
		return _Attacker_Max_Clans;
	}
	
	public final int getAttackerRespawnDelay()
	{
		return _Attacker_RespawnDelay;
	}
	
	public final int getDefenderMaxClans()
	{
		return _Defender_Max_Clans;
	}
	
	public final int getFlagMaxCount()
	{
		return _Flag_MaxCount;
	}
	
	public final Siege getSiege(L2Object activeObject)
	{
		return getSiege(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}
	
	public final Siege getSiege(int x, int y, int z)
	{
		for (final Castle castle : CastleManager.getInstance().getCastles())
		{
			if (castle.getSiege().checkIfInZone(x, y, z))
			{
				return castle.getSiege();
			}
		}
		return null;
	}
	
	public final int getSiegeClanMinLevel()
	{
		return _Siege_Clan_MinLevel;
	}
	
	public final int getSiegeLength()
	{
		return _Siege_Length;
	}
	
	public final List<Siege> getSieges()
	{
		final FastList<Siege> sieges = new FastList<>();
		for (final Castle castle : CastleManager.getInstance().getCastles())
		{
			sieges.add(castle.getSiege());
		}
		return sieges;
	}
	
	public class SiegeSpawn
	{
		Location _location;
		private final int _npc_id;
		private final int _heading;
		private final int _castle_id;
		private int _hp;
		
		public SiegeSpawn(int castle_id, int x, int y, int z, int heading, int npc_id)
		{
			_castle_id = castle_id;
			_location = new Location(x, y, z, heading);
			_heading = heading;
			_npc_id = npc_id;
		}
		
		public SiegeSpawn(int castle_id, int x, int y, int z, int heading, int npc_id, int hp)
		{
			_castle_id = castle_id;
			_location = new Location(x, y, z, heading);
			_heading = heading;
			_npc_id = npc_id;
			_hp = hp;
		}
		
		public int getCastleId()
		{
			return _castle_id;
		}
		
		public int getNpcId()
		{
			return _npc_id;
		}
		
		public int getHeading()
		{
			return _heading;
		}
		
		public int getHp()
		{
			return _hp;
		}
		
		public Location getLocation()
		{
			return _location;
		}
	}
}