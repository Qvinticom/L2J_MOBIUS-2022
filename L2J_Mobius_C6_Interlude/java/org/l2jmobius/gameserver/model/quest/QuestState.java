
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
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.GameTimeController;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.instancemanager.QuestManager;
import org.l2jmobius.gameserver.model.DropData;
import org.l2jmobius.gameserver.model.PlayerInventory;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.instance.MonsterInstance;
import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.model.skills.Stats;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowQuestMark;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.ItemList;
import org.l2jmobius.gameserver.network.serverpackets.PlaySound;
import org.l2jmobius.gameserver.network.serverpackets.QuestList;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.TutorialCloseHtml;
import org.l2jmobius.gameserver.network.serverpackets.TutorialEnableClientEvent;
import org.l2jmobius.gameserver.network.serverpackets.TutorialShowHtml;
import org.l2jmobius.gameserver.network.serverpackets.TutorialShowQuestionMark;

/**
 * @author Luis Arias
 */
public class QuestState
{
	protected static final Logger LOGGER = Logger.getLogger(Quest.class.getName());
	
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
	
	/** Quest associated to the QuestState */
	private final String _questName;
	
	/** Player who engaged the quest */
	private final PlayerInstance _player;
	
	/** State of the quest */
	private byte _state;
	
	/** List of couples (variable for quest,value of the variable for quest) */
	private Map<String, String> _vars;
	
	/** Boolean flag letting QuestStateManager know to exit quest when cleaning up */
	private boolean _isExitQuestOnCleanUp = false;
	
	/**
	 * Constructor of the QuestState : save the quest in the list of quests of the player.<BR/>
	 * <BR/>
	 * <U><I>Actions :</U></I><BR/>
	 * <LI>Save informations in the object QuestState created (Quest, Player, Completion, State)</LI>
	 * <LI>Add the QuestState in the player's list of quests by using setQuestState()</LI>
	 * <LI>Add drops gotten by the quest</LI> <BR/>
	 * @param quest : quest associated with the QuestState
	 * @param player : PlayerInstance pointing out the player
	 * @param state : state of the quest
	 */
	QuestState(Quest quest, PlayerInstance player, byte state)
	{
		_questName = quest.getName();
		_player = player;
		
		// Save the state of the quest for the player in the player's list of quest owned
		_player.setQuestState(this);
		
		// set the state of the quest
		_state = state;
	}
	
	public String getQuestName()
	{
		return _questName;
	}
	
	/**
	 * Return the quest
	 * @return Quest
	 */
	public Quest getQuest()
	{
		return QuestManager.getInstance().getQuest(_questName);
	}
	
	/**
	 * Return the PlayerInstance
	 * @return PlayerInstance
	 */
	public PlayerInstance getPlayer()
	{
		return _player;
	}
	
	/**
	 * Return the state of the quest
	 * @return State
	 */
	public byte getState()
	{
		return _state;
	}
	
	/**
	 * Return true if quest completed, false otherwise
	 * @return boolean
	 */
	public boolean isCompleted()
	{
		return (_state == State.COMPLETED);
	}
	
	/**
	 * Return true if quest started, false otherwise
	 * @return boolean
	 */
	public boolean isStarted()
	{
		return (_state == State.STARTED);
	}
	
	/**
	 * Return state of the quest after its initialization.<BR>
	 * <BR>
	 * <U><I>Actions :</I></U>
	 * <LI>Remove drops from previous state</LI>
	 * <LI>Set new state of the quest</LI>
	 * <LI>Add drop for new state</LI>
	 * <LI>Update information in database</LI>
	 * <LI>Send packet QuestList to client</LI>
	 * @param state
	 * @return object
	 */
	public Object setState(byte state)
	{
		// set new state
		_state = state;
		
		Quest.updateQuestInDb(this);
		final QuestList ql = new QuestList();
		_player.sendPacket(ql);
		return state;
	}
	
