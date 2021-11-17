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
package org.l2jmobius.gameserver.model.itemcontainer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.util.CommonUtil;
import org.l2jmobius.gameserver.cache.PaperdollCache;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.data.xml.AgathionData;
import org.l2jmobius.gameserver.data.xml.AppearanceItemData;
import org.l2jmobius.gameserver.data.xml.ArmorSetData;
import org.l2jmobius.gameserver.enums.ItemLocation;
import org.l2jmobius.gameserver.enums.ItemSkillType;
import org.l2jmobius.gameserver.enums.PrivateStoreType;
import org.l2jmobius.gameserver.enums.SkillFinishType;
import org.l2jmobius.gameserver.model.ArmorSet;
import org.l2jmobius.gameserver.model.PlayerCondOverride;
import org.l2jmobius.gameserver.model.VariationInstance;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventDispatcher;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerItemUnequip;
import org.l2jmobius.gameserver.model.holders.AgathionSkillHolder;
import org.l2jmobius.gameserver.model.holders.ArmorsetSkillHolder;
import org.l2jmobius.gameserver.model.holders.ItemSkillHolder;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.item.EtcItem;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.appearance.AppearanceStone;
import org.l2jmobius.gameserver.model.item.appearance.AppearanceType;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.item.type.EtcItemType;
import org.l2jmobius.gameserver.model.item.type.ItemType;
import org.l2jmobius.gameserver.model.item.type.WeaponType;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.skill.SkillConditionScope;
import org.l2jmobius.gameserver.network.serverpackets.ExUserInfoEquipSlot;
import org.l2jmobius.gameserver.network.serverpackets.SkillCoolTime;

/**
 * This class manages inventory
 * @version $Revision: 1.13.2.9.2.12 $ $Date: 2005/03/29 23:15:15 $ rewritten 23.2.2006 by Advi
 */
public abstract class Inventory extends ItemContainer
{
	protected static final Logger LOGGER = Logger.getLogger(Inventory.class.getName());
	
	public interface PaperdollListener
	{
		void notifyEquiped(int slot, Item inst, Inventory inventory);
		
		void notifyUnequiped(int slot, Item inst, Inventory inventory);
	}
	
	// Common Items
	public static final int ADENA_ID = 57;
	public static final int SILVER_COIN = 29983;
	public static final int GOLD_COIN = 29984;
	public static final int ANCIENT_ADENA_ID = 5575;
	public static final int BEAUTY_TICKET_ID = 36308;
	public static final int AIR_STONE_ID = 39461;
	public static final int TEMPEST_STONE_ID = 39592;
	public static final int ELCYUM_CRYSTAL_ID = 36514;
	
	public static final long MAX_ADENA = Config.MAX_ADENA;
	
	public static final int PAPERDOLL_UNDER = 0;
	public static final int PAPERDOLL_HEAD = 1;
	public static final int PAPERDOLL_HAIR = 2;
	public static final int PAPERDOLL_HAIR2 = 3;
	public static final int PAPERDOLL_NECK = 4;
	public static final int PAPERDOLL_RHAND = 5;
	public static final int PAPERDOLL_CHEST = 6;
	public static final int PAPERDOLL_LHAND = 7;
	public static final int PAPERDOLL_REAR = 8;
	public static final int PAPERDOLL_LEAR = 9;
	public static final int PAPERDOLL_GLOVES = 10;
	public static final int PAPERDOLL_LEGS = 11;
	public static final int PAPERDOLL_FEET = 12;
	public static final int PAPERDOLL_RFINGER = 13;
	public static final int PAPERDOLL_LFINGER = 14;
	public static final int PAPERDOLL_LBRACELET = 15;
	public static final int PAPERDOLL_RBRACELET = 16;
	public static final int PAPERDOLL_AGATHION1 = 17;
	public static final int PAPERDOLL_AGATHION2 = 18;
	public static final int PAPERDOLL_AGATHION3 = 19;
	public static final int PAPERDOLL_AGATHION4 = 20;
	public static final int PAPERDOLL_AGATHION5 = 21;
	public static final int PAPERDOLL_DECO1 = 22;
	public static final int PAPERDOLL_DECO2 = 23;
	public static final int PAPERDOLL_DECO3 = 24;
	public static final int PAPERDOLL_DECO4 = 25;
	public static final int PAPERDOLL_DECO5 = 26;
	public static final int PAPERDOLL_DECO6 = 27;
	public static final int PAPERDOLL_CLOAK = 28;
	public static final int PAPERDOLL_BELT = 29;
	public static final int PAPERDOLL_BROOCH = 30;
	public static final int PAPERDOLL_BROOCH_JEWEL1 = 31;
	public static final int PAPERDOLL_BROOCH_JEWEL2 = 32;
	public static final int PAPERDOLL_BROOCH_JEWEL3 = 33;
	public static final int PAPERDOLL_BROOCH_JEWEL4 = 34;
	public static final int PAPERDOLL_BROOCH_JEWEL5 = 35;
	public static final int PAPERDOLL_BROOCH_JEWEL6 = 36;
	public static final int PAPERDOLL_ARTIFACT_BOOK = 37;
	public static final int PAPERDOLL_ARTIFACT1 = 38; // Artifact Balance
	public static final int PAPERDOLL_ARTIFACT2 = 39; // Artifact Balance
	public static final int PAPERDOLL_ARTIFACT3 = 40; // Artifact Balance
	public static final int PAPERDOLL_ARTIFACT4 = 41; // Artifact Balance
	public static final int PAPERDOLL_ARTIFACT5 = 42; // Artifact Balance
	public static final int PAPERDOLL_ARTIFACT6 = 43; // Artifact Balance
	public static final int PAPERDOLL_ARTIFACT7 = 44; // Artifact Balance
	public static final int PAPERDOLL_ARTIFACT8 = 45; // Artifact Balance
	public static final int PAPERDOLL_ARTIFACT9 = 46; // Artifact Balance
	public static final int PAPERDOLL_ARTIFACT10 = 47; // Artifact Balance
	public static final int PAPERDOLL_ARTIFACT11 = 48; // Artifact Balance
	public static final int PAPERDOLL_ARTIFACT12 = 49; // Artifact Balance
	public static final int PAPERDOLL_ARTIFACT13 = 50; // Artifact Spirit
	public static final int PAPERDOLL_ARTIFACT14 = 51; // Artifact Spirit
	public static final int PAPERDOLL_ARTIFACT15 = 52; // Artifact Spirit
	public static final int PAPERDOLL_ARTIFACT16 = 53; // Artifact Protection
	public static final int PAPERDOLL_ARTIFACT17 = 54; // Artifact Protection
	public static final int PAPERDOLL_ARTIFACT18 = 55; // Artifact Protection
	public static final int PAPERDOLL_ARTIFACT19 = 56; // Artifact Support
	public static final int PAPERDOLL_ARTIFACT20 = 57; // Artifact Support
	public static final int PAPERDOLL_ARTIFACT21 = 58; // Artifact Support
	
	public static final int PAPERDOLL_TOTALSLOTS = 59;
	
	// Speed percentage mods
	public static final double MAX_ARMOR_WEIGHT = 12000;
	
	private final Item[] _paperdoll;
	private final List<PaperdollListener> _paperdollListeners;
	private final PaperdollCache _paperdollCache = new PaperdollCache();
	
	// protected to be accessed from child classes only
	protected int _totalWeight;
	
	// used to quickly check for using of items of special type
	private int _wearedMask;
	
	private int _blockedItemSlotsMask;
	
	// Recorder of alterations in inventory
	private static final class ChangeRecorder implements PaperdollListener
	{
		private final Inventory _inventory;
		private final List<Item> _changed = new ArrayList<>(1);
		
		/**
		 * Constructor of the ChangeRecorder
		 * @param inventory
		 */
		ChangeRecorder(Inventory inventory)
		{
			_inventory = inventory;
			_inventory.addPaperdollListener(this);
		}
		
		/**
		 * Add alteration in inventory when item equipped
		 * @param slot
		 * @param item
		 * @param inventory
		 */
		@Override
		public void notifyEquiped(int slot, Item item, Inventory inventory)
		{
			_changed.add(item);
		}
		
		/**
		 * Add alteration in inventory when item unequipped
		 * @param slot
		 * @param item
		 * @param inventory
		 */
		@Override
		public void notifyUnequiped(int slot, Item item, Inventory inventory)
		{
			_changed.add(item);
		}
		
		/**
		 * Returns alterations in inventory
		 * @return Item[] : array of altered items
		 */
		public List<Item> getChangedItems()
		{
			return _changed;
		}
	}
	
	private static final class BowCrossRodListener implements PaperdollListener
	{
		private static BowCrossRodListener instance = new BowCrossRodListener();
		
		public static BowCrossRodListener getInstance()
		{
			return instance;
		}
		
		@Override
		public void notifyUnequiped(int slot, Item item, Inventory inventory)
		{
			if (slot != PAPERDOLL_RHAND)
			{
				return;
			}
			
			if (item.getItemType() == WeaponType.BOW)
			{
				final Item arrow = inventory.getPaperdollItem(PAPERDOLL_LHAND);
				if (arrow != null)
				{
					inventory.setPaperdollItem(PAPERDOLL_LHAND, null);
				}
			}
			else if ((item.getItemType() == WeaponType.CROSSBOW) || (item.getItemType() == WeaponType.TWOHANDCROSSBOW))
			{
				final Item bolts = inventory.getPaperdollItem(PAPERDOLL_LHAND);
				if (bolts != null)
				{
					inventory.setPaperdollItem(PAPERDOLL_LHAND, null);
				}
			}
			else if (item.getItemType() == WeaponType.FISHINGROD)
			{
				final Item lure = inventory.getPaperdollItem(PAPERDOLL_LHAND);
				if (lure != null)
				{
					inventory.setPaperdollItem(PAPERDOLL_LHAND, null);
				}
			}
		}
		
