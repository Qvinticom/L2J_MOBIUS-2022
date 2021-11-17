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
package org.l2jmobius.gameserver.model;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.ai.ControllableMobAI;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.data.MobGroupTable;
import org.l2jmobius.gameserver.data.sql.SpawnTable;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.ControllableMob;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.spawn.GroupSpawn;

/**
 * @author littlecrow
 */
public class MobGroup
{
	private final NpcTemplate _npcTemplate;
	private final int _groupId;
	private final int _maxMobCount;
	
	private List<ControllableMob> _mobs;
	
	public MobGroup(int groupId, NpcTemplate npcTemplate, int maxMobCount)
	{
		_groupId = groupId;
		_npcTemplate = npcTemplate;
		_maxMobCount = maxMobCount;
	}
	
	public int getActiveMobCount()
	{
		return getMobs().size();
	}
	
	public int getGroupId()
	{
		return _groupId;
	}
	
	public int getMaxMobCount()
	{
		return _maxMobCount;
	}
	
	public List<ControllableMob> getMobs()
	{
		if (_mobs == null)
		{
			_mobs = new ArrayList<>();
		}
		return _mobs;
	}
	
	public String getStatus()
	{
		try
		{
			final ControllableMobAI mobGroupAI = (ControllableMobAI) getMobs().get(0).getAI();
			
			switch (mobGroupAI.getAlternateAI())
			{
				case ControllableMobAI.AI_NORMAL:
				{
					return "Idle";
				}
				case ControllableMobAI.AI_FORCEATTACK:
				{
					return "Force Attacking";
				}
				case ControllableMobAI.AI_FOLLOW:
				{
					return "Following";
				}
				case ControllableMobAI.AI_CAST:
				{
					return "Casting";
				}
				case ControllableMobAI.AI_ATTACK_GROUP:
				{
					return "Attacking Group";
				}
				default:
				{
					return "Idle";
				}
			}
		}
		catch (Exception e)
		{
			return "Unspawned";
		}
	}
	
	public NpcTemplate getTemplate()
	{
		return _npcTemplate;
	}
	
	public boolean isGroupMember(ControllableMob mobInst)
	{
		for (ControllableMob groupMember : getMobs())
		{
			if (groupMember == null)
			{
				continue;
			}
			
			if (groupMember.getObjectId() == mobInst.getObjectId())
			{
				return true;
			}
		}
		
		return false;
	}
	
	public void spawnGroup(int x, int y, int z)
	{
		if (!getMobs().isEmpty())
		{
			return;
		}
		
		try
		{
			for (int i = 0; i < _maxMobCount; i++)
			{
				final GroupSpawn spawn = new GroupSpawn(_npcTemplate);
				final int signX = Rnd.nextBoolean() ? -1 : 1;
				final int signY = Rnd.nextBoolean() ? -1 : 1;
				final int randX = Rnd.get(MobGroupTable.RANDOM_RANGE);
				final int randY = Rnd.get(MobGroupTable.RANDOM_RANGE);
				spawn.setX(x + (signX * randX));
				spawn.setY(y + (signY * randY));
				spawn.setZ(z);
				spawn.stopRespawn();
				
				SpawnTable.getInstance().addNewSpawn(spawn, false);
				getMobs().add((ControllableMob) spawn.doGroupSpawn());
			}
		}
		catch (ClassNotFoundException e)
		{
			// null
		}
		catch (NoSuchMethodException e2)
		{
			// null
		}
	}
	
	public void spawnGroup(Player player)
	{
		spawnGroup(player.getX(), player.getY(), player.getZ());
	}
	
	public void teleportGroup(Player player)
	{
		removeDead();
		
		for (ControllableMob mobInst : getMobs())
		{
			if (mobInst == null)
			{
				continue;
			}
			
			if (!mobInst.isDead())
			{
				final int x = player.getX() + Rnd.get(50);
				final int y = player.getY() + Rnd.get(50);
				mobInst.teleToLocation(x, y, player.getZ(), true);
				final ControllableMobAI ai = (ControllableMobAI) mobInst.getAI();
				ai.follow(player);
			}
		}
	}
	
	public ControllableMob getRandomMob()
	{
		removeDead();
		
		if (getMobs().isEmpty())
		{
			return null;
		}
		
		final int choice = Rnd.get(getMobs().size());
		return getMobs().get(choice);
	}
	