	/**
	 * Add parameter used in quests.
	 * @param var : String pointing out the name of the variable for quest
	 * @param val : String pointing out the value of the variable for quest
	 * @return String (equal to parameter "val")
	 */
	String setInternal(String var, String val)
	{
		if (_vars == null)
		{
			_vars = new HashMap<>();
		}
		
		if (val == null)
		{
			val = "";
		}
		
		_vars.put(var, val);
		
		return val;
	}
	
	/**
	 * Return value of parameter "val" after adding the couple (var,val) in class variable "vars".<BR>
	 * <BR>
	 * <U><I>Actions :</I></U><BR>
	 * <LI>Initialize class variable "vars" if is null</LI>
	 * <LI>Initialize parameter "val" if is null</LI>
	 * <LI>Add/Update couple (var,val) in class variable Map "vars"</LI>
	 * <LI>If the key represented by "var" exists in Map "vars", the couple (var,val) is updated in the database. The key is known as existing if the preceding value of the key (given as result of function put()) is not null.<BR>
	 * If the key doesn't exist, the couple is added/created in the database</LI>
	 * @param var : String indicating the name of the variable for quest
	 * @param val : String indicating the value of the variable for quest
	 * @return String (equal to parameter "val")
	 */
	public String set(String var, String val)
	{
		if (_vars == null)
		{
			_vars = new HashMap<>();
		}
		
		if (val == null)
		{
			val = "";
		}
		
		// Map.put() returns previous value associated with specified key, or null if there was no mapping for key.
		String old = _vars.put(var, val);
		
		if (old != null)
		{
			Quest.updateQuestVarInDb(this, var, val);
		}
		else
		{
			Quest.createQuestVarInDb(this, var, val);
		}
		
		if (var == "cond")
		{
			try
			{
				int previousVal = 0;
				try
				{
					previousVal = Integer.parseInt(old);
				}
				catch (Exception ex)
				{
					previousVal = 0;
				}
				setCond(Integer.parseInt(val), previousVal);
			}
			catch (Exception e)
			{
				LOGGER.finer(_player.getName() + ", " + _questName + " cond [" + val + "] is not an integer.  Value stored, but no packet was sent: " + e);
			}
		}
		
		return val;
	}
	
	/**
	 * Internally handles the progression of the quest so that it is ready for sending appropriate packets to the client<BR>
	 * <BR>
	 * <U><I>Actions :</I></U><BR>
	 * <LI>Check if the new progress number resets the quest to a previous (smaller) step</LI>
	 * <LI>If not, check if quest progress steps have been skipped</LI>
	 * <LI>If skipped, prepare the variable completedStateFlags appropriately to be ready for sending to clients</LI>
	 * <LI>If no steps were skipped, flags do not need to be prepared...</LI>
	 * <LI>If the passed step resets the quest to a previous step, reset such that steps after the parameter are not considered, while skipped steps before the parameter, if any, maintain their info</LI>
	 * @param cond : int indicating the step number for the current quest progress (as will be shown to the client)
	 * @param old : int indicating the previously noted step For more info on the variable communicating the progress steps to the client, please see
	 */
	private void setCond(int cond, int old)
	{
		int completedStateFlags = 0; // initializing...
		
		// if there is no change since last setting, there is nothing to do here
		if (cond == old)
		{
			return;
		}
		
		// cond 0 and 1 do not need completedStateFlags. Also, if cond > 1, the 1st step must always exist (i.e. it can never be skipped). So if cond is 2, we can still safely assume no steps have been skipped.
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
		// if this moves forward, it changes nothing on previously skipped steps...so just mark this state and we are done
		else
		{
			completedStateFlags |= 1 << (cond - 1);
			set("__compltdStateFlags", String.valueOf(completedStateFlags));
		}
		
		// send a packet to the client to inform it of the quest progress (step change)
		QuestList ql = new QuestList();
		_player.sendPacket(ql);
		
		final int questId = getQuest().getQuestIntId();
		
		if ((questId > 0) && (questId < 999) && (cond > 0))
		{
			_player.sendPacket(new ExShowQuestMark(questId));
		}
	}
	
