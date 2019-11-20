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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.Connection;
import org.l2jmobius.gameserver.GmListTable;
import org.l2jmobius.gameserver.data.CharStatsTable;
import org.l2jmobius.gameserver.data.CharTemplateTable;
import org.l2jmobius.gameserver.data.ExperienceTable;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.data.LevelUpData;
import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.handler.ISkillHandler;
import org.l2jmobius.gameserver.handler.SkillHandler;
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
import org.l2jmobius.gameserver.templates.L2Armor;
import org.l2jmobius.gameserver.templates.L2CharTemplate;
import org.l2jmobius.gameserver.templates.L2Item;
import org.l2jmobius.gameserver.templates.L2Weapon;

public class PlayerInstance extends Creature
{
	private static Logger _log = Logger.getLogger(PlayerInstance.class.getName());
	private static Timer _magicUseTimer = new Timer(true);
	private static Timer _enableSkillTimer = new Timer(true);
	private static Timer _enableAllSkillsTimer = new Timer(true);
	private final Map<Integer, Boolean> _disabledSkills = new HashMap<>();
	private boolean _allSkillsDisabled = false;
	private Connection _netConnection;
	private int _charId = 199546;
	private int _canCraft = 0;
	private int _exp;
	private int _sp;
	private int _karma;
	private int _pvpKills;
	private int _pkKills;
	private int _pvpFlag;
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
	private int _allyId;
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
	private int _clanId;
	private Clan _clan;
	private boolean _clanLeader;
	private boolean _isInvul = false;
	private boolean _isGm = false;
	private PlayerInstance _currentTransactionRequester;
	private ItemInstance _arrowItem;
	private L2Weapon _fistsWeaponItem;
	private long _uptime;
	public byte updateKnownCounter = 0;
	private Creature _interactTarget;
	
	public Skill addSkill(Skill newSkill)
	{
		Skill oldSkill = _skills.put(newSkill.getId(), newSkill);
		return oldSkill;
	}
	
	public Skill removeSkill(Skill skill)
	{
		return _skills.remove(skill.getId());
	}
	
	public Skill[] getAllSkills()
	{
		return _skills.values().toArray(new Skill[_skills.values().size()]);
	}
	
	public ShortCut[] getAllShortCuts()
	{
		return _shortCuts.values().toArray(new ShortCut[_shortCuts.values().size()]);
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
		Skill skill = _skills.get(skillId);
		if (skill == null)
		{
			return -1;
		}
		return skill.getLevel();
	}
	