		@Override
		public void notifyEquiped(int slot, Item item, Inventory inventory)
		{
			if (slot != PAPERDOLL_RHAND)
			{
				return;
			}
			
			if (item.getItemType() == WeaponType.BOW)
			{
				final Item arrow = inventory.findArrowForBow(item.getItem());
				if (arrow != null)
				{
					inventory.setPaperdollItem(PAPERDOLL_LHAND, arrow);
				}
			}
			else if ((item.getItemType() == WeaponType.CROSSBOW) || (item.getItemType() == WeaponType.TWOHANDCROSSBOW))
			{
				final Item bolts = inventory.findBoltForCrossBow(item.getItem());
				if (bolts != null)
				{
					inventory.setPaperdollItem(PAPERDOLL_LHAND, bolts);
				}
			}
		}
	}
	
	private static final class StatsListener implements PaperdollListener
	{
		private static StatsListener instance = new StatsListener();
		
		public static StatsListener getInstance()
		{
			return instance;
		}
		
		@Override
		public void notifyUnequiped(int slot, Item item, Inventory inventory)
		{
			inventory.getOwner().getStat().recalculateStats(true);
		}
		
		@Override
		public void notifyEquiped(int slot, Item item, Inventory inventory)
		{
			inventory.getOwner().getStat().recalculateStats(true);
		}
	}
	
	private static final class ItemSkillsListener implements PaperdollListener
	{
		private static ItemSkillsListener instance = new ItemSkillsListener();
		
		public static ItemSkillsListener getInstance()
		{
			return instance;
		}
		
		@Override
		public void notifyUnequiped(int slot, Item item, Inventory inventory)
		{
			if (!inventory.getOwner().isPlayer())
			{
				return;
			}
			
			final Player player = (Player) inventory.getOwner();
			final ItemTemplate it = item.getItem();
			final Map<Integer, Skill> addedSkills = new HashMap<>(1);
			final Map<Integer, Skill> removedSkills = new HashMap<>(1);
			boolean update = false;
			boolean updateTimestamp = false;
			
			// Remove augmentation bonuses on unequip
			if (item.isAugmented())
			{
				item.getAugmentation().removeBonus(player);
			}
			
			// Recalculate all stats
			player.getStat().recalculateStats(true);
			
			// Clear enchant bonus
			item.clearEnchantStats();
			
			// Clear SA Bonus
			item.clearSpecialAbilities();
			
			if (it.hasSkills())
			{
				final List<ItemSkillHolder> onEnchantSkills = it.getSkills(ItemSkillType.ON_ENCHANT);
				if (onEnchantSkills != null)
				{
					for (ItemSkillHolder holder : onEnchantSkills)
					{
						if (item.getEnchantLevel() < holder.getValue())
						{
							continue;
						}
						
						final Skill skill = holder.getSkill();
						if (skill != null)
						{
							removedSkills.putIfAbsent(skill.getId(), skill);
							update = true;
						}
					}
				}
				
				final List<ItemSkillHolder> normalSkills = it.getSkills(ItemSkillType.NORMAL);
				if (normalSkills != null)
				{
					for (ItemSkillHolder holder : normalSkills)
					{
						final Skill skill = holder.getSkill();
						if (skill != null)
						{
							removedSkills.putIfAbsent(skill.getId(), skill);
							update = true;
						}
					}
				}
				
				if (item.isArmor())
				{
					for (Item itm : inventory.getItems())
					{
						if (!itm.isEquipped() || itm.equals(item))
						{
							continue;
						}
						
						final List<ItemSkillHolder> otherNormalSkills = itm.getItem().getSkills(ItemSkillType.NORMAL);
						if (otherNormalSkills == null)
						{
							continue;
						}
						
						for (ItemSkillHolder holder : otherNormalSkills)
						{
							if (player.getSkillLevel(holder.getSkillId()) != 0)
							{
								continue;
							}
							
							final Skill skill = holder.getSkill();
							if (skill == null)
							{
								continue;
							}
							
							final Skill existingSkill = addedSkills.get(skill.getId());
							if (existingSkill != null)
							{
								if (existingSkill.getLevel() < skill.getLevel())
								{
									addedSkills.put(skill.getId(), skill);
								}
							}
							else
							{
								addedSkills.put(skill.getId(), skill);
							}
							
							if (skill.isActive() && !player.hasSkillReuse(skill.getReuseHashCode()))
							{
								final int equipDelay = item.getEquipReuseDelay();
								if (equipDelay > 0)
								{
									player.addTimeStamp(skill, equipDelay);
									player.disableSkill(skill, equipDelay);
								}
								updateTimestamp = true;
							}
							update = true;
						}
					}
				}
			}
			
			// Must check all equipped items for enchant conditions.
			for (Item equipped : inventory.getPaperdollItems())
			{
				if (!equipped.getItem().hasSkills())
				{
					continue;
				}
				
				final List<ItemSkillHolder> otherEnchantSkills = equipped.getItem().getSkills(ItemSkillType.ON_ENCHANT);
				if (otherEnchantSkills == null)
				{
					continue;
				}
				
				for (ItemSkillHolder holder : otherEnchantSkills)
				{
					if (equipped.getEnchantLevel() < holder.getValue())
					{
						continue;
					}
					
					final Skill skill = holder.getSkill();
					if (skill == null)
					{
						continue;
					}
					
					// Check passive skill conditions.
					if (skill.isPassive() && !skill.checkConditions(SkillConditionScope.PASSIVE, player, player))
					{
						removedSkills.putIfAbsent(skill.getId(), skill);
						update = true;
					}
				}
			}
			
			// Must check for toggle and isRemovedOnUnequipWeapon skill item conditions.
			for (Skill skill : player.getAllSkills())
			{
				if ((skill.isToggle() && player.isAffectedBySkill(skill.getId()) && !skill.checkConditions(SkillConditionScope.GENERAL, player, player)) //
					|| (it.isWeapon() && skill.isRemovedOnUnequipWeapon()))
				{
					player.stopSkillEffects(SkillFinishType.REMOVED, skill.getId());
					update = true;
				}
			}
			
			// Apply skill, if item has "skills on unequip" and it is not a secondary agathion.
			if ((slot < PAPERDOLL_AGATHION2) || (slot > PAPERDOLL_AGATHION5))
			{
				it.forEachSkill(ItemSkillType.ON_UNEQUIP, holder -> holder.getSkill().activateSkill(player, player));
			}
			
			if (update)
			{
				for (Skill skill : removedSkills.values())
				{
					player.removeSkill(skill, false, skill.isPassive());
				}
				
				for (Skill skill : addedSkills.values())
				{
					player.addSkill(skill, false);
				}
				
				player.sendSkillList();
			}
			
			if (updateTimestamp)
			{
				player.sendPacket(new SkillCoolTime(player));
			}
			
			if (item.isWeapon())
			{
				player.unchargeAllShots();
			}
		}
		
		@Override
		public void notifyEquiped(int slot, Item item, Inventory inventory)
		{
			if (!inventory.getOwner().isPlayer())
			{
				return;
			}
			
			final Player player = (Player) inventory.getOwner();
			final Map<Integer, Skill> addedSkills = new HashMap<>(1);
			boolean updateTimestamp = false;
			
			// Apply augmentation bonuses on equip
			if (item.isAugmented())
			{
				item.getAugmentation().applyBonus(player);
			}
			
			// Recalculate all stats
			player.getStat().recalculateStats(true);
			
			// Apply enchant stats
			item.applyEnchantStats();
			
			// Apply SA skill
			item.applySpecialAbilities();
			
			if (item.getItem().hasSkills())
			{
				final List<ItemSkillHolder> onEnchantSkills = item.getItem().getSkills(ItemSkillType.ON_ENCHANT);
				if (onEnchantSkills != null)
				{
					for (ItemSkillHolder holder : onEnchantSkills)
					{
						if (player.getSkillLevel(holder.getSkillId()) >= holder.getSkillLevel())
						{
							continue;
						}
						
						if (item.getEnchantLevel() < holder.getValue())
						{
							continue;
						}
						
						final Skill skill = holder.getSkill();
						if (skill == null)
						{
							continue;
						}
						
						// Check passive skill conditions.
						if (skill.isPassive() && !skill.checkConditions(SkillConditionScope.PASSIVE, player, player))
						{
							continue;
						}
						
						final Skill existingSkill = addedSkills.get(skill.getId());
						if (existingSkill != null)
						{
							if (existingSkill.getLevel() < skill.getLevel())
							{
								addedSkills.put(skill.getId(), skill);
							}
						}
						else
						{
							addedSkills.put(skill.getId(), skill);
						}
					}
				}
				
				final List<ItemSkillHolder> normalSkills = item.getItem().getSkills(ItemSkillType.NORMAL);
				if (normalSkills != null)
				{
					for (ItemSkillHolder holder : normalSkills)
					{
						if (player.getSkillLevel(holder.getSkillId()) >= holder.getSkillLevel())
						{
							continue;
						}
						
						final Skill skill = holder.getSkill();
						if (skill == null)
						{
							continue;
						}
						
						// Check passive skill conditions.
						if (skill.isPassive() && !skill.checkConditions(SkillConditionScope.PASSIVE, player, player))
						{
							continue;
						}
						
						final Skill existingSkill = addedSkills.get(skill.getId());
						if (existingSkill != null)
						{
							if (existingSkill.getLevel() < skill.getLevel())
							{
								addedSkills.put(skill.getId(), skill);
							}
						}
						else
						{
							addedSkills.put(skill.getId(), skill);
						}
						
						if (skill.isActive() && !player.hasSkillReuse(skill.getReuseHashCode()))
						{
							final int equipDelay = item.getEquipReuseDelay();
							if (equipDelay > 0)
							{
								player.addTimeStamp(skill, equipDelay);
								player.disableSkill(skill, equipDelay);
							}
							updateTimestamp = true;
						}
					}
				}
			}
			
			// Must check all equipped items for enchant conditions.
			for (Item equipped : inventory.getPaperdollItems())
			{
				if (!equipped.getItem().hasSkills())
				{
					continue;
				}
				
				final List<ItemSkillHolder> otherEnchantSkills = equipped.getItem().getSkills(ItemSkillType.ON_ENCHANT);
				if (otherEnchantSkills == null)
				{
					continue;
				}
				
				for (ItemSkillHolder holder : otherEnchantSkills)
				{
					if (player.getSkillLevel(holder.getSkillId()) >= holder.getSkillLevel())
					{
						continue;
					}
					
					if (equipped.getEnchantLevel() < holder.getValue())
					{
						continue;
					}
					
					final Skill skill = holder.getSkill();
					if (skill == null)
					{
						continue;
					}
					
					// Check passive skill conditions.
					if (skill.isPassive() && !skill.checkConditions(SkillConditionScope.PASSIVE, player, player))
					{
						continue;
					}
					
					final Skill existingSkill = addedSkills.get(skill.getId());
					if (existingSkill != null)
					{
						if (existingSkill.getLevel() < skill.getLevel())
						{
							addedSkills.put(skill.getId(), skill);
						}
					}
					else
					{
						addedSkills.put(skill.getId(), skill);
					}
				}
			}
			
			// Apply skill, if item has "skills on equip" and it is not a secondary agathion.
			if ((slot < PAPERDOLL_AGATHION2) || (slot > PAPERDOLL_AGATHION5))
			{
				item.getItem().forEachSkill(ItemSkillType.ON_EQUIP, holder -> holder.getSkill().activateSkill(player, player));
			}
			
			if (!addedSkills.isEmpty())
			{
				for (Skill skill : addedSkills.values())
				{
					player.addSkill(skill, false);
				}
				
				player.sendSkillList();
			}
			
			if (updateTimestamp)
			{
				player.sendPacket(new SkillCoolTime(player));
			}
		}
	}
	
