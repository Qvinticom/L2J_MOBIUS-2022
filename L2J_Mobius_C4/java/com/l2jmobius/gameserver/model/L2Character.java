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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.GameTimeController;
import com.l2jmobius.gameserver.GeoData;
import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.ai.CtrlEvent;
import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.ai.L2AttackableAI;
import com.l2jmobius.gameserver.ai.L2CharacterAI;
import com.l2jmobius.gameserver.datatables.DoorTable;
import com.l2jmobius.gameserver.datatables.MapRegionTable;
import com.l2jmobius.gameserver.datatables.MapRegionTable.TeleportWhereType;
import com.l2jmobius.gameserver.datatables.SkillTable;
import com.l2jmobius.gameserver.handler.ISkillHandler;
import com.l2jmobius.gameserver.handler.SkillHandler;
import com.l2jmobius.gameserver.instancemanager.TownManager;
import com.l2jmobius.gameserver.model.L2Skill.SkillTargetType;
import com.l2jmobius.gameserver.model.L2Skill.SkillType;
import com.l2jmobius.gameserver.model.actor.instance.L2BoatInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2ControlTowerInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2FriendlyMobInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2GuardInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2MinionInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcWalkerInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance.SkillDat;
import com.l2jmobius.gameserver.model.actor.instance.L2PetInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2RiftInvaderInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2SiegeFlagInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2SiegeGuardInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jmobius.gameserver.model.actor.knownlist.CharKnownList;
import com.l2jmobius.gameserver.model.actor.stat.CharStat;
import com.l2jmobius.gameserver.model.actor.status.CharStatus;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.Attack;
import com.l2jmobius.gameserver.network.serverpackets.ChangeMoveType;
import com.l2jmobius.gameserver.network.serverpackets.ChangeWaitType;
import com.l2jmobius.gameserver.network.serverpackets.CharMoveToLocation;
import com.l2jmobius.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jmobius.gameserver.network.serverpackets.MagicSkillCanceld;
import com.l2jmobius.gameserver.network.serverpackets.MagicSkillLaunched;
import com.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jmobius.gameserver.network.serverpackets.NpcInfo;
import com.l2jmobius.gameserver.network.serverpackets.Revive;
import com.l2jmobius.gameserver.network.serverpackets.ServerObjectInfo;
import com.l2jmobius.gameserver.network.serverpackets.SetupGauge;
import com.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import com.l2jmobius.gameserver.network.serverpackets.StopMove;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.network.serverpackets.TeleportToLocation;
import com.l2jmobius.gameserver.pathfinding.AbstractNodeLoc;
import com.l2jmobius.gameserver.pathfinding.PathFinding;
import com.l2jmobius.gameserver.skills.Calculator;
import com.l2jmobius.gameserver.skills.Formulas;
import com.l2jmobius.gameserver.skills.Stats;
import com.l2jmobius.gameserver.skills.funcs.Func;
import com.l2jmobius.gameserver.templates.L2CharTemplate;
import com.l2jmobius.gameserver.templates.L2NpcTemplate;
import com.l2jmobius.gameserver.templates.L2Weapon;
import com.l2jmobius.gameserver.templates.L2WeaponType;
import com.l2jmobius.gameserver.util.Util;
import com.l2jmobius.util.Rnd;

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastTable;

/**
 * Mother class of all character objects of the world (PC, NPC...)<BR>
 * <BR>
 * L2Character :<BR>
 * <BR>
 * <li>L2CastleGuardInstance</li>
 * <li>L2DoorInstance</li>
 * <li>L2NpcInstance</li>
 * <li>L2PlayableInstance</li><BR>
 * <BR>
 * <B><U> Concept of L2CharTemplate</U> :</B><BR>
 * <BR>
 * Each L2Character owns generic and static properties (ex : all Keltir have the same number of HP...). All of those properties are stored in a different template for each type of L2Character. Each template is loaded once in the server cache memory (reduce memory use). When a new instance of
 * L2Character is spawned, server just create a link between the instance and the template. This link is stored in <B>_template</B><BR>
 * <BR>
 * @version $Revision: 1.53.2.45.2.34 $ $Date: 2005/04/11 10:06:08 $
 */
public abstract class L2Character extends L2Object
{
	protected static final Logger _log = Logger.getLogger(L2Character.class.getName());
	
	// =========================================================
	// Data Field
	private List<L2Character> _AttackByList;
	private L2Skill _lastSkillCast;
	private boolean _IsAfraid = false; // Flee in a random direction
	private boolean _IsConfused = false; // Attack anyone randomly
	private boolean _IsFakeDeath = false; // Fake death
	private boolean _IsFlying = false; // Is flying Wyvern?
	private boolean _IsMuted = false; // Cannot use magic skills
	private boolean _IsPhysicalMuted = false; // Cannot use physical skills
	private boolean _IsDead = false;
	private boolean _IsOverloaded = false; // the char is carrying too much
	private boolean _IsParalyzed = false;
	private boolean _IsRiding = false; // Is Riding strider?
	private boolean _IsPendingRevive = false;
	private boolean _IsRooted = false; // Cannot move until root timed out
	private boolean _IsRunning = false;
	private boolean _IsSleeping = false; // Cannot move/attack until sleep timed out or monster is attacked
	private boolean _IsStunned = false; // Cannot move/attack until stun timed out
	private boolean _IsSingleSpear = false;
	private boolean _isSummoned = false;
	protected boolean _IsTeleporting = false;
	protected boolean _IsInvul = false;
	protected boolean _showSummonAnimation = false;
	private boolean _champion = false;
	private boolean _AIdisabled = false;
	private L2Character _LastBuffer = null;
	private int _LastHealAmount = 0;
	private CharStat _Stat;
	private CharStatus _Status;
	private L2CharTemplate _Template; // The link on the L2CharTemplate object containing generic and static properties of this L2Character type (ex : Max HP, Speed...)
	private String _Title;
	private String _aiClass = "default";
	private double _hpUpdateIncCheck = .0;
	private double _hpUpdateDecCheck = .0;
	private double _hpUpdateInterval = .0;
	
	private int _auraColor = 0;
	
	private int _immobileLevel = 0;
	
	/** Table of Calculators containing all used calculator */
	private Calculator[] _Calculators;
	
	/** FastMap(Integer, L2Skill) containing all skills of the L2Character */
	protected final Map<Integer, L2Skill> _Skills;
	
	/** Zone system */
	
	public static final byte ZONE_PVP = 0;
	public static final byte ZONE_PEACE = 1;
	
	public static final byte ZONE_SIEGE = 2;
	public static final byte ZONE_MOTHERTREE = 3;
	public static final byte ZONE_CLANHALL = 4;
	public static final byte ZONE_UNUSED = 5;
	public static final byte ZONE_NOLANDING = 6;
	public static final byte ZONE_WATER = 7;
	public static final byte ZONE_JAIL = 8;
	public static final byte ZONE_MONSTERTRACK = 9;
	public static final byte ZONE_NOHQ = 10;
	public static final byte ZONE_BOSS = 11;
	public static final byte ZONE_EFFECT = 12;
	public static final byte ZONE_NOSTORE = 13;
	
	private final byte[] _zones = new byte[14];
	
	protected byte _zoneValidateCounter = 4;
	
	public final boolean isInsideZone(final byte zone)
	{
		return zone == ZONE_PVP ? (_zones[ZONE_PVP] > 0) && (_zones[ZONE_PEACE] == 0) : _zones[zone] > 0;
	}
	
	public final void setInsideZone(final byte zone, final boolean state)
	{
		if (state)
		{
			_zones[zone]++;
		}
		else
		{
			_zones[zone]--;
			if (_zones[zone] < 0)
			{
				_zones[zone] = 0;
			}
		}
	}
	
	/**
	 * This will return true if the player is GM,<br>
	 * but if the player is not GM it will return false.
	 * @return GM status
	 */
	public boolean isGM()
	{
		return false;
	}
	
	// =========================================================
	// Constructor
	
	/**
	 * Constructor of L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * Each L2Character owns generic and static properties (ex : all Keltir have the same number of HP...). All of those properties are stored in a different template for each type of L2Character. Each template is loaded once in the server cache memory (reduce memory use). When a new instance of
	 * L2Character is spawned, server just create a link between the instance and the template This link is stored in <B>_template</B><BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Set the _template of the L2Character</li>
	 * <li>Set _overloaded to false (the character can take more items)</li><BR>
	 * <BR>
	 * <li>If L2Character is a L2NPCInstance, copy skills from template to object</li>
	 * <li>If L2Character is a L2NPCInstance, link _calculators to NPC_STD_CALCULATOR</li><BR>
	 * <BR>
	 * <li>If L2Character is NOT a L2NPCInstance, create an empty _skills slot</li>
	 * <li>If L2Character is a L2PcInstance or L2Summon, copy basic Calculator set to object</li><BR>
	 * <BR>
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2CharTemplate to apply to the object
	 */
	public L2Character(int objectId, L2CharTemplate template)
	{
		super(objectId);
		getKnownList();
		
		// Set its template to the new L2Character
		_Template = template;
		
		if ((template != null) && (this instanceof L2NpcInstance))
		{
			// Copy the Standard Calculators of the L2NPCInstance in _calculators
			_Calculators = NPC_STD_CALCULATOR;
			
			// Copy the skills of the L2NPCInstance from its template to the L2Character Instance
			// The skills list can be affected by spell effects so it's necessary to make a copy
			// to avoid that a spell affecting a L2NPCInstance, affects others L2NPCInstance of the same type too.
			_Skills = ((L2NpcTemplate) template).getSkills();
			if (_Skills != null)
			{
				for (final Map.Entry<Integer, L2Skill> skill : _Skills.entrySet())
				{
					addStatFuncs(skill.getValue().getStatFuncs(null, this));
				}
			}
		}
		else
		{
			// If L2Character is a L2PcInstance or a L2Summon, create the basic calculator set
			_Calculators = new Calculator[Stats.NUM_STATS];
			
			if ((this instanceof L2Summon) && (template != null))
			{
				_Skills = ((L2NpcTemplate) template).getSkills();
				if (_Skills != null)
				{
					for (final Map.Entry<Integer, L2Skill> skill : _Skills.entrySet())
					{
						addStatFuncs(skill.getValue().getStatFuncs(null, this));
					}
				}
			}
			else
			{
				// Initialize the FastMap _skills to null
				_Skills = new FastMap<Integer, L2Skill>().shared();
			}
			
			Formulas.getInstance().addFuncsToNewCharacter(this);
		}
		
		if (!(this instanceof L2PcInstance) && !(this instanceof L2MonsterInstance) && !(this instanceof L2GuardInstance) && !(this instanceof L2SiegeGuardInstance) && !(this instanceof L2ControlTowerInstance) && !(this instanceof L2SummonInstance) && !(this instanceof L2DoorInstance) && !(this instanceof L2SiegeFlagInstance) && !(this instanceof L2PetInstance) && !(this instanceof L2FriendlyMobInstance))
		{
			setIsInvul(true);
		}
	}
	
	protected void initCharStatusUpdateValues()
	{
		_hpUpdateInterval = getMaxHp() / 352.0; // MAX_HP div MAX_HP_BAR_PX
		_hpUpdateIncCheck = getMaxHp();
		_hpUpdateDecCheck = getMaxHp() - _hpUpdateInterval;
	}
	
	// =========================================================
	// Event - Public
	
	/**
	 * Remove the L2Character from the world when the decay task is launched.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from _allObjects of L2World </B></FONT><BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packets to players</B></FONT><BR>
	 * <BR>
	 */
	public void onDecay()
	{
		final L2WorldRegion reg = getWorldRegion();
		
		decayMe();
		
		if (reg != null)
		{
			reg.removeFromZones(this);
		}
		
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		
		_showSummonAnimation = false;
		revalidateZone(true);
	}
	
	public void onTeleported()
	{
		if (!isTeleporting())
		{
			return;
		}
		
		if (this instanceof L2Summon)
		{
			((L2Summon) this).getOwner().sendPacket(new TeleportToLocation(this, getPosition().getX(), getPosition().getY(), getPosition().getZ()));
		}
		
		if (isSummoned())
		{
			// Add the L2Object spawn in the world as a visible object
			L2World.getInstance().addVisibleObject(this, getPosition().getWorldRegion(), null);
			setIsSummoned(false);
		}
		else
		{
			spawnMe(getPosition().getX(), getPosition().getY(), getPosition().getZ());
		}
		
		setIsTeleporting(false);
	}
	
	// =========================================================
	// Method - Public
	
	/**
	 * Add L2Character instance that is attacking to the attacker list.<BR>
	 * <BR>
	 * @param player The L2Character that attcks this one
	 */
	public void addAttackerToAttackByList(L2Character player)
	{
		if ((player == null) || (player == this) || (getAttackByList() == null) || getAttackByList().contains(player))
		{
			return;
		}
		
		getAttackByList().add(player);
	}
	
	/**
	 * Send a packet to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>. In order to inform other players of state modification on the L2Character, server just need to go through _knownPlayers to send Server->Client Packet<BR>
	 * <BR>
	 * @param mov
	 */
	public void broadcastPacket(L2GameServerPacket mov)
	{
		for (final L2PcInstance player : getKnownList().getKnownPlayers().values())
		{
			player.sendPacket(mov);
		}
	}
	
	/**
	 * Returns true if hp update should be done, false if not
	 * @param barPixels
	 * @return boolean
	 */
	protected boolean needHpUpdate(int barPixels)
	{
		final double currentHp = getCurrentHp();
		if ((currentHp <= 1.0) || (getMaxHp() < barPixels))
		{
			return true;
		}
		
		if ((currentHp <= _hpUpdateDecCheck) || (currentHp >= _hpUpdateIncCheck))
		{
			if (currentHp == getMaxHp())
			{
				_hpUpdateIncCheck = currentHp + 1;
				_hpUpdateDecCheck = currentHp - _hpUpdateInterval;
			}
			else
			{
				final double doubleMulti = currentHp / _hpUpdateInterval;
				int intMulti = (int) doubleMulti;
				
				_hpUpdateDecCheck = _hpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				_hpUpdateIncCheck = _hpUpdateDecCheck + _hpUpdateInterval;
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Create the Server->Client packet StatusUpdate with current HP and MP</li>
	 * <li>Send the Server->Client packet StatusUpdate with current HP and MP to all L2Character called _statusListener that must be informed of HP/MP updates of this L2Character</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND CP information</B></FONT><BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance : Send current HP,MP and CP to the L2PcInstance and only current HP, MP and Level to all other L2PcInstance of the Party</li><BR>
	 * <BR>
	 */
	public void broadcastStatusUpdate()
	{
		if (getStatus().getStatusListener().isEmpty())
		{
			return;
		}
		
		if (!needHpUpdate(352))
		{
			return;
		}
		
		if (Config.DEBUG)
		{
			_log.fine("Broadcast Status Update for " + getObjectId() + "(" + getName() + "). HP: " + getCurrentHp());
		}
		
		// Create the Server->Client packet StatusUpdate with current HP
		final StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
		
		// Go through the StatusListener
		// Send the Server->Client packet StatusUpdate with current HP and MP
		
		synchronized (getStatus().getStatusListener())
		{
			for (final L2Character temp : getStatus().getStatusListener())
			{
				try
				{
					temp.sendPacket(su);
				}
				catch (final NullPointerException e)
				{
				}
			}
		}
	}
	
	/**
	 * Not Implemented.<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance</li><BR>
	 * <BR>
	 * @param mov
	 */
	public void sendPacket(L2GameServerPacket mov)
	{
		// default implementation
	}
	
	public void teleToLocation(int x, int y, int z)
	{
		teleToLocation(x, y, z, false);
	}
	
	/**
	 * Teleport a L2Character and its pet if necessary.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Stop the movement of the L2Character</li>
	 * <li>Set the x,y,z position of the L2Object and if necessary modify its _worldRegion</li>
	 * <li>Send a Server->Client packet TeleportToLocationt to the L2Character AND to all L2PcInstance in its _KnownPlayers</li>
	 * <li>Modify the position of the pet if necessary</li><BR>
	 * <BR>
	 * @param x
	 * @param y
	 * @param z
	 * @param allowRandomOffset
	 */
	public void teleToLocation(int x, int y, int z, boolean allowRandomOffset)
	{
		if (_IsPendingRevive)
		{
			doRevive();
		}
		
		// Stop movement
		stopMove(null);
		abortAttack();
		abortCast();
		
		setIsTeleporting(true);
		setTarget(null);
		
		if (Config.RESPAWN_RANDOM_ENABLED && allowRandomOffset)
		{
			x += Rnd.get(-Config.RESPAWN_RANDOM_MAX_OFFSET, Config.RESPAWN_RANDOM_MAX_OFFSET);
			y += Rnd.get(-Config.RESPAWN_RANDOM_MAX_OFFSET, Config.RESPAWN_RANDOM_MAX_OFFSET);
		}
		
		z += 5;
		
		if (Config.DEBUG)
		{
			_log.fine("Teleporting to: " + x + ", " + y + ", " + z);
		}
		
		final L2WorldRegion reg = getWorldRegion();
		
		// Send a Server->Client packet TeleportToLocation to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
		broadcastPacket(new TeleportToLocation(this, x, y, z));
		
		decayMe();
		
		// Set the x,y,z position of the L2Object and if necessary modify its _worldRegion
		setXYZ(x, y, z);
		
		if (!(this instanceof L2PcInstance) || ((L2PcInstance) this).inOfflineMode())
		{
			onTeleported();
		}
		
		if (reg != null)
		{
			reg.revalidateZones(this);
		}
	}
	
	public void teleToLocation(Location loc, boolean allowRandomOffset)
	{
		teleToLocation(loc.getX(), loc.getY(), loc.getZ(), allowRandomOffset);
	}
	
	public void teleToLocation(TeleportWhereType teleportWhere)
	{
		teleToLocation(MapRegionTable.getInstance().getTeleToLocation(this, teleportWhere), true);
	}
	
	// =========================================================
	// Method - Private
	
	/**
	 * Launch a physical attack against a target (Simple, Bow, Pole or Dual).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Get the active weapon (always equiped in the right hand)</li><BR>
	 * <BR>
	 * <li>If weapon is a bow, check for arrows, MP and bow re-use delay (if necessary, equip the L2PcInstance with arrows in left hand)</li>
	 * <li>If weapon is a bow, consumme MP and set the new period of bow non re-use</li><BR>
	 * <BR>
	 * <li>Get the Attack Speed of the L2Character (delay (in milliseconds) before next attack)</li>
	 * <li>Select the type of attack to start (Simple, Bow, Pole or Dual) and verify if SoulShot are charged then start calculation</li>
	 * <li>If the Server->Client packet Attack contains at least 1 hit, send the Server->Client packet Attack to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character</li>
	 * <li>Notify AI with EVT_READY_TO_ACT</li><BR>
	 * <BR>
	 * @param target The L2Character targeted
	 */
	protected void doAttack(L2Character target)
	{
		if (Config.DEBUG)
		{
			_log.fine(getName() + " doAttack: target=" + target);
		}
		
		if (isAlikeDead() || (target == null) || ((this instanceof L2NpcInstance) && target.isAlikeDead()) || ((this instanceof L2PcInstance) && target.isDead() && !target.isFakeDeath()) || !getKnownList().knowsObject(target) || ((this instanceof L2PcInstance) && isDead()))
		{
			// If L2PcInstance is dead or the target is dead, the action is stopped
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			sendPacket(new ActionFailed());
			return;
		}
		
		if (isAttackingDisabled())
		{
			return;
		}
		
		if (this instanceof L2PcInstance)
		
		{
			if (((L2PcInstance) this).inObserverMode())
			{
				sendPacket(new SystemMessage(SystemMessage.OBSERVERS_CANNOT_PARTICIPATE));
				sendPacket(new ActionFailed());
				return;
			}
			
			if (((L2PcInstance) this).isFlying())
			{
				((L2PcInstance) this).sendMessage("You cannot attack while flying.");
				sendPacket(new ActionFailed());
				return;
			}
			
			// Checking if target has moved to peace zone
			if (target.isInsidePeaceZone(((L2PcInstance) this)))
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				sendPacket(new ActionFailed());
				return;
			}
		}
		
		else if (isInsidePeaceZone(this, target))
		{
			
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			sendPacket(new ActionFailed());
			return;
		}
		
		// Get the active weapon instance (always equiped in the right hand)
		final L2ItemInstance weaponInst = getActiveWeaponInstance();
		
		// Get the active weapon item corresponding to the active weapon instance (always equiped in the right hand)
		final L2Weapon weaponItem = getActiveWeaponItem();
		
		if ((weaponItem != null) && (weaponItem.getItemType() == L2WeaponType.ROD))
		{
			// You can't make an attack with a fishing pole.
			sendPacket(new SystemMessage(SystemMessage.CANNOT_ATTACK_WITH_FISHING_POLE));
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			
			sendPacket(new ActionFailed());
			return;
		}
		
		// GeoData Los Check here (or dz > 1000)
		if (!GeoData.getInstance().canSeeTarget(this, target))
		{
			sendPacket(new SystemMessage(SystemMessage.CANT_SEE_TARGET));
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			sendPacket(new ActionFailed());
			return;
		}
		
		// Check for a bow
		if (((weaponItem != null) && (weaponItem.getItemType() == L2WeaponType.BOW)))
		{
			// Check for arrows and MP
			if (this instanceof L2PcInstance)
			{
				// Equip arrows needed in left hand and send a Server->Client packet ItemList to the L2PcINstance then return True
				if (!checkAndEquipArrows())
				{
					// Cancel the action because the L2PcInstance have no arrow
					getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					sendPacket(new ActionFailed());
					sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_ARROWS));
					return;
				}
				
				// Verify if the bow can be used
				if (_disableBowAttackEndTime <= GameTimeController.getGameTicks())
				{
					// Verify if L2PcInstance owns enough MP
					final int saMpConsume = (int) getStat().calcStat(Stats.MP_CONSUME, 0, null, null);
					final int mpConsume = saMpConsume == 0 ? weaponItem.getMpConsume() : saMpConsume;
					
					if (getCurrentMp() < mpConsume)
					{
						// If L2PcInstance doesn't have enough MP, stop the attack
						
						ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000);
						
						sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_MP));
						sendPacket(new ActionFailed());
						return;
					}
					// If L2PcInstance have enough MP, the bow consumes it
					getStatus().reduceMp(mpConsume);
					
					// Set the period of bow non re-use
					_disableBowAttackEndTime = (5 * GameTimeController.TICKS_PER_SECOND) + GameTimeController.getGameTicks();
				}
				else
				{
					// Cancel the action because the bow can't be re-use at this moment
					ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000);
					
