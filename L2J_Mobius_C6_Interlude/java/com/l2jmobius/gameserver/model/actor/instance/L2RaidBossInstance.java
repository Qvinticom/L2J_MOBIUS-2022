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

import com.l2jmobius.Config;
import com.l2jmobius.commons.concurrent.ThreadPoolManager;
import com.l2jmobius.commons.util.Rnd;
import com.l2jmobius.gameserver.instancemanager.RaidBossPointsManager;
import com.l2jmobius.gameserver.instancemanager.RaidBossSpawnManager;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.L2Summon;
import com.l2jmobius.gameserver.model.spawn.L2Spawn;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.templates.chars.L2NpcTemplate;

/**
 * This class manages all RaidBoss. In a group mob, there are one master called RaidBoss and several slaves called Minions.
 * @version $Revision: 1.20.4.6 $ $Date: 2005/04/06 16:13:39 $
 */
public final class L2RaidBossInstance extends L2MonsterInstance
{
	/** The Constant RAIDBOSS_MAINTENANCE_INTERVAL. */
	private static final int RAIDBOSS_MAINTENANCE_INTERVAL = 20000; // 20 sec
	
	/** The _raid status. */
	private RaidBossSpawnManager.StatusEnum _raidStatus;
	
	/**
	 * Constructor of L2RaidBossInstance (use L2Character and L2NpcInstance constructor).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Call the L2Character constructor to set the _template of the L2RaidBossInstance (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR)</li>
	 * <li>Set the name of the L2RaidBossInstance</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it</li><BR>
	 * <BR>
	 * @param objectId Identifier of the object to initialized
	 * @param template the template
	 */
	public L2RaidBossInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.model.L2Character#isRaid()
	 */
	@Override
	public boolean isRaid()
	{
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.model.actor.instance.L2MonsterInstance#getMaintenanceInterval()
	 */
	@Override
	protected int getMaintenanceInterval()
	{
		return RAIDBOSS_MAINTENANCE_INTERVAL;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.model.actor.instance.L2MonsterInstance#doDie(com.l2jmobius.gameserver.model.L2Character)
	 */
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		L2PcInstance player = null;
		
		if (killer instanceof L2PcInstance)
		{
			player = (L2PcInstance) killer;
		}
		else if (killer instanceof L2Summon)
		{
			player = ((L2Summon) killer).getOwner();
		}
		
		if (player != null)
		{
			SystemMessage msg = new SystemMessage(SystemMessageId.RAID_WAS_SUCCESSFUL);
			broadcastPacket(msg);
			if (player.getParty() != null)
			{
				for (L2PcInstance member : player.getParty().getPartyMembers())
				{
					RaidBossPointsManager.addPoints(member, getNpcId(), (getLevel() / 2) + Rnd.get(-5, 5));
				}
			}
			else
			{
				RaidBossPointsManager.addPoints(player, getNpcId(), (getLevel() / 2) + Rnd.get(-5, 5));
			}
		}
		
		if (!getSpawn().is_customBossInstance())
		{
			RaidBossSpawnManager.getInstance().updateStatus(this, true);
		}
		
		return true;
	}
	
	/**
	 * Spawn all minions at a regular interval Also if boss is too far from home location at the time of this check, teleport it home.
	 */
	@Override
	protected void manageMinions()
	{
		_minionList.spawnMinions();
		_minionMaintainTask = ThreadPoolManager.scheduleAtFixedRate(() ->
		{
			// teleport raid boss home if it's too far from home location
			L2Spawn bossSpawn = getSpawn();
			
			int rb_lock_range = Config.RBLOCKRAGE;
			if (Config.RBS_SPECIFIC_LOCK_RAGE.get(bossSpawn.getNpcId()) != null)
			{
				rb_lock_range = Config.RBS_SPECIFIC_LOCK_RAGE.get(bossSpawn.getNpcId());
			}
			
			if ((rb_lock_range != -1) && !isInsideRadius(bossSpawn.getX(), bossSpawn.getY(), bossSpawn.getZ(), rb_lock_range, true, false))
			{
				teleToLocation(bossSpawn.getX(), bossSpawn.getY(), bossSpawn.getZ(), true);
				// healFull(); // Prevents minor exploiting with it
			}
			/*
			 * if(!isInsideRadius(bossSpawn.getX(), bossSpawn.getY(), bossSpawn.getZ(), 5000, true, false)) { teleToLocation(bossSpawn.getX(), bossSpawn.getY(), bossSpawn.getZ(), true); healFull(); // prevents minor exploiting with it }
			 */
			_minionList.maintainMinions();
		}, 60000, getMaintenanceInterval());
	}
	
	/**
	 * Sets the raid status.
	 * @param status the new raid status
	 */
	public void setRaidStatus(RaidBossSpawnManager.StatusEnum status)
	{
		_raidStatus = status;
	}
	
	/**
	 * Gets the raid status.
	 * @return the raid status
	 */
	public RaidBossSpawnManager.StatusEnum getRaidStatus()
	{
		return _raidStatus;
	}
	
	/**
	 * Heal full.
	 */
	public void healFull()
	{
		super.setCurrentHp(super.getMaxHp());
		super.setCurrentMp(super.getMaxMp());
	}
}
