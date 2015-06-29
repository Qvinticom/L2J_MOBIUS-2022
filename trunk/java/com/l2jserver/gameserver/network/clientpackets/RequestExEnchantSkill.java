/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.network.clientpackets;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.l2jserver.Config;
import com.l2jserver.gameserver.data.xml.impl.EnchantSkillGroupsData;
import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.model.L2EnchantSkillGroup.EnchantSkillHolder;
import com.l2jserver.gameserver.model.L2EnchantSkillLearn;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.itemcontainer.Inventory;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ExEnchantSkillInfo;
import com.l2jserver.gameserver.network.serverpackets.ExEnchantSkillInfoDetail;
import com.l2jserver.gameserver.network.serverpackets.ExEnchantSkillResult;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;
import com.l2jserver.gameserver.network.serverpackets.UserInfo;
import com.l2jserver.util.Rnd;

/**
 * Format (ch) dd c: (id) 0xD0 h: (subid) 0x06 d: skill id d: skill lvl
 * @author -Wooden-
 */
public final class RequestExEnchantSkill extends L2GameClientPacket
{
	private static final String _C__D0_0F_REQUESTEXENCHANTSKILL = "[C] D0:0F RequestExEnchantSkill";
	private static final Logger _logEnchant = Logger.getLogger("enchant");
	
	private int _type; // enchant type: 0 - normal, 1 - safe, 2 - untrain, 3 - change route, 4 - 100%
	
	private int _skillId;
	private int _skillLvl;
	