	public void setPvpFlag(int pvpFlag)
	{
		_pvpFlag = pvpFlag;
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
	
	public void setFistsWeaponItem(L2Weapon weaponItem)
	{
		_fistsWeaponItem = weaponItem;
	}
	
	public L2Weapon getFistsWeaponItem()
	{
		return _fistsWeaponItem;
	}
	
	public L2Weapon findFistsWeaponItem(int classId)
	{
		L2Weapon weaponItem = null;
		if ((classId >= 0) && (classId <= 9))
		{
			L2Item temp = ItemTable.getInstance().getTemplate(246);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 10) && (classId <= 17))
		{
			L2Item temp = ItemTable.getInstance().getTemplate(251);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 18) && (classId <= 24))
		{
			L2Item temp = ItemTable.getInstance().getTemplate(244);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 25) && (classId <= 30))
		{
			L2Item temp = ItemTable.getInstance().getTemplate(249);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 31) && (classId <= 37))
		{
			L2Item temp = ItemTable.getInstance().getTemplate(245);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 38) && (classId <= 43))
		{
			L2Item temp = ItemTable.getInstance().getTemplate(250);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 44) && (classId <= 48))
		{
			L2Item temp = ItemTable.getInstance().getTemplate(248);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 49) && (classId <= 52))
		{
			L2Item temp = ItemTable.getInstance().getTemplate(252);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 53) && (classId <= 57))
		{
			L2Item temp = ItemTable.getInstance().getTemplate(247);
			weaponItem = (L2Weapon) temp;
		}
		return weaponItem;
	}
	
	@Override
	public void addExpAndSp(int addToExp, int addToSp)
	{
		_log.fine("adding " + addToExp + " exp and " + addToSp + " sp to " + getName());
		_exp += addToExp;
		_sp += addToSp;
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.EXP, _exp);
		su.addAttribute(StatusUpdate.SP, _sp);
		sendPacket(su);
		SystemMessage sm = new SystemMessage(95);
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
		int lvl = getLevel();
		if (lvl == 5)
		{
			Skill skill = SkillTable.getInstance().getInfo(194, 1);
			removeSkill(skill);
			_log.fine("removed skill 'Lucky' from " + getName());
		}
		else if (lvl == 20)
		{
			Skill skill = SkillTable.getInstance().getInfo(239, 1);
			addSkill(skill);
			_log.fine("awarded " + getName() + " with expertise D.");
		}
		else if (lvl == 40)
		{
			Skill skill = SkillTable.getInstance().getInfo(239, 2);
			addSkill(skill);
			_log.fine("awarded " + getName() + " with expertise C.");
		}
		else if (lvl == 52)
		{
			Skill skill = SkillTable.getInstance().getInfo(239, 3);
			addSkill(skill);
			_log.fine("awarded " + getName() + " with expertise B.");
		}
		else
		{
			_log.fine("No skills awarded at lvl: " + lvl);
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
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
		}
		else
		{
			player.setCurrentState((byte) 8);
			if (getPrivateStoreType() != 0)
			{
				player.setCurrentState((byte) 7);
			}
			player.moveTo(getX(), getY(), getZ(), 36);
		}
	}
	
	@Override
	public void broadcastStatusUpdate()
	{
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
		su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
		super.broadcastStatusUpdate();
		if (getNetConnection() != null)
		{
			sendPacket(su);
		}
		if (isInParty())
		{
			PartySmallWindowUpdate update = new PartySmallWindowUpdate(this);
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
		super.onHitTimer(target, damage, crit, miss, soulshot);
	}
	
	@Override
	protected void displayHitMessage(int damage, boolean crit, boolean miss)
	{
		if (crit)
		{
			sendPacket(new SystemMessage(44));
		}
		if (miss)
		{
			sendPacket(new SystemMessage(43));
		}
		else
		{
			SystemMessage sm = new SystemMessage(35);
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
	
	@Override
	protected void startCombat()
	{
		Creature target = (Creature) getTarget();
		if (target == null)
		{
			_log.warning("failed to start combat without target.");
			sendPacket(new ActionFailed());
		}
		else if (getAttackRange() < getDistance(target.getX(), target.getY()))
		{
			sendPacket(new ActionFailed());
		}
		else
		{
			_log.fine("starting combat");
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
				case 1:
				{
					doPickupItem();
					break;
				}
				case 5:
				{
					startCombat();
					break;
				}
				case 2:
				{
					useMagic(_skill);
				}
				case 7:
				{
					if (getTarget() instanceof PlayerInstance)
					{
						PlayerInstance temp = (PlayerInstance) getTarget();
						sendPacket(new ActionFailed());
						if (temp.getPrivateStoreType() == 1)
						{
							sendPacket(new PrivateBuyListSell(this, temp));
						}
						if (temp.getPrivateStoreType() == 3)
						{
							sendPacket(new PrivateBuyListBuy(temp, this));
						}
						setCurrentState((byte) 0);
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
		setCurrentState((byte) 0);
		if (!(getTarget() instanceof ItemInstance))
		{
			_log.warning("trying to pickup wrong target." + getTarget());
			return;
		}
		ItemInstance target = (ItemInstance) getTarget();
		sendPacket(new ActionFailed());
		StopMove sm = new StopMove(getObjectId(), getX(), getY(), getZ(), getHeading());
		_log.fine("pickup pos: " + target.getX() + " " + target.getY() + " " + target.getZ());
		sendPacket(sm);
		boolean pickupOk = false;
		ItemInstance ItemInstance = target;
		synchronized (ItemInstance)
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
		GetItem gi = new GetItem(target, getObjectId());
		sendPacket(gi);
		broadcastPacket(gi);
		DeleteObject del = new DeleteObject(target);
		sendPacket(del);
		broadcastPacket(del);
		if (!isInParty())
		{
			SystemMessage smsg;
			if (target.getItemId() == 57)
			{
				smsg = new SystemMessage(28);
				smsg.addNumber(target.getCount());
				sendPacket(smsg);
			}
			else
			{
				smsg = new SystemMessage(29);
				smsg.addNumber(target.getCount());
				smsg.addItemName(target.getItemId());
				sendPacket(smsg);
			}
			ItemInstance target2 = getInventory().addItem(target);
			InventoryUpdate iu = new InventoryUpdate();
			if (target2.getLastChange() == 1)
			{
				iu.addNewItem(target);
			}
			else
			{
				iu.addModifiedItem(target2);
			}
			sendPacket(iu);
			UserInfo ci = new UserInfo(this);
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
		if (getCurrentState() == 2)
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
		if ((newTarget != null) && (newTarget instanceof Creature))
		{
			((Creature) newTarget).addStatusListener(this);
		}
		super.setTarget(newTarget);
	}
	
	@Override
	public L2Weapon getActiveWeapon()
	{
		ItemInstance weapon = getInventory().getPaperdollItem(7);
		if (weapon == null)
		{
			return getFistsWeaponItem();
		}
		return (L2Weapon) weapon.getItem();
	}
	
	@Override
	public void reduceCurrentHp(int i, Creature attacker)
	{
		if (isInvul())
		{
			return;
		}
		super.reduceCurrentHp(i, attacker);
		if (isDead() && (getPet() != null))
		{
			getPet().unSummon(this);
		}
		if (attacker != null)
		{
			SystemMessage smsg = new SystemMessage(36);
			_log.fine("Attacker:" + attacker.getName());
			if ((attacker instanceof MonsterInstance) || (attacker instanceof NpcInstance))
			{
				int mobId = ((NpcInstance) attacker).getNpcTemplate().getNpcId();
				_log.fine("mob id:" + mobId);
				smsg.addNpcName(mobId);
			}
			else
			{
				smsg.addString(attacker.getName());
			}
			smsg.addNumber(i);
			sendPacket(smsg);
		}
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
			SpawnItem si = new SpawnItem((ItemInstance) object);
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
			PlayerInstance otherPlayer = (PlayerInstance) object;
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
			Creature obj = (Creature) object;
			if (obj.isMoving())
			{
				_log.fine("Spotted object in movement, updating status");
				CharMoveToLocation mov = new CharMoveToLocation(obj);
				sendPacket(mov);
			}
			else if (obj.isMovingToPawn())
			{
				_log.fine("Spotted object in movement to pawn, updating status");
				MoveToPawn mov = new MoveToPawn(obj, obj.getPawnTarget(), obj.getPawnOffset());
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
		LvlupData lvlData = LevelUpData.getInstance().getTemplate(getClassId());
		if (lvlData != null)
		{
			double hp1 = (getLevel() - 1) * lvlData.getDefaulthpadd();
			double hp2 = (getLevel() - 2) * lvlData.getDefaulthpbonus();
			double mp1 = (getLevel() - 1) * lvlData.getDefaultmpadd();
			double mp2 = (getLevel() - 2) * lvlData.getDefaultmpbonus();
			int newhp = (int) Math.rint(lvlData.getDefaulthp() + hp1 + hp2);
			int newmp = (int) Math.rint(lvlData.getDefaultmp() + mp1 + mp2);
			setMaxHp(newhp);
			setCurrentHp(newhp);
			StatusUpdate su = new StatusUpdate(getObjectId());
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
		L2Weapon weapon = getActiveWeapon();
		if ((weapon != null) && (weapon.getWeaponType() == 5))
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
		World world = World.getInstance();
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
			GmListTable.getInstance().deleteGm(this);
		}
		removeAllKnownObjects();
		setNetConnection(null);
		world.removeObject(this);
	}
	
	public void updatePAtk()
	{
		double lvlmod = (89.0 + getLevel()) / 100.0;
		StatModifiers modifier = CharStatsTable.getInstance().getTemplate(getClassId());
		double strmod = 1.0;
		if (modifier != null)
		{
			strmod = (100.0 + modifier.getModstr()) / 100.0;
		}
		else
		{
			_log.warning("Missing STR modifier for classId: " + getClassId());
		}
		L2CharTemplate template = CharTemplateTable.getInstance().getTemplate(getClassId());
		double weapondmg = 1.0;
		if (template != null)
		{
			weapondmg = template.getPatk();
		}
		else
		{
			_log.warning("Missing template for classId: " + getClassId());
		}
		L2Weapon weapon = getActiveWeapon();
		if (weapon != null)
		{
			weapondmg += weapon.getPDamage();
		}
		double pAtk = weapondmg * lvlmod * strmod;
		setPhysicalAttack((int) Math.rint(pAtk));
		_log.fine("new patk: " + pAtk + " weapon patk: " + weapondmg);
	}
	
	public void updatePDef()
	{
		L2Armor armorPiece;
		double lvlmod = (89.0 + getLevel()) / 100.0;
		L2CharTemplate template = CharTemplateTable.getInstance().getTemplate(getClassId());
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
			armorPiece = (L2Armor) dummy.getItem();
			totalItemDef += armorPiece.getPDef();
		}
		if ((dummy = getInventory().getPaperdollItem(10)) != null)
		{
			armorPiece = (L2Armor) dummy.getItem();
			totalItemDef += armorPiece.getPDef();
		}
		if ((dummy = getInventory().getPaperdollItem(12)) != null)
		{
			armorPiece = (L2Armor) dummy.getItem();
			totalItemDef += armorPiece.getPDef();
		}
		if ((dummy = getInventory().getPaperdollItem(9)) != null)
		{
			armorPiece = (L2Armor) dummy.getItem();
			totalItemDef += armorPiece.getPDef();
		}
		if ((dummy = getInventory().getPaperdollItem(6)) != null)
		{
			armorPiece = (L2Armor) dummy.getItem();
			totalItemDef += armorPiece.getPDef();
		}
		if ((dummy = getInventory().getPaperdollItem(11)) != null)
		{
			armorPiece = (L2Armor) dummy.getItem();
			totalItemDef += armorPiece.getPDef();
		}
		if ((dummy = getInventory().getPaperdollItem(0)) != null)
		{
			armorPiece = (L2Armor) dummy.getItem();
			totalItemDef += armorPiece.getPDef();
		}
		double pDef = totalItemDef * lvlmod;
		setPhysicalDefense((int) Math.round(pDef));
		_log.fine(getObjectId() + ": new pdef: " + pDef);
	}
	
	public void updateMAtk()
	{
		double lmod = (89.0 + getLevel()) / 100.0;
		double lvlmod = Math.sqrt(lmod);
		StatModifiers modifier = CharStatsTable.getInstance().getTemplate(getClassId());
		double imod = 1.0;
		if (modifier != null)
		{
			imod = (100.0 + modifier.getModint()) / 100.0;
		}
		else
		{
			_log.warning("Missing INT modifier for classId: " + getClassId());
		}
		double intmod = Math.sqrt(imod);
		L2CharTemplate template = CharTemplateTable.getInstance().getTemplate(getClassId());
		double weapondmg = 1.0;
		if (template != null)
		{
			weapondmg = template.getMatk();
		}
		else
		{
			_log.warning("Missing template for classId: " + getClassId());
		}
		L2Weapon weapon = getActiveWeapon();
		if (weapon != null)
		{
			weapondmg += weapon.getMDamage();
		}
		double mAtk = weapondmg * lvlmod * intmod;
		setMagicalAttack((int) Math.rint(mAtk));
		_log.fine("new matk: " + mAtk + " weapon matk: " + weapondmg);
	}
	
	public void updateMDef()
	{
		L2Armor armorPiece;
		double lvlBonus = (89.0 + getLevel()) / 100.0;
		StatModifiers modifier = CharStatsTable.getInstance().getTemplate(getClassId());
		double MENbonus = 1.0;
		if (modifier != null)
		{
			MENbonus = (100.0 + modifier.getModmen()) / 100.0;
		}
		else
		{
			_log.warning("Missing MEN modifier for classId: " + getClassId());
		}
		L2CharTemplate template = CharTemplateTable.getInstance().getTemplate(getClassId());
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
			armorPiece = (L2Armor) dummy.getItem();
			totalItemDef += armorPiece.getMDef();
		}
		if ((dummy = getInventory().getPaperdollItem(4)) != null)
		{
			armorPiece = (L2Armor) dummy.getItem();
			totalItemDef += armorPiece.getMDef();
		}
		if ((dummy = getInventory().getPaperdollItem(3)) != null)
		{
			armorPiece = (L2Armor) dummy.getItem();
			totalItemDef += armorPiece.getMDef();
		}
		if ((dummy = getInventory().getPaperdollItem(2)) != null)
		{
			armorPiece = (L2Armor) dummy.getItem();
			totalItemDef += armorPiece.getMDef();
		}
		if ((dummy = getInventory().getPaperdollItem(5)) != null)
		{
			armorPiece = (L2Armor) dummy.getItem();
			totalItemDef += armorPiece.getMDef();
		}
		double mDef = totalItemDef * lvlBonus * MENbonus;
		setMagicalDefense((int) Math.round(mDef));
		_log.fine(getObjectId() + ": new mdef: " + mDef);
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
			_log.fine("Attack magic has no target or target oneself.");
			return;
		}
		if (isDead() || target.isDead() || _allSkillsDisabled || isSkillDisabled(skill.getId()) || skill.isPassive())
		{
			return;
		}
		int weaponType = getActiveWeapon().getWeaponType();
		int skillId = skill.getId();
		if ((skillId == 56) && (weaponType != 5))
		{
			return;
		}
		if ((skillId == 16) && (weaponType != 4))
		{
			return;
		}
		if ((skillId == 3) && (weaponType != 3) && (weaponType != 2))
		{
			return;
		}
		if ((skillId == 29) && (weaponType != 10))
		{
			return;
		}
		if (SkillHandler.getInstance().getSkillHandler(skill.getId()) == null)
		{
			SystemMessage sm = new SystemMessage(614);
			sm.addString("This skill is not implemented yet");
			sendPacket(sm);
			return;
		}
		if (getCurrentMp() < skill.getMpConsume())
		{
			sendPacket(new SystemMessage(24));
			return;
		}
		if (getCurrentHp() < skill.getHpConsume())
		{
			sendPacket(new SystemMessage(23));
			return;
		}
		setCurrentState((byte) 2);
		setSkill(skill);
		double distance = getDistance(target.getX(), target.getY());
		if ((skill.getCastRange() > 0) && (distance > skill.getCastRange()))
		{
			moveTo(target.getX(), target.getY(), target.getZ(), skill.getCastRange());
			return;
		}
		int magicId = skill.getId();
		int level = getSkillLevel(magicId);
		ActionFailed af = new ActionFailed();
		sendPacket(af);
		MagicSkillUser msu = new MagicSkillUser(this, target, magicId, level, skill.getSkillTime(), skill.getReuseDelay());
		sendPacket(msu);
		broadcastPacket(msu);
		SetupGauge sg = new SetupGauge(0, skill.getSkillTime());
		sendPacket(sg);
		SystemMessage sm = new SystemMessage(46);
		sm.addSkillName(magicId);
		sendPacket(sm);
		if (skill.getSkillTime() > 300)
		{
			disableSkill(skill.getId(), true);
			_enableSkillTimer.schedule(new EnableSkill(skill.getId()), skill.getReuseDelay());
			disableAllSkills();
			_enableAllSkillsTimer.schedule(new EnableAllSkills(skill), skill.getSkillTime());
			_magicUseTimer.schedule(new MagicUseTask(target, skill), skill.getSkillTime() - 300);
		}
	}
	
	@Override
	protected void reduceArrowCount()
	{
		ItemInstance arrows = getInventory().destroyItem(getInventory().getPaperdollObjectId(8), 1);
		_log.info("arrow count:" + arrows.getCount());
		if (arrows.getCount() == 0)
		{
			getInventory().unEquipItemOnPaperdoll(8);
			_arrowItem = null;
			_log.info("removed arrows count");
			sendPacket(new ItemList(this, false));
		}
		else
		{
			InventoryUpdate iu = new InventoryUpdate();
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
				ItemList il = new ItemList(this, false);
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
		L2Weapon weaponItem = getActiveWeapon();
		if (weaponItem.getWeaponType() == 9)
		{
			return true;
		}
		if (weaponItem.getWeaponType() == 10)
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
	
	public void onMagicUseTimer(Creature target, Skill skill) throws IOException
	{
		if ((getCurrentState() == 2) && _allSkillsDisabled && isSkillDisabled(skill.getId()) && (getSkill() == skill))
		{
			int magicId = skill.getId();
			int level = getSkillLevel(magicId);
			if ((skill.getTargetType() == Skill.TARGET_PARTY) && isInParty())
			{
				Iterator<PlayerInstance> it = getParty().getPartyMembers().iterator();
				while (it.hasNext())
				{
					PlayerInstance player = it.next();
					_log.fine("msl: " + getName() + " " + magicId + " " + level + " " + player.getName());
					MagicSkillLaunched msl = new MagicSkillLaunched(this, magicId, level, player);
					sendPacket(msl);
					broadcastPacket(msl);
				}
			}
			else
			{
				MagicSkillLaunched msl = new MagicSkillLaunched(this, magicId, level, target);
				_log.fine("msl: " + getName() + " " + magicId + " " + level + " " + target.getName());
				sendPacket(msl);
				broadcastPacket(msl);
			}
			reduceCurrentMp(skill.getMpConsume());
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
			sendPacket(su);
			ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(skill.getId());
			if (handler == null)
			{
				_log.warning("no skillhandler registered for skillId:" + skill.getId());
			}
			else
			{
				handler.useSkill(this, skill, target);
			}
			setCurrentState((byte) 0);
		}
	}
	
	public void disableSkill(int skillId, boolean state)
	{
		_disabledSkills.put(skillId, state);
	}
	
	public boolean isSkillDisabled(int skillId)
	{
		try
		{
			return _disabledSkills.get(skillId);
		}
		catch (NullPointerException e)
		{
			return false;
		}
	}
	
	public void disableAllSkills()
	{
		_log.fine("all skills disabled");
		_allSkillsDisabled = true;
	}
	
	@Override
	protected void enableAllSkills()
	{
		_log.fine("all skills enabled");
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
		setCurrentState((byte) 0);
		enableAllSkills();
		MagicSkillCanceld msc = new MagicSkillCanceld(getObjectId());
		sendPacket(msc);
		broadcastPacket(msc);
	}
	
	class EnableAllSkills extends TimerTask
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
	
	class EnableSkill extends TimerTask
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
	
	class MagicUseTask extends TimerTask
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
			try
			{
				onMagicUseTimer(_target, _skill);
			}
			catch (Throwable e)
			{
				StringWriter pw = new StringWriter();
				PrintWriter prw = new PrintWriter(pw);
				e.printStackTrace(prw);
				_log.severe(pw.toString());
			}
		}
	}
	
}
