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
package com.l2jmobius.gameserver.model.actor;

import com.l2jmobius.Config;
import com.l2jmobius.commons.util.Rnd;
import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.ai.L2CharacterAI;
import com.l2jmobius.gameserver.ai.L2SummonAI;
import com.l2jmobius.gameserver.datatables.SkillTable;
import com.l2jmobius.gameserver.datatables.xml.ExperienceData;
import com.l2jmobius.gameserver.geodata.GeoData;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Party;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.L2Skill.SkillTargetType;
import com.l2jmobius.gameserver.model.L2Skill.SkillType;
import com.l2jmobius.gameserver.model.L2WorldRegion;
import com.l2jmobius.gameserver.model.PetInventory;
import com.l2jmobius.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2SiegeSummonInstance;
import com.l2jmobius.gameserver.model.actor.knownlist.SummonKnownList;
import com.l2jmobius.gameserver.model.actor.position.Location;
import com.l2jmobius.gameserver.model.actor.stat.SummonStat;
import com.l2jmobius.gameserver.model.actor.status.SummonStatus;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jmobius.gameserver.network.serverpackets.NpcInfo;
import com.l2jmobius.gameserver.network.serverpackets.PetDelete;
import com.l2jmobius.gameserver.network.serverpackets.PetStatusShow;
import com.l2jmobius.gameserver.network.serverpackets.PetStatusUpdate;
import com.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.taskmanager.DecayTaskManager;
import com.l2jmobius.gameserver.templates.chars.L2NpcTemplate;
import com.l2jmobius.gameserver.templates.item.L2Weapon;

public abstract class L2Summon extends L2Playable
{
	// private static Logger LOGGER = Logger.getLogger(L2Summon.class);
	
	protected int _pkKills;
	private L2PcInstance _owner;
	private int _attackRange = 36; // Melee range
	boolean _follow = true;
	private boolean _previousFollowStatus = true;
	private int _maxLoad;
	
	private int _chargedSoulShot;
	private int _chargedSpiritShot;
	
	// TODO: currently, all servitors use 1 shot. However, this value
	// should vary depending on the servitor template (id and level)!
	private final int _soulShotsPerHit = 1;
	private final int _spiritShotsPerHit = 1;
	protected boolean _showSummonAnimation;
	
	public class AIAccessor extends L2Character.AIAccessor
	{
		protected AIAccessor()
		{
			// null
		}
		
		public L2Summon getSummon()
		{
			return L2Summon.this;
		}
		
		public boolean isAutoFollow()
		{
			return _follow;
		}
		
		public void doPickupItem(L2Object object)
		{
			L2Summon.this.doPickupItem(object);
		}
	}
	
