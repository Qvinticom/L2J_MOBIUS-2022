
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
package org.l2jmobius.gameserver.model.quest;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.instancemanager.QuestManager;
import org.l2jmobius.gameserver.model.DropData;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.itemcontainer.PlayerInventory;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.model.skills.Stat;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowQuestMark;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.ItemList;
import org.l2jmobius.gameserver.network.serverpackets.PlaySound;
import org.l2jmobius.gameserver.network.serverpackets.QuestList;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.TutorialShowQuestionMark;

/**
 * Quest state class.
 * @author Luis Arias
 */
public class QuestState
{
	protected static final Logger LOGGER = Logger.getLogger(QuestState.class.getName());
	
	// Constants
	private static final String COND_VAR = "cond";
	
	public static final String SOUND_ACCEPT = "ItemSound.quest_accept";
	public static final String SOUND_ITEMGET = "ItemSound.quest_itemget";
	public static final String SOUND_MIDDLE = "ItemSound.quest_middle";
	public static final String SOUND_FINISH = "ItemSound.quest_finish";
	public static final String SOUND_GIVEUP = "ItemSound.quest_giveup";
	public static final String SOUND_JACKPOT = "ItemSound.quest_jackpot";
	public static final String SOUND_FANFARE = "ItemSound.quest_fanfare_2";
	public static final String SOUND_BEFORE_BATTLE = "Itemsound.quest_before_battle";
	
	public static final byte DROP_DIVMOD = 0;
	public static final byte DROP_FIXED_RATE = 1;
	public static final byte DROP_FIXED_COUNT = 2;
	public static final byte DROP_FIXED_BOTH = 3;
	
	/** The name of the quest of this QuestState */
	private final String _questName;
	
	/** The "owner" of this QuestState object */
	private final PlayerInstance _player;
	
	/** The current state of the quest */
	private byte _state;
	
	/** The current condition of the quest */
	private int _cond = 0;
	
	/** A map of key->value pairs containing the quest state variables and their values */
	private Map<String, String> _vars;
	
	/**
	 * Constructor of the QuestState. Creates the QuestState object and sets the player's progress of the quest to this QuestState.
	 * @param quest the {@link Quest} object associated with the QuestState
	 * @param player the owner of this {@link QuestState} object
	 * @param state the initial state of the quest
	 */
	public QuestState(Quest quest, PlayerInstance player, byte state)
	{
		_questName = quest.getName();
		_player = player;
		_state = state;
		player.setQuestState(this);
	}
	
	/**
	 * @return the name of the quest of this QuestState
	 */
	public String getQuestName()
	{
		return _questName;
	}
	
	/**
	 * @return the {@link Quest} object of this QuestState
	 */
	public Quest getQuest()
	{
		return QuestManager.getInstance().getQuest(_questName);
	}
	
	/**
	 * @return the {@link PlayerInstance} object of the owner of this QuestState
	 */
	public PlayerInstance getPlayer()
	{
		return _player;
	}
	
	/**
	 * @return the current State of this QuestState
	 * @see org.l2jmobius.gameserver.model.quest.State
	 */
	public byte getState()
	{
		return _state;
	}
	
	/**
	 * @return {@code true} if the State of this QuestState is CREATED, {@code false} otherwise
	 * @see org.l2jmobius.gameserver.model.quest.State
	 */
	public boolean isCreated()
	{
		return _state == State.CREATED;
	}
	
	/**
	 * @return {@code true} if the State of this QuestState is STARTED, {@code false} otherwise
	 * @see org.l2jmobius.gameserver.model.quest.State
	 */
	public boolean isStarted()
	{
		return _state == State.STARTED;
	}
	
	/**
	 * @return {@code true} if the State of this QuestState is COMPLETED, {@code false} otherwise
	 * @see org.l2jmobius.gameserver.model.quest.State
	 */
	public boolean isCompleted()
	{
		return _state == State.COMPLETED;
	}
	
	/**
	 * Return state of the quest after its initialization.<br>
	 * <u><i>Actions :</i></u>
	 * <li>Remove drops from previous state</li>
	 * <li>Set new state of the quest</li>
	 * <li>Add drop for new state</li>
	 * <li>Update information in database</li>
	 * <li>Send packet QuestList to client</li><br>
	 * @param state
	 */
	public void setState(byte state)
	{
		if (_state != state)
		{
			_state = state;
			Quest.updateQuestInDb(this);
			_player.sendPacket(new QuestList(_player));
		}
	}
	