	private static final class ArmorSetListener implements PaperdollListener
	{
		private static ArmorSetListener instance = new ArmorSetListener();
		
		public static ArmorSetListener getInstance()
		{
			return instance;
		}
		
		@Override
		public void notifyEquiped(int slot, Item item, Inventory inventory)
		{
			if (!inventory.getOwner().isPlayer())
			{
				return;
			}
			
			final Player player = (Player) inventory.getOwner();
			boolean update = false;
			
			// Verify and apply normal set
			if (verifyAndApply(player, item, Item::getId))
			{
				update = true;
			}
			
			// Verify and apply visual set
			final int itemVisualId = item.getVisualId();
			if (itemVisualId > 0)
			{
				final AppearanceStone stone = AppearanceItemData.getInstance().getStone(itemVisualId);
				if ((stone != null) && (stone.getType() == AppearanceType.FIXED) && verifyAndApply(player, item, Item::getVisualId))
				{
					update = true;
				}
			}
			
			if (update)
			{
				player.sendSkillList();
			}
			
			if ((item.getItem().getBodyPart() == ItemTemplate.SLOT_BROOCH_JEWEL) || (item.getItem().getBodyPart() == ItemTemplate.SLOT_BROOCH))
			{
				player.updateActiveBroochJewel();
			}
		}
		
		private static boolean applySkills(Player player, Item item, ArmorSet armorSet, Function<Item, Integer> idProvider)
		{
			final long piecesCount = armorSet.getPiecesCount(player, idProvider);
			if (piecesCount >= armorSet.getMinimumPieces())
			{
				// Applying all skills that matching the conditions
				boolean updateTimeStamp = false;
				boolean update = false;
				for (ArmorsetSkillHolder holder : armorSet.getSkills())
				{
					if (player.getSkillLevel(holder.getSkillId()) >= holder.getSkillLevel())
					{
						continue;
					}
					
					if (holder.validateConditions(player, armorSet, idProvider))
					{
						final Skill itemSkill = holder.getSkill();
						if (itemSkill == null)
						{
							LOGGER.warning("Inventory.ArmorSetListener.addSkills: Incorrect skill: " + holder);
							continue;
						}
						
						if (itemSkill.isPassive() && !itemSkill.checkConditions(SkillConditionScope.PASSIVE, player, player))
						{
							continue;
						}
						
						player.addSkill(itemSkill, false);
						if (itemSkill.isActive() && (item != null))
						{
							if (!player.hasSkillReuse(itemSkill.getReuseHashCode()))
							{
								final int equipDelay = item.getEquipReuseDelay();
								if (equipDelay > 0)
								{
									player.addTimeStamp(itemSkill, equipDelay);
									player.disableSkill(itemSkill, equipDelay);
								}
							}
							updateTimeStamp = true;
						}
						update = true;
					}
				}
				if (updateTimeStamp)
				{
					player.sendPacket(new SkillCoolTime(player));
				}
				return update;
			}
			return false;
		}
		
		private static boolean verifyAndApply(Player player, Item item, Function<Item, Integer> idProvider)
		{
			boolean update = false;
			final List<ArmorSet> armorSets = ArmorSetData.getInstance().getSets(idProvider.apply(item));
			for (ArmorSet armorSet : armorSets)
			{
				if (applySkills(player, item, armorSet, idProvider))
				{
					update = true;
				}
			}
			return update;
		}
		
		private static boolean verifyAndRemove(Player player, Item item, Function<Item, Integer> idProvider)
		{
			boolean update = false;
			final List<ArmorSet> armorSets = ArmorSetData.getInstance().getSets(idProvider.apply(item));
			for (ArmorSet armorSet : armorSets)
			{
				// Remove all skills that doesn't matches the conditions
				for (ArmorsetSkillHolder holder : armorSet.getSkills())
				{
					if (!holder.validateConditions(player, armorSet, idProvider))
					{
						final Skill itemSkill = holder.getSkill();
						if (itemSkill == null)
						{
							LOGGER.warning("Inventory.ArmorSetListener.removeSkills: Incorrect skill: " + holder);
							continue;
						}
						
						// Update if a skill has been removed.
						if (player.removeSkill(itemSkill, false, itemSkill.isPassive()) != null)
						{
							update = true;
						}
					}
				}
				
				// Attempt to apply lower level skills if possible
				if (applySkills(player, item, armorSet, idProvider))
				{
					update = true;
				}
			}
			
			return update;
		}
		
		@Override
		public void notifyUnequiped(int slot, Item item, Inventory inventory)
		{
			if (!inventory.getOwner().isPlayer())
			{
				return;
			}
			
			final Player player = (Player) inventory.getOwner();
			boolean remove = false;
			
			// Verify and remove normal set bonus
			if (verifyAndRemove(player, item, Item::getId))
			{
				remove = true;
			}
			
			// Verify and remove visual set bonus
			final int itemVisualId = item.getVisualId();
			if (itemVisualId > 0)
			{
				final AppearanceStone stone = AppearanceItemData.getInstance().getStone(itemVisualId);
				if ((stone != null) && (stone.getType() == AppearanceType.FIXED) && verifyAndRemove(player, item, Item::getVisualId))
				{
					remove = true;
				}
			}
			
			if (remove)
			{
				player.checkItemRestriction();
				player.sendSkillList();
			}
			
			if ((item.getItem().getBodyPart() == ItemTemplate.SLOT_BROOCH_JEWEL) || (item.getItem().getBodyPart() == ItemTemplate.SLOT_BROOCH))
			{
				player.updateActiveBroochJewel();
			}
		}
	}
	
	private static final class BraceletListener implements PaperdollListener
	{
		private static BraceletListener instance = new BraceletListener();
		
		public static BraceletListener getInstance()
		{
			return instance;
		}
		
		@Override
		public void notifyUnequiped(int slot, Item item, Inventory inventory)
		{
			final Player player = item.getActingPlayer();
			if ((player != null) && player.isChangingClass())
			{
				return;
			}
			
			if (item.getItem().getBodyPart() == ItemTemplate.SLOT_R_BRACELET)
			{
				inventory.unEquipItemInSlot(PAPERDOLL_DECO1);
				inventory.unEquipItemInSlot(PAPERDOLL_DECO2);
				inventory.unEquipItemInSlot(PAPERDOLL_DECO3);
				inventory.unEquipItemInSlot(PAPERDOLL_DECO4);
				inventory.unEquipItemInSlot(PAPERDOLL_DECO5);
				inventory.unEquipItemInSlot(PAPERDOLL_DECO6);
			}
		}
		
		// Note (April 3, 2009): Currently on equip, talismans do not display properly, do we need checks here to fix this?
		@Override
		public void notifyEquiped(int slot, Item item, Inventory inventory)
		{
		}
	}
	
	private static final class BroochListener implements PaperdollListener
	{
		private static BroochListener instance = new BroochListener();
		
		public static BroochListener getInstance()
		{
			return instance;
		}
		
		@Override
		public void notifyUnequiped(int slot, Item item, Inventory inventory)
		{
			final Player player = item.getActingPlayer();
			if ((player != null) && player.isChangingClass())
			{
				return;
			}
			
			if (item.getItem().getBodyPart() == ItemTemplate.SLOT_BROOCH)
			{
				inventory.unEquipItemInSlot(PAPERDOLL_BROOCH_JEWEL1);
				inventory.unEquipItemInSlot(PAPERDOLL_BROOCH_JEWEL2);
				inventory.unEquipItemInSlot(PAPERDOLL_BROOCH_JEWEL3);
				inventory.unEquipItemInSlot(PAPERDOLL_BROOCH_JEWEL4);
				inventory.unEquipItemInSlot(PAPERDOLL_BROOCH_JEWEL5);
				inventory.unEquipItemInSlot(PAPERDOLL_BROOCH_JEWEL6);
			}
		}
		
		// Note (April 3, 2009): Currently on equip, talismans do not display properly, do we need checks here to fix this?
		@Override
		public void notifyEquiped(int slot, Item item, Inventory inventory)
		{
		}
	}
	
	private static final class AgathionBraceletListener implements PaperdollListener
	{
		private static AgathionBraceletListener instance = new AgathionBraceletListener();
		
		public static AgathionBraceletListener getInstance()
		{
			return instance;
		}
		
		@Override
		public void notifyUnequiped(int slot, Item item, Inventory inventory)
		{
			final Player player = item.getActingPlayer();
			if ((player != null) && player.isChangingClass())
			{
				return;
			}
			
			if (item.getItem().getBodyPart() == ItemTemplate.SLOT_L_BRACELET)
			{
				inventory.unEquipItemInSlot(PAPERDOLL_AGATHION1);
				inventory.unEquipItemInSlot(PAPERDOLL_AGATHION2);
				inventory.unEquipItemInSlot(PAPERDOLL_AGATHION3);
				inventory.unEquipItemInSlot(PAPERDOLL_AGATHION4);
				inventory.unEquipItemInSlot(PAPERDOLL_AGATHION5);
			}
		}
		
