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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.L2DatabaseFactory;
import com.l2jmobius.gameserver.GeoData;
import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.datatables.ItemTable;
import com.l2jmobius.gameserver.instancemanager.ItemsOnGroundManager;
import com.l2jmobius.gameserver.instancemanager.MercTicketManager;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.knownlist.NullKnownList;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.GetItem;
import com.l2jmobius.gameserver.skills.funcs.Func;
import com.l2jmobius.gameserver.templates.L2Armor;
import com.l2jmobius.gameserver.templates.L2EtcItem;
import com.l2jmobius.gameserver.templates.L2Item;

/**
 * This class manages items.
 * @version $Revision: 1.4.2.1.2.11 $ $Date: 2005/03/31 16:07:50 $
 */
public final class L2ItemInstance extends L2Object
{
	private static final Logger _log = Logger.getLogger(L2ItemInstance.class.getName());
	private static final Logger _logItems = Logger.getLogger("item");
	
	/** Enumeration of locations for item */
	public static enum ItemLocation
	{
		VOID,
		INVENTORY,
		PAPERDOLL,
		WAREHOUSE,
		CLANWH,
		PET,
		PET_EQUIP,
		LEASE,
		FREIGHT,
		NPC
	}
	
	/** ID of the owner */
	private int _owner_id;
	
	/** Quantity of the item */
	private int _count;
	
	/** Initial Quantity of the item */
	private int _initCount;
	
	/** Time after restore Item count (in Hours) */
	private int _time;
	
	/** Quantity of the item can decrease */
	private boolean _decrease = false;
	
	/** ID of the item */
	private final int _itemId;
	
	/** Object L2Item associated to the item */
	private final L2Item _item;
	
	/** Location of the item : Inventory, PaperDoll, WareHouse */
	private ItemLocation _loc;
	
	/** Slot where item is stored */
	private int _loc_data;
	
	/** Level of enchantment of the item */
	private int _enchantLevel;
	
	/** Price of the item for selling */
	private int _price_sell;
	
	/** Price of the item for buying */
	private int _price_buy;
	
	/** Wear Item */
	private boolean _wear;
	
	/** Custom item types (used loto, race tickets) */
	private int _type1;
	private int _type2;
	
	private long _dropTime;
	
	public static final int CHARGED_NONE = 0;
	public static final int CHARGED_SOULSHOT = 1;
	public static final int CHARGED_SPIRITSHOT = 1;
	public static final int CHARGED_BLESSED_SOULSHOT = 2; // It's a realy exists? ;-)
	public static final int CHARGED_BLESSED_SPIRITSHOT = 2;
	
	/** Item charged with SoulShot (type of SoulShot) */
	private int _chargedSoulshot = CHARGED_NONE;
	/** Item charged with SpiritShot (type of SpiritShot) */
	private int _chargedSpiritshot = CHARGED_NONE;
	
	private boolean _chargedFishtshot = false;
	
	private boolean _protected;
	
	public static final int UNCHANGED = 0;
	public static final int ADDED = 1;
	public static final int REMOVED = 3;
	public static final int MODIFIED = 2;
	private int _lastChange = 2; // 1 ??, 2 modified, 3 removed
	private boolean _existsInDb; // if a record exists in DB.
	private boolean _storedInDb; // if DB data is up-to-date.
	
	private final ReentrantLock _dbLock = new ReentrantLock();
	
	private ScheduledFuture<?> itemLootSchedule = null;
	
	private final DropProtection _dropProtection = new DropProtection();
	
	/**
	 * Constructor of the L2ItemInstance from the objectId and the itemId.
	 * @param objectId : int designating the ID of the object in the world
	 * @param itemId : int designating the ID of the item
	 */
	public L2ItemInstance(int objectId, int itemId)
	{
		super(objectId);
		super.setKnownList(new NullKnownList(this));
		_itemId = itemId;
		_item = ItemTable.getInstance().getTemplate(itemId);
		if ((_itemId == 0) || (_item == null))
		{
			throw new IllegalArgumentException();
		}
		_count = 1;
		_loc = ItemLocation.VOID;
		_type1 = 0;
		_type2 = 0;
		_dropTime = 0;
	}
	
