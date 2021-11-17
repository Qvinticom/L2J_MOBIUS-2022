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
package org.l2jmobius.gameserver.model.actor.instance;

import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.data.ExperienceTable;
import org.l2jmobius.gameserver.enums.CreatureState;
import org.l2jmobius.gameserver.model.Inventory;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.ChangeMoveType;
import org.l2jmobius.gameserver.network.serverpackets.CharInfo;
import org.l2jmobius.gameserver.network.serverpackets.DeleteObject;
import org.l2jmobius.gameserver.network.serverpackets.DropItem;
import org.l2jmobius.gameserver.network.serverpackets.GetItem;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.ItemList;
import org.l2jmobius.gameserver.network.serverpackets.MyTargetSelected;
import org.l2jmobius.gameserver.network.serverpackets.PetDelete;
import org.l2jmobius.gameserver.network.serverpackets.PetInventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.PetItemList;
import org.l2jmobius.gameserver.network.serverpackets.PetStatusShow;
import org.l2jmobius.gameserver.network.serverpackets.PetStatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SocialAction;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.StopMove;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;
import org.l2jmobius.gameserver.templates.NpcTemplate;
import org.l2jmobius.gameserver.templates.Weapon;
import org.l2jmobius.gameserver.threads.ThreadPool;

public class Pet extends Creature
{
	private static Logger _log = Logger.getLogger(Pet.class.getName());
	private byte _petId = 1;
	private int _exp = 0;
	private int _sp = 0;
	private int _pkKills;
	private int _maxFed = 5;
	private int _curFed = 5;
	private Player _owner;
	private int _karma = 0;
	private final Inventory _inventory = new Inventory();
	private Weapon _dummyWeapon;
	private final NpcTemplate _template;
	private int _attackRange = 36;
	private boolean _follow = true;
	private ScheduledFuture<?> _decayTask;
	private int _controlItemId;
	private int _nextLevel;
	private int _lastLevel;
	private byte updateKnownCounter = 0;
	
	public Pet(NpcTemplate template)
	{
		setCollisionHeight(template.getHeight());
		setCollisionRadius(template.getRadius());
		setCurrentState(CreatureState.IDLE);
		setPhysicalAttack(9999);
		_template = template;
	}
	
	@Override
	public void onAction(Player player)
	{
		if (player == _owner)
		{
			player.sendPacket(new PetStatusShow(2));
			player.sendPacket(new PetStatusUpdate(this));
			player.sendPacket(new ActionFailed());
		}
		player.setCurrentState(CreatureState.IDLE);
		player.setTarget(this);
		player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
	}
	
	public void setSummonHp(int value)
	{
		int hp = value;
		if (hp == 0)
		{
			hp = 5000;
		}
		if (getMaxHp() < hp)
		{
			super.setMaxHp(hp);
		}
		super.setCurrentHp(hp);
	}
	
	public void setNextLevel(int level)
	{
		_nextLevel = level;
	}
	
	public int getNextLevel()
	{
		return _nextLevel;
	}
	
	public void setLastLevel(int level)
	{
		_lastLevel = level;
	}
	
	public int getLastLevel()
	{
		return _lastLevel;
	}
	
	@Override
	public void setWalkSpeed(int walkSpeed)
	{
		super.setWalkSpeed(Math.max(24, walkSpeed));
	}
	
	@Override
	public void setPhysicalDefense(int pdef)
	{
		super.setPhysicalDefense(Math.max(100, pdef));
	}
	
	@Override
	public void setRunSpeed(int runSpeed)
	{
		super.setRunSpeed(Math.max(125, runSpeed));
	}
	
	public int getPetId()
	{
		return _petId;
	}
	
	public void setPetId(byte petid)
	{
		_petId = petid;
	}
	
	public int getControlItemId()
	{
		return _controlItemId;
	}
	
	public void setControlItemId(int controlItemId)
	{
		_controlItemId = controlItemId;
	}
	
	public int getKarma()
	{
		return _karma;
	}
	
	public void setKarma(int karma)
	{
		_karma = karma;
	}
	
	public int getMaxFed()
	{
		return _maxFed;
	}
	
	public void setMaxFed(int num)
	{
		_maxFed = num;
	}
	
	public int getCurrentFed()
	{
		return _curFed;
	}
	
	public void setCurrentFed(int num)
	{
		_curFed = num;
	}
	
	public void setOwner(Player owner)
	{
		_owner = owner;
	}
	
	public Player getOwner()
	{
		return _owner;
	}
	