	@Override
	protected void readImpl()
	{
		_type = readD();
		_skillId = readD();
		_skillLvl = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if ((_skillId <= 0) || (_skillLvl <= 0))
		{
			return;
		}
		
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (player.getClassId().level() < 3) // requires to have 3rd class quest completed
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THE_SKILL_ENHANCING_FUNCTION_IN_THIS_CLASS_YOU_CAN_USE_CORRESPONDING_FUNCTION_WHEN_COMPLETING_THE_THIRD_CLASS_CHANGE);
			return;
		}
		
		if (player.getLevel() < 76)
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THE_SKILL_ENHANCING_FUNCTION_ON_THIS_LEVEL_YOU_CAN_USE_THE_CORRESPONDING_FUNCTION_ON_LEVELS_HIGHER_THAN_LV_76);
			return;
		}
		
		if (!player.isAllowedToEnchantSkills())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THE_SKILL_ENHANCING_FUNCTION_IN_THIS_STATE_YOU_CAN_ENHANCE_SKILLS_WHEN_NOT_IN_BATTLE_AND_CANNOT_USE_THE_FUNCTION_WHILE_TRANSFORMED_IN_BATTLE_ON_A_MOUNT_OR_WHILE_THE_SKILL_IS_ON_COOLDOWN);
			return;
		}
		
		final Skill skill = SkillData.getInstance().getSkill(_skillId, _skillLvl);
		if (skill == null)
		{
			return;
		}
		
		final L2EnchantSkillLearn s = EnchantSkillGroupsData.getInstance().getSkillEnchantmentBySkillId(_skillId);
		if (s == null)
		{
			return;
		}
		if (_type == 0) // enchant
		{
			final EnchantSkillHolder esd = s.getEnchantSkillHolder(_skillLvl);
			final int beforeEnchantSkillLevel = player.getSkillLevel(_skillId);
			if (beforeEnchantSkillLevel != s.getMinSkillLevel(_skillLvl))
			{
				return;
			}
			
			final int costMultiplier = EnchantSkillGroupsData.NORMAL_ENCHANT_COST_MULTIPLIER;
			final int requiredSp = esd.getSpCost() * costMultiplier;
			if (player.getSp() >= requiredSp)
			{
				// only first lvl requires book
				final boolean usesBook = (_skillLvl % 100) == 1; // 101, 201, 301 ...
				final int reqItemId;
				if (player.getClassId().level() == 3)
				{
					reqItemId = EnchantSkillGroupsData.NORMAL_ENCHANT_BOOK_OLD;
				}
				else
				{
					reqItemId = EnchantSkillGroupsData.NORMAL_ENCHANT_BOOK;
				}
				final L2ItemInstance spb = player.getInventory().getItemByItemId(reqItemId);
				
				if (Config.ES_SP_BOOK_NEEDED && usesBook && (spb == null))
				{
					player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
					return;
				}
				
				final int requiredAdena = (esd.getAdenaCost() * costMultiplier);
				if (player.getInventory().getAdena() < requiredAdena)
				{
					player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
					return;
				}
				
				boolean check = player.getStat().removeExpAndSp(0, requiredSp, false);
				if (Config.ES_SP_BOOK_NEEDED && usesBook)
				{
					check &= player.destroyItem("Consume", spb.getObjectId(), 1, player, true);
				}
				
				check &= player.destroyItemByItemId("Consume", Inventory.ADENA_ID, requiredAdena, player, true);
				if (!check)
				{
					player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
					return;
				}
				
				// ok. Destroy ONE copy of the book
				final int rate = esd.getRate(player);
				if (Rnd.get(100) <= rate)
				{
					if (Config.LOG_SKILL_ENCHANTS)
					{
						final LogRecord record = new LogRecord(Level.INFO, "Success");
						record.setParameters(new Object[]
						{
							player,
							skill,
							spb,
							rate
						});
						record.setLoggerName("skill");
						_logEnchant.log(record);
					}
					
					player.addSkill(skill, true);
					player.sendPacket(ExEnchantSkillResult.valueOf(true));
					
					final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SKILL_ENCHANT_WAS_SUCCESSFUL_S1_HAS_BEEN_ENCHANTED);
					sm.addSkillName(_skillId);
					player.sendPacket(sm);
					
					if (Config.DEBUG)
					{
						_log.fine("Learned skill ID: " + _skillId + " Level: " + _skillLvl + " for " + requiredSp + " SP, " + requiredAdena + " Adena.");
					}
				}
				else
				{
					player.addSkill(SkillData.getInstance().getSkill(_skillId, s.getBaseLevel()), true);
					player.sendPacket(SystemMessageId.SKILL_ENCHANT_FAILED_THE_SKILL_WILL_BE_INITIALIZED);
					player.sendPacket(ExEnchantSkillResult.valueOf(false));
					
					if (Config.LOG_SKILL_ENCHANTS)
					{
						final LogRecord record = new LogRecord(Level.INFO, "Fail");
						record.setParameters(new Object[]
						{
							player,
							skill,
							spb,
							rate
						});
						record.setLoggerName("skill");
						_logEnchant.log(record);
					}
				}
				
				player.sendPacket(new UserInfo(player));
				player.sendSkillList();
				final int afterEnchantSkillLevel = player.getSkillLevel(_skillId);
				player.sendPacket(new ExEnchantSkillInfo(_skillId, afterEnchantSkillLevel));
				player.sendPacket(new ExEnchantSkillInfoDetail(0, _skillId, afterEnchantSkillLevel + 1, player));
				player.updateShortCuts(_skillId, afterEnchantSkillLevel);
			}
			else
			{
				player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
			}
		}
		else if (_type == 1) // safe enchant
		{
			int costMultiplier = EnchantSkillGroupsData.SAFE_ENCHANT_COST_MULTIPLIER;
			final int reqItemId;
			if (player.getClassId().level() == 3)
			{
				reqItemId = EnchantSkillGroupsData.SAFE_ENCHANT_BOOK_OLD;
			}
			else
			{
				reqItemId = EnchantSkillGroupsData.SAFE_ENCHANT_BOOK;
			}
			final EnchantSkillHolder esd = s.getEnchantSkillHolder(_skillLvl);
			final int beforeEnchantSkillLevel = player.getSkillLevel(_skillId);
			if (beforeEnchantSkillLevel != s.getMinSkillLevel(_skillLvl))
			{
				return;
			}
			
			int requiredSp = esd.getSpCost() * costMultiplier;
			int requireditems = esd.getAdenaCost() * costMultiplier;
			int rate = esd.getRate(player);
			
			if (player.getSp() >= requiredSp)
			{
				L2ItemInstance spb = player.getInventory().getItemByItemId(reqItemId);
				if (spb == null)
				{
					player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
					return;
				}
				
				if (player.getInventory().getAdena() < requireditems)
				{
					player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
					return;
				}
				
				boolean check = player.getStat().removeExpAndSp(0, requiredSp, false);
				check &= player.destroyItem("Consume", spb.getObjectId(), 1, player, true);
				
				check &= player.destroyItemByItemId("Consume", Inventory.ADENA_ID, requireditems, player, true);
				
				if (!check)
				{
					player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
					return;
				}
				
				if (Rnd.get(100) <= rate)
				{
					if (Config.LOG_SKILL_ENCHANTS)
					{
						LogRecord record = new LogRecord(Level.INFO, "Safe Success");
						record.setParameters(new Object[]
						{
							player,
							skill,
							spb,
							rate
						});
						record.setLoggerName("skill");
						_logEnchant.log(record);
					}
					
					player.addSkill(skill, true);
					
					if (Config.DEBUG)
					{
						_log.fine("Learned skill ID: " + _skillId + " Level: " + _skillLvl + " for " + requiredSp + " SP, " + requireditems + " Adena.");
					}
					
					player.sendPacket(ExEnchantSkillResult.valueOf(true));
					
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SKILL_ENCHANT_WAS_SUCCESSFUL_S1_HAS_BEEN_ENCHANTED);
					sm.addSkillName(_skillId);
					player.sendPacket(sm);
				}
				else
				{
					if (Config.LOG_SKILL_ENCHANTS)
					{
						LogRecord record = new LogRecord(Level.INFO, "Safe Fail");
						record.setParameters(new Object[]
						{
							player,
							skill,
							spb,
							rate
						});
						record.setLoggerName("skill");
						_logEnchant.log(record);
					}
					
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SKILL_ENCHANT_FAILED_THE_SKILL_WILL_BE_INITIALIZED);
					sm.addSkillName(_skillId);
					player.sendPacket(sm);
					player.sendPacket(ExEnchantSkillResult.valueOf(false));
				}
				
				player.sendPacket(new UserInfo(player));
				player.sendSkillList();
				final int afterEnchantSkillLevel = player.getSkillLevel(_skillId);
				player.sendPacket(new ExEnchantSkillInfo(_skillId, afterEnchantSkillLevel));
				player.sendPacket(new ExEnchantSkillInfoDetail(1, _skillId, afterEnchantSkillLevel + 1, player));
				player.updateShortCuts(_skillId, afterEnchantSkillLevel);
			}
			else
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
				player.sendPacket(sm);
			}
		}
		else if (_type == 2) // untrain
		{
			if ((_skillLvl % 100) == 0)
			{
				_skillLvl = s.getBaseLevel();
			}
			
			final int reqItemId;
			if (player.getClassId().level() == 3)
			{
				reqItemId = EnchantSkillGroupsData.UNTRAIN_ENCHANT_BOOK_OLD;
			}
			else
			{
				reqItemId = EnchantSkillGroupsData.UNTRAIN_ENCHANT_BOOK;
			}
			
			final int beforeUntrainSkillLevel = player.getSkillLevel(_skillId);
			if (((beforeUntrainSkillLevel - 1) != _skillLvl) && (((beforeUntrainSkillLevel % 100) != 1) || (_skillLvl != s.getBaseLevel())))
			{
				return;
			}
			
			EnchantSkillHolder esd = s.getEnchantSkillHolder(beforeUntrainSkillLevel);
			
			int requiredSp = esd.getSpCost();
			int requireditems = esd.getAdenaCost();
			
			L2ItemInstance spb = player.getInventory().getItemByItemId(reqItemId);
			if (Config.ES_SP_BOOK_NEEDED)
			{
				if (spb == null)
				{
					player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
					return;
				}
			}
			
			if (player.getInventory().getAdena() < requireditems)
			{
				player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
				return;
			}
			
			boolean check = true;
			if (Config.ES_SP_BOOK_NEEDED)
			{
				check &= player.destroyItem("Consume", spb.getObjectId(), 1, player, true);
			}
			
			check &= player.destroyItemByItemId("Consume", Inventory.ADENA_ID, requireditems, player, true);
			
			if (!check)
			{
				player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
				return;
			}
			
			player.getStat().addSp((int) (requiredSp * 0.8));
			
			if (Config.LOG_SKILL_ENCHANTS)
			{
				LogRecord record = new LogRecord(Level.INFO, "Untrain");
				record.setParameters(new Object[]
				{
					player,
					skill,
					spb
				});
				record.setLoggerName("skill");
				_logEnchant.log(record);
			}
			
			player.addSkill(skill, true);
			player.sendPacket(ExEnchantSkillResult.valueOf(true));
			
			if (Config.DEBUG)
			{
				_log.fine("Learned skill ID: " + _skillId + " Level: " + _skillLvl + " for " + requiredSp + " SP, " + requireditems + " Adena.");
			}
			
			player.sendPacket(new UserInfo(player));
			
			if (_skillLvl > 100)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.UNTRAIN_OF_ENCHANT_SKILL_WAS_SUCCESSFUL_CURRENT_LEVEL_OF_ENCHANT_SKILL_S1_HAS_BEEN_DECREASED_BY_1);
				sm.addSkillName(_skillId);
				player.sendPacket(sm);
			}
			else
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.UNTRAIN_OF_ENCHANT_SKILL_WAS_SUCCESSFUL_CURRENT_LEVEL_OF_ENCHANT_SKILL_S1_BECAME_0_AND_ENCHANT_SKILL_WILL_BE_INITIALIZED);
				sm.addSkillName(_skillId);
				player.sendPacket(sm);
			}
			player.sendSkillList();
			final int afterUntrainSkillLevel = player.getSkillLevel(_skillId);
			player.sendPacket(new ExEnchantSkillInfo(_skillId, afterUntrainSkillLevel));
			player.sendPacket(new ExEnchantSkillInfoDetail(2, _skillId, afterUntrainSkillLevel - 1, player));
			player.updateShortCuts(_skillId, afterUntrainSkillLevel);
		}
		else if (_type == 3) // change route
		{
			final int reqItemId;
			if (player.getClassId().level() == 3)
			{
				reqItemId = EnchantSkillGroupsData.CHANGE_ENCHANT_BOOK_OLD;
			}
			else
			{
				reqItemId = EnchantSkillGroupsData.CHANGE_ENCHANT_BOOK;
			}
			
			final int beforeEnchantSkillLevel = player.getSkillLevel(_skillId);
			if (beforeEnchantSkillLevel <= 100)
			{
				return;
			}
			
			int currentEnchantLevel = beforeEnchantSkillLevel % 100;
			if (currentEnchantLevel != (_skillLvl % 100))
			{
				return;
			}
			EnchantSkillHolder esd = s.getEnchantSkillHolder(_skillLvl);
			
			int requiredSp = esd.getSpCost();
			int requireditems = esd.getAdenaCost();
			
			if (player.getSp() >= requiredSp)
			{
				L2ItemInstance spb = player.getInventory().getItemByItemId(reqItemId);
				if (Config.ES_SP_BOOK_NEEDED)
				{
					if (spb == null)
					{
						player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_SKILL_ROUTE_CHANGE);
						return;
					}
				}
				
				if (player.getInventory().getAdena() < requireditems)
				{
					player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
					return;
				}
				
				boolean check;
				check = player.getStat().removeExpAndSp(0, requiredSp, false);
				if (Config.ES_SP_BOOK_NEEDED)
				{
					check &= player.destroyItem("Consume", spb.getObjectId(), 1, player, true);
				}
				
				check &= player.destroyItemByItemId("Consume", Inventory.ADENA_ID, requireditems, player, true);
				
				if (!check)
				{
					player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
					return;
				}
				
				int levelPenalty = Rnd.get(Math.min(4, currentEnchantLevel));
				_skillLvl -= levelPenalty;
				if ((_skillLvl % 100) == 0)
				{
					_skillLvl = s.getBaseLevel();
				}
				
				if (Config.LOG_SKILL_ENCHANTS)
				{
					LogRecord record = new LogRecord(Level.INFO, "Route Change");
					record.setParameters(new Object[]
					{
						player,
						skill,
						spb
					});
					record.setLoggerName("skill");
					_logEnchant.log(record);
				}
				
				player.addSkill(skill, true);
				player.sendPacket(ExEnchantSkillResult.valueOf(true));
				
				if (Config.DEBUG)
				{
					_log.fine("Learned skill ID: " + _skillId + " Level: " + _skillLvl + " for " + requiredSp + " SP, " + requireditems + " Adena.");
				}
				
				player.sendPacket(new UserInfo(player));
				
				if (levelPenalty == 0)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.ENCHANT_SKILL_ROUTE_CHANGE_WAS_SUCCESSFUL_LV_OF_ENCHANT_SKILL_S1_WILL_REMAIN);
					sm.addSkillName(_skillId);
					player.sendPacket(sm);
				}
				else
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.ENCHANT_SKILL_ROUTE_CHANGE_WAS_SUCCESSFUL_LV_OF_ENCHANT_SKILL_S1_HAS_BEEN_DECREASED_BY_S2);
					sm.addSkillName(_skillId);
					
					if (_skillLvl > 100)
					{
						sm.addInt(_skillLvl % 100);
					}
					else
					{
						sm.addInt(0);
					}
					player.sendPacket(sm);
				}
				player.sendSkillList();
				final int afterEnchantSkillLevel = player.getSkillLevel(_skillId);
				player.sendPacket(new ExEnchantSkillInfo(_skillId, afterEnchantSkillLevel));
				player.sendPacket(new ExEnchantSkillInfoDetail(3, _skillId, afterEnchantSkillLevel, player));
				player.updateShortCuts(_skillId, afterEnchantSkillLevel);
			}
			else
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
				player.sendPacket(sm);
			}
		}
		else if (_type == 4) // 100% enchant
		{
			int reqItemId = EnchantSkillGroupsData.IMMORTAL_SCROLL;
			final int beforeEnchantSkillLevel = player.getSkillLevel(_skillId);
			if (beforeEnchantSkillLevel != s.getMinSkillLevel(_skillLvl))
			{
				return;
			}
			
			L2ItemInstance spb = player.getInventory().getItemByItemId(reqItemId);
			if (spb == null)
			{
				player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
				return;
			}
			
			player.destroyItem("Consume", spb.getObjectId(), 1, player, true);
			
			if (Config.LOG_SKILL_ENCHANTS)
			{
				LogRecord record = new LogRecord(Level.INFO, "100% Success");
				record.setParameters(new Object[]
				{
					player,
					skill,
					spb,
					100
				});
				record.setLoggerName("skill");
				_logEnchant.log(record);
			}
			
			player.addSkill(skill, true);
			
			if (Config.DEBUG)
			{
				_log.fine("Learned skill ID: " + _skillId + " Level: " + _skillLvl + ".");
			}
			
			player.sendPacket(ExEnchantSkillResult.valueOf(true));
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SKILL_ENCHANT_WAS_SUCCESSFUL_S1_HAS_BEEN_ENCHANTED);
			sm.addSkillName(_skillId);
			player.sendPacket(sm);
			player.sendPacket(new UserInfo(player));
			player.sendSkillList();
			final int afterEnchantSkillLevel = player.getSkillLevel(_skillId);
			player.sendPacket(new ExEnchantSkillInfo(_skillId, afterEnchantSkillLevel));
			player.sendPacket(new ExEnchantSkillInfoDetail(1, _skillId, afterEnchantSkillLevel + 1, player));
			player.updateShortCuts(_skillId, afterEnchantSkillLevel);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_0F_REQUESTEXENCHANTSKILL;
	}
}