	/**
	 * Constructor of the L2ItemInstance from the objetId and the description of the item given by the L2Item.
	 * @param objectId : int designating the ID of the object in the world
	 * @param item : L2Item containing informations of the item
	 */
	public L2ItemInstance(int objectId, L2Item item)
	{
		super(objectId);
		super.setKnownList(new NullKnownList(this));
		_itemId = item.getItemId();
		_item = item;
		if ((_itemId == 0) || (_item == null))
		{
			throw new IllegalArgumentException();
		}
		_count = 1;
		_loc = ItemLocation.VOID;
	}
	
	/**
	 * Remove a L2ItemInstance from the world and send server->client GetItem packets.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Send a Server->Client Packet GetItem to player that pick up and its _knowPlayers member</li>
	 * <li>Remove the L2Object from the world</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from _allObjects of L2World </B></FONT><BR>
	 * <BR>
	 * <B><U> Assert </U> :</B><BR>
	 * <BR>
	 * <li>this instanceof L2ItemInstance</li>
	 * <li>_worldRegion != null <I>(L2Object is visible at the beginning)</I></li><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Do Pickup Item : PCInstance and Pet</li><BR>
	 * <BR>
	 * @param player Player that pick up the item
	 */
	public final void pickupMe(L2Character player)
	{
		if (Config.ASSERT)
		{
			assert getPosition().getWorldRegion() != null;
		}
		
		final L2WorldRegion oldregion = getPosition().getWorldRegion();
		
		// Create a server->client GetItem packet to pick up the L2ItemInstance
		final GetItem gi = new GetItem(this, player.getObjectId());
		player.broadcastPacket(gi);
		
		synchronized (this)
		{
			getPosition().setWorldRegion(null);
		}
		
		// if this item is a mercenary ticket, remove the spawns!
		final int itemId = getItemId();
		
		if (MercTicketManager.getInstance().getTicketCastleId(itemId) > 0)
		{
			MercTicketManager.getInstance().removeTicket(this);
			ItemsOnGroundManager.getInstance().removeObject(this);
		}
		
		if ((itemId == 57) || (itemId == 6353))
		{
			final L2PcInstance actor = player.getActingPlayer();
			if (actor != null)
			{
				final QuestState qs = actor.getQuestState("255_Tutorial");
				if (qs != null)
				{
					qs.getQuest().notifyEvent("CE" + itemId + "", null, actor);
				}
			}
		}
		
		// outside of synchronized, to avoid deadlocks
		// Remove the L2ItemInstance from the world
		L2World.getInstance().removeVisibleObject(this, oldregion);
	}
	
	/**
	 * Sets the ownerID of the item
	 * @param process : String Identifier of process triggering this action
	 * @param owner_id : int designating the ID of the owner
	 * @param creator : L2PcInstance Player requesting the item creation
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 */
	public void setOwnerId(String process, int owner_id, L2PcInstance creator, L2Object reference)
	{
		setOwnerId(owner_id);
		
		if (Config.LOG_ITEMS)
		{
			final LogRecord record = new LogRecord(Level.INFO, "CHANGE:" + process);
			record.setLoggerName("item");
			record.setParameters(new Object[]
			{
				this,
				creator,
				reference
			});
			_logItems.log(record);
		}
	}
	
	/**
	 * Sets the ownerID of the item
	 * @param owner_id : int designating the ID of the owner
	 */
	public void setOwnerId(int owner_id)
	{
		if (owner_id == _owner_id)
		{
			return;
		}
		
		_owner_id = owner_id;
		_storedInDb = false;
	}
	
	/**
	 * Returns the ownerID of the item
	 * @return int : ownerID of the item
	 */
	public int getOwnerId()
	{
		return _owner_id;
	}
	
