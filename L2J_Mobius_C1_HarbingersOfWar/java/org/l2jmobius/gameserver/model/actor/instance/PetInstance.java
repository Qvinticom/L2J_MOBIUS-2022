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
import org.l2jmobius.gameserver.templates.Npc;
import org.l2jmobius.gameserver.templates.Weapon;
import org.l2jmobius.gameserver.threadpool.ThreadPool;

public class PetInstance extends Creature
{
	private static Logger _log = Logger.getLogger(PetInstance.class.getName());
	private byte _petId = 1;
	private int _exp = 0;
	private int _sp = 0;
	private int _pkKills;
	private int _maxFed = 5;
	private int _curFed = 5;
	private PlayerInstance _owner;
	private int _karma = 0;
	private final Inventory _inventory = new Inventory();
	private Weapon _dummyWeapon;
	private final Npc _template;
	private int _attackRange = 36;
	private boolean _follow = true;
	private ScheduledFuture<?> _decayTask;
	private int _controlItemId;
	private int _nextLevel;
	private int _lastLevel;
	private byte updateKnownCounter = 0;
	
	public PetInstance(Npc template)
	{
		setCollisionHeight(template.getHeight());
		setCollisionRadius(template.getRadius());
		setCurrentState(CreatureState.IDLE);
		setPhysicalAttack(9999);
		_template = template;
	}
	
	@Override
	public void onAction(PlayerInstance player)
	{
		if (player == _owner)
		{
			player.sendPacket(new PetStatusShow(2));
			player.sendPacket(new PetStatusUpdate(this));
			player.sendPacket(new ActionFailed());
		}
		player.setCurrentState(CreatureState.IDLE);
		player.setTarget(this);
		final MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
		player.sendPacket(my);
	}
	
	public void setSummonHp(int hp)
	{
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
		if (walkSpeed < 24)
		{
			walkSpeed = 24;
		}
		super.setWalkSpeed(walkSpeed);
	}
	
	@Override
	public void setPhysicalDefense(int pdef)
	{
		if (pdef < 100)
		{
			pdef = 100;
		}
		super.setPhysicalDefense(pdef);
	}
	
	@Override
	public void setRunSpeed(int runSpeed)
	{
		if (runSpeed < 125)
		{
			runSpeed = 125;
		}
		super.setRunSpeed(runSpeed);
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
	
	public void setOwner(PlayerInstance owner)
	{
		_owner = owner;
	}
	
	public PlayerInstance getOwner()
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
		final SocialAction sa = new SocialAction(getObjectId(), 15);
		broadcastPacket(sa);
		_owner.sendPacket(new SystemMessage(SystemMessage.YOU_INCREASED_YOUR_LEVEL));
	}
	
	public void followOwner(PlayerInstance owner)
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
	public void setPhysicalAttack(int physicalAttack)
	{
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
		final StopMove sm = new StopMove(getObjectId(), getX(), getY(), getZ(), getHeading());
		broadcastPacket(sm);
		if (!(getTarget() instanceof ItemInstance))
		{
			_log.warning("trying to pickup wrong target." + getTarget());
			_owner.sendPacket(new ActionFailed());
			return;
		}
		final ItemInstance target = (ItemInstance) getTarget();
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
		final GetItem gi = new GetItem(target, getObjectId());
		broadcastPacket(gi);
		World.getInstance().removeVisibleObject(target);
		final DeleteObject del = new DeleteObject(target);
		broadcastPacket(del);
		getInventory().addItem(target);
		final PetItemList iu = new PetItemList(this);
		_owner.sendPacket(iu);
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
			for (ItemInstance giveit : petInventory.getItems())
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
	
	public void giveItemToOwner(ItemInstance item)
	{
		try
		{
			_owner.getInventory().addItem(item);
			getInventory().dropItem(item, item.getCount());
			final PetInventoryUpdate petiu = new PetInventoryUpdate();
			final ItemList playerUI = new ItemList(_owner, false);
			petiu.addRemovedItem(item);
			_owner.sendPacket(petiu);
			_owner.sendPacket(playerUI);
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
	
	public void deleteMe(PlayerInstance owner)
	{
		unSummon(owner);
		destroyControlItem(owner);
		owner.sendPacket(new PetDelete(getObjectId(), 2));
	}
	
	public void unSummon(PlayerInstance owner)
	{
		giveAllToOwner();
		World.getInstance().removeVisibleObject(this);
		removeAllKnownObjects();
		owner.setPet(null);
		setTarget(null);
	}
	
	public void destroyControlItem(PlayerInstance owner)
	{
		try
		{
			final ItemInstance removedItem = owner.getInventory().destroyItem(getControlItemId(), 1);
			final InventoryUpdate iu = new InventoryUpdate();
			iu.addRemovedItem(removedItem);
			owner.sendPacket(iu);
			final StatusUpdate su = new StatusUpdate(owner.getObjectId());
			su.addAttribute(StatusUpdate.CUR_LOAD, owner.getCurrentLoad());
			owner.sendPacket(su);
			final UserInfo ui = new UserInfo(owner);
			owner.sendPacket(ui);
			final CharInfo info = new CharInfo(owner);
			owner.broadcastPacket(info);
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
			for (ItemInstance item : getInventory().getItems())
			{
				dropItemHere(item);
			}
		}
		catch (Exception e)
		{
			_log.warning("Pet Drop Error: " + e);
		}
	}
	
	public void dropItemHere(ItemInstance dropit)
	{
		dropit = getInventory().dropItem(dropit.getObjectId(), dropit.getCount());
		if (dropit != null)
		{
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
			final ChangeMoveType move = new ChangeMoveType(this, ChangeMoveType.RUN);
			broadcastPacket(move);
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
		if (range < 36)
		{
			range = 36;
		}
		_attackRange = range;
	}
	
	public void setFollowStatus(boolean state)
	{
		_follow = state;
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
					if (object instanceof MonsterInstance)
					{
						removeKnownObject(object);
						((MonsterInstance) object).removeKnownObject(this);
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
	public PlayerInstance getActingPlayer()
	{
		return _owner;
	}
	
	@Override
	public boolean isPet()
	{
		return true;
	}
}
