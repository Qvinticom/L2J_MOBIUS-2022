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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.CharStatsTable;
import org.l2jmobius.gameserver.data.CharTemplateTable;
import org.l2jmobius.gameserver.data.ExperienceTable;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.data.LevelUpData;
import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.enums.CreatureState;
import org.l2jmobius.gameserver.handler.ISkillHandler;
import org.l2jmobius.gameserver.handler.SkillHandler;
import org.l2jmobius.gameserver.managers.GmListManager;
import org.l2jmobius.gameserver.model.Clan;
import org.l2jmobius.gameserver.model.Inventory;
import org.l2jmobius.gameserver.model.LvlupData;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.ShortCut;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.StatModifiers;
import org.l2jmobius.gameserver.model.TradeItem;
import org.l2jmobius.gameserver.model.TradeList;
import org.l2jmobius.gameserver.model.Warehouse;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.network.Connection;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.ChangeWaitType;
import org.l2jmobius.gameserver.network.serverpackets.CharInfo;
import org.l2jmobius.gameserver.network.serverpackets.CharMoveToLocation;
import org.l2jmobius.gameserver.network.serverpackets.DeleteObject;
import org.l2jmobius.gameserver.network.serverpackets.GetItem;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.ItemList;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillCanceld;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillLaunched;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUser;
import org.l2jmobius.gameserver.network.serverpackets.MoveToPawn;
import org.l2jmobius.gameserver.network.serverpackets.MyTargetSelected;
import org.l2jmobius.gameserver.network.serverpackets.NpcInfo;
import org.l2jmobius.gameserver.network.serverpackets.PartySmallWindowUpdate;
import org.l2jmobius.gameserver.network.serverpackets.PrivateBuyListBuy;
import org.l2jmobius.gameserver.network.serverpackets.PrivateBuyListSell;
import org.l2jmobius.gameserver.network.serverpackets.PrivateStoreMsgSell;
import org.l2jmobius.gameserver.network.serverpackets.ServerBasePacket;
import org.l2jmobius.gameserver.network.serverpackets.SetupGauge;
import org.l2jmobius.gameserver.network.serverpackets.SpawnItem;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.StopMove;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;
import org.l2jmobius.gameserver.templates.Armor;
import org.l2jmobius.gameserver.templates.CharTemplate;
import org.l2jmobius.gameserver.templates.Item;
import org.l2jmobius.gameserver.templates.Weapon;
import org.l2jmobius.gameserver.threadpool.ThreadPool;
import org.l2jmobius.util.Rnd;

public class PlayerInstance extends Creature
{
	private static Logger _log = Logger.getLogger(PlayerInstance.class.getName());
	
	private final Map<Integer, Boolean> _disabledSkills = new ConcurrentHashMap<>();
	private volatile boolean _allSkillsDisabled = false;
	private Connection _netConnection;
	private int _charId = 199546;
	private int _canCraft = 0;
	private int _exp;
	private int _sp;
	private int _karma;
	private int _pvpKills;
	private int _pkKills;
	private int _pvpFlag = 0;
	private long _lastPvpTime;
	private ScheduledFuture<?> _pvpTask;
	private int _maxLoad;
	private int _race;
	private int _classId;
	private int _deleteTimer;
	private final Inventory _inventory = new Inventory();
	private final Warehouse _warehouse = new Warehouse();
	private int _moveType = 1;
	private int _waitType = 1;
	private int _crestId;
	private final Map<Integer, Skill> _skills = new HashMap<>();
	private Skill _skill;
	private final Map<Integer, ShortCut> _shortCuts = new TreeMap<>();
	private int _allyId = 0;
	private TradeList _tradeList;
	private List<TradeItem> _sellList;
	private List<TradeItem> _buyList;
	private int _privatestore = 0;
	private PetInstance _pet = null;
	private boolean _partyMatchingAutomaticRegistration;
	private boolean _partyMatchingShowLevel;
	private boolean _partyMatchingShowClass;
	private String _partyMatchingMemo;
	private Party _party = null;
	private int _clanId = 0;
	private Clan _clan;
	private boolean _clanLeader;
	private boolean _isInvul = false;
	private boolean _isGm = false;
	private PlayerInstance _currentTransactionRequester;
	private ItemInstance _arrowItem;
	private Weapon _fistsWeaponItem;
	private long _uptime;
	public byte updateKnownCounter = 0;
	private Creature _interactTarget;
	private ScheduledFuture<?> _waterTask;
	
	public Skill addSkill(Skill newSkill)
	{
		return _skills.put(newSkill.getId(), newSkill);
	}
	
	public Skill removeSkill(Skill skill)
	{
		return _skills.remove(skill.getId());
	}
	
	public Collection<Skill> getAllSkills()
	{
		return _skills.values();
	}
	
	public Collection<ShortCut> getAllShortCuts()
	{
		return _shortCuts.values();
	}
	
	public ShortCut getShortCut(int slot)
	{
		return _shortCuts.get(slot);
	}
	
	public void registerShortCut(ShortCut shortcut)
	{
		_shortCuts.put(shortcut.getSlot(), shortcut);
	}
	
	public void deleteShortCut(int slot)
	{
		_shortCuts.remove(slot);
	}
	
	public int getSkillLevel(int skillId)
	{
		final Skill skill = _skills.get(skillId);
		if (skill == null)
		{
			return -1;
		}
		return skill.getLevel();
	}
	
	public int getCanCraft()
	{
		return _canCraft;
	}
	
	public void setCanCraft(int canCraft)
	{
		_canCraft = canCraft;
	}
	
	public int getPkKills()
	{
		return _pkKills;
	}
	
	public void setPkKills(int pkKills)
	{
		_pkKills = pkKills;
	}
	
	public int getDeleteTimer()
	{
		return _deleteTimer;
	}
	
	public void setDeleteTimer(int deleteTimer)
	{
		_deleteTimer = deleteTimer;
	}
	
	public int getCurrentLoad()
	{
		return _inventory.getTotalWeight();
	}
	