		@Override
		public void notifyEquiped(int slot, Item item, Inventory inventory)
		{
		}
	}
	
	private static final class ArtifactBookListener implements PaperdollListener
	{
		private static ArtifactBookListener instance = new ArtifactBookListener();
		
		public static ArtifactBookListener getInstance()
		{
			return instance;
		}
		
		@Override
		public void notifyUnequiped(int slot, Item item, Inventory inventory)
		{
			final Player player = item.getActingPlayer();
			if ((player != null) && player.isChangingClass())
			{
				return;
			}
			
			if (item.getItem().getBodyPart() == ItemTemplate.SLOT_ARTIFACT_BOOK)
			{
				inventory.unEquipItemInSlot(PAPERDOLL_ARTIFACT1);
				inventory.unEquipItemInSlot(PAPERDOLL_ARTIFACT2);
				inventory.unEquipItemInSlot(PAPERDOLL_ARTIFACT3);
				inventory.unEquipItemInSlot(PAPERDOLL_ARTIFACT4);
				inventory.unEquipItemInSlot(PAPERDOLL_ARTIFACT5);
				inventory.unEquipItemInSlot(PAPERDOLL_ARTIFACT6);
				inventory.unEquipItemInSlot(PAPERDOLL_ARTIFACT7);
				inventory.unEquipItemInSlot(PAPERDOLL_ARTIFACT8);
				inventory.unEquipItemInSlot(PAPERDOLL_ARTIFACT9);
				inventory.unEquipItemInSlot(PAPERDOLL_ARTIFACT10);
				inventory.unEquipItemInSlot(PAPERDOLL_ARTIFACT11);
				inventory.unEquipItemInSlot(PAPERDOLL_ARTIFACT12);
				inventory.unEquipItemInSlot(PAPERDOLL_ARTIFACT13);
				inventory.unEquipItemInSlot(PAPERDOLL_ARTIFACT14);
				inventory.unEquipItemInSlot(PAPERDOLL_ARTIFACT15);
				inventory.unEquipItemInSlot(PAPERDOLL_ARTIFACT16);
				inventory.unEquipItemInSlot(PAPERDOLL_ARTIFACT17);
				inventory.unEquipItemInSlot(PAPERDOLL_ARTIFACT18);
				inventory.unEquipItemInSlot(PAPERDOLL_ARTIFACT19);
				inventory.unEquipItemInSlot(PAPERDOLL_ARTIFACT20);
				inventory.unEquipItemInSlot(PAPERDOLL_ARTIFACT21);
			}
		}
		
		@Override
		public void notifyEquiped(int slot, Item item, Inventory inventory)
		{
		}
	}
	
	/**
	 * Constructor of the inventory
	 */
	protected Inventory()
	{
		_paperdoll = new Item[PAPERDOLL_TOTALSLOTS];
		_paperdollListeners = new ArrayList<>();
		if (this instanceof PlayerInventory)
		{
			addPaperdollListener(ArmorSetListener.getInstance());
			addPaperdollListener(BowCrossRodListener.getInstance());
			addPaperdollListener(ItemSkillsListener.getInstance());
			addPaperdollListener(BraceletListener.getInstance());
			addPaperdollListener(BroochListener.getInstance());
			addPaperdollListener(AgathionBraceletListener.getInstance());
			addPaperdollListener(ArtifactBookListener.getInstance());
		}
		
		// common
		addPaperdollListener(StatsListener.getInstance());
	}
	
	protected abstract ItemLocation getEquipLocation();
	
	/**
	 * Returns the instance of new ChangeRecorder
	 * @return ChangeRecorder
	 */
	private ChangeRecorder newRecorder()
	{
		return new ChangeRecorder(this);
	}
	
	/**
	 * Drop item from inventory and updates database
	 * @param process : String Identifier of process triggering this action
	 * @param item : Item to be dropped
	 * @param actor : Player Player requesting the item drop
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return Item corresponding to the destroyed item or the updated item in inventory
	 */
	public Item dropItem(String process, Item item, Player actor, Object reference)
	{
		if (item == null)
		{
			return null;
		}
		
		synchronized (item)
		{
			if (!_items.contains(item))
			{
				return null;
			}
			
			removeItem(item);
			item.setOwnerId(process, 0, actor, reference);
			item.setItemLocation(ItemLocation.VOID);
			item.setLastChange(Item.REMOVED);
			
			item.updateDatabase();
			refreshWeight();
		}
		return item;
	}
	
	/**
	 * Drop item from inventory by using its <b>objectID</b> and updates database
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : int Item Instance identifier of the item to be dropped
	 * @param count : int Quantity of items to be dropped
	 * @param actor : Player Player requesting the item drop
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return Item corresponding to the destroyed item or the updated item in inventory
	 */
	public Item dropItem(String process, int objectId, long count, Player actor, Object reference)
	{
		Item item = getItemByObjectId(objectId);
		if (item == null)
		{
			return null;
		}
		
		synchronized (item)
		{
			if (!_items.contains(item))
			{
				return null;
			}
			
			// Adjust item quantity and create new instance to drop
			// Directly drop entire item
			if (item.getCount() > count)
			{
				item.changeCount(process, -count, actor, reference);
				item.setLastChange(Item.MODIFIED);
				item.updateDatabase();
				
				final Item newItem = ItemTable.getInstance().createItem(process, item.getId(), count, actor, reference);
				newItem.updateDatabase();
				refreshWeight();
				return newItem;
			}
		}
		
		return dropItem(process, item, actor, reference);
	}
	
	/**
	 * Adds item to inventory for further adjustments and Equip it if necessary (itemlocation defined)
	 * @param item : Item to be added from inventory
	 */
	@Override
	protected void addItem(Item item)
	{
		super.addItem(item);
		if (item.isEquipped())
		{
			equipItem(item);
		}
	}
	
	/**
	 * Removes item from inventory for further adjustments.
	 * @param item : Item to be removed from inventory
	 */
	@Override
	protected boolean removeItem(Item item)
	{
		// Unequip item if equiped
		for (int i = 0; i < _paperdoll.length; i++)
		{
			if (_paperdoll[i] == item)
			{
				unEquipItemInSlot(i);
			}
		}
		return super.removeItem(item);
	}
	
	/**
	 * @param slot the slot.
	 * @return the item in the paperdoll slot
	 */
	public Item getPaperdollItem(int slot)
	{
		return _paperdoll[slot];
	}
	
	/**
	 * @param slot the slot.
	 * @return {@code true} if specified paperdoll slot is empty, {@code false} otherwise
	 */
	public boolean isPaperdollSlotEmpty(int slot)
	{
		return _paperdoll[slot] == null;
	}
	
	public boolean isPaperdollSlotNotEmpty(int slot)
	{
		return _paperdoll[slot] != null;
	}
	
	public boolean isItemEquipped(int itemId)
	{
		for (Item item : _paperdoll)
		{
			if ((item != null) && (item.getId() == itemId))
			{
				return true;
			}
		}
		return false;
	}
	
	public static int getPaperdollIndex(long slot)
	{
		if (slot == ItemTemplate.SLOT_UNDERWEAR)
		{
			return PAPERDOLL_UNDER;
		}
		else if (slot == ItemTemplate.SLOT_R_EAR)
		{
			return PAPERDOLL_REAR;
		}
		else if ((slot == ItemTemplate.SLOT_LR_EAR) || (slot == ItemTemplate.SLOT_L_EAR))
		{
			return PAPERDOLL_LEAR;
		}
		else if (slot == ItemTemplate.SLOT_NECK)
		{
			return PAPERDOLL_NECK;
		}
		else if ((slot == ItemTemplate.SLOT_R_FINGER) || (slot == ItemTemplate.SLOT_LR_FINGER))
		{
			return PAPERDOLL_RFINGER;
		}
		else if (slot == ItemTemplate.SLOT_L_FINGER)
		{
			return PAPERDOLL_LFINGER;
		}
		else if (slot == ItemTemplate.SLOT_HEAD)
		{
			return PAPERDOLL_HEAD;
		}
		else if ((slot == ItemTemplate.SLOT_R_HAND) || (slot == ItemTemplate.SLOT_LR_HAND))
		{
			return PAPERDOLL_RHAND;
		}
		else if (slot == ItemTemplate.SLOT_L_HAND)
		{
			return PAPERDOLL_LHAND;
		}
		else if (slot == ItemTemplate.SLOT_GLOVES)
		{
			return PAPERDOLL_GLOVES;
		}
		else if ((slot == ItemTemplate.SLOT_CHEST) || (slot == ItemTemplate.SLOT_FULL_ARMOR) || (slot == ItemTemplate.SLOT_ALLDRESS))
		{
			return PAPERDOLL_CHEST;
		}
		else if (slot == ItemTemplate.SLOT_LEGS)
		{
			return PAPERDOLL_LEGS;
		}
		else if (slot == ItemTemplate.SLOT_FEET)
		{
			return PAPERDOLL_FEET;
		}
		else if (slot == ItemTemplate.SLOT_BACK)
		{
			return PAPERDOLL_CLOAK;
		}
		else if ((slot == ItemTemplate.SLOT_HAIR) || (slot == ItemTemplate.SLOT_HAIRALL))
		{
			return PAPERDOLL_HAIR;
		}
		else if (slot == ItemTemplate.SLOT_HAIR2)
		{
			return PAPERDOLL_HAIR2;
		}
		else if (slot == ItemTemplate.SLOT_R_BRACELET)
		{
			return PAPERDOLL_RBRACELET;
		}
		else if (slot == ItemTemplate.SLOT_L_BRACELET)
		{
			return PAPERDOLL_LBRACELET;
		}
		else if (slot == ItemTemplate.SLOT_DECO)
		{
			return PAPERDOLL_DECO1; // return first we deal with it later
		}
		else if (slot == ItemTemplate.SLOT_BELT)
		{
			return PAPERDOLL_BELT;
		}
		else if (slot == ItemTemplate.SLOT_BROOCH)
		{
			return PAPERDOLL_BROOCH;
		}
		else if (slot == ItemTemplate.SLOT_BROOCH_JEWEL)
		{
			return PAPERDOLL_BROOCH_JEWEL1;
		}
		else if (slot == ItemTemplate.SLOT_AGATHION)
		{
			return PAPERDOLL_AGATHION1;
		}
		else if (slot == ItemTemplate.SLOT_ARTIFACT_BOOK)
		{
			return PAPERDOLL_ARTIFACT_BOOK;
		}
		else if (slot == ItemTemplate.SLOT_ARTIFACT)
		{
			return PAPERDOLL_ARTIFACT1;
		}
		return -1;
	}
	