					sendPacket(new ActionFailed());
					return;
				}
			}
			else if (this instanceof L2NpcInstance)
			{
				if (_disableBowAttackEndTime > GameTimeController.getGameTicks())
				{
					return;
				}
			}
		}
		
		// Add the L2PcInstance to _knownObjects and _knownPlayer of the target
		target.getKnownList().addKnownObject(this);
		
		// Reduce the current CP if TIREDNESS configuration is activated
		if (Config.ALT_GAME_TIREDNESS)
		{
			setCurrentCp(getCurrentCp() - 10);
		}
		
		// Recharge any active auto soulshot tasks for player (or player's summon if one exists).
		if (this instanceof L2PcInstance)
		{
			((L2PcInstance) this).rechargeAutoSoulShot(true, false, false);
		}
		else if (this instanceof L2Summon)
		{
			((L2Summon) this).getOwner().rechargeAutoSoulShot(true, false, true);
		}
		
		// Verify if soulshots are charged.
		boolean wasSSCharged;
		
		if ((this instanceof L2Summon) && !(this instanceof L2PetInstance))
		{
			wasSSCharged = (((L2Summon) this).getChargedSoulShot() != L2ItemInstance.CHARGED_NONE);
		}
		else if (this instanceof L2NpcInstance)
		{
			wasSSCharged = ((L2NpcInstance) this).rechargeAutoSoulShot(true, false);
		}
		else
		{
			wasSSCharged = ((weaponInst != null) && (weaponInst.getChargedSoulshot() != L2ItemInstance.CHARGED_NONE));
		}
		
		// Get the Attack Speed of the L2Character (delay (in milliseconds) before next attack)
		final int timeAtk = calculateTimeBetweenAttacks(target, weaponItem);
		// the hit is calculated to happen halfway to the animation - might need further tuning e.g. in bow case
		final int timeToHit = timeAtk / 2;
		_attackEndTime = GameTimeController.getGameTicks();
		_attackEndTime += (timeAtk / GameTimeController.MILLIS_IN_TICK);
		_attackEndTime -= 1;
		
		int ssGrade = 0;
		
		if (weaponItem != null)
		{
			ssGrade = weaponItem.getCrystalType();
		}
		
		// Create a Server->Client packet Attack
		final Attack attack = new Attack(this, wasSSCharged, ssGrade);
		
		boolean hitted;
		
		// Set the Attacking Body part to CHEST
		setAttackingBodypart();
		
		// Make sure that char is facing selected target
		setHeading(Util.calculateHeadingFrom(this, target));
		
		// Get the Attack Reuse Delay of the L2Weapon
		final int reuse = calculateReuseTime(target, weaponItem);
		
		// Select the type of attack to start
		if (weaponItem == null)
		{
			hitted = doAttackHitSimple(attack, target, timeToHit);
		}
		else if (weaponItem.getItemType() == L2WeaponType.BOW)
		{
			hitted = doAttackHitByBow(attack, target, timeAtk, reuse);
		}
		else if ((weaponItem.getItemType() == L2WeaponType.POLE) && !isSingleSpear())
		{
			hitted = doAttackHitByPole(attack, target, timeToHit);
		}
		else if (isUsingDualWeapon())
		{
			hitted = doAttackHitByDual(attack, target, timeToHit);
		}
		else
		{
			hitted = doAttackHitSimple(attack, target, timeToHit);
		}
		
		// Flag the attacker if it's a L2PcInstance outside a PvP area
		final L2PcInstance player = getActingPlayer();
		if (player != null)
		{
			if (player.getPet() != target)
			{
				player.updatePvPStatus(target);
			}
		}
		
		// Check if hit isn't missed
		if (!hitted)
		{
			// Abort the attack of the L2Character and send Server->Client ActionFailed packet
			_attacking = 0;
			sendPacket(new ActionFailed());
		}
		else
		{
			/*
			 * ADDED BY nexus - 2006-08-17 As soon as we know that our hit landed, we must discharge any active soulshots. This must be done so to avoid unwanted soulshot consumption.
			 */
			
			// If we didn't miss the hit, discharge the shoulshots, if any
			if ((this instanceof L2Summon) && !(this instanceof L2PetInstance))
			{
				((L2Summon) this).setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
			}
			else
			{
				if (weaponInst != null)
				{
					weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
				}
			}
		}
		
		// If the Server->Client packet Attack contains at least 1 hit, send the Server->Client packet Attack
		// to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
		if (attack.hasHits())
		{
			broadcastPacket(attack);
		}
		
		// Notify AI with EVT_READY_TO_ACT
		ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), timeAtk + reuse);
	}
	
	/**
	 * Launch a Bow attack.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Calculate if hit is missed or not</li>
	 * <li>Consumme arrows</li>
	 * <li>If hit isn't missed, calculate if shield defense is efficient</li>
	 * <li>If hit isn't missed, calculate if hit is critical</li>
	 * <li>If hit isn't missed, calculate physical damages</li>
	 * <li>If the L2Character is a L2PcInstance, Send a Server->Client packet SetupGauge</li>
	 * <li>Create a new hit task with Medium priority</li>
	 * <li>Calculate and set the disable delay of the bow in function of the Attack Speed</li>
	 * <li>Add this hit to the Server-Client packet Attack</li><BR>
	 * <BR>
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The L2Character targeted
	 * @param sAtk The Attack Speed of the attacker
	 * @param reuse
	 * @return True if the hit isn't missed
	 */
	private boolean doAttackHitByBow(Attack attack, L2Character target, int sAtk, int reuse)
	{
		int damage1 = 0;
		boolean shld1 = false;
		boolean crit1 = false;
		
		// Consume arrows
		reduceArrowCount();
		
		// Calculate if hit is missed or not
		final boolean miss1 = Formulas.getInstance().calcHitMiss(this, target);
		
		_move = null;
		
		// Check if hit isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient
			shld1 = Formulas.getInstance().calcShldUse(this, target);
			
			// Calculate if hit is critical
			crit1 = Formulas.getInstance().calcCrit(getStat().getCriticalHit(target, null));
			
			// Calculate physical damages
			damage1 = (int) Formulas.getInstance().calcPhysDam(this, target, null, shld1, crit1, false, attack._soulshot);
		}
		
		// Check if the L2Character is a L2PcInstance
		if (this instanceof L2PcInstance)
		{
			// Send a system message
			sendPacket(new SystemMessage(SystemMessage.GETTING_READY_TO_SHOOT_AN_ARROW));
			
			// Send a Server->Client packet SetupGauge
			final SetupGauge sg = new SetupGauge(SetupGauge.RED, sAtk + reuse);
			sendPacket(sg);
		}
		
		// Create a new hit task with Medium priority
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack._soulshot, shld1), sAtk);
		
		// Calculate and set the disable delay of the bow in function of the Attack Speed
		_disableBowAttackEndTime = ((sAtk + reuse) / GameTimeController.MILLIS_IN_TICK) + GameTimeController.getGameTicks();
		
		// Add this hit to the Server-Client packet Attack
		attack.addHit(target, damage1, miss1, crit1, shld1);
		
		// Return true if hit isn't missed
		return !miss1;
	}
	
	/**
	 * Launch a Dual attack.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Calculate if hits are missed or not</li>
	 * <li>If hits aren't missed, calculate if shield defense is efficient</li>
	 * <li>If hits aren't missed, calculate if hit is critical</li>
	 * <li>If hits aren't missed, calculate physical damages</li>
	 * <li>Create 2 new hit tasks with Medium priority</li>
	 * <li>Add those hits to the Server-Client packet Attack</li><BR>
	 * <BR>
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The L2Character targeted
	 * @param sAtk
	 * @return True if hit 1 or hit 2 isn't missed
	 */
	private boolean doAttackHitByDual(Attack attack, L2Character target, int sAtk)
	{
		int damage1 = 0;
		int damage2 = 0;
		boolean shld1 = false;
		boolean shld2 = false;
		boolean crit1 = false;
		boolean crit2 = false;
		
		// Calculate if hits are missed or not
		final boolean miss1 = Formulas.getInstance().calcHitMiss(this, target);
		final boolean miss2 = Formulas.getInstance().calcHitMiss(this, target);
		
		// Check if hit 1 isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient against hit 1
			shld1 = Formulas.getInstance().calcShldUse(this, target);
			
			// Calculate if hit 1 is critical
			crit1 = Formulas.getInstance().calcCrit(getStat().getCriticalHit(target, null));
			
			// Calculate physical damages of hit 1
			damage1 = (int) Formulas.getInstance().calcPhysDam(this, target, null, shld1, crit1, true, attack._soulshot);
			damage1 /= 2;
		}
		
		// Check if hit 2 isn't missed
		if (!miss2)
		{
			// Calculate if shield defense is efficient against hit 2
			shld2 = Formulas.getInstance().calcShldUse(this, target);
			
			// Calculate if hit 2 is critical
			crit2 = Formulas.getInstance().calcCrit(getStat().getCriticalHit(target, null));
			
			// Calculate physical damages of hit 2
			damage2 = (int) Formulas.getInstance().calcPhysDam(this, target, null, shld2, crit2, true, attack._soulshot);
			damage2 /= 2;
		}
		
		// Create a new hit task with Medium priority for hit 1
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack._soulshot, shld1), sAtk / 2);
		
		// Create a new hit task with Medium priority for hit 2 with a higher delay
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage2, crit2, miss2, attack._soulshot, shld2), sAtk);
		
		// Add those hits to the Server-Client packet Attack
		attack.addHit(target, damage1, miss1, crit1, shld1);
		attack.addHit(target, damage2, miss2, crit2, shld2);
		
		// Return true if hit 1 or hit 2 isn't missed
		return (!miss1 || !miss2);
	}
	
	/**
	 * Launch a Pole attack.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Get all visible objects in a spheric area near the L2Character to obtain possible targets</li>
	 * <li>If possible target is the L2Character targeted, launch a simple attack against it</li>
	 * <li>If possible target isn't the L2Character targeted but is attakable, launch a simple attack against it</li><BR>
	 * <BR>
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target
	 * @param sAtk
	 * @return True if one hit isn't missed
	 */
	private boolean doAttackHitByPole(Attack attack, L2Character target, int sAtk)
	{
		double angleChar;
		final int maxRadius = getPhysicalAttackRange();
		final int maxAngleDiff = (int) getStat().calcStat(Stats.POWER_ATTACK_ANGLE, 120, null, null);
		
		if (Config.DEBUG)
		{
			_log.info("doAttackHitByPole: Max radius = " + maxRadius);
			_log.info("doAttackHitByPole: Max angle = " + maxAngleDiff);
		}
		
		// o1 x: 83420 y: 148158 (Giran)
		// o2 x: 83379 y: 148081 (Giran)
		// dx = -41
		// dy = -77
		// distance between o1 and o2 = 87.24
		// arctan2 = -120 (240) degree (excel arctan2(dx, dy); java arctan2(dy, dx))
		//
		// o2
		//
		// o1 ----- (heading)
		// In the diagram above:
		// o1 has a heading of 0/360 degree from horizontal (facing East)
		// Degree of o2 in respect to o1 = -120 (240) degree
		//
		// o2 / (heading)
		// /
		// o1
		// In the diagram above
		// o1 has a heading of -80 (280) degree from horizontal (facing north east)
		// Degree of o2 in respect to 01 = -40 (320) degree
		
		// Get char's heading degree
		angleChar = Util.convertHeadingToDegree(getHeading());
		final int attackcountmax = (int) getStat().calcStat(Stats.ATTACK_COUNT_MAX, 4, null, null) - 1;
		int attackcount = 0;
		
		if (angleChar <= 0)
		{
			angleChar += 360;
			// ===========================================================
		}
		
		boolean hitted = doAttackHitSimple(attack, target, 100, sAtk);
		double attackpercent = 85;
		L2Character temp;
		for (final L2Object obj : getKnownList().getKnownObjects().values())
		{
			if (obj == target)
			{
				continue; // do not hit twice
			}
			// Check if the L2Object is a L2Character
			if (obj instanceof L2Character)
			{
				if ((obj instanceof L2PetInstance) && (this instanceof L2PcInstance) && (((L2PetInstance) obj).getOwner() == ((L2PcInstance) this)))
				{
					continue;
				}
				
				if (!Util.checkIfInRange(maxRadius, this, obj, false))
				{
					continue;
				}
				
				// otherwise hit too high/low. 650 because mob z coord sometimes wrong on hills
				if (Math.abs(obj.getZ() - getZ()) > 650)
				{
					continue;
				}
				if (!isFacing(obj, maxAngleDiff))
				{
					continue;
				}
				
				temp = (L2Character) obj;
				
				// Launch a simple attack against the L2Character targeted
				if (!temp.isAlikeDead())
				{
					attackcount += 1;
					if (attackcount <= attackcountmax)
					{
						if ((temp == getAI().getAttackTarget()) || temp.isAutoAttackable(this))
						{
							
							hitted |= doAttackHitSimple(attack, temp, attackpercent, sAtk);
							attackpercent /= 1.15;
						}
					}
				}
			}
		}
		
		// Return true if one hit isn't missed
		return hitted;
	}
	
	/**
	 * Launch a simple attack.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Calculate if hit is missed or not</li>
	 * <li>If hit isn't missed, calculate if shield defense is efficient</li>
	 * <li>If hit isn't missed, calculate if hit is critical</li>
	 * <li>If hit isn't missed, calculate physical damages</li>
	 * <li>Create a new hit task with Medium priority</li>
	 * <li>Add this hit to the Server-Client packet Attack</li><BR>
	 * <BR>
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The L2Character targeted
	 * @param sAtk
	 * @return True if the hit isn't missed
	 */
	private boolean doAttackHitSimple(Attack attack, L2Character target, int sAtk)
	{
		return doAttackHitSimple(attack, target, 100, sAtk);
	}
	
	private boolean doAttackHitSimple(Attack attack, L2Character target, double attackpercent, int sAtk)
	{
		int damage1 = 0;
		boolean shld1 = false;
		boolean crit1 = false;
		
		// Calculate if hit is missed or not
		final boolean miss1 = Formulas.getInstance().calcHitMiss(this, target);
		
		// Check if hit isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient
			shld1 = Formulas.getInstance().calcShldUse(this, target);
			
			// Calculate if hit is critical
			crit1 = Formulas.getInstance().calcCrit(getStat().getCriticalHit(target, null));
			
			// Calculate physical damages
			damage1 = (int) Formulas.getInstance().calcPhysDam(this, target, null, shld1, crit1, false, attack._soulshot);
			
			if (attackpercent != 100)
			{
				damage1 = (int) ((damage1 * attackpercent) / 100);
			}
		}
		
		// Create a new hit task with Medium priority
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack._soulshot, shld1), sAtk);
		
		// Add this hit to the Server-Client packet Attack
		attack.addHit(target, damage1, miss1, crit1, shld1);
		
		// Return true if hit isn't missed
		return !miss1;
	}
	
	/**
	 * Manage the casting task (casting and interrupt time, re-use delay...) and display the casting bar and animation on client.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Verify the possibilty of the the cast : skill is a spell, caster isn't muted...</li>
	 * <li>Get the list of all targets (ex : area effects) and define the L2Charcater targeted (its stats will be used in calculation)</li>
	 * <li>Calculate the casting time (base + modifier of MAtkSpd), interrupt time and re-use delay</li>
	 * <li>Send a Server->Client packet MagicSkillUse (to diplay casting animation), a packet SetupGauge (to display casting bar) and a system message</li>
	 * <li>Disable all skills during the casting time (create a task EnableAllSkills)</li>
	 * <li>Disable the skill during the re-use delay (create a task EnableSkill)</li>
	 * <li>Create a task MagicUseTask (that will call method onMagicUseTimer) to launch the Magic Skill at the end of the casting time</li><BR>
	 * <BR>
	 * @param skill The L2Skill to use
	 */
	public void doCast(L2Skill skill)
	{
		if (skill == null)
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		// Check if skill is disabled
		if (isSkillDisabled(skill.getId()))
		{
			if (this instanceof L2PcInstance)
			{
				final SystemMessage sm = new SystemMessage(SystemMessage.S1_PREPARED_FOR_REUSE);
				sm.addSkillName(skill.getId(), skill.getLevel());
				sendPacket(sm);
			}
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			
			return;
		}
		
		// Check if the skill is a magic spell and if the L2Character is not muted
		if (skill.isMagic() && isMuted())
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		// Check if the skill is physical and if the L2Character is not physically muted
		if (!skill.isMagic() && isPhysicalMuted())
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		// Recharge AutoSoulShot
		if (skill.useSoulShot())
		{
			if (this instanceof L2PcInstance)
			{
				((L2PcInstance) this).rechargeAutoSoulShot(true, false, false);
			}
			else if (this instanceof L2Summon)
			{
				((L2Summon) this).getOwner().rechargeAutoSoulShot(true, false, true);
			}
			else if (this instanceof L2NpcInstance)
			{
				((L2NpcInstance) this).rechargeAutoSoulShot(true, false);
			}
		}
		else if (skill.useSpiritShot())
		{
			if (this instanceof L2PcInstance)
			{
				((L2PcInstance) this).rechargeAutoSoulShot(false, true, false);
			}
			else if (this instanceof L2Summon)
			{
				((L2Summon) this).getOwner().rechargeAutoSoulShot(false, true, true);
			}
		}
		
		// Get all possible targets of the skill in a table in function of the skill target type
		final L2Object[] targets = skill.getTargetList(this);
		
		if ((targets == null) || (targets.length == 0))
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		// Set the target of the skill in function of Skill Type and Target Type
		L2Character target = null;
		
		if ((skill.getSkillType() == SkillType.BUFF) || (skill.getSkillType() == SkillType.HEAL) || (skill.getSkillType() == SkillType.COMBATPOINTHEAL) || (skill.getSkillType() == SkillType.MANAHEAL) || (skill.getSkillType() == SkillType.REFLECT) || (skill.getSkillType() == SkillType.SEED) || (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF) || (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_PET) || (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_PARTY) || (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_CLAN) || (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_ALLY))
		{
			target = (L2Character) targets[0];
			
			if ((this instanceof L2PcInstance) && (target instanceof L2PcInstance) && (target.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK))
			{
				if ((skill.getSkillType() == SkillType.BUFF) || (skill.getSkillType() == SkillType.HOT) || (skill.getSkillType() == SkillType.HEAL) || (skill.getSkillType() == SkillType.HEAL_PERCENT) || (skill.getSkillType() == SkillType.MANAHEAL) || (skill.getSkillType() == SkillType.BALANCE_LIFE))
				{
					target.setLastBuffer(this);
				}
				
				if (((L2PcInstance) this).isInParty() && (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_PARTY))
				{
					for (final L2PcInstance member : ((L2PcInstance) this).getParty().getPartyMembers())
					{
						member.setLastBuffer(this);
					}
				}
			}
		}
		else
		{
			target = (L2Character) getTarget();
		}
		
		// AURA skills should always be using caster as target
		if ((skill.getTargetType() == SkillTargetType.TARGET_AURA) || (skill.getTargetType() == SkillTargetType.TARGET_AURA_UNDEAD))
		{
			target = this;
		}
		
		if (target == null)
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		setLastSkillCast(skill);
		
		// Get the Identifier of the skill
		final int magicId = skill.getId();
		
		// Get the Display Identifier for a skill that client can't display
		final int displayId = skill.getDisplayId();
		
		// Get the level of the skill
		int level = skill.getLevel();
		
		if (level < 1)
		{
			level = 1;
		}
		
		// Get the casting time of the skill (base)
		int hitTime = skill.getHitTime();
		int coolTime = skill.getCoolTime();
		
		// Calculate the casting time of the skill (base + modifier of MAtkSpd)
		hitTime = Formulas.getInstance().calcMAtkSpd(this, skill, hitTime);
		if (coolTime > 0)
		{
			coolTime = Formulas.getInstance().calcMAtkSpd(this, skill, coolTime);
		}
		
		// Calculate altered Cast Speed due to BSpS/SpS
		final L2ItemInstance weaponInst = getActiveWeaponInstance();
		
		if ((weaponInst != null) && skill.isMagic())
		{
			if ((weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT) || (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT))
			{
				// Only takes 70% of the time to cast a BSpS/SpS cast
				hitTime = (int) (0.70 * hitTime);
				coolTime = (int) (0.70 * coolTime);
			}
		}
		else if ((this instanceof L2NpcInstance) && skill.useSpiritShot())
		{
			if (((L2NpcInstance) this).rechargeAutoSoulShot(false, true))
			{
				hitTime = (int) (0.70 * hitTime);
				coolTime = (int) (0.70 * coolTime);
			}
		}
		
		// Set the _castEndTime and _castInterruptTime. +10 ticks for lag situations, will be reset in onMagicFinalizer
		_castEndTime = 10 + GameTimeController.getGameTicks() + ((coolTime + hitTime) / GameTimeController.MILLIS_IN_TICK);
		_castInterruptTime = -2 + GameTimeController.getGameTicks() + (hitTime / GameTimeController.MILLIS_IN_TICK);
		
		// Init the reuse time of the skill
		int reuseDelay;
		
		if (skill.isMagic())
		{
			reuseDelay = (int) (skill.getReuseDelay() * getStat().getMReuseRate(skill));
		}
		else
		{
			reuseDelay = (int) (skill.getReuseDelay() * getStat().getPReuseRate(skill));
		}
		
		reuseDelay *= 333.0 / (skill.isMagic() ? getMAtkSpd() : getPAtkSpd());
		
		final boolean skillMastery = Formulas.getInstance().calcSkillMastery(this, skill);
		
		// Skill reuse check
		if ((reuseDelay > 30000) && !skillMastery)
		{
			addTimeStamp(skill.getId(), reuseDelay);
		}
		
		// Check if this skill consume mp on start casting
		final int initmpcons = getStat().getMpInitialConsume(skill);
		if (initmpcons > 0)
		{
			getStatus().reduceMp(initmpcons);
			final StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
			sendPacket(su);
		}
		
		// Disable the skill during the re-use delay and create a task EnableSkill with Medium priority to enable it at the end of the re-use delay
		if (reuseDelay > 10)
		{
			if (skillMastery)
			{
				reuseDelay = 100;
			}
			disableSkill(skill.getId(), reuseDelay);
		}
		
		// Make sure that char is facing selected target
		if (target != this)
		{
			setHeading(Util.calculateHeadingFrom(this, target));
		}
		
		// Handle Blow chance here
		boolean success = false;
		if (skill.getSkillType() == SkillType.BLOW)
		{
			// initialize success
			success = true;
			
			// Blow success according to heading
			final int front = 50;
			final int side = 60;
			final int behind = 70;
			
			int _successChance = side;
			if (isBehindTarget())
			{
				_successChance = behind;
			}
			else if (isInFrontOfTarget())
			{
				_successChance = front;
			}
			
			// If skill requires Crit or skill requires behind,
			// calculate chance based on DEX, Position and on self BUFF
			if ((skill.getCondition() & L2Skill.COND_BEHIND) != 0)
			{
				success = (_successChance == behind);
			}
			if ((skill.getCondition() & L2Skill.COND_CRIT) != 0)
			{
				success = (success && Formulas.getInstance().calcBlow(this, target, _successChance));
			}
		}
		
		// Send a Server->Client packet MagicSkillUse with target, displayId, level, hitTime, reuseDelay
		// to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
		broadcastPacket(new MagicSkillUse(this, target, displayId, level, hitTime, reuseDelay, success));
		
		// Send a system message USE_S1 to the L2Character
		if ((this instanceof L2PcInstance) && (magicId != 1312))
		{
			final SystemMessage sm = new SystemMessage(SystemMessage.USE_S1);
			sm.addSkillName(magicId, skill.getLevel());
			sendPacket(sm);
		}
		
		// launch the magic in hitTime milliseconds
		if (hitTime > 210)
		{
			// Send a Server->Client packet SetupGauge with the color of the gauge and the casting time
			if (this instanceof L2PcInstance)
			{
				final SetupGauge sg = new SetupGauge(SetupGauge.BLUE, hitTime);
				sendPacket(sg);
			}
			
			// Disable all skills during the casting
			disableAllSkills();
			
			if (_skillCast != null)
			{
				try
				{
					
					_skillCast.cancel(true);
				}
				catch (final NullPointerException e)
				{
				}
				
				_skillCast = null;
			}
			
			// Create a task MagicUseTask to launch the MagicSkill at the end of the casting time (hitTime)
			// For client animation reasons (party buffs especially) 200 ms before!
			_skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, success, 1), hitTime - 200);
		}
		else
		{
			onMagicLaunchedTimer(targets, skill, coolTime, true, success);
		}
	}
	
	/**
	 * Index according to skill id the current timestamp of use.<br>
	 * <br>
	 * @param s
	 * @param r
	 */
	public void addTimeStamp(int s, int r)
	{
		/***/
	}
	
	/**
	 * Index according to skill id the current timestamp of use.<br>
	 * <br>
	 * @param s
	 */
	public void removeTimeStamp(int s)
	{
		/***/
	}
	
	/**
	 * Kill the L2Character.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Set target to null and cancel Attack or Cast</li>
	 * <li>Stop movement</li>
	 * <li>Stop HP/MP/CP Regeneration task</li>
	 * <li>Stop all active skills effects in progress on the L2Character</li>
	 * <li>Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform</li>
	 * <li>Notify L2Character AI</li><BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2NpcInstance : Create a DecayTask to remove the corpse of the L2NpcInstance after 7 seconds</li>
	 * <li>L2Attackable : Distribute rewards (EXP, SP, Drops...) and notify Quest Engine</li>
	 * <li>L2PcInstance : Apply Death Penalty, Manage gain/loss Karma and Item Drop</li><BR>
	 * <BR>
	 * @param killer The L2Character who killed it
	 * @return
	 */
	public boolean doDie(L2Character killer)
	{
		// killing is only possible one time
		synchronized (this)
		{
			if (isDead())
			{
				return false;
			}
			setCurrentHp(0);
			if (isFakeDeath())
			{
				stopEffects(L2Effect.EffectType.FAKE_DEATH);
			}
			setIsDead(true);
		}
		
		// Set target to null and cancel Attack or Cast
		setTarget(null);
		
		// Stop movement
		stopMove(null);
		
		// Stop HP/MP/CP Regeneration task
		getStatus().stopHpMpRegeneration();
		
		boolean keepEffects = false;
		
		// Stop all active skills effects in progress on the L2Character,
		// if the Character isn't a Noblesse Blessed L2PlayableInstance
		if ((this instanceof L2PlayableInstance) && ((L2PlayableInstance) this).isNoblesseBlessed())
		{
			keepEffects = true;
		}
		
		if ((this instanceof L2PcInstance) && (((L2PcInstance) this).getEventTeam() > 0))
		{
			keepEffects = false;
		}
		
		if (keepEffects)
		{
			((L2PlayableInstance) this).stopNoblesseBlessing(null);
			if (((L2PlayableInstance) this).getCharmOfLuck())
			{
				((L2PlayableInstance) this).stopCharmOfLuck(null);
			}
		}
		else
		{
			stopAllEffects();
		}
		
		calculateRewards(killer);
		
		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		broadcastStatusUpdate();
		
		// Notify L2Character AI
		getAI().notifyEvent(CtrlEvent.EVT_DEAD, null);
		
		// Notify Quest of character's death
		for (final QuestState qs : getNotifyQuestOfDeath())
		{
			if (qs == null)
			{
				continue;
			}
			
			qs.getQuest().notifyDeath((killer == null ? this : killer), this, qs);
		}
		
		getNotifyQuestOfDeath().clear();
		
		getAttackByList().clear();
		return true;
	}
	
	protected void calculateRewards(L2Character killer)
	{
	}
	
	/** Sets HP, MP and CP and revives the L2Character. */
	public void doRevive()
	{
		if (!isDead())
		{
			return;
		}
		
		if (!isTeleporting())
		{
			setIsPendingRevive(false);
			setIsDead(false);
			
			// Check if player is pc and is in Event
			if ((this instanceof L2PcInstance) && (((L2PcInstance) this).getEventTeam() > 0))
			{
				// Heal Player fully
				_Status.setCurrentCp(getMaxCp());
				_Status.setCurrentHp(getMaxHp());
				_Status.setCurrentMp(getMaxMp());
				
				final ChangeWaitType revive = new ChangeWaitType((this), ChangeWaitType.WT_STOP_FAKEDEATH);
				broadcastPacket(revive);
			}
			else
			{
				_Status.setCurrentHp(getMaxHp() * Config.RESPAWN_RESTORE_HP);
			}
			
			// Start broadcast status
			broadcastPacket(new Revive(this));
		}
		else
		{
			setIsPendingRevive(true);
		}
	}
	
	/**
	 * Revives the L2Character using skill.
	 * @param revivePower
	 */
	public void doRevive(double revivePower)
	{
		doRevive();
	}
	
	// =========================================================
	// Property - Public
	/**
	 * Return the L2CharacterAI of the L2Character and if its null create a new one.
	 * @return
	 */
	public L2CharacterAI getAI()
	{
		if (_ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
				{
					_ai = new L2CharacterAI(new AIAccessor());
				}
			}
		}
		
		return _ai;
	}
	
	public void setAI(L2CharacterAI newAI)
	{
		final L2CharacterAI oldAI = _ai;
		if ((oldAI != null) && (oldAI != newAI) && (oldAI instanceof L2AttackableAI))
		{
			((L2AttackableAI) oldAI).stopAITask();
		}
		_ai = newAI;
	}
	
	/**
	 * Return True if the L2Character has a L2CharacterAI.
	 * @return
	 */
	public boolean hasAI()
	{
		return _ai != null;
	}
	
	/**
	 * Return True if the L2Character is RaidBoss or his minion.
	 * @return
	 */
	public boolean isRaid()
	{
		return false;
	}
	
	/**
	 * Return a list of L2Character that attacked.
	 * @return
	 */
	public final List<L2Character> getAttackByList()
	{
		if (_AttackByList == null)
		{
			_AttackByList = new FastList<>();
		}
		return _AttackByList;
	}
	
	public final L2Skill getLastSkillCast()
	{
		return _lastSkillCast;
	}
	
	public void setLastSkillCast(L2Skill skill)
	{
		_lastSkillCast = skill;
	}
	
	public final boolean isAfraid()
	{
		return _IsAfraid;
	}
	
	public final void setIsAfraid(boolean value)
	{
		_IsAfraid = value;
	}
	
	/**
	 * Return True if the L2Character can't use its skills (ex : stun, sleep...).
	 * @return
	 */
	public final boolean isAllSkillsDisabled()
	{
		return _allSkillsDisabled || isStunned() || isSleeping() || isParalyzed();
	}
	
	/**
	 * Return True if the L2Character can't attack (stun, sleep, attackEndTime, fakeDeath, paralyze).
	 * @return
	 */
	public final boolean isAttackingDisabled()
	{
		return isStunned() || isSleeping() || (_attackEndTime > GameTimeController.getGameTicks()) || isAlikeDead() || isParalyzed() || isCoreAIDisabled();
	}
	
	public final Calculator[] getCalculators()
	{
		return _Calculators;
	}
	
	public final boolean isConfused()
	{
		return _IsConfused;
	}
	
	public final void setIsConfused(boolean value)
	{
		_IsConfused = value;
	}
	
	/**
	 * Return True if the L2Character is dead or use fake death.
	 * @return
	 */
	public final boolean isAlikeDead()
	{
		return isFakeDeath() || _IsDead;
	}
	
	/**
	 * Return True if the L2Character is dead.
	 * @return
	 */
	public final boolean isDead()
	{
		return _IsDead;
	}
	
	public final void setIsDead(boolean value)
	{
		_IsDead = value;
	}
	
	public final boolean isFakeDeath()
	{
		return _IsFakeDeath;
	}
	
	public final void setIsFakeDeath(boolean value)
	{
		_IsFakeDeath = value;
	}
	
	/**
	 * Return True if the L2Character is flying.
	 * @return
	 */
	public final boolean isFlying()
	{
		return _IsFlying;
	}
	
	/**
	 * Set the L2Character flying mode to True.
	 * @param mode
	 */
	public final void setIsFlying(boolean mode)
	{
		_IsFlying = mode;
	}
	
	public boolean isImmobilized()
	{
		return _immobileLevel > 0;
	}
	
	public void setIsImmobilized(boolean value)
	{
		if (value)
		{
			_immobileLevel++;
		}
		else if (_immobileLevel > 0)
		{
			_immobileLevel--;
		}
	}
	
	public final boolean isMuted()
	{
		return _IsMuted;
	}
	
	public final void setIsMuted(boolean value)
	{
		_IsMuted = value;
	}
	
	public final boolean isPhysicalMuted()
	{
		return _IsPhysicalMuted;
	}
	
	public final void setIsPhysicalMuted(boolean value)
	{
		_IsPhysicalMuted = value;
	}
	
	/**
	 * Return True if the L2Character can't move (stun, root, sleep, overload, paralyzed).
	 * @return
	 */
	public boolean isMovementDisabled()
	{
		return isStunned() || isRooted() || isSleeping() || isOverloaded() || isParalyzed() || isImmobilized() || isAlikeDead() || isTeleporting();
	}
	
	/**
	 * Return True if the L2Character can be controlled by the player (confused, afraid).
	 * @return
	 */
	public final boolean isOutOfControl()
	{
		return isConfused() || isAfraid();
	}
	
	public final boolean isOverloaded()
	{
		return _IsOverloaded;
	}
	
	/**
	 * Set the overloaded status of the L2Character is overloaded (if True, the L2PcInstance can't take more item).
	 * @param value
	 */
	public final void setIsOverloaded(boolean value)
	{
		_IsOverloaded = value;
	}
	
	public final boolean isParalyzed()
	{
		return _IsParalyzed;
	}
	
	public final void setIsParalyzed(boolean value)
	{
		_IsParalyzed = value;
	}
	
	public final boolean isPendingRevive()
	{
		return _IsPendingRevive;
	}
	
	public final void setIsPendingRevive(boolean value)
	{
		_IsPendingRevive = value;
	}
	
	/**
	 * Return the L2Summon of the L2Character.<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance</li><BR>
	 * <BR>
	 * @return
	 */
	public L2Summon getPet()
	{
		return null;
	}
	
	/**
	 * Return True if the L2Character is ridding.
	 * @return
	 */
	public final boolean isRiding()
	{
		return _IsRiding;
	}
	
	/**
	 * Set the L2Character riding mode to True.
	 * @param mode
	 */
	public final void setIsRiding(boolean mode)
	{
		_IsRiding = mode;
	}
	
	public final boolean isRooted()
	{
		return _IsRooted;
	}
	
	public final void setIsRooted(boolean value)
	{
		_IsRooted = value;
	}
	
	/**
	 * Return True if the L2Character is running.
	 * @return
	 */
	public final boolean isRunning()
	{
		return _IsRunning;
	}
	
	public final void setIsRunning(boolean value)
	{
		_IsRunning = value;
		if (getRunSpeed() != 0)
		{
			broadcastPacket(new ChangeMoveType(this));
		}
		if (this instanceof L2PcInstance)
		{
			((L2PcInstance) this).broadcastUserInfo();
		}
		else if (this instanceof L2Summon)
		{
			((L2Summon) this).broadcastStatusUpdate();
		}
		else if (this instanceof L2NpcInstance)
		{
			for (final L2PcInstance player : getKnownList().getKnownPlayers().values())
			{
				if (getRunSpeed() == 0)
				{
					player.sendPacket(new ServerObjectInfo((L2NpcInstance) this, player));
				}
				else
				{
					player.sendPacket(new NpcInfo((L2NpcInstance) this, player));
				}
			}
		}
	}
	
	/** Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance. */
	public final void setRunning()
	{
		if (!isRunning())
		{
			setIsRunning(true);
		}
	}
	
	public final boolean isSleeping()
	{
		return _IsSleeping;
	}
	
	public final void setIsSleeping(boolean value)
	{
		_IsSleeping = value;
	}
	
	public final boolean isStunned()
	{
		return _IsStunned;
	}
	
	public final void setIsStunned(boolean value)
	{
		_IsStunned = value;
	}
	
	public final boolean isTeleporting()
	{
		return _IsTeleporting;
	}
	
	public final void setIsTeleporting(boolean value)
	{
		_IsTeleporting = value;
	}
	
	public void setIsInvul(boolean value)
	{
		_IsInvul = value;
	}
	
	public boolean isInvul()
	{
		return _IsInvul || _IsTeleporting;
	}
	
	public boolean isSingleSpear()
	{
		return _IsSingleSpear;
	}
	
	public void setIsSingleSpear(boolean value)
	{
		_IsSingleSpear = value;
	}
	
	public boolean isSummoned()
	{
		return _isSummoned;
	}
	
	public void setIsSummoned(boolean value)
	{
		_isSummoned = value;
	}
	
	public boolean isUndead()
	{
		return _Template.isUndead;
	}
	
	@Override
	public CharKnownList getKnownList()
	{
		if ((super.getKnownList() == null) || !(super.getKnownList() instanceof CharKnownList))
		{
			setKnownList(new CharKnownList(this));
		}
		return ((CharKnownList) super.getKnownList());
	}
	
	public CharStat getStat()
	{
		if (_Stat == null)
		{
			_Stat = new CharStat(this);
		}
		return _Stat;
	}
	
	public final void setStat(CharStat value)
	{
		_Stat = value;
	}
	
	public CharStatus getStatus()
	{
		if (_Status == null)
		{
			_Status = new CharStatus(this);
		}
		return _Status;
	}
	
	public final void setStatus(CharStatus value)
	{
		_Status = value;
	}
	
	public L2CharTemplate getTemplate()
	{
		return _Template;
	}
	
	/**
	 * Set the template of the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * Each L2Character owns generic and static properties (ex : all Keltir have the same number of HP...). All of those properties are stored in a different template for each type of L2Character. Each template is loaded once in the server cache memory (reduce memory use). When a new instance of
	 * L2Character is spawned, server just create a link between the instance and the template This link is stored in <B>_template</B><BR>
	 * <BR>
	 * <B><U> Assert </U> :</B><BR>
	 * <BR>
	 * <li>this instanceof L2Character</li><BR>
	 * <BR
	 * @param template
	 */
	protected final void setTemplate(L2CharTemplate template)
	{
		_Template = template;
	}
	
	/**
	 * Return the Title of the L2Character.
	 * @return
	 */
	public final String getTitle()
	{
		return _Title;
	}
	
	/**
	 * Set the Title of the L2Character.
	 * @param value
	 */
	public final void setTitle(String value)
	{
		_Title = value;
	}
	
	/** Set the L2Character movement type to walk and send Server->Client packet ChangeMoveType to all others L2PcInstance. */
	public final void setWalking()
	{
		if (isRunning())
		{
			setIsRunning(false);
		}
	}
	
	public int getAuraColor()
	{
		return _auraColor;
	}
	
	public void setAuraColor(int color)
	{
		_auraColor = color;
	}
	
	/** Task lauching the function enableSkill() */
	class EnableSkill implements Runnable
	{
		int _skillId;
		
		public EnableSkill(int skillId)
		{
			_skillId = skillId;
		}
		
		@Override
		public void run()
		{
			try
			{
				enableSkill(_skillId);
			}
			catch (final Throwable e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}
	
	/**
	 * Task lauching the function onHitTimer().<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL and send a Server->Client packet ActionFailed (if attacker is a L2PcInstance)</li>
	 * <li>If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are L2PcInstance</li>
	 * <li>If attack isn't aborted and hit isn't missed, reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary</li>
	 * <li>if attack isn't aborted and hit isn't missed, manage attack or cast break of the target (calculating rate, sending message...)</li><BR>
	 * <BR>
	 */
	class HitTask implements Runnable
	{
		L2Character _hitTarget;
		int _damage;
		boolean _crit;
		boolean _miss;
		boolean _shld;
		boolean _soulshot;
		
		public HitTask(L2Character target, int damage, boolean crit, boolean miss, boolean soulshot, boolean shld)
		{
			_hitTarget = target;
			_damage = damage;
			_crit = crit;
			_shld = shld;
			_miss = miss;
			_soulshot = soulshot;
		}
		
		@Override
		public void run()
		{
			try
			{
				onHitTimer(_hitTarget, _damage, _crit, _miss, _soulshot, _shld);
			}
			catch (final Throwable e)
			{
				_log.severe(e.toString());
			}
		}
	}
	
	/** Task launching the magic skill phases */
	class MagicUseTask implements Runnable
	{
		L2Object[] _targets;
		L2Skill _skill;
		int _coolTime;
		int _phase;
		boolean _crit;
		
		public MagicUseTask(L2Object[] targets, L2Skill skill, int coolTime, boolean crit, int phase)
		{
			_targets = targets;
			_skill = skill;
			_coolTime = coolTime;
			_phase = phase;
			_crit = crit;
		}
		
		@Override
		public void run()
		{
			try
			{
				switch (_phase)
				{
					case 1:
						onMagicLaunchedTimer(_targets, _skill, _coolTime, false, _crit);
						break;
					case 2:
						onMagicHitTimer(_targets, _skill, _coolTime, false, _crit);
						break;
					case 3:
						onMagicFinalizer(_targets, _skill);
						break;
					default:
						break;
				}
			}
			catch (final Throwable e)
			{
				_log.log(Level.SEVERE, "", e);
				enableAllSkills();
			}
		}
	}
	
	/** Task lauching the function useMagic() */
	class QueuedMagicUseTask implements Runnable
	{
		L2PcInstance _currPlayer;
		L2Skill _queuedSkill;
		boolean _isCtrlPressed;
		boolean _isShiftPressed;
		
		public QueuedMagicUseTask(L2PcInstance currPlayer, L2Skill queuedSkill, boolean isCtrlPressed, boolean isShiftPressed)
		{
			_currPlayer = currPlayer;
			_queuedSkill = queuedSkill;
			_isCtrlPressed = isCtrlPressed;
			_isShiftPressed = isShiftPressed;
		}
		
		@Override
		public void run()
		{
			try
			{
				_currPlayer.useMagic(_queuedSkill, _isCtrlPressed, _isShiftPressed);
				
			}
			catch (final Throwable e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}
	
	/** Task of AI notification */
	public class NotifyAITask implements Runnable
	{
		private final CtrlEvent _evt;
		
		NotifyAITask(CtrlEvent evt)
		{
			_evt = evt;
		}
		
		@Override
		public void run()
		{
			try
			{
				getAI().notifyEvent(_evt, null);
			}
			catch (final Throwable t)
			{
				_log.log(Level.WARNING, "", t);
			}
		}
	}
	
	/** Task lauching the function stopPvPFlag() */
	class PvPFlag implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				if (System.currentTimeMillis() > getPvpFlagLasts())
				{
					// _log.fine("Stopping PvP");
					stopPvPFlag();
				}
				else if (System.currentTimeMillis() > (getPvpFlagLasts() - 5000))
				{
					updatePvPFlag(2);
				}
				else
				{
					updatePvPFlag(1);
					// Start a new PvP timer check
					// checkPvPFlag();
				}
			}
			catch (final Exception e)
			{
				_log.log(Level.WARNING, "error in pvp flag task:", e);
			}
		}
	}
	
	// =========================================================
	// =========================================================
	
	// Abnormal Effect - NEED TO REMOVE ONCE L2CHARABNORMALEFFECT IS COMPLETE
	// Data Field
	/** Map 16 bits (0x0000) containing all abnormal effect in progress */
	private short _AbnormalEffects;
	
	/**
	 * FastTable containing all active skills effects in progress of a L2Character.
	 */
	private FastTable<L2Effect> _effects;
	
	/** The table containing the List of all stacked effect in progress for each Stack group Identifier */
	protected Map<String, List<L2Effect>> _stackedEffects;
	
	/** Table EMPTY_EFFECTS shared by all L2Character without effects in progress */
	private static final L2Effect[] EMPTY_EFFECTS = new L2Effect[0];
	
	public static final int ABNORMAL_EFFECT_BLEEDING = 0x0001; // not sure
	public static final int ABNORMAL_EFFECT_POISON = 0x0002;
	public static final int ABNORMAL_EFFECT_UNKNOWN_3 = 0x0004;
	public static final int ABNORMAL_EFFECT_UNKNOWN_4 = 0x0008;
	public static final int ABNORMAL_EFFECT_UNKNOWN_5 = 0x0010;
	public static final int ABNORMAL_EFFECT_UNKNOWN_6 = 0x0020;
	public static final int ABNORMAL_EFFECT_STUN = 0x0040;
	public static final int ABNORMAL_EFFECT_SLEEP = 0x0080;
	public static final int ABNORMAL_EFFECT_MUTED = 0x0100; // not sure
	public static final int ABNORMAL_EFFECT_ROOT = 0x0200;
	public static final int ABNORMAL_EFFECT_HOLD_1 = 0x0400;
	public static final int ABNORMAL_EFFECT_HOLD_2 = 0x0800;
	public static final int ABNORMAL_EFFECT_UNKNOWN_13 = 0x1000;
	public static final int ABNORMAL_EFFECT_BIG_HEAD = 0x2000;
	public static final int ABNORMAL_EFFECT_FLAME = 0x4000;
	public static final int ABNORMAL_EFFECT_UNKNOWN_16 = 0x8000;
	// FIXME : correct the value of the abnormal effect confused
	public static final int ABNORMAL_EFFECT_CONFUSED = 0x0020;
	// FIXME : correct the value of the abnormal effect afraid
	public static final int ABNORMAL_EFFECT_AFRAID = 0x0010;
	
	// Method - Public
	/**
	 * Launch and add L2Effect (including Stack Group management) to L2Character and update client magic icon.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>. The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR>
	 * <BR>
	 * Several same effect can't be used on a L2Character at the same time. Indeed, effects are not stackable and the last cast will replace the previous in progress. More, some effects belong to the same Stack Group (ex WindWald and Haste Potion). If 2 effects of a same group are used at the same
	 * time on a L2Character, only the more efficient (identified by its priority order) will be preserve.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Add the L2Effect to the L2Character _effects</li>
	 * <li>If this effect doesn't belong to a Stack Group, add its Funcs to the Calculator set of the L2Character (remove the old one if necessary)</li>
	 * <li>If this effect has higher priority in its Stack Group, add its Funcs to the Calculator set of the L2Character (remove previous stacked effect Funcs if necessary)</li>
	 * <li>If this effect has NOT higher priority in its Stack Group, set the effect to Not In Use</li>
	 * <li>Update active skills in progress icons on player client</li><BR>
	 * @param newEffect
	 */
	public final void addEffect(L2Effect newEffect)
	{
		if (newEffect == null)
		{
			return;
		}
		
		synchronized (this)
		{
			if (_effects == null)
			{
				_effects = new FastTable<>();
			}
			
			if (_stackedEffects == null)
			{
				_stackedEffects = new FastMap<>();
			}
		}
		
		synchronized (_effects)
		{
			L2Effect tempEffect, tempEffect2;
			
			// Make sure there's no same effect previously
			for (final L2Effect e : _effects)
			{
				if (e == null)
				{
					continue;
				}
				
				if ((e.getEffectType() == newEffect.getEffectType()) && (e.getStackOrder() <= newEffect.getStackOrder()) && ((e.getSkill().getId() == newEffect.getSkill().getId()) || (!e.getStackType().equals("none") && e.getStackType().equals(newEffect.getStackType()))))
				{
					if (newEffect.getSkill().isOffensive())
					{
						// Started scheduled timer needs to be cancelled.
						newEffect.stopEffectTask();
						return;
					}
					// renew buffs, exit old
					e.exit();
				}
			}
			
			// Remove first Buff if buff count is max buff amount
			final L2Skill tempskill = newEffect.getSkill();
			if ((getBuffCount() >= Config.BUFFS_MAX_AMOUNT) && !doesStack(tempskill) && newEffect.getShowIcon() && (((tempskill.getSkillType() == L2Skill.SkillType.BUFF) || tempskill.isOffensive() || (tempskill.getSkillType() == L2Skill.SkillType.REFLECT) || (tempskill.getSkillType() == L2Skill.SkillType.HEAL_PERCENT)) && !((tempskill.getId() > 4363) && (tempskill.getId() < 4367))))
			{
				removeFirstBuff(tempskill.getId());
			}
			
			// Add the L2Effect to all effect in progress on the L2Character
			if (!newEffect.getSkill().isOffensive() && !newEffect.getSkill().isToggle())
			{
				int pos = 0;
				for (final L2Effect e : _effects)
				{
					if (e != null)
					{
						final int skillid = e.getSkill().getId();
						if (!e.getSkill().isToggle() && !e.getSkill().isOffensive() && (!((skillid > 4363) && (skillid < 4367))))
						{
							pos++;
						}
					}
					else
					{
						break;
					}
				}
				_effects.add(pos, newEffect);
			}
			else
			{
				_effects.addLast(newEffect);
			}
			
			// Check if a stack group is defined for this effect
			if (newEffect.getStackType().equals("none"))
			{
				// Set this L2Effect to In Use
				newEffect.setInUse(true);
				
				// Add Funcs of this effect to the Calculator set of the L2Character
				addStatFuncs(newEffect.getStatFuncs());
				
				// Update active skills in progress icons on player client
				updateEffectIcons();
				return;
			}
			
			// Get the list of all stacked effects corresponding to the stack type of the L2Effect to add
			List<L2Effect> stackQueue = _stackedEffects.get(newEffect.getStackType());
			
			if (stackQueue == null)
			{
				stackQueue = new FastList<>();
			}
			
			tempEffect = null;
			if (stackQueue.size() > 0)
			{
				// Get the first stacked effect of the Stack group selecte
				for (final L2Effect e : _effects)
				{
					if ((e != null) && (e == stackQueue.get(0)))
					{
						tempEffect = e;
						break;
					}
				}
			}
			
			// Add the new effect to the stack group selected at its position
			stackQueue = effectQueueInsert(newEffect, stackQueue);
			
			if (stackQueue == null)
			{
				return;
			}
			
			// Update the Stack Group table _stackedEffects of the L2Character
			_stackedEffects.put(newEffect.getStackType(), stackQueue);
			
			// Get the first stacked effect of the Stack group selected
			tempEffect2 = null;
			for (final L2Effect e : _effects)
			{
				if ((e != null) && (e == stackQueue.get(0)))
				{
					tempEffect2 = e;
					break;
				}
			}
			
			if (tempEffect != tempEffect2)
			{
				if (tempEffect != null)
				{
					// Remove all Func objects corresponding to this stacked effect from the Calculator set of the L2Character
					removeStatsOwner(tempEffect);
					
					// Set the L2Effect to Not In Use
					tempEffect.setInUse(false);
				}
				
				if (tempEffect2 != null)
				{
					// Set this L2Effect to In Use
					tempEffect2.setInUse(true);
					
					// Add all Func objects corresponding to this stacked effect to the Calculator set of the L2Character
					addStatFuncs(tempEffect2.getStatFuncs());
				}
			}
		}
		
		// Update active skills in progress (In Use and Not In Use because stacked) icons on client
		updateEffectIcons();
	}
	
	/**
	 * Insert an effect at the specified position in a Stack Group.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * Several same effect can't be used on a L2Character at the same time. Indeed, effects are not stackable and the last cast will replace the previous in progress. More, some effects belong to the same Stack Group (ex WindWald and Haste Potion). If 2 effects of a same group are used at the same
	 * time on a L2Character, only the more efficient (identified by its priority order) will be preserve.<BR>
	 * <BR>
	 * @param newStackedEffect
	 * @param stackQueue The Stack Group in wich the effect must be added
	 * @return
	 */
	private List<L2Effect> effectQueueInsert(L2Effect newStackedEffect, List<L2Effect> stackQueue)
	{
		// Get the L2Effect corresponding to the Effect Identifier from the L2Character _effects
		if (_effects == null)
		{
			return null;
		}
		
		// Create an Iterator to go through the list of stacked effects in progress on the L2Character
		final Iterator<L2Effect> queueIterator = stackQueue.iterator();
		
		int i = 0;
		while (queueIterator.hasNext())
		{
			final L2Effect cur = queueIterator.next();
			if (newStackedEffect.getStackOrder() < cur.getStackOrder())
			{
				i++;
			}
			else
			{
				break;
			}
		}
		
		// Add the new effect to the Stack list in function of its position in the Stack group
		stackQueue.add(i, newStackedEffect);
		
		// skill.exit() could be used, if the users don't wish to see "effect
		// removed" always when a timer goes off, even if the buff isn't active
		// any more (has been replaced). but then check e.g. npc hold and raid petrify.
		if (Config.EFFECT_CANCELING && (stackQueue.size() > 1))
		{
			// only keep the current effect, cancel other effects
			for (final L2Effect e : _effects)
			{
				if ((e != null) && (e == stackQueue.get(1)))
				{
					_effects.remove(e);
					break;
				}
			}
			stackQueue.remove(1);
		}
		return stackQueue;
	}
	
	/**
	 * Stop and remove L2Effect (including Stack Group management) from L2Character and update client magic icon.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>. The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR>
	 * <BR>
	 * Several same effect can't be used on a L2Character at the same time. Indeed, effects are not stackable and the last cast will replace the previous in progress. More, some effects belong to the same Stack Group (ex WindWald and Haste Potion). If 2 effects of a same group are used at the same
	 * time on a L2Character, only the more efficient (identified by its priority order) will be preserve.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Remove Func added by this effect from the L2Character Calculator (Stop L2Effect)</li>
	 * <li>If the L2Effect belongs to a not empty Stack Group, replace theses Funcs by next stacked effect Funcs</li>
	 * <li>Remove the L2Effect from _effects of the L2Character</li>
	 * <li>Update active skills in progress icons on player client</li><BR>
	 * @param effect
	 */
	public final void removeEffect(L2Effect effect)
	{
		if ((effect == null) || (_effects == null))
		{
			return;
		}
		
		synchronized (_effects)
		{
			if (effect.getStackType().equals("none"))
			{
				// Remove Func added by this effect from the L2Character Calculator
				removeStatsOwner(effect);
			}
			else
			{
				if (_stackedEffects == null)
				{
					return;
				}
				
				// Get the list of all stacked effects corresponding to the stack type of the L2Effect to add
				final List<L2Effect> stackQueue = _stackedEffects.get(effect.getStackType());
				
				if ((stackQueue == null) || (stackQueue.size() < 1))
				{
					return;
				}
				
				// Get the Identifier of the first stacked effect of the Stack group selected
				final L2Effect frontEffect = stackQueue.get(0);
				
				// Remove the effect from the Stack Group
				final boolean removed = stackQueue.remove(effect);
				
				if (removed)
				{
					// Check if the first stacked effect was the effect to remove
					if (frontEffect == effect)
					{
						// Remove all its Func objects from the L2Character calculator set
						removeStatsOwner(effect);
						
						// Check if there's another effect in the Stack Group
						if (stackQueue.size() > 0)
						{
							// Add its list of Funcs to the Calculator set of the L2Character
							for (final L2Effect e : _effects)
							{
								if ((e != null) && (e == stackQueue.get(0)))
								{
									// Add its list of Funcs to the Calculator set of the L2Character
									addStatFuncs(e.getStatFuncs());
									// Set the effect to In Use
									e.setInUse(true);
									break;
								}
							}
						}
					}
					
					if (stackQueue.isEmpty())
					{
						_stackedEffects.remove(effect.getStackType());
					}
					else
					{
						// Update the Stack Group table _stackedEffects of the L2Character
						_stackedEffects.put(effect.getStackType(), stackQueue);
					}
				}
				
			}
			
			// Remove the active skill L2effect from _effects of the L2Character
			// The Integer key of _effects is the L2Skill Identifier that has created the effect
			for (final L2Effect e : _effects)
			{
				if ((e != null) && (e == effect))
				{
					_effects.remove(e);
					break;
				}
			}
		}
		
		// Update active skills in progress (In Use and Not In Use because stacked) icons on client
		updateEffectIcons();
	}
	
	/**
	 * Active abnormal effects flags in the binary mask and send Server->Client UserInfo/CharInfo packet.<BR>
	 * <BR>
	 * @param mask
	 */
	public final void startAbnormalEffect(short mask)
	{
		_AbnormalEffects |= mask;
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Confused flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR>
	 * <BR>
	 */
	public final void startConfused()
	{
		setIsConfused(true);
		getAI().notifyEvent(CtrlEvent.EVT_CONFUSED);
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Fake Death flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR>
	 * <BR>
	 */
	public final void startFakeDeath()
	{
		setIsFakeDeath(true);
		/* Aborts any attacks/casts if fake dead */
		abortAttack();
		abortCast();
		getAI().notifyEvent(CtrlEvent.EVT_FAKE_DEATH, null);
		broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_START_FAKEDEATH));
	}
	
	/**
	 * Active the abnormal effect Fear flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR>
	 * <BR>
	 */
	public final void startFear()
	{
		setIsAfraid(true);
		getAI().notifyEvent(CtrlEvent.EVT_AFRAID);
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Muted flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR>
	 * <BR>
	 */
	public final void startMuted()
	{
		setIsMuted(true);
		/* Aborts any casts if muted */
		abortCast();
		getAI().notifyEvent(CtrlEvent.EVT_MUTED);
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Physical_Muted flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR>
	 * <BR>
	 */
	public final void startPhysicalMuted()
	{
		setIsPhysicalMuted(true);
		abortAttack();
		getAI().notifyEvent(CtrlEvent.EVT_MUTED);
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Root flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR>
	 * <BR>
	 */
	public final void startRooted()
	{
		setIsRooted(true);
		getAI().notifyEvent(CtrlEvent.EVT_ROOTED, null);
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Sleep flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR>
	 * <BR>
	 */
	public final void startSleeping()
	{
		setIsSleeping(true);
		/* Aborts any attacks/casts if sleeped */
		abortAttack();
		abortCast();
		getAI().notifyEvent(CtrlEvent.EVT_SLEEPING, null);
		updateAbnormalEffect();
	}
	
	/**
	 * Launch a Stun Abnormal Effect on the L2Character.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Calculate the success rate of the Stun Abnormal Effect on this L2Character</li>
	 * <li>If Stun succeed, active the abnormal effect Stun flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet</li>
	 * <li>If Stun NOT succeed, send a system message Failed to the L2PcInstance attacker</li><BR>
	 * <BR>
	 */
	public final void startStunning()
	{
		setIsStunned(true);
		/* Aborts any attacks/casts if stunned */
		abortAttack();
		abortCast();
		getAI().notifyEvent(CtrlEvent.EVT_STUNNED, null);
		updateAbnormalEffect();
	}
	
	/**
	 * Launch a Paralyze Abnormal Effect on the L2Character.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Calculate the success rate of the Paralyze Abnormal Effect on this L2Character</li>
	 * <li>If Paralyze succeed, active the abnormal effect Paralyze flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet</li>
	 * <li>If Paralyze NOT succeed, send a system message Failed to the L2PcInstance attacker</li><BR>
	 * <BR>
	 */
	public final void startParalyze()
	{
		setIsParalyzed(true);
		/* Aborts any attacks/casts if paralyzed */
		abortAttack();
		abortCast();
		getAI().notifyEvent(CtrlEvent.EVT_PARALYZED, null);
		updateAbnormalEffect();
	}
	
	/**
	 * Modify the abnormal effect map according to the mask.<BR>
	 * <BR>
	 * @param mask
	 */
	public final void stopAbnormalEffect(short mask)
	{
		_AbnormalEffects &= ~mask;
		updateAbnormalEffect();
	}
	
	/**
	 * Stop all active skills effects in progress on the L2Character.<BR>
	 * <BR>
	 */
	public final void stopAllEffects()
	{
		// Get all active skills effects in progress on the L2Character
		final L2Effect[] effects = getAllEffects();
		if (effects == null)
		{
			return;
		}
		
		// Go through all active skills effects
		for (final L2Effect e : effects)
		{
			if (e != null)
			{
				e.exit(true);
			}
		}
		
		if (this instanceof L2PcInstance)
		{
			((L2PcInstance) this).updateAndBroadcastStatus(2);
		}
		
		if (this instanceof L2Summon)
		{
			((L2Summon) this).updateAndBroadcastStatus(1);
		}
	}
	
	/**
	 * Stop a specified/all Confused abnormal L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Delete a specified/all (if effect=null) Confused abnormal L2Effect from L2Character and update client magic icon</li>
	 * <li>Set the abnormal effect flag _confused to False</li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR>
	 * <BR>
	 * @param effect
	 */
	public final void stopConfused(L2Effect effect)
	{
		if (effect == null)
		{
			stopEffects(L2Effect.EffectType.CONFUSION);
		}
		else
		{
			removeEffect(effect);
		}
		
		setIsConfused(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop and remove the L2Effects corresponding to the L2Skill Identifier and update client magic icon.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>. The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR>
	 * <BR>
	 * @param skillId The L2Skill Identifier of the L2Effect to remove from _effects
	 */
	public final void stopSkillEffects(int skillId)
	{
		// Get all skills effects on the L2Character
		final L2Effect[] effects = getAllEffects();
		if (effects == null)
		{
			return;
		}
		
		for (final L2Effect e : effects)
		{
			if (e.getSkill().getId() == skillId)
			{
				e.exit();
			}
			
		}
	}
	
	/**
	 * Stop and remove the L2Effects corresponding to the L2Skill Identifier and update client magic icon.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>. The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR>
	 * <BR>
	 * @param type
	 * @param negatePower
	 * @param max
	 */
	public final void negateEffects(SkillType type, float negatePower, int max)
	{
		// Get all skills effects on the L2Character
		final L2Effect[] effects = getAllEffects();
		if (effects == null)
		{
			return;
		}
		
		// Get the maximum effects that can be cancelled
		if ((max == 0) || (max > effects.length))
		{
			max = effects.length;
		}
		
		for (int i = 0; i < max; i++)
		{
			final L2Effect e = effects[i];
			if (e == null)
			{
				continue;
			}
			
			switch (e.getSkill().getId())
			{
				case 1323:
				case 1325:
				case 4082:
				case 4215:
				case 4515:
					continue;
			}
			
			SkillType effectType = e.getSkill().getSkillType();
			
			// For additional effects on PDAM and MDAM skills (like STUN, SHOCK, PARALYZE...)
			if ((effectType == SkillType.PDAM) || (effectType == SkillType.MDAM))
			{
				effectType = e.getSkill().getEffectType();
			}
			
			if ((type == null) || ((effectType != null) && (effectType == type)))
			{
				if (negatePower > 0)
				{
					if ((e.getSkill().getMagicLevel() / 10) <= negatePower)
					{
						e.exit();
					}
				}
				else
				{
					e.exit();
				}
			}
		}
	}
	
	/**
	 * Stop and remove the L2Effect corresponding to the L2Skill Identifier and update client magic icon.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>. The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR>
	 * <BR>
	 * @param effectId The L2Skill Identifier of the L2Effect to remove from _effects
	 */
	public final void stopEffect(int effectId)
	{
		final L2Effect effect = getFirstEffect(effectId);
		if (effect != null)
		{
			effect.exit();
		}
	}
	
	/**
	 * Stop and remove all L2Effect of the selected type (ex : BUFF, DMG_OVER_TIME...) from the L2Character and update client magic icon.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>. The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Remove Func added by this effect from the L2Character Calculator (Stop L2Effect)</li>
	 * <li>Remove the L2Effect from _effects of the L2Character</li>
	 * <li>Update active skills in progress icons on player client</li><BR>
	 * <BR>
	 * @param type The type of effect to stop ((ex : BUFF, DMG_OVER_TIME...)
	 */
	public final void stopEffects(L2Effect.EffectType type)
	{
		// Get all active skills effects in progress on the L2Character
		final L2Effect[] effects = getAllEffects();
		
		if (effects == null)
		{
			return;
		}
		
		// Go through all active skills effects
		for (final L2Effect e : effects)
		{
			// Stop active skills effects of the selected type
			if (e.getEffectType() == type)
			{
				e.exit();
			}
		}
	}
	
	/**
	 * Stop a specified/all Fake Death abnormal L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Delete a specified/all (if effect=null) Fake Death abnormal L2Effect from L2Character and update client magic icon</li>
	 * <li>Set the abnormal effect flag _fake_death to False</li>
	 * <li>Notify the L2Character AI</li><BR>
	 * <BR>
	 * @param effect
	 */
	public final void stopFakeDeath(L2Effect effect)
	{
		if (effect == null)
		{
			stopEffects(L2Effect.EffectType.FAKE_DEATH);
		}
		else
		{
			removeEffect(effect);
		}
		
		setIsFakeDeath(false);
		// if this is a player instance, start the grace period for this character (grace from mobs only)!
		if (this instanceof L2PcInstance)
		{
			((L2PcInstance) this).setRecentFakeDeath(true);
		}
		
		final ChangeWaitType revive = new ChangeWaitType(this, ChangeWaitType.WT_STOP_FAKEDEATH);
		broadcastPacket(revive);
		broadcastPacket(new Revive(this));
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
	}
	
	/**
	 * Stop a specified/all Fear abnormal L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Delete a specified/all (if effect=null) Fear abnormal L2Effect from L2Character and update client magic icon</li>
	 * <li>Set the abnormal effect flag _afraid to False</li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR>
	 * <BR>
	 * @param effect
	 */
	public final void stopFear(L2Effect effect)
	{
		if (effect == null)
		{
			stopEffects(L2Effect.EffectType.FEAR);
		}
		else
		{
			removeEffect(effect);
		}
		
		setIsAfraid(false);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop a specified/all Muted abnormal L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Delete a specified/all (if effect=null) Muted abnormal L2Effect from L2Character and update client magic icon</li>
	 * <li>Set the abnormal effect flag _muted to False</li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR>
	 * <BR>
	 * @param effect
	 */
	public final void stopMuted(L2Effect effect)
	{
		if (effect == null)
		{
			stopEffects(L2Effect.EffectType.MUTE);
		}
		else
		{
			removeEffect(effect);
		}
		
		setIsMuted(false);
		updateAbnormalEffect();
	}
	
	public final void stopPhysicalMuted(L2Effect effect)
	{
		if (effect == null)
		{
			stopEffects(L2Effect.EffectType.PHYSICAL_MUTE);
		}
		else
		{
			removeEffect(effect);
		}
		
		setIsPhysicalMuted(false);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop a specified/all Root abnormal L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Delete a specified/all (if effect=null) Root abnormal L2Effect from L2Character and update client magic icon</li>
	 * <li>Set the abnormal effect flag _rooted to False</li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR>
	 * <BR>
	 * @param effect
	 */
	public final void stopRooting(L2Effect effect)
	{
		if (effect == null)
		{
			stopEffects(L2Effect.EffectType.ROOT);
		}
		else
		{
			removeEffect(effect);
		}
		
		setIsRooted(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop a specified/all Sleep abnormal L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Delete a specified/all (if effect=null) Sleep abnormal L2Effect from L2Character and update client magic icon</li>
	 * <li>Set the abnormal effect flag _sleeping to False</li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR>
	 * <BR>
	 * @param effect
	 */
	public final void stopSleeping(L2Effect effect)
	{
		if (effect == null)
		{
			stopEffects(L2Effect.EffectType.SLEEP);
		}
		else
		{
			removeEffect(effect);
		}
		
		setIsSleeping(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop a specified/all Stun abnormal L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Delete a specified/all (if effect=null) Stun abnormal L2Effect from L2Character and update client magic icon</li>
	 * <li>Set the abnormal effect flag _stunned to False</li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR>
	 * <BR>
	 * @param effect
	 */
	public final void stopStunning(L2Effect effect)
	{
		if (effect == null)
		{
			stopEffects(L2Effect.EffectType.STUN);
		}
		else
		{
			removeEffect(effect);
		}
		
		setIsStunned(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop a specified/all Paralyze abnormal L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Delete a specified/all (if effect=null) Paralyze abnormal L2Effect from L2Character and update client magic icon</li>
	 * <li>Set the abnormal effect flag _paralyzed to False</li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR>
	 * <BR>
	 * @param effect
	 */
	public final void stopParalyze(L2Effect effect)
	{
		if (effect == null)
		{
			stopEffects(L2Effect.EffectType.PARALYZE);
		}
		else
		{
			removeEffect(effect);
		}
		
		setIsParalyzed(false);
		getAI().notifyEvent(CtrlEvent.EVT_PARALYZED, null);
		updateAbnormalEffect();
	}
	
	/**
	 * Not Implemented.<BR>
	 * <BR>
	 * <B><U> Overridden in</U> :</B><BR>
	 * <BR>
	 * <li>L2NPCInstance</li>
	 * <li>L2PcInstance</li>
	 * <li>L2Summon</li>
	 * <li>L2DoorInstance</li><BR>
	 * <BR>
	 */
	public abstract void updateAbnormalEffect();
	
	/**
	 * Update active skills in progress (In Use and Not In Use because stacked) icons on client.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress (In Use and Not In Use because stacked) are represented by an icon on the client.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method ONLY UPDATE the client of the player and not clients of all players in the party.</B></FONT><BR>
	 * <BR>
	 */
	public final void updateEffectIcons()
	{
		updateEffectIcons(false);
	}
	
	public void updateEffectIcons(boolean partyOnly)
	{
		// overriden
	}
	
	// Property - Public
	/**
	 * Return a map of 16 bits (0x0000) containing all abnormal effect in progress for this L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * In Server->Client packet, each effect is represented by 1 bit of the map (ex : BLEEDING = 0x0001 (bit 1), SLEEP = 0x0080 (bit 8)...). The map is calculated by applying a BINARY OR operation on each effect.<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Server Packet : CharInfo, NpcInfo, UserInfo...</li><BR>
	 * <BR>
	 * @return
	 */
	public short getAbnormalEffect()
	{
		short ae = _AbnormalEffects;
		if (isStunned())
		{
			ae |= ABNORMAL_EFFECT_STUN;
		}
		if (isRooted())
		{
			ae |= ABNORMAL_EFFECT_ROOT;
		}
		if (isSleeping())
		{
			ae |= ABNORMAL_EFFECT_SLEEP;
		}
		if (isConfused())
		{
			ae |= ABNORMAL_EFFECT_CONFUSED;
		}
		if (isMuted() || isPhysicalMuted())
		{
			ae |= ABNORMAL_EFFECT_MUTED;
		}
		if (isAfraid())
		{
			ae |= ABNORMAL_EFFECT_AFRAID;
		}
		return ae;
	}
	
	/**
	 * Return all active skills effects in progress on the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in <B>_effects</B>. The Integer key of _effects is the L2Skill Identifier that has created the effect.<BR>
	 * <BR>
	 * @return A table containing all active skills effect in progress on the L2Character
	 */
	public final L2Effect[] getAllEffects()
	{
		// Create a copy of the effects set
		final FastTable<L2Effect> effects = _effects;
		
		// If no effect found, return EMPTY_EFFECTS
		if ((effects == null) || effects.isEmpty())
		{
			return EMPTY_EFFECTS;
		}
		
		// Return all effects in progress in a table
		final int ArraySize = effects.size();
		final L2Effect[] effectArray = new L2Effect[ArraySize];
		for (int i = 0; i < ArraySize; i++)
		{
			if ((i >= effects.size()) || (effects.get(i) == null))
			{
				break;
			}
			effectArray[i] = effects.get(i);
		}
		return effectArray;
	}
	
	/**
	 * Return L2Effect in progress on the L2Character corresponding to the L2Skill Identifier.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in <B>_effects</B>.
	 * @param index The L2Skill Identifier of the L2Effect to return from the _effects
	 * @return The L2Effect corresponding to the L2Skill Identifier
	 */
	public final L2Effect getFirstEffect(int index)
	{
		final FastTable<L2Effect> effects = _effects;
		if (effects == null)
		{
			return null;
		}
		
		L2Effect eventNotInUse = null;
		for (final L2Effect e : effects)
		{
			if ((e != null) && (e.getSkill().getId() == index))
			{
				if (e.getInUse())
				{
					return e;
				}
				eventNotInUse = e;
			}
		}
		return eventNotInUse;
	}
	
	/**
	 * Return the first L2Effect in progress on the L2Character created by the L2Skill.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in <B>_effects</B>.
	 * @param skill The L2Skill whose effect must be returned
	 * @return The first L2Effect created by the L2Skill
	 */
	public final L2Effect getFirstEffect(L2Skill skill)
	{
		final FastTable<L2Effect> effects = _effects;
		if (effects == null)
		{
			return null;
		}
		
		L2Effect eventNotInUse = null;
		for (final L2Effect e : effects)
		{
			if ((e != null) && (e.getSkill() == skill))
			{
				if (e.getInUse())
				{
					return e;
				}
				eventNotInUse = e;
			}
		}
		return eventNotInUse;
	}
	
	/**
	 * Return the first L2Effect in progress on the L2Character corresponding to the Effect Type (ex : BUFF, STUN, ROOT...).<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>. The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR>
	 * <BR>
	 * @param tp The Effect Type of skills whose effect must be returned
	 * @return first L2Effect corresponding to the Effect Type
	 */
	public final L2Effect getFirstEffect(L2Effect.EffectType tp)
	{
		final FastTable<L2Effect> effects = _effects;
		if (effects == null)
		{
			return null;
		}
		
		L2Effect eventNotInUse = null;
		for (final L2Effect e : effects)
		{
			if ((e != null) && (e.getEffectType() == tp))
			{
				if (e.getInUse())
				{
					return e;
				}
				eventNotInUse = e;
			}
		}
		return eventNotInUse;
	}
	// =========================================================
	
	// =========================================================
	// NEED TO ORGANIZE AND MOVE TO PROPER PLACE
	/** This class permit to the L2Character AI to obtain informations and uses L2Character method */
	public class AIAccessor
	{
		public AIAccessor()
		{
		}
		
		/**
		 * Return the L2Character managed by this Accessor AI.<BR>
		 * <BR>
		 * @return
		 */
		public L2Character getActor()
		{
			return L2Character.this;
		}
		
		/**
		 * Accessor to L2Character moveToLocation() method with an interaction area.<BR>
		 * <BR>
		 * @param x
		 * @param y
		 * @param z
		 * @param offset
		 */
		public void moveTo(int x, int y, int z, int offset)
		{
			moveToLocation(x, y, z, offset);
		}
		
		/**
		 * Accessor to L2Character moveToLocation() method without interaction area.<BR>
		 * <BR>
		 * @param x
		 * @param y
		 * @param z
		 */
		public void moveTo(int x, int y, int z)
		{
			moveToLocation(x, y, z, 0);
		}
		
		/**
		 * Accessor to L2Character stopMove() method.<BR>
		 * <BR>
		 * @param pos
		 */
		public void stopMove(L2CharPosition pos)
		{
			L2Character.this.stopMove(pos);
		}
		
		/**
		 * Accessor to L2Character doAttack() method.<BR>
		 * <BR>
		 * @param target
		 */
		public void doAttack(L2Character target)
		{
			L2Character.this.doAttack(target);
		}
		
		/**
		 * Accessor to L2Character doCast() method.<BR>
		 * <BR>
		 * @param skill
		 */
		public void doCast(L2Skill skill)
		{
			L2Character.this.doCast(skill);
		}
		
		/**
		 * Create a NotifyAITask.<BR>
		 * <BR>
		 * @param evt
		 * @return
		 */
		public NotifyAITask newNotifyTask(CtrlEvent evt)
		{
			return new NotifyAITask(evt);
		}
		
		/**
		 * Cancel the AI.<BR>
		 * <BR>
		 */
		public void detachAI()
		{
			_ai = null;
		}
	}
	
	/**
	 * This class group all mouvement data.<BR>
	 * <BR>
	 * <B><U> Data</U> :</B><BR>
	 * <BR>
	 * <li>_moveTimestamp : Last time position update</li>
	 * <li>_xDestination, _yDestination, _zDestination : Position of the destination</li>
	 * <li>_xMoveFrom, _yMoveFrom, _zMoveFrom : Position of the origin</li>
	 * <li>_moveStartTime : Start time of the movement</li>
	 * <li>_ticksToMove : Nb of ticks between the start and the destination</li>
	 * <li>_xSpeedTicks, _ySpeedTicks : Speed in unit/ticks</li><BR>
	 * <BR>
	 */
	public static class MoveData
	{
		// when we retrieve x/y/z we use GameTimeControl.getGameTicks()
		// if we are moving, but move timestamp==gameticks, we don't need
		// to recalculate position
		public int _moveTimestamp;
		public int _moveStartTime;
		public int _xDestination;
		public int _yDestination;
		public int _zDestination;
		public double _xAccurate; // otherwise there would be rounding errors
		public double _yAccurate;
		public double _zAccurate;
		public int _heading;
		
		public boolean disregardingGeodata;
		public int onGeodataPathIndex;
		public List<AbstractNodeLoc> geoPath;
		public int geoPathAccurateTx;
		public int geoPathAccurateTy;
		public int geoPathGtx;
		public int geoPathGty;
	}
	
	/** Table containing all skillId that are disabled */
	protected List<Integer> _disabledSkills;
	private boolean _allSkillsDisabled;
	
	/** Movement data of this L2Character */
	protected MoveData _move;
	
	/** Orientation of the L2Character */
	private int _heading;
	
	/** L2Character targeted by the L2Character */
	private L2Object _target;
	
	// set by the start of casting, in game ticks
	private int _castEndTime;
	private int _castInterruptTime;
	
	// set by the start of attack, in game ticks
	private int _attackEndTime;
	private int _attacking;
	private int _disableBowAttackEndTime;
	
	/** Table of calculators containing all standard NPC calculator (ex : ACCURACY_COMBAT, EVASION_RATE */
	private static final Calculator[] NPC_STD_CALCULATOR;
	static
	{
		NPC_STD_CALCULATOR = Formulas.getInstance().getStdNPCCalculators();
	}
	
	protected L2CharacterAI _ai;
	
	/** Future Skill Cast */
	protected Future<?> _skillCast;
	
	/** Char Coords from Client */
	private int _clientX;
	private int _clientY;
	private int _clientZ;
	private int _clientHeading;
	
	/** List of all QuestState instance that needs to be notified of this character's death */
	private List<QuestState> _NotifyQuestOfDeathList = new FastList<>();
	
	/**
	 * Add QuestState instance that is to be notified of character's death.<BR>
	 * <BR>
	 * @param qs The QuestState that subscribe to this event
	 */
	public void addNotifyQuestOfDeath(QuestState qs)
	{
		if ((qs == null) || _NotifyQuestOfDeathList.contains(qs))
		{
			return;
		}
		
		_NotifyQuestOfDeathList.add(qs);
	}
	
	/**
	 * Return a list of L2Character that attacked.<BR>
	 * <BR>
	 * @return
	 */
	public final List<QuestState> getNotifyQuestOfDeath()
	{
		if (_NotifyQuestOfDeathList == null)
		{
			_NotifyQuestOfDeathList = new FastList<>();
		}
		
		return _NotifyQuestOfDeathList;
	}
	
	/**
	 * Add a Func to the Calculator set of the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>. Each Calculator (a calculator per state) own a table of Func object. A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...). To reduce cache memory use,
	 * L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR>
	 * <BR>
	 * That's why, if a L2NPCInstance is under a skill/spell effect that modify one of its state, a copy of the NPC_STD_CALCULATOR must be create in its _calculators before addind new Func object.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>If _calculators is linked to NPC_STD_CALCULATOR, create a copy of NPC_STD_CALCULATOR in _calculators</li>
	 * <li>Add the Func object to _calculators</li><BR>
	 * <BR>
	 * @param f The Func object to add to the Calculator corresponding to the state affected
	 */
	public final synchronized void addStatFunc(Func f)
	{
		if (f == null)
		{
			return;
		}
		
		// Check if Calculator set is linked to the standard Calculator set of NPC
		if (_Calculators == NPC_STD_CALCULATOR)
		{
			// Create a copy of the standard NPC Calculator set
			_Calculators = new Calculator[Stats.NUM_STATS];
			
			for (int i = 0; i < Stats.NUM_STATS; i++)
			{
				if (NPC_STD_CALCULATOR[i] != null)
				{
					_Calculators[i] = new Calculator(NPC_STD_CALCULATOR[i]);
				}
			}
		}
		
		// Select the Calculator of the affected state in the Calculator set
		final int stat = f._stat.ordinal();
		
		if (_Calculators[stat] == null)
		{
			_Calculators[stat] = new Calculator();
		}
		
		// Add the Func to the calculator corresponding to the state
		_Calculators[stat].addFunc(f);
	}
	
	/**
	 * Add a list of Funcs to the Calculator set of the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>. Each Calculator (a calculator per state) own a table of Func object. A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...). <BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method is ONLY for L2PcInstance</B></FONT><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Equip an item from inventory</li>
	 * <li>Learn a new passive skill</li>
	 * <li>Use an active skill</li><BR>
	 * <BR>
	 * @param funcs The list of Func objects to add to the Calculator corresponding to the state affected
	 */
	public final synchronized void addStatFuncs(Func[] funcs)
	{
		final FastList<Stats> modifiedStats = new FastList<>();
		
		for (final Func f : funcs)
		{
			modifiedStats.add(f._stat);
			addStatFunc(f);
		}
		broadcastModifiedStats(modifiedStats);
	}
	
	/**
	 * Remove a Func from the Calculator set of the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>. Each Calculator (a calculator per state) own a table of Func object. A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...). To reduce cache memory use,
	 * L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR>
	 * <BR>
	 * That's why, if a L2NPCInstance is under a skill/spell effect that modify one of its state, a copy of the NPC_STD_CALCULATOR must be create in its _calculators before addind new Func object.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Remove the Func object from _calculators</li><BR>
	 * <BR>
	 * <li>If L2Character is a L2NPCInstance and _calculators is equal to NPC_STD_CALCULATOR, free cache memory and just create a link on NPC_STD_CALCULATOR in _calculators</li><BR>
	 * <BR>
	 * @param f The Func object to remove from the Calculator corresponding to the state affected
	 */
	public final synchronized void removeStatFunc(Func f)
	{
		if (f == null)
		{
			return;
		}
		
		// Select the Calculator of the affected state in the Calculator set
		final int stat = f._stat.ordinal();
		
		if (_Calculators[stat] == null)
		{
			return;
		}
		
		// Remove the Func object from the Calculator
		_Calculators[stat].removeFunc(f);
		
		if (_Calculators[stat].size() == 0)
		{
			_Calculators[stat] = null;
		}
		
		// If possible, free the memory and just create a link on NPC_STD_CALCULATOR
		if (this instanceof L2NpcInstance)
		{
			int i = 0;
			for (; i < Stats.NUM_STATS; i++)
			{
				if (!Calculator.equalsCals(_Calculators[i], NPC_STD_CALCULATOR[i]))
				{
					break;
				}
			}
			
			if (i >= Stats.NUM_STATS)
			{
				_Calculators = NPC_STD_CALCULATOR;
			}
		}
	}
	
	/**
	 * Remove a list of Funcs from the Calculator set of the L2PcInstance.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>. Each Calculator (a calculator per state) own a table of Func object. A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...). <BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method is ONLY for L2PcInstance</B></FONT><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Unequip an item from inventory</li>
	 * <li>Stop an active skill</li><BR>
	 * <BR>
	 * @param funcs The list of Func objects to add to the Calculator corresponding to the state affected
	 */
	public final synchronized void removeStatFuncs(Func[] funcs)
	{
		final FastList<Stats> modifiedStats = new FastList<>();
		
		for (final Func f : funcs)
		{
			removeStatFunc(f);
		}
		broadcastModifiedStats(modifiedStats);
	}
	
	/**
	 * Remove all Func objects with the selected owner from the Calculator set of the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>. Each Calculator (a calculator per state) own a table of Func object. A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...). To reduce cache memory use,
	 * L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR>
	 * <BR>
	 * That's why, if a L2NPCInstance is under a skill/spell effect that modify one of its state, a copy of the NPC_STD_CALCULATOR must be create in its _calculators before addind new Func object.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Remove all Func objects of the selected owner from _calculators</li><BR>
	 * <BR>
	 * <li>If L2Character is a L2NPCInstance and _calculators is equal to NPC_STD_CALCULATOR, free cache memory and just create a link on NPC_STD_CALCULATOR in _calculators</li><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Unequip an item from inventory</li>
	 * <li>Stop an active skill</li><BR>
	 * <BR>
	 * @param owner The Object(Skill, Item...) that has created the effect
	 */
	public final synchronized void removeStatsOwner(Object owner)
	{
		FastList<Stats> modifiedStats = null;
		// Go through the Calculator set
		for (int i = 0; i < _Calculators.length; i++)
		{
			if (_Calculators[i] != null)
			{
				// Delete all Func objects of the selected owner
				if (modifiedStats != null)
				{
					modifiedStats.addAll(_Calculators[i].removeOwner(owner));
				}
				else
				{
					modifiedStats = _Calculators[i].removeOwner(owner);
				}
				
				if (_Calculators[i].size() == 0)
				{
					_Calculators[i] = null;
				}
			}
		}
		
		// If possible, free the memory and just create a link on NPC_STD_CALCULATOR
		if (this instanceof L2NpcInstance)
		{
			int i = 0;
			for (; i < Stats.NUM_STATS; i++)
			{
				if (!Calculator.equalsCals(_Calculators[i], NPC_STD_CALCULATOR[i]))
				{
					break;
				}
			}
			
			if (i >= Stats.NUM_STATS)
			{
				_Calculators = NPC_STD_CALCULATOR;
			}
		}
		
		if (owner instanceof L2Effect)
		{
			if (!((L2Effect) owner).preventExitUpdate)
			{
				broadcastModifiedStats(modifiedStats);
			}
		}
		else
		{
			broadcastModifiedStats(modifiedStats);
		}
	}
	
	private void broadcastModifiedStats(FastList<Stats> stats)
	{
		if ((stats == null) || stats.isEmpty())
		{
			return;
		}
		
		boolean broadcastFull = false;
		
		StatusUpdate su = null;
		
		for (final Stats stat : stats)
		{
			if (this instanceof L2Summon)
			{
				((L2Summon) this).updateAndBroadcastStatus(1);
				break;
			}
			else if (stat == Stats.POWER_ATTACK_SPEED)
			{
				if (su == null)
				{
					su = new StatusUpdate(getObjectId());
				}
				su.addAttribute(StatusUpdate.ATK_SPD, getPAtkSpd());
			}
			else if (stat == Stats.MAGIC_ATTACK_SPEED)
			{
				if (su == null)
				{
					su = new StatusUpdate(getObjectId());
				}
				su.addAttribute(StatusUpdate.CAST_SPD, getMAtkSpd());
			}
			
			else if (stat == Stats.RUN_SPEED)
			{
				broadcastFull = true;
			}
			
		}
		
		if (this instanceof L2PcInstance)
		{
			if (broadcastFull)
			{
				((L2PcInstance) this).updateAndBroadcastStatus(2);
			}
			else
			{
				((L2PcInstance) this).updateAndBroadcastStatus(1);
				if (su != null)
				{
					broadcastPacket(su);
				}
				
			}
		}
		else if (this instanceof L2NpcInstance)
		{
			if (broadcastFull)
			{
				for (final L2PcInstance player : getKnownList().getKnownPlayers().values())
				{
					if (player != null)
					{
						if (getRunSpeed() == 0)
						{
							player.sendPacket(new ServerObjectInfo((L2NpcInstance) this, player));
						}
						else
						{
							player.sendPacket(new NpcInfo((L2NpcInstance) this, player));
						}
					}
				}
			}
			else if (su != null)
			{
				broadcastPacket(su);
			}
		}
		else if (su != null)
		{
			broadcastPacket(su);
		}
	}
	
	/**
	 * Return the orientation of the L2Character.<BR>
	 * <BR>
	 * @return
	 */
	public final int getHeading()
	{
		return _heading;
	}
	
	/**
	 * Set the orientation of the L2Character.<BR>
	 * <BR>
	 * @param heading
	 */
	public final void setHeading(int heading)
	{
		_heading = heading;
	}
	
	/**
	 * Return the X destination of the L2Character or the X position if not in movement.<BR>
	 * <BR>
	 * @return
	 */
	public final int getClientX()
	{
		return _clientX;
	}
	
	public final int getClientY()
	{
		return _clientY;
	}
	
	public final int getClientZ()
	{
		return _clientZ;
	}
	
	public final int getClientHeading()
	{
		return _clientHeading;
	}
	
	public final void setClientX(int val)
	{
		_clientX = val;
	}
	
	public final void setClientY(int val)
	{
		_clientY = val;
	}
	
	public final void setClientZ(int val)
	{
		_clientZ = val;
	}
	
	public final void setClientHeading(int val)
	{
		_clientHeading = val;
	}
	
	public final int getXdestination()
	{
		final MoveData m = _move;
		
		if (m != null)
		{
			return m._xDestination;
		}
		
		return getX();
	}
	
	/**
	 * Return the Y destination of the L2Character or the Y position if not in movement.<BR>
	 * <BR>
	 * @return
	 */
	public final int getYdestination()
	{
		final MoveData m = _move;
		
		if (m != null)
		{
			return m._yDestination;
		}
		
		return getY();
	}
	
	/**
	 * Return the Z destination of the L2Character or the Z position if not in movement.<BR>
	 * <BR>
	 * @return
	 */
	public final int getZdestination()
	{
		final MoveData m = _move;
		
		if (m != null)
		{
			return m._zDestination;
		}
		
		return getZ();
	}
	
	/**
	 * Return True if the L2Character is in combat.<BR>
	 * <BR>
	 * @return
	 */
	public boolean isInCombat()
	{
		return ((getAI().getAttackTarget() != null) || getAI().isAutoAttacking());
	}
	
	/**
	 * Return True if the L2Character is moving.<BR>
	 * <BR>
	 * @return
	 */
	public final boolean isMoving()
	{
		return _move != null;
	}
	
	/**
	 * Return True if the L2Character is travelling a calculated path.<BR>
	 * <BR>
	 * @return
	 */
	public final boolean isOnGeodataPath()
	{
		final MoveData m = _move;
		if (m == null)
		{
			return false;
		}
		if (m.onGeodataPathIndex == -1)
		{
			return false;
		}
		if (m.onGeodataPathIndex == (m.geoPath.size() - 1))
		{
			return false;
		}
		return true;
	}
	
	/**
	 * Return True if the L2Character is casting.<BR>
	 * <BR>
	 * @return
	 */
	public final boolean isCastingNow()
	{
		return _castEndTime > GameTimeController.getGameTicks();
	}
	
	/**
	 * Return True if the cast of the L2Character can be aborted.<BR>
	 * <BR>
	 * @return
	 */
	public final boolean canAbortCast()
	{
		return _castInterruptTime > GameTimeController.getGameTicks();
	}
	
	/**
	 * Return True if the L2Character is attacking.<BR>
	 * <BR>
	 * @return
	 */
	public boolean isAttackingNow()
	{
		return _attackEndTime > GameTimeController.getGameTicks();
	}
	
	/**
	 * Return True if the L2Character has aborted its attack.<BR>
	 * <BR>
	 * @return
	 */
	public final boolean isAttackAborted()
	{
		return _attacking <= 0;
	}
	
	/**
	 * Abort the attack of the L2Character and send Server->Client ActionFailed packet.<BR>
	 * <BR>
	 */
	public final void abortAttack()
	{
		if (isAttackingNow())
		{
			_attacking = 0;
			sendPacket(new ActionFailed());
		}
	}
	
	/**
	 * Returns body part (paperdoll slot) we are targeting right now
	 * @return
	 */
	public final int getAttackingBodyPart()
	{
		return _attacking;
	}
	
	/**
	 * Abort the cast of the L2Character and send Server->Client MagicSkillCanceld/ActionFailed packet.<BR>
	 * <BR>
	 */
	public final void abortCast()
	{
		if (isCastingNow())
		{
			if (_skillCast != null)
			{
				try
				{
					
					_skillCast.cancel(true);
				}
				catch (final NullPointerException e)
				{
				}
				
				_skillCast = null;
			}
			// cancels the skill hit scheduled task
			enableAllSkills(); // re-enables the skills
			_castInterruptTime = 0;
			if (this instanceof L2PcInstance)
			{
				getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING); // setting back previous intention
			}
			broadcastPacket(new MagicSkillCanceld(getObjectId())); // broadcast packet to stop animations client-side
			sendPacket(new ActionFailed()); // send an "action failed" packet to the caster
		}
	}
	
	/**
	 * Update the position of the L2Character during a movement and return True if the movement is finished.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * At the beginning of the move action, all properties of the movement are stored in the MoveData object called <B>_move</B> of the L2Character. The position of the start point and of the destination permit to estimated in function of the movement speed the time to achieve the destination.<BR>
	 * <BR>
	 * When the movement is started (ex : by MovetoLocation), this method will be called each 0.1 sec to estimate and update the L2Character position on the server. Note, that the current server position can differe from the current client position even if each movement is straight foward. That's
	 * why, client send regularly a Client->Server ValidatePosition packet to eventually correct the gap on the server. But, it's always the server position that is used in range calculation.<BR>
	 * <BR>
	 * At the end of the estimated movement time, the L2Character position is automatically set to the destination position even if the movement is not finished.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : The current Z position is obtained FROM THE CLIENT by the Client->Server ValidatePosition Packet. But x and y positions must be calculated to avoid that players try to modify their movement speed.</B></FONT><BR>
	 * <BR>
	 * @param gameTicks Nb of ticks since the server start
	 * @return True if the movement is finished
	 */
	public boolean updatePosition(int gameTicks)
	{
		// Get movement data
		final MoveData m = _move;
		
		if (m == null)
		{
			return true;
		}
		
		if (!isVisible())
		{
			_move = null;
			return true;
		}
		
		// Check if this is the first update
		if (m._moveTimestamp == 0)
		{
			m._moveTimestamp = m._moveStartTime;
			m._xAccurate = getX();
			m._yAccurate = getY();
		}
		
		// Check if the position has already been calculated
		if (m._moveTimestamp == gameTicks)
		{
			return false;
		}
		
		final int xPrev = getX();
		final int yPrev = getY();
		int zPrev = getZ(); // the z coordinate may be modified by coordinate synchronizations
		
		double dx, dy, dz;
		// the only method that can modify x,y while moving (otherwise _move would/should be set null)
		if (Config.COORD_SYNCHRONIZE == 1)
		{
			dx = m._xDestination - xPrev;
			dy = m._yDestination - yPrev;
		}
		else // otherwise we need saved temporary values to avoid rounding errors
		{
			dx = m._xDestination - m._xAccurate;
			dy = m._yDestination - m._yAccurate;
		}
		
		final boolean isFloating = isFlying() || isInsideZone(L2Character.ZONE_WATER);
		
		// Z coordinate will follow geodata or client values
		if ((Config.GEODATA > 0) && (Config.COORD_SYNCHRONIZE == 2) && !isFloating && !m.disregardingGeodata && !(this instanceof L2BoatInstance) && ((GameTimeController.getGameTicks() % 10) == 0 // once a second to reduce possible cpu load
		) && GeoData.getInstance().hasGeo(xPrev, yPrev))
		{
			final int geoHeight = GeoData.getInstance().getSpawnHeight(xPrev, yPrev, zPrev - 30, zPrev + 30);
			dz = m._zDestination - geoHeight;
			// quite a big difference, compare to validatePosition packet
			if ((this instanceof L2PcInstance) && (Math.abs(((L2PcInstance) this).getClientZ() - geoHeight) > 200) && (Math.abs(((L2PcInstance) this).getClientZ() - geoHeight) < 1500))
			{
				dz = m._zDestination - zPrev; // allow diff
			}
			else if (isInCombat() && (Math.abs(dz) > 200) && (((dx * dx) + (dy * dy)) < 40000))
			{
				dz = m._zDestination - zPrev; // climbing
			}
			else
			{
				zPrev = geoHeight;
			}
		}
		else
		{
			dz = m._zDestination - zPrev;
		}
		
		double delta = (dx * dx) + (dy * dy);
		if ((delta < 10000) && ((dz * dz) > 2500) // close enough, allows error between client and server geodata if it cannot be avoided
			&& !isFloating)
		{
			delta = Math.sqrt(delta);
		}
		else
		{
			delta = Math.sqrt(delta + (dz * dz));
		}
		
		double distFraction = Double.MAX_VALUE;
		if (delta > 1)
		{
			final double distPassed = (getStat().getMoveSpeed() * (gameTicks - m._moveTimestamp)) / GameTimeController.TICKS_PER_SECOND;
			distFraction = distPassed / delta;
		}
		
		if (distFraction > 1) // already there
		{
			// Set the position of the L2Character to the destination
			super.getPosition().setXYZ(m._xDestination, m._yDestination, m._zDestination);
		}
		else
		{
			m._xAccurate += dx * distFraction;
			m._yAccurate += dy * distFraction;
			
			// Set the position of the L2Character to estimated after parcial move
			super.getPosition().setXYZ((int) (m._xAccurate), (int) (m._yAccurate), zPrev + (int) ((dz * distFraction) + 0.5));
		}
		
		revalidateZone(false);
		
		// Set the timer of last position update to now
		m._moveTimestamp = gameTicks;
		
		return (distFraction > 1);
	}
	
	public void revalidateZone(boolean force)
	{
		if (getWorldRegion() == null)
		{
			return;
		}
		
		// This function is called too often from movement code
		if (force)
		{
			_zoneValidateCounter = 4;
		}
		else
		{
			_zoneValidateCounter--;
			if (_zoneValidateCounter < 0)
			{
				_zoneValidateCounter = 4;
			}
			else
			{
				return;
			}
		}
		
		getWorldRegion().revalidateZones(this);
	}
	
	/**
	 * Stop movement of the L2Character (Called by AI Accessor only).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Delete movement data of the L2Character</li>
	 * <li>Set the current position (x,y,z), its current L2WorldRegion if necessary and its heading</li>
	 * <li>Remove the L2Object object from _gmList** of GmListTable</li>
	 * <li>Remove object from _knownObjects and _knownPlayer* of all surrounding L2WorldRegion L2Characters</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T send Server->Client packet StopMove/StopRotation </B></FONT><BR>
	 * <BR>
	 * @param pos
	 */
	public void stopMove(L2CharPosition pos)
	{
		// Delete movement data of the L2Character
		_move = null;
		
		// Set the current position (x,y,z), its current L2WorldRegion if necessary and its heading
		// All data are contained in a L2CharPosition object
		if (pos != null)
		{
			setXYZ(pos.x, pos.y, pos.z);
			setHeading(pos.heading);
			revalidateZone(true);
		}
		broadcastPacket(new StopMove(this));
	}
	
	/**
	 * Target a L2Object (add the target to the L2Character _target, _knownObject and L2Character to _KnownObject of the L2Object).<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * The L2Object (including L2Character) targeted is identified in <B>_target</B> of the L2Character<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Set the _target of L2Character to L2Object</li>
	 * <li>If necessary, add L2Object to _knownObject of the L2Character</li>
	 * <li>If necessary, add L2Character to _KnownObject of the L2Object</li>
	 * <li>If object==null, cancel Attak or Cast</li><BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance : Remove the L2PcInstance from the old target _statusListener and add it to the new target if it was a L2Character</li><BR>
	 * <BR>
	 * @param object L2object to target
	 */
	public void setTarget(L2Object object)
	{
		if ((object != null) && !object.isVisible())
		{
			object = null;
		}
		
		if ((object != null) && (object != _target))
		{
			getKnownList().addKnownObject(object);
			object.getKnownList().addKnownObject(this);
		}
		
		_target = object;
	}
	
	/**
	 * Return the identifier of the L2Object targeted or -1.<BR>
	 * <BR>
	 * @return
	 */
	public final int getTargetId()
	{
		if (_target != null)
		{
			return _target.getObjectId();
		}
		
		return -1;
	}
	
	/**
	 * Return the L2Object targeted or null.<BR>
	 * <BR>
	 * @return
	 */
	public final L2Object getTarget()
	{
		return _target;
	}
	
	// called from AIAccessor only
	/**
	 * Calculate movement data for a move to location action and add the L2Character to movingObjects of GameTimeController (only called by AI Accessor).<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * At the beginning of the move action, all properties of the movement are stored in the MoveData object called <B>_move</B> of the L2Character. The position of the start point and of the destination permit to estimated in function of the movement speed the time to achieve the destination.<BR>
	 * <BR>
	 * All L2Character in movement are identified in <B>movingObjects</B> of GameTimeController that will call the updatePosition method of those L2Character each 0.1s.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Get current position of the L2Character</li>
	 * <li>Calculate distance (dx,dy) between current position and destination including offset</li>
	 * <li>Create and Init a MoveData object</li>
	 * <li>Set the L2Character _move object to MoveData object</li>
	 * <li>Add the L2Character to movingObjects of the GameTimeController</li>
	 * <li>Create a task to notify the AI that L2Character arrives at a check point of the movement</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T send Server->Client packet MoveToPawn/CharMoveToLocation </B></FONT><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>AI : onIntentionMoveTo(L2CharPosition), onIntentionPickUp(L2Object), onIntentionInteract(L2Object)</li>
	 * <li>FollowTask</li><BR>
	 * <BR>
	 * @param x The X position of the destination
	 * @param y The Y position of the destination
	 * @param z The Y position of the destination
	 * @param offset The size of the interaction area of the L2Character targeted
	 */
	protected void moveToLocation(int x, int y, int z, int offset)
	{
		// Get the Move Speed of the L2Character
		final float speed = getStat().getMoveSpeed();
		if ((speed <= 0) || isMovementDisabled())
		{
			return;
		}
		
		// Get current position of the L2Character
		final int curX = super.getX();
		final int curY = super.getY();
		final int curZ = super.getZ();
		
		// Calculate distance (dx,dy) between current position and destination
		// TODO: improve Z axis move/follow support when dx,dy are small compared to dz
		double dx = (x - curX);
		double dy = (y - curY);
		double dz = (z - curZ);
		double distance = Math.sqrt((dx * dx) + (dy * dy));
		
		final boolean verticalMovementOnly = isFlying() && (distance == 0) && (dz != 0);
		if (verticalMovementOnly)
		{
			distance = Math.abs(dz);
		}
		
		// make water move short and use no geodata checks for swimming chars
		// distance in a click can easily be over 3000
		if ((Config.GEODATA > 0) && !(this instanceof L2BoatInstance) && isInsideZone(ZONE_WATER) && (distance > 700))
		{
			final double divider = 700 / distance;
			x = curX + (int) (divider * dx);
			y = curY + (int) (divider * dy);
			z = curZ + (int) (divider * dz);
			dx = (x - curX);
			dy = (y - curY);
			dz = (z - curZ);
			distance = Math.sqrt((dx * dx) + (dy * dy));
		}
		
		if (Config.DEBUG)
		{
			_log.fine("distance to target:" + distance);
		}
		
		// Define movement angles needed
		// ^
		// | X (x,y)
		// | /
		// | /distance
		// | /
		// |/ angle
		// X ---------->
		// (curx,cury)
		
		double cos;
		double sin;
		
		// Check if a movement offset is defined or no distance to go through
		if ((offset > 0) || (distance < 1))
		{
			// approximation for moving closer when z coordinates are different
			// TODO: handle Z axis movement better
			offset -= Math.abs(dz);
			if (offset < 5)
			{
				offset = 5;
			}
			
			// If no distance to go through, the movement is cancelled
			if ((distance < 1) || ((distance - offset) <= 0))
			{
				if (Config.DEBUG)
				{
					_log.fine("already in range, no movement needed.");
				}
				
				// Notify the AI that the L2Character is arrived at destination
				getAI().notifyEvent(CtrlEvent.EVT_ARRIVED, null);
				
				return;
			}
			// Calculate movement angles needed
			sin = dy / distance;
			cos = dx / distance;
			
			distance -= (offset - 5); // due to rounding error, we have to move a bit closer to be in range
			
			// Calculate the new destination with offset included
			x = curX + (int) (distance * cos);
			y = curY + (int) (distance * sin);
		}
		else
		{
			// Calculate movement angles needed
			sin = dy / distance;
			cos = dx / distance;
		}
		
		// Create and Init a MoveData object
		final MoveData m = new MoveData();
		
		// GEODATA MOVEMENT CHECKS AND PATHFINDING
		m.onGeodataPathIndex = -1; // Initialize not on geodata path
		m.disregardingGeodata = false;
		
		if ((Config.GEODATA > 0) && !isFlying() // currently flying characters not checked
			&& !(this instanceof L2NpcWalkerInstance) && !(this instanceof L2BoatInstance) && (!isInsideZone(ZONE_WATER) || isInsideZone(ZONE_SIEGE))) // swimming also not checked - but distance is limited
		{
			final boolean isInBoat = (this instanceof L2PcInstance) && ((L2PcInstance) this).isInBoat();
			if (isInBoat)
			{
				m.disregardingGeodata = true;
			}
			
			final double originalDistance = distance;
			final int originalX = x;
			final int originalY = y;
			final int originalZ = z;
			final int gtx = (originalX - L2World.MAP_MIN_X) >> 4;
			final int gty = (originalY - L2World.MAP_MIN_Y) >> 4;
			
			// Movement checks:
			// when geodata == 2, for all characters except mobs returning home (could be changed later to teleport if pathfinding fails)
			// when geodata == 1, for l2playableinstance and l2riftinvaderinstance
			if (((Config.GEODATA == 2) && !((this instanceof L2Attackable) && ((L2Attackable) this).isReturningToSpawnPoint())) || ((this instanceof L2PcInstance) && !(isInBoat && (distance > 1500))) || isAfraid() || (this instanceof L2RiftInvaderInstance) || ((this instanceof L2Summon) && !(getAI().getIntention() == CtrlIntention.AI_INTENTION_FOLLOW))) // assuming
																																																																																									// intention_follow
																																																																																									// only
																																																																																									// when
																																																																																									// following
																																																																																									// owner
			{
				if (isOnGeodataPath())
				{
					try
					{
						if ((gtx == _move.geoPathGtx) && (gty == _move.geoPathGty))
						{
							return;
						}
						_move.onGeodataPathIndex = -1; // Set not on geodata path
					}
					catch (final NullPointerException e)
					{
					}
				}
				
				if ((curX < L2World.MAP_MIN_X) || (curX > L2World.MAP_MAX_X) || (curY < L2World.MAP_MIN_Y) || (curY > L2World.MAP_MAX_Y))
				{
					// Temporary fix for character outside world region errors
					_log.warning("Character " + getName() + " outside world area, in coordinates x:" + curX + " y:" + curY);
					getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					if (this instanceof L2PcInstance)
					{
						((L2PcInstance) this).logout();
					}
					else if (this instanceof L2Summon)
					{
						return;
					}
					else
					{
						onDecay();
					}
					return;
				}
				
				if (!isInBoat)
				{
					final Location destiny = GeoData.getInstance().moveCheck(curX, curY, curZ, x, y, z);
					// location different if destination wasn't reached (or just z coord is different)
					x = destiny.getX();
					y = destiny.getY();
					z = destiny.getZ();
					dx = x - curX;
					dy = y - curY;
					dz = z - curZ;
					distance = verticalMovementOnly ? Math.abs(dz * dz) : Math.sqrt((dx * dx) + (dy * dy));
				}
			}
			
			// Pathfinding checks. Only when geodata setting is 2, the LoS check gives shorter result
			// than the original movement was and the LoS gives a shorter distance than 2000
			// This way of detecting need for pathfinding could be changed.
			if ((Config.GEODATA == 2) && ((originalDistance - distance) > 30) && (distance < 2000) && !isAfraid())
			{
				// Path calculation
				// Overrides previous movement check
				if (((this instanceof L2PlayableInstance) && !isInBoat) || isInCombat() || (this instanceof L2MinionInstance))
				{
					m.geoPath = PathFinding.getInstance().findPath(curX, curY, curZ, originalX, originalY, originalZ, this instanceof L2PlayableInstance);
					if ((m.geoPath == null) || (m.geoPath.size() < 2)) // No path found
					{
						
						// Even though there's no path found (remember geonodes aren't perfect),
						// the mob is attacking and right now we set it so that the mob will go
						// after target anyway, if dz is small enough. Summons will follow their masters no matter what.
						if ((this instanceof L2PcInstance) || (!(this instanceof L2PlayableInstance) && !(this instanceof L2MinionInstance) && (Math.abs(z - curZ) > 140)) || ((this instanceof L2Summon) && !((L2Summon) this).getFollowStatus()))
						{
							
							getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
							return;
						}
						m.disregardingGeodata = true;
						x = originalX;
						y = originalY;
						z = originalZ;
						distance = originalDistance;
					}
					else
					{
						
						m.onGeodataPathIndex = 0; // on first segment
						m.geoPathGtx = gtx;
						m.geoPathGty = gty;
						m.geoPathAccurateTx = originalX;
						m.geoPathAccurateTy = originalY;
						
						x = m.geoPath.get(m.onGeodataPathIndex).getX();
						y = m.geoPath.get(m.onGeodataPathIndex).getY();
						z = m.geoPath.get(m.onGeodataPathIndex).getZ();
						
						// check for doors in the route
						if (DoorTable.getInstance().checkIfDoorsBetween(curX, curY, curZ, x, y, z))
						{
							m.geoPath = null;
							getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
							return;
						}
						
						for (int i = 0; i < (m.geoPath.size() - 1); i++)
						{
							if (DoorTable.getInstance().checkIfDoorsBetween(m.geoPath.get(i), m.geoPath.get(i + 1)))
							{
								m.geoPath = null;
								
								getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
								return;
							}
						}
						
						dx = x - curX;
						dy = y - curY;
						dz = z - curZ;
						distance = verticalMovementOnly ? Math.abs(dz * dz) : Math.sqrt((dx * dx) + (dy * dy));
						sin = dy / distance;
						cos = dx / distance;
						
					}
					
				}
			}
			
			// If no distance to go through, the movement is cancelled
			if ((distance < 1) && ((Config.GEODATA == 2) || (this instanceof L2PlayableInstance) || isAfraid() || (this instanceof L2RiftInvaderInstance)))
			{
				if (this instanceof L2Summon)
				{
					((L2Summon) this).setFollowStatus(false);
				}
				getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				return;
			}
		}
		
		// Apply Z distance for flying or swimming for correct timing calculations
		if ((isFlying() || isInsideZone(ZONE_WATER)) && !verticalMovementOnly)
		{
			distance = Math.sqrt((distance * distance) + (dz * dz));
		}
		
		// Calculate the Nb of ticks between the current position and the destination
		// One tick added for rounding reasons
		final int ticksToMove = 1 + (int) ((GameTimeController.TICKS_PER_SECOND * distance) / speed);
		m._xDestination = x;
		m._yDestination = y;
		m._zDestination = z; // this is what was requested from client
		
		// Calculate and set the heading of the L2Character
		m._heading = 0; // initial value for coordinate sync
		
		// Does not break heading on vertical movements
		if (!verticalMovementOnly)
		{
			setHeading(Util.calculateHeadingFrom(cos, sin));
		}
		
		if (Config.DEBUG)
		{
			_log.fine("dist:" + distance + "speed:" + speed + " ttt:" + ticksToMove + " heading:" + getHeading());
		}
		
		m._moveStartTime = GameTimeController.getGameTicks();
		
		// Set the L2Character _move object to MoveData object
		_move = m;
		
		// Add the L2Character to movingObjects of the GameTimeController
		// The GameTimeController manage objects movement
		GameTimeController.getInstance().registerMovingObject(this);
		
		// Create a task to notify the AI that L2Character arrives at a check point of the movement
		if ((ticksToMove * GameTimeController.MILLIS_IN_TICK) > 3000)
		{
			ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000);
		}
		
		// the CtrlEvent.EVT_ARRIVED will be sent when the character will actually arrive
		// to destination by GameTimeController
	}
	
	public boolean moveToNextRoutePoint()
	{
		if (!isOnGeodataPath())
		{
			// Cancel the move action
			_move = null;
			return false;
		}
		
		// Get the Move Speed of the L2Charcater
		final float speed = getStat().getMoveSpeed();
		if ((speed <= 0) || isMovementDisabled())
		{
			// Cancel the move action
			_move = null;
			return false;
		}
		
		final MoveData md = _move;
		if (md == null)
		{
			return false;
		}
		
		// Create and Init a MoveData object
		final MoveData m = new MoveData();
		
		// Update MoveData object
		m.onGeodataPathIndex = md.onGeodataPathIndex + 1; // next segment
		m.geoPath = md.geoPath;
		m.geoPathGtx = md.geoPathGtx;
		m.geoPathGty = md.geoPathGty;
		m.geoPathAccurateTx = md.geoPathAccurateTx;
		m.geoPathAccurateTy = md.geoPathAccurateTy;
		
		if (md.onGeodataPathIndex == (md.geoPath.size() - 2))
		{
			m._xDestination = md.geoPathAccurateTx;
			m._yDestination = md.geoPathAccurateTy;
			m._zDestination = md.geoPath.get(m.onGeodataPathIndex).getZ();
		}
		else
		{
			m._xDestination = md.geoPath.get(m.onGeodataPathIndex).getX();
			m._yDestination = md.geoPath.get(m.onGeodataPathIndex).getY();
			m._zDestination = md.geoPath.get(m.onGeodataPathIndex).getZ();
		}
		final double dx = (m._xDestination - super.getX());
		final double dy = (m._yDestination - super.getY());
		final double distance = Math.sqrt((dx * dx) + (dy * dy));
		
		// Calculate and set the heading of the L2Character
		if (distance != 0)
		{
			setHeading(Util.calculateHeadingFrom(getX(), getY(), m._xDestination, m._yDestination));
		}
		
		// Calculate the Nb of ticks between the current position and the destination
		// One tick added for rounding reasons
		final int ticksToMove = 1 + (int) ((GameTimeController.TICKS_PER_SECOND * distance) / speed);
		
		m._heading = 0; // initial value for coordinate sync
		
		m._moveStartTime = GameTimeController.getGameTicks();
		
		if (Config.DEBUG)
		{
			_log.fine("time to target:" + ticksToMove);
		}
		
		// Set the L2Character _move object to MoveData object
		_move = m;
		
		// Add the L2Character to movingObjects of the GameTimeController
		// The GameTimeController manage objects movement
		GameTimeController.getInstance().registerMovingObject(this);
		
		// Create a task to notify the AI that L2Character arrives at a check point of the movement
		if ((ticksToMove * GameTimeController.MILLIS_IN_TICK) > 3000)
		{
			ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000);
		}
		
		// the CtrlEvent.EVT_ARRIVED will be sent when the character will actually arrive
		// to destination by GameTimeController
		
		// Send a Server->Client packet CharMoveToLocation to the actor and all L2PcInstance in its _knownPlayers
		final CharMoveToLocation msg = new CharMoveToLocation(this);
		broadcastPacket(msg);
		
		return true;
	}
	
	public boolean validateMovementHeading(int heading)
	{
		final MoveData m = _move;
		
		if (m == null)
		{
			return true;
		}
		
		boolean result = true;
		if (m._heading != heading)
		{
			result = (m._heading == 0); // initial value or false
			m._heading = heading;
		}
		
		return result;
	}
	
	/**
	 * Return the distance between the current position of the L2Character and the target (x,y).<BR>
	 * <BR>
	 * @param x X position of the target
	 * @param y Y position of the target
	 * @return the plan distance
	 * @deprecated use getPlanDistanceSq(int x, int y, int z)
	 */
	@Deprecated
	public final double getDistance(int x, int y)
	{
		final double dx = x - getX();
		final double dy = y - getY();
		
		return Math.sqrt((dx * dx) + (dy * dy));
	}
	
	/**
	 * Return the distance between the current position of the L2Character and the target (x,y).<BR>
	 * <BR>
	 * @param x X position of the target
	 * @param y Y position of the target
	 * @param z
	 * @return the plan distance
	 * @deprecated use getPlanDistanceSq(int x, int y, int z)
	 */
	@Deprecated
	public final double getDistance(int x, int y, int z)
	{
		final double dx = x - getX();
		final double dy = y - getY();
		final double dz = z - getZ();
		
		return Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
	}
	
	/**
	 * Return the squared distance between the current position of the L2Character and the given object.<BR>
	 * <BR>
	 * @param object L2Object
	 * @return the squared distance
	 */
	public final double getDistanceSq(L2Object object)
	{
		return getDistanceSq(object.getX(), object.getY(), object.getZ());
	}
	
	/**
	 * Return the squared distance between the current position of the L2Character and the given x, y, z.<BR>
	 * <BR>
	 * @param x X position of the target
	 * @param y Y position of the target
	 * @param z Z position of the target
	 * @return the squared distance
	 */
	public final double getDistanceSq(int x, int y, int z)
	{
		final double dx = x - getX();
		final double dy = y - getY();
		final double dz = z - getZ();
		
		return ((dx * dx) + (dy * dy) + (dz * dz));
	}
	
	/**
	 * Return the squared plan distance between the current position of the L2Character and the given object.<BR>
	 * (check only x and y, not z)<BR>
	 * <BR>
	 * @param object L2Object
	 * @return the squared plan distance
	 */
	public final double getPlanDistanceSq(L2Object object)
	{
		return getPlanDistanceSq(object.getX(), object.getY());
	}
	
	/**
	 * Return the squared plan distance between the current position of the L2Character and the given x, y, z.<BR>
	 * (check only x and y, not z)<BR>
	 * <BR>
	 * @param x X position of the target
	 * @param y Y position of the target
	 * @return the squared plan distance
	 */
	public final double getPlanDistanceSq(int x, int y)
	{
		final double dx = x - getX();
		final double dy = y - getY();
		
		return ((dx * dx) + (dy * dy));
	}
	
	/**
	 * Check if this object is inside the given radius around the given object. Warning: doesn't cover collision radius!<BR>
	 * <BR>
	 * @param object the target
	 * @param radius the radius around the target
	 * @param checkZ should we check Z axis also
	 * @param strictCheck true if (distance < radius), false if (distance <= radius)
	 * @return true is the L2Character is inside the radius.
	 */
	public final boolean isInsideRadius(L2Object object, int radius, boolean checkZ, boolean strictCheck)
	{
		return isInsideRadius(object.getX(), object.getY(), object.getZ(), radius, checkZ, strictCheck);
	}
	
	/**
	 * Check if this object is inside the given plan radius around the given point.<BR>
	 * <BR>
	 * @param x X position of the target
	 * @param y Y position of the target
	 * @param radius the radius around the target
	 * @param strictCheck true if (distance < radius), false if (distance <= radius)
	 * @return true is the L2Character is inside the radius.
	 */
	public final boolean isInsideRadius(int x, int y, int radius, boolean strictCheck)
	{
		return isInsideRadius(x, y, 0, radius, false, strictCheck);
	}
	
	/**
	 * Check if this object is inside the given radius around the given point. Warning: doesn't cover collision radius!<BR>
	 * <BR>
	 * @param x X position of the target
	 * @param y Y position of the target
	 * @param z Z position of the target
	 * @param radius the radius around the target
	 * @param checkZ should we check Z axis also
	 * @param strictCheck true if (distance < radius), false if (distance <= radius)
	 * @return true is the L2Character is inside the radius.
	 */
	public final boolean isInsideRadius(int x, int y, int z, int radius, boolean checkZ, boolean strictCheck)
	{
		final double dx = x - getX();
		final double dy = y - getY();
		final double dz = z - getZ();
		
		if (strictCheck)
		{
			if (checkZ)
			{
				return ((dx * dx) + (dy * dy) + (dz * dz)) < (radius * radius);
			}
			return ((dx * dx) + (dy * dy)) < (radius * radius);
		}
		if (checkZ)
		{
			return ((dx * dx) + (dy * dy) + (dz * dz)) <= (radius * radius);
		}
		return ((dx * dx) + (dy * dy)) <= (radius * radius);
	}
	
	/**
	 * Set _attacking corresponding to Attacking Body part to CHEST.<BR>
	 * <BR>
	 */
	public void setAttackingBodypart()
	{
		_attacking = Inventory.PAPERDOLL_CHEST;
	}
	
	/**
	 * Retun True if arrows are available.<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance</li><BR>
	 * <BR>
	 * @return
	 */
	protected boolean checkAndEquipArrows()
	{
		return true;
	}
	
	/**
	 * Add Exp and Sp to the L2Character.<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance</li>
	 * <li>L2PetInstance</li><BR>
	 * <BR>
	 * @param addToExp
	 * @param addToSp
	 */
	public void addExpAndSp(long addToExp, int addToSp)
	{
		// Dummy method (overridden by players and pets)
	}
	
	/**
	 * Return the active weapon instance (always equiped in the right hand).<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance</li><BR>
	 * <BR>
	 * @return
	 */
	public abstract L2ItemInstance getActiveWeaponInstance();
	
	/**
	 * Return the active weapon item (always equiped in the right hand).<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance</li><BR>
	 * <BR>
	 * @return
	 */
	public abstract L2Weapon getActiveWeaponItem();
	
	/**
	 * Return the secondary weapon instance (always equiped in the left hand).<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance</li><BR>
	 * <BR>
	 * @return
	 */
	public abstract L2ItemInstance getSecondaryWeaponInstance();
	
	/**
	 * Return the secondary weapon item (always equiped in the left hand).<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance</li><BR>
	 * <BR>
	 * @return
	 */
	public abstract L2Weapon getSecondaryWeaponItem();
	
	/**
	 * Manage hit process (called by Hit Task).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL and send a Server->Client packet ActionFailed (if attacker is a L2PcInstance)</li>
	 * <li>If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are L2PcInstance</li>
	 * <li>If attack isn't aborted and hit isn't missed, reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary</li>
	 * <li>if attack isn't aborted and hit isn't missed, manage attack or cast break of the target (calculating rate, sending message...)</li><BR>
	 * <BR>
	 * @param target The L2Character targeted
	 * @param damage Nb of HP to reduce
	 * @param crit True if hit is critical
	 * @param miss True if hit is missed
	 * @param soulshot True if SoulShot are charged
	 * @param shld True if shield is efficient
	 */
	protected void onHitTimer(L2Character target, int damage, boolean crit, boolean miss, boolean soulshot, boolean shld)
	{
		// If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL
		// and send a Server->Client packet ActionFailed (if attacker is a L2PcInstance)
		if ((target == null) || isAlikeDead() || ((this instanceof L2NpcInstance) && ((L2NpcInstance) this).isEventMob))
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			sendPacket(new ActionFailed());
			return;
		}
		
		if (((this instanceof L2NpcInstance) && target.isAlikeDead()) || target.isDead() || (!getKnownList().knowsObject(target) && !(this instanceof L2DoorInstance)))
		{
			// getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			
			sendPacket(new ActionFailed());
			return;
		}
		
		if (miss)
		{
			if (target instanceof L2PcInstance)
			{
				final SystemMessage sm = new SystemMessage(SystemMessage.AVOIDED_S1s_ATTACK);
				
				if (this instanceof L2Summon)
				{
					final int mobId = ((L2Summon) this).getTemplate().npcId;
					sm.addNpcName(mobId);
				}
				else
				{
					sm.addString(getName());
				}
				
				((L2PcInstance) target).sendPacket(sm);
			}
		}
		
		// If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are L2PcInstance
		if (!isAttackAborted())
		{
			int level = 0;
			if (this instanceof L2PcInstance)
			{
				level = getLevel();
			}
			else if (this instanceof L2Summon)
			{
				level = ((L2Summon) this).getOwner().getLevel();
			}
			
			if (target.isRaid() && (level > (target.getLevel() + 8)))
			{
				final L2Skill skill = SkillTable.getInstance().getInfo(4515, 1);
				
				if (skill != null)
				{
					skill.getEffects(target, this);
				}
				else
				{
					_log.warning("Skill 4515 at level 1 is missing in DP.");
				}
				damage = 0; // prevents messing up drop calculation
			}
			
			sendDamageMessage(target, damage, false, crit, miss);
			
			if (!miss && (damage > 0))
			{
				final L2Weapon weapon = getActiveWeaponItem();
				final boolean isBow = ((weapon != null) && (weapon.getItemType() == L2WeaponType.BOW));
				
				int reflectedDamage = 0;
				if (!isBow && !target.isInvul()) // Do not reflect if weapon is of type bow
				{
					
					// Reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary
					double reflectPercent = target.getStat().calcStat(Stats.REFLECT_DAMAGE_PERCENT, 0, null, null);
					
					// Check if RaidBoss level is high enough for reflect
					if (target.isRaid() && (getLevel() > (target.getLevel() + 8)))
					{
						reflectPercent = 0;
					}
					
					if (reflectPercent > 0)
					{
						reflectedDamage = (int) ((reflectPercent / 100.) * damage);
						damage -= reflectedDamage;
						
						if (reflectedDamage > target.getMaxHp())
						{
							reflectedDamage = target.getMaxHp();
						}
					}
				}
				
				// reduce target HP
				target.reduceCurrentHp(damage, this);
				
				if (reflectedDamage > 0)
				{
					reduceCurrentHp(reflectedDamage, target, true);
				}
				
				if (!isBow) // Do not absorb if weapon is of type bow
				{
					// Absorb HP from the damage inflicted
					final double absorbPercent = getStat().calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0, null, null);
					
					if (absorbPercent > 0)
					{
						final int maxCanAbsorb = (int) (getMaxHp() - getCurrentHp());
						int absorbDamage = (int) ((absorbPercent / 100.) * damage);
						
						if (absorbDamage > maxCanAbsorb)
						{
							absorbDamage = maxCanAbsorb; // Can't absord more than max hp
						}
						
						setCurrentHp(getCurrentHp() + absorbDamage);
					}
				}
				
				// Notify AI with EVT_ATTACKED
				target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
				getAI().clientStartAutoAttack();
				
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (Formulas.getInstance().calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}
			}
			
			if (crit)
			{
				// Launch weapon Special ability effect if available
				final L2Weapon activeWeapon = getActiveWeaponItem();
				if (activeWeapon != null)
				{
					activeWeapon.getSkillEffects(this, target);
				}
			}
			
			return;
		}
		
		getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
	}
	
	/**
	 * Break an attack and send Server->Client ActionFailed packet and a System Message to the L2Character.<BR>
	 * <BR>
	 */
	public void breakAttack()
	{
		if (isAttackingNow())
		{
			// Abort the attack of the L2Character and send Server->Client ActionFailed packet
			abortAttack();
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			
			if (this instanceof L2PcInstance)
			{
				// Send a system message
				sendPacket(new SystemMessage(SystemMessage.ATTACK_FAILED));
			}
		}
	}
	
	/**
	 * Break a cast and send Server->Client ActionFailed packet and a System Message to the L2Character.<BR>
	 * <BR>
	 */
	public void breakCast()
	{
		// damage can only cancel magical skills
		if (isCastingNow() && canAbortCast() && (getLastSkillCast() != null) && getLastSkillCast().isMagic())
		{
			// Abort the cast of the L2Character and send Server->Client MagicSkillCanceld/ActionFailed packet.
			abortCast();
			
			if (this instanceof L2PcInstance)
			{
				// Send a system message
				sendPacket(new SystemMessage(SystemMessage.CASTING_INTERRUPTED));
			}
		}
	}
	
	/**
	 * Reduce the arrow number of the L2Character.<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance</li><BR>
	 * <BR>
	 */
	protected void reduceArrowCount()
	{
		// default is to do nothin
	}
	
	/**
	 * Manage Forced attack (shift + select target).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>If L2Character or target is in a town area, send a system message TARGET_IN_PEACEZONE a Server->Client packet ActionFailed</li>
	 * <li>If target is confused, send a Server->Client packet ActionFailed</li>
	 * <li>If L2Character is a L2ArtefactInstance, send a Server->Client packet ActionFailed</li>
	 * <li>Send a Server->Client packet MyTargetSelected to start attack and Notify AI with AI_INTENTION_ATTACK</li><BR>
	 * <BR>
	 * @param player The L2PcInstance to attack
	 */
	@Override
	public void onForcedAttack(L2PcInstance player)
	{
		if (isInsidePeaceZone(player))
		{
			// If L2Character or target is in a peace zone, send a system message TARGET_IN_PEACEZONE a Server->Client packet ActionFailed
			player.sendPacket(new SystemMessage(SystemMessage.TARGET_IN_PEACEZONE));
			player.sendPacket(new ActionFailed());
			return;
		}
		
		if (player.isInOlympiadMode() && (player.getTarget() != null))
		{
			L2PcInstance target;
			if (player.getTarget() instanceof L2Summon)
			{
				target = ((L2Summon) player.getTarget()).getOwner();
			}
			else
			{
				target = (L2PcInstance) player.getTarget();
			}
			
			if ((target == null) || (target.isInOlympiadMode() && (!player.isOlympiadStart() || (player.getOlympiadGameId() != target.getOlympiadGameId()))))
			{
				// if L2PcInstance is in Olympia and the match isn't already start, send a Server->Client packet ActionFailed
				player.sendPacket(new ActionFailed());
				return;
			}
		}
		
		if ((player.getTarget() != null) && !player.getTarget().isAttackable() && (player.getAccessLevel() < Config.GM_PEACEATTACK))
		{
			// If target is not attackable, send a Server->Client packet ActionFailed
			player.sendPacket(new ActionFailed());
			return;
		}
		
		if (player.isConfused())
		{
			// If target is confused, send a Server->Client packet ActionFailed
			player.sendPacket(new ActionFailed());
			return;
		}
		
		// GeoData Los Check or dz > 1000
		if (!GeoData.getInstance().canSeeTarget(player, this))
		{
			player.sendPacket(new SystemMessage(SystemMessage.CANT_SEE_TARGET));
			player.sendPacket(new ActionFailed());
			return;
		}
		
		// Notify AI with AI_INTENTION_ATTACK
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
	}
	
	/**
	 * Return True if inside peace zone.<BR>
	 * <BR>
	 * @param attacker
	 * @return
	 */
	public boolean isInsidePeaceZone(L2PcInstance attacker)
	{
		return isInsidePeaceZone(attacker, this);
	}
	
	public boolean isInsidePeaceZone(L2Object attacker, L2Object target)
	{
		if (target == null)
		{
			return false;
		}
		
		if (target instanceof L2NpcInstance)
		{
			return false;
		}
		
		if (attacker instanceof L2NpcInstance)
		{
			return false;
		}
		
		final L2PcInstance attackingPlayer = attacker.getActingPlayer();
		
		if ((attackingPlayer != null) && (attackingPlayer.getAccessLevel() >= Config.GM_PEACEATTACK))
		{
			return false;
		}
		
		if (Config.ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE)
		{
			final L2PcInstance targetPlayer = target.getActingPlayer();
			
			// allows red to be attacked and red to attack flagged players
			if ((targetPlayer != null) && (targetPlayer.getKarma() > 0))
			{
				return false;
			}
			
			if ((attackingPlayer != null) && (attackingPlayer.getKarma() > 0))
			{
				if ((targetPlayer != null) && (targetPlayer.getPvpFlag() > 0))
				{
					return false;
				}
			}
		}
		
		if ((attacker instanceof L2Character) && (target instanceof L2Character))
		{
			return (((L2Character) target).isInsideZone(ZONE_PEACE) || ((L2Character) attacker).isInsideZone(ZONE_PEACE));
		}
		
		if (attacker instanceof L2Character)
		{
			return ((TownManager.getInstance().getTown(target.getX(), target.getY(), target.getZ()) != null) || ((L2Character) attacker).isInsideZone(ZONE_PEACE));
		}
		
		return ((TownManager.getInstance().getTown(target.getX(), target.getY(), target.getZ()) != null) || (TownManager.getInstance().getTown(attacker.getX(), attacker.getY(), attacker.getZ()) != null));
	}
	
	/**
	 * return true if this character is inside an active grid.
	 * @return
	 */
	public boolean isInActiveRegion()
	{
		
		return ((getWorldRegion() != null) && getWorldRegion().isActive());
		
	}
	
	/**
	 * Return True if the L2Character has a Party in progress.<BR>
	 * <BR>
	 * @return
	 */
	public boolean isInParty()
	{
		return false;
	}
	
	/**
	 * Return the L2Party object of the L2Character.<BR>
	 * <BR>
	 * @return
	 */
	public L2Party getParty()
	{
		return null;
	}
	
	/**
	 * Return the Attack Speed of the L2Character (delay (in milliseconds) before next attack).<BR>
	 * <BR>
	 * @param target
	 * @param weapon
	 * @return
	 */
	public int calculateTimeBetweenAttacks(L2Character target, L2Weapon weapon)
	{
		double atkSpd = 0;
		
		if (weapon != null)
		{
			switch (weapon.getItemType())
			{
				case BOW:
					atkSpd = getStat().getPAtkSpd();
					return (int) ((1500 * 345) / atkSpd);
				case DAGGER:
					atkSpd = getStat().getPAtkSpd();
					// atkSpd /= 1.15;
					break;
				default:
					atkSpd = getStat().getPAtkSpd();
			}
		}
		else
		{
			atkSpd = getPAtkSpd();
		}
		
		return Formulas.getInstance().calcPAtkSpd(this, target, atkSpd);
	}
	
	public int calculateReuseTime(L2Character target, L2Weapon weapon)
	{
		if (weapon == null)
		{
			return 0;
		}
		
		int reuse = weapon.getAttackReuseDelay();
		// only bows should continue for now
		if (reuse == 0)
		{
			return 0;
		}
		
		reuse *= getStat().getWeaponReuseModifier(target);
		final double atkSpd = getStat().getPAtkSpd();
		switch (weapon.getItemType())
		{
			case BOW:
				return (int) ((reuse * 345) / atkSpd);
			default:
				return (int) ((reuse * 312) / atkSpd);
		}
	}
	
	/**
	 * Return True if the L2Character use a dual weapon.<BR>
	 * <BR>
	 * @return
	 */
	public boolean isUsingDualWeapon()
	{
		return false;
	}
	
	/**
	 * Add a skill to the L2Character _skills and its Func objects to the calculator set of the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All skills own by a L2Character are identified in <B>_skills</B><BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Replace oldSkill by newSkill or Add the newSkill</li>
	 * <li>If an old skill has been replaced, remove all its Func objects of L2Character calculator set</li>
	 * <li>Add Func objects of newSkill to the calculator set of the L2Character</li><BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance : Save update in the character_skills table of the database</li><BR>
	 * <BR>
	 * @param newSkill The L2Skill to add to the L2Character
	 * @return The L2Skill replaced or null if just added a new L2Skill
	 */
	public L2Skill addSkill(L2Skill newSkill)
	{
		L2Skill oldSkill = null;
		
		if (newSkill != null)
		{
			// Replace oldSkill by newSkill or Add the newSkill
			oldSkill = _Skills.put(newSkill.getId(), newSkill);
			
			// If an old skill has been replaced, remove all its Func objects
			if (oldSkill != null)
			{
				removeStatsOwner(oldSkill);
			}
			
			// Add Func objects of newSkill to the calculator set of the L2Character
			addStatFuncs(newSkill.getStatFuncs(null, this));
		}
		
		return oldSkill;
	}
	
	/**
	 * Remove a skill from the L2Character and its Func objects from calculator set of the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All skills own by a L2Character are identified in <B>_skills</B><BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Remove the skill from the L2Character _skills</li>
	 * <li>Remove all its Func objects from the L2Character calculator set</li><BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance : Save update in the character_skills table of the database</li><BR>
	 * <BR>
	 * @param skill The L2Skill to remove from the L2Character
	 * @return The L2Skill removed
	 */
	public L2Skill removeSkill(L2Skill skill)
	{
		if (skill == null)
		{
			return null;
		}
		
		// Remove the skill from the L2Character _skills
		final L2Skill oldSkill = _Skills.remove(skill.getId());
		
		// Remove all its Func objects from the L2Character calculator set
		if (oldSkill != null)
		{
			removeStatsOwner(oldSkill);
		}
		
		return oldSkill;
	}
	
	/**
	 * Return all skills own by the L2Character in a table of L2Skill.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All skills own by a L2Character are identified in <B>_skills</B> the L2Character <BR>
	 * <BR>
	 * @return
	 */
	public final L2Skill[] getAllSkills()
	{
		if (_Skills == null)
		{
			return new L2Skill[0];
		}
		
		return _Skills.values().toArray(new L2Skill[_Skills.values().size()]);
	}
	
	/**
	 * Return the level of a skill owned by the L2Character.<BR>
	 * <BR>
	 * @param skillId The identifier of the L2Skill whose level must be returned
	 * @return The level of the L2Skill identified by skillId
	 */
	public int getSkillLevel(int skillId)
	{
		if (_Skills == null)
		{
			return -1;
		}
		
		final L2Skill skill = _Skills.get(skillId);
		
		if (skill == null)
		{
			return -1;
		}
		return skill.getLevel();
	}
	
	/**
	 * Return True if the skill is known by the L2Character.<BR>
	 * <BR>
	 * @param skillId The identifier of the L2Skill to check the knowledge
	 * @return
	 */
	public final L2Skill getKnownSkill(int skillId)
	{
		if (_Skills == null)
		{
			return null;
		}
		
		return _Skills.get(skillId);
	}
	
	/**
	 * Return the number of skills of type(Buff, Debuff, HEAL_PERCENT) affecting this L2Character.<BR>
	 * <BR>
	 * @return The number of Buffs affecting this L2Character
	 */
	public int getBuffCount()
	{
		final L2Effect[] effects = getAllEffects();
		int numBuffs = 0;
		
		if (effects == null)
		{
			return numBuffs;
		}
		
		for (final L2Effect e : effects)
		{
			if (e == null)
			{
				continue;
			}
			
			if (e.getShowIcon() && ((e.getSkill().getSkillType() == L2Skill.SkillType.BUFF) || e.getSkill().isOffensive() || (e.getSkill().getSkillType() == L2Skill.SkillType.REFLECT) || (e.getSkill().getSkillType() == L2Skill.SkillType.HEAL_PERCENT)) &&
				
				!((e.getSkill().getId() > 4360) && (e.getSkill().getId() < 4367)))
			{
				numBuffs++;
			}
		}
		
		return numBuffs;
	}
	
	/**
	 * Removes the first Buff of this L2Character.<BR>
	 * <BR>
	 * @param preferSkill
	 */
	public void removeFirstBuff(int preferSkill)
	{
		final L2Effect[] effects = getAllEffects();
		if (effects == null)
		{
			return;
		}
		
		L2Effect removeMe = null;
		for (final L2Effect e : effects)
		{
			if (e == null)
			{
				continue;
			}
			
			if (((e.getSkill().getSkillType() == L2Skill.SkillType.BUFF) || e.getSkill().isOffensive() || (e.getSkill().getSkillType() == L2Skill.SkillType.REFLECT) || (e.getSkill().getSkillType() == L2Skill.SkillType.HEAL_PERCENT)) &&
				
				!((e.getSkill().getId() > 4360) && (e.getSkill().getId() < 4367)))
			{
				if (preferSkill == 0)
				{
					removeMe = e;
					break;
				}
				else if (e.getSkill().getId() == preferSkill)
				{
					removeMe = e;
					break;
				}
				else if (removeMe == null)
				{
					removeMe = e;
				}
			}
		}
		
		if (removeMe != null)
		{
			removeMe.exit();
		}
	}
	
	public int getDanceCount()
	{
		int danceCount = 0;
		final L2Effect[] effects = getAllEffects();
		for (final L2Effect effect : effects)
		{
			if (effect == null)
			{
				continue;
			}
			if ((effect.getSkill().getNextDanceMpCost() > 0) && effect.getInUse())
			{
				danceCount++;
			}
		}
		return danceCount;
	}
	
	/**
	 * Checks if the given skill stacks with an existing one.<BR>
	 * <BR>
	 * @param checkSkill the skill to be checked
	 * @return Returns whether or not this skill will stack
	 */
	private boolean doesStack(L2Skill checkSkill)
	{
		if ((_effects == null) || (_effects.size() < 1) || (checkSkill._effectTemplates == null) || (checkSkill._effectTemplates.length < 1) || (checkSkill._effectTemplates[0]._stackType == null))
		{
			return false;
		}
		
		final String stackType = checkSkill._effectTemplates[0]._stackType;
		if (stackType.equals("none"))
		{
			return false;
		}
		
		for (final L2Effect e : _effects)
		{
			if (e == null)
			{
				continue;
			}
			
			if ((e.getStackType() != null) && e.getStackType().equals(stackType))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Manage the magic skill launching task (MP, HP, Item consummation...) and display the magic skill animation on client.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Send a Server->Client packet MagicSkillLaunched (to display magic skill animation) to all L2PcInstance of L2Charcater _knownPlayers</li>
	 * <li>Consumme MP, HP and Item if necessary</li>
	 * <li>Send a Server->Client packet StatusUpdate with MP modification to the L2PcInstance</li>
	 * <li>Launch the magic skill in order to calculate its effects</li>
	 * <li>If the skill type is PDAM, notify the AI of the target with AI_INTENTION_ATTACK</li>
	 * <li>Notify the AI of the L2Character with EVT_FINISH_CASTING</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : A magic skill casting MUST BE in progress</B></FONT><BR>
	 * <BR>
	 * @param targets
	 * @param skill The L2Skill to use
	 * @param coolTime
	 * @param instant
	 * @param crit
	 */
	public void onMagicLaunchedTimer(L2Object[] targets, L2Skill skill, int coolTime, boolean instant, boolean crit)
	{
		if ((skill == null) || (targets == null) || (targets.length <= 0))
		{
			_skillCast = null;
			enableAllSkills();
			_castInterruptTime = 0;
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		int escapeRange = 0;
		if (skill.getEffectRange() > escapeRange)
		{
			escapeRange = skill.getEffectRange();
		}
		else if ((skill.getCastRange() < 0) && (skill.getSkillRadius() > 80))
		{
			escapeRange = skill.getSkillRadius();
		}
		
		if (escapeRange > 0)
		{
			final List<L2Character> targetList = new FastList<>();
			for (final L2Object target : targets)
			{
				if (target instanceof L2Character)
				{
					if (!isInsideRadius(target, escapeRange, true, false))
					{
						continue;
					}
					
					if (skill.isOffensive())
					{
						if (this instanceof L2PcInstance)
						{
							if (((L2Character) target).isInsidePeaceZone((L2PcInstance) this))
							{
								continue;
							}
						}
						else
						{
							if (((L2Character) target).isInsidePeaceZone(this, target))
							{
								continue;
							}
						}
					}
					
					targetList.add((L2Character) target);
				}
				
			}
			
			if (targetList.isEmpty())
			{
				_skillCast = null;
				enableAllSkills();
				_castInterruptTime = 0;
				getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
				return;
			}
			targets = targetList.toArray(new L2Character[targetList.size()]);
		}
		
		// Ensure that a cast is in progress
		// Check if player is using fake death.
		if (!isCastingNow() || isAlikeDead())
		{
			_skillCast = null;
			enableAllSkills();
			_castInterruptTime = 0;
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		// Get the display identifier of the skill
		final int magicId = skill.getDisplayId();
		
		// Get the level of the skill
		int level = skill.getLevel();
		
		if (level < 1)
		{
			level = 1;
		}
		
		// Send a Server->Client packet MagicSkillLaunched to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
		broadcastPacket(new MagicSkillLaunched(this, magicId, level, targets));
		
		if (instant)
		{
			onMagicHitTimer(targets, skill, coolTime, true, crit);
		}
		else
		{
			_skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, crit, 2), 200);
		}
		
	}
	
	/*
	 * Runs in the end of skill casting
	 */
	public void onMagicHitTimer(L2Object[] targets, L2Skill skill, int coolTime, boolean instant, boolean crit)
	{
		if ((skill == null) || (targets == null) || (targets.length <= 0))
		{
			_skillCast = null;
			enableAllSkills();
			_castInterruptTime = 0;
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		try
		{
			// Go through targets table
			for (final L2Object tgt : targets)
			{
				
				if (tgt instanceof L2PlayableInstance)
				{
					final L2Character target = (L2Character) tgt;
					if ((this instanceof L2PcInstance) && (target instanceof L2Summon))
					{
						((L2Summon) target).updateAndBroadcastStatus(1);
					}
				}
			}
			
			final StatusUpdate su = new StatusUpdate(getObjectId());
			boolean isSendStatus = false;
			
			// Consume MP of the L2Character and Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
			final double mpConsume = getStat().getMpConsume(skill);
			if (mpConsume > 0)
			{
				getStatus().reduceMp(mpConsume);
				su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
				isSendStatus = true;
			}
			
			// Consume HP if necessary and Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
			if (skill.getHpConsume() > 0)
			{
				double consumeHp;
				
				consumeHp = calcStat(Stats.HP_CONSUME_RATE, skill.getHpConsume(), null, null);
				if ((consumeHp + 1) >= getCurrentHp())
				{
					consumeHp = getCurrentHp() - 1.0;
				}
				
				getStatus().reduceHp(consumeHp, this);
				
				su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
				isSendStatus = true;
			}
			
			// Send a Server->Client packet StatusUpdate with MP modification to the L2PcInstance
			if (isSendStatus)
			{
				sendPacket(su);
			}
			
			// Consume Items if necessary and Send the Server->Client packet InventoryUpdate with Item modification to all the L2Character
			if (skill.getItemConsume() > 0)
			{
				consumeItem(skill.getItemConsumeId(), skill.getItemConsume());
			}
			
			// Launch the magic skill in order to calculate its effects
			callSkill(skill, targets, crit);
		}
		catch (final NullPointerException e)
		{
		}
		
		if (instant || (coolTime == 0))
		{
			onMagicFinalizer(targets, skill);
		}
		else
		{
			_skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, false, 3), coolTime);
		}
	}
	
	/*
	 * Runs after skill hitTime+coolTime
	 */
	public void onMagicFinalizer(L2Object[] targets, L2Skill skill)
	{
		_skillCast = null;
		
		_castInterruptTime = 0;
		enableAllSkills();
		
		// If the skill type is PDAM or DRAIN_SOUL, notify the AI of the target with AI_INTENTION_ATTACK
		if ((getAI().getNextIntention() == null) && ((skill.getSkillType() == SkillType.PDAM) || (skill.getSkillType() == SkillType.FATAL) || (skill.getSkillType() == SkillType.BLOW) || (skill.getSkillType() == SkillType.CHARGEDAM) || (skill.getSkillType() == SkillType.STUN) || (skill.getSkillType() == SkillType.DRAIN_SOUL) || (skill.getSkillType() == SkillType.SPOIL) || (skill.getSkillType() == SkillType.SOW) || ((this instanceof L2Summon) && skill.isOffensive())))
		{
			
			if ((getTarget() instanceof L2Character) && (getTarget() != this) && (targets[0] == getTarget()))
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getTarget());
			}
		}
		
		if (skill.isOffensive() && (skill.getSkillType() != SkillType.UNLOCK) && (skill.getSkillType() != SkillType.DELUXE_KEY_UNLOCK))
		{
			getAI().clientStartAutoAttack();
		}
		
		// Notify the AI of the L2Character with EVT_FINISH_CASTING
		getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING);
		
		notifyQuestEventSkillFinished(skill, targets[0]);
		
		/*
		 * If character is a player, then wipe their current cast state and check if a skill is queued. If there is a queued skill, launch it and wipe the queue.
		 */
		if (this instanceof L2PcInstance)
		{
			final L2PcInstance currPlayer = (L2PcInstance) this;
			final SkillDat queuedSkill = currPlayer.getQueuedSkill();
			
			currPlayer.setCurrentSkill(null, false, false);
			
			if (queuedSkill != null)
			{
				currPlayer.setQueuedSkill(null, false, false);
				ThreadPoolManager.getInstance().executeTask(new QueuedMagicUseTask(currPlayer, queuedSkill.getSkill(), queuedSkill.isCtrlPressed(), queuedSkill.isShiftPressed()));
			}
		}
	}
	
	/**
	 * Reduce the item number of the L2Character.<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance</li><BR>
	 * <BR>
	 * @param itemConsumeId
	 * @param itemCount
	 */
	public void consumeItem(int itemConsumeId, int itemCount)
	{
	}
	
	// Quest event ON_SPELL_FNISHED
	protected void notifyQuestEventSkillFinished(L2Skill skill, L2Object target)
	{
		// do nothing here
	}
	
	/**
	 * Enable a skill (remove it from _disabledSkills of the L2Character).<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All skills disabled are identified by their skillId in <B>_disabledSkills</B> of the L2Character <BR>
	 * <BR>
	 * @param skillId The identifier of the L2Skill to enable
	 */
	public void enableSkill(int skillId)
	{
		if (_disabledSkills == null)
		{
			return;
		}
		
		_disabledSkills.remove(new Integer(skillId));
		
		if (this instanceof L2PcInstance)
		{
			removeTimeStamp(skillId);
		}
		
	}
	
	/**
	 * Disable a skill (add it to _disabledSkills of the L2Character).<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All skills disabled are identified by their skillId in <B>_disabledSkills</B> of the L2Character <BR>
	 * <BR>
	 * @param skillId The identifier of the L2Skill to disable
	 */
	public void disableSkill(int skillId)
	{
		if (_disabledSkills == null)
		{
			_disabledSkills = Collections.synchronizedList(new FastList<Integer>());
		}
		
		_disabledSkills.add(skillId);
	}
	
	/**
	 * Disable this skill id for the duration of the delay in milliseconds.
	 * @param skillId
	 * @param delay (seconds * 1000)
	 */
	public void disableSkill(int skillId, long delay)
	{
		disableSkill(skillId);
		if (delay > 10)
		{
			ThreadPoolManager.getInstance().scheduleAi(new EnableSkill(skillId), delay);
		}
	}
	
	/**
	 * Check if a skill is disabled.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All skills disabled are identified by their skillId in <B>_disabledSkills</B> of the L2Character <BR>
	 * <BR>
	 * @param skillId The identifier of the L2Skill to disable
	 * @param isAllDisabled
	 * @return
	 */
	public boolean isSkillDisabled(int skillId, boolean isAllDisabled)
	{
		if (isAllDisabled)
		{
			return true;
		}
		
		if (_disabledSkills == null)
		{
			return false;
		}
		
		return _disabledSkills.contains(skillId);
	}
	
	public boolean isSkillDisabled(int skillId)
	{
		return isSkillDisabled(skillId, isAllSkillsDisabled());
	}
	
	/**
	 * Disable all skills (set _allSkillsDisabled to True).<BR>
	 * <BR>
	 */
	public void disableAllSkills()
	{
		if (Config.DEBUG)
		{
			_log.fine("all skills disabled");
		}
		_allSkillsDisabled = true;
	}
	
	/**
	 * Enable all skills (set _allSkillsDisabled to False).<BR>
	 * <BR>
	 */
	public void enableAllSkills()
	{
		if (Config.DEBUG)
		{
			_log.fine("all skills enabled");
		}
		
		_castEndTime = 0;
		_allSkillsDisabled = false;
		
	}
	
	/**
	 * Launch the magic skill and calculate its effects on each target contained in the targets table.<BR>
	 * <BR>
	 * @param skill The L2Skill to use
	 * @param targets The table of L2Object targets
	 * @param crit
	 */
	public void callSkill(L2Skill skill, L2Object[] targets, boolean crit)
	{
		try
		{
			if (skill.isToggle() && (getFirstEffect(skill.getId()) != null))
			{
				return;
			}
			
			// Do initial checkings for skills and set pvp flag/draw aggro when needed
			for (final L2Object target : targets)
			{
				if (target instanceof L2Character)
				{
					// Set some values inside target's instance for later use
					final L2Character targetPlayer = (L2Character) target;
					
					// Check Raidboss attack and
					// check buffing chars who attack raidboss. Results in mute.
					final L2Character targetsAttackTarget = targetPlayer.getAI().getAttackTarget();
					final L2Character targetsCastTarget = targetPlayer.getAI().getCastTarget();
					
					// Check Raidboss attack
					if ((targetPlayer.isRaid() && (getLevel() > (targetPlayer.getLevel() + 8))) || (!skill.isOffensive() && (targetsAttackTarget != null) && targetsAttackTarget.isRaid() && targetsAttackTarget.getAttackByList().contains(targetPlayer) // has attacked raid
						&& (getLevel() > (targetsAttackTarget.getLevel() + 8))) || (!skill.isOffensive() && (targetsCastTarget != null) && targetsCastTarget.isRaid() && targetsCastTarget.getAttackByList().contains(targetPlayer) // has attacked raid
							&& (getLevel() > (targetsCastTarget.getLevel() + 8))))
					{
						final L2Skill tempSkill = SkillTable.getInstance().getInfo(4215, 1);
						if (tempSkill != null)
						{
							tempSkill.getEffects(targetPlayer, this);
						}
						else
						{
							_log.warning("Skill 4215 at level 1 is missing in DP.");
						}
						
						return;
					}
					
					// Launch weapon Special ability skill effect if available
					final L2Weapon activeWeapon = getActiveWeaponItem();
					if ((activeWeapon != null) && !targetPlayer.isDead())
					{
						activeWeapon.getSkillEffects(this, targetPlayer, skill);
					}
					
					// Check if over-hit is possible
					if (skill.isOverhit())
					{
						
						if (target instanceof L2Attackable)
						{
							((L2Attackable) target).overhitEnabled(true);
						}
					}
					
					final L2PcInstance activeChar = getActingPlayer();
					if (activeChar != null)
					{
						if (skill.isOffensive())
						{
							if ((targetPlayer instanceof L2PcInstance) || (targetPlayer instanceof L2Summon))
							{
								if ((skill.getSkillType() != SkillType.AGGREDUCE) && (skill.getSkillType() != SkillType.AGGREDUCE_CHAR) && (skill.getSkillType() != SkillType.AGGREMOVE) && (skill.getSkillType() != SkillType.BLOW))
								{
									targetPlayer.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
								}
								
								if (!(targetPlayer instanceof L2Summon) || (activeChar.getPet() != targetPlayer))
								{
									activeChar.updatePvPStatus(targetPlayer);
								}
							}
							else if (targetPlayer instanceof L2Attackable)
							{
								switch (skill.getSkillType())
								{
									case AGGREDUCE:
									case AGGREDUCE_CHAR:
									case AGGREMOVE:
									case BLOW:
										break;
									default:
									{
										if (skill.getId() != 51)
										{
											targetPlayer.addAttackerToAttackByList(this);
										}
										
										// notify the AI that it is attacked
										targetPlayer.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
									}
								}
							}
						}
						else
						{
							if (targetPlayer instanceof L2PcInstance)
							{
								// Casting non offensive skill on player with pvp flag set or with karma
								if (!(targetPlayer.equals(this) || targetPlayer.equals(activeChar)) && ((((L2PcInstance) targetPlayer).getPvpFlag() > 0) || (((L2PcInstance) targetPlayer).getKarma() > 0)))
								{
									activeChar.updatePvPStatus();
								}
							}
							else if ((targetPlayer instanceof L2Attackable) && !(skill.getSkillType() == L2Skill.SkillType.SUMMON) && !(skill.getSkillType() == L2Skill.SkillType.BEAST_FEED) && !(skill.getSkillType() == L2Skill.SkillType.UNLOCK) && !(skill.getSkillType() == L2Skill.SkillType.DELUXE_KEY_UNLOCK)
								
								&& (!(targetPlayer instanceof L2Summon) || (activeChar.getPet() != targetPlayer)))
							{
								activeChar.updatePvPStatus();
							}
						}
					}
				}
			}
			
			// Get the skill handler corresponding to the skill type (PDAM, MDAM, SWEEP...)
			final ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
			
			// Launch the magic skill and calculate its effects
			if (handler != null)
			{
				handler.useSkill(this, skill, targets, crit);
			}
			else
			{
				skill.useSkill(this, targets);
			}
			
			// Clear soulshots and spiritshots if possible
			if (skill.useSoulShot())
			{
				final L2ItemInstance weaponInst = getActiveWeaponInstance();
				if (weaponInst != null)
				{
					switch (weaponInst.getChargedSoulshot())
					{
						case L2ItemInstance.CHARGED_SOULSHOT:
						case L2ItemInstance.CHARGED_BLESSED_SOULSHOT:
							weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
							break;
					}
				}
				// If there is no weapon equipped, check for an active summon.
				else if (this instanceof L2Summon)
				{
					final L2Summon activeSummon = (L2Summon) this;
					switch (activeSummon.getChargedSoulShot())
					{
						case L2ItemInstance.CHARGED_SOULSHOT:
						case L2ItemInstance.CHARGED_BLESSED_SOULSHOT:
							activeSummon.setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
							break;
					}
				}
			}
			else if (skill.useSpiritShot())
			{
				final L2ItemInstance weaponInst = getActiveWeaponInstance();
				if (weaponInst != null)
				{
					switch (weaponInst.getChargedSpiritshot())
					{
						case L2ItemInstance.CHARGED_SPIRITSHOT:
						case L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT:
							weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
							break;
					}
				}
				// If there is no weapon equipped, check for an active summon.
				else if (this instanceof L2Summon)
				{
					final L2Summon activeSummon = (L2Summon) this;
					switch (activeSummon.getChargedSpiritShot())
					{
						case L2ItemInstance.CHARGED_SPIRITSHOT:
						case L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT:
							activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
							break;
					}
				}
			}
			
			final L2PcInstance caster = getActingPlayer();
			if (caster != null)
			{
				for (final L2Character cha : caster.getKnownList().getKnownCharactersInRadius(1000))
				{
					if (cha == null)
					{
						continue;
					}
					
					if (cha instanceof L2NpcInstance)
					{
						final L2NpcInstance npc = (L2NpcInstance) cha;
						if (npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_SKILL_SEE) != null)
						{
							for (final Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_SKILL_SEE))
							{
								quest.notifySkillSee(npc, caster, skill, targets, this instanceof L2Summon);
							}
						}
					}
					
					// buffer hate
					if (skill.hasAggro())
					{
						if (cha instanceof L2Attackable)
						{
							final L2Attackable mob = (L2Attackable) cha;
							if (mob.hasAI() && (mob.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK))
							{
								mob.seeSpell(caster, targets, skill);
							}
						}
					}
				}
			}
		}
		catch (final Exception e)
		{
			_log.log(Level.WARNING, "", e);
		}
	}
	
	/**
	 * Return True if the L2Character is behind the target and can't be seen.<BR>
	 * <BR>
	 * @param target
	 * @return
	 */
	public boolean isBehind(L2Object target)
	{
		double angleChar, angleTarget, angleDiff;
		final double maxAngleDiff = 45;
		
		if (target == null)
		{
			return false;
		}
		
		if (target instanceof L2Character)
		{
			final L2Character target1 = (L2Character) target;
			angleChar = Util.calculateAngleFrom(this, target1);
			angleTarget = Util.convertHeadingToDegree(target1.getHeading());
			angleDiff = angleChar - angleTarget;
			if (angleDiff <= (-360 + maxAngleDiff))
			{
				angleDiff += 360;
			}
			if (angleDiff >= (360 - maxAngleDiff))
			{
				angleDiff -= 360;
			}
			if (Math.abs(angleDiff) <= maxAngleDiff)
			
			{
				if (Config.DEBUG)
				{
					_log.info("Char " + getName() + " is behind " + target.getName());
				}
				
				return true;
			}
		}
		else
		{
			_log.fine("isBehindTarget's target not an L2 Character.");
		}
		return false;
	}
	
	public boolean isBehindTarget()
	{
		return isBehind(getTarget());
	}
	
	/**
	 * Return True if the target is facing the L2Character.<BR>
	 * <BR>
	 * @param target
	 * @return
	 */
	public boolean isInFrontOf(L2Character target)
	{
		double angleChar, angleTarget, angleDiff;
		final double maxAngleDiff = 45;
		if (target == null)
		{
			return false;
		}
		
		angleTarget = Util.calculateAngleFrom(target, this);
		angleChar = Util.convertHeadingToDegree(target.getHeading());
		angleDiff = angleChar - angleTarget;
		if (angleDiff <= (-360 + maxAngleDiff))
		{
			angleDiff += 360;
		}
		if (angleDiff >= (360 - maxAngleDiff))
		{
			angleDiff -= 360;
		}
		if (Math.abs(angleDiff) <= maxAngleDiff)
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Returns true if target is in front of L2Character (shield def etc)
	 * @param target
	 * @param maxAngle
	 * @return
	 */
	public boolean isFacing(L2Object target, int maxAngle)
	{
		double angleChar, angleTarget, angleDiff, maxAngleDiff;
		if (target == null)
		{
			return false;
		}
		
		maxAngleDiff = maxAngle / 2;
		angleTarget = Util.calculateAngleFrom(this, target);
		angleChar = Util.convertHeadingToDegree(getHeading());
		angleDiff = angleChar - angleTarget;
		if (angleDiff <= (-360 + maxAngleDiff))
		{
			angleDiff += 360;
		}
		if (angleDiff >= (360 - maxAngleDiff))
		{
			angleDiff -= 360;
		}
		if (Math.abs(angleDiff) <= maxAngleDiff)
		{
			return true;
		}
		return false;
	}
	
	public boolean isInFrontOfTarget()
	{
		final L2Object target = getTarget();
		if (target instanceof L2Character)
		{
			return isInFrontOf((L2Character) target);
		}
		return false;
	}
	
	/**
	 * Gets the condition bonus for hit miss.
	 * @param target the attacked character.
	 * @return the bonus of the attacker against the target.
	 */
	public double getHitMissConditionBonus(L2Character target)
	{
		double mod = 100;
		
		// Get high or low bonus
		if ((getZ() - target.getZ()) > 50)
		{
			mod += 3;
		}
		else if ((getZ() - target.getZ()) < -50)
		{
			mod -= 3;
		}
		
		// Get weather bonus
		if (GameTimeController.getInstance().isNowNight())
		{
			mod -= 10;
		}
		
		// Get side bonus
		if (isBehindTarget())
		{
			mod += 10;
		}
		else if (!isInFrontOfTarget())
		{
			mod += 5;
		}
		
		// If (mod / 100) is less than 0, return 0, because we can't lower more than 100%.
		return Math.max(mod / 100, 0);
	}
	
	/**
	 * Return 1.<BR>
	 * <BR>
	 * @return
	 */
	public double getLevelMod()
	{
		return 1;
	}
	
	public final void setSkillCast(Future<?> newSkillCast)
	{
		_skillCast = newSkillCast;
	}
	
	public final void setSkillCastEndTime(int newSkillCastEndTime)
	{
		_castEndTime = newSkillCastEndTime;
		// for interrupt -12 ticks; first removing the extra second and then -200 ms
		_castInterruptTime = newSkillCastEndTime - 12;
	}
	
	private Future<?> _PvPRegTask;
	
	private long _pvpFlagLasts;
	
	public void setPvpFlagLasts(long time)
	{
		_pvpFlagLasts = time;
	}
	
	public long getPvpFlagLasts()
	{
		return _pvpFlagLasts;
	}
	
	public void startPvPFlag()
	{
		updatePvPFlag(1);
		
		_PvPRegTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new PvPFlag(), 1000, 1000);
	}
	
	public void stopPvPFlag()
	{
		if (_PvPRegTask != null)
		{
			_PvPRegTask.cancel(false);
		}
		
		updatePvPFlag(0);
		
		_PvPRegTask = null;
	}
	
	public void updatePvPFlag(int value)
	{
		// Overriden in L2PcInstance
	}
	
	/**
	 * Return a Random Damage in function of the weapon.<BR>
	 * <BR>
	 * @param target
	 * @return
	 */
	public final int getRandomDamage(L2Character target)
	{
		final L2Weapon weaponItem = getActiveWeaponItem();
		
		if (weaponItem == null)
		{
			return 5 + (int) Math.sqrt(getLevel());
		}
		
		return weaponItem.getRandomDamage();
	}
	
	@Override
	public String toString()
	{
		return "mob " + getObjectId();
	}
	
	public int getAttackEndTime()
	{
		return _attackEndTime;
	}
	
	/**
	 * Not Implemented.<BR>
	 * <BR>
	 * @return
	 */
	public abstract int getLevel();
	// =========================================================
	
	// =========================================================
	// Stat - NEED TO REMOVE ONCE L2CHARSTAT IS COMPLETE
	// Property - Public
	public final double calcStat(Stats stat, double init, L2Character target, L2Skill skill)
	{
		return getStat().calcStat(stat, init, target, skill);
	}
	
	// Property - Public
	public int getAccuracy()
	{
		return getStat().getAccuracy();
	}
	
	public final float getAttackSpeedMultiplier()
	{
		return getStat().getAttackSpeedMultiplier();
	}
	
	public int getCON()
	{
		return getStat().getCON();
	}
	
	public int getDEX()
	{
		return getStat().getDEX();
	}
	
	public final double getCriticalDmg(L2Character target, double init)
	{
		return getStat().getCriticalDmg(target, init);
	}
	
	public int getCriticalHit(L2Character target, L2Skill skill)
	{
		return getStat().getCriticalHit(target, skill);
	}
	
	public int getEvasionRate(L2Character target)
	{
		return getStat().getEvasionRate(target);
	}
	
	public int getINT()
	{
		return getStat().getINT();
	}
	
	public final int getMagicalAttackRange(L2Skill skill)
	{
		return getStat().getMagicalAttackRange(skill);
	}
	
	public final int getMaxCp()
	{
		return getStat().getMaxCp();
	}
	
	public int getMAtk(L2Character target, L2Skill skill)
	{
		return getStat().getMAtk(target, skill);
	}
	
	public int getMAtkSpd()
	{
		return getStat().getMAtkSpd();
	}
	
	public int getMaxMp()
	{
		return getStat().getMaxMp();
	}
	
	public int getMaxHp()
	{
		return getStat().getMaxHp();
	}
	
	public final int getMCriticalHit(L2Character target, L2Skill skill)
	{
		return getStat().getMCriticalHit(target, skill);
	}
	
	public int getMDef(L2Character target, L2Skill skill)
	{
		return getStat().getMDef(target, skill);
	}
	
	public int getMEN()
	{
		return getStat().getMEN();
	}
	
	public double getMReuseRate(L2Skill skill)
	{
		return getStat().getMReuseRate(skill);
	}
	
	public float getMovementSpeedMultiplier()
	{
		return getStat().getMovementSpeedMultiplier();
	}
	
	public int getPAtk(L2Character target)
	{
		return getStat().getPAtk(target);
	}
	
	public double getPAtkAnimals(L2Character target)
	{
		return getStat().getPAtkAnimals(target);
	}
	
	public double getPAtkDragons(L2Character target)
	{
		return getStat().getPAtkDragons(target);
	}
	
	public double getPAtkInsects(L2Character target)
	{
		return getStat().getPAtkInsects(target);
	}
	
	public double getPAtkMonsters(L2Character target)
	{
		return getStat().getPAtkMonsters(target);
	}
	
	public double getPAtkPlants(L2Character target)
	{
		return getStat().getPAtkPlants(target);
	}
	
	public double getPAtkGiants(L2Character target)
	{
		return getStat().getPAtkGiants(target);
	}
	
	public double getPAtkMCreatures(L2Character target)
	{
		return getStat().getPAtkMCreatures(target);
	}
	
	public double getPAtkUndead(L2Character target)
	{
		return getStat().getPAtkUndead(target);
	}
	
	public double getPDefUndead(L2Character target)
	{
		return getStat().getPDefUndead(target);
	}
	
	public int getPAtkSpd()
	{
		return getStat().getPAtkSpd();
	}
	
	public int getPDef(L2Character target)
	{
		return getStat().getPDef(target);
	}
	
	public final int getPhysicalAttackRange()
	{
		return getStat().getPhysicalAttackRange();
	}
	
	public int getRunSpeed()
	{
		return getStat().getRunSpeed();
	}
	
	public final int getShldDef()
	{
		return getStat().getShldDef();
	}
	
	public int getSTR()
	{
		return getStat().getSTR();
	}
	
	public final int getWalkSpeed()
	{
		return getStat().getWalkSpeed();
	}
	
	public int getWIT()
	{
		return getStat().getWIT();
	}
	// =========================================================
	
	// =========================================================
	// Status - NEED TO REMOVE ONCE L2CHARTATUS IS COMPLETE
	// Method - Public
	public void addStatusListener(L2Character object)
	{
		getStatus().addStatusListener(object);
	}
	
	public void reduceCurrentHp(double i, L2Character attacker)
	{
		reduceCurrentHp(i, attacker, true);
	}
	
	public void reduceCurrentHp(double i, L2Character attacker, boolean awake)
	{
		if (Config.CHAMPION_ENABLE && isChampion() && (Config.CHAMPION_HP != 0))
		{
			getStatus().reduceHp(i / Config.CHAMPION_HP, attacker, awake);
		}
		else
		{
			getStatus().reduceHp(i, attacker, awake);
		}
	}
	
	public void reduceCurrentMp(double i)
	{
		getStatus().reduceMp(i);
	}
	
	public void removeStatusListener(L2Character object)
	{
		getStatus().removeStatusListener(object);
	}
	
	protected void stopHpMpRegeneration()
	{
		getStatus().stopHpMpRegeneration();
	}
	
	// Property - Public
	public final double getCurrentCp()
	{
		return getStatus().getCurrentCp();
	}
	
	public final void setCurrentCp(Double newCp)
	{
		setCurrentCp((double) newCp);
	}
	
	public final void setCurrentCp(double newCp)
	{
		getStatus().setCurrentCp(newCp);
	}
	
	public final double getCurrentHp()
	{
		return getStatus().getCurrentHp();
	}
	
	public final void setCurrentHp(double newHp)
	{
		getStatus().setCurrentHp(newHp);
	}
	
	public final void setCurrentHpMp(double newHp, double newMp)
	{
		getStatus().setCurrentHpMp(newHp, newMp);
	}
	
	public final double getCurrentMp()
	{
		return getStatus().getCurrentMp();
	}
	
	public final void setCurrentMp(Double newMp)
	{
		setCurrentMp((double) newMp);
	}
	
	public final void setCurrentMp(double newMp)
	{
		getStatus().setCurrentMp(newMp);
	}
	// =========================================================
	
	public void setAiClass(String aiClass)
	{
		_aiClass = aiClass;
	}
	
	public String getAiClass()
	{
		return _aiClass;
	}
	
	public L2Character getLastBuffer()
	{
		return _LastBuffer;
	}
	
	public void setChampion(boolean champ)
	{
		_champion = champ;
	}
	
	public boolean isChampion()
	{
		return _champion;
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
	
	public int getLastHealAmount()
	{
		return _LastHealAmount;
	}
	
	public void setLastBuffer(L2Character buffer)
	{
		_LastBuffer = buffer;
	}
	
	public void setLastHealAmount(int hp)
	{
		_LastHealAmount = hp;
	}
	
	public void sendMessage(String message)
	{
		
		// Overriden in L2PcInstance
	}
	
	/**
	 * Send system message about damage.<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2PcInstance
	 * <li>L2SummonInstance
	 * <li>L2PetInstance</li><BR>
	 * <BR>
	 * @param target
	 * @param damage
	 * @param mcrit
	 * @param pcrit
	 * @param miss
	 */
	public void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{
	}
	
	public void disableCoreAI(boolean val)
	{
		_AIdisabled = val;
	}
	
	public boolean isCoreAIDisabled()
	{
		return _AIdisabled;
	}
	
	public boolean isAIOBuffer()
	{
		return false;
	}
}