	public L2Summon(int objectId, L2NpcTemplate template, L2PcInstance owner)
	{
		super(objectId, template);
		getKnownList(); // init knownlist
		getStat(); // init stats
		getStatus(); // init status
		
		_showSummonAnimation = true;
		_owner = owner;
		_ai = new L2SummonAI(new AIAccessor());
		
		// Make sure summon does not spawn in a wall.
		final int x = owner.getX();
		final int y = owner.getY();
		final int z = owner.getZ();
		final Location location = GeoData.getInstance().moveCheck(x, y, z, x + Rnd.get(-100, 100), y + Rnd.get(-100, 100), z);
		setXYZInvisible(location.getX(), location.getY(), location.getZ());
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
					_ai = new L2SummonAI(new AIAccessor());
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
		for (L2PcInstance player : getKnownList().getKnownPlayers().values())
		{
			if (player != null)
			{
				player.sendPacket(new NpcInfo(this, player));
			}
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
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else if (player.getTarget() != this)
		{
			if (Config.DEBUG)
			{
				LOGGER.info("New target selected:" + getObjectId());
			}
			
			player.setTarget(this);
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
			player.sendPacket(my);
			
			// update status hp&mp
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
			su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
			su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
			player.sendPacket(su);
		}
		else if (player.getTarget() == this)
		{
			if (isAutoAttackable(player))
			{
				if (Config.PATHFINDING > 0)
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
				player.sendPacket(ActionFailed.STATIC_PACKET);
				
				if (Config.PATHFINDING > 0)
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
	
	public long getExpForThisLevel()
	{
		if (getLevel() >= ExperienceData.getInstance().getMaxPetLevel())
		{
			return 0;
		}
		return ExperienceData.getInstance().getExpForLevel(getLevel());
	}
	
	public long getExpForNextLevel()
	{
		if (getLevel() >= (ExperienceData.getInstance().getMaxPetLevel() - 1))
		{
			return 0;
		}
		return ExperienceData.getInstance().getExpForLevel(getLevel() + 1);
	}
	
	public final int getKarma()
	{
		return _owner != null ? _owner.getKarma() : 0;
	}
	
	public final byte getPvpFlag()
	{
		return _owner != null ? _owner.getPvpFlag() : 0;
	}
	
	public final L2PcInstance getOwner()
	{
		return _owner;
	}
	
	public final int getNpcId()
	{
		return getTemplate().npcId;
	}
	
	@Override
	protected void doAttack(L2Character target)
	{
		if ((_owner != null) && (_owner == target) && !_owner.isBetrayed())
		{
			sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return;
		}
		if (isInsidePeaceZone(this, target))
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			return;
		}
		if (!target.isAttackable())
		{
			if (!(this instanceof L2SiegeSummonInstance))
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				return;
			}
		}
		
		super.doAttack(target);
	}
	
	public void setPkKills(int pkKills)
	{
		_pkKills = pkKills;
	}
	
	public final int getPkKills()
	{
		return _pkKills;
	}
	
	public final int getMaxLoad()
	{
		return _maxLoad;
	}
	
	public final int getSoulShotsPerHit()
	{
		return _soulShotsPerHit;
	}
	
	public final int getSpiritShotsPerHit()
	{
		return _spiritShotsPerHit;
	}
	
	public void setMaxLoad(int maxLoad)
	{
		_maxLoad = maxLoad;
	}
	
	public void setChargedSoulShot(int shotType)
	{
		_chargedSoulShot = shotType;
	}
	
	public void setChargedSpiritShot(int shotType)
	{
		_chargedSpiritShot = shotType;
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
	public void broadcastStatusUpdate()
	{
		super.broadcastStatusUpdate();
		
		if ((_owner != null) && isVisible())
		{
			_owner.sendPacket(new PetStatusUpdate(this));
		}
	}
	
	public void deleteMe(L2PcInstance owner)
	{
		getAI().stopFollow();
		owner.sendPacket(new PetDelete(getObjectId(), 2));
		
		// FIXME: I think it should really drop items to ground and only owner can take for a while
		giveAllToOwner();
		decayMe();
		getKnownList().removeAllKnownObjects();
		owner.setPet(null);
	}
	
	public synchronized void unSummon(L2PcInstance owner)
	{
		/*
		 * if(isVisible() && !isDead()) { getAI().stopFollow(); owner.sendPacket(new PetDelete(getObjectId(), 2)); if(getWorldRegion() != null) { getWorldRegion().removeFromZones(this); } store(); giveAllToOwner(); decayMe(); getKnownList().removeAllKnownObjects(); owner.setPet(null);
		 * setTarget(null); }
		 */
		
		if (isVisible() && !isDead())
		{
			stopAllEffects();
			
			getAI().stopFollow();
			owner.sendPacket(new PetDelete(getObjectId(), 2));
			
			store();
			
			giveAllToOwner();
			
			stopAllEffects();
			
			final L2WorldRegion oldRegion = getWorldRegion();
			decayMe();
			if (oldRegion != null)
			{
				oldRegion.removeFromZones(this);
			}
			
			getKnownList().removeAllKnownObjects();
			owner.setPet(null);
			setTarget(null);
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
			getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, _owner);
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
	
	public int getControlItemId()
	{
		return 0;
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
		// TODO: Implement?
	}
	
	public void giveAllToOwner()
	{
		// TODO: Implement?
	}
	
	public void store()
	{
		// TODO: Implement?
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
		_owner.setCurrentPetSkill(skill, forceUse, dontMove);
		
		// ************************************* Check Target *******************************************
		
		// Get the target for the skill
		L2Object target = null;
		
		switch (skill.getTargetType())
		{
			// OWNER_PET should be cast even if no target has been found
			case TARGET_OWNER_PET:
			{
				target = _owner;
				break;
			}
			// PARTY, AURA, SELF should be cast even if no target has been found
			case TARGET_PARTY:
			case TARGET_AURA:
			case TARGET_SELF:
			{
				target = this;
				break;
			}
			default:
			{
				// Get the first target of the list
				target = skill.getFirstOfTargetList(this);
				break;
			}
		}
		
		// Check the validity of the target
		if (target == null)
		{
			if (_owner != null)
			{
				_owner.sendPacket(SystemMessageId.TARGET_CANT_FOUND);
			}
			return;
		}
		
		// ************************************* Check skill availability *******************************************
		
		// Check if this skill is enabled (ex : reuse time)
		if (isSkillDisabled(skill) && (_owner != null) && _owner.getAccessLevel().allowPeaceAttack())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
			sm.addString(skill.getName());
			_owner.sendPacket(sm);
			return;
		}
		
		// Check if all skills are disabled
		if (isAllSkillsDisabled() && (_owner != null) && _owner.getAccessLevel().allowPeaceAttack())
		{
			return;
		}
		
		// ************************************* Check Consumables *******************************************
		
		// Check if the summon has enough MP
		if (getCurrentMp() < (getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill)))
		{
			// Send a System Message to the caster
			if (_owner != null)
			{
				_owner.sendPacket(SystemMessageId.NOT_ENOUGH_MP);
			}
			
			return;
		}
		
		// Check if the summon has enough HP
		if (getCurrentHp() <= skill.getHpConsume())
		{
			// Send a System Message to the caster
			if (_owner != null)
			{
				_owner.sendPacket(SystemMessageId.NOT_ENOUGH_HP);
			}
			
			return;
		}
		
		// ************************************* Check Summon State *******************************************
		
		// Check if this is offensive magic skill
		if (skill.isOffensive())
		{
			if ((_owner != null) && (_owner == target) && !_owner.isBetrayed())
			{
				sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				return;
			}
			
			if (isInsidePeaceZone(this, target) && (_owner != null) && !_owner.getAccessLevel().allowPeaceAttack())
			{
				// If summon or target is in a peace zone, send a system message TARGET_IN_PEACEZONE
				sendPacket(new SystemMessage(SystemMessageId.TARGET_IN_PEACEZONE));
				return;
			}
			
			if ((_owner != null) && _owner.isInOlympiadMode() && !_owner.isOlympiadStart())
			{
				// if L2PcInstance is in Olympia and the match isn't already start, send a Server->Client packet ActionFailed
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			// Check if the target is attackable
			if (target instanceof L2DoorInstance)
			{
				if (!((L2DoorInstance) target).isAttackable(_owner))
				{
					return;
				}
			}
			else
			{
				if (!target.isAttackable() && (_owner != null) && _owner.getAccessLevel().allowPeaceAttack())
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
	public void setIsImobilised(boolean value)
	{
		super.setIsImobilised(value);
		
		if (value)
		{
			_previousFollowStatus = _follow;
			
			// if imobilized temporarly disable follow mode
			if (_previousFollowStatus)
			{
				setFollowStatus(false);
			}
		}
		else
		{
			// if not more imobilized restore previous follow mode
			setFollowStatus(_previousFollowStatus);
		}
	}
	
	public void setOwner(L2PcInstance newOwner)
	{
		_owner = newOwner;
	}
	
	/**
	 * @return Returns the showSummonAnimation.
	 */
	public boolean isShowSummonAnimation()
	{
		return _showSummonAnimation;
	}
	
	/**
	 * @param showSummonAnimation The showSummonAnimation to set.
	 */
	public void setShowSummonAnimation(boolean showSummonAnimation)
	{
		_showSummonAnimation = showSummonAnimation;
	}
	
	@Override
	public boolean isInCombat()
	{
		return _owner != null ? _owner.isInCombat() : false;
	}
	
	/**
	 * Servitors' skills automatically change their level based on the servitor's level. Until level 70, the servitor gets 1 lv of skill per 10 levels. After that, it is 1 skill level per 5 servitor levels. If the resulting skill level doesn't exist use the max that does exist!
	 * @see com.l2jmobius.gameserver.model.actor.L2Character#doCast(com.l2jmobius.gameserver.model.L2Skill)
	 */
	@Override
	public void doCast(L2Skill skill)
	{
		final int petLevel = getLevel();
		int skillLevel = petLevel / 10;
		
		if (skill.getSkillType() == SkillType.BUFF)
		{
			if (petLevel > 77)
			{
				skillLevel = (petLevel - 77) + 3; // max buff lvl 11 with pet lvl 85
			}
			else if (petLevel >= 70)
			{
				skillLevel = 3;
			}
			else if (petLevel >= 64)
			{
				skillLevel = 2;
			}
			else
			{
				skillLevel = 1;
			}
		}
		else
		{
			if (petLevel >= 70)
			{
				skillLevel += (petLevel - 65) / 10;
			}
			
			// adjust the level for servitors less than lv 10
			if (skillLevel < 1)
			{
				skillLevel = 1;
			}
		}
		
		L2Skill skillToCast = SkillTable.getInstance().getInfo(skill.getId(), skillLevel);
		
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
		return _owner;
	}
}