	/**
	 * Returns the item in the paperdoll Item slot
	 * @param slot identifier
	 * @return Item
	 */
	public Item getPaperdollItemByItemId(int slot)
	{
		final int index = getPaperdollIndex(slot);
		if (index == -1)
		{
			return null;
		}
		return _paperdoll[index];
	}
	
	/**
	 * Returns the ID of the item in the paperdoll slot
	 * @param slot : int designating the slot
	 * @return int designating the ID of the item
	 */
	public int getPaperdollItemId(int slot)
	{
		final Item item = _paperdoll[slot];
		if (item != null)
		{
			return item.getId();
		}
		return 0;
	}
	
	/**
	 * Returns the ID of the item in the paperdoll slot
	 * @param slot : int designating the slot
	 * @return int designating the ID of the item
	 */
	public int getPaperdollItemDisplayId(int slot)
	{
		final Item item = _paperdoll[slot];
		return (item != null) ? item.getDisplayId() : 0;
	}
	
	/**
	 * Returns the visual id of the item in the paperdoll slot
	 * @param slot : int designating the slot
	 * @return int designating the ID of the item
	 */
	public int getPaperdollItemVisualId(int slot)
	{
		final Item item = _paperdoll[slot];
		return (item != null) ? item.getVisualId() : 0;
	}
	
	public VariationInstance getPaperdollAugmentation(int slot)
	{
		final Item item = _paperdoll[slot];
		return (item != null) ? item.getAugmentation() : null;
	}
	
	/**
	 * Returns the objectID associated to the item in the paperdoll slot
	 * @param slot : int pointing out the slot
	 * @return int designating the objectID
	 */
	public int getPaperdollObjectId(int slot)
	{
		final Item item = _paperdoll[slot];
		return (item != null) ? item.getObjectId() : 0;
	}
	
	/**
	 * Adds new inventory's paperdoll listener.
	 * @param listener the new listener
	 */
	public synchronized void addPaperdollListener(PaperdollListener listener)
	{
		if (!_paperdollListeners.contains(listener))
		{
			_paperdollListeners.add(listener);
		}
	}
	
	/**
	 * Removes a paperdoll listener.
	 * @param listener the listener to be deleted
	 */
	public synchronized void removePaperdollListener(PaperdollListener listener)
	{
		_paperdollListeners.remove(listener);
	}
	
	/**
	 * Equips an item in the given slot of the paperdoll.<br>
	 * <u><i>Remark :</i></u> The item <b>must be</b> in the inventory already.
	 * @param slot : int pointing out the slot of the paperdoll
	 * @param item : Item pointing out the item to add in slot
	 * @return Item designating the item placed in the slot before
	 */
	public synchronized Item setPaperdollItem(int slot, Item item)
	{
		final Item old = _paperdoll[slot];
		if (old != item)
		{
			if (old != null)
			{
				_paperdoll[slot] = null;
				_paperdollCache.getPaperdollItems().remove(old);
				
				// Put old item from paperdoll slot to base location
				old.setItemLocation(getBaseLocation());
				old.setLastChange(Item.MODIFIED);
				// Get the mask for paperdoll
				int mask = 0;
				for (int i = 0; i < PAPERDOLL_TOTALSLOTS; i++)
				{
					final Item pi = _paperdoll[i];
					if (pi != null)
					{
						mask |= pi.getItem().getItemMask();
					}
				}
				_wearedMask = mask;
				// Notify all paperdoll listener in order to unequip old item in slot
				for (PaperdollListener listener : _paperdollListeners)
				{
					if (listener == null)
					{
						continue;
					}
					
					listener.notifyUnequiped(slot, old, this);
				}
				old.updateDatabase();
				
				// Remove agathion skills.
				if ((slot >= PAPERDOLL_AGATHION1) && (slot <= PAPERDOLL_AGATHION5) && getOwner().isPlayer())
				{
					final AgathionSkillHolder agathionSkills = AgathionData.getInstance().getSkills(old.getId());
					if (agathionSkills != null)
					{
						boolean update = false;
						for (SkillHolder holder : agathionSkills.getMainSkills(old.getEnchantLevel()))
						{
							getOwner().getActingPlayer().removeSkill(holder.getSkill(), false, holder.getSkill().isPassive());
							update = true;
						}
						for (SkillHolder holder : agathionSkills.getSubSkills(old.getEnchantLevel()))
						{
							getOwner().getActingPlayer().removeSkill(holder.getSkill(), false, holder.getSkill().isPassive());
							update = true;
						}
						if (update)
						{
							getOwner().getActingPlayer().sendSkillList();
						}
					}
				}
			}
			
			// Add new item in slot of paperdoll
			if (item != null)
			{
				_paperdoll[slot] = item;
				_paperdollCache.getPaperdollItems().add(item);
				
				item.setItemLocation(getEquipLocation(), slot);
				item.setLastChange(Item.MODIFIED);
				_wearedMask |= item.getItem().getItemMask();
				for (PaperdollListener listener : _paperdollListeners)
				{
					if (listener == null)
					{
						continue;
					}
					
					listener.notifyEquiped(slot, item, this);
				}
				item.updateDatabase();
				
				// Add agathion skills.
				if ((slot >= PAPERDOLL_AGATHION1) && (slot <= PAPERDOLL_AGATHION5) && getOwner().isPlayer())
				{
					final AgathionSkillHolder agathionSkills = AgathionData.getInstance().getSkills(item.getId());
					if (agathionSkills != null)
					{
						boolean update = false;
						if (slot == PAPERDOLL_AGATHION1)
						{
							for (SkillHolder holder : agathionSkills.getMainSkills(item.getEnchantLevel()))
							{
								if (holder.getSkill().isPassive() && !holder.getSkill().checkConditions(SkillConditionScope.PASSIVE, getOwner().getActingPlayer(), getOwner().getActingPlayer()))
								{
									continue;
								}
								getOwner().getActingPlayer().addSkill(holder.getSkill(), false);
								update = true;
							}
						}
						for (SkillHolder holder : agathionSkills.getSubSkills(item.getEnchantLevel()))
						{
							if (holder.getSkill().isPassive() && !holder.getSkill().checkConditions(SkillConditionScope.PASSIVE, getOwner().getActingPlayer(), getOwner().getActingPlayer()))
							{
								continue;
							}
							getOwner().getActingPlayer().addSkill(holder.getSkill(), false);
							update = true;
						}
						if (update)
						{
							getOwner().getActingPlayer().sendSkillList();
						}
					}
				}
			}
			
			_paperdollCache.clearCachedStats();
			getOwner().getStat().recalculateStats(!getOwner().isPlayer());
			
			if (getOwner().isPlayer())
			{
				getOwner().sendPacket(new ExUserInfoEquipSlot(getOwner().getActingPlayer()));
			}
		}
		
		// Notify to scripts
		if (old != null)
		{
			final Creature owner = getOwner();
			if ((owner != null) && owner.isPlayer())
			{
				EventDispatcher.getInstance().notifyEventAsync(new OnPlayerItemUnequip(owner.getActingPlayer(), old), old.getItem());
			}
		}
		
		return old;
	}
	
	/**
	 * @return the mask of wore item
	 */
	public int getWearedMask()
	{
		return _wearedMask;
	}
	
