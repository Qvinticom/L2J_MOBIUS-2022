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
package com.l2jmobius.gameserver.model.actor.instance;

import static com.l2jmobius.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;

import java.text.DateFormat;
import java.util.List;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.Olympiad;
import com.l2jmobius.gameserver.SevenSigns;
import com.l2jmobius.gameserver.SevenSignsFestival;
import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.TradeController;
import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.cache.HtmCache;
import com.l2jmobius.gameserver.datatables.ClanTable;
import com.l2jmobius.gameserver.datatables.HelperBuffTable;
import com.l2jmobius.gameserver.datatables.ItemTable;
import com.l2jmobius.gameserver.datatables.SkillTable;
import com.l2jmobius.gameserver.datatables.SpawnTable;
import com.l2jmobius.gameserver.idfactory.IdFactory;
import com.l2jmobius.gameserver.instancemanager.CastleManager;
import com.l2jmobius.gameserver.instancemanager.DimensionalRiftManager;
import com.l2jmobius.gameserver.instancemanager.QuestManager;
import com.l2jmobius.gameserver.instancemanager.TownManager;
import com.l2jmobius.gameserver.instancemanager.games.Lottery;
import com.l2jmobius.gameserver.model.L2Attackable;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2Clan;
import com.l2jmobius.gameserver.model.L2DropCategory;
import com.l2jmobius.gameserver.model.L2DropData;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.L2Multisell;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.L2Skill.SkillType;
import com.l2jmobius.gameserver.model.L2Spawn;
import com.l2jmobius.gameserver.model.L2TradeList;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.L2WorldRegion;
import com.l2jmobius.gameserver.model.MobGroupTable;
import com.l2jmobius.gameserver.model.NpcInventory;
import com.l2jmobius.gameserver.model.actor.knownlist.NpcKnownList;
import com.l2jmobius.gameserver.model.actor.stat.NpcStat;
import com.l2jmobius.gameserver.model.actor.status.NpcStatus;
import com.l2jmobius.gameserver.model.entity.Castle;
import com.l2jmobius.gameserver.model.entity.L2Event;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.model.zone.type.L2TownZone;
import com.l2jmobius.gameserver.network.L2GameClient;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.BuyList;
import com.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jmobius.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jmobius.gameserver.network.serverpackets.NpcInfo;
import com.l2jmobius.gameserver.network.serverpackets.RadarControl;
import com.l2jmobius.gameserver.network.serverpackets.ServerObjectInfo;
import com.l2jmobius.gameserver.network.serverpackets.SocialAction;
import com.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.network.serverpackets.ValidateLocation;
import com.l2jmobius.gameserver.skills.Stats;
import com.l2jmobius.gameserver.taskmanager.DecayTaskManager;
import com.l2jmobius.gameserver.templates.L2HelperBuff;
import com.l2jmobius.gameserver.templates.L2Item;
import com.l2jmobius.gameserver.templates.L2NpcTemplate;
import com.l2jmobius.gameserver.templates.L2Weapon;
import com.l2jmobius.util.Rnd;

import javolution.text.TextBuilder;
import javolution.util.FastList;

/**
 * This class represents a Non-Player-Character in the world. It can be a monster or a friendly character. It also uses a template to fetch some static values. The templates are hardcoded in the client, so we can rely on them.<BR>
 * <BR>
 * L2Character :<BR>
 * <BR>
 * <li>L2Attackable</li>
 * <li>L2BoxInstance</li>
 * <li>L2FolkInstance</li>
 * @version $Revision: 1.32.2.7.2.24 $ $Date: 2005/04/11 10:06:09 $
 */
public class L2NpcInstance extends L2Character
{
	// private static Logger _log = Logger.getLogger(L2NpcInstance.class.getName());
	
	/** The interaction distance of the L2NpcInstance(is used as offset in MovetoLocation method) */
	public static final int INTERACTION_DISTANCE = 150;
	
	/** The L2Spawn object that manage this L2NpcInstance */
	private L2Spawn _spawn;
	
	private NpcInventory _inventory = null;
	
	/** The flag to specify if this L2NpcInstance is busy */
	private boolean _IsBusy = false;
	
	/** The busy message for this L2NpcInstance */
	private String _BusyMessage = "";
	
	/** True if endDecayTask has already been called */
	volatile boolean _isDecayed = false;
	
	/** True if a Dwarf has used Spoil on this L2NpcInstance */
	private boolean _IsSpoil = false;
	
	/** The castle index in the array of L2Castle this L2NpcInstance belongs to */
	private int _castleIndex = -2;
	
	public boolean isEventMob = false;
	
	private boolean _isInTown = false;
	
	private int _isSpoiledBy = 0;
	
	protected RandomAnimationTask _rAniTask = null;
	
	private double _currentCollisionHeight;
	private double _currentCollisionRadius;
	
	private int _maxSiegeHp = 0;
	
	/** Task launching the function onRandomAnimation() */
	protected class RandomAnimationTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				if (this != _rAniTask)
				{
					return;
				}
				
				if (L2NpcInstance.this instanceof L2Attackable)
				{
					if (getAI().getIntention() != AI_INTENTION_ACTIVE)
					{
						return;
					}
				}
				
				else
				{
					if (!isInActiveRegion())
					{
						return;
					}
				}
				
				if (!(isDead() || isStunned() || isSleeping() || isParalyzed()))
				{
					onRandomAnimation();
				}
				