	/**
	 * Sets the location of the item
	 * @param loc : ItemLocation (enumeration)
	 */
	public void setLocation(ItemLocation loc)
	{
		setLocation(loc, 0);
	}
	
	/**
	 * Sets the location of the item.<BR>
	 * <BR>
	 * <U><I>Remark :</I></U> If loc and loc_data different from database, say datas not up-to-date
	 * @param loc : ItemLocation (enumeration)
	 * @param loc_data : int designating the slot where the item is stored or the village for freights
	 */
	public void setLocation(ItemLocation loc, int loc_data)
	{
		if ((loc == _loc) && (loc_data == _loc_data))
		{
			return;
		}
		_loc = loc;
		_loc_data = loc_data;
		_storedInDb = false;
	}
	
	public ItemLocation getLocation()
	{
		return _loc;
	}
	
	/**
	 * Returns the quantity of item
	 * @return int
	 */
	public int getCount()
	{
		return _count;
	}
	
	/**
	 * Sets the quantity of the item.<BR>
	 * <BR>
	 * <U><I>Remark :</I></U> If loc and loc_data different from database, say datas not up-to-date
	 * @param process : String Identifier of process triggering this action
	 * @param count : int
	 * @param creator : L2PcInstance Player requesting the item creation
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 */
	public void changeCount(String process, int count, L2PcInstance creator, L2Object reference)
	{
		if (count == 0)
		{
			return;
		}
		
		if ((count > 0) && (_count > (Integer.MAX_VALUE - count)))
		{
			_count = Integer.MAX_VALUE;
		}
		else
		{
			_count += count;
		}
		
		if (_count < 0)
		{
			_count = 0;
		}
		
		_storedInDb = false;
		
		if (Config.LOG_ITEMS && (process != null))
		{
			final LogRecord record = new LogRecord(Level.INFO, "CHANGE:" + process);
			record.setLoggerName("item");
			record.setParameters(new Object[]
			{
				this,
				creator,
				reference
			});
			_logItems.log(record);
		}
	}
	
	/**
	 * Sets the quantity of the item.<BR>
	 * <BR>
	 * <U><I>Remark :</I></U> If loc and loc_data different from database, say datas not up-to-date
	 * @param count : int
	 */
	public void setCount(int count)
	{
		if (_count == count)
		{
			return;
		}
		
		_count = count >= -1 ? count : 0;
		_storedInDb = false;
	}
	
