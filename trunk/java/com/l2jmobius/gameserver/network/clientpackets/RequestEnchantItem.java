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
package com.l2jmobius.gameserver.network.clientpackets;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.data.xml.impl.EnchantItemData;
import com.l2jmobius.gameserver.enums.UserInfoType;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.request.EnchantItemRequest;
import com.l2jmobius.gameserver.model.items.L2Item;
import com.l2jmobius.gameserver.model.items.enchant.EnchantResultType;
import com.l2jmobius.gameserver.model.items.enchant.EnchantScroll;
import com.l2jmobius.gameserver.model.items.enchant.EnchantSupportItem;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;
import com.l2jmobius.gameserver.model.items.type.EtcItemType;
import com.l2jmobius.gameserver.model.skills.CommonSkill;
import com.l2jmobius.gameserver.model.skills.Skill;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.EnchantResult;
import com.l2jmobius.gameserver.network.serverpackets.ExAdenaInvenCount;
import com.l2jmobius.gameserver.network.serverpackets.ExUserInfoInvenWeight;
import com.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jmobius.gameserver.network.serverpackets.ItemList;
import com.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.util.Util;
import com.l2jmobius.util.Rnd;

public final class RequestEnchantItem extends L2GameClientPacket
{
	protected static final Logger _logEnchant = Logger.getLogger("enchant");
	
	private static final String _C__5F_REQUESTENCHANTITEM = "[C] 5F RequestEnchantItem";
	
	private int _objectId;
	private int _supportId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_supportId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		final EnchantItemRequest request = activeChar.getRequest(EnchantItemRequest.class);
		if ((request == null) || request.isProcessing())
		{
			return;
		}
		
		request.setEnchantingItem(_objectId);
		request.setProcessing(true);
		
		if (!activeChar.isOnline() || getClient().isDetached())
		{
			activeChar.removeRequest(request.getClass());
			return;
		}
		