	/**
	 * Destroy element used by quest when quest is exited
	 * @param repeatable
	 */
	public void exitQuest(boolean repeatable)
	{
		if (isCompleted())
		{
			return;
		}
		
		// Say quest is completed
		setState(State.COMPLETED);
		
		// Clean registered quest items
		final int[] itemIdList = getQuest().getRegisteredItemIds();
		if (itemIdList != null)
		{
			for (int element : itemIdList)
			{
				takeItems(element, -1);
			}
		}
		
		// If quest is repeatable, delete quest from list of quest of the player and from database (quest CAN be created again => repeatable)
		if (repeatable)
		{
			_player.delQuestState(_questName);
			Quest.deleteQuestInDb(this);
			_vars = null;
		}
		else
		{
			// Otherwise, delete variables for quest and update database (quest CANNOT be created again => not repeatable)
			if (_vars != null)
			{
				for (String variable : _vars.keySet())
				{
					Quest.deleteQuestVarInDb(this, variable);
				}
				_vars.clear();
			}
			Quest.updateQuestInDb(this);
		}
	}
	
	/**
	 * Add parameter used in quests.
	 * @param variable String pointing out the name of the variable for quest
	 * @param value String pointing out the value of the variable for quest
	 */
	public void setInternal(String variable, String value)
	{
		if (_vars == null)
		{
			_vars = new HashMap<>();
		}
		
		if (value == null)
		{
			_vars.put(variable, "");
			return;
		}
		
		if (COND_VAR.equals(variable))
		{
			try
			{
				_cond = Integer.parseInt(value);
			}
			catch (Exception ignored)
			{
			}
		}
		
		_vars.put(variable, value);
	}
	
	/**
	 * Return value of parameter "value" after adding the couple (var,value) in class variable "vars".<br>
	 * Actions:<br>
	 * <ul>
	 * <li>Initialize class variable "vars" if is null.</li>
	 * <li>Initialize parameter "value" if is null</li>
	 * <li>Add/Update couple (var,value) in class variable Map "vars"</li>
	 * <li>If the key represented by "var" exists in Map "vars", the couple (var,value) is updated in the database.<br>
	 * The key is known as existing if the preceding value of the key (given as result of function put()) is not null.<br>
	 * If the key doesn't exist, the couple is added/created in the database</li>
	 * <ul>
	 * @param variable String indicating the name of the variable for quest
	 * @param value String indicating the value of the variable for quest
	 */
	public void set(String variable, String value)
	{
		if (_vars == null)
		{
			_vars = new HashMap<>();
		}
		
		String newValue = value;
		if (newValue == null)
		{
			newValue = "";
		}
		
		final String old = _vars.put(variable, newValue);
		if (old != null)
		{
			Quest.updateQuestVarInDb(this, variable, newValue);
		}
		else
		{
			Quest.createQuestVarInDb(this, variable, newValue);
		}
		
		if (COND_VAR.equals(variable))
		{
			try
			{
				int previousVal = 0;
				try
				{
					previousVal = Integer.parseInt(old);
				}
				catch (Exception ignored)
				{
				}
				int newCond = 0;
				try
				{
					newCond = Integer.parseInt(newValue);
				}
				catch (Exception ignored)
				{
				}
				
				_cond = newCond;
				setCond(newCond, previousVal);
			}
			catch (Exception e)
			{
				LOGGER.log(Level.WARNING, _player.getName() + ", " + _questName + " cond [" + newValue + "] is not an integer.  Value stored, but no packet was sent: " + e.getMessage(), e);
			}
		}
	}
	
