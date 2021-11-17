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
import org.l2jmobius.gameserver.instancemanager.GrandBossManager;
import org.l2jmobius.gameserver.instancemanager.RaidBossPointsManager;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.spawn.Spawn;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * This class manages all Grand Bosses.
 * @version $Revision: 1.0.0.0 $ $Date: 2006/06/16 $
 */
public class GrandBoss extends Monster
{
	/**
	 * Constructor for GrandBoss. This represent all grandbosses.
	 * @param objectId ID of the instance
	 * @param template NpcTemplate of the instance
	 */
	public GrandBoss(int objectId, NpcTemplate template)
	{
		super(objectId, template);
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
		return true;
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		GrandBossManager.getInstance().addBoss(this);
	}
	
	@Override
	protected void manageMinions()
	{
		_minionList.spawnMinions();
		_minionMaintainTask = ThreadPool.scheduleAtFixedRate(() ->
		{
			// Teleport raid boss home if it's too far from home location
			final Spawn bossSpawn = getSpawn();
			int rbLockRange = Config.RBLOCKRAGE;
			if (Config.RBS_SPECIFIC_LOCK_RAGE.get(bossSpawn.getNpcId()) != null)
			{
				rbLockRange = Config.RBS_SPECIFIC_LOCK_RAGE.get(bossSpawn.getNpcId());
			}
			
			if ((rbLockRange >= 100) && !isInsideRadius3D(bossSpawn.getX(), bossSpawn.getY(), bossSpawn.getZ(), rbLockRange))
			{
				teleToLocation(bossSpawn.getX(), bossSpawn.getY(), bossSpawn.getZ(), true);
				// healFull(); // Prevents minor exploiting with it
			}
			
			_minionList.maintainMinions();
		}, 60000, 20000);
	}
	
	public void healFull()
	{
		super.setCurrentHp(super.getMaxHp());
		super.setCurrentMp(super.getMaxMp());
	}
	
	@Override
	public boolean isRandomAnimationEnabled()
	{
		return false;
	}
	
	@Override
	public boolean isRandomWalkingEnabled()
	{
		return false;
	}
	
	@Override
	public boolean isRaid()
	{
		return true;
	}
}