	public long getSlotFromItem(Item item)
	{
		long slot = -1;
		final int location = item.getLocationSlot();
		switch (location)
		{
			case PAPERDOLL_UNDER:
			{
				slot = ItemTemplate.SLOT_UNDERWEAR;
				break;
			}
			case PAPERDOLL_LEAR:
			{
				slot = ItemTemplate.SLOT_L_EAR;
				break;
			}
			case PAPERDOLL_REAR:
			{
				slot = ItemTemplate.SLOT_R_EAR;
				break;
			}
			case PAPERDOLL_NECK:
			{
				slot = ItemTemplate.SLOT_NECK;
				break;
			}
			case PAPERDOLL_RFINGER:
			{
				slot = ItemTemplate.SLOT_R_FINGER;
				break;
			}
			case PAPERDOLL_LFINGER:
			{
				slot = ItemTemplate.SLOT_L_FINGER;
				break;
			}
			case PAPERDOLL_HAIR:
			{
				slot = ItemTemplate.SLOT_HAIR;
				break;
			}
			case PAPERDOLL_HAIR2:
			{
				slot = ItemTemplate.SLOT_HAIR2;
				break;
			}
			case PAPERDOLL_HEAD:
			{
				slot = ItemTemplate.SLOT_HEAD;
				break;
			}
			case PAPERDOLL_RHAND:
			{
				slot = ItemTemplate.SLOT_R_HAND;
				break;
			}
			case PAPERDOLL_LHAND:
			{
				slot = ItemTemplate.SLOT_L_HAND;
				break;
			}
			case PAPERDOLL_GLOVES:
			{
				slot = ItemTemplate.SLOT_GLOVES;
				break;
			}
			case PAPERDOLL_CHEST:
			{
				slot = item.getItem().getBodyPart();
				break;
			}
			case PAPERDOLL_LEGS:
			{
				slot = ItemTemplate.SLOT_LEGS;
				break;
			}
			case PAPERDOLL_CLOAK:
			{
				slot = ItemTemplate.SLOT_BACK;
				break;
			}
			case PAPERDOLL_FEET:
			{
				slot = ItemTemplate.SLOT_FEET;
				break;
			}
			case PAPERDOLL_LBRACELET:
			{
				slot = ItemTemplate.SLOT_L_BRACELET;
				break;
			}
			case PAPERDOLL_RBRACELET:
			{
				slot = ItemTemplate.SLOT_R_BRACELET;
				break;
			}
			case PAPERDOLL_DECO1:
			case PAPERDOLL_DECO2:
			case PAPERDOLL_DECO3:
			case PAPERDOLL_DECO4:
			case PAPERDOLL_DECO5:
			case PAPERDOLL_DECO6:
			{
				slot = ItemTemplate.SLOT_DECO;
				break;
			}
			case PAPERDOLL_BELT:
			{
				slot = ItemTemplate.SLOT_BELT;
				break;
			}
			case PAPERDOLL_BROOCH:
			{
				slot = ItemTemplate.SLOT_BROOCH;
				break;
			}
			case PAPERDOLL_BROOCH_JEWEL1:
			case PAPERDOLL_BROOCH_JEWEL2:
			case PAPERDOLL_BROOCH_JEWEL3:
			case PAPERDOLL_BROOCH_JEWEL4:
			case PAPERDOLL_BROOCH_JEWEL5:
			case PAPERDOLL_BROOCH_JEWEL6:
			{
				slot = ItemTemplate.SLOT_BROOCH_JEWEL;
				break;
			}
			case PAPERDOLL_AGATHION1:
			case PAPERDOLL_AGATHION2:
			case PAPERDOLL_AGATHION3:
			case PAPERDOLL_AGATHION4:
			case PAPERDOLL_AGATHION5:
			{
				slot = ItemTemplate.SLOT_AGATHION;
				break;
			}
			case PAPERDOLL_ARTIFACT_BOOK:
			{
				slot = ItemTemplate.SLOT_ARTIFACT_BOOK;
				break;
			}
			case PAPERDOLL_ARTIFACT1:
			case PAPERDOLL_ARTIFACT2:
			case PAPERDOLL_ARTIFACT3:
			case PAPERDOLL_ARTIFACT4:
			case PAPERDOLL_ARTIFACT5:
			case PAPERDOLL_ARTIFACT6:
			case PAPERDOLL_ARTIFACT7:
			case PAPERDOLL_ARTIFACT8:
			case PAPERDOLL_ARTIFACT9:
			case PAPERDOLL_ARTIFACT10:
			case PAPERDOLL_ARTIFACT11:
			case PAPERDOLL_ARTIFACT12:
			case PAPERDOLL_ARTIFACT13:
			case PAPERDOLL_ARTIFACT14:
			case PAPERDOLL_ARTIFACT15:
			case PAPERDOLL_ARTIFACT16:
			case PAPERDOLL_ARTIFACT17:
			case PAPERDOLL_ARTIFACT18:
			case PAPERDOLL_ARTIFACT19:
			case PAPERDOLL_ARTIFACT20:
			case PAPERDOLL_ARTIFACT21:
			{
				slot = ItemTemplate.SLOT_ARTIFACT;
			}
		}
		return slot;
	}
	
	/**
	 * Unequips item in body slot and returns alterations.<br>
	 * <b>If you dont need return value use {@link Inventory#unEquipItemInBodySlot(long)} instead</b>
	 * @param slot : int designating the slot of the paperdoll
	 * @return List<Item> : List of changes
	 */
	public List<Item> unEquipItemInBodySlotAndRecord(long slot)
	{
		final ChangeRecorder recorder = newRecorder();
		try
		{
			unEquipItemInBodySlot(slot);
		}
		finally
		{
			removePaperdollListener(recorder);
		}
		return recorder.getChangedItems();
	}
	
	/**
	 * Sets item in slot of the paperdoll to null value
	 * @param pdollSlot : int designating the slot
	 * @return Item designating the item in slot before change
	 */
	public Item unEquipItemInSlot(int pdollSlot)
	{
		return setPaperdollItem(pdollSlot, null);
	}
	
	/**
	 * Unequips item in slot and returns alterations<br>
	 * <b>If you dont need return value use {@link Inventory#unEquipItemInSlot(int)} instead</b>
	 * @param slot : int designating the slot
	 * @return List<Item> : List of items altered
	 */
	public List<Item> unEquipItemInSlotAndRecord(int slot)
	{
		final ChangeRecorder recorder = newRecorder();
		try
		{
			unEquipItemInSlot(slot);
		}
		finally
		{
			removePaperdollListener(recorder);
		}
		return recorder.getChangedItems();
	}
	
	/**
	 * Unequips item in slot (i.e. equips with default value)
	 * @param slot : int designating the slot
	 * @return {@link Item} designating the item placed in the slot
	 */
	public Item unEquipItemInBodySlot(long slot)
	{
		int pdollSlot = -1;
		if (slot == ItemTemplate.SLOT_L_EAR)
		{
			pdollSlot = PAPERDOLL_LEAR;
		}
		else if (slot == ItemTemplate.SLOT_R_EAR)
		{
			pdollSlot = PAPERDOLL_REAR;
		}
		else if (slot == ItemTemplate.SLOT_NECK)
		{
			pdollSlot = PAPERDOLL_NECK;
		}
		else if (slot == ItemTemplate.SLOT_R_FINGER)
		{
			pdollSlot = PAPERDOLL_RFINGER;
		}
		else if (slot == ItemTemplate.SLOT_L_FINGER)
		{
			pdollSlot = PAPERDOLL_LFINGER;
		}
		else if (slot == ItemTemplate.SLOT_HAIR)
		{
			pdollSlot = PAPERDOLL_HAIR;
		}
		else if (slot == ItemTemplate.SLOT_HAIR2)
		{
			pdollSlot = PAPERDOLL_HAIR2;
		}
		else if (slot == ItemTemplate.SLOT_HAIRALL)
		{
			setPaperdollItem(PAPERDOLL_HAIR, null);
			pdollSlot = PAPERDOLL_HAIR;
		}
		else if (slot == ItemTemplate.SLOT_HEAD)
		{
			pdollSlot = PAPERDOLL_HEAD;
		}
		else if ((slot == ItemTemplate.SLOT_R_HAND) || (slot == ItemTemplate.SLOT_LR_HAND))
		{
			pdollSlot = PAPERDOLL_RHAND;
		}
		else if (slot == ItemTemplate.SLOT_L_HAND)
		{
			pdollSlot = PAPERDOLL_LHAND;
		}
		else if (slot == ItemTemplate.SLOT_GLOVES)
		{
			pdollSlot = PAPERDOLL_GLOVES;
		}
		else if ((slot == ItemTemplate.SLOT_CHEST) || (slot == ItemTemplate.SLOT_ALLDRESS) || (slot == ItemTemplate.SLOT_FULL_ARMOR))
		{
			pdollSlot = PAPERDOLL_CHEST;
		}
		else if (slot == ItemTemplate.SLOT_LEGS)
		{
			pdollSlot = PAPERDOLL_LEGS;
		}
		else if (slot == ItemTemplate.SLOT_BACK)
		{
			pdollSlot = PAPERDOLL_CLOAK;
		}
		else if (slot == ItemTemplate.SLOT_FEET)
		{
			pdollSlot = PAPERDOLL_FEET;
		}
		else if (slot == ItemTemplate.SLOT_UNDERWEAR)
		{
			pdollSlot = PAPERDOLL_UNDER;
		}
		else if (slot == ItemTemplate.SLOT_L_BRACELET)
		{
			pdollSlot = PAPERDOLL_LBRACELET;
		}
		else if (slot == ItemTemplate.SLOT_R_BRACELET)
		{
			pdollSlot = PAPERDOLL_RBRACELET;
		}
		else if (slot == ItemTemplate.SLOT_DECO)
		{
			pdollSlot = PAPERDOLL_DECO1;
		}
		else if (slot == ItemTemplate.SLOT_BELT)
		{
			pdollSlot = PAPERDOLL_BELT;
		}
		else if (slot == ItemTemplate.SLOT_BROOCH)
		{
			pdollSlot = PAPERDOLL_BROOCH;
		}
		else if (slot == ItemTemplate.SLOT_BROOCH_JEWEL)
		{
			pdollSlot = PAPERDOLL_BROOCH_JEWEL1;
		}
		else if (slot == ItemTemplate.SLOT_AGATHION)
		{
			pdollSlot = PAPERDOLL_AGATHION1;
		}
		else if (slot == ItemTemplate.SLOT_ARTIFACT_BOOK)
		{
			pdollSlot = PAPERDOLL_ARTIFACT_BOOK;
		}
		else if (slot == ItemTemplate.SLOT_ARTIFACT)
		{
			pdollSlot = PAPERDOLL_ARTIFACT1;
		}
		else
		{
			LOGGER.info("Unhandled slot type: " + slot);
			LOGGER.info(CommonUtil.getTraceString(Thread.currentThread().getStackTrace()));
		}
		if (pdollSlot >= 0)
		{
			return setPaperdollItem(pdollSlot, null);
		}
		return null;
	}
	
	/**
	 * Equips item and returns list of alterations<br>
	 * <b>If you don't need return value use {@link Inventory#equipItem(Item)} instead</b>
	 * @param item : Item corresponding to the item
	 * @return List<Item> : List of alterations
	 */
	public List<Item> equipItemAndRecord(Item item)
	{
		final ChangeRecorder recorder = newRecorder();
		try
		{
			equipItem(item);
		}
		finally
		{
			removePaperdollListener(recorder);
		}
		return recorder.getChangedItems();
	}
	
