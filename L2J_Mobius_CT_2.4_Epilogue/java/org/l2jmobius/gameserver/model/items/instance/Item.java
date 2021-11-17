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
package org.l2jmobius.gameserver.model.items.instance;

import static org.l2jmobius.gameserver.model.itemcontainer.Inventory.ADENA_ID;
import static org.l2jmobius.gameserver.model.itemcontainer.Inventory.MAX_ADENA;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.data.xml.EnchantItemOptionsData;
import org.l2jmobius.gameserver.data.xml.OptionData;
import org.l2jmobius.gameserver.enums.InstanceType;
import org.l2jmobius.gameserver.enums.ItemLocation;
import org.l2jmobius.gameserver.enums.ShotType;
import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.instancemanager.IdManager;
import org.l2jmobius.gameserver.instancemanager.ItemsOnGroundManager;
import org.l2jmobius.gameserver.instancemanager.MercTicketManager;
import org.l2jmobius.gameserver.model.Augmentation;
import org.l2jmobius.gameserver.model.DropProtection;
import org.l2jmobius.gameserver.model.Elementals;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.WorldRegion;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventDispatcher;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerAugment;
import org.l2jmobius.gameserver.model.events.impl.creature.player.inventory.OnPlayerItemDrop;
import org.l2jmobius.gameserver.model.events.impl.creature.player.inventory.OnPlayerItemPickup;
import org.l2jmobius.gameserver.model.events.impl.item.OnItemBypassEvent;
import org.l2jmobius.gameserver.model.events.impl.item.OnItemTalk;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.items.Armor;
import org.l2jmobius.gameserver.model.items.EtcItem;
import org.l2jmobius.gameserver.model.items.ItemTemplate;
import org.l2jmobius.gameserver.model.items.Weapon;
import org.l2jmobius.gameserver.model.items.type.EtcItemType;
import org.l2jmobius.gameserver.model.items.type.ItemType;
import org.l2jmobius.gameserver.model.options.EnchantOptions;
import org.l2jmobius.gameserver.model.options.Options;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.model.stats.functions.AbstractFunction;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.DropItem;
import org.l2jmobius.gameserver.network.serverpackets.GetItem;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SpawnItem;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.taskmanager.ItemLifeTimeTaskManager;
import org.l2jmobius.gameserver.taskmanager.ItemManaTaskManager;
import org.l2jmobius.gameserver.util.GMAudit;

/**
 * This class manages items.
 * @version $Revision: 1.4.2.1.2.11 $ $Date: 2005/03/31 16:07:50 $
 */
public class Item extends WorldObject
{
	private static final Logger LOGGER = Logger.getLogger(Item.class.getName());
	private static final Logger LOG_ITEMS = Logger.getLogger("item");
	
	/** Owner */
	private int _ownerId;
	private Player _owner;
	
	/** ID of who dropped the item last, used for knownlist */
	private int _dropperObjectId = 0;
	
	/** Quantity of the item */
	private long _count;
	/** Initial Quantity of the item */
	private long _initCount;
	/** Remaining time (in miliseconds) */
	private long _time;
	/** Quantity of the item can decrease */
	private boolean _decrease = false;
	
	/** ID of the item */
	private final int _itemId;
	
	/** ItemTemplate associated to the item */
	private final ItemTemplate _item;
	
	/** Location of the item : Inventory, PaperDoll, WareHouse */
	private ItemLocation _loc;
	
	/** Slot where item is stored : Paperdoll slot, inventory order ... */
	private int _locData;
	
	/** Level of enchantment of the item */
	private int _enchantLevel;
	
	/** Wear Item */
	private boolean _wear;
	
	/** Augmented Item */
	private Augmentation _augmentation = null;
	
	/** Shadow item */
	private int _mana = -1;
	private boolean _consumingMana = false;
	
	/** Custom item types (used loto, race tickets) */
	private int _type1;
	private int _type2;
	
	private long _dropTime;
	
	private boolean _published = false;
	
	private boolean _protected;
	
	public static final int UNCHANGED = 0;
	public static final int ADDED = 1;
	public static final int REMOVED = 3;
	public static final int MODIFIED = 2;
	
	//@formatter:off
	public static final int[] DEFAULT_ENCHANT_OPTIONS = new int[] { 0, 0, 0 };
	//@formatter:on
	
	private int _lastChange = 2; // 1 ??, 2 modified, 3 removed
	private boolean _existsInDb; // if a record exists in DB.
	private boolean _storedInDb; // if DB data is up-to-date.
	
	private final ReentrantLock _dbLock = new ReentrantLock();
	
	private Elementals[] _elementals = null;
	
	private ScheduledFuture<?> _itemLootShedule = null;
	
	private final DropProtection _dropProtection = new DropProtection();
	
	private int _shotsMask = 0;
	
	private final List<Options> _enchantOptions = new ArrayList<>();
	
	/**
	 * Constructor of the Item from the objectId and the itemId.
	 * @param objectId : int designating the ID of the object in the world
	 * @param itemId : int designating the ID of the item
	 */
	public Item(int objectId, int itemId)
	{
		super(objectId);
		setInstanceType(InstanceType.Item);
		_itemId = itemId;
		_item = ItemTable.getInstance().getTemplate(itemId);
		if ((_itemId == 0) || (_item == null))
		{
			throw new IllegalArgumentException();
		}
		super.setName(_item.getName());
		setCount(1);
		_loc = ItemLocation.VOID;
		_type1 = 0;
		_type2 = 0;
		_dropTime = 0;
		_mana = _item.getDuration();
		_time = _item.getTime() == -1 ? -1 : Chronos.currentTimeMillis() + (_item.getTime() * 60 * 1000);
		_enchantLevel = 0;
		scheduleLifeTimeTask();
	}
	
	/**
	 * Constructor of the Item from the objetId and the description of the item given by the Item.
	 * @param objectId : int designating the ID of the object in the world
	 * @param item : Item containing informations of the item
	 */
	public Item(int objectId, ItemTemplate item)
	{
		super(objectId);
		setInstanceType(InstanceType.Item);
		_itemId = item.getId();
		_item = item;
		if (_itemId == 0)
		{
			throw new IllegalArgumentException();
		}
		super.setName(_item.getName());
		setCount(1);
		_loc = ItemLocation.VOID;
		_mana = _item.getDuration();
		_time = _item.getTime() == -1 ? -1 : Chronos.currentTimeMillis() + (_item.getTime() * 60 * 1000);
		scheduleLifeTimeTask();
	}
	
	/**
	 * Constructor overload.<br>
	 * Sets the next free object ID in the ID factory.
	 * @param itemId the item template ID
	 */
	public Item(int itemId)
	{
		this(IdManager.getInstance().getNextId(), itemId);
	}
	
