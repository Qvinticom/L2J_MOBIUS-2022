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
package org.l2jmobius.gameserver.network.clientpackets;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.xml.EnchantSkillGroupsData;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.model.EnchantSkillGroup.EnchantSkillHolder;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.EnchantSkillLearn;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExBrExtraUserInfo;
import org.l2jmobius.gameserver.network.serverpackets.ExEnchantSkillInfo;
import org.l2jmobius.gameserver.network.serverpackets.ExEnchantSkillInfoDetail;
import org.l2jmobius.gameserver.network.serverpackets.ExEnchantSkillResult;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;

/**
 * Format (ch) dd c: (id) 0xD0 h: (subid) 0x34 d: skill id d: skill level
 * @author -Wooden-
 */
public class RequestExEnchantSkillRouteChange implements IClientIncomingPacket
{
	private static final Logger LOGGER_ENCHANT = Logger.getLogger("enchant.skills");
	
	private int _skillId;
	private int _skillLevel;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_skillId = packet.readD();
		_skillLevel = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		if ((_skillId <= 0) || (_skillLevel <= 0))
		{
			return;
		}
		
		final Player player = client.getPlayer();
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
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THE_SKILL_ENHANCING_FUNCTION_ON_THIS_LEVEL_YOU_CAN_USE_THE_CORRESPONDING_FUNCTION_ON_LEVELS_HIGHER_THAN_76LV);
			return;
		}
		
		if (!player.isAllowedToEnchantSkills())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THE_SKILL_ENHANCING_FUNCTION_IN_THIS_CLASS_YOU_CAN_USE_THE_SKILL_ENHANCING_FUNCTION_UNDER_OFF_BATTLE_STATUS_AND_CANNOT_USE_THE_FUNCTION_WHILE_TRANSFORMING_BATTLING_AND_ON_BOARD);
			return;
		}
		
		Skill skill = SkillData.getInstance().getSkill(_skillId, _skillLevel);
		if (skill == null)
		{
			return;
		}
		
		final int reqItemId = EnchantSkillGroupsData.CHANGE_ENCHANT_BOOK;
		final EnchantSkillLearn s = EnchantSkillGroupsData.getInstance().getSkillEnchantmentBySkillId(_skillId);
		if (s == null)
		{
			return;
		}
		
		final int beforeEnchantSkillLevel = player.getSkillLevel(_skillId);
		// do you have this skill enchanted?
		if (beforeEnchantSkillLevel <= 100)
		{
			return;
		}
		
		final int currentEnchantLevel = beforeEnchantSkillLevel % 100;
		// is the requested level valid?
		if (currentEnchantLevel != (_skillLevel % 100))
		{
			return;
		}
		final EnchantSkillHolder esd = s.getEnchantSkillHolder(_skillLevel);
		final int requiredSp = esd.getSpCost();
		final int requireditems = esd.getAdenaCost();
		if (player.getSp() >= requiredSp)
		{
			// only first level requires book
			final Item spb = player.getInventory().getItemByItemId(reqItemId);
			// does not have spellbook
			if (Config.ES_SP_BOOK_NEEDED && (spb == null))
			{
				player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_SKILL_ROUTE_CHANGE);
				return;
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
			
			final int levelPenalty = Rnd.get(Math.min(4, currentEnchantLevel));
			_skillLevel -= levelPenalty;
			if ((_skillLevel % 100) == 0)
			{
				_skillLevel = s.getBaseLevel();
			}
			
			skill = SkillData.getInstance().getSkill(_skillId, _skillLevel);
			if (skill != null)
			{
				if (Config.LOG_SKILL_ENCHANTS)
				{
					LOGGER_ENCHANT.log(Level.INFO, "Route Change:" + player.getName() + " [" + player.getObjectId() + "] Account:" + player.getAccountName() + " IP:" + player.getIPAddress() + ", Skill:" + skill + ", SPB:" + spb);
				}
				
				player.addSkill(skill, true);
				player.sendPacket(ExEnchantSkillResult.valueOf(true));
			}
			
			player.sendPacket(new UserInfo(player));
			player.sendPacket(new ExBrExtraUserInfo(player));
			if (levelPenalty == 0)
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.ENCHANT_SKILL_ROUTE_CHANGE_WAS_SUCCESSFUL_LV_OF_ENCHANT_SKILL_S1_WILL_REMAIN);
				sm.addSkillName(_skillId);
				player.sendPacket(sm);
			}
			else
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.ENCHANT_SKILL_ROUTE_CHANGE_WAS_SUCCESSFUL_LV_OF_ENCHANT_SKILL_S1_HAS_BEEN_DECREASED_BY_S2);
				sm.addSkillName(_skillId);
				sm.addInt(levelPenalty);
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
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL));
		}
	}
}
