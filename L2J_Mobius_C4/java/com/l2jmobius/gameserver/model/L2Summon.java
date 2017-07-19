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
package com.l2jmobius.gameserver.model;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.GeoData;
import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.ai.L2CharacterAI;
import com.l2jmobius.gameserver.ai.L2SummonAI;
import com.l2jmobius.gameserver.datatables.SkillTable;
import com.l2jmobius.gameserver.model.L2Attackable.AggroInfo;
import com.l2jmobius.gameserver.model.L2Skill.SkillTargetType;
import com.l2jmobius.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jmobius.gameserver.model.actor.knownlist.SummonKnownList;
import com.l2jmobius.gameserver.model.actor.stat.SummonStat;
import com.l2jmobius.gameserver.model.actor.status.SummonStatus;
import com.l2jmobius.gameserver.model.base.Experience;
import com.l2jmobius.gameserver.network.L2GameClient;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jmobius.gameserver.network.serverpackets.NpcInfo;
import com.l2jmobius.gameserver.network.serverpackets.PartySpelled;
import com.l2jmobius.gameserver.network.serverpackets.PetDelete;
import com.l2jmobius.gameserver.network.serverpackets.PetInfo;
import com.l2jmobius.gameserver.network.serverpackets.PetStatusShow;
import com.l2jmobius.gameserver.network.serverpackets.PetStatusUpdate;
import com.l2jmobius.gameserver.network.serverpackets.RelationChanged;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.network.serverpackets.ValidateLocation;
import com.l2jmobius.gameserver.taskmanager.DecayTaskManager;
import com.l2jmobius.gameserver.templates.L2NpcTemplate;
import com.l2jmobius.gameserver.templates.L2Weapon;

public abstract class L2Summon extends L2PlayableInstance
{
	// private static Logger _log = Logger.getLogger(L2Summon.class.getName());
	
	private L2PcInstance _owner;
	
	private int _attackRange = 36; // Melee range
	private boolean _follow = true;
	private boolean _previousFollowStatus = true;
	public boolean _isSiegeGolem = false;
	
	private int _chargedSoulShot;
	private int _chargedSpiritShot;
	
	public class AIAccessor extends L2Character.AIAccessor
	{
		protected AIAccessor()
		{
		}
		
		public L2Summon getSummon()
		{
			return L2Summon.this;
		}
		
		public boolean isAutoFollow()
		{
			return getFollowStatus();
		}
		
		public void doPickupItem(L2Object object)
		{
			L2Summon.this.doPickupItem(object);
		}
	}
	
	public L2Summon(int objectId, L2NpcTemplate template, L2PcInstance owner)
	{
		super(objectId, template);
		getKnownList();
		getStat();
		getStatus();
		
		_showSummonAnimation = true;
		_owner = owner;
		_ai = new L2SummonAI(new L2Summon.AIAccessor());
		
		setXYZInvisible(owner.getX() + 50, owner.getY() + 100, owner.getZ() + 100);
	}
	
	@Override
	public void onSpawn()
	{
		setFollowStatus(true);
		
		updateAndBroadcastStatus(0);
		
		getOwner().sendPacket(new RelationChanged(this, getOwner().getRelation(getOwner()), false));
		
		for (final L2PcInstance player : getOwner().getKnownList().getKnownPlayersInRadius(800))
		{
			player.sendPacket(new RelationChanged(this, getOwner().getRelation(player), isAutoAttackable(player)));
		}
		
		super.onSpawn();
	}
	
	@Override
	public final SummonKnownList getKnownList()
	{
		if ((super.getKnownList() == null) || !(super.getKnownList() instanceof SummonKnownList))
		{
			setKnownList(new SummonKnownList(this));
		}
		return (SummonKnownList) super.getKnownList();
	}
	
	@Override
	public SummonStat getStat()
	{
		if ((super.getStat() == null) || !(super.getStat() instanceof SummonStat))
		{
			setStat(new SummonStat(this));
		}
		return (SummonStat) super.getStat();
	}
	