	/**
	 * Internally handles the progression of the quest so that it is ready for sending appropriate packets to the client.<br>
	 * <u><i>Actions :</i></u><br>
	 * <ul>
	 * <li>Check if the new progress number resets the quest to a previous (smaller) step.</li>
	 * <li>If not, check if quest progress steps have been skipped.</li>
	 * <li>If skipped, prepare the variable completedStateFlags appropriately to be ready for sending to clients.</li>
	 * <li>If no steps were skipped, flags do not need to be prepared...</li>
	 * <li>If the passed step resets the quest to a previous step, reset such that steps after the parameter are not considered, while skipped steps before the parameter, if any, maintain their info.</li>
	 * </ul>
	 * @param cond the current quest progress condition (0 - 31 including)
	 * @param old the previous quest progress condition to check against
	 */
	private void setCond(int cond, int old)
	{
		if (cond == old)
		{
			return;
		}
		
		int completedStateFlags = 0;
		// cond 0 and 1 do not need completedStateFlags. Also, if cond > 1, the 1st step must
		// always exist (i.e. it can never be skipped). So if cond is 2, we can still safely
		// assume no steps have been skipped.
		// Finally, more than 31 steps CANNOT be supported in any way with skipping.
		if ((cond < 3) || (cond > 31))
		{
			unset("__compltdStateFlags");
		}
		else
		{
			completedStateFlags = getInt("__compltdStateFlags");
		}
		
		// case 1: No steps have been skipped so far...
		if (completedStateFlags == 0)
		{
			// Check if this step also doesn't skip anything. If so, no further work is needed also, in this case, no work is needed if the state is being reset to a smaller value in those cases, skip forward to informing the client about the change...
			// ELSE, if we just now skipped for the first time...prepare the flags!!!
			if (cond > (old + 1))
			{
				// set the most significant bit to 1 (indicates that there exist skipped states)
				// also, ensure that the least significant bit is an 1 (the first step is never skipped, no matter what the cond says)
				completedStateFlags = 0x80000001;
				
				// since no flag had been skipped until now, the least significant bits must all be set to 1, up until "old" number of bits.
				completedStateFlags |= (1 << old) - 1;
				
				// now, just set the bit corresponding to the passed cond to 1 (current step)
				completedStateFlags |= 1 << (cond - 1);
				set("__compltdStateFlags", String.valueOf(completedStateFlags));
			}
		}
		// case 2: There were exist previously skipped steps
		else if (cond < old) // if this is a push back to a previous step, clear all completion flags ahead
		{
			completedStateFlags &= (1 << cond) - 1; // note, this also unsets the flag indicating that there exist skips
			
			// now, check if this resulted in no steps being skipped any more
			if (completedStateFlags == ((1 << cond) - 1))
			{
				unset("__compltdStateFlags");
			}
			else
			{
				// set the most significant bit back to 1 again, to correctly indicate that this skips states.
				// also, ensure that the least significant bit is an 1 (the first step is never skipped, no matter what the cond says)
				completedStateFlags |= 0x80000001;
				set("__compltdStateFlags", String.valueOf(completedStateFlags));
			}
		}
		// If this moves forward, it changes nothing on previously skipped steps.
		// Just mark this state and we are done.
		else
		{
			completedStateFlags |= 1 << (cond - 1);
			set("__compltdStateFlags", String.valueOf(completedStateFlags));
		}
		
		// send a packet to the client to inform it of the quest progress (step change)
		_player.sendPacket(new QuestList(_player));
		
		final int questId = getQuest().getQuestId();
		if ((questId > 0) && (questId < 999) && (cond > 0))
		{
			_player.sendPacket(new ExShowQuestMark(questId));
		}
	}
	
	/**
	 * Removes a quest variable from the list of existing quest variables.
	 * @param variable the name of the variable to remove
	 */
	public void unset(String variable)
	{
		if (_vars == null)
		{
			return;
		}
		
		final String old = _vars.remove(variable);
		if (old != null)
		{
			if (COND_VAR.equals(variable))
			{
				_cond = 0;
			}
			
			Quest.deleteQuestVarInDb(this, variable);
		}
	}
	
	/**
	 * @param variable the name of the variable to get
	 * @return the value of the variable from the list of quest variables
	 */
	public String get(String variable)
	{
		if (_vars == null)
		{
			return null;
		}
		
		return _vars.get(variable);
	}
	