	public void unspawnGroup()
	{
		removeDead();
		
		if (getMobs().isEmpty())
		{
			return;
		}
		
		for (ControllableMob mobInst : getMobs())
		{
			if (mobInst == null)
			{
				continue;
			}
			
			if (!mobInst.isDead())
			{
				mobInst.deleteMe();
			}
			
			SpawnTable.getInstance().deleteSpawn(mobInst.getSpawn(), false);
		}
		
		getMobs().clear();
	}
	
	public void killGroup(Player player)
	{
		removeDead();
		
		for (ControllableMob mobInst : getMobs())
		{
			if (mobInst == null)
			{
				continue;
			}
			
			if (!mobInst.isDead())
			{
				mobInst.reduceCurrentHp(mobInst.getMaxHp() + 1, player);
			}
			
			SpawnTable.getInstance().deleteSpawn(mobInst.getSpawn(), false);
		}
		
		getMobs().clear();
	}
	
	public void setAttackRandom()
	{
		removeDead();
		
		for (ControllableMob mobInst : getMobs())
		{
			if (mobInst == null)
			{
				continue;
			}
			
			final ControllableMobAI ai = (ControllableMobAI) mobInst.getAI();
			ai.setAlternateAI(ControllableMobAI.AI_NORMAL);
			ai.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
	}
	
	public void setAttackTarget(Creature target)
	{
		removeDead();
		
		for (ControllableMob mobInst : getMobs())
		{
			if (mobInst == null)
			{
				continue;
			}
			
			final ControllableMobAI ai = (ControllableMobAI) mobInst.getAI();
			ai.forceAttack(target);
		}
	}
	
	public void setIdleMode()
	{
		removeDead();
		
		for (ControllableMob mobInst : getMobs())
		{
			if (mobInst == null)
			{
				continue;
			}
			
			final ControllableMobAI ai = (ControllableMobAI) mobInst.getAI();
			ai.stop();
		}
	}
	
	public void returnGroup(Creature creature)
	{
		setIdleMode();
		
		for (ControllableMob mobInst : getMobs())
		{
			if (mobInst == null)
			{
				continue;
			}
			
			final int signX = Rnd.nextBoolean() ? -1 : 1;
			final int signY = Rnd.nextBoolean() ? -1 : 1;
			final int randX = Rnd.get(MobGroupTable.RANDOM_RANGE);
			final int randY = Rnd.get(MobGroupTable.RANDOM_RANGE);
			final ControllableMobAI ai = (ControllableMobAI) mobInst.getAI();
			ai.move(creature.getX() + (signX * randX), creature.getY() + (signY * randY), creature.getZ());
		}
	}
	
	public void setFollowMode(Creature creature)
	{
		removeDead();
		
		for (ControllableMob mobInst : getMobs())
		{
			if (mobInst == null)
			{
				continue;
			}
			
			final ControllableMobAI ai = (ControllableMobAI) mobInst.getAI();
			ai.follow(creature);
		}
	}
	
	public void setCastMode()
	{
		removeDead();
		
		for (ControllableMob mobInst : getMobs())
		{
			if (mobInst == null)
			{
				continue;
			}
			
			final ControllableMobAI ai = (ControllableMobAI) mobInst.getAI();
			ai.setAlternateAI(ControllableMobAI.AI_CAST);
		}
	}
	
	public void setNoMoveMode(boolean enabled)
	{
		removeDead();
		
		for (ControllableMob mobInst : getMobs())
		{
			if (mobInst == null)
			{
				continue;
			}
			
			final ControllableMobAI ai = (ControllableMobAI) mobInst.getAI();
			ai.setNotMoving(enabled);
		}
	}
	
	protected void removeDead()
	{
		final List<ControllableMob> deadMobs = new ArrayList<>();
		for (ControllableMob mobInst : getMobs())
		{
			if ((mobInst != null) && mobInst.isDead())
			{
				deadMobs.add(mobInst);
			}
		}
		
		getMobs().removeAll(deadMobs);
	}
	
	public void setInvul(boolean invulState)
	{
		removeDead();
		
		for (ControllableMob mobInst : getMobs())
		{
			if (mobInst != null)
			{
				mobInst.setInvul(invulState);
			}
		}
	}
	
	public void setAttackGroup(MobGroup otherGrp)
	{
		removeDead();
		
		for (ControllableMob mobInst : getMobs())
		{
			if (mobInst == null)
			{
				continue;
			}
			
			final ControllableMobAI ai = (ControllableMobAI) mobInst.getAI();
			ai.forceAttackGroup(otherGrp);
			ai.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
	}
}