	public int getKarma()
	{
		return _karma;
	}
	
	public void setKarma(int karma)
	{
		_karma = karma;
	}
	
	public int getMaxLoad()
	{
		return _maxLoad;
	}
	
	public void setMaxLoad(int maxLoad)
	{
		_maxLoad = maxLoad;
	}
	
	public int getPvpKills()
	{
		return _pvpKills;
	}
	
	public void setPvpKills(int pvpKills)
	{
		_pvpKills = pvpKills;
	}
	
	public int getClassId()
	{
		return _classId;
	}
	
	public void setClassId(int classId)
	{
		_classId = classId;
	}
	
	public int getExp()
	{
		return _exp;
	}
	
	public Skill getSkill()
	{
		return _skill;
	}
	
	public void setSkill(Skill skill)
	{
		_skill = skill;
	}
	
	public void setFistsWeaponItem(Weapon weaponItem)
	{
		_fistsWeaponItem = weaponItem;
	}
	
	public Weapon getFistsWeaponItem()
	{
		return _fistsWeaponItem;
	}
	
	public Weapon findFistsWeaponItem(int classId)
	{
		Weapon weaponItem = null;
		if ((classId >= 0) && (classId <= 9))
		{
			final Item temp = ItemTable.getInstance().getTemplate(246);
			weaponItem = (Weapon) temp;
		}
		else if ((classId >= 10) && (classId <= 17))
		{
			final Item temp = ItemTable.getInstance().getTemplate(251);
			weaponItem = (Weapon) temp;
		}
		else if ((classId >= 18) && (classId <= 24))
		{
			final Item temp = ItemTable.getInstance().getTemplate(244);
			weaponItem = (Weapon) temp;
		}
		else if ((classId >= 25) && (classId <= 30))
		{
			final Item temp = ItemTable.getInstance().getTemplate(249);
			weaponItem = (Weapon) temp;
		}
		else if ((classId >= 31) && (classId <= 37))
		{
			final Item temp = ItemTable.getInstance().getTemplate(245);
			weaponItem = (Weapon) temp;
		}
		else if ((classId >= 38) && (classId <= 43))
		{
			final Item temp = ItemTable.getInstance().getTemplate(250);
			weaponItem = (Weapon) temp;
		}
		else if ((classId >= 44) && (classId <= 48))
		{
			final Item temp = ItemTable.getInstance().getTemplate(248);
			weaponItem = (Weapon) temp;
		}
		else if ((classId >= 49) && (classId <= 52))
		{
			final Item temp = ItemTable.getInstance().getTemplate(252);
			weaponItem = (Weapon) temp;
		}
		else if ((classId >= 53) && (classId <= 57))
		{
			final Item temp = ItemTable.getInstance().getTemplate(247);
			weaponItem = (Weapon) temp;
		}
		return weaponItem;
	}
	