	/**
	 * Equips item in slot of paperdoll.
	 * @param item : Item designating the item and slot used.
	 */
	public void equipItem(Item item)
	{
		if (getOwner().isPlayer())
		{
			if (((Player) getOwner()).getPrivateStoreType() != PrivateStoreType.NONE)
			{
				return;
			}
			
			// Equip only identical grade arrows.
			final EtcItem etcItem = item.getEtcItem();
			if (etcItem != null)
			{
				final Item weapon = getPaperdollItem(Inventory.PAPERDOLL_RHAND);
				if (weapon != null)
				{
					final EtcItemType itemType = etcItem.getItemType();
					final ItemType weaponItemType = weapon.getItemType();
					if ((((weaponItemType == WeaponType.BOW) && (itemType == EtcItemType.ARROW)) //
						|| (((weaponItemType == WeaponType.CROSSBOW) || (weaponItemType == WeaponType.TWOHANDCROSSBOW)) && (itemType == EtcItemType.BOLT))) //
						&& (weapon.getItem().getCrystalTypePlus() != item.getItem().getCrystalTypePlus()))
					{
						return;
					}
				}
			}
			
			final Player player = (Player) getOwner();
			if (!player.canOverrideCond(PlayerCondOverride.ITEM_CONDITIONS) && !player.isHero() && item.isHeroItem())
			{
				return;
			}
		}
		
		final long targetSlot = item.getItem().getBodyPart();
		
		// Check if player is using Formal Wear and item isn't Wedding Bouquet.
		final Item formal = getPaperdollItem(PAPERDOLL_CHEST);
		if ((item.getId() != 21163) && (formal != null) && (formal.getItem().getBodyPart() == ItemTemplate.SLOT_ALLDRESS))
		{
			// only chest target can pass this
			if ((targetSlot == ItemTemplate.SLOT_LR_HAND) || (targetSlot == ItemTemplate.SLOT_L_HAND) || (targetSlot == ItemTemplate.SLOT_R_HAND) || (targetSlot == ItemTemplate.SLOT_LEGS) || (targetSlot == ItemTemplate.SLOT_FEET) || (targetSlot == ItemTemplate.SLOT_GLOVES) || (targetSlot == ItemTemplate.SLOT_HEAD))
			{
				return;
			}
		}
		
		// don't care about arrows, listener will unequip them (hopefully)
		// handle full armor
		// formal dress
		if (targetSlot == ItemTemplate.SLOT_LR_HAND)
		{
			setPaperdollItem(PAPERDOLL_LHAND, null);
			setPaperdollItem(PAPERDOLL_RHAND, item);
		}
		else if (targetSlot == ItemTemplate.SLOT_L_HAND)
		{
			final Item rh = getPaperdollItem(PAPERDOLL_RHAND);
			if ((rh != null) && (rh.getItem().getBodyPart() == ItemTemplate.SLOT_LR_HAND) && !(((rh.getItemType() == WeaponType.BOW) && (item.getItemType() == EtcItemType.ARROW)) || (((rh.getItemType() == WeaponType.CROSSBOW) || (rh.getItemType() == WeaponType.TWOHANDCROSSBOW)) && (item.getItemType() == EtcItemType.BOLT)) || ((rh.getItemType() == WeaponType.FISHINGROD) && (item.getItemType() == EtcItemType.LURE))))
			{
				setPaperdollItem(PAPERDOLL_RHAND, null);
			}
			setPaperdollItem(PAPERDOLL_LHAND, item);
		}
		else if (targetSlot == ItemTemplate.SLOT_R_HAND)
		{
			setPaperdollItem(PAPERDOLL_RHAND, item);
		}
		else if ((targetSlot == ItemTemplate.SLOT_L_EAR) || (targetSlot == ItemTemplate.SLOT_R_EAR) || (targetSlot == ItemTemplate.SLOT_LR_EAR))
		{
			if (_paperdoll[PAPERDOLL_LEAR] == null)
			{
				setPaperdollItem(PAPERDOLL_LEAR, item);
			}
			else if (_paperdoll[PAPERDOLL_REAR] == null)
			{
				setPaperdollItem(PAPERDOLL_REAR, item);
			}
			else
			{
				setPaperdollItem(PAPERDOLL_LEAR, item);
			}
		}
		else if ((targetSlot == ItemTemplate.SLOT_L_FINGER) || (targetSlot == ItemTemplate.SLOT_R_FINGER) || (targetSlot == ItemTemplate.SLOT_LR_FINGER))
		{
			if (_paperdoll[PAPERDOLL_LFINGER] == null)
			{
				setPaperdollItem(PAPERDOLL_LFINGER, item);
			}
			else if (_paperdoll[PAPERDOLL_RFINGER] == null)
			{
				setPaperdollItem(PAPERDOLL_RFINGER, item);
			}
			else
			{
				setPaperdollItem(PAPERDOLL_LFINGER, item);
			}
		}
		else if (targetSlot == ItemTemplate.SLOT_NECK)
		{
			setPaperdollItem(PAPERDOLL_NECK, item);
		}
		else if (targetSlot == ItemTemplate.SLOT_FULL_ARMOR)
		{
			setPaperdollItem(PAPERDOLL_LEGS, null);
			setPaperdollItem(PAPERDOLL_CHEST, item);
		}
		else if (targetSlot == ItemTemplate.SLOT_CHEST)
		{
			setPaperdollItem(PAPERDOLL_CHEST, item);
		}
		else if (targetSlot == ItemTemplate.SLOT_LEGS)
		{
			final Item chest = getPaperdollItem(PAPERDOLL_CHEST);
			if ((chest != null) && (chest.getItem().getBodyPart() == ItemTemplate.SLOT_FULL_ARMOR))
			{
				setPaperdollItem(PAPERDOLL_CHEST, null);
			}
			setPaperdollItem(PAPERDOLL_LEGS, item);
		}
		else if (targetSlot == ItemTemplate.SLOT_FEET)
		{
			setPaperdollItem(PAPERDOLL_FEET, item);
		}
		else if (targetSlot == ItemTemplate.SLOT_GLOVES)
		{
			setPaperdollItem(PAPERDOLL_GLOVES, item);
		}
		else if (targetSlot == ItemTemplate.SLOT_HEAD)
		{
			setPaperdollItem(PAPERDOLL_HEAD, item);
		}
		else if (targetSlot == ItemTemplate.SLOT_HAIR)
		{
			final Item hair = getPaperdollItem(PAPERDOLL_HAIR);
			if ((hair != null) && (hair.getItem().getBodyPart() == ItemTemplate.SLOT_HAIRALL))
			{
				setPaperdollItem(PAPERDOLL_HAIR2, null);
			}
			else
			{
				setPaperdollItem(PAPERDOLL_HAIR, null);
			}
			setPaperdollItem(PAPERDOLL_HAIR, item);
		}
		else if (targetSlot == ItemTemplate.SLOT_HAIR2)
		{
			final Item hair2 = getPaperdollItem(PAPERDOLL_HAIR);
			if ((hair2 != null) && (hair2.getItem().getBodyPart() == ItemTemplate.SLOT_HAIRALL))
			{
				setPaperdollItem(PAPERDOLL_HAIR, null);
			}
			else
			{
				setPaperdollItem(PAPERDOLL_HAIR2, null);
			}
			setPaperdollItem(PAPERDOLL_HAIR2, item);
		}
		else if (targetSlot == ItemTemplate.SLOT_HAIRALL)
		{
			setPaperdollItem(PAPERDOLL_HAIR2, null);
			setPaperdollItem(PAPERDOLL_HAIR, item);
		}
		else if (targetSlot == ItemTemplate.SLOT_UNDERWEAR)
		{
			setPaperdollItem(PAPERDOLL_UNDER, item);
		}
		else if (targetSlot == ItemTemplate.SLOT_BACK)
		{
			setPaperdollItem(PAPERDOLL_CLOAK, item);
		}
		else if (targetSlot == ItemTemplate.SLOT_L_BRACELET)
		{
			setPaperdollItem(PAPERDOLL_LBRACELET, item);
		}
		else if (targetSlot == ItemTemplate.SLOT_R_BRACELET)
		{
			setPaperdollItem(PAPERDOLL_RBRACELET, item);
		}
		else if (targetSlot == ItemTemplate.SLOT_DECO)
		{
			equipTalisman(item);
		}
		else if (targetSlot == ItemTemplate.SLOT_BELT)
		{
			setPaperdollItem(PAPERDOLL_BELT, item);
		}
		else if (targetSlot == ItemTemplate.SLOT_ALLDRESS)
		{
			setPaperdollItem(PAPERDOLL_LEGS, null);
			setPaperdollItem(PAPERDOLL_LHAND, null);
			setPaperdollItem(PAPERDOLL_RHAND, null);
			setPaperdollItem(PAPERDOLL_HEAD, null);
			setPaperdollItem(PAPERDOLL_FEET, null);
			setPaperdollItem(PAPERDOLL_GLOVES, null);
			setPaperdollItem(PAPERDOLL_CHEST, item);
		}
		else if (targetSlot == ItemTemplate.SLOT_BROOCH)
		{
			setPaperdollItem(PAPERDOLL_BROOCH, item);
		}
		else if (targetSlot == ItemTemplate.SLOT_BROOCH_JEWEL)
		{
			equipBroochJewel(item);
		}
		else if (targetSlot == ItemTemplate.SLOT_AGATHION)
		{
			equipAgathion(item);
		}
		else if (targetSlot == ItemTemplate.SLOT_ARTIFACT_BOOK)
		{
			setPaperdollItem(PAPERDOLL_ARTIFACT_BOOK, item);
		}
		else if (targetSlot == ItemTemplate.SLOT_ARTIFACT)
		{
			equipArtifact(item);
		}
		else
		{
			LOGGER.warning("Unknown body slot " + targetSlot + " for Item ID: " + item.getId());
		}
	}
	
	/**
	 * Refresh the weight of equipment loaded
	 */
	@Override
	protected void refreshWeight()
	{
		long weight = 0;
		for (Item item : _items)
		{
			if ((item != null) && (item.getItem() != null))
			{
				weight += item.getItem().getWeight() * item.getCount();
			}
		}
		_totalWeight = (int) Math.min(weight, Integer.MAX_VALUE);
	}
	
	/**
	 * @return the totalWeight.
	 */
	public int getTotalWeight()
	{
		return _totalWeight;
	}
	
	/**
	 * Return the Item of the arrows needed for this bow.
	 * @param bow : Item designating the bow
	 * @return Item pointing out arrows for bow
	 */
	public Item findArrowForBow(ItemTemplate bow)
	{
		if (bow == null)
		{
			return null;
		}
		
		Item arrow = null;
		for (Item item : _items)
		{
			if (item.isEtcItem() && (item.getEtcItem().getItemType() == EtcItemType.ARROW) && (item.getItem().getCrystalTypePlus() == bow.getCrystalTypePlus()))
			{
				arrow = item;
				break;
			}
		}
		
		// Get the Item corresponding to the item identifier and return it
		return arrow;
	}
	
