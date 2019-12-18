/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.gameserver.model.actor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.enums.CreatureState;
import org.l2jmobius.gameserver.model.DropData;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.serverpackets.DropItem;
import org.l2jmobius.gameserver.templates.Npc;
import org.l2jmobius.gameserver.templates.Weapon;
import org.l2jmobius.gameserver.threadpool.ThreadPool;
import org.l2jmobius.util.Rnd;

public class Attackable extends NpcInstance
{
	// private int _moveRadius;
	private boolean _active;
	private ScheduledFuture<?> _currentAiTask;
	private ScheduledFuture<?> _currentAIAttackTask;
	private final Map<WorldObject, Integer> _aggroList = new HashMap<>();
	private Weapon _dummyWeapon;
	private boolean _sweepActive;
	private boolean _killedAlready = false;
	
	public Attackable(Npc template)
	{
		super(template);
	}
	
	public boolean getCondition2(PlayerInstance player)
	{
		return false;
	}
	
	@Override
	public void onTargetReached()
	{
		super.onTargetReached();
		switch (getCurrentState())
		{
			case ATTACKING:
			{
				startCombat();
				break;
			}
			case RANDOM_WALK:
			{
				randomWalk();
				break;
			}
		}
	}
	
	private void randomWalk()
	{
		if (_active)
		{
			final int wait = (10 + Rnd.get(120)) * 1000;
			_currentAiTask = ThreadPool.schedule(new AITask(this), wait);
		}
	}
	
	protected void startRandomWalking()
	{
		final int wait = (10 + Rnd.get(120)) * 1000;
		_currentAiTask = ThreadPool.schedule(new AITask(this), wait);
		setCurrentState(CreatureState.RANDOM_WALK);
	}
	
	protected synchronized void startTargetScan()
	{
		if ((_currentAIAttackTask == null) && (getTarget() == null))
		{
			_currentAIAttackTask = ThreadPool.scheduleAtFixedRate(new AIAttackeTask(this), 100, 1000);
		}
	}
	
	@Override
	public void startAttack(Creature target)
	{
		stopTargetScan();
		super.startAttack(target);
	}
	
	@Override
	public void removeKnownObject(WorldObject object)
	{
		super.removeKnownObject(object);
		if (object instanceof Creature)
		{
			_aggroList.remove(object);
		}
	}
	
	@Override
	public void reduceCurrentHp(int damage, Creature attacker)
	{
		super.reduceCurrentHp(damage, attacker);
		calculateAggro(damage, attacker);
		if (!isDead() && (attacker != null))
		{
			if (!isInCombat())
			{
				stopRandomWalking();
				startAttack(attacker);
			}
		}
		else if (isDead())
		{
			final Attackable attackable = this;
			synchronized (attackable)
			{
				if (!_killedAlready)
				{
					_killedAlready = true;
					stopRandomWalking();
					stopTargetScan();
					calculateRewards(attacker);
					
					final PlayerInstance killer = attacker.getActingPlayer();
					if ((killer != null) && (killer.getKarma() > 0))
					{
						killer.decreaseKarma();
					}
				}
			}
		}
	}
	
	private void calculateRewards(Creature lastAttacker)
	{
		// TODO: Figure iterator logic and replace with for, if possible.
		Iterator<WorldObject> it = _aggroList.keySet().iterator();
		final int npcID = getNpcTemplate().getNpcId();
		while (it.hasNext())
		{
			PlayerInstance temp;
			final Creature attacker = (Creature) it.next();
			final Integer value = _aggroList.get(attacker);
			Party attackerParty = null;
			if ((attacker instanceof PlayerInstance) && (temp = (PlayerInstance) attacker).isInParty())
			{
				attackerParty = temp.getParty();
			}
			if ((npcID > 0) && (value != null) && (attackerParty == null))
			{
				int diff;
				int newXp = 0;
				int newSp = 0;
				int dmg = value;
				if (dmg > getMaxHp())
				{
					dmg = getMaxHp();
				}
				if (((diff = attacker.getLevel() - getLevel()) > 0) && (diff <= 9) && attacker.knownsObject(this))
				{
					newXp = getExpReward() * (dmg / getMaxHp());
					newSp = getSpReward() * (dmg / getMaxHp());
					newXp -= (int) ((diff / 10.0) * newXp);
					newSp -= (int) ((diff / 10.0) * newSp);
				}
				else if ((diff <= 0) && attacker.knownsObject(this))
				{
					newXp = getExpReward() * (dmg / getMaxHp());
					newSp = getSpReward() * (dmg / getMaxHp());
				}
				else if ((diff > 9) && attacker.knownsObject(this))
				{
					newXp = 0;
					newSp = 0;
				}
				if (newXp <= 0)
				{
					newXp = 0;
					newSp = 0;
				}
				else if ((newSp <= 0) && (newXp > 0))
				{
					newSp = 0;
				}
				attacker.addExpAndSp(newXp, newSp);
				_aggroList.remove(attacker);
				it = _aggroList.keySet().iterator();
				continue;
			}
			int partyDmg = 0;
			if (attackerParty != null)
			{
				final List<PlayerInstance> members = attackerParty.getPartyMembers();
				for (int i = 0; i < members.size(); ++i)
				{
					final PlayerInstance tmp = members.get(i);
					if (!_aggroList.containsKey(tmp))
					{
						continue;
					}
					partyDmg += _aggroList.get(tmp).intValue();
					_aggroList.remove(tmp);
				}
				it = _aggroList.keySet().iterator();
				if (partyDmg > getMaxHp())
				{
					partyDmg = getMaxHp();
				}
				attackerParty.distributeXpAndSp(partyDmg, getMaxHp(), getExpReward(), getSpReward());
			}
		}
		doItemDrop();
	}
	