	@Override
	public void addExpAndSp(int addToExp, int addToSp)
	{
		_exp += addToExp;
		_sp += addToSp;
		final StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.EXP, _exp);
		su.addAttribute(StatusUpdate.SP, _sp);
		sendPacket(su);
		final SystemMessage sm = new SystemMessage(SystemMessage.YOU_EARNED_S1_EXP_AND_S2_SP);
		sm.addNumber(addToExp);
		sm.addNumber(addToSp);
		sendPacket(sm);
		while (_exp >= ExperienceTable.getInstance().getExp(getLevel() + 1))
		{
			increaseLevel();
			rewardSkills();
			updatePAtk();
			updateMAtk();
			updatePDef();
			updateMDef();
			sendPacket(new UserInfo(this));
		}
	}
	
	private void rewardSkills()
	{
		// int charclass = this.getClassId();
		final int lvl = getLevel();
		if (lvl == 5)
		{
			final Skill skill = SkillTable.getInstance().getInfo(194, 1);
			removeSkill(skill);
		}
		else if (lvl == 20)
		{
			final Skill skill = SkillTable.getInstance().getInfo(239, 1);
			addSkill(skill);
		}
		else if (lvl == 40)
		{
			final Skill skill = SkillTable.getInstance().getInfo(239, 2);
			addSkill(skill);
		}
		else if (lvl == 52)
		{
			final Skill skill = SkillTable.getInstance().getInfo(239, 3);
			addSkill(skill);
		}
	}
	
	public void setExp(int exp)
	{
		_exp = exp;
	}
	
	public int getRace()
	{
		return _race;
	}
	
	public void setRace(int race)
	{
		_race = race;
	}
	
	public int getSp()
	{
		return _sp;
	}
	
	public void setSp(int sp)
	{
		_sp = sp;
	}
	
	public int getPvpFlag()
	{
		return _pvpFlag;
	}
	
	private void stopPvPFlag()
	{
		if (_pvpTask != null)
		{
			_pvpTask.cancel(true);
			_pvpTask = null;
		}
		updatePvPFlag(0);
	}
	
	public void updatePvPFlag(int value)
	{
		if (_karma > 0)
		{
			return;
		}
		if (value == 1)
		{
			if (_pvpTask == null)
			{
				_pvpTask = ThreadPool.scheduleAtFixedRate(new pvpTask(), 1000, 1000);
			}
			_lastPvpTime = System.currentTimeMillis() + 30000;
		}
		if (_pvpFlag == value)
		{
			return;
		}
		_pvpFlag = value;
		final UserInfo userInfo = new UserInfo(this);
		sendPacket(userInfo);
		broadcastPacket(userInfo);
	}
	
	class pvpTask implements Runnable
	{
		@Override
		public void run()
		{
			final long currentTime = System.currentTimeMillis();
			if (currentTime > _lastPvpTime)
			{
				stopPvPFlag();
			}
			else if (currentTime > (_lastPvpTime - 5000))
			{
				updatePvPFlag(2);
			}
		}
	}
	
	public boolean isEnemy(WorldObject target)
	{
		if ((target == null) || (target == this))
		{
			return false;
		}
		
		final PlayerInstance targetPlayer = target.getActingPlayer();
		if ((_party != null) && (_party == targetPlayer.getParty()))
		{
			return false;
		}
		if ((_clanId != 0) && (_clanId == targetPlayer.getClanId()))
		{
			return false;
		}
		if ((_allyId != 0) && (_allyId == targetPlayer.getAllyId()))
		{
			return false;
		}
		
		return true;
	}
	
	public int getClanId()
	{
		return _clanId;
	}
	
	public void setClanId(int clanId)
	{
		_clanId = clanId;
	}
	
	public Inventory getInventory()
	{
		return _inventory;
	}
	
	public int getMoveType()
	{
		return _moveType;
	}
	
	public void setMoveType(int moveType)
	{
		_moveType = moveType;
		setRunning(_moveType == 1);
	}
	
	public int getWaitType()
	{
		return _waitType;
	}
	
	public void setWaitType(int waitType)
	{
		_waitType = waitType;
	}
	
	public Warehouse getWarehouse()
	{
		return _warehouse;
	}
	
	public int getCharId()
	{
		return _charId;
	}
	
	public void setCharId(int charId)
	{
		_charId = charId;
	}
	
	public int getAdena()
	{
		return _inventory.getAdena();
	}
	
	public void reduceAdena(int adena)
	{
		_inventory.reduceAdena(adena);
	}
	
	public void addAdena(int adena)
	{
		_inventory.addAdena(adena);
	}
	
	public Connection getNetConnection()
	{
		return _netConnection;
	}
	
	public void setNetConnection(Connection connection)
	{
		_netConnection = connection;
	}
	
	public int getCrestId()
	{
		return _crestId;
	}
	
	public void setCrestId(int crestId)
	{
		_crestId = crestId;
	}
	
	@Override
	public void onAction(PlayerInstance player)
	{
		if (player.getTarget() != this)
		{
			player.setTarget(this);
			final MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
		}
		else
		{
			player.setCurrentState(CreatureState.FOLLOW);
			if (getPrivateStoreType() != 0)
			{
				player.setCurrentState(CreatureState.INTERACT);
			}
			player.moveTo(getX(), getY(), getZ(), 36);
		}
	}
	
	@Override
	public void broadcastStatusUpdate()
	{
		final StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
		su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
		super.broadcastStatusUpdate();
		if (getNetConnection() != null)
		{
			sendPacket(su);
		}
		if (isInParty())
		{
			final PartySmallWindowUpdate update = new PartySmallWindowUpdate(this);
			getParty().broadcastToPartyMembers(this, update);
		}
	}
	
	public int getAllyId()
	{
		return _allyId;
	}
	
	public void setAllyId(int allyId)
	{
		_allyId = allyId;
	}
	
	@Override
	protected void onHitTimer(Creature target, int damage, boolean crit, boolean miss, boolean soulshot)
	{
		final boolean isEnemy = isEnemy(target);
		if (isEnemy)
		{
			if (target.getActingPlayer() != null)
			{
				updatePvPFlag(1);
			}
		}
		else // TODO: Target handlers.
		{
			return;
		}
		super.onHitTimer(target, damage, crit, miss, soulshot);
	}
	
	@Override
	protected void displayHitMessage(int damage, boolean crit, boolean miss)
	{
		if (crit)
		{
			sendPacket(new SystemMessage(SystemMessage.CRITICAL_HIT));
		}
		if (miss)
		{
			sendPacket(new SystemMessage(SystemMessage.MISSED_TARGET));
		}
		else
		{
			final SystemMessage sm = new SystemMessage(SystemMessage.YOU_DID_S1_DMG);
			sm.addNumber(damage);
			sendPacket(sm);
		}
	}
	
	@Override
	public void sendPacket(ServerBasePacket packet)
	{
		try
		{
			getNetConnection().sendPacket(packet);
		}
		catch (Exception e)
		{
			// empty catch block
		}
	}
	
	public void sendMessage(String message)
	{
		final SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
		sm.addString(message);
		sendPacket(sm);
	}
	
	@Override
	protected void startCombat()
	{
		final Creature target = (Creature) getTarget();
		if (target == null)
		{
			_log.warning("Failed to start combat without target.");
			sendPacket(new ActionFailed());
		}
		else if (getAttackRange() < getDistance(target.getX(), target.getY()))
		{
			sendPacket(new ActionFailed());
		}
		else
		{
			super.startCombat();
		}
	}
	
	@Override
	public void onTargetReached()
	{
		super.onTargetReached();
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
				case CASTING:
				{
					useMagic(_skill);
					break; // Use a fallthrou?
				}
				case INTERACT:
				{
					if (getTarget() instanceof PlayerInstance)
					{
						final PlayerInstance temp = (PlayerInstance) getTarget();
						sendPacket(new ActionFailed());
						if (temp.getPrivateStoreType() == 1)
						{
							sendPacket(new PrivateBuyListSell(this, temp));
						}
						if (temp.getPrivateStoreType() == 3)
						{
							sendPacket(new PrivateBuyListBuy(temp, this));
						}
						setCurrentState(CreatureState.IDLE);
						break;
					}
					if (_interactTarget == null)
					{
						break;
					}
					_interactTarget.onAction(this);
				}
			}
		}
		catch (Exception io)
		{
			io.printStackTrace();
		}
	}
	
	private void doPickupItem()
	{
		setCurrentState(CreatureState.IDLE);
		if (!(getTarget() instanceof ItemInstance))
		{
			_log.warning("trying to pickup wrong target." + getTarget());
			return;
		}
		final ItemInstance target = (ItemInstance) getTarget();
		sendPacket(new ActionFailed());
		final StopMove sm = new StopMove(getObjectId(), getX(), getY(), getZ(), getHeading());
		sendPacket(sm);
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
			return;
		}
		World.getInstance().removeVisibleObject(target);
		final GetItem gi = new GetItem(target, getObjectId());
		sendPacket(gi);
		broadcastPacket(gi);
		final DeleteObject del = new DeleteObject(target);
		sendPacket(del);
		broadcastPacket(del);
		if (!isInParty())
		{
			SystemMessage smsg;
			if (target.getItemId() == 57)
			{
				smsg = new SystemMessage(SystemMessage.YOU_PICKED_UP_S1_ADENA);
				smsg.addNumber(target.getCount());
				sendPacket(smsg);
			}
			else
			{
				smsg = new SystemMessage(SystemMessage.YOU_PICKED_UP_S1_S2);
				smsg.addNumber(target.getCount());
				smsg.addItemName(target.getItemId());
				sendPacket(smsg);
			}
			final ItemInstance target2 = getInventory().addItem(target);
			final InventoryUpdate iu = new InventoryUpdate();
			if (target2.getLastChange() == 1)
			{
				iu.addNewItem(target);
			}
			else
			{
				iu.addModifiedItem(target2);
			}
			sendPacket(iu);
			final UserInfo ci = new UserInfo(this);
			sendPacket(ci);
		}
		else if (target.getItemId() == 57)
		{
			getParty().distributeAdena(target);
		}
		else
		{
			getParty().distributeItem(this, target);
		}
	}
	
	@Override
	public void setTarget(WorldObject newTarget)
	{
		WorldObject oldTarget;
		if (getCurrentState() == CreatureState.CASTING)
		{
			cancelCastMagic();
		}
		if ((oldTarget = getTarget()) != null)
		{
			if (oldTarget.equals(newTarget))
			{
				return;
			}
			if (oldTarget instanceof Creature)
			{
				((Creature) oldTarget).removeStatusListener(this);
			}
		}
		if (newTarget instanceof Creature)
		{
			((Creature) newTarget).addStatusListener(this);
		}
		super.setTarget(newTarget);
	}
	
	@Override
	public Weapon getActiveWeapon()
	{
		final ItemInstance weapon = getInventory().getPaperdollItem(7);
		if (weapon == null)
		{
			return getFistsWeaponItem();
		}
		return (Weapon) weapon.getItem();
	}
	
	@Override
	public void reduceCurrentHp(int amount, Creature attacker)
	{
		reduceCurrentHp(amount, attacker, true);
	}
	
	public void reduceCurrentHp(int amount, Creature attacker, boolean sendMessage)
	{
		if (isInvul())
		{
			return;
		}
		
		super.reduceCurrentHp(amount, attacker);
		if (isDead() && (getPet() != null))
		{
			getPet().unSummon(this);
		}
		
		if (attacker == null)
		{
			return;
		}
		
		// Damage message.
		if (sendMessage)
		{
			final SystemMessage msg = new SystemMessage(SystemMessage.S1_GAVE_YOU_S2_DMG);
			if ((attacker instanceof MonsterInstance) || (attacker instanceof NpcInstance))
			{
				final int mobId = ((NpcInstance) attacker).getNpcTemplate().getNpcId();
				msg.addNpcName(mobId);
			}
			else
			{
				msg.addString(attacker.getName());
			}
			msg.addNumber(amount);
			sendPacket(msg);
		}
		
		// Dead check.
		if (!isDead())
		{
			return;
		}
		
		// Stop water task.
		stopWaterTask();
		
		// Calculate Karma lost.
		if (getKarma() > 0)
		{
			for (ItemInstance item : getInventory().getItems())
			{
				if (Config.KARMA_PROTECTED_ITEMS.contains(item.getItemId()))
				{
					continue;
				}
				if (Rnd.get(100) < Config.KARMA_DROP_CHANCE)
				{
					getInventory().dropItem(item, 1);
				}
			}
			
			// Not self inflicted damage.
			if (attacker != this)
			{
				decreaseKarma();
			}
		}
		
		// Died from player.
		final PlayerInstance killer = attacker.getActingPlayer();
		if ((killer != null) && (killer != this))
		{
			if (_pvpFlag > 0)
			{
				killer.setPvpKills(killer.getPvpKills() + 1);
				killer.sendPacket(new UserInfo(killer));
			}
			else if (_karma == 0)
			{
				killer.increasePkKillsAndKarma(getLevel());
			}
		}
	}
	
	public void decreaseKarma()
	{
		float karmaLost = _karma / (getLevel() * 10);
		if (karmaLost < 0)
		{
			karmaLost = 1;
		}
		karmaLost *= Config.KARMA_LOST_MULTIPLIER;
		
		if (_karma > karmaLost)
		{
			setKarma(_karma - (int) karmaLost);
		}
		else
		{
			setKarma(0);
		}
		
		final UserInfo userInfo = new UserInfo(this);
		sendPacket(userInfo);
		broadcastPacket(userInfo);
	}
	
	public void increasePkKillsAndKarma(int level)
	{
		int newKarma = Config.KARMA_MIN_KARMA;
		final int pkLevel = getLevel();
		
		final int pkPKCount = getPkKills();
		int pkCountMulti = 0;
		if (pkPKCount > 0)
		{
			pkCountMulti = pkPKCount / 2;
		}
		else
		{
			pkCountMulti = 1;
		}
		if (pkCountMulti < 1)
		{
			pkCountMulti = 1;
		}
		newKarma *= pkCountMulti;
		
		int lvlDiffMulti = 0;
		if (pkLevel > level)
		{
			lvlDiffMulti = pkLevel / level;
		}
		else
		{
			lvlDiffMulti = 1;
		}
		if (lvlDiffMulti < 1)
		{
			lvlDiffMulti = 1;
		}
		newKarma *= lvlDiffMulti;
		
		if (newKarma < Config.KARMA_MIN_KARMA)
		{
			newKarma = Config.KARMA_MIN_KARMA;
		}
		if (newKarma > Config.KARMA_MAX_KARMA)
		{
			newKarma = Config.KARMA_MAX_KARMA;
		}
		if (_karma > (Integer.MAX_VALUE - newKarma))
		{
			newKarma = Integer.MAX_VALUE - getKarma();
		}
		
		stopPvPFlag();
		
		setPkKills(_pkKills + 1);
		setKarma(_karma + newKarma);
		
		final UserInfo userInfo = new UserInfo(this);
		sendPacket(userInfo);
		broadcastPacket(userInfo);
	}
	
	public void setPartyMatchingAutomaticRegistration(boolean b)
	{
		_partyMatchingAutomaticRegistration = b;
	}
	
	public void setPartyMatchingShowLevel(boolean b)
	{
		_partyMatchingShowLevel = b;
	}
	
	public void setPartyMatchingShowClass(boolean b)
	{
		_partyMatchingShowClass = b;
	}
	
	public void setPartyMatchingMemo(String memo)
	{
		_partyMatchingMemo = memo;
	}
	
	public boolean isPartyMatchingAutomaticRegistration()
	{
		return _partyMatchingAutomaticRegistration;
	}
	
	public String getPartyMatchingMemo()
	{
		return _partyMatchingMemo;
	}
	
	public boolean isPartyMatchingShowClass()
	{
		return _partyMatchingShowClass;
	}
	
	public boolean isPartyMatchingShowLevel()
	{
		return _partyMatchingShowLevel;
	}
	
	public void setTransactionRequester(PlayerInstance requestor)
	{
		_currentTransactionRequester = requestor;
	}
	
	public PlayerInstance getTransactionRequester()
	{
		return _currentTransactionRequester;
	}
	
	public boolean isTransactionInProgress()
	{
		return _currentTransactionRequester != null;
	}
	
	public void addKnownObjectWithoutCreate(WorldObject object)
	{
		super.addKnownObject(object);
	}
	
	@Override
	public void addKnownObject(WorldObject object)
	{
		NpcInfo ni;
		super.addKnownObject(object);
		if (object instanceof ItemInstance)
		{
			final SpawnItem si = new SpawnItem((ItemInstance) object);
			sendPacket(si);
		}
		else if (object instanceof NpcInstance)
		{
			ni = new NpcInfo((NpcInstance) object);
			sendPacket(ni);
		}
		else if (object instanceof PetInstance)
		{
			ni = new NpcInfo((PetInstance) object);
			sendPacket(ni);
		}
		else if (object instanceof PlayerInstance)
		{
			final PlayerInstance otherPlayer = (PlayerInstance) object;
			sendPacket(new CharInfo(otherPlayer));
			if ((otherPlayer.getPrivateStoreType() == 1) || (otherPlayer.getPrivateStoreType() == 3))
			{
				sendPacket(new ChangeWaitType(otherPlayer, 0));
				sendPacket(new UserInfo(otherPlayer));
				sendPacket(new PrivateStoreMsgSell(otherPlayer));
			}
		}
		if (object instanceof Creature)
		{
			final Creature obj = (Creature) object;
			if (obj.isMoving())
			{
				final CharMoveToLocation mov = new CharMoveToLocation(obj);
				sendPacket(mov);
			}
			else if (obj.isMovingToPawn())
			{
				final MoveToPawn mov = new MoveToPawn(obj, obj.getPawnTarget(), obj.getPawnOffset());
				sendPacket(mov);
			}
		}
	}
	
	@Override
	public void removeKnownObject(WorldObject object)
	{
		super.removeKnownObject(object);
		sendPacket(new DeleteObject(object));
	}
	
	@Override
	public void increaseLevel()
	{
		super.increaseLevel();
		final LvlupData lvlData = LevelUpData.getInstance().getTemplate(getClassId());
		if (lvlData != null)
		{
			final double hp1 = (getLevel() - 1) * lvlData.getDefaulthpadd();
			final double hp2 = (getLevel() - 2) * lvlData.getDefaulthpbonus();
			final double mp1 = (getLevel() - 1) * lvlData.getDefaultmpadd();
			final double mp2 = (getLevel() - 2) * lvlData.getDefaultmpbonus();
			final int newhp = (int) Math.rint(lvlData.getDefaulthp() + hp1 + hp2);
			final int newmp = (int) Math.rint(lvlData.getDefaultmp() + mp1 + mp2);
			setMaxHp(newhp);
			setCurrentHp(newhp);
			final StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.MAX_HP, newhp);
			su.addAttribute(StatusUpdate.CUR_HP, newhp);
			setMaxMp(newmp);
			setCurrentMp(newmp);
			su.addAttribute(StatusUpdate.MAX_MP, newmp);
			su.addAttribute(StatusUpdate.CUR_MP, newmp);
			sendPacket(su);
			if (isInParty())
			{
				getParty().recalculatePartyLevel();
			}
		}
		else
		{
			_log.warning("No lvl up data for class id: " + getClassId());
		}
	}
	
	@Override
	public int getAttackRange()
	{
		final Weapon weapon = getActiveWeapon();
		if ((weapon != null) && (weapon.getWeaponType() == Weapon.WEAPON_TYPE_BOW))
		{
			return 500;
		}
		return 36;
	}
	
	public void stopAllTimers()
	{
		stopAttackTask();
		stopHitTask();
		stopMpRegeneration();
		stopHpRegeneration();
		stopMove();
	}
	
	public PetInstance getPet()
	{
		return _pet;
	}
	
	public void setPet(PetInstance pet)
	{
		_pet = pet;
	}
	
	public void deleteMe()
	{
		stopAllTimers();
		setTarget(null);
		final World world = World.getInstance();
		world.removeVisibleObject(this);
		if (isInParty())
		{
			leaveParty();
		}
		if (getPet() != null)
		{
			getPet().unSummon(this);
		}
		if (getClanId() != 0)
		{
			getClan().getClanMember(getName()).setPlayerInstance(null);
		}
		if (getTransactionRequester() != null)
		{
			setTransactionRequester(null);
		}
		if (isGM())
		{
			GmListManager.getInstance().deleteGm(this);
		}
		removeAllKnownObjects();
		setNetConnection(null);
		world.removeObject(this);
	}
	
	public void updatePAtk()
	{
		final double lvlmod = (89.0 + getLevel()) / 100.0;
		final StatModifiers modifier = CharStatsTable.getInstance().getTemplate(getClassId());
		double strmod = 1.0;
		if (modifier != null)
		{
			strmod = (100.0 + modifier.getModstr()) / 100.0;
		}
		else
		{
			_log.warning("Missing STR modifier for classId: " + getClassId());
		}
		final CharTemplate template = CharTemplateTable.getInstance().getTemplate(getClassId());
		double weapondmg = 1.0;
		if (template != null)
		{
			weapondmg = template.getPatk();
		}
		else
		{
			_log.warning("Missing template for classId: " + getClassId());
		}
		final Weapon weapon = getActiveWeapon();
		if (weapon != null)
		{
			weapondmg += weapon.getPDamage();
		}
		final double pAtk = weapondmg * lvlmod * strmod;
		setPhysicalAttack((int) Math.rint(pAtk));
	}
	
	public void updatePDef()
	{
		Armor armorPiece;
		final double lvlmod = (89.0 + getLevel()) / 100.0;
		final CharTemplate template = CharTemplateTable.getInstance().getTemplate(getClassId());
		double totalItemDef = 40.0;
		if (template != null)
		{
			totalItemDef = template.getPdef();
		}
		else
		{
			_log.warning("Missing template for classId: " + getClassId());
		}
		ItemInstance dummy = getInventory().getPaperdollItem(13);
		if (dummy != null)
		{
			armorPiece = (Armor) dummy.getItem();
			totalItemDef += armorPiece.getPDef();
		}
		if ((dummy = getInventory().getPaperdollItem(10)) != null)
		{
			armorPiece = (Armor) dummy.getItem();
			totalItemDef += armorPiece.getPDef();
		}
		if ((dummy = getInventory().getPaperdollItem(12)) != null)
		{
			armorPiece = (Armor) dummy.getItem();
			totalItemDef += armorPiece.getPDef();
		}
		if ((dummy = getInventory().getPaperdollItem(9)) != null)
		{
			armorPiece = (Armor) dummy.getItem();
			totalItemDef += armorPiece.getPDef();
		}
		if ((dummy = getInventory().getPaperdollItem(6)) != null)
		{
			armorPiece = (Armor) dummy.getItem();
			totalItemDef += armorPiece.getPDef();
		}
		if ((dummy = getInventory().getPaperdollItem(11)) != null)
		{
			armorPiece = (Armor) dummy.getItem();
			totalItemDef += armorPiece.getPDef();
		}
		if ((dummy = getInventory().getPaperdollItem(0)) != null)
		{
			armorPiece = (Armor) dummy.getItem();
			totalItemDef += armorPiece.getPDef();
		}
		final double pDef = totalItemDef * lvlmod;
		setPhysicalDefense((int) Math.round(pDef));
	}
	
	public void updateMAtk()
	{
		final double lmod = (89.0 + getLevel()) / 100.0;
		final double lvlmod = Math.sqrt(lmod);
		final StatModifiers modifier = CharStatsTable.getInstance().getTemplate(getClassId());
		double imod = 1.0;
		if (modifier != null)
		{
			imod = (100.0 + modifier.getModint()) / 100.0;
		}
		else
		{
			_log.warning("Missing INT modifier for classId: " + getClassId());
		}
		final double intmod = Math.sqrt(imod);
		final CharTemplate template = CharTemplateTable.getInstance().getTemplate(getClassId());
		double weapondmg = 1.0;
		if (template != null)
		{
			weapondmg = template.getMatk();
		}
		else
		{
			_log.warning("Missing template for classId: " + getClassId());
		}
		final Weapon weapon = getActiveWeapon();
		if (weapon != null)
		{
			weapondmg += weapon.getMDamage();
		}
		final double mAtk = weapondmg * lvlmod * intmod;
		setMagicalAttack((int) Math.rint(mAtk));
	}
	
	public void updateMDef()
	{
		Armor armorPiece;
		final double lvlBonus = (89.0 + getLevel()) / 100.0;
		final StatModifiers modifier = CharStatsTable.getInstance().getTemplate(getClassId());
		double menBonus = 1.0;
		if (modifier != null)
		{
			menBonus = (100.0 + modifier.getModmen()) / 100.0;
		}
		else
		{
			_log.warning("Missing MEN modifier for classId: " + getClassId());
		}
		final CharTemplate template = CharTemplateTable.getInstance().getTemplate(getClassId());
		double totalItemDef = 40.0;
		if (template != null)
		{
			totalItemDef = template.getMdef();
		}
		else
		{
			_log.warning("Missing template for classId: " + getClassId());
		}
		ItemInstance dummy = getInventory().getPaperdollItem(1);
		if (dummy != null)
		{
			armorPiece = (Armor) dummy.getItem();
			totalItemDef += armorPiece.getMDef();
		}
		if ((dummy = getInventory().getPaperdollItem(4)) != null)
		{
			armorPiece = (Armor) dummy.getItem();
			totalItemDef += armorPiece.getMDef();
		}
		if ((dummy = getInventory().getPaperdollItem(3)) != null)
		{
			armorPiece = (Armor) dummy.getItem();
			totalItemDef += armorPiece.getMDef();
		}
		if ((dummy = getInventory().getPaperdollItem(2)) != null)
		{
			armorPiece = (Armor) dummy.getItem();
			totalItemDef += armorPiece.getMDef();
		}
		if ((dummy = getInventory().getPaperdollItem(5)) != null)
		{
			armorPiece = (Armor) dummy.getItem();
			totalItemDef += armorPiece.getMDef();
		}
		final double mDef = totalItemDef * lvlBonus * menBonus;
		setMagicalDefense((int) Math.round(mDef));
	}
	
	public void setTradeList(TradeList x)
	{
		_tradeList = x;
	}
	
	public TradeList getTradeList()
	{
		return _tradeList;
	}
	
	public void setSellList(List<TradeItem> x)
	{
		_sellList = x;
	}
	
	public List<TradeItem> getSellList()
	{
		return _sellList;
	}
	
	public void setBuyList(List<TradeItem> x)
	{
		_buyList = x;
	}
	
	public List<TradeItem> getBuyList()
	{
		return _buyList;
	}
	
	public void setPrivateStoreType(int type)
	{
		_privatestore = type;
	}
	
	public int getPrivateStoreType()
	{
		return _privatestore;
	}
	
	public void setClan(Clan clan)
	{
		_clan = clan;
	}
	
	public Clan getClan()
	{
		return _clan;
	}
	
	public void setIsClanLeader(boolean b)
	{
		_clanLeader = b;
	}
	
	public boolean isClanLeader()
	{
		return _clanLeader;
	}
	
	public void useMagic(Skill skill)
	{
		Creature target = null;
		target = getTarget() instanceof Creature ? (Creature) getTarget() : this;
		
		if ((skill.getTargetType() == Skill.TARGET_SELF) || (skill.getTargetType() == Skill.TARGET_PARTY))
		{
			target = this;
		}
		if ((skill.getTargetType() == Skill.TARGET_ONE) && (target == this))
		{
			return;
		}
		if (isDead() || target.isDead() || _allSkillsDisabled || isSkillDisabled(skill.getId()) || skill.isPassive())
		{
			return;
		}
		
		final int weaponType = getActiveWeapon().getWeaponType();
		final int skillId = skill.getId();
		if ((skillId == 56) && (weaponType != Weapon.WEAPON_TYPE_BOW))
		{
			return;
		}
		if ((skillId == 16) && (weaponType != Weapon.WEAPON_TYPE_DAGGER))
		{
			return;
		}
		if ((skillId == 3) && (weaponType != Weapon.WEAPON_TYPE_BLUNT) && (weaponType != Weapon.WEAPON_TYPE_SWORD))
		{
			return;
		}
		if ((skillId == 29) && (weaponType != Weapon.WEAPON_TYPE_DUALFIST))
		{
			return;
		}
		
		if (SkillHandler.getInstance().getSkillHandler(skill.getId()) == null)
		{
			sendMessage("This skill is not implemented yet.");
			return;
		}
		if (getCurrentMp() < skill.getMpConsume())
		{
			sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_MP));
			return;
		}
		if (getCurrentHp() < skill.getHpConsume())
		{
			sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_HP));
			return;
		}
		setCurrentState(CreatureState.CASTING);
		setSkill(skill);
		final double distance = getDistance(target.getX(), target.getY());
		if ((skill.getCastRange() > 0) && (distance > skill.getCastRange()))
		{
			moveTo(target.getX(), target.getY(), target.getZ(), skill.getCastRange());
			return;
		}
		final int magicId = skill.getId();
		final int level = getSkillLevel(magicId);
		final ActionFailed af = new ActionFailed();
		sendPacket(af);
		final MagicSkillUser msu = new MagicSkillUser(this, target, magicId, level, skill.getHitTime(), skill.getReuseDelay());
		sendPacket(msu);
		broadcastPacket(msu);
		sendPacket(new SetupGauge(SetupGauge.BLUE, skill.getHitTime()));
		final SystemMessage sm = new SystemMessage(SystemMessage.USE_S1);
		sm.addSkillName(magicId);
		sendPacket(sm);
		if (skill.getSkillTime() > 300)
		{
			disableSkill(skill.getId(), true);
			disableAllSkills();
			ThreadPool.schedule(new MagicUseTask(target, skill), skill.getHitTime());
			ThreadPool.schedule(new EnableSkill(skill.getId()), skill.getReuseDelay());
			ThreadPool.schedule(new EnableAllSkills(skill), skill.getSkillTime());
		}
	}
	
	private class MagicUseTask implements Runnable
	{
		Creature _target;
		Skill _skill;
		
		public MagicUseTask(Creature target, Skill skill)
		{
			_target = target;
			_skill = skill;
		}
		
		@Override
		public void run()
		{
			onMagicUseTimer(_target, _skill);
		}
	}
	
	private class EnableSkill implements Runnable
	{
		int _skillId;
		
		public EnableSkill(int skillId)
		{
			_skillId = skillId;
		}
		
		@Override
		public void run()
		{
			disableSkill(_skillId, false);
		}
	}
	
	private class EnableAllSkills implements Runnable
	{
		Skill _skill;
		
		public EnableAllSkills(Skill skill)
		{
			_skill = skill;
		}
		
		@Override
		public void run()
		{
			if (getSkill() == _skill)
			{
				enableAllSkills();
			}
		}
	}
	
	@Override
	protected void reduceArrowCount()
	{
		final ItemInstance arrows = getInventory().destroyItem(getInventory().getPaperdollObjectId(8), 1);
		// _log.info("arrow count:" + arrows.getCount());
		if (arrows.getCount() == 0)
		{
			getInventory().unEquipItemOnPaperdoll(8);
			_arrowItem = null;
			_log.info("removed arrows count");
			sendPacket(new ItemList(this, false));
		}
		else
		{
			final InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(arrows);
			sendPacket(iu);
		}
	}
	
	@Override
	protected boolean checkAndEquipArrows()
	{
		if (getInventory().getPaperdollItem(8) == null)
		{
			_arrowItem = getInventory().findArrowForBow(getActiveWeapon());
			if (_arrowItem != null)
			{
				getInventory().setPaperdollItem(8, _arrowItem);
				final ItemList il = new ItemList(this, false);
				sendPacket(il);
			}
		}
		else
		{
			_arrowItem = getInventory().getPaperdollItem(8);
		}
		return _arrowItem != null;
	}
	
	@Override
	protected boolean isUsingDualWeapon()
	{
		final Weapon weaponItem = getActiveWeapon();
		if (weaponItem.getWeaponType() == Weapon.WEAPON_TYPE_DUAL)
		{
			return true;
		}
		if (weaponItem.getWeaponType() == Weapon.WEAPON_TYPE_DUALFIST)
		{
			return true;
		}
		if (weaponItem.getItemId() == 248)
		{
			return true;
		}
		return weaponItem.getItemId() == 252;
	}
	
	public void setUptime(long time)
	{
		_uptime = time;
	}
	
	public long getUptime()
	{
		return System.currentTimeMillis() - _uptime;
	}
	
	public void onMagicUseTimer(Creature target, Skill skill)
	{
		if ((getCurrentState() == CreatureState.CASTING) && (getSkill() == skill))
		{
			final int magicId = skill.getId();
			final int level = getSkillLevel(magicId);
			if ((skill.getTargetType() == Skill.TARGET_PARTY) && isInParty())
			{
				for (PlayerInstance player : getParty().getPartyMembers())
				{
					final MagicSkillLaunched msl = new MagicSkillLaunched(this, magicId, level, player);
					sendPacket(msl);
					broadcastPacket(msl);
				}
			}
			else
			{
				final MagicSkillLaunched msl = new MagicSkillLaunched(this, magicId, level, target);
				sendPacket(msl);
				broadcastPacket(msl);
			}
			reduceCurrentMp(skill.getMpConsume());
			final StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
			sendPacket(su);
			final ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(skill.getId());
			if (handler == null)
			{
				_log.warning("No skillhandler registered for skillId: " + skill.getId());
			}
			else
			{
				handler.useSkill(this, skill, target);
			}
			setCurrentState(CreatureState.IDLE);
		}
	}
	
	public void disableSkill(int skillId, boolean state)
	{
		_disabledSkills.put(skillId, state);
	}
	
	public boolean isSkillDisabled(int skillId)
	{
		if (!_disabledSkills.containsKey(skillId))
		{
			return false;
		}
		return _disabledSkills.get(skillId);
	}
	
	public boolean isAllSkillsDisabled()
	{
		return _allSkillsDisabled;
	}
	
	public void disableAllSkills()
	{
		_allSkillsDisabled = true;
	}
	
	@Override
	public void enableAllSkills()
	{
		_allSkillsDisabled = false;
	}
	
	public void setIsInvul(boolean b)
	{
		_isInvul = b;
	}
	
	public boolean isInvul()
	{
		return _isInvul;
	}
	
	public boolean isInParty()
	{
		return _party != null;
	}
	
	public void setParty(Party party)
	{
		_party = party;
	}
	
	public void joinParty(Party party)
	{
		_party = party;
		_party.addPartyMember(this);
	}
	
	public void leaveParty()
	{
		if (isInParty())
		{
			_party.removePartyMember(this);
			_party = null;
		}
	}
	
	public Party getParty()
	{
		return _party;
	}
	
	public void setIsGM(boolean status)
	{
		_isGm = status;
	}
	
	public boolean isGM()
	{
		return _isGm;
	}
	
	public void setInteractTarget(Creature target)
	{
		_interactTarget = target;
	}
	
	public void cancelCastMagic()
	{
		setCurrentState(CreatureState.IDLE);
		enableAllSkills();
		final MagicSkillCanceld msc = new MagicSkillCanceld(getObjectId());
		sendPacket(msc);
		broadcastPacket(msc);
	}
	
	public void checkWaterState()
	{
		// Water level.
		if (getZ() < -3779) // TODO: Water zones.
		{
			// Banned "underwater" map regions.
			final int regionX = ((getX() - World.MAP_MIN_X) >> 15) + World.TILE_X_MIN;
			final int regionY = ((getY() - World.MAP_MIN_Y) >> 15) + World.TILE_Y_MIN;
			// TODO: Check for more?
			if (((regionX == 18) && (regionY == 19)) // School of Dark Arts
				|| ((regionX == 18) && (regionY == 23)) // Forgotten Temple
				|| ((regionX == 19) && (regionY == 23)) // Wastelands - Ant Nest
				|| ((regionX == 19) && (regionY == 24)) // Wastelands - Southern Entrance
				|| ((regionX == 20) && (regionY == 18)) // Dark Elf Village
				|| ((regionX == 20) && (regionY == 20)) // Elven Fortress
				|| ((regionX == 20) && (regionY == 21)) // Cruma Tower
				|| ((regionX == 21) && (regionY == 18)) // Sea of Spores
				|| ((regionX == 21) && (regionY == 25)) // Elven Ruins
				|| ((regionX == 22) && (regionY == 18)) // Ivory Tower
				|| ((regionX == 24) && (regionY == 21)) // Lair of Antharas
				|| ((regionX == 25) && (regionY == 12)) // Mithril Mines
				|| ((regionX == 25) && (regionY == 19)) // Giant's Cave
				|| ((regionX == 25) && (regionY == 21)) // Antharas Nest
			)
			{
				return;
			}
			
			startWaterTask();
		}
		else
		{
			stopWaterTask();
		}
	}
	
	public boolean isInWater()
	{
		return _waterTask != null;
	}
	
	private void startWaterTask()
	{
		if ((_waterTask == null) && !isDead())
		{
			_waterTask = ThreadPool.scheduleAtFixedRate(new waterTask(this), 86000, 1000);
			sendPacket(new SetupGauge(SetupGauge.CYAN, 86000));
		}
	}
	
	private void stopWaterTask()
	{
		if (_waterTask != null)
		{
			_waterTask.cancel(true);
			_waterTask = null;
			sendPacket(new SetupGauge(SetupGauge.CYAN, 0));
		}
	}
	
	class waterTask implements Runnable
	{
		private final PlayerInstance _player;
		
		public waterTask(PlayerInstance player)
		{
			_player = player;
		}
		
		@Override
		public void run()
		{
			int reduceHp = (int) (getMaxHp() / 100.0);
			if (reduceHp < 1)
			{
				reduceHp = 1;
			}
			
			reduceCurrentHp(reduceHp, _player, false);
			final SystemMessage sm = new SystemMessage(SystemMessage.DROWN_DAMAGE_S1);
			sm.addNumber(reduceHp);
			sendPacket(sm);
		}
	}
	
	@Override
	public boolean isPlayer()
	{
		return true;
	}
	
	@Override
	public PlayerInstance getActingPlayer()
	{
		return this;
	}
}