		if (activeChar.isProcessingTransaction() || activeChar.isInStoreMode())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_ENCHANT_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
			activeChar.removeRequest(request.getClass());
			return;
		}
		
		final L2ItemInstance item = request.getEnchantingItem();
		final L2ItemInstance scroll = request.getEnchantingScroll();
		final L2ItemInstance support = request.getSupportItem();
		if ((item == null) || (scroll == null))
		{
			activeChar.removeRequest(request.getClass());
			return;
		}
		
		// template for scroll
		final EnchantScroll scrollTemplate = EnchantItemData.getInstance().getEnchantScroll(scroll);
		if (scrollTemplate == null)
		{
			return;
		}
		
		// template for support item, if exist
		EnchantSupportItem supportTemplate = null;
		if (support != null)
		{
			if (support.getObjectId() != _supportId)
			{
				activeChar.removeRequest(request.getClass());
				return;
			}
			supportTemplate = EnchantItemData.getInstance().getSupportItem(support);
		}
		
		// first validation check - also over enchant check
		if (!scrollTemplate.isValid(item, supportTemplate) || (Config.DISABLE_OVER_ENCHANTING && (item.getEnchantLevel() == scrollTemplate.getMaxEnchantLevel())))
		{
			activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITIONS);
			activeChar.removeRequest(request.getClass());
			activeChar.sendPacket(new EnchantResult(EnchantResult.ERROR, 0, 0));
			return;
		}
		
		// fast auto-enchant cheat check
		if ((request.getTimestamp() == 0) || ((System.currentTimeMillis() - request.getTimestamp()) < 2000))
		{
			Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " use autoenchant program ", Config.DEFAULT_PUNISH);
			activeChar.removeRequest(request.getClass());
			activeChar.sendPacket(new EnchantResult(EnchantResult.ERROR, 0, 0));
			return;
		}
		
		// attempting to destroy scroll
		if (activeChar.getInventory().destroyItem("Enchant", scroll.getObjectId(), 1, activeChar, item) == null)
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT);
			Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to enchant with a scroll he doesn't have", Config.DEFAULT_PUNISH);
			activeChar.removeRequest(request.getClass());
			activeChar.sendPacket(new EnchantResult(EnchantResult.ERROR, 0, 0));
			return;
		}
		
		// attempting to destroy support if exist
		if ((support != null) && (activeChar.getInventory().destroyItem("Enchant", support.getObjectId(), 1, activeChar, item) == null))
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT);
			Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to enchant with a support item he doesn't have", Config.DEFAULT_PUNISH);
			activeChar.removeRequest(request.getClass());
			activeChar.sendPacket(new EnchantResult(EnchantResult.ERROR, 0, 0));
			return;
		}
		
		final InventoryUpdate iu = new InventoryUpdate();
		synchronized (item)
		{
			// last validation check
			if ((item.getOwnerId() != activeChar.getObjectId()) || (item.isEnchantable() == 0))
			{
				activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITIONS);
				activeChar.removeRequest(request.getClass());
				activeChar.sendPacket(new EnchantResult(EnchantResult.ERROR, 0, 0));
				return;
			}
			
			final EnchantResultType resultType = scrollTemplate.calculateSuccess(activeChar, item, supportTemplate);
			switch (resultType)
			{
				case ERROR:
				{
					activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITIONS);
					activeChar.removeRequest(request.getClass());
					activeChar.sendPacket(new EnchantResult(EnchantResult.ERROR, 0, 0));
					break;
				}
				case SUCCESS:
				{
					Skill enchant4Skill = null;
					final L2Item it = item.getItem();
					// Increase enchant level only if scroll's base template has chance, some armors can success over +20 but they shouldn't have increased.
					if (scrollTemplate.getChance(activeChar, item) > 0)
					{
						if (scrollTemplate.isGiant() && ((supportTemplate == null) || (supportTemplate.getItem().getItemType() != EtcItemType.GIANT2_SCRL_BLESS_INC_ENCHANT_PROP_AM) || (supportTemplate.getItem().getItemType() != EtcItemType.GIANT2_SCRL_BLESS_INC_ENCHANT_PROP_WP)))
						{
							item.setEnchantLevel(item.getEnchantLevel() + Rnd.get(1, 3));
						}
						else if (scrollTemplate.isGiant() && (supportTemplate != null) && ((supportTemplate.getItem().getItemType() == EtcItemType.GIANT2_SCRL_BLESS_INC_ENCHANT_PROP_AM) || (supportTemplate.getItem().getItemType() == EtcItemType.GIANT2_SCRL_BLESS_INC_ENCHANT_PROP_WP)))
						{
							item.setEnchantLevel(item.getEnchantLevel() + Rnd.get(2, 4));
						}
						else
						{
							item.setEnchantLevel(item.getEnchantLevel() + 1);
						}
						item.updateDatabase();
					}
					activeChar.sendPacket(new EnchantResult(EnchantResult.SUCCESS, item));
					
					if (Config.LOG_ITEM_ENCHANTS)
					{
						final LogRecord record = new LogRecord(Level.INFO, "Success");
						record.setParameters(new Object[]
						{
							activeChar,
							item,
							scroll,
							support,
						});
						record.setLoggerName("item");
						_logEnchant.log(record);
					}
					
					// announce the success
					final int minEnchantAnnounce = item.isArmor() ? 6 : 7;
					final int maxEnchantAnnounce = item.isArmor() ? 0 : 15;
					if ((item.getEnchantLevel() == minEnchantAnnounce) || (item.getEnchantLevel() == maxEnchantAnnounce))
					{
						final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_SUCCESSFULLY_ENCHANTED_A_S2_S3);
						sm.addCharName(activeChar);
						sm.addInt(item.getEnchantLevel());
						sm.addItemName(item);
						activeChar.broadcastPacket(sm);
						
						final Skill skill = CommonSkill.FIREWORK.getSkill();
						if (skill != null)
						{
							activeChar.broadcastPacket(new MagicSkillUse(activeChar, activeChar, skill.getId(), skill.getLevel(), skill.getHitTime(), skill.getReuseDelay()));
						}
					}
					
					if ((item.isArmor()) && (item.getEnchantLevel() >= 4) && item.isEquipped())
					{
						enchant4Skill = it.getEnchant4Skill();
						if (enchant4Skill != null)
						{
							// add skills bestowed from +4 armor
							activeChar.addSkill(enchant4Skill, false);
							activeChar.sendSkillList();
						}
					}
					break;
				}
				case FAILURE:
				{
					if (scrollTemplate.isSafe())
					{
						// safe enchant - remain old value
						activeChar.sendPacket(SystemMessageId.ENCHANT_FAILED_THE_ENCHANT_SKILL_FOR_THE_CORRESPONDING_ITEM_WILL_BE_EXACTLY_RETAINED);
						activeChar.sendPacket(new EnchantResult(EnchantResult.SAFE_FAIL, item));
						
						if (Config.LOG_ITEM_ENCHANTS)
						{
							final LogRecord record = new LogRecord(Level.INFO, "Safe Fail");
							record.setParameters(new Object[]
							{
								activeChar,
								item,
								scroll,
								support,
							});
							record.setLoggerName("item");
							_logEnchant.log(record);
						}
					}
					else
					{
						// unequip item on enchant failure to avoid item skills stack
						if (item.isEquipped())
						{
							if (item.getEnchantLevel() > 0)
							{
								final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_EQUIPMENT_S1_S2_HAS_BEEN_REMOVED);
								sm.addInt(item.getEnchantLevel());
								sm.addItemName(item);
								activeChar.sendPacket(sm);
							}
							else
							{
								final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_UNEQUIPPED);
								sm.addItemName(item);
								activeChar.sendPacket(sm);
							}
							
							final L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(item.getLocationSlot());
							for (L2ItemInstance itm : unequiped)
							{
								iu.addModifiedItem(itm);
							}
							
							activeChar.sendPacket(iu);
							activeChar.broadcastUserInfo();
						}
						
						if (scrollTemplate.isBlessed() && (supportTemplate != null) && ((supportTemplate.getItem().getItemType() == EtcItemType.BLESS_DROP_SCRL_INC_ENCHANT_PROP_AM) || (supportTemplate.getItem().getItemType() == EtcItemType.BLESS_DROP_SCRL_INC_ENCHANT_PROP_WP)))
						{
							// blessed enchant + using special stone - drop enchant value by 1
							activeChar.sendPacket(SystemMessageId.FAILURE_WILL_DEDUCT_YOUR_ENCHANT_VALUE_BY_1);
							
							item.setEnchantLevel(item.getEnchantLevel() - 1);
							item.updateDatabase();
							activeChar.sendPacket(new EnchantResult(EnchantResult.BLESSED_FAIL, 0, 0));
							
							if (Config.LOG_ITEM_ENCHANTS)
							{
								final LogRecord record = new LogRecord(Level.INFO, "Blessed Fail");
								record.setParameters(new Object[]
								{
									activeChar,
									item,
									scroll,
									support,
								});
								record.setLoggerName("item");
								_logEnchant.log(record);
							}
						}
						else if (scrollTemplate.isBlessed() || ((supportTemplate != null) && ((supportTemplate.getItem().getItemType() == EtcItemType.GIANT_SCRL_BLESS_INC_ENCHANT_PROP_AM) || (supportTemplate.getItem().getItemType() == EtcItemType.GIANT_SCRL_BLESS_INC_ENCHANT_PROP_WP) || (supportTemplate.getItem().getItemType() == EtcItemType.SCRL_BLESS_INC_ENCHANT_PROP_AM) || (supportTemplate.getItem().getItemType() == EtcItemType.SCRL_BLESS_INC_ENCHANT_PROP_WP))))
						{
							// blessed enchant - clear enchant value
							activeChar.sendPacket(SystemMessageId.THE_BLESSED_ENCHANT_FAILED_THE_ENCHANT_VALUE_OF_THE_ITEM_BECAME_0);
							
							item.setEnchantLevel(0);
							item.updateDatabase();
							activeChar.sendPacket(new EnchantResult(EnchantResult.BLESSED_FAIL, 0, 0));
							
							if (Config.LOG_ITEM_ENCHANTS)
							{
								final LogRecord record = new LogRecord(Level.INFO, "Blessed Fail");
								record.setParameters(new Object[]
								{
									activeChar,
									item,
									scroll,
									support,
								});
								record.setLoggerName("item");
								_logEnchant.log(record);
							}
						}
						else
						{
							// enchant failed, destroy item
							if (activeChar.getInventory().destroyItem("Enchant", item, activeChar, null) == null)
							{
								// unable to destroy item, cheater ?
								Util.handleIllegalPlayerAction(activeChar, "Unable to delete item on enchant failure from player " + activeChar.getName() + ", possible cheater !", Config.DEFAULT_PUNISH);
								activeChar.removeRequest(request.getClass());
								activeChar.sendPacket(new EnchantResult(EnchantResult.ERROR, 0, 0));
								
								if (Config.LOG_ITEM_ENCHANTS)
								{
									final LogRecord record = new LogRecord(Level.INFO, "Unable to destroy");
									record.setParameters(new Object[]
									{
										activeChar,
										item,
										scroll,
										support,
									});
									record.setLoggerName("item");
									_logEnchant.log(record);
								}
								return;
							}
							
							L2World.getInstance().removeObject(item);
							
							final int crystalId = item.getItem().getCrystalItemId();
							if ((crystalId != 0) && item.getItem().isCrystallizable())
							{
								int count = item.getCrystalCount() - ((item.getItem().getCrystalCount() + 1) / 2);
								count = count < 1 ? 1 : count;
								activeChar.getInventory().addItem("Enchant", crystalId, count, activeChar, item);
								
								final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EARNED_S2_S1_S);
								sm.addItemName(crystalId);
								sm.addLong(count);
								activeChar.sendPacket(sm);
								activeChar.sendPacket(new EnchantResult(EnchantResult.NO_CRYSTAL, crystalId, count));
							}
							else
							{
								activeChar.sendPacket(new EnchantResult(EnchantResult.FAIL, 0, 0));
							}
							
							if (Config.LOG_ITEM_ENCHANTS)
							{
								final LogRecord record = new LogRecord(Level.INFO, "Fail");
								record.setParameters(new Object[]
								{
									activeChar,
									item,
									scroll,
									support,
								});
								record.setLoggerName("item");
								_logEnchant.log(record);
							}
						}
					}
					break;
				}
			}
			
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				if (scroll.getCount() == 0)
				{
					iu.addRemovedItem(scroll);
				}
				else
				{
					iu.addModifiedItem(scroll);
				}
				
				if (item.getCount() == 0)
				{
					iu.addRemovedItem(item);
				}
				else
				{
					iu.addModifiedItem(item);
				}
				
				if (support != null)
				{
					if (support.getCount() == 0)
					{
						iu.addRemovedItem(support);
					}
					else
					{
						iu.addModifiedItem(support);
					}
				}
				
				activeChar.sendPacket(iu);
			}
			else
			{
				activeChar.sendPacket(new ItemList(activeChar, true));
			}
			
			request.setProcessing(false);
			activeChar.broadcastUserInfo(UserInfoType.ENCHANTLEVEL);
			activeChar.sendPacket(new ExUserInfoInvenWeight(activeChar));
			activeChar.sendPacket(new ExAdenaInvenCount(activeChar));
		}
	}
	
	@Override
	public String getType()
	{
		return _C__5F_REQUESTENCHANTITEM;
	}
}