	private void calculateAggro(int damage, Creature attacker)
	{
		int newAggro = damage;
		final Integer aggroValue = _aggroList.get(attacker);
		if (aggroValue != null)
		{
			newAggro += aggroValue.intValue();
		}
		_aggroList.put(attacker, newAggro);
		setTarget(attacker);
	}
	
	public void doItemDrop()
	{
		for (DropData drop : getNpcTemplate().getDropData())
		{
			if (drop.isSweep() || (Rnd.get(1000000) >= (drop.getChance() * Config.RATE_DROP)))
			{
				continue;
			}
			final ItemInstance dropit = ItemTable.getInstance().createItem(drop.getItemId());
			final int min = drop.getMinDrop();
			final int max = drop.getMaxDrop();
			int itemCount = 0;
			itemCount = min < max ? Rnd.get(max - min) + min : 1;
			if (dropit.getItemId() == 57)
			{
				itemCount *= Config.RATE_ADENA;
			}
			if (itemCount != 0)
			{
				dropit.setCount(itemCount);
				dropit.setX(getX());
				dropit.setY(getY());
				dropit.setZ(getZ() + 100);
				dropit.setOnTheGround(true);
				final DropItem dis = new DropItem(dropit, getObjectId());
				for (PlayerInstance player : broadcastPacket(dis))
				{
					player.addKnownObjectWithoutCreate(dropit);
				}
				World.getInstance().addVisibleObject(dropit);
			}
		}
	}
	
	protected void stopRandomWalking()
	{
		if (_currentAiTask != null)
		{
			_currentAiTask.cancel(true);
			_currentAiTask = null;
		}
	}
	
	protected boolean isTargetScanActive()
	{
		return _currentAIAttackTask != null;
	}
	
	protected synchronized void stopTargetScan()
	{
		if (_currentAIAttackTask != null)
		{
			_currentAIAttackTask.cancel(true);
			_currentAIAttackTask = null;
		}
	}
	
	@Override
	public Weapon getActiveWeapon()
	{
		return _dummyWeapon;
	}
	
	@Override
	public void setPhysicalAttack(int physicalAttack)
	{
		super.setPhysicalAttack(physicalAttack);
		_dummyWeapon = new Weapon();
		_dummyWeapon.setPDamage(physicalAttack);
		final int randDmg = getLevel();
		_dummyWeapon.setRandomDamage(randDmg);
	}
	
	public boolean noTarget()
	{
		return _aggroList.isEmpty();
	}
	
	public boolean containsTarget(Creature player)
	{
		return _aggroList.containsKey(player);
	}
	
	public void clearAggroList()
	{
		_aggroList.clear();
	}
	
	public boolean isActive()
	{
		return _active;
	}
	
	public void setActive(boolean b)
	{
		_active = b;
	}
	
	// public void setMoveRadius(int i)
	// {
	// this._moveRadius = i;
	// }
	
	public boolean isSweepActive()
	{
		return _sweepActive;
	}
	
	public void setSweepActive(boolean sweepActive)
	{
		_sweepActive = sweepActive;
	}
	
	class AIAttackeTask implements Runnable
	{
		Creature _instance;
		
		public AIAttackeTask(Creature instance)
		{
			_instance = instance;
		}
		
		@Override
		public void run()
		{
			if (!isInCombat())
			{
				for (PlayerInstance player : getKnownPlayers())
				{
					if (!getCondition2(player) || (getDistance(player.getX(), player.getY()) > (getCollisionRadius() + 200.0)))
					{
						continue;
					}
					if (_currentAiTask != null)
					{
						_currentAiTask.cancel(true);
					}
					setTarget(player);
					startAttack(player);
				}
			}
			else if (_currentAIAttackTask != null)
			{
				_currentAIAttackTask.cancel(true);
			}
		}
	}
	
	class AITask implements Runnable
	{
		Creature _instance;
		
		public AITask(Creature instance)
		{
			_instance = instance;
		}
		
		@Override
		public void run()
		{
			final int x1 = (getX() + Rnd.get(500)) - 250;
			final int y1 = (getY() + Rnd.get(500)) - 250;
			moveTo(x1, y1, getZ(), 0);
		}
	}
	
	@Override
	public boolean isAttackable()
	{
		return true;
	}
}