	/**
	 * Remove the variable of quest from the list of variables for the quest.<BR>
	 * <BR>
	 * <U><I>Concept : </I></U> Remove the variable of quest represented by "var" from the class variable Map "vars" and from the database.
	 * @param var : String designating the variable for the quest to be deleted
	 * @return String pointing out the previous value associated with the variable "var"
	 */
	public String unset(String var)
	{
		if (_vars == null)
		{
			return null;
		}
		
		final String old = _vars.remove(var);
		
		if (old != null)
		{
			Quest.deleteQuestVarInDb(this, var);
		}
		
		return old;
	}
	
	/**
	 * Return the value of the variable of quest represented by "var"
	 * @param var : name of the variable of quest
	 * @return Object
	 */
	public Object get(String var)
	{
		if (_vars == null)
		{
			return null;
		}
		
		return _vars.get(var);
	}
	
	/**
	 * Return the value of the variable of quest represented by "var"
	 * @param var : name of the variable of quest
	 * @return Object
	 */
	public String getString(String var)
	{
		if (_vars == null)
		{
			return "";
		}
		
		return _vars.get(var);
	}
	
	/**
	 * Return the value of the variable of quest represented by "var"
	 * @param var : String designating the variable for the quest
	 * @return int
	 */
	public int getInt(String var)
	{
		int varint = 0;
		
		String var_value = "";
		if ((_vars != null) && ((var_value = _vars.get(var)) != null))
		{
			try
			{
				varint = Integer.parseInt(var_value);
			}
			catch (Exception e)
			{
				LOGGER.info(_player.getName() + ": variable " + var + " isn't an integer: returned value will be " + varint + e);
				
				if (Config.AUTODELETE_INVALID_QUEST_DATA)
				{
					exitQuest(true);
				}
			}
		}
		
		return varint;
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
	 * Return the level of enchantment on the weapon of the player(Done specifically for weapon SA's)
	 * @param itemId : ID of the item to check enchantment
	 * @return int
	 */
	public int getEnchantLevel(int itemId)
	{
		final ItemInstance enchanteditem = _player.getInventory().getItemByItemId(itemId);
		
		if (enchanteditem == null)
		{
			return 0;
		}
		
		return enchanteditem.getEnchantLevel();
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
	
	public synchronized void giveItems(int itemId, int count, int enchantlevel)
	{
		if (count <= 0)
		{
			return;
		}
		
		final int questId = getQuest().getQuestIntId();
		
		// If item for reward is gold (ID=57), modify count with rate for quest reward
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
			SystemMessage smsg = new SystemMessage(SystemMessageId.EARNED_ADENA);
			smsg.addNumber(count);
			_player.sendPacket(smsg);
		}
		// Otherwise, send message of object reward to client
		else if (count > 1)
		{
			SystemMessage smsg = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
			smsg.addItemName(item.getItemId());
			smsg.addNumber(count);
			_player.sendPacket(smsg);
		}
		else
		{
			SystemMessage smsg = new SystemMessage(SystemMessageId.EARNED_ITEM);
			smsg.addItemName(item.getItemId());
			_player.sendPacket(smsg);
		}
		_player.sendPacket(new ItemList(_player, false));
		
		StatusUpdate su = new StatusUpdate(_player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, _player.getCurrentLoad());
		_player.sendPacket(su);
	}
	
	/**
	 * Reward player with items. The amount is affected by Config.RATE_QUEST_REWARD or Config.RATE_QUEST_REWARD_ADENA.
	 * @param itemId : Identifier of the item.
	 * @param itemCount : Quantity of item to reward before applying multiplier.
	 */
	public void rewardItems(int itemId, int itemCount)
	{
		if (itemId == 57)
		{
			giveItems(itemId, (int) (itemCount * Config.RATE_QUESTS_REWARD), 0); // TODO: RATE_QUEST_REWARD_ADENA
		}
		else
		{
			giveItems(itemId, (int) (itemCount * Config.RATE_QUESTS_REWARD), 0);
		}
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
		dropChance *= Config.RATE_DROP_QUEST / (_player.getParty() != null ? _player.getParty().getMemberCount() : 1);
		
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
		
		while (random < dropChance)
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
			dropChance -= DropData.MAX_CHANCE;
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
				dropChance *= Config.RATE_DROP_QUEST;
				amount = count * (dropChance / DropData.MAX_CHANCE);
				if (Rnd.get(DropData.MAX_CHANCE) < (dropChance % DropData.MAX_CHANCE))
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
					dropChance *= Config.RATE_DROP_QUEST;
					amount = count * (dropChance / DropData.MAX_CHANCE);
					if (Rnd.get(DropData.MAX_CHANCE) < (dropChance % DropData.MAX_CHANCE))
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
	
	// TODO: More radar functions need to be added when the radar class is complete.
	// BEGIN STUFF THAT WILL PROBABLY BE CHANGED
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
	
	// END STUFF THAT WILL PROBABLY BE CHANGED
	
	/**
	 * Remove items from player's inventory when talking to NPC in order to have rewards.<BR>
	 * <BR>
	 * <U><I>Actions :</I></U>
	 * <LI>Destroy quantity of items wanted</LI>
	 * <LI>Send new inventory list to player</LI>
	 * @param itemId : Identifier of the item
	 * @param count : Quantity of items to destroy
	 */
	public void takeItems(int itemId, int count)
	{
		// Get object item from player's inventory list
		ItemInstance item = _player.getInventory().getItemByItemId(itemId);
		
		if (item == null)
		{
			return;
		}
		
		if (_player.isProcessingTransaction())
		{
			_player.cancelActiveTrade();
		}
		
		// Tests on count value in order not to have negative value
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
		final InventoryUpdate u = new InventoryUpdate();
		u.addItem(item);
		_player.sendPacket(u);
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
		_player.addExpAndSp((int) _player.calcStat(Stats.EXPSP_RATE, exp * Config.RATE_QUESTS_REWARD, null, null), (int) _player.calcStat(Stats.EXPSP_RATE, sp * Config.RATE_QUESTS_REWARD, null, null));
	}
	
	/**
	 * Return random value
	 * @param max : max value for randomisation
	 * @return int
	 */
	public int getRandom(int max)
	{
		return Rnd.get(max);
	}
	
	/**
	 * @param loc
	 * @return number of ticks from GameTimeController.
	 */
	public int getItemEquipped(int loc)
	{
		return _player.getInventory().getPaperdollItemId(loc);
	}
	
	/**
	 * Return the number of ticks from the GameTimeController
	 * @return int
	 */
	public int getGameTicks()
	{
		return GameTimeController.getGameTicks();
	}
	
	/**
	 * Return true if quest is to exited on clean up by QuestStateManager
	 * @return boolean
	 */
	public boolean isExitQuestOnCleanUp()
	{
		return _isExitQuestOnCleanUp;
	}
	
	/**
	 * @param isExitQuestOnCleanUp
	 */
	public void setIsExitQuestOnCleanUp(boolean isExitQuestOnCleanUp)
	{
		_isExitQuestOnCleanUp = isExitQuestOnCleanUp;
	}
	
	/**
	 * Start a timer for quest.<BR>
	 * <BR>
	 * @param name The name of the timer. Will also be the value for event of onEvent
	 * @param time The millisecond value the timer will elapse
	 */
	public void startQuestTimer(String name, long time)
	{
		getQuest().startQuestTimer(name, time, null, getPlayer(), false);
	}
	
	public void startQuestTimer(String name, long time, NpcInstance npc)
	{
		getQuest().startQuestTimer(name, time, npc, getPlayer(), false);
	}
	
	public void startRepeatingQuestTimer(String name, long time)
	{
		getQuest().startQuestTimer(name, time, null, getPlayer(), true);
	}
	
	public void startRepeatingQuestTimer(String name, long time, NpcInstance npc)
	{
		getQuest().startQuestTimer(name, time, npc, getPlayer(), true);
	}
	
	/**
	 * Return the QuestTimer object with the specified name
	 * @param name
	 * @return QuestTimer<BR>
	 *         Return null if name does not exist
	 */
	public QuestTimer getQuestTimer(String name)
	{
		return getQuest().getQuestTimer(name, null, getPlayer());
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
	
	public NpcInstance addSpawn(int npcId, int despawnDelay)
	{
		return addSpawn(npcId, _player.getX(), _player.getY(), _player.getZ(), 0, false, despawnDelay);
	}
	
	public NpcInstance addSpawn(int npcId, int x, int y, int z)
	{
		return addSpawn(npcId, x, y, z, 0, false, 0);
	}
	
	/**
	 * Add spawn for player instance Will despawn after the spawn length expires Uses player's coords and heading. Adds a little randomization in the x y coords Return object id of newly spawned npc
	 * @param npcId
	 * @param creature
	 * @return
	 */
	public NpcInstance addSpawn(int npcId, Creature creature)
	{
		return addSpawn(npcId, creature, true, 0);
	}
	
	public NpcInstance addSpawn(int npcId, Creature creature, int despawnDelay)
	{
		return addSpawn(npcId, creature.getX(), creature.getY(), creature.getZ(), creature.getHeading(), true, despawnDelay);
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
	
	public String showHtmlFile(String fileName)
	{
		return getQuest().showHtmlFile(getPlayer(), fileName);
	}
	
	/**
	 * Destroy element used by quest when quest is exited
	 * @param repeatable
	 * @return QuestState
	 */
	public QuestState exitQuest(boolean repeatable)
	{
		if (isCompleted())
		{
			return this;
		}
		
		// Say quest is completed
		setState(State.COMPLETED);
		
		// Clean registered quest items
		int[] itemIdList = getQuest().getRegisteredItemIds();
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
				for (String var : _vars.keySet())
				{
					unset(var);
				}
			}
			
			Quest.updateQuestInDb(this);
		}
		
		return this;
	}
	
	public void showQuestionMark(int number)
	{
		_player.sendPacket(new TutorialShowQuestionMark(number));
	}
	
	public void playTutorialVoice(String voice)
	{
		_player.sendPacket(new PlaySound(2, voice, 0, 0, _player.getX(), _player.getY(), _player.getZ()));
	}
	
	public void showTutorialHTML(String html)
	{
		String text = HtmCache.getInstance().getHtm("data/scripts/ai/others/Tutorial/" + html);
		if (text == null)
		{
			LOGGER.warning("missing html page data/scripts/ai/others/Tutorial/" + html);
			text = "<html><body>File data/scripts/ai/others/Tutorial/" + html + " not found or file is empty.</body></html>";
		}
		
		_player.sendPacket(new TutorialShowHtml(text));
	}
	
	public void closeTutorialHtml()
	{
		_player.sendPacket(new TutorialCloseHtml());
	}
	
	public void onTutorialClientEvent(int number)
	{
		_player.sendPacket(new TutorialEnableClientEvent(number));
	}
	
	public void dropItem(MonsterInstance npc, PlayerInstance player, int itemId, int count)
	{
		npc.DropItem(player, itemId, count);
	}
	
	public NpcInstance getNpc()
	{
		if (_player.getTarget() instanceof NpcInstance)
		{
			return (NpcInstance) _player.getTarget();
		}
		return null;
	}
}
