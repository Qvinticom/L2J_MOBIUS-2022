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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.enums.CreatureState;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.Attack;
import org.l2jmobius.gameserver.network.serverpackets.AutoAttackStart;
import org.l2jmobius.gameserver.network.serverpackets.AutoAttackStop;
import org.l2jmobius.gameserver.network.serverpackets.CharMoveToLocation;
import org.l2jmobius.gameserver.network.serverpackets.Die;
import org.l2jmobius.gameserver.network.serverpackets.FinishRotation;
import org.l2jmobius.gameserver.network.serverpackets.MoveToPawn;
import org.l2jmobius.gameserver.network.serverpackets.ServerBasePacket;
import org.l2jmobius.gameserver.network.serverpackets.SetToLocation;
import org.l2jmobius.gameserver.network.serverpackets.SetupGauge;
import org.l2jmobius.gameserver.network.serverpackets.SocialAction;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.StopMove;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.TeleportToLocation;
import org.l2jmobius.gameserver.templates.Weapon;
import org.l2jmobius.gameserver.threadpool.ThreadPool;
import org.l2jmobius.util.Rnd;

public abstract class Creature extends WorldObject
{
	private static final Logger _log = Logger.getLogger(Creature.class.getName());
	
	private final List<Creature> _statusListener = new ArrayList<>();
	private ScheduledFuture<?> _attackTask;
	private ScheduledFuture<?> _hitTask;
	private ScheduledFuture<?> _regenTask;
	private final Object _mpLock = new Object();
	private boolean _mpRegenActive;
	private final Object _hpLock = new Object();
	private boolean _hpRegenActive;
	private int _moveOffset;
	private float _effectiveSpeed;
	private long _moveStartTime;
	private double _xAddition;
	private double _yAddition;
	private long _timeToTarget;
	private ScheduledFuture<?> _moveTask;
	private String _name;
	private int _level = 1;
	private int _maxHp;
	private double _currentHp;
	private int _maxMp;
	private double _currentMp;
	private int _accuracy;
	private int _criticalHit;
	private int _evasionRate;
	private int _magicalAttack;
	private int _magicalDefense;
	private int _magicalSpeed;
	private int _physicalAttack;
	private int _physicalDefense;
	private int _physicalSpeed;
	private int _runSpeed;
	private int _walkSpeed;
	private boolean _running;
	private int _flyingRunSpeed;
	private int _floatingWalkSpeed;
	private int _flyingWalkSpeed;
	private int _floatingRunSpeed;
	private int _int;
	private int _str;
	private int _con;
	private int _dex;
	private int _men;
	private int _wit;
	private int _face;
	private int _hairStyle;
	private int _hairColor;
	private int _sex;
	private int _heading;
	private int _xDestination;
	private int _yDestination;
	private int _zDestination;
	private double _movementMultiplier;
	private double _attackSpeedMultiplier;
	private double _collisionRadius;
	private double _collisionHeight;
	private WorldObject _target;
	private int _activeSoulShotGrade;
	private CreatureState _currentState = CreatureState.IDLE;
	private boolean _inCombat;
	private boolean _moving;
	private boolean _movingToPawn;
	private int _pawnOffset;
	private Creature _pawnTarget;
	private boolean _2ndHit = false;
	private boolean _currentlyAttacking = false;
	private WorldObject _attackTarget;
	protected String _title;
	
	public boolean knownsObject(WorldObject object)
	{
		return _knownObjects.contains(object);
	}
	
	public void onDecay()
	{
		World.getInstance().removeVisibleObject(this);
	}
	
	public void addStatusListener(Creature object)
	{
		_statusListener.add(object);
	}
	
	private List<Creature> getStatusListener()
	{
		return _statusListener;
	}
	
	public void removeStatusListener(Creature object)
	{
		_statusListener.remove(object);
	}
	
	public int getHeading()
	{
		return _heading;
	}
	
	public void setHeading(int heading)
	{
		_heading = heading;
	}
	
	public int getXdestination()
	{
		return _xDestination;
	}
	
	public void setXdestination(int x1)
	{
		_xDestination = x1;
	}
	
	public int getYdestination()
	{
		return _yDestination;
	}
	
	public void setYdestination(int y1)
	{
		_yDestination = y1;
	}
	
	public int getZdestination()
	{
		return _zDestination;
	}
	
	public void setZdestination(int z1)
	{
		_zDestination = z1;
	}
	