				startRandomAnimationTimer();
			}
			catch (final Throwable t)
			{
			}
		}
	}
	
	/**
	 * Send a packet SocialAction to all L2PcInstance in the _KnownPlayers of the L2NpcInstance and create a new RandomAnimation Task.<BR>
	 * <BR>
	 */
	public void onRandomAnimation()
	{
		// Send a packet SocialAction to all L2PcInstance in the _KnownPlayers of the L2NpcInstance
		final SocialAction sa = new SocialAction(getObjectId(), Rnd.get(1, 3));
		broadcastPacket(sa);
	}
	
	/**
	 * Create a RandomAnimation Task that will be launched after the calculated delay.<BR>
	 * <BR>
	 */
	public void startRandomAnimationTimer()
	{
		if (!hasRandomAnimation())
		{
			return;
		}
		
		final int minWait = this instanceof L2Attackable ? Config.MIN_MONSTER_ANIMATION : Config.MIN_NPC_ANIMATION;
		final int maxWait = this instanceof L2Attackable ? Config.MAX_MONSTER_ANIMATION : Config.MAX_NPC_ANIMATION;
		
		// Calculate the delay before the next animation
		final int interval = Rnd.get(minWait, maxWait) * 1000;
		
		// Create a RandomAnimation Task that will be launched after the calculated delay
		_rAniTask = new RandomAnimationTask();
		ThreadPoolManager.getInstance().scheduleGeneral(_rAniTask, interval);
	}
	
	/**
	 * Check if the server allows Random Animation.<BR>
	 * <BR>
	 * @return
	 */
	public boolean hasRandomAnimation()
	{
		return (Config.MAX_NPC_ANIMATION > 0);
	}
	
	/**
	 * Constructor of L2NpcInstance (use L2Character constructor).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Call the L2Character constructor to set the _template of the L2Character (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR)</li>
	 * <li>Set the name of the L2Character</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it</li><BR>
	 * <BR>
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2NpcTemplate to apply to the NPC
	 */
	public L2NpcInstance(int objectId, L2NpcTemplate template)
	{
		// Call the L2Character constructor to set the _template of the L2Character, copy skills from template to object
		// and link _calculators to NPC_STD_CALCULATOR
		super(objectId, template);
		
		if (template == null)
		{
			_log.severe("No template for Npc. Please check your datapack is setup correctly.");
			return;
		}
		
		getKnownList();
		getStat();
		getStatus();
		
		super.initCharStatusUpdateValues(); // init status update values
		
		_currentCollisionHeight = getTemplate().collisionHeight;
		_currentCollisionRadius = getTemplate().collisionRadius;
		
		// Set the name of the L2Character
		setName(template.name);
		
		if (((template.ss > 0) || (template.bss > 0)) && (template.ssRate > 0))
		{
			_inventory = new NpcInventory(this);
		}
	}
	
	@Override
	public NpcKnownList getKnownList()
	{
		if ((super.getKnownList() == null) || !(super.getKnownList() instanceof NpcKnownList))
		{
			setKnownList(new NpcKnownList(this));
		}
		return (NpcKnownList) super.getKnownList();
	}
	
	@Override
	public NpcStat getStat()
	{
		if ((super.getStat() == null) || !(super.getStat() instanceof NpcStat))
		{
			setStat(new NpcStat(this));
		}
		return (NpcStat) super.getStat();
	}
	
	@Override
	public NpcStatus getStatus()
	{
		if ((super.getStatus() == null) || !(super.getStatus() instanceof NpcStatus))
		{
			setStatus(new NpcStatus(this));
		}
		return (NpcStatus) super.getStatus();
	}
	
	/** Return the L2NpcTemplate of the L2NpcInstance. */
	@Override
	public final L2NpcTemplate getTemplate()
	{
		return (L2NpcTemplate) super.getTemplate();
	}
	
	/**
	 * Return the generic Identifier of this L2NpcInstance contained in the L2NpcTemplate.<BR>
	 * <BR>
	 * @return
	 */
	public int getNpcId()
	{
		return getTemplate().npcId;
	}
	
	@Override
	public boolean isAttackable()
	{
		return true;
	}
	
	/**
	 * Return the faction Identifier of this L2NpcInstance contained in the L2NpcTemplate.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * If a NPC belows to a Faction, other NPC of the faction inside the Faction range will help it if it's attacked<BR>
	 * <BR>
	 * @return
	 */
	public final String getFactionId()
	{
		return getTemplate().factionId;
	}
	
	/**
	 * Return the Level of this L2NpcInstance contained in the L2NpcTemplate.<BR>
	 * <BR>
	 */
	@Override
	public final int getLevel()
	{
		return getTemplate().level;
	}
	
	/**
	 * Return True if the L2NpcInstance is agressive (ex : L2MonsterInstance in function of aggroRange).<BR>
	 * <BR>
	 * @return
	 */
	public boolean isAggressive()
	{
		return false;
	}
	
	/**
	 * Return the Aggro Range of this L2NpcInstance contained in the L2NpcTemplate.<BR>
	 * <BR>
	 * @return
	 */
	public int getAggroRange()
	{
		return getTemplate().aggroRange;
	}
	
	/**
	 * Return the Faction Range of this L2NpcInstance contained in the L2NpcTemplate.<BR>
	 * <BR>
	 * @return
	 */
	public int getFactionRange()
	{
		return getTemplate().factionRange;
	}
	
	/**
	 * Return True if this L2NpcInstance is undead in function of the L2NpcTemplate.<BR>
	 * <BR>
	 */
	@Override
	public boolean isUndead()
	{
		return getTemplate().isUndead;
	}
	
	/**
	 * Return True if this L2NpcInstance is quest monster in function of the L2NpcTemplate.<BR>
	 * <BR>
	 * @return
	 */
	public boolean isQuestMonster()
	{
		return getTemplate().isQuestMonster;
	}
	
	/**
	 * Send a packet NpcInfo with state of abnormal effect to all L2PcInstance in the _KnownPlayers of the L2NpcInstance.<BR>
	 * <BR>
	 */
	@Override
	public void updateAbnormalEffect()
	{
		// Send a Server->Client packet NpcInfo with state of abnormal effect to all L2PcInstance in the _KnownPlayers of the L2NpcInstance
		for (final L2PcInstance player : getKnownList().getKnownPlayers().values())
		{
			if (player != null)
			{
				if (getRunSpeed() == 0)
				{
					player.sendPacket(new ServerObjectInfo(this, player));
				}
				else
				{
					player.sendPacket(new NpcInfo(this, player));
				}
			}
		}
	}
	
	/**
	 * Return the distance under which the object must be add to _knownObject in function of the object type.<BR>
	 * <BR>
	 * <B><U> Values </U> :</B><BR>
	 * <BR>
	 * <li>object is a L2FolkInstance : 0 (don't remember it)</li>
	 * <li>object is a L2Character : 0 (don't remember it)</li>
	 * <li>object is a L2PlayableInstance : 1500</li>
	 * <li>others : 500</li><BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2Attackable</li><BR>
	 * <BR>
	 * @param object The Object to add to _knownObject
	 * @return
	 */
	public int getDistanceToWatchObject(L2Object object)
	{
		if (object instanceof L2FestivalGuideInstance)
		{
			return 10000;
		}
		
		if ((object instanceof L2FolkInstance) || !(object instanceof L2Character))
		{
			return 0;
		}
		
		if (object instanceof L2PlayableInstance)
		{
			return 1500;
		}
		
		return 500;
	}
	
	/**
	 * Return the distance after which the object must be remove from _knownObject in function of the object type.<BR>
	 * <BR>
	 * <B><U> Values </U> :</B><BR>
	 * <BR>
	 * <li>object is not a L2Character : 0 (don't remember it)</li>
	 * <li>object is a L2FolkInstance : 0 (don't remember it)</li>
	 * <li>object is a L2PlayableInstance : 3000</li>
	 * <li>others : 1000</li><BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2Attackable</li><BR>
	 * <BR>
	 * @param object The Object to remove from _knownObject
	 * @return
	 */
	public int getDistanceToForgetObject(L2Object object)
	{
		return 2 * getDistanceToWatchObject(object);
	}
	
	/**
	 * Return False.<BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2MonsterInstance : Check if the attacker is not another L2MonsterInstance</li>
	 * <li>L2PcInstance</li><BR>
	 * <BR>
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}
	
	/**
	 * Return the Identifier of the item in the left hand of this L2NpcInstance contained in the L2NpcTemplate.<BR>
	 * <BR>
	 * @return
	 */
	public int getLeftHandItem()
	{
		return getTemplate().lhand;
	}
	
	/**
	 * Return the Identifier of the item in the right hand of this L2NpcInstance contained in the L2NpcTemplate.<BR>
	 * <BR>
	 * @return
	 */
	public int getRightHandItem()
	{
		return getTemplate().rhand;
	}
	
	/**
	 * Return True if this L2NpcInstance has drops that can be sweeped.<BR>
	 * <BR>
	 * @return
	 */
	public boolean isSpoil()
	{
		return _IsSpoil;
	}
	
	/**
	 * Set the spoil state of this L2NpcInstance.<BR>
	 * <BR>
	 * @param isSpoil
	 */
	public void setSpoil(boolean isSpoil)
	{
		_IsSpoil = isSpoil;
	}
	
	public final int getIsSpoiledBy()
	{
		return _isSpoiledBy;
	}
	
	public final void setIsSpoiledBy(int value)
	{
		_isSpoiledBy = value;
	}
	
	/**
	 * Return the busy status of this L2NpcInstance.<BR>
	 * <BR>
	 * @return
	 */
	public final boolean isBusy()
	{
		return _IsBusy;
	}
	
	/**
	 * Set the busy status of this L2NpcInstance.<BR>
	 * <BR>
	 * @param isBusy
	 */
	public void setBusy(boolean isBusy)
	{
		_IsBusy = isBusy;
	}
	
	/**
	 * Return the busy message of this L2NpcInstance.<BR>
	 * <BR>
	 * @return
	 */
	public final String getBusyMessage()
	{
		return _BusyMessage;
	}
	
	/**
	 * Set the busy message of this L2NpcInstance.<BR>
	 * <BR>
	 * @param message
	 */
	public void setBusyMessage(String message)
	{
		_BusyMessage = message;
	}
	
	protected boolean canTarget(L2PcInstance player)
	{
		if (player.isOutOfControl())
		{
			player.sendPacket(new ActionFailed());
			return false;
		}
		
		if (player.getEventTeam() > 0)
		{
			return false;
		}
		
		// TODO: More checks...
		
		return true;
	}
	
	public boolean canInteract(L2PcInstance player)
	{
		// TODO: NPC busy check etc...
		
		if (player.isCastingNow() || player.isSitting())
		{
			return false;
		}
		
		if (player.isDead() || player.isFakeDeath())
		{
			return false;
		}
		
		if (player.getPrivateStoreType() != 0)
		{
			return false;
		}
		
		if (!isInsideRadius(player, INTERACTION_DISTANCE, false, false))
		{
			return false;
		}
		
		if (player.getEventTeam() > 0)
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * Manage actions when a player click on the L2NpcInstance.<BR>
	 * <BR>
	 * <B><U> Actions on first click on the L2NpcInstance (Select it)</U> :</B><BR>
	 * <BR>
	 * <li>Set the L2NpcInstance as target of the L2PcInstance player (if necessary)</li>
	 * <li>Send a Server->Client packet MyTargetSelected to the L2PcInstance player (display the select window)</li>
	 * <li>If L2NpcInstance is autoAttackable, send a Server->Client packet StatusUpdate to the L2PcInstance in order to update L2NpcInstance HP bar</li>
	 * <li>Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client</li><BR>
	 * <BR>
	 * <B><U> Actions on second click on the L2NpcInstance (Attack it/Intercat with it)</U> :</B><BR>
	 * <BR>
	 * <li>Send a Server->Client packet MyTargetSelected to the L2PcInstance player (display the select window)</li>
	 * <li>If L2NpcInstance is autoAttackable, notify the L2PcInstance AI with AI_INTENTION_ATTACK (after a height verification)</li>
	 * <li>If L2NpcInstance is NOT autoAttackable, notify the L2PcInstance AI with AI_INTENTION_INTERACT (after a distance verification) and show message</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Each group of Server->Client packet must be terminated by a ActionFailed packet in order to avoid that client wait an other packet</B></FONT><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Client packet : Action, AttackRequest</li><BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2ArtefactInstance : Manage only fisrt click to select Artefact</li><BR>
	 * <BR>
	 * <li>L2GuardInstance :</li><BR>
	 * <BR>
	 * @param player The L2PcInstance that start an action on the L2NpcInstance
	 */
	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player))
		{
			return;
		}
		
		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			if (Config.DEBUG)
			{
				_log.fine("new target selected:" + getObjectId());
			}
			
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			
			// Check if the player is attackable (without a forced attack)
			if (isAutoAttackable(player))
			{
				// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
				// The player.getLevel() - getLevel() permit to display the correct color in the select window
				final MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
				player.sendPacket(my);
				
				// Send a Server->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
				final StatusUpdate su = new StatusUpdate(getObjectId());
				su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
				su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
				player.sendPacket(su);
			}
			else
			{
				// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
				final MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
				player.sendPacket(my);
			}
			
			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			player.sendPacket(new ValidateLocation(this));
			// Check if the player is attackable (without a forced attack) and isn't dead
			if (isAutoAttackable(player) && !isAlikeDead())
			{
				// Check the height difference
				if (Math.abs(player.getZ() - getZ()) < 400) // this max heigth difference might need some tweaking
				{
					// Set the L2PcInstance Intention to AI_INTENTION_ATTACK
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
					
				}
				else
				{
					// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
					player.sendPacket(new ActionFailed());
				}
			}
			else if (!isAutoAttackable(player))
			{
				// Calculate the distance between the L2PcInstance and the L2NpcInstance
				if (!canInteract(player))
				{
					// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				}
				else
				{
					// Send a Server->Client packet SocialAction to the all L2PcInstance on the _knownPlayer of the L2NpcInstance
					// to display a social action of the L2NpcInstance on their client
					final SocialAction sa = new SocialAction(getObjectId(), Rnd.get(8));
					broadcastPacket(sa);
					
					// Open a chat window on client with the text of the L2NpcInstance
					if (isEventMob)
					{
						L2Event.showEventHtml(player, String.valueOf(getObjectId()));
					}
					else
					{
						final Quest[] qlsa = getTemplate().getEventQuests(Quest.QuestEventType.QUEST_START);
						if ((qlsa != null) && (qlsa.length > 0))
						{
							player.setLastQuestNpcObject(getObjectId());
						}
						
						final Quest[] qlst = getTemplate().getEventQuests(Quest.QuestEventType.ON_FIRST_TALK);
						if ((qlst != null) && (qlst.length == 1))
						{
							qlst[0].notifyFirstTalk(this, player);
						}
						else
						{
							showChatWindow(player, 0);
						}
					}
					
				}
			}
			else
			{
				player.sendPacket(new ActionFailed());
			}
		}
	}
	
	/**
	 * Manage and Display the GM console to modify the L2NpcInstance (GM only).<BR>
	 * <BR>
	 * <B><U> Actions (If the L2PcInstance is a GM only)</U> :</B><BR>
	 * <BR>
	 * <li>Set the L2NpcInstance as target of the L2PcInstance player (if necessary)</li>
	 * <li>Send a Server->Client packet MyTargetSelected to the L2PcInstance player (display the select window)</li>
	 * <li>If L2NpcInstance is autoAttackable, send a Server->Client packet StatusUpdate to the L2PcInstance in order to update L2NpcInstance HP bar</li>
	 * <li>Send a Server->Client NpcHtmlMessage() containing the GM console about this L2NpcInstance</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Each group of Server->Client packet must be terminated by a ActionFailed packet in order to avoid that client wait an other packet</B></FONT><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Client packet : Action</li><BR>
	 * <BR>
	 * @param client The thread that manage the player that pessed Shift and click on the L2NpcInstance
	 */
	@Override
	public void onActionShift(L2GameClient client)
	{
		// Get the L2PcInstance corresponding to the thread
		final L2PcInstance player = client.getActiveChar();
		
		if (player == null)
		{
			return;
		}
		
		if (!canTarget(player))
		{
			return;
		}
		
		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			
			// Check if the player is attackable (without a forced attack)
			if (isAutoAttackable(player))
			{
				// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
				// The player.getLevel() - getLevel() permit to display the correct color in the select window
				final MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
				player.sendPacket(my);
				
				// Send a Server->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
				final StatusUpdate su = new StatusUpdate(getObjectId());
				su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
				su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
				player.sendPacket(su);
			}
			else
			{
				// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
				final MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
				player.sendPacket(my);
			}
		}
		
		// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
		player.sendPacket(new ValidateLocation(this));
		
		// Check if the L2PcInstance is a GM
		if (player.getAccessLevel() >= Config.GM_ACCESSLEVEL)
		{
			// Send a Server->Client NpcHtmlMessage() containing the GM console about this L2NpcInstance
			final NpcHtmlMessage html = new NpcHtmlMessage(0);
			final TextBuilder html1 = new TextBuilder("<html><body><center><font color=\"LEVEL\">NPC Information</font></center>");
			final String className = getClass().getName().substring(43);
			
			html1.append("<br>");
			
			html1.append("Instance Type: " + className + "<br1>Faction: " + getFactionId() + "<br1>Location ID: " + (getSpawn() != null ? getSpawn().getLocation() : 0) + "<br1>");
			
			if (this instanceof L2ControllableMobInstance)
			{
				html1.append("Mob Group: " + MobGroupTable.getInstance().getGroupForMob((L2ControllableMobInstance) this).getGroupId() + "<br>");
			}
			else
			{
				html1.append("Respawn Time: " + (getSpawn() != null ? (getSpawn().getRespawnDelay() / 1000) + "  Seconds<br>" : "?  Seconds<br>"));
			}
			
			html1.append("<table border=\"0\" width=\"100%\">");
			html1.append("<tr><td>Object ID</td><td>" + getObjectId() + "</td><td>NPC ID</td><td>" + getTemplate().npcId + "</td></tr>");
			
			html1.append("<tr><td>Castle</td><td>" + (getCastle() != null ? getCastle().getCastleId() : 0) + "</td><td>Coords</td><td>" + getX() + "," + getY() + "," + getZ() + "</td></tr>");
			
			html1.append("<tr><td>Level</td><td>" + getLevel() + "</td><td>Aggro</td><td>" + ((this instanceof L2Attackable) ? ((L2Attackable) this).getAggroRange() : 0) + "</td></tr>");
			html1.append("</table><br>");
			
			html1.append("<font color=\"LEVEL\">Combat</font>");
			html1.append("<table border=\"0\" width=\"100%\">");
			html1.append("<tr><td>Current HP</td><td>" + getCurrentHp() + "</td><td>Current MP</td><td>" + getCurrentMp() + "</td></tr>");
			html1.append("<tr><td>Max.HP</td><td>" + (int) (getMaxHp() / getStat().calcStat(Stats.MAX_HP, 1, this, null)) + "*" + getStat().calcStat(Stats.MAX_HP, 1, this, null) + "</td><td>Max.MP</td><td>" + getMaxMp() + "</td></tr>");
			html1.append("<tr><td>P.Atk.</td><td>" + getPAtk(null) + "</td><td>M.Atk.</td><td>" + getMAtk(null, null) + "</td></tr>");
			html1.append("<tr><td>P.Def.</td><td>" + getPDef(null) + "</td><td>M.Def.</td><td>" + getMDef(null, null) + "</td></tr>");
			html1.append("<tr><td>Accuracy</td><td>" + getAccuracy() + "</td><td>Evasion</td><td>" + getEvasionRate(null) + "</td></tr>");
			html1.append("<tr><td>Critical</td><td>" + getCriticalHit(null, null) + "</td><td>Speed</td><td>" + getRunSpeed() + "</td></tr>");
			html1.append("<tr><td>Atk.Speed</td><td>" + getPAtkSpd() + "</td><td>Cast.Speed</td><td>" + getMAtkSpd() + "</td></tr>");
			html1.append("</table><br>");
			
			html1.append("<font color=\"LEVEL\">Basic Stats</font>");
			html1.append("<table border=\"0\" width=\"100%\">");
			html1.append("<tr><td>STR</td><td>" + getSTR() + "</td><td>DEX</td><td>" + getDEX() + "</td><td>CON</td><td>" + getCON() + "</td></tr>");
			html1.append("<tr><td>INT</td><td>" + getINT() + "</td><td>WIT</td><td>" + getWIT() + "</td><td>MEN</td><td>" + getMEN() + "</td></tr>");
			html1.append("</table>");
			
			html1.append("<br><center><table><tr><td><button value=\"Edit NPC\" action=\"bypass -h admin_edit_npc " + getTemplate().npcId + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br1></td>");
			html1.append("<td><button value=\"Kill\" action=\"bypass -h admin_kill\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><br1></tr>");
			html1.append("<tr><td><button value=\"Show DropList\" action=\"bypass -h admin_show_droplist " + getTemplate().npcId + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			html1.append("<td><button value=\"Delete\" action=\"bypass -h admin_delete\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
			html1.append("</table></center><br>");
			html1.append("</body></html>");
			
			html.setHtml(html1.toString());
			player.sendPacket(html);
		}
		else if (Config.ALT_GAME_VIEWNPC)
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(0);
			final TextBuilder html1 = new TextBuilder("<html><body>");
			
			html1.append("<br><center><font color=\"LEVEL\">[Combat Stats]</font></center>");
			html1.append("<table border=0 width=\"100%\">");
			html1.append("<tr><td>Max.HP</td><td>" + (int) (getMaxHp() / getStat().calcStat(Stats.MAX_HP, 1, this, null)) + "*" + (int) getStat().calcStat(Stats.MAX_HP, 1, this, null) + "</td><td>Max.MP</td><td>" + getMaxMp() + "</td></tr>");
			html1.append("<tr><td>P.Atk.</td><td>" + getPAtk(null) + "</td><td>M.Atk.</td><td>" + getMAtk(null, null) + "</td></tr>");
			html1.append("<tr><td>P.Def.</td><td>" + getPDef(null) + "</td><td>M.Def.</td><td>" + getMDef(null, null) + "</td></tr>");
			html1.append("<tr><td>Accuracy</td><td>" + getAccuracy() + "</td><td>Evasion</td><td>" + getEvasionRate(null) + "</td></tr>");
			html1.append("<tr><td>Critical</td><td>" + getCriticalHit(null, null) + "</td><td>Speed</td><td>" + getRunSpeed() + "</td></tr>");
			html1.append("<tr><td>Atk.Speed</td><td>" + getPAtkSpd() + "</td><td>Cast.Speed</td><td>" + getMAtkSpd() + "</td></tr>");
			html1.append("<tr><td>Race</td><td>" + getTemplate().race + "</td><td></td><td></td></tr>");
			html1.append("</table>");
			
			html1.append("<br><center><font color=\"LEVEL\">[Basic Stats]</font></center>");
			html1.append("<table border=0 width=\"100%\">");
			html1.append("<tr><td>STR</td><td>" + getSTR() + "</td><td>DEX</td><td>" + getDEX() + "</td><td>CON</td><td>" + getCON() + "</td></tr>");
			html1.append("<tr><td>INT</td><td>" + getINT() + "</td><td>WIT</td><td>" + getWIT() + "</td><td>MEN</td><td>" + getMEN() + "</td></tr>");
			html1.append("</table>");
			
			html1.append("<br><center><font color=\"LEVEL\">[Drop Info]</font></center>");
			html1.append("Rates legend: <font color=\"ff0000\">50%+</font> <font color=\"00ff00\">30%+</font> <font color=\"0000ff\">less than 30%</font>");
			html1.append("<table border=0 width=\"100%\">");
			
			if (getTemplate().getDropData() != null)
			{
				for (final L2DropCategory cat : getTemplate().getDropData())
				{
					for (final L2DropData drop : cat.getAllDrops())
					{
						final String name = ItemTable.getInstance().getTemplate(drop.getItemId()).getName();
						
						if (drop.getChance() >= 600000)
						{
							html1.append("<tr><td><font color=\"ff0000\">" + name + "</font></td><td>" + (drop.isQuestDrop() ? "Quest" : (cat.isSweep() ? "Sweep" : "Drop")) + "</td></tr>");
						}
						else if (drop.getChance() >= 300000)
						{
							html1.append("<tr><td><font color=\"00ff00\">" + name + "</font></td><td>" + (drop.isQuestDrop() ? "Quest" : (cat.isSweep() ? "Sweep" : "Drop")) + "</td></tr>");
						}
						else
						{
							html1.append("<tr><td><font color=\"0000ff\">" + name + "</font></td><td>" + (drop.isQuestDrop() ? "Quest" : (cat.isSweep() ? "Sweep" : "Drop")) + "</td></tr>");
						}
					}
				}
			}
			
			html1.append("</table>");
			html1.append("</body></html>");
			
			html.setHtml(html1.toString());
			player.sendPacket(html);
		}
		
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(new ActionFailed());
	}
	
	/**
	 * Return the L2Castle this L2NpcInstance belongs to.
	 * @return
	 */
	public final Castle getCastle()
	{
		if (_castleIndex < 0)
		{
			
			final L2TownZone town = TownManager.getInstance().getTown(getX(), getY(), getZ());
			if (town != null)
			{
				_castleIndex = CastleManager.getInstance().getCastleIndex(town.getTaxById());
			}
			
			if (_castleIndex < 0)
			{
				_castleIndex = CastleManager.getInstance().findNearestCastleIndex(this);
			}
			else
			{
				_isInTown = true; // Npc was spawned in town
			}
		}
		
		if (_castleIndex < 0)
		{
			return null;
		}
		
		return CastleManager.getInstance().getCastles().get(_castleIndex);
		
	}
	
	public boolean getIsInCastleTown()
	{
		if (_castleIndex < 0)
		{
			getCastle();
		}
		return _isInTown;
	}
	
	/**
	 * Open a quest or chat window on client with the text of the L2NpcInstance in function of the command.<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Client packet : RequestBypassToServer</li><BR>
	 * <BR>
	 * @param player
	 * @param command The command string received from client
	 */
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		
		if (isBusy() && (getBusyMessage().length() > 0))
		{
			player.sendPacket(new ActionFailed());
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("data/html/npcbusy.htm");
			html.replace("%busymessage%", getBusyMessage());
			html.replace("%npcname%", getName());
			html.replace("%playername%", player.getName());
			player.sendPacket(html);
		}
		
		else if (command.equalsIgnoreCase("TerritoryStatus"))
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("data/html/territorystatus.htm");
			html.replace("%objectId%", String.valueOf(getObjectId()));
			html.replace("%npcname%", getName());
			
			if (getIsInCastleTown())
			{
				html.replace("%castlename%", getCastle().getName());
				html.replace("%taxpercent%", "" + getCastle().getTaxPercent());
				
				if (getCastle().getOwnerId() > 0)
				{
					final L2Clan clan = ClanTable.getInstance().getClan(getCastle().getOwnerId());
					html.replace("%clanname%", clan.getName());
					html.replace("%clanleadername%", clan.getLeaderName());
				}
				else
				{
					html.replace("%clanname%", "NPC");
					html.replace("%clanleadername%", "NPC");
				}
			}
			else
			{
				html.replace("%castlename%", "Open");
				html.replace("%taxpercent%", "0");
				
				html.replace("%clanname%", "No");
				html.replace("%clanleadername%", "None");
			}
			
			player.sendPacket(html);
		}
		else if (command.startsWith("Quest"))
		{
			String quest = "";
			try
			{
				quest = command.substring(5).trim();
			}
			catch (final IndexOutOfBoundsException ioobe)
			{
			}
			
			if (quest.length() == 0)
			{
				showQuestWindow(player);
			}
			else
			{
				showQuestWindow(player, quest);
			}
		}
		else if (command.startsWith("Chat"))
		{
			int val = 0;
			try
			{
				val = Integer.parseInt(command.substring(5));
			}
			catch (final IndexOutOfBoundsException ioobe)
			{
			}
			catch (final NumberFormatException nfe)
			{
			}
			
			showChatWindow(player, val);
		}
		else if (command.startsWith("Link"))
		{
			final String path = command.substring(5).trim();
			if (path.indexOf("..") != -1)
			{
				return;
			}
			
			final String filename = "data/html/" + path;
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(filename);
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
		}
		else if (command.startsWith("NobleTeleport"))
		{
			if (!player.isNoble())
			{
				final String filename = "data/html/teleporter/nobleteleporter-no.htm";
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile(filename);
				html.replace("%objectId%", String.valueOf(getObjectId()));
				html.replace("%npcname%", getName());
				player.sendPacket(html);
				return;
			}
			
			int val = 0;
			try
			{
				val = Integer.parseInt(command.substring(14));
			}
			catch (final IndexOutOfBoundsException ioobe)
			{
			}
			catch (final NumberFormatException nfe)
			{
			}
			
			showChatWindow(player, val);
		}
		else if (command.startsWith("Loto"))
		{
			int val = 0;
			try
			{
				val = Integer.parseInt(command.substring(5));
			}
			catch (final IndexOutOfBoundsException ioobe)
			{
			}
			catch (final NumberFormatException nfe)
			{
			}
			
			if (val == 0)
			{
				// new loto ticket
				for (int i = 0; i < 5; i++)
				{
					player.setLoto(i, 0);
				}
			}
			showLotoWindow(player, val);
		}
		else if (command.startsWith("CPRecovery"))
		{
			makeCPRecovery(player);
		}
		else if (command.startsWith("SupportMagic"))
		{
			makeSupportMagic(player);
		}
		else if (command.startsWith("multisell"))
		{
			L2Multisell.getInstance().createMultiSell(Integer.parseInt(command.substring(9).trim()), player, false, this);
		}
		else if (command.startsWith("exc_multisell"))
		{
			L2Multisell.getInstance().createMultiSell(Integer.parseInt(command.substring(13).trim()), player, true, this);
		}
		else if (command.startsWith("npcfind_byid"))
		{
			try
			{
				final L2Spawn spawn = SpawnTable.getInstance().getTemplate(Integer.parseInt(command.substring(12).trim()));
				
				if (spawn != null)
				{
					player.sendPacket(new RadarControl(2, 2, spawn.getLocx(), spawn.getLocy(), spawn.getLocz()));
					player.sendPacket(new RadarControl(0, 1, spawn.getLocx(), spawn.getLocy(), spawn.getLocz()));
				}
			}
			catch (final NumberFormatException nfe)
			{
				player.sendMessage("Wrong command parameters.");
			}
		}
		else if (command.startsWith("EnterRift"))
		{
			try
			{
				final Byte b1 = Byte.parseByte(command.substring(10)); // Selected Area: Recruit, Soldier etc
				DimensionalRiftManager.getInstance().start(player, b1, this);
			}
			catch (final Exception e)
			{
			}
		}
		else if (command.startsWith("ChangeRiftRoom"))
		{
			if (player.isInParty() && player.getParty().isInDimensionalRift())
			{
				player.getParty().getDimensionalRift().manualTeleport(player, this);
			}
			else
			{
				DimensionalRiftManager.getInstance().handleCheat(player, this);
			}
		}
		else if (command.startsWith("ExitRift"))
		{
			if (player.isInParty() && player.getParty().isInDimensionalRift())
			{
				player.getParty().getDimensionalRift().manualExitRift(player, this);
			}
			else
			{
				DimensionalRiftManager.getInstance().handleCheat(player, this);
			}
		}
		
	}
	
	/**
	 * Return null (regular NPCs don't have weapons instancies).<BR>
	 * <BR>
	 */
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		// regular NPCs dont have weapons instances
		return null;
	}
	
	/**
	 * Return the weapon item equiped in the right hand of the L2NpcInstance or null.<BR>
	 * <BR>
	 */
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		// Get the weapon identifier equiped in the right hand of the L2NpcInstance
		final int weaponId = getTemplate().rhand;
		
		if (weaponId < 1)
		{
			return null;
		}
		
		// Get the weapon item equiped in the right hand of the L2NpcInstance
		final L2Item item = ItemTable.getInstance().getTemplate(getTemplate().rhand);
		
		if (!(item instanceof L2Weapon))
		{
			return null;
		}
		
		return (L2Weapon) item;
	}
	
	/**
	 * Return null (regular NPCs don't have weapons instancies).<BR>
	 * <BR>
	 */
	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		// regular NPCs dont have weapons instancies
		return null;
	}
	
	/**
	 * Return the weapon item equiped in the left hand of the L2NpcInstance or null.<BR>
	 * <BR>
	 */
	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		// Get the weapon identifier equiped in the right hand of the L2NpcInstance
		final int weaponId = getTemplate().lhand;
		
		if (weaponId < 1)
		{
			return null;
		}
		
		// Get the weapon item equiped in the right hand of the L2NpcInstance
		final L2Item item = ItemTable.getInstance().getTemplate(getTemplate().lhand);
		
		if (!(item instanceof L2Weapon))
		{
			return null;
		}
		
		return (L2Weapon) item;
	}
	
	/**
	 * Send a Server->Client packet NpcHtmlMessage to the L2PcInstance in order to display the message of the L2NpcInstance.<BR>
	 * <BR>
	 * @param player The L2PcInstance who talks with the L2NpcInstance
	 * @param content The text of the L2NpcMessage
	 */
	public void insertObjectIdAndShowChatWindow(L2PcInstance player, String content)
	{
		// Send a Server->Client packet NpcHtmlMessage to the L2PcInstance in order to display the message of the L2NpcInstance
		content = content.replaceAll("%objectId%", String.valueOf(getObjectId()));
		final NpcHtmlMessage npcReply = new NpcHtmlMessage(getObjectId());
		npcReply.setHtml(content);
		player.sendPacket(npcReply);
	}
	
	/**
	 * Return the pathfile of the selected HTML file in function of the npcId and of the page number.<BR>
	 * <BR>
	 * <B><U> Format of the pathfile </U> :</B><BR>
	 * <BR>
	 * <li>if the file exists on the server (page number = 0) : <B>data/html/default/12006.htm</B> (npcId-page number)</li>
	 * <li>if the file exists on the server (page number > 0) : <B>data/html/default/12006-1.htm</B> (npcId-page number)</li>
	 * <li>if the file doesn't exist on the server : <B>data/html/npcdefault.htm</B> (message : "I have nothing to say to you")</li><BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2GuardInstance : Set the pathfile to data/html/guard/12006-1.htm (npcId-page number)</li><BR>
	 * <BR>
	 * @param npcId The Identifier of the L2NpcInstance whose text must be display
	 * @param val The number of the page to display
	 * @return
	 */
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}
		
		final String temp = "data/html/default/" + pom + ".htm";
		
		if (!Config.LAZY_CACHE)
		{
			// If not running lazy cache the file must be in the cache or it doesnt exist
			if (HtmCache.getInstance().contains(temp))
			{
				return temp;
			}
		}
		else
		{
			if (HtmCache.getInstance().isLoadable(temp))
			{
				return temp;
			}
		}
		
		// If the file is not found, the standard message "I have nothing to say to you" is returned
		return "data/html/npcdefault.htm";
	}
	
	public void showBuyWindow(L2PcInstance player, int val)
	{
		double taxRate = 0;
		if (getIsInCastleTown())
		{
			taxRate = getCastle().getTaxRate();
		}
		
		player.tempInventoryDisable();
		
		if (Config.DEBUG)
		{
			_log.fine("Showing buylist :" + player.getName() + " List ID :" + val);
		}
		
		final L2TradeList list = TradeController.getInstance().getBuyList(val);
		
		if ((list != null) && list.getNpcId().equals(String.valueOf(getNpcId())))
		{
			player.sendPacket(new BuyList(list, player.getAdena(), taxRate));
		}
		else
		{
			_log.warning("possible client hacker: " + player.getName() + " attempting to buy from GM shop! < Ban him!");
			_log.warning("buylist id:" + val);
		}
		
		player.sendPacket(new ActionFailed());
	}
	
	/**
	 * Open a choose quest window on client with all quests available of the L2NpcInstance.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Send a Server->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance</li><BR>
	 * <BR>
	 * @param player The L2PcInstance that talk with the L2NpcInstance
	 * @param quests The table containing quests of the L2NpcInstance
	 */
	public void showQuestChooseWindow(L2PcInstance player, Quest[] quests)
	{
		final TextBuilder sb = new TextBuilder();
		
		sb.append("<html><body><title>Talk about:</title><br>");
		
		for (final Quest q : quests)
		{
			sb.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Quest ").append(q.getName()).append("\">").append(q.getDescr()).append("</a><br>");
		}
		
		sb.append("</body></html>");
		
		// Send a Server->Client packet NpcHtmlMessage to the L2PcInstance in order to display the message of the L2NpcInstance
		insertObjectIdAndShowChatWindow(player, sb.toString());
	}
	
	/**
	 * Open a quest window on client with the text of the L2NpcInstance.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Get the text of the quest state in the folder data/scripts/quests/questId/stateId.htm</li>
	 * <li>Send a Server->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance</li>
	 * <li>Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet</li><BR>
	 * <BR>
	 * @param player The L2PcInstance that talk with the L2NpcInstance
	 * @param questId The Identifier of the quest to display the message
	 */
	public void showQuestWindow(L2PcInstance player, String questId)
	{
		String content;
		
		// Get the state of the selected quest
		QuestState qs = player.getQuestState(questId);
		
		if (qs != null)
		{
			// If the quest is already started, no need to show a window
			if (!qs.getQuest().notifyTalk(this, qs))
			{
				return;
			}
		}
		else
		{
			final Quest q = QuestManager.getInstance().getQuest(questId);
			if (q != null)
			{
				if ((q.getQuestIntId() >= 1) && (q.getQuestIntId() < 1000))
				{
					final Quest[] questList = player.getAllActiveQuests();
					if (questList.length >= 15) // if too many ongoing quests, don't show window and send message
					{
						player.sendPacket(new SystemMessage(401));
						return;
					}
					
					if ((player.getWeightPenalty() >= 3) || ((player.getInventoryLimit() * 0.8) <= player.getInventory().getSize()))
					{
						player.sendPacket(new SystemMessage(SystemMessage.INVENTORY_80_PERCENT_FOR_QUEST));
						return;
					}
				}
				
				// check for start point
				final Quest[] qlst = getTemplate().getEventQuests(Quest.QuestEventType.QUEST_START);
				if ((qlst != null) && (qlst.length > 0))
				{
					for (final Quest element : qlst)
					{
						if (element == q)
						{
							qs = q.newQuestState(player);
							if (!qs.getQuest().notifyTalk(this, qs))
							{
								return; // no need to show a window
							}
							break;
						}
					}
				}
			}
		}
		
		if (qs == null)
		{
			// no quests found
			content = "<html><body>I have no tasks for you right now.</body></html>";
		}
		else
		{
			questId = qs.getQuest().getName();
			final String stateId = qs.getStateId();
			final String path = "data/scripts/quests/" + questId + "/" + stateId + ".htm";
			content = HtmCache.getInstance().getHtm(path); // TODO path for quests html
			
			if (Config.DEBUG)
			{
				if (content != null)
				{
					_log.fine("Showing quest window for quest " + questId + " html path: " + path);
				}
				else
				{
					_log.fine("File not exists for quest " + questId + " html path: " + path);
				}
			}
		}
		
		// Send a Server->Client packet NpcHtmlMessage to the L2PcInstance in order to display the message of the L2NpcInstance
		if (content != null)
		{
			insertObjectIdAndShowChatWindow(player, content);
		}
		
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(new ActionFailed());
	}
	
	/**
	 * Collect awaiting quests/start points and display a QuestChooseWindow (if several available) or QuestWindow.<BR>
	 * <BR>
	 * @param player The L2PcInstance that talk with the L2NpcInstance
	 */
	public void showQuestWindow(L2PcInstance player)
	{
		// collect awaiting quests and start points
		final List<Quest> options = new FastList<>();
		
		final QuestState[] awaits = player.getQuestsForTalk(getTemplate().npcId);
		final Quest[] starts = getTemplate().getEventQuests(Quest.QuestEventType.QUEST_START);
		
		// Quests are limited between 1 and 999 because those are the quests that are supported by the client.
		if (awaits != null)
		{
			for (final QuestState x : awaits)
			{
				if (!options.contains(x))
				{
					if ((x.getQuest().getQuestIntId() > 0) && (x.getQuest().getQuestIntId() < 1000))
					{
						options.add(x.getQuest());
					}
				}
			}
		}
		
		if (starts != null)
		{
			for (final Quest x : starts)
			{
				if (!options.contains(x))
				{
					if ((x.getQuestIntId() > 0) && (x.getQuestIntId() < 1000))
					{
						options.add(x);
					}
				}
			}
		}
		
		// Display a QuestChooseWindow (if several quests are available) or QuestWindow
		if (options.size() > 1)
		{
			showQuestChooseWindow(player, options.toArray(new Quest[options.size()]));
		}
		else if (options.size() == 1)
		{
			showQuestWindow(player, options.get(0).getName());
		}
		else
		{
			showQuestWindow(player, "");
		}
	}
	
	/**
	 * Open a Loto window on client with the text of the L2NpcInstance.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Get the text of the selected HTML file in function of the npcId and of the page number</li>
	 * <li>Send a Server->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance</li>
	 * <li>Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet</li><BR>
	 * @param player The L2PcInstance that talk with the L2NpcInstance
	 * @param val The number of the page of the L2NpcInstance to display
	 */
	// 0 - first buy lottery ticket window
	// 1-20 - buttons
	// 21 - second buy lottery ticket window
	// 22 - selected ticket with 5 numbers
	// 23 - current lottery jackpot
	// 24 - Previous winning numbers/Prize claim
	// >24 - check lottery ticket by item object id
	public void showLotoWindow(L2PcInstance player, int val)
	{
		final int npcId = getTemplate().npcId;
		String filename;
		SystemMessage sm;
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		
		if (val == 0) // 0 - first buy lottery ticket window
		{
			filename = (getHtmlPath(npcId, 1));
			html.setFile(filename);
		}
		else if ((val >= 1) && (val <= 21)) // 1-20 - buttons, 21 - second buy lottery ticket window
		{
			if (!Lottery.getInstance().isStarted())
			{
				// tickets can't be sold
				player.sendPacket(new SystemMessage(930));
				return;
			}
			if (!Lottery.getInstance().isSellableTickets())
			{
				// tickets can't be sold
				player.sendPacket(new SystemMessage(784));
				return;
			}
			
			filename = (getHtmlPath(npcId, 5));
			html.setFile(filename);
			
			int count = 0;
			int found = 0;
			// counting buttons and unsetting button if found
			for (int i = 0; i < 5; i++)
			{
				if (player.getLoto(i) == val)
				{
					// unsetting button
					player.setLoto(i, 0);
					found = 1;
				}
				else if (player.getLoto(i) > 0)
				{
					count++;
				}
			}
			
			// if not rearched limit 5 and not unseted value
			if ((count < 5) && (found == 0) && (val <= 20))
			{
				for (int i = 0; i < 5; i++)
				{
					if (player.getLoto(i) == 0)
					{
						player.setLoto(i, val);
						break;
					}
				}
			}
			
			// setting pusshed buttons
			count = 0;
			for (int i = 0; i < 5; i++)
			{
				if (player.getLoto(i) > 0)
				{
					count++;
					String button = String.valueOf(player.getLoto(i));
					if (player.getLoto(i) < 10)
					{
						button = "0" + button;
					}
					final String search = "fore=\"L2UI.lottoNum" + button + "\" back=\"L2UI.lottoNum" + button + "a_check\"";
					final String replace = "fore=\"L2UI.lottoNum" + button + "a_check\" back=\"L2UI.lottoNum" + button + "\"";
					html.replace(search, replace);
				}
			}
			
			if (count == 5)
			{
				final String search = "0\">Return";
				final String replace = "22\">The winner selected the numbers above.";
				html.replace(search, replace);
			}
		}
		else if (val == 22) // 22 - selected ticket with 5 numbers
		{
			if (!Lottery.getInstance().isStarted())
			{
				// tickets can't be sold
				player.sendPacket(new SystemMessage(930));
				return;
			}
			if (!Lottery.getInstance().isSellableTickets())
			{
				// tickets can't be sold
				player.sendPacket(new SystemMessage(784));
				return;
			}
			
			final int price = Config.ALT_LOTTERY_TICKET_PRICE;
			final int lotonumber = Lottery.getInstance().getId();
			int enchant = 0;
			int type2 = 0;
			
			for (int i = 0; i < 5; i++)
			{
				if (player.getLoto(i) == 0)
				{
					return;
				}
				
				if (player.getLoto(i) < 17)
				{
					enchant += Math.pow(2, player.getLoto(i) - 1);
				}
				else
				{
					type2 += Math.pow(2, player.getLoto(i) - 17);
				}
			}
			if (player.getAdena() < price)
			{
				sm = new SystemMessage(SystemMessage.YOU_NOT_ENOUGH_ADENA);
				player.sendPacket(sm);
				return;
			}
			if (!player.reduceAdena("Loto", price, this, true))
			{
				return;
			}
			Lottery.getInstance().increasePrize(price);
			
			sm = new SystemMessage(SystemMessage.ACQUIRED);
			sm.addNumber(lotonumber);
			sm.addItemName(4442);
			player.sendPacket(sm);
			
			final L2ItemInstance item = new L2ItemInstance(IdFactory.getInstance().getNextId(), 4442);
			item.setCount(1);
			item.setCustomType1(lotonumber);
			item.setEnchantLevel(enchant);
			item.setCustomType2(type2);
			player.getInventory().addItem("Loto", item, player, this);
			
			final InventoryUpdate iu = new InventoryUpdate();
			iu.addItem(item);
			final L2ItemInstance adenaupdate = player.getInventory().getItemByItemId(57);
			iu.addModifiedItem(adenaupdate);
			player.sendPacket(iu);
			
			filename = (getHtmlPath(npcId, 3));
			html.setFile(filename);
		}
		else if (val == 23) // 23 - current lottery jackpot
		{
			filename = (getHtmlPath(npcId, 3));
			html.setFile(filename);
		}
		else if (val == 24) // 24 - Previous winning numbers/Prize claim
		{
			filename = (getHtmlPath(npcId, 4));
			html.setFile(filename);
			
			final int lotonumber = Lottery.getInstance().getId();
			String message = "";
			for (final L2ItemInstance item : player.getInventory().getItems())
			{
				if (item == null)
				{
					continue;
				}
				if ((item.getItemId() == 4442) && (item.getCustomType1() < lotonumber))
				{
					message = message + "<a action=\"bypass -h npc_%objectId%_Loto " + item.getObjectId() + "\">" + item.getCustomType1() + " Event Number ";
					final int[] numbers = Lottery.getInstance().decodeNumbers(item.getEnchantLevel(), item.getCustomType2());
					for (int i = 0; i < 5; i++)
					{
						message += numbers[i] + " ";
					}
					final int[] check = Lottery.getInstance().checkTicket(item);
					if (check[0] > 0)
					{
						switch (check[0])
						{
							case 1:
								message += "- 1st Prize";
								break;
							case 2:
								message += "- 2nd Prize";
								break;
							case 3:
								message += "- 3th Prize";
								break;
							case 4:
								message += "- 4th Prize";
								break;
						}
						message += " " + check[1] + "a.";
					}
					message += "</a><br>";
				}
			}
			if (message.isEmpty())
			{
				message += "There is no winning lottery ticket...<br>";
			}
			html.replace("%result%", message);
		}
		else if (val > 24) // >24 - check lottery ticket by item object id
		{
			final int lotonumber = Lottery.getInstance().getId();
			final L2ItemInstance item = player.getInventory().getItemByObjectId(val);
			if ((item == null) || (item.getItemId() != 4442) || (item.getCustomType1() >= lotonumber))
			{
				return;
			}
			final int[] check = Lottery.getInstance().checkTicket(item);
			
			sm = new SystemMessage(SystemMessage.DISSAPEARED_ITEM);
			sm.addItemName(4442);
			player.sendPacket(sm);
			
			final int adena = check[1];
			if (adena > 0)
			{
				player.addAdena("Loto", adena, this, true);
			}
			player.destroyItem("Loto", item, this, false);
			return;
		}
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%race%", "" + Lottery.getInstance().getId());
		html.replace("%adena%", "" + Lottery.getInstance().getPrize());
		html.replace("%ticket_price%", "" + Config.ALT_LOTTERY_TICKET_PRICE);
		html.replace("%prize5%", "" + (Config.ALT_LOTTERY_5_NUMBER_RATE * 100));
		html.replace("%prize4%", "" + (Config.ALT_LOTTERY_4_NUMBER_RATE * 100));
		html.replace("%prize3%", "" + (Config.ALT_LOTTERY_3_NUMBER_RATE * 100));
		html.replace("%prize2%", "" + Config.ALT_LOTTERY_2_AND_1_NUMBER_PRIZE);
		html.replace("%enddate%", "" + DateFormat.getDateInstance().format(Lottery.getInstance().getEndDate()));
		player.sendPacket(html);
		
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(new ActionFailed());
	}
	
	public void makeCPRecovery(L2PcInstance player)
	{
		if ((getNpcId() != 8225) && (getNpcId() != 8226))
		{
			return;
		}
		
		final int neededmoney = 100;
		
		if (!player.reduceAdena("RestoreCP", neededmoney, player.getLastFolkNPC(), true))
		{
			return;
		}
		
		final L2Skill skill = SkillTable.getInstance().getInfo(4380, 1);
		if (skill != null)
		{
			setTarget(player);
			doCast(skill);
		}
		player.sendPacket(new ActionFailed());
	}
	
	/**
	 * Add Newbie helper buffs to L2Player according to its level.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Get the range level in wich player must be to obtain buff</li>
	 * <li>If player level is out of range, display a message and return</li>
	 * <li>According to player level cast buff</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> Newbie Helper Buff list is define in sql table helper_buff_list</B></FONT><BR>
	 * <BR>
	 * @param player The L2PcInstance that talk with the L2NpcInstance
	 */
	public void makeSupportMagic(L2PcInstance player)
	{
		final int player_level = player.getLevel();
		int lowestLevel;
		int highestLevel;
		L2Skill skill;
		
		// Select the player
		setTarget(player);
		
		// Calculate the min and max level between wich the player must be to obtain buff
		if (player.isMageClass())
		{
			lowestLevel = HelperBuffTable.getInstance().getMagicClassLowestLevel();
			highestLevel = HelperBuffTable.getInstance().getMagicClassHighestLevel();
		}
		else
		{
			lowestLevel = HelperBuffTable.getInstance().getPhysicClassLowestLevel();
			highestLevel = HelperBuffTable.getInstance().getPhysicClassHighestLevel();
		}
		
		// If the player is too high level, display a message and return
		if (player_level > highestLevel)
		{
			final String content = "<html><body>Newbie Guide:<br>Only a <font color=\"LEVEL\">novice character of level " + highestLevel + " or less</font> can receive my support magic.<br>Your novice character is the first one that you created and raised in this world.</body></html>";
			insertObjectIdAndShowChatWindow(player, content);
			return;
		}
		
		// If the player is too low level, display a message and return
		if (player_level < lowestLevel)
		{
			final String content = "<html><body>Come back here when you have reached level " + lowestLevel + ". I will give you support magic then.</body></html>";
			insertObjectIdAndShowChatWindow(player, content);
			return;
		}
		
		// If the player is not a newbie, display a message and return
		if (player.getNewbieState() != L2PcInstance.NEW)
		{
			final String content = "<html><body>Newbie Guide:<br>Your novice character is not the first one that you created and raised in this world.<br>Therefore, you cannot receive support magic anymore.</body></html>";
			insertObjectIdAndShowChatWindow(player, content);
			return;
		}
		
		// Go through the Helper Buff list define in sql table helper_buff_list and cast skill
		for (final L2HelperBuff helperBuffItem : HelperBuffTable.getInstance().getHelperBuffTable())
		{
			if (helperBuffItem.isMagicClassBuff() == player.isMageClass())
			{
				if ((player_level >= helperBuffItem.getLowerLevel()) && (player_level <= helperBuffItem.getUpperLevel()))
				{
					skill = SkillTable.getInstance().getInfo(helperBuffItem.getSkillID(), helperBuffItem.getSkillLevel());
					if (skill.getSkillType() == SkillType.SUMMON)
					{
						player.doCast(skill);
					}
					else
					{
						doCast(skill);
					}
				}
			}
		}
	}
	
	public void showChatWindow(L2PcInstance player)
	{
		showChatWindow(player, 0);
	}
	
	/**
	 * Open a chat window on client with the text of the L2NpcInstance.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Get the text of the selected HTML file in function of the npcId and of the page number</li>
	 * <li>Send a Server->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance</li>
	 * <li>Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet</li><BR>
	 * @param player The L2PcInstance that talk with the L2NpcInstance
	 * @param val The number of the page of the L2NpcInstance to display
	 */
	public void showChatWindow(L2PcInstance player, int val)
	{
		if (getTemplate().type.equals("L2Auctioneer") && (val == 0))
		{
			return;
		}
		final int npcId = getTemplate().npcId;
		
		/* For use with Seven Signs implementation */
		String filename = SevenSigns.SEVEN_SIGNS_HTML_PATH;
		final int sealAvariceOwner = SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_AVARICE);
		final int sealGnosisOwner = SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_GNOSIS);
		final int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
		final boolean isSealValidationPeriod = SevenSigns.getInstance().isSealValidationPeriod();
		final int compWinner = SevenSigns.getInstance().getCabalHighestScore();
		
		switch (npcId)
		{
			case 8078:
			case 8079:
			case 8080:
			case 8081:
			case 8082: // Dawn Priests
			case 8083:
			case 8084:
			case 8168:
			case 8692:
			case 8694:
				switch (playerCabal)
				{
					case SevenSigns.CABAL_DAWN:
						if (isSealValidationPeriod)
						{
							if (compWinner == SevenSigns.CABAL_DAWN)
							{
								if (compWinner != sealGnosisOwner)
								{
									filename += "dawn_priest_2c.htm";
								}
								else
								{
									filename += "dawn_priest_2a.htm";
								}
							}
							else
							{
								filename += "dawn_priest_2b.htm";
							}
						}
						else
						{
							filename += "dawn_priest_1b.htm";
						}
						break;
					case SevenSigns.CABAL_DUSK:
						if (isSealValidationPeriod)
						{
							filename += "dawn_priest_3b.htm";
						}
						else
						{
							filename += "dawn_priest_3a.htm";
						}
						break;
					default:
						if (isSealValidationPeriod)
						{
							if (compWinner == SevenSigns.CABAL_DAWN)
							{
								filename += "dawn_priest_4.htm";
							}
							else
							{
								filename += "dawn_priest_2b.htm";
							}
						}
						else
						{
							filename += "dawn_priest_1a.htm";
						}
						break;
				}
				break;
			case 8085:
			case 8086:
			case 8087:
			case 8088: // Dusk Priest
			case 8089:
			case 8090:
			case 8091:
			case 8169:
			case 8693:
			case 8695:
				switch (playerCabal)
				{
					case SevenSigns.CABAL_DUSK:
						if (isSealValidationPeriod)
						{
							if (compWinner == SevenSigns.CABAL_DUSK)
							{
								if (compWinner != sealGnosisOwner)
								{
									filename += "dusk_priest_2c.htm";
								}
								else
								{
									filename += "dusk_priest_2a.htm";
								}
							}
							else
							{
								filename += "dusk_priest_2b.htm";
							}
						}
						else
						{
							filename += "dusk_priest_1b.htm";
						}
						break;
					case SevenSigns.CABAL_DAWN:
						if (isSealValidationPeriod)
						{
							filename += "dusk_priest_3b.htm";
						}
						else
						{
							filename += "dusk_priest_3a.htm";
						}
						break;
					default:
						if (isSealValidationPeriod)
						{
							if (compWinner == SevenSigns.CABAL_DUSK)
							{
								filename += "dusk_priest_4.htm";
							}
							else
							{
								filename += "dusk_priest_2b.htm";
							}
						}
						else
						{
							filename += "dusk_priest_1a.htm";
						}
						break;
				}
				break;
			case 8095: //
			case 8096: //
			case 8097: //
			case 8098: // Enter Necropolises
			case 8099: //
			case 8100: //
			case 8101: //
			case 8102: //
				if (isSealValidationPeriod)
				{
					if ((playerCabal != compWinner) || (sealAvariceOwner != compWinner))
					{
						switch (compWinner)
						{
							case SevenSigns.CABAL_DAWN:
								player.sendPacket(new SystemMessage(SystemMessage.CAN_BE_USED_BY_DAWN));
								filename += "necro_no.htm";
								break;
							case SevenSigns.CABAL_DUSK:
								player.sendPacket(new SystemMessage(SystemMessage.CAN_BE_USED_BY_DUSK));
								filename += "necro_no.htm";
								break;
							case SevenSigns.CABAL_NULL:
								filename = (getHtmlPath(npcId, val)); // do the default!
								break;
						}
					}
					else
					{
						filename = (getHtmlPath(npcId, val)); // do the default!
					}
				}
				else
				{
					if (playerCabal == SevenSigns.CABAL_NULL)
					{
						filename += "necro_no.htm";
					}
					else
					{
						filename = (getHtmlPath(npcId, val)); // do the default!
					}
				}
				break;
			case 8114: //
			case 8115: //
			case 8116: // Enter Catacombs
			case 8117: //
			case 8118: //
			case 8119: //
				if (isSealValidationPeriod)
				{
					if ((playerCabal != compWinner) || (sealGnosisOwner != compWinner))
					{
						switch (compWinner)
						{
							case SevenSigns.CABAL_DAWN:
								player.sendPacket(new SystemMessage(SystemMessage.CAN_BE_USED_BY_DAWN));
								filename += "cata_no.htm";
								break;
							case SevenSigns.CABAL_DUSK:
								player.sendPacket(new SystemMessage(SystemMessage.CAN_BE_USED_BY_DUSK));
								filename += "cata_no.htm";
								break;
							case SevenSigns.CABAL_NULL:
								filename = (getHtmlPath(npcId, val)); // do the default!
								break;
						}
					}
					else
					{
						filename = (getHtmlPath(npcId, val)); // do the default!
					}
				}
				else
				{
					if (playerCabal == SevenSigns.CABAL_NULL)
					{
						filename += "cata_no.htm";
					}
					else
					{
						filename = (getHtmlPath(npcId, val)); // do the default!
					}
				}
				break;
			case 8112: // Gatekeeper Spirit (Disciples)
				filename += "spirit_exit.htm";
				break;
			case 8127: //
			case 8128: //
			case 8129: // Dawn Festival Guides
			case 8130: //
			case 8131: //
				filename += "festival/dawn_guide.htm";
				break;
			case 8137: //
			case 8138: //
			case 8139: // Dusk Festival Guides
			case 8140: //
			case 8141: //
				filename += "festival/dusk_guide.htm";
				break;
			case 8092: // Black Marketeer of Mammon
				filename += "blkmrkt_1.htm";
				break;
			case 8113: // Merchant of Mammon
				switch (compWinner)
				{
					case SevenSigns.CABAL_DAWN:
						if ((playerCabal != compWinner) || (playerCabal != sealAvariceOwner))
						{
							player.sendPacket(new SystemMessage(SystemMessage.CAN_BE_USED_BY_DAWN));
							player.sendPacket(new ActionFailed());
							return;
						}
						break;
					case SevenSigns.CABAL_DUSK:
						if ((playerCabal != compWinner) || (playerCabal != sealAvariceOwner))
						{
							player.sendPacket(new SystemMessage(SystemMessage.CAN_BE_USED_BY_DUSK));
							player.sendPacket(new ActionFailed());
							return;
						}
						break;
				}
				filename += "mammmerch_1.htm";
				break;
			case 8126: // Blacksmith of Mammon
				switch (compWinner)
				{
					case SevenSigns.CABAL_DAWN:
						if ((playerCabal != compWinner) || (playerCabal != sealGnosisOwner))
						{
							player.sendPacket(new SystemMessage(SystemMessage.CAN_BE_USED_BY_DAWN));
							player.sendPacket(new ActionFailed());
							return;
						}
						break;
					case SevenSigns.CABAL_DUSK:
						if ((playerCabal != compWinner) || (playerCabal != sealGnosisOwner))
						{
							player.sendPacket(new SystemMessage(SystemMessage.CAN_BE_USED_BY_DUSK));
							player.sendPacket(new ActionFailed());
							return;
						}
						break;
				}
				filename += "mammblack_1.htm";
				break;
			case 8132:
			case 8133:
			case 8134:
			case 8135:
			case 8136: // Festival Witches
			case 8142:
			case 8143:
			case 8144:
			case 8145:
			case 8146:
				filename += "festival/festival_witch.htm";
				break;
			case 8688:
				if (player.isNoble())
				{
					filename = Olympiad.OLYMPIAD_HTML_FILE + "noble_main.htm";
				}
				else
				{
					filename = (getHtmlPath(npcId, val));
				}
				break;
			case 8690:
			case 8769:
			case 8770:
			case 8771:
			case 8772:
				if (player.isHero())
				{
					filename = Olympiad.OLYMPIAD_HTML_FILE + "hero_main.htm";
				}
				else
				{
					filename = (getHtmlPath(npcId, val));
				}
				break;
			default:
				if ((npcId >= 12901) && (npcId <= 12954))
				{
					if (val == 0)
					{
						filename += "rift/GuardianOfBorder.htm";
					}
					else
					{
						filename += "rift/GuardianOfBorder-" + val + ".htm";
					}
					break;
				}
				
				if (((npcId >= 8093) && (npcId <= 8094)) || ((npcId >= 8172) && (npcId <= 8201)) || ((npcId >= 8239) && (npcId <= 8254)))
				{
					return;
				}
				// Get the text of the selected HTML file in function of the npcId and of the page number
				filename = (getHtmlPath(npcId, val));
				break;
		}
		
		// Send a Server->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		
		// String word = "npc-"+npcId+(val>0 ? "-"+val : "" )+"-dialog-append";
		
		if (this instanceof L2MerchantInstance)
		{
			if (Config.LIST_PET_RENT_NPC.contains(npcId))
			{
				html.replace("_Quest", "_RentPet\">Rent Pet</a><br><a action=\"bypass -h npc_%objectId%_Quest");
			}
		}
		
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%festivalMins%", SevenSignsFestival.getInstance().getTimeToNextFestivalStr());
		player.sendPacket(html);
		
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(new ActionFailed());
	}
	
	/**
	 * Open a chat window on client with the text specified by the given file name and path,<BR>
	 * relative to the datapack root. <BR>
	 * <BR>
	 * Added by Tempy
	 * @param player The L2PcInstance that talk with the L2NpcInstance
	 * @param filename The filename that contains the text to send
	 */
	public void showChatWindow(L2PcInstance player, String filename)
	{
		// Send a Server->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
		
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(new ActionFailed());
	}
	
	/**
	 * Return the Exp Reward of this L2NpcInstance contained in the L2NpcTemplate (modified by RATE_XP).<BR>
	 * <BR>
	 * @return
	 */
	public int getExpReward()
	{
		final double rateXp = getStat().calcStat(Stats.MAX_HP, 1, this, null);
		return (int) (getTemplate().rewardExp * rateXp * Config.RATE_XP);
	}
	
	/**
	 * Return the SP Reward of this L2NpcInstance contained in the L2NpcTemplate (modified by RATE_SP).<BR>
	 * <BR>
	 * @return
	 */
	public int getSpReward()
	{
		final double rateSp = getStat().calcStat(Stats.MAX_HP, 1, this, null);
		return (int) (getTemplate().rewardSp * rateSp * Config.RATE_SP);
	}
	
	/**
	 * Kill the L2NpcInstance (the corpse disappeared after 7 seconds).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Create a DecayTask to remove the corpse of the L2NpcInstance after 7 seconds</li>
	 * <li>Set target to null and cancel Attack or Cast</li>
	 * <li>Stop movement</li>
	 * <li>Stop HP/MP/CP Regeneration task</li>
	 * <li>Stop all active skills effects in progress on the L2Character</li>
	 * <li>Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform</li>
	 * <li>Notify L2Character AI</li><BR>
	 * <BR>
	 * <B><U> Overriden in </U> :</B><BR>
	 * <BR>
	 * <li>L2Attackable</li><BR>
	 * <BR>
	 * @param killer The L2Character who killed it
	 */
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		_currentCollisionHeight = getTemplate().collisionHeight;
		_currentCollisionRadius = getTemplate().collisionRadius;
		DecayTaskManager.getInstance().addDecayTask(this);
		return true;
	}
	
	/**
	 * Set the spawn of the L2NpcInstance.<BR>
	 * <BR>
	 * @param spawn The L2Spawn that manage the L2NpcInstance
	 */
	public void setSpawn(L2Spawn spawn)
	{
		_spawn = spawn;
	}
	
	@Override
	public void onSpawn()
	{
		if (_inventory != null)
		{
			_inventory.reset();
		}
		
		super.onSpawn();
		
		if (getTemplate().getEventQuests(Quest.QuestEventType.ON_SPAWN) != null)
		{
			for (final Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_SPAWN))
			{
				quest.notifySpawn(this);
			}
		}
	}
	
	/**
	 * Remove the L2NpcInstance from the world and update its spawn object (for a complete removal use the deleteMe method).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Remove the L2NpcInstance from the world when the decay task is launched</li>
	 * <li>Decrease its spawn counter</li>
	 * <li>Manage Siege task (killFlag, killCT)</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from _allObjects of L2World </B></FONT><BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packets to players</B></FONT><BR>
	 * <BR>
	 */
	@Override
	public void onDecay()
	{
		if (isDecayed())
		{
			return;
		}
		setDecayed(true);
		
		// Manage Life Control Tower
		if (this instanceof L2ControlTowerInstance)
		{
			((L2ControlTowerInstance) this).onDeath();
		}
		
		// Remove the L2NpcInstance from the world when the decay task is launched
		super.onDecay();
		
		// Decrease its spawn counter
		if (_spawn != null)
		{
			_spawn.decreaseCount(this);
		}
	}
	
	/**
	 * Remove PROPERLY the L2NpcInstance from the world.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Remove the L2NpcInstance from the world and update its spawn object</li>
	 * <li>Remove all L2Object from _knownObjects and _knownPlayer of the L2NpcInstance then cancel Attak or Cast and notify AI</li>
	 * <li>Remove L2Object object from _allObjects of L2World</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packets to players</B></FONT><BR>
	 * <BR>
	 */
	public void deleteMe()
	{
		final L2WorldRegion oldRegion = getWorldRegion();
		
		try
		{
			decayMe();
		}
		catch (final Throwable t)
		{
			_log.severe("deletedMe(): " + t);
		}
		
		if (oldRegion != null)
		{
			oldRegion.removeFromZones(this);
		}
		
		// Remove all L2Object from _knownObjects and _knownPlayer of the L2Character then cancel Attak or Cast and notify AI
		try
		{
			getKnownList().removeAllKnownObjects();
		}
		catch (final Throwable t)
		{
			_log.severe("deletedMe(): " + t);
		}
		
		// Remove L2Object object from _allObjects of L2World
		L2World.getInstance().removeObject(this);
	}
	
	/**
	 * Return the L2Spawn object that manage this L2NpcInstance.<BR>
	 * <BR>
	 * @return
	 */
	public L2Spawn getSpawn()
	{
		return _spawn;
	}
	
	@Override
	public String toString()
	{
		return getTemplate().name;
	}
	
	public boolean isDecayed()
	{
		return _isDecayed;
	}
	
	public void setDecayed(boolean decayed)
	{
		_isDecayed = decayed;
	}
	
	public void endDecayTask()
	{
		if (!isDecayed())
		{
			
			DecayTaskManager.getInstance().cancelDecayTask(this);
			onDecay();
		}
	}
	
	public void setCollisionHeight(double height)
	{
		_currentCollisionHeight = height;
	}
	
	public void setCollisionRadius(double radius)
	{
		_currentCollisionRadius = radius;
	}
	
	public double getCollisionHeight()
	{
		return _currentCollisionHeight;
	}
	
	public double getCollisionRadius()
	{
		return _currentCollisionRadius;
	}
	
	public int getMaxSiegeHp()
	{
		return _maxSiegeHp;
	}
	
	public void setMaxSiegeHp(int hp)
	{
		_maxSiegeHp = hp;
	}
	
	public boolean rechargeAutoSoulShot(boolean physical, boolean magic)
	{
		if (getTemplate().ssRate == 0)
		{
			return false;
		}
		
		final L2Weapon weaponItem = getActiveWeaponItem();
		if (weaponItem == null)
		{
			return false;
		}
		
		if (magic)
		{
			if (getTemplate().ssRate < Rnd.get(100))
			{
				_inventory.bshotInUse = false;
				return false;
			}
			
			if (_inventory.destroyItemByItemId("Consume", 3947, weaponItem.getSpiritShotCount(), null, null) != null)
			{
				_inventory.bshotInUse = true;
				broadcastPacket(new MagicSkillUse(this, this, 2061, 1, 0, 0)); // no grade
				return true;
			}
			_inventory.bshotInUse = false;
		}
		
		if (physical)
		{
			if (getTemplate().ssRate < Rnd.get(100))
			{
				_inventory.sshotInUse = false;
				return false;
			}
			
			if (_inventory.destroyItemByItemId("Consume", 1835, weaponItem.getSoulShotCount(), null, null) != null)
			{
				_inventory.sshotInUse = true;
				broadcastPacket(new MagicSkillUse(this, this, 2039, 1, 0, 0)); // no grade
				return true;
			}
			_inventory.sshotInUse = false;
		}
		return false;
	}
	
	public boolean isUsingShot(boolean physical)
	{
		if (_inventory == null)
		{
			return false;
		}
		
		if (physical && _inventory.sshotInUse)
		{
			return true;
		}
		
		if (!physical && _inventory.bshotInUse)
		{
			return true;
		}
		
		return false;
	}
	
	@Override
	protected final void notifyQuestEventSkillFinished(L2Skill skill, L2Object target)
	{
		try
		{
			if (getTemplate().getEventQuests(Quest.QuestEventType.ON_SPELL_FINISHED) != null)
			{
				final L2PcInstance player = target.getActingPlayer();
				for (final Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_SPELL_FINISHED))
				{
					quest.notifySpellFinished(this, player, skill);
				}
			}
		}
		catch (final Exception e)
		{
			_log.severe("" + e);
		}
	}
}