	/**
	 * Remove a Item from the world and send server->client GetItem packets.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Send a Server->Client Packet GetItem to player that pick up and its _knowPlayers member</li>
	 * <li>Remove the WorldObject from the world</li><br>
	 * <font color=#FF0000><b><u>Caution</u>: This method DOESN'T REMOVE the object from _allObjects of World </b></font><br>
	 * <br>
	 * <b><u>Example of use</u>:</b><br>
	 * <li>Do Pickup Item : Player and Pet</li><br>
	 * @param creature Character that pick up the item
	 */
	public void pickupMe(Creature creature)
	{
		final WorldRegion oldregion = getWorldRegion();
		
		// Create a server->client GetItem packet to pick up the Item
		creature.broadcastPacket(new GetItem(this, creature.getObjectId()));
		
		synchronized (this)
		{
			setSpawned(false);
		}
		
		// if this item is a mercenary ticket, remove the spawns!
		
		if (MercTicketManager.getInstance().getTicketCastleId(_itemId) > 0)
		{
			MercTicketManager.getInstance().removeTicket(this);
			ItemsOnGroundManager.getInstance().removeObject(this);
		}
		
		if (!Config.DISABLE_TUTORIAL && ((_itemId == ADENA_ID) || (_itemId == 6353)))
		{
			// Note from UnAfraid:
			// Unhardcode this?
			final Player actor = creature.getActingPlayer();
			if (actor != null)
			{
				final QuestState qs = actor.getQuestState("Q00255_Tutorial");
				if ((qs != null) && (qs.getQuest() != null))
				{
					qs.getQuest().notifyEvent("CE" + _itemId, null, actor);
				}
			}
		}
		// outside of synchronized to avoid deadlocks
		// Remove the Item from the world
		World.getInstance().removeVisibleObject(this, oldregion);
		
		if (creature.isPlayer())
		{
			// Notify to scripts
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerItemPickup(creature.getActingPlayer(), this), getItem());
		}
	}
	
	/**
	 * Sets the ownerID of the item
	 * @param process : String Identifier of process triggering this action
	 * @param ownerId : int designating the ID of the owner
	 * @param creator : Player Player requesting the item creation
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 */
	public void setOwnerId(String process, int ownerId, Player creator, Object reference)
	{
		setOwnerId(ownerId);
		
		if (Config.LOG_ITEMS)
		{
			if (!Config.LOG_ITEMS_SMALL_LOG || (Config.LOG_ITEMS_SMALL_LOG && (_item.isEquipable() || (_item.getId() == ADENA_ID))))
			{
				if (_enchantLevel > 0)
				{
					LOG_ITEMS.info("SETOWNER:" + String.valueOf(process) // in case of null
						+ ", item " + getObjectId() //
						+ ":+" + _enchantLevel //
						+ " " + _item.getName() //
						+ "(" + _count + "), " //
						+ String.valueOf(creator) + ", " // in case of null
						+ String.valueOf(reference)); // in case of null
				}
				else
				{
					LOG_ITEMS.info("SETOWNER:" + String.valueOf(process) // in case of null
						+ ", item " + getObjectId() //
						+ ":" + _item.getName() //
						+ "(" + _count + "), " //
						+ String.valueOf(creator) + ", " // in case of null
						+ String.valueOf(reference)); // in case of null
				}
			}
		}
		
		if ((creator != null) && creator.isGM())
		{
			String referenceName = "no-reference";
			if (reference instanceof WorldObject)
			{
				referenceName = ((WorldObject) reference).getName() != null ? ((WorldObject) reference).getName() : "no-name";
			}
			else if (reference instanceof String)
			{
				referenceName = (String) reference;
			}
			final String targetName = creator.getTarget() != null ? creator.getTarget().getName() : "no-target";
			if (Config.GMAUDIT)
			{
				GMAudit.auditGMAction(creator.getName() + " [" + creator.getObjectId() + "]", process + "(id: " + _itemId + " name: " + getName() + ")", targetName, "Object referencing this action is: " + referenceName);
			}
		}
	}
	
	/**
	 * Sets the ownerID of the item
	 * @param ownerId : int designating the ID of the owner
	 */
	public void setOwnerId(int ownerId)
	{
		if (ownerId == _ownerId)
		{
			return;
		}
		
		// Remove any inventory skills from the old owner.
		removeSkillsFromOwner();
		
		_owner = null;
		_ownerId = ownerId;
		_storedInDb = false;
		
		// Give any inventory skills to the new owner only if the item is in inventory
		// else the skills will be given when location is set to inventory.
		giveSkillsToOwner();
	}
	
	/**
	 * Returns the ownerID of the item
	 * @return int : ownerID of the item
	 */
	public int getOwnerId()
	{
		return _ownerId;
	}
	
	/**
	 * Sets the location of the item
	 * @param loc : ItemLocation (enumeration)
	 */
	public void setItemLocation(ItemLocation loc)
	{
		setItemLocation(loc, 0);
	}
	
	/**
	 * Sets the location of the item.<br>
	 * <u><i>Remark :</i></u> If loc and loc_data different from database, say datas not up-to-date
	 * @param loc : ItemLocation (enumeration)
	 * @param locData : int designating the slot where the item is stored or the village for freights
	 */
	public void setItemLocation(ItemLocation loc, int locData)
	{
		if ((loc == _loc) && (locData == _locData))
		{
			return;
		}
		
		// Remove any inventory skills from the old owner.
		removeSkillsFromOwner();
		
		_loc = loc;
		_locData = locData;
		_storedInDb = false;
		
		// Give any inventory skills to the new owner only if the item is in inventory
		// else the skills will be given when location is set to inventory.
		giveSkillsToOwner();
	}
	
	public ItemLocation getItemLocation()
	{
		return _loc;
	}
	
	/**
	 * Sets the quantity of the item.
	 * @param count the new count to set
	 */
	public void setCount(long count)
	{
		if (_count == count)
		{
			return;
		}
		
		_count = count >= -1 ? count : 0;
		_storedInDb = false;
	}
	
	/**
	 * @return Returns the count.
	 */
	public long getCount()
	{
		return _count;
	}
	
	/**
	 * Sets the quantity of the item.<br>
	 * <u><i>Remark :</i></u> If loc and loc_data different from database, say datas not up-to-date
	 * @param process : String Identifier of process triggering this action
	 * @param count : int
	 * @param creator : Player Player requesting the item creation
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 */
	public void changeCount(String process, long count, Player creator, Object reference)
	{
		if (count == 0)
		{
			return;
		}
		final long old = _count;
		final long max = _itemId == ADENA_ID ? MAX_ADENA : Long.MAX_VALUE;
		
		if ((count > 0) && (_count > (max - count)))
		{
			setCount(max);
		}
		else
		{
			setCount(_count + count);
		}
		
		if (_count < 0)
		{
			setCount(0);
		}
		
		_storedInDb = false;
		
		if (Config.LOG_ITEMS && (process != null))
		{
			if (!Config.LOG_ITEMS_SMALL_LOG || (Config.LOG_ITEMS_SMALL_LOG && (_item.isEquipable() || (_item.getId() == ADENA_ID))))
			{
				if (_enchantLevel > 0)
				{
					LOG_ITEMS.info("CHANGE:" + String.valueOf(process) // in case of null
						+ ", item " + getObjectId() //
						+ ":+" + _enchantLevel //
						+ " " + _item.getName() //
						+ "(" + _count + "), PrevCount(" //
						+ String.valueOf(old) + "), " // in case of null
						+ String.valueOf(creator) + ", " // in case of null
						+ String.valueOf(reference)); // in case of null
				}
				else
				{
					LOG_ITEMS.info("CHANGE:" + String.valueOf(process) // in case of null
						+ ", item " + getObjectId() //
						+ ":" + _item.getName() //
						+ "(" + _count + "), PrevCount(" //
						+ String.valueOf(old) + "), " // in case of null
						+ String.valueOf(creator) + ", " // in case of null
						+ String.valueOf(reference)); // in case of null
				}
			}
		}
		
		if ((creator != null) && creator.isGM())
		{
			String referenceName = "no-reference";
			if (reference instanceof WorldObject)
			{
				referenceName = ((WorldObject) reference).getName() != null ? ((WorldObject) reference).getName() : "no-name";
			}
			else if (reference instanceof String)
			{
				referenceName = (String) reference;
			}
			final String targetName = creator.getTarget() != null ? creator.getTarget().getName() : "no-target";
			if (Config.GMAUDIT)
			{
				GMAudit.auditGMAction(creator.getName() + " [" + creator.getObjectId() + "]", process + "(id: " + _itemId + " objId: " + getObjectId() + " name: " + getName() + " count: " + count + ")", targetName, "Object referencing this action is: " + referenceName);
			}
		}
	}
	
	// No logging (function designed for shots only)
	public void changeCountWithoutTrace(int count, Player creator, Object reference)
	{
		changeCount(null, count, creator, reference);
	}
	
	/**
	 * Return true if item can be enchanted
	 * @return boolean
	 */
	public boolean isEnchantable()
	{
		return (_loc == ItemLocation.INVENTORY) || (_loc == ItemLocation.PAPERDOLL) ? _item.isEnchantable() : false;
	}
	
	/**
	 * Returns if item is equipable
	 * @return boolean
	 */
	public boolean isEquipable()
	{
		return (_item.getBodyPart() != 0) && (_item.getItemType() != EtcItemType.ARROW) && (_item.getItemType() != EtcItemType.BOLT) && (_item.getItemType() != EtcItemType.LURE);
	}
	
	/**
	 * Returns if item is equipped
	 * @return boolean
	 */
	public boolean isEquipped()
	{
		return (_loc == ItemLocation.PAPERDOLL) || (_loc == ItemLocation.PET_EQUIP);
	}
	
	/**
	 * Returns the slot where the item is stored
	 * @return int
	 */
	public int getLocationSlot()
	{
		return _locData;
	}
	
	/**
	 * Returns the characteristics of the item
	 * @return Item
	 */
	public ItemTemplate getItem()
	{
		return _item;
	}
	
	public int getCustomType1()
	{
		return _type1;
	}
	
	public int getCustomType2()
	{
		return _type2;
	}
	
	public void setCustomType1(int newtype)
	{
		_type1 = newtype;
	}
	
	public void setCustomType2(int newtype)
	{
		_type2 = newtype;
	}
	
	public void setDropTime(long time)
	{
		_dropTime = time;
	}
	
	public long getDropTime()
	{
		return _dropTime;
	}
	
	/**
	 * @return the type of item.
	 */
	public ItemType getItemType()
	{
		return _item.getItemType();
	}
	
	/**
	 * Gets the item ID.
	 * @return the item ID
	 */
	@Override
	public int getId()
	{
		return _itemId;
	}
	
	/**
	 * @return the display Id of the item.
	 */
	public int getDisplayId()
	{
		return _item.getDisplayId();
	}
	
	/**
	 * @return {@code true} if item is an EtcItem, {@code false} otherwise.
	 */
	public boolean isEtcItem()
	{
		return _item instanceof EtcItem;
	}
	
	/**
	 * @return {@code true} if item is a Weapon/Shield, {@code false} otherwise.
	 */
	public boolean isWeapon()
	{
		return _item instanceof Weapon;
	}
	
	/**
	 * @return {@code true} if item is an Armor, {@code false} otherwise.
	 */
	public boolean isArmor()
	{
		return _item instanceof Armor;
	}
	
	/**
	 * @return the characteristics of the EtcItem, {@code false} otherwise.
	 */
	public EtcItem getEtcItem()
	{
		return _item instanceof EtcItem ? (EtcItem) _item : null;
	}
	
	/**
	 * @return the characteristics of the Weapon.
	 */
	public Weapon getWeaponItem()
	{
		return _item instanceof Weapon ? (Weapon) _item : null;
	}
	
	/**
	 * @return the characteristics of the Armor.
	 */
	public Armor getArmorItem()
	{
		return _item instanceof Armor ? (Armor) _item : null;
	}
	
	/**
	 * @return the quantity of crystals for crystallization.
	 */
	public int getCrystalCount()
	{
		return _item.getCrystalCount(_enchantLevel);
	}
	
	/**
	 * @return the reference price of the item.
	 */
	public int getReferencePrice()
	{
		return _item.getReferencePrice();
	}
	
	/**
	 * @return the name of the item.
	 */
	public String getItemName()
	{
		return _item.getName();
	}
	
	/**
	 * @return the reuse delay of this item.
	 */
	public int getReuseDelay()
	{
		return _item.getReuseDelay();
	}
	
	/**
	 * @return the shared reuse item group.
	 */
	public int getSharedReuseGroup()
	{
		return _item.getSharedReuseGroup();
	}
	
	/**
	 * @return the last change of the item
	 */
	public int getLastChange()
	{
		return _lastChange;
	}
	
	/**
	 * Sets the last change of the item
	 * @param lastChange : int
	 */
	public void setLastChange(int lastChange)
	{
		_lastChange = lastChange;
	}
	
	/**
	 * Returns if item is stackable
	 * @return boolean
	 */
	public boolean isStackable()
	{
		return _item.isStackable();
	}
	
	/**
	 * Returns if item is dropable
	 * @return boolean
	 */
	public boolean isDropable()
	{
		if (Config.ALT_ALLOW_AUGMENT_TRADE && isAugmented())
		{
			return true;
		}
		return !isAugmented() && _item.isDropable();
	}
	
	/**
	 * Returns if item is destroyable
	 * @return boolean
	 */
	public boolean isDestroyable()
	{
		if (!Config.ALT_ALLOW_AUGMENT_DESTROY && isAugmented())
		{
			return false;
		}
		return _item.isDestroyable();
	}
	
	/**
	 * Returns if item is tradeable
	 * @return boolean
	 */
	public boolean isTradeable()
	{
		if (Config.ALT_ALLOW_AUGMENT_TRADE && isAugmented())
		{
			return true;
		}
		return !isAugmented() && _item.isTradeable();
	}
	
	/**
	 * Returns if item is sellable
	 * @return boolean
	 */
	public boolean isSellable()
	{
		if (Config.ALT_ALLOW_AUGMENT_TRADE && isAugmented())
		{
			return true;
		}
		return !isAugmented() && _item.isSellable();
	}
	
	/**
	 * @param isPrivateWareHouse
	 * @return if item can be deposited in warehouse or freight
	 */
	public boolean isDepositable(boolean isPrivateWareHouse)
	{
		return !isEquipped() && _item.isDepositable() && (isPrivateWareHouse || (isTradeable() && !isShadowItem()));
	}
	
	public boolean isPotion()
	{
		return _item.isPotion();
	}
	
	public boolean isElixir()
	{
		return _item.isElixir();
	}
	
	public boolean isScroll()
	{
		return _item.isScroll();
	}
	
	public boolean isHeroItem()
	{
		return _item.isHeroItem();
	}
	
	public boolean isCommonItem()
	{
		return _item.isCommon();
	}
	
	/**
	 * Returns whether this item is pvp or not
	 * @return boolean
	 */
	public boolean isPvp()
	{
		return _item.isPvpItem();
	}
	
	public boolean isOlyRestrictedItem()
	{
		return _item.isOlyRestrictedItem();
	}
	
	/**
	 * @param player
	 * @param allowAdena
	 * @param allowNonTradeable
	 * @return if item is available for manipulation
	 */
	public boolean isAvailable(Player player, boolean allowAdena, boolean allowNonTradeable)
	{
		return ((!isEquipped()) // Not equipped
			&& (_item.getType2() != ItemTemplate.TYPE2_QUEST) // Not Quest Item
			&& ((_item.getType2() != ItemTemplate.TYPE2_MONEY) || (_item.getType1() != ItemTemplate.TYPE1_SHIELD_ARMOR)) // not money, not shield
			&& (!player.hasSummon() || (getObjectId() != player.getSummon().getControlObjectId())) // Not Control item of currently summoned pet
			&& (player.getActiveEnchantItemId() != getObjectId()) // Not momentarily used enchant scroll
			&& (player.getActiveEnchantSupportItemId() != getObjectId()) // Not momentarily used enchant support item
			&& (player.getActiveEnchantAttrItemId() != getObjectId()) // Not momentarily used enchant attribute item
			&& (allowAdena || (_itemId != ADENA_ID)) // Not Adena
			&& ((player.getCurrentSkill() == null) || (player.getCurrentSkill().getSkill().getItemConsumeId() != _itemId)) && (!player.isCastingSimultaneouslyNow() || (player.getLastSimultaneousSkillCast() == null) || (player.getLastSimultaneousSkillCast().getItemConsumeId() != _itemId)) && (allowNonTradeable || (isTradeable() && (!((_item.getItemType() == EtcItemType.PET_COLLAR) && player.havePetInvItems())))));
	}
	
	/**
	 * Returns the level of enchantment of the item
	 * @return int
	 */
	public int getEnchantLevel()
	{
		return _enchantLevel;
	}
	
	/**
	 * @param enchantLevel the enchant value to set
	 */
	public void setEnchantLevel(int enchantLevel)
	{
		if (_enchantLevel == enchantLevel)
		{
			return;
		}
		clearEnchantStats();
		_enchantLevel = enchantLevel;
		applyEnchantStats();
		_storedInDb = false;
	}
	
	/**
	 * Returns whether this item is augmented or not
	 * @return true if augmented
	 */
	public boolean isAugmented()
	{
		return _augmentation != null;
	}
	
	/**
	 * Returns the augmentation object for this item
	 * @return augmentation
	 */
	public Augmentation getAugmentation()
	{
		return _augmentation;
	}
	
	/**
	 * Sets a new augmentation
	 * @param augmentation
	 * @return return true if sucessfull
	 */
	public boolean setAugmentation(Augmentation augmentation)
	{
		// there shall be no previous augmentation..
		if (_augmentation != null)
		{
			LOGGER.info("Warning: Augment set for (" + getObjectId() + ") " + getName() + " owner: " + _ownerId);
			return false;
		}
		
		_augmentation = augmentation;
		try (Connection con = DatabaseFactory.getConnection())
		{
			updateItemAttributes(con);
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.SEVERE, "Could not update atributes for item: " + this + " from DB:", e);
		}
		
		// Notify to scripts.
		EventDispatcher.getInstance().notifyEventAsync(new OnPlayerAugment(getActingPlayer(), this, augmentation, true), getItem());
		return true;
	}
	
	/**
	 * Remove the augmentation
	 */
	public void removeAugmentation()
	{
		if (_augmentation == null)
		{
			return;
		}
		
		// Copy augmentation before removing it.
		final Augmentation augment = _augmentation;
		_augmentation = null;
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM item_attributes WHERE itemId = ?"))
		{
			ps.setInt(1, getObjectId());
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Could not remove augmentation for item: " + this + " from DB:", e);
		}
		
		// Notify to scripts.
		EventDispatcher.getInstance().notifyEventAsync(new OnPlayerAugment(getActingPlayer(), this, augment, false), getItem());
	}
	
	public void restoreAttributes()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps1 = con.prepareStatement("SELECT augAttributes FROM item_attributes WHERE itemId=?");
			PreparedStatement ps2 = con.prepareStatement("SELECT elemType,elemValue FROM item_elementals WHERE itemId=?"))
		{
			ps1.setInt(1, getObjectId());
			try (ResultSet rs = ps1.executeQuery())
			{
				if (rs.next())
				{
					final int aug_attributes = rs.getInt(1);
					if (aug_attributes != -1)
					{
						_augmentation = new Augmentation(rs.getInt("augAttributes"));
					}
				}
			}
			
			ps2.setInt(1, getObjectId());
			try (ResultSet rs = ps2.executeQuery())
			{
				while (rs.next())
				{
					final byte elem_type = rs.getByte(1);
					final int elem_value = rs.getInt(2);
					if ((elem_type != -1) && (elem_value != -1))
					{
						applyAttribute(elem_type, elem_value);
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Could not restore augmentation and elemental data for item " + this + " from DB: " + e.getMessage(), e);
		}
	}
	
	private void updateItemAttributes(Connection con)
	{
		try (PreparedStatement ps = con.prepareStatement("REPLACE INTO item_attributes VALUES(?,?)"))
		{
			ps.setInt(1, getObjectId());
			ps.setInt(2, _augmentation != null ? _augmentation.getAugmentationId() : -1);
			ps.executeUpdate();
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.SEVERE, "Could not update atributes for item: " + this + " from DB:", e);
		}
	}
	
	private void updateItemElements(Connection con)
	{
		try (PreparedStatement ps = con.prepareStatement("DELETE FROM item_elementals WHERE itemId = ?"))
		{
			ps.setInt(1, getObjectId());
			ps.executeUpdate();
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.SEVERE, "Could not update elementals for item: " + this + " from DB:", e);
		}
		
		if (_elementals == null)
		{
			return;
		}
		
		try (PreparedStatement ps = con.prepareStatement("INSERT INTO item_elementals VALUES(?,?,?)"))
		{
			for (Elementals elm : _elementals)
			{
				ps.setInt(1, getObjectId());
				ps.setByte(2, elm.getElement());
				ps.setInt(3, elm.getValue());
				ps.executeUpdate();
				ps.clearParameters();
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.SEVERE, "Could not update elementals for item: " + this + " from DB:", e);
		}
	}
	
	public Elementals[] getElementals()
	{
		return _elementals;
	}
	
	public Elementals getElemental(byte attribute)
	{
		if (_elementals == null)
		{
			return null;
		}
		for (Elementals elm : _elementals)
		{
			if (elm.getElement() == attribute)
			{
				return elm;
			}
		}
		return null;
	}
	
	public byte getAttackElementType()
	{
		if (!isWeapon())
		{
			return -2;
		}
		else if (_item.getElementals() != null)
		{
			return _item.getElementals()[0].getElement();
		}
		else if (_elementals != null)
		{
			return _elementals[0].getElement();
		}
		return -2;
	}
	
	public int getAttackElementPower()
	{
		if (!isWeapon())
		{
			return 0;
		}
		else if (_item.getElementals() != null)
		{
			return _item.getElementals()[0].getValue();
		}
		else if (_elementals != null)
		{
			return _elementals[0].getValue();
		}
		return 0;
	}
	
	public int getElementDefAttr(byte element)
	{
		if (!isArmor())
		{
			return 0;
		}
		else if (_item.getElementals() != null)
		{
			final Elementals elm = _item.getElemental(element);
			if (elm != null)
			{
				return elm.getValue();
			}
		}
		else if (_elementals != null)
		{
			final Elementals elm = getElemental(element);
			if (elm != null)
			{
				return elm.getValue();
			}
		}
		return 0;
	}
	
	private void applyAttribute(byte element, int value)
	{
		if (_elementals == null)
		{
			_elementals = new Elementals[1];
			_elementals[0] = new Elementals(element, value);
		}
		else
		{
			Elementals elm = getElemental(element);
			if (elm != null)
			{
				elm.setValue(value);
			}
			else
			{
				elm = new Elementals(element, value);
				final Elementals[] array = new Elementals[_elementals.length + 1];
				System.arraycopy(_elementals, 0, array, 0, _elementals.length);
				array[_elementals.length] = elm;
				_elementals = array;
			}
		}
	}
	
	/**
	 * Add elemental attribute to item and save to db
	 * @param element
	 * @param value
	 */
	public void setElementAttr(byte element, int value)
	{
		applyAttribute(element, value);
		try (Connection con = DatabaseFactory.getConnection())
		{
			updateItemElements(con);
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.SEVERE, "Could not update elementals for item: " + this + " from DB:", e);
		}
	}
	
	/**
	 * Remove elemental from item
	 * @param element byte element to remove, -1 for all elementals remove
	 */
	public void clearElementAttr(byte element)
	{
		if ((getElemental(element) == null) && (element != -1))
		{
			return;
		}
		
		Elementals[] array = null;
		if ((element != -1) && (_elementals != null) && (_elementals.length > 1))
		{
			array = new Elementals[_elementals.length - 1];
			int i = 0;
			for (Elementals elm : _elementals)
			{
				if (elm.getElement() != element)
				{
					array[i++] = elm;
				}
			}
		}
		_elementals = array;
		
		final String query = (element != -1) ? "DELETE FROM item_elementals WHERE itemId = ? AND elemType = ?" : "DELETE FROM item_elementals WHERE itemId = ?";
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(query))
		{
			if (element != -1)
			{
				// Item can have still others
				ps.setInt(2, element);
			}
			
			ps.setInt(1, getObjectId());
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Could not remove elemental enchant for item: " + this + " from DB:", e);
		}
	}
	
	/**
	 * Returns true if this item is a shadow item Shadow items have a limited life-time
	 * @return
	 */
	public boolean isShadowItem()
	{
		return _mana >= 0;
	}
	
	/**
	 * Returns the remaining mana of this shadow item
	 * @return lifeTime
	 */
	public int getMana()
	{
		return _mana;
	}
	
	/**
	 * Decreases the mana of this shadow item, sends a inventory update schedules a new consumption task if non is running optionally one could force a new task
	 * @param resetConsumingMana if true forces a new consumption task if item is equipped
	 */
	public void decreaseMana(boolean resetConsumingMana)
	{
		decreaseMana(resetConsumingMana, 1);
	}
	
	/**
	 * Decreases the mana of this shadow item, sends a inventory update schedules a new consumption task if non is running optionally one could force a new task
	 * @param resetConsumingMana if forces a new consumption task if item is equipped
	 * @param count how much mana decrease
	 */
	public void decreaseMana(boolean resetConsumingMana, int count)
	{
		if (!isShadowItem())
		{
			return;
		}
		
		if ((_mana - count) >= 0)
		{
			_mana -= count;
		}
		else
		{
			_mana = 0;
		}
		
		if (_storedInDb)
		{
			_storedInDb = false;
		}
		if (resetConsumingMana)
		{
			_consumingMana = false;
		}
		
		final Player player = getActingPlayer();
		if (player == null)
		{
			return;
		}
		
		SystemMessage sm;
		switch (_mana)
		{
			case 10:
			{
				sm = new SystemMessage(SystemMessageId.S1_S_REMAINING_MANA_IS_NOW_10);
				sm.addItemName(_item);
				player.sendPacket(sm);
				break;
			}
			case 5:
			{
				sm = new SystemMessage(SystemMessageId.S1_S_REMAINING_MANA_IS_NOW_5);
				sm.addItemName(_item);
				player.sendPacket(sm);
				break;
			}
			case 1:
			{
				sm = new SystemMessage(SystemMessageId.S1_S_REMAINING_MANA_IS_NOW_1_IT_WILL_DISAPPEAR_SOON);
				sm.addItemName(_item);
				player.sendPacket(sm);
				break;
			}
		}
		
		if (_mana == 0) // The life time has expired
		{
			sm = new SystemMessage(SystemMessageId.S1_S_REMAINING_MANA_IS_NOW_0_AND_THE_ITEM_HAS_DISAPPEARED);
			sm.addItemName(_item);
			player.sendPacket(sm);
			
			// unequip
			if (isEquipped())
			{
				final InventoryUpdate iu = new InventoryUpdate();
				for (Item item : player.getInventory().unEquipItemInSlotAndRecord(getLocationSlot()))
				{
					item.unChargeAllShots();
					iu.addModifiedItem(item);
				}
				player.sendPacket(iu);
				player.broadcastUserInfo();
			}
			
			if (_loc != ItemLocation.WAREHOUSE)
			{
				// destroy
				player.getInventory().destroyItem("Item", this, player, null);
				
				// send update
				final InventoryUpdate iu = new InventoryUpdate();
				iu.addRemovedItem(this);
				player.sendPacket(iu);
				
				final StatusUpdate su = new StatusUpdate(player);
				su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
				player.sendPacket(su);
				
			}
			else
			{
				player.getWarehouse().destroyItem("Item", this, player, null);
			}
			
			// delete from world
			World.getInstance().removeObject(this);
		}
		else
		{
			// Reschedule if still equipped
			if (!_consumingMana && isEquipped())
			{
				scheduleConsumeManaTask();
			}
			if (_loc != ItemLocation.WAREHOUSE)
			{
				final InventoryUpdate iu = new InventoryUpdate();
				iu.addModifiedItem(this);
				player.sendPacket(iu);
			}
		}
	}
	
	public void scheduleConsumeManaTask()
	{
		if (_consumingMana)
		{
			return;
		}
		_consumingMana = true;
		ItemManaTaskManager.getInstance().add(this);
	}
	
	/**
	 * Returns false cause item can't be attacked
	 * @return boolean false
	 */
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return false;
	}
	
	/**
	 * This function basically returns a set of functions from Item/Armor/Weapon, but may add additional functions, if this particular item instance is enhanced for a particular player.
	 * @param creature the player
	 * @return the functions list
	 */
	public List<AbstractFunction> getStatFuncs(Creature creature)
	{
		return _item.getStatFuncs(this, creature);
	}
	
	/**
	 * Updates the database.
	 */
	public void updateDatabase()
	{
		updateDatabase(false);
	}
	
	/**
	 * Updates the database.
	 * @param force if the update should necessarily be done.
	 */
	public void updateDatabase(boolean force)
	{
		_dbLock.lock();
		
		try
		{
			if (_existsInDb)
			{
				if ((_ownerId == 0) || (_loc == ItemLocation.VOID) || (_loc == ItemLocation.REFUND) || ((_count == 0) && (_loc != ItemLocation.LEASE)))
				{
					removeFromDb();
				}
				else if (!Config.LAZY_ITEMS_UPDATE || force)
				{
					updateInDb();
				}
			}
			else
			{
				if ((_ownerId == 0) || (_loc == ItemLocation.VOID) || (_loc == ItemLocation.REFUND) || ((_count == 0) && (_loc != ItemLocation.LEASE)))
				{
					return;
				}
				insertIntoDb();
			}
		}
		finally
		{
			_dbLock.unlock();
		}
	}
	
	/**
	 * Returns a Item stored in database from its objectID
	 * @param ownerId
	 * @param rs
	 * @return Item
	 */
	public static Item restoreFromDb(int ownerId, ResultSet rs)
	{
		Item inst = null;
		int objectId;
		int itemId;
		int locData;
		int enchantLevel;
		int customType1;
		int customType2;
		int manaLeft;
		long time;
		long count;
		ItemLocation loc;
		try
		{
			objectId = rs.getInt(1);
			itemId = rs.getInt("item_id");
			count = rs.getLong("count");
			loc = ItemLocation.valueOf(rs.getString("loc"));
			locData = rs.getInt("loc_data");
			enchantLevel = rs.getInt("enchant_level");
			customType1 = rs.getInt("custom_type1");
			customType2 = rs.getInt("custom_type2");
			manaLeft = rs.getInt("mana_left");
			time = rs.getLong("time");
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Could not restore an item owned by " + ownerId + " from DB:", e);
			return null;
		}
		final ItemTemplate item = ItemTable.getInstance().getTemplate(itemId);
		if (item == null)
		{
			LOGGER.severe("Item item_id=" + itemId + " not known, object_id=" + objectId);
			return null;
		}
		inst = new Item(objectId, item);
		inst._ownerId = ownerId;
		inst.setCount(count);
		inst._enchantLevel = enchantLevel;
		inst._type1 = customType1;
		inst._type2 = customType2;
		inst._loc = loc;
		inst._locData = locData;
		inst._existsInDb = true;
		inst._storedInDb = true;
		
		// Setup life time for shadow weapons
		inst._mana = manaLeft;
		inst._time = time;
		
		// load augmentation and elemental enchant
		if (inst.isEquipable())
		{
			inst.restoreAttributes();
		}
		
		return inst;
	}
	
	/**
	 * Init a dropped Item and add it in the world as a visible object.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Set the x,y,z position of the Item dropped and update its _worldregion</li>
	 * <li>Add the Item dropped to _visibleObjects of its WorldRegion</li>
	 * <li>Add the Item dropped in the world as a <b>visible</b> object</li><br>
	 * <font color=#FF0000><b><u>Caution</u>: This method DOESN'T ADD the object to _allObjects of World </b></font><br>
	 * <br>
	 * <b><u>Example of use</u>:</b><br>
	 * <li>Drop item</li>
	 * <li>Call Pet</li>
	 * @param dropper
	 * @param locX
	 * @param locY
	 * @param locZ
	 */
	public void dropMe(Creature dropper, int locX, int locY, int locZ)
	{
		int x = locX;
		int y = locY;
		int z = locZ;
		
		if (dropper != null)
		{
			final Location dropDest = GeoEngine.getInstance().getValidLocation(dropper.getX(), dropper.getY(), dropper.getZ(), x, y, z, dropper.getInstanceId());
			x = dropDest.getX();
			y = dropDest.getY();
			z = dropDest.getZ();
		}
		
		if (dropper != null)
		{
			setInstanceId(dropper.getInstanceId()); // Inherit instancezone when dropped in visible world
		}
		else
		{
			setInstanceId(0); // No dropper? Make it a global item...
		}
		
		// Set the x,y,z position of the Item dropped and update its world region
		setSpawned(true);
		setXYZ(x, y, z);
		
		setDropTime(Chronos.currentTimeMillis());
		setDropperObjectId(dropper != null ? dropper.getObjectId() : 0); // Set the dropper Id for the knownlist packets in sendInfo
		
		// Add the Item dropped in the world as a visible object
		World.getInstance().addVisibleObject(this, getWorldRegion());
		if (Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance().save(this);
		}
		setDropperObjectId(0); // Set the dropper Id back to 0 so it no longer shows the drop packet
		
		if ((dropper != null) && dropper.isPlayer())
		{
			_owner = null;
			
			// Notify to scripts
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerItemDrop(dropper.getActingPlayer(), this, new Location(x, y, z)), getItem());
		}
	}
	
	/**
	 * Update the database with values of the item
	 */
	private void updateInDb()
	{
		if (!_existsInDb || _wear || _storedInDb)
		{
			return;
		}
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE items SET owner_id=?,count=?,loc=?,loc_data=?,enchant_level=?,custom_type1=?,custom_type2=?,mana_left=?,time=? WHERE object_id = ?"))
		{
			ps.setInt(1, _ownerId);
			ps.setLong(2, _count);
			ps.setString(3, _loc.name());
			ps.setInt(4, _locData);
			ps.setInt(5, _enchantLevel);
			ps.setInt(6, _type1);
			ps.setInt(7, _type2);
			ps.setInt(8, _mana);
			ps.setLong(9, _time);
			ps.setInt(10, getObjectId());
			ps.executeUpdate();
			_existsInDb = true;
			_storedInDb = true;
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Could not update item " + this + " in DB: Reason: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Insert the item in database
	 */
	private void insertIntoDb()
	{
		if (_existsInDb || (getObjectId() == 0) || _wear)
		{
			return;
		}
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO items (owner_id,item_id,count,loc,loc_data,enchant_level,object_id,custom_type1,custom_type2,mana_left,time) VALUES (?,?,?,?,?,?,?,?,?,?,?)"))
		{
			ps.setInt(1, _ownerId);
			ps.setInt(2, _itemId);
			ps.setLong(3, _count);
			ps.setString(4, _loc.name());
			ps.setInt(5, _locData);
			ps.setInt(6, _enchantLevel);
			ps.setInt(7, getObjectId());
			ps.setInt(8, _type1);
			ps.setInt(9, _type2);
			ps.setInt(10, _mana);
			ps.setLong(11, _time);
			
			ps.executeUpdate();
			_existsInDb = true;
			_storedInDb = true;
			
			if (_augmentation != null)
			{
				updateItemAttributes(con);
			}
			if (_elementals != null)
			{
				updateItemElements(con);
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Could not insert item " + this + " into DB: Reason: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Delete item from database
	 */
	private void removeFromDb()
	{
		if (!_existsInDb || _wear)
		{
			return;
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM items WHERE object_id = ?"))
			{
				ps.setInt(1, getObjectId());
				ps.executeUpdate();
				_existsInDb = false;
				_storedInDb = false;
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM item_attributes WHERE itemId = ?"))
			{
				ps.setInt(1, getObjectId());
				ps.executeUpdate();
			}
			
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM item_elementals WHERE itemId = ?"))
			{
				ps.setInt(1, getObjectId());
				ps.executeUpdate();
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Could not delete item " + this + " in DB: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Returns the item in String format
	 * @return String
	 */
	@Override
	public String toString()
	{
		return _item + "[" + getObjectId() + "]";
	}
	
	public void resetOwnerTimer()
	{
		if (_itemLootShedule != null)
		{
			_itemLootShedule.cancel(true);
			_itemLootShedule = null;
		}
	}
	
	public void setItemLootShedule(ScheduledFuture<?> sf)
	{
		_itemLootShedule = sf;
	}
	
	public ScheduledFuture<?> getItemLootShedule()
	{
		return _itemLootShedule;
	}
	
	public void setProtected(boolean isProtected)
	{
		_protected = isProtected;
	}
	
	public boolean isProtected()
	{
		return _protected;
	}
	
	public boolean isNightLure()
	{
		return (((_itemId >= 8505) && (_itemId <= 8513)) || (_itemId == 8485));
	}
	
	public void setCountDecrease(boolean decrease)
	{
		_decrease = decrease;
	}
	
	public boolean getCountDecrease()
	{
		return _decrease;
	}
	
	public void setInitCount(int initCount)
	{
		_initCount = initCount;
	}
	
	public long getInitCount()
	{
		return _initCount;
	}
	
	public void restoreInitCount()
	{
		if (_decrease)
		{
			setCount(_initCount);
		}
	}
	
	public boolean isTimeLimitedItem()
	{
		return _time > 0;
	}
	
	/**
	 * Returns (current system time + time) of this time limited item
	 * @return Time
	 */
	public long getTime()
	{
		return _time;
	}
	
	public long getRemainingTime()
	{
		return _time - Chronos.currentTimeMillis();
	}
	
	public void endOfLife()
	{
		final Player player = getActingPlayer();
		if (player == null)
		{
			return;
		}
		
		if (isEquipped())
		{
			final InventoryUpdate iu = new InventoryUpdate();
			for (Item item : player.getInventory().unEquipItemInSlotAndRecord(getLocationSlot()))
			{
				item.unChargeAllShots();
				iu.addModifiedItem(item);
			}
			player.sendPacket(iu);
			player.broadcastUserInfo();
		}
		
		if (_loc != ItemLocation.WAREHOUSE)
		{
			// destroy
			player.getInventory().destroyItem("Item", this, player, null);
			
			// send update
			final InventoryUpdate iu = new InventoryUpdate();
			iu.addRemovedItem(this);
			player.sendPacket(iu);
			
			final StatusUpdate su = new StatusUpdate(player);
			su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
			player.sendPacket(su);
			
		}
		else
		{
			player.getWarehouse().destroyItem("Item", this, player, null);
		}
		player.sendPacket(SystemMessageId.THE_LIMITED_TIME_ITEM_HAS_DISAPPEARED_BECAUSE_THE_REMAINING_TIME_RAN_OUT);
		// delete from world
		World.getInstance().removeObject(this);
	}
	
	public void scheduleLifeTimeTask()
	{
		if (!isTimeLimitedItem())
		{
			return;
		}
		if (getRemainingTime() <= 0)
		{
			endOfLife();
		}
		else
		{
			ItemLifeTimeTaskManager.getInstance().add(this, getTime());
		}
	}
	
	public void updateElementAttrBonus(Player player)
	{
		if (_elementals == null)
		{
			return;
		}
		for (Elementals elm : _elementals)
		{
			elm.updateBonus(player, isArmor());
		}
	}
	
	public void removeElementAttrBonus(Player player)
	{
		if (_elementals == null)
		{
			return;
		}
		for (Elementals elm : _elementals)
		{
			elm.removeBonus(player);
		}
	}
	
	public void setDropperObjectId(int id)
	{
		_dropperObjectId = id;
	}
	
	@Override
	public void sendInfo(Player player)
	{
		if (_dropperObjectId != 0)
		{
			player.sendPacket(new DropItem(this, _dropperObjectId));
		}
		else
		{
			player.sendPacket(new SpawnItem(this));
		}
	}
	
	public DropProtection getDropProtection()
	{
		return _dropProtection;
	}
	
	public boolean isPublished()
	{
		return _published;
	}
	
	public void publish()
	{
		_published = true;
	}
	
	@Override
	public boolean decayMe()
	{
		if (Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance().removeObject(this);
		}
		
		return super.decayMe();
	}
	
	public boolean isQuestItem()
	{
		return _item.isQuestItem();
	}
	
	public boolean isElementable()
	{
		return ((_loc == ItemLocation.INVENTORY) || (_loc == ItemLocation.PAPERDOLL)) && _item.isElementable();
	}
	
	public boolean isFreightable()
	{
		return _item.isFreightable();
	}
	
	public int useSkillDisTime()
	{
		return _item.useSkillDisTime();
	}
	
	public int getOlyEnchantLevel()
	{
		final Player player = getActingPlayer();
		int enchant = _enchantLevel;
		
		if (player == null)
		{
			return enchant;
		}
		
		if (player.isInOlympiadMode() && (Config.ALT_OLY_ENCHANT_LIMIT >= 0) && (enchant > Config.ALT_OLY_ENCHANT_LIMIT))
		{
			enchant = Config.ALT_OLY_ENCHANT_LIMIT;
		}
		
		return enchant;
	}
	
	public int getDefaultEnchantLevel()
	{
		return _item.getDefaultEnchantLevel();
	}
	
	public boolean hasPassiveSkills()
	{
		return (_item.getItemType() == EtcItemType.RUNE) && (_loc == ItemLocation.INVENTORY) && (_ownerId > 0) && _item.hasSkills();
	}
	
	public void giveSkillsToOwner()
	{
		if (!hasPassiveSkills())
		{
			return;
		}
		
		final Player player = getActingPlayer();
		
		if (player != null)
		{
			for (SkillHolder sh : _item.getSkills())
			{
				if (sh.getSkill().isPassive())
				{
					player.addSkill(sh.getSkill(), false);
				}
			}
		}
	}
	
	public void removeSkillsFromOwner()
	{
		if (!hasPassiveSkills())
		{
			return;
		}
		
		final Player player = getActingPlayer();
		
		if (player != null)
		{
			for (SkillHolder sh : _item.getSkills())
			{
				if (sh.getSkill().isPassive())
				{
					player.removeSkill(sh.getSkill(), false, true);
				}
			}
		}
	}
	
	@Override
	public boolean isItem()
	{
		return true;
	}
	
	@Override
	public Player getActingPlayer()
	{
		if ((_owner == null) && (_ownerId != 0))
		{
			_owner = World.getInstance().getPlayer(_ownerId);
		}
		return _owner;
	}
	
	public int getEquipReuseDelay()
	{
		return _item.getEquipReuseDelay();
	}
	
	/**
	 * @param player
	 * @param command
	 */
	public void onBypassFeedback(Player player, String command)
	{
		if (!command.startsWith("Quest"))
		{
			return;
		}
		
		final String questName = command.substring(6);
		String event = null;
		final int idx = questName.indexOf(' ');
		if (idx > 0)
		{
			event = questName.substring(idx).trim();
		}
		
		if (event != null)
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnItemBypassEvent(this, player, event), getItem());
		}
		else
		{
			EventDispatcher.getInstance().notifyEventAsync(new OnItemTalk(this, player), getItem());
		}
	}
	
	@Override
	public boolean isChargedShot(ShotType type)
	{
		return (_shotsMask & type.getMask()) == type.getMask();
	}
	
	@Override
	public void setChargedShot(ShotType type, boolean charged)
	{
		if (charged)
		{
			_shotsMask |= type.getMask();
		}
		else
		{
			_shotsMask &= ~type.getMask();
		}
	}
	
	public void unChargeAllShots()
	{
		_shotsMask = 0;
	}
	
	/**
	 * Returns enchant effect object for this item
	 * @return enchanteffect
	 */
	public int[] getEnchantOptions()
	{
		final EnchantOptions op = EnchantItemOptionsData.getInstance().getOptions(this);
		return op != null ? op.getOptions() : DEFAULT_ENCHANT_OPTIONS;
	}
	
	/**
	 * Clears all the enchant bonuses if item is enchanted and containing bonuses for enchant value.
	 */
	public void clearEnchantStats()
	{
		final Player player = getActingPlayer();
		if (player == null)
		{
			_enchantOptions.clear();
			return;
		}
		
		for (Options op : _enchantOptions)
		{
			op.remove(player);
		}
		_enchantOptions.clear();
	}
	
	/**
	 * Clears and applies all the enchant bonuses if item is enchanted and containing bonuses for enchant value.
	 */
	public void applyEnchantStats()
	{
		final Player player = getActingPlayer();
		if (!isEquipped() || (player == null) || (getEnchantOptions() == DEFAULT_ENCHANT_OPTIONS))
		{
			return;
		}
		
		for (int id : getEnchantOptions())
		{
			final Options options = OptionData.getInstance().getOptions(id);
			if (options != null)
			{
				options.apply(player);
				_enchantOptions.add(options);
			}
			else if (id != 0)
			{
				LOGGER.log(Level.INFO, "applyEnchantStats: Couldn't find option: " + id);
			}
		}
	}
	
	@Override
	public void setHeading(int heading)
	{
	}
	
	public void stopAllTasks()
	{
		ItemLifeTimeTaskManager.getInstance().remove(this);
	}
}