	@Override
	public int getX()
	{
		if (!isMoving())
		{
			return super.getX();
		}
		final long elapsed = System.currentTimeMillis() - _moveStartTime;
		final int diff = (int) (elapsed * _xAddition);
		final int remain = Math.abs(getXdestination() - super.getX()) - Math.abs(diff);
		if (remain > 0)
		{
			return super.getX() + diff;
		}
		return getXdestination();
	}
	
	@Override
	public int getY()
	{
		if (!isMoving())
		{
			return super.getY();
		}
		final long elapsed = System.currentTimeMillis() - _moveStartTime;
		final int diff = (int) (elapsed * _yAddition);
		final int remain = Math.abs(getYdestination() - super.getY()) - Math.abs(diff);
		if (remain > 0)
		{
			return super.getY() + diff;
		}
		return getYdestination();
	}
	
	@Override
	public int getZ()
	{
		if (!isMoving())
		{
			return super.getZ();
		}
		return super.getZ();
	}
	
	public boolean isMoving()
	{
		return _moving;
	}
	
	public void stopMove()
	{
		if (_moveTask != null)
		{
			_moveTask.cancel(true);
			_moveTask = null;
		}
		setX(getX());
		setY(getY());
		setZ(getZ());
		setIsMoving(false);
	}
	
	public int getCon()
	{
		return _con;
	}
	
	public void setCon(int con)
	{
		_con = con;
	}
	
	public int getDex()
	{
		return _dex;
	}
	
	public void setDex(int dex)
	{
		_dex = dex;
	}
	
	public int getInt()
	{
		return _int;
	}
	
	public void setInt(int int1)
	{
		_int = int1;
	}
	
	public int getMen()
	{
		return _men;
	}
	
	public void setMen(int men)
	{
		_men = men;
	}
	
	public int getStr()
	{
		return _str;
	}
	
	public void setStr(int str)
	{
		_str = str;
	}
	
	public int getWit()
	{
		return _wit;
	}
	
	public void setWit(int wit)
	{
		_wit = wit;
	}
	
	public double getCurrentHp()
	{
		return _currentHp;
	}
	
	public void setCurrentHp(double currentHp)
	{
		_currentHp = currentHp;
		if (_currentHp >= getMaxHp())
		{
			stopHpRegeneration();
			_currentHp = getMaxHp();
		}
		else if (!_hpRegenActive && !isDead())
		{
			startHpRegeneration();
		}
		broadcastStatusUpdate();
	}
	
	public void stopHpRegeneration()
	{
		if (_hpRegenActive)
		{
			if (_regenTask != null)
			{
				_regenTask.cancel(true);
				_regenTask = null;
			}
			_hpRegenActive = false;
		}
	}
	
	private void startHpRegeneration()
	{
		_regenTask = ThreadPool.scheduleAtFixedRate(new HpRegenTask(this), 3000, 3000);
		_hpRegenActive = true;
	}
	
	public double getCurrentMp()
	{
		return _currentMp;
	}
	
	public void setCurrentMp(double currentMp)
	{
		_currentMp = currentMp;
		if (_currentMp >= getMaxMp())
		{
			stopMpRegeneration();
			_currentMp = getMaxMp();
		}
		else if (!_mpRegenActive && !isDead())
		{
			startMpRegeneration();
		}
		broadcastStatusUpdate();
	}
	
	private void startMpRegeneration()
	{
		_regenTask = ThreadPool.scheduleAtFixedRate(new MpRegenTask(this), 3000, 3000);
		_mpRegenActive = true;
	}
	
	public void stopMpRegeneration()
	{
		if (_mpRegenActive)
		{
			if (_regenTask != null)
			{
				_regenTask.cancel(true);
				_regenTask = null;
			}
			_mpRegenActive = false;
		}
	}
	
	public void broadcastStatusUpdate()
	{
		final List<Creature> list = getStatusListener();
		if (list.isEmpty())
		{
			return;
		}
		final StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
		su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
		for (int i = 0; i < list.size(); ++i)
		{
			final Creature temp = list.get(i);
			if (!(temp instanceof PlayerInstance))
			{
				continue;
			}
			final PlayerInstance player = (PlayerInstance) temp;
			try
			{
				player.sendPacket(su);
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}
		}
	}
	
	public int getMaxHp()
	{
		return _maxHp;
	}
	
	public void setMaxHp(int maxHp)
	{
		_maxHp = maxHp;
	}
	
	public int getMaxMp()
	{
		return _maxMp;
	}
	
	public void setMaxMp(int maxMp)
	{
		_maxMp = maxMp;
	}
	
	public int getAccuracy()
	{
		return _accuracy;
	}
	
	public void setAccuracy(int accuracy)
	{
		_accuracy = accuracy;
	}
	