	/**
	 * @param variable the name of the variable to get
	 * @return the integer value of the variable or 0 if the variable does not exist or its value is not an integer
	 */
	public int getInt(String variable)
	{
		int varInt = 0;
		if (_vars != null)
		{
			final String value = _vars.get(variable);
			if (value != null)
			{
				try
				{
					varInt = Integer.parseInt(value);
				}
				catch (Exception e)
				{
					LOGGER.info(_player.getName() + ": variable " + variable + " isn't an integer: returned value will be " + varInt + e);
					if (Config.AUTODELETE_INVALID_QUEST_DATA)
					{
						exitQuest(true);
					}
				}
			}
		}
		return varInt;
	}
	
	/**
	 * Return the value of the variable of quest represented by "var"
	 * @param variable : name of the variable of quest
	 * @return String
	 */
	public String getString(String variable)
	{
		if (_vars == null)
		{
			return "";
		}
		
		return _vars.get(variable);
	}
	
	/**
	 * Checks if the quest state progress ({@code cond}) is at the specified step.
	 * @param condition the condition to check against
	 * @return {@code true} if the quest condition is equal to {@code condition}, {@code false} otherwise
	 * @see #getInt(String var)
	 */
	public boolean isCond(int condition)
	{
		return _cond == condition;
	}
	
	/**
	 * Sets the quest state progress ({@code cond}) to the specified step.
	 * @param value the new value of the quest state progress
	 * @see #set(String var, String value)
	 */
	public void setCond(int value)
	{
		if (isStarted())
		{
			set(COND_VAR, Integer.toString(value));
		}
	}
	
	/**
	 * @return the current quest progress ({@code cond})
	 */
	public int getCond()
	{
		if (isStarted())
		{
			return _cond;
		}
		
		return 0;
	}
	
	/**
	 * Add player to get notification of characters death
	 * @param creature : Creature of the creature to get notification of death
	 */
	public void addNotifyOfDeath(Creature creature)
	{
		if (creature == null)
		{
			return;
		}
		
		creature.addNotifyQuestOfDeath(this);
	}
	
	/**
	 * Return the quantity of one sort of item hold by the player
	 * @param itemId : ID of the item wanted to be count
	 * @return int
	 */
	public int getQuestItemsCount(int itemId)
	{
		int count = 0;
		if ((_player != null) && (_player.getInventory() != null) && (_player.getInventory().getItems() != null))
		{
			for (ItemInstance item : _player.getInventory().getItems())
			{
				if ((item != null) && (item.getItemId() == itemId))
				{
					count += item.getCount();
				}
			}
		}
		return count;
	}
	
	/**
	 * Check for an item in player's inventory.
	 * @param itemId the ID of the item to check for
	 * @return {@code true} if the item exists in player's inventory, {@code false} otherwise
	 */
	public boolean hasQuestItems(int itemId)
	{
		return _player.getInventory().getItemByItemId(itemId) != null;
	}
	