	/**
	 * Returns if item is equipable
	 * @return boolean
	 */
	public boolean isEquipable()
	{
		return !((_item.getBodyPart() == 0) || (_item instanceof L2EtcItem));
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
	public int getEquipSlot()
	{
		if (Config.ASSERT)
		{
			assert (_loc == ItemLocation.PAPERDOLL) || (_loc == ItemLocation.PET_EQUIP) || (_loc == ItemLocation.FREIGHT);
		}
		return _loc_data;
	}
	
	/**
	 * Returns the characteristics of the item
	 * @return L2Item
	 */
	public L2Item getItem()
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
	
	public boolean isWear()
	{
		return _wear;
	}
	
	public void setWear(boolean newwear)
	{
		_wear = newwear;
	}
	
	/**
	 * Returns the type of item
	 * @return Enum
	 */
	public Enum<?> getItemType()
	{
		return _item.getItemType();
	}
	
	/**
	 * Returns the ID of the item
	 * @return int
	 */
	public int getItemId()
	{
		return _itemId;
	}
	
	/**
	 * Returns the quantity of crystals for crystallization
	 * @return int
	 */
	public final int getCrystalCount()
	{
		return _item.getCrystalCount(_enchantLevel);
	}
	
	/**
	 * Returns the reference price of the item
	 * @return int
	 */
	public int getReferencePrice()
	{
		return _item.getReferencePrice();
	}
	
	/**
	 * Returns the name of the item
	 * @return String
	 */
	public String getItemName()
	{
		return _item.getName();
	}
	
	/**
	 * Returns the price of the item for selling
	 * @return int
	 */
	public int getPriceToSell()
	{
		return (isConsumable() ? (int) (_price_sell * Config.RATE_CONSUMABLE_COST) : _price_sell);
	}
	
	/**
	 * Sets the price of the item for selling <U><I>Remark :</I></U> If loc and loc_data different from database, say datas not up-to-date
	 * @param price : int designating the price
	 */
	public void setPriceToSell(int price)
	{
		_price_sell = price;
		_storedInDb = false;
	}
	
	/**
	 * Returns the price of the item for buying
	 * @return int
	 */
	public int getPriceToBuy()
	{
		return (isConsumable() ? (int) (_price_buy * Config.RATE_CONSUMABLE_COST) : _price_buy);
	}
	
	/**
	 * Sets the price of the item for buying <U><I>Remark :</I></U> If loc and loc_data different from database, say datas not up-to-date
	 * @param price : int
	 */
	public void setPriceToBuy(int price)
	{
		_price_buy = price;
		_storedInDb = false;
	}
	
	/**
	 * Returns the last change of the item
	 * @return int
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
		return _item.isDropable();
	}
	
	/**
	 * Returns if item is destroyable
	 * @return boolean
	 */
	public boolean isDestroyable()
	{
		return _item.isDestroyable();
	}
	
	/**
	 * Returns if item is tradeable
	 * @return boolean
	 */
	public boolean isTradeable()
	{
		return _item.isTradeable();
	}
	
	/**
	 * Returns if item is consumable
	 * @return boolean
	 */
	public boolean isConsumable()
	{
		return _item.isConsumable();
	}
	
	/**
	 * Returns if item is available for manipulation
	 * @param player
	 * @param allowAdena
	 * @return boolean
	 */
	public boolean isAvailable(L2PcInstance player, boolean allowAdena)
	{
		return ((!isEquipped()) // Not equipped
			&& (getItem().getType2() != 3) // Not Quest Item
			&& ((getItem().getType2() != 4) || (getItem().getType1() != 1)) // TODO: what does this mean?
			&& ((player.getPet() == null) || (getObjectId() != player.getPet().getControlItemId())) // Not Control item of currently summoned pet
			&& (player.getActiveEnchantItem() != this) // Not momentarily used enchant scroll
			&& (allowAdena || (_itemId != 57)) && ((player.getCurrentSkill() == null) || (player.getCurrentSkill().getSkill().getItemConsumeId() != _itemId)) && (isTradeable()));
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.model.L2Object#onAction(com.l2jmobius.gameserver.model.L2PcInstance) also check constraints: only soloing castle owners may pick up mercenary tickets of their castle
	 */
	@Override
	public void onAction(L2PcInstance player)
	{
		// this causes the validate position handler to do the pickup if the location is reached.
		// mercenary tickets can only be picked up by the castle owner.
		final int castleId = MercTicketManager.getInstance().getTicketCastleId(_itemId);
		if ((castleId > 0) && (!player.isCastleLord(castleId) || player.isInParty()))
		{
			if (player.isInParty())
			{
				player.sendMessage("You cannot pickup mercenaries while in party.");
			}
			else
			{
				player.sendMessage("Only the castle lord can pickup mercenaries.");
			}
			
			player.setTarget(this);
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
			player.sendPacket(new ActionFailed());
			
		}
		else
		{
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, this);
		}
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
	 * Sets the level of enchantment of the item
	 * @param enchantLevel
	 */
	public void setEnchantLevel(int enchantLevel)
	{
		if (_enchantLevel == enchantLevel)
		{
			return;
		}
		
		_enchantLevel = enchantLevel;
		_storedInDb = false;
	}
	
	/**
	 * Returns the physical defense of the item
	 * @return int
	 */
	public int getPDef()
	{
		if (_item instanceof L2Armor)
		{
			return ((L2Armor) _item).getPDef();
		}
		return 0;
	}
	
	/**
	 * Returns false cause item can't be attacked
	 * @return boolean false
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}
	
	/**
	 * Returns the type of charge with SoulShot of the item.
	 * @return int (CHARGED_NONE, CHARGED_SOULSHOT)
	 */
	public int getChargedSoulshot()
	{
		return _chargedSoulshot;
	}
	
	/**
	 * Returns the type of charge with SpiritShot of the item
	 * @return int (CHARGED_NONE, CHARGED_SPIRITSHOT, CHARGED_BLESSED_SPIRITSHOT)
	 */
	public int getChargedSpiritshot()
	{
		return _chargedSpiritshot;
	}
	
	public boolean getChargedFishshot()
	{
		return _chargedFishtshot;
	}
	
	/**
	 * Sets the type of charge with SoulShot of the item
	 * @param type : int (CHARGED_NONE, CHARGED_SOULSHOT)
	 */
	public void setChargedSoulshot(int type)
	{
		_chargedSoulshot = type;
	}
	
	/**
	 * Sets the type of charge with SpiritShot of the item
	 * @param type : int (CHARGED_NONE, CHARGED_SPIRITSHOT, CHARGED_BLESSED_SPIRITSHOT)
	 */
	public void setChargedSpiritshot(int type)
	{
		_chargedSpiritshot = type;
	}
	
	public void setChargedFishshot(boolean type)
	{
		_chargedFishtshot = type;
	}
	
	/**
	 * This function basically returns a set of functions from L2Item/L2Armor/L2Weapon, but may add additional functions, if this particular item instance is enhanched for a particular player.
	 * @param player : L2Character designating the player
	 * @return Func[]
	 */
	public Func[] getStatFuncs(L2Character player)
	{
		return getItem().getStatFuncs(this, player);
	}
	
	/**
	 * Updates the database.<BR>
	 */
	public void updateDatabase()
	{
		updateDatabase(false);
	}
	
	/**
	 * Updates the database.<BR>
	 * @param force if the update should necessarilly be done.
	 */
	public void updateDatabase(boolean force)
	{
		if (isWear())
		{
			return;
		}
		
		_dbLock.lock();
		try
		{
			if (_existsInDb)
			{
				if ((_owner_id == 0) || (_loc == ItemLocation.VOID) || ((_count == 0) && (_loc != ItemLocation.LEASE)))
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
				if ((_count == 0) && (_loc != ItemLocation.LEASE))
				{
					return;
				}
				
				if ((_loc == ItemLocation.VOID) || (_loc == ItemLocation.NPC) || (_owner_id == 0))
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
	 * Returns a L2ItemInstance stored in database from its objectID
	 * @param objectId : int designating the objectID of the item
	 * @return L2ItemInstance
	 */
	public static L2ItemInstance restoreFromDb(int objectId)
	{
		L2ItemInstance inst = null;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT owner_id, object_id, item_id, count, enchant_level, loc, loc_data, price_sell, price_buy, custom_type1, custom_type2 FROM items WHERE object_id = ?"))
		{
			statement.setInt(1, objectId);
			try (ResultSet rs = statement.executeQuery())
			{
				if (rs.next())
				{
					final int owner_id = rs.getInt("owner_id");
					final int item_id = rs.getInt("item_id");
					final int count = rs.getInt("count");
					final ItemLocation loc = ItemLocation.valueOf(rs.getString("loc"));
					final int loc_data = rs.getInt("loc_data");
					final int enchant_level = rs.getInt("enchant_level");
					final int custom_type1 = rs.getInt("custom_type1");
					final int custom_type2 = rs.getInt("custom_type2");
					final int price_sell = rs.getInt("price_sell");
					final int price_buy = rs.getInt("price_buy");
					
					final L2Item item = ItemTable.getInstance().getTemplate(item_id);
					if (item != null)
					{
						inst = new L2ItemInstance(objectId, item);
						inst._owner_id = owner_id;
						inst._count = count;
						inst._enchantLevel = enchant_level;
						inst._type1 = custom_type1;
						inst._type2 = custom_type2;
						inst._loc = loc;
						inst._loc_data = loc_data;
						inst._price_sell = price_sell;
						inst._price_buy = price_buy;
						inst._existsInDb = true;
						inst._storedInDb = true;
					}
					else
					{
						_log.severe("Item item_id=" + item_id + " not known, object_id=" + objectId);
					}
				}
				else
				{
					_log.severe("Item object_id=" + objectId + " not found");
				}
			}
		}
		catch (final Exception e)
		{
			_log.log(Level.SEVERE, "Could not restore item " + objectId + " from DB:", e);
			return null;
		}
		return inst;
	}
	
	public void changeCountWithoutTrace(int count, L2PcInstance creator, L2Object reference)
	{
		changeCount(null, count, creator, reference);
	}
	
	/**
	 * Init a dropped L2ItemInstance and add it in the world as a visible object.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Set the x,y,z position of the L2ItemInstance dropped and update its _worldregion</li>
	 * <li>Add the L2ItemInstance dropped to _visibleObjects of its L2WorldRegion</li>
	 * <li>Add the L2ItemInstance dropped in the world as a <B>visible</B> object</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T ADD the object to _allObjects of L2World </B></FONT><BR>
	 * <BR>
	 * <B><U> Assert </U> :</B><BR>
	 * <BR>
	 * <li>_worldRegion == null <I>(L2Object is invisible at the beginning)</I></li><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Drop item</li>
	 * <li>Call Pet</li><BR>
	 * @param dropper
	 * @param x
	 * @param y
	 * @param z
	 */
	public final void dropMe(L2Character dropper, int x, int y, int z)
	{
		if (Config.ASSERT)
		{
			assert getPosition().getWorldRegion() == null;
		}
		
		if ((Config.GEODATA > 0) && (dropper != null))
		{
			final Location dropDest = GeoData.getInstance().moveCheck(dropper.getX(), dropper.getY(), dropper.getZ(), x, y, z);
			x = dropDest.getX();
			y = dropDest.getY();
			z = dropDest.getZ();
		}
		
		synchronized (this)
		{
			// Set the x,y,z position of the L2ItemInstance dropped and update its _worldregion
			setIsVisible(true);
			getPosition().setWorldPosition(x, y, z);
			getPosition().setWorldRegion(L2World.getInstance().getRegion(getPosition().getWorldPosition()));
			
			// Add the L2ItemInstance dropped to _visibleObjects of its L2WorldRegion
			getPosition().getWorldRegion().addVisibleObject(this);
			
		}
		setDropTime(System.currentTimeMillis());
		
		// this can synchronize on others instancies, so it's out of
		// synchronized, to avoid deadlocks
		// Add the L2ItemInstance dropped in the world as a visible object
		L2World.getInstance().addVisibleObject(this, getPosition().getWorldRegion(), dropper);
		if (Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance().Save(this);
		}
	}
	
	/**
	 * Update the database with values of the item
	 */
	private void updateInDb()
	{
		if (Config.ASSERT)
		{
			assert _existsInDb;
		}
		
		if (_wear)
		{
			return;
		}
		
		if (_storedInDb)
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE items SET owner_id=?,count=?,loc=?,loc_data=?,enchant_level=?,price_sell=?,price_buy=?,custom_type1=?,custom_type2=? " + "WHERE object_id = ?"))
		{
			statement.setInt(1, _owner_id);
			statement.setInt(2, getCount());
			statement.setString(3, _loc.name());
			statement.setInt(4, _loc_data);
			statement.setInt(5, getEnchantLevel());
			statement.setInt(6, _price_sell);
			statement.setInt(7, _price_buy);
			statement.setInt(8, getCustomType1());
			statement.setInt(9, getCustomType2());
			statement.setInt(10, getObjectId());
			statement.executeUpdate();
			_existsInDb = true;
			_storedInDb = true;
		}
		catch (final Exception e)
		{
			_log.log(Level.SEVERE, "Could not update item " + getObjectId() + " in DB: Reason: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Insert the item in database
	 */
	private void insertIntoDb()
	{
		if (_wear)
		{
			return;
		}
		
		if (Config.ASSERT)
		{
			assert !_existsInDb && (getObjectId() != 0);
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO items (owner_id,item_id,count,loc,loc_data,enchant_level,price_sell,price_buy,object_id,custom_type1,custom_type2) VALUES (?,?,?,?,?,?,?,?,?,?,?)"))
		{
			statement.setInt(1, _owner_id);
			statement.setInt(2, _itemId);
			statement.setInt(3, getCount());
			statement.setString(4, _loc.name());
			statement.setInt(5, _loc_data);
			statement.setInt(6, getEnchantLevel());
			statement.setInt(7, _price_sell);
			statement.setInt(8, _price_buy);
			statement.setInt(9, getObjectId());
			statement.setInt(10, _type1);
			statement.setInt(11, _type2);
			
			statement.executeUpdate();
			_existsInDb = true;
			_storedInDb = true;
		}
		catch (final Exception e)
		{
			_log.log(Level.SEVERE, "Could not insert item " + getObjectId() + " into DB: Reason: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Delete item from database
	 */
	private void removeFromDb()
	{
		if (_wear)
		{
			return;
		}
		
		if (Config.ASSERT)
		{
			assert _existsInDb;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM items WHERE object_id=?"))
		{
			statement.setInt(1, getObjectId());
			statement.executeUpdate();
			_existsInDb = false;
			_storedInDb = false;
		}
		catch (final Exception e)
		{
			_log.log(Level.SEVERE, "Could not delete item " + getObjectId() + " in DB: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Returns the item in String format
	 * @return String
	 */
	@Override
	public String toString()
	{
		return "" + _item;
	}
	
	public void resetOwnerTimer()
	{
		if (itemLootSchedule != null)
		{
			itemLootSchedule.cancel(true);
		}
		itemLootSchedule = null;
	}
	
	public void setItemLootSchedule(ScheduledFuture<?> sf)
	{
		itemLootSchedule = sf;
	}
	
	public ScheduledFuture<?> getItemLootSchedule()
	{
		return itemLootSchedule;
	}
	
	public void setProtected(boolean is_protected)
	{
		_protected = is_protected;
	}
	
	public boolean isProtected()
	{
		return _protected;
	}
	
	public void setCountDecrease(boolean decrease)
	{
		_decrease = decrease;
	}
	
	public boolean getCountDecrease()
	{
		return _decrease;
	}
	
	public void setInitCount(int InitCount)
	{
		_initCount = InitCount;
	}
	
	public int getInitCount()
	{
		return _initCount;
	}
	
	public void restoreInitCount()
	{
		if (_decrease)
		{
			_count = _initCount;
		}
	}
	
	public void setTime(int time)
	{
		if (time > 0)
		{
			_time = time;
		}
		else
		{
			_time = 0;
		}
	}
	
	public void setRestoreTime(long savedTimer)
	{
		long remainingTime = savedTimer - System.currentTimeMillis();
		if (remainingTime < 0)
		{
			remainingTime = 0;
		}
		
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() ->
		{
			restoreInitCount();
			dataTimerSave();
		}, remainingTime, (long) getTime() * 60 * 60 * 1000);
	}
	
	protected void dataTimerSave()
	{
		final long timerSave = System.currentTimeMillis() + ((long) getTime() * 60 * 60 * 1000);
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE merchant_buylists SET savetimer =? WHERE time =?"))
		{
			statement.setLong(1, timerSave);
			statement.setInt(2, getTime());
			statement.executeUpdate();
		}
		catch (final Exception e)
		{
			_log.log(Level.SEVERE, "TradeController: Could not update Timer save in Buylist");
		}
	}
	
	public int getTime()
	{
		return _time;
	}
	
	public final DropProtection getDropProtection()
	{
		return _dropProtection;
	}
}