	public int getCriticalHit()
	{
		return _criticalHit;
	}
	
	public void setCriticalHit(int criticalHit)
	{
		_criticalHit = criticalHit;
	}
	
	public int getEvasionRate()
	{
		return _evasionRate;
	}
	
	public void setEvasionRate(int evasionRate)
	{
		_evasionRate = evasionRate;
	}
	
	public int getFace()
	{
		return _face;
	}
	
	public void setFace(int face)
	{
		_face = face;
	}
	
	public int getHairColor()
	{
		return _hairColor;
	}
	
	public void setHairColor(int hairColor)
	{
		_hairColor = hairColor;
	}
	
	public int getHairStyle()
	{
		return _hairStyle;
	}
	
	public void setHairStyle(int hairStyle)
	{
		_hairStyle = hairStyle;
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public void increaseLevel()
	{
		++_level;
		final StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.LEVEL, _level);
		sendPacket(su);
		sendPacket(new SystemMessage(SystemMessage.YOU_INCREASED_YOUR_LEVEL));
		final SocialAction sa = new SocialAction(getObjectId(), 15);
		broadcastPacket(sa);
		sendPacket(sa);
	}
	
	public void decreaseLevel()
	{
		--_level;
		final StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.LEVEL, _level);
		sendPacket(su);
	}
	
	public void setLevel(int level)
	{
		_level = level;
	}
	
	public int getMagicalAttack()
	{
		return _magicalAttack;
	}
	
	public void setMagicalAttack(int magicalAttack)
	{
		_magicalAttack = magicalAttack;
	}
	
	public int getMagicalDefense()
	{
		return _magicalDefense;
	}
	
	public void setMagicalDefense(int magicalDefense)
	{
		_magicalDefense = magicalDefense;
	}
	
	public int getMagicalSpeed()
	{
		return _magicalSpeed;
	}
	
	public void setMagicalSpeed(int magicalSpeed)
	{
		_magicalSpeed = magicalSpeed;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public void setName(String name)
	{
		_name = name;
	}
	
	public int getPhysicalAttack()
	{
		return _physicalAttack;
	}
	
	public void setPhysicalAttack(int physicalAttack)
	{
		_physicalAttack = physicalAttack;
	}
	
	public int getPhysicalDefense()
	{
		return _physicalDefense;
	}
	
	public void setPhysicalDefense(int physicalDefense)
	{
		_physicalDefense = physicalDefense;
	}
	
	public int getPhysicalSpeed()
	{
		return _physicalSpeed;
	}
	
	public void setPhysicalSpeed(int physicalSpeed)
	{
		_physicalSpeed = physicalSpeed;
	}
	
	public boolean isMale()
	{
		return _sex == 0;
	}
	
	public int getSex()
	{
		return _sex;
	}
	
	public void setSex(int sex)
	{
		_sex = sex;
	}
	
	public int getWalkSpeed()
	{
		return _walkSpeed;
	}
	
	public void setWalkSpeed(int walkSpeed)
	{
		_walkSpeed = walkSpeed;
		updateEffectiveSpeed();
	}
	
	private void updateEffectiveSpeed()
	{
		_effectiveSpeed = isRunning() ? getRunSpeed() * (float) getMovementMultiplier() : getWalkSpeed() * (float) getMovementMultiplier();
	}
	
	public void setTarget(WorldObject object)
	{
		if ((object == null) && isInCombat())
		{
			setInCombat(false);
		}
		_target = object;
	}
	
	public int getTargetId()
	{
		if (_target != null)
		{
			return _target.getObjectId();
		}
		return -1;
	}
	
	public WorldObject getTarget()
	{
		return _target;
	}
	
	public CreatureState getCurrentState()
	{
		return _currentState;
	}
	
	public void setCurrentState(CreatureState currentState)
	{
		_currentState = currentState;
	}
	
	public double getCollisionHeight()
	{
		return _collisionHeight;
	}
	
	public void setCollisionHeight(double offset)
	{
		_collisionHeight = offset;
	}
	
	public double getCollisionRadius()
	{
		return _collisionRadius;
	}
	
	public void setCollisionRadius(double collisionRadius)
	{
		_collisionRadius = collisionRadius;
	}
	
	public double getMovementMultiplier()
	{
		return _movementMultiplier;
	}
	
	public void setMovementMultiplier(double unknown1)
	{
		_movementMultiplier = unknown1;
		updateEffectiveSpeed();
	}
	
	public double getAttackSpeedMultiplier()
	{
		return _attackSpeedMultiplier;
	}
	
	public void setAttackSpeedMultiplier(double unknown2)
	{
		_attackSpeedMultiplier = unknown2;
	}
	
	public String getTitle()
	{
		return _title;
	}
	
	public void setTitle(String title)
	{
		_title = title;
	}
	
	public int getRunSpeed()
	{
		return _runSpeed;
	}
	
	public void setRunSpeed(int runSpeed)
	{
		_runSpeed = runSpeed;
		updateEffectiveSpeed();
	}
	
	public boolean isRunning()
	{
		return _running;
	}
	
	public void setRunning(boolean b)
	{
		_running = b;
		updateEffectiveSpeed();
	}
	
	public int getFloatingRunSpeed()
	{
		return _floatingRunSpeed;
	}
	
	public int getFloatingWalkSpeed()
	{
		return _floatingWalkSpeed;
	}
	
	public int getFlyingRunSpeed()
	{
		return _flyingRunSpeed;
	}
	
	public int getFlyingWalkSpeed()
	{
		return _flyingWalkSpeed;
	}
	
	public void setFloatingRunSpeed(int floatingRunSpeed)
	{
		_floatingRunSpeed = floatingRunSpeed;
	}
	
	public void setFloatingWalkSpeed(int floatingWalkSpeed)
	{
		_floatingWalkSpeed = floatingWalkSpeed;
	}
	
	public void setFlyingRunSpeed(int flyingRunSpeed)
	{
		_flyingRunSpeed = flyingRunSpeed;
	}
	
	public void setFlyingWalkSpeed(int flyingWalkSpeed)
	{
		_flyingWalkSpeed = flyingWalkSpeed;
	}
	
	public void reduceCurrentMp(int i)
	{
		final Object object = _mpLock;
		synchronized (object)
		{
			_currentMp -= i;
			if (!_mpRegenActive && !isDead())
			{
				startMpRegeneration();
			}
		}
		broadcastStatusUpdate();
	}
	
	public void reduceCurrentHp(int amount, Creature attacker)
	{
		synchronized (_hpLock)
		{
			if (!_hpRegenActive)
			{
				startHpRegeneration();
			}
			
			_currentHp -= amount;
			if (_currentHp <= 0.0)
			{
				_currentHp = 0.0;
				stopHpRegeneration();
				stopMpRegeneration();
				if (_attackTask != null)
				{
					_attackTask.cancel(true);
				}
				if (_hitTask != null)
				{
					_hitTask.cancel(true);
				}
				if (_moveTask != null)
				{
					_moveTask.cancel(true);
				}
				broadcastStatusUpdate();
				final StopMove stop = new StopMove(this);
				final Die die = new Die(this);
				broadcastPacket(stop);
				sendPacket(stop);
				broadcastPacket(die);
				sendPacket(die);
				if (attacker != null)
				{
					attacker.setTarget(null);
				}
				return;
			}
		}
		broadcastStatusUpdate();
	}
	
	public void moveTo(int x, int y, int z, int offset)
	{
		_moveOffset = offset;
		final double distance = getDistance(x, y);
		if ((distance > 0) || (offset > 0))
		{
			if (offset == 0)
			{
				if (isMovingToPawn())
				{
					setMovingToPawn(false);
					setPawnTarget(null);
				}
				calculateMovement(x, y, z, distance);
				final CharMoveToLocation mov = new CharMoveToLocation(this);
				if (getCurrentState() == CreatureState.CASTING)
				{
					setCurrentState(CreatureState.IDLE);
				}
				enableAllSkills();
				broadcastPacket(mov);
				sendPacket(mov);
			}
			else
			{
				if (distance <= offset)
				{
					onTargetReached();
					return;
				}
				if ((getPawnTarget() == null) || (getPawnTarget() != getTarget()))
				{
					setMovingToPawn(true);
					setPawnTarget((Creature) getTarget());
					setPawnOffset(offset);
					calculateMovement(x, y, z, distance);
					final MoveToPawn mov = new MoveToPawn(this, getTarget(), offset);
					broadcastPacket(mov);
					sendPacket(mov);
					return;
				}
				calculateMovement(x, y, z, distance);
			}
		}
		else
		{
			sendPacket(new ActionFailed());
			onTargetReached();
		}
	}
	
	private synchronized void calculateMovement(int x, int y, int z, double distance)
	{
		if (isMoving())
		{
			stopMove();
		}
		if ((getPawnTarget() != null) && (distance <= getAttackRange()) && (getCurrentState() == CreatureState.FOLLOW))
		{
			_moveTask = ThreadPool.schedule(new ArriveTask(this), 3000);
			return;
		}
		int dx = x - getX();
		int dy = y - getY();
		if (_moveOffset > 0)
		{
			if ((distance - _moveOffset) <= 0.0)
			{
				distance = 0.0;
			}
			else
			{
				distance -= _moveOffset - 5;
			}
			final double angle = Math.atan2(-dy, -dx);
			dy = (int) (-(Math.sin(angle) * distance));
			dx = (int) (-(Math.cos(angle) * distance));
		}
		if ((distance > 0.0) || (getPawnTarget() != null))
		{
			final float speed = _effectiveSpeed;
			if (speed == 0.0f)
			{
				_log.warning("speed is 0 for Character oid:" + getObjectId() + " movement canceld");
				return;
			}
			_timeToTarget = (long) ((distance * 1000.0) / speed);
			_xAddition = ((dx / distance) * speed) / 1000.0;
			_yAddition = ((dy / distance) * speed) / 1000.0;
			int heading = (int) (Math.atan2(-dy, -dx) * 10430.378350470453);
			setHeading(heading += 32768);
			final int destinationX = getX() + (int) (_xAddition * _timeToTarget);
			final int destinationY = getY() + (int) (_yAddition * _timeToTarget);
			setXdestination(destinationX);
			setYdestination(destinationY);
			setZdestination(z);
			_moveStartTime = System.currentTimeMillis();
			if (_timeToTarget < 0L)
			{
				_timeToTarget = 0L;
			}
			final ArriveTask newMoveTask = new ArriveTask(this);
			if (getPawnTarget() != null)
			{
				if (getCurrentState() == CreatureState.INTERACT)
				{
					_moveTask = ThreadPool.schedule(newMoveTask, _timeToTarget);
					setIsMoving(true);
					return;
				}
				if ((_timeToTarget < 2000L) && (distance > getAttackRange()))
				{
					_moveTask = ThreadPool.schedule(newMoveTask, _timeToTarget);
				}
				else if (getPawnTarget().isMoving())
				{
					_moveTask = ThreadPool.schedule(newMoveTask, 2000);
				}
				else
				{
					_moveTask = ThreadPool.schedule(newMoveTask, 3000);
				}
			}
			else
			{
				_moveTask = ThreadPool.schedule(newMoveTask, _timeToTarget);
			}
			setIsMoving(true);
		}
	}
	
	protected void stopHitTask()
	{
		if (_hitTask != null)
		{
			_hitTask.cancel(true);
			_hitTask = null;
		}
	}
	
	protected void stopAttackTask()
	{
		if (_attackTask != null)
		{
			_attackTask.cancel(true);
			_attackTask = null;
		}
	}
	
	public void setIsMoving(boolean b)
	{
		_moving = b;
	}
	
	public double getDistance(int x, int y)
	{
		final long dx = x - getX();
		final long dy = y - getY();
		final double distance = Math.sqrt((dx * dx) + (dy * dy));
		return distance;
	}
	
	public Collection<PlayerInstance> broadcastPacket(ServerBasePacket mov)
	{
		final Set<PlayerInstance> nearby = getKnownPlayers();
		for (Creature player : nearby)
		{
			player.sendPacket(mov);
		}
		return nearby;
	}
	
	public void sendPacket(ServerBasePacket mov)
	{
	}
	
	public void onTargetReached()
	{
		if (getPawnTarget() != null)
		{
			final int x = getPawnTarget().getX();
			final int y = getPawnTarget().getY();
			final int z = getPawnTarget().getZ();
			final double distance = getDistance(x, y);
			if (getCurrentState() == CreatureState.FOLLOW)
			{
				calculateMovement(x, y, z, distance);
				return;
			}
			if (((distance > getAttackRange()) && (getCurrentState() == CreatureState.ATTACKING)) || (getPawnTarget().isMoving() && (getCurrentState() != CreatureState.ATTACKING)))
			{
				calculateMovement(x, y, z, distance);
				return;
			}
		}
		stopMove();
		if (getPawnTarget() != null)
		{
			setPawnTarget(null);
			setMovingToPawn(false);
		}
	}
	
	public void setTo(int x, int y, int z, int heading)
	{
		ServerBasePacket setto;
		setX(x);
		setY(y);
		setZ(z);
		setHeading(heading);
		if (isMoving())
		{
			setCurrentState(CreatureState.IDLE);
			setto = new StopMove(this);
			broadcastPacket(setto);
		}
		else
		{
			setto = new SetToLocation(this);
			broadcastPacket(setto);
		}
		final FinishRotation fr = new FinishRotation(this);
		broadcastPacket(fr);
	}
	
	public boolean isDead()
	{
		return _currentHp <= 0.0;
	}
	
	protected void startCombat()
	{
		if (_attackTask == null)
		{
			_attackTask = ThreadPool.schedule(new AttackTask(this), 0);
		}
		// else
		// {
		// _log.warning("Multiple attacks want to start in parallel. Prevented.");
		// }
	}
	
	private void onAttackTimer()
	{
		_attackTask = null;
		final Creature target = (Creature) _attackTarget;
		if (isDead() || (target == null) || target.isDead() || ((getCurrentState() != CreatureState.ATTACKING) && (getCurrentState() != CreatureState.CASTING)) || !target.knownsObject(this) || !knownsObject(target))
		{
			setInCombat(false);
			setCurrentState(CreatureState.IDLE);
			final ActionFailed af = new ActionFailed();
			sendPacket(af);
			return;
		}
		if ((getActiveWeapon().getWeaponType() == Weapon.WEAPON_TYPE_BOW) && !checkAndEquipArrows())
		{
			setInCombat(false);
			setCurrentState(CreatureState.IDLE);
			final ActionFailed af = new ActionFailed();
			sendPacket(af);
			sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_ARROWS));
			return;
		}
		final double distance = getDistance(target.getX(), target.getY());
		if (distance > getAttackRange())
		{
			moveTo(_attackTarget.getX(), _attackTarget.getY(), _attackTarget.getZ(), getAttackRange());
			return;
		}
		if ((getCurrentState() == CreatureState.ATTACKING) && !_currentlyAttacking)
		{
			final Weapon weaponItem = getActiveWeapon();
			if (weaponItem == null)
			{
				setInCombat(false);
				setCurrentState(CreatureState.IDLE);
				final ActionFailed af = new ActionFailed();
				sendPacket(af);
				return;
			}
			if (!_currentlyAttacking)
			{
				_currentlyAttacking = true;
				final int baseDamage = weaponItem.getPDamage();
				final int randomDamage = weaponItem.getRandomDamage();
				int damage = 0;
				boolean crit = false;
				// int hitTarget = Creature.getRnd().nextInt(100);
				final boolean miss = false;
				boolean soulShotUse = false;
				if (!miss)
				{
					int pDef = target.getPhysicalDefense();
					if (pDef == 0)
					{
						pDef = 300;
						if (target instanceof NpcInstance)
						{
							_log.warning("target has bogus stats. check npc2.csv: id " + ((NpcInstance) target).getNpcTemplate().getNpcId());
						}
						else
						{
							_log.warning("target has bogus stats. Pdef was 0. temporary increased to 300");
						}
					}
					damage = ((baseDamage + Rnd.get(randomDamage)) * 70) / pDef;
					final int critHit = Rnd.get(100);
					crit = getCriticalHit() > critHit;
					if (crit)
					{
						damage *= 2;
					}
					if (getActiveSoulshotGrade() == weaponItem.getCrystalType())
					{
						soulShotUse = true;
						damage *= 2;
					}
				}
				if (!isInCombat() && !miss)
				{
					setInCombat(true);
				}
				if (isUsingDualWeapon())
				{
					_hitTask = ThreadPool.schedule(new HitTask(this, target, damage, crit, miss, false), calculateHitSpeed(weaponItem, 1));
					_hitTask = ThreadPool.schedule(new HitTask(this, target, damage, crit, miss, false), calculateHitSpeed(weaponItem, 2));
				}
				else if (getActiveWeapon().getWeaponType() == Weapon.WEAPON_TYPE_BOW)
				{
					if (getCurrentMp() < weaponItem.getMpConsume())
					{
						sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_MP));
						setInCombat(false);
						setCurrentState(CreatureState.IDLE);
						final ActionFailed af = new ActionFailed();
						sendPacket(af);
						return;
					}
					reduceCurrentMp(weaponItem.getMpConsume());
					sendPacket(new SystemMessage(SystemMessage.GETTING_READY_TO_SHOOT_AN_ARROW));
					sendPacket(new SetupGauge(SetupGauge.RED, calculateAttackSpeed(weaponItem) * 2));
					_hitTask = ThreadPool.schedule(new HitTask(this, target, damage, crit, miss, false), calculateHitSpeed(weaponItem, 1));
				}
				else
				{
					_hitTask = ThreadPool.schedule(new HitTask(this, target, damage, crit, miss, false), calculateHitSpeed(weaponItem, 1));
				}
				final Attack attack = new Attack(getObjectId(), _attackTarget.getObjectId(), damage, miss, crit, soulShotUse, getX(), getY(), getZ());
				setActiveSoulshotGrade(0);
				broadcastPacket(attack);
				sendPacket(attack);
			}
		}
	}
	
	protected boolean checkAndEquipArrows()
	{
		return true;
	}
	
	public void addExpAndSp(int addToExp, int addToSp)
	{
	}
	
	public abstract Weapon getActiveWeapon();
	
	protected void onHitTimer(Creature target, int damage, boolean crit, boolean miss, boolean soulshot)
	{
		if (isDead() || target.isDead() || !target.knownsObject(this) || !knownsObject(target))
		{
			setInCombat(false);
			setTarget(null);
			setCurrentState(CreatureState.IDLE);
			final ActionFailed af = new ActionFailed();
			sendPacket(af);
			return;
		}
		if (_currentlyAttacking)
		{
			if (getActiveWeapon().getWeaponType() == Weapon.WEAPON_TYPE_BOW)
			{
				reduceArrowCount();
				_attackTask = ThreadPool.schedule(new AttackTask(this), calculateAttackSpeed(getActiveWeapon()));
			}
			displayHitMessage(damage, crit, miss);
			if (!miss)
			{
				target.reduceCurrentHp(damage, this);
			}
			if (isUsingDualWeapon())
			{
				if (_2ndHit)
				{
					_2ndHit = false;
					_currentlyAttacking = false;
					_attackTask = ThreadPool.schedule(new AttackTask(this), calculateAttackSpeed(getActiveWeapon()));
				}
				else
				{
					_2ndHit = true;
				}
			}
			if (!isUsingDualWeapon())
			{
				_currentlyAttacking = false;
				_attackTask = ThreadPool.schedule(new AttackTask(this), calculateAttackSpeed(getActiveWeapon()));
			}
		}
	}
	
	protected void reduceArrowCount()
	{
	}
	
	protected void displayHitMessage(int damage, boolean crit, boolean miss)
	{
	}
	
	public int getAttackRange()
	{
		return -1;
	}
	
	public boolean isInCombat()
	{
		return _inCombat;
	}
	
	public void setInCombat(boolean inCombat)
	{
		if (inCombat)
		{
			sendPacket(new AutoAttackStart(getObjectId()));
			broadcastPacket(new AutoAttackStart(getObjectId()));
		}
		else
		{
			stopAttackTask();
			stopHitTask();
			sendPacket(new AutoAttackStop(getObjectId()));
			broadcastPacket(new AutoAttackStop(getObjectId()));
			_currentlyAttacking = false;
		}
		_inCombat = inCombat;
	}
	
	public void startAttack(Creature target)
	{
		if (target == null)
		{
			setInCombat(false);
			setCurrentState(CreatureState.IDLE);
			final ActionFailed af = new ActionFailed();
			sendPacket(af);
			return;
		}
		setTarget(target);
		_attackTarget = target;
		setCurrentState(CreatureState.ATTACKING);
		moveTo(target.getX(), target.getY(), target.getZ(), getAttackRange());
	}
	
	@Override
	public void onForcedAttack(PlayerInstance player)
	{
		player.startAttack(this);
	}
	
	public int getActiveSoulshotGrade()
	{
		return _activeSoulShotGrade;
	}
	
	public void setActiveSoulshotGrade(int soulshotGrade)
	{
		_activeSoulShotGrade = soulshotGrade;
	}
	
	public void setMovingToPawn(boolean val)
	{
		_movingToPawn = val;
	}
	
	public void setPawnTarget(Creature target)
	{
		_pawnTarget = target;
	}
	
	public void setPawnOffset(int offset)
	{
		_pawnOffset = offset;
	}
	
	public boolean isMovingToPawn()
	{
		return _movingToPawn;
	}
	
	public Creature getPawnTarget()
	{
		return _pawnTarget;
	}
	
	public int getPawnOffset()
	{
		return _pawnOffset;
	}
	
	public float getEffectiveSpeed()
	{
		return _effectiveSpeed;
	}
	
	public int calculateAttackSpeed(Weapon weaponItem)
	{
		int atkspd = weaponItem.getAttackSpeed();
		if (atkspd == 0)
		{
			atkspd = 325;
		}
		atkspd = ((886 - atkspd) * 5) / 2;
		atkspd = weaponItem.getWeaponType() == Weapon.WEAPON_TYPE_DAGGER ? (atkspd += 50) : (weaponItem.getWeaponType() == Weapon.WEAPON_TYPE_DUALFIST ? (atkspd += 100) : (weaponItem.getWeaponType() == Weapon.WEAPON_TYPE_DUAL ? (atkspd += 100) : ((weaponItem.getItemId() == 248) || (weaponItem.getItemId() == 252) ? (atkspd += 100) : (atkspd += 50))));
		return atkspd;
	}
	
	public int calculateHitSpeed(Weapon weaponItem, int hit)
	{
		int hitspd = weaponItem.getAttackSpeed();
		if (hitspd == 0)
		{
			hitspd = 325;
		}
		hitspd = ((886 - hitspd) * 5) / 2;
		hitspd = weaponItem.getWeaponType() == Weapon.WEAPON_TYPE_DAGGER ? (hitspd -= 50) : (weaponItem.getWeaponType() == Weapon.WEAPON_TYPE_DUALFIST ? (hit == 1 ? (hitspd -= 750) : (hitspd -= 100)) : (weaponItem.getWeaponType() == Weapon.WEAPON_TYPE_DUAL ? (hit == 1 ? (hitspd -= 750) : (hitspd -= 100)) : (((weaponItem.getItemId() == 248) || (weaponItem.getItemId() == 252)) && (weaponItem.getWeaponType() == Weapon.WEAPON_TYPE_FIST) ? (hit == 1 ? (hitspd -= 750) : (hitspd -= 150)) : ((weaponItem.getItemId() != 248) && (weaponItem.getItemId() != 252) && (weaponItem.getWeaponType() == Weapon.WEAPON_TYPE_FIST) ? (hitspd -= 250) : (hitspd -= 200)))));
		return hitspd;
	}
	
	protected boolean isUsingDualWeapon()
	{
		return false;
	}
	
	public void setAttackStatus(boolean status)
	{
		_currentlyAttacking = status;
	}
	
	public void enableAllSkills()
	{
	}
	
	public class DecayTask implements Runnable
	{
		Creature _instance;
		
		public DecayTask(Creature instance)
		{
			_instance = instance;
		}
		
		@Override
		public void run()
		{
			_instance.onDecay();
		}
	}
	
	class MpRegenTask implements Runnable
	{
		Creature _instance;
		
		public MpRegenTask(Creature instance)
		{
			_instance = instance;
		}
		
		@Override
		public void run()
		{
			final Object object = _mpLock;
			synchronized (object)
			{
				double nowMp = _instance.getCurrentMp();
				if (_instance.getCurrentMp() < _instance.getMaxMp())
				{
					_instance.setCurrentMp(nowMp += _instance.getMaxMp() * 0.014);
				}
			}
		}
	}
	
	class HpRegenTask implements Runnable
	{
		Creature _instance;
		
		public HpRegenTask(Creature instance)
		{
			_instance = instance;
		}
		
		@Override
		public void run()
		{
			synchronized (_hpLock)
			{
				double nowHp = _instance.getCurrentHp();
				if (_instance.getCurrentHp() < _instance.getMaxHp())
				{
					if ((nowHp += _instance.getMaxHp() * 0.018) > _instance.getMaxHp())
					{
						nowHp = _instance.getMaxHp();
					}
					_instance.setCurrentHp(nowHp);
				}
			}
		}
	}
	
	class HitTask implements Runnable
	{
		Creature _instance;
		Creature _target;
		int _damage;
		boolean _crit;
		boolean _miss;
		boolean _soulshot;
		
		public HitTask(Creature instance, Creature target, int damage, boolean crit, boolean miss, boolean soulshot)
		{
			_instance = instance;
			_target = target;
			_damage = damage;
			_crit = crit;
			_miss = miss;
			_soulshot = soulshot;
		}
		
		@Override
		public void run()
		{
			_instance.onHitTimer(_target, _damage, _crit, _miss, _soulshot);
		}
	}
	
	class AttackTask implements Runnable
	{
		Creature _instance;
		
		public AttackTask(Creature instance)
		{
			_instance = instance;
		}
		
		@Override
		public void run()
		{
			_instance.onAttackTimer();
		}
	}
	
	class ArriveTask implements Runnable
	{
		Creature _instance;
		
		public ArriveTask(Creature instance)
		{
			_instance = instance;
		}
		
		@Override
		public void run()
		{
			_instance.onTargetReached();
			_moveTask = null;
		}
	}
	
	public void teleToLocation(int x, int y, int z)
	{
		final TeleportToLocation teleportToLocation = new TeleportToLocation(this, x, y, z);
		sendPacket(teleportToLocation);
		broadcastPacket(teleportToLocation);
		World.getInstance().removeVisibleObject(this);
		removeAllKnownObjects();
		setX(x);
		setY(y);
		setZ(z);
		ThreadPool.schedule(() -> World.getInstance().addVisibleObject(this), 2000);
	}
	
	@Override
	public boolean isCreature()
	{
		return true;
	}
}