	/**
	 * Check if player possesses at least one given item.
	 * @param itemIds a list of item IDs to check for
	 * @return {@code true} if at least one item exists in player's inventory, {@code false} otherwise
	 */
	public boolean hasAtLeastOneQuestItem(int... itemIds)
	{
		final PlayerInventory inv = _player.getInventory();
		for (int itemId : itemIds)
		{
			if (inv.getItemByItemId(itemId) != null)
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Check for multiple items in player's inventory.
	 * @param itemIds a list of item IDs to check for
	 * @return {@code true} if all items exist in player's inventory, {@code false} otherwise
	 */
	public boolean hasQuestItems(int... itemIds)
	{
		final PlayerInventory inv = _player.getInventory();
		for (int itemId : itemIds)
		{
			if (inv.getItemByItemId(itemId) == null)
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Give item/reward to the player
	 * @param itemId
	 * @param count
	 */
	public synchronized void giveItems(int itemId, int count)
	{
		giveItems(itemId, count, 0);
	}
	
	public synchronized void giveItems(int itemId, int amount, int enchantlevel)
	{
		if (amount <= 0)
		{
			return;
		}
		
		final int questId = getQuest().getQuestId();
		
		// If item for reward is gold (ID=57), modify count with rate for quest reward
		int count = amount;
		if ((itemId == 57) && ((questId < 217) || (questId > 233)) && ((questId < 401) || (questId > 418)))
		{
			count = (int) (count * Config.RATE_QUESTS_REWARD);
		}
		
		// Set quantity of item
		// Add items to player's inventory
		final ItemInstance item = _player.getInventory().addItem("Quest", itemId, count, _player, _player.getTarget());
		if (item == null)
		{
			return;
		}
		
		if (enchantlevel > 0)
		{
			item.setEnchantLevel(enchantlevel);
		}
		
		// If item for reward is gold, send message of gold reward to client
		if (itemId == 57)
		{
			final SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S1_ADENA);
			smsg.addNumber(count);
			_player.sendPacket(smsg);
		}
		// Otherwise, send message of object reward to client
		else if (count > 1)
		{
			final SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S2_S1_S);
			smsg.addItemName(item.getItemId());
			smsg.addNumber(count);
			_player.sendPacket(smsg);
		}
		else
		{
			final SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_HAVE_EARNED_S1);
			smsg.addItemName(item.getItemId());
			_player.sendPacket(smsg);
		}
		_player.sendPacket(new ItemList(_player, false));
		
		final StatusUpdate su = new StatusUpdate(_player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, _player.getCurrentLoad());
		_player.sendPacket(su);
	}
	
	/**
	 * Remove items from player's inventory when talking to NPC in order to have rewards.<br>
	 * <u><i>Actions :</i></u>
	 * <li>Destroy quantity of items wanted</li>
	 * <li>Send new inventory list to player</li><br>
	 * @param itemId : Identifier of the item
	 * @param amount : Quantity of items to destroy
	 */
	public void takeItems(int itemId, int amount)
	{
		// Get object item from player's inventory list
		final ItemInstance item = _player.getInventory().getItemByItemId(itemId);
		if (item == null)
		{
			return;
		}
		
		if (_player.isProcessingTransaction())
		{
			_player.cancelActiveTrade();
		}
		
		// Tests on count value in order not to have negative value
		int count = amount;
		if ((count < 0) || (count > item.getCount()))
		{
			count = item.getCount();
		}
		
		// Destroy the quantity of items wanted
		if (itemId == 57)
		{
			_player.reduceAdena("Quest", count, _player, true);
		}
		else
		{
			// Fix for destroyed quest items
			if (item.isEquipped())
			{
				_player.getInventory().unEquipItemInBodySlotAndRecord(item.getItem().getBodyPart());
			}
			
			_player.destroyItemByItemId("Quest", itemId, count, _player, true);
		}
		
		// on quests, always refresh inventory
		final InventoryUpdate iu = new InventoryUpdate();
		iu.addItem(item);
		_player.sendPacket(iu);
	}
	
	/**
	 * Drop items to the player's inventory. Rate is 100%, amount is affected by Config.RATE_QUEST_DROP.
	 * @param itemId : Identifier of the item to be dropped.
	 * @param count : Quantity of items to be dropped.
	 * @param neededCount : Quantity of items needed to complete the task. If set to 0, unlimited amount is collected.
	 * @return boolean : Indicating whether item quantity has been reached.
	 */
	public boolean dropItemsAlways(int itemId, int count, int neededCount)
	{
		return dropItems(itemId, count, neededCount, DropData.MAX_CHANCE, DROP_FIXED_RATE);
	}
	
	/**
	 * Drop items to the player's inventory. Rate and amount is affected by DIVMOD of Config.RATE_QUEST_DROP.
	 * @param itemId : Identifier of the item to be dropped.
	 * @param count : Quantity of items to be dropped.
	 * @param neededCount : Quantity of items needed to complete the task. If set to 0, unlimited amount is collected.
	 * @param dropChance : Item drop rate (100% chance is defined by the DropData.MAX_CHANCE = 1.000.000).
	 * @return boolean : Indicating whether item quantity has been reached.
	 */
	public boolean dropItems(int itemId, int count, int neededCount, int dropChance)
	{
		return dropItems(itemId, count, neededCount, dropChance, DROP_DIVMOD);
	}
	
	/**
	 * Drop items to the player's inventory.
	 * @param itemId : Identifier of the item to be dropped.
	 * @param count : Quantity of items to be dropped.
	 * @param neededCount : Quantity of items needed to complete the task. If set to 0, unlimited amount is collected.
	 * @param dropChance : Item drop rate (100% chance is defined by the DropData.MAX_CHANCE = 1.000.000).
	 * @param type : Item drop behavior: DROP_DIVMOD (rate and), DROP_FIXED_RATE, DROP_FIXED_COUNT or DROP_FIXED_BOTH
	 * @return boolean : Indicating whether item quantity has been reached.
	 */
	public boolean dropItems(int itemId, int count, int neededCount, int dropChance, byte type)
	{
		// Get current amount of item.
		final int currentCount = getQuestItemsCount(itemId);
		
		// Required amount reached already?
		if ((neededCount > 0) && (currentCount >= neededCount))
		{
			return true;
		}
		
		int amount = 0;
		switch (type)
		{
			case DROP_DIVMOD:
				final int chanceWithQuestRate = (int) (dropChance * Config.RATE_DROP_QUEST);
				amount = count * (chanceWithQuestRate / DropData.MAX_CHANCE);
				if (Rnd.get(DropData.MAX_CHANCE) < (chanceWithQuestRate % DropData.MAX_CHANCE))
				{
					amount += count;
				}
				break;
			
			case DROP_FIXED_RATE:
				if (Rnd.get(DropData.MAX_CHANCE) < dropChance)
				{
					amount = (int) (count * Config.RATE_DROP_QUEST);
				}
				break;
			
			case DROP_FIXED_COUNT:
				if (Rnd.get(DropData.MAX_CHANCE) < (dropChance * Config.RATE_DROP_QUEST))
				{
					amount = count;
				}
				break;
			
			case DROP_FIXED_BOTH:
				if (Rnd.get(DropData.MAX_CHANCE) < dropChance)
				{
					amount = count;
				}
				break;
		}
		
		boolean reached = false;
		if (amount > 0)
		{
			// Limit count to reach required amount.
			if (neededCount > 0)
			{
				reached = (currentCount + amount) >= neededCount;
				amount = (reached) ? neededCount - currentCount : amount;
			}
			
			// Inventory slot check.
			if (!_player.getInventory().validateCapacityByItemId(itemId))
			{
				return false;
			}
			
			// Give items to the player.
			giveItems(itemId, amount, 0);
			
			// Play the sound.
			playSound(reached ? SOUND_MIDDLE : SOUND_ITEMGET);
		}
		
		return (neededCount > 0) && reached;
	}
	
	/**
	 * Drop multiple items to the player's inventory. Rate and amount is affected by DIVMOD of Config.RATE_QUEST_DROP.
	 * @param rewardsInfos : Infos regarding drops (itemId, count, neededCount, dropChance).
	 * @return boolean : Indicating whether item quantity has been reached.
	 */
	public boolean dropMultipleItems(int[][] rewardsInfos)
	{
		return dropMultipleItems(rewardsInfos, DROP_DIVMOD);
	}
	
	/**
	 * Drop items to the player's inventory.
	 * @param rewardsInfos : Infos regarding drops (itemId, count, neededCount, dropChance).
	 * @param type : Item drop behavior: DROP_DIVMOD (rate and), DROP_FIXED_RATE, DROP_FIXED_COUNT or DROP_FIXED_BOTH
	 * @return boolean : Indicating whether item quantity has been reached.
	 */
	public boolean dropMultipleItems(int[][] rewardsInfos, byte type)
	{
		// Used for the sound.
		boolean sendSound = false;
		
		// Used for the reached state.
		boolean reached = true;
		
		// For each reward type, calculate the probability of drop.
		for (int[] info : rewardsInfos)
		{
			final int itemId = info[0];
			final int currentCount = getQuestItemsCount(itemId);
			final int neededCount = info[2];
			
			// Required amount reached already?
			if ((neededCount > 0) && (currentCount >= neededCount))
			{
				continue;
			}
			
			final int count = info[1];
			int dropChance = info[3];
			int amount = 0;
			
			switch (type)
			{
				case DROP_DIVMOD:
					final int chanceWithQuestRate = (int) (dropChance * Config.RATE_DROP_QUEST);
					amount = count * (chanceWithQuestRate / DropData.MAX_CHANCE);
					if (Rnd.get(DropData.MAX_CHANCE) < (chanceWithQuestRate % DropData.MAX_CHANCE))
					{
						amount += count;
					}
					break;
				
				case DROP_FIXED_RATE:
					if (Rnd.get(DropData.MAX_CHANCE) < dropChance)
					{
						amount = (int) (count * Config.RATE_DROP_QUEST);
					}
					break;
				
				case DROP_FIXED_COUNT:
					if (Rnd.get(DropData.MAX_CHANCE) < (dropChance * Config.RATE_DROP_QUEST))
					{
						amount = count;
					}
					break;
				
				case DROP_FIXED_BOTH:
					if (Rnd.get(DropData.MAX_CHANCE) < dropChance)
					{
						amount = count;
					}
					break;
			}
			
			if (amount > 0)
			{
				// Limit count to reach required amount.
				if (neededCount > 0)
				{
					amount = ((currentCount + amount) >= neededCount) ? neededCount - currentCount : amount;
				}
				
				// Inventory slot check.
				if (!_player.getInventory().validateCapacityByItemId(itemId))
				{
					continue;
				}
				
				// Give items to the player.
				giveItems(itemId, amount, 0);
				
				// Send sound.
				sendSound = true;
			}
			
			// Illimited needed count or current count being inferior to needed count means the state isn't reached.
			if ((neededCount <= 0) || ((currentCount + amount) < neededCount))
			{
				reached = false;
			}
		}
		
		// Play the sound.
		if (sendSound)
		{
			playSound((reached) ? SOUND_MIDDLE : SOUND_ITEMGET);
		}
		
		return reached;
	}
	
	/**
	 * Reward player with items. The amount is affected by Config.RATE_QUEST_REWARD or Config.RATE_QUEST_REWARD_ADENA.
	 * @param itemId : Identifier of the item.
	 * @param itemCount : Quantity of item to reward before applying multiplier.
	 */
	public void rewardItems(int itemId, int itemCount)
	{
		// if (itemId == 57)
		// {
		// giveItems(itemId, (int) (itemCount * Config.RATE_QUEST_REWARD_ADENA), 0); // TODO: RATE_QUEST_REWARD_ADENA
		// }
		// else
		// {
		giveItems(itemId, (int) (itemCount * Config.RATE_QUESTS_REWARD), 0);
		// }
	}
	
	/**
	 * Drop Quest item using Config.RATE_DROP_QUEST
	 * @param itemId int Item Identifier of the item to be dropped
	 * @param count (minCount, maxCount) : int Quantity of items to be dropped
	 * @param neededCount Quantity of items needed for quest
	 * @param dropChance int Base chance of drop, same as in droplist
	 * @param sound boolean indicating whether to play sound
	 * @return boolean indicating whether player has requested number of items
	 */
	public boolean dropQuestItems(int itemId, int count, int neededCount, int dropChance, boolean sound)
	{
		return dropQuestItems(itemId, count, count, neededCount, dropChance, sound);
	}
	
	public boolean dropQuestItems(int itemId, int minCount, int maxCount, int neededCount, int dropChance, boolean sound)
	{
		final int currentCount = getQuestItemsCount(itemId);
		if ((neededCount > 0) && (currentCount >= neededCount))
		{
			return true;
		}
		
		if (currentCount >= neededCount)
		{
			return true;
		}
		
		int itemCount = 0;
		final int random = Rnd.get(DropData.MAX_CHANCE);
		
		int chanceWithQuestRate = (int) (dropChance * (Config.RATE_DROP_QUEST / (_player.getParty() != null ? _player.getParty().getMemberCount() : 1)));
		while (random < chanceWithQuestRate)
		{
			// Get the item quantity dropped
			if (minCount < maxCount)
			{
				itemCount += Rnd.get(minCount, maxCount);
			}
			else if (minCount == maxCount)
			{
				itemCount += minCount;
			}
			else
			{
				itemCount++;
			}
			
			// Prepare for next iteration if dropChance > DropData.MAX_CHANCE
			chanceWithQuestRate -= DropData.MAX_CHANCE;
		}
		
		if (itemCount > 0)
		{
			// if over neededCount, just fill the gap
			if ((neededCount > 0) && ((currentCount + itemCount) > neededCount))
			{
				itemCount = neededCount - currentCount;
			}
			
			// Inventory slot check
			if (!_player.getInventory().validateCapacityByItemId(itemId))
			{
				return false;
			}
			
			// Mobius: Thread.sleep?
			// just wait 3-5 seconds before the drop
			// try
			// {
			// Thread.sleep(Rnd.get(3, 5) * 1000);
			// }
			// catch (InterruptedException e)
			// {
			// }
			
			// Give the item to Player
			_player.addItem("Quest", itemId, itemCount, _player.getTarget(), true);
			if (sound)
			{
				playSound((currentCount + itemCount) < neededCount ? "Itemsound.quest_itemget" : "Itemsound.quest_middle");
			}
		}
		
		return (neededCount > 0) && ((currentCount + itemCount) >= neededCount);
	}
	
	// TODO: More radar functions need to be added when the radar class is complete.
	public void addRadar(int x, int y, int z)
	{
		_player.getRadar().addMarker(x, y, z);
	}
	
	public void removeRadar(int x, int y, int z)
	{
		_player.getRadar().removeMarker(x, y, z);
	}
	
	public void clearRadar()
	{
		_player.getRadar().removeAllMarkers();
	}
	
	/**
	 * Send a packet in order to play sound at client terminal
	 * @param sound
	 */
	public void playSound(String sound)
	{
		_player.sendPacket(new PlaySound(sound));
	}
	
	/**
	 * Add XP and SP as quest reward
	 * @param exp
	 * @param sp
	 */
	public void rewardExpAndSp(int exp, int sp)
	{
		_player.addExpAndSp((int) _player.calcStat(Stat.EXPSP_RATE, exp * Config.RATE_QUESTS_REWARD, null, null), (int) _player.calcStat(Stat.EXPSP_RATE, sp * Config.RATE_QUESTS_REWARD, null, null));
	}
	
	/**
	 * Add spawn for player instance Return object id of newly spawned npc
	 * @param npcId
	 * @return
	 */
	public NpcInstance addSpawn(int npcId)
	{
		return addSpawn(npcId, _player.getX(), _player.getY(), _player.getZ(), 0, false, 0);
	}
	
	public NpcInstance addSpawn(int npcId, int x, int y, int z)
	{
		return addSpawn(npcId, x, y, z, 0, false, 0);
	}
	
	/**
	 * Add spawn for player instance Will despawn after the spawn length expires Return object id of newly spawned npc
	 * @param npcId
	 * @param x
	 * @param y
	 * @param z
	 * @param despawnDelay
	 * @return
	 */
	public NpcInstance addSpawn(int npcId, int x, int y, int z, int despawnDelay)
	{
		return addSpawn(npcId, x, y, z, 0, false, despawnDelay);
	}
	
	/**
	 * Add spawn for player instance Inherits coords and heading from specified Creature instance. It could be either the player, or any killed/attacked mob Return object id of newly spawned npc
	 * @param npcId
	 * @param creature
	 * @param randomOffset
	 * @param despawnDelay
	 * @return
	 */
	public NpcInstance addSpawn(int npcId, Creature creature, boolean randomOffset, int despawnDelay)
	{
		return addSpawn(npcId, creature.getX(), creature.getY(), creature.getZ(), creature.getHeading(), randomOffset, despawnDelay);
	}
	
	/**
	 * Add spawn for player instance Return object id of newly spawned npc
	 * @param npcId
	 * @param x
	 * @param y
	 * @param z
	 * @param heading
	 * @param randomOffset
	 * @param despawnDelay
	 * @return
	 */
	public NpcInstance addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, int despawnDelay)
	{
		return getQuest().addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay);
	}
	
	/**
	 * Set condition to 1, state to STARTED and play the "ItemSound.quest_accept".<br>
	 * Works only if state is CREATED and the quest is not a custom quest.
	 */
	public void startQuest()
	{
		if (isCreated())
		{
			set(COND_VAR, "1");
			setState(State.STARTED);
			_player.sendPacket(new PlaySound(QuestState.SOUND_ACCEPT));
		}
	}
	
	public void showQuestionMark(int number)
	{
		_player.sendPacket(new TutorialShowQuestionMark(number));
	}
	
	public void playTutorialVoice(String voice)
	{
		_player.sendPacket(new PlaySound(2, voice, false, 0, _player.getLocation(), 0));
	}
}
