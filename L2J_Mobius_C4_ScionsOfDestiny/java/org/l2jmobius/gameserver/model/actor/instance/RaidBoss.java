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

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.enums.RaidBossStatus;
import org.l2jmobius.gameserver.instancemanager.RaidBossPointsManager;
import org.l2jmobius.gameserver.instancemanager.RaidBossSpawnManager;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.spawn.Spawn;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * This class manages all RaidBoss. In a group mob, there are one master called RaidBoss and several slaves called Minions.
 * @version $Revision: 1.20.4.6 $ $Date: 2005/04/06 16:13:39 $
 */
public class RaidBoss extends Monster
{
	private RaidBossStatus _raidStatus;
	
	/**
	 * Constructor of RaidBoss (use Creature and Npc constructor).<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Call the Creature constructor to set the _template of the RaidBoss (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR)</li>
	 * <li>Set the name of the RaidBoss</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it</li><br>
	 * @param objectId Identifier of the object to initialized
	 * @param template the template
	 */
	public RaidBoss(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public boolean isRaid()
	{
		return true;
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		Player player = null;
		if (killer instanceof Player)
		{
			player = (Player) killer;
		}
		else if (killer instanceof Summon)
		{
			player = ((Summon) killer).getOwner();
		}
		
		if (player != null)
		{
			broadcastPacket(new SystemMessage(SystemMessageId.CONGRATULATIONS_YOUR_RAID_WAS_SUCCESSFUL));
			if (player.getParty() != null)
			{
				for (Player member : player.getParty().getPartyMembers())
				{
					RaidBossPointsManager.addPoints(member, getNpcId(), (getLevel() / 2) + Rnd.get(-5, 5));
				}
			}
			else
			{
				RaidBossPointsManager.addPoints(player, getNpcId(), (getLevel() / 2) + Rnd.get(-5, 5));
			}
		}
		
		RaidBossSpawnManager.getInstance().updateStatus(this, true);
		return true;
	}
	
	/**
	 * Spawn all minions at a regular interval Also if boss is too far from home location at the time of this check, teleport it home.
	 */
	@Override
	protected void manageMinions()
	{
		_minionList.spawnMinions();
		_minionMaintainTask = ThreadPool.scheduleAtFixedRate(() ->
		{
			// teleport raid boss home if it's too far from home location
			final Spawn bossSpawn = getSpawn();
			int rbLockRange = Config.RBLOCKRAGE;
			if (Config.RBS_SPECIFIC_LOCK_RAGE.get(bossSpawn.getNpcId()) != null)
			{
				rbLockRange = Config.RBS_SPECIFIC_LOCK_RAGE.get(bossSpawn.getNpcId());
			}
			
			if ((rbLockRange != -1) && !isInsideRadius3D(bossSpawn.getX(), bossSpawn.getY(), bossSpawn.getZ(), rbLockRange))
			{
				teleToLocation(bossSpawn.getX(), bossSpawn.getY(), bossSpawn.getZ(), true);
				// healFull(); // Prevents minor exploiting with it
			}
			
			_minionList.maintainMinions();
		}, 60000, 20000);
	}
	
	/**
	 * Sets the raid status.
	 * @param status the new raid status
	 */
	public void setRaidStatus(RaidBossStatus status)
	{
		_raidStatus = status;
	}
	
	/**
	 * Gets the raid status.
	 * @return the raid status
	 */
	public RaidBossStatus getRaidStatus()
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