	public int getNpcId()
	{
		return _template.getNpcId();
	}
	
	public void setPkKills(int pkKills)
	{
		_pkKills = pkKills;
	}
	
	public int getPkKills()
	{
		return _pkKills;
	}
	
	public int getExp()
	{
		return _exp;
	}
	
	public void setExp(int exp)
	{
		_exp = exp;
	}
	
	public int getSp()
	{
		return _sp;
	}
	
	public void setSp(int sp)
	{
		_sp = sp;
	}
	
	@Override
	public void addExpAndSp(int addToExp, int addToSp)
	{
		_exp += addToExp;
		_sp += addToSp;
		final PetStatusUpdate su = new PetStatusUpdate(this);
		_owner.sendPacket(su);
		while (_exp >= getNextLevel())
		{
			increaseLevel();
		}
	}
	
	@Override
	public void increaseLevel()
	{
		setLastLevel(getNextLevel());
		setLevel(getLevel() + 1);
		setNextLevel(ExperienceTable.getInstance().getExp(getLevel() + 1));
		final PetStatusUpdate ps = new PetStatusUpdate(this);
		_owner.sendPacket(ps);
		final StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.LEVEL, getLevel());
		broadcastPacket(su);
		broadcastPacket(new SocialAction(getObjectId(), 15));
		_owner.sendPacket(new SystemMessage(SystemMessage.YOU_INCREASED_YOUR_LEVEL));
	}
	
	public void followOwner(Player owner)
	{
		setCurrentState(CreatureState.FOLLOW);
		setTarget(owner);
		moveTo(owner.getX(), owner.getY(), owner.getZ(), 30);
		broadcastPacket(new PetStatusUpdate(this));
	}
	
	@Override
	public void onTargetReached()
	{
		super.onTargetReached();
		updateKnownObjects();
		try
		{
			switch (getCurrentState())
			{
				case PICKUP_ITEM:
				{
					doPickupItem();
					break;
				}
				case ATTACKING:
				{
					startCombat();
					break;
				}
			}
		}
		catch (Exception io)
		{
			io.printStackTrace();
		}
	}
	
	@Override
	public Weapon getActiveWeapon()
	{
		return _dummyWeapon;
	}
	
	@Override
	public void setPhysicalAttack(int value)
	{
		int physicalAttack = value;
		if (physicalAttack < 100)
		{
			physicalAttack = 100;
		}
		
		super.setPhysicalAttack(physicalAttack);
		_dummyWeapon = new Weapon();
		_dummyWeapon.setPDamage(physicalAttack);
		_dummyWeapon.setRandomDamage(getLevel());
	}
	
	public Inventory getInventory()
	{
		return _inventory;
	}
	
	private void doPickupItem()
	{
		broadcastPacket(new StopMove(getObjectId(), getX(), getY(), getZ(), getHeading()));
		if (!(getTarget() instanceof Item))
		{
			_log.warning("trying to pickup wrong target." + getTarget());
			_owner.sendPacket(new ActionFailed());
			return;
		}
		final Item target = (Item) getTarget();
		boolean pickupOk = false;
		synchronized (target)
		{
			if (target.isOnTheGround())
			{
				pickupOk = true;
				target.setOnTheGround(false);
			}
		}
		if (!pickupOk)
		{
			_owner.sendPacket(new ActionFailed());
			setCurrentState(CreatureState.IDLE);
			return;
		}
		broadcastPacket(new GetItem(target, getObjectId()));
		World.getInstance().removeVisibleObject(target);
		broadcastPacket(new DeleteObject(target));
		getInventory().addItem(target);
		_owner.sendPacket(new PetItemList(this));
		setCurrentState(CreatureState.IDLE);
		if (getFollowStatus())
		{
			followOwner(_owner);
		}
	}
	
	@Override
	public void reduceCurrentHp(int damage, Creature attacker)
	{
		super.reduceCurrentHp(damage, attacker);
		if (!isDead() && (attacker != null) && !isInCombat())
		{
			startAttack(attacker);
		}
		if (isDead())
		{
			synchronized (this)
			{
				if ((_decayTask == null) || _decayTask.isCancelled() || _decayTask.isDone())
				{
					_decayTask = ThreadPool.schedule(new Creature.DecayTask(this), 7000);
				}
			}
		}
	}
	
	@Override
	public void onDecay()
	{
		deleteMe(_owner);
	}
	
	public void giveAllToOwner()
	{
		try
		{
			final Inventory petInventory = getInventory();
			for (Item giveit : petInventory.getItems())
			{
				if (((giveit.getItem().getWeight() * giveit.getCount()) + _owner.getInventory().getTotalWeight()) < _owner.getMaxLoad())
				{
					giveItemToOwner(giveit);
					continue;
				}
				dropItemHere(giveit);
			}
		}
		catch (Exception e)
		{
			_log.warning("Give all items error " + e);
		}
	}
	
	public void giveItemToOwner(Item item)
	{
		try
		{
			_owner.getInventory().addItem(item);
			getInventory().dropItem(item, item.getCount());
			final PetInventoryUpdate petiu = new PetInventoryUpdate();
			petiu.addRemovedItem(item);
			_owner.sendPacket(petiu);
			_owner.sendPacket(new ItemList(_owner, false));
		}
		catch (Exception e)
		{
			_log.warning("Error while giving item to owner: " + e);
		}
	}
	
	@Override
	public void broadcastStatusUpdate()
	{
		super.broadcastStatusUpdate();
		if (getOwner() != null)
		{
			getOwner().sendPacket(new PetStatusUpdate(this));
		}
	}
	
	public void deleteMe(Player owner)
	{
		unSummon(owner);
		destroyControlItem(owner);
		owner.sendPacket(new PetDelete(getObjectId(), 2));
	}
	
	public void unSummon(Player owner)
	{
		giveAllToOwner();
		World.getInstance().removeVisibleObject(this);
		removeAllKnownObjects();
		owner.setPet(null);
		setTarget(null);
	}
	
	public void destroyControlItem(Player owner)
	{
		try
		{
			final Item removedItem = owner.getInventory().destroyItem(getControlItemId(), 1);
			final InventoryUpdate iu = new InventoryUpdate();
			iu.addRemovedItem(removedItem);
			owner.sendPacket(iu);
			final StatusUpdate su = new StatusUpdate(owner.getObjectId());
			su.addAttribute(StatusUpdate.CUR_LOAD, owner.getCurrentLoad());
			owner.sendPacket(su);
			owner.sendPacket(new UserInfo(owner));
			owner.broadcastPacket(new CharInfo(owner));
			final World world = World.getInstance();
			world.removeObject(removedItem);
		}
		catch (Exception e)
		{
			_log.warning("Error while destroying control item: " + e);
		}
	}
	
	public void dropAllItems()
	{
		try
		{
			for (Item item : getInventory().getItems())
			{
				dropItemHere(item);
			}
		}
		catch (Exception e)
		{
			_log.warning("Pet Drop Error: " + e);
		}
	}
	
	public void dropItemHere(Item item)
	{
		final Item dropit = getInventory().dropItem(item.getObjectId(), item.getCount());
		if (dropit != null)
		{
			dropit.setX(getX());
			dropit.setY(getY());
			dropit.setZ(getZ() + 100);
			dropit.setOnTheGround(true);
			for (Player player : broadcastPacket(new DropItem(dropit, getObjectId())))
			{
				player.addKnownObjectWithoutCreate(dropit);
			}
			World.getInstance().addVisibleObject(dropit);
		}
	}
	
	@Override
	public void startAttack(Creature target)
	{
		if (!knownsObject(target))
		{
			target.addKnownObject(this);
			addKnownObject(target);
		}
		if (!isRunning())
		{
			setRunning(true);
			broadcastPacket(new ChangeMoveType(this, ChangeMoveType.RUN));
		}
		super.startAttack(target);
	}
	
	@Override
	public int getAttackRange()
	{
		return _attackRange;
	}
	
	public void setAttackRange(int range)
	{
		_attackRange = Math.max(36, range);
	}
	
	public void setFollowStatus(boolean value)
	{
		_follow = value;
	}
	
	public boolean getFollowStatus()
	{
		return _follow;
	}
	
	public void updateKnownObjects()
	{
		updateKnownCounter = (byte) (updateKnownCounter + 1);
		if (updateKnownCounter > 3)
		{
			if (!getKnownObjects().isEmpty())
			{
				for (WorldObject object : _knownObjects)
				{
					if (getDistance(object.getX(), object.getY()) <= 4000.0)
					{
						continue;
					}
					if (object instanceof Monster)
					{
						removeKnownObject(object);
						((Monster) object).removeKnownObject(this);
						continue;
					}
					removeKnownObject(object);
					object.removeKnownObject(this);
				}
			}
			updateKnownCounter = 0;
		}
	}
	
	@Override
	public Player getActingPlayer()
	{
		return _owner;
	}
	
	@Override
	public boolean isPet()
	{
		return true;
	}
}