	@Override
	public SummonStatus getStatus()
	{
		if ((super.getStatus() == null) || !(super.getStatus() instanceof SummonStatus))
		{
			setStatus(new SummonStatus(this));
		}
		return (SummonStatus) super.getStatus();
	}
	
	@Override
	public L2CharacterAI getAI()
	{
		if (_ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
				{
					_ai = new L2SummonAI(new L2Summon.AIAccessor());
				}
			}
		}
		
		return _ai;
	}
	
	@Override
	public L2NpcTemplate getTemplate()
	{
		return (L2NpcTemplate) super.getTemplate();
	}
	
	// this defines the action buttons, 1 for Summon, 2 for Pets
	public abstract int getSummonType();
	
	@Override
	public void updateAbnormalEffect()
	{
		for (final L2PcInstance player : getKnownList().getKnownPlayers().values())
		{
			player.sendPacket(new NpcInfo(this, player, 1));
		}
	}
	
	/**
	 * @return Returns the mountable.
	 */
	public boolean isMountable()
	{
		return false;
	}
	
	@Override
	public void onAction(L2PcInstance player)
	{
		if ((player == _owner) && (player.getTarget() == this))
		{
			player.sendPacket(new PetStatusShow(this));
			player.sendPacket(new ActionFailed());
		}
		else if (player.getTarget() != this)
		{
			if (Config.DEBUG)
			{
				_log.fine("new target selected:" + getObjectId());
			}
			
			player.setTarget(this);
			final MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
			player.sendPacket(my);
			
			player.sendPacket(new ValidateLocation(this));
		}
		else if (player.getTarget() == this)
		{
			player.sendPacket(new ValidateLocation(this));
			if (isAutoAttackable(player))
			{
				if (Config.GEODATA > 0)
				{
					if (GeoData.getInstance().canSeeTarget(player, this))
					{
						player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
						player.onActionRequest();
					}
				}
				else
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
					player.onActionRequest();
				}
			}
			else
			{
				// This Action Failed packet avoids player getting stuck when clicking three or more times
				player.sendPacket(new ActionFailed());
				if (Config.GEODATA > 0)
				{
					if (GeoData.getInstance().canSeeTarget(player, this))
					{
						player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
					}
				}
				else
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
				}
			}
		}
	}
	
	@Override
	public void onActionShift(L2GameClient client)
	{
		// Get the L2PcInstance corresponding to the thread
		final L2PcInstance player = client.getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (player.getTarget() != this)
		{
			player.setTarget(this);
			final MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
			player.sendPacket(my);
		}
		
		player.sendPacket(new ValidateLocation(this));
		
		// This Action Failed packet avoids player getting stuck when shift-clicking
		
		player.sendPacket(new ActionFailed());
	}
	
	public long getExpForThisLevel()
	{
		if (getLevel() >= Experience.LEVEL.length)
		{
			return 0;
		}
		return Experience.LEVEL[getLevel()];
	}
	
	public long getExpForNextLevel()
	{
		if (getLevel() >= (Experience.LEVEL.length - 1))
		{
			return 0;
		}
		return Experience.LEVEL[getLevel() + 1];
	}
	
	public final L2PcInstance getOwner()
	{
		return _owner;
	}
	
	public final int getNpcId()
	{
		return getTemplate().npcId;
	}
	
	public void setChargedSoulShot(int shotType)
	{
		_chargedSoulShot = shotType;
	}
	
	public void setChargedSpiritShot(int shotType)
	{
		_chargedSpiritShot = shotType;
	}
	
	public final short getSoulShotsPerHit()
	{
		if (getTemplate().ss > 1)
		{
			return getTemplate().ss;
		}
		
		return 1;
	}
	
	public final short getSpiritShotsPerHit()
	{
		if (getTemplate().bss > 1)
		{
			return getTemplate().bss;
		}
		
		return 1;
	}
	
	public void followOwner()
	{
		setFollowStatus(true);
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		if (getOwner() != null)
		{
			for (final L2Character TgMob : getKnownList().getKnownCharacters())
			{
				// get the mobs which have aggro on this instance
				if (TgMob instanceof L2Attackable)
				{
					if (((L2Attackable) TgMob).isDead())
					{
						continue;
					}
					
					final AggroInfo info = ((L2Attackable) TgMob).getAggroList().get(this);
					if (info != null)
					{
						((L2Attackable) TgMob).addDamageHate(getOwner(), info.damage, info.hate);
					}
				}
			}
		}
		DecayTaskManager.getInstance().addDecayTask(this);
		return true;
	}
	
	public boolean doDie(L2Character killer, boolean decayed)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		if (!decayed)
		{
			DecayTaskManager.getInstance().addDecayTask(this);
		}
		return true;
	}
	
	public void stopDecay()
	{
		DecayTaskManager.getInstance().cancelDecayTask(this);
	}
	
	@Override
	public void onDecay()
	{
		deleteMe(_owner);
	}
	
	@Override
	public void updateEffectIcons(boolean partyOnly)
	{
		final PartySpelled ps = new PartySpelled(this);
		
		// Go through all effects if any
		final L2Effect[] effects = getAllEffects();
		if ((effects != null) && (effects.length > 0))
		{
			for (final L2Effect effect : effects)
			{
				if ((effect == null) || !effect.getShowIcon())
				{
					continue;
				}
				
				if (effect.getInUse())
				{
					effect.addPartySpelledIcon(ps);
				}
				
			}
		}
		
		getOwner().sendPacket(ps);
	}
	
	@Override
	public void broadcastStatusUpdate()
	{
		super.broadcastStatusUpdate();
		updateAndBroadcastStatus(1);
	}
	
	public void updateAndBroadcastStatus(int val)
	{
		if (getOwner() == null)
		{
			return;
		}
		
		getOwner().sendPacket(new PetInfo(this, val));
		getOwner().sendPacket(new PetStatusUpdate(this));
		if (isVisible())
		{
			broadcastNpcInfo(val);
		}
		updateEffectIcons(true);
	}
	
	public void broadcastNpcInfo(int val)
	{
		for (final L2PcInstance player : getKnownList().getKnownPlayers().values())
		{
			
			if ((player == null) || (player == getOwner()))
			{
				continue;
			}
			player.sendPacket(new NpcInfo(this, player, val));
			
		}
	}
	
	public void deleteMe(L2PcInstance owner)
	{
		getAI().stopFollow();
		owner.sendPacket(new PetDelete(getSummonType(), getObjectId()));
		
		// pet will be deleted along with all its items
		if (getInventory() != null)
		{
			getInventory().destroyAllItems("pet deleted", getOwner(), this);
		}
		decayMe();
		getKnownList().removeAllKnownObjects();
		owner.setPet(null);
		
	}
	
	public void unSummon(L2PcInstance owner)
	{
		if (isVisible() && !isDead())
		{
			// stop HP and MP regeneration
			stopHpMpRegeneration();
			
			getAI().stopFollow();
			owner.sendPacket(new PetDelete(getSummonType(), getObjectId()));
			
			store();
			
			giveAllToOwner();
			
			owner.setPet(null);
			
			stopAllEffects();
			final L2WorldRegion oldRegion = getWorldRegion();
			
			decayMe();
			if (oldRegion != null)
			{
				oldRegion.removeFromZones(this);
			}
			
			getKnownList().removeAllKnownObjects();
			
			setTarget(null);
			for (final int itemId : owner.getAutoSoulShot())
			{
				if ((itemId == 6645) || (itemId == 6646) || (itemId == 6647))
				{
					owner.disableAutoShot(itemId);
				}
			}
		}
	}
	
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
		if (_follow)
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, getOwner());
		}
		else
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
		}
	}
	
	public boolean getFollowStatus()
	{
		return _follow;
	}
	
	public boolean isSiegeGolem()
	{
		return _isSiegeGolem;
	}
	
	public boolean isHungry()
	{
		return false;
	}
	
	public int getWeapon()
	{
		return 0;
	}
	
	public int getArmor()
	{
		return 0;
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return _owner.isAutoAttackable(attacker);
	}
	
	public int getChargedSoulShot()
	{
		return _chargedSoulShot;
	}
	
	public int getChargedSpiritShot()
	{
		return _chargedSpiritShot;
	}
	
	@Override
	public boolean isInCombat()
	{
		return getOwner().isInCombat();
	}
	
	public int getControlItemId()
	{
		return 0;
	}
	
	public int getCurrentFed()
	{
		return 0;
	}
	
	public int getMaxFed()
	{
		return 0;
	}
	
	public int getPetSpeed()
	{
		return getTemplate().baseRunSpd;
	}
	
	public L2Weapon getActiveWeapon()
	{
		return null;
	}
	
	public PetInventory getInventory()
	{
		return null;
	}
	
	protected void doPickupItem(L2Object object)
	{
		
	}
	
	public void giveAllToOwner()
	{
		
	}
	
	public void store()
	{
		
	}
	
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return null;
	}
	
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		return null;
	}
	
	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}
	
	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		return null;
	}
	
	/**
	 * Return the L2Party object of its L2PcInstance owner or null.<BR>
	 * <BR>
	 */
	@Override
	public L2Party getParty()
	{
		if (_owner == null)
		{
			return null;
		}
		return _owner.getParty();
	}
	
	@Override
	public boolean isInvul()
	{
		return _IsInvul || _IsTeleporting || getOwner().isSpawnProtected();
	}
	
	/**
	 * Return True if the L2Character has a Party in progress.<BR>
	 * <BR>
	 */
	@Override
	public boolean isInParty()
	{
		if (_owner == null)
		{
			return false;
		}
		return _owner.getParty() != null;
	}
	
	/**
	 * Check if the active L2Skill can be casted.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Check if the target is correct</li>
	 * <li>Check if the target is in the skill cast range</li>
	 * <li>Check if the summon owns enough HP and MP to cast the skill</li>
	 * <li>Check if all skills are enabled and this skill is enabled</li><BR>
	 * <BR>
	 * <li>Check if the skill is active</li><BR>
	 * <BR>
	 * <li>Notify the AI with AI_INTENTION_CAST and target</li><BR>
	 * <BR>
	 * @param skill The L2Skill to use
	 * @param forceUse used to force ATTACK on players
	 * @param dontMove used to prevent movement, if not in range
	 */
	public void useMagic(L2Skill skill, boolean forceUse, boolean dontMove)
	{
		if ((skill == null) || isDead())
		{
			return;
		}
		
		// Check if the skill is active
		if (skill.isPassive())
		{
			// just ignore the passive skill request. why does the client send it anyway ??
			return;
		}
		
		// ************************************* Check Casting in Progress *******************************************
		
		// If a skill is currently being used
		if (isCastingNow())
		{
			return;
		}
		
		// Set current pet skill
		getOwner().setCurrentPetSkill(skill, forceUse, dontMove);
		
		// ************************************* Check Target *******************************************
		
		// Get the target for the skill
		L2Object target = null;
		
		switch (skill.getTargetType())
		{
			// OWNER_PET should be cast even if no target has been found
			case TARGET_OWNER_PET:
				target = getOwner();
				break;
			// PARTY, AURA, SELF should be cast even if no target has been found
			case TARGET_PARTY:
			case TARGET_AURA:
			case TARGET_SELF:
				target = this;
				break;
			default:
				// Get the first target of the list
				target = skill.getFirstOfTargetList(this);
				break;
		}
		
		// Check the validity of the target
		if (target == null)
		{
			if (getOwner() != null)
			{
				getOwner().sendPacket(new SystemMessage(SystemMessage.TARGET_CANT_FOUND));
			}
			return;
		}
		
		// ************************************* Check skill availability *******************************************
		
		// Check if this skill is enabled (ex : reuse time)
		if (isAllSkillsDisabled() && (getOwner() != null) && (getOwner().getAccessLevel() < Config.GM_PEACEATTACK))
		{
			return;
		}
		
		// ************************************* Check Consumables *******************************************
		
		// Check if the summon has enough MP
		if (getCurrentMp() < (getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill)))
		{
			// Send a System Message to the caster
			if (getOwner() != null)
			{
				getOwner().sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_MP));
			}
			return;
		}
		
		// Check if the summon has enough HP
		if (getCurrentHp() <= skill.getHpConsume())
		{
			// Send a System Message to the caster
			if (getOwner() != null)
			{
				getOwner().sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_HP));
			}
			return;
		}
		
		// ************************************* Check Summon State *******************************************
		
		// Check if this is offensive magic skill
		if (skill.isOffensive())
		{
			if (isInsidePeaceZone(this, target) && (getOwner() != null) && (getOwner().getAccessLevel() < Config.GM_PEACEATTACK))
			{
				// If summon or target is in a peace zone, send a system message TARGET_IN_PEACEZONE
				getOwner().sendPacket(new SystemMessage(SystemMessage.TARGET_IN_PEACEZONE));
				return;
			}
			
			if ((getOwner() != null) && getOwner().isInOlympiadMode() && !getOwner().isOlympiadStart())
			{
				// if L2PcInstance is in Olympiad and the match isn't already start, send a Server->Client packet ActionFailed
				sendPacket(new ActionFailed());
				return;
			}
			
			// Check if the target is attackable
			if (target instanceof L2DoorInstance)
			{
				if (!((L2DoorInstance) target).isAttackable(getOwner()))
				{
					return;
				}
			}
			else
			{
				if (!target.isAttackable() && (getOwner() != null) && (getOwner().getAccessLevel() < Config.GM_PEACEATTACK))
				{
					return;
				}
				
				// Check if a Forced ATTACK is in progress on non-attackable target
				if (!target.isAutoAttackable(this) && !forceUse && (skill.getTargetType() != SkillTargetType.TARGET_AURA) && (skill.getTargetType() != SkillTargetType.TARGET_CLAN) && (skill.getTargetType() != SkillTargetType.TARGET_ALLY) && (skill.getTargetType() != SkillTargetType.TARGET_PARTY) && (skill.getTargetType() != SkillTargetType.TARGET_SELF))
				{
					return;
				}
			}
		}
		
		// Notify the AI with AI_INTENTION_CAST and target
		getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
	}
	
	@Override
	public void setIsImmobilized(boolean value)
	{
		super.setIsImmobilized(value);
		
		if (value)
		{
			_previousFollowStatus = getFollowStatus();
			// if immobilized temporarily disable follow mode
			if (_previousFollowStatus)
			{
				setFollowStatus(false);
			}
		}
		else
		{
			// if not more immobilized restore previous follow mode
			setFollowStatus(_previousFollowStatus);
		}
	}
	
	public void setOwner(L2PcInstance newOwner)
	{
		_owner = newOwner;
	}
	
	/**
	 * Servitors' skills automatically change their level based on the servitor's level. Until level 70, the servitor gets 1 lv of skill per 10 levels. After that, it is 1 skill level per 5 servitor levels. If the resulting skill level doesn't exist use the max that does exist!
	 * @see com.l2jmobius.gameserver.model.L2Character#doCast(com.l2jmobius.gameserver.model.L2Skill)
	 */
	@Override
	public void doCast(L2Skill skill)
	{
		if (!getOwner().checkPvpSkill(getTarget(), skill, true) && (getOwner().getAccessLevel() < Config.GM_PEACEATTACK))
		{
			getOwner().sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			getOwner().sendPacket(new ActionFailed());
			return;
		}
		
		final int petLevel = getLevel();
		int skillLevel = petLevel / 10;
		if (petLevel >= 70)
		{
			skillLevel += (petLevel - 65) / 10;
		}
		
		// adjust the level for servitors less than lv 10
		if (skillLevel < 1)
		{
			skillLevel = 1;
		}
		
		final L2Skill skillToCast = SkillTable.getInstance().getInfo(skill.getId(), skillLevel);
		if (skillToCast != null)
		{
			super.doCast(skillToCast);
		}
		else
		{
			super.doCast(skill);
		}
	}
	
	@Override
	public L2PcInstance getActingPlayer()
	{
		return getOwner();
	}
}