	/**
	 * Return the Item of the bolts needed for this crossbow.
	 * @param crossbow : Item designating the crossbow
	 * @return Item pointing out bolts for crossbow
	 */
	public Item findBoltForCrossBow(ItemTemplate crossbow)
	{
		Item bolt = null;
		for (Item item : _items)
		{
			if (item.isEtcItem() && (item.getEtcItem().getItemType() == EtcItemType.BOLT) && (item.getItem().getCrystalTypePlus() == crossbow.getCrystalTypePlus()))
			{
				bolt = item;
				break;
			}
		}
		
		// Get the Item corresponding to the item identifier and return it
		return bolt;
	}
	
	/**
	 * Get back items in inventory from database
	 */
	@Override
	public void restore()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM items WHERE owner_id=? AND (loc=? OR loc=?) ORDER BY loc_data"))
		{
			ps.setInt(1, getOwnerId());
			ps.setString(2, getBaseLocation().name());
			ps.setString(3, getEquipLocation().name());
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					try
					{
						final Item item = new Item(rs);
						if (getOwner().isPlayer())
						{
							final Player player = (Player) getOwner();
							if (!player.canOverrideCond(PlayerCondOverride.ITEM_CONDITIONS) && !player.isHero() && item.isHeroItem())
							{
								item.setItemLocation(ItemLocation.INVENTORY);
							}
						}
						
						World.getInstance().addObject(item);
						
						// If stackable item is found in inventory just add to current quantity
						if (item.isStackable() && (getItemByItemId(item.getId()) != null))
						{
							addItem("Restore", item, getOwner().getActingPlayer(), null);
						}
						else
						{
							addItem(item);
						}
					}
					catch (Exception e)
					{
						LOGGER.warning("Could not restore item " + rs.getInt("item_id") + " for " + getOwner());
					}
				}
			}
			refreshWeight();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Could not restore inventory: " + e.getMessage(), e);
		}
	}
	
	public int getTalismanSlots()
	{
		return getOwner().getActingPlayer().getStat().getTalismanSlots();
	}
	
	private void equipTalisman(Item item)
	{
		if (getTalismanSlots() == 0)
		{
			return;
		}
		
		// find same (or incompatible) talisman type
		for (int i = PAPERDOLL_DECO1; i < (PAPERDOLL_DECO1 + getTalismanSlots()); i++)
		{
			if ((_paperdoll[i] != null) && (getPaperdollItemId(i) == item.getId()))
			{
				// overwrite
				setPaperdollItem(i, item);
				return;
			}
		}
		
		// no free slot found - put on first free
		for (int i = PAPERDOLL_DECO1; i < (PAPERDOLL_DECO1 + getTalismanSlots()); i++)
		{
			if (_paperdoll[i] == null)
			{
				setPaperdollItem(i, item);
				return;
			}
		}
	}
	
	public int getArtifactSlots()
	{
		return getOwner().getActingPlayer().getStat().getArtifactSlots();
	}
	
	private void equipArtifact(Item item)
	{
		final int slotNumber = getArtifactSlots();
		if (slotNumber == 0)
		{
			return;
		}
		
		switch (item.getItem().getArtifactSlot())
		{
			case 1: // Attack
			{
				for (int i = PAPERDOLL_ARTIFACT13; i < (PAPERDOLL_ARTIFACT13 + slotNumber); i++)
				{
					if ((i <= PAPERDOLL_ARTIFACT15) && (_paperdoll[i] == null))
					{
						setPaperdollItem(i, item);
						return;
					}
				}
				break;
			}
			case 2: // Protection
			{
				for (int i = PAPERDOLL_ARTIFACT16; i < (PAPERDOLL_ARTIFACT16 + slotNumber); i++)
				{
					if ((i <= PAPERDOLL_ARTIFACT18) && (_paperdoll[i] == null))
					{
						setPaperdollItem(i, item);
						return;
					}
				}
				break;
			}
			case 3: // Support
			{
				for (int i = PAPERDOLL_ARTIFACT19; i < (PAPERDOLL_ARTIFACT19 + slotNumber); i++)
				{
					if ((i <= PAPERDOLL_ARTIFACT21) && (_paperdoll[i] == null))
					{
						setPaperdollItem(i, item);
						return;
					}
				}
				break;
			}
			case 4: // Balance
			{
				for (int i = PAPERDOLL_ARTIFACT1; i < (PAPERDOLL_ARTIFACT1 + (4 * slotNumber)); i++)
				{
					if ((i <= PAPERDOLL_ARTIFACT12) && (_paperdoll[i] == null))
					{
						setPaperdollItem(i, item);
						return;
					}
				}
				break;
			}
		}
	}
	
	public int getBroochJewelSlots()
	{
		return getOwner().getActingPlayer().getStat().getBroochJewelSlots();
	}
	
	private void equipBroochJewel(Item item)
	{
		if (getBroochJewelSlots() == 0)
		{
			return;
		}
		
		// find same (or incompatible) brooch jewel type
		for (int i = PAPERDOLL_BROOCH_JEWEL1; i < (PAPERDOLL_BROOCH_JEWEL1 + getBroochJewelSlots()); i++)
		{
			if ((_paperdoll[i] != null) && (getPaperdollItemId(i) == item.getId()))
			{
				// overwrite
				setPaperdollItem(i, item);
				return;
			}
		}
		
		// no free slot found - put on first free
		for (int i = PAPERDOLL_BROOCH_JEWEL1; i < (PAPERDOLL_BROOCH_JEWEL1 + getBroochJewelSlots()); i++)
		{
			if (_paperdoll[i] == null)
			{
				setPaperdollItem(i, item);
				return;
			}
		}
	}
	
	public int getAgathionSlots()
	{
		return getOwner().getActingPlayer().getStat().getAgathionSlots();
	}
	
	private void equipAgathion(Item item)
	{
		if (getAgathionSlots() == 0)
		{
			return;
		}
		
		// find same (or incompatible) agathion type
		for (int i = PAPERDOLL_AGATHION1; i < (PAPERDOLL_AGATHION1 + getAgathionSlots()); i++)
		{
			if ((_paperdoll[i] != null) && (getPaperdollItemId(i) == item.getId()))
			{
				// overwrite
				setPaperdollItem(i, item);
				return;
			}
		}
		
		// no free slot found - put on first free
		for (int i = PAPERDOLL_AGATHION1; i < (PAPERDOLL_AGATHION1 + getAgathionSlots()); i++)
		{
			if (_paperdoll[i] == null)
			{
				setPaperdollItem(i, item);
				return;
			}
		}
	}
	
	public boolean canEquipCloak()
	{
		return getOwner().getActingPlayer().getStat().canEquipCloak();
	}
	
	/**
	 * Re-notify to paperdoll listeners every equipped item.<br>
	 * Only used by player ClassId set methods.
	 */
	public void reloadEquippedItems()
	{
		int slot;
		for (Item item : _paperdoll)
		{
			if (item == null)
			{
				continue;
			}
			
			slot = item.getLocationSlot();
			for (PaperdollListener listener : _paperdollListeners)
			{
				if (listener == null)
				{
					continue;
				}
				
				listener.notifyUnequiped(slot, item, this);
				listener.notifyEquiped(slot, item, this);
			}
		}
		
		if (getOwner().isPlayer())
		{
			getOwner().sendPacket(new ExUserInfoEquipSlot(getOwner().getActingPlayer()));
		}
	}
	
	public int getArmorMinEnchant()
	{
		if ((getOwner() == null) || !getOwner().isPlayer())
		{
			return 0;
		}
		
		final Player player = getOwner().getActingPlayer();
		return _paperdollCache.getMaxSetEnchant(player);
	}
	
	public int getWeaponEnchant()
	{
		final Item item = getPaperdollItem(PAPERDOLL_RHAND);
		return item != null ? item.getEnchantLevel() : 0;
	}
	
	/**
	 * Blocks the given item slot from being equipped.
	 * @param itemSlot mask from Item
	 */
	public void blockItemSlot(long itemSlot)
	{
		_blockedItemSlotsMask |= itemSlot;
	}
	
	/**
	 * Unblocks the given item slot so it can be equipped.
	 * @param itemSlot mask from Item
	 */
	public void unblockItemSlot(long itemSlot)
	{
		_blockedItemSlotsMask &= ~itemSlot;
	}
	
	/**
	 * @param itemSlot mask from Item
	 * @return if the given item slot is blocked or not.
	 */
	public boolean isItemSlotBlocked(long itemSlot)
	{
		return (_blockedItemSlotsMask & itemSlot) == itemSlot;
	}
	
	/**
	 * @param itemSlotsMask use 0 to unset all blocked item slots.
	 */
	public void setBlockedItemSlotsMask(int itemSlotsMask)
	{
		_blockedItemSlotsMask = itemSlotsMask;
	}
	
	/**
	 * Reduce the arrow number of the Creature.<br>
	 * <br>
	 * <b><u>Overridden in</u>:</b>
	 * <li>Player</li><br>
	 * @param type
	 */
	public void reduceArrowCount(EtcItemType type)
	{
		// default is to do nothing
	}
	
	/**
	 * Gets the items in paperdoll slots filtered by filter.
	 * @param filters multiple filters
	 * @return the filtered items in inventory
	 */
	@SafeVarargs
	public final Collection<Item> getPaperdollItems(Predicate<Item>... filters)
	{
		if (filters.length == 0)
		{
			return _paperdollCache.getPaperdollItems();
		}
		
		Predicate<Item> filter = Objects::nonNull;
		for (Predicate<Item> additionalFilter : filters)
		{
			filter = filter.and(additionalFilter);
		}
		
		final List<Item> items = new ArrayList<>(_paperdoll.length / (filters.length + 1));
		for (Item item : _paperdoll)
		{
			if (filter.test(item))
			{
				items.add(item);
			}
		}
		return items;
	}
	
	@SafeVarargs
	public final int getPaperdollItemCount(Predicate<Item>... filters)
	{
		if (filters.length == 0)
		{
			return _paperdollCache.getPaperdollItems().size();
		}
		
		Predicate<Item> filter = Objects::nonNull;
		for (Predicate<Item> additionalFilter : filters)
		{
			filter = filter.and(additionalFilter);
		}
		
		int count = 0;
		for (Item item : _paperdoll)
		{
			if (filter.test(item))
			{
				count++;
			}
		}
		return count;
	}
	
	public PaperdollCache getPaperdollCache()
	{
		return _paperdollCache;
	}
}
