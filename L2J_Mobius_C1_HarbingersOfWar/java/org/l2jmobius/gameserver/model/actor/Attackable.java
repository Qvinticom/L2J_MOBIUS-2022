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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.model.DropData;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.serverpackets.DropItem;
import org.l2jmobius.gameserver.templates.L2Npc;
import org.l2jmobius.gameserver.templates.L2Weapon;
import org.l2jmobius.util.Rnd;

public class Attackable extends NpcInstance
{
	private static Logger _log = Logger.getLogger(Attackable.class.getName());
	
	// private int _moveRadius;
	private boolean _active;
	private AITask _currentAiTask;
	private AIAttackeTask _currentAIAttackeTask;
	private static Timer _aiTimer = new Timer(true);
	private static Timer _attackTimer = new Timer(true);
	private final Map<WorldObject, Integer> _aggroList = new HashMap<>();
	private L2Weapon _dummyWeapon;
	private boolean _sweepActive;
	private boolean _killedAlready = false;
	
	public Attackable(L2Npc template)
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
			case 1:
			{
				break;
			}
			case 5:
			{
				startCombat();
				break;
			}
			case 6:
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
			_log.fine(getObjectId() + ": target reached ! new target calculated.");
			int wait = (10 + Rnd.get(120)) * 1000;
			_currentAiTask = new AITask(this);
			_aiTimer.schedule(_currentAiTask, wait);
		}
		else
		{
			_log.fine(getObjectId() + ": target reached ! noone around .. cancel movement.");
		}
	}
	
	protected void startRandomWalking()
	{
		_currentAiTask = new AITask(this);
		int wait = (10 + Rnd.get(120)) * 1000;
		_aiTimer.schedule(_currentAiTask, wait);
		setCurrentState((byte) 6);
	}
	
	protected synchronized void startTargetScan()
	{
		if ((_currentAIAttackeTask == null) && (getTarget() == null))
		{
			_currentAIAttackeTask = new AIAttackeTask(this);
			_attackTimer.scheduleAtFixedRate(_currentAIAttackeTask, 100L, 1000L);
		}
	}
	
	@Override
	public void startAttack(Creature target)
	{
		stopTargetScan();
		super.startAttack(target);
	}
	
	@Override
	public void setX(int x)
	{
		super.setX(x);
	}
	
	@Override
	public void setY(int y)
	{
		super.setY(y);
	}
	
	@Override
	public void setZ(int z)
	{
		super.setZ(z);
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
			else
			{
				_log.fine("already attacking");
			}
		}
		else if (isDead())
		{
			Attackable Attackable = this;
			synchronized (Attackable)
			{
				if (!_killedAlready)
				{
					_killedAlready = true;
					stopRandomWalking();
					stopTargetScan();
					calculateRewards(attacker);
				}
			}
		}
	}
	
	private void calculateRewards(Creature lastAttacker)
	{
		Iterator<WorldObject> it = _aggroList.keySet().iterator();
		// int numberOfAttackers = this._aggroList.size();
		int npcID = getNpcTemplate().getNpcId();
		// int npcLvl = this.getLevel();
		while (it.hasNext())
		{
			PlayerInstance temp;
			Creature attacker = (Creature) it.next();
			Integer value = _aggroList.get(attacker);
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
				List<PlayerInstance> members = attackerParty.getPartyMembers();
				for (int i = 0; i < members.size(); ++i)
				{
					PlayerInstance tmp = members.get(i);
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
		Integer aggroValue = _aggroList.get(attacker);
		if (aggroValue != null)
		{
			newAggro += aggroValue.intValue();
		}
		_aggroList.put(attacker, newAggro);
		if (_aggroList.size() == 1)
		{
			setTarget(attacker);
		}
		else
		{
			setTarget(attacker);
		}
	}
	
	public void doItemDrop()
	{
		List<DropData> drops = getNpcTemplate().getDropData();
		_log.finer("This npc has " + drops.size() + " drops defined.");
		Iterator<DropData> iter = drops.iterator();
		while (iter.hasNext())
		{
			DropData drop = iter.next();
			if (drop.isSweep() || (Rnd.get(1000000) >= (drop.getChance() * Config.RATE_DROP)))
			{
				continue;
			}
			ItemInstance dropit = ItemTable.getInstance().createItem(drop.getItemId());
			int min = drop.getMinDrop();
			int max = drop.getMaxDrop();
			int itemCount = 0;
			itemCount = min < max ? Rnd.get(max - min) + min : 1;
			if (dropit.getItemId() == 57)
			{
				itemCount *= Config.RATE_ADENA;
			}
			if (itemCount != 0)
			{
				dropit.setCount(itemCount);
				_log.fine("Item id to drop: " + drop.getItemId() + " amount: " + dropit.getCount());
				dropit.setX(getX());
				dropit.setY(getY());
				dropit.setZ(getZ() + 100);
				dropit.setOnTheGround(true);
				DropItem dis = new DropItem(dropit, getObjectId());
				Creature[] players = broadcastPacket(dis);
				for (Creature player : players)
				{
					((PlayerInstance) player).addKnownObjectWithoutCreate(dropit);
				}
				World.getInstance().addVisibleObject(dropit);
				continue;
			}
			_log.finer("Roll produced 0 items to drop... Cancelling.");
		}
	}
	
	protected void stopRandomWalking()
	{
		if (_currentAiTask != null)
		{
			_currentAiTask.cancel();
			_currentAiTask = null;
		}
	}
	
	protected boolean isTargetScanActive()
	{
		return _currentAIAttackeTask != null;
	}
	
	protected synchronized void stopTargetScan()
	{
		if (_currentAIAttackeTask != null)
		{
			_currentAIAttackeTask.cancel();
			_currentAIAttackeTask = null;
		}
	}
	
	@Override
	public void setCurrentHp(double currentHp)
	{
		super.setCurrentHp(currentHp);
	}
	
	@Override
	public L2Weapon getActiveWeapon()
	{
		return _dummyWeapon;
	}
	
	@Override
	public void setPhysicalAttack(int physicalAttack)
	{
		super.setPhysicalAttack(physicalAttack);
		_dummyWeapon = new L2Weapon();
		_dummyWeapon.setPDamage(physicalAttack);
		int randDmg = getLevel();
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
	
	class AIAttackeTask extends TimerTask
	{
		Creature _instance;
		
		public AIAttackeTask(Creature instance)
		{
			_instance = instance;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (!isInCombat())
				{
					_log.finer(getObjectId() + ": monster knows " + getKnownPlayers().size() + " players");
					Set<PlayerInstance> knownPlayers = getKnownPlayers();
					Iterator<PlayerInstance> iter = knownPlayers.iterator();
					while (iter.hasNext())
					{
						PlayerInstance player = iter.next();
						if (!getCondition2(player) || !(getDistance(player.getX(), player.getY()) <= (getCollisionRadius() + 200.0)))
						{
							continue;
						}
						_log.fine(getObjectId() + ": Player " + player.getObjectId() + " in aggro range. Attacking!");
						if (_currentAiTask != null)
						{
							_currentAiTask.cancel();
						}
						setTarget(player);
						startAttack(player);
					}
				}
				else if (_currentAIAttackeTask != null)
				{
					_currentAIAttackeTask.cancel();
				}
			}
			catch (Throwable e)
			{
				_log.warning(getObjectId() + ": Problem occured in AiAttackTask:" + e);
				StringWriter pw = new StringWriter();
				PrintWriter prw = new PrintWriter(pw);
				e.printStackTrace(prw);
				_log.severe(pw.toString());
			}
		}
	}
	
	class AITask extends TimerTask
	{
		Creature _instance;
		
		public AITask(Creature instance)
		{
			_instance = instance;
		}
		
		@Override
		public void run()
		{
			try
			{
				int x1 = (getX() + Rnd.get(500)) - 250;
				int y1 = (getY() + Rnd.get(500)) - 250;
				moveTo(x1, y1, getZ(), 0);
			}
			catch (Throwable e)
			{
				_log.warning(getObjectId() + ": Problem occured in AiTask:" + e);
				StringWriter pw = new StringWriter();
				PrintWriter prw = new PrintWriter(pw);
				e.printStackTrace(prw);
				_log.severe(pw.toString());
			}
		}
	}
	
}
